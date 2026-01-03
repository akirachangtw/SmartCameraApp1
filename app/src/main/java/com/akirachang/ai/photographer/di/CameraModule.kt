package com.akirachang.ai.photographer.di

import android.os.Build
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.akirachang.ai.photographer.BuildConfig
import com.akirachang.ai.photographer.data.source.camera.CameraInputSource
import com.akirachang.ai.photographer.data.source.camera.StaticImageSource
import com.akirachang.ai.photographer.data.source.camera.LiveCameraSource
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CameraModule {

    /**
     * 自動判斷是否為模擬器，提供對應的資料源
     */
    @Provides
    @Singleton
    fun provideCameraInputSource(
        staticImageSource: StaticImageSource,
        liveCameraSource: LiveCameraSource
    ): CameraInputSource {
        val isEmulator = isRunningOnEmulator()
        val useEmulatorMode = BuildConfig.EMULATOR_MODE || BuildConfig.IS_EMULATOR_BUILD

        return when {
            useEmulatorMode || isEmulator -> {
                Timber.d("🎮 Using StaticImageSource (Test Images)")
                Timber.d("  - BuildConfig.EMULATOR_MODE: ${BuildConfig.EMULATOR_MODE}")
                Timber.d("  - BuildConfig.IS_EMULATOR_BUILD: ${BuildConfig.IS_EMULATOR_BUILD}")
                Timber.d("  - isRunningOnEmulator(): $isEmulator")
                staticImageSource
            }
            else -> {
                Timber.d("📱 Using LiveCameraSource (CameraX)")
                Timber.d("  - BuildConfig.EMULATOR_MODE: ${BuildConfig.EMULATOR_MODE}")
                Timber.d("  - BuildConfig.IS_EMULATOR_BUILD: ${BuildConfig.IS_EMULATOR_BUILD}")
                liveCameraSource
            }
        }
    }

    /**
     * 偵測是否在模擬器上運行
     */
    private fun isRunningOnEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || "google_sdk" == Build.PRODUCT)
    }
}
