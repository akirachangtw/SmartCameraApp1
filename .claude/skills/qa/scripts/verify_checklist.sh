#!/usr/bin/env bash
# /qa Step 0 — Ticket Scope 驗證 v3（DIFF_END 鎖定終點）
# 來源: INFRA-18 DIFF_END 精準終點治理
#
# 用法（conf 模式）:
#   bash .claude/skills/qa/scripts/verify_checklist.sh tmp/verify_cmd.conf
# 用法（CLI 模式）:
#   bash .claude/skills/qa/scripts/verify_checklist.sh \
#     --allowed "File1.kt,File2.kt" --diff-ref HEAD~1 --max-lines 100
#
# DIFF_REF/DIFF_END 合約：
#   DIFF_REF = Ticket 開工前的 git HEAD（精確起點）
#   DIFF_END = Ticket 最後一個 commit（精確終點）；缺少時 fallback HEAD
#   DIFF_REF..DIFF_END 只能包含 ALLOWED + Pipeline Artifact + Excluded Payload
#   分類: STALE_DIFF_REF（base 過舊）/ FUTURE_CONTAMINATION（end 過新）/ SCOPE_VIOLATION（真實越權）
#
# 退出碼: 0 = PASS, 1 = FAIL

set -u

# ─── Pipeline Artifact 白名單（有界豁免；超界 fall-through 至一般 ALLOWED 檢查） ───
PIPELINE_ARTIFACT=".claude/PROGRESS.md,.claude/PROGRESS_ARCHIVE.md"
# INFRA-VERIFY-PA-BOUND-01: 最大合法 Step-4 同步 delta 實測 4–16 行，40 為約 2.5x headroom。
PIPELINE_ARTIFACT_MAX_LINES=40

DEFAULT_FORBIDDEN="CameraViewModel.kt"

get_conf() {
  local key="$1" file="$2"
  grep "^${key}=" "$file" | head -1 | cut -d'=' -f2-
}

trim_token() {
  echo "$1" | xargs
}

token_matches_file() {
  local token file base
  token="$(trim_token "$1")"
  file="$2"
  base=$(basename "$file")
  [[ -n "$token" ]] || return 1
  if [[ "$token" == *"/"* || "$token" == *"\\"* ]]; then
    # INFRA-VERIFY-GLOB-01: RHS MUST stay unquoted — quoting disables `[[ ]]`
    # pattern matching, which silently reduced every path token (ALLOWED /
    # FORBIDDEN / EXCLUDED_PAYLOAD) to a literal string compare. Inside `[[ ]]`
    # there is no pathname expansion, so `*` also spans `/`: the token
    # `app/src/main/**/*State.kt` matches nested files as intended.
    # Wildcard-free path tokens still compare exactly (no metacharacters).
    [[ "$file" == $token ]]
  else
    [[ "$base" == "$token" ]]
  fi
}

# ─── Pipeline Artifact 豁免界線（INFRA-VERIFY-PA-BOUND-01 AI-1）───
pa_exemption_applies() { # pa_exemption_applies <delta_lines> → 0=豁免成立 1=豁免失效
  local delta="$1"
  [[ "$delta" -le "$PIPELINE_ARTIFACT_MAX_LINES" ]]
}

pa_file_lines_changed() { # pa_file_lines_changed <file> → added+deleted（numstat $1+$2）
  local file="$1" lines
  if [[ $CONF_MODE -eq 1 ]]; then
    lines=$(git diff "${DIFF_REF}..${DIFF_END_RESOLVED}" --numstat -- "$file" 2>/dev/null | awk '{s+=$1+$2} END {print s+0}')
  else
    lines=$(git diff "$DIFF_REF" --numstat -- "$file" 2>/dev/null | awk '{s+=$1+$2} END {print s+0}')
  fi
  echo "${lines:-0}"
}

