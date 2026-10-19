# TKA-BATCH-01: ticket-author 的 ticket mode 接受明列的 `Selected items` 清單，以 shared preflight 加逐項嚴格串行方式建立多張正式票（兩份 Skill 契約同步修訂）

**Revision**: **Rev A.3（2026-10-20；T2 功能審查核定「不得 CLOSED，同票 fix-forward」，Rev／4a 計數＝3）**。Rev A.2（2026-10-19；RGR cycle 1 修補，Rev／4a 計數＝2；報告 `.tmp/rigor-gate/RGR_TKA-BATCH-01_20261019.md`：F1 AI-2 第三處目標值改為「each condition of the preceding sentence applies to each selected item separately…」並明示原子項清單本身不算含獨立可交付成果之請求；F7 AI-7／HC8／Error Handling／coordination 依賴補 Gate `check commit`；F2 走查逐列附 `檔案:行號`；F3 新增 M4a（雙鍵相同即接受）；F4 刪除「same population as step 18.1」不實等同；F5 第 3 步僅 atomicity 逐 item、parent 讀取共用；F6 已知限制②具名觀察物；F8 AC 類型說明措辭更正。「已執行＝`REVIEW`／`CLOSED`」出處為 plan A17）。Rev A.1（2026-10-19；Akira 於 `STOP_DUPLICATE_TICKET` 後裁決選 B，授權就地修補本張尚未開工之 PENDING 票。修補：①決策 4 之 `STOP_ACCESS_BLOCKED` 改依受阻資源判定（parent／Ticket Template／Registry 受阻 → shared；僅單一 item 的 evidence target 受阻 → item-local），並自 AI-5 目標值與狀態矩陣移除 repository／worktree／共用工具之 shared 列舉；②撤回 `STOP_SOURCE_UNAVAILABLE` 為 shared 之字面推導（修正後 A18 未列舉 → 預設 item-local）；③HC4 與已知限制①之 `STOP_REPLAN_REQUIRED` 措辭改為「不改變其語義，僅定義於 selected-item collection 的觸發範圍與 per-item 套用方式」；④BASE_COMMIT／ANCHOR_PROBED 重探（HEAD `cb5a293e`，兩檔 blob 與錨點命中不變）；⑤parent plan 之 A18 同步為本版（僅 Akira 原文，推得部分另標）；⑥完整重審後補：HC6 明示本票自身收尾不在「不得修改既有票」之限、AC2／AI-6① 納入 plan G1、G2、G6–G11、已知限制①④⑤措辭更正與具名。修補後完整重跑 evidence／authoring gate。）

**Rev A.2（2026-10-19）**：Akira 對話裁決與外審紀錄見票末「Decision Addendum」；該節優先於上文同名處，上文其餘不變。

**Rev A.3 差分（逐點；T2 功能審查核定，Akira 2026-10-20）**：
①冊面 `REVIEW→IN_PROGRESS`（`lifecycle` 參數）、`reviews` 重置為 `PENDING`（traycer `N_A`）、票檔自 `deliver/` 退回 `Pending/`（先例：`INFRA-RULESLIM-01` Rev B）；
②Verify Config 重錨：`DIFF_REF`＝`0caf3c7a`、`BASE_COMMIT`＝`0caf3c7adba0ec230c17943eb8d7167ef0f880ac`、`TARGET_BLOB` 增列 `.claude/CLAUDE.md:2e8b0cec`（Rev A.2 值：`DIFF_REF=cb5a293e`、`BASE_COMMIT=cb5a293e…`、`ee799ad9`／`0b40039f`，保留於此作歷史），`ANCHOR_PROBED`＝2026-10-20 01:28；`ALLOWED` 增列 `.claude/CLAUDE.md`（僅 Rule 4e 單句）、`FORBIDDEN` 移除之；
③**F1**：Selected items 的拓撲排序、cycle detection、dependency-contagion 與 dependency STOP 只看 `execution_dependency`；`coordination_dependency` 僅供票務協調參考，不排序、不成環、不傳染 STOP；
④**F2**：execution 前置項已有可追溯正式票（同一 parent artifact＋plan-local item ID，lifecycle `REVIEW`／`CLOSED`）或已於同次 invocation 成功 write／read-back，視為依賴滿足；`STOP_DUPLICATE_TICKET` 不可直接阻塞後項，須先驗證匹配正式票，驗證不到才走既有 dependency STOP；
⑤**F3**：`Selected items` 僅一個 ID 時直接路由至既有 `Selected item` 單項流程，不執行 batch set resolution、不產生 batch progress 或摘要，單數與單一 ID 複數完全等價；
⑥**F5（甲案縮小版）**：`.claude/CLAUDE.md` Rule 4e 僅改一句——單項須提供 Goal；批次（兩個以上 ID）以各 Selected Ticket Map item 的 goal 作為各自 Goal；批次附單一自由文字 `Goal:` → `STOP_DECISION_REQUIRED`，不允許以 invocation 覆寫 Map item goal；
⑦**F4／F6／F7 不修**：列入收尾實錄 Observation，不建立新機制、不擴大 plan-series／parent-drift／telemetry 範圍；
⑧**4a**：Rev A.3 觸發結構歧義停損律①（版次 ≥ 3）；依核定在既有研究授權下作一次窄範圍 PPSS，輸出「續修／拆票／撤回」三案與推薦（推薦續修），記於收尾實錄 §7，不新增 Gate、state、reason code 或 permanent artifact；
⑨新增 AI-8～AI-12、決策 11–15、AC 增補；原 AI-3／AI-5 目標值為 Rev A.2 已落地版本，Rev A.3 以 AI-9／AI-10 對其最小替換，**二者差異以 Rev A.3 為準**；修畢後僅對新增 diff（`0caf3c7a..`）重跑 QA／SV／Codex／cross-review，並以真正 2-item batch 與 duplicate-retry 兩情境做文字級走查，**不得建立正式測試票模擬**。

**簽發日期**: 2026-10-19
**簽發者**: Claude
**來源議題**: TKAuthor（登記縮寫 TKA；`ticket_pattern.md` 已登記 family；冊面 `topic`＝`TKAuthor`）
**Mode**: normal  ||  **Effort**: High
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: TKA-ALIGN-01（三模式契約與 `ticket` 單 item 流程之來源）、TKA-CHANNEL-01（`.claude/CLAUDE.md` Rule 4e 開票通道之來源）——二票皆已 `CLOSED` 並位於 `deliver/archive/TKA/`
- `coordination_dependency`: Autonomous Engineering Gate commit 入口——提交若不經 `/qa` Step 4.5，須先取得 `check commit` receipt（Rev A.2，見 AI-7）；其餘無（2026-10-19 開票時掃描 `Pending/`、`deliver/`、`void/`、主冊與 archive，無在途票編輯 `.claude/skills/ticket-author/`；日後若有，須與本票串行）

**檔案治理／來源說明**：本票為 Plan-backed 單票（plan-local item `T01`，僅留於本來源區）。Parent 類型＝Plan-backed；Parent 絕對路徑＝`D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261019-tka-batch-plan.md`（2026-10-19；`.tmp` 非長期載體，下列資訊已內嵌，本票於該檔消失後仍可獨立閱讀）；Parent artifact 狀態＝`Fixture: false`／`Plan Gate: PASS`／`Ticket mode eligibility: ALLOWED`（本次 ticket 模式第 1 步逐項核對屬實）；Parent read 時間＝2026-10-19 22:54（UTC+8；Rev A.1 重讀，parent plan A18 已同步；原 19:29）。上游 diag：`D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261019-tka-batch-diag.md`（`Fixture: false`／`Gate: PASS`／`Verdict: CONFIRMED`）。

**Selected item 原文（plan-local `T01` goal）**：兩份契約檔同步修訂，使 `/ticket-author ticket` 在 Plan-backed parent 上可接受 `Selected items:` 並逐項串行執行既有單 item 流程；單項輸入行為不變。

**診斷結論（內嵌自 diag）**：現行契約為單 item（`SKILL.md:163`「one atomic plan item … one formal Ticket」、`input-contract.md:111`「One selected Ticket Map item」），且 `input-contract.md:116` 會把含獨立可交付成果之請求判 `STOP_REPLAN_REQUIRED`；逐張開票是合規執行，批次為 rule gap（非未執行）。`.claude/CLAUDE.md` Rule 4e 未限單數，不需修改。

**相關決策（Akira 2026-10-19 裁決，plan A1–A20 與本次 ticket 輸入，全部 `APPROVED`）**：
1. 採方向 C：擴充既有 ticket mode，不新增模式；保留 `Selected item` 單項相容；新增 `Selected items: <comma-separated item IDs>`；僅同一個 eligible Plan-backed parent 的明列 ID；不支援 `all eligible`。
2. shared preflight 僅做一次（fixture、parent eligibility、parent 與 template 完整讀取、集合解析）；每個 item 從 atomicity、live evidence、既有 Gate、duplicate scan、寫入到 read-back 獨立且嚴格串行；禁止並行與 subagent fan-out。
3. 集合內任一 item 的 `evidence_dependency` 指向未執行 sibling → shared preflight `STOP_REPLAN_REQUIRED`、零寫入；`execution_dependency` 指向集合內 sibling 允許（排序與依賴滿足判定見決策 11、12）。
4. `STOP_WRITE_FAILED`、`STOP_GOVERNANCE_UNAVAILABLE` 為 shared STOP，停止尚未開始項目；`STOP_ACCESS_BLOCKED` 依受阻資源判定：parent、Ticket Template 或 Registry 受阻時為 shared STOP；僅單一 item 的 evidence target 受阻時為 item-local STOP。
5. item-local STOP 僅停止該 item，不依賴它的後項繼續；後項的 `execution_dependency` 前置項未滿足（決策 12）時，輸出既有 `STOP_REPLAN_REQUIRED` receipt 與獨立 progress run，不做 evidence、不分配 ID。
6. 清單有重複 ID → shared preflight `STOP_DECISION_REQUIRED`，不做 normalization。
7. 已完成 write／read-back 的票保留不回滾；後票可引用已成功建立的前票，禁止回寫既有 sibling ticket。
8. 每 item 產出獨立既有 PASS／STOP receipt 與 progress run；最後摘要僅為資訊性彙整；不新增 mode、lifecycle、Gate、reason code、ledger 或持久 batch artifact。
9. 不改變既有 `STOP_REPLAN_REQUIRED` 的語義；若批次所需語義無法在既有 stop contract 下表達，STOP 並提出後續修訂需求。
10. 範圍只限兩份 TKA 契約文件；若唯讀盤點發現其他現役 consumer 確實依賴 selected-item 單數語義，STOP 回報，不得擴大修改範圍。（Rev A.3 修訂：範圍增列 `.claude/CLAUDE.md` Rule 4e 單句，見決策 14）
11. **（Rev A.3，F1）** Selected items 的拓撲排序、cycle detection、dependency-contagion 與 dependency STOP 只看 `execution_dependency`；`coordination_dependency` 僅供票務協調參考，不排序、不成環、不傳染 STOP。
12. **（Rev A.3，F2）** execution 前置項若已存在可追溯正式票（同一 parent artifact＋plan-local item ID，lifecycle `REVIEW` 或 `CLOSED`），或已在同次 invocation 成功 write／read-back，視為依賴滿足；`STOP_DUPLICATE_TICKET` 不可直接阻塞後項，必須先驗證匹配正式票，驗證不到才走既有 dependency STOP。
13. **（Rev A.3，F3）** `Selected items` 僅一個 ID 時直接路由至既有 `Selected item` 單項流程；不執行 batch set resolution，不產生 batch progress 或摘要；單數與單一 ID 複數完全等價。
14. **（Rev A.3，F5 甲案縮小版）** `ALLOWED` 加入 `.claude/CLAUDE.md`，只改 Rule 4e 一句：單項要提供 Goal；批次由各 Selected Ticket Map item 的 goal 作為各自 Goal；批次附單一自由文字 `Goal:` → `STOP_DECISION_REQUIRED`；不允許以 invocation 覆寫 Map item goal。
15. **（Rev A.3，F4／F6／F7）** 不修，列入收尾實錄 Observation；不建立新機制、不擴大 plan-series／parent-drift／telemetry 範圍。

