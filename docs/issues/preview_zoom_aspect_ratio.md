# Issue: PreviewView 預覽畫面縱橫比導致放大 ~1.6x

## 記錄日期: 2026-02-03
## 優先度: MEDIUM — 視覺影響明顯，但不影響核心功能

---

## 問題現象

APP 開啟後預覽畫面為滿屏，但相比 Sony / Pixel 9a 內建相機的預覽，APP 的 FOV（視角）明顯偏小，畫面看起來像放大了約 **1.4–1.6x**。

---

## 根因分析

### 數據

| 項目 | 數值 | 來源 |
|------|------|------|
| 屏幕解析度 | 1080 × 2272 (9:19.03) | Pixel 9a |
| 實際 image 輸出 | 960 × 1280 (3:4) | Logcat: downsample=480x640, scale=0.5 |
| Raw ImageProxy (landscape) | 1280 × 960 (4:3) | LiveCameraSource rotation log |
| PreviewView ScaleType | FILL_CENTER | CameraPreview.kt:107 |
| ResolutionSelector 目標 | 1280 × 720 (16:9) | selectDeviceOptimalResolution() Pixel branch |
| CameraX 實際選中 | **1280 × 960 (4:3)** | Logcat: `Preview resolution mismatch! Requested=1280x960, Expected=1280x720` |

### Pixel 9a 支持的 YUV_420_888 解析度 (29 total)

```
176x144, 320x240, 352x288,
640x360, 640x480, 720x480,
800x600, 1024x576, 1024x768,
1280x720, 1080x1080, 1280x960,
1344x1008, 1440x1080, 1792x1008,
1920x960, 1600x1200, 1920x1080,
1920x1440, 2048x1536, 2560x1280,
1920x1920, 2688x1512, 2560x1920,
3264x1836, 3264x2448, 4000x2000,
3840x2160, 4000x3000
```

### 數學推導：zoom 倍數

FILL_CENTER (= CENTER_CROP) 的邏輯：uniformScale = max(previewW/imageW, previewH/imageH)

**當前（960×1280 portrait image）：**
```
uniformScale = max(1080/960, 2272/1280) = max(1.125, 1.775) = 1.775
可見 X 範圍 = previewW / uniformScale = 1080 / 1.775 = 608.5 px (image space)
image 實際寬 = 960 px
水平裁切率 = 1 - 608.5/960 = 36.6%  ← 兩側各裁掉 18.3%
等效放大倍數 = 960 / 608.5 = 1.577x  ← 這就是用戶觀察到的 1.4–1.6x
```

**若能拿到 1080×1920 portrait (= 1920×1080 landscape rotated)：**
```
uniformScale = max(1080/1080, 2272/1920) = max(1.0, 1.183) = 1.183
可見 X 範圍 = 1080 / 1.183 = 912.9 px ≈ image 寬 1080 px → 幾乎無裁切
等效放大倍數 = 1.183x  ← 幾乎無視覺放大
```

### 為什麼 CameraX 沒有選 1280×720？

ResolutionSelector 同時套用於 **Preview** 和 **ImageAnalysis** 兩個 UseCase。
CameraX 內部會為兩個 UseCase 尋找**共同可用**的解析度，並優先選擇 ImageAnalysis 可支持的更高解析度。
1280×720 雖然在 supported list 裡，但 CameraX 的 negotiation 最終選了 1280×960 作為兩個 UseCase 的共同格式。

Logcat 證據：
```
[v2.9.0-beta5] Smart Resolution: target=1280x720, filter≤1920x1080
[Fallback] SurfaceProvider requested: 1280x960
[v2.9.0-beta4] Preview resolution mismatch! Requested=1280x960, Expected=1280x720
```

---

## 候選修正方案

### 方案 A — 將 Preview 改為獨立的 ResolutionSelector（推薦）

對 Preview UseCase 單獨設置一個 AspectRatio=16:9 的 ResolutionSelector，讓 CameraX 為 Preview 單獨選擇合適的 16:9 解析度。ImageAnalysis 保持原有配置不變。

```
Preview  → ResolutionSelector(AspectRatio.RATIO_16_9)  → 可能選 1920×1080 或 1280×720
ImageAnalysis → 當前的 ResolutionSelector (target=1280x720, filter≤1920x1080)
```

**影響**:
- Preview 和 ImageAnalysis 的解析度可能不同（CameraX 允許）
- CoordinateMapper 裡 previewWidth/previewHeight 是屏幕尺寸，不受影響
- ImageAnalysis bitmap 尺寸不變（960×1280 或類似）→ optical flow 座標映射不變

### 方案 B — 強制 Preview 目標為 1920×1080

直接將 Preview 的 target 設為 Size(1920, 1080)，利用已有的 filter≤1920x1080 範圍。

```
Preview  → ResolutionSelector(target=1920x1080)
ImageAnalysis → 當前配置 (不變)
```

**優點**: 簡單直接
**風險**: 若 Preview 和 ImageAnalysis 必須共用解析度，可能仍被 CameraX 覆蓋

### 方案 C — 用 AspectRatioStrategy 替代 ResolutionStrategy

使用 CameraX 1.3+ 的 `AspectRatioStrategy` API，指定 RATIO_16_9：

```kotlin
ResolutionSelector.Builder()
    .setAspectRatioStrategy(
        AspectRatioStrategy(AspectRatio.RATIO_16_9, AspectRatioStrategy.FALLBACK_RULE_AUTO)
    )
    .build()
```

此方案會同時影響 Preview + ImageAnalysis 的解析度（兩者共用 selector）。
若能拿到 1920×1080 (16:9)，optical flow 的 bitmap 也會變為 1080×1920，需要更新 OpticalFlowTracker 的 downsample 邏輯（當前動態計算，應自動適配）。

---

## 對其他模組的影響評估

| 模組 | 影響 | 說明 |
|------|------|------|
| CoordinateMapper | 無影響 | 使用屏幕尺寸 + image 實際尺寸動態計算 |
| OpticalFlowTracker | 低影響 | downsample 已動態 aspect-ratio-preserving，會自動適配新尺寸 |
| ML Kit (Face/Object) | 無影響 | 使用 InputImage，獨立處理 |
| LiveCameraSource bitmap | 低影響 | 旋轉邏輯已動態，新 bitmap 尺寸會自動反映 |
| 截圖/拍照 | 無影響 | ImageCapture UseCase 獨立配置 |

---

## 驗證方法

修正後確認：
1. Logcat 中 `SurfaceProvider requested` 的尺寸為 16:9 之比（如 1920×1080 或 1280×720）
2. 視覺比較：與 Pixel 9a 內建相機並排對比 FOV
3. 光學流動追蹤仍正常運作（quality ≥ 0.8，座標移動正確）
4. Round-trip 座標精度仍為零誤差

---

## 參考

- `app/src/main/java/.../data/source/camera/LiveCameraSource.kt:1428` — `selectDeviceOptimalResolution()`
- `app/src/main/java/.../presentation/camera/components/CameraPreview.kt:107` — ScaleType = FILL_CENTER
- `app/src/main/java/.../util/CoordinateMapper.kt` — CENTER_CROP 座標映射
- Pixel 9a device ID: `59131JEBF17244`
