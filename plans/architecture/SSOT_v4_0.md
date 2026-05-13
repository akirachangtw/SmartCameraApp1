# Cueshot 策略與架構 SSOT v4.0

**文件版本**: 4.0 (Converged Single Source of Truth)  
**建立日期**: 2026-05-12  
**文件性質**: 唯一真理文件 — 本文件作廢所有前版（v3.0~v3.3, Delta v3.4~v3.4.1）  
**作者**: CSO 策略分析 + Ambient UI Tech Auditor (Claude Opus 4.6) + 開發者 Akira  
**合併來源**: v3.3 SSOT + Delta v3.4.1 (corrected) + Layout Spec v2.1 佈局引用  
**搭配文件**: `Cueshot_Layout_Spec_v2_1.md`（佈局精確定義）

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

## 第二部分：Ambient UI 三通道 Traction 視覺引導體系

### 2.0 全局 Ambient 常數

所有 Ambient UI 元素共用的感知常數。統一後用戶不需要學習不同場景的不同「對齊門檻」— 體感永遠一致。

```kotlin
object AmbientConstants {
    // 對齊判定
    const val ALIGNED_THRESHOLD_DEG = 5f        // 所有場景統一對齊容許（±5°）

    // Level Traction 漸入
    const val LEVEL_FADE_RANGE_DEG = 5f          // Level Traction 漸入區間（0°=消失, 5°=完全可見）

    // 場景切換
    const val HYSTERESIS_SCENE_MS = 500L          // 場景切換防跳動
    const val HYSTERESIS_PITCH_LOW_DEG = 70f      // 45°→90° 切換下限
    const val HYSTERESIS_PITCH_HIGH_DEG = 80f     // 45°→90° 切換上限

    // 動畫時序
    const val BREATHE_CYCLE_MS = 2000L            // 呼吸韻律週期
    const val TRANSITION_FAST_MS = 300L           // 標準快速過渡
    const val SHUTTER_FLASH_DURATION_MS = 300L    // 快門閃爍持續時間
}
```

### 2.0.1 Level Traction 漸入退場規格

Level Traction 不是「開/關」二元切換，而是根據偏差程度漸入/漸出：

| 偏差（°） | opacity | 視覺狀態 |
|----------|---------|---------|
| 0° | 0.00 | 完全消失（已對齊 — 退場行為） |
| 1° | 0.20 | 微弱浮現 |
| 2° | 0.40 | 輕度可見 |
| 3° | 0.60 | 中度可見 |
| 4° | 0.80 | 強烈可見 |
| ≥5° | 1.00 | 完全可見（鎖定最大值） |

```kotlin
val levelOpacity = (maxDeviation / AmbientConstants.LEVEL_FADE_RANGE_DEG)
    .coerceIn(0f, 1f)
```

其中 `maxDeviation = max(abs(pitchDiff), abs(rollDiff))`。

### 2.1 核心原則

**不告訴用戶「你錯了」，而是讓畫面的視覺環境自然牽引用戶做出正確動作。**

類比：道路上的車道標線（你不需要「閱讀」標線，身體自然知道往哪裡開）。

### 2.2 三通道 Traction 引導體系

| 通道 | 職責 | 感測器 | 刷新率 | 視覺表現 |
|------|------|--------|--------|---------|
| **Pitch Traction** | 俯仰角引導（45°/90° 牽引） | IMU (accelerometer + magnetometer) | 60fps | 畫面邊緣漸層琥珀暖光，指向修正方向 |
| **Level Traction** | 雙軸水平修正（Roll + Pitch 微調） | IMU (accelerometer + magnetometer) | 60fps | 水平儀虛線 + 動態修正指示 |
| **Halo Traction** | 構圖位置牽引（跟隨 ML Kit bbox） | ML Kit (Face/Object Detection) | 10fps + EMA 平滑 | 主體周圍柔和光環，偏離目標時邊緣不均勻 |

**三通道獨立運作、獨立降級**：
- Pitch Traction 操作在螢幕邊緣（漸層暖光）
- Level Traction 操作在螢幕中央（虛線水平儀）
- Halo Traction 操作在主體周圍（圓形光環）
- 三者視覺區域不衝突，可同時啟用

**三通道均為 IMU 或 ML Kit 驅動，不存在任何名為 Edge Warmth 的渲染元件。**

### 2.3 Pitch Traction 規格

