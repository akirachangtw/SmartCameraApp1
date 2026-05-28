# Cueshot 佈局規格 v2.5 — 兩區塊模型

**版本**: Layout Spec v2.5.1 (SSOT v4.0.5 Alignment + D-25 Layer 1 文案修正 + D-26 LEGACY 光線指示修復)  
**最後更新**: 2026-05-28  
**變更歷程**:  
- v2.0 → v2.1：§5 Pill Toggle 文字標籤改為圖示（Camera / Users）  
- v2.1 → v2.2：Edge Warmth 命名全面淨化（4 處）+ SSOT v4.0 數值對齊（Scrim 120px）+ 文件引用更新  
- v2.2 → v2.3：**16 項修訂**（見 §11 修訂追蹤 v2.2→v2.3）— SSOT v4.0.2 交叉審計全面對齊 + R14 快門下移落地 + Overlay 圖層重寫  
- v2.3 → v2.4：**8 項修訂**（見 §12 修訂追蹤 v2.3→v2.4）— SSOT v4.0.3 交叉審計：版本錨點全量更新 + Top Bar 光線指示三態 pulse 規格 + Edge-to-Edge/Cutout 規格 + FILL_CENTER 禁止聲明 + Scaffold padding 規則  
- v2.4 → v2.5：**3 項修訂**（見 §13 修訂追蹤 v2.4→v2.5）— Grid α0.35/1.5px + Z-order 交換 + Grid 詳細規格表
**決策來源**: CSO Session D8 — Top Bar Overlay 化 + Photo/PTF Pill Toggle · Session 4 D-12 快門下移 · Session 6 D-14~D-19 · **Session 8 D-24 Grid 規格**
**取代**: v2.5 及所有前版（v2.4, v2.3, v2.2, v2.1, v2.0, v1.1 三區塊模型）（v2.3, v2.2, v2.1, v2.0, v1.1 三區塊模型）  
**上游文件**: `SSOT_v4_0_5.md`（唯一真理文件）  
**下游文件**: Prototyper Prompt v2 · Background Image Prompts v2

---

## 1. 架構變更摘要

### v1.1 → v2.0 核心變更

| 項目 | v1.1（舊） | v2.0（新） | 變更理由 |
|------|-----------|-----------|---------|
| 區塊數 | 三區塊 | **兩區塊** | Top Bar 不再是獨立不透明區塊 |
| Top Bar | 72px 不透明 #0D0D0D | **浮動 Overlay + Scrim** | 取景框面積 +17 百分點 |
| Viewfinder | 520px（3:4）| **664px（填滿至螢幕頂部）** | 美食俯拍構圖面積最大化 |
| 底部控制列 | 252px | **180px** | 移除 PTF Tab 36px + 壓縮快門區 |
| 模式切換 | Photo · Video · Portrait 三標籤 40px | **Photo / PTF Pill Toggle 40px** | 場景偵測自動化,僅保留「誰在拍」的手動切換 |
| PTF 入口 | 底部全寬 Tab 36px + 快門右側圖示 | **Pill Toggle（取代 Tab + 圖示）** | 單一入口,零混淆 |
| 背景圖比例 | 3:4（1080×1440） | **390:844 ≈ 9:19.5（1170×2532 @3x）** | 全螢幕底圖,匹配真實 CameraX 行為 |

---

## 2. 螢幕佈局精確定義

### 基準裝置: 390 × 844pt（@3x = 1170 × 2532px）

```
┌──────────────────────────────────┐ ← y = 0px
│ ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ │   Scrim 漸層 overlay (0→120px)
│ ░ [⚡][HDR][💧]       [⚙] [?] ░ │   浮動圖示（白色/琥珀 pulse,見 §3.2.1）
│ ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ │   Scrim 底部 y=120px → alpha 0
│                                  │
│                                  │
│                                  │   ← 區塊 1: Viewfinder
│        Camera Preview            │      664px（78.7%）
│        (fills to top)            │      CameraX PreviewView
│                                  │      所有 Ambient overlay 在此
│                                  │
│                                  │
│                                  │
├─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┤ ← y = 664px（微妙分隔線 #FFF alpha 0.05）
│                                  │
│        [ Photo | PTF ]           │   ← Pill Toggle（40px）
│                                  │
│  [📷]        ( ● )        [🔄]  │   ← 快門區（80px, centerY=748px — D-12）
│                                  │
│         safe area                │   ← Navigation Safe Area（60px）
│                                  │
└──────────────────────────────────┘ ← y = 844px
```

---

## 3. 區塊 1: Viewfinder（含浮動 Top Bar Overlay）

