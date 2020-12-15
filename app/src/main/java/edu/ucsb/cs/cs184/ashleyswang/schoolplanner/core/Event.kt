package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import java.time.Duration
import java.time.LocalDateTime

class Event {
    val TAG: String = "Event"
    val id: String
    val scope: Scope
    val db: DatabaseReference

    var name: String
        get() { return _name }
        set(value: String) {
            _name = value
            db.child("name").setValue(_name)
        }
    var start: LocalDateTime
        get() { return _start }
        set(value: LocalDateTime) {
            _start = value
            db.child("start").setValue(_start.toString())
        }
    var end: LocalDateTime?
        get() { return _end }
        set(value: LocalDateTime?) {
            _end = value
            if (_end != null) db.child("end").setValue(_end.toString())
            else db.child("end").setValue(_end)
        }
    var recurId: String?
        get() { return _recurId }
        set(value) {
            _recurId = value
            db.child("recurId").setValue(_recurId)
        }

    private var _name: String = "New Event"
    private var _start: LocalDateTime = LocalDateTime.now()
    private var _end: LocalDateTime? = null
    private var _recurId: String? = null

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
        _addDbListener()
    }

    /* Constructor used when copying info from database */
    constructor(scope: Scope, key: String, value: Map<String, Any>) {
        this.id = key
        this.scope = scope
        this.db = scope.db.child("events").child(id)

        if (value["name"] != null)
            this._name = value["name"]!! as String
        if (value["start"] != null)
            this._start = LocalDateTime.parse(value["start"] as String)
        if (value["end"] != null)
            this._end = LocalDateTime.parse(value["end"] as String)
        if (value["recurId"] != null)
            this._recurId = value["recurId"] as String

        _addDbListener()
    }

    fun getDuration(): Duration {
        if (_end == null)
            return Duration.between(_start, _start)
        else
            return Duration.between(_start, _end)
    }

    private fun _addDbListener() {
        db.child("name").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String>()
                if (value != null && value != _name) _name = value
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read name.", error.toException())
            }
        })

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

        db.child("recurId").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String?>()
                if (value != null && value != _recurId)
                    _recurId = value
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read end date.", error.toException())
            }
        })
    }


}