package com.akirachang.ai.photographer.domain.composition.rules

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PtfLevelRuleTest {

    // ── Positive ─────────────────────────────────────────────────────────────

    @Test
    fun `roll at zero passes`() {
        assertTrue(PtfLevelRule.evaluate(0f))
    }

    @Test
    fun `roll just inside positive boundary 7_9 passes`() {
        assertTrue(PtfLevelRule.evaluate(7.9f))
    }

    @Test
    fun `roll just inside negative boundary -7_9 passes`() {
        assertTrue(PtfLevelRule.evaluate(-7.9f))
    }

    // ── Negative ─────────────────────────────────────────────────────────────

    @Test
    fun `roll exactly at boundary 8_0 fails`() {
        assertFalse(PtfLevelRule.evaluate(8f))
    }

    @Test
    fun `roll exactly at negative boundary -8_0 fails`() {
        assertFalse(PtfLevelRule.evaluate(-8f))
    }

    @Test
    fun `roll beyond boundary 8_1 fails`() {
        assertFalse(PtfLevelRule.evaluate(8.1f))
    }

    @Test
    fun `roll far beyond boundary 45 fails`() {
        assertFalse(PtfLevelRule.evaluate(45f))
    }
}
