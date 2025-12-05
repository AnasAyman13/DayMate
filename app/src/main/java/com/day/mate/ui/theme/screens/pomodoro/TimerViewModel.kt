package com.day.mate.ui.theme.screens.pomodoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.day.mate.data.local.TimerMode
import com.day.mate.data.local.TimerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context
import com.day.mate.data.local.SettingsDataStore
import com.day.mate.data.local.reminder.ReminderScheduler
import java.time.LocalDateTime

class TimerViewModel(context: Context) : ViewModel() {

    private val scheduler = ReminderScheduler(context)


    private val settingsDataStore = SettingsDataStore(context)

    private val _timerState = MutableStateFlow(TimerState())
    val timerState = _timerState.asStateFlow()

    private var timerJob: Job? = null

    var focusTime = 25 * 60
    var shortBreakTime = 5 * 60
    var longBreakTime = 15 * 60
    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsDataStore.focusTimeFlow.collect { time ->
                focusTime = time
                val state = _timerState.value
                if (state.mode == TimerMode.FOCUS && !state.isRunning && !state.isFinished) {
                    _timerState.value = state.copy(
                        secondsLeft = focusTime,
                        totalSeconds = focusTime
                    )
                }
            }
        }
        viewModelScope.launch {
            settingsDataStore.shortBreakTimeFlow.collect { time ->
                shortBreakTime = time
                val state = _timerState.value
                if (state.mode == TimerMode.SHORT_BREAK && !state.isRunning && !state.isFinished) {
                    _timerState.value = state.copy(
                        secondsLeft = shortBreakTime,
                        totalSeconds = shortBreakTime
                    )
                }
            }
        }
        viewModelScope.launch {
            settingsDataStore.longBreakTimeFlow.collect { time ->
                longBreakTime = time
                val state = _timerState.value
                if (state.mode == TimerMode.LONG_BREAK && !state.isRunning && !state.isFinished) {
                    _timerState.value = state.copy(
                        secondsLeft = longBreakTime,
                        totalSeconds = longBreakTime
                    )
                }
            }
        }
        viewModelScope.launch {
            settingsDataStore.completedSessionsFlow.collect { count ->
                _timerState.value = _timerState.value.copy(
                    completedSessions = count
                )
            }
        }
        viewModelScope.launch {
            settingsDataStore.totalFocusSessionsFlow.collect { count ->
                _timerState.value = _timerState.value.copy(
                    totalFocusSessions = count
                )
            }
        }
    }


    fun skipTimer() {
        timerJob?.cancel()
        scheduler.cancelPomodoroBreak()
        handleSessionEnd()
    }

    fun updateTimesRaw(focusSeconds: Int, shortBreakSeconds: Int, longBreakSeconds: Int) {
        viewModelScope.launch {
            settingsDataStore.saveFocusTime(focusSeconds)
            settingsDataStore.saveShortBreakTime(shortBreakSeconds)
            settingsDataStore.saveLongBreakTime(longBreakSeconds)
        }
        focusTime = focusSeconds
        shortBreakTime = shortBreakSeconds
        longBreakTime = longBreakSeconds

        val state = _timerState.value
        val newSeconds = when (state.mode) {
            TimerMode.FOCUS -> focusSeconds
            TimerMode.SHORT_BREAK -> shortBreakSeconds
            TimerMode.LONG_BREAK -> longBreakSeconds
        }
        if (!state.isRunning) {
            _timerState.value = state.copy(
                secondsLeft = newSeconds,
                totalSeconds = newSeconds,
                isFinished = false
            )
        }
    }

    fun startTimer() {
        val state = _timerState.value
        if (state.isRunning) return

        _timerState.value = state.copy(isRunning = true, isFinished = false)

        timerJob = viewModelScope.launch {
            while (_timerState.value.secondsLeft > 0 && _timerState.value.isRunning) {
                delay(1000)
                _timerState.value = _timerState.value.copy(
                    secondsLeft = _timerState.value.secondsLeft - 1
                )
            }
            if (_timerState.value.secondsLeft <= 0) {
                _timerState.value = _timerState.value.copy(
                    isRunning = false,
                    isFinished = true
                )
            }
        }
    }

    fun handleSessionEnd(shouldSaveSession: Boolean = true) {
        val state = _timerState.value
        when (state.mode) {
            TimerMode.FOCUS -> {
                val newCompleted = if (shouldSaveSession) state.completedSessions + 1 else state.completedSessions
                val nextMode =
                    if (newCompleted % state.totalFocusSessions == 0 && newCompleted > 0) TimerMode.LONG_BREAK
                    else TimerMode.SHORT_BREAK

                _timerState.value = state.copy(
                    mode = nextMode,
                    completedSessions = newCompleted,
                    secondsLeft = if (nextMode == TimerMode.LONG_BREAK) longBreakTime else shortBreakTime,
                    totalSeconds = if (nextMode == TimerMode.LONG_BREAK) longBreakTime else shortBreakTime,
                    isFinished = false
                )
                if (shouldSaveSession) {
                    viewModelScope.launch {
                        settingsDataStore.saveCompletedSessions(newCompleted)
                        if (nextMode == TimerMode.LONG_BREAK) {
                            settingsDataStore.saveCompletedSessions(0)
                            _timerState.value = _timerState.value.copy(completedSessions = 0)
                        }
                    }
                }


                val breakDurationSeconds = if (nextMode == TimerMode.LONG_BREAK) longBreakTime else shortBreakTime
                val breakType = if (nextMode == TimerMode.LONG_BREAK) "Long Break" else "Short Break"

                val triggerDateTime = LocalDateTime.now().plusSeconds(breakDurationSeconds.toLong())
                if (shouldSaveSession) {
                    scheduler.schedulePomodoroBreak(triggerDateTime, breakType)
                }
            }
            else -> {
                _timerState.value = state.copy(
                    mode = TimerMode.FOCUS,
                    secondsLeft = focusTime,
                    totalSeconds = focusTime,
                    isFinished = false
                )
                scheduler.cancelPomodoroBreak()
            }
        }
    }

    fun pauseTimer() {
        val state = _timerState.value
        _timerState.value = state.copy(isRunning = false)
        timerJob?.cancel()
    }


    fun resetTimer() {
        timerJob?.cancel()
        scheduler.cancelPomodoroBreak()
        val state = _timerState.value
        val newSeconds = when (state.mode) {
            TimerMode.FOCUS -> focusTime
            TimerMode.SHORT_BREAK -> shortBreakTime
            TimerMode.LONG_BREAK -> longBreakTime
        }
        _timerState.value = state.copy(
            secondsLeft = newSeconds,
            totalSeconds = newSeconds,
            isRunning = false,
            isFinished = false
        )
    }

    fun progress(): Float {
        val state = _timerState.value
        return state.secondsLeft.toFloat() / state.totalSeconds
    }
}