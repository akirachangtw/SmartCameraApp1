# 預覽畫面問題記錄

**測試時間**: 2026-01-19 21:53-21:57
**測試裝置**: Pixel 9a (59131JEBF17244)
**APK 版本**: v2.9.6-phase1 (Build 52)

## 問題描述

當手機從 Portrait 旋轉到 Landscape Right 時，預覽畫面被**向右旋轉 90 度**（畫面內容方向不正確）。

## 測試環境

- Activity 方向鎖定: `android:screenOrientation="portrait"`
- 自動旋轉: 已啟用 (`accelerometer_rotation=1`)

## Log 記錄

### 初始化 Log (Portrait)
```
21:52:42.175 I LiveCameraSource: ✅ Camera bound successfully: Preview + ImageAnalysis + ImageCapture
21:52:42.175 I LiveCameraSource: Initial device rotation: 0
21:52:42.177 I LiveCameraSource: OrientationEventListener enabled
21:52:42.177 D LiveCameraSource: ImageCapture targetRotation updated: 0
21:52:42.177 I LiveCameraSource: Initial targetRotation set to: 0
21:52:42.252 I LiveCameraSource: OrientationEventListener first trigger confirmed
```

### 旋轉到 Landscape Right 後
```
21:53:34.695 D LiveCameraSource: ImageCapture targetRotation updated: 3
21:53:34.695 D LiveCameraSource: Orientation changed: 61° → rotation=3
```

### Preview Frame 狀態
```
Portrait 時:    Preview frame sent: 600x800, rotation: 90°
Landscape 時:   Preview frame sent: 800x600, rotation: 180°
```

## 分析

1. **OrientationEventListener 正常工作** - 偵測到裝置旋轉 (61° → rotation=3)
2. **targetRotation 正確更新** - ImageCapture targetRotation 從 0 更新到 3
3. **Preview rotationDegrees 變化** - 從 90° 變成 180°
4. **問題**: Preview 的 bitmap 旋轉可能沒有正確處理新的 rotationDegrees

## 可能原因

`imageProxy.imageInfo.rotationDegrees` 表示「從 sensor 座標系到目標方向需要旋轉的角度」。
當 targetRotation 更新後，rotationDegrees 會改變，但 `transformBitmap` 的旋轉邏輯可能需要調整。

## 待進一步分析

- [ ] 檢查 `transformBitmap` 函數的旋轉邏輯
- [ ] 確認 rotationDegrees 與實際需要的旋轉角度關係
- [ ] 考慮 Activity 鎖定 Portrait 對 Preview 旋轉的影響
