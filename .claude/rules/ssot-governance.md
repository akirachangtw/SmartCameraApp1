---
description: 治理規則 SSOT — Ticket 生命週期、Transition Table、Autonomous Engineering Gate、Closure、PROGRESS 紀律
---

> RFC 2119 Keywords: **MUST** = 違反即 FAIL; **SHOULD** = 強烈建議，可附理由跳過; **MAY** = 純可選。

# SSOT & Progress Governance

## Governance Artifact 清單（單一真相來源）

`/compact` Auto Commit 步驟 **MUST** 依此表逐檔獨立提交，Scope 汙染判斷式 **MUST** 以此表為準（表列外之 tracked dirty 檔案保留於工作樹、不納入本輪 commit、不阻斷；表列檔案照常逐檔提交，對齊 `compact/SKILL.md` Auto Commit 步驟 1）；新增 Governance Artifact 只需在此表加一行，**MUST NOT** 另開票或單獨改 SKILL.md 判斷邏輯。

| 檔案路徑 | Commit Prefix | 說明 |
| ------- | ------------- | ---- |
| `.claude/PROGRESS.md` | `[COMPACT-SYNC]` | Sprint Dashboard |
| `.claude/PROGRESS_ARCHIVE.md` | `[COMPACT-SYNC]` | Dashboard 歸檔 |
| `plans/cso_briefing/project/TICKET_REGISTRY.json` | `[GOVERNANCE-SYNC]` | 跨票狀態總帳（Manifest/Registry 區分見 `.gitignore` 註解，2026-07-28 CSO 裁決） |
| `.claude/rules/*.md` | `[GOVERNANCE-SYNC]` | 執行者面治理規則載體（含本檔；目錄級表列，新增規則檔免再補表。Akira 2026-08-14 對話裁決：`.claude/rules/` 皆以 `[GOVERNANCE-SYNC]` 提交） |
| `plans/cso_briefing/project/deliver/*.md` | `[GOVERNANCE-SYNC]` | 已交付票檔，`REVIEW`／`CLOSED` 之物理歸位產物（`ACAGOV-RUNTIME-TABLE-01`，§Session Persistence 邊界 AI-2 補列） |

（Akira 2026-07-30 對話裁決：以表驅動判斷 Scope 汙染。）

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
- 引用已刪除路徑 → structural_verify.sh Check F **MUST** 攔截。

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

**落差清單分類**（依現況快照產出，**MUST NOT** 代為處置任何一筆，處置屬後續裁決）：①`Pending/` 有檔而冊面查無（孤兒票檔）；②冊面活態未交付而 `Pending/` 無檔；③冊面 `file` 欄指標與實際落點不符；④`Pending/` 內非票檔文件。

### 五段觸發與寫入者

| 段 | 觸發 | 寫入者 |
| --- | --- | --- |
| ① `TICKET_RIGOR` | rigor gate 開工 | 執行者（併寫 `rigor` 欄；開票或過閘時若判定須深驗，MUST 同批寫 `verification` 欄） |
| ② `IN_PROGRESS` | 開工 | 執行者 |
| ③ `EXECUTED` | AC 自驗全綠，未過 Cross Review | 執行者 |
| ④ `REVIEW` | Cross Review 通過或依 RAPID MODE 省略 | 執行者（**併** `reviews.codex=PASS`／`N_A`＋票檔搬 `deliver/`＋`file` 欄及冊面更新）；Traycer T2 為可選外部審查，不阻擋後續 close |
| ⑤ `CLOSED` | 核心 AC 成立且無 hard blocker（`/ticket-author close` 流程判定） | 執行者（Agent）經 `/ticket-author close <票號>` 寫入（`reviews` 讀回確認＋票檔最小最終結果） |

**物理歸位邊界**（Akira 2026-09-07）：ticket 進入 `REVIEW` 即搬入 `deliver/`；`CLOSED` 保持於 `deliver/`；`VOID` 歸 `void/`。

### verification 與 reviews 之分工

`verification` ＝ 票之**要求**（開票或過閘時決定，屬性）；`reviews` ＝ 閘之**執行結果**（執行時回填）。**close 轉 `CLOSED` 前，核心 AC 所依賴之必要驗證（`verification.required` 中屬核心者）MUST 於 `reviews` 有對應結果；可選外審（Traycer／T2）與非核心項之缺漏依 §Ticket Closure 紀律之披露規則處理，不阻擋 `CLOSED`。**

### §0 waiver 直跳條款

票面帶 Rigor Gate Waiver 者得 `PENDING → IN_PROGRESS` 直跳；`TICKET_RIGOR` 僅過閘票必經。

### Traycer T2 執行邊界

Traycer 為封閉 VS Code plugin，**MUST NOT** 假設可由 agent／CLI／MCP 工具鏈外部觸發。票面聲明 Traycer T2 者，T2 為**可選外部審查**（由人於 VS Code 手動執行）：執行端 **MUST NOT** 推定其 PASS 或另尋機械替代路徑，亦 **MUST NOT** 以其未執行阻擋收束。此類票之收尾 **MUST** 正常轉入 `④REVIEW` 並由 close 繼續；`reviews.traycer` 維持 `PENDING`（或依票面填 `N_A`）；T2 後續若有具體 finding，另開 repair ticket。`EXECUTED` 僅代表「AC 自驗全綠、未過 Cross Review」，**MUST NOT** 用以表示「等 Traycer」。

### Traycer 產出物之票務地位（registry existence-first）

（Akira 2026-09-09 裁決）

1. **草案地位**：Traycer 產出之 ticket 一律為**草案**，`TICKET_REGISTRY.json` 為票務存在與狀態之唯一 SSOT。草案經 SMTR `registry_upsert_ticket` 入冊後方為有效票；未入冊者不具票務效力，**MUST NOT** 派工、**MUST NOT** 作為他票之依賴或證據。
2. **審查存在性前置**：ticket 級審查 **MUST** 以冊面 `ticket_id` 為錨（`registry_list(project, ticket_id)`，禁 `get_ticket`，同 §「治理載體↔存取工具對應圖譜」）。冊面查無此票者，該審查**無效**，**MUST NOT** 產生任何 Tier／verdict 裁決；正確處置為回報「審查對象不存在於冊面」。
3. **零 diff 之判讀前提**：「零 diff ＋ 追蹤器狀態未動 → 判定未執行」僅於審查對象**已入冊且已派工**時成立。缺此前置時，零 diff 為**草案未獲核准之預期結果**，**MUST NOT** 讀作交付偏移或執行失敗。
4. **不新增第二套流程**：本節僅界定草案與有效票之分野，**MUST NOT** 據此新增第二套 lifecycle、verdict 枚舉或 approval 流程；既有五段語義與 `reviews` 四欄不變。

## 治理載體↔存取工具對應圖譜

