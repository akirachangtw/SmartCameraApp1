package com.akirachang.ai.photographer.domain.composition

import com.akirachang.ai.photographer.domain.model.FaceLandmarkData

/**
 * Result of a single composition rule evaluation.
 *
 * @param score     Normalized score [0.0, 100.0]. Higher = better composition.
 * @param isAligned True when the subject satisfies this rule's alignment criterion.
 */
data class RuleResult(val score: Float, val isAligned: Boolean)

/**
 * Contract for all Cueshot composition rules.
 *
 * Rules are stateless, thread-safe, and UI-free — pure domain logic only.
 * Each rule receives pre-computed geometry and returns a [RuleResult].
 *
 * Implementations (ThirdsRule, HeadroomRule, EyeLineRule, GazeRule) are
 * added in D2+. This interface is the stable contract they must satisfy.
 *
 * Wiring: rules are injected as a [Set] into [AnalyzeCompositionUseCase]
 * via Hilt @Multibinds (configured in D2 DI module).
 */
interface CompositionRule {
    /** Stable identifier used as key in [CompositionResult.ruleScores]. Must be unique per rule. */
    val name: String

    /**
     * Base weight for priority calculation in [PriorityEngine]. Range: (0.0, 1.0].
     * Higher weight = this rule's misalignment is penalized more heavily.
     * Also used for weighted-average total score.
     */
    val weight: Float

    /**
     * Hardware/ML confidence factor for this rule. Range: [0.0, 1.0].
     * Rules that depend heavily on ML Kit detection (e.g. face detection) should
     * use a lower factor to dampen priority when detection reliability is uncertain.
     * Pure IMU-driven rules may use 1.0.
     *
     * Used in [PriorityEngine] formula:
     *   priority = weight * ((100 - score) / 100) * confidenceFactor
     */
    val confidenceFactor: Float

    /**
     * 評估構圖規則。
     *
     * @param faces 偵測到的人臉資料列表（含邊界框與 Landmark）。
     * ⚠️ 嚴格契約：[FaceLandmarkData.boundingBoxNormalized] 及所有座標欄位
     * 必須是 [0.0, 1.0] 的正規化 (Normalized) 數值。
     * 左上角為 (0.0, 0.0)，右下角為 (1.0, 1.0)。
     * 絕對不可傳入 UI Viewport 或 Sensor 的絕對像素 (Pixel) 座標。
     * 呼叫端（AnalyzeCompositionUseCase）負責在傳入前完成座標正規化，
     * Rule 實作不得在內部進行任何像素轉換或容錯補償。
     * @param imuPitch 設備俯仰角 (度)，向上傾斜為正值。
     * @param imuRoll 設備翻滾角 (度)，向右翻滾為正值。
     * @return [RuleResult] 包含分數與對齊旗標。永不拋出例外。
     */
    fun evaluate(faces: List<FaceLandmarkData>, imuPitch: Float, imuRoll: Float): RuleResult
}
