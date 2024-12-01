package com.example.todolistapp


import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import com.example.todolistapp.databinding.ActivityTask2Binding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class TaskActivity2 : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityTask2Binding
    private lateinit var myCalendar: Calendar

    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private lateinit var timeSetListener: TimePickerDialog.OnTimeSetListener

    private val labels = arrayListOf("Personal", "Business", "Insurance", "Shopping", "Banking")

    private val db by lazy { AppDatabase2.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTask2Binding.inflate(layoutInflater)
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

        // Create a TodoModel object
        val todoModel = TodoModel(
            title = title,
            description = description,
            category = category,
            date = alarmTime,
            time = alarmTime
        )

        // Insert task asynchronously and set an alarm
        lifecycleScope.launch {
            val newTaskId = db.todoDao2().insetTask(todoModel)
            Toast.makeText(this@TaskActivity2, "Note saved successfully!", Toast.LENGTH_SHORT).show()

            finish() // Close the activity after saving
        }
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
}
