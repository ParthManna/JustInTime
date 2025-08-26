package com.parthasarathimanna.todolistapp

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast

class SnoozeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Check for exact alarm permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(
                context,
                "Cannot snooze alarm. Exact alarm permission not granted.",
                Toast.LENGTH_LONG
            ).show()
            promptForPermission(context)
            return
        }

        // Extract taskId from the intent
        val taskId = intent?.getLongExtra("taskId", -1L) ?: -1L
        if (taskId == -1L) {
            Toast.makeText(context, "Error: Task ID not found.", Toast.LENGTH_SHORT).show()
            return
        }

        val snoozeTimeMillis = System.currentTimeMillis() + 5 * 60 * 1000 // Snooze for 5 minutes
        val snoozeIntent = Intent(context, AlarmManagerBroadcast::class.java).apply {
            putExtra("taskId", taskId) // Pass taskId to the broadcast receiver
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule the exact alarm
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            snoozeTimeMillis,
            pendingIntent
        )

        Toast.makeText(context, "Alarm snoozed for 5 minutes.", Toast.LENGTH_SHORT).show()
        stopAlarm(context) // Stop the current alarm sound and close the activity
    }

    private fun stopAlarm(context: Context) {
        val stopIntent = Intent(context, AlarmSoundService::class.java)
        context.stopService(stopIntent) // Stops the alarm sound

        // stop notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    private fun promptForPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
