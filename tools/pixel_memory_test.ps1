# Pixel Memory Test Script for v2.8.4
# Purpose: Verify if v2.8.4 BitmapPool optimization fixes Pixel 9a memory growth issue
#
# v2.8.0 Baseline: Memory Growth +79MB over 3 minutes
# v2.8.4 Target: Memory Growth < 79MB (ideally < 50MB)

param(
    [int]$DurationMinutes = 10,
    [string]$PackageName = "com.akirachang.ai.photographer",
    [string]$OutputDir = "tmp\pixel_memory_test",
    [string]$DeviceSerial = ""
)

# Color output function
function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Color
}

# Setup ADB device selection
if ($DeviceSerial) {
    $env:ANDROID_SERIAL = $DeviceSerial
}

# Create output directory
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir | Out-Null
}

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logFile = "$OutputDir\pixel_memory_${timestamp}.log"
$csvFile = "$OutputDir\pixel_memory_${timestamp}.csv"

Write-ColorOutput "=====================================" "Cyan"
Write-ColorOutput "Pixel Memory Test - v2.8.4" "Cyan"
Write-ColorOutput "=====================================" "Cyan"
Write-ColorOutput ""

# Check device connection
Write-ColorOutput "[1/6] Checking device connection..." "Yellow"
$devices = adb devices | Select-String "device$"
if ($devices.Count -eq 0) {
    Write-ColorOutput "❌ No device connected!" "Red"
    exit 1
}
Write-ColorOutput "✅ Device connected" "Green"
Write-ColorOutput ""

# Get device info
Write-ColorOutput "[2/6] Getting device information..." "Yellow"
$model = adb shell getprop ro.product.model
$android = adb shell getprop ro.build.version.release
$sdk = adb shell getprop ro.build.version.sdk

Write-ColorOutput "Device Model: $model" "Cyan"
Write-ColorOutput "Android Version: $android (SDK $sdk)" "Cyan"
Write-ColorOutput ""

# Check if app is installed
Write-ColorOutput "[3/6] Checking app installation..." "Yellow"
$appInfo = adb shell pm list packages | Select-String $PackageName
if (-not $appInfo) {
    Write-ColorOutput "❌ App not installed!" "Red"
    Write-ColorOutput "Please install: adb install -r app/build/outputs/apk/device/release/app-device-release.apk" "Yellow"
    exit 1
}
Write-ColorOutput "✅ App installed: $PackageName" "Green"
Write-ColorOutput ""

# Clear logcat
Write-ColorOutput "[4/6] Clearing logcat..." "Yellow"
adb logcat -c
Write-ColorOutput "✅ Logcat cleared" "Green"
Write-ColorOutput ""

# Start memory monitoring
Write-ColorOutput "[5/6] Starting memory test..." "Yellow"
Write-ColorOutput "Duration: $DurationMinutes minutes" "Cyan"
Write-ColorOutput "Sampling: Every 30 seconds" "Cyan"
Write-ColorOutput ""
Write-ColorOutput "⚠️  IMPORTANT: Please ensure good lighting conditions (desk lamp ON)" "Yellow"
Write-ColorOutput "⚠️  Keep the device stable and don't interact with it" "Yellow"
Write-ColorOutput ""
Write-ColorOutput "Press Enter to start the test..." "Yellow"
Read-Host

# Initialize CSV
"Timestamp,Elapsed(s),Native(MB),Java(MB),Total(MB),Growth(MB)" | Out-File -FilePath $csvFile -Encoding UTF8

# Get PID
$appPid = adb shell pidof $PackageName
if (-not $appPid) {
    Write-ColorOutput "❌ App not running! Please start the app manually." "Red"
    exit 1
}

Write-ColorOutput "✅ App PID: $appPid" "Green"
Write-ColorOutput ""

# Baseline memory
Write-ColorOutput "Getting baseline memory..." "Yellow"
$memInfo = adb shell dumpsys meminfo $PackageName | Out-String
$baseline = [regex]::Match($memInfo, "TOTAL PSS:\s+(\d+)").Groups[1].Value
$baselineNative = [regex]::Match($memInfo, "Native Heap:\s+(\d+)").Groups[1].Value
$baselineJava = [regex]::Match($memInfo, "Java Heap:\s+(\d+)").Groups[1].Value

if (-not $baseline) {
    Write-ColorOutput "❌ Failed to get memory info!" "Red"
    exit 1
}

