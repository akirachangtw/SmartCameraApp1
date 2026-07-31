# MEO 部署 Payload — 腳本附錄與驗收矩陣
Version: v1.2
Date: 2026-07-21(v1.2 修訂:R8–R10 PreToolUse bootstrap 死鎖裁決)
用途: MEO-T1~T4 開票之源內容。腳本原樣部署,禁止即興改寫;變更須回 CSO。
Governance: vault `wiki\decisions\CSO\2026-07-20_MEO_Skill_Governance_Ratification.md`(D1–D5)

## v1.2 Changelog(CSO 裁決 R8–R10)
- **R8 Bootstrap 解鎖**:PreToolUse gate 自我鎖死(診斷 TS 僅能經被攔截工具寫入);CSO 經 MCP 側寫入 `MEO_LAST_DIAGNOSIS_TS=bootstrap-20260721-CSO-R8` 解鎖(MCP 寫入不經 hook 體系)
- **R9 A1 → v1.2**:新增 STATE.md 診斷自舉豁免——Edit/Write/MultiEdit 之 `tool_input.file_path` 以 STATE.md 結尾即放行;**Bash 不豁免**(無可靠目標偵測),診斷寫入限用 Edit
- **R10 新增 A8**:SessionStart 重置腳本,每 session 歸零 DIAGNOSIS_TS/FASTPATH——消除「一次寫入永久放行」腐化,與 R9 構成完整循環:SessionStart 重置 → 首次管制呼叫被攔 → agent 以 Edit 寫診斷(豁免)→ 放行

## v1.1 Changelog(CSO 裁決 R1–R7)
- A2 重寫:單腳本雙事件(PostToolUse + PostToolUseFailure),成敗判別依 `hook_event_name`,**不依 isError 類欄位**(官方 schema 未文件化,issue #19372);error class 採 fallback 鏈
- A4 增列 PostToolUseFailure 註冊區塊(同一腳本雙掛;不違反 G2/D5——該條款禁的是第四個 **Stop** hook,lifecycle 事件不在此限)
- 部署路徑遷移:`skills\` → `.claude\skills\`
- 新增約束:/sv 排除清單不得豁免 `.claude/skills/model-effort-orchestrator/hooks/`(INFRA-001 教訓)
- streak 語意明文:成功僅重置 RETRY_COUNT,**不重置 STREAK**(交錯型硬磨偵測必要)
- 已知風險:issue #27886(PostToolUseFailure 可能不執行)→ V5 為偵測點,實測不觸發即停止回報並降級(RETRY 由 /sv reject 承載)

---

## A1 — meo_pretooluse_gate.sh(PreToolUse)

```bash
#!/usr/bin/env bash
# meo_pretooluse_gate.sh — PreToolUse gate for model-effort-orchestrator
# Contract: stdout JSON {decision, reason} only; exit 0 for JSON parsing.
set -euo pipefail

STATE_FILE="${MEO_STATE_FILE:-D:/DevSources/AndroidProj/SmartCameraApp1/STATE.md}"
command -v jq >/dev/null 2>&1 || { printf '{"decision":"approve","reason":"MEO fail-open: jq missing"}'; exit 0; }
INPUT="$(cat)"

TOOL_NAME="$(printf '%s' "$INPUT" | jq -r '.tool_name // empty')"

case "$TOOL_NAME" in
  Write|Edit|MultiEdit|Bash) ;;
  *) printf '{"decision":"approve","reason":"non-gated tool"}'; exit 0 ;;
esac

# v1.2 (CSO R9): STATE.md 診斷自舉豁免 — Edit/Write/MultiEdit 目標為 STATE.md 放行
# 注意:萬用字元尾綴匹配,他處同名 STATE.md 亦豁免(MVP 已知限制);Bash 不豁免
TARGET="$(printf '%s' "$INPUT" | jq -r '.tool_input.file_path // .tool_input.path // empty')"
case "$TARGET" in
  *STATE.md)
    printf '{"decision":"approve","reason":"MEO bootstrap: diagnosis write to STATE.md exempt"}'; exit 0 ;;
esac

if grep -q 'MEO_FASTPATH=true' "$STATE_FILE" 2>/dev/null; then
  printf '{"decision":"approve","reason":"MEO fast-path active"}'; exit 0
fi

if grep -q '^MEO_LAST_DIAGNOSIS_TS=..*' "$STATE_FILE" 2>/dev/null; then
  printf '{"decision":"approve","reason":"MEO diagnosis present"}'; exit 0
fi

printf '{"decision":"block","reason":"MEO: high-risk tool call without diagnosis in STATE.md; run diagnosis first"}'
exit 0
```

## A2 — meo_signal_update.sh(v1.1:PostToolUse + PostToolUseFailure 雙事件)

```bash
#!/usr/bin/env bash
# meo_signal_update.sh — mechanical signal accumulator (dual-event, v1.1)
# Registered on BOTH PostToolUse (success-only) and PostToolUseFailure.
# Success/failure discrimination = hook_event_name ONLY (CSO R2).
# Contract: stdout empty; exit 0 always (non-blocking accumulator).
set -uo pipefail

