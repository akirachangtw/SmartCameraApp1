# Cueshot 佈局規格 v2.2 — 兩區塊模型

**版本**: Layout Spec v2.2 (Naming Purge + SSOT v4.0 Alignment)  
**最後更新**: 2026-05-12  
**變更歷程**:  
- v2.0 → v2.1：§5 Pill Toggle 文字標籤改為圖示（Camera / Users）  
- v2.1 → v2.2：Edge Warmth 命名全面淨化（4 處）+ SSOT v4.0 數值對齊（Scrim 120px）+ 文件引用更新  
**決策來源**: CSO Session D8 — Top Bar Overlay 化 + Photo/PTF Pill Toggle  
**取代**: v2.1 及所有前版（v1.1 三區塊模型）  
**上游文件**: `SSOT_v4_0.md`（唯一真理文件）  
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
│ ░ [⚡][HDR][💧]       [⚙] [?] ░ │   浮動圖示（白色,alpha 0.85）
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
│    [📷]      ( ● )      [🔄]    │   ← 快門區（80px）
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
| 內容 | CameraX `PreviewView`（`scaleType = FILL_CENTER`） |
| Overlay 圖層 | Layer 0 ~ Layer 6（見 §3.3） |

### 3.2 浮動 Top Bar（Overlay on Viewfinder）

Top Bar **不是獨立區塊** — 它是 Viewfinder 上方的浮動 overlay。

| 屬性 | 值 |
|------|-----|
| Scrim 類型 | 線性漸層（從頂部向下） |
| Scrim 起始 | y = 0px, #000000 alpha **0.22** |
| Scrim 結束 | y = 120px, #000000 alpha **0** |
| Scrim 漸變曲線 | `FastOutSlowInEasing`（頂部濃 → 快速淡出） |
| 圖示位置 | y = 24px + `statusBarsPadding()` |
| 圖示顏色 | #FFFFFF, alpha **0.85** |
| 圖示內容 | 左: ⚡ 閃光燈, HDR, 💧 髒汙提醒 · 右: ⚙ Settings, ? 使用手冊 |
| 圖示尺寸 | 24dp, touch target 48dp |
| 圖示間距 | 左右 padding 16dp |

**設計原則**:
- Scrim alpha 上限 0.25 — 超過就變成「另一種不透明黑條」
- 圖示優先 (Icon-First)，左側排列高頻控制 (⚡, HDR, 💧)，右側排列極低頻設定 (⚙, ?)
- 不出現其他文字標籤（圖示即功能，唯一文字標籤為 HDR，視為圖示）
- 動態插入的髒污提示等使用 `AnimatedVisibility(slideIn)`，閃光燈及 HDR 位置固定

### 3.3 Viewfinder Overlay 圖層（對齊 SSOT v4.0 §Viewfinder Overlay）

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
│    [📷]      ( ● )      [🔄]    │   4.2b 快門區（80px）
│                                  │
├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤ ← y = 784px
│                                  │
│         (safe area)              │   4.2c Navigation Safe Area（60px）
│                                  │
└──────────────────────────────────┘ ← y = 844px
```

#### 4.2a Pill Toggle 區（y = 664 ~ 704px, 40px）

**此區域僅包含 Photo/PTF Pill Toggle,居中放置。**

#### 4.2b 快門區（y = 704 ~ 784px, 80px）

| 元素 | 位置 | 尺寸 | 樣式 |
|------|------|------|------|
| 快門按鈕 (Shutter) | 水平居中 | 72dp | 外環: 綠色呼吸 (條件達成) 或 白色靜態 |
| 檔案庫 (Gallery) | 快門左側 | 40dp | 存取相簿 |
| 鏡頭切換 (Flip) | 快門右側 | 40dp | 前後鏡頭切換 |

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
┌─────────────────────────────┐  track: stroke #555555, radius 14dp
│ ┌──────────┐                │
│ │   📷     │     👥         │  指示器在左, fill #4A4A4A
│ └──────────┘                │  📷: #CCCCCC · 👥: #666666
└─────────────────────────────┘
         底部背景: #0D0D0D
```

**State: PTF Selected**

```
┌─────────────────────────────┐  track: stroke #555555, radius 14dp
│                ┌──────────┐ │
│     📷         │   👥     │ │  指示器在右, fill #4A4A4A
│                └──────────┘ │  📷: #666666 · 👥: #CCCCCC
└─────────────────────────────┘
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
本文件 (Layout Spec v2.2)
  ├─→ SSOT v4.0 §Viewfinder Overlay / §B.5 — 雙向對齊（佈局定義 + Scrim 規格）
  ├─→ Prototyper Prompt v2 — 更新佈局段落（兩區塊 + Pill + Scrim）
  ├─→ Background Image Prompts v2 — 更新比例與構圖指引
  └─→ Claude Code 實作 — 按 SSOT v4.0 + 本文件拆票
```

---

## 文件元資訊

- **版本**: Layout Spec v2.2 (Naming Purge + SSOT v4.0 Alignment)
- **建立日期**: 2026-04-25
- **淨化日期**: 2026-05-12
- **決策鏈**: CSO Session D8 → Top Bar Overlay 化 → Photo/PTF Pill Toggle (Option A + Color C) → 暖色偏移
- **取代**: v2.1 及所有前版（v1.1 三區塊模型）
- **上游文件**: `SSOT_v4_0.md`（唯一真理文件）
- **審查狀態**: UITA Edge Warmth 淨化完成 + SSOT v4.0 對齊完成
- **命名規範**: 本文件中不存在 "Edge Warmth" 或 "水平儀" 作為渲染元件名稱。三通道體系統一使用 Pitch Traction / Level Traction / Halo Traction。

---

**End of Layout Spec v2.2**