```kotlin
// presentation/camera/overlay/PitchTractionLayer.kt

private fun DrawScope.drawPitchTraction(
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

| 場景 | Traction 通道行為 |
|------|-----------------|
| Food 45° | Pitch Traction 向 45° 牽引 + Level Traction 水平修正 |
| Food 90° | Pitch Traction 向 90° 牽引 + Level Traction 雙軸（Roll + Pitch 微調）|
| Portrait | Level Traction 水平修正 |
| Pass-to-Friend | Pitch Traction + Level Traction 簡化版 |
| Minimalist | Level Traction 水平修正（不做角度引導）|
| General | Level Traction 水平修正 |

**Level Traction 退場行為**：

> Level Traction 對齊後**完全消失**（alpha → 0），不保留基底 alpha。  
> 設計理由：符合 Ambient UI「完成即退場」哲學。全局對齊確認由快門外環呼吸韻律承擔。

**橫拍 Level Traction**：

> MVP 階段橫拍時 Level Traction **隱藏不顯示**。  
> 理由：「不做比做錯好」— 橫拍的水平判定需要四方向旋轉自適應邏輯，MVP 來不及做且做錯會破壞信任。  
> Phase 1.5 排程：實作 `OrientationAwareLevel` 四方向自適應。

### 2.3.1 Pitch Traction 動態角度配置

```kotlin
data class PitchTractionConfig(
    val targetDeg: Float,           // 45f or 90f
    val orbitRatio: Float,          // Pitch Traction 漸層深度佔螢幕比例
    val alignedThresholdDeg: Float  // 對齊容許誤差
) {
    companion object {
        val FOOD_45 = PitchTractionConfig(
            targetDeg = 45f,
            orbitRatio = 0.30f,     // 較長軌道 — 45° 是大多數用戶的學習區
            alignedThresholdDeg = 5f
        )
        val FOOD_90 = PitchTractionConfig(
            targetDeg = 90f,
            orbitRatio = 0.20f,     // 較短軌道 — 90° 手機已接近水平，物理行程短
            alignedThresholdDeg = 5f
        )
    }
}

/**
 * 場景引擎中的 Pitch 配置選擇邏輯
 * hysteresis: 從 45° 配置切到 90° 需 pitch > 80°
 *             從 90° 配置切到 45° 需 pitch < 70°
 */
fun selectPitchConfig(currentPitch: Float, currentConfig: PitchTractionConfig): PitchTractionConfig {
    return when {
        currentConfig == FOOD_45 && currentPitch > 80f -> FOOD_90
        currentConfig == FOOD_90 && currentPitch < 70f -> FOOD_45
        else -> currentConfig  // hysteresis 區間內維持不變
    }
}
```

| 配置 | 目標角度 | 軌道比例 | 對齊容許 | 啟用條件 |
|------|---------|---------|---------|---------|
| `FOOD_45` | 45° | 30% | ±5° | pitch ∈ [35°, 80°) |
| `FOOD_90` | 90° | 20% | ±5° | pitch ∈ (70°, 90°] |
| 切換閾值 | — | — | — | 75°（hysteresis 70°↔80°）|

### 2.4 Level Traction 規格

Level Traction 是原「水平儀」功能的正式名稱，以雙軸 IMU 驅動。

**渲染結構**：
- **reference 層**（靜態）：白色虛線 + 空心圓刻度標記 — 代表「理想水平線」
- **dynamic 層**（動態）：實線 + 實心圓 — 跟隨手機實際 Roll/Pitch，偏移時顯示琥珀金，對齊時變綠

**顏色邏輯**：
- reference 層：固定白色 α0.5
- dynamic 層：琥珀金 `#D4A030`（未對齊）→ 對齊綠 `#50C864`（已對齊）

**Canvas 優化**：reference 層首次繪製後快取為 Bitmap，後續幀直接 `drawImage`（4 次 drawXxx → 1 次）。

### 2.5 Halo Traction 規格

所有 Halo 統一為圓形，共用 `HaloGuidanceLayer` 組件。

```kotlin
// presentation/camera/overlay/HaloGuidanceLayer.kt

@Composable
fun HaloGuidanceLayer(
    subjectBbox: Rect?,
    targetPosition: Offset?,
    isAligned: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (subjectBbox == null) return@Canvas

        val haloCx = subjectBbox.center.x
        val haloCy = subjectBbox.center.y
        val haloRadius = max(subjectBbox.width, subjectBbox.height) * 0.6f

        val dx = (targetPosition?.x ?: haloCx) - haloCx
        val dy = (targetPosition?.y ?: haloCy) - haloCy
        val angle = atan2(dy, dx)

        val color = if (isAligned) Color(0xFF50C864)  // 綠色
                    else Color(0xFFD4A030)              // 金色

        drawCircle(
            color = color.copy(alpha = if (isAligned) 0.35f else 0.25f),
            radius = haloRadius,
            center = Offset(haloCx, haloCy),
            style = Stroke(width = 2f)
        )

        if (!isAligned) {
            // 不對齊 → 向目標側加亮弧
        }
    }
}
```

**統一 Halo 語言表**：

| 元素 | 形狀 | 顏色邏輯 | 觸發 |
|------|------|---------|------|
| 手動對焦 | 圓形 Halo | 金色→綠色 | 用戶觸摸螢幕 |
| 構圖引導（單主體）| 圓形 Halo（跟隨 bbox） | 金色→綠色 | ML Kit 偵測到主體 |
| 構圖引導（多人）| 群組包圍 Halo | 金色→綠色 | ML Kit 偵測 ≥2 人臉 |
| 美食引導 | 圓形 Halo（跟隨最大 food bbox） | 金色→綠色 | ML Kit Object Detection FOOD |
| 遞手機模式 | 圓形 Halo（跟隨人臉） | 金色→綠色 | Pass-to-Friend 模式 |
| 減法留白確認 | 微小圓形 Halo | 綠色 | MinimalistRule 通過 |

**場景映射**：

