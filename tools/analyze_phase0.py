#!/usr/bin/env python3
"""
v2.8.0 Phase 0 Baseline Data Analyzer

Analyzes Performance logs from LiveCameraSource to extract:
- FPS statistics (avg, min, max, std dev)
- Drop Frame statistics
- Memory usage (Native/Java/Total PSS)
"""

import re
import statistics
import sys

def analyze_performance_log(log_file):
    """Analyze performance log file and print statistics."""

    fps_values = []
    dropped_frames = []
    total_frames = []
    native_mb = []
    java_mb = []
    total_mb = []

    pattern = r'Performance: FPS=([\d.]+), Dropped=(\d+)/(\d+) \(([\d.]+)%\), Memory: Native=(\d+)MB, Java=(\d+)MB, Total=(\d+)MB'

    with open(log_file, 'r', encoding='utf-8', errors='ignore') as f:
        for line in f:
            match = re.search(pattern, line)
            if match:
                fps_values.append(float(match.group(1)))
                dropped_frames.append(int(match.group(2)))
                total_frames.append(int(match.group(3)))
                native_mb.append(int(match.group(5)))
                java_mb.append(int(match.group(6)))
                total_mb.append(int(match.group(7)))

    if not fps_values:
        print("❌ No performance data found in log file!")
        return

    print("=" * 60)
    print("v2.8.0 Phase 0 Baseline Analysis")
    print("=" * 60)
    print(f"\nSamples Collected: {len(fps_values)}")
    print(f"Total Frames: {total_frames[-1] if total_frames else 0}")
    print(f"Test Duration: ~{len(fps_values) // 60:.1f} minutes")

    print("\n" + "=" * 60)
    print("FPS Statistics")
    print("=" * 60)
    print(f"Average FPS: {statistics.mean(fps_values):.1f}")
    print(f"Min FPS: {min(fps_values):.1f}")
    print(f"Max FPS: {max(fps_values):.1f}")
    print(f"Std Dev: {statistics.stdev(fps_values):.2f}")
    print(f"P50 (Median): {statistics.median(fps_values):.1f}")
    print(f"P95: {sorted(fps_values)[int(len(fps_values) * 0.95)]:.1f}")

    print("\n" + "=" * 60)
    print("Drop Frame Statistics")
    print("=" * 60)
    if total_frames:
        final_dropped = dropped_frames[-1]
        final_total = total_frames[-1]
        drop_rate = (final_dropped / final_total) * 100 if final_total > 0 else 0
        print(f"Total Frames: {final_total}")
        print(f"Dropped Frames: {final_dropped}")
        print(f"Drop Rate: {drop_rate:.2f}%")

    print("\n" + "=" * 60)
    print("Memory Statistics (PSS in MB)")
    print("=" * 60)
    print(f"\nNative Heap:")
    print(f"  Average: {statistics.mean(native_mb):.0f} MB")
    print(f"  Min: {min(native_mb)} MB")
    print(f"  Max: {max(native_mb)} MB")
    print(f"  Std Dev: {statistics.stdev(native_mb):.1f} MB")

    print(f"\nJava Heap:")
    print(f"  Average: {statistics.mean(java_mb):.0f} MB")
    print(f"  Min: {min(java_mb)} MB")
    print(f"  Max: {max(java_mb)} MB")
    print(f"  Std Dev: {statistics.stdev(java_mb):.1f} MB")

    print(f"\nTotal PSS:")
    print(f"  Average: {statistics.mean(total_mb):.0f} MB")
    print(f"  Min: {min(total_mb)} MB")
    print(f"  Max: {max(total_mb)} MB")
    print(f"  Std Dev: {statistics.stdev(total_mb):.1f} MB")

    if len(total_mb) > 10:
        memory_growth = total_mb[-10:][0] - total_mb[:10][-1]
        print(f"\nMemory Growth (first 10 vs last 10 samples): {memory_growth:+.0f} MB")

    print("\n" + "=" * 60)
    print("Summary")
    print("=" * 60)
    print(f"✅ FPS: {statistics.mean(fps_values):.1f} (target: ~28-30 FPS)")
    print(f"✅ Drop Rate: {drop_rate:.2f}% (target: < 5%)")
    print(f"✅ Memory: {statistics.mean(total_mb):.0f} MB avg (target: < 500 MB)")

    if drop_rate < 5.0:
        print("\n🎉 Phase 0 Baseline: PASS")
    else:
        print("\n⚠️ Phase 0 Baseline: WARNING - High drop rate")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python analyze_phase0.py <log_file>")
        print("Example: python analyze_phase0.py tmp/phase0_sony_live.log")
        sys.exit(1)

    analyze_performance_log(sys.argv[1])
