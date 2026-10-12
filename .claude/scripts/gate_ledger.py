#!/usr/bin/env python3
"""gate_ledger.py - append-only incident / round / PPSS-evidence ledger for the
Autonomous Engineering Gate (ADV-GATE-02).

Python 3, standard library only. No registry access, no network, no PPSS or
Advisor calls: the helper only records and checks what the agent reports.

Ledger : <git-common-dir>/gate/<TICKET_ID>.jsonl   (lock: same name + ".lock")
         One UTF-8 JSON object per line, append-only; every count and state is
         recovered by folding the events (no in-place rewrite).
Ticket : --ticket <ID> (explicit) > tmp/verify/active.json at the main checkout
         root > none (exit 3, never "unknown"). --ticket only selects the ledger
         key; it is not an activation and grants no authorization. This helper
         never writes active.json or verify_cmd.conf.
Location (conservative usability judgement from where the ticket file sits under
         plans/cso_briefing/project/, NOT an authoritative lifecycle):
         loc:pending | loc:deliver | loc:void | loc:none | loc:multi
         Only loc:pending is mutable. status is read-only and works for any
         location as long as a ledger exists.

Global option (accepted before or after the sub-command): --ticket ID

Sub-commands
  status [--json]
  incident open --subject TOK [--subject TOK ...] [--gate-point P] [--verdict V]
                [--same-root-cause INC] [--independent-evidence REF]
  round start --incident INC --expect N --strategy S --authority A
  round end   --incident INC --result pass|fail
  step-limit  --incident INC --source NAME [--note N]
  consult     --incident INC --kind local|advisor [--note N]
  strategy-scan --incident INC --item "<strategy>::<reason excluded>" [--item ...]
  ppss record --incident INC --artifact PATH [--domains d1,d2]
              [--new-query-strategy S --reason R]
  ppss read   --incident INC --request-id RID --url U --source-file F
              --supports <claim_id|none> [--quote Q]
  human --category C [--incident INC] --basis B
  human resolve --ref R [--incident INC]
  blocks --note N
  missed-check --gate-point P
  check <qa|commit|sv|closeout|done|resume> [--late]

Exit codes
  0 ok | 1 check: pending human decision | 2 usage error
  3 ticket identity unresolved | 4 ticket file location not loc:pending
  (deliver/void), missing or ambiguous | 5 contract refusal (CAS mismatch,
  repeated strategy, 4th round, missing prerequisite ...) | 6 ledger IO / lock
  failure

Lock: O_CREAT|O_EXCL lock file, acquire only. FileExistsError and the transient
Windows PermissionError are retried inside acquire until LOCK_TIMEOUT; the last
original error is kept in the LockTimeout message and as __cause__. PermissionError
anywhere else fails at once. A stale lock is reclaimed only when it is older than
STALE_SECONDS AND its owner pid is confirmed dead. Windows liveness uses
OpenProcess + GetExitCodeProcess (os.kill(pid, 0) is not a liveness query on
Windows: per the Python 3.14 docs any signal other than CTRL_C_EVENT /
CTRL_BREAK_EVENT terminates the target through TerminateProcess).
"""
from __future__ import annotations

import argparse
import contextlib
import hashlib
import json
import os
import random
import re
import subprocess
import sys
import time
from datetime import datetime, timezone
from pathlib import Path

EXIT_OK, EXIT_BLOCKED, EXIT_USAGE, EXIT_IDENT, EXIT_TICKET, EXIT_CONTRACT, EXIT_IO = range(7)

LOCK_TIMEOUT = 10.0
STALE_SECONDS = 30.0
MAX_ROUNDS = 3
STILL_ACTIVE = 259
ERROR_INVALID_PARAMETER = 87
ERROR_ACCESS_DENIED = 5

TICKET_RE = re.compile(r"^[A-Z][A-Z0-9]*(-[A-Z0-9]+)+$")
GATE_POINTS = ("qa", "commit", "sv", "closeout", "done", "resume")
IMMEDIATE = ("PERMISSION", "IRREVERSIBLE_SECURITY_COST", "HUMAN_GATE", "COST")
VERIFY_FIRST = ("SSOT_CONFLICT", "PRODUCT_DECISION", "SCOPE", "NO_AUTHORIZED_STRATEGY")
MECHANICAL = ("EXHAUSTED", "NO_PROGRESS")
CATEGORIES = IMMEDIATE + VERIFY_FIRST + MECHANICAL
PROJECT_DIR = ("plans", "cso_briefing", "project")
LOCATIONS = {"Pending": "loc:pending", "deliver": "loc:deliver", "void": "loc:void"}


class GateError(Exception):
    def __init__(self, code: int, msg: str):
        super().__init__(msg)
        self.code = code
        self.msg = msg


class LockTimeout(GateError):
    def __init__(self, msg: str):
        super().__init__(EXIT_IO, msg)


def now_iso() -> str:
    return datetime.now(timezone.utc).isoformat(timespec="seconds")


