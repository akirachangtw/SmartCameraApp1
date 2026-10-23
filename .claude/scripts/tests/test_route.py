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
        self.assertEqual((rc, o["disposition"], o["alternatives"]), (4, "SOURCE_DISABLED", ["advisor.native", "advisor.claude-cli"]))
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
            t["sources"]["advisor.claude-cli"]["enabled"] = False
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

    def test_A12b_http_5xx_is_not_retried_and_not_recorded(self):
        req = httpx.Request("POST", "https://api.anthropic.com/v1/messages")
        err = anthropic.InternalServerError("upstream failure", response=httpx.Response(500, request=req), body=None)
        fac = Factory(FakeClient(err))
        pl, ev = self.good_api()
        n = self.n_events()
        rc, o, _, _ = self.run_api(pl, ev, factory=fac)
        self.assertEqual((rc, o["disposition"]), (6, "CALL_FAILED"), o)
        self.assertEqual(len(fac.client.calls), 1)                                       # one network attempt, no retry
        self.assertEqual(self.n_events(), n)
        self.assertEqual(len(self.consults()), 0)

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
        rc, r, _ = self.run_main(["consult", "--incident", self.inc, "--source", "advisor.native2", "--phase", "return",     # the new row also returns
                                  "--dispatch-id", o["dispatch_id"], "--purpose", "x", "--note", "ref"], table=tbl)
        self.assertEqual((rc, r["disposition"]), (0, "RECORDED"), r)
        self.assertEqual(len(self.consults()), 1)
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


# ---------------------------------------------------------------- ADV-CLAUDECLI-01: advisor.claude-cli
R_MODEL = "claude-opus-5-5"
SID = "7cf532d0-8802-4348-a4a1-b8305c2df91b"
ABSENT = object()
CLI_STUB = r'''
import json, os, subprocess, sys, time
sys.stdout.reconfigure(encoding="utf-8")
d = os.environ["STUB_DIR"]
sc = json.loads(os.environ["STUB_SCENARIO"])
data = sys.stdin.buffer.read().decode("utf-8")
with open(os.path.join(d, "launches.txt"), "a") as f:
    f.write("x\n")
with open(os.path.join(d, "seen.jsonl"), "a", encoding="utf-8") as f:
    f.write(json.dumps({"argv": sys.argv[1:], "env_keys": sorted(k.upper() for k in os.environ), "cwd": os.getcwd(),
                        "stdin": data, "pid": os.getpid(), "echo_key": os.environ.get("ANTHROPIC_API_KEY")}) + "\n")
if sc.get("close_incident"):
    L = [sys.executable, os.environ["STUB_LEDGER"], "--ticket", os.environ["STUB_TICKET"]]
    inc = os.environ["STUB_INCIDENT"]
    subprocess.run(L + ["round", "start", "--incident", inc, "--expect", "0", "--strategy", "s", "--authority", "a"],
                   cwd=os.environ["STUB_REPO"], capture_output=True)
    subprocess.run(L + ["round", "end", "--incident", inc, "--result", "pass"], cwd=os.environ["STUB_REPO"], capture_output=True)
if sc.get("grandchild"):
    subprocess.Popen([sys.executable, "-c", "import os,sys,time;open(sys.argv[1],'w').write(str(os.getpid()));time.sleep(600)",
                      os.path.join(d, "grandchild.pid")])
    with open(os.path.join(d, "stub.pid"), "w") as f:
        f.write(str(os.getpid()))
if sc.get("raw_stdout") is not None:
    sys.stdout.write(sc["raw_stdout"])
for e in sc.get("events", []):
    sys.stdout.write(json.dumps(e, ensure_ascii=False) + "\n")
    sys.stdout.flush()
if sc.get("sleep"):
    time.sleep(sc["sleep"])
sys.exit(sc.get("rc", 0))
'''


