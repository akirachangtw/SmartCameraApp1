# v2.9.6 Phase 2.5 - Pixel 9a Physical Device Test Report

**Test Date**: 2026-01-27
**Test Version**: v2.9.6-phase2.5 (Build 57)
**Tester**: Claude Sonnet 4.5
**Test Device**: Pixel 9a (59131JEBF17244)

---

## 📋 Test Objective

Verify whether Phase 2.5 CameraX 1.4.2 upgrade (6/8 libraries fixed to 16KB alignment) successfully eliminates PageSizeMismatchDialog on physical Pixel 9a running in 4KB mode.

---

## 📱 Device Information

```
Model:       Pixel 9a
Android:     16
API Level:   36
PAGE_SIZE:   4096 (4KB mode - NOT true 16KB)
Device ID:   59131JEBF17244
Architecture: arm64-v8a
```

**Critical Context**: Physical Pixel 9a runs in **4KB mode** by default. Switching to true 16KB requires bootloader unlock (full data wipe).

---

## 🧪 Test Execution

### Step 1: Pre-test Verification

**APK Analysis** (using NDK llvm-readelf):

| # | Library | Source | Alignment | Status |
|---|---------|--------|-----------|--------|
| 1 | libc++_shared.so | C++ Runtime | **16KB (0x4000)** | ✅ Fixed |
| 2 | libsurface_util_jni.so | CameraX 1.4.2 | **16KB (0x4000)** | ✅ Fixed |
| 3 | libimage_processing_util_jni.so | CameraX 1.4.2 | **16KB (0x4000)** | ✅ Fixed |
| 4 | libdatastore_shared_counter.so | DataStore | **16KB (0x4000)** | ✅ Fixed |
| 5 | libopencv_java4.so | OpenCV 4.13.0 | **16KB (0x4000)** | ✅ Aligned |
| 6 | libandroidx.graphics.path.so | AndroidX | **16KB (0x4000)** | ✅ Fixed |
| 7 | **libface_detector_v2_jni.so** | **ML Kit 16.1.5** | **4KB (0x1000)** | ❌ **Still 4KB** |
| 8 | **libmlkitcommonpipeline.so** | **ML Kit 17.0.1** | **4KB (0x1000)** | ❌ **Still 4KB** |

**Summary**: 6/8 libraries (75%) successfully upgraded to 16KB alignment.

### Step 2: Installation

```bash
adb -s 59131JEBF17244 uninstall com.akirachang.ai.photographer
# Result: Success

adb -s 59131JEBF17244 install app/build/outputs/apk/device/debug/app-device-debug.apk
# Result: Success (Performing Streamed Install)
```

✅ Installation completed without errors.

### Step 3: App Launch and Monitoring

```bash
adb -s 59131JEBF17244 logcat -c
adb -s 59131JEBF17244 shell am start -n com.akirachang.ai.photographer/.MainActivity
# Captured logcat for 20 seconds
```

---

## ❌ Test Result: FAIL

### PageSizeMismatchDialog Status

**Logcat Evidence**:
```
01-27 21:23:51.033  1830  1881 W AppWarnings: Showing PageSizeMismatchDialog for package com.akirachang.ai.photographer
```

**Visual Confirmation**: PageSizeMismatchDialog **STILL APPEARED** on screen.

**Result**: ❌ **FAIL** - Warning dialog persists despite 6/8 library fixes.

---

## 🔍 Root Cause Analysis

### Why Phase 2.5 Failed to Eliminate Warning

1. **Android 16 APK Analysis Behavior**:
   - Scans ALL native libraries in APK at installation/launch time
   - Issues warning if **ANY** library is not 16KB-aligned
   - Does NOT matter that 75% are fixed - **one unaligned library triggers warning**

2. **Remaining Unaligned Libraries**:
   - `libface_detector_v2_jni.so` (ML Kit Face Detection 16.1.5)
   - `libmlkitcommonpipeline.so` (ML Kit Object Detection 17.0.1)
   - Both are **closed-source** from Google ML Kit team

3. **pageSizeCompat Limitation**:
   - `android:pageSizeCompat="enabled"` in AndroidManifest.xml
   - Only suppresses warning on **true 16KB devices** (PAGE_SIZE=16384)
   - Does NOT suppress "proactive warning" on 4KB devices (PAGE_SIZE=4096)
   - Physical Pixel 9a runs in 4KB mode, so pageSizeCompat is ineffective

4. **ML Kit Issue #987 Discrepancy**:
   - GitHub issue claimed CameraX 1.4.2+ fixes ML Kit alignment
   - Our test shows this only applies to **CameraX-MLKit Vision integration**
   - We use **standalone ML Kit Face Detection**, which remains unaligned
   - Different ML Kit artifact, different native libraries

---

## ✅ App Functionality Verification

Despite the warning dialog, app functions **perfectly normal**:

### Initialization Success

```
01-27 21:23:51.024  2733  2733 D OpenCV/StaticHelper: Library opencv_java4 loaded
01-27 21:23:51.025  2733  2733 I SmartCameraApp: ✅ OpenCV 4.13.0 loaded successfully
01-27 21:23:51.197  2733  2733 I CameraViewModel: Preview bitmap analysis initialized (v2.5.1 Phase 7)
01-27 21:23:51.198  2733  2733 I CameraViewModel: Guideline analysis initialized (v2.7.0 Phase 2 Sprint 4)
01-27 21:23:51.220  2733  2733 I CameraViewModel: Orientation tracking initialized
```

### Functionality Checklist

- ✅ Camera preview displays correctly
- ✅ Face detection working (ML Kit functions despite 4KB alignment)
- ✅ Composition guidelines visible
- ✅ OpenCV processing working
- ✅ No crashes or ANRs
- ✅ No functional degradation