$baselineMB = [math]::Round($baseline / 1024, 2)
$baselineNativeMB = [math]::Round($baselineNative / 1024, 2)
$baselineJavaMB = [math]::Round($baselineJava / 1024, 2)

Write-ColorOutput "Baseline Memory:" "Cyan"
Write-ColorOutput "  Native Heap: $baselineNativeMB MB" "White"
Write-ColorOutput "  Java Heap: $baselineJavaMB MB" "White"
Write-ColorOutput "  Total PSS: $baselineMB MB" "White"
Write-ColorOutput ""

# Log header
"=====================================" | Out-File -FilePath $logFile -Encoding UTF8
"Pixel Memory Test - v2.8.4" | Out-File -FilePath $logFile -Append -Encoding UTF8
"=====================================" | Out-File -FilePath $logFile -Append -Encoding UTF8
"" | Out-File -FilePath $logFile -Append -Encoding UTF8
"Device: $model" | Out-File -FilePath $logFile -Append -Encoding UTF8
"Android: $android (SDK $sdk)" | Out-File -FilePath $logFile -Append -Encoding UTF8
"Duration: $DurationMinutes minutes" | Out-File -FilePath $logFile -Append -Encoding UTF8
"Baseline Memory: $baselineMB MB" | Out-File -FilePath $logFile -Append -Encoding UTF8
"" | Out-File -FilePath $logFile -Append -Encoding UTF8

$startTime = Get-Date
$samples = 0
$maxMemory = $baselineMB
$minMemory = $baselineMB

Write-ColorOutput "Starting memory monitoring..." "Cyan"
Write-ColorOutput "Time  | Native  | Java   | Total  | Growth | Status" "White"
Write-ColorOutput "------|---------|--------|--------|--------|--------" "White"

for ($i = 0; $i -lt ($DurationMinutes * 2); $i++) {
    Start-Sleep -Seconds 30

    $elapsed = ((Get-Date) - $startTime).TotalSeconds
    $elapsedMin = [math]::Round($elapsed / 60, 1)

    # Check if app is still running
    $currentPid = adb shell pidof $PackageName
    if (-not $currentPid) {
        Write-ColorOutput "❌ App crashed or stopped!" "Red"
        break
    }

    # Get current memory
    $memInfo = adb shell dumpsys meminfo $PackageName | Out-String
    $current = [regex]::Match($memInfo, "TOTAL PSS:\s+(\d+)").Groups[1].Value
    $currentNative = [regex]::Match($memInfo, "Native Heap:\s+(\d+)").Groups[1].Value
    $currentJava = [regex]::Match($memInfo, "Java Heap:\s+(\d+)").Groups[1].Value

    if (-not $current) {
        Write-ColorOutput "⚠️  Failed to get memory info at $elapsedMin min" "Yellow"
        continue
    }

    $currentMB = [math]::Round($current / 1024, 2)
    $currentNativeMB = [math]::Round($currentNative / 1024, 2)
    $currentJavaMB = [math]::Round($currentJava / 1024, 2)
    $growthMB = [math]::Round($currentMB - $baselineMB, 2)

    $samples++

    if ($currentMB -gt $maxMemory) { $maxMemory = $currentMB }
    if ($currentMB -lt $minMemory) { $minMemory = $currentMB }

    # CSV output
    $csvLine = "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss'),$elapsed,$currentNativeMB,$currentJavaMB,$currentMB,$growthMB"
    $csvLine | Out-File -FilePath $csvFile -Append -Encoding UTF8

    # Console output
    $growthColor = "Green"
    $status = "✅ OK"

    if ($growthMB -gt 50) {
        $growthColor = "Yellow"
        $status = "⚠️  WARN"
    }
    if ($growthMB -gt 79) {
        $growthColor = "Red"
        $status = "❌ HIGH"
    }

    $timeStr = "$elapsedMin min".PadRight(5)
    $nativeStr = "$currentNativeMB MB".PadRight(8)
    $javaStr = "$currentJavaMB MB".PadRight(7)
    $totalStr = "$currentMB MB".PadRight(7)
    $growthStr = "+$growthMB MB".PadRight(7)

    Write-Host "$timeStr| $nativeStr| $javaStr| $totalStr| " -NoNewline
    Write-Host $growthStr -ForegroundColor $growthColor -NoNewline
    Write-Host "| $status"

    # Log output
    "$timeStr| $nativeStr| $javaStr| $totalStr| $growthStr| $status" | Out-File -FilePath $logFile -Append -Encoding UTF8
}

