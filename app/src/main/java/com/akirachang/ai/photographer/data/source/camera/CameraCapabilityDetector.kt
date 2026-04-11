package com.akirachang.ai.photographer.data.source.camera

import android.content.Context
import android.content.SharedPreferences
import android.hardware.camera2.CameraCharacteristics
import android.util.Log
import android.util.Size
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalCameraInfo
import androidx.camera.lifecycle.ProcessCameraProvider
import com.akirachang.ai.photographer.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Camera Capability Detector - v2.9.5
 *
 * Detects camera hardware capabilities at first launch and caches results.
 * Removes dependency on manufacturer/model string checks.
 *
 * Key features:
 * - Uses Camera2 API to query supported resolutions
 * - Detects 800x600 support
 * - Caches results to SharedPreferences
 * - Thread-safe with in-flight guard
 * - Fail-safe fallback to 640x480
 */
class CameraCapabilityDetector(private val context: Context) {

    companion object {
        private const val TAG = "CameraCapabilityDetector"
        private const val PREFS_NAME = "camera_capabilities"

        // Cache keys
        private const val KEY_CACHE_VERSION = "cache_version"
        private const val KEY_APP_VERSION = "app_version"
        private const val KEY_MANUFACTURER = "manufacturer"
        private const val KEY_MODEL = "model"
        private const val KEY_SDK_INT = "sdk_int"
        private const val KEY_SUPPORTS_800x600 = "supports_800x600"
        private const val KEY_TARGET_WIDTH = "target_width"
        private const val KEY_TARGET_HEIGHT = "target_height"
        private const val KEY_FILTER_MAX_WIDTH = "filter_max_width"
        private const val KEY_FILTER_MAX_HEIGHT = "filter_max_height"

        private const val CACHE_VERSION = 1
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val detectingInFlight = AtomicBoolean(false)

    /**
     * Cached resolution strategy
     */
    data class CachedResolutionStrategy(
        val supports800x600: Boolean,
        val targetResolution: Size,
        val filterMaxWidth: Int,
        val filterMaxHeight: Int
    )

    /**
     * Check if capabilities are cached and valid
     */
    fun isCapabilitiesCached(): Boolean {
        val cacheVersion = prefs.getInt(KEY_CACHE_VERSION, 0)
        val cachedAppVersion = prefs.getString(KEY_APP_VERSION, "")
        val currentAppVersion = BuildConfig.VERSION_NAME

        return when {
            cacheVersion != CACHE_VERSION -> {
                Log.i(TAG, "Cache version mismatch: $cacheVersion != $CACHE_VERSION")
                false
            }
            cachedAppVersion != currentAppVersion -> {
                Log.i(TAG, "App version changed: $cachedAppVersion -> $currentAppVersion")
                false
            }
            !prefs.contains(KEY_TARGET_WIDTH) -> {
                Log.i(TAG, "Cache incomplete: missing target_width")
                false
            }
            else -> {
                Log.i(TAG, "Cache valid: v$cacheVersion, app=$currentAppVersion")
                true
            }
        }
    }

    /**
     * Get cached resolution strategy
     * @return Cached strategy, or null if not cached or invalid
     */
    fun getCachedStrategy(): CachedResolutionStrategy? {
        return try {
            if (!isCapabilitiesCached()) {
                Log.i(TAG, "[cache_miss] No valid cache")
                return null
            }

            val supports800x600 = prefs.getBoolean(KEY_SUPPORTS_800x600, false)
            val targetWidth = prefs.getInt(KEY_TARGET_WIDTH, 640)
            val targetHeight = prefs.getInt(KEY_TARGET_HEIGHT, 480)
            val filterMaxWidth = prefs.getInt(KEY_FILTER_MAX_WIDTH, 800)
            val filterMaxHeight = prefs.getInt(KEY_FILTER_MAX_HEIGHT, 600)

            val strategy = CachedResolutionStrategy(
                supports800x600 = supports800x600,
                targetResolution = Size(targetWidth, targetHeight),
                filterMaxWidth = filterMaxWidth,
                filterMaxHeight = filterMaxHeight
            )

            Log.i(TAG, "[cache_hit] Loaded: supports800x600=$supports800x600, " +
                      "target=${targetWidth}x${targetHeight}, filter≤${filterMaxWidth}x${filterMaxHeight}")

            strategy
        } catch (e: Exception) {
            Log.e(TAG, "[cache_fail] Failed to read cache: ${e.message}", e)
            null
        }
    }

    /**
     * Detect and cache camera capabilities
     * @return Result containing detected strategy, or error
     */
    @androidx.annotation.OptIn(
        androidx.camera.core.ExperimentalCameraInfo::class,
        androidx.camera.camera2.interop.ExperimentalCamera2Interop::class
    )
    suspend fun detectAndCacheCapabilities(): Result<CachedResolutionStrategy> = withContext(Dispatchers.IO) {
        try {
            // In-flight guard: prevent concurrent detection
            if (!detectingInFlight.compareAndSet(false, true)) {
                Log.w(TAG, "[detect_skip] Detection already in flight")
                return@withContext getCachedStrategy()?.let { Result.success(it) }
                    ?: Result.failure(IllegalStateException("Detection in flight, no cache available"))
            }

            val startTime = System.currentTimeMillis()
            Log.i(TAG, "[detect_start] Detecting camera capabilities...")

            // Get CameraProvider
            val cameraProvider = ProcessCameraProvider.getInstance(context).get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val cameraInfo = cameraProvider.getCameraInfo(cameraSelector)

            // Query supported resolutions using Camera2 API
            val supportedSizes = querySupportedResolutions(cameraInfo)

            if (supportedSizes.isEmpty()) {
                Log.w(TAG, "[detect_fail] No supported resolutions found")
                detectingInFlight.set(false)
                return@withContext Result.failure(IllegalStateException("No supported resolutions"))
            }

            // Filter acceptable resolutions (480x360 ~ 1280x720)
            val acceptableResolutions = supportedSizes.filter { size ->
                size.width in 480..1280 && size.height in 360..720
            }.sortedBy { it.width * it.height }

            Log.i(TAG, "[detect_info] Total: ${supportedSizes.size} resolutions, " +
                      "Acceptable: ${acceptableResolutions.size}")

            // Detect 800x600 support
            val supports800x600 = acceptableResolutions.any { it.width == 800 && it.height == 600 }

            // Select target resolution based on hardware support
            val (targetResolution, filterMaxWidth, filterMaxHeight) = if (supports800x600) {
                Triple(Size(800, 600), 1280, 720)
            } else {
                // Find largest acceptable resolution ≤ 800x600
                val optimal = acceptableResolutions.lastOrNull { s -> s.width <= 800 && s.height <= 600 }
                    ?: Size(640, 480)  // Ultimate fallback
                Triple(optimal, 800, 600)
            }

            val strategy = CachedResolutionStrategy(
                supports800x600 = supports800x600,
                targetResolution = targetResolution,
                filterMaxWidth = filterMaxWidth,
                filterMaxHeight = filterMaxHeight
            )

            // Cache the result
            cacheStrategy(strategy)

            val duration = System.currentTimeMillis() - startTime
            Log.i(TAG, "[detect_success] Detected in ${duration}ms: supports800x600=$supports800x600, " +
                      "target=${targetResolution.width}x${targetResolution.height}")

            detectingInFlight.set(false)
            Result.success(strategy)

        } catch (e: SecurityException) {
            Log.e(TAG, "[detect_fail] SecurityException - camera permission not granted: ${e.message}", e)
            detectingInFlight.set(false)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "[detect_fail] Unexpected error: ${e.message}", e)
            detectingInFlight.set(false)
            Result.failure(e)
        }
    }

