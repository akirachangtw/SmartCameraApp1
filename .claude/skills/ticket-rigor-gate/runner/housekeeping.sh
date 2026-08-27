#!/usr/bin/env bash
# repair_runner.sh 之附屬 housekeeping：清掃 TELEMETRY_ROOT 下逾額 run 目錄（P1 額度制，N=10）。
# 用法：bash housekeeping.sh <TELEMETRY_ROOT> [--dry-run]
#
# 構造性射程限定：僅操作 <TELEMETRY_ROOT>/<ticket_id>/<run_id>/ 兩層結構之 run 目錄；
# 每 ticket_id 保留最近 10 個 run，截斷序＝mtime 最舊者先刪。
# 逸出守門：每一刪除候選之 realpath MUST 位於 TELEMETRY_ROOT 之下；symlink／`..` 逸出
# 即整體中止、零刪除、非零警示。非兩層結構之項目一律跳過不刪。
# 一般失敗僅 stderr 警示；逸出偵測分支 exit 1 供呼叫端/automation 憑退出碼區分（RGR-ARTIFACTS-01-FIX-A）。汙染隔離由呼叫端子 shell || true 承擔。

set -uo pipefail

KEEP_N=10
TELEMETRY_ROOT_ARG="${1:-}"
DRY_RUN=0
[[ "${2:-}" == "--dry-run" ]] && DRY_RUN=1

if [[ -z "$TELEMETRY_ROOT_ARG" || ! -d "$TELEMETRY_ROOT_ARG" ]]; then
  echo "⚠️ housekeeping: TELEMETRY_ROOT 不存在或未指定：$TELEMETRY_ROOT_ARG" >&2
  exit 0
fi

TELEMETRY_ROOT_ABS="$(realpath -m -- "$TELEMETRY_ROOT_ARG")"

CANDIDATES=()
ESCAPE_DETECTED=0

for ticket_dir in "$TELEMETRY_ROOT_ABS"/*/; do
  [[ -d "$ticket_dir" ]] || continue
  ticket_dir="${ticket_dir%/}"
  ticket_dir_abs="$(realpath -m -- "$ticket_dir")"
  case "$ticket_dir_abs" in
    "$TELEMETRY_ROOT_ABS"/*) ;;
    *) ESCAPE_DETECTED=1; break ;;
  esac

  run_dirs=()
  while IFS= read -r -d '' entry; do
    if [[ -L "$entry" ]]; then
      entry_abs="$(realpath -m -- "$entry")"
      case "$entry_abs" in
        "$TELEMETRY_ROOT_ABS"/*) ;;
        *) ESCAPE_DETECTED=1 ;;
      esac
      continue
    fi
    [[ -d "$entry" ]] && run_dirs+=("$entry")
  done < <(find "$ticket_dir" -mindepth 1 -maxdepth 1 -print0 2>/dev/null)
  [[ "$ESCAPE_DETECTED" -eq 1 ]] && break

  n="${#run_dirs[@]}"
  [[ "$n" -le "$KEEP_N" ]] && continue

  mapfile -t sorted < <(for d in "${run_dirs[@]}"; do printf '%s\t%s\n' "$(stat -c %Y "$d")" "$d"; done | sort -n | cut -f2-)

  to_delete=$(( n - KEEP_N ))
  for ((i = 0; i < to_delete; i++)); do
    cand="${sorted[$i]}"
    cand_abs="$(realpath -m -- "$cand")"
    case "$cand_abs" in
      "$TELEMETRY_ROOT_ABS"/*) CANDIDATES+=("$cand") ;;
      *) ESCAPE_DETECTED=1 ;;
    esac
  done
  [[ "$ESCAPE_DETECTED" -eq 1 ]] && break
done

if [[ "$ESCAPE_DETECTED" -eq 1 ]]; then
  echo "⚠️ housekeeping: 偵測到刪除候選逸出 TELEMETRY_ROOT（symlink 或路徑異常），整體中止，零刪除" >&2
  exit 1
fi

for cand in "${CANDIDATES[@]}"; do
  if [[ "$DRY_RUN" -eq 1 ]]; then
    echo "would delete: $cand"
  else
    rm -rf -- "$cand" 2>/dev/null || echo "⚠️ housekeeping: 刪除失敗：$cand" >&2
  fi
done

exit 0
