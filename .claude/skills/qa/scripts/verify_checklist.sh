#!/usr/bin/env bash
# /qa Step 0 — Ticket Scope 驗證 v2（DIFF_REF 四段式合約）
# 來源: INFRA-17 DIFF_REF 精準性治理
#
# 用法（conf 模式）:
#   bash .claude/skills/qa/scripts/verify_checklist.sh tmp/verify_cmd.conf
# 用法（CLI 模式）:
#   bash .claude/skills/qa/scripts/verify_checklist.sh \
#     --allowed "File1.kt,File2.kt" --diff-ref HEAD~1 --max-lines 100
#
# DIFF_REF 合約：
#   DIFF_REF 必須等於 Ticket 開工前的 git HEAD（精確起點）。
#   DIFF_REF..HEAD 只能包含 ALLOWED + Pipeline Artifact + Excluded Payload。
#   其他檔案 → STALE_DIFF_REF（base 過舊）或 SCOPE_VIOLATION（真實越權）。
#
# 退出碼: 0 = PASS, 1 = FAIL (STALE_DIFF_REF or SCOPE_VIOLATION)

set -u

# ─── Pipeline Artifact 白名單（永不觸發 SCOPE_VIOLATION） ───
PIPELINE_ARTIFACT=".claude/PROGRESS.md"

DEFAULT_FORBIDDEN="CameraViewModel.kt"

get_conf() {
  local key="$1" file="$2"
  grep "^${key}=" "$file" | head -1 | cut -d'=' -f2-
}

ALLOWED=""
FORBIDDEN="$DEFAULT_FORBIDDEN"
EXCLUDED_PAYLOAD=""
MAX_LINES=999
DIFF_REF="HEAD~1"
TICKET_ID=""
CREATED=""
CONF_MODE=0

# ─── 參數解析 ───
if [[ $# -ge 1 && "$1" == *.conf ]]; then
  CONF_FILE="$1"
  [[ -r "$CONF_FILE" ]] || { echo "❌ conf file not found: $CONF_FILE"; exit 1; }
  CONF_MODE=1
  TICKET_ID=$(get_conf "TICKET_ID" "$CONF_FILE")
  CREATED=$(get_conf "CREATED" "$CONF_FILE")
  ALLOWED=$(get_conf "ALLOWED" "$CONF_FILE")
  FORBIDDEN_CONF=$(get_conf "FORBIDDEN" "$CONF_FILE")
  [[ -n "$FORBIDDEN_CONF" ]] && FORBIDDEN="$FORBIDDEN_CONF"
  EXCLUDED_PAYLOAD_CONF=$(get_conf "EXCLUDED_PAYLOAD" "$CONF_FILE")
  [[ -n "$EXCLUDED_PAYLOAD_CONF" ]] && EXCLUDED_PAYLOAD="$EXCLUDED_PAYLOAD_CONF"
  MAX_LINES_CONF=$(get_conf "MAX_LINES" "$CONF_FILE")
  [[ -n "$MAX_LINES_CONF" ]] && MAX_LINES="$MAX_LINES_CONF"
  DIFF_REF_CONF=$(get_conf "DIFF_REF" "$CONF_FILE")
  [[ -n "$DIFF_REF_CONF" ]] && DIFF_REF="$DIFF_REF_CONF"
else
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --allowed)          ALLOWED="$2";          shift 2 ;;
      --forbidden)        FORBIDDEN="$2";        shift 2 ;;
      --excluded-payload) EXCLUDED_PAYLOAD="$2"; shift 2 ;;
      --max-lines)        MAX_LINES="$2";        shift 2 ;;
      --diff-ref)         DIFF_REF="$2";         shift 2 ;;
      *) echo "Unknown option: $1"; exit 1 ;;
    esac
  done
fi

git rev-parse --verify "$DIFF_REF" >/dev/null 2>&1 || { echo "❌ DIFF_REF not found in git history: $DIFF_REF"; exit 1; }

echo "# /qa Step 0 — Ticket Scope 驗證"
echo "  diff-ref: $DIFF_REF"
echo "  allowed: ${ALLOWED:-<any>}"
echo "  forbidden: $FORBIDDEN"
echo "  max-lines: $MAX_LINES"
echo

FAIL=0
STALE=0
STALE_DIFF_REF_FILES=""

