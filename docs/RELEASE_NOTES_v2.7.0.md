# Release Notes - v2.7.0

**Release Date**: 2026-01-06
**Version Code**: 38 → 39
**Build Type**: Production Release

---

## 📋 Summary

v2.7.0 introduces **OpenCV-powered composition guideline detection** to assist photographers with real-time visual feedback on leading lines, symmetry, and architectural elements. This release focuses on performance optimization and device compatibility.

**Target Audience**: Mid-to-high-end Android devices (Snapdragon 7+ Gen 2 or better, Tensor G4+)

---

## ✨ New Features

### 1. OpenCV Guideline Detection (Phase 2 Sprint 3-5)

**What's New**:
- Real-time detection of composition guideline lines using OpenCV Hough Line Transform
- Visual overlay showing detected lines with strength indicators
- Support for:
  - Leading lines (roads, rails, perspective lines)
  - Symmetry detection (horizontal/vertical axes)
  - Architectural lines (building edges, structures)
  - Diagonal composition aids

**Technical Implementation**:
- Canny Edge Detection + Probabilistic Hough Transform
- YUV → Bitmap → OpenCV Mat conversion pipeline
- 10 FPS sampling (100ms intervals) to balance performance and responsiveness
- Normalized coordinates [0.0, 1.0] for screen independence

**Feature Flags** (3-Level Rollback):
- `enableGuidelineOverlay`: Toggle guideline overlay UI
- `enableOpenCvAnalysis`: Toggle OpenCV processing
- `enableOpenCvGuidelineBinding`: Toggle data binding between analyzer and UI

---

## 🎯 Performance Optimization (Sprint 5)

### OpenCV Parameter Tuning

**Optimized Parameters**:
- `HOUGH_THRESHOLD`: 50 → 70 (+40% selectivity)
- `HOUGH_MIN_LINE_LENGTH`: 50.0 → 80.0 (+60% minimum line length)
- `MAX_LINES`: 10 → 6 (-40% line count for stronger results)
- `MIN_LINE_STRENGTH`: 0.3 → 0.5 (+67% strength threshold)

**Performance Results**:
- **Flagship devices** (Tensor G4, Snapdragon 8 Gen 2+): avg 90.7ms ✅ (< 100ms target)
- **Mid-range devices** (Snapdragon 690): avg 113.9ms ⚠️ (borderline acceptable)
- P95 latency: 146-157ms (95th percentile)

**Device Testing**:
- Pixel 9a (Tensor G4, API 36): ✅ 90.7ms avg, 146ms P95
- Sony Xperia 10 III (Snapdragon 690, API 33): ⚠️ 113.9ms avg, 157ms P95
- Emulator (x86_64, API 36): ⚠️ 50ms avg, 135ms P95 (StaticImageSource overhead)

---

## 📊 Enhanced Performance Tracking (Sprint 5)

### New Logging Schema

**Added Fields to GuidelineAnalysisResult**:
- `deviceModel`: Device identifier (e.g., "Pixel 9a", "Sony XQ-BT52")
- `androidVersion`: API level (e.g., 36 = Android 16)
- `paramsPreset`: Parameter set identifier (e.g., "sprint5_optimized")

**Log Format Example**:
```
Performance (30 frames): avg=90ms, P95=146ms, min=68ms, max=223ms | device=Pixel 9a, android=API36, params=sprint5_optimized
```

**Use Cases**:
- Device tier performance analysis
- A/B testing different parameter presets
- Field performance monitoring

---

## 🛡️ Feature Flags & Rollback Strategy

### 3-Level Degradation Hierarchy

**Level 1**: Disable `enableOpenCvGuidelineBinding`
- Guideline overlay shows static grid (fallback mode)
- No OpenCV-driven dynamic updates

**Level 2**: Disable `enableOpenCvAnalysis`
- No OpenCV processing
- Removes CPU/memory overhead

**Level 3**: Disable `enableGuidelineOverlay`
- Complete feature rollback
- App behaves like v2.6.x

**Rollback Triggers**:
- Performance degradation (avg > 150ms or P95 > 200ms)
- Stability issues (crashes, memory leaks)
- User feedback (battery drain, heat)

---

## 📱 Device Tier Recommendations

### Minimum Requirements

