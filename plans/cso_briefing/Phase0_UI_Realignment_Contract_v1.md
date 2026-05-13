# Phase 0 — UI Realignment Contract v1.0

**Contract 類型**: Phase 0 清場（W2 前置條件）
**提交者**: CSO (Claude Opus)
**執行者**: Gemini 執行參謀長 → Claude Code
**日期**: 2026-05-13
**上游文件**: SSOT v4.0 · Layout Spec v2.2 · CSO Gap Analysis (Session 7)
**前置條件**: W1 CLOSED · Phase 0 BHA-01-FIX 數學 PASS
**完成定義**: Phase 0.5 Visual Baseline Gate PASS（8 場景實機截圖全綠）

---

## §0 Contract 摘要

本 Contract 包含 **6 張票**，目標是將 CameraScreen 的視覺骨架從目前的非規格狀態對齊到 Layout Spec v2.2 + SSOT v4.0 定義。

**為什麼現在做**: W2 的 T04（SettingsScreen D1 邊界）、T06（ML Kit Emit Flow）、T03（z-order）全部疊加在 CameraScreen 上。如果骨架是歪的，W2 的任何變更都無法歸因——出了問題分不清是「Phase 0 殘留」還是「W2 引入」。

**為什麼不能跳過**: 實機截圖（2026-05-12）揭示 5 項結構性偏離，其中 4 項為 🔴 嚴重等級（完全不符 spec），1 項為 🟡 debug 殘留。

---

## §1 票序總覽

```
P0-NAMING ──→ P0-VF ──→ P0-TOPBAR ──→ P0-BOTTOM ──→ P0-FPS ──→ P0-FOCUS
  (0.5hr)     (1hr)      (2hr)         (3hr)        (0.5hr)    (0.5hr)
    │           │           │              │            │          │
    └───────────┴───────────┴──────────────┴────────────┴──────────┘
                                                                   │
                                                          Phase 0.5 Visual
                                                          Baseline Gate
                                                          (8 場景截圖)
```

**總估時**: 7.5 小時
**Session 分割建議**: 3 sessions（Session A: NAMING+VF, Session B: TOPBAR+BOTTOM, Session C: FPS+FOCUS+Baseline）

---

## §2 Ticket P0-NAMING — EdgeWarmth → PitchTraction 代碼重命名

### 2.1 背景

SSOT v4.0 和 Layout Spec v2.2 已完成文件層命名淨化。三通道 Traction 體系（Pitch / Level / Halo）取代了所有 Edge Warmth 命名。代碼庫必須同步，否則 gstack AI 角色在 SSOT↔Code 之間出現語義斷裂（§3 反模式 #3「模型漂移」）。

SSOT v4.0 末尾明確聲明：「本文件中不存在 Edge Warmth 作為渲染元件名稱。三通道體系統一使用 Pitch Traction / Level Traction / Halo Traction。」

### 2.2 範圍

| 變更項目 | 舊名 | 新名 |
|---------|------|------|
| 檔案名 | `EdgeWarmthLayer.kt` | `PitchTractionLayer.kt` |
| Class 名 | `EdgeWarmthLayer` | `PitchTractionLayer` |
| Composable 函數 | `EdgeWarmthOverlay()` / 類似 | `PitchTractionOverlay()` |
| ViewModel 欄位 | `edgeWarmthState` / 類似 | `pitchTractionState` |
| Preferences key | `edge_warmth_enabled` / 類似 | `pitch_traction_enabled` |
| Test 檔案 | `EdgeWarmthLayerTest.kt` / 類似 | `PitchTractionLayerTest.kt` |
| CLAUDE.md 引用 | 任何 "Edge Warmth" 字串 | "Pitch Traction" |

### 2.3 執行指令

```
1. 全專案搜尋 "EdgeWarmth"（case-sensitive）和 "edgeWarmth"（camelCase），列出所有出現位置
2. 全專案搜尋 "edge_warmth"（snake_case），列出所有出現位置
3. 使用 IDE Refactor > Rename 進行批量重命名（保持 git blame 可追溯）
4. 確認 SharedPreferences migration：若有已儲存的 "edge_warmth_enabled" key，需加 migration 邏輯讀舊 key 寫新 key
5. 執行 ./gradlew testDebugUnitTest 確認零 regression
6. 執行 ./gradlew lintDebug 確認零新 warning
```

