# Cueshot D6 — Gemini 架構師技術提醒與補充事項

**用途**: D6 交接單產出前貼給 Gemini
**前置**: Gemini 已啟動架構師角色 + 已讀 D5 封板狀態 + Traycer v2.0 已上線
**Sprint Day**: D6 (2026-04-15) — **KAIROS #2 節點**
**CSO 產出日期**: 2026-04-13

---

## 0. D6 前置作業（T0 — Gemini 執行）

### 0.1 SSOT 型別偏差修正登記

**背景**: D5 Traycer 終審 (Path A) 確認代碼型別鏈正確，但 SSOT 文件中存在命名漂移——部分段落以 `RuleScore` 指代 `RuleResult`，可能導致下游交接單產生型別歧義。

**決策**: 修正歸入 KAIROS #2 統一處理（D6 結束時）。D6 實作期間以下方型別對照表為準。

**Gemini 執行項**:
1. 在 `PROGRESS.md` 的 D6 區塊新增一行：`[ ] KAIROS-#2-DEBT: SSOT RuleScore/RuleResult 型別命名統一修正（D6 /dream 處理）`
2. 在 `devspec/feature_record_Backlog.md` 新增條目：`SSOT-TYPE-DRIFT-01: 文件中 RuleScore 應統一為代碼中的 RuleResult，涉及 SSOT §4.1-§4.2 段落。優先級：KAIROS #2。來源：D5 Traycer 終審觀察。`

### 0.2 型別命名規範（D5 封板確立，D6 強制遵守）

```
⚠️ 型別命名對照表 — 所有 D6 交接單與代碼必須遵守
┌─────────────┬──────────────────┬──────────────────────────────┐
│ 代碼型別     │ 職責              │ 生命週期                       │
├─────────────┼──────────────────┼──────────────────────────────┤
│ RuleResult  │ 單幀原始評估       │ Rule.evaluate() → UseCase    │
│ RuleScore   │ UseCase 聚合輸出   │ UseCase → ViewModel → UI     │
│ Instruction │ (D6+ 觀察，暫不建) │ 未來 PriorityEngine → Halo   │
└─────────────┴──────────────────┴──────────────────────────────┘

🚫 禁止事項：
- 禁止在新 Rule 的 evaluate() 回傳型別中使用 RuleScore
- 禁止在 PriorityEngine 內部創造新型別（輸入 Map<CompositionRule, RuleScore>，輸出 RuleScore?）
- 禁止為 Halo UI 新建 Instruction 型別（D6 暫用 RuleScore? 傳遞，D8 PTF 再評估）
```

---

## 1. D5 戰果與 D6 必須遵守的教訓

### 1.1 D5 交付的架構資產（D6 的基座）

| 資產 | 狀態 | D6 依賴方式 |
|------|------|-----------|
| `PriorityEngine.kt` | ✅ 封板 | D6 新 Rule 自帶 weight/confidence，Engine 零修改 |
| `AnalyzeCompositionUseCase.kt` | ✅ 封板 | D6 新 Rule 透過 Hilt `@IntoSet` 自動注入，UseCase 零修改 |
| `CompositionRule` 介面 | ✅ 封板 | D6 新 Rule 實作此介面（含 weight, confidenceFactor, evaluate()） |
| `isAllAligned = (topInstruction == null)` | ✅ 契約 | D6 不得引入獨立布林旗標，呼吸韻律聯動靠此單一真理源 |
| Sprint 測試基線 | 133t / 85 pass / 48 skip | D6 結束後 pass 數必須 ≥ 85（零回歸） |

### 1.2 D5 Traycer 終審帶出的觀察項（D6 必處理）

| 項目 | 性質 | D6 處理方式 |
|------|------|-----------|
| `invokeSafeDivide()` 反射 Helper | D1 技術債，所有調用已 @Ignore | KAIROS #2 清理：刪除 Helper + 刪除 @Ignore 測試 |
| 候選規則 13（Null Sentinel 推導布林） | D5 發現 | KAIROS #2 /dream 確認後追加 CLAUDE.md |
| SSOT RuleScore/RuleResult 命名漂移 | 文件層 | 見 §0.1，KAIROS #2 統一修正 |

### 1.3 CLAUDE.md 當前防禦網（12 條 + Bash 安全 + 1 候選）

D6 新增 Rule 時，以下 ADR 直接適用：

