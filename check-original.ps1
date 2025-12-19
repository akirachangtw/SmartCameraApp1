Add-Type -AssemblyName System.Drawing

Write-Host "=== Original Source Images ===" -ForegroundColor Cyan
Write-Host ""

$img1 = [System.Drawing.Image]::FromFile("others\Testing-IMAGES\portrait_good_composition.JPG")
Write-Host "portrait_good_composition.JPG: $($img1.Width)x$($img1.Height)"
$img1.Dispose()

$img2 = [System.Drawing.Image]::FromFile("others\Testing-IMAGES\portrait_bad_headroom.JPG")
Write-Host "portrait_bad_headroom.JPG: $($img2.Width)x$($img2.Height)"
$img2.Dispose()

Write-Host ""
Write-Host "Note: These may have Exif rotation metadata causing Android to display them rotated"
