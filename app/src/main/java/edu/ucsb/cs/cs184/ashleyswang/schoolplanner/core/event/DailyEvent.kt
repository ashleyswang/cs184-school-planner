package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Scope
import java.time.LocalDateTime

class DailyEvent : RecurringEvent {
    val TAG: String = "DailyEvent"
    override val id: String = Scope.randomString()
    override val type: String = "daily"
    override val event: Event

    override var start: LocalDateTime
        get() { return _start }
        set(value: LocalDateTime) {
            _start = value
            db.child("start").setValue(_start)
        }
    override var end: LocalDateTime
        get() { return _end }
        set(value: LocalDateTime) {
            _end = value
            db.child("end").setValue(_end)
        }
    override val canceled: MutableSet<LocalDateTime>
        get () { return _canceled }

    private var _start: LocalDateTime = LocalDateTime.now()
    private var _end: LocalDateTime = LocalDateTime.MAX
    private var _canceled: MutableSet<LocalDateTime> = mutableSetOf<LocalDateTime>()

    val db: DatabaseReference

    constructor(event: Event) {
        this.event = event
        this.db = event.db.child("recur")
        this.db.child("type").setValue(type)
        this.start = event.start
        this.end = LocalDateTime.MAX
        _addDbListener()
    }

    constructor(event: Event, info: Map<String, Any>) {
        this.event = event
        this.db = event.db.child("recur")

        if (info["start"] != null)
            this._start = LocalDateTime.parse(info["start"] as String)
        if (info["end"] != null)
            this._end = LocalDateTime.parse(info["end"] as String)

        val cancelInfo = info["canceled"] as ArrayList<String>?
        if (cancelInfo != null)
            for (date in cancelInfo)
                this._canceled.add(LocalDateTime.parse(date))
        _addDbListener()
    }

    override fun generateEvents() {
        val scope = event.scope
        val duration = event.getDuration()
        val dates = getDates(start, end)
        for (d in dates) {
            val newEvent = scope.addEvent()
            newEvent.name = event.name
            newEvent.start = d
            newEvent.end = d.plus(duration)
            newEvent.recur = event.recur
        }
    }

    override fun removeEvents() {
        val scope = event.scope
        for (e in scope.events.values)
            if (e.recur?.id == this.id)
                scope.removeEvent(e)
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
        if (date.isAfter(_end))
            return null
        if (date.isBefore(_start))
            return _start
        val eventDate = event.start
        var newDate = date.withHour(eventDate.hour)
            .withMinute(eventDate.minute)
            .withSecond(eventDate.second)

        while (true) {
            if (newDate.isAfter(date) &&
                _canceled.count { e -> newDate.isEqual(e) } == 0)
                break
            newDate = newDate.plusDays(1.toLong())
        }

        return if (newDate.isAfter(_end)) null else newDate
    }

    private fun _addDbListener() {
        db.child("start").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String?>()
                if (value != null && value != _start.toString())
                    _start = LocalDateTime.parse(value)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read start date.", error.toException())
            }
        })

        db.child("end").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String?>()
                if (value != null && value != _end!!.toString())
                    _end = LocalDateTime.parse(value)
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