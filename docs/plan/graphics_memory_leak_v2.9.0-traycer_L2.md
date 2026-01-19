v2.9.0 Graphics 記憶體洩漏根因分析與可行性方案
📊 問題摘要
核心問題：v2.9.0-alpha 將解析度從 480x480 降至 360x360 後，Graphics 記憶體從 63.8 MB 暴增至 224.3 MB，退化至 v2.8.5 水平。

版本	解析度	Graphics @ 10min	狀態
v2.8.5 Baseline	480x480	225.8 MB	🔴 嚴重洩漏
v2.8.6 Phase 2	480x480	63.8 MB	✅ 已修復
v2.9.0-alpha	360x360	224.3 MB	🔴 退化
🔍 根因分析（靜態程式碼審查）
根因 #1: BitmapPool Key 不匹配 ⭐ 主要原因
問題描述
在 @app/src/main/java/com/akirachang/ai/photographer/util/BitmapPool.kt 中，Pool Key 是基於精確的 width x height x config 生成：

kotlin
private data class Key(
    val width: Int,
    val height: Int,
    val config: Bitmap.Config
)
問題點：

360x360 與 480x480 產生不同的 Pool Key
v2.8.4 的 BitmapPool 優化是針對 480x480 設計的
當解析度改為 360x360 時，所有 Bitmap 請求都會 Pool Miss
導致持續分配新 Bitmap，無法重用，Graphics 記憶體洩漏
證據
從 @app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt Line 227-291：

kotlin
// Line 227: 申請 Bitmap（使用 imageProxy 的尺寸）
val rawBuffer = bitmapPool.acquire(imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888)
// Line 770: Transform 後申請輸出 Bitmap
val out = bitmapPool.acquire(outWidth, outHeight, Bitmap.Config.ARGB_8888)
// Line 291-293: 釋放 Bitmap
if (outputBuffer !== rawBuffer) {
    bitmapPool.release(rawBuffer)
}
問題：

當解析度從 480x480 改為 360x360 時，Pool 中沒有對應尺寸的 Bitmap
每次都需要建立新的 Bitmap（Line 62-65 in BitmapPool.kt）
舊的 480x480 Bitmap 無法被重用，佔用 Graphics 記憶體
驗證方法
檢查 BitmapPool 的 Hit Rate：

預期：如果是此問題，Hit Rate 應該 < 20%（幾乎全部 Miss）
正常：v2.8.6 的 Hit Rate > 80%
根因 #2: Transform 尺寸計算問題（次要）
在 @app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt Line 754-771：

kotlin
// v2.8.4 Quick Win: Fixed output size to eliminate BitmapPool key explosion
// Problem: ceil(mapRect()) produces floating-point errors (721, 719, etc.)
//   causing pool keys to explode from 2 to 10+, hit rate <50%
// Solution: Calculate exact integer dimensions based on rotation
問題：

v2.8.4 修復了 Transform 尺寸計算的浮點誤差問題
但這個修復是基於 480x480 測試的
360x360 可能產生不同的浮點誤差模式
導致 Pool Key 數量爆炸（2 → 10+）
根因 #3: RenderScript Allocation 尺寸變化
在 @app/src/main/java/com/akirachang/ai/photographer/util/RenderScriptYuvConverter.kt Line 78-98：

kotlin
if (width != lastWidth || height != lastHeight || yuvAllocation == null || outAllocation == null) {
    destroyAllocations()
    
    // 重新建立 Allocation
    yuvAllocation = Allocation.createTyped(rs, yuvType, Allocation.USAGE_SCRIPT)
    outAllocation = Allocation.createTyped(rs, rgbType, Allocation.USAGE_SCRIPT)
    
    lastWidth = width
    lastHeight = height
}
問題：

當解析度從 480x480 改為 360x360 時，RenderScript 需要重新建立 Allocation
舊的 Allocation 被銷毀（Line 121-126），但 Graphics 記憶體可能未立即釋放
如果 GC 不及時，會累積 Graphics 記憶體
證據：

