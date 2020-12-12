package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import java.time.DayOfWeek
import java.time.LocalDateTime

class WeeklyEvent : RecurringEvent {
    val TAG: String = "WeeklyEvent"
    override val type: String = "weekly"
    override val event: Event

    override var start: LocalDateTime
        get() { return _start }
        set(value: LocalDateTime) {
            _start = value
            db.child("start").setValue(_start.toString())
        }
    override var end: LocalDateTime?
        get() { return _end }
        set(value: LocalDateTime?) {
            _end = value
            db.child("end").setValue(_end.toString())
        }
    override val canceled: MutableSet<LocalDateTime>
        get() { return _canceled }
    val days: MutableSet<DayOfWeek>
        get() { return _days }

    private var _start: LocalDateTime = LocalDateTime.now()
    private var _end: LocalDateTime? = null
    private var _canceled: MutableSet<LocalDateTime> = mutableSetOf<LocalDateTime>()
    private var _days: MutableSet<DayOfWeek> = mutableSetOf<DayOfWeek>()

    val db: DatabaseReference

    constructor(event: Event) {
        this.event = event
        this.db = event.db.child("recur")
        this.db.child("type").setValue(type)
        this.start = event.start
        this.end = null
        removeDays(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        _addDbListener()
    }

    constructor(event: Event, info: Map<String, Any>) {
        this.event = event
        this.db = event.db.child("recur")

        if (info["start"] != null)
            this._start = LocalDateTime.parse(info["start"] as String)
        if (info["end"] != null)
            this._end = LocalDateTime.parse(info["end"] as String)

        val daysInfo = info["days"] as Map<String, Boolean>?
        if (daysInfo != null)
            for (pair in daysInfo) {
                if (pair.value)
                    when (pair.key) {
                        "MONDAY"    -> _days.add(DayOfWeek.MONDAY)
                        "TUESDAY"   -> _days.add(DayOfWeek.TUESDAY)
                        "WEDNESDAY" -> _days.add(DayOfWeek.WEDNESDAY)
                        "THURSDAY"  -> _days.add(DayOfWeek.THURSDAY)
                        "FRIDAY"    -> _days.add(DayOfWeek.FRIDAY)
                        "SATURDAY"  -> _days.add(DayOfWeek.SATURDAY)
                        "SUNDAY"    -> _days.add(DayOfWeek.SUNDAY)
                    }
            }

        val cancelInfo = info["canceled"] as ArrayList<String>?
        if (cancelInfo != null)
            for (date in cancelInfo)
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
        db.child("canceled").child(_canceled.size.toString()).setValue(removedDate.toString())
    }

    private fun getDateAfter(date: LocalDateTime): LocalDateTime? {
        if (_end != null && date.isAfter(_end))
            return null
        if (date.isBefore(_start))
            return _start
        val eventDate = event.start
        var newDate: LocalDateTime = date.withHour(eventDate.hour)
            .withMinute(eventDate.minute)
            .withSecond(eventDate.second)

        while (true) {
            if (newDate.isAfter(date) && _days.contains(newDate.dayOfWeek) &&
                _canceled.count { e -> newDate.isEqual(e) } == 0)
                break
            newDate = newDate.plusDays(1.toLong())
        }

        return if (_end != null && newDate.isAfter(_end)) null else newDate
    }

    /* Add Day of the Week to recurrence */
    fun addDays(vararg days : DayOfWeek) {
        for (day in days) {
            _days.add(day)
            when (day) {
                DayOfWeek.MONDAY    -> db.child("days").child("MONDAY").setValue(true)
                DayOfWeek.TUESDAY   -> db.child("days").child("TUESDAY").setValue(true)
                DayOfWeek.WEDNESDAY -> db.child("days").child("WEDNESDAY").setValue(true)
                DayOfWeek.THURSDAY  -> db.child("days").child("THURSDAY").setValue(true)
                DayOfWeek.FRIDAY    -> db.child("days").child("FRIDAY").setValue(true)
                DayOfWeek.SATURDAY  -> db.child("days").child("SATURDAY").setValue(true)
                DayOfWeek.SUNDAY    -> db.child("days").child("SUNDAY").setValue(true)
            }
        }
    }

    /* Remove Day of the Week to recurrence */
    fun removeDays(vararg days : DayOfWeek) {
        for (day in days) {
            _days.remove(day)
            when (day) {
                DayOfWeek.MONDAY    -> db.child("days").child("MONDAY").setValue(false)
                DayOfWeek.TUESDAY   -> db.child("days").child("TUESDAY").setValue(false)
                DayOfWeek.WEDNESDAY -> db.child("days").child("WEDNESDAY").setValue(false)
                DayOfWeek.THURSDAY  -> db.child("days").child("THURSDAY").setValue(false)
                DayOfWeek.FRIDAY    -> db.child("days").child("FRIDAY").setValue(false)
                DayOfWeek.SATURDAY  -> db.child("days").child("SATURDAY").setValue(false)
                DayOfWeek.SUNDAY    -> db.child("days").child("SUNDAY").setValue(false)
            }
        }
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
                if (value != null && _end == null ||
                    value != null && value != _end!!.toString())
                    _end = LocalDateTime.parse(value)
                else if (value == null && _end != null)
                    _end = null
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read end date.", error.toException())
            }
        })

        db.child("days").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<Map<String, Boolean>>()
                if (value != null) {
                    var day: DayOfWeek? = null
                    for (pair in value) {
                        when (pair.key) {
                            "MONDAY" -> day = DayOfWeek.MONDAY
                            "TUESDAY" -> day = DayOfWeek.TUESDAY
                            "WEDNESDAY" -> day = DayOfWeek.WEDNESDAY
                            "THURSDAY" -> day = DayOfWeek.THURSDAY
                            "FRIDAY" -> day = DayOfWeek.FRIDAY
                            "SATURDAY" -> day = DayOfWeek.SATURDAY
                            "SUNDAY" -> day = DayOfWeek.SUNDAY
                        }
                        if (pair.value) _days.add(day!!)
                        else _days.remove(day!!)
                    }
                }

            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read recurrence days.", error.toException())
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