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

if [[ "$QUOTA" == "exhausted" ]]; then
  printf '{"decision":"block","reason":"MEO: defer_and_checkpoint (quota exhausted)"}'
else
  printf '{"decision":"block","reason":"MEO: escalation review required","systemMessage":"MEO escalation pending — choose: approve_upgrade / deny_retry_with_high_effort / defer_and_checkpoint"}'
fi
exit 0
