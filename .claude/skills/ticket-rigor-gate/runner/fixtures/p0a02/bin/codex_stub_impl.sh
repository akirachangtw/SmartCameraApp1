#!/usr/bin/env bash
# RGR-P0A-02 AI-3 deterministic true-call stub.
# Never invokes real Codex. Records invocation in CODEX_STUB_SENTINEL, then
# copies the canned response file (CODEX_STUB_RESPONSE_FILE) to whatever path
# was passed via --output-last-message, and emits a minimal JSONL stub event
# to stdout (captured by repair_runner.sh into events.jsonl). Exit 0.
set -uo pipefail
: "${CODEX_STUB_SENTINEL:?CODEX_STUB_SENTINEL 未設定}"
: "${CODEX_STUB_RESPONSE_FILE:?CODEX_STUB_RESPONSE_FILE 未設定}"

echo "STUB_INVOKED args=$*" >> "$CODEX_STUB_SENTINEL"

output_last_message=""
prev=""
for arg in "$@"; do
  if [[ "$prev" == "--output-last-message" ]]; then
    output_last_message="$arg"
  fi
  prev="$arg"
done

if [[ -z "$output_last_message" ]]; then
  echo "codex stub: --output-last-message 未提供於呼叫參數" >&2
  exit 1
fi

if ! cp -- "$CODEX_STUB_RESPONSE_FILE" "$output_last_message"; then
  echo "codex stub: 無法複製 canned response $CODEX_STUB_RESPONSE_FILE -> $output_last_message" >&2
  exit 1
fi

echo '{"type":"stub_event","note":"RGR-P0A-02 AI-3 deterministic stub, no real Codex call"}'
exit 0
