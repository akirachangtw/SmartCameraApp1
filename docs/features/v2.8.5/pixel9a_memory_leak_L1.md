<!-- generated: 2026-01-09 11:51:02 -->

# Level 1 規劃摘要｜pixel9a_memory_leak

## 1. 目標（Goal）
- 釐清 **Pixel 9a（Android 16）在 v2.8.4 出現嚴重記憶體成長（約 +210MB / 10min）** 的根因（App/CameraX/驅動/硬體差異）。
- 建立可重現、可量測、可對照的平台測試流程與驗證資料，判定：
  - 是否為 App 端可修正的洩漏/持有問題
  - 或是 Pixel 9a 特定相機驅動/圖形管線/平台行為導致的不可控成長（需規避/降級/上報）
- 在不破壞其他裝置（模擬器、Sony 實機正常）前提下，提出 **最小差異** 的緩解方案，避免 v3.0 前阻塞發布。

---

## 2. 背景與問題（Why / What）
- **觀察：**
  - Pixel 9a 實機於 v2.8.4 出現嚴重記憶體成長（+210MB/10min）。
  - 相同 Android 16 的模擬器與 Sony 實機表現正常。
- **初步推測：**
  - 可能是 Pixel 9a 的相機 HAL / 驅動 / GPU buffer 管線行為差異，使得某些 buffer/Surface/ImageReader/ML 推論中間結果在特定裝置上無法被回收或被系統緩慢回收。
  - 也不排除 v2.8.4 某段程式在特定機型觸發不同路徑（例如解析度/色彩格式/旋轉/穩定器/某 vendor extension）。
- **為什麼重要：**
  - 即時影像管線的記憶體成長會導致：掉幀、卡頓、背景被殺、甚至 Camera 斷線/重啟。
  - 「僅特定機型」最難回歸驗證，需快速建立對照測試與決策依據。

---

## 3. 影響範圍（Scope：模組/檔案/功能層級；若未知請以推測標註）
> 以下以 CameraX / 即時 AI 影像典型架構推測，實際以 repo 為準（標註「推測」）。

### 功能層級
- Live 相機預覽（Preview）
- 影像分析（ImageAnalysis）/ 即時 AI 推論
- 模式切換（Live ↔ Static / Debug / 重新綁定 UseCases）（推測）
- 錄影/拍照若共用 pipeline（推測）

### 模組/元件層級（推測）
- CameraX UseCase 綁定/解綁：`ProcessCameraProvider.bindToLifecycle()` / `unbindAll()`
- Analyzer 實作：`ImageAnalysis.Analyzer` 中 `ImageProxy.close()` 是否在所有分支都執行
- Bitmap/ByteBuffer/Tensor/Mat（若有 OpenCV）重用策略與釋放
- Preview/Surface/GL（若有 GPU filter 或 SurfaceTexture）生命週期管理（推測）
- ML runtime（TFLite / MediaPipe / 自研）推論輸入輸出 buffer 池（推測）

### 檔案/工具層級（已知）
- 已新增/規劃測試與文件（從 git status）：
  - `docs/V2.8.4_MEMORY_TEST_REPORT_THREE_PLATFORMS.md`
  - `docs/V2.8.4_PIXEL_RETEST_PLAN.md`
  - `docs/V2.8.4_TO_V3.0_ROADMAP_ANALYSIS.md`
  - `tools/README_PIXEL_TESTS.md`
  - `tools/pixel_memory_test.ps1`
  - `tools/pixel_memory_test.sh`
- 目前有本機設定檔變動（不應進入正式提交）：
  - `.claude/settings.local.json`（local）
  - `.idea/deploymentTargetSelector.xml`（IDE 狀態）

---

## 4. 風險清單（Risks：含相機管線/模式切換/效能回歸）
1. **相機管線記憶體洩漏/累積（高）**  
   - `ImageProxy` 未 close、或某些例外路徑漏 close → 直接造成 buffer 累積。
   - Pixel 9a 可能更敏感（buffer queue 行為不同），導致其他機型不明顯。
2. **模式切換 / 生命週期競態（中~高）**  
   - 重綁 use case、Activity/Fragment 重建、前景/背景切換造成舊 pipeline 未釋放（Surface/Executor/Analyzer thread）。
3. **效能回歸風險（中）**  
   - 為了止血改成更頻繁 unbind/rebind、降低解析度或提高丟幀，可能影響 FPS、延遲、畫質。
4. **裝置特異性導致不可修（中~高）**  
   - 若是 HAL/driver/GraphicBuffer 行為，App 端只能規避（降級/改格式/關閉某功能），風險在於「治標」與產品體驗下降。
