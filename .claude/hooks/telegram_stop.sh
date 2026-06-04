#!/usr/bin/env bash
# .claude/hooks/telegram_stop.sh
# Smart Stop Hook — Telegram 通知智能路由
# 黑名單靜默 → SV PASS 富通知 → UC DONE 通知 → 通用停止

set -euo pipefail

CACHE="tmp/claude_status_cache.json"
LAST_MSG=""

if [[ -f "$CACHE" ]]; then
    LAST_MSG=$(python3 -c "
import json, sys
try:
    d = json.load(open('$CACHE'))
    print(d.get('last_assistant_message',''))
except:
    print('')
" 2>/dev/null || true)
fi

# ── 黑名單 1: /status 狀態報告 ─────────────────────────────────
if echo "$LAST_MSG" | grep -q "Session 狀態報告"; then
    echo "[telegram_stop] Skip: /status report (blacklisted)"
    exit 0
fi

# ── 黑名單 2: Boot Sequence ────────────────────────────────────
if echo "$LAST_MSG" | grep -qE "停止等.*Ticket|停止等候指示|Boot Report.*停止"; then
    echo "[telegram_stop] Skip: Boot Sequence (blacklisted)"
    exit 0
fi

# ── 黑名單 3: 純 Git 操作（無 SV/QA 伴隨）─────────────────────
if echo "$LAST_MSG" | grep -qE "提交成功|原子提交完成|commit [0-9a-f]{7}"; then
    if ! echo "$LAST_MSG" | grep -qE "SV.*PASS|Cross-Review.*PASS|QA Report"; then
        echo "[telegram_stop] Skip: git-only operation (blacklisted)"
        exit 0
    fi
fi

# ── 觸發 1: SV + Cross-Review PASS ────────────────────────────
if echo "$LAST_MSG" | grep -qE "Cross-Review.*PASS|STRUCTURAL VERIFY PASS.*A/B/C/D/E/F"; then
    TICKET=$(echo "$LAST_MSG" | grep -oE "Ticket: [A-Z0-9(). _+-]+" | head -1 | sed 's/Ticket: //' | xargs)
    [[ -z "$TICKET" ]] && TICKET=$(echo "$LAST_MSG" | grep -oE "P08-[A-Z][0-9]+|D[0-9]+-T[0-9]+|INFRA-[0-9]+|HOTFIX-[0-9]+" | head -1)
    ROUND=$(echo "$LAST_MSG" | grep -oE "R[0-9]+ PASS|PASS R[0-9]+" | head -1 | grep -oE "R[0-9]+" || echo "R1")
    MSG="${TICKET} ✅ SV+Cross-Review ${ROUND} PASS — 準備 Traycer 終審。下一步？"
    bash .claude/skills/telegram-notify/scripts/notify.sh "$MSG" 0
    exit 0
fi

# ── 觸發 2: UC ✅ DONE ─────────────────────────────────────────
if echo "$LAST_MSG" | grep -qE "✅ DONE \|"; then
    DONE_LINE=$(echo "$LAST_MSG" | grep -E "✅ DONE \|" | head -1 | xargs)
    MSG="${DONE_LINE} — 請執行 /qa"
    bash .claude/skills/telegram-notify/scripts/notify.sh "$MSG" 0
    exit 0
fi

# ── 觸發 3: QA PASS（Auto-Chain 停止點）──────────────────────
if echo "$LAST_MSG" | grep -qE "QA Report|Build: ✅ PASS.*Tests:.*PASS"; then
    TICKET=$(echo "$LAST_MSG" | grep -oE "P08-[A-Z][0-9]+|D[0-9]+-T[0-9]+|INFRA-[0-9]+|HOTFIX-[0-9]+" | head -1)
    MSG="${TICKET} 🔨 /qa PASS — 請執行 /sv"
    bash .claude/skills/telegram-notify/scripts/notify.sh "$MSG" 0
    exit 0
fi

# ── 預設: 通用停止通知 ─────────────────────────────────────────
bash .claude/skills/telegram-notify/scripts/notify.sh "⏸️ Claude 已停止，等待 Akira 操作" 0
exit 0
