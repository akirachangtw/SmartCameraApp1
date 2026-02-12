# Branch Documentation Sync Checker
# Purpose: Compare devspec/ and tools/ files between branches before merging to main
# Usage: .\tools\check_branch_docs_sync.ps1 <source-branch> [target-branch]
# Example: .\tools\check_branch_docs_sync.ps1 feature/camera-core-v2.11.0 main

param(
    [Parameter(Mandatory=$true)]
    [string]$SourceBranch,

    [Parameter(Mandatory=$false)]
    [string]$TargetBranch = "main"
)

Write-Host "🔍 Checking devspec/ and tools/ documentation sync..." -ForegroundColor Cyan
Write-Host "   Source: $SourceBranch" -ForegroundColor Yellow
Write-Host "   Target: $TargetBranch" -ForegroundColor Yellow
Write-Host ""

# Get current branch
$currentBranch = git rev-parse --abbrev-ref HEAD

# Function to get files from a branch
function Get-BranchFiles {
    param([string]$Branch, [string]$Path)
    $files = git ls-tree -r --name-only $Branch $Path 2>$null | Where-Object { $_ -match '\.(md|py|pyw|ps1|sh)$' }
    return $files | Sort-Object
}

# Get files from both branches
Write-Host "📂 Listing files in devspec/..." -ForegroundColor Cyan
$sourceDevspec = Get-BranchFiles $SourceBranch "devspec/"
$targetDevspec = Get-BranchFiles $TargetBranch "devspec/"

Write-Host "📂 Listing files in tools/..." -ForegroundColor Cyan
$sourceTools = Get-BranchFiles $SourceBranch "tools/"
$targetTools = Get-BranchFiles $TargetBranch "tools/"

# Compare devspec/
Write-Host ""
Write-Host "=" * 80 -ForegroundColor Gray
Write-Host "📋 devspec/ Comparison" -ForegroundColor Cyan
Write-Host "=" * 80 -ForegroundColor Gray

$devspecOnlyInSource = $sourceDevspec | Where-Object { $_ -notin $targetDevspec }
$devspecOnlyInTarget = $targetDevspec | Where-Object { $_ -notin $sourceDevspec }

if ($devspecOnlyInSource.Count -gt 0) {
    Write-Host ""
    Write-Host "⚠️  Files in $SourceBranch but NOT in $TargetBranch:" -ForegroundColor Yellow
    $devspecOnlyInSource | ForEach-Object { Write-Host "   + $_" -ForegroundColor Green }
}

if ($devspecOnlyInTarget.Count -gt 0) {
    Write-Host ""
    Write-Host "⚠️  Files in $TargetBranch but NOT in $SourceBranch:" -ForegroundColor Yellow
    $devspecOnlyInTarget | ForEach-Object { Write-Host "   - $_" -ForegroundColor Red }
}

if ($devspecOnlyInSource.Count -eq 0 -and $devspecOnlyInTarget.Count -eq 0) {
    Write-Host ""
    Write-Host "✅ devspec/ files are in sync" -ForegroundColor Green
}

# Compare tools/
Write-Host ""
Write-Host "=" * 80 -ForegroundColor Gray
Write-Host "🛠️  tools/ Comparison" -ForegroundColor Cyan
Write-Host "=" * 80 -ForegroundColor Gray

$toolsOnlyInSource = $sourceTools | Where-Object { $_ -notin $targetTools }
$toolsOnlyInTarget = $targetTools | Where-Object { $_ -notin $sourceTools }

if ($toolsOnlyInSource.Count -gt 0) {
    Write-Host ""
    Write-Host "⚠️  Files in $SourceBranch but NOT in $TargetBranch:" -ForegroundColor Yellow
    $toolsOnlyInSource | ForEach-Object { Write-Host "   + $_" -ForegroundColor Green }
}

if ($toolsOnlyInTarget.Count -gt 0) {
    Write-Host ""
    Write-Host "⚠️  Files in $TargetBranch but NOT in $SourceBranch:" -ForegroundColor Yellow
    $toolsOnlyInTarget | ForEach-Object { Write-Host "   - $_" -ForegroundColor Red }
}

if ($toolsOnlyInSource.Count -eq 0 -and $toolsOnlyInTarget.Count -eq 0) {
    Write-Host ""
    Write-Host "✅ tools/ files are in sync" -ForegroundColor Green
}

# Summary
Write-Host ""
Write-Host "=" * 80 -ForegroundColor Gray
Write-Host "📊 Summary" -ForegroundColor Cyan
Write-Host "=" * 80 -ForegroundColor Gray

$totalMissing = $devspecOnlyInSource.Count + $toolsOnlyInSource.Count
$totalExtra = $devspecOnlyInTarget.Count + $toolsOnlyInTarget.Count

Write-Host ""
Write-Host "Missing in $TargetBranch : $totalMissing files" -ForegroundColor $(if ($totalMissing -gt 0) { "Yellow" } else { "Green" })
Write-Host "Extra in $TargetBranch   : $totalExtra files" -ForegroundColor $(if ($totalExtra -gt 0) { "Yellow" } else { "Green" })

# Recommendations
if ($totalMissing -gt 0) {
    Write-Host ""
    Write-Host "💡 Recommendation:" -ForegroundColor Cyan
    Write-Host "   Before merging to $TargetBranch, restore missing files:" -ForegroundColor Yellow
    Write-Host ""

    if ($devspecOnlyInSource.Count -gt 0) {
        Write-Host "   # Restore devspec/ files:" -ForegroundColor Gray
        $devspecOnlyInSource | ForEach-Object {
            Write-Host "   git show ${SourceBranch}:$_ > $_" -ForegroundColor White
        }
        Write-Host ""
    }

    if ($toolsOnlyInSource.Count -gt 0) {
        Write-Host "   # Restore tools/ files:" -ForegroundColor Gray
        $toolsOnlyInSource | ForEach-Object {
            Write-Host "   git show ${SourceBranch}:$_ > $_" -ForegroundColor White
        }
    }
}

Write-Host ""
if ($totalMissing -eq 0 -and $totalExtra -eq 0) {
    Write-Host "✅ All documentation is in sync!" -ForegroundColor Green
    exit 0
} else {
    Write-Host "⚠️  Documentation sync check completed with differences" -ForegroundColor Yellow
    exit 1
}
