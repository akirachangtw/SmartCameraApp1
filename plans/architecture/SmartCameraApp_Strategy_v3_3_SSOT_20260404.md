# SmartCameraApp 策略與架構 SSOT v3.3

**文件版本**: 3.3 (Converged SSOT)  
**建立日期**: 2026-04-04  
**文件性質**: 唯一真理文件 — 本文件作廢所有前版（v3.0, v3.1, v3.2）  
**作者**: CSO 策略分析 (Claude Opus 4.6) + 開發者 Akira 沙盤推演  
**合併來源**: v3.2 SSOT (20260402) + Delta v3.2→v3.3 (20260403)

---

## 第一部分：產品定位

### 1.1 一句話定位

> **它不只告訴你怎麼拍對，它還懂得什麼時候不該干預。**

### 1.2 定位拆解

| 能力 | 描述 |
|------|------|
| 角度引導 | 即時告訴你「此刻此景」的最佳拍攝角度 |
| 構圖診斷 | 告訴你主體位置、頭部空間、視線方向哪裡可以更好 |
| 減法留白偵測 | 辨識出你在刻意留白，閉嘴不干預 |
| 鏡頭髒汙偵測 | 在你拍之前就守護影像品質 |
| 遞手機模式 | 讓不會拍照的朋友也能幫你拍出好照片 |
| 拍後景深效果 | 自動偵測雜亂背景，拍後 AI 虛化突顯主體 |

### 1.3 競爭定位

| 競品 | 定位差異 |
|------|---------|
| Google Pixel Camera Coach | 雲端依賴、Pixel 獨佔。我們：離線、全 Android |
| WayShot | iOS 獨佔、$69.99/年、語音指導人像。我們：Android、$2.99/月、視覺引導美食+日常 |
| ProCam / Halide | 專業相機替代品。我們：不是相機，是導拍引擎（疊加在拍照流程上的決策層）|

---

## 第二部分：Ambient UI 統一視覺語法

### 2.1 核心原則

**不告訴用戶「你錯了」，而是讓畫面的視覺環境自然牽引用戶做出正確動作。**

類比：道路上的車道標線（你不需要「閱讀」標線，身體自然知道往哪裡開）。

### 2.2 雙通道引導體系

| 通道 | 觸發 | 感測器 | 刷新率 | 視覺表現 |
|------|------|--------|--------|---------|
| **邊緣暖光 (Edge Warmth)** | 手機旋轉/傾斜 | IMU (gyroscope) | 60fps | 畫面邊緣漸層暖色光暈，指向修正方向 |
| **Halo 牽引 (Focus Halo)** | 手機平移/取景 | ML Kit (bbox) | 10fps + EMA 平滑 | 主體周圍柔和光環，偏離目標時邊緣不均勻 |

**雙通道可同時運作**：邊緣暖光操作在螢幕邊緣，Halo 在畫面中央。視覺區域不衝突。

### 2.3 邊緣暖光 (Edge Warmth) 規格

```kotlin
// presentation/camera/overlay/EdgeWarmthLayer.kt

private fun DrawScope.drawEdgeWarmth(
    direction: Direction?,
    intensity: Float  // 0.0 = 完美, 1.0 = 偏差最大
) {
    val alpha = intensity * 0.20f  // 最大 20% 透明度
    val warmColor = Color(0xFFD4A030)  // 琥珀金

    when (direction) {
        Direction.UP -> {
            // 需要抬高 → 上邊緣暖光
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(warmColor.copy(alpha = alpha), Color.Transparent),
                    startY = 0f, endY = size.height * 0.15f
                ), size = size
            )
        }
        Direction.LEFT -> { /* 水平漸層，左邊亮 */ }
        Direction.RIGHT -> { /* 水平漸層，右邊亮 */ }
        Direction.DOWN -> { /* 下邊緣暖光 */ }
        null -> {
            // 所有方向滿足 → 四邊均勻光環 → 呼吸韻律
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, warmColor.copy(alpha = alpha * 0.5f)),
                    center = center, radius = size.maxDimension * 0.6f
                ), size = size
            )
        }
    }
}
```

**場景映射**：

| 場景 | Edge Warmth 引導內容 |
|------|---------------------|
| Food 45° | Pitch 向 45° 牽引 + Roll 水平修正 |
| Food 90° | Pitch 向 90° 牽引 + 雙軸水平（Roll + Pitch 微調）|
| Portrait | Roll 水平修正 |
| Pass-to-Friend | Pitch + Roll 簡化版 |
| Minimalist | Roll 水平修正（不做角度引導）|
| General | Roll 水平修正 |

### 2.4 Halo 牽引 (Focus Halo) 規格

**v3.3 更新：所有 Halo 統一為圓形，與構圖引導共用同一 `HaloGuidanceLayer` 組件。**

