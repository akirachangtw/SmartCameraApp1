# Cueshot MVP Sprint Dashboard

**Sprint**: D0-D15 | **Branch**: `feature/cueshot-mvp`
**Current Day**: D9 → P0-TOPBAR + P0-BOTTOM ✅ CLOSED（QA ✅ / SV ✅ / Device ✅ — BUG-SHUTTER-RING-OFFSET fixed, 實機驗收通過）
**Last Updated**: 2026-05-14

---

## Sprint Health

| Metric | Status |
| -------- | -------- |
| Schedule | Stage0 W1 ✅ CLOSED — FIX-CROSSREVIEW-01 ✅ CLOSED — FIX-P0-SESSION-A ✅ CLOSED — P0-TOPBAR+P0-BOTTOM ✅ Device ✅ CLOSED |
| Compile | 0 errors ✅ (assembleDeviceDebug 6s) |
| Tests | passed ✅ (testDeviceDebugUnitTest) |
| Lint | 0 errors ✅ / 0 new warnings (baseline unchanged) |

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

### Source Documents (Phase 0 現行基準)

- **SSOT v4.0**: `plans/architecture/SSOT_v4_0.md` ← 唯一真理文件
- **Layout Spec v2.2**: `plans/architecture/Cueshot_Layout_Spec_v2_2.md` ← 佈局精確定義
- **Phase0 UI Contract**: `plans/cso_briefing/Phase0_UI_Realignment_Contract_v1.md`

---

## Active Blockers

| ID | Issue | Severity | Root Cause |
| ---- | ------- | ---------- | ------------ |
| ~~I8~~ | ~~IFTR 歸零 — isAllAligned 永遠 false~~ | ✅ 已修復 | TICKET-00 — 改為加權平均 ≥ 70 |
| ~~I9~~ | ~~Traycer Path B 裁決阻擋 W2 啟動~~ | ✅ 已解除 | FIX-INFRA-02-FINAL + FIX-INFRA-02-D 完成 OpenCV 全鏈路清除，Path B 唯一 blocker 已修補 |
| I7 / BUG-I7-FOCUS-RING-PERSIST | FocusIndicatorOverlay 雙圓環持續顯示（實機確認） | 🔴 P1 | `EyeAfController` 每 2s 觸發新 `stableId` → `LaunchedEffect` 重置 `alphaTarget=1.0`，dwell fade 永遠被打斷。圓環為 `drawSearchingBase` 設計的 3 層同心圓（非兩 Overlay 疊加）。修復：Guard 7（`LensFocusState.Focused + drift < 閾值 → skip`）+ `lastTriggerPoint` instance var。 |
| BHA-01 | T02 Hard Gate — 歸一化 ✅，視覺 🔴 | 🟡 部分通過 | 歸一化數學 PASS（所有場景 normBbox [0,1]）。視覺 FAIL：4 個前置 Bug 阻塞（見下方）。 |
| BUG-MLKIT-DUAL-EMIT | ML Kit 雙管線交替發射 | 🔴 P0 Blocker | 切換鏡頭/方向後舊鏡頭 DetectionResult 未清除，與新鏡頭結果交替輸出 → Halo 忽大忽小 + 閃爍出現/消失。修復方向：切換時 `_state.update { copy(detectionResult = null) }` 或 `flatMapLatest`。 |
| BUG-SONY-ROTATION-CONST | Sony rotation 恆為 0 | 🔴 P0 | Sony XQ-BT52 直立/橫向皆回報 `rotationDegrees=0`，rotation swap 邏輯無法觸發。需裝置相容性處理或改用 CameraX Surface rotation。 |
| BUG-PTF-HALO-SIZE | PTF 圓環渲染過大且不跟隨 | 🔴 P0 | PTF Halo 渲染層 bbox → 圓半徑轉換錯誤。座標數學已正確，純渲染 bug。 |

## Next Actions

> **Session C 目標：P0-FPS + P0-FOCUS + Baseline**

1. **🔴 P0-FPS**（Sony FPS overlay 修正 + `BUG-SONY-ROTATION-CONST` 調查）：
   - `CameraViewModel.kt` — 調查 `imageInfo.rotationDegrees` vs `Surface.ROTATION_*`；Sony XQ-BT52 直立/橫向恆回報 `rotationDegrees=0`
   - FPS debug overlay 是否符合 Zero Tolerance 規則（Rule 19）
2. **🔴 P0-FOCUS**（`BUG-I7-FOCUS-RING-PERSIST`）：
   - `EyeAfController.kt:process()` 加入 Guard 7：`LensFocusState.Focused + drift < 閾值 → skip`
   - 加入 `lastTriggerPoint` instance var 防止 2s 週期重置 `alphaTarget`
3. **🔴 Baseline**：
   - `BUG-MLKIT-DUAL-EMIT`：`CameraViewModel.kt` 切換鏡頭清空 `detectionResult` 或改用 `flatMapLatest`
   - `BUG-PTF-HALO-SIZE`：PTF Halo 渲染層 bbox → 圓半徑轉換修正
4. **T03 重啟**（以上通過後）：Pill Toggle 重寫

## Active Debt（僅列未解決項）

- 🟡 SSOT-ORPHAN-RULE-01：PtfRuleAggregator 雙份邏輯（D10 KAIROS #3）
- 📝 Candidate Rule 17：有狀態平滑器 Lifecycle Gate（D10 歸入）

## D10 KAIROS #3 候選議程（Pre-W1-B 登記）

| ID | 候選項目 | 來源 | 類型 |
| ---- | --------- | ------ | ------ |
| K3-1 | Debug Overlay Zero Tolerance（debug 文字覆蓋零容忍） | D8 FIX-01~03 | CLAUDE.md 規則 |
| K3-2 | Hardcoded Text Scanning（硬編碼文字掃描） | D8 FIX-01~03 | /sv gate 擴充 |
| K3-3 | KAIROS Real-Device Screenshot Requirements | D8 FIX-01~03 | KAIROS 節點流程 |
| K3-4 | Decommission 票刪除驗收必須用 tracked filename glob 驗證 | FIX-INFRA-02-D | CLAUDE.md 規則 |
| K3-5 | Ticket 驗收清單與 SSOT 衝突時，以合併規格源為裁決依據並記錄衝突吸收 | TICKET-01 | CLAUDE.md 規則 |
| K3-6 | `cross_review.sh` payload 組裝時驗證 SV scope ↔ diff scope 一致性 | cross_review forensic | 腳本改進 |
| K3-7 | Edit 操作前強制 Re-view（>200 行目標檔 / /compact 後無條件觸發） | K3-7 Session Boundary Rule 修訂 | CLAUDE.md 規則 ✅ 已寫入 Rule 18 |

## Quick Links

- SSOT: `plans/architecture/SSOT_v4_0.md`
- Layout Spec: `plans/architecture/Cueshot_Layout_Spec_v2_2.md`
- Full History: `PROGRESS_ARCHIVE.md`
