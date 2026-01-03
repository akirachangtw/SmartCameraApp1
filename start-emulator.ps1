# start-emulator.ps1 - Android Emulator Launcher for SmartCameraApp
# 用途: 從 VSCode 啟動 Android 模擬器
# 使用: .\start-emulator.ps1 [-List] [-AvdName "模擬器名稱"]

param(
    [string]$AvdName = "",                # 模擬器名稱（留空會自動偵測）
    [switch]$List = $false                # 列出可用模擬器
)

Write-Host "🚀 SmartCameraApp - Android Emulator Launcher" -ForegroundColor Cyan
Write-Host "=" * 60 -ForegroundColor Gray

# 步驟 1: 尋找 Android SDK
Write-Host "`n📂 步驟 1: 尋找 Android SDK..." -ForegroundColor Yellow

$possibleSdkPaths = @(
    "$env:ANDROID_HOME",
    "$env:ANDROID_SDK_ROOT",
    "$env:LOCALAPPDATA\Android\Sdk",
    "$env:USERPROFILE\AppData\Local\Android\Sdk",
    "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"
)

$ANDROID_HOME = $null
foreach ($path in $possibleSdkPaths) {
    if ($path -and (Test-Path $path)) {
        $ANDROID_HOME = $path
        Write-Host "✅ 找到 Android SDK: $ANDROID_HOME" -ForegroundColor Green
        break
    }
}

if (-not $ANDROID_HOME) {
    Write-Host "❌ Android SDK 未找到！" -ForegroundColor Red
    Write-Host "`n請執行以下步驟:" -ForegroundColor Yellow
    Write-Host "1. 開啟 Android Studio" -ForegroundColor White
    Write-Host "2. File → Settings → Appearance & Behavior → System Settings → Android SDK" -ForegroundColor White
    Write-Host "3. 複製 'Android SDK Location' 路徑" -ForegroundColor White
    Write-Host "4. 設定環境變數 ANDROID_HOME 為該路徑" -ForegroundColor White
    Write-Host "`n或直接在 Android Studio 中啟動模擬器:" -ForegroundColor Cyan
    Write-Host "   Tools → Device Manager → 選擇 AVD → 點擊 ▶️" -ForegroundColor White
    exit 1
}

$emulatorPath = Join-Path $ANDROID_HOME "emulator\emulator.exe"
$adbPath = Join-Path $ANDROID_HOME "platform-tools\adb.exe"

# 驗證工具存在
if (-not (Test-Path $emulatorPath)) {
    Write-Host "❌ Emulator 執行檔未找到: $emulatorPath" -ForegroundColor Red
    Write-Host "請在 Android Studio SDK Manager 中安裝 'Android Emulator'" -ForegroundColor Yellow
    exit 1
}

if (-not (Test-Path $adbPath)) {
    Write-Host "❌ ADB 執行檔未找到: $adbPath" -ForegroundColor Red
    Write-Host "請在 Android Studio SDK Manager 中安裝 'Android SDK Platform-Tools'" -ForegroundColor Yellow
    exit 1
}

# 步驟 2: 列出可用模擬器
Write-Host "`n📱 步驟 2: 檢查可用模擬器..." -ForegroundColor Yellow

$avdList = & $emulatorPath -list-avds 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 無法列出模擬器" -ForegroundColor Red
    Write-Host $avdList -ForegroundColor Red
    exit 1
}

if (-not $avdList) {
    Write-Host "❌ 沒有可用的模擬器！" -ForegroundColor Red
    Write-Host "`n請在 Android Studio 中建立模擬器:" -ForegroundColor Yellow
    Write-Host "1. Tools → Device Manager" -ForegroundColor White
    Write-Host "2. Create Device" -ForegroundColor White
    Write-Host "3. 選擇 Pixel 5 或 Pixel 6" -ForegroundColor White
    Write-Host "4. 選擇 API Level 30-34" -ForegroundColor White
    exit 1
}

Write-Host "✅ 找到以下模擬器:" -ForegroundColor Green
$avdList | ForEach-Object { Write-Host "   - $_" -ForegroundColor Cyan }