**依修正後 A18 推得之兩點（隨票審查）**：①`STOP_SOURCE_UNAVAILABLE` 未列於決策 4 之 shared 列舉，依 item-local STOP 之預設（其餘 STOP 皆 item-local）為 item-local；②`STOP_ACCESS_BLOCKED` 之受阻資源若既非 parent／Ticket Template／Registry、亦非單一 item 的 evidence target（例如 repository／worktree 或多 item 共用的工具），決策未涵蓋，本票不新增判準，依「其餘 STOP 為 item-local」處理（後續每個 item 會各自遇到同一阻礙，見已知限制⑤）。

**由已裁決推得之唯一讀法（plan G1–G11，隨票審查）**：`Selected item: X` ≡ `Selected items: X`；並存且內容不同 → shared `STOP_DECISION_REQUIRED`；ID 不在 Ticket Map → shared `STOP_MISSING_SUBJECT`；「X 在 `execution_dependency`／`coordination_dependency` 具名 Y ⇒ Y 先於 X」之拓撲序（成環 → shared `STOP_REPLAN_REQUIRED`）；Progress runId＝`tka-<UTC timestamp>-<item>`，shared preflight 事件記於第一個 item 之 run；摘要無自己的 `Gate:`／`Final state:` 行；每 item 重做 duplicate 掃描與 live evidence，壓縮後重讀 template 與 parent；`Selected items` 僅限 Plan-backed parent；依賴 STOP 遞移；shared STOP 後未開始者不起 run。

**Consumer 盤點（開票時唯讀，2026-10-19）**：①`.claude/CLAUDE.md` Rule 4e 未限單數；②`.claude/skills/projman/SKILL.md` 僅宣告「不碰 ticket-author」，不依賴 selected-item 語義；③`plans/cso_briefing/project/file/TKAuthor_Protocol.md` 為上游 Desktop 提示詞樣板，其 `ticket` 範本以 `Selected item:` 單項形式書寫，為本票保留之相容形式，未斷言「僅限單項」，本票不修改該檔（新語法之上游樣板同步屬後續另案）；④`D:\DevSources\Smarter-Project\file\Instructions_for_Claude.md` 僅在 L140／L203 提及 `/ticket-author` 之上游產出與通道邊界，不涉 selected-item 單複數；⑤`.claude/hooks`、`.claude/scripts`、`.claude/settings.json`、`ticket-run` 對 `ticket-author`／`Selected item` 零命中。結論：無現役 consumer 依賴單數語義，不觸發 STOP。

**已知限制（非阻擋，如實揭露）**：①`stop-conditions.md` 之 `STOP_REPLAN_REQUIRED` 觸發描述與 `state-transitions.md` 之 `ticket` 路徑仍以單 item 敘述（依決策 10 之範圍禁止修改）；本票不改變 `STOP_REPLAN_REQUIRED` 之語義，僅在 `SKILL.md`／`input-contract.md` 定義其於 selected-item collection 的觸發範圍（集合不合格、依賴 STOP）與 per-item 套用方式，係依 Akira 決策 3、5 使用既有 code：code 與 Final state（`DECISION_REQUIRED`）不變，`Decision or input needed` 依觸發原因而異（移出集合、或解決前項 STOP 後另行重跑），與 `SKILL.md` 既有 parent-problem 路由表的做法一致（例如 Fixture 列為「Supply a non-fixture real parent」，並非一律回 plan）；`stop-conditions.md`／`state-transitions.md` 之單 item 敘述為有意不動，非矛盾；②本票無法以真實多 item 開票驗證模型遵循度（會建立正式票），驗證止於文字層（存在性＋狀態矩陣逐列走查）；首次真實 `Selected items` 使用為後續觀察，不構成本票驗證——觀察物為該次呼叫之逐 item receipt 與資訊性摘要，對照本票狀態矩陣核對（Rev A.2 具名承接）；零寫入之 shared STOP 路徑（M4–M10）技術上可真實驅動，本票未納入；③`TKAuthor_Protocol.md` 之 Desktop 提示詞樣板未含 `Selected items`（見 consumer 盤點③）；④唯一未照字面沿用 parent AC 之處：plan `acceptance_intent` #5 原標 `[行為性]` 文字走查，本票改標 `[存在性]`。原因：Template 不允許高報 AC 驗證等級（文字走查非 production 路徑驅動之測試）；實質：走查列數為 M1–M10（含 M4a）、E1–E9，多於 parent 的三情境，內容擴大而非縮減；決定權：Akira 可推翻此標記；⑤（預設推導、非裁決，列為待確認點）`STOP_ACCESS_BLOCKED` 之受阻資源既非 parent／Ticket Template／Registry、亦非單一 item 的 evidence target（如 repository／worktree 整體不可用）時，決策未涵蓋，依「其餘 STOP 為 item-local」處理，後續每個 item 將各自遇到同一阻礙並逐一 STOP；是否需新增 shared 判準留待後續修訂。

