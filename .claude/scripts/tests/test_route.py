"""Tests for route.py (ADV-ENTRY-01). Standard library unittest only; zero network.

Every behaviour case goes through the real entry: `route.main(argv, client_factory=..., env=..., now=...)` or a
`subprocess` run of the route.py CLI. Internal adapters / check helpers are never called directly, and the
behaviour under test is never produced by hand-writing ledger events (ledger setup uses the ledger CLI only).
Everything happens in a tempfile git repository; the real repository's ledger is never touched. The fake client
mirrors the shape used by test_advisor_call.py (copied, that module is not imported). The fake API key is
injected through `env=` and asserted to appear in no output.
"""
from __future__ import annotations

import contextlib
import hashlib
import io
import json
import os
import shutil
import stat
import subprocess
import sys
import tempfile
import time
import unittest
from datetime import datetime, timezone
from pathlib import Path
from types import SimpleNamespace
from unittest import mock

SCRIPTS = Path(__file__).resolve().parent.parent
ROUTE = SCRIPTS / "route.py"
LEDGER = SCRIPTS / "gate_ledger.py"
TABLE_REAL = SCRIPTS / "route_table.json"
sys.path.insert(0, str(SCRIPTS))
import anthropic  # noqa: E402
import httpx  # noqa: E402
import advisor_call as ac  # noqa: E402
import route  # noqa: E402

PROJECT = ("plans", "cso_briefing", "project")
TICKET = "ADV-TEST-01"
KEY = "test-advisor-key-0123456789"
FIXED = datetime(2026, 10, 20, 9, 30, 0, tzinfo=timezone.utc)
HEADS = ["## 目標", "## 失敗證據", "## 相關內容", "## 已試策略與結果", "## 既有授權", "## 要求輸出"]
GOOD_ADVICE = {"strategy": "套用上限", "steps": ["改 backoff.py"], "minimal_verification": "python check.py",
               "authority_gap": False, "authority_gap_reason": "", "evidence_needed": []}
GOOD_TEXT = json.dumps(GOOD_ADVICE, ensure_ascii=False)


def sha(b):
    return hashlib.sha256(b).hexdigest()


def _rm_readonly(func, path, _exc):
    os.chmod(path, stat.S_IWRITE)
    func(path)


def git(cwd, *args):
    res = subprocess.run(["git", "-c", "user.name=t", "-c", "user.email=t@example.invalid", "-c", "commit.gpgsign=false", *args],
                         cwd=cwd, capture_output=True, text=True, encoding="utf-8", errors="replace")
    if res.returncode != 0:
        raise RuntimeError(f"git {args} failed: {res.stderr}")
    return res.stdout.strip()


def make_resp(text=GOOD_TEXT, stop="end_turn", rid="req_fake_1"):
    usage = SimpleNamespace(input_tokens=1000, output_tokens=500, cache_read_input_tokens=0, cache_creation_input_tokens=0)
    return SimpleNamespace(content=[SimpleNamespace(type="text", text=text)], stop_reason=stop, usage=usage,
                           model="claude-opus-5-5", _request_id=rid, stop_details=None)


class FakeClient:
    def __init__(self, result):
        self.result, self.calls, self.messages = result, [], self

    def create(self, **kw):
        self.calls.append(kw)
        if isinstance(self.result, BaseException):
            raise self.result
        return self.result


class Factory:
    def __init__(self, client):
        self.client, self.called = client, False

    def __call__(self, **kw):
        self.called = True
        return self.client


def payload_text(token, prose="任意問題敘述"):
    parts = []
    for h in HEADS:
        parts += [h, token if h == "## 失敗證據" else prose]
    return "\n".join(parts) + "\n"


class RouteCase(unittest.TestCase):
    def setUp(self):
        self.tmp = Path(tempfile.mkdtemp(prefix="route_test_")).resolve()
        self.addCleanup(shutil.rmtree, self.tmp, onerror=_rm_readonly)
        self.repo = self.tmp / "repo"
        self.repo.mkdir()
        git(self.repo, "init", "-q")
        (self.repo / "README.md").write_text("x", encoding="utf-8")
        (self.repo / ".gitignore").write_text("plans/\ntmp/\n.tmp/\n", encoding="utf-8")
        git(self.repo, "add", "README.md", ".gitignore")
        git(self.repo, "commit", "-q", "-m", "init")
        for sub in ("Pending", "deliver", "void"):
            self.repo.joinpath(*PROJECT, sub).mkdir(parents=True)
        self.ticket_file = self.repo.joinpath(*PROJECT, "Pending", f"{TICKET}_x.md")
        self.ticket_file.write_text("# t\n", encoding="utf-8")
        self.inc = self.open_incident()

    # ---------------- ledger setup (CLI only)
    def ledger(self, *args):
        res = subprocess.run([sys.executable, str(LEDGER), "--ticket", TICKET, *args], cwd=self.repo, capture_output=True)
        return res.returncode, res.stdout.decode("utf-8", "replace"), res.stderr.decode("utf-8", "replace")

    def open_incident(self, subject="test:a", independent=False):
        extra = ["--independent-evidence", "separate failure output"] if independent else []
        rc, out, err = self.ledger("incident", "open", "--subject", subject, *extra)
        self.assertEqual(rc, 0, err)
        return json.loads(out)["incident"]

    def events(self):
        p = self.repo / ".git" / "gate" / f"{TICKET}.jsonl"
        return [json.loads(x) for x in p.read_text(encoding="utf-8").splitlines() if x.strip()] if p.exists() else []

    def consults(self):
        return [e for e in self.events() if e.get("type") == "consult"]

    def n_events(self):
        return len(self.events())

    # ---------------- entry drivers
    def run_main(self, argv, *, factory=None, env=None, now=None, table=None):
        pre = (["--table", str(table)] if table else [])
        out, err = io.StringIO(), io.StringIO()
        with contextlib.chdir(self.repo), contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
            rc = route.main(["--ticket", TICKET, *pre, *argv], client_factory=factory,
                            env={ac.KEY_VAR: KEY} if env is None else env, now=now or (lambda: FIXED))
        self.assertNotIn(KEY, out.getvalue() + err.getvalue())
        return rc, json.loads(out.getvalue()), err.getvalue()

    def cli_cmd(self, argv, table=None):
        pre = (["--table", str(table)] if table else [])
        return [sys.executable, "-I", str(ROUTE), "--ticket", TICKET, *pre, *argv]

    def cli(self, argv, table=None):
        res = subprocess.run(self.cli_cmd(argv, table), cwd=self.repo, capture_output=True)
        out, err = res.stdout.decode("utf-8", "replace"), res.stderr.decode("utf-8", "replace")
        return res.returncode, json.loads(out), err

    # ---------------- fixtures
    def write(self, name, data):
        p = self.tmp / name
        p.write_bytes(data if isinstance(data, bytes) else data.encode("utf-8"))
        return p

    def evidence(self, name="evidence.txt", body="FAILED check_backoff: expected 5 got 9\n"):
        p = self.write(name, body)
        return p, sha(p.read_bytes())

    def api_args(self, payload, evidence, incident=None):
        return ["consult", "--incident", incident or self.inc, "--source", "advisor.api",
                "--payload", str(payload), "--evidence", str(evidence)]

    def good_api(self, incident=None, kind="failure", prose="任意問題敘述", name="p"):
        ev, h = self.evidence(f"{name}_ev.txt", f"record for {name}\n")
        tok = f"incident={incident or self.inc} {kind}_sha256={h}"
        pl = self.write(f"{name}.md", payload_text(tok, prose))
        return pl, ev

    def table_copy(self, mutate=None, raw=None):
        t = json.loads(TABLE_REAL.read_text(encoding="utf-8"))
        if mutate:
            mutate(t)
        p = self.tmp / "table.json"
        p.write_text(raw if raw is not None else json.dumps(t, ensure_ascii=False), encoding="utf-8")
        return p


