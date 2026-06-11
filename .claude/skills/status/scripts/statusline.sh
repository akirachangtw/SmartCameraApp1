#!/usr/bin/env bash
# Cache the raw payload so /status skill can read real data when invoked mid-session.
# Use a relative path — Claude Code hooks always execute from the project root.
# NOTE: jq not installed on this system; all JSON parsing done via Python.
CACHE_FILE="tmp/claude_status_cache.json"
mkdir -p "$(dirname "$CACHE_FILE")"
input=$(cat)

PY=$(command -v python 2>/dev/null || command -v python3 2>/dev/null)
if [ -z "$PY" ]; then exit 0; fi

# Extract all fields in one Python call
_fields=$(echo "$input" | "$PY" -c "
import sys, json
try:
    d = json.load(sys.stdin)
except Exception:
    d = {}
def s(v): return '' if v is None else str(v)
ctx  = d.get('context_window') or {}
rl   = d.get('rate_limits') or {}
fh   = (rl.get('five_hour') or {})
sd   = (rl.get('seven_day') or {})
mdl  = (d.get('model') or {})
cost = (d.get('cost') or {})
print('EVENT=' + s(d.get('hook_event_name') or 'statusLine'))
print('MODEL=' + s(mdl.get('display_name')))
print('CTX='   + s(ctx.get('remaining_percentage')))
print('FH='    + s(fh.get('used_percentage')))
print('SD='    + s(sd.get('used_percentage')))
print('COST='  + s(cost.get('total_cost_usd')))
print('HAS_CTX=' + ('yes' if ctx else 'no'))
" 2>/dev/null)

_get() { echo "$_fields" | grep "^$1=" | cut -d= -f2-; }
event_name=$(_get EVENT)
model=$(_get MODEL)
ctx_remain=$(_get CTX)
five_h_used=$(_get FH)
seven_d_used=$(_get SD)
cost=$(_get COST)
has_ctx=$(_get HAS_CTX)

# Write raw payload per event type for diagnostics (avoids Stop/statusLine race)
echo "$input" > "tmp/statusline_raw_${event_name}.json"

# Only cache when payload has rich fields (statusLine context).
# Stop hook payload lacks context_window — skip to avoid overwriting rich data.
if [ "$has_ctx" = "yes" ]; then
  echo "$input" > "$CACHE_FILE"
fi

# ── Nerd Fonts 圖示 ──
ICON_CTX=$(printf '')
ICON_CLOCK=$(printf '')
ICON_COST=$(printf '')

# ── 顏色函式：依剩餘百分比決定顏色 ──
# 綠色 ≥50% │ 黃色 21-49% │ 紅色 ≤20%
color_by_remain() {
  local val=$1
  if [ "$val" -le 20 ]; then
    printf '\033[31m'
  elif [ "$val" -le 49 ]; then
    printf '\033[33m'
  else
    printf '\033[32m'
  fi
}

# ── 進度條：10 格寬度 ──
mini_bar() {
  local percent=$1
  local width=10
  local filled=$((percent * width / 100))
  local i=0
  while [ $i -lt $filled ]; do printf '━'; i=$((i + 1)); done
  while [ $i -lt $width ]; do printf '┄'; i=$((i + 1)); done
}

# ── 分隔符號 ──
SEP=$(printf '\033[2m │ \033[0m')
parts=""

if [ -n "$model" ]; then
  parts=$(printf '\033[1;35m%s\033[0m' "$model")
fi

if [ -n "$ctx_remain" ]; then
  val=$(printf '%.0f' "$ctx_remain")
  color=$(color_by_remain "$val")
  bar=$(mini_bar "$val")
  parts="$parts$SEP$(printf '%s%s %s %s%%\033[0m' "$color" "$ICON_CTX" "$bar" "$val")"
fi

if [ -n "$five_h_used" ]; then
  val=$((100 - $(printf '%.0f' "$five_h_used")))
  color=$(color_by_remain "$val")
  parts="$parts$SEP$(printf '%s%s 5h:%s%%\033[0m' "$color" "$ICON_CLOCK" "$val")"
fi

if [ -n "$seven_d_used" ]; then
  val=$((100 - $(printf '%.0f' "$seven_d_used")))
  color=$(color_by_remain "$val")
  parts="$parts$SEP$(printf '%s%s 7d:%s%%\033[0m' "$color" "$ICON_CLOCK" "$val")"
fi

if [ -n "$cost" ] && [ "$cost" != "0" ] && [ "$cost" != "null" ]; then
  parts="$parts$SEP$(printf '\033[2m%s $%s\033[0m' "$ICON_COST" "$cost")"
fi

printf '%s' "$parts"
