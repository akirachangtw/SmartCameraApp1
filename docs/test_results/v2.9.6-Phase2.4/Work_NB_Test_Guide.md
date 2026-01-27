# v2.9.6 Phase 2.4 - Work NB Physical Device Test Guide

**Test Version**: v2.9.6-phase2.4 (Build 56)
**Test Date**: 2026-01-27
**Test Devices**: Pixel 9a, Sony (Work NB)
**APK Location**: `app/build/outputs/apk/device/debug/app-device-debug.apk`

---

## 📋 Test Objective

Verify that Phase 2.4 `android:pageSizeCompat="enabled"` solution successfully eliminates PageSizeMismatchDialog on **physical devices**, specifically:

1. ✅ **Pixel 9a** (Android 16, 16KB page size) - **CRITICAL TEST**
2. ✅ **Sony Phone** - Cross-device compatibility verification

---

## 🔧 Prerequisites

### Required Tools
- ✅ ADB installed and accessible
- ✅ USB debugging enabled on both phones
- ✅ Phase 2.4 APK built (Build 56)

### Device Setup
```bash
# Enable Developer Options on phone:
# Settings → About Phone → Tap "Build Number" 7 times

# Enable USB Debugging:
# Settings → System → Developer Options → USB Debugging
```

---

## 📱 Test 1: Pixel 9a (CRITICAL)

### Device Information

**Expected Specifications**:
- Model: Google Pixel 9a
- Android Version: 16
- API Level: 36
- Page Size: **16KB** (critical for this test)

### Step 1: Connect Device

```bash
# Connect Pixel 9a to Work NB via USB
# Allow USB debugging when prompted on phone

# Verify connection
adb devices
# Expected output: 59131JEBF17244 (or similar device ID)
```

### Step 2: Verify Device Identity

```bash
# Get device model
adb -s <DEVICE_ID> shell getprop ro.product.model
# Expected: Pixel 9a

# Get Android version
adb -s <DEVICE_ID> shell getprop ro.build.version.release
# Expected: 16

# Get API level
adb -s <DEVICE_ID> shell getprop ro.build.version.sdk
# Expected: 36

# ⭐ CRITICAL: Verify 16KB page size
adb -s <DEVICE_ID> shell getconf PAGE_SIZE
# Expected: 16384
```

✅ **Checkpoint**: Confirm PAGE_SIZE = 16384 before proceeding

### Step 3: Clean Installation

```bash
# Uninstall any previous version (Phase 2.3 or earlier)
adb -s <DEVICE_ID> uninstall com.akirachang.ai.photographer
# Expected: Success

# Verify clean state
adb -s <DEVICE_ID> shell pm list packages | grep photographer
# Expected: No output (app completely removed)
```

### Step 4: Install Phase 2.4

```bash
# Navigate to project root
cd d:/MyDocuments/DevSources/AndroidStudioProjects/SmartCameraApp1

# Install APK
adb -s <DEVICE_ID> install app/build/outputs/apk/device/debug/app-device-debug.apk
# Expected: Performing Streamed Install
#           Success
```

✅ **Checkpoint**: Installation completes with no warning dialog visible on phone

### Step 5: Launch and Monitor

```bash
# Clear logcat before launch
adb -s <DEVICE_ID> logcat -c

# Launch app
adb -s <DEVICE_ID> shell am start -n com.akirachang.ai.photographer/.MainActivity
# Expected: Starting: Intent { cmp=... }

# Wait for app to fully start
# (Watch phone screen - should see camera preview, no warning dialog)

# After 15-20 seconds, capture logcat
adb -s <DEVICE_ID> logcat -d > docs/test_results/v2.9.6-Phase2.4/pixel9a_physical_phase2.4_startup.txt
```

### Step 6: Verify No Warning Dialog

**Visual Check (MOST IMPORTANT)**:
- ❌ **Should NOT see**: "This app uses native libraries that are not aligned to 16KB page boundaries" dialog
- ✅ **Should see**: Direct to camera preview screen with AI overlay

**Logcat Analysis**:
```bash
# Search for PageSizeMismatchDialog
grep -i "pagesize" docs/test_results/v2.9.6-Phase2.4/pixel9a_physical_phase2.4_startup.txt

# Expected: No results, or only system-level entries (not app-related)
```

