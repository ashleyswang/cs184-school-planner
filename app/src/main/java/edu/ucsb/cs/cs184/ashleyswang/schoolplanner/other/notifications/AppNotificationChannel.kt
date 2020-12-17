package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.other.notifications

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

class AppNotificationChannel: Application() {
    companion object {
        val CHANNEL_ID: String = "NotificationChannel"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(AppNotificationChannel::class.qualifiedName, "created school planner notification channel")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Notification Channel", NotificationManager.IMPORTANCE_HIGH
            )
            channel.enableVibration(true)
            channel.description = "This is our School Planner Notification Channel"
            channel.enableLights(true)
            val manager = getSystemService(NotificationManager::class.java) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}