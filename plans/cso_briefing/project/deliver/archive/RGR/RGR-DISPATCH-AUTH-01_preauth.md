# RGR-DISPATCH-AUTH-01: 立法「有效預授權命中時 Host 必須放行，禁逐案諮詢」

檔案治理／來源說明：本票依據 2026-09-18 本機 live read（`REPAIR_ROUTING.md`、`OPS_CALIBRATION.md`、跨 repo `ticket-quality-skill-codex-runner-contract.md` L5／L31、`repair_runner.sh` L108／L605–609）建立，依 `Unified_Ticket_Template_v3.md` v3.11 產出。本票為 doc-only 立法票，零程式異動。

**簽發日期**: 2026-09-18
**簽發者**: Claude（SKAD）
**來源議題**: RGR
**Mode**: normal  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: `RGR-REPORT-ANCHOR-01`（commit `780b079`，已 deliver）——預授權之 receipt、breaker 與歷史證據須先有不受 CWD 影響之穩定落點，方得擴大自主 apply 之使用面
- `evidence_dependency`: 跨 repo contract L5／L31；`REPAIR_ROUTING.md` Guardrails 1–2、Post-Signature Erratum Lane；`OPS_CALIBRATION.md` Phase Promotion Criteria
- `coordination_dependency`: `RGR-P0A-02`（本票為其法源；本票條文定案前，P0A-02 之欄位語意不得凍結）

## 問題陳述

現行法未定義「授權如何產生」。跨 repo contract L5 規定 runner「只執行**已授權**的修補任務」，L31 將「決定是否允許 apply」列為 Host 職責，但兩處皆未規定 Host 在何種條件下**必須**放行。缺口造成：正確修法已由權威來源唯一決定、target 在 scope 內、後驗可機械執行時，Host 仍可能逐 finding 詢問 Akira。

`repair_runner.sh` 本身無任何互動式詢問（全檔無 `read -p`），且已具自主寫盤能力（`RGR-P0A-01-FIXTURE-POSTFIX` 之 `Apply Status: applied`）。唯一人工旗標 `requires_human_decision`（L605–609）為**模型自報**，僅於 `disposition=propose_repair` 時生效——以自報承載授權語意，與 `RGR-MODEL-CONTRACT-01` 已裁退之 `run_summary` 自報同型。

**本票不改變**：fact／decision 邊界、A-1～A-4 closed whitelist、Out Of Range 清單、severity 上限、diff budget、frozen normative sentence、post-repair diff verification、Phase 0/1 門檻數值（N=10 @ 90%）、拒絕歸零規則。這些是預授權的防線，非待拆除之過度治理。

## 契約相容性判定（AI-0 MUST 復驗）

契約 L31 之限制欄字面為「不得在未經 contract 驗證下直接採信 Codex 輸出」——約束對象是**輕信 Codex**，非「Host 之決定不受上游法約束」。契約全文無「Host 之 apply 決定為不受約束之裁量」之明文。故本票條文落於 `REPAIR_ROUTING.md`（其開篇自述職責即 what may be repaired, **by whom**, under what boundary, through which lane），**不改跨 repo contract**。若 AI-0 讀出相反明文 → 停止回報，改開最小 contract Addendum 票。

## Guardrail 1 相容性（本票必須處理，否則執行端必撞牆）

`REPAIR_ROUTING.md` Guardrail 1「Pre-signature only」規定：票進 registry 後即離開 repair range。預授權之對象正是已簽發票中的 finding，表面衝突。

**解法（條文須明寫）**：預授權**由票本身於簽發時發出**，其授權基礎、target、後驗條件在簽發那一刻即已固定並受 L2 審視；它不是對已簽發票的事後 repair，而是簽發時一併授予的執行權。故預授權**不構成 Guardrail 1 之例外，而是落在其射程之外**。Guardrail 1 對「未經預授權之事後修補」維持全效。

## Verify Config
TICKET_ID=RGR-DISPATCH-AUTH-01
CREATED=2026-09-18
PRESET=docs
ALLOWED=.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md,.claude/skills/ticket-rigor-gate/OPS_CALIBRATION.md
FORBIDDEN=.claude/skills/ticket-rigor-gate/SKILL.md,.claude/skills/ticket-rigor-gate/runner/,.claude/rules/ssot-governance.md,.claude/PROGRESS.md,app/,plans/cso_briefing/project/deliver/
MAX_LINES=N/A
DIFF_REF=5ca39bec6ace1826a32202ff10837897b630e91e
ANCHOR_PROBED=2026-09-18 18:05
BASE_COMMIT=5ca39bec6ace1826a32202ff10837897b630e91e
TARGET_BLOB=N/A
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