✅ **Success Criteria**:
- No PageSizeMismatchDialog on screen
- No "Showing PageSizeMismatchDialog" in logcat
- App launches directly to camera preview

❌ **Failure Criteria**:
- Warning dialog appears on screen
- Logcat contains "Showing PageSizeMismatchDialog for package com.akirachang.ai.photographer"

### Step 7: Functionality Verification

**Test Checklist**:
- [ ] Camera preview displays correctly
- [ ] Back camera works
- [ ] Front camera works (switch camera button)
- [ ] AI overlay appears (face detection boxes)
- [ ] Composition guidelines visible
- [ ] Photo capture works (tap shutter button)
- [ ] Photos saved to gallery
- [ ] FPS counter shows 18-22 FPS
- [ ] No crashes or ANRs
- [ ] No visible lag or stutter

**Extract Key Info from Logcat**:
```bash
# Check OpenCV loaded
grep "OpenCV.*loaded" docs/test_results/v2.9.6-Phase2.4/pixel9a_physical_phase2.4_startup.txt

# Check CameraViewModel initialized
grep "CameraViewModel.*initialized" docs/test_results/v2.9.6-Phase2.4/pixel9a_physical_phase2.4_startup.txt

# Check for any crashes
grep -i "fatal\|crash\|exception" docs/test_results/v2.9.6-Phase2.4/pixel9a_physical_phase2.4_startup.txt | head -20
```

### Step 8: Screenshot Capture (Optional)

```bash
# Capture screenshot showing app running normally
adb -s <DEVICE_ID> shell "screencap -p > /sdcard/Download/phase2.4_pixel9a_running.png"
adb -s <DEVICE_ID> pull //sdcard//Download//phase2.4_pixel9a_running.png docs/test_results/v2.9.6-Phase2.4/
adb -s <DEVICE_ID> shell "rm /sdcard/Download/phase2.4_pixel9a_running.png"
```

---

## 📱 Test 2: Sony Phone (Cross-Device Verification)

### Step 1: Connect Sony Device

```bash
# Connect Sony phone to Work NB via USB
# Allow USB debugging when prompted

# Verify connection
adb devices
# Should see two devices if Pixel 9a still connected
```

### Step 2: Identify Sony Device

```bash
# List all devices
adb devices

# Get model of each device
adb -s <SONY_DEVICE_ID> shell getprop ro.product.model
adb -s <SONY_DEVICE_ID> shell getprop ro.build.version.release
adb -s <SONY_DEVICE_ID> shell getprop ro.build.version.sdk

# Check page size
adb -s <SONY_DEVICE_ID> shell getconf PAGE_SIZE
```

### Step 3-8: Repeat Same Tests as Pixel 9a

Follow identical steps as Pixel 9a test, replacing device ID:

```bash
# Uninstall
adb -s <SONY_DEVICE_ID> uninstall com.akirachang.ai.photographer

# Install
adb -s <SONY_DEVICE_ID> install app/build/outputs/apk/device/debug/app-device-debug.apk

# Launch and capture
adb -s <SONY_DEVICE_ID> logcat -c
adb -s <SONY_DEVICE_ID> shell am start -n com.akirachang.ai.photographer/.MainActivity
sleep 15
adb -s <SONY_DEVICE_ID> logcat -d > docs/test_results/v2.9.6-Phase2.4/sony_physical_phase2.4_startup.txt

# Verify no warnings
grep -i "pagesize" docs/test_results/v2.9.6-Phase2.4/sony_physical_phase2.4_startup.txt
```

---

## 📊 Test Results Documentation

### Create Test Report

After completing tests on both devices, create:

**File**: `docs/test_results/v2.9.6-Phase2.4/Physical_Devices_Test_Report.md`

**Template**:
```markdown
# v2.9.6 Phase 2.4 - Physical Devices Test Report

**Test Date**: 2026-01-27
**Test Version**: v2.9.6-phase2.4 (Build 56)
**Tester**: [Your Name]

## Test Summary

| Device | Android | Page Size | PageSizeDialog | Functionality | Result |
|--------|---------|-----------|----------------|---------------|--------|
| Pixel 9a | 16 | 16384 | ✅/❌ No/Yes | ✅/❌ Pass/Fail | PASS/FAIL |
| Sony | X | XXXX | ✅/❌ No/Yes | ✅/❌ Pass/Fail | PASS/FAIL |

## Pixel 9a Test Results

### Device Info
- Model: [from getprop]
- Android: [from getprop]
- Page Size: [from getconf PAGE_SIZE]

### PageSizeMismatchDialog
- Visual: ✅ No dialog / ❌ Dialog appeared
- Logcat: [paste grep results]

### Functionality
- [Checklist results]

### Logcat Evidence
- File: pixel9a_physical_phase2.4_startup.txt
- Lines captured: [wc -l]
- Key findings: [OpenCV loaded, CameraViewModel, etc.]

## Sony Test Results

[Same format as Pixel 9a]

## Conclusions

[Summary of findings]
```