- **兩套系統明示分立**：治理冊面（`plans/cso_briefing/project/TICKET_REGISTRY.json`，即本規則全篇治理對象）與執行審批票（SQLite，mcp-ticket-platform repo 之 `core/migrations/001_init_v2.sql`）為**互不相通之兩套資料**，同名概念不共享資料。工具由同一台 `mcp-ticket-platform` MCP server 提供，服務兩專案。
- **工具對應表**：治理冊面用 `registry_list`／`registry_upsert_ticket`／`registry_set_status`／`registry_set_checkpoint`／`registry_archive_tickets`，**均需 `project=SmartCameraApp1`**；執行審批票用 `create_ticket`／`get_ticket`／`list_tickets`／`approve_ticket`／`execute_approved_ticket`，**單鍵 `ticket_id`、無 `project` 參數**。
- **機械判準**：查詢本規則所治理之票（`Pending/`／`deliver/` 下票檔所對應者）**MUST** 用 `registry_list`；**MUST NOT** 用 `get_ticket`。
- **誤用簽名**：`get_ticket` 對治理冊面票**恆回 `NOT_FOUND`**——該回應為正確行為（查的是另一個資料庫），**MUST NOT** 被解讀為「該票未入冊」或「registry write-back 未完成」。
- **APP 端專屬約束**：敘事註記可用 `registry_append_note` 或 `registry_set_checkpoint`；用後者時敘事欄位 **MUST** 置於 `checkpoint_data` 物件內——未列於該工具 schema 之參數（如頂層 `summary`）會被**靜默丟棄且仍回 `SUCCESS`**，故寫入後 **MUST** 以 `registry_list fields=["checkpoint"]` read-back 驗證。
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
4. **Agent 寫入範圍**：Agent 可寫 lifecycle 至 `CLOSED`；`CLOSED` 僅得經 `/ticket-author close` 流程寫入（核心 AC 成立且無 hard blocker，見 §Ticket Closure 紀律）。

### 轉移表

| 已知事實 | MUST 執行 | MUST NOT |
| --- | --- | --- |
| RAPID MODE ON（QA/SV 已豁免） | Ticket DONE 輸出當下即為 closeout 觸發點，`MUST AUTO-CHAIN` 進 Agent closeout | 等待額外確認才觸發 closeout |
| RAPID MODE OFF | 觸發點為 SV PASS（Cross-Review 通過），`MUST AUTO-CHAIN` 進 Agent closeout | 在 SV PASS 前提早 closeout |
| Action Items 未完成 | `MUST AUTO-CHAIN` 續作下一個未完成 Action Item | 跳過未完成項宣告 Ticket DONE |
| Action Items 完成 → QA | `MUST AUTO-CHAIN` 呼叫 `/qa` | 停下等待使用者下達 `/qa` |
| QA PASS → SV | `MUST AUTO-CHAIN` 進 `/sv`（依既有 Auto-Chain 契約） | 停下等待使用者下達 `/sv` |
| QA FAIL | 可自修且在既有授權內：`MUST AUTO-CHAIN` 依 §Autonomous Engineering Gate 修復、局部驗證並揭露後重跑 `/qa`（`/qa` PASS 前不進入 `/sv`）；不可自修、人工邊界或停損成立：`MUST STOP`，回報失敗項 | 自行修復後靜默重跑並宣告 PASS |
| SV 或 Codex 判 BLOCKER | `MUST STOP`，依 SV／Cross-Review 既有契約處理 | 由 UC 自動重跑或改判（回指 `uc/SKILL.md` 規則 9） |
| SV／Codex 判 REPORT_FIX | **先判斷是否符合 report-only 閉環**（七條件須全成立：①verdict＝`REPORT_FIX`；②僅修改 `.tmp/sv_report*.md`；③不改 Ticket、SSOT、AC、contract、產品程式碼、`PROGRESS.md`、`CLAUDE.md` 或任何其他 repo 檔案；④修補僅限既有內容重排、必要標題補齊、既有事實之欄位格式校正；⑤不新增測試結果、commit、命令輸出、證據引用、完成宣稱或任何新事實；⑥修補後通過 SV report preflight（`structural_verify.sh` 不帶 `--post`）才重跑 Cross-Review；⑦本次 `cross_review.sh` 以 exit 3 結束，且本次 result 不含 `CHECKPOINT_REPORT_FIX_LIMIT` 或 `REPORT_FIX_ESCALATE`）。成立 → `MUST AUTO-CHAIN`（步驟見 `sv/SKILL.md` 階段 D「REPORT_FIX report-only 閉環」）；任一不成立 → `MUST STOP` 並輸出 `REPORT_FIX_ESCALATE`；第 3 次 → `CHECKPOINT_REPORT_FIX_LIMIT`（exit 2，人工裁決 checkpoint） | 於條件外修改 `.tmp/sv_report*.md` 以外任何檔案、新增事實、超過上限重跑、將 LIMIT 當 BLOCKER 或程式碼修正重試；新增 lifecycle／verdict／receipt 枚舉（上列標記為輸出字面，非 verdict） |
| SV Cross-Review `ADVISORY`，或 `codex` 不可用致降級為 `SKIPPED` | **無 BLOCKER／FAIL finding 時**視同 PASS，`MUST AUTO-CHAIN` 進 Agent closeout；存在任一 BLOCKER 或 FAIL finding → `MUST STOP`（Akira 2026-09-15 追認並加此條件；registry `reviews.codex` 維持既有 `PASS` enum，**實際 verdict MUST 落 checkpoint** 之 `codex_verdict_actual`）（`ADVISORY` 為非阻斷觀察記錄，`SKIPPED` 為降級純報告驗證，均非失敗，依 `cross_review.sh`／`sv/SKILL.md` 既有 exit 0 契約） | 因 `ADVISORY`／`SKIPPED` 而停工、要求人工介入或視為 BLOCKER |
| SV＋Codex PASS 且 `Traycer Workflow: N/A` | `MUST AUTO-CHAIN` 進 Agent closeout 至 `REVIEW`，`reviews.traycer=N_A` | 詢問或操作 Traycer |
| SV＋Codex PASS 且 `Traycer Workflow ≠ N/A` | `MUST AUTO-CHAIN` 進 Agent closeout 至 `REVIEW`，`reviews.traycer=PENDING` | 假設可由 agent／CLI／MCP 觸發 Traycer（回指 §Traycer T2 執行邊界）、因 `PENDING` 而停車 |
| Agent closeout 於本次呼叫實際完成（lifecycle 由本次寫入達 `REVIEW`、票檔已在 `deliver/`、checkpoint 已 read-back；非「已收束重入」分支），且同時成立：①存在唯一合理候選的成果文件（候選集合為 `projman` Skill〈成果文件定位規則〉之輸出；歸屬依三訊號綜合判讀：被收束票文字明確指名該文件或其議題識別、文件宣告的議題識別或票前綴與票 ID 或 registry topic 相符、票的實際交付〔ALLOWED 內 diff、AC〕對應該文件某階段的預期功能；票前綴僅輔助，缺前綴舊文件不整類排除）；②被收束票的實際交付使該文件某階段 ID 達到可檢查點（即 `projman` Skill〈align〉之使用時點），且該 ID 自其錨點起有未盤點的新交付。觸發與 `/qa`、`/sv` 是否依風險分級省略無關 | `MUST AUTO-CHAIN` 呼叫 `Skill(projman)` align，並於同一回合顯示具體 align 提示詞、實際執行；採軌道 A 者先完成當次 `[<TICKET_ID>]` 單票收尾提交，採軌道 B 者不為此提前 `/compact`。授權（逐字；「本票」＝被收束之票）：「本票正常收束後，符合條件的 Projman 校正依本條取得獨立的成果文件讀寫及單檔保存授權；範圍僅限已唯一定位的成果文件與其現況回填，不擴張原票實作範圍，不修改其他檔案、產品需求、Registry 或 lifecycle。」候選集合、每 ID 範圍與預設錨點、回填、回報項目與自動／手動判別依 `projman` Skill〈成果文件定位規則〉與〈被治理條款呼叫時（行為契約）〉，本列不複製；`git log` 僅輔助列示候選交付。保存、核對與回報依 `projman` Skill〈寫入與保存〉（前綴 `[PROJMAN-SYNC]`；寫前來源證明與略過、四態與無差分字面、寫後 blob id 皆在該節），本列不複製。被收束票 `FORBIDDEN` 中僅列檔名而無其他限制語意者，視為票內範圍排除、不阻擋本列；其他明確禁止、權限拒絕與安全限制仍遵守，無法排除實質衝突時略過本次 align 並回報。只消費既有證據，不新增付費呼叫，預設不新增本地建置或測試。回報字面（輸出字面，非 verdict／receipt 枚舉）與已知限制依 `projman` Skill〈寫入與保存〉第 5、6 項 | 在「已收束重入」分支觸發；左欄任一條件不成立時猜測歸屬或強行執行（本列不適用；該一行回報依 `projman` Skill〈被治理條款呼叫時〉第 1 項之定位回報判讀，含「定位未完成」與不輸出之情形，本列不複製）；以票前綴、提交前綴或目錄排除作為唯一資格，或整類排除 docs-only／規則型成果；遞迴（align 非 Ticket 收束，其提交不帶票前綴，不再觸發 align）；修改成果文件以外的檔案、Registry 或 lifecycle；以 `--allow-empty` 或補交 hash 製造提交；因失敗而回滾原票、還原檔案、重試迴圈、開 Gate incident（回指 §Autonomous Engineering Gate），或回頭視為原票或下一張票未收束；宣稱漏跑必能補判（漏跑僅得稱「可能於後續相關收束時補判」）或本鏈路已有機械後盾 |
| Agent closeout 至 `REVIEW` 於本次呼叫實際完成，或「已收束重入」判得 `lifecycle = REVIEW`（若上一列 projman align 條件成立，先依該列處理） | `MUST AUTO-CHAIN` 呼叫 `/ticket-author close <票號>`（一次；close 不自行重呼叫自己） | 逐票等待 Owner 批准、以 `REVIEW` 為停點、close 未達 `CLOSED` 後反覆重呼叫 |
| 本輪最後一張票的 `/ticket-author close` 結果已知後（限本次呼叫鏈實際執行了 close），或向使用者給出議題最終交付總結前（「最後一張」依 `projman` Skill〈最終覆蓋確認〉綜合判斷，Registry 無 OPEN 票不構成完成），且該議題存在相關候選成果文件（含被作廢清單排除者）或使用者已明示成果目標；後一觸發不經 Step 11、重入情形亦可執行，僅讀取票務事實，不改變「已收束重入」列對 closeout 的處置 | `MUST AUTO-CHAIN` 呼叫 `Skill(projman)` 依〈最終覆蓋確認〉確認成果表是否已涵蓋最新交付與審查結論：有新證據才 align，否則回報「已涵蓋」；有必要功能或驗收缺口尚未開票時於最終報告揭露。授權（逐字；「本輪最後一張票」＝依上述判斷之最後一張票）：「本輪最後一張票收束後，符合條件的 Projman 最終覆蓋確認及其 align 依本條取得獨立的成果文件讀寫及單檔保存授權；範圍僅限已唯一定位的成果文件與其現況回填，不擴張原票實作範圍，不修改其他檔案、產品需求、Registry 或 lifecycle。」定位、寫入、核對、保存與回報依 `projman` Skill〈寫入與保存〉與〈被治理條款呼叫時（行為契約）〉，本列不複製；只消費既有證據，不新增付費呼叫，預設不新增本地建置或測試 | 逐票重跑覆蓋確認、在非最後一張票觸發；因 Registry 無 OPEN 票即斷定完成；自行開立新工作或新票以補缺口（僅揭露）；遞迴（確認或其保存不是 Ticket 收束，不再觸發另一輪 align）；修改成果文件以外的檔案、Registry 或 lifecycle；以 `--allow-empty` 或補交 hash 製造提交；因失敗而回滾原票、還原檔案、重試迴圈或開 Gate incident（回指 §Autonomous Engineering Gate）；宣稱漏跑必能補判（漏跑僅得稱「可能於後續相關收束時補判」）或本鏈路已有機械後盾 |
| 已收束重入（`lifecycle ∈ {REVIEW, CLOSED}` 且票檔位於 `deliver/`，含 `deliver/archive/`） | `lifecycle = CLOSED`：`MUST RETURN`（見下「最小冪等回傳」）；`lifecycle = REVIEW`：closeout 部分 `MUST RETURN`（不重做），並接續上列 close 列 | 重寫 status、二次搬移票檔、二次寫等價 checkpoint、建空 commit |
| Header 缺失或非法（`Traycer Workflow` 非封閉四值、`ALLOWED`／`FORBIDDEN` 無法解析） | `MUST STOP`，列出具體缺失欄位 | 猜測值續行 |

