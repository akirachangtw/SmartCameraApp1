Summary:
1. Primary Request and Intent:
   - Original task: Continue optical flow tracking fixes from Phase 7. The CENTER_CROP coordinate fix was the last edit before session start — needed rebuild, install, and test.
   - User confirmed CENTER_CROP test passed, then reported a preview zoom issue ("APP預覽畫面像放大1.4-1.5倍") compared to built-in cameras.
   - User requested the zoom issue be documented as a .md file ("先將上述預覽比例及滿屏手機螢幕的議題記錄另存.md").
   - User ran Scene 2–7 tests and reported results. Key findings: Scene 2 (re-tap) ✓, Scene 3 (no texture) expected loss, Scene 5 (AE/AF lock) ✓, Scene 7 specifically on front camera selfie has a coordinate mismatch (tap point A vs circle at point B).
   - Critical user observation: "移動動作稍微大點, 其定點偏移度就變大" — drift increases with movement speed. Pixel 9a built-in camera has "glue-like" tracking with virtually zero drift.
   - After EMA removal fixed the speed-dependent lag, user reported: "10:1" drift ratio vs built-in camera still remains ("漂移量還是一樣, 沒有變小" after template matching was added).

2. Key Technical Concepts:
   - **Lucas-Kanade (LK) Optical Flow**: OpenCV `calcOpticalFlowPyrLK()`. Single-point sparse tracking. Known to have cumulative per-frame drift.
   - **CENTER_CROP / FILL_CENTER geometry**: CameraX PreviewView scales uniformly to fill view, crops excess. uniformScale = max(previewW/imageW, previewH/imageH). For Pixel 9a: uniformScale=1.7258, cropOffX=167.1, cropOffY=0.
   - **EMA (Exponential Moving Average)**: Was SMOOTHING_ALPHA=0.35. Steady-state lag = velocity × (1-α)/α = velocity × 1.86 frames. Removed because it caused speed-proportional offset and LK is now stable (quality avg 0.91).
   - **Template Matching Drift Correction**: `Imgproc.matchTemplate` with `TM_CCOEFF_NORMED`. Saves 64×64 patch on first frame, searches ±48px around tracked point every 10 frames, snaps to best match if quality ≥ 0.7. Updates template at corrected position. **Did NOT work** — user confirmed drift unchanged. Likely root cause: camera pan causes perspective/appearance changes that break normalized cross-correlation matching.
   - **Aspect Ratio Mismatch (Zoom Issue)**: Image 960×1280 (3:4) vs screen 1080×2272 (9:19). CameraX gave 1280×960 despite targeting 1280×720. Effective zoom = 1.58x. 16:9 resolutions (1920×1080) ARE available on sensor but not selected due to shared ResolutionSelector between Preview and ImageAnalysis.
   - **Front Camera ML Kit Mirror Bug**: PreviewView auto-mirrors front camera display. ML Kit bounding boxes are in un-mirrored space. `updateTrackedObjectPosition()` uses bounding box centerX directly → shows at mirrored X position.
   - **Multi-point LK tracking** (not yet implemented): Potential next approach — track multiple features (corners) around tap point, use median position. More robust to individual point drift.

