package com.day.mate.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.day.mate.R
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class AdhanReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayer = intent.getStringExtra("prayer") ?: return

        // تأكد أن الأذان مفعّل
        val enabled = context.getSharedPreferences("adhan_prefs", Context.MODE_PRIVATE)
            .getBoolean(prayer, false)
        if (!enabled) return

        val mediaPlayer = MediaPlayer.create(context, R.raw.adhan)
        mediaPlayer.start()

        // 🕒 وقف الأذان بعد 20 ثانية
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                mediaPlayer.stop()
                mediaPlayer.release()
            } catch (_: Exception) {}
        }, 20000)

        // 🔔 إنشاء Notification
        showAdhanNotification(context, prayer, mediaPlayer)
    }

    private fun showAdhanNotification(context: Context, prayer: String, mediaPlayer: MediaPlayer) {
        val channelId = "adhan_channel"

        // إنشاء القناة لو أندرويد 8 أو أعلى
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "إشعارات الأذان",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // لما المستخدم يمسح النوتفكيشن، يتوقف الأذان
        val stopIntent = Intent(context, StopAdhanReceiver::class.java)
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.forgrnd)
            .setContentTitle("🕌 حان وقت صلاة $prayer")
            .setContentText("اسحب أو اضغط لإيقاف الأذان")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDeleteIntent(stopPendingIntent)

        // ✅ تحقق من صلاحية POST_NOTIFICATIONS قبل الإرسال
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(prayer.hashCode(), builder.build())
        } else {
            android.util.Log.e("AdhanReceiver", "⛔ Permission POST_NOTIFICATIONS مرفوضة")
        }

        // حفظ الـ MediaPlayer علشان يتقفل لما المستخدم يمسح الإشعار
        StopAdhanReceiver.currentPlayer = mediaPlayer
    }
}
