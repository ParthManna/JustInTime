package com.example.todolistapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import android.app.Service
import android.graphics.Color
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlarmSoundService : Service() {

    private lateinit var wakeLock: PowerManager.WakeLock
    private var mediaPlayer: MediaPlayer? = null
    private val db by lazy { AppDatabase.getDatabase(this) }
//    private val db2 by lazy { AppDatabase3.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val taskId = intent?.getLongExtra("taskId", -1L) ?: -1L
        if (taskId == -1L) {
            Log.e("AlarmSoundService", "Invalid taskId received.")
            stopSelf()
            return START_NOT_STICKY
        }

        Log.i("AlarmSoundService", "Starting alarm service for taskId: $taskId")

        // Start playing the alarm sound
        playAlarmSound(taskId)

        // Show the notification with Snooze and Stop actions
        showNotification(taskId)

        // Set auto-dismiss timeout (e.g., 1 hour or less)
        setDismissTimeout(2 * 60 * 1000L) // 1 hour in milliseconds

        return START_NOT_STICKY
    }

    private fun setDismissTimeout(delayMillis: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            kotlinx.coroutines.delay(delayMillis)
            Log.i("AlarmSoundService", "Timeout reached. Stopping alarm and dismissing notification.")
            stopSelf() // Stops the service, dismissing the notification
        }
    }


    private fun playAlarmSound(taskId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val alarmUri = try {
                val task = db.todoDao().getTaskById(taskId)
                val customSoundUri = task?.soundUri
                if (customSoundUri.isNullOrEmpty() || !isUriAccessible(Uri.parse(customSoundUri))) {
                    Log.w("AlarmSoundService", "Custom sound URI is invalid or inaccessible. Using default alarm sound.")
                    Uri.parse("android.resource://${packageName}/raw/alarm_audio")
                } else {
                    Uri.parse(customSoundUri)
                }
            } catch (e: Exception) {
                Log.e("AlarmSoundService", "Error retrieving task or sound URI: ${e.message}", e)
                Uri.parse("android.resource://${packageName}/raw/alarm_audio")
            }

            withContext(Dispatchers.Main) {
                try {
                    mediaPlayer?.release()
                    mediaPlayer = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .build()
                        )
                        setDataSource(applicationContext, alarmUri)
                        setOnPreparedListener { start() }
                        setOnCompletionListener {
                            Log.i("AlarmSoundService", "Alarm sound playback completed.")
                            stopSelf()
                        }
                        prepareAsync()
                    }
                } catch (e: Exception) {
                    Log.e("AlarmSoundService", "Error initializing MediaPlayer: ${e.message}", e)
                    stopSelf()
                }
            }
        }
    }


    private fun isUriAccessible(uri: Uri): Boolean {
        return try {
            contentResolver.openInputStream(uri)?.close()
            true
        } catch (e: SecurityException) {
            Log.e("AlarmSoundService", "URI is not accessible: ${uri.toString()}", e)
            false
        } catch (e: Exception) {
            Log.e("AlarmSoundService", "Error checking URI: ${uri.toString()}", e)
            false
        }
    }

    private fun showNotification(taskId: Long) {
        val channelId = "alarm_service_channel"
        val channelName = "Alarm Notifications"
        val notificationId = 1

        // Step 1: Create a notification channel (required for Android O and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for alarm service notifications"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }

            // Safely create the channel
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        // Step 2: Create intents for Snooze and Stop actions
        val snoozeIntent = Intent(this, SnoozeReceiver::class.java).apply {
            putExtra("taskId", taskId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            this,
            taskId.toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, StopReceiver::class.java)
        val stopPendingIntent = PendingIntent.getBroadcast(
            this,
            taskId.toInt(),
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmIntent = Intent(this, AlarmActivity::class.java).apply {
            putExtra("taskId", taskId) // Pass any necessary extras like taskId to the activity
        }
        val alarmPendingIntent = PendingIntent.getActivity(
            this,
            taskId.toInt(), // You can use taskId or a unique request code
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch the task from the database
                val task = db.todoDao().getTaskById(taskId)

                withContext(Dispatchers.Main) {
                    // Null safety: Handle the case where the task is null
                    if (task == null) {
                        Log.e("NotificationService", "Task not found with ID: $taskId")
                        stopSelf() // Stop the service if no task is found
                        return@withContext
                    }

                    // Step 3: Inflate the custom notification layout
                    val notificationLayout =
                        RemoteViews(applicationContext.packageName, R.layout.custom_notification).apply {
                            setOnClickPendingIntent(R.id.btn_snooze, snoozePendingIntent)
                            setOnClickPendingIntent(R.id.btn_stop, stopPendingIntent)
                            setTextViewText(R.id.notification_title, task.title)
                            setTextViewText(R.id.notification_subtitle, task.description)
                        }

                    // Step 4: Build the notification
                    val notification: Notification = NotificationCompat.Builder(applicationContext, channelId)
                        .setSmallIcon(R.drawable.baseline_keyboard_double_arrow_up_24)
//                        .setCustomContentView(notificationLayout) // Custom compact layout
                        .setCustomBigContentView(notificationLayout) // Custom expanded layout
                        .setContentTitle(task.title) // Accessibility fallback
                        .setContentText(task.description) // Accessibility fallback
                        .setStyle(NotificationCompat.DecoratedCustomViewStyle()) // Enable decorated custom view
                        .setPriority(NotificationCompat.PRIORITY_HIGH) // Heads-up notification
                        .setDefaults(NotificationCompat.DEFAULT_ALL) // Sound, vibration, heads-up
                        .setOngoing(true) // Persistent notification
                        .setAutoCancel(false) // Prevent dismissal by user swipe
                        .setContentIntent(alarmPendingIntent) // Action on click
                        .addAction(R.drawable.baseline_snooze_24, "Snooze", snoozePendingIntent)
                        .addAction(R.drawable.baseline_stop_circle_24, "Stop", stopPendingIntent)
                        .build()

                    // Step 5: Start the foreground service with the notification
                    startForeground(notificationId, notification)
                }
            } catch (e: Exception) {
                Log.e("NotificationService", "Error showing notification: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    stopSelf() // Stop the service on error
                }
            }
        }

    }


    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "ToDoListApp:AlarmWakeLock"
        )
        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        wakeLock.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): android.os.IBinder? {
        // This service is not bound, so return null.
        return null
    }
}