| 場景 | Halo 行為 |
|------|----------|
| Food（有 ML Kit Object Detection bbox）| 食物 Halo → 三分法對齊（MVP 策略 A：跟隨最大 bbox）|
| Portrait（單人，有 Face Detection bbox）| 人臉 Halo → 三分法 + 頭部空間 + 視線空間 |
| Portrait（多人）| 群組包圍 Halo → 人數依賴 target（見 §2.5.2）|
| Pass-to-Friend | 人臉 Halo + 中心引力場 |
| Minimalist（小主體）| 微小確認 Halo（綠色，不說「靠近」）|
| Food（無 bbox）| 無 Halo，僅用 Pitch Traction + 網格 |
| General | 無 Halo，僅網格 |

**技術可行性**：CameraX 對焦 API (`CameraControl.startFocusAndMetering()`) 只需座標點，視覺形狀完全由 App UI 層自行繪製。改圓形零技術障礙。

### 2.5.1 美食 Halo 策略

```
MVP 策略 A：單一 Halo 跟隨最大 food bbox

選擇邏輯：
  foodObjects.maxByOrNull { it.boundingBox.area() }

EMA 平滑參數：
  Portrait EMA: smoothed = last * 0.7 + new * 0.3  (歷史權重 70%)
  Food EMA:     smoothed = last * 0.8 + new * 0.2  (歷史權重 80%)

  理由：食物不會像人臉那樣快速移動，
        更重歷史值可抑制 ML Kit Object Detection 的 bbox 抖動

Phase 1.5 擴展：
  多道菜場景 → 偵測到 ≥2 個 food bbox 時
  → Toast 建議：「多道菜？試試 90° 俯拍」
  → 不做多 Halo（視覺過於複雜）
```

### 2.5.2 多人群組 Halo

```kotlin
data class GroupHaloResult(
    val center: Offset,
    val radius: Float,
    val target: Offset,
    val alpha: Float
)

fun calculateGroupHalo(
    faces: List<Face>,
    screenWidth: Float,
    screenHeight: Float
): GroupHaloResult {
    val centers = faces.map { it.boundingBox.center() }

    val cx = centers.map { it.x }.average().toFloat()
    val cy = centers.map { it.y }.average().toFloat()

    val maxDist = centers.maxOf { distance(it, Offset(cx, cy)) }
    val padding = faces.maxOf { max(it.boundingBox.width(), it.boundingBox.height()) } * 0.3f
    val radius = maxDist + padding

    val target = when (faces.size) {
        2 -> Offset(screenWidth * 0.5f, screenHeight * 0.33f)
        3 -> Offset(screenWidth * 0.5f, screenHeight * 0.40f)
        else -> Offset(screenWidth * 0.5f, screenHeight * 0.5f)
    }

    val baseAlpha = 0.25f
    val groupAlpha = baseAlpha - 0.05f  // = 0.20

    return GroupHaloResult(Offset(cx, cy), radius, target, groupAlpha)
}
```

| 人數 | 構圖目標位置 | 理由 |
|------|------------|------|
| 2 人 | 水平中央，上三分線 (y=33%) | 雙人並排的經典構圖 |
| 3 人 | 中央偏上 (y=40%) | 三角構圖重心略高 |
| 4+ 人 | 正中央 (y=50%) | 多人合照以居中為優先 |

**Alpha 規則**：群組 Halo alpha = 單人 alpha − 0.05（未對齊 0.20 / 對齊 0.30），補償大面積 Halo 的視覺壓迫感。

**群組質心 EMA 平滑**：

```kotlin
private var lastGroupCenter: Offset? = null
private var lastGroupRadius: Float? = null

private companion object {
    const val GROUP_EMA_HISTORY = 0.75f
    const val GROUP_EMA_NEW = 0.25f
}

fun smoothGroupHalo(raw: GroupHaloResult): GroupHaloResult {
    val smoothedCenter = lastGroupCenter?.let { last ->
        Offset(
            x = last.x * GROUP_EMA_HISTORY + raw.center.x * GROUP_EMA_NEW,
            y = last.y * GROUP_EMA_HISTORY + raw.center.y * GROUP_EMA_NEW
        )
    } ?: raw.center

    val smoothedRadius = lastGroupRadius?.let { last ->
        last * GROUP_EMA_HISTORY + raw.radius * GROUP_EMA_NEW
    } ?: raw.radius

    lastGroupCenter = smoothedCenter
    lastGroupRadius = smoothedRadius

    return raw.copy(center = smoothedCenter, radius = smoothedRadius)
}
```

**EMA 參數設計對比**：

| 場景 | 歷史權重 | 新值權重 | 設計理由 |
|------|---------|---------|---------|
| Portrait 單人 | 0.7 | 0.3 | 人臉會轉頭點頭，需較快反應 |
| Group 多人 | 0.75 | 0.25 | 多人質心比單人更容易跳，但仍需追蹤隊形變化 |
| Food 美食 | 0.8 | 0.2 | 食物不動，最重穩定性 |

### 2.6 呼吸韻律 (Breathing Rhythm)

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

- Pitch Traction：從「有方向的暖光」變成「四邊均勻呼吸」
- Halo Traction：從金色變綠色，進入呼吸
- 快門按鈕外環：同步呼吸
- 含義：「一切就緒，隨時可以按快門」

### 2.7 首次使用教學動畫

