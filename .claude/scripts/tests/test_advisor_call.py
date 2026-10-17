"""Tests for advisor_call.py (ADV-APICALL-01). Standard library unittest only; zero network.

Every case drives main(...) with an injected fake client, a tempfile output directory and injected
env / now. The two MockTransport cases (3c, 3d) use the real anthropic SDK with a mocked transport
and assume anthropic==0.105.2. Nothing is written inside the repository; the repository's gate
ledger and verify conf for this ticket are only compared (byte-equal) before/after the module run.
"""
from __future__ import annotations

import contextlib
import hashlib
import io
import json
import os
import re
import subprocess
import sys
import tempfile
import unittest
from datetime import datetime, timezone
from pathlib import Path
from types import SimpleNamespace
from unittest import mock

SCRIPTS = Path(__file__).resolve().parent.parent
REPO_ROOT = SCRIPTS.parent.parent
FIXTURE = SCRIPTS / "tests" / "fixtures" / "advisor_seed_payload.md"
sys.path.insert(0, str(SCRIPTS))
import anthropic  # noqa: E402
import httpx  # noqa: E402
import advisor_call as ac  # noqa: E402

KEY = "test-advisor-key-0123456789"
FIXED = datetime(2026, 10, 17, 9, 30, 0, tzinfo=timezone.utc)
GOOD_ADVICE = {
    "strategy": "在 next_backoff 回傳前套用 cap 上限",
    "steps": ["backoff.py: 回傳值以 cap 為上限"],
    "minimal_verification": "python check_backoff.py",
    "authority_gap": False,
    "authority_gap_reason": "",
    "evidence_needed": [],
}
GOOD_TEXT = json.dumps(GOOD_ADVICE, ensure_ascii=False)
FORBIDDEN_KWARGS = ("tools", "tool_choice", "thinking", "temperature", "top_p", "top_k", "stream", "metadata")


def _git_common_dir():
    out = subprocess.run(["git", "rev-parse", "--git-common-dir"], cwd=REPO_ROOT, capture_output=True,
                         text=True, check=True).stdout.strip()
    p = Path(out)
    return p if p.is_absolute() else REPO_ROOT / p


def _own_paths():
    paths = [_git_common_dir() / "gate" / "ADV-APICALL-01.jsonl", _git_common_dir() / "gate" / "ADV-APICALL-01.jsonl.lock"]
    paths += sorted((REPO_ROOT / "tmp" / "verify").glob("ADV-APICALL-01.*.conf"))
    return paths


def _snapshot():
    return {str(p): (p.read_bytes() if p.exists() else None) for p in _own_paths()}


_BASELINE = {}


def setUpModule():
    _BASELINE.update(_snapshot())


def tearDownModule():
    after = _snapshot()
    if after != _BASELINE:
        raise AssertionError("本票可歸屬落點（gate ledger／verify conf）於測試期間被改變")


def usage(i=1000, o=500, cr=0, cw=0):
    return SimpleNamespace(input_tokens=i, output_tokens=o, cache_read_input_tokens=cr, cache_creation_input_tokens=cw)


def make_resp(text=GOOD_TEXT, stop="end_turn", use=None, content=None, rid="req_fake_1", stop_details=None):
    blocks = content if content is not None else [SimpleNamespace(type="text", text=text)]
    return SimpleNamespace(content=blocks, stop_reason=stop, usage=use if use is not None else usage(),
                           model="claude-opus-5-5", _request_id=rid, stop_details=stop_details)


def sdk_err(cls, status, msg="boom"):
    req = httpx.Request("POST", "https://api.anthropic.com/v1/messages")
    body = {"type": "error", "error": {"type": "x", "message": msg}}
    resp = httpx.Response(status, request=req, headers={"request-id": f"req_err_{status}"}, json=body)
    return cls(msg, response=resp, body=body)


class FakeClient:
    def __init__(self, result):
        self.result = result
        self.calls = []
        self.messages = self

    def create(self, **kw):
        self.calls.append(kw)
        if isinstance(self.result, BaseException):
            raise self.result
        return self.result