---

## ⚠️ Troubleshooting

### Issue: Device Not Detected

```bash
# Windows: Install Google USB Driver
# Download from: https://developer.android.com/studio/run/win-usb

# Verify ADB
adb version
# Should show: Android Debug Bridge version 1.0.XX

# Restart ADB server
adb kill-server
adb start-server
adb devices
```

### Issue: Installation Failed

```bash
# Error: INSTALL_FAILED_UPDATE_INCOMPATIBLE
# Solution: Uninstall old version completely
adb uninstall com.akirachang.ai.photographer

# Error: INSTALL_FAILED_INSUFFICIENT_STORAGE
# Solution: Free up space on device (need ~150MB)

# Error: INSTALL_FAILED_VERSION_DOWNGRADE
# Solution: Previous version had higher versionCode
# Use -d flag to force downgrade (testing only)
adb install -r -d app-device-debug.apk
```

### Issue: Logcat Too Large

```bash
# Capture only last 5000 lines
adb logcat -d -t 5000 > logcat_output.txt

# Filter for app only
adb logcat -d | grep "ai.photographer" > logcat_filtered.txt
```

---

## 🎯 Success Criteria Summary

### Critical Success Factors

1. **Pixel 9a (16KB Device)**:
   - ✅ PAGE_SIZE = 16384 confirmed
   - ✅ No PageSizeMismatchDialog on screen
   - ✅ No warning in logcat
   - ✅ App functions normally

2. **Sony Phone**:
   - ✅ App installs and runs
   - ✅ No crashes
   - ✅ Camera preview works

### If All Tests Pass

**Next Steps**:
1. ✅ Document results in Physical_Devices_Test_Report.md
2. ✅ Update CHANGELOG.md
3. ✅ Tag release: `git tag v2.9.6-phase2.4`
4. ✅ Push to GitHub (if applicable)
5. ✅ Consider Google Play submission (Nov 1, 2025 deadline)

### If Tests Fail

**Immediate Actions**:
1. ❌ Capture detailed logcat evidence
2. ❌ Screenshot of warning dialog (if appears)
3. ❌ Check AndroidManifest.xml for correct pageSizeCompat syntax
4. ❌ Verify APK is Build 56 (not older version)
5. ❌ Review Phase 2.4 implementation document
6. ❌ Consider alternative solutions or contact Google support

---

## 📁 Test Artifacts Checklist

After testing, ensure these files exist:

- [ ] `pixel9a_physical_phase2.4_startup.txt` (logcat)
- [ ] `sony_physical_phase2.4_startup.txt` (logcat)
- [ ] `Physical_Devices_Test_Report.md` (test report)
- [ ] `phase2.4_pixel9a_running.png` (screenshot, optional)
- [ ] Updated CHANGELOG.md (if tests pass)

---

## 📞 Support References

**Documentation**:
- Phase 2.4 Implementation: `docs/plan/v2.9.6-Phase2.4-pageSizeCompat-Official-Solution.md`
- Solutions Comparison: `docs/plan/v2.9.6-Phase2-Solutions-Comparison-Analysis.md`
- Emulator Test Report: `docs/test_results/v2.9.6-Phase2.4/Emulator_Stability_Test_Report.md`

**Official Android Docs**:
- 16KB Page Size Support: https://developer.android.com/guide/practices/page-sizes?hl=zh-tw
- Android 16 Behavior Changes: https://developer.android.com/about/versions/16/behavior-changes-all

**Commit Reference**:
- Commit: 0c86269
- Message: "[Fix] v2.9.6 Phase 2.4 - Official pageSizeCompat solution for Android 16"

---

**Test Guide Version**: 1.0
**Created By**: Claude Sonnet 4.5
**Date**: 2026-01-27
**Status**: Ready for Physical Device Testing
