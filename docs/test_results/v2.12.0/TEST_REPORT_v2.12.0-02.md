# Test Report — v2.12.0-02

**Date**: 2026-02-28
**Branch**: `feature/camera-core-v2.12.0`
**Commit**: `28b501f`
**Fix**: Remove `setAspectRatioStrategy()` from `analysisSelector` in `LiveCameraSource.kt`

---

## Root Cause (Confirmed via dumpsys media.camera)

Sony XQ-BT52 HAL has **no 16:9 YUV_420_888 resolution below 1280×720**.
- Smallest 16:9 YUV entry: **1280×720**
- Smallest 4:3 YUV entry: **320×240** (640×480 also available)

`AspectRatioStrategy(16:9)` in analysisSelector filtered to 16:9-only resolutions
→ Sony minimum = 1280×720 → `ResolutionStrategy(640×480)` cap failed (no lower 16:9 found)
→ fallback to 1280×720 → ML Kit processing 1280×720 NV21 → FPS < 20

---

## Test Results

| Device | Mode | vRowStride | Buffer | FPS | Pass |
|--------|------|-----------|--------|-----|------|
| Sony XQ-BT52 | 16:9 (BEFORE) | 1280 | 1280×720 | 14.8 | ❌ |
| Sony XQ-BT52 | 4:3 | 640 | 640×480 | 24.1 | ✅ |
| Sony XQ-BT52 | 16:9 | 640 | 640×480 | 22.8 | ✅ |
| Pixel 9a | 4:3 | 640 | 640×480 | 28.8 | ✅ |
| Pixel 9a | 16:9 | 640 | 640×480 | 29.3 | ✅ |

**Verdict: ALL PASS**

---

## Thermal Note (Sony at time of test)

- Thermal Status: 3 (SEVERE), SOC=88°C, skin=40°C
- Despite thermal throttling, FPS > 20 confirmed — fix is robust

---

## Coordinate System Impact

- 16:9 mode: ImageAnalysis physical buffer = 640×480 (4:3)
- `imageProxy.cropRect` reflects ViewPort 16:9 crop region
- `DetectionResult.imageWidth=640, imageHeight=480` (full buffer, not cropRect)
- ML Kit analyzes full 640×480 including ~60px letterbox top/bottom
- **Impact assessed: LOW** — letterbox region is black, no valid faces expected there
- Visual confirmation: face detection boxes aligned correctly during testing

---

## Log Files

- `tmp/logcat_sony_16_9_resolution.log` — Sony 16:9 BEFORE fix (vRowStride=1280, 14.8 FPS)
- `tmp/logcat_sony_16_9_after_fix.log` — Sony 4:3 AFTER fix (vRowStride=640, 24.1 FPS)
- `tmp/logcat_sony_16_9_v2.log` — Sony 16:9 AFTER fix (vRowStride=640, 22.8 FPS)
- `tmp/logcat_pixel9a_4_3.log` — Pixel 9a 4:3 (vRowStride=640, 28.8 FPS)
- `tmp/logcat_pixel9a_16_9.log` — Pixel 9a 16:9 (vRowStride=640, 29.3 FPS)
- `tmp/dumpsys_camera.txt` — Sony HAL stream configuration dump
