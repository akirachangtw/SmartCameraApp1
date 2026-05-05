# SSOT v3.4 Delta 文件

**文件版本**: SSOT v3.4.1 (Delta)
**最後更新**: 2026-05-03
**變更歷程**: 
  - v3.3 → v3.4: Delta 11 項（UI Tech Audit Session 2026-05-01~02）
  - v3.4 → v3.4.1: Delta Patch 7 項（Tech Auditor 審查 2026-05-03）
**適用方式**: 逐段覆蓋 SSOT v3.3 / TDD v3 / Layout Spec v2.0 對應段落  
**作者**: CSO + Ambient UI Tech Auditor (Claude Opus 4.6)

---

## 變更總覽

| # | Decision | 影響文件 | 影響段落 | 修改類型 |
|---|----------|---------|---------|---------|
| 1 | Level Traction 對齊即消失 | SSOT v3.3 | §2.3 | 新增備註 |
| 2 | LEGACY 快門冷卻 500ms | SSOT v3.3 | §10.3 | 修改表格 |
| 3 | Food Halo 策略 A + EMA 0.8/0.2 | SSOT v3.3 | §2.4 / §6.3 | 修改 + 新增 |
| 4 | PTF 加入 Level Traction | SSOT v3.3 | §4.5 | 修改 |
| 5 | 橫拍 Level Traction MVP 隱藏 | SSOT v3.3 | §2.3 | 新增備註 |
| 6 | Pitch Traction 45°/90° 雙配置 | SSOT v3.3 | §2.3（新增子節） | 新增規格 |
| 7 | 多人群組 Halo | SSOT v3.3 | §2.4（新增子節） | 新增規格 |
| 8 | Focus Lost 振幅 4px | TDD v3 | §3.3 | 修改數值 |
| 9 | Canvas 呼叫預算表 | SSOT v3.3 | §10（新增 §10.5） | 新增段落 |
| — | Top Scrim 72px → 120px | Layout Spec v2.0 | §3.2 | 修改數值 |
| — | TDD §4.4 PtfTabBar 作廢 | TDD v3 | §4.4 | 標記作廢 |

---

## Delta-0：SSOT v3.3 §2.0 全局 Ambient 常數（v3.4.1 新增）

### §2.0 全局 Ambient 常數

所有 Ambient UI 元素共用的感知常數。統一後用戶不需要學習不同場景的不同「對齊門檻」— 體感永遠一致。

```kotlin
/**
 * v3.4.1 新增：全局 Ambient 常數
 * 所有場景統一的感知參數，確保跨場景體感一致
 */
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

### §2.0.1 Level Traction 漸入退場規格

Level Traction 不是「開/關」二元切換，而是根據偏差程度漸入/漸出：

| 偏差（°） | opacity | 視覺狀態 |
|----------|---------|---------|
| 0° | 0.00 | 完全消失（已對齊 — Decision D1 退場行為） |
| 1° | 0.20 | 微弱浮現 |
| 2° | 0.40 | 輕度可見 |
| 3° | 0.60 | 中度可見 |
| 4° | 0.80 | 強烈可見 |
| ≥5° | 1.00 | 完全可見（鎖定最大值） |

**公式**：

```kotlin
val levelOpacity = (maxDeviation / AmbientConstants.LEVEL_FADE_RANGE_DEG)
    .coerceIn(0f, 1f)
