# Cueshot 策略與架構 SSOT v4.0.6

**文件版本**: 4.0.6 (Session VIII CSO — D-27: LANDMARK_NONE 降級 + Portrait 狀態機 + Detector Lifecycle)  
**建立日期**: 2026-05-12  
**修訂日期**: 2026-06-12  
**文件性質**: 唯一真理文件 — 本文件作廢所有前版（v3.0~v3.3, Delta v3.4~v3.4.1, v4.0~v4.0.4）  
**作者**: CSO 策略分析 + Ambient UI Tech Auditor (Claude Opus 4.6) + 開發者 Akira  
**合併來源**: v4.0.4 + Session 9 (D-25~D-26, R35~R36: Overlay 總表文案修正 · LEGACY 光線指示語義修復) + v4.0.6 (D-27: LANDMARK_NONE 降級 + Portrait 狀態機 + Detector Lifecycle)  
**搭配文件**: `Cueshot_Layout_Spec_v2_5_1.md`（佈局精確定義）

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

    // Level Traction 漸入（D-10: N=10°）
    const val LEVEL_FADE_RANGE_DEG = 10f         // Level Traction 漸入區間（0°=消失, 10°=完全可見）

    // 場景切換
    const val HYSTERESIS_SCENE_MS = 500L          // 場景切換防跳動
    const val HYSTERESIS_PITCH_LOW_DEG = 70f      // 45°→90° 切換下限
    const val HYSTERESIS_PITCH_HIGH_DEG = 80f     // 45°→90° 切換上限

    // 動畫時序
    const val BREATHE_CYCLE_MS = 2000L            // 呼吸韻律週期
    const val TRANSITION_FAST_MS = 300L           // 標準快速過渡
    const val SHUTTER_FLASH_DURATION_MS = 300L    // 快門閃爍持續時間

    // Focus 動畫（附錄 A）
    const val FOCUS_RING_SIZE_DP = 60f            // 對焦圓環外徑
    const val FOCUS_RING_STROKE_DP = 1.5f         // 對焦圓環邊框寬度
    const val FOCUS_AF_TIMEOUT_MS = 2500L         // D-17: AF 回調超時保護（Sony XQ-BT52 PDAF fine-tune 需額外裕度）
    const val FOCUS_FADEOUT_MS = 500L              // Focused/drift 淡出時間
    const val FOCUS_FAILED_MS = 800L              // Failed 振盪時間
    const val FOCUS_LONG_PRESS_MS = 500L          // AE/AF Lock 長按閾值
    const val FOCUS_DRIFT_THRESHOLD_DEG = 15f     // D-4: IMU drift 觸發 Focused 消失

    // Top Bar 光線指示（D-15/D-16/D-19, §2.9）
    const val LIGHT_DEBOUNCE_MS = 1000L           // D-16: 光線狀態 debounce 門檻
    const val LIGHT_HDR_PULSE_MS = 3000L          // D-19: HDR 圖示 pulse 週期（需 HDR 狀態）
    const val LIGHT_FLASH_PULSE_MS = 3000L        // D-19: ⚡ 圖示 pulse 週期（需補光狀態）
    const val LIGHT_PULSE_ALPHA_MIN = 0.35f       // pulse 最低 alpha
    const val LIGHT_PULSE_ALPHA_MAX = 1.0f        // pulse 最高 alpha
}

// Pitch Traction 指示柱常數（D-2, R2/R17）
object PitchTractionConstants {
    const val COLUMN_WIDTH_DP = 36f               // 指示柱容器寬度
    const val COLUMN_HEIGHT_RATIO = 0.25f         // 高度佔 viewfinder 25%
    const val COLUMN_RIGHT_MARGIN_DP = 16f        // 右側邊距
    const val AXIS_WIDTH_DP = 2f                  // 中軸線寬度
    const val RING_SIZE_DP = 32f                  // 定位環直徑
    const val RING_STROKE_DP = 1.5f               // 定位環線寬
    const val DOT_SIZE_DP = 20f                   // 浮動圓點直徑
    const val MAX_DOT_OFFSET_PX = 80f             // 圓點最大偏移量
    const val MAX_DEVIATION_DEG = 20f             // 偏差正規化基準
    const val COLOR_TRANSITION_RATIO = 0.5f       // 50%（=10°）以內綠色，以上琥珀
}
```

### 2.0.1 Level Traction 漸入退場規格

Level Traction 不是「開/關」二元切換，而是根據偏差程度連續漸入/漸出：

- **偏差 0°** → opacity 0.00（完全消失 — 已對齊，退場）
- **偏差 ≥10°** → opacity 1.00（完全可見 — 最大引導強度）
- **中間線性插值**（偏差 5° → opacity 0.50）

```kotlin
val levelOpacity = (maxDeviation / AmbientConstants.LEVEL_FADE_RANGE_DEG)
    .coerceIn(0f, 1f)

// 繪製跳過優化：alpha 極低時直接跳過整個 Level Canvas
if (levelOpacity < 0.01f) return@Canvas  // 省 4 次 drawXxx
```

其中 `maxDeviation = max(abs(pitchDiff), abs(rollDiff))`。

**設計理由（D-10, N=10°）**：N=5° 時正常手持微顫（±2°）會產生 opacity 0.40 的可見閃爍，違反 Ambient UI「不產生壓力」原則。N=10° 確保手持微顫（≤3°）僅產生 opacity ≤0.30，在 OLED/LCD 上融入背景不干擾。

### 2.1 核心原則

**不告訴用戶「你錯了」，而是讓畫面的視覺環境自然牽引用戶做出正確動作。**

類比：道路上的車道標線（你不需要「閱讀」標線，身體自然知道往哪裡開）。

### 2.2 三通道 Traction 引導體系

| 通道 | 職責 | 感測器 | 刷新率 | 視覺表現 |
|------|------|--------|--------|---------|
| **Pitch Traction** | 俯仰角引導（45°/90° 牽引） | IMU (accelerometer + magnetometer) | 60fps | 右側垂直指示柱：中軸線 + 定位環 + 浮動圓點（D-2）|
| **Level Traction** | 水平修正（Roll 微調） | IMU (accelerometer + magnetometer) | 60fps | 水平儀虛線 + 動態修正指示 |
| **Halo Traction** | 構圖位置牽引（跟隨 ML Kit bbox） | ML Kit (Face/Object Detection) | 10fps + EMA 平滑 | 主體周圍柔和光環，偏離目標時方向加亮弧 |

**三通道獨立運作、獨立降級**：
- Pitch Traction 操作在螢幕右側（垂直指示柱，D-2）
- Level Traction 操作在螢幕中央（虛線水平儀）
- Halo Traction 操作在主體周圍（圓形光環）
- 三者視覺區域不衝突，可同時啟用

**三通道職責邊界（D-11）**：
- **Pitch Traction**：俯仰角引導（1D 垂直軸），三態 `TILT_UP / TILT_DOWN / ALIGNED`
- **Level Traction**：水平偏差引導（1D roll），opacity 漸入/漸出即是引導
- **Halo Traction**：構圖位置引導（2D 平面），連續角度 `atan2(dy, dx)` 方向加亮弧

**Pitch Traction 與 Level Traction 場景互斥（D-9）**：Food 場景啟用 Pitch Traction，關閉 Level Traction；其他場景啟用 Level Traction，關閉 Pitch Traction。無任何場景同時啟用兩者。

**三通道均為 IMU 或 ML Kit 驅動，不存在任何名為 Edge Warmth 的渲染元件。**

### 2.3 Pitch Traction 規格（D-2: 指示柱設計）

**視覺結構**：右側垂直指示柱，由三個元素組成：
- **中軸線**：2dp 寬，全高，引導視線的靜態軌道
- **定位環**：32dp，1.5dp stroke，固定在軸線中點，代表「目標角度」
- **浮動圓點**：20dp，沿中軸線移動，代表「當前 pitch 角度」

**引導語義**：圓點在定位環上方 = 需要更俯（TILT_DOWN）；圓點在定位環下方 = 需要更抬（TILT_UP）；圓點在定位環內 = 已對齊（ALIGNED）。

```kotlin
// presentation/camera/overlay/PitchTractionLayer.kt

