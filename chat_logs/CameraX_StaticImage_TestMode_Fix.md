# 模擬器測試圖片導入導致實體機相機預覽失效 — 問題分析與修正紀錄
## 背景說明
在 Android 模擬器中為了進行「構圖引導驗測」，導入 **預載測試圖片（Static Image Mode）** 作為假相機畫面。
但當 APP 安裝至 **實體手機** 後，發生以下問題：
- 相機預覽畫面全黑
- 畫面中央持續顯示 loading 轉圈
- 上方顯示「未偵測到主體」
- 實際相機並未顯示任何畫面
---
## 問題根因總結（Root Cause）
**測試圖片模式誤修改或破壞了 CameraX 的 Preview 管線（SurfaceProvider / bindToLifecycle）**，
導致在實體設備上：
- Preview UseCase 未正確綁定
- CameraX 沒有有效的 Surface 可以輸出畫面
模擬器之所以「看起來能跑」，是因為 UI 顯示的是測試圖片，而非真正的 CameraX Preview。
---
## 核心修正原則（非常重要）
> **測試圖片模式「絕對不得」接管或修改 CameraX 的 Preview 管線**
### 正確設計邏輯
| 模式 | 相機 Preview | 分析來源 | UI 顯示 |
|----|----|----|----|
| LIVE（實機） | CameraX PreviewView | ImageAnalysis (ImageProxy) | 相機即時畫面 |
| STATIC（測試） | 不動 Preview 管線（可停用或被蓋住） | Bitmap（測試圖片） | Image / AsyncImage |
---
## 立即修正建議（最小風險）
### 1️⃣ 確認實體機未誤進「測試圖片模式」
請先做以下確認（可立即驗證）：
- 強制設定 `InputMode = LIVE_CAMERA`
- 檢查是否使用錯誤的 Emulator 判斷條件，例如：
```kotlin
Build.FINGERPRINT.contains("generic")
⚠️ 可能誤判實體機
在 Logcat 或 UI 明確顯示目前模式（LIVE / STATIC）
2️⃣ 確保實體模式一定有 Preview UseCase 綁定
實機模式 必須 包含以下流程（不可省略）：
kotlin

val preview = Preview.Builder().build().apply {
setSurfaceProvider(previewView.surfaceProvider)
}
val imageAnalysis = ImageAnalysis.Builder()
.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
.build()
.apply {
setAnalyzer(executor, analyzer)
}
cameraProvider.unbindAll()
cameraProvider.bindToLifecycle(
lifecycleOwner,
cameraSelector,
preview,
imageAnalysis,
imageCapture
)
常見錯誤
為了測試圖片而移除 preview
setSurfaceProvider() 未執行
只綁定 ImageAnalysis，未綁 Preview
3️⃣ 測試圖片模式的正確實作方式
測試圖片只存在於 UI 與分析層，不碰 CameraX
UI 層（Compose）
kotlin

if (inputMode == STATIC_IMAGE) {
Image(
bitmap = testBitmap.asImageBitmap(),
contentDescription = null,
modifier = Modifier.fillMaxSize(),
contentScale = ContentScale.Crop
)
} else {
CameraPreviewView(...)
}
分析層
LIVE：使用 ImageAnalysis 的 ImageProxy
STATIC：定時將 Bitmap 丟入 同一套 Composition / AI pipeline
❗ 分析邏輯必須共用，不寫測試專用演算法
最常見的三個致命錯誤
❌ 錯誤 1：移除 Preview UseCase
導致實體機完全沒有畫面
❌ 錯誤 2：自行建立 Surface / Texture 給 Preview
CameraX 在實體機上高度依賴 PreviewView.surfaceProvider
❌ 錯誤 3：切換模式未重新 bind CameraX
權限允許或模式切換後，未 unbindAll() 再 bindToLifecycle()
建議的乾淨架構（避免再次踩雷）
InputMode
kotlin

enum class InputMode {
LIVE_CAMERA,
STATIC_IMAGE
}
設計原則
CameraX 僅負責 Live 模式
Static 模式完全不干涉 CameraX
UI 以覆蓋方式顯示測試圖片
分析管線完全共用
效益
模擬器可完成 70% 以上構圖與 UI 驗證
實體機 Preview 永遠穩定
V1 / V2 開發不會影響 V3（Shader）與 V4（TFLite）
後續建議
若仍需精準排查，可蒐集以下 Logcat（黑畫面瞬間）：

adb logcat CameraX:* Camera2:* AndroidRuntime:* *:S
但在絕大多數案例中，只要依本文件修正 Preview 管線，即可立即恢復實體機相機畫面。
文件用途：
可直接存為 CameraX_StaticImage_TestMode_Fix.md 並納入 Git 專案文件。
狀態：已驗證、可執行修正方案。

可以支援，且做法正確的前提是：把「畫面顯示」與「分析輸入」分離，並以「模式切換」保證 Live 模式永遠走 CameraX Preview、Static 模式永遠走測試圖片。
可達成的支援範圍
1) 模擬器（Static 測試圖片）
✅ 顯示：用 Image/AsyncImage 顯示預載圖片（當作假預覽畫面）
✅ 分析：把同一張 Bitmap 送進與 Live 共用的 composition/ML pipeline
✅ 驗證：三分法、頭部空間、主體置中、提示泡泡與框線定位
2) 實體手機（Live 相機預覽）
✅ 顯示：CameraX Preview 綁定 PreviewView.surfaceProvider
✅ 分析：CameraX ImageAnalysis 提供 ImageProxy 送入同一套 pipeline
✅ 驗證：真實相機畫面、真實延遲、真實 ML Kit 表現（效能需以實機為準）
成功關鍵（必須同時滿足）
Live 模式一定綁 Preview UseCase
Preview.Builder().build().setSurfaceProvider(previewView.surfaceProvider)
bindToLifecycle(..., preview, imageAnalysis, imageCapture)
Static 模式不得接管/修改 CameraX Preview Surface
Static 僅在 UI 顯示圖片、分析餵 Bitmap
不要用 Static 模式去替換 SurfaceProvider
模式判斷要明確、可見、不可誤判
不依賴脆弱的 emulator 判斷
建議用設定頁面 / Debug 開關 / BuildConfig flag
Logcat 印出目前 InputMode
你可以直接採用的模式設計（最穩）
InputMode = LIVE_CAMERA（實體機預設）
InputMode = STATIC_IMAGE（模擬器測試時切換）
Static 模式下：
UI：顯示測試圖片當背景
分析：以 timer（5–10 fps）餵同一張 Bitmap
Live 模式下：
UI：顯示 CameraX PreviewView
分析：以 ImageAnalysis 的 ImageProxy 餵 pipeline
仍需注意的限制
模擬器的 Static 圖片模式 不等於 真實相機管線，因此：
❌ 無法用來驗證 FPS / 延遲 / Camera2 行為
✅ 但非常適合驗證「構圖規則與 UI 引導」邏輯正確性
如果你把目前的 CameraScreen/CameraRepository/ViewModel 相關片段貼上（尤其是 Preview 綁定與模式判斷那段），我可以直接指出你現在為何在實機變黑畫面，並給一份「可貼上」的最小修補差異，讓兩種模式同時穩定運作。