**Hardware**:
- CPU: Snapdragon 690 or equivalent (~280k AnTuTu)
- RAM: 4GB+ (6GB recommended)
- Android: API 24+ (Android 7.0+)

**Performance Expectations**:
- Snapdragon 690: ~110-130ms avg (borderline)
- Snapdragon 7+ Gen 2: ~90-110ms avg (acceptable)
- Tensor G4, Snapdragon 8 Gen 2+: ~70-95ms avg (excellent)

### Recommended Devices

**Flagship** (Optimal Experience):
- Google Pixel 9/9a/9 Pro (Tensor G4)
- Samsung Galaxy S24/S23 (Snapdragon 8 Gen 2/3)
- OnePlus 12/13 (Snapdragon 8 Gen 3)
- Xiaomi 14/15 (Snapdragon 8 Gen 3)

**Mid-Range** (Acceptable Experience):
- Google Pixel 7a/8a (Tensor G2/G3)
- Samsung Galaxy A55/A54 (Exynos 1480/1380)
- OnePlus Nord 4/3T (Snapdragon 7+ Gen 3)

**Not Recommended** (Performance Issues):
- Devices with Snapdragon 600 series or older
- Low-end devices with < 4GB RAM
- Devices older than 2021 (except flagship models)

---

## 🔧 Technical Details

### Architecture Changes

**New Components**:
- `GuidelineAnalyzer.kt`: OpenCV-based line detection analyzer
- `GuidelineOverlay.kt`: Jetpack Compose overlay for guideline rendering
- `GuidelineAnalysisResult.kt`: Data model for detected lines
- `ImageProxyExtensions.kt`: Image conversion utilities (Bitmap → Mat)

**Modified Components**:
- `CameraViewModel.kt`: Integrated guideline analysis with 10 FPS sampling
- `CameraScreen.kt`: Added GuidelineOverlay to camera preview
- `FeatureFlags.kt`: Added 3-level guideline feature flags

### Performance Bottleneck Analysis

**Current Pipeline** (v2.7.0):
1. Camera YUV output → Bitmap conversion (35ms, 31%)
2. Bitmap → OpenCV Mat conversion (25ms, 22%)
3. OpenCV processing (Canny + Hough) (34ms, 30%)
4. Buffer management overhead (20ms, 17%)
- **Total**: ~114ms avg (mid-range), ~91ms avg (flagship)

**Known Bottleneck**:
- YUV → Bitmap → Mat conversion accounts for ~53% of total time
- Optimization target for v2.8.0: Direct YUV → Mat conversion

---

## 🔮 Future Roadmap (v2.8.0+)

### v2.8.0 Priority 2: Architectural Optimization

**Objective**: Bypass Bitmap conversion bottleneck

**Planned Changes**:
- Direct ImageProxy → Mat conversion (extract Y plane only)
- Skip YUV → Bitmap step
- **Expected Improvement**: ~50ms reduction (44% faster)
  - Sony XQ-BT52: 113.9ms → ~65ms ✅
  - Pixel 9a: 90.7ms → ~45ms ✅

**Implementation Strategy**:
- Feature flag dual-path switching (`USE_DIRECT_Y_PLANE_MAT`)
- A/B testing with rotation alignment verification
- Gradual rollout (10% → 50% → 100%)

**Risk Assessment**:
- Medium risk (rotation/mirror handling needs verification)
- Mitigation: Dual-path fallback, device whitelisting

### v2.8.0+ Additional Optimizations

**Buffer Pool** (Memory Optimization):
- Reusable ByteArray pool for Mat conversion
- Reduce GC pressure (currently allocates per-frame)

**Adaptive Sampling** (Dynamic Performance):
- 10 FPS (normal): avg < 100ms
- 7 FPS (warning): avg 100-120ms
- 5 FPS (degraded): avg > 120ms

**Parameter Auto-Tuning**:
- Device tier detection (flagship/mid-range/low-end)
- Automatic parameter adjustment based on SoC model
- Trade-off: line count vs. performance

---

## 🧪 Testing Coverage

### Test Scenarios

**Functional Testing** (✅ Passed):
- 15 test images generated (architectural, road, indoor, symmetry, edge cases)
- Guideline detection accuracy: 6-12 lines per scene (high-quality)
- Overlay rendering: No jank, smooth updates
- Mode switching: Live ↔ Static (via build flavors)

