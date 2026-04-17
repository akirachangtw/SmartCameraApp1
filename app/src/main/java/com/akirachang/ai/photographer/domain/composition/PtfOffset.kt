package com.akirachang.ai.photographer.domain.composition

/**
 * Normalized displacement vector in PTF rule engine output.
 *
 * Both axes are in [0.0, 1.0] image space (origin = top-left).
 * A positive dx means the face center is to the left of the frame center
 * (i.e. the subject should move right). Analogously for dy.
 *
 * UI layer is responsible for multiplying by viewport size to obtain px values.
 */
data class PtfOffset(val dx: Float, val dy: Float) {
    companion object {
        val ZERO = PtfOffset(0f, 0f)
    }
}
