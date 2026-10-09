# TKA-CHANNEL-01: 使 Claude Code session 讀到 .claude/CLAUDE.md 即知正式開票必經 /ticket-author、自 plan 起步

**檔案治理/來源說明**：本票由 `/ticket-author ticket` 自 Plan-backed parent 產出；本節自含，`.tmp` 暫存檔消失後仍可獨立閱讀。
- Parent type：Plan-backed；plan-local item：`T01`。Parent 絕對路徑：`D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261009-claude-rule4e-plan.md`；artifact status：`Plan Gate: PASS`／`Ticket mode eligibility: ALLOWED`／`Fixture: false`（開票時 2026-10-09 09:26 重新讀取檔頭驗證）；Parent read：2026-10-09 09:27。
- 上游規格僅為 plan 呼叫內文（無持久檔）；核准之 e) 款全文已逐字嵌入 AI-1。核准決策（Akira 2026-10-08，經本次呼叫確認）：於 Rule 4 d) 之後新增 e) 款；議題線為 TKAuthor（ID 前綴 `TKA`）。
- 與 parent／呼叫文字之差異與修正（現值優先，已逐項處置）：
  1. **e) 款措辭**：本次呼叫之 e) 款文字較 plan artifact 所載更精確（新增「並」「根因未明時先 `diag`，再依診斷結果執行 `plan`」，且 `ticket` 條件擴為 `Plan Gate: PASS`、`Ticket mode eligibility: ALLOWED` 且 `Fixture: false`）。依來源優先序（當次明示裁決高於 parent artifact），本票採本次呼叫文字；plan 內舊措辭失效。
  2. **物理格式**：呼叫文字為顯示換行；依 plan 技術邊界「款式與 a)～d) 相同」，e) 款為**單一實體行**。換行處接合規則＝CJK 與 CJK 之間不補空白，CJK 與反引號／ASCII 之間補一空白（與 plan 原文標點／空白慣例一致）。
  3. **EOL 更正（parent 與呼叫文字皆有誤）**：plan artifact 與本次呼叫的 constraints 稱 CLAUDE.md「全檔 CRLF」。現值實測為 **LF**：`git ls-files --eol .claude/CLAUDE.md` ＝ `i/lf w/lf attr/text eol=lf`；`.gitattributes` 規定 `*.md text eol=lf`；perl 逐行 CR 計數＝0。plan 所記「130 個 CR」源自失效的量測（`grep -c $'\r'` 在此 shell 退化為空樣式而計入全部行）。限制之意圖為「不產生混合 EOL」，故本票改為**維持現有 LF、CR 計數為 0**；若改寫為 CRLF 將違反 `.gitattributes` 並造成整檔 diff。此屬事實層更正，不改變核准語義。
  4. **與 Skill 契約之落差（揭露，不處理）**：`.claude/skills/ticket-author/references/input-contract.md` 現行 parent 資格（觀測 L128–L130）仍接受 Diag-backed 與 Spec-backed 直入 `ticket` 模式；e) 款（`plan` 起步、僅 Plan-backed）較 Skill 現行行為更嚴。Skill 對齊不在本票範圍（constraints 禁改 ticket-author Skill），列為後續觀察。
  5. **多消費者掃描**：`ssot-governance.md`「修補票紀律」（觀測 L202：母票有未閉環 AC → 同步產出修補票置於 `Pending/`）與「結構歧義停損律」（觀測 L213）未規定開票通道；e) 款不與之矛盾，相容閱讀為修補票亦屬正式票、須經 `/ticket-author`。不修改該檔。`.claude/skills/*/SKILL.md` 與其他 rules 無開票通道指示（grep 實測）。
  6. **審查路徑風險**：`CLAUDE.md` 為 `cross-review.md` Checkpoint 觸發檔，且 e) 款含 `Gate` 字樣（命中該檔 token 清單，觀測 L14）；Cross-Review 若出 FAIL 且修正涉及本檔，MUST 依該檔輸出 Checkpoint 等待裁決，不得 inline 修正。
