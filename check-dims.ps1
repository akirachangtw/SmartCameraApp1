Add-Type -AssemblyName System.Drawing
$img = [System.Drawing.Image]::FromFile("app\src\main\assets\test_images\portrait_good_composition.jpg")
Write-Host "$($img.Width)x$($img.Height)"
$img.Dispose()
