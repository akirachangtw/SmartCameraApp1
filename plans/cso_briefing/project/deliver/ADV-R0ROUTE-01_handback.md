# ADV-R0ROUTE-01: 落實 R0 回件承接契約——ACTIONABLE 由 executor 承接修驗並重跑 R0、UNDECIDABLE 路由真實 Advisor、AUTHORITY 經 Advisor 確認後才升報；同批修正 file-class 硬停（D5=乙）、R0 職責 4(c) 自我提及，並執行真實鏈驗收

**Revision**: Rev A（2026-10-15；首次開票，Rev=0）。

**檔案治理/來源說明**：本票為 Plan-backed 單票（plan-local item `T02`，含原 T03）。Parent 類型＝Plan-backed；Parent 絕對路徑＝`D:\DevSources\AndroidProj\SmartCameraApp1\.tmp\ticket-author\20261014-layered-solve-plan.md`（Rev B，2026-10-14；`.tmp` 非長期載體，下列內容使本票獨立成立）；Parent artifact status＝`Fixture: false`／`Plan Gate: PASS`／`Ticket mode eligibility: ALLOWED`；plan-local item＝`T02`；Parent read timestamp＝2026-10-15 22:2x（+08:00，全文實讀）。Template 實讀：`D:\DevSources\AndroidProj\SmartCameraApp1\plans\cso_briefing\project\file\Unified_Ticket_Template_v3.md`，版本 v3.11，文件產出日期 2026-05-29，最近有效修訂日 2026-09-14（Revision 歷史末筆 v3.11）。

**Selected item 原文（T02 goal，摘要不改義）**：R0 回件由主 agent 依分類走 Gate Router——`ACTIONABLE`（ALLOWED 內可修，附修法）→ executor 修復並重跑 R0；`UNDECIDABLE`（判準衝突或無法判定）→ 真實 Advisor；`AUTHORITY`（未授權的 flag 預設值／DI 綁定變更，或 file-class 命中但不具「ALLOWED＋行為授權」者）→ Advisor 確認後 `HUMAN_REQUIRED(SCOPE)`；不再「R0 FAIL 直入人工 Checkpoint」。併入：D5=乙（`rules/cross-review.md` file-class 立即 Checkpoint 改為「ALLOWED 且行為已授權者走 Gate」）、原 T03（R0 職責 4(c) 區分「禁令文字自我提及」與「實作型別使用」）、parent §5 真實鏈驗收（[real] 部分）。

**相關約束（取自 parent，Akira 2026-10-14 裁決，T02 適用者）**：
- **D5=乙（Rev B）**：file-class 命中時，票面 `ALLOWED` 已列該檔且該行為已被 Action Item／AC 明確授權者走 Gate 續作；**`ALLOWED` 不是空白行為授權**，未授權者仍升報。
- **D2**：ADV-REVIEW-01 已 CLOSED，不重開、不回塞；**D3**：不新增平行 policy／framework／daemon／MCP／DB／finding registry／coordinator；重用 ledger 既有事件與承載點。
- **D4/D6/D7**：預算層級＝round 的策略來源層 `executor｜r0｜advisor`；R0 不是 Advisor，主 agent 自判不得記 advisor consult；某層耗盡必升下一適用層；同 incident 首個 `--layer r0／advisor` round 前須有同 incident 對應 `consult` 紀錄（機械僅證存在；真實性依 transcript 原生 `advisor` 調用，不新增解析機制）。**D8**：同 incident 同一外部事實缺口預設研究一次，不重送同題；**D9**：T03 併入本票，T01 已由 ADV-LAYER-01 承接並 CLOSED，**其限定交付邊界（僅 [stub]）保留，不提升為完整真實鏈驗證**。
- **驗收邊界（parent §5）**：[stub]（決定性）與 [real]（真實工具）分開記載；**hook 鏈需 Registry lifecycle，仍屬 [stub]，不得宣稱 hook 真實鏈已驗**；headless／subagent executor 無 Advisor，不在 [real] 範圍；不足者標 `UNVERIFIED`。

**證據分級**（2026-10-15 22:1x–22:3x live 直讀；HEAD `c32cc414`，開票時工作樹 clean）：
- **CONFIRMED（本地原始碼／工具輸出）**：①R0 回件無腳本節點——`cross_review.sh` 單次產 payload 並 `codex exec`，R0 只靠 `rules/cross-review.md` R0 路由條（現 L49）由主 agent 委派；producer＝`cross-reviewer` subagent（`.claude/agents/cross-reviewer.md`，`tools: Read, Grep, Glob`，非 Advisor）；唯一承接者＝主 agent；下一步唯一文字依據＝R0 路由條原句「R0 回報 FAIL / CHECKPOINT-REQUIRED → MUST NOT 送 Codex，直接進修正或 Checkpoint 裁決流程」（唯一命中）。②`rules/cross-review.md` 現 L13 file-class bullet（`PROGRESS.md / sv_report*.md / ssot_context.md / CLAUDE.md / SSOT*.md / *_Contract*.md`）無「ALLOWED＋行為授權」分支；現 L14（flag／DI bullet）已有「票面明確授權者走 Gate」分支——兩 bullet 結構不對稱，D5=乙 即補齊 L13。③`cross-reviewer.md` 輸出格式僅「裁決（FORWARD／FAIL／CHECKPOINT-REQUIRED）＋分級表（嚴重度／檔案:行號／問題／建議）」，無分類欄；職責 1 對 file-class 一律 `CHECKPOINT-REQUIRED`，票面授權判讀僅針對 flag／DI 關鍵詞 hunk，且以 `tmp/verify_cmd.conf`（cwd 相對）定位票檔——**跨 worktree 重放時 cwd 的 conf 不是 fixture 票**（本票 AI-2 補「委派 prompt 明示票號／票檔路徑優先」）；職責 4(c) 對零容忍型別名一律 `BLOCKER`，無自我提及分支。④`cross_review.sh` 步驟級預算／LIMIT 相關人工措辭現有 **6 處**：L14（頭註）、L51（`fail_budget()`）、L173（`CHECKPOINT_REPORT_FIX_LIMIT` 說明）、L182（`REPORT_FIX_ESCALATE` 說明）、L498、L501（parent 僅列 L498／L501；現值漂移見下）；`CROSS_REVIEW.md` L66、L81；`sv/SKILL.md` L157–160 已改為「步驟級事件→Gate」，不在本票範圍。⑤`gate_ledger.py` 現役已含 `LAYERS=("executor","r0","advisor")`、`round start --layer`、`consult --kind local|advisor|r0`、首個 r0／advisor 層 round 前需同 incident 對應 consult（現 L614–619）、`EXHAUSTED` basis `layer_advisor_3`（ADV-LAYER-01 CLOSED，限定交付 [stub]）——本票之 r0 層承接**直接重用**，不改 helper／hook。⑥無重複票：全冊（`TICKET_REGISTRY.json`、`_ARCHIVE.json`、`Pending/`、`deliver/`、`void/`、`reports/`）`R0ROUTE` 字面 0 命中；`ADV-` family 已登記（`ticket_pattern.md` Family Provenance `ADV-`）；`Advisor` 議題冊面 10 筆全 `CLOSED`，無等價結果之票（`ADV-CR-R0-PRECLEAR-01` 系列於 `void/`，範圍為 R0 預審前置而非回件承接）。⑦鏡像：`.codex/agents/cross-reviewer.toml`（`.gitignore:70` 命中）與 `.agents/skills/sv/**`（`.gitignore:69` 命中）皆**不入版控**且已與 `.claude` 版本分歧（`.agents` 版 `cross_review.sh` 與 `.claude` 版 diff 210 行；`.codex` toml 仍為 `AGENTS.md`＋舊 token 清單）；`.agents/` 下**不存在** `cross-reviewer.md`——parent 所稱「`.agents/` 鏡像」於 live 實為上述兩個未版控副本，**不同步、不納 ALLOWED**。
- **SUPPORTED**：官方文件（`code.claude.com/docs/en/sub-agents`，PPSS snippet，頁面標示 last_updated 2026-10-15）載明 Claude Code 監看 `.claude/agents/`，編輯後數秒內偵測，**下一次委派即使用新定義，無需重啟**（例外：新建 `agents` 目錄之首個檔、`--add-dir`、`--disable-slash-commands`）；`claude.com` 文章載明 subagent 之 name／description／tool list 於 session start 載入、body 於呼叫時載入。**與本地單次觀察衝突**：ADV-REVIEW-01 R0（2026-10-14 14:18）回件為舊版職責 1。衝突不裁決、不當作永久規律——本票將「R0 實際載入之定義版本」列為**每次重放須自行量測**的事項（見 AI-5），不假設新舊。
- **HYPOTHESIS**：新定義下 R0 於兩筆 fixture 之實際回件符合「預期路由」（本票 AI-5 之 bounded probe；N=1／同一模型／單次取樣，不宣稱一般可靠）。
- **UNKNOWN（有 AI-0／AI-5 probe）**：重放時 R0 subagent 實際載入新或舊定義；PPSS 於 `data/research/` 之 artifact 屆 AI-6 時是否仍在（retention）。
- **已知限制（誠實揭露）**：本票修訂 `cross-reviewer.md` 與 `rules/cross-review.md`，**本票自身 R0 可能以舊定義命中 4(c)**（修訂處緊鄰既有 (c) 行，diff context 含該行）——此為 fixture 1 同型，處置見 Error Handling 與「必要未決事項」，**不預設任何預授權**。

**External-review decision**（條文：`D:\DevSources\Smarter-Project\file\Instructions_for_Claude.md` v3.4 §6〔L43–L50〕與「分流順序與發送通道」節，2026-10-15 live 直讀；分項記錄，不以整節 WAIVED 掩蓋已成立的觸發）：
- **6a（成立；外審 EXECUTED）**：`INTRODUCED_CAPABILITY=feature:r0-handback-routing`≠`NONE`。corpus 核對（`Handoff/`、`Pending/` `deliver/` `void/`、`TICKET_REGISTRY.json`、`_ARCHIVE.json`、vault `D:\DevSources\Smarter-Project`，開票前）：`r0-handback-routing` 字面 **0 命中**（本票自身引用不計）→ 成立。處置＝PPSS 取證（Claude 自行呼叫 `research_submit`，非人工轉投）：`rsch-20261015-b560a4fac7`（2026-10-15 22:2x，3 條查詢，$0.005，snippet 級；artifact `D:\DevSources\workflow\mcp-ticket-platform\data\research\2026-10\rsch-20261015-b560a4fac7.json` 為 runtime 衍生物，**結論以本欄為準**）。**回件實況**：5 筆結果全部落在查詢 3（subagent 定義載入時機），查詢 1／2（R0 分類承接設計模式、僅 authority 缺口升人類之 anti-pattern 證據）**未回傳實質設計文獻**→ 該二題證據等級＝**UNKNOWN（無反證，亦無正證）**；不重送（防遞歸律）。設計面僅能沿用 `rsch-20261014-50d863d838`（ADV-LAYER-01，有界針對性恢復之一般方向 SUPPORTED；不驗證各層三次參數；無界重試為業界 anti-pattern、對策為硬上限＋升人類〔本票沿用逐層 3 次硬上限〕）。硬風險判準①–⑥：未命中（無跨子系統 hard dependency、無安全邊界／不可逆資料處理／外部成本新增面；PPSS 僅用既有單價）。**EXECUTED 不代表品質 PASS，不豁免 Cross-Review。**
- **bounded probe 狀態**：AI-5（兩筆 fixture 正反重放）與 AI-6（[real] 鏈 A–D）即本票 probe。**狀態：EXECUTED（部分 UNVERIFIED，2026-10-16）**——stub-equivalent 正反重放 6 組與 A／B／D 鏈、C 回流消費段已執行；真實 R0 重放（載入新定義）與 A 鏈「真實 R0」為 UNVERIFIED，詳見「收尾實錄」第 2、3、6 節。probe 結果只證各自標籤層級（[stub]／[real]），不代表品質 PASS。
- **其他觸發項**：6b①—`ALLOWED` 四檔首層 path segment 皆為 `.claude`，distinct＝1 <2，**不成立**（`.codex/`、`.agents/` 因不入版控且不同步而**不納 ALLOWED**；若開工後 ALLOWED 增跨首層路徑 MUST 重判並出查詢包，不靜默放行）；6b②—`### AI-[1-9][0-9]*:` 標題數＝7（AI-1…AI-7；AI-0 不計）<8，不成立；6b③—`DEPENDENCY_DELTA=NONE`，不成立；6c—四個標的檔檔頭無 `Revision` 欄修訂歷史，不納本款，不成立。若 AI 標題增至 ≥8 MUST 重判。計數讀取（O1）：`Rev=0 ｜ AI 標題數=7 ｜ AI 深度=1 ｜ 4a/4d/6b 狀態=未成立`。
- **Cross-Review**：**不豁免**；本欄任何狀態都不代表品質驗收 PASS。

