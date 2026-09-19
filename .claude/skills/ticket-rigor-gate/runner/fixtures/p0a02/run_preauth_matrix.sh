#!/usr/bin/env bash
# RGR-P0A-02 AI-3: deterministic fixture matrix for the preauthorized_autonomous_apply
# override consumed at RTR-012 (AI-2, commit b172441 + fix commit thereafter).
#
# Drives the ACTUAL repair_runner.sh main loop via a deterministic codex.cmd stub
# (fixtures/p0a02/bin/, isolated from the protected no-call stub in fixtures/bin/).
# Zero real Codex invocations. All targets are fixture-only.
#
# Usage: bash fixtures/p0a02/run_preauth_matrix.sh (from fixtures/p0a02/ or repo root)
set -uo pipefail

P0A02_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RUNNER_DIR="$(cd "$P0A02_DIR/../.." && pwd)"
REPO_ROOT="$(cd "$RUNNER_DIR/../../../.." && pwd)"
RUNNER="$RUNNER_DIR/repair_runner.sh"
TELEMETRY_ROOT="$REPO_ROOT/.tmp/rigor-gate/ticket-repair"

PAYLOADS="$P0A02_DIR/payloads"
RESPONSES="$P0A02_DIR/responses"
REPORTS="$P0A02_DIR/reports"

PASS=0
FAIL=0

sha_of() {
  if [[ -f "$1" ]]; then sha256sum "$1" | awk '{print $1}'; else echo "__ABSENT__"; fi
}

# Runs one case against the real repair_runner.sh main loop using the
# deterministic stub. Populates LAST_* globals for the caller's assertions.
run_case() {
  local name="$1" payload="$2" response="$3"
  local ticket_id source_run_id target_file report_path
  ticket_id="$(jq -r '.ticket_id' "$payload")"
  source_run_id="$(jq -r '.source_run_id' "$payload")"
  target_file="$(jq -r '.target.file_path' "$payload")"
  report_path="$(jq -r '.quality_report.report_path' "$payload")"

  LAST_ARTIFACTS="$TELEMETRY_ROOT/$ticket_id/$source_run_id"
  LAST_REPORT="$REPO_ROOT/$report_path"
  LAST_TARGET_FULL="$REPO_ROOT/$target_file"
  LAST_BACKUP="$(mktemp -u)"

  # Fresh canonical report + fresh telemetry artifacts (per-case, not shared history).
  rm -rf "$LAST_ARTIFACTS"
  rm -f "$LAST_REPORT"

  if [[ -f "$LAST_TARGET_FULL" ]]; then
    cp -- "$LAST_TARGET_FULL" "$LAST_BACKUP"
  else
    rm -f "$LAST_BACKUP"
  fi
  LAST_BEFORE_HASH="$(sha_of "$LAST_TARGET_FULL")"

  LAST_SENTINEL="$(mktemp -u)"

  (
    cd "$REPO_ROOT" || exit 99
    export PATH="$P0A02_DIR/bin:$PATH"
    export CODEX_STUB_SENTINEL="$LAST_SENTINEL"
    export CODEX_STUB_RESPONSE_FILE="$response"
    bash "$RUNNER" terra --payload "$payload" >"/tmp/rgr_p0a02_${name}.stdout" 2>"/tmp/rgr_p0a02_${name}.stderr"
  )
  LAST_EXIT=$?
  LAST_AFTER_HASH="$(sha_of "$LAST_TARGET_FULL")"
  LAST_STUB_CALLED="no"
  [[ -f "$LAST_SENTINEL" ]] && LAST_STUB_CALLED="yes"
}

restore_and_cleanup() {
  if [[ -f "$LAST_BACKUP" ]]; then
    cp -- "$LAST_BACKUP" "$LAST_TARGET_FULL"
  else
    rm -f "$LAST_TARGET_FULL"
  fi
  rm -f "$LAST_BACKUP" "$LAST_SENTINEL"
}

pass() { echo "✅ PASS | $1"; PASS=$((PASS+1)); }
fail() { echo "❌ FAIL | $1 | $2"; FAIL=$((FAIL+1)); }

