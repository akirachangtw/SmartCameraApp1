Add-Type -AssemblyName System.Drawing

Write-Host "=== Current assets/test_images/ ===" -ForegroundColor Cyan
Write-Host ""

Get-ChildItem "app\src\main\assets\test_images" | ForEach-Object {
    $img = [System.Drawing.Image]::FromFile($_.FullName)
    $sizeKB = [math]::Round($_.Length / 1KB, 1)
    Write-Host "$($_.Name): $($img.Width)x$($img.Height) ($sizeKB KB)"
    $img.Dispose()
}
