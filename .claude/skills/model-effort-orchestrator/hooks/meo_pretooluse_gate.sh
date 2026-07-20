#!/usr/bin/env bash
# meo_pretooluse_gate.sh — PreToolUse gate for model-effort-orchestrator
# Contract: stdout JSON {decision, reason} only; exit 0 for JSON parsing.
set -euo pipefail

STATE_FILE="${MEO_STATE_FILE:-D:/DevSources/AndroidProj/SmartCameraApp1/STATE.md}"
command -v jq >/dev/null 2>&1 || { printf '{"decision":"approve","reason":"MEO fail-open: jq missing"}'; exit 0; }
INPUT="$(cat)"

TOOL_NAME="$(printf '%s' "$INPUT" | jq -r '.tool_name // empty')"

case "$TOOL_NAME" in
  Write|Edit|MultiEdit|Bash) ;;
  *) printf '{"decision":"approve","reason":"non-gated tool"}'; exit 0 ;;
esac

# v1.2 (CSO R9): STATE.md 診斷自舉豁免 — Edit/Write/MultiEdit 目標為 STATE.md 放行
# 注意:萬用字元尾綴匹配,他處同名 STATE.md 亦豁免(MVP 已知限制);Bash 不豁免
TARGET="$(printf '%s' "$INPUT" | jq -r '.tool_input.file_path // .tool_input.path // empty')"
case "$TARGET" in
  *STATE.md)
    printf '{"decision":"approve","reason":"MEO bootstrap: diagnosis write to STATE.md exempt"}'; exit 0 ;;
esac

if grep -q 'MEO_FASTPATH=true' "$STATE_FILE" 2>/dev/null; then
  printf '{"decision":"approve","reason":"MEO fast-path active"}'; exit 0
fi

if grep -q '^MEO_LAST_DIAGNOSIS_TS=..*' "$STATE_FILE" 2>/dev/null; then
  printf '{"decision":"approve","reason":"MEO diagnosis present"}'; exit 0
fi

printf '{"decision":"block","reason":"MEO: high-risk tool call without diagnosis in STATE.md; run diagnosis first"}'
exit 0
