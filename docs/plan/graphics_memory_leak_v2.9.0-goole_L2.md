# Implementation Plan - v2.9.0 Graphics Leak Fix

This plan addresses the **Graphics Memory Leak** identified in v2.9.0-alpha (360x360 resolution), where Graphics memory grew to 224MB.
Based on **L2 Analysis**, the most feasible immediate fix is to align the resolution with CameraX standards (480x360) and add diagnostics.

## Goal Description
Fix the Graphics memory leak by ensuring the camera resolution is standard and supported, preventing potential BitmapPool misses or unreleased internal buffers.

## User Review Required
> [!IMPORTANT]
> This changes the resolution from **360x360** (Square) to **480x360** (4:3).
> This is a functional change but necessary for stability.

## Proposed Changes

### 1. Resolution Adjustment (Hypothesis 4 Check)
#### [MODIFY] [LiveCameraSource.kt](file:///d:/MyDocuments/DevSources/AndroidStudioProjects/SmartCameraApp1/app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt)
-   Change `targetResolution` for Pixel 9a from `Size(360, 360)` to `Size(480, 360)`.
-   **Rationale**: 480x360 is the guaranteed minimum supported resolution for ML Kit and CameraX. 360x360 might trigger software upsampling or odd-stride bitmaps that bypass the pool.

### 2. Diagnostic Logging (Hypothesis 1 Verification)
#### [MODIFY] [BitmapPool.kt](file:///d:/MyDocuments/DevSources/AndroidStudioProjects/SmartCameraApp1/app/src/main/java/com/akirachang/ai/photographer/util/BitmapPool.kt)
-   Enhance `acquire` and `release` to log specific dimensions and Pool Hit/Miss/Size events (verbose level).

#### [MODIFY] [LiveCameraSource.kt](file:///d:/MyDocuments/DevSources/AndroidStudioProjects/SmartCameraApp1/app/src/main/java/com/akirachang/ai/photographer/data/source/camera/LiveCameraSource.kt)
-   Add logging for `SurfaceTexture` creation/release counts to track potential surface leaks (Hypothesis 2).

## Verification Plan

### Automated/Manual Testing
1.  **Memory Test**:
    -   Run the standard 10-minute test script.
    -   **Pass Criteria**: Graphics Memory stabilizes < 100MB (similar to v2.8.6).
2.  **Log Analysis**:
    -   Filter logs for `BitmapPool`.
    -   **Pass Criteria**: Hit Rate > 90%.
    -   Filter logs for `SurfaceTexture`.
    -   **Pass Criteria**: Create count matches Release count (or difference is constant 1).