### 2.4 驗收標準

- [ ] `grep -r "EdgeWarmth\|edgeWarmth\|edge_warmth" --include="*.kt" --include="*.xml" --include="*.md"` 回傳空結果
- [ ] `grep -r "PitchTraction\|pitchTraction\|pitch_traction" --include="*.kt"` 回傳 ≥ 原 EdgeWarmth 出現數
- [ ] 全部 Unit Test PASS
- [ ] 零新 Lint warning
- [ ] SharedPreferences migration 涵蓋（若適用）

### 2.5 風險

| 風險 | 機率 | 對沖 |
|------|------|------|
| SharedPreferences 舊 key 遺失 | 低 | Migration 邏輯 + 預設值 fallback |
| 其他文件引用舊名 | 低 | 全專案 grep 掃描 |

### 2.6 估時

**0.5 小時**（純重命名，不改邏輯）

---

## §3 Ticket P0-VF — Viewfinder 全幅填充 + 移除角括號

### 3.1 背景

Layout Spec v2.2 §3.1 定義 Viewfinder 從 y=0 填滿至 y=664px（佔螢幕 78.7%），使用 CameraX `PreviewView` 的 `scaleType = FILL_CENTER`。

實機截圖顯示：
- 四個綠色角括號將取景框「框」在畫面中間的一個小矩形內
- 取景框上方有明顯黑色留空（未延伸到螢幕頂部）
- 取景框未佔滿 78.7% 高度

綠色角括號是 D1-D8 遺留的 `DetectionOverlay` debug 元素，不在 SSOT v4.0 任何 Overlay Layer 定義中。

### 3.2 範圍

#### 3.2a 移除項目

| 移除目標 | 檔案（預估） | 理由 |
|---------|------------|------|
| 綠色角括號 Composable | `DetectionOverlay.kt` 或 `CameraScreen.kt` 內 | 不在 SSOT v4.0 Layer 0-6 定義中 |
| 固定比例 Modifier（如 `aspectRatio(3f/4f)`） | `CameraScreen.kt` PreviewView 容器 | Layout Spec v2.2 要求填滿至頂部，非 3:4 |

#### 3.2b 修改項目

| 修改目標 | 當前狀態 | 目標狀態 | 規格來源 |
|---------|---------|---------|---------|
| PreviewView 容器 | 固定大小 + 居中 | `fillMaxWidth() + height(664.dp)` 從 y=0 開始 | Layout Spec v2.2 §3.1 |
| PreviewView scaleType | 未確認 | `FILL_CENTER` | Layout Spec v2.2 §3.1 |
| Viewfinder 與底部分隔 | 不存在或錯誤 | 1px 線, #FFFFFF alpha 0.05 @ y=664px | Layout Spec v2.2 §4.1 |

### 3.3 執行指令

```
1. 搜尋 CameraScreen.kt 中 PreviewView 的 Modifier 鏈，確認當前佈局邏輯
2. 搜尋所有繪製「角括號」或「corner bracket」的 Canvas/Composable 代碼
3. 移除角括號繪製代碼
4. 修改 PreviewView 容器：
   - 移除 aspectRatio 約束（如有）
   - 設定 Modifier.fillMaxWidth().height(664.dp)（或對應裝置比例的百分比計算）
   - 確認 y=0 起始（含 statusBarsPadding 考量 — Scrim overlay 在 P0-TOPBAR 處理）
5. 確認 PreviewView.scaleType = PreviewView.ScaleType.FILL_CENTER
6. 在 y=664 位置加入分隔線（1px, Color.White.copy(alpha = 0.05f)）
7. 執行 androidTest 確認 CameraScreen 可正常啟動
```

### 3.4 驗收標準

- [ ] 實機截圖：取景框從螢幕頂部延伸至 y≈664px，無黑色留空
- [ ] 實機截圖：無綠色角括號或任何 debug 框線
- [ ] `grep -r "corner\|bracket\|DetectionOverlay" --include="*.kt"` 確認相關 debug 繪製已清除
- [ ] PreviewView.scaleType == FILL_CENTER（Logcat 或代碼確認）
- [ ] 分隔線在 Viewfinder 底部可見（alpha 0.05，極微妙）
- [ ] CameraScreen androidTest PASS

### 3.5 裝置適配注意

