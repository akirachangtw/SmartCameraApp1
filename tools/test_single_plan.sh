#!/bin/bash
# v2.9.0-beta4 Single Plan Test Script
# Usage: ./tools/test_single_plan.sh <plan_name> <device_serial>
# Example: ./tools/test_single_plan.sh "Plan_A_480x360" HQ616R2713

set -e

PLAN_NAME=$1
DEVICE_SERIAL=$2

if [ -z "$PLAN_NAME" ] || [ -z "$DEVICE_SERIAL" ]; then
    echo "Usage: $0 <plan_name> <device_serial>"
    echo "Example: $0 'Plan_A_480x360' HQ616R2713"
    exit 1
fi

echo "=========================================="
echo "v2.9.0-beta4 Test: $PLAN_NAME"
echo "Device: $DEVICE_SERIAL"
echo "=========================================="
echo ""

# Clear logcat
echo "[1/11] Clearing logcat..."
adb -s $DEVICE_SERIAL logcat -c

# Launch app
echo "[2/11] Launching app..."
adb -s $DEVICE_SERIAL shell am start -n com.akirachang.ai.photographer/.MainActivity
sleep 8

# T0: Collect initial data
echo "[3/11] T0 (0 min) - Collecting initial data..."
echo "--- Resolution ---"
adb -s $DEVICE_SERIAL logcat -d | grep -E "(v2.9.0-beta4 Resolution|Preview frame sent)" | head -5
echo ""
echo "--- Graphics Memory T0 ---"
adb -s $DEVICE_SERIAL shell dumpsys meminfo com.akirachang.ai.photographer | grep Graphics
echo ""
echo "--- FPS T0 ---"
adb -s $DEVICE_SERIAL logcat -d | grep "Performance: FPS=" | tail -2
echo ""
echo "--- Thermal State T0 ---"
adb -s $DEVICE_SERIAL logcat -d | grep "Thermal=" | tail -1
echo ""

# Wait 5 minutes
echo "[4/11] Waiting 5 minutes for T5..."
sleep 300

# T5: Mid-point check
echo "[5/11] T5 (5 min) - Mid-point check..."
echo "--- Graphics Memory T5 ---"
adb -s $DEVICE_SERIAL shell dumpsys meminfo com.akirachang.ai.photographer | grep Graphics
echo ""
echo "--- FPS T5 ---"
adb -s $DEVICE_SERIAL logcat -d | grep "Performance: FPS=" | tail -2
echo ""
echo "--- Thermal State T5 ---"
adb -s $DEVICE_SERIAL logcat -d | grep "Thermal=" | tail -1
echo ""

# Wait 5 minutes
echo "[6/11] Waiting 5 minutes for T10..."
sleep 300

# T10: Final check
echo "[7/11] T10 (10 min) - Final check..."
echo "--- Graphics Memory T10 ---"
adb -s $DEVICE_SERIAL shell dumpsys meminfo com.akirachang.ai.photographer | grep Graphics
echo ""
echo "--- FPS T10 ---"
adb -s $DEVICE_SERIAL logcat -d | grep "Performance: FPS=" | tail -2
echo ""
echo "--- Thermal State T10 ---"
adb -s $DEVICE_SERIAL logcat -d | grep "Thermal=" | tail -1
echo ""
echo "--- BitmapPool Health T10 ---"
adb -s $DEVICE_SERIAL logcat -d | grep "v2.8.4 Pool" | tail -1
echo ""

# Stop app
echo "[8/11] Stopping app..."
adb -s $DEVICE_SERIAL shell am force-stop com.akirachang.ai.photographer

# Save logcat
echo "[9/11] Saving logcat..."
mkdir -p tmp/test_outputs/logcat
adb -s $DEVICE_SERIAL logcat -d > "tmp/test_outputs/logcat/${PLAN_NAME}_$(date +%Y%m%d_%H%M%S).log"

# Summary
echo "[10/11] Test Summary"
echo "===================="
echo "Plan: $PLAN_NAME"
echo "Device: $DEVICE_SERIAL"
echo "Time: $(date '+%Y-%m-%d %H:%M:%S')"
echo ""

# Cooling period
echo "[11/11] Starting 10-minute cooling period..."
echo "Current time: $(date '+%H:%M:%S')"
echo "Resume time: $(date -d '+10 minutes' '+%H:%M:%S' 2>/dev/null || date -v+10M '+%H:%M:%S' 2>/dev/null || echo '10 minutes later')"
sleep 600

echo ""
echo "=========================================="
echo "Test Complete: $PLAN_NAME"
echo "Device ready for next test"
echo "=========================================="
