#!/usr/bin/env bash
# index_path_verify.sh — 角色目錄 + plans/architecture/ 清單列印與雙向覆蓋率驗證
# ① 先列出角色目錄 + plans/architecture/ 所有檔案
# ② INDEX/SOP 引用 → 磁碟存在性 + 角色邊界；缺檔引用自動刪除
# ③ 磁碟檔案 → INDEX/SOP 覆蓋率；未引用者自動新增至 INDEX
# 退出碼：0=全部通過 / 1=有❌錯誤 / 2=環境問題

set -euo pipefail

ROOT="$(git rev-parse --show-toplevel 2>/dev/null)" || { echo "❌ 非 git repo，請在專案根目錄執行"; exit 2; }
cd "$ROOT"

PASS=0
FAIL=0
WARN=0
FIX=0
ADD=0
REFS_TMP="tmp/.index_verify_refs.tmp"
FILES_TMP="tmp/.index_verify_files.tmp"
mkdir -p tmp
trap 'rm -f "$REFS_TMP" "$FILES_TMP"' EXIT

# ── Helpers ──────────────────────────────────────────────────────────────────

is_cross_role_ok() {
  local path="$1"
  [[ "$path" == plans/architecture/* || "$path" == plans/architecture \
  || "$path" == .claude/*            || "$path" == .claude ]]
}

is_in_role_dir() {
  local path="${1%/}"
  local role_dir="${2%/}"
  [[ "$path" == "${role_dir}/"* || "$path" == "$role_dir" ]]
}

# 從 docs 列表提取所有反引號路徑（plans/ 或 .claude/），去重後寫入 REFS_TMP
collect_refs() {
  {
    for doc in "$@"; do
      [[ ! -f "$doc" ]] && continue
      grep -oE '`[^`]+`' "$doc" 2>/dev/null | tr -d '`' \
        | grep -E '^(plans/|\.claude/)' | grep -v '[*?]'
    done
  } | sort -u > "$REFS_TMP"
}

collect_files() {
  local dir="$1"
  find "$dir" -type f 2>/dev/null | sed 's|^\./||' | sort > "$FILES_TMP"
}

list_files() {
  local label="$1"
  local dir="$2"

  echo ""
  echo "┌─ 檔案清單: ${label} (${dir}/)"
  if [[ ! -d "$dir" ]]; then
    echo "│  ❌ 目錄不存在: $dir"
    ((FAIL++)) || true
    echo "└─"
    return
  fi

  local found=0
  while IFS= read -r file; do
    echo "│  $file"
    found=1
  done < <(find "$dir" -type f 2>/dev/null | sed 's|^\./||' | sort)

  [[ $found -eq 0 ]] && echo "│  ⚪ 無檔案"
  echo "└─"
}

strip_generated_block() {
  local doc="$1"
  local title="$2"
  local tmp_file
  tmp_file=$(mktemp)

  awk -v title="$title" '
    $0 == "<!-- BEGIN AUTO-INDEX: " title " -->" { skip = 1; next }
    $0 == "<!-- END AUTO-INDEX: " title " -->" { skip = 0; next }
    skip != 1 { print }
  ' "$doc" > "$tmp_file"
  mv "$tmp_file" "$doc"
}

insert_generated_block() {
  local doc="$1"
  local title="$2"
  local refs_file="$3"

  [[ -s "$refs_file" ]] || return 0
  [[ -f "$doc" ]] || return 0

  strip_generated_block "$doc" "$title"

  local block_file
  block_file=$(mktemp)
  {
    echo ""
    echo "<!-- BEGIN AUTO-INDEX: ${title} -->"
    echo "### Auto Index: ${title}"
    echo ""
    echo "| 文件 | Filesystem 路徑 | 讀取時機 |"
    echo "|------|----------------|---------|"
    while IFS= read -r file; do
      [[ -z "$file" ]] && continue
      local base
      base=$(basename "$file")
      echo "| ${base} | \`${file}\` | 按需讀取 |"
    done < "$refs_file"
    echo "<!-- END AUTO-INDEX: ${title} -->"
  } > "$block_file"

  local tmp_file
  tmp_file=$(mktemp)
  awk -v block="$block_file" '
    BEGIN {
      inserted = 0
      while ((getline line < block) > 0) {
        generated = generated line ORS
      }
      close(block)
    }
    !inserted && $0 ~ /^\*\*End of / {
      printf "%s\n", generated
      inserted = 1
    }
    { print }
    END {
      if (!inserted) {
        printf "%s", generated
      }
    }
  ' "$doc" > "$tmp_file"
  mv "$tmp_file" "$doc"
  rm -f "$block_file"
}

auto_index_unlisted() {
  local title="$1"
  local target_doc="$2"
  shift 2
  local docs=("$@")
  local refs_file
  local old_refs_file
  refs_file=$(mktemp)
  old_refs_file=$(mktemp)

  collect_refs "${docs[@]}"
  cp "$REFS_TMP" "$old_refs_file"

  # Strip existing AUTO-INDEX block before recomputing generated coverage.
  # ADD is incremented only when the original docs did not already cover the file.
  strip_generated_block "$target_doc" "$title"
  collect_refs "${docs[@]}"
  while IFS= read -r file; do
    [[ -z "$file" ]] && continue
    local skip=0
    for doc in "${docs[@]}"; do
      [[ "$file" == "$doc" ]] && skip=1 && break
    done
    [[ $skip -eq 1 ]] && continue

    if ! is_covered "$file"; then
      echo "$file" >> "$refs_file"
      if ! is_covered_in_refs "$file" "$old_refs_file"; then
        echo "│  🧾 [AUTO-INDEX:${title}] $target_doc 新增引用: $file"
        ((ADD++)) || true
      fi
    fi
  done < "$FILES_TMP"

  if [[ -s "$refs_file" ]]; then
    sort -u "$refs_file" -o "$refs_file"
    insert_generated_block "$target_doc" "$title" "$refs_file"
  fi

  rm -f "$refs_file" "$old_refs_file"
  collect_refs "${docs[@]}"
  return 0
}

prune_missing_refs() {
  local role_name="$1"
  shift
  local docs=("$@")

  local pruned=0
  for doc in "${docs[@]}"; do
    [[ ! -f "$doc" ]] && continue

    while IFS= read -r path; do
      [[ -z "$path" ]] && continue
      local clean="${path%/}"
      [[ -e "$clean" ]] && continue

      local tmp_file
      tmp_file=$(mktemp)
      awk -v ref="$path" '
        index($0, "`" ref "`") {
          if ($0 ~ /^[[:space:]]*\|/ || $0 ~ /^[[:space:]]*[-*][[:space:]]/) {
            next
          }
          gsub("`" ref "`", "")
        }
        { print }
      ' "$doc" > "$tmp_file"
      mv "$tmp_file" "$doc"

      echo "│  🧹 [PRUNED:${role_name}] $doc 移除缺檔引用: $path"
      ((FIX++)) || true
      pruned=1
    done < <(grep -oE '`[^`]+`' "$doc" 2>/dev/null | tr -d '`' \
      | grep -E '^(plans/|\.claude/)' | grep -v '[*?]' | sort -u)
  done

  [[ $pruned -eq 0 ]] && echo "│  ✅ 無缺檔引用需刪除"
  return 0
}

# 磁碟檔案是否被 REFS_TMP 中的引用覆蓋？
# 覆蓋條件：完全匹配 OR 任一祖先目錄被引用（含尾部 /）
is_covered_in_refs() {
  local file="$1"
  local refs_file="$2"
  grep -qxF "$file"       "$refs_file" 2>/dev/null && return 0
  grep -qxF "${file}/"    "$refs_file" 2>/dev/null && return 0
  local parent="$file"
  while true; do
    parent=$(dirname "$parent")
    [[ "$parent" == "." || "$parent" == "" ]] && break
    grep -qxF "${parent}/" "$refs_file" 2>/dev/null && return 0
    grep -qxF "$parent"    "$refs_file" 2>/dev/null && return 0
  done
  return 1
}

is_covered() {
  is_covered_in_refs "$1" "$REFS_TMP"
}

check_no_ref() {
  local file="$1" pattern="$2" fix="$3"
  if grep -q "$pattern" "$file" 2>/dev/null; then
    echo "  ⚠️  $file 含過期引用「$pattern」→ 應改為「$fix」"
    ((WARN++)) || true
  fi
}

# ── 角色區塊 ─────────────────────────────────────────────────────────────────
# $1=role_name  $2=role_dir  $3...=INDEX/SOP 文件
check_role() {
  local role_name="$1"
  local role_dir="${2%/}"
  shift 2
  local docs=("$@")

  # 顯示文件名稱
  local doc_names=""
  for d in "${docs[@]}"; do
    doc_names="${doc_names}$(basename "$d"), "
  done

  echo ""
  echo "┌─ ${role_name} 角色: ${role_dir}/"
  echo "│  引用文件: ${doc_names%, }"

  echo "│"
  echo "│  ── ⓪ 缺檔引用清理 ──"
  prune_missing_refs "$role_name" "${docs[@]}"

  collect_refs "${docs[@]}"

  # ── ① 引用 → 磁碟（存在性 + 角色邊界）──────────────
  echo "│"
  echo "│  ── ① INDEX/SOP 引用驗證 ──"
  while IFS= read -r path; do
    [[ -z "$path" ]] && continue
    local clean="${path%/}"
    if [[ -e "$clean" ]]; then
      echo "│  ✅ $path"
      ((PASS++)) || true
    else
      echo "│  ❌ $path  ← 不存在"
      ((FAIL++)) || true
    fi
    if ! is_cross_role_ok "$path" && ! is_in_role_dir "$path" "$role_dir"; then
      echo "│  ⚠️  [CROSS_ROLE] $path  ← 跨角色引用（應在 ${role_dir}/ 或 plans/architecture/）"
      ((WARN++)) || true
    fi
  done < "$REFS_TMP"

  # ── ② 磁碟 → 引用（覆蓋率）──────────────────────────
  echo "│"
  echo "│  ── ② 目錄覆蓋率: ${role_dir}/ ──"

  collect_files "$role_dir"
  auto_index_unlisted "${role_name} Role Files" "${docs[0]}" "${docs[@]}"

  local unlisted=0
  while IFS= read -r file; do
    # 跳過 INDEX/SOP 文件本身
    local skip=0
    for doc in "${docs[@]}"; do
      [[ "$file" == "$doc" ]] && skip=1 && break
    done
    [[ $skip -eq 1 ]] && continue

    if is_covered "$file"; then
      echo "│  ✅ [INDEXED] $file"
    else
      echo "│  ⚠️  [UNLISTED] $file  ← 存在但未被 INDEX/SOP 引用"
      ((WARN++)) || true
      unlisted=1
    fi
  done < "$FILES_TMP"

  [[ $unlisted -eq 0 ]] && echo "│  ✅ 所有檔案均已被引用"
  echo "└─"
}

# ── plans/architecture/ 區塊 ─────────────────────────────────────────────────
# 對照所有 INDEX/SOP（任一角色皆可引用架構文件）
check_architecture() {
  local all_docs=("$@")

  echo ""
  echo "┌─ plans/architecture/ （跨角色共用）"
  echo "│  引用文件: 所有 INDEX + SOP"

  collect_refs "${all_docs[@]}"

  echo "│"
  echo "│  ── ② 目錄覆蓋率: plans/architecture/ ──"

  collect_files "plans/architecture"
  auto_index_unlisted "Architecture Files" "plans/cso_briefing/CSO_INDEX.md" "plans/cso_briefing/CSO_INDEX.md"
  auto_index_unlisted "Architecture Files" "plans/uita_project/UITA_INDEX.md" "plans/uita_project/UITA_INDEX.md"
  collect_refs "${all_docs[@]}"

  local unlisted=0
  while IFS= read -r file; do
    if is_covered "$file"; then
      echo "│  ✅ [INDEXED] $file"
    else
      echo "│  ⚠️  [UNLISTED] $file  ← 存在但未被任何 INDEX/SOP 引用"
      ((WARN++)) || true
      unlisted=1
    fi
  done < "$FILES_TMP"

  [[ $unlisted -eq 0 ]] && echo "│  ✅ 所有架構文件均已被引用"
  echo "└─"
}

# ── Main ─────────────────────────────────────────────────────────────────────
echo "=================================================="
echo " INDEX Path Verify — $(date '+%Y-%m-%d %H:%M')"
echo "=================================================="

CSO_DOCS=("plans/cso_briefing/CSO_INDEX.md" "plans/cso_briefing/CSO_Session_Startup_SOP.md")
UITA_DOCS=("plans/uita_project/UITA_INDEX.md" "plans/uita_project/UITA_Session_Startup_SOP.md")
ALL_DOCS=("${CSO_DOCS[@]}" "${UITA_DOCS[@]}")

echo ""
echo "[ Scope File Inventory ]"
list_files "CSO 角色目錄" "plans/cso_briefing"
list_files "UITA 角色目錄" "plans/uita_project"
list_files "Architecture 共用目錄" "plans/architecture"

check_role "CSO"  "plans/cso_briefing" "${CSO_DOCS[@]}"
check_role "UITA" "plans/uita_project" "${UITA_DOCS[@]}"
check_architecture "${ALL_DOCS[@]}"

# ── 過期版本引用掃描 ─────────────────────────────────────────────────────────
echo ""
echo "[ 過期引用掃描 ]"
check_no_ref ".claude/CLAUDE.md"   "SSOT_v4_0_2"      "SSOT_v4_0_5"
check_no_ref ".claude/CLAUDE.md"   "Layout_Spec_v2_3" "Cueshot_Layout_Spec_v2_5_1"
check_no_ref ".claude/PROGRESS.md" "SSOT_v4_0_2"      "SSOT_v4_0_5"
check_no_ref "plans/cso_briefing/CSO_INDEX.md"  "SSOT_v4_0_2" "SSOT_v4_0_5"
check_no_ref "plans/uita_project/UITA_INDEX.md" "SSOT_v4_0_2" "SSOT_v4_0_5"

# ── 大小寫重複偵測 ───────────────────────────────────────────────────────────
echo ""
echo "[ 大小寫重複偵測 ]"
SOP_LOWER="plans/uita_project/UITA_Session_Startup_SOP.md"
SOP_UPPER="plans/uita_project/UITA_SESSION_STARTUP_SOP.md"
_GIT_LOWER=$(git ls-files "$SOP_LOWER" 2>/dev/null)
_GIT_UPPER=$(git ls-files "$SOP_UPPER" 2>/dev/null)
if [[ -n "$_GIT_LOWER" && -n "$_GIT_UPPER" ]]; then
  echo "  ⚠️  UITA SOP 大小寫重複（git tracked）：$SOP_LOWER 與 $SOP_UPPER 並存，建議保留一份"
  ((WARN++)) || true
else
  echo "  ✅ UITA SOP 無大小寫重複（git tracked 唯一）"
fi

# ── 摘要 ─────────────────────────────────────────────────────────────────────
echo ""
echo "=================================================="
echo " 摘要: ✅ $PASS 通過 / ❌ $FAIL 失敗 / ⚠️  $WARN 警告 / 🧹 $FIX 自動刪除 / 🧾 $ADD 自動新增"
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
