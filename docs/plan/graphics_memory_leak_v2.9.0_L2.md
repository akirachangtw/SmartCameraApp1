# Level 2: 可行性檢視 - v2.9.0 Graphics 記憶體洩漏根因調查

## 📋 問題背景（來自 L1 / Phase 0 測試）

### 現象描述

**v2.9.0-alpha Phase 0 測試發現嚴重 Graphics 記憶體洩漏**：

| 版本 | 解析度 | Graphics @ 10min | 狀態 |
|------|--------|------------------|------|
| **v2.8.5 Baseline** | 480x480 | 225.8 MB | 🔴 嚴重洩漏（無 PreviewView） |
| **v2.8.6 Phase 2** | 480x480 | 63.8 MB | ✅ **已修復**（有 PreviewView） |
| **v2.9.0-alpha** | **360x360** | **224.3 MB** | 🔴🔴 **退化至 v2.8.5 水平！** |

**時間線詳細數據**（v2.9.0-alpha）：
```
T0 (0 min):  113.4 MB  ⚠️ 初始已偏高
T1 (2 min):  205.1 MB  🔴 暴增 +91.7 MB (+81%)
T2 (4 min):  202.4 MB  持續高位
T3 (6 min):  211.6 MB  再次上升
T5 (10 min): 224.3 MB  🔴 洩漏峰值
```

### 關鍵問題

**為什麼降低解析度（480x480 → 360x360）會導致 Graphics 記憶體洩漏？**

---

## 🎯 L2 調查目標

### 主要目標

1. **確認洩漏根因**：找出 360x360 解析度觸發洩漏的具體原因
2. **評估修復可行性**：判斷是否可修復，還是必須維持 480x480
3. **提供修復方案**：如果可修復，提供具體實作建議

### 次要目標

4. **驗證中間解析度**：測試 400x400 / 420x420 是否可避免洩漏
5. **理解 PreviewView 機制**：深入理解 Phase 2 修復的原理

---

## 🔬 假設與驗證計畫

### 假設 1: BitmapPool 尺寸不匹配 ⭐ **最可能**

**理論**：
- v2.8.4 BitmapPool 優化針對 480x480 設計
- 360x360 尺寸可能導致 Pool Key 不匹配
- 無法重用 Bitmap，導致持續分配新記憶體

**驗證方法**：
1. 檢查 `BitmapPool.kt` 的 Key 生成邏輯
2. 檢查 `LiveCameraSource.kt` 的 Bitmap 申請/釋放邏輯
3. 添加日誌記錄 Pool Hit/Miss 率

**預期結果**：
- 如果是此問題：Pool Miss 率應該很高（>50%）
- 如果不是：Pool Hit 率應該正常（>80%）

**修復方案**（如果驗證成立）：
- 調整 BitmapPool Key 生成，支援動態尺寸
- 或：為 360x360 建立專用 Pool

---

### 假設 2: PreviewView SurfaceProvider 解析度不匹配

**理論**：
- PreviewView 的 SurfaceProvider 期望特定解析度範圍
- 360x360 太小，導致 CameraX 建立額外 Surface
- 無法正確釋放 Surface，造成 Graphics 記憶體洩漏

**驗證方法**：
1. 檢查 `LiveCameraSource.kt` Line 487-505 的 Preview UseCase 配置
2. 檢查 `externalSurfaceProvider` 和 `fallbackSurfaceProvider` 行為
3. 添加 Surface 生命週期日誌（create/destroy）

**預期結果**：
- 如果是此問題：應該看到 Surface 建立但未銷毀
- 已有監測：`surfaceTextureCreateCount` vs `surfaceTextureReleaseCount`

**修復方案**（如果驗證成立）：
- 限制最小解析度為 CameraX 建議值（480x360）
- 或：修復 Surface 釋放邏輯

---

### 假設 3: RenderScript Transform 小尺寸問題

**理論**：
- RenderScript `renderScriptYuvConverter` 在 360x360 尺寸下有 Bug
- Allocation 大小計算錯誤，導致記憶體未正確回收

