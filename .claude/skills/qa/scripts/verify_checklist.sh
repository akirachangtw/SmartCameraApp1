#!/usr/bin/env bash
# /qa Step 0 — Ticket Scope 驗證（確定性，零 AI 配額）
# 來源: CSO Session 14 — /uc 完成度強制機制
# 部署: .claude/skills/qa/scripts/verify_checklist.sh
#
# 用法（CLI 模式，向後相容）:
#   bash .claude/skills/qa/scripts/verify_checklist.sh \
#     --allowed "TapFocusRing.kt,TapFocusController.kt" \
#     --forbidden "CameraViewModel.kt" \
#     --max-lines 80 --diff-ref HEAD~1
#
# 用法（conf 模式）:
#   bash .claude/skills/qa/scripts/verify_checklist.sh tmp/verify_cmd.conf
#
# conf 檔格式: KEY=VALUE（每行），支援 TICKET_ID/CREATED/PRESET/ALLOWED/FORBIDDEN/MAX_LINES/DIFF_REF
#
# 退出碼: 0 = PASS, 1 = FAIL

set -u

# ─── 專案級禁區預設（P05 Contract §1） ───
DEFAULT_FORBIDDEN="CameraViewModel.kt"

# ─── get_conf: 安全讀取 conf 值（禁止 source） ───
get_conf() {
  local key="$1" file="$2"
  grep "^${key}=" "$file" | head -1 | cut -d'=' -f2-
}

# ─── 初始值 ───
ALLOWED=""
FORBIDDEN="$DEFAULT_FORBIDDEN"
MAX_LINES=999
DIFF_REF="HEAD~1"
TICKET_ID=""
CREATED=""
CONF_MODE=0

# ─── 參數解析：conf 模式 vs CLI 模式 ───
if [[ $# -ge 1 && "$1" == *.conf ]]; then
  CONF_FILE="$1"
  if [[ ! -r "$CONF_FILE" ]]; then
    echo "❌ conf file not found or not readable: $CONF_FILE"
    exit 1
  fi
  CONF_MODE=1
  TICKET_ID=$(get_conf "TICKET_ID" "$CONF_FILE")
  CREATED=$(get_conf "CREATED" "$CONF_FILE")
  ALLOWED=$(get_conf "ALLOWED" "$CONF_FILE")
  FORBIDDEN_CONF=$(get_conf "FORBIDDEN" "$CONF_FILE")
  [[ -n "$FORBIDDEN_CONF" ]] && FORBIDDEN="$FORBIDDEN_CONF"
  MAX_LINES_CONF=$(get_conf "MAX_LINES" "$CONF_FILE")
  [[ -n "$MAX_LINES_CONF" ]] && MAX_LINES="$MAX_LINES_CONF"
  DIFF_REF_CONF=$(get_conf "DIFF_REF" "$CONF_FILE")
  [[ -n "$DIFF_REF_CONF" ]] && DIFF_REF="$DIFF_REF_CONF"
else
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --allowed)   ALLOWED="$2";   shift 2 ;;
      --forbidden) FORBIDDEN="$2"; shift 2 ;;
      --max-lines) MAX_LINES="$2"; shift 2 ;;
      --diff-ref)  DIFF_REF="$2";  shift 2 ;;
      *) echo "Unknown option: $1"; exit 1 ;;
    esac
  done
fi

# ─── DIFF_REF 合法性驗證 ───
if ! git rev-parse --verify "$DIFF_REF" >/dev/null 2>&1; then
  echo "❌ DIFF_REF not found in git history: $DIFF_REF"
  exit 1
fi

echo "# /qa Step 0 — Ticket Scope 驗證"
echo "  diff-ref: $DIFF_REF"
echo "  allowed: ${ALLOWED:-<any>}"
echo "  forbidden: $FORBIDDEN"
echo "  max-lines: $MAX_LINES"
echo

FAIL=0

# ─── 取得變更檔案清單 ───
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

