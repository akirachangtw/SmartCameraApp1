<!-- generated: 2026-01-05 11:14:03 -->

# Level 1｜規劃摘要：smart_camera_app_with_UIUX_plan

## 1. 目標（Goal）
- 將「UI/UX 視覺優化改進」納入既有開發計畫文件 **`@plans\02-smart_camera_app_development_plan_v_2.md`**，形成可執行的增補章節與待辦清單。
- 在不破壞既有 CameraX 即時管線、模式切換（Live/Static/Debug）與 AI 分析效能前提下，提升：
  - 取景畫面可讀性（資訊層級、對比、遮擋控制）
  - 操作效率（常用操作可達性、減少誤觸）
  - 視覺一致性（字體、間距、色彩、動效規範）

---

## 2. 背景與問題（Why / What）
- 目前專案正在進行 Smart Camera App（CameraX + AI 即時影像）開發；使用者補充「新增 UI/UX 視覺優化改進」，需要整合到既有 v2 計畫文件中，避免 UI 改動變成臨時插單而造成：
  - 需求散落在對話或零碎備忘錄（缺乏 SSOT）
  - UI 視覺調整不小心影響相機預覽比例、裁切、overlay 對齊或 FPS
  - 模式切換時 UI state 不一致（例如 Live/Static 切換後控制項狀態殘留）
- Git 現況顯示近期偏向「工作流與文件保護/自動化」的變更增加（tools/hybrid_flow、VSCode tasks、docs 新增），因此本次 UI/UX 需要明確定義「要改什麼、在哪裡改、怎麼驗證」，以免導致改動擴散。

---

## 3. 影響範圍（Scope：模組/檔案/功能層級；若未知請以推測標註）
> 以下以推測標註（需依 repo 實際結構校正）

### 文件 / 規劃層
- **`@plans\02-smart_camera_app_development_plan_v_2.md`**：新增 UI/UX 視覺優化章節、里程碑、驗收標準、風險註記（本次主要目標）
- （推測）`docs/plan/smart_camera_app_with_UIUX_plan_L1.md`：本摘要落盤（若採用 hybrid_flow 產出位置則遵循 `docs/plan/`）

### UI 層（推測：Jetpack Compose 或 XML）
- （推測）`app/src/main/.../ui/`：畫面結構、控制列、資訊列、按鈕樣式、spacing
- （推測）主預覽畫面：PreviewView 容器、Overlay（偵測框/資訊提示）圖層順序與透明度
- （推測）狀態管理：模式切換 state（Live/Static/Debug）、按鈕 enable/disable、loading/暫停狀態

### 相機/影像管線耦合點（高風險）
- Preview 顯示比例（aspect ratio）、旋轉/鏡像、裁切座標（會影響 overlay 對齊）
- 模式切換：重新 bind use cases、重設 zoom/exposure、控制項可用性（尤其曾出現 CameraControl not available 類型問題的專案脈絡）

### 功能層（UI/UX 具體項目範圍）
- 視覺規範：字級、色彩、陰影、圓角、對比、暗色模式（若有）
- 操作動線：模式切換入口、常用控制（拍照/錄影/暫停/重置/縮放）
- 資訊呈現：FPS、解析度、模型狀態、推論延遲（如有 debug overlay）

---

## 4. 風險清單（Risks：含相機管線/模式切換/效能回歸）
1. **相機預覽與 Overlay 對齊風險**
   - UI 容器 padding/scale 調整可能造成 PreviewView 顯示區域改變，導致偵測框、點位或裁切區域錯位。
2. **模式切換 UI state 不一致**
   - Live/Static/Debug 切換時，控制項狀態（enabled、selected、顯示/隱藏）未正確重設，導致誤觸或錯誤操作序列。
3. **效能回歸（FPS/CPU/GPU/記憶體）**
   - 過多透明 overlay、陰影、模糊、頻繁重組（Compose recomposition）或過度 invalidate（View）可能導致掉幀，影響即時推論與預覽流暢度。
4. **相機控制可用性/時序問題被 UI 觸發放大**
   - UI 調整若引入更頻繁的 zoom/exposure/torch 操作，可能在 rebind 或切換過程更容易觸發 CameraControl not available 等時序問題（尤其實機更常見）。
