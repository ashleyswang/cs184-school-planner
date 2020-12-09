package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.other.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.MainActivity
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R

class Notifications: BroadcastReceiver() {
    companion object {
        val NOTIFICATION_ID = "notification_id";
        val NOTIFICATION_MESSAGE = "notification_message";
    }

    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.d("Notifications", "OnReceive")
        val id = p1?.getIntExtra(Notifications.NOTIFICATION_ID, 0)
        Log.d("Notifications", "id: " + id.toString())
        val notificationManager =
            p0?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
        val notification: Notification? = p1?.getParcelableExtra<Notification>(Notifications.NOTIFICATION_MESSAGE);
        if (id != null) {
            notificationManager.notify(id, notification)
            Log.d("Notifications", "notified")
        }
    }

}