# 如果只是列出模擬器，到此結束
if ($List) {
    Write-Host "`n📋 使用方式:" -ForegroundColor Yellow
    Write-Host "   .\start-emulator.ps1 -AvdName `"模擬器名稱`"" -ForegroundColor White
    exit 0
}

# 步驟 3: 選擇模擬器
Write-Host "`n🎯 步驟 3: 選擇模擬器..." -ForegroundColor Yellow

if (-not $AvdName) {
    # 自動選擇第一個模擬器
    $AvdName = $avdList | Select-Object -First 1
    Write-Host "⚡ 自動選擇: $AvdName" -ForegroundColor Green
} else {
    # 驗證指定的模擬器存在
    if ($avdList -notcontains $AvdName) {
        Write-Host "❌ 模擬器 '$AvdName' 不存在！" -ForegroundColor Red
        Write-Host "可用的模擬器:" -ForegroundColor Yellow
        $avdList | ForEach-Object { Write-Host "   - $_" -ForegroundColor Cyan }
        exit 1
    }
    Write-Host "✅ 選擇: $AvdName" -ForegroundColor Green
}

# 步驟 4: 檢查是否已有模擬器在運行
Write-Host "`n🔍 步驟 4: 檢查現有模擬器..." -ForegroundColor Yellow

$runningDevices = & $adbPath devices 2>&1 | Select-String "emulator.*device"
if ($runningDevices) {
    Write-Host "⚠️  偵測到已運行的模擬器:" -ForegroundColor Yellow
    & $adbPath devices

    Write-Host "`n是否繼續啟動新的模擬器? (y/N): " -ForegroundColor Cyan -NoNewline
    $continue = Read-Host
    if ($continue -ne 'y' -and $continue -ne 'Y') {
        Write-Host "✅ 使用現有模擬器，啟動程序取消" -ForegroundColor Green
        exit 0
    }
}

# 步驟 5: 啟動模擬器
Write-Host "`n🚀 步驟 5: 啟動模擬器..." -ForegroundColor Yellow
Write-Host "   AVD: $AvdName" -ForegroundColor White
Write-Host "   參數: -avd $AvdName -gpu host" -ForegroundColor Gray

try {
    Start-Process $emulatorPath -ArgumentList "-avd", $AvdName, "-gpu", "host" -NoNewWindow
    Write-Host "✅ 模擬器啟動中..." -ForegroundColor Green
} catch {
    Write-Host "❌ 啟動失敗: $_" -ForegroundColor Red
    exit 1
}

# 步驟 6: 等待模擬器完全啟動
Write-Host "`n⏳ 步驟 6: 等待模擬器啟動完成..." -ForegroundColor Yellow
Write-Host "   (預計需要 30-90 秒，首次啟動可能更久)" -ForegroundColor Gray

$timeout = 120  # 120 秒超時
$elapsed = 0
$checkInterval = 3  # 每 3 秒檢查一次

while ($elapsed -lt $timeout) {
    Start-Sleep -Seconds $checkInterval
    $elapsed += $checkInterval

    # 檢查是否出現 device（而非 offline）
    $bootCompleted = & $adbPath devices 2>&1 | Select-String "emulator.*device$"

    if ($bootCompleted) {
        # 再確認一次 boot completed 狀態
        $bootStatus = & $adbPath shell getprop sys.boot_completed 2>&1
        if ($bootStatus -eq "1") {
            Write-Host "`n✅ 模擬器啟動完成！" -ForegroundColor Green
            Write-Host "`n📱 連接的裝置:" -ForegroundColor Cyan
            & $adbPath devices

            Write-Host "`n🎯 下一步操作:" -ForegroundColor Yellow
            Write-Host "   1. 建置並安裝應用:" -ForegroundColor White
            Write-Host "      ./gradlew.bat installEmulatorDebug" -ForegroundColor Cyan
            Write-Host "`n   2. 或使用 VSCode Task (Ctrl+Shift+P):" -ForegroundColor White
            Write-Host "      Tasks: Run Task → 📱 Install to Emulator" -ForegroundColor Cyan
            Write-Host "`n   3. 或執行完整測試循環:" -ForegroundColor White
            Write-Host "      .\test-v246-emulator.bat" -ForegroundColor Cyan

            exit 0
        }
    }

    # 顯示進度
    Write-Host "." -NoNewline -ForegroundColor Gray
}

Write-Host "`n⚠️  模擬器啟動超時（${timeout}秒）" -ForegroundColor Yellow
Write-Host "   可能原因:" -ForegroundColor White
Write-Host "   - 首次啟動需要更多時間（請再等待 1-2 分鐘）" -ForegroundColor Gray
Write-Host "   - 電腦效能不足" -ForegroundColor Gray
Write-Host "   - 未啟用硬體加速（Intel HAXM / AMD Hypervisor）" -ForegroundColor Gray
Write-Host "`n請手動檢查模擬器視窗狀態" -ForegroundColor Cyan
Write-Host "使用 'adb devices' 檢查連接狀態" -ForegroundColor Cyan
