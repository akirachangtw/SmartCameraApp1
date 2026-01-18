#!/bin/bash
# Pixel Memory Test Script for v2.8.4
# Purpose: Verify if v2.8.4 BitmapPool optimization fixes Pixel 9a memory growth issue

DURATION_MINUTES=10
PACKAGE_NAME="com.akirachang.ai.photographer"
OUTPUT_DIR="tmp/pixel_memory_test"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# Create output directory
mkdir -p "$OUTPUT_DIR"

LOG_FILE="$OUTPUT_DIR/pixel_memory_${TIMESTAMP}.log"
CSV_FILE="$OUTPUT_DIR/pixel_memory_${TIMESTAMP}.csv"
LOGCAT_FILE="$OUTPUT_DIR/pixel_memory_${TIMESTAMP}.logcat"

# Color output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${CYAN}=====================================${NC}"
echo -e "${CYAN}Pixel Memory Test - v2.8.4${NC}"
echo -e "${CYAN}=====================================${NC}"
echo ""

# Check device connection
echo -e "${YELLOW}[1/6] Checking device connection...${NC}"
DEVICES=$(adb devices | grep "device$" | wc -l)
if [ $DEVICES -eq 0 ]; then
    echo -e "${RED}❌ No device connected!${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Device connected${NC}"
echo ""

# Get device info
echo -e "${YELLOW}[2/6] Getting device information...${NC}"
MODEL=$(adb shell getprop ro.product.model)
ANDROID=$(adb shell getprop ro.build.version.release)
SDK=$(adb shell getprop ro.build.version.sdk)

echo -e "${CYAN}Device Model: $MODEL${NC}"
echo -e "${CYAN}Android Version: $ANDROID (SDK $SDK)${NC}"
echo ""

# Check if app is installed
echo -e "${YELLOW}[3/6] Checking app installation...${NC}"
APP_INSTALLED=$(adb shell pm list packages | grep "$PACKAGE_NAME")
if [ -z "$APP_INSTALLED" ]; then
    echo -e "${RED}❌ App not installed!${NC}"
    exit 1
fi
echo -e "${GREEN}✅ App installed: $PACKAGE_NAME${NC}"
echo ""

# Clear logcat
echo -e "${YELLOW}[4/6] Clearing logcat...${NC}"
adb logcat -c
echo -e "${GREEN}✅ Logcat cleared${NC}"
echo ""

# Start memory monitoring
echo -e "${YELLOW}[5/6] Starting memory test...${NC}"
echo -e "${CYAN}Duration: $DURATION_MINUTES minutes${NC}"
echo -e "${CYAN}Sampling: Every 30 seconds${NC}"
echo ""
echo -e "${YELLOW}⚠️  IMPORTANT: Please ensure good lighting conditions (desk lamp ON)${NC}"
echo -e "${YELLOW}⚠️  Keep the device stable and don't interact with it${NC}"
echo ""

# Get PID
PID=$(adb shell pidof $PACKAGE_NAME)
if [ -z "$PID" ]; then
    echo -e "${RED}❌ App not running! Please start the app manually.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ App PID: $PID${NC}"
echo ""

# Get baseline memory
echo -e "${YELLOW}Getting baseline memory...${NC}"
MEMINFO=$(adb shell dumpsys meminfo $PACKAGE_NAME)
BASELINE=$(echo "$MEMINFO" | grep "TOTAL PSS:" | awk '{print $3}')
BASELINE_NATIVE=$(echo "$MEMINFO" | grep "Native Heap:" | head -1 | awk '{print $3}')
BASELINE_JAVA=$(echo "$MEMINFO" | grep "Java Heap:" | head -1 | awk '{print $3}')

if [ -z "$BASELINE" ]; then
    echo -e "${RED}❌ Failed to get memory info!${NC}"
    exit 1
fi

