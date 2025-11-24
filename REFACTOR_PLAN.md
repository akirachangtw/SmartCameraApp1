# 重構計劃：使用官方 MlKitAnalyzer (v1.4.0)

## 📋 當前狀態

### Git 分支
- **當前分支**: `feature/mlkit-analyzer`
- **基於**: `master` (commit 1365db5)
- **狀態**: 已加入依賴，尚未重構代碼

### 已完成的工作
✅ 建立 `feature/mlkit-analyzer` 分支
✅ 加入 `camera-mlkit-vision` 依賴到 `libs.versions.toml`
✅ 更新 `app/build.gradle.kts` 加入 implementation
✅ 分析現有架構

### 問題診斷總結

**v1.3.3-v1.3.5 嘗試的修復**:
1. v1.3.3: 移除維度交換 → 失敗
2. v1.3.4: 加入過濾邏輯 → 失敗（使用錯誤尺寸）
3. v1.3.5: 修正過濾使用 mediaImage 尺寸 → 仍失敗

**根本原因**:
手動座標轉換邏輯過於複雜且易出錯，ML Kit 座標系統與 PreviewView 不一致

---

## 🎯 重構目標

使用 CameraX 官方的 `MlKitAnalyzer`，利用 `COORDINATE_SYSTEM_VIEW_REFERENCED` 自動處理座標轉換

### 優點
- ✅ Google 官方支援，座標保證正確
- ✅ 自動處理旋轉、縮放、offset
- ✅ 內建支援 FIT_CENTER/FILL_* 等 ScaleType
- ✅ 無需手動計算轉換公式

---

## 📁 需要修改的檔案

### 1. CameraRepositoryImpl.kt ⭐ 核心修改
**位置**: `app/src/main/java/local/mylab/smartcameraapp1/data/repository/CameraRepositoryImpl.kt`

**當前實作**:
```kotlin
override fun startImageAnalysis() {
    imageAnalyzer.setDetectionCallback { result ->
        _detectionResults.value = result
    }
    _cameraController.setImageAnalysisAnalyzer(
        ContextCompat.getMainExecutor(context),
        imageAnalyzer
    )
}
```

**需要改為**:
```kotlin
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED

override fun startImageAnalysis() {
    val mlKitAnalyzer = MlKitAnalyzer(
        listOf(faceDetector, objectDetector),
        COORDINATE_SYSTEM_VIEW_REFERENCED,
        ContextCompat.getMainExecutor(context)
    ) { result: MlKitAnalyzer.Result ->
        // 處理結果
        val faces = result.getValue(faceDetector) ?: emptyList()
        val objects = result.getValue(objectDetector) ?: emptyList()

        _detectionResults.value = DetectionResult(
            faces = faces,
            objects = objects,
            imageWidth = 0, // 不再需要！
            imageHeight = 0,
            rotationDegrees = 0,
            timestamp = System.currentTimeMillis()
        )
    }

    _cameraController.setImageAnalysisAnalyzer(
        ContextCompat.getMainExecutor(context),
        mlKitAnalyzer
    )
}
```

**關鍵改變**:
- 移除 `ImageAnalyzer` 依賴
- 直接使用 `MlKitAnalyzer`
- 使用 `COORDINATE_SYSTEM_VIEW_REFERENCED` 參數
- 座標自動轉換到 View 空間

---

### 2. DetectionOverlay.kt ⭐ 簡化
**位置**: `app/src/main/java/local/mylab/smartcameraapp1/presentation/camera/components/overlay/DetectionOverlay.kt`

**需要移除**:
- ❌ `transformRect()` 函式（整個函式刪除）
- ❌ `imageSize` 參數（不再需要）

**修改前**:
```kotlin
fun DetectionOverlay(
    detectionResult: DetectionResult?,
    previewSize: Size,
    imageSize: Size,  // ❌ 移除
    modifier: Modifier = Modifier
)
```

