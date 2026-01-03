package com.akirachang.ai.photographer.presentation.camera.model

/**
 * HDR 模式列舉
 *
 * v2.6.1: 新增手動 HDR 控制功能
 *
 * @since v2.6.1
 */
enum class HDRMode {
    /**
     * 自動模式 (預設)
     *
     * 行為:
     * - 根據光線分析自動建議 HDR
     * - 顯示光線警告 UI (Backlight, HighContrast, Overexposed, Underexposed, Normal)
     * - 在 Backlight/HighContrast 場景自動啟用 HDR
     * - 在 Underexposed/Normal 場景自動停用 HDR
     */
    AUTO,

    /**
     * 強制開啟
     *
     * 行為:
     * - 永久啟用 HDR，忽略光線分析建議
     * - 隱藏光線警告 UI
     * - HDR 圖示顯示為常亮狀態
     * - 適用場景: 用戶明確知道需要 HDR（如拍攝高對比建築）
     */
    ON,

    /**
     * 強制關閉
     *
     * 行為:
     * - 永久停用 HDR，忽略光線分析建議
     * - 隱藏光線警告 UI
     * - HDR 圖示顯示為停用狀態
     * - 適用場景: 追求速度（HDR 會增加處理時間）、低光環境
     */
    OFF;

    /**
     * 是否為自動模式
     */
    val isAuto: Boolean
        get() = this == AUTO

    /**
     * 是否應該啟用 HDR
     *
     * 注意: AUTO 模式下此屬性無效，需要根據光線分析結果動態判斷
     */
    val isEnabled: Boolean
        get() = this == ON

    /**
     * 切換到下一個模式 (循環: AUTO → ON → OFF → AUTO)
     */
    fun next(): HDRMode = when (this) {
        AUTO -> ON
        ON -> OFF
        OFF -> AUTO
    }

    /**
     * 顯示名稱 (Traditional Chinese)
     */
    val displayName: String
        get() = when (this) {
            AUTO -> "自動"
            ON -> "開啟"
            OFF -> "關閉"
        }

    /**
     * 簡短描述
     */
    val description: String
        get() = when (this) {
            AUTO -> "根據場景自動調整"
            ON -> "強制啟用 HDR"
            OFF -> "不使用 HDR"
        }
}
