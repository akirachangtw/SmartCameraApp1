#!/usr/bin/env bash
# .claude/hooks/tests/session_start_owner_test.sh
# ADV-OWNER-01 — session_start.sh owner status hint (reset + inject share one function).
# ADV-RESETISO-01 — reset isolation matrix (protect vs existing legitimate reset).
# Isolated scratch repo; real session_start.sh copied in. Asserts hint *state semantics* and that the new hint
# adds/changes no side effect (reset keeps its existing legitimate deletions; inject changes nothing).

set -uo pipefail

SRC="$(cd "$(dirname "$0")/../../.." && pwd)"
HOOK_SRC="$SRC/.claude/hooks/session_start.sh"
S="$(mktemp -d)"
fail_count=0
trap 'rm -rf "$S"' EXIT

ok()  { echo "PASS: $1"; }
bad() { echo "FAIL: $1"; fail_count=$((fail_count+1)); }

TID="OWN-01"
setup() { # setup <lifecycle>
    rm -rf "$S"; mkdir -p "$S/.claude/hooks" "$S/tmp/verify" "$S/.tmp" "$S/plans/cso_briefing/project" "$S/.git/gate"
    cp "$HOOK_SRC" "$S/.claude/hooks/session_start.sh"
    echo '{"tickets": [{"id": "'"$TID"'", "lifecycle": "'"$1"'"}]}' > "$S/plans/cso_briefing/project/TICKET_REGISTRY.json"
    echo "# PROGRESS" > "$S/.claude/PROGRESS.md"
    echo '{"e":1}' > "$S/.git/gate/$TID.jsonl"
    echo "TICKET_ID=$TID" > "$S/tmp/verify_cmd.conf"
    echo "r" > "$S/.tmp/sv_report.md"
    echo "h" > "$S/tmp/handoff.md"
}
active() { # active <owner|NONE|EMPTY>
    case "$1" in
        NONE) printf '{\n  "ticket_id": "%s",\n  "ticket_revision": "rev-0",\n  "repo_root": "x",\n  "branch": "main",\n  "head_sha": "abcdef012345",\n  "activated_at": "t",\n  "activation_source": "new-session"\n}\n' "$TID" > "$S/tmp/verify/active.json" ;;
        EMPTY) printf '{\n  "ticket_id": "%s",\n  "ticket_revision": "rev-0",\n  "repo_root": "x",\n  "branch": "main",\n  "head_sha": "abcdef012345",\n  "activated_at": "t",\n  "activation_source": "new-session",\n  "owner_session_id": ""\n}\n' "$TID" > "$S/tmp/verify/active.json" ;;
        *) printf '{\n  "ticket_id": "%s",\n  "ticket_revision": "rev-0",\n  "repo_root": "x",\n  "branch": "main",\n  "head_sha": "abcdef012345",\n  "activated_at": "t",\n  "activation_source": "new-session",\n  "owner_session_id": "%s"\n}\n' "$TID" "$1" > "$S/tmp/verify/active.json" ;;
    esac
}
snap() { (cd "$S" && { find . -type f -not -path './.git/*' ; find .git/gate -type f 2>/dev/null; } | sort | while read -r f; do printf '%s %s\n' "$(sha1sum "$f" | cut -c1-12)" "$f"; done); }
run() { # run <mode> <sid|-> → OUT, RC
    if [[ "$2" == "-" ]]; then
        OUT="$(cd "$S" && env -u CLAUDE_CODE_SESSION_ID bash .claude/hooks/session_start.sh "$1" 2>/dev/null)"; RC=$?
    else
        OUT="$(cd "$S" && CLAUDE_CODE_SESSION_ID="$2" bash .claude/hooks/session_start.sh "$1" 2>/dev/null)"; RC=$?
    fi
}
has() { [[ "$OUT" == *"$1"* ]]; }