```kotlin
// presentation/camera/overlay/HaloGuidanceLayer.kt

@Composable
fun HaloGuidanceLayer(
    subjectBbox: Rect?,       // ML Kit face 或 object bbox
    targetPosition: Offset?,  // 目標三分交叉點
    isAligned: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (subjectBbox == null) return@Canvas

        val haloCx = subjectBbox.center.x
        val haloCy = subjectBbox.center.y
        val haloRadius = max(subjectBbox.width, subjectBbox.height) * 0.6f

        // 計算偏置方向（向目標位置偏移亮度）
        val dx = (targetPosition?.x ?: haloCx) - haloCx
        val dy = (targetPosition?.y ?: haloCy) - haloCy
        val angle = atan2(dy, dx)

        val color = if (isAligned) Color(0xFF50C864)  // 綠色
                    else Color(0xFFD4A030)              // 金色

        // 光環：偏離時邊緣不均勻（偏側更亮）
        drawCircle(
            color = color.copy(alpha = if (isAligned) 0.35f else 0.25f),
            radius = haloRadius,
            center = Offset(haloCx, haloCy),
            style = Stroke(width = 2f)
        )

        if (isAligned) {
            // 對齊 → 均勻圓形 + 呼吸韻律（由外層動畫控制 alpha）
        } else {
            // 不對齊 → 向目標側加亮
            val brightOffset = Offset(
                haloCx - cos(angle) * 3f,
                haloCy - sin(angle) * 3f
            )
            // 繪製偏亮側的加強弧
        }
    }
}
```

**統一 Halo 語言表（v3.3 新增）**：

| 元素 | 形狀 | 顏色邏輯 | 觸發 |
|------|------|---------|------|
| 手動對焦 | 圓形 Halo | 金色→綠色 | 用戶觸摸螢幕 |
| 構圖引導 | 圓形 Halo（跟隨 bbox） | 金色→綠色 | ML Kit 偵測到主體 |
| 遞手機模式 | 圓形 Halo（跟隨人臉） | 金色→綠色 | Pass-to-Friend 模式 |
| 減法留白確認 | 微小圓形 Halo | 綠色 | MinimalistRule 通過 |

**技術可行性**：CameraX 對焦 API (`CameraControl.startFocusAndMetering()`) 只需座標點，視覺形狀完全由 App UI 層自行繪製。改圓形零技術障礙。

**場景映射**：

| 場景 | Halo 引導內容 |
|------|-------------|
| Food（有 ML Kit Object Detection bbox）| 食物 Halo → 三分法對齊 |
| Portrait（有 Face Detection bbox）| 人臉 Halo → 三分法 + 頭部空間 + 視線空間 |
| Pass-to-Friend | 人臉 Halo + 中心引力場 |
| Minimalist（小主體）| 微小確認 Halo（綠色，不說「靠近」）|
| Food（無 bbox）| 無 Halo，僅用 Edge Warmth + 網格 |
| General | 無 Halo，僅網格 |

### 2.5 呼吸韻律 (Breathing Rhythm)

當所有可改善項目都通過時（所有 `canImprove == false`），整個畫面進入均勻呼吸：

```kotlin
val breatheAlpha by animateFloatAsState(
    targetValue = if (allConditionsMet) 0.18f else 0.10f,
    animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
    )
)
```

- 邊緣光環：從「有方向的暖光」變成「四邊均勻呼吸」
- Halo：從金色變綠色，進入呼吸
- 快門按鈕外環：同步呼吸
- 含義：「一切就緒，隨時可以按快門」

### 2.6 首次使用教學動畫

3 秒連續動畫循環一次（不是文字教學頁）：
1. 手機圖示出現 → 邊緣暖光亮起（方向性）
2. 手機跟著暖光方向旋轉 → 暖光蔓延至四邊
3. 四邊均勻呼吸 → 快門按鈕呼吸
4. 「Ready to shoot!」閃爍消失

開發成本：0.5 天。Compose 動畫序列。

### 2.7 雜亂背景區域 UI（v3.3 新增）

**圖示規格**: 黃色三角形 + 黑色驚嘆號 + Alpha 0.4  
**框線**: 虛線 (dashed) + Alpha 0.4  
**圖示選擇理由**: 三角形警告符號是全球通用的「注意」標誌，在低透明度下仍可被周邊視覺辨識

```kotlin
// Compose Canvas 實作
@Composable
fun ClutterWarningIcon(center: Offset, modifier: Modifier) {
    Canvas(modifier) {
        val iconSize = 14.dp.toPx()
        val path = Path().apply {
            moveTo(center.x, center.y - iconSize * 0.5f)
            lineTo(center.x + iconSize * 0.5f, center.y + iconSize * 0.4f)
            lineTo(center.x - iconSize * 0.5f, center.y + iconSize * 0.4f)
            close()
        }
        drawPath(path, color = Color(0xFFFFD040).copy(alpha = 0.4f))
        // 黑色驚嘆號
        drawLine(
            color = Color.Black.copy(alpha = 0.4f),
            start = Offset(center.x, center.y - iconSize * 0.15f),
            end = Offset(center.x, center.y + iconSize * 0.15f),
            strokeWidth = 2f, cap = StrokeCap.Round
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.4f),
            radius = 1.5f,
            center = Offset(center.x, center.y + iconSize * 0.28f)
        )
    }
}
```

---

## 第三部分：場景判斷引擎

