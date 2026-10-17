#!/usr/bin/env python3
"""advisor_call.py - single-shot, tool-less Opus engineering-advisor API caller (ADV-APICALL-01).

Usage:
    python .claude/scripts/advisor_call.py --ticket <TICKET_ID> --payload <path>
        [--out-dir <dir>] [--model claude-opus-5-5] [--effort low|medium|high|xhigh|max]
        [--max-tokens 12000] [--timeout 300] [--request-mode json_schema|prompt_json]
        [--max-payload-bytes 200000] [--dry-run]

Credential: ONLY the environment variable ADVISOR_ANTHROPIC_API_KEY is read. ANTHROPIC_API_KEY is
never read, no key file is read, and there is no --api-key option. The key never reaches stdout,
stderr, receipt, exception text or argv.

Request: one non-streaming Messages API call, no tools, fixed advisor role, max_retries=0.
request_mode:
  json_schema  main path: output_config.format = json_schema (ADVICE_SCHEMA).
  prompt_json  fallback chosen ONLY by the caller: no output_config.format, the system prompt embeds
               ADVICE_SCHEMA_JSON. Used only when the API explicitly rejects structured output (or its
               combination with thinking). If the message points at ADVICE_SCHEMA itself, fix the schema.
               Validation is identical in both modes (strict json.loads + ADVICE_SCHEMA check).
This script never retries or switches mode by itself. Real validation allows at most two network
attempts in total (shared quota); the caller decides the second attempt from the receipt.

Exit codes / receipt status:
  0 SUCCESS       end_turn, non-empty text, strict JSON, ADVICE_SCHEMA fields/types, strategy not blank
  1 (INTERNAL_ERROR) unexpected local Exception after a response was obtained (best-effort receipt;
                  if the receipt cannot be written, stderr gets a masked summary only); also unexpected
                  errors before a response (no receipt) and receipt write failure / existing target
  2 (no receipt)  usage / configuration / precheck failure (missing key, missing SDK, bad payload, bad args)
  3 API_ERROR     not retryable: 400/401/403/404/422 and other non-408/409/429/5xx status errors
  4 API_ERROR     transient (NOT retried here): 429, 5xx, 408/409, connection error, timeout
  5 REFUSAL       stop_reason == "refusal"
  6 TRUNCATED | EMPTY | UNPARSEABLE | SCHEMA_INVALID | UNEXPECTED_STOP

Receipt: <out-dir>/advisor_<UTC yyyymmddTHHMMSSZ>_<request_id|norequest>.json (default out-dir
tmp/<TICKET_ID>/advisor/), schema_version 1, kind "advisor_api_receipt"; fields: status, exit_code,
ticket, created_at_utc, model_requested, model_returned, request_id, stop_reason, stop_details, effort,
max_tokens, timeout_s, request_mode, sdk, sdk_max_retries (always 0), payload {path, sha256, bytes},
usage, estimated_cost_usd, pricing_basis, advice, raw_text, error {class, http_status, message}.
Written exclusively (never overwrites), UTF-8, LF. stdout line 1 is one JSON object:
{status, exit_code, receipt, receipt_sha256, request_id, estimated_cost_usd}.
A receipt is a local execution record, not independent proof of billing or provider-side execution.
estimated_cost_usd is an estimate; a request that timed out may still have been processed (cost is
not zero even though no usage is known).

A successful advisor reply is NOT a solved problem: the executor's local verification decides.
Until the T02 contract relaxation takes effect, a receipt must not be recorded as
`consult --kind advisor`.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import sys
import tempfile
from datetime import datetime, timezone
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent.parent
KEY_VAR = "ADVISOR_ANTHROPIC_API_KEY"
BASE_URL = "https://api.anthropic.com"
MODEL_DEFAULT = "claude-opus-5-5"
EFFORTS = ("low", "medium", "high", "xhigh", "max")
REQUEST_MODES = ("json_schema", "prompt_json")
MAX_TOKENS_DEFAULT = 12000
MAX_TOKENS_CEILING = 24000
TIMEOUT_DEFAULT = 300
TIMEOUT_CEILING = 600
MAX_PAYLOAD_BYTES_DEFAULT = 200000
TICKET_RE = re.compile(r"^[A-Z][A-Z0-9]*(-[A-Z0-9]+)+$")
SK_RE = re.compile(r"sk-ant-[A-Za-z0-9_-]+")
HEADINGS = ("## 目標", "## 失敗證據", "## 相關內容", "## 已試策略與結果", "## 既有授權", "## 要求輸出")
USAGE_KEYS = ("input_tokens", "output_tokens", "cache_read_input_tokens", "cache_creation_input_tokens")
PRICING_BASIS = (
    "claude-opus-5-5 $4/$20 per MTok, cache read $0.20, 5m cache write $5; "
    "platform.claude.com/docs/en/about-claude/pricing last_updated 2026-10-16"
)
PRICING_UNKNOWN = "unknown for model"
TRANSIENT_STATUS = (408, 409, 429)

ROLE = (
    "你是資深工程顧問，只依使用者提供之 payload 內容判斷；你看不到 repo 與其他任何檔案，也沒有任何工具。"
    "只裁決本地可推導之設計／修法；產品語義、SSOT、scope、權限、不可逆動作一律不裁決，"
    "將 authority_gap 設為 true 並於 authority_gap_reason 說明。不得假設已獲授權；"
    "證據不足時於 evidence_needed 列出所需證據。回覆以繁體中文為主，英文僅限技術術語與識別字。"
)
ADVICE_SCHEMA = {
    "type": "object",
    "properties": {
        "strategy": {"type": "string"},
        "steps": {"type": "array", "items": {"type": "string"}},
        "minimal_verification": {"type": "string"},
        "authority_gap": {"type": "boolean"},
        "authority_gap_reason": {"type": "string"},
        "evidence_needed": {"type": "array", "items": {"type": "string"}},
    },
    "required": [
        "strategy", "steps", "minimal_verification",
        "authority_gap", "authority_gap_reason", "evidence_needed",
    ],
    "additionalProperties": False,
}
ADVICE_SCHEMA_JSON = json.dumps(ADVICE_SCHEMA, ensure_ascii=False, sort_keys=True)
OUTPUT_INSTRUCTION = (
    "只輸出單一 JSON 物件，須符合下列 JSON Schema；不得使用 code fence，亦不得有前後說明文字。\n"
    "JSON Schema："
)


class _Usage(Exception):
    """Usage / configuration failure -> exit 2."""


class _Parser(argparse.ArgumentParser):
    def error(self, message):
        raise _Usage(message)


def _utf8() -> None:
    for stream in (sys.stdout, sys.stderr):
        reconfigure = getattr(stream, "reconfigure", None)
        if reconfigure:
            try:
                reconfigure(encoding="utf-8")
            except Exception:
                pass


def _redact(text, secret=""):
    text = SK_RE.sub("[REDACTED]", str(text))
    if secret:
        text = text.replace(secret, "[REDACTED]")
    return text


def _deep_redact(obj, secret=""):
    if isinstance(obj, str):
        return _redact(obj, secret)
    if isinstance(obj, list):
        return [_deep_redact(x, secret) for x in obj]
    if isinstance(obj, dict):
        return {k: _deep_redact(v, secret) for k, v in obj.items()}
    return obj


def _jsonable(value):
    if value is None or isinstance(value, (str, int, float, bool)):
        return value
    dump = getattr(value, "model_dump", None)
    if callable(dump):
        try:
            return dump(mode="json")
        except Exception:
            pass
    if isinstance(value, (list, tuple)):
        return [_jsonable(x) for x in value]
    if isinstance(value, dict):
        return {str(k): _jsonable(v) for k, v in value.items()}
    return str(value)


def _raw_text_sensitive(text, secret):
    # Fixed rule, no semantic judgement (header names matched case-insensitively: false positives only
    # null raw_text, which is the safe side).
    low = text.lower()
    return bool(
        SK_RE.search(text)
        or (secret and secret in text)
        or "authorization" in low
        or "x-api-key" in low
        or f"{KEY_VAR}=" in text
    )


def _positive_int(text):
    try:
        value = int(text)
    except ValueError:
        raise argparse.ArgumentTypeError(f"需為正整數: {text!r}")
    if value <= 0:
        raise argparse.ArgumentTypeError(f"需為正整數: {text!r}")
    return value


def _build_parser():
    p = _Parser(prog="advisor_call.py", description="Single-shot tool-less Opus advisor API caller.")
    p.add_argument("--ticket", required=True)
    p.add_argument("--payload", required=True)
    p.add_argument("--out-dir")
    p.add_argument("--model", default=MODEL_DEFAULT)
    p.add_argument("--effort", default="medium", choices=EFFORTS)
    p.add_argument("--max-tokens", type=_positive_int, default=MAX_TOKENS_DEFAULT)
    p.add_argument("--timeout", type=float, default=TIMEOUT_DEFAULT)
    p.add_argument("--request-mode", default="json_schema", choices=REQUEST_MODES)
    p.add_argument("--max-payload-bytes", type=_positive_int, default=MAX_PAYLOAD_BYTES_DEFAULT)
    p.add_argument("--dry-run", action="store_true")
    return p


def _parse(argv):
    args = _build_parser().parse_args(argv)
    if not TICKET_RE.match(args.ticket):
        raise _Usage(f"--ticket 格式非法: {args.ticket!r}")
    if args.max_tokens > MAX_TOKENS_CEILING:
        raise _Usage(f"--max-tokens 不得超過 {MAX_TOKENS_CEILING}")
    if not (0 < args.timeout <= TIMEOUT_CEILING):
        raise _Usage(f"--timeout 需為正數且不超過 {TIMEOUT_CEILING}")
    if args.timeout == int(args.timeout):
        args.timeout = int(args.timeout)
    return args


def _check_payload(path, max_bytes):
    p = Path(path)
    try:
        raw = p.read_bytes()
    except OSError as exc:
        raise _Usage(f"payload 不可讀: {path} ({type(exc).__name__})")
    if len(raw) > max_bytes:
        raise _Usage(f"payload 過大: {len(raw)} > {max_bytes} bytes")
    try:
        text = raw.decode("utf-8")
    except UnicodeDecodeError:
        raise _Usage("payload 非 UTF-8")
    lines = {line.rstrip() for line in text.splitlines()}
    missing = [h for h in HEADINGS if h not in lines]
    if missing:
        raise _Usage("payload 缺固定標題: " + "、".join(missing))
    if "sk-ant-" in text or f"{KEY_VAR}=" in text:
        raise _Usage("payload 含疑似金鑰字串，拒送")
    return text, hashlib.sha256(raw).hexdigest(), len(raw)


def _validate_advice(obj):
    """Return None when valid, else a short reason."""
    if not isinstance(obj, dict):
        return "not an object"
    if set(obj) != set(ADVICE_SCHEMA["required"]):
        return "fields mismatch"
    for key in ("strategy", "minimal_verification", "authority_gap_reason"):
        if not isinstance(obj[key], str):
            return f"{key} type"
    for key in ("steps", "evidence_needed"):
        if not isinstance(obj[key], list) or not all(isinstance(x, str) for x in obj[key]):
            return f"{key} type"
    if not isinstance(obj["authority_gap"], bool):
        return "authority_gap type"
    if not obj["strategy"].strip():
        return "strategy blank"
    return None


def _classify(resp, secret):
    """Return (status, exit_code, advice, raw_text)."""
    text = "".join(
        getattr(b, "text", "") or "" for b in (getattr(resp, "content", None) or []) if getattr(b, "type", None) == "text"
    )
    raw = None if (not text or _raw_text_sensitive(text, secret)) else text
    stop = getattr(resp, "stop_reason", None)
    if stop == "refusal":
        return "REFUSAL", 5, None, raw
    if stop == "max_tokens":
        return "TRUNCATED", 6, None, raw
    if stop != "end_turn":
        return "UNEXPECTED_STOP", 6, None, raw
    if not text.strip():
        return "EMPTY", 6, None, raw
    try:
        obj = json.loads(text)
    except ValueError:
        return "UNPARSEABLE", 6, None, raw
    if _validate_advice(obj) is not None:
        return "SCHEMA_INVALID", 6, None, raw
    return "SUCCESS", 0, _deep_redact(obj, secret), raw


def _usage_dict(resp):
    usage = getattr(resp, "usage", None)
    if usage is None:
        return None
    return {k: getattr(usage, k, None) for k in USAGE_KEYS}


def _estimate_cost(model, usage):
    if model != MODEL_DEFAULT or usage is None:
        return None
    z = lambda k: usage.get(k) or 0  # noqa: E731 - cache fields may be None
    total = (
        z("input_tokens") * 4 + z("output_tokens") * 20
        + z("cache_read_input_tokens") * 0.20 + z("cache_creation_input_tokens") * 5
    )
    return round(total / 1_000_000, 10)


def _base_receipt(ctx, status, exit_code):
    return {
        "schema_version": 1,
        "kind": "advisor_api_receipt",
        "status": status,
        "exit_code": exit_code,
        "ticket": ctx["args"].ticket,
        "created_at_utc": ctx["dt"].strftime("%Y-%m-%dT%H:%M:%SZ"),
        "model_requested": ctx["args"].model,
        "model_returned": None,
        "request_id": None,
        "stop_reason": None,
        "stop_details": None,
        "effort": ctx["args"].effort,
        "max_tokens": ctx["args"].max_tokens,
        "timeout_s": ctx["args"].timeout,
        "request_mode": ctx["args"].request_mode,
        "sdk": {"name": "anthropic", "version": ctx["sdk_version"]},
        "sdk_max_retries": 0,
        "payload": ctx["payload"],
        "usage": None,
        "estimated_cost_usd": None,
        "pricing_basis": PRICING_UNKNOWN,
        "advice": None,
        "raw_text": None,
        "error": None,
    }


def _receipt_from_response(resp, ctx):
    secret = ctx["secret"]
    status, code, advice, raw = _classify(resp, secret)
    receipt = _base_receipt(ctx, status, code)
    usage = _usage_dict(resp)
    cost = _estimate_cost(ctx["args"].model, usage)
    receipt.update(
        model_returned=getattr(resp, "model", None),
        request_id=getattr(resp, "_request_id", None),
        stop_reason=getattr(resp, "stop_reason", None),
        stop_details=_deep_redact(_jsonable(getattr(resp, "stop_details", None)), secret),
        usage=usage,
        estimated_cost_usd=cost,
        pricing_basis=PRICING_BASIS if cost is not None else PRICING_UNKNOWN,
        advice=advice,
        raw_text=raw,
    )
    return receipt, code


def _receipt_from_exception(exc, ctx):
    import anthropic

    secret = ctx["secret"]
    status_code = getattr(exc, "status_code", None) if isinstance(exc, anthropic.APIStatusError) else None
    transient = isinstance(exc, anthropic.APIConnectionError) or status_code in TRANSIENT_STATUS or (
        status_code is not None and status_code >= 500
    )
    code = 4 if transient else 3
    receipt = _base_receipt(ctx, "API_ERROR", code)
    receipt["request_id"] = getattr(exc, "request_id", None)
    receipt["error"] = {
        "class": type(exc).__name__,
        "http_status": status_code,
        "message": _redact(str(exc), secret)[:2000],
    }
    return receipt, code


def _internal_error_receipt(resp, exc, ctx):
    secret = ctx["secret"]
    receipt = _base_receipt(ctx, "INTERNAL_ERROR", 1)
    for field, fn in (
        ("request_id", lambda: getattr(resp, "_request_id", None)),
        ("usage", lambda: _usage_dict(resp)),
        ("model_returned", lambda: getattr(resp, "model", None)),
        ("stop_reason", lambda: getattr(resp, "stop_reason", None)),
    ):
        try:
            receipt[field] = fn()
        except Exception:
            pass
    receipt["error"] = {
        "class": type(exc).__name__,
        "http_status": None,
        "message": _redact(str(exc), secret)[:2000],
    }
    return receipt, 1


def _write_receipt(out_dir, receipt, dt):
    rid = re.sub(r"[^A-Za-z0-9_-]", "_", receipt.get("request_id") or "norequest")
    name = f"advisor_{dt.strftime('%Y%m%dT%H%M%SZ')}_{rid}.json"
    data = (json.dumps(receipt, ensure_ascii=False, indent=2, sort_keys=True) + "\n").encode("utf-8")
    out_dir = Path(out_dir)
    out_dir.mkdir(parents=True, exist_ok=True)
    target = out_dir / name
    fd, tmp = tempfile.mkstemp(dir=out_dir, prefix=".advisor_", suffix=".tmp")
    try:
        with os.fdopen(fd, "wb") as handle:
            handle.write(data)
        try:
            os.link(tmp, target)  # exclusive: raises FileExistsError, never overwrites
        except FileExistsError:
            raise
        except OSError:
            efd = os.open(target, os.O_WRONLY | os.O_CREAT | os.O_EXCL)
            with os.fdopen(efd, "wb") as handle:
                handle.write(data)
    finally:
        try:
            os.unlink(tmp)
        except OSError:
            pass
    return target, hashlib.sha256(data).hexdigest()


def _stderr_summary(prefix, receipt, exc, secret):
    summary = {
        "request_id": receipt.get("request_id"),
        "usage": receipt.get("usage"),
        "error": f"{type(exc).__name__}: {exc}",
    }
    print(f"advisor_call: {prefix} " + _redact(json.dumps(summary, ensure_ascii=False), secret), file=sys.stderr)


def _print_advice(advice):
    print(f"strategy: {advice['strategy']}")
    print("steps:")
    for i, step in enumerate(advice["steps"], 1):
        print(f"  {i}. {step}")
    print(f"minimal_verification: {advice['minimal_verification']}")
    print(f"authority_gap: {advice['authority_gap']}")


def _build_request(args, payload_text):
    system = ROLE
    output_config = {"effort": args.effort}
    if args.request_mode == "json_schema":
        output_config["format"] = {"type": "json_schema", "schema": ADVICE_SCHEMA}
    else:
        system = f"{ROLE}\n\n{OUTPUT_INSTRUCTION}\n{ADVICE_SCHEMA_JSON}"
    return {
        "model": args.model,
        "max_tokens": args.max_tokens,
        "system": system,
        "messages": [{"role": "user", "content": payload_text}],
        "output_config": output_config,
    }


def main(argv=None, *, client_factory=None, env=None, now=None) -> int:
    _utf8()
    env = os.environ if env is None else env
    now = now or (lambda: datetime.now(timezone.utc))
    secret = ""
    try:
        args = _parse(argv)
        payload_text, sha, nbytes = _check_payload(args.payload, args.max_payload_bytes)
        out_dir = Path(args.out_dir) if args.out_dir else REPO_ROOT / "tmp" / args.ticket / "advisor"
        payload = {"path": str(args.payload), "sha256": sha, "bytes": nbytes}
        if args.dry_run:
            print(json.dumps({
                "dry_run": True, "model": args.model, "effort": args.effort, "max_tokens": args.max_tokens,
                "request_mode": args.request_mode, "payload": payload, "out_dir": str(out_dir),
            }, ensure_ascii=False))
            return 0
        secret = (env.get(KEY_VAR) or "").strip()
        if not secret:
            raise _Usage(f"缺少環境變數 {KEY_VAR}（或為空）")
        try:
            import anthropic
        except ImportError:
            raise _Usage("缺少 anthropic SDK（pip install anthropic）")
    except _Usage as exc:
        print(f"advisor_call: {_redact(exc, secret)}", file=sys.stderr)
        return 2

    for var in ("ANTHROPIC_AUTH_TOKEN", "ANTHROPIC_PROFILE"):
        os.environ.pop(var, None)
        if env is not os.environ:
            env.pop(var, None)
    ctx = {"args": args, "payload": payload, "secret": secret, "sdk_version": anthropic.__version__}
    try:
        factory = client_factory or anthropic.Anthropic
        client = factory(api_key=secret, base_url=BASE_URL, timeout=args.timeout, max_retries=0)
        resp = client.messages.create(**_build_request(args, payload_text))
    except (anthropic.APIStatusError, anthropic.APIConnectionError) as exc:
        ctx["dt"] = now()
        receipt, code = _receipt_from_exception(exc, ctx)
    except Exception as exc:
        print(f"advisor_call: unexpected error before response: {type(exc).__name__}: "
              f"{_redact(exc, secret)}", file=sys.stderr)
        return 1
    else:
        ctx["dt"] = now()
        try:
            receipt, code = _receipt_from_response(resp, ctx)
        except Exception as exc:  # INTERNAL_ERROR: Exception only, BaseException propagates
            receipt, code = _internal_error_receipt(resp, exc, ctx)

    try:
        path, receipt_sha = _write_receipt(out_dir, receipt, ctx["dt"])
    except Exception as exc:
        _stderr_summary("receipt write failed:", receipt, exc, secret)
        return 1
    print(json.dumps({
        "status": receipt["status"], "exit_code": code, "receipt": str(path), "receipt_sha256": receipt_sha,
        "request_id": receipt["request_id"], "estimated_cost_usd": receipt["estimated_cost_usd"],
    }, ensure_ascii=False))
    if receipt["status"] == "SUCCESS":
        _print_advice(receipt["advice"])
    return code


if __name__ == "__main__":
    sys.exit(main())