for mode in reset inject; do
    # owner states (active lifecycle)
    setup IN_PROGRESS; active EMPTY;  run $mode sid-A; has "未登記" && ok "$mode: empty owner -> 未登記" || bad "$mode: empty owner not reported as 未登記"
    setup IN_PROGRESS; active NONE;   run $mode sid-A; has "未登記" && ok "$mode: legacy 7-field -> 未登記" || bad "$mode: legacy not 未登記"
    has "不完整或損毀" && bad "$mode: legacy 7-field wrongly reported corrupt" || ok "$mode: legacy 7-field not reported corrupt"
    has "Active ticket: $TID" && ok "$mode: legacy still reports active ticket" || bad "$mode: legacy active ticket line missing"
    setup IN_PROGRESS; active sid-A;  run $mode -;     has "歸屬未確認" && ok "$mode: SID unavailable -> 歸屬未確認" || bad "$mode: SID unavailable not 歸屬未確認"
    has "其他 session" && bad "$mode: SID unavailable misreported as other-session owner" || ok "$mode: SID unavailable not reported as other-session owner"
    setup IN_PROGRESS; active sid-A;  run $mode sid-A; has "本 session 為 owner" && ok "$mode: matching SID -> 本 session 為 owner" || bad "$mode: matching SID not reported"
    has "不完整或損毀" && bad "$mode: 8-field wrongly reported corrupt" || ok "$mode: 8-field not reported corrupt"
    setup IN_PROGRESS; active sid-A;  run $mode sid-B; has "其他 session 為 owner" && ok "$mode: different SID -> 其他 session 為 owner" || bad "$mode: different SID not reported"
    # silent lifecycles
    for lc in CLOSED REVIEW VOID; do
        setup $lc; active sid-A; run $mode sid-B
        has "Ticket owner" && bad "$mode: $lc ticket still shows owner hint" || ok "$mode: $lc ticket shows no owner hint"
    done
    setup IN_PROGRESS; active sid-A; echo '{"tickets": []}' > "$S/plans/cso_briefing/project/TICKET_REGISTRY.json"; run $mode sid-B
    has "Ticket owner" && bad "$mode: ticket missing from registry still shows hint" || ok "$mode: ticket missing from registry shows no hint"
    setup IN_PROGRESS; active sid-A; echo '{broken' > "$S/plans/cso_briefing/project/TICKET_REGISTRY.json"; run $mode sid-B
    has "Ticket owner" && bad "$mode: unreadable registry still shows hint" || ok "$mode: unreadable registry shows no hint (silent)"
    [[ "$RC" == "0" ]] && ok "$mode: unreadable registry still exit 0" || bad "$mode: unreadable registry exit $RC"

    # side effects (inject): the hint adds/changes nothing at all (reset side effects are covered by the matrix below)
    if [[ "$mode" == "inject" ]]; then
        setup IN_PROGRESS; active sid-A
        before="$(snap)"; run $mode sid-B; after="$(snap)"
        [[ "$RC" == "0" ]] && ok "$mode: exit 0" || bad "$mode: exit $RC"
        [[ "$before" == "$after" ]] && ok "$mode: no side effects at all" || bad "$mode: unexpected side effects"
    fi
done