**Fixture 耐久紀錄（開票時固化；`.tmp` 非版控，下列使兩筆重放來源在原始報告消失後仍可核對；不重貼完整 payload／整份歷史報告）**：

| 項目 | F1：ADV-REVIEW-01 R0（4(c) 自我提及） | F2：ADV-RESETISO-01 R0（D5=乙 正向案例） |
| --- | --- | --- |
| 來源識別 | `.tmp/fail-advisor/R0_ADV-REVIEW-01_20261014_141804.md`（4712 bytes，sha256 前 16 碼 `4ed5ca85b7652172`）；耐久承載＝`plans/cso_briefing/project/deliver/ADV-REVIEW-01_criteria.md` 收尾實錄 L322–L324（R0 事件／Akira 裁決 B／三類結果） | `.tmp/fail-advisor/R0_ADV-RESETISO-01_20261015_171028.md`（2446 bytes，sha256 前 16 碼 `0a3ff5b4b37c66d2`）；耐久承載＝`plans/cso_briefing/project/deliver/ADV-RESETISO-01_runtime.md` 收尾實錄 L167–L168（R0／Checkpoint 處置）；payload 副本 `tmp/ADV-RESETISO-01/payload_r0_1.md`（本機未版控） |
| diff 端點 | `d21e8f7259111743dc849a2c5bdd52804b289ca8`..`0cb6fd44830228ff061e375bebde1d0d635bbee5`（payload 56313 bytes，以 stub codex 僅生成；原 payload 與原 sv_report 未保存） | `6c541133`..`0475fbad`（3 個 `diff --git`：`.claude/CLAUDE.md`、`.claude/hooks/session_start.sh`、`session_start_owner_test.sh`；歸檔 sv_report：`.tmp/sv_report_20261015_170901.md`、`_171129.md`） |
| 原判定 | `FAIL`（含 CHECKPOINT-REQUIRED 列）；R0 載入**舊版**職責 1。BLOCKER 兩列（職責 4(c) 零容忍名稱字面命中）：`cross_review.sh:277`（BLOCKER 判準表 #8，payload L572）、`ssot_context.md:66`（payload L670）——**僅出現於禁令條文本身**，為票面 AI 逐字目標值；本 diff 無 .kt、無型別定義或使用處。另三列職責 1 token CHECKPOINT-REQUIRED（`cross-reviewer.md:13`、`rules/cross-review.md:14`、`cross_review.sh:275`），hunk 皆為規則／prompt 文字、無 flag 預設值／DI 綁定變更、票面授權。處置：主 agent 直接停車輸出人工 Checkpoint（**未完成必要 Gate 路由**）；Akira 2026-10-14 裁決 B，統一記 `TRUE-POSITIVE-RULE-GAP`（承接 ADV-REVIEW-01）後續送 Codex | `CHECKPOINT-REQUIRED`（職責 1：diff 觸碰 file-class 檔 `.claude/CLAUDE.md`；PROGRESS 條件豁免不含此檔）。R0 核對 hunk `@@ -4,7 +4,7 @@` 僅 1 刪 1 增（Boot 第 7 行移除 `rm -f tmp/verify_cmd.conf .tmp/sv_report.md tmp/handoff.md`、只留 `mkdir -p tmp`），與 D1 單行授權相符；K3-6 PASS；關鍵詞 0 命中；4a／4b／4c PASS。後續：主 agent 引用既有授權 Akira 2026-10-15 D1（見該票 AI-2／HC3／Error Handling）後送真實 Codex（ADVISORY）；`CLASSIFICATION: NORMAL` |
| 必要輸入片段（供重建 payload 與核對） | ①4(c) 兩列：禁令條文三個名稱出現於 `cross_review.sh` BLOCKER 表 #8 與 `ssot_context.md` §2 之條文行（`-`／`+` 行皆有）；②票面 Verify Config `ALLOWED` 七檔（含 `ssot_context.md`、`cross-reviewer.md`、`rules/cross-review.md`、`cross_review.sh`）與 AI-1／AI-5／AI-6 逐字目標值；③三列 token 列為規則文字 hunk | ①`.claude/CLAUDE.md` hunk 1 刪 1 增；②票面（`deliver/ADV-RESETISO-01_runtime.md`）Verify Config `ALLOWED` 含 `.claude/CLAUDE.md`，AI-2 與 HC3 明確授權 Boot 第 7 行單行修改（來源 Akira 2026-10-15 D1） |
| 預期路由（本票改後） | R0 對 4(c) 兩列**不判 BLOCKER**（分級表列「(c) 自我提及（禁令文字）」註記）；`ssot_context.md` 若被 R0 視為 file-class 命中（視大小寫比對而定）且 ALLOWED 已列＋AI-6 授權 → 註記並可 `FORWARD`；三列 token 列維持取證註記；整體 `FORWARD`（可送 Codex，非 PASS）。**負對照**（僅重放副本中構造）：於 domain `.kt` 增一行對零容忍名稱之型別宣告／引用 → 仍 `BLOCKER`（`ACTIONABLE`，附修法） | R0 對 `.claude/CLAUDE.md` 列**不標 CHECKPOINT-REQUIRED**：註記「file-class 命中：ALLOWED 已列、授權依據＝AI-2／HC3」→ `FORWARD`；主 agent 依 `cross-review.md` 續作（走 Gate，不停車）。**負對照**（僅重放副本中構造）：票檔副本移除 `.claude/CLAUDE.md` 於 `ALLOWED`（或移除授權該行為之 AI／HC）→ `AUTHORITY`＋`CHECKPOINT-REQUIRED`，主 agent 路由＝本地取證→真實 Advisor→`human --category SCOPE` |
| 實際重放結果（2026-10-16） | 真實 R0 兩輪皆舊格式（UNVERIFIED 新定義）；舊定義重現原判 `FAIL`（4(c)×2）。stub-equivalent：**FORWARD**，4(c) 兩列＝自我提及不計 BLOCKER，ssot_context file-class＝ALLOWED＋AI-6 授權註記；負對照 N1＝仍 BLOCKER（ACTIONABLE）。詳見收尾實錄第 2 節 | 真實 R0 兩輪皆舊格式（UNVERIFIED）；舊定義重現原判 `CHECKPOINT-REQUIRED`。stub-equivalent：**FORWARD**＋授權依據註記（AI-2＋HC3＋D1）；負對照 N2a／N2b＝`AUTHORITY`＋`CHECKPOINT-REQUIRED`；缺票上下文 U1＝`UNDECIDABLE`。詳見收尾實錄第 2 節 |

**必要未決事項（開票時；不阻擋開票，須 Akira 於派工前裁決其一）**：本票自身 R0 因舊定義載入或修訂處緊鄰既有 (c) 行，可能再現 fixture F1 同型攔截。選項——**A**：比照 ADV-REVIEW-01 HC12／ADV-LAYER-01 HC11，由 Akira 明示追認「僅限本票、僅限命中 hunk 為規則／prompt／文件文字且無 flag 預設值或 DI 綁定變更、無獨立 finding 或未授權變更者」記 `TRUE-POSITIVE-RULE-GAP`（承接 `ADV-R0ROUTE-01`）後續送 Codex，不停等同一情形；**B**：不追認，R0 若攔截則依 Error Handling 走 Gate（本地取證＋真實 Advisor），確認為規則缺口後以護欄二 `HUMAN_REQUIRED` 請 Akira override。建議 **A**（已兩度採同型預授權，且 AI-5 重放可補證其範圍）；**本票不自行預設 A**。

**簽發日期**: 2026-10-15
**簽發者**: Claude
**來源議題**: Advisor
**Mode**: uc  ||  **Effort**: High
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: ADV-LAYER-01（已 `CLOSED`，2026-10-14；提供現役 `--layer r0／advisor`、`consult --kind r0`、逐層上限；無未完成前置）
- `evidence_dependency`: ADV-REVIEW-01（收尾實錄 L322–L324：R0 fixture F1、三類結果、後續設計輸入）、ADV-RESETISO-01（收尾實錄 L167–L168：R0 fixture F2、D1 授權）、ADV-LAYER-01（逐層預算與 Gate 條文現值）、ADV-GATE-02（`gate_ledger.py` 介面）——僅作字面判準與介面來源，本票錨點皆 live 直查
- `coordination_dependency`: 無（ADV-OWNER-01、ADV-RESETISO-01 已 `CLOSED`，不重開、不追加紀錄；本票與任何編輯 `rules/cross-review.md`／`agents/cross-reviewer.md`／`cross_review.sh`／`CROSS_REVIEW.md` 之票須串行，`concurrency.md` §2；PROGRESS／Registry 單一寫入者）

