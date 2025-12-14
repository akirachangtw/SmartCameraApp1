package com.akirachang.ai.photographer.di

import android.os.Build
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.akirachang.ai.photographer.BuildConfig
import com.akirachang.ai.photographer.data.source.camera.CameraDataSource
import com.akirachang.ai.photographer.data.source.camera.FakeCameraDataSource
import com.akirachang.ai.photographer.data.source.camera.RealCameraDataSource
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
    fun provideCameraDataSource(
        fakeCameraDataSource: FakeCameraDataSource,
        realCameraDataSource: RealCameraDataSource
    ): CameraDataSource {
        val isEmulator = isRunningOnEmulator()
        val useEmulatorMode = BuildConfig.EMULATOR_MODE || BuildConfig.IS_EMULATOR_BUILD

        return when {
            useEmulatorMode || isEmulator -> {
                Timber.d("🎮 Using FakeCameraDataSource (Emulator Mode)")
                Timber.d("  - BuildConfig.EMULATOR_MODE: ${BuildConfig.EMULATOR_MODE}")
                Timber.d("  - BuildConfig.IS_EMULATOR_BUILD: ${BuildConfig.IS_EMULATOR_BUILD}")
                Timber.d("  - isRunningOnEmulator(): $isEmulator")
                fakeCameraDataSource
            }
            else -> {
                Timber.d("📱 Using RealCameraDataSource (Device Mode)")
                Timber.d("  - BuildConfig.EMULATOR_MODE: ${BuildConfig.EMULATOR_MODE}")
                Timber.d("  - BuildConfig.IS_EMULATOR_BUILD: ${BuildConfig.IS_EMULATOR_BUILD}")
                realCameraDataSource
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
