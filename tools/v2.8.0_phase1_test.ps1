# --- 設定區 ---
$AppPkg = "com.akirachang.ai.photographer"
$MainActivity = "$AppPkg/$AppPkg.MainActivity"
$LogPath = ".\tmp\phase1_test.log"

# 按鈕座標 (請根據您的手機修改)
$X_Live = 540
$Y_Live = 1800
$X_Switch = 315
$Y_Switch = 2300

# 建立 tmp 目錄 (如果不存在)
if (-not (Test-Path ".\tmp")) { New-Item -ItemType Directory -Force -Path ".\tmp" | Out-Null }

Write-Host "🚀 Starting Phase 1 Race Condition Test (Windows 11)..." -ForegroundColor Cyan

# 0. 清除舊 Log
adb logcat -c
Write-Host "🧹 Logcat cleared." -ForegroundColor Gray

# 1. 啟動 App
Write-Host "📲 Launching App..." -ForegroundColor Green
adb shell am start -n $MainActivity
Start-Sleep -Seconds 3

# 2. 進入 Live Mode
Write-Host "👆 Tapping 'Live' Mode..." -ForegroundColor Green
adb shell input tap $X_Live $Y_Live
Start-Sleep -Seconds 2

# 3. 快速切換鏡頭 20 次
Write-Host "📸 Starting Camera Switch Stress Test (20 times)..." -ForegroundColor Yellow
for ($i=1; $i -le 20; $i++) {
    Write-Host "   - Switch #$i"
    adb shell input tap $X_Switch $Y_Switch
    
    # 這裡可以調整秒數，支援小數點，例如 0.5
    Start-Sleep -Seconds 1
}

# 4. 前後景切換測試 (5 次)
Write-Host "🔄 Starting Foreground/Background Toggle Test (5 times)..." -ForegroundColor Yellow
for ($i=1; $i -le 5; $i++) {
    # 修改點：使用 $($i) 來避免冒號造成的語法誤判
    Write-Host "   - Toggle #$($i): Home -> Resume"
    
    # 模擬 Home 鍵 (KEYCODE_HOME = 3)
    adb shell input keyevent 3
    Start-Sleep -Seconds 1
    
    # 重新呼叫 Activity
    adb shell am start -n $MainActivity
    Start-Sleep -Seconds 2
}

# 5. 收集 Log 與分析
Write-Host "📝 Collecting Logs..." -ForegroundColor Cyan
# 注意：PowerShell 的 > 重導向有時會有編碼問題，建議用 cmd 轉發或直接讀取
# 這裡使用簡單方式：先存檔，再讀取內容檢查
cmd /c "adb logcat -d > $LogPath" 

Write-Host "🔍 Analyzing Errors..."
$LogContent = Get-Content -Path $LogPath

if ($LogContent | Select-String -Pattern "CameraControl not available" -Quiet) {
    Write-Host "❌ TEST FAILED: Found 'CameraControl not available' error!" -ForegroundColor Red
    Write-Host "   Please check $LogPath details."
} else {
    Write-Host "✅ TEST PASSED: No critical camera errors found." -ForegroundColor Green
}

Write-Host "🏁 Test Finished."