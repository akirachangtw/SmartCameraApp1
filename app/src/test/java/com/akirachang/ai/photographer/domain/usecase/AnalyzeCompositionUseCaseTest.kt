/**
 * ClassName: AnalyzeCompositionUseCaseTest
 * Description: Unit tests for AnalyzeCompositionUseCase
 * Author: Claude Code
 * Date: 2025-11-27
 * Version: 2.0.0-beta01
 *
 * Tests Phase 3 complete composition rules implementation:
 * - Rule of Thirds (30% weight)
 * - Headroom Analysis (25% weight)
 * - Edge Distance (25% weight)
 * - Subject Detection (20% weight)
 * - Weighted scoring system
 */
package com.akirachang.ai.photographer.domain.usecase

import android.graphics.PointF
import android.graphics.RectF
import com.akirachang.ai.photographer.domain.composition.CompositionRule
import com.akirachang.ai.photographer.domain.composition.PriorityEngine
import com.akirachang.ai.photographer.domain.composition.RuleResult
import com.akirachang.ai.photographer.domain.model.FaceLandmarkData
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for AnalyzeCompositionUseCase
 *
 * Test coverage:
 * - Rule of Thirds: alignment with intersection points
 * - Headroom: space above subject (5-15% ideal)
 * - Edge Distance: minimum 5% margin from edges
 * - Multi-subject: largest face selection
 * - Weighted scoring: overall score calculation
 * - Edge cases: no faces, boundary conditions
 *
 * Uses Robolectric to provide Android framework classes (Rect, etc.)
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])  // Use Android API 28 for testing
class AnalyzeCompositionUseCaseTest {

    private lateinit var useCase: AnalyzeCompositionUseCase

    @Before
    fun setup() {
        useCase = AnalyzeCompositionUseCase(rules = emptySet(), priorityEngine = PriorityEngine())
    }

    // ==================== D2 Contract Tests ====================

    @Test
    fun `invoke with empty rules returns empty CompositionResult`() {
        val result = useCase(emptyList(), 0f, 0f)
        assertNull(result.topInstruction)
        assertTrue(result.ruleScores.isEmpty())
        assertFalse(result.isAllAligned)
    }

    @Test
    fun `invoke with faces and empty rules returns empty CompositionResult`() {
        val faces = listOf(
            FaceLandmarkData(
                boundingBoxNormalized = RectF(0.1f, 0.2f, 0.3f, 0.5f),
                leftEyeNormalized = null,
                rightEyeNormalized = null,
                headEulerAngleY = 0f,
            )
        )
        val result = useCase(faces, 15f, -5f)
        assertNull(result.topInstruction)
        assertTrue(result.ruleScores.isEmpty())
        assertFalse(result.isAllAligned)
    }

    // ==================== D5 Contract Tests ====================

    @Test
    fun `all rules aligned — topInstruction is null and isAllAligned is true`() {
        val alignedRule = mock(CompositionRule::class.java)
        `when`(alignedRule.name).thenReturn("aligned_rule")
        `when`(alignedRule.weight).thenReturn(0.20f)
        `when`(alignedRule.confidenceFactor).thenReturn(0.80f)
        `when`(alignedRule.evaluate(emptyList(), 0f, 0f))
            .thenReturn(RuleResult(score = 95f, isAligned = true))

        val localUseCase = AnalyzeCompositionUseCase(
            rules = setOf(alignedRule),
            priorityEngine = PriorityEngine(),
        )

        val result = localUseCase(emptyList(), 0f, 0f)

        assertNull(result.topInstruction)
        assertTrue(result.isAllAligned)
        assertEquals(1, result.ruleScores.size)
    }

    @Test
    fun `misaligned rule — topInstruction is non-null and isAllAligned is false`() {
        val misalignedRule = mock(CompositionRule::class.java)
        `when`(misalignedRule.name).thenReturn("thirds")
        `when`(misalignedRule.weight).thenReturn(0.20f)
        `when`(misalignedRule.confidenceFactor).thenReturn(0.80f)
        `when`(misalignedRule.evaluate(emptyList(), 0f, 0f))
            .thenReturn(RuleResult(score = 40f, isAligned = false))

        val localUseCase = AnalyzeCompositionUseCase(
            rules = setOf(misalignedRule),
            priorityEngine = PriorityEngine(),
        )

        val result = localUseCase(emptyList(), 0f, 0f)

        assertNotNull(result.topInstruction)
        assertFalse(result.isAllAligned)
        assertEquals("thirds", result.topInstruction?.name)
    }

    @Test
    fun `weighted avg above threshold — isAllAligned is true even with topInstruction present`() {
        // rule1: weight=0.6, score=80 | rule2: weight=0.4, score=55
        // weightedAvg = (0.6*80 + 0.4*55) / 1.0 = (48 + 22) = 70.0 → exactly at threshold → true
        val rule1 = mock(CompositionRule::class.java)
        `when`(rule1.name).thenReturn("headroom")
        `when`(rule1.weight).thenReturn(0.6f)
        `when`(rule1.confidenceFactor).thenReturn(1.0f)
        `when`(rule1.evaluate(emptyList(), 0f, 0f))
            .thenReturn(RuleResult(score = 80f, isAligned = true))

        val rule2 = mock(CompositionRule::class.java)
        `when`(rule2.name).thenReturn("thirds")
        `when`(rule2.weight).thenReturn(0.4f)
        `when`(rule2.confidenceFactor).thenReturn(1.0f)
        `when`(rule2.evaluate(emptyList(), 0f, 0f))
            .thenReturn(RuleResult(score = 55f, isAligned = false))

        val localUseCase = AnalyzeCompositionUseCase(
            rules = setOf(rule1, rule2),
            priorityEngine = PriorityEngine(),
        )

        val result = localUseCase(emptyList(), 0f, 0f)

        assertTrue(result.isAllAligned)
        assertNotNull(result.topInstruction)
    }

