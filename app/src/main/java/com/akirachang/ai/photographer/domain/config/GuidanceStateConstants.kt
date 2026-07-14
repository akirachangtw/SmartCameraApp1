package com.akirachang.ai.photographer.domain.config

/**
 * Halo alpha/radius constants and hysteresis thresholds for the GuidanceState six-state contract.
 * All consumers MUST reference these constants — no inline magic numbers.
 * STATE-CONTRACT-01 AI-1.
 */
object GuidanceStateConstants {
    /** Amber halo alpha for GuidanceActive state. */
    const val HALO_ACTIVE_ALPHA = 0.8f // R-T2 錨移 0.25→0.8（SSOT §2.5 未對齊靜態，同步 v4.0.7）

    /** Green breathing halo alpha floor for Aligned state. */
    const val HALO_ALIGNED_ALPHA_MIN = 0.25f // R-T13 V-1 對齊呼吸帶重錨 0.10→0.18 ⇒ 0.25→0.40（SSOT §2.5 D-3）

    /** Green breathing halo alpha ceiling for Aligned state. */
    const val HALO_ALIGNED_ALPHA_MAX = 0.40f // R-T13 V-1 對齊呼吸帶重錨 0.10→0.18 ⇒ 0.25→0.40（SSOT §2.5 D-3）

    /** Halo radius = max(bboxW, bboxH) * this scale. */
    const val HALO_RADIUS_SCALE = 0.5f // SSOT §2.5 R-T3：直徑=faceSize；§2.5.4 明禁 0.6（UITA 原稿轉錄筆誤）

    /** Portrait bbox EMA history weight — SSOT §2.5.1 (:536-541) `smoothed = last * 0.7 + new * 0.3`. */
    const val PORTRAIT_EMA_HISTORY = 0.7f

    /** Portrait bbox EMA new-sample weight — SSOT §2.5.1 (:536-541); pairs with [PORTRAIT_EMA_HISTORY]. */
    const val PORTRAIT_EMA_NEW = 0.3f

    /** Weighted composition score threshold to enter Aligned state (0–100 scale). */
    const val ALIGNED_ENTER_SCORE = 75f

    /** Weighted composition score threshold to exit Aligned state (hysteresis). */
    const val ALIGNED_EXIT_SCORE = 65f

    /** Minimum frames to remain in Aligned before exiting, and SubjectStabilizing entry duration. */
    const val ALIGNED_DWELL_FRAMES = 3

    /** Consecutive frames in the [ALIGNED_EXIT_SCORE, ALIGNED_ENTER_SCORE) band before oscillation suppression. */
    const val OSCILLATION_THRESHOLD = 10
}
