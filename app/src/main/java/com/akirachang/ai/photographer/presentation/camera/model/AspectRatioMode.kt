package com.akirachang.ai.photographer.presentation.camera.model

import android.util.Rational
import androidx.camera.core.AspectRatio
import androidx.camera.view.PreviewView

/**
 * 長寬比模式列舉
 *
 * v2.10.0: 實作專業相機長寬比切換功能（基於 UseCaseGroup + ViewPort）
 * v2.12.0: 僅保留 4:3（sensor 原生）與 16:9（螢幕填充），移除 1:1 數位裁切模式
 *
 * 參考: Gemini 方案（UseCaseGroup + ViewPort）
 * - 4:3: FIT_CENTER（黑邊，無裁切）- 類似 Pixel Camera
 * - 16:9: FILL_CENTER（填滿螢幕）
 *
 * @since v2.10.0
 */
enum class AspectRatioMode {
    /**
     * 4:3 標準模式 (全感光元件)
     *
     * 行為:
     * - 使用全部 sensor 區域
     * - PreviewView.ScaleType = FIT_CENTER（顯示黑邊，無裁切）
     * - 完全解決 1.6x 放大感
     * - 類似 Pixel Camera 4:3 模式
     *
     * 適用場景:
     * - 需要最大 FOV（視野）
     * - 專業攝影（全感光元件解析度）
     * - 列印照片（3:2 或 4:3 相紙）
     */
    RATIO_4_3,

    /**
     * 16:9 寬螢幕模式 (預設)
     *
     * 行為:
     * - 裁切 sensor 上下邊緣
     * - PreviewView.ScaleType = FILL_CENTER（填滿螢幕）
     * - 符合現代手機螢幕比例
     *
     * 適用場景:
     * - 觀看影片（16:9 螢幕）
     * - 社交媒體分享
     * - 橫向拍攝
     */
    RATIO_16_9;

    /**
     * 切換到下一個模式 (循環: 4:3 → 16:9 → 4:3)
     */
    fun next(): AspectRatioMode = when (this) {
        RATIO_4_3 -> RATIO_16_9
        RATIO_16_9 -> RATIO_4_3
    }

    /**
     * 顯示名稱（UI 顯示）
     */
    val displayName: String
        get() = when (this) {
            RATIO_4_3 -> "4:3"
            RATIO_16_9 -> "16:9"
        }

    /**
     * 詳細描述
     */
    val description: String
        get() = when (this) {
            RATIO_4_3 -> "標準（全感光元件）"
            RATIO_16_9 -> "寬螢幕（填滿螢幕）"
        }

    /**
     * 轉換為 CameraX AspectRatio 常數
     */
    val toCameraXAspectRatio: Int
        get() = when (this) {
            RATIO_4_3 -> AspectRatio.RATIO_4_3
            RATIO_16_9 -> AspectRatio.RATIO_16_9
        }

    /**
     * 轉換為 ViewPort 的 Rational（精確比例）
     *
     * ViewPort 使用此比例定義可見區域，CameraX 自動處理裁切
     */
    fun toRational(): Rational = when (this) {
        RATIO_4_3 -> Rational(4, 3)
        RATIO_16_9 -> Rational(16, 9)
    }

    /**
     * 決定 PreviewView 的 ScaleType
     *
     * Phase0 P0-VF: 統一使用 FILL_CENTER，填滿 664dp 取景框，無黑邊。
     */
    val toScaleType: PreviewView.ScaleType
        get() = PreviewView.ScaleType.FILL_CENTER

    /** 是否顯示黑邊（FILL_CENTER 下永遠為 false）。 */
    val showsLetterbox: Boolean
        get() = toScaleType == PreviewView.ScaleType.FIT_CENTER
}
