package com.day.mate.data.local

import retrofit2.http.GET
import retrofit2.http.Query

data class PrayerResponse(
    val data: PrayerData
)

data class PrayerData(
    val timings: Timings
)

data class Timings(
    val Fajr: String,
    val Dhuhr: String,
    val Asr: String,
    val Maghrib: String,
    val Isha: String
)

interface PrayerApiService {
    @GET("v1/timingsByCity")
    suspend fun getPrayerTimes(
        @Query("city") city: String,
        @Query("country") country: String,
        @Query("method") method: Int = 5
    ): PrayerResponse
}
