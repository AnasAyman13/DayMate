package com.day.mate.ui.theme.screens.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.day.mate.data.local.toTimelineEvents
import com.day.mate.data.model.TimelineEvent
import com.day.mate.data.model.toTimelineEvent
import com.day.mate.data.repository.TodoRepository
import com.day.mate.data.repository.PrayerRepository
// ✅ الاستيرادات الضرورية لحل الأخطاء
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.SharingStarted


class TimelineViewModel(
    todoRepository: TodoRepository,
    prayerRepository: PrayerRepository
) : ViewModel() {

    // 1. ✅ يجب إعادة تعريف todosFlow هنا
    private val todosFlow = todoRepository.getAllTodos()
        .map { todos ->
            // تحويل قائمة الـ Todo إلى قائمة TimelineEvent
            todos.map { it.toTimelineEvent() }
        }


    private val prayerTimingsFlow = prayerRepository.getPrayerTimingsFlow("Cairo", "Egypt")
        .map { timings ->
            val currentTime = System.currentTimeMillis()
            val oneDayInMillis = 24 * 60 * 60 * 1000L // ثابت يمثل 24 ساعة

            // تحويل الـ Timings إلى قائمة TimelineEvent (أحداث اليوم)
            val todayEvents = timings?.toTimelineEvents() ?: emptyList()

            // 🔄 المنطق الرئيسي: إزاحة الصلوات التي فاتت إلى اليوم التالي
            val adjustedEvents = todayEvents.map { event ->
                // إذا كان الـ timestamp لهذه الصلاة أقل من الوقت الحالي:
                if (event.timestamp < currentTime) {
                    // أضف 24 ساعة لجعلها صلاة الغد
                    event.copy(timestamp = event.timestamp + oneDayInMillis)
                } else {
                    event
                }
            }
            return@map adjustedEvents

        }

    // 3. دمج كلا الـ Flows في Flow واحد
    val timelineEvents: StateFlow<List<TimelineEvent>> =
        combine(todosFlow, prayerTimingsFlow) { todoEvents, prayerEvents ->
            (todoEvents + prayerEvents)
                .sortedBy { it.timestamp }
                .filter { it.timestamp >= System.currentTimeMillis() - (60 * 60 * 1000) }
        }
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000), // ✅ تم حل خطأ SharingStarted
                initialValue = emptyList()
            )
}