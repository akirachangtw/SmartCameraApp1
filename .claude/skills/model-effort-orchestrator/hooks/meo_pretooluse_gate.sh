#!/usr/bin/env bash
# meo_pretooluse_gate.sh — PreToolUse gate for model-effort-orchestrator
# Contract (Codex M1): stdout JSON hookSpecificOutput.{hookEventName,
# permissionDecision,permissionDecisionReason} only; exit 0 for JSON parsing.
set -euo pipefail

STATE_FILE="${MEO_STATE_FILE:-D:/DevSources/AndroidProj/SmartCameraApp1/STATE.md}"

emit() {
  printf '{"hookSpecificOutput":{"hookEventName":"PreToolUse","permissionDecision":"%s","permissionDecisionReason":"%s"}}' "$1" "$2"
  exit 0
}

command -v jq >/dev/null 2>&1 || emit allow "MEO fail-open: jq missing"
INPUT="$(cat)"

TOOL_NAME="$(printf '%s' "$INPUT" | jq -r '.tool_name // empty')"

case "$TOOL_NAME" in
  Write|Edit|MultiEdit|Bash) ;;
  *) emit allow "non-gated tool" ;;
esac

# Rev F/AI-2 §C.4-5: release_pending() — sole executable consumer of the
# meo/adjudications/ dual-artifact channel + the atomic 5-step release
# transition. decision-tree.yaml remains the semantic SSOT; this is
# implementation only. Defined ahead of use (AI-3 wires it into the
# pending-gate branch below).
field() { grep -m1 "^$2:" "$1" 2>/dev/null | cut -d: -f2- | sed 's/^[[:space:]]*//;s/[[:space:]]*$//' || true; }
kv_get_f() { grep -m1 "^$2=" "$1" 2>/dev/null | cut -d= -f2- || true; }
kv_set_f() {
  if grep -q "^$2=" "$1" 2>/dev/null; then
    sed -i "s|^$2=.*|$2=$3|" "$1"
  else
    printf '%s=%s\n' "$2" "$3" >> "$1"
  fi
}

release_pending() {
  local id="$1"
  local rec="meo/adjudications/${id}.md"
  local aud="meo/adjudications/${id}.audit.md"
  [ -f "$rec" ] && [ -f "$aud" ] || return 1

  # §C.4 validation — all MUST pass before any STATE mutation is attempted.
  local decision adjudicator ts_rec signals_preserved
  decision="$(field "$rec" decision)"
  adjudicator="$(field "$rec" adjudicator)"
  ts_rec="$(field "$rec" timestamp)"
  signals_preserved="$(field "$rec" signals_preserved)"
  case "$decision" in
    approve_upgrade|deny_retry_with_high_effort|defer_and_checkpoint) ;;
    *) return 1 ;;
  esac
  [ -n "$adjudicator" ] && [ -n "$ts_rec" ] && [ -n "$signals_preserved" ] || return 1

  local episode_id audit_id ts_aud
  episode_id="$(field "$aud" episode_id)"
  audit_id="$(field "$aud" decision_record_audit_id)"
  ts_aud="$(field "$aud" timestamp)"
  [ "$episode_id" = "$id" ] && [ -n "$audit_id" ] && [ -n "$ts_aud" ] || return 1

  # §C.5: same mkdir lock as §A.1 signal producer, single critical section.
  local lock_dir="$STATE_FILE.lock"
  local locked=false i
  for i in 1 2 3 4 5; do
    if mkdir "$lock_dir" 2>/dev/null; then locked=true; break; fi
    sleep 0.1
  done
  [ "$locked" = true ] || return 1

  # Step 1: re-read-back inside the lock — mismatch => abort, zero writes.
  local cur_id cur_pending
  cur_id="$(kv_get_f "$STATE_FILE" MEO_ESCALATION_ID)"
  cur_pending="$(kv_get_f "$STATE_FILE" MEO_ESCALATION_PENDING)"
  if [ "$cur_id" != "$id" ] || [ "$cur_pending" != "true" ] || [ ! -f "$rec" ] || [ ! -f "$aud" ]; then
    rmdir "$lock_dir" 2>/dev/null
    return 1
  fi

  # Steps 2-4 (Codex A1): applied to a lock-scoped temp copy only — the
  # original STATE_FILE is untouched until the single atomic replace below.
  local tmp_copy="$lock_dir/STATE.md.tmp"
  cp "$STATE_FILE" "$tmp_copy" || { rmdir "$lock_dir" 2>/dev/null; return 1; }
  local wm_retry wm_streak wm_class wm_reject
  wm_retry="$(kv_get_f "$tmp_copy" MEO_RETRY_COUNT)"; wm_retry="${wm_retry:-0}"
  wm_streak="$(kv_get_f "$tmp_copy" MEO_SAME_ERROR_CLASS_STREAK)"; wm_streak="${wm_streak:-0}"
  wm_class="$(kv_get_f "$tmp_copy" MEO_LAST_ERROR_CLASS)"
  wm_reject="$(kv_get_f "$tmp_copy" MEO_VERIFIER_REJECT_COUNT)"; wm_reject="${wm_reject:-0}"

  kv_set_f "$tmp_copy" MEO_LAST_ESCALATION_DECISION "$decision"
  kv_set_f "$tmp_copy" MEO_WM_RETRY "$wm_retry"
  kv_set_f "$tmp_copy" MEO_WM_STREAK "$wm_streak"
  kv_set_f "$tmp_copy" MEO_WM_ERROR_CLASS "$wm_class"
  kv_set_f "$tmp_copy" MEO_WM_REJECT "$wm_reject"
  kv_set_f "$tmp_copy" MEO_ESCALATION_PENDING false

  # Step 5: terminal validation on the copy, then single atomic replace.
  if [ "$(kv_get_f "$tmp_copy" MEO_LAST_ESCALATION_DECISION)" = "$decision" ] \
    && [ "$(kv_get_f "$tmp_copy" MEO_WM_RETRY)" = "$wm_retry" ] \
    && [ "$(kv_get_f "$tmp_copy" MEO_WM_STREAK)" = "$wm_streak" ] \
    && [ "$(kv_get_f "$tmp_copy" MEO_WM_ERROR_CLASS)" = "$wm_class" ] \
    && [ "$(kv_get_f "$tmp_copy" MEO_WM_REJECT)" = "$wm_reject" ] \
    && [ "$(kv_get_f "$tmp_copy" MEO_ESCALATION_PENDING)" = "false" ]; then
    mv "$tmp_copy" "$STATE_FILE"
    rmdir "$lock_dir" 2>/dev/null
    return 0
  fi

  rm -f "$tmp_copy"
  rmdir "$lock_dir" 2>/dev/null
  return 1
}

