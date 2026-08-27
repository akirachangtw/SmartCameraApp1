#!/usr/bin/env bash
# Usage:
#   wktree-close.sh status
#   wktree-close.sh finish <topic> [--yes]
# Env: WKTR_VERIFY_CMD (optional) — command run before merge; non-zero aborts merge.
set -euo pipefail

MAIN_ROOT=$(cd "$(git rev-parse --path-format=absolute --git-common-dir)/.." && pwd)
WKTR_DATA_DIR="$MAIN_ROOT/.claude/wktrdata"
PROGRESS_FILE="$WKTR_DATA_DIR/PROGRESS.md"
TERMINAL_STATUSES="integrated_unverified merge_blocked"

die() { echo "$*" >&2; exit 1; }

is_terminal_status() {
  local s="$1" t
  for t in $TERMINAL_STATUSES; do [ "$s" = "$t" ] && return 0; done
  return 1
}

ensure_progress_file() {
  mkdir -p "$WKTR_DATA_DIR"
  if [ ! -s "$PROGRESS_FILE" ]; then
    {
      echo "# Worktree PROGRESS (auto-reconciled view, not version-controlled)"
      echo ""
      echo "| Topic | Path | Branch | Status | Head | Updated |"
      echo "| --- | --- | --- | --- | --- | --- |"
    } > "$PROGRESS_FILE"
  fi
}

progress_row() {
  awk -F'|' -v t="$1" 'NF>=6 { tt=$2; gsub(/^ +| +$/,"",tt); if (tt==t) print }' "$PROGRESS_FILE"
}

progress_field() { # $1=topic $2=index(1=Path,2=Branch,3=Status,4=Head,5=Updated)
  local row; row=$(progress_row "$1")
  [ -z "$row" ] && return 1
  echo "$row" | awk -F'|' -v i="$2" '{ v=$(i+2); gsub(/^ +| +$/,"",v); print v }'
}

worktree_lock_reason() {
  local target="$1" cur_path="" reason="" hit=1
  while IFS= read -r line; do
    case "$line" in
      worktree\ *) cur_path="${line#worktree }" ;;
      locked*)
        if [ "$cur_path" = "$target" ]; then
          reason="${line#locked}"
          reason="${reason# }"
          hit=0
        fi
        ;;
      "") cur_path="" ;;
    esac
  done < <(git worktree list --porcelain)
  [ $hit -eq 0 ] || return 1
  printf '%s' "$reason"
}

mark_status() {
  local topic="$1" status="$2"
  awk -F'|' -v t="$topic" -v s="$status" 'BEGIN{OFS="|"} { tt=$2; gsub(/^ +| +$/,"",tt); if (NF>=6 && tt==t) { $5=" "s" " } print }' "$PROGRESS_FILE" > "$PROGRESS_FILE.tmp"
  mv "$PROGRESS_FILE.tmp" "$PROGRESS_FILE"
}

