# Cueshot MVP Sprint Dashboard

**Sprint**: D0-D15 | **Branch**: `feature/cueshot-mvp`
**Current Day**: D12

| 最近 Ticket | 狀態 |
| ---------- | ---- |
| P08-F4-FIX-v2-REPAIR-3 | 🟡 /sv Cross-Review PASS · 待 Traycer execute_review + 真機 AC-1~AC-4 |
| P08-F4-FIX-v2-REPAIR-2 | 🟡 REPAIR-3 補完 · 待 Traycer + 真機 Hard Gate |
| P08-F4-FIX-v2-REPAIR | 🟡 REPAIR chain 完成 · 累積 Traycer briefing 待送審 |
| INFRA-52 | ✅ structural_verify v2.0 — 四級 verdict + lib_verify + policy env |
| P08-F4-FIX-REPAIR | ✅ Traycer execute_review PASS · 閉環（模擬器 only） |

**Last Updated**: 2026-06-17

## Sprint Health

| Metric | Status |
| -------- | -------- |
| Schedule | **S16** · P08-F4-FIX-v2-REPAIR-3 /sv Cross-Review PASS → 待 Traycer execute_review + 真機 AC-1~AC-4 |
| Compile | 0 errors ✅ (assembleDebug PASS, both device+emulator variants, 28s) |
| Tests | 362 passed ✅ (testDeviceDebugUnitTest + testEmulatorDebugUnitTest, 0 failed) |
| Lint | 0 errors ✅ (0 new warnings in ALLOWED files) |

## Active Blockers

| ID | Issue | Severity | Root Cause |
| ---- | ------- | ---------- | ------------ |
| I7 / BUG-I7-FOCUS-RING-PERSIST | FocusIndicatorOverlay 雙圓環持續顯示（實機確認） | 🔴 P1 | `EyeAfController` 每 2s 觸發新 `stableId` → `LaunchedEffect` 重置 `alphaTarget=1.0`，dwell fade 永遠被打斷。修復：Guard 7（`LensFocusState.Focused + drift < 閾值 → skip`）+ `lastTriggerPoint` instance var。 |
| BHA-01 | T02 Hard Gate — 歸一化 ✅，視覺 🔴 | 🟡 部分通過 | 歸一化數學 PASS（所有場景 normBbox [0,1]）。視覺 FAIL：4 個前置 Bug 阻塞。 |
| BUG-P08-F4-REAL-DEVICE | P08-F4-FIX 真機管線斷線（P08-F4-FIX-v2 /sv Cross-Review PASS）：LiveCameraSource 注入 FaceDetector + `onDetectionResult` callback，fromBitmap portrait-space，rotationDegrees=0。待 Traycer execute_review + 真機 AC-1~AC-4 Hard Gate。 | 🟡 Traycer 待審 | Contract C2 已實作，Cross-Review ADVISORY。 |
| BUG-LCSOURCE-HARDCODED-ROTATION | LiveCameraSource compensatedRotation 硬編碼（後=90°, 前=270°），橫向 HAL=0 但常數不變 → 後+90°、前+270° 偏差。S15 四場景實機量化完成。 | 🟡 P1 | 依賴 P08-F4-FIX 真機修復完成後開立 P08-F9。`LiveCameraSource.kt:340` |
| BUG-PTF-HALO-SIZE | PTF 圓環渲染過大且不跟隨（三重根因：固定中心 / 固定半徑 / alpha 偏高） | 🔴 P0 | P08-F7 待實作：`HaloGuidanceLayer.kt` bbox 跟隨 + alpha 呼吸韻律修正。 |
| BUG-MLKIT-PAREIDOLIA-FP | Ghost Face — K380 圓鍵觸發琥珀 Halo | ✅ HOTFIX-47 Traycer PASS | GF-01~03 雙裝置 PASS · K4-Rule 28 歸檔 · Sony F4 Discovery 待後續 |

## Next Actions

> **S16 P08-F4-FIX-v2（2026-06-17）：LiveCameraSource detection 接線 /qa PASS · Contract C2 fromBitmap portrait-space · 互斥 guard · F4v2-Diag 7 欄位**

1. **🟡 P08-F4-FIX-v2（P0）**：/sv → Traycer execute_review → 真機 Sony XQ-BT52 AC-1~AC-4 Hard Gate（後/前鏡頭 Halo + PTF）
2. **🔴 P08-F7（P0）**：PTF Halo bbox 跟隨 + alpha 修正（`HaloGuidanceLayer.kt`，可與 F4v2 驗收平行）
3. **P08-F6** → Portrait Contract v0.4 實作（domain/portrait/ 骨架建立）
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

## D13 KAIROS #4 候選議程（Pre-D13 登記）

| ID | 候選項目 | 來源 | 類型 |
| ---- | --------- | ------ | ------ |
| K4-21 | Scope Ban Wording Guideline → `.claude/rules/scope-ban-governance.md`（Perplexity 6 點框架：禁絕對語氣 / Net-New vs Name-New / 行內例外具名化 / 指標對應） | P08-F4-FIX BLOCKER + CSO Override | rules 文件 |
| K4-22 | ssot_context.md 事實數據（行數/倍率）隨 Ticket 閉環自動更新機制 | CameraViewModel 2457→2344 漂移 | 自動化 |
| K4-23 | 靜態驗收 + 模擬器 QA 無法捕捉真機專屬 Camera 後端分流 bug → execute_review 後仍 **MUST** 真機驗收方可標 DONE | P08-F4-FIX S15 真機 FAIL | CLAUDE.md 規則 |
| K4-24 | Discovery Report 已記載「Path A 從不執行」，FIX 卻扶正 Path A 未修根因 → FIX Ticket **MUST** 對 Discovery 前提缺陷逐項閉環 | P08-F4 Discovery vs P08-F4-FIX 矛盾 | CLAUDE.md 規則 |

## Quick Links

- SSOT: `plans/architecture/SSOT_v4_0_6.md`
- Layout Spec: `plans/architecture/Cueshot_Layout_Spec_v2_5_1.md`
- Full History: `PROGRESS_ARCHIVE.md`
