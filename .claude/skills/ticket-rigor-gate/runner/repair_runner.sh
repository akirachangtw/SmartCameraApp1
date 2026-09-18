#!/usr/bin/env bash
# 執行 shell：bash / POSIX（契約 §3 C-7）。Windows 下以 git-bash 執行，禁裸 `codex`，一律用 `codex.cmd`。
#
# Codex Ticket Repair Runner — 契約 SSOT：
#   Skill-Advisor/deliver/ticket-rigor-gate-skill/ticket-quality-skill-codex-runner-contract.md
#
# 用法：
#   bash repair_runner.sh [sol|terra] --payload <payload.json>
#
#   第一參數（可省略，缺省＝terra）僅接受字面 sol 或 terra；其他值一律拒收非零退出，
#   且拒收發生於任何 payload 讀取／Codex 呼叫之前（Codex 呼叫次數恆為 0）。
#   --payload 為必要旗標，指向 ticket-repair-payload/v1 JSON 檔。
#
# 退出碼：
#   0 — 全部 finding 均成功寫入或明確 no_change_needed
#   1 — 至少一筆 finding 因驗證層失敗或斷路器觸發而未寫入（詳見 quality report）
#   2 — 用法／環境錯誤（含 profile 非法），Codex 呼叫次數為 0

set -euo pipefail

# ═══════════════════════════════════════
#  路徑與常數
# ═══════════════════════════════════════

RUNNER_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROFILES_FILE="$RUNNER_DIR/profiles/model-profiles.v1.json"
PAYLOAD_SCHEMA="$RUNNER_DIR/schemas/ticket-repair-payload.v1.json"
RESPONSE_SCHEMA="$RUNNER_DIR/schemas/ticket-repair-response.v1.json"
PROMPT_TEMPLATE="$RUNNER_DIR/prompts/ticket-repair-prompt.v1.txt"
HOUSEKEEPING_SCRIPT="$RUNNER_DIR/housekeeping.sh"
REPO_ROOT="$(cd "$RUNNER_DIR/../../../.." && pwd)"
TELEMETRY_ROOT="$REPO_ROOT/.tmp/rigor-gate/ticket-repair"

CODEX_BIN="codex.cmd"
CODEX_CALL_COUNT=0

declare -A RTR_RETRYABLE=(
  [RTR-001]=false [RTR-002]=false [RTR-003]=true  [RTR-004]=true  [RTR-005]=true
  [RTR-006]=false [RTR-007]=false [RTR-008]=true  [RTR-009]=false [RTR-010]=false
  [RTR-011]=false [RTR-012]=false [RTR-013]=true  [RTR-014]=true  [RTR-015]=false
)

RETRYABLE_CODES_CSV=""
for _code in "${!RTR_RETRYABLE[@]}"; do
  [[ "${RTR_RETRYABLE[$_code]}" == "true" ]] && RETRYABLE_CODES_CSV="${RETRYABLE_CODES_CSV}${_code},"
done
unset _code

FROZEN_LEAKAGE_MARKERS=("new policy" "new acceptance criteria" "reassign" "change of ownership")

# ═══════════════════════════════════════
#  用法／環境錯誤（Codex 呼叫次數恆為 0）
# ═══════════════════════════════════════

usage_error() {
  echo "❌ USAGE ERROR: $*" >&2
  echo "用法：bash repair_runner.sh [sol|terra] --payload <payload.json>" >&2
  exit 2
}

fail_env() { echo "❌ ENV ERROR: $*" >&2; exit 2; }

command -v jq >/dev/null 2>&1 || fail_env "jq not found"
command -v sha256sum >/dev/null 2>&1 || fail_env "sha256sum not found"
command -v diff >/dev/null 2>&1 || fail_env "diff not found"

# ═══════════════════════════════════════
#  引數解析（profile 首參，payload 旗標）
# ═══════════════════════════════════════

PROFILE="terra"
PAYLOAD_PATH=""

if [[ $# -gt 0 && "$1" != --* ]]; then
  PROFILE="$1"
  shift
fi

case "$PROFILE" in
  sol|terra) ;;
  *) usage_error "profile 必須是 'sol' 或 'terra'（收到：'$PROFILE'）——禁自由 model 字串" ;;
esac

while [[ $# -gt 0 ]]; do
  case "$1" in
    --payload) PAYLOAD_PATH="${2:-}"; shift 2 ;;
    *) usage_error "未知引數：$1" ;;
  esac
done

# ═══════════════════════════════════════
#  Profile 解析（單一常數來源，AI-4／AI-5 共用）
# ═══════════════════════════════════════

[[ -f "$PROFILES_FILE" ]] || fail_env "profiles 常數檔不存在：$PROFILES_FILE"

MODEL="$(jq -r --arg p "$PROFILE" '.profiles[$p].model' "$PROFILES_FILE")"
EFFORT="$(jq -r --arg p "$PROFILE" '.profiles[$p].reasoning_effort' "$PROFILES_FILE")"

[[ "$MODEL" != "null" && -n "$MODEL" ]] || fail_env "profiles 檔缺 '$PROFILE' 之 model"
[[ "$EFFORT" != "null" && -n "$EFFORT" ]] || fail_env "profiles 檔缺 '$PROFILE' 之 reasoning_effort"

echo "🔧 Command assembly | profile=$PROFILE model=$MODEL effort=$EFFORT" >&2

