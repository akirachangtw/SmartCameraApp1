#!/usr/bin/env bash
# test_preflight_adapter.sh — AI-5.1 integration harness (adapter/I-O 層，非決策語義測試)
#
# 用法: bash .claude/skills/qa/tests/test_preflight_adapter.sh
#
# 驗證 R-7 core availability fail-closed 之四種觸發情境，逐一斷言：
#   ① active.json mtime 執行前後不變（MUST NOT 呼叫 P3 writer）
#   ② exit code = 1（R-5 之明示例外）
#   ③ stderr 含 ADVISORY_ONLY 字面
#   ④ stdout 含 key CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH（每情境一次，AI-6 收尾新增）
#   ⑤ stdout 含 key ADVISORY_ONLY=true（每情境一次，AI-6 收尾新增）
# 四情境：①python3 不可達 ②preflight_core.py 暫時移離 ③core 輸出截斷／缺欄 ④四欄各自不符
#
# 本檔屬 adapter／I-O integration 測試（AI-0 動作 D 分類），不映射至 preflight_rules.json
# rule row（該表僅收「決策語義測試」，見票面 AI-2）。

set -u
REPO_ROOT="$(git rev-parse --show-toplevel)"
cd "$REPO_ROOT" || exit 2

SCRIPT=".claude/skills/qa/scripts/preflight_activation.sh"
CORE_PY=".claude/skills/qa/scripts/preflight_core.py"
ACTIVE_JSON="tmp/verify/active.json"
TICKET_ID="INFRA-PREFLIGHT-CORE-01"

PASS=0
FAIL=0

_mtime() { stat -c '%Y' "$ACTIVE_JSON" 2>/dev/null || echo "MISSING"; }

_assert() { # _assert <desc> <ok 0|1>
  local desc="$1" ok="$2"
  if [[ "$ok" -eq 0 ]]; then
    PASS=$((PASS + 1)); echo "  ✅ $desc"
  else
    FAIL=$((FAIL + 1)); echo "  ❌ $desc"
  fi
}

_check_case() { # _check_case <desc> <before_mtime> <after_mtime> <exit_code> <stderr_output>
  local desc="$1" before="$2" after="$3" exit_code="$4" stderr_out="$5"
  echo "── ${desc} ──"
  _assert "mtime unchanged (before=${before} after=${after})" "$([[ "$before" == "$after" ]] && echo 0 || echo 1)"
  _assert "exit code = 1 (got ${exit_code})" "$([[ "$exit_code" -eq 1 ]] && echo 0 || echo 1)"
  _assert "stderr contains ADVISORY_ONLY" "$(printf '%s' "$stderr_out" | grep -q "ADVISORY_ONLY" && echo 0 || echo 1)"
}

mkdir -p tmp/verify
# 確保基線 active.json 存在，取得穩定 before/after 比較點
bash "$SCRIPT" "$TICKET_ID" >/dev/null 2>&1 || true

echo "═══ Scenario ①: python3 unreachable (PATH filtered) ═══"
_ORIG_PATH="$PATH"
_FILTERED_PATH=""
IFS=':' read -ra _DIRS <<< "$PATH"
for d in "${_DIRS[@]}"; do
  [[ -e "${d}/python3" || -e "${d}/python3.exe" ]] && continue
  _FILTERED_PATH="${_FILTERED_PATH:+${_FILTERED_PATH}:}${d}"
done
before1="$(_mtime)"
out1="$(PATH="$_FILTERED_PATH" bash "$SCRIPT" "$TICKET_ID" 2>&1 1>/dev/null)"
exit1=$?
stdout1="$(PATH="$_FILTERED_PATH" bash "$SCRIPT" "$TICKET_ID" 2>/dev/null)"
after1="$(_mtime)"
PATH="$_ORIG_PATH"
_check_case "① python3 unreachable" "$before1" "$after1" "$exit1" "$out1"
echo "$out1" | grep -q "CORE_UNAVAILABLE" && _assert "reason_code=CORE_UNAVAILABLE" 0 || _assert "reason_code=CORE_UNAVAILABLE" 1
echo "$stdout1" | grep -q "^CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH$" && _assert "stdout key CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH" 0 || _assert "stdout key CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH" 1
echo "$stdout1" | grep -q "^ADVISORY_ONLY=true$" && _assert "stdout key ADVISORY_ONLY=true" 0 || _assert "stdout key ADVISORY_ONLY=true" 1