3 秒連續動畫循環一次（不是文字教學頁）：
1. 手機圖示出現 → 邊緣暖光亮起（方向性）
2. 手機跟著暖光方向旋轉 → 暖光蔓延至四邊
3. 四邊均勻呼吸 → 快門按鈕呼吸
4. 「Ready to shoot!」閃爍消失

開發成本：0.5 天。Compose 動畫序列。

### 2.8 雜亂背景區域 UI

**圖示規格**: 黃色三角形 + 黑色驚嘆號 + Alpha 0.4  
**框線**: 虛線 (dashed) + Alpha 0.4

```kotlin
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
    → GeneralMode (三分法網格 + Level Traction)
elif pitch < -10°:
    → LowAngleMode (建議 0.5x 廣角)
```

### 3.2 ML Kit 模式互斥

Food Mode 使用 ML Kit Object Detection，Portrait Mode 使用 ML Kit Face Detection。
**兩者不可同時開在同一 ImageProxy 上。場景判斷引擎先決定模式，再啟用對應偵測器。**

切換邏輯需要 hysteresis（遲滯）防止模式跳動：

```kotlin
if (currentMode == FoodMode && pitch < 30°) {
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

| 規則 | 數據源 | 延遲 | 付費層 | 備註 |
|------|--------|------|--------|------|
| AngleRule | IMU pitch vs [45°, 60°, 90°] | < 2ms | Free | Pitch 配置見 §2.3.1 |
| LevelRule | IMU roll (+ dual-axis at 90°) | < 2ms | Free | — |
| FoodCompositionRule | ML Kit Object Detection bbox | < 80ms | Paid | Halo 跟隨最大 bbox；EMA 0.8/0.2（見 §2.5.1）|
| LightRule | 直方圖平均亮度 | < 10ms | Paid | — |
| MinimalistRule | bbox ratio + background CV | < 10ms | Paid | — |

### 4.4 人像模式規則

| 規則 | ML Kit 數據源 | 計算 | MVP 階段 |
|------|-------------|------|---------|
| ThirdsRule | face.boundingBox.center | 人臉中心 vs 四個黃金交叉點 | MVP (P0) |
| HeadroomRule | face.boundingBox.top | 比值 10%-20% 為佳 | MVP (P0) |
| EyeLineRule | landmarks[LEFT_EYE].y | 眼睛 Y vs 上三分線 | MVP (P1) |
| GazeRule | face.headEulerAngleY | 臉轉左→左側需更多空間 | MVP (P1) |
| TiltRule | face.headEulerAngleZ | 絕對值 > 8° 提示轉正 | Phase 1.5 |
| SmileRule | face.smilingProbability | > 0.4 持續 > 300ms = 黃金時刻 | Phase 1.5 |

### 4.5 遞手機模式規則

簡化版，保留三條核心規則 + Level Traction：

- **FramingRule**（居中）— 朋友幫拍最容易失誤：主體沒居中
- **LevelRule**（粗檢）— 朋友幫拍第二大失誤：手機歪斜
- **AngleRule**（粗檢）— 可犧牲：PTF 是社交場景，朋友不會為角度死磕

UI 使用 Halo Traction + Pitch Traction + Level Traction。對齊後 Halo 金色→綠色 + 呼吸。

Level Traction 的 IMU 驅動成本極低（60fps 無額外負擔），加入 PTF 的 Canvas 呼叫從 7 次增至 ~10 次，仍在預算內。

### 4.5.1 PTF Level Traction 顏色策略

> PTF 模式下 Level Traction 的 dynamic 層（移動線 + 圓點）**固定使用對齊綠 `#50C864`**，不經過 amber→green 漸變。  
> reference 層（虛線 + 空心圓）維持白色不變。

**設計理由**：PTF 用戶是「不懂攝影的朋友」。amber 金色在 Cueshot 全局視覺語言中代表「尚未對齊」，對陌生用戶產生「我做錯了」的焦慮感。固定綠色傳達「跟著這條線走就好」的正向引導，降低認知壓力。

**退場行為不變**：對齊後 α→0 完全消失，與所有模式一致。

```kotlin
val levelDynamicColor = if (isPtfMode) {
    AlignGreen  // PTF：固定綠色
} else {
    if (isLevelAligned) AlignGreen else AmberGold
}
```

### 4.6 減法留白偵測規則 (MinimalistRule)

```kotlin
data class MinimalistResult(
    val isMinimalist: Boolean,
    val confidence: Float,
    val isBusyBackground: Boolean,
    val subjectOnPowerPoint: Boolean
)

fun detectMinimalist(
    subjectRatio: Float?,
    bgCV: Float,
    displacement: Float?,
    thirdsDistance: Float?
): MinimalistResult {
    // Path A: 有主體偵測
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
            return MinimalistResult(false, 0f, true, false)
        }
    }

    // Path B: 無主體偵測
    if (bgCV < 0.12f) {
        return MinimalistResult(true, 0.5f, false, false)
    }

    return MinimalistResult(false, 0f, bgCV > 0.35f, false)
}
```

**負空間均質度 (bgCV) 計算**：

