package com.day.mate.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// تعريف DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pomodoro_settings")

/**
 * SettingsDataStore
 *
 * يوفر واجهة لحفظ واسترجاع إعدادات مؤقت البومودورو باستخدام Jetpack DataStore.
 * يتم تخزين الأوقات بالثواني.
 */
class SettingsDataStore(context: Context) {

    private val dataStore = context.dataStore

    // مفاتيح التخزين
    private object PreferencesKeys {
        val FOCUS_TIME_SECONDS = intPreferencesKey("focus_time_seconds")
        val SHORT_BREAK_TIME_SECONDS = intPreferencesKey("short_break_time_seconds")
        val LONG_BREAK_TIME_SECONDS = intPreferencesKey("long_break_time_seconds")
        val COMPLETED_SESSIONS = intPreferencesKey("completed_sessions_count")
        val TOTAL_FOCUS_SESSIONS = intPreferencesKey("total_focus_sessions_count")
    }

    // القيم الافتراضية (25 دقيقة، 5 دقائق، 15 دقيقة) بالثواني
    private val DEFAULT_FOCUS_TIME = 25 * 60
    private val DEFAULT_SHORT_BREAK_TIME = 5 * 60
    private val DEFAULT_LONG_BREAK_TIME = 15 * 60
    private val DEFAULT_COMPLETED_SESSIONS = 0

    /**
     * تدفق لقراءة وقت التركيز بالثواني.
     */
    val focusTimeFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FOCUS_TIME_SECONDS] ?: DEFAULT_FOCUS_TIME
    }

    /**
     * حفظ وقت التركيز الجديد بالثواني.
     */
    suspend fun saveFocusTime(seconds: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FOCUS_TIME_SECONDS] = seconds
        }
    }

    /**
     * تدفق لقراءة وقت الراحة القصيرة بالثواني.
     */
    val shortBreakTimeFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHORT_BREAK_TIME_SECONDS] ?: DEFAULT_SHORT_BREAK_TIME
    }

    /**
     * حفظ وقت الراحة القصيرة الجديد بالثواني.
     */
    suspend fun saveShortBreakTime(seconds: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHORT_BREAK_TIME_SECONDS] = seconds
        }
    }

    /**
     * تدفق لقراءة وقت الراحة الطويلة بالثواني.
     */
    val longBreakTimeFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LONG_BREAK_TIME_SECONDS] ?: DEFAULT_LONG_BREAK_TIME
    }

    /**
     * حفظ وقت الراحة الطويلة الجديد بالثواني.
     */
    suspend fun saveLongBreakTime(seconds: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LONG_BREAK_TIME_SECONDS] = seconds
        }
    }
    val completedSessionsFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.COMPLETED_SESSIONS] ?: DEFAULT_COMPLETED_SESSIONS
    }

    /**
     * حفظ عدد جلسات التركيز المكتملة الجديد.
     */
    suspend fun saveCompletedSessions(count: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.COMPLETED_SESSIONS] = count
        }
    }
    val totalFocusSessionsFlow: Flow<Int> = dataStore.data
        .map { preferences ->
            // الافتراضي: 4
            preferences[PreferencesKeys.TOTAL_FOCUS_SESSIONS] ?: 4
        }

    suspend fun saveTotalFocusSessions(count: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TOTAL_FOCUS_SESSIONS] = count
        }
    }
}