reconcile_progress() {
  ensure_progress_file
  local now; now=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
  local facts; facts=$(mktemp)
  local wt_path="" wt_head="" branch topic
  while IFS= read -r line; do
    case "$line" in
      worktree\ *) wt_path="${line#worktree }" ;;
      HEAD\ *) wt_head="${line#HEAD }" ;;
      "branch refs/heads/worktree-"*)
        branch="${line#branch refs/heads/}"
        topic="${branch#worktree-}"
        printf '%s\t%s\t%s\t%s\n' "$topic" "$wt_path" "$branch" "${wt_head:0:7}" >> "$facts"
        ;;
      "") wt_path=""; wt_head="" ;;
    esac
  done < <(git worktree list --porcelain)

  local list_topics; list_topics=$(cut -f1 "$facts")

  while IFS=$'\t' read -r ftopic fpath fbranch fhead; do
    [ -z "$ftopic" ] && continue
    if [ -n "$(progress_row "$ftopic")" ]; then
      local existing_status existing_path
      existing_status=$(progress_field "$ftopic" 3) || die "reconcile: 讀取 $ftopic 現有 Status 失敗（row 格式異常）"
      existing_path=$(progress_field "$ftopic" 1) || die "reconcile: 讀取 $ftopic 現有 Path 失敗（row 格式異常）"
      if [ "$existing_status" = "merge_blocked" ] && [ "$existing_path" = "$fpath" ]; then
        awk -F'|' -v t="$ftopic" -v h="$fhead" -v u="$now" 'BEGIN{OFS="|"} { tt=$2; gsub(/^ +| +$/,"",tt); if (NF>=6 && tt==t) { $6=" "h" "; $7=" "u" " } print }' "$PROGRESS_FILE" > "$PROGRESS_FILE.tmp" && mv "$PROGRESS_FILE.tmp" "$PROGRESS_FILE"
      else
        awk -F'|' -v t="$ftopic" -v p="$fpath" -v b="$fbranch" -v h="$fhead" -v u="$now" 'BEGIN{OFS="|"} { tt=$2; gsub(/^ +| +$/,"",tt); if (NF>=6 && tt==t) { $3=" "p" "; $4=" "b" "; $5=" active "; $6=" "h" "; $7=" "u" " } print }' "$PROGRESS_FILE" > "$PROGRESS_FILE.tmp" && mv "$PROGRESS_FILE.tmp" "$PROGRESS_FILE"
      fi
    else
      echo "| $ftopic | $fpath | $fbranch | active | $fhead | $now |" >> "$PROGRESS_FILE"
    fi
  done < "$facts"

  local all_topics; all_topics=$(awk -F'|' 'NF>=6 { tt=$2; gsub(/^ +| +$/,"",tt); if (tt!="" && tt!="Topic" && tt !~ /^-+$/) print tt }' "$PROGRESS_FILE")
  local ptopic status
  for ptopic in $all_topics; do
    if ! grep -qxF "$ptopic" <<<"$list_topics"; then
      status=$(progress_field "$ptopic" 3) || continue
      if ! is_terminal_status "$status"; then
        awk -F'|' -v t="$ptopic" 'BEGIN{OFS="|"} { tt=$2; gsub(/^ +| +$/,"",tt); if (NF>=6 && tt==t) { $3=" - "; $5=" stale " } print }' "$PROGRESS_FILE" > "$PROGRESS_FILE.tmp" && mv "$PROGRESS_FILE.tmp" "$PROGRESS_FILE"
      else
        awk -F'|' -v t="$ptopic" 'BEGIN{OFS="|"} { tt=$2; gsub(/^ +| +$/,"",tt); if (NF>=6 && tt==t) { $3=" - " } print }' "$PROGRESS_FILE" > "$PROGRESS_FILE.tmp" && mv "$PROGRESS_FILE.tmp" "$PROGRESS_FILE"
      fi
    fi
  done
  rm -f "$facts"
}

print_status() {
  local active_n stale_n mb_n iu_n
  active_n=$(grep -c '| active |' "$PROGRESS_FILE" || true)
  stale_n=$(grep -c '| stale |' "$PROGRESS_FILE" || true)
  mb_n=$(grep -c '| merge_blocked |' "$PROGRESS_FILE" || true)
  iu_n=$(grep -c '| integrated_unverified |' "$PROGRESS_FILE" || true)
  echo "active=$active_n stale=$stale_n merge_blocked=$mb_n integrated_unverified=$iu_n"
  cat "$PROGRESS_FILE"
  if [ "$iu_n" -gt 20 ]; then
    echo "NOTICE: integrated_unverified rows > 20 — consider manual PROGRESS cleanup (not automated)."
  fi
  local topic_dir_n
  topic_dir_n=$(find "$WKTR_DATA_DIR" -mindepth 1 -maxdepth 1 -type d 2>/dev/null | wc -l | tr -d ' ')
  if [ "${topic_dir_n:-0}" -gt 20 ]; then
    echo "NOTICE: $WKTR_DATA_DIR topic dirs > 20 — consider manual cleanup (not automated)."
  fi
}

resolve_target_branch() {
  if [ -n "${WKTR_MAIN_BRANCH:-}" ]; then
    printf '%s\tenv\n' "$WKTR_MAIN_BRANCH"
    return
  fi
  local origin_head
  origin_head=$(git symbolic-ref --short refs/remotes/origin/HEAD 2>/dev/null || true)
  if [ -n "$origin_head" ]; then
    printf '%s\torigin-HEAD\n' "${origin_head#origin/}"
    return
  fi
  printf '%s\tdefault\n' "main"
}

print_preview() {
  local topic="$1" branch="$2" path="$3" target_branch="$4" resolution_source="$5"
  echo "=== finish preview: $topic ==="
  echo "topic: $topic"
  echo "branch: $branch"
  echo "worktree path: $path"
  echo "main branch: $target_branch (resolution: $resolution_source)"
  echo "--- git log --oneline $target_branch..$branch ---"
  git log --oneline "$target_branch..$branch" || true
  echo "--- git diff --stat $target_branch..$branch ---"
  git diff --stat "$target_branch..$branch" || true
  if [ -n "${WKTR_VERIFY_CMD:-}" ]; then
    echo "VERIFY: ${WKTR_VERIFY_CMD} (will run before merge)"
  else
    echo "VERIFY: SKIPPED (WKTR_VERIFY_CMD unset)"
  fi
}

