package com.akirachang.ai.photographer.domain.camera

/**
 * Top-level camera shooting mode — Cueshot D7-B2-FIX.
 *
 * Represents the user-selected or IMU-inferred operational mode of the camera.
 * All scene-mode logic in ViewModel, Rule Engine, and UI must branch on this type.
 *
 * ## Extension contract (KAIROS #3 candidate rule)
 * When adding a new mode:
 * 1. Add a new subtype here.
 * 2. Update `CameraViewModel.setCameraMode()` with the corresponding transition logic.
 * 3. Add a UI mask filter in `CameraScreen` (hide/show overlays as required by SSOT §2).
 * 4. Register a corresponding `CompositionRule` or exclusion in `CompositionModule`.
 *
 * ## Mode Priority (SSOT §1.1)
 * ```
 * PassToFriend (user_selected) → highest priority
 * Food        (pitch > 35°, IMU auto)
 * Portrait    (pitch ∈ [-10°, 35°], face ≥ 1, ML Kit)
 * Normal      (fallback / default)
 * ```
 *
 * Note: Food, Portrait are reserved for D9+ implementation.
 */
sealed interface CameraMode {

    /** Default shooting mode. Composition guidance and Halo are fully active. */
    object Normal : CameraMode

    /**
     * Pass-To-Friend mode — user hands the phone to a subject.
     * Activated via [CameraTabBar] toggle; deactivated by tapping the same tab.
     * Composition overlays are hidden; Edge Warmth and BreathingRing remain active.
     *
     * Shutter cooldown (Rule 16) is enforced by [CameraViewModel] using a private
     * [lastCaptureTimeMs] sentinel — enterTimeMs is NOT stored here.
     */
    object PassToFriend : CameraMode

    // Reserved for D9+:
    // object Food : CameraMode
    // object Portrait : CameraMode
}
