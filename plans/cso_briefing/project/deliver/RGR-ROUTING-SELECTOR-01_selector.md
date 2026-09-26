# RGR-ROUTING-SELECTOR-01: 強制載入 routing law、將 Research 定為取證動作而非終局 route，並補最小 report trace

**簽發日期**: 2026-09-26
**簽發者**: Claude（SKAD）
**來源議題**: RGR
**Mode**: normal  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: RGR-ROUTING-CONTRACT-01
- `coordination_dependency`: 無

> **檔案治理／來源說明**：外部審查（`Skill-Advisor/deliver/ticket-rigor-gate-skill/RGR-ticket-rigor-gate-review.md`）指出 `SKILL.md` 三處契約缺口，經 SKAD 一手核驗：①`SKILL.md` L11／L347 僅描述 routing law「位於」`REPAIR_ROUTING.md`，全檔無要求執行時讀取該檔之條文；Claude Code 之 supporting file 不會因位於 skill 目錄而自動載入。②L418 將 `B_RESEARCH_SUBMITTED` 列為 `ROUTE` 終局值，但 `REPAIR_ROUTING.md` Research Fallback 將 research 定義為同步取證、回傳後重跑四條件、終局仍為直接修正或 exception——`REPAIR_ROUTING.md` 全檔無 route B 字樣。③disposition 無欄位記錄 research 是否送出及其 `request_id`／`run_id`。

## Source and Verification Boundary
本票不主張 `REPAIR_ROUTING.md` 曾於先前任何 live run 中缺席。已取得之兩份 gate report 均引用該檔條文。強制載入所補者為未來 session 之可靠性缺口，非已發生故障之根因。

本票僅交付 `SKILL.md` 之契約文字與 report 欄位。live interactive execution 中 `research_submit` 是否實際被呼叫、`RESEARCH_DISPATCHED=true` 與非空 `RESEARCH_REF` 是否正確回填，由 `RGR-ROUTING-CONTRACT-01` 尚未滿足之行為性 AC 承擔。本票 **MUST NOT** 宣稱 routing 行為已經驗證。

## Supersession
僅就 final-report disposition，本票明示取代：
1. `RGR-INTERACTIVE-CLOSURE-AUTH-01` Hard Constraint 9 中「Research 維持 `B_RESEARCH_SUBMITTED` 路由語義，MUST NOT 刪除或弱化之」之規定；
2. 2026-09-25「B-route 語義保留」之裁決。

理由：Research Fallback 為同步取證後重跑四條件，非終局處置。`ROUTE` 維持為唯一終局處置欄位，合法值為 `A_AUTO_REPAIR`、`C_EXCEPTION`。Research 為取證動作，不得單獨授權修正，亦不得取代 decision-layer escalation。前揭 HC-9 僅拘束該票自身實作，本票取代之無須回頭修改該票。

## Verify Config
TICKET_ID=RGR-ROUTING-SELECTOR-01
CREATED=2026-09-26
PRESET=docs
ALLOWED=.claude/skills/ticket-rigor-gate/SKILL.md
FORBIDDEN=.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md,.claude/skills/ticket-rigor-gate/OPS_CALIBRATION.md,.claude/skills/ticket-rigor-gate/EXAMPLE.md,.claude/skills/ticket-rigor-gate/runner/,.claude/skills/ticket-rigor-gate/experience-cards/,.claude/rules/,app/,plans/cso_briefing/project/TICKET_REGISTRY.json
MAX_LINES=45
DIFF_REF=3e02c84754cc5768bba7547bcbf7ab5b7a6e9f37
ANCHOR_PROBED=2026-09-26 23:15
BASE_COMMIT=3e02c84754cc5768bba7547bcbf7ab5b7a6e9f37
TARGET_BLOB=N/A
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

**開票時 live read**：Desktop 於 2026-09-26 23:15 CST 取證；HEAD=`3e02c84`。工作樹含一個未追蹤之既存路徑 `.claude/skills/ticket-rigor-gate.zip`（Akira 匯出供外部審查），非本票產物，不得刪除、不得提交、不得納入本票 diff。

**`TARGET_BLOB=N/A` 依據**：docs-only、單檔修改；TPL v3.8 允許此類票標 `N/A`（比例原則）。本票選定 AI-0 live 錨點復驗為證據機制；此為本票之選擇，非可推廣之通用豁免，`N/A` 不豁免 scope 或 evidence 驗證。

## Action Items

### AI-0: 錨點復驗
- **檔案**: `.claude/skills/ticket-rigor-gate/SKILL.md`
- 確認以下錨點各恰 1 處：`## Review Algorithm`（開票時 L246，本票插入點為其正前方）；L11 含「`REPAIR_ROUTING.md` holds Advisor Repair Mode's routing law」；L347 以「Repair routing law —」開頭；L418 為 `ROUTE=A_AUTO_REPAIR|B_RESEARCH_SUBMITTED|C_EXCEPTION`。
- 錨點 >1 處或消失 → 停止回報 `ANCHOR_MISSING_OR_UNREADABLE`，不得以推定位置寫入。內容漂移但唯一 → 記錄後以工作樹現值續作。
- [x] 完成 — 四錨點各恰 1 處，位置與開票時一致（L11/L246/L347/L418）

