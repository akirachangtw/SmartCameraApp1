package local.mylab.smartcameraapp1.di

import android.os.Build
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import local.mylab.smartcameraapp1.BuildConfig
import local.mylab.smartcameraapp1.data.source.camera.CameraDataSource
import local.mylab.smartcameraapp1.data.source.camera.FakeCameraDataSource
import local.mylab.smartcameraapp1.data.source.camera.RealCameraDataSource
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
