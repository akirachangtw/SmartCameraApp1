#!/usr/bin/env bash
# gstack v1.6 — Codex CLI Cross-Review Automation (4-Verdict System)
# 複合審查：Layer 1 Architectural Review + Layer 2 SV Report + Layer 3 Blind Scan
#
# 4-Verdict: PASS / BLOCKER / REPORT_FIX / ADVISORY
#
# 用法：
#   bash .claude/skills/sv/scripts/cross_review.sh <sv_report.md> [diff_scope] [extra_exclude] [ssot_context]
#   extra_exclude: 逗號分隔 pathspec（CLI override；亦可由 sv_report.md ## Excluded Payload 驅動）
#
# 退出碼：
#   0 — PASS 或 ADVISORY（流程繼續）
#   1 — BLOCKER（真實功能缺陷，修程式碼後重跑）
#   2 — ENV 錯誤 或 FAIL BUDGET EXHAUSTED (依 MAX_BLOCKER_BUDGET)
#   3 — REPORT_FIX（sv_report.md 問題，修報告後重跑）

set -euo pipefail

# ═══════════════════════════════════════
#  參數與常數
# ═══════════════════════════════════════

SV_REPORT="${1:-}"
DIFF_SCOPE="${2:-}"
EXTRA_EXCLUDE="${3:-}"
SSOT_CONTEXT="${4:-.claude/skills/sv/ssot_context.md}"
TMP_DIR=".tmp"

PAYLOAD="$TMP_DIR/cross_review_payload.md"
TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
RESULT_LOG="$TMP_DIR/cross_review_result_${TIMESTAMP}.md"

CODEX_MODEL="${CODEX_MODEL:-gpt-5.5}"
CODEX_REASONING_EFFORT="${CODEX_REASONING_EFFORT:-high}"
CODEX_REASONING_SUMMARY="${CODEX_REASONING_SUMMARY:-auto}"
MAX_BLOCKER_BUDGET=3

_POLICY=".claude/verify-policy.env"
if [[ -f "$_POLICY" ]]; then
  _v="$(grep "^MAX_BLOCKER_BUDGET=" "$_POLICY" 2>/dev/null | head -1 | cut -d'=' -f2- || true)"
  if [[ "$_v" =~ ^[0-9]+$ ]] && [[ "$_v" -gt 0 ]]; then
    MAX_BLOCKER_BUDGET="$_v"
  fi
fi

# ═══════════════════════════════════════
#  Error handlers
# ═══════════════════════════════════════

fail_env()        { echo "❌ ENV ERROR: $*" >&2; exit 2; }
fail_budget()     { echo "⛔ FAIL BUDGET EXHAUSTED (R${CURRENT_ROUND}) — 人工介入必要"; exit 2; }
fail_blocker()    { echo "❌ BLOCKER → 修正程式碼後重新 /sv"; exit 1; }
fail_report_fix() { echo "📋 REPORT_FIX → 修正 sv_report.md 後重新執行 cross_review"; exit 3; }

# ═══════════════════════════════════════
#  環境前置檢查
# ═══════════════════════════════════════

[[ -n "$SV_REPORT" ]] || fail_env "Usage: $0 <sv_report.md> [diff_scope] [extra_exclude] [ssot_context]"
[[ -f "$SV_REPORT" ]] || fail_env "SV Report not found: $SV_REPORT"

command -v git >/dev/null 2>&1 || fail_env "git not found"
command -v codex >/dev/null 2>&1 || fail_env "codex CLI not found — install: npm i -g @openai/codex"

git rev-parse --is-inside-work-tree >/dev/null 2>&1 || fail_env "not inside a git repository"
git rev-parse --verify HEAD >/dev/null 2>&1 || fail_env "no git HEAD found (first commit?)"

