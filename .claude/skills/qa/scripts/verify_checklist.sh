#!/usr/bin/env bash
# /qa Step 0 — Ticket Scope 驗證（確定性，零 AI 配額）
# 來源: CSO Session 14 — /uc 完成度強制機制
# 部署: .claude/skills/qa/scripts/verify_checklist.sh
#
# 用法:
#   bash .claude/skills/qa/scripts/verify_checklist.sh \
#     --allowed "TapFocusRing.kt,TapFocusController.kt" \
#     --forbidden "CameraViewModel.kt" \
#     --max-lines 80
#
# 參數說明:
#   --allowed   : 允許修改的檔案（逗號分隔的 basename）。省略 = 不檢查
#   --forbidden : 禁止修改的檔案（逗號分隔的 basename）。省略 = 使用專案預設
#   --max-lines : 新增行數上限。省略 = 999（不限）
#   --diff-ref  : git diff 參照點。預設 = HEAD~1
#
# 退出碼: 0 = PASS, 1 = FAIL

set -u

# ─── 專案級禁區預設（P05 Contract §1） ───
DEFAULT_FORBIDDEN="CameraViewModel.kt"

# ─── 參數解析 ───
ALLOWED=""
FORBIDDEN="$DEFAULT_FORBIDDEN"
MAX_LINES=999
DIFF_REF="HEAD~1"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --allowed)   ALLOWED="$2";   shift 2 ;;
    --forbidden) FORBIDDEN="$2"; shift 2 ;;
    --max-lines) MAX_LINES="$2"; shift 2 ;;
    --diff-ref)  DIFF_REF="$2";  shift 2 ;;
    *) echo "Unknown option: $1"; exit 1 ;;
  esac
done

echo "# /qa Step 0 — Ticket Scope 驗證"
echo "  diff-ref: $DIFF_REF"
echo "  allowed: ${ALLOWED:-<any>}"
echo "  forbidden: $FORBIDDEN"
echo "  max-lines: $MAX_LINES"
echo

FAIL=0

# ─── 取得變更檔案清單 ───
CHANGED=$(git diff --name-only "$DIFF_REF" 2>/dev/null || true)
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
      if [[ "$BASENAME" == "$a" ]]; then
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
    fb_trimmed=$(echo "$fb" | xargs)  # trim whitespace
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
ADDED=$(git diff "$DIFF_REF" --numstat 2>/dev/null | awk '{s+=$1} END {print s+0}')
if [[ "$ADDED" -gt "$MAX_LINES" ]]; then
  echo "  ⛔ BLOAT: +${ADDED} 行（上限: ${MAX_LINES}）"
  FAIL=1
else
  echo "  ✅ +${ADDED} 行（上限: ${MAX_LINES}）"
fi

# ─── [0.4] 新增 AmbientConstants 常數掃描 ───
echo
echo "=== [0.4] 自創常數掃描 ==="
NEW_CONSTANTS=$(git diff "$DIFF_REF" --unified=0 -- "*/AmbientConstants.kt" 2>/dev/null \
  | grep "^+" | grep -v "^+++" | grep -v "^+\s*//" || true)
if [[ -n "$NEW_CONSTANTS" ]]; then
  echo "  ⚠️ AmbientConstants 有新增行，需確認是否為授權變更："
  echo "$NEW_CONSTANTS" | head -5
else
  echo "  ✅ AmbientConstants 無新增"
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
