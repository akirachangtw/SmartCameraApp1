@echo off
REM SmartCameraApp Git Auto Commit Script (Windows)
REM Version: 1.0.0
REM Purpose: Automated git commit with version tagging

setlocal enabledelayedexpansion

echo === SmartCameraApp Git Auto Commit ===
echo.

REM Get version from build.gradle.kts
for /f "tokens=2 delims=^" " %%a in ('findstr /R "versionName = " app\build.gradle.kts') do set VERSION=%%a
for /f "tokens=2 delims== " %%a in ('findstr /R "versionCode = " app\build.gradle.kts') do set VERSION_CODE=%%a

echo Version: %VERSION% (Code: %VERSION_CODE%)
echo.

REM Check if there are changes
git status -s > nul 2>&1
if errorlevel 1 (
    echo No changes to commit
    exit /b 0
)

REM Show status
echo Current changes:
git status -s
echo.

REM Confirm with user
set /p CONFIRM="Proceed with commit? (y/n): "
if /i not "%CONFIRM%"=="y" (
    echo Commit cancelled
    exit /b 1
)

REM Stage all changes
echo.
echo Staging changes...
git add .

REM Create commit message (simplified for Windows)
echo.
echo Creating commit...
git commit -m "[v%VERSION%] Version Release - Auto Commit ^

Generated with Claude Code ^

Co-Authored-By: Claude <noreply@anthropic.com>"

REM Create tag
echo.
echo Creating tag v%VERSION%...
git tag -a "v%VERSION%" -m "v%VERSION% - Release"

REM Show commit
echo.
echo Commit created:
git log -1 --oneline

REM Ask about push
echo.
set /p PUSH="Push to remote? (y/n): "
if /i "%PUSH%"=="y" (
    echo.
    echo Pushing to remote...
    git push origin main --tags
    echo [SUCCESS] Pushed successfully
) else (
    echo Skipped push. Run 'git push origin main --tags' manually
)

echo.
echo [SUCCESS] Git operations completed
pause
