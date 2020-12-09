package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Scope
import java.time.*
import kotlin.math.absoluteValue
import kotlin.random.Random

@RequiresApi(Build.VERSION_CODES.O)
class Event {
    val TAG: String = "Event"
    val id: String
    var name: String
        get() { return name }
        set(value: String) {
            name = value
            db.child("name").setValue(name)
        }
    val scope: Scope

    var start: LocalDateTime
        get() { return start }
        set(value: LocalDateTime) {
            start = value
            db.child("start").setValue(start.toString())
        }
    var end: LocalDateTime?
        get() { return end }
        set(value: LocalDateTime?) {
            end = value
            db.child("end").setValue(end.toString())
        }

    val db: DatabaseReference
    var recur: RecurringEvent?
        get() { return recur }
        set(value: RecurringEvent?) {
            recur = value
        }

    /*
     * Constructor:
     * @params: scope: Scope - scope tied to deadline
     * @return:
     * SingleEvent object with given properties. Courses/Events are both empty.
     */
    constructor(scope: Scope) {
        this.id = Scope.randomString()
        this.scope = scope
        this.db = scope.db.child("events").child(id)
        this.name = "New Event"
        this.start = LocalDateTime.now()
        this.end = null
        this.recur = null
        _addDbListener()
    }

    constructor(scope: Scope, key: String, value: Map<String, Any>) {
        this.id = key
        this.scope = scope
        this.db = scope.db.child("events").child(id)
        this.name = value["name"] as String
        this.start = LocalDateTime.parse(value["start"] as String)

        if (value["end"] == null) this.end = null
        else this.end = LocalDateTime.parse(value["end"] as String)

        if (value["recur"] == null) this.recur = null
        else {
            val recurInfo = value["recur"] as Map<String, Any>
            when (recurInfo["type"] as String) {
                "weekly" -> this.recur = WeeklyEvent(this, recurInfo as Map<String, Any>)
                "daily"  -> this.recur = DailyEvent(this, recurInfo as Map<String, Any>)
            }
        }
        _addDbListener()
    }

    fun getDuration(): Duration {
        if (end == null)
            return Duration.between(start, start)
        else
            return Duration.between(start, end)
    }

    private fun _addDbListener() {
        db.child("name").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String>()
                if (value != null && value != name) name = value
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read name.", error.toException())
            }
        })

        db.child("start").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String?>()
                if (value != null && value != start.toString()) start = LocalDateTime.parse(value)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read start date.", error.toException())
            }
        })

        db.child("end").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String?>()
                if (value != null && end == null ||
                    value != null && value != end!!.toString())
                    end = LocalDateTime.parse(value)
                else if (value == null && end != null)
                    end = null
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read end date.", error.toException())
            }
        })


        db.child("recur").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<Map<String, Any>>()
                if (value != null &&
                    (recur == null || recur != null && value["type"] as String != recur!!.type)
                ) {
                    when (value["type"] as String) {
                        "weekly" -> recur = WeeklyEvent(this@Event, value)
                        "daily"  -> recur = DailyEvent(this@Event, value)
                    }
                } else if (value == null )
                    recur = null
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read recurrence.", error.toException())
            }
        })
    }
}