```

其中 `maxDeviation = max(abs(pitchDiff), abs(rollDiff))`。

---

## Delta-1：SSOT v3.3 §2.3 場景映射表（覆蓋原表）

**變更**：Decision #1 (Level Traction 退場) + #5 (橫拍隱藏) + #6 (Pitch 雙配置)

### §2.3 邊緣暖光 (Edge Warmth) 規格 — v3.4 更新

程式碼區塊不變（`drawEdgeWarmth` 函數維持原樣）。以下場景映射表**整表覆蓋**原版：

**場景映射（v3.4 更新）**：

| 場景 | Edge Warmth 引導內容 | v3.4 變更備註 |
|------|---------------------|-------------|
| Food 45° | Pitch Traction 向 45° 牽引 + Level Traction 水平修正 | Pitch 使用 `PitchTractionConfig.FOOD_45` (見 §2.3.1) |
| Food 90° | Pitch Traction 向 90° 牽引 + 雙軸水平（Roll + Pitch 微調）| Pitch 使用 `PitchTractionConfig.FOOD_90` (見 §2.3.1)；切換閾值 75°，hysteresis 70°↔80° |
| Portrait | Level Traction 水平修正 | — |
| Pass-to-Friend | Pitch Traction + Level Traction 簡化版 | **v3.4 新增 Level Traction**（Decision #4） |
| Minimalist | Level Traction 水平修正（不做角度引導）| — |
| General | Level Traction 水平修正 | — |

**Level Traction 退場行為（v3.4 新增 — Decision #1）**：

> Level Traction 對齊後**完全消失**（alpha → 0），不保留基底 alpha。  
> 設計理由：符合 Ambient UI「完成即退場」哲學。全局對齊確認由快門外環呼吸韻律承擔。  
> 撤回 v3.3 審查中「保留 alpha 0.10 基底」的建議。

**橫拍 Level Traction（v3.4 新增 — Decision #5）**：

> MVP 階段橫拍時 Level Traction **隱藏不顯示**。  
> 理由：「不做比做錯好」— 橫拍的水平判定需要四方向旋轉自適應邏輯，MVP 來不及做且做錯會破壞信任。  
> Phase 1.5 排程：實作 `OrientationAwareLevel` 四方向自適應。

---

### §2.3.1 Pitch Traction 動態角度配置（v3.4 新增 — Decision #6）

```kotlin
/**
 * v3.4 新增：45°/90° 雙配置
 * 切換閾值 75°，hysteresis 70°↔80° 防跳動
 */
