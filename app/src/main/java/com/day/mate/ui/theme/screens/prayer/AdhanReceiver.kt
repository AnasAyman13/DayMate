package com.day.mate.ui.theme.screens.prayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.day.mate.services.AdhanService

class AdhanReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AdhanReceiver", "Broadcast received. Starting AdhanService.")

        // ✅ استقبال اسم الصلاة بنفس المفتاح المستخدم في الـ Scheduler
        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: "الصلاة"

        val serviceIntent = Intent(context, AdhanService::class.java).apply {
            putExtra("PRAYER_NAME", prayerName)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}