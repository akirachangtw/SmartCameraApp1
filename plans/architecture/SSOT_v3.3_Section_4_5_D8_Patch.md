# SSOT v3.3 §4.5 D8 實作補丁

**文件版本**: SSOT-v3.3-§4.5-Patch-20260416
**性質**: 臨時補丁文件（Temporary Patch）— 非正式 SSOT 更新
**前置文件**: SmartCameraApp_Strategy_v3_3_SSOT_20260404.md §4.5
**適用範圍**: D8 Sprint 期間（2026-04-16 至 D8 封板為止）
**作者**: CSO 策略分析 (Claude Opus 4.6) + 開發者 Akira
**失效條件**: 本補丁在 D10 KAIROS 正式升級 SSOT v3.4 時作廢，內容併入 SSOT 本體

---

## 第一部分：補丁背景與動機

### 1.1 為什麼需要這份補丁

在 D8-B1 交接單覆檢過程中發現，SSOT v3.3 §4.5「遞手機模式規則」相較於 §4.3（美食）與 §4.4（人像六規則）存在嚴重的資訊密度不對稱：

| 章節 | 規則數 | 有數據源? | 有容忍度? | 有計算公式? | 有延遲預算? | 有付費層? |
|------|-------|----------|----------|------------|------------|----------|
| §4.3 美食模式 | 5 | ✅ | ✅ | ✅ | ✅ | ✅ |
| §4.4 人像模式 | 6 | ✅ | ✅ | ✅ | — | — |
| §4.5 PTF 模式 | 3 | ❌ | ❌ | ❌ | ❌ | ❌ |

§4.5 原文僅有三行自然語言描述，完全缺乏讓 Gemini 架構師與 Claude Code 可直接實作的規格密度。

### 1.2 補丁定位

**本補丁不修改 SSOT v3.3 本體**。原因：
- SSOT 是最高憲法級文件，D8 正在實作期，此時更新會造成已產出的 Gemini 交接單 / CSO Briefing 版本錯亂
- SSOT 更新應走正規流程（版本號升級 v3.3 → v3.4），此為架構決策
- D8 實作過程中可能發現更多 SSOT 缺口，等 D8 結束後一次補齊更有效率

**本補丁的效力**：
- D8 對話 1（/normal 架構對齊）必須以本補丁為準
- D8 對話 2（/uc 實作）必須依本補丁定義的介面與數值
- D10 KAIROS 回顧時，本補丁內容與其他 SSOT 留白發現一併升級為 v3.4

### 1.3 與 CLAUDE.md Rule 5（Anti-Drift）的關係

Rule 5 原本防禦的是「實作偏離 SSOT」。本補丁揭示一個新的漂移風險類型：

> **SSOT 本身的資訊留白，會導致實作在留白處自由發揮 — 這不是實作偏離 SSOT，而是 SSOT 沒給實作足夠的約束。**

此發現列為 **D10 KAIROS 候選 Rule 17（SSOT 留白檢測原則）** 的前置證據。

---

## 第二部分：SSOT §4.5 原文（保留參照）

以下為 SSOT v3.3 §4.5 的完整原文，不做任何修改，僅作為補丁的對照基準：

```
### 4.5 遞手機模式規則

簡化版，只保留三條：FramingRule（居中）+ AngleRule（粗檢）+ LevelRule（粗檢）。
UI 使用 Halo 牽引 + Edge Warmth。對齊後 Halo 金色→綠色 + 呼吸。
```

---

## 第三部分：§4.5 補強版（D8 實作依據）

### 3.1 設計哲學

> **朋友不是攝影師，容忍度必須拉寬。**

PTF 模式的核心假設：手持手機的人（朋友）不具備攝影訓練，對構圖、角度、水平的直覺敏銳度顯著低於 App 主人。因此：

1. **容忍度設計**：比 Normal 模式寬鬆 1.5-2 倍
2. **聚合邏輯**：採 **AND 閘**（三規則全過才算對齊），**不使用** Normal 模式的 Priority 權重排序
3. **速度優先於精度**：PTF 場景使用者心智是「趕快拍完還我手機」，一屏多指令會造成認知過載
4. **視覺引導 > 文字指令**：Halo 牽引 + Edge Warmth 足以傳遞資訊，無需文字 overlay

