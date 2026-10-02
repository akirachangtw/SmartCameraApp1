#!/usr/bin/env bash
# .claude/hooks/telegram_ask.sh
# PreToolUse Hook (matcher: AskUserQuestion) — Claude 提問等待選擇時推播 Telegram
# 原因：AskUserQuestion 是回合中的工具呼叫，Stop hook 不會觸發，需獨立通知

set -euo pipefail

_HOOK_INPUT=$(cat 2>/dev/null || true)

QUESTION_TEXT=""
if [[ -n "$_HOOK_INPUT" ]]; then
    # PYTHONUTF8=1: py on Windows defaults stdout to cp1252, which cannot
    # encode emoji/CJK and would silently fail the whole parse.
    QUESTION_TEXT=$(echo "$_HOOK_INPUT" | PYTHONUTF8=1 py -c "
import json, sys
try:
    d = json.load(sys.stdin)
    lines = []
    for q in d.get('tool_input', {}).get('questions', []):
        lines.append('❓ ' + q.get('question', ''))
        opts = [o.get('label', '') for o in q.get('options', [])]
        if opts:
            lines.append('🔘 ' + ' / '.join(opts))
    print('\n'.join(lines)[:600])
except Exception:
    print('')
" 2>/dev/null || true)
fi

[[ -z "$QUESTION_TEXT" ]] && QUESTION_TEXT="❓ Claude 提出選擇題"

bash .claude/hooks/notify.sh "🙋 Claude 等待 Akira 選擇
${QUESTION_TEXT}" 0
exit 0
