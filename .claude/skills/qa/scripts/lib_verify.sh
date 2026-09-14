#!/usr/bin/env bash
# lib_verify.sh v1.0 — QA Shared Verify Library
# Usage: source .claude/skills/qa/scripts/lib_verify.sh
#
# Provides: load_verify_policy, get_ticket_id, escalate_verdict, emit_check, archive_report,
#           read_budget, update_budget, budget_summary, display_verdict
# Verdict order (severity ascending): PASS < ADVISORY < REPORT_FIX < BLOCKER

# ─── Verdict State (shared across sourcing script) ───
_CURRENT_VERDICT="PASS"
_CURRENT_VERDICT_LEVEL=0

# ─── Budget State ───
_BUDGET_STATE_FILE="tmp/.fail_budget_state"

# ─── load_verify_policy ───
# Reads .claude/verify-policy.env; uses built-in defaults when file is absent.
# Exports: PROGRESS_WARN PROGRESS_FAIL CLAUDE_MD_WARN CLAUDE_MD_FAIL MAX_BLOCKER_BUDGET
load_verify_policy() {
  PROGRESS_WARN=60
  PROGRESS_FAIL=80
  CLAUDE_MD_WARN=150
  CLAUDE_MD_FAIL=200
  MAX_BLOCKER_BUDGET=3

  local _POLICY=".claude/verify-policy.env"
  if [[ ! -f "$_POLICY" ]]; then
    return 0
  fi
  local _v
  _v="$(grep "^PROGRESS_WARN=" "$_POLICY" 2>/dev/null | head -1 | cut -d'=' -f2- || true)"
  if [[ -n "$_v" ]]; then PROGRESS_WARN="$_v"; fi
  _v="$(grep "^PROGRESS_FAIL=" "$_POLICY" 2>/dev/null | head -1 | cut -d'=' -f2- || true)"
  if [[ -n "$_v" ]]; then PROGRESS_FAIL="$_v"; fi
  _v="$(grep "^CLAUDE_MD_WARN=" "$_POLICY" 2>/dev/null | head -1 | cut -d'=' -f2- || true)"
  if [[ -n "$_v" ]]; then CLAUDE_MD_WARN="$_v"; fi
  _v="$(grep "^CLAUDE_MD_FAIL=" "$_POLICY" 2>/dev/null | head -1 | cut -d'=' -f2- || true)"
  if [[ -n "$_v" ]]; then CLAUDE_MD_FAIL="$_v"; fi
  _v="$(grep "^MAX_BLOCKER_BUDGET=" "$_POLICY" 2>/dev/null | head -1 | cut -d'=' -f2- || true)"
  if [[ -n "$_v" ]]; then MAX_BLOCKER_BUDGET="$_v"; fi
}

# ─── get_ticket_id ───
# Returns TICKET_ID from tmp/verify_cmd.conf, or empty string if not found.
get_ticket_id() {
  local _id=""
  if [[ -f "tmp/verify_cmd.conf" ]]; then
    _id="$(grep "^TICKET_ID=" "tmp/verify_cmd.conf" 2>/dev/null | head -1 | cut -d'=' -f2- || true)"
  fi
  echo "$_id"
}

# ─── escalate_verdict ───
# Escalates global verdict if new verdict has higher severity.
# Verdict levels: PASS=0, ADVISORY=1, REPORT_FIX=2, BLOCKER=3
escalate_verdict() {
  local _new="$1"
  local _new_level
  case "$_new" in
    PASS)       _new_level=0 ;;
    ADVISORY)   _new_level=1 ;;
    REPORT_FIX|REPORTFIX) _new_level=2 ;;
    BLOCKER)    _new_level=3 ;;
    *)          return 0 ;;
  esac
  if [[ $_new_level -gt $_CURRENT_VERDICT_LEVEL ]]; then
    if [[ "$_new" == "REPORTFIX" ]]; then
      _CURRENT_VERDICT="REPORT_FIX"
    else
      _CURRENT_VERDICT="$_new"
    fi
    _CURRENT_VERDICT_LEVEL=$_new_level
  fi
}

# ─── emit_check ───
# Output a labelled check result line.
# Usage: emit_check "✅" "PASS" "message text"
emit_check() {
  local _emoji="$1"
  local _verdict="$2"
  local _msg="$3"
  echo "${_emoji} [${_verdict}] ${_msg}"
}

# ─── read_budget ───
# Returns the current structural BLOCKER count for the local fail budget.
read_budget() {
  if [[ -f "$_BUDGET_STATE_FILE" ]]; then
    cat "$_BUDGET_STATE_FILE"
  else
    echo "0"
  fi
}

# ─── update_budget ───
# BLOCKER increments the budget counter; PASS resets it. REPORT_FIX and ADVISORY do not consume budget.
update_budget() {
  local _verdict="$1"
  local _count
  _count="$(read_budget)"
  mkdir -p "tmp"
  if [[ "$_verdict" == "BLOCKER" ]]; then
    _count=$((_count + 1))
  elif [[ "$_verdict" == "PASS" ]]; then
    _count=0
  fi
  echo "$_count" > "$_BUDGET_STATE_FILE"
}

# ─── budget_summary ───
# Emits "<remaining>/<max> remaining" using MAX_BLOCKER_BUDGET from load_verify_policy.
budget_summary() {
  local _count _remaining
  _count="$(read_budget)"
  _remaining=$((MAX_BLOCKER_BUDGET - _count))
  if [[ $_remaining -lt 0 ]]; then
    _remaining=0
  fi
  echo "${_remaining}/${MAX_BLOCKER_BUDGET} remaining"
}

# ─── display_verdict ───
# Presents REPORT_FIX as REPORTFIX for compatibility with the original fail-budget plugin wording.
display_verdict() {
  local _v="${1:-$_CURRENT_VERDICT}"
  echo "${_v/REPORT_FIX/REPORTFIX}"
}

# ─── archive_report ───
# Copy sv_report to .tmp/sv_report_YYYYMMDD_HHMMSS.md, keeping only the newest 5 snapshots.
# Returns 2 on failure so caller can propagate to fail_env.
archive_report() {
  local _src="$1"
  local _ts
  local _keep=5
  local _archives=()
  local _i
  local _removed=0
  _ts="$(date '+%Y%m%d_%H%M%S')"
  if [[ ! -d ".tmp" ]]; then
    mkdir -p ".tmp" || { echo "❌ ENV ERROR: Cannot create .tmp/ directory" >&2; return 2; }
  fi
  mapfile -t _archives < <(ls -1t .tmp/sv_report_*.md 2>/dev/null || true)
  for ((_i = _keep - 1; _i < ${#_archives[@]}; _i++)); do
    rm -f -- "${_archives[$_i]}" \
      || { echo "ENV ERROR: Cannot prune old sv_report archive: ${_archives[$_i]}" >&2; return 2; }
    ((_removed += 1))
  done
  cp "$_src" ".tmp/sv_report_${_ts}.md" \
    || { echo "❌ ENV ERROR: Cannot archive sv_report to .tmp/" >&2; return 2; }
  echo "📁 Archived: .tmp/sv_report_${_ts}.md"
}