# ─── MAX_LINES execution gate 退役（INFRA-MAXLINES-GATE-01 AI-2/AI-3）───
maxlines_verdict() { # maxlines_verdict <max_lines> <added> → NA|MALFORMED|OVER|OK
  local max_lines="$1" added="$2"
  if [[ "$max_lines" == "N/A" ]]; then
    echo "NA"
  elif [[ ! "$max_lines" =~ ^[0-9]+$ ]]; then
    echo "MALFORMED"
  elif [[ "$added" -gt "$max_lines" ]]; then
    echo "OVER"
  else
    echo "OK"
  fi
}

# ─── Self-test（INFRA-VERIFY-GLOB-01 AI-2）───
# 用法: bash .claude/skills/qa/scripts/verify_checklist.sh --self-test
# 純函數級斷言，不觸 git、不落 repo 雜訊。
self_test() {
  local passed=0 failed=0
  _expect() { # _expect <hit|miss> <token> <file> <desc>
    local want="$1" token="$2" file="$3" desc="$4" got
    if token_matches_file "$token" "$file"; then got="hit"; else got="miss"; fi
    if [[ "$got" == "$want" ]]; then
      passed=$((passed + 1)); echo "  ✅ [$want] $desc"
    else
      failed=$((failed + 1)); echo "  ❌ [want=$want got=$got] $desc | token='$token' file='$file'"
    fi
  }
  echo "── token_matches_file() self-test ──"
  # path-glob 正（修復前必 FAIL：字面比對永不命中）
  _expect hit  "app/src/main/java/**/CameraViewModel.kt" "app/src/main/java/com/cueshot/presentation/CameraViewModel.kt"  "path-glob 正 · ALLOWED 深層路徑命中"
  _expect hit  "app/src/main/**/*State.kt"               "app/src/main/java/com/cueshot/domain/model/CameraUiState.kt"    "path-glob 正 · FORBIDDEN 萬用字元攔截"
  # path-glob 負
  _expect miss "app/src/main/java/**/CameraViewModel.kt" "app/src/test/java/com/cueshot/CameraViewModelTest.kt"           "path-glob 負 · 不同根目錄不得命中"
  _expect miss "app/src/main/**/*State.kt"               "app/src/main/java/com/cueshot/domain/model/CameraUiStates.kt"   "path-glob 負 · 後綴不符不得命中"
  # path 字面（無萬用字元）— 零回歸
  _expect hit  ".claude/skills/qa/scripts/verify_checklist.sh" ".claude/skills/qa/scripts/verify_checklist.sh"   "path 字面 正 · 無萬用字元 token 精確命中"
  _expect miss ".claude/skills/qa/scripts/verify_checklist.sh" ".claude/skills/qa/scripts/structural_verify.sh"  "path 字面 負 · 同目錄他檔不得命中"
  # basename token — 零回歸
  _expect hit  "CameraViewModel.kt" "app/src/main/java/com/cueshot/presentation/CameraViewModel.kt"         "basename 正 · DEFAULT_FORBIDDEN 品類"
  _expect miss "CameraViewModel.kt" "app/src/main/java/com/cueshot/presentation/CameraViewModelFactory.kt"  "basename 負 · 前綴相同不得誤命中"
  # Pipeline Artifact 豁免 · INFRA-VERIFY-EXEMPT-01
  _expect hit  ".claude/PROGRESS_ARCHIVE.md" ".claude/PROGRESS_ARCHIVE.md"          "pipeline-artifact 正 · PROGRESS_ARCHIVE 豁免命中"
  _expect miss ".claude/PROGRESS_ARCHIVE.md" ".claude/PROGRESS_ARCHIVE_Sprint2.md"  "pipeline-artifact 負 · 豁免不得外溢至衍生檔名"
  # Pipeline Artifact 豁免有界化 · INFRA-VERIFY-PA-BOUND-01
  _expect_num() { # _expect_num <want 0|1> <delta> <desc>
    local want="$1" delta="$2" desc="$3" got
    if pa_exemption_applies "$delta"; then got=0; else got=1; fi
    if [[ "$got" == "$want" ]]; then
      passed=$((passed + 1)); echo "  ✅ [$want] $desc"
    else
      failed=$((failed + 1)); echo "  ❌ [want=$want got=$got] $desc | delta=$delta"
    fi
  }
  _expect_num 0 12  "pa_exemption_applies 正 · 小規模同步 delta=12 → 豁免成立"
  _expect_num 1 200 "pa_exemption_applies 負 · 大規模覆寫 delta=200 → 豁免失效"
  _expect_num 0 40  "pa_exemption_applies 邊界 · delta=40（=MAX_LINES）→ 豁免成立"
  _expect_num 1 41  "pa_exemption_applies 邊界 · delta=41（=MAX_LINES+1）→ 豁免失效"
  # MAX_LINES execution gate 退役 · INFRA-MAXLINES-GATE-01
  _expect_verdict() { # _expect_verdict <want> <max_lines> <added> <desc>
    local want="$1" max_lines="$2" added="$3" desc="$4" got
    got=$(maxlines_verdict "$max_lines" "$added")
    if [[ "$got" == "$want" ]]; then
      passed=$((passed + 1)); echo "  ✅ [$want] $desc"
    else
      failed=$((failed + 1)); echo "  ❌ [want=$want got=$got] $desc | max_lines=$max_lines added=$added"
    fi
  }
  _expect_verdict NA        "N/A" 653 "maxlines_verdict · MAX_LINES=N/A → NA（安全票，行數閘不適用）"
  _expect_verdict MALFORMED "10O" 653 "maxlines_verdict · 非法格式 '10O' → MALFORMED"
  _expect_verdict OVER      100   653 "maxlines_verdict · 實測 653 > 估算 100 → OVER（非退件）"
  _expect_verdict OK        800   653 "maxlines_verdict · 實測 653 ≤ 估算 800 → OK"
  echo "── self-test: ${passed} passed, ${failed} failed ──"
  [[ $failed -eq 0 ]]
}

