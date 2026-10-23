#!/usr/bin/env python3
"""route.py - single central entry for engineering-advisor / PPSS consults (ADV-ENTRY-01, V1).

Usage:
    python .claude/scripts/route.py [--ticket ID] [--table PATH] consult --incident INC --source NAME [--phase P] [...]
    python .claude/scripts/route.py [--table PATH] consult --no-ticket --source advisor.claude-cli --payload PATH
    python .claude/scripts/route.py [--table PATH] table check

Sources (route_table.json):
    advisor.api     script-call: ONE invocation does the checks, ONE network attempt through advisor_call.main()
                    (no retry, no mode switch, no fallback), and records `consult --kind advisor` on exit 0.
                    flags: --payload PATH --evidence PATH [--effort E --max-tokens N --timeout S --request-mode M]
    advisor.native  model-dispatch: --phase dispatch --purpose TEXT  ->  the main agent calls the native `advisor`
                    tool  ->  --phase return --dispatch-id ID --purpose TEXT --note REF (records the consult).
    advisor.claude-cli  script-call: ONE launch of `claude -p` (Claude subscription CLI; model and effort come from the
                    table `cli` block; fixed flags, --tools "", --strict-mcp-config, --no-session-persistence,
                    stream-json; the three API-key env vars are removed from the child; wall-clock timeout; no retry,
                    no fallback, no switch to another channel or to the billing API). Ticketed (default): needs
                    --incident --payload --evidence, runs M1/M4/M5 before launching and records ONE `consult --kind
                    advisor` only on success. Ticketless (--no-ticket, explicit, never a downgrade): needs --payload
                    only, no ledger read or write. `apiKeySource` none/null proves only "no API-key source", not the
                    subscription billing; the effort flag is reported, never claimed effective; hooks/plugins still
                    start (--tools "" only guarantees no tool execution). Every launched call leaves a git-ignored
                    local execution record (tmp/<ticket>/advisor_cli/ or tmp/_advisor_cli_ticketless/): not billing
                    or provider-side proof. Success needs result.is_error == false (boolean), modelUsage keys == {the
                    requested model} == init.model, and a non-blank string answer.
                    reasons - REFUSED: auth_unreported api_key_source model_unreported model_mismatch multiple_models
                    empty_answer incident_closed_after_call; CALL_FAILED: cli_unavailable (detail bin_not_found
                    cwd_not_directory cwd_inside_repo cwd_prepare_failed launch failed) timeout init_missing
                    nonzero_exit unparseable (detail is_error_invalid model_usage_invalid answer_invalid) not_logged_in
                    model_unavailable cli_error. A timeout is final: leftover output is diagnostic only (never a
                    success, never an answer, never recorded).
    research.ppss   model-dispatch: --phase dispatch (pre-flight judgement, then the concrete research_submit
                    params) -> main agent calls research_submit -> --phase bind --dispatch-id ID --request-id R
                    --artifact PATH (R and PATH are the tool response's request_id and artifact_path; the entry
                    stores request_id, artifact path and sha256 in the saved dispatch description and refuses a
                    response already bound elsewhere) -> --phase record --dispatch-id ID --artifact PATH (needs the
                    binding, same path and same sha256; then pass-through of `ppss record`; the ledger refuses a
                    request id already recorded for the incident) -> --phase read ... (pass-through).
                    --cached-artifact (bind only) is a DECLARED flag for a cache hit with two separate, narrow
                    exceptions: (1) the captured-at-or-after-dispatch time condition is waived; (2) only when the
                    artifact really predates the dispatch, the response request_id may differ from the artifact's
                    own request_id (a cache hit returns a new response id with an old artifact). It never exempts
                    the binding, the sha256, the uniqueness, the ledger de-duplication or the dispatch slot checks,
                    and a request_id mismatch on an artifact captured after the dispatch is still refused.
    Disabled source: SOURCE_DISABLED + `alternatives` (same role, callable, enabled). The entry never picks one
    for the caller. No enabled source of that role at all: MISSING_REQUIRED.

Output: ALWAYS exactly one JSON object on stdout (stderr only carries a short human note on failure).
Envelope: {ok, verb, disposition, source?, phase?, via:"route.py", reason?, detail?, action?, return_with?,
           dispatch_id?, existing?, alternatives?, ledger_seq?, receipt?, receipt_sha256?, request_id?,
           advisor_exit?, advice?, ledger?}
Exit codes (own set; not the ledger's, not advisor_call's):
    0 OK (DISPATCHED | BOUND | RECORDED)   2 usage   3 route table missing/invalid
    4 UNKNOWN_SOURCE | NOT_CALLABLE | SOURCE_DISABLED | MISSING_REQUIRED
    5 REFUSED (reason: incident_not_open ticket_not_mutable incident_token_missing|mismatch|ambiguous
       evidence_token_missing|ambiguous evidence_file_unreadable|empty evidence_hash_mismatch receipt_mismatch
       request_invalid dispatch_mismatch stale_or_duplicate_dispatch existing_results_unconsumed
       prior_research_requires_strategy busy ledger_refused duplicate_request_id incident_closed_after_call
       artifact_predates_dispatch request_id_mismatch artifact_bound_elsewhere dispatch_already_bound unbound_dispatch
       binding_mismatch ledger_incompatible)
    6 CALL_FAILED (caller / infrastructure failure; advisor_exit carries the caller's exit code)

Mechanical checks (the ONLY thing "constructed" by this entry; they compare DECLARED items with each other):
    M1 incident is open and the ticket is loc:pending (ledger `status --json`).
    M2 receipt.ticket == ledger ticket.
    M3 receipt.payload.sha256 == sha256 of the payload bytes this entry read before calling the caller.
    M4 payload declares exactly one `incident=<ID>` token and it equals --incident (exact boundary).
    M5 payload carries exactly ONE evidence token, `failure_sha256=<h>` or `uncertainty_sha256=<h>`, and <h> equals
       the sha256 of the non-empty --evidence file (the empty-input hash never counts).
    M6 receipt status SUCCESS, exit_code 0, created_at_utc not earlier than this invocation's call start; the
       receipt comes from this invocation (no external receipt path is accepted).
    M7 the --note is composed by this entry from the verified values and carries `via=route.py`.
NOT proven by this entry: that the evidence belongs to the incident causally, that the payload prose is faithful,
whether a consult was needed, advice quality, native-advisor content (encrypted, unverifiable), or that a
dispatched model tool was really called. `--purpose` is declarative (only its consistency with the dispatch is
checked). The tokens are the primary evidence binding; the question prose is never parsed or restricted.

Dispatch ids bind "ledger slot + concrete request": d2-<16hex> of sha256(ticket|incident|source|n|digest).
n = advisor consults (native) / the incident's monotonic `ppss_records` event count (ppss; NOT the number of distinct
request ids); digest = purpose (native) or the fingerprint of the whole research request (ppss: question, queries,
task_type, filters.domains, justification; canonical JSON, see canon_request). The artifact only keeps `queries`, so
a PPSS dispatch saves its minimal description in the per-ticket scratch tmp/<ticket>/route/dispatch_<id>.json and
`record` loads it by --dispatch-id (declared, not proof that the model really called the tool with those
parameters). Return/record re-computes with the CURRENT n inside a
transient mutex (<git-common-dir>/gate/<ticket>.route.lock: pid+nonce only, not state, removed on exit; never
reclaimed by age: a contender waits --lock-wait-ms, then refuses `busy`) because the ledger's `consult` has no CAS.
A lock left by a killed process must be removed by hand. The mutex serializes only returns made THROUGH this entry.

Limits: bypassing this entry (calling gate_ledger.py directly) is not mechanically blocked; it is visible only
through `via=route.py` in advisor consult notes (PPSS ledger events carry no note, hence no marker). V2 (`next`,
skill successors, predicates, NEEDS_JUDGMENT) is not implemented here. No state file, DB or queue is created.
"""
from __future__ import annotations

