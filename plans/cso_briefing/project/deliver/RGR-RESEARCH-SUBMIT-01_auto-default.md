# RGR-RESEARCH-SUBMIT-01: REPAIR_ROUTING.md 新增通用自主修復預設與 Research Fallback 條款（獨立軌道，不擴充 RGR-AUTO-01、不建新 runner 基礎設施）

檔案治理／來源說明：本票依 TPL `Unified_Ticket_Template_v3.md` v3.11 產出。簽發依據為 Akira 2026-09-22 對話裁決直接授權開票並執行（同對話串內連續三則指示：授權範圍界定 → 前置文件澄清「SSOT_Evidence_Collection_Spec 為外審包規範，本票免依」→「繼續上述」執行指示）。本票為其自身之 dogfood 對象：草擬與實作同一回合內，Claude 依本票新增之兩條行為法自行判斷是否直接修、是否呼叫 `research_submit`、是否停止回報 exception。

**與 `RGR-AUTO-01` 之關係（明確切割，非擴充）**：`RGR-AUTO-01` 之 `single_attachment_live_lane` 為 **runner 驅動**（`repair_runner.sh` 呼叫 Codex CLI、schema 驗證、`PERMITTED_REPAIR_CLASSES=A-1,A-2` 封閉集）之窄範圍 carve-out。本票新增的兩節是**完全獨立的第二軌道**：適用於 Claude Code（或具直接工具存取權之對等執行者）**即時／人工模式**執行本 skill 審查時的行為預設，不經過 `repair_runner.sh`、不消費其 schema、不讀取其 `PERMITTED_REPAIR_CLASSES` 或 `live_binding_passed`。`SKILL.md:347`「Eligible repair MUST be invoked only through the project-owned non-interactive repair runner」一句所拘束的是 Advisor Repair Mode 之 runner 生產路徑，本票不修改、不牴觸該句——本票的兩節是該句管轄範圍**之外**的另一條軌道。

**與既有 Guardrail 之明示範圍關係**（AI-1 落地時逐條寫入 `REPAIR_ROUTING.md`，此處先行記錄裁決）：
- Repair Whitelist「the list is closed, not illustrative」（`REPAIR_ROUTING.md` L13）——本票新增之通用預設不進入該封閉列表，亦不使其變成開放列表；它是列表機制之外的另一套判準，僅適用於本票定義的獨立軌道。
- Guardrail 1（pre-signature only）——本票新增預設之「既有修改權限」定義為**當前任務已生效之 ALLOWED 範圍**（不論該任務票是 pre- 或 post-signature），因為它從不核發新權限、只決定同一權限內「直接修 vs 停下等確認」；不與 Guardrail 1 之字面衝突，亦不重啟已簽發票證文本可否編輯之爭議。
- Guardrail 2（per-class shadow calibration／`OPS_CALIBRATION.md` Phase Promotion）——**本票明示不繼承、不豁免、不觸碰**此機制；本票新增之通用預設沒有「class」可供計數，因此從第一天即以 Akira 本次對話之直接裁決為授權來源（非計數達標），與 Guardrail 2 之「累計達標後晉升」哲學並存但分屬不同法源。此點列為 AC 下方之 Exception 案例，見 Action Items 末段與最終報告。

**簽發日期**: 2026-09-22
**簽發者**: Claude（Akira 2026-09-22 對話裁決直接授權）
**來源議題**: RGR
**Mode**: uc  ||  **Effort**: Medium
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: 無
- `coordination_dependency`: 無