class SourceStateTests(RouteCase):
    def test_S1_unknown_source(self):
        n = self.n_events()
        rc, o, _ = self.run_main(["consult", "--incident", self.inc, "--source", "advisor.nope", "--phase", "dispatch"])
        self.assertEqual((rc, o["disposition"]), (4, "UNKNOWN_SOURCE"))
        self.assertEqual(self.n_events(), n)

    def test_S2_disabled_with_alternative_and_continuation(self):
        tbl = self.table_copy(lambda t: t["sources"]["advisor.api"].update(enabled=False))
        pl, ev = self.good_api()
        fac = Factory(FakeClient(make_resp()))
        n = self.n_events()
        rc, o, _ = self.run_main(self.api_args(pl, ev), factory=fac, table=tbl)
        self.assertEqual((rc, o["disposition"], o["alternatives"]), (4, "SOURCE_DISABLED", ["advisor.native"]))
        self.assertFalse(fac.called)
        self.assertEqual(self.n_events(), n)
        alt = o["alternatives"][0]                                   # main loop picks the legal alternative and continues
        rc, d, _ = self.run_main(["consult", "--incident", self.inc, "--source", alt, "--phase", "dispatch", "--purpose", "need a second opinion"], table=tbl)
        self.assertEqual((rc, d["disposition"]), (0, "DISPATCHED"))
        rc, r, _ = self.run_main(["consult", "--incident", self.inc, "--source", alt, "--phase", "return", "--dispatch-id", d["dispatch_id"],
                                  "--purpose", "need a second opinion", "--note", "native advisor called"], table=tbl)
        self.assertEqual((rc, r["disposition"]), (0, "RECORDED"))
        self.assertEqual(len(self.consults()), 1)

    def test_S3_all_sources_of_role_disabled(self):
        def mut(t):
            t["sources"]["advisor.api"]["enabled"] = False
            t["sources"]["advisor.native"]["enabled"] = False
        rc, o, _ = self.run_main(["consult", "--incident", self.inc, "--source", "advisor.api", "--phase", "call"], table=self.table_copy(mut))
        self.assertEqual((rc, o["disposition"], o["reason"]), (4, "MISSING_REQUIRED", "required_capability_absent"))
        self.assertEqual(o["alternatives"], [])

    def test_S4_domain_internal_not_callable(self):
        rc, o, _ = self.run_main(["consult", "--incident", self.inc, "--source", "advisor.rigor-repair", "--phase", "dispatch"])
        self.assertEqual((rc, o["disposition"]), (4, "NOT_CALLABLE"))

    def test_S5_broken_tables_are_zero_side_effect(self):
        n = self.n_events()
        for raw in ("{not json", json.dumps({"schema": 2, "sources": {}}), json.dumps({"schema": 1})):
            rc, o, _ = self.run_main(["consult", "--incident", self.inc, "--source", "advisor.native", "--phase", "dispatch", "--purpose", "x"],
                                     table=self.table_copy(raw=raw))
            self.assertEqual((rc, o["disposition"]), (3, "TABLE_INVALID"))
        self.assertEqual(self.n_events(), n)

    def test_S6_real_table_checks(self):
        rc, o, _ = self.run_main(["table", "check"])
        self.assertEqual(rc, 0)
        self.assertTrue(o["ok"])
        self.assertIn("advisor.api", o["enabled"])
        data = json.loads(TABLE_REAL.read_text(encoding="utf-8"))
        self.assertEqual(set(data), {"schema", "sources"})           # no successors / facts in V1


class ApiTests(RouteCase):
    def run_api(self, pl, ev, factory=None, **kw):
        factory = factory or Factory(FakeClient(make_resp()))
        rc, o, err = self.run_main(self.api_args(pl, ev), factory=factory, **kw)
        return rc, o, err, factory

    def test_A1_failure_path_records_once(self):
        pl, ev = self.good_api(kind="failure")
        rc, o, err, fac = self.run_api(pl, ev)
        self.assertEqual((rc, o["disposition"]), (0, "RECORDED"), o)
        self.assertEqual(err, "")
        cs = self.consults()
        self.assertEqual(len(cs), 1)
        note = cs[0]["note"]
        for needle in ("via=route.py", f"receipt_sha256={o['receipt_sha256']}", f"request_id={o['request_id']}",
                       f"payload_sha256={sha(pl.read_bytes())}", f"incident={self.inc}", "evidence=failure:"):
            self.assertIn(needle, note)
        self.assertEqual(len(fac.client.calls), 1)

    def test_A2_uncertainty_path_without_failure_output(self):
        pl, ev = self.good_api(kind="uncertainty")
        self.assertIn("## 失敗證據", pl.read_text(encoding="utf-8"))
        rc, o, _, _ = self.run_api(pl, ev)
        self.assertEqual((rc, o["disposition"]), (0, "RECORDED"), o)
        self.assertIn("evidence=uncertainty:", self.consults()[0]["note"])

    def assert_refused(self, pl, ev, reason, **kw):
        n = self.n_events()
        rc, o, _, fac = self.run_api(pl, ev, **kw)
        self.assertEqual((rc, o["disposition"], o["reason"]), (5, "REFUSED", reason), o)
        self.assertFalse(fac.called, "client must not be built on a pre-call refusal")
        self.assertEqual(self.n_events(), n)

    def test_A3_cross_incident_mismatch(self):
        other = self.open_incident("test:b", independent=True)   # payload (and its evidence) belong to self.inc; target is `other`
        pl, ev = self.good_api(incident=self.inc)
        n = self.n_events()
        fac = Factory(FakeClient(make_resp()))
        rc, o, _ = self.run_main(self.api_args(pl, ev, incident=other), factory=fac)
        self.assertEqual((rc, o["reason"]), (5, "incident_token_mismatch"))
        self.assertFalse(fac.called)
        self.assertEqual(self.n_events(), n)

    def test_A4_evidence_swapped(self):
        pl, _ = self.good_api()
        other_ev, _ = self.evidence("other.txt", "another incident's record\n")
        self.assert_refused(pl, other_ev, "evidence_hash_mismatch")

    def test_A5_empty_hash_with_empty_file(self):
        ev = self.write("empty.txt", b"")
        pl = self.write("p.md", payload_text(f"incident={self.inc} failure_sha256={route.EMPTY_SHA}"))
        self.assert_refused(pl, ev, "evidence_file_empty")

    def test_A6_empty_hash_with_nonempty_file(self):
        ev, _ = self.evidence()
        pl = self.write("p.md", payload_text(f"incident={self.inc} uncertainty_sha256={route.EMPTY_SHA}"))
        self.assert_refused(pl, ev, "evidence_hash_mismatch")

    def test_A7_no_evidence_token(self):
        ev, _ = self.evidence()
        pl = self.write("p.md", payload_text(f"incident={self.inc}"))
        self.assert_refused(pl, ev, "evidence_token_missing")

    def test_A8_both_tokens(self):
        ev, h = self.evidence()
        pl = self.write("p.md", payload_text(f"incident={self.inc} failure_sha256={h} uncertainty_sha256={h}"))
        self.assert_refused(pl, ev, "evidence_token_ambiguous")

    def test_A8b_incident_token_missing_and_prefix_boundary(self):
        ev, h = self.evidence()
        self.assert_refused(self.write("p.md", payload_text(f"failure_sha256={h}")), ev, "incident_token_missing")
        self.assert_refused(self.write("p2.md", payload_text(f"incident={self.inc}1 failure_sha256={h}")), ev, "incident_token_mismatch")

    def test_A9_payload_changed_after_entry_hash(self):
        pl, ev = self.good_api()
        calls = {"n": 0}

        def now():
            calls["n"] += 1
            if calls["n"] == 1:                            # first clock read happens after the entry hashed the payload
                with open(pl, "ab") as fh:
                    fh.write(b" ")
            return FIXED
        n = self.n_events()
        rc, o, _, fac = self.run_api(pl, ev, now=now)
        self.assertEqual((rc, o["reason"]), (5, "receipt_mismatch"))
        self.assertIn("payload_sha256", o["detail"])
        self.assertEqual(self.n_events(), n)

    def test_A10_incident_not_open(self):
        pl, ev = self.good_api(incident="INC-009")
        fac = Factory(FakeClient(make_resp()))
        rc, o, _ = self.run_main(self.api_args(pl, ev, incident="INC-009"), factory=fac)
        self.assertEqual((rc, o["reason"]), (5, "incident_not_open"))
        self.assertFalse(fac.called)
        self.ledger("round", "start", "--incident", self.inc, "--expect", "0", "--strategy", "s", "--authority", "a")
        self.ledger("round", "end", "--incident", self.inc, "--result", "pass")          # closes the incident
        n = self.n_events()
        pl2, ev2 = self.good_api(name="c")
        rc, o, _ = self.run_main(self.api_args(pl2, ev2), factory=Factory(FakeClient(make_resp())))
        self.assertEqual((rc, o["reason"]), (5, "incident_not_open"))
        self.assertEqual(self.n_events(), n)

    def test_A11_ticket_not_mutable(self):
        shutil.move(str(self.ticket_file), str(self.repo.joinpath(*PROJECT, "deliver", self.ticket_file.name)))
        pl, ev = self.good_api()
        self.assert_refused(pl, ev, "ticket_not_mutable")

    def test_A12_api_error_is_not_retried(self):
        err = anthropic.APIConnectionError(request=httpx.Request("POST", "https://api.anthropic.com/v1/messages"))
        fac = Factory(FakeClient(err))
        pl, ev = self.good_api()
        n = self.n_events()
        rc, o, _, _ = self.run_api(pl, ev, factory=fac)
        self.assertEqual((rc, o["disposition"], o["advisor_exit"]), (6, "CALL_FAILED", 4))
        self.assertEqual(len(fac.client.calls), 1)
        self.assertEqual(self.n_events(), n)

    def test_A13_non_success_is_not_recorded(self):
        fac = Factory(FakeClient(make_resp(stop="max_tokens")))
        pl, ev = self.good_api()
        n = self.n_events()
        rc, o, _, _ = self.run_api(pl, ev, factory=fac)
        self.assertEqual((rc, o["disposition"]), (6, "CALL_FAILED"))
        self.assertEqual(self.n_events(), n)

    def test_A14_token_is_the_binding_not_the_prose(self):
        for i, prose in enumerate(("短", "very long " * 200, "混合 mixed 語言 prose")):
            pl, ev = self.good_api(prose=prose, name=f"pp{i}")
            rc, o, _, _ = self.run_api(pl, ev, factory=Factory(FakeClient(make_resp(rid=f"req_fake_{i}"))))
            self.assertEqual((rc, o["disposition"]), (0, "RECORDED"), (i, o))
        self.assertEqual(len(self.consults()), 3)

    def test_A15_caller_limits_are_not_relaxed(self):
        pl, ev = self.good_api()
        rc, o, _, _ = self.run_api(pl, ev)
        self.assertEqual(rc, 0)
        receipt = json.loads((self.repo / o["receipt"]).read_text(encoding="utf-8"))
        self.assertEqual((receipt["max_tokens"], receipt["timeout_s"], receipt["request_mode"], receipt["sdk_max_retries"]),
                         (ac.MAX_TOKENS_DEFAULT, ac.TIMEOUT_DEFAULT, "json_schema", 0))
        self.assertEqual(receipt["ticket"], TICKET)


