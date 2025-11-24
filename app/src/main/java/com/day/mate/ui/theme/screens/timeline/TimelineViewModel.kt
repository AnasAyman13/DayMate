package com.day.mate.ui.theme.screens.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.day.mate.data.local.toTimelineEvents
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


class TimelineViewModel(
    private val todoRepository: TodoRepository,
    prayerRepository: PrayerRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _hideCompleted = MutableStateFlow(false)
    val hideCompleted: StateFlow<Boolean> = _hideCompleted.asStateFlow()
    init {
        // نبدأ التحميل عند إنشاء الـ ViewModel.
        // بما أن الـ flows تعمل تلقائياً، سنضع حالة التحميل على false في نهاية تدفق الأحداث.
        loadInitialData()
    }

    // دالة وهمية لإتمام عملية الـ init
    private fun loadInitialData() {
        viewModelScope.launch {
            // يمكن وضع أي عملية تهيئة هنا إن وجدت
        }
    }
    fun viewTomorrow() {
        _selectedDate.value = LocalDate.now().plusDays(1)
        _isLoading.value = true
    }

    fun viewToday() {
        _selectedDate.value = LocalDate.now()
        _isLoading.value = true
    }

    fun toggleHideCompleted() {
        _hideCompleted.value = !_hideCompleted.value
        _isLoading.value = true
    }

    fun markAllTasksAsDone(date: LocalDate) {
        viewModelScope.launch {
            // 1. تنسيق التاريخ المطلوب
            val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
            val dateString = date.format(dateFormatter)

            // 🚨 2. التعديل الحاسم: استدعاء دالة التحديث الجماعي المباشرة
            // هذه الدالة الجديدة تقوم بتحديث جميع المهام في تاريخ معين مباشرةً في قاعدة البيانات
            todoRepository.markAllTasksAsDone(dateString)

            // لم نعد نحتاج إلى:
            // val todosToUpdate = todoRepository.getAllTodos().first().filter...
            // todosToUpdate.forEach { ... }
        }
    }


    private val todosFlow = todoRepository.getAllTodos()
        .map { todos ->
            todos.map { it.toTimelineEvent() }
        }


    private val prayerTimingsFlow = prayerRepository.getPrayerTimingsFlow("Cairo", "Egypt")
        .map { timings ->
            // 🚨 التعديل الأول: نرسل جميع الصلوات كما هي، دون إزاحة إلى الغد هنا.
            timings?.toTimelineEvents() ?: emptyList()
        }


    // --- تدفق الأحداث النهائي (Timeline Events) ---
    val timelineEvents: StateFlow<List<TimelineEvent>> =
        combine(
            todosFlow,
            prayerTimingsFlow,
            _selectedDate,
            _hideCompleted
        ) { todoEvents, prayerEvents, selectedDate, hideCompleted ->

            val oneDayInMillis = 24 * 60 * 60 * 1000L
            val today = LocalDate.now()
            val daysOffset = ChronoUnit.DAYS.between(today, selectedDate)
            val offsetMillis = daysOffset * oneDayInMillis

            val currentTime = System.currentTimeMillis()

            val startOfDay = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = selectedDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()


            val todoEventsFilteredByDate = todoEvents
                .filter { it.timestamp in startOfDay until endOfDay }


            val shiftedPrayerEvents = prayerEvents.flatMap { event ->
                val eventsList = mutableListOf<TimelineEvent>()


                var currentDayTimestamp = event.timestamp


                if (daysOffset == 0L && currentDayTimestamp < currentTime) {

                    currentDayTimestamp += oneDayInMillis
                }


                val finalTimestamp = currentDayTimestamp + offsetMillis


                val finalEvent = event.copy(timestamp = finalTimestamp)


                if (finalEvent.timestamp in startOfDay until endOfDay) {
                    eventsList.add(finalEvent)
                }

                eventsList
            }


            var allEvents = (todoEventsFilteredByDate + shiftedPrayerEvents)
                .filter { if (hideCompleted) !it.isDone else true }
                .sortedBy { it.timestamp }


            if (daysOffset == 0L) {

                allEvents = allEvents.filter { it.timestamp >= System.currentTimeMillis() - (60 * 60 * 1000) }
            }

            allEvents
        }
            .flowOn(Dispatchers.Default)
            .onEach {
                _isLoading.value = false
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}