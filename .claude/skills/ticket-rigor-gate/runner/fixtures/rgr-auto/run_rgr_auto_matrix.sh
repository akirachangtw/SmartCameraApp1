#!/usr/bin/env bash
# RGR-AUTO-01 AI-4: deterministic fixture matrix for single_attachment_live_lane
# (cases 1, 2, 2b, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 — see case bundle files under
# fixtures/rgr-auto/cases/*.json). Case 6 is independently fixtured (meta.attachment_extra_file
# creates a SECOND real Pending file with matching content/hash, distinct from case 5's
# generic mismatch, per AI-4 target-value item 6: "another valid Pending ticket, hash also
# matches, != attachment target"). Case 15 (legacy payload / P0A-02 no-regression) is covered
# by re-running fixtures/p0a02/run_preauth_matrix.sh unmodified — see the final section below.
#
# Drives the ACTUAL repair_runner.sh main loop via a deterministic codex.cmd stub
# (fixtures/rgr-auto/bin/, self-contained, mirrors fixtures/p0a02/bin/ — never invokes
# real Codex). Each case bundle JSON carries {meta, target_body, payload, response};
# this harness extracts the pieces, places the target under the real repo tree
# (Pending/ or deliver/ per the case), dynamically injects target.expected_old_text_hash
# (hash_mode: correct / missing / mismatch) and resolves the __REPO_ROOT_WINABS__
# placeholder used by the canonical-equivalence case, since the live absolute repo path
# is only known at run time. All targets are transient fixture files created and deleted
# by this script; none are committed.
#
# Usage: bash fixtures/rgr-auto/run_rgr_auto_matrix.sh (from fixtures/rgr-auto/ or repo root)
set -uo pipefail

RGR_AUTO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RUNNER_DIR="$(cd "$RGR_AUTO_DIR/../.." && pwd)"
REPO_ROOT="$(cd "$RUNNER_DIR/../../../.." && pwd)"
RUNNER="$RUNNER_DIR/repair_runner.sh"
TELEMETRY_ROOT="$REPO_ROOT/.tmp/rigor-gate/ticket-repair"
CASES_DIR="$RGR_AUTO_DIR/cases"

PASS=0
FAIL=0

pass() { echo "✅ PASS | $1"; PASS=$((PASS+1)); }
fail() { echo "❌ FAIL | $1 | $2"; FAIL=$((FAIL+1)); }

sha_of() {
  if [[ -f "$1" ]]; then echo "sha256:$(sha256sum "$1" | awk '{print $1}')"; else echo ""; fi
}

# Windows-absolute (lower-case drive) form of $REPO_ROOT, computed from git-bash's own
# /d/... posix form; used only by the c3_winabs placeholder substitution.
repo_root_winabs() {
  local p="$REPO_ROOT"
  if [[ "$p" =~ ^/([A-Za-z])/(.*)$ ]]; then
    printf '%s:/%s' "${BASH_REMATCH[1]}" "${BASH_REMATCH[2]}"
  else
    printf '%s' "$p"
  fi
}

