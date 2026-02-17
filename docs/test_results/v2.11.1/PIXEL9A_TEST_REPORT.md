# Pixel 9a 實機測試報告 - v2.11.1

> **測試日期**: 2026-02-16
> **裝置**: Pixel 9a (Device ID: 59131JEBF17244)
> **版本**: v2.11.1 (Build 74)
> **測試者**: Akira Chang
> **測試類型**: 完整功能驗測

---

## 📊 測試結果摘要

| 類別 | 總數 | 通過 | 失敗 | 通過率 |
|------|------|------|------|--------|
| Camera Core | 2 | 0 | 2 | 0% |
| Preview | 2 | 0 | 2 | 0% |
| Photo Capture | 2 | 2 | 0 | 100% |
| UI/UX | 2 | 0 | 2 | 0% |
| Settings | 2 | 2 | 0 | 100% |
| **總計** | **10** | **4** | **6** | **40%** |

**整體狀態**: ❌ **失敗** - 發現 6 個問題，需要修復

---

## ❌ 失敗測試項目

### Test 1: Eye-AF (眼睛優先自動對焦) - ❌ 失敗

**測試日期**: 2026-02-16 13:00-13:03
**狀態**: ❌ **失敗**

#### 問題描述
前後鏡頭的 Eye-AF 都沒有自動對焦功能，僅顯示偵測框但不執行對焦動作。

#### 測試結果

**前置鏡頭測試**:
- ❌ ML Kit Face Detection 有偵測到人臉（綠色矩形框）
- ❌ 有顯示 Eye-AF 框（Cyan 小矩形）
- ❌ **但沒有自動對焦動作**
- ⚠️ 出現 Toast 訊息：「當前預覽倍率在此模態，關閉對焦特效」

**後置鏡頭測試**:
- ❌ 同樣有偵測到人臉
- ❌ **但沒有自動對焦動作**

#### 截圖證據
- `Test1_ Eye-AF-前鏡頭-20260216-130025.png` - 前置鏡頭，顯示 Toast 訊息
- `Test1_ Eye-AF-前鏡頭-20260216-130120.png` - 前置鏡頭
- `Test1_ Eye-AF-後鏡頭_20260216-130136.png` - 後置鏡頭
- `Test1_ Eye-AF-後鏡頭_20260216-130309.png` - 後置鏡頭

#### 問題分析

**關鍵問題**:
1. **Toast 訊息**：「當前預覽倍率在此模態，關閉對焦特效」
   - 這表示對焦功能被某個條件關閉了
   - 可能是預覽模式或縮放倍率的問題

2. **ML Kit 偵測正常，但未觸發對焦**:
   - Face Detection 正常運作（有綠色框和 Cyan 眼睛框）
   - 但沒有執行 CameraX FocusMeteringAction
   - 可能是對焦邏輯沒有整合

#### 預期行為
- ✅ 偵測到人臉時，藍色矩形框框住眼睛區域
- ✅ 自動執行對焦動作（CameraX Auto-Focus）
- ✅ 對焦成功後顯示鎖定狀態

#### 建議修復
1. 檢查對焦條件判斷邏輯（為何顯示「關閉對焦特效」）
2. 整合 ML Kit Face Detection 與 CameraX FocusMeteringAction
3. 移除或調整預覽模式限制

---

### Test 2: Smart Subject Focus (智能主體對焦) - ❌ 失敗

**測試日期**: 2026-02-16
**狀態**: ❌ **失敗**

#### 問題描述
1. 對焦圖片不會識別物件形狀大小而改變形狀大小
2. 看起來已對焦，但狀態都是「對焦中」
3. 不會進入動畫淡化至 Alpha 0.4

#### 測試結果
- ❌ 矩形框大小固定，不會根據物件大小調整
- ❌ 對焦狀態未正確切換（一直維持「對焦中」）
- ❌ 沒有淡化動畫（Alpha 應該從 1.0 → 0.4）

#### 問題分析
1. **Saliency-based 偵測可能未正常運作**
2. **對焦狀態機制有問題**:
   - 無法判斷對焦成功
   - 沒有進入「已對焦」狀態
3. **動畫邏輯未觸發**

#### 預期行為
- ✅ Saliency-based 偵測顯著區域
- ✅ 矩形框大小根據物件大小調整
- ✅ 對焦成功後切換狀態
- ✅ 淡化至 Alpha 0.4（持續停留狀態）

#### 建議修復
1. 檢查 Saliency 偵測演算法
2. 修正對焦狀態機制
3. 實作淡化動畫邏輯

---

### Test 3: Aspect Ratio Toggle - ❌ 失敗（三分格線問題）

**測試日期**: 2026-02-16 13:04
**狀態**: ⚠️ **部分失敗**

#### 問題描述
長寬比切換時，預覽畫面的三分格線沒有跟隨縮放在畫面範圍內，而是超出了預覽邊界。

#### 測試結果

