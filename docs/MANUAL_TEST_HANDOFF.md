# Manual Testing Handoff - v2.9.6 Phase 1

## 📋 Overview

**Purpose**: Precise test execution guide for Work NB + Pixel 9a phone
**Version**: v2.9.6 Phase 1
**Test Device**: Pixel 9a
**Last Updated**: 2026-01-16

---

## 🚀 Quick Start (Work NB Setup)

### Step 1: Sync from GitHub

```powershell
# Navigate to project directory
cd d:\MyWorking\DevSources\AndroidStudioProjects\SmartCameraApp1

# Pull latest changes
git pull origin main

# Verify current version
git log -1 --oneline
# Expected: e181e4a [Fix] v2.9.6 Phase 1: OrientationEventListener 修復完成
```

### Step 2: Connect Pixel 9a

```powershell
# Enable USB debugging on Pixel 9a:
# Settings → About phone → Tap "Build number" 7 times → Developer options → USB debugging

# Verify connection
adb devices
# Expected output: List of devices attached
#                  <DEVICE_SERIAL>    device

# Set device serial as environment variable (replace <SERIAL> with actual value)
$env:PIXEL9A = (adb devices | Select-String -Pattern '^\w+').Matches[0].Value
echo "Device: $env:PIXEL9A"
```

### Step 3: Install APK

```powershell
# Navigate to APK directory (if pre-built APK exists)
cd app\build\outputs\apk\device\debug

# Install APK
adb -s $env:PIXEL9A install -r app-device-debug.apk

# OR build and install from source
cd d:\MyWorking\DevSources\AndroidStudioProjects\SmartCameraApp1
.\gradlew installDeviceDebug
```

---

## 🧪 Test Execution Checklist

### Phase 1: Initialization Verification

**Objective**: Confirm OrientationListener is initialized correctly

**Steps**:

1. **Clear Logcat**
   ```powershell
   adb -s $env:PIXEL9A logcat -c
   ```

2. **Start Logcat Monitoring**
   ```powershell
   # Open a new PowerShell window and run:
   adb -s $env:PIXEL9A logcat | Select-String -Pattern "LiveCameraSource|Orientation"
   ```

3. **Launch App**
   - Open SmartCameraApp1 on Pixel 9a
   - Grant camera permission if prompted

4. **Verify Initial Logs** ✅
   ```
   Expected logs (within 2 seconds of launch):
   ✓ LiveCameraSource: ✅ Camera bound successfully: Preview + ImageAnalysis + ImageCapture
   ✓ LiveCameraSource: Camera sensor orientation: <XXX>°
   ✓ LiveCameraSource: Initial device rotation: 0
   ✓ LiveCameraSource: OrientationEventListener enabled
   ✓ LiveCameraSource: Initial targetRotation set to: 0
   ```

5. **Record Results**
   - [ ] All 5 logs appeared
   - [ ] Sensor orientation value: ______°
   - [ ] Initial rotation: ______

**Blocker**: If "Initial targetRotation set" is missing → STOP and report

---

### Phase 2: OrientationListener Trigger Test

**Objective**: Verify OrientationListener responds to device rotation

**Steps**:

1. **Keep Logcat Running** (from Phase 1)

2. **Rotate Device**
   - Hold Pixel 9a in portrait (vertical) → rotate to landscape left (90°)
   - Hold for 2 seconds
   - Rotate back to portrait

3. **Verify Trigger Log** ✅
   ```
   Expected log (first rotation):
   ✓ LiveCameraSource: OrientationEventListener first trigger confirmed
   ✓ LiveCameraSource: Orientation changed: <XXX>° → rotation=<Y>
   ```

4. **Record Results**
   - [ ] First trigger confirmed log appeared
   - [ ] Orientation changed log appeared
   - [ ] Time to trigger: ______ms (estimate)

**Blocker**: If no trigger log after 5 seconds → OrientationListener not working

---

### Phase 3: Photo Orientation Testing (CRITICAL)

**Objective**: Verify photos are saved with correct EXIF orientation

#### Preparation

