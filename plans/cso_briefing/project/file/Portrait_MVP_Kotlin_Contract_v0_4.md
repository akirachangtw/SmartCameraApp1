# Portrait MVP Kotlin Contract v0.4

Date: 2026-06-13
Status: UITA S12 audit + Perplexity dual-verification incorporated — CSO signed-off
Scope: Android / Kotlin / Jetpack Compose / CameraX / ML Kit Face Detection
Merge basis: Contract v0.2 (S11 merge: v0.1 base + Spec v0.1 + AF Reducer v0.1 + UITA S11 audit) + D-27 SSOT Amendment redline + UITA S12 state-machine hardening

## 1. Document Purpose

This contract is the single implementation handoff for Claude Code. It defines:

- The three-layer portrait guidance pipeline (sanitizer → publisher → evaluator)
- The two-stage face AF pipeline (owner reducer → AF state reducer)
- The AF coordinator bridge between auto AF and manual Tap-to-Focus
- State machines, threshold governance, overlay binding, and telemetry

No downstream document may override values defined here. Changes to rules, thresholds, or state definitions must first be written to SSOT v4.0.6 before being reflected here.

## 2. Source Authority

| Priority | Document | Governs |
|----------|----------|---------|
| 1 (highest) | SSOT v4.0.6 | Rules, thresholds, state semantics, detector config, performance budget |
| 2 | Layout Spec v2.5.1 | Preview bounds, overlay layer order, coordinate mapping |
| 3 | This contract | Kotlin types, reducer logic, AF pipeline, implementation boundary |

**Locked decisions referenced**: D-22 (detector mutual exclusion), D-23 (LANDMARK_ALL + CLASSIFICATION_ALL + CONTOUR_NONE + FAST + minFaceSize 0.15f + tracking) **[amended by D-27: LANDMARK_ALL → NONE for MVP; NEAR_EYE/EYE_CENTER/NOSE_BRIDGE focus targets and EyeLineRule deferred to Phase 1.5]**, D-24 (grid spec).

## 3. Architecture Overview

Three branches consume the same `List<RawDetectedFace>`:

```
ML Kit Face Detection (D-23 config)
         │
    List<RawDetectedFace>  ← shared input
         │
    ┌────┼──────────────────────┐
    │    │                      │
    ▼    ▼                      ▼
 Branch A:              Branch B:           Branch C:
 Guidance Pipeline      Face AF Pipeline    Scene Engine
 (eye-open gate)        (geometry+stability) (raw count)
    │                      │
    ▼                      ▼
 PublishedGuidance      FaceAfCommand ──┐
 Subject                                │
    │                 AF Coordinator ◄───┘◄── Manual Tap
    │                      │                  (SSOT App A)
    └──────────┬───────────┘
               ▼
      PortraitOverlayModel → Compose UI
```

Branch A gates on guidance eligibility (eye-open, ghost-face rejection). Branch B gates on AF suitability (geometry, stability). Branch C passes raw count to scene engine. A face may be AF-eligible but guidance-ineligible.

## 4. Package Layout

```
domain/portrait/
  PortraitConstants.kt           ← shared enums + constants
  PortraitThresholds.kt          ← unified threshold container
  PortraitThresholdProvider.kt   ← tier-aware provider interface

domain/portrait/model/
  RawDetectedFace.kt
  EligibilityDecision.kt
  EligibleFace.kt
  PublishedGuidanceSubject.kt
  PortraitSubjectPipelineResult.kt
  PortraitRuleResult.kt
  PortraitGuidanceState.kt

domain/portrait/sanitizer/
  PortraitSubjectSanitizer.kt
  GhostFaceGuard.kt

domain/portrait/rules/
  PortraitRuleEvaluator.kt       ← interface
  LevelRuleEvaluator.kt
  ThirdsRuleEvaluator.kt
  HeadroomRuleEvaluator.kt
  EyeLineRuleEvaluator.kt        ← Phase 1.5 (deferred; D-27: LANDMARK_ALL → NONE)
  GazeRuleEvaluator.kt           ← conditional: requires headEulerAngleY

domain/portrait/reducer/
  PortraitGuidanceReducer.kt

domain/portrait/af/
  MultiFaceAfOwnerReducer.kt
  FaceAfReducer.kt
  AfCoordinator.kt               ← bridges auto AF + manual tap

domain/portrait/telemetry/
  PortraitTelemetry.kt

presentation/camera/overlay/
  PortraitOverlayBinder.kt
  HaloGuidanceLayer.kt
  LevelTractionLayer.kt
```

Domain models must not import `androidx.compose.*`. Compose types (`Offset`, `Color`, `Rect` from Compose) are used only in `presentation/` package. Domain uses `android.graphics.RectF`, `android.graphics.PointF`, or custom value types.

## 5. Shared Enums

```kotlin
// --- Device tier (SSOT §10.3 naming) ---
enum class DeviceTier {
    FULL,       // 10fps ML Kit + all rules
    LIMITED,    // 8fps + core 4 rules
    LEGACY      // 5fps + 2 rules
}

// --- Focus target (AF pipeline) ---
enum class FocusTargetType {
    FACE_CENTER     // MVP: only available target; LANDMARK_ALL disabled per D-27

    // Phase 1.5 — re-enable when LANDMARK_ALL is restored:
    // NEAR_EYE,    // requires LANDMARK_ALL
    // EYE_CENTER,  // requires LANDMARK_ALL
    // NOSE_BRIDGE, // requires LANDMARK_ALL
}
```

## 6. Branch A: Guidance Pipeline Data Contracts

### 6.1 Layer 1 — Raw Detection

```kotlin
data class RawDetectedFace(
    val trackingId: Int?,
    val boundingBox: RectF,           // preview-transformed coordinates
    val boundingBoxImage: Rect,       // sensor/ImageProxy coordinates
    val faceSizeRatio: Float,         // bbox area / preview area
    val headEulerAngleY: Float,       // yaw
    val headEulerAngleZ: Float,       // roll
    val leftEyeOpenProbability: Float?,
    val rightEyeOpenProbability: Float?,
    val smilingProbability: Float?,
    val landmarks: Map<Int, PointF>?, // ML Kit landmark type -> preview-transformed point; D-27: always null in MVP (LANDMARK_MODE_NONE); populated when LANDMARK_ALL restored in Phase 1.5
    val timestampMs: Long
)
```

