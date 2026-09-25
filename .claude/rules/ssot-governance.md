---
description: PROGRESS.md / SSOT 文件治理規則 — 確保儀表板精簡、歸檔紀律
globs:
  - ".claude/PROGRESS.md"
  - ".claude/PROGRESS_ARCHIVE.md"
  - "plans/architecture/*SSOT*.md"
---

> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

# SSOT & Progress Governance

## Governance Artifact 清單（單一真相來源）

`/compact` Auto Commit 步驟 **MUST** 依此表逐檔獨立提交，Scope 汙染判斷式 **MUST** 以此表為準（表列外之 tracked dirty 檔案才判 BLOCKED）；新增 Governance Artifact 只需在此表加一行，**MUST NOT** 另開票或單獨改 SKILL.md 判斷邏輯。

| 檔案路徑 | Commit Prefix | 說明 |
| ------- | ------------- | ---- |
| `.claude/PROGRESS.md` | `[COMPACT-SYNC]` | Sprint Dashboard |
| `.claude/PROGRESS_ARCHIVE.md` | `[COMPACT-SYNC]` | Dashboard 歸檔 |
| `plans/cso_briefing/project/TICKET_REGISTRY.json` | `[GOVERNANCE-SYNC]` | 跨票狀態總帳（Manifest/Registry 區分見 `.gitignore` 註解，2026-07-28 CSO 裁決） |
| `.claude/rules/*.md` | `[GOVERNANCE-SYNC]` | 執行者面治理規則載體（含本檔；目錄級表列，新增規則檔免再補表。Akira 2026-08-14 對話裁決：`.claude/rules/` 皆以 `[GOVERNANCE-SYNC]` 提交） |
| `plans/cso_briefing/project/deliver/*.md` | `[GOVERNANCE-SYNC]` | 已交付票檔，`REVIEW`／`CLOSED` 之物理歸位產物（`ACAGOV-RUNTIME-TABLE-01`，§Session Persistence 邊界 AI-2 補列） |

觸發來源：Akira 2026-07-30 對話裁決——`.gitignore` Registry 裁決記錄僅存於 PROGRESS.md 敘述文字/memory，未回寫進 `compact/SKILL.md` 執行邏輯，導致每次 `/compact` 仍以單檔硬編碼判斷 Scope 汙染；改為表驅動以杜絕「規則寫了但執行邏輯沒讀到」重演。

**票檔目錄三分**：`plans/cso_briefing/project/Pending/`（活態未交付）／`deliver/`（已交付）／`void/`（已作廢，`lifecycle=VOID` 票檔之歸位目錄）三者並列，涵蓋票檔全生命週期落點。

## PROGRESS.md Dashboard 紀律

- PROGRESS.md **MUST** 保持 ≤80 行（目標 ≤50 行）。超過 80 行 → structural_verify.sh Check F.2 FAIL。
- 已完成的 Session Notes（`>` 開頭行）**MUST** 在 3 個 Session 內移至 `PROGRESS_ARCHIVE.md`。
- Current Day 行 **SHOULD** 使用 compact 表格格式列出最近 4–5 筆 Ticket，不使用 run-on 純文字串。
- Sprint Health `Schedule` 欄 **MUST** 僅列最近 5 筆 Ticket 狀態，歷史狀態移至 ARCHIVE。
- `PROGRESS_ARCHIVE.md` **MAY** 無行數限制，但 **MUST** 保持追加順序（新內容加在尾部）。

## Pipeline Artifact 豁免邊界

- Pipeline Artifact 豁免（`verify_checklist.sh` `PIPELINE_ARTIFACT` + `PIPELINE_ARTIFACT_MAX_LINES`）僅限 `/qa` Step 4 產生的**增量** Dashboard／ARCHIVE 同步。
- **任何服務於票面實質目的、不在 `ALLOWED` 內的內容變更（無論大小）MUST 明列進 `ALLOWED`**，不得倚賴 Pipeline Artifact 豁免夾帶。
- 任何 `sv_report.md` **MUST NOT** 以「Pipeline Artifact」標籤自行擴張 `ALLOWED` 範圍（`MEO-T3-FIX-A` Hard Constraint 4 常設化）。
- 超過 `PIPELINE_ARTIFACT_MAX_LINES` 的合法大規模輪替 **MUST** 走明列 `ALLOWED` 路徑取得授權，不得靠豁免放行；且 `PROGRESS_ARCHIVE.md` 輪替（3-session 規則）到期時 **MUST** 延後至專用 docsync 票執行（前例 `MEO-T3-DOCSYNC-01`），**MUST NOT** 在無關票內就地輪替。

## PROGRESS_ARCHIVE.md Sprint 邊界治理