def cli_ok(answer="先在 backoff.py 加上限，再重跑 check.py", key="none", init_model=R_MODEL, used=ABSENT, hooks=((0, ""), (0, ""))):
    """Event sequence with the field names of the real `claude -p --output-format stream-json --verbose` output
    (2.1.304; evidence classes C3/C5/C6 of the ticket). Built from the field description, not a recorded stream."""
    evs = []
    for i, (code, out) in enumerate(hooks):
        base = {"hook_id": f"h{i}", "hook_name": "SessionStart:startup", "hook_event": "SessionStart", "session_id": SID}
        evs.append({"type": "system", "subtype": "hook_started", **base})
        evs.append({"type": "system", "subtype": "hook_response", **base, "output": out, "stdout": out, "stderr": "",
                    "exit_code": code, "outcome": "success" if code == 0 else "error"})
    init = {"type": "system", "subtype": "init", "session_id": SID, "tools": [], "mcp_servers": [], "model": init_model,
            "claude_code_version": "2.1.304"}
    if key is not ABSENT:
        init["apiKeySource"] = key
    evs.append(init)
    evs.append({"type": "assistant", "message": {"role": "assistant", "content": [{"type": "text", "text": str(answer)}]},
                "session_id": SID})
    tokens = {"inputTokens": 2, "outputTokens": 783, "cacheReadInputTokens": 1559, "cacheCreationInputTokens": 4281,
              "costUSD": 0.0502}
    evs.append({"type": "result", "subtype": "success", "is_error": False, "result": answer, "session_id": SID,
                "total_cost_usd": 0.0502, "usage": {"input_tokens": 2, "output_tokens": 783},
                "modelUsage": {R_MODEL: tokens} if used is ABSENT else used, "terminal_reason": "completed",
                "duration_ms": 13654, "api_error_status": None})
    return evs


def cli_unauth():
    return [{"type": "system", "subtype": "init", "session_id": SID, "tools": [], "model": R_MODEL, "apiKeySource": "none"},
            {"type": "assistant", "error": "authentication_failed", "session_id": SID,
             "message": {"role": "assistant", "content": [{"type": "text", "text": "Not logged in · Please run /login"}]}},
            {"type": "result", "subtype": "success", "is_error": True, "result": "Not logged in · Please run /login",
             "session_id": SID, "total_cost_usd": 0, "usage": {"input_tokens": 0, "output_tokens": 0}, "modelUsage": {},
             "terminal_reason": "api_error"}]


def pid_alive(pid):
    if os.name == "nt":
        out = subprocess.run(["tasklist", "/FI", f"PID eq {pid}", "/NH"], capture_output=True).stdout.decode("utf-8", "replace")
        return str(pid) in out.split()
    try:
        os.kill(pid, 0)
    except ProcessLookupError:
        return False
    except PermissionError:
        return True
    return True