import argparse
import contextlib
import hashlib
import importlib.util
import io
import json
import os
import re
import shutil
import signal
import subprocess
import sys
import tempfile
import time
import unicodedata
import uuid
from datetime import datetime, timezone
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent
TABLE_DEFAULT = SCRIPT_DIR / "route_table.json"
LEDGER = SCRIPT_DIR / "gate_ledger.py"
ADVISOR_CALL = SCRIPT_DIR / "advisor_call.py"
VIA = "route.py"
SCHEMA = 1
KINDS = ("script-call", "model-dispatch", "domain-internal", "external-ssot")
CALLABLE_KINDS = ("script-call", "model-dispatch")
ROLES = ("advisor", "research")
ADAPTERS = ("api_advisor", "native_advisor", "claude_cli", "ppss")
CLI_EFFORTS = ("low", "medium", "high", "xhigh", "max")
CLI_STRIP_ENV = ("ANTHROPIC_API_KEY", "ADVISOR_ANTHROPIC_API_KEY", "ANTHROPIC_AUTH_TOKEN")
CLI_PAYLOAD_MAX = 200000
CLI_CWD_NAME = "cueshot_advisor_cli_cwd"
CLI_PREAMBLE = ("你是諮詢顧問：只回答問題，不操作任何任務、不改檔、不呼叫工具；"
                "請求端 executor 會自行評估你的回答並決定如何做。")
NAME_RE = re.compile(r"^[a-z]+\.[a-z0-9][a-z0-9-]*$")
DISPATCH_ID_RE = re.compile(r"^d2-[0-9a-f]{16}$")
DOMAIN_RE = re.compile(r"^[a-z0-9]([a-z0-9-]*[a-z0-9])?(\.[a-z0-9]([a-z0-9-]*[a-z0-9])?)+$")
EMPTY_SHA = hashlib.sha256(b"").hexdigest()
PURPOSE_MAX = 500
LOCK_WAIT_MS_DEFAULT = 10000
STALE_LOOKBACK = 8

E_OK, E_USAGE, E_TABLE, E_SOURCE, E_REFUSED, E_FAILED = 0, 2, 3, 4, 5, 6


class Stop(Exception):
    def __init__(self, code, disposition, reason=None, detail=None, **extra):
        super().__init__(f"{disposition}:{reason}")
        self.code, self.disposition, self.reason, self.detail, self.extra = code, disposition, reason, detail, extra


class _Parser(argparse.ArgumentParser):
    def error(self, message):
        raise Stop(E_USAGE, "USAGE", "usage", message)

    def exit(self, status=0, message=None):
        if status:
            raise Stop(E_USAGE, "USAGE", "usage", message)
        raise SystemExit(0)


def _utf8():
    for s in (sys.stdout, sys.stderr):
        try:
            s.reconfigure(encoding="utf-8")
        except Exception:
            pass


def sha(b):
    return hashlib.sha256(b).hexdigest()


def norm_text(s):
    return unicodedata.normalize("NFC", s or "").strip()


def norm_queries(queries):
    seen = []
    for q in queries or []:
        n = norm_text(q).casefold()
        if n and n not in seen:
            seen.append(n)
    return seen


def digest_purpose(purpose):
    return sha(norm_text(purpose).encode("utf-8"))


def canon_request(question, queries, task_type, domains, justification):
    """Whole research request as it is actually sent to research_submit and can change the result."""
    return {"v": 2, "question": norm_text(question), "queries": norm_queries(queries),
            "task_type": norm_text(task_type),
            "filters": ({"domains": sorted(set(domains))} if domains else {}),
            "justification": justification}


def digest_request(doc):
    return sha(json.dumps(doc, ensure_ascii=False, sort_keys=True, separators=(",", ":")).encode("utf-8"))


def dispatch_id(ticket, incident, source, n, digest):
    return "d2-" + sha(f"{ticket}|{incident}|{source}|{n}|{digest}".encode("utf-8"))[:16]


def one_line(s, limit=500):
    return re.sub(r"\s+", " ", s or "").strip()[:limit]


def scrub(text):
    return re.sub(r"sk-ant-[A-Za-z0-9_-]+", "sk-ant-***", text or "")


# ---------------------------------------------------------------- route table
def _no_dups(pairs):
    out = {}
    for k, v in pairs:
        if k in out:
            out["__duplicate__"] = out.get("__duplicate__", []) + [k]
        out[k] = v
    return out


def _cli_block_errors(name, c):
    if not isinstance(c, dict):
        return [f"{name}: cli must be an object"]
    errs = []
    for k in ("bin", "model"):
        if not isinstance(c.get(k), str) or not c.get(k).strip():
            errs.append(f"{name}: cli.{k} must be a non-empty string")
    pa = c.get("prefix_args")
    if not isinstance(pa, list) or any(not isinstance(x, str) for x in pa):
        errs.append(f"{name}: cli.prefix_args must be a list of strings")
    if c.get("effort") not in CLI_EFFORTS:
        errs.append(f"{name}: cli.effort must be one of {list(CLI_EFFORTS)}")
    ts = c.get("timeout_s")
    if isinstance(ts, bool) or not isinstance(ts, int) or not 30 <= ts <= 600:
        errs.append(f"{name}: cli.timeout_s must be an integer in 30..600")
    return errs


def validate_table(t):
    errs = []
    if not isinstance(t, dict):
        return ["table is not an object"]
    if t.get("schema") != SCHEMA:
        errs.append(f"schema must be {SCHEMA}")
    extra = sorted(set(t) - {"schema", "sources"})
    if extra:
        errs.append(f"unknown top-level keys: {extra}")
    src = t.get("sources")
    if not isinstance(src, dict) or not src:
        return errs + ["sources must be a non-empty object"]
    if "__duplicate__" in src:
        errs.append(f"duplicate source names: {src['__duplicate__']}")
    for name, s in src.items():
        if name == "__duplicate__":
            continue
        if not NAME_RE.match(name):
            errs.append(f"{name}: name must look like <role>.<name>")
        if not isinstance(s, dict):
            errs.append(f"{name}: entry is not an object")
            continue
        kind = s.get("kind")
        if kind not in KINDS:
            errs.append(f"{name}: illegal kind {kind!r}")
            continue
        if s.get("role") not in ROLES:
            errs.append(f"{name}: illegal role {s.get('role')!r}")
        if kind in CALLABLE_KINDS:
            if not isinstance(s.get("enabled"), bool):
                errs.append(f"{name}: enabled must be boolean")
            if s.get("adapter") not in ADAPTERS:
                errs.append(f"{name}: unknown adapter {s.get('adapter')!r}")
            if s.get("adapter") in ("api_advisor", "native_advisor", "claude_cli") and s.get("ledger_kind") != "advisor":
                errs.append(f"{name}: ledger_kind must be 'advisor'")
            if s.get("adapter") == "claude_cli":
                errs.extend(_cli_block_errors(name, s.get("cli")))
        if kind == "model-dispatch":
            d = s.get("dispatch")
            if not isinstance(d, dict) or not isinstance(d.get("tool"), str) or not d.get("tool") \
                    or not isinstance(d.get("params"), (dict, list)):
                errs.append(f"{name}: dispatch needs tool(str) and params(object|list)")
    return errs


def load_table(path):
    try:
        text = Path(path).read_text(encoding="utf-8")
        t = json.loads(text, object_pairs_hook=_no_dups)
    except (OSError, ValueError) as exc:
        raise Stop(E_TABLE, "TABLE_INVALID", "table_unreadable", f"{type(exc).__name__}: {exc}")
    errs = validate_table(t)
    if errs:
        raise Stop(E_TABLE, "TABLE_INVALID", "table_invalid", "; ".join(errs), errors=errs)
    return t


def resolve_source(table, name):
    srcs = table["sources"]
    if name not in srcs:
        raise Stop(E_SOURCE, "UNKNOWN_SOURCE", "unknown_source", name)
    s = srcs[name]
    if s["kind"] not in CALLABLE_KINDS:
        raise Stop(E_SOURCE, "NOT_CALLABLE", "not_callable", f"{name} is {s['kind']}")
    alts = [n for n, o in srcs.items()
            if n != name and o.get("kind") in CALLABLE_KINDS and o.get("enabled") is True and o.get("role") == s["role"]]
    if s["enabled"] is not True:
        if alts:
            raise Stop(E_SOURCE, "SOURCE_DISABLED", "source_disabled", name, alternatives=alts)
        raise Stop(E_SOURCE, "MISSING_REQUIRED", "required_capability_absent", name, alternatives=[])
    return s


