# ============================================================================
# SmartCameraApp1 Test Log Extraction Tool
# ============================================================================
# Description: Extracts key information from logcat files for analysis
# Author: Claude Code
# Version: 1.0.0
# Date: 2025-12-13
# ============================================================================

[CmdletBinding()]
param(
    [Parameter(Mandatory=$true, Position=0)]
    [string]$LogFile,

    [Parameter(Mandatory=$false)]
    [string]$OutputDir = ".",

    [Parameter(Mandatory=$false)]
    [switch]$IncludeDebug
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
# Log Pattern Definitions
# ============================================================================
$CRITICAL_PATTERNS = @{
    # Errors and warnings
    Errors = '(ERROR|FATAL|Exception|Error:|Failed)'
    Warnings = '(WARN|WARNING)'

    # Core tags
    CoreTags = '(CameraRepositoryImpl|DetectionOverlay|CameraViewModel|AnalyzeCompositionUseCase)'

    # Composition events
    FaceDetection = 'Detected (\d+) face'
    CompositionMessage = 'Final message:'
    GuideData = 'GuideData:'
    CompositionScore = '(overall:|Scores -)'

    # HDR and camera events
    HDRToggle = 'HDR mode (enabled|disabled)'
    EyePosition = '(Eye position|eyeY:)'
    EdgeDistances = 'Edge distances'

    # Initialization events
    Initialization = '(initialized|started)'
}

# ============================================================================
# Helper Functions
# ============================================================================

function Match-LogLine {
    param([string]$Line)

    # Always include errors/fatal
    if ($Line -match $CRITICAL_PATTERNS.Errors) {
        return $true
    }

    # Include warnings
    if ($Line -match $CRITICAL_PATTERNS.Warnings) {
        return $true
    }

    # Include core tags
    if ($Line -match $CRITICAL_PATTERNS.CoreTags) {
        # Check for specific composition events
        if ($Line -match $CRITICAL_PATTERNS.FaceDetection -or
            $Line -match $CRITICAL_PATTERNS.CompositionMessage -or
            $Line -match $CRITICAL_PATTERNS.GuideData -or
            $Line -match $CRITICAL_PATTERNS.CompositionScore -or
            $Line -match $CRITICAL_PATTERNS.HDRToggle -or
            $Line -match $CRITICAL_PATTERNS.EyePosition -or
            $Line -match $CRITICAL_PATTERNS.EdgeDistances -or
            $Line -match $CRITICAL_PATTERNS.Initialization) {
            return $true
        }
    }

    # Include DEBUG if requested
    if ($IncludeDebug -and $Line -match $CRITICAL_PATTERNS.CoreTags) {
        return $true
    }

    return $false
}

function Analyze-LogSession {
    param([string[]]$LogContent)

    $analysis = @{
        TotalLines = 0
        FilteredLines = 0
        FaceDetectionCount = 0
        TotalFaces = 0
        ErrorCount = 0
        WarningCount = 0
        HDRToggles = 0
        CompositionMessages = @()
        TimeRange = @{Start = $null; End = $null}
    }

    foreach ($line in $LogContent) {
        $analysis.TotalLines++

        # Extract timestamp
        if ($line -match '(\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3})') {
            $timestamp = $matches[1]
            if (-not $analysis.TimeRange.Start) {
                $analysis.TimeRange.Start = $timestamp
            }
            $analysis.TimeRange.End = $timestamp
        }

        # Count face detections
        if ($line -match 'Detected (\d+) face') {
            $analysis.FaceDetectionCount++
            $analysis.TotalFaces += [int]$matches[1]
        }

        # Count errors
        if ($line -match '(ERROR|FATAL|Exception)') {
            $analysis.ErrorCount++
        }

        # Count warnings
        if ($line -match '(WARN|WARNING)') {
            $analysis.WarningCount++
        }

        # Count HDR toggles
        if ($line -match 'HDR mode (enabled|disabled)') {
            $analysis.HDRToggles++
        }

        # Extract composition messages
        if ($line -match 'Final message: (.+)') {
            $message = $matches[1]
            if ($message -notin $analysis.CompositionMessages) {
                $analysis.CompositionMessages += $message
            }
        }
    }

    return $analysis
}

function Filter-LogContent {
    param(
        [string]$LogFilePath
    )

    $content = Get-Content $LogFilePath -Encoding UTF8
    $filtered = @()

    # Check for header and preserve it
    $headerEndIndex = -1
    for ($i = 0; $i -lt $content.Length; $i++) {
        if ($content[$i] -match '^====') {
            $headerEndIndex = $i
        }
        if ($headerEndIndex -ne -1 -and $content[$i] -match '^\s*$' -and $i -gt $headerEndIndex) {
            # Found end of header
            $filtered += $content[0..$i]
            break
        }
        if ($i -gt 20) {
            # No header found, start filtering from beginning
            break
        }
    }

    if ($filtered.Count -eq 0) {
        # No header, add our own
        $filtered += "========================================"
        $filtered += "SmartCameraApp1 Logcat (Filtered)"
        $filtered += "========================================"
        $filtered += ""
    }

    # Add filter notice
    $filtered += "=== FILTERED SUMMARY (Key Events Only) ==="
    $filtered += ""

    # Filter critical lines
    $startIndex = if ($headerEndIndex -ne -1) { $headerEndIndex + 2 } else { 0 }

    for ($i = $startIndex; $i -lt $content.Length; $i++) {
        $line = $content[$i]

        if (Match-LogLine -Line $line) {
            $filtered += $line
        }
    }

    return $filtered
}

function Format-FileSize {
    param([long]$Bytes)

    if ($Bytes -ge 1MB) {
        return "{0:N2} MB" -f ($Bytes / 1MB)
    } elseif ($Bytes -ge 1KB) {
        return "{0:N0} KB" -f ($Bytes / 1KB)
    } else {
        return "$Bytes bytes"
    }
}

# ============================================================================
# Main Script
# ============================================================================
Write-Host ""
Write-Host "========================================" -ForegroundColor Magenta
Write-Host " Test Log Extraction Tool" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta
Write-Host ""

# Validate log file
Write-Info "Validating log file..."
if (-not (Test-Path $LogFile)) {
    Write-Error-Custom "Log file does not exist: $LogFile"
    exit 1
}

$LogFile = Resolve-Path $LogFile
$originalSize = (Get-Item $LogFile).Length

Write-Success "Log file found: $(Split-Path $LogFile -Leaf)"
Write-Host "  Size: $(Format-FileSize $originalSize)" -ForegroundColor Gray
Write-Host ""

# Validate output directory
if (-not (Test-Path $OutputDir)) {
    Write-Info "Creating output directory: $OutputDir"
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
}

$OutputDir = Resolve-Path $OutputDir

# Filter log content
Write-Info "Filtering log content..."
$fullContent = Get-Content $LogFile -Encoding UTF8
$filteredContent = Filter-LogContent -LogFilePath $LogFile

Write-Success "Filtered $($fullContent.Count) lines → $($filteredContent.Count) lines"
Write-Host ""

# Analyze session
Write-Info "Analyzing session statistics..."
$stats = Analyze-LogSession -LogContent $fullContent

# Generate summary file
$logFileName = Split-Path $LogFile -Leaf
$summaryFileName = "summary_$logFileName"
$summaryFile = Join-Path $OutputDir $summaryFileName

# Build summary content
$summary = @()
$summary += "========================================"
$summary += "SmartCameraApp1 Test Log Summary"
$summary += "========================================"
$summary += "Source: $logFileName"
$summary += "Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
$summary += ""
$summary += "--- SESSION STATISTICS ---"
$summary += "Total log lines: $($stats.TotalLines)"
$summary += "Filtered lines: $($filteredContent.Count)"
$summary += "Reduction: $([math]::Round((1 - $filteredContent.Count/$stats.TotalLines) * 100, 1))%"
$summary += "Face detection events: $($stats.FaceDetectionCount)"
$summary += "Total faces detected: $($stats.TotalFaces)"
$summary += "Errors: $($stats.ErrorCount)"
$summary += "Warnings: $($stats.WarningCount)"
$summary += "HDR toggles: $($stats.HDRToggles)"

if ($stats.TimeRange.Start -and $stats.TimeRange.End) {
    $summary += "Time range: $($stats.TimeRange.Start) - $($stats.TimeRange.End)"
}

$summary += ""

if ($stats.CompositionMessages.Count -gt 0) {
    $summary += "--- COMPOSITION MESSAGES ---"
    foreach ($msg in $stats.CompositionMessages) {
        $summary += "  - $msg"
    }
    $summary += ""
}

if ($stats.ErrorCount -gt 0) {
    $summary += "--- ERRORS DETECTED ---"
    $summary += "Check filtered log content below for details"
    $summary += ""
}

$summary += "--- FILTERED LOG CONTENT ---"
$summary += $filteredContent

# Save summary
$summary | Out-File -FilePath $summaryFile -Encoding UTF8

$summarySize = (Get-Item $summaryFile).Length
$reduction = [math]::Round((1 - $summarySize / $originalSize) * 100, 1)

# Display results
Write-Host "========================================" -ForegroundColor Magenta
Write-Host " Extraction Summary" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta
Write-Host ""

Write-Host "  Session Statistics:" -ForegroundColor Cyan
Write-Host "    Total lines: $($stats.TotalLines)" -ForegroundColor White
Write-Host "    Filtered lines: $($filteredContent.Count)" -ForegroundColor White
Write-Host "    Face detections: $($stats.FaceDetectionCount) events ($($stats.TotalFaces) total faces)" -ForegroundColor White
Write-Host "    Errors: $($stats.ErrorCount)" -ForegroundColor $(if ($stats.ErrorCount -gt 0) { 'Red' } else { 'Green' })
Write-Host "    Warnings: $($stats.WarningCount)" -ForegroundColor $(if ($stats.WarningCount -gt 0) { 'Yellow' } else { 'Gray' })
Write-Host "    HDR toggles: $($stats.HDRToggles)" -ForegroundColor White

if ($stats.CompositionMessages.Count -gt 0) {
    Write-Host ""
    Write-Host "  Composition Messages:" -ForegroundColor Cyan
    foreach ($msg in $stats.CompositionMessages) {
        Write-Host "    - $msg" -ForegroundColor White
    }
}

Write-Host ""
Write-Host "  File Size:" -ForegroundColor Cyan
Write-Host "    Original: $(Format-FileSize $originalSize)" -ForegroundColor White
Write-Host "    Summary: $(Format-FileSize $summarySize)" -ForegroundColor White
Write-Host "    Reduction: $reduction%" -ForegroundColor Green

Write-Host ""
Write-Success "Summary saved to: $summaryFileName"

if ($stats.ErrorCount -gt 0) {
    Write-Host ""
    Write-Warning "Found $($stats.ErrorCount) error(s) in the log - review the summary file"
}

Write-Host ""