STATE_FILE="${MEO_STATE_FILE:-D:/DevSources/AndroidProj/SmartCameraApp1/STATE.md}"
command -v jq >/dev/null 2>&1 || exit 0   # fail-open

INPUT="$(cat)"
EVENT="$(printf '%s' "$INPUT" | jq -r '.hook_event_name // empty')"
TOOL_NAME="$(printf '%s' "$INPUT" | jq -r '.tool_name // empty')"

case "$TOOL_NAME" in
  Write|Edit|MultiEdit|Bash) ;;
  *) exit 0 ;;
esac

kv_get() { grep -m1 "^$1=" "$STATE_FILE" 2>/dev/null | cut -d= -f2-; }
kv_set() {
  if grep -q "^$1=" "$STATE_FILE" 2>/dev/null; then
    sed -i "s|^$1=.*|$1=$2|" "$STATE_FILE"
  fi
}

if [ "$EVENT" = "PostToolUse" ]; then
  # 成功路徑:僅重置 RETRY_COUNT。
  # CSO R4:STREAK 不重置——交錯型硬磨(成功/同類失敗交替)為必抓模式。
  kv_set MEO_RETRY_COUNT 0
  exit 0
fi

[ "$EVENT" = "PostToolUseFailure" ] || exit 0

# --- 失敗路徑 ---
RETRY="$(kv_get MEO_RETRY_COUNT)"; RETRY="${RETRY:-0}"
kv_set MEO_RETRY_COUNT "$((RETRY + 1))"

# error class:fallback 鏈對 schema 漂移免疫(#19372 欄位未定案)
ERR_MSG="$(printf '%s' "$INPUT" | jq -r '.error // .tool_error // "unknown"' 2>/dev/null | head -c 200)"
ERR_CLASS="$(printf '%s' "$ERR_MSG" | grep -oE '[A-Za-z_]+' | head -1)"
ERR_CLASS="${ERR_CLASS:-unknown}"

LAST_CLASS="$(kv_get MEO_LAST_ERROR_CLASS)"
STREAK="$(kv_get MEO_SAME_ERROR_CLASS_STREAK)"; STREAK="${STREAK:-0}"

if [ "$ERR_CLASS" = "$LAST_CLASS" ]; then
  kv_set MEO_SAME_ERROR_CLASS_STREAK "$((STREAK + 1))"
else
  kv_set MEO_SAME_ERROR_CLASS_STREAK 1
  kv_set MEO_LAST_ERROR_CLASS "$ERR_CLASS"
fi

# D1:閾值=2,數值直取
NEW_RETRY="$(kv_get MEO_RETRY_COUNT)"
NEW_STREAK="$(kv_get MEO_SAME_ERROR_CLASS_STREAK)"
if [ "${NEW_RETRY:-0}" -ge 2 ] || [ "${NEW_STREAK:-0}" -ge 2 ]; then
  kv_set MEO_ESCALATION_PENDING true
fi
exit 0
```

> 注意:PostToolUseFailure **不涵蓋**驗證駁回與 permission 拒絕(官方文件明載,兩者不觸發此事件),此類事前拒絕不計入 RETRY——屬正確語意,毋須補償。
> `MEO_VERIFIER_REJECT_COUNT` 由 `/sv` pipeline 於 Codex 駁回時直呼 kv_set 語意累加(MEO-T2 AI-5,不經 hook,不受本次事件模型問題影響)。`MEO_QUOTA_STATE` MVP 人工或 SessionStart 設定。

## A3 — STATE.md 增補區塊(不變,僅 append)

```text
## MEO (model-effort-orchestrator)
MEO_LAST_DIAGNOSIS_TS=
MEO_LAST_TASK_TYPE=
MEO_LAST_FAILURE_MODE=
MEO_RETRY_COUNT=0
MEO_SAME_ERROR_CLASS_STREAK=0
MEO_VERIFIER_REJECT_COUNT=0
MEO_LAST_ERROR_CLASS=none
MEO_QUOTA_STATE=abundant
MEO_ESCALATION_PENDING=false
MEO_FASTPATH=false
```

## A4 — settings.json hooks 註冊(v1.1:三區塊,exec form)

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Write|Edit|MultiEdit|Bash",
        "hooks": [
          { "type": "command",
            "command": ["bash", "D:/DevSources/AndroidProj/SmartCameraApp1/.claude/skills/model-effort-orchestrator/hooks/meo_pretooluse_gate.sh"] }
        ]
      }
    ],
    "PostToolUse": [
      {
        "matcher": "Write|Edit|MultiEdit|Bash",
        "hooks": [
          { "type": "command",
            "command": ["bash", "D:/DevSources/AndroidProj/SmartCameraApp1/.claude/skills/model-effort-orchestrator/hooks/meo_signal_update.sh"] }
        ]
      }
    ],
    "PostToolUseFailure": [
      {
        "matcher": "Write|Edit|MultiEdit|Bash",
        "hooks": [
          { "type": "command",
            "command": ["bash", "D:/DevSources/AndroidProj/SmartCameraApp1/.claude/skills/model-effort-orchestrator/hooks/meo_signal_update.sh"] }
        ]
      }
    ]
  }
}
```