v2.8.5 測試報告顯示 Native Heap 穩定（79-92 MB）
但 Graphics 記憶體持續增長（116 MB → 223 MB）
這符合 RenderScript Allocation 未釋放的模式
💡 可行性方案
方案 A: 動態 BitmapPool Key（推薦）⭐⭐⭐⭐⭐
實作策略
修改 @app/src/main/java/com/akirachang/ai/photographer/util/BitmapPool.kt：

kotlin
private data class Key(
    val width: Int,
    val height: Int,
    val config: Bitmap.Config
)
// 新增：將尺寸 Round 到 16px 邊界，提高重用率
private fun normalizeKey(width: Int, height: Int, config: Bitmap.Config): Key {
    // Round to nearest 16px boundary (GPU texture alignment)
    val roundedWidth = ((width + 15) / 16) * 16
    val roundedHeight = ((height + 15) / 16) * 16
    return Key(roundedWidth, roundedHeight, config)
}
fun acquire(width: Int, height: Int, config: Bitmap.Config = Bitmap.Config.ARGB_8888): BitmapBuffer {
    val key = normalizeKey(width, height, config)  // 使用正規化的 Key
    val deque = pools.getOrPut(key) { ArrayDeque() }
    val buffer = deque.removeFirstOrNull()
    
    if (buffer != null) {
        hits++
        // 如果尺寸不完全匹配，需要調整 Bitmap
        if (buffer.bitmap.width != width || buffer.bitmap.height != height) {
            // 使用 Bitmap.createBitmap() 裁切/擴展
            return BitmapBuffer(
                bitmap = Bitmap.createBitmap(buffer.bitmap, 0, 0, width, height),
                pixels = IntArray(width * height)
            )
        }
        return buffer
    } else {
        misses++
        // ... 建立新 Bitmap
    }
}
優點
✅ 高重用率：360x360 會 Round 到 368x368，480x480 會 Round 到 480x480
✅ GPU 友善：16px 對齊符合 GPU Texture 要求
✅ 向後相容：不影響現有功能
✅ 低風險：只修改 BitmapPool.kt 一個檔案
缺點
⚠️ 記憶體略微增加（~5-10%，因為 Bitmap 略大）
⚠️ 需要額外的 Bitmap 裁切操作（但比建立新 Bitmap 快）
預期效果
Graphics 記憶體：224 MB → 70-80 MB（接近 v2.8.6）
BitmapPool Hit Rate：< 20% → > 75%
FPS 影響：無（或略微提升）
方案 B: 多尺寸 BitmapPool（備選）⭐⭐⭐⭐
實作策略
修改 @app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt：

kotlin
// 為不同解析度建立專用 Pool
private val bitmapPoolSmall = BitmapPool(maxEntriesPerKey = 4)  // For 360x360
private val bitmapPoolMedium = BitmapPool(maxEntriesPerKey = 4) // For 480x480
private val bitmapPoolLarge = BitmapPool(maxEntriesPerKey = 4)  // For 720x720
private fun selectBitmapPool(width: Int, height: Int): BitmapPool {
    val pixels = width * height
    return when {
        pixels <= 360 * 360 -> bitmapPoolSmall
        pixels <= 480 * 480 -> bitmapPoolMedium
        else -> bitmapPoolLarge
    }
}
// 在 Bitmap 申請處使用
val pool = selectBitmapPool(imageProxy.width, imageProxy.height)
val rawBuffer = pool.acquire(imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888)
優點
✅ 完美隔離：不同解析度不會互相干擾
✅ 精確控制：每個 Pool 可以獨立調整大小
✅ 易於監控：可以分別追蹤各 Pool 的 Hit Rate
缺點
⚠️ 記憶體增加：需要維護多個 Pool（+20-30 MB）
⚠️ 複雜度增加：需要修改多處程式碼
⚠️ 維護成本：未來新增解析度需要調整邏輯
預期效果
Graphics 記憶體：224 MB → 80-90 MB
BitmapPool Hit Rate：< 20% → > 85%
FPS 影響：無
方案 C: 限制最小解析度（保守）⭐⭐⭐
實作策略
修改 @app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt Line 533：

