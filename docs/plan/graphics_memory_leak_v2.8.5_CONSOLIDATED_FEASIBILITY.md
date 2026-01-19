# Graphics Memory Leak - Consolidated Feasibility Review (v2.8.5)

**Generated**: 2026-01-10 17:10:00
**Based on**: L1/L2 plans + 3 previous chat logs analysis

---

## Executive Summary

### 🎯 Root Cause Identified
**PRIMARY CULPRIT**: Compose Bitmap Preview Path
**EVIDENCE**: Graphics PSS 116MB → 223MB (+91%), while Native Heap/Private Other remain stable

Based on triangulation of:
1. **L2 Plan**: Systematic architecture analysis
2. **v2.8.5_pixel9a_memory_analysis.md**: Previous deep-dive findings
3. **v2.8.5_pixel9a-memory-issue.md**: 4 verification comments
4. **v2.8.5_Pixel9a-Memory-Mitigation.md**: Initial mitigation plan

### 🔴 Critical Finding from Previous Analysis
**NOT ML Kit or ImageProxy leak**, but **Compose Bitmap-to-Texture pipeline**:

```kotlin
// CameraScreen.kt (line 148): subscribes to previewBitmap every frame
// CameraPreview.kt (line 41): Image(bitmap.asImageBitmap()) per frame
// LiveCameraSource.kt (line 172): ImageProxy → Bitmap at 30 FPS
```

Even though ML analysis is throttled to 2 FPS on Pixel 9a, **preview stream still runs at 30 FPS** pushing Bitmaps to UI → each frame creates texture upload/cache → Graphics memory bloat.

---

## Feasibility Matrix

| Solution | Complexity | Risk | Impact | Timeline | Recommendation |
|----------|------------|------|--------|----------|----------------|
| **Phase 0**: A/B Evidence Collection | Low | Low | High | 0.5-1 day | ✅ **DO FIRST** |
| **Phase 1**: Lifecycle Cleanup | Low-Med | Low | Medium | 1-2 days | ✅ **MUST DO** |
| **Phase 2**: PreviewView Migration | Medium | Medium | **Very High** | 2-4 days | ✅ **ROOT FIX** |
| **Phase 3**: Bitmap Ring Buffer | Medium | Medium | Low | 2-3 days | ⚠️ **FALLBACK** |

---

## Phase 0: Evidence Collection (A/B Experiment) ✅ PRIORITY 1

### Goal
Prove Graphics growth comes from **"Compose Bitmap Preview"** vs **"ML/Overlay/Surface"**

### A/B Test Matrix
| Test | Camera Bind | ImageAnalysis | Bitmap Preview | Overlay | Expected Result |
|------|-------------|---------------|----------------|---------|-----------------|
| A    | ✅          | ✅            | ❌ (black screen) | ❌ | Graphics should NOT grow |
| B    | ✅          | ✅            | ✅             | ❌ | Graphics WILL grow |
| C    | ✅          | ❌ (pause)    | ✅             | ✅ | Graphics still grows (confirms preview issue) |

### Implementation
```kotlin
// Add debug flag in CameraViewModel.kt
companion object {
    private const val DEBUG_DISABLE_BITMAP_PREVIEW = false  // A/B switch
}

// In CameraScreen.kt
if (!DEBUG_DISABLE_BITMAP_PREVIEW) {
    CameraPreview(bitmap = previewBitmap)
} else {
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Text("Preview Disabled (A/B Test)", color = Color.White)
    }
}
```

### Observability Metrics (Debug Only)
```kotlin
// LiveCameraSource.kt - track SurfaceTexture lifecycle
private var surfaceTextureCreateCount = 0
private var surfaceTextureReleaseCount = 0

// CameraPreview.kt - track Bitmap identity churn
private val bitmapIdentities = mutableSetOf<Int>()
LaunchedEffect(bitmap) {
    bitmapIdentities.add(System.identityHashCode(bitmap))
    if (bitmapIdentities.size % 60 == 0) {
        Log.d("BitmapChurn", "Unique bitmaps in 1 min: ${bitmapIdentities.size}")
    }
}
```

### Pass Criteria
- **Test A**: Graphics < 150MB after 10 min (proves ML/Surface not the issue)
- **Test C**: Graphics still grows (confirms preview is the culprit)

---

## Phase 1: Lifecycle Cleanup ✅ PRIORITY 2

### Goal
Prevent **"page navigation leaks"** where Surface/UseCase stack up on background/foreground cycles

### Critical Gaps Identified

