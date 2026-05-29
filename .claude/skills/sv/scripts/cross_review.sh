#!/usr/bin/env bash
# gstack v1.2 — Codex CLI Cross-Review Automation
# 複合審查：Layer 1 Architectural Review + Layer 2 SV Report Cross-Review
#
# 設計原則：
# - Codex 不修改程式碼，只做二審裁判
# - 一次呼叫 = 兩層防線，credit 消耗不變
# - SSOT context 注入讓 Codex 從「盲審」變「知情審」
# - 嚴格輸出格式：第一行 PASS 或 FAIL，杜絕模糊判斷
#
# 用法：
#   bash .claude/scripts/cross_review.sh ./sv_report.md [diff_scope] [extra_exclude] [ssot_context_path]
#   extra_exclude: 逗號分隔的 pathspec，排除已通過獨立審查的跨票檔案
#                 例如 "*/CameraScreen.kt,*/Foo.kt"
#                 必須有 CSO 授權，sv_report.md Scope Check 段落須聲明排除理由。
#
# 退出碼：
#   0 — Cross-review PASS，可進 Traycer 終審
#   1 — Cross-review FAIL，回修正
#   2 — 環境或輸入錯誤

set -euo pipefail

# ═══════════════════════════════════════
#  參數與預設值
# ═══════════════════════════════════════

SV_REPORT="${1:-}"
DIFF_SCOPE="${2:-}"
EXTRA_EXCLUDE="${3:-}"
SSOT_CONTEXT="${4:-.claude/skills/sv/ssot_context.md}"
TMP_DIR=".tmp"

PAYLOAD="$TMP_DIR/cross_review_payload.md"
TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
RESULT_LOG="$TMP_DIR/cross_review_result_${TIMESTAMP}.md"

# Model configuration — override via env vars for escalation (see Rule 17)
CODEX_MODEL="${CODEX_MODEL:-gpt-5.5}"
CODEX_REASONING_EFFORT="${CODEX_REASONING_EFFORT:-high}"
CODEX_REASONING_SUMMARY="${CODEX_REASONING_SUMMARY:-auto}"

# ═══════════════════════════════════════
#  錯誤處理函數
# ═══════════════════════════════════════

fail_env() {
  echo "❌ ENV ERROR: $*" >&2
  exit 2
}

fail_review() {
  echo "❌ FAIL → 修正後重新 /sv"
  exit 1
}

# ═══════════════════════════════════════
#  環境前置檢查
# ═══════════════════════════════════════

[[ -n "$SV_REPORT" ]] || fail_env "Usage: $0 <sv_report.md> [diff_scope] [ssot_context_path]"
[[ -f "$SV_REPORT" ]] || fail_env "SV Report not found: $SV_REPORT"

command -v git >/dev/null 2>&1 || fail_env "git not found"
command -v codex >/dev/null 2>&1 || fail_env "codex CLI not found — install: npm i -g @openai/codex"

git rev-parse --is-inside-work-tree >/dev/null 2>&1 || fail_env "not inside a git repository"
git rev-parse --verify HEAD >/dev/null 2>&1 || fail_env "no git HEAD found (first commit?)"

# Resolve DIFF_SCOPE: arg2 > verify_cmd.conf DIFF_REF..HEAD > HEAD~1..HEAD (legacy fallback)
if [[ -z "$DIFF_SCOPE" ]]; then
  _CONF_REF=""
  if [[ -f "tmp/verify_cmd.conf" ]]; then
    _CONF_REF="$(grep "^DIFF_REF=" "tmp/verify_cmd.conf" 2>/dev/null | head -1 | cut -d'=' -f2- || true)"
  fi
  if [[ -n "$_CONF_REF" ]] && git rev-parse --verify "$_CONF_REF" >/dev/null 2>&1; then
    DIFF_SCOPE="${_CONF_REF}..HEAD"
    echo "📌 DIFF_SCOPE from verify_cmd.conf: $DIFF_SCOPE"
  else
    echo "⚠️ WARNING: No DIFF_REF in tmp/verify_cmd.conf — falling back to HEAD~1..HEAD (legacy)" >&2
    DIFF_SCOPE="HEAD~1..HEAD"
  fi
fi

# SSOT context 檢查（非阻塞 — 無則降級為純 cross-review）
SSOT_AVAILABLE=false
if [[ -f "$SSOT_CONTEXT" ]]; then
  SSOT_AVAILABLE=true
  echo "📋 SSOT context loaded: $SSOT_CONTEXT"
