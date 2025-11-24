# ============================================================================
# SmartCameraApp1 Logcat Capture Script
# ============================================================================
# Description: Captures and filters logcat output for SmartCameraApp1 debugging
# Author: Claude Code
# Version: 1.0.0
# Date: 2025-11-25
# ============================================================================

# Enable strict error handling
$ErrorActionPreference = "Stop"

# ============================================================================
# Configuration
# ============================================================================
$PACKAGE_NAME = "local.mylab.smartcameraapp1"
$LOG_TAGS = @("CameraRepositoryImpl", "DetectionOverlay", "CameraViewModel")
$OUTPUT_DIR = "."
$TIMESTAMP = Get-Date -Format "yyyyMMdd_HHmmss"
$LOG_FILE = Join-Path $OUTPUT_DIR "v1.4.1_logcat_$TIMESTAMP.txt"

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
Write-Host " SmartCameraApp1 Logcat Capture" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta
Write-Host ""

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
    exit 1
}
Write-Success "$($devices.Count) device(s) connected"

# Clear logcat buffer
Write-Info "Clearing logcat buffer..."
adb logcat -c
Write-Success "Logcat buffer cleared"

# Display capture info
Write-Host ""
Write-Info "Capture Configuration:"
Write-Host "  Package: $PACKAGE_NAME" -ForegroundColor White
Write-Host "  Filter Tags: $($LOG_TAGS -join ', ')" -ForegroundColor White
Write-Host "  Output File: $LOG_FILE" -ForegroundColor White
Write-Host ""
Write-Success "Starting logcat capture..."
Write-Warning "Press Ctrl+C to stop capture"
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Live Logcat Output" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Build filter expression for logcat
# Format: tag:priority (e.g., "CameraRepositoryImpl:D")
$filterExpression = ($LOG_TAGS | ForEach-Object { "${_}:D" }) -join " "
$filterExpression += " *:S"  # Suppress all other tags

# Create output file with header
$header = @"
========================================
SmartCameraApp1 Logcat Capture
========================================
Timestamp: $TIMESTAMP
Package: $PACKAGE_NAME
Filter Tags: $($LOG_TAGS -join ', ')
========================================

"@
$header | Out-File -FilePath $LOG_FILE -Encoding UTF8

# Capture logcat with filtering
try {
    # Use -Append to add to the file we just created
    adb logcat $filterExpression | Tee-Object -FilePath $LOG_FILE -Append
} catch {
    Write-Host ""
    Write-Warning "Capture interrupted by user"
}

# Display completion message
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host " Capture Completed!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Success "Log saved to: $LOG_FILE"

# Count log lines
$lineCount = (Get-Content $LOG_FILE | Measure-Object -Line).Lines
Write-Info "Total lines captured: $lineCount"
Write-Host ""