[[ -n "$PAYLOAD_PATH" ]] || usage_error "缺 --payload <path>"
[[ -f "$PAYLOAD_PATH" ]] || usage_error "payload 檔不存在：$PAYLOAD_PATH"

[[ "$PWD" == "$REPO_ROOT" ]] || fail_env "CWD 必須為 repo root（$REPO_ROOT），現為：$PWD"

# ═══════════════════════════════════════
#  失敗碼與 Quality Report
# ═══════════════════════════════════════

TICKET_ID=""
SOURCE_RUN_ID=""
QUALITY_REPORT_PATH=""
CURRENT_FINDING_ID=""
TARGET_MUTATED=0
CURRENT_BEFORE_FILE=""
CURRENT_BEFORE_HASH=""

_report_write_failed() {
  local finding_id="$1" msg="$2"
  if [[ "$TARGET_MUTATED" -eq 1 ]]; then
    if rollback_target "$CURRENT_BEFORE_FILE" "$CURRENT_BEFORE_HASH"; then
      echo "❌ RTR-014 (retryable=${RTR_RETRYABLE[RTR-014]}) [$finding_id]: $msg（target 已回退）" >&2
    else
      echo "❌ ROLLBACK-FAILED [$finding_id]: $msg 且 target 回退失敗，現況 hash=$(file_hash "$TARGET_FILE") 前像檔=$CURRENT_BEFORE_FILE" >&2
    fi
  else
    echo "❌ RTR-014 (retryable=${RTR_RETRYABLE[RTR-014]}) [$finding_id]: $msg" >&2
  fi
  exit 1
}

append_report_entry() {
  local finding_id="$1" disposition="$2" apply_status="$3" failure_code="$4" notes="$5"
  [[ -n "$QUALITY_REPORT_PATH" ]] || return 0
  mkdir -p "$(dirname "$QUALITY_REPORT_PATH")" || _report_write_failed "$finding_id" "quality report 父目錄建立失敗：$QUALITY_REPORT_PATH"
  {
    echo ""
    echo "## Repair Execution Record - $(date -u +%Y-%m-%dT%H:%M:%SZ)"
    echo ""
    echo "- Ticket ID: $TICKET_ID"
    echo "- Source Run ID: $SOURCE_RUN_ID"
    echo "- Finding ID: $finding_id"
    echo "- Proposal Model: $MODEL"
    echo "- Reasoning Effort: $EFFORT"
    echo "- Prompt Template Version: ticket-repair-prompt/v1"
    echo "- Disposition: $disposition"
    echo "- Apply Status: $apply_status"
    echo "- Failure Code: $failure_code"
    echo "- Ratification Status: pending"
    echo "- Notes: $notes"
  } >> "$QUALITY_REPORT_PATH" || _report_write_failed "$finding_id" "quality report 寫入失敗：$QUALITY_REPORT_PATH"
}

# 三個 CB 共用之單一 record 邊界解析（禁固定行數視窗）。
# 每筆 record 以 "## Repair Execution Record" 起始；輸出 TSV：
# ticket_id<TAB>finding_id<TAB>failure_code<TAB>apply_status<TAB>ratification_status<TAB>notes
report_records() {
  [[ -f "$QUALITY_REPORT_PATH" ]] || return 0
  awk '
    /^## Repair Execution Record/ {
      if (started) print tid "\t" fid "\t" fc "\t" as "\t" rs "\t" notes
      started=1; tid=""; fid=""; fc=""; as=""; rs=""; notes=""
      next
    }
    /^- Ticket ID: / { tid=$0; sub(/^- Ticket ID: /, "", tid) }
    /^- Finding ID: / { fid=$0; sub(/^- Finding ID: /, "", fid) }
    /^- Failure Code: / { fc=$0; sub(/^- Failure Code: /, "", fc) }
    /^- Apply Status: / { as=$0; sub(/^- Apply Status: /, "", as) }
    /^- Ratification Status: / { rs=$0; sub(/^- Ratification Status: /, "", rs) }
    /^- Notes: / { notes=$0; sub(/^- Notes: /, "", notes) }
    END { if (started) print tid "\t" fid "\t" fc "\t" as "\t" rs "\t" notes }
  ' "$QUALITY_REPORT_PATH"
}

fail_rtr() {
  local code="$1" finding_id="$2" message="$3"
  local retryable="${RTR_RETRYABLE[$code]:-false}"
  echo "❌ $code (retryable=$retryable) [$finding_id]: $message" >&2
  append_report_entry "$finding_id" "n/a" "rejected" "$code" "$message"
  RUN_HAD_FAILURE=1
}

# ═══════════════════════════════════════
#  §5.1 Payload 必填驗證（Codex 呼叫前）
# ═══════════════════════════════════════