else
  echo "⚠️ SSOT context not found: $SSOT_CONTEXT — architectural review will be limited"
fi

mkdir -p "$TMP_DIR"

# Clean stale payloads before assembly (context isolation)
echo "   Cleaning stale .tmp/ payloads..."
find .tmp/ -name "cross_review_*.md" -delete 2>/dev/null || true

# Diff 存在性檢查
DIFF_AVAILABLE=false
set +e
git diff --quiet "$DIFF_SCOPE" >/dev/null 2>&1
DIFF_EXIT=$?
set -e

case "$DIFF_EXIT" in
  0)
    echo "⚠️ No diff detected for scope: $DIFF_SCOPE"
    echo "   Proceeding with SV Report review only."
    ;;
  1)
    DIFF_AVAILABLE=true
    ;;
  *)
    fail_env "invalid diff scope or git diff failed: $DIFF_SCOPE"
    ;;
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

## Layer 1: Architectural Review（架構重審）

根據 SSOT Reference 段落驗證：
1. diff 中的新增/修改是否符合 Clean Architecture 分層（domain → data → presentation，domain 不依賴任何人）
2. 是否存在 SSOT drift — 型別名稱、函數簽名與 SSOT 定義的核心型別清單不一致
3. 新增元件是否符合絞殺者模式（新建檔案 vs 修改既有 God Object）
4. 是否修改了 Scope Ban 清單中的檔案而無對應 Intent 授權
5. 硬體約束是否被遵守（ML Kit 互斥、CameraX 零重綁、Edge Warmth 60fps）

## Layer 2: SV Report Cross-Review（報告驗證）

根據 SV Report + Git Diff 交叉比對：
1. SV Report 宣稱的每個 ✅ 項目是否與 diff 事實相符
2. diff 是否修改了超出 SV Report 聲明範圍的檔案
3. 是否存在明顯 regression、缺少測試、或風險未揭露
4. 是否存在 Orphan Code（舊邏輯未移除導致 split-brain）
5. 是否存在 Anti-Mock（TODO()、placeholder return、hardcoded magic number 無 SSOT 依據）

## Layer 3: 實作級盲區掃描（Phase 0 經驗萃取）

根據歷史退件紀錄，額外掃描以下高頻缺陷模式。
任何一項 FAIL 即整體 FAIL，不可降級為 WARNING。

### 3.1 物理與系統邊界
- [ ] 是否使用 `statusBarsPadding()` / `navigationBarsPadding()` 而非 hardcoded padding？
- [ ] 極細線條是否使用 `LocalDensity` 換算為物理像素？
      （注意：`1.dp` 在 Divider / BorderStroke 中是合法的，但在 padding / height / width 中需審查）
- [ ] 互動元件 Touch Target 是否 ≥ 48dp？

### 3.2 全鏈路退場（四點反向掃描）
- [ ] 若本次 diff 含任何 UI 元件刪除/替換：
      追溯完整鏈路 UI → ViewModel → Settings → DataStore Preferences 是否同步清除？
      （缺任何一層 = 孤兒代碼 = FAIL）

### 3.3 狀態機時序
- [ ] 若本次 diff 含 Cooldown / Lockout / Timeout 邏輯：
      Timestamp 變數的寫入時機是否為「狀態轉移瞬間」而非「前次動作時間」？
- [ ] 邊界測試三問：timestamp=0 / now / now-cooldown+1 是否皆有正確行為？

### 3.4 治理證據鏈
- [ ] PROGRESS.md 與 CLAUDE.md 中的檔案連結是否全部指向存活的檔案？
- [ ] 若存在「尚未實作但已規劃」的 UI 槽位，是否留有隱形佔位符（不可有可見 placeholder text）？
- [ ] sv_report.md 的 diff 範圍是否與本次 Ticket Scope 精確對應？（diff 超出 = 越權嫌疑）

## 回答格式（嚴格遵守）

第一行必須且只能輸出 `PASS` 或 `FAIL`（無其他文字）

若 PASS：
```
PASS
- [arch] 架構審查通過理由（一句話）
- [sv] 報告驗證通過理由（一句話）
```

若 FAIL：
```
FAIL
- [severity: high|medium|low] [layer: arch|sv] <file>:<location> 問題描述
- 建議修正方式
```

---

PROMPT_HEADER

# 注入 SSOT context（如果存在）
if [[ "$SSOT_AVAILABLE" == true ]]; then
  {
    echo
    echo "## SSOT Reference (Architecture Rules)"
    echo
    cat "$SSOT_CONTEXT"
  } >> "$PAYLOAD"