if [[ $# -ge 1 && "$1" == "--self-test" ]]; then
  self_test
  exit $?
fi

ALLOWED=""
FORBIDDEN="$DEFAULT_FORBIDDEN"
EXCLUDED_PAYLOAD=""
MAX_LINES=999
DIFF_REF="HEAD~1"
DIFF_END=""
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
  DIFF_END_CONF=$(get_conf "DIFF_END" "$CONF_FILE")
  [[ -n "$DIFF_END_CONF" ]] && DIFF_END="$DIFF_END_CONF"
else
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --allowed)          ALLOWED="$2";          shift 2 ;;
      --forbidden)        FORBIDDEN="$2";        shift 2 ;;
      --excluded-payload) EXCLUDED_PAYLOAD="$2"; shift 2 ;;
      --max-lines)        MAX_LINES="$2";        shift 2 ;;
      --diff-ref)         DIFF_REF="$2";         shift 2 ;;
      --diff-end)         DIFF_END="$2";         shift 2 ;;
      *) echo "Unknown option: $1"; exit 1 ;;
    esac
  done
fi

# ─── DIFF_REF 驗證 ───
git rev-parse --verify "$DIFF_REF" >/dev/null 2>&1 || { echo "❌ DIFF_REF not found in git history: $DIFF_REF"; exit 1; }

# ─── DIFF_END 解析與驗證 ───
DIFF_END_RESOLVED="HEAD"
DIFF_END_SOURCE="HEAD"
if [[ -n "$DIFF_END" ]]; then
  git rev-parse --verify "$DIFF_END" >/dev/null 2>&1 \
    || { echo "❌ DIFF_END not found in git history: $DIFF_END"; exit 1; }
  git merge-base --is-ancestor "$DIFF_REF" "$DIFF_END" 2>/dev/null \
    || { echo "❌ DIFF_END ($DIFF_END) is not a descendant of DIFF_REF ($DIFF_REF)"; exit 1; }
  DIFF_END_RESOLVED="$DIFF_END"
  DIFF_END_SOURCE="DIFF_END"
