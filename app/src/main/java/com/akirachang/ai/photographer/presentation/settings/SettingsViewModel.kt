/**
 * ClassName: SettingsViewModel
 * Description: ViewModel for managing overlay settings preferences
 * Author: Claude Code
 * Date: 2025-12-01
 * Version: 2.1.0
 *
 * Modifications:
 * - 2025-12-01: Initial creation for V1.0 completion (Settings feature)
 */
package com.akirachang.ai.photographer.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import com.akirachang.ai.photographer.data.preferences.OverlayPreferences
import javax.inject.Inject

/**
 * ViewModel for Settings screen
 *
 * Manages camera overlay visibility preferences using DataStore.
 * Provides Flow-based reactive state for UI observation.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val overlayPreferences: OverlayPreferences,
    private val capabilityDetector: com.akirachang.ai.photographer.data.source.camera.CameraCapabilityDetector
) : ViewModel() {

    /**
     * Flow emitting grid overlay visibility state
     */
    val isGridEnabled: Flow<Boolean> = overlayPreferences.isGridEnabled

    /**
     * Flow emitting composition hint visibility state
     */
    val isCompositionHintEnabled: Flow<Boolean> = overlayPreferences.isCompositionHintEnabled

    /**
     * Flow emitting detection overlay visibility state
     */
    val isDetectionOverlayEnabled: Flow<Boolean> = overlayPreferences.isDetectionOverlayEnabled

    /**
     * Flow emitting level indicator visibility state
     */
    val isLevelIndicatorEnabled: Flow<Boolean> = overlayPreferences.isLevelIndicatorEnabled

    /**
     * Flow emitting directional indicator visibility state
     */
    val isDirectionalIndicatorEnabled: Flow<Boolean> = overlayPreferences.isDirectionalIndicatorEnabled

    /**
     * Flow emitting auto HDR state
     * v2.3.0: Auto HDR for backlit scenes
     */
    val isAutoHDREnabled: Flow<Boolean> = overlayPreferences.isAutoHDREnabled

    /**
     * Flow emitting GPS tagging state
     * v2.9.7 Day 1-2: GPS EXIF tagging
     */
    val isGpsTaggingEnabled: Flow<Boolean> = overlayPreferences.isGpsTaggingEnabled

    /**
     * Sets grid overlay visibility
     */
    suspend fun setGridEnabled(enabled: Boolean) {
        overlayPreferences.setGridEnabled(enabled)
    }

    /**
     * Sets composition hint visibility
     */
    suspend fun setCompositionHintEnabled(enabled: Boolean) {
        overlayPreferences.setCompositionHintEnabled(enabled)
    }

    /**
     * Sets detection overlay visibility
     */
    suspend fun setDetectionOverlayEnabled(enabled: Boolean) {
        overlayPreferences.setDetectionOverlayEnabled(enabled)
    }

    /**
     * Sets level indicator visibility
     */
    suspend fun setLevelIndicatorEnabled(enabled: Boolean) {
        overlayPreferences.setLevelIndicatorEnabled(enabled)
    }

    /**
     * Sets directional indicator visibility
     */
    suspend fun setDirectionalIndicatorEnabled(enabled: Boolean) {
        overlayPreferences.setDirectionalIndicatorEnabled(enabled)
    }

    /**
     * Sets auto HDR mode
     * v2.3.0: Auto HDR for backlit scenes
     */
    suspend fun setAutoHDREnabled(enabled: Boolean) {
        overlayPreferences.setAutoHDREnabled(enabled)
    }

    /**
     * Sets GPS tagging mode
     * v2.9.7 Day 1-2: GPS EXIF tagging
     */
    suspend fun setGpsTaggingEnabled(enabled: Boolean) {
        overlayPreferences.setGpsTaggingEnabled(enabled)
    }

    /**
     * Resets all preferences to default values
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            overlayPreferences.resetToDefaults()
        }
    }

    /**
     * Get camera capability diagnostic information
     * v2.9.5: For testing/debugging resolution detection
     */
    fun getCameraDiagnosticInfo(): Map<String, String> {
        return capabilityDetector.getDiagnosticInfo()
    }

    /**
     * Re-detect camera capabilities
     * v2.9.5: For testing/debugging resolution detection
     */
    fun reDetectCameraCapabilities() {
        viewModelScope.launch {
            capabilityDetector.detectAndCacheCapabilities()
        }
    }

    /**
     * Clear camera capability cache
     * v2.9.5: For testing/debugging resolution detection
     */
    fun clearCameraCapabilityCache() {
        capabilityDetector.clearCache()
    }
}
