# SmartCameraApp v3.3 技術實作深度分析 v3

**文件版本**: Tech-Deep-Dive-v3-20260405  
**性質**: v3.3 SSOT 補充文件 — 相機控制、UI 設計、EXIF、多語系、PTF 交互、權限策略  
**作者**: CSO 策略分析 (Claude Opus 4.6) + 開發者 Akira  
**取代文件**: Tech-Deep-Dive-v2-20260404（已作廢）、Tech-Deep-Dive-v1-20260404（已作廢）  
**變更紀錄**:
- v3 (20260405): PTF Tab toggle 雙路徑退出、快門冷卻、三級裝置效能相容、Settings 移除 Coaching intensity/Haptic feedback、啟動權限策略重寫、免費限制觸發式說明

---

## 第一章：前置鏡頭策略（人像自拍場景）

### 1.1 硬體現實：前鏡頭是 Fixed-Focus

絕大多數 Android 裝置的前置鏡頭是**定焦 (Fixed-Focus)**，沒有對焦馬達。

| 能力 | 前置鏡頭現實 |
|------|-------------|
| AF (Auto Focus) | ❌ 不可用 — 無對焦馬達，`startFocusAndMetering` 的 AF flag 無效 |
| AE (Auto Exposure) | ✅ 可用 — 曝光調整是 sensor 電子層面，不需馬達 |
| AWB (Auto White Balance) | ✅ 可用 — 同上 |
| Eye-AF | ❌ 不適用 — 沒有 AF 馬達，無法對焦到眼睛 |

**結論：前鏡頭不需要 Eye-AF。** 但需要 **Eye-AE**（以眼睛區域為曝光測光基準，避免逆光時人臉黑掉）。

### 1.2 前置鏡頭的 Eye-AE 實作

```kotlin
fun applyFrontCameraEyeAE(face: Face, meteringFactory: MeteringPointFactory) {
    val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)?.position ?: return
    val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position ?: return
    val eyeCenterX = (leftEye.x + rightEye.x) / 2f
    val eyeCenterY = (leftEye.y + rightEye.y) / 2f
    val meteringPoint = meteringFactory.createPoint(eyeCenterX, eyeCenterY, 0.15f)
    val action = FocusMeteringAction.Builder(
        meteringPoint,
        FocusMeteringAction.FLAG_AE or FocusMeteringAction.FLAG_AWB
    ).setAutoCancelDuration(3, TimeUnit.SECONDS).build()
    cameraControl.startFocusAndMetering(action)
}
```

### 1.3 前置鏡頭的人像六規則適用性

| 規則 | 前置鏡頭適用? | 備註 |
|------|-------------|------|
| ThirdsRule | ✅ 完全適用 | 人臉中心 vs 三分交叉點 |
| HeadroomRule | ✅ 完全適用 | 自拍時頭部空間更需引導 |
| EyeLineRule | ✅ 完全適用 | 眼睛 Y 位置 vs 上三分線 |
| GazeRule | ⚠️ 有限適用 | 單人自拍時降級：僅 `\|headEulerAngleY\| > 15°` 才觸發。多人自拍恢復正常 |
| TiltRule (Phase 1.5) | ✅ 完全適用 | 自拍時頭歪很常見 |
| SmileRule (Phase 1.5) | ✅ 完全適用 | 自拍黃金時刻 |

### 1.4 前置鏡頭 Screen Flash

```kotlin
imageCapture.flashMode = ImageCapture.FLASH_MODE_SCREEN  // CameraX 1.4+
```

前置鏡頭時閃光燈圖標改為螢幕閃光圖標（手機 + 光線發散），只有 Off / On 兩態。

---

## 第二章：後置鏡頭策略（全場景支援）

### 2.1 後置鏡頭能力矩陣

| 能力 | 是否需要 | 用途 | 實作方式 |
|------|---------|------|---------|
| Tap-to-Focus (AF+AE) | ✅ 必須 | 用戶點擊螢幕對焦+測光 | `FocusMeteringAction(FLAG_AF \| FLAG_AE)` |
| Eye-AE/AF | ✅ 人像模式 | 自動對焦到人眼 | ML Kit 眼睛座標 → `startFocusAndMetering` |
| AE/AF Lock | ✅ Phase 1.5 | 長按鎖定曝光/對焦 | Camera2 Interop: `CONTROL_AE_LOCK` + `AF_MODE_AUTO` |
| 食物對焦 | ✅ 美食模式 | 對焦到食物 bbox 中心 | Object Detection bbox → `startFocusAndMetering` |
| 連續對焦 | ✅ 預設 | CameraX 預設 `CONTINUOUS_PICTURE` | 不需額外設定 |
| 曝光補償 | Phase 2 | 過暗/過亮時微調曝光 | `CameraControl.setExposureCompensationIndex()` |

