#!/usr/bin/env bash
# m3_family_audit.sh — INFRA-M3-AUDIT-01 Family Provenance detective control.
#
# 非同步偵測控制（detective control）：把 ticket_pattern.md 的 family provenance
# 表 derive 成 family 白名單，對 git history 已 commit 的 ticket ID 對帳。結構合法
# 但家族未登記者（如 [FOOBAR-01]）列入 escalate report，交 CSO 語意裁決。
#
# 語意：Family-provenance（家族溯源），非 full-format 一致性。稽核「家族是否登記」，
# 不稽核「ID 是否符合家族內格式」。故 D8-STABILIZE-FIX → family D（已登記）→ 不 escalate。
#
# 鐵律：READ-ONLY + escalate，永不自動修復（DRA 保護）。唯一 write path 為
# plans/cso_briefing/reports/ 下的 dated artifact；絕不寫入 ticket_pattern.md /
# PROGRESS.md / CLAUDE.md / hooks 等 governance 檔（AC-3）。
#
# Modes:
#   (預設)         執行對帳，寫入當日 artifact，print 摘要。exit 0=無 escalate / 3=有 escalate / 2=env error
#   --self-test    確定性 self-test（AI-2 0 誤報 + FOOBAR escalate liveness），不寫檔
#   --stale-check  /dream 掛鉤：最近 artifact > 14 日或缺失 → fail-loud exit 1

set -uo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null)" || { echo "❌ not a git repo"; exit 2; }
cd "$REPO_ROOT" || exit 2

PATTERN_FILE=".claude/rules/ticket_pattern.md"
REPORT_DIR="plans/cso_briefing/reports"
STALE_DAYS=14

[[ -f "$PATTERN_FILE" ]] || { echo "❌ missing $PATTERN_FILE"; exit 2; }

# ── Structural grammar：mirror of commit-hook= SSOT line（drift-checked，非 hardcode 漂移）──
STRUCT_CORE='[A-Z][A-Z0-9]*(-[A-Z0-9]+)+'
EXPECTED_HOOK="^\\[${STRUCT_CORE}\\]"
ACTUAL_HOOK="$(grep -E '^commit-hook=' "$PATTERN_FILE" | head -1 | cut -d= -f2-)"
if [[ "$ACTUAL_HOOK" != "$EXPECTED_HOOK" ]]; then
  echo "❌ structural grammar drift: ticket_pattern.md commit-hook='${ACTUAL_HOOK}' != expected '${EXPECTED_HOOK}'"
  echo "   → 若結構文法確有變更，同步更新本腳本 STRUCT_CORE 後重跑。"
  exit 2
fi
STRUCT="^${STRUCT_CORE}$"