- Sprint 結束時，**SHOULD** 將整份 ARCHIVE 重命名為 `PROGRESS_ARCHIVE_Sprint[N].md`，新 Sprint 從空 ARCHIVE 開始。
- Session-level 散文 **MUST** 在歸檔前壓縮為 compact 表格格式（保留：Session ID / Ticket / 一句話任務 / Status / Commit）。
- Build/Test/Lint/ELF 詳細結果 **MUST NOT** 進入 ARCHIVE（活在 `sv_report.md` + git log）。

## SSOT 版本引用紀律

- `CLAUDE.md` / `PROGRESS.md` / `CSO_INDEX.md` / `UITA_INDEX.md` **MUST** 只引用現行 SSOT 版本路徑。
- 每次 SSOT 版本升級 **MUST** 在同一 commit 中同步更新所有引用文件。
- `/index-verify` **SHOULD** 在每次 SSOT 升級後立即執行，確認零過期引用。
- 引用已刪除路徑 → structural_verify.sh Check F 或 index-verify **MUST** 攔截。

## Ticket 生命週期流程（五段）

```text
PENDING → TICKET_RIGOR → IN_PROGRESS → EXECUTED → REVIEW → CLOSED
```

旁支：`BLOCKED` 可自任意活態進出；`VOID` 可自任意活態進入，為終態。

**Pending/ 淨空原則**：
1. `plans/cso_briefing/project/Pending/` 僅承載**活態未交付**票（`PENDING`／`TICKET_RIGOR`／`IN_PROGRESS`／`EXECUTED` 四值），**MUST NOT** 窄寫為「僅 PENDING」。
2. 經判定無復活路徑者 **MUST** 轉 `VOID` 並依「VOID 歸位紀律」（見 §Ticket Closure 紀律）離開 `Pending/`，**MUST NOT** 以 `blocked_by` 承載凍結語義而長期滯留活態。
3. `BLOCKED` 僅為暫時態，其解除或轉 `VOID` 之判準 **MUST** 記於冊面 note，禁無限期停留。
4. `Pending/` 內**非票檔**之文件（編纂檔、executor briefing、分析稿等）不受本原則之票檔紀律拘束，但 **MUST** 於目錄↔冊面落差清單中單獨分類，**MUST NOT** 混入「孤兒票」計數。

`lifecycle` 合法值之 SSOT 為平台 `registry_service.py` `VALID_LIFECYCLE`；本節為其流程語義之引用。

`Pending/` 之票檔集合與冊面活態未交付票集合 **MUST** 一一對應；發現落差即為治理 finding，**MUST** 於發現當批修正或明載於冊面。

**`file` 欄正規化前置**：APP 冊 `file` 欄現存兩種格式——短路徑（如 `Pending/INFRA-SV-GOV-01_SV_Governance_Fix.md`）與長路徑（如 `plans/cso_briefing/project/Pending/...md`）。比對前 **MUST** 先將兩者正規化至同一形式（取 basename 或統一去除 `plans/cso_briefing/project/` 前綴），**否則全部舊票將被誤判為落差**。

**落差清單分類**（依現況快照產出，**MUST NOT** 代為處置任何一筆，處置屬後續裁決）：①`Pending/` 有檔而冊面查無（孤兒票檔）；②冊面活態未交付而 `Pending/` 無檔；③冊面 `file` 欄指標與實際落點不符；④`Pending/` 內非票檔文件。事件支撐：PLT 端 2026-08-30 實測發現四張票檔在 `docs/pending/` 而冊面查無，其中兩張為已 VOID 歸檔票之滯留副本；APP 端 2026-09-05 已知至少存在 `MEO-FIX-A`／`MEO-FIX-B` 系列孤兒檔與 `void/INFRA-CLAUDE-MERGE-01`（票檔在盤而主冊與歸檔冊皆查無）。

### 五段觸發與寫入者

| 段 | 觸發 | 寫入者 |
| --- | --- | --- |
| ① `TICKET_RIGOR` | rigor gate 產報告 | 執行者（併寫 `rigor` 欄；開票或過閘時若判定須深驗，MUST 同批寫 `verification` 欄） |
| ② `IN_PROGRESS` | 開工 | 執行者 |
| ③ `EXECUTED` | AC 自驗全綠，未過 Cross Review | 執行者 |
| ④ `REVIEW` | Cross Review 通過或依 RAPID MODE 省略 | 執行者（**併** `reviews.codex=PASS`／`N_A`＋票檔搬 `deliver/`＋`file` 欄及冊面更新），Traycer T2 另由 Akira 於 VS Code 手動執行 T2/CSO 複審 |
| ⑤ `CLOSED` | 複審通過 | **限審查方 SMTR/CSO**（終審 `reviews` 回填＋收束 note） |

**物理歸位邊界**（Akira 2026-09-07）：ticket 進入 `REVIEW` 即搬入 `deliver/`；`CLOSED` 保持於 `deliver/`；`VOID` 歸 `void/`。

### verification 與 reviews 之分工