```kotlin
fun negativeSpaceCV(
    yPlane: ByteArray, frameW: Int, frameH: Int,
    subjectCell: Int
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

### 4.7 背景雜亂時的 DOF 建議（拍後處理方案）

#### 4.7.1 技術管線

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

      Step 3: 降解析度模糊
        原圖 4K → 下採樣 2K → StackBlur(radius=15) → 上採樣回 4K
        使用 Toolkit.blur()（GPU 加速）
        延遲: 50-150ms

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

#### 4.7.2 ML Kit 路徑分階

| 路徑 | API | 適用場景 | 延遲 | 階段 |
|------|-----|---------|------|------|
| Selfie Segmentation | `com.google.mlkit:segmentation-selfie` | 人像場景 | 25-65ms | Phase 1.5 |
| Subject Segmentation | `com.google.mlkit:subject-segmentation` | 人+寵物+食物+物體 | ~200ms | Phase 2 |

#### 4.7.3 散景品質定位

均勻高斯模糊（非距離相關模糊）。品質定位：相當於 Instagram 人像模式或早期 Pixel 2 效果。Phase 2 可用 Subject Segmentation 的多主體分割實現 2-3 層分段模糊。

#### 4.7.4 DOF 替代方案分層（100% 裝置覆蓋）

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

#### 4.7.5 DOF 功能定位

- 付費版功能
- MVP Sprint 中只做 UI 骨架：雜亂背景偵測 + 虛線框 + DOF 按鈕（反灰）
- DOF 實際處理在 Phase 1.5 接入
- 開發成本: 2-3 天

---

## 第五部分：鏡頭髒汙偵測 (LensSmudgeDetector)

### 5.1 六步演算法

**Step 1: 下採樣 + 4x4 網格分割**
- 輸入: ImageProxy Y 通道, 640x480
- 分割為 16 個 160x120 區塊
- 延遲: ~1ms

**Step 2: 每區塊 Laplacian 方差**
- 3x3 Laplacian kernel: [0,-1,0 / -1,4,-1 / 0,-1,0]
- 純像素迴圈，無需 OpenCV
- 延遲: ~8ms

**Step 3: 誤報過濾**
- 髒汙區塊特徵：低 Laplacian 方差但中等像素方差（紋理存在但邊緣被模糊）
- 排除自然平滑區域（天空、牆面、散景）

**Step 4: 相對異常偵測**
- 與鄰近區塊比較，非固定閾值
- 異常閾值: 區塊方差 < 中位數的 35%

**Step 5: 時序穩定性 (5 幀緩衝)**
- 真正的髒汙在連續 5 幀中固定在同一 grid 位置
- 5 幀 × 10fps = 0.5 秒確認窗口

**Step 6: 輸出 + 冷卻**
- 右上角半透明水滴圖標（微微脈動，opacity 0.4-0.7）
- 冷卻: 顯示後 30 秒內不重複提示

### 5.2 Settings 開關

`Settings > Camera > Lens smudge detection [Toggle]`
- 預設: 開啟
- 關閉後完全停止分析（節省 ~12ms/frame）

### 5.3 適用場景（白名單機制）

```
✓ 日間戶外拍攝（明亮光線 + 豐富紋理）
✓ 室內正常照明（餐廳、咖啡廳、辦公室）
✓ 有清晰前景物體的場景（人像、美食、靜物）
✓ 手機有輕微移動（走動、轉換角度）
```

### 5.4 效能

- 總延遲: ~12ms/frame (at 10fps analysis rate)
- 記憶體: ~600KB (16 blocks + 5-frame history)
- CPU: 單核心即可

---

## 第六部分：美食主體構圖引導 (FoodCompositionRule)

### 6.1 技術路徑：ML Kit Object Detection FOOD 類別

```kotlin
val options = ObjectDetectorOptions.Builder()
    .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
    .enableClassification()
    .enableMultipleObjects()
    .build()
val detector = ObjectDetection.getClient(options)
```

### 6.2 與 Face Detection 的互斥處理

場景判斷引擎決定模式後，啟用對應偵測器。不可同時開啟。切換時先 `detector.close()` 再初始化新偵測器。加入 500ms hysteresis 防跳動。

### 6.3 美食構圖引導能力

| 能力 | 數據源 | Ambient UI 表現 | 備註 |
|------|--------|----------------|------|
| 主體三分法對齊 | food bbox center vs golden points | Halo Traction 牽引（與人像相同語法）| MVP：跟隨最大 bbox（策略 A）|
| 主體佔比 | food bbox area / frame area | 太小觸發減法留白偵測 | — |
| 多道菜平衡 | 多個 food bbox 的分布 | MVP 不做多 Halo；Toast 建議 90° 俯拍 | Phase 1.5 擴展 |
| 背景整潔度 | negativeSpaceCV | 雜亂 → 冷色暈提示 | — |

### 6.4 訂閱定位

美食構圖引導列入**付費版**。理由：每次食物位置不同 → 不可學會 → 持續需要。

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
    })
    put("iftr", guidanceFollowed)
    put("minimalist_detected", isMinimalist)
}
exif.setAttribute(ExifInterface.TAG_USER_COMMENT, scoreJson.toString())
exif.saveAttributes()  // < 5ms
```

### 7.3 Free 版拍後教學卡片

免費用戶看到角度/水平分數（清晰），構圖/光線/診斷文字全部 `blur(3dp)` 模糊。底部顯示「Unlock full diagnosis」CTA。

---

## 第八部分：Freemium 架構

### 8.1 免費版（Hook — 可學會的技能）