5. **裝置差異**
   - 不同螢幕比例、瀏海/挖孔、安全區（insets）處理不一致，造成關鍵控制被遮擋或可點區域縮小。
6. **可回滾性下降**
   - UI/UX 若與相機管線邏輯混在同一批提交，回歸時難以回滾或二分定位。

---

## 5. 建議方案（Plan A / Plan B：最小差異優先）

### Plan A（最小差異、先整合到計畫文件 + 小步 UI 視覺規範落地）
1. **先在 `02-smart_camera_app_development_plan_v_2.md` 增補「UI/UX 視覺優化」章節**
   - 定義：目標、非目標、優先級（P0/P1/P2）、驗收標準（Definition of Done）
2. **建立 UI 視覺規範清單（不先大改畫面結構）**
   - 色彩/字級/間距/按鈕尺寸（至少符合可點區域）
   - overlay 層級規則（Preview 最底、AI overlay 次之、操作列最上）
3. **針對高風險耦合點採「只調樣式不動座標系」策略**
   - 避免改 PreviewView 的 scaleType/constraint 方式
   - overlay 僅調整透明度、線寬、文字背景，不改座標換算流程
4. **以 atomic commits 分離：文件 → UI style → 行為調整（若需要）**

### Plan B（需要較大調整時的替代方案：先做 UI Layer 抽離/狀態一致性）
1. 若現況 UI 與相機 state 強耦合，先做「UI 狀態機」或「單一 state source」
   - 模式切換流程定義：Entering/Active/Exiting/Unavailable
2. Overlay 渲染與控制列更新節流（throttle）
   - 避免每幀更新大量 UI 元件（特別是 debug 資訊）
3. 將「視覺效果」延後，優先確保模式切換穩定與效能指標不退步

---

## 6. 驗證方式（How to verify：模擬器/實機/效能指標）
### 功能與 UI 驗證
- 模擬器：
  - 檢查多解析度螢幕（16:9、20:9）下控制列是否遮擋預覽
  - 檢查深色/淺色背景對比（若支援）
- 實機（必要，尤其 CameraX + 模式切換）：
  - Live ↔ Static ↔ Debug（若有）來回切換至少 30 次，確認：
    - 控制項狀態一致（不殘留、不錯亂）
    - preview 不黑屏、不凍結
    - zoom/torch 等控制在切換後可用
  - 不同廠牌/晶片（若可）：至少 1 台中階機 + 1 台旗艦機

### 效能指標（至少擇一可量測方式）
- FPS：預覽 FPS（或 frame processing rate）
- CPU / GPU：Android Studio Profiler 觀察尖峰與平均（切換模式前後）
- 記憶體：切換模式後是否有持續成長（疑似 leak）
- UI jank：Frame drops（可用 `adb shell dumpsys gfxinfo` 或 profile）

---

## 7. 回滾策略（Rollback）
- 以提交粒度保證可回退：
  1) 先提交「計畫文件增補」  
  2) 再提交「純視覺樣式（theme/spacing/alpha）」  
  3) 最後才提交「互動/狀態/模式切換 UI 行為」  
- 若出現實機掉幀或模式切換不穩：
  - 優先回滾「互動/狀態行為」提交
  - 保留文件與低風險樣式（若已驗證不影響管線）
- 保留 UI/UX 改動 feature flag（若專案已有設定系統，推測）：可在 debug build 開關新 UI

---

## 8. 是否需要 Level 2 可行性檢視（Yes/No + 理由）
**Yes。**  
理由：
- UI/UX 優化在 CameraX 即時預覽專案中通常會碰到 **PreviewView 佈局、overlay 對齊、模式切換 state**，屬於 checklist 中的高風險條件（CameraX / 模式切換 / 效能）。
- 即使目標是「視覺優化」，一旦牽涉到 overlay 層級、透明效果、重組頻率，就可能造成 **FPS/功耗回歸**，需要在進入實作前由 Claude Code 依 repo 現況判斷最佳切入點與最小 diff 路徑。