# --------------------------------------------------------------------------
# git / identity
# --------------------------------------------------------------------------
def run_git(args, cwd=None):
    try:
        res = subprocess.run(["git", *args], cwd=cwd, capture_output=True, text=True,
                             encoding="utf-8", errors="replace")
    except OSError:
        return None
    if res.returncode != 0:
        return None
    return res.stdout.strip()


def get_common_dir(cwd=None) -> Path:
    out = run_git(["rev-parse", "--git-common-dir"], cwd)
    if not out:
        raise GateError(EXIT_IO, "not inside a git repository (git rev-parse --git-common-dir failed)")
    p = Path(out)
    if not p.is_absolute():
        p = Path(cwd or os.getcwd()) / p
    return p.resolve()


def validate_ticket_id(tid: str) -> str:
    if not isinstance(tid, str) or not TICKET_RE.match(tid) or not re.search(r"[0-9]", tid):
        raise GateError(EXIT_IDENT, f"invalid ticket id: {tid!r}")
    return tid


def read_active_ticket(root: Path):
    path = root / "tmp" / "verify" / "active.json"
    if not path.is_file():
        return None
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
        return str(data["ticket_id"])
    except (OSError, ValueError, KeyError, TypeError) as exc:
        raise GateError(EXIT_IDENT, f"active.json unreadable: {exc!r}") from exc


def resolve_ticket(explicit, root: Path):
    """Return (ticket_id, source). --ticket wins; it is identity selection only."""
    if explicit:
        return validate_ticket_id(explicit), "explicit"
    active = read_active_ticket(root)
    if active:
        return validate_ticket_id(active), "active_json"
    raise GateError(EXIT_IDENT, "no ticket: pass --ticket or activate one (active.json absent)")


def ticket_location(root: Path, tid: str) -> str:
    base = root.joinpath(*PROJECT_DIR)
    hits = []
    for sub, label in LOCATIONS.items():
        d = base / sub
        if not d.is_dir():
            continue
        names = [p for p in d.iterdir()
                 if p.is_file() and p.suffix == ".md" and (p.name == f"{tid}.md" or p.name.startswith(f"{tid}_"))]
        hits.extend((label, n) for n in names)
    if not hits:
        return "loc:none"
    if len(hits) > 1:
        return "loc:multi"
    return hits[0][0]


# --------------------------------------------------------------------------
# process liveness (lock owner)
# --------------------------------------------------------------------------
def _kernel32():
    import ctypes
    from ctypes import wintypes
    k32 = ctypes.WinDLL("kernel32", use_last_error=True)
    k32.OpenProcess.argtypes = [wintypes.DWORD, wintypes.BOOL, wintypes.DWORD]
    k32.OpenProcess.restype = wintypes.HANDLE
    k32.GetExitCodeProcess.argtypes = [wintypes.HANDLE, ctypes.POINTER(wintypes.DWORD)]
    k32.GetExitCodeProcess.restype = wintypes.BOOL
    k32.CloseHandle.argtypes = [wintypes.HANDLE]
    k32.CloseHandle.restype = wintypes.BOOL
    return k32


def _win_pid_state(pid: int):
    import ctypes
    from ctypes import wintypes
    k32 = _kernel32()
    handle = k32.OpenProcess(0x1000, False, pid)  # PROCESS_QUERY_LIMITED_INFORMATION
    if not handle:
        err = ctypes.get_last_error()
        if err == ERROR_INVALID_PARAMETER:  # 87: no such process
            return False, f"OpenProcess: no such pid (last_error={err})"
        if err == ERROR_ACCESS_DENIED:  # 5: process exists but cannot be queried
            return None, f"OpenProcess: access denied, process may exist (last_error={err})"
        return None, f"OpenProcess failed (last_error={err})"
    try:
        code = wintypes.DWORD()
        ok = k32.GetExitCodeProcess(handle, ctypes.byref(code))
        if not ok:  # zero return = cannot decide; keep the last error, never reclaim
            return None, f"GetExitCodeProcess returned 0 (last_error={ctypes.get_last_error()})"
        if code.value == STILL_ACTIVE:
            return True, "exit code 259 (STILL_ACTIVE)"
        return False, f"exit code {code.value}"
    finally:
        k32.CloseHandle(handle)


def _pid_state(pid: int):
    """Return (alive, detail): True alive, False dead, None undecidable."""
    if pid <= 0:  # never probe 0 / negative pids (os.kill would signal a process group)
        return None, f"invalid pid {pid}"
    if os.name == "nt":
        return _win_pid_state(pid)
    try:
        os.kill(pid, 0)
    except ProcessLookupError:
        return False, "ESRCH"
    except PermissionError:
        return True, "EPERM"
    except OSError as exc:
        return None, repr(exc)
    return True, "signal 0 ok"


