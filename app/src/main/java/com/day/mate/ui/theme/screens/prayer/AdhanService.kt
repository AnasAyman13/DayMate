package com.day.mate.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.day.mate.R
import com.day.mate.MainActivity

private const val CHANNEL_ID = "ADHAN_CHANNEL"
private const val NOTIFICATION_ID = 1
private const val ACTION_STOP_ADHAN = "STOP_ADHAN"

class AdhanService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "تنبيهات الأذان"
            val descriptionText = "قناة لتشغيل صوت الأذان وإرسال التنبيهات"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun stopAdhan() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        stopSelf()
    }

    private fun buildNotification(prayerName: String): Notification {
        val stopIntent = Intent(this, AdhanService::class.java).apply {
            action = ACTION_STOP_ADHAN
        }
        val stopPendingIntent: PendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val launchIntent = Intent(this, MainActivity::class.java)
        val launchPendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("حان وقت صلاة $prayerName")
            .setContentText("الله أكبر، الأذان قيد التشغيل. اضغط لإيقاف الأذان.")
            .setSmallIcon(R.drawable.forgrnd) // تأكد من وجود آيقونة في هذا المسار
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setContentIntent(launchPendingIntent)
            .setDeleteIntent(stopPendingIntent) // ✅ يتم إرسال هذا الـIntent عند مسح الإشعار
            .addAction(0, "إيقاف الأذان", stopPendingIntent)

        return notificationBuilder.build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prayerName = intent?.getStringExtra("PRAYER_NAME") ?: "الصلاة"

        // معالجة نية الإيقاف
        if (intent?.action == ACTION_STOP_ADHAN) {
            Log.d("AdhanService", "Stopping adhan due to user action or dismissal.")
            stopAdhan()
            return START_NOT_STICKY
        }

        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            return START_STICKY
        }

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification(prayerName))

        mediaPlayer = MediaPlayer.create(this, R.raw.adhan).apply {
            setOnCompletionListener {
                stopAdhan()
            }
            try {
                start()
            } catch (e: Exception) {
                Log.e("AdhanService", "Error starting media player", e)
                stopAdhan()
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopAdhan()
    }
}