# ============================================================================
# SmartCameraApp1 Test Session Organizer
# ============================================================================
# Description: Organizes test data and generates comprehensive test reports
# Author: Claude Code
# Version: 1.0.0
# Date: 2025-12-13
# ============================================================================

[CmdletBinding()]
param(
    [Parameter(Mandatory=$false)]
    [string]$SourceDir = ".",

    [Parameter(Mandatory=$false)]
    [string]$Version = $null,

    [Parameter(Mandatory=$false)]
    [switch]$SkipCompression,

    [Parameter(Mandatory=$false)]
    [switch]$SkipLogExtraction,

    [Parameter(Mandatory=$false)]
    [string]$OutputBase = "others"
)

# Enable strict error handling
$ErrorActionPreference = "Stop"

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
# Script-level path initialization
# ============================================================================
$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $SCRIPT_DIR

# ============================================================================
# Helper Functions
# ============================================================================

function Get-AppVersion {
    $BUILD_GRADLE = Join-Path $PROJECT_ROOT "app\build.gradle.kts"

    if (Test-Path $BUILD_GRADLE) {
        $content = Get-Content $BUILD_GRADLE -Raw
        if ($content -match 'versionName\s*=\s*"([^"]+)"') {
            return $matches[1]
        }
    }

    return "unknown"
}

function Initialize-DirectoryStructure {
    param(
        [string]$Version,
        [string]$BaseDir
    )

    if (-not [System.IO.Path]::IsPathRooted($BaseDir)) {
        $BaseDir = Join-Path $PROJECT_ROOT $BaseDir
    }

    $sessionDir = Join-Path $BaseDir "v$Version-Testing"

    $structure = @{
        Root = $sessionDir
        Optimized = Join-Path $sessionDir "optimized"
        Originals = Join-Path $sessionDir "originals"
    }

    foreach ($dir in $structure.Values) {
        if (-not (Test-Path $dir)) {
            New-Item -ItemType Directory -Path $dir -Force | Out-Null
        }
    }

    return $structure
}

function Move-TestFiles {
    param(
        [string]$SourceDir,
        [hashtable]$Structure
    )

    $stats = @{
        LogsMoved = 0
        ScreenshotsMoved = 0
        LogFiles = @()
        ScreenshotFiles = @()
    }

    # Move logcat files to originals/
    $logFiles = Get-ChildItem -Path $SourceDir -Filter "*.txt" -File |
                Where-Object { $_.Name -match '^v.*_logcat_.*\.txt$' -and $_.DirectoryName -eq $SourceDir }

    foreach ($log in $logFiles) {
        $destination = Join-Path $Structure.Originals $log.Name
        Move-Item -Path $log.FullName -Destination $destination -Force
        $stats.LogsMoved++
        $stats.LogFiles += $log.Name
    }

    # Move screenshots to originals/
    $screenshots = Get-ChildItem -Path $SourceDir -Filter "*.png" -File |
                   Where-Object { $_.DirectoryName -eq $SourceDir }

    foreach ($screenshot in $screenshots) {
        $destination = Join-Path $Structure.Originals $screenshot.Name
        Move-Item -Path $screenshot.FullName -Destination $destination -Force
        $stats.ScreenshotsMoved++
        $stats.ScreenshotFiles += $screenshot.Name
    }

    return $stats
}

# ============================================================================
# Main Script
# ============================================================================
Write-Host ""
Write-Host "========================================" -ForegroundColor Magenta
Write-Host " Test Session Organizer" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta
Write-Host ""

# Determine version
if (-not $Version) {
    Write-Info "Extracting version from build.gradle.kts..."
    $Version = Get-AppVersion
}
Write-Success "Version: v$Version"

# Validate source directory
Write-Info "Validating source directory..."
if (-not (Test-Path $SourceDir)) {
    Write-Error-Custom "Source directory does not exist: $SourceDir"
    exit 1
}

$SourceDir = Resolve-Path $SourceDir
Write-Success "Source: $SourceDir"
Write-Host ""

