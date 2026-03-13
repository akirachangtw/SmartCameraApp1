# SmartCameraApp1 — 對焦系統 L2 可行性規格

版本：v1.0  
日期：2026-03-11  
文件類型：Level 2 可行性檢視 / 技術設計規格  
適用範圍：CameraX / PreviewView / ImageAnalysis / ML Kit Face Detection / Tap Focus / Optical Flow / Compose Overlay

---

## 1. 文件目的

本文件針對目前「開發對焦功能規範」所遇到的四項技術卡點，進行 Level 2 可行性檢視，並整理成可交接、可實作、可驗證的技術規格。

本次目標不是直接進入所有功能實作，而是先確認以下事項：

1. 目前問題是否可在既有 Kotlin + Jetpack Compose + CameraX + ML Kit 架構下解決。
2. 是否需要拆分狀態機、幾何轉換與 UI overlay。
3. 哪些需求可在 MVP 階段完成，哪些需要進一步演算法或硬體能力支援。
4. 如何定義最小可行 diff、驗證方式與回滾策略。

---

## 2. L2 結論

**可行，但需調整架構。**

本議題並非單一 bug，而是以下三個子系統耦合所造成：

1. **幾何轉換耦合錯誤**：ImageProxy / ML Kit BoundingBox 座標未穩定映射到 PreviewView。
2. **狀態機語義混淆**：視覺鎖定、光學對焦成功、AE/AF Lock 被混成單一狀態。
3. **追蹤資料模型不足**：只取單一人臉、未使用 TrackingId、未建立 stable target identity。

因此，本次 L2 建議不是繼續補 if/else，而是先導入以下四層式架構：

- Detection Layer
- Geometry Layer
- Focus Control Layer
- Overlay / UI Layer

若依照本文件建議拆分，四個問題皆可在同一版架構重整中一起改善。

---

## 3. 問題清單與 L2 判斷

### 3.1 問題一：【座標轉換遺失】

**現象**
- 無論前後鏡頭，Eye-AF 與點擊對焦框皆為固定大小。
- ML Kit `BoundingBox` 無法正確映射到 Preview 螢幕座標。
- Overlay 在 Debug / Release 可能出現幾何不一致。

**L2 判斷**
- 可行。
- 但必須建立統一的 `GeometryMapper`，禁止 UI 直接吃 image-space rect。

**根因**
- ML Kit 輸出通常是 image/buffer 座標系。
- PreviewView 顯示涉及 rotation、crop、fit center、mirror 等多步轉換。
- 前鏡頭還額外涉及鏡像問題。

**技術結論**
- Overlay 必須只吃 `view-space rect`。
- Release / Debug 都必須走 `PreviewView` 幾何路徑。
- 不可在 Composable 裡面再補算比例與偏移。

---

### 3.2 問題二：【狀態機死鎖】

**現象**
- Eye-AF 無法進入「對焦成功」。
- 前鏡頭 fixed-focus 永遠不會回傳 AF success。
- 後鏡頭 continuous AF 下，狀態機錯誤等待單次對焦回調。

**L2 判斷**
- 可行。
- 但必須將「視覺鎖定」與「鏡頭光學 AF 成功」拆成兩組 state。

**根因**
- fixed-focus 鏡頭本質上不提供一般意義的單次 AF 成功流程。
- continuous picture 模式是被動連續調焦，不等於 one-shot AF callback。
- UI 色彩規格已明定 Cyan 與 Yellow 的語義不能混用。

**技術結論**
- Eye-AF 成功定義需改為：`TrackingStable + VisualLocked`。
- AE/AF Lock 仍維持 Yellow，且只由長按 / Lock Icon 觸發。
- 光學 AF 成功只作為次級資訊，不可作為 Eye-AF 進入成功狀態的唯一 gate。

---

### 3.3 問題三：【陣列處理偷懶】

**現象**
- 後鏡頭 Eye-AF 僅使用 `firstOrNull()`。
- 多人臉無法穩定追蹤。
- 未將 `TrackingId` 傳給光流模組。

**L2 判斷**
- 可行。
- 需改為完整 target list + priority ranking + stable identity。

**根因**
- 單一目標簡化邏輯導致多人臉、主體切換、追蹤重綁皆失效。
- 光流沒有 identity，只有框。

**技術結論**
- 建立 `FocusTarget` 資料模型。
- Face / Object / Tap subject 都要變成列表輸出。
- 追蹤與光流改綁 `stableId`，而不是暫時的 rect。

---

### 3.4 問題四：【點擊對焦邊界】

**現象**
- 後鏡頭點擊對焦可以成功。
- 但焦點框無法包覆主體邊界。

**L2 判斷**
- 部分可行。
- 單純依靠 `startFocusAndMetering()` 無法得到物件輪廓。
- 若要包覆物件，必須補一條視覺主體定位路徑。

**根因**
- AF metering point 只是一個點，不是 object box 或 segmentation result。

**技術結論**
- 點擊對焦必須拆為：
  - Tap Focus（硬體 AF/AE/AWB）
  - Tap Subject Acquisition（視覺主體定位）
- MVP 可先採 ROI + 光流估框。
- 完整版可升級為 object detector / segmentation。

---

## 4. 與 SSOT 對齊原則

本文件必須遵守既有對焦 SSOT：

1. **Cyan**：Eye-AF / 點擊對焦 / 物件對焦（對焦中、成功、追蹤中）。
2. **Yellow**：僅 AE/AF Lock（長按或點擊 lock icon）。
3. **Eye-AF / 物件框**：動態比例，需對應 ML Kit BoundingBox。
4. **環境 / 紋理對焦**：固定大小 72dp x 72dp。
5. **前鏡頭**：不支援 Yellow，僅 Cyan 視覺追蹤。
6. **Release / Debug**：必須一致使用 PreviewView 幾何路徑。

---

## 5. 建議技術架構

```text
CameraX Preview + ImageAnalysis
        │
        ├─ Detection Layer
        │    ├─ FaceDetectorAdapter
        │    ├─ ObjectDetectorAdapter
        │    └─ TapSubjectAcquirer
        │
        ├─ Geometry Layer
        │    ├─ Sensor/Image → View Matrix
        │    └─ Rect / Point mapping
        │
        ├─ Target Tracker Layer
        │    ├─ Target ranking
        │    ├─ Stable identity binding
        │    └─ Optical flow handoff / relink
        │
        ├─ Focus Control Layer
        │    ├─ Lens capability resolver
        │    ├─ Passive AF / Active AF / Fixed focus routing
        │    └─ AE/AF Lock routing
        │
        └─ Overlay Layer
             ├─ Cyan focus ring / brackets
             ├─ Yellow lock state
             └─ Alpha / dwell / fade animation
```

### 5.1 Detection Layer

輸出統一資料：
- `faces: List<TrackedFace>`
- `objects: List<TrackedObject>`
- `tapSubject: TapSubject?`

### 5.2 Geometry Layer

專責將 image-space / sensor-space 座標，統一映射為 PreviewView 可直接繪製的 `RectF` / `PointF`。

### 5.3 Target Tracker Layer

負責：
- 多目標排序
- stableId 維持
- 短暫遺失重綁
- optical flow 接手與回補

### 5.4 Focus Control Layer

根據鏡頭能力決定：
- fixed focus：僅視覺鎖定
- continuous AF：被動持續調焦 + 必要時單次 trigger
- tap focus：硬體對焦 + 視覺主體取得
- AE/AF Lock：單獨分支

### 5.5 Overlay Layer

UI 只接受：
- 已轉好的 `viewRect`
- 顏色 state
- alpha / dwell / animation state

禁止 UI 再自行做：
- 座標比例換算
- 鏡像判斷
- 鎖定狀態 OR 組合判斷

---

## 6. 資料模型建議

### 6.1 FocusTarget