data class PitchTractionConfig(
    val targetDeg: Float,           // 45f or 90f
    val orbitRatio: Float,          // Edge Warmth 漸層深度佔螢幕比例
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
            alignedThresholdDeg = 5f   // v3.4.1: 3f → 5f
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

---

## Delta-2：SSOT v3.3 §2.4 Halo 規格更新

**變更**：Decision #3 (Food Halo 策略 A) + #7 (多人群組 Halo)

### §2.4 場景映射表（覆蓋原表）

| 場景 | Halo 引導內容 | v3.4 變更備註 |
|------|-------------|-------------|
| Food（有 ML Kit Object Detection bbox）| 食物 Halo → 三分法對齊 | **v3.4：MVP 策略 A — 使用最大 bbox**（見 §2.4.1） |
| Portrait（單人，有 Face Detection bbox）| 人臉 Halo → 三分法 + 頭部空間 + 視線空間 | — |
| Portrait（多人）| **群組包圍 Halo** → 人數依賴 target | **v3.4 新增**（見 §2.4.2） |
| Pass-to-Friend | 人臉 Halo + 中心引力場 | — |
| Minimalist（小主體）| 微小確認 Halo（綠色，不說「靠近」）| — |
| Food（無 bbox）| 無 Halo，僅用 Edge Warmth + 網格 | — |
| General | 無 Halo，僅網格 | — |

### §2.4 統一 Halo 語言表（覆蓋原表）

| 元素 | 形狀 | 顏色邏輯 | 觸發 | v3.4 備註 |
|------|------|---------|------|----------|
| 手動對焦 | 圓形 Halo | 金色→綠色 | 用戶觸摸螢幕 | — |
| 構圖引導（單主體）| 圓形 Halo（跟隨 bbox） | 金色→綠色 | ML Kit 偵測到主體 | — |
| 構圖引導（多人）| **群組包圍 Halo** | 金色→綠色 | ML Kit 偵測 ≥2 人臉 | **v3.4 新增** |
| 美食引導 | 圓形 Halo（跟隨最大 food bbox） | 金色→綠色 | ML Kit Object Detection FOOD | **v3.4 明確策略 A** |
| 遞手機模式 | 圓形 Halo（跟隨人臉） | 金色→綠色 | Pass-to-Friend 模式 | — |
| 減法留白確認 | 微小圓形 Halo | 綠色 | MinimalistRule 通過 | — |

---

### §2.4.1 美食 Halo 策略（v3.4 新增 — Decision #3）

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

---

### §2.4.2 多人群組 Halo（v3.4 新增 — Decision #7）

```kotlin
/**
 * v3.4 新增：多人場景群組包圍 Halo
 * 取代逐人繪製 Halo（節省 Canvas 呼叫 + 視覺簡潔）
 */
data class GroupHaloResult(
    val center: Offset,      // 群組質心
    val radius: Float,       // 包圍半徑
    val target: Offset,      // 構圖引導目標點
    val alpha: Float          // 顯示透明度
)

fun calculateGroupHalo(
    faces: List<Face>,
    screenWidth: Float,
    screenHeight: Float
): GroupHaloResult {
    val centers = faces.map { it.boundingBox.center() }

    // 群組質心
    val cx = centers.map { it.x }.average().toFloat()
    val cy = centers.map { it.y }.average().toFloat()

    // 包圍半徑 = 質心到最遠人臉的距離 + 單人 Halo padding
    val maxDist = centers.maxOf { distance(it, Offset(cx, cy)) }
    val padding = faces.maxOf { max(it.boundingBox.width(), it.boundingBox.height()) } * 0.3f
    val radius = maxDist + padding

    // 人數依賴的構圖目標點
    val target = when (faces.size) {
        2 -> Offset(screenWidth * 0.5f, screenHeight * 0.33f)  // 水平中央，上三分線
        3 -> Offset(screenWidth * 0.5f, screenHeight * 0.40f)  // 中央偏上
        else -> Offset(screenWidth * 0.5f, screenHeight * 0.5f) // 4+ 人 → 正中央
    }

    // alpha 降 0.05 補償大面積 Halo 的視覺重量
    val baseAlpha = 0.25f  // 單人未對齊 alpha
    val groupAlpha = baseAlpha - 0.05f  // = 0.20

    return GroupHaloResult(Offset(cx, cy), radius, target, groupAlpha)
}


/**
 * v3.4.1 新增：群組 Halo 質心 EMA 平滑
 *
 * EMA 參數對比：
 *   Portrait:  0.7 / 0.3  — 人臉會轉頭點頭，需較快反應
 *   Group:     0.75 / 0.25 — 多人質心比單人更容易跳，但仍需追蹤隊形變化
 *   Food:      0.8 / 0.2  — 食物不動，最重穩定性
 *
 * 原理：每次新數據進來，不全信它，混合歷史值抑制抖動
 *   smoothed = last × 歷史權重 + new × 新值權重
 */

// 在 ViewModel / Halo 計算器中維護狀態
private var lastGroupCenter: Offset? = null
private var lastGroupRadius: Float? = null

private companion object {
    const val GROUP_EMA_HISTORY = 0.75f  // 歷史權重
    const val GROUP_EMA_NEW = 0.25f      // 新值權重
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

| 人數 | 構圖目標位置 | 理由 |
|------|------------|------|
| 2 人 | 水平中央，上三分線 (y=33%) | 雙人並排的經典構圖 |
| 3 人 | 中央偏上 (y=40%) | 三角構圖重心略高 |
| 4+ 人 | 正中央 (y=50%) | 多人合照以居中為優先 |

**Alpha 規則**：群組 Halo alpha = 單人 alpha − 0.05（未對齊 0.20 / 對齊 0.30），補償大面積 Halo 的視覺壓迫感。

---

## Delta-3：SSOT v3.3 §4.3 美食模式規則表（覆蓋原表）

**變更**：Decision #3 — 新增 Food Halo EMA 備註

| 規則 | 數據源 | 延遲 | 付費層 | v3.4 備註 |
|------|--------|------|--------|----------|
| AngleRule | IMU pitch vs [45°, 60°, 90°] | < 2ms | Free | Pitch 配置見 §2.3.1 |
| LevelRule | IMU roll (+ dual-axis at 90°) | < 2ms | Free | — |
| FoodCompositionRule | ML Kit Object Detection bbox | < 80ms | Paid | **Halo 跟隨最大 bbox；EMA 0.8/0.2**（見 §2.4.1） |
| LightRule | 直方圖平均亮度 | < 10ms | Paid | — |
| MinimalistRule | bbox ratio + background CV | < 10ms | Paid | — |

---

## Delta-4：SSOT v3.3 §4.5 遞手機模式規則（覆蓋原文）

**變更**：Decision #4 — PTF 加入 Level Traction

### §4.5 遞手機模式規則（v3.4 更新）

簡化版，保留三條核心規則 + Level Traction：

- **FramingRule**（居中）— 朋友幫拍最容易失誤：主體沒居中
- **LevelRule**（粗檢）— 朋友幫拍第二大失誤：手機歪斜
- **AngleRule**（粗檢）— 可犧牲：PTF 是社交場景，朋友不會為角度死磕

UI 使用 Halo 牽引 + Edge Warmth + **Level Traction**。對齊後 Halo 金色→綠色 + 呼吸。

**v3.4 變更說明**：
> 新增 Level Traction（Edge Warmth 水平修正引導），與 Portrait 模式共用同一套 Level Traction 渲染邏輯。  
> 理由：朋友幫拍第二大失誤是手機歪斜（Decision #4）。Level Traction 的 IMU 驅動成本極低（60fps 無額外負擔），加入 PTF 的 Canvas 呼叫從 7 次增至 ~10 次，仍在預算內。


### §4.5.1 PTF Level Traction 顏色策略（v3.4.1 新增）

> PTF 模式下 Level Traction 的 dynamic 層（移動線 + 圓點）**固定使用對齊綠 `#50C864`**，不經過 amber→green 漸變。  
> reference 層（虛線 + 空心圓）維持白色不變。

**設計理由**：  
PTF 用戶是「不懂攝影的朋友」。amber 金色在 Cueshot 全局視覺語言中代表「尚未對齊」，對陌生用戶產生「我做錯了」的焦慮感。固定綠色傳達「跟著這條線走就好」的正向引導，降低認知壓力。

**退場行為不變**：對齊後 α→0 完全消失，與所有模式一致。

**Compose 實作**：

```kotlin
// Level Traction dynamic 層顏色選擇
val levelDynamicColor = if (isPtfMode) {
    AlignGreen  // PTF：固定綠色，不看 isAligned 狀態
} else {
    // 標準模式：amber → green 漸變
    if (isLevelAligned) AlignGreen else AmberGold
}
```

---

## Delta-5：SSOT v3.3 §6.3 美食構圖引導能力表（覆蓋原表）

**變更**：Decision #3 — Food Halo 策略 A 明確化

| 能力 | 數據源 | Ambient UI 表現 | v3.4 備註 |
|------|--------|----------------|----------|
| 主體三分法對齊 | food bbox center vs golden points | Halo 牽引（與人像相同語法）| **MVP：跟隨最大 bbox（策略 A）** |
| 主體佔比 | food bbox area / frame area | 太小觸發減法留白偵測 | — |
| 多道菜平衡 | 多個 food bbox 的分布 | ~~建議切換俯視 90°~~ → **MVP 不做多 Halo；Toast 建議 90° 俯拍** | **Phase 1.5 擴展** |
| 背景整潔度 | negativeSpaceCV | 雜亂 → 冷色暈提示 | — |

---

## Delta-6：SSOT v3.3 §10.3 三級裝置降級表（覆蓋原表）

**變更**：Decision #2 — LEGACY 快門冷卻 500ms

| 硬體等級 | 核心引導 | 輔助功能 | 關閉功能 | 群組 Halo 行為 (v3.4.1) | 快門冷卻 |
|---------|---------|---------|---------|-------------------------|----------|
| FULL / LEVEL_3 | Edge Warmth 60fps + Halo 10fps + 所有規則 | 鏡頭髒汙 2fps + 雜亂框框 + 減法留白 | 無 | 完整群組包圍 Halo | **300ms** |
| LIMITED | Edge Warmth 60fps + Halo 8fps + 核心 4 規則 | 鏡頭髒汙 1fps + 減法留白 | 雜亂框框（僅冷色暈替代）| 完整群組包圍 Halo | **300ms** |
| LEGACY | Edge Warmth 60fps + Halo 5fps + 2 規則 | 無 | 鏡頭髒汙 + 減法留白 + 雜亂框框 + 光線分析 | **降級為最大人臉的單人 Halo** | **500ms + 外環閃爍** |

**LEGACY 500ms 設計理由（v3.4 新增 — Decision #2）**：

> LEGACY 裝置關閉了大部分輔助功能（含 Flash 提醒），快門按下後缺少視覺確認反饋。  
> 冷卻延長至 500ms + 快門外環閃爍一次（白色 alpha 0.45 → 0 在 300ms 內），  
> 補償 FULL/LIMITED 裝置上由 Flash 動畫提供的「已拍攝」確認感。  
> FULL/LIMITED 維持 300ms（有完整視覺反饋鏈）。

### §10.3.1 快門外環動畫互斥規則（v3.4.1 新增）

快門外環同時承擔「全條件對齊確認」（呼吸韻律）與「已拍攝確認」（閃爍）兩個視覺職責。  
兩者**不可同時播放**，以下為全裝置通用的時序規格：

**時序定義**：

| 時間點 | 行為 | 視覺狀態 | 設計理由 |
|--------|------|---------|---------|
| T0: 快門按下 | 暫停呼吸動畫，凍結外環為靜態綠色 | 外環靜態綠色 | 清空視覺通道，準備接收閃爍信號 |
| T0 ~ T0+300ms | 外環閃爍一次：白色 α0.45→0 | 白色閃爍 | 「已拍攝」確認；白色與綠色色相斷裂，不可能誤讀為呼吸 |
| T0+300ms ~ T0+冷卻結束 | 外環維持靜態，不恢復呼吸 | 靜態（無動畫） | 冷卻期間快門不可按，呼吸恢復會暗示「可以再拍」造成誤導 |
| T0+冷卻結束 | 判斷是否仍全對齊 → 是則恢復呼吸，否則保持靜態白環 | 呼吸或靜態 | 重新允許拍攝 |

**冷卻時長（沿用 Delta-6 定義）**：

| 裝置等級 | 冷卻時長 | 閃爍規格 |
|---------|---------|---------|
| FULL / LIMITED | 300ms | 白色 α0.45→0, 300ms（閃爍 = 冷卻全程） |
| LEGACY | 500ms | 白色 α0.45→0, 300ms（閃爍後 +200ms 靜態等待） |

**Compose 實作指引**：

```kotlin
// 快門外環狀態機
// 呼吸使用 InfiniteTransition，閃爍使用 LaunchedEffect
// 兩者通過 isBreathing 旗標互斥

var isBreathing by remember { mutableStateOf(false) }
var isFlashing by remember { mutableStateOf(false) }

// 呼吸動畫：僅在 isBreathing = true 時啟用
val breatheAlpha by if (isBreathing) {
    infiniteTransition.animateFloat(
        initialValue = 0.10f, targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        )
    )
} else {
    remember { mutableFloatStateOf(0f) } // 靜態，不消耗動畫資源
}

// 快門按下觸發
fun onShutterPressed() {
    isBreathing = false  // 立即暫停呼吸
    isFlashing = true
    
    scope.launch {
        // 閃爍動畫由 Animatable 驅動（300ms）
        flashAlpha.animateTo(0.45f, tween(50))
        flashAlpha.animateTo(0f, tween(250))
        
        // 冷卻等待（LEGACY 額外 +200ms）
        delay(if (isLegacy) 200L else 0L)
        
        isFlashing = false
        
        // 冷卻結束：檢查是否仍全對齊
        if (allConditionsMet) {
            isBreathing = true  // 恢復呼吸
        }
    }
}
```


---

## Delta-7：TDD v3 §3.3 Focus Lost 振幅修正

**變更**：Decision #8

### §3.3 動畫規格（修改行）

| 狀態 | 視覺效果 | 顏色 | 持續時間 | v3.4 變更 |
|------|---------|------|---------|----------|
| Focusing | 1.3x→1.0x 縮放 + 60° 弧線旋轉 | 白色 α=0.9 | 至 AF 完成 | — |
| Focused | 穩定 + 脈動一次 | 綠色 α=0.8 | 1.5s + 0.5s 淡出 | — |
| Failed | 左右震動 **4px** | 琥珀 α=0.6 | 0.8s + 0.5s 淡出 | **v3.4：2px → 4px（HTML 原型驗證值）** |
| Locked | 雙圈 + "AE/AF" 小字 | 亮金 α=0.85 | 至解鎖 | — |

---

## Delta-8：TDD v3 §4.4 作廢標記

**變更**：Pill Toggle 取代 PTF Tab

### §4.4 ~~PTF Tab（底部，主控制列下方）~~

> **⚠️ DEPRECATED (v3.4)**  
> 本段落已作廢。PTF Tab（36dp 全寬綠色條）已被 **Layout Spec v2.1 §5 Photo/PTF Pill Toggle** 完全取代。  
> Pill Toggle 規格見 Layout Spec v2.1 §5。  
> `PtfTabBar` Composable 程式碼不再使用。  
> 相關切換行為（零硬體操作、快門冷卻、首次 Bottom Sheet）仍有效，入口改為 Pill Toggle。

---

## Delta-9：Layout Spec v2.0 §3.2 Scrim 修正

**變更**：Top Scrim 72px → 120px

### §3.2 浮動 Top Bar — v2.1 更新

| 屬性 | v2.0 值 | **v2.1 值** | 變更理由 |
|------|---------|------------|---------|
| Scrim 結束 | y = 72px, alpha 0 | **y = 120px**, alpha 0 | Tech Audit 審查：72px 在高亮度戶外不足以保護圖示可讀性；120px 提供更自然的漸變退場 |

其餘參數不變（Scrim 起始 y=0 alpha 0.22 / 漸變曲線 FastOutSlowInEasing / 圖示位置 y=24px+statusBarsPadding）。

---

## Delta-10：SSOT v3.3 §10.5 Canvas 呼叫預算表（新增段落）

**變更**：Decision #9

### §10.5 Canvas 呼叫預算（v3.4 新增）

每幀 Compose Canvas `drawXxx` 呼叫次數上限 **< 15 次**。超過需優化。

| 狀態 | 場景 | Canvas 呼叫數 | 狀態 | 優化方案 |
|------|------|-------------|------|---------|
| State 1 | General | 11 | ✅ 預算內 | — |
| State 2 | Portrait Unaligned | ~~14~~ → **12** | ⚠️ 需優化 | Level reference 層預渲染為 Bitmap |
| State 3 | Portrait Aligned | ~8 | ✅ 預算內 | — |
| State 4 | PTF | 7 → **~10** | ✅ 預算內 | v3.4 加入 Level Traction 後 |
| State 5 | Focus | ~~13~~ → **11** | ⚠️ 需優化 | Focus ring 合併繪製 |
| State 6 | Food | 8 → **~12** | ✅ 預算內 | v3.4 加入 Food Halo 後 (v3.4.1 修正) |

**State 2 優化策略**：

```
問題：Level reference 線（水平儀虛線 + 刻度標記）佔 4 次 drawXxx
方案：首次繪製後快取為 Bitmap，後續幀直接 drawImage
效果：4 次 → 1 次，State 2 從 14 降至 ~11-12
副作用：Bitmap 記憶體 ~50KB（可忽略）
觸發時機：Level Traction 首次啟用時預渲染
```

---

## 未涵蓋項目（需獨立處理）

| 項目 | 理由 | 處理方式 |
|------|------|---------|
| Layout Spec v2.0 → v2.1 Pill 28dp 精確規格 | 原 Workbench 未抽取 §5 | Pill 規格已在 Layout Spec v2.0 §5 完整定義，28dp→維持不變；僅 Scrim 需更新 |
| HTML 原型 v3 → v3.1 更新指令 | 需 HTML 原型檔案 | 待補上 HTML v3 後產出 Gemini 更新任務 |
| SSOT v3.3 §2.5 呼吸韻律 | 審查通過無修改 | 不需回寫 |

---

## 文件元資訊

- **版本**: Delta v3.3→v3.4
- **建立日期**: 2026-05-03
- **決策來源**: UI Tech Audit Handoff 20260502（9 項 Key Decisions + 2 項補充）
- **適用目標**: SSOT v3.3 / TDD v3 / Layout Spec v2.0
- **下一步**: 
  1. 逐段覆蓋至 SSOT v3.4 正式版（或保持 Delta 獨立引用）
  2. 更新 HTML 原型 v3 → v3.1（PTF 加 Level / Food 加 Halo / Level 對齊消失）
  3. Claude Code 按 Delta 規格實作

---

**End of Delta v3.3→v3.4**
