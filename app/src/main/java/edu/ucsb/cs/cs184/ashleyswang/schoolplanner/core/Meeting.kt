package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

// Lecture or Section
// Automatically Make Recurring Meetings
class Meeting {
    val TAG: String = "Meeting"
    val id: String
    val course: Course
    val db: DatabaseReference

    var name: String
        get() { return _name }
        set(value: String) {
            _name = value
            db.child("name").setValue(_name)
        }
    var start: LocalTime
        get() { return _start }
        set(value) {
            _start = value
            db.child("start").setValue(_start.toString())
        }
    var end: LocalTime
        get() { return _end }
        set(value) {
            _end = value
            db.child("end").setValue(_end.toString())
        }
    val createdOn: LocalDateTime
        get() { return _createdOn }
    var notifTime: Duration?
        get() { return _notifTime }
        set(value) {
            _notifTime = value
            if (_notifTime != null)
                db.child("notifTime").setValue(_notifTime.toString())
            else
                db.child("notifTime").removeValue()
        }

    // [M, T, W, R, F]
    var daysToRepeat: BooleanArray
        get() { return _daysToRepeat }
        set(value) {
            for (i in 0 until 5)
                db.child("days").child(i.toString()).setValue(value[i])
        }

    private var _name: String = ""
    private var _start: LocalTime = LocalTime.MIN
    private var _end: LocalTime = LocalTime.MIN
    private var _createdOn: LocalDateTime = LocalDateTime.now()
    private var _notifTime: Duration? = null
    private var _daysToRepeat: BooleanArray = BooleanArray(5) { false }

    constructor(course: Course) {
        this.id = Scope.randomString()
        this.course = course
        this.db = course.db.child("meet").child(id)
        _addDbListener()
    }

    constructor(course: Course, key: String, value: Map<String, Any>) {
        this.id = key
        this.course = course
        this.db = course.db.child("meet").child(id)

        if (value["name"] != null)
            this._name = value["name"]!! as String
        if (value["start"] != null)
            this._start = LocalTime.parse(value["start"] as String)
        if (value["end"] != null)
            this._end = LocalTime.parse(value["end"] as String)
        if (value["createdOn"] != null)
            this._createdOn = LocalDateTime.parse(value["createdOn"] as String)
        if (value["notifTime"] != null)
            this._notifTime = Duration.parse(value["notifTime"] as String)

        if (value["days"] != null) {
            val repeatDaysInfo = value["days"] as ArrayList<Boolean>
            for (i in 0 until 5)
                _daysToRepeat[i] = repeatDaysInfo[i]
        }
        _addDbListener()
    }

    fun updateDatabase(
        name: String? = null,
        start: LocalTime? = null,
        end: LocalTime? = null,
        notifTime: Duration? = null,
        daysToRepeat: BooleanArray? = null
    ) {
        name?.let { _name = it }
        start?.let { _start = it }
        end?.let { _end = it }
        notifTime?.let { _notifTime = it }
        daysToRepeat?.let { _daysToRepeat = it }

        val map = mapOf<String, Any?>(
            "createdOn" to  _createdOn.toString(),
            "name"      to  _name,
            "start"     to  _start.toString(),
            "end"       to  _end.toString(),
            "notifTime" to  _notifTime?.toString(),
            "days"      to  _daysToRepeat.toList()
        )

        db.setValue(map)
    }

    fun updateEvents(field: String) {
        val meetEvents
            = course.events.values.filter { it.recurId == this.id }
        for (event in meetEvents) {
            when (field) {
                "name" -> event.name = name
                "start" ->
                    event.start = event.start.withHour(start.hour).withMinute(start.minute)
                "end" ->
                    event.end = event.end!!.withHour(end.hour).withMinute(end.minute)
            }
        }
    }

    fun meetsOnDay(day: DayOfWeek): Boolean {
        var dayOfWeek: Int
        when (day) {
            DayOfWeek.MONDAY -> dayOfWeek = 0
            DayOfWeek.TUESDAY -> dayOfWeek = 1
            DayOfWeek.WEDNESDAY -> dayOfWeek = 2
            DayOfWeek.THURSDAY -> dayOfWeek = 3
            DayOfWeek.FRIDAY -> dayOfWeek = 4
            else -> dayOfWeek = -1
        }
        return (dayOfWeek > 0) && daysToRepeat[dayOfWeek]
    }

    fun isInitialized(): Boolean {
        return _name != ""
                && _start != LocalTime.MIN
                && _end != LocalTime.MIN
                && _daysToRepeat.contains(true)
    }

    fun removeEvents(index: Int) {
        val dayOfWeek: DayOfWeek?
        when (index) {
            0 -> dayOfWeek = DayOfWeek.MONDAY
            1 -> dayOfWeek = DayOfWeek.TUESDAY
            2 -> dayOfWeek = DayOfWeek.WEDNESDAY
            3 -> dayOfWeek = DayOfWeek.THURSDAY
            4 -> dayOfWeek = DayOfWeek.FRIDAY
            else -> dayOfWeek = null
        }

        val meetEvents
            = course.events.values.filter { it.recurId == this.id }
        for (event in meetEvents) {
            if (dayOfWeek == null
                || dayOfWeek != null && event.start.dayOfWeek == dayOfWeek)
                course.removeEvent(event)
        }
    }

    fun generateEvents(index: Int) {
        val dayOfWeek: DayOfWeek
        when (index) {
            0 -> dayOfWeek = DayOfWeek.MONDAY
            1 -> dayOfWeek = DayOfWeek.TUESDAY
            2 -> dayOfWeek = DayOfWeek.WEDNESDAY
            3 -> dayOfWeek = DayOfWeek.THURSDAY
            4 -> dayOfWeek = DayOfWeek.FRIDAY
            else -> dayOfWeek = DayOfWeek.MONDAY
        }
        var eventStart = makeFirstMeetingOnDay(dayOfWeek)
        var eventEnd = eventStart.withHour(end.hour).withMinute(end.minute)

        while (eventStart.isBefore(course.term.end)) {
            var event = course.addEvent()
            event.recurId = this.id
            event.name = this.name
            event.start = eventStart
            event.end = eventEnd

            eventStart = eventStart.plusDays(7.toLong())
            eventEnd = eventEnd.plusDays(7.toLong())
        }
    }

    private fun makeFirstMeetingOnDay(dayOfWeek: DayOfWeek): LocalDateTime {
        var eventDate = course.term.start
            .withHour(start.hour).withMinute(start.minute)

        while (eventDate.dayOfWeek != dayOfWeek)
            eventDate = eventDate.plusDays(1.toLong())

        return eventDate
    }

    private fun _addDbListener() {
        db.child("name").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String>()
                if (value != null && value != _name)
                    _name = value
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read name.", error.toException())
            }
        })

        db.child("start").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String>()
                if (value != null && value != _start.toString())
                    _start = LocalTime.parse(value)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read start.", error.toException())
            }
        })

        db.child("end").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String>()
                if (value != null && value != _end.toString())
                    _end = LocalTime.parse(value)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read end.", error.toException())
            }
        })

        db.child("createdOn").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String>()
                if (value != null && value != _createdOn.toString())
                    _createdOn = LocalDateTime.parse(value)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read creation date.", error.toException())
            }
        })

        db.child("notifTime").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String>()
                if (value != null && value != _notifTime.toString())
                    _notifTime = Duration.parse(value)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read end date.", error.toException())
            }
        })

        db.child("days").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<ArrayList<Boolean>>()
                if (value != null ) {
                    for (i in 0 until 5)
                        _daysToRepeat[i] = value[i]
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read name.", error.toException())
            }
        })
    }

}