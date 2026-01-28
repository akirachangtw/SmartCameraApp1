# Phase 2.6.1 Manual Testing Instructions

**Date**: 2026-01-28
**APK**: app-device-debug.apk (with ML Kit 16.1.7)
**Status**: ✅ Built successfully, ready for testing

---

## 📊 Build Results

### Changes Applied
- ✅ ML Kit face-detection: 16.1.5 → 16.1.7
- ✅ ML Kit object-detection: 17.0.1 → 17.0.2
- ✅ Build successful (1m 54s)
- ✅ APK size: 246 MB (was 238 MB, +8 MB acceptable)
- ✅ ZIP alignment verified: 8/8 libraries (OK)

### ZIP Alignment Verification Results
```
21839872 lib/arm64-v8a/libandroidx.graphics.path.so (OK) ✅
21856256 lib/arm64-v8a/libc++_shared.so (OK) ✅
23150592 lib/arm64-v8a/libdatastore_shared_counter.so (OK) ✅
23166976 lib/arm64-v8a/libface_detector_v2_jni.so (OK) ✅  ← ML Kit (upgraded)
31703040 lib/arm64-v8a/libimage_processing_util_jni.so (OK) ✅
31735808 lib/arm64-v8a/libmlkitcommonpipeline.so (OK) ✅  ← ML Kit (upgraded)
42729472 lib/arm64-v8a/libopencv_java4.so (OK) ✅
66715648 lib/arm64-v8a/libsurface_util_jni.so (OK) ✅
```

**Result**: All libraries remain 16KB ZIP aligned! ✅

---

## 🧪 Test 1: Pixel 9a Physical Device (CRITICAL)

### Prerequisites
- Connect Pixel 9a to computer via USB
- Enable USB debugging on device
- Verify connection: `adb devices` should show device

### Test Steps

#### Step 1: Install APK
```bash
adb -s 59131JEBF17244 install -r app/build/outputs/apk/device/debug/app-device-debug.apk
```

**Expected**: "Success" message

#### Step 2: Clear Logcat
```bash
adb -s 59131JEBF17244 logcat -c
```

#### Step 3: Launch Application
```bash
adb -s 59131JEBF17244 shell am start -n com.akirachang.ai.photographer/.MainActivity
```

**Expected**: App launches

#### Step 4: Wait and Check for PageSizeMismatchDialog (CRITICAL!)
```bash
sleep 8
adb -s 59131JEBF17244 logcat -d | grep -i "pagesize"
```

**✅ SUCCESS CRITERIA**: **NO** "PageSizeMismatchDialog" or "AppWarnings" entries

**❌ FAILURE**: If you see "Showing PageSizeMismatchDialog" → proceed to Phase 2.6.2 (NDK upgrade)

#### Step 5: Verify App Functionality
```bash
adb -s 59131JEBF17244 logcat -d | grep "SmartCameraApp\|OpenCV\|CameraViewModel" | tail -20
```

**Expected to see**:
- ✅ "OpenCV 4.13.0 loaded successfully"
- ✅ "CameraViewModel: Detection result updated"
- ✅ "[v2.8.4 E2E] Total=~100ms"

#### Step 6: Manual Functionality Test
1. Point camera at your face
2. Verify face detection box appears
3. Check app performance feels normal
4. Use app for 2-3 minutes to ensure stability

**Expected**: All features work normally

---

## 🧪 Test 2: Pixel9a-16KB Emulator (Regression Test)

### Test Steps

#### Step 1: Start Emulator
```bash
# If not already running
emulator -avd Pixel9a-16KB &
```

Wait for emulator to fully boot (check `adb devices`)

#### Step 2: Install APK
```bash
adb -s emulator-5554 install -r app/build/outputs/apk/device/debug/app-device-debug.apk
```