`verification` ＝ 票之**要求**（開票或過閘時決定，屬性）；`reviews` ＝ 閘之**執行結果**（執行時回填）。**⑤段收束時 MUST 逐項核對 `verification.required` 各值於 `reviews` 有對應結果（PASS 或明示豁免記於 note），有缺即不得轉 `CLOSED`。**

### §0 waiver 直跳條款

票面帶 Rigor Gate Waiver 者得 `PENDING → IN_PROGRESS` 直跳；`TICKET_RIGOR` 僅過閘票必經。

### Traycer T2 執行邊界

Traycer 為封閉 VS Code plugin，**MUST NOT** 假設可由 agent／CLI／MCP 工具鏈外部觸發。票面聲明需 Traycer T2 審查者，該審查動作 **MUST** 由人工（Akira）於 VS Code 內手動執行；執行端 **MUST NOT** 因無法觸發而靜默跳過、推定 PASS，或另尋機械替代路徑。此類票之收尾 **MUST** 正常轉入 `④REVIEW`（票檔搬 `deliver/`＋`file` 欄及冊面更新，同 §「五段觸發與寫入者」④列），**MUST NOT** 停於 `③EXECUTED`——`REVIEW` 本身即為「等待最終 T2／CSO 複審」之合理停留態，`EXECUTED` 僅代表「AC 自驗全綠、未過 Cross Review」之更早期狀態，用其表示「等 Traycer」語意錯位。`reviews.traycer` 欄位維持 `PENDING`（或依 §4.5 契約填 `N_A`）以標記人工複審未完成；lifecycle 本身 **MUST NOT** 因此卡在 `EXECUTED`。

### Traycer 產出物之票務地位（registry existence-first）

（Akira 2026-09-09 裁決；事件：Traycer 對未入冊且已裁決不核准開立之 `INFRA-PREFLIGHT-CORE-01-FIX-D` 產出 T1 `Approach Drift` 判定，其存在性依據為 Traycer 自家 Epic 之 `Todo` 狀態）

1. **草案地位**：Traycer 產出之 ticket 一律為**草案**，`TICKET_REGISTRY.json` 為票務存在與狀態之唯一 SSOT。草案經 SMTR `registry_upsert_ticket` 入冊後方為有效票；未入冊者不具票務效力，**MUST NOT** 派工、**MUST NOT** 作為他票之依賴或證據。
2. **審查存在性前置**：ticket 級審查 **MUST** 以冊面 `ticket_id` 為錨（`registry_list(project, ticket_id)`，禁 `get_ticket`，同 §「治理載體↔存取工具對應圖譜」）。冊面查無此票者，該審查**無效**，**MUST NOT** 產生任何 Tier／verdict 裁決；正確處置為回報「審查對象不存在於冊面」。
3. **零 diff 之判讀前提**：「零 diff ＋ 追蹤器狀態未動 → 判定未執行」僅於審查對象**已入冊且已派工**時成立。缺此前置時，零 diff 為**草案未獲核准之預期結果**，**MUST NOT** 讀作交付偏移或執行失敗。
4. **不新增第二套流程**：本節僅界定草案與有效票之分野，**MUST NOT** 據此新增第二套 lifecycle、verdict 枚舉或 approval 流程；既有五段語義與 `reviews` 四欄不變。

## 治理載體↔存取工具對應圖譜

- **兩套系統明示分立**：治理冊面（`plans/cso_briefing/project/TICKET_REGISTRY.json`，即本規則全篇治理對象）與執行審批票（SQLite，mcp-ticket-platform repo 之 `core/migrations/001_init_v2.sql`）為**互不相通之兩套資料**，同名概念不共享資料。工具由同一台 `mcp-ticket-platform` MCP server 提供，服務兩專案。
- **工具對應表**：治理冊面用 `registry_list`／`registry_upsert_ticket`／`registry_set_status`／`registry_set_checkpoint`／`registry_archive_tickets`，**均需 `project=SmartCameraApp1`**；執行審批票用 `create_ticket`／`get_ticket`／`list_tickets`／`approve_ticket`／`execute_approved_ticket`，**單鍵 `ticket_id`、無 `project` 參數**。
- **機械判準**：查詢本規則所治理之票（`Pending/`／`deliver/` 下票檔所對應者）**MUST** 用 `registry_list`；**MUST NOT** 用 `get_ticket`。
- **誤用簽名**：`get_ticket` 對治理冊面票**恆回 `NOT_FOUND`**——該回應為正確行為（查的是另一個資料庫），**MUST NOT** 被解讀為「該票未入冊」或「registry write-back 未完成」。
- **APP 端專屬約束**：本專案為隔離專案，`registry_append_note` **恆回 `REGISTRY_CROSS_PROJECT_REF`**，敘事註記唯一合法通道為 `registry_set_checkpoint`，且敘事欄位 **MUST** 置於 `checkpoint_data` 物件內——未列於該工具 schema 之參數（如頂層 `summary`）會被**靜默丟棄且仍回 `SUCCESS`**，故寫入後 **MUST** 以 `registry_list fields=["checkpoint"]` read-back 驗證（2026-09-02 實測：頂層 `summary` 寫入後 `checkpoint` 為 `{}`）。
- **深入參照**：`mcp-ticket-platform` repo 之 `docs/deliver/archive/OTHERS/tool-contracts.md` L404-406 與 L598 起（**跨 repo 唯讀參照**；本專案不持有該檔）。