## Verify Config
TICKET_ID=TKA-BATCH-01
CREATED=2026-10-19
PRESET=docs
ALLOWED=.claude/skills/ticket-author/SKILL.md,.claude/skills/ticket-author/references/input-contract.md,.claude/CLAUDE.md
FORBIDDEN=.claude/skills/ticket-author/references/state-transitions.md,.claude/skills/ticket-author/references/stop-conditions.md,plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md,plans/cso_briefing/project/file/TKAuthor_Protocol.md,plans/cso_briefing/project/TICKET_REGISTRY.json,plans/cso_briefing/project/TICKET_REGISTRY_ARCHIVE.json,.claude/rules/**,.claude/PROGRESS.md,.claude/PROGRESS_ARCHIVE.md
MAX_LINES=100
DIFF_REF=0caf3c7a
ANCHOR_PROBED=2026-10-20 01:28
BASE_COMMIT=0caf3c7adba0ec230c17943eb8d7167ef0f880ac
TARGET_BLOB=.claude/skills/ticket-author/SKILL.md:7ec966aa;.claude/skills/ticket-author/references/input-contract.md:3248b6c0;.claude/CLAUDE.md:2e8b0cec
INTRODUCED_CAPABILITY=feature:ticket-selected-items
DEPENDENCY_DELTA=NONE

**FORBIDDEN 與管線觸碰面之處置（`cross-review.md` R0 Pre-flight）**：`.claude/PROGRESS.md`／`.claude/PROGRESS_ARCHIVE.md` 為 `/qa` Step 4／4.5 之標準觸碰面，本票將其列入 FORBIDDEN，處置條款 **`DEFERRED-TO-DOCSYNC`**：Dashboard 同步、`PROGRESS_ARCHIVE.md` Done 標記、票檔 Pending→deliver 搬移與票內 checkbox 勾選依標準 SOP 於下一輪 `/compact` 或後續 session 之正常收尾處理，**MUST NOT** 另開 DOCSYNC 票（`ssot-governance.md`「FORBIDDEN 延後項免開票紀律」）；其增量同步屬 Pipeline Artifact，不計為本票違規。`MAX_LINES` 為估算校準遙測（非 gate）：兩檔新增約 55 行、替換 4 行，取 100。`INTRODUCED_CAPABILITY` 依 fail-closed 顯式填寫：本票為既有 skill 新增一項輸入形式能力，不填 `NONE`。

## Action Items

### AI-0: 錨點與基線復驗（派工後、修改前）
- **檔案**: `.claude/skills/ticket-author/SKILL.md`、`.claude/skills/ticket-author/references/input-contract.md`
- **動作**: 逐字確認下列「現值」於各檔命中數為 1（`grep -cF`）：
  - `input-contract.md`：`- Selected Ticket Map item with a traceable parent source.`（L21）；`- One selected Ticket Map item or equivalently atomic objective.`（L111）；`- One observable Ticket goal.`（L113）；`or the selected item still has multiple goals.`（L116 行尾）；`## Structured option`（L153）
  - `SKILL.md`：`Purpose: collect complete live evidence for one atomic plan item and create one formal Ticket only after both evidence and authoring gates pass.`（L163）；`### Progress contract`（L193）
- **基線**: `git diff --name-only cb5a293e656ffebb24f80ef5020d1ab274dd29bd..HEAD -- <ALLOWED 兩檔>`（依 Template 基線漂移三分流分類，`ANCHOR_STALE_SCOPE_INTACT`／`CANDIDATE_SUPERSEDED` 僅資訊性、記錄後以現值續作；`ALREADY_EXECUTED_PENDING_CLOSURE` 停止）；記錄兩檔 `git hash-object` 與 `TARGET_BLOB` 之比對。
- **併做**: ①`git check-ignore -v` 對兩個 ALLOWED 檔須無命中（命中 → 停止）；②重掃 `Pending/` 與冊面確認無他票編輯 `.claude/skills/ticket-author/`（有 → 停止，須串行）；③記錄 `/mdlint` 對兩檔之 baseline warning 數；④重跑 consumer 盤點關鍵 grep（`grep -rn "Selected item\|ticket-author" .claude plans/cso_briefing/project/file`），若出現本票來源區未列、且斷言「僅限單項」之 consumer → 停止回報，不得擴大修改範圍。
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [x] 完成（見收尾實錄 §1）

### AI-1: `input-contract.md` Common fields 的主體識別行納入 `Selected items`
- **檔案**: `.claude/skills/ticket-author/references/input-contract.md`
- **現值**: `- Selected Ticket Map item with a traceable parent source.`
- **目標值**: `- Selected Ticket Map item (or, for a Plan-backed parent, an explicit list of items) with a traceable parent source.`
- [x] 完成（見收尾實錄 §2）

### AI-2: `input-contract.md` `## ticket` Required 與停止條件納入清單形式並明示逐 item 套用
- **檔案**: `.claude/skills/ticket-author/references/input-contract.md`
- **現值（三處，逐處替換）**:
  - `- One selected Ticket Map item or equivalently atomic objective.`
  - `- One observable Ticket goal.`
  - `or the selected item still has multiple goals.`（L116 行尾；此處為句尾，替換時保留前文）
- **目標值**:
  - ``- One selected Ticket Map item or equivalently atomic objective (`Selected item: <ID>`); or, for a Plan-backed parent only, an explicit comma-separated list of Ticket Map item IDs (`Selected items: <ID>, <ID>, ...`; see Selected items (batch form) below).``
  - ``- One observable Ticket goal (in the `Selected items` form, one per selected item, as stated in its Ticket Map item).``
  - ``or the selected item still has multiple goals. In the `Selected items` form each condition of the preceding sentence applies to each selected item separately, never to the list as a whole; a list of atomic items is not by itself a request with independently deployable outcomes.``（Rev A.2：指涉範圍明確為該句全部條件，含首子句「independently deployable outcomes」；AC4 片語保留）
- [x] 完成（見收尾實錄 §2）

### AI-3: `input-contract.md` 新增 `### Selected items (batch form)` 小節（集合解析與輸入規則）
- **檔案**: `.claude/skills/ticket-author/references/input-contract.md`
- **現值（插入錨點）**: `## Structured option`（唯一；新小節插入於其前一個空行之後，即既有 `ticket` 範例程式碼區塊之後）
- **目標值（逐字插入；其後保留一空行再接 `## Structured option`）**:

````markdown
### Selected items (batch form)

`Selected items: <ID>, <ID>, ...` is an additive input form of `ticket` mode for one Plan-backed parent. `Selected item: <ID>` keeps its existing single-item behavior, and `Selected items: <ID>` with one ID is identical to it. The form adds no mode, lifecycle, Gate, reason code, ledger, or persistent batch artifact.

- Only item IDs of the Ticket Map of one Plan-backed parent are accepted. A Diag-backed or Spec-backed parent with `Selected items` returns `STOP_REPLAN_REQUIRED`. `all eligible` and any wildcard are not supported; a token that is not an item ID fails the Ticket Map check below.
- Set resolution belongs to the shared preflight and runs after parent classification and the full parent read. Every failure below is a shared STOP: zero formal writes, no Ticket ID, no `Pending/` write, no Registry write, and no item started.

| Condition | Reason code |
| --- | --- |
| `Selected item` and `Selected items` both given with different content | `STOP_DECISION_REQUIRED` |
| The list contains a duplicate ID (no normalization; the user corrects the list) | `STOP_DECISION_REQUIRED` |
| A token is not an item of the parent Ticket Map | `STOP_MISSING_SUBJECT` |
| A selected item's `evidence_dependency` names a plan-local item that has not been executed (no formal Ticket for this parent and that item at lifecycle `REVIEW` or `CLOSED`; determined over the Registry main and archive, `Pending/`, `deliver/` including `deliver/archive/`, and `void/`, matching each Ticket's embedded source section by parent path and plan-local item ID), whether or not that item is in the list | `STOP_REPLAN_REQUIRED` (name the item and the sibling; move the item out of the list, or run it after the sibling is executed) |
| The `execution_dependency` / `coordination_dependency` references among the selected items form a cycle | `STOP_REPLAN_REQUIRED` |

- `Selected item` and `Selected items` given together with identical content are accepted as one list (no STOP).
- An `execution_dependency` that names another selected item is allowed. If an item names another selected item in its `execution_dependency` or `coordination_dependency`, the named item is processed first (topological order; ties keep the Ticket Map order). A reference to an item that is not selected behaves as in the single-item form.
- Each selected item's goal is the one stated in its Ticket Map item. Each item's Ticket embeds its own plan-local item ID and the parent read timestamp of the shared preflight.
- Execution (serial order, STOP routing, progress, receipts) is defined in `SKILL.md`, Mode: ticket, "Selected items (batch form)".

Example:

```text
/ticket-author ticket

Source: @.tmp/ticket-author/<YYYYMMDD-slug>-plan.md
Selected items: T01, T03
Collect live evidence for each item in turn; create each Ticket only after both of its gates pass.
```
````
- [x] 完成（見收尾實錄 §2）

### AI-4: `SKILL.md` ticket Purpose 指向批次形式
- **檔案**: `.claude/skills/ticket-author/SKILL.md`
- **現值**: `Purpose: collect complete live evidence for one atomic plan item and create one formal Ticket only after both evidence and authoring gates pass.`
- **目標值**: `Purpose: collect complete live evidence for one atomic plan item and create one formal Ticket only after both evidence and authoring gates pass. For a Plan-backed parent, several explicitly listed items may be handled in one call as separate serial single-item runs (see Selected items (batch form) below).`
- [x] 完成（見收尾實錄 §2）

### AI-5: `SKILL.md` 新增 `### Selected items (batch form)` 小節（執行、STOP 路由、progress、receipt）
- **檔案**: `.claude/skills/ticket-author/SKILL.md`
- **現值（插入錨點）**: `### Progress contract`（唯一；新小節插入於 Procedure 第 18 步結尾句 `A mismatch after the minimal safe retry → `STOP_WRITE_FAILED`.` 之後、此標題之前，各以一空行分隔）
- **目標值（逐字插入）**: 〔Decision Addendum A.2 第 6 點修訂下列第 3 點之起首與第 6 點之文字，優先於此目標值〕

````markdown
### Selected items (batch form)

`ticket` also accepts `Selected items: <ID>, <ID>, ...` for one Plan-backed parent (syntax and set resolution: `input-contract.md`). It adds no mode, lifecycle, Gate, reason code, ledger, or persistent batch artifact. `Selected item: <ID>` runs exactly as described above.

1. Shared preflight, once: steps 1 and 2, the full parent read of step 3, and set resolution. A STOP here is a shared STOP: zero formal writes and no item started.
2. Strict serial execution: each selected item then runs step 3's atomicity validation (the parent read is shared; re-read it only after compaction or resume, as below) through step 18.6 as its own single-item run, in the resolved order. Item N+1 starts only after item N has passed 18.6 or recorded its STOP receipt. Never run items in parallel and never fan out to subagents. Steps 4 and 18.1 and all live evidence are redone for every item and never reused from an earlier item, so a later item sees every Ticket the batch already wrote. After context compaction or resume, and before the next item, re-read the template and the parent.
3. STOP routing, using only the existing reason codes and their Final states:
   - Shared STOP — stop every item not yet started: any STOP in the shared preflight; `STOP_WRITE_FAILED`; `STOP_GOVERNANCE_UNAVAILABLE`; `STOP_ACCESS_BLOCKED` when the blocked resource is the parent, the Ticket template, or the Registry.
   - Item-local STOP — record that item's STOP receipt and continue with the next item that does not depend on it: every other STOP, including `STOP_ACCESS_BLOCKED` on any resource other than the parent, the Ticket template, or the Registry (for example when only that item's own evidence target is blocked).
   - Dependency STOP — an item whose `execution_dependency` or `coordination_dependency` names a selected item that STOPped, or that was itself dependency-blocked, is not run: no live evidence, no Ticket ID, no write. It outputs the existing `STOP_REPLAN_REQUIRED` stop response (`Decision or input needed`: resolve the named item's STOP, then rerun this item in a later call) and has its own progress run.
4. Completed Tickets stay: a Ticket whose write and read-back passed is never rolled back. A later item may reference an already created sibling by its formal Ticket ID; it MUST NOT edit any Ticket file or Registry entry that an earlier item wrote.
5. Progress: every item that starts has its own run with `runId: tka-<UTC timestamp>-<item>` and the events of the Progress contract below. Shared preflight events are reported on the first item's run (a shared preflight STOP is `run.failed` on that run, and no other run starts); a later item reports `templates-loaded` as reused from the shared preflight. Items not started after a shared STOP have no run.
6. Receipts: every item that started yields its own existing completion or stop response. After the last one, an informational summary lists each selected item as its Ticket ID and path, its stop code, or `not attempted` with the reason. The summary is not a Gate, state, or reason code: it carries no `Gate:` or `Final state:` line, is never written to a file, and cannot claim an overall PASS.
````
- [x] 完成（見收尾實錄 §2）

### AI-6: 交叉一致性檢核與執行證據（不改檔案）
- **動作**: 對兩份已修訂檔執行下列檢核並保存輸出於收尾實錄：①決策 1–10（涵蓋 plan A1–A20）與 plan G1、G2、G6–G11 逐條對照表（每條指出所在檔案與標題／行號）；②token 封閉性：`git diff -U0 -- <ALLOWED 兩檔> | grep '^+' | grep -oE 'STOP_[A-Z_]+' | sort -u` 之每一項皆出現於 `stop-conditions.md`；③`all eligible` 僅出現於「不支援」敘述；④新增行不含 `btplan`／`btticket`，且三模式程式碼區塊（`/ticket-author diag`／`plan`／`ticket`）未被改動；⑤下表「狀態矩陣」逐列走查：條文對每列皆唯一指向預期處置，**每列附最終兩檔之 `檔案:行號`（first-hand），無行號之列不計為證據**（Rev A.2；矩陣「落點」欄之 AI 編號僅為撰寫索引，不是證據）；⑥`/mdlint <各檔>` warning 數不高於 AI-0 baseline；⑦`git diff --name-only` 僅含 ALLOWED 兩檔。
- [x] 完成（見收尾實錄 §3）

### AI-7: 收尾實錄與保存（依既有流程）
- **動作**: 以 `[TKA-BATCH-01]` 單票前綴、明確 pathspec 僅提交 ALLOWED 兩檔；**提交若不經 `/qa` Step 4.5（例如依 SOP 風險分級省略 QA），提交前先執行 `python .claude/scripts/gate_ledger.py check commit --ticket TKA-BATCH-01`，exit 0 才提交**（Rev A.2；否則 Stop guard 會依本票前綴 commit 判為漏檢並阻擋——`gate_stop_guard.py:265-275, 293-310`；退出碼處置依 `ssot-governance.md` §入口與覆蓋矩陣之映射）；其餘收尾（QA／SV／Cross-Review 是否適用、Dashboard、Pending→deliver、冊面轉態）依既有流程與 `DEFERRED-TO-DOCSYNC` 條款處理，本票不在此重列。
- [x] 完成（commit `82a14d79`，`gate_ledger.py check qa`／`check commit` 皆 exit 0；經 /qa Step 4.5，Step 4 PROGRESS 依 FORBIDDEN 命中 SKIPPED — DEFERRED-TO-DOCSYNC）

### AI-8: Rev A.3 錨點與基線復驗（派工後、修改前）
- **檔案**: `.claude/skills/ticket-author/SKILL.md`、`.claude/skills/ticket-author/references/input-contract.md`、`.claude/CLAUDE.md`
- **動作**: 逐字確認 AI-9／AI-10／AI-11 之「現值」於各檔命中數為 1（`grep -cF`；`SKILL.md` Dependency STOP 以行首 `   - Dependency STOP — ` 計為 1）；`git diff --name-only 0caf3c7adba0ec230c17943eb8d7167ef0f880ac..HEAD -- <ALLOWED 三檔>` 分類（同 AI-0）；三檔 `git hash-object` 與 `TARGET_BLOB` 比對；`git check-ignore -v` 三檔無命中；`Pending/` 與冊面無他票編輯 `.claude/skills/ticket-author/` 或 `.claude/CLAUDE.md`；記錄 `.claude/CLAUDE.md` 行數（基線 143）。
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [x] 完成（Rev A.3，見收尾實錄 §6）

### AI-9: `input-contract.md` Rev A.3 替換（R1–R5；對 Rev A.2 已落地版本之最小替換）
- **檔案**: `.claude/skills/ticket-author/references/input-contract.md`
- **現值 → 目標值（逐處替換；現值字串於檔內各命中 1）**:
  - R1（Required 之 goal 行）：``- One observable Ticket goal (in the `Selected items` form, one per selected item, as stated in its Ticket Map item).`` → ``- One observable Ticket goal (single item: provided by the invocation; in the `Selected items` form of two or more IDs: one per selected item, as stated in its Ticket Map item).``
  - R2（小節首段，F3）：```Selected items: <ID>, <ID>, ...` is an additive input form of `ticket` mode for one Plan-backed parent. `Selected item: <ID>` keeps its existing single-item behavior, and `Selected items: <ID>` with one ID is identical to it. The form adds no mode, lifecycle, Gate, reason code, ledger, or persistent batch artifact.`` → ```Selected items: <ID>, <ID>, ...` is an additive input form of `ticket` mode for one Plan-backed parent. `Selected items: <ID>` with exactly one ID is routed to the existing `Selected item: <ID>` single-item flow: no batch set resolution, no batch progress, and no batch summary, so the two forms are fully equivalent. The batch rules below apply only to a list of two or more IDs. The form adds no mode, lifecycle, Gate, reason code, ledger, or persistent batch artifact.``
  - R3（表之 cycle 列，F1；並新增 Goal 列，F5）：``| The `execution_dependency` / `coordination_dependency` references among the selected items form a cycle | `STOP_REPLAN_REQUIRED` |`` → 兩列（以換行分隔）：``| The `execution_dependency` references among the selected items form a cycle | `STOP_REPLAN_REQUIRED` | ⏎ | A free-text `Goal:` is supplied together with a list of two or more IDs (an invocation must not override a Ticket Map item's goal) | `STOP_DECISION_REQUIRED` |``
  - R4（排序句，F1）：``- An `execution_dependency` that names another selected item is allowed. If an item names another selected item in its `execution_dependency` or `coordination_dependency`, the named item is processed first (topological order; ties keep the Ticket Map order). A reference to an item that is not selected behaves as in the single-item form.`` → ``- An `execution_dependency` that names another selected item is allowed. If an item names another selected item in its `execution_dependency`, the named item is processed first (topological order; ties keep the Ticket Map order). `coordination_dependency` is informational only: it does not order items, does not form cycles, and does not propagate a STOP. A reference to an item that is not selected behaves as in the single-item form. Whether an `execution_dependency` is satisfied is defined in `SKILL.md`, Mode: ticket, "Selected items (batch form)", Dependency STOP.``
  - R5（goal 句，F5）：``- Each selected item's goal is the one stated in its Ticket Map item. Each item's Ticket embeds its own plan-local item ID and the parent read timestamp of the shared preflight.`` → ``- Each selected item's goal is the one stated in its Ticket Map item; an invocation-level `Goal:` is not accepted for a list of two or more IDs (see the table above). Each item's Ticket embeds its own plan-local item ID and the parent read timestamp of the shared preflight.``
- [x] 完成（Rev A.3，見收尾實錄 §6）

### AI-10: `SKILL.md` Rev A.3 替換（R1、R2）
- **檔案**: `.claude/skills/ticket-author/SKILL.md`
- **現值 → 目標值**:
  - R1（小節首段，F3）：```ticket` also accepts `Selected items: <ID>, <ID>, ...` for one Plan-backed parent (syntax and set resolution: `input-contract.md`). It adds no mode, lifecycle, Gate, reason code, ledger, or persistent batch artifact. `Selected item: <ID>` runs exactly as described above.`` → ```ticket` also accepts `Selected items: <ID>, <ID>, ...` for one Plan-backed parent (syntax and set resolution: `input-contract.md`). It adds no mode, lifecycle, Gate, reason code, ledger, or persistent batch artifact. `Selected item: <ID>` and `Selected items: <ID>` with exactly one ID run exactly as described above, with no batch rules, batch progress, or batch summary; the rules below apply to a list of two or more IDs.``
  - R2（Dependency STOP 子項，F1／F2）：現值＝行首 `   - Dependency STOP — ` 之整行（Rev A.2 已落地：「an item whose `execution_dependency` or `coordination_dependency` names a selected item that STOPped, or that was itself dependency-blocked, is not run: …」）→ 目標值整行：``- Dependency STOP — an item whose `execution_dependency` names a selected item that is not met is dependency-blocked and is not run: no live evidence, no Ticket ID, no write. A named item is met when it passed 18.6 earlier in this invocation, or when it already has a traceable formal Ticket (same parent artifact and plan-local item ID in the Ticket's embedded source section) at lifecycle `REVIEW` or `CLOSED`, verified over the step 18.1 population (Registry main and archive, `Pending/`, `deliver/` including `deliver/archive/`, `void/`); a later item references a met Ticket by its formal Ticket ID. A `STOP_DUPLICATE_TICKET` raised by the named item never blocks the dependent item by itself: verify the matching Ticket first, and only if none is found is the dependent item dependency-blocked. A named item that STOPped for any other reason, or was itself dependency-blocked, is not met unless such a Ticket exists. A dependency-blocked item outputs the existing `STOP_REPLAN_REQUIRED` stop response (`Decision or input needed`: resolve the named item's STOP, then rerun this item in a later call) and has its own progress run. `coordination_dependency` is informational only: it neither orders items nor propagates a STOP.``
- [x] 完成（Rev A.3，見收尾實錄 §6）

### AI-11: `.claude/CLAUDE.md` Rule 4e 單句（F5）
- **檔案**: `.claude/CLAUDE.md`（L77，Rule 4e 之單一子句；其餘一字不動）
- **現值**: ```ticket` 必須引用 plan artifact、Selected item 與 Goal。``
- **目標值**: ```ticket` 必須引用 plan artifact 與 Selected item（單項須提供 Goal；Selected items 兩個以上時，以各 Selected Ticket Map item 的 goal 作為各自 Goal，附單一自由文字 Goal 則 `STOP_DECISION_REQUIRED`）。``
- [x] 完成（Rev A.3，見收尾實錄 §6）

### AI-12: Rev A.3 檢核、走查、Observation、4a 紀錄與增量重跑
- **動作**: ①三檔 token 封閉性與 diff 範圍（同 AI-6②⑦，改以 `0caf3c7a..` 增量與 ALLOWED 三檔）；②`grep` 逐處核對 `coordination_dependency` 無排序／成環／STOP 殘留；③`git diff -U0 -- .claude/CLAUDE.md` 恰一 hunk、行數 ≤ 150；④狀態矩陣新增／改動列逐列走查（first-hand `檔案:行號`），其中**真正 2-item batch**（A、B，B 的 `execution_dependency`＝A）與 **duplicate-retry**（A 觸發 `STOP_DUPLICATE_TICKET` 且已有 `REVIEW`／`CLOSED` 匹配票）兩情境完整走查；⑤收尾實錄新增 Observation（F4 plan-series、F6 parent-drift、F7 telemetry）與 4a 三案紀錄；⑥僅對新增 diff 重跑 `/qa`、`/sv`、Codex cross-review（DIFF_REF＝`0caf3c7a`），不重審 Rev A.2 已審範圍。**不得建立正式測試票模擬。**
- [x] 完成（Rev A.3，見收尾實錄 §10；增量 QA／SV／R0／Codex 已跑，2-item batch 與 duplicate-retry 走查見 §8）

## Hard Constraints
1. 僅修改 ALLOWED 三檔（Rev A.3 增列 `.claude/CLAUDE.md`，限 Rule 4e 單句，見 HC9）；不得修改 `state-transitions.md`、`stop-conditions.md`、Template、`TKAuthor_Protocol.md`、Registry 檔、`.claude/rules/`。
2. 不得新增 mode（含 `btplan`／`btticket`）、lifecycle、Gate、reason code、ledger、state 名稱或持久 batch artifact；新增文字只能引用 `stop-conditions.md` 已存在之 `STOP_*`。
3. `Selected item:` 單項路徑之既有文字行為不得改變：Procedure 第 1–18 步、Progress 事件表、Evidence／Authoring gate 條款、Response shape 均不編輯；批次規則只以新增小節與 AI-1／AI-2／AI-4 之最小替換表達。
4. 不得改變既有 `STOP_REPLAN_REQUIRED` 之語義，亦不得新增 reason code；本票只定義其在 selected-item collection 的觸發範圍（集合內 `evidence_dependency` 指向未執行 sibling、集合成環、依賴 STOP）與 per-item 套用方式。決策 3 與決策 5 已由 Akira 明示裁決以既有 `STOP_REPLAN_REQUIRED` 承載上述情境，其 `Decision or input needed` 文字（移出集合、或解決前項 STOP 後另行重跑）依該裁決書寫——與 `SKILL.md` 既有 parent-problem 路由表依觸發原因給不同 `Decision or input needed` 的做法一致——**不屬本條停止範圍**；本條僅適用於撰寫或走查中**新發現**、不在決策 1–10 內之其他批次情境（無法於既有 stop contract 表達 → 停止並提出後續修訂）。
5. 不得引入 `all eligible` 或任何萬用字元語法；不得引入靜默 normalization。
6. 不得以本票為由實際建立任何多 item 正式票或使用 `Selected items` 開票；不得修改本票以外之既有 Ticket 檔或冊面條目（本票自身之勾選、收尾實錄與冊面轉態依既有流程不在此限）。
7. 契約文字使用英文且風格同既有檔案（`MUST`／`MUST NOT` 用法一致）；Markdown 通過 `/mdlint`。
8. 本票提交若不經 `/qa` Step 4.5，提交前須先通過 `gate_ledger.py check commit --ticket TKA-BATCH-01`（Rev A.2）。
9. （Rev A.3）`.claude/CLAUDE.md` 僅限 Rule 4e 一句（`ticket` 必須引用…之子句）改寫；其餘一字不動，檔案行數仍 ≤ 150，`git diff -U0` 於該檔僅一個 hunk。
10. （Rev A.3）不得為落實 F1～F5 而新增 mode、lifecycle、Gate、state、reason code、ledger 或持久 artifact；F4／F6／F7 不處理。

## Error Handling
- AI-0 任一錨點命中數 ≠ 1，或 `check-ignore` 對 ALLOWED 檔命中 → 停止回報
- AI-0 發現他票同時編輯 `.claude/skills/ticket-author/`，或出現斷言「僅限單項」之現役 consumer → 停止回報，不得擴大修改範圍
- 撰寫或走查中發現**決策 1–10 未涵蓋**之批次情境，無法以既有 stop contract 表達而不改變 `STOP_REPLAN_REQUIRED` 之語義（或需新增 reason code／state）→ 停止回報，附該情境並提出後續修訂需求（決策 3、5 已裁決之 `STOP_REPLAN_REQUIRED` 用法不在此列）
- 完成修改需動到 ALLOWED 以外任何檔案 → 停止回報
- AI-7 提交前之 `gate_ledger.py check commit` 非 0 → 依 `ssot-governance.md` §入口與覆蓋矩陣之退出碼映射處置，**不提交**（Rev A.2）
- 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中（標的被 .gitignore 排除）→ 停止回報（gitignore 死結律，候選 #20）

## 狀態矩陣（涵蓋本票可達且與契約相關之組合；開票時建立）

**輸入形式與集合解析（shared preflight；全為零寫入）**：

| # | 輸入 | 預期 | 落點 |
|---|---|---|---|
| M1 | 僅 `Selected item: X` | 單項流程，行為不變 | AI-2／AI-5（「runs exactly as described above」） |
| M2 | `Selected items: X`（單一 ID） | （Rev A.3）路由至既有單項流程；不執行 batch set resolution，不產生 batch progress 或摘要 | AI-9 R2 |
| M3 | `Selected items: X, Y`，Plan-backed，無依賴 | 通過解析，依 Ticket Map 順序處理 | AI-3 |
| M4 | 兩形式並存且內容不同 | shared `STOP_DECISION_REQUIRED` | AI-3 表 |
| M4a | 兩形式並存且內容相同（Rev A.2） | 接受為同一清單，不 STOP | AI-3（identical content 句） |
| M5 | 清單含重複 ID | shared `STOP_DECISION_REQUIRED`，不 normalization | AI-3 表 |
| M6 | 含非 Ticket Map 之 token（含 `all eligible`） | shared `STOP_MISSING_SUBJECT` | AI-3 |
| M7 | Diag-backed／Spec-backed／Fixture parent 搭配 `Selected items` | shared `STOP_REPLAN_REQUIRED` | AI-3；Fixture 由既有第 1 步 |
| M8 | 某 item `evidence_dependency` 具名未執行 sibling（集合內或外） | shared `STOP_REPLAN_REQUIRED` | AI-3 表 |
| M9 | `execution_dependency` 具名集合內 sibling，其 evidence 無此 sibling | 允許；被具名者先處理 | AI-3 |
| M10 | `execution_dependency` 成環（Rev A.3：僅 execution） | shared `STOP_REPLAN_REQUIRED` | AI-9 R3 |
| M10a | 僅 `coordination_dependency` 具名（含互指） | （Rev A.3）忽略：不排序、不成環、不 STOP | AI-9 R4 |
| M11 | 兩個以上 ID 且附單一自由文字 `Goal:` | （Rev A.3）shared `STOP_DECISION_REQUIRED`，不覆寫 Map item goal | AI-9 R3／R5 |

**逐 item 執行結果（單 item 流程之後）**：

| # | 情境 | 預期 | 落點 |
|---|---|---|---|
| E1 | item 通過 18.6 | PASS receipt＋獨立 run；已寫票保留 | AI-5 第 2、4、5、6 點 |
| E2 | item 的 gate 以 `DECISION_REQUIRED`／`REPAIR_EXHAUSTED` 類 STOP | item-local：記 STOP receipt，繼續不依賴它的下一項 | AI-5 第 3 點 |
| E3 | 後項在 `execution_dependency` 具名 E2 的 item（或遞移），且該 item 無匹配 `REVIEW`／`CLOSED` 正式票 | dependency STOP：`STOP_REPLAN_REQUIRED` receipt＋run，不做 evidence、不分配 ID（Rev A.3：僅 execution） | AI-10 R2 |
| E3a | 後項僅在 `coordination_dependency` 具名 E2 的 item | （Rev A.3）不受影響，照常處理 | AI-10 R2 |
| E10 | duplicate-retry：前置項 A 觸發 `STOP_DUPLICATE_TICKET`，且已有同 parent＋同 plan-local item 之 `REVIEW`／`CLOSED` 正式票；後項 B `execution_dependency`＝A | （Rev A.3）A 記 item-local STOP；B 視依賴滿足，照常處理並引用該票正式 ID | AI-10 R2 |
| E11 | 前置項 A 觸發 `STOP_DUPLICATE_TICKET`，但驗證不到匹配的 `REVIEW`／`CLOSED` 正式票（例如僅 `PENDING`） | （Rev A.3）B dependency STOP | AI-10 R2 |
| E12 | 前置項 A 於同次 invocation 通過 18.6 | （Rev A.3）B 視依賴滿足，引用 A 的正式 ID | AI-10 R2 |
| E4 | `STOP_WRITE_FAILED`／`STOP_GOVERNANCE_UNAVAILABLE` | shared：未開始者 `not attempted`、不起 run；已完成者保留 | AI-5 第 3、4、5、6 點 |
| E5 | `STOP_ACCESS_BLOCKED`，被擋者為 parent／Ticket Template／Registry | shared | AI-5 第 3 點 |
| E6 | `STOP_ACCESS_BLOCKED`，被擋者為上述以外之資源（例如僅該 item 的 evidence target）；另 `STOP_SOURCE_UNAVAILABLE` 等其餘 STOP | item-local，繼續下一獨立項 | AI-5 第 3 點 |
| E7 | 後項欲編輯前項已寫的票檔或冊面條目 | 禁止；僅可引用前票正式 ID | AI-5 第 4 點 |
| E8 | 壓縮／續行發生於 item 之間 | 下一 item 前重讀 template 與 parent | AI-5 第 2 點 |
| E9 | 全部結束 | 逐 item 回應＋資訊性摘要（無 `Gate:`／`Final state:`） | AI-5 第 6 點 |

## Acceptance Criteria

> **Rev A.2 AC 修訂閉環（RGR cycle 1，2026-10-19）**：狀態矩陣走查 AC 增加 M4a，並要求逐列附最終兩檔 `檔案:行號`（first-hand），只收緊證據來源、不改預期。**兄弟 AC 重驗**：決策對照表 AC（AI-6①，已要求標題或行號）母體相同、判準不變；L116 字面 AC 的片語 `applies to each selected item separately, never to the list as a whole` 仍在 AI-2 第三處目標值內（已 `grep -cF` 核對）；token 封閉性 AC 不受影響（新增文字未引入新的 `STOP_*`）；`all eligible` AC 不受影響。沒有 AC 被移除、縮減或豁免。

> **AC 類型說明**：本票為 docs preset 之 Skill 契約文字，不含 `app/src/main/` 之 Guard／gate／early-return／外生 runtime state，故 `[可達性]`、`[行為性]`、`[實機]` 不適用；批次行為於 runtime 由模型遵循；寫入路徑無法在不建立正式票（有寫入副作用）下以 production 路徑驅動，亦不得以合成 parent 充當核准（Fixture 規則）；零寫入之 shared STOP 路徑技術上可驅動，本票未納入（Rev A.2 措辭更正，見已知限制②）；故不偽稱 `[行為性]`，驗收止於 `[存在性]` 與文字走查（已知限制②）。

- [x] **[存在性]** 兩檔皆含 `Selected items:` 與 `Selected item:` 字面；`input-contract.md` 含 `### Selected items (batch form)`、`SKILL.md` 含同名小節（`grep -c` 各為 1）
- [x] **[存在性]** 決策 1–15（涵蓋 plan A1–A20）與 plan G1、G2、G6–G11 逐條於兩檔中可指出落點（AI-6①對照表，每條至少一個標題或行號；plan G3–G5 已升格為 A17–A19）
- [x] **[存在性]** token 封閉性：ALLOWED 三檔 diff 新增行之 `STOP_*` 全數存在於 `stop-conditions.md`；新增行無 `btplan`／`btticket`；三模式程式碼區塊與 Procedure 第 1–18 步、Progress 事件表、Response shape 未被改動（`git diff -U0` 之刪除行僅為 AI-1／AI-2／AI-4 之現值）
- [x] **[存在性]** `all eligible` 僅出現於「不支援」敘述；`input-contract.md` L116 現含 `applies to each selected item separately, never to the list as a whole`
- [x] **[存在性]** 狀態矩陣 M1–M10（含 M4a）、E1–E9 逐列走查表（AI-6⑤）已保存，每列條文唯一指向預期處置與 receipt，且每列附最終兩檔之 `檔案:行號`（first-hand；無行號之列不計，Rev A.2）（文字走查，非 runtime 執行；見已知限制②）
- [x] **[存在性]** Consumer 盤點結果已保存（AI-0④與開票時盤點一致）；`TKAuthor_Protocol.md`、`projman/SKILL.md` 零變更；`.claude/CLAUDE.md` 僅 Rule 4e 單句變更（Rev A.3，見 AC A.3-5）
- [x] **[存在性]** `/mdlint` 對兩檔 warning 數不高於 AI-0 baseline
- [x] **[存在性]** （Addendum A.2-6）`SKILL.md` 新增小節含 `Selected:`、`Completed:`、`Stopped:`、`Not started:`、`Shared stop:` 五個摘要欄位字面各恰 1 次，且摘要段明言不含批次層 `Gate:`／`Final state:`；E9 走查預期為五行摘要
- [x] **[存在性]** （Addendum A.2-6）`SKILL.md` 新增小節第 3 點含 shared／item-local 單句判準（`A STOP is shared only if it arises in the shared preflight or is named in the shared list below; every other STOP is item-local.`），且與該點兩份列舉不矛盾
- ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] **[存在性]**（Rev A.3，A.3-1／F1）兩檔中拓撲排序、cycle、dependency STOP 皆僅提 `execution_dependency`；`coordination_dependency` 於兩檔皆明言 informational only（不排序、不成環、不傳染 STOP），且無任何句子仍把 `coordination_dependency` 當排序／成環／STOP 依據（`grep` 逐處核對）
- [x] **[存在性]**（Rev A.3，A.3-2／F2）`SKILL.md` Dependency STOP 含「滿足」定義：同次 invocation 通過 18.6，或已有同 parent artifact＋plan-local item ID 之 `REVIEW`／`CLOSED` 正式票（母體含 Registry main／archive、`Pending/`、`deliver/`〔含 `deliver/archive/`〕、`void/`）；且明言 `STOP_DUPLICATE_TICKET` 不直接阻塞後項、須先驗證匹配正式票、驗證不到才 dependency STOP
- [x] **[存在性]**（Rev A.3，A.3-3／F3）兩檔皆明言 `Selected items` 單一 ID 路由至既有單項流程、不執行 batch set resolution、不產生 batch progress 或摘要、與單數完全等價；批次規則僅適用兩個以上 ID
- [x] **[存在性]**（Rev A.3，A.3-4／F5）`input-contract.md` 表含「兩個以上 ID 附自由文字 `Goal:` → `STOP_DECISION_REQUIRED`」一列與各 Map item goal 為各自 Goal 之句
- [x] **[存在性]**（Rev A.3，A.3-5／F5）`.claude/CLAUDE.md` 僅 Rule 4e 該子句變更：`git diff -U0 -- .claude/CLAUDE.md` 恰一個 hunk、僅該行；行數 ≤ 150；未新增 mode／state／reason code
- [x] **[存在性]**（Rev A.3，A.3-6）狀態矩陣新增／改動列 M2、M10、M10a、M11、E3、E3a、E10、E11、E12 逐列走查表（含 first-hand `檔案:行號`）已保存，其中「真正 2-item batch」與「duplicate-retry」兩情境完整走查（文字級；不建立正式測試票）
- [x] **[存在性]**（Rev A.3，A.3-7）收尾實錄含 F4／F6／F7 Observation 與 4a「續修／拆票／撤回」三案及推薦（PPSS `request_id=rsch-20261019-5118792f50`），且未新增 Gate、state、reason code 或 permanent artifact
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（Rev A.3 增量 `0caf3c7a..abb97bcb`：3 files，+9／−8；Rev A.2 範圍 `cb5a293e..82a14d79` 為 2 files，+50／−5，已於前次審查）（2 files，+50／−5）

## Decision Addendum

> 開票後新增之使用者裁決一律記於本節（Akira 2026-10-19 裁決）；本節優先於上文同名處，上文不回改。

### A.2（2026-10-19，Akira 對話裁決）

1. **AC 標記維持 `[存在性]`**：首次真實 `Selected items` 執行才是行為觀測，不回補更高等級驗證。
2. **`STOP_SOURCE_UNAVAILABLE` 維持 item-local。**
3. **`STOP_ACCESS_BLOCKED` 對 repository／worktree／共用工具等未明定資源，暫維持 item-local**；列為首次真實批次執行的觀測點（見外審紀錄 O1），不為假設性案例擴張規則。上文「依修正後 A18 推得之兩點」與已知限制⑤由此轉為已裁決暫行，不再是待確認點。
4. **不再回寫 parent `.tmp` plan**（含 A18）。
5. **外審**（`INTRODUCED_CAPABILITY≠NONE`）：已執行一次窄範圍 PPSS 定向外審，`request_id=rsch-20261019-ce09322af5`、`run_id=run-c0d65bbb8782`（2026-10-19；僅送去識別化之通用問題與五則查詢，未含本票內容、路徑或程式碼）。題目：LLM agent 對多個明列 work item 嚴格串行處理時，局部失敗隔離、shared-preflight 失敗與 dependency 連帶之安全邊界。不開 Rev、不擋開工、不併入 AI-0。結論（供收尾實錄濃縮引用；證據為通用公開文件，屬 `SUPPORTED`，不構成對本契約之驗證）：
   - **未發現與已裁決設計衝突的硬風險。**
   - 支持：①AWS Well-Architected Agentic AI Lens（AGENTREL06-BP04）——重試前須 read-before-write 檢查既有結果以免重複副作用，對應每 item 重做 step 4／18.1 duplicate 掃描，及「shared STOP／dependency STOP 後另行重跑」；②Apache Airflow DAG 文件——預設 `all_success` 使上游失敗時下游連帶 skip、無關任務照跑，對應 item-local 隔離與依賴 STOP 之遞移；③arXiv 2605.08580（context compaction）——壓縮不可逆且錯誤難以自我偵測，對應「壓縮／續行後重讀 template 與 parent」。
   - 觀測點（僅記錄，不改規則）：**O1** 資源未明定之 `STOP_ACCESS_BLOCKED`（見第 3 點）；**O2** 壓縮後批次進度僅存於對話，已完成項應以 Registry／`Pending/` 現況（duplicate 掃描）為準而非記憶，首次真實批次執行時觀察是否有漏判。
6. **文字優化（採納；於 AI-5 實作時落地，優先於 AI-5 目標值原文）**：
   - **(a) 第 3 點起首補入單句判準**（置於 `3. STOP routing, using only the existing reason codes and their Final states:` 其後、三個子項之前，另起一行縮排三格）：`A STOP is shared only if it arises in the shared preflight or is named in the shared list below; every other STOP is item-local.`
   - **(b) 第 6 點改為固定五行摘要**，整點文字替換為：`6. Receipts: every item that started yields its own existing completion or stop response. After the last one, an informational summary of exactly five lines follows: `Selected:` (the resolved item order), `Completed:` (item → Ticket ID and path), `Stopped:` (item → stop code, dependency STOPs included), `Not started:` (item → reason), and `Shared stop:` (the shared STOP code and the item that raised it, or `none`). The summary is not a Gate, state, or reason code: it carries no `Gate:` or `Final state:` line and no batch-level Gate or Final state, is never written to a file, and cannot claim an overall PASS.`
   - 對應追加 AC 已列於 Acceptance Criteria（標註 Addendum A.2-6）；E9 預期同步為「逐 item 回應＋五行摘要（無批次 `Gate:`／`Final state:`）」。
7. **格式對齊（不改語意）**：AI-3 目標值表格分隔列採 `| --- | --- |`（`/mdlint` Rule G）。

## 收尾實錄（執行端；2026-10-19，Mode: normal；活動時戳 UTC+8）

### 1. 基線與 AI-0 結果（2026-10-19 23:5x 直讀）

- HEAD `cb5a293e656ffebb24f80ef5020d1ab274dd29bd`，工作樹 clean；`git diff --name-only <BASE_COMMIT>..HEAD -- <ALLOWED 兩檔>` 為空（範圍不相交）。兩檔 `git hash-object`＝`ee799ad9`／`0b40039f`，與 `TARGET_BLOB` 一致。
- 錨點命中數：`input-contract.md` 五項、`SKILL.md` 二項皆為 1。`git check-ignore -v` 對兩 ALLOWED 檔無命中（exit 1）。
- `Pending/` 無他票編輯 `ticket-author`；consumer 盤點關鍵 grep 與開票時一致（`.claude/CLAUDE.md`、`projman/SKILL.md`、`TKAuthor_Protocol.md` 等，均未斷言「僅限單項」）。
- `/mdlint` baseline（依其規則 A–H 手動掃描，該 skill 本身為讀檔規則掃描）：兩檔未加空格之表格分隔列各 3 筆（既有，不屬本票）。
- 啟動：`/normal TKA-BATCH-01` → `activate_ticket.sh` 回 `✅ ACTIVATED`；`gate_ledger.py status`：無 incident、無待人工。lifecycle 由執行端於開工寫為 `IN_PROGRESS`。
- **對齊紀錄（如實揭露）**：本票於我首次實作前已被 RGR 修補為 Rev A.2（目標值：AI-2 第三處、AI-3 第 4 列與 identical content 句、AI-5 第 2 點）；我最初依 Rev A.1 目標值寫入，於提交前以程式比對發現落差並改寫至與 Rev A.2＋Decision Addendum A.2 逐字一致（見 §2 驗證），**無任何提交以舊版文字落地**。

### 2. AI-1～AI-5：實作與逐字比對

- 修改僅 `SKILL.md`、`references/input-contract.md`；`git diff --stat`＝2 files，+50／−5。刪除行僅為 AI-1／AI-2／AI-4 之現值（Purpose、`- Selected Ticket Map item …`、`- One selected …`、`- One observable …`、L116 行尾）。
- 程式比對：票面 AI-3 目標值區塊逐字存在於 `input-contract.md`；票面 AI-5 目標值區塊套用 Addendum A.2-6(a)(b) 後逐字存在於 `SKILL.md`；AI-1／AI-2／AI-4 目標值字串全數命中。
- 以 Decision Addendum A.2 落地：第 3 點單句判準（`SKILL.md:200`）、第 6 點固定五行摘要（`SKILL.md:206`）。

### 3. AI-6：檢核證據

- **②token 封閉性**：兩檔 diff 新增行之 `STOP_*`＝`STOP_ACCESS_BLOCKED`、`STOP_DECISION_REQUIRED`、`STOP_GOVERNANCE_UNAVAILABLE`、`STOP_MISSING_SUBJECT`、`STOP_REPLAN_REQUIRED`、`STOP_WRITE_FAILED`，全數存在於 `stop-conditions.md`。
- **③④**：`all eligible` 僅 `input-contract.md:157`（「not supported」敘述）；新增行 `btplan`／`btticket` 0 筆；三模式程式碼區塊無刪除行。
- **五行摘要欄位**：`Selected:`／`Completed:`／`Stopped:`／`Not started:`／`Shared stop:` 各 1 次（`SKILL.md:206`）；單句判準 1 次（`SKILL.md:200`）；L116 片語 `applies to each selected item separately, never to the list as a whole` 1 次（`input-contract.md:116`）；兩檔 `### Selected items (batch form)` 各 1（`SKILL.md:193`、`input-contract.md:153`）。
- **⑥/mdlint（手動規則掃描）**：新增小節標題前後空行、清單與表格前後空行、表格分隔列採 `| --- | --- |`、範例 fence 標 `text`；未加空格分隔列仍各 3（不變）→ 無新增 warning。
- **⑦**：`git diff --name-only`＝僅 ALLOWED 兩檔。
- **①決策／plan 項對照**：

| 項 | 落點 |
| --- | --- |
| 決策 1（方向 C、單項相容、`Selected items`、僅 Plan-backed、不支援 all eligible） | `input-contract.md:111,155,157`；`SKILL.md:195` |
| 決策 2（shared preflight 一次、逐項串行、禁並行與 subagent） | `SKILL.md:197,198` |
| 決策 3（evidence_dependency 未執行 sibling → shared `STOP_REPLAN_REQUIRED`、execution_dependency 允許） | `input-contract.md:165,169` |
| 決策 4（WRITE_FAILED／GOVERNANCE shared；ACCESS_BLOCKED 依資源） | `SKILL.md:200-202` |
| 決策 5（item-local 繼續；依賴 STOP receipt＋run、不做 evidence／ID） | `SKILL.md:202,203` |
| 決策 6（重複 ID shared `STOP_DECISION_REQUIRED`、不 normalization） | `input-contract.md:163` |
| 決策 7（不回滾；可引用前票、不得回寫） | `SKILL.md:204` |
| 決策 8（逐 item receipt＋run；摘要非 Gate；不新增 mode／state／reason code／ledger） | `SKILL.md:195,205,206`；`input-contract.md:155` |
| 決策 9（不改 `STOP_REPLAN_REQUIRED` 語義；僅用既有 code） | `input-contract.md:157,165,166`；`SKILL.md:203`；token 封閉性 |
| 決策 10（範圍僅兩檔；consumer 無單數依賴） | `git diff --name-only`；§1 consumer 盤點 |
| plan G1（單項等價、並存衝突、非 Map token） | `input-contract.md:155,162,164,168` |
| plan G2（拓撲序、成環） | `input-contract.md:166,169` |
| plan G6（runId `-<item>`、preflight 事件記第一個 run） | `SKILL.md:205` |
| plan G7（摘要無自己的 Gate／Final state） | `SKILL.md:206` |
| plan G8（逐 item 重做 duplicate 與 evidence；壓縮後重讀） | `SKILL.md:198` |
| plan G9（僅 Plan-backed） | `input-contract.md:157` |
| plan G10（依賴 STOP 遞移） | `SKILL.md:203` |
| plan G11（shared STOP 後未開始者不起 run） | `SKILL.md:205,206` |

- **⑤狀態矩陣逐列走查（first-hand 行號；文字走查，非 runtime 執行）**：

| 列 | 條文唯一指向之處置 | 行號 |
| --- | --- | --- |
| M1 | 單項流程不變 | `SKILL.md:195`；`input-contract.md:155` |
| M2 | 單一 ID 等同單項 | `input-contract.md:155` |
| M3 | 通過解析、同層依 Ticket Map 順序 | `input-contract.md:157-158,169`（表內五條件皆不命中） |
| M4 | shared `STOP_DECISION_REQUIRED` | `input-contract.md:162` |
| M4a | 內容相同接受為同一清單 | `input-contract.md:168` |
| M5 | shared `STOP_DECISION_REQUIRED`、不 normalization | `input-contract.md:163` |
| M6 | shared `STOP_MISSING_SUBJECT`（`all eligible` 為非 ID token） | `input-contract.md:157,164` |
| M7 | Diag／Spec parent → `STOP_REPLAN_REQUIRED`；Fixture 由既有第 1 步 | `input-contract.md:157`；`SKILL.md:167,197` |
| M8 | shared `STOP_REPLAN_REQUIRED` | `input-contract.md:165` |
| M9 | 允許；被具名者先處理 | `input-contract.md:169` |
| M10 | shared `STOP_REPLAN_REQUIRED` | `input-contract.md:166` |
| E1 | PASS receipt＋獨立 run；已寫票保留 | `SKILL.md:198,204,205,206` |
| E2 | item-local，繼續不依賴它者 | `SKILL.md:200,202` |
| E3 | dependency STOP：`STOP_REPLAN_REQUIRED`＋run，不做 evidence／ID（含遞移） | `SKILL.md:203` |
| E4 | shared：未開始者 `not attempted`、不起 run；已完成保留 | `SKILL.md:201,204,205,206` |
| E5 | `STOP_ACCESS_BLOCKED` 於 parent／Ticket template／Registry → shared | `SKILL.md:201` |
| E6 | 其餘資源之 `STOP_ACCESS_BLOCKED` 及 `STOP_SOURCE_UNAVAILABLE` 等 → item-local | `SKILL.md:200,202` |
| E7 | 禁止回寫前票；僅可引用正式 ID | `SKILL.md:204` |
| E8 | 壓縮／續行後下一 item 前重讀 template 與 parent | `SKILL.md:198` |
| E9 | 逐 item 回應＋五行摘要、無批次 `Gate:`／`Final state:` | `SKILL.md:206` |

### 4. 外審結論（PPSS 窄範圍定向外審；Decision Addendum A.2-5 濃縮）

`request_id=rsch-20261019-ce09322af5`、`run_id=run-c0d65bbb8782`。**未發現與已裁決設計衝突的硬風險**；來源為通用公開文件，屬 `SUPPORTED`，非對本契約之驗證：①重試前 read-before-write（AWS Agentic AI Lens AGENTREL06-BP04）↔ 每 item 重做 duplicate 掃描與「另行重跑」；②預設 `all_success` 使上游失敗連帶 skip、無關任務照跑（Airflow DAG）↔ item-local 隔離與依賴 STOP 遞移；③壓縮不可逆、錯誤難自我偵測（arXiv 2605.08580）↔ 壓縮後重讀 template 與 parent。觀測點（不改規則）：O1 資源未明定之 `STOP_ACCESS_BLOCKED`；O2 壓縮後批次進度以 Registry／`Pending/` 現況為準而非記憶。

### 5. 殘餘與限制

- 驗證止於文字層（存在性＋逐列走查）；模型遵循度待首次真實 `Selected items` 呼叫觀察（觀察物＝逐 item receipt 與五行摘要，對照狀態矩陣；O1／O2）。
- `[存在性]` 標記維持（Addendum A.2-1）；`STOP_SOURCE_UNAVAILABLE`、未明定資源之 `STOP_ACCESS_BLOCKED` 維持 item-local（Addendum A.2-2／3）。
- `TKAuthor_Protocol.md` 之 Desktop 提示詞樣板未含 `Selected items`，依範圍不改。
- 已提交：`82a14d79`（2 files，+50／−5）。/qa：Step 0 PASS（無變更＝committed diff 為空，工作樹僅兩 ALLOWED 檔 dirty）；Step 1–3（assembleDebug／unit test／lint）對 docs preset 為 N/A（Template：`assembleDebug` 對 docs 型 N/A）未執行；Step 3.5 靜態盲區掃描僅 app/src/main 既有項目 ⚠️（本票零 app 變更，豁免理由待 sv_report 記錄）；Step 4 SKIPPED — DEFERRED-TO-DOCSYNC（票面 FORBIDDEN 命中 PROGRESS.md，同步債＝Dashboard／Done 標記／Pending→deliver／checkbox，於下一輪 /compact 處理，不另開票）。

### 6. QA／SV／Cross-Review 結果（2026-10-19）

- **/qa**：Step 0 `verify_checklist.sh` PASS（`gate_ledger.py check qa` exit 0）；Step 1–3（assembleDebug／unit test／lint）docs preset N/A 未執行；Step 3.5 靜態盲區掃描僅 `app/src/main` 既有項目 ⚠️，本票零 app 變更（豁免理由記於 sv_report）；Step 4 SKIPPED — DEFERRED-TO-DOCSYNC（FORBIDDEN 命中 PROGRESS.md）；Step 4.5 提交 `82a14d79`（`check commit` exit 0；DIFF_REF=`cb5a293e`、DIFF_END=`82a14d79`）。
- **/sv**：前置檢查 `check sv` exit 0、工作樹乾淨；Stage A 檔案邊界＝僅 ALLOWED 兩檔；Stage B 殘骸 0、Mock 無、Guard Input Reachability 無 UNREACHABLE GUARD、Integration Test 存在性＝⚠️ MISSING-INTEGRATION-TEST（如實，見已知限制②；全部 AC 為 `[存在性]` 層級）；Stage C 無 SSOT 衝突。
- **R0 預審**（`cross-reviewer`）：FORWARD（全 INFO，無 ACTIONABLE／UNDECIDABLE／AUTHORITY），記錄 `.tmp/fail-advisor/R0_TKA-BATCH-01_20261019_235336.md`（`CLASSIFICATION: NORMAL`）。
- **Codex Cross-Review**：R1 `REPORT_FIX`（`REPORT_FIX_SOURCE=codex`；sv_report Stage B 缺 sibling lifecycle `REVIEW`／`CLOSED` 之 production writer 舉證）＋ADVISORY（MISSING-INTEGRATION-TEST）。處置：Gate `INC-001`（`gate_point=sv`、`verdict=REPORT_FIX`），round 1（executor 層）補上 first-hand writer 舉證（`ssot-governance.md:81-82`、`sv/SKILL.md:206-212`、本 session `registry_set_status` 實寫、註冊表 `CLOSED` 5＋92／`VOID` 12 計數）後重審 → R2 **ADVISORY**（exit 0；`REACHABILITY` token 全列 `檔案:行號`；唯一 ADVISORY 仍為缺 production-path 驗證，解除條件＝以真實 Plan-backed parent 驗證拓撲順序、逐項 receipt 與 shared／item-local／dependency STOP routing，即首次真實 `Selected items` 使用）。`INC-001` round 1 `pass`，incident 已 `closed`（rounds 1／failed 0）。輪次：機械 R2（REPORT_FIX 不遞增 BLOCKER ROUND）。
- **QA Step 5 `structural_verify.sh --post`**：Verdict `ADVISORY`，exit 0（A/B/C/D/E/F/G）；唯一警示為 PROGRESS.md 80 行 > 60（既有，與本票無關，且 PROGRESS 屬 FORBIDDEN）。
- Step 10（Done 標記／PROGRESS 同步）：SKIPPED — DEFERRED-TO-DOCSYNC（FORBIDDEN 命中；同步債＝Dashboard、`PROGRESS_ARCHIVE.md` Done 標記、票檔 Pending→deliver 之冊面外同步、checkbox，於下一輪 `/compact` 處理，不另開票）。
- 實際 verdict：Codex `ADVISORY`（依 Transition Table 無 BLOCKER／FAIL finding 視同 PASS；冊面 `reviews.codex` 映射 `PASS`，實際 verdict 記於 checkpoint `codex_verdict_actual`）。Traycer Workflow＝N/A → `reviews.traycer=N_A`。