### 2.2 Eye-AF 觸發策略：高頻視覺 vs 低頻硬體脫鉤

**總原則**：Halo 視覺追蹤跑 10fps（純 UI），VCM 馬達呼叫採用**事件驅動 + 冷卻節流**。

**三條件同時滿足才呼叫 `startFocusAndMetering`**：

1. 距離上一次 AF 呼叫 ≥ cooldown（人像 2000ms / 美食 3000ms）
2. 眼睛/bbox 座標偏移量 > 閾值（人像 5% / 美食 8% 螢幕對角線）
3. 當前 AF 狀態 ≠ ACTIVE_SCAN

```kotlin
fun onFaceUpdate(face: Face) {
    val eyeCenter = calcEyeCenter(face)
    
    // 高頻路徑：每幀更新 Halo 視覺位置（10fps，純 UI）
    haloLayer.updatePosition(eyeCenter)
    
    // 低頻路徑：硬體對焦（事件驅動 + 冷卻）
    val displacement = distance(eyeCenter, lastAfTarget)
    val elapsed = now - lastAfTimestamp
    if (elapsed >= AF_COOLDOWN_MS
        && displacement > screenDiagonal * DISPLACEMENT_THRESHOLD
        && !isAfScanning) {
        triggerEyeAF(eyeCenter)
        lastAfTarget = eyeCenter
        lastAfTimestamp = now
    }
}

companion object {
    const val AF_COOLDOWN_MS = 2000L
    const val FOOD_AF_COOLDOWN_MS = 3000L
    const val DISPLACEMENT_THRESHOLD = 0.05f
    const val FOOD_DISPLACEMENT_THRESHOLD = 0.08f
}
```

### 2.3 光流追蹤：對焦框不需要

| 追蹤需求 | 方案 | 需要光流? |
|---------|------|----------|
| 人臉 Halo | ML Kit Face Detection 跨幀追蹤 | ❌ |
| 食物 Halo | ML Kit Object Detection trackingId | ❌ |
| 對焦圈 | 用戶觸摸座標（固定） | ❌ |
| Eye-AF 座標 | ML Kit Landmark 每幀輸出 | ❌ |
| IMU 補償 | ImuCompensator 光流 RANSAC | ✅ 已有 |

ML Kit 掉幀間隙用 EMA 平滑：`smoothed = last * 0.7 + new * 0.3`

### 2.4 場景對焦策略總覽

| 場景 | 對焦策略 | AE 策略 | AF 冷卻 | 位移閾值 |
|------|---------|---------|---------|---------|
| Portrait (後置) | Eye-AF | Eye-AE | 2000ms | 5% |
| Portrait (前置) | 無 AF | Eye-AE | 2000ms | 5% |
| Food | Food-AF（bbox 中心） | Food-AE | 3000ms | 8% |
| Pass-to-Friend | Eye-AF | Eye-AE | 2000ms | 5% |
| General | CONTINUOUS_PICTURE | Auto AE | — | — |
| AE/AF Lock | 鎖定 | 鎖定 | — | — |

---

## 第三章：對焦 UI 動畫設計

### 3.1 與 Halo 語義區分

| 維度 | Halo 引導 | 對焦指示 |
|------|----------|---------|
| 觸發 | AI 自動 | 用戶觸摸 / AI 輔助 |
| 重量 | 2dp + α 0.25-0.35 | 1.5dp + 快速消散 |
| 持續 | 持續存在 | 2 秒後淡出 |
| 顏色 | 金色 ↔ 綠色 | 白色 → 綠色/琥珀 |

### 3.2 狀態機

```
[Idle] → 觸摸 → [Focusing] → 成功 → [Focused] → 2s → [FadeOut] → [Idle]
                             → 失敗 → [Failed] → 1s → [FadeOut] → [Idle]
[Idle] → 長按 1s → [Focusing] → [AE/AF Locked] → 再觸摸 → [Idle]
```