5. **誤判 GC/快取為洩漏（中）**  
   - 若只看 PSS/RSS 成長，可能包含快取或系統層延遲回收；需用一致指標與多次重跑確認趨勢與上限（plateau vs 無上限成長）。

---

## 5. 建議方案（Plan A / Plan B：最小差異優先）

### Plan A（最小差異、以「定位+止血」為主）
1. **標準化三平台測試矩陣與指標**
   - Pixel 9a / Sony 實機 / Android 16 emulator 同條件跑固定 10–20 分鐘。
   - 固定：解析度、FPS、是否開啟 AI、是否開啟 debug overlay、螢幕常亮、相同場景（或 dummy feed 若支援）。
2. **加入「必備檢查點」：ImageProxy/Buffer 關閉與重用**
   - 確認 Analyzer 中 `image.close()` 在所有路徑（成功/失敗/exception/early return）都會執行（必要時用 `try/finally` 保證）。
   - 若有 Bitmap/Tensor/ByteBuffer：優先改為物件池或重用（但先做「不影響架構」的局部重用）。
3. **以 Pixel 9a 為條件的「保守規避」開關（Feature flag）**
   - 僅對 Pixel 9a（或特定 build fingerprint）暫時：
     - 降低 ImageAnalysis 解析度或改回較保守的 backpressure strategy（推測）
     - 關閉某些可能牽涉 GPU/Surface 的特效（若有）
   - 目的：快速確認是否與特定設定/格式相關，並在短期內避免 OOM/卡死。

### Plan B（若 Plan A 證實為平台/驅動特異：以「可控降級+長期修正」為主）
1. **建立「裝置相依相機設定檔」**
   - Pixel 9a 套用保守配置（解析度、格式、分析頻率、backpressure、是否啟用某些 extension）。
2. **隔離相機管線資源釋放流程**
   - 統一在 onStop/onDestroy 或模式切換時做 teardown：停止 analyzer、shutdown executor、unbind use cases、釋放 GPU/bitmap pool。
3. **蒐集可上報的證據**
   - 以可重現 script + 量測數據 +（如可行）perfetto/heap dump 摘要，準備對 Google issue tracker / CameraX issue 提報或查詢已知問題。

---

## 6. 驗證方式（How to verify：模擬器/實機/效能指標）

### 測試平台
- Pixel 9a（Android 16）**必測**
- Sony 實機（Android 16）對照
- Android 16 Emulator 對照

### 測試情境（至少）
1. Live Preview + ImageAnalysis（AI on）
2. Live Preview only（AI off）
3. 模式切換壓力測試（每 N 秒切換一次，或前後台切換）（若產品有此路徑）

### 指標與門檻（建議）
- 記憶體：
  - PSS / RSS / Java heap / Native heap（以 `dumpsys meminfo <package>` 或 script 蒐集）
  - **趨勢判定**：10 分鐘內成長應趨於平穩或低幅波動；不得出現 Pixel 9a 特有的線性成長（例如 > +50MB/10min 可視為警戒，> +100MB/10min 視為阻斷）
- 效能：
  - FPS（平均/最低）
  - 分析延遲（若有）
  - 掉幀/卡頓事件（log / UI 指標）
- 穩定性：
  - 是否觸發 Camera disconnect、ANR、OOM、背景被殺

### 產出
- 對照報告（沿用/更新既有 docs：`V2.8.4_MEMORY_TEST_REPORT_THREE_PLATFORMS.md`）
- 可重跑腳本（已存在：`tools/pixel_memory_test.*`），確保 CI/人工皆可用

---

## 7. 回滾策略（Rollback）
- 任何針對 Pixel 9a 的修正優先以 **Feature Flag / Remote Config / Build-time toggle** 包起來：
  - 可快速切回原設定，避免誤傷其他機型。
- 若引入新的重用池/釋放流程：
  - 保持變更原子化（單一 commit 可回退），並保留原路徑可切換。
- 若最後判定是平台問題：
  - 回滾「激進優化」改動，保留「裝置降級設定」作為熱修止血手段。

---

## 8. 是否需要 Level 2 可行性檢視（Yes/No + 理由）
**Yes。**  
理由：
- 問題直接涉及 **CameraX / ImageAnalysis / 即時影像效能與記憶體**（符合 Level 2 觸發條件）。
- 且呈現 **模擬器 vs 實機差異**、甚至 **特定機型差異（Pixel 9a only）**，高度可能牽涉相機管線生命週期、buffer 管理或 vendor 行為；需要進一步由 Claude Code 檢視 repo 中實際 pipeline、Analyzer、UseCase 綁定與釋放流程，才能判定最小可行修正與風險。