Write-ColorOutput "" "White"
Write-ColorOutput "[6/6] Test completed!" "Green"
Write-ColorOutput "" "White"

# Final analysis
$finalElapsed = ((Get-Date) - $startTime).TotalSeconds
$finalElapsedMin = [math]::Round($finalElapsed / 60, 1)

# Get final memory
$memInfo = adb shell dumpsys meminfo $PackageName | Out-String
$final = [regex]::Match($memInfo, "TOTAL PSS:\s+(\d+)").Groups[1].Value
$finalNative = [regex]::Match($memInfo, "Native Heap:\s+(\d+)").Groups[1].Value
$finalJava = [regex]::Match($memInfo, "Java Heap:\s+(\d+)").Groups[1].Value

$finalMB = [math]::Round($final / 1024, 2)
$finalNativeMB = [math]::Round($finalNative / 1024, 2)
$finalJavaMB = [math]::Round($finalJava / 1024, 2)
$totalGrowthMB = [math]::Round($finalMB - $baselineMB, 2)

Write-ColorOutput "=====================================" "Cyan"
Write-ColorOutput "Test Summary" "Cyan"
Write-ColorOutput "=====================================" "Cyan"
Write-ColorOutput "Duration: $finalElapsedMin minutes" "White"
Write-ColorOutput "Samples: $samples" "White"
Write-ColorOutput "" "White"

Write-ColorOutput "Memory Statistics:" "Cyan"
Write-ColorOutput "  Baseline: $baselineMB MB" "White"
Write-ColorOutput "  Final: $finalMB MB" "White"
Write-ColorOutput "  Max: $maxMemory MB" "White"
Write-ColorOutput "  Min: $minMemory MB" "White"
Write-ColorOutput "  Growth: +$totalGrowthMB MB" "White"
Write-ColorOutput "" "White"

Write-ColorOutput "Component Breakdown:" "Cyan"
Write-ColorOutput "  Native Heap: $baselineNativeMB MB → $finalNativeMB MB (+$([math]::Round($finalNativeMB - $baselineNativeMB, 2)) MB)" "White"
Write-ColorOutput "  Java Heap: $baselineJavaMB MB → $finalJavaMB MB (+$([math]::Round($finalJavaMB - $baselineJavaMB, 2)) MB)" "White"
Write-ColorOutput "" "White"

# Compare with v2.8.0 baseline
Write-ColorOutput "Comparison with v2.8.0:" "Cyan"
$v280Growth = 79  # MB (Pixel 9a, 3 minutes)
$v280Duration = 3  # minutes

# Normalize to 3 minutes for comparison
$normalizedGrowth = if ($finalElapsedMin -gt 0) {
    [math]::Round(($totalGrowthMB / $finalElapsedMin) * 3, 2)
} else {
    0
}

Write-ColorOutput "  v2.8.0 (3 min): +$v280Growth MB" "White"
Write-ColorOutput "  v2.8.4 (3 min normalized): +$normalizedGrowth MB" "White"

$improvement = $v280Growth - $normalizedGrowth
$improvementPct = if ($v280Growth -gt 0) {
    [math]::Round(($improvement / $v280Growth) * 100, 1)
} else {
    0
}

if ($improvement -gt 0) {
    Write-ColorOutput "  Improvement: -$improvement MB (-$improvementPct%)" "Green"
} elseif ($improvement -lt 0) {
    Write-ColorOutput "  Regression: +$([math]::Abs($improvement)) MB (+$([math]::Abs($improvementPct))%)" "Red"
} else {
    Write-ColorOutput "  No change" "Yellow"
}
Write-ColorOutput "" "White"

# Verdict
Write-ColorOutput "Verdict:" "Cyan"

$growthPerMin = if ($finalElapsedMin -gt 0) {
    [math]::Round($totalGrowthMB / $finalElapsedMin, 2)
} else {
    0
}

Write-ColorOutput "  Growth rate: +$growthPerMin MB/min" "White"

if ($totalGrowthMB -lt 50) {
    Write-ColorOutput "  ✅ EXCELLENT: Memory growth well under control" "Green"
    $verdict = "PASS"
} elseif ($totalGrowthMB -lt 79) {
    Write-ColorOutput "  ✅ GOOD: Memory growth improved vs v2.8.0" "Green"
    $verdict = "PASS"
} elseif ($totalGrowthMB -lt 100) {
    Write-ColorOutput "  ⚠️  ACCEPTABLE: Memory growth similar to v2.8.0" "Yellow"
    $verdict = "ACCEPTABLE"
} else {
    Write-ColorOutput "  ❌ CONCERN: Memory growth worse than v2.8.0" "Red"
    $verdict = "FAIL"
}
Write-ColorOutput "" "White"