## Verify Config
TICKET_ID=RGR-RESEARCH-SUBMIT-01
CREATED=2026-09-22
PRESET=docs
ALLOWED=.claude/skills/ticket-rigor-gate/SKILL.md,.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md
FORBIDDEN=.claude/skills/ticket-rigor-gate/runner/repair_runner.sh,.claude/skills/ticket-rigor-gate/runner/schemas/ticket-repair-payload.v1.json,.claude/skills/ticket-rigor-gate/runner/schemas/ticket-repair-response.v1.json,.claude/skills/ticket-rigor-gate/OPS_CALIBRATION.md,.claude/hooks/*,.claude/settings.json,.claude/settings.local.json,plans/cso_briefing/project/TICKET_REGISTRY.json
MAX_LINES=60
DIFF_REF=f78337e
ANCHOR_PROBED=2026-09-22 13:49
BASE_COMMIT=f78337e77e0c9f0d2ccea3a049290c8998e4f754
TARGET_BLOB=.claude/skills/ticket-rigor-gate/SKILL.md:67800867,.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md:c4b286e3
INTRODUCED_CAPABILITY=skill:general_autonomous_repair_default
DEPENDENCY_DELTA=NONE

**`INTRODUCED_CAPABILITY` 誠實揭露**（fail-closed 欄，MUST 顯式填寫）：本票引入的是 skill 層新行為（非套件/工具依賴），依規定不得填 `NONE`。`2026-09-11_RigorGate_Autonomous_Boundary_Mechanical_Preflight_Adjudication.md` 先例所述之「固定 corpus 零命中 → 觸發外審條件 6a → 執行票停止開立」為**外部審查流程**（Perplexity 回流、Craft 外審包），其 corpus 查核機制不在本 repo 內、Claude 無本地工具可執行該查核。本票如實記錄此欄無法本地機械驗證 6a 是否觸發，不代為宣稱「已查無需外審」；若後續 SMTR／Akira 判定本票應走同一外審流程，本票之落地（`REPAIR_ROUTING.md`／`SKILL.md` 兩節文字）屬可逆變更（git revert 即可撤銷），不構成既成事實。

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: `.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md`, `.claude/skills/ticket-rigor-gate/SKILL.md`
- **動作**: 確認 `REPAIR_ROUTING.md` 現值 blob hash 與 `TARGET_BLOB` 相符；確認 `SKILL.md:345` 現值字面（「Repair routing law — the fact-layer whitelist, the guardrails, and the Post-Signature Erratum Lane — is legislated in `REPAIR_ROUTING.md`.」）仍存在且唯一命中。
- **任一項不符 → 停止回報，禁自行推定等價位置。**
- [x] 完成 — `REPAIR_ROUTING.md` 修改前 blob=`c4b286e3`（與 `TARGET_BLOB` 相符）；`SKILL.md:345` 現值字面唯一命中，修改前核對通過。

### AI-1: REPAIR_ROUTING.md 新增「## General Autonomous Repair Default」節
- **檔案**: `.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md`
- **動作**: 於檔案末尾（`Cross-Review Execution Leg` 節之後）新增一節，陳述：適用範圍（僅限即時／人工執行本 skill 之獨立軌道，明示不經 `repair_runner.sh`、不繼承 `PERMITTED_REPAIR_CLASSES`、不依賴 `RGR-AUTO-01`）；四條件測試（任務範圍內＋既有權限內、修法可合理判定為事實層、可逆或風險有限、存在相稱驗證法）；四條件皆成立時直接修復並驗證、禁逐筆等待同意、成功僅於最終報告列出；任一條件不成立（決策層／需求／政策／SSOT 裁決／多種不可互換方案／不可逆或高風險）→ 停止回報 exception；明示不新增 Repair Ledger 義務、不新增 queue／webhook／notifier／retry。
- [x] 完成 — 節已附加於 `REPAIR_ROUTING.md` 檔尾（`Cross-Review Execution Leg` 之後），含「Open question, not resolved here」段落記錄 Guardrail 2 計數哲學之未決張力。

### AI-2: REPAIR_ROUTING.md 新增「## Research Fallback」節
- **檔案**: `.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md`
- **動作**: 緊接 AI-1 新節之後，新增本節，陳述：觸發條件（local evidence 不足、疑義屬公開外部技術規格／官方 API-SDK contract／平台行為／vendor policy）；呼叫既有已註冊 MCP 工具 `research_submit`，一次、最小、去敏（不含 secret／token／credential／私有 URL／絕對路徑／完整 diff／完整票面／customer data／私有原始碼）；明示呼叫為**同步**（`results[]` 隨同一次回應返回，無 submit-then-notify／queue／webhook／poll，亦不得為此新建之）；結果併回 AI-1 之四條件測試；於最終報告記錄 question 與 `request_id`/`run_id`；失敗僅記入最終報告，不阻擋其他 finding、不重試；不新增 dedup ledger／allowlist／DLP filter。
- [x] 完成 — 節已附加於 AI-1 新節之後；同步條件與失敗語意逐字陳述。

### AI-3: SKILL.md pointer 句同步（doc drift 自我修正）
- **檔案**: `.claude/skills/ticket-rigor-gate/SKILL.md`
- **現值**（第 345 行）: `Repair routing law — the fact-layer whitelist, the guardrails, and the Post-Signature Erratum Lane — is legislated in \`REPAIR_ROUTING.md\`.`
- **目標值**: 同句尾端補列 AI-1／AI-2 新增之兩節名稱，使 pointer 句完整列舉 `REPAIR_ROUTING.md` 現有全部法域，不留過期清單（此為本票自身造成之孤兒引用，依「Autonomous Repair Default」定義之第一個可驗案例：任務範圍內、既有權限內、事實層可判定、單行可逆、驗證法＝逐字核對句尾清單完整性——直接修，不等確認）。
- [x] 完成 — `SKILL.md:345` 句尾補列「the General Autonomous Repair Default, and Research Fallback」，零逐筆確認直接修正。

### AI-4: Research Fallback 實測呼叫（本票自身之外部疑義）
- **標的疑義**: `REPAIR_ROUTING.md` 現行 Tooling Note 對 Claude Code skill frontmatter 行為（`allowed-tools` 非限制性、`disallowed-tools` 限制性、subagent `tools`/`disallowedTools` 優先序、`context: fork`）之陳述標註「live-verified 2026-08-19」，距今逾一個月；AI-1 新節之「無 live 技術阻擋，僅靠 discipline」誠實揭露沿用同一事實基礎，需重新核實。
- **動作**: 呼叫 `research_submit`，`question` 與 `queries` 均不含本 repo 私有路徑／票號／內部欄位名，僅問公開 Claude Code 文件對 skill frontmatter 工具存取限制之現行行為；`metadata` 可含 `{"ticket_id": "RGR-RESEARCH-SUBMIT-01"}` 供可追溯性。
- **動作**: 將回傳結果（或維持現狀之結論）與 `request_id`/`run_id` 記入最終報告；若結果與現行 Tooling Note 陳述不一致，依 AI-1 四條件測試判斷是否直接修正 Tooling Note 文字。
- [x] 完成 — `request_id=rsch-20260922-8ce11e5279`／`run_id=run-6aafdf05b7a6`。官方 Claude Code Docs（`code.claude.com/docs/en/sub-agents`、`.../tools-reference`，last_updated 2026-09-16～2026-09-22）與 Claude Academy 課程逐字確認 Tooling Note 現行陳述四點皆準確：skill `allowed-tools` 僅預先核准、非限制；skill 層 `disallowed-tools` 為真限制；subagent `tools`/`disallowedTools` 為真限制，`disallowedTools` 優先序在先。**發現一筆第三方來源衝突**（CodeSignal Learn，2026-05-19）宣稱 skill `allowed-tools` 為「enforced limitation」，與官方文件及既有 Tooling Note 矛盾；官方來源較新且為主要來源，判定 CodeSignal 為過期／錯誤之第三方描述，不採用，Tooling Note **不需修正**。

## Hard Constraints
1. 禁止新增或修改 Repair Ledger 欄位、佇列、webhook、輪詢、notifier 或 retry 機制；`OPS_CALIBRATION.md` 不得被本票間接擴充或依賴。
2. 禁止將 AI-1 新預設縮限為僅 A-1/A-2、`single_attachment_live_lane` 或任何預列舉 finding 類型；MUST NOT 讀取或依賴 `RGR-AUTO-01` 之 `PERMITTED_REPAIR_CLASSES`／`live_binding_passed`。
3. 禁止新增 batch-approve 流程、第二審查模型呼叫，或本票兩節以外之任何第三項能力。
4. AI-2 之 `research_submit` 呼叫語意 MUST 陳述為同步（單次呼叫即拿結果），禁止撰寫任何隱含非同步等待／通知／輪詢語意之文字。
5. 禁止撰寫具體數字上限（如「≤3 queries」）作為 MUST 規則——平台層 `maxItems=5` 為唯一機械上限，文字僅得寫「最小、去敏」之原則性描述。
6. 不得以「缺乏官方來源」作為停止研究或斷定不可行之理由；MUST 先嘗試 Research Fallback（AI-2／AI-4）。
7. 禁止修改 `RGR-AUTO-01` 或 `RGR-SCOPE-BIND-01` 之票檔本文或 lifecycle。

## Error Handling
- AI-1／AI-2 文字若無法在條文內自洽陳述與既有 Repair Whitelist／Guardrail 1/2 之範圍界線 → 停止回報（禁默默各取一半，CLAUDE.md Rule 11）。
- AI-4 之 `research_submit` 呼叫若回傳 provider／cap／validation 錯誤 → 記入最終報告，不重試、不阻擋 AI-1／AI-2／AI-3。
- 令含 commit／版控標的時，`git check-ignore -v <標的路徑>` 命中（標的被 .gitignore 排除）→ 停止回報（gitignore 死結律，候選 #20）。

## Acceptance Criteria
- [x] **[存在性]** `REPAIR_ROUTING.md` 新增「## General Autonomous Repair Default」與「## Research Fallback」兩節，且明文陳述其與 Repair Whitelist（L13）、Guardrail 1、Guardrail 2 之範圍關係（獨立軌道，不擴張、不繼承、不豁免）。
- [x] **[存在性]** `SKILL.md:345` 附近之 pointer 句同步列出新增兩節，不留過期清單。
- [x] **[行為性]** 本票執行過程中，AI-3 之 doc-drift 修正被直接完成且零逐筆人工確認，於最終報告列出已修內容與核對方式（逐字核對句尾清單完整性）。
- [x] **[行為性]** AI-4 成功呼叫一次 `research_submit`（去敏、最小 queries），於最終報告記錄 question 與 `request_id`/`run_id` 作為 reference。
- [x] **[行為性]** 本票執行過程中，Guardrail 2／`OPS_CALIBRATION.md` 計數哲學與本票通用預設「零計數即生效」之關係，被正確判定為 exception（決策層，非事實層）並於最終報告停止回報，未被自行裁定為「顯然可略過」——見 `REPAIR_ROUTING.md`「General Autonomous Repair Default」節末「Open question, not resolved here」段落。
- [x] `git diff --stat` 僅涉及 ALLOWED 檔案（`SKILL.md`、`REPAIR_ROUTING.md`）——實測 `2 files changed, 30 insertions(+), 1 deletion(-)`，零意外檔案。
- [x] ~~assembleDebug PASS~~ (N/A: docs preset)
