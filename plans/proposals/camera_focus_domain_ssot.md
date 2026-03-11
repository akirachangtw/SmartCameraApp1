# **Android CameraX & ML Kit 終極對焦領域知識 (Domain Knowledge SSOT)**

⚠️ **【架構師最高警告】**：此文件為相機對焦模組的絕對真理 (Single Source of Truth)。任何涉及對焦狀態機、UI 繪圖、ML Kit 人臉辨識的實作，**必須絕對服從**以下數學公式與硬體物理限制。嚴禁 AI 自由發揮或使用簡化的假座標。

> **🔴 v2.12.0-14 重要更新（2026-03-11）**：Rule 1 的 `CoordinateMapper`（CENTER_CROP 幾何）已全面廢棄。唯一正確的座標真相來源為 `CoordinateTransformer`（FIT_CENTER 幾何）。詳見下方「Rule 1 補充」。

## **領域規則一：ML Kit 至 UI 的座標矩陣映射 (Coordinate Transformation)**

**🚫 嚴禁行為**：直接將 ML Kit 回傳的 Face.boundingBox 寬高 (例如 100.dp) 寫死並畫在 Compose UI 上。這會導致對焦框「固定大小」且位置錯亂。

**✅ 正確實作**：必須建立 CoordinateMapper。ML Kit 的分析圖片 (ImageProxy) 尺寸與螢幕預覽 (PreviewView/Compose) 尺寸完全不同，且帶有旋轉角度。

**⚠️ 【v2.12.0-14 廢棄警告】以下函式為 CENTER_CROP 幾何，僅保留作歷史參考，禁止在新程式碼中使用**：

/\*\*
 \* ❌ DEPRECATED since v2.12.0-14 — CENTER_CROP 幾何，已由 CoordinateTransformer 取代。
 \* 此函式使用 maxOf(scaleX, scaleY) 填滿螢幕邏輯，與 CameraX PreviewView 的
 \* FIT_CENTER 幾何不符，會導致 Eye-AF 框偏移與 hit-test 失準。
 \*/
fun mapBoundingBoxToView(
    faceBoundingBox: Rect,
    imageRect: Rect, // ML Kit 分析圖片的尺寸 (考慮旋轉後)
    viewWidth: Float, // UI Canvas 的寬度
    viewHeight: Float // UI Canvas 的高度
): RectF {
    val scaleX \= viewWidth / imageRect.width().toFloat()
    val scaleY \= viewHeight / imageRect.height().toFloat()
    // ❌ CENTER_CROP：取最大縮放比填滿螢幕（已廢棄）
    val scale \= maxOf(scaleX, scaleY)

    val offsetX \= (viewWidth \- imageRect.width() \* scale) / 2f
    val offsetY \= (viewHeight \- imageRect.height() \* scale) / 2f

    val mappedRect \= RectF(
        faceBoundingBox.left \* scale \+ offsetX,
        faceBoundingBox.top \* scale \+ offsetY,
        faceBoundingBox.right \* scale \+ offsetX,
        faceBoundingBox.bottom \* scale \+ offsetY
    )
    return mappedRect
}

**✅ v2.12.0-14 正確實作：CoordinateTransformer（FIT_CENTER 幾何）**

本專案 v2.11.1 起統一使用 `CoordinateTransformer`，採 FIT_CENTER 幾何（`minOf(scaleX, scaleY)`），與 CameraX `PreviewView` 的 `ScaleType.FIT_CENTER` 完全對齊。

```kotlin
// ✅ 正確路徑（v2.12.0-14 確立為唯一真相來源）
val transformer = _state.value.coordinateTransformer ?: return TapTargetInfo.Generic

// Image → Screen 轉換（FIT_CENTER 幾何）
val screenBox = imageRectToScreenRectF(face.boundingBox, transformer)

// Screen → Image 轉換（tap hit-test）
val (imageX, imageY) = transformer.screenToImage(x, y) ?: return TapTargetInfo.Generic
```