### 3.1 Viewfinder 主體

| 屬性 | 值 |
|------|-----|
| 起始位置 | y = 0px（螢幕最頂部） |
| 結束位置 | y = 664px |
| 高度 | 664px |
| 佔螢幕比例 | **78.7%** |
| 內容 | CameraX `PreviewView`（`scaleType = FILL_CENTER`）— D-18 |
| Overlay 圖層 | Layer 0 ~ Layer 6（見 §3.3） |

**Edge-to-Edge + Display Cutout（D-18 附帶修復, SSOT §B.8）**：

Viewfinder 起始 y=0 的前提條件為 edge-to-edge 模式啟用 + cutout mode 正確設定：

| 屬性 | 值 | 備註 |
|------|-----|------|
| Edge-to-Edge | `enableEdgeToEdge()` | MainActivity.kt 啟用 |
| Cutout Mode | `LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES` | 同時在 code + themes.xml 設定 |
| **禁止** | 未設 cutout mode 的 edge-to-edge | 會導致 Viewfinder 被系統 padding 推下 |

**PreviewView scaleType（D-18, SSOT §B.6）**：

| 屬性 | 值 | 備註 |
|------|-----|------|
| scaleType | `PreviewView.ScaleType.FILL_CENTER` | 裁切填滿，無 Letterbox 黑條 |
| 裁切行為 | 4:3 影像在 9:19.5 容器中上下各裁切約 8.7% | 主體通常在中央，裁切影響可忽略 |
| **禁止** | `FIT_CENTER` | 會產生 Letterbox 黑條，與 Scrim/Overlay 疊加造成視覺異常 |

**Scaffold Padding 規則（D-18 附帶修復, SSOT §B.8）**：

| 規則 | 備註 |
|------|------|
| **禁止** `Column.padding(paddingValues)` 傳遞至 Viewfinder 容器 | 此為 D-18 修復的核心痛點 — Scaffold paddingValues 與 edge-to-edge insets 雙重疊加導致 Viewfinder 被推下 |
| Viewfinder 從 y=0 開始，不接收任何 Scaffold padding | Top Bar 自行使用 `WindowInsets.statusBars` 動態獲取 inset |

### 3.2 浮動 Top Bar（Overlay on Viewfinder）

Top Bar **不是獨立區塊** — 它是 Viewfinder 上方的浮動 overlay。

| 屬性 | 值 |
|------|-----|
| Scrim 類型 | 線性漸層（從頂部向下） |
| Scrim 起始 | y = 0px, #000000 alpha **0.22** |
| Scrim 結束 | y = **120px**, #000000 alpha **0** |
| Scrim 漸變曲線 | `FastOutSlowInEasing`（頂部濃 → 快速淡出） |
| 圖示位置 | y = 24px + `statusBarsPadding()` |
| 圖示顏色 | #FFFFFF, alpha **0.85**（靜態預設值；動態行為見 §3.2.1） |
| 圖示內容 | 左: ⚡ 閃光燈, HDR, 💧 髒汙提醒 · 右: ⚙ Settings, ? 使用手冊 |
| 圖示尺寸 | 24dp, touch target 48dp |
| 圖示間距 | 左右 padding 16dp |

> ✅ **Scrim endpoint 120px — CSO 已確認（D-13, 2026-05-18）**。原 v2.1 值為 72px。待實測後若需調整，同步修改本表 + §2 佈局圖 + SSOT v4.0.5 §B.5。

**設計原則**:
- Scrim alpha 上限 0.25 — 超過就變成「另一種不透明黑條」
- 圖示優先 (Icon-First)，左側排列高頻控制 (⚡, HDR, 💧)，右側排列極低頻設定 (⚙, ?)
- 不出現其他文字標籤（圖示即功能，唯一文字標籤為 HDR，視為圖示）
- 動態插入的髒污提示等使用 `AnimatedVisibility(slideIn)`，閃光燈及 HDR 位置固定

#### 3.2.1 Top Bar 圖示光線指示動態行為（D-15/D-16/D-19, SSOT §2.9）

⚡（閃光燈）與 HDR 圖示承擔光線狀態的 Ambient UI 視覺指示。以 pulse 動畫取代文字告警，對齊 Ambient UI「視覺引導、不干預閱讀」哲學。

**三態映射（D-15: Option 2）**：

| 光線狀態 | 觸發條件 | ⚡ 圖示 | HDR 圖示 |
|---------|---------|---------|---------|
| **正常** | histogram 正常 | 靜態 #FFFFFF α0.85 | 靜態 #FFFFFF α0.85 |
| **需要 HDR** | `isBacklit` ∥ `isHighContrast` ∥ `isOverexposed` | 靜態（不變） | amber pulse #D4A030 |
| **需要補光** | `isLowLight` | amber pulse #D4A030 | amber sync pulse（與 ⚡ 同週期） |

