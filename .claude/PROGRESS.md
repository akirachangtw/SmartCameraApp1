# Cueshot MVP Sprint Dashboard

**Sprint**: D0-D15 | **Branch**: `feature/cueshot-mvp`
**Current Day**: D12

| 最近 Ticket | 狀態 |
| ---------- | ---- |
| P08-F4-FIX | ✅ Pipeline Consolidation — SmartCameraAnalyzer 獨佔 ML Kit · SV+Cross-Review PASS |
| P08-F5 | ✅ S12 Executable Amendment — SSOT v4.0.6 Hotfix (HF-A1/A2/A3) + Contract v0.4 (14 CR) |
| P08-F4 | ✅ Discovery 完整閉環 — H3 REFUTED · CSO Report → `reports/P08-F4_Discovery_Report.md` |

**Last Updated**: 2026-06-14

## Sprint Health

| Metric | Status |
| -------- | -------- |
| Schedule | **P08-F4-FIX ✅ SV PASS** · INFRA-51 ✅ ssot_context v1.7 · 待 Traycer |
| Compile | 0 errors ✅ (assembleDebug 23s) |
| Tests | passed ✅ (testDeviceDebugUnitTest 37 tasks, 0 failed) |
| Lint | 0 errors ✅ (0 new warnings in ALLOWED files) |

## Active Blockers

| ID | Issue | Severity | Root Cause |
| ---- | ------- | ---------- | ------------ |
| I7 / BUG-I7-FOCUS-RING-PERSIST | FocusIndicatorOverlay 雙圓環持續顯示（實機確認） | 🔴 P1 | `EyeAfController` 每 2s 觸發新 `stableId` → `LaunchedEffect` 重置 `alphaTarget=1.0`，dwell fade 永遠被打斷。修復：Guard 7（`LensFocusState.Focused + drift < 閾值 → skip`）+ `lastTriggerPoint` instance var。 |
| BHA-01 | T02 Hard Gate — 歸一化 ✅，視覺 🔴 | 🟡 部分通過 | 歸一化數學 PASS（所有場景 normBbox [0,1]）。視覺 FAIL：4 個前置 Bug 阻塞。 |
| BUG-LCSOURCE-HARDCODED-ROTATION | LiveCameraSource compensatedRotation 硬編碼 90° | 🟡 P1 | P08-F4-FIX 完成雙管線修復；LiveCameraSource.kt 橫向 90° 殘留問題不在本 Ticket ALLOWED，另開 Ticket 追蹤。 |
| BUG-PTF-HALO-SIZE | PTF 圓環渲染過大且不跟隨 | 🔴 P0 | PTF Halo bbox → 圓半徑轉換錯誤。座標數學已正確，純渲染 bug。 |
| BUG-MLKIT-PAREIDOLIA-FP | Ghost Face — K380 圓鍵觸發琥珀 Halo | ✅ HOTFIX-47 Traycer PASS | GF-01~03 雙裝置 PASS · K4-Rule 28 歸檔 · Sony F4 Discovery 待後續 |

## Next Actions

> **S12 P08-F4-FIX（2026-06-15）：Pipeline Consolidation SV+Cross-Review PASS · 待 Traycer execute_review**

1. **🔴 P08-F4-FIX Traycer** → execute_review 終審（~$0.25）
2. **🔴 BUG-PTF-HALO-SIZE（下一 P0）**：PTF Halo bbox → 圓半徑轉換（HaloGuidanceLayer.kt）
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

## Quick Links

- SSOT: `plans/architecture/SSOT_v4_0_6.md`
- Layout Spec: `plans/architecture/Cueshot_Layout_Spec_v2_5_1.md`
- Full History: `PROGRESS_ARCHIVE.md`