| 功能 | 價值定位 |
|------|---------|
| 角度引導（45°/60°/90°） | 3 次學會 → 建立信任 |
| 單軸水平儀 | 基礎工具 |
| 三分法網格 | 基礎工具 |
| Ambient UI（Pitch Traction 邊緣暖光 + 光點） | 體驗差異化 |
| 鏡頭髒汙偵測 | 信任建立器 (Trust Builder) |
| 閃光燈關閉提醒 | 低成本加分 |
| 每日 5 張帶評分照片 | 限制 → 轉換動力 |
| 遞手機模式：每月 3 次 | 社交場合 WOW → 轉換入口 |

### 8.2 付費版（$2.99/月 — 不可學會的即時診斷）

| 功能 | 不可學會原因 |
|------|------------|
| 構圖即時診斷 (L2) | 每次主體位置不同 |
| 人像四規則 | 每次人和距離不同 |
| 美食構圖引導 | 每次食物位置不同 |
| 光線環境分析 | 每次光線不同 |
| 減法留白偵測 | 每次場景不同 |
| 黃金時刻觸發 (Phase 1.5) | 時機不可預測 |
| 遞手機模式：無限 | 每次朋友不同 |
| 雙軸水平 + 黃金比例網格 | 進階工具 |
| 拍後景深效果 (DOF) | 每次背景不同 |
| 拍後診斷摘要 | 持續學習價值 |
| 無限帶評分照片 | 去除限制 |

### 8.3 Paywall 觸發機制

免費用戶在拍攝時**看得到**付費功能的視覺反饋，但拍後教學卡片中構圖/光線/診斷行被 blur(3dp) 模糊。

---

## 第九部分：北極星指標

### 9.1 唯一北極星：IFTR (Instruction Follow-Through Rate)

**定義**：用戶收到 Ambient UI 引導後，3 秒內手機姿態朝正確方向改變 ≥ 3° 的比例。  
**目標**：> 70%

### 9.2 MVP 追蹤的 3 個核心指標

| 優先級 | 指標 | 定義 | 目標 |
|--------|------|------|------|
| P0 | IFTR | 引導後 3 秒內姿態改變 ≥ 3° | > 70% |
| P1 | 模式觸發率 | 美食/人像/遞手機各模式 session 佔比 | 美食 > 40% |
| P2 | D7 回訪率 | 7 天後再次打開 App | > 25% |

---

## 第十部分：效能預算

### 10.1 分幀計算策略

```
Frame 1: ML Kit + Rules + Priority → 65ms
Frame 2: Histogram + Minimalist + Clutter → 13ms
Frame 3: ML Kit + Rules + Priority → 65ms
Frame 4: Histogram + Minimalist + Clutter → 13ms
Frame 5: Lens Smudge (每 5 幀一次) → 12ms
```

每幀最高負擔從 92ms 降到 65ms，餘裕提升至 35ms。

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

### 10.3 三級裝置降級策略

| 硬體等級 | Pitch Traction | Level Traction | Halo Traction | 輔助功能 | 關閉功能 | 群組 Halo | 快門冷卻 |
|---------|---------------|---------------|--------------|---------|---------|----------|---------|
| FULL / LEVEL_3 | 60fps | 60fps | 10fps + 所有規則 | 髒汙 2fps + 雜亂框框 + 減法留白 | 無 | 完整群組包圍 | 300ms |
| LIMITED | 60fps | 60fps | 8fps + 核心 4 規則 | 髒汙 1fps + 減法留白 | 雜亂框框（僅冷色暈替代）| 完整群組包圍 | 300ms |
| LEGACY | 60fps | 60fps | 5fps + 2 規則 | 無 | 髒汙 + 減法留白 + 雜亂框框 + 光線分析 | 降級為最大人臉單人 Halo | 500ms + 外環閃爍 |

**核心保護原則**: 無論哪個等級，Pitch Traction + Level Traction（IMU 驅動）永遠 60fps 運行——任何裝置都能跑。Halo Traction 受 ML Kit 分析頻率限制，按裝置等級降低刷新率。

**LEGACY 500ms 設計理由**：LEGACY 裝置關閉了大部分輔助功能（含 Flash 提醒），冷卻延長至 500ms + 快門外環閃爍一次（白色 alpha 0.45 → 0 在 300ms 內），補償 FULL/LIMITED 裝置上由 Flash 動畫提供的「已拍攝」確認感。

### 10.3.1 快門外環動畫互斥規則

快門外環同時承擔「全條件對齊確認」（呼吸韻律）與「已拍攝確認」（閃爍）兩個視覺職責。兩者**不可同時播放**：

| 時間點 | 行為 | 視覺狀態 | 設計理由 |
|--------|------|---------|---------|
| T0: 快門按下 | 暫停呼吸動畫，凍結外環為靜態綠色 | 外環靜態綠色 | 清空視覺通道，準備接收閃爍信號 |
| T0 ~ T0+300ms | 外環閃爍一次：白色 α0.45→0 | 白色閃爍 | 「已拍攝」確認 |
| T0+300ms ~ T0+冷卻結束 | 外環維持靜態，不恢復呼吸 | 靜態 | 冷卻期間快門不可按 |
| T0+冷卻結束 | 判斷是否仍全對齊 → 是則恢復呼吸 | 呼吸或靜態 | 重新允許拍攝 |