## Rev A.3 收尾實錄（執行端；2026-10-20）

### 6. Rev A.3 基線、實作與逐字比對

- **重開（先例 `INFRA-RULESLIM-01` Rev B）**：冊面 `REVIEW→IN_PROGRESS`、`reviews` 重置（qa／sv／codex＝`PENDING`，traycer＝`N_A`）、`file` 改指 `Pending/`、票檔自 `deliver/` 退回 `Pending/`；`activate_ticket.sh TKA-BATCH-01` 重新啟動，conf 讀得 `ALLOWED` 三檔、`DIFF_REF=0caf3c7a`、`BASE_COMMIT=0caf3c7adba0ec230c17943eb8d7167ef0f880ac`、`TARGET_BLOB` 三檔。
- **AI-8 結果**：三檔 `git hash-object`＝`7ec966aa`／`3248b6c0`／`2e8b0cec`，與 `TARGET_BLOB` 一致；`BASE_COMMIT..HEAD` 對 ALLOWED 三檔 diff 為空；`git check-ignore -v` 三檔無命中；`.claude/CLAUDE.md` 基線 143 行；`Pending/` 其餘兩票雖提及 `CLAUDE.md`，其 `ALLOWED` 為 `.claude/PROGRESS.md`／`app/src/main/...`，無重疊。
- **AI-9／AI-10／AI-11**：以程式（`apply_revA3.py`，保留 LF）依票面 R1–R5、R1／R2、Rule 4e 子句逐處替換，替換前每個現值於檔內命中恰 1；`git diff --stat -- <三檔>`＝3 files，+9／−8；`.claude/CLAUDE.md` 恰一個 hunk、143 行不變、僅 L77 該子句。
- 落點（first-hand）：`input-contract.md:113`（goal 行）、`:155`（單一 ID 路由）、`:166-167`（execution-only cycle 列、Goal 列）、`:170`（execution 排序與 coordination informational）、`:171`（各 item goal、不接受 invocation 級 `Goal:`）；`SKILL.md:195`（單一 ID 不套批次規則）、`:198`（第 2 點）、`:203`（Dependency STOP：滿足定義、`STOP_DUPLICATE_TICKET` 處置、coordination informational）；`.claude/CLAUDE.md:77`（Rule 4e 子句）。
- **token 封閉性**：新增行之 `STOP_*`＝`STOP_DECISION_REQUIRED`、`STOP_DUPLICATE_TICKET`、`STOP_REPLAN_REQUIRED`，皆存在於 `stop-conditions.md`；`coordination_dependency` 於兩檔僅剩 `SKILL.md:148`（Ticket Map 欄位清單，既有）、`SKILL.md:203`／`input-contract.md:170`（informational only），無排序／成環／STOP 殘留。

