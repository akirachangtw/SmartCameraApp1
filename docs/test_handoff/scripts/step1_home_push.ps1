param(
    [string]$Topic,
    [string]$Device,
    [string]$Objective
)

$ErrorActionPreference = "Stop"

Write-Host "📤 Test Handoff: 1. Home NB (Push)" -ForegroundColor Cyan
Write-Host ""

Write-Host "Step 1: Checking git status..." -ForegroundColor Yellow
git status --short
Write-Host ""

Write-Host "Step 2: Adding test handoff files..." -ForegroundColor Yellow
git add docs/test_handoff/
Write-Host ""

Write-Host "Step 3: Committing..." -ForegroundColor Yellow
$commitMsg1 = "[Docs] Test handoff: $Topic"
$commitMsg2 = "Test Device: $Device"
$commitMsg3 = "Test Objective: $Objective"
$commitMsg4 = ""
$commitMsg5 = "Co-Authored-By: Claude Sonnet 4.5 (noreply@anthropic.com)"

git commit -m $commitMsg1 -m $commitMsg2 -m $commitMsg3 -m $commitMsg4 -m $commitMsg5
Write-Host ""

Write-Host "Step 4: Pushing to GitHub..." -ForegroundColor Yellow
git push github main
Write-Host ""

Write-Host "✅ Test handoff pushed successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Next step: Switch to Work NB and run '📥 Test Handoff: 2. Work NB (Pull)'" -ForegroundColor Cyan