### AI-1: 強制載入 routing law
- **檔案**: `.claude/skills/ticket-rigor-gate/SKILL.md`（`## Review Algorithm` 正前方新增 `## Mandatory Runtime Load` 節）
- **目標值**：每次 gate execution 開始 review algorithm 前，MUST 完整讀取 `REPAIR_ROUTING.md`；`OPS_CALIBRATION.md` 僅於所選 lane 使用 calibration 時讀取；`REPAIR_ROUTING.md` 不可讀 → 停止 routing，final report 記錄 `ROUTING_LAW_UNAVAILABLE`；MUST NOT 依記憶、先前 session 或對話摘要推斷 routing。
- [x] 完成 — `## Mandatory Runtime Load` 節已插入 `## Review Algorithm` 正前方

### AI-2: 精簡 routing selector
- **檔案**: 同上，接於 AI-1 新增節內
- **目標值**：補一段等價之 selector（≤12 行）：
  - 本地證據足以處理之 finding → 執行既有四條件測試。
  - 本地證據不足之 genuine 外部事實歧義 → 以最小去敏問題送出 Research 一次，以回傳 evidence 重跑四條件。
  - 屬 decision、policy、SSOT adjudication 或非可互換選擇 → `ROUTE=C_EXCEPTION`；Research 不得取代人類判斷。
  - Research 失敗，或 research 後仍無法解析 → 不重試；`ROUTE=C_EXCEPTION`。
  - 明文：Research 為取證動作，非終局 route。
- [x] 完成 — Routing selector 4 條 bullet 已補於同節，共 4 行（≤12 行上限）

### AI-3: 最小 report trace
- **檔案**: 同上，L418 所在之 disposition 區塊
- **現值**: `ROUTE=A_AUTO_REPAIR|B_RESEARCH_SUBMITTED|C_EXCEPTION`
- **目標值**：
  - 保留 `ROUTE` 欄名；合法值改為 `A_AUTO_REPAIR|C_EXCEPTION`。
  - 新增且僅新增兩欄：`RESEARCH_DISPATCHED=true|false`、`RESEARCH_REF=<request_id/run_id 或空>`。
  - 對已成為 finding 之外部事實候選，`RESEARCH_DISPATCHED=false` 時 `REASON_CODES` MUST 使用下列之一：`LOCAL_EVIDENCE_SUFFICIENT`、`DECISION_REQUIRED`、`RESEARCH_UNAVAILABLE`、`RESEARCH_FAILED`、`UNRESOLVED_AFTER_RESEARCH`。
  - 明文：`RESEARCH_DISPATCHED=true` 不代表修正成功，亦不取代 `ROUTE`。
  - 既有 `AUTO_REPAIR_ATTEMPTED`、`RUNNER_DISPATCHED`、`REASON_CODES`、`DECISION_QUESTION`、`NEXT_ACTION` 欄位保留不動。
- [x] 完成 — `ROUTE` 合法值改為 `A_AUTO_REPAIR|C_EXCEPTION`；新增 `RESEARCH_DISPATCHED`／`RESEARCH_REF` 兩欄；5 個 `REASON_CODES` 標準代碼補於同段落

## Hard Constraints
1. 僅修改 `SKILL.md`。
2. 不得修改 routing law、runner、schema、fixture、prompt、profile、calibration、lifecycle、MCP 設定或 production code。
3. 不得新增 queue、webhook、poller、ledger、persistent dedup、hook、subagent、wrapper 或新 route／state。
4. 不得新增 `EVIDENCE_STATE` 欄位。
5. 不得改變 Research 之去敏限制、同 execution 單次送出、不重試或 decision-layer 邊界。
6. 本票 AC 不含 live run。
7. 不處理 detector 範圍與 Medium severity 判準缺口。
8. AI 總數 ≤3；需第四步 → 停止回報。

## Error Handling
- AI-0 錨點歧義或消失 → 停止回報 `ANCHOR_MISSING_OR_UNREADABLE`。
- 任何改動波及 FORBIDDEN 路徑 → 停止回報；不得以替代檔案繞過，不得以還原後當作未發生。
- 既有措辭與本票 selector 語意衝突 → 以本票 Supersession 與 HC 為準，僅於 `SKILL.md` 內修正。
- 超出 `MAX_LINES` → 記錄實測值（遙測用途，非停止事由）。
- 不得因無 live `research_submit` access 而阻塞本票；本票驗證契約文字，非工具執行。
- `git check-ignore -v .claude/skills/ticket-rigor-gate/SKILL.md` 命中 → 停止回報。

## Acceptance Criteria
- [x] **[存在性]** 契約文字：`## Mandatory Runtime Load` 節存在於 `## Review Algorithm` 正前方，要求完整讀取 `REPAIR_ROUTING.md` 並載明 `ROUTING_LAW_UNAVAILABLE` 停止碼。
- [x] **[存在性]** 契約文字：selector 條文將本地證據足夠之 finding 指向既有四條件測試。
- [x] **[存在性]** 契約文字：selector 條文將 genuine 外部事實歧義指向一次 Research 後重跑四條件。
- [x] **[存在性]** 契約文字：selector 條文明定 decision／research 失敗／research 後仍無法解析者收束至 `ROUTE=C_EXCEPTION`，且明文 Research 非終局 route。
- [x] **[存在性]** Report 契約：`ROUTE` 合法值僅 `A_AUTO_REPAIR|C_EXCEPTION`；`RESEARCH_DISPATCHED` 與 `RESEARCH_REF` 兩欄存在；`REASON_CODES` 五個標準代碼存在。
- [x] **[存在性]** 靜態掃描：`grep -n "B_RESEARCH_SUBMITTED" .claude/skills/ticket-rigor-gate/SKILL.md` 無結果。
- [x] **[存在性]** 範圍：`git diff --name-only` 僅 `SKILL.md`；`git diff --check` 通過。
- [x] ~~`assembleDebug` PASS~~ (N/A: docs preset)
