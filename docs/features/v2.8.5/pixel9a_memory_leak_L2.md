<!-- generated: 2026-01-09 11:52:21 -->

# Level 2 可行性檢視備忘錄（給 Claude Code 檢視 repo 用）
主題：**pixel9a_memory_leak（Pixel 9a / Android 16 / v2.8.4 記憶體線性成長）**

---

## 1. 可行性結論（可行 / 不可行 / 需調整）
**需調整（可行，但必須先完成「可重現 + 可歸因」的檢視與量測補強，才能進入實作修正）。**

理由：
- 目前現象是 **Pixel 9a only**，且 emulator + Sony 正常，表示很可能牽涉 **裝置特異的 buffer queue / HAL / GPU graphic buffer 行為**，或 App 在該機型走到不同設定/格式路徑。
- 在未確認成長屬於 **App 端可控洩漏（未 close / 持有）** 或 **平台端延遲回收/快取（但會 plateau）** 前，直接改 pipeline 風險高、也難驗證「真的修好」。
- 透過 repo 檢視與最小化插樁（logging + 嚴格 close 保證 + 可切換的降級旗標）可以在 1–2 個迭代內把問題切成：  
  (A) App bug 可修、(B) 裝置特異需規避、(C) 量測誤判。

---

## 2. 關鍵假設（Assumptions：哪些需由 repo 或 log 來驗證）
需由 Claude Code 透過 repo 與實機 log/量測確認：

1. **ImageAnalysis.Analyzer 是否保證每張 frame 的 `ImageProxy.close()` 在所有分支都執行**  
   - 包含：early return、exception、推論取消、背壓丟棄、模式切換期間。
2. **是否存在多重 Analyzer / Executor 未釋放**  
   - 例如重綁 use case 後舊 executor thread 仍持有 queue/bitmap/tensor。
3. **是否有 Bitmap/ByteBuffer/Tensor/Mat 每幀 new 且未重用，並被某處 cache/集合持有**  
   - 尤其是 debug overlay、結果列表、最近幀保留、追蹤器 history。
4. **Preview/Surface/GL 相關資源是否在 mode switch / lifecycle 中確實 teardown**（若有 GPU filter 或 SurfaceTexture）
5. **CameraX 設定在 Pixel 9a 上是否不同**  
   - 分析解析度、format (YUV_420_888/RGBA)、targetRotation、stabilization、vendor extension、dynamic range 等是否依裝置分岔。
6. **量測指標是否一致**  
   - 目前 +210MB/10min 是以 PSS/RSS/Native heap？需確認是否包含 GraphicBuffer/ION（meminfo 的 Graphics/GL/Other dev 項）。

---

## 3. 高風險點（High-risk areas：CameraX Preview/ImageAnalysis/模式切換/Thread/權限）
1. **CameraX ImageAnalysis 背壓與 close 行為（最高風險）**
   - `STRATEGY_KEEP_ONLY_LATEST` 下若 Analyzer 內阻塞/丟幀處理不當，Pixel 9a 可能更容易累積 native buffer。
2. **模式切換 / rebind use cases 的競態**
   - `unbindAll()` 與 analyzer 執行中並行，造成 image 未 close 或舊 pipeline 持有 Surface。
3. **Executor / Coroutine scope 生命週期**
   - Analyzer 若用自建 thread pool、或 coroutine scope 綁錯 lifecycle，可能造成長期持有大物件（tensor/bitmap）。
4. **Preview/GL/SurfaceTexture（若存在）**
   - GraphicBuffer 不回收常見於 Surface/GL context 未正確釋放或被多份引用。
5. **權限與相機重連**
   - Pixel 系列在 background/foreground、screen off/on 的 camera reopen 路徑可能更常發生；若重連邏輯沒清乾淨，會「每次重連多一份資源」。
6. **Debug/Overlay/錄影共用管線（推測）**
   - 開 debug overlay 可能額外做 RGBA conversion/bitmap render，導致 native heap/graphics 增長。