BASELINE_MB=$(echo "scale=2; $BASELINE / 1024" | bc)
BASELINE_NATIVE_MB=$(echo "scale=2; $BASELINE_NATIVE / 1024" | bc)
BASELINE_JAVA_MB=$(echo "scale=2; $BASELINE_JAVA / 1024" | bc)

echo -e "${CYAN}Baseline Memory:${NC}"
echo -e "  Native Heap: ${BASELINE_NATIVE_MB} MB"
echo -e "  Java Heap: ${BASELINE_JAVA_MB} MB"
echo -e "  Total PSS: ${BASELINE_MB} MB"
echo ""

# Initialize CSV
echo "Timestamp,Elapsed(s),Native(MB),Java(MB),Total(MB),Growth(MB)" > "$CSV_FILE"

# Log header
{
    echo "====================================="
    echo "Pixel Memory Test - v2.8.4"
    echo "====================================="
    echo ""
    echo "Device: $MODEL"
    echo "Android: $ANDROID (SDK $SDK)"
    echo "Duration: $DURATION_MINUTES minutes"
    echo "Baseline Memory: ${BASELINE_MB} MB"
    echo ""
} > "$LOG_FILE"

START_TIME=$(date +%s)
SAMPLES=0
MAX_MEMORY=$BASELINE_MB
MIN_MEMORY=$BASELINE_MB

echo -e "${CYAN}Starting memory monitoring...${NC}"
echo "Time  | Native  | Java   | Total  | Growth | Status"
echo "------|---------|--------|--------|--------|--------"

# Monitoring loop
for ((i=0; i<$((DURATION_MINUTES * 2)); i++)); do
    sleep 30

    CURRENT_TIME=$(date +%s)
    ELAPSED=$((CURRENT_TIME - START_TIME))
    ELAPSED_MIN=$(echo "scale=1; $ELAPSED / 60" | bc)

    # Check if app is still running
    CURRENT_PID=$(adb shell pidof $PACKAGE_NAME)
    if [ -z "$CURRENT_PID" ]; then
        echo -e "${RED}❌ App crashed or stopped!${NC}"
        break
    fi

    # Get current memory
    MEMINFO=$(adb shell dumpsys meminfo $PACKAGE_NAME)
    CURRENT=$(echo "$MEMINFO" | grep "TOTAL PSS:" | awk '{print $3}')
    CURRENT_NATIVE=$(echo "$MEMINFO" | grep "Native Heap:" | head -1 | awk '{print $3}')
    CURRENT_JAVA=$(echo "$MEMINFO" | grep "Java Heap:" | head -1 | awk '{print $3}')

    if [ -z "$CURRENT" ]; then
        echo -e "${YELLOW}⚠️  Failed to get memory info at ${ELAPSED_MIN} min${NC}"
        continue
    fi

    CURRENT_MB=$(echo "scale=2; $CURRENT / 1024" | bc)
    CURRENT_NATIVE_MB=$(echo "scale=2; $CURRENT_NATIVE / 1024" | bc)
    CURRENT_JAVA_MB=$(echo "scale=2; $CURRENT_JAVA / 1024" | bc)
    GROWTH_MB=$(echo "scale=2; $CURRENT_MB - $BASELINE_MB" | bc)

    SAMPLES=$((SAMPLES + 1))

    # Update max/min
    if (( $(echo "$CURRENT_MB > $MAX_MEMORY" | bc -l) )); then
        MAX_MEMORY=$CURRENT_MB
    fi
    if (( $(echo "$CURRENT_MB < $MIN_MEMORY" | bc -l) )); then
        MIN_MEMORY=$CURRENT_MB
    fi

    # CSV output
    CSV_LINE="$(date '+%Y-%m-%d %H:%M:%S'),$ELAPSED,$CURRENT_NATIVE_MB,$CURRENT_JAVA_MB,$CURRENT_MB,$GROWTH_MB"
    echo "$CSV_LINE" >> "$CSV_FILE"

    # Console output with color
    STATUS="✅ OK"
    COLOR=$NC

    if (( $(echo "$GROWTH_MB > 50" | bc -l) )); then
        STATUS="⚠️  WARN"
        COLOR=$YELLOW
    fi
    if (( $(echo "$GROWTH_MB > 79" | bc -l) )); then
        STATUS="❌ HIGH"
        COLOR=$RED
    fi

    printf "%-5s | %-8s| %-7s| %-7s| " "${ELAPSED_MIN}m" "${CURRENT_NATIVE_MB} MB" "${CURRENT_JAVA_MB} MB" "${CURRENT_MB} MB"
    echo -e "${COLOR}+${GROWTH_MB} MB${NC} | $STATUS"

    # Log output
    echo "${ELAPSED_MIN}m | ${CURRENT_NATIVE_MB} MB | ${CURRENT_JAVA_MB} MB | ${CURRENT_MB} MB | +${GROWTH_MB} MB | $STATUS" >> "$LOG_FILE"
