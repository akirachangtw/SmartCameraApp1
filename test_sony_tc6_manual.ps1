# Sony XQ-BT52 TC6 手動 HDR 測試腳本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Sony XQ-BT52 手動 HDR 測試 (TC6)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 設定裝置序號
$device = "HQ616R2713"

Write-Host "📱 檢查 Sony XQ-BT52 連接狀態..." -ForegroundColor Yellow
$connected = adb devices | Select-String $device

if (-not $connected) {
    Write-Host "❌ 找不到 Sony XQ-BT52 ($device)" -ForegroundColor Red
    Write-Host ""
    Write-Host "請確認:" -ForegroundColor Yellow
    Write-Host "  1. USB 已連接" -ForegroundColor White
    Write-Host "  2. USB 偵錯已啟用" -ForegroundColor White
    Write-Host "  3. 已授權此電腦" -ForegroundColor White
    Write-Host ""
    exit 1
}

Write-Host "✅ Sony XQ-BT52 已連接" -ForegroundColor Green
Write-Host ""

# 取得裝置資訊
$androidVersion = adb -s $device shell getprop ro.build.version.release
$apiLevel = adb -s $device shell getprop ro.build.version.sdk
$model = adb -s $device shell getprop ro.product.model

Write-Host "📱 裝置資訊:" -ForegroundColor Cyan
Write-Host "  型號: $model" -ForegroundColor White
Write-Host "  Android 版本: $androidVersion" -ForegroundColor White
Write-Host "  API Level: $apiLevel" -ForegroundColor White
Write-Host ""

# 檢查 APP 狀態
Write-Host "📱 檢查 APP 狀態..." -ForegroundColor Yellow
$pid = adb -s $device shell pidof com.akirachang.ai.photographer

if (-not $pid) {
    Write-Host "⚠️  APP 未運行,正在啟動..." -ForegroundColor Yellow
    adb -s $device shell am start -n com.akirachang.ai.photographer/com.akirachang.ai.photographer.MainActivity
    Start-Sleep -Seconds 3
    $pid = adb -s $device shell pidof com.akirachang.ai.photographer
}