### Traycer 語義

- `Traycer Workflow: N/A` → 不詢問、不操作，寫 `reviews.traycer=N_A`。
- `Traycer Workflow ∈ {architectural_review, execute_review, strict_verification}` → **MUST NOT** 假設可由 agent／CLI／MCP 工具鏈觸發封閉 VS Code plugin（回指 §Traycer T2 執行邊界），寫 `reviews.traycer=PENDING` 並完成 Agent closeout 至 `REVIEW`。
- 缺失或未知值 → `MUST STOP`，不寫猜測值、不轉態。
- **`PENDING` MUST NOT 被當作 blocker**——Agent closeout 後 lifecycle 為 `REVIEW`，由 close 繼續判定並轉 `CLOSED`；T2 為可選外審。

### 最小冪等回傳

`registry lifecycle = CLOSED` **且**票檔實際位於 `deliver/`（含 `deliver/archive/`；冊面 `file` 過期者以實檔位置為準）→ `MUST RETURN`（已收束）：**MUST NOT** 再呼叫 `registry_set_status`／`registry_upsert_ticket` 轉態、**MUST NOT** 再搬移或 unlink 票檔、**MUST NOT** 再寫等價 checkpoint、**MUST NOT** 建立空 commit。`lifecycle = REVIEW` 且票檔位於 `deliver/` 時，closeout 動作（搬移票檔、等價 checkpoint）視為已完成而不重做，但 **MUST NOT** 因此阻擋 `/ticket-author close`（其轉 `CLOSED` 不屬 closeout 重入）。二者不一致（如票檔已搬 `deliver/` 而冊面 `file` 仍指 `Pending/`）→ 交既有 Closure Gate 判定（回指 §Ticket Closure 紀律「矛盾停止」），Agent **MUST NOT** 猜測修復。

## Autonomous Engineering Gate

> 契約狀態：本節為契約文字；現況與宣告限制見 §實作狀態與宣告限制。入口 `check` 呼叫路徑屬模型契約，不被機械阻擋；agent **MUST NOT** 宣稱完整 Gate，**MUST NOT** 宣稱 hook 涵蓋途中違規。