| ADR | D6 落地影響 |
|-----|-----------|
| UI 組件交付三條件 | HaloGuidanceLayer 必須同時完成：邏輯 + 呼叫點 + 真實 State 接線 |
| Canvas 語意探針模式 | HaloGuidanceLayer 的 Canvas draw 需用零尺寸 Box testTag 作探針 |
| Dispose 測試強制覆蓋 | Halo 從顯示→隱藏（topInstruction 從有值→null）必須驗證離開 Composition |
| `@OptIn` 治理 | ML Kit Landmark API 若有 Experimental 標記，同理 |
| Null Sentinel（候選 13） | EyeLineRule/GazeRule 的 Landmark 缺失處理必須回傳明確的 RuleResult，不得 swallow null |

---

## 2. D6 核心任務：人像構圖完整體驗

### 2.1 任務總覽

D6 要讓 Cueshot 的人像模式從「能偵測人臉位置」升級到「能診斷眼線、視線方向，並用視覺牽引引導構圖」。這是 Portrait Coach 場景從骨架到可用的關鍵一天。

```
D6 交付物拓撲：

EyeLineRule ──┐
               ├──► PriorityEngine（已建好，零修改）──► topInstruction
GazeRule ──────┘                                           │
                                                           ▼
                                            HaloGuidanceLayer（新建 Canvas）
                                                           │
                                                           ▼
                                            Eye-AF 冷卻節流（CameraX 對焦觸發）
```

### 2.2 EyeLineRule 規格（SSOT §4.3）

**職責**: 評估被攝者雙眼連線是否位於畫面上方三分線附近。

**ML Kit 依賴**:
- `Face.getLandmark(FaceLandmark.LEFT_EYE)` → PointF
- `Face.getLandmark(FaceLandmark.RIGHT_EYE)` → PointF
- 需要 `FaceDetectorOptions.LANDMARK_MODE_ALL`（D2 已設為此值，確認即可）

**演算法草案**:
```
eyeMidY = (leftEye.y + rightEye.y) / 2
targetY = imageHeight / 3.0  // 上三分線
deviation = abs(eyeMidY - targetY) / imageHeight
score = (1.0 - deviation * 3.0).coerceIn(0f, 100f) * 100f
// deviation = 0 → score 100（完美）
// deviation ≥ 0.33 → score 0（眼線在畫面底部）
```

**關鍵屬性**:
- `weight = 0.20f`（構圖類）
- `confidenceFactor = 0.80f`（ML Kit face）
- `name = "EyeLineRule"`

**邊界防禦**:
- Landmark 為 null（側臉遮擋）→ 回傳 `RuleResult(score = -1f, canImprove = false)`，PriorityEngine 自動跳過
- 單眼 null → 使用可見眼的 Y 座標作為 eyeMidY

### 2.3 GazeRule 規格（SSOT §4.4）

**職責**: 評估被攝者的視線方向（headEulerAngleY）是否朝向畫面留白側，確保「視線空間」充足。

**ML Kit 依賴**:
- `Face.headEulerAngleY` — 水平旋轉角（正值 = 臉朝左轉，即觀眾視角的右邊）
- `Face.boundingBox` — 人臉框位置

**演算法草案**:
```
faceCenterX = face.boundingBox.centerX()
imageMiddleX = imageWidth / 2.0

// 人在畫面左半 → 視線應朝右（headEulerAngleY < 0）
// 人在畫面右半 → 視線應朝左（headEulerAngleY > 0）
gazeDirectionCorrect = if (faceCenterX < imageMiddleX) {
    headEulerAngleY < -5f  // 朝右看，留白在右
} else {
    headEulerAngleY > 5f   // 朝左看，留白在左
}

score = if (gazeDirectionCorrect) {
    // 角度越大（越明確朝向留白側），分數越高
    min(abs(headEulerAngleY) / 30f * 100f, 100f)
} else {
    // 視線朝向緊邊（構圖壓迫感）
    max(0f, 50f - abs(headEulerAngleY))
}
```

**關鍵屬性**:
- `weight = 0.20f`（構圖類）
- `confidenceFactor = 0.80f`（ML Kit face）
- `name = "GazeRule"`

**邊界防禦**:
- 人臉正中央（faceCenterX ≈ imageMiddleX ± 5%）→ 視線方向不重要，回傳 `score = 80f`（輕微加分，不作為改善指令）
- headEulerAngleY 極端值（> 45° 或 < -45°）→ 人幾乎側面，Landmark 信度下降，`confidenceFactor` 應降為 0.50f

**⚠️ 架構問題（請架構師判斷）**:

**問題 D：GazeRule 的動態 confidenceFactor**

GazeRule 在 headEulerAngleY 極端值時需要降低 confidence。但 `CompositionRule.confidenceFactor` 目前是 `val`（D5 封板的介面定義）。