## checkpoint 生命週期與 detail 檔

1. `checkpoint` 為**工作載體非耐久載體**；`state=OPEN` 時 `CLOSED` 轉態被拒；轉態成功後清為 `null`（PLT `tool-contracts.md:646`）。
2. `detail_content` 落 **PLT repo** `.tmp/checkpoints/{ticket_id}.md`（**非** APP 專案目錄），性質 transient，`CLOSED` 時 unlink（best-effort，失敗僅回 `warning`；`tool-contracts.md:664`／`719`，`registry_service.py` L888-895）。
3. **`TICKET_REGISTRY.json` `_schema` 註解之 `detail_ref: reports/checkpoints/<TICKET_ID>.md` 為過期字面**，實際落點見第 2 點；契約原文以 PLT `tool-contracts.md` 為準。**本檔不得直改 `TICKET_REGISTRY.json`**（`Instructions_for_Claude` 明禁），僅此記載。
4. 承本檔 `.tmp/` 產物不得作長期證據之原則（見 `## Ticket Rigor Gate` 節）：`.tmp/checkpoints/` 屬之。
5. **誤判防護**：查證 registry 工具行為時 MUST 讀 PLT `tool-contracts.md` 原文，**MUST NOT** 以 APP 端 schema 註解或工具回傳值形狀推定契約。

## Ticket Runtime Transition Table

> 本節為 Ticket runtime 下一步之**唯一 GO lane**；`/uc`／`/qa`／`/sv` 僅得引用本節，**MUST NOT** 各自複製 GO predicate 副本。本節 **MUST NOT** 定義新 lifecycle 值、新 approval／verdict／receipt 枚舉或新 registry schema——沿用 §Ticket 生命週期流程（五段）既有五段語義與 `reviews` 四欄。

### 控制詞（封閉四值）

- `MUST AUTO-CHAIN`：無需人類確認，立即接續下一步。
- `MUST STOP`：停止並回報，等待裁決。
- `MUST REQUEST APPROVAL`：向使用者請求核准後方可續行。
- `MUST RETURN`：本次呼叫正常返回，無新動作（冪等）。

以上四值為 next-step 執行語義之**唯一合法表達**；**MUST NOT** 以「→」、「準備」、「建議下一步」等敘事詞承擔執行語義。

### 不變量（四條）

1. **對話模式非 lifecycle**：`/uc`／`/normal`／`/compact` 之切換 **MUST NOT** 改變已啟動 Ticket 之 required next transition；lifecycle 狀態綁定票務本身（回指 §Ticket 生命週期流程（五段）），非對話模式之屬性。
2. **禁自行擴張工作**：Agent **MUST NOT** 由 MEMORY／PROGRESS／TODO／FIXME／`Pending/` 清單或歷史對話自行選取、開啟、排序或擴張另一張票（回指 `CLAUDE.md` Rule 4b）。
3. **唯一決定即 AUTO-CHAIN**：當前票之下一步若由 Header／Verify Config／Action Items／AC／gate receipt 唯一決定，Agent **MUST AUTO-CHAIN**；此非「自行開啟新工作」，不受不變量②拘束（回指 `CLAUDE.md` Rule 4b 但書）。
4. **Agent 寫入上限**：Agent 最高可寫 lifecycle 為 `REVIEW`；`CLOSED` 限審查方 SMTR/CSO（回指 §五段觸發與寫入者⑤）。

### 轉移表