### 3.3 動畫規格

| 狀態 | 視覺效果 | 顏色 | 持續時間 |
|------|---------|------|---------|
| Focusing | 1.3x→1.0x 縮放 + 60° 弧線旋轉 | 白色 α=0.9 | 至 AF 完成 |
| Focused | 穩定 + 脈動一次 | 綠色 α=0.8 | 1.5s + 0.5s 淡出 |
| Failed | 左右震動 2px | 琥珀 α=0.6 | 0.8s + 0.5s 淡出 |
| Locked | 雙圈 + "AE/AF" 小字 | 亮金 α=0.85 | 至解鎖 |

### 3.4 手勢

| 手勢 | 行為 |
|------|------|
| 單點觸摸 | Tap-to-Focus (AF+AE) |
| 長按 1 秒 (Phase 1.5) | AE/AF Lock |
| Lock 中再單點 | 解鎖 + 重新對焦 |
| 雙指捏合 | 數位變焦 |

---

## 第四章：預覽畫面 UI 介面設計

### 4.1 設計哲學

- **圖示優先 (Icon-First)**：拍攝畫面零文字標籤（唯一文字 = "HDR" + "PTF"，視為圖示）
- **控制項最少化**
- **不支援 = 完全隱藏**（不反灰）
- **不顯示場景模式標籤**（AI 誤判會破壞信任 + 違反 Ambient 哲學）
- **無側邊快捷列**（永久捨棄，低頻參數全放 Settings）

### 4.2 Top Bar

```
[⚡] [HDR] [💧]                          [⚙] [?]
←── 拍攝中即時控制 ──→        ←── 離開拍攝 ──→
   使用頻率×支援度降序              極低頻
```

| 位置 | 按鈕 | 頻率 | 支援度 |
|------|------|------|--------|
| 最左 | ⚡ 閃光燈 | 最高 | 100% |
| 左二 | HDR | 中低 | ~70%（不支援時隱藏） |
| 左三 | 💧 髒汙（動態） | 偶發 | 100% |
| 右二 | ⚙ Settings | 極低 | — |
| 最右 | ? 使用手冊 | 極低 | — |

💧 動態插入用 `AnimatedVisibility(slideIn)`，⚡ 和 HDR 位置不動。

閃光燈三態（純圖示）：Off = ⚡+斜線 / Auto = ⚡+小"A" / On = ⚡實心。前置 = 📱+光線，二態。

**無返回鍵**：Android 全面屏手勢已普及。

### 4.3 Bottom Bar

```
[📷 Gallery 40dp]    [◉ Shutter 72dp]    [🔄 Flip 40dp]
```

快門外環 = Ambient UI 呼吸韻律：`allConditionsMet` 時綠色呼吸（2s 週期），否則白色靜態。

### 4.4 PTF Tab（底部，主控制列下方）

**位置**：貼螢幕底邊，全寬 36dp。加 `navigationBarsPadding()` 處理手勢導航 vs 三鍵導航。

**行為**：**Tab 是 toggle**——點擊進入 PTF 模式，再點擊退出 PTF 模式（首次進入例外，見 §4.5）。

**觸摸熱區**：視覺高度 36dp，Compose `clickable()` 自動擴展至 48dp 最小觸摸目標。

```kotlin
@Composable
fun PtfTabBar(isPtfMode: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(
        if (isPtfMode) Color(0xFF50C864).copy(alpha = 0.08f)
        else Color.Black.copy(alpha = 0.35f), tween(300))
    val contentColor by animateColorAsState(
        if (isPtfMode) Color(0xFF50C864).copy(alpha = 0.6f)
        else Color.White.copy(alpha = 0.5f), tween(300))
    Surface(color = bgColor,
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .navigationBarsPadding()
            .clickable { onClick() }) {
        Row(Arrangement.Center, Alignment.CenterVertically, Modifier.fillMaxSize()) {
            Icon(Icons.Outlined.PeopleAlt, null, tint = contentColor, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(if (isPtfMode) "PTF mode" else "PTF",
                 color = contentColor, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }
    }
}
```

### 4.5 PTF 首次使用 Bottom Sheet（20%）

僅首次點擊 PTF Tab 時顯示。高度 = `max(screenHeight * 0.20, 96.dp)`。