# ─── 取得 DIFF_REF..HEAD 全量變更 ───
if [[ $CONF_MODE -eq 1 ]]; then
  CHANGED=$(git diff --name-only "${DIFF_REF}..HEAD" 2>/dev/null || true)
else
  CHANGED=$(git diff --name-only "$DIFF_REF" 2>/dev/null || true)
fi

if [[ -z "$CHANGED" ]]; then
  echo "⚪ 無變更檔案（diff 為空）"
  echo "=== ✅ Step 0 PASS（無變更） ==="
  exit 0
fi

# ─── [0.0] DIFF_REF Scope Preview ───
echo "=== [0.0] DIFF_REF Scope Preview ==="
BASE_SHORT=$(git rev-parse --short "$DIFF_REF" 2>/dev/null || echo "?")
HEAD_SHORT=$(git rev-parse --short HEAD 2>/dev/null || echo "?")
COMMIT_COUNT=$(git log --oneline "${DIFF_REF}..HEAD" 2>/dev/null | wc -l | tr -d ' ')
echo "  base: $BASE_SHORT → $HEAD_SHORT ($COMMIT_COUNT commits in scope)"

# Find ticket anchor: first commit in DIFF_REF..HEAD touching any ALLOWED file
# (--reverse = oldest first → TICKET_BASE_COMMIT = oldest ALLOWED-touching commit)
TICKET_BASE_COMMIT=""
SUGGESTED_BASE=""
if [[ -n "$ALLOWED" && $CONF_MODE -eq 1 ]]; then
  IFS=',' read -ra _AARR_PREVIEW <<< "$ALLOWED"
  while IFS= read -r _cmt; do
    _cmt_files=$(git diff-tree --no-commit-id -r --name-only "$_cmt" 2>/dev/null || true)
    for _a in "${_AARR_PREVIEW[@]}"; do
      _abase=$(basename "$_a")
      if echo "$_cmt_files" | grep -qF "$_abase"; then
        TICKET_BASE_COMMIT="$_cmt"
        break 2
      fi
    done
  done < <(git log --reverse --format="%h" "${DIFF_REF}..HEAD" 2>/dev/null || true)

  if [[ -n "$TICKET_BASE_COMMIT" ]]; then
    SUGGESTED_BASE=$(git rev-parse --short "${TICKET_BASE_COMMIT}^" 2>/dev/null || true)
    echo "  ticket anchor: $TICKET_BASE_COMMIT (first ALLOWED-touching commit)"
    [[ -n "$SUGGESTED_BASE" ]] && echo "  ✅ Suggested DIFF_REF: $SUGGESTED_BASE"
  else
    echo "  ⚠️ No ALLOWED-file commits found in ${BASE_SHORT}..${HEAD_SHORT}"
  fi
fi
echo

# ─── Helper: is_stale_commit ───
# Returns 0 (true) if a commit is a strict ancestor of TICKET_BASE_COMMIT
# i.e. it was made BEFORE the ticket's first ALLOWED commit
is_stale_commit() {
  local cmt="$1"
  [[ -z "$TICKET_BASE_COMMIT" ]] && return 1
  [[ "$cmt" == "$TICKET_BASE_COMMIT" ]] && return 1
  git merge-base --is-ancestor "$cmt" "$TICKET_BASE_COMMIT" 2>/dev/null
}

# ─── [0.1] 允許檔案範圍 ───
echo "=== [0.1] 允許檔案範圍 ==="

