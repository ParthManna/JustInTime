package com.parthasarathimanna.todolistapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import com.parthasarathimanna.todolistapp.databinding.ActivityAlarmBinding

class AlarmActivity : AppCompatActivity() {

    private val db by lazy { AppDatabase.Companion.getDatabase(this) }
    private var taskId: Long = -1L // Initialize to -1L as a fallback
    private lateinit var binding: ActivityAlarmBinding

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Retrieve taskId from intent
        taskId = intent.getLongExtra("taskId", -1L)



        // Fetch task details from the database and update UI
        CoroutineScope(Dispatchers.IO).launch {
            val task = db.todoDao().getTaskById(taskId)

            if (task != null) {
                withContext(Dispatchers.Main) {
                    val myFormat = "h:mm a"
                    val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
                    binding.timeDisplay.setText(sdf.format(task.time))
                    binding.label.text = task.title
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AlarmActivity, "Task not found.", Toast.LENGTH_SHORT).show()
                    finish() // Close the activity if task is invalid
                }
            }
        }

//        val drawable = GradientDrawable().apply {
//            shape = GradientDrawable.RECTANGLE
//            cornerRadius = 16f // Set corner radius in pixels
////            setColor(Color.parseColor("#3C1F7B")) // Set background color
//        }
//
//        binding.stopButton.background = drawable
//        binding.snoozeButton.background = drawable

        // Stop the current alarm
        binding.stopButton.setOnClickListener {
            stopAlarm()
        }

        // Snooze the alarm
        binding.snoozeButton.setOnClickListener {
            snoozeAlarm()
        }

        // Check and request exact alarm permission if needed
        if (!AlarmPermissionHelper.hasExactAlarmPermission(this)) {
            AlarmPermissionHelper.requestExactAlarmPermission(this)
            Toast.makeText(
                this,
                "Requesting exact alarm permission. Please grant it to snooze alarms.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun stopAlarm() {
        val intent = Intent(this, AlarmSoundService::class.java)
        stopService(intent) // Stops the alarm sound
        finish() // Close this activity
    }

    private fun snoozeAlarm() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        // Check for exact alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(
                this,
                "Cannot snooze alarm. Exact alarm permission not granted.",
                Toast.LENGTH_LONG
            ).show()
            promptForPermission()
            return
        }

        // Ensure taskId is valid
        if (taskId == -1L) {
            Toast.makeText(this, "Error: Task ID not found.", Toast.LENGTH_SHORT).show()
            return
        }

        val snoozeTimeMillis = System.currentTimeMillis() + 5 * 60 * 1000 // Snooze for 5 minutes
        val snoozeIntent = Intent(this, AlarmManagerBroadcast::class.java).apply {
            putExtra("taskId", taskId) // Pass taskId to the broadcast receiver
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
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

        Toast.makeText(this, "Alarm snoozed for 5 minutes.", Toast.LENGTH_SHORT).show()
        stopAlarm() // Stop the current alarm sound and close the activity
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun promptForPermission() {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error opening permission settings: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