| 裝置等級 | 冷卻時長 | 閃爍規格 |
|---------|---------|---------|
| FULL / LIMITED | 300ms | 白色 α0.45→0, 300ms |
| LEGACY | 500ms | 白色 α0.45→0, 300ms + 200ms 靜態等待 |

```kotlin
var isBreathing by remember { mutableStateOf(false) }
var isFlashing by remember { mutableStateOf(false) }

val breatheAlpha by if (isBreathing) {
    infiniteTransition.animateFloat(
        initialValue = 0.10f, targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        )
    )
} else {
    remember { mutableFloatStateOf(0f) }
}

fun onShutterPressed() {
    isBreathing = false
    isFlashing = true

    scope.launch {
        flashAlpha.animateTo(0.45f, tween(50))
        flashAlpha.animateTo(0f, tween(250))
        delay(if (isLegacy) 200L else 0L)
        isFlashing = false
        if (allConditionsMet) {
            isBreathing = true
        }
    }
}
```

### 10.4 Settings 效能模式

```
Settings > Performance
  ○ Auto (recommended) — 根據裝置自動調整
  ○ Battery saver — 降低分析頻率至 5fps
  ○ Full features — 強制啟用所有功能
```

### 10.5 Canvas 呼叫預算

每幀 Compose Canvas `drawXxx` 呼叫次數上限 **< 15 次**。超過需優化。

| 狀態 | 場景 | Canvas 呼叫數 | 狀態 | 優化方案 |
|------|------|-------------|------|---------|
| State 1 | General | 11 | ✅ 預算內 | — |
| State 2 | Portrait Unaligned | 12 | ✅ 預算內 | Level reference 層預渲染為 Bitmap |
| State 3 | Portrait Aligned | ~8 | ✅ 預算內 | — |
| State 4 | PTF | ~10 | ✅ 預算內 | 含 Level Traction |
| State 5 | Focus | 11 | ✅ 預算內 | Focus ring 合併繪製 |
| State 6 | Food | ~12 | ✅ 預算內 | 含 Food Halo |

**State 2 優化策略**：Level reference 線（虛線 + 刻度標記）首次繪製後快取為 Bitmap，後續幀直接 `drawImage`（4→1 次）。記憶體 ~50KB。

---

## 第十一部分：MVP Sprint 排程（15 天）

| 天數 | 交付 | 技術棧 |
|------|------|--------|
| D1-2 | 角度弧形 + Pitch Traction 漸層 + 場景自動切換 | IMU + Compose Canvas |
| D3-4 | Level Traction（水平儀）+ 三分法網格 + 浮動光點 | IMU + Compose |
| D5-6 | 優先級引擎 + 人像四規則 | ML Kit Face + Kotlin |
| D7-8 | 遞手機模式（Halo Traction UI）+ 首次教學動畫 | ML Kit Face + Compose |
| D9-10 | 美食構圖引導 + 減法留白偵測 | ML Kit ODT + Math |
| D11-12 | 鏡頭髒汙偵測 + 光線診斷 + 呼吸韻律 | ImageProxy + Compose |
| D13 | Paywall + 評分引擎 + EXIF + 拍後教學卡片 + DOF UI 骨架 | ExifInterface + UI |
| D14-15 | IFTR 埋點 + 整合測試 + 餐廳實測 + 參數微調 | 全棧 |

---

## 第十二部分：Phase 路線圖

### MVP (15 天)
維持上述排程。DOF 在 MVP 中只做 UI 骨架（反灰按鈕）。

### Phase 1.5 (MVP 上架後 30 天內)
- 拍後 DOF 處理管線（ML Kit Selfie Segmentation + Toolkit.blur）
- 人像 TiltRule + SmileRule
- DOF 按鈕啟用 + 拍後預覽比較 UI
- 橫拍 Level Traction：`OrientationAwareLevel` 四方向自適應
- 多道菜 Toast 建議（偵測到 ≥2 food bbox → 建議 90° 俯拍）

### Phase 2 (有現金流後)
- ML Kit Subject Segmentation 替代 Selfie Segmentation
- 多主體分段模糊（2-3 層距離分段）
- 多種構圖網格切換
- CVPR SPAS 模型評估
- 焦段建議

---

## 第十三部分：技術發現備忘

### 13.1 Pixel 9a Portrait Mode 技術本質

Pixel 的 Portrait Mode 是後置 AI 處理——先拍清晰照片（HDR+），再用神經網路做深度估計 + 人物分割 + 散景渲染。驗證了「拍後 DOF」是業界主流做法。

### 13.2 CameraX 1.5 的 SessionConfig API

CameraX 1.5 引入 `isSessionConfigSupported` API。在「拍後 DOF」方案中不再需要，但未來即時 Bokeh 預覽時可能有用。

---

## 第十四部分：風險矩陣

