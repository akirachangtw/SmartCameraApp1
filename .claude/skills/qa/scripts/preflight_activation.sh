#!/usr/bin/env bash
# preflight_activation.sh — Ticket Activation Protocol V0/V1/V2 preflight
# 來源: INFRA-QA-ACTIVATION-01（P2，TPL v3.8 執行端對應物）
#
# 用法:
#   bash .claude/skills/qa/scripts/preflight_activation.sh [<ticket_id>]
#   省略 <ticket_id> 時，讀取 tmp/verify/active.json 之 ticket_id。
#
# 輸出（stdout）: 逐行 KEY=VALUE 狀態，末尾一行 V2 JSON。
# 退出碼: 0 = V0 PASS（不代表 V1/V2 亦 PASS，逐層狀態各自輸出）
#         1 = V0 BLOCKED_ENV_MISMATCH（no writes, no test execution with ticket authority, no AC claim）
#
# HC 對應（票面 Hard Constraints 逐字）:
#   1. V2 MUST NOT 寫入任何檔案（唯讀探針）。
#   2. MUST NOT 自動將 SATISFIED 判為 DONE／ZERO-DIFF-DECLARED；
#      MUST NOT 自行修改票面 BASE_COMMIT／TARGET_BLOB；
#      MUST NOT 將現行行號視為舊錨點之等價位置；
#      MUST NOT 將來源不明之現況修改歸屬本票成果。
#   3. MUST NOT 改動 verify_checklist.sh（本檔僅唯讀擷取其函數定義以複用，不修改該檔）。
#   4. active.json 寫入 MUST 複用 P3 之寫入器，禁重複實作。
#   5. 狀態枚舉 MUST 沿用 TPL v3.8 字面，禁另立同義詞。
#   6. preflight 失敗 MUST NOT 阻斷唯讀取證動作。

set -u

REPO_ROOT_HINT="$(git rev-parse --show-toplevel 2>/dev/null)"
TICKET_FILE_BASE="${REPO_ROOT_HINT}/plans/cso_briefing/project"
TICKET_REGISTRY="${TICKET_FILE_BASE}/TICKET_REGISTRY.json"
PERTICKET_DIR="tmp/verify"
ACTIVE_JSON="${PERTICKET_DIR}/active.json"
VERIFY_CHECKLIST=".claude/skills/qa/scripts/verify_checklist.sh"

json_field() { # json_field <key> <file> — 單行 "key": "value" 擷取
  grep "\"$1\"" "$2" 2>/dev/null | head -1 | sed -E "s/.*\"$1\":[[:space:]]*\"([^\"]*)\".*/\1/"
}

# ─── P3 active.json 寫入器複用（HC-4：MUST 複用，禁重複實作）───
# 動態擷取 verify_checklist.sh 之 get_conf／write_active_json 函式定義並 source，
# 不 source 整個腳本（該腳本無 sourcing guard，整檔 source 會連帶執行其 Step 0 主流程）。
activate_via_p3_writer() { # activate_via_p3_writer <ticket_id> → 呼叫複用之 write_active_json
  local ticket_id="$1"
  local fn_src
  fn_src="$(sed -n '/^get_conf()/,/^}/p;/^write_active_json()/,/^}/p' "$VERIFY_CHECKLIST" 2>/dev/null)"
  [[ -z "$fn_src" ]] && { echo "⚠️ 無法自 ${VERIFY_CHECKLIST} 擷取 write_active_json（複用失敗，不影響 V0 唯讀取證）" >&2; return 1; }
  eval "$fn_src"
  TICKET_ID="$ticket_id" REVISION="preflight" CONF_FILE="/dev/null" write_active_json
}

