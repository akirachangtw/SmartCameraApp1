#!/usr/bin/env python3
"""
v2.9.7 Day1-2: GPS EXIF Analyzer
Analyzes JPEG photos to verify GPS EXIF tags

Usage:
    python analyze_gps_exif.py <photo_directory>
    python analyze_gps_exif.py docs/test_results/v2.9.7-Day1-2/images/
"""

import sys
import os
from pathlib import Path
try:
    from PIL import Image
    from PIL.ExifTags import TAGS, GPSTAGS
except ImportError:
    print("ERROR: Pillow library not found")
    print("Install: pip install Pillow")
    sys.exit(1)

def dms_to_decimal(dms_tuple):
    """Convert GPS DMS (Degrees, Minutes, Seconds) to Decimal Degrees"""
    degrees, minutes, seconds = dms_tuple
    return degrees + (minutes / 60.0) + (seconds / 3600.0)

def get_gps_exif(image_path):
    """Extract GPS EXIF data from JPEG"""
    try:
        image = Image.open(image_path)
        exif_data = image._getexif()

        if exif_data is None:
            return None, "No EXIF data"

        # Find GPS Info tag (tag number 34853)
        gps_info = exif_data.get(34853)
        if not gps_info:
            return None, "No GPS data"

        # Parse GPS data
        gps_data = {}
        for tag_id, value in gps_info.items():
            tag_name = GPSTAGS.get(tag_id, tag_id)
            gps_data[tag_name] = value

        # Extract key GPS fields
        result = {}

        # Latitude
        if 'GPSLatitude' in gps_data and 'GPSLatitudeRef' in gps_data:
            lat = dms_to_decimal(gps_data['GPSLatitude'])
            lat_ref = gps_data['GPSLatitudeRef']
            if lat_ref == 'S':
                lat = -lat
            result['latitude'] = lat
            result['latitude_ref'] = lat_ref
            result['latitude_dms'] = gps_data['GPSLatitude']

        # Longitude
        if 'GPSLongitude' in gps_data and 'GPSLongitudeRef' in gps_data:
            lon = dms_to_decimal(gps_data['GPSLongitude'])
            lon_ref = gps_data['GPSLongitudeRef']
            if lon_ref == 'W':
                lon = -lon
            result['longitude'] = lon
            result['longitude_ref'] = lon_ref
            result['longitude_dms'] = gps_data['GPSLongitude']

        # Altitude
        if 'GPSAltitude' in gps_data:
            result['altitude'] = float(gps_data['GPSAltitude'])
            result['altitude_ref'] = gps_data.get('GPSAltitudeRef', b'\x00')

        # Timestamp
        if 'GPSTimeStamp' in gps_data:
            time_tuple = gps_data['GPSTimeStamp']
            result['gps_time'] = f"{int(time_tuple[0]):02d}:{int(time_tuple[1]):02d}:{int(time_tuple[2]):02d}"

        # Date
        if 'GPSDateStamp' in gps_data:
            result['gps_date'] = gps_data['GPSDateStamp']

        # Other fields
        if 'GPSSpeed' in gps_data:
            result['speed'] = float(gps_data['GPSSpeed'])
        if 'GPSProcessingMethod' in gps_data:
            method = gps_data['GPSProcessingMethod']
            if isinstance(method, bytes):
                result['method'] = method.decode('utf-8', errors='ignore').strip('\x00')
            else:
                result['method'] = str(method)

        return result, "OK"

    except Exception as e:
        return None, f"Error: {str(e)}"

def analyze_directory(directory):
    """Analyze all JPEG files in directory"""
    photo_dir = Path(directory)

    if not photo_dir.exists():
        print(f"ERROR: Directory not found: {directory}")
        sys.exit(1)

    # Find all JPEG files (avoid duplicates)
    jpg_files = list(set(sorted(photo_dir.glob("*.jpg")) + sorted(photo_dir.glob("*.JPG"))))
    jpg_files = sorted(jpg_files)  # Sort for consistent output

    if not jpg_files:
        print(f"No JPEG files found in: {directory}")
        sys.exit(1)

    print("=" * 100)
    print(f"GPS EXIF Analysis - v2.9.7 Day1-2")
    print(f"Directory: {photo_dir.absolute()}")
    print(f"Files: {len(jpg_files)}")
    print("=" * 100)
    print()

    # Analyze each photo
    results = []
    for photo_path in jpg_files:
        filename = photo_path.name
        gps_data, status = get_gps_exif(photo_path)

        print(f"Photo: {filename}")
        print(f"  Status: {status}")

        if gps_data:
            if 'latitude' in gps_data:
                lat_dms = gps_data['latitude_dms']
                print(f"  GPS Latitude:  {float(lat_dms[0]):.0f}° {float(lat_dms[1]):.0f}' {float(lat_dms[2]):.2f}\" {gps_data['latitude_ref']}  ({gps_data['latitude']:.6f}°)")
            if 'longitude' in gps_data:
                lon_dms = gps_data['longitude_dms']
                print(f"  GPS Longitude: {float(lon_dms[0]):.0f}° {float(lon_dms[1]):.0f}' {float(lon_dms[2]):.2f}\" {gps_data['longitude_ref']}  ({gps_data['longitude']:.6f}°)")
            if 'altitude' in gps_data:
                print(f"  GPS Altitude:  {gps_data['altitude']:.1f} m")
            if 'gps_date' in gps_data and 'gps_time' in gps_data:
                print(f"  GPS DateTime:  {gps_data['gps_date']} {gps_data['gps_time']} UTC")
            if 'speed' in gps_data:
                print(f"  GPS Speed:     {gps_data['speed']:.1f} km/h")
            if 'method' in gps_data:
                print(f"  GPS Method:    {gps_data['method']}")
            print(f"  ✅ GPS EXIF tags present")
        else:
            print(f"  ❌ No GPS EXIF tags")

        print()
        results.append({
            'filename': filename,
            'has_gps': gps_data is not None,
            'gps_data': gps_data,
            'status': status
        })

    # Summary
    print("=" * 100)
    print("Summary:")
    gps_count = sum(1 for r in results if r['has_gps'])
    no_gps_count = len(results) - gps_count
    print(f"  Photos with GPS EXIF: {gps_count}/{len(results)}")
    print(f"  Photos without GPS:   {no_gps_count}/{len(results)}")
    print()

    # List photos without GPS
    if no_gps_count > 0:
        print("Photos without GPS EXIF:")
        for r in results:
            if not r['has_gps']:
                print(f"  - {r['filename']} ({r['status']})")
        print()

    print("Expected behavior for v2.9.7 Day1-2 test:")
    print("  ✅ GPS ON + Outdoor:  Should have GPS EXIF (Lat, Lon, Alt, Time)")
    print("  ❌ GPS OFF:           Should NOT have GPS EXIF")
    print("  ⚠️  GPS ON + Indoor:  May or may not have GPS (depends on signal)")

    return results

def main():
    if len(sys.argv) < 2:
        print("Usage: python analyze_gps_exif.py <photo_directory>")
        print("Example: python analyze_gps_exif.py docs/test_results/v2.9.7-Day1-2/images/")
        sys.exit(1)

    directory = sys.argv[1]
    analyze_directory(directory)

if __name__ == "__main__":
    main()