    /**
     * Query supported resolutions using Camera2 API
     */
    @androidx.annotation.OptIn(androidx.camera.camera2.interop.ExperimentalCamera2Interop::class)
    private fun querySupportedResolutions(cameraInfo: CameraInfo): List<Size> {
        return try {
            val characteristics = Camera2CameraInfo.from(cameraInfo)
                .getCameraCharacteristic(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

            val sizes = characteristics?.getOutputSizes(android.graphics.ImageFormat.YUV_420_888)?.toList()
                ?: emptyList()

            if (sizes.isNotEmpty()) {
                Log.d(TAG, "[query_success] Found ${sizes.size} YUV_420_888 resolutions")
            } else {
                Log.w(TAG, "[query_empty] No YUV_420_888 resolutions found")
            }

            sizes
        } catch (e: Exception) {
            Log.w(TAG, "[query_fail] Failed to query resolutions: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Cache the detected strategy to SharedPreferences
     */
    private fun cacheStrategy(strategy: CachedResolutionStrategy) {
        try {
            prefs.edit().apply {
                putInt(KEY_CACHE_VERSION, CACHE_VERSION)
                putString(KEY_APP_VERSION, BuildConfig.VERSION_NAME)
                putString(KEY_MANUFACTURER, android.os.Build.MANUFACTURER)
                putString(KEY_MODEL, android.os.Build.MODEL)
                putInt(KEY_SDK_INT, android.os.Build.VERSION.SDK_INT)
                putBoolean(KEY_SUPPORTS_800x600, strategy.supports800x600)
                putInt(KEY_TARGET_WIDTH, strategy.targetResolution.width)
                putInt(KEY_TARGET_HEIGHT, strategy.targetResolution.height)
                putInt(KEY_FILTER_MAX_WIDTH, strategy.filterMaxWidth)
                putInt(KEY_FILTER_MAX_HEIGHT, strategy.filterMaxHeight)
                apply()
            }
            Log.i(TAG, "[cache_write] Cached strategy: ${strategy.targetResolution.width}x${strategy.targetResolution.height}")
        } catch (e: Exception) {
            Log.e(TAG, "[cache_write_fail] Failed to cache: ${e.message}", e)
        }
    }

    /**
     * Clear cached capabilities (for debugging/testing)
     */
    fun clearCache() {
        prefs.edit().clear().apply()
        Log.i(TAG, "[cache_clear] Cache cleared")
    }

    /**
     * Get diagnostic info (for settings UI)
     */
    fun getDiagnosticInfo(): Map<String, String> {
        return mapOf(
            "Cache Version" to prefs.getInt(KEY_CACHE_VERSION, 0).toString(),
            "App Version" to (prefs.getString(KEY_APP_VERSION, "") ?: "N/A"),
            "Manufacturer" to (prefs.getString(KEY_MANUFACTURER, "") ?: "N/A"),
            "Model" to (prefs.getString(KEY_MODEL, "") ?: "N/A"),
            "SDK" to prefs.getInt(KEY_SDK_INT, 0).toString(),
            "Supports 800x600" to prefs.getBoolean(KEY_SUPPORTS_800x600, false).toString(),
            "Target Resolution" to "${prefs.getInt(KEY_TARGET_WIDTH, 0)}x${prefs.getInt(KEY_TARGET_HEIGHT, 0)}",
            "Filter Max" to "${prefs.getInt(KEY_FILTER_MAX_WIDTH, 0)}x${prefs.getInt(KEY_FILTER_MAX_HEIGHT, 0)}"
        )
    }
}
