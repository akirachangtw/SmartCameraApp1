# ADV-APICALL-01: 新增單次、無工具的 Opus 工程顧問 API 呼叫器，讀 payload、產出可讀回件與不含密鑰的 receipt，並以確定性測試覆蓋失敗模式、完成一次真實呼叫驗證

**Revision**: Rev B.4（2026-10-17；Akira INC-001 最終裁決 D，僅文書：SV report 與本票 External-review／限定交付聲明，不改實作、AC、測試；4a 計數 1→2，未成立）。Rev B.3（2026-10-17；Akira 最終裁決落檔：F5 `INTERNAL_ERROR` receipt failure contract、`raw_text` 最小安全邊界、External-review 回流、AI-5 timeout／SDK 註記；4a 計數＝1）。Rev B.2（2026-10-17；RGR cycle 2：F9 共享落點歸因與 AC9 修補）。Rev B.1（2026-10-17；RGR cycle 1：F1–F4、F6–F8 之 AC／HC／行為契約修補）。Rev B（2026-10-17；開票期修訂，Rev=1；只修票文字，Selected item `T01` 與 Goal 不變，不實作、不呼叫模型）。Rev A（2026-10-17；首次開票，Rev=0；檔案 45226 bytes，sha8 `461dcdde`，修訂前）。
**Rev B 差分（逐點）**：①**格式 fallback**——主路徑 `output_config.format=json_schema`；僅當 API 明確拒絕 structured-output 格式或其與 thinking 之相容組合時，授權同票 `prompt_json` fallback（AI-1 #2 #4 #10、AI-3 #3、AI-5 #2 #7、Error Handling、AC 6／9；receipt 新增 `request_mode`）。②**網路嘗試上限**——client `max_retries=0`、腳本不自動重試；真實驗證最多兩次網路嘗試且共用額度，第二次只可擇一（暫態後重試／格式相容拒絕後 fallback／TRUNCATED 後提高上限）；逾時成本不視為零（AI-1 #3 #8 #11、AI-3、AI-5 #2、HC8、AC 6／7／9、`sdk_max_retries=0`）。③**TRUNCATED 上限**——`--max-tokens` 上限 24000（AI-0 #7 先讀本地 SDK 確認非串流可接受，不發模型請求；本次已離線預驗）。④**AI-0 補項 #8**——`claude auth status` 基線；不可用則 AI-4 #5／AI-5 #5 改以限定落點 sha256／存在狀態比對，並訂歸因規則（Error Handling）。⑤**T03 接口提醒**——receipt 位於 `tmp/`（保留 7 日 mtime 清理），T03 須消費可讀取且 hash 一致之耐久副本，位置由 T03 定義；本票只保留提醒（相關約束末條、AI-6）。⑥**缺金鑰口徑**——實作／假 client 測試／靜態檢查照常；AI-5 `NOT_EXECUTED`、`[real]` AC 不勾、不宣稱 S2 顧問可用（AI-5 前置、AC 10、AI-6）。⑦**Rev B 期間環境存在性確認**——見證據分級 ③（僅存在性）。
**4a 停損紀錄**：Rev B＋B.1＋B.2 觸發 4a；外審存廢裁決＝**續修**（Akira 2026-10-17），裁決落檔後 4a 計數歸零；Rev B.3 後計數＝1。
**Rev B.1／B.2 差分**：見 `## Acceptance Criteria` 段首 Rigor Gate 修補紀錄（報告 `.tmp/rigor-gate/RGR_ADV-APICALL-01_20261017.md`）。
**Rev B.3 差分（逐點）**：①**F5 `INTERNAL_ERROR`**——已取得 API response 後之本地未預期 `Exception` 改記 `status="INTERNAL_ERROR"`（exit 1、`advice=null`），盡力寫 receipt，receipt 寫入失敗時 stderr 僅輸出遮蔽摘要；不捕捉 `BaseException`；不觸發重送（AI-1 #8 #10、AI-3 #6、Error Handling、HC6、AC7）。②**`raw_text` 最小安全邊界**（AI-1 #10、AI-3 #5）。③**External-review 回流**（證據分級、6a、現值漂移 ③、AI-5 #7、AI-6 ④）。④**AI-5 一律 `--timeout 600`**，不以 300 為 probe 預設或回退；不新增 streaming 分支（AI-5 #2）。⑤**MockTransport 測試以 `anthropic==0.105.2` 為前提**（AI-3 開頭）。

**檔案治理/來源說明**：本票為 Plan-backed 單票（plan-local item `T01`）。Parent 類型＝Plan-backed；Parent 絕對路徑＝`D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261016-cli-advisor-plan.md`（`.tmp` 非長期載體，下列內容使本票獨立成立）；Parent artifact status＝`Fixture: false`／`Plan Gate: PASS`／`Ticket mode eligibility: ALLOWED`；plan-local item＝`T01`；Parent read timestamp＝2026-10-17 00:5x（+08:00，全文實讀，156 行）。**標頭差異（如實）**：使用者 2026-10-17 訊息稱 Rev C.4 已獨立複審通過、可依 T01 → T03 → T02 開票；artifact 標頭仍載「Rev C.4 尚未經獨立複審」（標頭未更新）——C.4 增量僅涉 T03／T02，**T01 條文自 Rev C.3 起未變**，故不阻擋本票。Template 實讀：`D:\DevSources\AndroidProj\SmartCameraApp1\plans\cso_briefing\project\file\Unified_Ticket_Template_v3.md`，版本 v3.11，文件產出日期 2026-05-29，最近有效修訂日 2026-09-14（Revision 歷史末筆 v3.11）。**Rev B 重讀（2026-10-17，全文）**：parent artifact（156 行）與 Template（v3.11，同上日期）皆重新實讀，parent T01 條文與 Rev A 時一致（標頭 Rev C.4 狀態仍同）；本票 Rev B 七點屬開票期對 T01 實作細節之修訂，**不改 parent 的 T01 goal 與 acceptance_intent 主旨**（唯一偏離：parent「429／5xx 有限重試」改為「腳本不自動重試、呼叫端至多一次」，已列現值漂移 ⑥）。

**Selected item 原文（T01 goal，摘要不改義）**：新增單一呼叫器：讀 payload 檔 → 以專用變數憑證呼叫 `claude-opus-5-5`（單次、無工具、固定顧問角色、`output_config.format` 固定回件欄位）→ 寫 API 顧問回件紀錄 receipt（`schema_version`、模型、時間、`usage`、估算費用、`stop_reason`、`request_id`、回件全文，不含 key；位置與命名規則為本票交付，供 T03／T02 引用）→ 型別化失敗處理（429／5xx 有限重試、400／404／`refusal` 不重試、缺 key 明確報錯）；並完成一次真實呼叫驗證。

**相關約束（取自 parent，使用者 2026-10-16／17 裁決，T01 適用者）**：
- **D-c／D-d／D-e／D-f**：官方 API `max_uses` 不代替 incident／送審次數控制（本票不涉）；API key 與合理付費已接受；專用 Advisor tool（`advisor_20260301`）、CLI 工程顧問、直接 Opus Messages API 顧問為三種不同實作，**本票採直接 Opus Messages API**，不實作多 backend；**不更動主 session 認證**。
- **D-a（記帳，本票不落地）**：顧問記帳沿用 `consult --kind advisor`，由 plan-local T02 放寬條文；**T02 生效前，本票 receipt 不得以 `consult --kind advisor` 記帳，不得冒記為現役原生 advisor consult**。本票不以 API receipt 寫 ledger 記帳；Gate 入口 `check` 與依 Gate 契約之事件由主 agent 照 `ssot-governance.md` §Autonomous Engineering Gate 執行，不在此限（見 HC7）。
- **§1.1／§1.3 完成口徑**：T01 只交付「API 顧問可呼叫並留 receipt」（成果表 S2-R2 範圍）；呼叫器可回話**不等於**「問題 → 顧問策略 → executor 修驗 → 返回原工作」已完成；整體成果 E1（非 R0 顧問修驗續作）、E2（一次合法恢復送審）與 T03／T02 不在本票，**本票不代表 E1／E2 已驗**。
- **現值漂移補記（Rev B，parent 字面「型別化失敗處理（429／5xx 有限重試…）」）**：重試改由**呼叫端依明確原因安排至多一次**（暫態失敗後重試／格式相容拒絕後 fallback／TRUNCATED 後提高上限，三擇一，共用 AI-5 兩次網路嘗試總額度）；呼叫器本身 `max_retries=0`、不寫重試迴圈。語義不變處：400／404／`refusal` 不重試。
- **成果表 S2-R2 驗收（`plans/roadmap/autonomous-engineering-gate-outcomes.md`）**：真實呼叫須成功取得可用策略；錯誤處理以**決定性測試**驗證，**不刻意發送付費錯誤請求**；錯誤、空回件、截斷**一律不可記為成功策略**；plan、mock 或模型能回話不代表完整完成。
- **回件重用口徑（使用者 2026-10-17）**：本票真實回件日後重用於 E1／T02 ⑦ 時，**保留原時間與來源（`created_at_utc`、`request_id`、`payload.sha256`），不冒稱 T02 當下重新諮詢**；回件必須對應該代表性問題，不硬套不相關策略。故本票合成問題＝§1.3 隔離場景的種入缺陷（見 AI-2）。
- **屬 T03 的驗收意圖，T01 僅承接欄位**：`finish` 綁定本次執行結果（命令失敗／逾時／中斷不得靠舊成功回執判 SUCCESS，無法定位本次結果則 INDETERMINATE）、native 定位欄位可如實 UNAVAILABLE 且格式接受不證調用真實性——**T01 的承接面**＝receipt 的 `status`／`stop_reason`／`request_id`／`created_at_utc`／`payload.sha256` 欄位與固定位置命名，供 T03 做 ER 綁定；本票不實作 `claim`／`begin`／`finish`。
- **receipt 耐久副本接口提醒（交 T03，Rev B）**：receipt 落於 `tmp/<TICKET_ID>/advisor/`，受 `tools/cleanup_expired_tmp.py` 以 **7 日（`RETENTION_DAYS=7`，依 mtime）** 保留清理，過期即失。T03 的 `claim` MUST 消費**可讀取且 `sha256` 一致的耐久副本**（原檔位元組，非轉錄）；副本位置與引用方式由 **T03 開票時定義**；**不得**以票面貼上的 JSON 敘述代替原檔，**不得**因暫存過期而重發付費請求。本票**只保留此接口提醒**：不定義副本位置、不實作複製、不搬動或刪除 receipt 原檔（見 AI-6）。

