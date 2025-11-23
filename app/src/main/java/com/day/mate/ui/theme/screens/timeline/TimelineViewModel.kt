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

    private val _hideCompleted = MutableStateFlow(false)
    val hideCompleted: StateFlow<Boolean> = _hideCompleted.asStateFlow()

    fun viewTomorrow() {
        _selectedDate.value = LocalDate.now().plusDays(1)
    }

    fun viewToday() {
        _selectedDate.value = LocalDate.now()
    }

    fun toggleHideCompleted() {
        _hideCompleted.value = !_hideCompleted.value
    }

    fun markAllTasksAsDone(date: LocalDate) {
        viewModelScope.launch {
            val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
            val dateString = date.format(dateFormatter)

            val todosToUpdate = todoRepository.getAllTodos().first()
                .filter { it.date == dateString && !it.isDone }

            todosToUpdate.forEach { todo ->
                todoRepository.update(todo.copy(isDone = true))
            }
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

            // تحديد حدود اليوم المحدد بالمللي ثانية
            val startOfDay = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = selectedDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            // --- 1. فلترة Todos حسب التاريخ ---
            val todoEventsFilteredByDate = todoEvents
                .filter { it.timestamp in startOfDay until endOfDay }

            // --- 2. تجهيز الصلوات (التعديل الرئيسي) ---
            val shiftedPrayerEvents = prayerEvents.flatMap { event ->
                val eventsList = mutableListOf<TimelineEvent>()

                // 🚨 التعديل الثاني: تطبيق الإزاحة + منطق إزاحة الصلوات الفائتة لليوم التالي إذا كنا نعرض "اليوم"

                // الإزاحة الأساسية: توقيت الصلاة لليوم الحالي (اليوم)
                var currentDayTimestamp = event.timestamp

                // إذا كنا نعرض اليوم، نزيح الصلوات الفائتة منه إلى الغد
                if (daysOffset == 0L && currentDayTimestamp < currentTime) {
                    // إذا كان التوقيت في الماضي (فات)، نزيحه ليوم واحد
                    currentDayTimestamp += oneDayInMillis
                }

                // نطبق الإزاحة الكلية (التي تكون صفراً لليوم الحالي، أو +24 ساعة للغد)
                val finalTimestamp = currentDayTimestamp + offsetMillis

                // ننشئ نسخة الحدث بالتوقيت النهائي
                val finalEvent = event.copy(timestamp = finalTimestamp)

                // نضيف الحدث النهائي إذا كان يقع ضمن اليوم المحدد (startOfDay..endOfDay)
                if (finalEvent.timestamp in startOfDay until endOfDay) {
                    eventsList.add(finalEvent)
                }

                eventsList
            }

            // --- 3. دمج وتطبيق فلاتر العرض ---
            var allEvents = (todoEventsFilteredByDate + shiftedPrayerEvents)
                .filter { if (hideCompleted) !it.isDone else true }
                .sortedBy { it.timestamp }

            // --- 4. فلتر خاص بـ "اليوم" فقط (إخفاء الأحداث الماضية) ---
            if (daysOffset == 0L) {
                // إبقاء الأحداث التي حدثت خلال الساعة الماضية فقط
                allEvents = allEvents.filter { it.timestamp >= System.currentTimeMillis() - (60 * 60 * 1000) }
            }

            allEvents
        }
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}