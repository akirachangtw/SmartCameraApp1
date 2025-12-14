package com.akirachang.ai.photographer.domain.model

import android.net.Uri

sealed class CaptureResult {
    data class Success(val uri: Uri) : CaptureResult()
    data class Error(val exception: Throwable) : CaptureResult()
}
