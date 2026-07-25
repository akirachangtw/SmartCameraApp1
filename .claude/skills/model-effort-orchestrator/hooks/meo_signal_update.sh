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

# MEO-T3 AI-5:SV/QA 驗證管線執行期間之被動觀測訊號,不計入任務 STREAK/RETRY(94465d9 型汙染防制)
CMD="$(printf '%s' "$INPUT" | jq -r '.tool_input.command // empty')"
case "$CMD" in
  *.claude/skills/qa/scripts/*|*.claude/skills/sv/scripts/*) exit 0 ;;
esac

kv_get() { grep -m1 "^$1=" "$STATE_FILE" 2>/dev/null | cut -d= -f2-; }
kv_set() {
  if grep -q "^$1=" "$STATE_FILE" 2>/dev/null; then
    sed -i "s|^$1=.*|$1=$2|" "$STATE_FILE"
  else
    printf '%s=%s\n' "$1" "$2" >> "$STATE_FILE"
  fi
}

[ "$EVENT" = "PostToolUse" ] || [ "$EVENT" = "PostToolUseFailure" ] || exit 0

# Rev F/AI-2 §A.1-2: mkdir-atomic lock covers ID increment AND every kv_set
# read-modify-write below (not just the ID) — retry <=5x@0.1s, then fail-open
# with zero writes attempted (lock not yet held, nothing written).
LOCK_DIR="$STATE_FILE.lock"
LOCKED=false
for _ in 1 2 3 4 5; do
  if mkdir "$LOCK_DIR" 2>/dev/null; then LOCKED=true; break; fi
  sleep 0.1
done
[ "$LOCKED" = true ] || exit 0
trap 'rmdir "$LOCK_DIR" 2>/dev/null' EXIT

if [ "$EVENT" = "PostToolUse" ]; then
  # 成功路徑:僅重置 RETRY_COUNT。
  # CSO R4:STREAK 不重置——交錯型硬磨(成功/同類失敗交替)為必抓模式。
  kv_set MEO_RETRY_COUNT 0
  # §B watermark 衰減(成功路徑已歸零 RETRY → 衰減 WM_RETRY)
  WM_RETRY="$(kv_get MEO_WM_RETRY)"; WM_RETRY="${WM_RETRY:-0}"
  [ "$WM_RETRY" -gt 0 ] && kv_set MEO_WM_RETRY 0
  exit 0
fi

# --- 失敗路徑 ---
# §A 律三 corrupt-ID guard 最先執行(先於任何 kv_set),確保「非數字→零寫入」為字面事實。
ID_RAW="$(kv_get MEO_ESCALATION_ID)"
if [ -n "$ID_RAW" ] && ! [[ "$ID_RAW" =~ ^[0-9]+$ ]]; then
  exit 0
fi

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
  # §B watermark 衰減(class 已切換,STREAK 被重置為 1 → WM_STREAK 隨之衰減)
  kv_set MEO_WM_STREAK 0
  kv_set MEO_WM_ERROR_CLASS "$ERR_CLASS"
fi

# §B watermark 衰減(REJECT 由他處遞增,本腳本僅記錄衰減,MUST NOT 作為觸發條件)
REJECT="$(kv_get MEO_VERIFIER_REJECT_COUNT)"; REJECT="${REJECT:-0}"
WM_REJECT="$(kv_get MEO_WM_REJECT)"; WM_REJECT="${WM_REJECT:-0}"
[ "$REJECT" -lt "$WM_REJECT" ] && kv_set MEO_WM_REJECT "$REJECT"

NEW_RETRY="$(kv_get MEO_RETRY_COUNT)"; NEW_RETRY="${NEW_RETRY:-0}"
NEW_STREAK="$(kv_get MEO_SAME_ERROR_CLASS_STREAK)"; NEW_STREAK="${NEW_STREAK:-0}"

# §A 律二:PENDING=true 期間只累積原始訊號(已完成),不遞增 ID、不建立新 episode。
PENDING="$(kv_get MEO_ESCALATION_PENDING)"
[ "$PENDING" = "true" ] && exit 0

# §A 律三:ID 缺失/空值 → 以 0 初始化後續行。
ID="${ID_RAW:-0}"
[ -z "$ID_RAW" ] && kv_set MEO_ESCALATION_ID 0

# D1:閾值=2,數值直取,現改為相對 watermark 之增量比較(§B,缺值視為 0)。
WM_RETRY="$(kv_get MEO_WM_RETRY)"; WM_RETRY="${WM_RETRY:-0}"
WM_STREAK="$(kv_get MEO_WM_STREAK)"; WM_STREAK="${WM_STREAK:-0}"
DELTA_HIT=false
[ "$((NEW_RETRY - WM_RETRY))" -ge 2 ] && DELTA_HIT=true
[ "$((NEW_STREAK - WM_STREAK))" -ge 2 ] && DELTA_HIT=true

# §A 律一:僅 PENDING=false 且 delta 達標,才遞增 MEO_ESCALATION_ID 並掛起。
if [ "$DELTA_HIT" = true ]; then
  kv_set MEO_ESCALATION_ID "$((ID + 1))"
  kv_set MEO_ESCALATION_PENDING true
fi
exit 0
