#!/usr/bin/env bash
# structural_verify.sh — SV Report vs Git Diff 一致性驗證
#
# 用法：
#   bash .claude/skills/qa/scripts/structural_verify.sh [sv_report] [diff_ref]
#   sv_report:  預設 ./sv_report.md
#   diff_ref:   預設從 tmp/verify_cmd.conf 讀取；無則 HEAD~1
#
# 退出碼：
#   0 — PASS
#   1 — FAIL（SV Report 宣稱的檔案不在 git diff 中）
#   2 — 環境錯誤

set -euo pipefail

SV_REPORT="${1:-./sv_report.md}"
DIFF_REF_ARG="${2:-}"

fail_env() { echo "❌ ENV ERROR: $*" >&2; exit 2; }
fail_check() { echo "❌ STRUCTURAL VERIFY FAIL: $*"; exit 1; }

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

# Parse "## Files Changed" section from SV Report
# Matches lines like: - path/to/file.ext
REPORTED_FILES="$(awk '/^## Files Changed/{found=1;next} found && /^## /{exit} found && /^- /{print substr($0,3)}' "$SV_REPORT" | sed 's/[[:space:]]*$//')"

if [[ -z "$REPORTED_FILES" ]]; then
  echo "⚠️ No '## Files Changed' section found in SV Report — skipping structural verify"
  exit 0
fi

ACTUAL_FILES="$(git diff --name-only "$DIFF_SCOPE" 2>/dev/null || true)"

MISSING=""
while IFS= read -r f; do
  [[ -z "$f" ]] && continue
  if ! echo "$ACTUAL_FILES" | grep -qF "$f"; then
    MISSING="${MISSING}  - ${f}\n"
  fi
done <<< "$REPORTED_FILES"

if [[ -n "$MISSING" ]]; then
  echo "❌ STRUCTURAL VERIFY FAIL"
  echo "   SV Report 宣稱以下檔案已修改，但不在 git diff ${DIFF_SCOPE} 中："
  printf '%b' "$MISSING"
  echo "   修正方式：更新 SV Report 的 Files Changed 使其與實際 diff 一致，或調整 DIFF_REF。"
  exit 1
fi

echo "✅ STRUCTURAL VERIFY PASS — SV Report Files Changed 與 git diff ${DIFF_SCOPE} 一致"
exit 0
