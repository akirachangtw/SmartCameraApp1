<!-- generated: 2026-01-05 11:20:21 -->

## 1. 可行性結論（可行 / 不可行 / 需調整）
**需調整（可行但必須先做 repo 對齊與整合點確認）**。  
原因：本任務本質是「文件/規劃整合 + UI/UX 視覺優化改進」，通常不涉及 CameraX pipeline 的高風險程式改動，因此**技術上可行**；但你指定要整合進：
- `docs/plan/smart_camera_app_with_uiux_plan_L1.md`
- `plans/ui_ux_design_proposals.md`
- `plans/02-smart_camera_app_development_plan_v_2.md`

需要先由 Claude Code 確認 repo 內：
1) `plans/` 與 `docs/plan/` 的 SSOT（單一真實來源）策略；  
2) 既有 plan 文件章節結構是否一致、是否有重複/衝突；  
3) 是否已有對應的 UI 架構（Compose/View、Navigation、Design System）章節可以承接，避免「提案寫了但落不了地」。

---

## 2. 關鍵假設（Assumptions：哪些需由 repo 或 log 來驗證）
1. **文件存在性與路徑一致**：  
   - `docs/plan/smart_camera_app_with_uiux_plan_L1.md`（目前 git status 顯示新增）  
   - `plans/ui_ux_design_proposals.md`、`plans/02-smart_camera_app_development_plan_v_2.md`（需確認 repo 內實際存在、檔名大小寫、是否在不同資料夾）
2. **目前 UI 技術棧**：專案是 **Jetpack Compose** 或 **XML/View**（需從 `app/build.gradle`, `MainActivity`, `ui/` 結構確認）。
3. **相機頁面資訊架構已定義**：是否已有 Live/Static/Debug 模式切換、頂部/底部控制列、權限導引頁等既定流程（需從 navigation 或 activity/fragment 流程確認）。
4. **現有 AI/分析結果的 UI 呈現方式**：有無 overlay layer、bounding box、label list、FPS 指標等（需從 `ImageAnalysis` consumer/renderer 相關程式確認）。
5. **效能/幀率預算**：UI/UX 視覺優化是否允許額外 draw/compose cost（需從現有 perf 記錄或最低裝置 target 推斷）。
6. **設計資產策略**：是否已導入 Material3、ColorScheme、Typography、icon pack、Lottie 等（需查 dependencies 與 `theme`）。

---

## 3. 高風險點（High-risk areas：CameraX Preview/ImageAnalysis/模式切換/Thread/權限）
> 文件整合本身風險低；真正的風險在「UI/UX 改進若落實成 UI code」時會觸發下列點。此 L2 先把風險預先框住，避免後續 UI 改動踩到 CameraX/效能雷。

1. **CameraX Preview + Overlay 疊圖**
   - Compose Canvas / Android View overlay 疊加在 PreviewView 上，可能造成額外 GPU overdraw、或座標映射錯誤（旋轉、鏡像、裁切）。
2. **ImageAnalysis 與 UI thread 邊界**
   - 分析結果更新頻率太高，若直接推 UI state（Compose recomposition / RecyclerView notify）可能造成掉幀。
3. **模式切換（Live / Static / Debug）**
   - 切換時 camera use cases bind/unbind 的生命週期若處理不當，會出現黑畫面、卡住、或 `CameraControl not available` 等 race condition（你前面任務背景也提過類似情境）。
4. **權限流程與錯誤狀態 UX**
   - 權限被拒、僅允許一次、背景回前景、裝置不支援等狀態若 UI 未完整覆蓋，容易造成「看起來像壞掉」的體驗。
5. **Thread/Coroutine Scope**
   - UI 狀態容器（ViewModel/StateFlow）若與 camera pipeline scope 混用，容易造成 memory leak 或重複訂閱。
6. **不同裝置差異**
   - 長寬比、notch、旋轉、低階機型效能：UI 動效與 overlay 可能在實機翻車。

---

## 4. 最小差異建議（Minimal diff approach：步驟化）
目標：先把「UI/UX 視覺優化改進」**穩定落在文件**，並建立可落地的工程切入點；避免一開始就大改 UI 架構。

**Step 0｜先定 SSOT 與整合策略（只動文件，不動程式）**
1. 決定主文件：建議以 `plans/02-smart_camera_app_development_plan_v_2.md` 作為「開發主線」、`plans/ui_ux_design_proposals.md` 作為「提案庫」、`docs/plan/smart_camera_app_with_uiux_plan_L1.md` 作為「本次整合摘要/執行清單」。
2. 在三份文件間建立明確 cross-link（章節錨點/引用）。

**Step 1｜把 UI/UX 改進拆成可交付的 Epic → Story → Task**
- 例如：資訊架構、相機頁控制列、模式切換、狀態頁（權限/錯誤/載入）、AI overlay 呈現、Debug HUD、動效與微互動、無障礙與國際化。

