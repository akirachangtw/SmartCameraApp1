# APK Analyzer 16KB Warning Analysis

**Date**: 2026-01-27
**APK**: app-device-debug.apk (v2.9.6-phase2.5)
**Tool**: Android Studio APK Analyzer
**Source**: 16kb-warning-list.png

---

## Executive Summary

Android Studio's APK Analyzer has identified **multiple native libraries with 16KB page alignment warnings**.

**Key Finding**: The warnings are **NOT limited to ML Kit** libraries as previously thought. The issue is **more widespread** across multiple dependency categories.

---

## Warning Categories from Screenshot

Based on the visible orange warning indicators in the APK Analyzer tree:

### Category 1: ML Kit Libraries (Google Play Services)
These were already identified in Phase 2.5:
- ❌ `libface_detector_v2_jni.so` (7.6 MB)
- ❌ `libmlkitcommonpipeline.so` (8.9 MB)
- Possibly: `libimage_processing_util_jni.so` (29 KB)

**Status**: Waiting for Google ML Kit update

### Category 2: AndroidX/Jetpack Libraries
Visible warnings in the tree structure:
- ❌ `libandroidx.graphics.path.so` (10 KB)
- ❌ `libdatastore_shared_counter.so` (7 KB)
- Possibly: `libsurface_util_jni.so` (4.8 KB)

**Status**: Should be fixed by AndroidX library updates

### Category 3: System/Runtime Libraries
- ❌ `libc++_shared.so` (1.3 MB) - C++ standard library

**Status**: May be fixed by NDK/build tools update

### Category 4: OpenCV (Custom Build)
- Status from screenshot: **Unclear** (need to verify if opencv_jni_shared has warnings)

**Our Control**: High - We compile this ourselves

---

## Critical Analysis: ZIP vs ELF Alignment Discrepancy

### The Paradox

**Fact 1**: `zipalign -c -P 16` reports **all 8 libraries are 16KB aligned** ✅

**Fact 2**: Android Studio APK Analyzer shows **multiple libraries with warnings** ❌

**Fact 3**: Pixel 9a physical device shows **PageSizeMismatchDialog** ❌

**Fact 4**: Pixel9a-16KB emulator shows **no warnings** ✅

### Resolution: Two Different Checks

#### Check 1: ZIP Entry Offset (What zipalign verifies)
```
zipalign -c -P 16 4 app.apk
```
**What it checks**: The **starting position of the file data** inside the ZIP archive.

**Result for our APK**: ✅ 8/8 libraries pass

**Example**:
```
libandroidx.graphics.path.so: offset 21,807,104 → 21,807,104 % 16384 = 0 ✅
```

#### Check 2: ELF LOAD Segment Alignment (What Android Studio/Runtime checks)
```
llvm-readelf -l library.so
```
**What it checks**: The **internal memory layout** of the binary file itself (p_offset, p_vaddr, p_align fields).

**Result for our APK**: ❌ Multiple libraries fail (as shown in APK Analyzer)

**Example from Phase 2.5**:
```
libface_detector_v2_jni.so:
  LOAD p_offset=0x00000000 p_vaddr=0x00000000 p_align=0x00001000 (4KB) ❌
```

### Why Both Matter

**ZIP Alignment** (APK packaging):
- Controls how files are stored in the APK
- Affects installation and extraction
- AGP 8.10.1 handles this ✅

**ELF LOAD Alignment** (Binary internal):
- Controls how the library is mapped into memory at runtime
- Affects actual page-aligned mmap() calls
- Must be fixed by **library maintainers** (Google, AndroidX, etc.)

**Android 16 Validation**: Checks **BOTH** layers
- ZIP layer: ✅ All pass (AGP fixed)
- ELF layer: ❌ Multiple fail (dependency issue)
- **Result**: Warning triggered by ELF failures

---

## Affected Libraries by Source

### 1. Google Play Services / ML Kit (External, No Control)
- `libface_detector_v2_jni.so`
- `libmlkitcommonpipeline.so`
- `libimage_processing_util_jni.so`

**Fix**: Wait for Google to update ML Kit SDK

**Current Version**: (Need to check `app/build.gradle`)

**Workaround**: None (required for face detection)

### 2. AndroidX/Jetpack (External, Partial Control)
- `libandroidx.graphics.path.so`
- `libdatastore_shared_counter.so`
- `libsurface_util_jni.so`