**修改後**:
```kotlin
fun DetectionOverlay(
    detectionResult: DetectionResult?,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        detectionResult?.let { result ->
            result.faces.forEach { face ->
                val box = face.boundingBox.toComposeRect()
                // 直接使用座標，無需轉換！
                drawFaceBoundingBox(box, face.trackingId, face)
            }

            result.objects.forEach { obj ->
                val box = obj.boundingBox.toComposeRect()
                // 直接使用座標，無需轉換！
                drawObjectBoundingBox(box, obj.trackingId, obj)
            }
        }
    }
}

// 新增簡單轉換函式
private fun android.graphics.Rect.toComposeRect(): Rect {
    return Rect(
        left = left.toFloat(),
        top = top.toFloat(),
        right = right.toFloat(),
        bottom = bottom.toFloat()
    )
}
```

**關鍵改變**:
- 移除所有座標轉換邏輯
- 直接使用 ML Kit 返回的座標（已在 View 空間）
- 大幅簡化代碼

---

### 3. DetectionResult.kt
**位置**: `app/src/main/java/local/mylab/smartcameraapp1/domain/model/DetectionResult.kt`

**修改**:
```kotlin
data class DetectionResult(
    val faces: List<Face> = emptyList(),
    val objects: List<DetectedObject> = emptyList(),
    // 以下欄位可能不再需要，但保留以兼容
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val rotationDegrees: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
```

---

### 4. MLKitDetector.kt
**選項 A**: 保留過濾邏輯（推薦）
- 保留 `filterFaces()` 和 `filterObjects()`
- 修改為直接返回 Detector 而非偵測結果

**選項 B**: 完全移除（簡化）
- 刪除整個類別
- 在 CameraRepositoryImpl 直接建立 detector

**推薦選項 A**，因為過濾邏輯仍然有用（移除低信心度偵測）

---

### 5. ImageAnalyzer.kt
**狀態**: 可能不再需要

**選項**:
- 保留但不使用（以備後用）
- 完全刪除

推薦先保留，確認新實作穩定後再刪除

---

### 6. CameraScreen.kt / CameraViewModel.kt
**需要檢查**: 確認 `DetectionOverlay` 的調用方式

**修改前**:
```kotlin
DetectionOverlay(
    detectionResult = state.detectionResult,
    previewSize = previewSize,
    imageSize = imageSize  // ❌ 移除
)
```

**修改後**:
```kotlin
DetectionOverlay(
    detectionResult = state.detectionResult
)
```

---

### 7. build.gradle.kts
**更新版本**:
```kotlin
versionCode = 11
versionName = "1.4.0"
```

---

## 🔧 實作步驟（建議順序）

### 步驟 1: 修改 CameraRepositoryImpl
1. 加入 import
2. 建立 faceDetector 和 objectDetector 實例
3. 重寫 `startImageAnalysis()`
4. 測試能否正確接收偵測結果

### 步驟 2: 簡化 DetectionOverlay
1. 移除 `imageSize` 參數
2. 刪除 `transformRect()` 函式
3. 直接使用座標繪製

### 步驟 3: 更新調用方
1. 修改 CameraScreen.kt
2. 移除 imageSize 相關邏輯

### 步驟 4: 測試驗證
1. 建置 APK
2. 測試直向/橫向模式
3. 驗證框位置正確
4. 檢查無 OUT OF BOUNDS 警告

### 步驟 5: 清理代碼
1. 考慮移除 ImageAnalyzer
2. 更新文檔
3. 提交到 feature 分支

### 步驟 6: 合併回 master
1. 充分測試後
2. 更新 CHANGELOG.md
3. 合併分支

---

## ⚠️ 潛在問題與解決方案

### 問題 1: Detector 初始化位置
**問題**: faceDetector 和 objectDetector 現在在哪裡建立？

**解決方案**:
- 選項 A: 在 CameraRepositoryImpl 中直接建立（推薦）
- 選項 B: 注入 MLKitDetector 並使用其 detector 實例