### 詞彙域與不外溢條件

- Gate 詞彙＝`HUMAN_REQUIRED` 及其類別與 `basis`、PPSS 證據三態（`RETRIEVED`／`SOURCE_READ`／`INVALID`）、ledger 事件型別（`finding`／`round`／`step_limit`／`consult`／`missed_check` 等），僅存活於 Gate 子系統。
- 不外溢條件（僅針對 Gate 詞彙，非全檔枚舉之通用定義）：①不寫入 registry `lifecycle`／`reviews`／`file`／`checkpoint` 結構化欄位、不構成 registry schema（`checkpoint` 敘事摘要與票檔收尾實錄除外）；②不成為 Transition Table 控制詞或任一 Table 列之轉移條件（Table 節對本節之回指除外）；③不作為 approval／review verdict 於票或審查者之間交換。本節不新增 lifecycle 值、approval／review verdict 或 registry schema。
- 一致性依據：Transition Table 節首引言之限制字面範圍為其「本節」；本節為獨立節並受上述不外溢條件約束，將該限制改為全檔通用屬另案修訂。

### Gate 適用條件與順序

- 適用：active（經 `activate_ticket.sh` 啟動、`tmp/verify/active.json` 含 `ticket_id`）且非終態（registry lifecycle ∉ {`REVIEW`,`CLOSED`,`VOID`}）之 ticket；**本 Gate 適用範圍排除 `BLOCKED`**（不要求續作、不解除、不恢復編排；`BLOCKED` 不是 lifecycle 終態）。工作途中遇到失敗或不確定時即依本節順序處理，不待回合結束；回合結束、請示或交棒前的檢查維持。不攔使用者下達之 `ticket-run`、`/compact`、`/clear`，但計數與恢復義務不因此排除：clear／compact／resume 後若有未結 incident，下一個決策點前先讀其狀態。
- 順序：①入口檢查；②遇失敗記 `finding`（不開輪）；③已授權可執行者直接執行，不轉為請示；④未知先本地取證；⑤仍不能解，主 agent 諮詢 Advisor（僅裁決本地可推導之設計／修法，不裁決產品語義、SSOT、scope、權限、不可逆動作）；⑥需外部事實才用 PPSS；⑦以新且已授權策略開輪、修復、局部驗證；⑧輪 fail 後判斷續輪或升報；⑨權限被拒記 `PERMISSION`，不得改道重試同一被拒動作。

### Incident 與輪次

- incident 身分＝`ticket_id`×subject（根因對象）；verdict 類別與 gate point 為屬性，不入身分。verdict 改變不自動開新 incident，同根因續算；僅有獨立問題證據（獨立失敗輸出）且 subject 不重疊才分開。
- 一輪＝以一個新且已授權策略完成之完整解題循環（取證→Advisor→PPSS→修復→局部驗證）。初次 finding、單純執行腳本、步驟級上限事件皆不是輪。
- 逐層計輪：round 依**策略來源層**計入 `executor`、`r0`（僅 cross-review 路徑）、`advisor`，**每層最多 3 次**實質修復＋回驗；各層互不共用、互不重置；驗證通過即結案，不要求湊滿；某層有新且已授權策略時，任何其他機制不得提前截斷該層。本層無法判定下一個安全行動時，不必先用盡本層輪數即可升下一適用層（取證記 `consult --kind local`）；某層耗盡或回報無合法策略，**必須**升下一適用層，尚未 engaged 的 Advisor 不得視為已處理；提早升級後不要求回低層補滿。同 incident 首個 `--layer advisor` round 前須有至少一筆**同 incident** 的 `consult --kind advisor`（`--layer r0` 同理須 `--kind r0`）；後續該層 round 不要求新 consult，其他 incident 的 consult 不適用。機械檢查只證存在紀錄，不證真實調用；真實性與時序依 transcript 原生 `advisor` 調用與結果（API 顧問則依其 receipt 檔、`receipt_sha256` 與 `request_id`），策略與結論對應由模型負責，不新增 transcript 解析機制。最終適用層（`advisor`）3 次 fail 且無合法續作路徑→`EXHAUSTED`；`advisor` 層未耗盡而無可行已授權策略→`NO_AUTHORIZED_STRATEGY`。
- 工具使用（同一 incident）：Advisor 僅在本層無法判定時由主 agent 呼叫，新的不確定且附新證據才再諮詢，同一次結論可供不同合法策略使用；不允許自判掛名 Advisor，不以任意歷史 consult 開輪。PPSS 僅限外部事實缺口：同一 incident 同一缺口預設定向研究一次，消費已有證據，不重送同題；不因換查詢措辭或進下一 round 自動獲得研究額度；證據回流同一 incident，於後續 round 的策略引用消費，不新增或重置任何層額度。換策略、phase、模型或改名不重置額度，不同 incident 各自計。helper 允許的額外調用（例如第 2 次 PPSS 附新查詢策略）不等於本 Gate 授權使用。Advisor 建議與研究結果不構成新增授權；無新證據的相同命令、prompt、研究問題、patch 不重播。以上屬模型契約，不由機械控制強制（機械僅：per-layer 輪數上限、策略去重、首個 advisor／r0 層 round 前的同 incident consult 紀錄）；hook 僅於 reason 顯示用量。
- Gate 的「輪」≠ `cross_review.sh` BLOCKER `ROUND` ≠ RGR review round／session-local closure iteration，各自獨立計。

### 步驟級上限與 LIMIT

- 步驟級上限（`uc` 連續 2 次修復仍編譯失敗、BLOCKER budget、REPORT_FIX 閉環／`CHECKPOINT_REPORT_FIX_LIMIT`、QA 編譯重試等）到達＝該路徑終止，記 `step_limit` 並作為該輪局部驗證失敗之輸入；不重置 incident 計數，不自動等於最終適用層耗盡之 `EXHAUSTED` 或 `NO_AUTHORIZED_STRATEGY`，不自動升報；各步驟級數字不由本節改動。
- LIMIT 處置：先停止該腳本受限重試（超過 LIMIT 再重跑不是已授權策略，exit semantics 不變）→核對各層剩餘輪數與已授權策略→有可行策略即續作→經本地取證與 Advisor 確認無策略才升報。不清 state（含 `.tmp/cross_review_state`），不換 incident 繞過 LIMIT。

### HUMAN_REQUIRED

- 唯一升報路徑，單行：`HUMAN_REQUIRED: <類別> | ticket | incident | round=<n>/3 | basis`（`n`＝已 engaged 最高層之已開輪數；單行為呈現，事件為準）。
- 立即類：`PERMISSION`、`IRREVERSIBLE_SECURITY_COST`、`HUMAN_GATE`（Rule 4a 無票）、`COST`（最終適用層耗盡之 `EXHAUSTED` 後續跑 ack）。
- 驗證先行類（升報前須已有本地取證＋Advisor 諮詢記錄）：`SSOT_CONFLICT`、`PRODUCT_DECISION`（含 Rule 1 歧義）、`SCOPE`、`NO_AUTHORIZED_STRATEGY`（另須最小 `strategy_scan`：已考慮策略清單，每項一句排除依據；僅為紀錄，非策略搜尋框架）。
- 機械類：`EXHAUSTED`（僅限同 incident `advisor` 層已 3 輪 fail，basis 恆為 `layer_advisor_3`）、`NO_PROGRESS`。
- 權限被拒屬 `PERMISSION`，但不視為升裁已完成。腳本 stdout 之「人工介入」字樣不構成 `HUMAN_REQUIRED`。