fi

echo "# /qa Step 0 — Ticket Scope 驗證"
echo "  diff-ref: $DIFF_REF"
echo "  diff-end: ${DIFF_END:-<HEAD (unfixed)>} [source: $DIFF_END_SOURCE]"
echo "  allowed: ${ALLOWED:-<any>}"
echo "  forbidden: $FORBIDDEN"
echo "  max-lines: $MAX_LINES"
echo

FAIL=0
GENUINE_VIOLATION=0
STALE=0
STALE_DIFF_REF_FILES=""
FUTURE_CONTAMINATION=0
FUTURE_CONTAMINATION_FILES=""

# ─── 取得 DIFF_REF..DIFF_END_RESOLVED 全量變更 ───
if [[ $CONF_MODE -eq 1 ]]; then
  CHANGED=$(git diff --name-only "${DIFF_REF}..${DIFF_END_RESOLVED}" 2>/dev/null || true)
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
END_SHORT=$(git rev-parse --short "$DIFF_END_RESOLVED" 2>/dev/null || echo "?")
COMMIT_COUNT=$(git log --oneline "${DIFF_REF}..${DIFF_END_RESOLVED}" 2>/dev/null | wc -l | tr -d ' ')
echo "  base: $BASE_SHORT → $END_SHORT ($COMMIT_COUNT commits in scope) [end source: $DIFF_END_SOURCE]"

# Find ticket anchor: oldest ALLOWED-touching commit in range (--reverse = oldest first)
TICKET_BASE_COMMIT=""
SUGGESTED_BASE=""
if [[ -n "$ALLOWED" && $CONF_MODE -eq 1 ]]; then
  IFS=',' read -ra _AARR_PREVIEW <<< "$ALLOWED"
  while IFS= read -r _cmt; do
    _cmt_files=$(git diff-tree --no-commit-id -r --name-only "$_cmt" 2>/dev/null || true)
    for _a in "${_AARR_PREVIEW[@]}"; do
      while IFS= read -r _tf; do
        [[ -z "$_tf" ]] && continue
        if token_matches_file "$_a" "$_tf"; then
          TICKET_BASE_COMMIT="$_cmt"
          break 3
        fi
      done <<< "$_cmt_files"
    done
  done < <(git log --reverse --format="%h" "${DIFF_REF}..${DIFF_END_RESOLVED}" 2>/dev/null || true)

  if [[ -n "$TICKET_BASE_COMMIT" ]]; then
    SUGGESTED_BASE=$(git rev-parse --short "${TICKET_BASE_COMMIT}^" 2>/dev/null || true)
    echo "  ticket anchor: $TICKET_BASE_COMMIT (first ALLOWED-touching commit)"
    [[ -n "$SUGGESTED_BASE" ]] && echo "  ✅ Suggested DIFF_REF: $SUGGESTED_BASE"
  else
    echo "  ⚠️ No ALLOWED-file commits found in ${BASE_SHORT}..${END_SHORT}"
  fi
fi
echo

# ─── Helper: is_stale_commit ───
# Returns 0 (true) if commit is a strict ancestor of TICKET_BASE_COMMIT (pre-ticket)
is_stale_commit() {
  local cmt="$1"
  [[ -z "$TICKET_BASE_COMMIT" ]] && return 1
  [[ "$cmt" == "$TICKET_BASE_COMMIT" ]] && return 1
  git merge-base --is-ancestor "$cmt" "$TICKET_BASE_COMMIT" 2>/dev/null
}

# ─── Helper: is_future_commit ───
# Returns 0 (true) if commit is a strict descendant of TICKET_BASE_COMMIT (post-ticket)
is_future_commit() {
  local cmt="$1"
  [[ -z "$TICKET_BASE_COMMIT" ]] && return 1
  [[ "$cmt" == "$TICKET_BASE_COMMIT" ]] && return 1
  git merge-base --is-ancestor "$TICKET_BASE_COMMIT" "$cmt" 2>/dev/null
}