class Factory:
    def __init__(self, client):
        self.client = client
        self.called = False
        self.kwargs = None
        self.environ_at_build = None

    def __call__(self, **kw):
        self.called = True
        self.kwargs = kw
        self.environ_at_build = dict(os.environ)
        return self.client


class Base(unittest.TestCase):
    def setUp(self):
        self._tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self._tmp.cleanup)
        self.tmp = Path(self._tmp.name)
        self.out = self.tmp / "out"
        self.payload_text = FIXTURE.read_text(encoding="utf-8")

    def write_payload(self, text):
        p = self.tmp / "payload.md"
        p.write_text(text, encoding="utf-8", newline="\n")
        return p

    def run_main(self, extra=None, *, env=None, factory=None, payload=None, ticket="ADV-APICALL-01", os_env=None):
        argv = ["--ticket", ticket, "--payload", str(payload or FIXTURE), "--out-dir", str(self.out)] + list(extra or [])
        env = {ac.KEY_VAR: KEY} if env is None else env
        factory = factory or Factory(FakeClient(make_resp()))
        out, err = io.StringIO(), io.StringIO()
        with mock.patch.dict(os.environ, os_env or {}), contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
            rc = ac.main(argv, client_factory=factory, env=env, now=lambda: FIXED)
        return rc, out.getvalue(), err.getvalue(), factory

    def receipts(self):
        return sorted(self.out.glob("advisor_*.json")) if self.out.exists() else []

    def load_receipt(self):
        files = self.receipts()
        self.assertEqual(len(files), 1)
        return files[0], json.loads(files[0].read_text(encoding="utf-8"))

    def assert_no_secret(self, *texts):
        for t in texts:
            self.assertNotIn(KEY, t)
            self.assertNotRegex(t, r"sk-ant-[A-Za-z0-9_-]+")


class PrecheckTests(Base):
    def assert_exit2(self, extra=None, *, env=None, payload=None, ticket="ADV-APICALL-01", os_env=None):
        rc, out, err, factory = self.run_main(extra, env=env, payload=payload, ticket=ticket, os_env=os_env)
        self.assertEqual(rc, 2, err)
        self.assertFalse(factory.called)
        self.assertEqual(self.receipts(), [])
        self.assert_no_secret(out, err)
        return err

    def test_01_missing_or_wrong_key(self):
        self.assert_exit2(env={})
        self.assert_exit2(env={ac.KEY_VAR: "   "})
        self.assert_exit2(env={"ANTHROPIC_API_KEY": KEY}, os_env={"ANTHROPIC_API_KEY": KEY})

    def test_01_ticket_and_payload(self):
        self.assert_exit2(ticket="adv-1")
        self.assert_exit2(ticket="ADV")
        self.assert_exit2(payload=self.tmp / "nope.md")
        self.assert_exit2(["--max-payload-bytes", "10"])
        text = self.payload_text.replace("## 既有授權", "## X").replace("## 要求輸出", "## Y")
        err = self.assert_exit2(payload=self.write_payload(text))
        self.assertIn("## 既有授權", err)
        self.assertIn("## 要求輸出", err)
        self.assert_exit2(payload=self.write_payload(self.payload_text + "\nsk-ant-abcDEF123\n"))
        self.assert_exit2(payload=self.write_payload(self.payload_text + f"\n{ac.KEY_VAR}=x\n"))
        self.tmp.joinpath("bin.md").write_bytes(b"\xff\xfe\x00bad")
        self.assert_exit2(payload=self.tmp / "bin.md")

    def test_01_ticket_accepts_gate_ledger_shape(self):
        rc, _, err, _ = self.run_main(ticket="ADV-APICALL-01")
        self.assertEqual(rc, 0, err)

    def test_01_bad_arguments(self):
        for extra in (["--effort", "ultra"], ["--max-tokens", "0"], ["--max-tokens", "abc"], ["--max-tokens", "24001"],
                      ["--timeout", "0"], ["--timeout", "601"], ["--request-mode", "x"], ["--max-payload-bytes", "-1"]):
            with self.subTest(extra=extra):
                self.assert_exit2(extra)

    def test_02_dry_run(self):
        rc, out, err, factory = self.run_main(["--dry-run"], env={})
        self.assertEqual(rc, 0, err)
        self.assertFalse(factory.called)
        self.assertFalse(self.out.exists())
        self.assertEqual(len(out.strip().splitlines()), 1)
        data = json.loads(out)
        raw = FIXTURE.read_bytes()
        self.assertEqual(data["payload"]["sha256"], hashlib.sha256(raw).hexdigest())
        self.assertEqual(data["payload"]["bytes"], len(raw))
        self.assertEqual(data["request_mode"], "json_schema")
        self.assertNotIn("## 目標", out)
        self.assertNotIn("cap not applied", out)
        self.assert_no_secret(out, err)