| 已知事實 | MUST 執行 | MUST NOT |
| --- | --- | --- |
| RAPID MODE ON（QA/SV 已豁免） | Ticket DONE 輸出當下即為 closeout 觸發點，`MUST AUTO-CHAIN` 進 Agent closeout | 等待額外確認才觸發 closeout |
| RAPID MODE OFF | 觸發點為 SV PASS（Cross-Review 通過），`MUST AUTO-CHAIN` 進 Agent closeout | 在 SV PASS 前提早 closeout |
| Action Items 未完成 | `MUST AUTO-CHAIN` 續作下一個未完成 Action Item | 跳過未完成項宣告 Ticket DONE |
| Action Items 完成 → QA | `MUST AUTO-CHAIN` 呼叫 `/qa` | 停下等待使用者下達 `/qa` |
| QA PASS → SV | `MUST AUTO-CHAIN` 進 `/sv`（依既有 Auto-Chain 契約） | 停下等待使用者下達 `/sv` |
| QA FAIL | `MUST STOP`，回報失敗項 | 自行修復後靜默重跑並宣告 PASS |
| SV 或 Codex 非 PASS（BLOCKER／REPORT_FIX） | `MUST STOP`，依 SV／Cross-Review 既有契約處理 | 由 UC 自動重跑或改判（回指 `uc/SKILL.md` 規則 9） |
| SV Cross-Review `ADVISORY`，或 `codex` 不可用致降級為 `SKIPPED` | **無 BLOCKER／FAIL finding 時**視同 PASS，`MUST AUTO-CHAIN` 進 Agent closeout；存在任一 BLOCKER 或 FAIL finding → `MUST STOP`（Akira 2026-09-15 追認並加此條件；registry `reviews.codex` 維持既有 `PASS` enum，**實際 verdict MUST 落 checkpoint** 之 `codex_verdict_actual`）（`ADVISORY` 為非阻斷觀察記錄，`SKIPPED` 為降級純報告驗證，均非失敗，依 `cross_review.sh`／`sv/SKILL.md` 既有 exit 0 契約） | 因 `ADVISORY`／`SKIPPED` 而停工、要求人工介入或視為 BLOCKER |
| SV＋Codex PASS 且 `Traycer Workflow: N/A` | `MUST AUTO-CHAIN` 進 Agent closeout 至 `REVIEW`，`reviews.traycer=N_A` | 詢問或操作 Traycer |
| SV＋Codex PASS 且 `Traycer Workflow ≠ N/A` | `MUST AUTO-CHAIN` 進 Agent closeout 至 `REVIEW`，`reviews.traycer=PENDING` | 假設可由 agent／CLI／MCP 觸發 Traycer（回指 §Traycer T2 執行邊界）、因 `PENDING` 而停車 |
| 已收束重入（`lifecycle ∈ {REVIEW, CLOSED}` 且票檔位於 `deliver/`） | `MUST RETURN`（見下「最小冪等回傳」） | 重寫 status、二次搬移票檔、二次寫等價 checkpoint、建空 commit |
| Header 缺失或非法（`Traycer Workflow` 非封閉四值、`ALLOWED`／`FORBIDDEN` 無法解析） | `MUST STOP`，列出具體缺失欄位 | 猜測值續行 |

### Traycer 語義

- `Traycer Workflow: N/A` → 不詢問、不操作，寫 `reviews.traycer=N_A`。
- `Traycer Workflow ∈ {architectural_review, execute_review, strict_verification}` → **MUST NOT** 假設可由 agent／CLI／MCP 工具鏈觸發封閉 VS Code plugin（回指 §Traycer T2 執行邊界），寫 `reviews.traycer=PENDING` 並完成 Agent closeout 至 `REVIEW`。
- 缺失或未知值 → `MUST STOP`，不寫猜測值、不轉態。
- **`PENDING` MUST NOT 被當作 blocker**——lifecycle 正常停留於 `REVIEW`，等待人工 Traycer 複審與終審方轉 `CLOSED`。

### 最小冪等回傳

`registry lifecycle ∈ {REVIEW, CLOSED}` **且**票檔實際位於 `deliver/` → `MUST RETURN`（已收束）：**MUST NOT** 再呼叫 `registry_set_status`／`registry_upsert_ticket` 轉態、**MUST NOT** 再搬移或 unlink 票檔、**MUST NOT** 再寫等價 checkpoint、**MUST NOT** 建立空 commit。二者不一致（如票檔已搬 `deliver/` 而冊面 `file` 仍指 `Pending/`）→ 交既有 Closure Gate 判定（回指 §Ticket Closure 紀律「矛盾停止」），Agent **MUST NOT** 猜測修復。

## Session Persistence 邊界