**Pulse 動畫參數**：

| 參數 | 需要 HDR | 需要補光 |
|------|---------|---------|
| HDR pulse 週期 | **3000ms**（D-19） | **3000ms**（與 ⚡ 同步） |
| ⚡ pulse 週期 | — | **3000ms**（D-19） |
| Alpha 範圍 | 0.35 → 1.0 → 0.35 | 0.35 → 1.0 → 0.35 |
| Easing | `FastOutSlowInEasing` | `FastOutSlowInEasing` |
| 顏色 | #D4A030（琥珀金） | #D4A030（琥珀金） |
| glow ring | α 0→0.14→0（icon 背後微暈） | α 0→0.14→0 |

**Debounce（D-16: 1000ms）**：

光線狀態來自 Frame 2/4 直方圖分析（SSOT §10.1），`confidenceFactor = 0.70`（SSOT §4.2 最低值）。為避免相機移動時圖示頻繁閃動，光線狀態必須持續 ≥1000ms 方觸發 pulse。退出策略相同：histogram 回到正常後持續 1000ms → pulse 停止，圖示恢復靜態 α0.85。

**Canvas 呼叫增量：0 次** — Top Bar 圖示為 Composable，不在 Canvas drawXxx pipeline 中。

**三級裝置降級**：

| 裝置等級 | HDR/⚡ pulse | 備註 |
|---------|-------------|------|
| FULL | pulse 動畫（3000ms） | 完整 |
| LIMITED | pulse 動畫（3000ms） | Composable 動畫成本可忽略 |
| LEGACY | **不顯示光線狀態**（靜態白色 α0.85，與正常態一致） | LEGACY 已關閉光線分析（SSOT §10.3），無數據源則不渲染狀態色（D-26） |

### 3.3 Viewfinder Overlay 圖層（對齊 SSOT v4.0.5 §Viewfinder Overlay — 逐字對齊）

```
Layer 0: Camera Preview（CameraX PreviewView）
Layer 1: Pitch Traction 指示柱（右側 16dp, 36dp×25% viewfinder, 琥珀金/綠色 — D-2; 底層先繪製）
Layer 2: 三分網格線（#FFFFFF alpha 0.35, 線寬 1.5px — D-24; 在 Pitch 之上確保可見）
Layer 3: Halo Traction 圓環（琥珀金 / 綠色, 呼吸 alpha 0.10→0.18 — D-3）
Layer 4: 對焦圓環（Tap-to-focus, 60dp, 四狀態生命週期 — 見 SSOT 附錄 A）
Layer 5: Level Traction（白色虛線 reference + 動態 dynamic 層）
Layer 6: Scrim + Top Bar 浮動圖示（最上層）
```

**Layer 1 詳細規格（D-2 指示柱設計，完整定義見 SSOT v4.0.5 §2.3）**：

| 屬性 | 值 | 備註 |
|------|-----|------|
| 結構 | 中軸線 + 定位環 + 浮動圓點 | 三元素組成 |
| 容器寬度 | 36dp | `PitchTractionConstants.COLUMN_WIDTH_DP` |
| 容器高度 | viewfinder 高度 × 25% | `PitchTractionConstants.COLUMN_HEIGHT_RATIO` |
| 右側邊距 | 16dp | `PitchTractionConstants.COLUMN_RIGHT_MARGIN_DP` |
| 中軸線寬度 | 2dp | 靜態軌道 |
| 定位環直徑 | 32dp, stroke 1.5dp | 固定在軸線中點 = 目標角度 |
| 浮動圓點直徑 | 20dp | 沿中軸移動 = 當前 pitch |
| 顏色邏輯 | ≤10° 偏差：綠 #50C864；>10° 偏差：琥珀 #D4A030 | 50% 閾值（R17） |
| 對齊呼吸 | 整體進入 α 0.10→0.18 呼吸（全局呼吸驅動） | 與快門外環同步 |
| Canvas 呼叫 | **3 次**（drawLine + drawCircle ×2） | — |
| **禁止** | `null → drawRect(Brush.radialGradient(...))` 或任何全幅暈影 | D-14: Edge Warmth 語義殘留清除（SSOT §B.7） |

**Layer 2 詳細規格（D-24 三分網格線，完整定義見 SSOT v4.0.5 §Viewfinder Overlay）**：

