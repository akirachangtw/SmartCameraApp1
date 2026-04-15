# Cueshot D7 — Gemini 架構師技術提醒與補充事項

**用途**: D7 交接單產出前貼給 Gemini
**前置**: Gemini 已啟動架構師角色 + 已讀 D6 封板狀態 + KAIROS #2 已執行
**Sprint Day**: D7 (2026-04-16) — 非 KAIROS 節點
**CSO 產出日期**: 2026-04-15

---

## 0. D6 封板作戰履歷 (Combat Log)

### D6 里程碑總覽：✅ COMPLETE — 人像構圖指導全線竣工

| 維度 | D5 封板 → D6 封板 | 增量 |
|------|------------------|------|
| Sprint Tests | 133 → 228+ | +95 (+71%) |
| CLAUDE.md Rules | 12 → 16 | +4（Rules 13/14/15/16） |
| Domain Rules | 2（Thirds + Headroom） → 4（+ EyeLine + Gaze） | +2 |
| 新建核心元件 | — | FaceLandmarkData, EyeLineRule, GazeRule, HaloGuidanceLayer, EyeAfController |
| KAIROS #2 | — | ✅ EXECUTED |

### D6 Batch 交付明細

| Batch | 內容 | CSO | Gemini | Traycer |
|-------|------|-----|--------|---------|
| B0 | PROGRESS.md + Backlog 登記（SSOT 型別偏差） | ✅ | ✅ | N/A |
| B1-T1 | FaceLandmarkData + CompositionRule 介面升級 + D6_PROBE 探針 | ✅ | ✅ | ✅ execute_review |
| B1-T1.6 | GazeRule 極端角度 Fail-safe 熱修復（Priority Inversion 防禦） | ✅ | ✅ | ✅ |
| B1-T2/T3 | EyeLineRule + GazeRule 新建（推定已完成，見 Handoff） | ✅ | ✅ | — |
| B2 | HaloGuidanceLayer 視覺牽引（推定已完成，見 KAIROS #2） | ✅ | ✅ | — |
| B3 | EyeAfController — 6 道 Guard + 2s 冷卻 + 9 條測試 | ✅ | ✅ | — |
| KAIROS #2 | /dream 回顧 → Rules 13/14/15/16 收割 | ✅ | ✅ | — |

### D6 關鍵架構決策（D7 必須遵守）

| 決策 | 內容 | D7 影響 |
|------|------|--------|
| EyeAfController 非 DI | private member 於 ViewModel，無 Hilt 注入 | D7 PTF 若需暫停 Eye-AF → 呼叫 `eyeAfController.reset()` |
| focusOnPoint 簡單路徑 | 繞過 CameraXFocusController，直接走 cameraRepository | D7 不需碰對焦路徑 |
| lastTriggerMs 哨兵值 | `Long.MIN_VALUE / 2`（Rule 16 鐵律） | D7 任何新的節流機制必須遵守同一模式 |
| isAligned=true Fail-safe | 極端角度（>45°）回傳 aligned 而非低分 | D7 PTF 的 FramingRule 若遇邊界情況同理 |

### D6 遺留技術債（D7 待清）

| 項目 | 優先級 | 預估工時 | 依賴 |
|------|--------|---------|------|
| D5-TECH-DEBT-01: HDRWarningOverlayTest 3 條失敗 | **P0（D7 開工首項）** | 30 min | 改用 mutableStateOf |
| D5-TECH-DEBT-01: EdgeWarmthLayerTest Sony race condition | **P0** | 20 min | 加入 waitForIdle() |
| MAX_YAW → GUIDANCE_MAX_YAW_DEG + EXTREME_YAW_INVERSION_DEG 語意拆分 | P1 | 15 min | GazeRule.kt + KDoc |
| SSOT-TYPE-DRIFT-01: RuleScore 命名統一 | P2 | 20 min | SSOT 文件層 |
| D6_PROBE 探針移除 | P1 | 10 min | CameraViewModel.kt |
| ruleScores["gaze"] magic string → GazeRule.NAME | P1 | 10 min | EyeAfController + GazeRule |
| Gradle CC generateTestImages | P3（D8+） | 30 min | build.gradle.kts |