echo "═══ Scenario ②: preflight_core.py temporarily moved away ═══"
mv "$CORE_PY" "${CORE_PY}.movetest"
before2="$(_mtime)"
out2="$(bash "$SCRIPT" "$TICKET_ID" 2>&1 1>/dev/null)"
exit2=$?
stdout2="$(bash "$SCRIPT" "$TICKET_ID" 2>/dev/null)"
after2="$(_mtime)"
mv "${CORE_PY}.movetest" "$CORE_PY"
_check_case "② preflight_core.py missing" "$before2" "$after2" "$exit2" "$out2"
echo "$out2" | grep -q "CORE_UNAVAILABLE" && _assert "reason_code=CORE_UNAVAILABLE" 0 || _assert "reason_code=CORE_UNAVAILABLE" 1
echo "$stdout2" | grep -q "^CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH$" && _assert "stdout key CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH" 0 || _assert "stdout key CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH" 1
echo "$stdout2" | grep -q "^ADVISORY_ONLY=true$" && _assert "stdout key ADVISORY_ONLY=true" 0 || _assert "stdout key ADVISORY_ONLY=true" 1

echo "═══ Scenario ③: core output truncated / missing fields ═══"
cp "$CORE_PY" "${CORE_PY}.origbak"
cat > "$CORE_PY" << 'STUB_EOF'
import sys
print('{"ticket_id": "INFRA-PREFLIGHT-CORE-01"}')
sys.exit(0)
STUB_EOF
before3="$(_mtime)"
out3="$(bash "$SCRIPT" "$TICKET_ID" 2>&1 1>/dev/null)"
exit3=$?
stdout3="$(bash "$SCRIPT" "$TICKET_ID" 2>/dev/null)"
after3="$(_mtime)"
mv "${CORE_PY}.origbak" "$CORE_PY"
_check_case "③ core output truncated/missing fields" "$before3" "$after3" "$exit3" "$out3"
echo "$out3" | grep -q "CORE_PROTOCOL_INVALID" && _assert "reason_code=CORE_PROTOCOL_INVALID" 0 || _assert "reason_code=CORE_PROTOCOL_INVALID" 1
echo "$stdout3" | grep -q "^CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH$" && _assert "stdout key CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH" 0 || _assert "stdout key CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH" 1
echo "$stdout3" | grep -q "^ADVISORY_ONLY=true$" && _assert "stdout key ADVISORY_ONLY=true" 0 || _assert "stdout key ADVISORY_ONLY=true" 1

echo "═══ Scenario ④: four identity columns each mismatched ═══"
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
  before4="$(_mtime)"
  out4="$(bash "$SCRIPT" "$TICKET_ID" 2>&1 1>/dev/null)"
  exit4=$?
  after4="$(_mtime)"
  _check_case "④ identity mismatch: ${field}" "$before4" "$after4" "$exit4" "$out4"
  echo "$out4" | grep -q "IDENTITY_MISMATCH" && _assert "reason_code=IDENTITY_MISMATCH (${field})" 0 || _assert "reason_code=IDENTITY_MISMATCH (${field})" 1
  if [[ "$field" == "ticket_id" ]]; then
    # 情境層級斷言（非逐欄），與①②③一致每情境加 2 條，非乘以四欄
    stdout4="$(bash "$SCRIPT" "$TICKET_ID" 2>/dev/null)"
    echo "$stdout4" | grep -q "^CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH$" && _assert "stdout key CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH" 0 || _assert "stdout key CORE_FINAL_STATUS=BLOCKED_ENV_MISMATCH" 1
    echo "$stdout4" | grep -q "^ADVISORY_ONLY=true$" && _assert "stdout key ADVISORY_ONLY=true" 0 || _assert "stdout key ADVISORY_ONLY=true" 1
  fi
done
mv "${CORE_PY}.origbak" "$CORE_PY"

echo ""
echo "── test_preflight_adapter.sh: ${PASS} passed, ${FAIL} failed ──"
[[ "$FAIL" -eq 0 ]] && exit 0 || exit 1
