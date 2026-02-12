# PR Title
v2.10.2: Aspect Ratio + EXIF Info + Regression Prevention

# PR Description

## Summary

This PR includes v2.10.0, v2.10.1, v2.10.2 development and regression prevention mechanisms.

### v2.10.0 Features
- ✅ Aspect ratio WYSIWYG fix (UseCaseGroup + ViewPort implementation)
- ✅ EXIF info support (GPS, Camera settings, Image metadata)
- ✅ Photo info bottom sheet UI
- ✅ Location permission handler
- ✅ Settings UI modernization (Expandable cards)

### v2.10.2 Bug Fixes
- ✅ **Permission UX**: Request all permissions at startup (Camera + Location + Storage)
- ✅ **Aspect Ratio**: Fixed Settings changes not taking effect (`.first()` → `.collect()`)
- ✅ **Toast Regression**: Removed v2.10.1 Snackbar "照片已儲存" (restored v2.9.8 solution)
- ✅ **UI Cleanup**: Removed photo info button (user feedback: non-standard UX)

### Regression Prevention Phase 1
- ✅ **Pre-Commit Checklist** (`devspec/pre_commit_checklist.md`)
- ✅ **PR Template** with Regression Check (`devspec/pull_request_template.md`)
- ✅ **Feature Record Guide** (`devspec/FEATURE_RECORD_GUIDE.md`)
- ✅ **CHANGELOG.md** (version tracking)
- ✅ **VERSION_CLARIFICATION.md** (version number management)

## Changed Files
- **Core**: 11 Kotlin files (MainActivity, CameraScreen, CameraViewModel, etc.)
- **New**: AspectRatioMode enum, PhotoInfoBottomSheet, LocationPermissionHandler
- **Docs**: 30+ documentation files (plans, test reports, handoff logs)
- **Tools**: stage_verification_gui.pyw, test_handoff_gui.pyw updates

## Test Results
- ✅ **Pixel 9a**: 100% PASSED (v2.10.2 full validation)
- ✅ **Sony XQ-BT52**: Cross-device compatibility verified
- ✅ **16KB Compatibility Check**: ELF Verification Successful
- ✅ **Build**: 20s incremental compilation

## Regression Check
- [x] Searched historical fixes: `git log --grep="Snackbar"`, `git log --grep="aspect ratio"`
- [x] Checked `devspec/feature_record_Backlog.md` Critical Warnings
- [x] No duplicate fixes detected

### Critical Files Modified
- [x] **CameraScreen.kt** - Snackbar removed (v2.9.8 solution restored)
- [x] **CameraViewModel.kt** - Aspect ratio monitoring (`.collect()` used)
- [x] **MainActivity.kt** - Permission request logic updated

## Documentation Updates
- [x] `CHANGELOG.md` - v2.10.2 entry added
- [x] `devspec/feature_record_Backlog.md` - Critical Warnings added
- [x] `docs/VERSION_CLARIFICATION.md` - Version management guide created
- [x] Test reports for v2.10.0-fix, v2.10.1, v2.10.2

## Breaking Changes
None. All changes are backward compatible.

## Test Plan
- [x] Emulator testing (API 34)
- [x] Pixel 9a real device testing
- [x] Permission flow validation
- [x] Aspect ratio switching (4:3, 16:9, 1:1)
- [x] Photo capture feedback verification

## Deployment Notes
- Version updated: **v2.10.2** (versionCode: 72)
- No database migrations required
- No API changes

🤖 Generated with [Claude Code](https://claude.com/claude-code)