class NativeTests(RouteCase):
    def dispatch(self, purpose="design question A", table=None):
        rc, o, _ = self.run_main(["consult", "--incident", self.inc, "--source", "advisor.native", "--phase", "dispatch", "--purpose", purpose], table=table)
        self.assertEqual((rc, o["disposition"]), (0, "DISPATCHED"), o)
        return o

    def ret(self, did, purpose="design question A", note="native advisor called, ref t1"):
        argv = ["consult", "--incident", self.inc, "--source", "advisor.native", "--phase", "return", "--dispatch-id", did]
        if purpose is not None:
            argv += ["--purpose", purpose]
        if note is not None:
            argv += ["--note", note]
        return self.run_main(argv)

    def test_N1_dispatch_shape_and_no_events(self):
        n = self.n_events()
        o = self.dispatch()
        self.assertEqual((o["action"]["tool"], o["action"]["params"]), ("advisor", {}))
        self.assertRegex(o["dispatch_id"], r"^d2-[0-9a-f]{16}$")
        self.assertEqual(self.n_events(), n)

    def test_N2_different_dispatches_are_distinguishable(self):
        a, b, a2 = self.dispatch("question A"), self.dispatch("question B"), self.dispatch("question A")
        self.assertNotEqual(a["dispatch_id"], b["dispatch_id"])
        self.assertEqual(a["dispatch_id"], a2["dispatch_id"])

    def test_N3_mismatch_is_refused(self):
        a, b = self.dispatch("question A"), self.dispatch("question B")
        n = self.n_events()
        rc, o, _ = self.ret(a["dispatch_id"], purpose="question B")
        self.assertEqual((rc, o["reason"]), (5, "dispatch_mismatch"))
        self.assertEqual(self.n_events(), n)

    def test_N4_correct_return_records(self):
        a = self.dispatch("question A")
        rc, o, _ = self.ret(a["dispatch_id"], purpose="question A")
        self.assertEqual((rc, o["disposition"]), (0, "RECORDED"))
        note = self.consults()[0]["note"]
        self.assertIn(f"dispatch={a['dispatch_id']}", note)
        self.assertIn("purpose_sha256=", note)
        self.assertIn("via=route.py", note)

    def test_N5_replay_adds_no_event(self):
        a = self.dispatch("question A")
        self.assertEqual(self.ret(a["dispatch_id"], "question A")[0], 0)
        n = self.n_events()
        rc, o, _ = self.ret(a["dispatch_id"], "question A")
        self.assertEqual((rc, o["reason"]), (5, "stale_or_duplicate_dispatch"))
        self.assertEqual(self.n_events(), n)

    def test_N6_slot_consumed_by_another_path(self):
        a = self.dispatch("question A")
        pl, ev = self.good_api()
        self.assertEqual(self.run_main(self.api_args(pl, ev), factory=Factory(FakeClient(make_resp())))[0], 0)
        n = self.n_events()
        rc, o, _ = self.ret(a["dispatch_id"], "question A")
        self.assertEqual((rc, o["reason"]), (5, "stale_or_duplicate_dispatch"))
        self.assertEqual(self.n_events(), n)

    def test_N7_malformed_inputs(self):
        a = self.dispatch("question A")
        n = self.n_events()
        for did, purpose, note in (("nonsense", "question A", "ref"), (a["dispatch_id"], "", "ref"),
                                   (a["dispatch_id"], "question A", ""), (a["dispatch_id"], "question A", None),
                                   (a["dispatch_id"], None, "ref")):
            rc, o, _ = self.ret(did, purpose=purpose, note=note)
            self.assertEqual((rc, o["reason"]), (5, "request_invalid"), (did, purpose, note))
        rc, o, _ = self.run_main(["consult", "--incident", self.inc, "--source", "advisor.native", "--phase", "dispatch", "--purpose", ""])
        self.assertEqual((rc, o["reason"]), (5, "request_invalid"))
        self.assertEqual(self.n_events(), n)

    def test_F1_new_source_row_reuses_adapter(self):
        def mut(t):
            t["sources"]["advisor.native2"] = dict(t["sources"]["advisor.native"])
        before = {p.name: sha(p.read_bytes()) for p in SCRIPTS.glob("*") if p.is_file()}
        tbl = self.table_copy(mut)
        rc, o, _ = self.run_main(["consult", "--incident", self.inc, "--source", "advisor.native2", "--phase", "dispatch", "--purpose", "x"], table=tbl)
        self.assertEqual((rc, o["disposition"]), (0, "DISPATCHED"))
        rc, c, _ = self.run_main(["table", "check"], table=tbl)
        self.assertIn("advisor.native2", c["enabled"])
        self.assertEqual(before, {p.name: sha(p.read_bytes()) for p in SCRIPTS.glob("*") if p.is_file()})

    def test_F3_table_check_detects_defects(self):
        cases = {
            "dup": '{"schema":1,"sources":{"advisor.api":{"kind":"script-call","role":"advisor","enabled":true,"adapter":"api_advisor","ledger_kind":"advisor"},'
                   '"advisor.api":{"kind":"script-call","role":"advisor","enabled":true,"adapter":"api_advisor","ledger_kind":"advisor"}}}',
            "adapter": json.dumps({"schema": 1, "sources": {"advisor.x": {"kind": "script-call", "role": "advisor", "enabled": True, "adapter": "nope", "ledger_kind": "advisor"}}}),
            "kind": json.dumps({"schema": 1, "sources": {"advisor.x": {"kind": "weird", "role": "advisor"}}}),
        }
        for name, raw in cases.items():
            rc, o, _ = self.run_main(["table", "check"], table=self.table_copy(raw=raw))
            self.assertEqual((rc, o["disposition"]), (3, "TABLE_INVALID"), name)