if [[ -n "$ALLOWED" ]]; then
  IFS=',' read -ra AARR <<< "$ALLOWED"

  while IFS= read -r f; do
    [[ -z "$f" ]] && continue
    BASENAME=$(basename "$f")

    # Check: Pipeline Artifact (never a violation)
    if [[ "$f" == "$PIPELINE_ARTIFACT" ]]; then
      echo "  📌 Pipeline Artifact: $f"
      continue
    fi

    # Check: ALLOWED
    MATCH=0
    for a in "${AARR[@]}"; do
      A_BASENAME=$(basename "$a")
      if [[ "$BASENAME" == "$A_BASENAME" ]] || [[ "$f" == "$a" ]]; then
        MATCH=1; break
      fi
    done
    if [[ $MATCH -eq 1 ]]; then
      echo "  ✅ ALLOWED: $BASENAME"
      continue
    fi

    # Check: EXCLUDED_PAYLOAD (conf key or CLI flag)
    EXCL_MATCH=0
    if [[ -n "$EXCLUDED_PAYLOAD" ]]; then
      IFS=',' read -ra EXCL_ARR <<< "$EXCLUDED_PAYLOAD"
      for excl in "${EXCL_ARR[@]}"; do
        EXCL_BASE=$(basename "$excl")
        if [[ "$BASENAME" == "$EXCL_BASE" ]] || [[ "$f" == "$excl" ]]; then
          EXCL_MATCH=1; break
        fi
      done
    fi
    if [[ $EXCL_MATCH -eq 1 ]]; then
      echo "  📋 Excluded Payload: $BASENAME (authorized)"
      continue
    fi

    # Unexpected file: classify as STALE_DIFF_REF or genuine SCOPE_VIOLATION
    _file_commit=$(git log --format="%h" -1 "${DIFF_REF}..HEAD" -- "$f" 2>/dev/null | head -1 || true)
    if [[ -n "$_file_commit" ]] && is_stale_commit "$_file_commit"; then
      STALE_DIFF_REF_FILES="${STALE_DIFF_REF_FILES}  ⚠️ [stale] $BASENAME (commit: $_file_commit)\n"
      STALE=1
    else
      echo "  ⛔ SCOPE VIOLATION: $BASENAME (不在允許清單)"
      FAIL=1
    fi
  done <<< "$CHANGED"

  if [[ $FAIL -eq 0 && $STALE -eq 0 ]]; then
    echo "  ✅ 所有變更檔案在允許範圍內"
  fi

  if [[ $STALE -eq 1 ]]; then
    echo
    echo "  ⚠️ STALE_DIFF_REF: 以下檔案來自舊 Ticket，非本 Ticket 變更："
    printf '%b' "$STALE_DIFF_REF_FILES"
    if [[ -n "$SUGGESTED_BASE" ]]; then
      echo "  💡 修正: 更新 verify_cmd.conf → DIFF_REF=$SUGGESTED_BASE（ticket 真正起點）"
    fi
    FAIL=1
  fi
else
  echo "  ⚪ 未指定 --allowed，跳過範圍檢查"
fi

# ─── [0.2] 禁區檔案 ───
echo
echo "=== [0.2] 禁區檔案 ==="
if [[ -n "$FORBIDDEN" ]]; then
  IFS=',' read -ra FARR <<< "$FORBIDDEN"
  FORBIDDEN_FAIL=0
  for fb in "${FARR[@]}"; do
    fb_trimmed=$(echo "$fb" | xargs)
    if echo "$CHANGED" | grep -q "$fb_trimmed"; then
      # Apply STALE classification to forbidden files as well
      _fb_commit=$(git log --format="%h" -1 "${DIFF_REF}..HEAD" -- "*/$fb_trimmed" "$fb_trimmed" 2>/dev/null | head -1 || true)
      if [[ -n "$_fb_commit" ]] && is_stale_commit "$_fb_commit"; then
        echo "  ⚠️ [stale] FORBIDDEN $fb_trimmed (pre-ticket commit $_fb_commit — not this ticket)"
      else
        echo "  ⛔ FORBIDDEN FILE: $fb_trimmed 被修改"
        FAIL=1
        FORBIDDEN_FAIL=1
      fi
    fi
  done
  [[ $FORBIDDEN_FAIL -eq 0 ]] && echo "  ✅ 無禁區檔案被修改（本 Ticket）"
else
  echo "  ⚪ 無禁區定義"
fi

# ─── [0.3] 新增行數（僅計算 ALLOWED 檔案範圍） ───
echo
echo "=== [0.3] 新增行數 ==="
if [[ -n "$ALLOWED" ]]; then
  # Count lines only for ALLOWED files — ignore stale payload bloat
  ADDED=0
  for a in "${AARR[@]}"; do
    _abase=$(basename "$a")
    if [[ $CONF_MODE -eq 1 ]]; then
      _lines=$(git diff "${DIFF_REF}..HEAD" --numstat 2>/dev/null | \
        awk -v n="$_abase" '$3 ~ n {s+=$1} END {print s+0}')
    else
      _lines=$(git diff "$DIFF_REF" --numstat 2>/dev/null | \
        awk -v n="$_abase" '$3 ~ n {s+=$1} END {print s+0}')
    fi
    ADDED=$((ADDED + ${_lines:-0}))
  done
  echo "  (ALLOWED files only)"