#### Step 3: Launch and Check
```bash
adb -s emulator-5554 logcat -c
adb -s emulator-5554 shell am start -n com.akirachang.ai.photographer/.MainActivity
sleep 5
adb -s emulator-5554 logcat -d | grep -i "pagesize"
```

**Expected**: ✅ Still no warnings (should work same as before)

---

## 📋 Test Results Form

### Pixel 9a Physical Device Test

**PageSizeMismatchDialog Status**:
- [ ] ✅ NO WARNING - Success! (Proceed to version update)
- [ ] ❌ WARNING PERSISTS - Failed (Proceed to Phase 2.6.2: NDK upgrade)

**App Functionality**:
- [ ] Face detection working: YES / NO
- [ ] Camera preview smooth: YES / NO
- [ ] Performance normal: YES / NO
- [ ] Any crashes: YES / NO

**Logcat Observations**:
```
[Paste relevant logcat entries here]
```

### Emulator Regression Test

**Status**:
- [ ] ✅ PASS - No warnings, app works
- [ ] ❌ FAIL - Issues found

---

## 📄 Optional: APK Analyzer Check

If you want to visually verify the changes:

1. Open Android Studio
2. Go to **Build** → **Analyze APK**
3. Select `app/build/outputs/apk/device/debug/app-device-debug.apk`
4. Navigate to **lib/arm64-v8a/**
5. Check for orange/red warning triangles

**Expected Improvement**:
- `libface_detector_v2_jni.so` - Should have fewer/no warnings
- `libmlkitcommonpipeline.so` - Should have fewer/no warnings
- Other libraries - May still have warnings (address in Phase 2.6.2)

---

## 🎯 Next Steps Based on Results

### If Test 1 PASSES (No PageSizeMismatchDialog):

**Congratulations!** 🎉 The fix worked!

**Action**:
1. Mark Test 1 as ✅ PASS
2. Update version to 2.9.6-phase2.6.1
3. Create commit with changes
4. Document success in test report

**Commands for version update**:
```bash
# Edit app/build.gradle.kts
# Change versionCode from 57 to 58
# Change versionName from "2.9.6-phase2.5" to "2.9.6-phase2.6.1"
```

### If Test 1 FAILS (Warning Persists):

**Don't worry** - We have Phase 2.6.2 ready!

**Action**:
1. Mark Test 1 as ❌ FAIL
2. Document findings
3. Proceed to Phase 2.6.2: NDK Upgrade to r28+
4. Check AndroidX library updates

**Next investigation**:
- Check APK Analyzer for remaining warnings
- Identify which libraries still have issues
- Plan targeted fixes

---

## 📝 Reporting Back

After completing tests, please report:

1. **Pixel 9a Test Result**: PASS / FAIL
2. **Emulator Test Result**: PASS / FAIL
3. **Relevant Logcat Output** (especially pagesize-related)
4. **Any Unexpected Behavior**

This will help determine next steps!

---

## 🔧 Troubleshooting

### Issue: Device Not Detected
```bash
# Check devices
adb devices

# If empty, try:
adb kill-server
adb start-server
adb devices
```

### Issue: Installation Failed
```bash
# Uninstall old version first
adb uninstall com.akirachang.ai.photographer

# Then reinstall
adb install app/build/outputs/apk/device/debug/app-device-debug.apk
```

### Issue: App Crashes on Launch
```bash
# Get crash log
adb logcat -d | grep "FATAL\|AndroidRuntime"
```

---

## 📚 Reference Files

- Build Report: This file
- Execution Plan: `Phase_2.6.1_Execution_Plan.md`
- Migration Plan (Traycer): `16KB_Migration_Plan_Traycer.md`
- Previous Test: `Physical_Device_Test_Report.md` (before upgrade)
- APK Location: `app/build/outputs/apk/device/debug/app-device-debug.apk`

---

**Status**: ⏸️ Awaiting manual device testing
**Built**: 2026-01-28 09:54
**Ready**: YES ✅