### 7. 4a 紀錄：續修／拆票／撤回（窄範圍 PPSS，`request_id=rsch-20261019-5118792f50`、`run_id=run-ed195de7f44b`）

Rev A.3 使本票版次達 3，觸發結構歧義停損律①。依 Akira 2026-10-20 核定「同票 fix-forward」，於既有研究授權下作一次窄範圍 PPSS（僅送去識別化通用問題；證據為通用公開文件，屬 `SUPPORTED`，不構成對本契約之驗證），並記三案與推薦。未新增 Gate、state、reason code 或 permanent artifact。

- **外部要點**：fix-forward 為成熟組織預設，前提是「修正在手且可迅速安全上線」；rollback 適用於「修法未知、錯誤正在污染狀態、結構性無效、無法安全上線」（Fix Forward 指南）。變更適合 rollback 或 forward-fix 取決於四性質——冪等性、可逆性、資料效應、依賴觸及面（runbook.academy 決策框架）。次要缺陷且修法確定、低風險時以 roll-forward 為宜；嚴重、使用者可見的缺陷才傾向先 rollback（Google SRE 觀點）。
- **對應本票事實**：四項必修（F1／F2／F3／F5）語義已由 Akira 核定、修法在手；變更為純文字契約（`git revert` 即可還原）、無資料效應、觸及面僅 `SKILL.md`／`input-contract.md` 與 `CLAUDE.md` Rule 4e 一句；`Selected items` 尚未被任何真實呼叫使用，未造成任何正式票或狀態污染。