| 風險 | 嚴重度 | 機率 | 對沖方案 |
|------|--------|------|---------|
| 用戶懶得切 App | 高 | 高 | First-time wow 壓到 10 秒 + IMU 自動場景判斷 |
| Ambient UI 太微妙用戶沒注意到 | 高 | 中 | 3 秒首次教學動畫 + 漸進強化 |
| ML Kit Object Detection 對食物偵測不穩定 | 中 | 中 | 降級為純 IMU 模式（角度+水平+網格）|
| WayShot 推出 Android 版 | 高 | 中-高 | 深做美食+減法留白+遞手機 |
| 鏡頭髒汙偵測誤報 | 低 | 低 | Settings 開關 + 白名單機制 |
| 7 天畢業效應 | 高 | 中 | 免費層=可學會 hook，付費層=不可學會即時診斷 |
| Selfie Segmentation 邊緣粗糙 | 中 | 中 | Mask Gaussian softening (kernel=7) |
| 低階機 DOF 處理 > 2 秒 | 中 | 中 | 進度條 + 處理提示 |
| 拍後 DOF 不支援食物場景 | 中 | 確定 | Phase 2 升級到 Subject Segmentation |
| 散景品質不如 OEM 原生 | 中 | 高 | Phase 2 分段模糊升級 |

---

## 附錄 A：Focus 動畫規格

| 狀態 | 視覺效果 | 顏色 | 持續時間 |
|------|---------|------|---------|
| Focusing | 1.3x→1.0x 縮放 + 60° 弧線旋轉 | 白色 α=0.9 | 至 AF 完成 |
| Focused | 穩定 + 脈動一次 | 綠色 α=0.8 | 1.5s + 0.5s 淡出 |
| Failed | 左右震動 4px（`translateX` 視覺衰減振盪） | 琥珀 α=0.6 | 0.8s + 0.5s 淡出 |
| Locked | 雙圈 + "AE/AF" 小字 | 亮金 α=0.85 | 至解鎖 |

**AE/AF Lock**：正式納入 MVP scope（0.8 天排程）。

---

## 附錄 B：全局決策記錄

### B.1 Logo 拍攝畫面移除

拍攝畫面（CameraScreen）移除 Cueshot Logo。Logo 植入拍後評分卡片 + App Splash Screen。  
理由：拍攝畫面每一個像素都應服務於「引導拍攝」。

### B.2 Haptic 全面移除

MVP 全面移除 Haptic（震動反饋）。替代方案：

| 原 Haptic 場景 | 替代方案 |
|---------------|---------|
| Focus Lost | `translateX` 視覺衰減振盪（±4px） |
| 快門確認 | 外環白色閃爍（§10.3.1） |
| 對齊達成 | 快門外環呼吸韻律啟動 |

理由：Haptic 在不同 Android 裝置上的觸感差異極大，視覺通道在所有裝置上可控。

### B.3 進度環規格

快門冷卻期間外環以圓弧進度環顯示冷卻剩餘時間：

| 屬性 | 值 |
|------|-----|
| 形狀 | 快門外環上的圓弧掃過 |
| 起始角度 | 12 點鐘方向（-90°） |
| 掃過方向 | 順時針 |
| 顏色 | 白色 α0.30 |
| 持續時間 | 與冷卻時長同步 |

Phase 1.5 擴展：進度環動態跟隨 DOF 處理時間。

### B.4 Photo/PTF Pill Toggle

PTF 入口為 Photo/PTF Pill Toggle，取代舊版底部全寬 Tab。  
完整規格見 `Cueshot_Layout_Spec_v2_1.md` §5。

### B.5 Top Bar Scrim

Top Bar 為浮動 Overlay + 線性漸層 Scrim（取代原毛玻璃膠囊設計）。  
Scrim 起始 y=0 alpha 0.22 → 結束 y=120px alpha 0。  
完整規格見 `Cueshot_Layout_Spec_v2_1.md` §3.2。

---

## Viewfinder Overlay 圖層定義

```
Layer 0: Camera Preview（CameraX PreviewView）
Layer 1: 三分網格線（#FFFFFF alpha 0.08, 線寬 0.5px）
Layer 2: Pitch Traction 漸層（邊緣向內 60px, 琥珀金 #D4A030, alpha ≤ 0.20）
Layer 3: Halo Traction 圓環（琥珀金 / 綠色, alpha 0.20 ~ 0.35）
Layer 4: 對焦圓環（Tap-to-focus, 白色 → 綠色）
Layer 5: Level Traction（白色虛線 reference + 動態 dynamic 層）
Layer 6: Scrim + Top Bar 浮動圖示（最上層）
```

---

## 文件元資訊

- **版本**: SSOT v4.0 (Converged Single Source of Truth)
- **建立日期**: 2026-05-12
- **合併來源**:
  - v3.3 SSOT (2026-04-04) — 基底
  - Delta v3.4 (2026-05-01~02) — 11 項決策
  - Delta v3.4.1 Patch (2026-05-03) — 7 項補丁 + 2 項補錄
  - Naming Purge (2026-05-12) — Edge Warmth 全面淨化
- **作廢文件**: v3.0, v3.1, v3.2, v3.3, Delta v3.4, Delta v3.4.1 及所有前版
- **搭配文件**: `Cueshot_Layout_Spec_v2_1.md`（佈局精確定義，獨立維護）
- **命名規範**: 本文件中不存在 "Edge Warmth" 作為渲染元件名稱。三通道體系統一使用 Pitch Traction / Level Traction / Halo Traction。
- **下一步**:
  1. Claude Code 按本文件實作（按 State 1→6 拆票）
  2. 實機測試 → 截圖帶回 Tech Auditor 比對

---

**End of SSOT v4.0**
