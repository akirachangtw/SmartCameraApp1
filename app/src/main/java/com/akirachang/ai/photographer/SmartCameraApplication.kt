package com.akirachang.ai.photographer

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
// TODO Sprint 2: Restore after OpenCV SDK is manually added
// import org.opencv.android.OpenCVLoader

/**
 * Smart Camera Application
 *
 * v2.7.0 Phase 2: Added OpenCV initialization (temporarily disabled)
 */
@HiltAndroidApp
class SmartCameraApplication : Application() {

    companion object {
        private const val TAG = "SmartCameraApp"
    }

    override fun onCreate() {
        super.onCreate()

        // v2.7.0 Phase 2: Initialize OpenCV (temporarily disabled until SDK added)
        // initializeOpenCV()
    }

    /**
     * Initialize OpenCV library
     *
     * Uses OpenCVLoader.initDebug() for development builds.
     * For production, consider using OpenCVLoader.initAsync() with callback.
     *
     * TODO Sprint 2: Uncomment after OpenCV SDK is manually added to project
     */
    /*
    private fun initializeOpenCV() {
        try {
            if (OpenCVLoader.initDebug()) {
                Log.i(TAG, "✅ OpenCV ${OpenCVLoader.OPENCV_VERSION} loaded successfully")
            } else {
                Log.e(TAG, "❌ OpenCV initialization failed!")
                // TODO: Consider fallback or error reporting
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ OpenCV initialization exception: ${e.message}", e)
        }
    }
    */
}