# ─── [0.1] 允許檔案範圍 ───
echo "=== [0.1] 允許檔案範圍 ==="

ALLOWED_COUNT=0

if [[ -n "$ALLOWED" ]]; then
  IFS=',' read -ra AARR <<< "$ALLOWED"

  while IFS= read -r f; do
    [[ -z "$f" ]] && continue
    BASENAME=$(basename "$f")

    # Check: Pipeline Artifact (never a violation)
    PA_MATCH=0
    IFS=',' read -ra PAARR <<< "$PIPELINE_ARTIFACT"
    for pa in "${PAARR[@]}"; do
      token_matches_file "$pa" "$f" && { PA_MATCH=1; break; }
    done
    if [[ $PA_MATCH -eq 1 ]]; then
      _PA_DELTA=$(pa_file_lines_changed "$f")
      if pa_exemption_applies "$_PA_DELTA"; then
        echo "  📌 Pipeline Artifact: $f (Δ${_PA_DELTA} ≤ ${PIPELINE_ARTIFACT_MAX_LINES})"
        continue
      else
        echo "  ⚠️ Pipeline Artifact 豁免失效（Δ${_PA_DELTA} > ${PIPELINE_ARTIFACT_MAX_LINES}）: $f — fall through 至 ALLOWED 檢查"
      fi
    fi

    # Check: ALLOWED
    MATCH=0
    for a in "${AARR[@]}"; do
      if token_matches_file "$a" "$f"; then
        MATCH=1; break
      fi
    done
    if [[ $MATCH -eq 1 ]]; then
      echo "  ✅ ALLOWED: $BASENAME"
      ALLOWED_COUNT=$((ALLOWED_COUNT + 1))
      continue
    fi

    # Check: EXCLUDED_PAYLOAD
    EXCL_MATCH=0
    if [[ -n "$EXCLUDED_PAYLOAD" ]]; then
      IFS=',' read -ra EXCL_ARR <<< "$EXCLUDED_PAYLOAD"
      for excl in "${EXCL_ARR[@]}"; do
        if token_matches_file "$excl" "$f"; then
          EXCL_MATCH=1; break
        fi
      done
    fi
    if [[ $EXCL_MATCH -eq 1 ]]; then
      echo "  📋 Excluded Payload: $BASENAME (authorized)"
      continue
    fi

    # Unexpected file: classify as STALE / FUTURE_CONTAMINATION / SCOPE_VIOLATION
    _file_commit=$(git log --format="%h" -1 "${DIFF_REF}..${DIFF_END_RESOLVED}" -- "$f" 2>/dev/null | head -1 || true)
    if [[ -n "$_file_commit" ]] && is_stale_commit "$_file_commit"; then
      STALE_DIFF_REF_FILES="${STALE_DIFF_REF_FILES}  ⚠️ [stale] $BASENAME (commit: $_file_commit)\n"
      STALE=1
    elif [[ -n "$_file_commit" ]] && is_future_commit "$_file_commit"; then
      FUTURE_CONTAMINATION_FILES="${FUTURE_CONTAMINATION_FILES}  ⚠️ [future] $BASENAME (commit: $_file_commit)\n"
      FUTURE_CONTAMINATION=1
    else
      echo "  ⛔ SCOPE VIOLATION: $BASENAME (不在允許清單)"
      GENUINE_VIOLATION=1
      FAIL=1
    fi
  done <<< "$CHANGED"

  if [[ $FAIL -eq 0 && $STALE -eq 0 && $FUTURE_CONTAMINATION -eq 0 ]]; then
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

  if [[ $FUTURE_CONTAMINATION -eq 1 ]]; then
    echo
    echo "  ⚠️ FUTURE_CONTAMINATION: 以下檔案來自本 Ticket 之後的提交（HEAD 超出 Ticket 終點）："
    printf '%b' "$FUTURE_CONTAMINATION_FILES"
    echo "  💡 修正: 在 verify_cmd.conf 設定 DIFF_END=<ticket-last-commit> 鎖定審查終點"
    FAIL=1
  fi

  # Error: DIFF_END set but no ALLOWED files found in locked range
  if [[ -n "$DIFF_END" && $ALLOWED_COUNT -eq 0 ]]; then
    echo "  ⛔ ERROR: DIFF_REF..DIFF_END 不含任何 ALLOWED 檔案，請確認 DIFF_END 設定正確"
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
    fb_trimmed=$(trim_token "$fb")
    _FORBIDDEN_FILE=""
    while IFS= read -r _changed; do
      [[ -z "$_changed" ]] && continue
      if token_matches_file "$fb_trimmed" "$_changed"; then
        _FORBIDDEN_FILE="$_changed"
        break
      fi
    done <<< "$CHANGED"
    if [[ -n "$_FORBIDDEN_FILE" ]]; then
      _fb_commit=$(git log --format="%h" -1 "${DIFF_REF}..${DIFF_END_RESOLVED}" -- "$_FORBIDDEN_FILE" 2>/dev/null | head -1 || true)
      if [[ -n "$_fb_commit" ]] && is_stale_commit "$_fb_commit"; then
        echo "  ⚠️ [stale] FORBIDDEN $_FORBIDDEN_FILE (pre-ticket commit $_fb_commit — not this ticket)"
      else
        echo "  ⛔ FORBIDDEN FILE: $_FORBIDDEN_FILE 被修改"
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
  ADDED=0
  for a in "${AARR[@]}"; do
    _token=$(trim_token "$a")
    if [[ $CONF_MODE -eq 1 ]]; then
      _lines=$(git diff "${DIFF_REF}..${DIFF_END_RESOLVED}" --numstat 2>/dev/null | \
        awk -v n="$_token" 'index(n, "/") || index(n, "\\") {if ($3 == n) s+=$1+$2; next} {split($3,p,"/"); if (p[length(p)] == n) s+=$1+$2} END {print s+0}')
    else
      _lines=$(git diff "$DIFF_REF" --numstat 2>/dev/null | \
        awk -v n="$_token" 'index(n, "/") || index(n, "\\") {if ($3 == n) s+=$1+$2; next} {split($3,p,"/"); if (p[length(p)] == n) s+=$1+$2} END {print s+0}')
    fi
    ADDED=$((ADDED + ${_lines:-0}))
  done
  echo "  (ALLOWED files only)"