#### 1. CameraScreen.kt - Missing DisposableEffect
**Current**: Only `bind()`, no `unbind()`
**Risk**: Each navigation/background creates new Surface without releasing old one

```kotlin
// ADD in CameraScreen.kt
@Composable
fun CameraScreen(viewModel: CameraViewModel = hiltViewModel()) {
    DisposableEffect(Unit) {
        onDispose {
            viewModel.unbindCamera()  // NEW API needed
        }
    }
    // ... existing code
}
```

#### 2. CameraViewModel.kt - Missing onCleared()
**Current**: ViewModel never releases resources
**Risk**: Repository holds Surface/UseCase references after ViewModel destroyed

```kotlin
// ADD in CameraViewModel.kt
override fun onCleared() {
    super.onCleared()
    cameraRepository.releaseCamera()  // NEW API needed
    analysisScope.cancel()
}
```

#### 3. LiveCameraSource.kt - Unbind not guaranteed
**Existing**: `unbind()` exists at line 552, but **not called from ViewModel/UI**
**Fix**: Wire up call chain

```kotlin
// CameraRepositoryImpl.kt - ADD
suspend fun releaseCamera() {
    currentSource.value?.let { source ->
        if (source is LiveCameraSource) {
            source.unbind()  // ← Already implemented, just not called!
        }
    }
}
```

### Files to Modify
1. [CameraScreen.kt](app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraScreen.kt) - Add `DisposableEffect`
2. [CameraViewModel.kt](app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraViewModel.kt) - Add `onCleared()`, `unbindCamera()`
3. [CameraRepositoryImpl.kt](app/src/main/java/com/akirachang/ai/photographer/data/repository/CameraRepositoryImpl.kt) - Add `releaseCamera()`

### Verification
```bash
# Test: Background → Foreground 10 times
adb shell dumpsys meminfo com.akirachang.ai.photographer | grep -E "Graphics|TOTAL PSS"
# Expected: Graphics should DROP or MAINTAIN after each return to foreground
```

---

## Phase 2: PreviewView Migration (Root Fix) ✅ PRIORITY 3

### Goal
Eliminate **"Bitmap → Texture upload"** path by using native CameraX Surface

### Architecture Change

#### Before (Current - Inefficient)
```
CameraX Preview → customSurfaceProvider → SurfaceTexture
  ↓ (30 FPS)
ImageProxy → YUV→RGB Bitmap → previewBitmap StateFlow
  ↓
Compose Image(bitmap.asImageBitmap()) → GPU Texture Upload
  ↓
Graphics Memory Churn (116MB → 223MB)
```

#### After (Target - Efficient)
```
CameraX Preview → PreviewView.surfaceProvider → SurfaceTexture
  ↓ (Direct GPU rendering via SurfaceView/TextureView)
Screen Display (No CPU conversion, No Bitmap)
  ↓
Graphics Memory: Stable ~120MB
```

### Implementation Strategy

#### 1. UI Layer - Use PreviewView
```kotlin
// CameraPreview.kt - REPLACE Compose Image with AndroidView
@Composable
fun CameraPreview(
    previewView: PreviewView,  // Pass from CameraScreen
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

// CameraScreen.kt - Create PreviewView
val previewView = remember { PreviewView(context) }
viewModel.setPreviewView(previewView)  // NEW
```

#### 2. Data Layer - Remove Bitmap Stream
```kotlin
// LiveCameraSource.kt - MODIFY bindToLifecycle()
fun bindToLifecycle(
    lifecycleOwner: LifecycleOwner,
    surfaceProvider: Preview.SurfaceProvider  // ← From PreviewView
): Flow<CameraState> {
    preview.setSurfaceProvider(surfaceProvider)  // Use UI's provider
    // REMOVE: customSurfaceProvider
    // REMOVE: getPreviewStream() bitmap conversion
}
```

#### 3. Conditional Compilation - Keep Emulator Support
```kotlin
// DeviceConfig.kt - NEW
object DeviceConfig {
    val useNativePreview: Boolean
        get() = Build.FINGERPRINT.contains("generic").not()  // Real device only
}

// CameraScreen.kt
if (DeviceConfig.useNativePreview) {
    CameraPreview(previewView = previewView)  // PreviewView path
} else {
    CameraPreview(bitmap = previewBitmap)  // Legacy Bitmap path (emulator)
}
```

### Files to Modify
1. [CameraPreview.kt](app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraPreview.kt) - Add `AndroidView` path
2. [CameraScreen.kt](app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraScreen.kt) - Create `PreviewView`
3. [CameraViewModel.kt](app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraViewModel.kt) - Add `setPreviewView()`
4. [LiveCameraSource.kt](app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt) - Accept external `SurfaceProvider`

