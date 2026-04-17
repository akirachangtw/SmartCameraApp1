package com.akirachang.ai.photographer.domain.composition.rules

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PtfAngleRuleTest {

    // ── Positive ─────────────────────────────────────────────────────────────

    @Test
    fun `pitch at lower boundary -15 passes`() {
        assertTrue(PtfAngleRule.evaluate(-15f))
    }

    @Test
    fun `pitch at upper boundary 25 passes`() {
        assertTrue(PtfAngleRule.evaluate(25f))
    }

    @Test
    fun `pitch at zero passes`() {
        assertTrue(PtfAngleRule.evaluate(0f))
    }

    @Test
    fun `pitch at midpoint 5 passes`() {
        assertTrue(PtfAngleRule.evaluate(5f))
    }

    // ── Negative ─────────────────────────────────────────────────────────────

    @Test
    fun `pitch just below lower boundary -15_1 fails`() {
        assertFalse(PtfAngleRule.evaluate(-15.1f))
    }

    @Test
    fun `pitch just above upper boundary 25_1 fails`() {
        assertFalse(PtfAngleRule.evaluate(25.1f))
    }

    @Test
    fun `pitch extremely negative fails`() {
        assertFalse(PtfAngleRule.evaluate(-90f))
    }

    @Test
    fun `pitch extremely positive fails`() {
        assertFalse(PtfAngleRule.evaluate(90f))
    }
}
