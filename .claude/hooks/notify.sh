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

# Use py (Windows Python Launcher) to send — mingw64 curl --data-urlencode
# corrupts multi-byte UTF-8 (emoji/CJK) by replacing with '?' before encoding.
TELEGRAM_BOT_TOKEN="$BOT_TOKEN" TELEGRAM_CHAT_ID="$CHAT_ID" TELEGRAM_MSG="$FULL_MSG" \
  py -c "
import urllib.request, urllib.parse, os
token = os.environ['TELEGRAM_BOT_TOKEN']
chat_id = os.environ['TELEGRAM_CHAT_ID']
msg = os.environ['TELEGRAM_MSG']
data = urllib.parse.urlencode({'chat_id': chat_id, 'text': msg, 'parse_mode': 'HTML'}).encode('utf-8')
req = urllib.request.Request('https://api.telegram.org/bot' + token + '/sendMessage', data=data)
urllib.request.urlopen(req, timeout=10)
" > /dev/null 2>&1 &

echo "✅ [telegram-notify] 送出：${MSG_TEXT}"
exit 0
