package edu.ucsb.cs.cs184.ashleyswang.schoolplanner

import android.app.*
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Debug
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.other.notifications.CustomNotificationChannel
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.other.notifications.Notifications

class NotificationsTest : AppCompatActivity() {
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var editableTitle: EditText
    private lateinit var editableContent: EditText
    private lateinit var editableTime: EditText
    private lateinit var editableId: EditText
    private lateinit var button: Button

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications_test)
        notificationManager = NotificationManagerCompat.from(this) //gets reference from notificationManagerCompat and allows us to send
        editableTitle = findViewById(R.id.edit_text_title)
        editableContent = findViewById(R.id.edit_text_content)
        editableTime = findViewById(R.id.edit_text_time)
        editableId = findViewById(R.id.edit_text_id)
        button = findViewById(R.id.button)
        button.setOnClickListener{
            sendOnChannel(it)
        }
    }

    fun sendOnChannel(v: View) {
        Toast.makeText(this, "Notification is set and timed for 10 seconds!", Toast.LENGTH_SHORT);
        var title = editableTitle.text.toString()
        var content = editableContent.text.toString()
        var time = Integer.parseInt(editableTime.text.toString())
        var id = Integer.parseInt(editableId.text.toString())
        if (id == null) {
            id = 0
        }
        if (time == null) {
            time = 0
        }
        else {
            time *= 1000 //ms
            Toast.makeText(this, "Notification is set for " + time.toString() + " ms!", Toast.LENGTH_SHORT).show()
        }

        var notification = NotificationCompat.Builder(this, CustomNotificationChannel.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_calendar_today_24)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true) //allows user to dismiss when tapped
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER) //this is optional, make sure it creates a category
            //.setContentText(pendingIntent.toString()) //clickable notification. NOTE: If we do .putExtra() a long time after we create Intent, it gives us null.
            //source: https://stackoverflow.com/questions/42578842/android-getparcelableextra-object-always-returns-null
            .build() //this creates the notification
        val intent = Intent(this, Notifications::class.java)
        intent.putExtra(Notifications.NOTIFICATION_ID, id)
        intent.putExtra(Notifications.NOTIFICATION_MESSAGE, notification)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        val timeAtButtonTap: Long = System.currentTimeMillis()

        alarmManager.set(AlarmManager.RTC_WAKEUP, timeAtButtonTap + time, pendingIntent);
    }


}