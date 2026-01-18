# v2.9.6 Phase 1: Orientation Test Automation Script (PowerShell)
# Usage: .\test_phase1_orientation.ps1 -DeviceId HQ616R2713
param(
    [string]$DeviceId = "HQ616R2713"
)

$TEST_DIR = "tmp\v2.9.6_phase1_testing"
$TIMESTAMP = Get-Date -Format "yyyyMMdd_HHmmss"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "v2.9.6 Phase 1 Orientation Test" -ForegroundColor Cyan
Write-Host "Device: $DeviceId" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 1. Create test directory
New-Item -ItemType Directory -Force -Path $TEST_DIR | Out-Null

# 2. Check if app is running
Write-Host ""
Write-Host "[1/6] Checking app status..." -ForegroundColor Yellow
$PID = (adb -s $DeviceId shell pidof com.akirachang.ai.photographer).Trim()
if ([string]::IsNullOrWhiteSpace($PID)) {
    Write-Host "⚠️  App not running. Starting app..." -ForegroundColor Yellow
    adb -s $DeviceId shell am start -n com.akirachang.ai.photographer/com.akirachang.ai.photographer.MainActivity
    Start-Sleep -Seconds 3
} else {
    Write-Host "✅ App running (PID: $PID)" -ForegroundColor Green
}

# 3. Clear logcat and start monitoring
Write-Host ""
Write-Host "[2/6] Starting logcat monitoring..." -ForegroundColor Yellow
adb -s $DeviceId logcat -c
$LogcatFile = "$TEST_DIR\logcat_$TIMESTAMP.txt"
$LogcatJob = Start-Job -ScriptBlock {
    param($DeviceId, $LogcatFile)
    adb -s $DeviceId logcat -s LiveCameraSource:* > $LogcatFile
} -ArgumentList $DeviceId, $LogcatFile

Write-Host "✅ Logcat Job ID: $($LogcatJob.Id)" -ForegroundColor Green

# 4. Wait for user to perform tests
Write-Host ""
Write-Host "[3/6] Please perform the following tests:" -ForegroundColor Yellow
Write-Host "  - TC-001: Back camera, Portrait (0°) - 3 photos" -ForegroundColor White
Write-Host "  - TC-002: Back camera, Landscape Left (90°) - 3 photos" -ForegroundColor White
Write-Host "  - TC-003: Back camera, Landscape Right (270°) - 3 photos" -ForegroundColor White
Write-Host "  - TC-004: Front camera, repeat above - 9 photos" -ForegroundColor White
Write-Host ""
Read-Host "Press ENTER when all tests are completed"

# 5. Stop logcat
Write-Host ""
Write-Host "[4/6] Stopping logcat..." -ForegroundColor Yellow
Stop-Job -Job $LogcatJob
Receive-Job -Job $LogcatJob | Out-Null
Remove-Job -Job $LogcatJob
Write-Host "✅ Logcat saved to: $LogcatFile" -ForegroundColor Green

# 6. Pull photos from device
Write-Host ""
Write-Host "[5/6] Pulling test photos from device..." -ForegroundColor Yellow
$DCIM_PATH = "/sdcard/DCIM/SmartCamera"

# Get list of most recent photos (last 20)
$photos = (adb -s $DeviceId shell "ls -t $DCIM_PATH/*.jpg 2>/dev/null | head -20").Split("`n")
foreach ($photo in $photos) {
    $photo = $photo.Trim()
    if (![string]::IsNullOrWhiteSpace($photo)) {
        $filename = Split-Path -Leaf $photo
        Write-Host "  Pulling: $filename" -ForegroundColor Gray
        adb -s $DeviceId pull $photo "$TEST_DIR\" 2>&1 | Out-Null
    }
}
Write-Host "✅ Photos saved to: $TEST_DIR\" -ForegroundColor Green

# 7. Analyze EXIF data (placeholder - requires exiftool or Python)
Write-Host ""
Write-Host "[6/6] Listing pulled photos..." -ForegroundColor Yellow
$ExifFile = "$TEST_DIR\exif_analysis_$TIMESTAMP.txt"
"Photo Name                     | Size" | Out-File -FilePath $ExifFile
"-------------------------------|-------------" | Out-File -FilePath $ExifFile -Append

Get-ChildItem -Path "$TEST_DIR\*.jpg" | ForEach-Object {
    $size = "{0:N0} KB" -f ($_.Length / 1KB)
    "$($_.Name.PadRight(30)) | $size" | Out-File -FilePath $ExifFile -Append
}

Get-Content $ExifFile | ForEach-Object { Write-Host $_ }

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "✅ Test data collection completed!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Results:"
Write-Host "  - Logcat: $LogcatFile"
Write-Host "  - Photos: $TEST_DIR\*.jpg"
Write-Host "  - List:   $ExifFile"
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  1. Open photos in Windows Photo Viewer to verify orientation"
Write-Host "  2. Check logcat for 'OrientationEventListener enabled'"
Write-Host "  3. Check logcat for 'Orientation changed' messages"
Write-Host "  4. Verify FPS maintained 25-30 (no regression)"
