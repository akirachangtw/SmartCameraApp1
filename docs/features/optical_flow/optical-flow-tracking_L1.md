<!-- generated: 2026-02-02 14:23:54 -->

# Level 1 規劃摘要：optical-flow-tracking

## 1. 目標（Goal）
在 CameraX 即時預覽中新增「任意點點擊追蹤（tap-to-track point）」能力：  
使用者點擊預覽畫面任意位置後，即使移動手機，只要該畫面內容仍在畫面內，畫面上的圓圈/指示器應持續跟隨同一個視覺特徵點（例如 Sony 耳機盒上的某個角點 A），而非僅能追蹤 ML Kit 偵測到的物件（人臉/常見物件）。

---

## 2. 背景與問題（Why / What）
### Why（為什麼要做）
- 現代手機相機常見的基礎能力：使用者點選畫面後，UI 指示器會「黏著」在同一個畫面內容上（視覺追蹤），提升對焦/構圖/提示的可用性與專業感。
- 目前系統的追蹤依賴 ML Kit 的物件/人臉偵測框：  
  - 無法涵蓋「非 ML Kit 可辨識的任意物體」或「物體上的特定局部點」（如耳機盒上的 logo、邊角、某個按鍵位置）。
  - 因此點擊某處後，若沒有對應的 ML Kit 物件 ID，就只能顯示靜態 focus indicator，無法跟隨內容移動。

### What（現在遇到的問題）
- 現況：系統只支援「偵測到的物件追蹤」，不支援「任意點（feature point）追蹤」。
- 需求行為：點擊任意位置 → 系統在後續 frame 透過光流/特徵追蹤更新該點的新座標 → UI 圓圈平滑跟隨。
- **對焦指示器時序規格**：
  - **靜態對焦模式**（點擊空白區域，無追蹤）：
    1. 對焦成功後保持顯示 **2 秒**
    2. 然後用 **500ms 淡出動畫**降至 **Alpha 0.7**
    3. 淡出期間保持可見但不干擾拍照
  - **追蹤模式**（點擊物件/位置，啟動追蹤）：
    1. 圓圈持續顯示，Alpha 保持 1.0
    2. 追蹤期間圓圈跟隨目標移動
    3. **結束條件**：用戶再次點擊、或追蹤目標超出預覽畫面
- 補充觀察：目前 repo 已在 UI/狀態上做了指示器動畫與淡出等優化（`focusIndicatorFading`、追蹤用 spring 動畫），但追蹤來源仍是「ML Kit object」而非 optical flow。

---

## 3. 影響範圍（Scope：模組/檔案/功能層級；若未知請以推測標註）
> 以最小差異為前提，以下為可能影響範圍（含推測）。

### 功能層級
- 新增追蹤模式：`Tap-to-Track (Point)`（任意點光流追蹤）
- 點擊手勢行為調整：點擊不僅觸發對焦，也可啟動/重設 point tracking
- 指示器顯示邏輯：追蹤期間常駐顯示、失效時回落/消失

