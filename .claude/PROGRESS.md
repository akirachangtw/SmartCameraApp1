# Cueshot MVP Sprint Progress

**Sprint**: D0-D15 (MVP)
**Last Updated**: 2026-04-08 by CSO
**Current Day**: D0 (絞殺者手術) — D0.1 pending
**Branch**: `feature/cueshot-mvp` (from v2.12.0-24 `bad1400`)
**Next KAIROS**: D2 (2026-04-09)

---

## 🎯 Sprint Health

| Metric | Target | Current | Status |
|--------|--------|---------|--------|
| On Schedule | D0 | D0 | 🟢 |
| Compile | 0 errors | — | — |
| Lint Warnings | 0 new | — | — |
| OpenCV Residue | 0 grep hits | — | — |
| IFTR Pipeline | Ready by D14 | ❌ Not started | — |

---

## 📋 Today's Tickets (D0 — 絞殺者手術)

| Ticket ID | Description | Status | Files Modified |
|-----------|-------------|--------|----------------|
| D0-T1 | `git checkout -b feature/cueshot-mvp` from v2.12.0-24 | 🔲 TODO | |
| D0-T2 | 移除 OpenCV dependency + SplashScreen 改為 ML Kit warm-up | 🔲 TODO | |
| D0-T3 | 移除 OpticalFlowTracker + 解耦 ImuCompensator | 🔲 TODO | |
| D0-T4 | 移除 Glassmorphism / Aurora Blue / Neon Glow UI | 🔲 TODO | |
| D0-T5 | 四級降級 → 三級裝置相容骨架 (FULL/LIMITED/LEGACY) | 🔲 TODO | |
| D0-T6 | 簡化 Focus Pipeline + 更新對焦 UI 顏色 (White/Green/Amber) | 🔲 TODO | |
| D0-T7 | 編譯驗證 + grep 殘留檢查 + PROGRESS.md 確認 | 🔲 TODO | |

### D0.1 — 編譯警告清創手術 (追加)

| Ticket ID | Description | Status | Files Modified |
|-----------|-------------|--------|----------------|
| D0.1-T1 | `./gradlew assembleDeviceDebug` 取得完整 Warning 清單 | 🔲 TODO | |
| D0.1-T2 | 分類 Warning：Deprecation / Unused / Nullability / Cast | 🔲 TODO | |
| D0.1-T3 | 逐一修復所有 Warning（零容忍政策） | 🔲 TODO | |
| D0.1-T4 | 重新編譯確認 Warning = 0 new | 🔲 TODO | |

Status: 🔲 TODO → 🔄 WIP → ✅ DONE → 🚫 BLOCKED

---

## 📌 Blocking Issues

| Issue | Severity | Discovered | Resolution |
|-------|----------|------------|------------|
| (none) | — | — | — |

---

## 📅 Sprint Roadmap (D0-D15)

| Day | Date | Deliverable | KAIROS |
|-----|------|------------|--------|
| **D0** | 04-07 | 絞殺者手術：移除技術債，建立乾淨基底 | — |
| **D1** | 04-08 | CameraX preview + Tap-to-Focus + Edge Warmth 第一層 (pitch) | — |
| **D2** | 04-09 | Edge Warmth 完整 (roll) + 場景自動切換 | **#1** |
| **D3** | 04-10 | 水平儀 + 三分法網格 overlay | — |
| **D4** | 04-13 | Edge Warmth 第二層（浮動光點）+ 呼吸韻律初版 | — |
| **D5** | 04-14 | 優先級引擎骨架 + ThirdsRule + HeadroomRule | — |
| **D6** | 04-15 | EyeLineRule + GazeRule + HaloGuidanceLayer + Eye-AF | **#2** |
| **D7** | 04-16 | PTF Tab toggle + 極簡 UI + 1s 快門冷卻 | — |
| **D8** | 04-17 | PTF 規則引擎 + Halo 牽引 + 首次教學動畫 | — |
| **D9** | 04-20 | ML Kit Object Detection FOOD + 美食對焦 (3s 冷卻) | — |
| **D10** | 04-21 | FoodCompositionRule + MinimalistRule + 雜亂背景 UI | **#3** |
| **D11** | 04-22 | 鏡頭髒汙偵測（純 Kotlin Laplacian，無 OpenCV） | — |
| **D12** | 04-23 | 光線診斷（直方圖）+ 呼吸韻律完整版 + 三級裝置降級實作 | — |
| **D13** | 04-24 | Paywall + 評分引擎 + EXIF 寫入 + 拍後教學卡片 + DOF 骨架 | **#4** |
| **D14** | 04-27 | IFTR 埋點 + Settings UI + 多語系 (en/zh-TW/ja) | — |
| **D15** | 04-28 | 整合測試 + 餐廳實測 + 參數微調 | **#5 Final** |