```
┌──────────────────────────────────────────────┐
│  ━━━━━ (把手)                                  │
│  🤝 Pass to friend              [ ▶ Start ]   │
│  Let friends take great photos   (Free: 3/mo) │
│  ☐ Don't show again                           │
└──────────────────────────────────────────────┘
```

後續直接進入 + Toast「Free: 2/3 remaining」(3s 淡出)。次數用完 → Toast 提示升級，不進入。

### 4.6 Normal → PTF 切換（300ms，純 UI 層）

```
isPtfMode = true →
  ├── Top Bar: slideOut(Up) + fadeOut
  ├── Gallery: fadeOut + slideOut(Left)
  ├── Flip: fadeOut + slideOut(Right)
  ├── Shutter: 72dp → 80dp
  ├── Exit 按鈕: fadeIn（快門左側，純 ✕ 圖示，無文字）
  ├── PTF Tab: 黑→綠微光, "PTF"→"PTF mode"
  ├── Edge Warmth: amber → green
  └── Halo: target=畫面中心, amber → green
```

**零硬體操作**：無 CameraX rebind、無 ML Kit 重啟（PTF 和 Portrait 都用 Face Detection）、無 IMU 重校、無 shader 重編。

**快門冷卻**：進入 PTF 後快門 1 秒內不可按（防止朋友未拿穩手機誤觸）。

```kotlin
var shutterCooldown by remember { mutableStateOf(false) }

LaunchedEffect(isPtfMode) {
    if (isPtfMode) {
        shutterCooldown = true
        delay(1000)
        shutterCooldown = false
    }
}
```

**效能可行性驗證**：

| 切換動作 | 發生什麼 | 延遲 | 風險 |
|---------|---------|------|------|
| UI 轉場 | Top Bar fadeOut + Gallery/Flip fadeOut + Shutter scale up | ~300ms 動畫 | 無 — 純 Compose 動畫 |
| ML Kit 偵測器切換 | 不需要 — PTF 仍用 Face Detection | 0ms | 無 |
| 規則引擎切換 | 從六規則降為三規則 | < 0.1ms | 反而更輕量 |
| Edge Warmth 色系 | 改 color 變數 | 0ms | 無 |
| Halo 模式 | 改 targetPosition 計算 | 0ms | 無 |
| CameraX use case | 不需重新綁定 | 0ms | 關鍵：零 CameraX rebind |

**關鍵的「不發生」清單**：

| 不會發生的事 | 為什麼重要 |
|------------|----------|
| CameraX rebind | 不會黑屏、不會預覽中斷 |
| ML Kit 偵測器 close/reopen | PTF 和 Portrait 都用 Face Detection |
| IMU 重新校準 | ImuCompensator 持續運行 |
| 記憶體重分配 | 無新模型載入 |
| GPU shader 重編譯 | Edge Warmth 只改 uniform 變數 |

**三級裝置相容性**：

| 裝置等級 | Tab 切換表現 |
|---------|------------|
| FULL / LEVEL_3 | 300ms 流暢轉場，所有動畫完整 |
| LIMITED | 300ms 流暢轉場，動畫簡化（crossfade 取代 scale）|
| LEGACY | 200ms 硬切（跳過動畫），但功能完整 |

### 4.7 PTF 模式 UI

```
┌──────────────────────────────────────────────┐
│  （Top Bar 消失）                               │
│          綠色 Edge Warmth 四邊呼吸               │
│              ◯ 綠色 Halo (人臉)                 │
│           [Looking great!] pill                 │
├──────────────────────────────────────────────┤
│    [✕]              [◉ 80dp Shutter]           │
├──────────────────────────────────────────────┤
│  🤝 PTF mode (綠色微光)                        │
└──────────────────────────────────────────────┘
```

**退出方式：雙路徑**

| 路徑 | 操作 | 適用對象 |
|------|------|---------|
| Tab toggle | 再次點擊底部 PTF Tab | 手機主人（知道 Tab 在哪）|
| ✕ 圖示按鈕 | 點擊快門左側 ✕ 圖示（純圖示，無文字）| 朋友（看到 ✕ 直覺理解）|

兩條路徑都執行同一個 `isPtfMode = false`，完全對稱的退出動畫。

**Exit 按鈕規格**：

```kotlin
// ✕ Exit 按鈕 — 純圖示，無文字
IconButton(
    onClick = { isPtfMode = false },
    modifier = Modifier.size(40.dp)
) {
    Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "Exit PTF mode",
        tint = Color.White.copy(alpha = 0.7f),
        modifier = Modifier.size(24.dp)
    )
}
```

