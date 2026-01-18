$ErrorActionPreference = "Stop"

Write-Host "📥 Test Handoff: 2. Work NB (Pull)" -ForegroundColor Cyan
Write-Host ""

Write-Host "Step 1: Pulling latest from GitHub..." -ForegroundColor Yellow
git pull origin main
Write-Host ""

Write-Host "Step 2: Listing available test handoff docs..." -ForegroundColor Yellow
Get-ChildItem docs/test_handoff/*.md | Select-Object Name
Write-Host ""

Write-Host "✅ Test handoff pulled successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Next steps:" -ForegroundColor Cyan
Write-Host "  1. Open Claude Code Chat" -ForegroundColor White
Write-Host "  2. Run: 引用 @docs/test_handoff/v2.9.6_phase1_work_nb_execution.md" -ForegroundColor White
Write-Host "  3. Ask Claude to execute tests and generate report" -ForegroundColor White
Write-Host "  4. After testing, run '📤 Test Handoff: 3. Work NB (Push)'" -ForegroundColor Cyan
