# Phase 2.6.1: ML Kit Upgrade Execution Plan

**Date**: 2026-01-27
**Based On**:
- Traycer's Migration Plan (`16KB_Migration_Plan_Traycer.md`)
- Medium Article Analysis (`16kb-page-size.pdf`)
- Our Test Results (`Physical_Device_Test_Report.md`, `APK_Analyzer_16KB_Warning_Analysis.md`)

---

## 🎯 Executive Summary

**Objective**: Upgrade ML Kit libraries to versions with 16KB page size support

**Expected Result**: 95%+ probability of resolving PageSizeMismatchDialog on Pixel 9a

**Time Estimate**: 30 minutes (build + test)

**Risk Level**: 🟢 **LOW** (minor version bump, same API)

---

## 📋 Implementation Steps

### Step 1: Update libs.versions.toml (2 minutes)

**File**: `gradle/libs.versions.toml`

**Changes**:
```diff
[versions]
- mlkitFaceDetection = "16.1.5"
+ mlkitFaceDetection = "16.1.7"      # ✅ 16KB support added Jan 15, 2026

- mlkitObjectDetection = "17.0.1"
+ mlkitObjectDetection = "17.0.2"    # ✅ Latest version
```

**Rationale**:
- ML Kit 16.1.7 includes ELF LOAD segment alignment for 16KB pages
- Fixes `libface_detector_v2_jni.so` and `libmlkitcommonpipeline.so` warnings
- Official solution from Google (released 2026-01-15)

---

### Step 2: Clean Build (3 minutes)

**Commands**:
```bash
./gradlew clean
./gradlew assembleDeviceDebug
```

**Expected Output**:
- Build successful
- APK size: ~238 MB (±5 MB acceptable)
- No dependency conflicts

---

### Step 3: Verify ZIP Alignment (1 minute)

**Command**:
```bash
zipalign -c -v -P 16 4 app/build/outputs/apk/device/debug/app-device-debug.apk | grep "lib/arm64-v8a.*\.so"
```

**Expected Result**: All 8 libraries show "(OK)"

---

### Step 4: Check APK Analyzer (2 minutes)

**Steps**:
1. Android Studio → Build → Analyze APK
2. Open `app/build/outputs/apk/device/debug/app-device-debug.apk`
3. Navigate to lib/arm64-v8a/
4. Check for orange/red warning triangles

**Expected Result**:
- ✅ `libface_detector_v2_jni.so` - NO warning
- ✅ `libmlkitcommonpipeline.so` - NO warning
- Possible remaining: AndroidX libraries (to be addressed in Phase 2.6.2)

---

### Step 5: Test on Pixel 9a Physical Device (10 minutes)

**Critical Test**:
```bash
# Install
adb -s 59131JEBF17244 install -r app/build/outputs/apk/device/debug/app-device-debug.apk

# Clear logcat
adb -s 59131JEBF17244 logcat -c

# Launch app
adb -s 59131JEBF17244 shell am start -n com.akirachang.ai.photographer/.MainActivity

# Wait and check for warnings
sleep 8
adb -s 59131JEBF17244 logcat -d | grep -i "pagesize"
```

**Success Criteria**:
- ✅ NO "PageSizeMismatchDialog" log entry
- ✅ NO "AppWarnings" entries
- ✅ App launches successfully

---

### Step 6: Verify App Functionality (5 minutes)

**Tests**:
```bash
# Check core functionality
adb -s 59131JEBF17244 logcat -d | grep "SmartCameraApp\|CameraViewModel\|OpenCV"
```

**Expected Logs**:
- ✅ "OpenCV 4.13.0 loaded successfully"
- ✅ "CameraViewModel: Detection result updated"
- ✅ "[v2.8.4 E2E] Total=~100ms"
- ✅ "Guideline analysis initialized"

**Manual Verification**:
- Launch app
- Point camera at face
- Verify face detection box appears
- Check performance feels normal

---

### Step 7: Regression Test on Emulator (3 minutes)