### 3.1 場景切換邏輯

```
if user_selected == PASS_TO_FRIEND:
    → PassToFriendMode (手動觸發，最高優先)
elif pitch > 35°:
    → FoodMode (IMU 自動觸發)
    → 啟用 ML Kit Object Detection (FOOD category)
elif pitch ∈ [-10°, 35°] AND face_count >= 1:
    → PortraitMode (ML Kit Face Detection)
elif pitch ∈ [-10°, 35°] AND face_count == 0:
    → GeneralMode (三分法網格 + 水平儀)
elif pitch < -10°:
    → LowAngleMode (建議 0.5x 廣角)
```

### 3.2 ML Kit 模式互斥

Food Mode 使用 ML Kit Object Detection，Portrait Mode 使用 ML Kit Face Detection。
**兩者不可同時開在同一 ImageProxy 上。場景判斷引擎先決定模式，再啟用對應偵測器。**

此為架構決策（非限制）：手機內建相機也有手動場景切換，不同偵測模式各有功能邊界是合理的。

切換邏輯需要 hysteresis（遲滯）防止模式跳動：
```
if (currentMode == FoodMode && pitch < 30°) {
    // 延遲 500ms 後切換（不是 35° 立刻切）
    switchAfterDelay(PortraitMode, 500ms)
}
```

---

## 第四部分：規則引擎與優先級排序

### 4.1 一屏一指令原則

從所有 RuleResult 中選出 `canImprove == true` 的，按 `priority` 降序排列，只取 TOP 1。

```kotlin
fun prioritize(rules: List<RuleResult>): Instruction? {
    return rules
        .filter { it.canImprove }
        .maxByOrNull { it.priority * it.improvementEstimate }
        ?.toInstruction()
}
```

### 4.2 優先級計算公式

```
priority = ruleWeight × (100 - ruleScore) / 100 × confidenceFactor

其中:
  ruleWeight: 角度 0.40, 水平 0.25, 構圖 0.20, 光線 0.15
  ruleScore: 0-100
  confidenceFactor: IMU = 0.95, ML Kit face = 0.80, ML Kit object = 0.75, 直方圖 = 0.70
```

### 4.3 美食模式規則

| 規則 | 數據源 | 延遲 | 付費層 |
|------|--------|------|--------|
| AngleRule | IMU pitch vs [45°, 60°, 90°] | < 2ms | Free |
| LevelRule | IMU roll (+ dual-axis at 90°) | < 2ms | Free |
| FoodCompositionRule | ML Kit Object Detection bbox | < 80ms | Paid |
| LightRule | 直方圖平均亮度 | < 10ms | Paid |
| MinimalistRule | bbox ratio + background CV | < 10ms | Paid |

### 4.4 人像模式規則（六規則）

| 規則 | ML Kit 數據源 | 計算 | MVP 階段 |
|------|-------------|------|---------|
| ThirdsRule | face.boundingBox.center | 人臉中心 vs 四個黃金交叉點 | MVP (P0) |
| HeadroomRule | face.boundingBox.top | 比值 10%-20% 為佳 | MVP (P0) |
| EyeLineRule | landmarks[LEFT_EYE].y | 眼睛 Y vs 上三分線 | MVP (P1) |
| GazeRule | face.headEulerAngleY | 臉轉左→左側需更多空間 | MVP (P1) |
| TiltRule | face.headEulerAngleZ | 絕對值 > 8° 提示轉正 | Phase 1.5 |
| SmileRule | face.smilingProbability | > 0.4 持續 > 300ms = 黃金時刻 | Phase 1.5 |

### 4.5 遞手機模式規則

簡化版，只保留三條：FramingRule（居中）+ AngleRule（粗檢）+ LevelRule（粗檢）。
UI 使用 Halo 牽引 + Edge Warmth。對齊後 Halo 金色→綠色 + 呼吸。

### 4.6 減法留白偵測規則（MinimalistRule）

```kotlin
data class MinimalistResult(
    val isMinimalist: Boolean,
    val confidence: Float,
    val isBusyBackground: Boolean,
    val subjectOnPowerPoint: Boolean
)

fun detectMinimalist(
    subjectRatio: Float?,   // bbox.area / frame.area, null if no detection
    bgCV: Float,            // 背景區域亮度 Coefficient of Variation
    displacement: Float?,   // 主體偏離中心的歸一化距離
    thirdsDistance: Float?   // 主體到最近三分交叉點的歸一化距離
): MinimalistResult {

    // Path A: 有主體偵測（face 或 food object）
    if (subjectRatio != null && displacement != null) {
        val isSmall = subjectRatio < 0.10f
        val isClean = bgCV < 0.20f
        val isOffCenter = displacement > 0.20f
        val onPowerPt = (thirdsDistance ?: 1f) < 0.08f

        if (isSmall && isClean && isOffCenter) {
            return MinimalistResult(
                isMinimalist = true,
                confidence = if (onPowerPt) 0.95f else 0.75f,
                isBusyBackground = false,
                subjectOnPowerPoint = onPowerPt
            )
        }
        if (isSmall && !isClean) {
            return MinimalistResult(
                isMinimalist = false, confidence = 0f,
                isBusyBackground = true, subjectOnPowerPoint = false
            )
        }
    }

    // Path B: 無主體偵測 — 僅背景分析
    if (bgCV < 0.12f) {
        return MinimalistResult(
            isMinimalist = true, confidence = 0.5f,
            isBusyBackground = false, subjectOnPowerPoint = false
        )
    }

    return MinimalistResult(false, 0f, bgCV > 0.35f, false)
}

// 反向抑制邏輯：
// if (result.isMinimalist && result.confidence > 0.7f) {
//     priorityEngine.suppress("GET_CLOSER")
//     priorityEngine.suppress("CENTER_SUBJECT")
//     ambientUI.showConfirmation(GREEN_BREATHING)
// }
// if (result.isBusyBackground) {
//     ambientUI.showCoolEdgeHint()  // 背景區域微弱冷色暈
// }
```