# ─── [0.1] 允許檔案範圍 ───
echo "=== [0.1] 允許檔案範圍 ==="
if [[ -n "$ALLOWED" ]]; then
  IFS=',' read -ra AARR <<< "$ALLOWED"
  while IFS= read -r f; do
    BASENAME=$(basename "$f")
    MATCH=0
    for a in "${AARR[@]}"; do
      A_BASENAME=$(basename "$a")
      if [[ "$BASENAME" == "$A_BASENAME" ]] || [[ "$f" == "$a" ]]; then
        MATCH=1
        break
      fi
    done
    if [[ $MATCH -eq 0 ]]; then
      echo "  ⛔ SCOPE VIOLATION: $BASENAME (不在允許清單)"
      FAIL=1
    fi
  done <<< "$CHANGED"
  [[ $FAIL -eq 0 ]] && echo "  ✅ 所有變更檔案在允許範圍內"
else
  echo "  ⚪ 未指定 --allowed，跳過範圍檢查"
fi

# ─── [0.2] 禁區檔案 ───
echo
echo "=== [0.2] 禁區檔案 ==="
if [[ -n "$FORBIDDEN" ]]; then
  IFS=',' read -ra FARR <<< "$FORBIDDEN"
  for fb in "${FARR[@]}"; do
    fb_trimmed=$(echo "$fb" | xargs)
    if echo "$CHANGED" | grep -q "$fb_trimmed"; then
      echo "  ⛔ FORBIDDEN FILE: $fb_trimmed 被修改"
      FAIL=1
    fi
  done
  [[ $FAIL -eq 0 ]] && echo "  ✅ 無禁區檔案被修改"
else
  echo "  ⚪ 無禁區定義"
fi

# ─── [0.3] 新增行數 ───
echo
echo "=== [0.3] 新增行數 ==="
if [[ $CONF_MODE -eq 1 ]]; then
  ADDED=$(git diff "${DIFF_REF}..HEAD" --numstat 2>/dev/null | awk '{s+=$1} END {print s+0}')
else
  ADDED=$(git diff "$DIFF_REF" --numstat 2>/dev/null | awk '{s+=$1} END {print s+0}')
fi
if [[ "$ADDED" -gt "$MAX_LINES" ]]; then
  echo "  ⛔ BLOAT: +${ADDED} 行（上限: ${MAX_LINES}）"
  FAIL=1
else
  echo "  ✅ +${ADDED} 行（上限: ${MAX_LINES}）"
fi

# ─── [0.4] 新增 AmbientConstants 常數掃描 ───
echo
echo "=== [0.4] 自創常數掃描 ==="
if [[ $CONF_MODE -eq 1 ]]; then
  NEW_CONSTANTS=$(git diff "${DIFF_REF}..HEAD" --unified=0 -- "*/AmbientConstants.kt" 2>/dev/null \
    | grep "^+" | grep -v "^+++" | grep -v "^+\s*//" || true)
else
  NEW_CONSTANTS=$(git diff "$DIFF_REF" --unified=0 -- "*/AmbientConstants.kt" 2>/dev/null \
    | grep "^+" | grep -v "^+++" | grep -v "^+\s*//" || true)
fi
if [[ -n "$NEW_CONSTANTS" ]]; then
  echo "  ⚠️ AmbientConstants 有新增行，需確認是否為授權變更："
  echo "$NEW_CONSTANTS" | head -5
else
  echo "  ✅ AmbientConstants 無新增"
fi

# ─── [0.5] Config 新鮮度檢查（WARNING only，不 hard fail） ───
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
  echo "⛔ Step 0 — SCOPE VERIFICATION FAILED"
  echo "  修正範圍問題後重新執行 /qa"
  echo "══════════════════════════════════════"
  exit 1
else
  echo "✅ Step 0 — Scope 驗證通過，繼續 /qa Step 1"
  echo "══════════════════════════════════════"
  exit 0
fi
