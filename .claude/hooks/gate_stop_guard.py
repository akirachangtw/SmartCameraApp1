#!/usr/bin/env python3
"""gate_stop_guard.py - Stop hook for the Autonomous Engineering Gate (ADV-GATE-04).

Only brings an unqualified stop back to the Gate Router
(.claude/rules/ssot-governance.md, "Autonomous Engineering Gate"). It decides
nothing about QA PASS, product semantics, authority or "executable": checkboxes,
keywords, receipts, commits and mtimes are clues, never verdicts.

Reads : payload (stdin JSON), tmp/verify/active.json, TICKET_REGISTRY.json (file),
        `gate_ledger.py status --json` (public CLI), transcript (turn start / user text).
Writes: only through the public CLI (`blocks`, `missed-check`, `human NO_PROGRESS`)
        plus marker files under tmp/.stop_guard/ (git-ignored, per session x prompt).
Never : notifies, imports gate_ledger internals, reads the raw ledger, retries a failed
        ledger write, or fabricates events. Any unexpected error, a missing
        payload field or the internal deadline means "allow" (fail-open).

Output: allow -> no stdout; block -> {"decision": "block", "reason": ...}; exit 0.
"""
from __future__ import annotations

import glob
import json
import os
import re
import subprocess
import sys
import time
from datetime import datetime, timezone
from pathlib import Path

DEADLINE_SECONDS = 20.0      # internal limit; the settings.json timeout must be larger
K_NO_PROGRESS = 6            # this guard's own NO_PROGRESS budget (K <= 7)
TERMINAL = ("REVIEW", "CLOSED", "VOID")
EXCLUDED_LIFECYCLES = ("BLOCKED",)   # outside this Gate's scope; not a terminal state
TICKET_RE = re.compile(r"^[A-Z][A-Z0-9]*(-[A-Z0-9]+)+$")
REGISTRY_REL = ("plans", "cso_briefing", "project", "TICKET_REGISTRY.json")
PENDING_REL = ("plans", "cso_briefing", "project", "Pending")
MARKER_DIR_REL = ("tmp", ".stop_guard")
CANCEL_RE = re.compile(r"^(請)?(停止|取消|中止|先停|先到這|算了|stop|cancel|abort)(吧|一下)?$", re.IGNORECASE)
QUESTION_PREFIXES = ("為什麼", "請說明", "解釋", "如何")
# Explicit current-turn tool restriction ("don't use tools"): narrow constants, wrong-guess direction = block less.
TOOL_LIMIT_RE = re.compile(
    r"(不要|請勿|禁止|不得|不可|不|別)(再)?(使用|用|呼叫)(任何|所有)?工具"
    r"|(do not|don't|dont|without)\s+(use\s+|using\s+)?(any\s+)?tools?|no tools", re.IGNORECASE)
HUMAN_MARKS = ("[實機]", "DEFERRED-TO-DOCSYNC", "N_A", "豁免", "Akira", "Traycer", "人工")
BOUNDARY = ("不得繞過 BLOCKED、Hard Constraint 或 permissions；停止某個未授權操作不等於停止其他合法路徑；"
            "Advisor／研究結果不構成新增授權。")


class Deadline(Exception):
    pass


class LedgerFail(Exception):
    def __init__(self, detail: str):
        super().__init__(detail)
        self.detail = detail


class Ctx:
    def __init__(self, payload: dict, started: float):
        self.payload = payload
        self.started = started
        self.session = _safe(str(payload.get("session_id")))
        self.prompt = _safe(str(payload.get("prompt_id")))
        self.root = None
        self.ticket = None

    def remaining(self) -> float:
        left = DEADLINE_SECONDS - (time.monotonic() - self.started)
        if left <= 0.5:
            raise Deadline()
        return left


def _safe(text: str) -> str:
    return re.sub(r"[^A-Za-z0-9_.-]", "_", text)