**Ensure emulator still works**:
```bash
adb -s emulator-5554 install -r app/build/outputs/apk/device/debug/app-device-debug.apk
adb -s emulator-5554 shell am start -n com.akirachang.ai.photographer/.MainActivity
adb -s emulator-5554 logcat -d | grep -i "pagesize"
```

**Expected**: ✅ Still no warnings (should remain working)

---

### Step 8: Update Version and Document (5 minutes)

**If tests pass, update version**:

**File**: `app/build.gradle.kts`
```diff
- versionCode = 57
+ versionCode = 58

- versionName = "2.9.6-phase2.5"
+ versionName = "2.9.6-phase2.6.1"
```

**Documentation**:
- Update test report with results
- Create changelog entry
- Prepare commit message

---

## 📊 Success Metrics

### Must Pass ✅
- [ ] Build completes without errors
- [ ] No Gradle dependency conflicts
- [ ] ZIP alignment remains 8/8
- [ ] APK installs on Pixel 9a
- [ ] **NO PageSizeMismatchDialog on Pixel 9a** ← PRIMARY GOAL
- [ ] Face detection functional
- [ ] Performance metrics normal (E2E ~100ms)

### Should Pass ✅
- [ ] APK Analyzer shows fewer warnings
- [ ] No crashes during 5-minute session
- [ ] Emulator regression test passes

---

## 🔄 If Tests Fail

**Scenario 1**: Build fails with dependency conflict
- **Action**: Check Gradle output for conflict details
- **Rollback**: Revert `libs.versions.toml` changes
- **Next**: Investigate conflicting dependencies

**Scenario 2**: PageSizeMismatchDialog still appears
- **Action**: Check APK Analyzer for remaining warnings
- **Next**: Proceed to Phase 2.6.2 (NDK upgrade)
- **Investigate**: Other library warnings (AndroidX, etc.)

**Scenario 3**: Face detection broken
- **Action**: Check logcat for ML Kit errors
- **Rollback**: Revert to ML Kit 16.1.5
- **Report**: File bug with version compatibility issue

---

## 📝 Commit Message Template

**If successful**:
```
[Fix] v2.9.6 Phase 2.6.1 - ML Kit 16KB alignment upgrade

Changes:
- Upgrade ML Kit face-detection: 16.1.5 → 16.1.7 (16KB support)
- Upgrade ML Kit object-detection: 17.0.1 → 17.0.2 (latest)
- Version bump: 57 → 58, 2.9.6-phase2.5 → 2.9.6-phase2.6.1

Test Results:
- ✅ Pixel 9a physical device: NO PageSizeMismatchDialog
- ✅ Face detection functional
- ✅ Performance metrics maintained (E2E ~100ms)
- ✅ APK Analyzer: ML Kit warnings resolved
- ✅ Compliant with Google Play 16KB deadline (May 31, 2026)

Related:
- ML Kit 16.1.7 release: Jan 15, 2026 (16KB support)
- Test report: docs/test_results/v2.9.6-Phase2.6/

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
```

---

## 🎯 Ready to Execute

All analysis complete. Three independent sources confirm this approach:

1. **Traycer's Plan**: Recommends ML Kit 16.1.7 upgrade as Phase 1
2. **Medium Article**: "Upgrade everything to versions that fully support 16KB"
3. **Our Analysis**: ML Kit 16.1.7 released Jan 15, 2026 with 16KB support

**Confidence**: 🟢 **95%+** success probability

**Action**: Proceed with Step 1 - Update libs.versions.toml

---

## 📚 References

- Traycer Plan: `docs/test_results/v2.9.6-Phase2.6/16KB_Migration_Plan_Traycer.md`
- Article Analysis: `docs/test_results/v2.9.6-Phase2.6/16kb-page-size.pdf`
- Test Reports: `docs/test_results/v2.9.6-Phase2.6/Physical_Device_Test_Report.md`
- ML Kit Releases: https://developers.google.com/ml-kit/release-notes