kotlin
// 原本：360x360
val targetResolution = if (Build.MODEL == "Pixel 9a") {
    Size(360, 360)  // 🔴 導致洩漏
} else {
    Size(480, 360)
}
// 修改為：使用 CameraX 建議的最小解析度
val targetResolution = if (Build.MODEL == "Pixel 9a") {
    Size(480, 360)  // ✅ 使用 ML Kit 建議最小值
} else {
    Size(480, 360)
}
優點
✅ 零風險：只改一行程式碼
✅ 立即生效：無需測試 BitmapPool 邏輯
✅ 符合規範：ML Kit 建議最小解析度為 480x360
缺點
❌ 效能改善打折：480x360 vs 360x360，像素減少幅度從 44% 降至 25%
❌ 未解決根本問題：BitmapPool 仍然不支援動態尺寸
預期效果
Graphics 記憶體：224 MB → 63.8 MB（回到 v2.8.6）
FPS 改善：理論上 +25%（實際可能更少）
熱節流改善：中等（不如 360x360）
方案 D: 強制 RenderScript Allocation 釋放（輔助）⭐⭐
實作策略
修改 @app/src/main/java/com/akirachang/ai/photographer/util/RenderScriptYuvConverter.kt：

kotlin
private fun destroyAllocations() {
    yuvAllocation?.destroy()
    outAllocation?.destroy()
    yuvType = null
    rgbType = null
    yuvAllocation = null
    outAllocation = null
    
    // 新增：強制觸發 GC（僅在 Debug 模式）
    if (BuildConfig.DEBUG) {
        System.gc()
        Log.d(TAG, "[v2.9.0] Forced GC after Allocation destroy")
    }
}
優點
✅ 簡單：只加幾行程式碼
✅ 輔助效果：可以配合方案 A/B 使用
缺點
⚠️ 效果有限：GC 不保證立即執行
⚠️ 可能影響效能：強制 GC 會造成卡頓
預期效果
Graphics 記憶體：略微改善（-10-20 MB）
不建議單獨使用，應配合方案 A 或 B
🎯 建議實作順序
Phase 0: 快速驗證（1-2 小時）
添加 BitmapPool 詳細日誌

修改 @app/src/main/java/com/akirachang/ai/photographer/util/BitmapPool.kt
記錄每次 acquire/release 的尺寸和 Hit/Miss
確認 Hit Rate 是否真的很低
Pixel 9a 實機測試

運行 v2.9.0-alpha 5 分鐘
檢查 Logcat 中的 BitmapPool 統計
確認假設：Hit Rate < 20%
決策點：

如果 Hit Rate < 20% → 確認是 BitmapPool 問題，進入 Phase 1
如果 Hit Rate > 60% → 不是 BitmapPool 問題，需要深入調查
Phase 1: 實作方案 A（推薦）（2-3 小時）
修改 BitmapPool.kt

實作 normalizeKey() 函數
修改 acquire() 邏輯
添加單元測試
測試驗證

單元測試：驗證 360x360 和 368x368 可以共用
整合測試：Pixel 9a 運行 10 分鐘
檢查 Graphics 記憶體是否降至 70-80 MB
效能驗證

確認 FPS 無下降
確認 BitmapPool Hit Rate > 75%
決策點：

如果成功 → 發布 v2.9.0-beta1
如果失敗 → 回滾，嘗試方案 B 或 C
Phase 2: 備選方案（如果 Phase 1 失敗）
選項 2A: 實作方案 B（多尺寸 Pool）
工作量：3-4 小時
風險：低
預期效果：高
選項 2B: 實作方案 C（限制解析度）
工作量：0.5 小時
風險：極低
預期效果：中等（效能改善打折）
📊 方案比較
方案	工作量	風險	預期效果	記憶體改善	FPS 改善	推薦度
A: 動態 Key	2-3h	低	高	224→70 MB	維持	⭐⭐⭐⭐⭐
B: 多 Pool	3-4h	低	高	224→80 MB	維持	⭐⭐⭐⭐
C: 限制解析度	0.5h	極低	中	224→64 MB	+25%	⭐⭐⭐
D: 強制 GC	0.5h	低	低	224→200 MB	無	⭐⭐
🔬 驗證計畫
成功標準
Graphics 記憶體：< 100 MB @ 10 分鐘
BitmapPool Hit Rate：> 75%
FPS：維持或提升（不下降）
穩定性：無 Crash，無 ANR
測試矩陣
測試項目	v2.9.0-alpha	v2.9.0-beta1 (方案A)	目標
Graphics @ 10min	224.3 MB	?	< 100 MB
Pool Hit Rate	< 20%	?	> 75%
FPS (初期)	25-28	?	≥ 25
FPS (10min)	12-20	?	≥ 18
🎯 最終建議
推薦路徑：方案 A（動態 BitmapPool Key）
理由：