Mapped from ML Kit `Face` by a dedicated `RawFaceMapper` that applies preview coordinate transform. Landmarks keyed by ML Kit `FaceLandmark.LandmarkType` constants. Nullable when classification/landmark data is unavailable.

### 6.2 Layer 2 — Eligibility

```kotlin
data class EligibilityDecision(
    val isEligible: Boolean,
    val rejectReasons: Set<GuidanceRejectReason>,
    val eyeOpenScore: Float?,         // min(left, right) or null
    val ghostSuspected: Boolean,
    val boundaryScore: Float,         // 0.0 = fully out of bounds, 1.0 = fully in
    val stabilityFrames: Int
)

enum class GuidanceRejectReason {
    FACE_TOO_SMALL,
    FACE_OUT_OF_BOUNDS,
    FACE_PARTIALLY_CROPPED,
    EYE_CLOSED_OR_UNCERTAIN,
    EXTREME_POSE,
    LOW_STABILITY,
    GHOST_FACE_SUSPECTED,
    MISSING_CLASSIFICATION_DATA
}

data class EligibleFace(
    val raw: RawDetectedFace,
    val decision: EligibilityDecision,
    val isPrimaryCandidate: Boolean
)
```

### 6.3 Layer 3 — Published Guidance Subject

```kotlin
enum class PublishReason {
    SINGLE_ELIGIBLE_FACE,
    HIGHEST_SUBJECT_SCORE,
    STABLE_TRACKING_RETENTION
}

data class PublishedGuidanceSubject(
    val trackingId: Int?,
    val boundingBox: RectF,
    val faceCenter: PointF,
    val faceSizeRatio: Float,
    val headEulerAngleY: Float,
    val headEulerAngleZ: Float,
    val landmarks: Map<Int, PointF>?, // D-27: always null in MVP (LANDMARK_MODE_NONE); populated when LANDMARK_ALL restored in Phase 1.5
    val publishReason: PublishReason,
    val timestampMs: Long
)
```

Only `PublishedGuidanceSubject` may drive Halo, composition rules, and guidance text. Overlay layers must never consume `RawDetectedFace` or `EligibleFace`.

### 6.4 Sanitizer Contract

```kotlin
interface PortraitSubjectSanitizer {
    fun sanitize(
        rawFaces: List<RawDetectedFace>,
        thresholds: PortraitThresholds,
        deviceTier: DeviceTier
    ): SanitizeResult
}

data class SanitizeResult(
    val eligible: List<EligibleFace>,
    val rejected: List<RejectedFace>
)

data class RejectedFace(
    val trackingId: Int?,
    val boundingBox: RectF,
    val reason: GuidanceRejectReason
)

interface GhostFaceGuard {
    fun isSuspectedGhostFace(
        raw: RawDetectedFace,
        thresholds: PortraitThresholds
    ): Boolean
}
```

MVP sanitization rules in evaluation order:

1. `faceSizeRatio < minFaceSizeRatio` → FACE_TOO_SMALL
2. Bounding box mostly outside preview bounds → FACE_OUT_OF_BOUNDS
3. `abs(headEulerAngleY) > extremePoseThresholdDeg` → EXTREME_POSE
4. Eye-open probability unavailable when classification enabled → MISSING_CLASSIFICATION_DATA
5. `min(leftEyeOpen, rightEyeOpen) < minEyeOpenProbForGuidance` → EYE_CLOSED_OR_UNCERTAIN
6. `GhostFaceGuard.isSuspectedGhostFace()` → GHOST_FACE_SUSPECTED

**Hard Constraint (K4-R16) — Guidance Predicate Consumer Layer**

Guidance eligibility predicates (eye-open gate, ghost-face threshold, boundary filter, pose filter, etc.) **MUST** be evaluated exclusively inside the `PortraitSubjectSanitizer` consumer layer. These predicates **MUST NOT** be pushed down to:

- ML Kit `FaceDetector` configuration options
- `CameraRepository` or any data-layer class
- Any shared raw-state producer (e.g. `_state.detectionResult.faces`)

Rationale (K4-Rule 28 §①): Branch B (AF pipeline) depends on the full unfiltered `List<RawDetectedFace>`. Filtering at the producer level silently starves AF ownership and focus reacquire. All gating belongs at the consumer boundary only.

### 6.5 Publisher Contract

```kotlin
interface GuidanceSubjectPublisher {
    fun publish(
        eligible: List<EligibleFace>,
        previousSubject: PublishedGuidanceSubject?,
        nowMs: Long,
        thresholds: PortraitThresholds
    ): PublishedGuidanceSubject?
}
```

Selection rules:

- Zero eligible → return null
- One eligible → publish directly
- Multiple eligible → largest `faceSizeRatio` first, then center proximity, then tracking ID stability

Subject retention: if the previously published subject's tracking ID still appears in the eligible list, retain it unless a challenger exceeds the switch margin for the hold duration.

### 6.6 Detector Session Lifecycle (SSOT §4.4.2)

ML Kit `FaceDetector` is a stateful session object. Its **lifetime MUST be bound to the active Camera Session** — created when the camera session starts and closed when the session ends. The ViewModel manages session *state* and *intent* (e.g., whether portrait mode is active) but **MUST NOT** hold the detector instance as a long-lived ViewModel-scoped resource.

| Event | Required action |
|-------|----------------|
| Camera session binds (CameraX use case bound) | Create `FaceDetector` via `FaceDetection.getClient(options)` |
| Configuration change (rotation, fold) | Camera session rebinds — detector is closed and re-created with the new session |
| Camera session unbinds (background, phone call, mode switch) | Call `detector.close()` immediately; null the reference |
| Session fully terminated (user exits portrait mode) | `detector.close()` at use-case unbind; ViewModel clears session intent |
| ViewModel `onCleared()` | **MUST NOT** be the primary teardown point; detector must already be null at this stage |

