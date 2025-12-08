package com.day.mate.data.local.pomodoro

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pomodoro_settings")

data class TimerDynamicState(
    val secondsLeft: Int,
    val isRunning: Boolean,
    val mode: TimerMode,
    val completedSessions: Int,
    val totalFocusSessions: Int
)
class SettingsDataStore(context: Context) {

    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val FOCUS_TIME_SECONDS = intPreferencesKey("focus_time_seconds")
        val SHORT_BREAK_TIME_SECONDS = intPreferencesKey("short_break_time_seconds")
        val LONG_BREAK_TIME_SECONDS = intPreferencesKey("long_break_time_seconds")
        val COMPLETED_SESSIONS = intPreferencesKey("completed_sessions_count")
        val TOTAL_FOCUS_SESSIONS = intPreferencesKey("total_focus_sessions_count")
        val DYNAMIC_SECONDS_LEFT = intPreferencesKey("dynamic_seconds_left")
        val DYNAMIC_IS_RUNNING = booleanPreferencesKey("dynamic_is_running")
        val DYNAMIC_MODE = stringPreferencesKey("dynamic_mode")
    }
    private val DEFAULT_FOCUS_TIME = 25 * 60
    private val DEFAULT_SHORT_BREAK_TIME = 5 * 60
    private val DEFAULT_LONG_BREAK_TIME = 15 * 60
    private val DEFAULT_COMPLETED_SESSIONS = 0

    val focusTimeFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FOCUS_TIME_SECONDS] ?: DEFAULT_FOCUS_TIME
    }
    suspend fun saveFocusTime(seconds: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FOCUS_TIME_SECONDS] = seconds
        }
    }
    suspend fun saveDynamicState(
        secondsLeft: Int,
        isRunning: Boolean,
        mode: TimerMode
    ) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_SECONDS_LEFT] = secondsLeft
            preferences[PreferencesKeys.DYNAMIC_IS_RUNNING] = isRunning
            preferences[PreferencesKeys.DYNAMIC_MODE] = mode.name
        }
    }

    val shortBreakTimeFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHORT_BREAK_TIME_SECONDS] ?: DEFAULT_SHORT_BREAK_TIME
    }
    suspend fun saveShortBreakTime(seconds: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHORT_BREAK_TIME_SECONDS] = seconds
        }
    }
    val longBreakTimeFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LONG_BREAK_TIME_SECONDS] ?: DEFAULT_LONG_BREAK_TIME
    }

    suspend fun saveLongBreakTime(seconds: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LONG_BREAK_TIME_SECONDS] = seconds
        }
    }
    val completedSessionsFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.COMPLETED_SESSIONS] ?: DEFAULT_COMPLETED_SESSIONS
    }
    suspend fun saveCompletedSessions(count: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.COMPLETED_SESSIONS] = count
        }
    }
    val totalFocusSessionsFlow: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.TOTAL_FOCUS_SESSIONS] ?: 4
        }


    val dynamicStateFlow: Flow<TimerDynamicState> = dataStore.data
        .map { preferences ->
            val secondsLeft = preferences[PreferencesKeys.DYNAMIC_SECONDS_LEFT] ?: 0
            val isRunning = preferences[PreferencesKeys.DYNAMIC_IS_RUNNING] ?: false
            val modeString = preferences[PreferencesKeys.DYNAMIC_MODE] ?: TimerMode.FOCUS.name
            val completedSessions = preferences[PreferencesKeys.COMPLETED_SESSIONS] ?: DEFAULT_COMPLETED_SESSIONS
            val totalFocusSessions = preferences[PreferencesKeys.TOTAL_FOCUS_SESSIONS] ?: 4

            TimerDynamicState(
                secondsLeft = secondsLeft,
                isRunning = isRunning,
                mode = try { TimerMode.valueOf(modeString) } catch (e: IllegalArgumentException) { TimerMode.FOCUS },
                completedSessions = completedSessions,
                totalFocusSessions = totalFocusSessions
            )
        }
}