# --------------------------------------------------------------------------
# lock (acquire-only retry)
# --------------------------------------------------------------------------
def _maybe_reclaim(path: Path, stale: float, notes: list) -> bool:
    try:
        st = os.stat(path)
    except FileNotFoundError:
        return True
    except OSError as exc:
        notes.append(f"stat lock failed: {exc!r}")
        return False
    if time.time() - st.st_mtime <= stale:
        return False
    try:
        raw = path.read_text(encoding="utf-8")
    except OSError as exc:
        notes.append(f"read lock failed: {exc!r}")
        return False
    pid = None
    try:
        pid = int(json.loads(raw)["pid"])
    except (ValueError, KeyError, TypeError):
        pid = None
    if pid is not None and pid <= 0:  # invalid owner pid: ownerless, reclaimed by age alone
        notes.append(f"lock owner pid {pid} is invalid; treated as ownerless")
        pid = None
    if pid is not None:
        alive, detail = _pid_state(pid)
        if alive is not False:
            notes.append(f"owner pid {pid} not reclaimed: alive={alive} ({detail})")
            return False
    try:
        if os.stat(path).st_mtime_ns != st.st_mtime_ns:  # replaced meanwhile
            return False
        os.unlink(path)
    except FileNotFoundError:
        return True
    except OSError as exc:
        notes.append(f"unlink stale lock failed: {exc!r}")
        return False
    return True


def acquire_lock(path: Path, timeout: float = LOCK_TIMEOUT, stale: float = STALE_SECONDS) -> None:
    deadline = time.monotonic() + timeout
    notes: list = []
    last = None
    while True:
        try:
            fd = os.open(str(path), os.O_CREAT | os.O_EXCL | os.O_WRONLY)
        except FileExistsError as exc:
            last = exc
            _maybe_reclaim(path, stale, notes)
        except PermissionError as exc:  # transient delete-pending on Windows; bounded by deadline
            last = exc
        except OSError as exc:
            raise GateError(EXIT_IO, f"cannot create lock {path}: {exc!r}") from exc
        else:
            try:
                os.write(fd, json.dumps({"pid": os.getpid(), "ts": time.time()}).encode("utf-8"))
            except OSError:
                os.close(fd)
                with contextlib.suppress(OSError):
                    os.unlink(str(path))
                raise
            os.close(fd)
            return
        if time.monotonic() >= deadline:
            detail = f"{type(last).__name__}: {last!r}" + (f"; errno={getattr(last, 'errno', None)}"
                                                           f" winerror={getattr(last, 'winerror', None)}")
            tail = f"; {notes[-1]}" if notes else ""
            raise LockTimeout(f"lock {path} not acquired within {timeout}s; last error {detail}{tail}") from last
        time.sleep(random.uniform(0.005, 0.02))


def release_lock(path: Path) -> None:
    try:
        os.unlink(str(path))
    except FileNotFoundError:
        pass
    except OSError as exc:
        raise GateError(EXIT_IO, f"lock release failed (event already written): {exc!r}") from exc


# --------------------------------------------------------------------------
# ledger
# --------------------------------------------------------------------------
class Ledger:
    def __init__(self, common: Path, tid: str, lock_timeout: float = LOCK_TIMEOUT,
                 stale_seconds: float = STALE_SECONDS):
        self.dir = common / "gate"
        self.path = self.dir / f"{tid}.jsonl"
        self.lock_path = self.dir / f"{tid}.jsonl.lock"
        self.tid = tid
        self.lock_timeout = lock_timeout
        self.stale_seconds = stale_seconds

    def exists(self) -> bool:
        return self.path.is_file()

    def read(self, strict: bool = True) -> list:
        """strict (mutations): a damaged tail raises GateError(6) and the file is left untouched.
        lenient (status): a partial trailing line is ignored."""
        if not self.path.is_file():
            return []
        try:
            raw = self.path.read_bytes().decode("utf-8")
        except (OSError, ValueError) as exc:
            raise GateError(EXIT_IO, f"ledger read failed: {exc!r}") from exc
        if strict and raw and not raw.endswith("\n"):
            raise GateError(EXIT_IO, f"ledger {self.path} has an unterminated tail line; "
                                     "refusing to append (file left unchanged)")
        lines = raw.splitlines()
        events = []
        for i, line in enumerate(lines):
            if not line.strip():
                continue
            try:
                events.append(json.loads(line))
            except ValueError as exc:
                if i == len(lines) - 1 and not strict:  # partial trailing write, read-only view
                    break
                raise GateError(EXIT_IO, f"ledger corrupt at line {i + 1}") from exc
        return events

    @contextlib.contextmanager
    def locked(self):
        try:
            os.makedirs(self.dir, exist_ok=True)
        except OSError as exc:
            raise GateError(EXIT_IO, f"cannot create {self.dir}: {exc!r}") from exc
        acquire_lock(self.lock_path, self.lock_timeout, self.stale_seconds)
        try:
            yield
        finally:
            release_lock(self.lock_path)

    def append(self, last_seq: int, fields: dict) -> dict:
        ev = {"seq": last_seq + 1, "ts": now_iso(), "ticket": self.tid}
        ev.update(fields)
        self._write_line(json.dumps(ev, ensure_ascii=False))
        return ev

    def _write_line(self, line: str) -> None:
        try:
            with open(self.path, "a", encoding="utf-8", newline="\n") as fh:
                fh.write(line + "\n")
                fh.flush()
                os.fsync(fh.fileno())
        except OSError as exc:
            raise GateError(EXIT_IO, f"ledger append failed: {exc!r}") from exc