**負空間均質度 (bgCV) 計算**：

```kotlin
fun negativeSpaceCV(
    yPlane: ByteArray, frameW: Int, frameH: Int,
    subjectCell: Int  // 0-8, 哪個 3x3 格子含主體
): Float {
    val cellW = frameW / 3
    val cellH = frameH / 3
    val cellMeans = FloatArray(9)

    for (i in 0 until 9) {
        val col = i % 3; val row = i / 3
        var sum = 0L; var count = 0
        for (y in row * cellH until (row + 1) * cellH) {
            for (x in col * cellW until (col + 1) * cellW) {
                sum += yPlane[y * frameW + x].toInt() and 0xFF
                count++
            }
        }
        cellMeans[i] = sum.toFloat() / count
    }

    val bgCells = cellMeans.toList().filterIndexed { idx, _ -> idx != subjectCell }
    val mean = bgCells.average().toFloat()
    if (mean < 1f) return 1f
    val stdDev = sqrt(bgCells.map { (it - mean).pow(2) }.average().toFloat())
    return stdDev / mean  // CV: < 0.20 乾淨, > 0.35 雜亂
}
```

### 4.7 背景雜亂時的 DOF 建議（v3.3 重寫：拍後處理方案）

**v3.3 架構變更**：DOF 從「拍攝時切換 CameraX Bokeh Extension」完全改為「拍後 AI 處理」方案。

#### 4.7.1 變更理由

| 維度 | 舊方案（拍攝時切換 Extension）| 新方案（拍後處理）|
|------|-----|------|
| ML Kit 可用性 | 啟用 Bokeh Extension 時 ImageAnalysis 可能不可用 | 拍攝全程 ImageAnalysis 正常運作 |
| 黑屏問題 | 需要光圈轉場動畫掩飾 500-1500ms rebind | 完全不需要 rebind，零中斷 |
| 裝置覆蓋 | 依賴 OEM 是否實作 Bokeh Extension (~60-70%) | ML Kit Selfie Segmentation 覆蓋 API 23+ (>99%) |
| 拍攝體驗 | 切換模式時 Ambient UI 引導可能暫停 | 拍攝全程所有引導正常運作 |
| 用戶體驗 | 需要在拍攝前決定是否啟用 DOF | 拍照後自動偵測+處理，用戶選擇保留哪版 |

#### 4.7.2 技術管線

**核心原理**：Pixel 系列的 Portrait Mode 本質上就是後置 AI 處理——先拍清晰照片，再用神經網路做深度估計+人物分割+散景渲染。SmartCameraApp 採用相同理念，但用 ML Kit Selfie Segmentation 替代 Google 私有的深度估計模型。

```
拍照完成
  → 原始照片存檔（JPEG，清晰版）
  → 背景雜亂偵測結果判定（已在拍攝時由 MinimalistRule 計算）
  → if (bgCV > 0.35 AND 有人臉):
      自動啟動拍後 DOF 處理（背景執行）
      
      Step 1: ML Kit Selfie Segmentation
        輸入: 原始照片 Bitmap
        輸出: SegmentationMask (每像素 0.0-1.0 前景信心度)
        延遲: 25-65ms

      Step 2: Mask 邊緣軟化
        Gaussian blur (kernel=7) 軟化前景/背景過渡
        延遲: ~10ms

      Step 3: 降解析度模糊（關鍵優化）
        原圖 4K → 下採樣 2K → StackBlur(radius=15) → 上採樣回 4K
        使用 Toolkit.blur()（Google 官方 RenderScript 替代，GPU 加速）
        延遲: 50-150ms（優化後）

      Step 4: 逐像素合成
        output = original * mask + blurred * (1 - mask)
        延遲: 30-80ms

      Step 5: 儲存散景版 + EXIF 標註
        延遲: 80-150ms

  → 總處理時間: 高階機 0.3-0.5s, 中階機 0.5-1s, 低階機 1-2s

  → UI: 拍後預覽底部顯示進度條 + 「正在處理景深效果...」
  → 處理完成: 顯示 [原始] ← 滑動 → [景深] 切換
  → 用戶選擇: [保留原始] [保留景深] [兩者都保留]
```

#### 4.7.3 ML Kit 路徑分階