✅ 根本解決：解決 BitmapPool 不支援動態尺寸的問題
✅ 低風險：只修改一個檔案，易於回滾
✅ 高效益：預期 Graphics 記憶體降至 70 MB（-69%）
✅ 未來友善：支援任意解析度，不需要為每個解析度調整
實作時程：

Phase 0（驗證）：1-2 小時
Phase 1（實作）：2-3 小時
總計：3-5 小時
風險緩解：

如果方案 A 失敗，立即回滾至方案 C（限制解析度）
方案 C 只需 0.5 小時，可以作為保底方案
📁 相關檔案
需要修改的檔案
@app/src/main/java/com/akirachang/ai/photographer/util/BitmapPool.kt

主要修改：實作 normalizeKey() 函數
次要修改：調整 acquire() 邏輯
@app/src/test/java/com/akirachang/ai/photographer/util/BitmapPoolTest.kt

新增測試：驗證動態 Key 邏輯
需要監控的檔案
@app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt

監控：Bitmap 申請/釋放邏輯
確認：無洩漏路徑
@app/src/main/java/com/akirachang/ai/photographer/util/RenderScriptYuvConverter.kt

監控：Allocation 生命週期
確認：正確銷毀
🔗 參考文件
@docs/plan/graphics_memory_leak_v2.9.0_L2.md - L2 調查計畫
@docs/plan/v2.9.0_thermal_optimization_L1.md - v2.9.0 熱節流優化計畫
@pixel9a_report.md - v2.8.5 記憶體測試報告
@docs/V2.8.4_MEMORY_TEST_REPORT_THREE_PLATFORMS.md - v2.8.4 記憶體測試
我已經完成了詳細的根因分析和可行性評估。主要發現：

🎯 根本原因：BitmapPool 的 Key 是基於精確尺寸（360x360 vs 480x480），導致解析度改變後無法重用 Bitmap，造成 Graphics 記憶體洩漏。

💡 推薦方案：實作動態 BitmapPool Key（方案 A），將尺寸 Round 到 16px 邊界，提高重用率。預期可將 Graphics 記憶體從 224 MB 降至 70 MB。

⏱️ 實作時程：3-5 小時（包含驗證和測試）

我已經將 v2.9.0 Graphics 記憶體洩漏修復方案拆解成可執行的 Phases。這個計畫採用漸進式驗證策略，先快速確認根因，再實作核心修復，最後進行完整測試驗證。

Phase breakdown is complete

Show breakdown
Phase 0: 添加 BitmapPool 診斷日誌並驗證根因假設
Not Started
Phase 1: 實作動態 BitmapPool Key 正規化機制
Not Started
Phase 2: 優化 RenderScript Allocation 生命週期管理
Not Started
我已經將修復方案拆解成 6 個 Phases：

核心流程（Phase 0-4）：

Phase 0：驗證根因假設（添加診斷日誌）
Phase 1：實作動態 BitmapPool Key（核心修復）
Phase 2：優化 RenderScript Allocation（輔助修復）
Phase 3：Pixel 9a 實機測試驗證
Phase 4：文件更新與版本發布
備選方案（Phase 5）：

如果 Phase 1-2 失敗，使用保守的解析度限制方案
預估時程：

Phase 0-4：3-5 小時
Phase 5（如需要）：0.5 小時
決策點：

Phase 0 完成後：確認是否為 BitmapPool 問題
Phase 3 完成後：決定發布 v2.9.0 或回滾至備選方案