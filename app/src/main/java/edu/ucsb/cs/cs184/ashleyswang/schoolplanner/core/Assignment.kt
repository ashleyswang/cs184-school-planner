package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

class Assignment {
    val TAG: String = "Assignment"
    val id: String
    val course: Course
    val db: DatabaseReference
    val eventId: String
        get() { return _eventId }
    val event: Event
        get() { return course.events[_eventId]!! }

    var name: String
        get() { return event.name }
        set(value: String) {
            event.name = value
            db.child("name").setValue(value)
        }

    var date: LocalDateTime
        get() { return event.start }
        set(value: LocalDateTime) {
            event.start = value
            db.child("date").setValue(value.toString())
        }

    var descript: String
        get() { return _descript }
        set(value: String) {
            _descript = value
            db.child("descript").setValue(_descript)
        }

    var completed: Boolean
        get() { return _completed }
        set(value: Boolean) {
            _completed = value
            db.child("completed").setValue(_completed)
        }

    private var _eventId: String = ""
    private var _descript: String = ""
    private var _completed: Boolean = false

    constructor(course: Course, eventId: String) {
        this.id = Scope.randomString()
        this.course = course
        this._eventId = eventId
        this.db = course.db.child("assign").child(id)
        _addDbListener()
    }

    constructor(course: Course, key: String, value: Map<String, Any>) {
        this.id = key
        this.course = course
        this.db = course.db.child("assign").child(id)
        this._eventId = value["eventId"] as String? ?: ""
        this._descript = value["descript"] as String? ?: ""
        this._completed = value["completed"] as Boolean? ?: false
        _addDbListener()
    }

    fun updateDatabase(
        name: String? = null,
        date: LocalDateTime? = null,
        descript: String? = null,
        notifTime: Duration? = null,
        completed: Boolean = false
    ) {
        name?.let { this.name = it }
        date?.let { this.date = it }
        notifTime?.let { event.notifTime = it }
        descript?.let { _descript = it }
        _completed = completed

        val map = mapOf<String, Any?>(
            "eventId"   to  _eventId,
            "name"      to  this.name,
            "date"      to  this.date.toString(),
            "descript"  to  _descript,
            "completed" to  _completed
        )

        db.setValue(map)
    }

    private fun _addDbListener() {
        db.child("eventId").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String>()
                if (value != null && value != _eventId) _eventId = value
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read event id.", error.toException())
            }
        })

        db.child("descript").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String>()
                if (value != null && value != _descript) _descript = value
                else if (value == null) _descript = ""
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read name.", error.toException())
            }
        })

        db.child("completed").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<Boolean>()
                if (value != null && value != _completed) _completed = value
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read name.", error.toException())
            }
        })
    }
}