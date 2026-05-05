# Cueshot 規格 × 代碼 驗證矩陣 v1.0

**建立日期**: 2026-05-05
**建立者**: CSO (Claude Opus 4.6)
**用途**: GPT-5.5 全任務執行的主要輸入，取代 Gemini Phase A 焦土報告
**搭配文件**: `Cueshot_Merged_Spec_v3_4_1_Stage0.md`（合併規格包）

---

## 使用說明

本矩陣列出合併規格包 18 個 PART 對應的**預期檔案、預期行為、驗證方法**。
GPT-5.5 逐行掃描 codebase，對每一項給出判定：

| 判定 | 含義 |
|------|------|
| ✅ MATCH | 檔案存在且邏輯對齊規格 |
| ⚠️ DRIFT | 檔案存在但邏輯偏離規格（需修復票） |
| ❌ ABSENT | 檔案不存在（需新建票） |
| 🔴 CONFLICT | 存在且違反規格（需重寫票，高優先） |
| 🗑️ ZOMBIE | 存在但規格已作廢（需刪除票） |

---

## PART 1：全局常數

| # | 預期 | 驗證方法 | Gemini 報告 |
|---|------|---------|------------|
| 1.1 | `AmbientConstants` object 存在，包含 `ALIGNED_THRESHOLD_DEG = 5f` | 搜尋 `object AmbientConstants` 或等效常數定義 | ❓未提及 |
| 1.2 | `LEVEL_FADE_RANGE_DEG = 5f` | 同上 | ❓未提及 |
| 1.3 | `SCRIM_HEIGHT_PX = 120f` | 搜尋 Scrim 相關常數，確認非 72 | ❓未提及 |
| 1.4 | `HYSTERESIS_SCENE_MS = 500L` | 搜尋場景切換延遲常數 | ❓未提及 |
| 1.5 | `BREATHE_CYCLE_MS = 2000L` | 搜尋呼吸動畫週期 | ❓未提及 |
| 1.6 | `SHUTTER_FLASH_DURATION_MS = 300L` | 搜尋快門閃爍時長 | ❓未提及 |
| 1.7 | 所有常數集中在單一 object（非散落各處） | 確認無重複定義 | ❓未提及 |

---

## PART 2：螢幕佈局（兩區塊模型）

| # | 預期 | 驗證方法 | Gemini 報告 |
|---|------|---------|------------|
| 2.1 | Viewfinder 高度 664px（78.7%）—填滿至螢幕頂部 | 檢查 CameraX PreviewView 的 layout 參數 | ❓未提及 |
| 2.2 | 底部控制列 180px（21.3%） | 檢查底部 Composable 的高度定義 | ❓未提及 |
| 2.3 | Top Bar 是浮動 Overlay + Scrim，不是獨立不透明區塊 | 搜尋 Top Bar Composable，確認 Scrim 漸層實作 | ❓未提及 |
| 2.4 | Scrim: #000000 α0.22 → α0 @ **120px**（非 72px） | 搜尋 Scrim 高度值 | ❓未提及 |
| 2.5 | Overlay Layer 0-6 正確堆疊 | 確認 Compose Box/Canvas z-order | ❓未提及 |
| 2.6 | 三分網格線 #FFFFFF α0.08, 線寬 0.5px | 搜尋 grid 繪製邏輯 | ❓未提及 |
| 2.7 | 分隔線 #FFF α0.05 在 y=664px | 檢查 Viewfinder 與底部之間的 divider | ❓未提及 |

---

## PART 3：Pill Toggle