| 路徑 | API | 適用場景 | 延遲 | 階段 |
|------|-----|---------|------|------|
| Selfie Segmentation | `com.google.mlkit:segmentation-selfie` | 人像場景 | 25-65ms | MVP (Phase 1.5) |
| Subject Segmentation | `com.google.mlkit:subject-segmentation` | 人+寵物+食物+物體 | ~200ms | Phase 2 |

MVP 階段只做人像場景的 DOF（Selfie Segmentation），美食/物體場景的 DOF 留 Phase 2（Subject Segmentation）。

#### 4.7.4 散景品質的誠實評估

SmartCameraApp 的拍後 DOF 產出的是「均勻高斯模糊」（整個背景一視同仁），而非 OEM 原生的「距離相關模糊」（離主體越遠越模糊）。

品質定位：相當於 Instagram 人像模式或早期 Pixel 2 的效果。對日常用戶足夠。Phase 2 可用 Subject Segmentation 的多主體分割實現 2-3 層分段模糊。

#### 4.7.5 DOF 功能定位

- **付費版功能**
- **MVP Sprint 排程**: 列入 Phase 1.5（MVP 上架後第一個月）
- **MVP 中只做 UI 骨架**: 雜亂背景偵測 + 虛線框 + DOF 按鈕（反灰），DOF 實際處理在 Phase 1.5 接入
- **開發成本**: 2-3 天（Segmentation 調用 + Toolkit.blur 合成 + 拍後預覽 UI）

#### 4.7.6 DOF 替代方案分層（100% 裝置覆蓋）

```
if (有人臉 AND bgCV > 0.35):
    → 自動拍後 DOF（ML Kit Selfie Segmentation）
    
elif (無人臉 AND bgCV > 0.35 AND 有長焦鏡頭):
    → 拍後教學卡片建議：「可切換到 {maxZoom}x 長焦鏡頭壓縮背景」
    
elif (無人臉 AND bgCV > 0.35 AND 無長焦):
    → 拍後教學卡片建議：「讓主體離背景更遠，或找更乾淨的角度」
    
else:
    → 不觸發 DOF 相關邏輯
```

#### 4.7.7 DOF 使用手冊文案

```
景深效果 (DOF)

工作原理：
拍照後，App 會自動分析照片並分離主體與背景，
為背景加上柔和的模糊效果以突顯主體。
處理完成後，您可以比較原始版和景深版，選擇保留哪一張。

適用場景：
- 人像攝影中背景雜亂
- 希望突出單一主體的場景
- App 偵測到背景雜亂時會自動建議

處理時間：
處理相片時可能需要比較長的時間，視您的裝置效能而定。
通常約 0.5-2 秒。

已知特性：
- 目前景深效果適用於人像場景（未來版本將支援美食和物體）
- 效果為均勻背景虛化，非距離相關模糊
- 髮絲和半透明物體的邊緣可能不完全精確
- 所有處理在裝置本地完成，不需要網路連線
```

---

## 第五部分：鏡頭髒汙偵測 (LensSmudgeDetector)

### 5.1 六步演算法

**Step 1: 下採樣 + 4x4 網格分割**
- 輸入: ImageProxy Y 通道, 640x480
- 分割為 16 個 160x120 區塊
- 延遲: ~1ms

**Step 2: 每區塊 Laplacian 方差**
- 3x3 Laplacian kernel: [0,-1,0 / -1,4,-1 / 0,-1,0]
- 無需 OpenCV，純像素迴圈
- 輸出: 16 個方差值
- 延遲: ~8ms

**Step 3: 誤報過濾**
- 排除自然平滑區域（天空、牆面、散景）
- 判斷條件: 高亮度+低像素方差 = 天空; 低亮度+低方差 = 暗部; 極低方差 = 純色
- 關鍵洞察: 髒汙區塊有「低 Laplacian 方差但中等像素方差」（紋理存在但邊緣被模糊）

**Step 4: 相對異常偵測**
- 與鄰近區塊比較，非固定閾值
- 異常閾值: 區塊方差 < 中位數的 35%

**Step 5: 時序穩定性 (5 幀緩衝)**
- 真正的髒汙在連續 5 幀中固定在同一 grid 位置（因為黏在鏡頭上）
- 場景中的模糊物體會隨鏡頭移動而改變位置，被此步驟排除
- 5 幀 × 10fps = 0.5 秒確認窗口

**Step 6: 輸出 + 冷卻**
- 偵測到後顯示右上角半透明水滴圖標（微微脈動，opacity 0.4-0.7）
- 點擊圖標顯示: 「鏡頭可能有髒汙，擦拭後再試？」
- 冷卻: 顯示後 30 秒內不重複提示

### 5.2 Settings 開關

`Settings > Camera > Lens smudge detection [Toggle]`
- 預設: 開啟
- 關閉後完全停止 SmudgeDetector 分析（節省 ~12ms/frame）

### 5.3 鏡頭髒汙偵測 — 適用場景（v3.3 更新：改為白名單機制）

