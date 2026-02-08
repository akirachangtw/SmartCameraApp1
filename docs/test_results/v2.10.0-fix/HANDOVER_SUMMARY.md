# v2.10.0 Aspect Ratio WYSIWYG Fix - Handover Summary

**Date**: 2026-02-08
**Version**: v2.10.0-fix
**Branch**: `feature/aspect-ratio-usecasegroup-v2.10.0`
**Status**: ✅ **COMPLETED - All Issues Fixed**

---

## 📋 Summary

Successfully fixed two critical aspect ratio issues in v2.10.0 implementation:

1. ✅ **1:1 WYSIWYG破壞** - Preview shows square but Capture outputs 3:4 ratio
2. ✅ **16:9 FOV過度裁切** - Field of view 30-40% narrower than OEM camera

**Cross-device validation**: Sony XQ-BT52 + Google Pixel 9a ✅

---

## 🔧 Technical Changes

### 1. ViewPort.FIT（Fix 16:9 FOV Over-cropping）

**File**: `LiveCameraSource.kt` (Line 932-940)

```kotlin
val viewPort = ViewPort.Builder(
    currentAspectRatioMode.toRational(),
    currentRotation
)
    .setScaleType(ViewPort.FIT)  // ✅ NEW: Preserve full FOV
    .build()
```

**Impact**: Prevents double-cropping (ViewPort FILL_CENTER + PreviewView FILL_CENTER)

---

### 2. Unified PreviewView.FIT_CENTER（Fix Display Consistency）

**File**: `AspectRatioMode.kt` (Line 132-133)

```kotlin
val toScaleType: PreviewView.ScaleType
    get() = PreviewView.ScaleType.FIT_CENTER  // ✅ NEW: Unified for all modes
```

**Critical Discovery**: PreviewView.ScaleType has **HIGHER priority** than ViewPort.ScaleType. The original 16:9 mode used FILL_CENTER, which overrode ViewPort.FIT and caused screen-filling behavior.

---

### 3. ResolutionFilter for 1:1（Fix Square Output）

**File**: `LiveCameraSource.kt` (Line 868-888)

```kotlin
val commonSelector = if (currentAspectRatioMode == AspectRatioMode.RATIO_1_1) {
    // ✅ NEW: Use ResolutionFilter to find actual square resolutions
    ResolutionSelector.Builder()
        .setResolutionFilter { supportedSizes, _ ->
            val squareOrNearSquare = supportedSizes.sortedBy { size ->
                kotlin.math.abs(size.width.toFloat() / size.height - 1.0f)
            }
            listOf(squareOrNearSquare.firstOrNull() ?: supportedSizes.first())
        }
        .build()
} else {
    // 4:3/16:9 use standard AspectRatioStrategy
    ResolutionSelector.Builder()
        .setAspectRatioStrategy(
            AspectRatioStrategy(targetAspectRatio, FALLBACK_RULE_AUTO)
        )
        .build()
}
```

**Why Not Gemini Approach**: ViewPort(1:1) only defines **logical crop region**, does NOT affect ImageCapture's actual output file resolution.

---

## 📊 Test Results

### Sony XQ-BT52 (21:9 Screen)

| Mode | Resolution | Ratio | Status |
|------|-----------|-------|--------|
| 4:3  | 1920 x 1440 | 1.333 | ✅ |
| 16:9 | 3840 x 2160 | 1.778 | ✅ |
| 1:1  | 2448 x 2448 | 1.000 | ✅ |

**Letterbox**: 4:3/16:9 上下黑邊, 1:1 左右黑邊 ✅

---

### Google Pixel 9a (20:9 Screen)

| Mode | Resolution | Ratio | Status |
|------|-----------|-------|--------|
| 4:3  | 4000 x 3000 | 1.333 | ✅ |
| 16:9 | 3840 x 2160 | 1.778 | ✅ |
| 1:1  | 1920 x 1920 | 1.000 | ✅ |

**Letterbox**: 4:3/16:9 上下黑邊, 1:1 左右黑邊 ✅

---