**Lifetime boundary rule (SSOT §4.4.2)**: `FaceDetector` tracks face identities via `trackingId`. Its internal tracking state is valid only within a single continuous camera session. A new session requires a new detector instance so that `trackingId` counters reset cleanly and no stale identity bleeds across sessions.

The ViewModel is the correct owner for *session intent* (e.g., `isPortraitModeActive`) and *published state* (e.g., `PortraitGuidanceState`), but detector instance management belongs in the camera session layer (e.g., `LiveCameraSource` or equivalent `ImageAnalysis.Analyzer` owner).

**Closed-instance prohibition (SSOT §4.4.2)**: A closed `FaceDetector` instance MUST NOT be reused. Calling `process()` on a closed detector is undefined behavior — the native ML Kit state is invalidated upon `close()`, not merely the `trackingId` counters. A new camera session always requires a fresh `FaceDetection.getClient(options)` call.

```kotlin
// Correct: Camera-session-scoped ownership
class LiveCameraSource @Inject constructor(...) {
    private var faceDetector: FaceDetector? = null

    fun onCameraSessionStarted() {
        faceDetector = FaceDetection.getClient(buildDetectorOptions())
    }

    fun onCameraSessionStopped() {
        faceDetector?.close()
        faceDetector = null
    }
}
```

Detector options for MVP (D-23, amended by D-27):

```kotlin
private fun buildDetectorOptions(): FaceDetectorOptions =
    FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)  // D-27: was ALL
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
        .setMinFaceSize(0.15f)
        .enableTracking()
        .build()
```

## 7. Branch A: Rule Evaluation

### 7.1 Rule Result

```kotlin
data class PortraitRuleResult(
    val rule: PortraitRuleType,
    val canImprove: Boolean,
    val priority: Int,
    val improvementEstimate: Float,   // 0.0 = perfect, 1.0 = worst
    val confidenceFactor: Float,      // SSOT §4.2: IMU=0.95, ML Kit face=0.80
    val messageKey: String,           // localization key
    val debug: Map<String, Float> = emptyMap()
)

enum class PortraitRuleType {
    LEVEL,
    THIRDS,
    HEADROOM,
    EYELINE,    // Phase 1.5 — deferred; LANDMARK_ALL disabled per D-27
    GAZE,       // MVP P1 — conditional on headEulerAngleY reliability
    TILT,       // Phase 1.5 — deferred; headEulerAngleZ > 8° (SSOT §4.4)
    SMILE       // Phase 1.5 — deferred; smilingProbability > 0.4 sustained > 300ms (SSOT §4.4)
}
```

FRAMING removed per CSO S12 ruling: FramingRule is SSOT §4.5 PTF-only (designed for "friends who don't understand photography" centering use case); Portrait mode coverage via ThirdsRule + HeadroomRule is sufficient.

### 7.2 Rule Evaluator Interface

```kotlin
interface PortraitRuleEvaluator {
    val ruleType: PortraitRuleType
    fun evaluate(
        subject: PublishedGuidanceSubject,
        orientation: DeviceOrientation,
        previewBounds: RectF,
        thresholds: PortraitThresholds
    ): PortraitRuleResult
}

data class DeviceOrientation(
    val pitchDeg: Float,
    val rollDeg: Float
)
```

### 7.3 Priority Formula (SSOT §4.2)

```
finalScore = ruleWeight × improvementEstimate × confidenceFactor

Arbitration: rules.filter { it.canImprove }
                  .maxByOrNull { it.priority * it.improvementEstimate }
```

Rule weights per SSOT §4.2: angle=0.40, level=0.25, composition=0.20, lighting=0.15.

Confidence factors per SSOT §4.2: IMU=0.95, ML Kit face=0.80.

### 7.4 MVP Rule Priority Matrix

| Rule | Input source | Confidence | MVP Priority | SSOT phase |
|------|-------------|------------|-------------|------------|
| LEVEL | IMU roll | 0.95 | P0 | MVP |
| THIRDS | face center | 0.80 | P0 | MVP (§4.4) |
| HEADROOM | face bbox top | 0.80 | P0 | MVP (§4.4) |
| GAZE | headEulerAngleY | 0.80 | P1 conditional | MVP P1 |
| EYELINE | landmarks[LEFT_EYE].y | 0.80 | Phase 1.5 | Deferred — LANDMARK_ALL disabled per D-27 |
| TILT | headEulerAngleZ | 0.80 | Phase 1.5 | SSOT §4.4 — headEulerAngleZ > 8° |
| SMILE | smilingProbability | 0.80 | Phase 1.5 | SSOT §4.4 — smilingProbability > 0.4 sustained > 300ms |

## 8. Branch A: Guidance Reducer

### 8.1 Guidance State Machine

```kotlin
enum class PortraitGuidanceState {
    NO_FACE,                      // rawFaces is empty
    FACES_DETECTED_NONE_ELIGIBLE, // rawFaces non-empty, all rejected by sanitizer
    SUBJECT_STABILIZING,          // eligible face found, debounce hold not yet passed
    GUIDANCE_ACTIVE,              // subject locked, canImprove = true
    ALIGNED,                      // subject locked, all rules satisfied
    SUPPRESSED                    // publish suppressed (flicker guard / focus priority)
}
```

**Dual-layer separation note**: SSOT §4.4.1 defines the semantic meaning of each state (what it means for the user experience). This contract defines the state transition logic (§8.2: when and why to move between states). Both definitions are intentionally maintained — they serve different concerns and neither supersedes the other.

### 8.2 State Transitions