### 模組/檔案（推測 + 已知）
- **presentation/camera/**
  - `CameraScreen.kt`：接收 tap 事件、顯示追蹤點 indicator、座標 mapping（PreviewView/Compose overlay 對齊）
  - `CameraState.kt`：新增「point tracking」狀態（例如 trackingPoint、trackingQuality、trackingLost 等）
  - `CameraViewModel.kt`：管理 tracking lifecycle、接收 analyzer 輸出、更新 state
- **analysis/CameraX ImageAnalysis pipeline（推測）**
  - 既有 Analyzer（可能目前用於 ML Kit 偵測/追蹤）需擴充：輸入連續 frame，輸出 optical-flow 更新後座標
  - 若目前是以 ML Kit 結果更新 trackedObjectPosition，需新增一條「point tracking」更新分支
- **座標轉換/Mapping（推測，高風險區）**
  - ImageAnalysis buffer 座標 ↔ PreviewView 顯示座標 ↔ Compose Canvas 座標（含旋轉、鏡像、crop/scaleType）

---

## 4. 風險清單（Risks：含相機管線/模式切換/效能回歸）
1. **相機管線與延遲風險（CameraX pipeline）**
   - 光流需要連續 frame 計算；若分析執行緒阻塞，可能導致分析積壓、掉幀或預覽卡頓。
2. **效能回歸（CPU/GPU/Memory）**
   - Optical flow（即使是 LK sparse）在高解析度/高 FPS 下可能吃 CPU；若未降採樣或限頻，會造成發熱與耗電上升。
3. **模式切換複雜度**
   - 目前有：靜態對焦指示器、AE/AF Lock、ML Kit 物件追蹤（trackedObjectId != null）等狀態。
   - 新增 point tracking 容易出現狀態互斥不清：  
     例：長按鎖定時是否允許 point tracking？ML Kit tracking 與 point tracking 優先級？
4. **追蹤失效與漂移（drift / lost track）**
   - 被遮擋、模糊、低光、快速移動會導致追蹤點漂移或跳點；若 UI 不處理「信心度/失效」，會顯得更不可信。
5. **座標 mapping 錯誤**
   - 不同 device rotation、前鏡頭鏡像、PreviewView scaleType（FIT_CENTER/FILL_CENTER）會造成「指示器與實際內容不對齊」。
6. **與既有 UI 動畫衝突**
   - 目前 indicator 有 spring 平滑與 fade-out；追蹤點若更新頻繁，動畫參數不當會造成 lag/追不上或抖動。

---

## 5. 建議方案（Plan A / Plan B：最小差異優先）
### Plan A（最小差異、優先建議）：Sparse Optical Flow / 特徵點追蹤（單點或小區塊）
**核心概念**
- 點擊時在 ImageAnalysis 座標系選定一個點（或以點為中心的 ROI 小區塊）。
- 每個 frame 以 sparse LK optical flow（或等價的輕量追蹤）估計該點在下一幀的位置。
- 追蹤輸出：新座標 + quality（是否有效、誤差/狀態）。
- UI：用既有 FocusIndicator（目前已支援 tracking 動畫）顯示該點，並將 `isTracking` 延伸為「point tracking 也算 tracking」。

**最小化改動策略**
- 優先「沿用現有 state/UI 結構」：
  - 新增 `trackedPoint`（Float x,y）與 `trackedPointActive` / `trackedPointLost`（命名可再定）
  - 將 `trackedObjectId != null` 的 tracking 判斷，擴充為「trackedObject 或 trackedPoint 任一存在」
- 追蹤計算放在 ImageAnalysis 的 analyzer thread（或 dedicated dispatcher），並做：
  - 降採樣（例如在 320p~480p 灰階上追蹤）
  - 限頻（例如 15fps 做追蹤、或每 N 帧更新一次 UI）
  - 失效判定（追蹤品質差則停用，回到靜態 indicator 或直接消失）

**依賴/技術（推測）**
- OpenCV（Android SDK）是常見選擇；但會帶來 APK 體積與 NDK/ABI 複雜度（此為後續 Level 2 需評估）。
- 若不引入 OpenCV，可評估：
  - RenderScript 已淘汰，不建議
  - 自行實作 NCC / template matching（較簡單但準確度/速度不一定）
  - MediaPipe/其他 library（整合成本更高）

### Plan B（備援、低整合風險）：小 ROI Template Matching（區塊匹配）追蹤
**核心概念**
- 點擊時截取一個小 patch（例如 32x32 或 48x48 灰階）作為 template。
- 後續 frame 在鄰近搜尋窗內做 matching（SSD/NCC），找最佳位置作為新座標。
- 優點：不一定要引入完整 OpenCV optical flow；概念直覺、可控。
- 缺點：遇到旋轉/尺度變化/光照變化時容易失效；高速移動或模糊也較不穩。

**適用情境**
- 若 Plan A 因依賴或效能不可行，Plan B 可作為 MVP，先交付「可用但有限制」的 tap-to-track。

---

## 6. 驗證方式（How to verify：模擬器/實機/效能指標）
### 功能驗證（必做，實機優先）
1. **任意物體點追蹤**
   - 場景：Sony 耳機盒（或任何非 ML Kit 可辨識物）放桌上
   - 操作：點擊耳機盒上的角點 A → 手持手機左右平移/輕微前後移動
   - 期望：圓圈持續跟隨角點 A（允許少量誤差），耳機盒仍在畫面內時不應飄到背景
2. **失效處理**
   - 遮擋目標點、快速甩動、移出畫面
   - 期望：追蹤狀態能「合理結束」（例如顯示 lost 狀態或自動停止），不要亂跳到其他物體
3. **與 AE/AF Lock / 既有 ML Kit 追蹤互動**
   - 長按鎖定後再點擊追蹤點、或追蹤中切換鎖定
   - 期望：規則一致（需在需求中定義優先級），不出現 UI 狀態互相覆蓋/卡死

### 效能驗證（實機）
- 指標建議：
  - 預覽 FPS 是否明顯下降（主觀 + log）
  - analyzer 平均耗時（ms/frame），是否造成 backpressure
  - CPU 使用率/溫升（至少觀察 3–5 分鐘）
- 裝置建議：
  - 目標主力機型（Pixel / Samsung / 中階機至少各一）
  - 前鏡頭/後鏡頭、不同解析度各測一次（若產品會切換）

### 模擬器（可做但不作為主要依據）
- 模擬器通常不具代表性的相機管線效能與輸入，僅用於基本 UI 顯示/狀態切換不 crash。

---

## 7. 回滾策略（Rollback）
- 以 feature flag 或設定開關包住 point tracking：
  - 預設關閉或僅 debug 開啟（依產品策略）
- 回滾路徑：
  - 保留既有「ML Kit 物件追蹤」與「靜態點擊對焦指示器」流程不變
  - point tracking 相關程式集中在 analyzer/單一 manager，移除或關閉不影響主流程
- 若上線後發現效能回歸：
  - 立刻關閉 point tracking（remote config/本地 flag）
  - 或降頻/降採樣參數作為緊急降級

---

## 8. 是否需要 Level 2 可行性檢視（Yes/No + 理由）
**Yes，需要 Level 2 可行性檢視。**

理由：
- 明確涉及 **CameraX / Preview / ImageAnalysis** 連續影像處理與座標 mapping（屬高風險區）。
- 存在 **效能、FPS、延遲** 回歸風險，且可能需要新增第三方依賴（如 OpenCV）或改動 analyzer 執行模型。
- 需求牽涉 **模式切換/狀態互斥**（ML Kit tracking、AE/AF lock、focus indicator fade 等），需檢視 repo 現況才能定義最小改動切入點與可回滾設計。
