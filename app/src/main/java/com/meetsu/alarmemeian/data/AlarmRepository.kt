package com.meetsu.alarmemeian.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.meetsu.alarmemeian.data.model.AlarmBrand
import com.meetsu.alarmemeian.data.model.AlarmConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "alarm_settings")

class AlarmRepository(private val context: Context) {

    private object Keys {
        val PHONE_NUMBER = stringPreferencesKey("phoneNumber")
        val CODE = stringPreferencesKey("code")
        val BRAND = stringPreferencesKey("brand")
        val THEME_MODE = intPreferencesKey("theme_mode") // 0=system, 1=light, 2=dark
    }

    val config: Flow<AlarmConfig> = context.dataStore.data.map { prefs ->
        AlarmConfig(
            phoneNumber = prefs[Keys.PHONE_NUMBER] ?: migrateSharedPrefs()?.first ?: "",
            code = prefs[Keys.CODE] ?: migrateSharedPrefs()?.second ?: "",
            brand = AlarmBrand.fromName(prefs[Keys.BRAND] ?: AlarmBrand.ORION_MEIAN.name)
        )
    }

    suspend fun saveConfig(config: AlarmConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PHONE_NUMBER] = config.phoneNumber
            prefs[Keys.CODE] = config.code
            prefs[Keys.BRAND] = config.brand.name
        }
    }

    val themeMode: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE] ?: 0 // 0=system
    }

    suspend fun saveThemeMode(mode: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode
        }
    }

    private var migrated = false

    private fun migrateSharedPrefs(): Pair<String, String>? {
        if (migrated) return null
        migrated = true
        val oldPrefs = context.getSharedPreferences("alarm", Context.MODE_PRIVATE)
        val phone = oldPrefs.getString("phoneNumber", null)
        val code = oldPrefs.getString("code", null)
        return if (phone != null && code != null) Pair(phone, code) else null
    }
}
