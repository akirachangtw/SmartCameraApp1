#!/usr/bin/env bash
# test_preflight_adapter.sh — R-4/R-7/R-1 adapter integration harness (adapter/I-O 層，
# 非決策語義測試；AI-0 動作 D 分類，不映射至 preflight_rules.json rule row）
#
# 用法: bash .claude/skills/qa/tests/test_preflight_adapter.sh
#
# Case ID 對照（INFRA-PREFLIGHT-CORE-01-FIX-A AI-0 基線；R-7/R-4 既有 case ID 全數保留）：
#   R7-1  python3 不可達（PATH 遮蔽受測腳本，harness 自身經 HOST_PYTHON3 絕對路徑保有量測能力）
#   R7-2  preflight_core.py 暫時移離
#   R7-3  core 輸出截斷／缺欄（stub：整份 JSON 極簡，六舊欄缺漏）
#   R4-1..R4-4  四欄 identity 各自不符（ticket_id／repo_root／worktree_path／head_sha）
#   R7-4  protocol required-field 單欄缺漏，全 9 欄逐一覆蓋（子案清單自 preflight_activation.sh
#         之 CORE_REQUIRED_FIELDS 陣列宣告直接萃取，非手寫第二份欄位集合；FIX-A AI-2 新增，
#         每子案其餘欄位含 identity 四欄皆合法）
#   MTIME-1  filesystem mtime_ns 解析度精度探針（FIX-A AI-3c）
#   R1-1..R1-4  blob_check 四型真實 collector stimulus（MISMATCH／TARGET_MISSING／
#               COMMAND_FAILED／INVALID_TICKET_VALUE，FIX-A AI-4，V0 PASS 前提 exit 0）
#
# 零寫入雙證據（FIX-A AI-3b）：P3 writer call sentinel（stderr 是否含 verify_checklist.sh
# write_active_json() 唯一於成功寫入後輸出之 "📌 active.json:" 字面標記，該函式為 FORBIDDEN
# 檔案，未經任何修改，純觀測既有側通道）∧ active.json 內容未變（mtime_ns 或 content hash，
# 依 MTIME-1 探針結果擇一，見 _zero_write）。

set -u
REPO_ROOT="$(git rev-parse --show-toplevel)"
cd "$REPO_ROOT" || exit 2

SCRIPT=".claude/skills/qa/scripts/preflight_activation.sh"
CORE_PY=".claude/skills/qa/scripts/preflight_core.py"
ACTIVE_JSON="tmp/verify/active.json"
TICKET_ID="INFRA-PREFLIGHT-CORE-01"
FIXA_TICKET_ID="INFRA-PREFLIGHT-CORE-01-FIX-A"
FIXA_TICKET_FILE="plans/cso_briefing/project/deliver/INFRA-PREFLIGHT-CORE-01-FIX-A_gaps.md"

# HOST_PYTHON3 MUST 於任何 PATH 遮蔽動作之前解析為絕對路徑——Scenario R7-1 會遮蔽受測
# 腳本之 PATH，harness 自身仍須具備量測能力（FIX-A AI-3a）。
HOST_PYTHON3="$(command -v python3)" || { echo "FATAL: python3 not found for harness itself" >&2; exit 2; }

PASS=0
FAIL=0