### Advisor 與 PPSS

- Advisor 由主 agent 承接，hook 不呼叫；`consult --kind advisor` 只能記錄實際工程顧問調用，且一律經入口 `python .claude/scripts/route.py consult --incident <INC> --source <advisor.api｜advisor.native>`，不得直接以 `gate_ledger.py consult --kind advisor` 記帳（入口寫入之 `--note` 含 `via=route.py`；旁路無機械阻擋、僅可由該標記事後辨識，仍屬模型契約）。`advisor.api`＝本 incident 新發之 `advisor_call.py` 呼叫：入口於同一次呼叫內先核對、再呼叫（至多一次網路嘗試，不重試、不自動換模式），`exit 0`／`status=SUCCESS` 才記帳；`advisor.native`＝入口派發（`--phase dispatch --purpose <描述>`）→主 agent 呼叫原生 `advisor` 工具→`--phase return` 回交（須帶派發所得 `--dispatch-id` 與同一 `--purpose`；id 綁定記帳槽位與具體請求描述，錯配、過期或重複回交拒絕，經入口之並行回交由入口互斥確保僅記一次；`--purpose` 為聲明式、入口僅驗其與派發一致），入口無法確認工具是否真被呼叫、亦不可驗其內容，此為派發／回交而非完整路由。API 路徑記帳前 MUST 核對（入口機械檢查下列①②③與 receipt `ticket` 欄＝該 incident 所屬票號、`status=SUCCESS`、receipt 由本次呼叫產生）：①`receipt.payload.sha256` 等於實際送出之 payload 檔 sha256；②payload 載明目標 incident id（令牌 `incident=<INC>`）；③payload 以恰一個令牌綁定證據——`failure_sha256=<h>`（失敗輸出全文之 sha256）或 `uncertainty_sha256=<h>`（無失敗輸出時之非空不確定性紀錄之 sha256）——且 `<h>` 等於 `--evidence` 檔之 sha256；不得製造失敗，亦不得以空輸入之 sha256 充數；令牌為主要證據綁定，不限制問題內容；核對摘要併入同一 `--note`。入口檢查宣告項之間的一致性，**不**證明證據在因果上屬該 incident、payload 敘述忠實、是否需要諮詢或建議品質，此部分仍由執行者負責。非 SUCCESS、他 incident 或歷史 receipt 不得記。記帳時 ledger 於寫鎖內再次要求 incident 仍為 open：呼叫期間已被關閉者，回件與 receipt 保留、不記帳（成本已發生，入口回 `incident_closed_after_call`），主 loop 讀現況續作，不因此升 `HUMAN_REQUIRED`。來源被停用時入口回 `SOURCE_DISABLED` 與 `alternatives`：有合法替代者，主 agent 依本 Gate 順序自主選擇並續作，不停等人工；同 role 無任何啟用來源（`MISSING_REQUIRED`）依下列「Advisor 不可達」處置；主 agent 自判不得以 advisor kind 記錄；原生回答內容加密不可驗，API receipt 僅證識別與綁定（雜湊與 `request_id`），不證策略真實或問題解決，亦不構成新增授權。Advisor 不可達時：如實說明工具／環境缺口與原擬升報類別，不偽造 consult、不為滿足前置硬湊輪次；此時驗證先行類升報無法寫入 ledger，停止由 `NO_PROGRESS` 預算放行。Advisor 不可達時 `advisor` 層無法 engaged，`EXHAUSTED` 亦不可達（已知限制）。
- PPSS 證據：`RETRIEVED` 僅表取回，`SOURCE_READ` 僅表已讀，`INVALID` 為空、缺 artifact 或網域不符。`evidence_ok` 僅表示來源閱讀與支持聲明可追溯，不表示語意被 helper 驗真；`SOURCE_READ` 單獨不使其成立；離題來源已讀仍不得算問題已獲支持；`evidence_ok` 為必要條件，非主張成立之充分條件。 PPSS 之取得與回記一律經入口 `python .claude/scripts/route.py consult --incident <INC> --source research.ppss`：`--phase dispatch`（付費呼叫前入口先判定：請求形狀、既有結果須先消費、已有紀錄須附新查詢策略與理由；通過才回 `research_submit` 之具體參數，不寫 ledger 事件、不計用量，僅於每票 scratch 存最小派發描述）→主 agent 呼叫 `research_submit`→`--phase record --dispatch-id <ID> --artifact <回件 artifact_path>`（派發時入口以完整研究請求〔`question`／`queries`／`task_type`／`filters`／`justification`〕之指紋綁定 id 並將最小描述存於每票 scratch；回交時依 id 載入、與回件 `queries` 及時間核對，ledger 於寫鎖內拒絕同 incident 已記錄之 request ID；原樣透傳 `ppss record`，視窗與重送規則仍由 ledger 強制）→`--phase read …`（原樣透傳 `ppss read`）；入口不新增研究額度、不判斷證據語意、無法觀察 MCP 呼叫；經 ledger 之 PPSS 事件不帶 `via` 標記，旁路無法由 ledger 事後辨識，屬模型契約。

### 證據缺口與研究授權（Akira 2026-10-17 裁決）

1. **付費授權**：符合本 Gate 既有授權、用途、成本預算與工具權限的必要諮詢與研究（含付費模型、PPSS）可自主執行，**MUST NOT** 僅因「可能產生費用」停下再問。不得無限呼叫、超出既有預算、繞過拒絕或重複研究同一缺口；觸及新權限、成本上限或不可逆操作才依本 Gate 升報。本條不將通用規則放寬為無限放行，Advisor 建議與研究結果仍不構成新增授權。
2. **五種狀態不得互換**：①公開搜尋未命中；②資料可能存在但目前無合法存取（登入、訂閱、購買、授權後才可讀）；③官方文件未明說；④目前方法實測失敗；⑤已有直接證據顯示特定能力不支援。**MUST NOT** 把「查不到」寫成「不存在」、「未記載」寫成「不能做」、「某路線失敗」寫成「原成果應取消」。公開資料可見性不等於技術可行性。
3. **研究未解時續作**：先如實標明查詢範圍、結果與未知事項，再選下一個有價值的動作：使用已合法取得的文件、SDK、原始碼或既有紀錄；尋找可信替代來源（不限官方公開頁）；在既有授權內做低成本、可逆的最小實驗；換實作方法但保留原使用者成果；繼續不依賴該缺口的合法工作。不為湊證據反覆重送 PPSS，不虛構成功，也不因缺文件預設失敗。
4. **禁止無聲縮減**：PPSS 無結果、認證資料不可讀、官方文件缺席或工具不可達，單獨均不得作為下列行為的理由：刪除核心能力；將真實整合改 stub 後宣稱完成；將必要驗收移出範圍；降低成果表成功條件；以局部零件取代完整使用者場景；將未完成包裝成「只剩收尾」。確需變更成果時，**MUST** 明示變更前後、技術依據、可替代方案與交付影響，核心取捨由使用者確認。
5. **升報時機**：僅在直接觀察到的技術限制、真正的權限／成本邊界、或合法有界路徑已無可行下一步時，才依本 Gate 說明影響並提出決策。

