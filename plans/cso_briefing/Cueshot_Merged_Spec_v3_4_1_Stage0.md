# Cueshot 合併規格包 v3.4.1 — Stage 0 交付物

**版本**: Merged Spec v3.4.1-Stage0
**建立日期**: 2026-05-05
**用途**: Gemini 架構重審（Stage 1）唯一規格輸入，取代分散的 Layout Spec v2.1 / Delta v3.4.1 / Strategic Brief v1
**Context 預算**: 本文件 ≤ 15K tokens，為 Gemini 125K 有效 Context 留 ~110K 給 codebase 掃描
**合併規則**: Delta v3.4.1 為最高權威 → Layout Spec v2.1 次之 → Strategic Brief v1 僅供戰略參考

---

## ⚠️ OVERRIDE 衝突解決記錄

以下三處為合併前文件間的衝突，已統一解決：

| # | 衝突點 | 舊值（來源） | **新值（最終）** | 備註 |
|---|--------|-------------|-----------------|------|
| 1 | Top Scrim 結束位置 | 72px（Layout Spec v2.1 §3.2） | **120px**（Delta-9） | 待 Pixel 9a 實機驗測；代碼用 `AmbientConstants.SCRIM_HEIGHT_PX` 常數 |
| 2 | FOOD_90 alignedThresholdDeg | 3°（SSOT v3.3 原始） | **5°**（Delta §2.3.1） | 全場景統一 ±5°，見 AmbientConstants |
| 3 | PTF 入口 | Tab 36dp 全寬（Brief v1 PART B.3） | **Pill Toggle 128dp × 28dp**（Layout Spec v2.1 §5） | Brief PTF 段落已過時，一切以本文件為準 |

**TDD v3 §4.4 PtfTabBar**：已作廢（Delta-8）。Pill Toggle 完全取代。

---

## PART 1：全局常數（代碼 SSOT）

```kotlin
object AmbientConstants {
    // 對齊判定
    const val ALIGNED_THRESHOLD_DEG = 5f        // 所有場景統一（±5°）

    // Level Traction 漸入
    const val LEVEL_FADE_RANGE_DEG = 5f          // 0°=消失, 5°=完全可見

    // Scrim
    const val SCRIM_HEIGHT_PX = 120f             // [OVERRIDE] 72→120, 待實機驗測

    // 場景切換
    const val HYSTERESIS_SCENE_MS = 500L
    const val HYSTERESIS_PITCH_LOW_DEG = 70f     // 45°→90° 切換下限
    const val HYSTERESIS_PITCH_HIGH_DEG = 80f    // 45°→90° 切換上限

    // 動畫時序
    const val BREATHE_CYCLE_MS = 2000L
    const val TRANSITION_FAST_MS = 300L
    const val SHUTTER_FLASH_DURATION_MS = 300L
}
```

---

## PART 2：螢幕佈局（兩區塊模型）

### 基準裝置: 390 × 844pt（@3x = 1170 × 2532px）

```
┌──────────────────────────────────┐ ← y = 0px
│ ░░░ Scrim (0→120px) ░░░░░░░░░░ │   #000000 α0.22 → α0 @ 120px [OVERRIDE]
│ ░ [⚡][HDR][💧]       [⚙] [?] ░ │   白色 α0.85, 24dp @ y=24+statusBarsPadding
│ ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ │   FastOutSlowInEasing
│                                  │
│        Camera Preview            │   ← 區塊 1: Viewfinder 664px (78.7%)
│        CameraX PreviewView       │      scaleType = FILL_CENTER
│        (fills to top)            │
│                                  │
│  Overlay Layers:                 │
│  L0: Preview / L1: Grid α0.08   │
│  L2: Edge Warmth / L3: Halo     │
│  L4: Focus Ring / L5: Level     │
│  L6: Scrim + Top Bar            │
│                                  │
├─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┤ ← y = 664px (分隔線 #FFF α0.05)
│                                  │
│        [ 📷 | 👥 ]              │   ← Pill Toggle 區 40px
│                                  │   ← 區塊 2: 底部控制列 180px (21.3%)
│    [📷]      ( ● )      [🔄]    │   ← 快門區 80px
│                                  │
│         (safe area 60px)         │   ← navigationBarsPadding()
│                                  │
└──────────────────────────────────┘ ← y = 844px
```

