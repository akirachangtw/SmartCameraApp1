#!/bin/bash
# query_device_camera_info.sh
# 查詢連接裝置的相機硬體資訊（不需安裝 APK）
#
# 使用方法：
#   ./tools/query_device_camera_info.sh
#
# 需求：
#   - adb 已安裝並在 PATH 中
#   - 裝置已連接並授權

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUTPUT_DIR="$SCRIPT_DIR/../tmp/device_info"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# 顏色定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Camera Hardware Info Query Tool${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 檢查 adb
if ! command -v adb &> /dev/null; then
    echo -e "${RED}❌ Error: adb not found in PATH${NC}"
    echo "Please install Android SDK Platform Tools"
    exit 1
fi

# 檢查連接的裝置
echo -e "${YELLOW}📱 Checking connected devices...${NC}"
DEVICES=$(adb devices | grep -v "List" | grep "device$" | wc -l)

if [ "$DEVICES" -eq 0 ]; then
    echo -e "${RED}❌ No devices connected${NC}"
    echo "Please connect a device and enable USB debugging"
    exit 1
elif [ "$DEVICES" -gt 1 ]; then
    echo -e "${YELLOW}⚠️  Multiple devices connected:${NC}"
    adb devices
    echo ""
    echo -e "${YELLOW}Please specify device with: export ANDROID_SERIAL=<device_id>${NC}"
    exit 1
fi

# 獲取裝置資訊
echo -e "${GREEN}✅ Device connected${NC}"
DEVICE_MODEL=$(adb shell getprop ro.product.model | tr -d '\r')
DEVICE_MANUFACTURER=$(adb shell getprop ro.product.manufacturer | tr -d '\r')
DEVICE_SDK=$(adb shell getprop ro.build.version.sdk | tr -d '\r')
DEVICE_ANDROID=$(adb shell getprop ro.build.version.release | tr -d '\r')
DEVICE_SERIAL=$(adb get-serialno | tr -d '\r')

echo ""
echo -e "${BLUE}Device Information:${NC}"
echo -e "  Manufacturer: ${GREEN}${DEVICE_MANUFACTURER}${NC}"
echo -e "  Model: ${GREEN}${DEVICE_MODEL}${NC}"
echo -e "  Android: ${GREEN}${DEVICE_ANDROID}${NC} (API ${DEVICE_SDK})"
echo -e "  Serial: ${GREEN}${DEVICE_SERIAL}${NC}"
echo ""

# 建立輸出目錄
mkdir -p "$OUTPUT_DIR"

OUTPUT_FILE="${OUTPUT_DIR}/${DEVICE_MANUFACTURER}_${DEVICE_MODEL}_${TIMESTAMP}.txt"

# 開始寫入報告
{
    echo "========================================"
    echo "  Camera Hardware Information Report"
    echo "========================================"
    echo ""
    echo "Query Date: $(date)"
    echo "Device Serial: $DEVICE_SERIAL"
    echo "Manufacturer: $DEVICE_MANUFACTURER"
    echo "Model: $DEVICE_MODEL"
    echo "Android Version: $DEVICE_ANDROID (API $DEVICE_SDK)"
    echo ""
    echo "========================================"
    echo "  Camera Characteristics"
    echo "========================================"
    echo ""
} > "$OUTPUT_FILE"

# 推送並執行查詢工具
echo -e "${YELLOW}📸 Querying camera characteristics...${NC}"

# 建立臨時 Java 查詢程式
TEMP_JAVA_FILE="/data/local/tmp/CameraQuery.java"
TEMP_DEX_FILE="/data/local/tmp/CameraQuery.dex"

# 注意：以下方法需要 root 或使用專門的 APK
# 這裡我們使用 dumpsys 來獲取部分資訊

echo -e "${YELLOW}📊 Using dumpsys to gather camera info...${NC}"

{
    echo "--- Camera Service Dump ---"
    echo ""
} >> "$OUTPUT_FILE"

adb shell dumpsys media.camera >> "$OUTPUT_FILE" 2>&1

echo ""
echo -e "${GREEN}✅ Camera info collected${NC}"

# 使用更簡單的方法：讀取系統屬性
echo -e "${YELLOW}🔍 Collecting additional system properties...${NC}"

{
    echo ""
    echo "========================================"
    echo "  System Properties (Camera Related)"
    echo "========================================"
    echo ""
    echo "--- Display ---"
    adb shell getprop | grep "display\|resolution\|density" | sort
    echo ""
    echo "--- Camera ---"
    adb shell getprop | grep -i "camera" | sort
    echo ""
    echo "--- GPU ---"
    adb shell getprop | grep -i "gpu\|opengl" | sort
    echo ""
} >> "$OUTPUT_FILE"

echo -e "${GREEN}✅ System properties collected${NC}"

# 嘗試透過 CTS 測試工具獲取更多資訊（如果有的話）
echo -e "${YELLOW}🔬 Attempting advanced camera query...${NC}"

# 檢查是否已安裝測試 APK
TEST_APK_INSTALLED=$(adb shell pm list packages | grep "com.android.cts.verifier" || echo "")

if [ -n "$TEST_APK_INSTALLED" ]; then
    echo -e "${GREEN}✅ CTS Verifier found, can use for detailed camera info${NC}"
else
    echo -e "${YELLOW}⚠️  CTS Verifier not installed (optional)${NC}"
fi

# 總結報告
{
    echo ""
    echo "========================================"
    echo "  Summary"
    echo "========================================"
    echo ""
    echo "Report generated: $(date)"
    echo "Output file: $OUTPUT_FILE"
    echo ""
    echo "Note: Full camera resolution list requires running"
    echo "      Camera2 API queries via an installed APK."
    echo ""
    echo "Recommendation:"
    echo "  1. Install SmartCameraApp v2.9.0-beta5"
    echo "  2. Run app and check logcat for:"
    echo "     [v2.9.0-beta3] Found XX supported YUV_420_888 resolutions"
    echo "     [v2.9.0-beta3] 640x480, 800x600, ..."
    echo ""
} >> "$OUTPUT_FILE"

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}✅ Report generated successfully!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "📄 Output file: ${GREEN}${OUTPUT_FILE}${NC}"
echo ""
echo -e "${YELLOW}Note:${NC} Full camera resolution list requires Camera2 API queries."
echo -e "      Please install SmartCameraApp and check logcat for detailed info."
echo ""

# 顯示部分內容
echo -e "${BLUE}--- Report Preview (first 50 lines) ---${NC}"
head -50 "$OUTPUT_FILE"
echo ""
echo -e "${YELLOW}(See full report in output file)${NC}"
echo ""

# 提供下一步建議
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Next Steps${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo "To get complete camera resolution information:"
echo ""
echo "1. Install SmartCameraApp:"
echo "   cd $(dirname $SCRIPT_DIR)"
echo "   ./gradlew installDeviceDebug"
echo ""
echo "2. Launch app and capture logcat:"
echo "   adb logcat -c"
echo "   adb shell am start -n com.akirachang.ai.photographer/.MainActivity"
echo "   sleep 10"
echo "   adb logcat -d | grep \"\\[v2.9.0-beta\" > ${OUTPUT_DIR}/camera_resolutions_${TIMESTAMP}.log"
echo ""
echo "3. Check the log file for detailed resolution info"
echo ""