**證據分級**（Rev A：2026-10-17 00:3x–00:5x live，HEAD `0d1a5959`；**Rev B 重驗：2026-10-17，HEAD `dc7a9e16`，工作樹 clean**；`0d1a5959..dc7a9e16` 僅 2 筆（`c03864fd` ticket-author 進度契約、`dc7a9e16` PROGRESS／冊面），`git diff --name-only` 對三個 ALLOWED 路徑為空；三 ALLOWED 檔仍不存在、未追蹤、未被忽略；`anthropic` 仍為 0.105.2。以下條目除標明 Rev B 者外為 Rev A 取證）：
- **CONFIRMED（本地原始碼／工具輸出）**：①`anthropic` 0.105.2 已安裝於系統 Python 3.14.5（`C:\Python314\Lib\site-packages`）；專案內**無**依賴宣告檔涵蓋 `.claude/scripts/`（僅 `tools/requirements-image-optimizer.txt`，屬他域）。②`Messages.create` 簽名含 `max_tokens`／`messages`／`model`／`output_config`／`system`／`thinking`／`timeout`／`metadata`；`OutputConfigParam` 含 `effort`（`low`…`max`）與 `format`（`JSONOutputFormatParam`：`type: "json_schema"`、`schema`）；`Message.stop_reason` Literal 含 `refusal`、`max_tokens`、`pause_turn`；`Message` 有 `stop_details`；回應物件有 `_request_id`（`_models.py`）；異常類 `AuthenticationError`／`BadRequestError`／`NotFoundError`／`RateLimitError`／`InternalServerError`／`APIConnectionError`／`APITimeoutError`／`PermissionDeniedError`／`UnprocessableEntityError` 存在；client 預設 `max_retries=2`、預設 timeout `read=600`。③SDK `Anthropic.__init__` 會讀 `ANTHROPIC_API_KEY`、`ANTHROPIC_AUTH_TOKEN`、`ANTHROPIC_PROFILE`、`ANTHROPIC_BASE_URL` 環境變數；本機 `ANTHROPIC_BASE_URL` 已設為 `https://api.anthropic.com`；Rev A 時 `ANTHROPIC_API_KEY`、`ADVISOR_ANTHROPIC_API_KEY` 皆未設 → 呼叫器必須顯式傳 `api_key`／`base_url` 並在進程內中和其他來源。**Rev B 存在性確認（2026-10-17，實際執行之 Python 子行程，僅輸出 SET／NOT_SET，未輸出值、前綴、長度、hash 或環境變數清單；未建 client、未聯網、未測有效性）**：`ADVISOR_ANTHROPIC_API_KEY`＝**已設定**；`ANTHROPIC_API_KEY`＝**不存在**。此為單一時點觀察，AI-0 #4 執行當下重驗，AI-5 前置以執行當下為準。④`tmp/` 受 `.gitignore:52` 忽略（receipt 落點非版控，不是 commit 標的）；三個 ALLOWED 新檔路徑皆未被忽略、尚不存在、無同名符號；`tools/cleanup_expired_tmp.py` 對 `tmp/` 與 `.tmp/` 以 `RETENTION_DAYS=7`（mtime）清理（Rev B 實讀），故 receipt **不是長期證據載體**；票內轉錄僅供閱讀，耐久副本接口交 T03（見相關約束末條）。⑧（Rev B，本地原始碼＋離線 mock 實測，**未發模型請求**）SDK 非串流守衛：`_base_client.py:731-744` `_calculate_nonstreaming_timeout` 於 `3600×max_tokens/128000 > 600`（即 `max_tokens > 21333`）時 `raise ValueError`；`messages.py:987` 僅在 `not stream` 且未給 `timeout` 且 `client.timeout == DEFAULT_TIMEOUT`（`_constants.py:9`，`Timeout(600, connect=5.0)`）時才呼叫之；`MODEL_NONSTREAMING_TOKENS` 不含 `claude-opus-5-5`。實測（`httpx.MockTransport`，dummy 非真實 key）：client 顯式 `timeout=300` 時 `max_tokens=24000` 不被攔下（mock 收到請求）；預設 timeout 時 21333 通過、24000 `ValueError`。→ **`--max-tokens 24000` 於 SDK 層可接受，前提為 client 顯式 `timeout`（本票 AI-1 #3 已顯式傳入）**；API 服務端對該值之接受屬 UNKNOWN（SDK 守衛為客戶端檢查）。⑨（Rev B）`claude auth status` 於本機可執行，輸出為 JSON，含 `authMethod`／`apiProvider`／`subscriptionType` 欄位（值不入票，AI-0 #8 於開工記錄基線）。⑤`gate_ledger.py:92` `TICKET_RE=^[A-Z][A-Z0-9]*(-[A-Z0-9]+)+$`（呼叫器 `--ticket` 格式沿用，不 import）。⑥`settings.json` allow 含 `Bash(python *)`；deny 不含相關模式。⑦無重複票：`ADV-APICALL` 全冊（`TICKET_REGISTRY.json`／`_ARCHIVE.json`／`Pending/`／`deliver/`／`void/`／`reports/`）0 命中；`ADV` 冊面 12 筆（含已 CLOSED 之 ADV-REVIEW-01／ADV-LAYER-01／ADV-OWNER-01／ADV-RESETISO-01／ADV-R0ROUTE-01），無等價結果；`ADV-` family 已登記。
- **CONFIRMED（官方文件，PPSS snippet 級，頁面 last_updated 2026-10-15／16）**：Claude Opus 5.5 單價**輸入 $4／輸出 $20 每百萬 token**，5 分鐘 cache write $5、1 小時 cache write $8、cache read $0.20；adaptive thinking **always on、不可關**，`effort` 預設 `medium`；`thinking: {"type":"disabled"}` 或 `budget_tokens` 回 400；`thinking` 欄位可省略；1M context、128K max output。來源：`https://platform.claude.com/docs/en/models/opus-5-5/overview`、`…/about-claude/pricing`、`…/models/opus-5-5/whats-new-opus-5-5`（研究 `rsch-20261016-1ca12b1c7b`）。
- **CONFIRMED（External-review 回流，Rev B.3，Akira 2026-10-17 裁決轉錄）**：①`output_config.format`（json_schema）與 adaptive thinking 相容，Opus 5.5 支援（取代 Rev A／B 之 UNKNOWN①）。②client `max_retries=0` 與顯式 `timeout` 之 SDK 行為（與本地證據 ②⑧ 一致）。③官方文件明列 citations 與 message prefilling 與結構化輸出不相容（本票兩者皆不使用；**不得**寫成「唯一不相容是 citations 與 prefill」）。**`prompt_json` fallback 保留**：其作用是承接帳號、模型、區域、SDK 或服務端差異，**不得**寫成「一定用不到」（AI-1 #4、AI-5 #2）。
- **UNKNOWN（External-review 未決，維持）**：receipt／共享落點 anti-pattern 問項。UNKNOWN **不構成**刪除、縮窄或降級原需求之理由，亦**不重送外審**。
- **UNKNOWN（有 AI-5 probe）**：①（Rev B.3 已轉 CONFIRMED，見上；AI-5 仍實測本帳號本日可用性）；②`refusal` 的 HTTP 狀態與 `stop_details` 實際形狀；③帳號能否呼叫 `claude-opus-5-5`；④真實錯誤回件形狀。皆由 AI-5 的**一次真實呼叫**與 AI-3 決定性測試（以 SDK 型別構造假回應）處理，**不刻意發送付費錯誤請求**。
- **HYPOTHESIS**：payload-only（顧問看不到 repo）足以對本票合成缺陷給出可用策略（AI-5 驗證；N=1，不宣稱一般可靠）。

**External-review decision**（條文：`D:\DevSources\Smarter-Project\file\Instructions_for_Claude.md` v3.4 §6〔L43–L50〕與「分流順序與發送通道」節，2026-10-17 live 直讀；分項記錄）：
- **6a（成立；外審 EXECUTED）**：`INTRODUCED_CAPABILITY=feature:advisor-api-call`≠`NONE`；corpus（`Handoff/`、`Pending/` `deliver/` `void/`、`TICKET_REGISTRY.json`、`_ARCHIVE.json`、vault `D:\DevSources\Smarter-Project`）該 token 字面 **0 命中**（本票自身不計）→ 成立。處置＝PPSS 取證（Claude 自行呼叫 `research_submit`）：`rsch-20261016-1ca12b1c7b`（2026-10-17 00:5x，3 條查詢，$0.005，artifact `D:\DevSources\workflow\mcp-ticket-platform\data\research\2026-10\rsch-20261016-1ca12b1c7b.json` 為 runtime 衍生物，**結論以本欄為準**）。回流：單價、thinking 規格 CONFIRMED（見上）；結構化輸出相容性與 refusal／request-id 官方條文**未回傳** → UNKNOWN，不重送（防遞歸律）。**Rev B.3 External-review 回流**：結構化輸出＋adaptive thinking 相容、`max_retries=0`＋顯式 timeout 轉 CONFIRMED；receipt／共享落點 anti-pattern 維持 UNKNOWN（見證據分級）。硬風險判準①–⑥：⑤（外部成本控制）部分涉及——本票新增付費呼叫面，由 `max_tokens` 封頂、單次呼叫、無工具、payload 大小上限與 `usage`／估算費用留痕控制，**不建成本封頂平台**；其餘未命中。**EXECUTED 不代表品質 PASS，不豁免 Cross-Review。**
- **6b③（成立）**：`DEPENDENCY_DELTA=TOOL:anthropic@0.105.2`≠`NONE`。處置＝上列同一研究（單價、API 契約）＋ bounded probe＝**AI-5 真實呼叫**，**狀態：EXECUTED**（AI-5 第 1 次真實呼叫 `SUCCESS`，2026-10-17；證據見「收尾實錄」）；AI-6 收尾 MUST 改記實際結果（`EXECUTED`＋證據，或 `NOT_EXECUTED`／`UNVERIFIED`＋原因），不得留 PLANNED。依賴處置：不新增依賴宣告檔、不 pin 於 repo（SDK 為使用者環境既有安裝）；呼叫器於 receipt 記錄實際 SDK 版本，缺 SDK 時明確報錯（exit 2）。
- **其他觸發項**：6b①—`ALLOWED` 三檔首層 path segment 皆為 `.claude`，distinct＝1 <2，不成立（若開工後增跨首層路徑 MUST 重判並出查詢包）；6b②—`### AI-[1-9][0-9]*:` 標題數＝6（AI-1…AI-6；AI-0 不計）<8，不成立；6c—三檔皆新增、無 `Revision` 歷史，不成立。計數讀取（O1，Rev B.3 時）：`Rev（4a 計數）=1（Rev B＋B.1＋B.2 觸發 4a，續修裁決落檔後歸零，B.3 計 1）｜ AI 標題數=6（Rev B～B.3 未新增 AI 項）｜ AI 深度=1 ｜ 4a/4d/6b 狀態=6b③成立（已取證）`；Rev B 重判：6a／6b①②／6c 結論不變（`INTRODUCED_CAPABILITY` 與 `DEPENDENCY_DELTA` 未變、ALLOWED 未變、三檔仍全新增），**不重送研究**（防遞歸律）；結構化輸出相容性 Rev B.3 已轉 CONFIRMED（External-review 回流），fallback 保留以承接帳號／模型／區域／SDK／服務端差異，AI-5 probe 照常。
- **Cross-Review**：**不豁免**。
- **Rev B.4 Cross-Review 結果與 override（Akira 2026-10-17 經 SKAD，INC-001）**：實際 verdict＝Codex **REPORT_FIX×2**（R0＝FORWARD）；裁決 **D**——`ADVISOR_ANTHROPIC_API_KEY` 被判 `missing-wiring` 屬 SV 階段 B #7 規則缺口，非本票程式或 AC 缺陷；AC10 維持 PASS，不標 `(C)`／PARTIAL／不取消勾選；不授權 credential-location probe；不重跑 Codex（`REPORT_FIX_COUNT=2` 維持停止）。**外部輸入無 repo producer 是 HC3／AI-5 前置／parent §7 的預期安全設計，不是 missing-wiring**；證據固定 a（AI-0 不含值存在性）／b（`advisor_call.py:464`）／c（真實 SUCCESS receipt `request_id=req_011Cg7jy3wgQgG8nDTQnXDeF`）。registry `reviews.codex` 依 Transition Table 映射值 `PASS`，**實際 verdict 與 override 記於 checkpoint 與 SV report**。`os.environ.pop` ADVISORY 不改（符合 AI-1 #3 process-only 隔離與測試 04）。4a 計數：歸零後 1→**2**，未成立。

**簽發日期**: 2026-10-17
**簽發者**: Claude
**來源議題**: Advisor
**Mode**: uc  ||  **Effort**: High
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無（plan-local T03、T02 依賴本票；其正式票號未配，不構成本票停止條件）
- `evidence_dependency`: ADV-LAYER-01（逐層預算與 Gate 現役語義）、ADV-R0ROUTE-01（R0 承接與限定交付邊界）、ADV-GATE-02（`gate_ledger.py` 介面——僅唯讀參照 `TICKET_RE`）——皆 `CLOSED`，僅作字面判準與介面來源，本票錨點皆 live 直查
- `coordination_dependency`: 與 Gate 管線共用 `tmp/verify_cmd.conf`／`<git-common-dir>/gate/` 寫入面（入口 `check` 寫 ledger〔`gate_ledger.py:838-846`〕、DIFF_REF 回填再生 conf〔`qa/SKILL.md:41-45`〕），前後比對依 AI-4 #4 歸因規則；本票與任何新增 `.claude/scripts/` 同名檔之票須串行（`concurrency.md` §2）；PROGRESS／Registry 單一寫入者

