package edu.ucsb.cs.cs184.ashleyswang.schoolplanner

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Controller
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.other.notifications.Notifications
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    var controller: Controller = Controller("beepboop")
    private lateinit var termEventsPointer: DatabaseReference
    private lateinit var termsPointer: DatabaseReference
    var courseEventsList: ArrayList<DatabaseReference> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.navigation)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_calendar, R.id.navigation_courses, R.id.navigation_schedule, R.id.navigation_deadlines
            )
        )
        termEventsPointer = controller.db.child("terms").child("events")
        termEventsPointer.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                //our data snapshot is CurrentPointer value.
                val events = dataSnapshot.children
                for (event in events) {
                    //timestamp is the unique ID for our pendingIntent
                    //notificationTime will be the time it should be set to
                    //id is used for notifyManager.
                    var timestamp: Int? = event.child("timestamp").getValue<Int>()
                    if (timestamp == null) {
                        continue
                    }
                    var notificationTime: Long = findSmallestNotificationTime(event.child("notifications"))
                    if (notificationTime == Long.MAX_VALUE) {
                        continue
                    }
                    var title: String? = event.child("title").getValue<String>()
                    if (title == null) {
                        continue
                    }
                    var content: String? = event.child("content").getValue<String>()
                    if (content == null) {
                        continue
                    }
                    var id: String? = event.child("id").getValue<String>()
                    if (id == null) {
                        continue
                    }
                    Notifications.setNotification(title, content, timestamp, notificationTime, id, applicationContext)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Lines", "Failed to read value.", error.toException())
            }

        })
        Log.d(MainActivity::class.qualifiedName, "added listeners for termsEventsPointer")
        termsPointer = controller.db.child("terms")
        termsPointer.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                //our data snapshot is CurrentPointer value.
                if (dataSnapshot.value != null) {
                    //val lines = dataSnapshot.child("Lines")
                    val children = dataSnapshot.children
                    children.forEach{
                        if (it.key != "default") {
                            var events = it.child("events").children
                            for (event in events) {
                                //timestamp is the unique ID for our pendingIntent
                                //notificationTime will be the time it should be set to
                                //id is used for notifyManager.
                                var timestamp: Int? = event.child("timestamp").getValue<Int>()
                                if (timestamp == null) {
                                    continue
                                }
                                var notificationTime: Long = findSmallestNotificationTime(event.child("notifications"))
                                if (notificationTime == Long.MAX_VALUE) {
                                    continue
                                }
                                var title: String? = event.child("title").getValue<String>()
                                if (title == null) {
                                    continue
                                }
                                var content: String? = event.child("content").getValue<String>()
                                if (content == null) {
                                    continue
                                }
                                var id: String? = event.child("id").getValue<String>()
                                if (id == null) {
                                    continue
                                }
                                Notifications.setNotification(title, content, timestamp, notificationTime, id, applicationContext)
                            }
                        }
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

    private fun findSmallestNotificationTime(dataSnapshot: DataSnapshot): Long {
        val format: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
        //val formatDateTime: LocalDateTime = LocalDateTime.parse()
        //NOTE: datetime is usually in UTC, so be careful
        var currentTime: Long = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        //assuming we are in the "notifications" snapshot
        var time: Long = Long.MAX_VALUE
        var children = dataSnapshot.children
        for (child in children) {
            var temp: String? = child.getValue<String>()
            if (temp != null) {
                var tempTime: Long = LocalDateTime.parse(temp, format).atZone(TimeZone.getDefault().toZoneId()).toInstant().toEpochMilli()
                if (tempTime >= currentTime && tempTime < time) {
                    time = tempTime
                }
            }
        }
        return time
    }


}