class PpssTests(RouteCase):
    QUERIES = ["Alpha Query", "beta query"]

    def artifact(self, name="art.json", queries=None, results=True, rid="rsch-20261020-aaaaaaaaaa",
                 captured="2026-10-20T09:45:00+00:00"):
        data = {"request_id": rid, "captured_at": captured,
                "queries": [q.casefold() for q in (queries if queries is not None else self.QUERIES)],
                "results": ([{"title": "t", "url": "https://docs.example.com/a", "snippet": "hello world snippet", "date": None,
                              "last_updated": "2026-10-01"}] if results else [])}
        return self.write(name, json.dumps(data))

    def dispatch(self, queries=None, extra=(), question="what is X?"):
        argv = ["consult", "--incident", self.inc, "--source", "research.ppss", "--phase", "dispatch", "--question", question]
        for q in (queries if queries is not None else self.QUERIES):
            argv += ["--query", q]
        return self.run_main(argv + list(extra))

    def bind_args(self, did, art, rid=None, extra=()):
        if rid is None:
            rid = json.loads(Path(art).read_text(encoding="utf-8")).get("request_id", "none") if Path(art).is_file() else "none"
        return ["consult", "--incident", self.inc, "--source", "research.ppss", "--phase", "bind",
                "--dispatch-id", did, "--request-id", rid, "--artifact", str(art), *extra]

    def record(self, did, art, extra=(), bind=True):
        extra = list(extra)
        bind_extra = [x for x in extra if x == "--cached-artifact"]        # the time exemption belongs to bind only
        rec_extra = [x for x in extra if x != "--cached-artifact"]
        if bind:
            rc, o, e = self.run_main(self.bind_args(did, art, extra=bind_extra))
            if rc != 0:
                return rc, o, e
        return self.run_main(["consult", "--incident", self.inc, "--source", "research.ppss", "--phase", "record",
                              "--dispatch-id", did, "--artifact", str(art), *rec_extra])

    def read(self, rid="rsch-20261020-aaaaaaaaaa", supports="none", quote=None, src="hello world snippet"):
        sf = self.write("src.txt", src)
        argv = ["consult", "--incident", self.inc, "--source", "research.ppss", "--phase", "read", "--request-id", rid,
                "--url", "https://docs.example.com/a", "--source-file", str(sf), "--supports", supports]
        if quote:
            argv += ["--quote", quote]
        return self.run_main(argv)

    def test_P1_dispatch_shape_and_id_binds_query_set(self):
        n = self.n_events()
        rc, o, _ = self.dispatch(extra=("--task-type", "t", "--domains", "docs.example.com"))
        self.assertEqual((rc, o["disposition"]), (0, "DISPATCHED"), o)
        p = o["action"]["params"]
        self.assertEqual(o["action"]["tool"], "research_submit")
        self.assertEqual(set(p), {"question", "queries", "task_type", "filters"})
        self.assertEqual(p["filters"], {"domains": ["docs.example.com"]})
        self.assertTrue(1 <= len(p["queries"]) <= 5)
        same_extra = ("--task-type", "t", "--domains", "docs.example.com")
        rc, same, _ = self.dispatch(queries=[" alpha query ", "BETA QUERY", "alpha query"], extra=same_extra)
        self.assertEqual(o["dispatch_id"], same["dispatch_id"])             # same whole request, normalised spelling
        rc, diff, _ = self.dispatch(queries=["different query"], extra=same_extra)
        self.assertNotEqual(o["dispatch_id"], diff["dispatch_id"])
        self.assertEqual(self.n_events(), n)

    def test_P2_unconsumed_results_block_a_paid_dispatch(self):
        rc, d, _ = self.dispatch()
        self.assertEqual(self.record(d["dispatch_id"], self.artifact())[0], 0)
        n = self.n_events()
        rc, o, _ = self.dispatch(queries=["new query"], extra=("--new-query-strategy", "s", "--reason", "r"))
        self.assertEqual((rc, o["reason"]), (5, "existing_results_unconsumed"))
        self.assertNotIn("action", o)
        self.assertEqual(o["existing"][0]["state"], "RETRIEVED")
        self.assertEqual(self.n_events(), n)

    def test_P3_prior_record_requires_strategy_and_reason(self):
        rc, d, _ = self.dispatch()
        self.assertEqual(self.record(d["dispatch_id"], self.artifact())[0], 0)
        self.assertEqual(self.read()[0], 0)                                     # consume (supports none)
        n = self.n_events()
        rc, o, _ = self.dispatch(queries=["new query"])
        self.assertEqual((rc, o["reason"]), (5, "prior_research_requires_strategy"))
        rc, o, _ = self.dispatch(queries=["new query"], extra=("--new-query-strategy", "s"))
        self.assertEqual((rc, o["reason"]), (5, "prior_research_requires_strategy"))
        self.assertEqual(self.n_events(), n)
        rc, o, _ = self.dispatch(queries=["new query"], extra=("--new-query-strategy", "s", "--reason", "r"))
        self.assertEqual((rc, o["disposition"]), (0, "DISPATCHED"))

    def test_P4_request_shape_limits(self):
        n = self.n_events()
        cases = [dict(queries=[]), dict(queries=["a", "b", "c", "d", "e", "f"]), dict(queries=["x"], question=""),
                 dict(queries=["x"], extra=("--domains", "not a host!")), dict(queries=["  ", ""]),
                 dict(queries=["x"], extra=("--justification-json", "[1]"))]
        for kw in cases:
            rc, o, _ = self.dispatch(**kw)
            self.assertEqual((rc, o["reason"]), (5, "request_invalid"), kw)
        self.assertEqual(self.n_events(), n)

    def test_P5_record_matching_artifact(self):
        rc, d, _ = self.dispatch(extra=("--domains", "docs.example.com"))
        rc, o, _ = self.record(d["dispatch_id"], self.artifact(), extra=("--domains", "docs.example.com"))
        self.assertEqual((rc, o["disposition"]), (0, "RECORDED"), o)
        ev = [e for e in self.events() if e["type"] == "ppss_record"]
        self.assertEqual((len(ev), ev[0]["state"]), (1, "RETRIEVED"))

    def test_P6_record_with_other_queries_is_refused(self):
        rc, d, _ = self.dispatch()
        n = self.n_events()
        for art in (self.artifact("a1.json", queries=["something else"]), self.write("a2.json", json.dumps({"request_id": "r", "results": []}))):
            rc, o, _ = self.record(d["dispatch_id"], art)
            self.assertEqual((rc, o["reason"]), (5, "dispatch_mismatch"))
        self.assertEqual(self.n_events(), n)

    def test_P7_second_record_in_window_is_refused_by_ledger(self):
        rc, d, _ = self.dispatch()
        self.assertEqual(self.record(d["dispatch_id"], self.artifact())[0], 0)
        self.assertEqual(self.read()[0], 0)
        rc, d2, _ = self.dispatch(queries=["second gap"], extra=("--new-query-strategy", "s", "--reason", "r"))
        self.assertEqual(rc, 0)
        n = self.n_events()
        rc, o, _ = self.record(d2["dispatch_id"], self.artifact("b.json", queries=["second gap"], rid="rsch-20261020-bbbbbbbbbb"))
        self.assertEqual((rc, o["reason"]), (5, "ledger_refused"))
        self.assertEqual(self.n_events(), n)

    def test_P8_read_passthrough(self):
        rc, d, _ = self.dispatch()
        self.assertEqual(self.record(d["dispatch_id"], self.artifact())[0], 0)
        rc, o, _ = self.read(supports="claim1", quote="hello world")
        self.assertEqual((rc, o["disposition"]), (0, "RECORDED"), o)
        ev = [e for e in self.events() if e["type"] == "ppss_read"]
        self.assertTrue(ev[0]["evidence_ok"])

    def test_P9_invalid_artifact_state_is_reported_not_hidden(self):
        rc, d, _ = self.dispatch()
        rc, o, _ = self.record(d["dispatch_id"], self.artifact(results=False))
        self.assertEqual((rc, o["disposition"]), (0, "RECORDED"))
        self.assertEqual(o["ledger"]["state"], "INVALID")


