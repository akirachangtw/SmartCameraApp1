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

### Core Deployment Tools

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

### Test Data Optimization Tools (⭐ NEW - v1.1.0)

These tools help optimize test data for Claude Code analysis, solving the **413 Request Too Large** error.

#### Problem
When analyzing test data with Claude Code, referencing `@others\v2.4.6-Testing\` with multiple PNG screenshots (~15 MB total) causes a 413 error due to Claude API request size limits (5-10 MB).

#### Solution
Three automated tools compress and organize test data:
- **compress-screenshots.ps1** - Compress PNG → JPEG (reduce 85-90% size)
- **extract-test-logs.ps1** - Extract log summaries (reduce 90% size)
- **organize-test-session.ps1** - One-click test data organization

---

### 4. `compress-screenshots.ps1` ⭐ NEW
Batch compress test screenshots to reduce file size.

**Usage**:
```powershell
cd tools
.\compress-screenshots.ps1 -InputDir "..\others\v2.4.6-Testing"
```

**What it does**:
1. ✅ PNG → JPEG conversion (quality 85%)
2. ✅ Auto-resize images (max width 1920px, maintains aspect ratio)
3. ✅ Backup originals to `originals/` subdirectory
4. ✅ Display compression statistics

**Parameters**:
- `-Quality` (default: 85) - JPEG quality (1-100)
- `-MaxWidth` (default: 1920) - Maximum width in pixels
- `-SkipBackup` - Don't keep original files (use with caution!)

**Examples**:
```powershell
# High quality compression (90%)
.\compress-screenshots.ps1 -Quality 90

# Aggressive compression (quality 75, resize to 1280px)
.\compress-screenshots.ps1 -Quality 75 -MaxWidth 1280
```

**Output**:
```
[✓] Compressed: 前鏡頭_20251213-170234.png
    Original: 2.3 MB → Compressed: 320 KB (86.1% reduction)
    Dimensions: 1080x2405 (unchanged)
[✓] Backed up originals to: originals/
```

---

### 5. `extract-test-logs.ps1` ⭐ NEW
Extract key information from logcat files to generate summaries.

**Usage**:
```powershell
cd tools
.\extract-test-logs.ps1 -LogFile "..\others\v2.4.6-Testing\v2.4.6_logcat_20251213_170123.txt"
```

**What it does**:
1. ✅ Filter ERROR/FATAL/Exception messages
2. ✅ Keep 4 core tag logs
3. ✅ Extract composition analysis events
4. ✅ Generate statistics (face detection count, errors, time range)
5. ✅ Output concise summary file (target <50 KB)

**Filtering Rules**:
**Keeps**:
- ERROR, FATAL, Exception messages
- `Detected X faces` - Face detection events
- `Final message:` - Composition suggestions
- `HDR mode` - HDR state changes
- `GuideData:` - Guide data
- `Eye position` - Eye position analysis
- `Composition score` - Composition scores

**Removes**:
- Repeated DEBUG messages
- Preview size updates (too frequent)
- System framework logs

**Examples**:
```powershell
# Generate summary in same directory
.\extract-test-logs.ps1 -LogFile "v2.4.6_logcat_20251213_170123.txt"

# Output to specific directory
.\extract-test-logs.ps1 -LogFile "v2.4.6_logcat_20251213_170123.txt" `
                        -OutputDir "others\v2.4.6-Testing\logs"
```

**Output**:
```
========================================
SmartCameraApp1 Test Log Summary
========================================
Source: v2.4.6_logcat_20251213_170123.txt
Generated: 2025-12-13 18:30:15

--- SESSION STATISTICS ---
Total log lines: 3733
Filtered lines: 287
Reduction: 92.3%
Face detections: 156
Errors: 0
Time range: 12-13 17:00:10 - 12-13 17:03:45

[✓] Log summary saved: summary_v2.4.6_logcat_20251213_170123.txt
    Original: 486 KB → Summary: 42 KB (91.4% reduction)
```

---

### 6. `organize-test-session.ps1` (⭐ Recommended) ⭐ NEW
One-click organization of all test session data.

**Usage**:
```powershell
cd tools
.\organize-test-session.ps1 -SourceDir "..\others\v2.4.6-Testing"
```

**What it does**:
1. ✅ Create standardized directory structure (`optimized/` and `originals/`)
2. ✅ Move original files to `originals/` subdirectory
3. ✅ Auto-compress screenshots to JPEG (calls compress-screenshots.ps1)
4. ✅ Auto-generate log summaries (calls extract-test-logs.ps1)
5. ✅ Output optimized files to `optimized/` for Claude Code analysis

