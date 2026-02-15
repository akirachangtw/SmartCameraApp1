<!-- generated: 2026-02-02 14:31:20 -->

# Level 2 可行性檢視備忘錄（給 Claude Code 檢視 repo 用）
主題：optical-flow-tracking（CameraX 即時預覽 tap-to-track point）

---

## 1. 可行性結論（可行 / 不可行 / 需調整）
**需調整（可行但需先釐清管線與座標 mapping，且建議分階段交付）**

理由（給落地判斷用）：
- **功能本身可行**：以 ImageAnalysis 連續幀做 sparse tracking（LK optical flow / 或 template matching）在 Android 是成熟路線。
- **但 repo 目前 tracking 定義偏向「ML Kit object」**，且 UI/狀態有「focus fade / AEAF lock / tracking spring」等既有規則；直接塞 optical flow 容易造成互斥規則混亂與 mapping 偏移。
- **最大不確定性**在於：CameraX Analyzer 現況（解析度、backpressure、thread model）、目前 preview ↔ analysis ↔ overlay 的座標轉換是否已被封裝且正確處理 rotation/mirror/crop。

交付建議：先做 **Plan B（ROI template matching）MVP** 驗證 mapping/狀態機，再評估導入 **Plan A（OpenCV LK sparse）**。

---

## 2. 關鍵假設（Assumptions：哪些需由 repo 或 log 來驗證）
1. **CameraX use cases 組合**
   - 假設目前有 `Preview + ImageAnalysis`（可能還有 `ImageCapture`），且 Analyzer 有固定入口可擴充。
2. **Analyzer 的輸入格式**
   - 假設可取得 `ImageProxy` 並轉灰階（Y plane）且能取得 `imageInfo.rotationDegrees`。
3. **座標 mapping 現況**
   - 假設目前已存在「偵測框/追蹤點」從 analysis 座標轉到 UI overlay 的 mapping（或至少已有工具函式）。
   - 若目前僅用 PreviewView 的 meteringPointFactory 做對焦點，而 UI 只畫在 Compose 座標，則 optical flow 的點位更新需要額外 mapping。
4. **tracking 更新頻率**
   - 假設現有 tracked object position 更新不是每 frame 都更新 UI（或即使更新也不會造成 Compose 過度重組/卡頓）。需從 log 驗證。
5. **狀態互斥規則尚未定義**
   - 假設目前「trackedObjectId != null」即視為 tracking mode；新增 trackedPoint 必須定義優先權，否則會打架。
6. **依賴可接受度**
   - 假設專案可接受新增 OpenCV（apk size/ABI/NDK），或至少可先用純 Kotlin/RenderScript 替代方案（但 RS 已淘汰，不建議）。

---

## 3. 高風險點（High-risk areas：CameraX Preview/ImageAnalysis/模式切換/Thread/權限）
1. **CameraX ImageAnalysis Backpressure / latency**
   - 若 Analyzer 做 tracking 計算過重，會造成 `ImageAnalysis` 積壓，導致掉幀或預覽卡頓。
   - 需確認 `STRATEGY_KEEP_ONLY_LATEST` 是否已設定，以及 analyzer dispatcher/thread 是否與 ML Kit 共用。
2. **座標 mapping（最高風險）**
   - analysis buffer (Y plane) 座標 → PreviewView 顯示座標 → Compose Canvas 座標
   - 需處理：rotationDegrees、前鏡頭 mirror、PreviewView scaleType（FILL_CENTER/FIT_CENTER）、裁切（sensor crop rect）。
3. **模式切換互斥**
   - 既有：靜態點擊對焦 + fade、AE/AF lock（長按）、ML Kit 物件追蹤。
   - 新增：point tracking（任意點）後，需定義：
     - 追蹤中再次點擊：重設點 / 結束追蹤？
     - AEAF lock 時是否允許追蹤？lock 是否凍結追蹤點？
     - ML Kit tracking 與 point tracking 的優先級（同時存在時畫哪一個？用哪個驅動 indicator？）