```kotlin
data class FocusTarget(
    val stableId: String,           // face:123 / object:7 / tap:manual
    val type: TargetType,
    val rectImage: RectF,
    val rectView: RectF,
    val confidence: Float,
    val priorityScore: Float,
    val trackingId: Int?,
    val velocity: PointF,
    val lastSeenMs: Long,
    val source: TargetSource,
)

enum class TargetType {
    FACE,
    OBJECT,
    TAP_SUBJECT,
    TAP_TEXTURE
}

enum class TargetSource {
    ML_KIT_FACE,
    ML_KIT_OBJECT,
    OPTICAL_FLOW,
    USER_TAP
}
```

### 6.2 CameraState 建議拆分

```kotlin
sealed interface VisualTargetState {
    data object None : VisualTargetState
    data object Candidate : VisualTargetState
    data object TrackingStable : VisualTargetState
    data object VisualLocked : VisualTargetState
    data object TrackingFade : VisualTargetState
    data object Lost : VisualTargetState
}

sealed interface LensFocusState {
    data object NotRequested : LensFocusState
    data object PassiveContinuous : LensFocusState
    data object ActiveScan : LensFocusState
    data object Focused : LensFocusState
    data object NotFocused : LensFocusState
    data object UnsupportedFixedFocus : LensFocusState
}

sealed interface ExposureLockState {
    data object Unlocked : ExposureLockState
    data object AEAFLocked : ExposureLockState
}
```

---

## 7. 狀態機設計

```text
Idle
  -> Detecting
      -> TargetCandidate
          -> TrackingStable
              -> VisualLockedCyan
                  -> TrackingFade
                  -> TrackingLost
              -> FocusRequesting   (only if lens supports AF and mode allows)
                  -> OpticalFocused
                  -> OpticalNotFocused
                  -> UnsupportedFixedFocus
          -> AEAFLockYellow       (long press / lock icon only)
```

### 7.1 狀態機原則

1. `VisualLockedCyan` 與 `OpticalFocused` 必須分離。
2. `AEAFLockYellow` 必須與 Eye-AF 成功分離。
3. 前鏡頭 fixed-focus 不可等待 AF success callback。
4. 後鏡頭 continuous AF 不可把 one-shot callback 當唯一成功判斷。

### 7.2 視覺成功條件

建議定義 Eye-AF 視覺鎖定成功分數：

\[
S = w_c C + w_s S_t + w_v V_t
\]

其中：
- \(C\)：偵測信心
- \(S_t\)：連續幀穩定度
- \(V_t\)：速度穩定度

建議預設：
- \(w_c = 0.4\)
- \(w_s = 0.4\)
- \(w_v = 0.2\)
- 當 \(S \ge 0.75\) 時進入 `VisualLocked`

---

## 8. Geometry / 座標轉換公式

### 8.1 核心公式

使用齊次座標表示：

\[
\begin{bmatrix}
x_v \\
y_v \\
1
\end{bmatrix}
=
\mathbf{M}_{view \leftarrow image}
\begin{bmatrix}
x_i \\
y_i \\
1
\end{bmatrix}
\]

若可取得：
- `sensorToView`
- `sensorToBuffer`

則：

\[
\mathbf{M}_{view \leftarrow image}
=
\mathbf{M}_{view \leftarrow sensor}
\cdot
(\mathbf{M}_{image \leftarrow sensor})^{-1}
\]

### 8.2 Bounding Box 四角轉換

給定 image-space rect：
- \((l,t)\)
- \((r,t)\)
- \((r,b)\)
- \((l,b)\)

全部映射後取外包矩形：

\[
l' = \min(x'_k), \quad
r' = \max(x'_k), \quad
t' = \min(y'_k), \quad
b' = \max(y'_k)
\]

### 8.3 動態框尺寸

\[
w = r' - l', \qquad h = b' - t'
\]

加入外擴係數：

\[
w_{draw} = (1+\alpha)w, \qquad
h_{draw} = (1+\beta)h
\]

建議預設：
- \(\alpha = 0.08\)
- \(\beta = 0.08\)

### 8.4 前鏡頭鏡像

若底層 transform 未處理鏡像，可補：

\[
x_{mirror} = W_{view} - x
\]

但若已使用 CameraX / PreviewView 正確 transform，禁止二次鏡像。

---

## 9. 多人臉 / 多目標設計

### 9.1 不可再使用 `firstOrNull()`

人臉 / 物件偵測結果需完整保留為列表，並計算優先分數：

\[
P_i = w_a A_i + w_c C_i + w_t T_i + w_d D_i + w_u U_i
\]

其中：
- \(A_i\)：面積占比
- \(C_i\)：模型信心
- \(T_i\)：追蹤穩定時長
- \(D_i\)：距離點擊點接近度
- \(U_i\)：畫面中心或構圖加權

### 9.2 stableId 設計

建議規格：
- 臉：`face:{trackingId}`
- 物件：`object:{trackingId}`
- 手動點擊：`tap:manual:{sessionId}`

### 9.3 光流重綁公式

若新偵測框與舊追蹤框重疊度高，則視為同一目標：

\[
IoU(A,B)=\frac{|A\cap B|}{|A\cup B|}
\]

建議門檻：
- \(IoU \ge 0.5\)：直接視為同一目標
- \(0.3 \le IoU < 0.5\)：候選重綁
- \(< 0.3\)：視為新目標

---

## 10. 點擊對焦雙路徑設計

### 10.1 路徑 A：Tap Focus（硬體）

使用：
- `PreviewView.meteringPointFactory.createPoint(x, y)`
- `startFocusAndMetering()`

功能：
- 控制 AF / AE / AWB
- 不提供主體輪廓

### 10.2 路徑 B：Tap Subject Acquisition（視覺）

建立初始 ROI：

\[
R_0 = [x-r,\ y-r,\ x+r,\ y+r]
\]

接著可採：
- MVP：ROI + optical flow 估框
- 進階：object detector / segmentation / saliency

### 10.3 由特徵點估框

若有光流特徵點集合 \(\{(x_k,y_k)\}\)，則：

\[
l = \min_k x_k, \quad r = \max_k x_k, \quad
t = \min_k y_k, \quad b = \max_k y_k
\]

平滑後輸出：

\[
R_t = \lambda R_{obs} + (1-\lambda)R_{t-1}
\]

建議：
- \(\lambda = 0.25 \sim 0.4\)

---

## 11. 硬體與物理限制

### 11.1 前鏡頭 fixed-focus

若鏡頭為 fixed-focus：
- 不存在一般意義的鏡頭掃焦成功流程
- 不應等待 AF success callback
- 正確行為是「視覺鎖定成功」而非「光學對焦成功」

### 11.2 continuous AF

`CONTINUOUS_PICTURE` 是被動持續調焦：
- 不等同 one-shot AF
- 不可用單次回調作為唯一成功條件

### 11.3 Tap AF 的物理限制

Metering point 是點，不是物件框：
- 可拿來驅動 AF/AE/AWB
- 無法直接得到物件輪廓或 segmentation 邊界

---

## 12. MVP 與完整版本建議

### 12.1 MVP 必做

1. GeometryMapper 統一座標轉換
2. 前鏡頭改為 tracking-only success
3. 後鏡頭 AF state 分離
4. target list + stableId 導入
5. tap focus / tap subject 雙路徑拆分
6. overlay 改只吃 viewRect

### 12.2 Phase 2 可升級

1. 自拍模式導入 face mesh
2. object segmentation / saliency
3. 卡爾曼濾波 / IMM 平滑追蹤
4. 更細緻的多目標切換策略

---

## 13. 風險評估

### 13.1 主要風險

1. **機型差異風險**
   - 前鏡頭 fixed-focus / EIS / crop 行為因裝置差異而不同。

2. **Preview 與 Analysis 幾何不同步風險**
   - 若錯誤取得 rotation / crop 資訊，框仍可能偏移。

3. **多人臉切換抖動**
   - 若 priority score 設計不穩，可能產生焦點跳動。

4. **光流漂移**
   - 紋理不足或快速移動時，框可能漂移。

### 13.2 緩解方式

