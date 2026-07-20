#!/usr/bin/env bash
# meo_session_reset.sh — SessionStart: 每 session 重置診斷狀態(CSO R10)
# Contract: stdout empty; exit 0 always.
set -uo pipefail
STATE_FILE="${MEO_STATE_FILE:-D:/DevSources/AndroidProj/SmartCameraApp1/STATE.md}"
grep -q '^MEO_LAST_DIAGNOSIS_TS=' "$STATE_FILE" 2>/dev/null && sed -i 's|^MEO_LAST_DIAGNOSIS_TS=.*|MEO_LAST_DIAGNOSIS_TS=|' "$STATE_FILE"
grep -q '^MEO_FASTPATH=' "$STATE_FILE" 2>/dev/null && sed -i 's|^MEO_FASTPATH=.*|MEO_FASTPATH=false|' "$STATE_FILE"
exit 0