# ─────────────────────────────────────────────────────────────────────────────
# ADV-RESETISO-01 — reset isolation matrix (reset only). Real session_start.sh in a scratch repo.
# KEEP  = nothing in the tree changes (three runtime files, active.json, ledger, registry all byte-identical).
# CLEAR = existing legitimate reset: exactly the runtime files that existed are deleted, nothing else changes.
# ─────────────────────────────────────────────────────────────────────────────
set_active() { # set_active <ABSENT|CORRUPT|EMPTYTID|NONE|EMPTY|owner-value>
    case "$1" in
        ABSENT) rm -f "$S/tmp/verify/active.json" ;;
        CORRUPT) printf '{not json' > "$S/tmp/verify/active.json" ;;
        EMPTYTID) printf '{\n  "ticket_id": "",\n  "ticket_revision": "rev-0",\n  "repo_root": "x",\n  "branch": "main",\n  "head_sha": "abcdef012345",\n  "activated_at": "t",\n  "activation_source": "new-session",\n  "owner_session_id": "sid-A"\n}\n' > "$S/tmp/verify/active.json" ;;
        *) active "$1" ;;
    esac
}
set_conf() { # set_conf <SAME|OTHER|NOLINE|MISSING>
    case "$1" in
        SAME) echo "TICKET_ID=$TID" > "$S/tmp/verify_cmd.conf" ;;
        OTHER) echo "TICKET_ID=OTHER-9" > "$S/tmp/verify_cmd.conf" ;;
        NOLINE) echo "x" > "$S/tmp/verify_cmd.conf" ;;
        MISSING) rm -f "$S/tmp/verify_cmd.conf" ;;
    esac
}
shape_ok() { # shape_ok <active_spec> <conf_spec> -> starting shape asserted before the operation under test
    local a="$1" c="$2" f="$S/tmp/verify/active.json" conf_t
    case "$a" in
        ABSENT) [[ ! -e "$f" ]] || return 1 ;;
        CORRUPT) [[ -f "$f" && -z "$(sed -n 's/.*"ticket_id":[[:space:]]*"\([^"]*\)".*/\1/p' "$f")" ]] || return 1 ;;
        EMPTYTID) [[ -f "$f" ]] && grep -q '"ticket_id": ""' "$f" || return 1 ;;
        NONE) [[ -f "$f" && "$(grep -c '"owner_session_id":' "$f")" == "0" ]] || return 1 ;;
        EMPTY) [[ -f "$f" ]] && grep -q '"owner_session_id": ""' "$f" || return 1 ;;
        *) [[ -f "$f" ]] && grep -q "\"owner_session_id\": \"$a\"" "$f" || return 1 ;;
    esac
    conf_t="$(grep -m1 '^TICKET_ID=' "$S/tmp/verify_cmd.conf" 2>/dev/null | cut -d= -f2-)"
    case "$c" in
        SAME) [[ "$conf_t" == "$TID" ]] || return 1 ;;
        OTHER) [[ "$conf_t" == "OTHER-9" ]] || return 1 ;;
        NOLINE) [[ -f "$S/tmp/verify_cmd.conf" && -z "$conf_t" ]] || return 1 ;;
        MISSING) [[ ! -e "$S/tmp/verify_cmd.conf" ]] || return 1 ;;
    esac
    [[ -f "$S/.tmp/sv_report.md" && -f "$S/tmp/handoff.md" ]] || return 1
}
matrix() { # matrix <name> <active_spec> <conf_spec> <env-sid|-> <KEEP|CLEAR>
    local name="$1" a="$2" c="$3" sid="$4" want="$5" exp="" f
    setup IN_PROGRESS; set_active "$a"; set_conf "$c"
    shape_ok "$a" "$c" || { bad "matrix[$name]: starting shape not as seeded"; return; }
    before="$(snap)"; run reset "$sid"; after="$(snap)"
    [[ "$RC" == "0" ]] || bad "matrix[$name]: exit $RC"
    if [[ "$want" == "KEEP" ]]; then
        [[ "$before" == "$after" ]] && ok "matrix[$name]: KEEP (tree byte-identical, exit 0)" || bad "matrix[$name]: KEEP expected but tree changed"
    else
        for f in ./.tmp/sv_report.md ./tmp/handoff.md ./tmp/verify_cmd.conf; do
            if grep -qF " $f" <<<"$before"; then exp+="< $f"$'\n'; fi
        done
        exp="$(printf '%s' "$exp" | sort)"
        changed="$(diff <(echo "$before") <(echo "$after") | grep '^[<>]' | sed -E 's/^([<>]) [0-9a-f]+ /\1 /' | sort)"
        [[ "$changed" == "$exp" ]] && ok "matrix[$name]: CLEAR (existing legitimate reset only)" || bad "matrix[$name]: CLEAR expected, changes were: $changed"
    fi
}
matrix "1 A owner=SID"                 sid-A SAME  sid-A CLEAR
matrix "2 B owner!=SID"                sid-A SAME  sid-B KEEP
matrix "2b B conf other ticket"        sid-A OTHER sid-B KEEP
matrix "3 C SID unavailable"           sid-A SAME  -     KEEP
matrix "4 /clear new SID [stub]"       sid-A SAME  sid-NEW KEEP
matrix "5 D owner empty conf=active"   EMPTY SAME  sid-B KEEP
matrix "6 D legacy no owner key"       NONE  SAME  sid-B KEEP
matrix "7 owner empty conf other"      EMPTY OTHER sid-B CLEAR
matrix "8 owner empty conf no line"    NONE  NOLINE sid-B CLEAR
matrix "9 owner empty conf missing"    EMPTY MISSING sid-B CLEAR
matrix "10 active.json absent"         ABSENT SAME sid-B CLEAR
matrix "11 active.json corrupt"        CORRUPT SAME sid-B CLEAR
matrix "12 active ticket_id empty"     EMPTYTID SAME sid-B CLEAR