### 底部背景色

| 模式 | 色值 | 說明 |
|------|------|------|
| Photo（預設） | `#0D0D0D` | 冷中性黑 |
| PTF | `#1A1714` | 暖棕（animateColorAsState 300ms） |

---

## PART 3：Pill Toggle 完整規格（取代 PTF Tab）

| 屬性 | 值 |
|------|-----|
| 外部尺寸 | 128dp × 28dp, 圓角 14dp |
| Track 外框 | 1px stroke #555555 |
| 選中指示器 | 60dp × 24dp, 圓角 12dp, fill **#4A4A4A** |
| 選中圖示 | #CCCCCC, 20dp (Camera: `Icons.Outlined.CameraAlt` / Users: `Icons.Outlined.People`) |
| 未選中圖示 | #666666, 20dp |
| 切換動畫 | 300ms FastOutSlowInEasing |
| Touch target | 128dp × 48dp (含 padding 10dp) |

### 切換行為

| 行為 | 規格 |
|------|------|
| CameraX | **零硬體切換** — 不 rebind, 不 close ML Kit, 不重校 IMU |
| 快門冷卻 | 切至 PTF 後 **1 秒內不可按** |
| 首次 PTF | Bottom Sheet（max(screenHeight × 0.20, 96dp)）|
| 底色過渡 | #0D0D0D ↔ #1A1714, 300ms |
| PTF 進入 | 隱藏 Gallery/Flip + 浮現 ✕ 退出按鈕（快門左側）+ 快門放大 72→80dp |
| PTF 退出 | Pill 點 Photo 側 / ✕ 按鈕（雙路徑） |

---

## PART 4：Edge Warmth + Pitch Traction

### 場景映射表

| 場景 | Edge Warmth 引導 | 備註 |
|------|-----------------|------|
| Food 45° | Pitch Traction → 45° + Level Traction | Config: FOOD_45 |
| Food 90° | Pitch Traction → 90° + 雙軸水平 | Config: FOOD_90; 切換閾值 75°, hysteresis 70°↔80° |
| Portrait | Level Traction 水平修正 | — |
| PTF | Pitch Traction + Level Traction | Level 固定綠色（§PTF 特殊規格） |
| Minimalist | Level Traction（不做角度引導） | — |
| General | Level Traction 水平修正 | — |

### Pitch Traction 雙配置

```kotlin
data class PitchTractionConfig(
    val targetDeg: Float,
    val orbitRatio: Float,
    val alignedThresholdDeg: Float  // 統一 5f
) {
    companion object {
        val FOOD_45 = PitchTractionConfig(45f, 0.30f, 5f)
        val FOOD_90 = PitchTractionConfig(90f, 0.20f, 5f)  // [OVERRIDE] 3→5
    }
}
```

### Level Traction 漸入退場

```kotlin
// 不是二元開關，而是線性漸入
val levelOpacity = (maxDeviation / AmbientConstants.LEVEL_FADE_RANGE_DEG).coerceIn(0f, 1f)
// maxDeviation = max(abs(pitchDiff), abs(rollDiff))
// 0° = 完全消失 (對齊即退場)
// 5° = 完全可見
```

**橫拍**：MVP 隱藏 Level Traction（Phase 1.5 排程 `OrientationAwareLevel`）。

---

## PART 5：Halo 規格

### 場景映射表

| 場景 | Halo 行為 | EMA 參數 |
|------|----------|----------|
| Food（有 bbox） | 跟隨最大 food bbox → 三分法對齊 | 0.8 / 0.2（歷史 80%） |
| Portrait（單人） | 人臉 → 三分法 + headroom + gaze space | 0.7 / 0.3 |
| Portrait（多人） | **群組包圍 Halo** → 人數依賴 target | 0.75 / 0.25 |
| PTF | 人臉 + 中心引力場 | 0.7 / 0.3 |
| Minimalist | 微小確認 Halo（綠色） | — |
| Food（無 bbox） | 無 Halo | — |
| General | 無 Halo | — |

