package com.example.todolistapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StopReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        val stopIntent = Intent(context, AlarmSoundService::class.java)
        context.stopService(stopIntent)
    }
}