- 將 GeometryMapper 做成單元測試可驗證模組。
- 設定 dwell time 與 target switch hysteresis。
- IoU + tracking time 雙重門檻防止錯綁。
- 保留 feature flag 以便回退。

---

## 14. 驗證清單

### 14.1 幾何驗證

- 前鏡頭單臉：框是否準確包住臉/眼區域
- 後鏡頭單臉：框是否動態縮放
- 前鏡頭鏡像：左右是否反向錯位
- 裝置旋轉：90 / 180 / 270 度是否仍對齊

### 14.2 狀態機驗證

- 前鏡頭 Eye-AF 是否可進入 Cyan locked
- 前鏡頭是否不再卡死等待 AF success
- 後鏡頭 continuous AF 是否不再死等 callback
- 長按 lock 是否正確顯示 Yellow

### 14.3 多目標驗證

- 兩人以上畫面能否穩定選擇主目標
- 主目標短暫遮擋後能否重綁
- `TrackingId` 與光流是否一致

### 14.4 點擊對焦驗證

- 點擊後硬體 AF 是否成功
- 點擊後視覺框是否可估出主體範圍
- 物體移動時框是否可由光流接續

---

## 15. 實作拆分建議

### Sprint 1：Geometry + State 拆分
- 建立 GeometryMapper
- 拆分 VisualTargetState / LensFocusState / ExposureLockState
- Overlay 改只吃 view-space data

### Sprint 2：Target Tracker 導入
- 將 face/object/tap 統一為 FocusTarget list
- 導入 stableId
- 導入 priority ranking

### Sprint 3：Tap Subject 補齊
- 導入 TapSubjectAcquirer
- 光流接手與估框
- 補測多人 / 遮擋 / 快速位移

---

## 16. 回滾策略

若新架構導入後出現機型兼容性問題，建議保留下列 feature flag：

- `focus.geometry_mapper_v2`
- `focus.visual_state_split`
- `focus.multi_target_tracker`
- `focus.tap_subject_acquisition`

回滾順序建議：
1. 關閉 tap subject acquisition
2. 關閉 multi target tracker
3. 保留 state split 與 geometry mapper

GeometryMapper 與 state split 不建議整體回退，因為屬於基礎修正。

---

## 17. Eye-AF 與硬體限制設計原則

本節目的在於釐清：Eye-AF 不等於「一定能用鏡頭硬體精準鎖在眼睛上」，而是「先以眼睛作為最高優先主體，再依鏡頭能力決定能做到多少真實光學對焦行為」。設計上必須在視覺追蹤、相機控制與硬體極限之間找到平衡點，避免做出理論正確但產品體驗不穩定的流程。

### 17.1 核心判斷

Eye-AF 的技術實現可拆成三層：

1. **視覺層**：是否能穩定找出臉、眼睛、主眼、雙眼中心。
2. **幾何層**：是否能把眼睛位置正確投影到 PreviewView 與 metering region。
3. **光學層**：鏡頭是否真的支援有效 AF 掃描、是否允許持續調焦、是否能反映為可觀察的成像差異。

因此，Eye-AF 成功不可只看「有沒有偵測到眼睛」，也不可只看「有沒有 AF callback」，而是必須定義為一個分級成功模型。

### 17.2 分級成功模型

建議將 Eye-AF 成功拆為三級：

#### A. Visual Success（視覺成功）
條件：
- 已偵測到臉或眼睛
- 追蹤穩定達門檻
- 焦點框可穩定附著在目標上

此層成功代表：
- 可顯示 Cyan locked
- 可視為 Eye-AF 已進入可用狀態
- 不要求硬體 AF 一定成功

#### B. Metering Success（測光 / 對焦區域成功）
條件：
- 已將眼睛或雙眼中心映射為 metering point / region
- 相機控制層已接受 focus / exposure request

此層成功代表：
- 系統已把相機注意力導向眼睛附近
- 但不保證鏡頭一定改變焦距或回傳明確成功事件

#### C. Optical Success（光學成功）
條件：
- 鏡頭支援可變焦
- 當前模式允許 AF 行為
- 對焦結果或被動持續 AF 的畫面品質確有改善

此層成功代表：
- 真正意義上的「鏡頭朝眼睛優先對焦」
- 僅能在部分後鏡頭情境成立

產品與 UI 上，建議 **以 A 為主、B 為輔、C 為加分項**。這是目前最實際的平衡點。

### 17.3 現實瓶頸解析

#### 瓶頸一：前鏡頭 fixed-focus

許多前鏡頭本質上是 fixed-focus 或近似固定焦距設計，即使能偵測到眼睛，也不代表鏡頭可主動掃焦。

**現實限制**
- 沒有真正可操作的鏡頭移動範圍
- 不一定會回傳可用 AF success
- 即使送出 focus request，也可能只是形式上請求，畫質幾乎不變

**可利用的破口**
- 把前鏡頭 Eye-AF 重新定義為「視覺鎖定成功」
- 強化曝光、白平衡、臉部優先處理與 UI 回饋
- 用穩定的 eye box / face box 讓使用者感知「系統知道你是主體」

**平衡點**
- 前鏡頭不追求真 AF success
- 改追求「穩定鎖人、穩定框眼、穩定曝光」

#### 瓶頸二：後鏡頭 continuous AF 並非 one-shot AF

後鏡頭常用 continuous picture 模式，特性是持續微調，而不是每次都產生明確單次成功回報。

**現實限制**
- callback 時機不一定可作為狀態機依據
- 若把 UI 成功綁死在單次 callback，會造成假性失敗
- 主體持續移動時，continuous AF 可能一直處於被動調整

**可利用的破口**
- 將 `VisualLocked` 與 `OpticalFocused` 拆開
- 只在 target 切換、tap focus、景深變化較大時才主動送 focus trigger
- 平時讓 continuous AF 被動運作，UI 以視覺穩定為主

**平衡點**
- 後鏡頭 Eye-AF 成功 = 已穩定追眼 + metering 已導向眼區
- 光學成功則作為次級訊號，不作為主狀態唯一條件

#### 瓶頸三：景深與手機鏡頭物理極限

即使鏡頭支援 AF，手機鏡頭通常感光元件較小、實體焦距短、光圈固定，導致景深較深。

**現實限制**
- 即使對焦位置有改變，肉眼也不一定明顯看出「只有眼睛最清楚」
- 尤其前鏡頭自拍距離短、等效廣角常見，整張臉往往都落在可接受清晰範圍

**可利用的破口**
- 不把產品宣稱建立在「明顯光學淺景深鎖眼」
- 轉為主打「主體優先」「穩定鎖臉」「拍攝意圖明確」
- 可搭配人像模式、後製銳化或局部增強，提升使用者感知

**平衡點**
- 在物理鏡頭無法呈現大幅景深差異時，產品重點應放在構圖、曝光、追蹤穩定性，而不是肉眼可見的戲劇性鎖眼效果

#### 瓶頸四：眼睛偵測本身並不總是可靠

當眼睛太小、側臉、遮擋、低光、逆光、快速移動時，眼部 landmark 可能消失或抖動。

**現實限制**
- 只要直接綁死單眼點位，框會跳動
- 若只抓單一幀結果，容易造成 focus region 抖動

**可利用的破口**
- 以雙眼中心或臉框上半部作為較穩定的 metering 基準
- 當眼睛點位不可靠時，自動退化到 face bounding box
- 再不行則退化到 head/upper-face region

**平衡點**
- Eye-AF 不應是「只認眼睛，眼睛消失就失敗」
- 應設計成「眼睛優先、臉部退化、主體延續」的層級式降級機制

### 17.4 建議降級策略

建議依據觀測品質，採用以下優先順序：

```text
雙眼穩定 -> 單眼穩定 -> 臉框上半部 -> 臉框中心 -> 最近一次穩定光流位置 -> Lost
```

可用分數表示眼部可靠度：

R_eye = w_l * L + w_r * R + w_s * S + w_o * O