| From | To | Condition |
|------|-----|-----------|
| NO_FACE | FACES_DETECTED_NONE_ELIGIBLE | rawFaces.isNotEmpty() AND eligible.isEmpty() |
| NO_FACE | SUBJECT_STABILIZING | First eligible face appears |
| FACES_DETECTED_NONE_ELIGIBLE | SUBJECT_STABILIZING | First eligible face appears |
| SUBJECT_STABILIZING | GUIDANCE_ACTIVE | Same subject persists through publishEnterHoldMs |
| SUBJECT_STABILIZING | NO_FACE | All faces lost (rawFaces.isEmpty()) |
| SUBJECT_STABILIZING | FACES_DETECTED_NONE_ELIGIBLE | rawFaces.isNotEmpty() AND eligible.isEmpty() |
| SUBJECT_STABILIZING | SUPPRESSED | focusPriorityActive == true (user tap during stabilization) |
| GUIDANCE_ACTIVE | ALIGNED | All rules canImprove == false, stable for alignmentStableMs |
| GUIDANCE_ACTIVE | SUPPRESSED | Flicker guard triggered or focusPriorityActive == true |
| GUIDANCE_ACTIVE | FACES_DETECTED_NONE_ELIGIBLE | publishedSubject == null AND rawFaces.isNotEmpty() AND eligible.isEmpty() — SHOULD be debounced by publishExitHoldMs |
| ALIGNED | GUIDANCE_ACTIVE | Any rule canImprove == true |
| ALIGNED | SUPPRESSED | focusPriorityActive == true OR flicker guard triggered |
| ALIGNED | FACES_DETECTED_NONE_ELIGIBLE | publishedSubject == null AND rawFaces.isNotEmpty() AND eligible.isEmpty() — SHOULD be debounced by publishExitHoldMs |
| SUPPRESSED | GUIDANCE_ACTIVE | Suppression clears AND publishedSubject != null |
| SUPPRESSED | FACES_DETECTED_NONE_ELIGIBLE | Suppression clears AND publishedSubject == null AND rawFaces.isNotEmpty() AND eligible.isEmpty() |
| SUPPRESSED | NO_FACE | Suppression clears AND rawFaces.isEmpty() |
| ANY | NO_FACE | All faces lost beyond subjectLostTimeoutMs (rawFaces.isEmpty() sustained) |

**SUPPRESSED state hardening note (S12)**: SUPPRESSED entry is triggered by AfCoordinator manual tap or flicker guard from any subject-tracking state (GUIDANCE_ACTIVE, ALIGNED, SUBJECT_STABILIZING). SUPPRESSED exit depends on suppression-clear event AND current face/eligibility state: (1) subject still present → GUIDANCE_ACTIVE for rule re-evaluation — no direct SUPPRESSED → ALIGNED jump; (2) subject ineligible, faces present → FACES_DETECTED_NONE_ELIGIBLE; (3) no faces → NO_FACE (immediate, no timeout — suppression-clear is a discrete event).

**Eligibility-drop debounce note (S12)**: Transitions from GUIDANCE_ACTIVE and ALIGNED to FACES_DETECTED_NONE_ELIGIBLE SHOULD use `publishExitHoldMs` (§12.1, default 200ms) to avoid UI flicker when eye-open probability or pose oscillates near the sanitizer threshold boundary. SUBJECT_STABILIZING and SUPPRESSED do not need this debounce (subject not yet published / already suppressed, respectively).

### 8.3 Guidance Reducer Contract

```kotlin
interface PortraitGuidanceReducer {
    fun reduce(input: GuidanceReducerInput): GuidanceReducerOutput
}

data class GuidanceReducerInput(
    val nowMs: Long,
    val pipelineResult: PortraitSubjectPipelineResult,
    val ruleResults: List<PortraitRuleResult>,
    val previousState: PortraitGuidanceState,
    val focusPriorityActive: Boolean,
    val thresholds: PortraitThresholds
)

data class GuidanceReducerOutput(
    val state: PortraitGuidanceState,
    val winningRule: PortraitRuleResult?,
    val publishedSubject: PublishedGuidanceSubject?
)

data class PortraitSubjectPipelineResult(
    val rawFaces: List<RawDetectedFace>,
    val eligible: List<EligibleFace>,
    val rejected: List<RejectedFace>,
    val publishedSubject: PublishedGuidanceSubject?
    // guidanceState removed (S12 CR-B5): pipeline produces sanitization/publishing
    // results only; guidance state is computed solely by GuidanceReducer using
    // GuidanceReducerInput.previousState as single source of truth.
)
```

## 9. Branch B: Face AF Pipeline

### 9.1 AF Enums

```kotlin
enum class FaceAfState {
    IDLE,
    CANDIDATE_STABILIZING,
    ACQUIRING,
    LOCKED,
    HOLDING,
    REACQUIRE_PENDING,
    SUSPENDED
}

enum class AfOwnershipState {
    NO_OWNER,
    OWNER_LOCKED,
    OWNER_SWITCH_PENDING,
    OWNER_LOST_HOLD
}

enum class FaceAfCommandType {
    NONE,
    START_FOCUS,
    CANCEL_FOCUS
}

enum class ReacquireReason {
    FIRST_SUBJECT,
    TRACKING_ID_CHANGED,
    SUBJECT_MOVED,
    SUBJECT_SCALE_CHANGED,
    DEVICE_MOVED,
    SUBJECT_LOST_TIMEOUT,
    OWNER_SWITCHED,
    MANUAL_OVERRIDE
}

enum class OwnerSwitchReason {
    FIRST_OWNER,
    MANUAL_SELECTION,
    OWNER_LOST,
    NEW_FACE_STRONGLY_SUPERIOR
}
```

Note: `SHARPNESS_DROPPED` from AF Spec v0.1 is removed because ML Kit does not provide sharpness; custom computation deferred to Phase 1.5.

### 9.2 AF Data Contracts