# Initialize directory structure
Write-Info "Creating directory structure..."
$structure = Initialize-DirectoryStructure -Version $Version -BaseDir $OutputBase
Write-Success "Directory structure created"
Write-Host "  Root: $($structure.Root)" -ForegroundColor Gray
Write-Host "  Originals: $($structure.Originals)" -ForegroundColor Gray
Write-Host "  Optimized: $($structure.Optimized)" -ForegroundColor Gray
Write-Host ""

# Move files
Write-Info "Organizing test files..."
$moveStats = Move-TestFiles -SourceDir $SourceDir -Structure $structure
Write-Success "Files organized"
Write-Host "  Logs moved: $($moveStats.LogsMoved)" -ForegroundColor Gray
Write-Host "  Screenshots moved: $($moveStats.ScreenshotsMoved)" -ForegroundColor Gray
Write-Host ""

# Compress screenshots
$pngToCompress = @()
if (Test-Path $structure.Originals) {
    $pngToCompress = Get-ChildItem -Path $structure.Originals -Filter "*.png" -File -ErrorAction SilentlyContinue
}

if (-not $SkipCompression -and $pngToCompress.Count -gt 0) {
    Write-Info "Compressing screenshots..."
    $compressScript = Join-Path $PSScriptRoot "compress-screenshots.ps1"

    if (Test-Path $compressScript) {
        Write-Host ""
        & $compressScript -InputDir $structure.Originals -OutputDir $structure.Optimized -Quality 85 -SkipBackup
        Write-Host ""
    } else {
        Write-Warning "compress-screenshots.ps1 not found, skipping compression"
    }
} else {
    if ($SkipCompression) {
        Write-Info "Compression skipped (user requested)"
    } else {
        Write-Info "No screenshots to compress"
    }
}

# Extract log summaries
$logsToExtract = @()
if (Test-Path $structure.Originals) {
    $logsToExtract = Get-ChildItem -Path $structure.Originals -Filter "v*.txt" -File -ErrorAction SilentlyContinue
}

if (-not $SkipLogExtraction -and $logsToExtract.Count -gt 0) {
    Write-Info "Extracting log summaries..."
    $extractScript = Join-Path $PSScriptRoot "extract-test-logs.ps1"

    if (Test-Path $extractScript) {
        foreach ($log in $logsToExtract) {
            Write-Host ""
            & $extractScript -LogFile $log.FullName -OutputDir $structure.Optimized
            Write-Host ""
        }
    } else {
        Write-Warning "extract-test-logs.ps1 not found, skipping log extraction"
    }
} else {
    if ($SkipLogExtraction) {
        Write-Info "Log extraction skipped (user requested)"
    } else {
        Write-Info "No logs to extract"
    }
}

# Collect processed files statistics
$optimizedFiles = @()
$originalFiles = @()

if (Test-Path $structure.Optimized) {
    $optimizedFiles = Get-ChildItem -Path $structure.Optimized -File
}

if (Test-Path $structure.Originals) {
    $originalFiles = Get-ChildItem -Path $structure.Originals -File
}

# Display summary
Write-Host "========================================" -ForegroundColor Magenta
Write-Host " Organization Summary" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta
Write-Host ""

Write-Host "  Test Session: v$Version" -ForegroundColor Cyan
Write-Host "  Location: $($structure.Root)" -ForegroundColor Cyan
Write-Host ""

Write-Host "  Files Organized:" -ForegroundColor White
Write-Host "    Original files: $($originalFiles.Count)" -ForegroundColor Gray
Write-Host "    Optimized files: $($optimizedFiles.Count)" -ForegroundColor Gray
Write-Host ""

Write-Success "Test session organized successfully!"
Write-Host ""

Write-Info "Next steps:"
Write-Host "  1. Use Claude Code to analyze optimized files:" -ForegroundColor White
Write-Host "     @$($structure.Root.Replace($PWD.Path + '\', ''))optimized/" -ForegroundColor Cyan
Write-Host ""
Write-Host "  2. View original files (if needed):" -ForegroundColor White
Write-Host "     @$($structure.Root.Replace($PWD.Path + '\', ''))originals/" -ForegroundColor Cyan
Write-Host ""
