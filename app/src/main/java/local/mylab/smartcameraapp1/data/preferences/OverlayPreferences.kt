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
package local.mylab.smartcameraapp1.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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
        private val KEY_DETECTION_OVERLAY_ENABLED = booleanPreferencesKey("detection_overlay_enabled")
        private val KEY_LEVEL_INDICATOR_ENABLED = booleanPreferencesKey("level_indicator_enabled")
        private val KEY_DIRECTIONAL_INDICATOR_ENABLED = booleanPreferencesKey("directional_indicator_enabled")

        // Default values
        private const val DEFAULT_ENABLED = true
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
     * Flow emitting detection overlay visibility state
     * Default: true (enabled)
     */
    val isDetectionOverlayEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_DETECTION_OVERLAY_ENABLED] ?: DEFAULT_ENABLED
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
     * Sets detection overlay visibility
     *
     * @param enabled true to show bounding boxes, false to hide
     */
    suspend fun setDetectionOverlayEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DETECTION_OVERLAY_ENABLED] = enabled
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
     * Resets all preferences to default values (all enabled)
     */
    suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