# Resolve DIFF_SCOPE: arg2 > verify_cmd.conf DIFF_REF..DIFF_END > DIFF_REF..HEAD > HEAD~1..HEAD (legacy)
# RULE: HEAD~1..HEAD fallback is ONLY allowed when tmp/verify_cmd.conf is absent.
#       If conf exists but DIFF_REF is missing/invalid → hard error (not silent fallback).
#       If DIFF_END is set, use locked range DIFF_REF..DIFF_END (no HEAD drift).
#       If DIFF_END is absent, use DIFF_REF..HEAD with ⚠️ HEAD-moving scope warning.
if [[ -z "$DIFF_SCOPE" ]]; then
  _CONF_REF=""
  _CONF_END=""
  _HAS_CONF=0
  if [[ -f "tmp/verify_cmd.conf" ]]; then
    _HAS_CONF=1
    _CONF_REF="$(grep "^DIFF_REF=" "tmp/verify_cmd.conf" 2>/dev/null | head -1 | cut -d'=' -f2- || true)"
    _CONF_END="$(grep "^DIFF_END=" "tmp/verify_cmd.conf" 2>/dev/null | head -1 | cut -d'=' -f2- || true)"
  fi
  if [[ -n "$_CONF_REF" ]] && git rev-parse --verify "$_CONF_REF" >/dev/null 2>&1; then
    if [[ -n "$_CONF_END" ]] && git rev-parse --verify "$_CONF_END" >/dev/null 2>&1; then
      DIFF_SCOPE="${_CONF_REF}..${_CONF_END}"
      echo "📌 DIFF_SCOPE from verify_cmd.conf: $DIFF_SCOPE (locked range — DIFF_END set)"
    else
      DIFF_SCOPE="${_CONF_REF}..HEAD"
      COMMIT_COUNT="$(git log --oneline "${DIFF_SCOPE}" 2>/dev/null | wc -l | tr -d ' ')"
      echo "📌 DIFF_SCOPE from verify_cmd.conf: $DIFF_SCOPE ($COMMIT_COUNT commits) [⚠️ HEAD-moving scope — DIFF_END not set]"
      if [[ "$COMMIT_COUNT" -gt 5 ]]; then
        echo "⚠️ WARNING: DIFF_SCOPE spans $COMMIT_COUNT commits — DIFF_REF may be stale." >&2
        echo "   Run: bash .claude/skills/qa/scripts/verify_checklist.sh tmp/verify_cmd.conf" >&2
        echo "   to detect STALE_DIFF_REF and get the suggested correct base." >&2
      fi
    fi
  elif [[ $_HAS_CONF -eq 1 ]]; then
    echo "❌ ERROR: tmp/verify_cmd.conf exists but DIFF_REF is missing or invalid." >&2
    echo "   Fix: set DIFF_REF=<ticket-start-commit> in tmp/verify_cmd.conf" >&2
    exit 2
  else
    # Legacy path: no conf file at all
    echo "⚠️ WARNING: No tmp/verify_cmd.conf — falling back to HEAD~1..HEAD (legacy path)." >&2
    echo "   This path is only valid for ad-hoc use without a Ticket. Create verify_cmd.conf for tracked tickets." >&2
    DIFF_SCOPE="HEAD~1..HEAD"
  fi
fi

mkdir -p "$TMP_DIR"

# ═══════════════════════════════════════
#  Ticket ID + Fail Budget (R2 checkpoint, R3 hard stop)
# ═══════════════════════════════════════

TICKET_ID="unknown"
if [[ -f "tmp/verify_cmd.conf" ]]; then
  TICKET_ID="$(grep "^TICKET_ID=" "tmp/verify_cmd.conf" 2>/dev/null | head -1 | cut -d'=' -f2- || echo unknown)"
fi

STATE_DIR="$TMP_DIR/cross_review_state"
mkdir -p "$STATE_DIR"
STATE_FILE="${STATE_DIR}/${TICKET_ID}"

