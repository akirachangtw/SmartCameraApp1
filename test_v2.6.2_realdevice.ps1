# v2.6.2 實機驗證腳本
# 目的: 驗證 LiveCameraSource HDR 控制鏈路
# 預期: 真實 exposure compensation 控制生效

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "v2.6.2 實機驗證 - Phase 2" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 檢查實機是否連接
Write-Host "[Step 1/5] 檢查實機連接狀態..." -ForegroundColor Yellow
$deviceConnected = adb devices | Select-String "HQ616R2713"

if (-not $deviceConnected) {
    Write-Host "❌ 實機未連接 (HQ616R2713)" -ForegroundColor Red
    Write-Host ""
    Write-Host "已連接的裝置:" -ForegroundColor Yellow
    adb devices
    Write-Host ""
    Write-Host "按任意鍵退出..."
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit 1
}

Write-Host "✅ 實機已連接 (Sony XQ-BT52)" -ForegroundColor Green
Write-Host ""

# 編譯 APK
Write-Host "[Step 2/5] 編譯 deviceDebug APK..." -ForegroundColor Yellow
.\gradlew.bat assembleDeviceDebug

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build 失敗" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Build 成功" -ForegroundColor Green
Write-Host ""

# 安裝 APK
Write-Host "[Step 3/5] 安裝 APK 到實機..." -ForegroundColor Yellow
adb -s HQ616R2713 install -r app\build\outputs\apk\device\debug\app-device-debug.apk

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 安裝失敗" -ForegroundColor Red
    exit 1
}

Write-Host "✅ 安裝成功" -ForegroundColor Green
Write-Host ""

# 清除並啟動 logcat 監控
Write-Host "[Step 4/5] 啟動 logcat 監控..." -ForegroundColor Yellow
adb -s HQ616R2713 logcat -c

$logFileName = "realdevice_v2.6.2_test_$(Get-Date -Format 'yyyyMMdd_HHmmss').txt"

Write-Host "📝 Logcat 輸出到: $logFileName" -ForegroundColor Cyan
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "請執行以下測試步驟:" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "TC2: 手動 HDR ON" -ForegroundColor Cyan
Write-Host "  1. 開啟 SmartCameraApp" -ForegroundColor White
Write-Host "  2. 等待相機 Preview 穩定 (2 秒)" -ForegroundColor White
Write-Host "  3. 點擊 HDR 按鈕 → ON" -ForegroundColor Yellow
Write-Host "  4. 等待 2 秒" -ForegroundColor White
Write-Host ""
Write-Host "TC3: 手動 HDR OFF" -ForegroundColor Cyan
Write-Host "  5. 點擊 HDR 按鈕 → OFF" -ForegroundColor Yellow
Write-Host "  6. 等待 2 秒" -ForegroundColor White
Write-Host ""
Write-Host "TC4: Rebind 重套用測試" -ForegroundColor Cyan
Write-Host "  7. 點擊 HDR 按鈕 → ON" -ForegroundColor Yellow
Write-Host "  8. 點擊切換鏡頭按鈕 (前鏡頭)" -ForegroundColor Yellow
Write-Host "  9. 等待 2 秒" -ForegroundColor White
Write-Host " 10. 點擊切換鏡頭按鈕 (後鏡頭)" -ForegroundColor Yellow
Write-Host " 11. 等待 2 秒" -ForegroundColor White
Write-Host ""
Write-Host "TC5: AUTO 模式" -ForegroundColor Cyan
Write-Host " 12. 點擊 HDR 按鈕 → AUTO" -ForegroundColor Yellow
Write-Host " 13. 等待 AI 自動觸發 (對準高對比場景)" -ForegroundColor White
Write-Host ""
Write-Host " 14. 按 Ctrl+C 停止監控" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

Write-Host "[Step 5/5] 開始 logcat 監控..." -ForegroundColor Yellow
Write-Host "按 Ctrl+C 停止監控" -ForegroundColor Cyan
Write-Host ""

# 啟動 logcat 監控
adb -s HQ616R2713 logcat |
    Select-String "(HDR|exposure|CameraControl|CameraRepository|LiveCameraSource|Pending)" |
    Tee-Object -FilePath $logFileName

# 監控結束後分析
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "分析結果" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 檢查驗證項目
$content = Get-Content $logFileName -Raw