**驗證方法**：
1. 檢查 `RenderScriptYuvConverter.kt` 的 Allocation 管理
2. 檢查 `LiveCameraSource.kt` 的 Transform 流程
3. 測試是否與 `ENABLE_FUSED_TRANSFORM` 有關

**預期結果**：
- 如果是此問題：Native Heap 也應該增長
- 實測數據：Native Heap 穩定（79-92 MB），看起來不是此問題

**修復方案**（如果驗證成立）：
- 修復 RenderScript Allocation 尺寸計算
- 或：禁用 RenderScript，使用純 CPU YUV 轉換

---

### 假設 4: ImageAnalysis 解析度限制 ⚠️ **次要可能**

**理論**：
- CameraX ImageAnalysis 有最小解析度限制
- 360x360 低於限制，CameraX 強制上採樣
- 上採樣過程建立臨時 Buffer，未釋放

**驗證方法**：
1. 查閱 CameraX 文件確認最小解析度
2. 檢查 ImageAnalysis Builder 的警告日誌
3. 測試 480x360（CameraX 建議最小值）

**預期結果**：
- 如果是此問題：Logcat 應有 CameraX 警告
- Phase 0 測試中未見此警告

**修復方案**（如果驗證成立）：
- 使用 CameraX 建議的最小解析度（480x360）

---

## 📊 驗證計畫

### 階段 1: 程式碼靜態分析（1-2 小時）

#### Task 1.1: 檢查 BitmapPool 實作

**檔案**: `app/src/main/java/com/akirachang/ai/photographer/util/BitmapPool.kt`

**檢查項目**:
1. Pool Key 生成邏輯（Width x Height x Config）
2. `acquire()` 方法的尺寸匹配邏輯
3. `release()` 方法的回收邏輯
4. `maxEntriesPerKey` 設定是否適當

**預期發現**:
- Key 應該是 `"${width}_${height}_${config}"`
- 如果 360x360 與 480x480 Key 不同，會導致 Miss

#### Task 1.2: 檢查 LiveCameraSource Bitmap 管理

**檔案**: `app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt`

**檢查項目**:
1. Line 167: `bitmapPool` 初始化（maxEntriesPerKey = 4）
2. Line 168: `inFlightPreviewBuffers` 管理
3. Bitmap 申請位置（`ImageProxy.toBitmap(pool, converter)`）
4. Bitmap 釋放位置（`bitmapPool.release(outputBuffer)`）
5. 是否有洩漏路徑（未 release 的情況）

**預期發現**:
- 應該有配對的 acquire/release
- 檢查 Exception 處理是否會導致 leak

#### Task 1.3: 檢查 PreviewView Surface 管理

**檔案**: `app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt`

**檢查項目**:
1. Line 84-150: `fallbackSurfaceProvider` 實作
2. Line 496-500: Preview UseCase SurfaceProvider 設定
3. Line 69-71: Surface 生命週期追蹤變數
4. Surface 銷毀邏輯（onSurfaceTextureSizeChanged / onSurfaceTextureDestroyed）

**預期發現**:
- 檢查 `surfaceTextureCreateCount` vs `surfaceTextureReleaseCount`
- 驗證是否有 Surface 未釋放

---

### 階段 2: 動態測試驗證（2-3 小時）

#### Task 2.1: 添加詳細日誌

**修改檔案**: `LiveCameraSource.kt`, `BitmapPool.kt`

**添加日誌**:
```kotlin
// BitmapPool.kt - 在 acquire() 方法
Log.i(TAG, "Pool acquire: ${width}x${height}, Hit=${isHit}, PoolSize=${currentSize}")

// BitmapPool.kt - 在 release() 方法
Log.i(TAG, "Pool release: ${bitmap.width}x${bitmap.height}, Recycled=${wasRecycled}")

// LiveCameraSource.kt - 在 Bitmap 申請位置
Log.d(TAG, "Bitmap allocated: ${bitmap.width}x${bitmap.height}, " +
           "PoolHitRate=${poolHitCount}/${totalBitmapCount}")

// LiveCameraSource.kt - 在 Surface 事件
Log.i(TAG, "Surface lifecycle: Create=${createCount}, Release=${releaseCount}")
```

#### Task 2.2: 建立測試版本

