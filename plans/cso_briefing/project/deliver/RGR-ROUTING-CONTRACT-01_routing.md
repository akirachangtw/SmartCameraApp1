# RGR-ROUTING-CONTRACT-01: 對齊 C_EXCEPTION、Decision-Fork 與 Research fallback 的 routing contract

**簽發日期**: 2026-09-26
**簽發者**: Claude（SKAD）
**來源議題**: RGR
**Mode**: normal  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: RGR-RESEARCH-SUBMIT-01
- `coordination_dependency`: 無

> **檔案治理／來源說明**：`REPAIR_ROUTING.md` 之 routing law 與 `SKILL.md` 之 report disposition schema 間有四處未對齊：①L129 四條件失敗分支僅寫「report it as an exception」，未綁定 `SKILL.md` L418 disposition schema 之 `ROUTE=C_EXCEPTION`；②`SKILL.md` L164 定義 Decision-Fork 為 decision request，routing law 未指涉其 route 歸屬；③L141 Research Fallback 規定以 `results[]` 重跑四條件，未限制外部結果對 decision-layer 事項之修改權；④L143 記錄 `request_id`／`run_id`，未規範同一 execution 內同質問題之重送。本票以單檔補述對齊四處，不新增 route、機制或狀態。既有兩項保護維持不動：exception 不阻斷同 cycle 其他 independently eligible repair；submit failure 不 retry 且不阻斷其他 finding。
>
> Research 與 C-route 之順序依 L141 既有語意：外部事實歧義 → 最小取證 → 重跑四條件 → 可安全決定且可驗證者直接修正；仍屬 decision／policy／SSOT／高風險者方為 `C_EXCEPTION`。本票不改變此順序。

## Verify Config
TICKET_ID=RGR-ROUTING-CONTRACT-01
CREATED=2026-09-26
PRESET=docs
ALLOWED=.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md
FORBIDDEN=.claude/skills/ticket-rigor-gate/SKILL.md,.claude/skills/ticket-rigor-gate/OPS_CALIBRATION.md,.claude/skills/ticket-rigor-gate/EXAMPLE.md,.claude/skills/ticket-rigor-gate/runner/,.claude/skills/ticket-rigor-gate/experience-cards/,app/,plans/cso_briefing/project/TICKET_REGISTRY.json
MAX_LINES=N/A
DIFF_REF=9eab21317086f1e78d04a663ad1dbeed0f476483
ANCHOR_PROBED=2026-09-26 17:10
BASE_COMMIT=9eab21317086f1e78d04a663ad1dbeed0f476483
TARGET_BLOB=.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md:3f497aee
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

**開票時 live read**：Desktop 於 2026-09-26 17:10 CST 取證；HEAD=`9eab213`，工作樹 clean（故 `REPAIR_ROUTING.md` 工作樹即 HEAD 版本）；L129／L141／L143 與 `SKILL.md` L164／L418 字面逐一 grep 實存。`TARGET_BLOB` 值由執行端 `git hash-object` 提供並經交叉核驗工作樹與 `HEAD:<path>` 一致；Desktop 端無 blob producer，以工作樹 clean 旁證。依 TPL v3.11，本欄為資訊性診斷值。

**`MAX_LINES=N/A` 依據**：本票 AI 觸及 gate routing 判定（守門類安全不變量），失敗形狀枚舉為審查發現式，不可先驗估算。

## Action Items

### AI-0: 錨點復驗
- **檔案**: `.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md`
- 確認 L129 以段首標記 `**When any condition fails.**` 開頭（全檔唯一）、L141 含「results[]」回填四條件之規則、L143 含 `request_id`／`run_id` 記錄與「does not retry itself and does not block any other finding」，三處字面各恰 1 處。
- 字面錨點 >1 處（目標位置歧義）或完全消失 → 停止回報，不得以推定位置寫入。內容漂移但唯一 → 記錄後以工作樹現值續作。
- 註（SKAD 2026-09-26，EVIDENCE_GAP，不開 Rev）：原錨點「report it as an exception」於 L129 與 L141 各出現一次，不具定位唯一性，已由執行端 AI-0 正確停止並回報；改以段首標記定位。L141 之 exception 為 Research Fallback 重跑四條件後之同一失敗分支，由 AI-2 補述綁定至 `C_EXCEPTION`，AI-1 措辭無須變更。
- 重跑結果（2026-09-26）：`**When any condition fails.**` L129 恰 1 處、`results[]` L141 恰 1 處、`does not retry itself and does not block any other finding` L143 恰 1 處，三處錨點均無歧義，續作 AI-1～AI-3。
- [x] 完成

### AI-1: 對齊 exception、C_EXCEPTION 與 Decision-Fork
- **檔案**: `.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md`（以 `**When any condition fails.**` 定位之 L129 段落之後）
- **現值**: 失敗分支要求將 finding「report it as an exception in the final report」；未指定 disposition schema 之 `ROUTE` 值；未指涉 Decision-Fork。
- **目標值**: 於該分支後補述兩句：
  1. 依此分支作 exception 回報之 finding，final report MUST 使用 `ROUTE=C_EXCEPTION`；本檔之 exception 與 `SKILL.md` disposition schema 之 `C_EXCEPTION` 為同一路由。
  2. Decision-Fork（`SKILL.md` → Decision-Fork Findings）為 `C_EXCEPTION` 路由下之 decision-layer finding classification，非替代 repair route；MUST 以 `ROUTE=C_EXCEPTION` 回報，其既有 Decision-Fork record 提供 decision question 與 mechanical consequence。
- [x] 完成

