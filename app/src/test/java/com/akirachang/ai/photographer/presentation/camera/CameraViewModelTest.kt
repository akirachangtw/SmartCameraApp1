package com.akirachang.ai.photographer.presentation.camera

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * D3-T4 Negative tests: boundary defence for focal length computation and device roll state.
 *
 * KAIROS Rule 10 mandate: boundary conditions must be explicitly verified to prevent NaN crashes,
 * out-of-bounds values, or state contamination in the IMU compensation pipeline.
 *
 * Scope: CameraViewModel.computeFocalLengthPixels() (companion object — no DI required)
 *        CameraState.deviceRoll (pure data class — no DI required)
 */
class CameraViewModelTest {

    // -----------------------------------------------------------------------------------------
    // computeFocalLengthPixels — focal length boundary tests
    // -----------------------------------------------------------------------------------------

    @Test
    fun `computeFocalLengthPixels returns 0f when previewWidth is 0 and factor is null`() {
        // Sentinel contract: 0f tells ImuCompensator "not ready" → returns (0,0) silently.
        val result = CameraViewModel.computeFocalLengthPixels(factor = null, previewWidth = 0)
        assertEquals(0f, result, 0f)
    }

    @Test
    fun `computeFocalLengthPixels returns 0f when previewWidth is 0 and factor is provided`() {
        // Even if hardware factor is available, width=0 means layout not measured yet.
        // fail-safe: 0 / 2f = 0f (hardware path skipped because width <= 0).
        val result = CameraViewModel.computeFocalLengthPixels(factor = 0.8f, previewWidth = 0)
        assertEquals(0f, result, 0f)
    }

    @Test
    fun `computeFocalLengthPixels uses fail-safe (width div 2f) when factor is null`() {
        // Fail-safe path: camera unbound, no factor available.
        // Expected: previewWidth / 2f = 1920 / 2f = 960f
        val result = CameraViewModel.computeFocalLengthPixels(factor = null, previewWidth = 1920)
        assertEquals(960f, result, 0f)
    }

    @Test
    fun `computeFocalLengthPixels uses hardware-first path when factor is available`() {
        // Hardware-first path: factor * previewWidth
        // factor = 0.8f, previewWidth = 1920 → 1920 * 0.8f = 1536f
        val result = CameraViewModel.computeFocalLengthPixels(factor = 0.8f, previewWidth = 1920)
        assertEquals(1536f, result, 0.001f)
    }

    @Test
    fun `computeFocalLengthPixels result is never NaN`() {
        // NaN guard: all boundary inputs must produce finite results.
        val inputs = listOf(
            Pair(null, 0),
            Pair(null, 1920),
            Pair(0.0f, 0),
            Pair(0.0f, 1920),
            Pair(0.8f, 0),
            Pair(0.8f, 1920),
        )
        for ((factor, width) in inputs) {
            val result = CameraViewModel.computeFocalLengthPixels(factor, width)
            assertTrue(
                "Expected finite result for factor=$factor width=$width, got $result",
                result.isFinite()
            )
        }
    }

    @Test
    fun `computeFocalLengthPixels with very small factor does not underflow to NaN`() {
        // Extreme hardware factor (physically implausible but must not crash).
        val result = CameraViewModel.computeFocalLengthPixels(factor = Float.MIN_VALUE, previewWidth = 1920)
        assertTrue("Result must be finite", result.isFinite())
        assertTrue("Result must be non-negative", result >= 0f)
    }

    // -----------------------------------------------------------------------------------------
    // CameraState.deviceRoll — roll angle boundary tests
    // -----------------------------------------------------------------------------------------

    @Test
    fun `CameraState deviceRoll accepts extreme positive roll without exception`() {
        // +180 degrees: device fully inverted — must not throw, must store exact value.
        val state = CameraState(deviceRoll = 180f)
        assertEquals(180f, state.deviceRoll, 0f)
    }

    @Test
    fun `CameraState deviceRoll accepts extreme negative roll without exception`() {
        // -180 degrees: device fully inverted in opposite direction.
        val state = CameraState(deviceRoll = -180f)
        assertEquals(-180f, state.deviceRoll, 0f)
    }

    @Test
    fun `CameraState deviceRoll defaults to 0f`() {
        // Default state must be level (0 degrees roll).
        val state = CameraState()
        assertEquals(0f, state.deviceRoll, 0f)
    }

    @Test
    fun `CameraState deviceRoll copy preserves other fields`() {
        // Updating roll via copy() must not contaminate unrelated fields.
        val original = CameraState(isCapturing = true, deviceRoll = 0f)
        val updated = original.copy(deviceRoll = 90f)
        assertEquals(90f, updated.deviceRoll, 0f)
        assertTrue("isCapturing must be preserved", updated.isCapturing)
    }
}
