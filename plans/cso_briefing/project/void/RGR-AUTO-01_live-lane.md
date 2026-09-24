# RGR-AUTO-01: 正式審票對本輪 attachment 票之 A-1／A-2 唯一可驗修正自主 addendum 套用並閉環

檔案治理／來源說明：本票依 TPL `Unified_Ticket_Template_v3.md` v3.11 產出。設計來源為 `D:/DevSources/Skill-Advisor/deliver/RGR-AUTO_NN_draft-v3_2026-09-21.md`（含末尾 checkpoint）。簽發前唯讀 Recon：`D:/DevSources/Skill-Advisor/deliver/RGR-AUTO_Recon_2026-09-21.md`（未改檔、未呼叫 runner／模型）。跨 repo 契約：`D:/DevSources/Skill-Advisor/deliver/ticket-rigor-gate-skill/ticket-quality-skill-codex-runner-contract.md`（Recon 核對 L113/L149/L196/L230/L324/L383/L399/L466 無漂移；未禁止 preauthorized live apply；未封閉 `Ratification Status` 值域）。

**決策關係**：本票**部分取代** `2026-09-20_Scope_Binding_Authorization_Model_Infeasibility` 決策 1 對「一般 non-fixture target 不得消費 preauthorized override」之裁決，**僅新增** `single_attachment_live_lane`。P0A-02 對既有一般 non-fixture payload 之 fixture-only boundary 維持不變。本票之簽發，依 `RGR-SCOPE-BIND-01` 文中所定義之重開觸發事件描述而發生——**此為 provenance 記錄，MUST NOT 解讀為本票對 `RGR-SCOPE-BIND-01` 之協調義務**；本票不對該票之票檔本文或 lifecycle 做任何動作（見 Hard Constraints、Rigor Gate Rev B M5 裁決）。

**授權模型（Rev B：明確選定 Dispatch Authority，排除 Erratum Lane）**：本 lane 之 preauthorization **明確**依 `REPAIR_ROUTING.md` `Dispatch Authority — Preauthorized Autonomous Apply` 節承載，**不**採 `Post-Signature Erratum Lane`（該門對 A-2 排除，與 `PERMITTED_REPAIR_CLASSES=A-1,A-2` 矛盾，故不選）。六條件之逐一對應（**MUST NOT** 建 scope compiler；六條件由既有／本票新增之窄範圍 guard 逐一滿足，非新設一般化授權引擎）：

| 條件 | 滿足者 |
|---|---|
| ①authority anchor（repo-readable） | 本票（`RGR-AUTO-01`）簽發文字本身──`LANE_WRITE_MODE`／`PERMITTED_REPAIR_CLASSES`／`single_attachment_live_lane` 之存在，即為 lane 本身合法性之 repo-readable anchor；**個別 finding** 之 apply 資格另由②～⑥逐次收斂 |
| ②target scope（非他票本文） | `live_binding_passed`（lane-entry guard：session consistency binding ＋ containment guard）──本票同批於 `REPAIR_ROUTING.md` 新增窄範圍 carve-out 條文，明文：對 `single_attachment_live_lane`，「declaring ticket 之宣告 target」定義為「使用者於本次 `/ticket-rigor-gate` 呼叫中唯一附加之 Pending 票」（session-parametrized，非固定路徑），`live_binding_passed` 即為該定義之機械證明；carve-out 條文本身不擴張至 multi-root／glob／任意票對任意票之通用授權 |
| ③preimage | `validate_target()`（既有，RTR-008/009） |
| ④mutation shape | `validate_diff_budget()`／`validate_frozen_sentence()`（既有，RTR-010/011） |
| ⑤required verification | `required_verification` 非空檢查（P0A-02 既有） |
| ⑥no breaker／contract block | `circuit_breaker_precheck()`（既有，於主迴圈 L694，先於任何 finding 處理即執行） |

`preauth_eligible(payload)` **MUST** 於謂詞定義中逐一具名引用上表②⑥（見 AI-2），使謂詞與六條件一一對應，而非現行 4 項子集。

**P1 實測（2026-09-21，Claude Code 乾淨 session）**：`@` attachment 之 raw 字面（repo 相對 posix）與 resolved 路徑（Windows 絕對形式，如 `d:\DevSources\...`）均可取得；來源＝系統插入 metadata（Read tool 呼叫記錄），非模型推測。

**Class 授權依據（bootstrap 例外，如實揭露；輔助說明，非操作性法源）**：`CLASS_BASIS=whitelist-autonomy-full`——`REPAIR_ROUTING.md` Repair Whitelist 將 A-1、A-2 標為 `Autonomy=Full`，A-3、A-4 為 `Partial`（排除）。**此標籤描述內容判斷之自主度，不豁免 `OPS_CALIBRATION.md` Phase 0/1 之寫盤門檻**（Guardrail 2「All five apply」對所有 class 一致生效）；本 lane 實際寫盤授權之操作性法源唯一為上表之 Dispatch Authority 六條件。P3 非 fixture finding 母體為 0，原因為 live payload producer 尚不存在；本授權**非**宣稱已通過 P3 計數或 OPS_CALIBRATION Phase 1 Ledger 門檻——寫盤資格改由 Dispatch Authority 六條件豁免（`OPS_CALIBRATION.md` L15 既有例外句），而非計數達標。

**裁決 provenance（EVIDENCE_GAP 補證，2026-09-21；非 Rev，不改任何 R/AC/HC）**：`PERMITTED_REPAIR_CLASSES=A-1,A-2` 與 `CLASS_BASIS` 之裁決來源為 vault `wiki/decisions/SKAD/2026-09-21_RGR_Single_Attachment_Live_Lane_Authorization.md` Key Decision 5（Akira「確認簽發參數」＋「確認決議」2026-09-21；否決方案含「以 P3 計數授權——機械上不可填」「強制單一 class」）；授權模型（session 互動授權取代可離線 snapshot）見同檔 Key Decision 1。Rigor Gate R1/R2 報告之職責為票面嚴謹度審查，非 class 授權之裁決來源，未涵蓋此題屬預期。草稿 v2/v3 之「每個 class token MUST 有 P3／7b 計數證據」條款已被 KD5 明示否決；草稿非 SSOT。條件①之授權源頭為 Akira 裁決（vault 決策檔＋registry notes），本票文字為該裁決之載體，非自我授權。runtime MUST NOT 以票面字面為授權源（既有 HC3 已禁 runner 讀取 Pending 票作 authorization source）：條件①於 runtime 之可機械對應物＝本票實作後已提交之 `REPAIR_ROUTING.md` carve-out 條文與 runner lane 程式碼；session scope 載體＝當次唯一 attachment。授權鏈：vault KD1/KD5 → 已提交 carve-out → attachment binding → payload preauth／class／verification／hash → 既有 guard → apply／verify／receipt／closeout。

