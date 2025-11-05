package com.day.mate.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.day.mate.data.RetrofitInstance
import com.day.mate.data.Timings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PrayerViewModel : ViewModel() {

    private val _timings = MutableStateFlow<Timings?>(null)
    val timings: StateFlow<Timings?> = _timings

    init {
        loadPrayerTimes()
    }

    fun loadPrayerTimes(city: String = "Cairo", country: String = "Egypt") {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getPrayerTimes(city, country)
                _timings.value = response.data.timings
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
