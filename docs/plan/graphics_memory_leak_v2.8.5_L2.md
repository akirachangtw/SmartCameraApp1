<!-- generated: 2026-01-10 17:02:22 -->

# Level 2 可行性檢視備忘錄（給 Claude Code 檢視 repo 用）
主題：**graphics_memory_leak_v2.8.5**  
版本背景：v2.8.5 針對 Pixel 9a 已做「降解析度 + 降分析頻率」止血，但 **Graphics memory 仍在測試中由 116MB → 223MB（+91%）**，需定位根因並以最小差異修正。

---

## 1. 可行性結論（可行 / 不可行 / 需調整）
**需調整（Plan A 可行但需先補齊證據化與關鍵保護；Plan B 僅在證據指向 UI/texture churn 或 MLKit path 無法止漏時啟動）**

- **Plan A（最小差異止漏）可行性：高**  
  但必須由 repo 實作細節確認：`ImageProxy.close()` 是否「全路徑覆蓋」、ML Kit `process()` callback 是否持有 `ImageProxy`/buffer、是否存在多個 in-flight task，以及 Compose/UI state 是否持有 Bitmap/Texture 造成 Graphics 常駐。
- **Plan B（架構調整）可行性：中**  
  成本與回歸風險較高，適合在「Plan A 沒有找到 leak 或 leak 根因屬於 overlay/bitmap->texture churn」時作為第二階段。Plan B 必須先有 profiler/trace 證據，否則容易誤傷（降質掩蓋根因）。

---

## 2. 關鍵假設（Assumptions：哪些需由 repo 或 log 來驗證）
以下假設若不成立，Plan A 的修改點會不同，需先用 repo + log + profiler 證實：

1. **SmartCameraAnalyzer** 目前使用 CameraX `ImageAnalysis.Analyzer`，且在 `analyze(ImageProxy)` 內呼叫 ML Kit 非同步 `process()`。  
2. **ImageProxy 的關閉策略**：存在節流（throttle）/跳過 frame 的路徑；需確認這些路徑是否仍 `close()`。  
3. **InputImage 建立方式**：可能走 `InputImage.fromMediaImage(imageProxy.image, rotation)` 或 Bitmap 路徑（例如 `Bitmap.createBitmap` / `YUV->RGB` 後丟給 ML Kit）。不同路徑會影響 Graphics/Gralloc 壓力來源。  
4. **in-flight 數量**：ML Kit task 可能同時多筆未完成（尤其 throttle 不等於「只允許單一 in-flight」），導致 buffer queue 或中間產物累積。  
5. **LiveCameraSource 的 Preview SurfaceProvider** 為自訂（看 diff 註解），可能涉及 `SurfaceTexture/Surface` 重建/釋放不完整，導致 GraphicBuffer queue 階梯上升。  
6. **Compose overlay / UI** 可能每幀產生新的 `Bitmap`/`ImageBitmap`/texture（例如把 bitmap 放入 state，或每幀 `asImageBitmap()`），導致 Graphics memory 隨時間增加。  
7. Pixel 9a 的止血（480x480 + 2FPS）降低速率但不解決根因：表示 leak 可能是「每次分析/每次重組仍留一份」或「模式切換 Surface 未釋放」。

---

## 3. 高風險點（High-risk areas：CameraX Preview/ImageAnalysis/模式切換/Thread/權限）
1. **CameraX ImageAnalysis 管線阻塞**  
   - `ImageProxy.close()` 任何漏掉（含例外、早退、節流跳過）都會造成 pipeline 堆積，表現為延遲、黑畫面或 Graphics/Gralloc 逐步上升。
2. **ML Kit 非同步 callback 捕獲引用**  
   - callback/lambda 若捕獲 `imageProxy` 或其 `image/planes`，就算外部已 close，也可能保留引用直到 task 完成（甚至永遠不釋放若 task 泄漏/被 scope 持有）。
3. **多個 in-flight task（分析 backlog）**  
   - throttle 只降低進入頻率，但若每次 process 耗時 > throttle 間隔，仍可能累積多筆未完成 task，導致 native buffer / temp bitmap / internal GPU 資源累積。
4. **Preview 自訂 SurfaceProvider / SurfaceTexture 生命周期**  
   - rebind/unbind、進出頁、前後景、切鏡頭時，如果舊 SurfaceTexture 沒 release 或仍被某處持有，Graphics memory 會階梯式增加。
5. **Compose overlay texture churn**  
   - 每幀建立 `ImageBitmap`/`Painter` 或把大 bitmap 放在長生命週期 state（ViewModel/remember）可能導致 RenderThread / GPU cache 增長，Graphics memory 不易回落。