| 變化 | 理由 |
|------|------|
| Top Bar 消失 | 朋友不需要 |
| Gallery/Flip 消失 | 朋友不需要 |
| 快門 80dp | 一看就懂 |
| 只剩 ✕ + Shutter + Tab | 三個動作 |
| 綠色語言 | Aligned = 可以拍了 |
| ✕ 純圖示 | 圖示優先原則，跨語言零翻譯成本 |

### 4.8 PTF → Normal 模式切換（退出）

完全對稱：UI 元素 fadeIn 回來、規則引擎恢復六規則、Edge Warmth 切回金色。同樣零硬體操作、零 CameraX rebind。

```
isPtfMode = false →
  ├── Top Bar: slideIn(Down) + fadeIn
  ├── Gallery: fadeIn + slideIn(Left)
  ├── Flip: fadeIn + slideIn(Right)
  ├── Shutter: 80dp → 72dp
  ├── Exit 按鈕: fadeOut
  ├── PTF Tab: 綠微光→黑, "PTF mode"→"PTF"
  ├── Edge Warmth: green → amber
  └── Halo: target=三分交叉點, green → amber/gold
```

### 4.9 完整 Normal Mode 佈局

```
┌──────────────────────────────────────────────┐
│  [⚡][HDR][💧]                       [⚙] [?] │  Top Bar
├──────────────────────────────────────────────┤
│  Edge Warmth + Halo + 三分法網格 + 對焦圈       │  相機預覽 + Ambient UI
├──────────────────────────────────────────────┤
│    [📷]          [◉]           [🔄]            │  主控制列
├──────────────────────────────────────────────┤
│  ━━━━━━━  🤝 PTF                               │  PTF Tab (底部)
└──────────────────────────────────────────────┘
```

### 4.10 Gemini Mockup 偏差記錄

用 Gemini 生成的 UI Mockup 有以下偏差，不影響架構決策，技術規格以本文件為準：

**PTF Mode Mockup 偏差**：Exit ✕ 按鈕未顯示、Halo 圓心偏高（應在眼睛區域）、Edge Warmth 呈藍紫色（應為綠色呼吸，場景燈光干擾 Gemini 理解）。

**Normal Mode 美食 Mockup 偏差**：Top Bar 完全遺漏、快門外環灰色（應為白色靜態或綠色呼吸）、Edge Warmth 不可見、食物 Halo 不可見。

---

## 第五章：Settings 介面設計

### 5.1 結構

```
Settings
├── 📷 Camera
│   ├── Aspect ratio            [4:3] / [16:9]     預設 4:3
│   ├── Grid overlay            [Off] / [Rule of thirds] / [Golden ratio 🔒]
│   ├── Save location (GPS)     [Toggle Off]
│   │   └── Location info will be saved in photo metadata
│   ├── Shutter sound           [Toggle]            日韓法規強制
│   ├── Save original selfie    [Toggle On]
│   │   └── Photos are saved as others see you, not as the mirrored preview.
│   └── HEIF format             [Toggle]            支援裝置才顯示
│       └── Smaller file size with the same quality.
├── 🎯 Coaching
│   ├── Lens smudge detection   [Toggle On]
│   └── Flash reminder          [Toggle On]
├── ⚡ Performance
│   └── Performance mode        ○ Auto / ○ Battery saver / ○ Full features
├── 💎 Subscription
│   ├── Current plan / Manage / Restore
├── ℹ️ About
│   ├── Version / Privacy / Terms / Licenses / Feedback
└── 🌐 Language                 [System default] / 手動選擇
```

**v3 變更說明**：

| 項目 | 狀態 | 理由 |
|------|------|------|
| Coaching intensity | ❌ MVP 不顯示 | Subtle 定義需 IFTR 基線數據支撐，先用 Normal hardcode |
| Haptic feedback | ❌ 不存在 | 震動影響拍攝穩定性 + 違反 Ambient 無干擾哲學 |
| Analysis resolution | ❌ 已捨棄 | 由效能模式內部自動決定 |

### 5.2 長寬比（僅 4:3 + 16:9）

| 比例 | 解析度 | 適用 |
|------|--------|------|
| 4:3 (預設) | sensor 原生，最大像素 | 美食 |
| 16:9 | 裁切上下 ~25% | 風景 |