# --------------------------------------------------------------------------- io helpers
def run(ctx: Ctx, argv, cwd) -> subprocess.CompletedProcess:
    try:
        return subprocess.run(argv, cwd=str(cwd), capture_output=True, text=True, encoding="utf-8",
                              errors="replace", timeout=min(ctx.remaining(), 10.0))
    except subprocess.TimeoutExpired as exc:
        raise Deadline() from exc


def locate_root(ctx: Ctx):
    cwd = ctx.payload.get("cwd")
    res = run(ctx, ["git", "-C", str(cwd), "rev-parse", "--git-common-dir"], cwd)
    if res.returncode != 0 or not res.stdout.strip():
        return None
    common = Path(res.stdout.strip())
    if not common.is_absolute():
        common = Path(str(cwd)) / common
    return common.resolve().parent


def read_json(path: Path):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, ValueError):
        return None


def registry_lifecycle(root: Path, ticket: str):
    data = read_json(root.joinpath(*REGISTRY_REL))
    if not isinstance(data, dict):
        return None
    for item in data.get("tickets", []):
        if isinstance(item, dict) and item.get("id") == ticket:
            return item.get("lifecycle")
    return None


def parse_ts(text):
    try:
        return datetime.fromisoformat(str(text).replace("Z", "+00:00")).timestamp()
    except (ValueError, TypeError):
        return None


def turn_info(payload: dict):
    """(turn_start_epoch|None, user_text|None) from the transcript line(s) of this prompt_id."""
    path, prompt = payload.get("transcript_path"), payload.get("prompt_id")
    if not path or not prompt:
        return None, None
    start, text = None, None
    try:
        with open(path, encoding="utf-8") as handle:
            for raw in handle:
                try:
                    line = json.loads(raw)
                except ValueError:
                    continue
                if line.get("type") != "user" or line.get("promptId") != prompt:
                    continue
                content = (line.get("message") or {}).get("content")
                if isinstance(content, str):
                    parts = [content]
                elif isinstance(content, list):
                    parts = [c.get("text", "") for c in content if isinstance(c, dict) and c.get("type") == "text"]
                else:
                    parts = []
                joined = "\n".join(p for p in parts if p).strip()
                if not joined:
                    continue
                ts = parse_ts(line.get("timestamp"))
                if start is None or (ts is not None and ts < start):
                    start = ts
                if text is None:
                    text = joined
    except OSError:
        return None, None
    return start, text


def is_cancel(text: str) -> bool:
    norm = re.sub(r"[。.!！~～\s]+$", "", (text or "").strip())
    return 0 < len(norm) <= 12 and not norm.startswith("/") and bool(CANCEL_RE.match(norm))


def is_discussion(text: str) -> bool:
    norm = (text or "").strip()
    if not norm or norm.startswith("/"):
        return False
    return norm.endswith(("？", "?")) or norm.startswith(QUESTION_PREFIXES)


def is_tool_restriction(text: str) -> bool:
    return bool(TOOL_LIMIT_RE.search(text or ""))


# --------------------------------------------------------------------------- markers
def marker(ctx: Ctx, suffix: str) -> Path:
    return ctx.root.joinpath(*MARKER_DIR_REL) / f"{ctx.session}_{ctx.prompt}.{suffix}"


def marker_exists(ctx: Ctx, suffix: str) -> bool:
    return marker(ctx, suffix).is_file()


def marker_write(ctx: Ctx, suffix: str, content: str = "") -> bool:
    try:
        path = marker(ctx, suffix)
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding="utf-8")
        return True
    except OSError:
        return False


# --------------------------------------------------------------------------- ledger CLI
def ledger_cli(ctx: Ctx, *args) -> subprocess.CompletedProcess:
    script = ctx.root / ".claude" / "scripts" / "gate_ledger.py"
    return run(ctx, [sys.executable, str(script), *args, "--ticket", ctx.ticket], ctx.root)


def ledger_write(ctx: Ctx, *args) -> None:
    """Public-CLI write. Any non-zero exit (5 and 6 included) is a ledger failure: no retry."""
    res = ledger_cli(ctx, *args)
    if res.returncode != 0:
        raise LedgerFail(f"gate_ledger.py {' '.join(args)} -> exit {res.returncode}: {res.stderr.strip()[:300]}")


