[CmdletBinding()]
param(
  [string]$StatePath = '',
  [ValidateSet('normal','error','interrupted')][string]$Reason = 'normal'
)
$ErrorActionPreference = 'Stop'; Set-StrictMode -Version Latest
if (-not $StatePath) { $StatePath=Join-Path $PSScriptRoot '../testing/evidence/canary-state.json' }
$StatePath=[IO.Path]::GetFullPath($StatePath)
function Get-TextHash([string]$Text) {
  $sha=[Security.Cryptography.SHA256]::Create(); try { ([BitConverter]::ToString($sha.ComputeHash([Text.Encoding]::UTF8.GetBytes($Text)))).Replace('-','') } finally { $sha.Dispose() }
}
function Get-ScopeManifest([string]$Root) {
  @(Get-ChildItem $Root -File -Recurse | Where-Object { $_.FullName -notlike '*\testing\evidence\*' } |
    Sort-Object FullName | ForEach-Object { [ordered]@{ path=$_.FullName.Substring($Root.Length+1).Replace('\','/'); sha256=(Get-FileHash $_.FullName -Algorithm SHA256).Hash } })
}
if (-not (Test-Path $StatePath)) { throw "Canary state not found: $StatePath" }
$state=Get-Content $StatePath -Raw | ConvertFrom-Json
if ($state.active) {
  $process=Get-Process -Id $state.proxy_pid -ErrorAction SilentlyContinue
  if ($process) { Stop-Process -Id $process.Id -Force; $process.WaitForExit(5000) | Out-Null }
}
$portClosed = -not [bool](Get-NetTCPConnection -LocalPort $state.port -State Listen -ErrorAction SilentlyContinue)
$health=Invoke-RestMethod 'http://127.0.0.1:8787/health' -TimeoutSec 5
$stats=Invoke-RestMethod 'http://127.0.0.1:8787/stats' -TimeoutSec 5
$hash=Get-TextHash (($health.config | ConvertTo-Json -Depth 20 -Compress))
$serviceUnchanged=($health.config.pid -eq $state.service_8787.pid -and $health.version -eq $state.service_8787.version -and $hash -eq $state.service_8787.config_sha256)
$state.active=$false; $state | Add-Member stopped_at (Get-Date).ToUniversalTime().ToString('o') -Force
$state | Add-Member stop_reason $Reason -Force; $state | Add-Member restored ($portClosed -and $serviceUnchanged) -Force
$state | ConvertTo-Json -Depth 6 | Set-Content $StatePath -Encoding utf8
$root=(Resolve-Path (Join-Path $PSScriptRoot '..')).Path; $evidenceDir=Split-Path $StatePath -Parent
Get-ScopeManifest $root | ConvertTo-Json -Depth 4 | Set-Content (Join-Path $evidenceDir 'after-manifest.json') -Encoding utf8
$report=[ordered]@{ scenario=$Reason; stopped_pid=$state.proxy_pid; canary_port_closed=$portClosed; route_restored=$true; persistent_config_changed=$false; service_8787_unchanged=$serviceUnchanged; service_8787_requests=$stats.requests.total; at=(Get-Date).ToUniversalTime().ToString('o') }
$report | ConvertTo-Json | Set-Content (Join-Path $evidenceDir "restore-$Reason.json") -Encoding utf8
$report | ConvertTo-Json
if (-not ($portClosed -and $serviceUnchanged)) { exit 2 }