# ── Case 1: success — override hit, marker clean, apply succeeds ──
run_case "c1" "$PAYLOADS/payload_c1.json" "$RESPONSES/response_c1.json"
{
  ok=1; msg=""
  [[ "$LAST_EXIT" -eq 0 ]] || { ok=0; msg+="exit=$LAST_EXIT(expect 0); "; }
  [[ "$LAST_STUB_CALLED" == "yes" ]] || { ok=0; msg+="stub 未被呼叫; "; }
  grep -q "Apply Status: applied" "$LAST_REPORT" 2>/dev/null || { ok=0; msg+="report 無 applied; "; }
  grep -q "PREAUTHORIZED_AUTONOMOUS_APPLY=true" "$LAST_REPORT" 2>/dev/null || { ok=0; msg+="缺 PREAUTHORIZED_AUTONOMOUS_APPLY=true; "; }
  grep -q "REQUIRES_HUMAN_SELF_REPORT=true" "$LAST_REPORT" 2>/dev/null || { ok=0; msg+="缺 REQUIRES_HUMAN_SELF_REPORT=true; "; }
  grep -q "REQUIRED_VERIFICATION_REF=fixture-ref-c1" "$LAST_REPORT" 2>/dev/null || { ok=0; msg+="缺 REQUIRED_VERIFICATION_REF=fixture-ref-c1; "; }
  grep -q "HUMAN_CHECKPOINT_EMITTED=false" "$LAST_REPORT" 2>/dev/null || { ok=0; msg+="缺 HUMAN_CHECKPOINT_EMITTED=false; "; }
  grep -q "Addendum: FIXTURE_VALUE = 07 recorded for case 1." "$LAST_TARGET_FULL" 2>/dev/null || { ok=0; msg+="target 未見 addendum mutation; "; }
  [[ "$LAST_AFTER_HASH" != "$LAST_BEFORE_HASH" ]] || { ok=0; msg+="target hash 未變(預期應變); "; }
  [[ "$ok" -eq 1 ]] && pass "c1_success_applied" || fail "c1_success_applied" "$msg"
}
restore_and_cleanup

# ── Case 2: preimage mismatch — target guard rejects before RTR-012 is ever reached ──
run_case "c2" "$PAYLOADS/payload_c2.json" "$RESPONSES/response_c2.json"
{
  ok=1; msg=""
  [[ "$LAST_EXIT" -eq 1 ]] || { ok=0; msg+="exit=$LAST_EXIT(expect 1); "; }
  grep -q "Failure Code: RTR-008" "$LAST_REPORT" 2>/dev/null || { ok=0; msg+="report 未見 RTR-008; "; }
  grep -q "Apply Status: rejected" "$LAST_REPORT" 2>/dev/null || { ok=0; msg+="report 未見 rejected; "; }
  grep -q "Apply Status: applied" "$LAST_REPORT" 2>/dev/null && { ok=0; msg+="不應出現 applied; "; }
  grep -qi "decision leakage" "$LAST_REPORT" 2>/dev/null && { ok=0; msg+="不應觸及 decision-leakage 分支（preimage 應先攔）; "; }
  [[ "$LAST_AFTER_HASH" == "$LAST_BEFORE_HASH" ]] || { ok=0; msg+="target hash 改變(不應發生); "; }
  [[ "$ok" -eq 1 ]] && pass "c2_preimage_mismatch_rejected_before_rtr012" || fail "c2_preimage_mismatch_rejected_before_rtr012" "$msg"
}
restore_and_cleanup

# ── Case 3: requires_human=true, ref missing → override=false, original RTR-012 ──
run_case "c3" "$PAYLOADS/payload_c3.json" "$RESPONSES/response_c3.json"
{
  ok=1; msg=""
  [[ "$LAST_EXIT" -eq 1 ]] || { ok=0; msg+="exit=$LAST_EXIT(expect 1); "; }
  grep -q "Failure Code: RTR-012" "$LAST_REPORT" 2>/dev/null || { ok=0; msg+="report 未見 RTR-012; "; }
  grep -q "Apply Status: escalated" "$LAST_REPORT" 2>/dev/null || { ok=0; msg+="report 未見 escalated; "; }
  grep -q "Apply Status: applied" "$LAST_REPORT" 2>/dev/null && { ok=0; msg+="不應出現 applied; "; }
  grep -q "PREAUTHORIZED_AUTONOMOUS_APPLY" "$LAST_REPORT" 2>/dev/null && { ok=0; msg+="不應出現預授權 audit 欄(override 未命中); "; }
  [[ "$LAST_AFTER_HASH" == "$LAST_BEFORE_HASH" ]] || { ok=0; msg+="target hash 改變(不應發生); "; }
  [[ "$ok" -eq 1 ]] && pass "c3_ref_missing_selfreport_true_override_false" || fail "c3_ref_missing_selfreport_true_override_false" "$msg"
}
restore_and_cleanup

