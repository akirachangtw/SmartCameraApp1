#!/usr/bin/env python3
"""route.py - single central entry for engineering-advisor / PPSS consults (ADV-ENTRY-01, V1).

Usage:
    python .claude/scripts/route.py [--ticket ID] [--table PATH] consult --incident INC --source NAME [--phase P] [...]
    python .claude/scripts/route.py [--table PATH] table check

Sources (route_table.json):
    advisor.api     script-call: ONE invocation does the checks, ONE network attempt through advisor_call.main()
                    (no retry, no mode switch, no fallback), and records `consult --kind advisor` on exit 0.
                    flags: --payload PATH --evidence PATH [--effort E --max-tokens N --timeout S --request-mode M]
    advisor.native  model-dispatch: --phase dispatch --purpose TEXT  ->  the main agent calls the native `advisor`
                    tool  ->  --phase return --dispatch-id ID --purpose TEXT --note REF (records the consult).
    research.ppss   model-dispatch: --phase dispatch (pre-flight judgement, then the concrete research_submit
                    params) -> main agent calls research_submit -> --phase record --dispatch-id ID --artifact PATH
                    (pass-through of `ppss record`) -> --phase read ... (pass-through of `ppss read`).
    Disabled source: SOURCE_DISABLED + `alternatives` (same role, callable, enabled). The entry never picks one
    for the caller. No enabled source of that role at all: MISSING_REQUIRED.

Output: ALWAYS exactly one JSON object on stdout (stderr only carries a short human note on failure).
Envelope: {ok, verb, disposition, source?, phase?, via:"route.py", reason?, detail?, action?, return_with?,
           dispatch_id?, existing?, alternatives?, ledger_seq?, receipt?, receipt_sha256?, request_id?,
           advisor_exit?, advice?, ledger?}
Exit codes (own set; not the ledger's, not advisor_call's):
    0 OK (DISPATCHED | RECORDED)   2 usage   3 route table missing/invalid
    4 UNKNOWN_SOURCE | NOT_CALLABLE | SOURCE_DISABLED | MISSING_REQUIRED
    5 REFUSED (reason: incident_not_open ticket_not_mutable incident_token_missing|mismatch|ambiguous
       evidence_token_missing|ambiguous evidence_file_unreadable|empty evidence_hash_mismatch receipt_mismatch
       request_invalid dispatch_mismatch stale_or_duplicate_dispatch existing_results_unconsumed
       prior_research_requires_strategy busy ledger_refused)
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
n = advisor consults (native) / PPSS records (ppss) of the incident; digest = purpose (native) or the normalized
query set recovered from the research artifact (ppss). Return/record re-computes with the CURRENT n inside a
transient mutex (<git-common-dir>/gate/<ticket>.route.lock: pid+nonce only, not state, removed on exit, stale
after 120 s) because the ledger's `consult` has no CAS. The mutex serializes only returns made THROUGH this entry.

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
import subprocess
import sys
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
ADAPTERS = ("api_advisor", "native_advisor", "ppss")
NAME_RE = re.compile(r"^[a-z]+\.[a-z0-9][a-z0-9-]*$")
DISPATCH_ID_RE = re.compile(r"^d2-[0-9a-f]{16}$")
DOMAIN_RE = re.compile(r"^[a-z0-9]([a-z0-9-]*[a-z0-9])?(\.[a-z0-9]([a-z0-9-]*[a-z0-9])?)+$")
EMPTY_SHA = hashlib.sha256(b"").hexdigest()
PURPOSE_MAX = 500
STALE_LOCK_SECONDS = 120
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


def digest_queries(queries):
    return sha("\n".join(sorted(set(norm_queries(queries)))).encode("utf-8"))


def digest_purpose(purpose):
    return sha(norm_text(purpose).encode("utf-8"))


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
            if s.get("adapter") in ("api_advisor", "native_advisor") and s.get("ledger_kind") != "advisor":
                errs.append(f"{name}: ledger_kind must be 'advisor'")
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
            try:
                if time.time() - path.stat().st_mtime > STALE_LOCK_SECONDS:
                    path.unlink()
                    continue
            except OSError:
                pass
            if time.monotonic() >= deadline:
                raise Stop(E_REFUSED, "REFUSED", "busy", f"{path.name} held by another process")
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
    led = ledger_write(root, ticket, ["consult", "--incident", a.incident, "--kind", "advisor", "--note", note])
    return {"ok": True, "disposition": "RECORDED", "receipt": rel(root, rpath), "receipt_sha256": rsha,
            "request_id": receipt.get("request_id"), "ledger_seq": led.get("seq"), "advice": receipt.get("advice"),
            "detail": "evidence=" + kind}


def native_slot(inc):
    return int((inc.get("consults") or {}).get("advisor", 0))


def ppss_slot(st, incident):
    return sum(1 for r in st.get("ppss", []) if r.get("incident") == incident)


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
        note = (f"source=advisor.native via={VIA} dispatch={a.dispatch_id} "
                f"purpose_sha256={digest} ref={ref}")
        led = ledger_write(root, st["ticket"], ["consult", "--incident", a.incident, "--kind", "advisor", "--note", note])
    return {"ok": True, "disposition": "RECORDED", "dispatch_id": a.dispatch_id, "ledger_seq": led.get("seq"),
            "detail": "recorded as a declaration: native tool execution and content are not verifiable here"}


def ticket_of(root, ticket_arg):
    return ledger_status(root, ticket_arg)["ticket"]


def existing_summary(st, incident):
    return [{"request_id": r.get("request_id"), "state": r.get("state"), "urls": r.get("urls", []),
             "reads": len(r.get("reads", []))} for r in st.get("ppss", []) if r.get("incident") == incident]


def do_ppss(a, table_src, root, ticket_arg):
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
        did = dispatch_id(st["ticket"], a.incident, a.source, ppss_slot(st, a.incident), digest_queries(queries))
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
                "return_with": f"{cli_prefix(a.source, a.incident)} --phase record --dispatch-id {did} "
                               f"--artifact <research_submit 回件之 artifact_path>{extra}",
                "detail": "dispatch only: this entry cannot call the MCP tool nor observe its cost"}
    # record
    if not a.artifact:
        raise Stop(E_USAGE, "USAGE", "usage", "--artifact is required for record")
    try:
        art = json.loads(Path(a.artifact).read_text(encoding="utf-8"))
    except (OSError, ValueError) as exc:
        raise Stop(E_REFUSED, "REFUSED", "request_invalid", f"artifact unreadable: {type(exc).__name__}")
    aq = art.get("queries") if isinstance(art, dict) else None
    if not isinstance(aq, list) or not norm_queries([q for q in aq if isinstance(q, str)]):
        raise Stop(E_REFUSED, "REFUSED", "dispatch_mismatch", "artifact has no usable queries field")
    ticket = ticket_of(root, ticket_arg)
    with route_lock(root, ticket, a.lock_wait_ms):
        st, _ = precheck(root, ticket_arg, a.incident)
        verify_dispatch(a, st["ticket"], a.incident, a.source, ppss_slot(st, a.incident),
                        digest_queries([q for q in aq if isinstance(q, str)]))
        args = ["ppss", "record", "--incident", a.incident, "--artifact", a.artifact]
        if a.domains:
            args += ["--domains", a.domains]
        if a.new_query_strategy:
            args += ["--new-query-strategy", a.new_query_strategy]
        if a.reason:
            args += ["--reason", a.reason]
        led = ledger_write(root, st["ticket"], args)
    return {"ok": True, "disposition": "RECORDED", "dispatch_id": a.dispatch_id, "request_id": led.get("request_id"),
            "ledger_seq": led.get("seq"), "ledger": led}


PHASES = {"api_advisor": (None, "call"), "native_advisor": ("dispatch", "return"), "ppss": ("dispatch", "record", "read")}


def cmd_consult(a, client_factory, env, now):
    table = load_table(a.table or TABLE_DEFAULT)
    src = resolve_source(table, a.source)
    adapter = src["adapter"]
    phases = PHASES[adapter]
    if a.phase not in phases:
        raise Stop(E_USAGE, "USAGE", "usage", f"{a.source}: --phase must be one of {[p for p in phases if p]}")
    root = repo_root()
    if adapter == "api_advisor":
        body = do_api(a, root, a.ticket, client_factory, env, now)
    elif adapter == "native_advisor":
        body = do_native(a, src, root, a.ticket)
    else:
        body = do_ppss(a, src, root, a.ticket)
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
    c.add_argument("--incident", required=True)
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