| 方案 | 內容 | 評估 | 推薦 |
| --- | --- | --- | --- |
| 續修（fix-forward） | 同票 Rev A.3，修 F1／F2／F3／F5，F4／F6／F7 入 Observation | 修法在手、可逆、無資料效應、觸及面窄；四項缺陷同屬同兩份契約，一併修可避免中間不一致 | **推薦** |
| 拆票 | 將 F1／F2、F3、F5 分成多張票 | 缺陷共改同兩份契約檔與同一批次語義（plan A15 已定 coupled），拆開會產生「半修」中間狀態，增加而非降低風險；無證據顯示變更過度糾纏需拆 | 不推薦 |
| 撤回（revert） | `git revert` `82a14d79`（及票檔提交 `0caf3c7a`） | 可行且低風險（純文字、可逆），但目前無實害可止損，且會丟棄已通過審查的部分；列為下一輪若再被退回的後備方案 | 不推薦（保留為後備） |

### 8. 文字級走查（不建立正式測試票；first-hand 行號取自 Rev A.3 最終檔）

**情境 A：真正 2-item batch**——parent plan 有 `TA`（無依賴）、`TB`（`execution_dependency`＝`TA`、`coordination_dependency` 亦提及 `TA`），呼叫 `Selected items: TA, TB`，不附 `Goal:`。

