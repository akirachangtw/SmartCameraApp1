$ErrorActionPreference = "Stop"

Write-Host "📥 Test Handoff: 4. Home NB (Pull)" -ForegroundColor Cyan
Write-Host ""

Write-Host "Step 1: Pulling latest from GitHub..." -ForegroundColor Yellow
git pull origin main
Write-Host ""

Write-Host "Step 2: Listing test results..." -ForegroundColor Yellow
$reports = Get-ChildItem docs/test_results/*.md -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 5

if ($reports) {
    Write-Host "Recent test reports:" -ForegroundColor Green
    $reports | ForEach-Object { Write-Host "  - $($_.Name) ($($_.LastWriteTime))" }
    Write-Host ""
    
    $latest = $reports[0].FullName
    Write-Host "Opening latest report: $($reports[0].Name)" -ForegroundColor Yellow
    code $latest
} else {
    Write-Host "No test reports found." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "✅ Test handoff cycle completed!" -ForegroundColor Green