- 方案 1：保持 `val = 0.80f`，將信度衰減內嵌到 score 計算（score 自身就已反映不確定性）
- 方案 2：介面改為 `fun confidenceFactor(context: RuleContext): Float`（破壞 D5 封板介面）

CSO 強烈傾向方案 1 — D5 封板的介面定義是 Sprint 穩定性基石，不應在 D6 打破。信度衰減透過 score 內嵌更簡單且可測試。PriorityEngine 保持零修改。

### 2.4 HaloGuidanceLayer 規格（SSOT §2.4）

**職責**: 圓形 Focus Halo，以柔和視覺引力引導用戶將主體移向構圖最佳位置。

**設計哲學**: 這是「牽引式引導（Ambient Guidance）」的第二層——Edge Warmth 引導旋轉/傾斜（IMU 驅動），Halo 引導平移/構圖（ML Kit bbox 驅動）。兩者互不衝突，可同時顯示。

**視覺規格**:
```
┌─────────────────────────────────┐
│                                 │
│         ◯ ← Halo (目標位置)     │
│        ╱                        │
│   👤 ←── 人臉 bbox 中心         │
│                                 │
│   Halo 在目標位置脈動，          │
│   引導用戶移動手機讓人臉靠近 Halo │
│                                 │
└─────────────────────────────────┘

Halo 屬性：
- 半徑：人臉 bbox 寬度 × 0.6（與主體等比）
- 顏色：琥珀金 #FFB300 α0.35（與 Edge Warmth 統一色系）
- 描邊：2dp，無填充
- 動畫：半徑脈動 ±8%，週期 1.5s（InfiniteTransition）
- 目標位置：上三分線 × 左/右三分線交叉點（依 GazeRule 決定留白側）
```

**數據流（10fps，EMA 平滑）**:
```
ML Kit Face bbox (10fps)
    ↓ EMA smoothing (alpha = 0.3)
Smoothed Face Position
    ↓
HaloGuidanceLayer 計算目標位置
    ↓ Canvas drawCircle (60fps rendering)
Halo 顯示於目標三分交叉點
```

**與 Edge Warmth 的共存規則**:
- Edge Warmth = IMU 驅動，告訴你「手機姿態不對」
- Halo = ML Kit 驅動，告訴你「構圖位置不對」
- 兩者可同時存在（不同維度的改善建議）
- 但 PriorityEngine 的 TOP 1 機制確保 **文字指令只出一個**
- 視覺上：Edge Warmth 是邊緣光暈，Halo 是畫面中央圓環，物理位置不衝突

**顯示條件**:
- `topInstruction != null` 且 `topInstruction` 來自構圖類 Rule（ThirdsRule / HeadroomRule / EyeLineRule / GazeRule）
- 當 `isAllAligned == true` → Halo 消失，呼吸韻律啟動
- 當 topInstruction 來自非構圖類（AngleRule / LevelRule / LightRule）→ Halo 不顯示（D9+ 才有這些 Rule）

**⚠️ 架構問題（請架構師判斷）**:

**問題 E：Halo 目標位置的計算歸屬**

Halo 需要知道「目標三分交叉點在哪」。這個計算應該：
- 方案 1：在 HaloGuidanceLayer 內部算（View 層自己算目標位置）
- 方案 2：由 PriorityEngine 或 UseCase 輸出目標座標，Halo 只負責渲染

CSO 傾向方案 2 的簡化版 — UseCase 已經有 `topInstruction: RuleScore?`，其中 RuleScore 可以攜帶 `targetPosition: PointF?`。但這需要擴充 RuleScore 的欄位。

折衷方案：D6 用方案 1（Halo 內部根據 topInstruction.ruleName 判斷目標位置），D8 PTF 若需要不同的 Halo 行為再重構。避免在 D6 擴充 RuleScore。

### 2.5 Eye-AF 冷卻節流（SSOT §3.2）

**職責**: 當 PriorityEngine 判定構圖已足夠好時，觸發後置鏡頭自動對焦到眼睛區域。

**觸發條件**:
```
isAllAligned == true（或 topInstruction 為構圖類且 score > 80）
AND lastAfTriggerTime > 2000ms ago（冷卻節流）
AND facePositionShift > 5%（避免靜止時重複對焦）
```

**CameraX 實作要點**:
- 使用 `CameraControl.startFocusAndMetering(FocusMeteringAction)` 
- 對焦點 = 雙眼中點（ML Kit Landmark → 轉換為 CameraX SurfaceOrientedMeteringPointFactory 座標）
- 冷卻計時器：`System.currentTimeMillis()` 差值，不使用 `Handler.postDelayed`（避免生命週期洩漏）
- 前鏡頭（Fixed-Focus）：跳過 AF 觸發，只做視覺成功（Focus Indicator 動畫播放但不呼叫 CameraX AF）

