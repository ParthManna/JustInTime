package com.parthasarathimanna.todolistapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

class AlarmManagerBroadcast : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            Log.e("AlarmManagerBroadcast", "Context or Intent is null.")
            return
        }

        val taskId = intent.getLongExtra("taskId", -1L)
//        val taskId2 = intent.getLongExtra("taskId2", -1L) // Extract taskId2

        if (taskId == -1L) {
            Log.e("AlarmManagerBroadcast", "Invalid taskId or taskId = $taskId")
            return
        }

        Log.i("AlarmManagerBroadcast", "Alarm triggered for taskId: $taskId")

        // Start the foreground service to handle alarm sound and notification
        val serviceIntent = Intent(context, AlarmSoundService::class.java).apply {
            putExtra("taskId", taskId)
//            putExtra("taskId2", taskId2) // Pass taskId2 to the service
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