### 3.2 完整規則規格表

| 規則 | 數據源 | 計算邏輯 | 容忍度 | 延遲 | 前鏡頭適用 |
|------|--------|---------|--------|------|-----------|
| **FramingRule** | ML Kit Face Detection `face.boundingBox.center` | 人臉中心 vs 畫面中心歐氏距離（view-space px） | < 螢幕對角線 12% | < 2ms | 不適用（PTF 預設後鏡頭，見 §3.6） |
| **AngleRule (PTF 粗檢)** | IMU pitch | `pitch ∈ [-15°, 25°]` | 40° 寬友善範圍 | < 2ms | 適用 |
| **LevelRule (PTF 粗檢)** | IMU roll | `abs(roll) < 8°` | 比 Normal 模式的 5° 寬鬆 | < 2ms | 適用 |

**容忍度設計對照**（凸顯 PTF 模式的寬容性）：

| 規則 | Normal 模式容忍度 | PTF 模式容忍度 | 寬容倍數 |
|------|------------------|----------------|---------|
| Angle | 對齊目標 ±5°（如 45°/60°/90°） | `pitch ∈ [-15°, 25°]` 全範圍 | ~4x |
| Level | `abs(roll) < 5°` | `abs(roll) < 8°` | 1.6x |
| Framing | 三分交叉點容忍 8% | 畫面中心容忍 12% | 1.5x |

### 3.3 聚合邏輯（AND 閘，非 Priority 權重）

```kotlin
// domain/composition/PtfRuleAggregator.kt

class PtfRuleAggregator @Inject constructor(
    private val framingRule: PtfFramingRule,
    private val angleRule: PtfAngleRule,
    private val levelRule: PtfLevelRule
) {
    fun aggregate(
        face: Face?,
        pitch: Float,
        roll: Float,
        viewSize: Size
    ): PtfAlignmentResult {
        val framingResult = framingRule.evaluate(face, viewSize)
        val angleResult = angleRule.evaluate(pitch)
        val levelResult = levelRule.evaluate(roll)

        val isAligned = framingResult.aligned
                     && angleResult.aligned
                     && levelResult.aligned

        val failedRules = buildList {
            if (!framingResult.aligned) add(PtfRuleName.FRAMING)
            if (!angleResult.aligned) add(PtfRuleName.ANGLE)
            if (!levelResult.aligned) add(PtfRuleName.LEVEL)
        }

        // 牽引向量：face 中心 → 畫面中心的偏移
        val pullVector = if (face != null && !isAligned) {
            val center = Offset(viewSize.width / 2f, viewSize.height / 2f)
            val faceCenter = Offset(
                face.boundingBox.exactCenterX(),
                face.boundingBox.exactCenterY()
            )
            center - faceCenter
        } else {
            Offset.Zero
        }

        return PtfAlignmentResult(
            isAligned = isAligned,
            pullVector = pullVector,
            failedRules = failedRules
        )
    }
}
```

**關鍵決策說明**：

| 決策 | 選擇 | 理由 |
|------|------|------|
| 聚合方式 | AND 閘 | PTF 語意是「全部對齊才拍」，Priority 會輸出單一指令誤導朋友 |
| 與 PriorityEngine 關係 | 獨立，不共用介面 | 避免 PTF 寬鬆容忍度污染 Normal 模式規則 |
| failedRules 順序 | FRAMING → ANGLE → LEVEL | 依據「用戶視覺感知優先級」（居中最明顯，水平最隱性） |

### 3.4 牽引向量輸出介面

