package com.day.mate.ui.theme.screens.pomodoro
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context
import kotlinx.coroutines.flow.collectLatest
import com.day.mate.data.local.reminder.NotificationHelper
import com.day.mate.data.local.reminder.ReminderScheduler
import java.time.LocalDateTime
import androidx.lifecycle.SavedStateHandle
import com.day.mate.R
import com.day.mate.data.local.pomodoro.SettingsDataStore
import com.day.mate.data.local.pomodoro.TimerMode
import com.day.mate.data.local.pomodoro.TimerState

private const val SECONDS_LEFT_KEY = "seconds_left"
private const val TIMER_MODE_KEY = "timer_mode"
private const val IS_RUNNING_KEY = "is_running"
private const val TOTAL_SECONDS_KEY = "total_seconds"
private const val IS_FINISHED_KEY = "is_finished"

class TimerViewModel(private val context: Context,
                     private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val scheduler = ReminderScheduler(context)

    private val wasStateRestored: Boolean = savedStateHandle.contains(SECONDS_LEFT_KEY)
    private val settingsDataStore = SettingsDataStore(context)

    private val _timerState = MutableStateFlow(TimerState())
    val timerState = _timerState.asStateFlow()

    private var timerJob: Job? = null

    var focusTime = 25 * 60
    var shortBreakTime = 5 * 60
    var longBreakTime = 15 * 60

    private var isDataStoreStateLoaded = false

    init {
        loadSettings()
        restoreState()
        loadDynamicState()
    }
    private fun restoreState() {
        if (!wasStateRestored) return

        val restoredSecondsLeft = savedStateHandle.get<Int>(SECONDS_LEFT_KEY)
        val restoredModeString = savedStateHandle.get<String>(TIMER_MODE_KEY)
        val restoredIsRunning = savedStateHandle.get<Boolean>(IS_RUNNING_KEY) ?: false
        val restoredTotalSeconds = savedStateHandle.get<Int>(TOTAL_SECONDS_KEY)
        val restoredIsFinished = savedStateHandle.get<Boolean>(IS_FINISHED_KEY) ?: false

        if (restoredSecondsLeft != null) {
            val restoredMode = restoredModeString?.let { TimerMode.valueOf(it) } ?: TimerMode.FOCUS

            _timerState.value = _timerState.value.copy(
                mode = restoredMode,
                secondsLeft = restoredSecondsLeft,
                totalSeconds = restoredTotalSeconds ?: restoredSecondsLeft,
                isRunning = restoredIsRunning,
                isFinished = restoredIsFinished
            )
            isDataStoreStateLoaded = true

            if (restoredIsRunning && !restoredIsFinished) {
                startTimer(isResuming = true)
            }
        }
    }

    private fun loadDynamicState() {
        viewModelScope.launch {
            settingsDataStore.dynamicStateFlow
                .collectLatest { savedState ->
                    if (isDataStoreStateLoaded) return@collectLatest

                    if (savedState.secondsLeft > 0 || savedState.isRunning) {

                        val totalSecondsToUse = when (savedState.mode) {
                            TimerMode.FOCUS -> focusTime
                            TimerMode.SHORT_BREAK -> shortBreakTime
                            TimerMode.LONG_BREAK -> longBreakTime
                        }

                        _timerState.value = _timerState.value.copy(
                            mode = savedState.mode,
                            secondsLeft = savedState.secondsLeft,
                            totalSeconds = totalSecondsToUse,
                            isRunning = savedState.isRunning,
                            completedSessions = savedState.completedSessions,
                            totalFocusSessions = savedState.totalFocusSessions
                        )

                        isDataStoreStateLoaded = true

                        if (savedState.isRunning) {
                            startTimer(isResuming = true)
                        }
                    }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        saveCurrentState()
        timerJob?.cancel()
    }

    private fun saveCurrentState() {
        val state = _timerState.value
        viewModelScope.launch {
            val modeToSave = if (state.secondsLeft <= 0) TimerMode.FOCUS else state.mode
            val secondsToSave = if (state.secondsLeft <= 0) focusTime else state.secondsLeft

            settingsDataStore.saveDynamicState(
                secondsLeft = secondsToSave,
                isRunning = state.isRunning,
                mode = modeToSave
            )
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsDataStore.focusTimeFlow.collect { time ->
                focusTime = time
                val state = _timerState.value
                var newState = state
                val isIdleOrFinished = (state.secondsLeft == state.totalSeconds || state.secondsLeft == 0)

                if (state.mode == TimerMode.FOCUS && !state.isRunning && isIdleOrFinished) {
                    newState = state.copy(
                        secondsLeft = time,
                        totalSeconds = time
                    )
                }
                if (newState != state) {
                    _timerState.value = newState
                }
            }
        }
        viewModelScope.launch {
            settingsDataStore.shortBreakTimeFlow.collect { time ->
                shortBreakTime = time
                val state = _timerState.value
                var newState = state

                val isIdleOrFinished = (state.secondsLeft == state.totalSeconds || state.secondsLeft == 0)

                if (state.mode == TimerMode.SHORT_BREAK && !state.isRunning && isIdleOrFinished) {
                    newState = state.copy(
                        secondsLeft = time,
                        totalSeconds = time
                    )
                }
                if (newState != state) {
                    _timerState.value = newState
                }
            }
        }
        viewModelScope.launch {
            settingsDataStore.longBreakTimeFlow.collect { time ->
                longBreakTime = time
                val state = _timerState.value
                var newState = state

                val isIdleOrFinished = (state.secondsLeft == state.totalSeconds || state.secondsLeft == 0)

                if (state.mode == TimerMode.LONG_BREAK && !state.isRunning && isIdleOrFinished) {
                    newState = state.copy(
                        secondsLeft = time,
                        totalSeconds = time
                    )
                }
                if (newState != state) {
                    _timerState.value = newState
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
        NotificationHelper.cancelPersistentPomodoroNotification(context)
        handleSessionEnd()
        val state = _timerState.value
        savedStateHandle[IS_RUNNING_KEY] = false
        savedStateHandle[IS_FINISHED_KEY] = false
        savedStateHandle[TIMER_MODE_KEY] = state.mode.name
        savedStateHandle[SECONDS_LEFT_KEY] = state.secondsLeft
        savedStateHandle[TOTAL_SECONDS_KEY] = state.totalSeconds
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

    fun startTimer(isResuming: Boolean = false) {
        var state = _timerState.value
        if (state.isRunning && timerJob?.isActive == true) return
        if (state.secondsLeft <= 0) {
            handleSessionEnd(shouldSaveSession = false)
            state = _timerState.value
        }
        timerJob?.cancel()
        _timerState.value = state.copy(isRunning = true, isFinished = false)
        if (state.mode == TimerMode.SHORT_BREAK || state.mode == TimerMode.LONG_BREAK) {
            val breakDurationSeconds = state.secondsLeft
            val isLongBreak = state.mode == TimerMode.LONG_BREAK
            val breakType = if (isLongBreak) "Long Break" else "Short Break"
            val triggerDateTime = LocalDateTime.now().plusSeconds(breakDurationSeconds.toLong())
            scheduler.schedulePomodoroBreak(triggerDateTime, breakType)
        }
        NotificationHelper.showPersistentPomodoroNotification(
            context = context,
            timerMode = _timerState.value.mode,
            secondsLeft = -1
        )
        savedStateHandle[IS_RUNNING_KEY] = true
        savedStateHandle[TIMER_MODE_KEY] = state.mode.name
        savedStateHandle[TOTAL_SECONDS_KEY] = state.totalSeconds
        savedStateHandle[IS_FINISHED_KEY] = false
        timerJob = viewModelScope.launch {
            while (_timerState.value.secondsLeft > 0 && _timerState.value.isRunning) {
                delay(1000)

                val newSecondsLeft = _timerState.value.secondsLeft - 1
                _timerState.value = _timerState.value.copy(
                    secondsLeft = newSecondsLeft
                )
                savedStateHandle[SECONDS_LEFT_KEY] = newSecondsLeft
                if (newSecondsLeft % 10 == 0) {
                    saveCurrentState()
                }
            }
            if (_timerState.value.secondsLeft <= 0) {
                _timerState.value = _timerState.value.copy(
                    isRunning = false,
                    isFinished = true
                )
                handleSessionEnd(shouldSaveSession = true)
                NotificationHelper.playNotificationSound(context)
                NotificationHelper.cancelPersistentPomodoroNotification(context)
                savedStateHandle[IS_RUNNING_KEY] = false
                savedStateHandle[IS_FINISHED_KEY] = true
                saveCurrentState()
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
                    isFinished = false,
                    isRunning = false
                )
                if (shouldSaveSession) {
                    viewModelScope.launch {
                        settingsDataStore.saveCompletedSessions(newCompleted)
                        // لو هنلغي اعادة تعيين عدد الجليات الي 0 بعد 4 جلسات
                        /*if (nextMode == TimerMode.LONG_BREAK) {
                            settingsDataStore.saveCompletedSessions(0)
                            _timerState.value = _timerState.value.copy(completedSessions = 0)
                        }*/
                    }
                }
            }
            else -> {
                _timerState.value = state.copy(
                    mode = TimerMode.FOCUS,
                    secondsLeft = focusTime,
                    totalSeconds = focusTime,
                    isFinished = false,
                    isRunning = false
                )
                scheduler.cancelPomodoroBreak()
            }
        }
        saveCurrentState()
    }

    fun pauseTimer() {
        val state = _timerState.value

        timerJob?.cancel()

        _timerState.value = state.copy(isRunning = false)
        NotificationHelper.cancelPersistentPomodoroNotification(context)

        saveCurrentState()

        savedStateHandle[IS_RUNNING_KEY] = false
        savedStateHandle[SECONDS_LEFT_KEY] = state.secondsLeft
        savedStateHandle[IS_FINISHED_KEY] = false
    }

    fun resetTimer() {
        timerJob?.cancel()
        scheduler.cancelPomodoroBreak()
        NotificationHelper.cancelPersistentPomodoroNotification(context)
        val state = _timerState.value
        val newSeconds = when (state.mode) {
            TimerMode.FOCUS -> focusTime
            TimerMode.SHORT_BREAK -> shortBreakTime
            TimerMode.LONG_BREAK -> longBreakTime
        }
        savedStateHandle[IS_RUNNING_KEY] = false
        savedStateHandle[IS_FINISHED_KEY] = false
        savedStateHandle[SECONDS_LEFT_KEY] = newSeconds
        savedStateHandle[TOTAL_SECONDS_KEY] = newSeconds
        _timerState.value = state.copy(
            secondsLeft = newSeconds,
            totalSeconds = newSeconds,
            isRunning = false,
            isFinished = false
        )
        saveCurrentState()
    }

    fun resetCompletedSessions() {
        timerJob?.cancel()
        scheduler.cancelPomodoroBreak()
        NotificationHelper.cancelPersistentPomodoroNotification(context)

        viewModelScope.launch {
            settingsDataStore.saveCompletedSessions(0)
            _timerState.value = _timerState.value.copy(
                completedSessions = 0,
                mode = TimerMode.FOCUS,
                secondsLeft = focusTime,
                totalSeconds = focusTime,
                isRunning = false,
                isFinished = false
            )
        }
    }

    fun progress(): Float {
        val state = _timerState.value
        return state.secondsLeft.toFloat() / state.totalSeconds
    }
}