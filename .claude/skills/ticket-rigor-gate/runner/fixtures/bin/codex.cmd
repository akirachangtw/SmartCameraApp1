#!/usr/bin/env bash
# no-call fixture stub：被呼叫即代表 blocker_preflight 未攔下（fixture FAIL 訊號）。
# 觸發時寫入 sentinel（路徑由 CODEX_STUB_SENTINEL 指定），永遠非零退出。
set -uo pipefail
: "${CODEX_STUB_SENTINEL:?CODEX_STUB_SENTINEL 未設定}"
echo "STUB_INVOKED args=$*" >> "$CODEX_STUB_SENTINEL"
exit 9
