# INFRA-COMPACT-CLOSURE-GATE-APP-01: /compact 提交前冊面收束一致性閘（Dashboard CLOSED ≠ 冊面 CLOSED 缺口封口）

**簽發日期**: 2026-09-02
**簽發者**: Claude (SMTR)
**來源議題**: INFRA
**Mode**: uc  ||  **Effort**: Low
**Traycer Workflow**: N/A（單一 skill .md 條文增補，§0 五分鐘 `git restore` 域）
**Rev D**（2026-09-09，Round 2 Rigor Gate FAIL 回流裁決，派工前修訂）：Round 2 判 FAIL。**F7 Critical 成立，且成因為 SMTR Rev C 自身**——Rev C 新增之 AC「三集合枚舉與『五段觸發與寫入者』節逐字一致（含 `BLOCKED`／`DRAFT_NEEDED`）」**不可能滿足**。一手核實（`aud_20260909_230d670f04`／`28fa7fe321`）：該節僅列 `TICKET_RIGOR`／`IN_PROGRESS`／`EXECUTED`／`REVIEW`／`CLOSED` 五值（其性質為「誰在何時寫入」，非「哪個狀態歸哪個目錄」）；`PENDING`／`BLOCKED`／`VOID` 不在其中；`DRAFT_NEEDED` **於 APP SSOT 全檔零命中**。Rev C 在修正 HC5 之法源失效時，自身又造了一條法源錯位之 AC——同類錯誤同輪復現。**F8 Medium 成立**（「判定分支數＝3」未定義計數單位）。**F6' Medium 成立**：Rev C 摘要「F6 採納（Rev B 保全項補機械 assignee）」為誤記——原 F6 指 `coordination_dependency` 之 `INFRA-COMPACT-REVB-01` 保全要求無機械 assignee，Rev C 實際加入者為 AC 之執行端實跑自證，二者混同。

**Rev D 三項處置全數採納**，惟 **F7 之來源寫法改採 SSOT 原文用語**：外審草案提出「執行者觸發流程狀態 vs CSO 管理冊面狀態」二分，該分類法 **APP SSOT 並不存在**（SSOT 自身用語為「活態未交付」「旁支」「終態」），依 O5「外審之分層／分類提案預設拒絕」不採；且正確來源節本就在盤，直接逐條引用即可，無須另立分類。

**Extreview 4a 存廢裁決（2026-09-09，Owner：Akira）**：本票 Rev A→B→C→D 計 4，4a 於 Rev C 首次達 3 即成立，兩輪 rigor gate 事實上構成外審回流。**裁決：續修**（不退役、不降級）。授權範圍僅既有 AI-0～AI-2 依 Rev D 修訂後執行；票級 closure 前凍結本子系統新修補票之開立，凍結出口同 `INFRA-PREFLIGHT-CORE-01` D.3-4（新事實／規格矛盾／安全邊界改變三者之一且無法納入既開 AI，須 Akira 明示解除）。**依 O6 遲滯律，本裁決落檔後 4a 計數重置為 0；存廢裁決本身不計 Rev。**

**⛔ Rev D 落地後 MUST 重跑 rigor gate**；本票不預先宣稱 Round 3 PASS。

**Rev C**（2026-09-09，Rigor Gate FAIL 回流裁決，派工前修訂）：Rigor gate 判 FAIL（1 Critical）。**F1 Critical**——HC5「禁引入 PLT 五態字面」與 AC-2「三 token 命中＝0」之法源引註 `ssot-governance.md` L81「治理減量裁決」**已被同檔推翻**：SMTR 一手讀檔核實（`aud_20260909_712db8d5a9`），該檔現行「五段觸發與寫入者」節逐字列出 `TICKET_RIGOR`／`IN_PROGRESS`／`EXECUTED`／`REVIEW`／`CLOSED`，並明載 `lifecycle` 合法值 SSOT 為平台 `registry_service.py` `VALID_LIFECYCLE`——**APP 現行 SSOT 即五段，非四態**（2026-08-27 四態裁減已於 2026-09-06 由 Akira 以 `INFRA-LIFECYCLE-SSOT-01` 推翻）。且本票 AI-1 判準表自身即逐字列出 HC5 所禁之三 token，**票面在被寫下的當刻已違反自己的 HC5**，依字面實作必使 AC-2 FAIL。SMTR 另發現報告未及之第三層：判準表尚含 `BLOCKED`／`DRAFT_NEEDED` 二 token，同樣不在 HC5 之「APP 四態」內——HC5 之「只用四態」前提於本票判準表面前**整體失效**，非僅三 token 問題。