| # | 預期 | 驗證方法 | Gemini 報告 |
|---|------|---------|------------|
| 3.1 | Pill Toggle Composable 存在（取代 PtfTabBar） | 搜尋 Pill/Toggle 相關 Composable | Ticket-4 標記需新建 |
| 3.2 | 尺寸 128dp × 28dp，圓角 14dp | 檢查 Modifier 參數 | — |
| 3.3 | 選中指示器 60dp × 24dp，fill #4A4A4A | 檢查指示器繪製 | — |
| 3.4 | 圖示 Camera (`CameraAlt`) / Users (`People`)，20dp | 檢查 Icon 引用 | — |
| 3.5 | 選中 #CCCCCC / 未選中 #666666 | 檢查 tint 色值 | — |
| 3.6 | 切換動畫 300ms FastOutSlowInEasing | 檢查 animationSpec | — |
| 3.7 | 零 CameraX rebind / 零 ML Kit close | 確認切換邏輯只改 Compose state | — |
| 3.8 | 快門冷卻 1 秒（進入 PTF 後） | 搜尋冷卻邏輯 | — |
| 3.9 | 首次 PTF Bottom Sheet（max(screenHeight×0.20, 96dp)） | 搜尋 Bottom Sheet 觸發邏輯 | — |
| 3.10 | 底色過渡 #0D0D0D ↔ #1A1714, 300ms | 搜尋 animateColorAsState | — |
| 3.11 | PTF 進入：隱藏 Gallery/Flip + 浮現 ✕ + 快門 72→80dp | 搜尋 PTF 模式 UI 差異 | — |
| 3.12 | `PtfTabBar.kt` 已完全作廢，無殘留引用 | 全局搜尋 `PtfTabBar` | Gemini 報告 ✅已清理 |

---

## PART 4：Edge Warmth + Pitch/Level Traction

| # | 預期 | 驗證方法 | Gemini 報告 |
|---|------|---------|------------|
| 4.1 | `PitchTractionConfig` data class 存在 | 搜尋 class 定義 | ❓未提及 |
| 4.2 | `FOOD_45` companion: targetDeg=45, orbitRatio=0.30, aligned=**5f** | 檢查參數值（非 3f） | ❓未提及 |
| 4.3 | `FOOD_90` companion: targetDeg=90, orbitRatio=0.20, aligned=**5f** | 同上 | ❓未提及 |
| 4.4 | `selectPitchConfig` 函數：hysteresis 70°↔80° | 檢查切換邏輯 | ❓未提及 |
| 4.5 | `drawEdgeWarmth` 函數存在，邊緣向內 60px | 搜尋繪製函數 | ❓未提及 |
| 4.6 | Edge Warmth 顏色 #D4A030 α≤0.20 | 檢查色值 | ❓未提及 |
| 4.7 | Level Traction 漸入公式：`(maxDeviation / 5f).coerceIn(0f, 1f)` | 搜尋 opacity 計算邏輯 | ❓未提及 |
| 4.8 | Level Traction 對齊即消失（α→0，非保留基底） | 確認 0° 時 opacity = 0 | ❓未提及 |
| 4.9 | 橫拍 Level Traction MVP 隱藏 | 搜尋 orientation 判斷 | ❓未提及 |
| 4.10 | 場景映射表六場景各自的 Edge Warmth 行為正確 | 逐場景追蹤邏輯 | ❓未提及 |

---

## PART 5：Halo

| # | 預期 | 驗證方法 | Gemini 報告 |
|---|------|---------|------------|
| 5.1 | 單人 Halo：跟隨 face bbox → 三分法 | 搜尋 Halo 繪製邏輯 | ❓未提及 |
| 5.2 | 單人 EMA 0.7/0.3 | 檢查平滑參數 | ❓未提及 |
| 5.3 | 美食 Halo：跟隨最大 food bbox（策略 A） | 搜尋 `maxByOrNull { boundingBox.area() }` 或等效 | ❓未提及 |
| 5.4 | 美食 EMA 0.8/0.2 | 檢查平滑參數 | ❓未提及 |
| 5.5 | `GroupHaloResult` data class 存在 | 搜尋 class 定義 | ❓未提及 |
| 5.6 | `calculateGroupHalo` 函數：人數依賴 target（2人→y33%, 3人→y40%, 4+→y50%） | 檢查 when 分支 | ❓未提及 |
| 5.7 | `smoothGroupHalo` 函數：EMA 0.75/0.25 | 檢查平滑參數 | ❓未提及 |
| 5.8 | 群組 alpha = 單人 alpha − 0.05 | 檢查 alpha 計算 | ❓未提及 |
| 5.9 | Halo 顏色：未對齊 #D4A030 / 對齊 #50C864 | 檢查色值 | ❓未提及 |
| 5.10 | PTF Halo：中心引力場（target = 畫面中心） | 搜尋 PTF 模式的 Halo target | ❓未提及 |