3. Files and Code Sections:

   - **`app/src/main/java/.../presentation/camera/CameraViewModel.kt`** (MODIFIED, uncommitted)
     - Removed EMA variables (lines ~106-110): `smoothedPointX`, `smoothedPointY`, `isFirstTrackingFrame`, `SMOOTHING_ALPHA`
     - Replaced with single comment: `// v2.x.x Phase 7: EMA removed — LK quality avg 0.91 after aspect-ratio + FPS fixes, noise jitter no longer significant. Direct raw coordinates used for zero-lag tracking.`
     - Optical flow callback now uses raw coordinates directly:
       ```kotlin
       val pointX = trackingResult.point.x.toFloat()
       val pointY = trackingResult.point.y.toFloat()
       _state.update { it.copy(focusPoint = Pair(pointX, pointY), ...) }
       ```
     - Removed `isFirstTrackingFrame = true` from `focusOnPoint()` optical flow branch

   - **`app/src/main/java/.../data/opencv/OpticalFlowTracker.kt`** (MODIFIED, uncommitted)
     - Added imports: `org.opencv.core.Core`, `org.opencv.core.CvType`
     - Added companion constants: `TEMPLATE_HALF_SIZE=32`, `SEARCH_RADIUS=48`, `CORRECTION_INTERVAL=10`, `MIN_MATCH_QUALITY=0.7f`
     - Added state vars: `templatePatch: Mat?`, `frameCount: Int`
     - `startTracking()`: added `releaseTemplate()` and `frameCount = 0`
     - `stopTracking()`: added `releaseTemplate()`
     - `track()` first-frame path: added `saveTemplate(downsampledGray)` call
     - `track()` successful LK path: added drift correction block after `trackedPoint = newPoint`:
       ```kotlin
       frameCount++
       if (frameCount >= CORRECTION_INTERVAL && templatePatch != null) {
           val corrected = correctDrift(downsampledGray)
           if (corrected != null) { trackedPoint = corrected }
           frameCount = 0
       }
       ```
     - Changed return to use `scalePointToOriginal(trackedPoint!!)` instead of `scalePointToOriginal(newPoint)`
     - Added 3 new private functions: `saveTemplate()`, `correctDrift()`, `releaseTemplate()`
     - `correctDrift()`: extracts search region, calls `matchTemplate(TM_CCOEFF_NORMED)`, uses `Core.minMaxLoc` to find best match, updates template at corrected position

   - **`docs/issues/preview_zoom_aspect_ratio.md`** (NEW, uncommitted)
     - Documents the 1.58x zoom issue with full math, Pixel 9a supported resolutions (29 total), and 3 candidate solutions (A: separate Preview ResolutionSelector, B: force 1920×1080 target, C: AspectRatioStrategy)

   - **`docs/issues/front_camera_mlkit_mirror.md`** (NEW, uncommitted)
     - Documents Scene 7 front camera selfie coordinate mismatch. Identifies `findTappedTarget()` and `updateTrackedObjectPosition()` as the two code paths needing mirror correction. Includes verification steps.

   - **`docs/test_results/v2.x.x-OpticalFlow/PIXEL9A_TEST_REPORT.md`** (NEW, committed)
     - Full Scene 1 test results: 234 frames, 0 status=0, quality avg 0.914, round-trip zero error, coordinate movement trace

   - **`docs/test_results/v2.x.x-OpticalFlow/logcat_phase7_centercrop.log`** (NEW, committed)
     - 1665-line logcat from CENTER_CROP test

   - **`app/src/main/java/.../data/source/camera/LiveCameraSource.kt`** (READ, not modified)
     - `selectDeviceOptimalResolution()` at line 1428: Pixel targets Size(1280,720), filter≤1920×1080. But CameraX actually selects 1280×960.
     - `logSupportedResolutions()` at line 1215: logs all 29 YUV_420_888 resolutions

   - **`app/src/main/java/.../presentation/camera/components/CameraPreview.kt`** (READ, not modified)
     - Line 107: `scaleType = PreviewView.ScaleType.FILL_CENTER`

   - **`app/src/main/java/.../data/source/camera/SmartCameraAnalyzer.kt`** (READ, not modified)
     - Contains `trackFromBitmap()` (bitmap pipeline), `startOpticalFlowTracking()`, and unused `analyze()` optical flow block

4. Errors and fixes:
   - **EMA causing speed-dependent offset**: SMOOTHING_ALPHA=0.35 gave steady-state lag proportional to velocity. User feedback: "移動動作稍微大點, 其定點偏移度就變大". Fixed by removing EMA entirely. User confirmed: "目前不在跟隨速度而偏移變大".
   - **Template matching drift correction did not work**: User feedback: "漂移量還是一樣, 沒有變小". Root cause under investigation — likely perspective/appearance changes during pan break TM_CCOEFF_NORMED matching, OR template not saving / quality threshold too high. Logcat capture started (PID 1686) but user has not yet re-tested to generate logs.
   - **Bash cd command error**: `cd /d/...` failed on Windows; switched to using absolute paths directly in commands.

5. Problem Solving:
   - **SOLVED**: CENTER_CROP coordinate mapping — zero round-trip error confirmed via logcat
   - **SOLVED**: LK tracking loss (status=0) — 234 frames zero failures after Fix A-C (FPS, aspect ratio, LK params)
   - **SOLVED**: Speed-dependent tracking lag — EMA removal eliminated velocity-proportional offset
   - **IN PROGRESS**: LK cumulative drift — template matching correction was added but user reports no improvement. Need to read logcat to determine if template matching is even executing, then likely switch to multi-point LK tracking approach
   - **DOCUMENTED, NOT FIXED**: Preview 1.58x zoom (aspect ratio mismatch)
   - **DOCUMENTED, NOT FIXED**: Front camera ML Kit bounding box mirror offset

