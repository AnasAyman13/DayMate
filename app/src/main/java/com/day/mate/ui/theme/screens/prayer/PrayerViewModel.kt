package com.day.mate.ui.theme.screens.prayer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.day.mate.data.local.RetrofitInstance
import com.day.mate.data.local.Timings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PrayerViewModel : ViewModel() {

    private val _timings = MutableStateFlow<Timings?>(null)
    val timings: StateFlow<Timings?> = _timings

    init {
        loadPrayerTimes()
    }

    fun loadPrayerTimes(city: String = "Cairo", country: String = "Egypt", ctx: Context? = null) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getPrayerTimes(city, country)
                _timings.value = response.data.timings

                // ✅ جدولة الأذان إذا تم تمرير الـ context
                ctx?.let { context ->
                    scheduleAllAdhans(context, response.data.timings)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** جدول الأذان لكل صلاة مفعل لها */
    private fun scheduleAllAdhans(ctx: Context, timings: Timings) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val prayers = listOf(
            "Fajr" to timings.Fajr,
            "Dhuhr" to timings.Dhuhr,
            "Asr" to timings.Asr,
            "Maghrib" to timings.Maghrib,
            "Isha" to timings.Isha
        )

        prayers.forEach { (prayer, timeStr) ->
            val date = try { sdf.parse(timeStr) } catch (_: Exception) { null } ?: return@forEach
            val cal = Calendar.getInstance().apply { time = date }
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)

            // شوف إذا الصلاة مفعلة للأذان
            if (getAdhanPref(ctx, prayer)) {
                scheduleAdhan(
                    context = ctx,
                    prayer = prayer,
                    hour = hour,
                    minute = minute
                )
            } else {
                // ✅ التعديل: إلغاء الجدولة إذا كانت معطلة
                cancelAdhanSchedule(ctx, prayer)
            }
        }
    }
}