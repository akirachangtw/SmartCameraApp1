#!/usr/bin/env bash
# RGR-P0A-01 AI-3 no-call 組：三 reason 各一例，驗 blocker_preflight() 零命中 codex.cmd。
# 用法：bash fixtures/run_no_call_tests.sh（自 fixtures/ 或 repo root 執行皆可，內部自動定位）
set -uo pipefail

FIXTURES_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RUNNER_DIR="$(cd "$FIXTURES_DIR/.." && pwd)"
REPO_ROOT="$(cd "$RUNNER_DIR/../../../.." && pwd)"
RUNNER="$RUNNER_DIR/repair_runner.sh"
TELEMETRY_ROOT="$REPO_ROOT/.tmp/rigor-gate/ticket-repair"

FAIL=0
PASS=0

run_case() {
  local name="$1" payload="$2" expect_reason="$3"
  local ticket_id source_run_id target_file report_path
  ticket_id="$(jq -r '.ticket_id' "$payload")"
  source_run_id="$(jq -r '.source_run_id' "$payload")"
  target_file="$(jq -r '.target.file_path' "$payload")"
  report_path="$(jq -r '.quality_report.report_path' "$payload")"

  local artifacts_dir="$TELEMETRY_ROOT/$ticket_id/$source_run_id"
  local sentinel report_full target_full
  sentinel="$(mktemp -u)"
  rm -rf "$artifacts_dir"
  rm -f "$REPO_ROOT/$report_path" 2>/dev/null || true

  target_full="$REPO_ROOT/$target_file"
  local before_hash=""
  [[ -f "$target_full" ]] && before_hash="$(sha256sum "$target_full" | awk '{print $1}')"

  local exit_code=0
  (
    cd "$REPO_ROOT" || exit 99
    export PATH="$FIXTURES_DIR/bin:$PATH"
    export CODEX_STUB_SENTINEL="$sentinel"
    bash "$RUNNER" terra --payload "$payload" >/tmp/rgr_p0a01_${name}.stdout 2>/tmp/rgr_p0a01_${name}.stderr
  )
  exit_code=$?

  local ok=1
  local msg=""

  if [[ "$exit_code" -ne 1 ]]; then ok=0; msg+="exit_code=$exit_code(expect 1); "; fi
  if [[ -f "$sentinel" ]]; then ok=0; msg+="codex stub 被呼叫(不應發生); "; fi
  if [[ -f "$artifacts_dir/rendered_prompt.txt" ]]; then ok=0; msg+="rendered_prompt.txt 不應存在; "; fi
  if [[ -f "$artifacts_dir/raw_response.json" ]]; then ok=0; msg+="raw_response.json 不應存在; "; fi

  if [[ -n "$before_hash" ]]; then
    local after_hash
    after_hash="$(sha256sum "$target_full" | awk '{print $1}')"
    [[ "$after_hash" == "$before_hash" ]] || { ok=0; msg+="target hash 改變; "; }
  fi

  local receipt="$artifacts_dir/blocker-receipt.v1.json"
  if [[ ! -f "$receipt" ]]; then
    ok=0; msg+="receipt 不存在; "
  else
    jq -e '.automation_boundary | (.model_invoked==false and .prompt_rendered==false and .patch_attempted==false and .target_mutated==false and .external_side_effect==false and .final_pass_claimed==false)' "$receipt" >/dev/null 2>&1 \
      || { ok=0; msg+="automation_boundary 六旗標非全 false; "; }
    jq -e --arg r "$expect_reason" '.reasons | any(.reason == $r)' "$receipt" >/dev/null 2>&1 \
      || { ok=0; msg+="reasons[] 未含預期 reason=$expect_reason; "; }
    jq -e '.reasons | all(.producer_evidence.source != "" and .producer_evidence.read_result != "" and .producer_evidence.match_result != "")' "$receipt" >/dev/null 2>&1 \
      || { ok=0; msg+="producer_evidence 三欄非全非空; "; }
  fi

  local report_full_path="$REPO_ROOT/$report_path"
  if [[ ! -f "$report_full_path" ]]; then
    ok=0; msg+="report 檔不存在; "
  else
    grep -q 'CB-4' "$report_full_path" && { ok=0; msg+="report Notes 含 CB-4 子字串(禁止); "; }
    grep -q "P0A_PRECHECK_REASON=$expect_reason" "$report_full_path" || { ok=0; msg+="report 未含 P0A_PRECHECK_REASON=$expect_reason; "; }
    grep -q 'MODEL_INVOKED=false' "$report_full_path" || { ok=0; msg+="report 未含 MODEL_INVOKED=false; "; }
  fi

  rm -f "$sentinel"

  if [[ "$ok" -eq 1 ]]; then
    echo "✅ PASS | $name"
    PASS=$((PASS+1))
  else
    echo "❌ FAIL | $name | $msg"
    FAIL=$((FAIL+1))
  fi
}

run_case "anchor_missing" "$FIXTURES_DIR/no_call/payload_anchor_missing.json" "ANCHOR_MISSING_OR_UNREADABLE"
run_case "write_mode_forbidden" "$FIXTURES_DIR/no_call/payload_write_mode_forbidden.json" "WRITE_MODE_FORBIDDEN"
run_case "irreversible_target" "$FIXTURES_DIR/no_call/payload_irreversible_target.json" "IRREVERSIBLE_TARGET_DENIED"

# allow_scope_expansion=true 且 allow_decision_making=true 不得作放行權（AC 3）
run_case "write_mode_forbidden_flags" "$FIXTURES_DIR/no_call/payload_write_mode_forbidden_flags.json" "WRITE_MODE_FORBIDDEN"

# no-call 停止後再次對同 finding 執行，CB-4 不得因前次 preflight 記錄而誤凍結（AC 4）
echo "--- rerun (CB-4 latch 檢查) ---"
payload="$FIXTURES_DIR/no_call/payload_anchor_missing.json"
(
  cd "$REPO_ROOT" || exit 99
  export PATH="$FIXTURES_DIR/bin:$PATH"
  export CODEX_STUB_SENTINEL="$(mktemp -u)"
  bash "$RUNNER" terra --payload "$payload" >/tmp/rgr_p0a01_rerun.stdout 2>/tmp/rgr_p0a01_rerun.stderr
)
rerun_exit=$?
if [[ "$rerun_exit" -eq 1 ]] && ! grep -qi 'CB-4 tripped' /tmp/rgr_p0a01_rerun.stderr; then
  echo "✅ PASS | cb4_not_latched_on_rerun"
  PASS=$((PASS+1))
else
  echo "❌ FAIL | cb4_not_latched_on_rerun | exit=$rerun_exit stderr=$(cat /tmp/rgr_p0a01_rerun.stderr)"
  FAIL=$((FAIL+1))
fi

echo "--- rgr-auto matrix (RGR-AUTO-01 AI-4, single_attachment_live_lane, real Codex call=0) ---"
if bash "$FIXTURES_DIR/rgr-auto/run_rgr_auto_matrix.sh"; then
  echo "✅ PASS | rgr_auto_matrix"
  PASS=$((PASS+1))
else
  echo "❌ FAIL | rgr_auto_matrix | see output above"
  FAIL=$((FAIL+1))
fi

echo "----"
echo "PASS=$PASS FAIL=$FAIL"
[[ "$FAIL" -eq 0 ]]
