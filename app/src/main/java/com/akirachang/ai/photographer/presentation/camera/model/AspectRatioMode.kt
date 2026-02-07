package com.akirachang.ai.photographer.presentation.camera.model

import androidx.camera.core.AspectRatio
import androidx.camera.view.PreviewView
import androidx.core.util.Rational

/**
 * 長寬比模式列舉
 *
 * v2.10.0: 實作專業相機長寬比切換功能（基於 UseCaseGroup + ViewPort）
 *
 * 參考: Gemini 方案（UseCaseGroup + ViewPort）
 * - 4:3: FIT_CENTER（黑邊，無裁切）- 類似 Pixel Camera
 * - 16:9: FILL_CENTER（填滿螢幕）
 * - 1:1: FIT_CENTER（正方形 + 黑邊）
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
    RATIO_16_9,

    /**
     * 1:1 正方形模式
     *
     * 行為:
     * - 裁切為正方形
     * - PreviewView.ScaleType = FIT_CENTER（顯示黑邊）
     * - Instagram 風格
     *
     * 適用場景:
     * - Instagram 貼文
     * - 個人頭像
     * - 對稱構圖
     *
     * 實作注意:
     * - CameraX 無原生 RATIO_1_1
     * - 使用 ViewPort 的 Rational(1, 1) 實現
     */
    RATIO_1_1;

    /**
     * 切換到下一個模式 (循環: 4:3 → 16:9 → 1:1 → 4:3)
     */
    fun next(): AspectRatioMode = when (this) {
        RATIO_4_3 -> RATIO_16_9
        RATIO_16_9 -> RATIO_1_1
        RATIO_1_1 -> RATIO_4_3
    }

    /**
     * 顯示名稱（UI 顯示）
     */
    val displayName: String
        get() = when (this) {
            RATIO_4_3 -> "4:3"
            RATIO_16_9 -> "16:9"
            RATIO_1_1 -> "1:1"
        }

    /**
     * 詳細描述
     */
    val description: String
        get() = when (this) {
            RATIO_4_3 -> "標準（全感光元件）"
            RATIO_16_9 -> "寬螢幕（填滿螢幕）"
            RATIO_1_1 -> "正方形（Instagram）"
        }

    /**
     * 轉換為 CameraX AspectRatio 常數
     *
     * 注意: 1:1 沒有對應的 CameraX AspectRatio，fallback 到 4:3
     * 實際 1:1 裁切由 ViewPort 的 Rational(1, 1) 處理
     */
    val toCameraXAspectRatio: Int
        get() = when (this) {
            RATIO_4_3 -> AspectRatio.RATIO_4_3
            RATIO_16_9 -> AspectRatio.RATIO_16_9
            RATIO_1_1 -> AspectRatio.RATIO_4_3  // Fallback, 實際由 ViewPort 裁切
        }

    /**
     * 轉換為 ViewPort 的 Rational（精確比例）
     *
     * ViewPort 使用此比例定義可見區域，CameraX 自動處理裁切
     */
    fun toRational(): Rational = when (this) {
        RATIO_4_3 -> Rational(4, 3)
        RATIO_16_9 -> Rational(16, 9)
        RATIO_1_1 -> Rational(1, 1)
    }

    /**
     * 決定 PreviewView 的 ScaleType
     *
     * 策略（基於 Gemini 方案）:
     * - 4:3: FIT_CENTER（顯示黑邊，無裁切）→ 完全解決放大感
     * - 16:9: FILL_CENTER（填滿螢幕）
     * - 1:1: FIT_CENTER（正方形 + 黑邊）
     */
    val toScaleType: PreviewView.ScaleType
        get() = when (this) {
            RATIO_4_3 -> PreviewView.ScaleType.FIT_CENTER   // 黑邊，無裁切
            RATIO_16_9 -> PreviewView.ScaleType.FILL_CENTER  // 填滿螢幕
            RATIO_1_1 -> PreviewView.ScaleType.FIT_CENTER   // 黑邊
        }

    /**
     * 是否使用 FIT_CENTER（顯示黑邊）
     */
    val showsLetterbox: Boolean
        get() = toScaleType == PreviewView.ScaleType.FIT_CENTER
}
