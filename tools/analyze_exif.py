#!/usr/bin/env python3
"""
v2.9.6 Phase 1: EXIF Orientation Analyzer
Analyzes JPEG photos to verify EXIF Orientation tags

Usage:
    python analyze_exif.py <photo_directory>
    python analyze_exif.py tmp/v2.9.6_phase1_testing/
"""

import sys
import os
from pathlib import Path
try:
    from PIL import Image
    from PIL.ExifTags import TAGS
except ImportError:
    print("ERROR: Pillow library not found")
    print("Install: pip install Pillow")
    sys.exit(1)

# EXIF Orientation values
ORIENTATION_MAP = {
    1: "Normal (0°)",
    2: "Mirrored",
    3: "Rotated 180°",
    4: "Mirrored & Rotated 180°",
    5: "Mirrored & Rotated 90° CW",
    6: "Rotated 90° CW (Landscape Right)",
    7: "Mirrored & Rotated 90° CCW",
    8: "Rotated 90° CCW (Landscape Left)"
}

def get_exif_orientation(image_path):
    """Extract EXIF Orientation tag from JPEG"""
    try:
        image = Image.open(image_path)
        exif_data = image._getexif()

        if exif_data is None:
            return None, "No EXIF data"

        # Find Orientation tag (tag number 274)
        orientation = None
        for tag_id, value in exif_data.items():
            tag_name = TAGS.get(tag_id, tag_id)
            if tag_name == "Orientation":
                orientation = value
                break

        if orientation is None:
            return None, "No Orientation tag"

        description = ORIENTATION_MAP.get(orientation, f"Unknown ({orientation})")
        return orientation, description

    except Exception as e:
        return None, f"Error: {str(e)}"

def analyze_directory(directory):
    """Analyze all JPEG files in directory"""
    photo_dir = Path(directory)

    if not photo_dir.exists():
        print(f"ERROR: Directory not found: {directory}")
        sys.exit(1)

    # Find all JPEG files
    jpg_files = sorted(photo_dir.glob("*.jpg")) + sorted(photo_dir.glob("*.JPG"))

    if not jpg_files:
        print(f"No JPEG files found in: {directory}")
        sys.exit(1)

    print("=" * 80)
    print(f"EXIF Orientation Analysis")
    print(f"Directory: {photo_dir.absolute()}")
    print(f"Files: {len(jpg_files)}")
    print("=" * 80)
    print()

    # Table header
    print(f"{'Photo Name':<35} | {'Orient':<5} | {'Description':<30}")
    print(f"{'-' * 35}-+-{'-' * 5}-+-{'-' * 30}")

    results = []
    for photo_path in jpg_files:
        filename = photo_path.name
        orientation, description = get_exif_orientation(photo_path)

        if orientation is None:
            orient_str = "N/A"
        else:
            orient_str = str(orientation)

        print(f"{filename:<35} | {orient_str:<5} | {description:<30}")
        results.append({
            'filename': filename,
            'orientation': orientation,
            'description': description
        })

    print()
    print("=" * 80)

    # Summary
    print("Summary:")
    orientation_counts = {}
    for result in results:
        orient = result['orientation']
        if orient:
            orientation_counts[orient] = orientation_counts.get(orient, 0) + 1

    for orient, count in sorted(orientation_counts.items()):
        desc = ORIENTATION_MAP.get(orient, "Unknown")
        print(f"  Orientation {orient} ({desc}): {count} photos")

    print()

    # Validation hints
    print("Expected EXIF values for v2.9.6 Phase 1 test:")
    print("  - Portrait (0°):        Orientation = 1 (Normal)")
    print("  - Landscape Right (90°): Orientation = 6 (Rotated 90° CW)")
    print("  - Landscape Left (270°): Orientation = 8 (Rotated 90° CCW)")
    print("  - Upside down (180°):   Orientation = 3 (Rotated 180°)")

    return results

def main():
    if len(sys.argv) < 2:
        print("Usage: python analyze_exif.py <photo_directory>")
        print("Example: python analyze_exif.py tmp/v2.9.6_phase1_testing/")
        sys.exit(1)

    directory = sys.argv[1]
    analyze_directory(directory)

if __name__ == "__main__":
    main()
