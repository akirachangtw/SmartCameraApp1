package com.akirachang.ai.photographer

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

/**
 * Smart Camera Application
 */
@HiltAndroidApp
class SmartCameraApplication : Application() {

    companion object {
        private const val TAG = "SmartCameraApp"
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "SmartCameraApplication started")
    }
}
