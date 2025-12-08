package com.day.mate.data.local.prayer

import com.day.mate.util.convertPrayerTimeToTimestamp
import com.day.mate.data.model.EventType
import com.day.mate.data.model.TimelineEvent
import com.day.mate.util.formatTimestampToHourLabel
import com.day.mate.ui.theme.EventColorSky
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
fun Timings.toTimelineEvents(): List<TimelineEvent> {
    val prayerMap = mapOf(
        "Fajr" to this.Fajr,
        "Dhuhr" to this.Dhuhr,
        "Asr" to this.Asr,
        "Maghrib" to this.Maghrib,
        "Isha" to this.Isha
    )

    val prayerColor = EventColorSky

    return prayerMap.map { (name, timeStr) ->
        val timestamp = convertPrayerTimeToTimestamp(timeStr)

        TimelineEvent(
            id = "prayer-$name",
            timestamp = timestamp,
            title = "$name Prayer",
            description = "Time to pray $name.",
            timeRange = timeStr.split(" ")[0],
            type = EventType.PRAYER,
            icon = "self_improvement",
            iconColor = prayerColor,
            isDone = false, 
            isProgress = null,
            timeLabel = formatTimestampToHourLabel(timestamp)
        )
    }
}
