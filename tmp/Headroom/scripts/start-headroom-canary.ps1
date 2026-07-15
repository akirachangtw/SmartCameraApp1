[CmdletBinding()]
param(
  [int]$Port = 0,
  [string]$StatePath = '',
  [ValidateSet('kompress','structural')][string]$CompressionMode = 'kompress',
  [switch]$FailAfterStart
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
if (-not $StatePath) { $StatePath=Join-Path $PSScriptRoot '../testing/evidence/canary-state.json' }
$StatePath=[IO.Path]::GetFullPath($StatePath)

function Get-TextHash([string]$Text) {
  $sha = [Security.Cryptography.SHA256]::Create()
  try { ([BitConverter]::ToString($sha.ComputeHash([Text.Encoding]::UTF8.GetBytes($Text)))).Replace('-', '') }
  finally { $sha.Dispose() }
}
function Get-ScopeManifest([string]$Root) {
  @(Get-ChildItem $Root -File -Recurse | Where-Object { $_.FullName -notlike '*\testing\evidence\*' } |
    Sort-Object FullName | ForEach-Object { [ordered]@{ path = $_.FullName.Substring($Root.Length + 1).Replace('\','/'); sha256 = (Get-FileHash $_.FullName -Algorithm SHA256).Hash } })
}

$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$evidenceDir = Split-Path $StatePath -Parent
New-Item -ItemType Directory -Path $evidenceDir -Force | Out-Null
if (Test-Path $StatePath) {
  $oldState = Get-Content $StatePath -Raw | ConvertFrom-Json
  if ($oldState.active) { throw "An active canary already exists at $StatePath" }
}
foreach ($command in 'headroom','claude') { if (-not (Get-Command $command -ErrorAction SilentlyContinue)) { throw "$command is required" } }
if ($Port -eq 0) {
  $listener = [Net.Sockets.TcpListener]::new([Net.IPAddress]::Loopback, 0); $listener.Start()
  $Port = ([Net.IPEndPoint]$listener.LocalEndpoint).Port; $listener.Stop()
}
if ($Port -eq 8787 -or (Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue)) { throw "Unsafe or busy port: $Port" }

$serviceHealth = Invoke-RestMethod 'http://127.0.0.1:8787/health' -TimeoutSec 5
$serviceStats = Invoke-RestMethod 'http://127.0.0.1:8787/stats' -TimeoutSec 5
$serviceConfigHash = Get-TextHash (($serviceHealth.config | ConvertTo-Json -Depth 20 -Compress))
Get-ScopeManifest $root | ConvertTo-Json -Depth 4 | Set-Content (Join-Path $evidenceDir 'before-manifest.json') -Encoding utf8
$requestLog = Join-Path $evidenceDir 'canary-requests.jsonl'
$stdout = Join-Path $evidenceDir 'canary-proxy.stdout.log'
$stderr = Join-Path $evidenceDir 'canary-proxy.stderr.log'
$proxyArgs = @('proxy','--host','127.0.0.1','--port',"$Port",'--no-cache','--no-rate-limit','--no-ccr-inject-tool','--no-subscription-tracking','--intercept-tool-results','--log-file',$requestLog)
if ($CompressionMode -eq 'structural') { $proxyArgs += '--disable-kompress' } else { $proxyArgs += @('--target-ratio','0.5') }
$savedStable = $env:HEADROOM_COMPRESSION_STABLE_AFTER_TURN
$savedStale = $env:HEADROOM_STALE_READ_COMPRESS_AFTER_TURNS
$process = $null
try {
  $env:HEADROOM_COMPRESSION_STABLE_AFTER_TURN = '0'; $env:HEADROOM_STALE_READ_COMPRESS_AFTER_TURNS = '1'
  $process = Start-Process (Get-Command headroom).Path -ArgumentList $proxyArgs -PassThru -WindowStyle Hidden -RedirectStandardOutput $stdout -RedirectStandardError $stderr
  $health = $null
  for ($i=0; $i -lt 120; $i++) {
    if ($process.HasExited) { throw "Canary proxy exited: $($process.ExitCode)" }
    try { $health = Invoke-RestMethod "http://127.0.0.1:$Port/health" -TimeoutSec 1; if ($health.ready) { break } } catch {}
    Start-Sleep -Milliseconds 500
  }
  if (-not $health -or -not $health.ready) { throw 'Canary proxy did not become ready' }
  $state = [ordered]@{
    schema_version=1; active=$true; restore_registered=$true; persistent_config_changed=$false; compression_mode=$CompressionMode
    port=$Port; proxy_pid=$process.Id; proxy_started_at=$process.StartTime.ToUniversalTime().ToString('o')
    route_scope='child-process-only'; original_anthropic_base_url=$env:ANTHROPIC_BASE_URL
    request_log=$requestLog; started_at=(Get-Date).ToUniversalTime().ToString('o')
    service_8787=[ordered]@{ pid=$serviceHealth.config.pid; version=$serviceHealth.version; config_sha256=$serviceConfigHash; requests=$serviceStats.requests.total }
  }
  $state | ConvertTo-Json -Depth 6 | Set-Content $StatePath -Encoding utf8
  $oldBase = $env:ANTHROPIC_BASE_URL; $env:ANTHROPIC_BASE_URL = "http://127.0.0.1:$Port"
  try { (& headroom doctor --port $Port --json 2>&1 | Out-String | ConvertFrom-Json) | ConvertTo-Json -Depth 8 | Set-Content (Join-Path $evidenceDir 'doctor-start.json') -Encoding utf8 }
  finally { $env:ANTHROPIC_BASE_URL = $oldBase }
  if ($FailAfterStart) { throw 'Injected post-start failure for restore verification' }
  $state | ConvertTo-Json -Depth 6
} catch {
  if ($process -and -not $process.HasExited) { Stop-Process -Id $process.Id -Force; $process.WaitForExit(5000) | Out-Null }
  if (Test-Path $StatePath) {
    $failedState=Get-Content $StatePath -Raw|ConvertFrom-Json; $failedState.active=$false
    $failedState|Add-Member restored $true -Force; $failedState|ConvertTo-Json -Depth 6|Set-Content $StatePath -Encoding utf8
  }
  [ordered]@{ scenario='post-start-error'; restored=$true; error=$_.Exception.Message; at=(Get-Date).ToUniversalTime().ToString('o') } |
    ConvertTo-Json | Set-Content (Join-Path $evidenceDir 'restore-error-test.json') -Encoding utf8
  throw
} finally {
  $env:HEADROOM_COMPRESSION_STABLE_AFTER_TURN=$savedStable; $env:HEADROOM_STALE_READ_COMPRESS_AFTER_TURNS=$savedStale
}
