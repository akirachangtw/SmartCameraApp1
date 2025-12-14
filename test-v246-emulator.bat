@echo off
REM v2.4.6 模擬器測試自動化腳本
REM 使用方式: test-v246-emulator.bat [test-type]
REM   test-type: all (預設), unit, ui, quick

setlocal
set TEST_TYPE=%1
if "%TEST_TYPE%"=="" set TEST_TYPE=all

echo.
echo ================================================
echo   v2.4.6 模擬器測試套件
echo ================================================
echo   測試類型: %TEST_TYPE%
echo   日期時間: %date% %time%
echo ================================================
echo.

REM 設定日誌檔案
set LOG_FILE=v2.4.6_test_%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%.log
set LOG_FILE=%LOG_FILE: =0%

REM 步驟 1: 清理舊構建
if "%TEST_TYPE%"=="all" (
    echo [1/6] 清理舊構建...
    call gradlew.bat clean
    if errorlevel 1 (
        echo ❌ 清理失敗！
        exit /b 1
    )
    echo ✅ 清理完成
    echo.
)

REM 步驟 2: 構建模擬器版本
echo [2/6] 構建模擬器 Debug 版本...
call gradlew.bat assembleEmulatorDebug
if errorlevel 1 (
    echo ❌ 構建失敗！
    exit /b 1
)
echo ✅ 構建成功
echo.

REM 步驟 3: 執行單元測試
if "%TEST_TYPE%"=="all" (
    echo [3/6] 執行所有單元測試...
    call gradlew.bat testEmulatorDebugUnitTest
) else if "%TEST_TYPE%"=="unit" (
    echo [3/6] 執行單元測試...
    call gradlew.bat testEmulatorDebugUnitTest
) else if "%TEST_TYPE%"=="quick" (
    echo [3/6] 執行快速測試（僅構圖分析）...
    call gradlew.bat testEmulatorDebugUnitTest --tests "*AnalyzeCompositionUseCaseTest"
) else (
    echo [3/6] 跳過單元測試
)

if errorlevel 1 (
    echo ⚠️ 部分測試失敗，請檢查測試報告
) else (
    echo ✅ 測試通過
)
echo.

REM 步驟 4: 生成測試報告
echo [4/6] 生成測試報告...
echo 📊 測試報告位置: app\build\reports\tests\testEmulatorDebugUnitTest\index.html
echo.

REM 步驟 5: 檢查模擬器
echo [5/6] 檢查模擬器狀態...
adb devices | findstr "emulator"
if errorlevel 1 (
    echo ⚠️ 未偵測到模擬器，請先啟動模擬器
    echo    跳過安裝步驟...
) else (
    echo ✅ 模擬器已連接
    echo [5/6] 安裝到模擬器...
    call gradlew.bat installEmulatorDebug
    if errorlevel 1 (
        echo ❌ 安裝失敗！
    ) else (
        echo ✅ 安裝成功
    )
)
echo.

REM 步驟 6: 啟動應用並收集日誌（如果有模擬器）
adb devices | findstr "emulator" > nul
if not errorlevel 1 (
    echo [6/6] 啟動應用並收集日誌...
    adb shell am start -n local.mylab.smartcameraapp1.emulator/.MainActivity

    echo 📝 開始收集日誌（按 Ctrl+C 停止）...
    echo 日誌將儲存到: %LOG_FILE%

    adb logcat -c
    adb logcat | findstr /I "AnalyzeComposition CameraModule FakeCameraDataSource CompositionState" > %LOG_FILE%
) else (
    echo [6/6] 跳過應用啟動（無模擬器）
)

echo.
echo ================================================
echo   測試完成！
echo ================================================
echo   📊 測試報告: app\build\reports\tests\testEmulatorDebugUnitTest\index.html
echo   📝 日誌檔案: %LOG_FILE%
echo   📦 APK 位置: app\build\outputs\apk\emulator\debug\app-emulator-debug.apk
echo ================================================
echo.

endlocal
