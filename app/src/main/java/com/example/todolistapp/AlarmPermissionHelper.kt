package com.example.todolistapp

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

object AlarmPermissionHelper {

    // This method is only needed if you're using `canScheduleExactAlarms()` in your app
    @RequiresApi(Build.VERSION_CODES.S)  // Ensure this is available only for API level 31 and above
    fun hasExactAlarmPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Only call canScheduleExactAlarms() on API 31 or above
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            try {
                alarmManager.canScheduleExactAlarms() // This only works for API 31 and above
            } catch (e: NoSuchMethodError) {
                false
            }
        } else {
            // For devices below API 31, we don't need this check
            true
        }
    }

    // This method prompts users to enable exact alarm permission on Android 12 (API 31) and above
    fun requestExactAlarmPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "Exact alarm permission is not required on this device.", Toast.LENGTH_SHORT).show()
        }
    }
}

