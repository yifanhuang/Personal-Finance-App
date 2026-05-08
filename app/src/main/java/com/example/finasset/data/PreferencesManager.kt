package com.example.finasset.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_RED_UP_GREEN_DOWN = booleanPreferencesKey("red_up_green_down")
        val KEY_REFRESH_INTERVAL = intPreferencesKey("refresh_interval")
        val KEY_AUTO_REFRESH = booleanPreferencesKey("auto_refresh")
        val KEY_APP_LOCK = booleanPreferencesKey("app_lock")
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME_MODE] ?: "system"
    }

    val redUpGreenDown: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_RED_UP_GREEN_DOWN] ?: true
    }

    val refreshInterval: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_REFRESH_INTERVAL] ?: 60
    }

    val autoRefresh: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_REFRESH] ?: true
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs -> prefs[KEY_THEME_MODE] = mode }
    }

    suspend fun setRedUpGreenDown(value: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_RED_UP_GREEN_DOWN] = value }
    }

    suspend fun setRefreshInterval(minutes: Int) {
        context.dataStore.edit { prefs -> prefs[KEY_REFRESH_INTERVAL] = minutes }
    }

    suspend fun setAutoRefresh(value: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_AUTO_REFRESH] = value }
    }
}
