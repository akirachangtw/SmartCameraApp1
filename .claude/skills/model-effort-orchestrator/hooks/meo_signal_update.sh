#!/usr/bin/env bash
# meo_signal_update.sh — mechanical signal accumulator (dual-event, v1.1)
# Registered on BOTH PostToolUse (success-only) and PostToolUseFailure.
# Success/failure discrimination = hook_event_name ONLY (CSO R2).
# Contract: stdout empty; exit 0 always (non-blocking accumulator).
set -uo pipefail

STATE_FILE="${MEO_STATE_FILE:-D:/DevSources/AndroidProj/SmartCameraApp1/STATE.md}"
command -v jq >/dev/null 2>&1 || exit 0   # fail-open

INPUT="$(cat)"
EVENT="$(printf '%s' "$INPUT" | jq -r '.hook_event_name // empty')"
TOOL_NAME="$(printf '%s' "$INPUT" | jq -r '.tool_name // empty')"

case "$TOOL_NAME" in
  Write|Edit|MultiEdit|Bash) ;;
  *) exit 0 ;;
esac

kv_get() { grep -m1 "^$1=" "$STATE_FILE" 2>/dev/null | cut -d= -f2-; }
kv_set() {
  if grep -q "^$1=" "$STATE_FILE" 2>/dev/null; then
    sed -i "s|^$1=.*|$1=$2|" "$STATE_FILE"
  fi
}

if [ "$EVENT" = "PostToolUse" ]; then
  # 成功路徑:僅重置 RETRY_COUNT。
  # CSO R4:STREAK 不重置——交錯型硬磨(成功/同類失敗交替)為必抓模式。
  kv_set MEO_RETRY_COUNT 0
  exit 0
fi

[ "$EVENT" = "PostToolUseFailure" ] || exit 0

# --- 失敗路徑 ---
RETRY="$(kv_get MEO_RETRY_COUNT)"; RETRY="${RETRY:-0}"
kv_set MEO_RETRY_COUNT "$((RETRY + 1))"

# error class:fallback 鏈對 schema 漂移免疫(#19372 欄位未定案)
ERR_MSG="$(printf '%s' "$INPUT" | jq -r '.error // .tool_error // "unknown"' 2>/dev/null | head -c 200)"
ERR_CLASS="$(printf '%s' "$ERR_MSG" | grep -oE '[A-Za-z_]+' | head -1)"
ERR_CLASS="${ERR_CLASS:-unknown}"

LAST_CLASS="$(kv_get MEO_LAST_ERROR_CLASS)"
STREAK="$(kv_get MEO_SAME_ERROR_CLASS_STREAK)"; STREAK="${STREAK:-0}"

if [ "$ERR_CLASS" = "$LAST_CLASS" ]; then
  kv_set MEO_SAME_ERROR_CLASS_STREAK "$((STREAK + 1))"
else
  kv_set MEO_SAME_ERROR_CLASS_STREAK 1
  kv_set MEO_LAST_ERROR_CLASS "$ERR_CLASS"
fi

# D1:閾值=2,數值直取
NEW_RETRY="$(kv_get MEO_RETRY_COUNT)"
NEW_STREAK="$(kv_get MEO_SAME_ERROR_CLASS_STREAK)"
if [ "${NEW_RETRY:-0}" -ge 2 ] || [ "${NEW_STREAK:-0}" -ge 2 ]; then
  kv_set MEO_ESCALATION_PENDING true
fi
exit 0