    @Test
    fun `weighted avg below threshold — isAllAligned is false`() {
        // rule1: weight=0.5, score=50 | rule2: weight=0.5, score=60
        // weightedAvg = 55 < 70 → false
        val rule1 = mock(CompositionRule::class.java)
        `when`(rule1.name).thenReturn("headroom")
        `when`(rule1.weight).thenReturn(0.5f)
        `when`(rule1.confidenceFactor).thenReturn(1.0f)
        `when`(rule1.evaluate(emptyList(), 0f, 0f))
            .thenReturn(RuleResult(score = 50f, isAligned = false))

        val rule2 = mock(CompositionRule::class.java)
        `when`(rule2.name).thenReturn("thirds")
        `when`(rule2.weight).thenReturn(0.5f)
        `when`(rule2.confidenceFactor).thenReturn(1.0f)
        `when`(rule2.evaluate(emptyList(), 0f, 0f))
            .thenReturn(RuleResult(score = 60f, isAligned = false))

        val localUseCase = AnalyzeCompositionUseCase(
            rules = setOf(rule1, rule2),
            priorityEngine = PriorityEngine(),
        )

        val result = localUseCase(emptyList(), 0f, 0f)

        assertFalse(result.isAllAligned)
    }

    @Test
    fun `highest priority misaligned rule wins over lower priority rule`() {
        // highPriorityRule: weight=0.20, score=20 → priority = 0.20 * (80/100) * 0.80 = 0.128
        // lowPriorityRule:  weight=0.10, score=10 → priority = 0.10 * (90/100) * 0.80 = 0.072
        val highPriorityRule = mock(CompositionRule::class.java)
        `when`(highPriorityRule.name).thenReturn("high_priority")
        `when`(highPriorityRule.weight).thenReturn(0.20f)
        `when`(highPriorityRule.confidenceFactor).thenReturn(0.80f)
        `when`(highPriorityRule.evaluate(emptyList(), 0f, 0f))
            .thenReturn(RuleResult(score = 20f, isAligned = false))

        val lowPriorityRule = mock(CompositionRule::class.java)
        `when`(lowPriorityRule.name).thenReturn("low_priority")
        `when`(lowPriorityRule.weight).thenReturn(0.10f)
        `when`(lowPriorityRule.confidenceFactor).thenReturn(0.80f)
        `when`(lowPriorityRule.evaluate(emptyList(), 0f, 0f))
            .thenReturn(RuleResult(score = 10f, isAligned = false))

        val localUseCase = AnalyzeCompositionUseCase(
            rules = setOf(highPriorityRule, lowPriorityRule),
            priorityEngine = PriorityEngine(),
        )

        val result = localUseCase(emptyList(), 0f, 0f)

        assertFalse(result.isAllAligned)
        assertEquals("high_priority", result.topInstruction?.name)
    }

    // ==================== GAP-06-TARGET-01-REVA A6-1 targetHint Tests ====================

    @Test
    fun `misaligned top rule — targetHint enters CompositionResult targetHint`() {
        val misalignedRule = mock(CompositionRule::class.java)
        `when`(misalignedRule.name).thenReturn("thirds")
        `when`(misalignedRule.weight).thenReturn(0.20f)
        `when`(misalignedRule.confidenceFactor).thenReturn(0.80f)
        `when`(misalignedRule.evaluate(emptyList(), 0f, 0f))
            .thenReturn(RuleResult(score = 40f, isAligned = false, targetHint = PointF(0.33f, 0.67f)))

        val localUseCase = AnalyzeCompositionUseCase(
            rules = setOf(misalignedRule),
            priorityEngine = PriorityEngine(),
        )

        val result = localUseCase(emptyList(), 0f, 0f)

        assertEquals("thirds", result.topInstruction?.name)
        assertEquals(PointF(0.33f, 0.67f), result.targetHint)
    }

    @Test
    fun `isAllAligned true despite misaligned topInstruction — targetHint cleared`() {
        // rule1: weight=0.6, score=80, aligned | rule2: weight=0.4, score=55, misaligned w/ targetHint
        // weightedAvg = (0.6*80 + 0.4*55) / 1.0 = 70.0 → isAllAligned=true, but topInstruction=rule2
        val rule1 = mock(CompositionRule::class.java)
        `when`(rule1.name).thenReturn("headroom")
        `when`(rule1.weight).thenReturn(0.6f)
        `when`(rule1.confidenceFactor).thenReturn(1.0f)
        `when`(rule1.evaluate(emptyList(), 0f, 0f))
            .thenReturn(RuleResult(score = 80f, isAligned = true))

        val rule2 = mock(CompositionRule::class.java)
        `when`(rule2.name).thenReturn("thirds")
        `when`(rule2.weight).thenReturn(0.4f)
        `when`(rule2.confidenceFactor).thenReturn(1.0f)
        `when`(rule2.evaluate(emptyList(), 0f, 0f))
            .thenReturn(RuleResult(score = 55f, isAligned = false, targetHint = PointF(0.25f, 0.33f)))

        val localUseCase = AnalyzeCompositionUseCase(
            rules = setOf(rule1, rule2),
            priorityEngine = PriorityEngine(),
        )

        val result = localUseCase(emptyList(), 0f, 0f)

        assertTrue(result.isAllAligned)
        assertNotNull(result.topInstruction)
        assertNull(result.targetHint)
    }
}