| 步驟 | 條文指向之處置 | 行號 |
| --- | --- | --- |
| 1 | 兩個 ID → 套用批次規則（單一 ID 才路由單項流程） | `input-contract.md:155`；`SKILL.md:195` |
| 2 | shared preflight 一次：parent 分類、template／parent 完整讀取、集合解析；表內六條件皆不命中（無不同內容並存、無重複、token 皆在 Map、無未執行 sibling 的 `evidence_dependency`、execution 無環；未附 `Goal:`） | `SKILL.md:197`；`input-contract.md:157-158,162-167` |
| 3 | 排序：`TB` 在 `execution_dependency` 具名 `TA` → `TA` 先；`coordination_dependency` 不參與排序 | `input-contract.md:170` |
| 4 | `TA` 跑 step 3 atomicity 至 18.6 → PASS，獨立 progress run＋PASS 回應 | `SKILL.md:198,205,206` |
| 5 | `TB`：`execution_dependency` 前置項 `TA` 於同次 invocation 通過 18.6 → 視為滿足；`TB` 的 step 4／18.1 與 live evidence 重做（看得見 `TA` 剛寫的票），並以 `TA` 的正式 Ticket ID 引用 | `SKILL.md:198,203` |
| 6 | 摘要固定五行：`Selected: TA, TB`／`Completed:` 兩項→ID 與路徑／`Stopped: none`／`Not started: none`／`Shared stop: none`；無批次 `Gate:`／`Final state:` | `SKILL.md:206` |