其中：
- L：左眼可見度 / 品質
- R：右眼可見度 / 品質
- S：多幀穩定度
- O：遮擋懲罰後的有效性

建議：
- 當 R_eye >= 0.75：使用雙眼中心
- 當 0.5 <= R_eye < 0.75：使用單眼或臉框上半部
- 當 R_eye < 0.5：退化為 face-center / tracked face region

這樣可大幅降低低光、遮擋、側臉時的 UI 跳動。

### 17.5 眼睛 metering 點設計原則

若偵測到左右眼點位 p_l, p_r，建議先求雙眼中心：

c_eye = (p_l + p_r) / 2

但實際 metering 區域不應只是一個極小點，而應建立小型穩定區域：

R_meter = [c_x - k*d, c_y - k*d, c_x + k*d, c_y + k*d]

其中：
- d = ||p_l - p_r|| 為兩眼距離
- k 建議取 0.6 ~ 1.2 之間

這樣做的好處：
- 對小抖動更穩定
- 不會因單點噪聲讓 AF region 跳動
- 更符合相機硬體實際使用的是「區域」而不是數學單點

若無眼睛點位但有 face box，則可取臉框上半部中心：

c_upperFace = ((l + r) / 2, t + 0.35h)

其中 h 為 face box 高度。此點通常比 face center 更接近眼部實際位置。

### 17.6 UI 與產品定義建議

設計上建議避免對使用者暗示「一定已完成精準硬體鎖眼」，而應採用較真實的產品語義。

#### 建議語義
- Cyan：主體 / 眼睛已穩定鎖定
- Yellow：AE/AF Lock
- 不以顏色直接表示「鏡頭物理一定已完成鎖焦」

#### 建議互動
- 前鏡頭：優先給穩定視覺回饋與低抖動框
- 後鏡頭：可在 target 切換時強化 focus pulse 或短暫動畫
- 若硬體不支援，不顯示誤導性的「已精準對焦眼睛」文案

**平衡點**
- UI 應反映「系統正在優先服務這張臉 / 這雙眼」，而不是誇大硬體能力

### 17.7 技術破口整理

即使硬體有限，仍有幾個可明顯提升體驗的破口：

1. **把 Eye-AF 重心放在主體選擇而非絕對光學鎖焦**
   - 這是最現實也最穩定的產品方向。

2. **將 metering 點從 face-center 提升到 eye-center / upper-face**
   - 即使不是精準鎖焦，也常能改善曝光與 focus priority 的主觀感受。

3. **加入可靠的降級策略**
   - 眼睛失敗時不要整個 state 崩潰，而是平滑退到 face-based tracking。

4. **導入時間穩定與區域平滑**
   - 比起追求單幀最準，連續幀穩定更影響真實體驗。

5. **將多人臉 priority 與 trackingId 做好**
   - 使用者最在意的往往不是物理極限，而是「系統有沒有一直抓對同一個人」。

### 17.8 工程決策建議

對本專案建議採以下決策：

#### 前鏡頭
- Eye-AF = Visual Lock
- 不等待 AF success
- 以眼睛 / 臉框上半部作為 metering 與曝光優先區
- 強化 UI 穩定度與低光退化策略

#### 後鏡頭
- Eye-AF = Visual Lock + Metering Priority
- `OpticalFocused` 只作附加狀態
- continuous AF 不作主狀態唯一依據
- target 切換時才考慮主動 trigger focus

#### 通用
- 眼睛優先，但允許平滑退化到 face region
- 所有 UI 只吃 view-space geometry
- 狀態機不得將 visual success 與 optical success 綁死

### 17.9 本節結論

Eye-AF 的真正平衡點，不是硬要把每台手機都做成「相機級精準鎖眼」，而是：

1. **視覺上穩定知道誰是主體、哪裡是眼部區域**
2. **幾何上穩定把焦點框與 metering 區導向正確位置**
3. **硬體上能做多少就做多少，但不把產品體驗綁死在不可控 callback 上**
4. **當眼睛不可靠時，平滑退化為臉部或主體追蹤，而不是整體失效**

因此，本專案在技術與硬體瓶頸之間的最佳平衡策略為：

**以前後鏡頭分流、以 Visual Lock 為主、以 Metering Priority 為實用突破口、以 Optical Success 為附加價值。**

## 17.10 Eye-AF 實作決策表（前鏡頭 / 後鏡頭 / 低光 / 多人臉 / 遮擋）

本節提供可直接落地到程式流程與狀態機分支的決策表，目的不是追求理論最完整，而是讓開發時能快速判斷：在不同鏡頭能力、場景條件、偵測品質下，系統應採用哪一種 Eye-AF 策略、顯示哪種 UI、是否要真的送出 focus trigger。

### 17.10.1 決策原則

決策順序建議固定為：

1. 先判斷 **鏡頭能力**
2. 再判斷 **目標可靠度**
3. 再判斷 **是否需要主動 focus trigger**
4. 最後決定 **UI 呈現與降級策略**

也就是說，不可先看到眼睛就直接送 AF，也不可先等 AF callback 才決定 UI 成敗。

建議流程：

```text
Lens Capability -> Target Reliability -> Geometry Validity -> Trigger Policy -> UI State
```

### 17.10.2 前鏡頭決策表

| 條件 | 偵測輸入 | 控制策略 | UI / 狀態 | 備註 |
|---|---|---|---|---|
| 前鏡頭 + fixed-focus + 雙眼穩定 | 雙眼點位 + face box | 不等待 AF success；以雙眼中心或 upper-face 做 metering priority | Cyan locked | 主路徑 |
| 前鏡頭 + fixed-focus + 單眼穩定 | 單眼 + face box | 不送 one-shot AF；以單眼或 upper-face 區域穩定追蹤 | Cyan locked | 退化但可用 |
| 前鏡頭 + fixed-focus + 只有 face box | face box | 退化為 upper-face / face-center | Cyan tracking | 不宣稱鎖眼 |
| 前鏡頭 + fixed-focus + face intermittent lost | 最近穩定 target + optical flow | 短時間維持舊 target；超時後 fade out | Cyan fade -> Lost | 需設 timeout |
| 前鏡頭 + 低光 / 逆光 | face box 優先，眼點次之 | 提高平滑權重，降低切換頻率 | Cyan tracking | 穩定優先於靈敏 |

### 17.10.3 後鏡頭決策表

| 條件 | 偵測輸入 | 控制策略 | UI / 狀態 | 備註 |
|---|---|---|---|---|
| 後鏡頭 + variable focus + 雙眼穩定 | 雙眼點位 + trackingId | 導向 eye-centered metering；必要時送 focus trigger | Cyan locked + OpticalFocused 可選 | 理想路徑 |
| 後鏡頭 + variable focus + 單眼穩定 | 單眼 + face box | 導向單眼或 upper-face；避免頻繁 trigger | Cyan locked | 適合側臉 |
| 後鏡頭 + continuous AF + 主體穩定 | 雙眼或 face box | 以 continuous AF 為主，不頻繁送 one-shot | Cyan locked | callback 不作唯一 gate |
| 後鏡頭 + target switch | 新 target 高分勝出 | 允許一次 focus pulse / startFocusAndMetering | Cyan reacquire | 需設 hysteresis |
| 後鏡頭 + 只有 face box | face box | 退化為 upper-face / face-centered metering | Cyan tracking | 不強行標示鎖眼 |
| 後鏡頭 + 長按 lock | 任意穩定 target | 切換至 AE/AF lock 分支 | Yellow lock | 與 Eye-AF 分離 |

### 17.10.4 場景條件決策表

| 場景 | 典型問題 | 建議策略 | 不建議作法 |
|---|---|---|---|
| 低光 | 眼睛 landmark 不穩 | 提高 face box 權重；降低 target 切換 | 硬追單眼點位 |
| 側臉 | 常只剩單眼 | 單眼可接受；退化到 upper-face | 因缺少雙眼直接判定失敗 |
| 遮擋 | 眼睛短暫消失 | 維持最近穩定 target + optical flow | 立即清空 UI |
| 快速移動 | point jitter / box lag | 加 dwell time + box smoothing | 每幀切換 target |
| 多人臉 | 主體來回跳 | 用 trackingId + priority + hysteresis | firstOrNull |
| 背景複雜 | 光流漂移 | 以 ML face re-acquire 為主，光流只短暫接手 | 光流長時間單獨主導 |