```kotlin
data class FocusTarget(
    val point: PointF,                // preview-transformed coordinates
    val type: FocusTargetType
)

data class AfFaceCandidate(
    val trackingId: Int?,
    val boundingBox: RectF,
    val faceSizeRatio: Float,
    val centerDistanceNorm: Float,    // 0.0 = center, 1.0 = edge
    val focusTarget: FocusTarget,
    val guidanceEligible: Boolean     // for telemetry cross-reference only
)

data class FacePriorityScore(
    val trackingId: Int?,
    val totalScore: Float,
    val sizeScore: Float,
    val centerScore: Float,
    val persistenceBonus: Float,
    val manualBoost: Float
)

data class AfOwnerMemory(
    val state: AfOwnershipState = AfOwnershipState.NO_OWNER,
    val ownerTrackingId: Int? = null,
    val ownerSinceMs: Long = 0L,
    val switchCandidateTrackingId: Int? = null,
    val switchCandidateSinceMs: Long = 0L,
    val lastOwnerFocusAtMs: Long = 0L,
    val lastReason: OwnerSwitchReason? = null
)

data class FaceAfMemory(
    val state: FaceAfState = FaceAfState.IDLE,
    val trackingId: Int? = null,
    val lastFocusTarget: FocusTarget? = null,
    val lastFaceCenter: PointF? = null,
    val lastFaceArea: Float? = null,
    val stableFrameCount: Int = 0,
    val lastFocusStartMs: Long = 0L,
    val lastSeenMs: Long = 0L,
    val lastReason: ReacquireReason? = null
)

data class FaceAfCommand(
    val type: FaceAfCommandType,
    val target: FocusTarget? = null,
    val reason: ReacquireReason? = null
)
```

### 9.2.1 RawDetectedFace → AfFaceCandidate Mapping Contract

Every frame, the AF pipeline converts each `RawDetectedFace` into an `AfFaceCandidate`. This mapping is deterministic and stateless.

```kotlin
fun RawDetectedFace.toAfCandidate(
    previewBounds: RectF,
    guidanceEligible: Boolean = false  // filled by Branch A result; telemetry-only
): AfFaceCandidate {
    val previewCenterX = previewBounds.centerX()
    val previewCenterY = previewBounds.centerY()
    val previewDiagonal = hypot(previewBounds.width(), previewBounds.height())
    val bboxCenterX = boundingBox.centerX()
    val bboxCenterY = boundingBox.centerY()

    val centerDist = hypot(bboxCenterX - previewCenterX, bboxCenterY - previewCenterY)

    return AfFaceCandidate(
        trackingId = trackingId,
        boundingBox = boundingBox,
        faceSizeRatio = faceSizeRatio,
        centerDistanceNorm = (centerDist / previewDiagonal).coerceIn(0f, 1f),
        focusTarget = selectFocusTarget(this),
        guidanceEligible = guidanceEligible
    )
}
```

**Field mapping rules**:

| AfFaceCandidate field | Source | Computation |
|----------------------|--------|-------------|
| trackingId | `raw.trackingId` | Direct pass-through |
| boundingBox | `raw.boundingBox` | Direct (already preview-transformed) |
| faceSizeRatio | `raw.faceSizeRatio` | Direct |
| centerDistanceNorm | Computed | `distance(bboxCenter, previewCenter) / previewDiagonal` |
| focusTarget | Computed + D-23 fallback | See fallback chain below |
| guidanceEligible | Branch A sanitizer result | Filled after sanitizer runs; defaults to false |

### 9.2.2 Branch A/B Mapping Note

`AfFaceCandidate.guidanceEligible` exists for telemetry cross-reference and future extensibility, but Branch B must not require Branch A completion in order to run.

The mapping implication is important: a true `guidanceEligible` value can only be known after Branch A sanitizer has evaluated `RawDetectedFace` against eye-open, ghost-face, boundary, and pose rules.

To avoid introducing a hard execution-order dependency from Branch A to Branch B, MVP treats `guidanceEligible` as a telemetry-first field with neutral fallback behavior:

- If sanitizer output is available for the same frame, `AfFaceCandidate.guidanceEligible` may be populated from the matching `EligibleFace` result.
- If sanitizer output is not yet available, AF mapping must still proceed, and `guidanceEligible` must default to `false` without blocking owner ranking or AF state reduction.
- `MultiFaceAfOwnerReducer` and `FaceAfReducer` must not stall, skip, or reorder evaluation while waiting for Branch A output.
- In MVP v0.2, `guidanceEligible` must not directly alter AF ownership, AF reacquire, or focus command emission; it is observational only.

This preserves the architecture stated in §3: Branch A and Branch B share the same `List<RawDetectedFace>` input, but remain independently executable pipelines.

If a future version wants to bias AF ownership toward guidance-eligible faces, that behavior must be added as an explicit additive score term in AF ranking, with neutral behavior when guidance eligibility is unknown.

Future rule for such an extension:

- The bonus must be threshold-governed, not hardcoded.
- The absence of sanitizer output must behave as “no bonus,” not as a pipeline error.
- AF execution must remain valid even if Branch A is disabled, degraded, delayed, or temporarily unavailable on a given frame.

This note exists to prevent accidental re-coupling of the AF pipeline to guidance execution order through an apparently harmless shared boolean field.

### 9.2.3 FocusTarget Selection (D-23 amended by D-27)

**MVP (D-27)**: `LANDMARK_ALL` is disabled. `selectFocusTarget` returns `FACE_CENTER` directly on every call — no landmark branch needed.

```kotlin
fun selectFocusTarget(raw: RawDetectedFace): FocusTarget {
    // D-27: LANDMARK_ALL disabled for MVP; landmarks always null.
    // NEAR_EYE and NOSE_BRIDGE deferred to Phase 1.5.
    return FocusTarget(
        PointF(raw.boundingBox.centerX(), raw.boundingBox.centerY()),
        FocusTargetType.FACE_CENTER
    )
}
```

**Phase 1.5 target**: When LANDMARK_ALL is re-enabled, restore the full fallback chain `NEAR_EYE → NOSE_BRIDGE → FACE_CENTER` (highest precision to always-available bbox center). At that point, `FocusTargetType.NEAR_EYE`, `EYE_CENTER`, and `NOSE_BRIDGE` are also re-enabled in the enum (§5).

### 9.3 AF Owner Reducer Contract

```kotlin
sealed interface OwnerDecision {
    data class KeepOwner(val trackingId: Int?) : OwnerDecision
    data class SwitchOwner(val trackingId: Int?, val reason: OwnerSwitchReason) : OwnerDecision
    data object ClearOwner : OwnerDecision
}

interface MultiFaceAfOwnerReducer {
    fun reduce(
        memory: AfOwnerMemory,
        nowMs: Long,
        rankedFaces: List<FacePriorityScore>,
        manualSelectedTrackingId: Int?
    ): Pair<AfOwnerMemory, OwnerDecision>
}
```