# ── Case 3b: requires_human=false, ref also missing → NOT rejected by RTR-012 at all ──
run_case "c3b" "$PAYLOADS/payload_c3b.json" "$RESPONSES/response_c3b.json"
{
  ok=1; msg=""
  [[ "$LAST_EXIT" -eq 0 ]] || { ok=0; msg+="exit=$LAST_EXIT(expect 0); "; }
  grep -q "Apply Status: applied" "$LAST_REPORT" 2>/dev/null || { ok=0; msg+="report 無 applied; "; }
  grep -q "Failure Code: RTR-012" "$LAST_REPORT" 2>/dev/null && { ok=0; msg+="不應出現 RTR-012（缺 ref 本身不得人為觸發）; "; }
  grep -q "PREAUTHORIZED_AUTONOMOUS_APPLY" "$LAST_REPORT" 2>/dev/null && { ok=0; msg+="不應出現預授權 audit 欄(self-report=false，override 分支未進入); "; }
  grep -q "Addendum: FIXTURE_VALUE = 07 recorded for case 3b." "$LAST_TARGET_FULL" 2>/dev/null || { ok=0; msg+="target 未見 addendum mutation; "; }
  [[ "$ok" -eq 1 ]] && pass "c3b_ref_missing_selfreport_false_not_triggered" || fail "c3b_ref_missing_selfreport_false_not_triggered" "$msg"
}
restore_and_cleanup

# ── Case 4: legacy v1 payload (no preauth fields at all) — behavior identical to pre-AI-2 ──
run_case "c4" "$PAYLOADS/payload_c4.json" "$RESPONSES/response_c4.json"
{
  ok=1; msg=""
  [[ "$LAST_EXIT" -eq 0 ]] || { ok=0; msg+="exit=$LAST_EXIT(expect 0); "; }
  grep -q "Apply Status: applied" "$LAST_REPORT" 2>/dev/null || { ok=0; msg+="report 無 applied; "; }
  grep -q "Notes: bounded addendum write only$" "$LAST_REPORT" 2>/dev/null || { ok=0; msg+="Notes 未逐字等於 'bounded addendum write only'（不得含分號附加 audit 欄）; "; }
  grep -q "PREAUTHORIZED_" "$LAST_REPORT" 2>/dev/null && { ok=0; msg+="不應出現 PREAUTHORIZED_ note; "; }
  grep -q "REQUIRED_VERIFICATION_REF" "$LAST_REPORT" 2>/dev/null && { ok=0; msg+="不應出現 REQUIRED_VERIFICATION_REF; "; }
  grep -q "HUMAN_CHECKPOINT_EMITTED" "$LAST_REPORT" 2>/dev/null && { ok=0; msg+="不應出現 HUMAN_CHECKPOINT_EMITTED; "; }
  [[ "$ok" -eq 1 ]] && pass "c4_legacy_v1_payload_regression" || fail "c4_legacy_v1_payload_regression" "$msg"
}
restore_and_cleanup

# ── Case 5: boundary fail-closed — target outside fixture root entirely ──
run_case "c5" "$PAYLOADS/payload_c5.json" "$RESPONSES/response_c5.json"
{
  ok=1; msg=""
  [[ "$LAST_EXIT" -eq 1 ]] || { ok=0; msg+="exit=$LAST_EXIT(expect 1); "; }
  grep -q "Failure Code: RTR-012" "$LAST_REPORT" 2>/dev/null || { ok=0; msg+="report 未見 RTR-012; "; }
  grep -q "Apply Status: applied" "$LAST_REPORT" 2>/dev/null && { ok=0; msg+="不應出現 applied; "; }
  grep -q "PREAUTHORIZED_AUTONOMOUS_APPLY" "$LAST_REPORT" 2>/dev/null && { ok=0; msg+="不應出現預授權 audit 欄(boundary 未過); "; }
  [[ ! -e "$LAST_TARGET_FULL" ]] || { ok=0; msg+="target 不應被建立/寫入; "; }
  [[ "$ok" -eq 1 ]] && pass "c5_boundary_outside_fixture_root_fail_closed" || fail "c5_boundary_outside_fixture_root_fail_closed" "$msg"
}
restore_and_cleanup

