#!/usr/bin/env bash
# structural_verify.sh v1.3 — SV Report vs Git Diff 三向一致性驗證 + Pipeline 結構校驗
#
# 用法：
#   bash .claude/skills/qa/scripts/structural_verify.sh [sv_report] [diff_scope_or_ref] [--post]
#   arg2: "ref..HEAD" (scope) 或單一 commit ref (自動補 ..HEAD)
#   --post: 啟用 post mode，額外執行 Check D/E/F（/qa Step 5 使用）
#           pre-check 模式（cross_review.sh 呼叫，不帶 --post）只跑 A/B/C
#
# 檢查規則：
#   A. report 宣稱但 diff 無此檔 → FAIL (幽靈宣稱)         [pre + post]
#   B. diff 有但 report 未列    → FAIL (漏揭露)              [pre + post]
#   C. Excluded Payload 的每個檔案必須也出現在 Files Changed → FAIL [pre + post]
#   D. sv_report 五段結構完整性 → FAIL (缺必要段落)          [post only]
#   E. cross_review PASS 確認   → FAIL (未通過或 BLOCKER)    [post only]
#   F. PROGRESS.md 格式校驗     → FAIL (缺必要欄位)          [post only]
#
# 退出碼：
#   0 — PASS
#   1 — REPORT_FIX needed (sv_report.md 問題，修後重跑)
#   2 — 環境錯誤

set -euo pipefail

# ─── Parse --post flag (may appear in any position) ───
POST_MODE=false
POSITIONAL=()
for _arg in "$@"; do
  if [[ "$_arg" == "--post" ]]; then
    POST_MODE=true
  else
    POSITIONAL+=("$_arg")
  fi
done

SV_REPORT="${POSITIONAL[0]:-./sv_report.md}"
SCOPE_ARG="${POSITIONAL[1]:-}"

fail_env() { echo "❌ ENV ERROR: $*" >&2; exit 2; }

[[ -f "$SV_REPORT" ]] || fail_env "SV Report not found: $SV_REPORT"
command -v git >/dev/null 2>&1 || fail_env "git not found"

# Resolve DIFF_SCOPE: arg2 (scope or ref) > verify_cmd.conf DIFF_REF..DIFF_END > HEAD~1..HEAD
DIFF_SCOPE="$SCOPE_ARG"
if [[ -z "$DIFF_SCOPE" ]]; then
  _CONF_REF=""
  _CONF_END=""
  if [[ -f "tmp/verify_cmd.conf" ]]; then
    _CONF_REF="$(grep "^DIFF_REF=" "tmp/verify_cmd.conf" 2>/dev/null | head -1 | cut -d'=' -f2- || true)"
    _CONF_END="$(grep "^DIFF_END=" "tmp/verify_cmd.conf" 2>/dev/null | head -1 | cut -d'=' -f2- || true)"
  fi
  if [[ -n "$_CONF_REF" ]]; then
    if [[ -n "$_CONF_END" ]]; then
      DIFF_SCOPE="${_CONF_REF}..${_CONF_END}"
    else
      DIFF_SCOPE="${_CONF_REF}..HEAD"
    fi
  else
    echo "⚠️ WARNING: No DIFF_REF — falling back to HEAD~1..HEAD (legacy)" >&2
    DIFF_SCOPE="HEAD~1..HEAD"
  fi
elif [[ "$DIFF_SCOPE" != *".."* ]]; then
  DIFF_SCOPE="${DIFF_SCOPE}..HEAD"
fi

_REF="${DIFF_SCOPE%%\.\.*}"
git rev-parse --verify "$_REF" >/dev/null 2>&1 || fail_env "Ref not in git history: $_REF (scope: $DIFF_SCOPE)"

# Parse ## Files Changed — strip trailing ← Pipeline Artifact annotations, strip backticks, take first field
REPORTED_FILES="$(awk '/^## Files Changed/{found=1;next} found && /^## /{exit} found && /^- /{print substr($0,3)}' "$SV_REPORT" \
  | sed 's/[[:space:]]*←.*$//' \
  | sed 's/[[:space:]]*$//' \
  | sed 's/`//g' \
  | awk 'NF{print $1}')"

# Parse ## Excluded Payload — strip trailing — reason annotations, strip backticks, take first field
EXCLUDED_FILES="$(awk '/^## Excluded Payload/{found=1;next} found && /^## /{exit} found && /^- /{print substr($0,3)}' "$SV_REPORT" \
  | sed 's/[[:space:]]*—.*$//' \
  | sed 's/[[:space:]]*$//' \
  | sed 's/`//g' \
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
[[ -n "$UNDISCLOSED" ]] && { echo "❌ STRUCTURAL FAIL — [B] 漏揭露（diff 有，Report 無）："; printf '%b' "$UNDISCLOSED"; echo "   提示：.claude/PROGRESS.md 須列並標示 ← Pipeline Artifact"; echo "   提示：若列出大量舊 Ticket 檔案，表示 DIFF_REF 過舊 → 執行 verify_checklist.sh 確認 STALE_DIFF_REF，並更新 verify_cmd.conf DIFF_REF 至 Ticket 起點"; FAIL=1; }
[[ -n "$EXCLUDED_NOT_DISCLOSED" ]] && { echo "❌ STRUCTURAL FAIL — [C] Excluded Payload 未在 Files Changed 揭露："; printf '%b' "$EXCLUDED_NOT_DISCLOSED"; echo "   提示：排除的檔案仍須出現在 Files Changed"; FAIL=1; }