class RequestShapeTests(Base):
    def assert_common_shape(self, kw):
        self.assertEqual(set(kw), {"model", "max_tokens", "system", "messages", "output_config"})
        for bad in FORBIDDEN_KWARGS:
            self.assertNotIn(bad, kw)
        self.assertEqual(kw["model"], "claude-opus-5-5")
        self.assertEqual(kw["max_tokens"], 12000)
        self.assertTrue(isinstance(kw["system"], str) and kw["system"].strip())
        self.assertEqual(len(kw["messages"]), 1)
        self.assertEqual(kw["messages"][0], {"role": "user", "content": self.payload_text})
        self.assertNotEqual(kw["messages"][-1]["role"], "assistant")
        self.assertEqual(kw["output_config"]["effort"], "medium")

    def test_03a_json_schema_default(self):
        client = FakeClient(make_resp())
        rc, out, err, _ = self.run_main(factory=Factory(client))
        self.assertEqual(rc, 0, err)
        self.assertEqual(len(client.calls), 1)
        kw = client.calls[0]
        self.assert_common_shape(kw)
        fmt = kw["output_config"]["format"]
        self.assertEqual(fmt["type"], "json_schema")
        schema = fmt["schema"]
        self.assertIs(schema["additionalProperties"], False)
        self.assertEqual(sorted(schema["required"]),
                         sorted(["strategy", "steps", "minimal_verification", "authority_gap",
                                 "authority_gap_reason", "evidence_needed"]))
        self.assertEqual(self.load_receipt()[1]["request_mode"], "json_schema")

    def test_03b_prompt_json_fallback(self):
        client = FakeClient(make_resp())
        rc, out, err, _ = self.run_main(["--request-mode", "prompt_json"], factory=Factory(client))
        self.assertEqual(rc, 0, err)
        kw = client.calls[0]
        self.assert_common_shape(kw)
        self.assertEqual(set(kw["output_config"]), {"effort"})
        self.assertIn(ac.ADVICE_SCHEMA_JSON, kw["system"])
        self.assertIn("只輸出單一 JSON", kw["system"])
        self.assertEqual(self.load_receipt()[1]["request_mode"], "prompt_json")

    def test_03b_prompt_json_does_not_loosen_validation(self):
        for text in (f"```json\n{GOOD_TEXT}\n```", f"以下是結果：{GOOD_TEXT}"):
            with self.subTest(text=text[:12]):
                self.out = self.tmp / f"o{abs(hash(text))}"
                rc, out, err, _ = self.run_main(["--request-mode", "prompt_json"],
                                                factory=Factory(FakeClient(make_resp(text))))
                self.assertEqual(rc, 6)
                _, receipt = self.load_receipt()
                self.assertEqual(receipt["status"], "UNPARSEABLE")
                self.assertIsNone(receipt["advice"])

    def _real_factory(self, handler):
        def factory(**kw):
            return anthropic.Anthropic(http_client=httpx.Client(transport=httpx.MockTransport(handler)), **kw)
        return factory

    def test_03c_nonstreaming_guard_passes_24000(self):
        seen = []

        def handler(request):
            seen.append(json.loads(request.content))
            body = {"type": "error", "error": {"type": "invalid_request_error", "message": "mock"}}
            return httpx.Response(400, json=body, headers={"request-id": "req_hdr_400"})

        rc, out, err, _ = self.run_main(["--max-tokens", "24000"], factory=self._real_factory(handler))
        self.assertEqual(rc, 3, err)
        self.assertEqual(len(seen), 1)
        self.assertEqual(seen[0]["max_tokens"], 24000)
        _, receipt = self.load_receipt()
        self.assertEqual(receipt["status"], "API_ERROR")
        self.assertEqual(receipt["request_id"], "req_hdr_400")
        self.assertEqual(receipt["error"]["class"], "BadRequestError")
        self.assertEqual(receipt["error"]["http_status"], 400)

    def test_03d_real_sdk_success_chain(self):
        seen = []
        message = {
            "id": "msg_01", "type": "message", "role": "assistant", "model": "claude-opus-5-5",
            "content": [{"type": "text", "text": GOOD_TEXT}], "stop_reason": "end_turn", "stop_sequence": None,
            "usage": {"input_tokens": 123, "output_tokens": 45},
        }

        def handler(request):
            seen.append(json.loads(request.content))
            return httpx.Response(200, json=message, headers={"request-id": "req_hdr_ok"})

        rc, out, err, _ = self.run_main(factory=self._real_factory(handler), payload=FIXTURE)
        self.assertEqual(rc, 0, err)
        self.assertEqual(len(seen), 1)
        self.assertEqual(seen[0]["messages"][0]["content"], self.payload_text)
        path, receipt = self.load_receipt()
        self.assertEqual(receipt["status"], "SUCCESS")
        self.assertEqual(receipt["request_id"], "req_hdr_ok")
        self.assertIn("req_hdr_ok", path.name)
        self.assertEqual(receipt["usage"]["input_tokens"], 123)
        self.assertEqual(receipt["usage"]["output_tokens"], 45)
        first = json.loads(out.splitlines()[0])
        self.assertEqual(first["receipt_sha256"], hashlib.sha256(path.read_bytes()).hexdigest())
        self.assertEqual(first["request_id"], "req_hdr_ok")

    def test_04_credential_isolation(self):
        client = FakeClient(make_resp())
        factory = Factory(client)
        before = os.environ.get("ANTHROPIC_AUTH_TOKEN")
        env = {ac.KEY_VAR: KEY, "ANTHROPIC_BASE_URL": "https://evil.example", "ANTHROPIC_AUTH_TOKEN": "t",
               "ANTHROPIC_PROFILE": "p"}
        os_env = {"ANTHROPIC_BASE_URL": "https://evil.example", "ANTHROPIC_AUTH_TOKEN": "t", "ANTHROPIC_PROFILE": "p"}
        rc, out, err, _ = self.run_main(["--timeout", "450"], env=env, factory=factory, os_env=os_env)
        self.assertEqual(rc, 0, err)
        self.assertEqual(factory.kwargs, {"api_key": KEY, "base_url": "https://api.anthropic.com",
                                          "timeout": 450, "max_retries": 0})
        self.assertNotIn("ANTHROPIC_AUTH_TOKEN", factory.environ_at_build)
        self.assertNotIn("ANTHROPIC_PROFILE", factory.environ_at_build)
        self.assertEqual(os.environ.get("ANTHROPIC_AUTH_TOKEN"), before)
        self.assert_no_secret(out, err, self.load_receipt()[0].read_text(encoding="utf-8"))
        factory2 = Factory(FakeClient(make_resp()))
        self.out = self.tmp / "out2"
        self.run_main(factory=factory2)
        self.assertEqual(factory2.kwargs["timeout"], 300)


