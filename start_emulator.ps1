# 啟動 Android 模擬器腳本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "啟動 Android 模擬器" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 設定模擬器路徑
$emulatorPath = "C:\Users\Administrator\AppData\Local\Android\Sdk\emulator\emulator.exe"

# 檢查模擬器是否存在
if (-not (Test-Path $emulatorPath)) {
    Write-Host "❌ 找不到模擬器: $emulatorPath" -ForegroundColor Red
    Write-Host ""
    Write-Host "請檢查 Android SDK 安裝路徑" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ 找到模擬器: $emulatorPath" -ForegroundColor Green
Write-Host ""

# 列出可用的 AVD
Write-Host "可用的模擬器列表:" -ForegroundColor Yellow
$avds = & $emulatorPath -list-avds

if ($avds.Count -eq 0) {
    Write-Host "❌ 沒有可用的模擬器" -ForegroundColor Red
    Write-Host ""
    Write-Host "請先在 Android Studio 建立 AVD" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
$avds | ForEach-Object { Write-Host "  - $_" -ForegroundColor White }
Write-Host ""

# 選擇模擬器
$targetAVD = "Medium_Phone_API_36.1"

if ($avds -contains $targetAVD) {
    Write-Host "準備啟動: $targetAVD" -ForegroundColor Green
} else {
    Write-Host "⚠️  找不到 $targetAVD,使用第一個 AVD: $($avds[0])" -ForegroundColor Yellow
    $targetAVD = $avds[0]
}

Write-Host ""
Write-Host "啟動模擬器: $targetAVD" -ForegroundColor Cyan
Write-Host "請稍候,模擬器啟動需要 30-60 秒..." -ForegroundColor Yellow
Write-Host ""

# 啟動模擬器 (在新視窗)
Start-Process -FilePath $emulatorPath -ArgumentList "-avd", $targetAVD, "-gpu", "host" -WindowStyle Normal

Write-Host "模擬器啟動中..." -ForegroundColor Green
Write-Host ""
Write-Host "等待模擬器完全啟動後,請執行:" -ForegroundColor Yellow
Write-Host "  .\test_v2.6.2_emulator.ps1" -ForegroundColor White
Write-Host ""