# ─── V0: 環境驗證 ───
# 任一不符 → STATUS=BLOCKED_ENV_MISMATCH，明示 ACTION=No writes, no test execution
# with ticket authority, no AC claim。V0 PASS 後方得呼叫 P3 之 active.json 寫入器。
v0_check() { # v0_check <ticket_id> → 全域變數 V0_STATUS／V0_REPO_ROOT／V0_BRANCH／V0_HEAD／V0_STATUS_SHORT
  local ticket_id="$1"
  V0_REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null)"
  V0_BRANCH="$(git rev-parse --abbrev-ref HEAD 2>/dev/null)"
  V0_HEAD="$(git rev-parse HEAD 2>/dev/null)"
  V0_STATUS_SHORT="$(git status --short 2>/dev/null)"
  local active_ticket_id=""
  [[ -f "$ACTIVE_JSON" ]] && active_ticket_id="$(json_field ticket_id "$ACTIVE_JSON")"

  if [[ -z "$V0_REPO_ROOT" || -z "$V0_HEAD" ]]; then
    V0_STATUS="BLOCKED_ENV_MISMATCH"
    V0_REASON="git repo root or HEAD unresolvable"
    return 1
  fi
  if [[ -z "$ticket_id" ]]; then
    V0_STATUS="BLOCKED_ENV_MISMATCH"
    V0_REASON="no ticket_id supplied and active.json missing/empty"
    return 1
  fi
  # target path scope check: ticket 之票檔本身 MUST 落於 TICKET_FILE_BASE 之下
  local ticket_file
  ticket_file="$(json_field id "$TICKET_REGISTRY")" # placeholder，實際查找見 lookup_ticket_file
  V0_ACTIVE_IDENTITY="$active_ticket_id"
  V0_STATUS="PASS"
  return 0
}

# ─── 票面欄位查找（TICKET_REGISTRY.json 唯讀擷取，不使用 registry_* 工具——preflight 為
#     本地 Bash 腳本，不具 MCP 呼叫能力）。以 jq 解析：`tickets[].notes`/`schema_notes` 等
#     欄位含字面 `{`／`}`（如 checkpoint schema 說明文字），純 awk 括號計數會被字串內容
#     破壞深度追蹤，故 MUST 用真正的 JSON 解析器，不得手刻括號匹配。───
lookup_ticket_file() { # lookup_ticket_file <ticket_id> → stdout: file 欄位值（短或長路徑）；查無或 jq 缺失 → 空字串
  local ticket_id="$1"
  command -v jq >/dev/null 2>&1 || return 1
  jq -r --arg id "$ticket_id" '.tickets[] | select(.id == $id) | .file // empty' "$TICKET_REGISTRY" 2>/dev/null | head -1
}

