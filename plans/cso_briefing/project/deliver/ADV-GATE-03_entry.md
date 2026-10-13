# ADV-GATE-03: 將現役「等待 Akira／停止」條款終點回指 Autonomous Engineering Gate，並於五個正常入口加入 gate_ledger check 呼叫、Boot 加入 status 讀取，並對齊 Gate 節三處狀態字面（Rev A）

**Revision**: Rev A（2026-10-13；依 Akira 終審裁決 D1 甲、D2 甲就地修訂票面；未實作、未改 lifecycle、不重做 plan、不追加 RGR、不新增 ### AI- 標題）。變更：P1 `check closeout` 落點改為入口冪等判定之後、第一個 registry／票檔寫入之前，並補兩案重放；P2 `check` 退出碼映射單一來源改寫入 `ssot-governance.md` 入口落點句，HC7 與 AI-3／AI-4／AI-5 改為回指；P3 ALLOWED 加入 `ssot-governance.md`（僅三處狀態字面，併入 AI-2）；External-review decision 改 `WAIVED`。Rev A.1（Akira 2026-10-13；僅更新票面、不新增 AI 標題）：A.1-1 `resume` 讀 `status` 非 0 之處置；A.1-2 SSOT 明載「本 Gate 專用 Stop guard 與升報通知對齊」尚未實作（不寫成專案沒有 Stop hooks），Advisor 採納之驗證限制照實保留。

**檔案治理/來源說明**：本票為 Plan-backed 單票（plan-local item `T02`）。Parent 類型＝Plan-backed；Parent 絕對路徑＝`D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261011-autogate-plan.md`；Parent artifact status＝`Plan Gate: PASS`／`Ticket mode eligibility: ALLOWED`／`Fixture: false`（Revision C＋C.1，2026-10-11）；plan-local item ID＝`T02`；Parent read timestamp＝2026-10-13 11:09（T02 段、處置範圍節、入口落點表、覆蓋矩陣、Decisions 全讀）。`.tmp` 檔日後可能被清理，故本票自帶下列摘錄，不依賴其存續。

**Selected item 原文（T02 goal，摘要不改義）**：現役 consumer 檔案中，每處直接等待 Akira／人工之條款，終點改為「記為該 incident 一輪失敗輸入或 `step_limit`；有新且已授權策略即續作，輪數 3、立即類／驗證先行類（前置已滿足）、或無剩餘已授權策略才 `HUMAN_REQUIRED`」；`uc`「連續 2 次」改終點不改數字；`cross-review.md` 例行 ack 改為 EXHAUSTED 後恢復門；Rule 1 改為驗證先行；於入口落點表五個 gate_point 加入 `gate_ledger.py check <gate_point>` 呼叫，並加入 `resume` 列（Boot Sequence 讀 `status`）；`compact/SKILL.md` closeout 確認點與同檔 Auto Commit 節矛盾須調和。Parent 約束：以**回指式**處置（保留條款觸發條件、改終點）；不新增清單載體、不改任何腳本或 exit 語意、不改各步驟上限數字、不新增通用攔截。

**已裁決補充（Akira 2026-10-13，S1–S3 於開票指令中裁決、S4–S7 於終審中裁決，優先於 plan 對應措辭）**：
- **S1（用已完成介面）**：使用已完成並進入 `REVIEW` 的 `gate_ledger.py`（ADV-GATE-02，`cc541546`＋`0452a579`）之介面替換舊停止路由；**不重新設計 ledger、不改 helper**。
- **S2（不重複）**：`ADV-RULE4C-01` 已完成之 Rule 4c 與 Transition Table QA FAIL 列修改**不重複施作**，本票只核對其字面未被本票改動。
- **S3（不追加補丁）**：**不再追加例外補丁**——不得以新增例外句、過渡句或並列條款方式處理舊停止條款，一律改其終點或回指 Gate 節；已被 Gate 節涵蓋者不另寫副本。
- **S4（P1 closeout 落點）**：`check closeout` 置於 `sv/SKILL.md` Step 11 第 1 項「入口冪等」判定**之後**、第一個 registry／票檔寫入**之前**。入口冪等命中 `MUST RETURN` 者不呼叫 `check`；票在 `deliver/` 而冊面 `file` 仍指 `Pending/` 者不 RETURN、亦不呼叫 `check`，交既有 Closure Gate（`ssot-governance.md` §最小冪等回傳）與「矛盾停止」（§Ticket Closure 紀律），**不新增恢復流程**。
- **S5（P2 退出碼映射，單一來源）**：映射寫在 `ssot-governance.md` 入口落點句；各 SKILL／CLAUDE.md 呼叫處只回指。映射內容：0＝續行該入口動作；1＝ledger 已有待人工，依印出的類別、incident、basis 輸出 `HUMAN_REQUIRED`，不新造理由；2–6＝檢核未完成，**不代表有待人工**，不續行該入口動作，依原始錯誤在既有授權內排除後重跑，不得視為通過；同一入口同一原始錯誤重跑仍失敗 → 記 `step_limit`，依 Gate 處置，需權限或擴 scope 者依 Gate 升報；不與 `cross_review.sh` 的 exit 2 或 Gate 三輪合併計數；不得以偽造事件修復損毀 ledger。
- **S6（P3 擴 scope）**：ALLOWED 加入 `.claude/rules/ssot-governance.md`、自 FORBIDDEN 移除、`TARGET_BLOB` 補該檔；**只改三處**：L175 契約狀態句、L216 入口落點括註（承載 S5 映射）、L239「皆尚不存在」句。目標內容：helper 與 ledger 已存在（ADV-GATE-02）；入口 `check` 呼叫已接入 qa／commit／sv／closeout／done，resume 讀 `status`（ADV-GATE-03）；呼叫路徑屬模型契約，不被機械阻擋；「本 Gate 專用 Stop guard 與升報通知對齊」尚未實作（待後繼票），Advisor 採納之驗證限制（內容加密、不可驗）照實保留。入口落點句 SSOT 字面為「入口 check 呼叫已接入（ADV-GATE-03）。」，不帶條件語。L175 原句「MUST NOT 宣稱已有機械計數或 hook 阻擋」拆分：刪「機械計數」半句（ledger 已存在），保留不得宣稱 hook 阻擋、不得宣稱完整 Gate。SSOT 字面不寫「本票完成驗收後」等條件語，不寫「專案沒有 Stop hook 或通知」（`settings.json` 已有 `telegram_stop.sh`、`telegram_ask.sh`）。三處以外不改，不新增治理說明；Gate 其餘謂詞、Transition Table（含 QA FAIL 列）、Rule 4c 位元組不變。入口 `check` 已接入 ≠ Stop hook 已接入 ≠ 完整 Gate 已交付；驗收通過前，收尾與回報不得宣稱入口接線已驗證，驗收是否通過由 AC、測試與收尾實錄承擔。
- **S7（外審）**：見下 `External-review decision`（WAIVED，範圍限本票）。
- **S8（A.1-1，`resume` 的 `status` 非 0）**：`status` 非 0（含無票 exit 3、ledger IO 失敗）→ 記原始錯誤與「ledger 狀態未知」，可續行 Boot／讀取／取證，**不得當作無 open incident**；後續受 Gate 控制的動作須先通過第一個對應 `check`，不得偽造事件、重置 ledger 或繞過檢核。取代 Rev A 於 G2 所加「比照 2–6 列處置」一句。
- **S9（A.1-2，SSOT 狀態字面）**：`ssot-governance.md` 明確寫「本 Gate 專用 Stop guard 與升報通知對齊」尚未實作，**不**寫成專案沒有 Stop hooks（`settings.json` 已有 `telegram_stop.sh`、`telegram_ask.sh`）；Advisor 採納之驗證限制（內容加密、不可驗）照實保留；入口狀態句字面仍為「入口 check 呼叫已接入（ADV-GATE-03）。」。

