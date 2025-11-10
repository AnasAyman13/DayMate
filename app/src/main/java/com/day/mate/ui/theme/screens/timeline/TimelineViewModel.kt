package com.day.mate.ui.theme.screens.timeline



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.day.mate.data.local.toTimelineEvents
import com.day.mate.data.model.TimelineEvent
import com.day.mate.data.model.toTimelineEvent
import com.day.mate.data.repository.TodoRepository
import com.day.mate.data.repository.PrayerRepository

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

class TimelineViewModel(
    todoRepository: TodoRepository, // Repository الخاص بالـ Todo
    prayerRepository: PrayerRepository // Repository الخاص بالصلاة
) : ViewModel() {

    // 1. جلب مهام الـ Todo كـ Flow
    private val todosFlow = todoRepository.getAllTodos()
        .map { todos ->
            // تحويل قائمة الـ Todo إلى قائمة TimelineEvent
            todos.map { it.toTimelineEvent() }
        }

    // 2. جلب مواقيت الصلاة كـ Flow
    // يُفضل وضع الموقع في مكان مركزي (مثل SharedPreferences أو Configuration)
    private val prayerTimingsFlow = prayerRepository.getPrayerTimingsFlow("Cairo", "Egypt")
        .map { timings ->
            // تحويل الـ Timings (كائن الصلاة) إلى قائمة TimelineEvent
            timings?.toTimelineEvents() ?: emptyList()
        }

    // 3. دمج كلا الـ Flows في Flow واحد
    val timelineEvents: StateFlow<List<TimelineEvent>> =
        combine(todosFlow, prayerTimingsFlow) { todoEvents, prayerEvents ->
            // دمج القائمتين
            (todoEvents + prayerEvents)
                // فرز القائمة بالـ timestamp
                .sortedBy { it.timestamp }
                // تصفية الأحداث التي حدثت بالفعل (إذا أردنا عرض المستقبل فقط)
                .filter { it.timestamp >= System.currentTimeMillis() - (60 * 60 * 1000) } // عرض الأحداث من ساعة سابقة للوقت الحالي
        }
            .flowOn(Dispatchers.Default) // إجراء عمليات الفرز والتحويل على Thread منفصل
            .stateIn(
                scope = viewModelScope,
                started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}