4. **追蹤漂移 / lost handling**
   - 無 quality gate 會造成 UI 指示器亂跳（用戶信任崩）。
   - 必須至少有：ROI 邊界檢查、matching score 或 optical-flow status 判定、出界結束。
5. **Thread/同步風險**
   - 點擊事件發生在 UI thread；tracking 狀態更新在 analyzer thread；需要 thread-safe state（Flow/StateFlow）與避免競態（例如切鏡頭、pause/resume 時）。
6. **權限/生命週期**
   - Activity/Compose lifecycle（onPause/onResume）中，tracking 的上一幀 buffer 是否要清空；相機重綁定後追蹤應自動終止。

---

## 4. 最小差異建議（Minimal diff approach：步驟化）
> 目標：先讓「點一下 → indicator 會跟著同一塊影像內容移動」成立；再提升準確度與效能。避免一次導入 OpenCV + 大改 state。

### Step 0｜先不碰 OpenCV：用 Plan B（ROI template matching）做 MVP
1. 新增一個獨立的 tracking 元件（建議新檔案）：
   - `PointTrackingManager`（或 `TemplateTracker`）
   - 職責：持有 template patch、上一幀灰階、當前點座標、輸出新座標+quality
2. 點擊時：
   - 將 UI tap 座標轉成 analysis 座標（或反過來：以 PreviewView 的 `meteringPointFactory`/view transform 推得 sensor/analysis 座標）
   - 從當前 frame 擷取 template（例如 32x32 或 48x48 灰階）
3. 每個 analyzer frame：
   - 在前一次點附近的 search window（例如 ±24px 或 ±40px）做 matching（SSD/NCC）
   - 得到最佳位置作為新點；若 score 低於閾值則 lost
4. UI 顯示：
   - `isTracking` 判斷擴充：`trackedObjectId != null || trackedPoint != null`
   - tracking 模式 alpha 固定 1.0（沿用既有 FocusIndicator）
5. 結束條件：
   - 用戶再點擊 → 重設 template / 重設追蹤點
   - 追蹤點出界（mapping 到預覽範圍外）→ 停止追蹤並清 state

### Step 1｜確認 mapping 正確後，再升級 Plan A（OpenCV LK sparse）
1. 將 Plan B 的追蹤介面抽象化：
   - `interface PointTracker { fun onInit(...); fun onFrame(...): TrackingResult }`
2. 新增 OpenCV tracker 實作（LK sparse / `calcOpticalFlowPyrLK`）：
   - 使用降採樣灰階（例如把 Y plane downscale 到 320~480 寬）
   - 每 N 幀 re-acquire（或用 `goodFeaturesToTrack` 在 ROI 重新找點）降低 drift
3. 加入限頻：
   - 例如只在 15fps 更新 tracking，或每 2 frame 更新一次 UI state

### Step 2｜把狀態互斥規則寫死（避免長期爆炸）
- 明確定義 priority（建議）：
  1. AE/AF Lock（是否只影響對焦/曝光，不必凍結 tracking UI？需產品決策）
  2. trackedPoint（tap-to-track）
  3. trackedObjectId（ML Kit）
- 或相反（若產品以 ML Kit 為主）：ML Kit 優先，tap-to-track 只在「沒物件」時生效。此條需在 ViewModel 內統一。

---

## 5. 需要檢視的檔案/關鍵函式清單（請用待辦清單列出）
> 下面清單以「先搞懂現況」為目的，Claude Code 請逐項打開確認。

- [ ] `presentation/camera/CameraScreen.kt`
  - [ ] tap 手勢處理位置：目前點擊如何取得 (x,y)？座標系是 Compose 還是 PreviewView？
  - [ ] FocusIndicator 的座標輸入來源：`state.focusPoint` 是否已是 Canvas 座標？
  - [ ] PreviewView 與 Compose overlay 的對齊方式（Box layering / matchParentSize / clip / padding）
