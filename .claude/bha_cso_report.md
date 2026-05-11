# BHA-CSO Report - Camera Boundary Health Audit Review

**Date**: 2026-05-11  
**Input Reviewed**: `.claude/bha_report.md`  
**Scope**: `app/src/main/java/` read-only review, plus CSO decision framing  
**Code Change Status**: 0 changes under `app/src/main/java/`

---

## CSO Decision

**Decision: Path B - local hemostasis first, no broad surgery yet.**

T02 Hard Gate remains blocked by a real coordinate-contract defect, but the current evidence does not justify a broad Clean Architecture rewrite inside the T02 fix. The correct next move is:

1. Fix only the two failing normalization call sites after preserving the existing rotation-aware contract.
2. Add/require verification that proves the face bbox coordinate space on the target Sony device.
3. Open a separate boundary rehabilitation plan for the polluted module seams before future camera-pipeline expansion.

CSO rationale: the patient is not healthy, but several organs are reusable. Do not replace the whole camera architecture during Hard Gate recovery.

---

## Audit Reliability Review

`.claude/bha_report.md` is **directionally correct** but **not audit-grade as written**.

Accepted findings:

- `CameraViewModel.kt` is a god object: 2457 lines, 58 functions, 58 imports.
- Domain purity is materially compromised by Android, AndroidX, CameraX, ML Kit, Data, and Presentation references.
- `CameraRepository.kt` in domain leaks framework/API details that should belong behind adapters.
- `observeCompositionAdvice()` and `observePtfAlignment()` normalize face bboxes with raw `det.imageWidth` / `det.imageHeight`, while nearby mapper code already uses rotation-aware dimensions.

Corrections required before treating BHA as final evidence:

| Area | BHA Claim | Recheck Result | CSO Treatment |
|---|---:|---:|---|
| D1 `^import android\.` | Report says no direct output, then lists android imports | 23 hits across 15 domain files | Original report evidence is wrong; conclusion still valid |
| D1 `RectF|Bitmap|ImageProxy|InputImage` | Report says no direct naming | 37 hits with the requested pattern; 70 hits if Matrix/PointF included | Original report under-reports framework type leakage |
| D2 Face/Object refs | Report says 23 lines | 54 hits for requested pattern | Use 54 as raw scan count; classify comments separately if needed |
| D4 StateFlow presentation | Report says 21 hits | 22 hits | Minor count drift |
| Git status | Report says clean | Current tree has doc changes/untracked BHA report; app source diff is clean | Code scope is clean, repository status is not |

Verdict on BHA report: **accept as diagnostic direction, reject as precise raw-command transcript.**

---

## Quantified Boundary Health

Health score scale:

- 90-100: clean boundary, adapter-only framework contact.
- 70-89: minor leaks, no runtime symptoms.
- 40-69: bounded pollution requiring planned extraction.
- 0-39: boundary leak is already causing runtime or Hard Gate failure.

| Boundary Cut | Metric | Observed Value | Health | Verdict |
|---|---:|---:|---:|---|
| Domain -> Data | Domain references to `com...data` | 2 hits / 1 file | 35/100 | Fail |
| Domain -> Presentation | Domain references to `com...presentation` | 3 hits / 2 files | 25/100 | Fail |
| Domain -> Framework | `android.*` imports in domain | 23 hits / 15 files | 30/100 | Fail |
| Domain -> AndroidX | `androidx.*` imports in domain | 5 hits / 3 files | 35/100 | Fail |
| Domain -> ML Kit | `com.google.mlkit` in domain | 4 hits / 2 files | 25/100 | Fail |
| Presentation internal | `CameraViewModel.kt` size | 2457 lines | 15/100 | Critical |
| Presentation internal | coordinate/bbox pattern hits in VM | 74 hits | 15/100 | Critical |
| Presentation internal | Face/Object pattern hits in VM | 54 hits | 20/100 | Critical |
| Data -> Framework | CameraX/ML Kit in data repository | 8 ML Kit hits, 8 CameraX hits | 70/100 | Expected in implementation |
| Repository abstraction | framework hits in domain repository | 20 hits | 20/100 | Fail |

Overall boundary health: **Red, approximately 32/100**.

Interpretation: the Camera module is not clean enough to support confident feature expansion, but the failure is localized enough for a restrained T02 fix.

---

## Boundary Findings

### 1. Domain -> Data Boundary

Domain imports concrete Data-layer analysis classes:

- `domain/usecase/AnalyzeLightingUseCase.kt`
  - `ExposureDetector`
  - `HistogramCalculator`

This is a reverse dependency. Domain should define policy contracts or pure rules; Data should implement acquisition and framework-backed analysis.

CSO diagnosis: **polluted but not the immediate T02 root cause.** Schedule as boundary rehab, not Hard Gate hotfix.

### 2. Domain -> Presentation Boundary

Domain references Presentation models:

- `domain/usecase/AnalyzeLightingUseCase.kt` imports `presentation.camera.model.LightingState`.
- `domain/repository/CameraRepository.kt` imports `presentation.camera.model.LightingState`.
- `domain/repository/CameraRepository.kt` accepts `presentation.camera.model.AspectRatioMode`.

This makes domain policy depend on UI-layer state names. It also increases model drift risk because presentation can mutate domain semantics.

CSO diagnosis: **architectural fail.** Extract domain-owned camera/light/aspect contracts later; do not patch by creating duplicate names during T02.

### 3. Presentation Internal Structure

`CameraViewModel.kt` currently owns too many unrelated responsibilities:

- ML Kit result consumption.
- Bitmap analysis routing.
- CameraX PreviewView references.
- Coordinate normalization and matrix rebuilds.
- Composition aggregation inputs.
- PTF alignment smoothing.
- AF trigger calls.
- StateFlow orchestration.

