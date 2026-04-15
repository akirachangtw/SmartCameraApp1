package com.akirachang.ai.photographer.data.preferences

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

private val Context.ptfDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ptf_preferences"
)

/**
 * Manages Pass-To-Friend onboarding preferences using DataStore.
 *
 * Cueshot D7-B2-T3: Persists the first-time PTF tutorial flag.
 * Follows the same pattern as [OverlayPreferences] for DI consistency.
 *
 * @param context Application context (provided via Hilt AppModule).
 */
class PtfPreferences(private val context: Context) {

    companion object {
        private val KEY_PTF_ONBOARDED = booleanPreferencesKey("ptf_onboarded")
    }

    /**
     * Flow emitting whether the user has dismissed the PTF onboarding hint.
     * Default: false — hint is shown on first PTF session.
     */
    val isPtfOnboarded: Flow<Boolean> = context.ptfDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_PTF_ONBOARDED] ?: false
        }

    /**
     * Marks the PTF onboarding hint as permanently dismissed.
     * Called when the user taps "Got it" on the first-launch hint.
     */
    suspend fun markPtfOnboarded() {
        context.ptfDataStore.edit { preferences ->
            preferences[KEY_PTF_ONBOARDED] = true
        }
    }
}
