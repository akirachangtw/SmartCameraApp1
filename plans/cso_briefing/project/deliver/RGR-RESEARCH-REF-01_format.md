# RGR-RESEARCH-REF-01: RESEARCH_REF 值標準化為可查核之 question／request_id／run_id／sources 具名片段

**簽發日期**: 2026-09-30
**簽發者**: Claude（SKAD）
**來源議題**: RGR
**Mode**: normal  ||  **Effort**: Low
**Traycer Workflow**: N/A
**前置依賴（依賴三分）**：
- `execution_dependency`: 無
- `evidence_dependency`: RGR-ROUTING-CONTRACT-01
- `coordination_dependency`: 無

> **檔案治理／來源說明**：`REPAIR_ROUTING.md` L147 要求 final report 逐次記錄 Research 之「question asked」與 `request_id`/`run_id`；`SKILL.md` L432 之 `RESEARCH_REF=` 未規定值格式，致 2026-09-28 一次 live 執行之 report 僅含 ID 與來源、缺問題原文，無法查核查詢是否最小、去敏。本票以既有欄位承接既有義務，不增欄位（Akira 2026-09-29 裁決方案 2；2026-09-30 審查修訂採納：單行插入、去敏禁令擴及內部路徑與識別碼、四段必備且缺段不改變 `RESEARCH_DISPATCHED` 語意）。

## Verify Config
TICKET_ID=RGR-RESEARCH-REF-01
CREATED=2026-09-30
PRESET=docs
ALLOWED=.claude/skills/ticket-rigor-gate/SKILL.md
FORBIDDEN=.claude/skills/ticket-rigor-gate/REPAIR_ROUTING.md,.claude/skills/ticket-rigor-gate/OPS_CALIBRATION.md,.claude/skills/ticket-rigor-gate/runner/,.claude/skills/ticket-rigor-gate/experience-cards/,plans/cso_briefing/project/Pending/RGR-LIVE-DOGFOOD-01_closure.md,plans/cso_briefing/project/TICKET_REGISTRY.json,app/
MAX_LINES=5
DIFF_REF=4a1b777
ANCHOR_PROBED=2026-09-30 14:10
BASE_COMMIT=4a1b777
TARGET_BLOB=.claude/skills/ticket-rigor-gate/SKILL.md:25631a0f
INTRODUCED_CAPABILITY=NONE
DEPENDENCY_DELTA=NONE

## Action Items

### AI-0: 錨點復驗
- **檔案**: `.claude/skills/ticket-rigor-gate/SKILL.md`
- 確認 ``nor does it substitute for `ROUTE`.`` 恰 1 處（L438 Consistency rules 段）、`RESEARCH_REF=` 恰 1 處（L432）、`ten-field disposition` 與 `this does not add an eleventh field` 各恰 1 處。
- 任一不符 → 停止回報，不得以推定位置寫入。
- [x] 完成 — 四錨點各恰 1 處（L423/L432/L438/L440）

### AI-1: 規定 RESEARCH_REF 值格式
- **檔案**: `.claude/skills/ticket-rigor-gate/SKILL.md`（L438，緊接 ``nor does it substitute for `ROUTE`.`` 之後，同段內插入；**單行插入，不斷行**）
- **現值**: Consistency rules 未規定 `RESEARCH_REF` 值格式。
- **目標值**（逐字插入）:

```text
When `RESEARCH_DISPATCHED=true`, `RESEARCH_REF` MUST be written as `question=<minimal sanitized external-fact question>; request_id=<request identifier>; run_id=<run identifier>; sources=<returned evidence sources>`, one such group per Research call; the `question` segment MUST NOT contain the full ticket body, secrets, credentials, personal data, internal file paths, project or ticket identifiers, or context beyond what the question needs. All four segments MUST be present; a segment with no value is written as `none` (e.g. `sources=none` when the call returned no evidence). A group with a missing segment is a non-conforming report record; it does not change what `RESEARCH_DISPATCHED` means. A finding that reuses a result already obtained in the same execution cites that call's group unchanged. When `RESEARCH_DISPATCHED=false`, `RESEARCH_REF` stays empty.
```

- [x] 完成 — 逐字插入於 L438 同段內，單行未斷行

## Hard Constraints
1. disposition 維持十欄；`ten-field disposition`、`this does not add an eleventh field` 與 L426-435 欄位模板逐字不動。
2. 不修改 runner、Research API、queue、ledger、registry 或 dogfood fixture；不新增 persistent deduplication 或新的 report 檔案。
3. 僅規範 report 可觀測性，不改變 routing selector、四條件、Research 觸發條件或 `RESEARCH_DISPATCHED` 語意。

## Error Handling
- AI-0 錨點不符 → 停止回報。
- 插入文字若可被解讀為新增欄位、改變 Research 觸發條件或改變 `RESEARCH_DISPATCHED` 語意 → 停止，改寫為僅限值格式之措辭。
- `git check-ignore -v .claude/skills/ticket-rigor-gate/SKILL.md` 命中 → 停止回報。

## Acceptance Criteria
- [x] **[存在性]** L438 段內含 `question=`、`request_id=`、`run_id=`、`sources=` 四具名片段之格式規定。
- [x] **[存在性]** 載明同 execution 復用結果時引用原 group、`RESEARCH_DISPATCHED=false` 時留空。
- [x] **[存在性]** 載明四段皆必須出現、無值時寫 `none`、缺段即為不合規 report 紀錄且不改變 `RESEARCH_DISPATCHED` 語意；去敏禁令含 internal file paths 與 project or ticket identifiers。
- [x] **[存在性]** `ten-field disposition`、`this does not add an eleventh field` 與十欄模板未被 `git diff` 觸及。
- [x] ~~`assembleDebug` PASS~~ (N/A: docs preset)
- [x] **[存在性]** `git diff --stat` 僅涉及 ALLOWED 檔案。
