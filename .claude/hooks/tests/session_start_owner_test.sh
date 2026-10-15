#!/usr/bin/env bash
# .claude/hooks/tests/session_start_owner_test.sh
# ADV-OWNER-01 — session_start.sh owner status hint (reset + inject share one function).
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

    # side effects: hint adds/changes nothing beyond the mode's existing legitimate effects
    setup IN_PROGRESS; active sid-A
    before="$(snap)"; run $mode sid-B; after="$(snap)"
    [[ "$RC" == "0" ]] && ok "$mode: exit 0" || bad "$mode: exit $RC"
    changed="$(diff <(echo "$before") <(echo "$after") | grep '^[<>]' | sed -E 's/^([<>]) [0-9a-f]+ /\1 /' | sort)"
    if [[ "$mode" == "reset" ]]; then
        expected="$(printf '%s\n' '< ./.tmp/sv_report.md' '< ./tmp/handoff.md' '< ./tmp/verify_cmd.conf' | sort)"
    else
        expected=""
    fi
    [[ "$changed" == "$expected" ]] && ok "$mode: side effects limited to the mode's existing legitimate set" || bad "$mode: unexpected side effects: $changed"
    for keep in tmp/verify/active.json ".git/gate/$TID.jsonl" plans/cso_briefing/project/TICKET_REGISTRY.json; do
        b="$(grep -F " ./$keep" <<<"$before" || grep -F " $keep" <<<"$before")"; a="$(grep -F " ./$keep" <<<"$after" || grep -F " $keep" <<<"$after")"
        [[ -n "$b" && "$b" == "$a" ]] && ok "$mode: $keep unchanged" || bad "$mode: $keep changed"
    done
done

if [[ "$fail_count" -eq 0 ]]; then echo "ALL PASS"; exit 0; else echo "${fail_count} case(s) FAILED"; exit 1; fi
