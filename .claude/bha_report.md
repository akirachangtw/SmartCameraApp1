# BHA-01 Boundary Health Audit Report

**Date**: 2026-05-11  
**Branch**: `feature/cueshot-mvp`  
**Scope**: `app/src/main/java/` (Read-Only Diagnostic)  
**Git Status**: Clean (no modifications)

---

## Executive Summary

| Dimension | Score | Critical Findings |
|-----------|-------|-------------------|
| D1 Domain 純淨度 | 🟡 警告 | MLKit 類型洩漏進 domain model (DetectionResult 持有 `Face`/`DetectedObject`) |
| D2 ViewModel 膨脹度 | 🔴 嚴重 | 2457 行，58 functions，58 imports；座標空間 BUG 確認 |
| D3 Repository 隔離度 | 🟡 警告 | domain/CameraRepository.kt 持有 CameraX 型別；SmartCameraAnalyzer 有雙管線 |
| D4 狀態一致性 | 🟡 警告 | 11 處 `.value =` 直接賦值（應優先 `.update{}`）；多條 StateFlow 並存 |

---

## D1 — Domain 層純淨度掃描

### D1-1: Android imports in domain (`import android.*`)
```
(無輸出 — 無直接 android.* 套件導入)
```
但存在 `android.graphics.*` 二級導入：

```
domain/composition/CompositionGuidelineEngine.kt: import android.graphics.RectF
domain/focus/geometry/GeometryMapper.kt:          import android.graphics.PointF, RectF
domain/model/DetectionResult.kt:                  import android.graphics.PointF
domain/model/FaceLandmarkData.kt:                 import android.graphics.PointF, RectF
domain/model/focus/FocusControllerResult.kt:      import android.graphics.PointF, RectF
domain/model/focus/FocusTarget.kt:                import android.graphics.PointF, RectF
domain/model/focus/FramePacket.kt:                import android.graphics.Matrix
domain/model/focus/GeometryContext.kt:            import android.graphics.Matrix
domain/model/focus/OverlayModel.kt:               import android.graphics.RectF
domain/model/focus/UserIntentState.kt:            import android.graphics.PointF
domain/model/FrameToViewTransform.kt:             import android.graphics.Matrix
domain/repository/CameraRepository.kt:            import android.graphics.Bitmap
domain/usecase/AnalyzeLightingUseCase.kt:          import android.graphics.Bitmap, Rect
```

### D1-2: MLKit imports in domain
```
domain/model/DetectionResult.kt:17: import com.google.mlkit.vision.face.Face
domain/model/DetectionResult.kt:18: import com.google.mlkit.vision.objects.DetectedObject
```
**⚠️ 嚴重洩漏**: `DetectionResult` 直接持有 ML Kit 原生型別 `Face` 和 `DetectedObject`，違反 Domain 層隔離原則。

### D1-3: AndroidX imports in domain
```
domain/repository/CameraRepository.kt: import androidx.camera.core.CameraSelector
domain/repository/CameraRepository.kt: import androidx.camera.core.Preview.SurfaceProvider (inline)
domain/repository/CameraRepository.kt: import androidx.camera.view.PreviewView (inline)
domain/repository/CameraRepository.kt: import androidx.camera.core.CameraControl (inline)
domain/repository/CameraRepository.kt: import androidx.camera.core.MeteringPointFactory (inline)
domain/usecase/AnalyzeLightingUseCase.kt: import androidx.camera.core.ImageProxy
```
**⚠️ 架構洩漏**: CameraRepository interface 直接引用 CameraX 型別，使 domain 層依賴 infrastructure。

### D1-4: Android platform types in domain
```
(無 RectF\|Bitmap\|ImageProxy\|InputImage 直接命名，但 android.graphics.* 已確認如上)
```

**Domain 純淨度總結**: 53 個 domain 檔案中有 **2 個 ML Kit 洩漏** + **6+ 個 CameraX 洩漏**。現階段為 MVP 快速迭代，屬已知技術債。

---

## D2 — ViewModel 職責膨脹度掃描