```
鏡頭髒汙偵測 — 適用場景

本功能在以下場景中運作最佳：
✓ 日間戶外拍攝（明亮光線 + 豐富紋理）
✓ 室內正常照明（餐廳、咖啡廳、辦公室）
✓ 有清晰前景物體的場景（人像、美食、靜物）
✓ 手機有輕微移動（走動、轉換角度）

偵測原理需要畫面中存在足夠的邊緣資訊和場景變化。
極端環境下偵測能力會降低。
```

### 5.4 鏡頭髒汙 UI 行為

- 右上角顯示鏡頭有髒污圖標（半透明，微微脈動）
- 用戶注意到就擦，沒注意到就繼續拍（不阻斷拍攝流程）
- Settings 中提供啟用/關閉的功能按鈕

### 5.5 效能

- 總延遲: ~12ms/frame (at 10fps analysis rate)
- 記憶體: ~600KB (16 blocks + 5-frame history)
- CPU: 單核心即可

---

## 第六部分：美食主體構圖引導 (FoodCompositionRule)

### 6.1 技術路徑：ML Kit Object Detection FOOD 類別

ML Kit Object Detection 內建 `PredefinedCategory.FOOD` 分類器，可在 STREAM_MODE 下即時偵測食物並分配 trackingId 進行跨幀追蹤。信心度可達 90%。無需自訂模型。

```kotlin
val options = ObjectDetectorOptions.Builder()
    .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
    .enableClassification()
    .enableMultipleObjects()
    .build()
val detector = ObjectDetection.getClient(options)
```

### 6.2 與 Face Detection 的互斥處理

場景判斷引擎決定模式後，啟用對應偵測器：
- Food Mode → ObjectDetection (FOOD)
- Portrait Mode → FaceDetection

不可同時開啟。切換時先 `detector.close()` 再初始化新偵測器。加入 500ms hysteresis 防跳動。

### 6.3 美食構圖引導能力

| 能力 | 數據源 | Ambient UI 表現 |
|------|--------|----------------|
| 主體三分法對齊 | food bbox center vs golden points | Halo 牽引（與人像相同語法）|
| 主體佔比 | food bbox area / frame area | 太小觸發減法留白偵測 |
| 多道菜平衡 | 多個 food bbox 的分布 | 建議切換俯視 90° |
| 背景整潔度 | negativeSpaceCV | 雜亂 → 冷色暈提示 |

### 6.4 訂閱定位

美食構圖引導（FoodCompositionRule）列入**付費版**。理由：
- 每次食物位置不同 → 不可學會 → 持續需要
- 免費版已有角度引導（可學會的 hook）

---

## 第七部分：評分系統與 EXIF 寫入

### 7.1 評分公式

```
total_score = angle_score × 0.40
            + level_score × 0.25
            + composition_score × 0.20
            + light_score × 0.15

各子分數 0-100。
angle_score = 100 - min(|current_pitch - nearest_suggested|, 15) / 15 × 100
level_score = 100 - min(|roll|, 5) / 5 × 100
composition_score = f(thirds_distance, headroom, eye_line, gaze_space)
light_score = f(histogram_mean, histogram_spread)
```

### 7.2 EXIF 寫入規格

**不在照片上印分數。分數寫入 EXIF UserComment。**

```kotlin
// 寫入時機: ImageCapture.OnImageSavedCallback
val exif = ExifInterface(outputFilePath)
val scoreJson = JSONObject().apply {
    put("smartcamera_version", "1.0.0")
    put("score", totalScore)
    put("mode", currentMode.name)
    put("rules", JSONObject().apply {
        put("angle", JSONObject().apply {
            put("score", angleScore)
            put("target", targetAngle)
            put("actual", currentPitch)
        })
        put("level", JSONObject().apply {
            put("score", levelScore)
            put("roll_deg", currentRoll)
        })
        // ... 其他規則
    })
    put("iftr", guidanceFollowed)  // IFTR 追蹤
    put("minimalist_detected", isMinimalist)
}
exif.setAttribute(ExifInterface.TAG_USER_COMMENT, scoreJson.toString())
exif.saveAttributes()  // < 5ms
```

### 7.3 Free 版拍後教學卡片的模糊診斷

免費用戶看到角度/水平分數（清晰），構圖/光線/診斷文字全部 `blur(3dp)` 模糊。底部顯示「Unlock full diagnosis」CTA。

---

## 第八部分：Freemium 架構

### 8.1 免費版（Hook — 可學會的技能）

| 功能 | 價值定位 |
|------|---------|
| 角度引導（45°/60°/90°） | 3 次學會 → 建立信任 |
| 單軸水平儀 | 基礎工具 |
| 三分法網格 | 基礎工具 |
| Ambient UI（邊緣暖光 + 光點） | 體驗差異化 |
| 鏡頭髒汙偵測 | 信任建立器 (Trust Builder) |
| 閃光燈關閉提醒 | 低成本加分 |
| 每日 5 張帶評分照片 | 限制 → 轉換動力 |
| 遞手機模式：每月 3 次 | 社交場合 WOW → 轉換入口 |

### 8.2 付費版（$2.99/月 — 不可學會的即時診斷）

