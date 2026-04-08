package com.akirachang.ai.photographer.di

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.akirachang.ai.photographer.BuildConfig
import com.akirachang.ai.photographer.data.source.camera.CameraCapabilityDetector
import com.akirachang.ai.photographer.data.source.camera.CameraInputSource
import com.akirachang.ai.photographer.data.source.camera.StaticImageSource
import com.akirachang.ai.photographer.data.source.camera.LiveCameraSource
import com.akirachang.ai.photographer.domain.model.DeviceCapabilityTier
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CameraModule {

    /**
     * Provide CameraCapabilityDetector - v2.9.5
     *
     * Hardware capability detection for dynamic resolution selection.
     */
    @Provides
    @Singleton
    fun provideCameraCapabilityDetector(
        @ApplicationContext context: Context
    ): CameraCapabilityDetector {
        return CameraCapabilityDetector(context)
    }

    /**
     * Provide DeviceCapabilityTier by reading Camera2 INFO_SUPPORTED_HARDWARE_LEVEL
     * from the back-facing camera (primary camera).
     *
     * Cueshot v3.3 SSOT §8: FULL/LEVEL_3→FULL, LIMITED→LIMITED, else→LEGACY.
     * Used by CameraViewModel to configure pipeline degradation strategy.
     */
    @Provides
    @Singleton
    fun provideDeviceCapabilityTier(
        @ApplicationContext context: Context
    ): DeviceCapabilityTier {
        return try {
            val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val backCameraId = manager.cameraIdList.firstOrNull { id ->
                manager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            }
            val level = backCameraId?.let { id ->
                manager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
            } ?: -1
            val tier = DeviceCapabilityTier.fromCamera2Level(level)
            Log.i("CameraModule", "[D0.3] DeviceCapabilityTier=$tier (Camera2 level=$level)")
            tier
        } catch (e: Exception) {
            Log.w("CameraModule", "[D0.3] Failed to detect capability tier, defaulting to LEGACY", e)
            DeviceCapabilityTier.LEGACY
        }
    }

    /**
     * 根據 MOCK_CAMERA_DATA flag 選擇相機資料源
     * v2.x.x Phase 6: MOCK_CAMERA_DATA 為唯一開關
     *   - true  → StaticImageSource (循環測試圖片)
     *   - false → LiveCameraSource (CameraX 真實/虛擬相機)
     */
    @Provides
    @Singleton
    fun provideCameraInputSource(
        staticImageSource: StaticImageSource,
        liveCameraSource: LiveCameraSource
    ): CameraInputSource {
        return if (BuildConfig.MOCK_CAMERA_DATA) {
            Timber.d("🎮 Using StaticImageSource (Test Images)")
            staticImageSource
        } else {
            Timber.d("📱 Using LiveCameraSource (CameraX)")
            liveCameraSource
        }
    }

}