if ($pid) {
    Write-Host "✅ APP 正在運行 (PID: $pid)" -ForegroundColor Green
} else {
    Write-Host "❌ 無法啟動 APP" -ForegroundColor Red
    Write-Host ""
    Write-Host "請手動啟動 SmartCameraApp 並重新執行此腳本" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# 設定日誌檔案
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logFile = "$PSScriptRoot\sony_tc6_manual_$timestamp.txt"

Write-Host "📊 日誌檔案: $logFile" -ForegroundColor Cyan
Write-Host ""

# 清除 logcat buffer
Write-Host "🧹 清除 logcat buffer..." -ForegroundColor Yellow
adb -s $device logcat -c
Start-Sleep -Seconds 1
Write-Host "✅ Buffer 已清除" -ForegroundColor Green
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "⚠️  準備執行 TC6 手動測試" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "請在 Sony 手機螢幕上執行以下操作:" -ForegroundColor White
Write-Host ""
Write-Host "  📱 步驟 1: 確認 APP 已開啟相機畫面" -ForegroundColor Cyan
Write-Host "  📱 步驟 2: 找到 HDR 控制按鈕" -ForegroundColor Cyan
Write-Host ""
Write-Host "  然後執行以下操作序列 (重複 2-3 次):" -ForegroundColor Yellow
Write-Host ""
Write-Host "  1️⃣  點擊 HDR 按鈕 → ON (開啟)" -ForegroundColor Green
Write-Host "  ⏱️   等待 3 秒" -ForegroundColor Gray
Write-Host "  2️⃣  點擊 HDR 按鈕 → OFF (關閉)" -ForegroundColor Red
Write-Host "  ⏱️   等待 3 秒" -ForegroundColor Gray
Write-Host "  3️⃣  點擊 HDR 按鈕 → AUTO (自動)" -ForegroundColor Yellow
Write-Host "  ⏱️   等待 3 秒" -ForegroundColor Gray
Write-Host ""
Write-Host "  ↻  重複以上步驟 2-3 次" -ForegroundColor Cyan
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "按 Enter 鍵開始收集 logcat..." -ForegroundColor Yellow
Read-Host

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "📡 Logcat 收集中 (串流模式)..." -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "⚠️  請現在在手機上執行 TC6 測試操作" -ForegroundColor Yellow
Write-Host ""
Write-Host "完成後按 Ctrl+C 停止收集" -ForegroundColor Cyan
Write-Host ""

# 串流收集 logcat
try {
    adb -s $device logcat -v threadtime --pid=$pid | Tee-Object -FilePath $logFile
} catch {
    Write-Host ""
    Write-Host "✅ 收集已停止" -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🔍 分析收集到的日誌" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 檢查檔案大小
if (-not (Test-Path $logFile)) {
    Write-Host "❌ 日誌檔案不存在" -ForegroundColor Red
    exit 1
}

$fileSize = (Get-Item $logFile).Length
Write-Host "📦 日誌檔案大小: $fileSize bytes" -ForegroundColor White
Write-Host ""

if ($fileSize -lt 100) {
    Write-Host "⚠️  日誌檔案過小 ($fileSize bytes),可能收集失敗" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "可能原因:" -ForegroundColor Yellow
    Write-Host "  1. APP PID 變更 (APP 重啟了?)" -ForegroundColor Gray
    Write-Host "  2. 收集時間過短" -ForegroundColor Gray
    Write-Host "  3. logcat 權限問題" -ForegroundColor Gray
    Write-Host ""
    exit 1
}

# 快速分析
Write-Host "檢查關鍵日誌..." -ForegroundColor White
Write-Host ""

$logContent = Get-Content $logFile -Raw

# 統計
$delegatingEnableCount = ([regex]::Matches($logContent, "Delegating HDR enable")).Count
$delegatingDisableCount = ([regex]::Matches($logContent, "Delegating HDR disable")).Count
$liveEnableCount = ([regex]::Matches($logContent, "LiveCameraSource.*HDR enabled")).Count
$liveDisableCount = ([regex]::Matches($logContent, "LiveCameraSource.*HDR disabled")).Count
$staticEnableCount = ([regex]::Matches($logContent, "StaticImageSource.*enableHDR")).Count
$staticDisableCount = ([regex]::Matches($logContent, "StaticImageSource.*disableHDR")).Count
$hdrModeChangedCount = ([regex]::Matches($logContent, "HDR mode changed")).Count
$hdrForceEnabledCount = ([regex]::Matches($logContent, "HDR mode: ON - Force enabled")).Count
$hdrForceDisabledCount = ([regex]::Matches($logContent, "HDR mode: OFF - Force disabled")).Count
$autoSkipCount = ([regex]::Matches($logContent, "Auto HDR skipped")).Count
$controlErrorCount = ([regex]::Matches($logContent, "CameraControl not available")).Count
$exposureSetCount = ([regex]::Matches($logContent, "exposure compensation set to")).Count
$exposureResetCount = ([regex]::Matches($logContent, "exposure compensation reset to")).Count

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "📊 統計結果" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Repository 層 (委派):" -ForegroundColor Yellow
Write-Host "  Delegating HDR enable: $delegatingEnableCount 次" -ForegroundColor $(if ($delegatingEnableCount -gt 0) { "Green" } else { "Red" })
Write-Host "  Delegating HDR disable: $delegatingDisableCount 次" -ForegroundColor $(if ($delegatingDisableCount -gt 0) { "Green" } else { "Red" })
Write-Host ""

Write-Host "Camera 層 (執行):" -ForegroundColor Yellow
Write-Host "  LiveCameraSource HDR enabled: $liveEnableCount 次" -ForegroundColor $(if ($liveEnableCount -gt 0) { "Green" } else { "Gray" })
Write-Host "  LiveCameraSource HDR disabled: $liveDisableCount 次" -ForegroundColor $(if ($liveDisableCount -gt 0) { "Green" } else { "Gray" })
Write-Host "  StaticImageSource enableHDR: $staticEnableCount 次" -ForegroundColor $(if ($staticEnableCount -gt 0) { "Cyan" } else { "Gray" })
Write-Host "  StaticImageSource disableHDR: $staticDisableCount 次" -ForegroundColor $(if ($staticDisableCount -gt 0) { "Cyan" } else { "Gray" })
Write-Host "  Exposure compensation set: $exposureSetCount 次" -ForegroundColor $(if ($exposureSetCount -gt 0) { "Green" } else { "Gray" })
Write-Host "  Exposure compensation reset: $exposureResetCount 次" -ForegroundColor $(if ($exposureResetCount -gt 0) { "Green" } else { "Gray" })
Write-Host ""

Write-Host "ViewModel 層 (狀態):" -ForegroundColor Yellow
Write-Host "  HDR mode: ON - Force enabled: $hdrForceEnabledCount 次" -ForegroundColor $(if ($hdrForceEnabledCount -gt 0) { "Green" } else { "Gray" })
Write-Host "  HDR mode: OFF - Force disabled: $hdrForceDisabledCount 次" -ForegroundColor $(if ($hdrForceDisabledCount -gt 0) { "Green" } else { "Gray" })
Write-Host "  HDR mode changed: $hdrModeChangedCount 次" -ForegroundColor $(if ($hdrModeChangedCount -gt 0) { "Green" } else { "Red" })
Write-Host "  Auto HDR skipped: $autoSkipCount 次" -ForegroundColor $(if ($autoSkipCount -eq 0) { "Green" } else { "Yellow" })
Write-Host ""

Write-Host "錯誤檢查:" -ForegroundColor Yellow
Write-Host "  CameraControl not available: $controlErrorCount 次" -ForegroundColor $(if ($controlErrorCount -eq 0) { "Green" } else { "Red" })
Write-Host ""

# 評估結果
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🎯 測試評估" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$totalDelegations = $delegatingEnableCount + $delegatingDisableCount
$totalModeChanges = $hdrModeChangedCount

if ($controlErrorCount -eq 0 -and $totalDelegations -gt 0 -and $totalModeChanges -gt 0) {
    Write-Host "✅ 測試通過!" -ForegroundColor Green
    Write-Host ""
    Write-Host "結論:" -ForegroundColor Green
    Write-Host "  ✓ v2.6.2 在 Sony XQ-BT52 (Android 11) 上正常運作" -ForegroundColor Green
    Write-Host "  ✓ HDR 手動控制功能完全正常" -ForegroundColor Green
    Write-Host "  ✓ Camera Input Abstraction Layer 運作正確" -ForegroundColor Green
    Write-Host "  ✓ 零 CameraControl 錯誤" -ForegroundColor Green
    Write-Host ""

    if ($liveEnableCount -gt 0 -or $liveDisableCount -gt 0) {
        Write-Host "  🎉 LiveCameraSource 模式驗證成功!" -ForegroundColor Cyan
        Write-Host "     - 真實相機曝光補償調整正常" -ForegroundColor Cyan
    }
    if ($staticEnableCount -gt 0 -or $staticDisableCount -gt 0) {
        Write-Host "  🎉 StaticImageSource 模式驗證成功!" -ForegroundColor Cyan
        Write-Host "     - 測試圖片模式 no-op 行為正常" -ForegroundColor Cyan
    }

} elseif ($controlErrorCount -eq 0 -and $totalDelegations -eq 0) {
    Write-Host "⚠️  可疑結果" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "未偵測到 HDR 操作,可能原因:" -ForegroundColor Yellow
    Write-Host "  1. TC6 測試未正確執行 (未點擊 HDR 按鈕)" -ForegroundColor Gray
    Write-Host "  2. APP 在 StaticImageSource 模式 (測試圖片模式)" -ForegroundColor Gray
    Write-Host "  3. 日誌收集時機問題" -ForegroundColor Gray
    Write-Host ""
    Write-Host "建議:" -ForegroundColor Yellow
    Write-Host "  1. 確認 APP 是否在 Live Camera 模式" -ForegroundColor White
    Write-Host "  2. 重新執行測試,確實點擊 HDR 按鈕" -ForegroundColor White
    Write-Host "  3. 延長收集時間 (至少 30 秒)" -ForegroundColor White

} else {
    Write-Host "❌ 測試失敗!" -ForegroundColor Red
    Write-Host ""
    Write-Host "發現問題:" -ForegroundColor Red
    Write-Host "  ✗ CameraControl 錯誤: $controlErrorCount 次" -ForegroundColor Red
    Write-Host ""
    Write-Host "這表示 v2.6.2 在 Sony 裝置上可能有問題" -ForegroundColor Red
    Write-Host "需要進入 L2 可行性分析" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "📄 關鍵日誌摘錄 (前 50 行)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 提取關鍵日誌
$keyLogs = Select-String -Path $logFile -Pattern "CameraRepositoryImpl|LiveCameraSource.*HDR|StaticImageSource.*HDR|CameraViewModel.*HDR|Delegating|enableHDR|disableHDR|HDR mode|exposure compensation|CameraControl not available" | Select-Object -First 50

if ($keyLogs) {
    $keyLogs | ForEach-Object {
        $line = $_.Line
        # 高亮關鍵字
        if ($line -match "CameraControl not available") {
            Write-Host $line -ForegroundColor Red
        } elseif ($line -match "Delegating HDR") {
            Write-Host $line -ForegroundColor Green
        } elseif ($line -match "HDR mode changed") {
            Write-Host $line -ForegroundColor Cyan
        } elseif ($line -match "HDR enabled|exposure compensation set") {
            Write-Host $line -ForegroundColor Green
        } elseif ($line -match "HDR disabled|exposure compensation reset") {
            Write-Host $line -ForegroundColor Yellow
        } elseif ($line -match "Auto HDR skipped") {
            Write-Host $line -ForegroundColor Gray
        } else {
            Write-Host $line -ForegroundColor White
        }
    }
} else {
    Write-Host "⚠️  未找到任何 HDR 相關日誌" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "這可能表示:" -ForegroundColor Yellow
    Write-Host "  1. APP 未啟動相機功能" -ForegroundColor Gray
    Write-Host "  2. 日誌過濾有問題" -ForegroundColor Gray
    Write-Host "  3. PID 不正確" -ForegroundColor Gray
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "完整日誌: $logFile" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "✅ 測試完成!" -ForegroundColor Green
Write-Host ""
