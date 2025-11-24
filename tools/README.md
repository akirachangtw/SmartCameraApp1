# SmartCameraApp1 Development Tools

PowerShell automation scripts for SmartCameraApp1 development, testing, and debugging.

## 📋 Prerequisites

### Required Software
- **Android SDK Platform Tools** - Must be in system PATH
  - Verify with: `adb version`
  - Download: https://developer.android.com/studio/releases/platform-tools

### Device Setup
- **Android device** connected via USB with USB Debugging enabled
  - OR Android emulator running
- **Enable USB Debugging**: Settings → Developer Options → USB Debugging

## 🚀 Scripts Overview

### 1. `install-apk.ps1`
Installs SmartCameraApp1 APK to connected device.

**Usage**:
```powershell
cd tools
.\install-apk.ps1
```

**What it does**:
1. ✅ Checks if APK exists in `app/build/outputs/apk/debug/`
2. ✅ Verifies ADB is available
3. ✅ Checks for connected devices
4. ✅ Uninstalls previous version (if exists)
5. ✅ Installs new APK
6. ✅ Launches the app

**Prerequisites**:
- APK must be built first in Android Studio:
  - `Build → Build Bundle(s) / APK(s) → Build APK(s)`

---

### 2. `capture-logcat.ps1`
Captures filtered logcat output for debugging.

**Usage**:
```powershell
cd tools
.\capture-logcat.ps1
```

**What it does**:
1. ✅ Clears logcat buffer
2. ✅ Filters logs for SmartCameraApp1 tags:
   - `CameraRepositoryImpl` - ML Kit detection coordinates
   - `DetectionOverlay` - Animation and rendering
   - `CameraViewModel` - UI state management
3. ✅ Displays real-time logs in console
4. ✅ Saves to timestamped file: `v1.4.1_logcat_YYYYMMDD_HHMMSS.txt`

**Control**:
- Press **Ctrl+C** to stop capture and save file

**Output Location**:
- Project root directory (same level as `app/` folder)

---

### 3. `deploy-and-log.ps1` (⭐ Recommended)
One-click deployment and logcat capture.

**Usage**:
```powershell
cd tools
.\deploy-and-log.ps1
```

**What it does**:
1. ✅ Runs `install-apk.ps1` (install and launch)
2. ✅ Waits 3 seconds for app to stabilize
3. ✅ Runs `capture-logcat.ps1` (start logging)
4. ✅ You test the app while logs are captured
5. ✅ Press Ctrl+C when done to save logs

**Workflow**:
```
1. Script installs APK
   ↓
2. App launches automatically
   ↓
3. You test detection scenarios
   ↓
4. Logs captured in real-time
   ↓
5. Press Ctrl+C to save and exit
```

---

## 🐛 Debugging v1.4.1 Rendering Issue

### Current Issue
- **Symptom**: UI shows "4 人臉" but only 1-2 green boxes render
- **Root cause**: Unknown (need logcat analysis)

### Debug Workflow

#### Step 1: Build APK with Debug Logging
In Android Studio:
1. Build → Build Bundle(s) / APK(s) → Build APK(s)
2. Wait for build to complete

#### Step 2: Deploy and Capture Logs
```powershell
cd tools
.\deploy-and-log.ps1
```

#### Step 3: Test Scenarios
While the script is running:
1. Test with **4-person scene** (reproduce the issue)
2. Move camera to different angles
3. Test close-up vs distant faces
4. Observe how many boxes render

#### Step 4: Save Logs
- Press **Ctrl+C** to stop capture
- Log file saved as `v1.4.1_logcat_YYYYMMDD_HHMMSS.txt`

#### Step 5: Analyze Logs
Look for patterns in the log file:

**Key Log Lines to Check**:
```
CameraRepositoryImpl: === Detected 4 faces ===
CameraRepositoryImpl: Face #0: trackingId=123, box=[L:100, T:200, R:300, B:400], size=[W:200, H:200]
CameraRepositoryImpl: Face #1: trackingId=456, box=[L:500, T:100, R:700, B:300], size=[W:200, H:200]
...

DetectionOverlay: === Animating 4 faces ===
DetectionOverlay: Animating Face #0: trackingId=123, rect=[L:100.0, T:200.0, R:300.0, B:400.0]
DetectionOverlay: Animating Face #1: trackingId=456, rect=[L:500.0, T:100.0, R:700.0, B:300.0]
...
```

**Questions to Answer**:
1. ✅ Are all 4 faces being detected by ML Kit? (CameraRepositoryImpl logs)
2. ✅ Are all 4 faces being animated? (DetectionOverlay logs)
3. ❓ Are coordinates within screen bounds? (check if L/T/R/B are positive and reasonable)
4. ❓ Are there tracking ID conflicts? (duplicate IDs)
5. ❓ Do some faces have negative coordinates? (off-screen)

---

## 📝 Log File Format

### Filename Pattern
```
v1.4.1_logcat_YYYYMMDD_HHMMSS.txt
```

### Example
```
v1.4.1_logcat_20251125_153045.txt
```

### Header
```
========================================
SmartCameraApp1 Logcat Capture
========================================
Timestamp: 20251125_153045
Package: local.mylab.smartcameraapp1
Filter Tags: CameraRepositoryImpl, DetectionOverlay, CameraViewModel
========================================
```

---

## 🛠️ Troubleshooting

### "ADB not found in PATH"
**Solution**:
1. Install Android SDK Platform Tools
2. Add to system PATH:
   - Windows: `C:\Users\<Username>\AppData\Local\Android\Sdk\platform-tools`
   - macOS/Linux: `~/Library/Android/sdk/platform-tools`
3. Restart PowerShell

### "No devices connected"
**Solution**:
1. Connect Android device via USB
2. Enable USB Debugging on device:
   - Settings → About Phone → Tap "Build Number" 7 times
   - Settings → Developer Options → USB Debugging
3. Verify with: `adb devices`
4. OR start Android Emulator

### "APK not found"
**Solution**:
Build APK first in Android Studio:
- `Build → Build Bundle(s) / APK(s) → Build APK(s)`

### Script Execution Policy Error
**Solution**:
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

---

## 📂 Output Files Location

All output files are saved to the **project root directory**:

```
SmartCameraApp1/
├── app/
├── tools/              ← Scripts are here
├── v1.4.1_logcat_*.txt ← Log files saved here
└── ...
```

---

## 💡 Tips

### Faster Testing Workflow
After first APK build:
```powershell
# In Android Studio: Make code changes and rebuild
# Then in PowerShell:
.\deploy-and-log.ps1
```

### Filter Specific Logs
To view only detection logs:
```powershell
Get-Content v1.4.1_logcat_*.txt | Select-String "Detected|Animating"
```

### Compare Test Runs
```powershell
# Run 1: Test scenario A
.\deploy-and-log.ps1
# Ctrl+C, logs saved as v1.4.1_logcat_20251125_153000.txt

# Run 2: Test scenario B
.\deploy-and-log.ps1
# Ctrl+C, logs saved as v1.4.1_logcat_20251125_153500.txt

# Compare files later
```

---

## 📜 Version History

- **1.0.0** (2025-11-25) - Initial release
  - APK installation script
  - Logcat capture script
  - Combined deploy and log script
  - Debugging support for v1.4.1 rendering issue

---

## 🤝 Contributing

When adding new scripts:
1. Follow PowerShell naming convention: `verb-noun.ps1`
2. Add color-coded output (Green=Success, Yellow=Warning, Red=Error)
3. Include error handling with `$ErrorActionPreference = "Stop"`
4. Update this README with usage instructions

---

## 📞 Support

For issues or questions:
- Check CHANGELOG.md for known issues
- Review logcat output for error messages
- Verify prerequisites are met (ADB, device connection)
