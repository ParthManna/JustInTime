package com.parthasarathimanna.todolistapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.parthasarathimanna.todolistapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationBroadcast : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra("taskId", -1)
        if (taskId == -1L) return

        val channelId = "TaskNotificationChannel"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel (required for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Task Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Notifications for task reminders" }

            notificationManager.createNotificationChannel(channel)
        }

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val task = db.todoDao().getTaskById(taskId)

            if (task != null) {
                val stopIntent = Intent(context, StopReceiver2::class.java).apply {
                    putExtra("taskId", taskId)
                }
                val stopPendingIntent = PendingIntent.getBroadcast(
                    context,
                    taskId.toInt(),
                    stopIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val myFormat = "h:mm a"
                val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
                val time = sdf.format(task.time)

                val notification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.baseline_keyboard_double_arrow_up_24)
                    .addAction(
                        R.drawable.baseline_stop_circle_24,
                        "Dismiss Alarm",
                        stopPendingIntent
                    ) // Ensure this is the first action added
                    .setContentTitle("Upcoming alarm")
                    .setContentText("${time} - ${task.title}")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()


                notificationManager.notify(taskId.toInt(), notification)
            } else {
                // Log error or handle case when task is not found
                Log.e("NotificationBroadcast", "Task not found for taskId: $taskId")
            }
        }
    }
}
