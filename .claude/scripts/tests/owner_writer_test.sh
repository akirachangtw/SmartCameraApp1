#!/usr/bin/env bash
# .claude/scripts/tests/owner_writer_test.sh
# ADV-OWNER-01 — active.json owner_session_id writer matrix (isolated scratch git repo, no repo mutation).
# Drives the REAL activate_ticket.sh (claim) and the REAL write_active_json (sed-extracted from
# verify_checklist.sh the same way preflight_activation.sh does; non-claim), then one end-to-end handoff
# through the REAL registry_review_stop_guard.py.

set -uo pipefail

SRC="$(cd "$(dirname "$0")/../../.." && pwd)"
ACTIVATE="$SRC/.claude/scripts/activate_ticket.sh"
VERIFY="$SRC/.claude/skills/qa/scripts/verify_checklist.sh"
REG_HOOK="$SRC/.claude/hooks/registry_review_stop_guard.py"
S="$(mktemp -d)"
fail_count=0
trap 'rm -rf "$S"' EXIT

ok()   { echo "PASS: $1"; }
bad()  { echo "FAIL: $1"; fail_count=$((fail_count+1)); }
expect() { # expect <name> <expected> <actual>
    if [[ "$2" == "$3" ]]; then ok "$1"; else bad "$1 (expected '$2', got '$3')"; fi
}

PROJ="plans/cso_briefing/project"
setup() {
    rm -rf "$S"; mkdir -p "$S"
    (cd "$S" && git init -q && git -c user.email=t@t -c user.name=t commit -q --allow-empty -m init)
    mkdir -p "$S/$PROJ/Pending" "$S/$PROJ/deliver" "$S/tmp/verify"
}
mkticket() { # mkticket <dir> <id>
    printf '# %s\nTICKET_ID=%s\nALLOWED=a.txt\n\n## Acceptance Criteria\n- [x] a\n' "$2" "$2" > "$S/$PROJ/$1/${2}_t.md"
}
owner_of() { sed -n 's/.*"owner_session_id":[[:space:]]*"\([^"]*\)".*/\1/p' "$S/tmp/verify/active.json" | head -1; }
has_owner_key() { grep -c '"owner_session_id":' "$S/tmp/verify/active.json"; }
seed() { # seed <ticket> <owner|NONE|CORRUPT|ABSENT>
    case "$2" in
        ABSENT) rm -f "$S/tmp/verify/active.json" ;;
        CORRUPT) printf '{not json' > "$S/tmp/verify/active.json" ;;
        NONE) printf '{\n  "ticket_id": "%s",\n  "ticket_revision": "rev-0",\n  "repo_root": "x",\n  "branch": "main",\n  "head_sha": "h",\n  "activated_at": "t",\n  "activation_source": "new-session"\n}\n' "$1" > "$S/tmp/verify/active.json" ;;
        *) printf '{\n  "ticket_id": "%s",\n  "ticket_revision": "rev-0",\n  "repo_root": "x",\n  "branch": "main",\n  "head_sha": "h",\n  "activated_at": "t",\n  "activation_source": "new-session",\n  "owner_session_id": "%s"\n}\n' "$1" "$2" > "$S/tmp/verify/active.json" ;;
    esac
}
claim() { # claim <ticket> <sid|-> → sets CLAIM_OUT / CLAIM_RC
    if [[ "$2" == "-" ]]; then
        CLAIM_OUT="$(cd "$S" && env -u CLAUDE_CODE_SESSION_ID bash "$ACTIVATE" "$1" 2>&1)"; CLAIM_RC=$?
    else
        CLAIM_OUT="$(cd "$S" && CLAUDE_CODE_SESSION_ID="$2" bash "$ACTIVATE" "$1" 2>&1)"; CLAIM_RC=$?
    fi
}
FN_SRC="$(sed -n '/^get_conf()/,/^}/p;/^write_active_json()/,/^}/p' "$VERIFY")"
noclaim() { # noclaim <ticket> <sid|-> — real write_active_json, caller env SID must not matter
    local sidenv=()
    [[ "$2" == "-" ]] && sidenv=(env -u CLAUDE_CODE_SESSION_ID) || sidenv=(env CLAUDE_CODE_SESSION_ID="$2")
    (cd "$S" && "${sidenv[@]}" bash -c 'eval "$1"; PERTICKET_DIR=tmp/verify TICKET_ID="$2" REVISION=r CONF_FILE=/dev/null write_active_json' _ "$FN_SRC" "$1") >/dev/null 2>&1
}

[[ -n "$FN_SRC" ]] || { echo "FAIL: cannot extract write_active_json"; exit 1; }

# ── claim (activate_ticket.sh) ───────────────────────────────────────────────
setup; mkticket Pending T1-A; seed T1-A ABSENT
claim T1-A sid-A
expect "claim: absent file, SID present -> owner" "sid-A" "$(owner_of)"
expect "claim: exit 0" "0" "$CLAIM_RC"

setup; mkticket Pending T1-A; seed T1-A ABSENT
claim T1-A -
expect "claim: absent file, SID unavailable -> owner empty" "" "$(owner_of)"
expect "claim: SID unavailable still exit 0" "0" "$CLAIM_RC"
[[ "$CLAIM_OUT" == *"ACTIVATED"* ]] && ok "claim: SID unavailable still prints ACTIVATED" || bad "claim: SID unavailable did not print ACTIVATED"
[[ "$CLAIM_OUT" == *"⚠️"* && "$CLAIM_OUT" == *"owner"* ]] && ok "claim: SID unavailable prints owner warning" || bad "claim: SID unavailable printed no owner warning"

