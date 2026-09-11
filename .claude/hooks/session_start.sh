#!/usr/bin/env bash
# session_start.sh — SessionStart hook: 機械清檔（reset）／續作短注入（inject）
# 來源: INFRA-SESSION-HOOK-01
#
# 用法:
#   bash .claude/hooks/session_start.sh reset   # matcher: startup|clear
#   bash .claude/hooks/session_start.sh inject  # matcher: resume|compact|fork
#
# reset  : 清 tmp/verify_cmd.conf／sv_report.md／handoff.md，輸出 active ticket 三態＋PROGRESS.md 過期提示（僅當該檔無本地未提交變更、且舊於最後提交時）＋PROGRESS.md（≤80 行上界）
# inject : 不清任何檔，輸出 active ticket 三態＋PROGRESS.md 前 25 行＋續作提醒
#
# 恆 exit 0；任何子步驟失敗僅 echo >&2，不阻斷 session 啟動（HC-1）。
# stdout 為純文字（HC-6），不輸出 JSON 決策物件。

ACTIVE_JSON="tmp/verify/active.json"

# ─── active.json 三態（reset／inject 共用）───
report_active_ticket() {
  if [[ ! -f "$ACTIVE_JSON" ]]; then
    echo "📍 Active ticket: 無"
    return 0
  fi

  local total first last
  total="$(wc -l < "$ACTIVE_JSON" 2>/dev/null | tr -d '[:space:]')"
  first="$(sed -n '1p' "$ACTIVE_JSON" 2>/dev/null)"
  last="$(sed -n '9p' "$ACTIVE_JSON" 2>/dev/null)"

  local ok=1
  [[ "$total" == "9" ]] || ok=0
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
}

mode="${1:-}"

case "$mode" in
  reset)
    mkdir -p tmp 2>/dev/null
    rm -f tmp/verify_cmd.conf tmp/sv_report.md tmp/handoff.md 2>/dev/null
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
