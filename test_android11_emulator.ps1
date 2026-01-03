# Android 11 模擬器測試腳本 (v2.6.2 對比測試)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Android 11 模擬器 HDR 測試 (v2.6.2)" -ForegroundColor Cyan
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

# 列出可用的 AVD
Write-Host "🔍 檢查可用的模擬器..." -ForegroundColor Yellow
$avds = & $emulatorPath -list-avds

if ($avds.Count -eq 0) {
    Write-Host "❌ 沒有可用的模擬器" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "可用模擬器:" -ForegroundColor Green
$avds | ForEach-Object { Write-Host "  - $_" -ForegroundColor White }
Write-Host ""

# 尋找 Android 11 模擬器
$android11AVD = $avds | Where-Object { $_ -match "A11|API_30|Android_11" } | Select-Object -First 1

if (-not $android11AVD) {
    Write-Host "⚠️  未找到 Android 11 模擬器,嘗試使用包含 'Medium' 和 'A11' 的 AVD" -ForegroundColor Yellow
    $android11AVD = $avds | Where-Object { $_ -match "Medium.*A11" } | Select-Object -First 1
}

if (-not $android11AVD) {
    Write-Host "❌ 找不到 Android 11 (API 30) 模擬器" -ForegroundColor Red
    Write-Host ""
    Write-Host "可用的模擬器清單:" -ForegroundColor Yellow
    $avds | ForEach-Object { Write-Host "  - $_" -ForegroundColor White }
    Write-Host ""
    Write-Host "請輸入要使用的模擬器名稱 (例如: Medium_Phone_A11):" -ForegroundColor Yellow
    $android11AVD = Read-Host
}

Write-Host ""
Write-Host "✅ 選擇模擬器: $android11AVD" -ForegroundColor Green
Write-Host ""

# 檢查模擬器是否已在運行
$runningEmulators = adb devices | Select-String "emulator"
if ($runningEmulators) {
    Write-Host "⚠️  偵測到運行中的模擬器:" -ForegroundColor Yellow
    adb devices
    Write-Host ""
    Write-Host "是否要使用已運行的模擬器? (Y/N)" -ForegroundColor Yellow
    $useExisting = Read-Host

    if ($useExisting -eq "Y" -or $useExisting -eq "y") {
        Write-Host "✅ 使用現有模擬器" -ForegroundColor Green
    } else {
        Write-Host "❌ 請先關閉現有模擬器,然後重新執行此腳本" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "🚀 啟動模擬器: $android11AVD" -ForegroundColor Cyan
    Write-Host "請稍候 30-60 秒..." -ForegroundColor Yellow
    Write-Host ""

    Start-Process -FilePath $emulatorPath -ArgumentList "-avd", $android11AVD, "-gpu", "host" -WindowStyle Normal

    Write-Host "⏳ 等待模擬器啟動..." -ForegroundColor Yellow
    Start-Sleep -Seconds 5

    # 等待模擬器完全啟動
    $timeout = 120
    $elapsed = 0
    while ($elapsed -lt $timeout) {
        $bootComplete = adb shell getprop sys.boot_completed 2>$null
        if ($bootComplete -eq "1") {
            Write-Host "✅ 模擬器已完全啟動!" -ForegroundColor Green
            break
        }
        Start-Sleep -Seconds 2
        $elapsed += 2
        Write-Host "." -NoNewline
    }
    Write-Host ""
}

# 取得模擬器序號
$emulatorSerial = adb devices | Select-String "emulator" | ForEach-Object { ($_ -split "\s+")[0] }
if (-not $emulatorSerial) {
    Write-Host "❌ 無法取得模擬器序號" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "✅ 模擬器序號: $emulatorSerial" -ForegroundColor Green
Write-Host ""

# 檢查 Android 版本
$androidVersion = adb -s $emulatorSerial shell getprop ro.build.version.release
$apiLevel = adb -s $emulatorSerial shell getprop ro.build.version.sdk

Write-Host "📱 模擬器資訊:" -ForegroundColor Cyan
Write-Host "  Android 版本: $androidVersion" -ForegroundColor White
Write-Host "  API Level: $apiLevel" -ForegroundColor White
Write-Host ""

if ($apiLevel -ne "30") {
    Write-Host "⚠️  警告: API Level 不是 30 (Android 11)" -ForegroundColor Yellow
    Write-Host "繼續測試? (Y/N)" -ForegroundColor Yellow
    $continue = Read-Host
    if ($continue -ne "Y" -and $continue -ne "y") {
        exit 1
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "開始 v2.6.2 測試流程" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 編譯 emulator-debug APK
Write-Host "📦 步驟 1: 編譯 emulator-debug APK" -ForegroundColor Yellow
Write-Host ""

powershell -ExecutionPolicy Bypass -Command "cd '$PSScriptRoot'; .\gradlew.bat assembleEmulatorDebug --quiet"

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 編譯失敗" -ForegroundColor Red
    exit 1
}

Write-Host "✅ 編譯成功" -ForegroundColor Green
Write-Host ""

# 2. 安裝 APK
Write-Host "📲 步驟 2: 安裝 APK 到模擬器" -ForegroundColor Yellow
Write-Host ""

$apkPath = "$PSScriptRoot\app\build\outputs\apk\emulator\debug\app-emulator-debug.apk"

adb -s $emulatorSerial install -r "$apkPath"

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 安裝失敗" -ForegroundColor Red
    exit 1
}

Write-Host "✅ 安裝成功" -ForegroundColor Green
Write-Host ""

# 3. 啟動 APP
Write-Host "🚀 步驟 3: 啟動 APP" -ForegroundColor Yellow
Write-Host ""

adb -s $emulatorSerial shell am start -n com.akirachang.ai.photographer.emulator/com.akirachang.ai.photographer.MainActivity

Start-Sleep -Seconds 3

Write-Host "✅ APP 已啟動" -ForegroundColor Green
Write-Host ""

# 4. 收集 logcat
Write-Host "📊 步驟 4: 收集 logcat (TC6 手動測試)" -ForegroundColor Yellow
Write-Host ""

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logFile = "$PSScriptRoot\android11_emulator_tc6_$timestamp.txt"

Write-Host "日誌檔案: $logFile" -ForegroundColor White
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "⚠️  請執行 TC6 手動測試:" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. 點擊 HDR 按鈕 → ON (開啟)" -ForegroundColor White
Write-Host "2. 等待 3 秒" -ForegroundColor Gray
Write-Host "3. 點擊 HDR 按鈕 → OFF (關閉)" -ForegroundColor White
Write-Host "4. 等待 3 秒" -ForegroundColor Gray
Write-Host "5. 點擊 HDR 按鈕 → AUTO (自動)" -ForegroundColor White
Write-Host "6. 等待 3 秒" -ForegroundColor Gray
Write-Host "7. 重複 1-6 步驟 2-3 次" -ForegroundColor White
Write-Host ""
Write-Host "完成後按 Enter 鍵停止收集..." -ForegroundColor Yellow
Write-Host ""

# 清除舊 logcat
adb -s $emulatorSerial logcat -c

# 開始收集 (背景執行)
$logcatProcess = Start-Process -FilePath "adb" -ArgumentList "-s", $emulatorSerial, "logcat", "-v", "threadtime" -RedirectStandardOutput $logFile -NoNewWindow -PassThru

Write-Host "📡 Logcat 收集中..." -ForegroundColor Green
Write-Host ""

# 等待使用者完成測試
Read-Host

# 停止收集
Stop-Process -Id $logcatProcess.Id -Force
Write-Host ""
Write-Host "✅ Logcat 收集完成" -ForegroundColor Green
Write-Host ""

# 5. 分析日誌
Write-Host "🔍 步驟 5: 快速分析日誌" -ForegroundColor Yellow
Write-Host ""

$logContent = Get-Content $logFile -Raw

# 檢查關鍵日誌
Write-Host "檢查關鍵日誌..." -ForegroundColor White
Write-Host ""

$delegatingCount = ($logContent | Select-String "Delegating HDR" -AllMatches).Matches.Count
$enableHDRCount = ($logContent | Select-String "enableHDR\(\) called|HDR enabled" -AllMatches).Matches.Count
$disableHDRCount = ($logContent | Select-String "disableHDR\(\) called|HDR disabled" -AllMatches).Matches.Count
$controlErrorCount = ($logContent | Select-String "CameraControl not available" -AllMatches).Matches.Count

Write-Host "結果:" -ForegroundColor Cyan
Write-Host "  Delegating HDR: $delegatingCount 次" -ForegroundColor $(if ($delegatingCount -gt 0) { "Green" } else { "Red" })
Write-Host "  enableHDR 呼叫: $enableHDRCount 次" -ForegroundColor $(if ($enableHDRCount -gt 0) { "Green" } else { "Yellow" })
Write-Host "  disableHDR 呼叫: $disableHDRCount 次" -ForegroundColor $(if ($disableHDRCount -gt 0) { "Green" } else { "Yellow" })
Write-Host "  CameraControl 錯誤: $controlErrorCount 次" -ForegroundColor $(if ($controlErrorCount -eq 0) { "Green" } else { "Red" })
Write-Host ""

if ($controlErrorCount -eq 0 -and $delegatingCount -gt 0) {
    Write-Host "✅ 測試通過! v2.6.2 在 Android 11 模擬器上正常運作" -ForegroundColor Green
} elseif ($controlErrorCount -eq 0 -and $delegatingCount -eq 0) {
    Write-Host "⚠️  未偵測到 HDR 操作,請檢查是否正確執行 TC6 測試" -ForegroundColor Yellow
} else {
    Write-Host "❌ 測試失敗! 發現 CameraControl 錯誤" -ForegroundColor Red
}

Write-Host ""
Write-Host "詳細日誌: $logFile" -ForegroundColor Cyan
Write-Host ""

# 提取關鍵日誌片段
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "關鍵日誌片段 (前 30 行):" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$keyLogs = $logContent | Select-String "CameraRepositoryImpl|LiveCameraSource|StaticImageSource|CameraViewModel.*HDR|Delegating|enableHDR|disableHDR|CameraControl not available" | Select-Object -First 30

if ($keyLogs) {
    $keyLogs | ForEach-Object { Write-Host $_.Line -ForegroundColor White }
} else {
    Write-Host "⚠️  未找到 HDR 相關日誌" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "測試完成!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