**Fix**: Upgrade AndroidX/Jetpack Compose libraries

**Current Version**: (Need to check `libs.versions.toml`)

**Workaround**: Check for alpha/beta releases with 16KB support

### 3. C++ Standard Library (NDK/Build Tools)
- `libc++_shared.so`

**Fix**: Upgrade NDK version or use static linking

**Current NDK**: (Need to check `gradle.properties` or `local.properties`)

**Workaround**:
- Option A: Upgrade NDK to latest (r27+)
- Option B: Use `c++_static` instead of `c++_shared`

### 4. OpenCV (Internal, Full Control)
- `libopencv_java4.so`

**Status**: Need to verify if this has warnings in screenshot

**Fix**: Recompile with proper flags (if needed)

**Current Flags**: Already using `-Wl,-z,max-page-size=16384` ✅

---

## Recommended Action Plan

### Phase 2.6.1: Immediate Assessment (1 hour)

1. **Extract exact warning list from APK Analyzer**
   ```bash
   # Export detailed report
   Android Studio → Build → Analyze APK → app-device-debug.apk
   → Right panel: Check all libraries with warnings
   → Document exact list
   ```

2. **Verify current dependency versions**
   - ML Kit: Check `app/build.gradle`
   - AndroidX: Check `libs.versions.toml`
   - NDK: Check `local.properties`
   - CameraX: Already at 1.4.2 ✅

3. **Check for available updates**
   - ML Kit: Search "ML Kit 16KB support" release notes
   - AndroidX: Check androidx.dev release notes
   - NDK: Check developer.android.com NDK downloads

### Phase 2.6.2: Quick Wins (2-4 hours)

1. **Upgrade AndroidX libraries** (if updates available)
   - Target: `androidx.graphics`, `androidx.datastore`
   - Test: Re-run APK Analyzer
   - Impact: May fix 2-3 libraries

2. **Upgrade NDK** (if r27+ available)
   - Update `local.properties` or `gradle.properties`
   - Rebuild OpenCV module
   - Test: Check `libc++_shared.so`

3. **Re-build and re-test**
   - Build new APK
   - Verify with zipalign + APK Analyzer
   - Test on Pixel 9a physical device

### Phase 2.6.3: ML Kit Migration Planning (If no updates available)

**Option A: MediaPipe Face Detection**
- Pros: Google-maintained, likely 16KB ready
- Cons: API differences, migration effort
- Time: 2-3 weeks

**Option B: TensorFlow Lite Direct**
- Pros: Lower-level control
- Cons: Need to manage models manually
- Time: 3-4 weeks

**Option C: Pure OpenCV Face Detection**
- Pros: Already integrated, no new dependencies
- Cons: Lower accuracy than ML Kit
- Time: 1 week (already have code)

---

## Updated Root Cause Statement

**Previous Understanding** (Phase 2.5):
> "ML Kit libraries (2/8) have ELF misalignment"

**Updated Understanding** (Phase 2.6):
> "Multiple libraries across ML Kit, AndroidX, and system dependencies have ELF LOAD segment misalignment, despite correct ZIP alignment. Android Studio APK Analyzer confirms widespread 16KB compliance issues beyond ML Kit."

**Implication**:
- More comprehensive dependency audit needed
- Multiple library updates required (not just ML Kit)
- May need NDK upgrade + rebuild

---

## Next Steps - User Decision Required

Based on this new information, I recommend:

1. **Immediate**: Extract complete warning list from APK Analyzer
2. **Short-term**: Check for library updates (ML Kit, AndroidX, NDK)
3. **Medium-term**: Upgrade all updatable dependencies and retest
4. **Long-term**: Plan ML Kit migration if no updates available

**Question for User**:
- Should I check current versions in `build.gradle` and `libs.versions.toml`?
- Should I search for latest ML Kit / AndroidX releases with 16KB support?
- Do you want me to create a dependency upgrade plan?

---

## References

- Screenshot: `docs/test_results/v2.9.6-Phase2.6/16kb-warning-list.png`
- Physical Test: `docs/test_results/v2.9.6-Phase2.6/Physical_Device_Test_Report.md`
- ZIP Verification: `docs/test_results/v2.9.6-Phase2.6/ZIP_Alignment_Verification_Report.md`