**Performance Testing** (✅ Passed on Flagship):
- Emulator (x86_64, API 36): 60 samples, avg 50ms, P95 135ms
- Sony XQ-BT52 (SD 690, API 33): 60 samples, avg 113.9ms, P95 157ms
- Pixel 9a (Tensor G4, API 36): 60 samples, avg 90.7ms, P95 146ms ✅

**Stability Testing** (✅ Passed):
- 3-minute continuous run: No crashes, no memory leaks
- Background/foreground switching: Camera rebind stable
- Feature flag toggling: No side effects

**Device Matrix** (✅ Verified):
- Flagship: ✅ Pixel 9a (Tensor G4)
- Mid-range: ⚠️ Sony XQ-BT52 (Snapdragon 690)
- Emulator: ⚠️ Medium_Phone_API_36.1 (x86_64)

---

## 📚 Documentation Updates

**New Documents**:
- `docs/V2.7.0_EMULATOR_TEST_REPORT.md`: Emulator baseline testing
- `docs/V2.7.0_REAL_DEVICE_TEST_REPORT.md`: Sony XQ-BT52 Sprint 4 results
- `docs/V2.7.0_SPRINT5_OPTIMIZATION_REPORT.md`: Parameter tuning analysis
- `docs/V2.7.0_DEVICE_COMPARISON_REPORT.md`: Pixel vs Sony comparison
- `docs/V2.7.0_SPRINT4_TEST_SUMMARY.md`: Sprint 4 completion summary
- `docs/V2.7.0_PERFORMANCE_COMPARISON.md`: Emulator vs device analysis
- `docs/plan/v2.7.0_phase2_opencv_optimizer_L2.md`: L2 feasibility review

**Updated Documents**:
- `tools/prompts_v2.7.0_guideline_test.txt`: Test image generation prompts (15 scenarios)
- `app/src/debug/assets/test_images/`: 15 test images (guideline_*.jpg)

---

## 🚨 Known Issues & Limitations

### Performance Limitations

**Mid-Range Devices** (Snapdragon 690):
- Average processing time: ~114ms (13.9ms over 100ms target)
- P95 latency: ~157ms (acceptable but borderline)
- Recommendation: Flagship or mid-to-high-end devices preferred

**Emulator Performance**:
- StaticImageSource adds image switching overhead (~35ms)
- Not representative of real device performance
- Use real ARM devices for accurate testing

### Architectural Bottleneck

**Current Limitation**:
- YUV → Bitmap → Mat conversion: ~60ms (53% of total time)
- Cannot be optimized further with current architecture

**Resolution Timeline**:
- v2.8.0 Priority 2: Direct ImageProxy → Mat conversion
- Expected release: Q1 2026

### Feature Scope

**Not Included in v2.7.0**:
- No line classification (all lines are `LineType.GENERAL`)
- No horizontal/vertical/diagonal filtering in UI (available in API)
- No adaptive sampling based on device performance
- No CSV/JSON export for performance data

**Planned for Future Releases**:
- v2.8.0: Architectural optimization, adaptive sampling
- v2.9.0: Line classification, composition suggestions
- v3.0.0: AI-powered composition scoring

---

## 🔄 Migration Guide

### From v2.6.x to v2.7.0

**Breaking Changes**: ❌ None

**New Dependencies**:
- OpenCV 4.10.0 (already included in v2.6.x)

**Configuration Changes**: ❌ None

**Build Changes**:
- New feature flags in `build.gradle.kts`:
  ```kotlin
  buildConfigField("Boolean", "ENABLE_GUIDELINE_OVERLAY", "true")
  buildConfigField("Boolean", "ENABLE_OPENCV_ANALYSIS", "true")
  buildConfigField("Boolean", "ENABLE_OPENCV_GUIDELINE_BINDING", "true")
  ```

**API Changes**:
- New StateFlow in CameraViewModel: `guidelineResults: StateFlow<GuidelineAnalysisResult?>`
- No changes to existing APIs (fully backward compatible)

### Rollback Procedure

**Emergency Rollback** (Production Issue):
1. Set all feature flags to `false` in Firebase Remote Config (if enabled)
2. Redeploy v2.6.2 APK to affected users
3. Monitor crash reports and performance metrics