def ledger_status(ctx: Ctx):
    """Returns the status dict, None when the ticket identity/location makes the guard moot."""
    res = ledger_cli(ctx, "status", "--json")
    if res.returncode in (3, 4):
        return None
    if res.returncode != 0:
        raise LedgerFail(f"gate_ledger.py status exit {res.returncode}: {res.stderr.strip()[:300]}")
    try:
        return json.loads(res.stdout)
    except ValueError as exc:
        raise LedgerFail(f"gate_ledger.py status returned unparsable output: {exc}") from exc


# --------------------------------------------------------------------------- ticket file
def ticket_file(root: Path, ticket: str):
    base = root.joinpath(*PENDING_REL)
    try:
        hits = [p for p in base.iterdir() if p.is_file() and p.suffix == ".md"
                and (p.name == f"{ticket}.md" or p.name.startswith(f"{ticket}_"))]
    except OSError:
        return None
    return hits[0] if len(hits) == 1 else None


def scan_ticket(path):
    """(unchecked non-human-marked lines, checked count) over Action Items / Acceptance Criteria."""
    unchecked, checked, section, heading = [], 0, None, ""
    if path is None:
        return unchecked, checked
    try:
        lines = path.read_text(encoding="utf-8").splitlines()
    except OSError:
        return unchecked, checked
    for line in lines:
        if line.startswith("## "):
            title = line[3:].strip()
            section = title if title in ("Action Items", "Acceptance Criteria") else None
            continue
        if line.startswith("### "):
            heading = line[4:].split(":")[0].strip()
        if section is None:
            continue
        stripped = line.lstrip()
        if stripped.startswith("- [x]") or stripped.startswith("- [X]"):
            checked += 1
        elif stripped.startswith("- [ ]") and not any(mark in line for mark in HUMAN_MARKS):
            unchecked.append(f"{heading}: 未完成" if stripped == "- [ ] 完成" else stripped[:120])
    return unchecked, checked


# --------------------------------------------------------------------------- evidence (O2)
def evidence_times(ctx: Ctx, turn_start: float) -> dict:
    out = {}
    stamps = []
    for path in glob.glob(str(ctx.root / "tmp" / ctx.ticket / "evidence" / "step0_*.txt")):
        with_ts = _mtime(path)
        if with_ts is not None and with_ts >= turn_start:
            stamps.append(with_ts)
    if stamps:
        out["qa"] = min(stamps)
    res = run(ctx, ["git", "-C", str(ctx.root), "log", "-n", "50", "--format=%ct%x09%s"], ctx.root)
    if res.returncode == 0:
        commits = []
        for row in res.stdout.splitlines():
            ct, _, subject = row.partition("\t")
            if subject.startswith(f"[{ctx.ticket}]") and ct.isdigit() and int(ct) >= turn_start:
                commits.append(int(ct))
        if commits:
            out["commit"] = min(commits)
    stamps = []
    for path in glob.glob(str(ctx.root / ".tmp" / "sv_report_*.md")):
        with_ts = _mtime(path)
        if with_ts is not None and with_ts >= turn_start:
            stamps.append(with_ts)
    if stamps:
        out["sv"] = min(stamps)
    return out


def _mtime(path):
    try:
        return os.path.getmtime(path)
    except OSError:
        return None


def missed_gate_points(ctx: Ctx, st: dict, turn_start):
    """Gate points whose progress evidence has no earlier (or late) receipt. UNKNOWN -> skipped."""
    if turn_start is None:
        return []
    receipts = st.get("receipts") or []
    window_full = len(receipts) >= 5
    missed = []
    for gate, ev_time in evidence_times(ctx, turn_start).items():
        mine = [r for r in receipts if r.get("gate_point") == gate]
        stamped = [(parse_ts(r.get("ts")), r) for r in mine]
        if any(ts is not None and ts <= ev_time for ts, _ in stamped):
            continue
        if any(ts is not None and ts > ev_time and r.get("late") for ts, r in stamped):
            continue
        if not mine and window_full:
            continue          # receipt window saturated: cannot tell absent from pushed out -> UNKNOWN
        missed.append(gate)
    return missed


