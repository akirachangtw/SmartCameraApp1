# v2.6.2 模擬器驗證腳本
# 目的: 驗證 Camera Input Abstraction 修復
# 預期: 無 "CameraControl not available" 警告

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "v2.6.2 模擬器驗證 - Phase 1" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 檢查模擬器是否運行
Write-Host "[Step 1/5] 檢查模擬器狀態..." -ForegroundColor Yellow
$emulatorRunning = adb devices | Select-String "emulator"

if (-not $emulatorRunning) {
    Write-Host "❌ 模擬器未運行" -ForegroundColor Red
    Write-Host ""
    Write-Host "請先啟動模擬器:" -ForegroundColor Yellow
    Write-Host '  $env:ANDROID_HOME\emulator\emulator -avd Medium_Phone_API_36.1 -gpu host' -ForegroundColor White
    Write-Host ""
    Write-Host "按任意鍵退出..."
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit 1
}

Write-Host "✅ 模擬器運行中" -ForegroundColor Green
Write-Host ""

# 編譯 APK
Write-Host "[Step 2/5] 編譯 emulatorDebug APK..." -ForegroundColor Yellow
.\gradlew.bat assembleEmulatorDebug

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build 失敗" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Build 成功" -ForegroundColor Green
Write-Host ""

# 安裝 APK
Write-Host "[Step 3/5] 安裝 APK 到模擬器..." -ForegroundColor Yellow
adb install -r app\build\outputs\apk\emulator\debug\app-emulator-debug.apk

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 安裝失敗" -ForegroundColor Red
    exit 1
}

Write-Host "✅ 安裝成功" -ForegroundColor Green
Write-Host ""

# 清除並啟動 logcat 監控
Write-Host "[Step 4/5] 啟動 logcat 監控..." -ForegroundColor Yellow
adb logcat -c

$logFileName = "emulator_v2.6.2_test_$(Get-Date -Format 'yyyyMMdd_HHmmss').txt"

Write-Host "📝 Logcat 輸出到: $logFileName" -ForegroundColor Cyan
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "請執行以下測試步驟:" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "1. 開啟 SmartCameraApp" -ForegroundColor White
Write-Host "2. 等待畫面穩定 (2 秒)" -ForegroundColor White
Write-Host "3. 點擊 HDR 按鈕 → ON" -ForegroundColor Yellow
Write-Host "4. 等待 2 秒" -ForegroundColor White
Write-Host "5. 點擊 HDR 按鈕 → OFF" -ForegroundColor Yellow
Write-Host "6. 等待 2 秒" -ForegroundColor White
Write-Host "7. 點擊 HDR 按鈕 → AUTO" -ForegroundColor Yellow
Write-Host "8. 等待 AI 自動觸發 (切換測試圖片)" -ForegroundColor White
Write-Host "9. 按 Ctrl+C 停止監控" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

Write-Host "[Step 5/5] 開始 logcat 監控..." -ForegroundColor Yellow
Write-Host "按 Ctrl+C 停止監控" -ForegroundColor Cyan
Write-Host ""

# 啟動 logcat 監控
adb logcat |
    Select-String "(HDR|CameraControl|CameraRepository|StaticImageSource|enableHDR|disableHDR)" |
    Tee-Object -FilePath $logFileName

# 監控結束後分析
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "分析結果" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 檢查是否有錯誤
$content = Get-Content $logFileName -Raw

$hasCameraControlError = $content -match "CameraControl not available"
$hasNoOp = $content -match "no-op for static images"
$hasDelegating = $content -match "Delegating HDR"

Write-Host ""
Write-Host "檢查項目:" -ForegroundColor Yellow
Write-Host ""

if ($hasCameraControlError) {
    Write-Host "❌ 發現 'CameraControl not available' 警告 (修復失敗)" -ForegroundColor Red
} else {
    Write-Host "✅ 無 'CameraControl not available' 警告 (修復成功)" -ForegroundColor Green
}

if ($hasNoOp) {
    Write-Host "✅ 發現 'no-op for static images' (StaticImageSource 正常)" -ForegroundColor Green
} else {
    Write-Host "⚠️  未發現 'no-op for static images' (可能未點擊 HDR 按鈕)" -ForegroundColor Yellow
}

if ($hasDelegating) {
    Write-Host "✅ 發現 'Delegating HDR' (Repository 委派正常)" -ForegroundColor Green
} else {
    Write-Host "⚠️  未發現 'Delegating HDR' (可能未點擊 HDR 按鈕)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "完整日誌: $logFileName" -ForegroundColor Cyan
Write-Host ""

# 總結
if (-not $hasCameraControlError -and $hasNoOp -and $hasDelegating) {
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "✅ Phase 1 驗證通過!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "下一步: 執行 Phase 2 實機驗證" -ForegroundColor Yellow
    Write-Host "  .\test_v2.6.2_realdevice.ps1" -ForegroundColor White
} else {
    Write-Host "========================================" -ForegroundColor Yellow
    Write-Host "⚠️  Phase 1 驗證需要檢查" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "請檢查日誌: $logFileName" -ForegroundColor Cyan
}

Write-Host ""
