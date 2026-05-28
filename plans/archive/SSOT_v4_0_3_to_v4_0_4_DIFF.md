# SSOT v4.0.3 → v4.0.4 修訂差異文件

**產出日期**: 2026-05-28  
**產出角色**: UITA (Ambient UI Tech Auditor)  
**修訂項數**: 6 項（R29~R34）  
**觸發來源**: Session 8 — ML Kit Pipeline Architecture 審查 + Grid 實機回流 + Cold Start 過渡規格  
**新增決策**: D-20~D-24（全部 CSO 確認 locked）

---

## 合併指令（給 Gemini Merge Technician）

**嚴格合併規則**：
1. 逐項比對「搜尋錨點」定位修改位置
2. 僅修改 DIFF 指定的行，不得修改任何未提及的內容
3. 版本號、修訂日期、合併來源欄位按本文件末尾的「元資訊更新」執行
4. 完成後輸出修改行數統計供 UITA 驗證

---

## R29（🔴 Critical）：Grid 三分網格線參數 + Z-order 交換

**搜尋錨點**: `## Viewfinder Overlay 圖層定義`（約 L1728）

**原文**:
```
Layer 0: Camera Preview（CameraX PreviewView）
Layer 1: 三分網格線（#FFFFFF alpha 0.08, 線寬 0.5px）
Layer 2: Pitch Traction 指示柱（右側 16dp, 36×25% viewfinder, 琥珀金/綠色 — D-2）
```

**替換為**:
```
Layer 0: Camera Preview（CameraX PreviewView）
Layer 1: Pitch Traction 漸層（邊帶 15% viewport, 琥珀金/綠色 — D-2; 底層先繪製）
Layer 2: 三分網格線（#FFFFFF alpha 0.35, 線寬 1.5px — D-24; 在 Pitch 之上確保可見）
```

**理由**: alpha 0.08 + 線寬 0.5px 在所有 DPI 裝置上次像素級不可見（S8 實機回流確認）。Z-order 交換確保 Grid 不被 Pitch aligned 模式四邊呼吸（α0.10~0.18）遮蓋。CSO 裁定 alpha = 0.35。

---

## R30（🔴 Critical）：§6.1 ODT enableMultipleObjects 移除

**搜尋錨點**: `### 6.1 技術路徑：ML Kit Object Detection FOOD 類別`（約 L1023）

**原文**:
```kotlin
val options = ObjectDetectorOptions.Builder()
    .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
    .enableClassification()
    .enableMultipleObjects()
    .build()
```

**替換為**:
```kotlin
val options = ObjectDetectorOptions.Builder()
    .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
    .enableClassification()
    // MVP 策略 A: 單一最大 bbox（D-21）. Phase 1.5 再啟用 enableMultipleObjects()
    .build()
```

**理由**: Google 官方警告「STREAM_MODE 中不要用 multiple object detection，大多數裝置無法產生足夠幀率」。§2.5.1 MVP 策略 A = 跟隨最大 bbox = 只追蹤一個。原代碼範例與策略 A 矛盾。

---

## R31（🔴 Critical）：§10.2 ML Kit 互斥運行 + Cold Start + Thermal 備註

**搜尋錨點**: `記憶體預算：< 150MB（含 ML Kit 模型）`（約 L1186）

**在該行之後插入（不刪除原有行）**:

```markdown

**ML Kit 偵測器互斥運行原則（D-22）**：
- 同一 ImageProxy 上僅運行**一個** ML Kit 偵測器（Face Detection 或 Object Detection），不可同時開啟兩個
- 切換模式時先 `detector.close()` 再初始化新偵測器，由 500ms hysteresis（§3.2）吸收
- Google 官方約束引用：「在即時應用程式中使用串流模式時，請勿使用多個物件偵測功能，因為大多數裝置無法產生足夠的影格速率」
- ODT STREAM_MODE cold start：首 30 幀（FULL tier ≈ 3s / LIMITED ≈ 3.75s / LEGACY ≈ 6s）Halo Traction 不可用，以 Grid 網格（α0.35）+ Pitch Traction 過渡（見場景映射「Food 無 bbox」）。首個 Food bbox 到達後 300ms crossfade 至 Halo。
- Thermal throttle 響應：Android PowerManager thermal status ≥ MODERATE 時降頻至 LEGACY tier 分析率（5fps）；≥ SEVERE 時暫停 ML Kit 推理 5 秒冷卻
```

---

## R32（🟠 High）：§10.2 ML Kit 延遲行精確化

**搜尋錨點**: `| ML Kit Face/Object Detection | < 80ms | 30-60ms |`（約 L1177）

**原文**:
```
| ML Kit Face/Object Detection | < 80ms | 30-60ms |
```