---

## 4. 最小差異建議（Minimal diff approach：步驟化）
目標：**先歸因，再止血**；所有改動以 **可開關**、**不改架構** 為原則。

### Step 0｜鎖定測試條件（先不改 code）
- 確認 `tools/pixel_memory_test.sh/.ps1` 能在 Pixel 9a / Sony / emulator 用同一套參數跑 10–20 分鐘。
- 固定：AI on/off、解析度（或 targetResolution）、FPS（若可控）、螢幕常亮、相同場景。

### Step 1｜在 Analyzer 入口做「必關閉」保證（最小 code diff）
- 將 Analyzer 主體改成：
  - `try { ... } catch { log } finally { image.close() }`
- 額外加上「一次性防呆」：若 `image.image == null` 或已關閉狀態（如可判斷）也要走 finally。
- 目的：先排除最常見的 App 端 buffer 洩漏。

### Step 2｜加輕量插樁（僅 log + 計數器，不引入重工具）
- 每 N 幀輸出：
  - analyzer 被呼叫幀數、成功處理幀數、丟棄幀數、平均耗時、目前 active use case 狀態
  - 目前 native heap / java heap（可用 `Debug.getNativeHeapAllocatedSize()` 等）作為趨勢參考
- 目的：判斷是否存在「處理變慢→背壓→buffer 異常」或「重綁次數增加→資源累積」。

### Step 3｜加 Pixel 9a 專用降級旗標（只動設定，不動核心流程）
以 feature flag（build config / remote config / runtime device check）包住：
- ImageAnalysis：
  - 降低 target resolution（例如從 1280x720 → 640x480 或依 repo 現況）
  - 明確指定 backpressure strategy（必要時改成 `BLOCK_PRODUCER` 做對照測試）
  - 降低 analyzer 執行頻率（例如每 2~3 幀做一次推論；仍需 close 每幀）
- 若有 RGBA/Bitmap conversion：Pixel 9a 先關閉或改走較保守路徑（例如只取 Y plane）

> 這一步的目的不是「永久解」，而是用最小差異快速回答：**成長是否與解析度/格式/背壓/推論頻率高度相關**。

### Step 4｜如果確認是平台/graphics buffer 類（再做較大改動）
- 才考慮：
  - 在 mode switch / onStop 強制 teardown：停 analyzer、shutdown executor、unbind use cases、釋放 GL
  - 或改用不同輸入管線（例如避免某些 conversion path）

---

## 5. 需要檢視的檔案/關鍵函式清單（待辦清單）
> Claude Code 請用 ripgrep/IDE 全域搜尋，找出實際檔名與位置；以下是「必查關鍵點」。

- [ ] CameraX 綁定入口：`ProcessCameraProvider.getInstance()` 後的初始化流程  
  - [ ] `bindToLifecycle(...)` 的呼叫點（可能在 CameraFragment/CameraViewModel/CameraManager）
  - [ ] `unbindAll()` / `unbind(useCase)` 是否在模式切換與 onStop/onDestroy 有被呼叫
- [ ] `ImageAnalysis.Builder` 設定處  
  - [ ] `setBackpressureStrategy(...)`
  - [ ] `setTargetResolution(...)` / `setTargetAspectRatio(...)`
  - [ ] `setOutputImageFormat(...)`（若使用 RGBA）
  - [ ] `setTargetRotation(...)`
- [ ] Analyzer 實作（最重要）
  - [ ] `ImageAnalysis.Analyzer#analyze(image: ImageProxy)`：是否 **100% finally close**
  - [ ] 是否把 `ImageProxy`、`Image`、`ByteBuffer` 存到全域/queue/cache
  - [ ] 是否在 analyzer 內 `Bitmap.createBitmap` / `ByteBuffer.allocateDirect` / `TensorImage` 每幀 new
  - [ ] 推論失敗/取消/timeout 路徑是否漏 close（常見在 early return）
