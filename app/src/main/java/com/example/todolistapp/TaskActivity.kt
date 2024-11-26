package com.example.todolistapp

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.todolistapp.AppDatabase
import com.example.todolistapp.databinding.ActivityTaskBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

const val DB_NAME = "todo.db"
class TaskActivity : AppCompatActivity()
//    , View.OnClickListener
{

    private lateinit var binding : ActivityTaskBinding

    lateinit var myCalendar: Calendar

    lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    lateinit var timeSetListener: TimePickerDialog.OnTimeSetListener

    private val labels = arrayListOf("Personal", "Business", "Insurence", "Shopping", "Banking")

    val db by lazy{
        AppDatabase.getDatabase(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        myCalendar = Calendar.getInstance()

//        binding.dateEdt.setOnClickListener(this)
//        binding.timeEdt.setOnClickListener(this)
//        binding.saveBtn.setOnClickListener(this)

//        setUpSpinner()

    }

//    private fun setUpSpinner() {
//        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, labels)
//        labels.sort()
//        binding.spinnerCategory.adapter = adapter
//
//    }
//
//    override fun onClick(v : View){
//        when(v.id){
//            R.id.dateEdt -> {
//                setDateListener()
//            }
//            R.id.timeEdt -> {
//                setTimeListener()
//            }
//            R.id.saveBtn -> {
//                saveTask()
//            }
//        }
//    }
//
//    private fun saveTask() {
//        val title = binding.taskTitleInput.text.toString().trim()
//        val description = binding.taskDescriptionInput.text.toString().trim()
//        val category = binding.spinnerCategory.selectedItem.toString()
//        val alarmTime = myCalendar.timeInMillis // Full date and time in milliseconds
//
//        // Validate inputs
//        if (title.isEmpty() || alarmTime <= System.currentTimeMillis()) {
//            Toast.makeText(this, "Please enter a valid title and future date/time.", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        // Create a TodoModel object
//        val todoModel = TodoModel(
//            title = title,
//            description = description,
//            category = category,
//            date = alarmTime,
//            time = alarmTime
//        )
//
//        // Insert task asynchronously and set an alarm
//        lifecycleScope.launch {
//            val newTaskId = db.todoDao().insetTask(todoModel)
//            Toast.makeText(this@TaskActivity, "Task saved successfully", Toast.LENGTH_SHORT).show()
//
//            // Schedule alarm for the task
//            scheduleAlarm(newTaskId, alarmTime)
//            finish()
//        }
//    }
//
//    public lateinit var pendingIntent: PendingIntent
//
//    private fun scheduleAlarm(taskId: Long, alarmTime: Long) {
//        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
//
//        // Create an intent for the broadcast receiver
//        val intent = Intent(this, AlarmManagerBroadcast::class.java).apply {
//            putExtra("taskId", taskId)  // Pass task ID
//        }
//
//        // Unique request code derived from taskId
//        val requestCode = taskId.toInt()
//
//        // Create the PendingIntent with unique request code
//        pendingIntent = PendingIntent.getBroadcast(
//            this,
//            requestCode,
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//
//        alarmManager.setExactAndAllowWhileIdle(
//            AlarmManager.RTC_WAKEUP,
//            alarmTime,
//            pendingIntent
//        )
//
//
//    }
//
//    public fun getpendingIntent() = pendingIntent
//
//
//
//
//    private fun setTimeListener() {
//        timeSetListener = TimePickerDialog.OnTimeSetListener{ _: TimePicker, hourofday: Int, minute: Int ->
//            myCalendar.set(Calendar.HOUR_OF_DAY,hourofday)
//            myCalendar.set(Calendar.MINUTE,minute)
//            updateTime()
//        }
//
//        val timePickerDialog = TimePickerDialog(
//            this, timeSetListener, myCalendar.get(Calendar.HOUR_OF_DAY),
//            myCalendar.get(Calendar.MINUTE),
//            false
//        )
//
//        timePickerDialog.show()
//    }
//
//    private fun updateTime() {
//        val myformat = "h : mm a"
//        val sdf = SimpleDateFormat(myformat, Locale.getDefault())
//        binding.timeEdt.setText(sdf.format(myCalendar.time))
//    }
//
//
//    private fun setDateListener() {
//
//        dateSetListener = DatePickerDialog.OnDateSetListener{ _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
//            myCalendar.set(Calendar.YEAR,year)
//            myCalendar.set(Calendar.MONTH,month)
//            myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth)
//            updateDate()
//        }
//
//        val datePickerDialog = DatePickerDialog(
//            this, dateSetListener, myCalendar.get(Calendar.YEAR),
//            myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)
//        )
//
//        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
//        datePickerDialog.show()
//    }
//
//    private fun updateDate() {
//        val myformat = "EEE, d MMM yyyy"
//        val sdf = SimpleDateFormat(myformat)
//        binding.dateEdt.setText(sdf.format(myCalendar.time))
//
//        binding.timeInptLay.visibility = View.VISIBLE
//    }
//
}