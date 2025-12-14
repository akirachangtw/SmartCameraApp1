package com.akirachang.ai.photographer.data.source.camera

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow

/**
 * 相機資料源介面
 * 可以有不同的實現（真實相機、假相機等）
 */
interface CameraDataSource {

    /**
     * 取得相機預覽流
     */
    fun getPreviewStream(): Flow<Bitmap>

    /**
     * 拍攝照片
     */
    suspend fun takePicture(): Result<Bitmap>

    /**
     * 停止預覽
     */
    fun stopPreview()

    /**
     * 設置縮放比例
     */
    suspend fun setZoomRatio(ratio: Float): Result<Unit>
}
