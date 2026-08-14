# INFRA-QA-COMMIT-EMPTYDIFF-01: /qa Step 4.5 Auto Commit 真零 diff 情境明訂

**簽發日期**: 2026-07-19
**簽發者**: Claude
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: execute_review（.md skill 行為變更 = 治理票）
**前置依賴**: INFRA-SV-ZERODIFF-01（AI-2 全管線掃描發現本缺口並登記待開票；Akira 2026-07-19 對話裁決「同意 2」授權開票）

> 檔案治理/來源說明：`.claude/rules/decommission.md`「退場票型管線相容性清單」表格第 6 列已記錄本缺口：「`/qa` Step 4.5（Auto Commit）— 真零 diff（非 FORBIDDEN-hit SKIP）情境下「Commit」子步驟無 `--allow-empty` 或等效處理，`git commit` 對空 staging 會失敗，行為未定義」。此為 INFRA-SV-ZERODIFF-01 唯讀掃描（Hard Constraint 2：本票只修 `structural_verify.sh` 單檔）刻意不夾帶之承接缺口，非新發現問題。

## Verify Config
TICKET_ID=INFRA-QA-COMMIT-EMPTYDIFF-01
CREATED=2026-07-19
PRESET=refactor
ALLOWED=.claude/skills/qa/SKILL.md
FORBIDDEN=.claude/CLAUDE.md,.claude/settings.json,.claude/PROGRESS.md,.claude/skills/qa/scripts/structural_verify.sh,.claude/skills/qa/scripts/verify_checklist.sh,.claude/skills/sv/scripts/cross_review.sh,.claude/agents/cross-reviewer.md,.claude/rules/cross-review.md
MAX_LINES=100
DIFF_REF=c2c3bf6
<!-- re-anchor c2c3bf6, 2026-07-19T07:32:52Z, 開工前合法重錨；原 0c7d471，git diff 0c7d471..HEAD -- .claude/skills/qa/SKILL.md 為空 -->

## Action Items

### AI-1: Step 4.5 Auto Commit 零 diff 分支
- **檔案**: `.claude/skills/qa/SKILL.md`（Step 4.5，約 :95-128）
- **現值**: 前置確認/Scope 汙染確認後直接執行 `git add` + `git commit`；若 ALLOWED 範圍內實際無檔案變更（`git status --short` 對 ALLOWED 檔案為空），`git commit` 會因無 staged 內容而失敗（exit 1），且此路徑無專屬處置分支，行為未定義（區別於 Step 4「票面明文優先序」已解決的 FORBIDDEN-hit SKIP 路徑——該路徑是「Step 4 跳過」，不是「Step 4.5 對零 diff 的 commit 行為」）
- **目標值**: 於「4. Commit」步驟前新增零 diff 偵測分支：
  1. 執行前檢查 `git status --short -- <ALLOWED 檔案清單>`；若輸出為空（ALLOWED 範圍內無任何變更）→ 判定為真零 diff。「輸出為空」語義明訂：untracked（`??`）新增檔**視為有變更**——ALLOWED 範圍內出現未追蹤新檔即走既有 Commit 流程，非零 diff（Amendment A1, 2026-07-19）
  2. 真零 diff 時 **MUST NOT** 呼叫 `git commit --allow-empty` 產生語意空洞的提交；**MUST** 改為：
     - 跳過「4. Commit」子步驟
     - `tmp/verify_cmd.conf` 的 `DIFF_END` 寫回 **與 `DIFF_REF` 相同值**（即不新增 commit，維持零 diff 事實）
     - QA Report 對應行改輸出 `✅ Auto Commit: SKIPPED（ZERO-DIFF-DECLARED，DIFF_END=DIFF_REF）`——此行 **MUST** 含精確 token 字串 `ZERO-DIFF-DECLARED`（複用 Check B 既有字面，不造第二 token），供下游 lint／`/sv` grep 直取；禁「語意一致但措辭自由」之改寫（Amendment A1, 2026-07-19）
     - 此輸出格式 **MUST** 與 `structural_verify.sh` Check B 既有 `ZERO-DIFF-DECLARED` 三條件宣告制語意一致（零 diff 是被宣告的狀態，非默認可放行狀態），供下游 `/sv` 承接時可直接複用同一 `ZERO-DIFF-DECLARED` 判斷邏輯
  3. ALLOWED 範圍內有實際變更（非零 diff）→ 沿用現行 Commit 流程，行為零變更
- **設計原則**: 零 diff 情境不應被「強迫產生一個空提交」掩蓋，亦不應讓腳本因未定義行為而不可預期地失敗；與 `structural_verify.sh` Check B 之零 diff 三條件宣告制同一立法精神延伸
- [x] 已實作（`43e4e5a`，`SKILL.md:108-116` 現場核對與目標值逐條相符）

## Hard Constraints
1. ALLOWED 範圍內有實際變更（非零 diff）之既有 Commit 路徑行為零變更（回歸測試必驗）
2. 本票 `.md` 觸碰僅限 `.claude/skills/qa/SKILL.md` 單檔；若判斷需連動修改 `structural_verify.sh` 或 `cross_review.sh` 之零 diff 承接邏輯 → 僅登記待開票，MUST NOT 本票夾帶（FORBIDDEN 已明列排除）
3. 真零 diff 分支 **MUST NOT** 使用 `git commit --allow-empty` 產生空提交（設計原則明文禁止），改採 DIFF_END=DIFF_REF 宣告制

## Error Handling
- 判斷「真零 diff」時發現 ALLOWED 清單本身格式異常（如含未逃逸的萬用字元）→ 停止回報附實際 `verify_cmd.conf` 內容，不得憑記憶猜測解析規則
- 若發現此缺口牽動 `structural_verify.sh` Check B 需同步調整才能正確承接 `DIFF_END=DIFF_REF` 的零 diff 宣告（例如 Check B 目前是否要求 sv_report.md 顯式 `ZERO-DIFF-DECLARED` token 才會 PASS）→ 記入結論 + 待開票，本票僅完成 SKILL.md 側行為定義，不夾帶跨檔修復

## Acceptance Criteria
- [x] **[行為性]** 建構真零 diff 情境（ALLOWED 檔案於 Step 4 完成後仍與 DIFF_REF 基準一致）→ Auto Commit 判定 SKIPPED，`DIFF_END` 寫回等於 `DIFF_REF`，QA Report 輸出含精確 token `ZERO-DIFF-DECLARED` 之 SKIPPED 行（grep 實測命中回報）—— UC02 核收點實測轉 PASS，證據見 PROGRESS.md Header row
- [x] **[行為性]** 反向測試：ALLOWED 範圍內有實際變更 → 沿用既有 Commit 流程，`DIFF_END` 為新 commit hash，行為與修改前一致 —— UC02 核收點實測轉 PASS
- [x] **[行為性]** 回歸：任一既有非零 diff 票之 Auto Commit 流程重跑 → 判定與修改前一致 —— UC02 核收點文字機械複測轉 PASS（誠實揭露：非即時觸發，附重開條件，見 PROGRESS.md Header row）
- [x] `git diff --stat` 僅涉及 `.claude/skills/qa/SKILL.md` —— `git show --stat 43e4e5a` 確認單檔 13+/4-

## Closure

- Codex Cross-Review: R2 **PASS**（REACHABILITY: NONE）
- `structural_verify.sh --post`: PASS（ADVISORY）
- Commit: `43e4e5a`
- Traycer 終審: ✅ APPROVE（Akira 2026-07-19 確認）
- 票檔搬移: `deliver/`（本次收尾）
