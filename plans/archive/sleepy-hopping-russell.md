# v2.4.5 修正計劃：灰色框雙重鏡像錯誤

## 問題確認

✅ **模擬測試完成** - 已找到根本原因

用戶反饋：「灰色框一直顯示在左三分線」

## 根本原因：雙重鏡像錯誤

### 問題位置
`AnalyzeCompositionUseCase.kt` 第 817-823 行

```kotlin
if (isFrontCamera) {
    currentLeft = imageWidth - faceBoundingBox.right  // ❌ 錯誤！
    currentRight = imageWidth - faceBoundingBox.left  // ❌ 錯誤！
}
```

### 錯誤原因
ML Kit 的 `faceBoundingBox` 已經是**預覽座標系統**（前置鏡頭已鏡像），代碼又再次鏡像，導致座標完全錯誤。

### 實際影響（模擬測試結果）

| 臉部實際位置 | faceBoundingBox | 錯誤計算後的 faceCenterX | targetCenterX | 結果 |
|-------------|----------------|------------------------|---------------|------|
| 左側 (left=100) | 100-300 | 880 (右側) | 720 (右三分線) | ❌ 錯誤 |
| 右側 (left=700) | 700-900 | 280 (左側) | 360 (左三分線) | ❌ 錯誤 |

**所有位置都被反轉**，這就是為什麼用戶看到「一直在左三分線」。

## 修復方案

### 方案：移除錯誤的鏡像處理

**修改文件：** `AnalyzeCompositionUseCase.kt`

#### 修改 1：calculateGuideData() 第 817-823 行

```kotlin
// 修改前（錯誤）
if (isFrontCamera) {
    currentLeft = imageWidth - faceBoundingBox.right
    currentRight = imageWidth - faceBoundingBox.left
} else {
    currentLeft = faceBoundingBox.left.toFloat()
    currentRight = faceBoundingBox.right.toFloat()
}

// 修改後（正確）
// ML Kit faceBoundingBox 已經是預覽座標系統，不需要再次鏡像
currentLeft = faceBoundingBox.left.toFloat()
currentRight = faceBoundingBox.right.toFloat()
```

#### 修改 2：invoke() 第 134-144 行（同樣的問題）

```kotlin
// 修改前（錯誤）
if (isFrontCamera) {
    faceCenterX = imageWidth - boundingBox.centerX().toFloat()
    faceLeft = imageWidth - boundingBox.right.toFloat()
    faceRight = imageWidth - boundingBox.left.toFloat()
    Log.d(TAG, "Front camera: mirrored X coordinates...")
} else {
    faceCenterX = boundingBox.centerX().toFloat()
    faceLeft = boundingBox.left.toFloat()
    faceRight = boundingBox.right.toFloat()
}

// 修改後（正確）
// ML Kit 輸出已經匹配預覽座標系統，不需要鏡像
faceCenterX = boundingBox.centerX().toFloat()
faceLeft = boundingBox.left.toFloat()
faceRight = boundingBox.right.toFloat()
```

### 為什麼之前需要鏡像？

回顧 v2.3.1 的修改說明：
> "Fix front camera mirror coordinate issue - X coordinates now properly mirrored"

**v2.3.1 的分析是錯誤的**。ML Kit 的輸出已經匹配預覽座標系統，不需要手動鏡像。

## 修改影響

| 項目 | 評估 |
|------|------|
| 風險 | 低 - 移除錯誤邏輯 |
| 影響範圍 | 前置鏡頭構圖引導 |
| 破壞性 | 無 |
| 測試需求 | 前置+後置鏡頭驗證 |

## 驗證標準

修復後必須驗證：
- [ ] 前置鏡頭：臉在左側 → 灰框在左三分線
- [ ] 前置鏡頭：臉在右側 → 灰框在右三分線
- [ ] 後置鏡頭：正常運作（不影響）
- [ ] 眼睛對準灰框中央 → 顯示「構圖良好」
