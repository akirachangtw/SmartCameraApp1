[CmdletBinding()]
param([string]$StatePath='')
$ErrorActionPreference='Stop'; Set-StrictMode -Version Latest
if (-not $StatePath) { $StatePath=Join-Path $PSScriptRoot '../testing/evidence/canary-state.json' }
$StatePath=[IO.Path]::GetFullPath($StatePath)
$root=(Resolve-Path (Join-Path $PSScriptRoot '..')).Path; $evidenceDir=Split-Path $StatePath -Parent
$state=Get-Content $StatePath -Raw | ConvertFrom-Json
if (-not $state.active -or -not $state.restore_registered) { throw 'Canary is not active with restore registered' }
$scenarios=Get-Content (Join-Path $root 'testing/canary-scenarios.json') -Raw | ConvertFrom-Json
if ($scenarios.Count -ne 20 -or ($scenarios | Group-Object category | Where-Object Count -ne 5)) { throw 'Expected four categories with five scenarios each' }
$oldBase=$env:ANTHROPIC_BASE_URL; $env:ANTHROPIC_BASE_URL="http://127.0.0.1:$($state.port)"
$results=@(); $eligibleSession=[guid]::NewGuid().ToString(); $eligibleStarted=$false
try {
  $doctor=(& headroom doctor --port $state.port --json 2>&1 | Out-String | ConvertFrom-Json)
  $statsBefore=Invoke-RestMethod "http://127.0.0.1:$($state.port)/stats" -TimeoutSec 5
  Push-Location (New-Item -ItemType Directory -Path (Join-Path $env:TEMP 'headroom-canary-public') -Force).FullName
  try {
    foreach ($scenario in $scenarios) {
      $beforeLines=@(Get-Content $state.request_log -ErrorAction SilentlyContinue).Count
      $args=@('--safe-mode','--model','haiku','--add-dir','C:\Python314\Lib','--tools','Bash','Read','Grep','--allowedTools','Bash','Read','Grep','--permission-mode','dontAsk','--prompt-suggestions','false','--max-budget-usd','0.15','--output-format','json','-p')
      if ($scenario.category -eq 'forced_bypass') { $args=@('--session-id',[guid]::NewGuid().ToString())+$args }
      elseif (-not $eligibleStarted) { $args=@('--session-id',$eligibleSession)+$args; $eligibleStarted=$true }
      else { $args=@('--resume',$eligibleSession)+$args }
      $watch=[Diagnostics.Stopwatch]::StartNew(); $raw=& claude @args $scenario.prompt 2>&1; $exit=$LASTEXITCODE; $watch.Stop()
      $reply=$null; try { $reply=$raw | ConvertFrom-Json } catch {}
      $rows=@(Get-Content $state.request_log | Select-Object -Skip $beforeLines | ForEach-Object { $_ | ConvertFrom-Json })
      $original=($rows | Measure-Object input_tokens_original -Sum).Sum; $optimized=($rows | Measure-Object input_tokens_optimized -Sum).Sum
      if ($null -eq $original) {$original=0}; if ($null -eq $optimized) {$optimized=0}
      $saved=$original-$optimized; $savings=if($original){[math]::Round(100*$saved/$original,2)}else{0}
      $optMs=($rows|Measure-Object optimization_latency_ms -Maximum).Maximum; if($null -eq $optMs){$optMs=0}
      $replyText=if($reply){$reply.result}else{$null}; $sessionId=if($reply){$reply.session_id}else{'unavailable'}
      $quality=if($exit -eq 0 -and $replyText -match [regex]::Escape("CANARY_OK $($scenario.id)")){5}else{0}
      $isBypass=$scenario.category -ne 'forced_bypass' -or ($saved -eq 0 -and -not ($rows.transforms_applied -match 'compress'))
      $hashInput="$($scenario.id)|$sessionId|$(($rows.request_id)-join ',')|$quality"
      $sha=[Security.Cryptography.SHA256]::Create(); try{$hash=([BitConverter]::ToString($sha.ComputeHash([Text.Encoding]::UTF8.GetBytes($hashInput)))).Replace('-','')}finally{$sha.Dispose()}
      $responseTokens=($rows|Measure-Object output_tokens -Sum).Sum; if($null-eq$responseTokens){$responseTokens=0}
      $results += [pscustomobject][ordered]@{ scenario_id=$scenario.id; category=$scenario.category; source_kind='real'; session_id=$sessionId; request_ids=@($rows.request_id); input_tokens=$original; optimized_input_tokens=$optimized; output_tokens=$responseTokens; token_savings_rate=$savings/100; overhead_ms=[math]::Round([double]$optMs,2); elapsed_ms=$watch.ElapsedMilliseconds; fallback=($exit -ne 0 -or [bool]($rows|Where-Object error)); quality=$quality; route=if($scenario.category -eq 'forced_bypass'){'bypass'}else{'eligible'}; bypass_reason=$scenario.bypass_reason; bypass_verified=[bool]$isBypass; evidence_sha256=$hash }
      $results|ConvertTo-Json -Depth 6|Set-Content (Join-Path $evidenceDir 'canary-checkpoint.json') -Encoding utf8
    }
  } finally { Pop-Location }
  $statsAfter=Invoke-RestMethod "http://127.0.0.1:$($state.port)/stats" -TimeoutSec 5
} finally { $env:ANTHROPIC_BASE_URL=$oldBase }
$eligible=@($results|Where-Object category -ne 'forced_bypass'); $rates=@($eligible.token_savings_rate|Sort-Object); $median=if($rates.Count%2){$rates[[int]($rates.Count/2)]}else{($rates[$rates.Count/2-1]+$rates[$rates.Count/2])/2}
$qualityAvg=($results|Measure-Object quality -Average).Average; $fallbackRate=@($results|Where-Object fallback).Count/$results.Count
$latencies=@($results.overhead_ms|Sort-Object); $p95=$latencies[[math]::Ceiling(.95*$latencies.Count)-1]
$delta=$statsAfter.requests.total-$statsBefore.requests.total; $bypassOk=-not [bool]($results|Where-Object {$_.category-eq'forced_bypass'-and-not$_.bypass_verified})
$pass=($delta-gt 0-and$results.Count-eq 20-and$bypassOk-and$median-ge .30-and$qualityAvg-ge 4-and$fallbackRate-lt .05-and$p95-le 1000)
$evidence=[ordered]@{ schema_version=1; source_kind='real'; generated_at=(Get-Date).ToUniversalTime().ToString('o'); route=[ordered]@{port=$state.port;doctor_routed=[bool]($doctor.checks|Where-Object{$_.name -eq 'shell env' -and $_.status -eq 'pass'});requests_before=$statsBefore.requests.total;requests_after=$statsAfter.requests.total;request_delta=$delta}; metrics=[ordered]@{qualified_token_savings_median=$median;quality_average=[math]::Round($qualityAvg,2);fallback_rate=$fallbackRate;p95_overhead_ms=$p95;bypass_100_percent=$bypassOk}; promotion_verdict=if($pass){'PASS'}else{'HOLD'}; requests=$results }
$evidence|ConvertTo-Json -Depth 8|Set-Content (Join-Path $evidenceDir 'canary-evidence.json') -Encoding utf8
$evidence|ConvertTo-Json -Depth 5
if(-not $pass){exit 2}