class ReceiptTests(Base):
    def test_05_success_receipt(self):
        content = [SimpleNamespace(type="thinking", thinking="SECRET-THINKING-TEXT"),
                   SimpleNamespace(type="text", text=GOOD_TEXT)]
        rc, out, err, _ = self.run_main(factory=Factory(FakeClient(make_resp(content=content))))
        self.assertEqual(rc, 0, err)
        path, receipt = self.load_receipt()
        self.assertEqual(path.name, "advisor_20261017T093000Z_req_fake_1.json")
        self.assertEqual(receipt["schema_version"], 1)
        self.assertEqual(receipt["kind"], "advisor_api_receipt")
        self.assertEqual(receipt["status"], "SUCCESS")
        self.assertEqual(receipt["exit_code"], 0)
        self.assertEqual(receipt["advice"], GOOD_ADVICE)
        self.assertEqual(receipt["payload"]["sha256"], hashlib.sha256(FIXTURE.read_bytes()).hexdigest())
        self.assertEqual(receipt["sdk_max_retries"], 0)
        self.assertEqual(receipt["request_mode"], "json_schema")
        self.assertEqual(receipt["created_at_utc"], "2026-10-17T09:30:00Z")
        self.assertEqual(receipt["estimated_cost_usd"], 0.014)
        self.assertTrue(receipt["pricing_basis"])
        self.assertEqual(receipt["usage"]["input_tokens"], 1000)
        self.assertEqual(receipt["sdk"]["name"], "anthropic")
        for field in ("model_requested", "model_returned", "request_id", "stop_reason", "stop_details", "effort",
                      "max_tokens", "timeout_s", "raw_text", "error", "ticket"):
            self.assertIn(field, receipt)
        self.assertEqual(receipt["raw_text"], GOOD_TEXT)
        first = json.loads(out.splitlines()[0])
        self.assertEqual(set(first), {"status", "exit_code", "receipt", "receipt_sha256", "request_id",
                                      "estimated_cost_usd"})
        self.assertEqual(first["receipt_sha256"], hashlib.sha256(path.read_bytes()).hexdigest())
        self.assertIn("strategy:", out)
        raw = path.read_bytes()
        self.assertNotIn(b"\r", raw)
        for blob in (raw.decode("utf-8"), out, err):
            self.assert_no_secret(blob)
            self.assertNotIn("## 目標", blob)
            self.assertNotIn("SECRET-THINKING-TEXT", blob)

    def test_05_cost_variants(self):
        cases = [(usage(1000, 500, 2000, 400), 0.0164), (usage(1000, 500, None, None), 0.014)]
        for i, (use, expected) in enumerate(cases):
            with self.subTest(i=i):
                self.out = self.tmp / f"c{i}"
                rc, _, err, _ = self.run_main(factory=Factory(FakeClient(make_resp(use=use))))
                self.assertEqual(rc, 0, err)
                _, receipt = self.load_receipt()
                self.assertEqual(receipt["estimated_cost_usd"], expected)
                if i == 1:
                    self.assertIsNone(receipt["usage"]["cache_read_input_tokens"])
                    self.assertIsNone(receipt["usage"]["cache_creation_input_tokens"])

    def test_05_unknown_model_has_no_estimate(self):
        rc, _, err, _ = self.run_main(["--model", "claude-other"], factory=Factory(FakeClient(make_resp())))
        self.assertEqual(rc, 0, err)
        _, receipt = self.load_receipt()
        self.assertIsNone(receipt["estimated_cost_usd"])
        self.assertEqual(receipt["pricing_basis"], "unknown for model")

    def test_05_key_like_reply_nulls_raw_text(self):
        leaked = dict(GOOD_ADVICE, strategy="用 sk-ant-abcDEF123 呼叫")
        text = json.dumps(leaked, ensure_ascii=False)
        rc, out, err, _ = self.run_main(factory=Factory(FakeClient(make_resp(text))))
        self.assertEqual(rc, 0, err)
        path, receipt = self.load_receipt()
        self.assertIsNone(receipt["raw_text"])
        self.assertEqual(receipt["status"], "SUCCESS")
        self.assertEqual(receipt["request_id"], "req_fake_1")
        self.assertEqual(receipt["usage"]["input_tokens"], 1000)
        self.assertEqual(receipt["payload"]["sha256"], hashlib.sha256(FIXTURE.read_bytes()).hexdigest())
        for blob in (path.read_text(encoding="utf-8"), out, err):
            self.assertNotIn("sk-ant-abcDEF123", blob)

    def test_05_sensitive_header_words_null_raw_text(self):
        for word in ("Authorization", "x-api-key", f"{ac.KEY_VAR}="):
            with self.subTest(word=word):
                self.out = self.tmp / f"h{abs(hash(word))}"
                text = json.dumps(dict(GOOD_ADVICE, strategy=f"檢查 {word} 設定"), ensure_ascii=False)
                rc, _, err, _ = self.run_main(factory=Factory(FakeClient(make_resp(text))))
                self.assertEqual(rc, 0, err)
                self.assertIsNone(self.load_receipt()[1]["raw_text"])

    def test_06_failure_modes(self):
        small = make_resp
        cases = [
            ("refusal", small("", "refusal", stop_details={"category": "x"}), 5, "REFUSAL"),
            ("max_tokens full json", small(GOOD_TEXT, "max_tokens"), 6, "TRUNCATED"),
            ("empty content", small(content=[]), 6, "EMPTY"),
            ("thinking only", small(content=[SimpleNamespace(type="thinking", thinking="x")]), 6, "EMPTY"),
            ("non-json", small("not json at all"), 6, "UNPARSEABLE"),
            ("missing field", small(json.dumps({"strategy": "s"})), 6, "SCHEMA_INVALID"),
            ("bad type", small(json.dumps(dict(GOOD_ADVICE, steps="x"))), 6, "SCHEMA_INVALID"),
            ("extra field", small(json.dumps(dict(GOOD_ADVICE, extra=1))), 6, "SCHEMA_INVALID"),
            ("blank strategy", small(json.dumps(dict(GOOD_ADVICE, strategy="  "))), 6, "SCHEMA_INVALID"),
            ("pause_turn", small(GOOD_TEXT, "pause_turn"), 6, "UNEXPECTED_STOP"),
            ("tool_use", small(GOOD_TEXT, "tool_use"), 6, "UNEXPECTED_STOP"),
            ("stop None", small(GOOD_TEXT, None), 6, "UNEXPECTED_STOP"),
            ("429", sdk_err(anthropic.RateLimitError, 429), 4, "API_ERROR"),
            ("500", sdk_err(anthropic.InternalServerError, 500), 4, "API_ERROR"),
            ("connection", anthropic.APIConnectionError(request=httpx.Request("POST", "https://x")), 4, "API_ERROR"),
            ("timeout", anthropic.APITimeoutError(request=httpx.Request("POST", "https://x")), 4, "API_ERROR"),
            ("400", sdk_err(anthropic.BadRequestError, 400), 3, "API_ERROR"),
            ("401", sdk_err(anthropic.AuthenticationError, 401), 3, "API_ERROR"),
            ("404", sdk_err(anthropic.NotFoundError, 404), 3, "API_ERROR"),
        ]
        for i, (name, result, code, status) in enumerate(cases):
            with self.subTest(name=name):
                self.out = self.tmp / f"f{i}"
                client = FakeClient(result)
                rc, out, err, _ = self.run_main(factory=Factory(client))
                self.assertEqual(rc, code, err)
                self.assertEqual(len(client.calls), 1)
                _, receipt = self.load_receipt()
                self.assertEqual(receipt["status"], status)
                self.assertEqual(receipt["exit_code"], code)
                self.assertIsNone(receipt["advice"])
                self.assertNotEqual(receipt["status"], "SUCCESS")
                self.assertEqual(json.loads(out.splitlines()[0])["status"], status)

    def test_06_refusal_records_stop_details(self):
        resp = make_resp("", "refusal", stop_details={"category": "x"})
        self.run_main(factory=Factory(FakeClient(resp)))
        self.assertEqual(self.load_receipt()[1]["stop_details"], {"category": "x"})

    def test_06_exception_message_is_masked(self):
        err_obj = sdk_err(anthropic.BadRequestError, 400, msg=f"bad key {KEY} and sk-ant-zzz999")
        rc, out, err, _ = self.run_main(factory=Factory(FakeClient(err_obj)))
        self.assertEqual(rc, 3)
        path, receipt = self.load_receipt()
        self.assertIn("[REDACTED]", receipt["error"]["message"])
        self.assert_no_secret(path.read_text(encoding="utf-8"), out, err)

    def test_06_internal_error_after_response(self):
        client = FakeClient(make_resp())
        with mock.patch.object(ac, "_estimate_cost", side_effect=RuntimeError(f"cost boom {KEY}")):
            rc, out, err, _ = self.run_main(factory=Factory(client))
        self.assertEqual(rc, 1)
        self.assertEqual(len(client.calls), 1)
        path, receipt = self.load_receipt()
        self.assertEqual(receipt["status"], "INTERNAL_ERROR")
        self.assertEqual(receipt["exit_code"], 1)
        self.assertIsNone(receipt["advice"])
        self.assertIsNone(receipt["raw_text"])
        self.assertEqual(receipt["request_id"], "req_fake_1")
        self.assertEqual(receipt["usage"]["input_tokens"], 1000)
        self.assertEqual(receipt["error"]["class"], "RuntimeError")
        self.assertIsNone(receipt["error"]["http_status"])
        self.assert_no_secret(path.read_text(encoding="utf-8"), out, err)
        self.assertEqual(json.loads(out.splitlines()[0])["status"], "INTERNAL_ERROR")

    def test_06_internal_error_receipt_write_failure(self):
        client = FakeClient(make_resp())
        with mock.patch.object(ac, "_write_receipt", side_effect=OSError(f"disk full {KEY}")):
            rc, out, err, _ = self.run_main(factory=Factory(client))
        self.assertEqual(rc, 1)
        self.assertEqual(len(client.calls), 1)
        self.assertEqual(self.receipts(), [])
        self.assertIn("req_fake_1", err)
        self.assertNotIn("Traceback", err)
        self.assertNotIn("## 目標", err)
        self.assert_no_secret(out, err)

    def test_06_keyboard_interrupt_propagates(self):
        with mock.patch.object(ac, "_estimate_cost", side_effect=KeyboardInterrupt):
            with self.assertRaises(KeyboardInterrupt):
                self.run_main(factory=Factory(FakeClient(make_resp())))
        self.assertEqual(self.receipts(), [])

    def test_06_unexpected_error_before_response_writes_no_receipt(self):
        rc, out, err, _ = self.run_main(factory=Factory(FakeClient(ValueError(f"weird {KEY}"))))
        self.assertEqual(rc, 1)
        self.assertEqual(self.receipts(), [])
        self.assertNotIn("Traceback", err)
        self.assert_no_secret(out, err)

    def test_07_receipt_is_never_overwritten(self):
        rc, _, _, _ = self.run_main()
        self.assertEqual(rc, 0)
        path, _ = self.load_receipt()
        original = path.read_bytes()
        client = FakeClient(make_resp(use=usage(1, 1)))
        rc, out, err, _ = self.run_main(factory=Factory(client))
        self.assertEqual(rc, 1)
        self.assertEqual(path.read_bytes(), original)
        self.assertEqual([p.name for p in self.out.iterdir()], [path.name])
        self.assertIn("req_fake_1", err)
        self.assertNotIn("Traceback", err)
        self.assert_no_secret(out, err)


