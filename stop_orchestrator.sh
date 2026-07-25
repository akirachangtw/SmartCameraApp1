#!/usr/bin/env bash
# stop_orchestrator.sh — unified Stop gate for gate.conf [meo] (single stop_orchestrator, G2)
# Contract: stdout JSON {decision, reason, systemMessage} only when blocking; exit 0 always.
set -uo pipefail

GATE_CONF="${MEO_GATE_CONF:-gate.conf}"
[[ -f "$GATE_CONF" ]] || exit 0

ENABLED="$(grep -A5 '^\[meo\]' "$GATE_CONF" | grep -m1 '^enabled=' | cut -d= -f2-)"
[[ "$ENABLED" == "true" ]] || exit 0

STATE_FILE="$(grep -A5 '^\[meo\]' "$GATE_CONF" | grep -m1 '^state_file=' | cut -d= -f2-)"
STATE_FILE="${STATE_FILE:-STATE.md}"
[[ -f "$STATE_FILE" ]] || exit 0

PENDING="$(grep -m1 '^MEO_ESCALATION_PENDING=' "$STATE_FILE" 2>/dev/null | cut -d= -f2-)"
[[ "$PENDING" == "true" ]] || exit 0

QUOTA="$(grep -m1 '^MEO_QUOTA_STATE=' "$STATE_FILE" 2>/dev/null | cut -d= -f2-)"

# Rev F/AI-1: pending/quota-exhausted are "awaiting external adjudication" states —
# Stop MUST NOT block (livelock) and MUST NOT approve (no such value); omit decision.
if [[ "$QUOTA" == "exhausted" ]]; then
  printf '{"systemMessage":"MEO quota exhausted — deferring escalation to next quota window; new MEO governance writes require external adjudication, see meo/adjudications/"}'
else
  printf '{"systemMessage":"MEO escalation pending — new MEO governance writes require external adjudication, see meo/adjudications/"}'
fi
exit 0
