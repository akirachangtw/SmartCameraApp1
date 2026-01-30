/**
 * ClassName: AppModule
 * Description: Hilt dependency injection module for application-level dependencies
 * Author: Claude Code
 * Date: 2025-11-17
 * Version: 2.1.0
 *
 * Modifications:
 * - 2025-11-17: Add BackgroundExecutor for ML Kit processing
 * - 2025-12-01: Add DeviceSensorManager provider for LevelIndicator feature
 */
package com.akirachang.ai.photographer.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.akirachang.ai.photographer.data.preferences.OverlayPreferences
import com.akirachang.ai.photographer.data.repository.CameraRepositoryImpl
import com.akirachang.ai.photographer.data.sensor.DeviceSensorManager
import com.akirachang.ai.photographer.domain.repository.CameraRepository
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * 背景執行緒 Executor 限定符
 * 用於區分不同用途的 Executor
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BackgroundExecutor

/**
 * Application-scoped CoroutineScope 限定符
 * v2.9.8: 用於非同步 GPS EXIF 寫入，避免阻塞主要儲存流程
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindCameraRepository(
        cameraRepositoryImpl: CameraRepositoryImpl
    ): CameraRepository

    companion object {
        /**
         * 提供單執行緒 Executor 供 ML 分析使用
         *
         * 使用單執行緒避免執行緒競爭
         * 確保偵測結果按順序處理
         */
        @Provides
        @Singleton
        @BackgroundExecutor
        fun provideBackgroundExecutor(): Executor {
            return Executors.newSingleThreadExecutor { runnable ->
                Thread(runnable, "MLKit-Analysis-Thread").apply {
                    priority = Thread.NORM_PRIORITY
                }
            }
        }

        /**
         * 提供 DeviceSensorManager 單例 (v2.1.0)
         *
         * 用於 LevelIndicator 功能的設備方向追蹤
         */
        @Provides
        @Singleton
        fun provideDeviceSensorManager(
            @ApplicationContext context: Context
        ): DeviceSensorManager {
            return DeviceSensorManager(context)
        }

        /**
         * 提供 OverlayPreferences 單例 (v2.1.0)
         *
         * 用於設定畫面的覆蓋層偏好設定管理
         */
        @Provides
        @Singleton
        fun provideOverlayPreferences(
            @ApplicationContext context: Context
        ): OverlayPreferences {
            return OverlayPreferences(context)
        }

        /**
         * 提供 Application-scoped CoroutineScope (v2.9.8)
         *
         * 用於非同步 GPS EXIF 寫入，確保照片保存流程不被阻塞
         * 使用 SupervisorJob 確保子協程失敗不影響其他協程
         */
        @Provides
        @Singleton
        @ApplicationScope
        fun provideApplicationScope(): CoroutineScope {
            return CoroutineScope(SupervisorJob() + Dispatchers.Default)
        }
    }
}
