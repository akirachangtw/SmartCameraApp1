#!/usr/bin/env bash
# .claude/hooks/tests/telegram_stop_test.sh
# INFRA-HOOK-STOP-01 regression test — no network calls.
# Blanks TELEGRAM_BOT_TOKEN/CHAT_ID so notify.sh short-circuits before any request.

set -uo pipefail

HOOK="$(dirname "$0")/../telegram_stop.sh"
export TELEGRAM_BOT_TOKEN=""
export TELEGRAM_CHAT_ID=""

run_case() {
    local name="$1"
    local payload="$2"
    printf '%s' "$payload" | bash "$HOOK" >/dev/null 2>&1
    local code=$?
    if [[ "$code" -eq 0 ]]; then
        echo "PASS: $name"
        return 0
    else
        echo "FAIL: $name (exit $code)"
        return 1
    fi
}

fail_count=0

run_case "Cross-Review PASS with Ticket ID" \
    '{"last_assistant_message":"GAP-10-BREATH-RESTART ✅ Done · Codex Cross-Review R1 PASS"}' || fail_count=$((fail_count+1))

run_case "Cross-Review PASS without Ticket ID (original bug payload)" \
    '{"last_assistant_message":"📍 Ticket: 無 → 停止\nCodex Cross-Review R1 PASS"}' || fail_count=$((fail_count+1))

run_case "QA Report without Ticket ID" \
    '{"last_assistant_message":"QA Report\nBuild: ✅ PASS Tests: ✅ PASS"}' || fail_count=$((fail_count+1))

run_case "Generic message (default branch)" \
    '{"last_assistant_message":"一般回覆內容，無特殊觸發條件"}' || fail_count=$((fail_count+1))

if [[ "$fail_count" -eq 0 ]]; then
    echo "ALL PASS"
    exit 0
else
    echo "${fail_count} case(s) FAILED"
    exit 1
fi