# ---------------------------------------------------------------- git / ledger
def _git(args, cwd):
    try:
        res = subprocess.run(["git", *args], cwd=str(cwd), capture_output=True)
    except OSError as exc:
        raise Stop(E_FAILED, "CALL_FAILED", "git_unavailable", repr(exc))
    return res.returncode, res.stdout.decode("utf-8", "replace").strip(), res.stderr.decode("utf-8", "replace")


def repo_root():
    rc, out, err = _git(["rev-parse", "--show-toplevel"], os.getcwd())
    if rc != 0 or not out:
        raise Stop(E_USAGE, "USAGE", "not_a_git_repo", one_line(err))
    return Path(out)


def common_dir(root):
    rc, out, err = _git(["rev-parse", "--path-format=absolute", "--git-common-dir"], root)
    if rc != 0 or not out:
        raise Stop(E_FAILED, "CALL_FAILED", "git_common_dir_unavailable", one_line(err))
    return Path(out)


def run_ledger(root, ticket, args):
    argv = [sys.executable, str(LEDGER)] + (["--ticket", ticket] if ticket else []) + [str(a) for a in args]
    env = dict(os.environ)
    env["PYTHONIOENCODING"] = "utf-8"
    try:
        res = subprocess.run(argv, cwd=str(root), capture_output=True, env=env)
    except OSError as exc:
        raise Stop(E_FAILED, "CALL_FAILED", "ledger_unavailable", repr(exc))
    return res.returncode, res.stdout.decode("utf-8", "replace"), res.stderr.decode("utf-8", "replace")


def parse_json(text):
    text = (text or "").strip()
    if not text:
        return None
    try:
        return json.loads(text)
    except ValueError:
        pass
    for line in text.splitlines():
        line = line.strip()
        if line.startswith("{"):
            try:
                return json.loads(line)
            except ValueError:
                continue
    return None


def ledger_status(root, ticket_arg):
    rc, out, err = run_ledger(root, ticket_arg, ["status", "--json"])
    if rc == 6:
        raise Stop(E_FAILED, "CALL_FAILED", "ledger_io", one_line(err))
    if rc != 0:
        raise Stop(E_REFUSED, "REFUSED", "ledger_refused", one_line(err) or f"ledger status exit {rc}")
    st = parse_json(out)
    if not isinstance(st, dict) or "ticket" not in st:
        raise Stop(E_FAILED, "CALL_FAILED", "ledger_output_unparseable", one_line(out, 200))
    return st


def precheck(root, ticket_arg, incident):
    """M1: incident open and ticket mutable. Returns (status, incident object)."""
    st = ledger_status(root, ticket_arg)
    if st.get("ticket_location") != "loc:pending":
        raise Stop(E_REFUSED, "REFUSED", "ticket_not_mutable", st.get("ticket_location"))
    inc = next((i for i in st.get("incidents", []) if i.get("id") == incident), None)
    if inc is None or inc.get("status") != "open":
        raise Stop(E_REFUSED, "REFUSED", "incident_not_open", incident)
    return st, inc


def ledger_write(root, ticket, args):
    rc, out, err = run_ledger(root, ticket, args)
    if rc == 6:
        raise Stop(E_FAILED, "CALL_FAILED", "ledger_io", one_line(err))
    if rc != 0:
        raise Stop(E_REFUSED, "REFUSED", "ledger_refused", one_line(err) or f"ledger exit {rc}")
    return parse_json(out) or {}


def ledger_consult(root, ticket, incident, note, closed_extra=None, closed_detail=None):
    """consult --kind advisor. The ledger checks `open` inside its write lock right before the append; if it
    refuses because the incident is no longer open, report that precisely instead of a generic refusal."""
    try:
        return ledger_write(root, ticket, ["consult", "--incident", incident, "--kind", "advisor", "--note", note])
    except Stop as stop:
        if stop.reason != "ledger_refused":
            raise
        try:
            inc = next((i for i in ledger_status(root, ticket).get("incidents", []) if i.get("id") == incident), None)
        except Stop:
            raise stop
        if inc is not None and inc.get("status") != "open":
            if closed_extra is not None:
                raise Stop(E_REFUSED, "REFUSED", "incident_closed_after_call", closed_detail, **closed_extra)
            raise Stop(E_REFUSED, "REFUSED", "incident_not_open", incident)
        raise


# ---------------------------------------------------------------- mutex
@contextlib.contextmanager
def route_lock(root, ticket, wait_ms):
    gate = common_dir(root) / "gate"
    gate.mkdir(parents=True, exist_ok=True)
    path = gate / f"{ticket}.route.lock"
    token = f"{os.getpid()}:{uuid.uuid4().hex}"
    deadline = time.monotonic() + max(0, wait_ms) / 1000.0
    held = False
    while True:
        try:
            fd = os.open(str(path), os.O_CREAT | os.O_EXCL | os.O_WRONLY)
            with os.fdopen(fd, "w", encoding="utf-8") as fh:
                fh.write(f"{token} {time.time():.0f}\n")
            held = True
            break
        except (FileExistsError, PermissionError):
            if time.monotonic() >= deadline:
                raise Stop(E_REFUSED, "REFUSED", "busy",
                           f"{path.name} held by another process (if no route.py process is running, remove the file by hand)")
            time.sleep(0.03)
    try:
        yield
    finally:
        if held:
            try:
                if path.read_text(encoding="utf-8").startswith(token):
                    path.unlink()
            except OSError:
                pass


# ---------------------------------------------------------------- evidence tokens (M4/M5)
def check_tokens(text, incident, evidence_path):
    ids = set(re.findall(r"(?<![0-9A-Za-z_-])incident=([A-Za-z0-9][A-Za-z0-9-]*)", text))
    if not ids:
        raise Stop(E_REFUSED, "REFUSED", "incident_token_missing", "payload declares no incident=<ID> token")
    if len(ids) > 1:
        raise Stop(E_REFUSED, "REFUSED", "incident_token_ambiguous", ",".join(sorted(ids)))
    if next(iter(ids)) != incident:
        raise Stop(E_REFUSED, "REFUSED", "incident_token_mismatch", f"payload={next(iter(ids))} target={incident}")
    pat = r"(?<![0-9A-Za-z_-]){}=([0-9a-f]{{64}})(?![0-9a-f])"
    fails = re.findall(pat.format("failure_sha256"), text)
    uncs = re.findall(pat.format("uncertainty_sha256"), text)
    if len(fails) + len(uncs) == 0:
        raise Stop(E_REFUSED, "REFUSED", "evidence_token_missing", "need exactly one failure_sha256= or uncertainty_sha256=")
    if len(fails) + len(uncs) > 1:
        raise Stop(E_REFUSED, "REFUSED", "evidence_token_ambiguous", "more than one evidence token")
    kind, h = ("failure", fails[0]) if fails else ("uncertainty", uncs[0])
    try:
        data = Path(evidence_path).read_bytes()
    except (OSError, TypeError) as exc:
        raise Stop(E_REFUSED, "REFUSED", "evidence_file_unreadable", type(exc).__name__)
    if not data:
        raise Stop(E_REFUSED, "REFUSED", "evidence_file_empty", "the evidence file is empty")
    if h == EMPTY_SHA or sha(data) != h:
        raise Stop(E_REFUSED, "REFUSED", "evidence_hash_mismatch", f"{kind} token does not match the evidence file")
    return kind, h


# ---------------------------------------------------------------- adapters
_ADVISOR_MOD = None


def load_advisor_call():
    global _ADVISOR_MOD
    if _ADVISOR_MOD is None:
        spec = importlib.util.spec_from_file_location("route_advisor_call", str(ADVISOR_CALL))
        mod = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(mod)
        _ADVISOR_MOD = mod
    return _ADVISOR_MOD


def rel(root, p):
    try:
        return Path(os.path.relpath(str(p), str(root))).as_posix()
    except ValueError:
        return Path(p).as_posix()


