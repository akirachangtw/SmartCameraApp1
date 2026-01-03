雙相機同步修正後「實機無法切換前置鏡頭」回歸

## 1. 問題摘要
在套用「雙相機同步（CameraRepositoryImpl + LiveCameraSource）」修正後，實體手機出現「無法切換前置鏡頭」的回歸現象。

## 2. 影響範圍
- 僅影響 **實機**（`LiveCameraSource` + `ProcessCameraProvider` 路徑）。
- 模擬器/測試圖片模式（`StaticImageSource`）不受影響。

## 3. 現象描述（可觀察行為）
- 點擊切換鏡頭後，UI 狀態可能顯示已切換，但畫面/預覽流停止更新，使用者感受為「切不到前置」或「切換後畫面卡住/黑」。
- 根因並非 CameraSelector 沒換，而是 **切換後 preview frame 不再流動**。

## 4. 根因分析（回歸點）
`LiveCameraSource.switchCamera()` 會觸發 `bindToLifecycle()` 重新建立 UseCases（特別是 `ImageAnalysis`）。
目前 `getPreviewStream()` 的 analyzer 只在「當下的 imageAnalysis」上設定：
- 切換鏡頭導致重新 new `ImageAnalysis` 後，**原本設定的 analyzer 遺失**。
- 結果：`callbackFlow` 還在，但新 `imageAnalysis` 沒 analyzer → **沒有 frame** → 看起來像前置切換失敗。

關聯檔案與位置（以目前 repo 結構）：
- `app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt`
  - `getPreviewStream()`
  - `bindToLifecycle()`
  - `switchCamera()`

## 5. 修正目標
- 切換鏡頭（BACK ↔ FRONT）後，預覽 frame 仍持續流動。
- 不改變現有架構（短期修復），只補齊 rebind 時 analyzer 恢復機制。
- 保持切換必須在 Main thread 的既有約束。

## 6. 修正方案（最小變更）
### 6.1 核心修正：持久化並在每次 rebind 後重新掛回 analyzer
在 `LiveCameraSource` 新增成員：
- `private var previewStreamAnalyzer: ImageAnalysis.Analyzer? = null`

調整流程：
1. `getPreviewStream()` 建立 analyzer 後：
   - `previewStreamAnalyzer = analyzer`
   - 若 `imageAnalysis` 已初始化：照舊立即 `setAnalyzer(...)`
   - 若尚未初始化：不再只靠一次性的 pending；等 `bindToLifecycle()` 完成後再掛回
2. `bindToLifecycle()` 成功 `bindToLifecycle(...)` 後：
   - 若 `previewStreamAnalyzer != null`：對「新建立的 imageAnalysis」呼叫 `setAnalyzer(...)`
   - 確保 switch/rebind 後仍有 frame
3. `awaitClose` / `stopPreview()` 時：
   - 清掉 analyzer 並 `previewStreamAnalyzer = null`，避免持有無效引用

### 6.2 日誌（用於實機確認）
在以下點加/調整 Log：
- rebind 完成後：「attached previewStreamAnalyzer」的 log
- switchCamera 後 selector 的 log（現有已有）

## 7. 風險評估
- 低風險：只影響 `LiveCameraSource` 的 analyzer 掛載時機。
- 潛在風險：
  - 重複 setAnalyzer：可接受；CameraX 會以最後一次為準。
  - lifecycle 多次 bind：已存在，這次只是確保 analyzer 不遺失。

## 8. 驗證計畫（實機為主）
### 8.1 基本切換
1. 開啟相機畫面，確認預覽正常（持續更新）。
2. 切到前置（按一次切換）。
3. 觀察 3 秒：預覽應持續更新、不黑屏、不停格。
4. 再切回後置，重複確認。

### 8.2 壓力測試
- 連續切換 10 次（含快連點）：預覽不應停止更新。
- 切換後拍照：應可成功（若 capture flow 已修正）。

### 8.3 logcat 關鍵字
- `LiveCameraSource`
- `Camera selector switched`
- `✅ Preview stream analyzer attached`
- `Preview frame sent`

## 9. 回滾策略
若修正造成新問題：
- revert `LiveCameraSource` analyzer 持久化改動（回到原本 pending 機制）
- 保留 `switchCamera()` Main thread 機制不動

## 10. 變更清單（預期）
- 修改：`app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt`
  - `pendingAnalyzer` → `previewStreamAnalyzer`（語意調整）
  - `getPreviewStream()` 記錄 analyzer
  - `bindToLifecycle()` 每次 bind 後重掛 analyzer
  - `stopPreview()`/`awaitClose` 清理 analyzer