- [ ] `presentation/camera/CameraViewModel.kt`
  - [ ] 點擊對焦事件處理：`onTap`/`handleTapToFocus`（實際函式名以 repo 為準）
  - [ ] tracked object 更新入口：`updateTrackedObjectPosition` 或類似函式（從 analyzer/ML Kit 回推 UI 的點）
  - [ ] focusIndicator fade-out 的時序邏輯：目前只在 `trackedObjectId == null` 觸發；新增 point tracking 後要改成「trackedObjectId == null && trackedPoint == null」
  - [ ] AE/AF lock 狀態切換：長按事件處理、鎖定時是否覆蓋 focusPoint
- [ ] `presentation/camera/CameraState.kt`
  - [ ] 新增欄位規劃：`trackedPoint: Pair<Float,Float>?`、`trackedPointStatus/quality`、`trackingMode enum`（是否需要）
- [ ] CameraX 設定檔 / 相機綁定處（通常在 ViewModel 或 camera controller 類）
  - [ ] `ImageAnalysis.Builder()`：resolution/target aspect/backpressure strategy
  - [ ] Analyzer 設定：executor/dispatcher、是否已做 throttle
- [ ] ML Kit analyzer（如果存在獨立類）
  - [ ] 目前是否已在 analyzer 內做 heavy work（object detection / face detection）
  - [ ] 是否可插入另一個 tracking branch，或需要分離 pipeline
- [ ] 座標轉換工具（若有）
  - [ ] 類似 `CoordinateMapper` / `mapImageToPreview` / `mapPreviewToImage`
  - [ ] 是否有處理 `rotationDegrees` 與 front camera mirror

---

## 6. 驗證清單（Build/Test/Device/Perf）
### Build / Unit（最低限度）
- [ ] App 可正常編譯（Debug/Release 至少一個）
- [ ] 不新增 OpenCV 時，先確保不引入 NDK/ABI 相關風險

### 功能（實機必做）
- [ ] 後鏡頭：點擊桌上任意小物角點 → 平移手機 → indicator 跟隨（同一視覺點）
- [ ] 目標出畫：追蹤點移出預覽邊界 → tracking 結束（indicator 行為符合 spec：tracking 模式常駐；結束後回落到靜態/或消失）
- [ ] 遮擋/模糊：遮住目標或快速移動 → 不應跳到背景；應進入 lost 並結束
- [ ] 與既有 ML Kit 追蹤互動：
  - [ ] ML Kit 正在追蹤物件時 tap 任意點：優先級行為符合定義
- [ ] 與 AE/AF lock：
  - [ ] lock 開啟後 tap-to-track 是否允許？若允許，UI 是否衝突（顏色/alpha/位置）

### 效能（實機）
- [ ] log 每幀 analyzer 耗時（移動平均）：目標 < 10~15ms（視解析度）
- [ ] 預覽主觀 FPS 不明顯下降（至少 3 分鐘）
- [ ] CPU/溫升觀察：中階機不應快速過熱
- [ ] backpressure：確認 analyzer 沒有 queue 爆掉（可加 dropped frames 計數）

---

## 7. 回滾與降風險策略（Rollback/Feature flag）
1. **Feature flag**
   - `enablePointTracking`（預設 false 或 debug-only）
   - 可由 BuildConfig/remote config/設定頁控制
2. **降級路徑**
   - 追蹤計算超時或分數過低 → 自動停止 point tracking（回到靜態 focus indicator）
   - 降頻：每 2~3 frame 更新一次 tracking
   - 降解析度：tracking 僅用 downscale 灰階
3. **隔離變更**
   - 追蹤核心集中在獨立類（`PointTrackingManager/Tracker`），ViewModel 只收 result 更新 state
   - 關閉 flag 時完全不改動既有 ML Kit tracking 與 focus fade 行為
4. **上線後緊急處置**
   - 若遇到特定機型掉幀：遠端關閉 flag 或針對機型黑名單停用
   - 保留日誌開關：可在 debug 打印 tracking 耗時與 lost 原因（出界/分數低/rotation mismatch）

---