1. **Create Test Output Directory**
   ```powershell
   New-Item -ItemType Directory -Force -Path "tmp\pixel9a_phase1_test"
   ```

2. **Clear Existing Photos** (optional)
   ```powershell
   adb -s $env:PIXEL9A shell "rm -rf /sdcard/DCIM/SmartCamera/*"
   ```

#### TC-001: Portrait Photo (0°)

**Steps**:

1. **Hold Device**: Pixel 9a in portrait (vertical), home button at bottom
2. **Verify UI**: Shutter button should be at bottom of screen
3. **Take Photo**: Tap shutter button once
4. **Wait**: 2 seconds for file save

**Expected**:
- EXIF Orientation: 1 (Normal)
- Visual: Photo displays upright without rotation

#### TC-002: Landscape Left Photo (90°)

**Steps**:

1. **Rotate Device**: Turn Pixel 9a 90° counter-clockwise (landscape left)
2. **Verify UI**: Shutter button should remain at visual bottom
3. **Take Photo**: Tap shutter button once
4. **Wait**: 2 seconds for file save

**Expected**:
- EXIF Orientation: 6 (Rotate 90° CW)
- Visual: Photo displays correctly when viewed

#### TC-003: Landscape Right Photo (270°)

**Steps**:

1. **Rotate Device**: Turn Pixel 9a 90° clockwise (landscape right)
2. **Verify UI**: Shutter button should remain at visual bottom
3. **Take Photo**: Tap shutter button once
4. **Wait**: 2 seconds for file save

**Expected**:
- EXIF Orientation: 8 (Rotate 90° CCW)
- Visual: Photo displays correctly when viewed

---

## 📤 Photo Extraction and Analysis

### Step 1: Pull Photos from Device

```powershell
# Pull all photos from SmartCamera folder
adb -s $env:PIXEL9A pull /sdcard/DCIM/SmartCamera tmp/pixel9a_phase1_test/

# Verify files
Get-ChildItem tmp/pixel9a_phase1_test/ -Filter *.jpg
```

### Step 2: Analyze EXIF Data

```powershell
# Ensure Python is installed
python --version

# Install dependencies (if needed)
pip install pillow

# Run EXIF analysis
python tools/analyze_exif.py tmp/pixel9a_phase1_test/

# Expected output format:
# File: IMG_20260116_123456.jpg
#   EXIF Orientation: 1 (Normal)
#   Dimensions: 3024x4032
#   DateTime: 2026:01:16 12:34:56
```

### Step 3: Visual Verification

**Open each photo in Windows Photos app**:

1. TC-001 photo: Should display upright (portrait)
2. TC-002 photo: Should display upright (originally landscape left)
3. TC-003 photo: Should display upright (originally landscape right)

**Red flags**:
- ❌ Photo appears rotated 90° or 180°
- ❌ EXIF orientation doesn't match device orientation

---

## 📊 Test Results Template

### Copy and Fill This Template

```markdown
# v2.9.6 Phase 1 Test Results - Pixel 9a

**Tester**: [Your Name]
**Date**: 2026-01-16
**Device**: Pixel 9a (Serial: ____________)
**APK Version**: v2.9.6-phase1 (Build 52)

## Phase 1: Initialization

- [ ] ✅ All 5 initialization logs appeared
- [ ] ✅ Sensor orientation: ______°
- [ ] ✅ Initial rotation: 0

**Issues**: None / [Describe if any]

## Phase 2: OrientationListener Trigger

- [ ] ✅ First trigger confirmed log appeared
- [ ] ✅ Rotation response time: ~______ms

**Issues**: None / [Describe if any]

## Phase 3: Photo Orientation

### TC-001: Portrait (0°)
- **EXIF Orientation**: ______ (Expected: 1)
- **Visual Check**: ✅ Correct / ❌ Rotated ______°
- **Screenshot**: [Attach if failed]

### TC-002: Landscape Left (90°)
- **EXIF Orientation**: ______ (Expected: 6)
- **Visual Check**: ✅ Correct / ❌ Rotated ______°
- **Screenshot**: [Attach if failed]

### TC-003: Landscape Right (270°)
- **EXIF Orientation**: ______ (Expected: 8)
- **Visual Check**: ✅ Correct / ❌ Rotated ______°
- **Screenshot**: [Attach if failed]

## Overall Result

- [ ] ✅ PASS: All tests passed
- [ ] ❌ FAIL: See issues below

**Critical Issues**:
1. [List any blockers]

**Minor Issues**:
1. [List any non-blockers]

**Logcat Snapshot**:
```
[Paste key logs here]
```

**Next Steps**:
- [Recommendations based on results]
```