- 依賴說明：`execution_dependency`＝`TKA-ALIGN-01`（e) 款指向之 `/ticket-author` 入口由其交付，已 CLOSED，已滿足）；`evidence_dependency`＝`RGR-ACTYPE-01`（僅其 replay report 之一手證據，用於佐證 subagent 會載入專案 rules 脈絡，作為 AI-2 探針之可達性依據，非判準）。`TKAuthor_Protocol.md`（commit `91e1a2f7`）為同一裁決之 repo 內佐證，其 commit 前綴 `TKA-PROTOCOL-01` 無票檔亦無冊面，故不列為依賴票；vault 決策檔 `SKAD/2026-10-08_TicketAuthor_Upstream_Ticketing_Channel` 不可由本地讀取，標 `SUPPORTED`（relayed）。
- AC 類型說明：本票不含 `[可達性]` AC（TPL 定義為 `app/src/main/` production producer，本票標的為治理文字）。「session 讀到即知」屬落地語義，TPL 不允許僅以 `[存在性]` 承載，故以 `[行為性]` 探針（AI-2）承載；探針為單次抽樣（N=1），未跑改前對照，其判準欄位取 e) 款獨有內容以降低 Self-Satisfying 風險。

**簽發日期**: 2026-10-09
**簽發者**: Claude
**來源議題**: TKAuthor
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: TKA-ALIGN-01 —— `/ticket-author` Skill 之交付票（已 CLOSED，已滿足）
- `evidence_dependency`: RGR-ACTYPE-01 —— 其 replay report 為 subagent 脈絡可達性之一手佐證（REVIEW）
- `coordination_dependency`: 無

