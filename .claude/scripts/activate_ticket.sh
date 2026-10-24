#!/usr/bin/env bash
# Usage: activate_ticket.sh <TICKET_ID|@票檔路徑> [附帶指示…]
#
# 參數以空白 token 化，取第一個符合票號格式（^[A-Z][A-Z0-9]*(-[A-Z0-9]+)+$，
# 且含至少一位數字）者為 TICKET_ID_ARG；比對前去除 token 尾端之 :／,／;／。／，。
# @ 開頭或（去引號後）以 .md 結尾之 token 視為票檔路徑拼法，取其 basename（去 .md）
# 中首個 _ 之前的子字串為候選票號，套用同一 regex；腳本不開啟/讀取/resolve/stat
# 該路徑，票檔一律經票號 glob 於 repo 內 Pending/／deliver/ 定位。
# 路徑形式與純票號形式同時出現且票號不同 → exit 2。
# 參數含多個相異純票號 token 時，以首個為準，並印出 ⚠️ 提示。
# 票號 token 以外之文字視為附帶指示，由呼叫端 skill 承接，本腳本僅回報字數不處理內容。
# 成功時於 ✅ ACTIVATED 行前輸出 📄 <票檔路徑>，並同步原子寫入 tmp/verify/active.json
# （八欄含 owner_session_id＝CLAUDE_CODE_SESSION_ID，本腳本為唯一 claim 入口；與 qa/scripts/verify_checklist.sh
# write_active_json 同構；失敗僅 stderr 警示，不影響 exit 0）。取不到 SID → owner 寫空並輸出警示。
#
# Exit codes:
#   0  ACTIVATED — tmp/verify_cmd.conf 已恆覆寫為指定票號之九欄值
#   2  用法錯誤（無參數／參數中無合法票號 token／路徑與票號 token 不一致）或無法定位 repo root
#   3  票檔定位失敗（找不到票檔，或找到 >=2 個候選——禁自選）
#   4  票檔缺 TICKET_ID 或 ALLOWED 欄值（fail-closed，不寫入 conf）
#   5  票面 TICKET_ID 與傳入參數不符
#
# fail-closed：任一前提不成立時，本腳本 MUST NOT 寫入或清空既有 tmp/verify_cmd.conf。
set -u

if [ -z "${1:-}" ]; then
  echo "Usage: activate_ticket.sh <TICKET_ID|@票檔路徑> [附帶指示…]" >&2
  exit 2
fi

TICKET_TOKEN_RE='^[A-Z][A-Z0-9]*(-[A-Z0-9]+)+$'

is_ticket_token() {
  local t="$1"
  [[ "$t" =~ $TICKET_TOKEN_RE ]] || return 1
  [[ "$t" =~ [0-9] ]] || return 1
  return 0
}

ARGS_JOINED="$*"
FOUND_TOKENS=()
PLAIN_TOKENS_SEEN=()
PATH_FORM_SEEN=""
REST_CHARS=0

