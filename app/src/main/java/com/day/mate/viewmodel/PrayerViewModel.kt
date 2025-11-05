package com.day.mate.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.day.mate.data.RetrofitInstance
import com.day.mate.data.Timings
import com.day.mate.scheduleAdhan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.day.mate.ui.screens.getAdhanPref

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

            // شوف إذا الصلاة مفعلة للأذان
            if (getAdhanPref(ctx, prayer)) {
                scheduleAdhan(
                    context = ctx,
                    prayer = prayer,
                    hour = cal.get(Calendar.HOUR_OF_DAY),
                    minute = cal.get(Calendar.MINUTE)
                )
            }
        }
    }
}
