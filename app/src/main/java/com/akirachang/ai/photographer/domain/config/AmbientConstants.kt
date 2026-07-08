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

    /** Roll/pitch range (degrees) over which Level Traction UI fades in. 0° = invisible, 10° = fully visible. SSOT §2.0.1 D-10. */
    const val LEVEL_FADE_RANGE_DEG = 10f

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

    /** Re-tap Failed flash duration (ms): old ring shows Failed before new Focusing starts (SSOT A.4). */
    const val FOCUS_RETAP_FLASH_MS = 100L

    /** Duration (ms) of the ring color cross-fade animation when state changes. */
    const val FOCUS_COLOR_TRANSITION_MS = 200L

    /** Duration (ms) of one pulse half-cycle for Focused state breathing animation. */
    const val FOCUS_PULSE_CYCLE_MS = 800L

    /** Duration (ms) of arc rotation animation during Focusing state. */
    const val FOCUS_ARC_ROTATION_MS = 400L

    /** Duration (ms) of Locked badge enter animation (scale + alpha). */
    const val FOCUS_BADGE_ENTER_MS = 300L

    /** Failed oscillation keyframe timestamps (ms). */
    const val FOCUS_FAILED_KEYFRAME_1_MS = 60L
    const val FOCUS_FAILED_KEYFRAME_2_MS = 120L
    const val FOCUS_FAILED_KEYFRAME_3_MS = 180L
    const val FOCUS_FAILED_KEYFRAME_4_MS = 240L

    /** Failed state max-expansion keyframe (ms) — peak scale before settle. */
    const val FOCUS_FAILED_EXPAND_MS = 500L

    // ── Halo traction dot (GAP-06-RENDER-DOT-01) ──────────────────────────────

    /** NEW — Halo traction dot diameter (dp). Distinct from Pitch dot — never reuse. SSOT v4.0.7 §2.5 L490. */
    const val HALO_DOT_SIZE_DP = 36f

    /** NEW — Traction dot V-curve alpha valley (dist midpoint). SSOT v4.0.7 §2.5 L494 / 骨架 L461. */
    const val HALO_DOT_ALPHA_VALLEY = 0.35f

    /** NEW — Traction dot V-curve alpha peak (dist extremes + home). SSOT v4.0.7 §2.5 L494 / 骨架 L461. */
    const val HALO_DOT_ALPHA_PEAK = 0.85f

    /** NEW — Traction dot scale when homed (dist=0, strong presence). SSOT v4.0.7 §2.5 L494 / 骨架 L461. */
    const val HALO_DOT_ALIGNED_SCALE = 1.1f

    /** NEW — Traction dot radial-gradient core stop (currentColor). SSOT v4.0.7 §2.5 L490. */
    const val HALO_DOT_GRADIENT_CORE_STOP = 0.6f

    /** NEW — Traction dot radial-gradient edge stop (transparent). SSOT v4.0.7 §2.5 L490. */
    const val HALO_DOT_GRADIENT_EDGE_STOP = 0.8f
}