**證據分級**：①**CONFIRMED（2026-10-13 11:09 live 直讀）**：HEAD `b712fb3e`、工作樹 clean；八個標的檔皆存在，`git ls-files --eol`：`.claude/CLAUDE.md`、`rules/cross-review.md`、`rules/kairos-lessons.md`、`rules/ssot-governance.md`、`skills/sv/SKILL.md`、`skills/compact/SKILL.md` 為 `i/lf w/lf`；`skills/uc/SKILL.md`、`skills/qa/SKILL.md` 為 `i/lf w/crlf`（工作樹 CRLF，`.gitattributes` `*.md eol=lf`，index 為 LF；plan 稱兩檔 CRLF 屬工作樹現況，已核）；`.claude/CLAUDE.md` 129 行（上限 150）。五個入口錨點標題仍在：`qa/SKILL.md` `## Step 0: Ticket Scope 驗證 (Scope Gate)`、`## Step 4.5: Ticket Auto Commit`；`sv/SKILL.md` `## 前置檢查：Ticket 已 Commit Gate`、`## Step 11: Closeout Auto-Chain`；`uc/SKILL.md` 規則 8「Ticket DONE = QA 交棒信號」；`.claude/CLAUDE.md` `## ⚡ Boot Sequence`。②**CONFIRMED（helper 介面直讀）**：`gate_ledger.py` 檔頭 Usage 列有 `status [--json]` 與 `check <qa|commit|sv|closeout|done|resume> [--late]`，`--ticket ID` 可置於子命令前後；exit code：0 成功／1 `check` 有待人工／2 用法錯誤／3 ticket 身分無法解析／4 票檔落點非 `loc:pending`、不存在或矛盾／5 契約拒絕／6 ledger IO 或 lock 失敗；`status` 唯讀，對任何落點皆可讀歷史 ledger。③**CONFIRMED**：`.claude/rules/ssot-governance.md`（292 行，blob `2b296a37`）含 `## Autonomous Engineering Gate` 節；三處過時字面（行號僅供定位）：L175 `契約狀態` 句稱 helper、ledger、Stop hook、入口 `check` 呼叫與三輪機械停損「目前尚不存在」；L216 入口落點括註稱「目前尚未加入呼叫，屬後續票」；L239 稱 helper、Stop hook、入口 `check` 呼叫、三輪機械停損、Advisor 採納之驗證、PPSS 取回／證據記錄「皆尚不存在」——與 ADV-GATE-02 完成後現況不符；Akira 2026-10-13 擴 scope，三處納入本票 AI-2（S6）。`最小冪等回傳`（L169 起）與 `矛盾停止`（`Ticket Closure 紀律`）為 S4 所引既有條文，不改。；Rule 4c 已為「不適用於當前已啟動 Ticket 之 ALLOWED 內可自修問題…依 Gate 修復」（ADV-RULE4C-01，`CLOSED`）。④**CONFIRMED（grep 盤點，樣本非母體——母體以 AI-0 固定樣式現盤為準）**：現役直接停止／等待／人工類條款位置見下列 AI 之「處置表」；registry：`ADV-GATE-01`、`ADV-RULE4C-01` `CLOSED`，`ADV-GATE-02` `REVIEW`；`ADV-GATE-03` 於 registry 主檔／archive／`Pending/`／`deliver/`／`void/`／plans／.claude 全無出現，`ADV-GATE-\d+` 最大號＝02（取號歷史，2026-10-13 11:09）。⑤**HYPOTHESIS（不阻擋）**：「Gate 實施後過早停止減少」由 T04 端到端弱證據檢驗，本票僅交付 consumer 對齊。

**External-review decision**: WAIVED — Akira 2026-10-13。範圍限本票：既有 helper 的文件入口接線與同檔狀態句對齊；以暫存 repo smoke 與情境重放驗證，不送 PPSS；不傳遞至後繼 hook 票。

**簽發日期**: 2026-10-13
**簽發者**: Claude
**來源議題**: Advisor
**Mode**: uc  ||  **Effort**: High
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: ADV-GATE-01（`CLOSED`，Gate 契約節）、ADV-GATE-02（`REVIEW`，`gate_ledger.py` 介面）、ADV-RULE4C-01（`CLOSED`，Rule 4c／QA FAIL 已對齊）
- `evidence_dependency`: ADV-GATE-01、ADV-GATE-02、ADV-RULE4C-01——僅作字面判準來源，本票錨點皆為 live 直查
- `coordination_dependency`: 無（autogate T04 Stop hook 須在本票之後；本票與其他編輯上述八檔之票須串行，`concurrency.md` §2）