1:1 永久捨棄（數位裁切、非 sensor 原生、增加預覽成本）。

### 5.3 說明文字 UI 規格

帶說明的項目統一：`12sp`, `Color.White.copy(alpha = 0.5f)`, Toggle 正下方。

### 5.4 風格

| 元素 | 規格 |
|------|------|
| 背景 | 深灰 #121212 |
| 標題 | 白色 16sp Medium |
| 說明 | 白色 α0.5 12sp |
| Toggle Active | 琥珀金 #D4A030 |
| 分區標題 | 琥珀金 12sp 全大寫 |
| 付費標記 | 🔒 + "Pro" pill |

---

## 第六章：EXIF 資訊設計

### 6.1 CameraX 自動寫入

TAG_MAKE, TAG_MODEL, TAG_DATETIME, TAG_IMAGE_WIDTH/LENGTH, TAG_ORIENTATION, TAG_FOCAL_LENGTH, TAG_F_NUMBER, TAG_EXPOSURE_TIME, TAG_ISO_SPEED_RATINGS, TAG_WHITE_BALANCE, TAG_FLASH

### 6.2 手動寫入

| Tag | 內容 | 原因 |
|-----|------|------|
| TAG_GPS_* | 地理位置 | 需授權 + Settings |
| TAG_USER_COMMENT | SmartCamera JSON | 評分資料 |
| TAG_SOFTWARE | "SmartCameraApp v1.0.0" | App 識別 |

### 6.3 自訂 JSON 結構

```json
{
  "smartcamera_version": "1.0.0",
  "score": 82, "mode": "FOOD", "aspect_ratio": "4:3",
  "front_camera": false, "hdr_enabled": false,
  "rules": {
    "angle": { "score": 95, "target_deg": 45, "actual_deg": 43.2 },
    "level": { "score": 88, "roll_deg": 0.6 },
    "composition": { "score": 72, "thirds_distance": 0.12 },
    "light": { "score": 68, "histogram_mean": 0.42 }
  },
  "iftr": true, "minimalist_detected": false,
  "busy_background": false, "dof_applied": false,
  "lens_smudge_detected": false
}
```

---

## 第七章：多語系 (i18n)

### 7.1 MVP 語言

| 優先 | 語言 | code | 理由 |
|------|------|------|------|
| P0 | 英文 | en | 全球預設 |
| P0 | 繁體中文 | zh-rTW | Akira 母語 + 台灣市場 |
| P0 | 日文 | ja | 美食文化（AI/社群翻譯） |
| P1 | 簡體中文 | zh-rCN | 最大 Android 市場 |
| P1 | 韓文 | ko | 美食文化 |

### 7.2 關鍵字串（en / zh-TW / ja）

| key | en | zh-TW | ja |
|-----|----|----|-----|
| ptf_title | Pass to friend | 遞手機 | 友達に渡す |
| ptf_aligned | Looking great! | 很棒！ | いい感じ！ |
| ptf_remaining | Free: %1$d/%2$d remaining | 免費：剩餘 %1$d/%2$d 次 | 無料：残り %1$d/%2$d 回 |
| settings_save_original_selfie | Save original selfie | 儲存原始自拍 | オリジナルの自撮りを保存 |
| smudge_detected | Lens may be smudged... | 鏡頭可能有髒汙... | レンズが汚れている可能性... |
| paywall_unlock | Unlock full diagnosis | 解鎖完整診斷 | フル診断をアンロック |

### 7.3 多語系 UI 防禦

- 所有 Text: `maxLines = 2, overflow = Ellipsis`
- 按鈕: `wrapContentWidth()`
- **圖示優先紅利**: Top Bar + Bottom Bar 全圖示 = 零翻譯、零長度問題
- Compose Preview: `@Preview(locale = "de")` 測最長語言
- 日韓快門聲: Toggle 強制 On + disabled

---

## 第八章：開發優先級矩陣