# --------------------------------------------------------------------------
# fold
# --------------------------------------------------------------------------
def _new_incident(iid: str) -> dict:
    return {"id": iid, "subjects": [], "status": "open", "rounds_started": 0, "results": [],
            "round_open": False, "strategies": [], "consults": {"local": 0, "advisor": 0},
            "scans": 0, "findings": 0, "step_limits": 0, "ppss_window": 0,
            "gate_points": [], "verdicts": []}


def fold(events: list) -> dict:
    st = {"incidents": {}, "order": [], "pending": [], "ppss": {}, "blocks": 0, "missed": 0,
          "receipts": [], "last_seq": 0}
    for e in events:
        st["last_seq"] = max(st["last_seq"], int(e.get("seq", 0)))
        t = e.get("type")
        inc = st["incidents"].get(e.get("incident"))
        if t == "finding":
            if e["new"]:
                inc = _new_incident(e["incident"])
                st["incidents"][inc["id"]] = inc
                st["order"].append(inc["id"])
            inc["findings"] += 1
            for s in e.get("subjects", []):
                if s not in inc["subjects"]:
                    inc["subjects"].append(s)
            inc["gate_points"].append(e.get("gate_point"))
            inc["verdicts"].append(e.get("verdict"))
        elif t == "round_start":
            inc["rounds_started"] += 1
            inc["round_open"] = True
            inc["strategies"].append(e["strategy_norm"])
        elif t == "round_end":
            inc["round_open"] = False
            inc["results"].append(e["result"])
            inc["ppss_window"] = 0
            if e["result"] == "pass":
                inc["status"] = "closed"
        elif t == "step_limit":
            inc["step_limits"] += 1
        elif t == "consult":
            inc["consults"][e["kind"]] += 1
        elif t == "strategy_scan":
            inc["scans"] += 1
        elif t == "ppss_record":
            inc["ppss_window"] += 1
            rid = e.get("request_id") or f"invalid-{e['seq']}"
            st["ppss"][rid] = {"request_id": rid, "incident": e["incident"], "state": e["state"],
                               "evidence_ok": False, "urls": e.get("urls", []),
                               "reason": e.get("reason_invalid"), "reads": []}
        elif t == "ppss_read":
            rec = st["ppss"][e["request_id"]]
            rec["state"] = "SOURCE_READ"
            rec["reads"].append({"url": e["url"], "sha256": e["source_sha256"], "supports": e["supports"]})
            rec["evidence_ok"] = rec["evidence_ok"] or bool(e["evidence_ok"])
        elif t == "human":
            st["pending"].append({"category": e["category"], "incident": e.get("incident"),
                                  "basis": e.get("basis"), "seq": e["seq"]})
        elif t == "human_resolve":
            gone = set(e.get("cleared", []))
            st["pending"] = [p for p in st["pending"] if p["seq"] not in gone]
        elif t == "blocks":
            st["blocks"] += 1
        elif t == "missed_check":
            st["missed"] += 1
        elif t == "check":
            st["receipts"].append({k: e.get(k) for k in
                                   ("seq", "ts", "gate_point", "ticket_source", "head", "exit", "late")})
    return st


def norm(text) -> str:
    return " ".join(str(text or "").split()).lower()


def pending_for(st: dict, incident_id) -> list:
    return [p for p in st["pending"] if p["incident"] == incident_id]


def get_incident(st: dict, iid) -> dict:
    inc = st["incidents"].get(iid) if iid else None
    if inc is None:
        raise GateError(EXIT_CONTRACT, f"unknown incident: {iid!r}")
    return inc


# --------------------------------------------------------------------------
# command context
# --------------------------------------------------------------------------
class Ctx:
    def __init__(self, args, mutation: bool):
        self.common = get_common_dir()
        self.root = self.common.parent
        self.tid, self.source = resolve_ticket(getattr(args, "ticket", None), self.root)
        self.location = ticket_location(self.root, self.tid)
        if mutation and self.location != "loc:pending":
            raise GateError(EXIT_TICKET, f"ticket {self.tid} is not mutable ({self.location})")
        self.ledger = Ledger(self.common, self.tid)


def emit(obj) -> None:
    print(json.dumps(obj, ensure_ascii=False))


def _need(value, name):
    if not str(value or "").strip():
        raise GateError(EXIT_CONTRACT, f"{name} is required and must be non-empty")
    return str(value).strip()


# --------------------------------------------------------------------------
# commands
# --------------------------------------------------------------------------
def summarize(ctx: Ctx, st: dict) -> dict:
    incidents = []
    for iid in st["order"]:
        i = st["incidents"][iid]
        incidents.append({"id": iid, "status": i["status"], "subjects": i["subjects"],
                          "rounds_started": i["rounds_started"],
                          "rounds_failed": i["results"].count("fail"),
                          "round_open": i["round_open"], "strategies": i["strategies"],
                          "pending_human": [p["category"] for p in pending_for(st, iid)],
                          "consults": i["consults"], "strategy_scans": i["scans"],
                          "findings": i["findings"], "step_limits": i["step_limits"]})
    return {"ticket": ctx.tid, "ticket_source": ctx.source, "ticket_location": ctx.location,
            "ledger_exists": ctx.ledger.exists(), "ledger_path": str(ctx.ledger.path),
            "last_seq": st["last_seq"], "incidents": incidents, "pending_human": st["pending"],
            "ppss": list(st["ppss"].values()), "blocks": st["blocks"], "missed_checks": st["missed"],
            "receipts": st["receipts"][-5:]}


