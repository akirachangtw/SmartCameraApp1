#!/usr/bin/env bash
# structural_verify.sh v1.2 — SV Report vs Git Diff 三向一致性驗證
#
# 用法：
#   bash .claude/skills/qa/scripts/structural_verify.sh [sv_report] [diff_scope_or_ref]
#   arg2: "ref..HEAD" (scope) 或單一 commit ref (自動補 ..HEAD)
#
# 檢查規則：
#   A. report 宣稱但 diff 無此檔 → FAIL (幽靈宣稱)
#   B. diff 有但 report 未列    → FAIL (漏揭露)
#   C. Excluded Payload 的每個檔案必須也出現在 Files Changed → FAIL (排除但未揭露)
#
# 退出碼：
#   0 — PASS
#   1 — REPORT_FIX needed (sv_report.md 問題，修後重跑)
#   2 — 環境錯誤

set -euo pipefail

SV_REPORT="${1:-./sv_report.md}"
SCOPE_ARG="${2:-}"

fail_env() { echo "❌ ENV ERROR: $*" >&2; exit 2; }

[[ -f "$SV_REPORT" ]] || fail_env "SV Report not found: $SV_REPORT"
command -v git >/dev/null 2>&1 || fail_env "git not found"

# Resolve DIFF_SCOPE: arg2 (scope or ref) > verify_cmd.conf > HEAD~1..HEAD
DIFF_SCOPE="$SCOPE_ARG"
if [[ -z "$DIFF_SCOPE" ]]; then
  _CONF_REF=""
  if [[ -f "tmp/verify_cmd.conf" ]]; then
    _CONF_REF="$(grep "^DIFF_REF=" "tmp/verify_cmd.conf" 2>/dev/null | head -1 | cut -d'=' -f2- || true)"
  fi
  if [[ -n "$_CONF_REF" ]]; then
    DIFF_SCOPE="${_CONF_REF}..HEAD"
  else
    echo "⚠️ WARNING: No DIFF_REF — falling back to HEAD~1..HEAD (legacy)" >&2
    DIFF_SCOPE="HEAD~1..HEAD"
  fi
elif [[ "$DIFF_SCOPE" != *".."* ]]; then
  DIFF_SCOPE="${DIFF_SCOPE}..HEAD"
fi

_REF="${DIFF_SCOPE%%\.\.*}"
git rev-parse --verify "$_REF" >/dev/null 2>&1 || fail_env "Ref not in git history: $_REF (scope: $DIFF_SCOPE)"

# Parse ## Files Changed — strip trailing ← Pipeline Artifact annotations, take first field
REPORTED_FILES="$(awk '/^## Files Changed/{found=1;next} found && /^## /{exit} found && /^- /{print substr($0,3)}' "$SV_REPORT" \
  | sed 's/[[:space:]]*←.*$//' \
  | sed 's/[[:space:]]*$//' \
  | awk 'NF{print $1}')"

# Parse ## Excluded Payload — strip trailing — reason annotations, take first field
EXCLUDED_FILES="$(awk '/^## Excluded Payload/{found=1;next} found && /^## /{exit} found && /^- /{print substr($0,3)}' "$SV_REPORT" \
  | sed 's/[[:space:]]*—.*$//' \
  | sed 's/[[:space:]]*$//' \
  | awk 'NF{print $1}')"

if [[ -z "$REPORTED_FILES" ]]; then
  echo "⚠️ No '## Files Changed' section in SV Report — skipping structural verify"
  exit 0
fi

ACTUAL_FILES="$(git diff --name-only "$DIFF_SCOPE" 2>/dev/null || true)"

FAIL=0

# Check A: report claims file not in diff (ghost claim)
GHOST=""
while IFS= read -r f; do
  [[ -z "$f" ]] && continue
  if ! echo "$ACTUAL_FILES" | grep -qxF "$f"; then
    GHOST="${GHOST}  - ${f}\n"
  fi
done <<< "$REPORTED_FILES"

# Check B: diff has file not listed in report (missing disclosure)
UNDISCLOSED=""
while IFS= read -r f; do
  [[ -z "$f" ]] && continue
  if ! echo "$REPORTED_FILES" | grep -qxF "$f"; then
    UNDISCLOSED="${UNDISCLOSED}  - ${f}\n"
  fi
done <<< "$ACTUAL_FILES"

# Check C: each excluded file must also appear in Files Changed
EXCLUDED_NOT_DISCLOSED=""
if [[ -n "$EXCLUDED_FILES" ]]; then
  while IFS= read -r f; do
    [[ -z "$f" ]] && continue
    if ! echo "$REPORTED_FILES" | grep -qxF "$f"; then
      EXCLUDED_NOT_DISCLOSED="${EXCLUDED_NOT_DISCLOSED}  - ${f}\n"
    fi
  done <<< "$EXCLUDED_FILES"
fi

[[ -n "$GHOST" ]] && { echo "❌ STRUCTURAL FAIL — [A] 幽靈宣稱（Report 有，diff 無）："; printf '%b' "$GHOST"; FAIL=1; }
[[ -n "$UNDISCLOSED" ]] && { echo "❌ STRUCTURAL FAIL — [B] 漏揭露（diff 有，Report 無）："; printf '%b' "$UNDISCLOSED"; echo "   提示：.claude/PROGRESS.md 須列並標示 ← Pipeline Artifact"; FAIL=1; }
[[ -n "$EXCLUDED_NOT_DISCLOSED" ]] && { echo "❌ STRUCTURAL FAIL — [C] Excluded Payload 未在 Files Changed 揭露："; printf '%b' "$EXCLUDED_NOT_DISCLOSED"; echo "   提示：排除的檔案仍須出現在 Files Changed"; FAIL=1; }

if [[ $FAIL -eq 1 ]]; then
  echo "   修正方式：更新 sv_report.md ## Files Changed 使其與 git diff ${DIFF_SCOPE} 精確一致。"
  exit 1
fi

echo "✅ STRUCTURAL VERIFY PASS (A/B/C) — git diff ${DIFF_SCOPE}"
exit 0