## Verify Config
TICKET_ID=ADV-R0ROUTE-01
CREATED=2026-10-15
PRESET=docs
ALLOWED=.claude/rules/cross-review.md,.claude/agents/cross-reviewer.md,.claude/skills/sv/scripts/cross_review.sh,.claude/skills/sv/CROSS_REVIEW.md
FORBIDDEN=.claude/scripts/gate_ledger.py,.claude/scripts/tests/test_gate_ledger.py,.claude/hooks/gate_stop_guard.py,.claude/hooks/tests/gate_stop_guard_test.py,.claude/hooks/session_start.sh,.claude/hooks/registry_review_stop_guard.py,.claude/rules/ssot-governance.md,.claude/rules/concurrency.md,.claude/rules/ticket_pattern.md,.claude/skills/sv/SKILL.md,.claude/skills/sv/ssot_context.md,.claude/skills/qa/SKILL.md,.claude/skills/qa/scripts/structural_verify.sh,.claude/skills/qa/tests/test_report_fix_closure.sh,.claude/skills/uc/SKILL.md,.claude/settings.json,.claude/settings.local.json,.claude/CLAUDE.md,CLAUDE.md,CLAUDE.local.md,.claude/PROGRESS.md,.claude/PROGRESS_ARCHIVE.md,.codex/**,.agents/**,tools/**,app/**,plans/cso_briefing/project/TICKET_REGISTRY.json,plans/cso_briefing/project/TICKET_REGISTRY_ARCHIVE.json
MAX_LINES=N/A
DIFF_REF=<開工時 git rev-parse --short HEAD 回填>
ANCHOR_PROBED=2026-10-15 22:19
BASE_COMMIT=c32cc414
TARGET_BLOB=.claude/rules/cross-review.md:277c4067;.claude/agents/cross-reviewer.md:5c562de8;.claude/skills/sv/scripts/cross_review.sh:a6b61a76;.claude/skills/sv/CROSS_REVIEW.md:18f6a3a1
INTRODUCED_CAPABILITY=feature:r0-handback-routing
DEPENDENCY_DELTA=NONE

> PRESET=docs（四檔皆為治理文字／agent 契約／shell echo 與註解字面；無 `.kt`／`.xml`／gradle，`assembleDebug` 與 `git diff --stat` 之 docs 型適用性見 AC）。`MAX_LINES=N/A`：含授權判定／升報路由類不變量，失敗形狀為審查發現式，不可先驗估算（TPL v3.5 R3）；MAX_LINES 非 execution gate。`INTRODUCED_CAPABILITY=feature:r0-handback-routing`（canonical token；R0 回件分類承接為新路由行為）；`DEPENDENCY_DELTA=NONE`。`TARGET_BLOB`＝`git hash-object` 前 8 碼，四檔均為既有檔，工作樹與 HEAD blob 一致（資訊性診斷值，不作 gate，TPL v3.11）。`DIFF_REF` 依 `ticket_pattern.md`「票據模板規範」為字面佔位，開工時依 `qa/SKILL.md` Step 0 三原子動作回填。`Traycer Workflow: N/A`：純 `.claude/` 治理工具，不觸 `app/`；審查走 SV＋Cross-Review。**EOL**：`cross_review.sh` 工作樹為 CRLF（`file` 實測 `with CRLF line terminators`，index 為 LF、`attr/text eol=lf`），其餘三檔為 LF；編輯 MUST 保持各檔現行行尾，diff 不得出現整檔行尾改寫。**DEFERRED-TO-DOCSYNC（不開票）**：FORBIDDEN 命中 `.claude/PROGRESS.md`／`PROGRESS_ARCHIVE.md`（對照 `rules/cross-review.md`「管線標準觸碰面登記表」） → `/qa` Step 4 SKIP；Dashboard 同步、Done 標記、票檔 Pending→deliver 搬移與 checkbox 勾選屬標準 SOP 收尾，於下一輪 `/compact` 或後續 session 正常處理，**不另開票**。重放／真實鏈暫存物 MUST 置於 `tmp/ADV-R0ROUTE-01/` 或系統暫存 worktree（`tmp/` 受 `.gitignore` 忽略，不入 ALLOWED），符合 `concurrency.md` §1。
>
> **現值漂移紀錄（local-live vs parent，不重跑 plan）**：①parent 稱 D5 落點 `cross-review.md` L11，live 為 **L13**（L11 為節首句）；R0 路由條 L49 不變。②parent 稱 `cross_review.sh` 僅 L498／L501，live 同類人工措辭共 **6 處**（L14／L51／L173／L182／L498／L501），本票一併以**僅字面**方式收斂（同檔、同語義類、不動邏輯）。③parent 稱 `.agents/` 鏡像，live 為 `.codex/agents/cross-reviewer.toml` 與 `.agents/skills/sv/**` 兩個**未版控**且已分歧之副本，不同步。④parent 之 T01 前置已滿足（ADV-LAYER-01 CLOSED），helper 介面現役。⑤parent「R0 載入舊／新定義」之單次觀察與官方文件衝突，已列 SUPPORTED/HYPOTHESIS 並改為每次量測。⑥新增發現：R0 以 cwd 相對的 `tmp/verify_cmd.conf` 定位票檔，不足以支撐跨 worktree 重放 → 納入 AI-2。其餘 parent 事實（D5=乙文字、D9 併入、護欄一／二／三與事件三分類逐字不變）live 一致，無實質衝突。

## Action Items

> **AI-0 為強制首項**。以下各項為錨點與基線復驗；任一項不符，先依 TPL「基線漂移三分流」分類（`ANCHOR_STALE_SCOPE_INTACT`／`CANDIDATE_SUPERSEDED` 為資訊性，記錄後以現值續作；`ALREADY_EXECUTED_PENDING_CLOSURE`、觸及 `app/` 之 `BLOCKED_UNATTRIBUTED_STATE` 維持停止），再依 Error Handling。

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: 四個 ALLOWED 檔及唯讀參照檔
- **動作**: 逐字確認各「現值」於檔內命中數與票面所載相符（標「唯一」者恰 1 次），並記錄 `DIFF_REF`：
  1. 四檔皆已追蹤（`git ls-files` 4/4）、`git check-ignore -v` 對四檔零命中、工作樹 blob＝`TARGET_BLOB`；`cross_review.sh` `\r` 行數＝總行數（開票時 530/530），其餘三檔 `\r` 行數 0；`cross-review.md` 總行數 58。
  2. `rules/cross-review.md`：`直接進修正或 Checkpoint 裁決流程`（唯一）；`- 修正涉及：PROGRESS.md / sv_report*.md / ssot_context.md / CLAUDE.md / SSOT*.md / *_Contract*.md`（唯一，開票時 L13）；`票面已明確授權、屬已授權 scope 內之契約修復者走 Gate`（唯一，L14）。護欄與事件三分類基線——以前綴定位該行（`**護欄一（無放行權）**`、`**護欄二（否決申訴）**`、`**事件三分類**`、`**護欄三（Budget 隔離）**`）各取該行 `sha256sum` 前 16 碼，須等於 `0f131b144a958627`／`c4e9e22433db3e7a`／`dbb8dc96a07eaf1a`／`e8b369bf908aba98`（開票時 L50–L53）；自「`- **Checkpoint 格式增列機械狀態行**`」至檔尾（開票時 L54–L58）整段 `sha256sum` 前 16 碼＝`2d0378f3657e7c9d`。
  3. `agents/cross-reviewer.md`：`1. **Checkpoint 觸發掃描**`（唯一）、`授權無法判定`（唯一）、`(c) §2 零容忍型別名 grep`（唯一）、`裁決（**FORWARD** / FAIL / CHECKPOINT-REQUIRED）+ 分級表：`（唯一）；frontmatter `tools: Read, Grep, Glob`、`model: opus`、`effort: high` 各唯一。
  4. `cross_review.sh`：`人工介入必要`＝2（L51、L498）、`等待 Akira 裁決`＝1（L501）、`人工裁決 checkpoint（非 BLOCKER）`＝1（L173）、`等待人工裁決`＝1（L182）、L14 頭註含 `— 人工介入`（唯一）；`CROSS_REVIEW.md`：`人工介入（LIMIT 為人工裁決 checkpoint，非 BLOCKER）`＝1（L66）、`要求人工裁決`＝1（L81）。
  5. 唯讀參照：`gate_ledger.py` 含 `LAYERS = ("executor", "r0", "advisor")`、`--layer executor|r0|advisor`、`--kind local|advisor|r0`，且 `round start` 對非 executor 層檢查同 incident 對應 consult（開票時 L614–L619）；`sv/SKILL.md` L157–L160 仍為「步驟級事件→Gate」措辭（不改）；`.codex/agents/cross-reviewer.toml`、`.agents/` 命中 `.gitignore`（L70／L69），`.agents/` 下無 `cross-reviewer*`。
  6. 基線測試與環境：`bash -n .claude/skills/sv/scripts/cross_review.sh` 通過；`bash .claude/skills/qa/tests/test_report_fix_closure.sh` 基線結果記錄（本票不改該測試）；記錄 `git worktree list`、`.tmp/cross_review_state/` 內容與 sha256、`~/.claude` 內本 repo transcript 目錄（供 AI-6 取證）。
  7. Fixture 來源存在性：兩筆 R0 記錄檔 sha256 前 16 碼是否仍等於上表；F2 payload 副本、`.tmp/sv_report_*` 歸檔是否在；不在者 AI-5 改以票面「Fixture 耐久紀錄」＋git 端點重建並標「重建」。PPSS artifact（`rsch-20261015-b560a4fac7.json`）是否仍在（供 AI-6 場景 C）。
  8. `INTRODUCED_CAPABILITY` 與外審狀態未變（本票 External-review 欄）。
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [x] 完成（2026-10-16；錨點全符；step_limit 為既有事件〔gate_ledger.py L450 event type、ssot-governance.md L198／L233 引用〕，AI-3 維持含事件名；證據見收尾實錄）

### AI-1: `rules/cross-review.md`——D5=乙 與 R0 回件路由（兩處，護欄一／二／三與事件三分類逐字不變）
- **檔案**: `.claude/rules/cross-review.md`
- **現值（唯一，逐字）**：
  1. file-class bullet 主體（L13，含其後「**例外**…不觸發本款」括號，括號原文保留不動）：`- 修正涉及：PROGRESS.md / sv_report*.md / ssot_context.md / CLAUDE.md / SSOT*.md / *_Contract*.md（**例外**：…）`
  2. R0 路由條句尾（L49）：`R0 回報 FAIL / CHECKPOINT-REQUIRED → **MUST NOT** 送 Codex，直接進修正或 Checkpoint 裁決流程。`
- **目標值**：
  1. file-class bullet 改為對稱於其下 flag／DI bullet 的結構：`- 修正涉及：PROGRESS.md / sv_report*.md / ssot_context.md / CLAUDE.md / SSOT*.md / *_Contract*.md，且該檔未列於票面 `ALLOWED`，或該修改行為未被票面 Action Item／AC 明確授權（屬新 authority）→ MUST 經 Gate 本地取證＋Advisor 確認後 `HUMAN_REQUIRED(SCOPE)` 並輸出 Checkpoint；**`ALLOWED` 僅界定可觸碰檔案，不是行為授權**。該檔已列 `ALLOWED` **且**行為已被 Action Item／AC 明確授權（可指出對應 AI／AC 編號或具名裁決）者走 Gate 續作，不因檔名 Checkpoint（**例外**：…原括號全文保留）`
  2. R0 路由條句尾改為（句首「`R0 回報 FAIL / CHECKPOINT-REQUIRED → **MUST NOT** 送 Codex`」保留，護欄一不變）：`；**主 agent** 為 R0 回件唯一承接者，於 R0 記錄落盤後 **MUST** 逐列依 R0 分類欄處置，**MUST NOT** 僅因 verdict 字樣即停車輸出 Checkpoint：` 接下列四個子 bullet——
     - `ACTIONABLE`（ALLOWED 內、修法可本地判定）：記 `gate_ledger.py consult --kind r0`（`--note` 填 R0 記錄檔名）→ 以該修法 `round start --layer r0` → 修復 → 局部驗證＝重跑 R0 並確認該列已消除 → `round end`；重跑後同列仍在＝該輪 fail（逐層計輪，每層最多 3 次）。
     - `UNDECIDABLE`（判準衝突、授權或語境無法判定）：先本地取證（`consult --kind local`）；仍不能判定 → 主 agent 呼叫原生 `advisor` 工具（R0 為唯讀 subagent，**不是** Advisor，不得代記 advisor consult）→ `consult --kind advisor` → `round start --layer advisor`；Advisor 確認之事實與一手依據附入重跑 R0 之委派 prompt。
     - `AUTHORITY`（未獲票面授權之 feature flag 預設值／DI 綁定變更，或 file-class 命中而不具「ALLOWED 已列＋行為已被 AI／AC 明確授權」）：先本地取證，再經原生 Advisor 確認（含查證票面是否實有授權依據）；確認仍屬授權缺口 → `human --category SCOPE`，輸出 `HUMAN_REQUIRED` 單行，**僅此時**使用下方 Checkpoint 格式；確認已有授權依據 → 轉 `ACTIONABLE`／`FORWARD` 路徑並於 R0 記錄註明依據，不重問已裁決政策。
     - 無分類欄之列（舊格式回件）一律視為 `UNDECIDABLE`；R0 重跑後仍判 FAIL 之列，主 agent **不得自判放行**（護欄二：僅 Akira 可 override R0 誤攔）；incident／round／consult／human 事件與前置條件以 `ssot-governance.md` §Autonomous Engineering Gate 為準，本條不複製。
- **約束**：兩處之外 `rules/cross-review.md` 零 diff；不得改寫護欄一／二／三與事件三分類四行、其後 L54–L58 整段、`## FAIL 硬規範` 內 flag／DI bullet、`次數限制`、`退出機制`、Checkpoint 格式區塊；不得新增 verdict 字彙、CLASSIFICATION 值或 R0 記錄欄位。
- [x] 完成（2026-10-16；rules/cross-review.md 2 處 hunk，含 r0 層耗盡轉 UNDECIDABLE 補句）

### AI-2: `agents/cross-reviewer.md`——分類欄、職責 1 file-class 讀「ALLOWED＋行為授權」、職責 4(c) 自我提及、票檔定位
- **檔案**: `.claude/agents/cross-reviewer.md`
- **現值（唯一，逐字）**：職責 1 首句「`1. **Checkpoint 觸發掃描**：payload 或 diff 觸碰 `PROGRESS.md` / `sv_report*.md` / `SSOT*.md` / `*_Contract*.md` / `CLAUDE.md` → 標記 `CHECKPOINT-REQUIRED`，不放行。`」；同條內「`判定「票面是否明確授權」時，讀 `tmp/verify_cmd.conf` 的 `TICKET_ID`，…（R0 僅有唯讀工具，無法讀到時回報「授權無法判定」並標 `CHECKPOINT-REQUIRED`，不得推定已授權）。`」；職責 4(c) 行「`   - (c) §2 零容忍型別名 grep（…）→ `BLOCKER``」（本行**不改寫**）；輸出格式節「`裁決（**FORWARD** / FAIL / CHECKPOINT-REQUIRED）+ 分級表：`」及其表。
- **目標值**：
  1. **職責 1 file-class**：句改為「payload 或 diff 觸碰 …/`CLAUDE.md` 時（**清單本身不增刪**），**先**讀票面判定授權：該檔已列 `ALLOWED` **且**該修改行為已被 Action Item／AC 明確授權（回報須指出對應 AI／AC 編號或具名裁決）→ **不因檔名標 `CHECKPOINT-REQUIRED`**，於分級表列「file-class 命中：授權依據＝…」註記，可 `FORWARD`；未列 `ALLOWED` 或行為未被授權 → 分類 `AUTHORITY` 並標 `CHECKPOINT-REQUIRED`；票面讀不到、授權無法判定 → 分類 `UNDECIDABLE` 並標 `CHECKPOINT-REQUIRED`，不得推定已授權。**`ALLOWED` 列檔不等於行為授權。**」；既有「PROGRESS／PROGRESS_ARCHIVE 條件豁免」三要件區塊原文不動。flag／DI 關鍵詞句：未授權之預設值／DI 綁定變更 → `AUTHORITY`；授權無法判定 → `UNDECIDABLE`；其餘維持取證註記。
  2. **票檔定位**：在判定授權處加一句「票號與票檔**優先採委派 prompt 明示之 `TICKET_ID` 與票檔絕對路徑**（跨 worktree 重放用），未明示才讀 `tmp/verify_cmd.conf`」。
  3. **職責 4(c) 補則**：於 (c) 行**之後**新增一個子 bullet（不改寫 (c) 行、**不重列三個型別名**，以「(c) 所列名稱」指代）：型別名僅出現於「敘述／枚舉該禁令本身」之文字（`.md`／`.sh` prompt／票面目標值中的禁令條文、BLOCKER 判準表、`ssot_context.md` §2 條文）且 diff 無 `.kt`／`.xml`／`.java` 的型別宣告、引用、`import` 或繼承 → **不計 `BLOCKER`**，列「(c) 自我提及（禁令文字）」註記並可 `FORWARD`；型別名出現於程式碼 diff 之宣告／引用／import，或出現於「指示採用該型別」之文字 → 仍 `BLOCKER`（分類 `ACTIONABLE`，附 ALLOWED 內修法）；語境無法判定 → 分類 `UNDECIDABLE`，不得靜默放行亦不得直接 BLOCKER。
  4. **輸出格式**：分級表改為五欄「嚴重度｜分類｜檔案:行號｜問題｜建議」；`分類` 值限 `ACTIONABLE`／`UNDECIDABLE`／`AUTHORITY`／`—`（`—` 僅用於 FORWARD 註記列）；`ACTIONABLE` 列 **MUST** 附 ALLOWED 內可執行修法（檔案＋改法）。裁決規則：任一列 `UNDECIDABLE` 或 `AUTHORITY` → `CHECKPOINT-REQUIRED`；否則任一列 `ACTIONABLE` → `FAIL`；否則 `FORWARD`。補一句：「裁決與分類僅為主 agent 路由輸入，**不是**放行，`CHECKPOINT-REQUIRED` 不等於停車等人；由主 agent 依 `rules/cross-review.md` R0 路由逐列承接。」末句「`FORWARD` 僅表示『可送 Codex』，任何情況下不得表述為放行終審字彙。」原文保留。
- **約束**：frontmatter（`name`／`description`／`tools: Read, Grep, Glob`／`model`／`effort`）零變更；不新增職責項目、不改職責 2／3／5、不新增 R0 檢查或 agent；新增文字不得出現「放行」「PASS」作 R0 結論。
- [x] 完成（2026-10-16；cross-reviewer.md 分類欄／職責1／4(c) 補則／票檔定位）

### AI-3: `cross_review.sh` 與 `CROSS_REVIEW.md`——步驟級人工措辭改指 Gate（僅字面，不動邏輯）
- **檔案**: `.claude/skills/sv/scripts/cross_review.sh`（CRLF，保持）、`.claude/skills/sv/CROSS_REVIEW.md`
- **現值→目標值**（皆唯一；僅替換引號內 echo 字串或註解字面，不動條件式、`exit` 碼、變數、`STATE_FILE`、token 行）：
  1. L14 頭註尾 `…／closure 守門拒絕（REPORT_FIX_ESCALATE）— 人工介入` → `…／closure 守門拒絕（REPORT_FIX_ESCALATE）— 步驟級事件，依 ssot-governance.md §Autonomous Engineering Gate 處置`
  2. L51 `fail_budget()` 內 `— 人工介入必要` → `— 步驟級預算用盡，依 Gate 處置（記 step_limit）`
  3. L173 `— 人工裁決 checkpoint（非 BLOCKER）；不得再自動重跑。` → `— 步驟級上限（非 BLOCKER，記 step_limit），依 Gate 處置；不得再自動重跑。`（`echo "CHECKPOINT_REPORT_FIX_LIMIT"` token 行**不動**）
  4. L182 `停止自動閉環，等待人工裁決。` → `停止自動閉環（步驟級事件），依 Gate 處置。`
  5. L498 `FAIL BUDGET EXHAUSTED. 人工介入必要。` → `FAIL BUDGET EXHAUSTED（步驟級預算），依 Gate 處置。`
  6. L501 `will hard-stop. 等待 Akira 裁決。` → `will hard-stop（步驟級預算），依 Gate 處置。`
  7. `CROSS_REVIEW.md` L66 `人工介入（LIMIT 為人工裁決 checkpoint，非 BLOCKER）` → `步驟級事件：記 step_limit，依 Gate 處置（LIMIT 為步驟級上限，非 BLOCKER）`；L81 `要求人工裁決` → `為步驟級上限，依 Gate 處置`。
- **約束**：`exit` 碼語義、`MAX_BLOCKER_BUDGET`、`REPORT_FIX_COUNT` 上限、`CHECKPOINT_REPORT_FIX_LIMIT`／`REPORT_FIX_ESCALATE` token、PROMPT_HEADER heredoc 全文零變更；`BLOCKER CHECKPOINT` 標籤字樣保留。
- [x] 完成（2026-10-16；cross_review.sh 6 處＋CROSS_REVIEW.md 2 處，CR=530/530，bash -n ok）

### AI-4: 靜態檢核、consumer 一致性與回歸
- **檔案**: 不改檔（執行驗證）
- **動作**：
  1. `bash -n cross_review.sh` 通過；`bash .claude/skills/qa/tests/test_report_fix_closure.sh` 結果與 AI-0 基線一致（全綠，F1–F9 斷言不變）；`git diff -U0 <DIFF_REF>..<DIFF_END> -- cross_review.sh` 僅含 AI-3 第 1–6 項六處 hunk，每個 hunk 只動 echo／註解字面。
  2. `rules/cross-review.md`：`git diff -U0` 僅 AI-1 兩處 hunk；護欄一／二／三與事件三分類四行之 `sha256sum` 前 16 碼仍等於 AI-0 第 2 項基線；L54–L58 整段 sha 仍為 `2d0378f3657e7c9d`。
  3. 殘留 grep 為 0：`直接進修正或 Checkpoint 裁決流程`（cross-review.md）、`人工介入必要`／`等待 Akira 裁決`／`人工裁決 checkpoint（非 BLOCKER）`／`等待人工裁決`（cross_review.sh）、`要求人工裁決`／`人工裁決 checkpoint`（CROSS_REVIEW.md）。**保留（設計內，不算殘留）**：`rules/cross-review.md` Checkpoint 格式區塊內的 `⏸️ 等待 Akira 裁決`（僅 `HUMAN_REQUIRED` 成立時輸出，現 L23，語義不變）。
  4. consumer 一致性：對 `.claude/` 內 `R0`／`CHECKPOINT-REQUIRED`／`cross-reviewer` 全文 grep（排除 `.tmp`、PROGRESS 檔、已 `CLOSED` 之 deliver 票），確認無與新路由互斥之敘述；`sv/SKILL.md`、`ssot-governance.md`、`gate_stop_guard.py` 之 R0／r0 敘述僅**讀取比對、不修改**，若發現互斥敘述 → 列入回報並停止（屬 T01 範圍或需擴 ALLOWED，見 Error Handling）。
  5. 行尾：`cross_review.sh` `\r` 行數仍＝總行數，其餘三檔 `\r` 行數 0；`git diff --stat <DIFF_REF>..<DIFF_END>` 僅涉 ALLOWED、FORBIDDEN 零變更；`cross-reviewer.md` frontmatter 逐行與 HEAD 相同。
  6. 護欄字面核對：`cross-reviewer.md` 與 `rules/cross-review.md` 新增文字不含「放行終審」之 R0 結論用語、不含零容忍型別名三字串於**新增行**（`git diff -U0 | grep '^+'` 對該三名 0 命中）。
- [x] 完成（2026-10-16；rules 2 hunk、護欄四行 sha 與 tail sha 等基線、殘留 grep 全 0、新增行對三名 0 命中、frontmatter 零 diff、CR cross_review.sh 530/530 其餘 0、test_report_fix_closure ALL PASS、consumer 無互斥敘述〔ssot-governance L192-193／gate_stop_guard L341-358 僅讀取比對〕、diff stat 4F +21 −14）

### AI-5: Fixture 正反重放（隔離）與耐久紀錄
- **檔案**: 不改檔（執行驗證；重放產物僅在 `tmp/ADV-R0ROUTE-01/` 與系統暫存 worktree；結果寫入本票收尾實錄）
- **動作**：
  1. **環境**：於系統暫存目錄 `git worktree add --detach` 兩個暫存 worktree 分別至 F1 端點 `0cb6fd44`、F2 端點 `0475fbad`（不改共用 Git config、不 fetch／pull、不動主工作樹）。各放入 `tmp/verify_cmd.conf`（`TICKET_ID`＝`ADV-REVIEW-01`／`ADV-RESETISO-01`，`DIFF_REF`／`DIFF_END`／`ALLOWED` 照抄各原票 Verify Config）；對應票檔自主 repo `deliver/` 複製到暫存 worktree 同相對路徑並記 sha256（票檔為 gitignored，暫存 worktree 內無）。
  2. **Payload 生成（不呼叫真實 Codex）**：PATH 前置 stub `codex`（讀 stdin 存檔、回固定 `PASS`），**先驗** `command -v codex` 解析到 stub；於各暫存 worktree 根目錄以 `bash .claude/skills/sv/scripts/cross_review.sh <sv_report> <DIFF_REF>..<DIFF_END>` 僅生成 payload（F2 另有主樹副本 `tmp/ADV-RESETISO-01/payload_r0_1.md` 可交叉比對）。`sv_report` 優先取歸檔（`.tmp/sv_report_*`，記 sha256）；取不回者以最小重建版（`Files Changed`／`Implementation Scope` 與票面 `ALLOWED` 一致）並標「重建」。`structural_verify` 預檢因暫存 worktree 缺檔而擋下：僅補齊重放環境後重跑，不解讀為審查結果。
  3. **R0 重放**：自主 session 以 `Agent subagent_type=cross-reviewer` 委派，prompt 附 payload 絕對路徑、**`TICKET_ID` 與票檔絕對路徑**（AI-2 第 2 項）。**量測實載定義版本**：回件含五欄「分類」表 → 新定義；否則舊定義。回件為舊格式 → 等待 ≥15 秒（官方文件：編輯後數秒偵測）後重委派一次；仍為舊格式 → 該列記 `UNVERIFIED（R0 實載舊定義，原因未明）`，並以「general-purpose agent＋`cross-reviewer.md` 全文為指示、僅 Read/Grep/Glob 語義」補作**預期路由證據**，標 `[stub-equivalent]`，**不冒充 R0 實測**。
  4. **正對照**：F1 payload → 預期見「Fixture 耐久紀錄」；F2 payload → 預期同表。逐列記錄：原列 → 新回件分級表對應列、分類、verdict。
  5. **負對照（僅在 `tmp/ADV-R0ROUTE-01/` 的 payload／票檔副本中構造，不入任何 repo 檔）**：F1 副本於 domain `.kt` 增一行對零容忍名稱之型別使用 → 預期仍 `BLOCKER`（`ACTIONABLE`）；F2 票檔副本移除 `.claude/CLAUDE.md` 於 `ALLOWED`（另一副本移除授權該行為之 AI／HC）→ 預期 `AUTHORITY`＋`CHECKPOINT-REQUIRED`；並於 F1 副本構造一個語境模糊的名稱出現（例如「建議改用該型別」之 `.md` 敘述）→ 預期 `UNDECIDABLE`。
  6. **耐久紀錄**：於收尾實錄寫入每筆 fixture：來源識別（路徑＋sha256 前 16 碼＋耐久承載位置）、原判定、必要輸入片段（引用本票上表，不重貼完整 payload）、預期路由、**實際重放結果**（含實載定義版本證據、回件分級表摘要、與預期是否一致、`[R0 實測]`／`[stub-equivalent]`／`UNVERIFIED` 標籤）；不一致者如實記錄，不為達成預期而改寫輸入。結論限 N=1、同一模型、單次取樣，不宣稱一般可靠。
  7. **隔離證明與清理**：重放前後比對 AI-0 第 6 項各值（`.tmp/cross_review_state/` 與 `.tmp/cross_review_pass/` 不含 `ADV-R0ROUTE-01`／兩 fixture 票之新增、Registry 條目不變、`git worktree list`）；任何變動 → 停止回報。核對暫存路徑確為本次建立後 `git worktree remove`（被替換 tracked 檔拒絕時先 `git -C <path> checkout -- <檔>` 再重試，不盲目 `--force`），`git worktree list` 回到開工前筆數。
- [x] 完成（2026-10-16；真實 R0 重放 UNVERIFIED（兩輪舊格式）；stub-equivalent 6 組符合預期；紀錄 tmp/ADV-R0ROUTE-01/save/*，耐久紀錄於收尾實錄〔AI-7〕）

### AI-6: [real] 真實鏈驗收 A–D（parent §5；hook 鏈仍 [stub]）
- **檔案**: 不改檔（執行驗證；暫存 worktree＋scratch 票；結果寫入收尾實錄）
- **動作**：在 AI-5 的暫存 worktree 之一（或新建拋棄式 worktree）建立 scratch 票檔 `plans/cso_briefing/project/Pending/ADV-R0ROUTE-SCRATCH-<A|B|C|D>.md`（極簡，gitignored），以 `python .claude/scripts/gate_ledger.py --ticket <SCRATCH> …` 驅動 helper（`--ticket` 僅選 ledger key，不是 activation，不碰 Registry、不寫 `active.json`）；**不寫本票或任一真實票的 ledger**。四場景在**本 session**（具原生 `advisor` 工具）真實執行：
  - **A 已知答案、已授權**：scratch payload 含一個 R0 可判 `ACTIONABLE` 的真實缺陷（例：domain `.kt` 副本新增 `import android.*`，修法＝移除該 import，在 scratch ALLOWED 內）→ 真實 R0 回件 `ACTIONABLE` → `incident open`→`consult --kind r0`→`round start --layer r0`→修復→重跑 R0 回 `FORWARD`→`round end --result pass`。證據：ledger 事件序號＋時戳；transcript 該段**無對 Akira 之提問**、無 `HUMAN_REQUIRED`。
  - **B R0 無法判定**：scratch payload 含語境模糊的 `UNDECIDABLE` 列 → `consult --kind local` → 主 agent **真實呼叫原生 `advisor`** → `consult --kind advisor` → `round start --layer advisor` → 修復／確認 → 重跑 R0 → `round end --result pass`。證據：本 session transcript（`~/.claude/projects/<本 repo>/<session>.jsonl`）中 `"name":"advisor"` 之 `tool_use` 與其 `advisor_tool_result`／`advisor_redacted_result` 的**行號與時序**，早於 ledger `consult --kind advisor` 事件、早於 `round start --layer advisor`；**以 transcript 為真實性依據，不以 stub、block／allow 表代替，亦不宣稱機械已驗**。
  - **C Advisor 指出外部事實缺口**：使用本票開票時已執行的**真實** `research_submit`（`rsch-20261015-b560a4fac7`；官方文件載明 `.claude/agents/` 編輯後數秒內被偵測、下一次委派用新定義）：先驗 artifact 仍在 → `ppss record --artifact <路徑>` → `ppss read --quote <artifact 內來源子字串>`（`evidence_ok`）→ 於同 scratch incident 的後續 round 策略中**引用該 request_id 並驗證**（例：R0 回舊格式 → 等待後重委派）。僅記錄答案不算通過；artifact 已逾 retention → C 記 `UNVERIFIED`，**不為湊驗收重送同題**（D8）；若執行中出現新的、不同的外部事實缺口才可另送一次並說明。
  - **D 真人類權威缺口**：F2 負對照（票檔副本移除授權）之 R0 回件 `AUTHORITY` → 本地取證 → **真實 `advisor`** 確認 → `consult --kind advisor` → `human --category SCOPE`（缺 local＋advisor consult 時 helper 應拒）→ 僅輸出最小 `HUMAN_REQUIRED` 單行，於實錄標「SCRATCH，非真實升報」；無偽造 ledger 事件。另驗：`PERMISSION` 路徑在權限拒絕後不再重試同一動作屬 [stub]（沿用 ADV-GATE 既有測試，不重做）。
  - **hook 鏈**：Stop hook 需 Registry lifecycle，**不在本票 [real] 範圍**；實錄明寫「hook 真實鏈未驗」。
  - **清理**：保存各場景 ledger 內容摘要與 sha256 後，刪除 scratch 票檔及 `<git-common-dir>/gate/ADV-R0ROUTE-SCRATCH-*.jsonl(.lock)`（僅 scratch；先核對檔名前綴），`git worktree remove`。
- 每場景結果標籤：`[real]`（真實工具＋transcript 證據齊備）／`UNVERIFIED`（Advisor 不可達、artifact 逾期、helper 拒絕 scratch 票等）。**不足者保持 `UNVERIFIED` 並回報 Akira，不追加補作、不改 helper 以通過驗收**。
- [x] 完成（2026-10-16；A／B／D 以 ledger＋原生 advisor（#3 line 1022–1023）執行，R0 產出為 stub-equivalent；C 僅證研究回流消費（事後登錄）；hook 鏈未驗；證據見收尾實錄第 3、4 節）

### AI-7: 收尾實錄與外審欄位回填（寫入本票檔）
- **檔案**: 本票檔（`plans/cso_briefing/project/Pending/ADV-R0ROUTE-01_handback.md`，收尾實錄；非 ALLOWED 清單內之正式收尾寫入，與既有 ADV 票同）
- **動作**：寫「收尾實錄」段：AI-0 命中數與基線；各 AC 證據；AI-4 回歸結果；AI-5 兩筆 fixture 完整耐久紀錄（含實載定義版本、正反重放逐列結果、sha256）；AI-6 四場景證據（transcript 行號、ledger 序號、標籤）與清理證明；**把頂部 `External-review decision` 的 bounded probe 欄由 `PLANNED` 改記實際結果**（`EXECUTED`＋摘要，或 `NOT_EXECUTED`／`UNVERIFIED`＋原因）；本票自身 R0 事件（若有）之落盤檔名、分類與處置；**限定交付聲明**：列出所有 `UNVERIFIED` 項與 [stub]／[real] 分布，不得宣稱 hook 真實鏈或完整跨 session 驗證；將兩筆 R0 fixture 之摘要確認已於本票「Fixture 耐久紀錄」＋實錄中落地（不僅 `.tmp` 路徑）。
- [x] 完成（2026-10-16；收尾實錄第 1–6 節，probe 欄由 PLANNED 改 EXECUTED（部分 UNVERIFIED）；本票自身 R0／SV 結果待 /sv 後補記）

## Hard Constraints
1. `rules/cross-review.md` 護欄一／二／三與事件三分類四行**逐字不變**（sha 基線 AI-0 第 2 項），其後 L54–L58 整段與 `## FAIL 硬規範`內 flag／DI bullet、`次數限制`、`退出機制`、Checkpoint 格式區塊零變更。
2. R0 **不得放行或計 PASS**；不新增 R0 機制、agent、檢查項或 verdict／CLASSIFICATION 值；`cross-reviewer` frontmatter（`tools: Read, Grep, Glob`、`model`、`effort`）零變更；R0 不是 Advisor，不得代記 advisor consult。
3. **不把 `ALLOWED` 當空白行為授權**：D5 與職責 1 文字 MUST 以「ALLOWED 已列**且**行為被 AI／AC／具名裁決明確授權」為走 Gate 之條件，R0 與主 agent 回報授權時 MUST 指出對應 AI／AC 編號或具名裁決。
4. 不改 BLOCKER／REPORT_FIX 預算、`exit` 碼語義、`ROUND`／`REPORT_FIX_COUNT`／state／LIMIT、token 行、`PROMPT_HEADER` heredoc；`cross_review.sh` 僅 echo／註解字面（AI-3 六處）。
5. 不改 `gate_ledger.py`、Stop hook、兩者測試、`ssot-governance.md` Gate 條文、`sv/SKILL.md`、`ssot_context.md`、`session_start.sh`（屬 T01／已 CLOSED 之票）；不新增 ledger 事件型別、consult kind 或層。
6. 新增文字**不得重列零容忍型別名**，亦不得改寫既有 (c) 行；R0 路由條不複製 Gate 的 incident／round 機制細節，僅指向 `ssot-governance.md` §Autonomous Engineering Gate 並列出事件名。
7. 不併入 `gate_ledger.py` 的 owner 殘餘窗口、reset 額外清理政策、`.codex/`／`.agents/` 副本同步或其他新能力；不重開 ADV-OWNER-01、ADV-RESETISO-01、ADV-LAYER-01、ADV-REVIEW-01，不對其追加紀錄。
8. [stub]／[real] 標籤誠實：不以 stub、單次歷史可達或契約描述冒充本輪實測；hook 鏈不宣稱已驗；T01 之限定交付邊界不提升。
9. 重放與真實鏈只在暫存 worktree／`tmp/ADV-R0ROUTE-01/`／scratch 票 ID 內進行；**重放不得呼叫真實 Codex**（stub 預驗）；不寫真實票 ledger、Registry、`active.json`、`.tmp/cross_review_state/` 之真實票條目。
10. 負對照輸入只存在於 `tmp/` 副本，不得提交進任何 repo 檔；兩筆 fixture 之耐久紀錄 MUST 落在本票檔（不得僅留 `.tmp` 路徑），且不重貼完整 payload／整份歷史報告。
11. `ALLOWED` 維持單一首層 `.claude`；擴至其他首層或第 5 檔 MUST 停止並重判 6b①（出查詢包），不得靜默放行。

## Error Handling
- AI-0 任一錨點／基線不符（命中數、blob、sha、行尾）→ 依 TPL「基線漂移三分流」分類後處置；範圍內可續者記錄後續作，其餘停止回報，禁自行推定等價位置。
- `git check-ignore -v` 對任一 ALLOWED 檔命中，或 `Pending/` 票檔以外標的被忽略 → 停止回報（gitignore 死結律）。
- **本票自身 R0**：本票修訂 `cross-reviewer.md`／`rules/cross-review.md`，R0 若以**舊定義**命中 4(c) 名稱（修訂處緊鄰既有 (c) 行）或回 `CHECKPOINT-REQUIRED`，**不得直接停車輸出人工 Checkpoint**，亦**不得自行豁免**。依現行 Gate：落盤 R0 記錄（`.tmp/fail-advisor/R0_ADV-R0ROUTE-01_*.md`）→ 本地取證（hunk 是否僅為規則文字、是否有 `.kt` 型別使用、票面 AI-2 是否授權）→ 呼叫**真實 Advisor**。若 Advisor 確認為 fixture F1 同型之規則缺口（攔截忠實於現行規格、規格缺分支）：分類 `TRUE-POSITIVE-RULE-GAP`（承接票號＝`ADV-R0ROUTE-01`，不計 `_OVERRIDE`、不耗誤攔預算）；**能否送 Codex 取決於 Akira 是否已就本票追認預授權**（見「必要未決事項」）——未追認 → 依護欄二 `HUMAN_REQUIRED` 請 Akira override；已追認 → 依追認範圍續送。若發現獨立 finding、未授權變更、或 hunk 實為 flag 預設值／DI 綁定變更 → 不適用任何預授權，依 AUTHORITY 路徑。
- AI-5 R0 回件實載舊定義 → 依 AI-5 第 3 項重委派一次，仍舊 → `UNVERIFIED` ＋ `[stub-equivalent]` 補證，不冒充實測。
- 重放時偵測到真實 `codex` 將被呼叫（stub 預驗失敗）、或任何重放步驟寫入真實票 state／ledger／Registry → 立即停止，保存現場，揭露（ADV-LAYER-01 曾有「stub PATH 誤跑真實 Codex」偏差前例），不補呼叫、不重試同一動作。
- 真實 Advisor 不可達 → AI-6 B／D 記 `UNVERIFIED`，**不偽造 consult、不為滿足前置硬湊輪次**；PPSS artifact 逾期 → C 記 `UNVERIFIED`。
- `gate_ledger.py` 對 scratch 票回 exit 3／4（身分／位置）→ 於暫存 worktree 補 scratch 票檔至 `Pending/` 後重試一次；仍拒絕 → 該場景 `UNVERIFIED`，**不改 helper**（屬 T01）。
- AI-4 第 4 項發現與新路由互斥且在 FORBIDDEN 內的文字（如 `ssot-governance.md`、`gate_stop_guard.py`、`sv/SKILL.md`）→ 停止回報（列出檔案：行號與互斥句），由 Akira／後續票裁決是否擴 scope 或開 T01 follow-up；**不自行擴 ALLOWED**。
- 需新增 `ALLOWED` 第 5 檔或跨首層路徑才能完成 → 停止回報並重判 6b①（出查詢包）；不得靜默放行。
- 任何要求新增 R0 檢查項、新 verdict／CLASSIFICATION 值、或改 Gate／helper／hook 才能達成目標 → 停止回報（SCOPE），不自行實作。
- **[標配]** 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中（標的被 .gitignore 排除）→ 停止回報（gitignore 死結律）。

## Acceptance Criteria
- [x] **[存在性]** `rules/cross-review.md` file-class bullet 含「`ALLOWED` 僅界定可觸碰檔案，不是行為授權」及「已列 `ALLOWED` 且行為已被 Action Item／AC 明確授權者走 Gate 續作」，未授權／未列者仍經 Advisor 確認後 `HUMAN_REQUIRED(SCOPE)`（AI-1，grep 逐字） 〔2026-10-16；grep 逐字，rules L13〕
- [x] **[存在性]** `rules/cross-review.md` R0 路由條含 `ACTIONABLE`／`UNDECIDABLE`／`AUTHORITY` 三類處置與「主 agent 為 R0 回件唯一承接者」「不得自判放行（護欄二）」，且 `直接進修正或 Checkpoint 裁決流程` 殘留為 0；護欄一／二／三與事件三分類四行 sha、L54–L58 整段 sha 與基線相等（AI-4 第 2–3 項） 〔2026-10-16；rules L49–L53；殘留 0；四行 sha＋L54–L58 sha 等基線；收尾實錄第 1 節〕
- [x] **[存在性]** `cross-reviewer.md` 含五欄「分類」輸出格式與裁決規則、職責 1 file-class「ALLOWED＋行為授權」判讀與 `ALLOWED 列檔不等於行為授權`、票檔定位「委派 prompt 優先」、職責 4(c) 補則；frontmatter 逐行與 HEAD 相同；新增行對零容忍三型別名 0 命中（AI-4 第 5–6 項） 〔2026-10-16；cross-reviewer.md L13／L36／L40＋補則；frontmatter 零 diff；新增行三名 0 命中〕
- [x] **[存在性]** `cross_review.sh` 六處與 `CROSS_REVIEW.md` 兩處人工措辭已替換且殘留 grep 為 0；`bash -n` 通過；`test_report_fix_closure.sh` 結果與基線一致；`git diff -U0` 顯示 `cross_review.sh` 僅六處字面 hunk；`\r` 行數與基線相同（AI-3／AI-4） 〔2026-10-16；6 處＋2 處殘留 0；bash -n ok；test_report_fix_closure ALL PASS；CR 530/530〕
- [x] **[可達性]** R0 回件 producer→consumer→路由全鏈可達並附 producer 證據欄（執行後回填 `檔案:行號`）：producer＝`cross-reviewer.md` 分類欄輸出格式〔證據：〕；承接者＝主 agent 依 `rules/cross-review.md` R0 路由條〔證據：〕；Router 消費＝`gate_ledger.py` `consult --kind r0`／`round start --layer r0`／`--layer advisor`／`human --category SCOPE` 之 CLI 與前置檢查〔證據：〕；並於收尾實錄明載「R0 無腳本節點，承接點為主 agent 文字契約，Stop hook 僅觀察 ledger 事件」，**目標不縮成 echo 措辭、分類表或 ledger 紀錄** 〔2026-10-16；證據欄見收尾實錄第 5 節〕
- [ ] **[行為性]** given F1（ADV-REVIEW-01 重建 payload，R0 實載新定義）／expect 4(c) 兩列不再判 `BLOCKER`（註記為禁令文字自我提及）、整體 `FORWARD`；**負對照** given 同 payload 副本新增 domain `.kt` 型別使用／expect 仍 `BLOCKER`（`ACTIONABLE`）；given 語境模糊副本／expect `UNDECIDABLE`（AI-5）。實載舊定義者該列標 `UNVERIFIED`＋`[stub-equivalent]` 〔未勾：真實 R0 兩輪皆舊格式，前提未成立；stub-equivalent 結果見收尾實錄第 2 節〕
- [ ] **[行為性]** given F2（ADV-RESETISO-01 payload＋票檔，`.claude/CLAUDE.md` 已列 ALLOWED 且 AI-2／HC3 授權）／expect R0 不標 `CHECKPOINT-REQUIRED`、註記授權依據、`FORWARD`，主 agent 走 Gate 續作；**負對照** given 票檔副本移除 ALLOWED 或授權／expect `AUTHORITY`＋`CHECKPOINT-REQUIRED`，路由＝本地取證→真實 Advisor→`human --category SCOPE`（AI-5、AI-6 D） 〔未勾：同上；stub-equivalent F2／N2a／N2b 符合預期〕
- [ ] **[行為性]** [real] 鏈 A：真實 R0 `ACTIONABLE` → `consult --kind r0` → `round start --layer r0` → 修復 → 重跑 R0 → `round end pass`，transcript 無對 Akira 提問、無 `HUMAN_REQUIRED` 〔未勾：A 鏈 R0 產出為 stub-equivalent，非真實 R0；ledger／修復段為 real〕
- [x] **[行為性]** [real] 鏈 B：transcript 依序出現 `tool_use name=advisor` 與 `advisor_tool_result`，其後 ledger `consult --kind advisor` → `round start --layer advisor` → `round end pass`；無 consult／跨 incident consult 時 `--layer advisor` 被 helper 拒絕；實錄標明**真實性依 transcript、非機械已驗** 〔2026-10-16；transcript line 1022→1023 早於 ledger consult／round start；跨 incident 與缺 consult 皆被拒 exit 5；R0 產出為 stub-equivalent；收尾實錄第 3 節〕
- [x] **[行為性]** [real] 鏈 C：`ppss record／read --quote`（`evidence_ok`）後，該證據被同 incident 後續 round 策略引用並驗證，`ppss` 動作不改任何層計數；artifact 逾期者標 `UNVERIFIED` 不補送同題 〔2026-10-16；**僅證研究回流消費段**，事後登錄，不證 Advisor→研究觸發完整鏈；ppss 不改層計數〕
- [x] **[行為性]** [real] 鏈 D：真實 Advisor 確認後僅輸出最小 `HUMAN_REQUIRED(SCOPE)` 單行（標 SCRATCH），缺 local／advisor consult 時 `human` 被拒；無偽造事件、無硬湊輪次 〔2026-10-16；SCRATCH 單行；缺 consult 時 exit 5；未 resolve；無偽造事件〕
- [x] **[存在性]** 收尾實錄含：兩筆 fixture 耐久紀錄（來源識別、sha256、原判定、必要輸入片段、預期路由、實際重放結果、實載定義版本證據、標籤）、AI-6 各場景證據與 `UNVERIFIED` 清單、「hook 真實鏈未驗」、External-review probe 欄已由 `PLANNED` 改記實際結果、隔離證明與清理證明 〔2026-10-16；收尾實錄第 2–6 節〕
- [x] **[存在性]** 實作 commit 前 `git diff --stat` 僅涉 ALLOWED 四檔，FORBIDDEN 零變更；`git worktree list` 與開工前相同；`tmp/` 與 scratch ledger 已清理 〔2026-10-16；commit 前 4F +21 −14 皆 ALLOWED；`git worktree list` 2 筆同開工前；scratch 票檔／ledger 已逐檔 rm、`tmp/ADV-R0ROUTE-01/` 於 closeout 移除〕
- [x] ~~assembleDebug PASS~~ (N/A: docs preset) 〔N/A: docs preset〕
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案 〔2026-10-16；git diff --stat 4F +21 −14，皆 ALLOWED，FORBIDDEN 零變更〕

## 收尾實錄（Agent，QA／SV 前；2026-10-16）

> 本段為耐久載體（`.tmp/`、`tmp/` 皆非版控）。原始保存：`tmp/ADV-R0ROUTE-01/save/`（payload、R0 回件摘要、stub-equivalent 結果、scratch ledger 副本）與 `variants/`（負對照輸入）。

### 1. 實作與靜態檢核（AI-0～AI-4）
- **AI-0**：四檔 tracked 4/4、`check-ignore` 零命中、blob＝`TARGET_BLOB`、行尾基線（`cross_review.sh` CR 530/530，其餘 0）、各錨點命中數與票面相符、護欄四行與 L54–L58 sha 等基線；`step_limit` 為既有事件（`gate_ledger.py` 事件型別 `step_limit`，`ssot-governance.md` L198／L233 引用）→ **AI-3 第 2、3 項與 `CROSS_REVIEW.md` L66 維持帶事件名**（依使用者校正，無需改為「依 Gate 處置」）。`DIFF_REF` 由 `activate_ticket.sh` 回填 `c32cc414`。
- **實作**：4F +21L −14L（未提交）。`rules/cross-review.md` 2 hunk（L13 file-class bullet 對稱化；R0 路由條 L49＋四個子 bullet，ACTIONABLE 子 bullet 依使用者校正補「r0 層三次用盡或本層已無可行修法可提早升級 → 轉 UNDECIDABLE 路徑，不停等 Akira；升級時攜帶同 incident 已試策略、結果與未解原因，Advisor 層不原樣重播」）；`cross-reviewer.md`（職責 1 file-class 讀「ALLOWED＋行為授權」＋票檔定位委派 prompt 優先＋AUTHORITY／UNDECIDABLE 分類；職責 4(c) 補則為新增子 bullet、不改 (c) 行；五欄分類表與裁決規則）；`cross_review.sh` 6 處＋`CROSS_REVIEW.md` 2 處僅字面。
- **AI-4**：`bash -n` ok；`test_report_fix_closure.sh` ALL PASS；護欄一／二／三／事件三分類四行 sha 與 L54–L58 sha 等基線（`0f131b14…／c4e9e224…／dbb8dc96…／e8b369bf…`、`2d0378f3657e7c9d`）；殘留 grep 全 0（保留 `⏸️ 等待 Akira 裁決` 現 L23）；新增行對零容忍三名 0 命中；frontmatter 零 diff；`cross_review.sh` CR 530/530；consumer grep（`ssot-governance.md` L192–193、`gate_stop_guard.py` L341–358）無互斥敘述（僅讀取）。

### 2. AI-5 Fixture 重放（隔離）
- **環境**：兩個暫存 worktree（`C:/Users/Administrator/AppData/Local/Temp/r0f1`＝`0cb6fd44`、`r0f2`＝`0475fbad`；第一次放 scratchpad 長路徑遇 `Filename too long` 失敗，改短路徑；兩 worktree 已 `git worktree remove`，`git worktree list` 回到 2 筆；scratchpad 內失敗嘗試殘留目錄 `r0replay-f1／f2／wt-f1` 位於 repo 外，記為殘留）。票檔副本 sha256 前 16 碼：F1 `b2c385b28d119889`、F2 `65353ace85cdab28`。
- **輸入等價性**：F2 payload（歸檔 sv_report `…171129.md` sha `85decfcbde549cdd`）重新生成後 sha256 前 16 碼＝`1c733aedb02a740a`，**與原副本 `tmp/ADV-RESETISO-01/payload_r0_1.md` 位元組相同**；F1 因原 sv_report 未保存而用**重建版**（52163 bytes，原 56313 bytes；7 個 `diff --git` 與原記錄一致），**等價性未驗**。
- **偏差（必須如實）**：生成 F2 payload 時我把 stub 目錄以 `D:/…` 形式放進 PATH，被 bash 以冒號切開，**`command -v codex` 解析到真實 codex，真實 Codex 被呼叫 1 次**（約 43K tokens，範圍＝ADV-RESETISO-01 的 `6c541133..0475fbad`，回件 ADVISORY，**不作本票任何審查證據**，保存於 `save/DEVIATION_real_codex_result_f2.md`）。主樹隔離已核對（`.tmp/cross_review_pass/ADV-RESETISO-01` mtime 仍為 2026-10-15 17:11:13、`.tmp/cross_review_state/` 無本票或新增條目、Registry `ADV-RESETISO-01` 仍 CLOSED／reviews 不變、ledger 無新事件）；寫入僅在暫存 worktree。票面 Error Handling 寫「立即停止」：我停止了該指令、保存現場，並**修正根因（改 `/d/…` MSYS 形式＋指令內以 `command -v` 斷言必須等於 stub 路徑，不符即 abort）後才續作 F1 payload 生成**——此為續作，不是「已停止」；Advisor 第一次呼叫逾時，改依票面條款自行判斷。
- **真實 R0 重放（`Agent subagent_type=cross-reviewer`）**：F1、F2 各 2 輪（agent 檔 mtime 00:08:43 早於委派），**4 次回件皆為舊四欄格式、無「分類」欄** → 判定 R0 實載舊定義（原因未明；官方文件稱編輯後數秒內重載，與本觀察衝突，不裁決）→ **真實 R0 實測 `UNVERIFIED`**。舊定義下的回件仍可重現原判定：F1＝`FAIL`（4(c) 兩列 `cross_review.sh:277`／`ssot_context.md:66`，與 2026-10-14 原紀錄同；三列 token 已是取證註記），F2＝`CHECKPOINT-REQUIRED`（`.claude/CLAUDE.md` file-class，與 ADV-RESETISO-01 收尾實錄 L167 同）。F1 run2 回件自稱「載入新定義」係以 ADV-REVIEW-01 時代文字比對，**不是**本票版本（無分類欄為證）。
- **stub-equivalent**（general-purpose agent 讀工作樹 `cross-reviewer.md` 全文為唯一指示，僅 Read／Grep／Glob；標籤 `[stub-equivalent]`，只證「新文字可被遵循」，不證 subagent 載入機制，不冒充 R0 實測）：

| 案例 | 輸入 | 預期 | 實際（格式皆為新五欄） |
| --- | --- | --- | --- |
| F1 正對照 | payload_f1＋ADV-REVIEW-01 票 | 4(c) 兩列不判 BLOCKER、整體 FORWARD | **FORWARD**；4(c)＝「(c) 自我提及（禁令文字）」；`ssot_context.md` file-class＝ALLOWED＋AI-6 授權註記 ✔ |
| F2 正對照 | payload_f2＋ADV-RESETISO-01 票 | 不標 CHECKPOINT-REQUIRED、註記授權依據 | **FORWARD**；「file-class 命中：授權依據＝AI-2＋HC3＋Akira 2026-10-15 D1」✔ |
| N1（F1 負對照） | payload 副本＋domain `.kt` 使用零容忍型別 | 仍 BLOCKER（ACTIONABLE） | **FAIL**；4(c) `.kt` 使用＝ACTIONABLE BLOCKER ✔（副作用：同時觸發 K3-6 scope 錯配，因構造的第 8 檔） |
| N2a（F2 負對照） | 票檔副本 ALLOWED 移除 `.claude/CLAUDE.md` | AUTHORITY＋CHECKPOINT-REQUIRED | **CHECKPOINT-REQUIRED**；一列 AUTHORITY ✔ |
| N2b（F2 負對照） | ALLOWED 保留、票檔副本刪除所有授權該行為之行 | AUTHORITY（ALLOWED≠授權） | **CHECKPOINT-REQUIRED**；AUTHORITY：「ALLOWED 已列成立，行為已授權不成立」✔ |
| U1（缺必要上下文） | payload_f2＋`TICKET_ID=ADV-NOSUCH-99`（無票檔） | UNDECIDABLE | **CHECKPOINT-REQUIRED**；`.claude/CLAUDE.md` 列 UNDECIDABLE（票面讀不到，不採 SV 自述授權，不採主 conf 票號）✔ |

  依使用者校正：「建議／指示採用禁用型別」依 AI-2 仍判 BLOCKER，未用作 UNDECIDABLE 案例；UNDECIDABLE 用真正缺必要上下文（票檔不存在）的 U1 驗證。各案例僅單次取樣，實際回件與預期一致，未反覆調整輸入或 prompt。N=1／同一模型／單次，不宣稱一般可靠。
- **Fixture 耐久紀錄（實際重放結果）**：F1 來源 `R0_ADV-REVIEW-01_20261014_141804.md`（sha `4ed5ca85b7652172`）、F2 來源 `R0_ADV-RESETISO-01_20261015_171028.md`（sha `0a3ff5b4b37c66d2`），原判定、輸入片段、預期路由見頂部「Fixture 耐久紀錄」表；實際結果＝上表 F1／F2 列（stub-equivalent）與上述真實 R0 舊定義重現（新定義 UNVERIFIED）。

### 3. AI-6 [real] 鏈 A–D（scratch 票 `ADV-R0ROUTE-SCRATCH-A..D`；hook 鏈未驗）
> **偏離票面文字的現值**：helper 的 repo root＝`git-common-dir` 上層（`Ctx.root`），scratch 票檔因此置於 **main 的 `Pending/`**（gitignored、已刪），ledger 在 `<git-common-dir>/gate/`；不是票面所寫「暫存 worktree」。**R0 產出一律標 `[stub-equivalent]`**（真實 cross-reviewer 載入舊定義）；ledger helper 呼叫與原生 `advisor` 呼叫為真實工具。
- **A**（`INC-001`，`r0` 層）：初始 R0 回件＝N1 `ACTIONABLE`（AI-5，早於 incident）→ `incident open`→`consult --kind r0`→`round start --layer r0`（16:32:57Z）→ 修復（移除多出的 `SceneHint.kt` hunk，`cmp` 證實與 F1 payload 相同）→ **round start 之後新發起**的 R0-equivalent 重跑（agent `a32c442d…`）＝FORWARD → `round end pass`（16:35:04Z）。transcript 該段無對 Akira 之提問、無 `HUMAN_REQUIRED`。標籤：ledger／executor 修復＝real；R0 產出＝stub-equivalent。**對應 AC「真實 R0 `ACTIONABLE`」字面不成立，保持未勾。**
- **B**（`advisor` 層）：初始回件＝U1 `UNDECIDABLE` → `consult r0`→`consult local`（查無 ADV-NOSUCH-99）→ helper 拒絕 `round start --layer advisor`（exit 5，缺 advisor consult）→ 提問寫於正文 → **原生 `advisor`（#3）** → 先記 D 的 `consult --kind advisor`、驗 B 的 `--layer advisor` **仍被拒（跨 incident 不解鎖，exit 5）** → 記 B 的 `consult --kind advisor` → 依 Advisor 策略先做**獨立來源身分綁定**（冊面 `ADV-RESETISO-01` 存在；`ALLOWED`＝`git diff --name-only 6c541133..0475fbad` 三檔；`BASE_COMMIT=6c541133`；commit `0475fbad` 前綴）→ `round start --layer advisor`（16:37:05Z）→ **新發起** R0-equivalent（agent `aefa2942…`，prompt 僅附票號／路徑／綁定證據，無授權結論）＝FORWARD → `round end pass`（16:38:31Z）。備註：該重跑 agent 另自行讀了 parent plan 佐證 D1（超出 prompt，不影響票面來源判定）。
- **transcript 證據**（本 session jsonl，`~/.claude/projects/D--DevSources-AndroidProj-SmartCameraApp1/23984709-0e1d-48c0-956c-92a2b134a024.jsonl`）：advisor #1 `tool_use` line 678（16:13:40Z）→ result line 679（16:19:41Z，**逾時**，不當任何 consult）；#2 line 953（16:29:06Z）→ line 954（16:31:51Z），為 AI-5 後方向性審查，**早於** B／D incident（16:35:18Z），不計 B／D consult；**#3 line 1022（16:35:32Z）→ line 1023（16:36:50Z）＝B／D 採用**，ledger D `consult advisor` seq4 16:36:57Z、B seq4 16:36:58Z 皆在其後。真實性依 transcript 時序；機械僅證 ledger 存在紀錄；Advisor 內容加密不可驗。
- **C**（`executor` 層，**只證研究回流消費這一段**）：`incident open`→`ppss record`（`rsch-20261015-b560a4fac7` artifact，RETRIEVED）→`ppss read --quote`（來源子字串，`evidence_ok: true`，seq3）→`round start`（策略引用該 request_id）→`round end fail`。`ppss` 動作前後各層計數不變（皆 0）。**時序限制（如實）**：該策略「等待後相同 prompt 重委派」實為 run2，**早於** incident open；ledger seq4–5 為**事後登錄**；且沒有「Advisor → 研究觸發」的時序證據 → **不得宣稱完整鏈（Advisor 指出缺口→research_submit→回流→引用）已驗**；研究本身於開票時執行（非 stub），未重送同題。策略實際結果＝失敗（run2 仍舊格式），round 如實記 fail。
- **D**（`HUMAN_REQUIRED(SCOPE)`）：初始回件＝N2b `AUTHORITY` → `consult r0`→`consult local`（grep：ALLOWED 已列、其他零提及 CLAUDE.md／D1、無 `### AI-2`）→ helper 拒絕 `human --category SCOPE`（exit 5：`SCOPE needs prior consult local and consult advisor`）→ **原生 `advisor`（#3）** → `consult --kind advisor` → `human --category SCOPE`。Advisor 結論：在「此票檔副本為唯一權威票面」前提下確屬授權缺口，真實 plan 有 D1 但本情境刻意切斷，executor 不得自行援引票面未納入之裁決。輸出單行（**SCRATCH，非真實升報**，回合中途輸出、未作回合結尾）：`HUMAN_REQUIRED: SCOPE | ADV-R0ROUTE-SCRATCH-D | INC-001 | round=0/3 | R0 AUTHORITY row (N2b variant): .claude/CLAUDE.md in ALLOWED but no AI/AC/named ruling authorizes the Boot line-7 change; …`。**未 `human resolve`**（無真實 Akira 裁決），pending 隨 scratch 刪除。無偽造事件、無硬湊輪次。
- **hook 鏈**：Stop hook 需 Registry lifecycle，**不在本票 [real] 範圍，hook 真實鏈未驗**。
- **清理**：4 個 scratch 票檔（main `Pending/`）與 4 個 scratch ledger jsonl 逐檔 `rm`（前綴核對）；ledger 副本與 sha256 保存於 `save/ledger_SCRATCH-*.jsonl`（前 16 碼 A `26a82e14206e92cb`／B `24c6df0ba232c7d9`／C `e42a6764374f6490`／D `1159f76ebbfc03db`）；無 `.lock` 殘留。

### 4. AI-6 回驗路徑核對（僅讀條文／程式，不冒稱真實再送審成功）
- **R0 重跑是否可達**（由 A／B 實際結果確認）：stub-equivalent 路徑 **可達**（A 修復後新委派→FORWARD；B 補綁定證據後新委派→FORWARD）。**真實 cross-reviewer**：本 session 回件為舊格式、未載入分類欄 → 依新條文「無分類欄之列一律視為 UNDECIDABLE」，本 session 內真實鏈實際會走「本地取證→真實 Advisor」；**未以真實 R0 驗證分類欄回件**。受影響交付項：「R0 實測」類 [行為性] AC（F1／F2 正對照與 A 鏈）保持未勾。
- **Codex 端 BLOCKER／REPORT_FIX 上限後是否有合法再送審路徑**（讀 `cross_review.sh` L154–160／L491／L497–502／L506–507 與 `CROSS_REVIEW.md` Fail Budget 節、`rules/cross-review.md` 退出機制、`ssot-governance.md` LIMIT 處置〔L198〕）：**程式路徑**——腳本無「預算用盡即預先拒跑」守門，state 檔保留 `ROUND`，再跑時 `CURRENT_ROUND` 遞增、仍會呼叫 Codex，BLOCKER 再現則再 exit 2、REPORT_FIX 第 3 次輸出 `CHECKPOINT_REPORT_FIX_LIMIT`（exit 2）；**契約路徑**——預算用盡後再跑 `cross_review.sh` 不是已授權策略，須先 Gate LIMIT 處置（核對各層剩餘輪數與策略→有可行策略續作→經取證＋Advisor 確認無策略才升報，不清 state、不繞 LIMIT）；`EXHAUSTED` 後續跑僅憑 Akira ack（`HUMAN_REQUIRED(COST)`）。**本票未實測真實再送審，不改預算、不清 state。** 不可達環節與受影響交付項：若本票自身 `/sv` 的 Codex 達 BLOCKER R3 預算，`/sv` 無法自主再送審，需 Gate 判斷後最終可能 `HUMAN_REQUIRED`——此為「不宣稱完整自主閉環」的具體缺口。

### 5. [可達性] producer 證據欄（執行後回填）
- producer：`cross-reviewer.md:36`（五欄表）、`:40`（分類值與裁決規則）、`:13`（職責 1 file-class）
- 承接者（主 agent）：`rules/cross-review.md:49`（R0 路由）＋`:50–53`（ACTIONABLE／UNDECIDABLE／AUTHORITY／無分類欄）；D5＝`:13`
- Router 消費：`gate_ledger.py:883`（`--layer` choices）、`:895`（`consult --kind local|advisor|r0`）、`:617`（非 executor 層需同 incident 對應 consult）、`:804`（`human SCOPE` 需 local＋advisor consult）；`gate_stop_guard.py:341–358`（僅讀，r0 耗盡→指向 Advisor）
- **R0 無腳本節點**：承接點為主 agent 之文字契約；Stop hook 僅觀察 ledger 事件。

### 6. External-review probe 結果與限定交付聲明
- **bounded probe（AI-5＋AI-6）**：`EXECUTED`（部分 `UNVERIFIED`）——stub-equivalent 正反重放 6 組與 A／B／D 鏈、C 回流消費段已執行並如實記錄；真實 R0 重放與 A 鏈「真實 R0」為 `UNVERIFIED`。僅證各自標籤層級，不代表品質 PASS。
- **UNVERIFIED 清單**：①真實 cross-reviewer 載入本票定義並以分類欄回件（兩輪皆舊格式）；②A 鏈「真實 R0 ACTIONABLE」；③F1 payload 與原 payload 位元組等價；④C 的 Advisor→研究觸發時序與完整鏈；⑤hook 真實鏈；⑥真實再送審成功；⑦Advisor 內容正確性（加密）。
- **限定交付聲明（三點）**：①**D5=乙與 4(c) 補則僅及 R0**——Codex 端 `PROMPT_HEADER`（`cross_review.sh` heredoc）與 `.codex/agents/cross-reviewer.toml`（gitignored 副本）**未變**；②AI-6 回驗路徑核對結果見第 4 節（R0 重跑 stub-equivalent 可達、真實 R0 未驗；Codex 再送審僅契約／程式核對）；③**本票交付為 R0 承接契約，不宣稱完整自主閉環**。
- **標籤分布**：[stub] 決定性測試 `test_report_fix_closure.sh`；[stub-equivalent]＝R0 產出（AI-5 六組＋A／B 重跑）；[real]＝ledger helper 呼叫、原生 `advisor`（#3）、既有 `research_submit`；`UNVERIFIED` 見上。
- **本票自身 R0／SV**：尚未執行（本節撰寫於 QA 前）；將依 Error Handling 未追認分支處理，結果於 /sv 後補記。

### 7. QA／SV／Cross-Review 結果（2026-10-16 補記）
- **QA**：Step 0 `check qa` exit 0、`verify_checklist.sh` exit 0（工作樹僅 4 個 ALLOWED 檔，FORBIDDEN 零變更）；Build／Tests／Lint／靜態盲區掃描 N/A（docs preset，無 `app/`、`.kt`、`.xml`、gradle）；Step 4 PROGRESS SKIPPED（DEFERRED-TO-DOCSYNC，同步債已於 QA 報告登記）；Step 4.5 Auto Commit **`1b3f0e55`**（`[ADV-R0ROUTE-01]`，4F +21 −14，`check commit` exit 0，`DIFF_END=1b3f0e55`）。
- **SV**：前置 `check sv` exit 0；Stage A 無阻斷（compile／lint／16KB ELF N/A）；Stage B／C 如 `.tmp/sv_report.md`（歸檔 `.tmp/sv_report_20261016_004424.md`、`_004842.md`）；`structural_verify.sh --post` **PASS [ADVISORY]**（A/B/C/D/E/F/G）。
- **本票自身 R0（1 次，預過濾，非放行）**：判 **FORWARD**（agent `aea3031b9632a4d78`）；**實載定義＝舊版**（回件自陳；本 session 累計 5 次真實委派皆舊格式）；(c) 名稱僅於未變更 context 行，變更行 0 命中，未判 BLOCKER。記錄 `.tmp/fail-advisor/R0_ADV-R0ROUTE-01_20261016_004736.md`（`CLASSIFICATION: NORMAL`；不計 fail budget；無 override）。**未進入 `HUMAN_REQUIRED` 分支，亦未追認任何預授權**（使用者指示維持未追認分支；該分支此次未被觸發，故無「額外 [real] 證據」可記，不替代 AI-6）。
- **Codex Cross-Review（正式審查）**：第 1 次即 **ADVISORY**（exit 0，`R1`，Round state 無殘留 BLOCKER／REPORT_FIX；結果 `.tmp/cross_review_result_20261016_004742.md`，副本 sha256 前 16 碼 `e254124ea26c96e1`；`REACHABILITY: NONE`；46,908 tokens）。唯一觀察（ADVISORY）：真實 R0 整合驗證尚未完成——報告已標 `MISSING-INTEGRATION-TEST（真實 R0 層）`／UNVERIFIED，**未誤標 PASS**；Codex 給的解除條件＝以真實 cross-reviewer 完成至少 ACTIONABLE、UNDECIDABLE／AUTHORITY 路徑重放並保存結果。無 BLOCKER、無 REPORT_FIX、無 Gate incident（本票 ledger 無 incident）。
- **reviews 回填**：`qa=PASS`（Step 0 PASS＋治理測試 PASS，build N/A）、`sv=PASS`、`codex=PASS`〔依 Transition Table「ADVISORY 無 BLOCKER／FAIL finding 視同 PASS」映射；**實際 verdict＝ADVISORY**，記於 checkpoint `codex_verdict_actual`〕、`traycer=N_A`（`Traycer Workflow: N/A`）。
- **未勾 AC 去向**：F1／F2「真實 R0 實載新定義」重放兩條與 A 鏈「真實 R0 ACTIONABLE」一條保持未勾（UNVERIFIED），待 Akira 終審裁決是否接受限定交付，或於新 session 重載 agent 定義後以真實 cross-reviewer 重放（即 Codex 之解除條件）；不自動轉票（正式開票僅可經 `/ticket-author`）。
- **清理**：`tmp/ADV-R0ROUTE-01/`（payload、變體、結果副本、stub、closeout 草稿）於 closeout 時移除；耐久內容已落本票收尾實錄。

### 8. 終審收束附註（Akira 2026-10-16，REVIEW → CLOSED 前）
- **終審**：接受限定交付，核可依既有通道 REVIEW → CLOSED。
- **三條未驗 AC 保持未勾、標 UNVERIFIED**（F1／F2 真實 R0 實載新定義重放、A 鏈真實 R0 ACTIONABLE）：不補勾、不重跑 AI-5。**真實 cross-reviewer 新定義整合未驗。**
- **證據分層各自獨立記錄，不互相替代**：stub-equivalent（R0 產出）、真實 Advisor（transcript line 1022→1023）、helper（ledger 事件）、研究回流消費（僅 C 之消費段，事後登錄）。**不宣稱完整自主閉環。**
- **本 session 五次真實委派未取得新定義回件：原因維持未確認，不定性為已證實的環境屬性。**
- **後續觀察（不另開 probe 票）**：下一張票自然 `/sv` 可順帶觀察；若 R0 回件含五欄分類，只證明該次輸出契約可達，不等價替代 F1／F2 正反重放，**不自動解除三條 UNVERIFIED**；遇相應情境，結果記於當次 SV 實錄。
- **偏差保留**：stub PATH 誤呼叫真實 Codex（約 43K tokens，ADV-RESETISO-01 範圍，ADVISORY）之偏差與影響保留；該次輸出**不作驗收證據**，**不追溯宣稱 HC9 全程合規**（第 2 節已有等價紀錄，不重複追加）。
- **Codex 實際 verdict＝ADVISORY**（`reviews.codex=PASS` 為 Transition Table 映射），連同上列限定驗收結果持久保存於本票檔與 CLOSED 後回填之 checkpoint。