def do_api(a, root, ticket_arg, client_factory, env, now):
    st, _ = precheck(root, ticket_arg, a.incident)
    ticket = st["ticket"]
    if not a.payload or not a.evidence:
        raise Stop(E_USAGE, "USAGE", "usage", "advisor.api needs --payload and --evidence")
    try:
        pbytes = Path(a.payload).read_bytes()
        ptext = pbytes.decode("utf-8")
    except (OSError, UnicodeDecodeError) as exc:
        raise Stop(E_REFUSED, "REFUSED", "request_invalid", f"payload unreadable: {type(exc).__name__}")
    psha = sha(pbytes)
    kind, h = check_tokens(ptext, a.incident, a.evidence)            # M4, M5 (before any network)
    t0 = now().replace(microsecond=0)
    argv = ["--ticket", ticket, "--payload", str(a.payload), "--out-dir", str(root / "tmp" / ticket / "advisor")]
    for flag, val in (("--effort", a.effort), ("--max-tokens", a.max_tokens), ("--timeout", a.timeout),
                      ("--request-mode", a.request_mode)):
        if val is not None:
            argv += [flag, str(val)]
    mod = load_advisor_call()
    out, err = io.StringIO(), io.StringIO()
    try:
        with contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
            rc = mod.main(argv, client_factory=client_factory, env=env, now=now)
    except SystemExit as exc:
        rc = exc.code if isinstance(exc.code, int) else 2
    summary = parse_json(out.getvalue()) or {}
    if rc != 0:
        raise Stop(E_FAILED, "CALL_FAILED", "advisor_call_failed", scrub(one_line(err.getvalue(), 300)),
                   advisor_exit=rc, receipt=summary.get("receipt"), request_id=summary.get("request_id"))
    rpath = Path(str(summary.get("receipt") or ""))
    try:
        rbytes = rpath.read_bytes()
        receipt = json.loads(rbytes)
    except (OSError, ValueError) as exc:
        raise Stop(E_REFUSED, "REFUSED", "receipt_mismatch", f"receipt unreadable: {type(exc).__name__}",
                   receipt=str(rpath))
    rsha = sha(rbytes)
    try:
        created = datetime.strptime(receipt.get("created_at_utc", ""), "%Y-%m-%dT%H:%M:%SZ").replace(tzinfo=timezone.utc)
    except ValueError:
        created = None
    bad = []
    if rsha != summary.get("receipt_sha256"):
        bad.append("receipt_sha256")
    if receipt.get("ticket") != ticket:                                  # M2
        bad.append("ticket")
    if (receipt.get("payload") or {}).get("sha256") != psha:             # M3
        bad.append("payload_sha256")
    if receipt.get("status") != "SUCCESS" or receipt.get("exit_code") != 0 or created is None or created < t0:  # M6
        bad.append("status_or_time")
    if bad:
        raise Stop(E_REFUSED, "REFUSED", "receipt_mismatch", ",".join(bad),
                   receipt=str(rpath), receipt_sha256=rsha, request_id=receipt.get("request_id"))
    note = (f"source=advisor.api via={VIA} receipt={rel(root, rpath)} receipt_sha256={rsha} "
            f"request_id={receipt.get('request_id')} payload_sha256={psha} incident={a.incident} evidence={kind}:{h}")
    led = ledger_consult(
        root, ticket, a.incident, note,
        closed_detail=("advisor reply obtained and its receipt kept (cost already incurred), but the target incident is no "
                       "longer open: nothing was recorded, the API was not called again, the incident was not reopened"),
        closed_extra={"receipt": rel(root, rpath), "receipt_sha256": rsha, "request_id": receipt.get("request_id"),
                      "advice": receipt.get("advice"), "recorded": False})
    return {"ok": True, "disposition": "RECORDED", "receipt": rel(root, rpath), "receipt_sha256": rsha,
            "request_id": receipt.get("request_id"), "ledger_seq": led.get("seq"), "advice": receipt.get("advice"),
            "detail": "evidence=" + kind}


# ---------------------------------------------------------------- claude_cli adapter (ADV-CLAUDECLI-01)
def _cli_env(env):
    out = dict(env if env is not None else os.environ)
    for k in list(out):
        if k.upper() in CLI_STRIP_ENV:
            del out[k]
    return out


def prepare_cli_cwd(root):
    """Fixed git-initialised directory under the system temp dir, never inside the repo. Any failure is zero-launch."""
    def fail(detail):
        raise Stop(E_FAILED, "CALL_FAILED", "cli_unavailable", detail)
    try:
        parent = Path(tempfile.gettempdir()).resolve()
        root_r = Path(root).resolve()
    except OSError:
        fail("cwd_prepare_failed")
    base = parent / CLI_CWD_NAME
    if base.exists() and not base.is_dir():
        fail("cwd_not_directory")
    if base == root_r or root_r in base.parents:
        fail("cwd_inside_repo")
    try:
        base.mkdir(parents=True, exist_ok=True)
    except OSError:
        fail("cwd_prepare_failed")
    if not (base / ".git").exists():
        try:
            res = subprocess.run(["git", "init", "-q"], cwd=str(base), capture_output=True, timeout=30)
        except (OSError, subprocess.SubprocessError):
            fail("cwd_prepare_failed")
        if res.returncode != 0:
            fail("cwd_prepare_failed")
    return base


def kill_cli_tree(proc):
    """Kill the whole process tree, then reap. Bounded: one escalation (Popen.kill), every wait has a ceiling."""
    failed = False
    try:
        if os.name == "nt":
            res = subprocess.run(["taskkill", "/F", "/T", "/PID", str(proc.pid)], capture_output=True, timeout=15)
            failed = res.returncode != 0
        else:
            os.killpg(proc.pid, signal.SIGKILL)
    except (OSError, subprocess.SubprocessError):
        failed = True
    if failed and proc.poll() is None:
        try:
            proc.kill()
        except OSError:
            pass
    try:
        proc.wait(timeout=10)
        return True
    except subprocess.TimeoutExpired:
        return False


def _close_pipes(proc):
    for f in (proc.stdin, proc.stdout, proc.stderr):
        try:
            if f:
                f.close()
        except OSError:
            pass


def parse_cli_stream(text):
    p = {"events": 0, "init": None, "hooks": {}, "retries": 0, "retry_error": None, "assistant_error": None, "result": None}
    for line in (text or "").splitlines():
        line = line.strip()
        if not line.startswith("{"):
            continue
        try:
            e = json.loads(line)
        except ValueError:
            continue
        if not isinstance(e, dict):
            continue
        p["events"] += 1
        t, st = e.get("type"), e.get("subtype")
        if t == "system" and st == "init":
            if p["init"] is None:
                p["init"] = e
        elif t == "system" and st in ("hook_started", "hook_response"):
            h = p["hooks"].setdefault(e.get("hook_id") or f"#{len(p['hooks'])}",
                                      {"event": e.get("hook_event"), "name": e.get("hook_name"),
                                       "outcome": None, "exit_code": None, "stdout": ""})
            if st == "hook_response":
                h["outcome"], h["exit_code"] = e.get("outcome"), e.get("exit_code")
                h["stdout"] = e.get("stdout") if isinstance(e.get("stdout"), str) else ""
        elif t == "system" and st == "api_retry":
            p["retries"] += 1
            p["retry_error"] = e.get("error")
        elif t == "assistant":
            if p["assistant_error"] is None and e.get("error"):
                p["assistant_error"] = e.get("error")
        elif t == "result":
            p["result"] = e
    return p


def cli_key_source_text(init):
    if init is None or "apiKeySource" not in init:
        return "<absent>"
    v = init["apiKeySource"]
    if v is None:
        return "<null>"
    return scrub(one_line(v if isinstance(v, str) else json.dumps(v, ensure_ascii=False), 200))