# BitmapPool check
Write-ColorOutput "Checking BitmapPool statistics..." "Cyan"
$bitmapPoolLogs = adb logcat -d | Select-String "BitmapPool|Pool Keys|Hit Rate"

if ($bitmapPoolLogs) {
    $lastPoolLog = $bitmapPoolLogs | Select-Object -Last 1
    Write-ColorOutput "  Last BitmapPool log:" "White"
    Write-ColorOutput "  $lastPoolLog" "Gray"

    # Extract Pool Keys and Hit Rate if available
    if ($lastPoolLog -match "Pool Keys:\s*(\d+)") {
        $poolKeys = $matches[1]
        Write-ColorOutput "  Pool Keys: $poolKeys" "White"

        if ($poolKeys -eq "1") {
            Write-ColorOutput "  ✅ Pool Keys = 1 (Optimal)" "Green"
        } else {
            Write-ColorOutput "  ⚠️  Pool Keys = $poolKeys (Expected: 1)" "Yellow"
        }
    }

    if ($lastPoolLog -match "Hit Rate:\s*(\d+)%") {
        $hitRate = $matches[1]
        Write-ColorOutput "  Hit Rate: $hitRate%" "White"

        if ([int]$hitRate -ge 99) {
            Write-ColorOutput "  ✅ Hit Rate ≥ 99% (Excellent)" "Green"
        } elseif ([int]$hitRate -ge 95) {
            Write-ColorOutput "  ⚠️  Hit Rate $hitRate% (Good but below 99%)" "Yellow"
        } else {
            Write-ColorOutput "  ❌ Hit Rate $hitRate% (Below target)" "Red"
        }
    }
} else {
    Write-ColorOutput "  ⚠️  No BitmapPool logs found" "Yellow"
}
Write-ColorOutput "" "White"

# Save logcat
$logcatFile = "$OutputDir\pixel_memory_${timestamp}.logcat"
Write-ColorOutput "Saving logcat to $logcatFile..." "Yellow"
adb logcat -d > $logcatFile
Write-ColorOutput "✅ Logcat saved" "Green"
Write-ColorOutput "" "White"

# Output files
Write-ColorOutput "Output files:" "Cyan"
Write-ColorOutput "  Log: $logFile" "White"
Write-ColorOutput "  CSV: $csvFile" "White"
Write-ColorOutput "  Logcat: $logcatFile" "White"
Write-ColorOutput "" "White"

# Final verdict summary
"" | Out-File -FilePath $logFile -Append -Encoding UTF8
"=====================================" | Out-File -FilePath $logFile -Append -Encoding UTF8
"FINAL VERDICT: $verdict" | Out-File -FilePath $logFile -Append -Encoding UTF8
"=====================================" | Out-File -FilePath $logFile -Append -Encoding UTF8
"Total Growth: +$totalGrowthMB MB" | Out-File -FilePath $logFile -Append -Encoding UTF8
"Growth Rate: +$growthPerMin MB/min" | Out-File -FilePath $logFile -Append -Encoding UTF8
"vs v2.8.0: $improvement MB ($improvementPct%)" | Out-File -FilePath $logFile -Append -Encoding UTF8

Write-ColorOutput "Test completed successfully!" "Green"
Write-ColorOutput "" "White"

# Recommendation
if ($verdict -eq "PASS") {
    Write-ColorOutput "✅ RECOMMENDATION: v2.8.4 memory optimization is effective on Pixel" "Green"
    Write-ColorOutput "   Safe to proceed with v3.0 UI/UX development" "Green"
} elseif ($verdict -eq "ACCEPTABLE") {
    Write-ColorOutput "⚠️  RECOMMENDATION: v2.8.4 shows marginal improvement" "Yellow"
    Write-ColorOutput "   Monitor memory during v3.0 development" "Yellow"
} else {
    Write-ColorOutput "❌ RECOMMENDATION: Investigate v2.8.4 memory issue on Pixel" "Red"
    Write-ColorOutput "   Consider delaying v3.0 until memory is optimized" "Red"
}
