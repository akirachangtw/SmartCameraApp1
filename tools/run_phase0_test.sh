#!/bin/bash

# Phase 0 A/B Test Runner
# Usage: bash tools/run_phase0_test.sh [baseline|testA|testB|testC]

TEST_NAME=$1
OUTPUT_DIR="tmp"
PACKAGE="com.akirachang.ai.photographer"

if [ -z "$TEST_NAME" ]; then
    echo "Usage: bash tools/run_phase0_test.sh [baseline|testA|testB|testC]"
    echo ""
    echo "Tests:"
    echo "  baseline  - Current behavior (all flags false)"
    echo "  testA     - No bitmap preview (DEBUG_DISABLE_BITMAP_PREVIEW=true)"
    echo "  testB     - No overlays (DEBUG_DISABLE_OVERLAYS=true)"
    echo "  testC     - No analysis (DEBUG_DISABLE_ANALYSIS=true)"
    exit 1
fi

echo "========================================="
echo "Phase 0 A/B Test: $TEST_NAME"
echo "========================================="

# Check device connection
echo ""
echo "[1/7] Checking device connection..."
if ! adb devices | grep -q "device$"; then
    echo "❌ No device connected!"
    exit 1
fi
echo "✅ Device connected"

# Clear app data
echo ""
echo "[2/7] Clearing app data..."
adb shell pm clear $PACKAGE
echo "✅ App data cleared"

# Clear logcat
echo ""
echo "[3/7] Clearing logcat..."
adb logcat -c
echo "✅ Logcat cleared"

# Create output directory
mkdir -p $OUTPUT_DIR

# Start memory monitoring in background
echo ""
echo "[4/7] Starting memory monitoring (10 minutes, 30s interval)..."
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
MEMINFO_FILE="$OUTPUT_DIR/${TEST_NAME}_meminfo_${TIMESTAMP}.txt"

{
    echo "=== Phase 0 A/B Test: $TEST_NAME ==="
    echo "Test Start: $(date '+%Y-%m-%d %H:%M:%S')"
    echo "Sampling Interval: 30 seconds"
    echo ""

    for i in {1..20}; do
        CURRENT_TIME=$(date +%H:%M:%S)
        echo "--- Sample #$i at $CURRENT_TIME ---"
        adb shell dumpsys meminfo $PACKAGE | grep -E "TOTAL PSS:|Native Heap:|Dalvik Heap:|\.so mmap:|\.dex mmap:|\.oat mmap:|Graphics:|Private Other:|System:|TOTAL SWAP PSS:"
        echo ""

        if [ $i -lt 20 ]; then
            sleep 30
        fi
    done

    echo "Test End: $(date '+%Y-%m-%d %H:%M:%S')"
} > "$MEMINFO_FILE" &

MONITOR_PID=$!
echo "✅ Memory monitoring started (PID: $MONITOR_PID)"
echo "   Output: $MEMINFO_FILE"

# Launch app
echo ""
echo "[5/7] Launching app..."
echo "⚠️  IMPORTANT: Keep app on camera screen for 10 minutes!"
echo "⚠️  Do NOT navigate away or lock the screen!"
adb shell am start -n $PACKAGE/.MainActivity
echo "✅ App launched"

# Wait for monitoring to complete
echo ""
echo "[6/7] Waiting for test completion (10 minutes)..."
echo "Progress:"
for i in {1..20}; do
    sleep 30
    echo "  Sample $i/20 completed ($(($i * 5))%)"
done

# Wait for monitor process to finish
wait $MONITOR_PID

# Collect logcat
echo ""
echo "[7/7] Collecting logcat..."
LOGCAT_FILE="$OUTPUT_DIR/${TEST_NAME}_logcat_${TIMESTAMP}.txt"
adb logcat -d > "$LOGCAT_FILE"
echo "✅ Logcat saved: $LOGCAT_FILE"

# Quick analysis
echo ""
echo "========================================="
echo "Test Complete: $TEST_NAME"
echo "========================================="
echo ""
echo "📊 Quick Analysis:"
echo ""

# Extract Graphics memory
echo "Graphics Memory Timeline:"
grep "Graphics:" "$MEMINFO_FILE" | head -n 5
echo "..."
grep "Graphics:" "$MEMINFO_FILE" | tail -n 5

echo ""
echo "Bitmap Churn Logs:"
grep "BitmapChurn" "$LOGCAT_FILE" | tail -n 3

echo ""
echo "SurfaceTexture Leaks:"
grep "SurfaceTexture.*Leaked" "$LOGCAT_FILE" | tail -n 3

echo ""
echo "📁 Output Files:"
echo "  - $MEMINFO_FILE"
echo "  - $LOGCAT_FILE"
echo ""
echo "Next steps:"
echo "  1. Review output files"
echo "  2. Fill in tmp/phase0_ab_test_results_template.md"
echo "  3. Run next test if needed"
echo ""