def judge_cli_stream(p, rc, model):
    """First hit wins. None = success. Otherwise (exit code, disposition, reason, detail)."""
    def fail(reason, detail=None):
        return E_FAILED, "CALL_FAILED", reason, detail

    def refuse(reason, detail=None):
        return E_REFUSED, "REFUSED", reason, detail
    init, res = p["init"], p["result"]
    if p["events"] == 0:
        return fail("nonzero_exit" if rc else "unparseable", "no parseable JSON event on stdout")
    if init is None:
        return fail("init_missing", "no system/init event")
    if "apiKeySource" not in init:
        return refuse("auth_unreported", "system/init carries no apiKeySource key")
    if init["apiKeySource"] is not None and init["apiKeySource"] != "none":
        return refuse("api_key_source", cli_key_source_text(init))
    if res is None:
        return fail("nonzero_exit" if rc else "unparseable", "no result event")
    is_error = res.get("is_error")
    if not isinstance(is_error, bool):
        return fail("unparseable", "is_error_invalid")
    if is_error:
        cat = p["assistant_error"]
        detail = one_line(f"error={cat} terminal_reason={res.get('terminal_reason')}", 300)
        if cat in ("authentication_failed", "oauth_org_not_allowed", "cloud_credential_error"):
            return fail("not_logged_in", detail)
        if cat == "model_not_found":
            return fail("model_unavailable", detail)
        return fail("cli_error", detail)
    if rc:
        return fail("nonzero_exit", f"exit code {rc} with a successful result event")
    mu = res.get("modelUsage")
    if mu is not None and not isinstance(mu, dict):
        return fail("unparseable", "model_usage_invalid")
    used = set(mu or {})
    if not used:
        return refuse("model_unreported", "result.modelUsage is empty or missing")
    if model not in used or init.get("model") != model:
        return refuse("model_mismatch", f"requested={model} init={init.get('model')} used={sorted(used)}")
    if len(used) > 1:
        return refuse("multiple_models", f"used={sorted(used)}")
    ans = res.get("result")
    if not isinstance(ans, str):
        return fail("unparseable", "answer_invalid")
    if not ans.strip():
        return refuse("empty_answer", "result.result is blank")
    return None


def cli_info(p, mode, cli, ms, reaped=None, diagnostic=False):
    res = p["result"] or {}
    mu = res.get("modelUsage") if isinstance(res.get("modelUsage"), dict) else {}
    keys = ("inputTokens", "outputTokens", "cacheReadInputTokens", "cacheCreationInputTokens")
    hooks, warns = [], []
    for h in p["hooks"].values():
        hooks.append({k: h[k] for k in ("event", "name", "outcome", "exit_code")})
        if h["exit_code"] not in (0, None):
            warns.append(f"hook_nonzero_exit name={h['name']} exit_code={h['exit_code']}")
        if h["stdout"].strip():
            warns.append(f"hook_stdout_nonempty name={h['name']}")
    if p["retries"]:
        warns.append(f"api_retry count={p['retries']} last_error={p['retry_error']}")
    init = p["init"] or {}
    info = {"mode": mode, "channel": "claude-subscription-cli", "requested_model": cli["model"],
            "models_used": {m: {k: (v or {}).get(k) for k in keys} for m, v in mu.items()},
            "effort_flag": cli["effort"], "api_key_source": cli_key_source_text(p["init"]),
            "session_id": res.get("session_id") or init.get("session_id"),
            "usage": res.get("usage") if isinstance(res.get("usage"), dict) else None,
            "cost_reported_usd": res.get("total_cost_usd"),
            "cost_note": "CLI list-price figure, not billing", "duration_ms": ms,
            "cli_internal_retries": p["retries"], "hooks": hooks, "warnings": warns}
    if reaped is not None:
        info["reaped"] = reaped
    if diagnostic:
        info["diagnostic"] = True
    return info


def write_cli_record(dirp, doc, stamp):
    """Bounded, non-fatal. Returns (path | None, error class name | None)."""
    try:
        dirp.mkdir(parents=True, exist_ok=True)
    except OSError as exc:
        return None, type(exc).__name__
    text = scrub(json.dumps(doc, ensure_ascii=False, sort_keys=True))
    err = "FileExistsError"
    for _ in range(3):
        path = dirp / f"advisor_cli_{stamp}_{uuid.uuid4().hex[:8]}.json"
        try:
            with open(path, "x", encoding="utf-8") as fh:
                fh.write(text)
            return path, None
        except FileExistsError:
            continue
        except OSError as exc:
            return None, type(exc).__name__
    return None, err


def do_claude_cli(a, src, root, ticket_arg, env, now):
    cli = src["cli"]
    ticketless = bool(a.no_ticket)
    if ticketless:
        if a.incident or a.evidence or ticket_arg:
            raise Stop(E_USAGE, "USAGE", "usage", "--no-ticket cannot be combined with --incident, --evidence or --ticket")
        if not a.payload:
            raise Stop(E_USAGE, "USAGE", "usage", f"{a.source} needs --payload")
        ticket = None
    else:
        if not (a.incident and a.payload and a.evidence):
            raise Stop(E_USAGE, "USAGE", "usage", f"{a.source} needs --incident, --payload and --evidence (or --no-ticket with --payload)")
        st, _ = precheck(root, ticket_arg, a.incident)
        ticket = st["ticket"]
    try:
        pbytes = Path(a.payload).read_bytes()
    except OSError as exc:
        raise Stop(E_REFUSED, "REFUSED", "request_invalid", f"payload unreadable: {type(exc).__name__}")
    if len(pbytes) > CLI_PAYLOAD_MAX:
        raise Stop(E_REFUSED, "REFUSED", "request_invalid", f"payload larger than {CLI_PAYLOAD_MAX} bytes")
    try:
        ptext = pbytes.decode("utf-8")
    except UnicodeDecodeError:
        raise Stop(E_REFUSED, "REFUSED", "request_invalid", "payload is not UTF-8")
    psha = sha(pbytes)
    kind = h = None
    if not ticketless:
        kind, h = check_tokens(ptext, a.incident, a.evidence)            # M4, M5 (before any launch)
    exe = shutil.which(cli["bin"])
    if not exe:
        raise Stop(E_FAILED, "CALL_FAILED", "cli_unavailable", f"bin_not_found: {cli['bin']}")
    cwd = prepare_cli_cwd(root)
    argv = [exe] + list(cli["prefix_args"]) + [
        "-p", "--model", cli["model"], "--effort", cli["effort"], "--tools", "", "--strict-mcp-config",
        "--disable-slash-commands", "--no-session-persistence", "--output-format", "stream-json", "--verbose"]
    prompt = (CLI_PREAMBLE + "\n\n" + ptext).encode("utf-8")
    flags = subprocess.CREATE_NEW_PROCESS_GROUP if os.name == "nt" else 0
    mode = "ticketless" if ticketless else "ticketed"
    t0 = time.monotonic()
    try:
        proc = subprocess.Popen(argv, cwd=str(cwd), env=_cli_env(env), stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                                stderr=subprocess.PIPE, creationflags=flags, start_new_session=(os.name != "nt"))
    except OSError as exc:
        raise Stop(E_FAILED, "CALL_FAILED", "cli_unavailable", f"launch failed: {type(exc).__name__}")
    timed_out, reaped = False, None
    try:
        out_b, err_b = proc.communicate(input=prompt, timeout=cli["timeout_s"])
    except subprocess.TimeoutExpired:
        timed_out = True
        reaped = kill_cli_tree(proc)
        out_b = err_b = b""
        if reaped:
            try:
                out_b, err_b = proc.communicate(timeout=5)
            except (subprocess.TimeoutExpired, OSError, ValueError):
                out_b = err_b = b""
        _close_pipes(proc)
    ms = int((time.monotonic() - t0) * 1000)
    out_t = (out_b or b"").decode("utf-8", "replace")
    err_t = (err_b or b"").decode("utf-8", "replace")
    p = parse_cli_stream(out_t)
    info = cli_info(p, mode, cli, ms, reaped=reaped, diagnostic=timed_out)
    if timed_out:
        verdict = (E_FAILED, "CALL_FAILED", "timeout", f"no result within {cli['timeout_s']}s; process tree killed")
        if reaped is False:
            verdict = (E_FAILED, "CALL_FAILED", "timeout",
                       f"no result within {cli['timeout_s']}s; process pid={proc.pid} not reaped")
            info["warnings"].append(f"process_not_reaped pid={proc.pid}")
    else:
        verdict = judge_cli_stream(p, proc.returncode, cli["model"])
    stamp = now().strftime("%Y%m%dT%H%M%SZ")
    rdir = (root / "tmp" / ticket / "advisor_cli") if ticket else (root / "tmp" / "_advisor_cli_ticketless")
    rec_doc = {"verdict": "SUCCESS" if verdict is None else {"disposition": verdict[1], "reason": verdict[2], "detail": verdict[3]},
               "payload_sha256": psha, "returncode": proc.returncode, "source": a.source, "envelope": dict(info),
               "answer": ((p["result"] or {}).get("result") if verdict is None else None),
               "stream": out_t[:100000], "stderr": err_t[:4000],
               "note": "local execution record; not billing or provider-side proof"}
    rec_path, rec_err = write_cli_record(rdir, rec_doc, stamp)
    info["record"] = rel(root, rec_path) if rec_path else None
    if rec_err:
        info["record_error"] = rec_err
        info["warnings"].append(f"record_error {rec_err}")
    if verdict is not None:
        code, disp, reason, detail = verdict
        info = json.loads(scrub(json.dumps(info, ensure_ascii=False)))
        raise Stop(code, disp, reason, scrub(detail or ""), **info)
    answer = p["result"]["result"]
    info["model"] = cli["model"]
    info["answer"] = answer
    info = json.loads(scrub(json.dumps(info, ensure_ascii=False)))
    if ticketless:
        return dict(info, ok=True, disposition="ANSWERED", detail="ticketless: no ledger event was written")
    note = (f"source={a.source} via={VIA} channel=claude-subscription-cli model={cli['model']} effort_flag={cli['effort']} "
            f"api_key_source={info['api_key_source']} session_id={info['session_id'] or 'none'} payload_sha256={psha} "
            f"incident={a.incident} evidence={kind}:{h} record={info['record'] or 'none'}")
    led = ledger_consult(
        root, ticket, a.incident, note,
        closed_detail=("advisor reply obtained and its execution record kept (cost already incurred), but the target incident is "
                       "no longer open: nothing was recorded, the CLI was not called again, the incident was not reopened"),
        closed_extra=dict(info, recorded=False))
    return dict(info, ok=True, disposition="RECORDED", ledger_seq=led.get("seq"), detail="evidence=" + kind)