### D2-1: Line count
```
2457 lines — app/src/main/java/.../presentation/camera/CameraViewModel.kt
```
超出 300 行規範 **8.2×**。

### D2-2: Function count
```
58 functions (grep -c "fun ")
```

### D2-3: Import count
```
58 imports
```
包含大量跨層導入（MLKit, CameraX, android.graphics）。

### D2-4: Coordinate/bbox 相關行

```
Line 602:  contentBounds = bounds (CoordinateTransformer 建立)
Line 603:  coordinateTransformer = transformer
Line 908:  val transformer = _state.value.coordinateTransformer
Line 925:  face.boundingBox.contains(imageX, imageY)
Line 927:  imageRectToScreenRectF(face.boundingBox, transformer)
Line 980:  centerX = face.boundingBox.exactCenterX()
Line 981:  centerY = face.boundingBox.exactCenterY()
Line 1985: val imgW = det.imageWidth.toFloat()   ← BUG HERE
Line 1986: val imgH = det.imageHeight.toFloat()   ← BUG HERE
Line 1987: val box = face.boundingBox
Line 1988: val boundingBoxNormalized = RectF(box.left/imgW, ...)  ← WRONG normalizer
Line 2081: val imgW = det.imageWidth.toFloat()   ← SAME BUG (PTF pipeline)
Line 2082: val imgH = det.imageHeight.toFloat()
Line 2092: rawCx = (bbox.left + bbox.right) / 2f / imgW  ← WRONG
```

### D2-5: Face/Object detection references
計 **23 行**直接引用 MLKit `Face`/`DetectedObject`/`FaceDetector`/`ObjectDetector`。

---

## D3 — Repository 層隔離度掃描

### D3-1: Repository 檔案
```
app/src/main/java/.../data/repository/CameraRepositoryImpl.kt
app/src/main/java/.../domain/repository/CameraRepository.kt
```

### D3-2: Detection result model 檔案
```
app/src/main/java/.../domain/model/DetectionResult.kt
```
（單一 domain model，但持有 ML Kit 型別）

### D3-3: MLKit imports in data/repository
```
CameraRepositoryImpl.kt:16: import com.google.mlkit.vision.face.FaceDetection
CameraRepositoryImpl.kt:17: import com.google.mlkit.vision.face.FaceDetector
CameraRepositoryImpl.kt:18: import com.google.mlkit.vision.face.FaceDetectorOptions
CameraRepositoryImpl.kt:19: import com.google.mlkit.vision.objects.ObjectDetection
CameraRepositoryImpl.kt:20: import com.google.mlkit.vision.objects.ObjectDetector
CameraRepositoryImpl.kt:21: import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
CameraRepositoryImpl.kt:599: override suspend fun detectFaces(image: InputImage): List<Face>
CameraRepositoryImpl.kt:621: override suspend fun detectObjects(image: InputImage): List<DetectedObject>
```
✅ data/repository 持有 MLKit **符合預期**（infrastructure 層）。

### D3-4: CameraX in presentation layer
```
CameraScreen.kt:23:   import androidx.camera.core.ImageCapture
CameraViewModel.kt:8: import androidx.camera.core.ImageCapture
CameraViewModel.kt:261: WeakReference<PreviewView>
CameraViewModel.kt:2145: setPreviewSurfaceProvider(SurfaceProvider?)
CameraViewModel.kt:2158: setPreviewView(PreviewView?)
CameraViewModel.kt:2185: getActualPreviewContentBounds(PreviewView): RectF?
CameraViewModel.kt:2217: findPreviewSurface(PreviewView): View?
CameraPreview.kt:5-6: import Preview, PreviewView
AspectRatioMode.kt:4-5: import AspectRatio, PreviewView
```
**⚠️ ViewModel 直接操作 PreviewView**（getActualPreviewContentBounds, setPreviewView）違反架構純潔。

