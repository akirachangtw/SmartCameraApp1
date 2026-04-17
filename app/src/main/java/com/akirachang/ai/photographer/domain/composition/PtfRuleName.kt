package com.akirachang.ai.photographer.domain.composition

/**
 * Identifies each PTF composition rule that can fail.
 *
 * Used in [PtfAlignmentResult.failedRules] to allow the UI layer to map
 * specific rule failures to targeted visual feedback.
 *
 * Priority order for UI display: FRAMING > ANGLE > LEVEL
 */
enum class PtfRuleName {
    /** Face bounding box center is not sufficiently close to frame center. */
    FRAMING,

    /** Head yaw angle exceeds tolerance (face not looking toward camera). */
    ANGLE,

    /** Device roll exceeds tolerance (camera not level). */
    LEVEL,
}
