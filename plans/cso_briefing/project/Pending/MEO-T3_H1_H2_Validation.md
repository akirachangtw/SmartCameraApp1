# MEO-T3: H1/H2 驗證 — 歷史失敗案例 corpus 建置與 retrospective classification(T4 前置 gate)

**簽發日期**: 2026-07-20
**簽發者**: Claude
**Mode**: normal  ||  **Effort**: Medium
**Traycer Workflow**: N/A
**前置依賴**: MEO-T1

## Verify Config
TICKET_ID=MEO-T3
CREATED=2026-07-20
PRESET=feature
ALLOWED=.claude/skills/model-effort-orchestrator/examples/h1-corpus.md,.claude/skills/model-effort-orchestrator/examples/h2-baseline.md,.claude/skills/model-effort-orchestrator/examples/h1-report.md
FORBIDDEN=.claude/skills/model-effort-orchestrator/decision-tree.yaml,.claude/skills/model-effort-orchestrator/SKILL.md,STATE.md
MAX_LINES=400
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>

## Action Items

### AI-1: corpus 收集
- **檔案**: `examples/h1-corpus.md`
- **現值**: 不存在
- **目標值**: `search_decisions` 撈取歷史失敗案例(關鍵詞:timeout / retry / escalation / review reject / FAIL)10–15 例;每例含任務描述、失敗表現、事後人工判定(context/execution/capability);三類各至少 2 例
- [ ] 完成

### AI-2: retrospective classification
- **檔案**: `examples/h1-report.md`
- **現值**: 不存在
- **目標值**: 逐例以 decision-tree.yaml R1–R7 字面規則判定,列 skill 判定 vs 人工判定對照表 + 一致率計算
- [ ] 完成

### AI-3: H2 基線量測
- **檔案**: `examples/h2-baseline.md`
- **現值**: 不存在
- **目標值**: 回溯近 4 週升 tier 次數/週(自 escalation 相關決策檔 / session 紀錄),記錄計算依據
- [ ] 完成

## Hard Constraints
1. 分類嚴格依 decision-tree.yaml 字面規則,禁止語義合理化邊界案例(閾值不得語義化原則)
2. 本票對生產檔案唯讀;僅得寫 ALLOWED 三檔
3. 執行者為主 session(需 search_decisions),非 Claude Code

## Error Handling
- vault 案例不足 10 例 → 以實際數量產出並標注統計效力限制,停止回報由 CSO 裁決放行與否
- H1 < 80% → 產出不一致案例分析 + decision-tree.yaml 修訂提案,停止回報;**MEO-T4 維持 blocked**
- 兩輪修訂後 H1 仍 < 80% → 升級為架構議題,另開 session

## Acceptance Criteria
- [ ] **[存在性]** 三檔案存在;corpus ≥10 例且三類 failure mode 各 ≥2 例
- [ ] **[行為性]** h1-report 逐例對照可稽核,每例引用 vault 檔名(producer 證據欄:決策檔 filename,執行後回填)
- [ ] **[行為性]** H1 一致率 ≥ 80%(D2)或已產出修訂提案
- [ ] **[存在性]** h2-baseline 含計算依據與數值
- [ ] `assembleDebug` PASS
- [ ] `git diff --stat` 僅涉及 ALLOWED 檔案