1. **可見性**：尚未持久化之收尾異動，以 `git status --short` 之 APP 工作樹 tracked dirty 集合，與 `registry_list` 讀得之 `lifecycle`／`file` 現值**共同觀測**；此二者即唯一觀測面，**MUST NOT** 另設狀態檔。
2. **Artifact set 範圍**：含本 session 已收束票之 ①`Pending/` → `deliver/` 票檔移動、②`.claude/PROGRESS.md`／`.claude/PROGRESS_ARCHIVE.md` 同步、③`plans/cso_briefing/project/TICKET_REGISTRY.json`。**registry 之雙面事實 MUST 明載**：其**合法寫入通道**為平台 API（`registry_set_status`／`registry_set_checkpoint` 等，**MUST NOT** 以檔案寫入直改，回指 §治理載體↔存取工具對應圖譜），但其**檔案落點在 APP repo 內**且已列於 §Governance Artifact 清單（`[GOVERNANCE-SYNC]`），故仍由既有治理 artifact persistence 納入。**MUST NOT** 因「寫入通道在平台側」而將其排除於 APP artifact set 之外。
3. **時效**：未持久化之 APP closeout artifact **MUST NOT** 跨 session；若收尾當下未走軌道 A（見第 5 點），則 `/handoff`、主題切換或明確結束 session 前 **MUST** 執行 `/compact closeout`。已隨票即時提交者不受本條拘束（已持久化）。
4. **驗證**：任一軌道於 commit 前 **MUST** 執行 scoped `git_diff` 零意外變更驗證（軌道 B 另含既有 Closure Gate）；任一失敗 → 禁止提交，**MUST NOT** 將未驗證之治理異動帶過 session 邊界。
5. **雙軌持久化（Akira 2026-09-15 裁決；取代原「路徑指認」單軌條款）**：已完成 Ticket 之 closeout artifact **得擇一**持久化，兩者均合法，執行者自行選擇，**MUST NOT** 因選擇其一而被判違規：
   - **軌道 A — 隨票即時**：以 `[<TICKET_ID>]` 單票前綴立即 commit。**MUST** 為單票，**MUST NOT** 混入他票 artifact（`compact/SKILL.md` 之多票合併禁令與 TPL 三分流 R2 之 commit 前綴歸因**不受本節修改**）。
   - **軌道 B — session 邊界**：延至 `/compact closeout`，走 `## Auto Commit: Governance Artifacts` 表驅動路徑；其 prefix、pathspec、逐檔處理、同 prefix 合併與 zero-diff SKIPPED 語義依 §Governance Artifact 清單與 `compact/SKILL.md` 既有規則執行。
   **唯一禁止**：將**無法歸屬於已收束 Ticket** 之 tracked dirty change 混入任一軌道之保存集合。除此之外本節**不新增任何提交授權**，亦 **MUST NOT** 擴義 `<TICKET_ID>` 佔位符值域為多票列舉。
   **立法事由**：`ACAGOV-RUNTIME-TABLE-01` 自身收尾即走軌道 A（commit `2fbc017`），與原單軌 MUST 相衝；Akira 裁決不追罰、不 revert，改撤該 MUST——本地 commit 為最廉價之回退點，強制延後反降低可回復性（可逆優先）。
6. **Governance Artifact 清單補列**：於 §Governance Artifact 清單表新增一列 `plans/cso_briefing/project/deliver/*.md` → `[GOVERNANCE-SYNC]`（說明欄：已交付票檔，`REVIEW`／`CLOSED` 之物理歸位產物）。依該表節首「新增 Governance Artifact 只需在此表加一行，**MUST NOT** 另開票或單獨改 SKILL.md 判斷邏輯」之既有授權執行（已於本檔 §Governance Artifact 清單表落地，見該表列）。**不補此列則票檔搬移將落在 `compact` Scope 盤點之表列外而永不提交，屬靜默失效。**

## Mid-Execution 回報前雙軌同步義務

任一對話模式（`/uc`、`/normal`、或未切換模式之預設對話）下，執行者於本回合完成任一 Action Item 或 Acceptance Criteria 後，若即將停下回報、詢問下一步、或結束一輪工具呼叫序列，**MUST** 同批完成雙軌同步，缺一律屬未完成：

1. **本地票檔軌**：對應項目由 `- [ ]` 改為 `- [x]`，並附完成依據（commit hash／驗證方法）。`Pending/` 票檔縱因 `.gitignore` 而 untracked，仍 **MUST** 執行——untracked 不構成免做理由。
2. **Registry 軌**：以 `registry_list(ticket_id=...)` 確認該票已入冊時，**MUST** 呼叫 `registry_set_checkpoint` 將本回合完成範圍、對應 commit hash（如有）、尚未完成項目與其原因寫入 checkpoint，並依既有規範以 `registry_list fields=["checkpoint"]` read-back 驗證。

**本義務不因對話模式而免除**——回指 §Ticket Runtime Transition Table 不變量①「對話模式非 lifecycle」之同一原則：模式僅決定回報語氣與詳略，不決定紀錄義務是否存在。僅完成軌 1（本地勾選）而未同步軌 2（registry）→ 對 Akira 而言等同「什麼都沒發生」（TKR 系統視圖為其唯一可信來源），**MUST** 視為回報不完整，不得宣稱已如實揭露進度。

觸發來源：`RGR-INTERACTIVE-CLOSURE-AUTH-01` 執行期間，Action Items 與 AC checkbox 僅勾入本地 untracked 票檔、未同步 registry checkpoint，導致 Akira 由 TKR 系統視角判讀為「全數未完成」，與執行端本地認知不同步（Akira 2026-09-25 對話裁決，免票直接授權補此條）。

## Ticket Closure 紀律

