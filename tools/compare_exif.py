#!/usr/bin/env python3
"""
v2.9.7 Day1-2: EXIF Comparison Tool
Compares EXIF data between Pixel 9a native camera and SmartCameraApp

Usage:
    python compare_exif.py <pixel_photo> <smartcamera_photo>
    python compare_exif.py docs/test_results/v2.9.7-Day1-2/images/PXL_20260129_Pixel9a.jpg docs/test_results/v2.9.7-Day1-2/images/2026-01-29-15-38-36-測試6-室外1.jpg
"""

import sys
from pathlib import Path
try:
    from PIL import Image
    from PIL.ExifTags import TAGS, GPSTAGS
except ImportError:
    print("ERROR: Pillow library not found")
    print("Install: pip install Pillow")
    sys.exit(1)

def get_all_exif(image_path):
    """Extract ALL EXIF data from image"""
    try:
        image = Image.open(image_path)
        exif_data = image._getexif()

        if exif_data is None:
            return None, "No EXIF data"

        # Convert tag IDs to names
        exif_dict = {}
        for tag_id, value in exif_data.items():
            tag_name = TAGS.get(tag_id, tag_id)

            # Special handling for GPS Info
            if tag_name == "GPSInfo":
                gps_dict = {}
                for gps_tag_id, gps_value in value.items():
                    gps_tag_name = GPSTAGS.get(gps_tag_id, gps_tag_id)
                    gps_dict[gps_tag_name] = gps_value
                exif_dict[tag_name] = gps_dict
            else:
                exif_dict[tag_name] = value

        return exif_dict, "OK"

    except Exception as e:
        return None, f"Error: {str(e)}"

def format_value(value, indent=0):
    """Format EXIF value for display"""
    prefix = "  " * indent

    if isinstance(value, dict):
        lines = []
        for k, v in value.items():
            if isinstance(v, dict):
                lines.append(f"{prefix}{k}:")
                lines.append(format_value(v, indent + 1))
            else:
                lines.append(f"{prefix}{k}: {format_value(v, 0)}")
        return "\n".join(lines)
    elif isinstance(value, bytes):
        try:
            decoded = value.decode('utf-8', errors='ignore').strip('\x00')
            if decoded:
                return f"{decoded} (bytes)"
            return f"<binary data: {len(value)} bytes>"
        except:
            return f"<binary data: {len(value)} bytes>"
    elif isinstance(value, tuple):
        return f"({', '.join(str(v) for v in value)})"
    else:
        return str(value)