class FixtureTests(Base):
    def test_08_seed_fixture_is_valid_and_has_no_answer_key(self):
        text = self.payload_text
        ac._check_payload(FIXTURE, ac.MAX_PAYLOAD_BYTES_DEFAULT)
        self.assertNotIn("min(cap", text)
        self.assertIn("cap not applied: got 1024.0", text)
        blocks = re.findall(r"```python\n(# file: (\S+)\n.*?)```", text, re.S)
        self.assertEqual(sorted(name for _, name in blocks), ["backoff.py", "check_backoff.py"])
        work = self.tmp / "seed"
        work.mkdir()
        for body, name in blocks:
            (work / name).write_text(body, encoding="utf-8", newline="\n")
        run = lambda: subprocess.run([sys.executable, "check_backoff.py"], cwd=work, capture_output=True,  # noqa: E731
                                     text=True)
        broken = run()
        self.assertNotEqual(broken.returncode, 0)
        self.assertIn("cap not applied: got 1024.0", broken.stderr)
        backoff = (work / "backoff.py").read_text(encoding="utf-8")
        fixed = backoff.replace("return base * (2 ** attempt)", "return min(cap, base * (2 ** attempt))")
        self.assertNotEqual(fixed, backoff)
        (work / "backoff.py").write_text(fixed, encoding="utf-8", newline="\n")
        ok = run()
        self.assertEqual(ok.returncode, 0, ok.stderr)
        self.assertEqual(ok.stdout.strip(), "OK")


if __name__ == "__main__":
    unittest.main()