**替換為**:
```
| ML Kit Face 或 Object Detection（互斥，D-22）| < 80ms | 30-60ms |
```

---

## R33（🟠 High）：§4.4 人像模式規則 — Face Detection 配置鎖定

**搜尋錨點**: `### 4.4 人像模式規則`（約 L778）

**在表格之後（SmileRule 行之後）插入**:

```markdown

**Face Detection 配置（D-23）**：

| 配置項 | 值 | 理由 |
|--------|-----|------|
| LANDMARK_MODE | ALL | 解鎖 EyeLineRule（MVP P1）+ true Eye-AF（nearest eye focus） |
| CLASSIFICATION_MODE | ALL | 解鎖 SmileRule（Phase 1.5）+ eyes-open 偵測 |
| CONTOUR_MODE | NONE | Google 官方建議：Contour 不可與 Landmark 併用（效能互斥） |
| PERFORMANCE_MODE | FAST | 正面拍攝場景足夠；PTF 側臉場景 Phase 1.5 評估切 ACCURATE |
| minFaceSize | 0.15f | 佔畫面 15% 的最小人臉 |
| enableTracking | true | 跨幀 trackingId 穩定 |
```

---

## R34（🟡 Medium）：附錄 B 新增 B.9~B.13 決策記錄

**搜尋錨點**: `### B.8 Edge-to-Edge + Display Cutout（D-18 附帶修復）`（約 L1708）

**在 B.8 整個段落結束後（即 `## Viewfinder Overlay 圖層定義` 之前）插入**:

```markdown

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

### B.13 Grid 三分網格線規格鎖定（D-24）

#FFFFFF α0.35, 線寬 1.5px, StrokeCap.Round, contentBounds 內繪製。  
Z-order: Grid 在 PitchTractionLayer 之上（Layer 2 > Layer 1）— 確保不被 Pitch aligned 模式遮蓋。  
Cold start 過渡：ODT warm-up 期間 Grid α0.35 → 首個 Food bbox 到達後 300ms 淡出至 α0.15。

**否決方案**: α0.25 過淡、α0.50 過重、不交換 Z-order（邊緣被 aligned 呼吸削弱）。
```

---

## 元資訊更新

### 文件頭（L1~L9）

**原文**:
```
**文件版本**: 4.0.3 (Session 6 Revision — D-14~D-19 + R19~R28)
**修訂日期**: 2026-05-27
**合併來源**: v4.0.2 + Session 6 (D-14~D-19, R19~R28: AF timeout · HDR Light Indicator · PreviewView · Edge Warmth remnant cleanup)
**搭配文件**: `Cueshot_Layout_Spec_v2_3.md`（佈局精確定義）
```

**替換為**:
```
**文件版本**: 4.0.4 (Session 8 Revision — D-20~D-24 + R29~R34)
**修訂日期**: 2026-05-28
**合併來源**: v4.0.3 + Session 8 (D-20~D-24, R29~R34: ML Kit Pipeline Architecture · Grid Visibility · Cold Start Transition)
**搭配文件**: `Cueshot_Layout_Spec_v2_5.md`（佈局精確定義）
```

### 文件元資訊區塊（約 L1742~L1788）

**搜尋錨點**: `- **版本**: SSOT v4.0.3`

**替換版本行**:
```
- **版本**: SSOT v4.0.4 (Session 8 Revision — D-20~D-24 + R29~R34)
- **修訂日期**: 2026-05-28
```

**搜尋錨點**: `- **搭配文件**: `Cueshot_Layout_Spec_v2_3.md``

**替換為**:
```
- **搭配文件**: `Cueshot_Layout_Spec_v2_5.md`（佈局精確定義，獨立維護）
```

**搜尋錨點**: `- **決策記錄**: D-1~D-4 (Session 3), D-5r~D-12 (Session 4), D-13 (Session 5), D-14~D-19 (Session 6) — 全部 locked`

**替換為**:
```
- **決策記錄**: D-1~D-4 (Session 3), D-5r~D-12 (Session 4), D-13 (Session 5), D-14~D-19 (Session 6), D-20~D-24 (Session 8) — 全部 locked
```

**在合併來源列表末尾（L1771 之後）插入**:
```
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
```

**搜尋錨點**: 下一步清單中 `3. Claude Code 按 SSOT v4.0.3 + Layout Spec v2.3 重新實作：`

**替換整個下一步區塊為**:
```
- **下一步**:
  1. ~~Layout Spec v2.4 → v2.5~~ → 待部署
  2. Claude Code 按 SSOT v4.0.4 + Layout Spec v2.5 實作：
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
```

---

**End of SSOT v4.0.3 → v4.0.4 DIFF**