def native_slot(inc):
    return int((inc.get("consults") or {}).get("advisor", 0))


def ppss_slot(st, incident):
    """Monotonic per-incident ppss_record event count (ledger `incidents[].ppss_records`). status.ppss[] is a
    result index keyed by request_id and must not be used as the consumption counter, so a ledger that does not
    report the count is refused outright instead of falling back to counting the index."""
    inc = next((i for i in st.get("incidents", []) if i.get("id") == incident), None)
    if inc is None or isinstance(inc.get("ppss_records"), bool) or not isinstance(inc.get("ppss_records"), int):
        raise Stop(E_REFUSED, "REFUSED", "ledger_incompatible",
                   "ledger status has no incidents[].ppss_records (older gate_ledger.py): PPSS dispatch/bind/record refused, "
                   "no fallback to the request-id index")
    return inc["ppss_records"]


def desc_path(root, ticket, did):
    return Path(root) / "tmp" / ticket / "route" / f"dispatch_{did}.json"


def save_descriptor(root, ticket, did, doc, slot, when):
    p = desc_path(root, ticket, did)
    digest = digest_request(doc)
    try:
        old = json.loads(p.read_text(encoding="utf-8"))
        if isinstance(old, dict) and old.get("digest") == digest and old.get("request") == doc:
            return p                                    # same request again: keep the first dispatched_at
    except (OSError, ValueError):
        pass
    body = {"dispatch_id": did, "ticket": ticket, "slot": slot, "digest": digest,
            "dispatched_at": when.isoformat() if hasattr(when, "isoformat") else str(when),
            "request": doc}
    write_descriptor(p, body)
    return p


def write_descriptor(p, body):
    tmpf = p.with_name(p.name + f".{os.getpid()}.tmp")
    try:
        p.parent.mkdir(parents=True, exist_ok=True)
        tmpf.write_text(json.dumps(body, ensure_ascii=False, sort_keys=True), encoding="utf-8")
        os.replace(str(tmpf), str(p))
    except OSError as exc:
        try:
            tmpf.unlink()
        except OSError:
            pass
        raise Stop(E_FAILED, "CALL_FAILED", "scratch_unwritable", f"cannot save the dispatch description: {type(exc).__name__}")


def norm_path(s):
    try:
        return Path(os.path.abspath(str(s))).as_posix()
    except (OSError, ValueError):
        return str(s)


def load_descriptor(root, ticket, did):
    try:
        d = json.loads(desc_path(root, ticket, did).read_text(encoding="utf-8"))
    except (OSError, ValueError):
        raise Stop(E_REFUSED, "REFUSED", "dispatch_mismatch", "no dispatch description for this id (dispatch through this entry first)")
    req = d.get("request") if isinstance(d, dict) else None
    if not isinstance(req, dict) or d.get("dispatch_id") != did or digest_request(req) != d.get("digest"):
        raise Stop(E_REFUSED, "REFUSED", "dispatch_mismatch", "dispatch description is corrupt or does not belong to this id")
    return d


def parse_when(s):
    try:
        v = datetime.fromisoformat(str(s).replace("Z", "+00:00"))
    except ValueError:
        return None
    return v if v.tzinfo else v.replace(tzinfo=timezone.utc)


def classify_mismatch(given, ticket, incident, source, n, digest):
    for k in range(max(0, n - STALE_LOOKBACK), n):
        if dispatch_id(ticket, incident, source, k, digest) == given:
            return "stale_or_duplicate_dispatch"
    return "dispatch_mismatch"


def verify_dispatch(a, ticket, incident, source, n, digest):
    given = a.dispatch_id or ""
    if not DISPATCH_ID_RE.match(given):
        raise Stop(E_REFUSED, "REFUSED", "request_invalid", "dispatch id missing or malformed")
    if given != dispatch_id(ticket, incident, source, n, digest):
        raise Stop(E_REFUSED, "REFUSED", classify_mismatch(given, ticket, incident, source, n, digest),
                   "dispatch id does not match the current slot and request description")


def cli_prefix(source, incident):
    return f"python .claude/scripts/route.py consult --incident {incident} --source {source}"


def do_native(a, table_src, root, ticket_arg):
    if a.phase == "dispatch":
        purpose = norm_text(a.purpose)
        if not purpose or len(purpose) > PURPOSE_MAX:
            raise Stop(E_REFUSED, "REFUSED", "request_invalid", f"--purpose must be 1..{PURPOSE_MAX} characters")
        st, inc = precheck(root, ticket_arg, a.incident)
        did = dispatch_id(st["ticket"], a.incident, a.source, native_slot(inc), digest_purpose(purpose))
        return {"ok": True, "disposition": "DISPATCHED", "dispatch_id": did,
                "action": {"type": "model_tool", "tool": table_src["dispatch"]["tool"], "params": table_src["dispatch"]["params"]},
                "return_with": f"{cli_prefix(a.source, a.incident)} --phase return --dispatch-id {did} "
                               f"--purpose <同派發之 --purpose> --note <原生 advisor 呼叫之引用>",
                "detail": "dispatch only: this entry cannot confirm the native tool is called nor verify its content"}
    purpose = norm_text(a.purpose)
    ref = one_line(a.note)
    if not purpose or len(purpose) > PURPOSE_MAX or not ref:
        raise Stop(E_REFUSED, "REFUSED", "request_invalid", "return needs a non-empty --purpose and --note")
    with route_lock(root, ticket_of(root, ticket_arg), a.lock_wait_ms):
        st, inc = precheck(root, ticket_arg, a.incident)
        digest = digest_purpose(purpose)
        verify_dispatch(a, st["ticket"], a.incident, a.source, native_slot(inc), digest)
        note = (f"source={a.source} via={VIA} dispatch={a.dispatch_id} "
                f"purpose_sha256={digest} ref={ref}")
        led = ledger_consult(root, st["ticket"], a.incident, note)
    return {"ok": True, "disposition": "RECORDED", "dispatch_id": a.dispatch_id, "ledger_seq": led.get("seq"),
            "detail": "recorded as a declaration: native tool execution and content are not verifiable here"}