# ── Case 5b: sibling near-miss prefix (fixtures-old/) — boundary must still reject ──
run_case "c5b" "$PAYLOADS/payload_c5b.json" "$RESPONSES/response_c5b.json"
{
  ok=1; msg=""
  [[ "$LAST_EXIT" -eq 1 ]] || { ok=0; msg+="exit=$LAST_EXIT(expect 1); "; }
  grep -q "Failure Code: RTR-012" "$LAST_REPORT" 2>/dev/null || { ok=0; msg+="report 未見 RTR-012; "; }
  grep -q "Apply Status: applied" "$LAST_REPORT" 2>/dev/null && { ok=0; msg+="不應出現 applied; "; }
  grep -q "PREAUTHORIZED_AUTONOMOUS_APPLY" "$LAST_REPORT" 2>/dev/null && { ok=0; msg+="不應出現預授權 audit 欄(sibling prefix 應被排除); "; }
  [[ ! -e "$LAST_TARGET_FULL" ]] || { ok=0; msg+="target 不應被建立/寫入; "; }
  [[ ! -d "$RUNNER_DIR/fixtures-old" ]] || { ok=0; msg+="fixtures-old/ 不應被建立(harness 未曾 mkdir，若存在必為 runner 誤寫); "; }
  [[ "$ok" -eq 1 ]] && pass "c5b_sibling_prefix_boundary_rejected" || fail "c5b_sibling_prefix_boundary_rejected" "$msg"
}
restore_and_cleanup

# ── Case 6: override hit, marker reject — no applied, no audit fields on the failure entries ──
run_case "c6" "$PAYLOADS/payload_c6.json" "$RESPONSES/response_c6.json"
{
  ok=1; msg=""
  [[ "$LAST_EXIT" -eq 1 ]] || { ok=0; msg+="exit=$LAST_EXIT(expect 1); "; }
  grep -q "命中決策洩漏標記" "/tmp/rgr_p0a02_c6.stderr" 2>/dev/null || { ok=0; msg+="stderr 未見 marker reject 訊息; "; }
  grep -q "Failure Code: RTR-012" "$LAST_REPORT" 2>/dev/null || { ok=0; msg+="report 未見 RTR-012; "; }
  grep -q "Apply Status: escalated" "$LAST_REPORT" 2>/dev/null || { ok=0; msg+="report 未見 escalated; "; }
  grep -q "Apply Status: applied" "$LAST_REPORT" 2>/dev/null && { ok=0; msg+="不應出現 applied; "; }
  grep -q "PREAUTHORIZED_AUTONOMOUS_APPLY" "$LAST_REPORT" 2>/dev/null && { ok=0; msg+="成功 receipt 不含四欄——不應出現於 escalated/rejected 記錄中; "; }
  grep -q "REQUIRES_HUMAN_SELF_REPORT" "$LAST_REPORT" 2>/dev/null && { ok=0; msg+="不應出現 REQUIRES_HUMAN_SELF_REPORT; "; }
  grep -q "REQUIRED_VERIFICATION_REF" "$LAST_REPORT" 2>/dev/null && { ok=0; msg+="不應出現 REQUIRED_VERIFICATION_REF; "; }
  grep -q "HUMAN_CHECKPOINT_EMITTED" "$LAST_REPORT" 2>/dev/null && { ok=0; msg+="不應出現 HUMAN_CHECKPOINT_EMITTED; "; }
  [[ "$LAST_AFTER_HASH" == "$LAST_BEFORE_HASH" ]] || { ok=0; msg+="target hash 改變(不應發生); "; }
  echo "--- Case 6 full report record (for manual excerpt review) ---"
  cat "$LAST_REPORT" 2>/dev/null
  echo "--- end Case 6 report ---"
  [[ "$ok" -eq 1 ]] && pass "c6_override_hit_marker_reject_no_leak" || fail "c6_override_hit_marker_reject_no_leak" "$msg"
}
restore_and_cleanup

echo "----"
echo "PASS=$PASS FAIL=$FAIL"
[[ "$FAIL" -eq 0 ]]