**三條硬性防禦規則（v2.12.0-14 確立）**：
1. `transformer == null` 時：`resolveTapTarget()` 必須 `return TapTargetInfo.Generic`，絕不使用 raw coords
2. `transformer == null` 時：OF 路徑必須 `return@launch`，絕不呼叫 `startOpticalFlowTracking(x, y)`（CoordinateMapper 路徑）
3. Tap 在 content bounds 外時：OF 路徑必須 `return@launch`，絕不 fallback

**Bootstrap 快取機制**（解決 App 啟動時 transformer 尚未就緒的問題）：
```kotlin
private var lastKnownImageWidth: Int = 0   // v2.12.0-14
private var lastKnownImageHeight: Int = 0  // v2.12.0-14
// updateContentBounds() 中：先快取再以快取建立 transformer
val resolvedW = if (effectiveImageWidth > 0) effectiveImageWidth else lastKnownImageWidth
val resolvedH = if (effectiveImageHeight > 0) effectiveImageHeight else lastKnownImageHeight
// ⚠️ resolvedH guard 必須對應 effectiveImageHeight，禁止誤用 effectiveImageWidth
```

* **架構約束**：UI 層只負責接收轉換好的 RectF 進行繪製。轉換邏輯必須存在於 ViewModel 或獨立的 Mapper 類別中 (符合 UDF)。

## **領域規則二：定焦鏡頭防死鎖與 Bypass 機制 (Fixed-Focus Hardware Bypass)**

**🚫 嚴禁行為**：在任何鏡頭下發送對焦指令後，死等 FocusState.Locked 回調。前鏡頭通常沒有對焦馬達，這會導致狀態機永久卡死在 Focusing。

**✅ 正確實作**：在啟動 startFocusAndMetering 之前，必須先向 CameraCharacteristics 查詢硬體能力。

**核心檢測與狀態跳轉邏輯**：

// 1\. 查詢鏡頭是否為定焦 (Fixed-Focus)  
val minFocusDistance \= cameraCharacteristics.get(CameraCharacteristics.LENS\_INFO\_MINIMUM\_FOCUS\_DISTANCE) ?: 0f  
val isFixedFocus \= minFocusDistance \== 0f

// 2\. 狀態機約束邏輯  
if (isFixedFocus) {  
    // ⚠️ 絕對禁止發送 AF 觸發指令給底層！  
    // 直接發送 AE (曝光) 指令即可。  
    // 狀態機必須直接從 Focusing 轉移到 Focused (Bypass)，  
    // 或使用 Coroutines delay(300L) 模擬對焦動畫時間後跳轉。  
    simulateFocusSuccessForFixedLens()   
} else {  
    // 只有支援 AF 的鏡頭，才能進入傳統的等待 Callback 狀態  
    waitForAutoFocusCallback()  
}

## **領域規則三：多人臉與光流追蹤綁定 (Multi-Face Tracking ID)**

**🚫 嚴禁行為**：直接使用 faces.firstOrNull() 放棄其他人臉，或沒有將人臉特徵交給光流追蹤器，導致後鏡頭 Eye-AF 亂跳。

**✅ 正確實作**：必須利用 ML Kit 賦予的 TrackingId 作為跨幀追蹤的唯一憑證。

**核心綁定邏輯約束**：

1. 當啟用 ML Kit Face Detection 時，必須開啟 Tracking 設定：setTrackingEnabled(true)。  
2. 狀態機必須維護一個 currentTargetId: Int?。  
3. 當畫面上出現多張臉時，依據「距離中心點最近」或「面積最大」的臉，將其 TrackingId 鎖定為 currentTargetId。  
4. 光流追蹤模組 (Optical Flow) 必須接收這組包含正確 TrackingId 的特徵點，確保即使用戶轉頭或有多人入鏡，對焦框依然死鎖在同一人身上。

**本文件為最高優先級。AI Agent (Gemini/Claude) 在產出代碼或審查架構時，若違反上述三條領域規則，請立即宣告失敗並要求重構。**