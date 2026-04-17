package com.akirachang.ai.photographer.domain.composition.rules

import com.akirachang.ai.photographer.domain.composition.PtfRuleName
import com.akirachang.ai.photographer.domain.model.FaceLandmarkData
import kotlin.math.sqrt

/**
 * PTF Framing Rule — face bbox center must be within [THRESHOLD] Euclidean distance
 * of the normalized frame center (0.5, 0.5).
 *
 * Passes when: sqrt((cx - 0.5)² + (cy - 0.5)²) < 0.12
 */
object PtfFramingRule {

    const val THRESHOLD = 0.12f
    val RULE_NAME = PtfRuleName.FRAMING

    fun evaluate(face: FaceLandmarkData): Boolean {
        val cx = (face.boundingBoxNormalized.left + face.boundingBoxNormalized.right) / 2f
        val cy = (face.boundingBoxNormalized.top + face.boundingBoxNormalized.bottom) / 2f
        val dx = 0.5f - cx
        val dy = 0.5f - cy
        return sqrt(dx * dx + dy * dy) < THRESHOLD
    }
}
