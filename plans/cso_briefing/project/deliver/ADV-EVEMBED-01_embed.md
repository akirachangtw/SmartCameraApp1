# ADV-EVEMBED-01: `ticket-author` close 第 4 步明定——reviewer 無法讀檔時，核心 AC 所依賴之 producer 原件須逐字嵌入 review prompt 與票檔，摘要表與 scratch 路徑不得作唯一證據

**簽發日期**: 2026-10-24
**簽發者**: Claude
**來源議題**: Advisor
**Mode**: normal  ||  **Effort**: Medium
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: ADV-CLAUDECLI-01 —— 核心 AC12 兩輪 UNVERIFIED（reviewer 回覆皆載無法讀取 scratchpad 原件），補件 4 逐字嵌入原始事件後具名續審 PASS 之實證；TKA-CLOSE-01 —— close 第 4 步原立法
- `coordination_dependency`: ADV-SAVE-01 —— 同檔（`.claude/skills/ticket-author/SKILL.md` close 程序），本票於其交付後執行，以其交付後之第 4 步現文為錨點

## 來源（Parent）
- Parent type：Plan-backed；路徑 `D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261024-autonomy-closeloop-plan.md`（Revision R2；Fixture false／Plan Gate PASS／Ticket mode eligibility ALLOWED）；plan-local item `T05`；讀取時間 2026-10-24T05:30Z。
- Selected item：close 收尾證據逐字嵌入慣例（diag C7、C8）。
- 承重決策：plan D4（使用者裁決）——本輪不建耐久證據路徑，以逐字嵌入規範化。

## 證據（開票時 live，2026-10-24 @2d8e7e34）
- close 第 4 步現文要求「Build a compact evidence bundle from `preflight.json` and the Ticket (reuse valid existing evidence; never truncate AC text)」，未規定 reviewer 不能讀檔時之原件處置。
- ADV-CLAUDECLI-01 reviewer 回覆 1–4 均載「本機 shell 無法啟動／無法讀取原件」；`codex exec` 讀檔失敗慣例另見 memory `codex-exec-helper-unknown-error-use-evidence-bundle`（輔助，非承重）。
- 原件常駐位置非耐久：route.py 執行記錄於 git-ignored `tmp/`、案例 repo 於 session scratchpad；唯一耐久方式為嵌入 `deliver/` 票檔（ADV-CLAUDECLI-01〈AC12 原始事件嵌入〉，以 `L<n>:` 標行號、`sk-ant-` 遮蔽規則）。
- 反例檢查：要求「一律嵌入」會使大型原件膨脹票檔；故觸發限於「核心 AC 依賴之 producer 原件且 reviewer 無法直接讀取」，範圍限與該 AC 相關之行（逐字＋行號＋整檔 sha256），非整檔。

- 票號歸屬：本票由「工程師自主閉環」議題（源自 ADV-CLAUDECLI-01 收束與 Advisor 成果表 P1 缺口）發起，依 Akira 2026-10-24 裁決，同一議題 session 發起之票一律掛該議題前綴 `ADV`，不依修改落點另分前綴；原開票 ID 已以 `registry_rename_ticket` 改名。