Ownership behavior rules:

| Condition | Behavior |
|-----------|----------|
| No owner + at least one face | Assign first owner → OWNER_LOCKED |
| New face slightly higher score | Keep current owner (prevent one-frame preemption) |
| New face exceeds superiority margin but hold not passed | Enter OWNER_SWITCH_PENDING |
| New face remains superior past switch hold + cooldown | Switch owner |
| Manual selection present | Override immediately |
| No candidates < ownerLostTimeoutMs | Enter OWNER_LOST_HOLD |
| No candidates ≥ ownerLostTimeoutMs | Clear owner |

### 9.4 AF State Reducer Contract

```kotlin
interface FaceAfReducer {
    fun reduce(
        memory: FaceAfMemory,
        nowMs: Long,
        ownerFace: AfFaceCandidate?,
        deviceMotionScore: Float
    ): Pair<FaceAfMemory, FaceAfCommand>
}
```

AF state behavior:

| State | Input | Output |
|-------|-------|--------|
| IDLE | Owner appears | → CANDIDATE_STABILIZING, emit NONE |
| CANDIDATE_STABILIZING | Same owner stable N frames | → ACQUIRING, emit START_FOCUS |
| ACQUIRING | Focus dispatched | → LOCKED, emit NONE |
| LOCKED / HOLDING | No reacquire trigger | Stay HOLDING, emit NONE |
| LOCKED / HOLDING | Reacquire triggered + cooldown passed | → REACQUIRE_PENDING, emit CANCEL_FOCUS |
| REACQUIRE_PENDING | Owner still valid | → ACQUIRING, emit START_FOCUS |
| Any active | Owner lost beyond timeout | → SUSPENDED, emit CANCEL_FOCUS |
| SUSPENDED | Owner reappears | → CANDIDATE_STABILIZING, emit NONE |

## 10. AF Coordinator — Auto AF ↔ Manual Focus Bridge

The AF Coordinator arbitrates between automatic face-driven AF (Branch B) and user-initiated Tap-to-Focus (SSOT Appendix A).

```kotlin
enum class AfMode {
    AUTO_FACE,      // FaceAfReducer driving
    MANUAL_TAP,     // User tap → Focus Ring lifecycle (SSOT App A)
    LOCKED          // User long-press AE/AF Lock (SSOT A.2.4)
}

interface AfCoordinator {
    fun onFaceAfCommand(command: FaceAfCommand): CameraAfAction
    fun onUserTap(point: PointF): CameraAfAction
    fun onUserLongPress(point: PointF): CameraAfAction
    fun onFocusResult(success: Boolean)
    fun onImuDrift(driftDeg: Float)
    val currentMode: AfMode
}

sealed interface CameraAfAction {
    data class StartFocusAndMetering(val point: PointF, val flags: Int) : CameraAfAction
    data object CancelFocusAndMetering : CameraAfAction
    data object NoOp : CameraAfAction
}
```

Priority rules:

| User action | Effect on auto AF |
|-------------|-------------------|
| Tap anywhere | Suspend auto AF → enter MANUAL_TAP → show Focus Ring (SSOT A.2.1) |
| Long press | Suspend auto AF → enter LOCKED → show Locked Ring + Badge (SSOT A.2.4) |
| Focus Ring fades (drift ≥ 15° or re-tap) | Resume auto AF → enter AUTO_FACE |
| No user interaction | AUTO_FACE (FaceAfReducer drives) |

Manual tap priority aligns with SSOT A.4 re-tap behavior and SSOT Appendix A Focus lifecycle.

## 11. Unified Overlay Model

```kotlin
data class PortraitOverlayModel(
    val guidanceState: PortraitGuidanceState,
    val publishedSubject: PublishedGuidanceSubject?,
    val winningRule: PortraitRuleResult?,
    val halo: HaloModel?,
    val level: LevelModel?,
    val gridVisible: Boolean,
    val guidanceTextKey: String?,
    val showCaptureReadyBreathing: Boolean,
    val afMode: AfMode,
    val focusRingState: FocusRingState?  // SSOT Appendix A visual state
)

data class HaloModel(
    val visible: Boolean,
    val center: PointF?,
    val radiusPx: Float,
    val colorArgb: Long,
    val alpha: Float
)

data class LevelModel(
    val visible: Boolean,
    val aligned: Boolean,
    val rollDeg: Float,
    val alpha: Float,
    val colorArgb: Long
)

enum class FocusRingState {
    NONE,
    FOCUSING,     // SSOT A.2.1
    FOCUSED,      // SSOT A.2.2
    FAILED,       // SSOT A.2.3
    LOCKED_AE_AF  // SSOT A.2.4
}
```

### 11.1 UI State Matrix

| Guidance State | Halo | Level | Grid | Guidance text | Breathing |
|----------------|------|-------|------|---------------|-----------|
| NO_FACE | Off | Low alpha | Optional | Off | Off |
| FACES_DETECTED_NONE_ELIGIBLE | Off or dim | On | Optional | Off | Off |
| SUBJECT_STABILIZING | Dim amber | On | On α0.35 | Off | Off |
| GUIDANCE_ACTIVE | Amber α0.25 | On | On α0.35 | Winning rule message | Off |
| ALIGNED | Green breathing 0.10–0.18 | Green | On or fade | Off | On (2000ms cycle) |
| SUPPRESSED | Fade out | Keep current | Optional | Off | Off |

Layer order per SSOT: 0=Preview, 1=Pitch, 2=Grid, 3=Halo, 4=Focus Ring, 5=Level, 6=Scrim+TopBar.

### 11.2 Overlay Binding Contract

```kotlin
interface PortraitOverlayBinder {
    fun bind(model: PortraitOverlayModel): Unit
}
```

Rules:

- Halo layer renders only from `model.halo` — never from raw face bbox
- Level traction renders from `model.level` — never recomputes roll from IMU
- Focus ring renders from `model.focusRingState` per SSOT Appendix A specs
- Grid visibility is reducer-controlled, not auto-enabled by UI
- Compose animation must not redefine business state

## 12. Threshold Governance

### 12.1 Unified Threshold Container

