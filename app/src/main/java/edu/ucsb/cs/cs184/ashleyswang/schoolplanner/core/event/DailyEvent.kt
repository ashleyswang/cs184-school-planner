package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import java.time.DayOfWeek
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
class DailyEvent : RecurringEvent {
    val TAG: String = "DailyEvent"
    override val type: String = "daily"
    override var start: LocalDateTime
        get() { return start }
        set(value: LocalDateTime) {
            start = value
            db.child("start").setValue(start)
        }
    override var end: LocalDateTime?
        get() { return end }
        set(value: LocalDateTime?) {
            end = value
            db.child("end").setValue(end)
        }
    override var event: Event
    private var _canceled: MutableSet<LocalDateTime> = mutableSetOf<LocalDateTime>()

    val db: DatabaseReference

    constructor(event: Event) {
        this.event = event
        this.db = event.db.child("recur")
        this.start = event.start
        this.end = null
        _addDbListener()
    }

    constructor(event: Event, info: Map<String, Any>) {
        this.event = event
        this.db = event.db.child("recur")
        this.start = LocalDateTime.parse(info["start"] as String)

        if (info["end"] == null) this.end = null
        else this.end = LocalDateTime.parse(info["end"] as String)

        val canceled = info["canceled"] as ArrayList<String>
        for (date in canceled)
            this._canceled.add(LocalDateTime.parse(date))
        _addDbListener()
    }

    /* Returns next event based on today's date */
    override fun getNextDate(): LocalDateTime? {
        val current: LocalDateTime = LocalDateTime.now()
        return getDateAfter(current)
    }

    /* Returns all events from "from" to "to" */
    override fun getDates(from: LocalDateTime, to: LocalDateTime): ArrayList<LocalDateTime> {
        var events: ArrayList<LocalDateTime> = ArrayList<LocalDateTime>()
        var curr: LocalDateTime? = getDateAfter(from)
        while (curr != null && curr.isAfter(to)) {
            events.add(curr)
            curr = getDateAfter(curr)
        }
        return events
    }

    /* Removes single event from recurrence with matching date */
    override fun removeDate(date: LocalDateTime) {
        val eventDate = event.start
        val removedDate: LocalDateTime = date.withHour(eventDate.hour)
            .withMinute(eventDate.minute)
            .withSecond(eventDate.second)
        _canceled.add(removedDate)
        db.child("canceled").child(_canceled.size.toString()).setValue(removedDate)
    }

    private fun getDateAfter(date: LocalDateTime): LocalDateTime? {
        if (end != null && date.isAfter(end))
            return null
        if (date.isBefore(start))
            return start
        val eventDate = event.start
        var newDate = date.withHour(eventDate.hour)
            .withMinute(eventDate.minute)
            .withSecond(eventDate.second)

        while (!newDate.isAfter(date))
            newDate = newDate.plusDays(1.toLong())

        if (end != null && newDate.isAfter(end)) return null
        else return newDate
    }

    private fun _addDbListener() {
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

        db.child("canceled").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<ArrayList<String>>()
                if (value != null)
                    for (date in value)
                        _canceled.add(LocalDateTime.parse(date))
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read canceled dates.", error.toException())
            }
        })
    }
}