**位移閾值計算**:
```
shift = sqrt((newEyeMidX - lastEyeMidX)² + (newEyeMidY - lastEyeMidY)²) / imageWidth
if (shift > 0.05f) → 允許重新對焦
```

**⚠️ 與 D0 Tap-to-Focus 的互斥**:
- 用戶手動 Tap-to-Focus → 立即執行，重置冷卻計時器
- Eye-AF 自動觸發 → 受冷卻節流限制
- 兩者不可同時進行（CameraX `startFocusAndMetering` 本身是序列化的，但 UI 動畫需互斥）

---

## 3. D6 技術風險

### 3.1 ML Kit Landmark 可用性

`LANDMARK_MODE_ALL` 在 D2 已設定，但 D5 測試中未實際使用 Landmark 數據。D6 是首次真正依賴 Landmark 的 Sprint Day。

風險：模擬器的 ML Kit Face Detection 可能不回傳準確的 Landmark 座標（尤其是 LEFT_EYE / RIGHT_EYE）。

建議：D6 的第一個 Ticket 應該是**驗證性 Ticket** — 在模擬器上列印 Landmark 座標，確認數據可用。如果模擬器不可靠，需立即切換到實機測試路徑（Akira 的開發環境支援 ~80% 模擬器迭代 + ~15% 交叉驗證）。

### 3.2 HaloGuidanceLayer 與 D4 呼吸韻律的共存

D4 的呼吸韻律（BreathingRing）在 `isAllAligned == true` 時啟動。D6 的 Halo 在 `topInstruction != null` 且為構圖類時啟動。邏輯上兩者互斥（isAllAligned == true ↔ topInstruction == null），但必須確保 Compose 層的 `AnimatedVisibility` / `Crossfade` 轉場不會出現兩者同時可見的閃爍。

驗證方式：D6 完成後跑 D4 的全部 instrumented tests，特別是 `CameraScreenBreathingRingTest`（5 tests）。

### 3.3 CoordinateTransformer 的精確度

Eye-AF 需要將 ML Kit 的圖像座標轉換為 CameraX 的 MeteringPoint 座標。D0 保留的 `CoordinateTransformer`（v2.12.0-14，對齊度 70%）可能需要微調。

風險點：
- ML Kit 輸出座標系 = ImageProxy 座標（旋轉後的圖像空間）
- CameraX MeteringPoint = Preview 座標系（Surface 空間）
- 前後鏡頭的映射不同（前鏡頭可能需要水平翻轉）

建議：Eye-AF 的座標轉換邏輯應獨立為可測試的 pure function，不要嵌入 ViewModel 中。

### 3.4 四規則同權重的退化行為

D6 結束後，PriorityEngine 面對 4 個構圖類 Rule（ThirdsRule, HeadroomRule, EyeLineRule, GazeRule），全部 weight = 0.20, confidence = 0.80。priority 計算退化為「score 最低的優先」。

CSO 判斷：這是正確行為。「最差的那個維度最需要改善」是合理的優先級邏輯。直到 D9（AngleRule, weight 0.40）和 D12（LightRule, weight 0.15）加入後才有真正的跨權重排序。無需為此調整。

---

## 4. D6 Ticket 拆解建議

### Batch 0：前置作業（Gemini 執行，非 Claude Code）

| Ticket | 內容 | 性質 |
|--------|------|------|
| D6-T0a | PROGRESS.md 登記 KAIROS-#2-DEBT（SSOT 型別修正） | 文件 |
| D6-T0b | devspec/feature_record_Backlog.md 登記 SSOT-TYPE-DRIFT-01 | 文件 |

### Batch 1：新 Rule 建造

| Ticket | 內容 | 性質 | 模式建議 |
|--------|------|------|---------|
| D6-B1-T1 | ML Kit Landmark 驗證（模擬器列印 LEFT_EYE / RIGHT_EYE / headEulerAngleY） | 驗證 | /uc |
| D6-B1-T2 | `EyeLineRule.kt` 新建（§2.2 演算法 + weight/confidence + 邊界防禦） | 新建 | 先 /normal 再 /uc |
| D6-B1-T3 | `GazeRule.kt` 新建（§2.3 演算法 + 正中央 deadzone + 極端角度衰減） | 新建 | 先 /normal 再 /uc |
| D6-B1-T4 | Hilt DI 綁定（EyeLineRule + GazeRule `@IntoSet`） | 修改 | /uc |
| D6-B1-T5 | 單元測試：EyeLineRule（正常 / 單眼 null / 雙眼 null / 邊界值）+ GazeRule（左半臉右看 / 右半臉左看 / 正中央 / 極端角度） | 測試 | /uc |
| D6-B1-T6 | 回歸驗證：D5 PriorityEngineTest（5t）+ D5 AnalyzeCompositionUseCaseTest（3t+）全 PASS | 驗證 | /uc |

