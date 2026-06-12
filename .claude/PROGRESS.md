# Cueshot MVP Sprint Dashboard

**Sprint**: D0-D15 | **Branch**: `feature/cueshot-mvp`
**Current Day**: D12

| 最近 Ticket | 狀態 |
| ---------- | ---- |
| INFRA-45 | ✅ D-27 SSOT Amendment — v4.0.5→v4.0.6 + K4-15 修正 |
| P08-F4 | ✅ Discovery 完整閉環 — H3 REFUTED · CSO Report → `reports/P08-F4_Discovery_Report.md` |

**Last Updated**: 2026-06-12 17:40

## Sprint Health

| Metric | Status |
| -------- | -------- |
| Schedule | **P08-F4 ✅ Discovery 閉環** · INFRA-45 ✅ SSOT v4.0.6 · INFRA-43~44 ✅ |
| Compile | 0 errors ✅ (assembleDebug 18s) |
| Tests | passed ✅ (testDeviceDebugUnitTest — 19 suites, 0 failed) |
| Lint | 0 errors ✅ / 0 new warnings (ALLOWED files clean) |

## Active Blockers

| ID | Issue | Severity | Root Cause |
| ---- | ------- | ---------- | ------------ |
| I7 / BUG-I7-FOCUS-RING-PERSIST | FocusIndicatorOverlay 雙圓環持續顯示（實機確認） | 🔴 P1 | `EyeAfController` 每 2s 觸發新 `stableId` → `LaunchedEffect` 重置 `alphaTarget=1.0`，dwell fade 永遠被打斷。修復：Guard 7（`LensFocusState.Focused + drift < 閾值 → skip`）+ `lastTriggerPoint` instance var。 |
| BHA-01 | T02 Hard Gate — 歸一化 ✅，視覺 🔴 | 🟡 部分通過 | 歸一化數學 PASS（所有場景 normBbox [0,1]）。視覺 FAIL：4 個前置 Bug 阻塞。 |
| BUG-MLKIT-DUAL-EMIT | ML Kit 雙管線交替發射 | 🔴 P0 Blocker | 切換鏡頭/方向後舊鏡頭 DetectionResult 未清除。修復：`_state.update { copy(detectionResult = null) }` 或 `flatMapLatest`。 |
| BUG-LCSOURCE-HARDCODED-ROTATION | LiveCameraSource compensatedRotation 硬編碼 90° | 🟡 P1 | P08-F4 Discovery：Sony HAL 正常（直向=90）；原 BUG-SONY-ROTATION-CONST 假說推翻。兩裝置橫向時 `compensatedRotation` 仍套 90°，bitmap 強制 portrait。MVP 影響有限，待 P08-F4-FIX。 |
| BUG-PTF-HALO-SIZE | PTF 圓環渲染過大且不跟隨 | 🔴 P0 | PTF Halo bbox → 圓半徑轉換錯誤。座標數學已正確，純渲染 bug。 |
| BUG-MLKIT-PAREIDOLIA-FP | Ghost Face — K380 圓鍵觸發琥珀 Halo | ✅ HOTFIX-47 Traycer PASS | GF-01~03 雙裝置 PASS · K4-Rule 28 歸檔 · Sony F4 Discovery 待後續 |

## Next Actions

> **Session AK 完成（2026-06-12）：P08-F4 Discovery 完整閉環 — H3 REFUTED · 四組實機測試 · CSO Report 提交**

1. **🔴 CSO 待簽發**：P08-F4-FIX（方案 A：廢棄 Path B，SmartCameraAnalyzer 為唯一偵測路徑）
2. **🔴 Baseline（開 Ticket）**：
   - `BUG-MLKIT-DUAL-EMIT`：切換鏡頭清空 `detectionResult`（CameraViewModel.kt `flatMapLatest`）
   - `BUG-PTF-HALO-SIZE`：PTF Halo bbox → 圓半徑轉換（HaloGuidanceLayer.kt）
3. **P08-F5/F6** → **Portrait Contract v0.3 redline**（D-27 依賴已解除）
4. **T03 重啟**（Baseline 通過後）：Pill Toggle 重寫

## Active Debt（僅列未解決項）

- 🟡 SSOT-ORPHAN-RULE-01：PtfRuleAggregator 雙份邏輯（D10 KAIROS #3）
- 📝 Candidate Rule 17：有狀態平滑器 Lifecycle Gate（D10 歸入）
- 📝 INFRA-34（待下輪）：/sv Step 10.5 Session Note 自動歸檔職責

## D10 KAIROS #3 候選議程（Pre-W1-B 登記）

| ID | 候選項目 | 來源 | 類型 |
| ---- | --------- | ------ | ------ |
| K3-1 | Debug Overlay Zero Tolerance | D8 FIX-01~03 | CLAUDE.md 規則 |
| K3-2 | Hardcoded Text Scanning | D8 FIX-01~03 | /sv gate 擴充 |
| K3-3 | KAIROS Real-Device Screenshot Requirements | D8 FIX-01~03 | KAIROS 節點流程 |
| K3-4 | Decommission 票刪除驗收必須用 tracked filename glob 驗證 | FIX-INFRA-02-D | CLAUDE.md 規則 |
| K3-5 | Ticket 驗收清單與 SSOT 衝突時，以合併規格源為裁決依據 | TICKET-01 | CLAUDE.md 規則 |
| K3-6 | `cross_review.sh` payload 驗證 SV scope ↔ diff scope 一致性 | cross_review forensic | 腳本改進 |
| K3-7 | Edit 前強制 Re-view（>200 行目標檔 / /compact 後無條件觸發） | K3-7 | CLAUDE.md 規則 ✅ Rule 18 |

## Quick Links

- SSOT: `plans/architecture/SSOT_v4_0_6.md`
- Layout Spec: `plans/architecture/Cueshot_Layout_Spec_v2_5_1.md`
- Full History: `PROGRESS_ARCHIVE.md`