- **勾選紀律**：碼閉環 + `/sv` PASS（Codex Cross-Review 終局 PASS）後，主 session **MUST** 逐項勾選母票 Action Items 與 Acceptance Criteria；僅勾「有 SV / QA / commit 佐證」之項；`[行為性]` / `[實機]` 未實測項 **MUST** 保留未勾並於 PROGRESS.md 誠實揭露（K5-Rule 29）。**MUST** 於該項目實際完成的同一回合內立即勾選（Edit 落地 / 測試轉綠 / Codex PASS / commit 成功發生當下），**MUST NOT** 留待後續 /sv Step 10 之外的另一輪「確認完成度」請求才回頭補勾。
- **搬移紀律**：勾選完成後，票檔 **MUST** 由 `plans/cso_briefing/project/Pending/` 搬移至 `plans/cso_briefing/project/deliver/`。
  **凍結點語義**（INFRA-TRACKSCOPE-01，2026-08-27）：`Pending/` 期票檔為 untracked，搬移至 `deliver/` 之首次 commit 即該票檔進入版控之凍結版本，此前修訂史不入 git log。
- **修補票紀律**：母票存在未閉環 AC → **MUST** 同步產出修補票（`-FIX-[X]` 後綴或後續票）置於 `Pending/`，載明追蹤條目與轉 PASS 條件。
- 母票 AC 佐證不明（無法判定該勾或保留）→ **MUST** 停止勾選該項並列入修補票追蹤，禁止推定勾選。
- **冊面轉態紀律**：票檔層三步（勾選／搬移／修補票）完成後，`TICKET_REGISTRY.json` 對應條目 **MUST** 同步完成**四要件**——①`lifecycle` 轉終態；②`reviews` 四欄（qa／sv／codex／traycer）各填實測值，無可考記錄者填 `N_A` 並於 checkpoint 揭露豁免依據，**MUST NOT** 以推定補 `PASS`（撤警正向取證律）；③`file` 欄指標由 `Pending/` 改指實際落點 `deliver/` 或 `archive/`；④收尾實錄之耐久載體為**票檔（`deliver/`）＋vault 決策檔**；`checkpoint` 為收尾前之工作載體，依 PLT 契約於 `set_status(CLOSED)` 轉態時清為 `null` 並 unlink `.tmp/checkpoints/{ticket_id}.md`（見「checkpoint 生命週期與 detail 檔」節）。**轉終態前**，收尾實錄 MUST 已落票檔；若需保留冊面可見之收尾摘要，MUST 於 `set_status(CLOSED)` **之後**以 `registry_set_checkpoint` 回填（此序不改變已轉 `CLOSED` 之 `closed_at`）。
- **原子性**：四要件 **MUST** 於同一收尾回合內完成，**MUST NOT** 分批跨 session；任一要件缺漏即視為該票**未收尾**，不得回報結案。`CLOSED` 情形之合法時序為「②③備妥＋④收尾實錄落票檔（四要件之必要部分，轉態前完成）→ ①`set_status(CLOSED)` 執行轉態 →（若需）checkpoint 摘要回填（④之選填延伸，非四要件計數對象）」，該序**不違反**本條之同回合要求。冊面異動之 APP Git persistence 依 §Session Persistence 邊界第 5 點**雙軌擇一**執行（隨票即時 `[<TICKET_ID>]` 單票 commit，或延至 `/compact closeout` 走 `[GOVERNANCE-SYNC]`）；scoped `git_diff` 零意外變更之驗證義務隨該軌道執行，**MUST NOT 取消**。
- **矛盾停止**：票檔層與冊面層狀態不一致（如票檔已搬 `deliver/` 而冊面 `file` 仍指 `Pending/`）→ **MUST** 停止回報並列入待裁，禁單方推定何者為真。
- **VOID 歸位紀律**：票務判定作廢時 **MUST** 完成四要件：①票檔移 `plans/cso_briefing/project/void/`；②冊面 `file` 欄同步改指 void 路徑；③以 `registry_set_checkpoint`（敘事欄位置於 `checkpoint_data` 物件內）載明作廢事由，**MUST NOT** 使用 `registry_append_note`——依本檔「治理載體↔存取工具對應圖譜」節既有記載，該工具對本專案恆回 `REGISTRY_CROSS_PROJECT_REF`，寫入必定失敗；寫入後 **MUST** 以 `registry_list fields=["checkpoint"]` read-back 驗證欄位確實落地（防頂層鍵位靜默丟棄，同節既有規範）；④**復活紀律**——`VOID` 為終態，議題日後重啟 **MUST** 開新票，**MUST NOT** 改回原票 `lifecycle`；原解凍／續辦條件自動降級為新票之開票前置檢核，既有產物留存不刪，供新票直取為 `evidence_dependency`。
  **平台限制**：`registry_upsert_ticket` 對已歸檔票回 `REGISTRY_ARCHIVED_TICKET`，且 `registry_unarchive` 平台**未實作**（PLT `INFRA-REGISTRY-API-GAPS-01` 明示範圍收斂不實作）——故「開新票」非風格建議而是**平台強制**。