### 群組 Halo（≥2 人臉）

| 人數 | 構圖目標 | Alpha |
|------|---------|-------|
| 2 | 中央, y=33% | 0.20（未對齊）/ 0.30（對齊） |
| 3 | 中央, y=40% | 同上 |
| 4+ | 正中央, y=50% | 同上 |

Alpha = 單人 alpha − 0.05（補償大面積視覺壓迫）。

### LEGACY 群組降級

LEGACY 裝置：群組 Halo 降級為**最大人臉的單人 Halo**（節省 Canvas 呼叫）。

### Halo 顏色

| 狀態 | 色值 | Alpha |
|------|------|-------|
| 未對齊 | `#D4A030`（琥珀金） | 0.25（單人）/ 0.20（群組） |
| 對齊 | `#50C864`（綠） | 0.35（單人）/ 0.30（群組） |

---

## PART 6：PTF 模式特殊規格

### 三規則（簡化版）

1. **FramingRule**（居中）— 主體沒居中是最大失誤
2. **LevelRule**（粗檢）— 手機歪斜是第二大失誤
3. **AngleRule**（粗檢）— 可犧牲

### PTF Level Traction 顏色

> **固定使用對齊綠 `#50C864`**，不經過 amber→green 漸變。
> Reference 層維持白色不變。
> 設計理由：朋友不懂攝影，amber 會產生「我做錯了」焦慮。

```kotlin
val levelDynamicColor = if (isPtfMode) {
    AlignGreen  // 固定綠，不看 isAligned
} else {
    if (isLevelAligned) AlignGreen else AmberGold
}
```

退場行為不變：對齊後 α→0 消失。

---

## PART 7：快門外環動畫互斥

快門外環承擔兩個語義：「全對齊呼吸」+「已拍攝閃爍」。**不可同時播放。**

| 時間點 | 行為 | 視覺 |
|--------|------|------|
| T0: 快門按下 | 暫停呼吸, 凍結靜態綠 | 靜態綠 |
| T0 ~ T0+300ms | 外環閃爍：白 α0.45→0 | 白色閃爍 |
| T0+300ms ~ 冷卻結束 | 靜態（不恢復呼吸） | 無動畫 |
| 冷卻結束 | 全對齊→恢復呼吸；否則靜態白環 | 呼吸或靜態 |

| 裝置等級 | 冷卻 | 閃爍 |
|---------|------|------|
| FULL / LIMITED | 300ms | 白 α0.45→0, 300ms |
| LEGACY | 500ms | 白 α0.45→0, 300ms + 200ms 靜態等待 |

---

## PART 8：三級裝置降級

| 等級 | 核心 | 輔助 | 關閉 | 群組 Halo | 快門冷卻 |
|------|------|------|------|----------|----------|
| FULL | EW 60fps + Halo 10fps + 全規則 | 髒汙 2fps + 雜亂框 + 減法留白 | — | 完整群組包圍 | 300ms |
| LIMITED | EW 60fps + Halo 8fps + 核心 4 規則 | 髒汙 1fps + 減法留白 | 雜亂框 | 完整群組包圍 | 300ms |
| LEGACY | EW 60fps + Halo 5fps + 2 規則 | — | 髒汙+減法+雜亂+光線 | **最大人臉單人** | 500ms+閃爍 |

**鐵律**: Edge Warmth 永遠 60fps。

---

## PART 9：Canvas 呼叫預算

每幀 `drawXxx` 上限 **< 15 次**。

| State | 場景 | 呼叫數 | 優化 |
|-------|------|--------|------|
| 1 | General | 11 | — |
| 2 | Portrait Unaligned | ~12 | Level reference 預渲染 Bitmap |
| 3 | Portrait Aligned | ~8 | — |
| 4 | PTF | ~10 | — |
| 5 | Focus | ~11 | Focus ring 合併 |
| 6 | Food | ~12 | — |

---

## PART 10：美食模式規則表

