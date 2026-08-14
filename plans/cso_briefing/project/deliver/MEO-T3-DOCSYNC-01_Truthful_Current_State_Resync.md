# MEO-T3-DOCSYNC-01: 恢復 MEO-T3 Dashboard 單一真相後重新送審

**檔案治理／來源說明**：依第三次審查 `Approach Drift` 裁決、`deliver/MEO-T3-FIX-A_Scope_Payload_Cleanup.md` Hard Constraint 6 與 live `HEAD=2c883cf` 起草。母票核心實作與 FIX-A scope cleanup 均不重做；本票只修復 `.claude/PROGRESS.md` 的 current-state split-brain。

**簽發日期**: 2026-07-22
**簽發者**: GPT（實作裁決官）
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴**: MEO-T3-FIX-A（`25b77f3`）+ QA sync（`2c883cf`）

## Verify Config

TICKET_ID=MEO-T3-DOCSYNC-01
CREATED=2026-07-22
PRESET=bugfix
ALLOWED=.claude/PROGRESS.md,tmp/verify_cmd.conf
FORBIDDEN=.claude/PROGRESS_ARCHIVE.md,.claude/skills/model-effort-orchestrator/hooks/meo_signal_update.sh,.claude/skills/model-effort-orchestrator/decision-tree.yaml,.gitignore,STATE.md,.claude/skills/sv/scripts/cross_review.sh,plans/cso_briefing/project/deliver/MEO-T3_Deferred_Debt_Signal_Governance.md,plans/cso_briefing/project/deliver/MEO-T3-FIX-A_Scope_Payload_Cleanup.md
MAX_LINES=30
DIFF_REF=2c883cf

## Action Items

### AI-1: 同步 Dashboard 日期
- **檔案**: `.claude/PROGRESS.md`
- **現值**: `Last Updated` 為 `2026-07-21`，但 Compile／Tests 已記錄 `2026-07-22`
- **目標值**: `Last Updated` 更新為 `2026-07-22`
- [x] 完成

### AI-2: 將 Sprint Health 改為分層真實狀態
- **檔案**: `.claude/PROGRESS.md`
- **現值**: S76 只寫 `MEO-T3-FIX-A AI-1~3 ✅ Done`，未揭露第三次審查未通過
- **目標值**: 以最新 Schedule 條目同時保留：母票核心 payload 未受破壞、`25b77f3` scope cleanup 機械完成、`2c883cf` QA sync、第三次審查為 `Approach Drift`、本票 truthful-resync 完成後待重審、`strict_verification` 仍 blocked
- [x] 完成

### AI-3: 消除 Next Actions #17 的過期前置票語氣
- **檔案**: `.claude/PROGRESS.md`
- **現值**: #17 仍稱「MEO-T3 為下一張 MEO 票開工前置票」，與 S76 已完成敘述互斥
- **目標值**: #17 改為與 AI-2 相同狀態：核心實作與 scope cleanup 已完成，但第三次治理審查未通過；DOCSYNC 完成後等待 execute_review，PASS 前禁止 MEO 後續功能票與 strict verification
- [x] 完成

### AI-4: 執行 Dashboard 自洽 read-back
- **現值**: 機械 net-zero diff 通過，但 artifact truth 未驗收
- **目標值**: 逐項讀回 Last Updated、Schedule、Next Actions #17；確認無 `MEO-T3 為下一張 MEO 票開工前置票`、無 `MEO-T3 Closed/Well Implemented`，且檔案總行數 `<=80`
- [x] 完成

## Hard Constraints

1. `25b77f3`／`2c883cf` cleanup 歷程 MUST 保留；禁止以回退 Dashboard 真相換取 net-zero diff
2. 「機械 cleanup 完成」MUST NOT 等同「第三次治理審查通過」；未取得新 PASS 前禁止寫 `Closed`、`Well Implemented` 或可進 strict verification
3. 禁止恢復 `89f41af`／`06cacb9` 的整段舊 PROGRESS hunk，禁止修改或搬移 `PROGRESS_ARCHIVE.md`
4. Compile／Tests 現有數值與日期不得改寫；本票只修 metadata 與 MEO-T3/FIX-A 狀態語意
5. `/qa` 若再產生 PROGRESS auto-sync hunk，MUST 逐 hunk 核對本票狀態模型，禁止以「Pipeline Artifact」名義自動放行矛盾文字
6. `Do NOT refactor outside this scope. Do NOT rename variables in unrelated files. Do NOT add TODO() or placeholder implementations. Once completed and successfully built, STOP and wait for Quality Gate verification.`

## Error Handling

- `HEAD != 2c883cf` → 停止回報 Anchor Drift；若新增 commit 觸碰 PROGRESS，先判 owner/provenance，禁自行重錨
- `.claude/PROGRESS.md` 有非本票所有之 working-tree／staged hunk → 停止回報，禁整檔覆寫
- Schedule 與 #17 無法同時表達同一狀態模型且維持 `<=80` 行 → 停止回報，不得刪除其他票真實狀態騰空間
- execute_review 未 PASS → 保持 blocked 並回報新 finding；不得進 strict verification

## Acceptance Criteria

- [x] **[存在性]** `Last Updated` 精確為 `2026-07-22`
- [x] **[存在性]** Schedule 同時記錄 `25b77f3`、`2c883cf`、第三次 `Approach Drift`、truthful-resync 與 `strict_verification blocked`
- [x] **[存在性]** Next Actions #17 與 Schedule 狀態一致，且不再含「MEO-T3 為下一張 MEO 票開工前置票」
- [x] **[存在性]** PROGRESS 不含未經重審支持的 `MEO-T3 Closed`／`Well Implemented`，總行數 `<=80`
- [x] **[行為性]** `assembleDebug` PASS（47s，0 error）
- [x] **[存在性]** `git diff --stat` 僅涉及 ALLOWED 檔案（CSO 裁決A重錨後改測 `8f597df..HEAD`，非票面原始 `2c883cf..HEAD`；`2c883cf` 為 `8f597df` 祖先，重錨基準已涵蓋原 diff 範圍，結果同為僅 `.claude/PROGRESS.md` 1 檔）

## Closure Note（2026-07-22）

- Cross-Review：Codex PASS（REACHABILITY: NONE），`structural_verify.sh --post` PASS/ADVISORY。
- **Traycer execute_review 豁免**：本票 Header 原載明 `Traycer Workflow: N/A`；Akira 於 2026-07-22 對話中明確授權「請授權刪除或跳過 AC7（Traycer execute_review），因為這基本上應該是從外部做審核」，確認本票收尾不依賴 Traycer 終審，比照票面 N/A 設定執行標準歸檔。
- 母票 `MEO-T3`（第三次 `Approach Drift`、`strict_verification` blocked）狀態**不受本票收尾影響**，維持 blocked，待母票另行送審。
- 全部 AI-1~4、AC 1~6 已逐項驗證 PASS，票檔搬移至 `deliver/`。