## 🔍 Critical Discoveries

### Discovery 1: ViewPort Crop ≠ ImageCapture Output
**Problem**: Original Gemini approach assumed ViewPort(1:1) would automatically crop ImageCapture output.
**Reality**: ViewPort only defines **logical crop region** for WYSIWYG coordination between Preview/Analysis/Capture.
**Solution**: Must use ResolutionFilter to select actual square resolutions.

### Discovery 2: PreviewView.ScaleType Priority
**Problem**: ViewPort.FIT was set, but 16:9 still filled screen.
**Root Cause**: `PreviewView.ScaleType` has **HIGHER priority** than `ViewPort.ScaleType`.
**Solution**: Unified all modes to `PreviewView.FIT_CENTER`.

### Discovery 3: Device-Specific Resolutions
**Finding**: Different devices support different square resolutions.
**Evidence**: Sony (2448²) vs Pixel 9a (1920²)
**Validation**: ResolutionFilter's fallback mechanism works reliably across devices.

---

## 📁 Files Modified

### Core Implementation
- `app/src/main/java/.../data/source/camera/LiveCameraSource.kt`
  - Line 868-888: ResolutionFilter for 1:1 mode
  - Line 932-940: ViewPort.setScaleType(FIT)

- `app/src/main/java/.../presentation/camera/model/AspectRatioMode.kt`
  - Line 132-133: Unified PreviewView.FIT_CENTER

- `app/build.gradle.kts`
  - Version tracking changes

### Documentation
- `docs/plan/v2.10.0_aspect_ratio_wysiwyg_fix_L1.md` - Level 1 planning
- `docs/plan/v2.10.0_aspect_ratio_wysiwyg_fix_L2.md` - Level 2 feasibility
- `docs/plan/v2.10.0_SOLUTION_COMPARISON.md` - Traycer vs Gemini comparison
- `docs/test_results/v2.10.0-fix/SONY_XQ-BT52_TEST_REPORT.md` - Sony test results
- `docs/test_results/v2.10.0-fix/CROSS_DEVICE_TEST_REPORT.md` - Final cross-device report
- `chat_logs/handofflog_20260208_235900.md` - Original handoff document

---

## ✅ Verification Checklist

- [x] 1:1 WYSIWYG restored (Preview square → Capture square)
- [x] 16:9 FOV preserved (no over-cropping, matches OEM camera)
- [x] All modes display correct letterbox behavior
- [x] Cross-device testing (Sony + Pixel 9a)
- [x] Aspect ratio switching stable (20+ cycles, no crashes)
- [x] Photo capture 100% success rate
- [x] Logcat evidence collected

---

## 🎯 Next Steps

### Immediate Actions
- [x] Cross-device testing completed
- [x] Documentation updated
- [ ] Git commit with test evidence
- [ ] Consider PR to main branch

### Optional Enhancements
- [ ] Add device resolution database (log which devices select which 1:1 resolution)
- [ ] UI hint for first-time users (explain letterbox is correct)
- [ ] Performance profiling for 1:1 mode on lower-end devices
- [ ] Test on additional devices (Samsung, OnePlus, etc.)

---

## 📞 Contact

**Implementation**: Claude Sonnet 4.5 + User
**Test Devices**:
- Sony XQ-BT52 (HQ616R2713)
- Google Pixel 9a (59131JEBF17244)

**Test Date**: 2026-02-08
**Status**: ✅ Production Ready

---

## 📚 Related Documents

- [Sony XQ-BT52 Test Report](./SONY_XQ-BT52_TEST_REPORT.md)
- [Cross-Device Test Report](./CROSS_DEVICE_TEST_REPORT.md)
- [Level 1 Planning](../../plan/v2.10.0_aspect_ratio_wysiwyg_fix_L1.md)
- [Level 2 Feasibility](../../plan/v2.10.0_aspect_ratio_wysiwyg_fix_L2.md)
- [Solution Comparison](../../plan/v2.10.0_SOLUTION_COMPARISON.md)
- [Original Handoff](../../../chat_logs/handofflog_20260208_235900.md)
