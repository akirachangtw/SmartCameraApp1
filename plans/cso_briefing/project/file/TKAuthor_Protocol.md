### 開票通道規則（2026-10-07 Akira 裁決；取代原「開票格式強制規則」）

- Desktop 得經 MCP 讀取本地資料（grep／git／read 等）以提高上游輸入品質。Desktop **不得執行與 `/ticket-author` 重疊之作業**：逐票取證（ticket 模式之 live evidence、查重、配號）與正式開票（建立票檔、寫入 Pending、Registry 登冊）。其餘既有授權（確認決議歸檔、交接、上游診斷與規格、審查、記憶等）保留。
- 正式開票只在目標專案之 Claude Code session 以 `/ticket-author` 進行，**一律自 `plan` 起步**（2026-10-08 Akira 裁決；廢止 v1.1 Fast-path Gate）：上游規格先跑 `/ticket-author plan` 產出 Ticket Map；`/ticket-author ticket` 只對 `Plan Gate: PASS` 之 plan artifact 的選定 item 執行。單一項目之規格亦同（產出單項 Ticket Map 即可）。問題或根因未釐清時先跑 `diag`。
- 上游規格屬開票前之暫時輸入，**不強制保存位置**：可直接貼入 `plan` 提示詞，或以任一 Claude Code 可讀之檔案提供。上游文件檔頭不得使用 `Plan Gate:`／`Ticket mode eligibility:`／`Verdict:`／`Fixture:`（正文提及不影響分類；檔頭若含任一欄位，Skill 依 Plan → Diag → Spec 分類並 fail-closed），且不得寫「直交 ticket」等繞過 `plan` 之指示。
- **提示詞樣板**（欄位依 `input-contract.md`；MUST NOT 只給檔案路徑）。`Constraints` 以上游規格之 ALLOWED／排除清單原文為準，MUST NOT 要求 `plan` 拆分規格已明確排除之範圍：

```text
/ticket-author diag
<症狀與影響>
Source @<log 或診斷檔>, live implementation, and applicable SSOT.
Find the causal path and smallest credible repair directions. Do not open a Ticket.
```

```text
/ticket-author plan
Source: @<Claude Code 可讀之上游規格檔>，或直接於此貼入規格內文
Goal: <一個可觀察的總結果，照錄規格之目標節>
Constraints: <照錄規格 ALLOWED 與排除清單>；不建立正式 Ticket；不修改 production code、Pending、Registry、正式票檔；只可寫本次 .tmp/ticket-author/ artifact
Decisions: <規格中 APPROVED 項>；PENDING_DECISION 照實標示
Requested output: Ticket Map（每項單一結果、候選 ALLOWED、依賴三分、ticket 階段需重做之 live evidence）
```

```text
/ticket-author ticket
Source: @.tmp/ticket-author/<YYYYMMDD-slug>-plan.md
Selected item: <plan-local ID>
Goal: <該 item 之單一可觀察結果>
Ticket ID hint: <議題前綴或候選 ID>（正式 ID 由 ticket 模式於 Registry 含 archive 與 Pending／deliver／void 查重後分配）
Collect live evidence first; create the Ticket only after both gates pass.
```
- 上游文件之決策分類一律用 `APPROVED`／`RECOMMENDED_ONLY`／`PENDING_DECISION`；Desktop 之 MCP 讀取可記為本地證據，但不得宣稱已確認 current-live 行為、已建立正式 Ticket 或已寫入 Registry，除非 Claude Code 回傳 receipt。
- `TICKET_TPL`（`D:/DevSources/AndroidProj/SmartCameraApp1/plans/cso_briefing/project/file/Unified_Ticket_Template_v3.md`）由 `/ticket-author` 於 ticket 模式完整讀取；Desktop 審查票面（RGR 回流、收束審查）時仍以其為審查依據。Desktop 審查只產出 findings 與裁決，不改正式票面與 lifecycle；裁決經 Akira 轉交 Claude Code 寫入票面（直接轉貼或落 Claude Code 可讀之檔案皆可）。
- `.tmp/ticket-author/` artifact 滿 7 天僅為清理資格（CLEANUP-06），非開票期限；artifact 不存在或資格失效時重跑 `plan`。