package com.day.mate.ui.theme.screens.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.day.mate.data.local.prayer.toTimelineEvents
import com.day.mate.data.model.TimelineEvent
import com.day.mate.data.model.toTimelineEvent
import com.day.mate.data.repository.TodoRepository
import com.day.mate.data.repository.PrayerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


/**
 * TimelineViewModel
 *
 * Manages the data and business logic for the Timeline Screen.
 * It combines events from two sources: Todo tasks (local DB) and Prayer timings (remote API).
 * The core functionality involves calculating and shifting event timestamps based on the
 * selected date (Today, Tomorrow, or a specific date).
 *
 * @property todoRepository Repository for Todo task data.
 * @property prayerRepository Repository for Prayer timing data.
 */
class TimelineViewModel(
    private val todoRepository: TodoRepository,
    prayerRepository: PrayerRepository
) : ViewModel() {

    // --- UI State Flows ---

    /** The date currently selected and displayed on the timeline. Defaults to today. */
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    /** Indicates whether data is currently being loaded or processed. */
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Flag to determine whether completed tasks should be hidden from the timeline. */
    private val _hideCompleted = MutableStateFlow(false)
    val hideCompleted: StateFlow<Boolean> = _hideCompleted.asStateFlow()
    private val _isViewingToday = MutableStateFlow(true)
    val isViewingToday: StateFlow<Boolean> = _isViewingToday.asStateFlow()

    init {
        // Start loading sequence upon initialization.
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Placeholder for any async initial setup required.
        }
    }

    // --- User Actions ---

    /** Sets the selected date to tomorrow and reloads the timeline data. */
    fun viewTomorrow() {
        _selectedDate.value = LocalDate.now().plusDays(1)
        _isLoading.value = true
    }

    /** Sets the selected date back to today and reloads the timeline data. */
    fun viewToday() {
        _selectedDate.value = LocalDate.now()
        _isLoading.value = true
    }

    /** Toggles the state of hiding completed tasks and triggers a timeline refresh. */
    fun toggleHideCompleted() {
        _hideCompleted.value = !_hideCompleted.value
        _isLoading.value = true
    }

    /**
     * Marks all Todo tasks scheduled for the given date as done (isDone = true).
     *
     * @param date The specific date for which to mark all tasks as completed.
     */
    fun markAllTasksAsDone(date: LocalDate) {
        viewModelScope.launch {
            // 1. Format the date string required by the repository function.
            val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
            val dateString = date.format(dateFormatter)

            // 2. Call the direct batch update function in the repository.
            todoRepository.markAllTasksAsDone(dateString)
        }
    }


    // --- Data Source Flows (Transformation) ---

    /**
     * Converts the Flow of Todo entities from the local database into a Flow of TimelineEvents.
     */
    private val todosFlow = todoRepository.getAllTodos()
        .map { todos ->
            todos.map { it.toTimelineEvent() }
        }

    /**
     * Fetches prayer timings (assumed to be for Today) and converts them to a Flow of TimelineEvents.
     */
    private val prayerTimingsFlow = prayerRepository.getPrayerTimingsFlow("Cairo", "Egypt")
        .map { timings ->
            // Timings here are typically based on the API response day (often today/current day).
            timings?.toTimelineEvents() ?: emptyList()
        }


    // --- Final Timeline Events Flow (Combination and Logic) ---

    /**
     * The definitive list of events to display on the timeline.
     * This flow combines todos, prayer times, selected date, and the hide completed filter.
     */
    val timelineEvents: StateFlow<List<TimelineEvent>> =
        combine(
            todosFlow,
            prayerTimingsFlow,
            _selectedDate,
            _hideCompleted
        ) { todoEvents, prayerEvents, selectedDate, hideCompleted ->

            // Calculate offset parameters
            val oneDayInMillis = 24 * 60 * 60 * 1000L
            val today = LocalDate.now()
            // Days difference between today and the selected date (0 for today, 1 for tomorrow, etc.)
            val daysOffset = ChronoUnit.DAYS.between(today, selectedDate)
            val offsetMillis = daysOffset * oneDayInMillis
            _isViewingToday.value = daysOffset == 0L
            val currentTime = System.currentTimeMillis()

            // Calculate bounds for filtering events specific to the selected day
            val startOfDay = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = selectedDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()


            // 1. Filter Todo events by the selected date range (startOfDay to endOfDay)
            val todoEventsFilteredByDate = todoEvents
                .filter { it.timestamp in startOfDay until endOfDay }


            // 2. Process and shift Prayer events
            val shiftedPrayerEvents = prayerEvents.flatMap { event ->
                val eventsList = mutableListOf<TimelineEvent>()
                var currentDayTimestamp = event.timestamp // Timestamp of the prayer on the base day (usually today's timing)

                // --- Prayer Shifting Logic ---

                // If viewing TODAY (daysOffset == 0L):
                // If a prayer already occurred (timestamp < currentTime), shift it to tomorrow's timing
                if (daysOffset == 0L && currentDayTimestamp < currentTime) {
                    currentDayTimestamp += oneDayInMillis
                }

                // Apply the full day offset based on selectedDate (0 for today, 1 for tomorrow, etc.)
                // This ensures that if selectedDate is 5 days from now, the prayer timings are shifted 5 days forward.
                val finalTimestamp = currentDayTimestamp + offsetMillis

                val finalEvent = event.copy(timestamp = finalTimestamp)

                // Only include the prayer event if its final shifted timestamp falls within the selected day's bounds
                if (finalEvent.timestamp in startOfDay until endOfDay) {
                    eventsList.add(finalEvent)
                }

                eventsList
            }

            // 3. Combine and filter all events
            var allEvents = (todoEventsFilteredByDate + shiftedPrayerEvents)
                .filter { if (hideCompleted) !it.isDone else true } // Apply hide completed filter
                .sortedBy { it.timestamp } // Sort by time

            // 4. Final filter for Today's view: hide events that happened in the past
            if (daysOffset == 0L) {
                // Keep only events that are scheduled now or in the future (allowing for a 1-hour grace period for events already passed)
                allEvents = allEvents.filter { it.timestamp >= System.currentTimeMillis() - (60 * 60 * 1000) }
            }

            allEvents
        }
            .flowOn(Dispatchers.Default) // Perform heavy filtering and shifting on the default dispatcher
            .onEach {
                _isLoading.value = false // Hide loading indicator once the data is processed
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}