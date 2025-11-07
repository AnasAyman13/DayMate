package com.day.mate.ui.theme.screens.prayer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.day.mate.data.AdhanReceiver
import java.util.*

fun scheduleAdhan(context: Context, prayer: String, hour: Int, minute: Int) {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (before(Calendar.getInstance())) add(Calendar.DATE, 1)
    }

    val intent = Intent(context, AdhanReceiver::class.java).apply {
        putExtra("prayer", prayer)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        prayer.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
}