---

## 1. KAIROS #2 收割成果（D7 強制遵守的新鐵律）

### 新增 Rules 13-16

| Rule | 內容 | D7 落地影響 |
|------|------|-----------|
| **Rule 13** | Android SDK JVM 測試：`isReturnDefaultValues = true` 或 mock 所有 SDK 呼叫 | D7 PTF 新測試必須遵守 |
| **Rule 14** | Compose slot-table 首幀：狀態寫入必須在 Composition 建立前完成 | D7 PTF Tab toggle 的 `remember { mutableStateOf }` 必須置頂 |
| **Rule 15** | KDoc 呼叫鏈準確性：`@see` 標註必須指向真實呼叫路徑 | D7 新建元件的 KDoc |
| **Rule 16** | 硬體呼叫雙重門控：前鏡頭 Guard + 節流 Guard 缺一不可，哨兵值 `Long.MIN_VALUE / 2` | D7 PTF 快門冷卻（1s）必須遵守此模式 |

CLAUDE.md 當前規則數：**16 條 + Bash 安全**。防禦網持續強化。

---

## 2. D7 核心任務：PTF 遞手機模式 UI

### 2.1 PTF 戰略定位（CSO 視角）

PTF (Pass-to-Friend) 是 Cueshot 的**最高痛點場景**。用戶畫像：在餐廳聚餐，想要一張好看的團體照，把手機遞給不太會拍照的朋友。**速度優先於確認** — 朋友正在等，不能有學習成本。

**D7 的交付目標**：純 UI 骨架 + 狀態切換。D7 不建 PTF 規則引擎（那是 D8），D7 只確保「遞出手機時，UI 瞬間變成最簡模式」。

### 2.2 PTF UI 規格（SSOT §2.6）

```
┌─────────────────────────────────────┐
│  [Normal Mode]                      │
│  ┌─ Top Bar: 場景/設定 ────────────┐│
│  │                                 ││
│  │    Camera Preview               ││
│  │    + Edge Warmth                ││
│  │    + Halo                       ││
│  │                                 ││
│  └─────────────────────────────────┘│
│  [●] Shutter                        │
│  ╔═══════════════════════════════╗  │
│  ║  Normal │ PTF    ← Tab Bar   ║  │
│  ╚═══════════════════════════════╝  │
└─────────────────────────────────────┘

         ↓ 點擊 PTF Tab（首次觸發 Bottom Sheet 教學 20%）

┌─────────────────────────────────────┐
│  [PTF Mode]                         │
│  ┌─────────────────────────────────┐│
│  │                                 ││
│  │    Camera Preview               ││
│  │    + PTF Halo (D8)             ││
│  │    + Edge Warmth (共用)         ││
│  │                                 ││
│  │                        [✕] ←────── Exit (右上)
│  └─────────────────────────────────┘│
│                                      │
│        [●●●] 80dp 大快門             │
│         1s 冷卻防連拍                │
│                                      │
│  ╔═══════════════════════════════╗  │
│  ║  Normal │ PTF    ← Tab Bar   ║  │
│  ╚═══════════════════════════════╝  │
└─────────────────────────────────────┘
```

### 2.3 狀態模型

```kotlin
// 建議的 Sealed Interface（請架構師裁決）
sealed interface CameraMode {
    data object Normal : CameraMode
    data object PassToFriend : CameraMode
}

// ViewModel 持有
private val _cameraMode = MutableStateFlow<CameraMode>(CameraMode.Normal)
val cameraMode: StateFlow<CameraMode> = _cameraMode.asStateFlow()
```

**關鍵約束 — 零 CameraX Rebind**：

Normal → PTF 切換**只是 UI 狀態變更**，CameraX Preview 不重新綁定，ML Kit 管線不中斷。這是因為：
- PTF 仍然需要 ML Kit Face Detection（D8 的 FramingRule）
- 重新綁定 CameraX 會造成 0.5-1s 黑屏，遞手機場景不可接受
- Edge Warmth 在 PTF 模式仍然作用（共用 IMU 管線）