Layout Spec v2.2 基準裝置為 390×844pt。實際裝置需按比例計算 Viewfinder 高度（螢幕高度 × 0.787）。使用 `fillMaxHeight(0.787f)` 或等效的 weight-based 佈局比 hardcode dp 更安全。

### 3.6 風險

| 風險 | 機率 | 對沖 |
|------|------|------|
| 角括號繪製散佈在多個檔案 | 中 | 全專案搜尋 drawLine/drawPath 中含綠色 (#50C864) 的繪製 |
| PreviewView scaleType 變更導致 ML Kit 座標映射偏移 | 低 | ML Kit 使用 ImageProxy 座標，與 PreviewView 顯示比例獨立 |
| StatusBar 重疊 | 中 | Scrim 在 P0-TOPBAR 處理，此票先確認 PreviewView 從 y=0 開始 |

### 3.7 估時

**1 小時**

---

## §4 Ticket P0-TOPBAR — Top Bar Scrim 漸層 + 圖示骨架對齊

### 4.1 背景

Layout Spec v2.2 §3.2 定義 Top Bar 為浮動 Overlay + Scrim 線性漸層，不是獨立不透明區塊。

實機截圖顯示：
- 齒輪 ⚙ 在左側（spec: 右側）
- 旗幟 "A" 在中央（spec: 不存在）
- 星號 ★ 在右側（spec: 右側是 ⚙ 和 ?）
- 無 Scrim 漸層
- 無 HDR 控制
- 無 💧 髒汙提醒入口

### 4.2 範圍

#### 4.2a Scrim 漸層（必做）

| 屬性 | 值 | 來源 |
|------|-----|------|
| Scrim 類型 | 線性漸層（從頂部向下） | Layout Spec v2.2 §3.2 |
| 起始 | y=0px, #000000 alpha 0.22 | Layout Spec v2.2 §3.2 |
| 結束 | y=120px, #000000 alpha 0 | Layout Spec v2.2 §3.2 |
| 曲線 | `FastOutSlowInEasing` | Layout Spec v2.2 §3.2 |

```kotlin
// Scrim 實作參考
Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(120.dp)
        .background(
            Brush.verticalGradient(
                colors = listOf(
                    Color.Black.copy(alpha = 0.22f),
                    Color.Transparent
                )
            )
        )
)
```

#### 4.2b 圖示骨架對齊（必做）

| 位置 | 圖示 | 功能 | 狀態 |
|------|------|------|------|
| 左側 - Slot 1 | ⚡ 閃光燈 | 切換閃光燈模式 | 骨架（功能綁定在對應 Sprint Day） |
| 左側 - Slot 2 | HDR | HDR 開關 | 骨架（MVP 文字圖示） |
| 左側 - Slot 3 | 💧 髒汙提醒 | AnimatedVisibility, slideIn | 骨架（LensSmudge 觸發後顯示） |
| 右側 - Slot 1 | ⚙ Settings | 導航到 SettingsScreen | 骨架（導航連結） |
| 右側 - Slot 2 | ? 使用手冊 | 導航到 Help | 骨架 |

**圖示規格**（Layout Spec v2.2 §3.2）:
- 尺寸: 24dp, touch target 48dp
- 顏色: #FFFFFF, alpha 0.85
- 位置: y = 24px + `statusBarsPadding()`
- 間距: 左右 padding 16dp

#### 4.2c 移除項目

| 移除目標 | 理由 |
|---------|------|
| 旗幟 "A" 圖示 | 不在 Layout Spec v2.2 或 SSOT v4.0 定義中 |
| 星號 ★ 圖示 | 不在 Layout Spec v2.2 或 SSOT v4.0 定義中 |
| 舊版 Top Bar 不透明背景（如有） | Layout Spec v2.2 §1: 72px 不透明黑條已作廢 |

### 4.3 執行指令

```
1. 搜尋 CameraScreen.kt / TopBar.kt 中的 Top Bar Composable
2. 移除不在 spec 中的圖示（旗幟、星號等）
3. 移除任何不透明背景色（#0D0D0D 或類似）
4. 實作 Scrim 漸層 Composable（Box + Brush.verticalGradient）
5. 佈局圖示：
   - Row(horizontalArrangement = SpaceBetween)
   - 左側 Row: ⚡, HDR, 💧（AnimatedVisibility 預留）
   - 右側 Row: ⚙, ?
   - 每個 Icon: 24dp 大小, Modifier.size(48.dp) touch target
   - padding(horizontal = 16.dp, top = statusBarsPadding + 24.dp)
6. 圖示暫時全部為 IconButton 骨架（onClick = {} 或導航占位）
7. 圖示顏色統一為 Color.White.copy(alpha = 0.85f)
8. 執行 androidTest 確認 CameraScreen 正常啟動 + Top Bar 顯示
```

### 4.4 驗收標準

- [ ] 實機截圖：Top Bar 為透明漸層（上方略暗 → 120px 處透明），非不透明黑條
- [ ] 實機截圖：左側顯示 ⚡ / HDR 圖示，右側顯示 ⚙ / ? 圖示
- [ ] 實機截圖：無旗幟 "A" 或星號 ★ 圖示
- [ ] Scrim alpha 上限 ≤ 0.25（代碼確認 alpha = 0.22f）
- [ ] 圖示 touch target 為 48dp（代碼確認）
- [ ] CameraScreen androidTest PASS
- [ ] 零新 Lint warning

### 4.5 邊界聲明（不做什麼）

- **不做**：閃光燈實際功能綁定（CameraX API 呼叫）
- **不做**：HDR 實際功能綁定
- **不做**：髒汙提醒 AnimatedVisibility 邏輯（LensSmudge 在 D11-12）
- **不做**：Settings 導航實際跳轉（除非已有 NavHost 路由）
- **不做**：使用手冊頁面

### 4.6 風險

| 風險 | 機率 | 對沖 |
|------|------|------|
| Scrim 在高亮度戶外不足以讓圖示可讀 | 中 | KAIROS #3 議程 #12 已排程審議，Phase 0 先按 spec alpha 0.22 實作 |
| statusBarsPadding 在不同裝置差異大 | 低 | Jetpack Compose `WindowInsets.statusBars` 自動處理 |

### 4.7 估時

**2 小時**

---

## §5 Ticket P0-BOTTOM — 底部控制列重建（Pill Toggle + Shutter + Gallery + Flip）

### 5.1 背景

這是 Phase 0 中最大的一張票。實機截圖顯示底部控制列與 Layout Spec v2.2 §4 + §5 有根本性結構差異：

| 組件 | Spec 定義 | 實機狀態 | 偏離等級 |
|------|----------|---------|---------|
| Pill Toggle | 128dp×28dp 灰度膠囊，📷/👥 圖示 | 綠色大膠囊 + 獨立藍色/灰色圓形 | 🔴 完全不符 |
| 快門按鈕 | 72dp 白/綠外環，獨立居中 | 似被整合進綠色 Pill 中 | 🔴 完全不符 |
| Gallery | 40dp, 快門左側 | 不可見 | 🔴 缺失 |
| Flip | 40dp, 快門右側 | 不可見 | 🔴 缺失 |
| 底部背景 | #0D0D0D (Photo) / #1A1714 (PTF) | 灰色，無暖色偏移 | 🟡 偏離 |

### 5.2 範圍

#### 5.2a 底部控制列總體架構（Layout Spec v2.2 §4.1）

```
┌──────────────────────────────────┐ ← y = 664px (screenHeight × 0.787)
│                                  │
│        [ Photo | PTF ]           │   5.2b Pill Toggle 區（40px）
│                                  │
├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤ ← y = 704px
│                                  │
│    [📷]      ( ● )      [🔄]    │   5.2c 快門區（80px）
│                                  │
├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤ ← y = 784px
│                                  │
│         (safe area)              │   5.2d Navigation Safe Area（60px）
│                                  │
└──────────────────────────────────┘ ← y = 844px
```

| 屬性 | 值 | 來源 |
|------|-----|------|
| 高度 | 180px (screenHeight × 0.213) | Layout Spec v2.2 §4.1 |
| 背景色 (Photo) | #0D0D0D | Layout Spec v2.2 §4.1 |
| 背景色 (PTF) | #1A1714 | Layout Spec v2.2 §6.2 |
| 背景過渡 | animateColorAsState, 300ms, FastOutSlowInEasing | Layout Spec v2.2 §4.1 |
| 與 Viewfinder 分隔 | 1px, #FFFFFF alpha 0.05 | Layout Spec v2.2 §4.1 |

#### 5.2b Pill Toggle（Layout Spec v2.2 §5）

**完整規格表**:

| 屬性 | 值 |
|------|-----|
| 外部尺寸 | 寬 128dp × 高 28dp |
| 圓角 | 14dp（完全膠囊形） |
| 外框 Track | 1px stroke, #555555 |
| Track 背景 | transparent |
| 指示器尺寸 | 寬 60dp × 高 24dp, 圓角 12dp |
| 指示器填充 | #4A4A4A |
| 選中圖示 | #CCCCCC, 20dp |
| 未選中圖示 | #666666, 20dp |
| 圖示 (Photo) | Icons.Outlined.CameraAlt |
| 圖示 (PTF) | Icons.Outlined.People |
| 切換動畫 | 300ms, FastOutSlowInEasing |
| Touch target | 128dp × 48dp（含上下 padding 10dp） |

**兩種狀態**:

Photo Selected（預設）: 指示器在左 → 📷 #CCCCCC · 👥 #666666
PTF Selected: 指示器在右 → 📷 #666666 · 👥 #CCCCCC

**對比度驗證已通過**（Layout Spec v2.2 §5.2）:
- 指示器 #4A4A4A vs 底色 #0D0D0D: 3.6:1（WCAG AA 非文字 3:1 ✅）
- 選中圖示 #CCCCCC vs 指示器 #4A4A4A: 4.2:1 ✅
- 未選中圖示 #666666 vs 底色 #0D0D0D: 5.3:1 ✅

#### 5.2c 快門區（Layout Spec v2.2 §4.2b）

| 元素 | 位置 | 尺寸 | 樣式 |
|------|------|------|------|
| 快門按鈕 (Shutter) | 水平居中 | 72dp | 外環: 白色靜態（條件未達成）/ 綠色呼吸（全對齊） |
| Gallery | 快門左側 | 40dp | 存取相簿入口 |
| Flip | 快門右側 | 40dp | 前後鏡頭切換 |

**快門外環規格**（SSOT v4.0 §10.3.1）:
- 靜態: 白色, alpha 根據對齊狀態
- 呼吸: 全對齊時啟動，alpha 0.10↔0.18, 2000ms cycle, FastOutSlowInEasing
- 閃爍: 快門按下時，白色 α0.45→0 in 300ms

**PTF 模式互動變更**（Layout Spec v2.2 §4.2b）:
- 快門放大至 80dp
- Gallery + Flip fadeOut 隱藏
- 左側浮現 PTF Exit (✕) 按鈕

#### 5.2d Navigation Safe Area

純背景色（#0D0D0D / #1A1714），60px，不放任何 UI 元素。
對應 `Modifier.navigationBarsPadding()`。

### 5.3 執行指令

```
Phase 0 範圍內的底部控制列重建：

1. 搜尋 CameraScreen.kt 中底部控制列相關 Composable
2. 完全移除當前的 Pill Toggle / 快門 / 相關控件
3. 建立新的 BottomControlBar Composable:

   Column(
       modifier = Modifier
           .fillMaxWidth()
           .fillMaxHeight(0.213f)   // 或 weight-based
           .background(bottomBarColor)  // animateColorAsState
   ) {
       // 5.2b: PillToggle 區 (40dp height)
       PillToggle(
           isPhotoMode = isPhotoMode,
           onToggle = { viewModel.toggleMode() },
           modifier = Modifier.align(Alignment.CenterHorizontally)
       )
       
       // 5.2c: 快門區 (80dp height)
       ShutterRow(
           onShutter = { viewModel.capture() },
           onGallery = { /* 導航 */ },
           onFlip = { viewModel.flipCamera() },
           isPtfMode = isPtfMode,
           isAllAligned = isAllAligned
       )
       
       // 5.2d: Safe Area (navigationBarsPadding)
       Spacer(Modifier.navigationBarsPadding())
   }

4. 實作 PillToggle Composable:
   - Box(128.dp × 28.dp, RoundedCornerShape(14.dp), border = 1px #555555)
   - 內部 animatable 指示器 (60dp × 24dp, RoundedCornerShape(12.dp), fill #4A4A4A)
   - 兩個 Icon (CameraAlt / People), 20dp, 選中 #CCCCCC / 未選中 #666666
   - animateOffset for 指示器滑動

5. 實作 ShutterButton Composable:
   - 72dp 圓形, 外環可切換 白色靜態/綠色呼吸
   - 呼吸韻律: infiniteTransition alpha 0.10↔0.18, 2000ms
   - 快門閃爍: alpha 0.45→0, 300ms

6. 實作 Gallery + Flip 圖示:
   - 各 40dp, 左右對齊快門
   - 骨架（onClick 暫時導航占位或 no-op）

7. PTF 模式切換行為:
   - 底部背景 #0D0D0D ↔ #1A1714 (animateColorAsState, 300ms)
   - 快門 72dp → 80dp
   - Gallery/Flip fadeOut, PTF Exit (✕) fadeIn（左側）
   - 快門冷卻 1 秒（切換至 PTF 後不可按快門）

8. 執行 Unit Test + androidTest
```

### 5.4 驗收標準

- [ ] 實機截圖：Pill Toggle 為 128×28dp 灰度膠囊，含 📷/👥 圖示
- [ ] 實機截圖：Pill Toggle 預設選中 Photo（指示器在左）
- [ ] 實機截圖：快門按鈕 72dp 居中，外環白色
- [ ] 實機截圖：Gallery 圖示在快門左側，Flip 圖示在快門右側
- [ ] 實機截圖：底部背景色為 #0D0D0D
- [ ] 切換到 PTF 時：底部背景平滑過渡到 #1A1714
- [ ] 切換到 PTF 時：快門放大至 80dp, Gallery/Flip fadeOut
- [ ] Pill Toggle 切換動畫 300ms（視覺確認）
- [ ] 快門冷卻：PTF 切換後 1 秒內快門不可按（Logcat 確認）
- [ ] 分隔線在底部控制列頂部可見（1px, alpha 0.05）
- [ ] CameraScreen androidTest PASS
- [ ] 零新 Lint warning

### 5.5 邊界聲明（不做什麼）

- **不做**：快門外環呼吸韻律的觸發邏輯（需 PriorityEngine allConditionsMet，在對應 Sprint Day 接入）
- **不做**：快門外環閃爍動畫的觸發邏輯（需 onShutterPressed 事件，在快門功能接入時）
- **不做**：Gallery 實際相簿導航
- **不做**：PTF 首次切換的 Bottom Sheet 教學
- **先做骨架**：快門外環預設白色靜態，呼吸/閃爍的 Composable 代碼預留但不啟動

### 5.6 風險

| 風險 | 機率 | 對沖 |
|------|------|------|
| Pill Toggle 重寫影響 ViewModel 的 mode 狀態管理 | 中 | 確認 ViewModel 中 isPhotoMode / isPtfMode 的 StateFlow 已存在，Pill Toggle 只是 UI 消費者 |
| 底部 Safe Area 在不同裝置高度差異 | 低 | 使用 Modifier.navigationBarsPadding() 自動處理 |
| 快門按鈕 touch target 與 Pill Toggle 重疊 | 低 | Pill Toggle 40dp 區域與快門 80dp 區域有明確分隔 |

### 5.7 估時

**3 小時**（Phase 0 最大票，含 Pill Toggle + Shutter + Gallery/Flip + PTF 行為）

---

## §6 Ticket P0-FPS — FPS Debug Overlay BuildConfig.DEBUG 門控

### 6.1 背景

FPS overlay 在 D1-D8 開發中證明了偵測效能回歸的價值。CSO 裁決保留但必須門控：Release build 中完全不存在。

實機截圖顯示 15.9 FPS（紅色背景紅牌警告）。此值本身是效能紅旗（SSOT §10.2 定義 UI 渲染 <16ms ≈ 60fps），需在 Phase 0.5 Visual Baseline 時記錄為基線。

### 6.2 範圍

| 變更項目 | 目標 |
|---------|------|
| FPS overlay Composable | 包裹在 `if (BuildConfig.DEBUG)` 區塊內 |
| FPS 計算邏輯 | 同樣包裹在 `if (BuildConfig.DEBUG)` 區塊內（避免 Release build 做無用計算） |
| Proguard/R8 | 確認 Release build strip 掉 debug-only 代碼（Android default behavior） |

### 6.3 執行指令

```
1. 搜尋 FPS overlay 的 Composable 位置（可能在 CameraScreen.kt 或獨立檔案）
2. 搜尋 FPS 計算邏輯（frame counter / time delta）
3. 將兩者包裹在 if (BuildConfig.DEBUG) { ... }
4. 確認 Release build 不包含 FPS 相關代碼:
   ./gradlew assembleRelease
   確認 APK size 無增長 / proguard mapping 無 FPS 相關 class
5. Debug build 確認 FPS overlay 仍可見且功能正常
```

### 6.4 驗收標準

- [ ] Debug build: FPS overlay 可見且正常顯示幀率
- [ ] Release build: FPS overlay 完全不存在（APK 不含相關代碼）
- [ ] `grep -r "BuildConfig.DEBUG" --include="*.kt"` 確認 FPS 相關代碼已包裹
- [ ] 零新 Lint warning

### 6.5 Settings Debug 開關（延遲到 W2 T04）

按 CSO 裁決，Settings 中的 Debug 區塊開關**不在本票範圍**。理由：SettingsScreen 是 W2 T04 的領地，Phase 0 不觸碰。W2 T04 執行時順帶加入 Debug 區塊（邊際成本最低）。

D10 KAIROS 正式登記 Debug Infrastructure Rule 進 CLAUDE.md。

### 6.6 估時

**0.5 小時**

---

## §7 Ticket P0-FOCUS — BUG-I7-FOCUS-RING-PERSIST 清除

### 7.1 背景

BUG-I7-FOCUS-RING-PERSIST 是已知 Bug：Focus Ring（對焦圓環）在完成後不消失，持續留在螢幕上。SSOT v4.0 附錄 A 定義 Focus 動畫的 Focused 狀態持續 1.5s + 0.5s 淡出，之後應完全消失。

### 7.2 範圍

| 修改目標 | 當前狀態 | 目標狀態 |
|---------|---------|---------|
| Focus Ring 消失邏輯 | 不消失 (persist) | Focused 後 1.5s + 0.5s 淡出 → alpha=0 → 移除 |
| Focus Ring 動畫狀態機 | 缺少 timeout | Focusing → Focused (1.5s) → FadeOut (0.5s) → Gone |

### 7.3 Focus 動畫規格（SSOT v4.0 附錄 A）

| 狀態 | 視覺效果 | 顏色 | 持續時間 |
|------|---------|------|---------|
| Focusing | 1.3x→1.0x 縮放 + 60° 弧線旋轉 | 白色 α=0.9 | 至 AF 完成 |
| Focused | 穩定 + 脈動一次 | 綠色 α=0.8 | 1.5s + 0.5s 淡出 |
| Failed | 左右震動 4px translateX | 琥珀 α=0.6 | 0.8s + 0.5s 淡出 |
| Locked | 雙圈 + "AE/AF" 小字 | 亮金 α=0.85 | 至解鎖 |

### 7.4 執行指令

```
1. 搜尋 Focus Ring 相關 Composable（可能在 FocusRingOverlay.kt 或 CameraScreen.kt）
2. 確認目前的狀態管理邏輯（為什麼不消失？）
   - 可能原因 A: 缺少 Focused → Gone 的 timer
   - 可能原因 B: isFocused state 未被重置
   - 可能原因 C: 動畫結束後 Composable 未條件移除
3. 修復：加入 LaunchedEffect(focusState == Focused) 啟動 delay(1500) + 500ms fadeOut
4. 確認 Failed 狀態也有正確的消失邏輯（0.8s + 0.5s 淡出）
5. 確認 Locked 狀態不自動消失（至用戶解鎖）
6. 執行 Unit Test
```

### 7.5 驗收標準

- [ ] 實機測試：點擊螢幕觸發對焦 → Focus Ring 出現 → 完成後 2 秒內消失
- [ ] 實機測試：連續點擊不同位置 → 每個 Focus Ring 獨立生命週期，不累積
- [ ] Focused 狀態持續 1.5s + 0.5s 淡出（Logcat 確認 timing）
- [ ] Failed 狀態持續 0.8s + 0.5s 淡出
- [ ] Unit Test PASS

### 7.6 估時

**0.5 小時**

---

## §8 Phase 0.5 — Visual Baseline Gate（所有票完成後）

### 8.1 目的

在 W2 啟動前拍攝一組「乾淨基線截圖」，作為 W2 變更的視覺歸因錨點。

### 8.2 截圖場景清單（8 場景）

| # | 場景 | 拍攝條件 | 驗證重點 |
|---|------|---------|---------|
| 1 | Photo 模式 - 靜態 | 面向桌面物體，無人臉 | Viewfinder 全幅 + Top Bar Scrim + Pill Toggle Photo 選中 + 快門白色靜態 |
| 2 | Photo 模式 - Level Traction 啟動 | 手機傾斜 ≥5° | Level Traction 虛線可見 + 動態線琥珀色 |
| 3 | Photo 模式 - Level Traction 對齊 | 手機水平 | Level Traction 消失（alpha→0） + 快門外環靜態（呼吸需 PriorityEngine，Phase 0 不含） |
| 4 | PTF 模式 | 切換 Pill Toggle | 底部背景 #1A1714 + 快門 80dp + Gallery/Flip 消失 + Pill Toggle PTF 選中 |
| 5 | 美食角度 45° | 手機傾斜至 ~45° | Pitch Traction 邊緣暖光（如已實作） + Top Bar Scrim 可見 |
| 6 | 人像偵測 | 面向人臉 | Halo Traction 圓環（如已實作） + Top Bar 圖示可讀 |
| 7 | 對焦動畫 | 點擊螢幕 | Focus Ring 出現 → 2 秒後消失 |
| 8 | FPS Debug (Debug build only) | Debug build 截圖 | FPS overlay 可見 + 記錄 FPS 數值作為基線 |

### 8.3 Gate 判定

- 8 張截圖全部符合上述驗證重點 → **PASS** → W2 RESUME Gate 開啟
- 任一截圖不符 → **FAIL** → 開 HOTFIX 票修復 → 重拍

### 8.4 FPS 基線記錄

Phase 0.5 截圖 #8 的 FPS 值記錄在 PROGRESS.md 中，作為 W2 結束後效能對照的錨點。如果 W2 後 FPS 下降 >20%，需開效能調查票。

---

## §9 Gemini Action Items（收到本 Contract 後）

| # | 動作 | 產出 |
|---|------|------|
| 1 | 審閱本 Contract，確認無遺漏或衝突 | 確認信或修正建議 |
| 2 | 按 Session 分割建議拆為 3 份 Claude Code 執行 Slice | Session A/B/C Slice 文件（各 ~2.5K tokens） |
| 3 | 更新 Execution Sequence v1.1，插入 Phase 0 票序 | Exec Seq v1.1 修訂版 |
| 4 | 更新 PROGRESS.md，標記 Phase 0 開始 | PROGRESS.md 更新 |

### Session 分割建議

| Session | 票 | 估時 | 上下文負載 |
|---------|-----|------|-----------|
| **A** | P0-NAMING + P0-VF | 1.5hr | 低（重命名 + 佈局調整） |
| **B** | P0-TOPBAR + P0-BOTTOM | 5hr | 高（最大票，UI 重建） |
| **C** | P0-FPS + P0-FOCUS + Phase 0.5 截圖 | 1hr + 截圖時間 | 低（門控 + Bug fix + 截圖） |

---

## §10 文件相依性

```
本 Contract (Phase 0 UI Realignment v1.0)
  ├── 上游
  │   ├── SSOT v4.0 — 所有渲染規格、動畫規格、效能預算
  │   ├── Layout Spec v2.2 — 佈局精確定義、Pill Toggle、Scrim
  │   └── CSO Gap Analysis (Session 7) — 5 項偏離識別 + 裁決
  │
  ├── 下游
  │   ├── Gemini Execution Slices (Session A/B/C)
  │   ├── Claude Code 執行
  │   ├── Traycer 逐票審查
  │   └── Phase 0.5 Visual Baseline Gate
  │
  └── 後續
      ├── W2 RESUME Gate（Phase 0.5 PASS 後）
      └── D10 KAIROS #3（Debug Infrastructure Rule 登記）
```

---

## 文件元資訊

- **版本**: Phase 0 UI Realignment Contract v1.0
- **建立日期**: 2026-05-13
- **提交者**: CSO (Claude Opus 4.6)
- **執行鏈**: CSO → Gemini (打包 Slice) → Claude Code (實作) → Traycer (審查)
- **上游文件**: SSOT v4.0 + Layout Spec v2.2
- **驗收文件**: Phase 0.5 Visual Baseline Gate 截圖

---

**End of Phase 0 UI Realignment Contract v1.0**
