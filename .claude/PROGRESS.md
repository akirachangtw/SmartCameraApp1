# Cueshot MVP Sprint Dashboard

**Sprint**: D0-D15 | **Branch**: `feature/cueshot-mvp`
**Current Day**: D8-VERIFY → Stage0 Realignment 進行中 — W2 T02 🔴 Hard Gate FAIL
**Last Updated**: 2026-05-11 17:45

---

## Sprint Health

| Metric | Status |
|--------|--------|
| Schedule | Stage0 W1 ✅ CLOSED — FIX-CROSSREVIEW-01 ✅ CLOSED — W2 🔴 T02 Hard Gate FAIL（BHA-01）, T03→T04 blocked |
| Compile | 0 errors ✅ (assembleDebug 59s) |
| Tests | 178t / 0 failed ✅ (testDeviceDebugUnitTest) |
| Lint | 0 errors ✅ / 555 warnings (499 LogNotTimber, 16 GradleDep, baseline) |

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
| BHA-01 | T02 Hard Gate — 人臉 bbox 偏移 | 🔴 P0 Blocker | **根因確認**：`observeCompositionAdvice`（line 1982）+ `observePtfAlignment`（line 2081）使用 `det.imageWidth=480`（raw un-rotated）歸一化，但 `face.boundingBox` 座標在旋轉後空間（x∈[0,640]）。修復方案 A 確定：加入 rotation swap 與 `updateCoordinateTransformer`（line 585）對齊。.claude/bha_report.md 已產出。 |

## Next Actions

1. **🔴 BHA-01-FIX 實作票**（P0，已取得根因，等待授權）：
   - `CameraViewModel.kt` line ~1982：加入 rotation swap（imgW/imgH 互換，參考 line 585 模式）
   - `CameraViewModel.kt` line ~2081：同上修正 PTF pipeline
   - /qa → /sv → Sony 實機 Hard Gate 重驗
2. **T03 重啟**（BHA-01 通過後）：提交單授權後執行 Pill Toggle 重寫
3. **I7 修復**（可並行）：`EyeAfController.kt` 加入 `lens=Focused && 漂移 < 10px → skip`
4. **statusLine hook 驗證**（重啟後）：確認 `tmp/claude_status_cache.json` 寫入真實 API 數據，`/status` 顯示 🟢 真實來源

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
| K3-7 | Edit 操作前強制 Re-view（>200 行目標檔 / /compact 後無條件觸發） | K3-7 Session Boundary Rule 修訂 | CLAUDE.md 規則 ✅ 已寫入 Rule 18 |

## Quick Links

- SSOT: `SmartCameraApp_Strategy_v3_3_SSOT_20260404.md`
- Tech Spec: `SmartCameraApp_TechDeepDive_v3_20260405.md`
- Roadmap: `Cueshot_MVP_Phase1_Roadmap_v1.md`
- Full History: `PROGRESS_ARCHIVE.md`