# /clear [stub]: new SID does not claim and does not clear owner
setup IN_PROGRESS; set_active sid-A; set_conf SAME; run reset sid-NEW
grep -q '"owner_session_id": "sid-A"' "$S/tmp/verify/active.json" && ok "clear [stub]: owner untouched after reset with new SID" || bad "clear [stub]: owner changed"

# chain: real activate_ticket.sh writes owner+conf, real session_start.sh reset consumes them
setup IN_PROGRESS; rm -f "$S/tmp/verify_cmd.conf" "$S/tmp/verify/active.json"
mkdir -p "$S/.claude/scripts" "$S/plans/cso_briefing/project/Pending"
cp "$SRC/.claude/scripts/activate_ticket.sh" "$S/.claude/scripts/activate_ticket.sh"
printf '# %s\nTICKET_ID=%s\nALLOWED=a.txt\n' "$TID" "$TID" > "$S/plans/cso_briefing/project/Pending/${TID}_t.md"
(cd "$S" && git init -q && git -c user.email=t@t -c user.name=t commit -q --allow-empty -m init)
(cd "$S" && CLAUDE_CODE_SESSION_ID=sid-A bash .claude/scripts/activate_ticket.sh "$TID" >/dev/null 2>&1)
if grep -q '"owner_session_id": "sid-A"' "$S/tmp/verify/active.json" && grep -qx "TICKET_ID=$TID" "$S/tmp/verify_cmd.conf"; then
    ok "chain: real activate_ticket.sh wrote owner=sid-A and conf TICKET_ID"
else
    bad "chain: producer did not write owner/conf as expected"
fi
echo "r" > "$S/.tmp/sv_report.md"; echo "h" > "$S/tmp/handoff.md"
before="$(snap)"; run reset sid-B; after="$(snap)"
[[ "$RC" == "0" && "$before" == "$after" ]] && ok "chain: reset with sid-B keeps everything" || bad "chain: reset with sid-B changed the tree"
run reset sid-A
if [[ ! -e "$S/tmp/verify_cmd.conf" && ! -e "$S/.tmp/sv_report.md" && ! -e "$S/tmp/handoff.md" && -f "$S/tmp/verify/active.json" ]]; then
    ok "chain: reset with owner SID clears the three files, active.json stays"
else
    bad "chain: reset with sid-A did not behave as legitimate reset"
fi

if [[ "$fail_count" -eq 0 ]]; then echo "ALL PASS"; exit 0; else echo "${fail_count} case(s) FAILED"; exit 1; fi
