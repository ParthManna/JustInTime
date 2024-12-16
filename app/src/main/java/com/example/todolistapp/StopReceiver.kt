package com.example.todolistapp

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StopReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        // Stop the AlarmSoundService
        val stopIntent = Intent(context, AlarmSoundService::class.java)
        context.stopService(stopIntent)

        // Cancel all notifications using NotificationManager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }
}