### Expected Impact
- **Graphics Memory**: 116MB → ~120MB stable (no growth)
- **CPU Usage**: -30~40% (no YUV→RGB conversion for preview)
- **FPS**: Smoother (GPU direct rendering)

### Risks & Mitigation
| Risk | Mitigation |
|------|------------|
| PreviewView black screen | Keep legacy Bitmap path as fallback |
| Overlay rendering broken | Use `View.setOnDrawListener()` or Compose overlay above `AndroidView` |
| Emulator incompatibility | Feature flag to use Bitmap path on emulator |

---

## Phase 3: Bitmap Ring Buffer (Fallback) ⚠️ ONLY IF Phase 2 Blocked

### Goal
If PreviewView migration blocked (e.g., tight deadline), reduce texture churn via fixed Bitmap pool

### Implementation
```kotlin
// CameraViewModel.kt
private val bitmapRingBuffer = Array(3) {
    Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
}
private var currentBitmapIndex = 0

fun updatePreviewBitmap(source: Bitmap) {
    val targetBitmap = bitmapRingBuffer[currentBitmapIndex]
    source.copyPixelsTo(targetBitmap)  // Reuse existing bitmap
    _previewBitmap.value = targetBitmap
    currentBitmapIndex = (currentBitmapIndex + 1) % 3
}
```

### Expected Impact
- Graphics Memory: 116MB → ~160MB (reduced but not eliminated)
- Complexity: Medium (need to manage lifecycle)
- **Verdict**: Sub-optimal, prefer Phase 2

---

## Previous Verification Comments Status

From [v2.8.5_pixel9a-memory-issue.md](chat_logs/v2.8.5_pixel9a-memory-issue.md):

| # | Comment | Status | Notes |
|---|---------|--------|-------|
| 1 | Pixel 9a analysis still 10 FPS despite warning 2 FPS | ⚠️ **UNRESOLVED** | `observePreviewForAnalysis()` needs device-specific throttle |
| 2 | RenderScript not disabled for Pixel 9a | ⚠️ **UNRESOLVED** | `useRenderScript` needs Pixel 9a gate |
| 3 | ImageAnalysis config not relaxed (backpressure) | ⚠️ **UNRESOLVED** | Use `STRATEGY_BLOCK_PRODUCER` for Pixel 9a |
| 4 | ML Kit detectors not downgraded | ⚠️ **UNRESOLVED** | Apply safer options (increase `minFaceSize`, disable tracking) |

**Recommendation**: Address Comments 1-4 in **Phase 1** alongside lifecycle cleanup (low-risk, high-value quick wins).

---

## Testing Matrix

### Devices
| Priority | Device | Android | Purpose |
|----------|--------|---------|---------|
| P0 | Pixel 9a | 16 | Primary test (known issue device) |
| P1 | Sony/Other | 13-15 | Regression check |
| P2 | Emulator | 16 | Build/basic sanity |

### Test Scenarios
| ID | Scenario | Duration | Success Criteria |
|----|----------|----------|------------------|
| T1 | Continuous preview (no navigation) | 15 min | Graphics < 150MB stable after 2-min warmup |
| T2 | Background ↔ Foreground cycle | 15 min (1 cycle/min) | Graphics not stepping up each cycle |
| T3 | Front ↔ Back camera switch | 10 min (1 switch/min) | Graphics drops/maintains on each switch |

### Metrics Collection
```bash
# Baseline snapshot
adb shell dumpsys meminfo com.akirachang.ai.photographer > baseline.txt

# Continuous monitoring (30s interval)
while true; do
  echo "$(date +%H:%M:%S)" >> monitor.txt
  adb shell dumpsys meminfo com.akirachang.ai.photographer | \
    grep -E "Graphics|TOTAL PSS" >> monitor.txt
  sleep 30
done

# Graphics info (texture churn)
adb shell dumpsys gfxinfo com.akirachang.ai.photographer
```

---

## Rollout Strategy

### Phase 0: Evidence (Week 1, Day 1-1)
- [ ] Add A/B test flags
- [ ] Run 3 test scenarios on Pixel 9a
- [ ] Confirm Bitmap preview is root cause
- [ ] Document findings in `tmp/phase0_ab_test_results.md`

