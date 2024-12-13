package com.example.todolistapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi

class SnoozeReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(context: Context, intent: Intent?) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Check if the Android version supports canScheduleExactAlarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e("SnoozeReceiver", "Exact alarm scheduling is not permitted.")
                return
            }
        }

        // Proceed to reschedule the alarm
        val taskId = intent?.getLongExtra("taskId", -1L) ?: -1L
        val snoozeTimeMillis = System.currentTimeMillis() + 5 * 60 * 1000 // Snooze for 5 minutes

        val snoozeIntent = Intent(context, AlarmSoundService::class.java).apply {
            putExtra("taskId", taskId)
        }
        val pendingIntent = PendingIntent.getService(
            context,
            taskId.toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTimeMillis, pendingIntent)

        Log.i("SnoozeReceiver", "Snooze scheduled for taskId: $taskId")
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun promptForPermission(context: Context) {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        context.startActivity(intent)
    }
}
