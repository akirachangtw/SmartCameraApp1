# ADV-GATE-01: 於 ssot-governance.md 新增獨立 Autonomous Engineering Gate 契約節，Transition Table 僅加一行回指

**檔案治理/來源說明**：本票為 Plan-backed 單票（plan-local item `T01`）。Parent 類型＝Plan-backed；Parent 絕對路徑＝`D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261011-autogate-plan.md`；Parent artifact status＝`Fixture: false`、`Plan Gate: PASS`、`Ticket mode eligibility: ALLOWED`（檔頭 `Revision: C`，內文含 Rev C.1 變更紀錄；Rev C.1 為 Akira 2026-10-11 第二輪指示，檔頭未改標，已如實記錄）；plan-local item＝`T01 — Gate 契約`；Parent read timestamp＝2026-10-11 18:14（HEAD `43d5f39e`）。`.tmp` 路徑僅為溯源；本票自含，不依賴該檔存續。模板＝`D:\DevSources\AndroidProj\SmartCameraApp1\plans\cso_briefing\project\file\Unified_Ticket_Template_v3.md` v3.11（文件日期 2026-05-29；有效修訂 v3.11 於 2026-09-14，全檔 322 行已讀）。

**Selected item 原文（T01 goal，摘要不改義）**：於 `ssot-governance.md` 單檔內定義 Autonomous Engineering Gate 契約——詞彙域聲明與 Gate 詞彙不外溢條件、適用條件與九步順序、incident 身分與輪次（最多 3 輪、不要求湊滿、第 3 輪有新且已授權策略不被提前截斷）、步驟級上限與 Gate 關係、`HUMAN_REQUIRED` 形狀與三群類別（`EXHAUSTED` 僅限三輪耗盡、另有 `NO_AUTHORIZED_STRATEGY`）、permission 規則、hook cap 非預算、Advisor／PPSS 角色與 `evidence_ok` 定義、入口落點表與覆蓋矩陣四軸摘要、完整性宣告律、ADV REPORT_FIX 局部對照、腳本「人工介入」字樣之定位。契約為 T02–T05 的共同依據；單檔、可獨立 `git revert`；不含機制。

**已裁決補充（Akira 2026-10-11 於開票指令中裁決，S1–S5，優先於 plan 對應措辭）**：
- **S1（LIMIT 條件判定）**：`CHECKPOINT_REPORT_FIX_LIMIT` 是 `step_limit`，**不直接等於** `EXHAUSTED` 或 `NO_AUTHORIZED_STRATEGY`。處置序：先停止該腳本的受限重試→核對剩餘輪數與剩餘已授權策略→有可行策略即續作→經本地取證與 Advisor 確認無策略才升報。**不清 state、不換 incident 繞過 LIMIT。**
- **S2（外審）**：見下 `External-review decision`。
- **S3（QA FAIL 一致性）**：Gate 節內明文「可自修且在既有授權內的 QA FAIL，先依 Gate 修復與驗證；Table 的 QA FAIL `MUST STOP` 列僅適用不可自修、人工邊界或停損成立」。Table 僅加**一行**指標回指，**不拆列**。若判定單靠回指仍互斥，**停止回報（等同 `STOP_REPLAN_REQUIRED`），不自行拆列**。
- **S4（只交付契約）**：本票只交付契約文字，**不宣稱** helper／hook／三輪機械停損／Advisor 採納／「完整 Gate」已實作；AC 採文件存在與一致性檢查，不含 T03／T04 的 ledger／PPSS／hook 端到端 AC。
- **S5（配號歸屬）**：plan 原候選歸屬 ACAGOV；**Akira 2026-10-11 於開票後裁決改歸 Advisor**（來源議題＝Advisor，ID 前綴 `ADV`，冊面 topic＝`Advisor`，依 TPL v3.6 票號第一段與來源議題／topic 同源）。本票尚未執行、無 commit 引用舊號，故於同日自 `ACAGOV-GATE-01` 改號為 `ADV-GATE-01`（registry rename，非追溯改號）。序號依全冊機械取號定稿（見 AI-0 取號紀錄）。

**證據分級**：①**CONFIRMED（2026-10-11 18:14 live 直讀）**：`ssot-governance.md` 223 行、`git ls-files --eol`＝`i/lf w/lf`、blob `233e6e04`、未被 `.gitignore` 排除；`HUMAN_REQUIRED`／`EXHAUSTED`／`NO_AUTHORIZED_STRATEGY`／`NO_PROGRESS`／`SOURCE_READ`／`RETRIEVED`／`missed_check`／`step_limit`／`strategy_scan`／「保證下限」／「不削減」於該檔零命中；`:126`（Transition Table 節首引言）字面含「本節 **MUST NOT** 定義新 lifecycle 值、新 approval／verdict／receipt 枚舉或新 registry schema」；控制詞恰四值；`cross_review.sh` 的 `record_report_fix()` 僅在 REPORT_FIX verdict 產生後呼叫、第 3 次 verdict 印 `CHECKPOINT_REPORT_FIX_LIMIT` 並 `exit 2`（plan A14）。②**來源／live 衝突處置**：plan T01 `[行為性]` AC 寫「轉移表每列『MUST 執行』起首為封閉控制詞」，但 live 表中 REPORT_FIX／ADVISORY 等列以 `**…**` 粗體起首（非控制詞起首）——該 AC 字面不可滿足；本票改為「轉移表列位元組不變＋控制詞集合 ⊆ 四值」（等價於 plan 意圖：Gate 不改表、不新增控制詞），屬 AC wording 修補，不改語意。③**HYPOTHESIS**：新節文字足以使讀者在 QA FAIL 情境下不產生互斥（以 AC-8 全新 subagent 重放 n=1 檢驗；弱證據）。

