#!/usr/bin/env bash
# index_path_verify.sh v2.0 — 角色目錄 + plans/architecture/ 清單列印與雙向覆蓋率驗證
# v2.0 變更：
#   - 🆕 Auto-Index 智慧路由：file/ → 核心文件、reports/ → 審查報告（取代底部泛用區塊）
#   - 🆕 cleanup_blank_lines：strip/insert 後自動壓縮多餘空白行
#   - 🆕 detect_current_versions / check_stale_versions：動態版本偵測取代硬編碼
#   - 🆕 SOP prune 語意告警升級
#
# ① 先列出角色目錄 + plans/architecture/ 所有檔案
# ② INDEX/SOP 引用 → 磁碟存在性 + 角色邊界；缺檔引用自動刪除
# ③ 磁碟檔案 → INDEX/SOP 覆蓋率；未引用者自動路由至對應 Section
# 退出碼：0=全部通過 / 1=有❌錯誤 / 2=環境問題

set -euo pipefail

ROOT="$(git rev-parse --show-toplevel 2>/dev/null)" || { echo "❌ 非 git repo，請在專案根目錄執行"; exit 2; }
cd "$ROOT"

PASS=0
FAIL=0
WARN=0
FIX=0
ADD=0
ROUTE=0
REFS_TMP="tmp/.index_verify_refs.tmp"
FILES_TMP="tmp/.index_verify_files.tmp"
mkdir -p tmp
trap 'rm -f "$REFS_TMP" "$FILES_TMP"' EXIT

# ── 角色路由規則 ─────────────────────────────────────────────────────────────
# 格式: "相對路徑 pattern|Section header 比對字串|預設讀取時機"
# 比對順序：第一個命中的 pattern 生效（由上到下）

CSO_ROUTES=(
  "project/file/|核心文件|按需讀取"
  "reports/|審查報告|追溯歷史裁決"
)

UITA_ROUTES=(
  "file/|核心文件|按需讀取"
  "reports/|審查報告|追溯歷史審查裁決"
)

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

collect_refs() {
  {
    for doc in "$@"; do
      [[ ! -f "$doc" ]] && continue
      grep -oE '`[^`]+`' "$doc" 2>/dev/null | tr -d '`' \
        | grep -E '^(plans/|\.claude/)' | grep -v '[*?]' || true
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

# ── 🆕 空白行清理 ────────────────────────────────────────────────────────────
# 壓縮連續 2+ 空白行為最多 1 行；移除檔尾多餘空白行
cleanup_blank_lines() {
  local doc="$1"
  [[ ! -f "$doc" ]] && return

  local tmp_file
  tmp_file=$(mktemp)

  awk '
    /^[[:space:]]*$/ {
      blank++
      if (blank <= 1) print ""
      next
    }
    { blank = 0; print }
  ' "$doc" > "$tmp_file"

  # 移除檔尾空白行
  sed -i -e :a -e '/^\n*$/{$d;N;ba' -e '}' "$tmp_file" 2>/dev/null || true

  if ! diff -q "$doc" "$tmp_file" >/dev/null 2>&1; then
    mv "$tmp_file" "$doc"
    echo "│  🧹 [BLANK] $doc 多餘空白行已壓縮"
    ((FIX++)) || true
  else
    rm -f "$tmp_file"
  fi
}

# ── 🆕 Section 智慧路由 ──────────────────────────────────────────────────────

# 根據路徑 pattern 判斷檔案應歸屬的 Section header
# $1=file_path  $2=role_dir  $3...=route_rules (array elements)
# 輸出: "section_pattern|timing" 或空字串(fallback)
classify_file_section() {
  local file="$1"
  local role_dir="${2%/}"
  shift 2
  local routes=("$@")

  local relative="${file#${role_dir}/}"

  for rule in "${routes[@]}"; do
    local path_pat="${rule%%|*}"
    local rest="${rule#*|}"
    local section_pat="${rest%%|*}"
    local timing="${rest##*|}"

    if [[ "$relative" == ${path_pat}* ]]; then
      echo "${section_pat}|${timing}"
      return 0
    fi
  done

  # 無匹配 → fallback
  return 1
}

# 在 Markdown 文件的指定 Section table 末尾插入新行
# $1=doc  $2=section_header_pattern  $3=new_table_row
append_to_section_table() {
  local doc="$1"
  local section_pat="$2"
  local new_row="$3"

  local tmp_file
  tmp_file=$(mktemp)

  awk -v pat="$section_pat" -v row="$new_row" '
    BEGIN { found = 0; last_table = 0; inserted = 0 }
    {
      lines[NR] = $0
      n = NR
    }
    !found && index($0, pat) > 0 && /^###/ {
      found = 1
    }
    found && !inserted && /^\|/ {
      last_table = NR
    }
    # Section 結束偵測：找到下一個 ### 或 --- 分隔線（且已經過 table rows）
    found && last_table > 0 && /^###/ && NR > last_table {
      inserted = 1
    }
    found && last_table > 0 && /^---/ && NR > last_table {
      inserted = 1
    }
    END {
      done = 0
      for (i = 1; i <= n; i++) {
        print lines[i]
        if (i == last_table && !done) {
          print row
          done = 1
        }
      }
    }
  ' "$doc" > "$tmp_file"

  mv "$tmp_file" "$doc"
}

