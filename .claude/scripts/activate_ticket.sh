#!/usr/bin/env bash
# Usage: activate_ticket.sh <TICKET_ID>
#
# Exit codes:
#   0  ACTIVATED — tmp/verify_cmd.conf 已恆覆寫為指定票號之九欄值
#   2  用法錯誤（無參數）或無法定位 repo root
#   3  票檔定位失敗（找不到票檔，或找到 >=2 個候選——禁自選）
#   4  票檔缺 TICKET_ID 或 ALLOWED 欄值（fail-closed，不寫入 conf）
#   5  票面 TICKET_ID 與傳入參數不符
#
# fail-closed：任一前提不成立時，本腳本 MUST NOT 寫入或清空既有 tmp/verify_cmd.conf。
set -u

if [ -z "${1:-}" ]; then
  echo "Usage: activate_ticket.sh <TICKET_ID>" >&2
  exit 2
fi
TICKET_ID_ARG="$1"

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

# 九欄擷取：regex 與 ticket-run/SKILL.md L96 字面一致（禁另立欄位集）
FIELDS=$(grep -E '^(TICKET_ID|CREATED|PRESET|ALLOWED|FORBIDDEN|MAX_LINES|ANCHOR_PROBED|BASE_COMMIT|TARGET_BLOB)=' "$TICKET_FILE")
TICKET_ID=$(printf '%s\n' "$FIELDS" | grep '^TICKET_ID=' | head -1 | cut -d= -f2-)
CREATED=$(printf '%s\n' "$FIELDS" | grep '^CREATED=' | head -1 | cut -d= -f2-)
PRESET=$(printf '%s\n' "$FIELDS" | grep '^PRESET=' | head -1 | cut -d= -f2-)
ALLOWED=$(printf '%s\n' "$FIELDS" | grep '^ALLOWED=' | head -1 | cut -d= -f2-)
FORBIDDEN=$(printf '%s\n' "$FIELDS" | grep '^FORBIDDEN=' | head -1 | cut -d= -f2-)
MAX_LINES=$(printf '%s\n' "$FIELDS" | grep '^MAX_LINES=' | head -1 | cut -d= -f2-)
ANCHOR_PROBED=$(printf '%s\n' "$FIELDS" | grep '^ANCHOR_PROBED=' | head -1 | cut -d= -f2-)
BASE_COMMIT=$(printf '%s\n' "$FIELDS" | grep '^BASE_COMMIT=' | head -1 | cut -d= -f2-)
TARGET_BLOB=$(printf '%s\n' "$FIELDS" | grep '^TARGET_BLOB=' | head -1 | cut -d= -f2-)

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
ALLOWED_PREVIEW=$(printf '%s' "$ALLOWED" | cut -c1-60)
echo "✅ ACTIVATED | ${TICKET_ID} | ${BRANCH} @ ${DIFF_REF} | ALLOWED=${ALLOWED_PREVIEW}"
exit 0
