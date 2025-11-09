package com.day.mate.data.local

data class TimerState(
    val mode: TimerMode = TimerMode.FOCUS,
    val secondsLeft: Int = 25 * 60,
    val totalSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false,
    val completedSessions: Int = 0,
    val focusTime: Int = 25 * 60,
    val shortBreakTime: Int = 5 * 60,
    val longBreakTime: Int = 15 * 60
)
