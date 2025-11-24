# Changelog

All notable changes to SmartCameraApp1 will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