class ClaudeCliTests(RouteCase):
    SRC = "advisor.claude-cli"

    def setUp(self):
        super().setUp()
        self.stub_dir = self.tmp / "stub"
        self.stub_dir.mkdir()
        self.stub = self.tmp / "stub_claude.py"
        self.stub.write_text(CLI_STUB, encoding="utf-8")

    # ---------------- drivers
    def cli_table(self, mutate=None, timeout_s=None):
        def mut(t):
            c = t["sources"][self.SRC]["cli"]
            c["bin"], c["prefix_args"] = sys.executable, [str(self.stub)]
            if timeout_s:
                c["timeout_s"] = timeout_s
            if mutate:
                mutate(t)
        return self.table_copy(mut)

    def stub_env(self, scenario, extra=None):
        env = dict(os.environ)
        env.update({"STUB_DIR": str(self.stub_dir), "STUB_SCENARIO": json.dumps(scenario, ensure_ascii=False),
                    "STUB_LEDGER": str(LEDGER), "STUB_REPO": str(self.repo), "STUB_TICKET": TICKET, "STUB_INCIDENT": self.inc})
        env.update(extra or {})
        return env

    def launches(self):
        p = self.stub_dir / "launches.txt"
        return len(p.read_text(encoding="utf-8").split()) if p.exists() else 0

    def seen(self):
        p = self.stub_dir / "seen.jsonl"
        return [json.loads(x) for x in p.read_text(encoding="utf-8").splitlines() if x.strip()] if p.exists() else []

    def ticketed(self, scenario, *, pl=None, ev=None, table=None, env=None, incident=None, name="p"):
        if pl is None:
            pl, ev = self.good_api(name=name)
        argv = ["consult", "--incident", incident or self.inc, "--source", self.SRC, "--payload", str(pl), "--evidence", str(ev)]
        return self.run_main(argv, env=env if env is not None else self.stub_env(scenario), table=table or self.cli_table())

    def drive(self, argv, *, ticket=None, cwd=None, env=None, table=None):
        pre = (["--ticket", ticket] if ticket else []) + (["--table", str(table)] if table else [])
        out, err = io.StringIO(), io.StringIO()
        with contextlib.chdir(cwd or self.repo), contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
            rc = route.main([*pre, *argv], env=env if env is not None else dict(os.environ), now=lambda: FIXED)
        return rc, json.loads(out.getvalue()), err.getvalue()

    def ticketless(self, scenario, *, cwd=None, payload=None, extra=(), ticket=None, table=None):
        pl = payload or self.write("tl.md", payload_text("no tokens needed", "無票問題"))
        return self.drive(["consult", "--no-ticket", "--source", self.SRC, "--payload", str(pl), *extra], ticket=ticket,
                          cwd=cwd, env=self.stub_env(scenario), table=table or self.cli_table())

    def fresh_repo(self):
        r = self.tmp / "repo2"
        r.mkdir()
        git(r, "init", "-q")
        (r / "README.md").write_text("x", encoding="utf-8")
        git(r, "add", "README.md")
        git(r, "commit", "-q", "-m", "init")
        return r

    def records(self, repo=None, ticketless=False):
        d = (repo or self.repo) / "tmp" / ("_advisor_cli_ticketless" if ticketless else f"{TICKET}/advisor_cli")
        return sorted(d.glob("advisor_cli_*.json")) if d.exists() else []

    # ---------------- table (AC 1)
    def test_K1_real_table_has_the_source(self):
        rc, o, _ = self.run_main(["table", "check"])
        self.assertEqual(rc, 0)
        self.assertIn(self.SRC, o["callable"])
        self.assertIn(self.SRC, o["enabled"])
        c = json.loads(TABLE_REAL.read_text(encoding="utf-8"))["sources"][self.SRC]
        self.assertEqual((c["adapter"], c["ledger_kind"], c["cli"]["bin"], c["cli"]["prefix_args"], c["cli"]["model"],
                          c["cli"]["effort"]), ("claude_cli", "advisor", "claude", [], R_MODEL, "high"))

    def test_K2_cli_block_is_validated(self):
        muts = {"missing cli": lambda s: s.pop("cli"), "cli not object": lambda s: s.update(cli=[]),
                "empty model": lambda s: s["cli"].update(model=""), "bad effort": lambda s: s["cli"].update(effort="ultra"),
                "timeout low": lambda s: s["cli"].update(timeout_s=29), "timeout high": lambda s: s["cli"].update(timeout_s=601),
                "timeout str": lambda s: s["cli"].update(timeout_s="180"), "timeout bool": lambda s: s["cli"].update(timeout_s=True),
                "empty bin": lambda s: s["cli"].update(bin=""), "prefix not list": lambda s: s["cli"].update(prefix_args="x"),
                "prefix not str": lambda s: s["cli"].update(prefix_args=[1]),
                "wrong ledger_kind": lambda s: s.update(ledger_kind="ppss")}
        for label, m in muts.items():
            rc, o, _ = self.run_main(["table", "check"], table=self.table_copy(lambda t, m=m: m(t["sources"][self.SRC])))
            self.assertEqual((rc, o["disposition"]), (3, "TABLE_INVALID"), (label, o))
            self.assertIn(self.SRC, o["detail"], label)

    # ---------------- success (AC 2, 3)
    def test_K3_ticketed_success_records_once(self):
        pl, ev = self.good_api()
        rc, o, err = self.ticketed({"events": cli_ok()}, pl=pl, ev=ev)
        self.assertEqual((rc, o["disposition"]), (0, "RECORDED"), o)
        self.assertEqual((o["mode"], o["channel"], o["model"], o["effort_flag"], o["api_key_source"], o["session_id"]),
                         ("ticketed", "claude-subscription-cli", R_MODEL, "high", "none", SID))
        self.assertEqual(o["answer"], cli_ok()[-1]["result"])
        self.assertEqual(o["models_used"][R_MODEL]["outputTokens"], 783)
        self.assertEqual((o["cli_internal_retries"], o["hooks"][0]["event"], o["warnings"]), (0, "SessionStart", []))
        self.assertIn("cost_reported_usd", o)
        self.assertEqual(self.launches(), 1)
        cs = self.consults()
        self.assertEqual(len(cs), 1)
        note = cs[0]["note"]
        rec = self.records()
        self.assertEqual(len(rec), 1)
        for needle in ("via=route.py", f"source={self.SRC}", f"model={R_MODEL}", "effort_flag=high", "api_key_source=none",
                       f"session_id={SID}", f"payload_sha256={sha(pl.read_bytes())}", f"incident={self.inc}", "evidence=failure:",
                       "channel=claude-subscription-cli", f"record={o['record']}"):
            self.assertIn(needle, note)
        self.assertEqual(o["record"], f"tmp/{TICKET}/advisor_cli/{rec[0].name}")
        doc = json.loads(rec[0].read_text(encoding="utf-8"))
        self.assertEqual((doc["verdict"], doc["payload_sha256"]), ("SUCCESS", sha(pl.read_bytes())))

    def test_K4_api_key_source_null_is_noted_as_null(self):
        rc, o, _ = self.ticketed({"events": cli_ok(key=None)})
        self.assertEqual((rc, o["disposition"], o["api_key_source"]), (0, "RECORDED", "<null>"), o)
        self.assertIn("api_key_source=<null>", self.consults()[0]["note"])
        self.assertNotIn("api_key_source=none", self.consults()[0]["note"])

    def test_K5_ticketless_success_touches_no_ledger(self):
        r2 = self.fresh_repo()
        gate = r2 / ".git" / "gate"
        self.assertFalse(gate.exists())
        rc, o, _ = self.ticketless({"events": cli_ok()}, cwd=r2)
        self.assertEqual((rc, o["disposition"], o["mode"]), (0, "ANSWERED", "ticketless"), o)
        self.assertEqual(o["answer"], cli_ok()[-1]["result"])
        self.assertFalse(gate.exists() and any(gate.iterdir()))
        self.assertEqual(len(self.records(r2, ticketless=True)), 1)
        self.assertEqual(self.launches(), 1)
        n = self.n_events()                                           # the main repo has a ledger: still untouched
        rc, o, _ = self.ticketless({"events": cli_ok()})
        self.assertEqual((rc, o["disposition"]), (0, "ANSWERED"))
        self.assertEqual(self.n_events(), n)

    def test_K6_ticketless_misuse_and_no_downgrade(self):
        pl = self.write("tl.md", payload_text("x", "q"))
        ev, _h = self.evidence("e.txt")
        n = self.n_events()
        base = ["consult", "--no-ticket", "--source", self.SRC, "--payload", str(pl)]
        for extra, ticket in ((["--incident", self.inc], None), (["--evidence", str(ev)], None), ([], TICKET)):
            rc, o, _ = self.drive(base + extra, ticket=ticket, env=self.stub_env({"events": cli_ok()}), table=self.cli_table())
            self.assertEqual((rc, o["reason"]), (2, "usage"), (extra, ticket, o))
        rc, o, _ = self.drive(["consult", "--no-ticket", "--source", self.SRC], env=self.stub_env({}), table=self.cli_table())
        self.assertEqual((rc, o["reason"]), (2, "usage"))
        nogit = self.tmp / "nogit"
        nogit.mkdir()
        rc, o, _ = self.drive(base, cwd=nogit, env=self.stub_env({"events": cli_ok()}), table=self.cli_table())
        self.assertEqual((rc, o["reason"]), (2, "not_a_git_repo"), o)
        pl2, ev2 = self.good_api(name="nd")                          # no --no-ticket + unresolvable ticket: never downgraded
        r2 = self.fresh_repo()
        rc, o, _ = self.drive(["consult", "--incident", self.inc, "--source", self.SRC, "--payload", str(pl2), "--evidence", str(ev2)],
                              ticket="NOPE-01", cwd=r2, env=self.stub_env({"events": cli_ok()}), table=self.cli_table())
        self.assertEqual((rc, o["disposition"], o["reason"]), (5, "REFUSED", "ledger_refused"), o)
        for argv in (["consult", "--source", "advisor.native", "--phase", "dispatch", "--purpose", "x"],
                     ["consult", "--no-ticket", "--source", "advisor.native", "--phase", "dispatch", "--purpose", "x"]):
            rc, o, _ = self.run_main(argv)
            self.assertEqual((rc, o["reason"]), (2, "usage"), o)
        self.assertEqual(self.launches(), 0)
        self.assertEqual(self.n_events(), n)

    # ---------------- zero-launch refusals (AC 4)
    def test_K7_pre_launch_refusals_never_start_the_cli(self):
        env, tbl = self.stub_env({"events": cli_ok()}), self.cli_table()
        pl, ev = self.good_api()
        src = ["--source", self.SRC]

        def run(argv, table=tbl):
            return self.run_main(["consult", *argv], env=env, table=table)
        full = [*src, "--incident", self.inc, "--payload", str(pl), "--evidence", str(ev)]
        other = self.write("other.md", payload_text("incident=INC-077 failure_sha256=" + sha(ev.read_bytes())))
        none = self.write("none.md", payload_text("no token here"))
        bad = self.write("bad.md", payload_text(f"incident={self.inc} failure_sha256={'0' * 64}"))
        nonutf = self.write("nonutf.md", b"\xff\xfe\xfa not utf8")
        big = self.write("big.md", b"a" * 200001)
        nobin = self.cli_table(lambda t: t["sources"][self.SRC]["cli"].update(bin="no-such-binary-xyz-123", prefix_args=[]))
        n = self.n_events()
        cases = [
            (run([*src, "--incident", self.inc, "--evidence", str(ev)]), 2, "usage"),
            (run([*src, "--payload", str(pl), "--evidence", str(ev)]), 2, "usage"),
            (run([*src, "--incident", self.inc, "--payload", str(pl)]), 2, "usage"),
            (run([*src, "--incident", "INC-099", "--payload", str(pl), "--evidence", str(ev)]), 5, "incident_not_open"),
            (run([*src, "--incident", self.inc, "--payload", str(other), "--evidence", str(ev)]), 5, "incident_token_mismatch"),
            (run([*src, "--incident", self.inc, "--payload", str(none), "--evidence", str(ev)]), 5, "incident_token_missing"),
            (run([*src, "--incident", self.inc, "--payload", str(bad), "--evidence", str(ev)]), 5, "evidence_hash_mismatch"),
            (run([*src, "--incident", self.inc, "--payload", str(self.tmp / "nope.md"), "--evidence", str(ev)]), 5, "request_invalid"),
            (run([*src, "--incident", self.inc, "--payload", str(nonutf), "--evidence", str(ev)]), 5, "request_invalid"),
            (run([*src, "--incident", self.inc, "--payload", str(big), "--evidence", str(ev)]), 5, "request_invalid"),
            (run(full, table=nobin), 6, "cli_unavailable"),
        ]
        for (rc, o, _), want_rc, reason in cases:
            self.assertEqual((rc, o["reason"]), (want_rc, reason), o)
        moved = self.repo.joinpath(*PROJECT, "deliver", self.ticket_file.name)
        shutil.move(str(self.ticket_file), str(moved))
        rc, o, _ = run(full)
        self.assertEqual((rc, o["reason"]), (5, "ticket_not_mutable"), o)
        shutil.move(str(moved), str(self.ticket_file))
        self.assertEqual(self.n_events(), n)
        self.ledger("round", "start", "--incident", self.inc, "--expect", "0", "--strategy", "s", "--authority", "a")
        self.ledger("round", "end", "--incident", self.inc, "--result", "pass")
        n = self.n_events()
        rc, o, _ = run(full)
        self.assertEqual((rc, o["reason"]), (5, "incident_not_open"), o)
        self.assertEqual(self.launches(), 0)
        self.assertEqual(self.n_events(), n)
        self.assertEqual(self.records(), [])

    # ---------------- post-launch failures (AC 5, 6, 7)
    def assert_failed_once(self, scenario, rc_want, reason, *, detail=None, table=None):
        n, l0, r0, c0 = self.n_events(), self.launches(), len(self.records()), len(self.consults())
        rc, o, _ = self.ticketed(scenario, table=table, name=f"f{l0}")
        self.assertEqual((rc, o["reason"]), (rc_want, reason), o)
        if detail:
            self.assertIn(detail, o.get("detail", ""), o)
        self.assertEqual(self.launches(), l0 + 1, "exactly one launch, no restart")
        self.assertEqual(self.n_events(), n)
        self.assertEqual(len(self.consults()), c0)
        self.assertEqual(len(self.records()), r0 + 1)
        return o

    def test_K8_post_launch_failures_are_classified_and_never_recorded(self):
        def res(**kw):
            e = cli_ok()
            e[-1].update(kw)
            return e

        def drop(key):
            e = cli_ok()
            e[-1].pop(key)
            return e
        err_model = cli_unauth()
        err_model[1]["error"] = "model_not_found"
        err_rate = cli_unauth()
        err_rate[1]["error"] = "rate_limit"
        o = self.assert_failed_once({"events": cli_unauth(), "rc": 1}, 6, "not_logged_in", detail="authentication_failed")
        self.assertEqual(o["models_used"], {})                                   # C5 shape: empty modelUsage
        self.assert_failed_once({"events": err_model, "rc": 1}, 6, "model_unavailable")
        self.assert_failed_once({"events": err_rate, "rc": 1}, 6, "cli_error", detail="rate_limit")
        self.assert_failed_once({"events": [cli_ok()[-1]]}, 6, "init_missing")
        self.assert_failed_once({"events": cli_ok()[:-1], "rc": 1}, 6, "nonzero_exit")
        self.assert_failed_once({"events": cli_ok()[:-1]}, 6, "unparseable")
        self.assert_failed_once({"raw_stdout": "garbage, not json\n"}, 6, "unparseable")
        self.assert_failed_once({"events": cli_ok(), "rc": 3}, 6, "nonzero_exit")
        for label, ev in (("missing", drop("is_error")), ("null", res(is_error=None)), ("string", res(is_error="false")),
                          ("zero", res(is_error=0))):
            self.assert_failed_once({"events": ev}, 6, "unparseable", detail="is_error_invalid")
        self.assert_failed_once({"events": res(modelUsage=[])}, 6, "unparseable", detail="model_usage_invalid")

    def test_K9_api_key_source_three_states(self):
        for i, (key, shown) in enumerate(((None, "<null>"), ("none", "none"))):
            rc, o, _ = self.ticketed({"events": cli_ok(key=key)}, name=f"ok{i}")
            self.assertEqual((rc, o["disposition"], o["api_key_source"]), (0, "RECORDED", shown), o)
        o = self.assert_failed_once({"events": cli_ok(key=ABSENT)}, 5, "auth_unreported")
        self.assertEqual(o["api_key_source"], "<absent>")
        for key in ("user", "project", "ANTHROPIC_API_KEY", "", 5, {"a": 1}):
            o = self.assert_failed_once({"events": cli_ok(key=key)}, 5, "api_key_source")
            self.assertEqual(o["api_key_source"], key if isinstance(key, str) else json.dumps(key))
        o = self.assert_failed_once({"events": cli_ok(key="sk-ant-abc123secret")}, 5, "api_key_source")
        self.assertNotIn("abc123secret", json.dumps(o))
        self.assertIn("sk-ant-***", o["detail"])

    def test_K10_model_judgement(self):
        t = {"inputTokens": 1, "outputTokens": 2, "cacheReadInputTokens": 0, "cacheCreationInputTokens": 0}
        rc, o, _ = self.ticketed({"events": cli_ok(used={R_MODEL: t})}, name="okm")
        self.assertEqual((rc, o["disposition"], o["model"]), (0, "RECORDED", R_MODEL))
        o = self.assert_failed_once({"events": cli_ok(used={R_MODEL: t, "claude-haiku-5-5": t})}, 5, "multiple_models")
        self.assertEqual(set(o["models_used"]), {R_MODEL, "claude-haiku-5-5"})
        self.assert_failed_once({"events": cli_ok(used={"claude-haiku-5-5": t})}, 5, "model_mismatch")
        self.assert_failed_once({"events": cli_ok(used={})}, 5, "model_unreported")
        gone = cli_ok()
        gone[-1].pop("modelUsage")
        self.assert_failed_once({"events": gone}, 5, "model_unreported")
        self.assert_failed_once({"events": cli_ok(init_model="claude-haiku-5-5")}, 5, "model_mismatch")

    # ---------------- timeout (AC 8)
    def test_K11_timeout_kills_the_tree_and_leftovers_are_diagnostic_only(self):
        init = [e for e in cli_ok() if e.get("subtype") == "init"]
        retry = [{"type": "system", "subtype": "api_retry", "attempt": i, "max_retries": 10, "retry_delay_ms": 500,
                  "error_status": None, "error": "unknown", "session_id": SID} for i in (1, 2)]
        n = self.n_events()
        rc, o, _ = self.ticketed({"events": init + retry, "grandchild": True, "sleep": 120}, table=self.cli_table(timeout_s=30))
        self.assertEqual((rc, o["disposition"], o["reason"]), (6, "CALL_FAILED", "timeout"), o)
        self.assertEqual((o["reaped"], o["diagnostic"], o["cli_internal_retries"]), (True, True, 2))
        self.assertNotIn("answer", o)
        self.assertEqual((self.launches(), len(self.consults()), self.n_events()), (1, 0, n))
        for name in ("stub.pid", "grandchild.pid"):
            pid_file = self.stub_dir / name
            self.assertTrue(pid_file.exists(), name)
            self.assertFalse(pid_alive(int(pid_file.read_text())), f"{name} still running")

    def test_K12_timeout_is_final_even_after_a_complete_success_stream(self):
        n = self.n_events()
        rc, o, _ = self.ticketed({"events": cli_ok(), "sleep": 120}, table=self.cli_table(timeout_s=30))
        self.assertEqual((rc, o["reason"], o["reaped"]), (6, "timeout", True), o)
        self.assertNotIn("answer", o)
        self.assertEqual((o["session_id"], o["diagnostic"]), (SID, True))
        self.assertEqual((self.launches(), len(self.consults()), self.n_events()), (1, 0, n))
        doc = json.loads(self.records()[0].read_text(encoding="utf-8"))
        self.assertEqual(doc["verdict"]["reason"], "timeout")
        self.assertIsNone(doc["answer"])

    # ---------------- empty answer / invalid answer / incident closed during the call (AC 9)
    def test_K13_blank_answers_and_invalid_answers(self):
        for ans in ("", "   \n\t"):
            o = self.assert_failed_once({"events": cli_ok(answer=ans)}, 5, "empty_answer")
            self.assertEqual(o["session_id"], SID)
            self.assertIn("usage", o)
            self.assertIn("cost_reported_usd", o)
        gone = cli_ok()
        gone[-1].pop("result")
        for ev in (cli_ok(answer=None), cli_ok(answer=5), gone):
            self.assert_failed_once({"events": ev}, 6, "unparseable", detail="answer_invalid")

    def test_K14_incident_closed_during_the_call(self):
        rc, o, _ = self.ticketed({"events": cli_ok(), "close_incident": True})
        self.assertEqual((rc, o["disposition"], o["reason"]), (5, "REFUSED", "incident_closed_after_call"), o)
        self.assertEqual((o["recorded"], o["session_id"], o["answer"]), (False, SID, cli_ok()[-1]["result"]))
        self.assertTrue((self.repo / o["record"]).exists())
        self.assertEqual((self.launches(), len(self.consults())), (1, 0))
        rc, out, _ = self.ledger("status", "--json")
        inc = next(i for i in json.loads(out)["incidents"] if i["id"] == self.inc)
        self.assertNotEqual(inc["status"], "open")                        # not reopened

    # ---------------- hooks / api_retry / warnings (AC 10)
    def test_K15_hooks_and_retries_are_reported_without_changing_success(self):
        evs = cli_ok(hooks=((0, ""), (2, "boom"), (0, "noise")))
        retry = {"type": "system", "subtype": "api_retry", "attempt": 1, "max_retries": 10, "retry_delay_ms": 500,
                 "error_status": None, "error": "unknown", "session_id": SID}
        evs.insert(len(evs) - 1, retry)
        evs.insert(len(evs) - 1, dict(retry, attempt=2))
        rc, o, _ = self.ticketed({"events": evs})
        self.assertEqual((rc, o["disposition"]), (0, "RECORDED"), o)
        self.assertEqual([h["exit_code"] for h in o["hooks"]], [0, 2, 0])
        self.assertEqual([h["outcome"] for h in o["hooks"]], ["success", "error", "success"])
        w = " | ".join(o["warnings"])
        self.assertIn("hook_nonzero_exit", w)
        self.assertEqual(w.count("hook_stdout_nonempty"), 2)
        self.assertIn("api_retry count=2", w)
        self.assertEqual(o["cli_internal_retries"], 2)
        self.assertEqual(len(self.consults()), 1)

    # ---------------- argv / env / cwd / stdin / secrets / records (AC 11)
    def test_K16_argv_env_cwd_stdin_and_secrets(self):
        secrets = {"ANTHROPIC_API_KEY": KEY, "ADVISOR_ANTHROPIC_API_KEY": KEY, "ANTHROPIC_AUTH_TOKEN": KEY}
        env = self.stub_env({"events": cli_ok(answer="見 sk-ant-secretvalue123 的處理")}, secrets)
        before = dict(env)
        pl, ev = self.good_api()
        rc, o, _ = self.ticketed(None, pl=pl, ev=ev, env=env)
        self.assertEqual((rc, o["disposition"]), (0, "RECORDED"), o)
        self.assertEqual(env, before, "the caller's env object must not be touched")
        seen = self.seen()[-1]
        self.assertEqual(seen["argv"], ["-p", "--model", R_MODEL, "--effort", "high", "--tools", "", "--strict-mcp-config",
                                        "--disable-slash-commands", "--no-session-persistence", "--output-format",
                                        "stream-json", "--verbose"])
        for k in secrets:
            self.assertNotIn(k, seen["env_keys"])
        self.assertIn("STUB_DIR", seen["env_keys"])                       # everything else is passed on
        self.assertIsNone(seen["echo_key"])
        want_cwd = Path(tempfile.gettempdir()).resolve() / "cueshot_advisor_cli_cwd"
        self.assertEqual(Path(seen["cwd"]).resolve(), want_cwd)
        self.assertTrue((want_cwd / ".git").exists())
        self.assertNotIn(self.repo.resolve(), want_cwd.parents)
        self.assertTrue(seen["stdin"].startswith("你是諮詢顧問"))
        self.assertIn(pl.read_text(encoding="utf-8"), seen["stdin"])
        self.assertIn("sk-ant-***", o["answer"])
        blob = json.dumps(o) + self.records()[0].read_text(encoding="utf-8") + self.consults()[0]["note"]
        self.assertNotIn("secretvalue123", blob)
        self.assertNotIn(KEY, blob)

    def test_K17_record_names_are_unique_and_record_failure_is_not_fatal(self):
        for i in range(2):
            rc, o, _ = self.ticketed({"events": cli_ok()}, name=f"u{i}")
            self.assertEqual(rc, 0, o)
        names = [p.name for p in self.records()]
        self.assertEqual(len(names), 2)
        self.assertEqual(len(set(names)), 2)                              # same frozen clock second, still distinct
        shutil.rmtree(self.repo / "tmp" / TICKET / "advisor_cli")
        (self.repo / "tmp" / TICKET / "advisor_cli").write_text("a regular file in the way", encoding="utf-8")
        rc, o, _ = self.ticketed({"events": cli_ok()}, name="rf1")
        self.assertEqual((rc, o["disposition"], o["record"], o["record_error"]), (0, "RECORDED", None, "FileExistsError"), o)
        self.assertTrue(any("record_error" in w for w in o["warnings"]))
        self.assertIn("record=none", self.consults()[-1]["note"])
        n = len(self.consults())
        rc, o, _ = self.ticketed({"events": cli_unauth(), "rc": 1}, name="rf2")
        self.assertEqual((rc, o["reason"], o["record"]), (6, "not_logged_in", None), o)   # classification unchanged
        self.assertEqual(len(self.consults()), n)

    def sub_cli(self, tdir, scenario=None):
        env = self.stub_env(scenario or {"events": cli_ok()}, {"TEMP": str(tdir), "TMP": str(tdir), "TMPDIR": str(tdir)})
        pl, ev = self.good_api(name=f"s{tdir.name}")
        res = subprocess.run([sys.executable, "-I", str(ROUTE), "--ticket", TICKET, "--table", str(self.cli_table()),
                              "consult", "--incident", self.inc, "--source", self.SRC, "--payload", str(pl), "--evidence", str(ev)],
                             cwd=self.repo, capture_output=True, env=env)
        return res.returncode, json.loads(res.stdout.decode("utf-8", "replace"))

    def test_K18_cwd_preparation_branches(self):
        t1 = self.tmp / "t1"
        t1.mkdir()
        (t1 / "cueshot_advisor_cli_cwd").write_text("a file", encoding="utf-8")
        rc, o = self.sub_cli(t1)
        self.assertEqual((rc, o["reason"], o["detail"]), (6, "cli_unavailable", "cwd_not_directory"), o)
        t2 = self.repo / "tt"
        t2.mkdir()
        rc, o = self.sub_cli(t2)
        self.assertEqual((rc, o["reason"], o["detail"]), (6, "cli_unavailable", "cwd_inside_repo"), o)
        self.assertFalse((t2 / "cueshot_advisor_cli_cwd").exists(), "nothing may be created inside the repo")
        self.assertEqual(self.launches(), 0)
        t3 = self.tmp / "t3"
        (t3 / "cueshot_advisor_cli_cwd").mkdir(parents=True)               # exists, but has no .git: initialised, not refused
        rc, o = self.sub_cli(t3)
        self.assertEqual((rc, o["disposition"]), (0, "RECORDED"), o)
        self.assertTrue((t3 / "cueshot_advisor_cli_cwd" / ".git").exists())
        self.assertEqual(self.launches(), 1)

    # ---------------- do_native source label (AC 13)
    def test_K19_native_note_records_the_actual_source(self):
        alias = self.table_copy(lambda t: t["sources"].update({"advisor.alias": dict(t["sources"]["advisor.native"])}))
        for i, (source, tbl) in enumerate((("advisor.alias", alias), ("advisor.native", None))):
            purpose = f"second opinion {i}"
            rc, d, _ = self.run_main(["consult", "--incident", self.inc, "--source", source, "--phase", "dispatch",
                                      "--purpose", purpose], table=tbl)
            self.assertEqual(rc, 0, d)
            rc, r, _ = self.run_main(["consult", "--incident", self.inc, "--source", source, "--phase", "return",
                                      "--dispatch-id", d["dispatch_id"], "--purpose", purpose, "--note", "native advisor called"],
                                     table=tbl)
            self.assertEqual((rc, r["disposition"]), (0, "RECORDED"), r)
            self.assertIn(f"source={source} via=route.py", self.consults()[-1]["note"])


if __name__ == "__main__":
    unittest.main()