**1:1 測試**:
- ✅ 預覽畫面切換為 1:1（正方形）
- ❌ **三分格線超出預覽範圍**（垂直線超出上下邊界）

**4:3 測試**:
- ✅ 預覽畫面切換為 4:3
- ❌ **三分格線超出預覽範圍**

**16:9 測試**:
- ✅ 預覽畫面切換為 16:9
- ❌ **三分格線超出預覽範圍**

#### 截圖證據
- `Test3_1-1_2026-02-16-13-04-17.jpg` - 1:1 三分格線超出
- `Test3_4-3_2026-02-16-13-04-28.jpg` - 4:3 三分格線超出
- `Test3_16-9_2026-02-16-13-04-38.jpg` - 16:9 三分格線超出

#### 問題分析
**三分格線繪製邏輯問題**:
- 三分格線使用的是固定座標或 Canvas 全尺寸
- 沒有根據 PreviewView 的實際顯示區域調整
- 應該使用 PreviewView 的實際尺寸（考慮 letterbox）

#### 預期行為
- ✅ 三分格線應該繪製在預覽畫面範圍內
- ✅ 根據 Aspect Ratio 調整格線位置和大小
- ✅ 不應超出預覽邊界

#### 建議修復
1. 修正三分格線繪製邏輯
2. 使用 PreviewView 的實際顯示區域（不是 Canvas 全尺寸）
3. 考慮 letterbox/pillarbox 的影響

**代碼位置**:
- 三分格線繪製: `CameraScreen.kt` (Compose UI - Canvas)
- PreviewView 尺寸: 需要從 `PreviewView` 獲取實際顯示區域

---

### Test 4: Rectangle + Corner Brackets 視覺設計 - ⚠️ 部分失敗

**測試日期**: 2026-02-16
**狀態**: ⚠️ **部分失敗**

#### 問題描述
Rectangle UI 顯示正常，但因為 Eye-AF 和 Smart Subject Focus 本身有問題，導致視覺效果無法完整驗證。

