# Cueshot MVP Sprint Dashboard

**Sprint**: D0-D15 | **Branch**: `feature/cueshot-mvp`
**Current Day**: D8-VERIFY ✅ → Stage0 Realignment 開始
**Last Updated**: 2026-05-07

---

## Sprint Health

| Metric | Status |
|--------|--------|
| Schedule | Stage0 W1 ✅ CLOSED — INFRA-02 ✅ DONE — FIX-INFRA-02 ✅ DONE — W2 待啟動 |
| Compile | 0 errors ✅ |
| Tests | 178t / 0 failed ✅ |
| Lint | 0 errors ✅ (opencv module removed) |

---

## Stage0 Realignment（2026-05-05 啟動）

### Active Tickets

- **TICKET-00** isAllAligned fix — P0 — Est 30m / Actual 35m (+5m) — DONE
- **TICKET-01** AmbientConstants SSOT — P0 — Est 30m / Actual 25m (-5m) — DONE

### Wave Queue
- **W1**: T00 → T01
- **W2**: T02 → T03 → T04 → T06
- **W3**: T08 → T09 → T05
- **W4**: T07 → T10 → T11 → T12A → T13 → T15
- **W5**: T12B → T14

### Session Plan
- S1(W1): T00→T01
- S2(W2): T02→T03→T04
- S3(W2'): T06
- S4(W3): T08→T09
- S5(W3): T05
- S6(W4): T07→T10
- S7(W4): T11→T12A
- S8(W4): T13+T15
- S9(W5): T12B→T14

### Source Documents
- Execution Sequence: `plans/cso_briefing/Cueshot_Execution_Sequence_v1.md`
- Merged Spec: `plans/cso_briefing/Cueshot_Merged_Spec_v3_4_1_Stage0.md`
- Verification Matrix: `plans/cso_briefing/Cueshot_Verification_Matrix_v1.md`

---

## Active Blockers

| ID | Issue | Severity | Root Cause |
|----|-------|----------|------------|
| ~~I8~~ | ~~IFTR 歸零 — isAllAligned 永遠 false~~ | ✅ 已修復 | TICKET-00 — 改為加權平均 ≥ 70 |
| I7 | 對焦圓環永動循環 | 🟡 Medium | EyeAfController 每 2s 無條件重觸發 |

## Next Actions

1. **W2 啟動**：S2(W2) → T02 → T03 → T04（讀 `Cueshot_Execution_Sequence_v1.md`）
2. **I7 修復**（可選先做）：`EyeAfController.kt` 加入 `lens=Focused && 漂移 < 10px → skip`

## Active Debt（僅列未解決項）

- 🟡 SSOT-ORPHAN-RULE-01：PtfRuleAggregator 雙份邏輯（D10 KAIROS #3）
- 📝 Candidate Rule 17：有狀態平滑器 Lifecycle Gate（D10 歸入）

## Quick Links

- SSOT: `SmartCameraApp_Strategy_v3_3_SSOT_20260404.md`
- Tech Spec: `SmartCameraApp_TechDeepDive_v3_20260405.md`
- Roadmap: `Cueshot_MVP_Phase1_Roadmap_v1.md`
- Full History: `PROGRESS_ARCHIVE.md`