enum class PitchGuidance { TILT_UP, TILT_DOWN, ALIGNED }

@Composable
fun PitchTractionLayer(
    currentPitch: Float,
    targetPitch: Float,   // 45f or 90f
    modifier: Modifier = Modifier
) {
    val pitchDiff = targetPitch - currentPitch
    val isAligned = abs(pitchDiff) <= AmbientConstants.ALIGNED_THRESHOLD_DEG

    // 圓點偏移量（線性映射，±80px）
    val rawOffset = (pitchDiff / PitchTractionConstants.MAX_DEVIATION_DEG)
        .coerceIn(-1f, 1f) * PitchTractionConstants.MAX_DOT_OFFSET_PX

    // 吸附動畫：進入對齊區時用 spring 彈性吸附（R17）
    val dotOffset by animateFloatAsState(
        targetValue = if (isAligned) 0f else rawOffset,
        animationSpec = if (isAligned) {
            spring(dampingRatio = 0.7f, stiffness = 300f)  // 彈性「卡入」
        } else {
            tween(150, easing = CubicBezierEasing(0.25f, 1f, 0.5f, 1f))
        }
    )

    // 正規化距離（0~1，用於顏色計算）
    val pitchDist = (abs(pitchDiff) / PitchTractionConstants.MAX_DEVIATION_DEG)
        .coerceIn(0f, 1f)

    // 顏色邏輯（R17）
    val dotColor = when {
        isAligned -> Color(0xFF50C864)  // 綠色
        pitchDist > PitchTractionConstants.COLOR_TRANSITION_RATIO -> Color(0xFFD4A030)  // 琥珀（>10° 偏差）
        else -> Color(0xFF50C864)  // 綠色（≤10° 偏差，接近對齊）
    }
    val axisColor = if (isAligned) Color(0xFF50C864).copy(alpha = 0.4f)
                    else Color(0xFFD4A030).copy(alpha = 0.9f)

    Canvas(modifier = modifier
        .width(PitchTractionConstants.COLUMN_WIDTH_DP.dp)
        .fillMaxHeight(PitchTractionConstants.COLUMN_HEIGHT_RATIO)
        .padding(end = PitchTractionConstants.COLUMN_RIGHT_MARGIN_DP.dp)
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        // 1. 中軸線
        drawLine(
            color = axisColor,
            start = Offset(centerX, 0f),
            end = Offset(centerX, size.height),
            strokeWidth = PitchTractionConstants.AXIS_WIDTH_DP.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 2. 定位環
        drawCircle(
            color = if (isAligned) Color(0xFF50C864).copy(alpha = 0.6f)
                    else Color.Transparent,
            radius = PitchTractionConstants.RING_SIZE_DP.dp.toPx() / 2,
            center = Offset(centerX, centerY),
            style = Stroke(PitchTractionConstants.RING_STROKE_DP.dp.toPx())
        )

        // 3. 浮動圓點
        drawCircle(
            color = dotColor.copy(alpha = 0.1f + pitchDist * 0.7f),
            radius = PitchTractionConstants.DOT_SIZE_DP.dp.toPx() / 2,
            center = Offset(centerX, centerY + dotOffset)
        )
    }
}
```

**Canvas 呼叫**：3 次（`drawLine` + `drawCircle` × 2）

**對齊呼吸**：當 `isAligned == true` 時，整個指示柱進入呼吸韻律（由全局呼吸驅動），中軸線與定位環同步 opacity 0.10→0.18。

**⚠️ Edge Warmth 殘留清除（D-14, R22）**：PitchTractionLayer **禁止**包含 `null -> drawRect(Brush.radialGradient(...))` 或任何全幅暈影渲染邏輯。此代碼為 Edge Warmth 時代 `Direction` enum 的語義殘留（D-11 已將 Direction enum 移交 Halo Traction）。`PitchGuidance` 三態 `{ TILT_UP, TILT_DOWN, ALIGNED }` 不存在 `null` 值。實作中若存在此分支，必須刪除。

**場景映射（D-1, D-9 更新）**：

| 場景 | Traction 通道行為 |
|------|-----------------|
| Food 45° | Pitch Traction 向 45° 牽引 + Halo Traction（Food bbox）|
| Food 90° | Pitch Traction 向 90° 牽引 + Halo Traction（Food bbox）|
| Portrait | Level Traction 水平修正 + Halo Traction（人臉 bbox）|
| Pass-to-Friend | Level Traction 水平修正 + Halo Traction（人臉 bbox）（D-1: 不含 Pitch）|
| Minimalist | Level Traction 水平修正（不做角度引導）|
| General | Level Traction 水平修正 |

**Pitch Traction 與 Level Traction 場景互斥**：Food 場景關閉 Level Traction（D-9），避免 Pitch 指示柱與 Level 虛線同時出現造成視覺混亂。其他場景關閉 Pitch Traction。

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
    val alignedThresholdDeg: Float  // 對齊容許誤差
) {
    companion object {
        val FOOD_45 = PitchTractionConfig(
            targetDeg = 45f,
            alignedThresholdDeg = 5f
        )
        val FOOD_90 = PitchTractionConfig(
            targetDeg = 90f,
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

| 配置 | 目標角度 | 對齊容許 | 啟用條件 |
|------|---------|---------|---------|
| `FOOD_45` | 45° | ±5° | pitch ∈ [35°, 80°) |
| `FOOD_90` | 90° | ±5° | pitch ∈ (70°, 90°] |
| 切換閾值 | — | — | 75°（hysteresis 70°↔80°）|

### 2.4 Level Traction 規格

Level Traction 是原「水平儀」功能的正式名稱，以雙軸 IMU 驅動。

**渲染結構**：
- **reference 層**（靜態）：白色虛線 + 空心圓刻度標記 — 代表「理想水平線」
- **dynamic 層**（動態）：實線 + 實心圓 — 跟隨手機實際 Roll/Pitch，偏移時顯示琥珀金，對齊時變綠

**顏色邏輯**：
- reference 層：固定白色 α0.5
- dynamic 層：琥珀金 `#D4A030`（未對齊）→ 對齊綠 `#50C864`（已對齊）

**Canvas 優化**：reference 層首次繪製後快取為 Bitmap，後續幀直接 `drawImage`（4 次 drawXxx → 1 次）。

### 2.4.1 IMU 角度計算骨架（R11, R12）

**Pitch 座標定義（R12）**：手機直立時 pitch = 0°，手機水平朝下（俯拍）時 pitch = 90°。

```kotlin
// data/sensor/OrientationCalculator.kt

data class DeviceOrientation(
    val pitchDeg: Float,   // 0°=直立, 90°=水平俯拍
    val rollDeg: Float     // 0°=水平, 正值=順時針傾斜
)

class OrientationCalculator {
    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    /**
     * 使用 TYPE_ROTATION_VECTOR（quaternion 融合感測器）
     * 避免 Euler 角在 pitch=90° 時的 gimbal lock
     */
    fun calculate(rotationVector: FloatArray): DeviceOrientation {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)
        SensorManager.getOrientation(rotationMatrix, orientation)

        // orientation[1] = pitch（弧度，直立=0, 俯=負）
        // orientation[2] = roll（弧度）
        val pitchDeg = Math.toDegrees(-orientation[1].toDouble()).toFloat()
            .coerceIn(0f, 90f)
        val rollDeg = Math.toDegrees(orientation[2].toDouble()).toFloat()

        return DeviceOrientation(pitchDeg, rollDeg)
    }
}
```

**使用場景**：
- **Pitch Traction**（Food 場景）：使用 `pitchDeg` 與目標角度比較
- **Level Traction**（非 Food 場景）：使用 `rollDeg` 計算水平偏差
- **場景引擎**（§3.1）：使用 `pitchDeg` 判斷 Food / Portrait / General 場景切換

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
    // D-3: 對齊時呼吸韻律 α 0.10→0.18，未對齊時靜態 α 0.25
    val haloAlpha by animateFloatAsState(
        targetValue = if (isAligned) 0.18f else 0.25f,
        animationSpec = if (isAligned) {
            infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(300)
        }
    )

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
            color = color.copy(alpha = haloAlpha),
            radius = haloRadius,
            center = Offset(haloCx, haloCy),
            style = Stroke(width = 2f)
        )

        if (!isAligned) {
            // 不對齊 → 向目標側加亮弧（D-11: 方向引導由 Halo 承擔）
            // 使用 angle = atan2(dy, dx) 連續角度繪製方向加亮弧
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

- Pitch Traction：指示柱整體進入綠色呼吸（中軸線 + 定位環 + 圓點同步）
- Halo Traction：從金色變綠色，進入呼吸
- 快門按鈕外環：同步呼吸
- 含義：「一切就緒，隨時可以按快門」

### 2.7 首次使用教學動畫

3 秒連續動畫循環一次（不是文字教學頁）：
1. 手機圖示出現 → 右側指示柱亮起（圓點偏移）
2. 手機跟著指示方向旋轉 → 圓點滑入定位環
3. 指示柱變綠 + 全局呼吸韻律啟動 → 快門按鈕呼吸
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

### 2.9 Top Bar 光線指示（D-15/D-16/D-19, R23）

Top Bar 浮動圖示中的 ⚡（閃光燈）與 HDR 圖示承擔光線狀態的 Ambient UI 視覺指示。以 pulse 動畫取代文字告警，對齊 Ambient UI「視覺引導、不干預閱讀」哲學。

**三態映射（D-15: Option 2）**：

| 光線狀態 | 觸發條件 | ⚡ 圖示 | HDR 圖示 | 設計理由 |
|---------|---------|---------|---------|---------|
| **正常** | histogram 正常 | 靜態 #FFFFFF α0.85 | 靜態 #FFFFFF α0.85 | 不干預 |
| **需要 HDR** | `isBacklit` ∥ `isHighContrast` ∥ `isOverexposed` | 靜態（不變） | amber pulse #D4A030 | 逆光/高對比/過曝 → 建議開 HDR |
| **需要補光** | `isLowLight` | amber slow pulse #D4A030 | amber sync pulse（與 ⚡ 同週期） | 低光 → 建議開閃光燈 + HDR |

**Pulse 動畫規格**：

| 參數 | 需要 HDR | 需要補光 |
|------|---------|---------|
| HDR pulse 週期 | **3000ms** | **3000ms**（與 ⚡ 同步） |
| ⚡ pulse 週期 | — | **3000ms** |
| Alpha 範圍 | 0.35 → 1.0 → 0.35 | 0.35 → 1.0 → 0.35 |
| Easing | `FastOutSlowInEasing` | `FastOutSlowInEasing` |
| 顏色 | #D4A030（琥珀金） | #D4A030（琥珀金） |
| glow ring | α 0→0.14→0（icon 背後微暈） | α 0→0.14→0 |

**Debounce（D-16: 1000ms）**：

光線狀態來自 Frame 2/4 直方圖分析（§10.1），`confidenceFactor = 0.70`（§4.2 最低值）。為避免相機移動時圖示頻繁閃動：

```kotlin
// LightStateDebouncer
private var pendingState: LightState? = null
private var stateStartMs: Long = 0L

fun onNewLightState(state: LightState) {
    if (state == pendingState) {
        if (SystemClock.elapsedRealtime() - stateStartMs >= LIGHT_DEBOUNCE_MS) {
            commitLightState(state)  // 持續 ≥1000ms → 觸發 pulse
        }
    } else {
        pendingState = state
        stateStartMs = SystemClock.elapsedRealtime()
    }
}
```

**退出策略**：histogram 回到正常後，相同 debounce（1000ms 持續正常）→ pulse 停止，圖示恢復靜態 α0.85。

**Compose 實作骨架**：

```kotlin
@Composable
fun HdrIconWithPulse(lightState: LightState) {
    val needsHdr = lightState == LightState.BACKLIT || lightState == LightState.HIGH_CONTRAST
    val needsFlash = lightState == LightState.LOW_LIGHT

    val hdrAlpha by if (needsHdr || needsFlash) {
        val transition = rememberInfiniteTransition()
        transition.animateFloat(
            initialValue = LIGHT_PULSE_ALPHA_MIN,
            targetValue = LIGHT_PULSE_ALPHA_MAX,
            animationSpec = infiniteRepeatable(
                tween(LIGHT_HDR_PULSE_MS.toInt(), easing = FastOutSlowInEasing),
                RepeatMode.Reverse
            )
        )
    } else {
        remember { mutableFloatStateOf(0.85f) }
    }

    Text(
        text = "HDR",
        color = if (needsHdr || needsFlash) Color(0xFFD4A030).copy(alpha = hdrAlpha)
                else Color.White.copy(alpha = 0.85f),
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold
    )
}
```

**Canvas 呼叫增量：0 次** — Top Bar 圖示為 Composable，不在 Canvas drawXxx pipeline 中。pulse 動畫使用 `InfiniteTransition.animateFloat`，不增加任何 Canvas 呼叫。

**三級裝置降級**：

| 裝置等級 | HDR/⚡ pulse | 備註 |
|---------|-------------|------|
| FULL | pulse 動畫（3000ms） | 完整 |
| LIMITED | pulse 動畫（3000ms） | Composable 動畫成本可忽略 |
| LEGACY | **不顯示光線狀態**（靜態白色 α0.85，與正常態一致） | LEGACY 已關閉光線分析（§10.3），無數據源則不渲染狀態色（D-26） |

**LightRule 文字映射（Paid 功能，拍後教學卡片用）**：

| 光線狀態 | 診斷文字 | 付費層 |
|---------|---------|--------|
| `isBacklit` | "逆光場景 — 建議使用 HDR" | Paid |
| `isOverexposed` | "過曝風險 — 降低曝光補償" | Paid |
| `isLowLight` | "光線不足 — 建議開啟閃光燈" | Paid |
| `isHighContrast` | "高對比場景 — 建議使用 HDR" | Paid |
| else | "曝光正常" | Free（但在拍後卡片中顯示） |

> 免費版用戶看到 pulse 動畫（所有人可見），但拍後教學卡片中的文字診斷 `blur(3dp)` — 形成「HDR 在閃，怎麼回事？」→ 付費解鎖完整診斷的轉換漏斗。

---

## 第三部分：場景判斷引擎

### 3.1 場景切換邏輯

**Pitch 座標定義（R12）**：手機直立時 pitch = 0°，手機水平朝下（俯拍）時 pitch = 90°。負值 = 手機向上仰（拍天花板）。參見 §2.4.1 `OrientationCalculator`。

```
if user_selected == PASS_TO_FRIEND:
    → PassToFriendMode (手動觸發，最高優先)
elif pitch > 35°:
    → FoodMode (IMU 自動觸發)
    → 啟用 Pitch Traction + ML Kit Object Detection (FOOD category)
    → 關閉 Level Traction（D-9）
elif pitch ∈ [-10°, 35°] AND face_count >= 1:
    → PortraitMode (ML Kit Face Detection)
    → 啟用 Level Traction + Halo Traction
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
| LevelRule | IMU roll (+ dual-axis at 90°) | < 2ms | Free | D-9: 僅供評分，Level Traction UI 關閉 |
| FoodCompositionRule | ML Kit Object Detection bbox | < 80ms | Paid | Halo 跟隨最大 bbox；EMA 0.8/0.2（見 §2.5.1）|
| LightRule | 直方圖平均亮度 | < 10ms | Paid | — |
| MinimalistRule | bbox ratio + background CV | < 10ms | Paid | — |

### 4.4 人像模式規則

| 規則 | ML Kit 數據源 | 計算 | MVP 階段 |
|------|-------------|------|---------|
| ThirdsRule | face.boundingBox.center | 人臉中心 vs 四個黃金交叉點 | MVP (P0) |
| HeadroomRule | face.boundingBox.top | 比值 10%-20% 為佳 | MVP (P0) |
| EyeLineRule | landmarks[LEFT_EYE].y | 眼睛 Y vs 上三分線 | **Phase 1.5**（D-27: LANDMARK_NONE → landmark 不可用） |
| GazeRule | face.headEulerAngleY | 臉轉左→左側需更多空間 | MVP (P1) |
| TiltRule | face.headEulerAngleZ | 絕對值 > 8° 提示轉正 | Phase 1.5 |
| SmileRule | face.smilingProbability | > 0.4 持續 > 300ms = 黃金時刻 | Phase 1.5 |

**Face Detection 配置（D-23, D-27 修正）**：

| 配置項 | 值 | 理由 |
|--------|-----|------|
| LANDMARK_MODE | **NONE（D-27 降級）** | v2.12.0 crash 未根治 + 從未成功部署。EyeLineRule + NEAR_EYE AF 延後至 Phase 1.5（待 ML Kit 穩定性驗證） |
| CLASSIFICATION_MODE | ALL | Ghost Face eye-open gate（HOTFIX-47）+ SmileRule（Phase 1.5） |
| CONTOUR_MODE | NONE | Google 官方建議：效能互斥 |
| PERFORMANCE_MODE | FAST | 正面拍攝場景足夠；PTF 側臉場景 Phase 1.5 評估切 ACCURATE |
| minFaceSize | 0.15f | 佔畫面 15% 的最小人臉 |
| enableTracking | true | 跨幀 trackingId 穩定 |

#### 4.4.1 Portrait Guidance 狀態機（D-27）

人像模式的引導系統遵循六態狀態機。此狀態機定義**產品行為**（用戶可觀測的 UI 回饋），具體轉換條件與時間參數由 Contract 層定義。

| 狀態 | 產品語義 | 用戶可觀測行為 |
|------|---------|---------------|
| NO_FACE | 畫面中無人臉 | Halo 關閉、Level 低 alpha、無引導文字 |
| FACES_DETECTED_NONE_ELIGIBLE | 偵測到人臉但無合格主體（閉眼/遮擋/極端側臉/ghost face） | Halo 關閉或極暗、Level 開啟、無引導文字 |
| SUBJECT_STABILIZING | 合格主體出現，防抖穩定中 | Halo 暗琥珀、Level 開啟、Grid 開啟、無引導文字 |
| GUIDANCE_ACTIVE | 主體鎖定，有可改善項 | Halo 琥珀 α0.25、Level 開啟、Grid 開啟、顯示最高優先規則文字 |
| ALIGNED | 主體鎖定，所有規則滿足 | Halo 綠色呼吸 0.10→0.18、Level 綠色、Grid 淡出或維持、隱藏引導文字 |
| SUPPRESSED | 引導暫時抑制（防閃爍/對焦優先） | Halo 淡出、維持當前 Level/Grid、無引導文字 |

**規則**：

- 從 NO_FACE 到 GUIDANCE_ACTIVE 的轉換必須經過 SUBJECT_STABILIZING 防抖——禁止跳過
- ALIGNED 綠色呼吸週期 = `AmbientConstants.BREATHE_CYCLE_MS`（2000ms）
- SUPPRESSED 由 AfCoordinator manual tap 或 flicker guard 觸發，自動 AF 恢復時退出
- 任何狀態的 face 全部丟失超過 timeout → 回到 NO_FACE

#### 4.4.2 Face Detector Session Lifecycle（D-27）

ML Kit Face Detector 為 stateful resource，需遵循以下 lifecycle 約束：

| 事件 | 行為 | 理由 |
|------|------|------|
| Camera Session 開啟（Preview active） | 建立 FaceDetector instance（D-23/D-27 配置） | 與 Preview 生命週期綁定 |
| Camera Session 暫停（App background / Activity stop） | **close() Detector + 釋放 native 記憶體** | 防止 native memory leak（ML Kit 佔用 40-60MB） |
| Camera Session 恢復 | 重新建立 Detector instance | 不可復用已 close 的 instance |
| 切換偵測模式（Food ↔ Portrait，D-22 互斥） | 銷毀舊 Detector → 建立新配置 Detector | D-22 互斥原則 — 禁止雙 Detector 並存 |

**Hard Constraint**：禁止全局 singleton FaceDetector——Detector 生命週期必須 ≤ Camera Session 生命週期。

### 4.5 遞手機模式規則

簡化版，保留三條核心規則 + Level Traction：

- **FramingRule**（居中）— 朋友幫拍最容易失誤：主體沒居中
- **LevelRule**（粗檢）— 朋友幫拍第二大失誤：手機歪斜
- **AngleRule**（粗檢）— 可犧牲：PTF 是社交場景，朋友不會為角度死磕

UI 使用 Halo Traction + Level Traction（D-1: 不含 Pitch Traction）。對齊後 Halo 金色→綠色 + 呼吸。

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
    // MVP 策略 A: 單一最大 bbox（D-21）. Phase 1.5 再啟用 enableMultipleObjects()
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
| Ambient UI（Pitch Traction 指示柱 + 呼吸韻律） | 體驗差異化 |
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
| ML Kit Face 或 Object Detection（互斥，D-22）| < 80ms | 30-60ms |
| 直方圖分析 | < 10ms | ~5ms |
| 場景判斷 | < 1ms | ~0.1ms |
| 規則引擎（含 MinimalistRule） | < 5ms | ~2ms |
| 鏡頭髒汙偵測 | < 15ms | ~12ms |
| 優先級排序 | < 1ms | ~0.1ms |
| UI 渲染 (Compose) | < 16ms | ~8ms |
| **端到端（分幀後峰值）** | **< 100ms** | **~65ms** |

記憶體預算：< 150MB（含 ML Kit 模型）  
電池增耗：< 15%

**ML Kit 偵測器互斥運行原則（D-22）**：
- 同一 ImageProxy 上僅運行**一個** ML Kit 偵測器（Face Detection 或 Object Detection），不可同時開啟兩個
- 切換模式時先 `detector.close()` 再初始化新偵測器，由 500ms hysteresis（§3.2）吸收
- Google 官方約束引用：「在即時應用程式中使用串流模式時，請勿使用多個物件偵測功能，因為大多數裝置無法產生足夠的影格速率」
- ODT STREAM_MODE cold start：首 30 幀（FULL tier ≈ 3s / LIMITED ≈ 3.75s / LEGACY ≈ 6s）Halo Traction 不可用，以 Grid 網格（α0.35）+ Pitch Traction 過渡（見場景映射「Food 無 bbox」）。首個 Food bbox 到達後 300ms crossfade 至 Halo。
- Thermal throttle 響應：Android PowerManager thermal status ≥ MODERATE 時降頻至 LEGACY tier 分析率（5fps）；≥ SEVERE 時暫停 ML Kit 推理 5 秒冷卻

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
| State 5 | Focus | 10 | ✅ 預算內 | 方案 B 單圈 + Badge（見附錄 A.5） |
| State 6 | Food | **9** | ✅ 預算內 | D-9: 移除 Level Traction，僅 Pitch 指示柱 + Food Halo |

**State 2 優化策略**：Level reference 線（虛線 + 刻度標記）首次繪製後快取為 Bitmap，後續幀直接 `drawImage`（4→1 次）。記憶體 ~50KB。

**State 6 明細（D-9 後）**：三分網格 4 + Pitch 指示柱 3 + Food Halo 1 + Scrim 1 = **9 次**。

---

## 第十一部分：MVP Sprint 排程（15 天）

| 天數 | 交付 | 技術棧 |
|------|------|--------|
| D1-2 | 角度引導 + Pitch Traction 指示柱 + 場景自動切換 | IMU + Compose Canvas |
| D3-4 | Level Traction（水平儀）+ 三分法網格 | IMU + Compose |
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

## 附錄 A：Focus 動畫規格（完整版）

### A.0 概述

Tap-to-Focus 為 Layer 4 對焦圓環，提供用戶手動對焦的視覺反饋。  
四個狀態形成完整生命週期：**Focusing → Focused → (auto-fadeout)** 或 **Focusing → Failed → (auto-remove)**。  
Long press 觸發 **Locked (AE/AF Lock)**，獨立於上述生命週期。

### A.1 共用幾何常數

| 常數 | 值 | 說明 |
|------|-----|------|
| `FOCUS_RING_SIZE` | **60dp** | 圓環外徑（與快門 72dp 形成階層） |
| `FOCUS_RING_STROKE` | **1.5dp** | 邊框寬度（輕於 Halo Traction 2dp） |
| `FOCUS_RING_Z` | Layer 4 | 在 Halo Traction (Layer 3) 之上、Level Traction (Layer 5) 之下 |

### A.2 四狀態精確規格

#### A.2.1 Focusing（正在對焦）

| 參數 | 值 | 備註 |
|------|-----|------|
| 觸發 | 用戶點擊 Viewfinder 任意位置 | `pointerInput { detectTapGestures(onTap=...) }` |
| 圓環顏色 | **白色 #FFFFFF α=0.9** | — |
| 入場動畫 | **1.3x→1.0x 收縮 + 60° 弧線旋轉** | 兩個動畫同步執行 |
| 收縮時長 | **300ms** | `FastOutSlowInEasing` |
| 弧旋時長 | **300ms** | `LinearOutSlowInEasing`（尾端減速停穩） |
| 結束條件 | CameraX AF 回調成功 → 進入 Focused | — |
| 超時保護 | **2500ms** 無回調 → 強制進入 Failed | D-17: Sony PDAF fine-tune 裕度 |

```kotlin
// Focusing 入場動畫
val focusingScale = remember { Animatable(1.3f) }
val focusingRotation = remember { Animatable(0f) }

LaunchedEffect(focusState == FocusState.FOCUSING) {
    launch { focusingScale.animateTo(1.0f, tween(300, easing = FastOutSlowInEasing)) }
    launch { focusingRotation.animateTo(60f, tween(300, easing = LinearOutSlowInEasing)) }
}

// Canvas 繪製
drawCircle(
    color = Color.White.copy(alpha = 0.9f),
    radius = FOCUS_RING_SIZE.toPx() / 2,
    center = focusCenter,
    style = Stroke(FOCUS_RING_STROKE.toPx()),
    // scale + rotate applied via graphicsLayer
)
```

**Canvas 呼叫**：1 次 `drawCircle`（scale/rotate 由 `graphicsLayer` 處理，不增加 draw call）

---

#### A.2.2 Focused（對焦成功）— D-5r: 永駐 + 雙路徑消失

| 參數 | 值 | 備註 |
|------|-----|------|
| 觸發 | CameraX AF 回調成功 | `FocusMeteringResult.isFocusSuccessful` |
| 圓環顏色 | **綠色 #50C864 α=0.8** | 從白色 → 綠色，300ms `animateColorAsState` |
| 穩定 scale | **0.85x** | 從 Focusing 1.0x 收縮至 0.85x，暗示「鎖定」 |
| 脈動確認 | **0.85→0.92→0.85**，200ms | AF 完成瞬間觸發一次，`FastOutSlowInEasing` |
| 持續時間 | **永駐**（D-5r） | 無 timeout — 直到消失路徑觸發 |
| 消失路徑 | **雙路徑**（見 §A.2.2.1） | ① IMU drift ≥15° ② 用戶 re-tap |

```kotlin
// Focused 入場（脈動確認後永駐）
LaunchedEffect(focusState == FocusState.FOCUSED) {
    // T0: 脈動確認
    focusScale.animateTo(0.92f, tween(100, easing = FastOutSlowInEasing))
    focusScale.animateTo(0.85f, tween(100, easing = FastOutSlowInEasing))
    // 脈動後靜止 — 無 delay + fadeout，永駐直到外力消除
}
```

**Canvas 呼叫**：1 次 `drawCircle`

#### A.2.2.1 Focused 消失路徑（R7）

Focused 圓環只有兩條消失路徑：

| 路徑 | 觸發條件 | 動畫 | 備註 |
|------|---------|------|------|
| ① IMU Drift | 手機姿態偏移 ≥ 15°（`FOCUS_DRIFT_THRESHOLD_DEG`） | α 0.8→0, 500ms fadeout | 重新拿起手機時自然觸發 |
| ② Re-tap | 用戶點擊其他位置 | 觸發 Failed 動畫 → 新 Focusing | 見 §A.4 覆蓋規則 |

```kotlin
// Drift 監聽（在 Focused 狀態下持續運行）
LaunchedEffect(focusState == FocusState.FOCUSED) {
    val initialOrientation = orientationCalculator.current()
    orientationCalculator.orientationFlow.collect { current ->
        val drift = abs(current.pitchDeg - initialOrientation.pitchDeg) +
                    abs(current.rollDeg - initialOrientation.rollDeg)
        if (drift >= AmbientConstants.FOCUS_DRIFT_THRESHOLD_DEG) {
            // 觸發淡出
            focusAlpha.animateTo(0f, tween(500, easing = LinearEasing))
            onFocusRingDismissed()
        }
    }
}
```

**設計理由（D-5r）**：刪除 timeout 後，IMU drift 機制成為天然的 timeout 替代品 — 用戶放下手機再拿起時，drift 幾乎必然 >15°，自動觸發消失。比固定 8s timeout 更符合用戶行為模式。

---

#### A.2.3 Failed（對焦失敗 / 追蹤遺失）

| 參數 | 值 | 備註 |
|------|-----|------|
| 觸發 | CameraX AF 回調失敗 **或** 2500ms 超時 | — |
| 圓環顏色 | **琥珀 #D4A030 α=0.6** | 瞬間變色（不漸變，與 Focused 的漸變形成對比） |
| 動畫 | **衰減振盪 + scale 膨脹 + opacity 衰減** | 三者同步，800ms |
| 振盪路徑 | `translateX`: 0→-4→+4→-3→+2→-1→0 px | 振幅逐幀遞減 |
| scale 膨脹 | **0.85→1.20** | 模擬「失焦擴散」感 |
| opacity 衰減 | **0.6→0.0** | 與振盪同步衰減 |
| 總時長 | **800ms** | 結束時 opacity=0，無需額外淡出 |

```kotlin
// Failed 衰減振盪 keyframes
LaunchedEffect(focusState == FocusState.FAILED) {
    launch {
        focusScale.animateTo(1.20f, tween(800, easing = LinearOutSlowInEasing))
    }
    launch {
        focusAlpha.animateTo(0f, tween(800, easing = FastOutLinearInEasing))
    }
    launch {
        focusOffsetX.animateTo(0f, keyframes {
            durationMillis = 800
            0f at 0                        // 起始
            (-4f) at 100                   // 第一次偏移
            4f at 200                      // 反彈
            (-3f) at 300                   // 衰減
            2f at 400                      // 衰減
            (-1f) at 500                   // 接近靜止
            0f at 600                      // 停止震動
            0f at 800                      // 保持
        })
    }
    delay(800)
    onFocusRingDismissed()
}
```

**Canvas 呼叫**：1 次 `drawCircle`（translate/scale 由 `graphicsLayer`）

---

#### A.2.4 Locked（AE/AF Lock）— 方案 B：單圈 + 🔒 Badge（D-6r/D-7 簡化）

| 參數 | 值 | 備註 |
|------|-----|------|
| 觸發 | **Long press ≥ 500ms** | 對齊 Android `ViewConfiguration.getLongPressTimeout()` |
| 圓環顏色 | **亮金 #D4A030 α=0.85** | — |
| scale | **0.85x**（與 Focused 同） | 從 Focusing 1.0x 收縮，表示「鎖定」 |
| 🔒 Badge | 右上角 offset **(-4dp, -4dp)** | 背景 #0D0D0D α0.85 + 1px 琥珀金邊框 |
| Badge 入場 | **scale 0.5→1.0, opacity 0→1, 300ms** | `cubic-bezier(0.175, 0.885, 0.32, 1.275)` overshoot |
| Badge 圖示 | Lucide `lock` icon, **10dp** | 琥珀金色 |
| 持續時間 | **永駐至解鎖**（D-6r: 無 timeout） | 唯一消失路徑 = 用戶 re-tap |
| IMU drift | **不受影響**（D-7） | Locked 是用戶明確意圖，不被環境感測器覆蓋 |
| 解鎖行為 | 當前 Locked 圈觸發 Failed 動畫 → 新位置產生新 Focusing 圈 | — |
| CameraX 整合 | `camera.cameraControl.startFocusAndMetering(action)` 帶 `FLAG_AF or FLAG_AE` | 鎖定曝光+對焦 |

**方案 B 選擇理由**：
- 效能優先：Canvas 呼叫 1 次 `drawCircle` + 1 個 Composable Icon = **2 次**（vs 雙圈方案 3 次）
- 視覺干擾最小：單圈不增加 Viewfinder 視覺重量
- 國際化：🔒 圖示無需翻譯，比 "AE/AF" 文字更通用

```kotlin
// Locked 狀態 Compose 結構
Box {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Color(0xFFD4A030).copy(alpha = 0.85f),
            radius = FOCUS_RING_SIZE.toPx() / 2 * 0.85f,
            center = focusCenter,
            style = Stroke(FOCUS_RING_STROKE.toPx())
        )
    }
    // Badge 使用 Composable 而非 Canvas drawText
    AnimatedVisibility(
        visible = focusState == FocusState.LOCKED,
        enter = scaleIn(
            initialScale = 0.5f,
            animationSpec = tween(300, easing = CubicBezierEasing(0.175f, 0.885f, 0.32f, 1.275f))
        ) + fadeIn(tween(300))
    ) {
        LockBadge(modifier = Modifier.offset(x = (-4).dp, y = (-4).dp))
    }
}
```

**Canvas 呼叫**：1 次 `drawCircle` + 1 次 Composable（不計入 Canvas budget）= **1 次**

---

### A.3 狀態轉換矩陣（D-5r/D-6r 簡化版）

```
                    ┌───────────┐
          Tap       │           │    AF Success
    ─────────────►  │ Focusing  │ ─────────────►  Focused ──(永駐)
                    │           │                     │
                    └─────┬─────┘                     ├── IMU drift ≥15° → fadeout 500ms → [移除]
                          │                           └── Re-tap → Failed + 新 Focusing
                    AF Fail / 2.5s timeout
                          │
                          ▼
                       Failed ──(0.8s)──► [移除]

                    ┌───────────┐
     Long press     │           │
    ─────────────►  │  Locked   │ ──(永駐, 不受 IMU drift)
     (≥500ms)       │  + 🔒    │
                    └─────┬─────┘
                          │
                     Re-tap → Failed + 新 Focusing