def ticket_of(root, ticket_arg):
    return ledger_status(root, ticket_arg)["ticket"]


def existing_summary(st, incident):
    return [{"request_id": r.get("request_id"), "state": r.get("state"), "urls": r.get("urls", []),
             "reads": len(r.get("reads", []))} for r in st.get("ppss", []) if r.get("incident") == incident]


def do_ppss(a, table_src, root, ticket_arg, now):
    if a.phase == "read":
        for flag in ("request_id", "url", "source_file", "supports"):
            if not getattr(a, flag):
                raise Stop(E_USAGE, "USAGE", "usage", f"--{flag.replace('_', '-')} is required for read")
        precheck(root, ticket_arg, a.incident)
        args = ["ppss", "read", "--incident", a.incident, "--request-id", a.request_id, "--url", a.url,
                "--source-file", a.source_file, "--supports", a.supports]
        if a.quote:
            args += ["--quote", a.quote]
        led = ledger_write(root, ticket_of(root, ticket_arg), args)
        return {"ok": True, "disposition": "RECORDED", "ledger": led, "request_id": a.request_id}
    if a.phase == "dispatch":
        question = norm_text(a.question)
        queries = [norm_text(q) for q in (a.query or []) if norm_text(q)]
        nq = norm_queries(queries)
        domains = [d.strip().lower() for d in (a.domains or "").split(",") if d.strip()]
        justification = None
        if a.justification_json:
            try:
                justification = json.loads(a.justification_json)
            except ValueError:
                justification = None
            if not isinstance(justification, dict):
                raise Stop(E_REFUSED, "REFUSED", "request_invalid", "--justification-json must be a JSON object")
        task_type = norm_text(a.task_type) or "gate_ppss"
        if not question or not (1 <= len(nq) <= 5) or any(not DOMAIN_RE.match(d) for d in domains):
            raise Stop(E_REFUSED, "REFUSED", "request_invalid",
                       "question must be non-empty, 1..5 distinct queries, domains must be valid hostnames")
        st, _ = precheck(root, ticket_arg, a.incident)
        prior = existing_summary(st, a.incident)
        if any(r["state"] == "RETRIEVED" and r["reads"] == 0 for r in prior):
            raise Stop(E_REFUSED, "REFUSED", "existing_results_unconsumed",
                       "consume the retrieved results (--phase read) before paying for another request", existing=prior)
        if prior and not (norm_text(a.new_query_strategy) and norm_text(a.reason)):
            raise Stop(E_REFUSED, "REFUSED", "prior_research_requires_strategy",
                       "this incident already has PPSS records: pass --new-query-strategy and --reason", existing=prior)
        doc = canon_request(question, queries, task_type, domains, justification)
        slot = ppss_slot(st, a.incident)
        did = dispatch_id(st["ticket"], a.incident, a.source, slot, digest_request(doc))
        save_descriptor(root, st["ticket"], did, doc, slot, now())
        seen, params_queries = set(), []
        for q in queries:
            if q.casefold() not in seen:
                seen.add(q.casefold())
                params_queries.append(q)
        params = {"question": question, "queries": params_queries, "task_type": task_type}
        if domains:
            params["filters"] = {"domains": domains}
        if justification is not None:
            params["justification"] = justification
        extra = (" --new-query-strategy <同派發> --reason <同派發>" if prior else "")
        return {"ok": True, "disposition": "DISPATCHED", "dispatch_id": did,
                "action": {"type": "model_tool", "tool": table_src["dispatch"]["tool"], "params": params},
                "return_with": (f"{cli_prefix(a.source, a.incident)} --phase bind --dispatch-id {did} "
                                f"--request-id <research_submit 回件之 request_id> --artifact <回件之 artifact_path>"
                                f" [--cached-artifact 僅當回件 cache 欄為 hit]；再 {cli_prefix(a.source, a.incident)} "
                                f"--phase record --dispatch-id {did} --artifact <同一 artifact_path>{extra}"),
                "detail": "dispatch only: this entry cannot call the MCP tool nor observe its cost"}
    if a.phase == "bind":
        return ppss_bind(a, root, ticket_arg, now)
    # record
    if a.cached_artifact:
        raise Stop(E_USAGE, "USAGE", "usage", "--cached-artifact belongs to --phase bind (it only exempts the time condition)")
    if not a.artifact:
        raise Stop(E_USAGE, "USAGE", "usage", "--artifact is required for record")
    given = a.dispatch_id or ""
    if not DISPATCH_ID_RE.match(given):
        raise Stop(E_REFUSED, "REFUSED", "request_invalid", "dispatch id missing or malformed")
    ticket = ticket_of(root, ticket_arg)
    with route_lock(root, ticket, a.lock_wait_ms):
        st, _ = precheck(root, ticket_arg, a.incident)
        desc = load_descriptor(root, ticket, given)
        req = desc["request"]
        verify_dispatch(a, st["ticket"], a.incident, a.source, ppss_slot(st, a.incident), desc["digest"])
        bound = desc.get("binding")
        if not isinstance(bound, dict):
            raise Stop(E_REFUSED, "REFUSED", "unbound_dispatch",
                       "no research_submit response is bound to this dispatch: run --phase bind first")
        try:
            sha_now = sha(Path(a.artifact).read_bytes())
        except OSError as exc:
            raise Stop(E_REFUSED, "REFUSED", "request_invalid", f"artifact unreadable: {type(exc).__name__}")
        if norm_path(a.artifact) != bound.get("artifact_path") or sha_now != bound.get("artifact_sha256"):
            raise Stop(E_REFUSED, "REFUSED", "binding_mismatch",
                       "the artifact path or content differs from what was bound to this dispatch")
        want_domains = list((req.get("filters") or {}).get("domains") or [])
        if a.domains and {d.strip().lower() for d in a.domains.split(",") if d.strip()} != set(want_domains):
            raise Stop(E_REFUSED, "REFUSED", "dispatch_mismatch", "--domains differs from the dispatched request")
        args = ["ppss", "record", "--incident", a.incident, "--artifact", a.artifact]
        if want_domains:
            args += ["--domains", ",".join(want_domains)]
        if a.new_query_strategy:
            args += ["--new-query-strategy", a.new_query_strategy]
        if a.reason:
            args += ["--reason", a.reason]
        try:
            led = ledger_write(root, st["ticket"], args)
        except Stop as stop:
            if stop.reason == "ledger_refused" and "already recorded" in (stop.detail or ""):
                raise Stop(E_REFUSED, "REFUSED", "duplicate_request_id", stop.detail)
            raise
    return {"ok": True, "disposition": "RECORDED", "dispatch_id": a.dispatch_id, "request_id": led.get("request_id"),
            "ledger_seq": led.get("seq"), "ledger": led}


