package com.akirachang.ai.photographer.presentation.camera

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * D1 contract tests for PtfState sealed class equality behaviour.
 * Required by SV-D1-FIX-04: stale-state guard in onTapToFocus() relies on data class ==.
 */
class PtfStateTest {

    @Test
    fun `PtfState equality works correctly`() {
        assertEquals(PtfState.Focused(1f, 2f), PtfState.Focused(1f, 2f))
        assertNotEquals(PtfState.Focused(1f, 2f), PtfState.Focusing(1f, 2f))
        assertNotEquals(PtfState.Focused(1f, 2f), PtfState.Failed(1f, 2f))
        val idle1: PtfState = PtfState.Idle
        val idle2: PtfState = PtfState.Idle
        assertEquals(idle1, idle2)
        assertFalse(idle1 == PtfState.Focused(0f, 0f))
    }

    @Test
    fun `PtfState Focused with different coords are not equal`() {
        assertNotEquals(PtfState.Focused(1f, 2f), PtfState.Focused(3f, 4f))
    }
}