### 2.4 Tab Bar 規格

| 屬性 | 值 | 備註 |
|------|---|------|
| 位置 | 底部全寬 | 快門按鈕之下 |
| 高度 | 36dp | 足夠觸擊，不佔太多空間 |
| 切換方式 | Tap 對應 Tab | 無 swipe gesture |
| 動畫 | 選中指示器 300ms 滑動 | Compose AnimatedContent |
| 退出 PTF | Tab 回 Normal **或** ✕ 按鈕 | 雙路退出（Roadmap D7-T1 規格） |

### 2.5 PTF 模式 UI 差異

| 元素 | Normal Mode | PTF Mode |
|------|------------|----------|
| Top Bar | 顯示（場景切換、設定入口） | **隱藏**（Animated out 300ms） |
| Shutter 大小 | 標準（56dp） | **80dp**（朋友更容易按到） |
| Shutter 冷卻 | 無（或很短） | **1s**（防連拍，Rule 16 模式） |
| Halo | Normal Halo（三分交叉點牽引） | **PTF Halo — D8 建（居中引力場）** |
| Edge Warmth | 共用 | 共用 |
| ✕ 退出按鈕 | 無 | **右上角，無文字標籤** |
| 首次教學 | 無 | **Bottom Sheet 20% 高度，一次性顯示** |

### 2.6 首次使用教學（Bottom Sheet）

**觸發條件**：第一次點擊 PTF Tab（SharedPreferences flag `ptf_onboarded`）
**內容**：簡短文字 + 3 秒循環動畫，說明 PTF 模式的使用方式
**高度**：螢幕 20%（peek height）
**消失條件**：用戶點擊「了解」或背景區域

---

## 3. D7 技術風險

### 3.1 Tab Bar 與快門按鈕的佈局衝突

底部區域同時容納 Tab Bar (36dp) + Shutter Button (Normal 56dp / PTF 80dp)。需確認垂直空間是否足夠，特別是在小螢幕裝置（5" / LIMITED tier）。

建議：Tab Bar 在 Shutter 之下，固定於 bottomBar slot。Shutter 在 content 區域最底部，兩者不重疊。

### 3.2 PTF 模式下的 PriorityEngine 行為

D7 只建 UI，不建 PTF 規則引擎（D8）。但 PriorityEngine 在 PTF 模式下仍會運算（因為 ML Kit 管線沒斷）。問題：Normal 模式的 4 規則（Thirds/Headroom/EyeLine/Gaze）在 PTF 場景可能給出不適當的指令（例如 ThirdsRule 說「移到三分線」，但 PTF 要的是居中）。

**⚠️ 架構問題 F（請架構師判斷）**：

- 方案 1：D7 PTF 模式暫時禁用 topInstruction 顯示（UI 層過濾 `if (cameraMode == PTF) hide`），D8 再接入 PTF 專用規則
- 方案 2：D7 PTF 模式切換時，替換 Rule Set（Hilt 動態切換）

CSO 強烈傾向方案 1 — D7 是純 UI 日，不應觸碰 Domain 層。UI 層的 `when (cameraMode)` 過濾足夠乾淨，D8 再做完整方案。

### 3.3 快門冷卻 1s 的實作模式

Roadmap 規定 PTF 快門冷卻 1s。根據 Rule 16（硬體呼叫雙重門控），實作必須：
- 哨兵值：`Long.MIN_VALUE / 2`（非 0L）
- Guard 結構：`if (nowMs - lastShutterMs < COOLDOWN_MS) return`
- 負向測試：必須有「冷卻期內按快門被拒絕」的單元測試

這與 EyeAfController 的 2s 冷卻是同一模式，但參數不同（1s vs 2s）。建議考慮是否抽出共用的 `ThrottleGuard` 工具類。

### 3.4 SharedPreferences 的首次教學 flag