**Step 2｜建立「UI 不影響 camera pipeline」的落地規範**
- 文件先定：overlay 更新節流策略、state 更新頻率、分析結果 buffer、UI thread 安全原則。

**Step 3｜若要進入實作，採 Feature Flag / 分階段切換**
- 先做 theme/design system（低風險）
- 再做 UI 元件替換（控制列/按鈕）
- 最後才動到 Preview overlay 與模式切換行為

**Step 4｜每一個 UI/UX 改動都要求可驗證**
- emulator + 實機（至少一台中低階）  
- 旋轉、背景切回前景、權限拒絕等情境

---

## 5. 需要檢視的檔案/關鍵函式清單（請用待辦清單列出）
> Claude Code 請先用檢視方式確認現況（不要先改）。

### 文件與規劃（本任務直接相關）
- [ ] `docs/plan/smart_camera_app_with_uiux_plan_L1.md`（內容章節、是否已包含整合點）
- [ ] `plans/ui_ux_design_proposals.md`（提案範圍、是否可拆成可實作項）
- [ ] `plans/02-smart_camera_app_development_plan_v_2.md`（主線章節結構、可插入 UI/UX 的位置）
- [ ] 專案是否已有其他 UI/UX 或 design system 文件（搜尋 `uiux`, `design`, `theme`, `material`）

### UI 架構（用來判定落地方式）
- [ ] `app/build.gradle` / `build.gradle.kts`（Compose / Material3 / Navigation 依賴）
- [ ] `MainActivity` / `Application`（入口、theme 設定）
- [ ] `ui/`、`presentation/`、`screen/` 或 `feature/` 目錄結構（是否模組化）
- [ ] Navigation：`NavHost` / `navigation.xml` / router 類（頁面切換與模式切換入口）

### CameraX 與模式切換（避免 UI/UX 改動踩雷）
- [ ] Camera 相關封裝：`CameraProvider` 綁定處（bind/unbind use cases）
- [ ] `PreviewView` 使用處、overlay 疊圖 layer（自訂 view / compose canvas）
- [ ] `ImageAnalysis` analyzer 設定與 callback（分析結果如何傳到 UI）
- [ ] 模式切換流程：Live/Static/Debug 的 state machine（若存在）
- [ ] 權限處理：request / rationale / denied flow

### 效能與 debug（UI/UX 優化常用）
- [ ] FPS/延遲/記憶體監控是否已有（Debug HUD、log）
- [ ] 是否已有節流/取樣策略（例如每 N 幀更新一次 UI）

---

## 6. 驗證清單（Build/Test/Device/Perf）
### Build
- [ ] `./gradlew assembleDebug`（或對應 variant）可過
- [ ] 若有 Compose：確保 R8/Proguard（release 先不要求，但需確認依賴合理）

### Test（偏情境驗證）
- [ ] 啟動 App → 首次權限允許 → 進入相機頁面（UI 正常）
- [ ] 權限拒絕（含「不再詢問」）→ UI 引導頁/設定導引正確
- [ ] Live/Static/Debug 來回切換 20 次（若存在此模式）→ 不黑屏、不 crash
- [ ] 背景切換：home → 回前景（camera 重新綁定、UI state 不亂）
- [ ] 旋轉（或模擬旋轉）→ overlay/控制列位置正確

### Device
- [ ] 至少 1 台實機（最好含中低階）驗證 UI 動效與 overlay
- [ ] 不同長寬比（18:9 / 20:9）或有瀏海挖孔的裝置（safe area）

### Perf
- [ ] 觀察 Preview FPS（或體感）在 UI 新增 overlay/動效後無明顯掉幀
- [ ] 分析結果推 UI 有節流（例如 10Hz）而不是每幀更新

---

## 7. 回滾與降風險策略（Rollback/Feature flag）
1. **Feature Flag（建議）**
   - `uiUxRevampEnabled`：預設關閉或僅 debug 開啟  
   - 可讓新 UI/UX 與舊版並存，避免一次性切換造成大面積回歸。
2. **分層回滾**
   - Theme/樣式（可獨立回退）
   - 控制列元件（可獨立回退）
   - Overlay 呈現（最高風險，最後上線、可單獨關閉）
3. **節流與降載策略**
   - Overlay 更新頻率限制、只在結果變化時更新
   - Debug 元件僅 debug build 顯示
4. **模式切換保護**
   - 切換時加入 state gate（例如 camera ready 才允許操作）
   - 對 `bind/unbind` 加入序列化（mutex/single-thread executor）避免 race
5. **回滾標準**
   - 一旦出現：黑屏、相機無法恢復、明顯掉幀、實機專屬 bug → 立即關閉新 UI/UX flag，先保留舊流程。

--- 

> 交付給 Claude Code 的下一步建議：先檢視上述「需要檢視的檔案清單」，確認 UI 技術棧與 Camera pipeline 的實際結構後，再把 `ui_ux_design_proposals.md` 的內容以「可落地的最小任務切片」方式寫入 `02-smart_camera_app_development_plan_v_2.md`（含里程碑、驗證與回滾）。