done

echo ""
echo -e "${GREEN}[6/6] Test completed!${NC}"
echo ""

# Final analysis
FINAL_TIME=$(date +%s)
FINAL_ELAPSED=$((FINAL_TIME - START_TIME))
FINAL_ELAPSED_MIN=$(echo "scale=1; $FINAL_ELAPSED / 60" | bc)

# Get final memory
MEMINFO=$(adb shell dumpsys meminfo $PACKAGE_NAME)
FINAL=$(echo "$MEMINFO" | grep "TOTAL PSS:" | awk '{print $3}')
FINAL_NATIVE=$(echo "$MEMINFO" | grep "Native Heap:" | head -1 | awk '{print $3}')
FINAL_JAVA=$(echo "$MEMINFO" | grep "Java Heap:" | head -1 | awk '{print $3}')

FINAL_MB=$(echo "scale=2; $FINAL / 1024" | bc)
FINAL_NATIVE_MB=$(echo "scale=2; $FINAL_NATIVE / 1024" | bc)
FINAL_JAVA_MB=$(echo "scale=2; $FINAL_JAVA / 1024" | bc)
TOTAL_GROWTH_MB=$(echo "scale=2; $FINAL_MB - $BASELINE_MB" | bc)

echo -e "${CYAN}=====================================${NC}"
echo -e "${CYAN}Test Summary${NC}"
echo -e "${CYAN}=====================================${NC}"
echo "Duration: ${FINAL_ELAPSED_MIN} minutes"
echo "Samples: $SAMPLES"
echo ""

echo -e "${CYAN}Memory Statistics:${NC}"
echo "  Baseline: ${BASELINE_MB} MB"
echo "  Final: ${FINAL_MB} MB"
echo "  Max: ${MAX_MEMORY} MB"
echo "  Min: ${MIN_MEMORY} MB"
echo "  Growth: +${TOTAL_GROWTH_MB} MB"
echo ""

# Component breakdown
NATIVE_GROWTH=$(echo "scale=2; $FINAL_NATIVE_MB - $BASELINE_NATIVE_MB" | bc)
JAVA_GROWTH=$(echo "scale=2; $FINAL_JAVA_MB - $BASELINE_JAVA_MB" | bc)

echo -e "${CYAN}Component Breakdown:${NC}"
echo "  Native Heap: ${BASELINE_NATIVE_MB} MB → ${FINAL_NATIVE_MB} MB (+${NATIVE_GROWTH} MB)"
echo "  Java Heap: ${BASELINE_JAVA_MB} MB → ${FINAL_JAVA_MB} MB (+${JAVA_GROWTH} MB)"
echo ""

# Compare with v2.8.0
echo -e "${CYAN}Comparison with v2.8.0:${NC}"
V280_GROWTH=79
NORMALIZED_GROWTH=$(echo "scale=2; ($TOTAL_GROWTH_MB / $FINAL_ELAPSED_MIN) * 3" | bc)
echo "  v2.8.0 (3 min): +${V280_GROWTH} MB"
echo "  v2.8.4 (3 min normalized): +${NORMALIZED_GROWTH} MB"

