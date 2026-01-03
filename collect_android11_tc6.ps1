# Android 11 模擬器 TC6 手動測試 - Logcat 收集腳本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Android 11 模擬器 TC6 手動測試" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 確認模擬器連接
$emulator = "emulator-5554"

Write-Host "📱 檢查模擬器狀態..." -ForegroundColor Yellow
$devices = adb devices | Select-String $emulator

if (-not $devices) {
    Write-Host "❌ 找不到模擬器 $emulator" -ForegroundColor Red
    Write-Host ""
    Write-Host "請先啟動 Android 11 模擬器" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ 模擬器已連接: $emulator" -ForegroundColor Green
Write-Host ""

# 確認 APP 是否在運行
Write-Host "📱 檢查 APP 狀態..." -ForegroundColor Yellow
$pid = adb -s $emulator shell pidof com.akirachang.ai.photographer.emulator

if (-not $pid) {
    Write-Host "⚠️  APP 未運行,正在啟動..." -ForegroundColor Yellow
    adb -s $emulator shell am start -n com.akirachang.ai.photographer.emulator/com.akirachang.ai.photographer.MainActivity
    Start-Sleep -Seconds 3
    $pid = adb -s $emulator shell pidof com.akirachang.ai.photographer.emulator
}

if ($pid) {
    Write-Host "✅ APP 正在運行 (PID: $pid)" -ForegroundColor Green
} else {
    Write-Host "❌ 無法啟動 APP" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 設定日誌檔案
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logFile = "$PSScriptRoot\android11_tc6_manual_$timestamp.txt"

Write-Host "📊 日誌檔案: $logFile" -ForegroundColor Cyan
Write-Host ""

# 清除舊 logcat
Write-Host "🧹 清除 logcat buffer..." -ForegroundColor Yellow
adb -s $emulator logcat -c
Write-Host "✅ Buffer 已清除" -ForegroundColor Green
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "⚠️  準備執行 TC6 手動測試" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "請在模擬器中執行以下操作:" -ForegroundColor White
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
Write-Host "📡 開始收集 logcat (串流模式)..." -ForegroundColor Green
Write-Host "⚠️  請現在執行上述 TC6 測試操作" -ForegroundColor Yellow
Write-Host ""
Write-Host "完成後按 Ctrl+C 停止收集" -ForegroundColor Cyan
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 串流收集 logcat (使用 package 過濾)
try {
    adb -s $emulator logcat -v threadtime --pid=$pid | Tee-Object -FilePath $logFile
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
$fileSize = (Get-Item $logFile).Length
Write-Host "📦 日誌檔案大小: $fileSize bytes" -ForegroundColor White
Write-Host ""

if ($fileSize -lt 100) {
    Write-Host "⚠️  日誌檔案過小,可能收集失敗" -ForegroundColor Yellow
    Write-Host ""
    exit 1
}

# 快速分析
Write-Host "檢查關鍵日誌..." -ForegroundColor White
Write-Host ""

$logContent = Get-Content $logFile -Raw

# 統計
$delegatingCount = ([regex]::Matches($logContent, "Delegating HDR")).Count
$enableCount = ([regex]::Matches($logContent, "enableHDR\(\)|HDR enabled")).Count
$disableCount = ([regex]::Matches($logContent, "disableHDR\(\)|HDR disabled")).Count
$controlErrorCount = ([regex]::Matches($logContent, "CameraControl not available")).Count
$hdrModeChangeCount = ([regex]::Matches($logContent, "HDR mode changed")).Count
$autoSkipCount = ([regex]::Matches($logContent, "Auto HDR skipped")).Count

Write-Host "統計結果:" -ForegroundColor Cyan
Write-Host "  ✓ Delegating HDR: $delegatingCount 次" -ForegroundColor $(if ($delegatingCount -gt 0) { "Green" } else { "Red" })
Write-Host "  ✓ enableHDR: $enableCount 次" -ForegroundColor $(if ($enableCount -gt 0) { "Green" } else { "Yellow" })
Write-Host "  ✓ disableHDR: $disableCount 次" -ForegroundColor $(if ($disableCount -gt 0) { "Green" } else { "Yellow" })
Write-Host "  ✓ HDR mode changed: $hdrModeChangeCount 次" -ForegroundColor $(if ($hdrModeChangeCount -gt 0) { "Green" } else { "Yellow" })
Write-Host "  ⚠ Auto HDR skipped: $autoSkipCount 次" -ForegroundColor $(if ($autoSkipCount -eq 0) { "Green" } else { "Yellow" })
Write-Host "  ❌ CameraControl 錯誤: $controlErrorCount 次" -ForegroundColor $(if ($controlErrorCount -eq 0) { "Green" } else { "Red" })
Write-Host ""

# 評估結果
if ($controlErrorCount -eq 0 -and $delegatingCount -gt 0 -and $hdrModeChangeCount -gt 0) {
    Write-Host "✅ 測試通過!" -ForegroundColor Green
    Write-Host "   v2.6.2 在 Android 11 模擬器上正常運作" -ForegroundColor Green
} elseif ($controlErrorCount -eq 0 -and $delegatingCount -eq 0) {
    Write-Host "⚠️  可疑結果" -ForegroundColor Yellow
    Write-Host "   未偵測到 HDR 操作,可能:" -ForegroundColor Yellow
    Write-Host "   1. TC6 測試未正確執行" -ForegroundColor Gray
    Write-Host "   2. 日誌收集時機問題" -ForegroundColor Gray
    Write-Host "   3. HDR 控制被某些條件阻擋" -ForegroundColor Gray
} else {
    Write-Host "❌ 測試失敗!" -ForegroundColor Red
    Write-Host "   發現 $controlErrorCount 次 CameraControl 錯誤" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "📄 關鍵日誌摘錄 (前 50 行)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 提取關鍵日誌
$keyLogs = Select-String -Path $logFile -Pattern "CameraRepositoryImpl|LiveCameraSource|StaticImageSource|CameraViewModel.*HDR|Delegating|enableHDR|disableHDR|HDR mode|CameraControl not available" | Select-Object -First 50

if ($keyLogs) {
    $keyLogs | ForEach-Object {
        $line = $_.Line
        # 高亮關鍵字
        if ($line -match "CameraControl not available") {
            Write-Host $line -ForegroundColor Red
        } elseif ($line -match "Delegating|enableHDR|disableHDR") {
            Write-Host $line -ForegroundColor Green
        } elseif ($line -match "HDR mode changed") {
            Write-Host $line -ForegroundColor Cyan
        } elseif ($line -match "Auto HDR skipped") {
            Write-Host $line -ForegroundColor Yellow
        } else {
            Write-Host $line -ForegroundColor White
        }
    }
} else {
    Write-Host "⚠️  未找到任何 HDR 相關日誌" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "完整日誌: $logFile" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
