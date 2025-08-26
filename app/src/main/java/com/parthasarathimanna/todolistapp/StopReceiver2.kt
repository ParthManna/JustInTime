package com.parthasarathimanna.todolistapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat

class StopReceiver2 : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val taskId = intent.getLongExtra("taskId", -1L)

        if (taskId != -1L) {
            Log.d("StopReceiver", "Stopping alarm and notification for taskId: $taskId")

            // Cancel the notification
            cancelNotification(context, taskId)

            // Stop the alarm sound service
            val stopIntent = Intent(context, AlarmSoundService::class.java)
            context.stopService(stopIntent)

            // Cancel the alarm
            cancelAlarm(context, taskId)
        } else{
            Log.e("StopReceiver", "Invalid taskId received in StopReceiver $taskId")
        }
    }

    /**
     * Cancel the notification for the given taskId.
     */
    private fun cancelNotification(context: Context, taskId: Long) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(taskId.toInt())
        Log.d("StopReceiver", "Notification canceled for taskId: $taskId")
    }

    /**
     * Cancel the alarm associated with the given taskId.
     */
    private fun cancelAlarm(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Create an intent matching the one used when scheduling the alarm
        val alarmIntent = Intent(context, AlarmManagerBroadcast::class.java).apply {
            putExtra("taskId", taskId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Cancel the alarm
        alarmManager.cancel(pendingIntent)
        Log.d("StopReceiver", "Alarm canceled for taskId: $taskId")
    }
}
