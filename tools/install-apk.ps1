# ============================================================================
# SmartCameraApp1 APK Installation Script
# ============================================================================
# Description: Installs the SmartCameraApp1 APK to a connected Android device
# Author: Claude Code
# Version: 1.0.0
# Date: 2025-11-25
# ============================================================================

# Enable strict error handling
$ErrorActionPreference = "Stop"

# ============================================================================
# Configuration
# ============================================================================
$APK_PATH = "app\build\outputs\apk\debug\app-debug.apk"
$PACKAGE_NAME = "com.akirachang.ai.photographer"
$MAIN_ACTIVITY = "$PACKAGE_NAME/.presentation.MainActivity"

# ============================================================================
# Color Output Functions
# ============================================================================
function Write-Success {
    param([string]$Message)
    Write-Host "[✓] $Message" -ForegroundColor Green
}

function Write-Info {
    param([string]$Message)
    Write-Host "[i] $Message" -ForegroundColor Cyan
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[!] $Message" -ForegroundColor Yellow
}

function Write-Error-Custom {
    param([string]$Message)
    Write-Host "[✗] $Message" -ForegroundColor Red
}

# ============================================================================
# Main Script
# ============================================================================
Write-Host ""
Write-Host "========================================" -ForegroundColor Magenta
Write-Host " SmartCameraApp1 APK Installer" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta
Write-Host ""

# Check if APK exists
Write-Info "Checking for APK file..."
if (-not (Test-Path $APK_PATH)) {
    Write-Error-Custom "APK not found at: $APK_PATH"
    Write-Warning "Please build the APK first in Android Studio:"
    Write-Warning "  Build -> Build Bundle(s) / APK(s) -> Build APK(s)"
    exit 1
}
Write-Success "APK found: $APK_PATH"

# Check if adb is available
Write-Info "Checking for ADB..."
try {
    $adbVersion = adb version 2>&1 | Select-String "Android Debug Bridge version"
    Write-Success "ADB found: $adbVersion"
} catch {
    Write-Error-Custom "ADB not found in PATH"
    Write-Warning "Please ensure Android SDK platform-tools is installed and in PATH"
    exit 1
}

# Check for connected devices
Write-Info "Checking for connected devices..."
$devices = adb devices | Select-String -Pattern "^\w+\s+device$"
if ($devices.Count -eq 0) {
    Write-Error-Custom "No devices connected"
    Write-Warning "Please connect an Android device via USB or start an emulator"
    Write-Warning "Enable USB Debugging in Developer Options on your device"
    exit 1
}
Write-Success "$($devices.Count) device(s) connected"

# Uninstall previous version (if exists)
Write-Info "Checking for previous installation..."
$installed = adb shell pm list packages | Select-String $PACKAGE_NAME
if ($installed) {
    Write-Warning "Previous version found, uninstalling..."
    adb uninstall $PACKAGE_NAME | Out-Null
    Write-Success "Previous version uninstalled"
} else {
    Write-Info "No previous installation found"
}

# Install APK
Write-Info "Installing APK..."
try {
    $installResult = adb install -r $APK_PATH 2>&1
    if ($installResult -match "Success") {
        Write-Success "APK installed successfully"
    } else {
        Write-Error-Custom "Installation failed: $installResult"
        exit 1
    }
} catch {
    Write-Error-Custom "Installation error: $_"
    exit 1
}

# Launch app
Write-Info "Launching app..."
try {
    adb shell am start -n $MAIN_ACTIVITY | Out-Null
    Write-Success "App launched successfully"
} catch {
    Write-Warning "Could not launch app automatically (app may still be installed)"
}

# Display completion message
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host " Installation Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Info "Package: $PACKAGE_NAME"
Write-Info "APK: $APK_PATH"
Write-Host ""