_mtime_ns() { "$HOST_PYTHON3" -c 'import os,sys
print(os.stat(sys.argv[1]).st_mtime_ns)' "$1" 2>/dev/null || echo "MISSING"; }

_content_hash() { "$HOST_PYTHON3" -c 'import hashlib,sys
print(hashlib.sha256(open(sys.argv[1],"rb").read()).hexdigest())' "$1" 2>/dev/null || echo "MISSING"; }

_assert() { # _assert <desc> <ok 0|1>
  local desc="$1" ok="$2"
  if [[ "$ok" -eq 0 ]]; then
    PASS=$((PASS + 1)); echo "  ✅ $desc"
  else
    FAIL=$((FAIL + 1)); echo "  ❌ $desc"
  fi
}

# _zero_write <before_ns> <after_ns> <before_hash> <after_hash> <stderr_output> → stdout 0|1
# 0 = 確認零寫入（sentinel 未觸發 ∧ 檔案未變）；1 = 有寫入或無法確認
_zero_write() {
  local before_ns="$1" after_ns="$2" before_hash="$3" after_hash="$4" stderr_out="$5"
  local sentinel_fired=1
  printf '%s' "$stderr_out" | grep -qF "📌 active.json:" && sentinel_fired=0
  local file_unchanged=1
  if [[ "$MTIME_RESOLUTION_OK" -eq 1 ]]; then
    [[ "$before_ns" == "$after_ns" ]] && file_unchanged=0
  else
    [[ "$before_hash" == "$after_hash" ]] && file_unchanged=0
  fi
  if [[ "$sentinel_fired" -eq 1 && "$file_unchanged" -eq 0 ]]; then
    echo 0
  else
    echo 1
  fi
}

_check_r7_case() { # _check_r7_case <desc> <before_ns> <after_ns> <before_hash> <after_hash> <exit_code> <stderr_output>
  local desc="$1" before_ns="$2" after_ns="$3" before_hash="$4" after_hash="$5" exit_code="$6" stderr_out="$7"
  echo "── ${desc} ──"
  _assert "zero-write confirmed (sentinel absent ∧ file unchanged)" "$(_zero_write "$before_ns" "$after_ns" "$before_hash" "$after_hash" "$stderr_out")"
  _assert "exit code = 1 (got ${exit_code})" "$([[ "$exit_code" -eq 1 ]] && echo 0 || echo 1)"
  _assert "stderr contains ADVISORY_ONLY" "$(printf '%s' "$stderr_out" | grep -q "ADVISORY_ONLY" && echo 0 || echo 1)"
}

mkdir -p tmp/verify
# 確保基線 active.json 存在，取得穩定 before/after 比較點
bash "$SCRIPT" "$TICKET_ID" >/dev/null 2>&1 || true

echo "═══ MTIME-1: filesystem mtime_ns resolution precision probe (AI-3c) ═══"
_PROBE_DIR="tmp/verify/.precision_probe.$$"
mkdir -p "$_PROBE_DIR"
_PROBE_FILE="${_PROBE_DIR}/probe.txt"
printf 'aaa' > "$_PROBE_FILE"
_probe_before_ns="$(_mtime_ns "$_PROBE_FILE")"
_probe_before_hash="$(_content_hash "$_PROBE_FILE")"
printf 'bbb' > "$_PROBE_FILE"
_probe_after_ns="$(_mtime_ns "$_PROBE_FILE")"
_probe_after_hash="$(_content_hash "$_PROBE_FILE")"
rm -rf "$_PROBE_DIR"

MTIME_RESOLUTION_OK=1
if [[ "$_probe_before_ns" == "$_probe_after_ns" ]]; then
  MTIME_RESOLUTION_OK=0
fi
_assert "content hash differs across same-process rapid rewrite (before=${_probe_before_hash:0:8} after=${_probe_after_hash:0:8})" "$([[ "$_probe_before_hash" != "$_probe_after_hash" ]] && echo 0 || echo 1)"
if [[ "$MTIME_RESOLUTION_OK" -eq 1 ]]; then
  _assert "mtime_ns differs across same-process rapid rewrite (before=${_probe_before_ns} after=${_probe_after_ns})" 0
else
  echo "  ⚠️ LIMITED_BY_FILESYSTEM_RESOLUTION：同 process 內同秒兩次寫入 mtime_ns 無法區分（before=${_probe_before_ns} after=${_probe_after_ns}）；本輪起以 content hash 補強零寫入判定"
  _assert "mtime_ns resolution insufficient → LIMITED_BY_FILESYSTEM_RESOLUTION (content hash fallback verified above)" 0
fi

echo "═══ R7-1: python3 unreachable (PATH filtered) ═══"
_ORIG_PATH="$PATH"
_FILTERED_PATH=""
IFS=':' read -ra _DIRS <<< "$PATH"
for d in "${_DIRS[@]}"; do
  [[ -e "${d}/python3" || -e "${d}/python3.exe" ]] && continue
  _FILTERED_PATH="${_FILTERED_PATH:+${_FILTERED_PATH}:}${d}"
done
before_ns1="$(_mtime_ns "$ACTIVE_JSON")"; before_hash1="$(_content_hash "$ACTIVE_JSON")"
out1="$(PATH="$_FILTERED_PATH" bash "$SCRIPT" "$TICKET_ID" 2>&1 1>/dev/null)"
exit1=$?
stdout1="$(PATH="$_FILTERED_PATH" bash "$SCRIPT" "$TICKET_ID" 2>/dev/null)"
after_ns1="$(_mtime_ns "$ACTIVE_JSON")"; after_hash1="$(_content_hash "$ACTIVE_JSON")"
PATH="$_ORIG_PATH"
_check_r7_case "R7-1 python3 unreachable" "$before_ns1" "$after_ns1" "$before_hash1" "$after_hash1" "$exit1" "$out1"
echo "$out1" | grep -q "CORE_UNAVAILABLE" && _assert "reason_code=CORE_UNAVAILABLE" 0 || _assert "reason_code=CORE_UNAVAILABLE" 1
echo "$stdout1" | grep -q "^CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH$" && _assert "stdout key CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH" 0 || _assert "stdout key CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH" 1
echo "$stdout1" | grep -q "^ADVISORY_ONLY=true$" && _assert "stdout key ADVISORY_ONLY=true" 0 || _assert "stdout key ADVISORY_ONLY=true" 1

echo "═══ R7-2: preflight_core.py temporarily moved away ═══"
mv "$CORE_PY" "${CORE_PY}.movetest"
before_ns2="$(_mtime_ns "$ACTIVE_JSON")"; before_hash2="$(_content_hash "$ACTIVE_JSON")"
out2="$(bash "$SCRIPT" "$TICKET_ID" 2>&1 1>/dev/null)"
exit2=$?
stdout2="$(bash "$SCRIPT" "$TICKET_ID" 2>/dev/null)"
after_ns2="$(_mtime_ns "$ACTIVE_JSON")"; after_hash2="$(_content_hash "$ACTIVE_JSON")"
mv "${CORE_PY}.movetest" "$CORE_PY"
_check_r7_case "R7-2 preflight_core.py missing" "$before_ns2" "$after_ns2" "$before_hash2" "$after_hash2" "$exit2" "$out2"
echo "$out2" | grep -q "CORE_UNAVAILABLE" && _assert "reason_code=CORE_UNAVAILABLE" 0 || _assert "reason_code=CORE_UNAVAILABLE" 1
echo "$stdout2" | grep -q "^CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH$" && _assert "stdout key CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH" 0 || _assert "stdout key CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH" 1
echo "$stdout2" | grep -q "^ADVISORY_ONLY=true$" && _assert "stdout key ADVISORY_ONLY=true" 0 || _assert "stdout key ADVISORY_ONLY=true" 1

echo "═══ R7-3: core output truncated / missing fields ═══"
cp "$CORE_PY" "${CORE_PY}.origbak"
cat > "$CORE_PY" << 'STUB_EOF'
import sys
print('{"ticket_id": "INFRA-PREFLIGHT-CORE-01"}')
sys.exit(0)
STUB_EOF
before_ns3="$(_mtime_ns "$ACTIVE_JSON")"; before_hash3="$(_content_hash "$ACTIVE_JSON")"
out3="$(bash "$SCRIPT" "$TICKET_ID" 2>&1 1>/dev/null)"
exit3=$?
stdout3="$(bash "$SCRIPT" "$TICKET_ID" 2>/dev/null)"
after_ns3="$(_mtime_ns "$ACTIVE_JSON")"; after_hash3="$(_content_hash "$ACTIVE_JSON")"
mv "${CORE_PY}.origbak" "$CORE_PY"
_check_r7_case "R7-3 core output truncated/missing fields" "$before_ns3" "$after_ns3" "$before_hash3" "$after_hash3" "$exit3" "$out3"
echo "$out3" | grep -q "CORE_PROTOCOL_INVALID" && _assert "reason_code=CORE_PROTOCOL_INVALID" 0 || _assert "reason_code=CORE_PROTOCOL_INVALID" 1
echo "$stdout3" | grep -q "^CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH$" && _assert "stdout key CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH" 0 || _assert "stdout key CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH" 1
echo "$stdout3" | grep -q "^ADVISORY_ONLY=true$" && _assert "stdout key ADVISORY_ONLY=true" 0 || _assert "stdout key ADVISORY_ONLY=true" 1

echo "═══ R4-1..R4-4: four identity columns each mismatched ═══"
cp "$CORE_PY" "${CORE_PY}.origbak"
for field in ticket_id repo_root worktree_path head_sha; do
  cat > "$CORE_PY" << STUB_EOF2
import sys, json
payload = json.loads(sys.stdin.read())
ctx = payload["context"]
d = {
    "context_id": "deadbeefdeadbeef",
    "ticket_id": ctx["ticket_id"],
    "repo_root": ctx["repo_root"],
    "worktree_path": ctx["worktree_path"],
    "head_sha": ctx["head_sha"],
    "final_status": "BASELINE_MATCHED",
    "reason_code": "TEST_STUB",
    "process_exit_code": 0,
    "write_authority": "YES",
    "may_activate_ticket": True,
    "v2_probe_may_write": False,
    "may_run_tests_with_authority": True,
    "evidence_required": "None beyond standard AC evidence",
}
d["${field}"] = "CORRUPTED_VALUE"
print(json.dumps(d))
STUB_EOF2
  before_ns4="$(_mtime_ns "$ACTIVE_JSON")"; before_hash4="$(_content_hash "$ACTIVE_JSON")"
  out4="$(bash "$SCRIPT" "$TICKET_ID" 2>&1 1>/dev/null)"
  exit4=$?
  after_ns4="$(_mtime_ns "$ACTIVE_JSON")"; after_hash4="$(_content_hash "$ACTIVE_JSON")"
  _check_r7_case "R4 identity mismatch: ${field}" "$before_ns4" "$after_ns4" "$before_hash4" "$after_hash4" "$exit4" "$out4"
  echo "$out4" | grep -q "IDENTITY_MISMATCH" && _assert "reason_code=IDENTITY_MISMATCH (${field})" 0 || _assert "reason_code=IDENTITY_MISMATCH (${field})" 1
  if [[ "$field" == "ticket_id" ]]; then
    stdout4="$(bash "$SCRIPT" "$TICKET_ID" 2>/dev/null)"
    echo "$stdout4" | grep -q "^CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH$" && _assert "stdout key CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH" 0 || _assert "stdout key CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH" 1
    echo "$stdout4" | grep -q "^ADVISORY_ONLY=true$" && _assert "stdout key ADVISORY_ONLY=true" 0 || _assert "stdout key ADVISORY_ONLY=true" 1
  fi
done
mv "${CORE_PY}.origbak" "$CORE_PY"

echo "═══ R7-4: protocol required-field single-column omission, full CORE_REQUIRED_FIELDS coverage (FIX-A AI-2) ═══"
# 子案清單 MUST 自 preflight_activation.sh 之 CORE_REQUIRED_FIELDS 陣列宣告直接萃取（非本檔手寫
# 第二份欄位集合）——涵蓋全部九欄，非僅 AI-1 之補強揭露三欄。
mapfile -t _REQUIRED_FIELDS < <(sed -n '/^readonly CORE_REQUIRED_FIELDS=(/,/^)/p' "$SCRIPT" | tail -n +2 | grep -oE 'CORE_[A-Z_]+')
_assert "CORE_REQUIRED_FIELDS extraction yields 9 fields (got ${#_REQUIRED_FIELDS[@]})" "$([[ "${#_REQUIRED_FIELDS[@]}" -eq 9 ]] && echo 0 || echo 1)"

declare -A _JSON_KEY_OF=(
  [CORE_FINAL_STATUS]=final_status
  [CORE_EXIT_CODE]=process_exit_code
  [CORE_MAY_ACTIVATE_TICKET]=may_activate_ticket
  [CORE_WRITE_AUTHORITY]=write_authority
  [CORE_TICKET_ID]=ticket_id
  [CORE_CONTEXT_ID]=context_id
  [CORE_REPO_ROOT]=repo_root
  [CORE_WORKTREE_PATH]=worktree_path
  [CORE_HEAD_SHA]=head_sha
)

cp "$CORE_PY" "${CORE_PY}.origbak"
for core_field in "${_REQUIRED_FIELDS[@]}"; do
  json_key="${_JSON_KEY_OF[$core_field]}"
  cat > "$CORE_PY" << STUB_EOF3
import sys, json
payload = json.loads(sys.stdin.read())
ctx = payload["context"]
d = {
    "context_id": "deadbeefdeadbeef",
    "ticket_id": ctx["ticket_id"],
    "repo_root": ctx["repo_root"],
    "worktree_path": ctx["worktree_path"],
    "head_sha": ctx["head_sha"],
    "final_status": "BASELINE_MATCHED",
    "reason_code": "TEST_STUB",
    "process_exit_code": 0,
    "write_authority": "YES",
    "may_activate_ticket": True,
    "v2_probe_may_write": False,
    "may_run_tests_with_authority": True,
    "evidence_required": "None beyond standard AC evidence",
}
del d["${json_key}"]
print(json.dumps(d))
STUB_EOF3
  before_ns5="$(_mtime_ns "$ACTIVE_JSON")"; before_hash5="$(_content_hash "$ACTIVE_JSON")"
  out5="$(bash "$SCRIPT" "$TICKET_ID" 2>&1 1>/dev/null)"
  exit5=$?
  stdout5="$(bash "$SCRIPT" "$TICKET_ID" 2>/dev/null)"
  after_ns5="$(_mtime_ns "$ACTIVE_JSON")"; after_hash5="$(_content_hash "$ACTIVE_JSON")"
  _check_r7_case "R7-4 protocol field omitted: ${core_field} (json:${json_key})" "$before_ns5" "$after_ns5" "$before_hash5" "$after_hash5" "$exit5" "$out5"
  echo "$out5" | grep -q "CORE_PROTOCOL_INVALID" && _assert "reason_code=CORE_PROTOCOL_INVALID (${core_field})" 0 || _assert "reason_code=CORE_PROTOCOL_INVALID (${core_field})" 1
  echo "$stdout5" | grep -q "^CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH$" && _assert "stdout key CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH (${core_field})" 0 || _assert "stdout key CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH (${core_field})" 1
  echo "$stdout5" | grep -q "^ADVISORY_ONLY=true$" && _assert "stdout key ADVISORY_ONLY=true (${core_field})" 0 || _assert "stdout key ADVISORY_ONLY=true (${core_field})" 1
done
mv "${CORE_PY}.origbak" "$CORE_PY"

echo "═══ R1-1..R1-4: blob_check four types, real collector stimulus (FIX-A AI-4; FIX-C history-invariant) ═══"
# ─── FIX-C 共同不變量前提：BASE_COMMIT..HEAD MUST 含攜本票 ID 前綴之 commit——
#     此為 R1-2..R1-4 曾穩定 FAIL（F-3 遮蔽 F-2/R-1）之重現前提本身，非可略過之環境細節。
#     此檢查獨立於 TARGET_BLOB 刺激值，對四案共通，故於迴圈外驗證一次。───
_FIXA_BASE_COMMIT="$(grep '^BASE_COMMIT=' "$FIXA_TICKET_FILE" 2>/dev/null | head -1 | cut -d'=' -f2- | xargs)"
_FIXA_SELF_TICKET_COMMITS="$(git log --oneline "${_FIXA_BASE_COMMIT}..HEAD" -- .claude/skills/qa/ 2>/dev/null | grep -cF "[${FIXA_TICKET_ID}]")"
_assert "history-invariant: BASE_COMMIT(${_FIXA_BASE_COMMIT})..HEAD contains >=1 [${FIXA_TICKET_ID}]-prefixed commit (got ${_FIXA_SELF_TICKET_COMMITS})" "$([[ "${_FIXA_SELF_TICKET_COMMITS:-0}" -ge 1 ]] && echo 0 || echo 1)"

_FIXA_BACKUP_FILE="tmp/verify/.fixa_ticket_backup.$$"
cp "$FIXA_TICKET_FILE" "$_FIXA_BACKUP_FILE"
_GIT_REAL="$(command -v git)"
_GIT_SHIM_DIR="tmp/verify/.git_shim.$$"
mkdir -p "$_GIT_SHIM_DIR"
cat > "${_GIT_SHIM_DIR}/git" << SHIM_EOF
#!/usr/bin/env bash
if [[ "\$1" == "hash-object" ]]; then
  exit 1
fi
exec "${_GIT_REAL}" "\$@"
SHIM_EOF
chmod +x "${_GIT_SHIM_DIR}/git"

_r1_case() { # _r1_case <case_id> <target_blob_line> <expect_blob_check> <use_git_shim 0|1>
  # 三層驗證：①直接呼叫既有（未修改）collect_blob_check_fact() 收集函式，斷言其對真實
  # TARGET_BLOB 刺激產生之精確 blob_check 型別（非 stub core output，係真實 collector）；
  # ②直接呼叫 v0_check()+invoke_core_decision()，斷言 core 之 final_status 確為 R-1 guard
  # （BLOCKED_SCOPE_OR_TARGET_MISMATCH／may_activate_ticket=false），非經 F-3 遮蔽而恰巧
  # 經 F-8/R-2 unlisted fallthrough 才被攔下（FIX-C 修復意圖之精確斷言，而非僅零寫入
  # 之外部可觀測代理）；③全流程黑箱執行 adapter，斷言 exit/zero-write/WRITE_AUTHORITY
  # 之可觀測外部行為。
  local case_id="$1" blob_line="$2" expect_blob="$3" use_shim="$4"
  sed -i "s|^TARGET_BLOB=.*|TARGET_BLOB=${blob_line}|" "$FIXA_TICKET_FILE"

  local got_blob
  if [[ "$use_shim" -eq 1 ]]; then
    got_blob="$(PATH="${_GIT_SHIM_DIR}:${PATH}" bash -c '
      source .claude/skills/qa/scripts/preflight_activation.sh
      collect_blob_check_fact "'"$FIXA_TICKET_FILE"'"
    ' 2>/dev/null)"
  else
    got_blob="$(bash -c '
      source .claude/skills/qa/scripts/preflight_activation.sh
      collect_blob_check_fact "'"$FIXA_TICKET_FILE"'"
    ' 2>/dev/null)"
  fi
  echo "── ${case_id}: blob_check=${expect_blob} (真實 collect_blob_check_fact() 刺激，got=${got_blob}) ──"
  _assert "${case_id}: collect_blob_check_fact()=${expect_blob}" "$([[ "$got_blob" == "$expect_blob" ]] && echo 0 || echo 1)"

  local core_probe got_final_status got_may_activate
  # 複製 main() 之 V0→V1→V2→invoke_core_decision 呼叫序（map_v1_facts／map_v2_facts
  # 依賴 V1_STATUS／V2_OVERALL／V2_PROVENANCE 全域變數，set -u 下省略 v1_check／v2_probe
  # 會直接 unbound variable 中止，非本探針之孤立呼叫可省略的步驟）。
  local _core_probe_script='
      source .claude/skills/qa/scripts/preflight_activation.sh
      v0_check "'"$FIXA_TICKET_ID"'" 1 >/dev/null 2>&1
      allowed_targets="$(grep "^ALLOWED=" "$V0_TICKET_PATH" 2>/dev/null | head -1 | cut -d"=" -f2-)"
      v1_check "'"$FIXA_TICKET_ID"'" "$V0_TICKET_PATH" "$allowed_targets" >/dev/null 2>&1
      v2_probe "'"$FIXA_TICKET_ID"'" >/dev/null 2>&1
      invoke_core_decision "'"$FIXA_TICKET_ID"'" "EXPLICIT_SWITCH" >/dev/null 2>&1
      printf "%s|%s" "${CORE_FINAL_STATUS:-}" "${CORE_MAY_ACTIVATE_TICKET:-}"
  '
  if [[ "$use_shim" -eq 1 ]]; then
    core_probe="$(PATH="${_GIT_SHIM_DIR}:${PATH}" bash -c "$_core_probe_script" 2>/dev/null)"
  else
    core_probe="$(bash -c "$_core_probe_script" 2>/dev/null)"
  fi
  got_final_status="${core_probe%%|*}"
  got_may_activate="${core_probe##*|}"
  _assert "${case_id}: core final_status=BLOCKED_SCOPE_OR_TARGET_MISMATCH via R-1 guard (got ${got_final_status})" "$([[ "$got_final_status" == "BLOCKED_SCOPE_OR_TARGET_MISMATCH" ]] && echo 0 || echo 1)"
  _assert "${case_id}: core may_activate_ticket=false (got ${got_may_activate})" "$([[ "$got_may_activate" == "false" ]] && echo 0 || echo 1)"

  local before_ns after_ns before_hash after_hash out exit_code stdout_out
  before_ns="$(_mtime_ns "$ACTIVE_JSON")"; before_hash="$(_content_hash "$ACTIVE_JSON")"
  if [[ "$use_shim" -eq 1 ]]; then
    out="$(PATH="${_GIT_SHIM_DIR}:${PATH}" bash "$SCRIPT" "$FIXA_TICKET_ID" 2>&1 1>/dev/null)"
    exit_code=$?
    stdout_out="$(PATH="${_GIT_SHIM_DIR}:${PATH}" bash "$SCRIPT" "$FIXA_TICKET_ID" 2>/dev/null)"
  else
    out="$(bash "$SCRIPT" "$FIXA_TICKET_ID" 2>&1 1>/dev/null)"
    exit_code=$?
    stdout_out="$(bash "$SCRIPT" "$FIXA_TICKET_ID" 2>/dev/null)"
  fi
  after_ns="$(_mtime_ns "$ACTIVE_JSON")"; after_hash="$(_content_hash "$ACTIVE_JSON")"
  _assert "${case_id}: exit code = 0 (V0 PASS, R-5 相容, got ${exit_code})" "$([[ "$exit_code" -eq 0 ]] && echo 0 || echo 1)"
  _assert "${case_id}: zero-write confirmed (blocked → writer not called)" "$(_zero_write "$before_ns" "$after_ns" "$before_hash" "$after_hash" "$out")"
  echo "$stdout_out" | grep -q "^WRITE_AUTHORITY=NO$" && _assert "${case_id}: WRITE_AUTHORITY=NO" 0 || _assert "${case_id}: WRITE_AUTHORITY=NO" 1
}

_r1_case "R1-1" ".claude/skills/qa/scripts/preflight_activation.sh:deadbeef" "MISMATCH" 0
_r1_case "R1-2" ".claude/skills/qa/scripts/preflight_activation_NONEXISTENT.sh:deadbeef" "TARGET_MISSING" 0
_r1_case "R1-3" ".claude/skills/qa/scripts/preflight_activation.sh:deadbeef" "COMMAND_FAILED" 1
_r1_case "R1-4" "malformed_no_colon_value" "INVALID_TICKET_VALUE" 0

cp "$_FIXA_BACKUP_FILE" "$FIXA_TICKET_FILE"
rm -rf "$_GIT_SHIM_DIR"
if diff -q "$_FIXA_BACKUP_FILE" "$FIXA_TICKET_FILE" >/dev/null 2>&1; then
  _assert "FIX-A ticket file restored byte-identical after R1-1..R1-4" 0
else
  _assert "FIX-A ticket file restored byte-identical after R1-1..R1-4" 1
fi
rm -f "$_FIXA_BACKUP_FILE"

echo ""
echo "── test_preflight_adapter.sh: ${PASS} passed, ${FAIL} failed ──"
[[ "$FAIL" -eq 0 ]] && exit 0 || exit 1