**版本配置**:
- v2.9.0-beta1: 360x360 + 詳細日誌
- v2.9.0-beta2: 400x400 + 詳細日誌（中間解析度測試）
- v2.9.0-beta3: 420x420 + 詳細日誌（接近 480 測試）

**測試方法**:
- 每個版本運行 5 分鐘
- 記錄 Graphics Memory 成長趨勢
- 分析 Logcat Pool Hit/Miss 統計

#### Task 2.3: 對比測試

**對比組合**:
1. v2.8.6 (480x480, 穩定) vs v2.9.0-alpha (360x360, 洩漏)
2. v2.9.0-beta2 (400x400) 測試是否改善
3. v2.9.0-beta3 (420x420) 測試是否接近 v2.8.6

**成功標準**:
- Graphics Memory < 100 MB @ 5 分鐘
- Pool Hit Rate > 80%
- Surface Create/Release 數量平衡

---

### 階段 3: 根因確認與修復設計（1-2 小時）

#### 如果是 BitmapPool 問題

**修復方案 A**: 動態 Pool Key
```kotlin
// BitmapPool.kt
private fun getKey(width: Int, height: Int, config: Bitmap.Config): String {
    // Round to nearest standard size to improve reuse
    val roundedWidth = ((width + 15) / 16) * 16  // Round to 16px boundary
    val roundedHeight = ((height + 15) / 16) * 16
    return "${roundedWidth}_${roundedHeight}_${config}"
}
```

**修復方案 B**: 多尺寸 Pool
```kotlin
// LiveCameraSource.kt
private val bitmapPoolSmall = BitmapPool(maxEntriesPerKey = 4)  // For < 400x400
private val bitmapPoolLarge = BitmapPool(maxEntriesPerKey = 4)  // For >= 400x400
```

#### 如果是 PreviewView 問題

**修復方案**: 限制最小解析度
```kotlin
// LiveCameraSource.kt
val targetResolution = if (Build.MODEL == "Pixel 9a") {
    Size(480, 360)  // Use CameraX recommended minimum
} else {
    Size(640, 480)
}
```

#### 如果是 RenderScript 問題

**修復方案**: 條件禁用
```kotlin
// LiveCameraSource.kt
val useRenderScript = targetWidth >= 400 && targetHeight >= 400
val converter = if (useRenderScript) renderScriptYuvConverter else yuvConverter
```

---

## 🎯 L2 決策樹

### Decision Gate 1: 根因是否可確認？

```
階段 1 + 階段 2 完成後
   ↓
根因清楚？
   ├─ ✅ 是 → 進入 Decision Gate 2（評估修復可行性）
   └─ ❌ 否 → 回滾至 v2.8.6，暫緩優化
```

### Decision Gate 2: 修復是否可行？

```
根因已確認
   ↓
修復成本評估
   ├─ ✅ 低成本（< 1天） → 實作修復，進入 L3 驗證
   ├─ ⚠️ 中成本（1-2天） → 評估 ROI，決定是否修復
   └─ 🔴 高成本（> 2天） → 回滾至 v2.8.6，使用中間解析度折衷
```

### Decision Gate 3: 中間解析度是否可行？

```
如果直接修復成本過高
   ↓
測試 400x400 / 420x420
   ├─ ✅ 無洩漏 → 採用中間解析度（FPS 改善打折扣）
   └─ 🔴 仍洩漏 → 放棄降低解析度方案，回滾至 v2.8.6
```

---

## 📋 可行性評估標準

### 技術可行性

| 假設 | 驗證難度 | 修復難度 | 風險等級 |
|------|---------|---------|---------|
| BitmapPool 尺寸不匹配 | ⭐ 低 | ⭐⭐ 中 | ⚠️ 中 |
| PreviewView 解析度不匹配 | ⭐⭐ 中 | ⭐ 低 | ✅ 低 |
| RenderScript 問題 | ⭐⭐⭐ 高 | ⭐⭐⭐ 高 | 🔴 高 |
| ImageAnalysis 限制 | ⭐ 低 | ⭐ 低 | ✅ 低 |

### 時程評估

