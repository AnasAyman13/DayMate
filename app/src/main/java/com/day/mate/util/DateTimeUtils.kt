package com.day.mate.util



import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
fun combineDateTimeToTimestamp(dateStr: String, timeStr: String): Long {
    val zoneId = ZoneId.systemDefault()

    val date = LocalDate.parse(dateStr, DATE_FORMATTER)
    val time = LocalTime.parse(timeStr, TIME_FORMATTER)

    val dateTime = LocalDateTime.of(date, time)
    return dateTime.atZone(zoneId).toInstant().toEpochMilli()
}
fun convertPrayerTimeToTimestamp(timeStr: String): Long {
    val today = LocalDate.now()

    val zoneId = ZoneId.systemDefault()

    val time = try {
        LocalTime.parse(timeStr, TIME_FORMATTER)
    } catch (e: Exception) {
        val cleanTimeStr = timeStr.split(" ")[0]
        LocalTime.parse(cleanTimeStr, TIME_FORMATTER)
    }

    val dateTime = LocalDateTime.of(today, time)

    return dateTime.atZone(zoneId).toInstant().toEpochMilli()
}
fun formatTimestampToHourLabel(timestamp: Long): String {
    val instant = Instant.ofEpochMilli(timestamp)
    val time = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime()
    return time.format(DateTimeFormatter.ofPattern("hh a"))
}