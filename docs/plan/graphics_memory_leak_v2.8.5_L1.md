<!-- generated: 2026-01-10 17:01:00 -->

# Level 1 規劃摘要：graphics_memory_leak_v2.8.5

## 1. 目標（Goal）
- 釐清並定位 v2.8.5 測試中 **Graphics 記憶體由 116MB 成長至 223MB（+91%）** 的原因。
- 確認是否為 **ML Kit `InputImage` 生命週期 / CameraX `ImageProxy` 關閉 / Compose Overlay 的 texture/bitmap** 未釋放所致。
- 產出可落地的修正方向（以**最小差異**為優先），並建立可重複的驗證流程與指標。

---

## 2. 背景與問題（Why / What）
- 目前觀察：v2.8.5 在測試期間 **Graphics memory 顯著成長**（116MB → 223MB）。
- 初步懷疑來源（待證實）：
  1. **ML Kit 分析鏈**：`InputImage` 或其底層 buffer/bitmap 轉換導致 GPU/Graphic buffer 累積（或 callback/Task 未完成導致保留）。
  2. **Compose overlay / UI layer**：即時疊圖（例如 Canvas、ImageBitmap、Bitmap、AndroidView/TextureView）可能在每幀生成新 texture 或 bitmap，舊的未釋放或被快取持有。
  3. **CameraX pipeline**：`ImageAnalysis` 的 `ImageProxy` 若未在所有路徑 `close()`，可能導致 buffer queue 堆積，進一步反映到 Graphics/Gralloc 記憶體。
- 需要優先調查的元件（使用者點名）：
  - `SmartCameraAnalyzer`
  - `LiveCameraSource`
  - `CameraViewModel`

---

## 3. 影響範圍（Scope：模組/檔案/功能層級；若未知請以推測標註）
### 模組/檔案（已知）
- `app/src/main/java/.../data/source/camera/SmartCameraAnalyzer.kt`
  - 與 ML Kit 分析節流、`ImageProxy` → `InputImage`/Bitmap 轉換、協程/Task callback 生命週期高度相關。
- `app/src/main/java/.../data/source/camera/LiveCameraSource.kt`
  - CameraX `Preview` / `ImageAnalysis` use case 建立與 SurfaceProvider（SurfaceTexture/Surface）管理相關。
- `app/src/main/java/.../presentation/camera/CameraViewModel.kt`
  - Bitmap/overlay 狀態傳遞、UI 更新頻率、快取策略（持有最新 frame、歷史 frame 或 debug bitmap）可能造成 Graphics/Bitmap 常駐。

### 功能層級（推測）
- Live camera 預覽 + AI 即時分析流程
- Compose overlay（偵測框/提示/疊圖）
- 模式切換（例如進出相機頁、前後景、切換鏡頭、重啟分析）時的資源釋放

---

## 4. 風險清單（Risks：含相機管線/模式切換/效能回歸）
1. **相機管線阻塞風險**：若 `ImageProxy.close()` 有漏（含例外路徑），會導致 `ImageAnalysis` 堆積、延遲上升、甚至黑畫面/卡死。
2. **模式切換資源洩漏**：進出頁面、切換鏡頭、重啟 use case 時，舊的 Surface/Texture/Bitmap 若未解除引用，Graphics memory 會階梯式上升。
3. **效能回歸（CPU/GPU）**：修正若改成每幀 copy/轉換，可能降低 FPS 或提高耗電；或因強制回收/縮放造成額外成本。
4. **修正不精準導致誤傷**：為止血而粗暴降低解析度/頻率（如 v2.8.5 Pixel 9a）可能掩蓋根因，讓其他機型/版本繼續累積問題。
5. **Compose 快取/重組行為不可控**：不當的 state 持有 Bitmap 或頻繁建立 `ImageBitmap`/`Painter` 可能引發不可預期的 texture churn。

---

## 5. 建議方案（Plan A / Plan B：最小差異優先）
### Plan A（最小差異：先證據化 + 快速止漏點排查）
目標：不用大改架構，先用「必關閉/必釋放/必取消」檢查清單把最常見的漏點抓出來。

1. **SmartCameraAnalyzer：強制保證 close 與取消**
   - 確認 `analyze(imageProxy)` 內所有路徑（成功/失敗/節流跳過/例外）都會 `imageProxy.close()`。
   - 若使用 ML Kit `process()` 非同步 callback：
     - 在 callback 完成後不再持有 `imageProxy`/其 buffer（避免 closure 捕獲）。
     - 若存在「同時多個 in-flight」分析，加入「只允許單一 in-flight」或明確取消舊任務（避免 backlog 造成 buffer/texture 累積）。