6. **Thread/Coroutine scope 未取消**  
   - Analyzer 使用 `SupervisorJob()`，若沒有在 stop/release 時 cancel，可能持續保留工作與資源引用鏈。
7. **權限/生命周期（Process lifecycle）**  
   - 若相機頁面退出但 use case 未確實 unbind，會持續持有 Surface/buffer（尤其背景回前景多次）。

---

## 4. 最小差異建議（Minimal diff approach：步驟化）
目標：先用最小變更做到「必關閉、必取消、必不持有」＋「可觀測」，快速縮小範圍。

### Step 0｜先補「可觀測」而非先大改
- 在 Analyzer、Camera bind/unbind、UI bitmap 更新處加上輕量 log/metric（可用 BuildConfig.DEBUG 守門）：
  - frameId、inFlightCount、process 耗時、close 是否被呼叫、bind/unbind 次數、SurfaceTexture hashCode。
- 在關鍵路徑加 `StrictMode`（debug only）或簡單計數器，用於判斷是否有未 close。

### Step 1｜SmartCameraAnalyzer：保證 `ImageProxy.close()` 全路徑覆蓋
- 寫法原則：**`analyze()` 內用 `try/finally` 或明確保證每個 return 前 close**。
- 若採 ML Kit 非同步：
  - **不要把 `imageProxy` 傳進 callback**（避免 capture）。  
  - 建議：先從 `imageProxy` 建 `InputImage`（或拷貝必要資料），然後**立刻 close**（若 ML Kit 路徑允許）；否則至少在 `addOnCompleteListener { imageProxy.close() }`，且要確保任何 failure/cancel 都會走到 complete。
- 加入「單一 in-flight」保護（最小差異版本）：
  - 使用 `AtomicBoolean processing`：若正在處理則直接 close 當前 frame 並 return。
  - 或 `Semaphore(1)` / `Mutex.tryLock()`：拿不到就 close 丟棄。
  - 這是為了避免 backlog（尤其 Pixel 裝置 MLKit 內部耗時/資源較高）。

### Step 2｜InputImage/Bitmap：限制每幀分配與避免 UI 持有多份
- 若目前是 Bitmap 路徑：
  - 優先檢查是否每幀 `Bitmap.createBitmap` + `asImageBitmap()` 丟到 state。
  - 最小差異：ViewModel/UI **只保留最新一張引用**，更新時不累積列表、不在 remember 內 cache 多份。
- 若可改成 `InputImage.fromMediaImage`（仍屬 Plan A 範圍內的小改）：
  - 先確認 rotation、format、與 ML Kit API 支援狀況；若可行，通常能減少 Bitmap/texture 中間產物。

### Step 3｜LiveCameraSource：SurfaceProvider / unbind / release 檢查
- 在 stop camera / onCleared / onDispose 時：
  - 明確 `cameraProvider.unbindAll()`
  - 釋放自訂 `Surface`、`SurfaceTexture.release()`（若是自己建立/持有）
  - 避免把 SurfaceTexture 存在 singleton 或 long-lived object（例如 ViewModel）
- 在重啟 use case 前先確保舊的 release。

### Step 4｜Compose overlay：避免「每幀新 texture」
- 最小差異方向：
  - overlay 改為「只畫框/文字」的 Canvas（若目前是把 bitmap 當背景再疊圖，先把疊圖資料與 bitmap 分離）。
  - 若 UI 必須顯示 bitmap：避免每幀 `ImageBitmap` 新建與長期 state 持有；改用單一 mutable holder（僅更新像素/引用）。

> **Plan B 觸發條件**（建議寫入文件/Issue）：  
> 若完成 Step 1~4 後 Graphics memory 仍線性上升，且 profiler 顯示成長主要在 RenderThread/GPU/Skia 或 GraphicBuffer（而非 ImageAnalysis backlog），才進 Plan B。

---

## 5. 需要檢視的檔案/關鍵函式清單（請用待辦清單列出）
### SmartCameraAnalyzer.kt
- [ ] `class SmartCameraAnalyzer : ImageAnalysis.Analyzer` 的 `analyze(imageProxy: ImageProxy)` 全內容  
- [ ] 節流/跳過 frame 的分支：是否仍 `imageProxy.close()`  
- [ ] ML Kit `process()` 建立與 callback：是否 capture `imageProxy` / `Image` / `planes`  
- [ ] in-flight 控制：是否可能同時多個 task 未完成  
- [ ] `analysisScope` 的生命週期：是否在 stop/release 時 cancel（誰呼叫？）  
- [ ] InputImage 建立方式：`fromMediaImage` vs `fromBitmap` vs 自製 YUV->RGB

