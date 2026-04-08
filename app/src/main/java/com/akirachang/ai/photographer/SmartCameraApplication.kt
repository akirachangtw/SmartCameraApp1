package com.akirachang.ai.photographer

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

/**
 * Smart Camera Application
 *
 * v2.7.0 Phase 2: Added OpenCV 4.13.0 initialization
 * v2.13.0: OpenCV init MOVED to MainActivity (Dispatchers.IO + SplashScreen).
 *          Application.onCreate() must never block the Main Thread with native lib loading.
 */
@HiltAndroidApp
class SmartCameraApplication : Application() {

    companion object {
        private const val TAG = "SmartCameraApp"
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "SmartCameraApplication started — OpenCV init delegated to MainActivity (IO thread)")
    }
}