normalize_ticket_path() { # normalize_ticket_path <file_field> → 絕對路徑（短/長路徑正規化，
  # 呼應 ssot-governance.md「file 欄正規化前置」條文）
  local f="$1"
  case "$f" in
    plans/cso_briefing/project/*) echo "${REPO_ROOT_HINT}/${f}" ;;
    *) echo "${TICKET_FILE_BASE}/${f}" ;;
  esac
}

# ─── V1: baseline 判定（R1 範圍不相交規則 + R2 歸因）───
v1_check() { # v1_check <ticket_id> <ticket_path> <targets_csv>
  local ticket_id="$1" ticket_path="$2" targets_csv="$3"
  local base_commit target_blob
  base_commit="$(grep '^BASE_COMMIT=' "$ticket_path" 2>/dev/null | head -1 | cut -d'=' -f2- | xargs)"
  target_blob="$(grep '^TARGET_BLOB=' "$ticket_path" 2>/dev/null | head -1 | cut -d'=' -f2- | xargs)"
  V1_BASE_COMMIT="$base_commit"
  V1_TARGET_BLOB="$target_blob"

  if [[ -z "$base_commit" || "$base_commit" == "<開工時回填>" || "$base_commit" == "<開工時"* ]]; then
    V1_STATUS="BLOCKED_UNATTRIBUTED_STATE"
    V1_REASON="BASE_COMMIT 未回填或為佔位字面"
    return 0
  fi
  if ! git rev-parse --verify "$base_commit" >/dev/null 2>&1; then
    V1_STATUS="BLOCKED_UNATTRIBUTED_STATE"
    V1_REASON="BASE_COMMIT ${base_commit} 不在 git 歷史內"
    return 0
  fi

  # 正規化 targets（逗號分隔）為 git pathspec 陣列
  local IFS=','
  local -a target_paths=()
  for t in $targets_csv; do
    t="$(echo "$t" | xargs)"
    [[ -n "$t" ]] && target_paths+=("$t")
  done
  unset IFS

  local diff_names
  diff_names="$(git diff --name-only "${base_commit}..HEAD" -- "${target_paths[@]}" 2>/dev/null)"

  # ─── R1 範圍不相交規則（本票核心價值）：BASE_COMMIT..HEAD 對 targets 之 diff 為空
  #     → 視同 BASELINE_MATCHED 直進 V2，不進 rebase 流程。───
  if [[ -z "$diff_names" ]]; then
    V1_STATUS="BASELINE_MATCHED"
    V1_REASON="R1: BASE_COMMIT..HEAD 對 targets 之 diff 為空，範圍不相交，直進 V2"
    return 0
  fi

  # ─── R2 歸因：非空時清點區間內觸及 targets 之 commit，依前綴分類 ───
  local commits attributed_self=0 attributed_other="" unattributed=0
  commits="$(git log --format='%h %s' "${base_commit}..HEAD" -- "${target_paths[@]}" 2>/dev/null)"
  while IFS= read -r line; do
    [[ -z "$line" ]] && continue
    local subject="${line#* }"
    if [[ "$subject" == "[${ticket_id}]"* || "$subject" == "[${ticket_id} "* ]]; then
      attributed_self=1
    elif [[ "$subject" =~ ^\[([A-Z][A-Z0-9-]+)\] ]]; then
      attributed_other="${BASH_REMATCH[1]}"
    else
      unattributed=1
    fi
  done <<< "$commits"

  if [[ "$attributed_self" -eq 1 ]]; then
    V1_STATUS="ALREADY_EXECUTED_PENDING_CLOSURE"
    V1_REASON="區間內存在攜本票 ID 前綴之 commit"
  elif [[ -n "$attributed_other" ]]; then
    V1_STATUS="CANDIDATE_SUPERSEDED"
    V1_REASON="區間內存在攜他票 ID 前綴之 commit：${attributed_other}"
  else
    V1_STATUS="BLOCKED_UNATTRIBUTED_STATE"
    V1_REASON="區間內 targets 有異動但 commit 訊息無可辨識之票號前綴"
  fi
}

# ─── V2: 唯讀語義探針（symbol 存在性 / fixture I-O / self_test 分支覆蓋）───
# 契約檔（選填）：tmp/verify/<ticket_id>.semantic_contract.json，格式：
#   {"contracts": [{"id": "...", "type": "symbol_exists", "target": "<file>", "pattern": "<regex>"}]}
# 缺契約檔時 → overall=MISSING（非 SATISFIED，避免誤觸 CANDIDATE_SUPERSEDED 分支）。
# 全零寫入：本函式 MUST NOT 建立、修改、刪除任何檔案。
v2_probe() { # v2_probe <ticket_id>
  local ticket_id="$1"
  local contract_file="${PERTICKET_DIR}/${ticket_id}.semantic_contract.json"
  V2_PROVENANCE="UNKNOWN"

  if [[ ! -f "$contract_file" ]]; then
    V2_OVERALL="MISSING"
    V2_RESULTS="[]"
    return 0
  fi

  local results="[" first=1 all_pass=1 any_pass=0
  while IFS= read -r cid; do
    [[ -z "$cid" ]] && continue
    local type target pattern verdict
    type="$(grep -A3 "\"id\": *\"${cid}\"" "$contract_file" | grep '"type"' | head -1 | sed -E 's/.*"type":[[:space:]]*"([^"]*)".*/\1/')"
    target="$(grep -A3 "\"id\": *\"${cid}\"" "$contract_file" | grep '"target"' | head -1 | sed -E 's/.*"target":[[:space:]]*"([^"]*)".*/\1/')"
    pattern="$(grep -A3 "\"id\": *\"${cid}\"" "$contract_file" | grep '"pattern"' | head -1 | sed -E 's/.*"pattern":[[:space:]]*"([^"]*)".*/\1/')"
    verdict="FAIL"
    case "$type" in
      symbol_exists)
        [[ -f "$target" ]] && grep -qE "$pattern" "$target" 2>/dev/null && verdict="PASS"
        ;;
      file_exists)
        [[ -f "$target" ]] && verdict="PASS"
        ;;
      *)
        verdict="FAIL"
        ;;
    esac
    [[ "$verdict" == "FAIL" ]] && all_pass=0
    [[ "$verdict" == "PASS" ]] && any_pass=1
    [[ $first -eq 0 ]] && results="${results},"
    results="${results}{\"id\":\"${cid}\",\"verdict\":\"${verdict}\"}"
    first=0
  done < <(grep '"id"' "$contract_file" | sed -E 's/.*"id":[[:space:]]*"([^"]*)".*/\1/')
  results="${results}]"
  V2_RESULTS="$results"

  # V2_OVERALL 三態沿用票面 AI-3 Write Authority 條文之字面（SATISFIED／PARTIAL／MISSING），
  # 不另立同義詞：全數 PASS=SATISFIED；有 PASS 有 FAIL=PARTIAL；全數 FAIL 視同尚無可用結果=PARTIAL
  # （非 MISSING，因契約檔確實存在——MISSING 專指契約檔本身不存在）。
  if [[ "$all_pass" -eq 1 ]]; then
    V2_OVERALL="SATISFIED"
  else
    V2_OVERALL="PARTIAL"
  fi
}