---

## ✅ Completed (Cumulative)

### D0 (2026-04-07) — 絞殺者手術
- [ ] Branch created: `feature/cueshot-mvp`
- [ ] OpenCV removed
- [ ] OpticalFlowTracker removed, ImuCompensator decoupled
- [ ] Glassmorphism / Aurora Blue / Neon Glow removed
- [ ] 4-tier degradation → 3-tier DeviceCapabilityTier
- [ ] Focus Pipeline simplified (Tap-to-Focus + cooldown only)
- [ ] Compile PASS + grep residue = 0

---

## 🧠 Active Decisions / Deviations from SSOT

| Decision | SSOT Reference | Deviation? | Rationale |
|----------|---------------|------------|-----------|
| LowAngle → General fallback | §3.1 | Confirmed | v3.3: no LowAngleMode |
| SmileRule = Phase 1.5 | §4.4 | Confirmed | Cut from MVP |
| DOF = UI skeleton only | §12 MVP | Confirmed | Phase 1.5 implementation |
| Coaching intensity hidden | Tech v3 §5.1 | Confirmed | Needs IFTR baseline data |

---

## 🔴 KAIROS Findings (Auto-populated by /dream)

### KAIROS #1 — D2 (2026-04-09)
- (will be populated: IMU accuracy, Edge Warmth alpha tuning, scene switch hysteresis)

### KAIROS #2 — D6 (2026-04-15)
- (will be populated: ML Kit stability, Face Detection params, Halo smoothness)

### KAIROS #3 — D10 (2026-04-21)
- (will be populated: Object Detection FOOD confidence, bgCV thresholds, MinimalistRule coverage)

### KAIROS #4 — D13 (2026-04-24)
- (will be populated: scoring weights, Paywall UX, blur quality)

### KAIROS #5 Final — D15 (2026-04-28)
- (will be populated: IFTR initial data, device degradation, i18n quality, launch readiness)

---

## 📎 Quick Links

- SSOT: `SmartCameraApp_Strategy_v3_3_SSOT_20260404.md`
- Tech Spec: `SmartCameraApp_TechDeepDive_v3_20260405.md`
- Roadmap: `Cueshot_MVP_Phase1_Roadmap_v1.md`
- Architecture: CLAUDE.md §Architecture
- Gemini Prompts: `Cueshot_Gemini_Architect_Prompt_v2.md`
- D0 Surgery Package: `Cueshot_D0_Gemini_Feeding_Package.md`
- Last Handoff: `MVP策略對話Handoff_20260406.md`

---

## 💳 Debt / Backlog (技術債與待辦)

- [ ] [Infrastructure] Kapt to KSP migration (保留 K2 編譯器相容性，推遲至 MVP D6 階段後再執行，目前維持 1.9 降級模式以避免依賴重構的範圍蔓延)

---

## 🗄️ Standby Mechanisms (Phase 2 啟用)

- Merge SOP: `devspec/MERGE_SOP.md` — 多分支時啟用
- Branch Releases: `devspec/BRANCH_RELEASES.md` — 多分支時啟用
- 啟用條件：第二位開發者加入 / 產品上架後維護 / 跨版本回歸測試