# ── 🆕 改造後的 Auto-Index：智慧路由 + fallback ──────────────────────────────

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

# 🆕 核心改造：路由未索引檔案至對應 Section，剩餘才進 fallback Auto-Index
smart_auto_index() {
  local role_name="$1"
  local role_dir="${2%/}"
  local target_doc="$3"
  shift 3
  local docs=("$@")

  # 選擇路由規則
  local routes=()
  case "$role_name" in
    CSO)  routes=("${CSO_ROUTES[@]}") ;;
    UITA) routes=("${UITA_ROUTES[@]}") ;;
    *)    routes=() ;;
  esac

  local fallback_file routed_file old_refs_file
  fallback_file=$(mktemp)
  routed_file=$(mktemp)
  old_refs_file=$(mktemp)

  # 備份舊 refs（用於判斷是否為新增）
  collect_refs "${docs[@]}"
  cp "$REFS_TMP" "$old_refs_file"

  # Strip 舊 Auto-Index block
  strip_generated_block "$target_doc" "${role_name} Role Files"

  # 重新掃描覆蓋率（block 刪除後）
  collect_refs "${docs[@]}"
  collect_files "$role_dir"

  while IFS= read -r file; do
    [[ -z "$file" ]] && continue

    # 跳過 INDEX/SOP 文件本身
    local skip=0
    for doc in "${docs[@]}"; do
      [[ "$file" == "$doc" ]] && skip=1 && break
    done
    [[ $skip -eq 1 ]] && continue

    # 已被覆蓋 → 跳過
    is_covered "$file" && continue

    # 嘗試路由至對應 Section
    local routing=""
    if [[ ${#routes[@]} -gt 0 ]]; then
      routing=$(classify_file_section "$file" "$role_dir" "${routes[@]}") || true
    fi

    if [[ -n "$routing" ]]; then
      local section_pat="${routing%%|*}"
      local timing="${routing##*|}"
      local base
      base=$(basename "$file")
      local short_name="${base%.md}"
      short_name="${short_name%.html}"

      # 建構 4-column 行（配合 INDEX 既有格式：ID | 文件 | 路徑 | 讀取時機）
      local new_row="| ${short_name} | ${base} | \`${file}\` | ${timing} |"

      append_to_section_table "$target_doc" "$section_pat" "$new_row"

      if ! is_covered_in_refs "$file" "$old_refs_file"; then
        echo "│  📌 [ROUTED:${section_pat}] $target_doc 新增: $file"
        ((ROUTE++)) || true
      fi
      echo "$file" >> "$routed_file"
    else
      # 無法路由 → fallback
      echo "$file" >> "$fallback_file"
      if ! is_covered_in_refs "$file" "$old_refs_file"; then
        echo "│  🧾 [FALLBACK] $target_doc fallback 新增: $file"
        ((ADD++)) || true
      fi
    fi
  done < "$FILES_TMP"

  # Fallback Auto-Index block（僅放無法路由的檔案）
  if [[ -s "$fallback_file" ]]; then
    sort -u "$fallback_file" -o "$fallback_file"
    insert_generated_block "$target_doc" "${role_name} Role Files" "$fallback_file"
  fi

  rm -f "$fallback_file" "$routed_file" "$old_refs_file"

  # 重建 refs（含新路由的引用）
  collect_refs "${docs[@]}"
  return 0
}

# ── Prune（v2.0: SOP 語意告警升級）────────────────────────────────────────────

prune_missing_refs() {
  local role_name="$1"
  shift
  local docs=("$@")

  local pruned=0
  for doc in "${docs[@]}"; do
    [[ ! -f "$doc" ]] && continue

    # 🆕 判斷 doc 類型
    local doc_type="INDEX"
    [[ "$doc" == *SOP* || "$doc" == *sop* ]] && doc_type="SOP"

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

      if [[ "$doc_type" == "SOP" ]]; then
        echo "│  🔴 [SOP-PRUNED:${role_name}] $doc 移除語意引用: $path"
        echo "│     ↳ SOP 路徑附帶載入條件，刪除後操作流程可能斷裂，請手動補正"
        ((WARN++)) || true
      else
        echo "│  🧹 [PRUNED:${role_name}] $doc 移除缺檔引用: $path"
      fi
      ((FIX++)) || true
      pruned=1
    done < <(grep -oE '`[^`]+`' "$doc" 2>/dev/null | tr -d '`' \
      | grep -E '^(plans/|\.claude/)' | grep -v '[*?]' | sort -u || true)
  done

  [[ $pruned -eq 0 ]] && echo "│  ✅ 無缺檔引用需刪除"
  return 0
}

# ── 🆕 動態版本偵測 ──────────────────────────────────────────────────────────

CURRENT_SSOT=""
CURRENT_LAYOUT=""

detect_current_versions() {
  CURRENT_SSOT=$(ls plans/architecture/SSOT_v*.md 2>/dev/null \
    | sort -V | tail -1 | xargs -r basename 2>/dev/null | sed 's/\.md$//')
  CURRENT_LAYOUT=$(ls plans/architecture/*Layout_Spec*.md 2>/dev/null \
    | sort -V | tail -1 | xargs -r basename 2>/dev/null | sed 's/\.md$//')

  echo "  📌 Current SSOT: ${CURRENT_SSOT:-UNKNOWN}"
  echo "  📌 Current Layout: ${CURRENT_LAYOUT:-UNKNOWN}"
}

check_stale_versions() {
  local doc="$1"
  [[ ! -f "$doc" ]] && return

  # 掃描 SSOT 版本引用
  if [[ -n "$CURRENT_SSOT" ]]; then
    while IFS= read -r ref; do
      [[ -z "$ref" ]] && continue
      [[ "$ref" == "$CURRENT_SSOT" ]] && continue
      echo "  ⚠️  $doc 含過期 SSOT 引用「$ref」→ 最新為「$CURRENT_SSOT」"
      ((WARN++)) || true
    done < <(grep -oE 'SSOT_v[0-9_]+' "$doc" 2>/dev/null | sort -u)
  fi

  # 掃描 Layout Spec 版本引用
  if [[ -n "$CURRENT_LAYOUT" ]]; then
    while IFS= read -r ref; do
      [[ -z "$ref" ]] && continue
      [[ "$ref" == "$CURRENT_LAYOUT" ]] && continue
      echo "  ⚠️  $doc 含過期 Layout 引用「$ref」→ 最新為「$CURRENT_LAYOUT」"
      ((WARN++)) || true
    done < <(grep -oE '(Cueshot_)?Layout_Spec_v[0-9_]+' "$doc" 2>/dev/null | sort -u)
  fi
}

# ── 角色驗證區塊 ─────────────────────────────────────────────────────────────

check_role() {
  local role_name="$1"
  local role_dir="${2%/}"
  shift 2
  local docs=("$@")

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

  # ── ② 磁碟 → 引用（覆蓋率 + 🆕 智慧路由）────────────
  echo "│"
  echo "│  ── ② 目錄覆蓋率 + 智慧路由: ${role_dir}/ ──"

  collect_files "$role_dir"
  smart_auto_index "$role_name" "$role_dir" "${docs[0]}" "${docs[@]}"

  local unlisted=0
  while IFS= read -r file; do
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

  # 🆕 空白行清理
  echo "│"
  echo "│  ── ③ 空白行清理 ──"
  for doc in "${docs[@]}"; do
    cleanup_blank_lines "$doc"
  done

  echo "└─"
}

# ── plans/architecture/ 區塊 ─────────────────────────────────────────────────

check_architecture() {
  local all_docs=("$@")

  echo ""
  echo "┌─ plans/architecture/ （跨角色共用）"
  echo "│  引用文件: 所有 INDEX + SOP"

  collect_refs "${all_docs[@]}"

  echo "│"
  echo "│  ── ② 目錄覆蓋率: plans/architecture/ ──"

  collect_files "plans/architecture"

  # Architecture auto-index 仍用 fallback block（跨角色，無 Section 路由）
  local fallback_file old_refs_file
  fallback_file=$(mktemp)
  old_refs_file=$(mktemp)
  cp "$REFS_TMP" "$old_refs_file"

  # Strip + rebuild for both INDEXes
  for idx_doc in "plans/cso_briefing/CSO_INDEX.md" "plans/uita_project/UITA_INDEX.md"; do
    strip_generated_block "$idx_doc" "Architecture Files"
  done
  collect_refs "${all_docs[@]}"

  > "$fallback_file"
  while IFS= read -r file; do
    if ! is_covered "$file"; then
      echo "$file" >> "$fallback_file"
      if ! is_covered_in_refs "$file" "$old_refs_file"; then
        echo "│  🧾 [AUTO-INDEX:Architecture] 新增引用: $file"
        ((ADD++)) || true
      fi
    fi
  done < "$FILES_TMP"

  if [[ -s "$fallback_file" ]]; then
    sort -u "$fallback_file" -o "$fallback_file"
    insert_generated_block "plans/cso_briefing/CSO_INDEX.md"  "Architecture Files" "$fallback_file"
    insert_generated_block "plans/uita_project/UITA_INDEX.md" "Architecture Files" "$fallback_file"
  fi

  rm -f "$fallback_file" "$old_refs_file"
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

  # 🆕 空白行清理
  echo "│"
  echo "│  ── ③ 空白行清理 ──"
  cleanup_blank_lines "plans/cso_briefing/CSO_INDEX.md"
  cleanup_blank_lines "plans/uita_project/UITA_INDEX.md"

  echo "└─"
}

# ── Main ─────────────────────────────────────────────────────────────────────
echo "=================================================="
echo " INDEX Path Verify v2.0 — $(date '+%Y-%m-%d %H:%M')"
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

# ── 🆕 動態過期引用掃描 ──────────────────────────────────────────────────────
echo ""
echo "[ 過期引用掃描 (動態版本偵測) ]"
detect_current_versions

for doc in .claude/CLAUDE.md .claude/PROGRESS.md "${ALL_DOCS[@]}"; do
  check_stale_versions "$doc"
done

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
echo " 摘要: ✅ $PASS 通過 / ❌ $FAIL 失敗 / ⚠️  $WARN 警告 / 🧹 $FIX 自動修正 / 📌 $ROUTE 路由新增 / 🧾 $ADD fallback 新增"
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