**簽發日期**: 2026-09-21
**Revision**: Rev B（2026-09-21，Ticket Rigor Gate PARTIAL 裁決收束，Akira 裁定；報告 `.tmp/rigor-gate/RGR_RGR-AUTO-01_20260921.md`）：①M1/M2 收束——明確選定 Dispatch Authority（排除 Erratum Lane），六條件逐一映射如上表，`preauth_eligible` 擴充至顯式覆蓋②⑥（AI-2）；②M3 收束——Repair Ledger 義務由既有 quality report／batch closeout 承載，不另建 ledger 子系統（AI-2）；③M4 收束——`edit_intent` 改為 lane-only optional 欄，MUST NOT 破壞 P0A-02／legacy fixture 之 schema 相容性（AI-2）；④M5 收束——`RGR-SCOPE-BIND-01` 僅作 decision provenance 引用，本票不回改其票檔或 lifecycle（Hard Constraints）；⑤Medium/Minor（guard 執行序位、schema 新欄命名、sandbox ticket_id 隔離、依賴三分標籤精度、`fixture_preauth_eligible` 用語澄清、殘餘風險 assignee）併入本輪修訂（AI-1/AI-4）。Rev A 內容（lane-entry binding 與 RTR-012 拆分、A-1 eligibility 收緊）逐字保留，未被本輪修訂觸動。**R2 校正**（第二輪 Rigor Gate 複審，報告 `.tmp/rigor-gate/RGR_RGR-AUTO-01_20260921_R2.md`）：⑥HC7 拆分為 (a)實作期編輯範圍／(b)runtime lane 交付／(c)sandbox 新建三款，解除「MUST NOT 編輯既有 Pending 票檔」與本票自身 runtime 交付（寫入 attachment target 之 addendum）之字面自我矛盾；⑦`HASH_FIELD_MISSING` 子碼由 `SCOPE_EXPANSION` 改隸 `VERIFICATION_FAILURE`（AI-3／AI-4／AC 同步）。
**簽發者**: Claude / Perplexity
**來源議題**: RGR
**Mode**: normal → uc  ||  **Effort**: High
**Traycer Workflow**: execute_review
**前置依賴（依賴三分；Rev B 依 Rigor Gate Mi1 校正分類）**：
- `execution_dependency`: `RGR-P0A-02`（已 deliver；本票 AI-2 之 `preauth_eligible`／`fixture_preauth_eligible` 設計建立於其既有 runner 程式碼結構——override 分支、marker loop 可達性、fixture-only boundary——非僅讀取其結論）
- `evidence_dependency`: `RGR-DISPATCH-AUTH-01`（六條件法條字面）、`RGR-SCOPE-BIND-01`（僅讀其六問答案與 AI-0 停止結論，本票消費的是一份已完成之調查結果，非排程協調）
- `coordination_dependency`: 無——`RGR-SCOPE-BIND-01` 之票檔／lifecycle 明確排除於本票動作範圍之外（見決策關係段落、Hard Constraints）；MUST NOT 重啟其 multi-root／glob／generic scope binding 候選 A

