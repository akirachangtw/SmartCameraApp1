# Changelog

All notable changes to SmartCameraApp1 will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.9.6-phase2.6.2] - 2026-01-28

### Fixed
- **CRITICAL**: Android 16 (API 36) 16KB page size alignment issues completely resolved
  - Eliminated PageSizeMismatchDialog on Pixel 9a (PAGE_SIZE=4096)
  - Upgraded ML Kit Face Detection: 16.1.5 → 16.1.7 (16KB support, released Jan 15, 2026)
  - Upgraded ML Kit Object Detection: 17.0.1 → 17.0.2 (16KB support)
  - All native libraries now 16KB ELF LOAD segment aligned

### Verified
- ✅ **Triple Verification Passed**:
  1. zipalign CLI: 16/16 libraries 16KB aligned (ZIP level)
  2. Google official script (AOSP): ELF + ZIP alignment verified
  3. Android Studio APK Analyzer: No 16KB warnings
- ✅ **Physical Device Test**: Pixel 9a (Android 16, PAGE_SIZE=4096) - NO warnings
- ✅ **Emulator Test**: Pixel9a-16KB (PAGE_SIZE=16384) - Stable, no regressions
- ✅ **Google Play Compliance**: Meets 16KB requirement (123 days before May 31, 2026 deadline)

### Technical Details
- **Root Cause**: ML Kit 16.1.5/17.0.1 native libraries had ELF LOAD segments aligned to 4KB (2^12) instead of 16KB (2^14)
- **Solution**: Direct ML Kit upgrade (not transitive via CameraX)
- **Dependencies Updated**:
  - com.google.mlkit:face-detection: 16.1.5 → 16.1.7
  - com.google.mlkit:object-detection: 17.0.1 → 17.0.2
- **APK Size**: 246 MB (was 238 MB, +8 MB acceptable)
- **Performance**: No regressions (E2E ~100ms on physical device maintained)
- **Stability**: 100% (0 crashes, 0 dropped frames in 6-minute test)

### Testing
- Home NB: Triple verification (zipalign + Google tool + APK Analyzer)
- Work NB: Physical Pixel 9a test confirmation
- Test Reports: `docs/test_results/v2.9.6-Phase2.6/`
  - FINAL_VERIFICATION_REPORT.md
  - Phase_2.6.1_Verification_Results.md
  - Emulator_Stability_Test_Report.md
  - Work_NB_Phase2.6.1_Test_Report.md

### Changed
- Version: 2.9.6-phase2.5 (Build 57) → 2.9.6-phase2.6.2 (Build 58)
- gradle/libs.versions.toml: ML Kit versions updated

## [1.1.0] - 2025-11-17

### Added
- ML Kit Face Detection integration (v16.1.5)
  - Fast performance mode for real-time detection
  - Face tracking enabled with minimum size threshold
  - Detects multiple faces simultaneously
- ML Kit Object Detection integration (v17.0.1)
  - Stream mode optimized for video frames
  - Multiple object detection with classification
  - Real-time object tracking
- Real-time image analysis system
  - 10 FPS target frame rate with throttling
  - Background thread processing with single-threaded executor
  - Automatic frame skipping to prevent backlog
- Detection result state management
  - StateFlow-based reactive updates
  - Domain model for detection results
  - ViewModel integration for UI observation
- Background executor for ML processing
  - Dedicated single-threaded executor
  - Hilt dependency injection configuration
  - Thread priority management

### Changed
- Enhanced logging levels from DEBUG to INFO
  - Improved visibility on Android 11+ devices
  - Production-ready log filtering
  - Frame analysis timestamps added
- Repository pattern implementation
  - Added ML analysis control methods
  - Detection result flow integration
  - Lifecycle-aware processing

### Fixed
- Gradle version catalog naming conflicts
  - Resolved Kotlin reserved keyword issue with "object"
  - Used camelCase naming convention (mlkitObjectDetection)
- Missing kotlinx-coroutines-play-services dependency
  - Added support for Task.await() extension
  - Enables suspend function integration with ML Kit
- Experimental API annotation warnings
  - Added @OptIn(ExperimentalGetImage::class) annotations
  - Lint check compliance achieved

### Technical Details
- Architecture: Clean Architecture + MVVM
- Dependencies:
  - ML Kit Face Detection: 16.1.5
  - ML Kit Object Detection: 17.0.1
  - kotlinx-coroutines-play-services: 1.9.0
- Performance:
  - Target FPS: 10
  - Frame interval: 100ms
  - Processing strategy: KEEP_ONLY_LATEST
- Code Statistics:
  - New files: 4
  - Modified files: 5
  - Total lines: ~600
  - Implementation time: ~2 hours

## [1.0.0] - 2025-11-12

### Added
- Initial release
- Basic camera functionality
  - Camera preview with CameraX
  - Photo capture with flash support
  - Camera switching (front/back)
- Clean Architecture foundation
  - Domain layer with repository pattern
  - Data layer with repository implementation
  - Presentation layer with MVVM
- Hilt dependency injection setup
- Material Design 3 UI
