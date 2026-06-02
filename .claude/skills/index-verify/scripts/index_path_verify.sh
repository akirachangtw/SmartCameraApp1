#!/usr/bin/env bash
# index_path_verify.sh — INDEX 文件路徑存在性 + 過期引用驗證
# 用途：日常維運確認 CSO_INDEX.md / UITA_INDEX.md 所有引用路徑均有效
# 退出碼：0=全部通過 / 1=有❌錯誤 / 2=環境問題

set -euo pipefail

ROOT="$(git rev-parse --show-toplevel 2>/dev/null)" || { echo "❌ 非 git repo，請在專案根目錄執行"; exit 2; }
cd "$ROOT"

PASS=0
FAIL=0
WARN=0

check_path() {
  local label="$1"
  local path="$2"
  if [[ -e "$path" ]]; then
    echo "  ✅ [$label] $path"
    ((PASS++)) || true
  else
    echo "  ❌ [$label] $path  ← 不存在"
    ((FAIL++)) || true
  fi
}

check_no_ref() {
  local file="$1"
  local pattern="$2"
  local fix="$3"
  if grep -q "$pattern" "$file" 2>/dev/null; then
    echo "  ⚠️  $file 含過期引用「$pattern」→ 應改為「$fix」"
    ((WARN++)) || true
  fi
}

echo "=================================================="
echo " INDEX Path Verify — $(date '+%Y-%m-%d %H:%M')"
echo "=================================================="

# ── CSO_INDEX.md 引用路徑 ───────────────────────────
echo ""
echo "[ CSO_INDEX.md ]"
check_path "SSOT"        "plans/architecture/SSOT_v4_0_5.md"
check_path "LAYOUT"      "plans/architecture/Cueshot_Layout_Spec_v2_5_1.md"
check_path "KB"          "plans/cso_briefing/project/file/Cueshot_CSO_Knowledge_Base_v2.md"
check_path "CONTRACT"    "plans/cso_briefing/project/file/P08_Codebase_Sanitization_Contract.md"
check_path "EFFORT"      "plans/cso_briefing/project/file/Sonnet4.6-Effort等級解析.md"
check_path "TICKET_TPL"  "plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md"
check_path "CLAUDE"      ".claude/CLAUDE.md"
check_path "PROGRESS"    ".claude/PROGRESS.md"
check_path "PENDING"     "plans/cso_briefing/project/Pending"
check_path "POSTPONED"   "plans/cso_briefing/project/Postponed.md"
check_path "SOP"         "plans/cso_briefing/CSO_Session_Startup_SOP.md"
check_path "BHA"         ".claude/bha_report.md"
check_path "AUDIT"       "plans/cso_briefing/reports/full_audit_and_tickets.md"
check_path "SCORCHED"    "plans/cso_briefing/reports/scorched_earth_report.md"
check_path "ARCHIVE"     "plans/cso_briefing/archive"

# ── UITA_INDEX.md 引用路徑 ──────────────────────────
echo ""
echo "[ UITA_INDEX.md ]"
check_path "SSOT"        "plans/architecture/SSOT_v4_0_5.md"
check_path "LAYOUT"      "plans/architecture/Cueshot_Layout_Spec_v2_5_1.md"
check_path "SOP"         "plans/uita_project/UITA_Session_Startup_SOP.md"
check_path "REPORTS"     "plans/uita_project/reports"
check_path "FILE"        "plans/uita_project/file"

# ── 過期版本引用掃描（不修改，僅警告）──────────────
echo ""
echo "[ 過期引用掃描 ]"
check_no_ref ".claude/CLAUDE.md"   "SSOT_v4_0_2"         "SSOT_v4_0_5"
check_no_ref ".claude/CLAUDE.md"   "Layout_Spec_v2_3"    "Cueshot_Layout_Spec_v2_5_1"
check_no_ref ".claude/PROGRESS.md" "SSOT_v4_0_2"         "SSOT_v4_0_5"
check_no_ref "plans/cso_briefing/CSO_INDEX.md"   "SSOT_v4_0_2" "SSOT_v4_0_5"
check_no_ref "plans/uita_project/UITA_INDEX.md"  "SSOT_v4_0_2" "SSOT_v4_0_5"

# ── 大小寫重複檔案偵測 ──────────────────────────────
echo ""
echo "[ 大小寫重複偵測 ]"
SOP_LOWER="plans/uita_project/UITA_Session_Startup_SOP.md"
SOP_UPPER="plans/uita_project/UITA_SESSION_STARTUP_SOP.md"
# 用 git ls-files 做大小寫精確比對（Windows NTFS -e 不分大小寫，無法使用）
_GIT_LOWER=$(git ls-files "$SOP_LOWER" 2>/dev/null)
_GIT_UPPER=$(git ls-files "$SOP_UPPER" 2>/dev/null)
if [[ -n "$_GIT_LOWER" && -n "$_GIT_UPPER" ]]; then
  echo "  ⚠️  UITA SOP 大小寫重複（git tracked）：$SOP_LOWER 與 $SOP_UPPER 並存，建議保留一份"
  ((WARN++)) || true
else
  echo "  ✅ UITA SOP 無大小寫重複（git tracked 唯一）"
fi

# ── 摘要 ───────────────────────────────────────────
echo ""
echo "=================================================="
echo " 摘要: ✅ $PASS 通過 / ❌ $FAIL 失敗 / ⚠️  $WARN 警告"
echo "=================================================="
echo ""

if [[ $FAIL -gt 0 ]]; then
  echo "❌ 路徑驗證 FAIL：請修正以上 ❌ 項目後重新執行"
  exit 1
elif [[ $WARN -gt 0 ]]; then
  echo "⚠️  路徑全數有效，但存在警告項目（不阻斷流程）"
  exit 0
else
  echo "✅ 所有路徑有效，無警告"
  exit 0
fi