# ─── 收尾狀態路由（HC-2：SATISFIED+UNKNOWN → CANDIDATE_SUPERSEDED，禁 DONE/ZERO-DIFF）───
route_v2_state() {
  if [[ "$V2_OVERALL" == "SATISFIED" && "$V2_PROVENANCE" == "UNKNOWN" ]]; then
    echo "CANDIDATE_SUPERSEDED"
  else
    echo "${V2_OVERALL}"
  fi
}

main() {
  local ticket_id="${1:-}"
  if [[ -z "$ticket_id" && -f "$ACTIVE_JSON" ]]; then
    ticket_id="$(json_field ticket_id "$ACTIVE_JSON")"
  fi

  v0_check "$ticket_id"
  echo "V0_STATUS=${V0_STATUS}"
  echo "REPO_ROOT=${V0_REPO_ROOT:-}"
  echo "BRANCH=${V0_BRANCH:-}"
  echo "HEAD=${V0_HEAD:-}"
  echo "GIT_STATUS_SHORT_LINES=$(echo "${V0_STATUS_SHORT:-}" | grep -c . || true)"
  echo "ACTIVE_TICKET_IDENTITY=${V0_ACTIVE_IDENTITY:-}"

  if [[ "$V0_STATUS" != "PASS" ]]; then
    echo "REASON=${V0_REASON}"
    echo "ACTION=No writes, no test execution with ticket authority, no AC claim"
    exit 1
  fi

  activate_via_p3_writer "$ticket_id" >&2

  local ticket_file_field ticket_path
  ticket_file_field="$(lookup_ticket_file "$ticket_id")"
  if [[ -z "$ticket_file_field" ]]; then
    echo "V1_STATUS=BLOCKED_UNATTRIBUTED_STATE"
    if ! command -v jq >/dev/null 2>&1; then
      echo "REASON=jq 不可用，無法解析 TICKET_REGISTRY.json（V0 唯讀取證不受影響）"
    else
      echo "REASON=TICKET_REGISTRY.json 查無 ${ticket_id} 之 file 欄位"
    fi
    exit 0
  fi
  ticket_path="$(normalize_ticket_path "$ticket_file_field")"
  echo "TICKET_FILE=${ticket_path}"

  local allowed_targets
  allowed_targets="$(grep '^ALLOWED=' "$ticket_path" 2>/dev/null | head -1 | cut -d'=' -f2-)"
  v1_check "$ticket_id" "$ticket_path" "$allowed_targets"
  echo "V1_STATUS=${V1_STATUS}"
  echo "V1_BASE_COMMIT=${V1_BASE_COMMIT:-}"
  echo "V1_REASON=${V1_REASON:-}"

  v2_probe "$ticket_id"
  local v2_state
  v2_state="$(route_v2_state)"
  echo "V2_STATE=${v2_state}"
  echo "V2_JSON={\"ticket_id\":\"${ticket_id}\",\"semantic_contract\":${V2_RESULTS},\"overall\":\"${V2_OVERALL}\",\"provenance\":\"${V2_PROVENANCE}\"}"

  exit 0
}

if [[ $# -ge 1 && "$1" == "--self-test" ]]; then
  echo "── preflight_activation.sh self-test ──"
  passed=0; failed=0
  _expect_eq() {
    local desc="$1" want="$2" got="$3"
    if [[ "$want" == "$got" ]]; then passed=$((passed+1)); echo "  ✅ $desc"
    else failed=$((failed+1)); echo "  ❌ $desc | want=$want got=$got"; fi
  }
  _expect_eq "route_v2_state: SATISFIED+UNKNOWN→CANDIDATE_SUPERSEDED" "CANDIDATE_SUPERSEDED" "$(V2_OVERALL=SATISFIED; V2_PROVENANCE=UNKNOWN; route_v2_state)"
  _expect_eq "route_v2_state: MISSING 直通" "MISSING" "$(V2_OVERALL=MISSING; V2_PROVENANCE=UNKNOWN; route_v2_state)"
  _expect_eq "route_v2_state: PARTIAL 直通" "PARTIAL" "$(V2_OVERALL=PARTIAL; V2_PROVENANCE=UNKNOWN; route_v2_state)"
  echo "── self-test: ${passed} passed, ${failed} failed ──"
  [[ $failed -eq 0 ]] && exit 0 || exit 1
fi

[[ "${BASH_SOURCE[0]:-$0}" == "${0}" ]] && main "$@"