```kotlin
// domain/composition/PtfAlignmentResult.kt

import androidx.compose.ui.geometry.Offset

/**
 * PTF 規則引擎聚合結果
 *
 * @property isAligned 三規則（Framing + Angle + Level）是否全部對齊
 * @property pullVector 牽引向量（view-space px），從 face bbox 中心指向畫面中心的偏移量
 *                     - 未偵測到人臉 → Offset.Zero
 *                     - 已對齊 → Offset.Zero（Halo 進入呼吸韻律）
 *                     - 未對齊 → 實際偏移向量（Halo 以此驅動引力場動畫）
 * @property failedRules 未通過的規則清單（依視覺感知優先級排序：Framing > Angle > Level）
 *                      用於 UI 顯示除錯資訊或未來做 A/B 測試
 */
data class PtfAlignmentResult(
    val isAligned: Boolean,
    val pullVector: Offset,
    val failedRules: List<PtfRuleName>
)

enum class PtfRuleName {
    FRAMING, ANGLE, LEVEL
}
```

**座標系規範**：
- `pullVector` 單位為 **view-space px**（非 normalized 0-1，非 sensor-space）
- 原點為畫面左上角（Compose Canvas 預設座標系）
- 正向量含義：face 需要往該方向移動以對齊畫面中心

### 3.5 UI 映射規格

| 狀態 | Halo 顏色 | Halo 行為 | Edge Warmth |
|------|----------|----------|-------------|
| 未偵測人臉 | — | 不顯示 | 綠色微光（PTF 場景底色） |
| 未對齊（`isAligned == false`）| 金色 `#D4A030` | 跟隨 face bbox + pullVector 驅動引力場動畫 | 綠色方向性暖光 |
| 已對齊（`isAligned == true`）| 綠色 `#50C864` | 均勻圓形 + 2s 週期呼吸韻律 | 綠色四邊均勻呼吸 |

**色碼來源**：與 SSOT §2.3 / §2.4 完全一致，PTF 模式無自訂色彩。

**引力場動畫規格**（Halo 偏亮側計算）：
```kotlin
// 參照 SSOT §2.4 HaloGuidanceLayer
val angle = atan2(pullVector.y, pullVector.x)
val brightOffset = Offset(
    haloCx - cos(angle) * 3f,  // 3dp 偏亮位移
    haloCy - sin(angle) * 3f
)
// 在 brightOffset 位置繪製偏亮側的加強弧
```

### 3.6 前鏡頭適用性

**結論：PTF 模式強制使用後鏡頭，不支援前鏡頭切換。**

理由：
1. **使用場景語意衝突**：PTF 是「遞手機給朋友幫你拍」，99% 為後鏡頭場景
2. **前鏡頭鏡像預覽干擾**：pullVector 方向在前鏡頭鏡像下語意反轉，會誤導朋友
3. **Framing 基準不一致**：前鏡頭的「儲存原始自拍」設定（Tech Deep Dive §5.1）會讓 FramingRule 結果在預覽與成片之間不一致

**實作防禦**：
```kotlin
// CameraViewModel.kt（偽代碼）
fun enterPtfMode() {
    if (currentCameraDirection == CameraDirection.FRONT) {
        switchToBackCamera()  // 強制切後鏡頭
    }
    isPtfMode = true
}
```

PTF 模式啟動時若當前為前鏡頭，自動切換至後鏡頭，不提供選項。此行為寫入 Tech Deep Dive §4.6 PTF 切換邏輯的附加約束。

### 3.7 延遲預算

| 階段 | 延遲 | 說明 |
|------|------|------|
| IMU 讀取 + 平滑 | ~2ms | 與 Normal 模式共用 ImuReader |
| ML Kit Face Detection | 30-60ms | 與 Normal 模式共用，PTF 模式僅取 bbox，不需 landmarks |
| PtfRuleAggregator.aggregate() | < 2ms | 三個 IF 判斷 + 一次 Offset 計算 |
| Halo UI 渲染 | < 16ms | 60fps frame budget 內 |
| **端到端峰值** | **~80ms** | 優於 Normal 模式的 65ms（因為規則更少） |

### 3.8 付費層歸屬

**PTF 規則引擎本身為 Paid 功能**，與 SSOT §8.1/§8.2 的「遞手機模式：每月 3 次免費 / 無限」限制同層。

實作含義：
- Free 用戶每月前 3 次可完整體驗 PTF 規則引擎（Halo 牽引 + Edge Warmth + 對齊回饋）
- 第 4 次進入 PTF Tab → Toast「PTF limit reached. Upgrade to Pro.」，不啟動規則引擎
- Paid 用戶無次數限制