def cmd_status(args) -> int:
    ctx = Ctx(args, mutation=False)
    if not ctx.ledger.exists() and ctx.location == "loc:none":
        raise GateError(EXIT_TICKET, f"no ledger and no ticket file for {ctx.tid} (loc:none)")
    st = fold(ctx.ledger.read(strict=False))
    out = summarize(ctx, st)
    out["stale"] = ctx.source == "active_json" and ctx.location != "loc:pending"
    if args.json:
        emit(out)
    else:
        print(f"ticket={out['ticket']} source={out['ticket_source']} location={out['ticket_location']}"
              f" stale={out['stale']} ledger={'yes' if out['ledger_exists'] else 'no'} last_seq={out['last_seq']}")
        for i in out["incidents"]:
            print(f"  {i['id']} {i['status']} rounds={i['rounds_started']}/{MAX_ROUNDS}"
                  f" failed={i['rounds_failed']} pending_human={i['pending_human']} subjects={i['subjects']}")
        for p in out["pending_human"]:
            print(f"  PENDING_HUMAN {p['category']} incident={p['incident']} basis={p['basis']}")
        print(f"  blocks={out['blocks']} missed_checks={out['missed_checks']}")
    return EXIT_OK


def cmd_incident_open(args) -> int:
    ctx = Ctx(args, mutation=True)
    subjects = [s for s in (norm(x) for x in (args.subject or [])) if s]
    if not subjects:
        raise GateError(EXIT_CONTRACT, "at least one non-empty --subject is required")
    with ctx.ledger.locked():
        st = fold(ctx.ledger.read())
        open_incs = [st["incidents"][i] for i in st["order"] if st["incidents"][i]["status"] == "open"]
        action, target, reason = "new", None, None
        if args.same_root_cause:
            target = get_incident(st, args.same_root_cause)
            if target["status"] != "open":
                raise GateError(EXIT_CONTRACT, f"{target['id']} is {target['status']}")
            action = "attached"
        else:
            overlap = [i for i in open_incs if set(i["subjects"]) & set(subjects)]
            if overlap:
                target, action = overlap[0], "attached"
            elif open_incs:
                if args.independent_evidence and args.independent_evidence.strip():
                    action = "new"
                else:
                    target, action = open_incs[-1], "attached_instead_of_new"
                    reason = "open incident exists and no --independent-evidence"
        new = target is None
        iid = f"INC-{len(st['order']) + 1:03d}" if new else target["id"]
        ev = ctx.ledger.append(st["last_seq"], {
            "type": "finding", "incident": iid, "new": new, "subjects": subjects,
            "gate_point": args.gate_point, "verdict": args.verdict,
            "same_root_cause": args.same_root_cause,
            "independent_evidence": args.independent_evidence, "attach_reason": reason})
    emit({"ok": action != "attached_instead_of_new", "action": action, "incident": iid, "reason": reason,
          "seq": ev["seq"]})
    return EXIT_CONTRACT if action == "attached_instead_of_new" else EXIT_OK


def cmd_round_start(args) -> int:
    ctx = Ctx(args, mutation=True)
    with ctx.ledger.locked():
        st = fold(ctx.ledger.read())
        inc = get_incident(st, args.incident)
        if inc["status"] != "open":
            raise GateError(EXIT_CONTRACT, f"{inc['id']} is {inc['status']}")
        if pending_for(st, inc["id"]):
            raise GateError(EXIT_CONTRACT, f"{inc['id']} is pending a human decision")
        if inc["round_open"]:
            raise GateError(EXIT_CONTRACT, f"{inc['id']} already has a round in progress")
        if inc["rounds_started"] >= MAX_ROUNDS:
            raise GateError(EXIT_CONTRACT, f"{inc['id']} already used {MAX_ROUNDS} rounds")
        if args.expect is None:
            raise GateError(EXIT_CONTRACT, "--expect is required (CAS)")
        if args.expect != inc["rounds_started"]:
            raise GateError(EXIT_CONTRACT, f"CAS mismatch: expect {args.expect}, ledger has {inc['rounds_started']}")
        strategy = _need(args.strategy, "--strategy")
        authority = _need(args.authority, "--authority")
        if norm(strategy) in inc["strategies"]:
            raise GateError(EXIT_CONTRACT, "strategy repeats an earlier round of this incident")
        ev = ctx.ledger.append(st["last_seq"], {
            "type": "round_start", "incident": inc["id"], "round": inc["rounds_started"] + 1,
            "strategy": strategy, "strategy_norm": norm(strategy), "authority": authority})
    emit({"ok": True, "incident": inc["id"], "round": ev["round"], "seq": ev["seq"]})
    return EXIT_OK


