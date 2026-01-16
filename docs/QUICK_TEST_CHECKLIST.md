# Quick Test Checklist - v2.9.6 Phase 1
## Print and Use on Work NB

---

## ✅ Pre-Test Setup

```powershell
cd d:\MyWorking\DevSources\AndroidStudioProjects\SmartCameraApp1
git pull origin main
$env:PIXEL9A = (adb devices | Select-String -Pattern '^\w+').Matches[0].Value
```

- [ ] Git synced (commit: e181e4a)
- [ ] Pixel 9a connected (Serial: _____________)
- [ ] APK installed (v2.9.6-phase1)

---

## 📋 Test Execution

### Phase 1: Init Logs (Launch app, wait 3s)

```powershell
adb -s $env:PIXEL9A logcat -c
adb -s $env:PIXEL9A logcat | Select-String "LiveCameraSource"
```

- [ ] "Camera bound successfully"
- [ ] "Camera sensor orientation: ____°"
- [ ] "Initial device rotation: 0"
- [ ] "OrientationEventListener enabled"
- [ ] "Initial targetRotation set to: 0"

**STOP if any missing**

---

### Phase 2: Rotation Test (Rotate device)

- [ ] Rotate to landscape → wait 2s
- [ ] "OrientationEventListener first trigger confirmed"
- [ ] "Orientation changed" log appeared

**STOP if no trigger after 5s**

---

### Phase 3: Photo Tests

```powershell
adb -s $env:PIXEL9A shell "rm -rf /sdcard/DCIM/SmartCamera/*"
```

#### TC-001: Portrait (📱)
- [ ] Hold device vertical, home button bottom
- [ ] Tap shutter
- [ ] Wait 2s

#### TC-002: Landscape Left (📱↶)
- [ ] Rotate 90° counter-clockwise
- [ ] Tap shutter
- [ ] Wait 2s

#### TC-003: Landscape Right (📱↷)
- [ ] Rotate 90° clockwise
- [ ] Tap shutter
- [ ] Wait 2s

---

## 📤 Photo Analysis

```powershell
adb -s $env:PIXEL9A pull /sdcard/DCIM/SmartCamera tmp/pixel9a_phase1_test/
python tools/analyze_exif.py tmp/pixel9a_phase1_test/
```

### Results Table

| Test | EXIF Expected | EXIF Actual | Visual OK? |
|------|---------------|-------------|------------|
| TC-001 Portrait | 1 | _____ | ✅ / ❌ |
| TC-002 Landscape L | 6 | _____ | ✅ / ❌ |
| TC-003 Landscape R | 8 | _____ | ✅ / ❌ |

---

## 📊 Final Result

- [ ] **PASS**: All 3 photos correct
- [ ] **FAIL**: _____ photos incorrect

**Issues**:
- _________________________________
- _________________________________

---

## 📤 Report Back

```powershell
notepad docs\test_results\v2.9.6_phase1_pixel9a.md
# Fill template from MANUAL_TEST_HANDOFF.md

git add docs/test_results/v2.9.6_phase1_pixel9a.md
git commit -m "[Test] v2.9.6 Phase 1: Pixel 9a results - PASS/FAIL"
git push origin main
```

**Time**: ____:____ to ____:____ (Total: ____ min)

---

**Tester**: ________________
**Date**: 2026-01-16
**Device**: Pixel 9a