run_one_case() {
  local case_file="$1"
  local name
  name="$(jq -r '.meta.name' "$case_file")"

  local target_rel target_full
  target_rel="$(jq -r '.payload.target.file_path' "$case_file")"
  target_full="$REPO_ROOT/$target_rel"

  local report_rel report_full artifacts_dir ticket_id source_run_id
  report_rel="$(jq -r '.payload.quality_report.report_path' "$case_file")"
  report_full="$REPO_ROOT/$report_rel"
  ticket_id="$(jq -r '.payload.ticket_id' "$case_file")"
  source_run_id="$(jq -r '.payload.source_run_id' "$case_file")"
  artifacts_dir="$TELEMETRY_ROOT/$ticket_id/$source_run_id"

  # Fresh per-case telemetry/report/target — no shared history across cases.
  rm -rf "$artifacts_dir"
  rm -f "$report_full"
  mkdir -p "$(dirname "$target_full")"
  [[ -f "$target_full" ]] && rm -f "$target_full"
  # 直接 pipe 寫入（不經 $(...) capture）：command substitution 會吃掉尾隨換行，
  # 且此環境之 jq.exe 對 stdout 做 text-mode LF→CRLF 轉換；tr -d '\r' 還原純 LF，
  # 確保落地內容與 JSON 字面 target_body 之 \n 語意一致（c14 之預算邊界計算依賴此精確性）。
  jq -r '.target_body' "$case_file" | tr -d '\r' > "$target_full"
  local before_hash
  before_hash="$(sha_of "$target_full")"

  # Optional second real file (case 6 only): a distinct, also-real Pending ticket that
  # the attachment_target points at, separate from payload.target.file_path — exercises
  # "another valid Pending ticket, hash also matches, != attachment target" (AI-4 item 6).
  local attach_extra_rel attach_extra_full=""
  attach_extra_rel="$(jq -r '.meta.attachment_extra_file // ""' "$case_file")"
  if [[ -n "$attach_extra_rel" ]]; then
    attach_extra_full="$REPO_ROOT/$attach_extra_rel"
    mkdir -p "$(dirname "$attach_extra_full")"
    jq -r '.meta.attachment_extra_body // .target_body' "$case_file" | tr -d '\r' > "$attach_extra_full"
  fi

  # Build a run-scoped payload copy so hash injection / placeholder substitution never
  # touches the committed case bundle file.
  local payload_tmp response_tmp
  payload_tmp="$(mktemp)"
  response_tmp="$(mktemp)"
  jq '.payload' "$case_file" > "$payload_tmp"
  jq '.response' "$case_file" > "$response_tmp"

  local hash_mode
  hash_mode="$(jq -r '.meta.hash_mode // "correct"' "$case_file")"
  case "$hash_mode" in
    correct)
      local h; h="$(sha_of "$target_full")"
      jq --arg h "$h" '.target.expected_old_text_hash = $h' "$payload_tmp" > "${payload_tmp}.2" && mv "${payload_tmp}.2" "$payload_tmp"
      jq --arg h "$h" '.finding_results[0].patch.expected_old_text_hash = $h' "$response_tmp" > "${response_tmp}.2" && mv "${response_tmp}.2" "$response_tmp"
      ;;
    missing)
      jq 'del(.target.expected_old_text_hash)' "$payload_tmp" > "${payload_tmp}.2" && mv "${payload_tmp}.2" "$payload_tmp"
      ;;
    mismatch)
      jq '.target.expected_old_text_hash = "sha256:0000000000000000000000000000000000000000000000000000000000000000"' "$payload_tmp" > "${payload_tmp}.2" && mv "${payload_tmp}.2" "$payload_tmp"
      ;;
  esac

  if [[ "$(jq -r '.meta.resolved_needs_repo_root_winabs // false' "$case_file")" == "true" ]]; then
    local winabs; winabs="$(repo_root_winabs)"
    jq --arg r "$winabs" '.attachment_target.resolved |= sub("__REPO_ROOT_WINABS__"; $r)' "$payload_tmp" > "${payload_tmp}.2" && mv "${payload_tmp}.2" "$payload_tmp"
  fi

  local sentinel
  sentinel="$(mktemp -u)"

  local exit_code=0
  (
    cd "$REPO_ROOT" || exit 99
    export PATH="$RGR_AUTO_DIR/bin:$PATH"
    export CODEX_STUB_SENTINEL="$sentinel"
    export CODEX_STUB_RESPONSE_FILE="$response_tmp"
    bash "$RUNNER" terra --payload "$payload_tmp" >"/tmp/rgr_auto_${name}.stdout" 2>"/tmp/rgr_auto_${name}.stderr"
  )
  exit_code=$?
  local after_hash
  after_hash="$(sha_of "$target_full")"
  local stub_called="no"
  [[ -f "$sentinel" ]] && stub_called="yes"

  local ok=1 msg=""
  local expect_exit expect_applied expect_substr expect_no_substr
  expect_exit="$(jq -r '.meta.expect_exit' "$case_file")"
  expect_applied="$(jq -r '.meta.expect_applied' "$case_file")"
  expect_substr="$(jq -r '.meta.expect_report_substr // ""' "$case_file")"
  expect_no_substr="$(jq -r '.meta.expect_no_substr // ""' "$case_file")"

  [[ "$exit_code" -eq "$expect_exit" ]] || { ok=0; msg+="exit=$exit_code(expect $expect_exit); "; }
  [[ "$stub_called" == "yes" ]] || { ok=0; msg+="codex stub 未被呼叫（真實 Codex call 必為 0，但 stub 本身應被呼叫一次）; "; }

  if [[ "$expect_applied" == "true" ]]; then
    grep -q "Apply Status: applied" "$report_full" 2>/dev/null || { ok=0; msg+="report 無 applied; "; }
    [[ "$after_hash" != "$before_hash" ]] || { ok=0; msg+="target hash 未變(預期應變); "; }
  else
    grep -q "Apply Status: applied" "$report_full" 2>/dev/null && { ok=0; msg+="不應出現 applied; "; }
    [[ "$after_hash" == "$before_hash" ]] || { ok=0; msg+="target hash 改變(不應發生，零 mutation 要求); "; }
  fi

  if [[ -n "$expect_substr" ]]; then
    grep -q -- "$expect_substr" "$report_full" 2>/dev/null || { ok=0; msg+="report 未見 '$expect_substr'; "; }
  fi
  if [[ -n "$expect_no_substr" ]]; then
    grep -q -- "$expect_no_substr" "$report_full" 2>/dev/null && { ok=0; msg+="report 不應出現 '$expect_no_substr'; "; }
  fi

  if [[ "$(jq -r '.meta.expect_rollback // false' "$case_file")" == "true" ]]; then
    [[ "$after_hash" == "$before_hash" ]] || { ok=0; msg+="rollback 後 target hash 應等於前像(不等); "; }
  fi

  rm -f "$sentinel" "$payload_tmp" "$response_tmp"
  rm -f "$target_full"
  [[ -n "$attach_extra_full" ]] && rm -f "$attach_extra_full"
  rm -f "$report_full"
  rm -rf "$artifacts_dir"

  [[ "$ok" -eq 1 ]] && pass "$name" || fail "$name" "$msg"
}

for f in "$CASES_DIR"/*.json; do
  run_one_case "$f"
done

echo "--- P0A-02 legacy regression (case 15, unmodified existing matrix, zero files changed) ---"
if bash "$RUNNER_DIR/fixtures/p0a02/run_preauth_matrix.sh"; then
  pass "c15_p0a02_legacy_regression"
else
  fail "c15_p0a02_legacy_regression" "p0a02 既有矩陣未全數 PASS"
fi

echo "----"
echo "PASS=$PASS FAIL=$FAIL"
[[ "$FAIL" -eq 0 ]]