else
  if [[ $CONF_MODE -eq 1 ]]; then
    ADDED=$(git diff "${DIFF_REF}..${DIFF_END_RESOLVED}" --numstat 2>/dev/null | awk '{s+=$1+$2} END {print s+0}')
  else
    ADDED=$(git diff "$DIFF_REF" --numstat 2>/dev/null | awk '{s+=$1+$2} END {print s+0}')
  fi
fi
case "$(maxlines_verdict "$MAX_LINES" "$ADDED")" in
  NA)        echo "  ⚪ MAX_LINES=N/A（安全票，行數閘不適用）— 實測 ${ADDED} 行（估算校準用）" ;;
  MALFORMED) echo "  ⚠️ MAX_LINES 值非法（'${MAX_LINES}'）— 遙測跳過，票面格式待 ticket lint 修；實測 ${ADDED} 行" ;;
  OVER)      echo "  ⚠️ 實測 ${ADDED} 行 > 估算 ${MAX_LINES}（超出 $((ADDED - MAX_LINES)) 行）— 非退件，供估算校準" ;;
  OK)        echo "  ✅ 實測 ${ADDED} 行（估算 ${MAX_LINES}）" ;;
esac

# ─── Evidence Record（INFRA-MAXLINES-EVIDENCE-01 AI-1/AI-2）───
# Hard Constraint 1: evidence 缺失/寫入失敗 MUST NOT 影響 Step 0 exit code（不觸碰 $FAIL）。
if [[ -n "$TICKET_ID" ]]; then
  WORKTREE=$(git rev-parse --show-toplevel 2>/dev/null)
  COMMIT_SHA=$(git rev-parse HEAD 2>/dev/null)
  if [[ -n "$WORKTREE" && -n "$COMMIT_SHA" ]]; then
    EVIDENCE_DIR="${WORKTREE}/tmp/${TICKET_ID}/evidence"
    EVIDENCE_FILE="${EVIDENCE_DIR}/step0_${COMMIT_SHA}.txt"
    if mkdir -p "$EVIDENCE_DIR" 2>/dev/null; then
      EVIDENCE_CONTENT=$(printf 'TICKET_ID=%s\nWORKTREE=%s\nCOMMIT_SHA=%s\nDIFF_REF=%s\nDIFF_END_RESOLVED=%s\nMEASURED_LINES=%s\n' \
        "$TICKET_ID" "$WORKTREE" "$COMMIT_SHA" "$DIFF_REF" "$DIFF_END_RESOLVED" "$ADDED")
      if [[ -f "$EVIDENCE_FILE" ]]; then
        if [[ "$(cat "$EVIDENCE_FILE" 2>/dev/null)" != "$EVIDENCE_CONTENT" ]]; then
          echo "  ⚠️ evidence record 衝突（既有內容不一致，禁覆寫，並行競態徵兆）: $EVIDENCE_FILE" >&2
        fi
      else
        printf '%s' "$EVIDENCE_CONTENT" > "$EVIDENCE_FILE" 2>/dev/null \
          && echo "  📌 evidence record: $EVIDENCE_FILE" \
          || echo "  ⚠️ evidence record 寫入失敗（不影響 Step 0 判定）: $EVIDENCE_FILE" >&2
      fi
    else
      echo "  ⚠️ evidence 目錄建立失敗（不影響 Step 0 判定）: $EVIDENCE_DIR" >&2
    fi
  fi