| 功能 | 不可學會原因 |
|------|------------|
| 構圖即時診斷 (L2) | 每次主體位置不同 |
| 人像四規則（三分法/頭部/眼睛/視線）| 每次人和距離不同 |
| 美食構圖引導 (ML Kit Object Detection) | 每次食物位置不同 |
| 光線環境分析 | 每次光線不同 |
| 減法留白偵測 | 每次場景不同 |
| 黃金時刻觸發 (Phase 1.5) | 時機不可預測 |
| 遞手機模式：無限 | 每次朋友不同 |
| 雙軸水平 + 黃金比例網格 | 進階工具 |
| 拍後景深效果 (DOF) | 每次背景不同 |
| 拍後診斷摘要 | 持續學習價值 |
| 無限帶評分照片 | 去除限制 |

### 8.3 Paywall 觸發機制

免費用戶在拍攝時**看得到**付費功能的視覺反饋（三分交叉點亮起、Halo 出現、光線暖冷色提示），但拍後教學卡片中**構圖/光線/診斷行被 blur(3dp) 模糊**。

用戶能隱約看到「有分數」「有顏色」（綠好橙待改），但看不清具體數字和文字。底部顯示「Unlock full diagnosis」CTA。

---

## 第九部分：北極星指標

### 9.1 唯一北極星：IFTR (Instruction Follow-Through Rate)

**定義**：用戶收到 Ambient UI 引導後，3 秒內手機姿態朝正確方向改變 ≥ 3° 的比例。

**目標**：> 70%

**為什麼是 IFTR 而非 GSSR**：GSSR（照片保留率）受太多非產品因素影響，且追蹤困難。IFTR 直接回答「Ambient UI 引導，用戶有跟嗎？」

### 9.2 MVP 追蹤的 3 個核心指標

| 優先級 | 指標 | 定義 | 目標 |
|--------|------|------|------|
| P0 | IFTR | 引導後 3 秒內姿態改變 ≥ 3° | > 70% |
| P1 | 模式觸發率 | 美食/人像/遞手機各模式 session 佔比 | 美食 > 40% |
| P2 | D7 回訪率 | 7 天後再次打開 App | > 25% |

---

## 第十部分：效能預算（v3.3 更新）

### 10.1 分幀計算策略

v3.2 最壞情況 92ms/frame，餘裕僅 8ms。v3.3 引入分幀計算降低每幀峰值負擔：

```
Frame 1: ML Kit + Rules + Priority → 65ms
Frame 2: Histogram + Minimalist + Clutter → 13ms
Frame 3: ML Kit + Rules + Priority → 65ms
Frame 4: Histogram + Minimalist + Clutter → 13ms
Frame 5: Lens Smudge (每 5 幀一次) → 12ms
```

**效果**: 每幀最高負擔從 92ms 降到 65ms，餘裕提升至 35ms。

### 10.2 管線延遲預算

| 管線階段 | 延遲上限 | 實際預估 |
|---------|---------|---------|
| IMU 讀取 + 平滑 | < 5ms | ~2ms |
| ML Kit Face/Object Detection | < 80ms | 30-60ms |
| 直方圖分析 | < 10ms | ~5ms |
| 場景判斷 | < 1ms | ~0.1ms |
| 規則引擎（含 MinimalistRule） | < 5ms | ~2ms |
| 鏡頭髒汙偵測 | < 15ms | ~12ms |
| 優先級排序 | < 1ms | ~0.1ms |
| UI 渲染 (Compose) | < 16ms | ~8ms |
| **端到端（分幀後峰值）** | **< 100ms** | **~65ms** |

記憶體預算：< 150MB（含 ML Kit 模型）
電池增耗：< 15%

### 10.3 三級裝置降級策略（v3.3 新增）

| 硬體等級 | 核心引導 | 輔助功能 | 關閉功能 |
|---------|---------|---------|---------|
| FULL / LEVEL_3 | Edge Warmth 60fps + Halo 10fps + 所有規則 | 鏡頭髒汙 2fps + 雜亂框框 + 減法留白 | 無 |
| LIMITED | Edge Warmth 60fps + Halo 8fps + 核心 4 規則 | 鏡頭髒汙 1fps + 減法留白 | 雜亂框框（僅冷色暈替代）|
| LEGACY | Edge Warmth 60fps + Halo 5fps + 2 規則 | 無 | 鏡頭髒汙 + 減法留白 + 雜亂框框 + 光線分析 |

**核心保護原則**: 無論哪個等級，Edge Warmth（IMU 驅動）永遠 60fps 運行——任何裝置都能跑。

### 10.4 Settings 效能模式（v3.3 新增）

```
Settings > Performance
  ○ Auto (recommended) — 根據裝置自動調整
  ○ Battery saver — 降低分析頻率至 5fps
  ○ Full features — 強制啟用所有功能
```

---

## 第十一部分：MVP Sprint 排程（15 天）

