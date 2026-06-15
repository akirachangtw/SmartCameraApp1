#!/usr/bin/env bash
# lib_verify.sh v1.0 — QA Shared Verify Library
# Usage: source .claude/skills/qa/scripts/lib_verify.sh
#
# Provides: load_verify_policy, get_ticket_id, escalate_verdict, emit_check, archive_report
# Verdict order (severity ascending): PASS < ADVISORY < REPORT_FIX < BLOCKER

# ─── Verdict State (shared across sourcing script) ───
_CURRENT_VERDICT="PASS"
_CURRENT_VERDICT_LEVEL=0

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
    REPORT_FIX) _new_level=2 ;;
    BLOCKER)    _new_level=3 ;;
    *)          return 0 ;;
  esac
  if [[ $_new_level -gt $_CURRENT_VERDICT_LEVEL ]]; then
    _CURRENT_VERDICT="$_new"
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

# ─── archive_report ───
# Copy sv_report to tmp/sv_report_YYYYMMDD_HHMMSS.md.
# Returns 2 on failure so caller can propagate to fail_env.
archive_report() {
  local _src="$1"
  local _ts
  _ts="$(date '+%Y%m%d_%H%M%S')"
  if [[ ! -d "tmp" ]]; then
    mkdir -p "tmp" || { echo "❌ ENV ERROR: Cannot create tmp/ directory" >&2; return 2; }
  fi
  cp "$_src" "tmp/sv_report_${_ts}.md" \
    || { echo "❌ ENV ERROR: Cannot archive sv_report to tmp/" >&2; return 2; }
  echo "📁 Archived: tmp/sv_report_${_ts}.md"
}