## Verify Config
TICKET_ID=TKA-CHANNEL-01
CREATED=2026-10-09
PRESET=docs
ALLOWED=.claude/CLAUDE.md
FORBIDDEN=.claude/skills/ticket-author/**,.claude/rules/**,plans/cso_briefing/project/file/TKAuthor_Protocol.md,plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md,plans/cso_briefing/project/TICKET_REGISTRY.json,app/**
MAX_LINES=5
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-10-09 09:27
BASE_COMMIT=9544b556
TARGET_BLOB=.claude/CLAUDE.md:bb6000d1
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

> `MAX_LINES=5`：預期淨增 1 行（e) 款），緩衝後進位；估算參考，非 gate。`FORBIDDEN` 中的 `TICKET_REGISTRY.json` 僅禁直接編輯；經平台 `registry_*` 原語之本票生命週期操作不在此限。`TARGET_BLOB` 已由 `git hash-object` 實測取得。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/CLAUDE.md`
- **動作**: 逐字確認下列現值（開票時 2026-10-09 09:27 live 探測；行號僅定位用）：
  - `### 4. 越權禁令` 恰 1 次（L68）；`^d) Session 首訊無 Ticket → 詢問後停止。$` 恰 1 次（L74）；`^### 5. 寫之前先讀$` 恰 1 次（L76）；d) 與 `### 5.` 之間僅隔一空行（L75）。
  - `^e) ` 命中 0 次；`ticket-author` 於全檔命中 0 次。
  - 全檔 130 行；`git ls-files --eol .claude/CLAUDE.md` 為 `i/lf w/lf`；perl 逐行 CR 計數為 0；`git hash-object` 前 8 碼 `bb6000d1`。
- d)／`### 5.` 錨點命中數 ≠ 1、`^e) ` 已存在、或 EOL 非 LF → 停止回報，禁推定等價位置；其餘漂移依 TPL v3.11 三分流記錄分類後以工作樹現值續作。回填 `DIFF_REF`（三原子動作由 `/uc` 啟動腳本完成）。
- [x] 完成（2026-10-09；錨點全命中，130 行、i/lf w/lf、CR=0、blob bb6000d1；DIFF_REF=9544b556）

### AI-1: Rule 4 於 d) 之後新增 e) 款（單一實體行，LF）
- **檔案**: `.claude/CLAUDE.md`
- **現值**: `d) Session 首訊無 Ticket → 詢問後停止。` 之後為一空行，接 `### 5. 寫之前先讀`。
- **目標值**: 於 d) 行之後、該空行之前，逐字插入下列單一行（行尾 LF，不含 CR；該行 sha1 前 8 碼 `b18e247d`，含行尾 LF 共 445 bytes）：

```text
e) 正式開票一律經 `/ticket-author`，並自 `plan` 起步（單項規格亦同；根因未明時先 `diag`，再依診斷結果執行 `plan`）；`ticket` 僅可對 `Plan Gate: PASS`、`Ticket mode eligibility: ALLOWED` 且 `Fixture: false` 的 plan artifact 之選定 item 執行。禁止手工建立 Pending 票檔或直接 Registry 登冊（Akira 2026-10-08；錨 Obsidian SKAD/2026-10-08_TicketAuthor_Upstream_Ticketing_Channel）。
```
- 寫入後 MUST 以 `git diff -U0`、`git ls-files --eol`、perl CR 計數自驗（見 AC1、AC2）。
- [x] 完成（e) 款已插入於 L75，單一行；sha1_8 b18e247d；工作樹未提交）

### AI-2: 以全新 subagent 做 e) 款可見性探針並記錄
- **動作**（AI-1 完成後執行；證據一律寫入本票收尾實錄）：
  1. 以 Agent 工具派發全新 subagent（`subagent_type=general-purpose`；不得使用 fork，不繼承本票 context），dispatch prompt 逐字為下列全文，且 MUST 不含 e) 款字句、預期答案或本票內容；prompt 原文 MUST 存入收尾實錄：

```text
Using only the project instructions already loaded in your context (do not read, search, or open any files and do not call any tools), answer in at most 6 sentences: in this repository, what is the required procedure for creating a formal ticket, and what is prohibited? Then stop.
```

  2. 記錄 subagent 的 agentId、`tool_uses` 計數與回答全文（逐字）。
  3. 依 AC3 逐項判定 (i)(ii)(iii)；結果寫入收尾實錄，並如實揭露 N=1 與未跑改前對照。
- [x] 完成（見收尾實錄：同 session subagent 探針無效；依 Akira 2026-10-09 裁決改以 claude -p 無頭 CLI 探針單次取證，三項判準皆成立）

## Hard Constraints
1. 產品修改僅限 ALLOWED 單檔。流程載體不計入產品範圍：本票票檔（`Pending/` → `deliver/` 搬移）、本票之 Registry 生命週期操作（僅經平台 `registry_*` 原語）、`.claude/PROGRESS.md`／`.claude/PROGRESS_ARCHIVE.md` 之例行 Dashboard 同步（依 `ssot-governance.md`「Governance Artifact 清單」與「Dashboard 慣例同步」條，行號僅定位用）；不得觸及他票之票檔與 Registry 條目。
2. 僅新增 e) 款一行：不改 a)～d) 及其他條文、不改 Skills 清單、不新增核心規則編號；`CLAUDE.md` 維持 ≤150 行（預期 131）。
3. e) 款文字依 AI-1 目標值逐字寫入；單一實體行；行尾維持現有 LF，不得引入 CR 或混合 EOL。
4. 不修改 `ticket-author` Skill、`ticket_pattern.md`、`ssot-governance.md`（含「修補票紀律」）及其他 rules、`TKAuthor_Protocol.md`；不新增或修改任何 Pending 票檔（本票自身除外）與他票 Registry 條目。
5. 探針結論限界：僅證明「單次、全新 subagent、僅憑預載入脈絡」得到所列判準內容；MUST NOT 宣稱所有未來 session 必然遵守，MUST NOT 宣稱 e) 款使模型可自行開票（`/ticket-author` 設有 `disable-model-invocation: true`，實際動作為請 Akira 執行 `/ticket-author plan`）。
6. 探針專屬限制（僅適用 AI-2）：不呼叫 Codex 或 runner；subagent 不得寫入任何檔案、Registry 或 Pending。

## Error Handling
- AI-0 錨點命中數不符、`^e) ` 已存在、行數非 130 或 EOL 非 `i/lf w/lf` → 停止回報，附實測輸出。
- 寫入後 `git diff -U0` 非恰 +1／-0，或任何非 e) 行發生變動 → 還原本票之修改並停止回報。
- 寫入後 CR 計數 > 0 或 `git ls-files --eol` 非 `i/lf w/lf` → 移除該行 CR 修復一次；仍不符 → 停止回報。
- 探針之 `tool_uses` > 0、subagent 回報其讀檔／搜尋，或實際 dispatch prompt 與 AI-2 全文不符 → 該次探針無效，停止回報。
- 探針回答缺 (i)(ii)(iii) 任一 → 停止回報，附回答原文；禁為迎合探針改寫 e) 款或 `CLAUDE.md` 其他文字。
- 判定 e) 款與 `ssot-governance.md`「修補票紀律」互相矛盾且無法唯一導出 → 停止回報 `DECISION_REQUIRED`；禁擴張 ALLOWED 修改該檔。
- Cross-Review 出 FAIL 且修正涉及 `CLAUDE.md`（或含 `Gate` token）→ 依 `cross-review.md` 輸出 Checkpoint 並等待裁決，禁 inline 修正。
- 同一驗證項經兩次修正仍不符 AC → 停止回報。
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中（標的被 .gitignore 排除）→ 停止回報（gitignore 死結律，候選 #20）。開票時實測：`.claude/CLAUDE.md` 為 git 追蹤、未被排除；`Pending/` 期票檔依 `.gitignore` 為 untracked 屬既有凍結點語義（INFRA-TRACKSCOPE-01），非本票 commit 標的。

## Acceptance Criteria
- [x] **[存在性]** `.claude/CLAUDE.md` 中以 `e) ` 開頭之行恰 1 條，位於 d) 行之後、其後空行與 `### 5.` 之前；該行（去除行尾 LF）與 AI-1 目標值區塊文字逐字相同（行 sha1 前 8 碼 `b18e247d`）。（證據：e) 恰 1 條位於 L75，鄰行為 d) 與空行／
- [x] **[存在性]** diff 純度與格式：`git diff -U0 <DIFF_REF>..<DIFF_END> -- .claude/CLAUDE.md` 新增恰 1 行、刪除 0 行（`git diff --numstat` 為 `1 0`）；`git diff --name-only <DIFF_REF>..<DIFF_END>` 僅 `.claude/CLAUDE.md`；全檔 131 行（≤150）；`git ls-files --eol .claude/CLAUDE.md` 為 `i/lf w/lf`；perl 逐行 CR 計數為 0；Skills 清單與 `### 5.` 之後內容位元組級不變（刪除行數 0 即涵蓋）。（證據：提交後 git diff --numstat 9544b556..101ef872 為 1 0；131 行；i/lf w/lf；CR=0；僅 .claude/CLAUDE.md）
- [x] **[行為性]** given AI-1 完成後之 `.claude/CLAUDE.md`，when 由全新 subagent（不繼承本票 context，依 AI-2 dispatch prompt 原文，不呼叫任何工具）被詢問「建立正式票之必要流程與禁止事項」，expect 其回答含：(i) `/ticket-author`；(ii) 自 `plan` 起步（先 plan，而非直接 ticket）；(iii) 禁止手工建立 Pending 票檔和／或直接 Registry 登冊；且 Agent 回報之 `tool_uses` 為 0。observer＝dispatch prompt 原文、subagent 回答全文與 `tool_uses` 計數（皆存於收尾實錄）。(ii)(iii) 為 e) 款獨有內容。結論限界見 HC-5。（證據：claude -p 單次，0 個 tool_use，回答含 (i)(ii)(iii)，原文見收尾實錄）
- [ ] ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] **[存在性]** `git diff --stat` 僅涉及 ALLOWED 檔案（以 `DIFF_REF..DIFF_END` 實作 commit 範圍計；HC-1 所列流程載體另行提交，不在此範圍內計算）（證據：git diff --name-only 9544b556..101ef872 僅 .claude/CLAUDE.md）

## 收尾實錄（Closeout，2026-10-09）

- **實作 commit**：`101ef872`（`[TKA-CHANNEL-01]`，`.claude/CLAUDE.md` +1／-0）；`DIFF_REF=9544b556`、`DIFF_END=101ef872`。提交後 131 行、`git ls-files --eol` 為 `i/lf w/lf`、`git diff --numstat 9544b556..101ef872` 為 `1 0`。
- **裁決紀錄（Akira 2026-10-09）**：AI-2 同 session subagent 探針無效後，裁決 AC3 改以無頭 CLI 探針取證、只試一次、不開 Rev A；本票只改一個 `.md`、可 `git revert`，依 2026-10-02 風險分級省略 `/qa`、`/sv`、cross_review；e) 款提交後依既有關票順序轉 `CLOSED`。
- **同 session subagent 探針（無效，已作廢）**：agent `ae8e8a78c11bdaad8`，`tool_uses=1`（>0，依 Error Handling 無效），回答缺 `/ticket-author` 與禁手工建 Pending／登冊；判斷為 subagent 繼承 session 啟動時載入之舊 CLAUDE.md，session 中途改動不可見。
- **AC3 證據（`claude -p` 單次，未重試）**：`claude --version` 2.1.295；以與 AI-2 相同之 prompt 起新 process（`--no-session-persistence`，`--disallowedTools Read Grep Glob Bash Edit Write Agent Skill WebFetch WebSearch`，`--output-format stream-json --verbose`），`tool_use` 事件數 0，assistant 訊息 1 則。回答原文：

```text
正式開立 Ticket 必須經由 `/ticket-author`，且一律從 `plan` 模式起步（即使是單項規格；若根因未明則先 `diag`，再依診斷結果走 `plan`）。`ticket` 模式僅能對 `Plan Gate: PASS`、`Ticket mode eligibility: ALLOWED`、且 `Fixture: false` 的 plan artifact 中「已選定的項目」執行。

禁止事項：不得手工建立 `Pending/` 票檔，也不得直接對 Registry 進行登冊（CLAUDE.md Rule 4e，Akira 2026-10-08 裁決）。此外無 Ticket 不得動手（Rule 4a），也不得依 MEMORY/PROGRESS/TODO/FIXME 自行開啟新工作（Rule 4b）。
```

  判定：(i) `/ticket-author` ✓；(ii) 自 `plan` 起步 ✓；(iii) 禁止手工建立 Pending 票檔或直接 Registry 登冊 ✓ → AC3 PASS。回答直接引用 e) 款並稱「Rule 4e」，e) 款獨有內容皆出現。
- **限制與揭露**：①單次抽樣（N=1），未跑改前對照；結論限於「全新 process 讀工作樹現值 CLAUDE.md 後，單次回答含所列判準」，不宣稱所有未來 session 必然遵守，亦不代表模型可自行呼叫 `/ticket-author`（`disable-model-invocation: true`）。②`claude -p` 的 SessionStart hook 清除了 `tmp/verify_cmd.conf`；本票已不需要該檔（省略 `/qa`、`/sv`），收尾時未重建。③本票省略 `/qa`、`/sv`、Cross-Review（依裁決），故 `reviews` 之 qa／sv／codex 填 `N_A` 並於此揭露豁免依據；Traycer 為 Header `N/A`。
- **觀測（Akira 裁決指定記載）**：AC3 設計為 session 內不可達之 observer，屬 ticket-author 票面缺陷首次觀測；未達回修條件，不開票。