### 問題 2: 過濾邏輯如何處理
**問題**: MlKitAnalyzer 回調中如何應用過濾？

**解決方案**:
```kotlin
val filteredFaces = faces.filter { face ->
    val box = face.boundingBox
    // 套用過濾邏輯
    box.left >= 0 && box.top >= 0 &&
    box.width() >= MIN_SIZE &&
    box.height() >= MIN_SIZE
}
```

### 問題 3: PreviewView ScaleType
**問題**: 需要確保 PreviewView 使用 FIT_CENTER？

**解決方案**:
檢查 CameraPreview.kt，確認:
```kotlin
AndroidView(
    factory = { context ->
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FIT_CENTER
        }
    }
)
```

---

## 📚 參考資源

### 官方文檔
- [MlKitAnalyzer 文檔](https://developer.android.com/reference/androidx/camera/mlkit/vision/MlKitAnalyzer)
- [COORDINATE_SYSTEM_VIEW_REFERENCED](https://developer.android.com/reference/androidx/camera/view/CameraController#COORDINATE_SYSTEM_VIEW_REFERENCED())

### 相關 Issue
- [camera-mlkit-vision landscape bug](https://issuetracker.google.com/issues/201425933) - 已在 1.4.0 修復

### 程式碼範例
```kotlin
// 完整範例
val mlKitAnalyzer = MlKitAnalyzer(
    listOf(faceDetector, objectDetector),
    COORDINATE_SYSTEM_VIEW_REFERENCED,
    executor
) { result ->
    result.getValue(faceDetector)?.let { faces ->
        // 處理人臉
    }
    result.getValue(objectDetector)?.let { objects ->
        // 處理物件
    }
}
```

---

## 🧪 測試計劃

### 測試場景
1. ✅ 直向模式 + 人臉偵測
2. ✅ 直向模式 + 物件偵測
3. ✅ 橫向模式 + 人臉偵測
4. ✅ 橫向模式 + 物件偵測
5. ✅ 前置/後置鏡頭切換
6. ✅ HD 影片場景（之前失敗的場景）

### 驗證標準
- ✅ 框正確對齊目標
- ✅ 偵測計數 = 可見框數量
- ✅ 無 OUT OF BOUNDS 警告
- ✅ 直向/橫向模式均正常

---

## 📝 CHANGELOG 預覽

```markdown
## [1.4.0] - 2025-11-23

### Changed
- **MAJOR: Migrated to Official MlKitAnalyzer**
  - Replaced custom ImageAnalyzer with CameraX official `MlKitAnalyzer`
  - Uses `COORDINATE_SYSTEM_VIEW_REFERENCED` for automatic coordinate transformation
  - Removed all manual coordinate transformation logic
  - Simplified DetectionOverlay - no longer needs imageSize or rotation handling

### Fixed
- **Complete fix for bounding box coordinate issues**
  - Coordinates now guaranteed correct by Google's official implementation
  - No more manual calculation errors
  - Works correctly in all orientations (portrait/landscape)

### Removed
- Custom `transformRect()` function in DetectionOverlay
- imageSize parameter from DetectionOverlay
- Manual rotation and scaling calculations

### Dependencies
- Added `androidx.camera:camera-mlkit-vision:1.4.1`

### Version Bump
- versionCode: 10 → 11
- versionName: 1.3.5 → 1.4.0
```

---

## 🚀 下一步行動（新對話）

1. **繼續重構**: 按照上述步驟實作
2. **測試驗證**: 確認所有場景正常
3. **文檔更新**: 更新 CHANGELOG 和代碼注釋
4. **合併分支**: 測試通過後合併到 master

---

**建立日期**: 2025-11-23
**當前分支**: feature/mlkit-analyzer
**Token 使用**: 120K/200K (60%)
**建議**: 新對話繼續重構，有充足 token 進行測試與調整