**Akira 2026-09-09 裁決：採甲案**（HC5 由「禁字面出現」改為「禁在判斷邏輯中使用超額狀態」），附 SMTR 兩處收緊（AC-2 連動改寫＋判準表單一 SSOT 指向）。**乙案（將集合縮為 `{REVIEW, CLOSED}`）否決**，三項理由：①會使級 A 反向判定失去機械依據，退回票面 L57 明禁之語義裁量；②會使 AC〔Rev B 新增〕之人造情境（`deliver/` 而 `lifecycle=PENDING` → FAILED）失去判準，語義變為隱式否定，未來新增 lifecycle 值將被靜默判 FAILED；③乙案係遷就一條法源已失效之 HC——正解為修 HC，非砍判準表以配合錯誤約束。

**F2–F6 處置**：F2 採納（補列 `evidence_dependency`，並刪除已消滅之條件分支）；F3 採納但**降級 Minor**（最小處置為一句批次語義，不另立排序機制）；F4 採納（TPL 引註 v3.8→v3.10）；F5 **不處理**（級 B 字串比對鬆散且風險方向偏安全，依 §0 不為安全方向加碼）；F6 採納（Rev B 保全項補機械 assignee）。**〔Rev D／F6' 更正〕本句誤記**：原 F6 指 `coordination_dependency` 之 `INFRA-COMPACT-REVB-01` 保全要求無機械 assignee；Rev C 實際加入者為 **AC 之執行端實跑輸出自證**，非 assignee。本票**未新增** `coordination_dependency` 之 mechanical assignee；如需機械化責任指派，MUST 另案裁決，**禁回溯宣稱本票已完成**。

**⛔ 本 Rev 落地後 MUST 重跑 rigor gate**（依報告要求，本裁決不代為判定重跑結果）。

**Rev B**（2026-09-07，Akira 回流採納裁決，派工前修訂）：Rev A 之一致性判定「票檔進 `deliver/` → lifecycle ∈ {CLOSED, VOID}」**錯誤**——PLT 五段④明載 `REVIEW` 時票檔即搬 `deliver/`，`REVIEW` 為**合法交付態**，Rev A 判定會使每張 `REVIEW` 票於 `/compact` 時被閘門誤報 FAILED。成因為 SMTR 2026-09-05 丁案將 `Pending/` 邊界錨於終態而非交付點，本票沿用了該錯誤錨點。Rev B 改採**目錄—狀態明確集合**之兩級判定（見 AI-1 第 2 條）。同型缺陷於 PLT `INFRA-COMPACT-CLOSURE-GATE-01` 之存在與否列為 PLT 改善候選 C-3（`INFRA-LIFECYCLE-SSOT-01` 附錄），本票不推定。
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: `INFRA-LIFECYCLE-SSOT-01`（**Rev C 補列，F2**——APP 五段 lifecycle 語義之 SSOT 來源票，已交付 `deliver/`；本票 AI-1 判準表之集合字面直接源自其交付內容）；PLT `INFRA-COMPACT-CLOSURE-GATE-01`（CLOSED，`docs/deliver/`）為設計藍本，其二次復發事件（TKR-13 commit `b334b93`／TKR-14 commit `9d4af62`，兩次 Dashboard 標 CLOSED 而冊面 lifecycle 停留 PENDING、`file` 欄殘留 pending 死指標）為缺口存在之因果證據；APP `.claude/rules/ssot-governance.md` L58 機械判準（查治理冊面票 MUST 用 `registry_list`，MUST NOT 用 `get_ticket`）為 AI-1 判準之法源
- `coordination_dependency`: **`INFRA-COMPACT-REVB-01` 之 APP 部署票（尚未開立，待 Akira 裁三點）——ALLOWED 交集硬約束**：該票為 `.claude/skills/compact/SKILL.md` 整檔重構，將覆蓋本票成果。**本票 MUST 先行**；REVB APP 部署票開立時 MUST 於其 AI 明載「本節（冊面收束一致性閘）MUST 逐字保留並併入重構後版本」，違者即回退本票交付

## 檔案治理／來源說明

**缺陷字面（APP 端，2026-09-02 live 取證 `aud_20260902_4b494ca91d`）**：`compact/SKILL.md` 之 `## Auto Commit: Governance Artifacts` 段規定「Governance Artifact 清單 MUST 以 `ssot-governance.md` 表為單一真相來源，逐檔迴圈提交」，並具備 Scope 汙染確認、零 diff SKIPPED、`⛔ BLOCKED` 停止全部 commit 之完整紀律——**但全段只管「檔案有無 diff」，不管「冊面狀態對不對」**。當 session 完成一票（票檔搬 `deliver/`、PROGRESS.md 標 CLOSED）卻未呼叫 `registry_set_status` 時，`TICKET_REGISTRY.json` 零 diff → 判 SKIPPED → 全流程「成功」結束，缺口靜默通過。與 PLT 缺口同型。