def ppss_bind(a, root, ticket_arg, now):
    """Bind the research_submit response (request_id, artifact path, artifact sha256) to one dispatch description.
    No ledger event. Declared inputs are compared with each other and with the saved description; whether the model
    really called the tool with the dispatched parameters is not proven."""
    given = a.dispatch_id or ""
    if not DISPATCH_ID_RE.match(given):
        raise Stop(E_REFUSED, "REFUSED", "request_invalid", "dispatch id missing or malformed")
    tool_rid = norm_text(a.request_id)
    if not tool_rid or not a.artifact:
        raise Stop(E_USAGE, "USAGE", "usage", "--request-id and --artifact are required for bind")
    try:
        raw = Path(a.artifact).read_bytes()
        art = json.loads(raw.decode("utf-8"))
    except (OSError, ValueError) as exc:
        raise Stop(E_REFUSED, "REFUSED", "request_invalid", f"artifact unreadable: {type(exc).__name__}")
    aq = art.get("queries") if isinstance(art, dict) else None
    anq = norm_queries([q for q in aq if isinstance(q, str)]) if isinstance(aq, list) else []
    art_rid = art.get("request_id") if isinstance(art, dict) else None
    if not anq or not isinstance(art_rid, str) or not art_rid.strip():
        raise Stop(E_REFUSED, "REFUSED", "dispatch_mismatch", "artifact has no usable queries or request_id field")
    art_sha, art_path = sha(raw), norm_path(a.artifact)
    ticket = ticket_of(root, ticket_arg)
    with route_lock(root, ticket, a.lock_wait_ms):
        st, _ = precheck(root, ticket_arg, a.incident)
        desc = load_descriptor(root, ticket, given)
        req = desc["request"]
        verify_dispatch(a, st["ticket"], a.incident, a.source, ppss_slot(st, a.incident), desc["digest"])
        if set(anq) != set(req.get("queries") or []):
            raise Stop(E_REFUSED, "REFUSED", "dispatch_mismatch", "artifact queries differ from the dispatched request")
        cap, sent = parse_when(art.get("captured_at")), parse_when(desc.get("dispatched_at"))
        if cap is None or sent is None:
            raise Stop(E_REFUSED, "REFUSED", "dispatch_mismatch", "artifact captured_at or dispatch time unreadable")
        predates = cap < sent
        if predates and not a.cached_artifact:
            raise Stop(E_REFUSED, "REFUSED", "artifact_predates_dispatch",
                       "artifact was captured before this dispatch; if research_submit reported cache=hit, bind again "
                       "with --cached-artifact (declared; it exempts only this time condition)")
        if art_rid != tool_rid and not (a.cached_artifact and predates):
            raise Stop(E_REFUSED, "REFUSED", "request_id_mismatch",
                       "--request-id differs from the artifact's request_id (only a declared cache hit, whose artifact "
                       "predates the dispatch, may carry an older artifact request_id)")
        for other in desc_path(root, ticket, given).parent.glob("dispatch_d2-*.json"):
            if other.name == desc_path(root, ticket, given).name:
                continue
            try:
                ob = (json.loads(other.read_text(encoding="utf-8")) or {}).get("binding")
            except (OSError, ValueError):
                continue
            if not isinstance(ob, dict):
                continue
            if ob.get("request_id") == tool_rid or (ob.get("artifact_sha256") == art_sha and ob.get("incident") == a.incident):
                raise Stop(E_REFUSED, "REFUSED", "artifact_bound_elsewhere",
                           "this research response (request_id or artifact) is already bound to another dispatch")
        new = {"request_id": tool_rid, "artifact_path": art_path, "artifact_sha256": art_sha,
               "artifact_request_id": art_rid, "incident": a.incident, "cached_declared": bool(a.cached_artifact),
               "bound_at": now().isoformat()}
        old = desc.get("binding")
        if isinstance(old, dict):
            same = all(old.get(k) == new[k] for k in ("request_id", "artifact_path", "artifact_sha256"))
            if not same:
                raise Stop(E_REFUSED, "REFUSED", "dispatch_already_bound",
                           "this dispatch is already bound to a different research response")
        else:
            body = dict(desc)
            body["binding"] = new
            write_descriptor(desc_path(root, ticket, given), body)
    return {"ok": True, "disposition": "BOUND", "dispatch_id": given, "request_id": tool_rid,
            "artifact_sha256": art_sha, "artifact_request_id": art_rid,
            "detail": "bound only: no ledger event; the next step is --phase record with the same --artifact"}


PHASES = {"api_advisor": (None, "call"), "native_advisor": ("dispatch", "return"), "claude_cli": (None, "call"),
          "ppss": ("dispatch", "bind", "record", "read")}


def cmd_consult(a, client_factory, env, now):
    table = load_table(a.table or TABLE_DEFAULT)
    src = resolve_source(table, a.source)
    adapter = src["adapter"]
    phases = PHASES[adapter]
    if a.phase not in phases:
        raise Stop(E_USAGE, "USAGE", "usage", f"{a.source}: --phase must be one of {[p for p in phases if p]}")
    if adapter != "claude_cli":
        if a.no_ticket:
            raise Stop(E_USAGE, "USAGE", "usage", f"--no-ticket is only valid for the claude_cli source, not {a.source}")
        if not a.incident:
            raise Stop(E_USAGE, "USAGE", "usage", f"--incident is required for {a.source}")
    root = repo_root()
    if adapter == "api_advisor":
        body = do_api(a, root, a.ticket, client_factory, env, now)
    elif adapter == "native_advisor":
        body = do_native(a, src, root, a.ticket)
    elif adapter == "claude_cli":
        body = do_claude_cli(a, src, root, a.ticket, env, now)
    else:
        body = do_ppss(a, src, root, a.ticket, now)
    return body


def cmd_table_check(a):
    table = load_table(a.table or TABLE_DEFAULT)
    srcs = table["sources"]
    return {"ok": True, "disposition": "OK", "detail": "table valid",
            "sources": sorted(srcs), "callable": sorted(n for n, s in srcs.items() if s["kind"] in CALLABLE_KINDS),
            "enabled": sorted(n for n, s in srcs.items() if s["kind"] in CALLABLE_KINDS and s.get("enabled") is True)}


def build_parser():
    p = _Parser(prog="route.py", description="central entry for advisor / PPSS consults")
    p.add_argument("--ticket")
    p.add_argument("--table")
    sub = p.add_subparsers(dest="verb", required=True, parser_class=_Parser)
    c = sub.add_parser("consult")
    c.add_argument("--incident")
    c.add_argument("--no-ticket", dest="no_ticket", action="store_true")
    c.add_argument("--source", required=True)
    c.add_argument("--phase")
    c.add_argument("--payload")
    c.add_argument("--evidence")
    c.add_argument("--effort")
    c.add_argument("--max-tokens", dest="max_tokens")
    c.add_argument("--timeout")
    c.add_argument("--request-mode", dest="request_mode")
    c.add_argument("--purpose")
    c.add_argument("--note")
    c.add_argument("--dispatch-id", dest="dispatch_id")
    c.add_argument("--question")
    c.add_argument("--query", action="append")
    c.add_argument("--task-type", dest="task_type")
    c.add_argument("--domains")
    c.add_argument("--justification-json", dest="justification_json")
    c.add_argument("--new-query-strategy", dest="new_query_strategy")
    c.add_argument("--reason")
    c.add_argument("--artifact")
    c.add_argument("--request-id", dest="request_id")
    c.add_argument("--url")
    c.add_argument("--source-file", dest="source_file")
    c.add_argument("--supports")
    c.add_argument("--quote")
    c.add_argument("--cached-artifact", dest="cached_artifact", action="store_true")
    c.add_argument("--lock-wait-ms", dest="lock_wait_ms", type=int, default=LOCK_WAIT_MS_DEFAULT)
    t = sub.add_parser("table")
    t.add_argument("action", choices=["check"])
    return p


def main(argv=None, *, client_factory=None, env=None, now=None) -> int:
    _utf8()
    now = now or (lambda: datetime.now(timezone.utc))
    verb, source, phase = None, None, None
    try:
        a = build_parser().parse_args(argv)
        verb = a.verb
        if verb == "consult":
            source, phase = a.source, a.phase
            body = cmd_consult(a, client_factory, env, now)
        else:
            verb = "table check"
            body = cmd_table_check(a)
        env_out = {"ok": True, "verb": verb, "via": VIA}
        env_out.update(body)
        if source:
            env_out.setdefault("source", source)
        if phase:
            env_out.setdefault("phase", phase)
        print(json.dumps(env_out, ensure_ascii=False))
        return E_OK
    except Stop as stop:
        out = {"ok": False, "verb": verb or "consult", "disposition": stop.disposition, "via": VIA}
        if source:
            out["source"] = source
        if phase:
            out["phase"] = phase
        if stop.reason:
            out["reason"] = stop.reason
        if stop.detail:
            out["detail"] = stop.detail
        out.update(stop.extra)
        print(json.dumps(out, ensure_ascii=False))
        print(f"route.py: {stop.disposition} {stop.reason or ''}".strip(), file=sys.stderr)
        return stop.code
    except SystemExit as exc:
        return exc.code if isinstance(exc.code, int) else 0


if __name__ == "__main__":
    sys.exit(main())
