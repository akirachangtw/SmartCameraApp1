# 焦土掃描報告 (Scorched Earth Report)

**掃描日期**: 2026-05-05
**基準規格**: `Merged Spec v3.4.1-Stage0`

---

## 1. 檔案盤點

| 檔案路徑 | 狀態 | 行動 | 備註 |
|---------|------|------|------|
| `domain/model/CompositionResult.kt` | ✅穩定 | 維持現狀 | 已取代 `CompositionScore` |
| `domain/composition/PriorityEngine.kt` | ✅穩定 | 維持現狀 | 已提取為獨立類別 |
| `domain/usecase/AnalyzeCompositionUseCase.kt` | ⚠️需改 | 修改 `isAllAligned` 邏輯 | 目前使用 `topInstruction == null`，未完全遵照動態聚合（`rules.all`） |
| `Presentation/.../PtfTabBar.kt` | ✅已清理 | 無 | 掃描未發現殘留，已被完全作廢，將由 Pill Toggle 取代 |
| `domain/model/DeviceTierClassifier.kt` | 🆕缺失 | 需新增 | 支援 PART 8 三級裝置降級 (FULL/LIMITED/LEGACY) 的核心判斷邏輯 |
| `domain/.../FreeTierLimitTracker.kt` | 🆕缺失 | 需新增 | 預留 PART 16 Paywall 相關介面 |

---

## 2. 基礎健康

| 檢查項 | 狀態 | 問題描述 |
|--------|------|---------|
| CameraX PreviewView Binding | 🟢 正常 | `CameraScreen.kt` 中綁定正常 |
| ML Kit Face Detection | 🟢 正常 | `MLKitDetector.kt` 與 `SmartCameraAnalyzer.kt` 初始化與呼叫正常 |
| Hilt DI Module | 🟢 正常 | Clean Architecture 分層有正確的 `@Inject` 與依賴注入設計 |
| `assembleDebug` 編譯狀態 | 🟢 執行中 | Gradle build 正常執行中（含 OpenCV NDK 編譯），未發現語法或結構性編譯錯誤 |

---

## 3. 架構對齊度

| 規格要求 | 現狀 | 差距 |
|---------|------|------|
| `CompositionResult` 是否已取代 `CompositionScore` | 完全取代 | 無差距 |
| `PriorityEngine` 是否為獨立 class | 是獨立的 class | 無差距 |
| `isAllAligned` 是否使用動態聚合 `rules.all { ... }` | 使用 `topInstruction == null` 判斷 | **有差距**：規格要求明確用 `rules.all` 動態聚合各 rule 的 boolean 狀態，目前的寫法過於依賴優先級引擎的回傳值，可能產生 Edge case。 |
| Clean Architecture 層次是否正確分離 | `domain`, `data`, `presentation` 包結構清晰 | 無差距 |

---

## 4. 粗拆票建議 (Ticket Breakdown)

| 票序 | 範圍 | 前置依賴 | 估算 |
|------|------|---------|------|
| **Ticket-1** | 修正 `AnalyzeCompositionUseCase` 中的 `isAllAligned` 邏輯，改為 `rules.all { it.evaluate().isAligned }`。 | 無 | 0.5 Story Point |
| **Ticket-2** | 實作 `DeviceTierClassifier` (分級降級邏輯，支援 PART 8)。 | Ticket-1 | 1 Story Point |
| **Ticket-3** | 實作 `FreeTierLimitTracker` & `PaywallTrigger` 介面結構 (PART 16)。 | 無 | 0.5 Story Point |
| **Ticket-4** | 實作全新的 Pill Toggle 元件 (PART 3) 以完全取代舊的 PTF 進入點，並加入 Bottom Sheet 過渡。 | 無 | 2 Story Points |
| **Ticket-5** | 實作 Edge Warmth (PART 4) 與 Halo (PART 5) 的最新視覺規格對齊與重構。 | Ticket-2 | 3 Story Points |