class SemanticsAndCliTests(RouteCase):
    def test_M1_M2_event_shape_and_no_forbidden_events(self):
        self.ledger("consult", "--incident", self.inc, "--kind", "advisor", "--note", "manual")
        d = NativeTests.dispatch(self)
        NativeTests.ret(self, d["dispatch_id"])
        pl, ev = self.good_api()
        self.assertEqual(self.run_main(self.api_args(pl, ev), factory=Factory(FakeClient(make_resp())))[0], 0)
        cs = self.consults()
        self.assertEqual(len(cs), 3)
        self.assertEqual({tuple(sorted(c)) for c in cs[1:]}, {tuple(sorted(cs[0]))})
        types = {e["type"] for e in self.events()}
        self.assertTrue(types <= {"finding", "consult", "ppss_record", "ppss_read", "blocks", "check", "missed_check"}, types)
        self.assertFalse(types & {"round_start", "round_end", "human", "step_limit", "strategy_scan"})

    def test_M4_cli_success_is_one_json_object_and_clean_stderr(self):
        rc, o, err = self.cli(["table", "check"])
        self.assertEqual((rc, err), (0, ""))
        self.assertTrue(o["ok"])

    def test_C1_C3_cli_basics(self):
        rc, o, err = self.cli(["consult", "--incident", self.inc, "--source", "advisor.native"])
        self.assertEqual(rc, 2)
        self.assertEqual(o["disposition"], "USAGE")
        rc, o, _ = self.cli(["consult", "--incident", self.inc, "--source", "advisor.native", "--phase", "bogus"])
        self.assertEqual((rc, o["disposition"]), (2, "USAGE"))
        rc, o, _ = self.cli(["consult", "--source", "advisor.native"])
        self.assertEqual((rc, o["disposition"]), (2, "USAGE"))

    def test_C2_cli_native_full_flow_with_replay_refusal(self):
        rc, d, _ = self.cli(["consult", "--incident", self.inc, "--source", "advisor.native", "--phase", "dispatch", "--purpose", "q"])
        self.assertEqual(rc, 0)
        args = ["consult", "--incident", self.inc, "--source", "advisor.native", "--phase", "return", "--dispatch-id", d["dispatch_id"],
                "--purpose", "q", "--note", "ref"]
        self.assertEqual(self.cli(args)[0], 0)
        rc, o, _ = self.cli(args)
        self.assertEqual((rc, o["reason"]), (5, "stale_or_duplicate_dispatch"))
        self.assertEqual(len(self.consults()), 1)


class ConcurrencyTests(RouteCase):
    """Real concurrent subprocess CLI returns; no sequential replay is used to infer concurrent dedupe."""

    def lock_path(self):
        return self.repo / ".git" / "gate" / f"{TICKET}.route.lock"

    def spawn(self, cmds):
        starts, procs = [], []
        for cmd in cmds:
            starts.append(time.time())
            procs.append(subprocess.Popen(cmd, cwd=self.repo, stdout=subprocess.PIPE, stderr=subprocess.PIPE))
        results, ends = [], []
        for p in procs:
            out, _ = p.communicate()
            ends.append(time.time())
            results.append((p.returncode, json.loads(out.decode("utf-8", "replace"))))
        self.assertLess(max(starts), min(ends), "all processes must have been started before any finished (overlap evidence)")
        self.overlap = (starts, ends)
        self.results_seen = results
        return results

    def native_return_cmd(self, did, purpose):
        return self.cli_cmd(["consult", "--incident", self.inc, "--source", "advisor.native", "--phase", "return",
                             "--dispatch-id", did, "--purpose", purpose, "--note", "ref"])

    def dispatch_native(self, purpose):
        rc, d, _ = self.cli(["consult", "--incident", self.inc, "--source", "advisor.native", "--phase", "dispatch", "--purpose", purpose])
        self.assertEqual(rc, 0)
        return d["dispatch_id"]

    def test_K1_same_dispatch_concurrent_returns_record_once(self):
        did = self.dispatch_native("concurrent q")
        res = self.spawn([self.native_return_cmd(did, "concurrent q") for _ in range(6)])
        ok = [r for r in res if r[0] == 0]
        bad = [r for r in res if r[0] != 0]
        self.assertEqual(len(ok), 1, res)
        self.assertEqual(len(self.consults()), 1)
        for rc, o in bad:
            self.assertEqual(rc, 5)
            self.assertIn(o["reason"], ("stale_or_duplicate_dispatch", "busy"))
        self.assertFalse(self.lock_path().exists())

    def test_K2_two_different_dispatches_same_slot(self):
        a, b = self.dispatch_native("question A"), self.dispatch_native("question B")
        self.assertNotEqual(a, b)
        res = self.spawn([self.native_return_cmd(a, "question A"), self.native_return_cmd(b, "question B"),
                          self.native_return_cmd(a, "question A"), self.native_return_cmd(b, "question B")])
        self.assertEqual(sum(1 for r in res if r[0] == 0), 1, res)
        self.assertEqual(len(self.consults()), 1)

    def test_K3_ppss_concurrent_records(self):
        qs = ["gap one query", "gap two query"]
        argv = ["consult", "--incident", self.inc, "--source", "research.ppss", "--phase", "dispatch", "--question", "q?"]
        for q in qs:
            argv += ["--query", q]
        rc, d, _ = self.cli(argv)
        self.assertEqual(rc, 0)
        later = datetime.fromtimestamp(time.time() + 600, timezone.utc).isoformat()     # the CLI dispatch used the real clock
        art = self.write("art.json", json.dumps({"request_id": "rsch-20261020-cccccccccc", "queries": qs, "captured_at": later,
                         "results": [{"url": "https://docs.example.com/a", "title": "t", "snippet": "s"}]}))
        rc, b, _ = self.cli(["consult", "--incident", self.inc, "--source", "research.ppss", "--phase", "bind",
                             "--dispatch-id", d["dispatch_id"], "--request-id", "rsch-20261020-cccccccccc", "--artifact", str(art)])
        self.assertEqual((rc, b["disposition"]), (0, "BOUND"), b)
        cmd = self.cli_cmd(["consult", "--incident", self.inc, "--source", "research.ppss", "--phase", "record",
                            "--dispatch-id", d["dispatch_id"], "--artifact", str(art), "--new-query-strategy", "s", "--reason", "r"])
        res = self.spawn([cmd for _ in range(4)])
        self.assertEqual(sum(1 for r in res if r[0] == 0), 1, res)
        self.assertEqual(len([e for e in self.events() if e["type"] == "ppss_record"]), 1)

    def test_K4_lock_semantics_are_deterministic(self):
        did = self.dispatch_native("lock q")
        lock = self.lock_path()
        lock.parent.mkdir(parents=True, exist_ok=True)
        lock.write_text("999:other 0\n", encoding="utf-8")
        n = self.n_events()
        rc, o, _ = self.cli(["consult", "--incident", self.inc, "--source", "advisor.native", "--phase", "return", "--dispatch-id", did,
                             "--purpose", "lock q", "--note", "ref", "--lock-wait-ms", "300"])
        self.assertEqual((rc, o["reason"]), (5, "busy"))
        self.assertEqual(self.n_events(), n)
        self.assertEqual(lock.read_text(encoding="utf-8"), "999:other 0\n")           # someone else's lock is never removed
        old = time.time() - 300
        os.utime(lock, (old, old))                                                    # aged lock: age alone never reclaims it
        rc, o, _ = self.cli(["consult", "--incident", self.inc, "--source", "advisor.native", "--phase", "return", "--dispatch-id", did,
                             "--purpose", "lock q", "--note", "ref", "--lock-wait-ms", "300"])
        self.assertEqual((rc, o["reason"]), (5, "busy"))
        self.assertEqual(self.n_events(), n)
        self.assertEqual(lock.read_text(encoding="utf-8"), "999:other 0\n")
        lock.unlink()                                                                 # operator removes an orphaned lock by hand
        rc, o, _ = self.cli(["consult", "--incident", self.inc, "--source", "advisor.native", "--phase", "return", "--dispatch-id", did,
                             "--purpose", "lock q", "--note", "ref"])
        self.assertEqual((rc, o["disposition"]), (0, "RECORDED"), o)
        self.assertFalse(lock.exists())
        rc, o, _ = self.cli(["consult", "--incident", self.inc, "--source", "advisor.native", "--phase", "return", "--dispatch-id", did,
                             "--purpose", "lock q", "--note", "ref"])
        self.assertEqual((rc, o["reason"]), (5, "stale_or_duplicate_dispatch"))
        self.assertFalse(lock.exists())

    def test_K5_concurrent_contenders_never_reclaim_an_aged_lock(self):
        did = self.dispatch_native("aged lock q")
        lock = self.lock_path()
        lock.parent.mkdir(parents=True, exist_ok=True)
        lock.write_text("999:other 0\n", encoding="utf-8")
        old = time.time() - 300
        os.utime(lock, (old, old))
        n = self.n_events()
        cmd = self.cli_cmd(["consult", "--incident", self.inc, "--source", "advisor.native", "--phase", "return",
                            "--dispatch-id", did, "--purpose", "aged lock q", "--note", "ref", "--lock-wait-ms", "400"])
        res = self.spawn([cmd for _ in range(4)])
        for rc, o in res:
            self.assertEqual((rc, o["reason"]), (5, "busy"), res)
        self.assertEqual(self.n_events(), n)                                          # no contender entered the critical section
        self.assertEqual(lock.read_text(encoding="utf-8"), "999:other 0\n")           # nobody deleted the foreign lock