# ── Derive family 白名單：解析「## 合法格式一覽」表，取每列格式 cell 的字面前綴 ──
# family key = 第一個 placeholder（'['/'('）之前的字面前綴，去尾端 '-'。
# 雙段家族（STATE-CONTRACT / KAIROS-BACKFILL / KAIROS-D）完整保留，禁 first-token split（AI-2）。
derive_family_keys() {
  awk '
    /^## 合法格式一覽/ { insec=1; next }
    /^## / { if (insec) insec=0 }
    insec && /^\|/ {
      if (match($0, /`[^`]+`/)) print substr($0, RSTART+1, RLENGTH-2)
    }
  ' "$PATTERN_FILE" \
  | while IFS= read -r fmt; do
      key="${fmt%%[*}"   # cut at first '['
      key="${key%%(*}"   # cut at first '(' (safety)
      key="${key%-}"     # strip trailing '-'
      [[ -n "$key" ]] && printf '%s\n' "$key"
    done \
  | awk 'NF' | sort -u
}

mapfile -t FAMILY_KEYS < <(derive_family_keys)
[[ ${#FAMILY_KEYS[@]} -gt 0 ]] || { echo "❌ derived 0 family keys — table parse failed"; exit 2; }

# ── Family 抽取：longest boundary-prefix match。回傳最長命中 key（空=未登記）──
attribute_family() {
  local id="$1" key rest best=""
  for key in "${FAMILY_KEYS[@]}"; do
    rest="${id#"$key"}"
    [[ "$rest" == "$id" ]] && continue                       # key 非前綴
    # boundary：key 之後須為結尾 / '-' / 數字，避免 UA 誤配 UAT、D 誤配 DASH
    if [[ -z "$rest" || "$rest" == -* || "$rest" =~ ^[0-9] ]]; then
      (( ${#key} > ${#best} )) && best="$key"
    fi
  done
  printf '%s' "$best"
}

# ── Mode: --self-test（確定性，不寫檔）──
if [[ "${1:-}" == "--self-test" ]]; then
  fail=0
  # 已登記良品（含雙段家族 + D 家族後綴變體）→ MUST NOT escalate（AI-2 0 誤報）
  for good in D9-T1 D8-STABILIZE-FIX INFRA-SV-STATE-CLEANUP-01 STATE-CONTRACT-01 \
              STATE-CONTRACT-01-FIX-C KAIROS-BACKFILL-01-FIX-A KAIROS-D25 \
              TRACTION-SSOT-01 P08-F10 P0-FPS V16-A2 SPIKE-GF UA-01 HOTFIX-47; do
    if [[ ! "$good" =~ $STRUCT ]]; then echo "SELFTEST FAIL: $good 非結構合法"; fail=1; continue; fi
    if [[ -z "$(attribute_family "$good")" ]]; then echo "SELFTEST FAIL: $good 被誤 escalate（false-positive）"; fail=1; fi
  done
  # 未登記家族 → MUST escalate（FOOBAR liveness）
  for bad in FOOBAR-01 DOGE-99 XYZZY-01-A; do
    if [[ "$bad" =~ $STRUCT && -n "$(attribute_family "$bad")" ]]; then
      echo "SELFTEST FAIL: $bad 未被 escalate（false-negative）"; fail=1
    fi
  done
  # 非結構合法（legacy conventional tag）→ MUST 被結構閘濾除
  for leg in Docs Fix v2.1.1; do
    if [[ "$leg" =~ $STRUCT ]]; then echo "SELFTEST FAIL: legacy '$leg' 誤過結構閘"; fail=1; fi
  done
  if (( fail )); then echo "❌ SELF-TEST FAIL"; exit 1; fi
  echo "✅ SELF-TEST PASS — 14 良品（含 STATE-CONTRACT/KAIROS 雙段 + D8 後綴變體）0 誤報；3 未登記家族 escalate；3 legacy 濾除"
  exit 0
fi

latest_artifact_date() {
  ls "$REPORT_DIR"/M3_Family_Audit_*.md 2>/dev/null \
    | sed -E 's|.*/M3_Family_Audit_([0-9]{8})\.md$|\1|' | grep -E '^[0-9]{8}$' | sort -rn | head -1
}
days_since() {
  local ymd="$1" iso="${1:0:4}-${1:4:2}-${1:6:2}" a n
  a="$(date -d "$iso" +%s 2>/dev/null)" || return 1
  n="$(date +%s)"
  echo $(( (n - a) / 86400 ))
}

# ── Mode: --stale-check（/dream 掛鉤，fail-loud）──
if [[ "${1:-}" == "--stale-check" ]]; then
  latest="$(latest_artifact_date)"
  if [[ -z "$latest" ]]; then
    echo "🔴 M3 控制 stale：無任何 M3_Family_Audit 稽核戳 → 從未執行"; exit 1
  fi
  age="$(days_since "$latest")" || { echo "❌ date parse fail: $latest"; exit 2; }
  if (( age > STALE_DAYS )); then
    echo "🔴 M3 控制 stale：最近稽核 ${latest}（${age} 日前 > ${STALE_DAYS} 日）→ 執行 m3_family_audit.sh 刷新"; exit 1
  fi
  echo "🟢 M3 控制 fresh：最近稽核 ${latest}（${age} 日前 ≤ ${STALE_DAYS} 日）"; exit 0
fi

# ── 預設：執行對帳 ──
prev="$(latest_artifact_date)"
if [[ -n "$prev" ]]; then
  prev_age="$(days_since "$prev" 2>/dev/null || echo '?')"
  echo "ℹ️ 前次稽核戳：${prev}（${prev_age} 日前）"
else
  echo "ℹ️ 前次稽核戳：無（首次執行）"
fi

# 只取 commit 訊息「行首」ticket ID（commit-msg hook 治理範圍）。行首以外的
# 內文引用（如 `[D05-T2] ... [CSO-PATCH-001]`）非治理 ID，MUST 不納入對帳（FP2 防禦）。
mapfile -t RAW_IDS < <(
  {
    git log --format='%s' 2>/dev/null | grep -oE '^\[[^][]+\]'
    # reflog：TAB 後為 message，去 'commit: ' 等 action 前綴，取行首 bracket
    awk -F'\t' 'NF>1 { m=$2; sub(/^[^:]*: /,"",m); print m }' .git/logs/HEAD 2>/dev/null \
      | grep -oE '^\[[^][]+\]'
  } \
  | sed -E 's/^\[//; s/\]$//' \
  | sort -u
)

declare -a ESCALATE=()
declare -i n_total=0 n_legacy=0 n_struct=0 n_reg=0
for id in "${RAW_IDS[@]}"; do
  [[ -n "$id" ]] || continue
  n_total+=1
  if [[ ! "$id" =~ $STRUCT ]]; then n_legacy+=1; continue; fi   # 前結構文法時代，超出稽核範圍
  n_struct+=1
  if [[ -n "$(attribute_family "$id")" ]]; then n_reg+=1; else ESCALATE+=("$id"); fi
done

n_esc=${#ESCALATE[@]}
TODAY="$(date +%Y%m%d)"
mkdir -p "$REPORT_DIR"
REPORT="$REPORT_DIR/M3_Family_Audit_${TODAY}.md"

{
  echo "# M3 Family Provenance Audit — ${TODAY}"
  echo
  echo "**控制**: INFRA-M3-AUDIT-01（detective / READ-ONLY + escalate）"
  echo "**資料源**: \`git log --format='%s'\` + \`.git/logs/HEAD\`"
  echo "**語意**: Family-provenance（稽核家族登記，非家族內格式一致性）"
  echo "**結構文法**: \`${STRUCT}\`（mirror commit-hook= SSOT，drift-checked）"
  echo
  echo "## 統計"
  echo
  echo "| 指標 | 數 |"
  echo "|------|----|"
  echo "| 掃描 ticket ID（去重） | ${n_total} |"
  echo "| 前結構文法 legacy（結構閘濾除，超範圍） | ${n_legacy} |"
  echo "| 結構合法 | ${n_struct} |"
  echo "| 家族已登記 | ${n_reg} |"
  echo "| **Escalate（結構合法但家族未登記）** | **${n_esc}** |"
  echo
  echo "## 派生 family 白名單（${#FAMILY_KEYS[@]} 家族）"
  echo
  printf '`%s` ' "${FAMILY_KEYS[@]}"; echo
  echo
  echo "## Escalate 清單"
  echo
  if (( n_esc == 0 )); then
    echo "✅ 無 escalate — 全部結構合法 ID 家族均已登記。控制已執行並留戳（liveness PASS）。"
  else
    echo "🔴 以下 ID 結構合法但家族未於 ticket_pattern.md 登記，交 CSO 語意裁決（**MUST NOT** 自動補表 / auto-register）："
    echo
    for e in "${ESCALATE[@]}"; do echo "- \`${e}\`"; done
  fi
  echo
  echo "---"
  echo "_Generated by \`.claude/scripts/m3_family_audit.sh\`. READ-ONLY control — 本檔為唯一 write path。_"
} > "$REPORT"

echo "📁 Artifact: ${REPORT}"
echo "📊 掃描 ${n_total} · legacy ${n_legacy} · 結構合法 ${n_struct} · 已登記 ${n_reg} · escalate ${n_esc}"
if (( n_esc == 0 )); then
  echo "✅ PASS — 0 escalate（已留戳，liveness OK）"
  exit 0
else
  echo "🔴 ESCALATE — ${n_esc} 個未登記家族，見 artifact，交 CSO 裁決"
  printf '   - %s\n' "${ESCALATE[@]}"
  exit 3
fi