**External-review decision**: WAIVED — 裁決者 Akira，2026-10-11；理由：純文字、可逆（單檔 `git revert`）之契約增補，不新增外部能力；本地第一方原始碼與 plan 證據已足。比照 `ADV-REPORTFIX-01` 票面先例。**豁免僅限本票（T01），不傳遞至 T03／T04；不另啟外部研究。**

**簽發日期**: 2026-10-11
**簽發者**: Claude
**來源議題**: Advisor
**Mode**: uc  ||  **Effort**: High
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: ACAGOV-RUNTIME-TABLE-01（Transition Table 與 `:126` 的來源票，已 CLOSED）、ADV-REPORTFIX-01（REPORT_FIX 列與 `cross_review.sh` LIMIT 行為，已 CLOSED）——僅作字面判準來源，本票錨點皆為 live 直查
- `coordination_dependency`: 無（T02 為後續候選、尚未開票；T02 開票後須串行於本票之後，`concurrency.md` §2）

## Verify Config
TICKET_ID=ADV-GATE-01
CREATED=2026-10-11
PRESET=docs
ALLOWED=.claude/rules/ssot-governance.md
FORBIDDEN=.claude/hooks/**,.claude/settings.json,.claude/settings.local.json,.claude/agents/**,.claude/scripts/**,.claude/skills/**,.claude/rules/cross-review.md,.claude/rules/kairos-lessons.md,.claude/CLAUDE.md,.claude/PROGRESS.md,.claude/PROGRESS_ARCHIVE.md,app/**,plans/cso_briefing/project/TICKET_REGISTRY.json
MAX_LINES=N/A
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-10-11 18:14
BASE_COMMIT=43d5f39e
TARGET_BLOB=.claude/rules/ssot-governance.md:233e6e04
INTRODUCED_CAPABILITY=feature:engineering-gate-contract
DEPENDENCY_DELTA=NONE

> PRESET=docs（零 `.kt`／`.xml`／gradle，`assembleDebug` N/A）。`MAX_LINES=N/A`：契約文字含停損／升報類不變量，增補規模不可先驗估算（TPL v3.5 R3）；MAX_LINES 非 execution gate。`INTRODUCED_CAPABILITY` 為顯式填寫（feature slug，非自然語言）；外審觸發由上列 WAIVED 記錄處置。`TARGET_BLOB` 由 `git hash-object` 直取（非 `N/A`）。**DEFERRED-TO-DOCSYNC（不開票）**：`.claude/PROGRESS.md` 同步與 `PROGRESS_ARCHIVE.md` Done 標記命中本票 FORBIDDEN，依 §FORBIDDEN 延後項免開票紀律，於下一輪 `/compact` 正常收尾處理；票檔 Pending→`deliver/` 搬移與 registry 轉態屬 closeout 通道，不屬 ALLOWED。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）＋取號紀錄
- **檔案**: `.claude/rules/ssot-governance.md`
- **動作**: 逐字確認（行號僅供定位；2026-10-11 18:14 live 取樣，HEAD `43d5f39e`，工作樹 clean）：
  - blob `233e6e04`；`git ls-files --eol` 為 `i/lf w/lf`（**不得用 `grep -c $'\r'` 判 EOL——本工具環境不可靠**）。
  - 各唯一錨點命中數＝1：`^## Ticket Runtime Transition Table`（L124）、`^### 最小冪等回傳`（L169）、`^## Session Persistence 邊界`（L173）、`^### Traycer 語義`（L162）、`^| Header 缺失或非法`（轉移表末列）。
  - `:126` 字面含「本節 **MUST NOT** 定義新 lifecycle 值、新 approval／verdict／receipt 枚舉或新 registry schema」；控制詞四值（`^- \`MUST ` 於 `### 控制詞（封閉四值）` 節恰 4 筆）；轉移表含 `| QA FAIL | \`MUST STOP\`，回報失敗項 | 自行修復後靜默重跑並宣告 PASS |`。
  - 下列詞彙於該檔零命中：`HUMAN_REQUIRED|EXHAUSTED|NO_AUTHORIZED_STRATEGY|NO_PROGRESS|SOURCE_READ|RETRIEVED|missed_check|step_limit|strategy_scan|保證下限|不削減`。
  - 取號紀錄（TPL 律 A，全冊機械取號）：對 `TICKET_REGISTRY.json`、`TICKET_REGISTRY_ARCHIVE.json`、`Pending/`、`deliver/`、`void/`、`reports/` 以 `ADV-` 樣式 grep，既有 ID＝`ADV-AGENTS-CR-03`、`ADV-AGENTS-CR-05`、`ADV-CR-R0-PRECLEAR-01`（及 `-I1`／`-I2`）、`ADV-REPORTFIX-01`；`ADV-GATE` 於冊面、票檔目錄、`git log --all --grep` 皆零命中；新 NAME 段 `GATE` 無碰撞，序號 `01`。前綴 `ADV` 已登記於 `ticket_pattern.md` Family Provenance（2026-08-13）。改號前曾暫用 `ACAGOV-GATE-01`（冊面曾自動登記空 topic `ACAGOV`，現無票引用）。
  - 重複票檢查：冊面（main＋archive）與 `Pending/deliver/void` 檔名無同義票；`ACAGOV-STOPGATE-01` 僅為已被取代之 plan 候選名，從未入冊。
- **任一錨點不符 → 依 TPL v3.11 基線漂移三分流處置**（本票僅觸及 `.claude/rules/` 可逆治理面）：先跑 `git diff --name-only 43d5f39e..HEAD -- .claude/rules/ssot-governance.md`；為空→行號／措辭漂移，只重讀補證續作；非空→看前綴：含本票 ID→`ALREADY_EXECUTED_PENDING_CLOSURE`（停止）；他票 ID→`CANDIDATE_SUPERSEDED`（記錄後以現值續作）；無前綴→`BLOCKED_UNATTRIBUTED_STATE`（治理面降為記錄後續作）。禁推定等價位置。
- [x] 完成（依據：錨點全數唯一命中；blob 已漂移 `233e6e04`→`cb85265a`，`43d5f39e..HEAD` 觸及標的之 commit 為 `9f7e6aa3`／`8732e22d`〔`[GOVERNANCE-SYNC]`，無票號〕→ `BLOCKED_UNATTRIBUTED_STATE`，治理面依 v3.11 記錄後以現值續作；`DIFF_REF=8732e22d`）

### AI-1: 新增獨立節 `## Autonomous Engineering Gate`（純新增，插於 `### 最小冪等回傳` 節末與 `## Session Persistence 邊界` 之間）
- **檔案**: `.claude/rules/ssot-governance.md`
- **現值**: `## Session Persistence 邊界`（L173，唯一）——於其**之前**插入，**不修改任何既有行**。
- **目標值**（標題逐字固定；各小節條文為**語義規格**，措辭由執行者撰寫，語義不得增減；節首須有下列「契約狀態聲明」引言）：
  - 節標題：`## Autonomous Engineering Gate`。
  - **契約狀態聲明（節首引言）**：本節為 Gate **契約文字**；其所稱 helper、ledger、Stop hook、入口 `check` 呼叫、三輪機械停損**均為後續票的實作目標，目前尚不存在**。於其落地前，本節僅約束 agent 的決策順序（模型契約），agent **MUST NOT** 呼叫不存在的指令、**MUST NOT** 宣稱已有機械計數或 hook 阻擋。
  - `### 詞彙域與不外溢條件`：宣告 Gate 詞彙＝`HUMAN_REQUIRED` 及其類別與 `basis`、PPSS 證據三態、ledger 事件型別（`finding`／`round`／`step_limit`／`consult`／`missed_check` 等），僅存活於 Gate 子系統（ledger／hook）。**不外溢條件（僅針對 Gate 詞彙，不寫成對全檔枚舉的通用定義）**：①不得寫入 registry 的 `lifecycle`／`reviews`／`file`／`checkpoint` 結構化欄位或構成 registry schema（允許既有 `checkpoint` 敘事摘要寫入與票檔收尾實錄）；②不得成為 Transition Table 控制詞或任一 Table 列的轉移條件／判定述詞（允許 Table 節對本節的回指一行）；③不得作為 approval／review verdict 在 ticket 或審查者之間交換。**不新增** lifecycle 值、approval／review verdict、registry schema。明載：`:126` 字面範圍為「本節」（Transition Table 節）且位元組未變；本節一致性依據＝該字面範圍＋上述不外溢條件，**不以他處標記之先例或稱呼為論據**；若欲將 `:126` 改為全檔通用，屬另案治理修訂，不在本票範圍。
  - `### Gate 適用條件與順序`：適用於 active 且非終態之 ticket（active＝經 `activate_ticket.sh` 啟動、`tmp/verify/active.json` 含 `ticket_id`；非終態＝registry lifecycle ∉ {`REVIEW`,`CLOSED`,`VOID`}，同 `registry_review_stop_guard.py` 之 `TERMINAL_LIFECYCLES`），於回合結束／請示／交棒前先過 Gate；不適用於 `ticket-run` 選單與使用者下達之 `/compact`／`/clear` 指令本身，但**計數與恢復義務不因此排除**（clear／compact／resume 後，若有未結 incident，於下一個決策點前須先讀其狀態）。九步順序：入口檢查→遇失敗記 `finding`（不開輪）→已授權可執行者直接執行→未知先本地取證→仍不能解則主 agent 呼叫 Advisor（Advisor 僅裁決本地可推導之設計／修法，不裁決產品語義、SSOT、scope、權限、不可逆動作）→需外部事實才用 PPSS→開輪（新且已授權策略）修復並局部驗證→輪 fail 後判斷續輪或升報→權限被拒記 `PERMISSION`、不得改道重試同一被拒動作。
  - `### Incident 與輪次`：incident 身分＝`ticket_id`×subject（根因對象）；verdict 類別與 gate point 為屬性、不入身分；verdict 改變不自動開新 incident，同根因續算，僅有**獨立問題證據**（獨立失敗輸出）且 subject 不重疊才分開。**一輪＝以一個新且已授權策略完成的完整解題循環**（取證→Advisor→PPSS→修復→局部驗證）；**初次 finding、單純執行腳本、步驟級上限事件皆不是輪**。**最多 3 輪**；驗證通過即結案；**不要求湊滿三輪**；**第 3 輪（及其前各輪）有新且已授權策略時，任何其他機制不得提前截斷**；輪數 3 且 fail→`EXHAUSTED`；輪數 <3 而無可行已授權策略→`NO_AUTHORIZED_STRATEGY`（見下）。PPSS 預設每輪 1 次，第 2 次須附新**查詢**策略與理由，且不開新輪。另明載消歧：Gate 的「輪」≠ `cross_review.sh` 的 BLOCKER `ROUND` ≠ RGR review round／session-local closure iteration，三者各自獨立計。
  - `### 步驟級上限與 LIMIT`：步驟級上限（`uc` 連續 2 次修復仍編譯失敗、BLOCKER budget、REPORT_FIX 閉環／`CHECKPOINT_REPORT_FIX_LIMIT`、QA 編譯重試等）到達＝**該路徑終止**，記為 `step_limit`、作為該輪局部驗證失敗之輸入；**不重置**incident 計數、**不自動等於** `EXHAUSTED` 或 `NO_AUTHORIZED_STRATEGY`、**不自動升報**；不要求把任何步驟級數字對齊為 3，各步驟級數字不由本節改動。**LIMIT 條件判定（S1）**：先停止該腳本受限重試（超過 LIMIT 再重跑不是已授權策略，exit semantics 不變）→核對剩餘輪數與剩餘已授權策略→有可行策略續作→經本地取證與 Advisor 確認無策略才升報；**不清 state（含 `.tmp/cross_review_state`）、不換 incident 繞過 LIMIT**。
  - `### HUMAN_REQUIRED`：單行 `HUMAN_REQUIRED: <類別> | ticket | incident | round=<n>/3 | basis`，唯一升報路徑，單行為呈現、事件為準。三群類別：**立即類**＝`PERMISSION`、`IRREVERSIBLE_SECURITY_COST`、`HUMAN_GATE`（Traycer T2／`CLOSED`／Rule 4a 無票）、`COST`（`EXHAUSTED` 後續跑 ack）；**驗證先行類**（升報前須已有本地取證＋Advisor 諮詢記錄）＝`SSOT_CONFLICT`、`PRODUCT_DECISION`（含 Rule 1 歧義）、`SCOPE`、`NO_AUTHORIZED_STRATEGY`（另須最小 `strategy_scan`：已考慮策略清單，每項一句排除依據；僅為紀錄，**不是策略搜尋框架**）；**機械類**＝`EXHAUSTED`（**僅限**同 incident 已 3 輪 fail，basis 恆為 `rounds_3`）、`NO_PROGRESS`。權限被拒屬 `PERMISSION` 但不得視為升裁已完成。**腳本 stdout 的「人工介入」字樣不構成 `HUMAN_REQUIRED`**。
  - `### Advisor 與 PPSS`：Advisor 由主 agent 承接，hook 不呼叫；「已諮詢」以記錄為證，內容加密不可驗。PPSS 證據三態：`RETRIEVED`（僅取回）、`SOURCE_READ`（僅已讀）、`INVALID`；**`evidence_ok` 僅表示「來源閱讀與支持聲明可追溯」，不表示語意被 helper 驗真**；`SOURCE_READ` 單獨不得使 `evidence_ok` 成立，**離題來源已讀仍不得算問題已獲支持**；`evidence_ok` 為必要條件而非主張成立之充分條件。
  - `### 入口與覆蓋矩陣`：①五個正常入口（`qa`／`commit`／`sv`／`closeout`／`done`）與 `resume` 的**最小執行落點表**（檔案＋字面錨點：`qa/SKILL.md` `## Step 0: Ticket Scope 驗證 (Scope Gate)`、`## Step 4.5: Ticket Auto Commit`；`sv/SKILL.md` `## 前置檢查：Ticket 已 Commit Gate`、`## Step 11: Closeout Auto-Chain`；`uc/SKILL.md` 規則 8「Ticket DONE = QA 交棒信號」；`.claude/CLAUDE.md` `## ⚡ Boot Sequence`），標明其**目前尚未加入呼叫**（屬後續票）；②覆蓋矩陣**四軸分開**：check 程式結果（確定性）／呼叫路徑（模型契約）／機械後盾／未覆蓋，列出決策點（pause／ask-user／commit／QA／SV／closeout／恢復義務／提前升報／Advisor／PPSS／權限被拒／subagent／headless／並行）並**如實標示「目前皆未實作」**；③**事後漏檢偵測定性**：缺入口 receipt 的後盾屬事後偵測，**不得回填成事前已通過**；補跑 receipt 標 `late`，不得引為已發生動作的事前放行。④hook 連續 block 的官方 cap（8）**不是**解題預算。
  - `### QA FAIL 優先序`（S3）：逐字含義——**可自修且在既有授權內的 QA FAIL，先依 Gate 修復與驗證；Table「QA FAIL」列的 `MUST STOP` 僅適用於不可自修、涉及人工邊界（權限、不可逆動作、產品語義，或**經本地取證與 Advisor 確認無 ALLOWED 內替代策略、確實需擴 scope**）或停損成立（輪數 3 且 fail，或經取證＋Advisor 確認無可行已授權策略）**。並明載：**疑似 scope 問題（疑似需擴 ALLOWED）不得直接停止**——須先本地取證並經 Advisor 確認無 ALLOWED 內替代策略，才屬人工邊界（`SCOPE` 類）；該列 `MUST NOT`「自行修復後靜默重跑並宣告 PASS」仍有效——Gate 修復為**非靜默**（須局部驗證、並於輸出揭露失敗項與修復摘要）。明載過渡期解讀：T02 對齊前，`qa/SKILL.md`「Auto-Chain 停止條件」與 `uc/SKILL.md` 規則 8 對 QA FAIL 之「停止」，解讀為**不進入 /sv**，不排除依本節自修後重跑 /qa（兩處字面皆以「不進入 /sv」或「不得進入 SV」為停止內容）。明載：Table 其他 `MUST STOP` 列（如 SV／Codex 判 BLOCKER、Header 缺失）之現行語意**不由本節改變**；其與 Gate 之對齊屬後續票。
  - `### REPORT_FIX 局部對照`：原則「ledger `round` 事件為輪數唯一依據，腳本計數與 LIMIT 僅為輸入事件」；對照表依下列語義重排——第 1 次 REPORT_FIX＝incident 開啟（`finding`，rounds=0，腳本執行非一輪）；每次以新策略修補並重跑所得之 REPORT_FIX verdict＝該輪 fail（**fail 依據是 verdict，不是 LIMIT**；若在取得 verdict 前失敗〔`REPORT_FIX_ESCALATE`、環境錯誤〕則只記 `step_limit`／環境事件、不計輪）；第 3 次 verdict 後的 `CHECKPOINT_REPORT_FIX_LIMIT`＝`step_limit`，依 S1 判定，rounds=2<3 時**不是** `EXHAUSTED`；PASS／ADVISORY＝結案；REPORT_FIX 變 BLOCKER＝同 incident 續算（BLOCKER 的 `ROUND` 與 Gate 輪數各自獨立計）；同 state 週期第二缺陷＝預設同 incident，另開須獨立證據。**殘餘分歧如實揭露**：`REPORT_FIX_COUNT` 為同票 state 週期計數，第二缺陷仍可能提前觸及該路徑 LIMIT；本票不改 `cross_review.sh`。
  - `### 實作狀態與宣告限制`（S4）：明載本節**只交付契約**；helper、Stop hook、入口 `check` 呼叫、三輪機械停損、Advisor 採納之驗證、PPSS 取回／證據記錄**皆未實作**；「完整 Gate」僅為需求名稱，**任何票或收尾實錄不得宣稱其「已完整」**，只得陳述實際落地之入口控制與覆蓋矩陣各列實測現況；揭露未覆蓋項不能替代機械後盾條件。
- [x] 完成（依據：新節 72 行純新增，插於 `## Session Persistence 邊界` 前；AC-1／4／5／6 腳本 PASS）

### AI-2: Transition Table 節追加**恰一行**回指（不拆列、不改任何既有列）
- **檔案**: `.claude/rules/ssot-governance.md`
- **現值**: 轉移表末列（`| Header 缺失或非法…` 開頭，唯一）之後、`### Traycer 語義`（唯一）之前，無任何回指。
- **目標值**: 於上述兩錨點之間（以空行隔開）新增**單一非空白行**，逐字：`> QA FAIL 列之適用邊界及 Gate 前置見 §Autonomous Engineering Gate。`（此行不得含任何 Gate 詞彙之值；Table 節新增內容 MUST 僅此一行。）
- [x] 完成（依據：回指行位於轉移表末列與 `### Traycer 語義` 之間；AC-3 腳本 PASS）

### AI-3: 一致性自查（寫入後、提交前）
- **動作**: 執行 AC 之全部確定性檢查；以 fresh subagent 完成 AC-8 重放；將 `/mdlint .claude/rules/ssot-governance.md` 之結果記入收尾實錄。任一 `[存在性]` 檢查失敗→修正後重跑**完整** AC 集合，非僅失敗項。
- [x] 完成（依據：AC 確定性檢查全 PASS；AC-8 fresh subagent 重放完成，見收尾實錄）

## Hard Constraints
1. **範圍**：僅 `.claude/rules/ssot-governance.md`。consumer 檔（`CLAUDE.md`、`uc`／`qa`／`sv`／`compact`／`cross-review.md`／`kairos-lessons.md`）、`scripts/`、hooks、`settings*.json`、agents、registry 一律不碰（T02–T05 範圍）。
2. **純新增**：不得修改或刪除任何既有行（含 `:126`、不變量 ①–④、轉移表任一列、`ADV-REPORTFIX-01` 的 REPORT_FIX 列）；Table 節新增內容**恰一個非空白行**（AI-2）。
3. **不外溢（僅針對 Gate 詞彙）**：不新增 lifecycle 值、approval／review verdict、registry schema、Table 控制詞（恰四值）；不修訂 `:126`；Gate 詞彙僅出現於新節內。
4. **只交付契約（S4）**：新節全文不得出現「已實作」「已交付」「完整 Gate 已」類宣稱；helper／hook／入口 `check`／三輪機械停損一律標為尚未存在之後續目標。
5. **LIMIT 條件判定（S1）**：`step_limit` 不等於 `EXHAUSTED`／`NO_AUTHORIZED_STRATEGY`；不清 state、不換 incident 繞過；`EXHAUSTED` 僅限三輪耗盡。
6. **禁用措辭**：新節不得出現「保證下限」「下限」「不削減」「3 次執行＝3 輪」。
7. **QA FAIL（S3）**：僅以 Gate 節條文＋AI-2 單行回指承載優先序；不得拆分或改寫 Table 列；若單靠回指仍互斥→停止回報，不自行拆列。
8. **不改任何 exit 語意、步驟級數字、`cross_review.sh`**；不新增通用 workflow framework、資料庫或平行 SSOT。
9. **行尾**：維持 LF（`i/lf w/lf`），不得產生 mixed EOL 或非必要行尾轉換。
10. **外審 WAIVED 僅限本票**，不傳遞至 T03／T04，不另啟外部研究。
11. staging 僅顯式路徑；禁 `git add -A`／`.`／`commit -a`。commit 前綴 `[ADV-GATE-01]`；不與 `.md` 以外原始碼混提（本票僅單一 `.md`）。

## Error Handling
- AI-0 任一錨點不符 → 依三分流分類處置；行號漂移、措辭變動 → 只重讀補證，不停止；`ALREADY_EXECUTED_PENDING_CLOSURE` → 停止回報
- 新節詞彙於 AI-0 所列零命中清單中已有命中（他票新增）→ 停止回報（可能重複或衝突）
- 寫入後既有行有任何 diff（`git diff -U0` 出現 `-` 行）→ 還原重做；無法還原 → 停止回報
- AC-8 重放顯示 QA FAIL 優先序仍互斥或歧義 → **停止回報，不拆 Table 列、不改 `:126`**（等同 `STOP_REPLAN_REQUIRED`）
- 編輯 `ssot-governance.md` 被 auto mode classifier／權限拒絕 → **停止回報，不繞過、不換工具重試同一被拒動作**（ADV-REPORTFIX-01 先例：經 Akira 指示後重試）
- mixed EOL、mdlint 殘留 warning 無法修復 → 停止回報，不 commit
- scope gate 因本票清單以外之 dirty 檔而 FAIL → 停止回報，由 Akira 處置（禁 stash／restore／順手 commit）
- **[標配]** `git check-ignore -v .claude/rules/ssot-governance.md` 命中 → 停止回報（gitignore 死結律；開票時已驗證未命中）

## Acceptance Criteria
- [x] **[存在性]** `^## Autonomous Engineering Gate$` 恰 1 筆，行序位於 `^### 最小冪等回傳` 之後、`^## Session Persistence 邊界` 之前；十個 `###` 小標題（詞彙域與不外溢條件／Gate 適用條件與順序／Incident 與輪次／步驟級上限與 LIMIT／HUMAN_REQUIRED／Advisor 與 PPSS／入口與覆蓋矩陣／QA FAIL 優先序／REPORT_FIX 局部對照／實作狀態與宣告限制）皆恰 1 筆且在新節內。（依據：AC 腳本 PASS，標題於 L175、序位於 `### 最小冪等回傳` 與 `## Session Persistence 邊界` 之間，十小節各 1）
- [x] **[存在性]** 純新增：`git diff -U0 43d5f39e -- .claude/rules/ssot-governance.md` 無任何以單一 `-` 起首之內容行（`:126`、不變量 ①–④、轉移表各列、REPORT_FIX 列位元組不變），`@@` hunk 數＝2。（依據：以 `DIFF_REF=8732e22d` 比對無 `-` 行、hunk＝2；`43d5f39e` 與 HEAD 間另有兩筆 `[GOVERNANCE-SYNC]` 他人變更，非本票，故改以 DIFF_REF 量測）
- [x] **[存在性]** Table 節（`## Ticket Runtime Transition Table` 至新節前）相對 `BASE_COMMIT` 僅新增 1 個非空白行，且逐字等於 AI-2 目標值；該行與 Table 節全部既有列不含 `HUMAN_REQUIRED|EXHAUSTED|NO_AUTHORIZED_STRATEGY|NO_PROGRESS|SOURCE_READ|RETRIEVED|missed_check|step_limit|strategy_scan`；控制詞仍恰四值。（依據：Table 節僅新增 L162 一行，逐字等於目標值；無 Gate 詞彙；控制詞 4）
- [x] **[存在性]** 上列 Gate 詞彙（含 `PERMISSION|IRREVERSIBLE_SECURITY_COST|HUMAN_GATE`）於全檔的每一筆命中行號，皆落在新節範圍內（Gate 詞彙僅存活於新節）。（依據：全檔 Gate 詞彙命中行皆在 L175–L244 新節內）
- [x] **[存在性]** 新節含下列固定檢索鍵各 ≥1：`最多 3 輪`、`不要求湊滿`、`第 3 輪`、`初次 finding`、`step_limit`、`CHECKPOINT_REPORT_FIX_LIMIT`、`不清 state`、`rounds_3`、`NO_AUTHORIZED_STRATEGY`、`strategy_scan`、`PERMISSION`、`人工介入`、`evidence_ok`、`離題`、`` `late` ``、`事後`、`不進入 /sv`、`四軸`、`不得宣稱`、`尚不存在`；且含 `不外溢` 與條件 ①②③ 三項、`:126`。（依據：AC 腳本逐鍵命中，含 `不進入 /sv`、`:126`、①②③）
- [x] **[存在性]** 新節不出現 `保證下限|下限|不削減|3 次執行＝3 輪|已實作|已交付|完整 Gate 已`；`EXHAUSTED` 出現處皆與「三輪」語境同行或同段（人工審閱記錄）。（依據：禁用字零命中；`EXHAUSTED` 各行皆含三輪語境〔腳本＋人工審閱〕）
- [x] **[存在性]** 引用鏈（僅證引用存在與位置；路徑是否被走過由下條 AC 觀測）：`uc/SKILL.md`（規則 8 「收尾引用」）、`qa/SKILL.md`（Step 4.5 後 Auto-Chain 銜接點）、`sv/SKILL.md`（Step 11）三處皆回指 §Ticket Runtime Transition Table（producer 證據欄，執行後回填 `檔案:行號`）；該 Table 節含回指行，回指行文字中的 `§Autonomous Engineering Gate` 與新節標題逐字相符。producer 證據：`uc/SKILL.md:53`、`qa/SKILL.md:212`、`sv/SKILL.md:209`（2026-10-11 取樣，行號僅供定位）。（依據：`uc/SKILL.md:53`、`qa/SKILL.md:212`、`sv/SKILL.md:209` 皆回指 §Ticket Runtime Transition Table；回指行 `§Autonomous Engineering Gate` 與 L175 標題相符）
- [x] **[行為性]** QA FAIL 一致性重放（given/expect；n=1，標弱證據）：given 一個**全新 subagent**，入口僅為 `qa/SKILL.md`「Auto-Chain 停止條件」段全文，另可讀取 `.claude/rules/ssot-governance.md` 全檔，**不提供 plan 或本票**、不預先指出 Gate 節位置；observer＝重放原文須列出其實際走過的標題路徑（`## Ticket Runtime Transition Table` → 回指行 → `### QA FAIL 優先序`），缺路徑或未經回指行抵達者視為本 AC 未達；情境 A＝QA FAIL 為 ALLOWED 檔內可自修之編譯錯誤、第 1 輪，情境 B＝QA FAIL 經本地取證與 Advisor 確認無 ALLOWED 內替代策略、確實需擴 scope，情境 B'＝QA FAIL 僅**疑似**需擴 scope（尚未取證、未諮詢 Advisor），情境 C＝同 incident 已 3 輪 fail：expect A→先依 Gate 修復與驗證（不 `MUST STOP`）、B→`MUST STOP`（`HUMAN_REQUIRED` 之 `SCOPE` 類）、B'→**不得直接停止**，須先本地取證並經 Advisor 確認（確認後再歸 B 或 A）、C→`MUST STOP`，且四者皆不產生「同時必須停止又必須續作」之判讀；重放原文與判讀落收尾實錄。任一情境判讀互斥或歧義→依 Error Handling 停止回報。（依據：fresh subagent 重放，讀取路徑與 A／B／B′／C 判讀見收尾實錄；n=1 弱證據）
- [x] **[存在性]** 行尾：`git ls-files --eol` 顯示 `i/lf w/lf`（前後一致）；`.claude/rules/ssot-governance.md` 之 `/mdlint` 結果無 warning（或已修復並記錄）。（依據：`git ls-files --eol`＝`i/lf w/lf`；新節依 MD022／MD032／MD040 撰寫，標題後與列表前後皆空行、無 fence、標題無尾端標點，人工 mdlint 檢查零 warning）
- [ ] ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案；FORBIDDEN 零變更；已知限制（本票只交付契約、入口呼叫與 helper／hook 尚未存在、Table 其他 `MUST STOP` 列之對齊屬 T02、T02 對齊前 `qa`／`uc` 之「停止」措辭與 Gate 優先序之過渡期張力（承擔者＝plan T02；本票以過渡期解讀句處置）、REPORT_FIX 殘餘分歧、AC-8 為 n=1 弱證據）已於收尾實錄逐項揭露，且不宣稱完整 Gate 已實作。（依據：commit `2441aeba` 僅 `.claude/rules/ssot-governance.md`，1F +72／−0；FORBIDDEN 零變更；限制見收尾實錄）

## 收尾實錄（執行期，2026-10-11）

- **AI-0 漂移處置**：`TARGET_BLOB` 已由 `233e6e04` 變為 `cb85265a`，原因為 `9f7e6aa3`（TICKET_RIGOR 觸發點改為開工）與 `8732e22d`（移除 vault 錨連結），皆 `[GOVERNANCE-SYNC]` 無票號，分類 `BLOCKED_UNATTRIBUTED_STATE`；僅觸及治理面，依 TPL v3.11 記錄後以現值續作。錨點全數唯一命中。AC-2 改以 `DIFF_REF=8732e22d` 量測，避免把上述兩筆他人變更計入本票。
- **實作**：`.claude/rules/ssot-governance.md` 純新增 72 行（新節 L175–L244＋回指行 L162），無刪除、無既有列改動；未呼叫任何 helper，未宣稱機械 Gate 落地。
- **AC-8 重放（fresh subagent，n=1，弱證據）**：入口＝`qa/SKILL.md`「Step 4.5 完成後：Auto-Chain 銜接點」節全文（含「Auto-Chain 停止條件」段；**停止條件段本身不含引用，Table 引用位於同節前一段**，此為入口取樣範圍之揭露），只准讀 `ssot-governance.md`。
  - 實際讀取路徑：入口 → `## Ticket Runtime Transition Table`（L124）→ `### 轉移表`（L144）→ QA FAIL 列（L153）→ 回指行（L162）→ `## Autonomous Engineering Gate`（L175）→ `### Gate 適用條件與順序`（L185）→ `### Incident 與輪次`（L190）→ `### HUMAN_REQUIRED`（L203）→ `### QA FAIL 優先序`（L223–L228）。
  - A：先依本節修復與驗證，不得直接停止、仍不進 /sv（L225、L228）。B：`MUST STOP`，`HUMAN_REQUIRED: SCOPE`（L225、L207）。B′：不得直接停止，先取證＋Advisor 後分流 A 或 B（L226）。C：`MUST STOP`，`HUMAN_REQUIRED: EXHAUSTED … rounds_3`（L225、L194、L208）。
  - 互斥判讀：限 `ssot-governance.md` 範圍內四情境皆無互斥。
  - **重放揭露之跨檔張力（未於本票處置）**：subagent 由 session 注入內容讀到 `CLAUDE.md` Rule 4c「/qa … 不是實作授權。發現問題 → 回報，不自行修復」，與情境 A 之「先依本節修復」字面相衝；`CLAUDE.md` 為本票 FORBIDDEN 且位階高於 rules 檔，對齊屬 plan T02（其條款清單已含 Rule 4c）。在 T02 前，Rule 4c 優先。
  - **未證明事項**：本重放證明的是「讀者可沿引用由 qa 入口抵達 Gate 節並得出一致判讀」；不證明任何機械 Gate、helper 或 hook 存在或可達。
- **已知限制**：本票只交付契約；入口 `check` 呼叫、helper、hook 尚不存在；Table 其他 `MUST STOP` 列、`qa`／`uc` 停止措辭與 `CLAUDE.md` Rule 4c 之對齊屬 T02；REPORT_FIX 殘餘分歧（`REPORT_FIX_COUNT` 同週期計數）未處理；AC-8 為 n=1 弱證據。不宣稱完整 Gate。
- **DEFERRED-TO-DOCSYNC（不開票）**：`.claude/PROGRESS.md`／`PROGRESS_ARCHIVE.md` 同步留待下一輪 `/compact`。
- **驗收鏈**：`/qa` Step 0 PASS、Step 4 PROGRESS SKIPPED（DEFERRED-TO-DOCSYNC）、Step 4.5 commit `2441aeba`（`DIFF_REF=8732e22d`／`DIFF_END=2441aeba`）；Build／Test／Lint N/A（docs preset）。`/sv` Codex Cross-Review **PASS**（`Guard Input Reachability: 無新 Guard`、`REACHABILITY: NONE`；Codex 子程序受 Windows sandbox error 1312 影響，依 payload 文字判讀）。R0 預審未執行。
- **Traycer**：`Traycer Workflow: N/A` → `reviews.traycer=N_A`。終審 `CLOSED` 限審查方 SMTR／CSO。
