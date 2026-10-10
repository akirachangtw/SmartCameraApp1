#!/usr/bin/env bash
# Fixture: REPORT_FIX 不耗 BLOCKER ROUND + 有界閉環 + closure 守門（ADV-REPORTFIX-01）
# 只在 scratch fake git repo 內執行（stub codex）；不寫真 repo／真 .tmp／真 worktree。
# 用法：bash .claude/skills/qa/tests/test_report_fix_closure.sh   （exit 0 = 全 PASS）
set -u

REAL_ROOT="$(git rev-parse --show-toplevel)"
S=""
BASE=""
RUN_N=0
FAILS=0
CLOSURE=0
RC=0
OUT=""

check() { # name expected actual
  if [[ "$2" == "$3" ]]; then echo "PASS  $1 | $3"; else echo "FAIL  $1 | expected=[$2] actual=[$3]"; FAILS=$((FAILS + 1)); fi
}

join() { local IFS=/; echo "$*"; }
has() { grep -q -- "$1" "$2" 2>/dev/null && echo 1 || echo 0; }
calls() { cat "$S/codex_calls" 2>/dev/null || echo 0; }
state_get() { grep "^$1=" "$S/.tmp/cross_review_state/FX-01" 2>/dev/null | head -1 | cut -d= -f2-; }

setup_scratch() {
  S="$(mktemp -d)"
  mkdir -p "$S/bin" "$S/_out" "$S/tmp" "$S/.tmp" "$S/.claude/skills/sv/scripts" "$S/.claude/skills/qa/scripts"
  cp "$REAL_ROOT/.claude/skills/sv/scripts/cross_review.sh" "$S/.claude/skills/sv/scripts/"
  cp "$REAL_ROOT/.claude/skills/qa/scripts/structural_verify.sh" "$REAL_ROOT/.claude/skills/qa/scripts/lib_verify.sh" "$S/.claude/skills/qa/scripts/"
  cp "$REAL_ROOT/.claude/verify-policy.env" "$S/.claude/"
  printf 'tmp/\n.tmp/\nbin/\n_out/\ncodex_calls\n' > "$S/.gitignore"
  cat > "$S/bin/codex" <<'STUB'
#!/usr/bin/env bash
cat > /dev/null
n="$(cat "$STUB_COUNT" 2>/dev/null || echo 0)"
echo $((n + 1)) > "$STUB_COUNT"
echo "codex"
echo "${STUB_VERDICT:-PASS}"
echo "REACHABILITY: NONE"
STUB
  chmod +x "$S/bin/codex"
  if [[ "$(PATH="$S/bin:$PATH" command -v codex)" != "$S/bin/codex" ]]; then
    echo "ABORT: codex does not resolve to the stub"; rm -rf "$S"; exit 2
  fi
  (
    cd "$S" || exit 1
    git init -q && git config user.email t@t && git config user.name t && git config core.autocrlf false
    echo base > a.txt && git add -A && git commit -q -m base
    echo change >> a.txt && git add -A && git commit -q -m change
  )
  BASE="$(cd "$S" && git rev-parse --short HEAD~1)"
  printf 'TICKET_ID=FX-01\nDIFF_REF=%s\nALLOWED=a.txt\n' "$BASE" > "$S/tmp/verify_cmd.conf"
}

teardown_scratch() { [[ -n "$S" && -d "$S" ]] && rm -rf "$S"; }

write_report() { # good | noStageB | ghost
  {
    echo "# SV Report"; echo "## Files Changed"; echo "- a.txt"
    [[ "$1" == "ghost" ]] && echo "- ghost.txt"
    echo "## Implementation Scope"; echo x; echo "## Stage A"; echo x
    [[ "$1" != "noStageB" ]] && { echo "## Stage B"; echo x; }
    echo "## Stage C"; echo x
  } > "$S/.tmp/sv_report.md"
}

reset_state() { rm -rf "$S/.tmp/cross_review_state" "$S/.tmp/cross_review_pass" "$S/tmp/.fail_budget_state" "$S/codex_calls"; }

run_cr() { # stub verdict
  RUN_N=$((RUN_N + 1)); OUT="$S/_out/run_${RUN_N}.txt"
  ( cd "$S" && PATH="$S/bin:$PATH" STUB_VERDICT="$1" STUB_COUNT="$S/codex_calls" REPORT_FIX_CLOSURE="$CLOSURE" \
      bash .claude/skills/sv/scripts/cross_review.sh .tmp/sv_report.md "$BASE..HEAD" ) > "$OUT" 2>&1
  RC=$?
}

run_seq() { # report_kind verdict...  -> RCS LIM CALLS CNT
  write_report "$1"; shift; reset_state; RCS=(); LIM=(); CALLS=(); CNT=()
  local v
  for v in "$@"; do
    run_cr "$v"
    RCS+=("$RC"); LIM+=("$(grep -c '^CHECKPOINT_REPORT_FIX_LIMIT$' "$OUT")"); CALLS+=("$(calls)"); CNT+=("$(state_get REPORT_FIX_COUNT)")
  done
}

