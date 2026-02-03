package com.akirachang.ai.photographer.di

import android.content.Context
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