for tok in $ARGS_JOINED; do
  STRIPPED_TOK="${tok%[:,;。，]}"
  DEQUOTED_TOK="${STRIPPED_TOK#\"}"
  DEQUOTED_TOK="${DEQUOTED_TOK%\"}"
  DEQUOTED_TOK="${DEQUOTED_TOK#\'}"
  DEQUOTED_TOK="${DEQUOTED_TOK%\'}"

  IS_PATH_FORM=0
  if [[ "$DEQUOTED_TOK" == @* || "$DEQUOTED_TOK" == *.md ]]; then
    PATH_NO_AT="${DEQUOTED_TOK#@}"
    BASENAME="${PATH_NO_AT##*/}"
    BASENAME="${BASENAME##*\\}"
    BASENAME_NOEXT="${BASENAME%.md}"
    CANDIDATE="${BASENAME_NOEXT%%_*}"
    IS_PATH_FORM=1
  else
    CANDIDATE="$STRIPPED_TOK"
  fi

  if is_ticket_token "$CANDIDATE"; then
    FOUND_TOKENS+=("$CANDIDATE")
    if [ "$IS_PATH_FORM" -eq 1 ]; then
      PATH_FORM_SEEN="$CANDIDATE"
    else
      PLAIN_TOKENS_SEEN+=("$CANDIDATE")
    fi
  else
    REST_CHARS=$((REST_CHARS + ${#tok} + 1))
  fi
done

if [ ${#FOUND_TOKENS[@]} -eq 0 ]; then
  echo "no ticket ID token in arguments: ${ARGS_JOINED:0:60}" >&2
  exit 2
fi

if [ -n "$PATH_FORM_SEEN" ] && [ ${#PLAIN_TOKENS_SEEN[@]} -gt 0 ]; then
  for p in "${PLAIN_TOKENS_SEEN[@]}"; do
    if [ "$p" != "$PATH_FORM_SEEN" ]; then
      echo "path-form and plain ticket ID token mismatch: ${PATH_FORM_SEEN} vs ${p}" >&2
      exit 2
    fi
  done
fi

TICKET_ID_ARG="${FOUND_TOKENS[0]}"

UNIQUE_TOKENS=()
for t in "${FOUND_TOKENS[@]}"; do
  dup=0
  for u in "${UNIQUE_TOKENS[@]}"; do
    [ "$u" = "$t" ] && dup=1 && break
  done
  [ "$dup" -eq 0 ] && UNIQUE_TOKENS+=("$t")
done
if [ ${#UNIQUE_TOKENS[@]} -gt 1 ]; then
  echo "⚠️ 參數含多個票號 token：${UNIQUE_TOKENS[*]}，以 ${TICKET_ID_ARG} 為準"
fi

if [ "$REST_CHARS" -gt 0 ]; then
  echo "📝 附帶指示 ${REST_CHARS} 字（由 skill 承接，腳本不處理）"
fi

REPO_ROOT=$(git rev-parse --show-toplevel 2>/dev/null)
if [ -z "$REPO_ROOT" ]; then
  echo "not a git repository" >&2
  exit 2
fi
cd "$REPO_ROOT" || exit 2

CANDIDATES=$(ls plans/cso_briefing/project/Pending/"${TICKET_ID_ARG}"_*.md plans/cso_briefing/project/deliver/"${TICKET_ID_ARG}"_*.md 2>/dev/null)
CANDIDATE_COUNT=$(printf '%s\n' "$CANDIDATES" | grep -c . || true)

if [ -z "$CANDIDATES" ] || [ "$CANDIDATE_COUNT" -eq 0 ]; then
  echo "no ticket file found for ${TICKET_ID_ARG}" >&2
  exit 3
fi
if [ "$CANDIDATE_COUNT" -ge 2 ]; then
  echo "ambiguous ticket file for ${TICKET_ID_ARG}, candidates:" >&2
  printf '%s\n' "$CANDIDATES" >&2
  exit 3
fi
TICKET_FILE="$CANDIDATES"

# 九欄＋可選 EXCLUDED_PAYLOAD 擷取：regex 與 ticket-run/SKILL.md L96 字面一致（禁另立欄位集）
FIELDS=$(grep -E '^(TICKET_ID|CREATED|PRESET|ALLOWED|FORBIDDEN|MAX_LINES|ANCHOR_PROBED|BASE_COMMIT|TARGET_BLOB|EXCLUDED_PAYLOAD)=' "$TICKET_FILE")
TICKET_ID=$(printf '%s\n' "$FIELDS" | grep '^TICKET_ID=' | head -1 | cut -d= -f2-)
CREATED=$(printf '%s\n' "$FIELDS" | grep '^CREATED=' | head -1 | cut -d= -f2-)
PRESET=$(printf '%s\n' "$FIELDS" | grep '^PRESET=' | head -1 | cut -d= -f2-)
ALLOWED=$(printf '%s\n' "$FIELDS" | grep '^ALLOWED=' | head -1 | cut -d= -f2-)
FORBIDDEN=$(printf '%s\n' "$FIELDS" | grep '^FORBIDDEN=' | head -1 | cut -d= -f2-)
MAX_LINES=$(printf '%s\n' "$FIELDS" | grep '^MAX_LINES=' | head -1 | cut -d= -f2-)
ANCHOR_PROBED=$(printf '%s\n' "$FIELDS" | grep '^ANCHOR_PROBED=' | head -1 | cut -d= -f2-)
BASE_COMMIT=$(printf '%s\n' "$FIELDS" | grep '^BASE_COMMIT=' | head -1 | cut -d= -f2-)
TARGET_BLOB=$(printf '%s\n' "$FIELDS" | grep '^TARGET_BLOB=' | head -1 | cut -d= -f2-)
EXCLUDED_PAYLOAD=$(printf '%s\n' "$FIELDS" | grep '^EXCLUDED_PAYLOAD=' | head -1 | cut -d= -f2-)

if [ -z "$TICKET_ID" ] || [ -z "$ALLOWED" ]; then
  echo "ticket file missing TICKET_ID or ALLOWED field: ${TICKET_FILE}" >&2
  exit 4
fi

if [ "$TICKET_ID" != "$TICKET_ID_ARG" ]; then
  echo "ticket file TICKET_ID (${TICKET_ID}) does not match argument (${TICKET_ID_ARG})" >&2
  exit 5
fi

if [ -f tmp/verify_cmd.conf ]; then
  OLD_TICKET_ID=$(grep -E '^TICKET_ID=' tmp/verify_cmd.conf | head -1 | cut -d= -f2-)
  if [ -n "$OLD_TICKET_ID" ] && [ "$OLD_TICKET_ID" != "$TICKET_ID" ]; then
    echo "🔀 切票：${OLD_TICKET_ID} → ${TICKET_ID}"
  fi
fi

mkdir -p tmp
DIFF_REF=$(git rev-parse --short HEAD)
{
  echo "TICKET_ID=${TICKET_ID}"
  echo "CREATED=${CREATED}"
  echo "PRESET=${PRESET}"
  echo "ALLOWED=${ALLOWED}"
  echo "FORBIDDEN=${FORBIDDEN}"
  echo "MAX_LINES=${MAX_LINES}"
  echo "DIFF_REF=${DIFF_REF}"
  echo "ANCHOR_PROBED=${ANCHOR_PROBED}"
  echo "BASE_COMMIT=${BASE_COMMIT}"
  echo "TARGET_BLOB=${TARGET_BLOB}"
  [ -n "$EXCLUDED_PAYLOAD" ] && echo "EXCLUDED_PAYLOAD=${EXCLUDED_PAYLOAD}"
} > tmp/verify_cmd.conf

mkdir -p tmp/verify
REMOVED_COUNT=0
for f in tmp/verify/"${TICKET_ID}".*.conf; do
  [ -e "$f" ] || continue
  rm -f "$f"
  REMOVED_COUNT=$((REMOVED_COUNT + 1))
done
echo "🧹 已清除 ${TICKET_ID} 之 per-ticket 快照 ${REMOVED_COUNT} 筆"

BRANCH=$(git rev-parse --abbrev-ref HEAD)

# 5a: active.json 同步（與 qa/scripts/verify_checklist.sh write_active_json 位元組級同構；失敗 fail-open）
HEAD_SHA_FULL=$(git rev-parse HEAD)
ACTIVATED_AT=$(date -u +%Y-%m-%dT%H:%M:%SZ)
ACTIVE_JSON_DEST="tmp/verify/active.json"
ACTIVE_JSON_TMP="${ACTIVE_JSON_DEST}.tmp.$$"
OWNER_SID="${CLAUDE_CODE_SESSION_ID:-}"
OLD_OWNER=""
if [ -f "$ACTIVE_JSON_DEST" ]; then
  OLD_OWNER=$(sed -n 's/.*"owner_session_id":[[:space:]]*"\([^"]*\)".*/\1/p' "$ACTIVE_JSON_DEST" 2>/dev/null | head -1)
fi
if printf '{\n  "ticket_id": "%s",\n  "ticket_revision": "%s",\n  "repo_root": "%s",\n  "branch": "%s",\n  "head_sha": "%s",\n  "activated_at": "%s",\n  "activation_source": "%s",\n  "owner_session_id": "%s"\n}\n' \
  "$TICKET_ID" "rev-0" "$REPO_ROOT" "$BRANCH" "$HEAD_SHA_FULL" "$ACTIVATED_AT" "activate_ticket.sh" "$OWNER_SID" > "$ACTIVE_JSON_TMP" 2>/dev/null \
  && mv -f "$ACTIVE_JSON_TMP" "$ACTIVE_JSON_DEST" 2>/dev/null; then
  :
else
  echo "⚠️ 無法寫入 tmp/verify/active.json（fail-open，不影響 activation）" >&2
fi

if [ -z "$OWNER_SID" ]; then
  echo "⚠️ 取不到 CLAUDE_CODE_SESSION_ID：owner 未登記，票務 Stop guard 對 ${TICKET_ID} 不生效（請於可取得 session ID 的環境重跑 activation）"
elif [ -n "$OLD_OWNER" ] && [ "$OLD_OWNER" != "$OWNER_SID" ]; then
  echo "🔀 owner 移轉：${OLD_OWNER:0:8} → ${OWNER_SID:0:8}"
fi

ALLOWED_PREVIEW=$(printf '%s' "$ALLOWED" | cut -c1-60)
echo "📄 ${TICKET_FILE}"
echo "✅ ACTIVATED | ${TICKET_ID} | ${BRANCH} @ ${DIFF_REF} | ALLOWED=${ALLOWED_PREVIEW}"
exit 0
