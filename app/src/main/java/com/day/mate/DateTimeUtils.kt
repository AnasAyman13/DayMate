package com.day.mate



import com.day.mate.data.local.Timings
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// 1. صيغ التاريخ والوقت المستخدمة في تطبيقك
private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/**
 * دالة لتحويل تاريخ ووقت في شكل نصي (Todo Task) إلى Timestamp بالمللي ثانية.
 */
fun combineDateTimeToTimestamp(dateStr: String, timeStr: String): Long {
    // نستخدم الـ ZoneId الافتراضي للجهاز (مهم للتوافق مع الوقت المحلي)
    val zoneId = ZoneId.systemDefault()

    val date = LocalDate.parse(dateStr, DATE_FORMATTER)
    val time = LocalTime.parse(timeStr, TIME_FORMATTER)

    val dateTime = LocalDateTime.of(date, time)

    // تحويل الـ LocalDateTime إلى Long Timestamp
    return dateTime.atZone(zoneId).toInstant().toEpochMilli()
}

/**
 * دالة لتحويل وقت صلاة اليوم (من Timings) إلى Timestamp بالمللي ثانية.
 */
fun convertPrayerTimeToTimestamp(timeStr: String): Long {
    // التاريخ هو اليوم الحالي
    val today = LocalDate.now()

    val zoneId = ZoneId.systemDefault()

    val time = try {
        LocalTime.parse(timeStr, TIME_FORMATTER)
    } catch (e: Exception) {
        // في حالة وجود صيغة غير متوقعة مثل '11:55 (EEST)'
        val cleanTimeStr = timeStr.split(" ")[0]
        LocalTime.parse(cleanTimeStr, TIME_FORMATTER)
    }

    val dateTime = LocalDateTime.of(today, time)

    // تحويل الـ LocalDateTime إلى Long Timestamp
    return dateTime.atZone(zoneId).toInstant().toEpochMilli()
}

/**
 * دالة لعرض الساعة واليوم (مثل 10 AM)
 */
fun formatTimestampToHourLabel(timestamp: Long): String {
    val instant = Instant.ofEpochMilli(timestamp)
    val time = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime()
    return time.format(DateTimeFormatter.ofPattern("hh a")) // مثال: "05 PM"
}