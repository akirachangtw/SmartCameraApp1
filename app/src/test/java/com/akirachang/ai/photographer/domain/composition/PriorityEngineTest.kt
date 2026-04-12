package com.akirachang.ai.photographer.domain.composition

import com.akirachang.ai.photographer.domain.model.RuleScore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

/**
 * D5-T2 Unit tests for [PriorityEngine].
 *
 * Verifies SSOT §4.1 priority formula:
 *   priority = rule.weight * ((100 - ruleScore.score) / 100) * rule.confidenceFactor
 *
 * Pure JVM tests — no Robolectric, no Android dependencies.
 * CompositionRule is mocked via Mockito; only weight/confidenceFactor are stubbed.
 */
@RunWith(MockitoJUnitRunner::class)
class PriorityEngineTest {

    private lateinit var engine: PriorityEngine

    @Before
    fun setUp() {
        engine = PriorityEngine()
    }

    // ── Test 1: All aligned → null (breathing-rhythm trigger) ────────────────

    /**
     * When every rule reports isAligned=true, calculateTopInstruction must return null.
     * This signals the UI to activate the breathing-rhythm composable.
     */
    @Test
    fun allAligned_returnsNull() {
        val ruleA = mock(CompositionRule::class.java)
        val ruleB = mock(CompositionRule::class.java)
        // weight/confidenceFactor not stubbed: engine never reads them when all are aligned
        // (filter produces empty list → early return null before accessing rule properties)

        val results = mapOf(
            ruleA to RuleScore(name = "thirds",   score = 95f,  isAligned = true, weight = 0.20f),
            ruleB to RuleScore(name = "headroom", score = 100f, isAligned = true, weight = 0.20f),
        )

        assertNull(engine.calculateTopInstruction(results))
    }

    // ── Test 2: score=100 → priority=0, not selected ─────────────────────────

    /**
     * A misaligned rule with score=100 has priority = weight * 0 * confidenceFactor = 0.
     * It must NOT be selected over a rule with lower score.
     * ruleA priority = 0.20 * 0.0 * 0.80 = 0.0
     * ruleB priority = 0.20 * 0.5 * 0.80 = 0.08   ← winner
     * Expected: ruleB (score=50f).
     */
    @Test
    fun misalignedWithScore100_hasZeroPriority_notSelected() {
        val ruleA = mock(CompositionRule::class.java)
        val ruleB = mock(CompositionRule::class.java)
        `when`(ruleA.weight).thenReturn(0.20f)
        `when`(ruleA.confidenceFactor).thenReturn(0.80f)
        `when`(ruleB.weight).thenReturn(0.20f)
        `when`(ruleB.confidenceFactor).thenReturn(0.80f)

        val scoreA = RuleScore(name = "thirds",   score = 100f, isAligned = false, weight = 0.20f)
        val scoreB = RuleScore(name = "headroom", score = 50f,  isAligned = false, weight = 0.20f)

        val results = mapOf(ruleA to scoreA, ruleB to scoreB)
        val top = engine.calculateTopInstruction(results)

        assertEquals(50f, top?.score ?: -1f, 0.001f)
    }

    // ── Test 3: score=0 → max priority = weight × confidence ─────────────────

    /**
     * A rule with score=0 has priority = weight * 1.0 * confidenceFactor (maximum).
     * ruleA priority = 0.20 * 1.0 * 0.80 = 0.16   ← winner
     * ruleB priority = 0.20 * 0.5 * 0.80 = 0.08
     * Expected: ruleA (score=0f).
     */
    @Test
    fun misalignedWithScore0_hasMaxPriority_selectedFirst() {
        val ruleA = mock(CompositionRule::class.java)
        val ruleB = mock(CompositionRule::class.java)
        `when`(ruleA.weight).thenReturn(0.20f)
        `when`(ruleA.confidenceFactor).thenReturn(0.80f)
        `when`(ruleB.weight).thenReturn(0.20f)
        `when`(ruleB.confidenceFactor).thenReturn(0.80f)

        val scoreA = RuleScore(name = "thirds",   score = 0f,  isAligned = false, weight = 0.20f)
        val scoreB = RuleScore(name = "headroom", score = 50f, isAligned = false, weight = 0.20f)

        val results = mapOf(ruleA to scoreA, ruleB to scoreB)
        val top = engine.calculateTopInstruction(results)

        assertEquals(0f, top?.score ?: -1f, 0.001f)
    }