$hasCameraControlError = $content -match "CameraControl not available"
$hasHDREnabled = $content -match "HDR enabled: exposure compensation set to"
$hasHDRDisabled = $content -match "HDR disabled: exposure compensation reset to 0"
$hasPendingReapplied = $content -match "Pending HDR exposure compensation reapplied"
$hasDelegating = $content -match "Delegating HDR"

Write-Host ""
Write-Host "檢查項目:" -ForegroundColor Yellow
Write-Host ""

# TC2: 手動 HDR ON
if ($hasHDREnabled) {
    Write-Host "✅ TC2 通過: HDR enabled 日誌正常" -ForegroundColor Green

    # 提取 range 資訊
    if ($content -match "range: ([-\d]+)\.\.([-\d]+)") {
        $rangeLower = $Matches[1]
        $rangeUpper = $Matches[2]
        Write-Host "   曝光補償範圍: $rangeLower..$rangeUpper" -ForegroundColor Cyan
    }
} else {
    Write-Host "❌ TC2 失敗: 未發現 'HDR enabled' 日誌" -ForegroundColor Red
}

# TC3: 手動 HDR OFF
if ($hasHDRDisabled) {
    Write-Host "✅ TC3 通過: HDR disabled 日誌正常" -ForegroundColor Green
} else {
    Write-Host "❌ TC3 失敗: 未發現 'HDR disabled' 日誌" -ForegroundColor Red
}

# TC4: Rebind 重套用
if ($hasPendingReapplied) {
    Write-Host "✅ TC4 通過: Pending HDR 重套用成功" -ForegroundColor Green
} else {
    Write-Host "⚠️  TC4 未驗證: 未發現 'Pending...reapplied' 日誌" -ForegroundColor Yellow
    Write-Host "   (可能未執行切換鏡頭操作)" -ForegroundColor Gray
}

# Repository 委派
if ($hasDelegating) {
    Write-Host "✅ Repository 委派: 正常" -ForegroundColor Green
} else {
    Write-Host "⚠️  Repository 委派: 未發現相關日誌" -ForegroundColor Yellow
}

# 關鍵: 無錯誤警告
Write-Host ""
if ($hasCameraControlError) {
    Write-Host "❌ 嚴重: 發現 'CameraControl not available' 警告" -ForegroundColor Red
    Write-Host "   修復失敗,需要重新檢查實作" -ForegroundColor Red
} else {
    Write-Host "✅ 關鍵: 無 'CameraControl not available' 警告" -ForegroundColor Green
}

Write-Host ""
Write-Host "完整日誌: $logFileName" -ForegroundColor Cyan
Write-Host ""

# 總結
$tc2Pass = $hasHDREnabled
$tc3Pass = $hasHDRDisabled
$noError = -not $hasCameraControlError

if ($tc2Pass -and $tc3Pass -and $noError) {
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "✅ Phase 2 驗證通過!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "核心功能驗證:" -ForegroundColor Yellow
    Write-Host "  ✅ TC2: 手動 HDR ON 成功" -ForegroundColor Green
    Write-Host "  ✅ TC3: 手動 HDR OFF 成功" -ForegroundColor Green
    if ($hasPendingReapplied) {
        Write-Host "  ✅ TC4: Rebind 重套用成功" -ForegroundColor Green
    }
    Write-Host "  ✅ 無 CameraControl 錯誤" -ForegroundColor Green
    Write-Host ""
    Write-Host "下一步: 生成 PR Notes" -ForegroundColor Yellow
    Write-Host "  告知 Claude: '生成 PR Notes,包含驗證結果'" -ForegroundColor White
} elseif ($noError -and ($hasHDREnabled -or $hasHDRDisabled)) {
    Write-Host "========================================" -ForegroundColor Yellow
    Write-Host "⚠️  Phase 2 部分通過" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "建議: 檢查是否完整執行所有測試步驟" -ForegroundColor Cyan
    Write-Host "完整日誌: $logFileName" -ForegroundColor White
} else {
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "❌ Phase 2 驗證失敗" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "請檢查日誌並回報問題" -ForegroundColor Yellow
    Write-Host "日誌檔案: $logFileName" -ForegroundColor White
}

Write-Host ""
