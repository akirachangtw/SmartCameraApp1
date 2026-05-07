package com.akirachang.ai.photographer.domain.config

/**
 * AmbientConstants — Cueshot Stage0 global constant SSOT.
 *
 * Single authoritative source for all Cueshot-specific thresholds, timing values,
 * and UI geometry constants. Sourced from Cueshot_Merged_Spec_v3_4_1_Stage0.md PART 1.
 *
 * Do NOT add non-Cueshot app constants here — see [com.akirachang.ai.photographer.util.Constants].
 */
object AmbientConstants {

    // ── Alignment ──────────────────────────────────────────────────────────────

    /** Roll/pitch deviation threshold (degrees) at which the device is considered aligned. All scenes. */
    const val ALIGNED_THRESHOLD_DEG = 5f

    // ── Level Traction fade ────────────────────────────────────────────────────

    /** Roll/pitch range (degrees) over which Level Traction UI fades in. 0° = invisible, 5° = fully visible. */
    const val LEVEL_FADE_RANGE_DEG = 5f

    // ── Scrim ──────────────────────────────────────────────────────────────────

    /** Bottom scrim height in pixels. Override: 72→120 per Stage0 spec (pending real-device validation). */
    const val SCRIM_HEIGHT_PX = 120f

    // ── Scene hysteresis ───────────────────────────────────────────────────────

    /** Minimum time (ms) between scene-mode transitions to prevent rapid toggling. */
    const val HYSTERESIS_SCENE_MS = 500L

    /** Lower pitch bound (degrees) for the 45°→90° FoodMode hysteresis band. */
    const val HYSTERESIS_PITCH_LOW_DEG = 70f

    /** Upper pitch bound (degrees) for the 45°→90° FoodMode hysteresis band. */
    const val HYSTERESIS_PITCH_HIGH_DEG = 80f

    // ── Animation timing ──────────────────────────────────────────────────────

    /** Duration (ms) of the full aligned-breathing animation cycle (half-cycle each direction). */
    const val BREATHE_CYCLE_MS = 2000L

    /** Duration (ms) of fast UI transitions (PTF color change, Halo fade). */
    const val TRANSITION_FAST_MS = 300L

    /** Duration (ms) of the shutter flash overlay animation on capture. */
    const val SHUTTER_FLASH_DURATION_MS = 300L
}