## Verify Config
TICKET_ID=ADV-GATE-03
CREATED=2026-10-13
PRESET=docs
ALLOWED=.claude/CLAUDE.md,.claude/rules/cross-review.md,.claude/rules/kairos-lessons.md,.claude/rules/ssot-governance.md,.claude/skills/uc/SKILL.md,.claude/skills/qa/SKILL.md,.claude/skills/sv/SKILL.md,.claude/skills/compact/SKILL.md
FORBIDDEN=.claude/rules/concurrency.md,.claude/rules/engineering-discipline.md,.claude/rules/ticket_pattern.md,.claude/rules/decommission.md,.claude/rules/critical-files.md,.claude/rules/mcp-service-safety.md,.claude/rules/phase0-lessons.md,.claude/rules/state-machine.md,.claude/scripts/**,.claude/hooks/**,.claude/settings.json,.claude/settings.local.json,.claude/agents/**,.claude/skills/qa/scripts/**,.claude/skills/qa/tests/**,.claude/skills/sv/scripts/**,.claude/skills/sv/CROSS_REVIEW.md,.claude/skills/sv/ssot_context.md,.claude/skills/android-ui-design/**,.claude/skills/dream/**,.claude/skills/mdlint/**,.claude/skills/normal/**,.claude/skills/status/**,.claude/skills/ticket-author/**,.claude/skills/ticket-rigor-gate/**,.claude/skills/ticket-run/**,.claude/skills/wktree-close/**,.claude/skills/wktree-compact/**,.claude/PROGRESS.md,.claude/PROGRESS_ARCHIVE.md,app/**,tools/**,plans/cso_briefing/project/TICKET_REGISTRY.json,plans/cso_briefing/project/TICKET_REGISTRY_ARCHIVE.json
MAX_LINES=N/A
DIFF_REF=b712fb3e
ANCHOR_PROBED=2026-10-13 11:44
BASE_COMMIT=b712fb3e
TARGET_BLOB=.claude/CLAUDE.md:2da1e552;.claude/rules/cross-review.md:54f17049;.claude/rules/kairos-lessons.md:eec722ee;.claude/rules/ssot-governance.md:2b296a37;.claude/skills/uc/SKILL.md:ce58ec06;.claude/skills/qa/SKILL.md:3189d8dd;.claude/skills/sv/SKILL.md:c3016829;.claude/skills/compact/SKILL.md:82fa8e97
INTRODUCED_CAPABILITY=feature:gate-entry-check-wiring
DEPENDENCY_DELTA=NONE

> PRESET=docs（零 `.kt`／`.xml`／gradle，`assembleDebug` N/A）。`MAX_LINES=N/A`：停損／升報類條款改寫，增補規模不可先驗估算（TPL v3.5 R3）；MAX_LINES 非 execution gate。`TARGET_BLOB` 由 `git hash-object` 直取（`git rev-parse --short=8 HEAD:<path>`，非 `N/A`）。`INTRODUCED_CAPABILITY` 為顯式 canonical token；外審觸發由上列 WAIVED 記錄處置。**DEFERRED-TO-DOCSYNC（不開票）**：FORBIDDEN 命中 `.claude/PROGRESS.md`／`PROGRESS_ARCHIVE.md` → `/qa` Step 4 SKIP；Dashboard 同步、Done 標記、票檔 Pending→deliver 搬移與 checkbox 勾選留待下一輪 `/compact` 標準收尾，**不另開票**。`uc`／`qa` 工作樹為 CRLF（index LF）：編輯 **MUST NOT** 造成整檔換行改寫（見 HC8）。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）＋條款盤點
- **檔案**: 八個 ALLOWED 檔（唯讀確認）；唯讀參照 `.claude/scripts/gate_ledger.py`
- **動作**: 逐項確認（行號僅供定位；以字面搜尋為準；`uc`／`qa` 比對前以 `tr -d '\r'` 去 CR）：
  - `git rev-parse --short HEAD`、`git status --short`（八檔皆無本地變更）；`git rev-parse --short=8 HEAD:<path>` 對照 `TARGET_BLOB`（不符 → TPL v3.11 基線漂移三分流，行號／措辭漂移只重讀補證）。
  - 取號：開票取號歷史（2026-10-13 11:09，登冊前：`ADV-GATE-03` 全冊無命中、`ADV-GATE-\d+` 最大號＝`02`）僅為歷史；**開工核對**：`registry_list(project=SmartCameraApp1, ticket_id=ADV-GATE-03)` 冊面僅**一筆**且 `file` 指向本票檔；本票自身存在不算占號。
  - 六個入口錨點字面各恰 1 筆（見證據①）；`gate_ledger.py` 檔頭 Usage 仍含 `status [--json]`、`check <qa|commit|sv|closeout|done|resume>` 與 exit code 0–6；`ssot-governance.md` 含 `^## Autonomous Engineering Gate$` 恰 1 筆；AI-2 三處現值字面各恰 1 筆：`契約狀態：本節為契約文字。所稱 helper、ledger、Stop hook、入口 `check` 呼叫與三輪機械停損均為後續票目標，目前尚不存在`、`正常入口最小落點（目前尚未加入呼叫，屬後續票）`、`本節只交付契約；helper、Stop hook、入口 `check` 呼叫、三輪機械停損、Advisor 採納之驗證、PPSS 取回／證據記錄皆尚不存在。`；S4 所引 `### 最小冪等回傳` 與 sv `入口冪等` 項仍在。
  - **Rule 4c 基線**：記錄 `.claude/CLAUDE.md` 中 Rule 4c 整行之 `sha256`（`sed -n '/^c) \/qa \/sv/p'`），AI-1 後逐位元組比對；`ssot-governance.md` 另記錄 Transition Table 節（`## Ticket Runtime Transition Table` 至 `## Autonomous Engineering Gate` 前）與 Gate 節除 AI-2 三處外全文之 `sha256`，AI-2 後逐位元組比對；該檔只允許 AI-2 三處有 hunk。
  - **條款盤點（固定樣式，結果落票檔收尾實錄）**：對七個 consumer 檔（不含 `ssot-governance.md`）執行 `grep -nE "等待|停止|STOP|人工|裁決|\back\b|BLOCKED|必須等待|需.{0,6}確認" <file>`（`uc`／`qa` 先 `tr -d '\r'`）；每一命中 **MUST** 歸入下列「處置表」之一列，或標 `K-不涉停止/等待語意`（如純敘述、歷史事件名）；**盤點出處置表未列之直接停止／等待／人工類條款 → 不自行擴張處置，列入收尾實錄「盤點新增」並依 Error Handling 回報**。
- **任一項不符 → 依 TPL v3.11 基線漂移三分流分類處置**（`ANCHOR_STALE_SCOPE_INTACT`／`CANDIDATE_SUPERSEDED` 為資訊性，記錄後以現值續作；`ALREADY_EXECUTED_PENDING_CLOSURE` 停止；`BLOCKED_UNATTRIBUTED_STATE` 因僅觸 `.claude/` 治理面，記錄後續作）；Gate 節或 helper Usage 不符，或 `ADV-GATE-03` 被另一張不同票重號／冊面 `file` 與票檔實際落點不一致 → 停止回報。
- [x] 完成（2026-10-13，HEAD `b712fb3e`）：八檔 blob 與 `TARGET_BLOB` 相符、工作樹 clean、冊面 `ADV-GATE-03` 僅一筆且 `file` 一致；入口錨點與 helper Usage（`status [--json]`、`check <…>`、exit 0–6）、Gate 節、AI-2 三處現值字面與 `### 最小冪等回傳`、sv `入口冪等` 各恰 1 筆；基線已記錄（Rule 4c 整行 sha256 `ff733c7f…`、Transition Table 節 sha256 `2357f69f…`、`CLAUDE.md` 130 行）；盤點樣式命中數 CLAUDE 6／cross-review 6／kairos 1／uc 9／qa 18／sv 15／compact 11，歸類見收尾實錄

**回指式處置統一句（本票全部「R」列之唯一終點措辭來源；各處只回指，不複製 Gate 節謂詞）**：`→ 依 \`ssot-governance.md\` §Autonomous Engineering Gate 處置`。各處可附該處觸發條件的一句事實描述，**MUST NOT** 在回指處複製輪數、類別清單或 GO 謂詞。

**處置表（R＝終點改回指 Gate；K＝保留，立即類／驗證先行類或與 Gate 無涉，僅於盤點歸類；錨點為字面片語）**

| # | 檔 | 現值錨點（字面片語） | 處置 | 說明 |
|---|---|---|---|---|
| C1 | `.claude/CLAUDE.md` | `輸出 Boot Report → **停止等 Ticket**` | R（resume） | 無 active 票維持停止（Rule 4a 立即類 `HUMAN_GATE`）；有 active 票先讀 `status`（見 AI-1） |
| C2 | `.claude/CLAUDE.md` | Rule 1 `不假設。有歧義就問，不要靜默選擇。` | R | 歧義先本地取證與 Advisor；仍屬不可推導之產品語義才 `HUMAN_REQUIRED(PRODUCT_DECISION)`；「對話正文提問、禁 `AskUserQuestion`」等提問格式條文不動 |
| C3 | `.claude/CLAUDE.md` | Rule 4a `無 Ticket → 輸出 Boot Report 並停止`；Rule 4d `Session 首訊無 Ticket → 詢問後停止` | K | 立即類 `HUMAN_GATE`，不編輯 |
| C4 | `.claude/CLAUDE.md` | Rule 4c 整行 | K | `ADV-RULE4C-01` 已完成，**位元組不變** |
| X1 | `rules/cross-review.md` | `修正涉及：PROGRESS.md / sv_report*.md / ssot_context.md / CLAUDE.md…`、`修正含 token：FeatureFlags / gate…` 兩款 | K | 涉治理檔／Gate 詞彙之修正＝需新權限或擴 scope，屬驗證先行類 `SCOPE`／立即類 `HUMAN_GATE`；保留 Checkpoint 等待，不編輯 |
| X2 | `rules/cross-review.md` | `第 2 次 FAIL（同票任何維度…` 與 `次數限制：…完成後 **MUST** 立即回報等 ack。第 2 次 → **MUST** 停。` | R | 第 2 次 FAIL 屬步驟級事件（D-NEST）：改為記 `step_limit` 並回指 Gate；例行 ack **不構成**「無已授權策略」之理由 |
| X3 | `rules/cross-review.md` | `退出機制：無 Akira ack **MUST** 不得連續執行 2 次以上 cross_review.sh…` | R | D-ACK：ack 保留於 `EXHAUSTED` 之後的續跑（`HUMAN_REQUIRED(COST)` 恢復門）；超過步驟級上限後再重跑同腳本**不是**已授權策略（exit semantics 不變，不改 `cross_review.sh`） |
| X4 | `rules/cross-review.md` | Checkpoint 格式區塊 `⏸️ 等待 Akira 裁決` | R | 保留格式，註明僅於 `HUMAN_REQUIRED` 成立（X1 款或 X3 恢復門）時輸出 |
| K1 | `rules/kairos-lessons.md` | K3-Rule 26 `**MUST** 回報 CSO，等待裁決是否擴充 ALLOWED 或開後續 Ticket` | R | 疑似需擴 ALLOWED＝驗證先行類 `SCOPE`（先本地取證與 Advisor） |
| U1 | `skills/uc/SKILL.md` | 規則 9 `QA FAIL、Auto Commit FAIL 或 \`Auto-Chain STOPPED\` 時，**MUST** 停止且不得進入 SV` | R | 保留「`/qa` PASS 前不得進入 SV」；終點改回指 Gate（可自修者修復後重跑 `/qa`） |
| U2 | `skills/uc/SKILL.md` | 唯一例外 `連續 2 次修復仍編譯失敗 → 回報 \`🔴 需要人工介入\`` | R | **只改終點不改數字**：連續 2 次＝步驟級上限，記 `step_limit` 並回指 Gate |
| U3 | `skills/uc/SKILL.md` | 唯一例外 `需修改 CLAUDE.md 禁區內的檔案`、`發現 SSOT 規格衝突` | R | 分別為驗證先行類 `SCOPE`、`SSOT_CONFLICT`（先取證＋Advisor）；其餘例外（Erratum、AC 佐證不明）不動 |
| U4 | `skills/uc/SKILL.md` | 規則 8 `輸出 Ticket DONE 後，**MUST** 立即呼叫 \`Skill(qa)\`` 之前 | 入口 | 加入 `check done`（見 AI-3） |
| U5 | `skills/uc/SKILL.md` | 規則 8 `MUST STOP 並回報 ⛔ UC Auto…`、Phase 0 `⛔ UC Activation STOPPED…` | K | 票面 Header／載體缺失之 fail-closed 前置，與 Transition Table 末列「Header 缺失或非法 MUST STOP」同義，不編輯 |
| Q1 | `skills/qa/SKILL.md` | Step 0 `⛔ FAIL → **MUST** 立即回報違規項目，**不執行 assembleDebug**` | R＋入口 | 保留「不執行 assembleDebug」；終點回指 Gate；Step 0 開頭加入 `check qa`（見 AI-4） |
| Q2 | `skills/qa/SKILL.md` | Anchor Drift STOP `需人裁決` | R | 觸及本票 ALLOWED 檔之 commit＝疑似 scope／基線問題，驗證先行類（先取證＋Advisor），回指 Gate |
| Q3 | `skills/qa/SKILL.md` | Step 1 `第 3 次仍失敗 → 回報 \`🔴 QA BLOCKED: Compile\` 並**停止整個 QA 流程**` | R | 步驟級上限，記 `step_limit` 並回指 Gate；**不改數字（2 次重試）** |
| Q4 | `skills/qa/SKILL.md` | `承接條件（缺一仍 STOP）`、`其餘規則衝突維持 Rule 11 停下確認` | R | 規則衝突＝驗證先行類（`SSOT_CONFLICT`／`PRODUCT_DECISION`），回指 Gate，保留「不得推廣為任意衝突之自動裁決」 |
| Q5 | `skills/qa/SKILL.md` | Step 4.5 `BLOCKED: tmp/verify_cmd.conf 不存在`、`ALLOWED 為空`、`⛔ Scope 汙染`、`Malformed ALLOWED 防禦 … **STOP**` | R | 終點回指 Gate（可自修者——如還原本票越權變更、修正 conf——修復後重跑；需擴 ALLOWED 才 `HUMAN_REQUIRED(SCOPE)`）；保留「不執行 commit」「逐字引述 conf」等防禦動作 |
| Q6 | `skills/qa/SKILL.md` | Step 4.5 第 5 項 `git commit` 之前 | 入口 | 加入 `check commit`（見 AI-4） |
| Q7 | `skills/qa/SKILL.md` | `Auto-Chain 停止條件 … 任一 QA Step FAIL … ⛔ Auto-Chain STOPPED` | R | 「任一 QA Step FAIL」回指 Gate；「context 剩餘 ≤ 30K tokens」資源上限條不動 |
| S1 | `skills/sv/SKILL.md` | 開場 `任一階段失敗則停止並回報` | R | 回指 Gate；保留「失敗階段後續不續行」 |
| S2 | `skills/sv/SKILL.md` | `前置檢查：Ticket 已 Commit Gate` 之 `**MUST** 停止整個 SV 流程` | R＋入口 | 保留「未 commit 不進階段 A」；該節開頭加入 `check sv`；未 commit 為可自修（回 `/qa` Step 4.5）→ 回指 Gate |
| S3 | `skills/sv/SKILL.md` | 階段 A `第 3 次仍失敗則回報 \`🔴 SV FAIL: Compile error\` 並停止` | R | 步驟級上限，記 `step_limit`；不改數字 |
| S4 | `skills/sv/SKILL.md` | 檔案邊界檢查 `**MUST** 立刻回報 \`🚫 Scope Violation\`` | R | 回指 Gate（還原本票越權變更屬可自修；需擴 scope 才 `HUMAN_REQUIRED(SCOPE)`） |
| S5 | `skills/sv/SKILL.md` | 退出碼對應 `exit 2 … ⛔ 人工介入`、Fail Budget `R2 BLOCKER → ⚠️ Checkpoint 警告，等待裁決`、`R3 BLOCKER → ⛔ 強制停止 (exit 2)，需人工審查` | R | `cross_review.sh` 預算與 `CHECKPOINT_REPORT_FIX_LIMIT` 為步驟級事件：記 `step_limit` 並回指 Gate；**不改 exit 語意與預算數字**；「`CHECKPOINT_REPORT_FIX_LIMIT` 不得自動重跑」保留；「REPORT_FIX report-only 閉環」②之『停止等待人工裁決』同為 exit 2 步驟級事件，一併改為停止閉環重跑＋記 `step_limit`＋回指 Gate（執行期補漏，見收尾實錄） |
| S6 | `skills/sv/SKILL.md` | 回報格式 `仍有 Y 項需人工介入` | R | 改為回指 Gate 的續作／`HUMAN_REQUIRED` 描述 |
| S7 | `skills/sv/SKILL.md` | Step 11 第 1 項 `入口冪等` 判定之後、第一個 registry／票檔寫入之前 | 入口 | 加入 `check closeout`（見 AI-5；S4）；入口冪等 `MUST RETURN` 與 `file` 仍指 `Pending/` 的矛盾停止兩路徑**不**呼叫 `check` |
| M1 | `skills/compact/SKILL.md` | 4.3 closeout `**MAY** 提出 Git stage / commit 建議，但必須等待顯式確認` 與 8.2 `**MAY** 依使用者確認逐檔 \`git add\`` | R（調和） | 與同檔「Auto Commit: Governance Artifacts」節（closeout 路徑 **MUST** 機械執行）矛盾：Governance Artifact 之 commit 由該節機械執行、不另等待確認；非 Governance Artifact 之 stage／commit 維持須顯式確認（D-SCOPE：排除攔截≠排除計數／恢復義務，一併明載） |
| M2 | `skills/compact/SKILL.md` | 6.2 handoff 建議內容 `## Dashboard（新 session 先讀這段）` | 入口（resume） | 加入一行：有 active 票時，新 session 先 `gate_ledger.py status --json` 讀 open incident／待人工（見 AI-6） |
| M3 | `skills/compact/SKILL.md` | `⛔ Closure Gate FAILED`、`UNKNOWN_LIFECYCLE`、`⛔ Auto Commit BLOCKED/FAILED` | K | 冊面／票檔矛盾停止＝Closure 紀律「矛盾停止」，非等待 Akira 路由，不編輯 |
| G1 | `rules/ssot-governance.md` | `契約狀態：本節為契約文字。所稱 helper、ledger、Stop hook、入口 check 呼叫與三輪機械停損均為後續票目標，目前尚不存在…` | R（狀態句） | AI-2：改為 helper 與 ledger 已存在（ADV-GATE-02）、入口 `check` 已接入且 `resume` 讀 `status`（ADV-GATE-03）、呼叫路徑屬模型契約不被機械阻擋、「本 Gate 專用 Stop guard 與升報通知對齊」尚未實作（待後繼票）、Advisor 採納之驗證限制照實保留；刪「機械計數」半句，保留不得宣稱 hook 阻擋／完整 Gate |
| G2 | `rules/ssot-governance.md` | `正常入口最小落點（目前尚未加入呼叫，屬後續票）` | R（狀態句＋S5 映射） | AI-2：括註改為「入口 check 呼叫已接入（ADV-GATE-03）。」並於同條承載 S5 退出碼映射（單一來源） |
| G3 | `rules/ssot-governance.md` | `本節只交付契約；helper、Stop hook、入口 check 呼叫、三輪機械停損、Advisor 採納之驗證、PPSS 取回／證據記錄皆尚不存在。` | R（狀態句） | AI-2：改為與 G1 一致的現況陳述，尚未實作者（本 Gate 專用 Stop guard 與升報通知對齊）與限制（Advisor 採納之驗證）如實保留 |

### AI-1: `.claude/CLAUDE.md` — Boot `resume` 讀 status、Rule 1 驗證先行（C1、C2；C3／C4 不動）
- **檔案**: `.claude/CLAUDE.md`
- **現值**: C1、C2 錨點（見處置表）
- **目標值**:
  - C1（Boot Sequence）：於「輸出 Boot Report」之前加入**一行**：有 active 票（`tmp/verify/active.json` 之 `ticket_id`）時，先執行 `python .claude/scripts/gate_ledger.py status --json`（`--ticket` 省略即讀 `active.json`）；有 open incident 或待人工者，依 Gate 續作或輸出 `HUMAN_REQUIRED`，**不以「停止等 Ticket」收尾**（與 Rule 4b 但書一致：同票由既有事實唯一決定之後續）；無 open incident 者維持原 Boot 行為；`status` 非 0（含無票 exit 3、ledger IO 失敗）→ 記原始錯誤與「ledger 狀態未知」，可續行 Boot／讀取／取證，**不得當作無 open incident**；後續受 Gate 控制的動作須先通過第一個對應 `check`，不得偽造事件、重置 ledger 或繞過檢核。無 active 票時原「停止等 Ticket」不變（C3）。
  - C2（Rule 1）：`不假設。有歧義就問，不要靜默選擇。` 改為：歧義先本地取證與 Advisor；仍屬不可推導之產品語義才依 Gate 輸出 `HUMAN_REQUIRED(PRODUCT_DECISION)` 並以對話正文提問；不得靜默選擇。同一 Rule 其餘提問格式條款位元組不變。
  - 淨增 ≤ +3 行（129 → ≤132，上限 150）；Rule 4a／4c／4d 位元組不變。
- [x] 完成：`.claude/CLAUDE.md` Boot 加入 `status --json` 讀取段（含 A.1-1 非 0 處置）、Rule 1 改驗證先行；Rule 4a／4c／4d 未動；130 → 132 行

### AI-2: `rules/cross-review.md`、`rules/kairos-lessons.md`、`rules/ssot-governance.md`（三處）— X2、X3、X4、K1、G1–G3（X1 不動）
- **檔案**: `.claude/rules/cross-review.md`、`.claude/rules/kairos-lessons.md`、`.claude/rules/ssot-governance.md`
- **現值**: X2、X3、X4、K1、G1–G3 錨點
- **目標值**:
  - X2：「第 2 次 FAIL」與「次數限制」兩處之終點改為記 `step_limit` 並回指 Gate；刪除「完成後立即回報等 ack」「第 2 次 → 必須停」的獨立終點措辭（不新增例外句）；X1 兩款觸發條件原樣保留。
  - X3：「退出機制」改為：ack 僅作為 `EXHAUSTED` 後續跑之恢復門（`HUMAN_REQUIRED(COST)`）；步驟級預算用盡後再重跑 `cross_review.sh` 不是已授權策略；report-only REPORT_FIX 閉環括註與 `CHECKPOINT_REPORT_FIX_LIMIT` 指涉字面保留。
  - X4：Checkpoint 格式區塊保留，另加一句：僅於 `HUMAN_REQUIRED` 成立時輸出。
  - K1：K3-Rule 26 之「回報 CSO，等待裁決是否擴充 ALLOWED 或開後續 Ticket」改為驗證先行類 `SCOPE`（先本地取證與 Advisor）並回指 Gate；盤點指令與「嚴禁只修觸手可及的路徑」條文不動。
  - G1（L175）：`契約狀態` 句改為 S6 目標內容；不寫條件語（不寫「本票完成驗收後」）、不寫「專案沒有 Stop hook 或通知」；原「MUST NOT 宣稱已有機械計數或 hook 阻擋」拆分——刪「機械計數」半句，保留「MUST NOT 宣稱已有 hook 阻擋」並明載不得宣稱完整 Gate。
  - G2（L216）：括註「（目前尚未加入呼叫，屬後續票）」改為字面「入口 check 呼叫已接入（ADV-GATE-03）。」，其後承載 S5 退出碼映射（0／1／2–6 三列語意與「不與 cross_review.sh 的 exit 2 或 Gate 三輪合併計數」「不得以偽造事件修復損毀 ledger」兩句，原意不增不減）；`resume` 讀 `status` 非 0 之處置依 S8（記原始錯誤與 ledger 狀態未知、可續行 Boot／讀取／取證、不得當作無 open incident、後續受 Gate 控制動作先過第一個對應 `check`）。
  - G3（L239）：「皆尚不存在」句改為與 G1 一致之現況陳述；「本 Gate 專用 Stop guard 與升報通知對齊」尚未實作、Advisor 採納之驗證限制（內容加密、不可驗）如實保留，不寫成專案沒有 Stop hooks。
  - ssot-governance.md 其餘位元組（含 Transition Table、QA FAIL 列、Gate 其餘謂詞、Rule 4c 相關字面）不變。
- [x] 完成：X2／X3／X4（`cross-review.md`）、K1（`kairos-lessons.md`）、G1／G2／G3（`ssot-governance.md`，3 hunks／3+ 3−，僅三處）

### AI-3: `skills/uc/SKILL.md` — U1、U2、U3、U4（U5 不動）
- **檔案**: `.claude/skills/uc/SKILL.md`（工作樹 CRLF）
- **現值**: U1–U4 錨點
- **目標值**:
  - U1：規則 9 該句保留「`/qa` PASS 前不得進入 SV」，終點改回指 Gate；其餘（`/uc` 不呼叫 SV、不改 `.tmp/sv_report*.md`）不動。
  - U2：「連續 2 次修復仍編譯失敗」→ 改記 `step_limit` 並回指 Gate；**數字 2 不動**。
  - U3：禁區檔案、SSOT 衝突兩例外之終點分別改為驗證先行類 `SCOPE`、`SSOT_CONFLICT`（先取證＋Advisor）並回指 Gate。
  - U4（入口 `done`）：規則 8「輸出 Ticket DONE 後，**MUST** 立即呼叫 `Skill(qa)`」所屬步驟中，於「輸出 `✅ DONE`」**之前**加入：`python .claude/scripts/gate_ledger.py check done --ticket <TICKET_ID>`（`<TICKET_ID>` 取自 `tmp/verify_cmd.conf`，`grep/head/cut`，禁 `source`）；exit 0 續行，非 0 **不輸出 Ticket DONE**，退出碼映射見 `ssot-governance.md` 入口落點句（S5，單一來源）。
- [x] 完成：U1／U2／U3 回指 Gate、U4 `check done`（CRLF 保留，`git diff --numstat` 5+／4−）

### AI-4: `skills/qa/SKILL.md` — Q1–Q7（入口 `qa`、`commit`）
- **檔案**: `.claude/skills/qa/SKILL.md`（工作樹 CRLF）
- **現值**: Q1–Q7 錨點
- **目標值**:
  - 入口 `qa`（Q1）：`## Step 0` 於「若 `tmp/verify_cmd.conf` 存在，執行：」區塊、執行 `verify_checklist.sh` **之前**加入：`python .claude/scripts/gate_ledger.py check qa --ticket <TICKET_ID>`（取得方式同 U4）；exit 0 續行，非 0 **不續行** Step 0，退出碼映射見 `ssot-governance.md` 入口落點句（S5）。`tmp/verify_cmd.conf` 不存在時沿用既有「跳過 Step 0」，不呼叫 `check`。
  - 入口 `commit`（Q6）：Step 4.5 第 5 項 Commit 之前加入 `python .claude/scripts/gate_ledger.py check commit --ticket <TICKET_ID>`；exit 0 續行，非 0 不執行 `git commit`，退出碼映射同上。
  - Q1–Q5、Q7 之終點改回指 Gate（見處置表各列保留項）；**不改任何步驟上限數字、不改 Step 5 verdict/exit 語意**。
- [x] 完成：Q1–Q7（`check qa`、`check commit`、終點回指；數字未動；CRLF 保留，12+／11−）

### AI-5: `skills/sv/SKILL.md` — S1–S7（入口 `sv`、`closeout`）
- **檔案**: `.claude/skills/sv/SKILL.md`
- **現值**: S1–S7 錨點
- **目標值**:
  - 入口 `sv`（S2）：`## 前置檢查：Ticket 已 Commit Gate` 於讀取 `tmp/verify_cmd.conf` 後、階段 A 之前加入 `python .claude/scripts/gate_ledger.py check sv --ticket <TICKET_ID>`；exit 0 續行，非 0 **不進階段 A**，退出碼映射見 `ssot-governance.md` 入口落點句（S5）。
  - 入口 `closeout`（S7）：`## Step 11` 第 1 項「入口冪等」判定**之後**、第一個 registry／票檔寫入**之前**加入 `python .claude/scripts/gate_ledger.py check closeout --ticket <TICKET_ID>`；exit 0 續行，非 0 **不寫任何 registry／不搬移票檔**，退出碼映射見 `ssot-governance.md` 入口落點句（S5）。入口冪等命中（票在 `deliver/`、冊面 `REVIEW`／`CLOSED`、`file` 一致）→ `MUST RETURN`，**不呼叫** `check`；票在 `deliver/` 而冊面 `file` 仍指 `Pending/` → 不 RETURN、**不呼叫** `check`，交既有 Closure Gate 與「矛盾停止」，不新增恢復流程（S4）。
  - S1、S3、S4、S5、S6 終點改回指 Gate；`cross_review.sh` 預算、exit 碼對應、REPORT_FIX 閉環七條件（唯一來源仍為 `ssot-governance.md` Transition Table）與「`CHECKPOINT_REPORT_FIX_LIMIT` 不得自動重跑」字面保留，**不改數字與 exit 語意**。
- [x] 完成：S1–S7（`check sv`、`check closeout` 置於入口冪等之後、首個寫入之前；矛盾路徑不呼叫）

### AI-6: `skills/compact/SKILL.md` — M1、M2（M3 不動）
- **檔案**: `.claude/skills/compact/SKILL.md`
- **現值**: M1、M2 錨點
- **目標值**:
  - M1：4.3 closeout 與 8.2 之「等待顯式確認／依使用者確認逐檔 `git add`」改為：Governance Artifact 之 commit 由本檔「Auto Commit: Governance Artifacts」節機械執行，不另等待確認；非 Governance Artifact 之 stage／commit 維持須顯式確認；並明載「Gate 不攔截使用者指令（`/compact`／`/clear`），但不排除計數與恢復義務」（D-SCOPE）。不改該 Auto Commit 節與冊面收束一致性閘本文。
  - M2：`## Dashboard（新 session 先讀這段）` 之建議內容加入一行：有 active 票時先 `python .claude/scripts/gate_ledger.py status --json` 讀 open incident／待人工。
- [x] 完成：M1 調和（Governance Artifact 機械 commit／非 Governance 須確認／D-SCOPE 明載）、M2 Dashboard 加 `status --json` 恢復行

### AI-7: 驗證與收尾證據
- **檔案**: （無修改；票檔收尾實錄）
- **動作**: 重跑 AI-0 固定盤點樣式，將「每一命中的歸類」（處置表列號或 `K-不涉停止/等待語意`）貼入收尾實錄；比對 Rule 4c 整行 `sha256`、`ssot-governance.md` 除 AI-2 三處外之 `sha256`（AI-0 記錄）與 `git diff -U0`（`ssot-governance.md` 僅三處有 hunk，不觸及 Rule 4c／Transition Table）；執行 AC 之 helper 指令 smoke（暫存 repo）與 EOL／行數檢查；記錄 `[行為性]` 重放（n=1 弱證據）；列出 `盤點新增`（若有）。收尾**不得**宣稱「完整 Gate」，**驗收通過前不得宣稱入口接線已驗證**（S6）；Cross-Review 走 `cross_review.sh` 現役入口，註明 payload 審查，**不預填 PASS**。
- [x] 完成（見「收尾實錄」）

## Hard Constraints
1. **範圍**：僅 ALLOWED 八檔（`ssot-governance.md` 僅 AI-2 三處）；不改任何腳本／hook／settings／registry／`PROGRESS*`；`git diff --name-only` ⊆ ALLOWED。
2. **S1 不改 helper**：只使用 `gate_ledger.py` 現有子命令與 exit code；不得要求新增旗標或改 exit 語意。
3. **S2 不重複**：Rule 4c 整行與 Transition Table `QA FAIL` 列位元組不變；不改 `ADV-RULE4C-01` 已驗收之任何字面。
4. **S3 不追加補丁**：不新增「例外」「過渡期」「暫時」類並列條款；R 列一律改終點或回指；回指句為 `→ 依 ssot-governance.md §Autonomous Engineering Gate 處置`，**MUST NOT** 在回指處複製輪數、`HUMAN_REQUIRED` 類別清單或 GO 謂詞（避免第二來源）。
5. **不改數字與語意**：`uc` 連續 2 次、`qa` 編譯 2 次重試、`sv` 編譯 2 次重試、`cross_review.sh` BLOCKER 預算、REPORT_FIX 上限、`CHECKPOINT_REPORT_FIX_LIMIT`、各 exit 碼語意一律不動（D-NEST）。
6. **立即類／驗證先行類不弱化**：C3、X1、U5、M3 等 K 列不編輯；R 列不得使「需新增權限、擴 scope、不可逆動作、無票」變成可自行續作。
7. **check 呼叫形狀固定**：僅 `python .claude/scripts/gate_ledger.py check <qa|commit|sv|closeout|done> --ticket <TICKET_ID>` 與 `status --json`；退出碼處置一律回指 `ssot-governance.md` 入口落點句之映射（S5，單一來源），呼叫處不複製映射；不得自創子命令或別名；不得在無 `tmp/verify_cmd.conf`（無 active 票）時呼叫 `check`。
8. **EOL**：`uc`／`qa`（工作樹 CRLF、index LF）編輯 **MUST NOT** 造成整檔換行改寫：`git diff --stat` 對該二檔之變更行數 = 實際編輯行數，`git ls-files --eol` 仍為 `i/lf`；其餘五檔維持 `w/lf`。`.claude/CLAUDE.md` ≤ 150 行；寫入後 mdlint 無 warning。
9. **完整性宣告律**：收尾不得宣稱「完整 Gate」；本票交付僅為 consumer 對齊與入口 `check` 呼叫接線，hook（T04）與通知（T05）未完成；呼叫路徑屬模型契約，不被阻擋。
10. **不取號、不開他票**：不新增或修改其他票檔／冊面。
11. **SSOT 字面與宣告紀律（S6、S9）**：SSOT 明載「本 Gate 專用 Stop guard 與升報通知對齊」尚未實作，不寫成專案沒有 Stop hooks，Advisor 採納之驗證限制照實保留；`ssot-governance.md` 三處字面不得帶條件語（如「本票完成驗收後」），不得寫「專案沒有 Stop hook 或通知」，三處以外不改、不新增治理說明；入口 `check` 已接入 ≠ Stop hook 已接入 ≠ 完整 Gate 已交付；驗收通過前，收尾與回報不得宣稱入口接線已驗證。

## Error Handling
- AI-0 錨點／TARGET_BLOB 不符 → 依 TPL v3.11 三分流分類處置（見 AI-0）；行號漂移、措辭微變只重讀補證，不停止。
- Gate 節或 helper Usage 與本票引用不一致，或 `ADV-GATE-03` 被另一張不同票重號、冊面 `file` 與票檔落點不一致 → 停止回報（本票自身存在不算占號）。
- AI-0 盤點出處置表未列之直接停止／等待／人工類條款 → **不自行擴張處置**，列入「盤點新增」並停止回報，由 SMTR／CSO 決定是否擴 ALLOWED 或另票。
- 某 R 列處置需使「需新權限／擴 scope／不可逆動作」變成可自行續作才能達成 → 停止回報（`STOP_REPLAN_REQUIRED` 性質）。
- 需要修改 `ssot-governance.md` 於 AI-2 三句以外、`gate_ledger.py` 或任何腳本才能達成 AC → 停止回報，不擴 ALLOWED。
- 入口 `check` 退出碼處置需新增映射以外的恢復流程（例如修復損毀 ledger、為矛盾票況補寫事件）才能達成 AC → 停止回報；映射內「不得以偽造事件修復損毀 ledger」不得被繞過。
- `git diff` 顯示 `uc`／`qa` 整檔換行改寫（變更行數遠大於編輯行數）→ 還原重做；無法還原 → 停止回報。
- helper smoke（暫存 repo）無法建立 → 記錄具體錯誤，該 AC 標未驗，不得改用真實 `.git`／真實第二 worktree 寫入。
- 連續 2 次修復仍無法使 AC 轉綠 → 停止回報（步驟級上限；依 Gate 記為 `step_limit`，不自行升報）。
- `git check-ignore -v <ALLOWED 標的>` 命中 → 停止回報（gitignore 死結律）。

## Acceptance Criteria
- [x] **[存在性]** 範圍與基線：`git diff --name-only <DIFF_REF>` ⊆ ALLOWED 八檔；Rule 4c 整行 `sha256` 與 AI-0 記錄逐位元組相同；`git diff -U0 <DIFF_REF> -- .claude/CLAUDE.md` 無任何 hunk 觸及 Rule 4a／4c／4d 行；`ssot-governance.md` 僅 AI-2 三處有 hunk（Transition Table 節、Gate 節除三處外 `sha256` 與 AI-0 記錄相同）；`scripts/**` 零變更；`.claude/CLAUDE.md` ≤ 150 行。 〔證據：`git diff --name-only b712fb3e` 為 ALLOWED 八檔之子集；Rule 4c 整行與 Transition Table 節 sha256 與基線相同；`ssot-governance.md` 3 hunks；`CLAUDE.md` 無觸及 Rule 4a／4c／4d 之 hunk、132 行（`verify03.py` 55 項斷言 PASS）〕
- [x] **[存在性]** 條款盤點：AI-0 固定樣式於七個 consumer 檔（不含 `ssot-governance.md`）重跑，每一命中皆歸入處置表列或 `K-不涉停止/等待語意`，且 R 列錨點的舊終點措辭（如「`**停止等 Ticket**`」僅限無 active 票語境、`等待裁決`、`需人工審查`、`回報等 ack`）不再作為獨立終點存在；`盤點新增` 為空（非空則依 Error Handling 停止，不勾）。 〔證據：固定樣式重跑後逐命中歸類見收尾實錄；R 列舊終點措辭已清除（殘留僅 `cross-review.md` X1 保留之 Checkpoint 標題與其格式列，其格式列已註明僅於 `HUMAN_REQUIRED` 成立時輸出）；`盤點新增`：複查發現 `sv/SKILL.md` 閉環②一處（歸處置表 S5 之執行遺漏，已於 `a18a0ee5` 修正，非新增處置範圍），其餘為空〕
- [x] **[存在性]** 回指不複製：七個 consumer 檔中新增的回指句皆含 `ssot-governance.md` 與 `§Autonomous Engineering Gate`，且新增行內**不含**「3 輪」「`NO_AUTHORIZED_STRATEGY`」「`EXHAUSTED`」等 Gate 謂詞字面（僅 X3 必須提及 `EXHAUSTED` 恢復門，為 D-ACK 之指定例外，列於收尾實錄）。 〔證據：七個 consumer 檔新增行皆回指 `ssot-governance.md` §Autonomous Engineering Gate，且不含「3 輪」／`NO_AUTHORIZED_STRATEGY`；`EXHAUSTED` 僅見 `cross-review.md` X3（D-ACK 指定例外）〕
- [x] **[存在性]** 互斥矩陣：無任一條款同時要求「**MUST** 停止／等待 Akira」且（同事件）「**MUST** AUTO-CHAIN／續作」；R 列改寫後與 Transition Table「QA FAIL」「SV 或 Codex 判 BLOCKER」「REPORT_FIX」列不矛盾（逐列比對結果落收尾實錄）；數字（uc 2、qa 2、sv 2、exit 碼）未被改動（`git diff -U0` 無數字變更）。 〔證據：兩個 fresh subagent 重放與逐列比對均未發現互斥（QA FAIL 列與 qa Step 0、SV BLOCKER／REPORT_FIX 列與 sv 退出碼對應）；數字 uc 2、qa 2、sv 2、exit 碼未改（`git diff -U0` 無數字變更）〕
- [x] **[存在性]** 入口落點：`check` 呼叫五處（各恰一處）與 `status` 讀取兩處，位置在表列字面錨點之下、不可逆動作之前，且參數與表一致——`qa`（Step 0）、`commit`（Step 4.5 第 5 項前）、`sv`（前置檢查）、`closeout`（Step 11 第 1 項入口冪等判定之後、第一個 registry／票檔寫入之前）、`done`（uc 規則 8 輸出 `✅ DONE` 前）；`resume`＝`.claude/CLAUDE.md` Boot Sequence 與 `compact/SKILL.md` Dashboard 建議內容各一行 `status --json`；皆不引用行號。 〔證據：`check qa`／`check commit`／`check sv`／`check closeout`／`check done` 各恰 1 處且順序斷言通過；`status --json` 於 `.claude/CLAUDE.md` 與 `compact/SKILL.md` 各 1 處〕
- [x] **[存在性]** SSOT 三處與映射單一來源：`ssot-governance.md` 字面「入口 check 呼叫已接入（ADV-GATE-03）。」恰 1 筆且無條件語；L175 句含「helper 與 ledger 已存在（ADV-GATE-02）」、不含「機械計數」且仍含「hook 阻擋」與「完整 Gate」禁述；L239 句不再含「皆尚不存在」整句；全檔不含「本票完成驗收後」「專案沒有 Stop hook」；L175／L239 明載「本 Gate 專用 Stop guard 與升報通知對齊」尚未實作並保留 Advisor 採納之驗證限制；S5 映射（0／1／2–6 三列）僅出現於該入口落點條，七個 consumer 檔內**不含**映射副本（`grep` 於 `2–6`、`不代表有待人工` 零命中）。 〔證據：「入口 check 呼叫已接入（ADV-GATE-03）。」恰 1 筆；L175 無「機械計數」且保留 hook／完整 Gate 禁述；「皆尚不存在」「本票完成驗收後」「專案沒有」零命中；映射僅在 `ssot-governance.md`，七個 consumer 檔無 `2–6`／`不代表有待人工`〕
- [x] **[可達性]** SSOT 狀態字面所稱「已存在／已接入」皆有 production 證據（執行後回填 `檔案:行號`）：helper＝`gate_ledger.py`＿＿（ADV-GATE-02 commit `cc541546`／`0452a579`）；入口接線＝五處 `check` 呼叫與兩處 `status` 讀取所在檔案行＿＿；「Stop guard 與升報通知對齊尚待後繼票」之現況＝`.claude/settings.json` Stop 陣列現有三 hook（無 Gate 專用）＿＿——僅陳述，不寫入 SSOT「專案沒有」類字面。 〔證據：helper＝`gate_ledger.py` `cmd_status`(:517)／`cmd_check`(:801)（ADV-GATE-02 `cc541546`／`0452a579`）；入口接線＝`.claude/CLAUDE.md:9`、`qa/SKILL.md:17,191`、`sv/SKILL.md:14,212`、`uc/SKILL.md:49`、`compact/SKILL.md:187`；`.claude/settings.json` Stop 陣列現有三 hook（`stop.sh`、`telegram_stop.sh`、`registry_review_stop_guard.py`，無 Gate 專用）——僅陳述，未寫入 SSOT 之「專案沒有」類字面〕
- [x] **[可達性]** producer 證據欄（執行後回填 `檔案:行號`）：每個被呼叫的子命令與旗標在 `gate_ledger.py` 檔頭 Usage 與實作中存在——`check`＝＿＿、`status --json`＝＿＿、`--ticket` 置於子命令後＝＿＿、exit code 0–6＝＿＿；`<TICKET_ID>` 取得來源 `tmp/verify_cmd.conf`（writer＝`activate_ticket.sh`＿＿／`verify_checklist.sh`＿＿）。 〔證據：`check`＝`gate_ledger.py:40`（Usage）／`cmd_check`:801；`status --json`＝:24／`cmd_status`:517；`--ticket` 置於子命令後＝:21（Usage）；exit code 0–6＝:43–44（Usage）；`<TICKET_ID>` 來源 conf：writer＝`activate_ticket.sh:187`、`verify_checklist.sh:55`〕
- [x] **[行為性]** 文件所載指令於暫存環境可執行且退出碼語意與文件一致：given 暫存 git repo（`tempfile`，不觸真實 `.git`）含 `Pending/` 票檔與 `tmp/verify_cmd.conf`（`TICKET_ID`）：expect 文件所載 `check qa|commit|sv|closeout|done --ticket <ID>` 在無待人工時 exit 0；寫入一筆 `human --category PERMISSION` 後同指令 exit 1 並印出 `PENDING_HUMAN`；`status --json` 在票移至 `deliver/` 後仍 exit 0 並含 `ticket_location`；無票／票非 `loc:pending` 時 `check` exit 3／4（驗證 S5 映射「2–6＝檢核未完成、不代表有待人工、不續行」之觸發條件為真實可達；exit 1 與 exit 3／4 在輸出上可區分；`status` 無票時 exit 3，S8 所述處置〔記原始錯誤、ledger 狀態未知、不當作無 open incident〕之觸發條件真實可達）。 〔證據：`verify03.py`（暫存 repo）：五個 `check <gp> --ticket` 無待人工 exit 0；`human PERMISSION` 後 `check closeout` exit 1 且印 `PENDING_HUMAN`；無票 `check`／`status` exit 3；票移至 `deliver/` 後 `status --json` exit 0 含 `loc:deliver`、`check closeout` exit 4〕
- [x] **[行為性]** `check closeout` 落點兩案（S4，文件層情境重放＋暫存 repo 驗證）：given (a) 票檔在 `deliver/`、冊面 `lifecycle` 為 `REVIEW` 或 `CLOSED`、`file` 一致：expect 讀 `sv/SKILL.md` Step 11 者依入口冪等判定 `MUST RETURN`，**未呼叫** `check`（且暫存 repo 中對 `deliver/` 之票呼叫 `check closeout` 本應 exit 4，證明若錯放於冪等判定之前會誤擋）；given (b) 票檔在 `deliver/`、冊面 `file` 仍指 `Pending/`：expect **不 RETURN、不呼叫** `check`，依 `ssot-governance.md` §最小冪等回傳交既有 Closure Gate，並依 §Ticket Closure 紀律「矛盾停止」回報；文件**不**出現任何新增恢復流程（無「補寫事件」「搬回」「改寫 file」類步驟）。重放為 n=1 弱證據；出現互斥讀法 → 記 REPORT_FIX 級缺口，不勾。 〔證據：fresh subagent 重放 A（n=1，弱證據）：case (a) 讀出入口冪等 `MUST RETURN`、不呼叫 `check`；case (b) 讀出不 RETURN、不呼叫 `check`、交 Closure Gate 與矛盾停止、無新恢復流程；暫存 repo 證明若 `check closeout` 錯放於冪等判定之前，對 `deliver/` 票會 exit 4 誤擋；未發現互斥讀法〕
- [x] **[行為性]** 文件層重放（n=1，弱證據，誠實標示）：以兩個 fresh subagent（不繼承本 session 規則快取）各讀 `.claude/CLAUDE.md` 與對應 SKILL，重放 (a) `qa` Step 0 FAIL（越權變更可由本票還原）：expect 讀出「依 Gate 修復後重跑」、不讀出「停下等待 Akira」，且入口先呼叫 `check qa`；(b) `uc` 連續 2 次編譯失敗：expect 讀出「記 `step_limit` 並依 Gate 處置」、數字 2 未變。重放出現互斥讀法 → 記為 REPORT_FIX 級缺口，不勾。 〔證據：fresh subagent 重放 A：qa Step 0 FAIL（可還原）→ 讀出入口先 `check qa`、FAIL 後走 Gate 自修重跑、不等 Akira；重放 B：uc 連續 2 次編譯失敗 → 讀出記 `step_limit` 依 Gate、數字 2 未變；另重放 `done` 入口 exit 0／1／2–6 與 `resume` `status` 非 0 處置；皆未發現互斥讀法。重放 B 觀察（非互斥）：`uc` 例外清單之 emoji 回報行與 `HUMAN_REQUIRED` 單行是否並用，文字未明說〕
- [x] **[存在性]** EOL／格式：`uc`／`qa` `git ls-files --eol` 為 `i/lf`，`git diff --stat` 對該二檔變更行數與實際編輯行數一致（無整檔換行改寫）；其餘五檔 `w/lf`；mdlint 無 warning。 〔證據：`uc`／`qa` `git ls-files --eol` 為 `i/lf`，工作樹仍全為 CRLF（`\n` 數＝`\r\n` 數），`git diff --numstat` 為 5+／4−、12+／11−（等於實際編輯行數）；其餘六檔 `w/lf`；**mdlint 以人工核對新增行（未觸及標題、fence、表格，MD022／025／026／029／032／036／040／055／060 皆不受影響），未另跑 mdlint skill**〕
- [x] **[存在性]** D-SCOPE：`compact/SKILL.md` closeout 確認點與 Auto Commit 節之矛盾已調和（文字明載「排除攔截≠排除計數／恢復義務」），且 Auto Commit 節與冊面收束一致性閘本文位元組不變。 〔證據：`compact/SKILL.md` 4.3 與 8.2 已調和並明載「Gate 不攔截使用者指令，但不排除計數與恢復義務」；Auto Commit 節與冊面收束一致性閘本文未動（diff 僅 3 處：4.3、8.2、Dashboard）〕
- ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案 〔證據：`git diff --name-only b712fb3e..a18a0ee5` 僅 ALLOWED 八檔〕


## 收尾實錄（Agent closeout，2026-10-13）

- **交付**：`0386ec9e`（初版）＋`a18a0ee5`（執行期補漏，+1／−1）；`git diff --shortstat b712fb3e..a18a0ee5`：8 files、42 insertions、37 deletions，全在 ALLOWED 八檔內。`ssot-governance.md` 僅 3 個 hunk（契約狀態句、入口落點句〔承載 `check` 退出碼映射，單一來源〕、「只交付契約」句）；Rule 4c 整行與 Transition Table 節 sha256 與開工基線相同；`.claude/CLAUDE.md` 本票改動後 132 行。Rev A.1 兩項：A.1-1（`resume` 讀 `status` 非 0 → 記原始錯誤與 ledger 狀態未知、可續行 Boot／讀取／取證、不當作無 open incident、後續受 Gate 控制動作先過第一個對應 `check`）見 `CLAUDE.md` Boot 段與 SSOT 映射；A.1-2（SSOT 明載「本 Gate 專用 Stop guard 與升報通知對齊」尚未實作、保留 Advisor 採納之驗證限制，未寫「專案沒有 Stop hooks」）見 `ssot-governance.md` 兩處狀態句。
- **條款盤點（固定樣式，七個 consumer 檔，改後命中數與歸類）**：`.claude/CLAUDE.md` 7＝Boot 停止行／resume 新增段（R，C1）、Boot 範本 `無 → 停止` 與 Rule 4a／4d（K，立即類 `HUMAN_GATE`，C3）、Rule 1 提問格式（K）、風險分級裁決敘述（K-不涉停止/等待語意）；`rules/cross-review.md` 5＝X1 觸發標題（K）、X4 Checkpoint 格式列（R，已註明僅於 `HUMAN_REQUIRED` 成立時輸出）、X3 退出機制（R）、R0 路由與記錄格式（K-不涉）；`rules/kairos-lessons.md` 0（K1 已改〔R〕）；`skills/uc/SKILL.md` 10＝規則 8 支持 AUTO 之「不得停下等待」與 `MUST STOP` Header 缺失（K，U5）、規則 9 QA FAIL（R，U1）、唯一例外連續 2 次（R，U2）、Erratum 與 Phase 0 activation 各行（K，U5／不涉）；`skills/qa/SKILL.md` 15＝Anchor Drift（R，Q2）、Preflight 四層／決策核心／R-7 之 `BLOCKED_*` 字面（K-不涉停止/等待）、編譯第 3 次（R，Q3）、Step 4／4.5 SKIP 例外與承接條件（R，Q4）、Step 4.5 前置與 Scope 汙染／Malformed ALLOWED（R，Q5）、Auto-Chain 停止條件（R／資源上限 K，Q7）；`skills/sv/SKILL.md` 11＝未 commit 訊息範本（K，S2）、編譯第 3 次（R，S3）、cross-review 路徑說明與範例腳本 `⛔ SV BLOCKED: DIFF_END 未設定…`（K，自帶修復指示之前置停止）、exit 3／REPORT_FIX 閉環（K，與 Transition Table 同源；其中②為補漏，見下）、R3 BLOCKER（R，S5）、邏輯輪次人工判例帳敘述（K-不涉）、Step 11 入口冪等＋`check closeout`（入口，S7）、未勾項模板之 `裁決中<待裁者>`（K-不涉）；`skills/compact/SKILL.md` 10＝handoff／scoped commit 的 MUST 敘述（K-不涉）、4.3 closeout 確認點（R，M1）、Dashboard 恢復行（入口，M2）、未決檔降級（K-不涉）、冊面收束一致性閘與 Auto Commit 狀態行（K，M3）。**盤點新增**：`sv/SKILL.md`「REPORT_FIX report-only 閉環」②『見 `CHECKPOINT_REPORT_FIX_LIMIT` 或 `REPORT_FIX_ESCALATE`（exit 2）即停止等待人工裁決』——與同檔已改為步驟級事件的 exit 2 退出碼對應行同源不一致，判為處置表 S5 之執行遺漏（`CHECKPOINT_REPORT_FIX_LIMIT` 為步驟級），於 `a18a0ee5` 改為『停止閉環重跑，記 `step_limit`，終點 → 依 Gate 處置』；不改 exit 語意與預算數字。此為依 S5 範圍之補漏，請審查方確認是否接受此歸類。
- **驗證**：`verify03.py`（55 項斷言：範圍子集、Rule 4a／4c／4d 無 hunk、行數、SSOT 字面與禁語、無映射副本、無 Gate 謂詞副本、`uc`／`qa` CRLF 保留、五個 `check` 與 `status` 位置）通過；暫存 repo helper 指令 smoke（五個 `check` exit 0、待人工 exit 1＋`PENDING_HUMAN`、無票 exit 3、移至 `deliver/` 後 `status` exit 0／`check closeout` exit 4）通過；fresh subagent 情境重放 A（qa Step 0 FAIL、closeout case a／b）、B（uc 連續 2 次、`done` exit 分類、`resume` `status` 非 0）皆未發現互斥讀法（n=1，弱證據）；重放觀察（非互斥）：`uc` 例外清單 emoji 回報行與 `HUMAN_REQUIRED` 單行是否並用，文字未明說。`mdlint`：**未另跑 skill**，人工核對新增行未觸及標題、fence、表格。
- **Cross-Review（payload 審查，非 reviewer 獨立執行測試）**：第 1 次（`b712fb3e..0386ec9e`）Codex `REPORT_FIX`（`REPORT_FIX_SOURCE=codex`；SV report 的 gate ledger producer 缺 `檔案:行號`；結果檔 `.tmp/cross_review_result_20261013_130936.md`）→ 補報告證據（`gate_ledger.py:737,538,574,370,376,396,801,517`）→ 同範圍 **PASS**（`…_131052.md`）；補漏 commit 後第 3 次（`b712fb3e..a18a0ee5`）**PASS**（exit 0，`Guard Input Reachability: 有新 Guard — State: gate ledger／tmp/verify/active.json — Producer: (B)`，`REACHABILITY: NONE`；結果檔 `.tmp/cross_review_result_20261013_131305.md`，sha256 前 12 碼 `95164817f88b`）。三次日誌皆無 `exec_command failed`。`structural_verify --post` PASS (A–G) [ADVISORY：`PROGRESS.md` 80 行，與本票無關]。
- **Gate 記錄**（真實 ledger `<common-dir>/gate/ADV-GATE-03.jsonl`）：`INC-001`（SV REPORT_FIX 補 producer 證據，round 1 pass）、`INC-002`（上述補漏，round 1 pass）皆 `closed`，無 pending-human、blocks 0、missed_check 0；`check` receipts：`done`(seq 1)、`qa`(2)、`commit`(3)、`sv`(4)、`closeout`(12) 皆 exit 0，`commit` 另有補漏 commit 前一次(10) exit 0。第一次 Cross-Review REPORT_FIX（`codex` 來源）未等待人工 ack，依 `/uc` 開工指示「scope 內可確定的失敗自行修正、局部回驗」以新證據重跑，未遭權限拒絕。
- **未驗與限制**：入口接線僅以暫存 repo smoke 與 n=1 情境重放驗證，**驗收是否通過待審查方終審，本實錄不宣稱入口接線已驗證**；`check` 呼叫路徑屬模型契約，不被機械阻擋；本 Gate 專用 Stop guard 與升報通知對齊**尚未實作**；Advisor 採納之驗證限制保留；**不得宣稱「完整 Gate」**。T04（Stop guard）與 T05（升報通知）待後繼票。
- **工作樹另有非本票變更**：`.claude/CLAUDE.md` 於本票 commit 之後出現 11 行未提交新增（`## 角色邊界（Role Boundary）`、`## 停止前自我檢查（Pre-Pause Self-Check）` 兩節，非本票所為），**未納入本票 diff、Cross-Review 與 commit**，原樣保留；該節含「同一 incident 最多三次自主處理循環」字樣，與 Gate 節重疊，請審查方另行裁決其歸屬。
- **DEFERRED-TO-DOCSYNC（不開票）**：`PROGRESS.md`／`PROGRESS_ARCHIVE.md` 同步與 Done 標記留待下一輪 `/compact`。
- **Traycer**：`Traycer Workflow: N/A` → `reviews.traycer=N_A`；External-review decision：WAIVED（Akira 2026-10-13，範圍限本票）。
