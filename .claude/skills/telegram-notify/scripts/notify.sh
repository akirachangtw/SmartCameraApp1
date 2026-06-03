#!/bin/bash
BOT_TOKEN="${TELEGRAM_BOT_TOKEN}"
CHAT_ID="${TELEGRAM_CHAT_ID}"

if [[ -z "$BOT_TOKEN" || -z "$CHAT_ID" ]]; then
  echo "[notify] 缺少環境變數，跳過通知" >&2
  exit 0
fi

MSG_TEXT="${1:-✅ Claude Code 任務完成}"
EXIT_CODE="${2:-0}"
TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')
DIR=$(basename "$(pwd)")

if [[ "$EXIT_CODE" -eq 0 ]]; then
  STATUS_EMOJI="✅"
  STATUS_TEXT="完成"
else
  STATUS_EMOJI="❌"
  STATUS_TEXT="中斷 (exit $EXIT_CODE)"
fi

FULL_MSG="${STATUS_EMOJI} <b>Claude Code ${STATUS_TEXT}</b>
📁 專案：<code>${DIR}</code>
🕐 時間：${TIMESTAMP}
📝 ${MSG_TEXT}"

curl -s -X POST "https://api.telegram.org/bot${BOT_TOKEN}/sendMessage" \
  --data-urlencode "chat_id=${CHAT_ID}" \
  --data-urlencode "text=${FULL_MSG}" \
  --data-urlencode "parse_mode=HTML" \
  > /dev/null 2>&1 &

echo "✅ [telegram-notify] 送出：${MSG_TEXT}"
exit 0
