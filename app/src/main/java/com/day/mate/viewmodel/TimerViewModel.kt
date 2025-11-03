package com.day.mate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.day.mate.data.TimerMode
import com.day.mate.data.TimerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {

    private val _timerState = MutableStateFlow(TimerState())
    val timerState = _timerState.asStateFlow()

    private var timerJob: Job? = null

    var focusTime = 25 * 60
    var shortBreakTime = 5 * 60
    var longBreakTime = 15 * 60
    private val totalFocusSessions = 4
    fun skipTimer() {
        timerJob?.cancel()
        handleSessionEnd()

    }
    fun updateTimes(focus: Int, shortBreak: Int, longBreak: Int) {
        focusTime = focus * 60
        shortBreakTime = shortBreak * 60
        longBreakTime = longBreak * 60

        _timerState.value = _timerState.value.copy(
            secondsLeft = focusTime,
            totalSeconds = focusTime
        )
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


    fun handleSessionEnd() {
        val state = _timerState.value
        when (state.mode) {
            TimerMode.FOCUS -> {
                val newCompleted = state.completedSessions + 1
                val nextMode =
                    if (newCompleted % totalFocusSessions == 0) TimerMode.LONG_BREAK
                    else TimerMode.SHORT_BREAK

                _timerState.value = state.copy(
                    mode = nextMode,
                    completedSessions = newCompleted,
                    secondsLeft = if (nextMode == TimerMode.LONG_BREAK) longBreakTime else shortBreakTime,
                    totalSeconds = if (nextMode == TimerMode.LONG_BREAK) longBreakTime else shortBreakTime,
                    isFinished = false
                )
            }

            else -> {
                _timerState.value = state.copy(
                    mode = TimerMode.FOCUS,
                    secondsLeft = focusTime,
                    totalSeconds = focusTime,
                    isFinished = false
                )
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
