package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import java.time.DayOfWeek
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
            updateEvents("name")
        }
    var start: LocalTime
        get() { return _start }
        set(value) {
            _start = value
            db.child("start").setValue(_start.toString())
            updateEvents("start")
        }
    var end: LocalTime
        get() { return _end }
        set(value) {
            _end = value
            db.child("end").setValue(_end.toString())
            updateEvents("end")
        }
    val createdOn: LocalDateTime
        get() { return _createdOn }

    // [M, T, W, R, F]
    var daysToRepeat: BooleanArray
        get() { return _daysToRepeat }
        set(value) {
            for (i in 0 until 5) {
                db.child("days").child(i.toString()).setValue(value[i])
                if (value[i] && !_daysToRepeat[i]) generateEvents(i)
                else if (!value[i] && _daysToRepeat[i]) removeEvents(i)
            }
        }

    private var _name: String = "New Meeting"
    private var _start: LocalTime = LocalTime.now()
    private var _end: LocalTime = LocalTime.now()
    private var _createdOn: LocalDateTime = LocalDateTime.now()
    private var _daysToRepeat: BooleanArray = BooleanArray(5) { false }

    constructor(course: Course) {
        this.id = Scope.randomString()
        this.course = course
        this.db = course.db.child("meet").child(id)
        this.name = "New Meeting"
        this.start = _start
        this.end = _end
        this.daysToRepeat = _daysToRepeat
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

        if (value["days"] != null) {
            val repeatDaysInfo = value["days"] as ArrayList<Boolean>
            for (i in 0 until 5)
                _daysToRepeat[i] = repeatDaysInfo[i]
        }

        _addDbListener()
    }

    private fun updateEvents(field: String) {
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

    private fun generateEvents(index: Int) {
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
                if (value != null && value != _name) {
                    _name = value
                    updateEvents("name")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read name.", error.toException())
            }
        })

        db.child("start").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String>()
                if (value != null && value != _start.toString()) {
                    _start = LocalTime.parse(value)
                    updateEvents("start")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read start.", error.toException())
            }
        })

        db.child("end").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String>()
                if (value != null && value != _end.toString()) {
                    _end = LocalTime.parse(value)
                    updateEvents("end")
                }
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