- [ ] 推論管線（TFLite/MediaPipe/自研 wrapper）
  - [ ] interpreter / graph 是否重建過於頻繁
  - [ ] input/output buffer 是否有 pool、是否被靜態 singleton 持有且持續成長
- [ ] Preview / GL / Overlay（若有）
  - [ ] `PreviewView` 使用模式（TextureView/SurfaceView）
  - [ ] 自定 renderer / OpenGL / SurfaceTexture 的建立與釋放點
- [ ] 模式切換 / 前後台
  - [ ] `onStart/onStop/onDestroy` 或 `repeatOnLifecycle` 區塊
  - [ ] 切換模式（Live ↔ Static/Debug）的狀態機：是否重複建立 use case、但沒 teardown 舊的
- [ ] 測試工具與文件
  - [ ] `tools/pixel_memory_test.sh` / `tools/pixel_memory_test.ps1`：蒐集哪些指標（meminfo 欄位、頻率、輸出格式）
  - [ ] `docs/V2.8.4_MEMORY_TEST_REPORT_THREE_PLATFORMS.md`：目前結論是否缺少 native/graphics breakdown

---

## 6. 驗證清單（Build/Test/Device/Perf）
**Build**
- [ ] `./gradlew clean :app:assemble<variant>`（至少與 v2.8.4 同 variant）
- [ ] 確認未提交 `.claude/settings.local.json`、`.idea/deploymentTargetSelector.xml`（保持工作區乾淨）

**Test（自動化/半自動）**
- [ ] 以既有 script 在三平台各跑 2 次（避免偶發）
  - [ ] Pixel 9a：10/20 分鐘
  - [ ] Sony：10/20 分鐘
  - [ ] Emulator：10/20 分鐘
- [ ] 測試情境矩陣（至少）
  - [ ] Preview + AI on
  - [ ] Preview only（AI off）
  - [ ] 模式切換壓測（若有該功能）：每 10–30 秒切一次，跑 10 分鐘
  - [ ] 前後台切換：每 30 秒 Home/background 再回前台（如產品允許）

**Perf / Memory 指標（必收）**
- [ ] `dumpsys meminfo <package>`：記錄
  - [ ] Java Heap / Native Heap
  - [ ] Graphics / EGL mtrack / GL mtrack（若有）
  - [ ] TOTAL PSS、TOTAL RSS
- [ ] 以同樣 sampling interval（例如每 30 秒）輸出到檔案，畫趨勢
- [ ] 成功門檻（建議先用「趨勢」而非單點）
  - [ ] Pixel 9a：10 分鐘內不應持續線性成長；若仍 > +100MB/10min 視為未止血
  - [ ] 三平台差異：Pixel 9a 不應顯著偏離（除非已啟用 Pixel 專用降級旗標）

---

## 7. 回滾與降風險策略（Rollback/Feature flag）
1. **所有 Pixel 9a 專用行為必須包 feature flag**
   - runtime 判斷（Build fingerprint / model）+ 可由遠端設定或 build 開關切回
2. **改動分兩個 atomic commits**
   - Commit A：`ImageProxy.close()` finally 保證 + 基礎插樁（理論上對所有機型風險最低）
   - Commit B：Pixel 9a 降級策略（解析度/背壓/頻率/關閉某特效），可單獨回退
3. **避免一次引入大型 refactor（例如全面 object pool / 重寫 pipeline）**
   - 若需要 object pool，先做「局部重用」且可關閉，並用壓測證明不造成畫面/延遲回歸
4. **保留原路徑可切換**
   - 例如 backpressure strategy、target resolution、是否做 RGBA conversion 都需可切回預設
5. **若判定為平台問題**
   - 以「裝置相依相機設定檔」長期維護  
   - 同步準備：可重現 script + meminfo/趨勢圖 + 版本/設備資訊，上報 CameraX/IssueTracker

---
