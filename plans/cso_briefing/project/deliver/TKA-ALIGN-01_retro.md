# TKA-ALIGN-01: 已部署 ticket-author P0 對齊核准三模式契約並以無正式票務副作用之 fixtures 驗證

**檔案治理/來源說明**：裁決 SSOT＝vault `wiki/decisions/SKAD/2026-10-06_TicketAuthor_P0_Retro_Alignment.md`（本文 D1–D21＋「補記 2026-10-06」A1–A12）。規範契約全文＝Intake `D:\DevSources\Smarter-Project\deliver\ticket-author-p0\ticket-author-implementation-intake.md`（2026-10-06 改寫版，§1–§10 為 Current Normative Contract；下文「Intake §n」均指該檔）。本票為 Spec-backed 直接開票（Desktop 手工依 TPL v3.11 開立，非經 `/ticket-author ticket`）。

**簽發日期**: 2026-10-06
**簽發者**: Claude
**來源議題**: TKAuthor
**Mode**: uc  ||  **Effort**: High
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: 無
- `coordination_dependency`: 無

## Verify Config
TICKET_ID=TKA-ALIGN-01
CREATED=2026-10-06
PRESET=docs
ALLOWED=.claude/skills/ticket-author/SKILL.md,.claude/skills/ticket-author/references/input-contract.md,.claude/skills/ticket-author/references/state-transitions.md,.claude/skills/ticket-author/references/stop-conditions.md
FORBIDDEN=.claude/skills/ticket-run/**,.claude/skills/ticket-run/ticket-run.project.md,app/**,.claude/hooks/**,TICKET_REGISTRY.json,plans/cso_briefing/reports/GAP-10-BREATH-RESTART_D17_HaloJitter_Diagnosis_20260713.md
MAX_LINES=N/A
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-10-06 14:47
BASE_COMMIT=5159079
TARGET_BLOB=N/A
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

> MAX_LINES=N/A：AI 含 fail-closed 資格判定（Fixture／Plan Gate／eligibility／Verdict）之守門不變量，失敗形狀枚舉為審查發現式，不可先驗估算（TPL R3）。
> TARGET_BLOB=N/A：Desktop 開票端無合法 blob-hash producer（MCP 唯讀 git 原語不回傳 blob hash），依 TPL v3.11 ③ fallback；BASE_COMMIT 已填，AI-0 逐檔讀取現值錨點為替代控制。
> 範圍外（不入 ALLOWED、不得修改）：Intake 文件、Smarter-Project 下之 UC fixture、外部研究（`research_submit`）接線、rigor 審查自動串接、project profile／schema、Registry schema、MCP／daemon／database、fixture 專用正式寫票旁路、假票號或直接 Registry state injection、Craft 同步。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: ALLOWED 四檔
- **動作**: 逐字確認下列現值於檔內命中（開票時 2026-10-06 14:47 live grep 取樣；行號僅定位用）：
  - `SKILL.md` L43–L45：`- Always read \`references/input-contract.md\`.`／`state-transitions.md`／`stop-conditions.md` 三行
  - `SKILL.md` L46：含 `before creating, reviewing, or modifying a formal Ticket.`
  - `SKILL.md` L47：`- If Craft synchronization is explicitly requested, fully read \`Craft_MCP_Protocol.md\` before any Craft operation.`
  - `SKILL.md` L165：`17. Write/register only through the existing authorized mechanism and verify the write.`
  - `SKILL.md` L190：含 `next action \`RGR\``
  - `SKILL.md` L219：`## Response shape`
  - `input-contract.md` L33：`| \`requested_output\` | Report, Ticket Map, dry run, or formal Ticket |`
  - `input-contract.md` L82：`- One selected Ticket Map item or equivalently atomic objective.`
  - `stop-conditions.md` L3：`## Response contract`；L23 `STOP_INVALID_MODE`…`BLOCKED`；L38 `STOP_SYMPTOM_NOT_OBSERVABLE`…`BLOCKED`；L48 `STOP_NOT_DECOMPOSABLE`…`BLOCKED`；L56 `STOP_GOVERNANCE_UNAVAILABLE` 含 `Evidence spec or current Ticket template`；L58 `STOP_DUPLICATE_TICKET`…`BLOCKED`；L60 `STOP_BASELINE_CONTAMINATED`；L66 `STOP_EVIDENCE_STALE`
  - **F3 收尾表對應錨點**（RGR TKA-ALIGN-01 第 4 次複查新增，2026-10-06 live grep 實測）：`SKILL.md` L240：`Final state: DECISION_REQUIRED | BLOCKED`；`state-transitions.md` L21–23：`ANY ACTIVE STATE → BLOCKED`／`DECISION_REQUIRED → INPUT_VALIDATED | MODE_ANALYSIS | BLOCKED`／`BLOCKED → end run`；L42：`` | `BLOCKED` | Access, governance, evidence, or write prevents progress | ``；L92：`A failed write verification transitions to \`BLOCKED\`, not \`COMPLETE\`.`
- 錨點不符時依 TPL v3.11 基線漂移三分流處置（本票僅觸及 `.claude/` 可逆治理面：記錄分類後以工作樹現值續作）。
- [x] 完成（錨點全命中；git log 5159079..HEAD -- ticket-author 零提交，無漂移）

### AI-1: Probe 正式寫票與 Registry 工具契約，修正 F4 寫票序列
- **檔案**: `.claude/skills/ticket-author/SKILL.md`
- **現值**: L165 `17. Write/register only through the existing authorized mechanism and verify the write.`（未具名工具與參數）
- **目標值**: 依 Intake §6.5 改為具名序列（查重 `Pending/`／`deliver/`／`void/`＋同 project Registry → 落 `PROFILE.TICKET_FILE_BASE` 下 `Pending/` → upsert 顯式 project／ID／file → 顯式 `PENDING` lifecycle → `registry_list(project=<PROFILE.PROJECT>, fields 含 file)` read-back → `ticket-run` 同等查詢可發現）。工具名、參數、`file` 欄路徑慣例以本 AI **唯讀 probe 實測**為準並寫入：①讀 `ticket-run.project.md` 確認五欄；②`registry_list(project=SmartCameraApp1, ticket_id=TKA-ALIGN-01, fields=["id","lifecycle","file","topic"])` 觀測本票冊面形狀；③判定 `file` 欄採 repo 相對（`plans/cso_briefing/project/Pending/...`）或 `TICKET_FILE_BASE` 相對（`Pending/...`）——冊面現存兩種慣例並存，Skill MUST 寫明單一採用值及理由。開票時觀測（2026-10-06）：現行 PENDING 十筆均為 `Pending/...`（`TICKET_FILE_BASE` 相對，與 `ticket-run` L68 `TICKET_FILE_BASE + "/" + file` 組路徑一致）；repo 相對僅見於 archive 舊票；本票冊面 `file` 初登 repo 相對後已更正為 `Pending/TKA-ALIGN-01_retro.md`。本 AI 不執行任何 Registry 寫入或測試開票。
- [x] 完成（probe：profile 五欄齊備；registry_list 本票 file=Pending/TKA-ALIGN-01_retro.md → 採 TICKET_FILE_BASE 相對；upsert ticket_data 為含 id 之 dict；寫入於 SKILL.md ticket 步驟 17）

### AI-2: 修正 F1–F3；三軸證據模型與 L18 逐字四態
- **檔案**: `SKILL.md`、`references/input-contract.md`、`references/state-transitions.md`、`references/stop-conditions.md`
- **現值**: diag 無雙 baseline 分欄；plan 無決策三態與 provisional 規則；`stop-conditions.md` L56 `Evidence spec or current Ticket template`；L60 `STOP_BASELINE_CONTAMINATED`、L66 `STOP_EVIDENCE_STALE`；無三軸模型與四態定義
- **目標值**: 依 Intake §2（TPL 唯一必讀，L56 改為僅 TPL 並記錄版本／文件日期／有效修訂日）、§3（三軸＋Claim Status 逐字副本；副本 MUST 與 `D:\DevSources\Smarter-Project\file\Instructions_for_Claude.md` L18 全行逐字一致，標示 verbatim local copy 與來源節名）、§4（diag 固定章節，`External research` 可為 `NOT_RUN`）、§5（plan 三態、provisional 檔頭欄位）、§8 末段（刪 L60、L66 二碼；HEAD／工作樹／revision 降為診斷記錄）
- [x] 完成（L18 逐字副本 grep -x 雙檔各 1 命中；三軸模型、diag 固定章節、plan 三態入 SKILL.md；L56/L60/L66 已改刪）

### AI-3: 修正 F5–F6；機械 parent 分流、Fixture fail-closed、來源嵌入、停止模型
- **檔案**: `SKILL.md`、`references/input-contract.md`、`references/state-transitions.md`、`references/stop-conditions.md`
- **現值**: `input-contract.md` L82 `One selected Ticket Map item or equivalently atomic objective.`；停止碼終態為 `BLOCKED`／`DECISION_REQUIRED` 二分；無 parent 類型判定、無檔頭定義、無來源嵌入規則、無跨模式落點
- **目標值**: 依 Intake §6.1–§6.4（檔頭定義、Plan→Diag→Spec 優先序、三類資格表、`STOP_REPLAN_REQUIRED` 適用、PARTIALLY_CONFIRMED 停止範圍與非硬風險未知之 AI-0 probe＋`[行為性]` AC 路徑、票面嵌入九欄）、§8（三終態＋reason_code；L23/L58 → `DECISION_REQUIRED`、L48 改以單一耦合項 COMPLETE、L38 併入 `INSUFFICIENT_EVIDENCE`；不新增同義碼）、§9（`<PROFILE.REPO_ROOT>/.tmp/ticket-author/` 落點與命名）
- **F2 補正（2026-10-06 Akira 裁決；RGR TKA-ALIGN-01 F2「甲修正版」）**：保留 `stop-conditions.md` 表之 `Final state` 欄——不刪欄、不將逐碼映射搬入 `SKILL.md`，表欄維持 `Code｜Trigger｜Final state` 三欄（恢復點仍屬 `state-transitions.md`，不新增欄）。依 Intake §8 改值：`STOP_SOURCE_UNAVAILABLE`、`STOP_ACCESS_BLOCKED`、`STOP_GOVERNANCE_UNAVAILABLE`、`STOP_WRITE_FAILED` → `CHANNEL_BLOCKED`（`STOP_WRITE_FAILED` 限已依契約正式寫入、最小安全重試後仍寫入失敗或 read-back 不符之通道異常；票面內容或格式錯誤先走對應修復碼，不得提前轉此碼）；`STOP_POPULATION_UNKNOWN`、`STOP_AC_UNREACHABLE`、`STOP_STATE_SPACE_INCOMPLETE`、`STOP_CONSUMER_UNKNOWN`、`STOP_FIXTURE_INVALID`、`STOP_TEMPLATE_INVALID` → `REPAIR_EXHAUSTED`，表內註明「先自動修復或補證並重新驗證；同一 blocker 兩次未通過方可輸出」。未命中硬風險且可由 AI-0 probe 與可達 observer 驗證之 runtime 未知，不得直接輸出上列 STOP，依 Intake §6.3 記 `UNKNOWN`＋AI-0 probe＋failure branch＋`[行為性]` AC。停止回應欄位名統一沿用現行 `Final state:`（Intake §3 所稱 `State` 即此欄，不另增 `State:`）；response shape 其餘欄位以現行 `SKILL.md` 範本為準，本補正不新增欄位。定位以 reason code 字面為準，行號僅供參考。
- **F2 完成檢查**：①掃描 `stop-conditions.md` **與 `SKILL.md`** 全部 `BLOCKED` 字面（含 `SKILL.md` L240 與 `stop-conditions.md` L14 現值 `Final state: DECISION_REQUIRED | BLOCKED`），區分歷史／遷移／禁止敘述與有效值；有效 STOP 終態值僅限 `DECISION_REQUIRED`／`CHANNEL_BLOCKED`／`REPAIR_EXHAUSTED`（PASS 範本之 `COMPLETE` 不在此限）；②每個保留之 `STOP_*` 有且僅有一個合法終態；③六個 `REPAIR_EXHAUSTED` 列含「兩次未通過」語義；④未硬風險 runtime 未知之 AI-0 probe 路徑仍存在；⑤逐碼表未複製至 `SKILL.md`；⑥AI-3、AC 與 Intake §8 用語一致。空參數 probe 由 AI-5 執行、以既有 AC 驗證，本 AI 不重複。
- **F3（RGR TKA-ALIGN-01 第 4 次複查新增，非 Akira 裁決原文——下方為 RGR 補充之兄弟檔收尾指示）**：F2 完成檢查①之掃描範圍僅明文列出 `stop-conditions.md`／`SKILL.md`，未納入同屬本 AI 檔案清單、同樣承載終態字面的 `references/state-transitions.md`——其 L92（現值「A failed write verification transitions to `BLOCKED`, not `COMPLETE`.」）具名指稱 `STOP_WRITE_FAILED` 之終態，而該碼已被上方「F2 補正」改映射為 `CHANNEL_BLOCKED`，此行用字須同步修正（改寫為 `CHANNEL_BLOCKED` 或改指向 `stop-conditions.md` 對應列）。`state-transitions.md` 其餘以 `BLOCKED` 為內部狀態機節點名稱之處（側狀態圖「`ANY ACTIVE STATE → BLOCKED`」「`BLOCKED → end run`」、Receipts 表「`` | `BLOCKED` | Access, governance, evidence, or write prevents progress | `` 」列）依 Intake §10 檔案職責分工（本檔僅負責「狀態、receipt、恢復」，三終態 SSOT 在 `SKILL.md`）**無須**因三終態改名而連動改名；是否改名屬另一輪獨立設計選擇，本 AI 不代為裁決、不因此停止。
- **F3 收尾表**（以下項目須逐項落實；行號僅為開票時錨點，實作與驗收一律依可搜尋字面及 AC-118 定位。`stop-conditions.md`／`SKILL.md` 之具體修改分屬 AI-2／AI-4 既有範圍，此處列入僅供 AC-118 完整性核對用，非另行指派 AI-3 執行其中非 `state-transitions.md` 之項）：

  | 位置／契約 | 處置 | 驗證 |
  |---|---|---|
  | `stop-conditions.md` 各 `Final state` 儲存格中的裸 `BLOCKED` | 依 F2 補正逐碼改為 `CHANNEL_BLOCKED`、`REPAIR_EXHAUSTED`，或依既有裁決刪除整列 | 依 AC-118 子項①驗證 |
  | `stop-conditions.md` 的 Response contract 範本 | 依既有 AC 刪除，該檔僅保留 reason-code 表及自修／重試規則 | 沿用既有「無 response shape 章節」AC |
  | `SKILL.md` 的 `Final state: DECISION_REQUIRED \| BLOCKED` | 改為三終態 response shape | 依 AC-118 子項②驗證 |
  | `state-transitions.md` 的合法內部 `BLOCKED` 狀態機節點 | 保留，不因輸出三終態遷移而改名 | 依 AC-118 子項③的排除清單確認 |
  | `state-transitions.md` 的 write-verification failure 終態斷言（L92） | 將輸出終態改為 `CHANNEL_BLOCKED`，或明確引用 `stop-conditions.md` 對應 reason-code 列 | 依 AC-118 子項③驗證 |
  | 六個 `REPAIR_EXHAUSTED` reason-code 列 | 保留「先自修／補證；同一 blocker 兩次未過才達終態」語義 | 依 AC-118 子項④驗證 |
- [x] 完成（AC-118 子項①②③④ 實測皆零殘留 / 六列 REPAIR_EXHAUSTED=6）

### AI-4: 修正 F7–F10；fixture 收緊、檔名雙限制、response shape 落點
- **檔案**: `SKILL.md`、`references/input-contract.md`、`references/stop-conditions.md`
- **現值**: `SKILL.md` L43–L45 三行 `Always read`；L46 `reviewing, or modifying`；L47 Craft；L190 `next action \`RGR\``；`input-contract.md` L33 `dry run`；`stop-conditions.md` L3 `## Response contract` 與 `SKILL.md` L219 `## Response shape` 並存；無 `fixture_execution`；無檔名規則
- **目標值**: 依 Intake §10（references 按需載入；response shape 與三終態 SSOT 移入 `SKILL.md`，`stop-conditions.md` 僅留 reason_code 表與自修上限；刪 `dry run`、`reviewing, or modifying`、Craft 行）、§6.7（PASS 後 Next 指向專案既有審查路徑）、§7（`fixture_execution` 欄、四訊號、`Input normalization` 輸出、Fixture 禁止事項、artifact 契約表）、§6.6（slug ≤12 且 basename ≤40）
- [x] 完成（references 路由、dry run/Craft/reviewing 字樣已刪、response shape 三終態入 SKILL.md、fixture 四訊號與檔名雙限制入 input-contract.md）

### AI-5: 空參數 probe 與 Halo fixture
- **動作**: ①Claude Code 執行 `/ticket-author`（不帶任何參數）；②執行 `/ticket-author diag` 與 `/ticket-author plan`，輸入含 `fixture_execution: true` 與 source `D:\DevSources\AndroidProj\SmartCameraApp1\plans\cso_briefing\reports\GAP-10-BREATH-RESTART_D17_HaloJitter_Diagnosis_20260713.md`（原地唯讀）。前後依零寫入 observer 記錄。輸出原文存為執行證據。
- [x] 完成（證據：2026-10-06 對話 /ticket-author 空參數→STOP_INVALID_MODE；Halo diag→PASS/PARTIALLY_CONFIRMED；Halo plan→STOP_GOAL_AMBIGUOUS；artifact 存 .tmp/ticket-author/，前後 observer 一致）

### AI-6: UC fixture（未裁決＋synthetic decision）
- **動作**: ①`/ticket-author diag`、`/ticket-author plan`，`fixture_execution: true`，source `D:\DevSources\Smarter-Project\deliver\Autonomous_closed_Antiover\Autonomous_closed_Antiover_uc_Permit.md`；②重跑 `/ticket-author plan`，**省略** `fixture_execution`，附 `Fixture decisions:`（來源 §10 六項皆 APPROVED）；③以 ② 之 plan artifact 為 parent 執行 `/ticket-author ticket`，預期拒收。全程零寫入 observer 記錄；禁手工建立或修改 artifact。
- [x] 完成（證據：UC diag→PASS/PARTIALLY_CONFIRMED；UC plan 未裁決→STOP_DECISION_REQUIRED；plan+Fixture decisions→Input normalization 輸出、Plan Gate PASS、T01–T03、Fixture: true、eligibility FORBIDDEN；ticket 以該 plan 為 parent→STOP_REPLAN_REQUIRED；每步 observer 一致）

### AI-7: 唯讀驗證、ALLOWED diff、轉 REVIEW 並寫 live acceptance contract
- **動作**: ①以 `ticket-run` L30 同等字面 `registry_list(project=SmartCameraApp1, lifecycle="PENDING", fields=["id","title","file","lifecycle"], limit=50)` 唯讀確認可發現性查詢字面與 Skill 寫入序列一致；②`git diff --name-only` ⊆ ALLOWED；③依序 `registry_set_status(TKA-ALIGN-01, REVIEW)` → `registry_set_checkpoint(checkpoint_data={live_acceptance_contract: <Intake Acceptance Intent 所載 YAML>})` → `registry_list(ticket_id=TKA-ALIGN-01, fields=["lifecycle","checkpoint"])` read-back。**MUST NOT** 先 checkpoint 後轉狀態（set_status 會清空既有 checkpoint）。
- [x] 完成（①`registry_list(PENDING)` 字面與 Skill 步驟 17.6 一致，TKA-ALIGN-01 當時為 IN_PROGRESS 故不在 PENDING 清單屬預期；②`git diff --name-only` ⊆ ALLOWED；③見收尾實錄：先搬 deliver/ → set_status REVIEW → set_checkpoint → read-back）

## Hard Constraints
1. Claim Status 定義 MUST 為 `Instructions_for_Claude.md` L18 全行逐字副本；禁摘要、改寫或為 `HYPOTHESIS` 附加 probe 義務；Gate 與 Diag Verdict 不得標為鏡像。
2. Fixture 訊號只能收緊權限；任何參數不得解除 `Fixture: true`。
3. 不新增與既有碼同義之 reason code（含 `STOP_PLAN_REQUIRED`）。
4. 本票不以 fixture 執行完整正式寫票：禁 seed plan、fixture ticket、測試票號、測試 Registry 寫入與事後 void。
5. AI-5／AI-6 MUST 經 `/ticket-author` 正式入口執行；禁手工建立、編輯 artifact 或注入內部狀態。
6. HEAD、工作樹狀態、Registry revision 不得寫成 Skill 之阻斷條件。
7. 禁以任何通道直改 `TICKET_REGISTRY.json`；冊面僅經平台 registry 原語。

## Error Handling
- TPL v3.11 或 `Instructions_for_Claude.md` L18 不可完整讀取 → 停止回報（CHANNEL_BLOCKED）。
- AI-1 probe 顯示平台工具無法達成 Intake §6.5 任一步驟（例如 lifecycle 無法顯式設定且 upsert 不帶 lifecycle）→ 停止回報，附實測回傳原文。
- 同一驗證項經兩次修正仍不符 AC → 停止回報（REPAIR_EXHAUSTED）。
- AI-5／AI-6 執行前後零寫入 observer 出現差異 → 停止回報，附差異清單。
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中（標的被 .gitignore 排除）→ 停止回報（gitignore 死結律，候選 #20）

## Acceptance Criteria
- [x] **[存在性]** `SKILL.md` 含與 `Instructions_for_Claude.md` L18 全行逐字一致之一行（full-line anchored grep 雙檔命中），並附 verbatim local copy 標示與來源路徑／版本／節名。（雙檔 grep -x 各 1 命中）
- [x] **[存在性]** ALLOWED 四檔 grep 零命中：`STOP_BASELINE_CONTAMINATED`、`STOP_EVIDENCE_STALE`、`Evidence spec`、`Craft`、`dry run`、`reviewing, or modifying`、`` next action `RGR` ``、`STOP_PLAN_REQUIRED`、`Always read`。（9 項禁用字串於 ALLOWED 四檔 grep 零命中）
- [x] **[存在性] AC-118 — 三終態 Migration-Lite 完整性**（唯一可執行 observer 來源；AI-3「F3 收尾表」各格僅引用本 AC 子項編號，不重複貼 shell 字面）（子項①②③④實測：①零命中 ②零命中 ③排除後零殘留 ④六列 REPAIR_EXHAUSTED 帶兩次未過語義）
  - ①`stop-conditions.md`：表格有效 `Final state` 儲存格不得再有裸 `BLOCKED`。驗證：`` grep -nE '\| *`BLOCKED` *\|' `` 零命中（現行基線 16 處，非子字串計數之 17——差額為 Response contract 範本列，已由既有 AC「`stop-conditions.md` 無 response shape 章節」覆蓋刪除）。
  - ②`SKILL.md`：`Final state:` 同行不得再有裸 `BLOCKED`，須含三終態 `DECISION_REQUIRED | CHANNEL_BLOCKED | REPAIR_EXHAUSTED`。驗證：`grep -nE 'Final state:.*\bBLOCKED\b'` 零命中（現行基線僅 1 處，即 Response shape 範本行；已實測確認不誤判三終態行，因限定在 `Final state:` 同行）。
  - ③`state-transitions.md`：合法內部狀態機節點 `BLOCKED` 可保留；排除合法節點後不得有 write-failure 之舊輸出終態殘留，`STOP_WRITE_FAILED` 對外終態須為 `CHANNEL_BLOCKED`。驗證：`grep -nE '\bBLOCKED\b'` 之結果，以 `grep -v` 排除含「`ANY ACTIVE STATE`」「`MODE_ANALYSIS |`」「`BLOCKED → end run`」「`` | `BLOCKED` | Access ``」之列後，殘留須為零（現行基線排除後殘留 1 行，即 L92；已實測確認即使側狀態圖節點被改名，排除條件改用內容而非行號亦不受影響）。
  - ④`REPAIR_EXHAUSTED`：指定六個 reason code 均須包含先自修／補證及第二次未過才停止之語義。
- [x] **[存在性]** `SKILL.md` 含 response shape 與三終態 `DECISION_REQUIRED`／`CHANNEL_BLOCKED`／`REPAIR_EXHAUSTED`；`stop-conditions.md` 無 response shape 章節；`SKILL.md` ticket 寫入步驟具名平台工具、`project` 參數與 `file` 欄單一慣例（AI-1 probe 結果）。（SKILL.md Response shape 三終態；stop-conditions.md 無 response shape 章節；步驟 17 具名 registry_upsert_ticket／project／file=Pending/<basename>）
- [x] **[可達性]** ticket 模式資格判定所需之檔頭欄位（`Fixture`、`Plan Gate`、`Ticket mode eligibility`、`Gate`、`Verdict`）均有 producer：diag／plan 輸出規格中產生該欄之位置（producer 證據欄：`SKILL.md:125`、`input-contract.md:64`）；且 ticket 模式消費該欄之判定步驟（`SKILL.md:169`、`input-contract.md:118-128`）。
- [x] **[行為性]** given `/ticket-author`（無參數）expect `Gate: STOP`、`Reason code: STOP_INVALID_MODE`、`Final state: DECISION_REQUIRED`（Intake §8 映射補正），未讀完整 TPL、未產 artifact、零寫入 observer 前後一致。（Gate: STOP／STOP_INVALID_MODE／DECISION_REQUIRED；未讀 TPL、未產 artifact、observer 前後一致）
- [x] **[行為性]** given Halo＋`fixture_execution: true` expect `diag` 輸出 `Gate: PASS`、`Verdict ∈ {CONFIRMED, PARTIALLY_CONFIRMED}`、historical 與 current-live 分欄、檔頭 `Fixture: true`；`plan` 輸出 `STOP_GOAL_AMBIGUOUS` 且無正式 Ticket Map。（Halo diag PASS/PARTIALLY_CONFIRMED 且 historical/current-live 分欄、Fixture: true；plan→STOP_GOAL_AMBIGUOUS 無 Ticket Map）
- [x] **[行為性]** given UC＋`fixture_execution: true` expect `plan` 輸出 `STOP_DECISION_REQUIRED`；given UC＋`Fixture decisions:`（省略 `fixture_execution`）expect 輸出 `Input normalization: fixture_execution forced to true…`、`Plan Gate: PASS`、plan-local IDs、檔頭 `Fixture: true` 與 `Ticket mode eligibility: FORBIDDEN`。（UC plan→STOP_DECISION_REQUIRED；附 Fixture decisions→Input normalization、Plan Gate PASS、T01–T03、Fixture: true、FORBIDDEN）
- [x] **[行為性]** given AI-6 ② 之 fixture plan artifact 作 `ticket` parent expect 拒收，無 `Pending/` 落檔、無 Registry 寫入。（ticket 以 fixture plan 為 parent→STOP_REPLAN_REQUIRED，無 Pending/ 落檔、無 Registry 寫入）
- [x] **[行為性]** AI-5／AI-6 全程前後比對：目標專案 `Pending/` 檔案集合、`registry_list(project=SmartCameraApp1)` 之 Ticket IDs、total 與對應 file 欄均不變（禁用 Git 追蹤狀態、全域 revision、全域票數）。（每步前後 Pending/ 16 檔集合與 registry_list(project=SmartCameraApp1) 11 筆 ID／file 皆不變）
- [x] **[存在性]** 冊面 read-back：`TKA-ALIGN-01` lifecycle=`REVIEW`，`checkpoint.live_acceptance_contract.status`=`OPEN`，target 與四項 required_observers 完整。（證據：registry_list read-back lifecycle=REVIEW、file=deliver/、checkpoint.live_acceptance_contract.status=OPEN，target 與四項 required_observers 完整；先搬 deliver/→set_status→set_checkpoint 順序）
- [ ] ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（git diff --name-only 僅 ALLOWED 四檔）

**Live acceptance（closure 條件，非本票執行項）**：本票停 REVIEW；APP 專案內第一張經 `/ticket-author ticket` 完整寫入之真實票（預期 `research_submit` 整合票）完成寫入／read-back／可發現性後方可 CLOSED。dogfood 失敗 → 回本票修復，不轉嫁功能票。

## 收尾實錄（Closeout，2026-10-06）
- **實作 commit**：`9e4f7bf2`（4 檔 +136／-59，僅 ALLOWED）；`DIFF_REF=1a927e09`、`DIFF_END=9e4f7bf2`。
- **QA**：Step 0 Scope Gate PASS；Build／Tests／Lint N/A（PRESET=docs）；PROGRESS.md 已達 80 行上限，Step 4 SKIPPED，Dashboard 同步留待後續 `/compact`。
- **SV／Cross-Review**：Codex R1／R2／R3 皆 `REPORT_FIX`（皆為 sv_report 措辭／格式：AI-6 ③ 宣稱精度；Stage B §7 把文件契約判定寫成 production Guard；§7 機械格式），R4 **PASS**（`Guard Input Reachability: 無新 Guard`、`REACHABILITY: NONE`）。機械輪次 R4。Akira 裁決：`A_REVISED`、`A_REPORT_ONLY_R3_AUTHORIZED`、再一次 A（格式）。四輪均未修改 implementation commit、`SKILL.md` 步驟順序或 `DIFF_END`。
- **誠實揭露**：①AI-6 ③「fixture parent 拒收結果」PASS，「完整程序順序符合性」**PARTIAL**（實跑先分類 parent 並拒收，未先完整讀 Ticket template；未取得符合 `SKILL.md` 步驟 1→2 順序之重跑證據）；②R0 預審（`cross-reviewer`）本輪未執行，依 Akira 裁決為 optional advisory；③`[行為性]` AC 之證據為使用者手動觸發 `/ticket-author` 之實跑與零寫入 observer（無可重播腳本；`.tmp/ticket-author/` artifact 屬短期傳遞，非耐久證據）；④`[可達性]` AC 之 `檔案:行號` 為文件契約實作位置，非 production Guard 可達性。
- **reviews**：qa=PASS（僅 Step 0；Build／Test／Lint N/A）、sv=PASS、codex=PASS（R4）、traycer=N_A（Header `Traycer Workflow: N/A`）。
- **Live acceptance**：依票面停 REVIEW；`live_acceptance_contract.status=OPEN`，待 APP 內第一張經 `/ticket-author ticket` 完整寫入之真實票完成寫入／read-back／可發現性後由審查方 CLOSED。