**Gradual Rollback** (Performance Degradation):
1. Disable `enableOpenCvGuidelineBinding` (fallback to static overlay)
2. If still degraded, disable `enableOpenCvAnalysis` (no processing)
3. If still degraded, disable `enableGuidelineOverlay` (complete rollback)

---

## 📞 Support & Feedback

**Issue Reporting**:
- GitHub Issues: [SmartCameraApp1 Issues](https://github.com/yourusername/SmartCameraApp1/issues)
- Email: akirachang@example.com

**Performance Data Collection**:
- Logcat filter: `adb logcat -s GuidelineAnalyzer:V CameraViewModel:V`
- Expected output: Performance stats every 30 frames
- Include device model, Android version, and avg/P95 metrics

**Known Device Issues**:
- If avg > 150ms, report device model + SoC info
- If crashes occur, attach full logcat dump

---

## 👥 Credits

**v2.7.0 Development Team**:
- Architecture: Claude Code (AI Assistant)
- Testing: Pixel 9a, Sony Xperia 10 III, Emulator API 36
- Documentation: Sprint 3-5 test reports, L2 feasibility review

**Special Thanks**:
- OpenCV Community: Open-source computer vision library
- CameraX Team: Modern Android camera API
- Jetpack Compose: Declarative UI framework

---

## 📝 Changelog (Detailed)

### Sprint 3 (OpenCV Integration)
- [Feature] GuidelineAnalyzer with Canny + Hough Line Transform
- [Feature] GuidelineOverlay Compose component
- [Feature] GuidelineAnalysisResult data model
- [Test] 15 test images generated (guideline_*.jpg)

### Sprint 4 (ViewModel Integration)
- [Feature] Bitmap.toGrayMat() extension function
- [Feature] GuidelineAnalyzer.analyzeBitmap() method
- [Feature] CameraViewModel guideline analysis StateFlow
- [UI] CameraScreen GuidelineOverlay integration
- [Test] Emulator testing (Medium_Phone_API_36.1)
- [Test] Sony XQ-BT52 real device testing

### Sprint 5 (Parameter Optimization)
- [Perf] HOUGH_THRESHOLD: 50 → 70
- [Perf] HOUGH_MIN_LINE_LENGTH: 50.0 → 80.0
- [Perf] MAX_LINES: 10 → 6
- [Perf] MIN_LINE_STRENGTH: 0.3 → 0.5
- [Test] Pixel 9a real device testing
- [Docs] Device comparison report
- [Docs] L2 feasibility review

### Sprint 5+ (Enhanced Logging)
- [Feature] GuidelineAnalysisResult: Added deviceModel, androidVersion, paramsPreset
- [Feature] GuidelineAnalyzer: Enhanced performance logging with device info
- [Docs] Release notes with device tier recommendations

---

## 🎯 Acceptance Criteria (v2.7.0)

### Functional Requirements ✅

- [x] OpenCV guideline detection working on Live mode
- [x] Guideline overlay rendering with normalized coordinates
- [x] 10 FPS sampling (100ms intervals)
- [x] 3-level feature flag rollback mechanism
- [x] Mode switching (Live ↔ Static) via build flavors

### Performance Requirements ⚠️

- [x] **Flagship devices**: avg < 100ms ✅ (Pixel 9a: 90.7ms)
- [⚠️] **Mid-range devices**: avg < 100ms ⚠️ (Sony XQ-BT52: 113.9ms, borderline)
- [x] P95 < 200ms ✅ (All devices: 146-157ms)
- [x] No memory leaks ✅ (3-minute continuous run)

### Testing Requirements ✅

- [x] Dual-device verification (Flagship + Mid-range)
- [x] Emulator baseline testing
- [x] 15 test images with varied scenarios
- [x] Performance data collection (avg, P95, min, max)
- [x] Device tier classification documented

### Release Gate Decision ✅

**Verdict**: ✅ **Approved for Release**

**Rationale**:
- Flagship devices meet acceptance criteria ✅
- Mid-range borderline but acceptable (13.9ms over target)
- 3-level rollback mechanism ready
- Target audience: Mid-to-high-end devices clearly documented
- v2.8.0 optimization roadmap provides clear improvement path

---

**End of Release Notes**