def cmd_round_end(args) -> int:
    ctx = Ctx(args, mutation=True)
    with ctx.ledger.locked():
        st = fold(ctx.ledger.read())
        inc = get_incident(st, args.incident)
        if not inc["round_open"]:
            raise GateError(EXIT_CONTRACT, f"{inc['id']} has no round in progress")
        ev = ctx.ledger.append(st["last_seq"], {
            "type": "round_end", "incident": inc["id"], "round": inc["rounds_started"],
            "result": args.result})
    emit({"ok": True, "incident": inc["id"], "round": ev["round"], "result": args.result, "seq": ev["seq"]})
    return EXIT_OK


def cmd_step_limit(args) -> int:
    ctx = Ctx(args, mutation=True)
    with ctx.ledger.locked():
        st = fold(ctx.ledger.read())
        inc = get_incident(st, args.incident)
        ev = ctx.ledger.append(st["last_seq"], {"type": "step_limit", "incident": inc["id"],
                                                "source": _need(args.source, "--source"), "note": args.note})
    emit({"ok": True, "incident": inc["id"], "seq": ev["seq"]})
    return EXIT_OK


def cmd_consult(args) -> int:
    ctx = Ctx(args, mutation=True)
    with ctx.ledger.locked():
        st = fold(ctx.ledger.read())
        inc = get_incident(st, args.incident)
        ev = ctx.ledger.append(st["last_seq"], {"type": "consult", "incident": inc["id"],
                                                "kind": args.kind, "note": args.note})
    emit({"ok": True, "incident": inc["id"], "seq": ev["seq"]})
    return EXIT_OK


def cmd_strategy_scan(args) -> int:
    ctx = Ctx(args, mutation=True)
    items = []
    for raw in args.item or []:
        head, sep, tail = str(raw).partition("::")
        if not sep or not head.strip() or not tail.strip():
            raise GateError(EXIT_CONTRACT, f"strategy-scan item needs '<strategy>::<reason>': {raw!r}")
        items.append({"strategy": head.strip(), "excluded_because": tail.strip()})
    if not items:
        raise GateError(EXIT_CONTRACT, "at least one --item is required")
    with ctx.ledger.locked():
        st = fold(ctx.ledger.read())
        inc = get_incident(st, args.incident)
        seq = st["last_seq"]
        for it in items:
            seq = ctx.ledger.append(seq, {"type": "strategy_scan", "incident": inc["id"], **it})["seq"]
    emit({"ok": True, "incident": inc["id"], "items": len(items), "seq": seq})
    return EXIT_OK


_HOST_RE = re.compile(r"^[a-z][a-z0-9+.\-]*://([^/:?#]+)", re.IGNORECASE)


def _host(url) -> str:
    m = _HOST_RE.match(str(url or ""))
    return m.group(1).lower() if m else ""


def cmd_ppss_record(args) -> int:
    ctx = Ctx(args, mutation=True)
    artifact = _need(args.artifact, "--artifact")
    domains = [d.strip().lower() for d in (args.domains or "").split(",") if d.strip()]
    state, why, rid, urls = "INVALID", None, None, []
    try:
        data = json.loads(Path(artifact).read_text(encoding="utf-8"))
        rid = data.get("request_id")
        results = data.get("results") or []
        urls = [r.get("url") for r in results if isinstance(r, dict) and r.get("url")]
        if not results:
            why = "empty results"
        elif domains and not any(h == d or h.endswith("." + d) for h in map(_host, urls) for d in domains):
            why = "no result url inside requested domains"
        else:
            state = "RETRIEVED"
    except (OSError, ValueError, AttributeError) as exc:
        why = f"artifact unreadable: {exc!r}"
    with ctx.ledger.locked():
        st = fold(ctx.ledger.read())
        inc = get_incident(st, args.incident)
        if inc["ppss_window"] >= 1:
            if not (args.new_query_strategy or "").strip() or not (args.reason or "").strip():
                raise GateError(EXIT_CONTRACT, "second ppss record in this window needs "
                                               "--new-query-strategy and --reason")
        ev = ctx.ledger.append(st["last_seq"], {
            "type": "ppss_record", "incident": inc["id"], "request_id": rid, "state": state,
            "reason_invalid": why, "urls": urls, "artifact": str(args.artifact), "domains": domains,
            "new_query_strategy": args.new_query_strategy, "reason": args.reason})
    emit({"ok": True, "state": state, "evidence_ok": False, "request_id": rid, "reason_invalid": why,
          "seq": ev["seq"]})
    return EXIT_OK