- **Dashboard 慣例同步**：`.claude/PROGRESS.md`／`PROGRESS_ARCHIVE.md` 之例行 Dashboard 列更新得與實作同 commit 或以 `[COMPACT-SYNC]` 獨立提交，**不計入**票面「git diff --stat 僅涉 ALLOWED」AC 之違規判定（INFRA-GOVSYNC-01 定調，消解 RGR-APPDEPLOY-01 G2 帳實矛盾）。
> **強制層級（誠實定性）**：本節「冊面轉態紀律」為**紀律層**條款，非機械閘。冊面轉態發生於 Desktop 治理 session，而 `verify_checklist.sh` / `structural_verify.sh` 執行於 Claude Code 端——**閘門位置與動作發生位置不重合**，腳本層對本失效型無攔截力（`INFRA-TPL-V32-01` 事故全程未經 Claude Code）。真正之機械強制須由 mcp-ticket-platform server 端提供（`lifecycle` 轉終態前 `file` 欄指標所指票檔 MUST 實際存在），屬 TKR-05 平台原語域，依前置資源恆存律**不得由本票預支**；在該閘落地前，本節效力等同宣告式紀律，違反不會被工具攔截。`checkpoint` 於終態轉換時被清空為 PLT 刻意設計（見「checkpoint 生命週期與 detail 檔」節），APP **MUST NOT** 反向要求其非空。
- 觸發來源：INFRA-CLOSURE-SOP-01（Akira 2026-07-17 對話裁決：全票適用之標準收尾流程；實例缺口 INFRA-AGENTS-CR-02 Done 但票內 checkbox 全未勾）；即時勾選條款觸發來源：`INFRA-QA-COMMIT-EMPTYDIFF-01` + `-FIX-A`（碼閉環與 Codex PASS 數日後才因 Akira 詢問完成度而補勾，Akira 2026-07-19 對話裁決授權補訂）。
- **FORBIDDEN 延後項免開票紀律**：票面 `FORBIDDEN` 命中 `.claude/PROGRESS.md` 導致的收尾延後項（Dashboard 同步／`PROGRESS_ARCHIVE.md` Done 標記／票檔 Pending→deliver 搬移／票內 AC checkbox 勾選）**MUST NOT** 另開獨立治理票（如 `*-DOCSYNC-01`）承接；此四項本屬標準 SOP 收尾流程，**MUST** 於下一輪 `/compact` 或後續 session 之正常收尾動作中處理即可。觸發來源：`INFRA-CONCURRENCY-01`（Akira 2026-07-19 對話裁決：「這本來就是標準的 SOP 執行票流程」，避免治理疊床架屋）。
- **結構歧義停損律**(2026-08-01 CSO 確認決議):同一子系統下列任一**字面**成立,MUST 停止開立/受理新修補票並升報 CSO:①同一票修訂版次 ≥ 3(票面 Rev 直取);②結構性死結/自指阻塞 ≥ 2 種(決策檔/Handoff 記載筆數直取);③任一 workaround 額度制用量達上限 50%;④修補深度 ≥ 2(FIX 票之 FIX 票)。凍結僅及修補線,不及執行中之非修補工作;解除條件=外審回流+CSO 存廢三案(退役/降級/續修)裁決落檔。禁語義裁量(「此票很重要」不豁免)。SSOT:vault `2026-08-01_Structural_Ambiguity_Stop_Loss_Law.md`;觸發來源:MEO v1 十輪修補無停損(`MEO_V1_Enforcement_Layer_Decommission`)。

## Ticket Rigor Gate（按需工具，非常駐前置）

> `TICKET_RIGOR` 為 TKR 冊面合法 lifecycle 狀態；其允許轉換、寫入者與後繼狀態依 PLT/TKR 平台之權威定義（見 §Ticket 生命週期流程（五段）①）。APP 不得將此狀態排除、歸零或映射為自建的「審查中」。`.claude/skills/ticket-rigor-gate/` 是按需使用的工具能力，不是一般新票的自動派工前置條件；除非未來存在已驗證的、由固定狀態或欄位觸發的 `ticket-run`／dispatcher consumer，否則不得將「得執行 rigor」作為常駐治理規則。

- 報告與遙測一律落 `.tmp/rigor-gate/`（`RGR-APPDEPLOY-01` L2 裁決：執行期產物，KEEP_N=10 輪替）；**MUST NOT** 於任何票面 AC 引用 `.tmp/` 下 rigor 產物為長期證據——耐久結論唯一載體為票面／冊面收尾實錄。
- 本節不新增任何 MUST 級派工前置；未跑 gate 之票不因此退件。

**翻盤紀錄**：2026-08-27 治理減量裁決（APP 冊 lifecycle 維持 `PENDING / REVIEW / CLOSED / VOID` 四態之刻意裁剪）由 2026-09-06 Akira D-1 裁決推翻，翻盤型態＝明示 supersede：PLT／TKR 平台為 lifecycle、狀態轉換、registry 資料契約與共通票務流程之唯一語義權威，APP 自此採 PLT 五段語義（見 §Ticket 生命週期流程（五段）），採取最小必要之 consumer 實作而非重寫平台 enum 或移植全套 script。（`INFRA-LIFECYCLE-SSOT-01`）