class ChainTests(RouteCase):
    """ADV-ENTRY-01 Rev C / C.1: Traycer T2 F1-F3 on the dispatch -> bind -> record chain. Real route.main / CLI and
    the public ledger CLI only; atomicity cases use real concurrent processes or a real ledger CLI action at the
    interleaving point. Zero network."""
    QUERIES = ["Alpha Query", "beta query"]
    RA, RB, RC = "rsch-20261020-aaaaaaaaaa", "rsch-20261020-bbbbbbbbbb", "rsch-20261020-cccccccccc"
    STRAT = ("--new-query-strategy", "s2", "--reason", "r2")
    OLD = "2026-10-01T00:00:00+00:00"                      # predates every dispatch of these tests (FIXED = 2026-10-20 09:30)

    def art(self, name="art.json", rid=RA, captured="2026-10-20T09:45:00+00:00", queries=None):
        return PpssTests.artifact(self, name, queries=queries, rid=rid, captured=captured)

    def disp(self, **kw):
        return PpssTests.dispatch(self, **kw)

    def rec(self, did, art, extra=(), bind=True):
        return PpssTests.record(self, did, art, extra, bind)

    def bind(self, did, art, rid=None, extra=(), now=None):
        return self.run_main(PpssTests.bind_args(self, did, art, rid, extra), now=now)

    def read_it(self, rid=None):
        return PpssTests.read(self, rid or self.RA)

    def inc_status(self):
        rc, out, err = self.ledger("status", "--json")
        self.assertEqual(rc, 0, err)
        st = json.loads(out)
        return {i["id"]: i for i in st["incidents"]}[self.inc], st

    def ppss_events(self):
        return [e for e in self.events() if e["type"] == "ppss_record"]

    def desc_file(self, did):
        return self.repo / "tmp" / TICKET / "route" / f"dispatch_{did}.json"

    def cmd(self, phase, did, art, *more):
        return self.cli_cmd(["consult", "--incident", self.inc, "--source", "research.ppss", "--phase", phase,
                             "--dispatch-id", did, "--artifact", str(art), *more])

    # ------------------------------------------------------------- F1
    def test_F1_slot_is_the_monotonic_record_count_and_request_ids_dedupe(self):
        rc, d1, _ = self.disp()
        rc, o, _ = self.rec(d1["dispatch_id"], self.art())
        self.assertEqual((rc, o["disposition"]), (0, "RECORDED"), o)
        inc, _ = self.inc_status()
        self.assertEqual(inc["ppss_records"], 1)
        self.assertEqual(self.read_it()[0], 0)                                          # consume the result
        rc, d2, _ = self.disp(extra=self.STRAT)
        self.assertNotEqual(d1["dispatch_id"], d2["dispatch_id"])                      # the slot advanced
        n = self.n_events()
        for _ in range(2):                                                             # the same response again, even with a new strategy
            rc, o, _ = self.rec(d2["dispatch_id"], self.art(), self.STRAT)
            self.assertEqual((rc, o["reason"]), (5, "artifact_bound_elsewhere"), o)
        rc, o, _ = self.rec(d1["dispatch_id"], self.art())                             # the consumed dispatch cannot be replayed
        self.assertEqual((rc, o["reason"]), (5, "stale_or_duplicate_dispatch"), o)
        self.assertEqual(self.n_events(), n)
        rc, o, _ = self.rec(d2["dispatch_id"], self.art("b.json", rid=self.RB), self.STRAT)   # a genuinely new request id
        self.assertEqual((rc, o["disposition"]), (0, "RECORDED"), o)
        self.assertEqual(len(self.ppss_events()), 2)
        inc, st = self.inc_status()                                                    # rebuilt from the file: still exact
        self.assertEqual((inc["ppss_records"], len(st["ppss"])), (2, 2))
        rc, o, _ = self.rec(d2["dispatch_id"], self.art("b.json", rid=self.RB), self.STRAT)   # and d2 is consumed too
        self.assertEqual((rc, o["reason"]), (5, "stale_or_duplicate_dispatch"), o)
        self.assertEqual(len(self.ppss_events()), 2)

    def test_F1_a_refused_repeat_keeps_the_slot_and_the_dispatch_usable(self):
        rc, d1, _ = self.disp()
        self.assertEqual(self.rec(d1["dispatch_id"], self.art())[0], 0)
        self.assertEqual(self.read_it()[0], 0)
        rc, d2, _ = self.disp(extra=self.STRAT)
        self.assertEqual(self.rec(d2["dispatch_id"], self.art(), self.STRAT)[0], 5)
        inc, st = self.inc_status()
        self.assertEqual((inc["ppss_records"], len(st["ppss"])), (1, 1))
        rc, d2b, _ = self.disp(extra=self.STRAT)
        self.assertEqual(d2["dispatch_id"], d2b["dispatch_id"])

    def test_F1_ledger_duplicate_request_id_is_reported_when_the_binding_layer_is_bypassed(self):
        art = self.art()
        rc, out, err = self.ledger("ppss", "record", "--incident", self.inc, "--artifact", str(art))     # public ledger CLI, no route
        self.assertEqual(rc, 0, err)
        self.assertEqual(self.read_it()[0], 0)
        rc, d, _ = self.disp(extra=self.STRAT)
        self.assertEqual(rc, 0)
        n = self.n_events()
        rc, o, _ = self.rec(d["dispatch_id"], art, self.STRAT)
        self.assertEqual((rc, o["reason"]), (5, "duplicate_request_id"), o)
        self.assertEqual(self.n_events(), n)

    def test_F1_concurrent_records_land_at_most_once(self):
        rc, d1, _ = self.disp()
        self.assertEqual(self.rec(d1["dispatch_id"], self.art())[0], 0)
        self.assertEqual(self.read_it()[0], 0)
        rc, d2, _ = self.disp(extra=self.STRAT)
        new = self.art("new.json", rid=self.RB)
        self.assertEqual(self.bind(d2["dispatch_id"], new)[0], 0)
        res = ConcurrencyTests.spawn(self, [self.cmd("record", d2["dispatch_id"], new, *self.STRAT) for _ in range(4)])
        self.assertEqual(sum(1 for r in res if r[0] == 0), 1, res)                    # exactly one wins
        self.assertEqual(len(self.ppss_events()), 2)
        res = ConcurrencyTests.spawn(self, [self.cmd("bind", d2["dispatch_id"], new, "--request-id", self.RB) for _ in range(4)])
        self.assertEqual({r[0] for r in res}, {5}, res)                               # d2 is consumed: nobody binds or records again
        self.assertEqual(len(self.ppss_events()), 2)

    # ------------------------------------------------------------- F2
    def closing_client(self):
        repo, ledger, inc = self.repo, str(LEDGER), self.inc

        class ClosingClient(FakeClient):
            def create(inner, **kw):
                for args in (["round", "start", "--incident", inc, "--layer", "executor", "--expect", "0",
                              "--authority", "t", "--strategy", "close while the call is in flight"],
                             ["round", "end", "--incident", inc, "--result", "pass"]):
                    r = subprocess.run([sys.executable, ledger, "--ticket", TICKET, *args], cwd=repo, capture_output=True)
                    assert r.returncode == 0, r.stderr
                return super().create(**kw)

        return ClosingClient(make_resp())

    def test_F2_incident_closed_during_the_api_call_keeps_the_receipt_and_records_nothing(self):
        pl, ev = self.good_api()
        client = self.closing_client()
        fac = Factory(client)
        rc, o, _ = self.run_main(self.api_args(pl, ev), factory=fac)
        self.assertEqual((rc, o["disposition"], o["reason"]), (5, "REFUSED", "incident_closed_after_call"), o)
        self.assertIs(o["recorded"], False)
        self.assertIn("cost", o["detail"])
        self.assertTrue(o["advice"])
        self.assertEqual(sha((self.repo / o["receipt"]).read_bytes()), o["receipt_sha256"])    # receipt kept and intact
        self.assertEqual(len(client.calls), 1)                                                # no second API call
        self.assertEqual(len(self.consults()), 0)
        inc, _ = self.inc_status()
        self.assertEqual((inc["status"], inc["consults"]["advisor"]), ("closed", 0))         # closed stays closed
        rc, o2, _ = self.run_main(self.api_args(pl, ev), factory=fac)                        # next time it stops before any API call
        self.assertEqual((rc, o2["reason"]), (5, "incident_not_open"))
        self.assertEqual(len(client.calls), 1)

    def test_F2_native_return_closed_between_the_check_and_the_write(self):
        d = NativeTests.dispatch(self)
        real_precheck, closed = route.precheck, []

        def precheck_then_close(root, ticket_arg, incident):
            out = real_precheck(root, ticket_arg, incident)
            if not closed:                                  # a real ledger action right after the entry's own check
                closed.append(True)
                for args in (["round", "start", "--incident", incident, "--layer", "executor", "--expect", "0",
                              "--authority", "t", "--strategy", "close between check and write"],
                             ["round", "end", "--incident", incident, "--result", "pass"]):
                    self.assertEqual(self.ledger(*args)[0], 0)
            return out

        with mock.patch.object(route, "precheck", precheck_then_close):
            rc, o, _ = NativeTests.ret(self, d["dispatch_id"])
        self.assertEqual((rc, o["reason"]), (5, "incident_not_open"), o)
        self.assertEqual(len(self.consults()), 0)
        inc, _ = self.inc_status()
        self.assertEqual((inc["status"], inc["consults"]["advisor"]), ("closed", 0))

    # ------------------------------------------------------------- F3
    def test_F3_each_request_dimension_changes_the_dispatch_id(self):
        base = self.disp(question="Android observer leak")[1]["dispatch_id"]
        variants = {
            "question": self.disp(question="Android UI color")[1]["dispatch_id"],
            "domains": self.disp(question="Android observer leak", extra=("--domains", "docs.example.com"))[1]["dispatch_id"],
            "task_type": self.disp(question="Android observer leak", extra=("--task-type", "other_type"))[1]["dispatch_id"],
            "justification": self.disp(question="Android observer leak",
                                       extra=("--justification-json", json.dumps({"why": "x"})))[1]["dispatch_id"],
        }
        ids = [base, *variants.values()]
        self.assertEqual(len(set(ids)), len(ids), variants)
        again = self.disp(question="Android observer leak")[1]["dispatch_id"]
        self.assertEqual(again, base)                                                   # same whole request: same id

    def test_F3_missing_tampered_or_mismatching_descriptions_are_refused(self):
        rc, d = self.disp(extra=("--domains", "docs.example.com"))[:2]
        did = d["dispatch_id"]
        desc = self.desc_file(did)
        self.assertTrue(desc.is_file())
        n = self.n_events()
        rc, o, _ = self.rec(did, self.art(), ("--domains", "other.example.org"))              # declared domains differ at record
        self.assertEqual((rc, o["reason"]), (5, "dispatch_mismatch"), o)
        body = json.loads(desc.read_text(encoding="utf-8"))
        body["request"]["question"] = "tampered question"                                       # description edited after dispatch
        desc.write_text(json.dumps(body), encoding="utf-8")
        rc, o, _ = self.rec(did, self.art())
        self.assertEqual((rc, o["reason"]), (5, "dispatch_mismatch"), o)
        desc.unlink()                                                                            # never dispatched through this entry
        rc, o, _ = self.rec(did, self.art())
        self.assertEqual((rc, o["reason"]), (5, "dispatch_mismatch"), o)
        self.assertEqual(self.n_events(), n)

    # ------------------------------------------------------------- C.1: bind the tool response to the dispatch
    def test_B1_the_response_is_bound_to_the_dispatch_and_record_checks_the_association(self):
        rc, d, _ = self.disp()
        did = d["dispatch_id"]
        art = self.art()
        n = self.n_events()
        rc, o, _ = self.rec(did, art, bind=False)                                                # nothing bound yet
        self.assertEqual((rc, o["reason"]), (5, "unbound_dispatch"), o)
        rc, o, _ = self.bind(did, art)
        self.assertEqual((rc, o["disposition"], o["request_id"]), (0, "BOUND", self.RA), o)
        b = json.loads(self.desc_file(did).read_text(encoding="utf-8"))["binding"]
        self.assertEqual((b["request_id"], b["artifact_sha256"], b["artifact_path"]),
                         (self.RA, sha(art.read_bytes()), Path(os.path.abspath(art)).as_posix()))
        self.assertEqual(self.bind(did, art)[0], 0)                                              # idempotent for the same response
        rc, o, _ = self.bind(did, self.art("other.json", rid=self.RB))                           # a different response for the same dispatch
        self.assertEqual((rc, o["reason"]), (5, "dispatch_already_bound"), o)
        moved = self.write("copy.json", art.read_bytes())                                       # same content, another path
        rc, o, _ = self.rec(did, moved, bind=False)
        self.assertEqual((rc, o["reason"]), (5, "binding_mismatch"), o)
        art.write_text(art.read_text(encoding="utf-8") + " ", encoding="utf-8")                  # content changed after binding
        rc, o, _ = self.rec(did, art, bind=False)
        self.assertEqual((rc, o["reason"]), (5, "binding_mismatch"), o)
        self.assertEqual(self.n_events(), n)
        art.write_text(art.read_text(encoding="utf-8").rstrip(" "), encoding="utf-8")            # restored: the association holds again
        rc, o, _ = self.rec(did, art, bind=False)
        self.assertEqual((rc, o["disposition"]), (0, "RECORDED"), o)

    def test_B2_same_queries_different_requests_cannot_swap_responses(self):
        argv = lambda q: ["consult", "--incident", self.inc, "--source", "research.ppss", "--phase", "dispatch",
                          "--question", q, "--query", "Alpha Query", "--query", "beta query"]
        rc, a, _ = self.run_main(argv("Android observer leak"))
        rc, b, _ = self.run_main(argv("Android UI color"))
        self.assertNotEqual(a["dispatch_id"], b["dispatch_id"])                        # distinguishable dispatches, same slot
        art_a, art_b = self.art("a.json", rid=self.RA), self.art("b.json", rid=self.RB)
        self.assertEqual(self.bind(a["dispatch_id"], art_a)[0], 0)                      # A's response is bound to A right away
        n = self.n_events()
        rc, o, _ = self.bind(b["dispatch_id"], art_a)                                   # B cannot claim A's response
        self.assertEqual((rc, o["reason"]), (5, "artifact_bound_elsewhere"), o)
        rc, o, _ = self.rec(b["dispatch_id"], art_a, bind=False)                        # nor record it without a binding of its own
        self.assertEqual((rc, o["reason"]), (5, "unbound_dispatch"), o)
        rc, o, _ = self.bind(b["dispatch_id"], art_a, rid=self.RB)                      # nor relabel it with B's request id
        self.assertEqual((rc, o["reason"]), (5, "request_id_mismatch"), o)
        self.assertEqual(self.n_events(), n)
        self.assertEqual(self.bind(b["dispatch_id"], art_b)[0], 0)                      # B's own response binds fine
        rc, o, _ = self.rec(a["dispatch_id"], art_a, bind=False)
        self.assertEqual((rc, o["disposition"]), (0, "RECORDED"), o)
        rc, o, _ = self.rec(b["dispatch_id"], art_b, bind=False)                        # B lost its slot to A: stale, not silently recorded
        self.assertEqual((rc, o["reason"]), (5, "stale_or_duplicate_dispatch"), o)
        self.assertEqual(len(self.ppss_events()), 1)

    def test_B3_a_response_older_than_the_dispatch_needs_the_declared_cache_flag(self):
        t_a = FIXED
        t_b = datetime(2026, 10, 20, 9, 40, 0, tzinfo=timezone.utc)
        argv = lambda q: ["consult", "--incident", self.inc, "--source", "research.ppss", "--phase", "dispatch",
                          "--question", q, "--query", "Alpha Query", "--query", "beta query"]
        rc, a, _ = self.run_main(argv("Android observer leak"), now=lambda: t_a)
        rc, b, _ = self.run_main(argv("Android UI color"), now=lambda: t_b)
        art_a = self.art("a.json", captured="2026-10-20T09:35:00+00:00")                # research A ran between the two dispatches
        n = self.n_events()
        rc, o, _ = self.bind(b["dispatch_id"], art_a)
        self.assertEqual((rc, o["reason"]), (5, "artifact_predates_dispatch"), o)
        self.assertEqual(self.n_events(), n)
        rc, o, _ = self.rec(a["dispatch_id"], art_a)                                      # it belongs to A
        self.assertEqual((rc, o["disposition"]), (0, "RECORDED"), o)

    def test_B4_the_request_id_of_the_response_must_match_the_artifact(self):
        rc, d, _ = self.disp()
        art = self.art()
        rc, o, _ = self.bind(d["dispatch_id"], art, rid="rsch-20261020-zzzzzzzzzz")
        self.assertEqual((rc, o["reason"]), (5, "request_id_mismatch"), o)
        rc, o, _ = self.bind(d["dispatch_id"], art, rid="rsch-20261020-zzzzzzzzzz", extra=("--cached-artifact",))
        self.assertEqual((rc, o["reason"]), (5, "request_id_mismatch"), o)               # the cache flag does not excuse a fresh artifact

    def test_B5_cached_artifact_exempts_only_the_time_condition(self):
        old = self.art("old.json", captured=self.OLD)
        rc, d, _ = self.disp()
        did = d["dispatch_id"]
        rc, o, _ = self.bind(did, old, rid="rsch-20261020-hit0000001")
        self.assertEqual((rc, o["reason"]), (5, "artifact_predates_dispatch"), o)       # without the flag the time rule holds
        self.assertEqual(PpssTests.dispatch(self, extra=())[0], 0)
        # the flag lets a cache hit (new tool request id, older artifact) through the time rule only
        rc, o, _ = self.bind(did, old, rid="rsch-20261020-hit0000001", extra=("--cached-artifact",))
        self.assertEqual((rc, o["disposition"]), (0, "BOUND"), o)
        self.assertEqual(o["artifact_request_id"], self.RA)
        # ... but never the uniqueness of the response: a second dispatch cannot take the same response
        rc, d2, _ = self.disp(question="a different question")
        rc, o, _ = self.bind(d2["dispatch_id"], old, rid="rsch-20261020-hit0000001", extra=("--cached-artifact",))
        self.assertEqual((rc, o["reason"]), (5, "artifact_bound_elsewhere"), o)
        rc, o, _ = self.bind(d2["dispatch_id"], old, rid="rsch-20261020-hit0000002", extra=("--cached-artifact",))
        self.assertEqual((rc, o["reason"]), (5, "artifact_bound_elsewhere"), o)          # nor the same artifact in the same incident
        # record does not accept the flag
        rc, o, _ = self.run_main(["consult", "--incident", self.inc, "--source", "research.ppss", "--phase", "record",
                                  "--dispatch-id", did, "--artifact", str(old), "--cached-artifact"])
        self.assertEqual(rc, 2)
        # the binding, the slot and the ledger de-duplication still apply: record it once, then nothing repeats
        rc, o, _ = self.rec(did, old, bind=False)
        self.assertEqual((rc, o["disposition"]), (0, "RECORDED"), o)
        n = self.n_events()
        rc, o, _ = self.bind(did, old, rid="rsch-20261020-hit0000001", extra=("--cached-artifact",))   # dispatch consumed: slot moved on
        self.assertEqual((rc, o["reason"]), (5, "stale_or_duplicate_dispatch"), o)
        self.assertEqual(self.n_events(), n)

    def test_B6_cache_flag_does_not_exempt_ledger_deduplication(self):
        old = self.art("old.json", captured=self.OLD)
        rc, out, err = self.ledger("ppss", "record", "--incident", self.inc, "--artifact", str(old))   # this artifact was recorded before
        self.assertEqual(rc, 0, err)
        self.assertEqual(self.read_it()[0], 0)
        rc, d, _ = self.disp(extra=self.STRAT)
        self.assertEqual(self.bind(d["dispatch_id"], old, rid="rsch-20261020-hit0000009", extra=("--cached-artifact",))[0], 0)
        n = self.n_events()
        rc, o, _ = self.run_main(["consult", "--incident", self.inc, "--source", "research.ppss", "--phase", "record",
                                  "--dispatch-id", d["dispatch_id"], "--artifact", str(old), *self.STRAT])
        self.assertEqual((rc, o["reason"]), (5, "duplicate_request_id"), o)
        self.assertEqual(self.n_events(), n)

    def test_B7_a_ledger_without_ppss_records_is_refused_not_approximated(self):
        rc, d, _ = self.disp()
        art = self.art()
        self.assertEqual(self.bind(d["dispatch_id"], art)[0], 0)
        n = self.n_events()
        real = route.ledger_status

        def old_ledger_status(root, ticket_arg):
            st = real(root, ticket_arg)
            for i in st.get("incidents", []):
                i.pop("ppss_records", None)                  # what an older gate_ledger.py reports
            return st

        with mock.patch.object(route, "ledger_status", old_ledger_status):
            for argv in (["consult", "--incident", self.inc, "--source", "research.ppss", "--phase", "dispatch",
                          "--question", "q?", "--query", "x"],
                         self.bind_args(d["dispatch_id"], art),
                         ["consult", "--incident", self.inc, "--source", "research.ppss", "--phase", "record",
                          "--dispatch-id", d["dispatch_id"], "--artifact", str(art)]):
                rc, o, _ = self.run_main(argv)
                self.assertEqual((rc, o["reason"]), (5, "ledger_incompatible"), (argv, o))
        self.assertEqual(self.n_events(), n)
        self.assertEqual(len(self.ppss_events()), 0)

    def bind_args(self, did, art, rid=None, extra=()):
        return PpssTests.bind_args(self, did, art, rid, extra)


if __name__ == "__main__":
    unittest.main()