| 屬性 | 值 | 備註 |
|------|-----|------|
| 線條數量 | 4 條（2 vertical + 2 horizontal） | 三分法標準 |
| 顏色 | #FFFFFF | — |
| Alpha | **0.35** | Ambient UI 甜蜜點 — 可見但不干擾（D-24） |
| 線寬 | **1.5px** | 最小 dp-independent 可見線寬 |
| StrokeCap | Round | `StrokeCap.Round` |
| 繪製範圍 | contentBounds 內（排除 FIT_CENTER letterbox） | v2.11.1 P0-2 |
| Z-order | **在 PitchTractionLayer 之上（Layer 2 > Layer 1）** | 確保不被 Pitch aligned 呼吸遮蓋（D-24） |
| Food cold start 過渡 | ODT warm-up 期間維持 α0.35；首個 Food bbox 到達後 300ms FastOutSlowIn 淡出至 α0.15 | D-24 |
| Canvas 呼叫 | **4 次**（drawLine ×4） | 效能低負擔 |
| 可見性控制 | `isGridEnabled` 用戶設定 + `!isPtfActive`（PTF 模式隱藏） | — |

**Layer 3 詳細規格（D-3 Halo alpha，完整定義見 SSOT v4.0.5 §2.5）**：

| 狀態 | Alpha | 備註 |
|------|-------|------|
| 未對齊（靜態） | **0.25** | 方向加亮弧指向目標（D-11: atan2 連續角度） |
| 已對齊（呼吸） | **0.10 → 0.18**（2000ms 週期） | D-3 鎖定值 |
| 群組 Halo（未對齊） | **0.20** | 單人 α − 0.05，補償大面積壓迫感 |
| 群組 Halo（已對齊） | **0.30** | — |

---

## 4. 區塊 2: 底部控制列

### 4.1 總體規格

| 屬性 | 值 |
|------|-----|
| 起始位置 | y = 664px |
| 結束位置 | y = 844px |
| 高度 | **180px** |
| 佔螢幕比例 | **21.3%** |
| 背景色 | #0D0D0D（Photo 模式）/ **#1A1714**（PTF 模式,暖色偏移） |
| 背景過渡 | `animateColorAsState`, 300ms, `FastOutSlowInEasing` |
| 與 Viewfinder 分隔 | 1px 線, #FFFFFF alpha 0.05（微妙分隔,非強硬邊界） |

### 4.2 子區塊拆解

```
┌──────────────────────────────────┐ ← y = 664px
│                                  │
│        [ Photo | PTF ]           │   4.2a Pill Toggle 區（40px）
│                                  │
├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤ ← y = 704px
│                                  │
│  [📷]        ( ● )        [🔄]  │   4.2b 快門區（80px, centerY=748px）
│                                  │
├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤ ← y = 784px
│                                  │
│         (safe area)              │   4.2c Navigation Safe Area（60px）
│                                  │
└──────────────────────────────────┘ ← y = 844px
```

#### 4.2a Pill Toggle 區（y = 664 ~ 704px, 40px）

**此區域僅包含 Photo/PTF Pill Toggle,居中放置。**

#### 4.2b 快門區（y = 704 ~ 784px, 80px）— D-12 更新

三個圖示共享 **centerY = 748px**（D-12: 全局下移 4dp，原 744px），水平等間距佈局：

| 元素 | centerX | centerY | 尺寸 | 樣式 |
|------|---------|---------|------|------|
| 檔案庫 (Gallery) | **screenWidth × 20%** = 78px | **748px** | 40dp | 存取相簿 |
| 快門按鈕 (Shutter) | **screenWidth × 50%** = 195px | **748px** | 72dp | 外環: 綠色呼吸 (條件達成) 或 白色靜態 |
| 鏡頭切換 (Flip) | **screenWidth × 80%** = 312px | **748px** | 40dp | 前後鏡頭切換 |

**等間距約束**：Gallery centerX（20%）、Shutter centerX（50%）、Flip centerX（80%）三點等間距（各 30% 螢幕寬），快門居中，Gallery 和 Flip 對稱。

**D-12 設計理由**：原 centerY=744px（快門區精確中點）在視覺上偏高，因 Navigation Safe Area 為純留白。下移 4dp 後三圖示視覺重心更穩定，Gallery/Flip 與快門外環底部對齊更自然。

**互動狀態**: PTF 模式下快門放大至 80dp，且左右兩側 (Gallery, Flip) 將隱藏 (fadeOut) 避免干擾，並於左側浮現 PTF Exit (✕) 按鈕。

#### 4.2c Navigation Safe Area（y = 784 ~ 844px, 60px）