else
  if [[ $CONF_MODE -eq 1 ]]; then
    ADDED=$(git diff "${DIFF_REF}..HEAD" --numstat 2>/dev/null | awk '{s+=$1} END {print s+0}')
  else
    ADDED=$(git diff "$DIFF_REF" --numstat 2>/dev/null | awk '{s+=$1} END {print s+0}')
  fi
fi
if [[ "$ADDED" -gt "$MAX_LINES" ]]; then
  echo "  ⛔ BLOAT: +${ADDED} 行（上限: ${MAX_LINES}）"
  FAIL=1
else
  echo "  ✅ +${ADDED} 行（上限: ${MAX_LINES}）"
fi

# ─── [0.4] 自創常數掃描（ticket 起點以後） ───
echo
echo "=== [0.4] 自創常數掃描 ==="
# Use ticket anchor for scan scope (ignore pre-ticket AmbientConstants changes)
if [[ -n "$SUGGESTED_BASE" ]]; then
  _SCAN_BASE="$SUGGESTED_BASE"
elif [[ $CONF_MODE -eq 1 ]]; then
  _SCAN_BASE="$DIFF_REF"
else
  _SCAN_BASE="$DIFF_REF"
fi
NEW_CONSTANTS=$(git diff "${_SCAN_BASE}..HEAD" --unified=0 -- "*/AmbientConstants.kt" 2>/dev/null \
  | grep "^+" | grep -v "^+++" | grep -v "^+\s*//" || true)
if [[ -n "$NEW_CONSTANTS" ]]; then
  echo "  ⚠️ AmbientConstants 有新增行，需確認是否為授權變更："
  echo "$NEW_CONSTANTS" | head -5
else
  echo "  ✅ AmbientConstants 無新增"
fi

# ─── [0.5] Config 新鮮度檢查（WARNING only） ───
if [[ $CONF_MODE -eq 1 && -n "$CREATED" ]]; then
  echo
  echo "=== [0.5] Config 新鮮度檢查 ==="
  NOW_EPOCH=$(date +%s)
  CREATED_EPOCH=$(date -d "$CREATED" +%s 2>/dev/null || \
                  date -j -f "%Y-%m-%d" "$CREATED" +%s 2>/dev/null || echo 0)
  DIFF_HOURS=$(( (NOW_EPOCH - CREATED_EPOCH) / 3600 ))
  if [[ $DIFF_HOURS -gt 48 ]]; then
    echo "  ⚠️ STALE: Verify Config 建立於 ${DIFF_HOURS}h 前（>48h），請確認仍有效"
  else
    echo "  ✅ Config 新鮮度正常（${DIFF_HOURS}h）"
  fi
fi

# ─── 結果彙總 ───
echo
echo "══════════════════════════════════════"
if [[ $FAIL -ne 0 ]]; then
  if [[ $STALE -eq 1 && $FAIL -eq 1 ]]; then
    # Distinguish: if FAIL came ONLY from stale (not genuine violations)
    # We need to check if there were genuine violations too
    # FAIL=1 could be from stale-only or from genuine violations
    # The STALE_DIFF_REF_FILES variable captures stale ones
    # If all violations were stale → STALE_DIFF_REF error type
    # If there were genuine violations → SCOPE_VIOLATION error type
    # We track this via FORBIDDEN_FAIL and the SCOPE VIOLATION echo above
    # Since we can't easily distinguish here, output both if stale exists
    echo "⛔ Step 0 — STALE_DIFF_REF"
    if [[ -n "$SUGGESTED_BASE" ]]; then
      echo "  修正: 在 verify_cmd.conf 中將 DIFF_REF 更新為 $SUGGESTED_BASE"
    else
      echo "  修正: 更新 DIFF_REF 為 Ticket 開工前的 HEAD"
    fi
  else
    echo "⛔ Step 0 — SCOPE VERIFICATION FAILED"
    echo "  修正範圍問題後重新執行 /qa"
  fi
  echo "══════════════════════════════════════"
  exit 1
else
  echo "✅ Step 0 — Scope 驗證通過，繼續 /qa Step 1"
  echo "══════════════════════════════════════"
  exit 0
fi