6. All user messages:
   - "在logcat日誌應該有紀錄定點座標應該有在移動?" — asking whether logcat shows coordinate movement
   - "測試完成" — CENTER_CROP test complete
   - "其結果: 偏移變小, 應該還可以更優化" (from prior session) — offset smaller after Phase 7, but can optimize further
   - "追蹤持續隨相機移動直至物件離開畫面（末尾座標被 coerceIn 鎖在 X=0.0，代表物件已移出左邊緣）-> 可忽略, 已放下手機" — end-of-track X=0 is OK, phone was put down
   - "發現Sony, Pixel9a內建相機與APP預覽預設畫面比例小, APP預覽畫面像放大1.4-1.5倍" — preview appears zoomed 1.4-1.5x vs built-in cameras
   - "APP預覽畫面是滿屏手機螢幕" — confirming preview is full-screen
   - "先將上述預覽比例及滿屏手機螢幕的議題記錄另存.md" — save the preview ratio issue as a .md file
   - Scene 2–7 results block: Scene 2 正常, Scene 3 無法跟隨, Scene 4 如何驗測, Scene 5 正常, Scene 6 如何驗測, Scene 7 point A vs point B + critical drift observation
   - "場景 7：相機切換行為: 自拍前置鏡頭追蹤人臉會出現點擊定點A, 實際對焦圓圈定點B" — clarifying Scene 7 is front camera selfie
   - "目前不在跟隨速度而偏移變大, APP 與 Pixel 9a 內建相機比較看看跟隨偏移: 10 : 1, Pixel 9a像有膠水黏住, 偏移度小" — EMA removed: no more speed lag, but 10:1 drift vs built-in
   - "漂移量還是一樣, 沒有變小" — template matching correction had no effect on drift

7. Pending Tasks:
   - **IMMEDIATE**: Read logcat from drift debug session (`tmp/logcat_drift_debug.log`) — logcat PID 1686 is running, user needs to test to generate data, then analyze why template matching isn't correcting drift
   - **HIGH**: Fix LK cumulative drift — template matching failed; likely need multi-point LK tracking (detect corners around tap, track all, use median position, periodically re-detect)
   - **MEDIUM**: Fix preview 1.58x zoom — implement one of 3 documented solutions (separate Preview ResolutionSelector recommended)
   - **MEDIUM**: Fix front camera ML Kit mirror offset — mirror bounding box X in `findTappedTarget()` and `updateTrackedObjectPosition()` when front camera active
   - **LOW**: Commit uncommitted changes (EMA removal + template matching + issue docs)

8. Current Work:
   - Immediately before summary: User reported template matching drift correction had no effect ("漂移量還是一樣, 沒有變小"). I started a V-level logcat capture for OpticalFlowTracker (PID 1686) to debug WHY template matching isn't working. The logcat was set up but the user had not yet run a test to populate it.
   - The template matching code in OpticalFlowTracker.kt is: save 64×64 patch on first frame, every 10 LK frames call `correctDrift()` which does `matchTemplate(TM_CCOEFF_NORMED)` in a ±48px search window, accepts if quality ≥ 0.7, updates template at corrected position.
   - Uncommitted files: CameraViewModel.kt (EMA removal), OpticalFlowTracker.kt (template matching), docs/issues/preview_zoom_aspect_ratio.md, docs/issues/front_camera_mlkit_mirror.md

9. Optional Next Step:
   User's last message was "漂移量還是一樣, 沒有變小"
   — template matching didn't reduce drift. Logcat capture (PID 1686) was started. Next step: read the logcat (`tmp/logcat_drift_debug.log`) to check (a) whether "Template save" succeeded, (b) whether "Drift correction" messages appear, and (c) what quality values are being returned. Based on findings, likely need to switch approach from template matching to **multi-point LK tracking**: detect multiple corners around the tap point with `GoodFeaturesToTrack`, track all of them with `calcOpticalFlowPyrLK`, filter outliers, and use the median position as the tracked point. This naturally averages out per-point drift and is fundamentally more robust than single-point LK + template correction for panning cameras.