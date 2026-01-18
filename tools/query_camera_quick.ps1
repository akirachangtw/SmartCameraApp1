# query_camera_quick.ps1
# 快速查詢連接裝置的相機解析度資訊
# 使用 SmartCameraApp v2.9.0-beta5 的診斷功能

param(
    [switch]$SkipInstall = $false
)

$ErrorActionPreference = "Stop"

Write-Host "======================================" -ForegroundColor Blue
Write-Host "  Camera Resolution Query Tool" -ForegroundColor Blue
Write-Host "  Using SmartCameraApp v2.9.0-beta5" -ForegroundColor Blue
Write-Host "======================================" -ForegroundColor Blue
Write-Host ""

# 1. 檢查 adb
Write-Host "[1/8] Checking adb..." -ForegroundColor Yellow
try {
    $null = & adb version 2>&1
} catch {
    Write-Host "Error: adb not found" -ForegroundColor Red
    Write-Host "Please install Android SDK Platform Tools" -ForegroundColor Red
    exit 1
}
Write-Host "      adb found" -ForegroundColor Green

# 2. 檢查裝置
Write-Host "[2/8] Checking connected devices..." -ForegroundColor Yellow
$devices = & adb devices | Select-String "device$" | Where-Object { $_ -notmatch "List of devices" }
if ($devices.Count -eq 0) {
    Write-Host "Error: No devices connected" -ForegroundColor Red
    Write-Host "Please connect a device and enable USB debugging" -ForegroundColor Red
    exit 1
} elseif ($devices.Count -gt 1) {
    Write-Host "Warning: Multiple devices connected" -ForegroundColor Yellow
    & adb devices
    Write-Host "Please specify device with: `$env:ANDROID_SERIAL=<device_id>" -ForegroundColor Yellow
    exit 1
}
Write-Host "      Device connected" -ForegroundColor Green

# 3. 獲取裝置資訊
Write-Host "[3/8] Getting device info..." -ForegroundColor Yellow
$manufacturer = (& adb shell getprop ro.product.manufacturer).Trim()
$model = (& adb shell getprop ro.product.model).Trim()
$androidVersion = (& adb shell getprop ro.build.version.release).Trim()
$apiLevel = (& adb shell getprop ro.build.version.sdk).Trim()

Write-Host "      Manufacturer: $manufacturer" -ForegroundColor Cyan
Write-Host "      Model: $model" -ForegroundColor Cyan
Write-Host "      Android: $androidVersion (API $apiLevel)" -ForegroundColor Cyan

# 4. 安裝 app（如果需要）
if (-not $SkipInstall) {
    Write-Host "[4/8] Installing SmartCameraApp..." -ForegroundColor Yellow
    $apkPath = "app\build\outputs\apk\device\debug\app-device-debug.apk"

    if (-not (Test-Path $apkPath)) {
        Write-Host "      APK not found, building..." -ForegroundColor Yellow
        & .\gradlew assembleDeviceDebug
    }

    & adb install -r $apkPath 2>&1 | Out-Null
    Write-Host "      App installed" -ForegroundColor Green
} else {
    Write-Host "[4/8] Skipping installation (--SkipInstall)" -ForegroundColor Yellow
}

# 5. 授予權限
Write-Host "[5/8] Granting camera permission..." -ForegroundColor Yellow
& adb shell pm grant com.akirachang.ai.photographer android.permission.CAMERA 2>&1 | Out-Null
Write-Host "      Permission granted" -ForegroundColor Green

# 6. 清除 logcat 並啟動
Write-Host "[6/8] Launching app and capturing logs..." -ForegroundColor Yellow
& adb logcat -c
& adb shell am start -n com.akirachang.ai.photographer/.MainActivity 2>&1 | Out-Null
Write-Host "      App launched, waiting 10 seconds..." -ForegroundColor Cyan

Start-Sleep -Seconds 10

# 7. 擷取日誌
Write-Host "[7/8] Extracting camera info..." -ForegroundColor Yellow
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$outputDir = "tmp\device_info"
$outputFile = "$outputDir\${manufacturer}_${model}_${timestamp}.log"

New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

$logContent = & adb logcat -d
$betaLogs = $logContent | Select-String "\[v2.9.0-beta"

if ($betaLogs.Count -eq 0) {
    Write-Host "      Warning: No camera info found in logs" -ForegroundColor Yellow
    Write-Host "      This may indicate the app didn't start properly" -ForegroundColor Yellow
} else {
    $betaLogs | Out-File -FilePath $outputFile -Encoding UTF8
    Write-Host "      Logs captured" -ForegroundColor Green
}

