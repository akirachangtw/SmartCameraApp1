#!/usr/bin/env bash
# ADV-FORBIDEXCL-01 — verify_checklist.sh [0.2] 對 EXCLUDED_PAYLOAD 具名檔之處理比照 [0.1]。
# 沙盒：mktemp 真實 git repo＋真實 conf 模式執行腳本（非 mock）。
# 用法：bash .claude/skills/qa/tests/test_verify_forbidden_excluded.sh
set -u
REAL_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)"
SCRIPT="$REAL_ROOT/.claude/skills/qa/scripts/verify_checklist.sh"
PASS=0; FAIL=0
S=""

ok()   { PASS=$((PASS + 1)); echo "  ✅ $1"; }
bad()  { FAIL=$((FAIL + 1)); echo "  ❌ $1"; }
check() { # name, condition-exit-code
  if [[ "$2" -eq 0 ]]; then ok "$1"; else bad "$1"; fi
}

setup() { # $1=EXCLUDED_PAYLOAD value (may be empty) $2=FORBIDDEN value
  S="$(mktemp -d)"
  mkdir -p "$S/tmp" "$S/dir" "$S/.claude/skills/qa/scripts"
  cp "$SCRIPT" "$S/.claude/skills/qa/scripts/"
  printf 'tmp/\n.tmp/\n' > "$S/.gitignore"
  (
    cd "$S" || exit 1
    git init -q && git config user.email t@t && git config user.name t && git config core.autocrlf false
    echo base > a.txt; echo base > f.txt; echo base > dir/x.txt; echo base > dir/y.txt
    git add -A && git commit -q -m base
    echo change >> a.txt; echo change >> f.txt; echo change >> dir/x.txt; echo change >> dir/y.txt
    git add -A && git commit -q -m "[FX-01] change"
  )
  BASE="$(cd "$S" && git rev-parse --short HEAD~1)"
  {
    printf 'TICKET_ID=FX-01\nDIFF_REF=%s\nALLOWED=a.txt\nFORBIDDEN=%s\n' "$BASE" "$2"
    [[ -n "$1" ]] && printf 'EXCLUDED_PAYLOAD=%s\n' "$1"
    printf 'MAX_LINES=N/A\nPRESET=docs\n'
  } > "$S/tmp/verify_cmd.conf"
}

run() { # runs the script in the sandbox; sets OUT and RC
  OUT="$(cd "$S" && bash .claude/skills/qa/scripts/verify_checklist.sh tmp/verify_cmd.conf 2>&1)"; RC=$?
}

teardown() { [[ -n "$S" && -d "$S" ]] && rm -rf "$S"; S=""; }

has()  { grep -qF -- "$1" <<< "$OUT"; }

echo "== (a) FORBIDDEN 檔被修改且未列 EXCLUDED_PAYLOAD → FAIL"
setup "" "f.txt"; run
has "⛔ FORBIDDEN FILE: f.txt" ; check "(a) 輸出含 FORBIDDEN FILE: f.txt" $?
[[ $RC -ne 0 ]] ; check "(a) 整體 exit 非 0（rc=$RC）" $?
teardown

echo "== (b) 同檔列於 EXCLUDED_PAYLOAD → [0.2] 不判 FAIL，[0.1] 行為不變"
# 其餘變更檔（dir/x.txt、dir/y.txt）亦授權排除，避免 [0.1] 之無關 SCOPE VIOLATION 干擾 exit 判讀
setup "f.txt,dir/x.txt,dir/y.txt" "f.txt"; run
has "📋 Excluded Payload (FORBIDDEN, authorized): f.txt" ; check "(b) [0.2] 輸出已授權排除字面" $?
! has "⛔ FORBIDDEN FILE" ; check "(b) 不含 ⛔ FORBIDDEN FILE" $?
has "📋 Excluded Payload: f.txt (authorized)" ; check "(b) [0.1] 原行為字面不變" $?
[[ $RC -eq 0 ]] ; check "(b) 無其他違規時整體 exit 0（rc=$RC）" $?
teardown

echo "== (c) 無 FORBIDDEN 命中 → 輸出與修改前一致"
setup "" "nothing-matches.txt"; run
has "✅ 無禁區檔案被修改（本 Ticket）" ; check "(c) 無禁區字面" $?
! has "Excluded Payload (FORBIDDEN" ; check "(c) 不出現新排除字面" $?
teardown

echo "== (d) EXCLUDED_PAYLOAD 為空時 [0.2] 行為不變（命中仍 FAIL）"
setup "" "dir/**"; run
has "⛔ FORBIDDEN FILE: dir/x.txt" ; check "(d) dir/x.txt 判 FORBIDDEN" $?
[[ $RC -ne 0 ]] ; check "(d) exit 非 0（rc=$RC）" $?
teardown

echo "== (e) 一個 FORBIDDEN token 命中多檔：首檔已排除、次檔未排除 → 次檔仍判 FAIL"
setup "dir/x.txt" "dir/**"; run
has "📋 Excluded Payload (FORBIDDEN, authorized): x.txt" ; check "(e) 首檔 x.txt 已授權排除" $?
has "⛔ FORBIDDEN FILE: dir/y.txt" ; check "(e) 次檔 y.txt 仍判 FORBIDDEN" $?
[[ $RC -ne 0 ]] ; check "(e) exit 非 0（rc=$RC）" $?
teardown

echo "== (f) 路徑形式 EXCLUDED token（含 /）亦比對成功"
setup "dir/x.txt,dir/y.txt,f.txt" "dir/**"; run
! has "⛔ FORBIDDEN FILE" ; check "(f) 兩檔皆已排除，無 FORBIDDEN FILE" $?
[[ $RC -eq 0 ]] ; check "(f) 整體 exit 0（rc=$RC）" $?
teardown

echo
echo "Result: PASS=$PASS FAIL=$FAIL"
[[ $FAIL -eq 0 ]]
