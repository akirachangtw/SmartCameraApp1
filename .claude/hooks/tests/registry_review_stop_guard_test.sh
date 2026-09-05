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
    echo "{\"ticket_id\": \"$1\"}" > "$SCRATCH/tmp/verify/active.json"
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
write_conf "GHOST-01"
write_registry "OTHER-01" "PENDING"
assert_empty "ticket not in registry" "$(run "$PAYLOAD_SID")"

setup
write_conf "GHOST-01"
echo "{not valid json" > "$SCRATCH/plans/cso_briefing/project/TICKET_REGISTRY.json"
assert_empty "registry JSON parse failure" "$(run "$PAYLOAD_SID")"

setup
write_conf "DUP-01"
write_registry "DUP-01" "PENDING"
write_ticket_file "deliver" "DUP-01" "- [x] a"
write_ticket_file "deliver" "DUP-01-EXTRA" "- [x] a"
mv "$SCRATCH/plans/cso_briefing/project/deliver/DUP-01-EXTRA_Test.md" "$SCRATCH/plans/cso_briefing/project/deliver/DUP-01_Test2.md"
assert_empty "ticket file multi match" "$(run "$PAYLOAD_SID")"

setup
write_conf "NOFILE-01"
write_registry "NOFILE-01" "PENDING"
assert_empty "ticket file zero match" "$(run "$PAYLOAD_SID")"

setup
assert_empty "missing session_id" "$(run '{}')"

setup
write_conf "NOAC-01"
write_registry "NOAC-01" "PENDING"
mkdir -p "$SCRATCH/plans/cso_briefing/project/deliver"
echo "# NOAC-01 no AC header at all" > "$SCRATCH/plans/cso_briefing/project/deliver/NOAC-01_Test.md"
assert_empty "AC header missing -> fail-open" "$(run "$PAYLOAD_SID")"

# ② 觸發情境：跡象成立 + lifecycle=PENDING -> block（AI-0 動作C：registry_set_status 可用 -> fail-closed）
setup
write_conf "TRIG-01"
write_registry "TRIG-01" "PENDING"
write_ticket_file "deliver" "TRIG-01" "- [x] a
- [x] b"
assert_block "trigger: deliver + AC all checked + PENDING" "$(run "$PAYLOAD_SID")"

# ③ 非觸發：lifecycle in {REVIEW, CLOSED, VOID} -> fail-open
setup
write_conf "OK-01"
write_registry "OK-01" "REVIEW"
write_ticket_file "deliver" "OK-01" "- [x] a"
assert_empty "non-trigger: lifecycle=REVIEW" "$(run "$PAYLOAD_SID")"

# ④ 同 session 同票第二次必放行
setup
write_conf "ONCE-01"
write_registry "ONCE-01" "PENDING"
write_ticket_file "deliver" "ONCE-01" "- [x] a"
first="$(run "$PAYLOAD_SID")"
second="$(run "$PAYLOAD_SID")"
assert_block "same session first stop" "$first"
assert_empty "same session second stop" "$second"

# ⑤ 跨 session：不同 session_id 各自一次額度 + 舊 session 標記檔被淘汰
setup
write_conf "XSESS-01"
write_registry "XSESS-01" "PENDING"
write_ticket_file "deliver" "XSESS-01" "- [x] a"
out_sid_a="$(run '{"session_id":"sid-A"}')"
assert_block "cross-session sid-A first block" "$out_sid_a"
out_sid_b="$(run '{"session_id":"sid-B"}')"
assert_block "cross-session sid-B first block (new session own quota)" "$out_sid_b"
remaining=$(ls "$SCRATCH/tmp/.stop_guard" | grep -c '^sid-A_' || true)
if [[ "$remaining" -eq 0 ]]; then
    echo "PASS: stale sid-A marker evicted after sid-B write"
else
    echo "FAIL: stale sid-A marker NOT evicted"
    fail_count=$((fail_count+1))
fi

# ⑥ AC 區塊界定回歸：AC 區塊外含未勾 checkbox 不影響判定（版型固定含 trailing unchecked in Other Section）
setup
write_conf "SCOPE-01"
write_registry "SCOPE-01" "PENDING"
write_ticket_file "deliver" "SCOPE-01" "- [x] a
- [x] b"
assert_block "AC-scope: outer unchecked ignored, AC block fully checked -> still trigger" "$(run "$PAYLOAD_SID")"

# ⑦ 票檔定位邊界：<TICKET_ID>-FIX-A 不被誤配
setup
write_conf "BASE-01"
write_registry "BASE-01" "PENDING"
write_ticket_file "deliver" "BASE-01-FIX-A" "- [x] a"
assert_empty "prefix boundary: BASE-01 not matched by BASE-01-FIX-A file" "$(run "$PAYLOAD_SID")"

# ⑧ active.json 優先於 verify_cmd.conf
setup
write_conf "CONF-ONLY"
write_active_json "ACTIVE-01"
write_registry "ACTIVE-01" "PENDING"
write_ticket_file "deliver" "ACTIVE-01" "- [x] a"
assert_block "active.json takes precedence over verify_cmd.conf" "$(run "$PAYLOAD_SID")"

setup
write_conf "CONF-01"
write_registry "CONF-01" "PENDING"
write_ticket_file "deliver" "CONF-01" "- [x] a"
assert_block "verify_cmd.conf fallback when active.json absent" "$(run "$PAYLOAD_SID")"

rm -rf "$SCRATCH"

if [[ "$fail_count" -eq 0 ]]; then
    echo "ALL PASS"
    exit 0
else
    echo "${fail_count} case(s) FAILED"
    exit 1
fi