**`MAX_LINES=N/A` 依據**：TPL v3.5 R3——標的為授權／fail-closed 判定類安全不變量。
**`TARGET_BLOB=N/A` 依據**：TPL v3.11 Desktop fallback（開票端無 blob-hash producer）；`BASE_COMMIT` 已填。
**`INTRODUCED_CAPABILITY=NONE` 依據**：本票不導入 feature／skill／tech token，僅於既有單一立法位置補一條放行條件；預授權之資料承載欄位由 `RGR-P0A-02` 處理。

## Action Items

### AI-0: 錨點復驗與契約相容性確認
- **檔案**: `REPAIR_ROUTING.md`、`OPS_CALIBRATION.md`、（唯讀）跨 repo contract
- **動作 A**: 確認 `REPAIR_ROUTING.md` 現含下列節，回填各節起始行號：`Repair Whitelist`（A-1～A-4 表）、`Out Of Range (always human)`、`Guardrails`（五條，第 1 條為 Pre-signature only、第 2 條為 Per-class shadow calibration）、`Post-Signature Erratum Lane`。
- **動作 B**: 確認 `OPS_CALIBRATION.md` 現含 `Phase Promotion Criteria (C-3 校準閘)`，其內容為「每 class 自 Phase 0 起，N=10 @ ≥90% 人工原樣接受方入 Phase 1；未達標前 proposal-only、MUST NOT 寫盤；任一拒絕歸零」。回填行號。
- **動作 C**: 唯讀 `D:/DevSources/Skill-Advisor/deliver/ticket-rigor-gate-skill/ticket-quality-skill-codex-runner-contract.md`，搜尋是否存在「Host 之 apply 決定不受上游法約束」之明文。**存在 → 停止回報**，本票改由 contract Addendum 承接。
- **動作 D**: `git rev-parse HEAD`、`git status --short`（判定範圍限本票二檔；他檔 dirty 僅記錄不阻擋）、`git check-ignore -v` 二檔（任一命中 → 停止）。
- [ ] 完成

### AI-1: `REPAIR_ROUTING.md` 新增 Dispatch Authority 節
- **檔案**: `.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md`
- **位置**: 置於 `Guardrails` 節之後、`Post-Signature Erratum Lane` 之前（AI-0 動作 A 回填之行號）。
- **目標值**: 新增一節 `### Dispatch Authority — Preauthorized Autonomous Apply`，內容 MUST 涵蓋且僅涵蓋：
  1. **定義**：預授權由票於簽發時發出，非事後 repair；因此落在 Guardrail 1「Pre-signature only」射程之外，而非其例外。Guardrail 1 對未經預授權之事後修補維持全效。
  2. **六項放行條件（全真方命中）**：①authority anchor 存在且可定位於 repo-readable source；②target 落於票面宣告 scope；③preimage 與宣告值相符；④proposal 之變更形狀落於宣告之 mutation shape；⑤required verification 已宣告且可執行；⑥當前無 breaker 或 contract block。
  3. **命中之後果**：Host **MUST** 放行 apply；runner apply → verify → 持久化 receipt → 續處理同批下一件；**MUST NOT** 就該 finding 逐案詢問人類。
  4. **未命中之後果**：**MUST NOT** 猜測、**MUST NOT** 以「安全起見」之自由文字詢問人類；輸出既有機械碼（RTR／CB）或依既有 Out Of Range／decision-layer 規則交還人類。
  5. **自報不承載授權**：response 之 `requires_human_decision` 為模型自報，於預授權命中時僅為 audit 欄，**MUST NOT** 單獨構成停點（與 `RGR-MODEL-CONTRACT-01` 對 `run_summary` 之處置同型）。
- **不動點**: fact／decision 邊界句、A-1～A-4 表、Out Of Range 清單、Guardrails 五條原文、Post-Signature Erratum Lane 原文、severity 上限句、technical enforcement 段——逐字不改。
- **禁**: 不得新增 dispatch status 狀態詞集、evidence class 分類、closed-set stop reason 集、rerun matrix、finding routing 欄位表。此類分類學為 KAIROS 候選，本票不立。
- [ ] 完成