Quantitative signals:

- 2457 lines.
- 58 functions.
- 58 imports.
- 74 coordinate/bbox pattern hits.
- 54 Face/Object pattern hits.
- 11 direct `.value =` assignments.

CSO diagnosis: **critical bloat.** This is the highest-risk organ, but should be strangled by extracting new components over time rather than rewritten in place during T02.

### 4. Data -> Framework Boundary

Framework contact inside `data/repository/CameraRepositoryImpl.kt` is expected:

- ML Kit detector setup and calls live in Data.
- CameraX controller calls live in Data.

However, the domain repository interface leaks framework types:

- `CameraSelector`
- `Preview.SurfaceProvider`
- `PreviewView`
- `CameraControl`
- `MeteringPointFactory`
- ML Kit `InputImage`, `Face`, `DetectedObject`

CSO diagnosis: **implementation placement is partly usable, abstraction boundary is unhealthy.** Keep Data implementation, redesign the domain interface later.

---

## T02 Root Cause Review

The T02 symptom is face halo/bbox offset. The strongest code-supported finding is a **coordinate contract mismatch**, not merely a rendering bug.

Evidence:

- `SmartCameraAnalyzer.kt` creates `InputImage.fromMediaImage(mediaImage, rotation)` but stores `DetectionResult.imageWidth = imageProxy.width`, `imageHeight = imageProxy.height`, and `rotationDegrees`.
- `CameraViewModel.updateContentBounds()` already computes effective dimensions by swapping width/height for 90/270 rotation.
- `CameraViewModel.rebuildNewPipelineSensorToViewMatrix()` uses the same rotation-aware effective dimensions.
- `CameraViewModel.observeCompositionAdvice()` uses raw `det.imageWidth` / `det.imageHeight` before creating `FaceLandmarkData.boundingBoxNormalized`.
- `CameraViewModel.observePtfAlignment()` repeats the same raw-width/raw-height normalization.
- `CompositionRule.evaluate()` explicitly requires normalized `[0, 1]` coordinates and forbids pixel-space compensation inside rules.
- `NormalizedToPixelTransformer` directly multiplies normalized values by canvas width/height; out-of-range normalized values become out-of-screen pixels by design.

CSO conclusion: **BHA-01 root direction is correct.** The two call sites violate the module's own rotation-aware coordinate invariant.

Important caveat: BHA states as fact that ML Kit bboxes are in rotated space. Official ML Kit docs state `Face.boundingBox` is in the input image, and Android's `MlKitAnalyzer` docs make coordinate systems explicit (`VIEW_REFERENCED` vs `ORIGINAL`). Since this project uses a custom analyzer rather than `MlKitAnalyzer`, the repo must verify actual bbox space with device logs or a deterministic test frame. The immediate defect remains valid because the repo already has inconsistent internal dimension handling.

Official references:

- ML Kit Face Detection Android: https://developers.google.com/ml-kit/vision/face-detection/android
- ML Kit `InputImage`: https://developers.google.com/android/reference/com/google/mlkit/vision/common/InputImage
- CameraX ML Kit Analyzer coordinate systems: https://developer.android.com/media/camera/camerax/mlkitanalyzer

---

## Reusable Organs

Keep and reuse:

- Composition rule classes that consume `FaceLandmarkData` as normalized `[0, 1]` input.
- `CompositionRule.evaluate()` coordinate contract.
- `NormalizedToPixelTransformer`, if upstream normalized coordinates are correct.
- `PreviewViewGeometryMapper` / existing rotation-aware effective-dimension pattern.
- `CameraRepositoryImpl` as the place where CameraX and ML Kit are allowed to exist.

Do not treat as stable SSOT:

- `DetectionResult` as a domain model while it carries ML Kit `Face` / `DetectedObject`.
- `CameraRepository` as a clean domain port while it exposes CameraX/ML Kit/PreviewView.
- `CameraViewModel` as owner for future coordinate, AF, and detection policy.
- Any new feature work that adds more direct ML Kit or CameraX handling into the ViewModel.

---

## CSO Orders

### Order 1 - T02 Hard Gate Local Fix

Authorize a narrow BHA-01-FIX ticket only if it touches the two normalization sites:

- `CameraViewModel.observeCompositionAdvice()`
- `CameraViewModel.observePtfAlignment()`

Required invariant:

```kotlin
val rotation = det.rotationDegrees
val imgW = (if (rotation == 90 || rotation == 270) det.imageHeight else det.imageWidth).toFloat()
val imgH = (if (rotation == 90 || rotation == 270) det.imageWidth else det.imageHeight).toFloat()
```

Acceptance criteria:

- No repository/domain/interface refactor in this ticket.
- No new models unless explicitly approved.
- Sony device or deterministic test verifies bbox/holo alignment.
- Log or test confirms raw image size, rotation, effective size, and normalized bbox remain within expected range.

### Order 2 - Boundary Rehab Plan

After T02 passes, create a separate architecture-review plan for:

- `DetectionResult` de-MLKit domain model extraction.
- `CameraRepository` port cleanup.
- ViewModel strangler extraction for geometry, ML result adaptation, AF policy, and PTF alignment.
- Domain-owned `LightingState` / `AspectRatioMode` contracts.

### Order 3 - Feature Gate

T03/T04 may resume only after T02 device validation, but any ticket that touches camera geometry, ML Kit, focus control, or repository API must go through boundary review first.

---

## Final CSO Verdict

**BHA diagnosis: accepted with corrections.**  
**Camera boundary health: red / not healthy.**  
**Immediate action: local T02 fix, not full surgery.**  
**Strategic action: schedule boundary rehabilitation before expanding camera features.**