def qa_without_sv_clue(st: dict) -> bool:
    receipts = st.get("receipts") or []
    if len(receipts) >= 5:
        return False          # window saturated -> UNKNOWN, no clue
    qa = [r.get("seq") for r in receipts if r.get("gate_point") == "qa"]
    sv = [r.get("seq") for r in receipts if r.get("gate_point") == "sv"]
    return bool(qa) and (not sv or max(sv) < max(qa))


# --------------------------------------------------------------------------- progress vector
def progress_vector(st: dict, checked: int):
    incidents = st.get("incidents") or []
    closed = sum(1 for i in incidents if i.get("status") == "closed")
    ended = sum(int(i.get("rounds_started", 0)) - (1 if i.get("round_open") else 0) for i in incidents)
    hashes = {read.get("sha256") for rec in (st.get("ppss") or []) for read in (rec.get("reads") or [])
              if read.get("sha256")}
    return [closed, ended, int(checked), len(hashes)]


def load_seq(ctx: Ctx):
    try:
        data = json.loads(marker(ctx, "seq").read_text(encoding="utf-8"))
        return int(data["count"]), [int(x) for x in data["hw"]]
    except (OSError, ValueError, KeyError, TypeError):
        return None


# --------------------------------------------------------------------------- reasons
LAYER_NAMES = ("executor", "r0", "advisor")


def layer_stat(inc: dict, name: str):
    """(started, failed) of one known layer; a missing `layers` field reads as (0, 0)."""
    bucket = (inc.get("layers") or {}).get(name) or {}
    return int(bucket.get("started", 0)), int(bucket.get("failed", 0))


def exhausted_note(inc: dict) -> str:
    """Pointer to the next layer when a lower layer is spent and Advisor is not engaged yet."""
    if layer_stat(inc, "advisor")[0] > 0:
        return ""
    if layer_stat(inc, "r0")[1] >= 3:
        return ("；r0 層已耗盡：不得再要求跑已耗盡的 R0；必須升 Advisor（`consult --kind advisor` 後 "
                "`round start --layer advisor`）；不得僅因 r0 耗盡而升人類")
    if layer_stat(inc, "executor")[1] >= 3:
        return ("；executor 層已耗盡：必須升下一適用層（cross-review 路徑先 R0，否則 Advisor）；"
                "尚未 engaged 的 Advisor 不得視為已處理")
    return ""


def incident_lines(st: dict, incidents) -> list:
    ppss = st.get("ppss") or []
    lines = []
    for inc in incidents:
        used_ppss = sum(1 for rec in ppss if rec.get("incident") == inc["id"])
        advisor = (inc.get("consults") or {}).get("advisor", 0)
        stats = "｜".join(f"{name} {layer_stat(inc, name)[0]}/3（失敗 {layer_stat(inc, name)[1]}）"
                          for name in LAYER_NAMES)
        lines.append(f"- {inc['id']}（{', '.join(inc.get('subjects') or [])}）：{stats}；"
                     f"Advisor 已用 {advisor}；PPSS 已用 {used_ppss}" + exhausted_note(inc))
    return lines


ROUTER = ("依 Gate 順序走 Router：R1 已授權可做者直接做並驗證；R2 未知先本地取證（`consult --kind local`）；"
          "R3 仍不能判定下一個安全行動才取得 Advisor（來源、核對與記帳依 Gate「Advisor 與 PPSS」條文，一律經入口 `route.py consult`；`consult --kind advisor` 由入口寫入；本層不確定即可升層，"
          "不必先用完本層輪數）；R4 缺必要外部事實才用 PPSS（經入口 `route.py consult --source research.ppss`：派發→`research_submit`→record／read）；"
          "R5 PPSS 證據回流同一 incident，於後續 round 策略引用並消費；同缺口不重送、不新增研究額度；"
          "R6 以新且已授權策略 `round start`（`--layer` 依策略來源層；advisor 層首輪前須有同 incident "
          "`consult --kind advisor`）→修復→局部驗證→`round end`。")