**與 Normal 模式付費邏輯的區隔**：
- Normal 模式：人像/美食構圖規則需要 Paid，但 Free 用戶仍有角度引導
- PTF 模式：**整個模式**在次數用完後完全不可用（無降級版）

### 3.9 架構隔離要求

```
┌─────────────────────────────────────────────────────────┐
│                    CameraViewModel                       │
│  ┌───────────────────────────────────────────────────┐  │
│  │ if (cameraMode is CameraMode.Ptf)                 │  │
│  │     → PtfRuleAggregator                           │  │
│  │ else (cameraMode is CameraMode.Normal)            │  │
│  │     → PriorityEngine                              │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
          │                                   │
          ▼                                   ▼
┌──────────────────────┐          ┌──────────────────────┐
│ PtfRuleAggregator    │          │ PriorityEngine       │
│ (AND 閘)             │          │ (Weighted Priority)  │
│                      │          │                      │
│ • PtfFramingRule     │          │ • ThirdsRule         │
│ • PtfAngleRule       │          │ • HeadroomRule       │
│ • PtfLevelRule       │          │ • EyeLineRule        │
│                      │          │ • GazeRule           │
│ 容忍度：寬鬆          │          │ • AngleRule (Normal) │
│                      │          │ • LevelRule (Normal) │
└──────────────────────┘          └──────────────────────┘
          │                                   │
          └───────────────┬───────────────────┘
                          ▼
                   ┌─────────────┐
                   │ HaloGuidanceLayer │  （SSOT §2.4 共用元件）
                   └─────────────┘
```

**隔離禁令**：
1. ❌ `PtfRuleAggregator` 不得實作 `CompositionAnalyzer` 介面
2. ❌ `PriorityEngine` 不得呼叫 PTF 規則
3. ❌ PTF 規則不得繼承 Normal 模式規則類別（例如 `PtfAngleRule` 不可 extend `AngleRule`）
4. ✅ HaloGuidanceLayer 可共用（僅 UI 表現層，業務邏輯已隔離）
5. ✅ ML Kit Face Detection 結果可共用（僅數據源，不涉及規則邏輯）

### 3.10 與 D9 美食模式的前瞻性考量

**CSO 決策：D8 不抽出通用 BaseRuleAggregator，避免過早抽象。**

理由：
- D9 美食模式的 FoodRuleAggregator 目前未設計，可能採 Priority 而非 AND 閘
- 若現在抽骨架，極可能產生錯誤抽象（wrong abstraction 比 no abstraction 更昂貴）
- 正確時機是 D10 KAIROS，屆時已有 PTF + Food 兩個實作樣本，可歸納共通模式

此決策記錄為 D10 KAIROS 回顧議題。

---

## 第四部分：補丁的使用流程

### 4.1 D8 對話 1（/normal 架構對齊）

1. 開啟對話後載入以下文件：
   - 本補丁文件（`SSOT_v3.3_§4.5_D8_Patch.md`）
   - Gemini 產出的 D8-B1-T1 / D8-B1-T2 交接單
   - `SESSION_HANDOFF_20260416_1400.md`（D7 封板狀態）
   - `CLAUDE.md`（架構規則，特別是 Rule 5 / Rule 13 / Rule 16）

2. 對齊議題依 D8-B1 修正指示的 §3.5 六大議題逐項討論：
   - 議題 1：PtfRuleAggregator vs PriorityEngine 隔離邊界 → 參照本補丁 §3.9
   - 議題 2：PtfAlignmentResult 資料結構 → 參照本補丁 §3.4
   - 議題 3：Halo 元件共用策略 → 參照本補丁 §3.5 + §3.9
   - 議題 4：face bbox 抖動對沖（EMA α=0.3 沿用）→ 本補丁未定義，依 /normal 討論結論
   - 議題 5：D9 美食模式前瞻性 → 參照本補丁 §3.10
   - 議題 6：Rule 16 共用 ThrottleGuard → 本補丁未定義，依 /normal 討論結論

