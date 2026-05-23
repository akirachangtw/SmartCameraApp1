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

    // ── Layout proportions ─────────────────────────────────────────────────────

    /** Viewfinder block vertical weight (78.7%) per Stage0 layout spec (664px reference). */
    const val VIEWFINDER_WEIGHT = 0.787f

    /** Control band vertical weight (21.3%) per Stage0 layout spec (180px reference). */
    const val CONTROL_BAND_WEIGHT = 0.213f

    // ── Scrim ──────────────────────────────────────────────────────────────────

    /** Top scrim height in pixels. Stage0 layout spec §2. */
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

    // ── Tap Focus Ring (P05-T2) ───────────────────────────────────────────────

    /** Tap focus ring diameter (dp). SSOT Appendix A. */
    const val FOCUS_RING_SIZE_DP = 60f

    /** Tap focus ring stroke width (dp). SSOT Appendix A. */
    const val FOCUS_RING_STROKE_DP = 1.5f

    /** Locked badge offset from ring edge (dp). SSOT Slice Protocol v2.1: right-top -4dp,-4dp. */
    const val FOCUS_RING_BADGE_OFFSET_DP = 4f

    // ── Tap Focus timing / thresholds (P05-T3) ───────────────────────────────

    /** Focusing → Failed if CameraX AF callback is absent after this window (ms). */
    const val FOCUS_AF_TIMEOUT_MS = 2000L

    /** Focused → Idle fadeout duration (ms) after IMU drift ≥ FOCUS_DRIFT_THRESHOLD_DEG. */
    const val FOCUS_FADEOUT_MS = 500L

    /** Failed state hold duration (ms) before beginning fadeout → Idle. */
    const val FOCUS_FAILED_MS = 800L

    /** Minimum long-press duration (ms) to trigger Locked + AE/AF lock. */
    const val FOCUS_LONG_PRESS_MS = 500L

    /** IMU drift threshold (degrees): Focused → fadeout → Idle when pitch+roll deviation exceeds this. */
    const val FOCUS_DRIFT_THRESHOLD_DEG = 15f
}