def build_reason(ctx: Ctx, st: dict, router_incidents, escalate_incidents, missed, other_pending) -> str:
    parts = [f"[gate_stop_guard] {ctx.ticket}：目前停止不合格，帶回 Gate Router。"]
    if escalate_incidents:
        parts.append("以下 incident 的 advisor 層已用滿 3 輪失敗，**僅允許升報**：執行 `python .claude/scripts/gate_ledger.py human "
                     "--category EXHAUSTED --incident <INC> --basis layer_advisor_3` 並輸出 `HUMAN_REQUIRED` 單行；"
                     "不提出或執行第四輪、不再修復／研究／諮詢／重試該 incident。")
        parts.extend(incident_lines(st, escalate_incidents))
    if router_incidents:
        parts.append("以下 incident 仍有未結義務：" + ROUTER)
        parts.extend(incident_lines(st, router_incidents))
    if other_pending:
        parts.append("另有 incident 已升報待人工（" + "、".join(other_pending) + "），其阻塞範圍僅限該 incident；其餘義務仍須先完成。")
    if missed:
        parts.append("本回合已見推進證據但 ledger 缺較早的 `check` receipt（" + "、".join(missed) + "）：先核對當前狀態"
                     "（git status／HEAD／`status`），再對下一個動作呼叫 `check`；補跑 receipt 標 `--late`，"
                     "不得當作事前放行。")
    parts.append(BOUNDARY)
    return "\n".join(parts)


def build_check_reason(ctx: Ctx, unchecked, qa_clue: bool, local_pending: bool) -> str:
    parts = [f"[gate_stop_guard] {ctx.ticket}：停止前請核對並如實處理或說明（本提醒每個 prompt 只出現一次，hook 不裁定結論）。"]
    if unchecked:
        parts.append("票檔尚有未勾項線索：" + "；".join(unchecked[:5]) + ("…" if len(unchecked) > 5 else "")
                     + "。請判斷哪些已授權且可由 agent 執行（續作）、哪些屬人工／豁免／延後（如實說明）。")
    if qa_clue:
        parts.append("ledger 有 `qa` receipt 而其後無 `sv` receipt：請核對 `/qa` 最終結果與本票義務——"
                     "QA PASS 且本票要求 SV 則續接 `/sv`；FAIL 則依 Gate 記 finding 並自修／取證；"
                     "RAPID 豁免或票面不要求 SV 則如實說明後可停止。")
    if local_pending:
        parts.append("部分 incident 已升報待人工；請判斷上述項目是否與其無關，無關者續作。")
    parts.append(BOUNDARY)
    return "\n".join(parts)


# --------------------------------------------------------------------------- decision
def ledger_failure_reason(ctx: Ctx, detail: str):
    """First failure per prompt blocks once; marker present or unwritable -> allow."""
    if marker_exists(ctx, "ledgerfail"):
        return None
    if not marker_write(ctx, "ledgerfail", detail):
        return None
    return (f"[gate_stop_guard] {ctx.ticket}：ledger 讀寫失敗，狀態未知（不得當作無 open incident）。原始錯誤：{detail}\n"
            "請貼出此原始錯誤；不要補造任何 ledger 事件。若底層義務存在，繼續對應的 Router 步驟；"
            "若確屬需人工的情形，依 Gate 以實際類別如實輸出一行 `HUMAN_REQUIRED` 並註明 ledger 寫入失敗。\n" + BOUNDARY)