```

### A.4 再次點擊行為（覆蓋規則）

| 當前狀態 | 用戶行為 | 結果 |
|---------|---------|------|
| Focusing | 點擊其他位置 | 當前圈觸發 Failed → 新位置產生 Focusing |
| Focused | 點擊其他位置 | 當前圈觸發 Failed → 新位置產生 Focusing |
| Failed | 點擊任意位置 | （圈已在消失中）新位置產生 Focusing |
| Locked | 點擊任意位置 | 當前 Locked 觸發 Failed → 新位置產生 Focusing |
| 無圈 | 點擊任意位置 | 新位置產生 Focusing |

**同時只存在一個活躍 Focus Ring**（Failed 動畫中的舊圈不計，它正在消失）。

### A.5 Canvas 預算更新（State 5）

| 元素 | 呼叫數 |
|------|-------|
| 三分網格 (Layer 1) | 4 |
| Focus Ring (Layer 4) — 最大值 Locked | **1** |
| Level Traction reference (Layer 5) | 1 (bitmap) |
| Level Traction dynamic (Layer 5) | 3 |
| Scrim (Layer 6) | 1 |
| **State 5 總計** | **10** |

方案 B 將 State 5 從原預算 11 降至 **10**，低於 15 次上限。

### A.6 CameraX 整合延遲鏈

```
T0: 用戶手指觸碰螢幕
T0+1ms: pointerInput 回調 → 建立 Focus Ring → 開始 Focusing 動畫
T0+16ms: 第一幀渲染（用戶看到圓環出現）
T0+16ms: startFocusAndMetering() 發送至 CameraX
T0+200~800ms: AF 完成回調 → 觸發 Focused 或 Failed
T0+2200~3000ms: Focus Ring 完全消失
```

**E2E 視覺反饋延遲**：16ms（1 frame）  
**E2E AF 完成延遲**：200-800ms（CameraX 硬體依賴，不可控）

### A.7 三級裝置降級

| 裝置等級 | Focusing 動畫 | Focused 脈動 | Failed 振盪 | Locked Badge |
|---------|-------------|------------|------------|-------------|
| FULL | 1.3x→1.0x + 60° 弧旋 | 0.85→0.92→0.85 | 衰減振盪 + 膨脹 | 🔒 Badge |
| LIMITED | 1.3x→1.0x（**無弧旋**） | 0.85→0.92→0.85 | 衰減振盪 + 膨脹 | 🔒 Badge |
| LEGACY | **無動畫，直接 1.0x 出現** | **無脈動** | **僅 fadeout 0.8s** | 🔒 Badge（靜態出現） |

---

## 附錄 B：全局決策記錄

### B.1 Logo 拍攝畫面移除

拍攝畫面（CameraScreen）移除 Cueshot Logo。Logo 植入拍後評分卡片 + App Splash Screen。  
理由：拍攝畫面每一個像素都應服務於「引導拍攝」。

### B.2 Haptic 全面移除

MVP 全面移除 Haptic（震動反饋）。替代方案：

| 原 Haptic 場景 | 替代方案 |
|---------------|---------|
| Focus Lost | `translateX` 衰減振盪 + scale 膨脹（±4px → 0.85→1.20 — 見附錄 A.2.3） |
| 快門確認 | 外環白色閃爍（§10.3.1） |
| 對齊達成 | 快門外環呼吸韻律啟動 |

理由：Haptic 在不同 Android 裝置上的觸感差異極大，視覺通道在所有裝置上可控。

### B.3 進度環規格

快門按下瞬間及冷卻期間，外環以圓弧進度環提供視覺回饋：

**B.3.1 拍攝確認環（Capture Confirmation）**

| 屬性 | 值 |
|------|-----|
| 形狀 | 快門外環上的圓弧掃過 |
| 起始角度 | 12 點鐘方向（-90°） |
| 掃過方向 | 順時針 |
| 顏色 | **綠色 #50C864 α0.80** |
| 持續時間 | **300ms**（0%→100% 掃滿） |
| 觸發 | 快門按下瞬間 |
| 結束 | 掃滿後 50ms 重置為 0% |

**B.3.2 冷卻進度環（Cooldown Progress）**（Phase 1.5）

| 屬性 | 值 |
|------|-----|
| 形狀 | 快門外環上的圓弧掃過 |
| 起始角度 | 12 點鐘方向（-90°） |
| 掃過方向 | 順時針 |
| 顏色 | 白色 α0.30 |
| 持續時間 | 與冷卻時長同步 |

Phase 1.5 擴展：進度環動態跟隨 DOF 處理時間。

**B.3.3 快門色彩硬編碼聲明（R15）**

快門按鈕（外環 + 內圓 + 進度環）**不使用 Material Theme 預設色彩**。所有色值必須硬編碼：

| 元素 | 色值 | 備註 |
|------|------|------|
| 外環靜態 | #FFFFFF α0.30 | — |
| 外環呼吸（對齊時） | #50C864 α 0.10→0.18 | 與全局呼吸同步 |
| 外環閃爍（拍攝時） | #FFFFFF α0.45→0 | 300ms |
| 進度環 | #50C864 α0.80 | 拍攝確認 |
| 內圓 | #FFFFFF | — |

**理由**：Claude Code 在實作中使用了 `MaterialTheme.colorScheme.primary`，導致快門顏色跟隨 Sony（藍色）/ Pixel（粉色）等廠商主題色，破壞 Cueshot 品牌一致性。

### B.4 Photo/PTF Pill Toggle

PTF 入口為 Photo/PTF Pill Toggle，取代舊版底部全寬 Tab。  
完整規格見 `Cueshot_Layout_Spec_v2_5_1.md` §5。

### B.5 Top Bar Scrim + 光線指示（D-13/D-15/D-16/D-19）

Top Bar 為浮動 Overlay + 線性漸層 Scrim（取代原毛玻璃膠囊設計）。  
Scrim 起始 y=0 alpha 0.22 → 結束 y=**120px** alpha 0。（D-13: CSO 確認 120px，待實測調整）  
完整 Scrim 規格見 `Cueshot_Layout_Spec_v2_5_1.md` §3.2。

**Top Bar 圖示光線指示動畫**：三態映射（正常/需HDR/需補光），pulse 週期 3000ms，debounce 1000ms。完整規格見 §2.9。

### B.6 PreviewView scaleType（D-18: FILL_CENTER）

CameraX `PreviewView` 使用 `ScaleType.FILL_CENTER`（裁切填滿，無 Letterbox 黑條）。

| 屬性 | 值 | 備註 |
|------|-----|------|
| scaleType | `PreviewView.ScaleType.FILL_CENTER` | Layout Spec v2.5 §3.1 規格 |
| 裁切行為 | 4:3 影像在 9:19.5 容器中上下各裁切約 8.7% | 主體通常在中央，裁切影響可忽略 |
| **禁止** | `FIT_CENTER` | 會產生 Letterbox 黑條，與 Scrim/Overlay 疊加造成視覺異常 |

**設計理由**：  
1. 所有主流相機 App（Google Camera, Samsung Camera, iPhone）均使用 FILL 策略  
2. Cueshot 為導拍引擎（§1.3），非專業相機替代品 — 用戶不精確計較邊緣裁切  
3. ML Kit Face/Object bbox 使用 ImageProxy 座標，不受 PreviewView scaleType 影響

### B.7 Edge Warmth 語義殘留清除（D-14）

PitchTractionLayer 在命名淨化（2026-05-12）後仍殘留 Edge Warmth 行為代碼：`null -> drawRect(Brush.radialGradient(WarmAmber))`。  
此代碼引用已移交 Halo Traction 的 `Direction` enum（D-11），且 `PitchGuidance` 三態不含 `null` 值。

**處理**：實作中刪除 `PitchTractionLayer.kt` 的 `null ->` 分支。無替代邏輯。  
**教訓**：命名淨化須同步執行**語義淨化** — 檢查每個 when/switch 分支行為是否仍屬於新架構職責。

### B.8 Edge-to-Edge + Display Cutout（D-18 附帶修復）

啟用 edge-to-edge 時，必須同步設定 cutout mode 以避免 Viewfinder 被系統 padding 推下：

```kotlin
// MainActivity.kt
enableEdgeToEdge()
window.attributes.layoutInDisplayCutoutMode =
    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