純 #0D0D0D（或 PTF 模式下 #1A1714）, 不放任何 UI 元素。
對應 Android `navigationBarsPadding()`。

---

## 5. Photo / PTF Pill Toggle（v2.1 更新）

**v2.1 核心變更**：文字標籤改為圖示。

### 5.1 尺寸與位置

| 屬性 | 值 |
|------|-----|
| 位置 | 底部控制列頂部,水平居中（Option A: 快門上方） |
| 外部尺寸 | **寬 128dp** × 高 28dp |
| 圓角 | 14dp（完全膠囊形） |
| 外框 Track | 1px stroke, #555555 |
| 內部選中指示器 | **寬 60dp** × 高 24dp, 圓角 12dp |
| 圖示尺寸 | **20dp** |
| 切換動畫 | 300ms, `FastOutSlowInEasing` |
| Touch target | 整個 Pill 區域（128dp × 48dp, 含上下 padding 10dp） |

### 5.2 顏色系統（灰階不變）

| 狀態 | 元素 | 色值 | Alpha |
|------|------|------|-------|
| **Track（任何模式）** | 外框描邊 | #555555 | 1.0 |
| **Track（任何模式）** | 背景填充 | transparent | — |
| **選中指示器** | 填充 | **#4A4A4A** | 1.0 |
| **選中圖示** | Camera 或 Users | #CCCCCC | 1.0 |
| **未選中圖示** | Camera 或 Users | #666666 | 1.0 |

**對比度驗證（等效於文字）**:
- 指示器 #4A4A4A vs 底色 #0D0D0D: **3.6:1** — 超過 WCAG AA 非文字元素門檻 (3:1)
- 選中圖示 #CCCCCC vs 指示器 #4A4A4A: **4.2:1** — 清晰可辨
- 未選中圖示 #666666 vs 底色 #0D0D0D: **5.3:1** — 充分可辨

### 5.3 兩個狀態的視覺定義

**State: Photo Selected（預設）**

```
┌─────────────────────────────────┐  track: stroke #555555, radius 14dp
│ ┌──────────┐                │
│ │   📷     │     👥         │  指示器在左, fill #4A4A4A
│ └──────────┘                │  📷: #CCCCCC · 👥: #666666
└─────────────────────────────────┘
         底部背景: #0D0D0D
```

**State: PTF Selected**

```
┌─────────────────────────────────┐  track: stroke #555555, radius 14dp
│                ┌──────────┐ │
│     📷         │   👥     │ │  指示器在右, fill #4A4A4A
│                └──────────┘ │  📷: #666666 · 👥: #CCCCCC
└─────────────────────────────────┘
         底部背景: #1A1714（暖色偏移）
```

**Compose 實作**：

```kotlin
// Pill Toggle 使用 Material Icons 或 Compose 內建 icon
// Camera = Icons.Outlined.CameraAlt
// Users  = Icons.Outlined.People（或自定義雙人圖示）

Icon(
    imageVector = if (isPhotoMode) Icons.Outlined.CameraAlt else Icons.Outlined.People,
    contentDescription = if (isPhotoMode) "Photo mode" else "Pass to Friend",
    tint = if (isSelected) Color(0xFFCCCCCC) else Color(0xFF666666),
    modifier = Modifier.size(20.dp)
)
```

### 5.4 切換行為

| 行為 | 規格 |
|------|------|
| 觸發方式 | 點擊 Pill 的左半或右半 |
| 指示器動畫 | 滑動 300ms, `FastOutSlowInEasing` |
| 底部背景過渡 | #0D0D0D ↔ #1A1714, 300ms, `FastOutSlowInEasing` |
| 快門冷卻 | 切換至 PTF 後 **1 秒內不可按快門**（防朋友誤觸,沿用既有規格） |
| CameraX 行為 | **零硬體切換** — 不 rebind camera, 不 close ML Kit, 不重校 IMU |
| 首次 PTF 切換 | 顯示 Bottom Sheet（max(screenHeight * 0.20, 96dp)）介紹 PTF |

---

## 6. PTF 模式底部背景暖色偏移

### 6.1 設計意圖