fi

# ─── [0.4] 自創常數掃描（ticket 起點以後） ───
echo
echo "=== [0.4] 自創常數掃描 ==="
if [[ -n "$SUGGESTED_BASE" ]]; then
  _SCAN_BASE="$SUGGESTED_BASE"
elif [[ $CONF_MODE -eq 1 ]]; then
  _SCAN_BASE="$DIFF_REF"
else
  _SCAN_BASE="$DIFF_REF"
fi
NEW_CONSTANTS=$(git diff "${_SCAN_BASE}..${DIFF_END_RESOLVED}" --unified=0 -- "*/AmbientConstants.kt" 2>/dev/null \
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
  if [[ $STALE -eq 1 ]]; then
    echo "⛔ Step 0 — STALE_DIFF_REF"
    if [[ -n "$SUGGESTED_BASE" ]]; then
      echo "  修正: 在 verify_cmd.conf 中將 DIFF_REF 更新為 $SUGGESTED_BASE"
    else
      echo "  修正: 更新 DIFF_REF 為 Ticket 開工前的 HEAD"
    fi
  fi
  if [[ $FUTURE_CONTAMINATION -eq 1 ]]; then
    echo "⛔ Step 0 — FUTURE_CONTAMINATION"
    echo "  修正: 在 verify_cmd.conf 設定 DIFF_END=<ticket-last-commit> 後重新執行 /qa"
  fi
  if [[ $GENUINE_VIOLATION -eq 1 ]]; then
    echo "⛔ Step 0 — SCOPE VIOLATION"
    echo "  修正範圍問題後重新執行 /qa"
  fi
  echo "══════════════════════════════════════"
  exit 1
else
  echo "✅ Step 0 — Scope 驗證通過，繼續 /qa Step 1"
  echo "══════════════════════════════════════"
  exit 0
fi
