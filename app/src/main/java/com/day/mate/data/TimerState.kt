package com.day.mate.data

data class TimerState(
    val totalSeconds: Int = 25 * 60, // 25 دقيقة
    var secondsLeft: Int =25 * 60,
    var isRunning: Boolean = false,
    val mode: TimerMode = TimerMode.FOCUS,
    var completedSessions: Int = 0,
    val isFinished: Boolean = false
)