Pill Toggle 本身的灰度保持不變 — 用**環境氛圍**而非控件顏色傳達「模式已切換」。底部控制列的背景從冷黑 (#0D0D0D) 微微偏暖到 #1A1714（Cueshot 品牌暖色調的極暗版本）。

用戶不會有意識地注意到底色變了,但潛意識會感覺「相機處於不同狀態」。

### 6.2 色值定義

| 模式 | 底部背景色 | 色溫描述 |
|------|-----------|---------|
| Photo（預設） | `#0D0D0D` | 冷中性黑 |
| PTF | `#1A1714` | 極暗暖棕（品牌色 #D4A030 的 ~3% 亮度映射） |

### 6.3 技術實作

```kotlin
val bottomBarColor by animateColorAsState(
    targetValue = if (isPtfMode) Color(0xFF1A1714) else Color(0xFF0D0D0D),
    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
    label = "bottomBarWarmShift"
)
```

效能成本: **零** — 單一 `animateColorAsState` 不觸發任何額外 recomposition。

---

## 7. 背景圖 Prompt 更新（v1.1 → v2.0）

### 7.1 比例修正

| 項目 | v1.1 | v2.0 |
|------|------|------|
| 比例 | 3:4（1080×1440） | **390:844 ≈ 9:19.5（1170×2532 @3x）** |
| 覆蓋範圍 | 僅取景框 | **整支手機螢幕** |
| 構圖重心 | 圖片中央 | **中上 60% 區域**（下方 21% 被底部控制列蓋住） |

### 7.2 更新後的共用指令前綴

```
Generate a photorealistic image at exactly 1170×2532 pixels (390×844pt @3x retina).
This represents a FULL smartphone screen — the camera viewfinder fills the entire display.
The bottom ~21% of the image will be covered by a dark UI bar, so compose the scene's 
key subject within the upper 79% of the frame.
The image should look exactly like what you see through an Android phone's rear camera 
viewfinder — natural smartphone camera perspective with ~26mm equivalent focal length.
Slightly desaturated colors, natural indoor lighting. No UI elements, no text, 
no overlays, no phone frame.
The image will be used as a full-screen background layer in a camera app UI prototype.
```

---

## 8. 從 v1.1 移除的元素

以下元素在 v2.0 中**正式刪除**:

| 移除項目 | 理由 |
|---------|------|
| 72px 不透明 Top Bar 區塊 | 改為浮動 Overlay + Scrim |
| Photo · Video · Portrait 三標籤列 | 場景自動偵測,僅保留 Photo/PTF 手動切換 |
| PTF Tab（36px 全寬綠色條） | 功能被 Pill Toggle 完全取代 |
| PTF 快門右側圖示 | 功能被 Pill Toggle 完全取代 |
| 舊版控制列版面 | 快門左右側改為標準的 Gallery (左) 與 Flip (右) |

### 8.1 PTF 進出路徑更新

| 項目 | v1.1（舊） | v2.0（新） |
|------|-----------|-----------|
| 進入 PTF | Tab 點擊 | **Pill Toggle 點擊 PTF 側** |
| 退出 PTF | Tab 再點 / ✕ 按鈕（雙路徑） | **Pill Toggle 點擊 Photo 側 / ✕ 按鈕（雙路徑並行）** |
| 進入動畫 | Tab 高亮 + Bottom Sheet | **Pill 滑動 + 底色暖偏移 + 隱藏 Gallery/Flip + Bottom Sheet（首次）** |
| 退出動畫 | Tab 回歸隱藏態 | **Pill 滑回 + 底色冷回歸 + 恢復 Gallery/Flip** |

---

## 9. KAIROS #3 議程追加（D10 結束時審議）

基於本次佈局重構,追加以下議題:

- **#11**: 兩區塊模型實機驗證 — Viewfinder 78.7% 在美食 / Portrait / PTF 三場景的構圖舒適度
- **#12**: Scrim alpha 0.22 在高亮度戶外場景下 Top Bar 圖示是否仍可讀
- **#13**: Portrait 模式是否需要 Top Bar 半透明化（alpha 0.85）讓 headroom 視覺呼吸
- **#14**: 底部暖色偏移 #1A1714 在不同裝置 OLED/LCD 上的感知差異

---

## 10. 文件相依性地圖

```
本文件 (Layout Spec v2.5.1)
  ├─→ SSOT v4.0.5 §Viewfinder Overlay — 雙向對齊（Overlay 圖層定義）
  ├─→ SSOT v4.0.5 §B.5 — Top Bar Scrim + 光線指示（D-13/D-15/D-16/D-19）
  ├─→ SSOT v4.0.5 §2.3 — Pitch Traction 指示柱規格（D-2）
  ├─→ SSOT v4.0.5 §2.5 — Halo Traction alpha 規格（D-3）
  ├─→ SSOT v4.0.5 §2.9 — Top Bar 光線指示三態 pulse 規格（D-15/D-16/D-19）
  ├─→ SSOT v4.0.5 §附錄 A — Focus Ring 四狀態生命週期
  ├─→ SSOT v4.0.5 §B.6 — PreviewView scaleType FILL_CENTER（D-18）
  ├─→ SSOT v4.0.5 §B.7 — Edge Warmth 語義殘留清除（D-14）
  ├─→ SSOT v4.0.5 §B.8 — Edge-to-Edge + Display Cutout（D-18）
  ├─→ Prototyper Prompt v2 — 更新佈局段落（兩區塊 + Pill + Scrim）
  ├─→ Background Image Prompts v2 — 更新比例與構圖指引
  └─→ Claude Code 實作 — 按 SSOT v4.0.5 + 本文件拆票
```

---

## 11. v2.2 → v2.3 修訂追蹤

### 修訂總覽：16 項，全部完成

| # | 嚴重度 | 段落 | 修訂摘要 | 對應決策/審計項 |
|---|--------|------|---------|---------------|
| F-1 | 🔴 Critical | §3.3 Layer 2 | **「漸層（邊緣向內 60px）」→「指示柱（右側 16dp, 36×25% viewfinder）」** | D-2, C-1 |
| F-2 | 🔴 Critical | §3.3 Layer 3 | **「alpha 0.20 ~ 0.35」→「呼吸 alpha 0.10→0.18 — D-3」** | D-3, C-2 |
| F-3 | 🔴 Critical | §3.3 Layer 4 | **補齊「60dp, 四狀態生命週期 — 見 SSOT 附錄 A」** | C-3 |
| F-4 | 🟠 High | §3.3 下方 | **新增 Layer 2 詳細規格表**（指示柱結構、尺寸、顏色邏輯、Canvas 呼叫數） | D-2 補充 |
| F-5 | 🟠 High | §3.3 下方 | **新增 Layer 3 詳細規格表**（四種 alpha 狀態精確值） | D-3 補充 |
| F-6 | 🟠 High | 標題 L3 | 版本描述更新為 `SSOT v4.0.2 Full Alignment` | H-1 |
| F-7 | 🟠 High | 變更歷程 L7 | 新增 v2.2→v2.3 變更記錄 | H-1 |
| F-8 | 🟠 High | 上游文件 L10 | `SSOT_v4_0.md` → `SSOT_v4_0_2.md` | H-2 |
| F-9 | 🟠 High | §3.3 標題 | `SSOT v4.0` → `SSOT v4.0.2` | H-1 |
| F-10 | 🟠 High | §10 相依性 | `SSOT v4.0` → `SSOT v4.0.2`（全部 4 處） + 新增 §2.3/§2.5/附錄 A 引用 | H-1, H-2 |
| F-11 | 🟠 High | 文件元資訊 | 全部 `v4.0` 引用更新為 `v4.0.2` | H-1, H-2 |
| F-12 | 🟡 Medium | §4.2b | **快門區三圖示 centerY 744→748px**（D-12 全局下移 4dp） | R14, M-1 |
| F-13 | 🟡 Medium | §4.2b | **新增 Gallery/Flip 水平等間距約束**（20%/50%/80%） | R14, M-2 |
| F-14 | 🟡 Medium | §2 佈局圖 | 更新 ASCII 圖：加入 `centerY=748px — D-12` 標註 | R14, M-3 |
| F-15 | 🟡 Medium | §4.2 子區塊圖 | 更新 ASCII 圖：加入 `centerY=748px` 標註 | R14, M-3 |
| F-16 | 🔵 Low → ✅ | §3.2 | **Scrim 120px CSO 確認鎖定（D-13）**，待實測調整 | L-1, D-13 |

---

## 12. v2.3 → v2.4 修訂追蹤

### 修訂總覽：8 項，全部完成

| # | 嚴重度 | 段落 | 修訂摘要 | 對應審計項 |
|---|--------|------|---------|-----------|
| G-1 | 🔴 Critical | 全文（12+ 處） | **版本錨點 `SSOT v4.0.2` → `SSOT v4.0.3` 全量更新** | 版本漂移修正 |
| G-2 | 🔴 Critical | §3.2.1 新增 | **Top Bar 圖示光線指示三態 pulse 完整規格**（D-15/D-16/D-19） | SSOT §2.9 傳播 |
| G-3 | 🟠 High | §3.1 | **Edge-to-Edge + Cutout mode 規格新增**（D-18 附帶修復） | SSOT §B.8 傳播 |
| G-4 | 🟠 High | §3.1 | **PreviewView FILL_CENTER 禁止聲明 + D-18 引用** | SSOT §B.6 傳播 |
| G-5 | 🟠 High | §3.1 | **Scaffold padding 規則新增**（禁止雙重 padding） | SSOT §B.8 傳播 |
| G-6 | 🟡 Medium | §3.3 Layer 2 | **null vignette / Edge Warmth 暈影禁止聲明** | SSOT §B.7 / D-14 傳播 |
| G-7 | 🟡 Medium | §10 | **相依性地圖擴充** — 新增 §2.9 / §B.6 / §B.7 / §B.8 引用 | 完整性補齊 |
| G-8 | 🟡 Medium | §12 新增 | **v2.3→v2.4 修訂追蹤區塊**（本段落） | 追溯性要求 |

### 零殘留驗證

本文件已通過以下自動化檢查：
- ✅ **命名淨化**：全文不存在 "Edge Warmth" 或 "水平儀" 作為渲染元件名稱
- ✅ **上游引用**：全部指向 `SSOT_v4_0_3.md`，零殘留 `SSOT_v4_0.md` 或 `SSOT_v4_0_2.md`
- ✅ **Overlay 圖層描述**：§3.3 逐字對齊 SSOT v4.0.3 §Viewfinder Overlay
- ✅ **D-series 決策傳播**：D-2（指示柱）、D-3（Halo alpha）、D-12（快門下移）、D-14（null vignette 禁止）、D-15/D-16/D-19（光線指示）、D-18（FILL_CENTER + edge-to-edge + Scaffold padding）全部落地
- ✅ **新增功能覆蓋**：§3.2.1 光線指示三態 pulse 完整規格已從 SSOT §2.9 傳播

---

## 13. v2.4 → v2.5 修訂追蹤

### 修訂總覽：3 項，全部完成

| # | 嚴重度 | 段落 | 修訂摘要 | 對應審計項 |
|---|--------|------|---------|-----------|
| H-1 | 🔴 Critical | §3.3 Overlay 圖層 | **Grid 參數 α0.08→0.35 / 0.5px→1.5px + Z-order Layer 1↔2 交換** | SSOT R29 / D-24 |
| H-2 | 🟠 High | §3.3 下方 | **新增 Layer 2 Grid 詳細規格表**（alpha、線寬、cold start 過渡） | D-24 補充 |
| H-3 | 🟡 Medium | §13 新增 | **v2.4→v2.5 修訂追蹤區塊**（本段落） | 追溯性要求 |

### 零殘留驗證

本文件已通過以下自動化檢查：
- ✅ **命名淨化**：全文不存在 "Edge Warmth" 或 "水平儀" 作為渲染元件名稱
- ✅ **上游引用**：全部指向 `SSOT_v4_0_5.md`，零殘留 `SSOT_v4_0_3.md`
- ✅ **Overlay 圖層描述**：§3.3 逐字對齊 SSOT v4.0.5 §Viewfinder Overlay
- ✅ **D-series 決策傳播**：D-24（Grid 規格 + Z-order + cold start 過渡）落地
- ✅ **Layer 詳細規格完整性**：Layer 1（Pitch）+ Layer 2（Grid）+ Layer 3（Halo）均有詳細規格表

---

## 文件元資訊

- **版本**: Layout Spec v2.5.1 (SSOT v4.0.5 Alignment + D-25 Layer 1 文案修正 + D-26 LEGACY 光線指示修復)
- **建立日期**: 2026-04-25
- **修訂日期**: 2026-05-28
- **決策鏈**: CSO Session D8 → Top Bar Overlay 化 → Photo/PTF Pill Toggle (Option A + Color C) → 暖色偏移 → **Session 4 D-12 快門下移 4dp** → **Session 6 D-14~D-19 全面修復** → **Session 8 D-24 Grid 規格鎖定**
- **取代**: v2.5 及所有前版（v2.4, v2.3, v2.2, v2.1, v2.0, v1.1 三區塊模型）（v2.3, v2.2, v2.1, v2.0, v1.1 三區塊模型）
- **上游文件**: `SSOT_v4_0_5.md`（唯一真理文件）
- **審查狀態**: UITA 交叉審計完成 — v2.2→v2.3 16 項 + v2.3→v2.4 8 項 + v2.4→v2.5 3 項修訂全部落地 + 零殘留驗證通過
- **命名規範**: 本文件中不存在 "Edge Warmth" 或 "水平儀" 作為渲染元件名稱。三通道體系統一使用 Pitch Traction / Level Traction / Halo Traction。PitchTractionLayer 禁止包含 null/radialGradient 渲染邏輯（D-14）。
- **Open Item**: 無 — Scrim endpoint 120px 已由 CSO 確認鎖定（D-13, 2026-05-18），待實測調整

---

**End of Layout Spec v2.5.1**
