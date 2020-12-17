package edu.ucsb.cs.cs184.ashleyswang.schoolplanner

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Controller
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.other.notifications.AppNotificationChannel
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.other.notifications.NotificationsBroadcastReceiver
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {
    var format: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
    private lateinit var acct: GoogleSignInAccount
    lateinit var controller: Controller
    private lateinit var termEventsPointer: DatabaseReference
    private lateinit var termsPointer: DatabaseReference
    var courseEventsList: ArrayList<DatabaseReference> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createNotificationChannel()
        var isGoogleSignIn = intent.getBooleanExtra("isGoogleSignIn", false)
        var user: String? =  intent.getStringExtra("user")
        Log.d(MainActivity::class.qualifiedName, "user: " + user)
        if (isGoogleSignIn) {
            acct = GoogleSignIn.getLastSignedInAccount(this)!!
            if (acct != null) {
                controller = Controller(acct!!.id.toString())
            }
        }
        else {
            if (user != null) {
                controller = Controller(user!!)
            }
            else {
                controller = Controller("NoUser")
            }
        }
        val navView: BottomNavigationView = findViewById(R.id.navigation)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_calendar, R.id.navigation_manager, R.id.navigation_schedule, R.id.navigation_deadlines
            )
        )
        termsPointer = controller.db.child("terms")
        if (termsPointer == null) {
            Log.d("key", "key is null")
        }
        Log.d("terms key: ", termsPointer.key.toString() + " is terms key")

        termsPointer.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("hi", "dataChanged on eachTermEventsPointer")
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                //our data snapshot is CurrentPointer value.
                if (dataSnapshot.value != null) {
                    Log.d("snapshot", "data snapshot is not null")
                    if (dataSnapshot.hasChildren()) {
                        val terms = dataSnapshot
                        for (term in terms.children) {
                            if (term.key.toString() == "events") {
                                continue
                            }
                            Log.d("term key", term.key.toString())
                            if (term.hasChild("courses")) {
                                val courses = term.child("courses")
                                if (courses.hasChildren()) {
                                    for (course in courses.children) {
                                        if (course.hasChild("events")) {
                                            val events = course.child("events")
                                            if (events.hasChildren()) {
                                                for (event in events.children) {
                                                    if (event.hasChild("notifications")) {
                                                        val notifications = event.child("notifications")
                                                        var timestamp: String? = event.child("timestamp").getValue<String>()
                                                        if (timestamp == null) {
                                                            Log.d("timestamp", "timestamp is null")
                                                            continue
                                                        }
                                                        var timestampInSeconds: Int = (LocalDateTime.parse(timestamp, format).atZone(TimeZone.getDefault().toZoneId()).toInstant().toEpochMilli()/1000).toInt()
                                                        var notificationTime: Long = findSmallestNotificationTime(event.child("notifications"))
                                                        if (notificationTime == Long.MAX_VALUE) {
                                                            Log.d("notificationTime", "notification time is null") //this is null!
                                                            continue
                                                        }
                                                        //Log.d("notification time", convertLongToTime(notificationTime))
                                                        //Log.d("notification time in Long", notificationTime.toString())
                                                        //Log.d("current time in ms: ", System.currentTimeMillis().toString())
                                                        var name: String? = event.child("name").getValue<String>()
                                                        if (name == null) {
                                                            Log.d("name", "name is null")
                                                            continue
                                                        }
                                                        var content: String? = event.child("content").getValue<String>()
                                                        if (content == null) {
                                                            Log.d("content", "content is null")
                                                            continue
                                                        }
                                                        var id: String? = event.child("id").getValue<String>()
                                                        if (id == null) {
                                                            Log.d("id", "id is null")
                                                            continue
                                                        }
                                                        Log.d("[For each term] notification id", "logged id: " + id + " and timestamp: " + timestamp + "and notification time: " + convertLongToTime(notificationTime))
                                                        //Notifications.setNotification(name, content, timestampInSeconds, notificationTime, id, applicationContext)
                                                        sendOnChannel(name, content, timestampInSeconds, notificationTime, id)
                                                    }
                                                    else {
                                                        Log.d("event", "event.notifications does not exist")
                                                    }
                                                }
                                            }
                                            else {
                                                Log.d("events", "empty events list")
                                            }
                                        }
                                        else {
                                            Log.d("course", "course.events does not exist")
                                        }
                                    }
                                }
                                else {
                                    Log.d("courses", "empty courses list")
                                }
                            }
                            else {
                                Log.d("term", "term.courses does not exist")
                            }
                        }
                    }
                    else {
                        Log.d("terms", "empty terms list")
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Lines", "Failed to read value.", error.toException())
            }
        })
        Log.d(MainActivity::class.qualifiedName, "added listeners for termsPointer")
        navView.setupWithNavController(navController)
    }

    private fun findSmallestNotificationTime(notificationsList: DataSnapshot): Long {
        //val format: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
        //val formatDateTime: LocalDateTime = LocalDateTime.parse()
        //NOTE: datetime is usually in UTC, so be careful
        //assuming we are in the "notifications" snapshot
        if (!notificationsList.hasChildren()) {
            return Long.MAX_VALUE
        }
        var time: Long = Long.MAX_VALUE
        var currentTime: Long = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        var notifications = notificationsList.children
        for (notification in notifications) {
            //below returns the data in native types, along with its priority, or null if there is no data at this location.
            var temp: String? = notification.getValue<String>()
            if (temp != null) {
                var tempTime: Long = LocalDateTime.parse(temp, format).atZone(TimeZone.getDefault().toZoneId()).toInstant().toEpochMilli()
                if (tempTime >= currentTime && tempTime < time) {
                    time = tempTime
                }
            }
        }
        return time
    }

    fun convertLongToTime(time: Long): String {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), TimeZone.getDefault().toZoneId()).format(format)
    }

    fun sendOnChannel(name: String, content: String, timestampInSeconds: Int, notificationTime: Long, id: String) {
        Toast.makeText(this, "Notification is set and timed for 10 seconds!", Toast.LENGTH_SHORT);

        var notification = NotificationCompat.Builder(this, AppNotificationChannel.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_calendar_today_24)
            .setContentTitle(name)
            .setContentText(content)
            //.setAutoCancel(true) //allows user to dismiss when tapped
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER) //this is optional, make sure it creates a category
            //.setContentText(pendingIntent.toString()) //clickable notification. NOTE: If we do .putExtra() a long time after we create Intent, it gives us null.
            //source: https://stackoverflow.com/questions/42578842/android-getparcelableextra-object-always-returns-null
            .build() //this creates the notification
        val intent = Intent(this, NotificationsBroadcastReceiver::class.java)
        intent.putExtra(NotificationsBroadcastReceiver.NOTIFICATION_ID, id)
        intent.putExtra(NotificationsBroadcastReceiver.NOTIFICATION_MESSAGE, notification)
        val pendingIntent = PendingIntent.getBroadcast(this, timestampInSeconds, intent, PendingIntent.FLAG_CANCEL_CURRENT)

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        //val timeAtButtonTap: Long = System.currentTimeMillis() //just check if this works okay

        alarmManager.set(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AppNotificationChannel.CHANNEL_ID, "Notification Channel", NotificationManager.IMPORTANCE_HIGH
            )
            channel.enableVibration(true)
            channel.description = "This is our School Planner Notification Channel"
            channel.enableLights(true)
            val manager = getSystemService(NotificationManager::class.java) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        Log.d("MainActivity", "createdNotificationChannel in Main")
    }

}