### 入口與覆蓋矩陣

- 正常入口最小落點：`qa`＝`qa/SKILL.md` `## Step 0: Ticket Scope 驗證 (Scope Gate)`；`commit`＝同檔 `## Step 4.5: Ticket Auto Commit`；`sv`＝`sv/SKILL.md` `## 前置檢查：Ticket 已 Commit Gate`；`closeout`＝同檔 `## Step 11: Closeout Auto-Chain`；`done`＝`uc/SKILL.md` 規則 8；`resume`＝`.claude/CLAUDE.md` `## ⚡ Boot Sequence`（讀 `status`）。`check` 退出碼映射（單一來源，各入口呼叫處只回指）：0＝續行該入口動作；1＝ledger 已有待人工，依印出的類別、incident、basis 輸出 `HUMAN_REQUIRED`，不新造理由；2–6＝檢核未完成，不代表有待人工，不續行該入口動作，依原始錯誤在既有授權內排除後重跑，不得視為通過，同一入口同一原始錯誤重跑仍失敗 → 記 `step_limit`，依 Gate 處置，需權限或擴 scope 者依 Gate 升報；不與 `cross_review.sh` 的 exit 2 或 Gate 輪次合併計數；不得以偽造事件修復損毀 ledger。`resume` 讀 `status` 非 0：記原始錯誤與 ledger 狀態未知，可續行 Boot／讀取／取證，不得當作無 open incident，後續受 Gate 控制的動作須先通過第一個對應 `check`，不得偽造事件、重置 ledger 或繞過檢核。
- 覆蓋矩陣四軸分開記錄：check 程式結果（確定性）／呼叫路徑（模型契約）／機械後盾／未覆蓋。決策點：pause、ask-user、commit、QA、SV、closeout、恢復義務、提前升報、Advisor、PPSS、權限被拒、subagent、headless、並行。現況：呼叫路徑（入口 `check`，模型契約）已接入 qa／commit／sv／closeout／done，`resume` 讀 `status`（`done` 見 `.claude/skills/uc/SKILL.md` 的 `check done` 呼叫）；機械後盾：pause（本 Gate Stop guard `gate_stop_guard.py`）、qa／commit／sv 的漏檢事後偵測（受 receipt 視窗限制，僅回合起點可得時）；無機械後盾：done（有入口 check，但無推進證據 producer）、closeout（漏檢偵測無此腳）及其餘決策點。
- 缺入口 receipt 之後盾屬事後漏檢偵測，不得回填成事前已通過；補跑 receipt 標 `late`，不得引為已發生動作之事前放行。
- hook 連續 block 之預設上限（8 次）不是解題預算。

### QA FAIL 優先序

- 可自修且在既有授權內之 QA FAIL，先依本節修復與驗證。Table「QA FAIL」列之 `MUST STOP` 僅適用於：不可自修；人工邊界（權限、不可逆動作、產品語義，或經本地取證與 Advisor 確認無 ALLOWED 內替代策略、確實需擴 scope）；或停損成立（輪數 3 且 fail，或經取證＋Advisor 確認無可行已授權策略）。
- 疑似 scope 問題不得直接停止，須先本地取證並經 Advisor 確認。
- 該列 `MUST NOT`「自行修復後靜默重跑並宣告 PASS」仍有效：Gate 修復為非靜默，須局部驗證並於輸出揭露失敗項與修復摘要。
- Table 其他 `MUST STOP` 列之現行語意不由本節改變。

### REPORT_FIX 局部對照

- 輪數唯一依據為 ledger `round` 事件；`cross_review.sh` 計數與 LIMIT 僅為輸入事件。
- 第 1 次 REPORT_FIX＝incident 開啟（`finding`，rounds=0）。
- 每次以新策略修補並重跑所得之 REPORT_FIX verdict＝該輪 fail（依據為 verdict，非 LIMIT）；取得 verdict 前即失敗（`REPORT_FIX_ESCALATE`、環境錯誤）只記 `step_limit` 或環境事件，不計輪。
- 第 3 次 verdict 後之 `CHECKPOINT_REPORT_FIX_LIMIT`＝`step_limit`，依上節 LIMIT 處置；executor 層 rounds=2<3 時不是 `EXHAUSTED`（`EXHAUSTED` 僅限 `advisor` 層耗盡）。
- PASS／ADVISORY＝結案。REPORT_FIX 變 BLOCKER＝同 incident 續算（BLOCKER `ROUND` 與 Gate 輪數各自獨立）。同 state 週期第二缺陷預設同 incident，另開須獨立證據。
- 殘餘分歧：`REPORT_FIX_COUNT` 為同票 state 週期計數，第二缺陷仍可能提前觸及該路徑 LIMIT；本節不改 `cross_review.sh`。

### 實作狀態與宣告限制

- 現況：helper 與 ledger、入口 `check` 呼叫均已落地；本 Gate Stop guard（`gate_stop_guard.py`）已註冊於 `.claude/settings.json` 之 Stop；升報通知對齊尚未實作。Advisor 採納之驗證限制（內容加密、不可驗）照實保留；不宣稱完整 Gate，不宣稱 hook 涵蓋途中違規。
- 「完整 Gate」僅為需求名稱；任何票或收尾實錄不得宣稱其「已完整」，只得陳述實際落地之入口控制與覆蓋矩陣各列實測現況；揭露未覆蓋項不能替代機械後盾條件。

## Session Persistence 邊界

1. **可見性**：尚未持久化之收尾異動，以 `git status --short` 之 APP 工作樹 tracked dirty 集合，與 `registry_list` 讀得之 `lifecycle`／`file` 現值**共同觀測**；此二者即唯一觀測面，**MUST NOT** 另設狀態檔。
2. **Artifact set 範圍**：含本 session 已收束票之 ①`Pending/` → `deliver/` 票檔移動、②`.claude/PROGRESS.md`／`.claude/PROGRESS_ARCHIVE.md` 同步、③`plans/cso_briefing/project/TICKET_REGISTRY.json`。**registry 之雙面事實 MUST 明載**：其**合法寫入通道**為平台 API（`registry_set_status`／`registry_set_checkpoint` 等，**MUST NOT** 以檔案寫入直改，回指 §治理載體↔存取工具對應圖譜），但其**檔案落點在 APP repo 內**且已列於 §Governance Artifact 清單（`[GOVERNANCE-SYNC]`），故仍由既有治理 artifact persistence 納入。**MUST NOT** 因「寫入通道在平台側」而將其排除於 APP artifact set 之外。
3. **時效**：未持久化之 APP closeout artifact **MUST NOT** 跨 session；若收尾當下未走軌道 A（見第 5 點），則 `/handoff`、主題切換或明確結束 session 前 **MUST** 執行 `/compact closeout`。已隨票即時提交者不受本條拘束（已持久化）。
4. **驗證**：任一軌道於 commit 前 **MUST** 執行 scoped `git_diff` 零意外變更驗證（軌道 B 另含既有 Closure Gate）；任一失敗 → 禁止提交，**MUST NOT** 將未驗證之治理異動帶過 session 邊界。
5. **雙軌持久化（Akira 2026-09-15 裁決；取代原「路徑指認」單軌條款）**：已完成 Ticket 之 closeout artifact **得擇一**持久化，兩者均合法，執行者自行選擇，**MUST NOT** 因選擇其一而被判違規：
   - **軌道 A — 隨票即時**：以 `[<TICKET_ID>]` 單票前綴立即 commit。**MUST** 為單票，**MUST NOT** 混入他票 artifact（`compact/SKILL.md` 之多票合併禁令與 TPL 三分流 R2 之 commit 前綴歸因**不受本節修改**）。
   - **軌道 B — session 邊界**：延至 `/compact closeout`，走 `## Auto Commit: Governance Artifacts` 表驅動路徑；其 prefix、pathspec、逐檔處理、同 prefix 合併與 zero-diff SKIPPED 語義依 §Governance Artifact 清單與 `compact/SKILL.md` 既有規則執行。
   **唯一禁止**：將**無法歸屬於已收束 Ticket** 之 tracked dirty change 混入任一軌道之保存集合。除此之外本節**不新增任何提交授權**，亦 **MUST NOT** 擴義 `<TICKET_ID>` 佔位符值域為多票列舉。