**APP 端形狀差異（不可照抄 PLT 錨點）**：PLT 版插於「步驟 2 分類後、步驟 3 提交前」並於 `📌` 狀態行首位加 `⓪` 欄；APP 無編號步驟鏈，故本票改採**獨立小節**置於 `## Auto Commit: Governance Artifacts` 之前，狀態行則沿用 APP 既有 `📌` 行慣例另起一行，不動既有逐檔行格式。

**觸發訊號亦須適配**：APP 之 `Pending/` 票檔為 untracked，搬入 `deliver/` 之首次 commit 才進版控（`INFRA-TRACKSCOPE-01` 凍結點語義，`ssot-governance.md` L67）——故「票檔出現於 `git status` 之 `deliver/` 路徑」即為可靠觸發訊號，語義比 PLT 版更強。

**設計取態（沿用 PLT）**：本閘**只偵測不代寫**。冊面寫入序列有嚴格不變量（`set_status` → `append_note` → `set_checkpoint`，且 `set_status` 會 null 掉 checkpoint），塞進 auto-commit 鏈風險大於收益；偵測到不一致即停止並回報，由 host session 依正規原語補齊後重跑 `/compact`。

## Verify Config
TICKET_ID=INFRA-COMPACT-CLOSURE-GATE-APP-01
CREATED=2026-09-02
PRESET=docs
ALLOWED=.claude/skills/compact/SKILL.md
FORBIDDEN=.claude/rules/**,.claude/hooks/**,.claude/settings.json,.claude/CLAUDE.md,.claude/agents/**,.claude/skills/status/**,plans/cso_briefing/project/TICKET_REGISTRY.json,plans/cso_briefing/project/TICKET_REGISTRY_ARCHIVE.json,PROGRESS.md,app/**
MAX_LINES=35
DIFF_REF=<開工時 `git rev-parse --short HEAD` 回填>
BASE_COMMIT=<同上>
TARGET_BLOB=N/A（docs preset 單檔條文增補，V1 降級為 AI-0 錨點制）
ANCHOR_PROBED=2026-09-02 17:10

**MAX_LINES 推導註記**：AI-1 新節條文 ~22 行 ＋ AI-2 狀態行 ~2 行 ≈ 24 → ×1.4 → 34 → 進位 **35**（估算校準遙測，非判準）。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/skills/compact/SKILL.md`
- 三錨點逐字確認唯一命中：①`## Auto Commit: Governance Artifacts`；②該節內 `1. **Scope 汙染確認**`；③`📌 Auto Commit: [hash] [Prefix] <檔案>` 所在之輸出格式行
- **對照確認（負向）**：`收束閘`／`Closure Gate` 字面命中＝0（證缺口仍在，非已被他票封口）
- **任一不符 → 依 TPL v3.10 基線漂移三分流分類回報，禁單一 STOP 無分類**（Rev C／F4：引註版本由 v3.8 更正為現行 v3.10）
- **〔Rev C 新增，Rev D 校正引用範圍〕法源時效復驗**：MUST 逐條查驗下列四處來源仍在盤且未被推翻——①「Pending/ 淨空原則」§1 四值；②「Ticket 生命週期流程（五段）」旁支條（`BLOCKED`／`VOID`）；③「物理歸位邊界」（`REVIEW`→`deliver/`、`CLOSED` 保持）；④「VOID 歸位紀律」。任一不在盤或已被他票推翻 → 停止回報，禁依票面枚舉快照續作。
- **〔Rev D 新增〕CSO 冊面狀態來源缺失處置**：`PENDING`／`BLOCKED`／`VOID` 任一於 APP SSOT 找不到合法來源與目錄歸屬 → 停止回報 `CSO_STATE_MAPPING_SOURCE_MISSING`
- **〔Rev D 新增，evidence only〕** 查驗平台 `registry_service.py` `VALID_LIFECYCLE` 是否含 `DRAFT_NEEDED`，結果寫入收尾 evidence。**本項為取證，非判定輸入**——含與不含皆走 `UNKNOWN_LIFECYCLE_MAPPING`，執行端 MUST NOT 據其結果改變任何分支行為
- **[標配]** `git check-ignore -v .claude/skills/compact/SKILL.md` 命中 → 停止回報
- [x] 完成 — pre-image `0ed0676a`／`BASE_COMMIT=416c711`；三錨點各命中1（`## Auto Commit: Governance Artifacts` L88＋17／`1. **Scope 汙染確認**` L92＋17／`📌 Auto Commit:` L99＋17，行號為插入前基準）；負向確認：`收束閘`／`Closure Gate` 命中＝0（開工前）；法源復驗四項於 `.claude/rules/ssot-governance.md` 全數在盤（`Pending/ 淨空原則`§1 L65、旁支條 L62、物理歸位邊界 L88、`VOID 歸位紀律` L121）；CSO 冊面狀態來源缺失檢查：PENDING/BLOCKED/VOID 皆有來源，未觸發 `CSO_STATE_MAPPING_SOURCE_MISSING`；PLT `registry_service.py` L30 `VALID_LIFECYCLE` 實測**含** `DRAFT_NEEDED`（evidence only，未改變分支行為，APP 端仍走 `UNKNOWN_LIFECYCLE_MAPPING`）；check-ignore 未命中

### AI-1: 新增「## 冊面收束一致性閘」節（置於 `## Auto Commit: Governance Artifacts` 之前）
- **檔案**: 同上
- **現值**: `## 最後 Housekeeping` 之後直接進 `## Auto Commit: Governance Artifacts`，無任何冊面狀態檢核
- **目標值**: 新增小節，**MUST** 在 Auto Commit 全部步驟之前執行，含以下三條機械判準（**禁語義化裁量**）：
  1. **觸發偵測**：`git status --short` 輸出中存在 `plans/cso_briefing/project/deliver/<票號>*.md`（票檔進交付區），**或** `PROGRESS.md` diff 含該票號之 CLOSED 字面 → 該票號列入待驗清單
  2. **一致性驗證〔Rev B：目錄—狀態明確集合，兩級判定〕**：對待驗清單每一票號，以 `registry_list(project="SmartCameraApp1", ticket_id=<票號>)` 讀取冊面**現值**，依觸發訊號分級檢核：
     - **級 A（票檔進 `deliver/`）** → `lifecycle` ∈ {`REVIEW`, `CLOSED`}（`REVIEW` 為合法交付態，PLT 五段④；`VOID` 不屬 `deliver/`，其歸位為 `void/`）
     - **級 B（`PROGRESS.md` diff 標該票 CLOSED）** → `lifecycle` = `CLOSED`
     - **共同**：`file` 欄指向現存路徑（非 `Pending/` 死指標）
     - **目錄—狀態明確集合**（判定之唯一依據，禁語義裁量）：`Pending/` ↔ {`PENDING`,`TICKET_RIGOR`,`IN_PROGRESS`,`EXECUTED`,`BLOCKED`}；`deliver/` ↔ {`REVIEW`,`CLOSED`}；`void/` ↔ {`VOID`}。**〔Rev D／F7 修正——逐集合分別指認來源，禁以單一子節作全部集合之 SSOT〕**：
       - `Pending/` 集合：`PENDING`／`TICKET_RIGOR`／`IN_PROGRESS`／`EXECUTED` 四值源自 `.claude/rules/ssot-governance.md`「**Pending/ 淨空原則**」§1（明文「**MUST NOT** 窄寫為『僅 PENDING』」）；`BLOCKED` 源自同檔「Ticket 生命週期流程（五段）」之**旁支**條（「`BLOCKED` 可自任意活態進出」）＋淨空原則 §3（暫時態，禁無限期停留）。
       - `deliver/` 集合：源自「**物理歸位邊界**」（Akira 2026-09-07）——`REVIEW` 即搬 `deliver/`、`CLOSED` 保持於 `deliver/`。
       - `void/` 集合：源自同旁支條（`VOID` 為終態）＋「**VOID 歸位紀律**」（票檔移 `void/`）。
       - 「**五段觸發與寫入者**」節之性質為「**誰在何時寫入**」，**MUST NOT** 被引為目錄歸屬之來源；該節僅列五值，不含 `PENDING`／`BLOCKED`／`VOID`。
       - 平台 `registry_service.py` `VALID_LIFECYCLE` 為 lifecycle **合法值**之上游檢核來源，**不取代** APP 端之目錄歸屬定義。
       - **未知值 fail-closed〔Rev D／F7 收緊〕**：`DRAFT_NEEDED` **不屬本表**（APP SSOT 全檔零命中，`aud_20260909_28fa7fe321`）。`registry_list` 回傳任何未列入本表之 lifecycle 值（含 `DRAFT_NEEDED`）→ 回報 `⛔ Closure Gate UNKNOWN_LIFECYCLE_MAPPING @<票號>: lifecycle=<實值> / file=<實值>` 並停止全部後續 Auto Commit；**MUST NOT** 推定映射至任一目錄。
       - 本表為引用快照；任一來源條款與票面不一致、被更新裁決推翻，或必要來源不在盤 → 停止回報，禁依舊快照續作。〔Rev C／F2：原「本票開工時若該節尚未落地，以 PLT `ssot-governance.md` 五段節為準」之條件分支**刪除**——`INFRA-LIFECYCLE-SSOT-01` 已交付 `deliver/`，該節已落地，分支前提消滅，留置即死條款〕**MUST NOT** 使用 `get_ticket`——該工具查的是執行審批票 SQLite（另一套資料），對治理冊面票恆回 `NOT_FOUND`，誤用即把「查錯資料庫」讀成「該票未入冊」（`ssot-governance.md` L58-59 機械判準與誤用簽名）
  3. **不一致處置**：任一項不符 → **MUST** 回報 `⛔ Closure Gate FAILED @<票號>: lifecycle=<實值> / file=<實值>` 並**停止全部後續 Auto Commit 步驟**（比照既有 `⛔ Auto Commit BLOCKED` 之停止語義）；**MUST NOT** 代呼叫任何 registry 寫入原語；由 host session 以正規原語補齊後重跑 `/compact`
     - **〔Rev C／F3〕批次語義**：待驗清單含多票號時，**逐票獨立判定**，任一票 FAILED 即整批阻擋；處置順序不影響判定結果，故**不另立排序機制**。回報行 MUST 列出全部 FAILED 票號（非僅首個），使 host session 一次補齊
  - 待驗清單為空 → 該節 SKIPPED，**MUST NOT** 阻斷（無票收束之純交接 session 不受影響）
- [x] 完成 — 新節已插入 `## Auto Commit: Governance Artifacts` 之前（`.claude/skills/compact/SKILL.md` L88起），三條判準逐字齊備，含 `registry_list`＋`get_ticket` 禁用字面（L105），逐集合分別標示來源（L99/L100/L101），`DRAFT_NEEDED` fail-closed 明載（L104）

### AI-2: 狀態行增列
- **檔案**: 同上，`📌` 輸出格式段
- **動作**: 於逐檔 Auto Commit 狀態行**之前**增列一行格式定義：`📌 收束閘: [PASS|SKIPPED|⛔FAILED@票號]`；既有逐檔行三種格式（hash／SKIPPED／BLOCKED-FAILED）**逐字不動**
- [x] 完成 — 狀態行格式已插入（compact/SKILL.md 確認訊息段），既有 `📌 Auto Commit:` 三種格式行逐字未動（`git diff` 該行純新增，無修改既有行）

## Hard Constraints
1. 本閘**只偵測不代寫**——禁在 skill 內加入任何 registry 寫入原語呼叫（`registry_set_status`／`append_note`／`set_checkpoint` 一律禁）
2. 待驗清單為空時 **MUST NOT** 阻斷
3. 禁動 `## 最後 Housekeeping` 之 PowerShell 區塊、`## 輸出格式` 之 Handoff 模板、`## Auto Commit` 既有兩步判定與逐檔行格式
4. 判準禁語義化：以「路徑模式命中」與「冊面欄位實值比對」直取，不得以「看起來已收束」等敘述性判斷替代
5. **分類固定、驗證分層，禁個別 lifecycle 特例**〔Rev D／F8 改寫；承 Rev C 甲案〕：
   - **目錄歸屬分類恰有三類**：`Pending/`／`deliver/`／`void/`；每一類**只以其對應集合之成員關係**判定。
   - **級 A**（票檔進 `deliver/`）與**級 B**（`PROGRESS.md` diff 標 CLOSED）為觸發訊號下之**驗證規則**，**不是**新增目錄分類；`file` 欄檢核、`SKIPPED`／`FAILED`／`UNKNOWN` 處置亦**不計入**分類分支數。
   - **MUST NOT** 因任一單一 lifecycle 值（如 `TICKET_RIGOR`／`IN_PROGRESS`／`EXECUTED`／`BLOCKED`／`DRAFT_NEEDED`）新增專屬判定分支、例外、轉向或例外處置。
   - lifecycle 值僅得作其所屬集合之成員比對；未列值一律走 `UNKNOWN_LIFECYCLE_MAPPING` 停止處置，**不得回退為個別值例外**。
   - **集合枚舉字面之出現屬「判準表資料」，不受本條拘束。**
   - 原條文「禁引入 PLT 五態字面……（`ssot-governance.md` L81 治理減量裁決）」**作廢**：其法源之 2026-08-27 四態裁減已於 2026-09-06 經 `INFRA-LIFECYCLE-SSOT-01` 推翻，APP 現行 SSOT 為五段語義
6. 禁改 `frontmatter` 之 `name`／`description`／`disable-model-invocation`

## Error Handling
- AI-0 任一錨點不符 → 依三分流分類回報
- 需改 `.claude/rules/**` 或 hooks 方能達成 → 停止回報升裁（違 FORBIDDEN）
- `registry_list` 之 `ticket_id` 參數不可用（回傳全冊而非單票）→ 停止回報：表示 MCP server 未重啟或版本落後於 PLT `INFRA-REGISTRY-API-GAPS-01`，本票判準之前提不成立
- **[標配]** `git check-ignore -v <標的>` 命中 → 停止回報

## Acceptance Criteria
- [x] **[存在性]** SKILL.md 含「冊面收束一致性閘」節，三條判準字面齊備（觸發偵測／一致性驗證／不一致處置），且位於 `## Auto Commit: Governance Artifacts` 之前
- [x] **[存在性]** 條文含 `registry_list` 且含 `MUST NOT` 與 `get_ticket` 之禁用字面
- [x] **[存在性]〔Rev D／F7 取代 Rev C 同位 AC〕** 目錄—狀態集合**逐集合分別標示來源**且與該來源逐字一致：`Pending/` 四值 → 「Pending/ 淨空原則」§1；`BLOCKED` → 旁支條＋§3；`deliver/` → 「物理歸位邊界」；`void/` → 旁支條＋「VOID 歸位紀律」。條文**MUST NOT** 宣稱所有值皆與「五段觸發與寫入者」節逐字一致（該節僅五值，不含 `PENDING`／`BLOCKED`／`VOID`）
- [x] **[存在性]〔Rev D／F7 收緊〕** 條文明載 `DRAFT_NEEDED` 不屬目錄—狀態集合；任何未列入三集合之 lifecycle 值均走 `UNKNOWN_LIFECYCLE_MAPPING` 並停止 Auto Commit。收尾 MUST 附平台 `VALID_LIFECYCLE` 之核實結果，並註明該結果為 evidence、不替代 APP 目錄歸屬來源 —— 實測：PLT `core/services/registry_service.py` L30 `VALID_LIFECYCLE = {"DRAFT_NEEDED","PENDING","TICKET_RIGOR","IN_PROGRESS","EXECUTED","REVIEW","CLOSED","BLOCKED","VOID"}`，**含** `DRAFT_NEEDED`；此結果僅為上游合法值核實（evidence），不改變 APP 端目錄歸屬判定（仍走 `UNKNOWN_LIFECYCLE_MAPPING`）
- [x] **[行為性]〔Rev D／F8 取代 Rev C 同位 AC——補計數單位定義〕** given 產出條文，expect **目錄歸屬分類恰為三類**：`Pending/`／`deliver/`／`void/`。驗收輸出 MUST 列出三類及其集合，並逐項標記「**不計入分類數**」：級 A、級 B、`PROGRESS.md` 觸發、`file` 欄檢核、`SKIPPED`、`FAILED`、`UNKNOWN` 處置 —— 三類集合：`Pending/`↔{PENDING,TICKET_RIGOR,IN_PROGRESS,EXECUTED,BLOCKED}、`deliver/`↔{REVIEW,CLOSED}、`void/`↔{VOID}（compact/SKILL.md L99-101）；級A／級B／PROGRESS.md觸發／file欄檢核／SKIPPED／FAILED／UNKNOWN 七項於 L111 明文標記為「驗證規則而非目錄分類，不計入分類分支數」
- [x] **[行為性]〔Rev D／HC5 落地〕** given `lifecycle=IN_PROGRESS` 或 `BLOCKED`，expect 僅作 `Pending/` 集合成員比對、**無專屬例外分支**；given `lifecycle=DRAFT_NEEDED` 或其他未列值，expect `UNKNOWN_LIFECYCLE_MAPPING`＋停止 Auto Commit、**無推定目錄歸屬**。收尾 MUST 逐分支列出條文行號 —— `IN_PROGRESS`／`BLOCKED`：compact/SKILL.md L99（同屬 `Pending/` 集合成員比對，無專屬分支）＋L111（明禁單值專屬分支）；`DRAFT_NEEDED`／未列值：L104（`UNKNOWN_LIFECYCLE_MAPPING`＋停止＋禁推定歸屬）＋L111（禁回退個別值例外）
- [x] **[存在性]〔Rev D／F6' 更正〕** 本票 Rev 歷史明載：Rev C 加入者為 AC 之執行端實跑自證，**非** `coordination_dependency` 之 mechanical assignee 修正；不得保留「F6 已補機械 assignee」之不實摘要 —— 已於票面 Rev D（L8）明載此更正，Rev C 段（L20）保留原誤記文字供對照，未被靜默抹除
- [x] **[存在性]** `📌 收束閘:` 狀態行格式已定義，既有逐檔 Auto Commit 行格式逐字未變
- [x] **[行為性]** 以 PLT 之 TKR-13 歷史情境作紙上推演並記錄於收尾：若當時本閘存在於 APP 等價流程，`deliver/TKR-13_*.md` 命中觸發偵測 → 冊面實值 `lifecycle=PENDING` → 判 FAILED 阻擋——證本閘可攔下該類事件（因果鏈取證，非僅條文存在） —— 紙上推演：TKR-13（commit `b334b93`，PLT 端 Dashboard 標 CLOSED 但冊面 lifecycle 停留 `PENDING`）若發生於 APP：`git status --short` 命中 `deliver/TKR-13_*.md`（觸發偵測①成立）→ `registry_list` 讀得 `lifecycle=PENDING` → 級A判定 `PENDING ∉ {REVIEW,CLOSED}` → `⛔ Closure Gate FAILED @TKR-13: lifecycle=PENDING / file=deliver/...` → 停止全部 Auto Commit。證本閘可攔下該類「Dashboard 已收但冊面未轉態」事件
- [x] **[行為性]〔Rev B 改寫〕** 以 APP 冊現況作**正向**空轉驗證並記錄：對現存 lifecycle=`REVIEW` 且票檔在 `deliver/` 之票（如 seq 30／seq 34），本閘級 A 判 **PASS**——`REVIEW` 為合法交付態，誤報即回歸（防過度攔截，與上一項互為守門）。**〔Rev C／F6：assignee＝執行端，於收尾自證〕** MUST 附實跑輸出（票號、`lifecycle` 實值、`file` 實值、判定結果四欄逐票列出），禁以「已確認不誤報」之敘述代替輸出 —— 實跑輸出（`registry_list` 2026-09-09 實測）：<br>`INFRA-SESSION-HOOK-01` \| `REVIEW` \| `deliver/INFRA-SESSION-HOOK-01_ctx-inject.md` \| 級A PASS<br>`INFRA-PENDING-HYGIENE-APP-01` \| `CLOSED`（原REVIEW，另一session已續轉CLOSED） \| `deliver/...` \| 級A PASS<br>`INFRA-LIFECYCLE-SSOT-01` \| `CLOSED`（原REVIEW，另一session已續轉CLOSED） \| `deliver/...` \| 級A PASS
- [x] **[行為性]〔Rev B 新增〕** 對一張 lifecycle=`CLOSED` 且票檔在 `deliver/` 之票，級 A 與級 B 皆 PASS；對人造情境「票檔在 `deliver/` 而 lifecycle=`PENDING`」級 A 判 FAILED——三案輸出存收尾 —— 三案實測：①`INFRA-TPL-V310-01` \| `CLOSED` \| `deliver/INFRA-TPL-V310-01_extrev.md` \| 級A PASS（PROGRESS.md Dashboard 亦標 CLOSED，級B PASS）；②人造情境（純邏輯推演，未實際搬檔）：取真實 PENDING 票 `INFRA-SV-GOV-01`（現實際位於 `Pending/`），假設性代入 `file=deliver/INFRA-SV-GOV-01_SV_Governance_Fix.md`、`lifecycle=PENDING`（registry 實值不變）→ 級A判定 `PENDING ∉ {REVIEW,CLOSED}` → FAILED
- [x] ~~`assembleDebug` PASS~~ (N/A: docs preset，純 skill 文本，無編譯產物)
- [x] `git diff --stat` 僅涉及 `.claude/skills/compact/SKILL.md` —— 本票 commit 僅 stage/commit 該檔；session 開頭快照另有 `test_preflight_adapter.sh` 一筆非本票 dirty 檔（`INFRA-PREFLIGHT-CORE-01-FIX-B` 前次工作殘留），依 Akira 2026-09-08 裁決（非 ALLOWED 之 dirty 檔不整批 BLOCKED）不予處理、不納入本票 commit

---

**End of INFRA-COMPACT-CLOSURE-GATE-APP-01**

---

## 收尾實錄（耐久載體＝本票檔；依四要件④，轉終態前落檔）

**碼閉環（2026-09-09）**：AI-0～AI-2 全數完成，AC 13 項全勾（票檔 `- [ ]` 命中＝0）。

**Commits（SMTR 一手 `git_log` 驗證）**：`d85452e`（16:04:21，`[INFRA-COMPACT-CLOSURE-GATE-APP-01] 新增冊面收束一致性閘＋收束閘狀態行`，`.claude/skills/compact/SKILL.md` +26L 僅此檔，`aud_20260909_d12d6471ed`）＋`0e56ea0`（16:11:29，`[GOVERNANCE-SYNC]` 票檔搬 `deliver/`，`aud_20260909_d12bc215ce`）。後者即本票檔之凍結點 commit。

**審查鏈**：`/qa` PASS（Build／Test／Lint／ELF 全綠；Step 3.5 之 5 項既有 baseline 發現與本票無關）→ `/sv` Cross-Review R1 `REPORT_FIX`（`sv_report.md` §7 Guard Reachability 格式）→ R2 **PASS**。

**Ticket Rigor Gate 三輪**：Round 1 Critical F1（HC5／AC-2 法源 `ssot-governance.md` L81 已被同檔推翻）→ Round 2 Critical F7（Rev C 新增之 AC 不可能滿足，成因為 SMTR Rev C 自身）→ **Round 3 PASS**（報告 `.tmp/rigor-gate/RGR_INFRA-COMPACT-CLOSURE-GATE-APP-01_20260909_R3.md`）。Rev D 之根治：AI-1 改為逐集合分別溯源（`Pending/` 四值→淨空原則 §1；`BLOCKED`→旁支條＋§3；`deliver/`→物理歸位邊界；`void/`→旁支條＋VOID 歸位紀律），明文排除「五段觸發與寫入者」節作為目錄歸屬來源，`DRAFT_NEEDED` 明文排除，未列值 `UNKNOWN_LIFECYCLE_MAPPING` fail-closed。SMTR 一手逐條核對 `ssot-governance.md` 原文全數字面相符。2 輪 FAIL 計數於 Round 3 PASS 中止，未觸發 3 輪凍結條款。

### Traycer 裁決：`N_A`（2026-09-09，D-1 甲案）

票面 L7 開票時即宣告 `**Traycer Workflow**: N/A（單一 skill .md 條文增補，§0 五分鐘 git restore 域）`，該宣告為法源；冊面原 `PENDING` 係無實跑 receipt 支撐之殘留值，經 Akira D-1 甲案裁決回填 `N_A`。**N_A 意為結構上不適用 Traycer，非推定 PASS**（撤警正向取證律）。`closure_record` 內「`traycer:PENDING`」之歷史字面保留不刪。

### Extreview 4a 狀態（收票時）

本票 Rev A→B→C→D 計 4，4a 於 Rev C 首次達 3 即成立，兩輪 rigor gate 事實上構成外審回流；Akira 2026-09-09 存廢三案裁決為**續修**（不退役、不降級），依 O6 遲滯律該裁決落檔後 **4a 計數重置為 0**。Rev D 授權範圍僅既有 AI-0～AI-2；票級 closure 前之新修補票凍結，隨本票轉 CLOSED 而失效（凍結標的為「closure 前」）。

### 收票時仍在盤之非阻擋項（Round 3 報告所列，不另開票）

- **Medium F10**：`MAX_LINES=35` 未依 Rev D 內容膨脹重新校準（實際 `/qa` PASS，未成災）
- **Minor F9**：AC 第 124 行遺留 Rev C 舊標籤未清理
- **Minor F11**：`void/` 集合引註未取最直接來源，非錯誤僅風格不對稱
- Step 10 `PROGRESS_ARCHIVE.md` Done 標記因票面 `FORBIDDEN` 命中 `PROGRESS.md` 而延後至下輪 `/compact`（沿用 DEFERRED-TO-DOCSYNC 慣例，依 FORBIDDEN 延後項免開票紀律不另開票）

### 下游影響

`coordination_dependency` `INFRA-COMPACT-REVB-APP-01`（seq 32）之前行阻擋已於本票達 `REVIEW` 時解除；該票開立／派工時 **MUST** 於其 AI 明載「本節（`## 冊面收束一致性閘`）MUST 逐字保留並併入重構後版本」，否則整檔重構將覆蓋本票成果。