### AI-2: `OPS_CALIBRATION.md` 加一句優先序
- **檔案**: `.claude/skills/ticket-rigor-gate/OPS_CALIBRATION.md`
- **位置**: `Phase Promotion Criteria (C-3 校準閘)` 節內，`proposal-only` 段落之後。
- **目標值**: 加入**一句**：命中 `REPAIR_ROUTING.md` `Dispatch Authority` 六項條件之 finding，**免除該 finding 之 Phase 0 proposal-only 限制**；未命中者一律回既有 Phase 0/1 法。門檻數值（N=10 @ ≥90%）、拒絕歸零、ledger 記錄義務均不變；預授權之 apply 結果**仍計入**該 class 之 calibration counter。
- **禁**: 不得調整門檻數值、不得改拒絕歸零規則、不得新增 phase、不得改 Calibration Coupling 節。
- [ ] 完成

## Hard Constraints
1. 本票為 doc-only：**不得**修改 `repair_runner.sh`、schema、prompt、profiles、fixtures 或任何程式檔。
2. **不得**改變 fact／decision 邊界、A-1～A-4 closed whitelist、Out Of Range 清單或 severity 上限——預授權只回答「已在範圍內者何時必須自動執行」，不擴大範圍本身。
3. **不得**新增五態 dispatch status、evidence class、closed-set stop reason、rerun matrix 或 finding routing 欄位表。
4. **不得**修改跨 repo contract；AI-0 動作 C 若證實衝突，停止回報改開 Addendum 票。
5. **不得**調整 Phase 0/1 門檻數值或拒絕歸零規則；預授權是**豁免對象**，不是**降低門檻**。
6. 預授權之判定 producer 一律為票面宣告值與 repo-readable source，**不得**以模型自報欄位（`requires_human_decision` 等）作為授權或拒絕之依據。

## Error Handling
- AI-0 動作 A／B 之節名或內容與票面不符 → 停止回報，禁推定等價位置
- AI-0 動作 C 讀出「Host 決定不受約束」明文 → 停止回報，本票改 Addendum 承接
- 條文撰寫需觸及 `SKILL.md`、runner 或 schema 方能成立 → 停止回報（表示落點選錯）
- 新增條文與既有 Guardrails 任一條產生字面矛盾 → 停止回報，禁自行調和既有條文
- **[標配]** `git check-ignore -v` 命中任一 ALLOWED 檔 → 停止回報

## Acceptance Criteria
- [ ] **[存在性]** `REPAIR_ROUTING.md` 存在 `Dispatch Authority — Preauthorized Autonomous Apply` 節，位於 `Guardrails` 之後、`Post-Signature Erratum Lane` 之前（回填行號 `______`）
- [ ] **[存在性]** 該節含六項放行條件、命中之 MUST 放行、未命中之 MUST NOT 猜測／MUST NOT 自由文字詢問、自報不承載授權四段；且**不含**任何 dispatch status 狀態詞集、evidence class、stop reason 集或 rerun matrix（grep 驗證）
- [ ] **[存在性]** Guardrail 1「Pre-signature only」原文逐字未改；新節明載預授權落在其射程之外而非例外（回填二處行號 `______`）
- [ ] **[存在性]** A-1～A-4 表、Out Of Range 清單、Guardrails 五條、Post-Signature Erratum Lane、Calibration Coupling 節逐字未改（scoped diff 逐 hunk 檢視）
- [ ] **[存在性]** `OPS_CALIBRATION.md` 之 Phase 0 例外為單句，且 N=10／90%／拒絕歸零三項數值與規則逐字未改（回填行號 `______`）
- [ ] **[可達性]** 新節與 OPS 例外句互相可追溯：OPS 句以節名指向 `REPAIR_ROUTING.md` 之 `Dispatch Authority`，該節之六項條件為 OPS 句之唯一判準來源（回填雙向引用行號）
- [ ] **[行為性]** Given 一個滿足六項條件的 finding，依新條文判讀 expect Host MUST 放行且不得詢問；Given 任一條件不成立，expect 不得猜測、不得自由文字詢問，只得輸出既有機械碼或依既有規則交還人類（以票面推演驗收，本票無程式可執行）
- [ ] ~~`assembleDebug` PASS~~ (N/A: doc-only，無編譯面)
- [ ] `git diff --stat` 僅涉及 ALLOWED 二檔
