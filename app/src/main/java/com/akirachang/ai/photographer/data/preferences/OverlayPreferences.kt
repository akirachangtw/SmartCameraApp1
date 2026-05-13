/**
 * ClassName: OverlayPreferences
 * Description: DataStore preferences manager for camera overlay visibility settings
 * Author: Claude Code
 * Date: 2025-12-01
 * Version: 2.1.0
 *
 * Modifications:
 * - 2025-12-01: Initial creation for V1.0 completion (Settings feature)
 */
package com.akirachang.ai.photographer.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * DataStore extension property
 * Creates a single DataStore instance per application context
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "overlay_preferences"
)

/**
 * Manages overlay visibility preferences using DataStore
 *
 * Provides reactive Flow-based API for reading and updating overlay settings.
 * All preferences default to true (enabled) on first launch.
 *
 * Usage:
 * ```
 * val preferences = OverlayPreferences(context)
 *
 * // Observe preference changes
 * preferences.isGridEnabled.collect { enabled ->
 *     updateGridVisibility(enabled)
 * }
 *
 * // Update preference
 * preferences.setGridEnabled(false)
 * ```
 */
class OverlayPreferences(private val context: Context) {

    companion object {
        // Preference keys
        private val KEY_GRID_ENABLED = booleanPreferencesKey("grid_enabled")
        private val KEY_COMPOSITION_HINT_ENABLED = booleanPreferencesKey("composition_hint_enabled")
        private val KEY_LEVEL_INDICATOR_ENABLED = booleanPreferencesKey("level_indicator_enabled")
        private val KEY_DIRECTIONAL_INDICATOR_ENABLED = booleanPreferencesKey("directional_indicator_enabled")
        private val KEY_AUTO_HDR_ENABLED = booleanPreferencesKey("auto_hdr_enabled")  // v2.3.0
        private val KEY_GPS_TAGGING_ENABLED = booleanPreferencesKey("gps_tagging_enabled")  // v2.9.7 Day 1-2
        private val KEY_ASPECT_RATIO_MODE = stringPreferencesKey("aspect_ratio_mode")  // v2.11.0 Phase 2

        // Default values
        private const val DEFAULT_ENABLED = true
        private const val DEFAULT_GPS_ENABLED = false  // Privacy-first: GPS default OFF
        private const val DEFAULT_ASPECT_RATIO = "RATIO_16_9"  // v2.11.0: Default 16:9 widescreen
    }

    /**
     * Flow emitting grid overlay visibility state
     * Default: true (enabled)
     */
    val isGridEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_GRID_ENABLED] ?: DEFAULT_ENABLED
        }

    /**
     * Flow emitting composition hint visibility state
     * Default: true (enabled)
     */
    val isCompositionHintEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_COMPOSITION_HINT_ENABLED] ?: DEFAULT_ENABLED
        }

    /**
     * Flow emitting level indicator visibility state
     * Default: true (enabled)
     */
    val isLevelIndicatorEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_LEVEL_INDICATOR_ENABLED] ?: DEFAULT_ENABLED
        }

    /**
     * Flow emitting directional indicator visibility state
     * Default: true (enabled)
     */
    val isDirectionalIndicatorEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_DIRECTIONAL_INDICATOR_ENABLED] ?: DEFAULT_ENABLED
        }

    /**
     * Flow emitting auto HDR state
     * Default: true (enabled)
     * v2.3.0: Auto HDR for backlit scenes
     */
    val isAutoHDREnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_AUTO_HDR_ENABLED] ?: DEFAULT_ENABLED
        }

    /**
     * Flow emitting GPS tagging state
     * Default: false (disabled for privacy)
     * v2.9.7 Day 1-2: GPS EXIF tagging
     */
    val isGpsTaggingEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_GPS_TAGGING_ENABLED] ?: DEFAULT_GPS_ENABLED
        }

    /**
     * Flow emitting aspect ratio mode setting
     * Default: "RATIO_16_9" (16:9 widescreen)
     * v2.11.0 Phase 2: Aspect ratio persistence
     */
    val aspectRatioMode: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_ASPECT_RATIO_MODE] ?: DEFAULT_ASPECT_RATIO
        }

    /**
     * Sets grid overlay visibility
     *
     * @param enabled true to show grid, false to hide
     */
    suspend fun setGridEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_GRID_ENABLED] = enabled
        }
    }

    /**
     * Sets composition hint visibility
     *
     * @param enabled true to show hint, false to hide
     */
    suspend fun setCompositionHintEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_COMPOSITION_HINT_ENABLED] = enabled
        }
    }

    /**
     * Sets level indicator visibility
     *
     * @param enabled true to show level indicator, false to hide
     */
    suspend fun setLevelIndicatorEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LEVEL_INDICATOR_ENABLED] = enabled
        }
    }

    /**
     * Sets directional indicator visibility
     *
     * @param enabled true to show arrows, false to hide
     */
    suspend fun setDirectionalIndicatorEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DIRECTIONAL_INDICATOR_ENABLED] = enabled
        }
    }

    /**
     * Sets auto HDR mode
     *
     * @param enabled true to enable auto HDR, false to disable
     * v2.3.0: Auto HDR for backlit scenes
     */
    suspend fun setAutoHDREnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTO_HDR_ENABLED] = enabled
        }
    }

    /**
     * Sets GPS tagging mode
     *
     * @param enabled true to enable GPS EXIF tagging, false to disable
     * v2.9.7 Day 1-2: GPS EXIF tagging
     */
    suspend fun setGpsTaggingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_GPS_TAGGING_ENABLED] = enabled
        }
    }

    /**
     * Sets aspect ratio mode
     *
     * @param mode Aspect ratio mode string ("RATIO_4_3" or "RATIO_16_9")
     * v2.11.0 Phase 2: Aspect ratio persistence
     */
    suspend fun setAspectRatioMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ASPECT_RATIO_MODE] = mode
        }
    }

    /**
     * Resets all preferences to default values (all enabled)
     */
    suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
