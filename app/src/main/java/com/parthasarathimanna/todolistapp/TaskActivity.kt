package com.parthasarathimanna.todolistapp

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.parthasarathimanna.todolistapp.AlarmPermissionHelper.requestExactAlarmPermission
import com.parthasarathimanna.todolistapp.databinding.ActivityTaskBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.provider.Settings
import com.parthasarathimanna.todolistapp.R
import com.parthasarathimanna.todolistapp.TodoModel

const val DB_NAME = "todo.db"
const val DB_NAME2 = "todo2.db"
const val DB_NAME3 = "todo3.db"

class TaskActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityTaskBinding
    private lateinit var myCalendar: Calendar
    private var ischange : Boolean = false

    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private lateinit var timeSetListener: TimePickerDialog.OnTimeSetListener

    private val labels = arrayListOf("Personal", "Business", "Insurance", "Shopping", "Banking")

    private val db by lazy { AppDatabase.getDatabase(this) }
    private val db2 by lazy { AppDatabase3.getDatabase(this) }

    private val audioPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri: Uri? = result.data?.data
            uri?.let {
                try {
                    // Persist the URI permissions
                    contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    saveAlarmSoundUri(it)
                    val fileName = getFileNameFromUri(it)
                    Toast.makeText(this, "Selected sound: $fileName", Toast.LENGTH_SHORT).show()
                    binding.alarmAudio.text = fileName
                    ischange = true
                } catch (e: SecurityException) {
                    Log.e("AlarmSoundService", "Failed to persist URI permissions: ${e.message}", e)
                    Toast.makeText(this, "Unable to persist URI permissions.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply insets for proper padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        myCalendar = Calendar.getInstance()

        binding.dateEdt.setOnClickListener(this)
        binding.timeEdt.setOnClickListener(this)
        binding.saveBtn.setOnClickListener(this)
        binding.alarmAudio.setOnClickListener(this)

        setupSpinner()
    }

    private fun setupSpinner() {
        labels.sort() // Sort categories alphabetically
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, labels)
        binding.spinnerCategory.adapter = adapter
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.dateEdt -> setDateListener()
            R.id.timeEdt -> setTimeListener()
            R.id.saveBtn -> saveTask()
            R.id.alarm_audio -> openAudioPicker()
        }
    }

    private fun saveTask() {
        val title = binding.taskTitleInput.text.toString().trim()
        val description = binding.taskDescriptionInput.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem?.toString() ?: ""
        val alarmTime = myCalendar.timeInMillis // Full date and time in milliseconds

        // Validate inputs
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a task title.", Toast.LENGTH_SHORT).show()
            return
        }
        if (alarmTime <= System.currentTimeMillis()) {
            Toast.makeText(this, "Please select a future date and time.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedSoundUri : String

//        // Retrieve the selected sound URI from shared preferences
        if(ischange) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            selectedSoundUri = sharedPreferences.getString("alarm_sound_uri", null) ?: ""
        }else{
            selectedSoundUri = ""
        }
        // Create a TodoModel object
        val todoModel = TodoModel(
            title = title,
            description = description,
            category = category,
            date = alarmTime,
            time = alarmTime,
            soundUri = selectedSoundUri

        )



//        val todo = TodoModel2(
//        )

        // Insert task asynchronously and set an alarm
        lifecycleScope.launch {
            val newTaskId = db.todoDao().insetTask(todoModel)
//            val newTaskId2 = db2.todoDao3().insetTask(todo)
            Toast.makeText(this@TaskActivity, "Task saved successfully!", Toast.LENGTH_SHORT).show()

            // Schedule alarm for the task
            scheduleAlarm(newTaskId,alarmTime)
            finish() // Close the activity after saving
        }
    }

    private fun ensureExactAlarmPermission() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 14+
                    Toast.makeText(
                        this,
                        "Go to settings and allow 'Exact Alarms' for proper alarm functionality.",
                        Toast.LENGTH_LONG
                    ).show()

                    // Open settings for user to enable exact alarms manually
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Exact alarm permission required.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun scheduleAlarm(taskId: Long, alarmTime: Long) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        ensureExactAlarmPermission()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            requestExactAlarmPermission(this)
            Toast.makeText(
                this,
                "Exact alarm permission is required to schedule alarms.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val currentTime = System.currentTimeMillis()

        // Schedule the main alarm
        val mainAlarmIntent = Intent(this, AlarmManagerBroadcast::class.java).apply {
            putExtra("taskId", taskId)
            putExtra("alarmTime", alarmTime)
        }
        val mainAlarmPendingIntent = PendingIntent.getBroadcast(
            this,
            taskId.toInt(),
            mainAlarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        // ✅ Ensure Alarm is Set Correctly
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime,
                    mainAlarmPendingIntent
                )
            } else {
                Toast.makeText(this, "Exact alarm permission not granted!", Toast.LENGTH_SHORT).show()
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                mainAlarmPendingIntent
            )
        }

        // Calculate notification time
        val notificationTime = alarmTime - (60 * 60 * 1000) // 1 hour before

        if (notificationTime > currentTime) {

            val notificationIntent = Intent(this, NotificationBroadcast::class.java).apply {
                putExtra("taskId", taskId)
            }
            val notificationPendingIntent = PendingIntent.getBroadcast(
                this,
                taskId.toInt() + 1, // Unique ID for notification
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                notificationTime,
                notificationPendingIntent
            )

        } else {
            // Trigger immediate notification if less than 1 hour remains
            showImmediateNotification(taskId)
        }
    }


    private fun showImmediateNotification(taskId: Long) {
        val intent = Intent(this, NotificationBroadcast::class.java).apply {
            putExtra("taskId", taskId)
        }
        sendBroadcast(intent)
    }



    private fun setDateListener() {
        dateSetListener = DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDate()
        }

        val datePickerDialog = DatePickerDialog(
            this,
            dateSetListener,
            myCalendar.get(Calendar.YEAR),
            myCalendar.get(Calendar.MONTH),
            myCalendar.get(Calendar.DAY_OF_MONTH)
        )

        // Disable past dates
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun updateDate() {
        val myFormat = "EEE, d MMM yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.dateEdt.setText(sdf.format(myCalendar.time))

        // Make the time input visible after selecting a date
        binding.timeInptLay.visibility = View.VISIBLE
    }

    private fun setTimeListener() {
        timeSetListener = TimePickerDialog.OnTimeSetListener { _: TimePicker, hourOfDay: Int, minute: Int ->
            myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            myCalendar.set(Calendar.MINUTE, minute)
            updateTime()
        }

        val timePickerDialog = TimePickerDialog(
            this,
            timeSetListener,
            myCalendar.get(Calendar.HOUR_OF_DAY),
            myCalendar.get(Calendar.MINUTE),
            false
        )

        timePickerDialog.show()
    }

    private fun updateTime() {
        val myFormat = "h:mm a"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.timeEdt.setText(sdf.format(myCalendar.time))

    }

    private fun openAudioPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "audio/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        try {
            audioPickerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to open audio picker.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveAlarmSoundUri(uri: Uri) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        try {
            sharedPreferences.edit().putString("alarm_sound_uri", uri.toString()).apply()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save alarm sound URI.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        return try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex != -1) {
                    cursor.getString(nameIndex)
                } else {
                    "Unknown Sound"
                }
            } ?: "Unknown Sound"
        } catch (e: Exception) {
            "Unknown Sound"
        }
    }
}