if [[ $FAIL -eq 1 ]]; then
  echo "   修正方式：更新 sv_report.md ## Files Changed 使其與 git diff ${DIFF_SCOPE} 精確一致。"
  exit 1
fi

echo "✅ STRUCTURAL VERIFY PASS (A/B/C) — git diff ${DIFF_SCOPE}"

if [[ "$POST_MODE" != "true" ]]; then
  exit 0
fi

# ─── Check D: sv_report 五段結構 ───
echo ""
echo "=== [D] SV Report 五段結構 ==="
_MISSING_SECTIONS=""
for section in "## Files Changed" "## Implementation Scope" "## Stage A" "## Stage B" "## Stage C"; do
  if ! grep -qF "$section" "$SV_REPORT"; then
    _MISSING_SECTIONS="${_MISSING_SECTIONS}  - ${section}\n"
  fi
done
if [[ -n "$_MISSING_SECTIONS" ]]; then
  echo "❌ STRUCTURAL FAIL — [D] SV Report 缺少必要段落："
  printf '%b' "$_MISSING_SECTIONS"
  FAIL=1
else
  echo "✅ SV Report 五段結構完整"
fi

# ─── Check E: cross_review PASS 確認 ───
echo ""
echo "=== [E] Cross-Review PASS 確認 ==="
_TICKET_ID=""
if [[ -f "tmp/verify_cmd.conf" ]]; then
  _TICKET_ID="$(grep "^TICKET_ID=" "tmp/verify_cmd.conf" | head -1 | cut -d'=' -f2- || true)"
fi
if [[ -n "$_TICKET_ID" ]]; then
  _STATE_FILE=".tmp/cross_review_state/${_TICKET_ID}"
  if [[ -f "$_STATE_FILE" ]]; then
    _LAST_VERDICT="$(grep "^LAST=" "$_STATE_FILE" | head -1 | cut -d'=' -f2- || true)"
    if [[ "$_LAST_VERDICT" == "PASS" || "$_LAST_VERDICT" == "ADVISORY" ]]; then
      echo "✅ Cross-Review PASS (Ticket: ${_TICKET_ID}, Verdict: ${_LAST_VERDICT})"
    else
      echo "❌ STRUCTURAL FAIL — [E] Cross-Review 未 PASS (Ticket: ${_TICKET_ID}, Verdict: ${_LAST_VERDICT:-UNKNOWN})"
      FAIL=1
    fi
  else
    echo "⚪ Cross-Review state 不存在 (${_TICKET_ID}) — 跳過 Check E"
  fi
else
  echo "⚪ TICKET_ID 不存在 — 跳過 Check E"
fi

# ─── Check F: PROGRESS.md 格式校驗 ───
echo ""
echo "=== [F] PROGRESS.md 格式校驗 ==="
_PROGRESS=".claude/PROGRESS.md"
if [[ -f "$_PROGRESS" ]]; then
  _MISSING_FIELDS=""
  for field in "**Sprint**" "**Current Day**" "## Sprint Health"; do
    if ! grep -qF "$field" "$_PROGRESS"; then
      _MISSING_FIELDS="${_MISSING_FIELDS}  - ${field}\n"
    fi
  done
  if [[ -n "$_MISSING_FIELDS" ]]; then
    echo "❌ STRUCTURAL FAIL — [F] PROGRESS.md 缺少必要欄位："
    printf '%b' "$_MISSING_FIELDS"
    FAIL=1
  else
    echo "✅ PROGRESS.md 格式正常"
  fi
  # Check F.2: PROGRESS.md 行數閾值
  _PROG_LINES=$(wc -l < "$_PROGRESS")
  if [[ $_PROG_LINES -gt 80 ]]; then
    echo "❌ STRUCTURAL FAIL — [F.2] PROGRESS.md ${_PROG_LINES} 行 > 80（目標 ≤80）→ 執行 PROGRESS 精簡或歸檔舊 Session Notes"
    FAIL=1
  elif [[ $_PROG_LINES -gt 60 ]]; then
    echo "⚠️ WARNING [F.2]: PROGRESS.md ${_PROG_LINES} 行 > 60（接近上限，建議於下輪 Session 精簡）"
  else
    echo "✅ PROGRESS.md 行數正常（${_PROG_LINES} 行 ≤ 60）"
  fi
else
  echo "⚪ PROGRESS.md 不存在 — 跳過 Check F"
fi

# ─── 最終結果 ───
echo ""
if [[ $FAIL -eq 1 ]]; then
  echo "══════════════════════════════════════"
  echo "⛔ STRUCTURAL VERIFY FAIL (D/E/F)"
  echo "   修正後重新執行 structural_verify.sh"
  echo "══════════════════════════════════════"
  exit 1
fi

echo "══════════════════════════════════════"
echo "✅ STRUCTURAL VERIFY PASS (A/B/C/D/E/F)"
echo "══════════════════════════════════════"
exit 0