6. **Governance Artifact 清單補列**：於 §Governance Artifact 清單表新增一列 `plans/cso_briefing/project/deliver/*.md` → `[GOVERNANCE-SYNC]`（說明欄：已交付票檔，`REVIEW`／`CLOSED` 之物理歸位產物）。依該表節首「新增 Governance Artifact 只需在此表加一行，**MUST NOT** 另開票或單獨改 SKILL.md 判斷邏輯」之既有授權執行（已於本檔 §Governance Artifact 清單表落地，見該表列）。**不補此列則票檔搬移將落在 `compact` Scope 盤點之表列外而永不提交，屬靜默失效。**

## Mid-Execution 回報前同步與自我核對義務

任一對話模式（`/uc`、`/normal`、或未切換模式之預設對話）下，執行者於本回合完成任一 Action Item 或 Acceptance Criteria 後，若即將停下回報、詢問下一步、或結束一輪工具呼叫序列，**MUST** 依序完成三軌，缺一律屬未完成：

1. **本地票檔軌**：對應項目由 `- [ ]` 改為 `- [x]`，並附完成依據（commit hash／驗證方法）。`Pending/` 票檔縱因 `.gitignore` 而 untracked，仍 **MUST** 執行——untracked 不構成免做理由。
2. **Registry 軌**：以 `registry_list(ticket_id=...)` 確認該票已入冊時，**MUST** 呼叫 `registry_set_checkpoint` 將本回合完成範圍、對應 commit hash（如有）、尚未完成項目與其原因寫入 checkpoint，並依既有規範以 `registry_list fields=["checkpoint"]` read-back 驗證。
3. **回報前核對軌**：完成軌 1／軌 2 之後、實際送出回報文字之前，**MUST** 重新讀取（`Read` 本地票檔對應區段、`registry_list fields=["checkpoint"]` 現值——**非**憑先前 Edit／工具呼叫的回傳結果推定已經寫對）逐項核對其內容與即將回報給 Akira 的敘述是否一致。發現落差 → 先修正或於回報中明示該落差，**MUST NOT** 以「應該已經寫好了」代替實際核對就送出回報。

**本義務不因對話模式而免除**——回指 §Ticket Runtime Transition Table 不變量①「對話模式非 lifecycle」之同一原則：模式僅決定回報語氣與詳略，不決定紀錄義務是否存在。僅完成軌 1（本地勾選）而未同步軌 2（registry），或完成軌 1／2 但未執行軌 3 核對即回報 → 對 Akira 而言等同「什麼都沒發生」或「回報與票面不一致」（TKR 系統視圖為其唯一可信來源），**MUST** 視為回報不完整，不得宣稱已如實揭露進度。

（Akira 2026-09-25 對話裁決，免票直接授權補此條；軌 3 為同日追加。）

## Ticket Closure 紀律

- **勾選紀律**：碼閉環 + `/sv` PASS（Codex Cross-Review 終局 PASS）後，主 session **MUST** 逐項勾選母票 Action Items 與 Acceptance Criteria；僅勾「有 SV / QA / commit 佐證」之項；`[行為性]` / `[實機]` 未實測項 **MUST** 保留未勾並於票檔最終結果（close）誠實揭露（K5-Rule 29）。**MUST** 於該項目實際完成的同一回合內立即勾選（Edit 落地 / 測試轉綠 / Codex PASS / commit 成功發生當下），**MUST NOT** 留待後續 /sv Step 10 之外的另一輪「確認完成度」請求才回頭補勾。
- **搬移紀律**：勾選完成後，票檔 **MUST** 由 `plans/cso_briefing/project/Pending/` 搬移至 `plans/cso_briefing/project/deliver/`。
  **凍結點語義**（INFRA-TRACKSCOPE-01，2026-08-27）：`Pending/` 期票檔為 untracked，搬移至 `deliver/` 之首次 commit 即該票檔進入版控之凍結版本，此前修訂史不入 git log。
