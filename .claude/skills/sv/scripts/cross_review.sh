#!/usr/bin/env bash
# gstack v1.5 — Codex CLI Cross-Review Automation
# 複合審查：Layer 1 Architectural Review + Layer 2 SV Report Cross-Review
#
# 設計原則：
# - Codex 不修改程式碼，只做二審裁判
# - 一次呼叫 = 兩層防線，credit 消耗不變
# - SSOT context 注入讓 Codex 從「盲審」變「知情審」
# - 嚴格輸出格式：第一行 PASS 或 FAIL，杜絕模糊判斷
#
# 用法：
#   bash .claude/scripts/cross_review.sh ./sv_report.md [diff_scope] [ssot_context_path]
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
DIFF_SCOPE="${2:-HEAD~1..HEAD}"
SSOT_CONTEXT="${3:-.claude/skills/sv/ssot_context.md}"
TMP_DIR=".tmp"

# Auto-cleanup: delete sv_report.md on exit (PASS or FAIL) so the next
# ticket always starts with a fresh report. Prevents stale reports from
# a prior ticket silently passing review as-is.
trap '[[ -n "${SV_REPORT:-}" ]] && rm -f "$SV_REPORT" 2>/dev/null || true' EXIT
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
    git diff "$DIFF_SCOPE"
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
