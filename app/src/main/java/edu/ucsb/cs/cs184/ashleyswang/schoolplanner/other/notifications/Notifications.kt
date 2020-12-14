package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.other.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.database.ValueEventListener
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import java.util.*

class Notifications {
    companion object {

        fun setNotification(
            title: String,
            content: String,
            timestamp: Int,
            notificationTime: Long,
            id: String,
            context: Context
        ) {

            var notification =
                NotificationCompat.Builder(context, AppNotificationChannel.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_baseline_calendar_today_24)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(true) //allows user to dismiss when tapped
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER) //this is optional, make sure it creates a category
                    //.setContentText(pendingIntent.toString()) //clickable notification. NOTE: If we do .putExtra() a long time after we create Intent, it gives us null.
                    //source: https://stackoverflow.com/questions/42578842/android-getparcelableextra-object-always-returns-null
                    .build() //this creates the notification
            val intent = Intent(context, Notifications::class.java)
            intent.putExtra(NotificationsBroadcastReceiver.NOTIFICATION_ID, id)
            intent.putExtra(NotificationsBroadcastReceiver.NOTIFICATION_MESSAGE, notification)
            val pendingIntent = PendingIntent.getBroadcast(context, timestamp, intent, 0)

            val alarmManager =
                context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager

            //val zonedDateTime: ZonedDateTime = notificationTime.atZone(TimeZone.getDefault().toZoneId())
            //val timeInMillis: Long = time.toLong() * 1000

            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                //timeInMillis,
                notificationTime,
                //zonedDateTime.toInstant().toEpochMilli(),
                pendingIntent
            )
        }
    }
}