# MEO-T4: Effort adapter 落地(Claude Code + Codex 兩欄具體化)與 escalation_history 建檔

**簽發日期**: 2026-07-20
**簽發者**: Claude
**Mode**: normal → uc  ||  **Effort**: High
**Traycer Workflow**: N/A
**前置依賴**: MEO-T2, MEO-T3(**硬性 gate:H1 ≥ 80% 通過方可開工**)

## Verify Config
TICKET_ID=MEO-T4
CREATED=2026-07-20
PRESET=feature
ALLOWED=.claude/skills/model-effort-orchestrator/SKILL.md,.claude/skills/model-effort-orchestrator/examples/coding-task.md,.claude/skills/model-effort-orchestrator/examples/review-task.md,.claude/skills/model-effort-orchestrator/examples/timeout-task.md,.claude/skills/model-effort-orchestrator/meo_escalation_history.yaml
FORBIDDEN=.claude/skills/model-effort-orchestrator/decision-tree.yaml,STATE.md,gate.conf,.claude/settings.json
MAX_LINES=300
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>

## Action Items

### AI-1: SKILL.md §8.2 adapter 兩欄具體化
- **檔案**: `SKILL.md`
- **現值**: Claude Code / Codex 欄為抽象描述
- **目標值**: 各 effort 級對應可執行配置(Claude Code:plan mode 觸發條件、讀檔範圍指令、測試強制;Codex:self-review 輪數、cross-validation 開關);「其他平台」欄維持留空
- [ ] 完成

### AI-2: 三個 example 撰寫
- **檔案**: `examples/coding-task.md`、`review-task.md`、`timeout-task.md`
- **現值**: 不存在
- **目標值**: 各例含輸入任務、預期分流路徑(R2 fast-path vs R5/R6 full)、adapter 配置輸出;timeout 例僅引用 `MCP_Search_Pipeline_Timeout_Hardening` 決策檔,禁重述規則
- [ ] 完成

### AI-3: escalation_history 建檔
- **檔案**: `meo_escalation_history.yaml`
- **現值**: 不存在
- **目標值**: 依 decision-tree.yaml `escalation_history_schema` 建空檔 + 一筆測試紀錄(含 `model_version_at_decision`、`ttl_days: 30`、invalidation 條件)
- [ ] 完成

## Hard Constraints
1. adapter 參數在 runtime 實際不存在(平台版本差異)→ 降級為最近似配置並於 §8.2 註記,同時列 KAIROS 教訓候選
2. decision-tree.yaml 唯讀;adapter 發現的邏輯問題回 CSO,禁自行改 SSOT
3. SKILL.md 修改限 §8.2 表格,採唯一錨點 patch

## Error Handling
- MEO-T3 H1 未通過或報告缺失 → 拒絕開工回報 blocked
- effort 參數映射無法在 runtime 驗證 → 該格標注 UNVERIFIED,停止回報
- example 分流結果與 decision-tree 預期不符 → 停止回報(此為 H1 漏網信號)

## Acceptance Criteria
- [ ] **[存在性]** §8.2 兩欄具體化;三 example + escalation_history 檔存在
- [ ] **[可達性]** 每個 effort 級之 Claude Code 配置在當前 runtime 實際可設定(producer 證據欄:配置指令 + 生效證據,執行後回填)
- [ ] **[行為性]** 三 example 各跑一次真實分流,結果與 decision-tree.yaml 預期一致(禁紙上推演充當)
- [ ] **[存在性]** escalation_history 測試紀錄含 TTL 30 與 invalidation 欄位
- [ ] `assembleDebug` PASS
- [ ] `git diff --stat` 僅涉及 ALLOWED 檔案