### Phase 1: Quick Wins (Week 1, Day 2-3)
- [ ] Add lifecycle cleanup (`DisposableEffect`, `onCleared()`)
- [ ] Fix 4 verification comments (throttle, RenderScript, etc.)
- [ ] Test T2 (background/foreground) on Pixel 9a
- [ ] Tag: `v2.8.6-alpha1` (lifecycle fixes only)

### Phase 2: Root Fix (Week 1-2, Day 4-7)
- [ ] Implement PreviewView path
- [ ] Feature flag: `USE_NATIVE_PREVIEW` (default true for real devices)
- [ ] Test T1/T2/T3 on Pixel 9a + Sony
- [ ] Tag: `v2.8.6-beta1` (PreviewView migration)

### Phase 3: Validation (Week 2, Day 8-10)
- [ ] Extended soak test (1 hour continuous)
- [ ] Multi-device regression (3+ devices)
- [ ] Performance comparison vs v2.8.5
- [ ] Tag: `v2.8.6-rc1` (release candidate)

### Rollback Plan
```kotlin
// Feature flags for gradual rollout
object FeatureFlags {
    const val ENABLE_NATIVE_PREVIEW = true        // Phase 2 main switch
    const val ENABLE_LIFECYCLE_CLEANUP = true     // Phase 1 (low-risk)
    const val PIXEL9A_MITIGATIONS = true          // v2.8.5 settings (keep until proven)
}

// Per-phase rollback:
// - Phase 0: Just remove logs (no code change)
// - Phase 1: Flip ENABLE_LIFECYCLE_CLEANUP = false
// - Phase 2: Flip ENABLE_NATIVE_PREVIEW = false (fallback to Bitmap path)
```

---

## Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Phase 2 breaks emulator | High | Medium | Keep Bitmap path for emulator |
| PreviewView black screen on some devices | Medium | High | Feature flag + device whitelist |
| Overlay rendering incompatible with AndroidView | Medium | Medium | Compose overlay above AndroidView |
| Performance regression on low-end devices | Low | Medium | A/B test on multiple devices |
| Timeline overrun | Low | Low | Phase 3 (ring buffer) as emergency fallback |

---

## Dependencies & Constraints

### Required APIs (Already in Gradle)
- ✅ CameraX Preview 1.3.0+ (has PreviewView)
- ✅ Jetpack Compose 1.5+ (has AndroidView)

### Team Capacity
- **Phase 0**: 0.5 day (can start today)
- **Phase 1**: 1-2 days (parallel with Phase 0 analysis)
- **Phase 2**: 2-4 days (requires UI/Data coordination)
- **Total**: ~1 week (aggressive) to 2 weeks (conservative)

### External Blockers
- None identified (all components available in current stack)

---

## Recommendation Summary

### ✅ Immediate Actions (Today)
1. **START Phase 0**: Add A/B test flags, run evidence collection
2. **PREPARE Phase 1**: Review lifecycle cleanup code locations

### ✅ Week 1 Execution
- **Days 1-3**: Phase 0 + Phase 1 (parallel)
- **Days 4-7**: Phase 2 implementation + initial testing

### ✅ Week 2 Validation
- **Days 8-10**: Multi-device testing, soak tests, RC tagging

### 🎯 Success Criteria
- Pixel 9a Graphics memory: 116MB → **< 130MB stable** (10% growth allowance)
- No background/foreground step-up pattern
- FPS maintained or improved vs v2.8.5
- Zero regression on Sony/other devices

---

## Appendix: Key File Locations

### Phase 1 (Lifecycle)
- [CameraScreen.kt:148](app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraScreen.kt#L148) - Add `DisposableEffect`
- [CameraViewModel.kt](app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraViewModel.kt) - Add `onCleared()`, fix line 463 throttle
- [LiveCameraSource.kt:552](app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt#L552) - Verify `unbind()` called

### Phase 2 (PreviewView)
- [CameraPreview.kt:41](app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraPreview.kt#L41) - Replace with `AndroidView`
- [LiveCameraSource.kt:172](app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt#L172) - Remove Bitmap stream

### Verification Comments (Phase 1)
- [CameraViewModel.kt:463](app/src/main/java/com/akirachang/ai/photographer/presentation/camera/CameraViewModel.kt#L463) - `observePreviewForAnalysis()` throttle
- [LiveCameraSource.kt](app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt) - RenderScript gate, ImageAnalysis backpressure
- [MLKitDetector.kt](app/src/main/java/com/akirachang/ai/photographer/data/source/ml/MLKitDetector.kt) - Detector options downgrade

---

**Next Step**: User decision on execution priority (recommend Phase 0 → Phase 1 → Phase 2)