    // ── Test 4: Multi-rule descending sort → correct TOP 1 ───────────────────

    /**
     * Three misaligned rules with different weights/scores/confidenceFactors.
     * Hand-calculated priorities:
     *   ruleA: 0.40 * (20/100) * 0.95 = 0.40 * 0.20 * 0.95 = 0.076
     *   ruleB: 0.20 * (80/100) * 0.80 = 0.20 * 0.80 * 0.80 = 0.128  ← highest
     *   ruleC: 0.25 * (50/100) * 0.95 = 0.25 * 0.50 * 0.95 = 0.11875
     * Expected winner: ruleB (score=20f, name="ruleB").
     */
    @Test
    fun multiRuleDescendingSort_correctTop1Selected() {
        val ruleA = mock(CompositionRule::class.java)
        val ruleB = mock(CompositionRule::class.java)
        val ruleC = mock(CompositionRule::class.java)
        `when`(ruleA.weight).thenReturn(0.40f)
        `when`(ruleA.confidenceFactor).thenReturn(0.95f)
        `when`(ruleB.weight).thenReturn(0.20f)
        `when`(ruleB.confidenceFactor).thenReturn(0.80f)
        `when`(ruleC.weight).thenReturn(0.25f)
        `when`(ruleC.confidenceFactor).thenReturn(0.95f)

        val scoreA = RuleScore(name = "ruleA", score = 80f, isAligned = false, weight = 0.40f)
        val scoreB = RuleScore(name = "ruleB", score = 20f, isAligned = false, weight = 0.20f)
        val scoreC = RuleScore(name = "ruleC", score = 50f, isAligned = false, weight = 0.25f)

        val results = mapOf(ruleA to scoreA, ruleB to scoreB, ruleC to scoreC)
        val top = engine.calculateTopInstruction(results)

        assertEquals("ruleB", top?.name)
        assertEquals(20f, top?.score ?: -1f, 0.001f)
    }

    // ── Test 5: Mixed aligned/not-aligned → only pick from not-aligned ────────

    /**
     * ruleA has score=0 but isAligned=true — must be excluded from priority contest.
     * Remaining candidates: ruleB (score=30) and ruleC (score=60), both isAligned=false.
     * ruleB priority = 0.20 * (70/100) * 0.80 = 0.112   ← winner
     * ruleC priority = 0.20 * (40/100) * 0.80 = 0.064
     * Expected winner: ruleB (score=30f).
     */
    @Test
    fun mixedAlignedAndMisaligned_onlyMisalignedParticipate() {
        val ruleA = mock(CompositionRule::class.java)
        val ruleB = mock(CompositionRule::class.java)
        val ruleC = mock(CompositionRule::class.java)
        // ruleA: weight/confidenceFactor not stubbed — should not be accessed (isAligned=true)
        `when`(ruleB.weight).thenReturn(0.20f)
        `when`(ruleB.confidenceFactor).thenReturn(0.80f)
        `when`(ruleC.weight).thenReturn(0.20f)
        `when`(ruleC.confidenceFactor).thenReturn(0.80f)

        val scoreA = RuleScore(name = "ruleA", score = 0f,  isAligned = true,  weight = 0.40f)
        val scoreB = RuleScore(name = "ruleB", score = 30f, isAligned = false, weight = 0.20f)
        val scoreC = RuleScore(name = "ruleC", score = 60f, isAligned = false, weight = 0.20f)

        val results = mapOf(ruleA to scoreA, ruleB to scoreB, ruleC to scoreC)
        val top = engine.calculateTopInstruction(results)

        assertEquals("ruleB", top?.name)
        assertEquals(30f, top?.score ?: -1f, 0.001f)
    }
}
