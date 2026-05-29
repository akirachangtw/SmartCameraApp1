#!/usr/bin/env bash
# structural_verify.sh — SV Report vs Git Diff 雙向一致性驗證
#
# 用法：
#   bash .claude/skills/qa/scripts/structural_verify.sh [sv_report] [diff_ref]
#   sv_report:  預設 ./sv_report.md
#   diff_ref:   預設從 tmp/verify_cmd.conf 讀取；無則 HEAD~1
#
# 檢查規則：
#   A. report 宣稱但 diff 無此檔 → FAIL（幽靈宣稱）
#   B. diff 有但 report 未列    → FAIL（漏揭露）
#   Pipeline Artifacts（PROGRESS.md）仍須列於 Files Changed，但不計入 ALLOWED 比對
#
# 退出碼：
#   0 — PASS
#   1 — FAIL
#   2 — 環境錯誤

set -euo pipefail

SV_REPORT="${1:-./sv_report.md}"
DIFF_REF_ARG="${2:-}"

fail_env() { echo "❌ ENV ERROR: $*" >&2; exit 2; }

[[ -f "$SV_REPORT" ]] || fail_env "SV Report not found: $SV_REPORT"
command -v git >/dev/null 2>&1 || fail_env "git not found"

# Resolve DIFF_REF: arg2 > verify_cmd.conf > HEAD~1
DIFF_REF="$DIFF_REF_ARG"
if [[ -z "$DIFF_REF" ]]; then
  if [[ -f "tmp/verify_cmd.conf" ]]; then
    DIFF_REF="$(grep "^DIFF_REF=" "tmp/verify_cmd.conf" 2>/dev/null | head -1 | cut -d'=' -f2- || true)"
  fi
fi
if [[ -z "$DIFF_REF" ]]; then
  echo "⚠️ WARNING: No DIFF_REF — falling back to HEAD~1 (legacy)" >&2
  DIFF_REF="HEAD~1"
fi

git rev-parse --verify "$DIFF_REF" >/dev/null 2>&1 || fail_env "DIFF_REF not in git history: $DIFF_REF"

DIFF_SCOPE="${DIFF_REF}..HEAD"

# Parse "## Files Changed" section (supports both "## Files Changed" and "## Files Changed (payload)")
# Strips trailing annotations like "← Pipeline Artifact" or "(document update, not implementation)"
REPORTED_FILES="$(awk '/^## Files Changed/{found=1;next} found && /^## /{exit} found && /^- /{print substr($0,3)}' "$SV_REPORT" \
  | sed 's/[[:space:]]*←.*$//' \
  | sed 's/[[:space:]]*$//'\
  | awk '{print $1}')"

if [[ -z "$REPORTED_FILES" ]]; then
  echo "⚠️ No '## Files Changed' section found in SV Report — skipping structural verify"
  exit 0
fi

ACTUAL_FILES="$(git diff --name-only "$DIFF_SCOPE" 2>/dev/null || true)"

FAIL=0

# Check A: report claims file not in diff (ghost claim)
GHOST=""
while IFS= read -r f; do
  [[ -z "$f" ]] && continue
  if ! echo "$ACTUAL_FILES" | grep -qF "$f"; then
    GHOST="${GHOST}  - ${f}\n"
  fi
done <<< "$REPORTED_FILES"

# Check B: diff has file not listed in report (missing disclosure)
UNDISCLOSED=""
while IFS= read -r f; do
  [[ -z "$f" ]] && continue
  if ! echo "$REPORTED_FILES" | grep -qF "$f"; then
    UNDISCLOSED="${UNDISCLOSED}  - ${f}\n"
  fi
done <<< "$ACTUAL_FILES"

if [[ -n "$GHOST" ]]; then
  echo "❌ STRUCTURAL VERIFY FAIL — [A] Report 宣稱但 diff 無此檔（幽靈宣稱）："
  printf '%b' "$GHOST"
  FAIL=1
fi

if [[ -n "$UNDISCLOSED" ]]; then
  echo "❌ STRUCTURAL VERIFY FAIL — [B] Diff 有但 Report 未揭露（漏列）："
  printf '%b' "$UNDISCLOSED"
  echo "   提示：.claude/PROGRESS.md 須列於 Files Changed 並標示 ← Pipeline Artifact"
  FAIL=1
fi

if [[ $FAIL -eq 1 ]]; then
  echo "   修正方式：更新 SV Report ## Files Changed 使其與 git diff ${DIFF_SCOPE} 精確一致。"
  exit 1
fi

echo "✅ STRUCTURAL VERIFY PASS — SV Report Files Changed 與 git diff ${DIFF_SCOPE} 雙向一致"
exit 0
