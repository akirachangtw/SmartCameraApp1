# MEO-T2-FIX-A: stop_orchestrator.sh quota-exhausted 分支補齊三鍵 Stop hook schema

**簽發日期**: 2026-07-21
**簽發者**: Traycer (execute_review 裁決轉修補票)
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: execute_review
**前置依賴**: 無（承接 MEO-T2 execute_review ⛔ Invariant Violation 發現）

**問題背景**：`stop_orchestrator.sh` 的 `MEO_QUOTA_STATE=exhausted` 分支只輸出 `decision`/`reason` 兩鍵，與 `escalation review required` 分支（三鍵：`decision`/`reason`/`systemMessage`）不一致，違反 `plans/cso_briefing/project/deliver/MEO-T2_Hook_Enforcement_Gate.md` Hard Constraint 2「Stop 類僅三鍵」，也與 `.claude/skills/model-effort-orchestrator/decision-tree.yaml` 記載之 `Stop hook stdout schema {decision, reason, systemMessage}` 契約不符。

## Verify Config

```
TICKET_ID=MEO-T2-FIX-A
CREATED=2026-07-21
PRESET=bugfix
ALLOWED=stop_orchestrator.sh
FORBIDDEN=.claude/skills/model-effort-orchestrator/hooks/meo_pretooluse_gate.sh,.claude/skills/model-effort-orchestrator/hooks/meo_signal_update.sh,.claude/skills/model-effort-orchestrator/hooks/meo_session_reset.sh,.claude/settings.json,gate.conf,STATE.md,.claude/skills/sv/scripts/cross_review.sh
MAX_LINES=20
DIFF_REF=94465d9
```

## Action Items

### AI-1: quota exhausted 分支補上 `systemMessage` 鍵

- 檔案：`stop_orchestrator.sh`
- 現值：`printf '{"decision":"block","reason":"MEO: defer_and_checkpoint (quota exhausted)"}'`
- 目標值：與另一分支同構的三鍵輸出，新增 `systemMessage` 欄位，內容需清楚說明「因 quota 耗盡而 defer，需等下一配額窗口」，不得沿用 escalation 分支的三態裁決文字（語義不同，不可複製貼上）

完成

## Hard Constraints

1. 兩個分支（quota exhausted / escalation review required）輸出的 JSON key 集合必須完全一致（`decision`/`reason`/`systemMessage`），value 內容依語義各自撰寫
2. 不得新增第四個 Stop hook 掛載點，僅修改既有 `stop_orchestrator.sh` 內部邏輯
3. 不得變更 `gate.conf` 讀取邏輯或 `STATE.md` 欄位結構

## Error Handling

- 若 `stop_orchestrator.sh` 現有內容與本票「現值」不符（表示已被其他修補動作變更）→ 停止回報，不自行猜測合併
- 若發現 `.claude/settings.json` 對 Stop hook 的呼叫方式已變更（非三支 hook 陣列）→ 停止回報

## Acceptance Criteria

[存在性] stop_orchestrator.sh 兩個輸出分支（quota exhausted / escalation review required）皆含 decision、reason、systemMessage 三鍵（grep 確認）[行為性] 手動以 MEO_QUOTA_STATE=exhausted + MEO_ESCALATION_PENDING=true 組合執行腳本，確認輸出為合法 JSON 且含三鍵assembleDebug PASSgit diff --stat 僅涉及 ALLOWED 檔案（stop_orchestrator.sh）

⚠️ DEFENSIVE WARNING: Do NOT refactor outside this scope. Do NOT rename variables in unrelated files. Do NOT add TODO() or placeholder implementations. Once completed and successfully built, STOP and wait for Quality Gate verification.