fi

# 注入 SV Report
{
  echo
  echo "## SV Report"
  echo
  cat "$SV_REPORT"
} >> "$PAYLOAD"

# 注入 Git Diff
{
  echo
  echo "## Git Diff (scope: $DIFF_SCOPE)"
  echo
  if [[ "$DIFF_AVAILABLE" == true ]]; then
    # 動態調整 .claude/ 排除：workflow ticket（ALLOWED 含 .claude/）時納入 .claude/ diff
    CLAUDE_ALLOWED=false
    if [[ -f "tmp/verify_cmd.conf" ]]; then
      CONF_ALLOWED=$(grep "^ALLOWED=" "tmp/verify_cmd.conf" | head -1 | cut -d'=' -f2-)
      if echo "$CONF_ALLOWED" | grep -q "\.claude/"; then
        CLAUDE_ALLOWED=true
      fi
    fi
    # Build exclusion list (array-based, avoids eval shell injection)
    if [[ "$CLAUDE_ALLOWED" == true ]]; then
      EXCLUDE_ARGS=(':!sv_report.md' ':!docs/' ':!plans/')
    else
      EXCLUDE_ARGS=(':!sv_report.md' ':!.claude/' ':!docs/' ':!plans/')
    fi
    if [ -n "$EXTRA_EXCLUDE" ]; then
      IFS=',' read -ra EXTRA_PATHS <<< "$EXTRA_EXCLUDE"
      for path in "${EXTRA_PATHS[@]}"; do
        EXCLUDE_ARGS+=( ":!${path}" )
      done
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
echo "   SV Report: $SV_REPORT"
echo "   Diff scope: $DIFF_SCOPE"
if [ -n "$EXTRA_EXCLUDE" ]; then
  echo "   Extra excludes: $EXTRA_EXCLUDE"
fi
echo "   SSOT context: $(if $SSOT_AVAILABLE; then echo 'loaded'; else echo 'not available'; fi)"
echo "   Payload: ${PAYLOAD_BYTES} bytes"
echo "   Codex model: $CODEX_MODEL"
echo "   Reasoning effort: $CODEX_REASONING_EFFORT"
echo "   Reasoning summary: $CODEX_REASONING_SUMMARY"
echo

set +e
RESULT="$(
  codex exec \
    -c "model=\"$CODEX_MODEL\"" \
    -c "model_reasoning_effort=\"$CODEX_REASONING_EFFORT\"" \
    -c "model_reasoning_summary=\"$CODEX_REASONING_SUMMARY\"" \
    - < "$PAYLOAD" 2>&1
)"
CODEX_EXIT=$?
set -e

# Codex 執行失敗處理
if [[ $CODEX_EXIT -ne 0 ]]; then
  echo "❌ codex CLI failed (exit $CODEX_EXIT)" >&2
  echo "$RESULT" >&2
  fail_env "codex execution error — check auth, network, and payload size"
fi

# ═══════════════════════════════════════
#  結果解析與輸出
# ═══════════════════════════════════════

# 保存完整結果到日誌
{
  echo "# Cross-Review Result — $TIMESTAMP"
  echo "- SV Report: $SV_REPORT"
  echo "- Diff scope: $DIFF_SCOPE"
  echo "- SSOT context: $SSOT_AVAILABLE"
  echo
  echo "$RESULT"
} > "$RESULT_LOG"

echo
echo "═══════════════════════════════════════"
echo "  Codex CROSS-REVIEW RESULT"
echo "═══════════════════════════════════════"
echo "$RESULT"
echo "═══════════════════════════════════════"
echo
echo "📝 Full result saved: $RESULT_LOG"

# 嚴格解析：跳過 Codex CLI header 及 prompt echo，只讀 "codex" 分隔符後的第一行 PASS/FAIL
FIRST_LINE="$(printf '%s\n' "$RESULT" | tr -d '\r' | awk '/^codex$/{found=1;next} found && /^(PASS|FAIL)$/{print;exit}' | xargs)"

if [[ "$FIRST_LINE" == "PASS" ]]; then
  echo
  echo "✅ PASS → Ready for Traycer 終審"
  exit 0
elif [[ "$FIRST_LINE" == "FAIL" ]]; then
  echo
  fail_review
else
  echo
  echo "⚠️ Codex output format invalid: first line is '$FIRST_LINE' (expected PASS or FAIL)"
  echo "   Treating as FAIL for safety. Review the full output above."
  fail_review
fi
