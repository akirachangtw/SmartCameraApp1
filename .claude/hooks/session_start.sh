#!/usr/bin/env bash
# session_start.sh — SessionStart hook: 機械清檔（reset）／續作短注入（inject）
# 來源: INFRA-SESSION-HOOK-01
#
# 用法:
#   bash .claude/hooks/session_start.sh reset   # matcher: startup|clear
#   bash .claude/hooks/session_start.sh inject  # matcher: resume|compact|fork
#
# reset  : 清 tmp/verify_cmd.conf／.tmp/sv_report.md／handoff.md，輸出 active ticket 三態＋PROGRESS.md 過期提示（僅當該檔無本地未提交變更、且舊於最後提交時）＋PROGRESS.md（≤80 行上界）
# inject : 不清任何檔，輸出 active ticket 三態＋PROGRESS.md 前 25 行＋續作提醒
#
# 恆 exit 0；任何子步驟失敗僅 echo >&2，不阻斷 session 啟動（HC-1）。
# stdout 為純文字（HC-6），不輸出 JSON 決策物件。

ACTIVE_JSON="tmp/verify/active.json"

# ─── owner 狀態提示（唯讀、非阻斷；report_active_ticket 通過校驗後呼叫，reset／inject 共用）───
# 不寫任何檔、不認領；REVIEW／CLOSED／VOID 或無法判定 lifecycle 者不輸出。
report_owner_status() { # report_owner_status <ticket_id>
  local ticket_id="$1" lifecycle owner current
  lifecycle="$(python -c 'import json,sys
d=json.load(open("plans/cso_briefing/project/TICKET_REGISTRY.json",encoding="utf-8"))
for t in d.get("tickets",[]):
    if t.get("id")==sys.argv[1]:
        print(t.get("lifecycle") or ""); break' "$ticket_id" 2>/dev/null | tr -d '\r')"
  case "$lifecycle" in
    ""|REVIEW|CLOSED|VOID) return 0 ;;
  esac
  owner="$(sed -n 's/.*"owner_session_id":[[:space:]]*"\([^"]*\)".*/\1/p' "$ACTIVE_JSON" 2>/dev/null | head -1)"
  current="${CLAUDE_CODE_SESSION_ID:-}"
  if [[ -z "$owner" ]]; then
    echo "👤 Ticket owner: 未登記（票務 Stop guard 對 ${ticket_id} 不生效；需承接請顯式 activation，不會自動認領）"
  elif [[ -z "$current" ]]; then
    echo "👤 Ticket owner: 無法取得當前 session ID，歸屬未確認（不代表非 owner）"
  elif [[ "$owner" == "$current" ]]; then
    echo "👤 Ticket owner: 本 session 為 owner（票務 Stop guard 生效）"
  else
    echo "👤 Ticket owner: 其他 session 為 owner（本 session 不受 ${ticket_id} 票務 Stop guard 控制；需承接請顯式 activation）"
  fi
}

# ─── active.json 三態（reset／inject 共用）───
report_active_ticket() {
  if [[ ! -f "$ACTIVE_JSON" ]]; then
    echo "📍 Active ticket: 無"
    return 0
  fi

  # 舊 7 欄檔＝9 行；含選用 owner_session_id 之 8 欄檔＝10 行
  local total first last owner_cnt expect_total=9
  owner_cnt="$(grep -c '"owner_session_id":' "$ACTIVE_JSON" 2>/dev/null)"
  [[ "$owner_cnt" == "1" ]] && expect_total=10
  total="$(wc -l < "$ACTIVE_JSON" 2>/dev/null | tr -d '[:space:]')"
  first="$(sed -n '1p' "$ACTIVE_JSON" 2>/dev/null)"
  last="$(sed -n "${expect_total}p" "$ACTIVE_JSON" 2>/dev/null)"

  local ok=1
  [[ "$owner_cnt" == "0" || "$owner_cnt" == "1" ]] || ok=0
  [[ "$total" == "$expect_total" ]] || ok=0
  [[ "$first" == "{" ]] || ok=0
  [[ "$last" == "}" ]] || ok=0

  local key
  for key in ticket_id ticket_revision repo_root branch head_sha activated_at activation_source; do
    local cnt
    cnt="$(grep -c "\"${key}\":" "$ACTIVE_JSON" 2>/dev/null)"
    [[ "$cnt" == "1" ]] || { ok=0; continue; }
    local val
    val="$(sed -n "s/.*\"${key}\":[[:space:]]*\"\\([^\"]*\\)\".*/\\1/p" "$ACTIVE_JSON" 2>/dev/null)"
    [[ -n "$val" ]] || ok=0
  done

  if [[ "$ok" != "1" ]]; then
    echo "⚠️ Active ticket: active.json 不完整或損毀（已忽略；請重跑 preflight_activation.sh）"
    return 0
  fi

  local ticket_id branch head_sha activated_at
  ticket_id="$(sed -n 's/.*"ticket_id":[[:space:]]*"\([^"]*\)".*/\1/p' "$ACTIVE_JSON")"
  branch="$(sed -n 's/.*"branch":[[:space:]]*"\([^"]*\)".*/\1/p' "$ACTIVE_JSON")"
  head_sha="$(sed -n 's/.*"head_sha":[[:space:]]*"\([^"]*\)".*/\1/p' "$ACTIVE_JSON")"
  activated_at="$(sed -n 's/.*"activated_at":[[:space:]]*"\([^"]*\)".*/\1/p' "$ACTIVE_JSON")"
  echo "📍 Active ticket: ${ticket_id} | ${branch} @ ${head_sha:0:8} | activated ${activated_at}"
  report_owner_status "$ticket_id"
}

mode="${1:-}"

case "$mode" in
  reset)
    mkdir -p tmp 2>/dev/null
    rm -f tmp/verify_cmd.conf .tmp/sv_report.md tmp/handoff.md 2>/dev/null
    echo "🧹 SessionStart(reset)：tmp/ 預載檔已清"
    report_active_ticket
    if [[ -f .claude/PROGRESS.md ]]; then
      # 單檔 dirty guard：PROGRESS.md 有本地未提交變更（worktree 或 index）即注入新內容，不得宣告「前次狀態」
      if [[ -z "$(git status --porcelain -- .claude/PROGRESS.md 2>/dev/null)" ]]; then
        p_mtime="$(git log -1 --format=%ct -- .claude/PROGRESS.md 2>/dev/null)"
        last_ct="$(git log -1 --format=%ct 2>/dev/null)"
        if [[ "$p_mtime" =~ ^[0-9]+$ && "$last_ct" =~ ^[0-9]+$ && "$p_mtime" -lt "$last_ct" ]]; then
          echo "⚠️ PROGRESS.md 舊於最後提交（可能未 checkpoint）——內容為前次狀態"
        fi
      fi
      progress_n="$(wc -l < .claude/PROGRESS.md 2>/dev/null | tr -d '[:space:]')"
      if [[ "$progress_n" =~ ^[0-9]+$ && "$progress_n" -gt 80 ]]; then
        echo "⚠️ PROGRESS.md ${progress_n} 行 > 80（ssot-governance Check F.2）— 僅注入前 80 行"
        head -n 80 .claude/PROGRESS.md
      else
        cat .claude/PROGRESS.md 2>/dev/null
      fi
    else
      echo "⚠️ .claude/PROGRESS.md 不存在" >&2
    fi
    ;;
  inject)
    report_active_ticket
    if [[ -f .claude/PROGRESS.md ]]; then
      head -n 25 .claude/PROGRESS.md
    else
      echo "⚠️ .claude/PROGRESS.md 不存在" >&2
    fi
    echo "🔁 post-compact/resume：依 CLAUDE.md §5 重新 Read 操作檔"
    if [[ -f .claude/skills/qa/scripts/preflight_activation.sh ]]; then
      echo "🔁 票續作前先跑 preflight_activation.sh"
    fi
    ;;
  *)
    echo "⚠️ session_start.sh: unknown mode '${mode}' (expected reset|inject)" >&2
    ;;
esac

exit 0