變體 A'：`TA` 以 item-local STOP 結束（如 `STOP_AC_UNREACHABLE`）且無匹配 `REVIEW`／`CLOSED` 正式票 → `TB` 前置項未滿足 → dependency STOP：`STOP_REPLAN_REQUIRED`＋獨立 run，不做 evidence、不分配 ID；摘要 `Stopped:` 列 `TA`→code 與 `TB`→`STOP_REPLAN_REQUIRED`（`SKILL.md:202,203,206`）。變體 A''：`TB` 僅以 `coordination_dependency` 具名 `TA` 而 `TA` STOP → `TB` 不受影響，照常處理（`SKILL.md:203`；`input-contract.md:170`）。

**情境 B：duplicate-retry**——同一 parent 先前已建立 `TA` 的正式票，再次呼叫 `Selected items: TA, TB`（`TB` 的 `execution_dependency`＝`TA`）。

| 步驟 | 條文指向之處置 | 行號 |
| --- | --- | --- |
| 1 | 集合解析通過（同情境 A 步驟 2） | `input-contract.md:157-167` |
| 2 | `TA` 的 step 4／18.1 查重命中既有正式票 → `STOP_DUPLICATE_TICKET`，屬「其餘 STOP」＝item-local，記 receipt、繼續不被其阻塞者 | `SKILL.md:200,202` |
| 3a | 既有 `TA` 票 lifecycle 為 `REVIEW` 或 `CLOSED`、且內嵌 source 為同一 parent artifact＋plan-local item `TA`（母體含 Registry main／archive、`Pending/`、`deliver/`〔含 `deliver/archive/`〕、`void/`） → 驗證到匹配正式票 → 前置項視為滿足，**`STOP_DUPLICATE_TICKET` 不直接阻塞 `TB`**；`TB` 照常處理並引用該票正式 ID | `SKILL.md:203` |
| 3b | 既有 `TA` 票僅為 `PENDING`／`IN_PROGRESS`／`EXECUTED`（驗證不到 `REVIEW`／`CLOSED`） → 前置項未滿足 → `TB` dependency STOP（`STOP_REPLAN_REQUIRED`＋run；`Decision or input needed`：先處理 `TA` 之 STOP，另行重跑 `TB`） | `SKILL.md:203` |
| 4 | 摘要：3a 時 `Completed:` `TB`、`Stopped:` `TA`→`STOP_DUPLICATE_TICKET`；3b 時 `Completed: none`、`Stopped:` 含 `TA` 與 `TB` | `SKILL.md:206` |

變體 B'（部分寫入後重試）：首次呼叫 `TA` 成功寫入（`PENDING`）而 `TB` 因 `STOP_WRITE_FAILED`（shared）未完成；重試時 `TA` 觸發 `STOP_DUPLICATE_TICKET` 且其票尚非 `REVIEW`／`CLOSED` → 3b。條文給出的處置即「另行重跑尚未完成之項（只列 `TB`）」：單獨 `Selected item: TB` 時，對未選定 sibling 的引用維持單項既有行為（`input-contract.md:170`），不受批次規則影響。

其他矩陣新增／改動列：M2（單一 ID 路由，`input-contract.md:155`）、M10（execution-only 成環，`:166`）、M10a（僅 coordination 具名→忽略，`:170`）、M11（≥2 ID＋自由文字 `Goal:`→shared `STOP_DECISION_REQUIRED`，`:167,171`；`.claude/CLAUDE.md:77`）、E3／E3a／E10／E11／E12（`SKILL.md:203`）皆由上述條文唯一指向預期處置。

### 9. Observation（F4／F6／F7 不修；依核定如實記錄）

- 依 Akira 2026-10-20 核定，F4（plan-series）、F6（parent-drift）、F7（telemetry）不修，不建立新機制，不擴大 plan-series／parent-drift／telemetry 範圍。原 T2 審查全文未隨本 session 提供，故僅依核定所述範圍標籤記錄、不另行詮釋其細節。
- 與既有觀測點合併，首次真實 `Selected items` 使用時一併觀察：O1（未明定資源之 `STOP_ACCESS_BLOCKED`）、O2（壓縮後批次進度以 Registry／`Pending/` 為準）。

### 10. Rev A.3 增量 QA／SV／R0／Codex 結果（2026-10-20；僅審 `0caf3c7a..abb97bcb`）

- **/qa**：`check qa` exit 0；Step 0 `verify_checklist.sh` PASS（`diff-ref 0caf3c7a`，ALLOWED 三檔）；Step 1–3 docs preset N/A 未執行；Step 3.5 與本票零 app 變更無關（同 Rev A.2 理由）；Step 4 SKIPPED — DEFERRED-TO-DOCSYNC（沿用 Rev A.2 同步債登記）；Step 4.5 `check commit` exit 0，提交 `abb97bcb`（3 files，+9／−8），`DIFF_END=abb97bcb`。
- **/sv**：`check sv` exit 0；Stage A 邊界＝僅三 ALLOWED 檔，FORBIDDEN 零變更，`.claude/CLAUDE.md` 恰 1 hunk、143 行不變；Stage B 殘骸 0、Mock 無、Guard Input Reachability 無 UNREACHABLE GUARD、Integration Test 存在性＝⚠️ MISSING-INTEGRATION-TEST（如實；Akira 核定不得建立正式測試票模擬）；Stage C 無 SSOT 衝突。
- **R0 預審**：FORWARD（全 INFO；`CLAUDE.md` file-class 命中但授權依據齊備〔ALLOWED、AI-11、Decision 14、HC9、AC A.3-5〕），記錄 `.tmp/fail-advisor/R0_TKA-BATCH-01_20261020_013645.md`（`CLASSIFICATION: NORMAL`）。
- **Codex Cross-Review**：R1 **ADVISORY**（exit 0；無 BLOCKER／REPORT_FIX）。唯一 ADVISORY＝缺行為驗證：`SKILL.md:203` 之 dependency gate 與 `input-contract.md:167` 之多項 Goal gate 僅文字走查，首次真實批次呼叫需補 production-path 行為證據；`REACHABILITY` token＝`batch-selection:SKILL.md:148`、`dependency-satisfaction:ssot-governance.md:81`。機械輪次 R1（本輪無 REPORT_FIX，無新 Gate incident；`INC-001` 仍 closed）。
- **QA Step 5 `structural_verify.sh --post`**：Verdict `ADVISORY`，exit 0（A/B/C/D/E/F/G）。
- Step 10（Done 標記／PROGRESS 同步）：SKIPPED — DEFERRED-TO-DOCSYNC（FORBIDDEN 命中；沿用）。
- 實際 verdict：Codex `ADVISORY`（無 BLOCKER／FAIL，視同 PASS；冊面 `reviews.codex` 映射 `PASS`，實際 verdict 記於 checkpoint `codex_verdict_actual`）。Traycer Workflow＝N/A → `reviews.traycer=N_A`。

### 11. 決策 1–15 與 plan G 項對照（Rev A.3 最終檔；first-hand 行號）

| 項 | 落點 |
| --- | --- |
| 決策 1（方向 C、單項相容、`Selected items`、僅 Plan-backed、不支援 all eligible） | `input-contract.md:111,155,157`；`SKILL.md:195` |
| 決策 2（shared preflight 一次、逐項串行、禁並行與 subagent） | `SKILL.md:197,198` |
| 決策 3（evidence_dependency 未執行 sibling → shared `STOP_REPLAN_REQUIRED`；execution_dependency 允許） | `input-contract.md:165,170` |
| 決策 4（WRITE_FAILED／GOVERNANCE shared；ACCESS_BLOCKED 依資源） | `SKILL.md:200-202` |
| 決策 5（item-local 繼續；前置項未滿足之 dependency STOP receipt＋run） | `SKILL.md:202,203` |
| 決策 6（重複 ID shared `STOP_DECISION_REQUIRED`） | `input-contract.md:163` |
| 決策 7（不回滾；引用前票、不得回寫） | `SKILL.md:204` |
| 決策 8（逐 item receipt＋run；摘要非 Gate；不新增 mode 等） | `SKILL.md:195,205,206`；`input-contract.md:155` |
| 決策 9（不改 `STOP_REPLAN_REQUIRED` 語義） | `input-contract.md:165,166`；`SKILL.md:203`；token 封閉性 |
| 決策 10（範圍；consumer 無單數依賴；Rev A.3 增列 `CLAUDE.md` Rule 4e） | `git diff --name-only 0caf3c7a..abb97bcb`＝三檔；§6 consumer 盤點 |
| 決策 11（F1 execution-only） | `input-contract.md:166,170`；`SKILL.md:203` |
| 決策 12（F2 依賴滿足、`STOP_DUPLICATE_TICKET` 不直接阻塞） | `SKILL.md:203` |
| 決策 13（F3 單一 ID 路由單項流程） | `input-contract.md:155`；`SKILL.md:195` |
| 決策 14（F5 Goal 規則與 Rule 4e 單句） | `input-contract.md:113,167,171`；`.claude/CLAUDE.md:77` |
| 決策 15（F4／F6／F7 Observation） | 本票收尾實錄 §9 |
| plan G1（單項等價、並存衝突、非 Map token） | `input-contract.md:155,162,164,169` |
| plan G2（拓撲序、成環；Rev A.3 限 execution） | `input-contract.md:166,170` |
| plan G6（runId `-<item>`、preflight 事件記第一個 run） | `SKILL.md:205` |
| plan G7（摘要五行、無批次 Gate／Final state） | `SKILL.md:206` |
| plan G8（逐 item 重做 duplicate 與 evidence；壓縮後重讀） | `SKILL.md:198` |
| plan G9（僅 Plan-backed） | `input-contract.md:157` |
| plan G10（依賴 STOP 遞移；Rev A.3 限 execution、含滿足判定） | `SKILL.md:203` |
| plan G11（shared STOP 後未開始者不起 run） | `SKILL.md:205,206` |

### 12. 殘餘與限制（Rev A.3）

- 驗證仍止於文字層（存在性＋2-item batch／duplicate-retry 走查）；Codex 之 ADVISORY 解除條件＝首次真實 `Selected items` 呼叫提供 production-path 行為證據（觀察物：逐 item receipt 與五行摘要，對照狀態矩陣）。
- 部分寫入後重試（變體 B'）之處置為「只重跑尚未完成之項」；多 ID 重試若前置項票尚非 `REVIEW`／`CLOSED` 則依 3b 走 dependency STOP——此為條文如實結果，非缺陷。
- F4／F6／F7 與 O1／O2 見 §9；4a 三案見 §7。