```kotlin
data class PortraitThresholds(
    // --- Guidance eligibility ---
    val minFaceSizeRatio: Float = 0.15f,          // SSOT §4.4 D-23
    val minEyeOpenProbForGuidance: Float = 0.9f,  // Ghost Face hotfix baseline
    val extremePoseThresholdDeg: Float = 60f,      // yaw beyond this = EXTREME_POSE
    val minBoundaryScore: Float = 0.3f,            // reject faces mostly out of preview

    // --- Subject publishing ---
    val publishEnterHoldMs: Long = 300L,
    val publishExitHoldMs: Long = 200L,
    val subjectLostTimeoutMs: Long = 500L,
    val subjectSwitchMargin: Float = 0.15f,
    val subjectSwitchHoldMs: Long = 500L,

    // --- Composition rules ---
    val levelAlignThresholdDeg: Float = 5f,        // SSOT AmbientConstants.ALIGNED_THRESHOLD_DEG
    val levelFadeRangeDeg: Float = 10f,            // SSOT AmbientConstants.LEVEL_FADE_RANGE_DEG
    val headroomMinRatio: Float = 0.10f,
    val headroomMaxRatio: Float = 0.20f,
    val thirdsToleranceRatio: Float = 0.08f,
    val alignmentStableMs: Long = 500L,

    // --- AF ownership ---
    val afOwnerCooldownMs: Long = 1500L,      // Consumer: MultiFaceAfOwnerReducer (§9.3). Purpose: prevent rapid owner switching when challenger face briefly exceeds superiority margin. NOT interchangeable with afReacquireCooldownMs.
    val afSwitchHoldMs: Long = 800L,
    val afSuperiorityMargin: Float = 12f,
    val afOwnerLostTimeoutMs: Long = 500L,

    // --- AF state ---
    val afCandidateStableFrames: Int = 3,
    val afReacquireCooldownMs: Long = 1500L,  // [REDLINE D-27] Consumer: FaceAfReducer (§9.4). Purpose: prevent redundant focus commands when subject jitters around reacquire threshold. 1500L is provisional lower bound; range 1500–2000ms — lock after real-device testing. NOT interchangeable with afOwnerCooldownMs.
    val afMaxCenterShiftNorm: Float = 0.06f,
    val afMaxAreaChangeRatio: Float = 0.18f,
    val afDeviceMotionTrigger: Float = 1.0f,
    val afPersistenceBonus: Float = 20f,          // AF owner ranking bonus for tracking persistence

    // --- UI timing ---
    val breatheCycleMs: Long = 2000L,              // SSOT AmbientConstants.BREATHE_CYCLE_MS
    val transitionFastMs: Long = 300L              // SSOT AmbientConstants.TRANSITION_FAST_MS
)
```

### 12.2 Tier-Aware Provider

```kotlin
interface PortraitThresholdProvider {
    fun current(tier: DeviceTier): PortraitThresholds
}
```

Rules:

- Default compile-time constants must exist for offline safety
- Device-tier overrides are allowed for timing thresholds only
- Product tuning must not alter data-model shape
- LEGACY tier may increase hold durations and reduce AF reacquire frequency
- All threshold changes go through SSOT amendment first

## 13. Telemetry Contract

### 13.1 Frame Summary Event

| Field | Type | Description |
|-------|------|-------------|
| session_id | String | Camera session |
| frame_ts_ms | Long | Monotonic frame timestamp |
| raw_face_count | Int | Raw detector faces |
| eligible_face_count | Int | After guidance sanitizer |
| rejected_face_count | Int | Rejected by sanitizer |
| published_subject_present | Boolean | Guidance subject exists |
| af_owner_tracking_id | Int? | Current AF owner |
| af_state | String | FaceAfState name |
| af_ownership_state | String | AfOwnershipState name |
| guidance_state | String | PortraitGuidanceState name |
| focus_command_type | String | NONE / START_FOCUS / CANCEL_FOCUS |
| af_mode | String | AUTO_FACE / MANUAL_TAP / LOCKED |
| device_tier | String | FULL / LIMITED / LEGACY |
| top_rule | String? | Winning rule type |
| can_improve | Boolean | Any rule canImprove |

### 13.2 Guidance Drop Event

| Field | Type | Description |
|-------|------|-------------|
| tracking_id | Int? | Face identifier |
| reject_reason | String | GuidanceRejectReason name |
| eye_open_left | Float? | Left eye probability |
| eye_open_right | Float? | Right eye probability |
| face_size_ratio | Float | Face relative size |

### 13.3 AF Reacquire Event

| Field | Type | Description |
|-------|------|-------------|
| tracking_id | Int? | AF owner at reacquire |
| reacquire_reason | String | ReacquireReason name |
| time_since_last_focus_ms | Long | Interval |
| center_shift_norm | Float? | Movement |
| area_change_ratio | Float? | Scale change |
| device_motion_score | Float | IMU input |

### 13.4 Owner Switch Event

| Field | Type | Description |
|-------|------|-------------|
| prev_tracking_id | Int? | Previous owner |
| next_tracking_id | Int? | New owner |
| switch_reason | String | OwnerSwitchReason name |
| score_delta | Float | Score difference |

## 14. Device-Tier Behavior (SSOT §10.3)

| Capability | FULL | LIMITED | LEGACY |
|------------|------|---------|--------|
| ML Kit analysis rate | 10fps | 8fps | 5fps |
| Guidance sanitizer | Full | Full, reduced cadence | Guarded |
| ThirdsRule | Enabled | Enabled | Optional |
| HeadroomRule | Enabled | Enabled | Optional |
| EyeLineRule | Phase 1.5 (D-27) | Phase 1.5 (D-27) | Phase 1.5 (D-27) |
| GazeRule | Enabled (conditional) | Disabled | Disabled |
| Halo | Full animation | Simplified | Static or disabled |
| AF owner switching | Normal cooldown | Extended cooldown | Extended + reduced reacquire |
| Focus Ring animation | Full (SSOT A.7) | No arc rotation | No animation, static appear |

## 15. Testing Requirements

### 15.1 Unit Tests