# Durable PASS/ADVISORY attestation receipt (Concern B) — a SEPARATE carrier from the
# fail-budget STATE_FILE (Concern A). Lives in cross_review_pass/, untouched by either janitor:
#   • cleanup_terminal_success_states scans $STATE_DIR/* only (never cross_review_pass/)
#   • `find .tmp/ -name "cross_review_*.md" -delete` never matches (receipt has no .md extension)
RECEIPT_DIR="$TMP_DIR/cross_review_pass"
RECEIPT_FILE="${RECEIPT_DIR}/${TICKET_ID}"

# Stale receipt GC: drop attestations older than 14 days so cross_review_pass/ stays bounded.
# First-level regular files only — never recursive, never rm -rf.
if [[ -d "$RECEIPT_DIR" ]]; then
  find "$RECEIPT_DIR" -maxdepth 1 -type f -mtime +14 -delete 2>/dev/null || true
fi

# Sweep historical terminal-success residue so a stale PASS/ADVISORY file never
# inflates a fresh run into R2/R3. Only PASS/ADVISORY are removed; BLOCKER,
# REPORT_FIX, UNKNOWN, missing, or malformed LAST= lines are preserved (fail budget).
# First-level regular files only — never recursive, never rm -rf.
cleanup_terminal_success_states() {
  [[ -d "$STATE_DIR" ]] || return 0
  local _sf _last
  for _sf in "$STATE_DIR"/*; do
    [[ -f "$_sf" ]] || continue
    _last="$(grep "^LAST=" "$_sf" 2>/dev/null | head -1 | cut -d'=' -f2- | tr -d '[:space:]' || true)"
    case "$_last" in
      PASS|ADVISORY) rm -f "$_sf" 2>/dev/null || true ;;
      *) : ;;
    esac
  done
}
cleanup_terminal_success_states

CURRENT_ROUND=1
if [[ -f "$STATE_FILE" ]]; then
  _PREV="$(grep "^ROUND=" "$STATE_FILE" 2>/dev/null | head -1 | cut -d'=' -f2- || echo 0)"
  CURRENT_ROUND=$(( _PREV + 1 ))
fi
echo "📊 Round: R${CURRENT_ROUND} | Ticket: ${TICKET_ID}"

# ═══════════════════════════════════════
#  Parse ## Excluded Payload from sv_report.md → merge into EXTRA_EXCLUDE
# ═══════════════════════════════════════

if [[ -f "$SV_REPORT" ]]; then
  while IFS= read -r _ef; do
    [[ -z "$_ef" ]] && continue
    if [[ -z "$EXTRA_EXCLUDE" ]]; then EXTRA_EXCLUDE="$_ef"; else EXTRA_EXCLUDE="${EXTRA_EXCLUDE},${_ef}"; fi
  done < <(awk '/^## Excluded Payload/{found=1;next} found && /^## /{exit} found && /^- /{line=substr($0,3); sub(/[[:space:]]*—.*$/,"",line); sub(/[[:space:]]*$/,"",line); gsub(/`/,"",line); if(length(line)>0) print line}' "$SV_REPORT" 2>/dev/null || true)
fi

# ═══════════════════════════════════════
#  Pre-Check: structural_verify.sh (gates Codex invocation)
# ═══════════════════════════════════════

_SV_SCRIPT=".claude/skills/qa/scripts/structural_verify.sh"
if [[ -f "$_SV_SCRIPT" ]]; then
  echo "🔍 Pre-check: structural_verify.sh..."
  set +e
  # pre-check: no --post flag → only A/B/C (D/E/F reserved for /qa Step 5 --post mode)
  bash "$_SV_SCRIPT" "$SV_REPORT" "$DIFF_SCOPE"
  _SV_EXIT=$?
  set -e
  if [[ $_SV_EXIT -eq 1 ]]; then
    printf 'TICKET=%s\nROUND=%s\nLAST=REPORT_FIX\n' "$TICKET_ID" "$CURRENT_ROUND" > "$STATE_FILE"
    fail_report_fix
  elif [[ $_SV_EXIT -ne 0 ]]; then
    echo "⚠️ structural_verify returned $_SV_EXIT — proceeding with Codex review"
  fi
else
  echo "⚠️ structural_verify.sh not found — skipping pre-check"
fi

# ═══════════════════════════════════════
#  SSOT Context
# ═══════════════════════════════════════

SSOT_AVAILABLE=false
if [[ -f "$SSOT_CONTEXT" ]]; then
  SSOT_AVAILABLE=true
  echo "📋 SSOT context loaded: $SSOT_CONTEXT"
else
  echo "⚠️ SSOT context not found: $SSOT_CONTEXT — architectural review will be limited"
fi

# Clean stale payloads
echo "   Cleaning stale .tmp/ payloads..."
find .tmp/ -name "cross_review_*.md" -delete 2>/dev/null || true

# Diff availability
DIFF_AVAILABLE=false
set +e; git diff --quiet "$DIFF_SCOPE" >/dev/null 2>&1; DIFF_EXIT=$?; set -e
case "$DIFF_EXIT" in
  0) echo "⚠️ No diff detected for scope: $DIFF_SCOPE"; echo "   Proceeding with SV Report review only." ;;
  1) DIFF_AVAILABLE=true ;;
  *) fail_env "invalid diff scope or git diff failed: $DIFF_SCOPE" ;;
esac

# ═══════════════════════════════════════
#  Payload 組裝
# ═══════════════════════════════════════

cat > "$PAYLOAD" << 'PROMPT_HEADER'
你是 Cueshot 專案的 Code Review 審查官，執行複合審查。

## 審查紀律

- 只根據 payload 中提供的資訊判斷
- 不得假設未提供的規格、Intent 或檔案內容
- 不得根據「可能」或「也許」給出判斷
- 發現問題時，必須引用具體的 diff 行號或 SV Report 段落

## 裁決格式（嚴格遵守）

第一行必須且只能輸出以下四種裁決之一（無其他文字）：
`PASS` / `BLOCKER` / `REPORT_FIX` / `ADVISORY`

**BLOCKER 白名單 — 只有以下情況可判 BLOCKER，其他一律降級**：
1. .kt/.xml 中確認存在的 Runtime crash 風險或功能 regression（非假設性）
2. Scope Ban 清單中的禁止檔案被修改且無授權
3. 真實 Anti-Mock：`TODO()`、`error("Not implemented")`、無 SSOT 依據的 hardcoded magic
4. Clean Architecture 分層違反（domain 依賴 presentation/data）
5. CameraX 重綁、ML Kit 互斥、Edge Warmth 60fps 約束違反
6. 狀態機時序錯誤（Cooldown timestamp 寫入時機錯誤，有 diff 行號為證）

**REPORT_FIX 情境（修 sv_report.md 即可，不要求修程式碼，但會阻擋 Cross/Traycer pipeline）**：
- SV Report 段落描述措辭與 diff 事實輕微不符
- 治理文件連結問題（PROGRESS.md 死連結等）
- sv_report.md 格式/完備性問題

**ADVISORY 情境（記錄即可，exit 0）**：
- 已豁免的 static scan warning（1.dp BorderStroke、既有 hardcoded string）
- 潛在但未確認的 regression（無 diff 行號）
- 建議性改進（不影響功能正確性）

## Layer 1: Architectural Review（架構重審）

根據 SSOT Reference 驗證：
1. diff 是否符合 Clean Architecture 分層（domain 不依賴任何人）→ BLOCKER
2. SSOT drift — 型別名稱與核心型別清單不一致 → BLOCKER
3. Scope Ban 清單檔案被修改且無授權 → BLOCKER
4. 硬體約束違反（ML Kit 互斥、CameraX 零重綁）→ BLOCKER

## Layer 2: SV Report Cross-Review（報告驗證）

1. SV Report ✅ 項目與 diff 事實不符（措辭偏差 → REPORT_FIX；事實錯誤 → BLOCKER）
2. diff 修改超出 SV Report 聲明範圍 → BLOCKER
3. Orphan Code（split-brain）→ BLOCKER
4. Anti-Mock（TODO()、placeholder return）→ BLOCKER
5. 缺測試的 business logic 變更 → ADVISORY

## Layer 3: 實作級盲區掃描

### 3.1 物理與系統邊界（僅本次 diff 新增的 .kt 代碼）
- `statusBarsPadding()` / `navigationBarsPadding()` 缺失 → BLOCKER
- 極細線條缺 LocalDensity → BLOCKER
- Touch Target < 48dp → BLOCKER
- 既有代碼（diff 未改動）類似問題 → ADVISORY

### 3.2 全鏈路退場（僅本次 diff 含 UI 元件刪除/替換時檢查）
- UI → ViewModel → Settings → DataStore 未同步清除 → BLOCKER

### 3.3 狀態機時序（僅本次 diff 含 Cooldown/Lockout/Timeout 時檢查）
- Timestamp 寫入時機錯誤 → BLOCKER（必須引用 diff 行號）

### 3.4 治理證據鏈
- PROGRESS.md / CLAUDE.md 連結指向已刪除檔案 → REPORT_FIX（非 BLOCKER）

### 3.5 Guard Input Reachability（INFRA-REACH-01 固定項）

以下問題為跨 Ticket 強制回答項（即使本次 diff 無相關新增，也必須明確輸出一行回答）：

1. 本 Ticket diff 是否新增任何 guard / gate / early-return / reducer branch / fallback，且其觸發條件依賴 diff 外部的 CameraState 欄位或 runtime state？
2. 若有，該 state 是否在 `app/src/main/` 存在 production writer（非預設初始化、非 reset-to-Idle 語意）？
3. producer 判定三選一：(A) diff 內可見；(B) diff 外但 production 可達；(C) 不存在或不可證明 → 標記 `missing-wiring` 風險
4. 若 producer 為 (C) → 即使 unit test 全綠，對應 AC **不得標 PASS**，標記 `missing-wiring`

**強制輸出格式（每次必須輸出此行）**：
`Guard Input Reachability: [無新 Guard] / [有新 Guard — State: X — Producer: (A)/(B)/(C)] / [missing-wiring: State X 無 producer]`

**機械 token 格式（INFRA-REACH-02 AI-4，額外必須輸出，供 structural_verify.sh 機械解析，不得省略）**：
獨立一行，開頭 `REACHABILITY:`，值為以下二選一：
- `REACHABILITY: NONE`（無新 Guard，或所有 producer 判定為 (A)/(B)）
- `REACHABILITY: <guard1>:<producer位置>,<guard2>:MISSING`（逐一列出每個新 Guard；producer 位置格式 `檔案:行號`；判定 (C) 一律填 `MISSING`）

### 3.6 注意事項
- Pipeline Artifact（PROGRESS.md）揭露問題已由 structural_verify.sh 前置處理
- 若 SV Report 已通過 structural_verify，不應就 PROGRESS.md 揭露問題重複 flag

---

PROMPT_HEADER

# Inject authorized exclusions (DO NOT FLAG section)
{
  echo
  echo "## Authorized Exclusions (絕對禁止就這些路徑給出任何裁決)"
  echo
  if [[ -n "$EXTRA_EXCLUDE" ]]; then
    echo "以下檔案已獲 CSO 授權排除，不在本次 Codex 審查範圍："
    IFS=',' read -ra _EXCL_LIST <<< "$EXTRA_EXCLUDE"
    for _ef in "${_EXCL_LIST[@]}"; do echo "- ${_ef}"; done
  else
    echo "(無授權排除)"
  fi
} >> "$PAYLOAD"

# Inject SSOT context
[[ "$SSOT_AVAILABLE" == true ]] && { echo; echo "## SSOT Reference (Architecture Rules)"; echo; cat "$SSOT_CONTEXT"; } >> "$PAYLOAD"

# Inject SV Report
{ echo; echo "## SV Report"; echo; cat "$SV_REPORT"; } >> "$PAYLOAD"

# Inject Git Diff
{
  echo; echo "## Git Diff (scope: $DIFF_SCOPE)"; echo
  if [[ "$DIFF_AVAILABLE" == true ]]; then
    # ALLOWED-aware exclusion: sv_report.md always excluded. Each governance
    # top-level path (.claude/, docs/, plans/) is excluded UNLESS the ticket's
    # ALLOWED explicitly authorizes it — otherwise a plans/-scoped SSOT ticket
    # gets fed an empty diff (:!plans/ strips its only ALLOWED file).
    CONF_ALLOWED=""
    if [[ -f "tmp/verify_cmd.conf" ]]; then
      CONF_ALLOWED=$(grep "^ALLOWED=" "tmp/verify_cmd.conf" | head -1 | cut -d'=' -f2-)
    fi
    EXCLUDE_ARGS=(':!sv_report.md')
    for _gpath in .claude/ docs/ plans/; do
      echo "$CONF_ALLOWED" | grep -qF "$_gpath" || EXCLUDE_ARGS+=( ":!${_gpath}" )
    done
    if [[ -n "$EXTRA_EXCLUDE" ]]; then
      IFS=',' read -ra EXTRA_PATHS <<< "$EXTRA_EXCLUDE"
      for path in "${EXTRA_PATHS[@]}"; do EXCLUDE_ARGS+=( ":!${path}" ); done
    fi
    git diff "$DIFF_SCOPE" -- "${EXCLUDE_ARGS[@]}"
  else
    echo "(no diff detected)"
  fi
} >> "$PAYLOAD"

PAYLOAD_BYTES="$(wc -c < "$PAYLOAD" | xargs)"

# ═══════════════════════════════════════
#  Codex 執行
# ═══════════════════════════════════════

echo "🔍 Codex Cross-Review in progress..."
echo "   SV Report: $SV_REPORT | Diff scope: $DIFF_SCOPE | Round: R${CURRENT_ROUND}"
[[ -n "$EXTRA_EXCLUDE" ]] && echo "   Authorized excludes: $EXTRA_EXCLUDE"
echo "   SSOT: $(if $SSOT_AVAILABLE; then echo 'loaded'; else echo 'N/A'; fi) | Payload: ${PAYLOAD_BYTES} bytes"
echo "   Codex: $CODEX_MODEL | effort: $CODEX_REASONING_EFFORT"
echo

set +e
RESULT="$(codex exec \
  -c "model=\"$CODEX_MODEL\"" \
  -c "model_reasoning_effort=\"$CODEX_REASONING_EFFORT\"" \
  -c "model_reasoning_summary=\"$CODEX_REASONING_SUMMARY\"" \
  - < "$PAYLOAD" 2>&1)"
CODEX_EXIT=$?
set -e

if [[ $CODEX_EXIT -ne 0 ]]; then
  echo "❌ codex CLI failed (exit $CODEX_EXIT)" >&2
  echo "$RESULT" >&2
  fail_env "codex execution error — check auth, network, and payload size"
fi

# ═══════════════════════════════════════
#  結果解析、Fail Budget 更新、輸出
# ═══════════════════════════════════════

{
  echo "# Cross-Review Result — $TIMESTAMP"
  echo "- Ticket: $TICKET_ID | Round: R${CURRENT_ROUND} | Diff: $DIFF_SCOPE"
  echo "- SSOT: $SSOT_AVAILABLE | Excludes: ${EXTRA_EXCLUDE:-none}"
  echo; echo "$RESULT"
} > "$RESULT_LOG"

echo
echo "═══════════════════════════════════════"
echo "  Codex CROSS-REVIEW RESULT (R${CURRENT_ROUND})"
echo "═══════════════════════════════════════"
echo "$RESULT"
echo "═══════════════════════════════════════"
echo
echo "📝 Full result saved: $RESULT_LOG"

# Parse verdict (after "codex" separator line). REPORTFIX is accepted only as
# compatibility input from the original generic plugin; project state writes REPORT_FIX.
VERDICT="$(printf '%s\n' "$RESULT" | tr -d '\r' | awk '/^codex$/{found=1;next} found && /^(PASS|BLOCKER|REPORT_FIX|REPORTFIX|ADVISORY)$/{print;exit}' | xargs)"
if [[ "$VERDICT" == "REPORTFIX" ]]; then
  VERDICT="REPORT_FIX"
fi

# Parse REACHABILITY: token (INFRA-REACH-02 AI-4) — machine-parseable companion to the
# prose "Guard Input Reachability:" line. Missing/malformed → UNSET (receipt still gets
# the field; structural_verify.sh Check E-2 flags UNSET as a FAIL, not a silent skip).
REACHABILITY="$(printf '%s\n' "$RESULT" | tr -d '\r' | grep -E "^REACHABILITY:" | head -1 | sed 's/^REACHABILITY:[[:space:]]*//' | xargs || true)"
[[ -z "$REACHABILITY" ]] && REACHABILITY="UNSET"

# Save fail budget state
printf 'TICKET=%s\nROUND=%s\nLAST=%s\n' "$TICKET_ID" "$CURRENT_ROUND" "${VERDICT:-UNKNOWN}" > "$STATE_FILE"

# Fail budget enforcement
if [[ "$VERDICT" == "BLOCKER" ]]; then
  if [[ $CURRENT_ROUND -ge $MAX_BLOCKER_BUDGET ]]; then
    echo "⛔ R${CURRENT_ROUND} BLOCKER — FAIL BUDGET EXHAUSTED. 人工介入必要。"
    fail_budget
  elif [[ $CURRENT_ROUND -ge $((MAX_BLOCKER_BUDGET - 1)) ]]; then
    echo "⚠️ R${CURRENT_ROUND} BLOCKER CHECKPOINT — R${MAX_BLOCKER_BUDGET} will hard-stop. 等待 Akira 裁決。"
  fi
fi

# Terminal-success cleanup: fail budget no longer needed once the ticket passes.
# BLOCKER/REPORT_FIX must KEEP the state file so ROUND keeps accumulating across reruns.
clear_budget_state() { rm -f "$STATE_FILE" 2>/dev/null || true; rmdir "$STATE_DIR" 2>/dev/null || true; }

# Durable range-bound PASS/ADVISORY receipt (Concern B). Written BEFORE clear_budget_state so
# Check E (structural_verify.sh --post) survives the fail-budget STATE_FILE deletion. DIFF_SCOPE
# binds the attestation to the reviewed range (Check E rejects a stale scope = false-open defense).
write_pass_receipt() {
  mkdir -p "$RECEIPT_DIR" 2>/dev/null || true
  printf 'TICKET=%s\nVERDICT=%s\nDIFF_SCOPE=%s\nCOMMIT=%s\nTS=%s\nREACHABILITY=%s\n' \
    "$TICKET_ID" "$VERDICT" "$DIFF_SCOPE" "$(git rev-parse HEAD 2>/dev/null || echo unknown)" "$TIMESTAMP" "$REACHABILITY" \
    > "$RECEIPT_FILE"
}

# Exit by verdict
case "$VERDICT" in
  PASS)       write_pass_receipt; clear_budget_state; echo; echo "✅ PASS → Ready for Traycer 終審"; exit 0 ;;
  ADVISORY)   write_pass_receipt; clear_budget_state; echo; echo "💡 ADVISORY → 記錄觀察，流程繼續"; exit 0 ;;
  REPORT_FIX) echo; fail_report_fix ;;
  BLOCKER)    echo; fail_blocker ;;
  *)
    echo "⚠️ Codex output format invalid: verdict='${VERDICT}' (expected PASS/BLOCKER/REPORT_FIX/ADVISORY)"
    echo "   Treating as BLOCKER for safety."
    fail_blocker
    ;;
esac