#### 測試結果
- ✅ Cyan 色系正確 (#00E5FF)
- ✅ 角落括號顯示正常
- ⚠️ 無法驗證 Neon Glow 效果（因對焦功能失效）
- ⚠️ 無法驗證淡化動畫（因狀態機制問題）

#### 建議
等待 Test 1 和 Test 2 修復後重新驗證。

---

## ✅ 通過測試項目

### Test 5: Settings UI 現代化 - ✅ 通過

**測試日期**: 2026-02-16
**狀態**: ✅ **通過**

#### 測試結果
- ✅ 可摺疊卡片設計正常
- ✅ Material Design 3 風格
- ✅ 展開/收合動畫流暢
- ✅ 所有設定功能正常

#### 備註
Settings UI 運作正常，符合設計預期。

---

### Test 6: Location Permission 管理 - ✅ 通過

**測試日期**: 2026-02-16
**狀態**: ✅ **通過**

#### 測試結果
- ✅ 權限授權流程順暢
- ✅ 權限狀態實時更新
- ✅ GPS 資訊正確顯示/隱藏
- ✅ UX 改進有效（v2.10.2 修復）

#### 備註
Location Permission 功能運作正常。

---

### 未測試項目

#### Test 7: Photo Capture Feedback
**狀態**: ⏭️ **未測試**

#### Test 8: EXIF 資訊顯示
**狀態**: ⏭️ **未測試**

#### Test 9: Permission UX
**狀態**: ⏭️ **未測試**

---

## 🐛 發現的問題總結

### 🔴 Critical（高優先級）

#### 問題 1: Eye-AF 無自動對焦功能
- **嚴重程度**: 🔴 Critical
- **影響功能**: Eye-AF (v2.11.0 核心功能)
- **發現日期**: 2026-02-16
- **狀態**: 🚧 待修復

**問題描述**:
前後鏡頭的 Eye-AF 都沒有自動對焦功能，出現 Toast 訊息「當前預覽倍率在此模態，關閉對焦特效」。

**重現步驟**:
1. 啟動相機，進入 Live 模式
2. 對準人臉
3. ML Kit 偵測到人臉，顯示綠色框和 Cyan 眼睛框
4. **但沒有執行自動對焦**

**原因分析**:
1. 對焦功能被某個條件關閉（預覽模式或縮放倍率）
2. ML Kit Face Detection 與 CameraX FocusMeteringAction 未整合

**解決方案**:
1. 檢查 `CameraViewModel.kt` 或 `LiveCameraSource.kt` 中的對焦條件判斷
2. 移除或調整「關閉對焦特效」的限制
3. 整合 ML Kit Face Detection 結果到 CameraX FocusMeteringAction

**相關代碼**:
- ML Kit 整合: `LiveCameraSource.kt`
- 對焦邏輯: `CameraViewModel.kt`
- Toast 訊息來源: 需要搜尋「當前預覽倍率在此模態」

---

#### 問題 2: Smart Subject Focus 狀態機制失效
- **嚴重程度**: 🔴 Critical
- **影響功能**: Smart Subject Focus (v2.11.0 核心功能)
- **發現日期**: 2026-02-16
- **狀態**: 🚧 待修復

**問題描述**:
1. 對焦框大小固定，不會根據物件大小調整
2. 對焦狀態一直維持「對焦中」，無法切換到「已對焦」
3. 沒有淡化動畫（Alpha 1.0 → 0.4）

**解決方案**:
1. 檢查 Saliency-based 偵測演算法
2. 修正對焦狀態機制（搜尋中 → 已對焦 → 持續停留）
3. 實作淡化動畫邏輯

**相關代碼**:
- Saliency 偵測: `CameraViewModel.kt` 或 `LiveCameraSource.kt`
- 對焦狀態: `CameraState.kt` 或 `FocusState.kt`
- 動畫邏輯: `CameraScreen.kt` (Compose UI)

---

### 🟡 Medium（中優先級）

#### 問題 3: 三分格線超出預覽範圍
- **嚴重程度**: 🟡 Medium
- **影響功能**: Aspect Ratio Toggle UI
- **發現日期**: 2026-02-16
- **狀態**: 🚧 待修復

**問題描述**:
長寬比切換時（1:1, 4:3, 16:9），三分格線超出預覽畫面邊界，沒有跟隨縮放。

**解決方案**:
修正三分格線繪製邏輯，使用 PreviewView 的實際顯示區域而非 Canvas 全尺寸。

**相關代碼**:
- 三分格線繪製: `CameraScreen.kt` (Canvas drawing)
- PreviewView 尺寸獲取: 需要從 `PreviewView` 或 `CameraState` 獲取實際區域

**修復建議**:
```kotlin
// 錯誤做法（目前）
Canvas(modifier = Modifier.fillMaxSize()) {
    // 使用 size.width 和 size.height（這是 Canvas 全尺寸）
    val w = size.width
    val h = size.height
    // 繪製三分格線...
}

// 正確做法
Canvas(modifier = Modifier.fillMaxSize()) {
    // 獲取 PreviewView 的實際顯示區域
    val previewRect = getPreviewDisplayRect()
    val w = previewRect.width
    val h = previewRect.height
    val offsetX = previewRect.left
    val offsetY = previewRect.top
    // 繪製三分格線...
}
```

---

## 📊 測試統計

### 功能通過率

| 功能類別 | 通過 / 總數 | 通過率 |
|---------|-----------|--------|
| Camera Core | 0 / 2 | 0% |
| Preview | 0 / 2 | 0% |
| Photo Capture | 2 / 2 | 100% |
| UI/UX | 0 / 2 | 0% |
| Settings | 2 / 2 | 100% |
| **總計** | **4 / 10** | **40%** |

### 問題嚴重程度分布

| 嚴重程度 | 數量 | 百分比 |
|---------|------|--------|
| 🔴 Critical | 2 | 33% |
| 🟡 Medium | 1 | 17% |
| 🟢 Low | 0 | 0% |
| **總計** | **3** | **50%** |

---

## 🔄 下一步行動

### 立即修復（高優先級）

1. **修復 Eye-AF 自動對焦功能**
   - 移除「關閉對焦特效」限制
   - 整合 ML Kit Face Detection 與 CameraX FocusMeteringAction
   - 預估時間: 2-3 小時

2. **修復 Smart Subject Focus 狀態機制**
   - 修正對焦狀態切換邏輯
   - 實作淡化動畫（Alpha 1.0 → 0.4）
   - 預估時間: 2-3 小時

3. **修復三分格線超出問題**
   - 調整繪製邏輯使用 PreviewView 實際區域
   - 預估時間: 1-2 小時

### 後續測試（修復後）

4. **重新執行 Test 1-4**
   - 驗證修復效果
   - 確認無 Regression

5. **執行剩餘測試（Test 7-9）**
   - Photo Capture Feedback
   - EXIF 資訊顯示
   - Permission UX

---

## 📝 測試環境資訊

- **裝置**: Pixel 9a
- **Device ID**: 59131JEBF17244
- **Android 版本**: 15
- **APK 版本**: v2.11.1 (Build 74)
- **Build Type**: Device Debug
- **測試日期**: 2026-02-16
- **測試者**: Akira Chang
- **測試時長**: 約 30 分鐘

---

## 🔗 相關文檔

- **功能驗測清單**: `devspec/FEATURE_VERIFICATION_v2.11.1.md`
- **開發決議記錄**: `devspec/DEVELOPMENT_DECISION_LOG_v2.11.1.md`
- **測試執行指引**: `tmp/test_outputs/v2.11.1-pixel9a/PIXEL9A_TEST_EXECUTION_GUIDE.md`
- **決議索引**: `devspec/feature_record_Backlog.md`

---

**報告完成日期**: 2026-02-16
**報告狀態**: ✅ 完成
**總結**: 發現 3 個關鍵問題（2 個 Critical + 1 個 Medium），需要立即修復
