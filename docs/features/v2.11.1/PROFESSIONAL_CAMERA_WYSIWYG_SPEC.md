# Professional Camera WYSIWYG Spec — v2.11.1

**Feature**: Professional Camera — Preview/Capture Consistency
**Version**: v2.11.1
**Date**: 2026-02-17
**Status**: Implemented (Traycer Plan A)

---

## 1. 設計目標 (Design Goal)

確保使用者在預覽畫面所見的構圖，與最終拍攝輸出的照片**完全一致（WYSIWYG）**。

> **WYSIWYG**: What You See Is What You Get
> 預覽畫面比例 = 輸出照片比例（構圖一致）

---

## 2. 各長寬比行為規範

### 4:3 模式
| 項目 | 規格 |
|------|------|
| ResolutionSelector | `AspectRatioStrategy(RATIO_4_3)` |
| ViewPort | `Rational(4, 3)` + `FIT` |
| PreviewView 顯示 | 4:3（少量黑邊） |
| ImageCapture 輸出 | 4:3 |
| WYSIWYG | ✅ 完全一致 |

### 16:9 模式
| 項目 | 規格 |
|------|------|
| ResolutionSelector | `AspectRatioStrategy(RATIO_16_9)` |
| ViewPort | `Rational(16, 9)` + `FIT` |
| PreviewView 顯示 | 16:9（少量黑邊） |
| ImageCapture 輸出 | 16:9 |
| WYSIWYG | ✅ 完全一致 |

### 1:1 模式（v2.11.1 Traycer Plan A）
| 項目 | 規格 |
|------|------|
| ResolutionSelector | `ResolutionFilter`：優先真正方形（`width==height`，最接近 1920×1920）<br>若無則 fallback 到最接近 1:1 的解析度 |
| ViewPort | `Rational(1, 1)` + `FIT` |
| PreviewView 顯示 | **1:1 正方形**（ViewPort 裁切至正方形） |
| ImageCapture 輸出 | **1:1 正方形**（與 Preview 完全一致） |
| WYSIWYG | ✅ 完全一致（真正的 1:1 WYSIWYG） |

---

## 3. 1:1 模式技術實作

### 方案選擇依據

**參考**: [`docs/features/v2.10.0/v2.10.0_aspect-ratio-wysiwyg-fix_Traycer.md`](../../v2.10.0/v2.10.0_aspect-ratio-wysiwyg-fix_Traycer.md)

採用 **Traycer Plan A**：ResolutionFilter 優先尋找設備支援的真正方形解析度，搭配 ViewPort `Rational(1,1)` 確保 Preview 與 Capture WYSIWYG。

**排除方案（不可行）**：
- ❌ 4:3 Sensor + ViewPort Rational(4,3) + 軟體裁切至 1:1
  - 問題：Preview 顯示 4:3 而非 1:1 → WYSIWYG 破壞
  - 問題：軟體裁切增加複雜度

### ResolutionFilter 邏輯

```kotlin
// LiveCameraSource.kt - 1:1 mode
ResolutionSelector.Builder()
    .setResolutionFilter { supportedSizes, _ ->
        // Step 1: 尋找真正的正方形解析度
        val squareSizes = supportedSizes.filter { it.width == it.height }
        if (squareSizes.isNotEmpty()) {
            // 選擇最接近 1920×1920 的正方形
            val selected = squareSizes.minByOrNull {
                kotlin.math.abs(it.width - 1920)
            } ?: squareSizes.first()
            listOf(selected)  // e.g. 1920×1920
        } else {
            // Fallback: 選最接近 1:1 的解析度（多數設備無真正方形）
            val fallback = supportedSizes.minByOrNull { size ->
                kotlin.math.abs(size.width.toFloat() / size.height - 1.0f)
            } ?: supportedSizes.first()
            listOf(fallback)
        }
    }
    .build()
```

### ViewPort 配置

```kotlin
// 1:1 mode: ViewPort Rational(1,1) + FIT
// CameraX 對輸入的解析度（正方形或接近正方形）應用 1:1 裁切
val viewPort = ViewPort.Builder(
    currentAspectRatioMode.toRational(),  // Rational(1, 1) for 1:1 mode
    currentRotation
).setScaleType(ViewPort.FIT).build()
```

### 設備支援情況

| 設備類型 | 正方形解析度支援 | 預期行為 |
|---------|--------------|---------|
| 多數 Android 設備 | ❌ 無真正方形 | Fallback → 選最接近 1:1 的解析度（通常 4:3），ViewPort 裁切為正方形 |
| 部分前鏡頭設備 | ✅ 有正方形 | 直接使用真正方形（如 1920×1920），完美 WYSIWYG |

---

## 4. PreviewViewportContainer 設計

1:1 模式的 `contentBounds` 計算仍使用 `Rational(1,1)`，確保：
- Grid 線繪製在 1:1 正方形範圍內（與預覽一致）
- Tap-to-Focus 邊界過濾在 1:1 範圍內

```kotlin
// PreviewViewportContainer.kt
// 1:1 mode uses Rational(1,1) → contentBounds is a centered square
val rational = targetAspectRatio.toRational()  // Rational(1, 1) for 1:1
```

---

## 5. 方案對比

| 方案 | 描述 | 可行性 | 選擇 |
|------|------|--------|------|
| **Traycer Plan A** | ResolutionFilter 正方形優先 + ViewPort Rational(1,1) | ✅ 可行 | **已採用** |
| Gemini 方案 | 4:3 Sensor + ViewPort Rational(4,3) + 軟體裁切 | ❌ 不可行（WYSIWYG 破壞） | 排除 |

---

## 6. 已知限制

1. **多數設備無真正方形解析度**
   - Fallback 邏輯：選最接近 1:1 的 4:3 解析度，ViewPort Rational(1,1) 裁切
   - Preview 顯示 1:1 正方形（有上下黑邊），ImageCapture 輸出 1:1 正方形
   - WYSIWYG 維持 ✅

2. **Letterbox 行為**
   - 1:1 模式在 Portrait 螢幕上有顯著上下黑邊（螢幕為 ~9:16，內容為 1:1）
   - 此為預期行為（ViewPort FIT 不強制填滿螢幕）

---

*Document created: 2026-02-17*
*Based on: docs/features/v2.10.0/v2.10.0_aspect-ratio-wysiwyg-fix_Traycer.md*
