# Issue: 前置鏡頭 ML Kit 追蹤座標鏡像偏移

## 記錄日期: 2026-02-03
## 優先度: MEDIUM — 僅影響前置鏡頭 + ML Kit 追蹤路徑

---

## 問題現象

使用前置鏡頭（自拍）追蹤人臉時：
- 點擊人臉 → 對焦圓圈先出現在點擊位置（A點，正確）
- 下一帧 `updateTrackedObjectPosition()` 觸發 → 圓圈移動到 B點（X 方向偏移）
- B點的 X 座標疑似為 A點的水平鏡像位置

---

## 根因分析（推測，待 logcat 驗證）

### CameraX 前置鏡頭的鏡像行為

CameraX `PreviewView` 對前置鏡頭自動做**水平鏡像**，讓用戶看到「照鏡子」效果。
但 ML Kit 偵測結果（`Face.boundingBox`）是在**未鏡像**的圖像座標裡。

### 涉及代碼路徑

1. **`findTappedTarget(x, y)`** (`CameraViewModel.kt`)
   - `x, y` = 屏幕座標（已鏡像）
   - `face.boundingBox` = ML Kit 座標（未鏡像）
   - `boundingBox.contains(x, y)` 在前置鏡頭時可能匹配不正確

2. **`updateTrackedObjectPosition()`** (`CameraViewModel.kt`)
   - `centerX = targetBoundingBox.exactCenterX()` — ML Kit 座標（未鏡像）
   - 直接設為 `focusPoint` → 顯示在錯誤的 X 位置

### 修正思路

在前置鏡頭時，將 ML Kit 的 boundingBox X 座標做鏡像映射：
```
mirroredX = detectionImageWidth - mlkitX
```
具體實作需要確認：
- `detectionResult.imageWidth` 是旋轉前還是旋轉後的寬度
- ML Kit boundingBox 是旋轉前還是旋轉後的座標系
- 需要帶 front camera logcat 驗證確認映射關係

---

## 驗證步驟

1. 切換到前置鏡頭
2. 清除 logcat，開啟: `adb logcat -s "CameraViewModel:D SmartCameraAnalyzer:D"`
3. 等待偵測到人臉（logcat 出現 Face detection）
4. 點擊人臉，觀察圓圈位置偏移方向
5. 從 logcat 提取:
   - `[Coordinate Debug] Tap: (x, y)` — 點擊座標
   - `[Coordinate Debug] Face[0] BoundingBox: Rect(...)` — ML Kit detectbox
   - `Tracking update: position=(centerX, centerY)` — 更新後的位置
6. 確認 centerX 是否為 tap x 的鏡像 (`imageWidth - x`)

---

## 參考

- `CameraViewModel.kt` line ~532: `findTappedTarget()`
- `CameraViewModel.kt` line ~581: `updateTrackedObjectPosition()`
- `CameraViewModel.kt` line ~1137: `isFrontCamera` 判斷（已用於 analyzeComposition）