IMPROVEMENT=$(echo "scale=2; $V280_GROWTH - $NORMALIZED_GROWTH" | bc)
IMPROVEMENT_PCT=$(echo "scale=1; ($IMPROVEMENT / $V280_GROWTH) * 100" | bc)

if (( $(echo "$IMPROVEMENT > 0" | bc -l) )); then
    echo -e "  ${GREEN}Improvement: -${IMPROVEMENT} MB (-${IMPROVEMENT_PCT}%)${NC}"
elif (( $(echo "$IMPROVEMENT < 0" | bc -l) )); then
    ABS_IMPROVEMENT=$(echo "$IMPROVEMENT * -1" | bc)
    ABS_PCT=$(echo "$IMPROVEMENT_PCT * -1" | bc)
    echo -e "  ${RED}Regression: +${ABS_IMPROVEMENT} MB (+${ABS_PCT}%)${NC}"
else
    echo -e "  ${YELLOW}No change${NC}"
fi
echo ""

# Verdict
echo -e "${CYAN}Verdict:${NC}"
GROWTH_PER_MIN=$(echo "scale=2; $TOTAL_GROWTH_MB / $FINAL_ELAPSED_MIN" | bc)
echo "  Growth rate: +${GROWTH_PER_MIN} MB/min"

if (( $(echo "$TOTAL_GROWTH_MB < 50" | bc -l) )); then
    echo -e "  ${GREEN}✅ EXCELLENT: Memory growth well under control${NC}"
    VERDICT="PASS"
elif (( $(echo "$TOTAL_GROWTH_MB < 79" | bc -l) )); then
    echo -e "  ${GREEN}✅ GOOD: Memory growth improved vs v2.8.0${NC}"
    VERDICT="PASS"
elif (( $(echo "$TOTAL_GROWTH_MB < 100" | bc -l) )); then
    echo -e "  ${YELLOW}⚠️  ACCEPTABLE: Memory growth similar to v2.8.0${NC}"
    VERDICT="ACCEPTABLE"
else
    echo -e "  ${RED}❌ CONCERN: Memory growth worse than v2.8.0${NC}"
    VERDICT="FAIL"
fi
echo ""

# Save logcat
echo -e "${YELLOW}Saving logcat to $LOGCAT_FILE...${NC}"
adb logcat -d > "$LOGCAT_FILE"
echo -e "${GREEN}✅ Logcat saved${NC}"
echo ""

# Output files
echo -e "${CYAN}Output files:${NC}"
echo "  Log: $LOG_FILE"
echo "  CSV: $CSV_FILE"
echo "  Logcat: $LOGCAT_FILE"
echo ""

# Save verdict to log
{
    echo ""
    echo "====================================="
    echo "FINAL VERDICT: $VERDICT"
    echo "====================================="
    echo "Total Growth: +${TOTAL_GROWTH_MB} MB"
    echo "Growth Rate: +${GROWTH_PER_MIN} MB/min"
    echo "vs v2.8.0: ${IMPROVEMENT} MB (${IMPROVEMENT_PCT}%)"
} >> "$LOG_FILE"

echo -e "${GREEN}Test completed successfully!${NC}"
echo ""

# Recommendation
if [ "$VERDICT" = "PASS" ]; then
    echo -e "${GREEN}✅ RECOMMENDATION: v2.8.4 memory optimization is effective on Pixel${NC}"
    echo -e "${GREEN}   Safe to proceed with v3.0 UI/UX development${NC}"
elif [ "$VERDICT" = "ACCEPTABLE" ]; then
    echo -e "${YELLOW}⚠️  RECOMMENDATION: v2.8.4 shows marginal improvement${NC}"
    echo -e "${YELLOW}   Monitor memory during v3.0 development${NC}"
else
    echo -e "${RED}❌ RECOMMENDATION: Investigate v2.8.4 memory issue on Pixel${NC}"
    echo -e "${RED}   Consider delaying v3.0 until memory is optimized${NC}"
fi
