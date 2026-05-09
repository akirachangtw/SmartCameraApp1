# Cueshot MVP Sprint Dashboard

**Sprint**: D0-D15 | **Branch**: `feature/cueshot-mvp`
**Current Day**: D8-VERIFY ✅ → Stage0 Realignment 進行中
**Last Updated**: 2026-05-10

---

## Sprint Health

| Metric | Status |
|--------|--------|
| Schedule | Stage0 W1 — INFRA-02 ✅ CLOSED — FIX-INFRA-02-FINAL ✅ CLOSED — FIX-INFRA-02-D ✅ CLOSED — FIX-CROSSREVIEW-01 ✅ CLOSED — W2 🟡 READY (Rule 17 授權待決) |
| Compile | 0 errors ✅ |
| Tests | 178t / 0 failed ✅ |
| Lint | 0 errors ✅ (opencv module removed, ndk block removed) |

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
| ~~I9~~ | ~~Traycer Path B 裁決阻擋 W2 啟動~~ | ✅ 已解除 | FIX-INFRA-02-FINAL + FIX-INFRA-02-D 完成 OpenCV 全鏈路清除，Path B 唯一 blocker 已修補 |
| I7 | 對焦圓環永動循環 | 🟡 Medium | EyeAfController 每 2s 無條件重觸發，缺 `lens=Focused && 漂移 < 10px → skip` guard |

## Next Actions

1. **Rule 17 授權**（可選）：人工豁免 CLAUDE.md 禁區 → append Rule 17 → commit [Infra]
2. **Traycer 終審**：FIX-CROSSREVIEW-01 `strict_verification`
3. **W2 啟動**：S2(W2) → T02 → T03 → T04（讀 `plans/cso_briefing/Cueshot_Execution_Sequence_v1.md`）
4. **I7 修復**（可選先做）：`EyeAfController.kt` 加入 `lens=Focused && 漂移 < 10px → skip`
5. **D10 KAIROS #3 議程備忘**：K3-1~K3-6 候選規則待審（見 Active Debt）

## Active Debt（僅列未解決項）

- 🟡 SSOT-ORPHAN-RULE-01：PtfRuleAggregator 雙份邏輯（D10 KAIROS #3）
- 📝 Candidate Rule 17：有狀態平滑器 Lifecycle Gate（D10 歸入）

## D10 KAIROS #3 候選議程（Pre-W1-B 登記）

| ID | 候選項目 | 來源 | 類型 |
|----|---------|------|------|
| K3-1 | Debug Overlay Zero Tolerance（debug 文字覆蓋零容忍） | D8 FIX-01~03 | CLAUDE.md 規則 |
| K3-2 | Hardcoded Text Scanning（硬編碼文字掃描） | D8 FIX-01~03 | /sv gate 擴充 |
| K3-3 | KAIROS Real-Device Screenshot Requirements | D8 FIX-01~03 | KAIROS 節點流程 |
| K3-4 | Decommission 票刪除驗收必須用 tracked filename glob 驗證 | FIX-INFRA-02-D | CLAUDE.md 規則 |
| K3-5 | Ticket 驗收清單與 SSOT 衝突時，以合併規格源為裁決依據並記錄衝突吸收 | TICKET-01 | CLAUDE.md 規則 |
| K3-6 | `cross_review.sh` payload 組裝時驗證 SV scope ↔ diff scope 一致性 | cross_review forensic | 腳本改進 |

## Quick Links

- SSOT: `SmartCameraApp_Strategy_v3_3_SSOT_20260404.md`
- Tech Spec: `SmartCameraApp_TechDeepDive_v3_20260405.md`
- Roadmap: `Cueshot_MVP_Phase1_Roadmap_v1.md`
- Full History: `PROGRESS_ARCHIVE.md`