### 17.10.5 多人臉選主體決策表

建議先算每個候選目標的 priority score：

P_i = w_a * A_i + w_c * C_i + w_t * T_i + w_d * D_i + w_u * U_i

其中：
- A_i：面積占比
- C_i：模型信心
- T_i：追蹤穩定時長
- D_i：距離使用者互動點接近度
- U_i：構圖或畫面中心權重

實務決策：

| 條件 | 決策 |
|---|---|
| 無使用者手動點擊 | 取最高 priority target |
| 剛有 tap focus / tap subject | 該 target 暫時加最高權重 |
| 新 target 分數僅略高 | 不切換，避免跳動 |
| 新 target 分數明顯高且持續 N 幀 | 允許切換 |
| 舊 target 短暫消失 | 先保留 stableId 與光流位置，等待重綁 |

建議加入切換遲滯條件：

```text
only switch if newScore > oldScore + margin for N consecutive frames
```

建議預設：
- `margin = 0.12 ~ 0.2`
- `N = 3 ~ 5 frames`

### 17.10.6 Trigger Policy 決策表

| 條件 | 是否送 focus trigger | 理由 |
|---|---|---|
| 前鏡頭 fixed-focus | 否 / 可僅送形式 request | 不應依賴 AF success |
| 後鏡頭 continuous AF + target 穩定 | 通常否 | 避免頻繁打斷被動連續對焦 |
| 後鏡頭新 target 切換 | 是，送一次 | 幫助重新導向主體 |
| 使用者 tap focus | 是 | 使用者明確意圖 |
| 低光 + landmark 抖動 | 保守 | 避免反覆 trigger 導致體驗更差 |
| AE/AF lock 模式 | 視 lock 流程而定 | 與 Eye-AF 獨立處理 |

### 17.10.7 UI 呈現決策表

| 狀態 | 顏色 | 顯示內容 | 說明 |
|---|---|---|---|
| 雙眼穩定 | Cyan | 動態 eye/face box | Eye-AF 主狀態 |
| 單眼穩定 | Cyan | 稍保守 box | 視覺成功仍成立 |
| 僅 face box | Cyan | upper-face / face box | 降級但不中斷 |
| target 短暫遺失 | Cyan alpha fade | 保留短暫軌跡 | 避免閃爍 |
| AE/AF lock | Yellow | lock state | 嚴格獨立於 Eye-AF |
| optical success 附加成立 | 不另換色 | 可加 pulse / dwell 強化 | 不改變主語義 |

### 17.10.8 建議參數預設

| 參數 | 建議值 | 用途 |
|---|---|---|
| eye reliability lock threshold | 0.75 | 雙眼中心可直接使用 |
| eye reliability fallback threshold | 0.5 | 低於此值退化到 face region |
| target switch margin | 0.12 ~ 0.2 | 避免多人臉跳動 |
| consecutive frames for switch | 3 ~ 5 | 確認新目標穩定 |
| lost timeout | 150 ~ 350 ms | 短暫遮擋不立刻丟失 |
| box smoothing alpha | 0.25 ~ 0.4 | 平滑 UI 框 |
| trigger cooldown | 600 ~ 1200 ms | 避免頻繁 focus trigger |

### 17.10.9 推薦實作偽代碼

```kotlin
fun resolveEyeAfPolicy(input: EyeAfInput): EyeAfDecision {
    val lens = input.lensCapability
    val target = input.bestTarget
    val reliability = input.eyeReliability

    if (target == null) {
        return EyeAfDecision.lost()
    }

    val anchor = when {
        reliability >= 0.75f && target.hasBothEyes -> Anchor.BothEyesCenter
        reliability >= 0.5f && target.hasSingleEye -> Anchor.SingleEyeOrUpperFace
        target.hasFaceBox -> Anchor.UpperFace
        target.hasTrackedRegion -> Anchor.TrackedRegion
        else -> Anchor.Lost
    }

    val shouldTriggerFocus = when {
        lens.isFrontFixedFocus -> false
        input.isUserTap -> true
        input.isTargetSwitch && input.triggerCooldownPassed -> true
        lens.isContinuousAf && input.targetStable -> false
        else -> false
    }

    return EyeAfDecision(
        anchor = anchor,
        showCyanLocked = anchor != Anchor.Lost,
        shouldTriggerFocus = shouldTriggerFocus,
        allowOpticalFocusedState = lens.supportsVariableFocus,
    )
}
```

### 17.10.10 本節結論

實作上最重要的不是把所有情況都逼成同一套 Eye-AF 流程，而是建立 **可預期的分流策略**：

- 前鏡頭以 Visual Lock 為主
- 後鏡頭以 Metering Priority 為主，Optical Success 為附加
- 低光、遮擋、側臉時允許平滑降級
- 多人臉時用 priority + hysteresis 保持穩定
- UI 成功不依賴不可控的單次 AF callback

如此才能在技術可實現性與硬體真實限制之間，取得可交付且可維護的平衡。

## 17.11 CameraX + ML Kit + StateMachine 實作介面草圖

本節目的在於把前述設計原則進一步落成可實作的模組邊界，避免後續開發仍把偵測、座標轉換、focus 控制與 UI overlay 混寫在同一層。以下介面草圖不追求完整程式碼，而是先定義責任、輸入輸出與關聯資料流。

### 17.11.1 模組分層總覽

```text
CameraX (Preview / ImageAnalysis)
    -> FrameAdapter
    -> DetectionEngine (ML Kit Face / Object)
    -> GeometryMapper
    -> TargetTracker
    -> EyeAfPolicyResolver
    -> FocusController
    -> FocusStateMachine
    -> OverlayViewModel
    -> Compose Overlay Renderer
```

分層原則：
- `DetectionEngine` 不碰 UI。
- `GeometryMapper` 不做狀態判斷。
- `FocusController` 不決定畫框大小。
- `OverlayRenderer` 不自行推論 focus 成敗。
- `StateMachine` 不直接讀取 CameraX raw callback 以外的 UI 狀態。

### 17.11.2 Frame 與偵測輸入模型

```kotlin
data class FramePacket(
    val frameId: Long,
    val timestampMs: Long,
    val imageWidth: Int,
    val imageHeight: Int,
    val rotationDegrees: Int,
    val lensFacing: Int,
    val imageProxyRef: Any?,
    val sensorToBufferMatrix: android.graphics.Matrix?,
)

data class DetectionFrameContext(
    val frame: FramePacket,
    val previewWidth: Int,
    val previewHeight: Int,
    val isMirroredPreview: Boolean,
)
```

說明：
- `FramePacket` 只描述單幀輸入與必要幾何資訊。
- 不建議在資料層直接長期持有 `ImageProxy`；若需跨 coroutine 傳遞，應盡快轉成可安全使用的中介資料。

### 17.11.3 DetectionEngine 介面

```kotlin
interface DetectionEngine {
    suspend fun detect(context: DetectionFrameContext): DetectionResult
}

data class DetectionResult(
    val frameId: Long,
    val faces: List<DetectedFace>,
    val objects: List<DetectedObject>,
    val detectLatencyMs: Long,
)

data class DetectedFace(
    val trackingId: Int?,
    val boundingBoxImage: RectF,
    val leftEyeImage: PointF?,
    val rightEyeImage: PointF?,
    val confidence: Float,
)

data class DetectedObject(
    val trackingId: Int?,
    val boundingBoxImage: RectF,
    val confidence: Float,
    val label: String?,
)
```

實作建議：
- `MlKitFaceDetectionEngine`
- `MlKitObjectDetectionEngine`
- `CompositeDetectionEngine` 聚合多路結果