# 8. 顯示結果
Write-Host "[8/8] Results:" -ForegroundColor Yellow
Write-Host ""
Write-Host "======================================" -ForegroundColor Blue
Write-Host "  Camera Information" -ForegroundColor Blue
Write-Host "======================================" -ForegroundColor Blue
Write-Host ""

# 提取關鍵資訊
$deviceInfo = $betaLogs | Select-String "Device:" | Select-Object -First 1
$acceptable = $betaLogs | Select-String "Acceptable:" | Select-Object -First 1
$strategy = $betaLogs | Select-String "(Pixel:|Sony:|Generic:)" | Select-Object -First 1
$smartRes = $betaLogs | Select-String "Smart Resolution:" | Select-Object -First 1
$filterResult = $betaLogs | Select-String "Filter\] Allowed:" | Select-Object -First 1
$supportedLine = $betaLogs | Select-String "Supported Resolutions\].*total" | Select-Object -First 1
$resolutionsList = $betaLogs | Select-String "v2.9.0-beta3\].*x.*," | Select-Object -First 1

if ($deviceInfo) {
    Write-Host "Device Detection:" -ForegroundColor Green
    Write-Host "  $($deviceInfo.Line.Split(']:')[1].Trim())" -ForegroundColor White
    Write-Host ""
}

if ($supportedLine) {
    Write-Host "Supported Resolutions:" -ForegroundColor Green
    Write-Host "  $($supportedLine.Line.Split(']:')[1].Trim())" -ForegroundColor White
    Write-Host ""
}

if ($resolutionsList) {
    Write-Host "Resolution List:" -ForegroundColor Green
    $resList = $resolutionsList.Line.Split(']:')[1].Trim()
    # 格式化顯示（每行 10 個）
    $resArray = $resList -split ', '
    for ($i = 0; $i -lt $resArray.Count; $i += 10) {
        $line = $resArray[$i..([Math]::Min($i+9, $resArray.Count-1))] -join ', '
        Write-Host "  $line" -ForegroundColor White
    }
    Write-Host ""
}

if ($acceptable) {
    Write-Host "Acceptable Resolutions (480x360-1280x720):" -ForegroundColor Green
    Write-Host "  $($acceptable.Line.Split(']:')[1].Trim())" -ForegroundColor White
    Write-Host ""
}

if ($strategy) {
    Write-Host "Strategy Applied:" -ForegroundColor Green
    Write-Host "  $($strategy.Line.Split(']:')[1].Trim())" -ForegroundColor White
    Write-Host ""
}

if ($smartRes) {
    Write-Host "Final Selection:" -ForegroundColor Green
    Write-Host "  $($smartRes.Line.Split(']:')[1].Trim())" -ForegroundColor White
    Write-Host ""
}

if ($filterResult) {
    Write-Host "Filtered Resolutions:" -ForegroundColor Green
    Write-Host "  $($filterResult.Line.Split(']:')[1].Trim())" -ForegroundColor White
    Write-Host ""
}

Write-Host "======================================" -ForegroundColor Blue
Write-Host ""
Write-Host "Full log saved to:" -ForegroundColor Cyan
Write-Host "  $outputFile" -ForegroundColor White
Write-Host ""

# 生成摘要
Write-Host "Summary:" -ForegroundColor Green
Write-Host "  Device: $manufacturer $model" -ForegroundColor White
Write-Host "  Android: $androidVersion (API $apiLevel)" -ForegroundColor White

if ($supportedLine) {
    $totalCount = [regex]::Match($supportedLine.Line, '\((\d+) total\)').Groups[1].Value
    Write-Host "  Supported Resolutions: $totalCount" -ForegroundColor White
}

if ($strategy) {
    $strategyName = if ($strategy.Line -match "Pixel:") { "Pixel" } elseif ($strategy.Line -match "Sony:") { "Sony" } else { "Generic" }
    Write-Host "  Strategy: $strategyName" -ForegroundColor White
}

if ($smartRes) {
    $finalRes = [regex]::Match($smartRes.Line, 'target=(\d+x\d+)').Groups[1].Value
    Write-Host "  Selected Resolution: $finalRes" -ForegroundColor White
}

Write-Host ""
Write-Host "Done!" -ForegroundColor Green
