# Optimize test images for mobile deployment
# Resizes portrait images to 1080x1920, landscape to 1920x1080

Add-Type -AssemblyName System.Drawing

$sourceDir = "others\Testing-IMAGES"
$targetDir = "app\src\main\assets\test_images"

# Create target directory if it doesn't exist
if (!(Test-Path $targetDir)) {
    New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
}

Write-Host "🖼️  Optimizing test images..." -ForegroundColor Cyan
Write-Host ""

# Process each image
Get-ChildItem -Path $sourceDir -Filter *.* | ForEach-Object {
    $sourceFile = $_.FullName
    $fileName = $_.Name.ToLower()  # Convert to lowercase for consistency
    $targetFile = Join-Path $targetDir $fileName

    Write-Host "Processing: $($_.Name)" -ForegroundColor Yellow

    try {
        # Load image
        $img = [System.Drawing.Image]::FromFile($sourceFile)
        $originalWidth = $img.Width
        $originalHeight = $img.Height

        Write-Host "  Original: ${originalWidth}x${originalHeight}" -ForegroundColor Gray

        # Determine target dimensions based on orientation
        # Calculate aspect ratio
        $aspectRatio = $originalWidth / $originalHeight

        if ($originalHeight > $originalWidth) {
            # Portrait - maintain aspect ratio
            # Use 1080 as max width
            $targetWidth = 1080
            $targetHeight = [int]($targetWidth / $aspectRatio)

            # If height exceeds typical phone screen, cap it
            if ($targetHeight > 1920) {
                $targetHeight = 1920
                $targetWidth = [int]($targetHeight * $aspectRatio)
            }
        } else {
            # Landscape - maintain aspect ratio
            # Use 1920 as max width
            $targetWidth = 1920
            $targetHeight = [int]($targetWidth / $aspectRatio)

            # If height exceeds 1080, cap it
            if ($targetHeight > 1080) {
                $targetHeight = 1080
                $targetWidth = [int]($targetHeight * $aspectRatio)
            }
        }

        # Create new bitmap with target dimensions
        $resized = New-Object System.Drawing.Bitmap($targetWidth, $targetHeight)
        $graphics = [System.Drawing.Graphics]::FromImage($resized)

        # Set high-quality rendering
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
        $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality

        # Draw resized image
        $graphics.DrawImage($img, 0, 0, $targetWidth, $targetHeight)

        # Save as JPEG with quality 85
        $jpegEncoder = [System.Drawing.Imaging.ImageCodecInfo]::GetImageEncoders() | Where-Object { $_.MimeType -eq 'image/jpeg' }
        $encoderParams = New-Object System.Drawing.Imaging.EncoderParameters(1)
        $encoderParams.Param[0] = New-Object System.Drawing.Imaging.EncoderParameter([System.Drawing.Imaging.Encoder]::Quality, 85)

        $resized.Save($targetFile, $jpegEncoder, $encoderParams)

        # Cleanup
        $graphics.Dispose()
        $resized.Dispose()
        $img.Dispose()

        $targetSize = (Get-Item $targetFile).Length
        $targetSizeKB = [math]::Round($targetSize / 1KB, 1)

        Write-Host "  Optimized: ${targetWidth}x${targetHeight} (${targetSizeKB} KB)" -ForegroundColor Green
        Write-Host ""

    } catch {
        Write-Host "  ❌ Error: $_" -ForegroundColor Red
        Write-Host ""
    }
}

Write-Host "✅ Image optimization complete!" -ForegroundColor Green
Write-Host ""
Write-Host "📁 Optimized images saved to: $targetDir" -ForegroundColor Cyan
