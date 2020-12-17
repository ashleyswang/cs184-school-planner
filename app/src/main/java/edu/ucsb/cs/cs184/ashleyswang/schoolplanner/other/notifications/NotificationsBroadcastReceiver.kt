package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.other.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationsBroadcastReceiver: BroadcastReceiver() {
    companion object {
        val NOTIFICATION_ID = "notification_id";
        val NOTIFICATION_MESSAGE = "notification_message";
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(NotificationsBroadcastReceiver::class.qualifiedName, "OnReceive")
        val id = intent?.getIntExtra(NotificationsBroadcastReceiver.NOTIFICATION_ID, 0)
        Log.d(NotificationsBroadcastReceiver::class.qualifiedName, "id: " + id.toString())
        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
        val notification: Notification? = intent?.getParcelableExtra<Notification>(NotificationsBroadcastReceiver.NOTIFICATION_MESSAGE);
        if (id != null) {
            notificationManager.notify(id, notification)
            Log.d(NotificationsBroadcastReceiver::class.qualifiedName, "notified")
        }
    }

}