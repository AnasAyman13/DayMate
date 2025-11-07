package com.day.mate.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer

class StopAdhanReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        currentPlayer?.let {
            try {
                it.stop()
                it.release()
            } catch (_: Exception) {}
            currentPlayer = null
        }
    }

    companion object {
        var currentPlayer: MediaPlayer? = null
    }
}