| 規則 | 數據源 | 延遲 | 付費層 | 備註 |
|------|--------|------|--------|------|
| AngleRule | IMU pitch vs [45°,60°,90°] | <2ms | Free | Pitch 配置見 PART 4 |
| LevelRule | IMU roll (+ dual-axis @ 90°) | <2ms | Free | — |
| FoodCompositionRule | ML Kit Object Detection bbox | <80ms | Paid | Halo 跟隨最大 bbox; EMA 0.8/0.2 |
| LightRule | 直方圖平均亮度 | <10ms | Paid | — |
| MinimalistRule | bbox ratio + background CV | <10ms | Paid | — |

---

## PART 11：Focus Ring 規格

| 狀態 | 視覺效果 | 顏色 | 持續時間 |
|------|---------|------|---------|
| Focusing | 1.3x→1.0x 縮放 + 60° 弧線旋轉 | 白 α0.9 | 至 AF 完成 |
| Focused | 穩定 + 脈動一次 | 綠 α0.8 | 1.5s + 0.5s 淡出 |
| Failed | 左右震動 **4px** | 琥珀 α0.6 | 0.8s + 0.5s 淡出 |
| Locked | 雙圈 + "AE/AF" 小字 | 亮金 α0.85 | 至解鎖 |

---

## PART 12：呼吸韻律

```
allConditionsMet → alpha 0.10 ↔ 0.18, 2000ms 週期, FastOutSlowInEasing
```

---

## PART 13：場景判斷邏輯

```
PASS_TO_FRIEND (手動 Pill Toggle) 
  > pitch > 35° → Food 
  > face detected → Portrait 
  > 其他 → General
切換 hysteresis: 500ms 防跳動
```

---

## PART 14：效能分幀策略

```
Frame 1: ML Kit + Rules + Priority → 65ms
Frame 2: Histogram + Minimalist + Clutter → 13ms
Frame 3: ML Kit + Rules + Priority → 65ms
Frame 4: Histogram + Minimalist + Clutter → 13ms
Frame 5: Lens Smudge → 12ms
```

---

## PART 15：Freemium 切分

| 層級 | 功能 | 邏輯 |
|------|------|------|
| **免費 (Hook)** | 角度引導/單軸水平/三分網格/Ambient UI 外觀/鏡頭髒汙/每日 5 張評分/每月 3 次 PTF | 可學會 → 建立信任 |
| **付費 $2.99/月 (Retention)** | 構圖即時診斷/人像六規則/FoodCompositionRule/光線/減法留白/無限 PTF/拍後 DOF/無限評分 | 不可學會 → 持續需要 |

### Paywall 視覺

拍後教學卡片：免費版構圖/光線/診斷文字 **blur(3dp)**，只看得到角度/水平分數。
底部 CTA: "Unlock full diagnosis"

---

## PART 16：Paywall 模組 Interface（CSO 預定義）

> 以下 interface 在基礎穩定後實作，但結構在票拆分時就需要存在。

```kotlin
// Domain 層
interface FreeTierLimitTracker {
    fun incrementDailyScore()
    fun canScore(): Boolean          // 每日 ≤ 5
    fun getRemainingCount(): Int
    fun incrementPtfUse()
    fun canUsePtf(): Boolean         // 每月 ≤ 3
}

interface PaywallTrigger {
    fun shouldShowPaywall(event: PaywallEvent): Boolean
}

enum class PaywallEvent {
    SCORE_LIMIT_REACHED,      // 第 6 張拍照
    PTF_LIMIT_REACHED,        // 第 4 次 PTF
    PAID_FEATURE_TAPPED       // 點擊付費功能
}
```

---

## PART 17：Gemini 架構重審指令

> 以下段落供 Gemini Stage 1 使用，定義焦土掃描的範圍與產出格式。

### 掃描目標

1. **焦土盤點**：掃描 `app/src/main/` 全目錄，列出：
   - 可直接使用的穩定檔案（與本規格對齊）
   - 需修改的檔案（存在但邏輯過時或命名衝突）
   - 需刪除的殘留檔案（bad1400 / v2.12.0-24 殘留、已作廢的 PtfTabBar 等）
   - 缺失的檔案（規格要求但尚未建立）

2. **基礎健康檢查**：
   - CameraX PreviewView binding 是否正常
   - ML Kit Face Detection 初始化是否正常
   - Hilt DI module 是否完整
   - `build.gradle` 依賴是否對齊（ML Kit, CameraX, Compose BOM 版本）
   - emulator 上 `assembleDebug` 是否通過

