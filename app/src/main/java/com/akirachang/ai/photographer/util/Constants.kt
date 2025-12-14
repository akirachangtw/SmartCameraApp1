package com.akirachang.ai.photographer.util

import android.os.Build

object Constants {
    // 相機相關
    const val CAMERA_PERMISSION = android.Manifest.permission.CAMERA
    const val PHOTOS_DIRECTORY = "SmartCamera"
    const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

    // 媒體權限 (Android 13+)
    const val READ_MEDIA_IMAGES = android.Manifest.permission.READ_MEDIA_IMAGES
    const val READ_MEDIA_VIDEO = android.Manifest.permission.READ_MEDIA_VIDEO

    // Android 14+ 部分照片存取權限
    val READ_MEDIA_VISUAL_USER_SELECTED = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"
    } else {
        null
    }

    // 圖片處理
    const val IMAGE_CAPTURE_QUALITY = 95
    const val PREVIEW_ASPECT_RATIO = 16f / 9f

    // 性能相關
    const val ANALYSIS_FRAME_THROTTLE_MS = 100L // 每100ms分析一次
    const val IMAGE_ANALYSIS_WIDTH = 640
    const val IMAGE_ANALYSIS_HEIGHT = 480
}
