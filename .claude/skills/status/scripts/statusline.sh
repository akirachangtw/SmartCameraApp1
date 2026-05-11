#!/usr/bin/env bash
# Cache the raw payload so /status skill can read real data when invoked mid-session.
# Use a relative path — Claude Code hooks always execute from the project root.
CACHE_FILE="tmp/claude_status_cache.json"
mkdir -p "$(dirname "$CACHE_FILE")"
input=$(cat | tee "$CACHE_FILE")

# ── Nerd Fonts 圖示 ──
# 如果你的終端機沒有安裝 Nerd Fonts，可以把這些改成一般文字
# 例如 ICON_CTX="ctx"、ICON_CLOCK="⏰"、ICON_COST="$"
ICON_CTX=$(printf '\uf2db')     # 晶片圖示
ICON_CLOCK=$(printf '\uf017')   # 時鐘圖示
ICON_COST=$(printf '\uf155')    # 美元圖示

# ── 讀取資料 ──
model=$(echo "$input" | jq -r '.model.display_name // ""')
ctx_remain=$(echo "$input" | jq -r '.context_window.remaining_percentage // empty')
five_h_used=$(echo "$input" | jq -r '.rate_limits.five_hour.used_percentage // empty')
seven_d_used=$(echo "$input" | jq -r '.rate_limits.seven_day.used_percentage // empty')
cost=$(echo "$input" | jq -r '.cost.total_cost_usd // empty')

# ── 顏色函式：依剩餘百分比決定顏色 ──
# 綠色 ≥50% │ 黃色 21-49% │ 紅色 ≤20%
color_by_remain() {
  local val=$1
  if [ "$val" -le 20 ]; then
    printf '\033[31m'   # 紅色
  elif [ "$val" -le 49 ]; then
    printf '\033[33m'   # 黃色
  else
    printf '\033[32m'   # 綠色
  fi
}

# ── 進度條：10 格寬度 ──
mini_bar() {
  local percent=$1
  local width=10
  local filled=$((percent * width / 100))
  local empty=$((width - filled))
  local i=0
  while [ $i -lt $filled ]; do printf '━'; i=$((i + 1)); done
  while [ $i -lt $width ]; do printf '┄'; i=$((i + 1)); done
}

# ── 分隔符號 ──
SEP=$(printf '\033[2m │ \033[0m')

# ── 組合輸出 ──
parts=""

# Model（紫色粗體，讓它跟其他資訊有區隔）
if [ -n "$model" ]; then
  parts=$(printf '\033[1;35m%s\033[0m' "$model")
fi

# Context 剩餘（進度條 + 百分比）
if [ -n "$ctx_remain" ]; then
  val=$(printf '%.0f' "$ctx_remain")
  color=$(color_by_remain "$val")
  bar=$(mini_bar "$val")
  parts="$parts$SEP$(printf '%s%s %s %s%%\033[0m' "$color" "$ICON_CTX" "$bar" "$val")"
fi

# 5 小時 Rate Limit
if [ -n "$five_h_used" ]; then
  val=$((100 - $(printf '%.0f' "$five_h_used")))
  color=$(color_by_remain "$val")
  parts="$parts$SEP$(printf '%s%s 5h:%s%%\033[0m' "$color" "$ICON_CLOCK" "$val")"
fi

# 7 天 Rate Limit
if [ -n "$seven_d_used" ]; then
  val=$((100 - $(printf '%.0f' "$seven_d_used")))
  color=$(color_by_remain "$val")
  parts="$parts$SEP$(printf '%s%s 7d:%s%%\033[0m' "$color" "$ICON_CLOCK" "$val")"
fi

# Session 花費（灰色，不搶眼但需要時看得到）
if [ -n "$cost" ] && [ "$cost" != "0" ] && [ "$cost" != "null" ]; then
  parts="$parts$SEP$(printf '\033[2m%s $%s\033[0m' "$ICON_COST" "$cost")"
fi

printf '%s' "$parts"