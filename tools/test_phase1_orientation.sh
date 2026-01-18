#!/bin/bash
# v2.9.6 Phase 1: Orientation Test Automation Script
# Usage: ./test_phase1_orientation.sh <device_id>

DEVICE_ID=${1:-HQ616R2713}
TEST_DIR="tmp/v2.9.6_phase1_testing"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

echo "=========================================="
echo "v2.9.6 Phase 1 Orientation Test"
echo "Device: $DEVICE_ID"
echo "=========================================="

# 1. Create test directory
mkdir -p "$TEST_DIR"

# 2. Check if app is running
echo ""
echo "[1/6] Checking app status..."
PID=$(adb -s "$DEVICE_ID" shell pidof com.akirachang.ai.photographer)
if [ -z "$PID" ]; then
    echo "⚠️  App not running. Starting app..."
    adb -s "$DEVICE_ID" shell am start -n com.akirachang.ai.photographer/com.akirachang.ai.photographer.MainActivity
    sleep 3
else
    echo "✅ App running (PID: $PID)"
fi

# 3. Clear logcat and start monitoring
echo ""
echo "[2/6] Starting logcat monitoring..."
adb -s "$DEVICE_ID" logcat -c
adb -s "$DEVICE_ID" logcat -s LiveCameraSource:* > "$TEST_DIR/logcat_${TIMESTAMP}.txt" &
LOGCAT_PID=$!
echo "✅ Logcat PID: $LOGCAT_PID"

# 4. Wait for user to perform tests
echo ""
echo "[3/6] Please perform the following tests:"
echo "  - TC-001: Back camera, Portrait (0°) - 3 photos"
echo "  - TC-002: Back camera, Landscape Left (90°) - 3 photos"
echo "  - TC-003: Back camera, Landscape Right (270°) - 3 photos"
echo "  - TC-004: Front camera, repeat above - 9 photos"
echo ""
read -p "Press ENTER when all tests are completed..."

# 5. Stop logcat
echo ""
echo "[4/6] Stopping logcat..."
kill $LOGCAT_PID
wait $LOGCAT_PID 2>/dev/null
echo "✅ Logcat saved to: $TEST_DIR/logcat_${TIMESTAMP}.txt"

# 6. Pull photos from device
echo ""
echo "[5/6] Pulling test photos from device..."
DCIM_PATH="/sdcard/DCIM/SmartCamera"

# Get list of most recent photos (last 20)
adb -s "$DEVICE_ID" shell "ls -t $DCIM_PATH/*.jpg | head -20" | while read photo; do
    # Remove carriage return
    photo=$(echo "$photo" | tr -d '\r')
    filename=$(basename "$photo")
    echo "  Pulling: $filename"
    adb -s "$DEVICE_ID" pull "$photo" "$TEST_DIR/" 2>/dev/null
done

echo "✅ Photos saved to: $TEST_DIR/"

# 7. Analyze EXIF data
echo ""
echo "[6/6] Analyzing EXIF Orientation..."
echo "Photo Name                     | Orientation" > "$TEST_DIR/exif_analysis_${TIMESTAMP}.txt"
echo "-------------------------------|-------------" >> "$TEST_DIR/exif_analysis_${TIMESTAMP}.txt"

for photo in "$TEST_DIR"/*.jpg; do
    if [ -f "$photo" ]; then
        filename=$(basename "$photo")
        # Try to get EXIF orientation using Python (if available)
        # Otherwise, manual verification needed
        echo "$filename | (manual check needed)" >> "$TEST_DIR/exif_analysis_${TIMESTAMP}.txt"
    fi
done

cat "$TEST_DIR/exif_analysis_${TIMESTAMP}.txt"

echo ""
echo "=========================================="
echo "✅ Test data collection completed!"
echo "=========================================="
echo "Results:"
echo "  - Logcat: $TEST_DIR/logcat_${TIMESTAMP}.txt"
echo "  - Photos: $TEST_DIR/*.jpg"
echo "  - EXIF:   $TEST_DIR/exif_analysis_${TIMESTAMP}.txt"
echo ""
echo "Next steps:"
echo "  1. Open photos in gallery app to verify orientation"
echo "  2. Check logcat for 'OrientationEventListener enabled'"
echo "  3. Check logcat for 'Orientation changed' messages"
echo "  4. Verify FPS maintained 25-30 (no regression)"