setup; mkticket Pending T1-A; mkticket Pending T1-B; seed T1-A sid-A
claim T1-B sid-B
expect "claim: different ticket -> new owner" "sid-B" "$(owner_of)"

setup; mkticket Pending T1-A; seed T1-A NONE
claim T1-A sid-A
expect "claim: legacy 7-field same ticket -> owner" "sid-A" "$(owner_of)"
[[ "$CLAIM_OUT" != *"🔀"* ]] && ok "claim: legacy file -> no handoff notice" || bad "claim: legacy file printed handoff notice"

setup; mkticket Pending T1-A; seed T1-A sid-A
claim T1-A sid-B
expect "claim handoff: same ticket owner A -> B" "sid-B" "$(owner_of)"
[[ "$CLAIM_OUT" == *"🔀"* ]] && ok "claim handoff: prints transfer notice" || bad "claim handoff: no transfer notice"

setup; mkticket Pending T1-A; seed T1-A sid-A
claim T1-A -
expect "claim: same ticket, SID unavailable -> owner empty (old owner not kept)" "" "$(owner_of)"

setup; mkticket Pending T1-A; seed T1-A CORRUPT
claim T1-A sid-A
expect "claim: corrupt existing file -> owner" "sid-A" "$(owner_of)"
[[ "$CLAIM_RC" == "0" && "$CLAIM_OUT" != *"🔀"* ]] && ok "claim: corrupt existing file -> exit 0, no transfer notice" || bad "claim: corrupt existing file misbehaved"

# ── non-claim (write_active_json) ───────────────────────────────────────────
for env_sid in sid-X -; do
    setup; seed T2-A sid-A; noclaim T2-B "$env_sid"
    expect "non-claim: different ticket (env=$env_sid) -> owner cleared" "" "$(owner_of)"
    setup; seed T2-A NONE; noclaim T2-B "$env_sid"
    expect "non-claim: different ticket from legacy (env=$env_sid) -> owner empty" "" "$(owner_of)"
done
for env_sid in sid-A sid-Z -; do
    setup; seed T2-A sid-A; noclaim T2-A "$env_sid"
    expect "non-claim: same ticket (env=$env_sid) -> owner preserved" "sid-A" "$(owner_of)"
done
for env_sid in sid-A -; do
    setup; seed T2-A NONE; noclaim T2-A "$env_sid"
    expect "non-claim: same ticket, no prior owner (env=$env_sid) -> still empty (no auto-claim)" "" "$(owner_of)"
    expect "non-claim: same ticket legacy -> 8-field output" "1" "$(has_owner_key)"
done
setup; seed T2-A ABSENT; noclaim T2-A sid-A
expect "non-claim: absent file -> owner empty (no auto-claim)" "" "$(owner_of)"
setup; seed T2-A CORRUPT; noclaim T2-A sid-A
expect "non-claim: corrupt file -> owner empty" "" "$(owner_of)"

# ── byte-structure isomorphism of the two writers ───────────────────────────
setup; mkticket Pending T3-A; seed T3-A ABSENT
claim T3-A sid-A
shape_a="$(sed -E 's/: "[^"]*"/: "V"/' "$S/tmp/verify/active.json")"
seed T3-A ABSENT; noclaim T3-A sid-A
shape_b="$(sed -E 's/: "[^"]*"/: "V"/' "$S/tmp/verify/active.json")"
expect "writers produce identical key order/format" "$shape_a" "$shape_b"

# ── end-to-end handoff through the real registry Stop guard ─────────────────
run_hook() { (cd "$S" && printf '%s' "{\"session_id\":\"$1\"}" | python "$REG_HOOK"); }
setup; mkticket deliver E2E-01
echo '{"tickets": [{"id": "E2E-01", "lifecycle": "PENDING"}]}' > "$S/$PROJ/TICKET_REGISTRY.json"
claim E2E-01 sid-A
out="$(run_hook sid-B)"; expect "e2e: non-owner sid-B allowed" "" "$out"
[[ -z "$(ls "$S/tmp/.stop_guard" 2>/dev/null)" ]] && ok "e2e: non-owner created no marker" || bad "e2e: non-owner created marker"
out="$(run_hook sid-A)"; [[ "$out" == *'"decision": "block"'* ]] && ok "e2e: owner sid-A blocked" || bad "e2e: owner sid-A not blocked ($out)"
claim E2E-01 sid-B
out="$(run_hook sid-A)"; expect "e2e handoff: old owner sid-A allowed" "" "$out"
out="$(run_hook sid-B)"; [[ "$out" == *'"decision": "block"'* ]] && ok "e2e handoff: new owner sid-B controlled" || bad "e2e handoff: new owner sid-B not blocked ($out)"
[[ -f "$S/tmp/.stop_guard/sid-A_E2E-01.blocked" ]] && ok "e2e handoff: old owner's marker not deleted by new owner's block" || bad "e2e handoff: old owner's marker deleted"

if [[ "$fail_count" -eq 0 ]]; then echo "ALL PASS"; exit 0; else echo "${fail_count} case(s) FAILED"; exit 1; fi
