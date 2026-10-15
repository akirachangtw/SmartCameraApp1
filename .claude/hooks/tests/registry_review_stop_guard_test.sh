#!/usr/bin/env bash
# .claude/hooks/tests/registry_review_stop_guard_test.sh
# INFRA-REGISTRY-LIFECYCLE-GATE-APP-01 regression test — isolated scratch cwd, no repo mutation.

set -uo pipefail

HOOK="$(cd "$(dirname "$0")/.." && pwd)/registry_review_stop_guard.py"
SCRATCH="$(mktemp -d)"
fail_count=0

setup() {
    rm -rf "$SCRATCH"
    mkdir -p "$SCRATCH/plans/cso_briefing/project/Pending" \
             "$SCRATCH/plans/cso_briefing/project/deliver" \
             "$SCRATCH/tmp/verify" \
             "$SCRATCH/tmp/.stop_guard"
}

write_registry() {
    # $1 = ticket_id, $2 = lifecycle
    cat > "$SCRATCH/plans/cso_briefing/project/TICKET_REGISTRY.json" << EOF
{"tickets": [{"id": "$1", "lifecycle": "$2"}]}
EOF
}

write_ticket_file() {
    # $1 = dir (Pending|deliver), $2 = ticket_id, $3 = ac_block content
    cat > "$SCRATCH/plans/cso_briefing/project/$1/${2}_Test.md" << EOF
# $2

## Acceptance Criteria
$3

## Other Section
- [ ] unrelated trailing checkbox
EOF
}

write_active_json() {
    # $1 = ticket_id, $2 = owner_session_id (unset -> sid-A, matching PAYLOAD_SID; empty string -> empty owner)
    echo "{\"ticket_id\": \"$1\", \"owner_session_id\": \"${2-sid-A}\"}" > "$SCRATCH/tmp/verify/active.json"
}

write_conf() {
    echo "TICKET_ID=$1" > "$SCRATCH/tmp/verify_cmd.conf"
}

run() {
    # $1 = payload json
    (cd "$SCRATCH" && printf '%s' "$1" | python "$HOOK")
}

assert_empty() {
    local name="$1" out="$2"
    if [[ -z "$out" ]]; then
        echo "PASS: $name (fail-open)"
    else
        echo "FAIL: $name (expected fail-open, got: $out)"
        fail_count=$((fail_count+1))
    fi
}

assert_block() {
    local name="$1" out="$2"
    if echo "$out" | grep -q '"decision": "block"'; then
        echo "PASS: $name (blocked)"
    else
        echo "FAIL: $name (expected block, got: $out)"
        fail_count=$((fail_count+1))
    fi
}

PAYLOAD_SID='{"session_id":"sid-A"}'

# ① fail-open 六類
setup
assert_empty "no TICKET_ID anywhere" "$(run "$PAYLOAD_SID")"

setup
write_active_json "GHOST-01"
write_registry "OTHER-01" "PENDING"
assert_empty "ticket not in registry" "$(run "$PAYLOAD_SID")"

setup
write_active_json "GHOST-01"
echo "{not valid json" > "$SCRATCH/plans/cso_briefing/project/TICKET_REGISTRY.json"
assert_empty "registry JSON parse failure" "$(run "$PAYLOAD_SID")"

setup
write_active_json "DUP-01"
write_registry "DUP-01" "PENDING"
write_ticket_file "deliver" "DUP-01" "- [x] a"
write_ticket_file "deliver" "DUP-01-EXTRA" "- [x] a"
mv "$SCRATCH/plans/cso_briefing/project/deliver/DUP-01-EXTRA_Test.md" "$SCRATCH/plans/cso_briefing/project/deliver/DUP-01_Test2.md"
assert_empty "ticket file multi match" "$(run "$PAYLOAD_SID")"

setup
write_active_json "NOFILE-01"
write_registry "NOFILE-01" "PENDING"
assert_empty "ticket file zero match" "$(run "$PAYLOAD_SID")"

setup
assert_empty "missing session_id" "$(run '{}')"

setup
write_active_json "NOAC-01"
write_registry "NOAC-01" "PENDING"
mkdir -p "$SCRATCH/plans/cso_briefing/project/deliver"
echo "# NOAC-01 no AC header at all" > "$SCRATCH/plans/cso_briefing/project/deliver/NOAC-01_Test.md"
assert_empty "AC header missing -> fail-open" "$(run "$PAYLOAD_SID")"

# ② 觸發情境：跡象成立 + lifecycle=PENDING -> block（AI-0 動作C：registry_set_status 可用 -> fail-closed）
setup
write_active_json "TRIG-01"
write_registry "TRIG-01" "PENDING"
write_ticket_file "deliver" "TRIG-01" "- [x] a
- [x] b"
assert_block "trigger: deliver + AC all checked + PENDING" "$(run "$PAYLOAD_SID")"

# ③ 非觸發：lifecycle in {REVIEW, CLOSED, VOID} -> fail-open
setup
write_active_json "OK-01"
write_registry "OK-01" "REVIEW"
write_ticket_file "deliver" "OK-01" "- [x] a"
assert_empty "non-trigger: lifecycle=REVIEW" "$(run "$PAYLOAD_SID")"

# ④ 同 session 同票第二次必放行
setup
write_active_json "ONCE-01"
write_registry "ONCE-01" "PENDING"
write_ticket_file "deliver" "ONCE-01" "- [x] a"
first="$(run "$PAYLOAD_SID")"
second="$(run "$PAYLOAD_SID")"
assert_block "same session first stop" "$first"
assert_empty "same session second stop" "$second"