---

## 📤 Reporting Back to Home NB

### Method 1: Git Commit (Recommended)

```powershell
# Create test results file
notepad docs\test_results\v2.9.6_phase1_pixel9a.md
# Paste filled template above

# Add photos (if failures exist)
git add tmp/pixel9a_phase1_test/*.jpg

# Commit results
git add docs/test_results/v2.9.6_phase1_pixel9a.md
git commit -m "[Test] v2.9.6 Phase 1: Pixel 9a manual test results

- Phase 1 Initialization: PASS/FAIL
- Phase 2 OrientationListener: PASS/FAIL
- Phase 3 Photo Orientation: PASS/FAIL

Device: Pixel 9a
Tester: [Your Name]
Date: 2026-01-16"

# Push to GitHub
git push origin main
```

### Method 2: Quick Message (For urgent issues)

**Use Claude Code Chat on Work NB to send message**:

```
@Home NB v2.9.6 Phase 1 測試完成

結果摘要:
- Phase 1: PASS/FAIL
- Phase 2: PASS/FAIL
- Phase 3: PASS/FAIL

詳細結果已推送至 GitHub: docs/test_results/v2.9.6_phase1_pixel9a.md

緊急問題: [如有]
```

---

## 🔧 Troubleshooting

### Issue: ADB device not found

**Solution**:
```powershell
# Restart ADB server
adb kill-server
adb start-server
adb devices

# If still not working:
# 1. Reconnect USB cable
# 2. Revoke USB debugging authorizations on Pixel 9a
# 3. Re-enable USB debugging
```

### Issue: App crashes on launch

**Solution**:
```powershell
# Pull crash logs
adb -s $env:PIXEL9A logcat -d > tmp/pixel9a_crash.txt

# Search for crash reason
Select-String -Path tmp/pixel9a_crash.txt -Pattern "FATAL|AndroidRuntime"

# Report crash log back to Home NB
```

### Issue: Photos not found in DCIM/SmartCamera

**Solution**:
```powershell
# Search for photos in all locations
adb -s $env:PIXEL9A shell "find /sdcard -name '*.jpg' -type f -mmin -10"

# Check app storage permissions
adb -s $env:PIXEL9A shell "dumpsys package com.akirachang.ai.photographer | grep -i permission"
```

### Issue: EXIF analysis script fails

**Solution**:
```powershell
# Verify Python installation
python --version
pip list | Select-String pillow

# Reinstall Pillow
pip install --upgrade pillow

# Manual EXIF check (Windows)
# Right-click photo → Properties → Details → Origin section
```

---

## ⏱️ Estimated Time

- **Setup** (first time): 10-15 minutes
- **Phase 1 Test**: 2 minutes
- **Phase 2 Test**: 3 minutes
- **Phase 3 Test**: 5 minutes (3 photos + analysis)
- **Reporting**: 5 minutes

**Total**: ~25-30 minutes (subsequent runs: ~15 minutes)

---

## 📞 Emergency Contact

If you encounter blockers during testing:

1. **Take Screenshots**: Device screen + logcat output
2. **Save Logcat**: `adb logcat -d > tmp/emergency_log.txt`
3. **Commit and Push**: Emergency logs to GitHub
4. **Tag Issue**: Use `[BLOCKER]` prefix in commit message

---

**Document Version**: 1.0
**Created**: 2026-01-16
**Author**: Claude Code Assistant
**Next Review**: After Phase 1 completion