| 天數 | 交付 | 技術棧 |
|------|------|--------|
| D1-2 | 角度弧形 + Edge Warmth 第一層 + 場景自動切換 | IMU + Compose Canvas |
| D3-4 | 水平儀 + 三分法網格 + 浮動光點（Edge Warmth 第二層） | IMU + Compose |
| D5-6 | 優先級引擎 + 人像四規則（ThirdsRule/Headroom/EyeLine/Gaze）| ML Kit Face + Kotlin |
| D7-8 | 遞手機模式（Halo 牽引 UI）+ 首次教學動畫 | ML Kit Face + Compose |
| D9-10 | 美食構圖引導（ML Kit Object Detection FOOD）+ 減法留白偵測 | ML Kit ODT + Math |
| D11-12 | 鏡頭髒汙偵測 + 光線診斷（直方圖）+ 呼吸韻律 | ImageProxy + Compose |
| D13 | Paywall 整合 + 評分引擎 + EXIF 寫入 + 拍後教學卡片 + DOF UI 骨架（反灰按鈕）| ExifInterface + UI |
| D14-15 | IFTR 埋點 + 整合測試 + 餐廳實測 + 參數微調 | 全棧 |

### 被砍掉的功能（Phase 2+）

- 多種構圖網格切換（黃金比例、對角線等）
- CVPR SPAS 模型整合
- 焦段建議（0.5x/1x/3x）
- 拍後教學卡片的分享功能

---

## 第十二部分：Phase 路線圖（v3.3 更新）

### MVP (15 天)
維持上述排程。DOF 在 MVP 中只做 UI 骨架（反灰按鈕）。

### Phase 1.5 (MVP 上架後 30 天內)
- 拍後 DOF 處理管線（ML Kit Selfie Segmentation + Toolkit.blur）
- 人像 TiltRule + SmileRule
- DOF 按鈕啟用 + 拍後預覽比較 UI

### Phase 2 (有現金流後)
- ML Kit Subject Segmentation 替代 Selfie Segmentation（支援美食/物體 DOF）
- 多主體分段模糊（2-3 層距離分段）
- 多種構圖網格切換
- CVPR SPAS 模型評估
- 焦段建議

---

## 第十三部分：技術發現備忘（v3.3 新增）

### 13.1 Pixel 9a Portrait Mode 技術本質

經深度研究確認：Pixel 的 Portrait Mode 是後置 AI 處理——先拍清晰照片（HDR+），再用神經網路（TensorFlow Lite on GPU）做深度估計 + 人物分割 + 散景渲染。拍照後才處理，甚至可以在 Google Photos 中事後調整模糊程度。

**對 SmartCameraApp 的影響**: 驗證了「拍後 DOF」是業界主流做法，不是取巧。

### 13.2 CameraX 1.5 的 SessionConfig API

CameraX 1.5 引入 `isSessionConfigSupported` API，允許查詢 Extension + ImageAnalysis 是否可共存。此 API 在「拍後 DOF」方案中不再需要（因為完全不使用 Extension），但在未來版本如果要回到即時 Bokeh 預覽時可能有用。

---

## 第十四部分：風險矩陣（v3.3 合併更新）

| 風險 | 嚴重度 | 機率 | 對沖方案 |
|------|--------|------|---------|
| 用戶懶得切 App | 高 | 高 | First-time wow 壓到 10 秒 + IMU 自動場景判斷 |
| Ambient UI 太微妙用戶沒注意到 | 高 | 中 | 3 秒首次教學動畫 + 漸進強化（初期 alpha 稍高）|
| ML Kit Object Detection 對食物偵測不穩定 | 中 | 中 | 降級為純 IMU 模式（角度+水平+網格）|
| WayShot 推出 Android 版 | 高 | 中-高 | 深做美食+減法留白+遞手機，WayShot DNA 是人像+語音 |
| 鏡頭髒汙偵測誤報 | 低 | 低 | Settings 開關 + 使用手冊說明適用場景（白名單）|
| 7 天畢業效應 | 高 | 中 | 免費層=可學會 hook，付費層=不可學會即時診斷 |
| Selfie Segmentation 邊緣粗糙（髮絲/衣服） | 中 | 中 | Mask Gaussian softening (kernel=7) 漸變過渡 |
| 低階機 DOF 處理 > 2 秒 | 中 | 中 | 進度條 + 處理提示「正在處理景深效果」|
| 拍後 DOF 不支援食物場景 | 中 | 確定 | Phase 2 升級到 Subject Segmentation |
| 散景品質不如 OEM 原生 | 中 | 高 | 使用手冊誠實說明，Phase 2 分段模糊升級 |
| Selfie Segmentation 仍為 Beta | 低 | 低 | ML Kit Beta 穩定性良好 + Subject Seg 備選 |

---

## 文件元資訊

- **文件版本**: 3.3 (Converged SSOT)
- **建立日期**: 2026-04-04
- **作者**: CSO 策略分析 (Claude Opus 4.6) + 開發者 Akira
- **合併來源**: v3.2 SSOT (20260402) + Delta v3.2→v3.3 (20260403)
- **作廢文件**: v3.0, v3.1, v3.2, 及所有前版策略文件與 Delta 文件
- **文件狀態**: 完成 ✅ — 可交付架構師
- **下一步**: 開始 D1 實作