### LiveCameraSource.kt
- [ ] 建立 Preview 的 `setSurfaceProvider(customSurfaceProvider)` 實作：customSurfaceProvider 如何產生/管理 `SurfaceTexture`、`Surface`  
- [ ] camera bind/unbind 流程：`bindToLifecycle` 前是否 unbindAll  
- [ ] 停止/釋放流程：是否 release SurfaceTexture / 清掉引用  
- [ ] 模式切換（前後鏡頭、進出頁面、背景回前景）是否會重建 use cases，舊資源是否留存  
- [ ] `ImageAnalysis` 設定：backpressure 策略已是 KEEP_ONLY_LATEST，但仍需 close 保證

### CameraViewModel.kt
- [ ] 是否持有 `Bitmap`/`ImageBitmap`/overlay 相關 state（StateFlow/MutableState）  
- [ ] 更新頻率：是否每幀更新整張 bitmap 進 state（導致 Compose 每幀新 texture）  
- [ ] 是否有 debug history、list、cache、或記錄前幾張 frame  
- [ ] onCleared：是否通知 LiveCameraSource stop/release、取消 analyzer scope

### Compose UI（若存在，雖未列在 diff 但必查）
- [ ] 相機頁 Composable：`remember` 是否持有 bitmap、`AndroidView(TextureView/SurfaceView)` 的 lifecycle  
- [ ] overlay 繪製：是否使用 `Image(bitmap.asImageBitmap())` 每幀重建  
- [ ] `DisposableEffect` / `LaunchedEffect` 是否在離開頁時確實停相機/停止分析

---

## 6. 驗證清單（Build/Test/Device/Perf）
### Build
- [ ] `./gradlew assembleDeviceDebug` / `assembleDeviceRelease` 可過  
- [ ] 開啟/關閉 Pixel 9a 特例（throttle/resolution）都可編譯、可執行

### Device（必要，模擬器僅輔助）
- [ ] Pixel 9a：10–15 分鐘不切頁測試（preview+analysis+overlay 全開）  
- [ ] Pixel 9a：每 60 秒切前後景/切頁一次，跑 10–15 分鐘  
- [ ] 對照機：Sony/其他一台跑同腳本

### Perf / Memory 指標
- [ ] Android Studio Profiler：Graphics memory 曲線是否「warm-up 後趨平」  
- [ ] `dumpsys meminfo <package>`：每 1 分鐘取樣，關注 Graphics / EGL mtrack / GL mtrack（依版本顯示）  
- [ ] 檢查 in-flight：log 顯示同時處理數應長期維持 0/1（若採單一 in-flight）  
- [ ] 檢查 close：log 計數（frames received == frames closed）  
- [ ] 若可：Perfetto（SurfaceFlinger / GraphicBuffer / GPU memory 相關 track）用來判斷是否 Surface/Buffer queue 累積

### Pass/Fail（暫定）
- [ ] 10 分鐘內 Graphics memory 不可持續線性上升；允許前 1–2 分鐘 warm-up，但之後需穩定在區間  
- [ ] 切頁/背景回前景後，Graphics memory 不應階梯式只升不降（至少要能回落或維持）

---

## 7. 回滾與降風險策略（Rollback/Feature flag）
1. **提交切分（atomic commits）**
   - Commit A：只加 log/metric（可隨時移除，風險最低）
   - Commit B：`ImageProxy.close()` 全路徑保證（低風險、最應先上）
   - Commit C：單一 in-flight gate（中低風險；可能影響分析頻率但可控）
   - Commit D：SurfaceTexture release/unbind 強化（中風險；需實機驗證黑畫面風險）
   - Commit E：Compose overlay 更新策略調整（中風險；可能影響 UI 行為）
2. **Feature flag / BuildConfig 守門**
   - [ ] `ENABLE_SINGLE_IN_FLIGHT_ANALYSIS`
   - [ ] `ENABLE_OVERLAY_BITMAP_PATH`（可切回舊 overlay）
   - [ ] Pixel 9a 特例維持獨立開關，避免影響其他機型
3. **回滾準則**
   - 出現：預覽黑畫面、分析完全不觸發、FPS 明顯掉、ANR/卡死  
     → 立刻 revert 最近一個 commit（保持每個 commit 可單獨回退）
4. **降風險操作**
   - [ ] 所有 release/unbind 改動先在「離開頁面/stop」時做，避免在 active preview 期間破壞 Surface
   - [ ] 單一 in-flight 先在 debug 開啟，確認不影響核心流程再進 release

--- 

> 交付給 Claude Code 的工作重點：  
> **先用 repo 查證** `ImageProxy.close()` 覆蓋率、ML Kit callback 是否 capture、SurfaceTexture 是否可被釋放、UI 是否每幀造新 texture；  
> 再按「Step 0→4」做最小 diff 修正，並用 Pixel 9a 實機腳本驗證 Graphics memory 是否趨平。