### 17.11.4 GeometryMapper 介面

```kotlin
interface GeometryMapper {
    fun mapRectImageToView(rect: RectF, context: GeometryContext): RectF
    fun mapPointImageToView(point: PointF, context: GeometryContext): PointF
}

data class GeometryContext(
    val imageWidth: Int,
    val imageHeight: Int,
    val previewWidth: Int,
    val previewHeight: Int,
    val sensorToBufferMatrix: android.graphics.Matrix?,
    val sensorToViewMatrix: android.graphics.Matrix?,
    val isFrontCamera: Boolean,
    val isMirroredPreview: Boolean,
)
```

工程原則：
- 所有 `rectView`、`pointView` 都必須經過這層。
- UI / ViewModel / StateMachine 禁止手寫比例換算。
- 若 `sensorToViewMatrix` 缺失，必須明確 fallback，不能靜默採錯誤比例。

### 17.11.5 FocusTarget 與 Tracker 介面

```kotlin
data class FocusTarget(
    val stableId: String,
    val type: TargetType,
    val rectImage: RectF,
    val rectView: RectF,
    val leftEyeView: PointF?,
    val rightEyeView: PointF?,
    val confidence: Float,
    val trackingId: Int?,
    val priorityScore: Float,
    val firstSeenMs: Long,
    val lastSeenMs: Long,
    val source: TargetSource,
)

interface TargetTracker {
    fun update(
        detection: DetectionResult,
        geometryContext: GeometryContext,
        nowMs: Long,
        userIntent: UserIntentState,
    ): TrackedTargetState
}

data class TrackedTargetState(
    val allTargets: List<FocusTarget>,
    val primaryTarget: FocusTarget?,
    val recentlyLostTarget: FocusTarget?,
)
```

實作責任：
- 將 face/object/tap subject 統一成 target list。
- 計算 priority score。
- 保留 stableId。
- 管理短暫遺失、重綁與 hysteresis。

### 17.11.6 UserIntentState 介面

```kotlin
data class UserIntentState(
    val tapPointView: PointF?,
    val tapTimestampMs: Long?,
    val isTapToFocusActive: Boolean,
    val isAeAfLockRequested: Boolean,
)
```

說明：
- 使用者意圖不可散落在 UI 與 controller 各自管理。
- `tap` 事件應成為 target ranking 的加權來源，而不是只驅動一次 AF request。

### 17.11.7 EyeAfPolicyResolver 介面

```kotlin
interface EyeAfPolicyResolver {
    fun resolve(input: EyeAfPolicyInput): EyeAfDecision
}

data class EyeAfPolicyInput(
    val primaryTarget: FocusTarget?,
    val lensCapability: LensCapability,
    val userIntent: UserIntentState,
    val nowMs: Long,
    val lastFocusTriggerMs: Long?,
)

data class EyeAfDecision(
    val anchor: MeteringAnchor,
    val visualState: VisualTargetState,
    val shouldTriggerFocus: Boolean,
    val shouldTriggerMetering: Boolean,
    val allowOpticalState: Boolean,
)

enum class MeteringAnchor {
    BOTH_EYES_CENTER,
    SINGLE_EYE,
    UPPER_FACE,
    FACE_CENTER,
    TRACKED_REGION,
    TAP_POINT,
    NONE,
}
```

核心原則：
- 這一層負責「決策」，不是直接呼叫 CameraX。
- 所有前後鏡頭差異、low-light 降級、遮擋 fallback 都應集中在此。

### 17.11.8 LensCapability 與 FocusController 介面

```kotlin
data class LensCapability(
    val isFrontCamera: Boolean,
    val isFixedFocus: Boolean,
    val supportsAutoFocus: Boolean,
    val supportsContinuousAf: Boolean,
    val supportsAeAfLock: Boolean,
)

interface FocusController {
    suspend fun apply(decision: EyeAfDecision, target: FocusTarget?, geometryContext: GeometryContext): FocusControllerResult
}

data class FocusControllerResult(
    val triggerSent: Boolean,
    val meteringPointView: PointF?,
    val meteringRegionView: RectF?,
    val opticalResult: OpticalFocusState?,
)

enum class OpticalFocusState {
    UNKNOWN,
    PASSIVE_CONTINUOUS,
    FOCUSED,
    NOT_FOCUSED,
    UNSUPPORTED,
}
```

說明：
- `FocusController` 只接收 policy decision 後執行，不自己判斷產品語義。
- `OpticalFocusState` 只作附加訊號，不能回頭覆蓋 visual state。

### 17.11.9 FocusStateMachine 介面

```kotlin
interface FocusStateMachine {
    fun reduce(input: FocusStateInput): FocusUiState
}

data class FocusStateInput(
    val trackedTargetState: TrackedTargetState,
    val eyeAfDecision: EyeAfDecision,
    val focusControllerResult: FocusControllerResult?,
    val userIntent: UserIntentState,
    val systemEvent: SystemEvent,
    val nowMs: Long,
)

enum class SystemEvent {
    NORMAL_UPDATE,
    LENS_SWITCHED,
    LIFECYCLE_STOPPED,
}

data class FocusUiState(
    val visualTargetState: VisualTargetState,
    val lensFocusState: LensFocusState,
    val exposureLockState: ExposureLockState,
    val overlay: OverlayModel,
)
```

狀態機原則：
- `VisualTargetState`、`LensFocusState`、`ExposureLockState` 三者分開維護。
- `AE/AF Lock` 不可被 Eye-AF 成功隱式觸發。
- `OpticalFocused` 不可覆蓋 `Yellow lock` 或 `Cyan visual lock` 的語義。
- `SystemEvent.LENS_SWITCHED` 進入狀態機時，必須強制清空 target、overlay 與任何 fade / synthetic lock 殘留狀態。
- `SystemEvent.LIFECYCLE_STOPPED` 進入狀態機時，必須重置追蹤、停止馬達請求，並讓 UI 回到完全空白狀態。

### 17.11.10 OverlayModel 與 ViewModel 介面

```kotlin
data class OverlayModel(
    val primaryBox: RectF?,
    val secondaryBoxes: List<RectF>,
    val primaryColor: OverlayColor,
    val alpha: Float,
    val showLockIcon: Boolean,
    val animation: OverlayAnimation,
)

enum class OverlayColor {
    CYAN,
    YELLOW,
}

enum class OverlayAnimation {
    NONE,
    PULSE,
    FADE,
    REACQUIRE,
}

interface OverlayViewModelContract {
    fun onFocusUiState(state: FocusUiState)
}
```

UI 規則：
- Compose Overlay 僅根據 `OverlayModel` 畫圖。
- 不在 Composable 內重算業務狀態。
- secondary boxes 可用於多人臉弱提示，但主 target 必須只有一個。
- **絕對禁止預設框 (No Fallback Box)**：當 `primaryBox` 為 `null`，或 `VisualTargetState` 為 `None` 時，Compose Overlay 必須完全透明 / 隱藏。絕對禁止在畫面上渲染任何 36dp 或固定大小的預設方塊。

### 17.11.11 資料流時序建議

```text
FramePacket
  -> DetectionEngine.detect()
  -> GeometryMapper.map(...)
  -> TargetTracker.update(...)
  -> EyeAfPolicyResolver.resolve(...)
  -> FocusController.apply(...)
  -> FocusStateMachine.reduce(...)
  -> OverlayViewModel.onFocusUiState(...)
  -> Compose Overlay
```

時序原則：
- 幾何轉換在 target ranking 前完成。
- policy decision 在 focus controller 前完成。
- UI state 一律由 state machine 輸出，不由 callback 直接改 UI。

### 17.11.12 執行緒與效能建議