- Sanitizer drops each GuidanceRejectReason correctly
- Sanitizer passes valid frontal face with open eyes
- Publisher retains stable tracking ID across frames
- Guidance reducer debounce prevents one-frame flicker
- AF owner reducer hysteresis prevents rapid switching
- AF state reducer cooldown prevents focus churn
- AF coordinator: user tap suspends auto AF
- AF coordinator: focus ring fade resumes auto AF
- Overlay binder maps model to render state without raw detector access

### 15.2 Clip-Based Evaluation

- Single face frontal
- Single face mild yaw
- Small face / far subject
- Busy background
- Partial face clipping
- Ghost face / poster / pareidolia hard negatives
- Multi-face ownership contention
- Manual tap during auto face AF

## 16. Implementation Order

1. Freeze data contracts (§6, §9.2) and threshold provider (§12)
2. Implement RawFaceMapper (ML Kit Face → RawDetectedFace with coordinate transform)
3. Implement sanitizer with GhostFaceGuard placeholder
4. Implement guidance subject publisher
5. Implement MVP P0 rule evaluators: LEVEL, THIRDS, HEADROOM
6. Implement guidance reducer with state machine
7. Implement AF owner reducer and AF state reducer
8. Implement AF coordinator (auto + manual bridge)
9. Bind PortraitOverlayModel into Compose layers
10. Add telemetry events
11. Tune thresholds on device-tier matrix
12. Enable MVP P1 rule (GAZE) conditional on headEulerAngleY reliability
13. [Phase 1.5] Re-enable LANDMARK_ALL → restore EYELINE rule + NEAR_EYE/NOSE_BRIDGE focus targets

## 17. Open Items

| # | Item | Blocker? | Owner |
|---|------|----------|-------|
| 1 | Ghost Face Guard heuristics numeric threshold memo | No — placeholder returns false for MVP | ML/Vision |
| 2 | Primary face selection policy for multiple eligible faces (tiebreaker details) | No — largest-first + center is sufficient MVP default | Domain |
| 3 | ~~LANDMARK_ALL crash root-cause (v2.12.0 "Unknown landmark type")~~ | **RESOLVED via D-27** — LANDMARK_ALL → NONE for MVP; EyeLineRule and NEAR_EYE deferred to Phase 1.5; crash risk eliminated | Android ✅ |
| 4 | Grid visibility in all portrait states vs selected states | No — reducer-controlled | Product |
| 5 | AF coordinator resume delay after manual tap focus ring fade | No — can default to immediate resume | AF |
| 6 | ~~Formal D-27 amendment if LANDMARK_ALL proves unstable~~ | **CLOSED** — D-27 signed; LANDMARK_ALL → NONE applied in v0.3 | CSO ✅ |
| 7 | Branch A / Branch B execution-order guardrail for `guidanceEligible` mapping | No — MVP uses telemetry-only + default-false fallback, but future AF score bias must remain weakly coupled to sanitizer output | Domain / AF |

## 18. Acceptance Criteria

This contract is implementation-ready when:

- All types in §6 and §9 are mapped to Kotlin files
- No portrait UI layer consumes raw ML Kit output
- All guidance originates from reducer-published state
- All rejected subjects emit one explicit GuidanceRejectReason
- AF commands go through AfCoordinator, never directly from reducer to CameraX
- Thresholds are centralized in PortraitThresholds, not scattered in evaluators
- Focus Ring visual lifecycle follows SSOT Appendix A exactly
- Device-tier naming is FULL/LIMITED/LEGACY throughout
- Face AF remains executable when Branch A sanitizer or publisher is delayed, degraded, or unavailable; `guidanceEligible` must not create a hard dependency on Branch A completion in MVP v0.3.

## Citation Legend

| Ref | Document | Version |
|-----|----------|---------|
| SSOT | SSOT_v4_0_6.md | v4.0.6 |
| LAYOUT | Cueshot_Layout_Spec_v2_5_1.md | v2.5.1 |
| GF | Ghost-Face-Pareidolia (SPIKE-GF + Perplexity + GPT convergent) | S10 |
| AF-SPEC | FaceAF_Reducer_Spec_v0_1.md | v0.1 |
| KS | portrait_mvp_kotlin_spec_v0_1.md | v0.1 |
| KC | portrait_mvp_kotlin_contract_v0_1.md | v0.1 |
| D-27 | CSO Decision 27 — LANDMARK_ALL → NONE for MVP | 2026-06-12 |

## Appendix: AF Deficiency Cross-Reference

Original deficiency list from UITA S11 audit, with v0.2 resolution status.

| # | Severity | Original Finding | v0.2 Resolution | Contract Section | Status |
|---|----------|-----------------|-----------------|-----------------|--------|
| AF-1 | HIGH | AF ↔ Manual Focus arbitration missing | AfCoordinator with AfMode enum + priority table + SSOT A.2.x alignment | §10 | ✅ Closed |
| AF-2 | HIGH | PortraitFaceCandidate fourth intermediate type | Renamed to AfFaceCandidate; explicit mapping contract from RawDetectedFace with field-by-field rules | §9.2, §9.2.1 | ✅ Closed |
| AF-3 | MEDIUM | sharpnessScore not ML Kit native | Removed SHARPNESS_DROPPED from ReacquireReason; sharpness deferred to Phase 1.5 | §9.1 note | ✅ Closed |
| AF-4 | MEDIUM | NEAR_EYE depends on LANDMARK_ALL without fallback | v0.2: deterministic fallback chain NEAR_EYE → NOSE_BRIDGE → FACE_CENTER. v0.3 (D-27): LANDMARK_ALL disabled; MVP always returns FACE_CENTER directly. | §9.2.3 | ✅ Closed |
| AF-5 | MEDIUM | Citation missing SSOT Appendix A and §4.4 | Source Authority table (§2) + inline SSOT references throughout AF and rule chapters | §2, §7.3, §10, §11 | ✅ Closed |
| AF-6 | LOW | persistenceScore = 20f hard-coded | Renamed to persistenceBonus; added afPersistenceBonus to PortraitThresholds | §9.2, §12.1 | ✅ Closed |

**End of Portrait MVP Kotlin Contract v0.3**