# ⑤ 跨 session／跨 hook 隔離（ADV-OWNER-01）：僅 owner 受控；非 owner 放行且不碰任何 marker；owner 觸發 block 不刪他方 marker
setup
write_active_json "XSESS-01"
write_registry "XSESS-01" "PENDING"
write_ticket_file "deliver" "XSESS-01" "- [x] a"
echo '{"count":3}' > "$SCRATCH/tmp/.stop_guard/othersession_p1.seq"
: > "$SCRATCH/tmp/.stop_guard/othersession_XSESS-01.blocked"
: > "$SCRATCH/tmp/.stop_guard/sid-A_oldprompt.check"
before_list="$(ls -1 "$SCRATCH/tmp/.stop_guard" | sort)"
out_sid_b="$(run '{"session_id":"sid-B"}')"
assert_empty "non-owner sid-B same ticket/cwd -> allowed" "$out_sid_b"
after_list="$(ls -1 "$SCRATCH/tmp/.stop_guard" | sort)"
if [[ "$before_list" == "$after_list" ]]; then
    echo "PASS: non-owner left tmp/.stop_guard untouched"
else
    echo "FAIL: non-owner changed tmp/.stop_guard"
    fail_count=$((fail_count+1))
fi
out_sid_a="$(run '{"session_id":"sid-A"}')"
assert_block "owner sid-A first block" "$out_sid_a"
for keep in othersession_p1.seq othersession_XSESS-01.blocked sid-A_oldprompt.check; do
    if [[ -f "$SCRATCH/tmp/.stop_guard/$keep" ]]; then
        echo "PASS: marker $keep preserved after owner block"
    else
        echo "FAIL: marker $keep deleted"
        fail_count=$((fail_count+1))
    fi
done
if [[ "$(cat "$SCRATCH/tmp/.stop_guard/othersession_p1.seq")" == '{"count":3}' ]]; then
    echo "PASS: other-session .seq content unchanged"
else
    echo "FAIL: other-session .seq content changed"
    fail_count=$((fail_count+1))
fi

# ⑤b 缺 owner／空 owner／缺 key／僅 conf／payload 無 session_id → 放行且 tmp/.stop_guard 與 active.json 不變
for variant in missing empty confonly nosid; do
    setup
    write_registry "OWN-01" "PENDING"
    write_ticket_file "deliver" "OWN-01" "- [x] a"
    : > "$SCRATCH/tmp/.stop_guard/othersession_keep.seq"
    case "$variant" in
        missing) echo '{"ticket_id": "OWN-01"}' > "$SCRATCH/tmp/verify/active.json" ;;
        empty) write_active_json "OWN-01" "" ;;
        confonly) write_conf "OWN-01" ;;
        nosid) write_active_json "OWN-01" ;;
    esac
    snap_before="$(ls -1 "$SCRATCH/tmp/.stop_guard" | sort; cat "$SCRATCH/tmp/verify/active.json" 2>/dev/null)"
    if [[ "$variant" == "nosid" ]]; then out="$(run '{}')"; else out="$(run "$PAYLOAD_SID")"; fi
    assert_empty "owner variant $variant -> fail-open" "$out"
    snap_after="$(ls -1 "$SCRATCH/tmp/.stop_guard" | sort; cat "$SCRATCH/tmp/verify/active.json" 2>/dev/null)"
    if [[ "$snap_before" == "$snap_after" ]]; then
        echo "PASS: owner variant $variant left marker dir and active.json unchanged"
    else
        echo "FAIL: owner variant $variant changed marker dir or active.json"
        fail_count=$((fail_count+1))
    fi
done

# ⑥ AC 區塊界定回歸：AC 區塊外含未勾 checkbox 不影響判定（版型固定含 trailing unchecked in Other Section）
setup
write_active_json "SCOPE-01"
write_registry "SCOPE-01" "PENDING"
write_ticket_file "deliver" "SCOPE-01" "- [x] a
- [x] b"
assert_block "AC-scope: outer unchecked ignored, AC block fully checked -> still trigger" "$(run "$PAYLOAD_SID")"

# ⑦ 票檔定位邊界：<TICKET_ID>-FIX-A 不被誤配
setup
write_active_json "BASE-01"
write_registry "BASE-01" "PENDING"
write_ticket_file "deliver" "BASE-01-FIX-A" "- [x] a"
assert_empty "prefix boundary: BASE-01 not matched by BASE-01-FIX-A file" "$(run "$PAYLOAD_SID")"

# ⑧ active.json 優先於 verify_cmd.conf
setup
write_conf "CONF-ONLY"
write_active_json "ACTIVE-01"
write_registry "ACTIVE-01" "PENDING"
write_ticket_file "deliver" "ACTIVE-01" "- [x] a"
assert_block "active.json (owner) takes precedence over verify_cmd.conf" "$(run "$PAYLOAD_SID")"

setup
write_conf "CONF-01"
write_registry "CONF-01" "PENDING"
write_ticket_file "deliver" "CONF-01" "- [x] a"
assert_empty "verify_cmd.conf without owner is not an ownership fallback -> fail-open" "$(run "$PAYLOAD_SID")"

rm -rf "$SCRATCH"

if [[ "$fail_count" -eq 0 ]]; then
    echo "ALL PASS"
    exit 0
else
    echo "${fail_count} case(s) FAILED"
    exit 1
fi
