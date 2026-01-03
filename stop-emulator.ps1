# stop-emulator.ps1 - 關閉 Android 模擬器

Write-Host "🔴 關閉 Android 模擬器..." -ForegroundColor Red

# 檢查是否有運行的模擬器
$devices = adb devices 2>&1 | Select-String "emulator"

if (-not $devices) {
    Write-Host "✅ 沒有運行中的模擬器" -ForegroundColor Green
    exit 0
}

Write-Host "📱 偵測到以下模擬器:" -ForegroundColor Cyan
adb devices

# 關閉所有模擬器
$emulators = adb devices | Select-String "emulator-\d+" | ForEach-Object { $_.Matches.Value }

foreach ($emulator in $emulators) {
    Write-Host "`n🔴 關閉模擬器: $emulator" -ForegroundColor Yellow
    adb -s $emulator emu kill 2>&1 | Out-Null

    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ 已發送關閉命令到 $emulator" -ForegroundColor Green
    } else {
        Write-Host "⚠️  無法關閉 $emulator，嘗試強制終止..." -ForegroundColor Yellow

        # 找到並終止 emulator 程序
        $processes = Get-Process emulator* -ErrorAction SilentlyContinue
        if ($processes) {
            $processes | Stop-Process -Force
            Write-Host "✅ 已強制終止模擬器程序" -ForegroundColor Green
        }
    }
}

# 等待確認
Start-Sleep -Seconds 2

Write-Host "`n🔍 檢查狀態..." -ForegroundColor Cyan
$remainingDevices = adb devices 2>&1 | Select-String "emulator"

if (-not $remainingDevices) {
    Write-Host "✅ 所有模擬器已關閉" -ForegroundColor Green
} else {
    Write-Host "⚠️  仍有模擬器運行中:" -ForegroundColor Yellow
    adb devices
}