- `ImageAnalysis` 與 ML 偵測跑背景執行緒。
- `GeometryMapper` 應盡量無配置、低成本。
- `TargetTracker` 與 `PolicyResolver` 可在同一 background pipeline 中完成。
- `OverlayViewModel` 才回主執行緒。
- 每幀都送 focus trigger 是禁止行為，需有 cooldown。
- **狀態流 (`StateFlow`)**：`FocusStateMachine` 輸出的 `FocusUiState` 必須透過 `StateFlow` 暴露給 UI 層，確保 Compose 只渲染最新狀態，並可透過 `collectAsStateWithLifecycle` 做生命週期感知收集。
- **硬體非同步 (`Coroutines` / `Channel`)**：`FocusController.apply(...)` 呼叫相機底層時，必須在獨立 coroutine context 執行，且建議以 `Channel` 或等價序列化機制管理 focus / metering / lock 指令，避免 UI thread 阻塞、callback 競態與馬達命令重入。

建議：
- 偵測頻率可低於 preview FPS
- 其間用 tracker / optical flow 補足
- UI 更新節流，但不可犧牲主 target 穩定性

### 17.11.13 錯誤處理與 fallback 建議

| 問題 | fallback |
|---|---|
| 臉偵測失敗 | 保留最近穩定 target 短時間 + fade |
| 眼睛 landmark 缺失 | 退化到 upper-face |
| geometry matrix 缺失 | 暫停顯示精準 eye box，改顯示保守 face box 或隱藏 |
| focus trigger 失敗 | 保留 visual lock，不立即清空 UI |
| object / face trackingId 缺失 | 以 IoU + 時間窗產生暫時 stableId |

### 17.11.14 最小落地實作順序

1. 先定義 `FocusTarget`、`GeometryContext`、`FocusUiState`
2. 把現有 ML Kit 結果改由 `DetectionEngine` 統一輸出
3. 導入 `GeometryMapper`，移除 UI 端手算比例
4. 導入 `TargetTracker` 與 `stableId`
5. 導入 `EyeAfPolicyResolver`
6. Focus callback 改由 `FocusControllerResult` 輸入狀態機
7. Overlay 僅吃 `OverlayModel`

### 17.11.15 本節結論

若沒有先把模組介面定清楚，後續就算局部修好座標或 AF callback，仍會因資料責任混亂而再次失控。最佳實作方式不是把更多邏輯塞進 CameraX callback，而是先建立：

- 可測試的 DetectionEngine
- 單一真實來源的 GeometryMapper
- 可維護的 EyeAfPolicyResolver
- 不混語義的 FocusStateMachine
- 純顯示用 OverlayModel

如此才能讓 Eye-AF 從「看起來像功能」進化成「可驗證、可擴充、可維護的系統能力」。

## 17.12 Kotlin 類別骨架與檔案切分建議

本節將前述介面草圖直接對應到目前專案既有分層：

- `domain` 放純資料模型與介面
- `data` 放 CameraX / ML Kit / OpenCV 實作
- `presentation` 放 state machine、UI state 與 overlay mapping
- `di` 放 Hilt 綁定

目標不是一次把所有檔案大搬家，而是讓新模組可以逐步取代目前散落在 `CameraViewModel`、`FocusOrchestrator`、`AfStateMachine`、`CoordinateMapper`、`SmartCameraAnalyzer` 中的責任。

### 17.12.1 建議 package tree

```text
com.akirachang.ai.photographer
├─ domain
│  ├─ model
│  │  └─ focus
│  │     ├─ DetectionFrameContext.kt
│  │     ├─ DetectionResult.kt
│  │     ├─ EyeAfPolicyModels.kt
│  │     ├─ FocusControllerResult.kt
│  │     ├─ FocusStateInput.kt
│  │     ├─ FocusTarget.kt
│  │     ├─ FocusStates.kt
│  │     ├─ FramePacket.kt
│  │     ├─ GeometryContext.kt
│  │     ├─ LensCapability.kt
│  │     ├─ OverlayModel.kt
│  │     ├─ TrackedTargetState.kt
│  │     └─ UserIntentState.kt
│  └─ focus
│     ├─ control
│     │  ├─ FocusController.kt
│     │  └─ LensCapabilityResolver.kt
│     ├─ detection
│     │  └─ DetectionEngine.kt
│     ├─ geometry
│     │  └─ GeometryMapper.kt
│     ├─ policy
│     │  └─ EyeAfPolicyResolver.kt
│     └─ tracking
│        ├─ TapSubjectAcquirer.kt
│        └─ TargetTracker.kt
├─ data
│  └─ focus
│     ├─ control
│     │  ├─ CameraXFocusController.kt
│     │  └─ CameraXLensCapabilityResolver.kt
│     ├─ detection
│     │  ├─ CompositeDetectionEngine.kt
│     │  ├─ MlKitFaceDetectionEngine.kt
│     │  └─ MlKitObjectDetectionEngine.kt
│     ├─ geometry
│     │  └─ PreviewViewGeometryMapper.kt
│     ├─ policy
│     │  └─ DefaultEyeAfPolicyResolver.kt
│     └─ tracking
│        ├─ DefaultTargetTracker.kt
│        └─ OpticalFlowTapSubjectAcquirer.kt
├─ presentation
│  └─ camera
│     ├─ focus
│     │  ├─ DefaultFocusStateMachine.kt
│     │  ├─ FocusStateMachine.kt
│     │  ├─ FocusUiState.kt
│     │  ├─ FocusUiStateMapper.kt
│     │  └─ FocusTriggerThrottle.kt
│     └─ components
│        └─ overlay
│           └─ FocusOverlay.kt
└─ di
   └─ FocusModule.kt
```

### 17.12.2 interface / implementation / 檔名對照表

| 責任 | Interface package + 檔名 | Implementation package + 檔名 | 備註 |
|---|---|---|---|
| 偵測聚合 | `domain.focus.detection.DetectionEngine` / `DetectionEngine.kt` | `data.focus.detection.CompositeDetectionEngine` / `CompositeDetectionEngine.kt` | 對外只暴露單一 `detect()` 入口 |
| 人臉偵測 | 無需額外 domain interface，可做 detection 子元件 | `data.focus.detection.MlKitFaceDetectionEngine` / `MlKitFaceDetectionEngine.kt` | 包住 ML Kit FaceDetector |
| 物件偵測 | 無需額外 domain interface，可做 detection 子元件 | `data.focus.detection.MlKitObjectDetectionEngine` / `MlKitObjectDetectionEngine.kt` | 包住 ML Kit ObjectDetector |
| 座標映射 | `domain.focus.geometry.GeometryMapper` / `GeometryMapper.kt` | `data.focus.geometry.PreviewViewGeometryMapper` / `PreviewViewGeometryMapper.kt` | 取代目前 `util/CoordinateMapper.kt` 的業務責任 |
| 多目標追蹤 | `domain.focus.tracking.TargetTracker` / `TargetTracker.kt` | `data.focus.tracking.DefaultTargetTracker` / `DefaultTargetTracker.kt` | 管理 `stableId`、排序、重綁、hysteresis |
| 點擊主體取得 | `domain.focus.tracking.TapSubjectAcquirer` / `TapSubjectAcquirer.kt` | `data.focus.tracking.OpticalFlowTapSubjectAcquirer` / `OpticalFlowTapSubjectAcquirer.kt` | 先吃 ROI + optical flow，之後可再接 detector |
| Eye-AF 決策 | `domain.focus.policy.EyeAfPolicyResolver` / `EyeAfPolicyResolver.kt` | `data.focus.policy.DefaultEyeAfPolicyResolver` / `DefaultEyeAfPolicyResolver.kt` | 吸收目前 `FocusOrchestrator` 的產品規則 |
| 鏡頭能力解析 | `domain.focus.control.LensCapabilityResolver` / `LensCapabilityResolver.kt` | `data.focus.control.CameraXLensCapabilityResolver` / `CameraXLensCapabilityResolver.kt` | 包裝 fixed-focus / continuous AF / AEAF lock 能力 |
| 對焦執行 | `domain.focus.control.FocusController` / `FocusController.kt` | `data.focus.control.CameraXFocusController` / `CameraXFocusController.kt` | 單純執行 metering / trigger，不做產品語義判斷 |
| 狀態機 | `presentation.camera.focus.FocusStateMachine` / `FocusStateMachine.kt` | `presentation.camera.focus.DefaultFocusStateMachine` / `DefaultFocusStateMachine.kt` | 將 visual / lens / exposure 三路 state 分開 reduce |
| 節流控制 | 無需 interface，屬 presentation 細節 | `presentation.camera.focus.FocusTriggerThrottle` / `FocusTriggerThrottle.kt` | 從目前 `AfStateMachine.kt` 拆出 throttle 與 stability tracking |
| UI state 組裝 | 無需 interface，屬 presentation 細節 | `presentation.camera.focus.FocusUiStateMapper` / `FocusUiStateMapper.kt` | 將 domain focus state 轉為 Compose 可直接吃的 UI model |
| Overlay Render | 無需 interface，屬 UI 元件 | `presentation.camera.components.overlay.FocusOverlay` / `FocusOverlay.kt` | 只吃 `OverlayModel`，不自行推論對焦結果 |

