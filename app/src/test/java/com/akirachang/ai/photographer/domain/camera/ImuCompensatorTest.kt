package com.akirachang.ai.photographer.domain.camera

import com.akirachang.ai.photographer.domain.sensor.GyroscopeData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * D3-T4 Boundary tests for ImuCompensator.
 *
 * KAIROS Rule 10: verify fail-safe / silent-degradation behaviour at extreme focal length inputs.
 * The sentinel value focalLengthPixels=0f must produce (0,0) displacement — no NaN, no crash.
 */
class ImuCompensatorTest {

    private lateinit var compensator: ImuCompensator

    // Bootstrap sample (timestamp must be non-zero so the bootstrap stores a real reference).
    // accumulate() skips integration on the very first call (when lastTimestampNs == 0L it
    // stores the timestamp and returns early — so timestamp=0L would leave lastTimestampNs=0).
    private val sampleBootstrap = GyroscopeData(x = 0f, y = 0f, z = 0f, timestamp = 1_000_000L) // t=1ms
    // Real sample 10ms later — dt = 0.01s, within [DT_MIN=0, DT_MAX=0.1] → accepted.
    private val sample1 = GyroscopeData(x = 1f, y = 0.5f, z = 0f, timestamp = 11_000_000L)  // t=11ms
    // Second real sample another 10ms later.
    private val sample2 = GyroscopeData(x = 1f, y = 0.5f, z = 0f, timestamp = 21_000_000L)  // t=21ms

    @Before
    fun setup() {
        compensator = ImuCompensator()
    }

    // -----------------------------------------------------------------------------------------
    // Zero focal length sentinel — ImuCompensator must return (0,0) silently
    // -----------------------------------------------------------------------------------------

    @Test
    fun `consumeCameraMotion with focalLengthPixels 0f returns (0,0) even after accumulation`() {
        // Simulate real gyro data accumulated before camera is bound (factor unavailable).
        compensator.accumulate(sampleBootstrap) // bootstrap only — no displacement
        compensator.accumulate(sample1)         // first real accumulation
        compensator.accumulate(sample2)         // second real accumulation

        val (dx, dy) = compensator.consumeCameraMotion(focalLengthPixels = 0f)

        assertEquals("dx must be 0f when focal length is 0", 0f, dx, 0f)
        assertEquals("dy must be 0f when focal length is 0", 0f, dy, 0f)
    }

    @Test
    fun `consumeCameraMotion with focalLengthPixels 0f is finite (no NaN or Infinity)`() {
        compensator.accumulate(sampleBootstrap)
        compensator.accumulate(sample1)
        compensator.accumulate(sample2)

        val (dx, dy) = compensator.consumeCameraMotion(focalLengthPixels = 0f)

        assertTrue("dx must be finite", dx.isFinite())
        assertTrue("dy must be finite", dy.isFinite())
    }

    // -----------------------------------------------------------------------------------------
    // Normal operation — verify formula correctness
    // -----------------------------------------------------------------------------------------

    @Test
    fun `consumeCameraMotion with known focal length produces correct displacement`() {
        // Bootstrap then one real sample (dt = 10ms = 0.01s)
        compensator.accumulate(sampleBootstrap) // bootstrap: lastTimestampNs = 1_000_000L, no accumulation
        compensator.accumulate(sample1)         // dt = (11_000_000 - 1_000_000) / 1e9 = 0.01s

        // Expected: omegaY=0.5, dt=0.01 → accumulatedOmegaYDt = 0.005
        //           omegaX=1.0, dt=0.01 → accumulatedOmegaXDt = 0.01
        // focalLengthPixels = 1000f
        // dx = -0.005 * 1000 = -5f
        // dy = +0.01  * 1000 = +10f
        val (dx, dy) = compensator.consumeCameraMotion(focalLengthPixels = 1000f)

        assertEquals(-5f, dx, 0.001f)
        assertEquals(10f, dy, 0.001f)
    }

    @Test
    fun `consumeCameraMotion resets accumulator after consume`() {
        compensator.accumulate(sampleBootstrap)
        compensator.accumulate(sample1)

        // First consume drains accumulator
        compensator.consumeCameraMotion(focalLengthPixels = 1000f)

        // Second consume must return (0,0) — accumulator is empty
        val (dx, dy) = compensator.consumeCameraMotion(focalLengthPixels = 1000f)
        assertEquals("dx must be 0 after accumulator reset", 0f, dx, 0f)
        assertEquals("dy must be 0 after accumulator reset", 0f, dy, 0f)
    }

    // -----------------------------------------------------------------------------------------
    // No-sample baseline — fresh compensator with no gyro input
    // -----------------------------------------------------------------------------------------

    @Test
    fun `consumeCameraMotion with no accumulated samples returns (0,0)`() {
        val (dx, dy) = compensator.consumeCameraMotion(focalLengthPixels = 1000f)
        assertEquals(0f, dx, 0f)
        assertEquals(0f, dy, 0f)
    }

    // -----------------------------------------------------------------------------------------
    // Extreme focal length inputs — must not produce NaN/Infinity
    // -----------------------------------------------------------------------------------------

    @Test
    fun `consumeCameraMotion with large but realistic focal length is finite`() {
        // 10000f is well above any real-device focal length in pixels (~800-1200px typical).
        // Verifies no overflow for implausibly large (but non-MAX_VALUE) inputs.
        compensator.accumulate(sampleBootstrap)
        compensator.accumulate(sample1)

        val (dx, dy) = compensator.consumeCameraMotion(focalLengthPixels = 10000f)

        assertTrue("dx must be finite for large focal length", dx.isFinite())
        assertTrue("dy must be finite for large focal length", dy.isFinite())
    }
}