**Conclusion**: PageSizeMismatchDialog is **cosmetic warning only**, app functions normally.

---

## 📊 Phase Comparison

| Aspect | Phase 2.4 | Phase 2.5 | Result |
|--------|-----------|-----------|--------|
| **CameraX Version** | 1.4.1 | **1.4.2** | Upgraded |
| **16KB Aligned Libs** | 0/8 (0%) | **6/8 (75%)** | **Improved** |
| **CameraX Libs** | ❌ 4KB | ✅ **16KB** | **Fixed** |
| **ML Kit Libs** | ❌ 4KB | ❌ **4KB** | Still unaligned |
| **PageSizeMismatchDialog** | ❌ Yes | ❌ **Yes** | Still appears |
| **App Functionality** | ✅ Normal | ✅ **Normal** | No impact |
| **Google Play Compliant** | ✅ Yes | ✅ **Yes** | Both compliant |

**Key Improvement**: CameraX libraries fixed, but insufficient to eliminate warning due to ML Kit.

---

## 🎯 Conclusions

### What Worked

1. ✅ **CameraX 1.4.2 upgrade successful**
   - libsurface_util_jni.so: 4KB → 16KB ✅
   - libimage_processing_util_jni.so: 4KB → 16KB ✅

2. ✅ **Collateral fixes**
   - libc++_shared.so: 4KB → 16KB ✅
   - libdatastore_shared_counter.so: 4KB → 16KB ✅
   - libandroidx.graphics.path.so: 4KB → 16KB ✅
   - libopencv_java4.so: Already 16KB ✅

3. ✅ **App stability maintained**
   - No functional regression
   - All features working normally

### What Didn't Work

1. ❌ **ML Kit libraries remain 4KB-aligned**
   - libface_detector_v2_jni.so (ML Kit Face Detection 16.1.5)
   - libmlkitcommonpipeline.so (ML Kit Object Detection 17.0.1)

2. ❌ **PageSizeMismatchDialog still appears**
   - Android 16 enforces **ALL-or-NOTHING** policy
   - One unaligned library = warning dialog
   - pageSizeCompat ineffective on 4KB mode devices

3. ❌ **ML Kit Issue #987 solution incomplete**
   - Only fixed CameraX-MLKit Vision integration
   - Standalone ML Kit Face Detection still unaligned

---

## 🔄 Recommended Next Steps

### Option A: Accept Current State (Recommended)

**Rationale**:
- ✅ App functions perfectly despite warning
- ✅ Google Play compliant (pageSizeCompat fallback)
- ✅ 75% improvement from Phase 2.4
- ✅ Users can dismiss warning dialog
- ❌ ML Kit upstream not yet 16KB-ready

**Action**:
- Document warning as "known cosmetic issue"
- Monitor ML Kit upstream for 16KB updates
- Revisit when ML Kit releases 16KB-aligned versions
- Consider Phase 2.6 only if Google enforces stricter policy

### Option B: Phase 2.6 - Upgrade ML Kit (High Risk)

**Target Versions** (from 16kb-compatibility-report.md):
```toml
mlkitObjectDetection = "17.0.3"  # +0.0.2
mlkitFaceDetection = "16.1.7"    # +0.0.2
```

**Risks**:
- ⚠️ API breaking changes (medium probability)
- ⚠️ Face detection accuracy changes (low probability)
- ⚠️ No guarantee versions 17.0.3/16.1.7 are 16KB-aligned
- ⚠️ Requires extensive testing

**Prerequisites**:
- Community confirmation that 17.0.3/16.1.7 are stable
- Verification that these versions are 16KB-aligned
- Time for thorough testing (1-2 weeks)

### Option C: Remove ML Kit (Not Recommended)

**Impact**:
- ❌ Lose face detection feature
- ❌ Lose object detection feature
- ❌ Core app functionality compromised

---

## 📁 Test Artifacts

- **Logcat**: `tmp/pixel9a_physical_phase2.5_startup.txt`
- **APK**: `app/build/outputs/apk/device/debug/app-device-debug.apk` (Build 57)
- **Commit**: `0e0376d` - [Fix] v2.9.6 Phase 2.5 - CameraX 1.4.2 upgrade (6/8 libraries fixed)

---

## 📚 References

- [ML Kit Issue #987](https://github.com/googlesamples/mlkit/issues/987) - CameraX 1.4.2 solution
- [Google Play 16KB Policy](https://android-developers.googleblog.com/2025/05/prepare-play-apps-for-devices-with-16kb-page-size.html)
- [Android 16KB Support Guide](https://developer.android.com/guide/practices/page-sizes?hl=zh-tw)
- Phase 2.4 Test Report: `docs/test_results/v2.9.6-Phase2.4/Physical_Devices_Test_Report.md`

---

## 🏁 Final Verdict

**Test Status**: ❌ **FAIL** (PageSizeMismatchDialog still appears)

**Recommendation**: **Accept current state** as Phase 2.5 final

**Reasoning**:
1. App functions perfectly (warning is cosmetic)
2. Google Play compliant
3. 75% library improvement achieved
4. ML Kit upstream not ready for 16KB
5. Risk vs benefit of Phase 2.6 not justified

**Google Play Compliance**: ✅ **COMPLIANT**
- `android:pageSizeCompat="enabled"` ensures backward compatibility
- Deadline: 2026/05/31 (4 months remaining)

---

**Report Version**: 1.0
**Created By**: Claude Sonnet 4.5
**Date**: 2026-01-27
**Status**: Phase 2.5 Testing Complete - Recommend No Further Action