---

## PART 6：PTF 模式特殊規格

| # | 預期 | 驗證方法 | Gemini 報告 |
|---|------|---------|------------|
| 6.1 | PTF 規則引擎：FramingRule + LevelRule + AngleRule（三規則簡化版） | 搜尋 PTF 模式的規則配置 | ❓未提及 |
| 6.2 | PTF Level Traction 固定綠色 #50C864（不經 amber→green 漸變） | 搜尋 `isPtfMode` 相關顏色邏輯 | ❓未提及 |
| 6.3 | PTF Level reference 層維持白色 | 確認 reference 層不受 PTF 影響 | ❓未提及 |

---

## PART 7：快門外環動畫互斥

| # | 預期 | 驗證方法 | Gemini 報告 |
|---|------|---------|------------|
| 7.1 | `isBreathing` flag 存在，控制呼吸 vs 閃爍互斥 | 搜尋狀態旗標 | ❓未提及 |
| 7.2 | 快門按下 → 暫停呼吸 → 閃爍 300ms → 冷卻 → 條件恢復呼吸 | 追蹤 `onShutterPressed` 時序 | ❓未提及 |
| 7.3 | 呼吸：α 0.10↔0.18, 2000ms, FastOutSlowInEasing | 檢查 InfiniteTransition 參數 | ❓未提及 |
| 7.4 | 閃爍：白 α0.45→0, 300ms | 檢查 Animatable 參數 | ❓未提及 |
| 7.5 | LEGACY 冷卻 500ms（+200ms 靜態等待） | 搜尋裝置等級分支 | ❓未提及 |

---

## PART 8：三級裝置降級

| # | 預期 | 驗證方法 | Gemini 報告 |
|---|------|---------|------------|
| 8.1 | `DeviceTierClassifier` 存在 | 搜尋 class 定義 | 🆕缺失（Gemini 確認） |
| 8.2 | 分類邏輯：FULL / LIMITED / LEGACY | 檢查分類依據（CPU 核心數? RAM? benchmark?） | — |
| 8.3 | FULL: 所有功能啟用 | 逐功能確認 flag | — |
| 8.4 | LIMITED: 關閉雜亂框 | 確認 flag 控制 | — |
| 8.5 | LEGACY: 關閉髒汙+減法+雜亂+光線 | 確認 flag 控制 | — |
| 8.6 | LEGACY 群組 Halo → 最大人臉單人 Halo | 搜尋降級邏輯 | — |
| 8.7 | Edge Warmth 60fps 全等級保證 | 確認無裝置等級相關的 EW 降級 | — |

---

## PART 9：Canvas 呼叫預算

| # | 預期 | 驗證方法 | Gemini 報告 |
|---|------|---------|------------|
| 9.1 | 每幀 drawXxx < 15 次的預算管理 | 計數每個 State 的 Canvas 呼叫 | ❓未提及 |
| 9.2 | State 2 Level reference 預渲染 Bitmap 優化 | 搜尋 Bitmap cache 邏輯 | ❓未提及 |

---

## PART 10：美食模式規則表