3. 產出 `D8_B1_Architecture_Memo.md`，附加本補丁作為規格依據

4. 產出對話 2 冷啟動指令

5. **停止動作**，等候 Akira 檢視

### 4.2 D8 對話 2（/uc 實作）

1. 依架構備忘錄實作 T1（Aggregator 骨架 + DI + PtfAlignmentResult）
2. `/qa` 驗證 T1
3. 實作 T2（三規則 + 負向測試）
4. `/qa` → `/sv` → Traycer 終審
5. D8-B1 封板 → PROGRESS.md 更新

### 4.3 補丁失效時機

本補丁在以下任一條件成立時作廢：
- D10 KAIROS 完成 SSOT v3.4 升級，§4.5 內容併入 SSOT 本體
- D8 實作過程發現本補丁內容需重大修訂（觸發 mini-KAIROS）

---

## 第五部分：未解決的待辦項（交給 /normal 對話 1 討論）

以下項目本補丁**刻意不定義**，留給 D8 對話 1 架構對齊時討論決策：

| 議題 | CSO 傾向 | 待討論原因 |
|------|---------|-----------|
| face bbox EMA 平滑係數 | 沿用 D6 的 α=0.3 | PTF 場景朋友手抖可能更嚴重，但無實機數據支撐提高 α |
| PTF 是否需要獨立 ThrottleGuard | 先不抽，D10 KAIROS 評估 | 避免過早抽象；Halo 位置更新頻率目前沿用 Rule 16 模式 |
| Halo 金→綠切換的轉場時長 | 建議 300ms（與 Normal 一致） | 需驗證是否與 PTF 的「快速對齊」節奏衝突 |
| 未對齊時 Edge Warmth 的方向性強度 | alpha 建議 0.12-0.18 | 比 Normal 模式的 0.20 弱（降低朋友焦慮感） |

---

## 第六部分：7 個缺口對照表（補丁完成度自檢）

對照上一對話發現的 7 個規格缺口，確認本補丁覆蓋狀況：

| 缺口編號 | 缺口內容 | 補丁章節 | 覆蓋狀態 |
|---------|---------|---------|---------|
| 缺口 1 | 數據源明確定義 | §3.2 規則規格表 | ✅ 完整 |
| 缺口 2 | 容忍度數值 | §3.2 + §3.2 對照表 | ✅ 完整 |
| 缺口 3 | 聚合邏輯（AND vs Priority）| §3.3 + §3.9 | ✅ 完整 |
| 缺口 4 | 延遲預算 | §3.7 | ✅ 完整 |
| 缺口 5 | 付費層歸屬 | §3.8 | ✅ 完整 |
| 缺口 6 | 前置鏡頭適用性 | §3.6 | ✅ 完整（加強為強制後鏡頭）|
| 缺口 7 | 牽引向量輸出規格 | §3.4 + §3.5 | ✅ 完整 |

**額外補強項**（超出原 7 個缺口）：
- §3.9 架構隔離圖與禁令（防 Gemini/Claude Code 誤抽象）
- §3.10 D9 前瞻性決策記錄（防 D9 實作時重啟爭論）
- §5 未解決待辦項清單（明確切分 CSO 定義 vs /normal 討論的界線）

---

## 文件元資訊

- **文件版本**: SSOT-v3.3-§4.5-Patch-20260416
- **建立日期**: 2026-04-16
- **作者**: CSO 策略分析 (Claude Opus 4.6) + 開發者 Akira
- **前置文件**:
  - SmartCameraApp_Strategy_v3_3_SSOT_20260404.md §4.5
  - SmartCameraApp_TechDeepDive_v3_20260405.md §4.5 PTF Tab 相關段落
  - Cueshot D8 啟動策略 Handoff（SSOT PTF完整性議題分析）
  - D8_B1_Gemini_Correction_Instructions.md（5 點修正指示）
- **作廢條件**: D10 KAIROS 升級 SSOT v3.4 後併入本體
- **下一步**: Akira 將本補丁 + 修正指示一併交給 Gemini，要求參考本補丁產出修正版 T1 + T2 交接單