validate_payload() {
  local p="$1"
  jq -e . "$p" >/dev/null 2>&1 || { echo "❌ RTR-001: payload 非合法 JSON：$p" >&2; exit 1; }

  local missing
  missing="$(jq -r '
    [
      (if has("schema_version") then empty else "schema_version" end),
      (if has("ticket_id") then empty else "ticket_id" end),
      (if (has("finding_ids") and (.finding_ids | length) > 0) then empty else "finding_ids" end),
      (if (.target // {} | has("file_path")) then empty else "target.file_path" end),
      (if (.target // {} | has("write_mode")) then empty else "target.write_mode" end),
      (if ((.findings // []) | all(has("finding_id"))) then empty else "findings[*].finding_id" end),
      (if ((.findings // []) | all(has("severity"))) then empty else "findings[*].severity" end),
      (if ((.findings // []) | all(has("decisionness"))) then empty else "findings[*].decisionness" end),
      (if ((.findings // []) | all(has("repair_eligibility"))) then empty else "findings[*].repair_eligibility" end),
      (if ((.evidence // []) | all(has("path"))) then empty else "evidence[*].path" end),
      (if (.constraints // {} | has("max_changed_lines")) then empty else "constraints.max_changed_lines" end),
      (if (.quality_report // {} | has("report_path")) then empty else "quality_report.report_path" end)
    ] | join(", ")
  ' "$p")"

  if [[ -n "$missing" ]]; then
    echo "❌ RTR-001 PAYLOAD_SCHEMA_INVALID: 缺必填欄位 [$missing]" >&2
    exit 1
  fi

  local schema_version
  schema_version="$(jq -r '.schema_version' "$p")"
  [[ "$schema_version" == "ticket-repair-payload/v1" ]] || { echo "❌ RTR-001: schema_version 不合法：$schema_version" >&2; exit 1; }

  local report_path report_path_abs
  report_path="$(jq -r '.quality_report.report_path' "$p")"
  report_path_abs="$(realpath -m -- "$report_path")"
  case "$report_path_abs" in
    "$TELEMETRY_ROOT"|"$TELEMETRY_ROOT"/*)
      echo "❌ RTR-001: quality_report.report_path 落於 TELEMETRY_ROOT 內，latch 不得住進可清掃區：$report_path" >&2
      exit 1
      ;;
  esac
}

# ═══════════════════════════════════════
#  §14 斷路器 CB-1~CB-4（每次 codex 呼叫前）
# ═══════════════════════════════════════

circuit_breaker_precheck() {
  [[ -f "$QUALITY_REPORT_PATH" ]] || return 0

  # CB-4: 曾有 RTR-012／escalated+CB-4 durable 紀錄 → 永久凍結直至人工解凍（完整 record 邊界，禁固定視窗）
  local cb4_tripped="false"
  while IFS=$'\t' read -r r_tid r_fid r_fc r_as r_rs r_notes; do
    [[ "$r_tid" == "$TICKET_ID" ]] || continue
    if [[ "$r_fc" == "RTR-012" ]] || { [[ "$r_as" == "escalated" ]] && [[ "$r_notes" == *"CB-4"* ]]; }; then
      cb4_tripped="true"
      break
    fi
  done < <(report_records)
  if [[ "$cb4_tripped" == "true" ]]; then
    echo "⛔ CB-4 tripped：Ticket $TICKET_ID 曾觸發 DECISION_LEAKAGE，凍結中，需 Akira 明示解凍" >&2
    append_report_entry "$CURRENT_FINDING_ID" "n/a" "escalated" "n/a" "CB-4 frozen — 曾觸發 RTR-012"
    return 1
  fi

  # CB-2: 本次 finding_id 曾被人工 rejected／reverted（讀 Ratification Status，非 Apply Status）
  local cb2_tripped="false"
  while IFS=$'\t' read -r r_tid r_fid r_fc r_as r_rs r_notes; do
    [[ "$r_fid" == "$CURRENT_FINDING_ID" ]] || continue
    if [[ "$r_rs" == "rejected" ]] || [[ "$r_rs" == "reverted" ]]; then
      cb2_tripped="true"
      break
    fi
  done < <(report_records)
  if [[ "$cb2_tripped" == "true" ]]; then
    echo "⛔ CB-2 tripped：Finding $CURRENT_FINDING_ID 曾被人工 rejected／reverted" >&2
    append_report_entry "$CURRENT_FINDING_ID" "n/a" "escalated" "n/a" "CB-2 frozen — 既往人工 rejected/reverted"
    return 1
  fi

  # CB-1: 同一 ticket_id 連續 2 次以失敗碼收場（Retryable 碼首次出現不計入，RTR_RETRYABLE 同源判定）
  local -a cb1_countable=()
  local -A cb1_seen=()
  while IFS=$'\t' read -r r_tid r_fid r_fc r_as r_rs r_notes; do
    [[ "$r_tid" == "$TICKET_ID" ]] || continue
    local countable="false"
    if [[ -n "$r_fc" && "$r_fc" != "none" ]]; then
      local is_retryable="false" is_first="false"
      [[ "$RETRYABLE_CODES_CSV" == *"${r_fc},"* ]] && is_retryable="true"
      [[ -z "${cb1_seen[$r_fc]:-}" ]] && is_first="true"
      if [[ "$is_retryable" == "true" && "$is_first" == "true" ]]; then
        countable="false"
      else
        countable="true"
      fi
      cb1_seen["$r_fc"]=1
    fi
    cb1_countable+=("$countable")
  done < <(report_records)
  local cb1_n="${#cb1_countable[@]}"
  if [[ "$cb1_n" -ge 2 && "${cb1_countable[$((cb1_n-2))]}" == "true" && "${cb1_countable[$((cb1_n-1))]}" == "true" ]]; then
    echo "⛔ CB-1 tripped：Ticket $TICKET_ID 連續 2 次以失敗碼收場" >&2
    append_report_entry "$CURRENT_FINDING_ID" "n/a" "escalated" "n/a" "CB-1 frozen — 連續 2 次失敗"
    return 1
  fi

  # CB-3: 同一 source_run_id 累計 Codex 呼叫 >= 5
  if [[ "$CODEX_CALL_COUNT" -ge 5 ]]; then
    echo "⛔ CB-3 tripped：source_run_id=$SOURCE_RUN_ID 累計呼叫已達 $CODEX_CALL_COUNT 次" >&2
    append_report_entry "$CURRENT_FINDING_ID" "n/a" "escalated" "n/a" "CB-3 frozen — 累計呼叫 >= 5"
    return 1
  fi

  return 0
}

# ═══════════════════════════════════════
#  Blocker Preflight（機械可指認 reason，Codex 呼叫前，run 級停止）
# ═══════════════════════════════════════

IRREVERSIBLE_TARGET_PATTERNS=("app/src/*" "*.gradle*" "gradle/*" ".github/*" "*migration*/*")

declare -a BP_HIT_REASON=() BP_HIT_SOURCE=() BP_HIT_READ=() BP_HIT_MATCH=()

write_blocker_receipt() {
  local receipt_path="$ARTIFACTS_DIR/blocker-receipt.v1.json"
  local reasons_ndjson="" resume_ndjson="" status="CHECKPOINT_REQUIRED" i resume

  if [[ "${#BP_HIT_REASON[@]}" -eq 1 && "${BP_HIT_REASON[0]}" == "ANCHOR_MISSING_OR_UNREADABLE" ]]; then
    status="PENDING_EVIDENCE"
  fi

  for i in "${!BP_HIT_REASON[@]}"; do
    case "${BP_HIT_REASON[$i]}" in
      ANCHOR_MISSING_OR_UNREADABLE) resume="確認 evidence[].path 存在且可讀後重跑" ;;
      WRITE_MODE_FORBIDDEN) resume="確認 target.write_mode 為 addendum_only 後重跑" ;;
      IRREVERSIBLE_TARGET_DENIED) resume="人工核准或改指非不可逆路徑後重跑" ;;
      *) resume="人工核驗後重跑" ;;
    esac
    reasons_ndjson+="$(jq -nc --arg reason "${BP_HIT_REASON[$i]}" --arg fid "$CURRENT_FINDING_ID" \
      --arg source "${BP_HIT_SOURCE[$i]}" --arg read_result "${BP_HIT_READ[$i]}" --arg match_result "${BP_HIT_MATCH[$i]}" \
      '{reason:$reason, finding_id:$fid, producer_evidence:{source:$source, read_result:$read_result, match_result:$match_result}}')"$'\n'
    resume_ndjson+="$(jq -nc --arg r "$resume" '$r')"$'\n'
  done

  jq -n \
    --arg schema_version "blocker-receipt/v1" \
    --arg ticket_id "$TICKET_ID" \
    --arg source_run_id "$SOURCE_RUN_ID" \
    --arg created_at "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
    --arg status "$status" \
    --argjson reasons "$(printf '%s' "$reasons_ndjson" | jq -sc '.')" \
    --argjson resume_condition "$(printf '%s' "$resume_ndjson" | jq -sc '.')" \
    '{
      schema_version: $schema_version,
      ticket_id: $ticket_id,
      source_run_id: $source_run_id,
      created_at: $created_at,
      status: $status,
      reasons: $reasons,
      automation_boundary: {
        model_invoked: false,
        prompt_rendered: false,
        patch_attempted: false,
        target_mutated: false,
        external_side_effect: false,
        final_pass_claimed: false
      },
      resume_condition: $resume_condition
    }' > "$receipt_path"

  for i in "${!BP_HIT_REASON[@]}"; do
    append_report_entry "$CURRENT_FINDING_ID" "n/a" "escalated" "n/a" \
      "P0A_PRECHECK_REASON=${BP_HIT_REASON[$i]}; BLOCKER_RECEIPT=blocker-receipt.v1.json; MODEL_INVOKED=false"
  done
}

blocker_preflight() {
  local payload="$1"
  BP_HIT_REASON=(); BP_HIT_SOURCE=(); BP_HIT_READ=(); BP_HIT_MATCH=()

  local ev_path
  while IFS= read -r ev_path; do
    ev_path="${ev_path%$'\r'}"
    [[ -n "$ev_path" ]] || continue
    if [[ ! -r "$ev_path" ]]; then
      BP_HIT_REASON+=("ANCHOR_MISSING_OR_UNREADABLE")
      BP_HIT_SOURCE+=("evidence[].path")
      BP_HIT_READ+=("$ev_path")
      BP_HIT_MATCH+=("not_readable_or_missing")
    fi
  done < <(jq -r '(.evidence // [])[].path' "$payload")

  local write_mode
  write_mode="$(jq -r '.target.write_mode' "$payload")"
  if [[ "$write_mode" != "addendum_only" ]]; then
    BP_HIT_REASON+=("WRITE_MODE_FORBIDDEN")
    BP_HIT_SOURCE+=("target.write_mode")
    BP_HIT_READ+=("$write_mode")
    BP_HIT_MATCH+=("expected_addendum_only")
  fi

  local target_file pattern irreversible_hit="false" matched_pattern=""
  target_file="$(jq -r '.target.file_path' "$payload")"
  for pattern in "${IRREVERSIBLE_TARGET_PATTERNS[@]}"; do
    if [[ "$target_file" == $pattern ]]; then
      irreversible_hit="true"
      matched_pattern="$pattern"
      break
    fi
  done
  if [[ "$irreversible_hit" == "true" ]]; then
    BP_HIT_REASON+=("IRREVERSIBLE_TARGET_DENIED")
    BP_HIT_SOURCE+=("target.file_path")
    BP_HIT_READ+=("$target_file")
    BP_HIT_MATCH+=("$matched_pattern")
  fi

  [[ "${#BP_HIT_REASON[@]}" -eq 0 ]] && return 0

  write_blocker_receipt
  return 1
}

# ═══════════════════════════════════════
#  Prompt 組裝（版本化模板，禁人工即席 prompt）
# ═══════════════════════════════════════

render_prompt() {
  local payload="$1" out="$2"

  local findings_block evidence_block
  findings_block="$(jq -r '.findings[] | "- finding_id: \(.finding_id)\n  class: \(.class // "")\n  severity: \(.severity)\n  decisionness: \(.decisionness)\n  repair_eligibility: \(.repair_eligibility)\n  summary: \(.summary // "")"' "$payload")"
  evidence_block="$(jq -r '.evidence[] | "- evidence_id: \(.evidence_id // "")\n  path: \(.path)\n  lines: \(.lines // "")\n  excerpt: \(.excerpt // "")\n  claim: \(.claim // "")"' "$payload")"

  local ticket_id target_file write_mode section_id old_hash max_lines frozen_terms allow_norm allow_scope allow_decision
  ticket_id="$(jq -r '.ticket_id' "$payload")"
  target_file="$(jq -r '.target.file_path' "$payload")"
  write_mode="$(jq -r '.target.write_mode' "$payload")"
  section_id="$(jq -r '.target.section_id // ""' "$payload")"
  old_hash="$(jq -r '.target.expected_old_text_hash // ""' "$payload")"
  max_lines="$(jq -r '.constraints.max_changed_lines' "$payload")"
  frozen_terms="$(jq -r '(.constraints.frozen_terms // []) | join(", ")' "$payload")"
  allow_norm="$(jq -r '.constraints.allow_normative_edit_only_if_cited_finding // false' "$payload")"
  allow_scope="$(jq -r '.constraints.allow_scope_expansion // false' "$payload")"
  allow_decision="$(jq -r '.constraints.allow_decision_making // false' "$payload")"

  awk -v findings="$findings_block" -v evidence="$evidence_block" \
      -v ticket_id="$ticket_id" -v target_file="$target_file" -v write_mode="$write_mode" \
      -v section_id="$section_id" -v old_hash="$old_hash" -v max_lines="$max_lines" \
      -v frozen_terms="$frozen_terms" -v allow_norm="$allow_norm" -v allow_scope="$allow_scope" \
      -v allow_decision="$allow_decision" '
    /{{#findings}}/ { print findings; skip_f=1; next }
    /{{\/findings}}/ { skip_f=0; next }
    skip_f { next }
    /{{#evidence}}/ { print evidence; skip_e=1; next }
    /{{\/evidence}}/ { skip_e=0; next }
    skip_e { next }
    {
      line=$0
      gsub(/{{ticket_id}}/, ticket_id, line)
      gsub(/{{target\.file_path}}/, target_file, line)
      gsub(/{{target\.write_mode}}/, write_mode, line)
      gsub(/{{target\.section_id}}/, section_id, line)
      gsub(/{{target\.expected_old_text_hash}}/, old_hash, line)
      gsub(/{{constraints\.max_changed_lines}}/, max_lines, line)
      gsub(/{{constraints\.frozen_terms}}/, frozen_terms, line)
      gsub(/{{constraints\.allow_normative_edit_only_if_cited_finding}}/, allow_norm, line)
      gsub(/{{constraints\.allow_scope_expansion}}/, allow_scope, line)
      gsub(/{{constraints\.allow_decision_making}}/, allow_decision, line)
      print line
    }
  ' "$PROMPT_TEMPLATE" > "$out"
}

# ═══════════════════════════════════════
#  §7 驗證層（response 級：§7.1／§7.2）
# ═══════════════════════════════════════

validate_response_structure() {
  local response="$1" payload="$2"
  jq -e . "$response" >/dev/null 2>&1 || { echo "❌ RTR-004: response 非合法 JSON" >&2; return 4; }

  local schema_version
  schema_version="$(jq -r '.schema_version // ""' "$response")"
  [[ "$schema_version" == "ticket-repair-response/v1" ]] || { echo "❌ RTR-005: schema_version 不符：$schema_version" >&2; return 5; }

  jq -e 'has("finding_results") and has("run_summary")' "$response" >/dev/null 2>&1 || { echo "❌ RTR-005: 缺 finding_results／run_summary" >&2; return 5; }

  local payload_ticket_id resp_ticket_id
  payload_ticket_id="$(jq -r '.ticket_id // ""' "$payload")"
  resp_ticket_id="$(jq -r '.ticket_id // ""' "$response")"
  [[ "$resp_ticket_id" == "$payload_ticket_id" ]] || { echo "❌ RTR-005: response ticket_id（$resp_ticket_id）不符 payload ticket_id（$payload_ticket_id）" >&2; return 5; }

  local payload_finding_count resp_finding_count
  payload_finding_count="$(jq -r '(.finding_ids // []) | length' "$payload")"
  resp_finding_count="$(jq -r '(.finding_results // []) | length' "$response")"
  if [[ "$payload_finding_count" -gt 0 && "$resp_finding_count" -eq 0 ]]; then
    echo "❌ RTR-005: finding_results 為空但 payload 含 $payload_finding_count 個 finding" >&2
    return 5
  fi

  local unauthorized
  unauthorized="$(jq -rn --slurpfile p "$payload" --slurpfile r "$response" '
    ($p[0].finding_ids) as $allowed
    | ($r[0].finding_results // []) | map(.finding_id) - $allowed | join(",")
  ')"
  [[ -z "$unauthorized" ]] || { echo "❌ RTR-005: response 含未授權 finding_id：$unauthorized" >&2; return 5; }

  return 0
}

# ═══════════════════════════════════════
#  §7 驗證層（finding 級：§7.3–§7.8）
# ═══════════════════════════════════════

validate_eligibility() {
  local orig_finding="$1" disposition="$2"
  local severity decisionness eligibility
  severity="$(jq -r '.severity' <<<"$orig_finding")"
  decisionness="$(jq -r '.decisionness' <<<"$orig_finding")"
  eligibility="$(jq -r '.repair_eligibility' <<<"$orig_finding")"

  case "$severity" in Medium|Minor) ;; *) echo "❌ RTR-002: severity=$severity 超出可修補範圍" >&2; return 2 ;; esac
  [[ "$decisionness" == "fact-layer" ]] || { echo "❌ RTR-002: decisionness=$decisionness 非 fact-layer" >&2; return 2; }
  [[ "$eligibility" == "eligible" ]] || {
    if [[ "$disposition" == "propose_repair" ]]; then
      echo "❌ RTR-002: repair_eligibility=$eligibility 但 response 仍回 propose_repair" >&2
      return 2
    fi
  }
  return 0
}

validate_evidence() {
  local payload="$1" finding_result="$2"
  local refs_count
  refs_count="$(jq -r '(.source_refs_used // []) | length' <<<"$finding_result")"
  [[ "$refs_count" -gt 0 ]] || { echo "❌ RTR-007: propose_repair 缺 source_refs_used" >&2; return 7; }

  local unmatched
  unmatched="$(jq -rn --slurpfile pl "$payload" --argjson fr "$finding_result" '
    ($pl[0].evidence // []) as $ev
    | ($fr.source_refs_used // [])
    | map(select(([.path] - ($ev | map(.path))) | length > 0))
    | length
  ')"
  [[ "$unmatched" -eq 0 ]] || { echo "❌ RTR-007: source_refs_used 引用 payload 外憑據" >&2; return 7; }

  local path
  while IFS= read -r path; do
    path="${path%$'\r'}"
    [[ -n "$path" ]] || continue
    [[ -r "$path" ]] || { echo "❌ RTR-007: cited source 不可讀：$path" >&2; return 7; }
  done < <(jq -r '(.source_refs_used // [])[].path' <<<"$finding_result")
  return 0
}

validate_target() {
  local payload="$1" finding_result="$2"
  local payload_file patch_file write_mode
  payload_file="$(jq -r '.target.file_path' "$payload")"
  patch_file="$(jq -r '.patch.target_file // ""' <<<"$finding_result")"
  write_mode="$(jq -r '.target.write_mode' "$payload")"

  [[ "$patch_file" == "$payload_file" ]] || { echo "❌ RTR-009: patch.target_file=$patch_file 不等於 payload target.file_path=$payload_file" >&2; return 9; }

  if [[ "$write_mode" != "addendum_only" ]]; then
    echo "❌ RTR-009: write_mode=$write_mode 非本 MVP 支援之允許寫入位置（僅支援 addendum_only）" >&2
    return 9
  fi

  local expected_hash live_hash
  expected_hash="$(jq -r '.target.expected_old_text_hash // ""' "$payload")"
  if [[ -n "$expected_hash" ]]; then
    [[ -f "$payload_file" ]] || { echo "❌ RTR-008: target 檔不存在：$payload_file" >&2; return 8; }
    live_hash="sha256:$(sha256sum "$payload_file" | awk '{print $1}')"
    [[ "$live_hash" == "$expected_hash" ]] || { echo "❌ RTR-008: target hash 不符（live=$live_hash expected=$expected_hash）" >&2; return 8; }
  fi
  return 0
}

validate_diff_budget() {
  local max_lines="$1" finding_result="$2" payload_file="$3"
  local replacement estimate actual_changed tmp_new
  estimate="$(jq -r '.changed_line_estimate // 0' <<<"$finding_result")"
  [[ "$estimate" -le "$max_lines" ]] || { echo "❌ RTR-010: changed_line_estimate=$estimate 超過上限=$max_lines" >&2; return 10; }

  replacement="$(jq -r '.patch.replacement_text // ""' <<<"$finding_result")"
  tmp_new="$(mktemp)"
  cat "$payload_file" > "$tmp_new" 2>/dev/null || true
  printf '%s\n' "$replacement" >> "$tmp_new"
  actual_changed="$(diff -u "$payload_file" "$tmp_new" 2>/dev/null | grep -c '^[+-][^+-]' || true)"
  rm -f "$tmp_new"
  [[ "$actual_changed" -le "$max_lines" ]] || { echo "❌ RTR-010: 實際 diff 行數=$actual_changed 超過上限=$max_lines" >&2; return 10; }
  return 0
}

validate_frozen_sentence() {
  local payload="$1" finding_result="$2"
  local replacement allow_norm term
  replacement="$(jq -r '.patch.replacement_text // ""' <<<"$finding_result")"
  allow_norm="$(jq -r '.constraints.allow_normative_edit_only_if_cited_finding // false' "$payload")"

  while IFS= read -r term; do
    term="${term%$'\r'}"
    [[ -n "$term" ]] || continue
    if grep -qiw -- "$term" <<<"$replacement"; then
      [[ "$allow_norm" == "true" ]] || { echo "❌ RTR-011: replacement_text 觸及 frozen term '$term' 但未經 allow_normative_edit_only_if_cited_finding 授權" >&2; return 11; }
    fi
  done < <(jq -r '(.constraints.frozen_terms // [])[]' "$payload")
  return 0
}

validate_decision_leakage() {
  local finding_result="$1"
  local requires_human disposition replacement marker
  requires_human="$(jq -r '.requires_human_decision // false' <<<"$finding_result")"
  disposition="$(jq -r '.disposition' <<<"$finding_result")"
  replacement="$(jq -r '.patch.replacement_text // ""' <<<"$finding_result")"

  if [[ "$requires_human" == "true" && "$disposition" == "propose_repair" ]]; then
    echo "❌ RTR-012: requires_human_decision=true 但 disposition=propose_repair（自相矛盾）" >&2
    return 12
  fi

  for marker in "${FROZEN_LEAKAGE_MARKERS[@]}"; do
    if grep -qi -- "$marker" <<<"$replacement"; then
      echo "❌ RTR-012: replacement_text 命中決策洩漏標記：'$marker'" >&2
      return 12
    fi
  done
  return 0
}

# ═══════════════════════════════════════
#  §8 寫入（僅 addendum_only）
# ═══════════════════════════════════════

apply_patch() {
  local payload_file="$1" replacement="$2"
  printf '\n%s\n' "$replacement" >> "$payload_file"
}

# 交易封口共用 hash 函式（前像備援驗證／回退驗證／post-verify 三處同源）。
file_hash() {
  echo "sha256:$(sha256sum "$1" | awk '{print $1}')"
}

rollback_target() {
  local before_file="$1" expected_hash="$2"
  cp "$before_file" "$TARGET_FILE" 2>/dev/null || return 1
  [[ "$(file_hash "$TARGET_FILE")" == "$expected_hash" ]]
}

validate_post_apply() {
  local before_file="$1" target_file="$2" replacement="$3" max_lines="$4"
  local before_hash after_hash expected_after actual_after before_lines after_lines new_lines
  before_hash="$(file_hash "$before_file")"
  after_hash="$(file_hash "$target_file")"
  [[ "$after_hash" != "$before_hash" ]] || return 1
  expected_after="$(cat "$before_file"; printf '\n%s\n' "$replacement")"
  actual_after="$(cat "$target_file")"
  [[ "$expected_after" == "$actual_after" ]] || return 1
  before_lines="$(wc -l < "$before_file")"
  after_lines="$(wc -l < "$target_file")"
  new_lines=$(( after_lines - before_lines ))
  [[ "$new_lines" -le "$max_lines" ]]
}

# ═══════════════════════════════════════
#  主執行路徑
# ═══════════════════════════════════════

RUN_HAD_FAILURE=0

validate_payload "$PAYLOAD_PATH"

TICKET_ID="$(jq -r '.ticket_id' "$PAYLOAD_PATH")"
SOURCE_RUN_ID="$(jq -r '.source_run_id // "run"' "$PAYLOAD_PATH")"
QUALITY_REPORT_PATH="$(jq -r '.quality_report.report_path' "$PAYLOAD_PATH")"
TARGET_FILE="$(jq -r '.target.file_path' "$PAYLOAD_PATH")"
MAX_CHANGED_LINES="$(jq -r '.constraints.max_changed_lines' "$PAYLOAD_PATH")"

ARTIFACTS_DIR="$TELEMETRY_ROOT/$TICKET_ID/$SOURCE_RUN_ID"
mkdir -p "$ARTIFACTS_DIR"
cp "$PAYLOAD_PATH" "$ARTIFACTS_DIR/input_payload.json"

CURRENT_FINDING_ID="$(jq -r '.finding_ids[0]' "$PAYLOAD_PATH")"
if ! circuit_breaker_precheck; then
  exit 1
fi

if ! blocker_preflight "$PAYLOAD_PATH"; then
  exit 1
fi

PROMPT_FILE="$ARTIFACTS_DIR/rendered_prompt.txt"
render_prompt "$PAYLOAD_PATH" "$PROMPT_FILE"
cp "$PROMPT_FILE" "$ARTIFACTS_DIR/prompt_used.txt"

CODEX_CALL_COUNT=$((CODEX_CALL_COUNT + 1))
RAW_RESPONSE="$ARTIFACTS_DIR/raw_response.json"
EVENT_STREAM="$ARTIFACTS_DIR/events.jsonl"

command -v "$CODEX_BIN" >/dev/null 2>&1 || fail_env "$CODEX_BIN not found"

if ! "$CODEX_BIN" exec --json \
      -m "$MODEL" \
      -c "model_reasoning_effort=$EFFORT" \
      --output-schema "$RESPONSE_SCHEMA" \
      --output-last-message "$RAW_RESPONSE" \
      - < "$PROMPT_FILE" > "$EVENT_STREAM" 2>"$ARTIFACTS_DIR/stderr.log"; then
  fail_rtr RTR-003 "$CURRENT_FINDING_ID" "codex exec 非零退出"
  exit 1
fi

[[ -f "$RAW_RESPONSE" ]] || { fail_rtr RTR-004 "$CURRENT_FINDING_ID" "raw response 檔未產生"; exit 1; }

if ! validate_response_structure "$RAW_RESPONSE" "$PAYLOAD_PATH"; then
  code=$?
  case "$code" in 4) fail_rtr RTR-004 "$CURRENT_FINDING_ID" "response 結構驗證失敗" ;; *) fail_rtr RTR-005 "$CURRENT_FINDING_ID" "response 結構驗證失敗" ;; esac
  exit 1
fi

VALIDATED_RESPONSE="$ARTIFACTS_DIR/validated_response.json"
cp "$RAW_RESPONSE" "$VALIDATED_RESPONSE"

CB4_TRIPPED=0
while IFS= read -r row; do
  fid="$(jq -r '.finding_id' <<<"$row")"
  CURRENT_FINDING_ID="$fid"
  disposition="$(jq -r '.disposition' <<<"$row")"
  orig_finding="$(jq -c --arg fid "$fid" '.findings[] | select(.finding_id == $fid)' "$PAYLOAD_PATH")"

  if [[ "$disposition" != "propose_repair" ]]; then
    append_report_entry "$fid" "$disposition" "no_write" "none" "非 propose_repair，依契約不寫入"
    continue
  fi

  if ! validate_eligibility "$orig_finding" "$disposition"; then fail_rtr RTR-002 "$fid" "eligibility 驗證失敗"; continue; fi
  if ! validate_evidence "$PAYLOAD_PATH" "$row"; then fail_rtr RTR-007 "$fid" "evidence 驗證失敗"; continue; fi
  target_rc=0
  validate_target "$PAYLOAD_PATH" "$row" || target_rc=$?
  if [[ "$target_rc" -ne 0 ]]; then
    case "$target_rc" in
      8) fail_rtr RTR-008 "$fid" "target hash 不符或檔案不存在" ;;
      *) fail_rtr RTR-009 "$fid" "target 驗證失敗" ;;
    esac
    continue
  fi
  if ! validate_diff_budget "$MAX_CHANGED_LINES" "$row" "$TARGET_FILE"; then fail_rtr RTR-010 "$fid" "diff budget 超限"; continue; fi
  if ! validate_frozen_sentence "$PAYLOAD_PATH" "$row"; then fail_rtr RTR-011 "$fid" "frozen sentence 未授權觸及"; continue; fi
  if ! validate_decision_leakage "$row"; then
    fail_rtr RTR-012 "$fid" "decision leakage"
    append_report_entry "$fid" "$disposition" "escalated" "RTR-012" "CB-4：DECISION_LEAKAGE 觸發 1 次即停損"
    CB4_TRIPPED=1
    continue
  fi

  replacement="$(jq -r '.patch.replacement_text // ""' <<<"$row")"
  before_file="$ARTIFACTS_DIR/patch_before_${fid}.txt"
  pre_hash="$(file_hash "$TARGET_FILE")"
  if ! cp "$TARGET_FILE" "$before_file" 2>/dev/null || [[ "$(file_hash "$before_file")" != "$pre_hash" ]]; then
    fail_rtr RTR-008 "$fid" "前像備援失敗，寫入前置條件不足"
    continue
  fi

  CURRENT_BEFORE_FILE="$before_file"
  CURRENT_BEFORE_HASH="$pre_hash"
  TARGET_MUTATED=1
  apply_patch "$TARGET_FILE" "$replacement"
  cp "$TARGET_FILE" "$ARTIFACTS_DIR/patch_after_${fid}.txt" 2>/dev/null || true

  if ! validate_post_apply "$before_file" "$TARGET_FILE" "$replacement" "$MAX_CHANGED_LINES"; then
    if rollback_target "$before_file" "$CURRENT_BEFORE_HASH"; then
      TARGET_MUTATED=0
      fail_rtr RTR-015 "$fid" "post-verify 失敗，已回退（hash 已驗）"
    else
      echo "❌ ROLLBACK-FAILED [$fid]: target 回退失敗，現況 hash=$(file_hash "$TARGET_FILE") 前像檔=$before_file" >&2
      append_report_entry "$fid" "$disposition" "rollback_failed" "RTR-015" "post-verify 失敗且回退失敗"
      exit 1
    fi
    continue
  fi

  append_report_entry "$fid" "$disposition" "applied" "none" "bounded addendum write only"
  TARGET_MUTATED=0
done < <(jq -c '.finding_results[]' "$VALIDATED_RESPONSE")

echo "quality report append 完成：$QUALITY_REPORT_PATH" > "$ARTIFACTS_DIR/report_append_result.log"

# P3：主流程自然終點觸發 housekeeping（子 shell 隔離，janitor 失敗不遮蔽 run 結果，不改寫既定 exit code）
if [[ -f "$HOUSEKEEPING_SCRIPT" ]]; then
  ( bash "$HOUSEKEEPING_SCRIPT" "$TELEMETRY_ROOT" ) || true
fi

if [[ "$CB4_TRIPPED" -eq 1 ]]; then
  echo "⛔ CB-4 escalated | Ticket=$TICKET_ID | codex 呼叫次數=$CODEX_CALL_COUNT" >&2
  exit 1
fi

if [[ "$RUN_HAD_FAILURE" -eq 1 ]]; then
  echo "⚠️ Runner 完成但含失敗 finding | Ticket=$TICKET_ID | codex 呼叫次數=$CODEX_CALL_COUNT" >&2
  exit 1
fi

echo "✅ Runner 完成 | Ticket=$TICKET_ID | codex 呼叫次數=$CODEX_CALL_COUNT"
exit 0