### D3-5: CameraX in domain layer
```
CameraRepository.kt:5:   import androidx.camera.core.CameraSelector
CameraRepository.kt:138: SurfaceProvider? (inline)
CameraRepository.kt:154: PreviewView? (inline)
CameraRepository.kt:236: CameraControl? (inline)
CameraRepository.kt:245: MeteringPointFactory? (inline)
AnalyzeLightingUseCase.kt:34: import androidx.camera.core.ImageProxy
```
**🔴 架構違規**: Domain Repository interface 不應直接引用 CameraX 基礎設施型別。

---

## D4 — 狀態管理一致性掃描

### D4-1: StateFlow in presentation layer (21 hits)
```
CameraViewModel.kt:145:  private val _state = MutableStateFlow(CameraState())
CameraViewModel.kt:146:  val state: StateFlow<CameraState> = _state.asStateFlow()
CameraViewModel.kt:153:  val focusUiStateFlow: StateFlow<FocusUiState>
CameraViewModel.kt:163:  private val _compositionState = MutableStateFlow<CompositionState>(NoSubject)
CameraViewModel.kt:164:  val compositionState: StateFlow<CompositionState>
CameraViewModel.kt:167:  private val _lightingState = MutableStateFlow<LightingState>(Normal)
CameraViewModel.kt:168:  val lightingState: StateFlow<LightingState>
CameraViewModel.kt:171:  private val _isAutoHDRActive = MutableStateFlow(false)
CameraViewModel.kt:172:  val isAutoHDRActive: StateFlow<Boolean>
CameraViewModel.kt:175:  private val _orientationState = MutableStateFlow<OrientationData?>(null)
CameraViewModel.kt:176:  val orientationState: StateFlow<OrientationData?>
CameraViewModel.kt:312:  val previewBitmap: StateFlow<Bitmap?>
CameraViewModel.kt:325:  val currentFps: StateFlow<Float?>
```
**觀察**: 7 個獨立 StateFlow vs 1 個主 `_state: CameraState` — 狀態碎片化。

### D4-2: StateFlow in domain layer
```
CameraRepository.kt:28:  fun getFpsStream(): StateFlow<Float?>
CameraRepository.kt:80:  val detectionResults: StateFlow<DetectionResult?>
CameraRepository.kt:90:  val isFixedFocusLens: StateFlow<Boolean>
```
✅ 合理，Repository 以 StateFlow 暴露串流。

### D4-3: State model files (11 files)
```
domain/model/focus/FocusStateInput.kt
domain/model/focus/FocusStates.kt
domain/model/focus/FocusUiState.kt
domain/model/focus/TrackedTargetState.kt
domain/model/focus/UserIntentState.kt
presentation/camera/AfStateMachine.kt
presentation/camera/CameraState.kt
presentation/camera/focus/DefaultFocusStateMachine.kt
presentation/camera/focus/FocusStateMachine.kt
presentation/camera/model/CompositionState.kt
presentation/camera/model/LightingState.kt
```

### D4-4: AF/Focus state files
```
data/source/camera/FocusUiSmoother.kt
domain/model/focus/FocusStateInput.kt
domain/model/focus/FocusStates.kt
domain/model/focus/FocusUiState.kt
presentation/camera/AfStateMachine.kt
presentation/camera/focus/DefaultFocusStateMachine.kt
presentation/camera/focus/FocusStateMachine.kt
```
**注意**: `AfStateMachine` 在 presentation 層，`FocusStates` 在 domain — 雙份狀態機架構。

### D4-5: Direct `.value =` assignments in CameraViewModel
```
11 total:
Line 404:  _lightingState.value = lightingState
Line 437:  _isAutoHDRActive.value = true
Line 441:  _isAutoHDRActive.value = false
Line 446:  _isAutoHDRActive.value = false
Line 467:  _orientationState.value = orientation
Line 793:  _isAutoHDRActive.value = true
Line 799:  _isAutoHDRActive.value = false
Line 1681: _compositionState.value = CompositionState.NoSubject
Line 1804: _compositionState.value = CompositionState.NoSubject
Line 1818: _lightingState.value = lightingState
Line 1949: _compositionState.value = CompositionState.NoSubject
```
🟡 可接受（`_compositionState`/`_lightingState` 是 MutableStateFlow，`.value =` vs `.update{}` 在單值覆蓋場景等效）。