## A5 — gate.conf `[meo]` 條目(不變)

```ini
[meo]
enabled=true
state_file=D:/DevSources/AndroidProj/SmartCameraApp1/STATE.md
# stop_orchestrator.sh 讀取邏輯:
#   MEO_ESCALATION_PENDING=true 時:
#     MEO_QUOTA_STATE=exhausted -> decision=block, reason="MEO: defer_and_checkpoint (quota exhausted)"
#     否則 -> decision=block, reason="MEO: escalation review required"
#        systemMessage 提示三態裁決 (approve_upgrade / deny_retry_with_high_effort / defer_and_checkpoint)
#   裁決完成後由主 session 將 MEO_ESCALATION_PENDING 重置為 false
```

## A6 — T2 驗收矩陣 V1–V8(V5 判準更新)

| # | 情境 | 輸入 | 預期 |
|---|------|------|------|
| V1 | 非管制工具 | PreToolUse `tool_name=Read` | approve "non-gated tool", exit 0 |
| V2 | fast-path 放行 | `MEO_FASTPATH=true` + `tool_name=Edit` | approve, reason 含 fast-path |
| V3 | 有診斷放行 | `MEO_LAST_DIAGNOSIS_TS=` 非空 + `tool_name=Write` | approve |
| V4 | 無診斷攔截 | 兩標記均無 + `tool_name=Bash` | block + 指示先跑診斷, exit 0 |
| V5 | 信號累加 | 真實工具失敗兩次(觸發 PostToolUseFailure),同 error class | RETRY=2, STREAK=2, PENDING=true;**若 PostToolUseFailure 實測不觸發(#27886)→ 停止回報,降級裁決** |
| V6 | streak 重置與保留 | 失敗換 class → streak 歸 1;失敗→成功→同 class 失敗 → streak 續累(R4) |
| V7 | orchestrator 讀取 | `MEO_ESCALATION_PENDING=true` | stop_orchestrator 輸出 MEO gate 裁決;stdout 僅三鍵 schema |
| V8 | quota 阻斷 | `MEO_QUOTA_STATE=exhausted` + 信號達閾值 | defer_and_checkpoint,禁 upgrade |

## A7 — Rollback(路徑同步 v1.1)

| 層級 | 動作 | 影響 |
|------|------|------|
| L1 | gate.conf `[meo] enabled=false` + settings.json 移除三 hook 條目 | 完全旁路,零檔案刪除 |
| L2 | 移除 hooks 註冊,保留 SSOT 與 STATE.md 區段 | 失去 enforcement,契約可手動用 |
| L3 | 刪 `.claude\skills\model-effort-orchestrator\` + STATE.md 移除 §MEO(patch mode) | 需同步 supersede 歸檔;escalation_history 唯讀保留 |

---

## A8 — meo_session_reset.sh(SessionStart,v1.2 CSO R10)

```bash
#!/usr/bin/env bash
# meo_session_reset.sh — SessionStart: 每 session 重置診斷狀態(CSO R10)
# Contract: stdout empty; exit 0 always.
set -uo pipefail
STATE_FILE="${MEO_STATE_FILE:-D:/DevSources/AndroidProj/SmartCameraApp1/STATE.md}"
grep -q '^MEO_LAST_DIAGNOSIS_TS=' "$STATE_FILE" 2>/dev/null && sed -i 's|^MEO_LAST_DIAGNOSIS_TS=.*|MEO_LAST_DIAGNOSIS_TS=|' "$STATE_FILE"
grep -q '^MEO_FASTPATH=' "$STATE_FILE" 2>/dev/null && sed -i 's|^MEO_FASTPATH=.*|MEO_FASTPATH=false|' "$STATE_FILE"
exit 0
```

settings.json 增補區塊(併入 A4 註冊,SessionStart 無 matcher):

```json
"SessionStart": [
  { "hooks": [
      { "type": "command",
        "command": ["bash", "D:/DevSources/AndroidProj/SmartCameraApp1/.claude/skills/model-effort-orchestrator/hooks/meo_session_reset.sh"] }
  ] }
]
```

> 完整循環(R9+R10):SessionStart 重置 → 首次管制工具呼叫被 A1 攔截並提示 → agent 以 Edit 寫診斷至 STATE.md(R9 豁免放行)→ 後續管制呼叫放行。RETRY/STREAK/REJECT 計數**不隨 session 重置**(cross-session 累積為 escalation 信號設計本意)。

**End of Payload v1.2**
