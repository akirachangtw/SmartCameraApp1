# wait-for-emulator.ps1 - 等待模擬器完全啟動

Write-Host "⏳ 等待模擬器完全啟動..." -ForegroundColor Cyan
Write-Host "   (提示：模擬器視窗應該從黑色畫面逐漸顯示 Android 開機動畫)`n" -ForegroundColor Gray

$timeout = 180  # 3 分鐘
$elapsed = 0
$checkInterval = 5

while ($elapsed -lt $timeout) {
    $devices = adb devices 2>&1
    $emulatorLine = $devices | Select-String "emulator.*device$"

    if ($emulatorLine -and $emulatorLine -notmatch "offline") {
        Write-Host "`n✅ 模擬器已完全啟動！" -ForegroundColor Green
        Write-Host "`n📱 連接的裝置:" -ForegroundColor Cyan
        adb devices

        # 檢查 boot completed
        $bootStatus = adb shell getprop sys.boot_completed 2>&1
        if ($bootStatus -eq "1") {
            Write-Host "`n🎉 系統啟動完成！可以安裝應用了。" -ForegroundColor Green
        }

        exit 0
    }

    $elapsed += $checkInterval
    $remaining = $timeout - $elapsed
    Write-Host ("[{0}] 等待中... ({1}/{2} 秒，剩餘 {3} 秒)" -f (Get-Date -Format "HH:mm:ss"), $elapsed, $timeout, $remaining) -ForegroundColor Yellow

    # 顯示當前狀態
    $status = $devices | Select-String "emulator"
    if ($status) {
        Write-Host "   當前狀態: $status" -ForegroundColor Gray
    }

    Start-Sleep -Seconds $checkInterval
}

Write-Host "`n⚠️ 超時（${timeout}秒）- 模擬器可能需要更長時間啟動" -ForegroundColor Yellow
Write-Host "`n請檢查:" -ForegroundColor White
Write-Host "  1. 模擬器視窗是否顯示 Android 畫面？" -ForegroundColor Gray
Write-Host "  2. 視窗是否有錯誤訊息？" -ForegroundColor Gray
Write-Host "  3. 電腦效能是否足夠（建議 8GB+ RAM）？" -ForegroundColor Gray
Write-Host "`n當前狀態:" -ForegroundColor Cyan
adb devices