## Verify Config
TICKET_ID=ADV-APICALL-01
CREATED=2026-10-17
PRESET=feature
ALLOWED=.claude/scripts/advisor_call.py,.claude/scripts/tests/test_advisor_call.py,.claude/scripts/tests/fixtures/advisor_seed_payload.md
FORBIDDEN=.claude/scripts/gate_ledger.py,.claude/scripts/activate_ticket.sh,.claude/scripts/m3_family_audit.sh,.claude/scripts/tests/test_gate_ledger.py,.claude/scripts/tests/owner_writer_test.sh,.claude/hooks/gate_stop_guard.py,.claude/hooks/registry_review_stop_guard.py,.claude/hooks/session_start.sh,.claude/hooks/stop.sh,.claude/hooks/telegram_stop.sh,.claude/hooks/tests/gate_stop_guard_test.py,.claude/rules/ssot-governance.md,.claude/rules/cross-review.md,.claude/rules/concurrency.md,.claude/rules/ticket_pattern.md,.claude/agents/cross-reviewer.md,.claude/skills/sv/scripts/cross_review.sh,.claude/skills/sv/SKILL.md,.claude/skills/sv/CROSS_REVIEW.md,.claude/skills/sv/ssot_context.md,.claude/skills/qa/SKILL.md,.claude/skills/uc/SKILL.md,.claude/settings.json,.claude/settings.local.json,.claude/CLAUDE.md,CLAUDE.md,CLAUDE.local.md,.claude/PROGRESS.md,.claude/PROGRESS_ARCHIVE.md,.codex/**,.agents/**,tools/**,app/**,plans/roadmap/autonomous-engineering-gate-outcomes.md,plans/cso_briefing/project/TICKET_REGISTRY.json,plans/cso_briefing/project/TICKET_REGISTRY_ARCHIVE.json
MAX_LINES=N/A
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-10-17 17:50
BASE_COMMIT=dc7a9e16
TARGET_BLOB=.claude/scripts/advisor_call.py:NEW;.claude/scripts/tests/test_advisor_call.py:NEW;.claude/scripts/tests/fixtures/advisor_seed_payload.md:NEW
INTRODUCED_CAPABILITY=feature:advisor-api-call
DEPENDENCY_DELTA=TOOL:anthropic@0.105.2

> PRESET=feature（新增 `.py` 行為＋unittest＋一份文字 fixture；無 `.kt`／`.xml`／gradle，`assembleDebug` N/A）。`MAX_LINES=N/A`：含密鑰處理、fail-closed、成本邊界類不變量，失敗形狀為審查發現式，不可先驗估算（TPL v3.5 R3）；MAX_LINES 非 execution gate。`TARGET_BLOB`：三檔皆新增（`NEW`，V1 跳過比對）。`DIFF_REF` 為字面佔位，開工時依 `qa/SKILL.md` Step 0 三原子動作回填。`Traycer Workflow: N/A`：純 `.claude/` 工具，不觸 `app/`；審查走 SV＋Cross-Review。`FORBIDDEN` 使用字面路徑（避開 `verify_checklist.sh` 對含 `/` token 的 glob 字面比對缺陷）；`gate_ledger.py`、其測試與兩支 Stop hook 僅表示**不得修改**，必要時仍可**執行**作回歸。**EOL**：新檔 MUST 為 LF，行尾不得出現 CRLF。**DEFERRED-TO-DOCSYNC（不開票）**：FORBIDDEN 命中 `.claude/PROGRESS.md`／`PROGRESS_ARCHIVE.md`（對照 `rules/cross-review.md`「管線標準觸碰面登記表」）→ `/qa` Step 4 SKIP；Dashboard 同步、Done 標記、票檔 Pending→deliver 搬移與 checkbox 勾選屬標準 SOP 收尾，於下一輪 `/compact` 正常處理，**不另開票**。`plans/roadmap/autonomous-engineering-gate-outcomes.md` 列 FORBIDDEN：成果表由 `projman align` 回填，非本票寫入範圍。暫存物（payload 副本、receipt、temp repo）MUST 置於 `tmp/ADV-APICALL-01/` 或系統暫存目錄，符合 `concurrency.md` §1。
>
> **現值漂移紀錄（local-live vs parent，不重跑 plan）**：①HEAD 自 `5b1a1d21` 前進至 `0d1a5959`（3 筆 `plans/roadmap/` 文件提交），與 ALLOWED 無交集。②parent §2 稱「Python SDK 單次呼叫」、§3 稱「`anthropic 0.105.2`」——live 一致。③parent 預設以 `output_config.format` 固定回件欄位——**結構化輸出與 adaptive thinking 併用之 API 相容性**：Rev A／B 時 UNKNOWN，Rev B.3 經 External-review 回流轉 CONFIRMED（見證據分級），沿用 parent 設計為**主路徑**，風險交 AI-5 probe；**Rev B 修訂（取代 Rev A「400 指向 format／schema 即停止、不降級」）**：僅當 API **明確拒絕** structured-output 格式或其與 thinking 之相容組合時，授權**同票** `prompt_json` fallback（移除 `format`、system prompt 內嵌 schema，驗證與成功判準不變）；訊息指向 `ADVICE_SCHEMA` 自身錯誤者先修實作，不得 fallback。fallback 由**呼叫端以 `--request-mode prompt_json` 明確觸發**，腳本不自動切換。④parent 稱 receipt 落點「ticket 階段定」——本票定為 `tmp/<TICKET_ID>/advisor/`（見 AI-1），`<TICKET_ID>` 為**諮詢發生時的現役票**（AI-5 即 `ADV-APICALL-01`）。⑤本機 `ANTHROPIC_BASE_URL` 與 `ANTHROPIC_AUTH_TOKEN`／`ANTHROPIC_PROFILE` 環境變數會被 SDK 讀取（parent 僅提 `ANTHROPIC_API_KEY`），故 AI-1 加進程內中和（HC4）。⑥（Rev B）parent「429／5xx 有限重試」→ client `max_retries=0`、腳本不自動重試，由呼叫端依明確原因安排至多一次（見相關約束「現值漂移補記」）。⑦（Rev B）HEAD 自 `0d1a5959` 前進至 `dc7a9e16`（2 筆，皆與 ALLOWED 無交集），`BASE_COMMIT` 同步更新為 `dc7a9e16`（資訊性診斷值）。其餘 parent 事實 live 一致，無實質衝突。

## Action Items

> **AI-0 為強制首項**。以下為錨點與基線復驗；任一項不符，先依 TPL「基線漂移三分流」分類（`ANCHOR_STALE_SCOPE_INTACT`／`CANDIDATE_SUPERSEDED` 為資訊性，記錄後以現值續作；`ALREADY_EXECUTED_PENDING_CLOSURE` 維持停止），再依 Error Handling。

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: 三個 ALLOWED 新檔路徑及唯讀參照
- **動作**: 逐字確認各「現值」並記錄實際輸出：
  1. 三個 ALLOWED 路徑尚不存在（`git ls-files` 與檔案系統皆 0）；`git check-ignore -v` 對三路徑零命中；`tmp/ADV-APICALL-01/advisor/x.json` 命中 `.gitignore:52`（`tmp/`）。
  2. `python -c "import anthropic; print(anthropic.__version__)"` 成功並記錄版本（開票時 0.105.2）；用 `inspect` 確認：`Messages.create` 參數含 `max_tokens`、`messages`、`model`、`output_config`、`system`、`timeout`；`OutputConfigParam` 鍵含 `effort`、`format`；`JSONOutputFormatParam` 鍵含 `type`、`schema`；`Message.model_fields['stop_reason']` 含 `refusal`、`max_tokens`、`pause_turn`；`anthropic` 命名空間含 `AuthenticationError`、`BadRequestError`、`NotFoundError`、`PermissionDeniedError`、`UnprocessableEntityError`、`RateLimitError`、`InternalServerError`、`APIConnectionError`、`APITimeoutError`、`APIStatusError`。
  3. `gate_ledger.py:92` `TICKET_RE` 原式（唯一，唯讀）；`.claude/settings.json` allow 含 `Bash(python *)`。
  4. 環境（**只查存在性，禁輸出值、前綴、長度、hash 與環境變數清單**；以實際執行之 Python 子行程判定，輸出僅 `SET`／`NOT_SET`）：`ADVISOR_ANTHROPIC_API_KEY` 是否已設（決定 AI-5 可否執行；開票 Rev B 時為已設定）、`ANTHROPIC_API_KEY`／`ANTHROPIC_AUTH_TOKEN`／`ANTHROPIC_PROFILE`／`ANTHROPIC_BASE_URL` 是否已設（記錄，供 AI-3 中和斷言與 AI-4 隔離比對）。
  5. 基線：記錄 `tmp/verify_cmd.conf`、`tmp/verify/active.json`、`.tmp/cross_review_state/`、`<git-common-dir>/gate/` 目錄清單與各檔 sha256（供 AI-4／AI-5 前後比對）；記錄 `git worktree list`。
  6. 重新 grep 確認 `advisor_call`、`feature:advisor-api-call` 在 repo 內仍無既有命中（本票自身除外）。
  7. **非串流上限確認（不發任何模型請求）**：讀本地 SDK `_base_client.py` `_calculate_nonstreaming_timeout` 與 `resources/messages/messages.py` `create` 內「`not stream` 且未給 `timeout` 且 `client.timeout == DEFAULT_TIMEOUT`」之守衛條件，並以 `httpx.MockTransport`＋dummy key 離線實測：client 顯式 `timeout` 時 `max_tokens=24000` 不被 SDK 攔下。結果符合（開票 Rev B 已預驗）→ `MAX_TOKENS_CEILING=24000` 沿用；SDK 行為改變致不支持 → 在授權成本範圍內（單次 `max_tokens` 封頂、不改 payload、不換模型、不新增 backend）改選可用上限值並記錄，不得改用串流。
  8. **主 session 認證基線**：確認 `claude auth status` 可執行（唯讀），僅記錄 `authMethod`／`apiProvider`／`subscriptionType` 三欄之值（其餘欄位含身分資訊，**不記錄、不輸出**）。不可用（非零 exit、無法解析或欄位缺）→ 記錄原因，AI-4 #5 與 AI-5 #5 改採限定落點比對（見 AI-4 #5），不掃整個 `~/.claude`。
- **#1–#6 任一項不符 → 停止回報，禁自行推定等價位置；#7／#8 依各自所載處置（非停止條件）。**
- [x] 完成（依據見「收尾實錄」）

### AI-1: 新增 `.claude/scripts/advisor_call.py`（單一檔、stdlib＋`anthropic`）
- **檔案**: `.claude/scripts/advisor_call.py`（新增；LF）
- **現值**: 不存在
- **目標值**（介面契約；實作細節由執行端依 SDK 契約完成，**不得超出下列範圍**）：
  1. **進入點**：`main(argv=None, *, client_factory=None, env=None, now=None) -> int`；`if __name__ == "__main__": sys.exit(main())`。`client_factory`／`env`／`now` 僅為測試注入口，預設走真實 SDK、`os.environ`、`datetime.now(timezone.utc)`。`anthropic` 為**惰性匯入**（缺 SDK 時 exit 2 並明確訊息，不 traceback）。啟動時 `sys.stdout`／`sys.stderr` 重新設定為 UTF-8（Windows cp1252 會使中文輸出崩潰）。
  2. **CLI**：`advisor_call.py --ticket <TICKET_ID> --payload <path> [--out-dir <dir>] [--model claude-opus-5-5] [--effort low|medium|high|xhigh|max] [--max-tokens 12000] [--timeout 300] [--request-mode json_schema|prompt_json] [--max-payload-bytes 200000] [--dry-run]`。`--ticket` 必填且符合 `^[A-Z][A-Z0-9]*(-[A-Z0-9]+)+$`（語義完全同 `gate_ledger.py:92`，不另加數字要求，**不 import** 該檔）；預設 `--effort medium`（Opus 5.5 預設值，顯式帶出）、`--max-tokens 12000`（非串流，adaptive thinking 的 token 計入 `max_tokens`；允許範圍正整數且 ≤ 常數 `MAX_TOKENS_CEILING=24000`，超出 → exit 2；該上限供 TRUNCATED 後提高上限之單次重跑，依據見 AI-0 #7）、`--timeout 300`（正數且 ≤ 600，超出 → exit 2；提高 `--max-tokens` 重跑時可同步提高 `--timeout`，上限 600）、`--request-mode json_schema`（主路徑；`prompt_json` 為 fallback，**只由呼叫端明確指定**，腳本不自動切換）；`--out-dir` 預設 `tmp/<TICKET_ID>/advisor/`。**不提供** `--api-key` 參數、不讀檔案型密鑰。
  3. **憑證與端點隔離**：只讀環境變數 `ADVISOR_ANTHROPIC_API_KEY`（缺或空 → exit 2，訊息不含任何值）；**絕不**讀 `ANTHROPIC_API_KEY`。建 client 前於進程內 `os.environ.pop` 掉 `ANTHROPIC_AUTH_TOKEN`、`ANTHROPIC_PROFILE`（僅限本進程，不寫回、不影響父 shell）；以 `Anthropic(api_key=<專用變數值>, base_url="https://api.anthropic.com", timeout=<--timeout>, max_retries=0)` 顯式建構（`base_url` 為常數，忽略 `ANTHROPIC_BASE_URL`；`max_retries=0`：SDK 不自動重試，每次呼叫器執行至多一次網路嘗試；顯式 `timeout` 同時使 SDK 非串流守衛不攔 `max_tokens` ≤ 24000）。密鑰值**不得**出現在 stdout、stderr、receipt、例外訊息、argv；錯誤訊息寫入前以固定規則遮蔽 `sk-ant-[A-Za-z0-9_-]+` 與專用變數值本身。
  4. **請求形狀**（單次、無工具；兩種 `request_mode`，一次執行只走其一）：
     - **`json_schema`（主路徑，預設）**：`client.messages.create(model=<--model>, max_tokens=<--max-tokens>, system=<固定顧問角色字串常數>, messages=[{"role":"user","content":<payload 全文>}], output_config={"effort": <--effort>, "format": {"type":"json_schema","schema": ADVICE_SCHEMA}})`。
     - **`prompt_json`（fallback，授權條件見下）**：同上但 `output_config={"effort": <--effort>}`（**不含 `format`**），且 `system` ＝ 固定顧問角色常數 ＋ 固定輸出指示（只輸出**單一** JSON 物件、符合下列 schema、不得有 code fence 或前後說明文字）＋ `ADVICE_SCHEMA_JSON`（＝`json.dumps(ADVICE_SCHEMA, ensure_ascii=False, sort_keys=True)`，模組常數，測試以同一常數斷言）。回件驗證與 `json_schema` 模式**完全相同**（`json.loads` 嚴格解析、`ADVICE_SCHEMA` 欄位／型別檢查、`strategy` 非空白），**不放寬**：含 code fence 或前後文字即 `UNPARSEABLE`；不降低成功判準。
     - **fallback 授權條件**（呼叫端判斷，腳本不自動切換、不自動重試）：僅當 API 回 400 且訊息**明確**指出 structured-output／`output_config.format` 不被接受，或其與 thinking（adaptive）之組合不被接受時，得以 `--request-mode prompt_json` 重跑一次（占用 AI-5 總額度之第 2 次，見 AI-5 #2）。訊息指向 `ADVICE_SCHEMA` 自身錯誤（不支援之 schema 關鍵字、schema 結構錯誤等）者，**先修實作並以離線測試覆蓋，不得 fallback**。
     兩種模式皆**不得**傳 `tools`／`tool_choice`／`thinking`（Opus 5.5 省略＝adaptive，`disabled`／`budget_tokens` 皆 400）／`temperature`／`top_p`／`top_k`／assistant prefill／`stream`／`metadata`。固定顧問角色常數 MUST 載明：只依 payload 內容判斷、看不到 repo 與其他檔案；只裁決本地可推導之設計／修法，不裁決產品語義、SSOT、scope、權限、不可逆動作（此類一律 `authority_gap=true`）；不得假設已授權；證據不足時列 `evidence_needed`；以繁體中文為主、英文僅限技術術語與識別字。
  5. **`ADVICE_SCHEMA`**（`additionalProperties:false`，全部 required）：`strategy`（string，非空）、`steps`（string 陣列，具體修法：檔案＋改法）、`minimal_verification`（string，最小局部驗證法）、`authority_gap`（boolean）、`authority_gap_reason`（string，`authority_gap=false` 時為空字串）、`evidence_needed`（string 陣列，可空）。
  6. **payload 前置檢查**（任一不符 → exit 2，**不呼叫 API**、不寫 receipt）：檔案存在且可讀、UTF-8 可解碼；位元組數 ≤ `--max-payload-bytes`；含 6 個固定標題（整行精確比對，去除行尾空白） `## 目標`、`## 失敗證據`、`## 相關內容`、`## 已試策略與結果`、`## 既有授權`、`## 要求輸出`（缺者逐一列出）；不含字串 `sk-ant-` 或字面 `ADVISOR_ANTHROPIC_API_KEY=`（避免把密鑰送出）。
  7. **`--dry-run`**：完成上述前置檢查後，輸出 JSON（模型、effort、max_tokens、`request_mode`、payload 路徑／sha256／位元組數、將寫入的目錄；**不含**密鑰、不含 payload 全文）並 exit 0；**不建 client、不聯網、不寫檔**。dry-run 不需要金鑰。
  8. **結果分類與 exit code**（receipt 於收到回應或 SDK 例外後一律寫入；前置檢查失敗不寫）：

     | exit | `status` | 條件 |
     | --- | --- | --- |
     | 0 | `SUCCESS` | `stop_reason=="end_turn"`、取 `type=="text"` 區塊串接為非空字串、`json.loads` 成功、通過 `ADVICE_SCHEMA` 欄位／型別檢查、`strategy` 非空白 |
     | 2 | （無 receipt）| 用法／設定／前置檢查失敗（缺金鑰、缺 SDK、payload 不符、參數非法） |
     | 3 | `API_ERROR` | 不可重試：`BadRequestError`(400)、`AuthenticationError`(401)、`PermissionDeniedError`(403)、`NotFoundError`(404)、`UnprocessableEntityError`(422)，及其他非 408／409／429／5xx 的 `APIStatusError`（400 中「格式相容拒絕」是否授權 fallback 由呼叫端依 receipt `error.message` 判斷，**不另設 exit 碼**） |
     | 4 | `API_ERROR` | 暫態類（**未自動重試**，`max_retries=0`，單次嘗試即回報）：`RateLimitError`(429)、`InternalServerError`／5xx、408／409、`APIConnectionError`、`APITimeoutError` |
     | 5 | `REFUSAL` | `stop_reason=="refusal"`（記 `stop_details`） |
     | 6 | `TRUNCATED`／`EMPTY`／`UNPARSEABLE`／`SCHEMA_INVALID`／`UNEXPECTED_STOP` | `stop_reason=="max_tokens"`；無 text 或空白；非 JSON；JSON 但欄位／型別不符；`pause_turn`／`tool_use`／`stop_sequence`／`None` 等非預期停止 |
     | 1 | `INTERNAL_ERROR` | （Rev B.3）**已取得 API response 後**，本地解析、正規化、成本計算、receipt 組裝或落位發生未預期 `Exception`；`advice=null`；盡力寫 receipt |

     **只有 exit 0／`SUCCESS` 可被視為「可用策略」；錯誤、空回件、截斷、拒絕、解析失敗、`INTERNAL_ERROR` 一律不可記為成功。** **`INTERNAL_ERROR` 契約（Rev B.3）**：已取得 response 後之未預期 `Exception` → exit 1、`status="INTERNAL_ERROR"`、`advice=null`，盡力寫 receipt，保留已取得之 `request_id`、`usage` 與遮蔽後 `error.class`／`error.message`（`http_status=null`）；receipt 本身寫入失敗時，stderr **只**輸出已遮蔽之 `request_id`、`usage` 與錯誤摘要；**只捕捉 `Exception`**，不捕捉 `KeyboardInterrupt`、`SystemExit` 或其他 `BaseException`（照常傳播）；已取得 response 後之 `INTERNAL_ERROR` **不觸發任何額外 API 重送**。取得 response 前（SDK 例外以外）之未預期例外 → exit 1，不寫 receipt、不吞錯、不寫成功。**重試語義（Rev B）**：呼叫器 `max_retries=0`，**不自動重試、不寫重試迴圈**，receipt 記 `sdk_max_retries=0`；重試由**呼叫端依明確原因安排至多一次**（AI-5 #2 三擇一）。`APITimeoutError`／連線中斷之請求可能已被服務端處理，無 `usage` 時 `estimated_cost_usd=null` **不代表未計費**（成本不視為零）。
  9. **receipt 位置與命名（固定，供 T03／T02 引用；改動須同步下游）**：`<out-dir>/advisor_<UTC yyyymmddTHHMMSSZ>_<request_id 或 norequest>.json`；`request_id` 取回應物件 `_request_id` 或例外之 request id，檔名內非 `[A-Za-z0-9_-]` 字元一律改 `_`；以同目錄暫存檔寫入後以 exclusive 方式落位（`os.link` 或 `O_CREAT|O_EXCL`；**不得**用會靜默覆蓋既有目標之 `os.replace`），**目標已存在則拒絕覆寫**（刪除本次暫存檔，依 `INTERNAL_ERROR` 之 receipt 寫入失敗處置：exit 1、stderr 只輸出已遮蔽之 `request_id`、`usage` 與錯誤摘要）；目錄不存在則建立；UTF-8、`ensure_ascii=False`、`indent=2`、`sort_keys=True`、LF 行尾。stdout 輸出**單行 JSON**：`{"status", "exit_code", "receipt", "receipt_sha256", "request_id", "estimated_cost_usd"}`（`receipt_sha256` 為已寫入檔案位元組之 SHA-256 全十六進位）；成功時另在 stdout 後續行輸出 `advice` 摘要（`strategy`／`steps`／`minimal_verification`／`authority_gap`）。
  10. **receipt 欄位（`schema_version: 1`、`kind: "advisor_api_receipt"`）**：`status`（`SUCCESS`｜`API_ERROR`｜`REFUSAL`｜`TRUNCATED`｜`EMPTY`｜`UNPARSEABLE`｜`SCHEMA_INVALID`｜`UNEXPECTED_STOP`｜`INTERNAL_ERROR`，末值 Rev B.3 增）、`exit_code`、`ticket`、`created_at_utc`（ISO-8601 `Z`）、`model_requested`、`model_returned`、`request_id`、`stop_reason`、`stop_details`、`effort`、`max_tokens`、`timeout_s`、`request_mode`（`"json_schema"`｜`"prompt_json"`，Rev B 新增）、`sdk`（`{"name":"anthropic","version":<實際版本>}`）、`sdk_max_retries`（恆 `0`）、`payload`（`{"path","sha256","bytes"}`，**不含 payload 全文**）、`usage`（`input_tokens`／`output_tokens`／`cache_read_input_tokens`／`cache_creation_input_tokens`，無回應時 `null`）、`estimated_cost_usd`、`pricing_basis`、`advice`（SUCCESS 時為解析後物件，否則 `null`）、`raw_text`（text 區塊串接；無則 `null`）、`error`（`{"class","http_status","message"}`，已遮蔽；無則 `null`）。**不含**金鑰、環境變數值、payload 全文、thinking 內容。**`raw_text` 最小安全邊界（Rev B.3）**：`raw_text` 僅可保存模型回覆文字；不得保存 API key、`Authorization`／header、環境變數、完整 request payload 或未遮蔽 traceback。回覆含 payload 內可能敏感的逐字內容時寫 `raw_text=null`，判定採固定規則（不做語意判斷）：回覆文字命中密鑰遮蔽規則（`sk-ant-[A-Za-z0-9_-]+`、專用變數值）或含 `Authorization`、`x-api-key`、`ADVISOR_ANTHROPIC_API_KEY=` 字樣；此時仍保留 `request_id`、`usage`、`status`、`payload.sha256` 與遮蔽後 `error`。receipt 是**本地執行紀錄**，不是遠端帳務或 Anthropic 執行成功的獨立證明。
  11. **成本估算**：`estimated_cost_usd = (input_tokens×4 + output_tokens×20 + cache_read_input_tokens×0.20 + cache_creation_input_tokens×5)/1_000_000`（僅 `claude-opus-5-5`；常數與 `pricing_basis` 字串〔`claude-opus-5-5 $4/$20 per MTok, cache read $0.20, 5m cache write $5; platform.claude.com/docs/en/about-claude/pricing last_updated 2026-10-16`〕同檔宣告）；`cache_read_input_tokens`／`cache_creation_input_tokens` 為 `None` 時（SDK 0.105.2 `Usage` 型別為 `int | None`）計算以 0 計、receipt `usage` 原樣記 `null`，不得因此例外；其他 `--model` 或無 `usage` 時 `estimated_cost_usd=null`、`pricing_basis="unknown for model"`。此為**估算**，不代表帳單；credits 抵扣與額度用完後扣款屬帳戶層，repo 內不可驗證（UNKNOWN）。
  12. 檔頭 docstring：用途、用法、exit code 表、`request_mode` 兩模式與 fallback 授權條件、`max_retries=0`（不自動重試；真實驗證網路嘗試總額度）、receipt 位置與欄位、「顧問回件成功≠問題解決，最終以 executor 局部驗證為準」、「T02 契約放寬生效前不得以 receipt 記 `consult --kind advisor`」、金鑰只讀 `ADVISOR_ANTHROPIC_API_KEY`。
- [x] 完成（依據見「收尾實錄」）

### AI-2: 新增 `.claude/scripts/tests/fixtures/advisor_seed_payload.md`（代表性種入缺陷 payload）
- **檔案**: `.claude/scripts/tests/fixtures/advisor_seed_payload.md`（新增；LF）
- **現值**: 不存在
- **目標值**：一份**完整、可直接送出**的 payload，同時是 §1.3 隔離場景（E1）的種入缺陷來源，故為**確定性、非 R0 的小型工程缺陷**（唯一已知修法）：含 AI-1 第 6 項之 6 個固定標題；`## 相關內容` 內以 fenced code block 逐檔給出，**每個 code block 第一行為 `# file: <檔名>`**，至少兩檔：
  - `backoff.py`：`def next_backoff(attempt: int, base: float = 1.0, cap: float = 30.0) -> float:` 僅 `return base * (2 ** attempt)`（**缺少 `cap` 上限**）。
  - `check_backoff.py`：`assert next_backoff(0) == 1.0`、`assert next_backoff(3) == 8.0`、`assert next_backoff(10, base=1.0, cap=30.0) == 30.0, f"cap not applied: got {next_backoff(10, base=1.0, cap=30.0)}"`、`print("OK")`。
  - `## 失敗證據`：執行 `python check_backoff.py` 的實際輸出（`AssertionError: cap not applied: got 1024.0` 與行號）；`## 已試策略與結果`：寫「無」；`## 既有授權`：僅可修改 `backoff.py`、不得改 `check_backoff.py`、不得新增依賴；`## 要求輸出`：依 `ADVICE_SCHEMA` 欄位回答，並說明是否屬權威缺口。
  - **答案鍵不得出現在 payload**（已知修法＝在回傳前套用 `min(cap, …)`），只存在於 AI-3 測試常數。
- [x] 完成（依據見「收尾實錄」）

### AI-3: 新增 `.claude/scripts/tests/test_advisor_call.py`（決定性、零網路）
- **檔案**: `.claude/scripts/tests/test_advisor_call.py`（新增；LF；stdlib `unittest`，風格同 `test_gate_ledger.py`；`python -I` 可執行）
- **現值**: 不存在
- **目標值**：全部案例以注入的假 client、`tempfile` 目錄與注入 `env`／`now` 驅動 `main(...)`；**不得**聯網、不得讀真實環境金鑰、不得寫 repo 內任何路徑（receipt 寫臨時目錄）。假回應用 `anthropic.types.Message`（或最小同形物件）構造；SDK 例外用 `httpx.Request`／`httpx.Response` 構造真實 `anthropic.*Error`。**SDK 版本前提（Rev B.3）**：`MockTransport` 相關案例（3c、3d）以 `anthropic==0.105.2` 為前提；SDK 升版時重跑既有測試並檢查 transport／timeout 相容性，**現在不做相容層或升版矩陣**。案例至少：
  1. **前置／設定（exit 2、無 receipt、client_factory 未被呼叫）**：缺 `ADVISOR_ANTHROPIC_API_KEY`；只設 `ANTHROPIC_API_KEY`（仍 exit 2，證明不 fallback）；`--ticket` 格式非法；payload 不存在／過大／缺任一固定標題（訊息逐一列缺）／含 `sk-ant-`；參數非法（`--effort` 不在集合、`--max-tokens` 非正整數或 > 24000、`--timeout` 非正數或 > 600、`--request-mode` 不在集合）。
  2. **`--dry-run`**：exit 0、輸出為單一 JSON、含 payload sha256 與位元組數、**不含**密鑰與 payload 全文、不建 client、不寫檔、無金鑰也可執行。
  3. **請求形狀斷言**（以假 client 捕獲 `create` 的 kwargs；**兩種 `request_mode` 各一案**）：共通——`model`、`max_tokens`、`system` 為非空字串、`messages` 恰一則 user 且內容＝payload 全文；**不含** `tools`／`tool_choice`／`thinking`／`temperature`／`top_p`／`top_k`／`stream`／`metadata`，且 messages 末則非 assistant。**(3a) `json_schema`（預設）**：`output_config` 含 `effort` 與 `format.type=="json_schema"`、schema `additionalProperties` 為 `False` 且 required 為六欄；receipt `request_mode=="json_schema"`。**(3b) `prompt_json`（`--request-mode prompt_json`）**：`output_config` **不含** `format`（僅 `effort`）、`system` 含 `ADVICE_SCHEMA_JSON` 字串與「只輸出單一 JSON」指示；receipt `request_mode=="prompt_json"`；回件含 code fence 或前後文字 → `UNPARSEABLE`（成功判準未放寬）；合法裸 JSON → `SUCCESS`。**(3c) 非串流守衛（零網路）**：以真實 `anthropic.Anthropic`（`httpx.MockTransport`、dummy key）為 `client_factory`、`--max-tokens 24000` 驅動 `main(...)` → mock 恰收到 1 次請求，未被 SDK 非串流守衛（`ValueError`）攔下（mock 回 400 並帶 `request-id` header 即可，斷言請求已送出、非 `ValueError`，且 receipt `request_id` 等於該 header 值〔SDK `APIStatusError.request_id` 取自 `request-id` header〕）。**(3d) 真實 SDK 成功全鏈（零網路，`[可達性]` AC 之 trace）**：以真實 `anthropic.Anthropic`（`httpx.MockTransport`、dummy key）為 `client_factory`，mock 回 200 合法 `end_turn` Message JSON（text 區塊為通過 `ADVICE_SCHEMA` 之 JSON、含 `usage`）並帶 `request-id` header，以 `advisor_seed_payload.md` 驅動 `main(...)` → mock 恰收到 1 次請求且 body 含 payload 全文；exit 0、receipt `status=="SUCCESS"`、檔名與 `request_id` 皆為該 header 值（經 SDK `_request_id` 取得）、`usage` 為 SDK 解析值；stdout 首行 `receipt_sha256` 等於實際檔案 SHA-256。
  4. **憑證隔離**：以假 `client_factory` 捕獲建構參數——`api_key` 為專用變數值、`base_url=="https://api.anthropic.com"`（即使 `env` 內 `ANTHROPIC_BASE_URL` 指向他處）、`max_retries==0`、`timeout` 為 `--timeout`；`ANTHROPIC_AUTH_TOKEN`／`ANTHROPIC_PROFILE` 於建構時已不在進程環境；測試結束後父環境還原。
  5. **SUCCESS 路徑**：receipt 檔存在於 `<out-dir>`、檔名符合固定規則（含 UTC 時戳與 `request_id`）、JSON 欄位齊全（含 `request_mode`、`sdk_max_retries==0`）且 `schema_version==1`、`status=="SUCCESS"`、`advice` 為物件、`payload.sha256` 等於 payload 實際 SHA-256、`usage` 與 `estimated_cost_usd` 一致（例：input 1000／output 500 → `0.014`；含 cache read／write 之組合另一例；cache 兩欄為 `None` 之一例——不例外、成本以 0 計、receipt `usage` 記 `null`；回覆文字含 `sk-ant-` 樣式字串之一例（Rev B.3）——`raw_text` 為 `null`，`request_id`／`usage`／`status`／`payload.sha256` 保留，receipt 與 stdout 不含該字串）、`pricing_basis` 非空；stdout 首行為合法 JSON 且 `receipt_sha256` 等於實際檔案 SHA-256；receipt 與 stdout 皆**不含**金鑰值與 payload 全文、不含 thinking 內容。
  6. **失敗模式各一案**（皆須：exit 碼如 AI-1 第 8 項表、receipt 的 `status` 正確、**`advice` 為 `null`**、不被視為成功；**假 client 的 `create` 恰被呼叫 1 次**，證明腳本無自動重試迴圈）：`stop_reason=="refusal"`（exit 5，記 `stop_details`）；`max_tokens`（TRUNCATED，exit 6，即使 text 看似完整 JSON）；空 content／僅 thinking 區塊（EMPTY）；非 JSON 文字（UNPARSEABLE）；JSON 缺欄位或型別錯（SCHEMA_INVALID）；`strategy` 僅空白；`pause_turn`／`tool_use`／`None`（UNEXPECTED_STOP）；`RateLimitError`(429)、`InternalServerError`(500)、`APIConnectionError`、`APITimeoutError`（exit 4）；`BadRequestError`(400)、`AuthenticationError`(401)、`NotFoundError`(404)（exit 3）；例外訊息內含金鑰值時 receipt 與 stdout 皆已遮蔽。**`INTERNAL_ERROR`（Rev B.3）**：假 client 回合法回應後，於成本計算或 receipt 組裝注入 `Exception` → exit 1、receipt `status=="INTERNAL_ERROR"`、`advice` 為 `null`、`request_id`／`usage` 保留、`error` 已遮蔽且 `http_status` 為 `null`、`create` 恰呼叫 1 次（無重送）；再注入 receipt 寫入失敗 → exit 1、無 receipt、stderr 只含遮蔽後 `request_id`／`usage`／錯誤摘要（不含金鑰、payload 全文、traceback）；注入 `KeyboardInterrupt` → 照常傳播、不被捕捉、不寫 receipt。
  7. **receipt 不覆寫**：同時戳＋同 `request_id` 二次寫入 → 拒絕覆寫（exit 1、原檔位元組不變、目錄內無殘留暫存檔、stderr 只含遮蔽後 `request_id`／`usage`／錯誤摘要）。
  8. **種入缺陷 fixture 有效性**（[行為性]，不聯網）：解析 `advisor_seed_payload.md`，通過 AI-1 第 6 項前置檢查；依 `# file:` 抽出兩檔寫入臨時目錄，以 `sys.executable` 執行 `check_backoff.py` → 必須**失敗**且 stderr 含 `cap not applied: got 1024.0`；套用已知修法（`backoff.py` 改為 `return min(cap, base * (2 ** attempt))`）後再執行 → 輸出 `OK`、exit 0；並斷言 payload 全文**不含**答案鍵字串 `min(cap`。
  9. **隔離**：全部案例前後比對**本票可歸屬落點**——`<git-common-dir>/gate/ADV-APICALL-01.jsonl`（含同名 `.lock`）、`tmp/verify/ADV-APICALL-01.*.conf`——存在狀態與位元組不變（唯讀比對真實 repo；測試不寫入）。全域共用槽位（`tmp/verify_cmd.conf`、`tmp/verify/active.json`、`.tmp/cross_review_state/`、gate 目錄內他票檔）可能被並行 session 合法改寫，**不在測試內做位元組比對**（避免非決定性），改由 AI-4 #2 寫入路徑靜態掃描證明呼叫器與測試無寫入路徑。
- [x] 完成（依據見「收尾實錄」）

### AI-4: 靜態檢核、隔離與回歸
- **檔案**: 不改檔（執行驗證）
- **動作**：
  1. `python -m py_compile .claude/scripts/advisor_call.py`；`python -I .claude/scripts/tests/test_advisor_call.py` 全綠（記錄案例數）；既有 `python -I .claude/scripts/tests/test_gate_ledger.py` 與 `python -I .claude/hooks/tests/gate_stop_guard_test.py` 基線結果不變（本票不改它們，僅執行作回歸）。
  2. 密鑰與端點掃描：`grep -n` 於三個新檔確認 **無**硬編碼金鑰字樣（`sk-ant-` 僅允許出現在「遮蔽／拒絕」邏輯與其測試的規則字串）、**無** `ANTHROPIC_API_KEY` 的讀取（僅允許出現在說明與「只設該變數仍 exit 2」的測試）、`tools=`／`tool_choice=`／`thinking=`／`stream=` 不出現在請求建構處；`max_retries=` 僅出現於 client 建構處且值為 `0`；呼叫器內無重試迴圈（無 `for`／`while` 包住 `create`）；**寫入路徑掃描**：呼叫器與測試的寫檔呼叫（`open(..., "w"/"x"/"a")`、`write_text`、`write_bytes`、`os.link`、`os.open`、`mkdir`、`os.replace`）之目標皆源自 `--out-dir` 或 `tempfile`，無寫入 `tmp/verify*`、`tmp/verify_cmd.conf`、`.tmp/`、`<git-common-dir>`、`~/.claude`、`settings*.json` 之路徑。
  3. 行尾：三新檔 `\r` 行數 0；`git diff --stat <DIFF_REF>..<DIFF_END>` 僅涉 ALLOWED 三檔，FORBIDDEN 零變更；`git check-ignore -v` 三檔零命中。
  4. 前後比對 AI-0 第 5 項各值（`tmp/verify_cmd.conf`、`active.json`、`.tmp/cross_review_state/`、`<git-common-dir>/gate/` 清單與 sha256、`git worktree list`）。不變量＝**呼叫器與其測試未寫入上列落點**；有差異先歸因：可歸於主 agent 依 Gate 契約之合法事件（入口 `check`、`finding`／`round`／`consult` 等，以 ledger 事件 `type` 與時序對照）或 DIFF_REF 回填三原子動作之 conf 再生 → 如實記錄、續行；可歸於他 session／他票（gate 目錄內非 `ADV-APICALL-01` 檔名之檔、全域 `tmp/verify_cmd.conf`／`active.json` 被他票改寫、他票 `cross_review_state`；比照 AI-4 #5 並行 session 歸因）→ 如實記錄、續行；可歸於呼叫器或測試，或無法判定 → 停止回報。
  5. 主 session 認證不變：
     - **主法**（AI-0 #8 基線可用時）：比對 `claude auth status` 輸出的 `authMethod`／`apiProvider`／`subscriptionType`（唯讀；與 AI-0 #8 基線相同）。
     - **備法**（AI-0 #8 判定 `claude auth status` 不可用時，**僅限下列落點**，不掃整個 `~/.claude`）：`.claude/settings.json`、`.claude/settings.local.json`、`~/.claude/settings.json`、`~/.claude/.credentials.json`（若存在）——只比對**存在狀態與 sha256**，**不輸出、不複製內容**。
     - `ANTHROPIC_API_KEY` 未被本票設定、未寫入任何檔（以 Python 子行程查存在性；輸出僅 `PRESENT`／`ABSENT`，並與 AI-0 #4 相同）。
     - **歸因規則**：比對出差異時**先判歸因再判停止**——僅當差異可歸於本票（本票唯一寫入面為 `tmp/ADV-APICALL-01/`、臨時目錄與三個 ALLOWED 檔；呼叫器靜態檢查〔AI-4 #2〕證明無寫入 `~/.claude` 或 `settings*.json` 之路徑；或本票進程設定了 `ANTHROPIC_API_KEY`）才依 Error Handling 停止；**無法歸於本票**（並行 session、OAuth token 刷新等）→ 如實記錄差異與判斷依據，**不歸咎本票、不因此停止**。
- [x] 完成（依據見「收尾實錄」）

### AI-5: 一次真實呼叫（bounded probe；前置為使用者已提供專用變數）
- **檔案**: 不改檔（執行驗證；receipt 寫入 `tmp/ADV-APICALL-01/advisor/`）
- **前置**：`ADVISOR_ANTHROPIC_API_KEY` 已由使用者在執行環境提供（執行當下只查存在性，輸出僅 `SET`／`NOT_SET`）。**缺金鑰口徑（Rev B）**：實作（AI-1、AI-2）、假 client 測試（AI-3）、靜態檢查與回歸（AI-4）**照常執行**；本項記 `NOT_EXECUTED`（不向使用者索取金鑰值於對話中，**金鑰不得貼入對話**），`[行為性][real]` AC **保持未勾**（`UNVERIFIED`）；**不宣稱 S2 顧問真正可用**；轉 `REVIEW`／`CLOSED` 依限定驗收處理——以「`[real]` AC 未勾＋明載 `UNVERIFIED` 與缺金鑰原因」呈報，**不自動視為完成**，其後補跑本項之條件與時機由審查方裁決。
- **動作**：
  1. 先 `--dry-run` 驗證 payload（`advisor_seed_payload.md` **原樣**，不修改），記錄 sha256／位元組數。
  2. 執行第 1 次真實呼叫：`python .claude/scripts/advisor_call.py --ticket ADV-APICALL-01 --payload .claude/scripts/tests/fixtures/advisor_seed_payload.md --timeout 600`（預設 `claude-opus-5-5`、`medium`、`max_tokens 12000`、`request_mode json_schema`；`--timeout 600` 顯式傳入：SDK 保守估算 `3600×12000/128000≈337s` 高於預設 300，為免逾時耗掉第 2 次額度，取授權上限 600；估算偏保守，不代表必然逾時）。
     **timeout（Rev B.3）**：AI-5 每次真實 probe（含第 2 次之 (a)／(b)／(c)）**一律明示 `--timeout 600`**；**不得**以 `--timeout 300`（CLI 預設值）作為真實 probe 之預設或回退值；`max_retries=0` 不變。
     **網路嘗試總額度（Rev B，上限 2，兩次共用）**：「網路嘗試」＝呼叫器通過前置檢查後進入 `create` 的一次執行（含逾時、連線中斷）；`--dry-run` 與 exit 2 不計。**第 2 次僅在第 1 次結果屬下列三類之一時才可執行，且三類只能擇一**：
     - **(a) 自然暫態失敗**（exit 4：429／5xx／408／409／連線／逾時）→ 等待後以**同參數**重跑一次（429 遵守回應之等待指示）。逾時／連線中斷之第 1 次請求**可能已被服務端處理，成本不視為零**，AI-6 記為「成本未知且不為零」。
     - **(b) 格式相容拒絕**（exit 3 且 receipt `error.http_status==400`、`error.message` **明確**指出 structured-output／`output_config.format` 或其與 thinking 之組合不被接受）→ 以 `--request-mode prompt_json` 重跑一次，其餘參數同第 1 次。訊息指向 `ADVICE_SCHEMA` 自身錯誤者：**先離線修實作並補測試，不得 fallback**；修正版（仍 `json_schema`）之重送占用第 2 次額度（此為本類額度之用途，不新增第 4 類、不增額度）。訊息無法判定指向哪一類 → **停止回報，不取得第 2 次**。
     - **(c) `TRUNCATED`**（exit 6，`stop_reason=="max_tokens"`）→ 提高 `--max-tokens`（≤ 24000，依 AI-0 #7）重跑一次，`--timeout` 維持 600（上限），其餘參數同第 1 次（已知殘餘風險：24000 之 SDK 保守估算約 675s，仍高於上限 600）；不改 payload、不換模型、不新增 backend；維持非串流單一路徑，**不新增 streaming 分支**——日後若真實 probe 仍出現 `TRUNCATED`，才以新 evidence 決定是否另開 streaming ticket。
     第 2 次仍失敗 → 如實回報，**不取得第 3 次**。其他結果（exit 3 非 (b)、exit 5、exit 6 之 `EMPTY`／`UNPARSEABLE`／`SCHEMA_INVALID`／`UNEXPECTED_STOP`）→ **停止，記錄 receipt，不取得第 2 次**。**不得**為了測錯誤處理而刻意送出錯誤請求（400／401／404／refusal 一律以 AI-3 決定性測試驗證）。每次嘗試之 receipt 皆保留，不覆寫、不刪除。
  3. 判讀：exit 0 且 `status=="SUCCESS"` 才記可用（`request_mode` 為 `json_schema` 或 `prompt_json` 皆同一判準，並記錄實際模式）；另核對「對應代表性問題」——`advice.strategy`／`advice.steps` 須提及 `next_backoff` 與 `cap`（或 `min`）之一以上關鍵詞，且 `authority_gap==false`；不符 → 如實記錄為「回件未對應該問題」，**不硬套**、不調整 payload 重試。
  4. **本票不以 receipt 寫 ledger**：不以本票 receipt 呼叫 `gate_ledger.py consult`／`round`／`human`（T02 契約放寬生效前，API receipt 不得記 `consult --kind advisor`）；Gate 入口 `check` 與 Gate 契約事件不在此限（見 HC7）。
  5. 比對前後：AI-0 第 5 項各值依 AI-4 #4 歸因規則比對、`ANTHROPIC_API_KEY` 未設、主 session 認證不變（主法 `claude auth status`；不可用則依 AI-4 #5 備法；差異依 AI-4 #5 歸因規則處置）；receipt 與 stdout 不含金鑰。
  6. 記錄**每次嘗試**之 receipt 全文（JSON）、`receipt_sha256`、`request_id`、`request_mode`、`max_tokens`、`usage`、`estimated_cost_usd` 至本票收尾實錄；**該轉錄僅供閱讀，不是長期載體，亦不得代替 receipt 原檔**——原檔位於 `tmp/ADV-APICALL-01/advisor/`（`cleanup_expired_tmp` 7 日 mtime 清理），本票**不搬動、不刪除、不改寫**原檔，耐久副本由 T03 開票時定義（見相關約束末條）；保留原 `created_at_utc`、`request_id` 與 `payload.sha256`，供日後 E1／T02 ⑦ 重用時**不冒稱重新諮詢**。
  7. **probe 結果判讀**：結構化輸出（`output_config.format`）與 adaptive thinking 併用被接受（第 1 次 `json_schema` 成功進入有效回件）＝官方文件相容性（Rev B.3 已 CONFIRMED）於本帳號本日實測可用（N=1）；被**明確拒絕**＝記錄並依第 2 次額度 (b) 處置；以 `prompt_json` 成功者，結論限定為「本帳號本日 `json_schema` 模式被拒、`prompt_json` 可用」，記為本帳號／區域／服務端與官方 CONFIRMED 之偏差（即 fallback 承接之差異），**不得**以 `prompt_json` 成功作為相容性證據。
- [x] 完成（依據見「收尾實錄」）

### AI-6: 收尾實錄與外審欄位回填（寫入本票檔）
- **檔案**: 本票檔（`plans/cso_briefing/project/Pending/ADV-APICALL-01_receipt.md`，收尾實錄）
- **動作**：寫「收尾實錄」：AI-0 命中與基線；各 AC 證據與 `[可達性]` producer 證據欄；AI-3 測試案例清單與數量；AI-4 回歸結果；AI-5 真實呼叫完整紀錄（**每次網路嘗試**之 receipt JSON 轉錄、sha256、`request_id`、`request_mode`、`max_tokens`、`usage`、`estimated_cost_usd`、關鍵詞核對結果、判讀；嘗試次數與第 2 次所屬類別 (a)／(b)／(c)；逾時請求標「成本未知且不為零」；轉錄僅供閱讀，**不得代替 receipt 原檔**，原檔不搬動不刪除）或 `NOT_EXECUTED` 原因；**把頂部 External-review 的 6b③ probe 欄由 `PLANNED` 改記實際結果**；明載**限定交付聲明**：①本票只交付「API 顧問可呼叫並留 receipt」，不代表 E1／E2、T03、T02 已驗，不宣稱顧問流程完整閉環；②`SUCCESS` 只表示回件可用，**不表示問題已解決**，最終以 executor 局部驗證為準；③credits 抵扣與額度用完後扣款 UNKNOWN；④structured outputs＋adaptive thinking 相容性：官方文件 CONFIRMED（Rev B.3 External-review 回流）；本帳號實測結果限本日 N=1；⑤receipt 重用時須保留原時間與來源；⑥**缺金鑰時**（AI-5 `NOT_EXECUTED`）：不宣稱 S2 顧問真正可用、`[real]` AC 保持未勾並記 `UNVERIFIED`，6b③ probe 欄記 `NOT_EXECUTED`＋原因；⑦receipt 耐久副本交 T03 定義，本票不保證 `tmp/` 內原檔之 7 日後存續。若有任何偏差（網路嘗試數、重試類別、環境、認證差異之歸因）逐項如實列出。
- [x] 完成（依據見「收尾實錄」）

## Hard Constraints
1. **只動 ALLOWED 三檔**；不改 `gate_ledger.py`、任何 hook、Gate 條文、`rules/`、`sv/`、`qa/`、`uc/` SKILL、`settings*.json`、`CLAUDE.md`、PROGRESS 檔、Registry、`.codex/**`、`.agents/**`、成果表；不新增依賴宣告檔、不 pin 版本於 repo、不新增 daemon／MCP／DB／佇列。
2. **不給顧問任何工具、不讀 repo**：請求不得含 `tools`／`tool_choice`；顧問只能依 payload 判斷。不用 `advisor_20260301`；不實作 CLI（`claude -p`）或 `--bare` 後端；不實作多 backend。
3. **不更動主 session 認證**：不設、不寫、不要求使用者設定全域 `ANTHROPIC_API_KEY`；呼叫器只讀 `ADVISOR_ANTHROPIC_API_KEY`；不修改 `~/.claude`、`settings*.json`、登入狀態。
4. **進程內隔離**：建 client 前中和 `ANTHROPIC_AUTH_TOKEN`／`ANTHROPIC_PROFILE`，`base_url` 為常數、`api_key` 顯式傳入，使環境變數不改變憑證或請求去向。
5. **密鑰不外洩**：不出現在 stdout／stderr／receipt／例外訊息／argv／測試輸出／本票檔／對話；payload 含 `sk-ant-` 即拒送；金鑰值不得被輸出或複製。
6. **不得把失敗記為成功**：錯誤、空回件、截斷、拒絕、解析失敗、schema 不符、非預期停止、`INTERNAL_ERROR` 一律不為 exit 0／`SUCCESS`；`advice` 於非 SUCCESS 一律 `null`；`prompt_json` fallback 之驗證判準與 `json_schema` 完全相同，**不得放寬**（含 code fence／前後文字即 `UNPARSEABLE`）。
7. **不以 receipt 寫 ledger、不記帳**：呼叫器與其測試不呼叫 `gate_ledger.py`、不寫其 ledger；Gate 入口 `check`（qa／commit／sv／closeout／done）與依 Gate 契約記錄之事件由主 agent 照 `ssot-governance.md` §Autonomous Engineering Gate 執行，不受本條限制（Rule 27 SSOT 優先）；T02 契約放寬生效前，不得以本票 receipt 記 `consult --kind advisor`，不得冒記為現役原生 advisor consult；顧問回件不構成新增授權。
8. **成本邊界**：呼叫器每次執行至多一次網路嘗試（client `max_retries=0`，不自動重試、無重試迴圈）、`max_tokens` 封頂（≤24000）、payload 大小上限、`usage`／估算費用留痕；AI-5 **網路嘗試總數 ≤2（兩次共用額度）**，第 2 次僅限 AI-5 #2 之 (a)／(b)／(c) 擇一，**不得有第 3 次**，不刻意送錯誤請求；逾時／中斷之請求可能已被服務端處理，**成本不視為零**；不建成本封頂平台。輸出 token 費用上界（算術，非帳單）：12000×$20／百萬＝$0.24、24000×$20／百萬＝$0.48，另加輸入費用。
9. **receipt 位置與命名、`schema_version`、欄位名**一經定案即為 T03／T02 的引用依據；變更須同步更新下游票，不得靜默改名。receipt 不含 payload 全文與 thinking 內容。
10. **誠實標籤**：[stub]（決定性測試、假 client）與 [real]（真實呼叫）分開；結構化輸出相容性、帳務層行為不得超出證據宣稱；T01 成功不推導 E1／E2／T03／T02 完成。
11. 新檔 LF 行尾；stdout／stderr UTF-8。
12. `ALLOWED` 維持單一首層 `.claude`；擴至其他首層或第 4 檔 MUST 停止並重判 6b①。

## Error Handling
- AI-0 任一錨點／基線不符 → 依 TPL「基線漂移三分流」分類後處置；範圍內可續者記錄後續作，其餘停止回報，禁自行推定等價位置。
- `git check-ignore -v` 對任一 ALLOWED 路徑命中，或 `anthropic` 無法匯入／缺必要簽名／型別鍵（`output_config.format`／`effort`）→ 停止回報（gitignore 死結律／SDK 契約不符）。
- 發現主 session 認證（`claude auth status` 三欄，或備法限定落點之 sha256／存在狀態，見 AI-4 #5）、`settings*.json` 在本票期間被改變，或 `ANTHROPIC_API_KEY` 被設定 → **先依 AI-4 #5 歸因規則判斷**：可歸於本票 → 立即停止，保存現場，回報；**無法歸於本票**（並行 session、token 刷新等）→ 如實記錄差異與依據，不歸咎本票、不停止。**不掃整個 `~/.claude`，不輸出敏感內容。**
- AI-5：`ADVISOR_ANTHROPIC_API_KEY` 缺 → `NOT_EXECUTED`（`[real]` AC 保持未勾、`UNVERIFIED`；實作／假 client 測試／靜態檢查照常；不宣稱 S2 顧問可用），**不得要求使用者在對話中貼金鑰**。真實呼叫：exit 3 非 (b) 類（401、403、404 模型不可用、400 但訊息非格式相容拒絕）、exit 5、exit 6 之非 `TRUNCATED` 各類 → 停止，記錄 receipt 與判讀，**不重試、不改參數、不換模型**，回報由 Akira 裁決。第 2 次嘗試僅限 AI-5 #2 之 (a) 暫態後重試、(b) 格式相容拒絕後 `prompt_json` fallback、(c) `TRUNCATED` 後提高上限，**三者共用兩次總額度、擇一**；訊息指向 `ADVICE_SCHEMA` 自身錯誤者先修實作、不得 fallback；訊息無法判定者停止；第 2 次仍失敗 → 如實回報，**不得有第 3 次**。
- 回件 `SUCCESS` 但 `advice` 未對應該缺陷（無 `next_backoff`／`cap`／`min` 關鍵詞）或 `authority_gap==true` → 如實記錄為「未對應／權威缺口」，**不視為可用於 E1**，不調整 payload 重試。
- 呼叫器回 `INTERNAL_ERROR`（Rev B.3，已取得 response 後之本地未預期例外）→ 不視為成功、**不重送 API**、不占用 AI-5 第 2 次額度之 (a)／(b)／(c) 任一類；記錄 receipt（或 receipt 寫入失敗時之 stderr 遮蔽摘要）後先修實作並以離線測試覆蓋，回報由 Akira 裁決是否另行安排真實驗證。
- 偵測到密鑰值出現於 stdout／receipt／測試輸出／票檔任一處 → 立即停止，刪除含值檔案（僅限本票產物），回報。
- 需要修改 `gate_ledger.py`／hook／Gate 條文／其他 FORBIDDEN 檔才能達成目標 → 停止回報（SCOPE，屬 T03／T02 或後續票），不自行擴 ALLOWED。
- 任何要求新增 backend、工具呼叫、重試迴圈（含腳本內自動 fallback／自動重試）、ledger 寫入、成本封頂機制、依賴宣告檔才能通過驗收 → 停止回報，不自行實作。
- AI-0 #7 離線預驗顯示 SDK 對 `max_tokens`＝24000 之非串流守衛不再放行 → 依 AI-0 #7 在授權成本範圍內改選可用上限並記錄；**不得改用串流、不得新增 backend**。
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中（標的被 .gitignore 排除）→ 停止回報（gitignore 死結律，候選 #20）

## Acceptance Criteria

> **Rigor Gate 修補紀錄（2026-10-17，lifecycle＝`TICKET_RIGOR`；F1–F4、F6–F8＝Rev B.1，F9＝Rev B.2；報告 `.tmp/rigor-gate/RGR_ADV-APICALL-01_20261017.md`）**：F1 `[可達性]` AC 由「僅列 `檔案:行號`」改為真實 SDK＋`MockTransport` trace（AI-3 補 3d，3c 補 `request-id` 斷言）；F2 HC7／D-a／AI-5 #4 縮限為「呼叫器與其測試不呼叫 `gate_ledger.py`、不以 receipt 記帳」，AI-4 #4／AI-5 #5／AC9 改為歸因比對，依賴三分補 coordination；F3 AI-5 顯式 `--timeout 600`；F4 cache 欄位 `None` 以 0 計；F6 刪「並含數字」；F7 拒絕覆寫改 exclusive 落位；F8 固定標題整行比對；F9（同 F2 根因之反向掃描）AI-3 #9 收窄至本票可歸屬落點、AI-4 #4 補他 session／他票寫入歸因、AI-4 #2 補寫入路徑靜態掃描。**兄弟 AC 重驗**：AC4（假 client SUCCESS）與 3d 共用 `main()` 入口與 receipt 契約，預期一致、未縮減；AC6 請求形狀（3a–3c）不受影響，3c 僅加斷言；AC9 縮限比對範圍而非豁免——「呼叫器與測試零寫入」不變量完整保留，證明方式改為 AI-3 #9 對本票可歸屬落點位元組比對＋AI-4 #2 寫入路徑靜態掃描（F9：全域共用槽位可被並行 session 合法改寫，測試內不做位元組比對）；AC10 `[real]` 判準不變，僅第 1 次命令帶 `--timeout 600`；AC11 收尾實錄之「`[可達性]` producer 證據欄」改為輔助欄，仍須回填。無 AC 被移除或豁免。**F5 已裁決（Rev B.3，Akira 2026-10-17）**：新增 receipt `status="INTERNAL_ERROR"`（見 AI-1 #8／#10、AI-3 #6、Error Handling、HC6、失敗模式 AC）；兄弟 AC 重驗——AC4 SUCCESS 判準不變，`[可達性]` AC 之 trace 走 SUCCESS 路徑不受影響，失敗模式 AC 為擴充（新增一類失敗必不為 SUCCESS），無 AC 被移除或縮減。
- [x] **[存在性]** 三個 ALLOWED 新檔存在；`advisor_call.py` 含 `main(argv, *, client_factory, env, now)`、固定顧問角色常數、`ADVICE_SCHEMA`（`additionalProperties:false`、六欄 required）、exit code 表與檔頭 docstring（AI-1）；`python -m py_compile` 通過
- [x] **[存在性]** `advisor_seed_payload.md` 含 6 個固定標題、兩個 `# file:` code block（`backoff.py`、`check_backoff.py`）、失敗證據含 `cap not applied: got 1024.0`，且**不含**答案鍵 `min(cap`（AI-2）
- [x] **[可達性]** given 真實 `anthropic.Anthropic`（`httpx.MockTransport` 回 200 合法 `end_turn` JSON 並帶 `request-id` header、dummy key、零網路）為 `client_factory` ／ when 以 `advisor_seed_payload.md` 驅動 `main(...)` ／ expect mock 恰收到 1 次請求且 body 含 payload 全文、exit 0、receipt 寫入 `<out-dir>` 且檔名與 `request_id` 皆為該 header 值、`status=="SUCCESS"`、`usage` 為 SDK 解析值、stdout 首行 `receipt_sha256` 等於實際檔案雜湊（AI-3 案例 3d 執行結果＝路徑被走過之證據）。`檔案:行號` 僅為輔助證據欄（執行後回填）：payload producer＝`advisor_seed_payload.md`〔證據：fixtures/advisor_seed_payload.md:1-49；讀取端 advisor_call.py:216 `_check_payload`〕；請求建構＝`advisor_call.py`〔證據：advisor_call.py:432 `_build_request`〕；SDK 呼叫＝`client.messages.create`〔證據：advisor_call.py:483〕；receipt 寫入與命名＝〔證據：advisor_call.py:387 `_write_receipt`（命名）、:327 `_receipt_from_response`（組裝）〕；stdout 單行 JSON＝〔證據：advisor_call.py:503〕；**只列位置不得勾選本項**。**下游消費者（T03 ER `api_receipt`、T02 `--note`）尚無正式票號與實作，本 AC 只驗介面（位置、命名、欄位、`receipt_sha256`）已固定並經上述 trace 走過，不宣稱下游已接線**
- [x] **[行為性]** given 假 client 回傳合法 `end_turn` JSON ／ expect exit 0、receipt `status=="SUCCESS"`、欄位齊全（含 `schema_version`、`request_id`、`payload.sha256`、`usage`、`estimated_cost_usd`、`pricing_basis`）、檔名符合固定規則、stdout `receipt_sha256` 等於實際檔案雜湊、無金鑰與 payload 全文（AI-3 案例 5）
- [x] **[行為性]** given 缺金鑰／僅設 `ANTHROPIC_API_KEY`／payload 不符各案 ／ expect exit 2、無 receipt、client 未被建構；`--dry-run` 無金鑰可執行、不聯網不寫檔（AI-3 案例 1–2）
- [x] **[行為性]** given 請求建構 ／ expect `create` kwargs 無 `tools`／`tool_choice`／`thinking`／`temperature`／`top_p`／`top_k`／`stream`／`metadata`／assistant prefill；**`json_schema` 模式**含 `output_config.effort` 與 `format.json_schema`、receipt `request_mode=="json_schema"`；**`prompt_json` 模式**無 `format`、`system` 含 `ADVICE_SCHEMA_JSON` 且 receipt `request_mode=="prompt_json"`、成功判準未放寬；`--max-tokens 24000` 不被 SDK 非串流守衛攔下（零網路）；client 以專用變數值、常數 `base_url`、`max_retries==0` 建構，`ANTHROPIC_AUTH_TOKEN`／`ANTHROPIC_PROFILE` 已中和（AI-3 案例 3a–3c、4）
- [x] **[行為性]** given `refusal`／`max_tokens`／空 content／非 JSON／schema 不符／空白 strategy／非預期停止／429／500／連線／逾時／400／401／404 各案 ／ expect 依 AI-1 第 8 項表之 exit 碼與 `status`，`advice` 為 `null`，**無一案被記為 SUCCESS**，且每案假 client `create` 恰呼叫 1 次（無自動重試）；例外訊息內金鑰值已遮蔽；given 已取得 response 後注入本地 `Exception` ／ expect exit 1、`status=="INTERNAL_ERROR"`、`advice` 為 `null`、`request_id`／`usage` 保留、無重送，receipt 寫入失敗時 stderr 只含遮蔽摘要，`KeyboardInterrupt` 不被捕捉（Rev B.3）（AI-3 案例 6）
- [x] **[行為性]** given 同時戳同 request_id 二次寫入 ／ expect 拒絕覆寫、原檔不變；given 種入缺陷 fixture ／ expect 原樣執行失敗且含 `got 1024.0`、套用已知修法後 `OK`（AI-3 案例 7–8）
- [x] **[行為性]** 隔離：全部測試與 AI-4 前後比對下，`tmp/verify_cmd.conf`、`tmp/verify/active.json`、`.tmp/cross_review_state/`、`<git-common-dir>/gate/` 除 AI-4 #4 歸因排除之 Gate 合法事件、DIFF_REF 回填與他 session／他票寫入外位元組不變（呼叫器與測試零寫入：AI-3 #9 對本票可歸屬落點位元組比對＋AI-4 #2 寫入路徑靜態掃描）；主 session 認證（`claude auth status` 主法；不可用則 AI-4 #5 備法）與 `ANTHROPIC_API_KEY` 狀態不變，差異依 AI-4 #5 歸因規則記錄（AI-3 案例 9、AI-4）
- [x] **[行為性][real]** 真實呼叫驗證（AI-5）：最終有效回件 `exit 0`、`status=="SUCCESS"`、`stop_reason=="end_turn"`、`advice` 通過 schema 且 `authority_gap==false`、`advice` 內容提及 `next_backoff` 與 `cap`／`min` 之一以上、`usage`／`estimated_cost_usd` 有值、`request_id` 有值、`request_mode` 已記錄、每次網路嘗試之 receipt `timeout_s==600`（Rev B.3）；receipt 與 stdout 不含金鑰；**網路嘗試總數 ≤2（共用額度），第 2 次僅限 (a)／(b)／(c) 擇一**，無第 3 次、無刻意錯誤請求；逾時請求成本標「未知且不為零」；`created_at_utc`／`request_id`／`payload.sha256` 已記入收尾實錄供日後重用，receipt 原檔未搬動未刪除；**本項只證「呼叫能力＋回件可用」，不證問題已解決、不證 E1／E2；`prompt_json` 成功不證 structured outputs 與 thinking 相容**。缺金鑰則 `NOT_EXECUTED`／`UNVERIFIED`（**不補勾**、不宣稱 S2 顧問可用，轉 `REVIEW`／`CLOSED` 依限定驗收處理，不自動視為完成）
- [x] **[存在性]** 收尾實錄含：AC 證據、`[可達性]` producer 證據欄已回填、測試案例清單與數量、回歸結果、AI-5 完整 receipt 紀錄或 `NOT_EXECUTED` 原因、6b③ probe 欄已由 `PLANNED` 改記實際結果、限定交付聲明（AI-6 七點）、偏差清單、AI-0 #7（非串流上限）與 #8（認證基線）實測記錄
- [ ] ~~assembleDebug PASS~~ (N/A: feature preset 之純 `.claude/` 工具，無 `app/`／gradle 變更)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（`aa94d361..5b80bc32`：3 檔 +1109／−0，皆 ALLOWED；FORBIDDEN 零變更）

## 收尾實錄（UC 執行，2026-10-17）

**AI-0**（HEAD `aa94d361`，`DIFF_REF=aa94d361`）：#1 三 ALLOWED 檔不存在、未追蹤、`check-ignore` 零命中，`tmp/ADV-APICALL-01/advisor/x.json` 命中 `.gitignore:52`；#2 `anthropic` 0.105.2，`Messages.create` 含 `max_tokens`／`messages`／`model`／`output_config`／`system`／`timeout`，`OutputConfigParam`＝`effort`／`format`，`JSONOutputFormatParam`＝`schema`／`type`，`stop_reason` Literal 含 `refusal`／`max_tokens`／`pause_turn`，10 個例外類皆在；#3 `gate_ledger.py:92` `TICKET_RE` 原式一致，`settings.json` allow 含 `Bash(python *)`；#4 `ADVISOR_ANTHROPIC_API_KEY`＝SET、`ANTHROPIC_API_KEY`／`ANTHROPIC_AUTH_TOKEN`／`ANTHROPIC_PROFILE`＝NOT_SET、`ANTHROPIC_BASE_URL`＝SET；#5 基線落 `tmp/ADV-APICALL-01/baseline_ai0.txt`（15 行），worktree 2 個；#6 `advisor_call`／`feature:advisor-api-call` 僅命中 `.claude/PROGRESS.md`（登冊提交之 Dashboard 敘述）與本票檔，無既有實作；#7 離線 `MockTransport`：client 顯式 `timeout=300` 時 `max_tokens=24000` 不被攔（mock 收到 1 次請求）→ `MAX_TOKENS_CEILING=24000` 沿用；#8 `claude auth status` 可執行，基線三欄＝`authMethod=claude.ai`／`apiProvider=firstParty`／`subscriptionType=max`。

**AI-1／2／3 交付**：`advisor_call.py` 513 行、`test_advisor_call.py` 547 行、`advisor_seed_payload.md` 49 行；三檔 LF（`` 計 0）。測試 **25 案**（`python -I`，0.9s，零網路）：前置／設定（缺鑰、僅設 `ANTHROPIC_API_KEY`、ticket／payload 各案、參數非法）、`--dry-run`、3a／3b／3c／3d（`json_schema`、`prompt_json` 與未放寬驗證、24000 非串流守衛、真實 SDK＋`MockTransport` 成功全鏈）、憑證隔離、SUCCESS receipt、成本三例（含 cache `None`）、未知模型、key-like 回覆 `raw_text=null`、敏感字樣 `raw_text=null`、失敗模式 19 案（`create` 皆恰 1 次）、`refusal` `stop_details`、例外訊息遮蔽、`INTERNAL_ERROR`（成本計算注入、receipt 寫入失敗、`KeyboardInterrupt` 傳播、取得 response 前未預期例外）、receipt 不覆寫、種入缺陷 fixture（原樣失敗含 `got 1024.0`、套用已知修法後 `OK`）；模組級 `tearDownModule` 比對本票可歸屬落點（`gate/ADV-APICALL-01.jsonl`＋`.lock`、`tmp/verify/ADV-APICALL-01.*.conf`）位元組不變（通過）。

**AI-4**：`py_compile` 通過；`python -I test_advisor_call.py` 25／25 OK；回歸 `test_gate_ledger.py` 53／53 OK、`gate_stop_guard_test.py` 54／54 OK（本票未改二者）。掃描：三檔無硬編碼金鑰（`sk-ant-` 僅在遮蔽／拒絕規則與測試字串）、`advisor_call.py` 無 `ANTHROPIC_API_KEY` 讀取（僅測試內「只設該變數仍 exit 2」一處）、請求建構處無 `tools=`／`tool_choice=`／`thinking=`／`stream=`、`max_retries=` 僅 client 建構處（值 `0`）、無包住 `create` 之迴圈、寫檔呼叫目標皆源自 `--out-dir`／`tempfile`。`git status` 僅三個 ALLOWED 新檔未追蹤；`check-ignore` 零命中。前後比對 AI-0 #5 基線：AI-4 後、AI-5 後皆 `SAME`（無差異，無需歸因）；`git worktree list` 不變；`ANTHROPIC_API_KEY`＝ABSENT；`claude auth status` 三欄與基線相同。

**AI-5 真實呼叫（`[real]`，網路嘗試 **1／2**，未使用第 2 次額度）**：dry-run（payload sha256 `4d1541186a02c86c6206abb674d1f54e21256819a5d7a8abe70251201b897f53`，1634 bytes）後執行 `python .claude/scripts/advisor_call.py --ticket ADV-APICALL-01 --payload .claude/scripts/tests/fixtures/advisor_seed_payload.md --timeout 600`：`exit 0`、`status=SUCCESS`、`stop_reason=end_turn`、`request_mode=json_schema`（structured outputs 與 adaptive thinking 於本帳號本日被接受，N=1）、`model_returned=claude-opus-5-5`、`request_id=req_011Cg7jy3wgQgG8nDTQnXDeF`、`created_at_utc=2026-10-17T11:37:03Z`、`usage` input 1331／output 649／cache 0／0、`estimated_cost_usd=0.018304`（估算，非帳單）、`timeout_s=600`、`sdk_max_retries=0`。關鍵詞核對：`advice` 含 `next_backoff`、`cap`、`min`，`authority_gap=false`，對應該代表性問題。receipt 與 stdout 不含金鑰（`sk-ant-` 與金鑰值皆零命中）。**receipt 原檔**：`tmp/ADV-APICALL-01/advisor/advisor_20261017T113703Z_req_011Cg7jy3wgQgG8nDTQnXDeF.json`，`receipt_sha256=58fc9596ef40a8f53e2fa36d77f0f70579d7f937a988687fa6a601b0c569301d`；原檔未搬動、未刪除、未改寫。下方 JSON 為**轉錄，僅供閱讀，不是長期載體，不得代替原檔**（`tmp/` 7 日 mtime 清理；耐久副本由 T03 開票時定義）：

```json
{
  "advice": {
    "authority_gap": false,
    "authority_gap_reason": "修法僅變更 `backoff.py`，不修改 `check_backoff.py`、不新增依賴，完全落在既有授權內；`cap` 語義為上限已由參數名稱與檢查腳本 assert 明確表達，無需額外產品語義裁決。",
    "evidence_needed": [],
    "minimal_verification": "在僅含兩檔案的目錄執行 `python check_backoff.py`，預期 stdout 為 `OK` 且 exit code 為 0（可用 `python check_backoff.py; echo $?` 確認）。亦可局部執行 `python -c \"from backoff import next_backoff as f; print(f(0), f(3), f(10, base=1.0, cap=30.0))\"`，預期輸出 `1.0 8.0 30.0`。",
    "steps": [
      "修改 `backoff.py` 的 `next_backoff`：將 `return base * (2 ** attempt)` 改為 `return min(base * (2 ** attempt), cap)`。",
      "保留函式簽名與預設值（`base: float = 1.0, cap: float = 30.0`）不變，避免影響其他呼叫端。",
      "（可選，非本題必要）若 attempt 極大時擔心 `2 ** attempt` 產生巨大整數，可另行評估是否先截斷 attempt；此舉不影響本檢查結果，故不列為必要變更。"
    ],
    "strategy": "在 `next_backoff` 回傳前以 `min(..., cap)` 套用上限，使回傳值不超過呼叫端傳入的 `cap`。對 attempt=0、3 的既有結果（1.0、8.0，皆小於預設 cap 30.0）不受影響；attempt=10 時 1024.0 被截為 30.0，滿足 assert。僅改 `backoff.py`，不新增依賴，屬既有授權範圍內的本地修法。"
  },
  "created_at_utc": "2026-10-17T11:37:03Z",
  "effort": "medium",
  "error": null,
  "estimated_cost_usd": 0.018304,
  "exit_code": 0,
  "kind": "advisor_api_receipt",
  "max_tokens": 12000,
  "model_requested": "claude-opus-5-5",
  "model_returned": "claude-opus-5-5",
  "payload": {
    "bytes": 1634,
    "path": ".claude/scripts/tests/fixtures/advisor_seed_payload.md",
    "sha256": "4d1541186a02c86c6206abb674d1f54e21256819a5d7a8abe70251201b897f53"
  },
  "pricing_basis": "claude-opus-5-5 $4/$20 per MTok, cache read $0.20, 5m cache write $5; platform.claude.com/docs/en/about-claude/pricing last_updated 2026-10-16",
  "raw_text": "{\"strategy\":\"在 `next_backoff` 回傳前以 `min(..., cap)` 套用上限，使回傳值不超過呼叫端傳入的 `cap`。對 attempt=0、3 的既有結果（1.0、8.0，皆小於預設 cap 30.0）不受影響；attempt=10 時 1024.0 被截為 30.0，滿足 assert。僅改 `backoff.py`，不新增依賴，屬既有授權範圍內的本地修法。\",\"steps\":[\"修改 `backoff.py` 的 `next_backoff`：將 `return base * (2 ** attempt)` 改為 `return min(base * (2 ** attempt), cap)`。\",\"保留函式簽名與預設值（`base: float = 1.0, cap: float = 30.0`）不變，避免影響其他呼叫端。\",\"（可選，非本題必要）若 attempt 極大時擔心 `2 ** attempt` 產生巨大整數，可另行評估是否先截斷 attempt；此舉不影響本檢查結果，故不列為必要變更。\"],\"minimal_verification\":\"在僅含兩檔案的目錄執行 `python check_backoff.py`，預期 stdout 為 `OK` 且 exit code 為 0（可用 `python check_backoff.py; echo $?` 確認）。亦可局部執行 `python -c \\\"from backoff import next_backoff as f; print(f(0), f(3), f(10, base=1.0, cap=30.0))\\\"`，預期輸出 `1.0 8.0 30.0`。\",\"authority_gap\":false,\"authority_gap_reason\":\"修法僅變更 `backoff.py`，不修改 `check_backoff.py`、不新增依賴，完全落在既有授權內；`cap` 語義為上限已由參數名稱與檢查腳本 assert 明確表達，無需額外產品語義裁決。\",\"evidence_needed\":[]}",
  "request_id": "req_011Cg7jy3wgQgG8nDTQnXDeF",
  "request_mode": "json_schema",
  "schema_version": 1,
  "sdk": {
    "name": "anthropic",
    "version": "0.105.2"
  },
  "sdk_max_retries": 0,
  "status": "SUCCESS",
  "stop_details": null,
  "stop_reason": "end_turn",
  "ticket": "ADV-APICALL-01",
  "timeout_s": 600,
  "usage": {
    "cache_creation_input_tokens": 0,
    "cache_read_input_tokens": 0,
    "input_tokens": 1331,
    "output_tokens": 649
  }
}
```

**6b③ probe 欄**：`PLANNED` → `EXECUTED`（上列真實呼叫，限本帳號、本日、N=1）。

**限定交付聲明**（Rev B.4 增補⑧：Codex Cross-Review 非 PASS——實際 verdict＝REPORT_FIX×2，經 Akira override D 續行至 `REVIEW`；`reviews.codex=PASS` 為平台映射值，不代表 Codex 通過；失敗分支真實 API 形狀 UNVERIFIED（stub-only））：①本票只交付「API 顧問可呼叫並留 receipt」，不代表 E1／E2、T03、T02 已驗，不宣稱顧問流程完整閉環；②`SUCCESS` 只表示回件可用，**不表示問題已解決**，最終以 executor 局部驗證為準（本次回件之修法未被套用到任何檔案）；③credits 抵扣與額度用完後扣款 UNKNOWN；④structured outputs＋adaptive thinking 相容性：官方文件 CONFIRMED（Rev B.3）；本帳號實測 `json_schema` 模式成功，限本日 N=1；⑤receipt 重用時須保留原 `created_at_utc`、`request_id`、`payload.sha256`，不冒稱重新諮詢；⑥金鑰存在，AI-5 已 `EXECUTED`（非缺金鑰口徑）；⑦receipt 耐久副本交 T03 定義，本票不保證 `tmp/` 內原檔 7 日後存續。

**偏差與實作選擇（如實）**：(1) 網路嘗試 1 次，無重試、無 fallback、無提高上限。(2) AI-0 #6 grep 命中 `.claude/PROGRESS.md`（登冊提交之敘述），非既有實作。(3) 為使「回覆含 key-like 字串時 receipt 與 stdout 不含該字串」成立，`advice` 內字串亦以 `sk-ant-[A-Za-z0-9_-]+` 與專用變數值深度遮蔽（`raw_text` 固定規則依 Rev B.3 未擴張）；`Authorization`／`x-api-key` 字樣比對採不分大小寫（誤判僅使 `raw_text=null`，屬安全側）。(4) 無 `usage`／非 `claude-opus-5-5` 時 `pricing_basis="unknown for model"`，依 AI-1 #11 字面。(5) 取得 response 前之非 SDK 未預期例外：exit 1、不寫 receipt、stderr 僅遮蔽摘要（AI-1 #8 `INTERNAL_ERROR` 契約之反面）。(6) `--timeout` 上限檢核於 CLI 解析（>600 → exit 2）。(7) `python -m py_compile` 產生之 `__pycache__` 被 `.gitignore` 排除，非提交標的。(8) 認證差異：無。

**QA／Commit 記錄**：`/qa` Step 0 PASS（commit 後 `DIFF_REF=aa94d361`／`DIFF_END=5b80bc32`，實測 1109 行，MAX_LINES=N/A）；Step 1–3.5（`assembleDebug`／`testDebugUnitTest`／`lint`／靜態盲區掃描）依票面 N/A（feature preset 之純 `.claude/` 工具，`app/`／gradle 零變更，`git status` 僅三個 ALLOWED 新檔）未執行；Step 4 PROGRESS.md `SKIPPED — DEFERRED-TO-DOCSYNC`（FORBIDDEN 命中；同步債＝本票 Dashboard 同步、Done 標記、票檔 Pending→deliver 搬移，依 SOP 於下一輪 `/compact` 處理，不另開票）；Step 4.5 Auto Commit `5b80bc32`（`[ADV-APICALL-01]`，僅 stage 三個 ALLOWED 檔；blob 內 `` 計 0）。

**receipt 耐久保存（Akira 2026-10-18 經 SKAD）**：durable path＝`plans/cso_briefing/project/deliver/ADV-APICALL-01_advisor_receipt.json`；sha256＝`58fc9596ef40a8f53e2fa36d77f0f70579d7f937a988687fa6a601b0c569301d`（與 tmp 原檔位元組一致，`cmp` 通過）；request_id＝`req_011Cg7jy3wgQgG8nDTQnXDeF`；created_at_utc＝`2026-10-17T11:37:03Z`；preserved_at_utc＝`2026-10-18T07:41:53Z`；原始 tmp path＝`tmp/ADV-APICALL-01/advisor/advisor_20261017T113703Z_req_011Cg7jy3wgQgG8nDTQnXDeF.json`（原檔未移動、未刪除）。未重發 API、未改 JSON 任何 byte、未寫 ledger，不記為 consult／claim／E1／E2。