3. **架構對齊度**：
   - `CompositionResult` 是否已取代 `CompositionScore`
   - `PriorityEngine` 是否為獨立 class
   - `isAllAligned` 是否使用動態聚合 `rules.all { ... }`
   - Clean Architecture 層次（domain/data/presentation）是否正確分離

### 產出格式

```markdown
# 焦土掃描報告

## 1. 檔案盤點
| 檔案路徑 | 狀態 | 行動 | 備註 |
|---------|------|------|------|
| `path/to/file.kt` | ✅穩定 / ⚠️需改 / ❌刪除 / 🆕缺失 | 具體行動 | — |

## 2. 基礎健康
| 檢查項 | 狀態 | 問題描述 |
|--------|------|---------|

## 3. 架構對齊度
| 規格要求 | 現狀 | 差距 |
|---------|------|------|

## 4. 粗拆票建議
| 票序 | 範圍 | 前置依賴 | 估算 |
|------|------|---------|------|
```

---

## PART 18：架構壓力測試 Checklist（Phase B 用）

> 提取自 Traycer Ultimate Defensive Workflow v2.0.1 `architectural_review` Command。
> 供 GPT-5.5 Phase B 使用，搭配焦土報告逐項驗證。

### C1: 硬體極限

- [ ] 前鏡頭 Fixed-Focus bypass：只追求視覺成功，不等物理對焦
- [ ] VCM 馬達延遲：高頻視覺追蹤 (10fps) 與低頻馬達呼叫 (冷卻 2-3s) 脫鉤
- [ ] ML Kit 互斥：Face Detection 和 Object Detection 不可同時開啟，切換需 500ms hysteresis
- [ ] IMU 頻率脫鉤：Edge Warmth 60fps 渲染 ← IMU 200Hz 取樣，兩者獨立管線

### C2: 狀態機防禦

- [ ] Sealed Class 隔離：前一場景的 target state 不可污染新場景
- [ ] 場景切換 hysteresis 500ms 防跳動（AmbientConstants.HYSTERESIS_SCENE_MS）
- [ ] Pill Toggle Photo↔PTF：純 Compose state 切換，零 CameraX rebind，零 ML Kit close/reopen
- [ ] 呼吸動畫 × 快門閃爍互斥：isBreathing flag 控制，不可同時播放
- [ ] Level Traction 漸入退場：0°→5° opacity 線性映射，非二元開關
- [ ] 橫拍 Level Traction：MVP 階段隱藏（不做比做錯好）

### C3: UDF 解耦（Unidirectional Data Flow）

- [ ] Compose UI 透過 StateFlow 觀察 ViewModel，不阻塞硬體 AF callback
- [ ] 無 `runBlocking`、無直接 callback chain 從 CameraX 到 Compose
- [ ] CameraX callback 在背景 Executor 執行，Compose recomposition 在 Main thread
- [ ] ML Kit 結果透過 Channel 或 StateFlow 傳遞，不在 Analyzer callback 中直接更新 UI

### C4: 三級裝置降級

- [ ] DeviceTierClassifier 正確分類 FULL / LIMITED / LEGACY
- [ ] Edge Warmth 60fps 在所有等級都保證（鐵律）
- [ ] LEGACY 群組 Halo 降級為最大人臉單人 Halo
- [ ] LEGACY 快門冷卻 500ms + 外環閃爍（FULL/LIMITED 為 300ms）
- [ ] LIMITED 關閉雜亂框，保留減法留白
- [ ] LEGACY 關閉髒汙 + 減法留白 + 雜亂框 + 光線分析

### C5: Paywall 邊界檢查（CSO 追加）

- [ ] FreeTierLimitTracker interface 是否已定義或預留位置
- [ ] 付費功能（FoodCompositionRule / LightRule / MinimalistRule）是否可透過 flag 控制啟用
- [ ] 評分寫入 EXIF 的路徑是否預留 Paywall 攔截點
- [ ] blur overlay 的 Compose 層級是否在正確的 z-order

---

**End of Merged Spec v3.4.1-Stage0**