`ptf_onboarded` flag 需要持久化。CameraViewModel 已注入 `@ApplicationContext`？如果沒有，需要確認 DI 路徑。或者可以用 DataStore（更現代），但 MVP 階段 SharedPreferences 更簡單。

---

## 4. D7 Ticket 拆解建議

### Batch 0：技術債清理（D7 開工首項）

| Ticket | 內容 | 性質 | 模式建議 |
|--------|------|------|---------|
| D7-DEBT-01 | HDRWarningOverlayTest 重寫 3 條測試（mutableStateOf） | 修復 | /uc |
| D7-DEBT-02 | EdgeWarmthLayerTest Sony race condition（waitForIdle） | 修復 | /uc |
| D7-DEBT-03 | MAX_YAW → GUIDANCE_MAX_YAW_DEG + EXTREME_YAW_INVERSION_DEG 語意拆分 | 重命名 | /uc |
| D7-DEBT-04 | D6_PROBE 探針移除 + ruleScores["gaze"] → GazeRule.NAME | 清理 | /uc |

### Batch 1：PTF 狀態模型 + Tab Bar

| Ticket | 內容 | 性質 | 模式建議 |
|--------|------|------|---------|
| D7-B1-T1 | `CameraMode` Sealed Interface + ViewModel StateFlow + Tab Bar UI | 新建 | 先 /normal 再 /uc |
| D7-B1-T2 | Normal → PTF 切換動畫 300ms（AnimatedContent / Crossfade） | 新建 | /uc |
| D7-B1-T3 | PTF 模式下 topInstruction 顯示過濾（UI 層 `when (cameraMode)`） | 修改 | /uc |

### Batch 2：PTF 模式 UI

| Ticket | 內容 | 性質 | 模式建議 |
|--------|------|------|---------|
| D7-B2-T1 | PTF Mode UI（Top Bar 隱藏 + 80dp 快門 + ✕ Exit） | 新建 | /uc |
| D7-B2-T2 | 快門 1s 冷卻節流（Rule 16 模式 + 負向測試） | 新建 | 先 /normal 再 /uc |
| D7-B2-T3 | 首次 PTF 教學 Bottom Sheet（SharedPreferences flag + 3s 動畫循環） | 新建 | /uc |

### Batch 3：回歸驗證

| Ticket | 內容 | 性質 |
|--------|------|------|
| D7-B3-T1 | 全量回歸：D4 BreathingRing + D6 Halo + Eye-AF 在 Normal 模式仍正常 | 驗證 |
| D7-B3-T2 | `/qa` → `/sv` → Traycer execute_review × N → strict_verification | 驗收 |

---

## 5. 架構師決策請求摘要

| 問題 | CSO 傾向 | 請架構師裁決 |
|------|---------|------------|
| **F (PTF 模式 PriorityEngine 行為)** | 方案 1：D7 UI 層過濾 topInstruction，D8 再接 PTF 規則 | 是否同意不動 Domain 層？ |
| **CameraMode 型別** | Sealed Interface（可擴充 GroupPhoto 等未來模式） | Sealed Class 或 Sealed Interface？ |
| **快門冷卻共用 ThrottleGuard** | D7 不抽（只有 1 個冷卻點），D8+ 若出現第 3 個冷卻點再抽 | 是否同意暫不重構？ |
| **教學 flag 持久化** | SharedPreferences（MVP 最簡） | SharedPreferences 或 DataStore？ |
| **技術債清理順序** | Batch 0 先清（測試紅燈 → 命名 → 探針），再開主線 | 是否同意此優先級？ |

**請架構師確認上述裁決 + Ticket 拆解後，產出 D7 交接單。**

---

## 文件元資訊

- **文件版本**: D7-Briefing-v1.0-20260415
- **建立日期**: 2026-04-15
- **作者**: CSO 策略分析 (Claude Opus 4.6) + 開發者 Akira
- **前置文件**: D6 Session Handoff (20260415_1516), KAIROS #2 Report, Cueshot_MVP_Phase1_Roadmap_v1, gstack v1.3
- **下一步**: 貼給 Gemini 架構師，取得 D7 Batch 0-3 交接單後開始實作