### AI-2: 限制 Research evidence 之 authority boundary
- **檔案**: `.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md`（L141 結果回填規則之後）
- **現值**: 以 `results[]` 重跑四條件，未限制外部結果對 decision-layer 事項之修改權。
- **目標值**: 補述一句：Research result 僅得作為 General Autonomous Repair Default 四條件 reassessment 之 evidence；finding 於 reassessment 後若仍屬 requirement、policy、SSOT 或其他 decision-layer 事項（即 `C_EXCEPTION`），result MUST NOT 授權自主修改。
- [x] 完成

### AI-3: 同 execution 同質問題不重送
- **檔案**: `.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md`（L143 之後）
- **現值**: 每次呼叫記錄 `request_id`／`run_id`；未規範同一 execution 內之重送。
- **目標值**: 補述一句：同一 interactive execution 中，MUST NOT 對 materially equivalent 之 external-fact question 重複 submit；後續判定 MUST 復用該 execution 已取得之結果並於 final report 引用同一 `request_id`／`run_id`。此為 session-local execution discipline，不建立 persistent deduplication mechanism。
- [x] 完成

## Hard Constraints
1. 僅修改 `REPAIR_ROUTING.md`；`SKILL.md` 之 disposition schema 與 Decision-Fork 定義一字不動。
2. 不新增 route、severity、runner、queue、webhook、polling、state file、dedup ledger、schema、fixture、allowlist 或新依賴。
3. 既有兩項保護 MUST 維持原語意：exception 不阻斷同 cycle 其他 independently eligible repair；submit failure 不 retry 且不阻斷其他 finding。
4. 不執行 `research_submit`。
5. 三項補述 MUST 維持 prompt-level、session-local 之 MVP 邊界。
6. AI 總數 ≤3；需第四步 → 停止回報。

## Error Handling
- AI-0 錨點歧義或消失 → 停止回報，不得以推定位置寫入。
- 任何改動擴及 `ALLOWED` 以外檔案 → 停止回報；不得以還原後當作未發生。
- 補述文字若可被解讀為 research result 授權 policy／SSOT 之自動決策 → 停止，改寫為僅限 evidence 用途之措辭。
- 補述文字若引入 persistent state、成本帳本、queue 或 retry → 停止，維持 session-local 紀律。
- `git check-ignore -v .claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md` 命中 → 停止回報。

## Acceptance Criteria
- [x] **[存在性]** L129 失敗分支後明確將 exception 對映為 `ROUTE=C_EXCEPTION`，並載明與 disposition schema 為同一路由。
- [x] **[存在性]** Decision-Fork 被明確定義為 `C_EXCEPTION` 之 decision-layer classification，非替代 route。
- [x] **[存在性]** Research result 被限定為四條件 reassessment 之 evidence，不得授權自主修改仍屬 `C_EXCEPTION` 之 decision-layer 事項。
- [x] **[存在性]** 同一 interactive execution 對 materially equivalent external-fact question 不重送，並要求復用與引用同一 `request_id`／`run_id`；載明不建立 persistent dedup。
- [x] **[存在性]** 既有「exception 不短路其他 independently eligible repair」與「submit failure 不 retry、不阻斷其他 finding」語意仍存在（`git diff` 未觸及其字面）。
- [ ] **[行為性]** 於一次隔離之 direct-interactive live run 中，Gate 遇到本地 evidence 無法決定之 genuine external-fact ambiguity 時，至多提交一次最小、去敏之外部查證；final report 記錄 `request_id`／`run_id` 與來源；Gate 僅對被回傳 evidence 支持、且重跑四條件後仍屬可安全修正之文字作最小修改。（證據來源與回填時點由 SKAD 於票外指定；本項於本票實作完成時尚未滿足，本票以 REVIEW 等待回填。）
- [ ] ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] **[存在性]** `git diff --stat` 僅涉及 `ALLOWED` 檔案。


---

## Closeout（SKAD 2026-09-30）

**Lifecycle**: CLOSED　**Reviews**: sv=PASS（SKAD 一手靜態審查）、qa=N_A（docs preset）、codex=N_A（未執行，見下）、traycer=N_A　**實作 commit**: `3e02c84`（`REPAIR_ROUTING.md` +6，純新增）

| 項目 | 結果 |
|---|---|
| AI-0 | 首次執行時原錨點「report it as an exception」於 L129／L141 各出現一次，執行端依規停止回報；SKAD 改以段首標記 `**When any condition fails.**` 定位（EVIDENCE_GAP，不開 Rev）後重跑通過 |
| AI-1～AI-3 | 三段補述分別落於 L129 段後、L141 段後、L143 段後 |
| 存在性 AC 1～5 | ✅ 皆通過；既有「不短路」與「不 retry」兩項保護原字未動 |
| `TARGET_BLOB` | ✅ diff 標頭 `index 3f497ae..73bfe46` 一手證實開票值 |
| **行為性 AC** | ❌ **未驗證**。live run 中之 research 送出與 `request_id`／`run_id` 回填，始終未取得實機證據 |

**收束依據**：Akira 2026-09-30 裁定——本票以存在性驗證收束；**實際發生 routing 問題時再開修補票**。行為性 AC 以「未驗證、豁免」收束，**非通過**。Codex 交叉審查未執行，依同一裁定豁免。

**已知觀察（非缺陷）**：L143 原句將跨 execution 之重送紀律定為 executor 判斷，新增句將單一 execution 內定為 MUST NOT 重送；範圍不同、邏輯一致，但僅讀其一可能困惑。

**後續關聯**：`RGR-ROUTING-SELECTOR-01`（CLOSED）於 `SKILL.md` 補強制載入本檔、routing selector 與 `RESEARCH_DISPATCHED`／`RESEARCH_REF` trace 欄位，其行為驗證原歸屬本票行為性 AC，一併依本裁定豁免。