2. **InputImage/Bitmap 建立策略盤點**
   - 盤點是否每幀都 `Bitmap.createBitmap` / `copyPixelsFromBuffer` / `asImageBitmap()`。
   - 若有「每幀 new Bitmap」：改為重用固定 buffer（若可行）或至少確保舊 bitmap 不被 UI state/queue 持有。
3. **LiveCameraSource：Surface/SurfaceTexture 生命週期檢查**
   - 確認 CameraX unbind/rebind 時，舊的 `Surface`、`SurfaceTexture`、自訂 SurfaceProvider 是否有釋放/解除引用（避免 GraphicBuffer 留在 producer/consumer queue）。
4. **CameraViewModel：UI state 不持有歷史 bitmap**
   - 確認 ViewModel 只保留「最新一張」且替換時不保留舊引用（例如 list/queue/debug history）。
   - 若 overlay 透過 Compose 記住 bitmap（remember）或 snapshotFlow 造成快取，需避免把大 bitmap 放入長生命週期 state。

> 產出：一份「生命週期與引用鏈」清單（誰建立、誰持有、何時釋放），並用 profiler 佐證哪一段造成 Graphics memory 成長。

### Plan B（若 Plan A 找不到明確漏點：偏架構/管線調整但仍控範圍）
1. **降低 overlay 貼圖成本**
   - 將 overlay 從「貼 bitmap/texture」改為「純 Compose Canvas 畫框/文字」（不依賴每幀 bitmap）。（推測）
2. **改用更可控的幀輸入**
   - 若目前走 Bitmap 分析：評估改為 ML Kit 支援的 `MediaImage` / YUV 直入（視現行實作而定），減少 bitmap->texture 的路徑。（推測）
3. **集中資源池（bitmap pool / buffer pool）**
   - 針對固定解析度建立可重用 bitmap pool，避免每幀 allocate/free 造成 Graphics/Native 碎片化。（推測）

---

## 6. 驗證方式（How to verify：模擬器/實機/效能指標）
### 測試矩陣
- **實機優先**：Pixel 9a（已觀察到異常）、另外選 1 台非 Pixel（Sony/其他）作為對照。
- **模擬器**：Android 16 emulator 作 baseline（但不得作為唯一判斷）。

### 測試步驟（建議固定腳本/流程）
1. 進入相機頁 → 開啟 live preview + overlay + analysis（若有開關則測開/關各一次）
2. 持續運行 10–15 分鐘（不切頁 vs 每 1 分鐘切換前後景/切頁一次，各跑一輪）
3. 記錄：
   - Android Studio Profiler：
     - **Graphics memory**（主指標）
     - Native / Java heap（輔助判讀）
   - `dumpsys meminfo <package>`（定期取樣）
   - 若可：Perfetto / GPU memory 相關 track（視環境）
4. 通過/失敗判準（可先用暫定門檻）
   - 10 分鐘內 Graphics memory 成長 **不應持續線性上升**；允許初期 warm-up，但需在某區間趨於平穩。
   - 切頁/背景回前景後，Graphics memory 不應階梯式累積（可回落或維持）。

---

## 7. 回滾策略（Rollback）
- 所有修正以 feature-flag 或最小提交切分：
  1. 先上「close/釋放/取消保證」類改動（低風險、可單獨回滾）
  2. 再上「重用 buffer / pool」類改動（中風險、需回滾開關）
  3. overlay 路徑調整獨立提交（可快速 revert）
- 若修正導致：
  - 分析結果延遲、預覽卡頓、相機黑畫面  
  立即 revert 最近一個提交（保持每個 commit 可單獨回退）。

---

## 8. 是否需要 Level 2 可行性檢視（Yes/No + 理由）
**Yes。**

理由：
- 問題涉及 **CameraX Preview/ImageAnalysis、ML Kit、Graphics memory**，屬於高風險效能/資源議題。
- 已牽涉多檔案（`SmartCameraAnalyzer` / `LiveCameraSource` / `CameraViewModel`），且可能牽動相機管線與 UI overlay 的生命週期，符合 Level 2 自動升級條件。
- 需要 Claude Code/工程側進一步讀 repo 實作細節，確認：
  - `ImageProxy.close()` 是否全路徑覆蓋
  - `InputImage` 建立方式與 callback 引用鏈
  - Compose overlay 是否每幀生成新 texture/bitmap、以及 state 持有策略  
  才能提出可行且最小 diff 的修正方案。