### Batch 2：Halo 牽引 UI

| Ticket | 內容 | 性質 | 模式建議 |
|--------|------|------|---------|
| D6-B2-T1 | `HaloGuidanceLayer.kt` 新建（Canvas 圓環 + 脈動動畫 + EMA 平滑） | 新建 | 先 /normal 再 /uc |
| D6-B2-T2 | CameraScreen 接線（HaloGuidanceLayer 顯示條件 + 與 BreathingRing 互斥轉場） | 修改 | /uc |
| D6-B2-T3 | Canvas 語意探針 testTag + Dispose 測試（Halo 出現/消失） | 測試 | /uc |

### Batch 3：Eye-AF 冷卻節流

| Ticket | 內容 | 性質 | 模式建議 |
|--------|------|------|---------|
| D6-B3-T1 | `EyeAfController.kt` 新建（冷卻計時器 + 位移閾值 + 前鏡頭跳過） | 新建 | 先 /normal 再 /uc |
| D6-B3-T2 | ViewModel 接線（PriorityEngine 結果 → EyeAfController 觸發） | 修改 | /uc |
| D6-B3-T3 | 座標轉換（ML Kit Landmark → CameraX MeteringPoint，獨立 pure function） | 新建 | /uc |
| D6-B3-T4 | Eye-AF 與 Tap-to-Focus UI 動畫互斥 | 修改 | /uc |
| D6-B3-T5 | 單元測試：EyeAfController（冷卻期內拒絕 / 位移不足拒絕 / 正常觸發 / 前鏡頭跳過） | 測試 | /uc |

### Batch 4：KAIROS #2

| Ticket | 內容 | 性質 |
|--------|------|------|
| D6-T-KAIROS | `/qa` → `/sv` → Traycer strict_verification → `/dream` KAIROS #2 | 驗收 |

**KAIROS #2 輸入項預埋**:
- D5 候選規則 13（Null Sentinel 推導布林）→ 確認追加 CLAUDE.md
- D5 `invokeSafeDivide()` 技術債 → 清理決議
- SSOT RuleScore/RuleResult 命名修正 → 執行文件更新
- D6 新發現（HaloGuidanceLayer 共存行為、Landmark 模擬器精度、CoordinateTransformer 微調需求）

---

## 5. KAIROS #2 預期回顧範圍

依 Roadmap §3.3：

| 維度 | 回顧內容 |
|------|---------|
| ML Kit 整合穩定性 | Face Detection + Landmark 在模擬器/實機的數據品質差異 |
| 優先級引擎 | 四規則同權重的實際排序行為是否符合直覺 |
| Eye-AF | 冷卻節流 2000ms 是否太長/太短（需實機驗證） |
| Halo 跟蹤平滑度 | EMA alpha = 0.3 是否造成延遲感或抖動 |
| 累積技術債 | invokeSafeDivide 清理 + SSOT 型別修正 + 候選規則確認 |

---

## 6. 架構師決策請求摘要

| 問題 | CSO 傾向 | 請架構師裁決 |
|------|---------|------------|
| **D (GazeRule 動態 confidence)** | 方案 1：信度衰減內嵌 score，保持介面 `val` 不變 | 是否同意不破壞 D5 封板介面？ |
| **E (Halo 目標位置計算)** | 折衷：D6 Halo 內部算，D8 PTF 再評估 | 是否同意 View 層暫時持有目標位置邏輯？ |
| **Landmark 驗證優先** | D6-B1-T1 作為第一張票 | 是否同意在建 Rule 前先驗證數據可用性？ |
| **Batch 拆分粒度** | 4 Batch（Rule → Halo → Eye-AF → KAIROS） | 是否同意此順序？或建議調整？ |

**請架構師確認上述裁決 + Ticket 拆解後，產出 D6 交接單。**

---

## 文件元資訊

- **文件版本**: D6-Briefing-v1.0-20260413
- **建立日期**: 2026-04-13
- **作者**: CSO 策略分析 (Claude Opus 4.6) + 開發者 Akira
- **前置文件**: D5 Gemini Briefing, D5 Traycer 終審 (Path A), Cueshot_MVP_Phase1_Roadmap_v1, gstack v1.3, SSOT v3.3
- **下一步**: 貼給 Gemini 架構師，取得 D6 Batch 0-3 交接單後開始實作