def cmd_ppss_read(args) -> int:
    ctx = Ctx(args, mutation=True)
    supports = _need(args.supports, "--supports")
    for name, value in (("--request-id", args.request_id), ("--url", args.url),
                        ("--source-file", args.source_file)):
        _need(value, name)
    try:
        text = Path(args.source_file).read_text(encoding="utf-8", errors="replace")
    except OSError as exc:
        raise GateError(EXIT_CONTRACT, f"source file unreadable: {exc!r}") from exc
    ok = False
    quote = None
    if supports.lower() != "none":
        quote = (args.quote or "").strip()
        if not quote or quote not in text:
            raise GateError(EXIT_CONTRACT, "--supports <claim_id> needs --quote that is a substring of the source")
        ok = True
    with ctx.ledger.locked():
        st = fold(ctx.ledger.read())
        inc = get_incident(st, args.incident)
        rec = st["ppss"].get(args.request_id)
        if rec is None or rec["incident"] != inc["id"]:
            raise GateError(EXIT_CONTRACT, f"unknown ppss request for this incident: {args.request_id!r}")
        if rec["state"] == "INVALID":
            raise GateError(EXIT_CONTRACT, "ppss request is INVALID")
        if args.url not in rec["urls"]:
            raise GateError(EXIT_CONTRACT, "url is not one of the request's results")
        ev = ctx.ledger.append(st["last_seq"], {
            "type": "ppss_read", "incident": inc["id"], "request_id": args.request_id, "url": args.url,
            "source_sha256": hashlib.sha256(text.encode("utf-8")).hexdigest(),
            "supports": "none" if supports.lower() == "none" else supports,
            "quote_sha256": hashlib.sha256(quote.encode("utf-8")).hexdigest() if quote else None,
            "evidence_ok": ok})
    emit({"ok": True, "state": "SOURCE_READ", "evidence_ok": ok, "seq": ev["seq"]})
    return EXIT_OK


def cmd_human(args) -> int:
    ctx = Ctx(args, mutation=True)
    if args.action == "resolve":
        ref = _need(args.ref, "--ref")
        with ctx.ledger.locked():
            st = fold(ctx.ledger.read())
            targets = [p for p in st["pending"] if not args.incident or p["incident"] == args.incident]
            if not targets:
                raise GateError(EXIT_CONTRACT, "no pending human decision to resolve")
            if any(p["category"] == "EXHAUSTED" for p in targets):
                raise GateError(EXIT_CONTRACT, "EXHAUSTED cannot be resolved here (continuation is a COST decision)")
            ev = ctx.ledger.append(st["last_seq"], {"type": "human_resolve", "ref": ref,
                                                    "cleared": [p["seq"] for p in targets]})
        emit({"ok": True, "cleared": len(targets), "seq": ev["seq"]})
        return EXIT_OK
    cat = args.category
    if cat not in CATEGORIES:
        raise GateError(EXIT_USAGE, f"unknown category {cat!r}; expected one of {', '.join(CATEGORIES)}")
    basis = _need(args.basis, "--basis")
    with ctx.ledger.locked():
        st = fold(ctx.ledger.read())
        inc = None
        if args.incident:
            inc = get_incident(st, args.incident)
        elif cat not in IMMEDIATE and cat != "NO_PROGRESS":
            raise GateError(EXIT_CONTRACT, f"{cat} needs --incident")
        if cat == "EXHAUSTED":
            if basis != "rounds_3" or inc["rounds_started"] != MAX_ROUNDS or inc["results"].count("fail") != MAX_ROUNDS:
                raise GateError(EXIT_CONTRACT, "EXHAUSTED needs 3 failed rounds on the incident and --basis rounds_3")
        elif cat in VERIFY_FIRST:
            if inc["consults"]["local"] < 1 or inc["consults"]["advisor"] < 1:
                raise GateError(EXIT_CONTRACT, f"{cat} needs prior consult local and consult advisor")
            if cat == "NO_AUTHORIZED_STRATEGY":
                if inc["rounds_started"] >= MAX_ROUNDS or inc["round_open"]:
                    raise GateError(EXIT_CONTRACT, "NO_AUTHORIZED_STRATEGY needs rounds<3 and no round in progress")
                if inc["scans"] < 1:
                    raise GateError(EXIT_CONTRACT, "NO_AUTHORIZED_STRATEGY needs a non-empty strategy-scan")
        elif cat == "NO_PROGRESS" and st["blocks"] < 1:
            raise GateError(EXIT_CONTRACT, "NO_PROGRESS needs at least one recorded blocks event")
        ev = ctx.ledger.append(st["last_seq"], {"type": "human", "category": cat,
                                                "incident": inc["id"] if inc else None, "basis": basis})
        rounds = inc["rounds_started"] if inc else 0
    print(f"HUMAN_REQUIRED: {cat} | {ctx.tid} | {inc['id'] if inc else 'none'} | round={rounds}/{MAX_ROUNDS} | {basis}")
    return EXIT_OK


def cmd_blocks(args) -> int:
    ctx = Ctx(args, mutation=True)
    with ctx.ledger.locked():
        st = fold(ctx.ledger.read())
        ev = ctx.ledger.append(st["last_seq"], {"type": "blocks", "note": args.note})
    emit({"ok": True, "seq": ev["seq"]})
    return EXIT_OK


def cmd_missed_check(args) -> int:
    ctx = Ctx(args, mutation=True)
    with ctx.ledger.locked():
        st = fold(ctx.ledger.read())
        ev = ctx.ledger.append(st["last_seq"], {"type": "missed_check", "gate_point": args.gate_point})
    emit({"ok": True, "seq": ev["seq"]})
    return EXIT_OK