def compare_exif(pixel_path, smartcam_path):
    """Compare EXIF data between two photos"""

    print("=" * 100)
    print("EXIF Comparison - Pixel 9a Native Camera vs SmartCameraApp")
    print("=" * 100)
    print()

    # Load EXIF from both photos
    print(f"Loading Pixel Camera:     {Path(pixel_path).name}")
    pixel_exif, pixel_status = get_all_exif(pixel_path)
    if pixel_exif is None:
        print(f"ERROR: {pixel_status}")
        sys.exit(1)
    print(f"  ✅ Loaded {len(pixel_exif)} EXIF tags")
    print()

    print(f"Loading SmartCameraApp:   {Path(smartcam_path).name}")
    smartcam_exif, smartcam_status = get_all_exif(smartcam_path)
    if smartcam_exif is None:
        print(f"ERROR: {smartcam_status}")
        sys.exit(1)
    print(f"  ✅ Loaded {len(smartcam_exif)} EXIF tags")
    print()

    # Get all unique keys
    all_keys = sorted(set(list(pixel_exif.keys()) + list(smartcam_exif.keys())))

    # Categorize keys
    common_keys = [k for k in all_keys if k in pixel_exif and k in smartcam_exif]
    pixel_only = [k for k in all_keys if k in pixel_exif and k not in smartcam_exif]
    smartcam_only = [k for k in all_keys if k in smartcam_exif and k not in pixel_exif]

    print("=" * 100)
    print("📊 COMPARISON SUMMARY")
    print("=" * 100)
    print()
    print(f"Common tags (both):        {len(common_keys)}")
    print(f"Pixel camera only:         {len(pixel_only)}")
    print(f"SmartCameraApp only:       {len(smartcam_only)}")
    print()

    # Show common tags
    if common_keys:
        print("=" * 100)
        print("✅ COMMON TAGS (Present in Both)")
        print("=" * 100)
        print()
        for key in common_keys:
            print(f"Tag: {key}")
            print(f"  Pixel:      {format_value(pixel_exif[key])}")
            print(f"  SmartCam:   {format_value(smartcam_exif[key])}")
            print()

    # Show Pixel-only tags (missing in SmartCameraApp)
    if pixel_only:
        print("=" * 100)
        print("⚠️  PIXEL CAMERA ONLY (Missing in SmartCameraApp) ⭐ IMPORTANT")
        print("=" * 100)
        print()
        for key in pixel_only:
            print(f"Tag: {key}")
            print(f"  Value: {format_value(pixel_exif[key])}")
            print()

    # Show SmartCameraApp-only tags
    if smartcam_only:
        print("=" * 100)
        print("🆕 SMARTCAMERAAPP ONLY")
        print("=" * 100)
        print()
        for key in smartcam_only:
            print(f"Tag: {key}")
            print(f"  Value: {format_value(smartcam_exif[key])}")
            print()

    # Camera-specific tag recommendations
    print("=" * 100)
    print("📸 CAMERA TAGS ANALYSIS")
    print("=" * 100)
    print()

    camera_tags = [
        "Make", "Model", "Software",
        "ExposureTime", "FNumber", "ISO", "ISOSpeedRatings",
        "FocalLength", "FocalLengthIn35mmFilm",
        "Flash", "WhiteBalance", "MeteringMode", "ExposureMode",
        "SceneCaptureType", "Contrast", "Saturation", "Sharpness",
        "LensMake", "LensModel", "LensSpecification"
    ]

    print("Recommended camera-related tags to consider adding:")
    print()
    for tag in camera_tags:
        if tag in pixel_exif and tag not in smartcam_exif:
            print(f"  ⚠️  {tag:30s} = {format_value(pixel_exif[tag])}")
        elif tag in smartcam_exif:
            print(f"  ✅ {tag:30s} = {format_value(smartcam_exif[tag])}")
        elif tag in pixel_exif:
            print(f"  ℹ️  {tag:30s} (in Pixel camera)")

    print()
    print("=" * 100)
    print("📋 RECOMMENDATIONS")
    print("=" * 100)
    print()

    critical_missing = []
    recommended_missing = []
    optional_missing = []

    for tag in pixel_only:
        if tag in ["Make", "Model", "Software"]:
            critical_missing.append(tag)
        elif tag in ["ExposureTime", "FNumber", "ISO", "ISOSpeedRatings", "FocalLength"]:
            recommended_missing.append(tag)
        else:
            optional_missing.append(tag)

    if critical_missing:
        print("🔴 CRITICAL - Should add:")
        for tag in critical_missing:
            print(f"   - {tag}: {format_value(pixel_exif[tag])}")
        print()

    if recommended_missing:
        print("🟡 RECOMMENDED - Consider adding:")
        for tag in recommended_missing:
            print(f"   - {tag}: {format_value(pixel_exif[tag])}")
        print()

    if optional_missing:
        print("🔵 OPTIONAL - Nice to have:")
        for tag in optional_missing[:10]:  # Show first 10
            print(f"   - {tag}")
        if len(optional_missing) > 10:
            print(f"   ... and {len(optional_missing) - 10} more")
        print()

    print("=" * 100)

    return {
        'common': common_keys,
        'pixel_only': pixel_only,
        'smartcam_only': smartcam_only,
        'critical_missing': critical_missing,
        'recommended_missing': recommended_missing
    }

def main():
    if len(sys.argv) < 3:
        print("Usage: python compare_exif.py <pixel_photo> <smartcamera_photo>")
        print("Example:")
        print("  python compare_exif.py \\")
        print("    docs/test_results/v2.9.7-Day1-2/images/PXL_20260129_Pixel9a.jpg \\")
        print("    docs/test_results/v2.9.7-Day1-2/images/2026-01-29-15-38-36-測試6-室外1.jpg")
        sys.exit(1)

    pixel_path = sys.argv[1]
    smartcam_path = sys.argv[2]

    if not Path(pixel_path).exists():
        print(f"ERROR: Pixel photo not found: {pixel_path}")
        sys.exit(1)

    if not Path(smartcam_path).exists():
        print(f"ERROR: SmartCamera photo not found: {smartcam_path}")
        sys.exit(1)

    compare_exif(pixel_path, smartcam_path)

if __name__ == "__main__":
    main()