# --- AI-3 §3(i)(ii): canonical absolute project root, normalized to '/' ---
PROJECT_ROOT="${CLAUDE_PROJECT_DIR:-}"
if [ -z "$PROJECT_ROOT" ]; then
  HOOK_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd 2>/dev/null || true)"
  if [ -n "$HOOK_DIR" ]; then
    PROJECT_ROOT="$(cd "$HOOK_DIR/../../../.." 2>/dev/null && pwd || true)"
  fi
fi
PROJECT_ROOT="${PROJECT_ROOT//\\//}"
PROJECT_ROOT="${PROJECT_ROOT%/}"
[ -n "$PROJECT_ROOT" ] || emit allow "MEO fail-open: canonical project root undeterminable"

# --- AI-3 §3(iii)(iv)(v): MEO-governance-surface hit detection -----------
MEO_GOV_HIT=false
TARGET=""
if [ "$TOOL_NAME" = "Bash" ]; then
  CMD="$(printf '%s' "$INPUT" | jq -r '.tool_input.command // empty')"
  case "$CMD" in
    *STATE.md*|*gate.conf*|*.claude/skills/model-effort-orchestrator*|*.claude\\skills\\model-effort-orchestrator*|*meo/adjudications*|*meo\\adjudications*)
      MEO_GOV_HIT=true ;;
  esac
else
  TARGET="$(printf '%s' "$INPUT" | jq -r '.tool_input.file_path // .tool_input.path // empty')"
  TARGET_NORM="${TARGET//\\//}"
  case "$TARGET_NORM" in
    "$PROJECT_ROOT/STATE.md"|"$PROJECT_ROOT/gate.conf") MEO_GOV_HIT=true ;;
    "$PROJECT_ROOT/.claude/skills/model-effort-orchestrator/"*) MEO_GOV_HIT=true ;;
    "$PROJECT_ROOT/meo/adjudications/"*) MEO_GOV_HIT=true ;;
  esac
fi

# --- AI-3 §3.2/§3.4/§3.5: pending gate — MUST precede STATE bootstrap exemption
if [ "$MEO_GOV_HIT" = true ] && grep -q '^MEO_ESCALATION_PENDING=true' "$STATE_FILE" 2>/dev/null; then
  PENDING_ID="$(kv_get_f "$STATE_FILE" MEO_ESCALATION_ID)"
  if [ -n "$PENDING_ID" ] && release_pending "$PENDING_ID"; then
    emit deny "MEO: escalation released (record-before-clear complete) - pending cleared, retry this call"
  fi
  emit deny "MEO: escalation pending - new MEO governance writes require external adjudication in meo/adjudications/"
fi

# v1.2 (CSO R9): STATE.md 診斷自舉豁免 — Edit/Write/MultiEdit 目標為 STATE.md 放行
# 注意:萬用字元尾綴匹配,他處同名 STATE.md 亦豁免(MVP 已知限制);Bash 不豁免
case "$TARGET" in
  *STATE.md)
    emit allow "MEO bootstrap: diagnosis write to STATE.md exempt" ;;
esac

if grep -q 'MEO_FASTPATH=true' "$STATE_FILE" 2>/dev/null; then
  emit allow "MEO fast-path active"
fi

if grep -q '^MEO_LAST_DIAGNOSIS_TS=..*' "$STATE_FILE" 2>/dev/null; then
  emit allow "MEO diagnosis present"
fi

emit deny "MEO: high-risk tool call without diagnosis in STATE.md; run diagnosis first"