def decide(ctx: Ctx):
    payload = ctx.payload
    if not all(payload.get(k) for k in ("session_id", "prompt_id", "cwd")):
        return None
    ctx.root = locate_root(ctx)
    if ctx.root is None:
        return None
    active = read_json(ctx.root / "tmp" / "verify" / "active.json")
    ticket = active.get("ticket_id") if isinstance(active, dict) else None
    if not isinstance(ticket, str) or not TICKET_RE.match(ticket):
        return None
    owner = active.get("owner_session_id")
    if not isinstance(owner, str) or not owner or owner != payload.get("session_id"):
        return None                                   # owner-only: absent / empty / other session -> allow, no ledger or marker touch
    ctx.ticket = ticket
    lifecycle = registry_lifecycle(ctx.root, ticket)
    if lifecycle is None or lifecycle in TERMINAL or lifecycle in EXCLUDED_LIFECYCLES:
        return None
    try:
        st = ledger_status(ctx)
    except LedgerFail as exc:
        return ledger_failure_reason(ctx, exc.detail)
    if st is None or st.get("ticket_location") != "loc:pending":
        return None

    turn_start, user_text = turn_info(payload)
    if user_text is not None and (is_cancel(user_text) or is_discussion(user_text) or is_tool_restriction(user_text)):
        return None

    pending = st.get("pending_human") or []
    if any(not p.get("incident") for p in pending):
        return None                                   # whole-ticket pending, trusted from helper
    pending_ids = {p.get("incident") for p in pending}
    incidents = st.get("incidents") or []
    open_incs = [i for i in incidents if i.get("status") == "open" and i["id"] not in pending_ids]
    escalate = [i for i in open_incs if layer_stat(i, "advisor")[1] >= 3]
    router = [i for i in open_incs if i not in escalate]

    try:
        missed = missed_gate_points(ctx, st, turn_start)
        for gate in missed:
            if not marker_exists(ctx, f"missed_{gate}"):
                ledger_write(ctx, "missed-check", "--gate-point", gate)
                marker_write(ctx, f"missed_{gate}")
        persistent = bool(open_incs or missed)
        if persistent:
            return persistent_block(ctx, st, router, escalate, missed, sorted(i for i in pending_ids if i))
    except LedgerFail as exc:
        return ledger_failure_reason(ctx, exc.detail)

    unchecked, _ = scan_ticket(ticket_file(ctx.root, ticket))
    qa_clue = qa_without_sv_clue(st)
    if (unchecked or qa_clue) and not marker_exists(ctx, "check"):
        if marker_write(ctx, "check"):
            return build_check_reason(ctx, unchecked, qa_clue, bool(pending_ids))
    return None


def persistent_block(ctx: Ctx, st: dict, router, escalate, missed, other_pending):
    _, checked = scan_ticket(ticket_file(ctx.root, ctx.ticket))
    vector = progress_vector(st, checked)
    previous = load_seq(ctx)
    if previous is None:
        count, high = 1, vector
    else:
        count, high = previous
        if any(v > h for v, h in zip(vector, high)):
            count, high = 0, [max(v, h) for v, h in zip(vector, high)]
        else:
            count += 1
    if count >= K_NO_PROGRESS:
        ledger_write(ctx, "human", "--category", "NO_PROGRESS", "--basis", f"no_progress_k{K_NO_PROGRESS}")
        marker_write(ctx, "seq", json.dumps({"count": count, "hw": high}))
        return None
    ledger_write(ctx, "blocks", "--note", f"p={ctx.prompt} n={count}")
    marker_write(ctx, "seq", json.dumps({"count": count, "hw": high}))
    return build_reason(ctx, st, router, escalate, missed, other_pending)


def main() -> int:
    for stream in (sys.stdout, sys.stderr):
        try:
            stream.reconfigure(encoding="utf-8")
        except (AttributeError, ValueError):
            pass
    try:
        payload = json.loads(sys.stdin.buffer.read().decode("utf-8", errors="replace"))
        if not isinstance(payload, dict):
            return 0
        reason = decide(Ctx(payload, time.monotonic()))
    except Exception:          # noqa: BLE001 - fail-open by contract (Deadline included)
        return 0
    if reason:
        print(json.dumps({"decision": "block", "reason": reason}, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    sys.exit(main())