run_cases() {
  local v

  # F1：structural 來源，報告缺 ## Stage B
  write_report noStageB; reset_state; run_cr PASS
  check "F1 exit" 3 "$RC"
  check "F1 [D] marker" 1 "$(has '\[D\]' "$OUT")"
  check "F1 source token" 1 "$(has '^REPORT_FIX_SOURCE=structural_preflight$' "$OUT")"
  check "F1 codex calls" 0 "$(calls)"
  check "F1 REPORT_FIX_COUNT" 1 "$(state_get REPORT_FIX_COUNT)"

  # F2：Codex 來源 REPORT_FIX→REPORT_FIX→BLOCKER
  run_seq good REPORT_FIX REPORT_FIX BLOCKER
  check "F2 exits" 3/3/1 "$(join "${RCS[@]}")"
  check "F2 state ROUND" 1 "$(state_get ROUND)"
  check "F2 state REPORT_FIX_COUNT" 2 "$(state_get REPORT_FIX_COUNT)"
  check "F2 state LAST" BLOCKER "$(state_get LAST)"
  check "F2 no PASS receipt" 0 "$([[ -e "$S/.tmp/cross_review_pass/FX-01" ]] && echo 1 || echo 0)"

  # F3：BLOCKER×3
  run_seq good BLOCKER BLOCKER BLOCKER
  check "F3 exits" 1/1/2 "$(join "${RCS[@]}")"

  # F4：structural 來源 REPORT_FIX×3（ghost 報告）
  run_seq ghost PASS PASS PASS
  check "F4 exits" 3/3/2 "$(join "${RCS[@]}")"
  check "F4 REPORT_FIX_COUNT" 1/2/2 "$(join "${CNT[@]}")"
  check "F4 LIMIT token" 0/0/1 "$(join "${LIM[@]}")"
  check "F4 codex calls" 0/0/0 "$(join "${CALLS[@]}")"

  # F4b：Codex 來源 REPORT_FIX×3
  run_seq good REPORT_FIX REPORT_FIX REPORT_FIX
  check "F4b exits" 3/3/2 "$(join "${RCS[@]}")"
  check "F4b LIMIT token" 0/0/1 "$(join "${LIM[@]}")"
  check "F4b codex calls" 1/2/3 "$(join "${CALLS[@]}")"

  # F5：closure 守門
  write_report good; reset_state
  mkdir -p "$S/.tmp/cross_review_state"
  printf 'TICKET=FX-01\nROUND=0\nREPORT_FIX_COUNT=1\nLAST=REPORT_FIX\n' > "$S/.tmp/cross_review_state/FX-01"
  local sum0; sum0="$(cksum < "$S/.tmp/cross_review_state/FX-01")"
  CLOSURE=1
  echo dirty >> "$S/a.txt"; run_cr PASS
  check "F5 tracked exit" 2 "$RC"
  check "F5 tracked ESCALATE" 1 "$(has '^REPORT_FIX_ESCALATE$' "$OUT")"
  check "F5 tracked state unchanged" "$sum0" "$(cksum < "$S/.tmp/cross_review_state/FX-01")"
  check "F5 tracked codex calls" 0 "$(calls)"
  (cd "$S" && git checkout -q a.txt)
  echo n > "$S/new.txt"; run_cr PASS
  check "F5 untracked exit" 2 "$RC"
  check "F5 untracked ESCALATE" 1 "$(has '^REPORT_FIX_ESCALATE$' "$OUT")"
  check "F5 untracked state unchanged" "$sum0" "$(cksum < "$S/.tmp/cross_review_state/FX-01")"
  check "F5 untracked codex calls" 0 "$(calls)"
  rm -f "$S/new.txt"
  echo c > "$S/.tmp/scratch.txt"; run_cr PASS
  check "F5 control (.tmp only) exit" 0 "$RC"
  CLOSURE=0

  # F6：preflight 只跑 A/B/C/D；--post 才進 E
  write_report good; reset_state
  ( cd "$S" && bash .claude/skills/qa/scripts/structural_verify.sh .tmp/sv_report.md "$BASE..HEAD" ) > "$S/_out/f6_pre.txt" 2>&1; RC=$?
  check "F6 pre exit" 0 "$RC"
  check "F6 pre no E/F/G" 0 "$(grep -c '\[E\]\|\[F\]\|\[G\]' "$S/_out/f6_pre.txt")"
  check "F6 pre no fail_budget_state" 0 "$([[ -e "$S/tmp/.fail_budget_state" ]] && echo 1 || echo 0)"
  ( cd "$S" && bash .claude/skills/qa/scripts/structural_verify.sh .tmp/sv_report.md "$BASE..HEAD" --post ) > "$S/_out/f6_post.txt" 2>&1; RC=$?
  check "F6 post exit" 1 "$RC"
  check "F6 post blocked by [E]" 1 "$(has 'STRUCTURAL FAIL — \[E\]' "$S/_out/f6_post.txt")"
  check "F6 post [D] once" 1 "$(grep -c '=== \[D\]' "$S/_out/f6_post.txt")"

  # F7：舊格式 state（無 REPORT_FIX_COUNT）＋ REPORT_FIX
  write_report good; reset_state
  mkdir -p "$S/.tmp/cross_review_state"
  printf 'TICKET=FX-01\nROUND=1\nLAST=BLOCKER\n' > "$S/.tmp/cross_review_state/FX-01"
  run_cr REPORT_FIX
  check "F7 exit" 3 "$RC"
  check "F7 ROUND kept" 1 "$(state_get ROUND)"
  check "F7 REPORT_FIX_COUNT" 1 "$(state_get REPORT_FIX_COUNT)"

  # F8：BLOCKER,REPORT_FIX,BLOCKER,BLOCKER
  run_seq good BLOCKER REPORT_FIX BLOCKER BLOCKER
  check "F8 exits" 1/3/1/2 "$(join "${RCS[@]}")"
}

if [[ "${BASH_SOURCE[0]}" == "$0" ]]; then
  setup_scratch
  trap teardown_scratch EXIT
  run_cases
  echo "----"
  if [[ $FAILS -eq 0 ]]; then echo "ALL PASS"; else echo "FAILED: $FAILS check(s)"; exit 1; fi
fi
