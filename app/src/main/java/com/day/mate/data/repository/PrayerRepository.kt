package com.day.mate.data.repository



import com.day.mate.data.local.prayer.PrayerApiService
import com.day.mate.data.local.prayer.Timings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
class PrayerRepository(private val apiService: PrayerApiService) {

    fun getPrayerTimingsFlow(city: String = "Cairo", country: String = "Egypt"): Flow<Timings?> = flow {
        try {
            val response = apiService.getPrayerTimes(city, country)
            emit(response.data.timings)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(null)
        }
    }
}