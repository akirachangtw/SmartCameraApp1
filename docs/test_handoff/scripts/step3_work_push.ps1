param(
    [string]$Topic,
    [string]$Device
)

$ErrorActionPreference = "Stop"

Write-Host "📤 Test Handoff: 3. Work NB (Push)" -ForegroundColor Cyan
Write-Host ""

Write-Host "Step 1: Checking git status..." -ForegroundColor Yellow
git status --short
Write-Host ""

Write-Host "Step 2: Adding test results..." -ForegroundColor Yellow
git add docs/test_results/
Write-Host ""

Write-Host "Step 3: Committing test results..." -ForegroundColor Yellow
$testResult = Read-Host "Enter test result (PASS/FAIL)"
$dateStr = Get-Date -Format "yyyy-MM-dd"

$commitMsg1 = "[Test] $Topic : Test results - $testResult"
$commitMsg2 = "Device: $Device"
$commitMsg3 = "Tester: Claude Code (Work NB)"
$commitMsg4 = "Date: $dateStr"
$commitMsg5 = ""
$commitMsg6 = "Co-Authored-By: Claude Sonnet 4.5 (noreply@anthropic.com)"

git commit -m $commitMsg1 -m $commitMsg2 -m $commitMsg3 -m $commitMsg4 -m $commitMsg5 -m $commitMsg6
Write-Host ""

Write-Host "Step 4: Pushing to GitHub..." -ForegroundColor Yellow
git push origin main
Write-Host ""

Write-Host "✅ Test results pushed successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Next step: Switch to Home NB and run '📥 Test Handoff: 4. Home NB (Pull)'" -ForegroundColor Cyan