do_finish() {
  local topic="$1" yes="$2"
  reconcile_progress
  local branch="worktree-$topic"
  local path; path=$(progress_field "$topic" 1) || true
  [ -n "${path:-}" ] || die "對帳後 PROGRESS 仍無目標 topic 行：$topic"

  local target_branch resolution_source
  IFS=$'\t' read -r target_branch resolution_source <<<"$(resolve_target_branch)"
  [ -n "$target_branch" ] || die "無法解析目標主分支（env/origin-HEAD/default 三層皆失敗），禁猜測"

  if [ "$yes" -ne 1 ]; then
    print_preview "$topic" "$branch" "$path" "$target_branch" "$resolution_source"
    exit 1
  fi

  local current_branch; current_branch=$(git rev-parse --abbrev-ref HEAD)
  [ "$current_branch" = "$target_branch" ] || die "current=$current_branch target=$target_branch (resolution: $resolution_source) — finish 只允許整併到目標主分支，請先 git switch $target_branch"

  local common_dir git_dir
  common_dir=$(git rev-parse --path-format=absolute --git-common-dir)
  git_dir=$(git rev-parse --path-format=absolute --git-dir)
  [ "$common_dir" = "$git_dir" ] || die "finish MUST run from the main checkout, not a linked worktree"

  [ -z "$(git status --porcelain)" ] || die "main checkout not clean"

  [ -z "$(git -C "$path" status --porcelain)" ] || die "worktree $topic not clean"

  local topic_dir="$WKTR_DATA_DIR/$topic"
  local hf_dir="$topic_dir/handoffs"
  local hf_name=""
  if [ -d "$hf_dir" ]; then
    # pipefail 容忍：零 .md 時 grep 回 1，不得提前終止腳本，須落到下方既定 die（FIX-D T1FIX）
    hf_name=$(ls -1 "$hf_dir" 2>/dev/null | grep -E '\.md$' | sort | tail -1 || true)
  fi
  [ -n "$hf_name" ] || die "先執行 wktree-compact 產出交接檔：$hf_dir/"
  case "$hf_name" in
    [0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]-[0-9][0-9][0-9][0-9].md) ;;
    *) die "先執行 wktree-compact 產出交接檔：$hf_dir/ (非規檔名: $hf_name)" ;;
  esac
  local hf="$hf_dir/$hf_name"
  [ -s "$hf" ] || die "先執行 wktree-compact 產出交接檔：$hf_dir/"

  local ahead; ahead=$(git rev-list --count "$target_branch..$branch")
  [ "$ahead" -ge 1 ] || die "該 branch 無 ahead commit，無可整併內容"

  if [ -n "${WKTR_VERIFY_CMD:-}" ]; then
    eval "$WKTR_VERIFY_CMD" || die "VERIFY failed (WKTR_VERIFY_CMD), merge aborted"
  else
    echo "VERIFY: SKIPPED (WKTR_VERIFY_CMD unset)"
  fi

  if git merge --no-ff "$branch" -m "Merge worktree $topic (WKTR-01 finish)"; then
    if ! git worktree remove "$path" 2>/dev/null; then
      local lock_reason
      if lock_reason=$(worktree_lock_reason "$path"); then
        die "worktree $topic 顯式 locked（${lock_reason:-no reason given}）— 拒絕強制移除，請先手動 unlock：git worktree unlock \"$path\""
      fi
      echo "worktree remove retry: 僅 session 殘留鎖（無顯式 lock 標記），降級 -f -f"
      git worktree remove -f -f "$path"
    fi
    git branch -d "$branch"
    mark_status "$topic" integrated_unverified
    printf -- '- 狀態更新: integrated_unverified @ %s\n' "$(date -u +"%Y-%m-%dT%H:%M:%SZ")" >> "$hf"
  else
    local conflicts; conflicts=$(git diff --name-only --diff-filter=U)
    git merge --abort
    mark_status "$topic" merge_blocked
    echo "merge_blocked: conflicted files:"
    echo "$conflicts"
    echo "recovery: resolve conflicts manually in $path, or re-run finish after fixing; state left at merge_blocked."
    exit 1
  fi
}

main() {
  case "${1:-}" in
    status)
      reconcile_progress
      print_status
      ;;
    finish)
      local topic="${2:?wktree-close.sh finish <topic> [--yes]}"
      local yes=0
      [ "${3:-}" = "--yes" ] && yes=1
      do_finish "$topic" "$yes"
      ;;
    *)
      die "Usage: wktree-close.sh status | finish <topic> [--yes]"
      ;;
  esac
}

main "$@"
