package com.akirachang.ai.photographer.domain.model.guidance

/**
 * Six-state UI-facing guidance contract. CameraViewModel is the sole authority for emission.
 *
 * STATE-CONTRACT-01 AI-1: SuppressionReason is an internal detail; it does NOT constitute
 * a seventh public state. Consumers branch on the sealed class variant, not on the reason.
 */
sealed class GuidanceState {
    /** No faces detected in the current frame. Halo hidden. */
    object NoFace : GuidanceState()

    /** Raw faces detected, but none passed the HOTFIX-47 eligibility gate (eye-open probability). */
    object FacesDetectedNoneEligible : GuidanceState()

    /**
     * Eligible face(s) detected for the first [GuidanceStateConstants.ALIGNED_DWELL_FRAMES] frames.
     * Renders a low-alpha face-following halo (no directional arc) to indicate detection without
     * committing to full guidance.
     */
    object SubjectStabilizing : GuidanceState()

    /**
     * Eligible face(s) detected; composition score < [GuidanceStateConstants.ALIGNED_ENTER_SCORE].
     * Renders amber guidance halo with EyeLine guide.
     * Under LANDMARK_NONE, EyeLine visual overlay may be unavailable/degraded, but semantic
     * fallback scoring still participates.
     */
    object GuidanceActive : GuidanceState()

    /**
     * Composition weighted score ≥ [GuidanceStateConstants.ALIGNED_ENTER_SCORE] and held for
     * [GuidanceStateConstants.ALIGNED_DWELL_FRAMES]. Renders green breathing halo.
     */
    object Aligned : GuidanceState()

    /**
     * Guidance output is temporarily frozen due to [reason]. Renders muted halo.
     * @param reason Internal suppression cause — do not surface directly in UI.
     */
    data class Suppressed(val reason: SuppressionReason) : GuidanceState()
}

enum class SuppressionReason {
    AF_IN_PROGRESS,
    PTF_GAZE_AF_DISABLED,
    ALIGNMENT_BOUNDARY_OSCILLATION,
}