## Verify Config
TICKET_ID=ADV-EVEMBED-01
CREATED=2026-10-24
PRESET=docs
ALLOWED=.claude/skills/ticket-author/SKILL.md
FORBIDDEN=.claude/skills/ticket-author/scripts/**,.claude/skills/ticket-author/tests/**,.claude/skills/ticket-author/references/**,.claude/skills/sv/**,.claude/skills/compact/**,.claude/rules/**,.claude/scripts/**,.claude/hooks/**,.claude/CLAUDE.md,.claude/PROGRESS.md,.claude/PROGRESS_ARCHIVE.md,CLAUDE.md,app/**
MAX_LINES=10
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-10-24 14:15
BASE_COMMIT=2d8e7e34
TARGET_BLOB=N/A（docs preset；AI-0 錨點替代，且錨點須於 ADV-SAVE-01 交付後重取）
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/skills/ticket-author/SKILL.md`
- **動作**: 確認 close 程序第 4 步含「Build a compact evidence bundle from `preflight.json` and the Ticket (reuse valid existing evidence; never truncate AC text).」恰 1 次；確認 ADV-SAVE-01 已交付（close-dispatch 含 `persist`）或記錄其狀態；確認 `.claude/skills/ticket-author/tests/test_close.py` 之 `TestUpstreamWiring`／`TestSkillCallParser` 不以全文比對鎖定第 4 步文字（若有，停止回報）。
- **任一項不符 → 依 TPL v3.11 基線漂移三分流處置。**
- [x] 完成（2026-10-24：錨點命中 1 次〔SKILL.md:286 step 4〕；ADV-SAVE-01 已交付〔close-dispatch 含 persist〕；`test_close.py` 之 TestUpstreamWiring／TestSkillCallParser 未鎖定 step 4 文字〔grep `evidence bundle|never truncate|review_prompt` 於測試檔 0 命中〕）

### AI-1: 第 4 步新增逐字嵌入規則
- **檔案**: `.claude/skills/ticket-author/SKILL.md`
- **現值**: 第 4 步僅要求 compact evidence bundle、不截斷 AC 文字。
- **目標值**: 於該句之後新增一句（英文，與 SKILL 現有語言一致），要旨：reviewer 無法直接讀取某核心 AC 所依賴之 producer 原件（scratch、git-ignored 或 session 外路徑）時，Agent 須將該原件中與該 AC 相關之行**逐字**（附 `L<n>:` 行號與整檔 sha256，秘密依既有規則遮蔽並註明遮蔽規則）嵌入 review prompt，並於收束前將同一嵌入落於票檔（耐久）；摘要表、改寫敘述或僅給路徑不得作該 AC 之唯一證據；不新增 verdict、欄位或 reason code。
- [x] 完成（2026-10-24：提交 `81c20f72`，SKILL.md 1 檔 1+/1-）

## Hard Constraints
1. 只改第 4 步一句之增補；close 其他步驟、decide 判定與 helper 不變。
2. 不建耐久證據路徑或新目錄（D4）。

## Error Handling
- 既有測試全文鎖定第 4 步 → 停止回報。
- **[標配]** `git check-ignore -v .claude/skills/ticket-author/SKILL.md` 命中 → 停止回報。

## Acceptance Criteria
- [x] **[存在性]** close 第 4 步含逐字嵌入規則，涵蓋：觸發條件（核心 AC＋reviewer 無法讀原件）、嵌入範圍（相關行逐字＋行號＋sha256＋遮蔽註明）、prompt 與票檔雙落點、摘要／路徑不得作唯一證據。（觸發＝reviewer 無法直接讀取核心 AC 所依賴之 producer 原件〔scratch、git-ignored 或 session 外〕；範圍＝與該 AC 相關行逐字＋`L<n>:` 行號＋整檔 sha256＋遮蔽規則註明；落點＝`<dir>/review_prompt.md` 與票檔〔耐久〕；摘要表、改寫敘述或僅給路徑不得作唯一證據；末句『adds no verdict, field or reason code』）
- [x] **[存在性]** `test_close.py` 全數通過（證明未破壞既有 SKILL 解析測試）。（`python -I test_close.py` → `Ran 130 tests ... OK`，2026-10-24，於提交 81c20f72 前之工作樹）
- [x] ~~assembleDebug PASS~~ (N/A: docs preset)（票面劃記 N/A，無 app/ 變更，未執行 Gradle）
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（實測 `ticket-author/SKILL.md | 2 +-`，1 insertion／1 deletion；`git diff --check` 無輸出）

## 收尾實錄（Agent closeout，2026-10-24；交 TKA close 判定，非自行終審）
- **路徑**：低風險免 QA／SV（ALLOWED 僅 `.md` 之 `ticket-author/SKILL.md`；無 `app/`／腳本／hook 變更、無不可逆操作、可 `git revert`）；已先 `check commit`（exit 0）再以 `[ADV-EVEMBED-01]` 提交。
- **實作提交**：`81c20f72`（`SKILL.md` 1 insertion／1 deletion）。
- **驗證**：close 第 4 步新增句逐字見 AC1 回填；`test_close.py` 130 OK；`git diff --check` 無輸出。
- **未執行**：`/qa`、`/sv`、Cross-Review、Gradle（依風險分級省略／票面 N/A）；`reviews` 四欄皆 `N_A`。
- **揭露**：PROGRESS.md Dashboard 同步屬 FORBIDDEN 延後項，待 `/compact`。本票即為 reviewer 讀不到檔問題之規範化；TKA close 沿用本 session 已用之「證據逐字嵌入 prompt」做法（本票 close 本身即遵循新規則之範例，指示式觀察、非盲測）。


<!-- close-result:begin -->
## 收束結果（close）
- 交付摘要: 交付：ticket-author SKILL close 第 4 步新增規則——reviewer 無法讀檔時，核心 AC 依賴之 producer 原件相關行逐字（含 L<n>: 行號、整檔 sha256、遮蔽註明）嵌入 review prompt 與票檔，摘要／路徑不得作唯一證據（提交 81c20f72，1 檔 1+/1-）。 / 驗證：test_close.py 130 OK；TKA close reviewer gpt-6-sol 4 條 AC：PASS／PASS／N/A／PASS（本次 close 即依新規則將 SKILL.md 該行原件與 sha256 逐字嵌入 prompt）。 / 限制：低風險 docs 票省略 /qa、/sv、Cross-Review（reviews 皆 N_A）；新規則為模型契約，無機械後盾。
- 最終驗收結論: 核心 AC 1 條皆為 PASS 或有效 N/A；非核心 PASS 2／N/A 1／UNVERIFIED 0／FAIL 0（共 4 條 AC）
- 實作提交: 81c20f72
- 實際收束結果: 2026-10-24 轉 CLOSED，lifecycle／file／reviews 讀回一致；reviewer: codex/gpt-6-sol（tier=heavy）；覆蓋 AC 4／4（全部），independent_call=True
- 披露: reviewer limitation: reviewer 無法讀檔，證據（含 SKILL.md 該行原件、整檔 sha256、行號）逐字嵌入 prompt，依本票新增之規則；回覆檔取自 codex exec 最終訊息段（原始輸出 reviewer_reply_raw.txt）；Q
<!-- close-result:end -->
