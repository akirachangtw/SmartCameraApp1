# ============================================================================
# SmartCameraApp1 Deploy and Log Script
# ============================================================================
# Description: One-click deployment and logcat capture for SmartCameraApp1
# Author: Claude Code
# Version: 1.0.1
# Date: 2025-12-06
# ============================================================================
#
# USAGE (choose one of these methods):
#
#   Method 1 - Direct execution (if execution policy allows):
#     cd tools
#     .\deploy-and-log.ps1
#
#   Method 2 - Bypass execution policy (recommended for first-time users):
#     cd tools
#     powershell -ExecutionPolicy Bypass -File .\deploy-and-log.ps1
#
#   Method 3 - Set execution policy permanently:
#     Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
#     cd tools
#     .\deploy-and-log.ps1
#
# ============================================================================

# Enable strict error handling
$ErrorActionPreference = "Stop"

# ============================================================================
# Configuration
# ============================================================================
$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $SCRIPT_DIR
$INSTALL_SCRIPT = Join-Path $SCRIPT_DIR "install-apk.ps1"
$LOGCAT_SCRIPT = Join-Path $SCRIPT_DIR "capture-logcat.ps1"
$PACKAGE_NAME = "com.akirachang.ai.photographer"

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
Write-Host "================================================" -ForegroundColor Magenta
Write-Host " SmartCameraApp1 One-Click Deploy & Log" -ForegroundColor Magenta
Write-Host "================================================" -ForegroundColor Magenta
Write-Host ""

# Change to project root directory
Set-Location $PROJECT_ROOT
Write-Info "Working directory: $PROJECT_ROOT"
Write-Host ""

# ============================================================================
# Step 1: Install APK
# ============================================================================
Write-Host "================================================" -ForegroundColor Yellow
Write-Host " STEP 1: Installing APK" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow
Write-Host ""

if (-not (Test-Path $INSTALL_SCRIPT)) {
    Write-Error-Custom "Install script not found: $INSTALL_SCRIPT"
    exit 1
}

try {
    & $INSTALL_SCRIPT
    if ($LASTEXITCODE -ne 0) {
        Write-Error-Custom "Installation failed with exit code: $LASTEXITCODE"
        exit 1
    }
} catch {
    Write-Error-Custom "Installation failed: $_"
    exit 1
}

# ============================================================================
# Step 2: Wait for app to stabilize
# ============================================================================
Write-Host ""
Write-Info "Waiting 3 seconds for app to stabilize..."
Start-Sleep -Seconds 3
Write-Success "App should be ready"
Write-Host ""

# ============================================================================
# Step 3: Capture logcat
# ============================================================================
Write-Host "================================================" -ForegroundColor Yellow
Write-Host " STEP 2: Starting Logcat Capture" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow
Write-Host ""

if (-not (Test-Path $LOGCAT_SCRIPT)) {
    Write-Error-Custom "Logcat script not found: $LOGCAT_SCRIPT"
    exit 1
}

Write-Info "Use the app and test detection scenarios"
Write-Info "Logs will be captured in real-time"
Write-Warning "Press Ctrl+C when done testing to save the log file"
Write-Host ""

# Give user a moment to read instructions
Start-Sleep -Seconds 2

try {
    & $LOGCAT_SCRIPT
} catch {
    Write-Warning "Logcat capture interrupted"
}

# ============================================================================
# Completion
# ============================================================================
Write-Host ""
Write-Host "================================================" -ForegroundColor Green
Write-Host " Deploy & Log Session Complete!" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host ""
Write-Info "Next steps:"
Write-Host "  1. Review the generated logcat file" -ForegroundColor White
Write-Host "  2. Look for face detection coordinate patterns" -ForegroundColor White
Write-Host "  3. Check for animation system issues" -ForegroundColor White
Write-Host "  4. Analyze why only some boxes render" -ForegroundColor White
Write-Host ""