**Directory Structure**:
```
others/v2.4.6-Testing/
├── optimized/                                  (optimized files for analysis)
│   ├── 前鏡頭_20251213-170234.jpg             (JPEG screenshots)
│   ├── 前鏡頭_20251213-170257.jpg
│   ├── 後鏡頭Screenshot_20251213-170034.jpg
│   ├── summary_v2.4.6_logcat_20251213_170010.txt  (log summaries)
│   └── summary_v2.4.6_logcat_20251213_170123.txt
└── originals/                                  (original backup files)
    ├── 前鏡頭_20251213-170234.png             (PNG screenshots)
    ├── 前鏡頭_20251213-170257.png
    ├── 後鏡頭Screenshot_20251213-170034.png
    ├── v2.4.6_logcat_20251213_170010.txt      (full logcat)
    └── v2.4.6_logcat_20251213_170123.txt
```

**Parameters**:
- `-Version` (default: auto-extract from build.gradle.kts) - App version
- `-SkipCompression` - Skip screenshot compression
- `-SkipLogExtraction` - Skip log summary generation

**Examples**:
```powershell
# Full organization (recommended)
.\organize-test-session.ps1 -SourceDir "others\v2.4.6-Testing"

# Only organize structure, don't optimize
.\organize-test-session.ps1 -SkipCompression -SkipLogExtraction
```

---

### 📋 Complete Testing Workflow (Updated)

#### Method 1: Traditional Workflow (Manual Optimization)
```powershell
# Step 1: Deploy + capture logs
cd tools
.\deploy-and-log.ps1

# Step 2: Test the app
# (Use app on device, it will auto-screenshot)

# Step 3: Ctrl+C to stop log capture

# Step 4: Copy screenshots from device
# Device path: Pictures/SmartCamera/*.png

# Step 5: Organize test data
.\organize-test-session.ps1 -SourceDir "..\others\v2.4.6-Testing"

# Step 6: Analyze with Claude Code
# Reference: @others/v2.4.6-Testing/optimized/
```

#### Method 2: Quick Workflow (Recommended)
```powershell
# One-click deploy + test + optimize
cd tools

# 1. Deploy and test
.\deploy-and-log.ps1
# (Test complete, press Ctrl+C)

# 2. Manually copy screenshots to test directory

# 3. One-click optimize
.\organize-test-session.ps1
```

---

### 💾 File Size Comparison

#### Before Optimization (Cannot Analyze)
```
others/v2.4.6-Testing/
├── 前鏡頭_20251213-170234.png     2.2 MB
├── 前鏡頭_20251213-170257.png     2.3 MB
├── 前鏡頭_20251213-170308.png     2.1 MB
├── 後鏡頭_20251213-170034.png     2.4 MB
├── 後鏡頭_20251213-170145.png     2.3 MB
├── 後鏡頭_20251213-170206.png     2.6 MB
├── v2.4.6_logcat_*.txt (1)        52 KB
└── v2.4.6_logcat_*.txt (2)       486 KB
----------------------------------------
Total:                            ~15 MB  ❌ Exceeds Claude API limit
```

#### After Optimization (Ready for Analysis)
```
others/v2.4.6-Testing/
├── optimized/
│   ├── 前鏡頭_20251213-170234.jpg   280 KB
│   ├── 前鏡頭_20251213-170257.jpg   320 KB
│   ├── 前鏡頭_20251213-170308.jpg   270 KB
│   ├── 後鏡頭_20251213-170034.jpg   350 KB
│   ├── 後鏡頭_20251213-170145.jpg   310 KB
│   ├── 後鏡頭_20251213-170206.jpg   380 KB
│   ├── summary_*.txt (1)             5 KB
│   └── summary_*.txt (2)            38 KB
└── originals/
    └── [original files backed up]
----------------------------------------
Total (optimized/):                 ~2.0 MB  ✅ Fits Claude API requirement
Reduction:                               87%
```

---

### 🎓 Tool Selection Guide

| Need | Use Tool |
|------|----------|
| Only compress screenshots | `compress-screenshots.ps1` |
| Only generate log summaries | `extract-test-logs.ps1` |
| Full test data organization | `organize-test-session.ps1` ⭐ |
| One-click deploy + test | `deploy-and-log.ps1` |

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

- **1.1.0** (2025-12-13) - Test Data Optimization Tools
  - ✨ NEW: `compress-screenshots.ps1` - PNG → JPEG compression (85-90% reduction)
  - ✨ NEW: `extract-test-logs.ps1` - Logcat summary generation (90% reduction)
  - ✨ NEW: `organize-test-session.ps1` - One-click test session organization
  - 🎯 Solves Claude Code 413 Request Too Large error
  - 📝 Updated testing workflow and documentation

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
