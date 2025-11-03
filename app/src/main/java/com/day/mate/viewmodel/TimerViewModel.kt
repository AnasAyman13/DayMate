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

    // المدد الزمنية
    private val focusTime = 25 * 60
    private val shortBreakTime = 5 * 60
    private val longBreakTime = 15 * 60
    private val totalFocusSessions = 4
    fun skipTimer() {
        timerJob?.cancel() // توقف المؤقت لو شغال
        handleSessionEnd() // تنهي الجلسة الحالية مباشرة وتبدأ الجلسة التالية
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
                    isFinished = true // ✅ نعلم إن المؤقت خلص
                )
            }
        }
    }


    fun handleSessionEnd() {  // بدل private
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
                    isFinished = false // ✅ نرجّعها false للجولة الجديدة
                )
            }

            else -> {
                _timerState.value = state.copy(
                    mode = TimerMode.FOCUS,
                    secondsLeft = focusTime,
                    totalSeconds = focusTime,
                    isFinished = false // ✅ مهم جدًا
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
        _timerState.value = TimerState()
    }


    fun progress(): Float {
        val state = _timerState.value
        return state.secondsLeft.toFloat() / state.totalSeconds
    }
}
