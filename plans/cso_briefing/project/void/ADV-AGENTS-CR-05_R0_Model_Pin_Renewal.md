# ADV-AGENTS-CR-05: cross-reviewer R0 subagent model pin 翻新（Opus 4.8 → Opus 5） **(原Ticket_ID: INFRA-AGENTS-CR-05更名以符合議題)**

**簽發日期**: 2026-07-28
**簽發者**: Claude
**來源議題**: Advisor
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴**: 無（ADV-AGENTS-CR-01 v1.1 已部署為既成前提）

> 檔案治理/來源說明：ADV-AGENTS-CR-01 v1.1（2026-07-14 收案）AI-3 將 R0 subagent
> model pin 定為 `claude-opus-4-8`。模型分流協議 v2（2026-07-25 修訂）已裁定 Opus 5
> 繼任並取代 Opus 4.8，該 pin 成為殭屍條款。現值經 2026-07-28 grep 實測：
> `.claude/agents/cross-reviewer.md:5` = `model: claude-opus-4-8`。
> ID 編號取證：CR-01/CR-02/CR-04 均在 archive、CR-03 在 Pending（2026-07-28 雙目錄
> 普查），故本票編 CR-05。原草稿曾誤編 CR-02、CR-04 各一次，均為未普查即推定之
> 前提失真，已入 KAIROS「指令前提活性覆核律」卷宗。

## Verify Config
TICKET_ID=ADV-AGENTS-CR-05
CREATED=2026-07-28
PRESET=hotfix
ALLOWED=.claude/agents/cross-reviewer.md
FORBIDDEN=.claude/rules/cross-review.md,.claude/CLAUDE.md,.claude/settings.json,.claude/PROGRESS.md
MAX_LINES=10
DIFF_REF=<執行前 git rev-parse --short HEAD 回填>

## Action Items

### AI-1: 修訂波及面全檔清點（先行，非可選）
- **檔案**: `.claude/agents/`、`.claude/rules/` 全域（唯讀清點）
- **現值**: 未知——MUST 以 `grep -rE 'opus-4-8|claude-opus'` 清點**所有**實例
- **目標值**: ALLOWED 以外若有任何命中 → 逐一列出並**停止回報**（不在 ALLOWED，禁寫入，改 defer 至相容票）；僅 `.claude/agents/cross-reviewer.md` 命中 → 續行 AI-2
- [ ] 完成

### AI-2: model pin 翻新
- **檔案**: `.claude/agents/cross-reviewer.md`
- **現值**: L5 `model: claude-opus-4-8`
- **目標值**: `model: claude-opus-5`
- [ ] 完成

## Hard Constraints
1. `tools:` 欄位維持 `Read, Grep, Glob`，禁增 Write / Edit / Bash（承繼 CR-01 HC1）
2. `effort: high` 不得變更
3. 全文禁出現裁決字彙 `PASS`（承繼 CR-01 HC3，grep 機械驗證）

## Error Handling
- `CLAUDE_CODE_SUBAGENT_MODEL` 環境變數非空 → 停止回報（優先序高於 frontmatter，pin 失效）
- `claude-opus-5` 於本環境非有效 model 識別字（委派即報錯）→ 停止回報並原樣回傳錯誤字串，**禁自行猜測替代字串**
- AI-1 於 ALLOWED 外命中 → 停止回報

## Acceptance Criteria
- [ ] **[存在性]** grep 命中 `model: claude-opus-5`，且 `claude-opus-4-8` 於該檔零命中
- [ ] **[存在性]** grep 確認 `tools: Read, Grep, Glob`、`effort: high` 未變，`PASS` 零命中
- [ ] **[行為性]** 新 session 委派 `@cross-reviewer` 任一歷史 payload，回傳含 FORWARD / FAIL / CHECKPOINT-REQUIRED 裁決與分級表（證明 pin 生效且 agent 可用）
- [ ] `assembleDebug` PASS
- [ ] `git diff --stat` 僅涉及 ALLOWED 檔案