## Verify Config
TICKET_ID=RGR-AUTO-01
CREATED=2026-09-21
PRESET=feature
ALLOWED=.claude/skills/ticket-rigor-gate/SKILL.md,.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md,.claude/skills/ticket-rigor-gate/OPS_CALIBRATION.md,.claude/skills/ticket-rigor-gate/runner/repair_runner.sh,.claude/skills/ticket-rigor-gate/runner/schemas/ticket-repair-payload.v1.json,.claude/skills/ticket-rigor-gate/runner/schemas/ticket-repair-response.v1.json,.claude/skills/ticket-rigor-gate/runner/prompts/ticket-repair-prompt.v1.txt,.claude/skills/ticket-rigor-gate/runner/fixtures/run_no_call_tests.sh,.claude/skills/ticket-rigor-gate/runner/fixtures/rgr-auto/
FORBIDDEN=.claude/settings.json,.claude/settings.local.json,.claude/hooks/**,.claude/scripts/**,.claude/rules/**,.claude/skills/qa/**,.claude/skills/uc/**,plans/cso_briefing/project/deliver/**,plans/cso_briefing/project/file/**,app/**,build.gradle,settings.gradle,TICKET_REGISTRY.json
MAX_LINES=N/A
DIFF_REF=65fd8c4afcc7c0007821c1135f3b971338a106e8
ANCHOR_PROBED=2026-09-21 15:59 (+08:00)
BASE_COMMIT=65fd8c4afcc7c0007821c1135f3b971338a106e8
TARGET_BLOB=N/A（Desktop fallback：開票端無合法 blob-hash producer；AI-0 逐檔直讀現值為替代控制）
INTRODUCED_CAPABILITY=feature:autonomous-repair-default
DEPENDENCY_DELTA=NONE

**安全票量尺註記**：本票改變自主寫入判定與 fail-closed guard；失敗形狀枚舉為審查發現式，不可先驗估算，故 `MAX_LINES=N/A`（遙測欄，非 gate）。

**票面參數（簽發固定值；改動任一 MUST 開 Rev）**：
- `LANE_WRITE_MODE=addendum_only`
- `PERMITTED_REPAIR_CLASSES=A-1,A-2`
- `CLASS_BASIS=whitelist-autonomy-full`
- receipt 授權欄：`Authorization Basis: ticket-session-preauthorized`；`Ratification Status` 維持既有值 `pending`
- runner 呼叫形式：`bash .claude/skills/ticket-rigor-gate/runner/repair_runner.sh ...`（Recon P2：既有 `Bash(bash *)` 已涵蓋，無需新權限規則）

## Action Items

### AI-0: 錨點復驗（派工後、修改前）
- **檔案**: 全部 `ALLOWED` 檔
- **動作**: 逐字確認下列 Recon 現場錨點仍存在（行號為定位參考，以字面命中為準）：`repair_runner.sh` L153 `append_report_entry` 寫死 `pending`、L173 Ratification Status 擷取、L255/L259 CB-2 僅比對 `rejected`／`reverted`、L654-658 `rollback_target()`、L685 `QUALITY_REPORT_PATH` 取自 payload、L689 `ARTIFACTS_DIR`、L768-771 前像備援建立與 hash 驗證、L780-786 post-apply 失敗 rollback、L803-804 housekeeping 呼叫、`validate_target` 與 RTR-012 判定之字面位置、P0A-02 fixture-only override 之邊界謂詞與「override 不 early return」之字面位置；`SKILL.md` Review Algorithm 止於 proposal／gate decision；`OPS_CALIBRATION.md` Phase 0 proposal-only 條款；`REPAIR_ROUTING.md` Repair Whitelist A-1～A-4 之 Autonomy 欄。另確認：①lane-entry guard 落點於**整個既有 guard 鏈最前端**（僅次於 `circuit_breaker_precheck()`／`blocker_preflight()` 之 run 級前置，早於 `validate_eligibility`／`validate_evidence`／`validate_target`／`validate_diff_budget`／`validate_frozen_sentence`／RTR-012 全部既有 finding 級 guard）——若現行程式結構無法在不搬移既有 guard 之前提下做到，改置於 RTR-012 判定之前（Rev A 底線，MUST 至少滿足此弱化版本並停止回報說明原因）；②契約（跨 repo `ticket-quality-skill-codex-runner-contract.md`）之 response 定義是否容許新增**optional**欄位 `edit_intent`——若契約封閉 response 欄位集**或要求新欄必為 required** → 停止回報；③`ticket-repair-response.v1.json` 現值 `additionalProperties: false`（top-level 與 `finding_results` items 皆是）——確認新增 `edit_intent` 屬性後，**不將其列入 `required` 陣列**即可維持 P0A-02 既有 legacy stub response 之 schema 驗證通過（回填驗證方式：以 `fixtures/p0a02/` 任一既有 response fixture 對更新後 schema 做一次 `jq`／schema validator 試驗，非僅閱讀 schema 文字）；④確認 `REPAIR_ROUTING.md` `Dispatch Authority` 節現行六條件字面與決策關係段落之六條件映射表一致，逐項回填行號；⑤確認 `RGR-SCOPE-BIND-01` 之票檔（`plans/cso_briefing/project/Pending/RGR-SCOPE-BIND-01_target-scope.md`）與其 registry lifecycle 現況（`registry_list`，非 `get_ticket`），僅供 AI-0 記錄用途，**MUST NOT** 於本票任何 Action Item 中對其執行寫入或轉態。錨點失配依 TPL v3.11 基線漂移三分流處置。
- [x] 完成（2026-09-21，取證記錄 `.tmp/rigor-gate/RGR-AUTO-01_AI0_anchor_reverify_20260921.md`：全數逐字錨點確認存在；工作樹相對 DIFF_REF 已有未提交之 AI-1/AI-2 schema 子集 + 5 個未接線狀態變數宣告，判定為良性漂移非 STOP；①guard 落點結構可行免走 Rev A 弱化版；②契約無衝突；③8/8 legacy response fixture 實測相容；④六條件行號 L44-49 回填；⑤RGR-SCOPE-BIND-01 唯讀確認 lifecycle=DRAFT_NEEDED，未寫入）

### AI-1: attachment-derived session target、canonicalization 與三重約束
- **檔案**: `SKILL.md`、`runner/schemas/ticket-repair-payload.v1.json`、`runner/repair_runner.sh`
- **現值**: Review Algorithm 止於 proposal／gate decision；runner 僅收外部 `--payload`，無 live payload producer，無 attachment target 與 payload target 之比對。
- **目標值**:
  - skill 對單一 attachment ticket 啟動 `single_attachment_live_lane`；自動取用本輪 attachment（系統插入 metadata 之 resolved path 與使用者 `@` raw 字面），MUST NOT 新增任何使用者輸入。
  - `attachment_target` MUST 於 session 起始（review 開始前）取得並固定；finding 產生後 MUST NOT 重選或覆寫。
  - **canonicalization**：canonical 形式＝repo 相對 posix 路徑。反斜線→`/`；絕對路徑之磁碟代號與 repo 根以不分大小寫比對後剝除 repo 根前綴；`..` 正規化後落於 repo 根外 → reject。MUST NOT 依賴 bash `realpath` 直接處理 `d:\` 形式輸入。
  - payload 攜帶（欄位命名／型別／路徑為本 AI 之交付內容，schema 修改前 MUST 逐字採用）：
    - `lane`：payload 頂層、optional、`{"type":"string","enum":["single_attachment_live_lane"]}`——不存在或不等於此值即非本 lane，既有流程逐字不變。
    - `attachment_target`：payload 頂層、optional object，`{"raw":{"type":"string"},"resolved":{"type":"string"}}`，二子欄皆 required within this object。
    - `target.file_path`、`expected_old_text_hash`（既有欄位，live lane 下 `expected_old_text_hash` 硬必填——由 runner 之 lane-entry guard 以應用層邏輯檢查其存在且非空，非提升為 schema `required`，以維持既有 payload schema 對非 lane payload 之相容性）。
    - 既有 `finding`／`evidence`、`preauthorized_autonomous_apply`、non-empty `required_verification`（P0A-02 既有欄位，逐字沿用）。
  - runner 三重約束（缺一不可；對**所有**帶 lane discriminator 之 payload 無條件執行，與是否進入 RTR-012 分支無關）：
    1. **session consistency binding**：canonical(`attachment_target`) == canonical(`target.file_path`)，不等 → fail closed、零 mutation、target-mismatch receipt。定性為 anti-drift 一致性控制，MUST NOT 稱為獨立授權證明；transcript 比對僅供事後稽核，非執行期防護。
    2. **containment guard**：canonical target 須為 `plans/cso_briefing/project/Pending/` 下單一 `.md` 實檔（非目錄、非 glob、非 symlink 逃逸）。此為圍堵，非授權來源。
    3. **preimage**：apply 前 hash 完全相符，且前像備援已落盤並驗證。
  - receipt 記錄 raw、resolved、canonical 三值。
  - legacy／fixture payload 既有行為 MUST NOT 被全域改變（hash 於 legacy 路徑維持 optional）。
- [ ] 完成

### AI-2: proposal → auto-apply → verify → receipt → batch closeout
- **檔案**: `SKILL.md`、`REPAIR_ROUTING.md`、`OPS_CALIBRATION.md`、`runner/repair_runner.sh`、`runner/prompts/ticket-repair-prompt.v1.txt`、`runner/schemas/ticket-repair-response.v1.json`
- **現值**: Review Algorithm 無 apply step；Phase 0 未校準 class 一律 proposal-only；quality report 每筆寫死 `Ratification Status: pending`。
- **目標值**:
  - **apply 條件（全數成立方 apply）**：修法可由上位 SSOT／ticket／HC／AC 唯一推導；finding class ∈ `PERMITTED_REPAIR_CLASSES`；A-1 符合下列 A-1 addendum eligibility；lane-entry guard 已通過（`live_binding_passed`）；RTR-012 分支依下列 override 謂詞處置；其後既有 guard 依**現行時序**全數通過（hash、evidence、diff budget、frozen marker、CB、RTR、前像備援、post-apply verification）。
  - **A-1 addendum eligibility（機械；二條件皆成立方可 auto-apply；非規範性節本身不構成放行條件）**：
    1. **錨點位置**：以修正錨點所在之最近上層標題或票頭欄位判定。規範性節＝標題行、`前置依賴`、`Verify Config`、`Action Items`（含其下全部 `### AI-*`）、`Hard Constraints`、`Error Handling`、`Acceptance Criteria`。錨點 MUST 落於規範性節以外。
    2. **編輯意圖**：response schema 新增 enum 欄 `edit_intent ∈ {additive, supersede}`——**MUST 為 optional（不入 `ticket-repair-response.v1.json` 之 `required` 陣列）**，且**僅於 payload 帶 `lane=single_attachment_live_lane` 時由本 lane 之應用層邏輯消費**；非 lane payload（含 P0A-02 既有 legacy fixture response）不受影響、schema 驗證不要求該欄存在，`additionalProperties: false` 僅需新增此屬性定義本身，不動既有 `required` 集合。`additive`＝僅補充既有文字未載之資訊（如缺漏之交叉引用），不宣告任何既有文字錯誤、失效或被取代；`supersede`＝更正或取代既有文字。A-1 MUST 為 `additive`；`supersede` 或欄位缺失（**lane 內**，缺失與 `supersede` 同等處置，MUST NOT 將缺失預設為 `additive`）→ 不適格。
    - 任一不成立 → 不寫入，輸出 `SCOPE_EXPANSION`（子碼 `A1_NEEDS_INPLACE`，並記不適格原因 `normative_section`／`not_additive`；`edit_intent` 缺失併入 `not_additive` 計數，不另立第三分類）。
    - A-2 為純追加宣告，同樣 MUST 為 `edit_intent=additive`，不受條件 1 限制。
    - 殘餘風險（字面保留）：`edit_intent` 為模型申報值；錯標 `additive` 之語義更正無法由 post-apply verify 偵測。其失效形狀為 append-only、可由 closeout 與前像備援回復。
  - **addendum 形式**：依契約 §8.1 addendum 位置寫入；每筆 addendum MUST 引用所補充之錨點節標題、preimage 行號、錨點行 sha256、`edit_intent` 及 `finding_id`。
  - **RTR-012 override 謂詞（無循環）**：
    - **執行時序（固定）**：①lane-entry guard：對帶 lane discriminator＝`single_attachment_live_lane` 之 payload **無條件**執行 binding（canonical 相等）、containment、`expected_old_text_hash` 欄位存在檢查；任一失敗即 reject、零 mutation；全過記為 `live_binding_passed`。②RTR-012 分支（下述）。③其餘既有 guard 依現行時序。
    - `preauth_eligible(payload)`（Rev B：擴充為顯式覆蓋 Dispatch Authority 六條件之②⑥，非僅 4 項子集）＝ `preauthorized_autonomous_apply=true`（條件⑤之旗標本身） ∧ `required_verification` 非空（條件⑤） ∧ `live_binding_passed`（**條件②**——見決策關係段落六條件映射表；lane-entry guard 之輸出） ∧ payload write mode＝`addendum_only` ∧ **`circuit_breaker_precheck() 已通過`（條件⑥，既有 L694 run 級前置，主迴圈已保證此謂詞求值時該檢查必已執行且為真，本項為顯式具名引用，非新增程式碼）**。條件①之滿足見決策關係段落（票面自身為 anchor，非執行期可求值之布林項，不入本謂詞）；條件③④由既有 `validate_target()`／`validate_diff_budget()`／`validate_frozen_sentence()` 於主迴圈原位置獨立把關，同樣不入本謂詞（避免重複判定）。本謂詞**只消費**①之結果（lane-entry guard 輸出），MUST NOT 重新定義或替代 binding；binding MUST NOT 僅於 RTR-012 分支內被評估。僅含 RTR-012 評估時點可判定之 payload 靜態事實與已完成之前置檢查結果；hash 比對、前像備援、frozen marker 等執行期 guard 之**求值時機**不入此謂詞（其**結果**已於條件③④⑥之既有把關中獨立生效）。
    - `requires_human_decision ∧ propose_repair` 時：`fixture_preauth_eligible ∨ preauth_eligible` → 僅不因此矛盾分支拒絕；否則維持 RTR-012 reject／CB-4 latch。**用語澄清（Rev B，Mi2）**：`fixture_preauth_eligible` 為本票對 P0A-02 既有機制之**描述性代稱**，`repair_runner.sh` 現無此字面識別名——其實際對應為 `validate_decision_leakage()` 內以 `override` 區域變數表達之布林邏輯（fixture-only boundary 判定）。實作 MUST 定位該既有邏輯之實際變數／程式位置，MUST NOT 因本票用語而另建同名新函式或變數。
    - override 後 MUST NOT early return；其餘 guard 依現行時序照跑（沿 P0A-02 原則）。MUST NOT 另造 self-report 語意。
  - **Dispatch Authority carve-out 條文（新增，寫入 `REPAIR_ROUTING.md`）**：於 `Dispatch Authority — Preauthorized Autonomous Apply` 節末（`Post-Signature Erratum Lane` 之前）新增一小節，內容 MUST 涵蓋且僅涵蓋：決策關係段落六條件映射表之逐字或等義轉錄；明文本 carve-out **僅**適用於 `single_attachment_live_lane`，**MUST NOT** 泛化為其他 lane 或一般化 scope compiler；明文條件②之「declaring ticket 宣告 target」於本 lane 下之定義＝session-parametrized attachment（非固定路徑），並指出此定義之機械證明即為 `live_binding_passed`。**不動點**：Dispatch Authority 既有六條件原文、Guardrails 五條原文、Repair Whitelist 表、Out Of Range 清單、Post-Signature Erratum Lane 原文——逐字不改，carve-out 為新增小節，非改寫既有條文。
  - **Repair Ledger 義務（Rev B M3 收束，不建獨立子系統）**：`REPAIR_ROUTING.md` Tooling Note 與 `OPS_CALIBRATION.md` 所稱之 Repair Ledger，本 lane 由**既有 quality report／batch closeout 機制承載**，不另建 ledger 檔案或格式。欄位對應（於 `OPS_CALIBRATION.md` Repair Ledger 節新增一句說明此對應，**不改其既有格式定義**）：ledger 之 `ticket-id`→quality report `Ticket ID`；`class`→`findings[].class`；`proposed`→`Disposition`；`accepted/rejected`→`Apply Status`（`applied`＝accepted 等價、`rejected`／`escalated`＝rejected 等價）；`date`→ receipt 之 UTC timestamp。batch closeout 即為該 session 之 ledger 摘要視圖。預授權 apply 之結果**仍計入**該 class 之 calibration counter（`OPS_CALIBRATION.md` L15 既有規則，未變動）——因 quality report 本身已可由既有 `report_records()` 機械解析，滿足「calibration counter 可從真實紀錄推導」之既有設計意圖，不需另建 ledger 子系統。
  - **Phase 0**：proposal-only 限制**僅在** `preauth_eligible` 為真、class ∈ `PERMITTED_REPAIR_CLASSES`（A-1 另須符合 addendum eligibility）、全部機械 guard 通過時對該 finding 不適用；其餘 finding／class、無 attachment binding、multi-target payload 一律維持 proposal-only。
  - **流程**：每 finding 一次 apply、一次 mechanical verify；成功 → receipt；session 結束輸出 batch closeout。無自動 retry loop。
  - **Ratification**：成功條目新增獨立欄 `Authorization Basis: ticket-session-preauthorized`；`Ratification Status` 維持 `pending`，其語意僅為「未經事後人工追認」，MUST NOT 表示寫入未取得執行授權，MUST NOT 觸發或暗示逐 finding 核准；MUST NOT 寫 `accepted`。CB-2 判讀不受影響。
  - **batch closeout MUST 含**：applied／exception／proposal-only 三類計數；`A1_NEEDS_INPLACE` 筆數，依不適格原因（`normative_section`／`not_additive`）分列（後續 inplace 票之證據母體）；各 receipt 前像備援檔路徑（供 housekeeping／tmp 清理前手動保全）。
  - runner 呼叫形式固定為票面參數所載 `bash ...` 形式。
- **不動點**: `REPAIR_ROUTING.md` 之 Dispatch Authority 六條件原文、Guardrails 五條原文、Repair Whitelist 表、Out Of Range 清單、Post-Signature Erratum Lane 原文、`OPS_CALIBRATION.md` 之 Repair Ledger 既有格式定義與 Phase Promotion Criteria 既有數值／規則（N=10／90%／拒絕歸零）——逐字不改，本 AI 僅新增 carve-out 小節與 ledger 對應說明句；RTR-002／RTR-005（二守門）／RTR-007／RTR-009、`blocker_preflight()`、`apply_patch()` 本體、CB-1～CB-4、`RTR_RETRYABLE` 表、非 `propose_repair` → `no_write`、report parser 與歷史格式——零改動。
- **禁**: 不得將 `edit_intent` 列入 response schema 之 `required` 陣列；不得為 Repair Ledger 另建獨立檔案格式或子系統；不得於 `REPAIR_ROUTING.md` carve-out 條文中引入 multi-root、glob 或任何形式之一般化 scope compiler；不得以 `preauthorized_autonomous_apply` 旁路 scope、preimage、evidence、diff budget、frozen sentence、decision leakage 或任一 breaker。
- [ ] 完成

### AI-3: exception taxonomy 與 fail-closed 輸出
- **檔案**: `SKILL.md`、`REPAIR_ROUTING.md`、`runner/repair_runner.sh`、`runner/prompts/ticket-repair-prompt.v1.txt`
- **現值**: proposal-only／human adjudication 為正常 control flow；disposition 未收束為唯一 batch exception 輸出。
- **目標值**:
  - 原因碼六類：`SSOT_CONFLICT`、`AUTHORITY_AMBIGUITY`、`SCOPE_EXPANSION`、`VERIFICATION_FAILURE`、`REPAIR_NONCONVERGENCE`、`REPAIR_OSCILLATION`。前四類有 runtime producer；後二類為保留碼，本票 runner 與 skill MUST NOT 發出。
  - `SCOPE_EXPANSION` 子碼至少含：`TARGET_MISMATCH`（session consistency binding 失敗）、`CONTAINMENT`（containment guard 失敗）、`A1_NEEDS_INPLACE`、`CLASS_NOT_PERMITTED`。
  - `VERIFICATION_FAILURE` 子碼新增 `HASH_FIELD_MISSING`（lane-entry guard 之 `expected_old_text_hash` 欄位存在檢查失敗）——**分類理由（R2 校正）**：此案為「lane 必要輸入缺失」，非「target 落於宣告 scope 之外」，語意上屬驗證失敗而非 scope 擴張，故歸 `VERIFICATION_FAILURE` 而非 `SCOPE_EXPANSION`；區別於 `validate_target()` 之既有 hash-不符 `RTR-008`（欄位存在但值不符，走既有路徑，不新增子碼）。
  - 下位 artifact 與唯一明確之上位 SSOT 衝突 → 自動對齊上位，MUST NOT 轉為 approval request。
  - exception 輸出 MUST 含 finding、原因碼（含子碼）、未修改事實、可重跑條件；MUST NOT 以「請同意建議修訂」作正常 control flow 輸出。
- [ ] 完成

### AI-4: deterministic fixture matrix、不回歸與實機 sandbox
- **檔案**: `runner/fixtures/rgr-auto/`、`runner/fixtures/run_no_call_tests.sh`
- **現值**: P0A-02 已證 fixture-only override；無 attachment-derived single-target live lane 契約測試。
- **目標值**（deterministic Codex stub、零真實模型呼叫、驅動實際 runner path）:
  1. exact target ＋ A-2 唯一修法 ＋全 guard 過 → applied、verify pass、receipt（含 `Authorization Basis`、raw/resolved/canonical）、batch closeout。
  2. A-1：非規範性節＋`additive` → applied；非規範性節＋`supersede` → 不寫入、`SCOPE_EXPANSION/A1_NEEDS_INPLACE`（`not_additive`）；`Acceptance Criteria` 內＋`additive` → 不寫入、同子碼（`normative_section`）；`edit_intent` 缺失 → 不適格；closeout 依原因分列計數。
  2b. 帶 lane discriminator、`requires_human_decision=false`（不進 RTR-012 分支）且 attachment ≠ target → lane-entry guard 仍 reject、零 mutation。
  3. canonical 等價：同一 target 以 repo 相對 posix、Windows 絕對（小寫 `d:\`）、大寫磁碟代號三形式 → 皆判定相等並放行。
  4. repo 根外絕對路徑 → reject、零 mutation。
  5. `attachment_target ≠ target.file_path` → reject、零 mutation。
  6. 另一張合法 Pending 票（hash 亦相符）≠ attachment target → reject、零 mutation。
  7. target 於 `Pending/` 之外 → containment reject。
  8. old hash 缺失或不符 → reject、零 mutation。
  9. `required_verification` 缺失／非法 → reject。
  10. frozen marker／diff budget／evidence／CB／RTR 各自仍可拒絕。
  11. `preauth_eligible` 為真但 frozen marker 命中 → 仍拒絕（override 不 early return）。
  12. 未授權之 `requires_human_decision=true ∧ propose_repair` → RTR-012／CB-4 維持。
  13. A-3 或 A-4 finding → proposal-only、`CLASS_NOT_PERMITTED`、不 apply。
  14. apply 後 verify 失敗 → 前像還原，target 與 preimage 位元相同，無自動重試。
  15. legacy payload（無 `lane` 欄，即 P0A-02 既有 `payload_addendum.json` 等）＋ P0A-02 既有矩陣不回歸：`edit_intent` 為 optional 欄，legacy response 不含此欄仍通過 `ticket-repair-response.v1.json` 驗證；沿用 P0A-02 之 `fixtures/p0a02/` 既有 fixture 直接重跑作為回歸證據，**不修改**該目錄任一檔案（該目錄不在本票 ALLOWED）。
  16. **guard 執行序位（Me1 收束）**：lane-entry guard 早於 `validate_eligibility`（RTR-002）——以一個「lane-entry 應拒絕（attachment≠target）**且** finding 本身 class 不在 whitelist（本應觸發 RTR-002）」之複合失敗案例驗證：expect 實際失敗碼為 lane-entry 之 `SCOPE_EXPANSION/TARGET_MISMATCH`，而非 RTR-002；證明 lane-entry guard 確實運行於既有 guard 鏈最前端，非僅早於 RTR-012。
  17. **hash 欄位存在性 vs hash 不符之區辨（AI-3 新增子碼）**：live lane payload 缺 `expected_old_text_hash` 欄位 → lane-entry guard 拒絕、`VERIFICATION_FAILURE/HASH_FIELD_MISSING`；欄位存在但值與 live 檔案不符 → 走既有 `validate_target()` 路徑、`RTR-008`。兩案 receipt 之失敗碼須可區辨。
  - **實機 sandbox**（無 test-mode：與正式 target 同一 runtime 路徑、同一 guard；MUST NOT 為 sandbox 增設任何旗標、分支或 containment 例外）：固定路徑 `plans/cso_briefing/project/Pending/_SANDBOX_rgr-auto.md`（不匹配 `RGR-AUTO-[0-9]+`）；預埋至少一個 A-2 可唯一推導缺陷、一個非規範性節之 additive 型 A-1 缺陷（缺漏交叉引用）與一個非規範性節之 supersede 型 A-1 缺陷。建立與刪除由執行 `[實機]` AC 之 session 負責，驗畢刪除並以 `list_directory` 確認不存在。sandbox 不登冊、不作正式 ticket、不作取號來源；receipt 不作 durable evidence（證據以 transcript 片段＋receipt 內容轉錄入票面收尾記錄）。**Me3 收束（circuit breaker 隔離）**：sandbox payload 之 `ticket_id` 與 `quality_report.report_path` **MUST** 使用與 `RGR-AUTO-01` 不同之獨立值（建議 `_SANDBOX-RGR-AUTO` 與對應獨立 report 路徑），**MUST NOT** 重用 `TICKET_ID=RGR-AUTO-01`——因 CB-4 為以 `TICKET_ID` 為鍵之永久閂鎖，sandbox 刻意預埋之 supersede 型拒絕案例若誤記於 `RGR-AUTO-01` 名下，將凍結本票日後對真實 target 之修補能力。`[實機]` AC 驗收 **MUST** 額外確認：sandbox 執行前後，`RGR-AUTO-01` 自身既有 quality report 內容與 CB 狀態不受影響（比對執行前後之 `report_records()` 輸出）。
- [ ] 完成

## Hard Constraints
1. 使用者入口維持 `/ticket-rigor-gate @<single-pending-ticket>.md`；MUST NOT 要求重複輸入 target path、`sol|terra` 或逐 finding approval。
2. 僅單一 attachment ticket、單一 canonical target；禁 multi-target、directory、glob、跨 ticket、跨 repo、generic scope compiler。
3. runner runtime MUST NOT 讀取或解析 Pending ticket Markdown、`TICKET_REGISTRY.json`、deliver artifact **作為 scope 或 authorization source**（對 target 本身之 preimage 讀取與 addendum 寫入不在此限）。
4. MUST NOT 解除 P0A-02 fixture-only boundary 對一般 non-fixture payload 之效力；「non-fixture」本身不構成授權。
5. MUST NOT 移除或弱化 schema、evidence、preimage、diff budget、frozen marker、CB、RTR、post-apply rollback。
6. MUST NOT 以 `.claude/settings*` 寬鬆 allow rule、第二 MCP write channel 或繞過 self-modification classifier 達成寫入。
7. **實作期範圍與 runtime 交付之分離（Rev B R2 校正，避免自我封死）**：(a) **實作期**（撰寫本票 AI-1～AI-4 程式碼之階段）MUST NOT **編輯**任何既有 Pending／deliver 票檔（`_SANDBOX_rgr-auto.md` 之新建與刪除除外，見(c)）、`app/**`、Gradle、registry；(b) **runtime lane 交付**（本票功能上線後，`preauth_eligible` 成立時對 attachment target 之 addendum 寫入）為本票**預期交付本身**，不受 (a) 拘束——其寫入邊界由 lane-entry guard 之 containment＋binding 決定，非由 (a) 之「不得編輯既有票檔」字面禁止；(a) 針對的是**實作階段之工具本身修改行為**，(b) 針對的是**該工具上線後之正常運作行為**，兩者主體不同，不構成矛盾。(c) `_SANDBOX_rgr-auto.md` 為唯一實作期可**新建**之 Pending 檔，限 `[實機]` AC 使用、驗畢刪除，MUST NOT 作為正式 ticket、registry 記錄、取號來源或 durable evidence。
8. `requires_human_decision` MUST NOT 成為單獨之人工 approval veto；其與 `propose_repair` 之未授權矛盾 guard MUST NOT 被刪除。
9. MUST NOT 將 `Ratification Status` 偽標為人工 accepted。
10. MUST NOT 擴張為 state machine 重設、evidence taxonomy、scope compiler、校準平台、獨立 Ledger 子系統或自動 retry loop；MUST NOT 新增 write mode（`single_exact_inplace` 屬後續票）。Repair Ledger 義務由既有 quality report／batch closeout 承載（見 AI-2），不得誤讀本條為「Ledger 義務可整條省略」。
11. 殘餘風險字面保留，並各附監控／接受機制（Mi3 收束，不留無 assignee 之揭露）：①`Pending/` 與 `.tmp/` 均 gitignore；前像備援保證僅限當次 invocation 至 housekeeping（每 ticket_id 保留 10 run，FIFO）或外部 tmp 清理介入前，非 git-level durable rollback——**接受者**：Akira（本裁決明示接受，不另設監控設施）；②`edit_intent` 為模型申報值，錯標 `additive` 之語義更正無法由 post-apply verify 偵測——**監控機制**：batch closeout 之 `A1_NEEDS_INPLACE`／`applied` 計數比例為既有可觀測訊號，若 `applied` 案例事後由人工複核發現誤標，依既有 CB-2（人工 rejected／reverted）機制回饋，不另建新監控子系統。
12. skill／runner／schema／prompt MUST NOT 含任何 `_SANDBOX_` 字面分支。
13. **M1／M2 收束（Dispatch Authority，Rev B）**：本 lane 之 preauthorization MUST 明確依 `REPAIR_ROUTING.md` `Dispatch Authority` 節承載，MUST NOT 採 `Post-Signature Erratum Lane`（該門對 A-2 排除）。`preauth_eligible` 謂詞 MUST 顯式引用六條件中之②（`live_binding_passed`）與⑥（`circuit_breaker_precheck()` 已通過），不得停留於原 4 項子集。新增之 carve-out 條文 MUST 明示僅適用於 `single_attachment_live_lane`，MUST NOT 泛化為通用 scope compiler、multi-root 或 glob 語意（呼應 `RGR-SCOPE-BIND-01` 六問 stop condition，MUST NOT 直接沿用其候選 A）。
14. **M4 收束（`edit_intent` 相容性，Rev B）**：`edit_intent` MUST 為 `ticket-repair-response.v1.json` 之 optional 欄（MUST NOT 入 `required` 陣列），且 MUST NOT 因本票變動而使 `fixtures/p0a02/` 既有 legacy response fixture 之 schema 驗證失敗。
15. **M5 收束（`RGR-SCOPE-BIND-01` 邊界，Rev B）**：本票 MUST NOT 編輯 `RGR-SCOPE-BIND-01` 之票檔本文，MUST NOT 呼叫任何使其 registry lifecycle 變動之操作（含 `registry_set_status`／`registry_set_checkpoint`）。決策關係段落所述之「重開觸發事件」純為 provenance 記錄，MUST NOT 被實作為本票任一 Action Item 之觸發動作。

## Error Handling
- 契約對 attachment→payload、preauthorized live apply 出現禁止或衝突條款（Recon 後新增者）→ 停止回報。
- attachment path 完全無法取得、非單一 ticket、canonicalize 失敗或落於 repo 根外 → `SCOPE_EXPANSION`，零 mutation。
- canonical target ≠ payload target → `SCOPE_EXPANSION/TARGET_MISMATCH`，零 mutation。
- hash 缺失／不符、`required_verification` 缺失／非法 → 拒絕 apply，零 mutation。
- 同級 SSOT 衝突 → `SSOT_CONFLICT`；修法不唯一 → `AUTHORITY_AMBIGUITY`；MUST NOT 輸出一般 approval request。
- apply 後 verify 失敗 → 前像還原＋`VERIFICATION_FAILURE`；MUST NOT 自動重試。
- **[標配]** `git check-ignore -v` 命中任何需提交之 `ALLOWED` 標的 → 停止回報。
- auto mode 對 `.claude/skills/**` 自我修改遭 classifier 拒絕 → 不新增 broad allow、不走第二通道；停止並輸出完整已驗證 diff 供一次性人工處置。
- 實作需將 `edit_intent` 列入 `ticket-repair-response.v1.json` 之 `required` 陣列，或使 `fixtures/p0a02/` 既有 fixture 回歸失敗 → 停止回報（違反 HC14）。
- 任何步驟需編輯 `RGR-SCOPE-BIND-01` 票檔本文或呼叫其 registry 轉態 → 停止回報（違反 HC15）。
- `REPAIR_ROUTING.md` Dispatch Authority carve-out 條文之撰寫需觸及既有六條件原文、Guardrails 原文、Repair Whitelist 表或 Post-Signature Erratum Lane 原文之字面修改 → 停止回報（carve-out 為新增小節，非改寫既有條文）。
- sandbox 實機測試之 payload `ticket_id`／`quality_report.report_path` 與正式 `TICKET_ID=RGR-AUTO-01` 相同 → 停止回報（違反 Me3 隔離要求，執行前修正）。

## Acceptance Criteria
（本票無 `app/` 面：`[可達性]` 之 producer 證據取 skill／runner 之 `檔案:行號`。）
- [ ] **[存在性]** 票面與 `REPAIR_ROUTING.md` 明載對 2026-09-20 決策 1 之部分取代範圍、P0A-02 boundary 不變、`CLASS_BASIS=whitelist-autonomy-full` 與 bootstrap 例外之字面
- [ ] **[存在性]**（M1／M2 收束）`REPAIR_ROUTING.md` 含 `single_attachment_live_lane` 專屬 Dispatch Authority carve-out 小節，位於既有 `Dispatch Authority` 節內、`Post-Signature Erratum Lane` 之前；carve-out 逐字或等義轉錄決策關係段落之六條件映射表；grep 確認 carve-out 文字不含 multi-root、glob 或任何形式之一般化 scope compiler 字樣；既有六條件原文、Guardrails 五條、Repair Whitelist 表、Out Of Range 清單、Post-Signature Erratum Lane 原文逐字未改（scoped diff 逐 hunk 檢視）
- [ ] **[可達性]**（M2 收束）`preauth_eligible` 謂詞定義處（`repair_runner.sh`）可定位到對 `live_binding_passed`（條件②）與 `circuit_breaker_precheck()` 通過狀態（條件⑥）之顯式引用，行號回填
- [ ] **[存在性]**（M3 收束）`OPS_CALIBRATION.md` 含 quality report／batch closeout 與 Repair Ledger 既有格式之欄位對應說明句；既有 Ledger 格式定義、Phase Promotion Criteria 數值（N=10／90%／拒絕歸零）逐字未改
- [ ] **[存在性]**（M4 收束）`ticket-repair-response.v1.json` 之 `edit_intent` 屬性**不在** `required` 陣列中；以既有 `fixtures/p0a02/` 任一 legacy response fixture 對更新後 schema 實測驗證通過（非僅閱讀 schema 文字）
- [ ] **[存在性]**（M5 收束）`git diff --stat` 不含 `plans/cso_briefing/project/Pending/RGR-SCOPE-BIND-01_target-scope.md`；`registry_list`（`project=SmartCameraApp1`，`ticket_id=RGR-SCOPE-BIND-01`）於本票實作前後之 `lifecycle` 值相同
- [ ] **[存在性]** Review Algorithm 含 eligibility 成立時之 apply → verify → receipt → batch closeout 步驟
- [ ] **[可達性]** attachment 之 raw 與 resolved path 由 skill runtime 自動進入 payload `attachment_target`；producer 證據回填 `檔案:行號`
- [ ] **[行為性]** Given exact target、A-2 唯一修法、全 guard 通過，expect 無 approval wait，直接 apply、verify、receipt，session 結束輸出 batch closeout
- [ ] **[行為性]** Given A-1 錨點於規範性節或 `edit_intent≠additive`，expect 不寫入、`SCOPE_EXPANSION/A1_NEEDS_INPLACE`、closeout 依原因分列計數；Given A-1 錨點於非規範性節且 `edit_intent=additive`，expect applied 且 addendum 含錨點節標題＋preimage 行號＋錨點行 sha256＋`edit_intent`
- [ ] **[行為性]** Given 帶 lane discriminator、`requires_human_decision=false` 且 attachment ≠ target，expect lane-entry guard reject、零 mutation（binding 不依附 RTR-012 分支）
- [ ] **[行為性]** Given 同一 target 之 posix 相對／Windows 絕對／大寫磁碟代號形式，expect canonical 相等並放行；Given repo 根外路徑，expect reject
- [ ] **[行為性]** Given 另一張 Pending 票且 hash 相符但 ≠ attachment target，expect reject、零 mutation、target-mismatch receipt
- [ ] **[行為性]** Given target 在 `Pending/` 之外，expect containment reject、零 mutation
- [ ] **[行為性]** Given hash 不符／verification 缺失／frozen marker／diff budget／evidence／CB／RTR 任一失敗，expect 該 guard 拒絕且零 mutation
- [ ] **[行為性]** Given `preauth_eligible` 為真但 frozen marker 命中，expect override 後仍遭拒、零 mutation
- [ ] **[行為性]** Given A-3／A-4 finding，expect proposal-only、`CLASS_NOT_PERMITTED`、零 mutation
- [ ] **[行為性]** Given apply 後 verify 失敗，expect 前像還原後 target 與 preimage 位元相同，`VERIFICATION_FAILURE`，無自動重試
- [ ] **[行為性]** Given 下位 artifact 與唯一明確上位 SSOT 衝突，expect 自動對齊，不輸出 per-finding approval request
- [ ] **[行為性]** Given 同級 SSOT 衝突或修法不唯一，expect 僅輸出對應 exception，不寫入
- [ ] **[行為性]** Given 未授權之 `requires_human_decision=true ∧ propose_repair`，expect RTR-012／CB-4 fail closed
- [ ] **[行為性]** Given legacy payload 與 P0A-02 矩陣，expect 結果不變且真實 Codex call＝0；CB-2 判讀不受 `Authorization Basis` 欄影響
- [ ] **[存在性]** 成功 receipt 含 `Authorization Basis: ticket-session-preauthorized`、raw／resolved／canonical 三值；不含 `accepted`；batch closeout 含三類計數、`A1_NEEDS_INPLACE` 分原因筆數與各備援檔路徑
- [ ] **[存在性]** `ALLOWED` 內 skill／runner／schema／prompt 對 `_SANDBOX_` 字面 grep 命中＝0
- [ ] **[行為性]**（Me1 收束）Given lane-entry 應拒絕（attachment≠target）且該 finding class 本不在 whitelist（本應觸發 RTR-002），expect 實際失敗碼為 lane-entry 之 `SCOPE_EXPANSION/TARGET_MISMATCH`，證明 lane-entry guard 運行於既有 finding 級 guard 鏈最前端
- [ ] **[行為性]**（AI-3 新增子碼）Given live lane payload 缺 `expected_old_text_hash` 欄位，expect `VERIFICATION_FAILURE/HASH_FIELD_MISSING`；Given 欄位存在但值不符，expect `RTR-008`——兩案失敗碼可區辨
- [ ] **[實機]** 於真實 Claude Code session 對 `_SANDBOX_rgr-auto.md` 執行 `/ticket-rigor-gate @plans/cso_briefing/project/Pending/_SANDBOX_rgr-auto.md`：全程無「同意以上建議修訂」類 approval wait、無 Bash permission prompt；receipt 之 raw／resolved／canonical 與指令字面一致；預埋 A-2 與 additive 型 A-1 缺陷經 addendum 補正、addendum 引用正確錨點定位；supersede 型 A-1 未寫入且以 `A1_NEEDS_INPLACE` 出現於 closeout；產出 batch closeout；驗畢 sandbox 已刪除。**sandbox payload 之 `ticket_id`／`quality_report.report_path` 與 `RGR-AUTO-01` 隔離**（Me3）：執行前後比對 `RGR-AUTO-01` 自身 quality report 之 `report_records()` 輸出與 CB 狀態不變。證據＝transcript 片段＋receipt 內容轉錄。fixture PASS 不得單獨作為 UX 已交付之證明
- [ ] `assembleDebug` PASS
- [ ] `git diff --stat` 僅涉及 ALLOWED 檔案
- [ ] `bash -n .claude/skills/ticket-rigor-gate/runner/repair_runner.sh` PASS
- [ ] `run_no_call_tests.sh` PASS（含 rgr-auto 矩陣，真實 Codex call＝0）