| 階段 | 預估時間 | 依賴 |
|------|---------|------|
| 階段 1: 靜態分析 | 1-2 小時 | 程式碼審查 |
| 階段 2: 動態測試 | 2-3 小時 | Pixel 9a 實機 |
| 階段 3: 修復設計 | 1-2 小時 | 階段 1+2 結果 |
| **總計** | **4-7 小時** | - |

### ROI 評估

**投資**:
- 調查時間：4-7 小時
- 修復實作：1-3 天（取決於根因）
- 測試驗證：0.5-1 天

**回報**:
- ✅ **FPS 改善**：理論上 -44% 像素處理量（如果成功）
- ⚠️ **實際 FPS 改善未知**：Phase 0 測試顯示無顯著改善
- 🔴 **風險**：可能無法修復，浪費時間

**結論**:
- 如果 1-2 天內能修復 → 值得嘗試
- 如果 > 2 天 → 回滾至 v2.8.6 更合理

---

## 🎯 L2 最終建議（預先評估）

### 情境 A: BitmapPool 問題（最可能）⭐

**如果驗證為 Pool 不匹配**:
- ✅ **可行性**: 高（修復邏輯清楚）
- ✅ **修復成本**: 低-中（1-2 天）
- ✅ **建議**: 修復並繼續 v2.9.0 開發

### 情境 B: PreviewView/CameraX 限制

**如果驗證為 CameraX 不支援 360x360**:
- ✅ **可行性**: 高（使用 480x360）
- ✅ **修復成本**: 低（0.5 天）
- ⚠️ **效果打折**：480x360 vs 360x360 改善幅度縮小
- ✅ **建議**: 使用 480x360 折衷方案

### 情境 C: RenderScript 或複雜問題

**如果驗證為深層架構問題**:
- 🔴 **可行性**: 低-中（複雜度高）
- 🔴 **修復成本**: 高（3+ 天）
- 🔴 **建議**: 放棄降低解析度，回滾至 v2.8.6

### 情境 D: 無法確認根因

**如果 L2 調查後仍不清楚**:
- 🔴 **建議**: 立即回滾至 v2.8.6
- 📝 **記錄**: 將此問題標記為已知限制
- 🔬 **未來**: 等待 CameraX 更新或重構 Transform 流程

---

## 📌 下一步行動

### 立即行動（今天）

1. ✅ **階段 1: 靜態分析**（1-2 小時）
   - 檢查 BitmapPool.kt 實作
   - 檢查 LiveCameraSource.kt Bitmap 管理
   - 檢查 Surface 生命週期追蹤

2. ✅ **初步結論**（根據靜態分析）
   - 如果發現明顯問題 → 直接修復
   - 如果不清楚 → 進入階段 2 動態測試

### 條件行動（明天，如需要）

3. ⏳ **階段 2: 動態測試**（2-3 小時，如階段 1 無法確認）
   - 添加詳細日誌
   - 建立測試版本（360/400/420 解析度）
   - Pixel 9a 實機測試

4. ⏳ **階段 3: 修復實作**（1-3 天，根據根因）
   - 實作修復方案
   - 單元測試
   - 整合測試

---

## 🔗 相關文件

- [v2.9.0 Thermal 優化 L1 計畫](./v2.9.0_thermal_optimization_L1.md)
- [v2.9.0-alpha Phase 0 測試報告](../../tmp/v2.9.0_alpha_pixel9a_phase0_test.md)
- [v2.8.6 Pixel 9a 測試報告](../../tmp/v2.8.6_pixel9a_10min_test.md)
- [v2.8.5 Baseline 測試報告](../../tmp/v2.8.5_pixel9a_baseline_fps.md)
- [BitmapPool 實作](../../app/src/main/java/com/akirachang/ai/photographer/util/BitmapPool.kt)
- [LiveCameraSource 實作](../../app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt)

---

**文件版本**: L2-v1.0
**建立時間**: 2026-01-11 17:45
**負責人**: Claude Code (Sonnet 4.5)
**狀態**: ⏳ 待執行階段 1 靜態分析

---

## ❓ 用戶決策點

準備開始 **階段 1: 靜態分析**（程式碼審查），預估 1-2 小時。

**是否立即開始？**