| 功能 | MVP | Ph 1.5 | Ph 2 |
|------|-----|--------|------|
| Tap-to-Focus + 圓形動畫 | ✅ D1-2 | | |
| 前置 Eye-AE | ✅ D5-6 | | |
| 後置 Eye-AF（事件驅動+冷卻） | ✅ D5-6 | | |
| 食物自動對焦（3s 冷卻） | ✅ D9-10 | | |
| AE/AF Lock（長按） | | ✅ | |
| 閃光燈 (Off/Auto/On) | ✅ D1 | | |
| Screen Flash (前置) | | ✅ | |
| HDR Toggle | | ✅ | |
| 前後鏡頭切換 | ✅ D1 | | |
| Gallery 入口 | ✅ D13 | | |
| PTF Tab toggle + 直接切換 | ✅ D7-8 | | |
| PTF 首次 Bottom Sheet | ✅ D7-8 | | |
| PTF Exit ✕ 圖示按鈕 | ✅ D7-8 | | |
| PTF 快門 1s 冷卻 | ✅ D7-8 | | |
| 4:3/16:9 | ✅ Settings | | |
| GPS EXIF | ✅ Settings | | |
| 三分法網格 | ✅ | | |
| 鏡頭髒汙 Toggle | ✅ | | |
| 效能模式 | ✅ | | |
| Save original selfie | ✅ | | |
| 多語系 en/zh-TW/ja | ✅ | | |
| 多語系 ko/zh-CN | | ✅ | |
| 曝光補償 Slider | | | ✅ |
| 計時器 3s/10s | | ✅ | |
| Coaching intensity Toggle | | ✅ | |

---

## 第九章：首次開啟 App 體驗流程

### 9.1 啟動序列

```
步驟 1: 相機權限 + 相片存取權限（同時請求，缺一不可）
        → 授權 → 繼續
        → 拒絕 → 顯示說明頁，引導至系統設定

步驟 2: 3 秒 Ambient UI 教學動畫（v3.3 SSOT §2.6）

步驟 3: 進入正常拍攝模式
```

### 9.2 PTF 首次使用流程

```
首次點擊 PTF Tab:
  → 20% Bottom Sheet（說明 + 免費限制 + Start + Don't show again）
  → 點 Start → 進入 PTF 模式

後續點擊 PTF Tab:
  → 直接進入 + Toast「Free: 2/3 remaining」(3s)

當月次數用完:
  → Toast「PTF limit reached. Upgrade to Pro.」→ 不進入

付費用戶:
  → 永遠直接進入，無任何限制提示
```

### 9.3 權限請求策略

| 權限 | 請求時機 | 理由 |
|------|---------|------|
| 相機 (CAMERA) | App 首次啟動 | 核心功能，缺此權限 App 無法運作 |
| 儲存 (READ/WRITE_EXTERNAL_STORAGE 或 MediaStore) | App 首次啟動 | 拍照後需存檔，與相機同等必要 |
| 位置 (ACCESS_FINE_LOCATION) | 用戶在 Settings 首次開啟 GPS Toggle | 非必要權限，延遲請求提高核准率 |

**GPS 延遲請求理由**：位置權限在用戶心中敏感度高於相機和儲存。啟動時連問三個權限的核准率會下降。GPS 預設 Off，用戶有意識地去 Settings 開啟時再請求，核准率接近 100%。

### 9.4 免費版限制說明策略（觸發式，非啟動式）

**原則**：用戶第一次體驗到價值（看到評分卡片）的瞬間，才是告知限制的最佳時機。在啟動時說限制，用戶還不知道這東西值不值得付錢。

| 觸發點 | 說明方式 |
|-------|---------|
| 首次拍照完成 → 拍後教學卡片底部 | 一行小字：「5 free scored photos today · Upgrade for unlimited」|
| 第 6 張照片 | Toast：「Daily limit reached. Upgrade to Pro for unlimited.」|
| PTF 首次點擊 | 20% Bottom Sheet（§9.2）|
| PTF 次數用完 | Toast 提示升級 |

---

## 文件元資訊

- **文件版本**: Tech-Deep-Dive-v3-20260405
- **建立日期**: 2026-04-05
- **作者**: CSO 策略分析 (Claude Opus 4.6) + 開發者 Akira
- **前置文件**: SmartCameraApp_Strategy_v3_3_SSOT_20260404.md
- **取代文件**: Tech-Deep-Dive-v2-20260404（已作廢）、Tech-Deep-Dive-v1-20260404（已作廢）
- **整合來源**: v2 全部內容 + 20260405 對話決議（PTF Tab toggle、快門冷卻、Settings 精簡、權限策略、免費限制觸發式說明）
- **下一步**: 作為 D1 實作參考