def cmd_check(args) -> int:
    ctx = Ctx(args, mutation=True)
    head = run_git(["rev-parse", "HEAD"])
    with ctx.ledger.locked():
        st = fold(ctx.ledger.read())
        code = EXIT_BLOCKED if st["pending"] else EXIT_OK
        ev = ctx.ledger.append(st["last_seq"], {
            "type": "check", "gate_point": args.gate_point, "ticket_source": ctx.source, "head": head,
            "exit": code, "late": bool(args.late)})
        pending = list(st["pending"])
    emit({"ok": code == EXIT_OK, "gate_point": args.gate_point, "exit": code, "late": bool(args.late),
          "seq": ev["seq"]})
    for p in pending:
        print(f"PENDING_HUMAN: {p['category']} | {ctx.tid} | {p['incident'] or 'none'} | {p['basis']}")
    return code


# --------------------------------------------------------------------------
# cli
# --------------------------------------------------------------------------
def build_parser() -> argparse.ArgumentParser:
    common = argparse.ArgumentParser(add_help=False)
    common.add_argument("--ticket", default=argparse.SUPPRESS)
    ap = argparse.ArgumentParser(prog="gate_ledger.py", description=__doc__.split("\n")[0], parents=[common])
    sub = ap.add_subparsers(dest="cmd", required=True)

    p = sub.add_parser("status", parents=[common])
    p.add_argument("--json", action="store_true")
    p.set_defaults(fn=cmd_status)

    p = sub.add_parser("incident", parents=[common])
    p.add_argument("action", choices=["open"])
    p.add_argument("--subject", action="append")
    p.add_argument("--gate-point")
    p.add_argument("--verdict")
    p.add_argument("--same-root-cause")
    p.add_argument("--independent-evidence")
    p.set_defaults(fn=cmd_incident_open)

    p = sub.add_parser("round", parents=[common])
    p.add_argument("action", choices=["start", "end"])
    p.add_argument("--incident", required=True)
    p.add_argument("--expect", type=int)
    p.add_argument("--strategy")
    p.add_argument("--authority")
    p.add_argument("--result", choices=["pass", "fail"])
    p.set_defaults(fn=lambda a: cmd_round_start(a) if a.action == "start" else _round_end(a))

    p = sub.add_parser("step-limit", parents=[common])
    p.add_argument("--incident", required=True)
    p.add_argument("--source")
    p.add_argument("--note")
    p.set_defaults(fn=cmd_step_limit)

    p = sub.add_parser("consult", parents=[common])
    p.add_argument("--incident", required=True)
    p.add_argument("--kind", required=True, choices=["local", "advisor"])
    p.add_argument("--note")
    p.set_defaults(fn=cmd_consult)

    p = sub.add_parser("strategy-scan", parents=[common])
    p.add_argument("--incident", required=True)
    p.add_argument("--item", action="append")
    p.set_defaults(fn=cmd_strategy_scan)

    p = sub.add_parser("ppss", parents=[common])
    p.add_argument("action", choices=["record", "read"])
    p.add_argument("--incident", required=True)
    p.add_argument("--artifact")
    p.add_argument("--domains")
    p.add_argument("--new-query-strategy")
    p.add_argument("--reason")
    p.add_argument("--request-id")
    p.add_argument("--url")
    p.add_argument("--source-file")
    p.add_argument("--supports")
    p.add_argument("--quote")
    p.set_defaults(fn=lambda a: cmd_ppss_record(a) if a.action == "record" else cmd_ppss_read(a))

    p = sub.add_parser("human", parents=[common])
    p.add_argument("action", nargs="?", choices=["resolve"])
    p.add_argument("--category")
    p.add_argument("--incident")
    p.add_argument("--basis")
    p.add_argument("--ref")
    p.set_defaults(fn=cmd_human)

    p = sub.add_parser("blocks", parents=[common])
    p.add_argument("--note")
    p.set_defaults(fn=cmd_blocks)

    p = sub.add_parser("missed-check", parents=[common])
    p.add_argument("--gate-point", required=True, choices=GATE_POINTS)
    p.set_defaults(fn=cmd_missed_check)

    p = sub.add_parser("check", parents=[common])
    p.add_argument("gate_point", choices=GATE_POINTS)
    p.add_argument("--late", action="store_true")
    p.set_defaults(fn=cmd_check)
    return ap


def _round_end(args) -> int:
    if not args.result:
        raise GateError(EXIT_USAGE, "round end needs --result pass|fail")
    return cmd_round_end(args)


def main(argv=None) -> int:
    for stream in (sys.stdout, sys.stderr):
        with contextlib.suppress(AttributeError, ValueError):
            stream.reconfigure(encoding="utf-8")
    args = build_parser().parse_args(argv)
    try:
        return args.fn(args)
    except GateError as exc:
        print(f"gate_ledger: {exc.msg}", file=sys.stderr)
        return exc.code
    except OSError as exc:
        print(f"gate_ledger: IO failure: {exc!r}", file=sys.stderr)
        return EXIT_IO


if __name__ == "__main__":
    raise SystemExit(main())
