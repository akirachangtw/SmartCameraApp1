# v2.9.6 Phase 2.6: ZIP Alignment Verification Report

**Date**: 2026-01-27
**APK Version**: v2.9.6-phase2.5 (Build 57)
**Test Environment**: Windows 11, AGP 8.10.1, Build Tools 36.1.0

---

## Executive Summary

**Critical Finding**: The original APK built by AGP 8.10.1 **IS ALREADY 16KB PAGE-ALIGNED**.

All 8 native libraries in `lib/arm64-v8a/` are correctly aligned to 16KB (16384 bytes) boundaries, as verified by `zipalign -c -P 16` tool.

This contradicts the Phase 2.5 conclusion that "AGP 8.10.1 automatic alignment ineffective".

---

## Verification Results

### Method 1: zipalign Check Mode

**Command**:
```bash
zipalign -c -v -P 16 4 app/build/outputs/apk/device/debug/app-device-debug.apk
```

**Result**: All .so files show **(OK)** status

| Library | File Data Offset | 16KB Aligned? |
|---------|-----------------|---------------|
| libandroidx.graphics.path.so | 21,807,104 | ✅ YES (mod 0) |
| libc++_shared.so | 21,823,488 | ✅ YES (mod 0) |
| libdatastore_shared_counter.so | 23,117,824 | ✅ YES (mod 0) |
| libface_detector_v2_jni.so | 23,134,208 | ✅ YES (mod 0) |
| libimage_processing_util_jni.so | 30,752,768 | ✅ YES (mod 0) |
| libmlkitcommonpipeline.so | 30,785,536 | ✅ YES (mod 0) |
| libopencv_java4.so | 39,649,280 | ✅ YES (mod 0) |
| libsurface_util_jni.so | 63,635,456 | ✅ YES (mod 0) |

**Alignment Rate**: 8/8 (100%) ✅

### Method 2: zipinfo Analysis

**Note**: zipinfo reports "local header offset", NOT file data offset.

The relationship is:
```
File Data Offset = Local Header Offset + 30 (fixed header) + filename_length + extra_field_length
```

Example for `libandroidx.graphics.path.so`:
- Local header offset: 21,792,296 (from zipinfo)
- Filename length: 42 bytes
- Extra field: 0 bytes
- Calculated data offset: 21,792,296 + 30 + 42 = 21,792,368
- **Actual data offset**: 21,807,104 (from zipalign -v)

The difference (14,736 bytes) is due to alignment padding inserted by AGP.

---

## Analysis: Why Phase 2.5 Appeared to Fail?

### Hypothesis 1: Wrong Verification Tool
Phase 2.5 likely used `zipinfo` to check "local header offset", which **does not reflect page alignment**.

Android runtime mmap() operates on **file data offset**, not header offset. AGP correctly aligns the data by adding padding between the header and data.

### Hypothesis 2: ELF vs ZIP Confusion
Phase 2.5 focused on ELF LOAD segment alignment (using `llvm-readelf`), which is a **separate layer** from ZIP alignment.

The two layers are:
1. **ZIP Entry Offset** (checked by Android PackageManager) ← AGP handles this ✅
2. **ELF LOAD Segment** (checked by dynamic linker) ← CameraX 1.4.2 fixed 6/8 ✅

### Hypothesis 3: Cached Test APK?
The Phase 2.5 test might have used a stale APK from cache that lacked proper alignment.

---

## Implications for Phase 2.6

### Option A: Test Original APK (Recommended)
Since the original APK is already 16KB aligned, we should:
1. Test `app-device-debug.apk` (unmodified) on Pixel 9a
2. Verify PageSizeMismatchDialog does NOT appear
3. If successful, Phase 2.6 = validation only (no code changes needed)

### Option B: Apply zipalign Anyway (Conservative)
As a defensive measure:
1. Apply `zipalign -v -f -P 16 4` to ensure alignment
2. Test aligned APK on Pixel 9a
3. Document that manual zipalign had no effect

**Recommended**: Option A first, fall back to Option B if warnings persist.

---

## Next Steps

1. ✅ **Verify ZIP alignment** - COMPLETE (8/8 aligned)
2. ⏭️ **Test on Pixel 9a physical device** - Ready to execute
3. ⏭️ **Analyze test results**:
   - If no warning → Phase 2.6 success (no changes needed)
   - If warning persists → Investigate other root causes

---

## Technical Notes

### AGP 8.10.1 Behavior
AGP 8.5.1+ includes automatic 16KB page alignment for native libraries.

Evidence from this test confirms it **is working correctly** for:
- Direct dependencies (opencv module)
- Transitive dependencies (ML Kit, CameraX)

### zipalign Tool Usage
Correct command for 16KB alignment:
```bash
zipalign -v -f -P 16 4 input.apk output.apk
```

Where:
- `-v`: Verbose output
- `-f`: Force overwrite
- `-P 16`: Align .so files to 16KB pages
- `4`: Align all other files to 4 bytes (standard)

**DO NOT USE**:
- `-p` (conflicts with `-P`)
- `-z` (Zopfli compression, unrelated to alignment)

---

## Conclusion

The original APK from Phase 2.5 build is **already compliant** with 16KB page alignment requirements.

If PageSizeMismatchDialog still appears on Pixel 9a, the root cause is **not ZIP alignment**. Possible alternatives:
- ELF LOAD segment misalignment (ML Kit libraries)
- Android 16 false positive detection
- Device-specific quirks

**Recommendation**: Proceed with physical device testing to validate.