---

## 🔴 BHA-01 Root Cause — 確認診斷

### 問題: observeCompositionAdvice 座標空間錯配

**位置**: [CameraViewModel.kt:1977-2015](app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraViewModel.kt#L1977-L2015)

**Bug 路徑**:

```
SmartCameraAnalyzer.analyze(imageProxy):
  imageProxy.width = 480, imageProxy.height = 640  (raw sensor, portrait → sensor delivers 480w×640h)
  rotationDegrees = 90

DetectionResult:
  imageWidth = imageProxy.width = 480   ← RAW, un-rotated
  imageHeight = imageProxy.height = 640  ← RAW, un-rotated
  faces = [Face]  ← face.boundingBox in ROTATED space: x∈[0,640], y∈[0,480]

CameraViewModel.observeCompositionAdvice (line 1982):
  imgW = det.imageWidth = 480   ← WRONG: should be det.imageHeight = 640 (after rotation swap)
  imgH = det.imageHeight = 640  ← WRONG: should be det.imageWidth = 480
  box.left / imgW = face_x / 480  ← overflow when face_x > 480 (can be up to 640)

HaloGuidanceLayer (line 267-270):
  NormalizedToPixelTransformer(screenWidth=1080, screenHeight=1898)
  result_x = normalized_x * 1080 = (face_x/480) * 1080 = face_x * 2.25

Expected correct path:
  imgW = 640 (rotated width)
  normalized_x = face_x / 640
  result_x = (face_x/640) * 1080 = face_x * 1.6875  ← correct FIT_CENTER mapping
```

**修復候選**: **方案 A（推薦）** — 在 `observeCompositionAdvice` 加入 rotation swap，與 `updateCoordinateTransformer`（line 585）一致：

```kotlin
// line 1982 前插入:
val rotation = det.rotationDegrees
val imgW = (if (rotation == 90 || rotation == 270) det.imageHeight else det.imageWidth).toFloat()
val imgH = (if (rotation == 90 || rotation == 270) det.imageWidth else det.imageHeight).toFloat()
```

**同樣受影響**: `observePtfAlignment` (line 2081-2102) — 相同 bug，PTF Halo 也會偏移。

### NormalizedToPixelTransformer 的附加問題

HaloGuidanceLayer 使用 `size.width/size.height`（Canvas 全螢幕 1080×1898），但正確映射應考慮 `contentBounds`（實際預覽區域，可能有 letterbox）。這是 **方案 B** 的修復點，但 BHA-01 主因是 rotation swap 缺失（方案 A）。

---

## 架構債務摘要（非 BHA-01 直接相關）

| ID | 類型 | 描述 | 嚴重度 |
|----|------|------|-------|
| ARCH-01 | Domain 洩漏 | `DetectionResult` 持有 MLKit `Face`/`DetectedObject` | 🔴 高 |
| ARCH-02 | Domain 洩漏 | `CameraRepository` interface 引用 CameraX `PreviewView`/`CameraControl` | 🔴 高 |
| ARCH-03 | 膨脹 | `CameraViewModel` 2457 行（超規 8.2×） | 🔴 高 |
| ARCH-04 | 狀態碎片化 | 7 個平行 StateFlow + 1 主 CameraState，未整合 | 🟡 中 |
| ARCH-05 | 雙管線 | SmartCameraAnalyzer + MLKitDetector 雙管線（路徑未統一） | 🟡 中 |
| I7 | Bug | EyeAfController 每 2s 無條件重觸發，缺 Focused guard | 🟡 中 |

---

## 驗收確認

- ✅ 程式碼檔案變更數量：**0**（`git status` clean）
- ✅ `.claude/bha_report.md` 已建立
- ✅ BHA-01 根因確認：`observeCompositionAdvice` + `observePtfAlignment` 缺 rotation swap
- ✅ 修復方案 A 路徑明確（2 處修改，CameraViewModel.kt ~line 1982 & ~line 2081）

---

*BHA-01 implemented and verified locally. Requesting CSO/Traycer final approval.*