| # | 預期 | 驗證方法 | Gemini 報告 |
|---|------|---------|------------|
| 10.1 | `AngleRule` 存在，IMU pitch 驅動 | 搜尋 Rule class | ❓未提及 |
| 10.2 | `LevelRule` 存在，IMU roll 驅動 | 同上 | ❓未提及 |
| 10.3 | `FoodCompositionRule` 存在，ML Kit Object Detection bbox 驅動 | 同上 | ❓未提及 |
| 10.4 | `LightRule` 存在，直方圖驅動 | 同上 | ❓未提及 |
| 10.5 | `MinimalistRule` 存在，bbox ratio + background CV 驅動 | 同上 | ❓未提及 |
| 10.6 | 付費/免費 flag 可控制 FoodComposition/Light/Minimalist | 搜尋 flag 邏輯 | ❓未提及 |

---

## PART 11：Focus Ring

| # | 預期 | 驗證方法 | Gemini 報告 |
|---|------|---------|------------|
| 11.1 | Focus Ring 四狀態動畫（Focusing/Focused/Failed/Locked） | 搜尋 Focus Ring Composable | ❓未提及 |
| 11.2 | Failed 震動 **4px**（非 2px） | 檢查振幅值 | ❓未提及 |
| 11.3 | Locked: 雙圈 + "AE/AF" 小字 | 搜尋 Locked 狀態渲染 | ❓未提及 |

---

## PART 12：呼吸韻律

| # | 預期 | 驗證方法 | Gemini 報告 |
|---|------|---------|------------|
| 12.1 | `allConditionsMet` → 快門外環呼吸 α0.10↔0.18 | 搜尋呼吸觸發條件 | ❓未提及 |
| 12.2 | `InfiniteTransition` scoped inside conditional（效能） | 確認不在無條件 Composable 中 | ❓未提及 |

---

## PART 13：場景判斷邏輯

| # | 預期 | 驗證方法 | Gemini 報告 |
|---|------|---------|------------|
| 13.1 | 場景優先級：PTF（手動）> pitch>35°（Food）> face（Portrait）> General | 搜尋場景判斷邏輯 | ❓未提及 |
| 13.2 | 切換 hysteresis 500ms | 搜尋防跳動邏輯 | ❓未提及 |

---

## PART 14：效能分幀策略

| # | 預期 | 驗證方法 | Gemini 報告 |
|---|------|---------|------------|
| 14.1 | 五幀交錯：ML Kit / Histogram / ML Kit / Histogram / Lens Smudge | 搜尋分幀調度邏輯 | ❓未提及 |

---

## PART 15-16：Freemium / Paywall

| # | 預期 | 驗證方法 | Gemini 報告 |
|---|------|---------|------------|
| 15.1 | `FreeTierLimitTracker` interface | 搜尋 interface 定義 | 🆕缺失（Gemini 確認） |
| 15.2 | `PaywallTrigger` interface | 同上 | 🆕缺失 |
| 15.3 | `PaywallEvent` enum | 同上 | 🆕缺失 |
| 15.4 | 付費功能 flag 控制（FoodComposition/Light/Minimalist） | 搜尋 feature flag 機制 | ❓未提及 |

---

## 已確認項（Gemini 報告可信部分）

| # | 項目 | Gemini 判定 | 可信度 |
|---|------|-----------|--------|
| G1 | `CompositionResult` 已取代 `CompositionScore` | ✅穩定 | 高 |
| G2 | `PriorityEngine` 為獨立 class | ✅穩定 | 高 |
| G3 | `PtfTabBar.kt` 已清理無殘留 | ✅已清理 | 高 |
| G4 | CameraX PreviewView binding 正常 | 🟢 | 中（需行為驗證） |
| G5 | ML Kit Face Detection 初始化正常 | 🟢 | 中（需行為驗證） |
| G6 | Hilt DI 正常 | 🟢 | 中 |
| G7 | `isAllAligned` 使用 `topInstruction == null`（需改為 `rules.all`） | ⚠️需改 | 高 |

---

## 統計摘要

| 類別 | 數量 |
|------|------|
| 總驗證項 | **75** |
| Gemini 已覆蓋 | **7**（9.3%） |
| ❓ 未提及 | **64**（85.3%） |
| 🆕 確認缺失 | **4**（5.3%） |

---

**End of Verification Matrix v1.0**
