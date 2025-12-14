# ============================================================================
# SmartCameraApp1 Screenshot Compression Tool
# ============================================================================
# Description: Compresses PNG screenshots to JPEG for Claude Code analysis
# Author: Claude Code
# Version: 1.0.0
# Date: 2025-12-13
# ============================================================================

[CmdletBinding()]
param(
    [Parameter(Mandatory=$false)]
    [string]$InputDir = ".",

    [Parameter(Mandatory=$false)]
    [string]$OutputDir = $null,

    [Parameter(Mandatory=$false)]
    [ValidateRange(1, 100)]
    [int]$Quality = 85,

    [Parameter(Mandatory=$false)]
    [ValidateRange(640, 4096)]
    [int]$MaxWidth = 1920,

    [Parameter(Mandatory=$false)]
    [switch]$SkipBackup
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
# Helper Functions
# ============================================================================

function Test-Dependencies {
    try {
        Add-Type -AssemblyName System.Drawing
        return $true
    } catch {
        Write-Error-Custom "System.Drawing assembly not available"
        Write-Warning "This tool requires .NET Framework with GDI+ support"
        return $false
    }
}

function Get-JpegEncoder {
    $encoders = [System.Drawing.Imaging.ImageCodecInfo]::GetImageEncoders()
    foreach ($encoder in $encoders) {
        if ($encoder.MimeType -eq 'image/jpeg') {
            return $encoder
        }
    }
    throw "JPEG encoder not found"
}

function Calculate-TargetSize {
    param(
        [System.Drawing.Image]$SourceImage,
        [int]$MaxWidth
    )

    $originalWidth = $SourceImage.Width
    $originalHeight = $SourceImage.Height

    # If original width is smaller than max width, don't resize
    if ($originalWidth -le $MaxWidth) {
        return @{
            Width = $originalWidth
            Height = $originalHeight
            Resized = $false
        }
    }

    # Calculate scale ratio (maintain aspect ratio)
    $ratio = $MaxWidth / $originalWidth
    $targetWidth = $MaxWidth
    $targetHeight = [math]::Round($originalHeight * $ratio)

    return @{
        Width = $targetWidth
        Height = $targetHeight
        Resized = $true
    }
}

function Compress-ImageFile {
    param(
        [string]$SourcePath,
        [string]$OutputPath,
        [int]$Quality,
        [int]$MaxWidth
    )

    $sourceImage = $null
    $targetBitmap = $null
    $graphics = $null

    try {
        # Load source image
        $sourceImage = [System.Drawing.Image]::FromFile($SourcePath)

        # Calculate target size
        $targetSize = Calculate-TargetSize -SourceImage $sourceImage -MaxWidth $MaxWidth

        # Create target bitmap
        $targetBitmap = New-Object System.Drawing.Bitmap($targetSize.Width, $targetSize.Height)

        # High quality scaling
        $graphics = [System.Drawing.Graphics]::FromImage($targetBitmap)
        $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
        $graphics.DrawImage($sourceImage, 0, 0, $targetSize.Width, $targetSize.Height)

        # Setup JPEG encoder
        $jpegEncoder = Get-JpegEncoder
        $encoderParams = New-Object System.Drawing.Imaging.EncoderParameters(1)
        $encoderParams.Param[0] = New-Object System.Drawing.Imaging.EncoderParameter(
            [System.Drawing.Imaging.Encoder]::Quality,
            [long]$Quality
        )

        # Save JPEG
        $targetBitmap.Save($OutputPath, $jpegEncoder, $encoderParams)

        return @{
            Success = $true
            OriginalSize = (Get-Item $SourcePath).Length
            CompressedSize = (Get-Item $OutputPath).Length
            OriginalDimensions = "$($sourceImage.Width)x$($sourceImage.Height)"
            TargetDimensions = "$($targetSize.Width)x$($targetSize.Height)"
            Resized = $targetSize.Resized
        }

    } catch {
        throw "Image compression failed: $($_.Exception.Message)"
    } finally {
        # Clean up resources
        if ($graphics) { $graphics.Dispose() }
        if ($targetBitmap) { $targetBitmap.Dispose() }
        if ($sourceImage) { $sourceImage.Dispose() }
    }
}

function Backup-OriginalFiles {
    param(
        [string]$SourceDir,
        [array]$Files
    )

    # Create originals/ subdirectory
    $backupDir = Join-Path $SourceDir "originals"
    if (-not (Test-Path $backupDir)) {
        New-Item -ItemType Directory -Path $backupDir | Out-Null
    }

    # Copy original files
    foreach ($file in $Files) {
        Copy-Item -Path $file.FullName -Destination $backupDir -Force
    }

    return $backupDir
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
Write-Host " Screenshot Compression Tool" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta
Write-Host ""

# Check dependencies
Write-Info "Checking dependencies..."
if (-not (Test-Dependencies)) {
    exit 1
}
Write-Success "System.Drawing available"

# Validate input directory
Write-Info "Validating input directory..."
if (-not (Test-Path $InputDir)) {
    Write-Error-Custom "Input directory does not exist: $InputDir"
    exit 1
}

if (-not (Test-Path $InputDir -PathType Container)) {
    Write-Error-Custom "Path is not a directory: $InputDir"
    exit 1
}

$InputDir = Resolve-Path $InputDir

# Find PNG files
Write-Info "Searching for PNG files..."
$pngFiles = Get-ChildItem -Path $InputDir -Filter "*.png" -File

if ($pngFiles.Count -eq 0) {
    Write-Warning "No PNG files found in directory: $InputDir"
    exit 0
}

Write-Success "Found $($pngFiles.Count) PNG file(s)"

# Determine output directory
if ($OutputDir) {
    # Use specified output directory
    $compressedDir = $OutputDir
    if (-not (Test-Path $compressedDir)) {
        New-Item -ItemType Directory -Path $compressedDir -Force | Out-Null
    }
    $outputDisplayPath = $compressedDir
} else {
    # Default: create compressed/ subdirectory in InputDir
    $compressedDir = Join-Path $InputDir "compressed"
    if (-not (Test-Path $compressedDir)) {
        New-Item -ItemType Directory -Path $compressedDir -Force | Out-Null
    }
    $outputDisplayPath = "compressed/"
}

# Display configuration
Write-Host ""
Write-Info "Compression Configuration:"
Write-Host "  Quality: $Quality%" -ForegroundColor White
Write-Host "  Max Width: ${MaxWidth}px" -ForegroundColor White
Write-Host "  Backup: $(if ($SkipBackup) { 'No' } else { 'Yes' })" -ForegroundColor White
Write-Host "  Output: $outputDisplayPath" -ForegroundColor White
Write-Host ""

# Backup original files
if (-not $SkipBackup) {
    Write-Info "Backing up original files..."
    $backupDir = Backup-OriginalFiles -SourceDir $InputDir -Files $pngFiles
    Write-Success "Backed up to: $backupDir"
    Write-Host ""
}

# Process each PNG file
$results = @()
$totalOriginalSize = 0
$totalCompressedSize = 0

foreach ($png in $pngFiles) {
    # Output JPEG to compressed/ subdirectory
    $jpegFileName = [System.IO.Path]::GetFileNameWithoutExtension($png.Name) + ".jpg"
    $jpegPath = Join-Path $compressedDir $jpegFileName

    Write-Info "Processing: $($png.Name)"

    try {
        $result = Compress-ImageFile -SourcePath $png.FullName `
                                     -OutputPath $jpegPath `
                                     -Quality $Quality `
                                     -MaxWidth $MaxWidth

        # Only delete originals when writing back into the same folder (default behavior).
        # When -OutputDir is used (e.g. optimized/originals workflow), keep the PNG source.
        if (-not $OutputDir) {
            Remove-Item $png.FullName -Force
        }

        # Calculate reduction
        $reduction = [math]::Round((1 - $result.CompressedSize / $result.OriginalSize) * 100, 1)

        Write-Success "Compressed: $($png.Name)"
        Write-Host "    Original: $(Format-FileSize $result.OriginalSize) → Compressed: $(Format-FileSize $result.CompressedSize) ($reduction% reduction)" -ForegroundColor Gray

        if ($result.Resized) {
            Write-Host "    Dimensions: $($result.OriginalDimensions) → $($result.TargetDimensions)" -ForegroundColor Gray
        } else {
            Write-Host "    Dimensions: $($result.OriginalDimensions) (unchanged)" -ForegroundColor Gray
        }

        $totalOriginalSize += $result.OriginalSize
        $totalCompressedSize += $result.CompressedSize

        $results += @{
            File = $png.Name
            Success = $true
            OriginalSize = $result.OriginalSize
            CompressedSize = $result.CompressedSize
            Reduction = $reduction
        }

    } catch {
        Write-Error-Custom "Failed to compress: $($png.Name)"
        Write-Host "    Error: $($_.Exception.Message)" -ForegroundColor Red

        $results += @{
            File = $png.Name
            Success = $false
            Error = $_.Exception.Message
        }
    }

    Write-Host ""
}

# Display summary
Write-Host "========================================" -ForegroundColor Magenta
Write-Host " Compression Summary" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta

$successCount = ($results | Where-Object { $_.Success }).Count
$failCount = ($results | Where-Object { -not $_.Success }).Count

Write-Host "  Total files: $($pngFiles.Count)" -ForegroundColor White
Write-Host "  Successful: $successCount" -ForegroundColor Green
Write-Host "  Failed: $failCount" -ForegroundColor $(if ($failCount -gt 0) { 'Red' } else { 'Gray' })

if ($successCount -gt 0) {
    $totalReduction = [math]::Round((1 - $totalCompressedSize / $totalOriginalSize) * 100, 1)
    Write-Host ""
    Write-Host "  Total original size: $(Format-FileSize $totalOriginalSize)" -ForegroundColor White
    Write-Host "  Total compressed size: $(Format-FileSize $totalCompressedSize)" -ForegroundColor White
    Write-Host "  Total reduction: $totalReduction%" -ForegroundColor Cyan
}

Write-Host ""

if ($successCount -eq $pngFiles.Count) {
    Write-Success "All screenshots compressed successfully!"
} elseif ($successCount -gt 0) {
    Write-Warning "Some screenshots failed to compress"
} else {
    Write-Error-Custom "No screenshots were compressed"
    exit 1
}

Write-Host ""
