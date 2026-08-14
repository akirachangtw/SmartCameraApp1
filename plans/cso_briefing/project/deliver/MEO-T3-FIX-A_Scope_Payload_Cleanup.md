# MEO-T3-FIX-A: 清除 MEO-T3 越界 PROGRESS payload 並重建可重審範圍

**檔案治理／來源說明**：依第一次審查之 `Approach Drift` 裁決、母票 `deliver/MEO-T3_Deferred_Debt_Signal_Governance.md` 與 live Git 證據起草。`89f41af` 越界納入 `.claude/PROGRESS.md`、`.claude/PROGRESS_ARCHIVE.md`；`06cacb9` 為後續 PROGRESS 同步。採 Path A，以新 cleanup commit 抵銷越界 hunk，不追認「Pipeline Artifact」例外、不改寫既有歷史。

**簽發日期**: 2026-07-22
**簽發者**: GPT（實作裁決官）
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: execute_review
**前置依賴**: MEO-T3（`89f41af`）+ 後續同步 commit `06cacb9`

## Verify Config

TICKET_ID=MEO-T3-FIX-A
CREATED=2026-07-22
PRESET=bugfix
ALLOWED=.claude/PROGRESS.md,.claude/PROGRESS_ARCHIVE.md,tmp/verify_cmd.conf
FORBIDDEN=.claude/skills/model-effort-orchestrator/hooks/meo_signal_update.sh,.claude/skills/model-effort-orchestrator/decision-tree.yaml,.gitignore,STATE.md,.claude/skills/sv/scripts/cross_review.sh,plans/cso_briefing/project/deliver/MEO-T3_Deferred_Debt_Signal_Governance.md
MAX_LINES=80
DIFF_REF=06cacb9

## Action Items

### AI-1: 鎖定 cleanup 起點與 hunk provenance
- **現值**: `HEAD=06cacb9`；`89f41af` 同時含 MEO-T3 核心四檔與兩個未授權 PROGRESS 檔，`06cacb9` 再修改 `.claude/PROGRESS.md`
- **目標值**: 開工先確認 `HEAD=06cacb9`，並以 `git diff b79538a..06cacb9 -- .claude/PROGRESS.md .claude/PROGRESS_ARCHIVE.md` 建立唯一 rollback 集；不得把其他 commit 或 working-tree hunk 納入
- [x] 完成

### AI-2: 精準抵銷越界 PROGRESS hunk
- **檔案**: `.claude/PROGRESS.md`, `.claude/PROGRESS_ARCHIVE.md`
- **現值**: `b79538a..06cacb9` 對兩檔存在 MEO-T3 closure、Schedule、QA/SV 結果與歸檔輪替變更
- **目標值**: 僅反向抵銷 `89f41af` + `06cacb9` 對兩檔造成的淨變更；cleanup commit 後 `git diff --exit-code b79538a..HEAD --` 對兩檔回傳 0
- [x] 完成

### AI-3: 建立雙範圍 scope receipt
- **現值**: `sv_report.md` 把兩個越界檔自行定義為 `Pipeline Artifact` 並宣告 Scope PASS
- **目標值**: 記錄兩組機械證據：(a) `06cacb9..HEAD` 僅含 ALLOWED 兩個 tracked 文件；(b) `b79538a..HEAD` 僅含母票原授權的四個 tracked payload；任何驗證報告不得再以 artifact 類別擴張 ALLOWED
- [x] 完成

### AI-4: 重新送審母票 payload
- **現值**: 第一次審查因 scope 與驗證口徑失真裁定 `Approach Drift`
- **目標值**: 以 `b79538a..<cleanup_commit>` 重新執行 MEO-T3 execute_review；回件逐字列出實際 diff，未取得 reviewer PASS 前不得恢復 MEO-T3 `Well Implemented/Closed` 治理結論
- [x] 完成

## Hard Constraints
1. 禁止 `commit --amend`、interactive rebase、reset、force-push 或重寫 `89f41af`／`06cacb9` 歷史；只允許新增一個 cleanup commit
2. 禁止整檔 checkout/restore 後覆蓋他票內容；只能抵銷 AI-1 已封證的 hunk
3. 禁止修改母票四個核心 payload；本票只修 scope custody，不重做或改寫功能實作
4. 禁止手改 `sv_report.md` 來增加 `Pipeline Artifact`、implicit allowlist 或任何票面不存在的豁免
5. cleanup commit 訊息 MUST 使用 `[MEO-T3-FIX-A]`；不得混入 PROGRESS 重新同步、票檔搬移或其他 housekeeping
6. MEO-T3 狀態若重審 PASS 後需回填 PROGRESS，MUST 另開 `MEO-T3-DOCSYNC-01`；不得在本票重加已切出的內容
7. `Do NOT refactor outside this scope. Do NOT rename variables in unrelated files. Do NOT add TODO() or placeholder implementations. Once completed and successfully built, STOP and wait for Quality Gate verification.`

## Error Handling
- `HEAD != 06cacb9` → 停止回報 Anchor Drift；列出新增 commit 與兩個 PROGRESS 檔的重疊 hunk，禁自行重錨
- 兩檔存在非 `89f41af`／`06cacb9` 所有之 working-tree 或 staged hunk → 停止回報 owner/provenance，禁整檔回退
- cleanup 後 `b79538a..HEAD` 仍含兩個 PROGRESS 檔，或缺少母票任一核心檔 → 停止回報，禁送審
- reviewer 要求追認 Pipeline Artifact 例外或修改治理規則 → 停止並回 CSO 裁決；不得在 FIX-A 內採 Path B

## Acceptance Criteria
- [x] **[存在性]** `git diff --name-only 06cacb9..HEAD` 精確為 `.claude/PROGRESS.md`、`.claude/PROGRESS_ARCHIVE.md`
- [x] **[行為性]** `git diff --exit-code b79538a..HEAD -- .claude/PROGRESS.md .claude/PROGRESS_ARCHIVE.md` exit 0
- [x] **[行為性]** `git diff --name-status b79538a..HEAD` 精確保留母票四個 tracked payload：兩個 MEO 實作檔、`.gitignore`、`STATE.md` 刪除
- [x] **[存在性]** cleanup commit 為單一新 commit，commit 訊息與檔案清單符合 Hard Constraints 1/5
- [x] **[行為性]** MEO-T3 以 `b79538a..<cleanup_commit>` 重審，回件不含未授權 artifact 豁免且取得 PASS
- [x] **[行為性]** `assembleDebug` PASS
- [x] **[存在性]** `git diff --stat 06cacb9..HEAD` 僅涉及 ALLOWED 檔案

## Closure Evidence
- cleanup commit: `25b77f3`（[MEO-T3-FIX-A] 抵銷 89f41af/06cacb9 越界 PROGRESS payload）
- QA sync commit: `2c883cf`（/qa Step 4.5 Auto Commit，標準流程，非 cleanup commit 混入）
- Codex Cross-Review R1 **PASS**（REACHABILITY: NONE，Scope `06cacb9..2c883cf`）
- `structural_verify.sh --post` Verdict: ADVISORY（F.2 PROGRESS.md 79 行 > 60，非阻塞）
- `assembleDebug` PASS(15s) + `testDeviceDebugUnitTest` 209/209
- sv_report.md 全文見 `tmp/sv_report_20260722_140802.md`