### 17.12.3 建議資料模型檔名

建議將純資料模型集中到 `com.akirachang.ai.photographer.domain.model.focus`，避免 model 散落在 `presentation` 與 `data`。

| 檔名 | 主要內容 |
|---|---|
| `FramePacket.kt` | `FramePacket` |
| `DetectionFrameContext.kt` | `DetectionFrameContext` |
| `DetectionResult.kt` | `DetectionResult`、`DetectedFace`、`DetectedObject` |
| `EyeAfPolicyModels.kt` | `EyeAfPolicyInput`、`EyeAfDecision`、`MeteringAnchor` |
| `FocusControllerResult.kt` | `FocusControllerResult` |
| `FocusStateInput.kt` | `FocusStateInput`、`SystemEvent` |
| `GeometryContext.kt` | `GeometryContext` |
| `FocusTarget.kt` | `FocusTarget`、`TargetType`、`TargetSource` |
| `TrackedTargetState.kt` | `TrackedTargetState` |
| `UserIntentState.kt` | `UserIntentState` |
| `LensCapability.kt` | `LensCapability` |
| `FocusStates.kt` | `VisualTargetState`、`LensFocusState`、`ExposureLockState`、`OpticalFocusState` |
| `OverlayModel.kt` | `OverlayModel`、`OverlayColor`、`OverlayAnimation` |

若考量檔案數量，也可先採以下合併策略：

- `FocusPolicyModels.kt`：放 `EyeAfPolicyInput`、`EyeAfDecision`、`MeteringAnchor`、`FocusControllerResult`
- `FocusTrackingModels.kt`：放 `FocusTarget`、`TrackedTargetState`
- `FocusUiState.kt`：放 `FocusUiState`、`FocusStateInput`、`SystemEvent` 與 overlay 相關 state

### 17.12.4 代表性 Kotlin 骨架

```kotlin
package com.akirachang.ai.photographer.domain.focus.geometry

import android.graphics.PointF
import android.graphics.RectF
import com.akirachang.ai.photographer.domain.model.focus.GeometryContext

interface GeometryMapper {
    fun mapRectImageToView(rect: RectF, context: GeometryContext): RectF
    fun mapPointImageToView(point: PointF, context: GeometryContext): PointF
}
```

```kotlin
package com.akirachang.ai.photographer.domain.focus.tracking

import com.akirachang.ai.photographer.domain.model.focus.DetectionResult
import com.akirachang.ai.photographer.domain.model.focus.GeometryContext
import com.akirachang.ai.photographer.domain.model.focus.TrackedTargetState
import com.akirachang.ai.photographer.domain.model.focus.UserIntentState

interface TargetTracker {
    fun update(
        detection: DetectionResult,
        geometryContext: GeometryContext,
        nowMs: Long,
        userIntent: UserIntentState,
    ): TrackedTargetState
}
```

```kotlin
package com.akirachang.ai.photographer.data.focus.control

import com.akirachang.ai.photographer.domain.focus.control.FocusController
import com.akirachang.ai.photographer.domain.model.focus.EyeAfDecision
import com.akirachang.ai.photographer.domain.model.focus.FocusTarget
import com.akirachang.ai.photographer.domain.model.focus.GeometryContext

class CameraXFocusController(
    private val cameraRepository: com.akirachang.ai.photographer.domain.repository.CameraRepository,
) : FocusController {
    override suspend fun apply(
        decision: EyeAfDecision,
        target: FocusTarget?,
        geometryContext: GeometryContext,
    ): com.akirachang.ai.photographer.domain.model.focus.FocusControllerResult {
        TODO("delegate CameraX metering / trigger here")
    }
}
```

```kotlin
package com.akirachang.ai.photographer.presentation.camera.focus

import com.akirachang.ai.photographer.domain.model.focus.FocusStateInput

interface FocusStateMachine {
    fun reduce(input: FocusStateInput): FocusUiState
}
```

### 17.12.5 與現有檔案的遷移對照

| 現有檔案 | 建議處理方式 | 新位置 / 新檔名 |
|---|---|---|
| `util/CoordinateMapper.kt` | 保留為舊版相容工具，新的 focus 幾何邏輯不要再繼續擴充 | `domain.focus.geometry.GeometryMapper` + `data.focus.geometry.PreviewViewGeometryMapper` |
| `presentation/camera/FocusOrchestrator.kt` | 改為薄轉接或直接退役 | `domain.focus.policy.EyeAfPolicyResolver` + `data.focus.policy.DefaultEyeAfPolicyResolver` |
| `presentation/camera/AfStateMachine.kt` | 拆為狀態機與節流器兩個檔案 | `presentation/camera/focus/DefaultFocusStateMachine.kt`、`FocusTriggerThrottle.kt` |
| `presentation/camera/CameraState.kt` | 保留 UI 聚合入口，但 focus 相關欄位逐步收斂為 `FocusUiState` | `presentation/camera/focus/FocusUiState.kt` |
| `data/source/camera/SmartCameraAnalyzer.kt` | 僅保留 frame pipeline / callback 橋接，不再承擔 target ranking 與 UI 幾何 | 改依賴 `DetectionEngine`、`TargetTracker`、`GeometryMapper` |
| `data/repository/CameraRepositoryImpl.kt` | 保留 façade 與 CameraX 委派，不再塞入 Eye-AF 規則 | 由 `FocusModule.kt` 注入 focus pipeline 元件 |

### 17.12.6 最小可行切分順序

若要避免一次重構過大，建議依以下順序建立檔案：

1. 先新增 `domain.model.focus` 下的 model 檔案，不動既有邏輯。
2. 新增 `GeometryMapper.kt` 與 `PreviewViewGeometryMapper.kt`，先取代 `CoordinateMapper` 的新使用點。
3. 新增 `EyeAfPolicyResolver.kt` 與 `DefaultEyeAfPolicyResolver.kt`，先讓 `FocusOrchestrator` 變成 thin adapter。
4. 將 `AfStateMachine.kt` 拆成 `DefaultFocusStateMachine.kt` 與 `FocusTriggerThrottle.kt`。
5. 最後再新增 `TargetTracker.kt`、`DefaultTargetTracker.kt` 與 `CameraXFocusController.kt`，把 `CameraViewModel` 內的 tracking / focus orchestration 移出。

## 17. 最終 L2 結論

本議題在既有技術棧下**具備高可行性**，但前提是停止以局部 patch 修補，改採結構化重整：

1. 幾何映射獨立
2. 視覺鎖定 / 光學 AF / AEAF Lock 三者分離
3. 多目標資料模型導入 stable identity
4. 點擊對焦拆成硬體路徑與視覺路徑

若依本規格實施，將可同時解決：
- 對焦框固定大小
- 前鏡頭 Eye-AF 死鎖
- 後鏡頭 continuous AF 卡死
- 多人臉失效
- 點擊對焦無法包覆主體

並為後續 Face Mesh、Segmentation、進階追蹤平滑預留擴充空間。