- **修補票紀律**：核心 AC 未閉環者票不得轉 `CLOSED`（見下「核心 AC 與 close 判定」）；非核心 AC 未閉環只於票檔最終結果披露，**不強制**開修補票，執行端得自行判斷另開 follow-up。
- 母票 AC 佐證不明（無法判定該勾或保留）→ **MUST** 停止勾選該項並於票檔最終結果披露；該項為核心 AC 時，票保留 `REVIEW`（先依 close 補證與 fallback），禁止推定勾選。
- **核心 AC 與 close 判定**：核心 AC＝其不成立則票面一句話交付目標不成立之 AC；難以驗證不是降級為非核心的理由，有疑義時從嚴視為核心並先補證；僅「與交付目標無必要性」或票面明示為可選／觀測／外審者為非核心。close 轉 `CLOSED` 須所有核心 AC 為 PASS 或有效 N/A。hard blocker 僅：核心 AC 已確認 FAIL、已知安全問題、不可逆損害／資料遺失風險、下一張相依票明確需要但尚未交付的 contract（只查直接相依與具體 contract；有具體缺件線索者針對該線索補查，不以「未知」蓋過；無法判定且無線索時標示「未知」，不宣稱已確認無缺件）。核心 AC 為 UNVERIFIED 時先自主補證與 fallback，仍不足則保留 `REVIEW`，不報 PASS，亦不新增人工批准步驟。可選外審工具的缺席不構成缺陷，但其發現的實質核心缺陷同樣計入核心 AC 判定。非核心 UNVERIFIED、有效 N/A、可選測試未跑、首次真實使用待觀察、可選外審不可用，披露於票檔最終結果，不阻擋 `CLOSED`。
- **checkpoint 處置**：轉 `CLOSED` 前若冊面 checkpoint 為 `OPEN`，僅於其事項確實解決時以 `resolution` 標 `RESOLVED`（其事項僅為已被本節取消之人工關票等待者，視為已解決，於 `resolution` 註明）；事項未解決則不為通過平台檢查而標記，票保留 `REVIEW`。
- **冊面轉態紀律**：票檔層三步（勾選／搬移／修補票）完成後，`TICKET_REGISTRY.json` 對應條目 **MUST** 同步完成**四要件**——①`lifecycle` 轉終態；②`reviews` 四欄（qa／sv／codex／traycer）有可考記錄者填實測值（既有 enum，附證據定位），無可考記錄者填 `N_A` 並於票檔最終結果之披露行揭露依據，**MUST NOT** 以推定補 `PASS`（撤警正向取證律），可選外審無證據者亦不得填 `PASS`；③`file` 欄指標由 `Pending/` 改指實際落點 `deliver/` 或 `archive/`；④收尾結果之耐久載體為**票檔（`deliver/`）內之最小最終結果**（交付摘要、最終驗收結論、實作提交、實際收束結果），於轉態與 read-back 完成後寫入；close report、AC 明細、命令輸出、reviewer 回應等中間衍生物只存 `.tmp` 七天，不 commit、不上 TKR；`checkpoint` 為收尾前之工作載體，依 PLT 契約於 `set_status(CLOSED)` 轉態時清為 `null` 並 unlink `.tmp/checkpoints/{ticket_id}.md`（見「checkpoint 生命週期與 detail 檔」節），冊面不要求保留 CLOSED 後之 checkpoint 摘要。
- **原子性**：①②③ 與轉態 **MUST** 於同一收尾回合內完成；④最小最終結果於轉態與 read-back 後寫入，部分成功時如實回報並允許同票重入補寫，**MUST NOT** 虛報結案。冊面異動之 APP Git persistence 依 §Session Persistence 邊界第 5 點**雙軌擇一**執行（隨票即時 `[<TICKET_ID>]` 單票 commit，或延至 `/compact closeout` 走 `[GOVERNANCE-SYNC]`）；scoped `git_diff` 零意外變更之驗證義務隨該軌道執行，**MUST NOT 取消**。
- **矛盾停止**：票檔層與冊面層狀態不一致（如票檔已搬 `deliver/` 而冊面 `file` 仍指 `Pending/`）→ **MUST** 停止回報並列入待裁，禁單方推定何者為真。
- **VOID 歸位紀律**：票務判定作廢時 **MUST** 完成四要件：①票檔移 `plans/cso_briefing/project/void/`；②冊面 `file` 欄同步改指 void 路徑；③以 `registry_set_checkpoint`（敘事欄位置於 `checkpoint_data` 物件內）載明作廢事由；寫入後 **MUST** 以 `registry_list fields=["checkpoint"]` read-back 驗證欄位確實落地（防頂層鍵位靜默丟棄，同節既有規範）；④**復活紀律**——`VOID` 為終態，議題日後重啟 **MUST** 開新票，**MUST NOT** 改回原票 `lifecycle`；原解凍／續辦條件自動降級為新票之開票前置檢核，既有產物留存不刪，供新票直取為 `evidence_dependency`。
  **平台限制**：`registry_upsert_ticket` 對已歸檔票回 `REGISTRY_ARCHIVED_TICKET`，且 `registry_unarchive` 平台**未實作**（PLT `INFRA-REGISTRY-API-GAPS-01` 明示範圍收斂不實作）——故「開新票」非風格建議而是**平台強制**。
- **Dashboard 慣例同步**：`.claude/PROGRESS.md`／`PROGRESS_ARCHIVE.md` 之例行 Dashboard 列更新得與實作同 commit 或以 `[COMPACT-SYNC]` 獨立提交，**不計入**票面「git diff --stat 僅涉 ALLOWED」AC 之違規判定（INFRA-GOVSYNC-01 定調，消解 RGR-APPDEPLOY-01 G2 帳實矛盾）。
> **強制層級（誠實定性）**：本節「冊面轉態紀律」為**紀律層**條款，非機械閘。冊面轉態發生於 Desktop 治理 session，而 `verify_checklist.sh` / `structural_verify.sh` 執行於 Claude Code 端——**閘門位置與動作發生位置不重合**，腳本層對本失效型無攔截力（`INFRA-TPL-V32-01` 事故全程未經 Claude Code）。真正之機械強制須由 mcp-ticket-platform server 端提供（`lifecycle` 轉終態前 `file` 欄指標所指票檔 MUST 實際存在），屬 TKR-05 平台原語域，依前置資源恆存律**不得由本票預支**；在該閘落地前，本節效力等同宣告式紀律，違反不會被工具攔截。`checkpoint` 於終態轉換時被清空為 PLT 刻意設計（見「checkpoint 生命週期與 detail 檔」節），APP **MUST NOT** 反向要求其非空。
- 授權：Akira 2026-07-17 對話裁決（全票適用之標準收尾流程）；即時勾選條款由 Akira 2026-07-19 對話裁決授權補訂。
- **FORBIDDEN 延後項免開票紀律**：票面 `FORBIDDEN` 命中 `.claude/PROGRESS.md` 導致的收尾延後項（Dashboard 同步／`PROGRESS_ARCHIVE.md` Done 標記／票檔 Pending→deliver 搬移／票內 AC checkbox 勾選）**MUST NOT** 另開獨立治理票（如 `*-DOCSYNC-01`）承接；此四項本屬標準 SOP 收尾流程，**MUST** 於下一輪 `/compact` 或後續 session 之正常收尾動作中處理即可。（Akira 2026-07-19 對話裁決）
- **結構歧義停損律**(2026-08-01 CSO 確認決議):同一子系統下列任一**字面**成立,MUST 停止開立/受理新修補票並升報 CSO:①同一票修訂版次 ≥ 3(票面 Rev 直取);②結構性死結/自指阻塞 ≥ 2 種(決策檔/Handoff 記載筆數直取);③任一 workaround 額度制用量達上限 50%;④修補深度 ≥ 2(FIX 票之 FIX 票)。凍結僅及修補線,不及執行中之非修補工作;解除條件=外審回流+CSO 存廢三案(退役/降級/續修)裁決落檔。禁語義裁量(「此票很重要」不豁免)。

## Ticket Rigor Gate（按需工具，非常駐前置）

> `TICKET_RIGOR` 為 TKR 冊面合法 lifecycle 狀態；其允許轉換、寫入者與後繼狀態依 PLT/TKR 平台之權威定義（見 §Ticket 生命週期流程（五段）①）。APP 不得將此狀態排除、歸零或映射為自建的「審查中」。`.claude/skills/ticket-rigor-gate/` 是按需使用的工具能力，不是一般新票的自動派工前置條件；除非未來存在已驗證的、由固定狀態或欄位觸發的 `ticket-run`／dispatcher consumer，否則不得將「得執行 rigor」作為常駐治理規則。

- 報告與遙測一律落 `.tmp/rigor-gate/`（`RGR-APPDEPLOY-01` L2 裁決：執行期產物，KEEP_N=10 輪替）；**MUST NOT** 於任何票面 AC 引用 `.tmp/` 下 rigor 產物為長期證據——耐久結論唯一載體為票面／冊面收尾實錄。
- 本節不新增任何 MUST 級派工前置；未跑 gate 之票不因此退件。

**翻盤紀錄**：2026-09-06 Akira D-1 裁決推翻 2026-08-27 治理減量裁決（APP 冊 lifecycle 四態之刻意裁剪）。PLT／TKR 平台為 lifecycle、狀態轉換、registry 資料契約與共通票務流程之唯一語義權威，APP 自此採 PLT 五段語義（見 §Ticket 生命週期流程（五段））。APP 採取最小必要之 consumer 實作，而非重寫平台 enum 或移植全套 script。