```

```xml
<!-- themes.xml -->
<item name="android:windowLayoutInDisplayCutoutMode">shortEdges</item>
```

**Scaffold padding 修正**：移除 `Column.padding(paddingValues)` — Viewfinder 從 y=0 開始。Top Bar 使用 `WindowInsets.statusBars` 動態獲取 inset（避免雙重 padding）。

### B.9 MVP 不含獨立 Tracking 管線（D-20）

ObjectTrackingPipeline.kt 標記 `@Deprecated`，不作為 Eye-AF 替代追蹤器。  
ML Kit ODT 仍作為 Food mode 的偵測 + 分類引擎運作（§6.1）。  
Eye-AF 由 DefaultTargetTracker（IoU rebinding + TTL 300ms）完整服務。  
OpticalFlowTracker.kt / OpticalFlowConfig.kt 已從 Filesystem 永久刪除。

**否決方案**: Option C（重建 OpticalFlow）= Resume-Driven Development，REJECT。

### B.10 MVP 關閉 enableMultipleObjects（D-21）

SSOT §6.1 代碼範例移除 `.enableMultipleObjects()`，對齊 §2.5.1 MVP 策略 A（跟隨最大 bbox）。  
Google 官方警告：STREAM_MODE + enableMultipleObjects = 大多數裝置幀率不足。  
Phase 1.5 在多道菜場景需求經實機驗證後重新啟用。

### B.11 ML Kit 偵測器互斥運行原則（D-22）

強化 §3.2 已有文字為正式決策：同一 ImageProxy 僅運行一個偵測器。  
消除 dual inference 的效能違規（§10.2 peak latency 60-120ms）、記憶體壓力（雙模型 80-120MB）、thermal throttle 加速觸發。  
MLKitDetector.kt 需重構為 mode-aware：接收 SceneMode 參數，只啟動對應 detector。

### B.12 Face Detection 配置鎖定（D-23）

LANDMARK_ALL + CLASSIFICATION_ALL + CONTOUR_NONE + FAST + minFaceSize 0.15f。  
解鎖 EyeLineRule（MVP P1）+ SmileRule（Phase 1.5）+ true Eye-AF（nearest eye focus）。  
v2.12.0「Unknown landmark type」crash 需 root-cause 後修復，以 try-catch graceful degradation 防護。

**翻盤條件**: 實機驗證側臉偵測率不足 → PTF 模式切 PERFORMANCE_MODE_ACCURATE。

### B.14 Face Detection 配置修正 + Portrait 狀態機入 SSOT（D-27）

D-23 修正：LANDMARK_ALL 降為 NONE（MVP）。v2.12.0 "Unknown landmark type" crash 未根治 + production 從未成功部署 = SSOT 與代碼語義分裂。EyeLineRule 延後至 Phase 1.5；AF focus target 降級為 FACE_CENTER。
新增 §4.4.1 Portrait Guidance 六態狀態機（NO_FACE → FACES_DETECTED_NONE_ELIGIBLE → SUBJECT_STABILIZING → GUIDANCE_ACTIVE → ALIGNED → SUPPRESSED）。
新增 §4.4.2 Face Detector Session Lifecycle（建立/暫停/恢復/切換）。

**翻盤條件**：

- LANDMARK_ALL：ML Kit 新版修復 crash + 1000 幀雙裝置零 crash → Phase 1.5 恢復
- 狀態機：六態不足以覆蓋新產品場景 → 在 SSOT 層增態（禁止 Contract 層自行擴展後再升格）

### B.13 Grid 三分網格線規格鎖定（D-24）

#FFFFFF α0.35, 線寬 1.5px, StrokeCap.Round, contentBounds 內繪製。  
Z-order: Grid 在 PitchTractionLayer 之上（Layer 2 > Layer 1）— 確保不被 Pitch aligned 模式遮蓋。  
Cold start 過渡：ODT warm-up 期間 Grid α0.35 → 首個 Food bbox 到達後 300ms 淡出至 α0.15。

**否決方案**: α0.25 過淡、α0.50 過重、不交換 Z-order（邊緣被 aligned 呼吸削弱）。

---

## Viewfinder Overlay 圖層定義

```
Layer 0: Camera Preview（CameraX PreviewView）
Layer 1: Pitch Traction 指示柱（右側 16dp, 36dp×25% viewfinder, 琥珀金/綠色 — D-2; 底層先繪製）
Layer 2: 三分網格線（#FFFFFF alpha 0.35, 線寬 1.5px — D-24; 在 Pitch 之上確保可見）
Layer 3: Halo Traction 圓環（琥珀金 / 綠色, 呼吸 alpha 0.10→0.18 — D-3）
Layer 4: 對焦圓環（Tap-to-focus, 60dp, 四狀態生命週期 — 見附錄 A）
Layer 5: Level Traction（白色虛線 reference + 動態 dynamic 層）
Layer 6: Scrim + Top Bar 浮動圖示（最上層）
```

---

## 文件元資訊

- **版本**: SSOT v4.0.6 (D-27: LANDMARK_NONE 降級 + Portrait 狀態機 + Detector Lifecycle)
- **建立日期**: 2026-05-12
- **修訂日期**: 2026-06-12
- **合併來源**:
  - v3.3 SSOT (2026-04-04) — 基底
  - Delta v3.4 (2026-05-01~02) — 11 項決策
  - Delta v3.4.1 Patch (2026-05-03) — 7 項補丁 + 2 項補錄
  - Naming Purge (2026-05-12) — Edge Warmth 全面淨化
  - Focus Spec v1 (2026-05-15) — 附錄 A 完整規格書（方案 B：單圈 + 🔒 Badge）
  - Cross-Audit v1 (2026-05-15) — HTML Prototype v3.1.1 vs SSOT 全面校正
  - **Session 4 Full Revision (2026-05-18)** — D-5r~D-12 鎖定 + R1~R18 全面修訂：
    - D-2: Pitch Traction 指示柱取代邊緣漸層（R1, R2, R13, R17）
    - D-5r/D-6r: Focus/Locked timeout 刪除，簡化為永駐（R6~R9）
    - D-9: Food 場景關閉 Level Traction（R3, R18）
    - D-10: Level Traction N=10°（R10, R16）
    - D-11: Direction enum 移交 Halo Traction（R2）
    - D-12: PTF 快門全局下移 4dp（R14 → Layout Spec v2.3）
    - R11/R12: IMU 計算骨架 + pitch 座標定義
    - R15: 快門色彩硬編碼聲明
  - **Session 5 Patch (2026-05-18)** — D-13 Scrim endpoint 確認 + Layout Spec v2.3 產出：
    - D-13: Scrim endpoint 120px CSO 確認鎖定（待實測調整）
  - **Session 6 Revision (2026-05-27)** — D-14~D-19 鎖定 + R19~R28：
    - D-14: PitchTractionLayer null vignette 刪除 — Edge Warmth 語義殘留清除（R22, §B.7）
    - D-15: HDR 三態光線指示（Option 2: 正常/需HDR/需補光）（R23, §2.9）
    - D-16: 光線指示 debounce = 1000ms（R21, §2.9）
    - D-17: FOCUS_AF_TIMEOUT_MS 2000→2500ms — Sony XQ-BT52 PDAF 裕度（R20/R24/R25/R26）
    - D-18: PreviewView scaleType = FILL_CENTER + edge-to-edge cutout 修復（R27, §B.6/B.8）
    - D-19: HDR pulse 3000ms / ⚡ pulse 3000ms（R21/R23, §2.9）
  - **Session 8 Revision (2026-05-28)** — D-20~D-24 鎖定 + R29~R34：
    - D-20: MVP 不含獨立 Tracking 管線（ObjectTrackingPipeline @Deprecated）
    - D-21: MVP 關閉 enableMultipleObjects()（對齊策略 A + Google 官方警告）
    - D-22: ML Kit 偵測器互斥運行原則（§10.2 效能預算強化）
    - D-23: Face Detection 配置鎖定（LANDMARK_ALL + CLASSIFICATION_ALL）
    - D-24: Grid 三分網格線 α0.35/1.5px + Z-order 交換 + cold start 過渡規格
    - R29: Grid 參數 + Z-order（§Overlay 圖層）
    - R30: §6.1 enableMultipleObjects 移除
    - R31: §10.2 互斥運行 + cold start + thermal 備註
    - R32: §10.2 延遲行精確化
    - R33: §4.4 Face Detection 配置表
    - R34: 附錄 B.9~B.13 決策記錄
  - **Session 9 Micro-Revision (2026-05-28)** — D-25~D-26 鎖定 + R35~R36：
    - D-25: Overlay 總表 Layer 1 文案修正 — 「漸層（邊帶 15% viewport）」→「指示柱（右側 16dp, 36dp×25% viewfinder）」（GPT 交叉審查命中）
    - D-26: LEGACY 光線指示降級 — 不顯示光線狀態，維持靜態白色 α0.85（GPT 交叉審查命中）
    - R35: §Overlay 圖層定義 Layer 1 文案對齊 D-2
    - R36: §2.9 LEGACY 降級行語義修復
  - **v4.0.6 (D-27: LANDMARK_NONE 降級 + Portrait 狀態機 + Detector Lifecycle)**：
    - D-27: LANDMARK_ALL → NONE（MVP）— v2.12.0 crash 未根治 + production 從未部署；EyeLineRule 延後 Phase 1.5
    - R37: §4.4 Face Detection 配置表修正（D-23, D-27 修正）
    - R38: §4.4 EyeLineRule 階段 MVP(P1) → Phase 1.5
    - R39: 新增 §4.4.1 Portrait Guidance 六態狀態機
    - R40: 新增 §4.4.2 Face Detector Session Lifecycle
    - R41: 附錄 B.14 決策記錄
- **作廢文件**: v3.0, v3.1, v3.2, v3.3, Delta v3.4, Delta v3.4.1, v4.0, v4.0.1, v4.0.2, v4.0.3, v4.0.4 及所有前版
- **搭配文件**: `Cueshot_Layout_Spec_v2_5_1.md`（佈局精確定義，獨立維護）
- **命名規範**: 本文件中不存在 "Edge Warmth" 作為渲染元件名稱。三通道體系統一使用 Pitch Traction / Level Traction / Halo Traction。PitchTractionLayer 禁止包含 null/radialGradient 渲染邏輯（D-14）。
- **決策記錄**: D-1~D-4 (Session 3), D-5r~D-12 (Session 4), D-13 (Session 5), D-14~D-19 (Session 6), D-20~D-24 (Session 8), D-25~D-26 (Session 9), D-27 (Session VIII CSO) — 全部 locked
- **下一步**:
  1. ~~Layout Spec v2.4 → v2.5.1~~ → 待部署
  2. Claude Code 按 SSOT v4.0.5 + Layout Spec v2.5.1 實作：
     a. C-1: GridOverlay alpha 0.35 + strokeWidth 1.5f（D-24, R29）
     b. C-2: PitchTractionLayer null vignette 刪除（D-14, R22）
     c. C-3: CameraScreen Z-order Grid↔Pitch 交換（D-24, R29）
     d. C-4: MLKitDetector mode-aware 重構（D-22, R31）— 含 LANDMARK_ALL 恢復（D-23, R33）+ enableMultipleObjects 移除（D-21, R30）
     e. C-5: Food mode 雙重確認 + cold start Grid→Halo 過渡（D-24, R31）
     f. PreviewView scaleType → FILL_CENTER（D-18）
     g. edge-to-edge + cutout mode 修復（D-18/B.8）
     h. Scaffold padding 修正（移除雙重 padding）
     i. FOCUS_AF_TIMEOUT_MS → 2500L（D-17）
     j. Top Bar HDR 光線指示三態 pulse（D-15/D-16/D-19, §2.9）
  3. 實機測試 → 截圖帶回 Tech Auditor 比對
  4. Thermal probe 插樁 + 5 分鐘壓力測試

---

**End of SSOT v4.0.6**
