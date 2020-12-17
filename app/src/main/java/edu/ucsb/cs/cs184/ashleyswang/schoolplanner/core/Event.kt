package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
            if (_end != null) db.child("end").setValue(_end!!.toString())
            else db.child("end").setValue(_end)
        }
    var recurId: String?
        get() { return _recurId }
        set(value) {
            _recurId = value
            db.child("recurId").setValue(_recurId)
        }
    var isAssign: Boolean
        get() { return _isAssign }
        set(value) {
            _isAssign = value
            db.child("isAssign").setValue(_isAssign)
        }
    val createdOn: String //this is System.CurrentTimeMillis/1000 to be put into seconds
        get() { return _createdOn }
    var notifTime: Duration?
        get() { return _notifTime }
        set(value) {
            _notifTime = value
            db.child("notifTime").setValue(_notifTime.toString())
        }

    private val _format: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
    private var _name: String = "New Event"
    private var _start: LocalDateTime = LocalDateTime.now()
    private var _end: LocalDateTime? = null
    private var _recurId: String? = null
    private var _isAssign: Boolean = false
    private var _createdOn: String = LocalDateTime.now().format(_format)
    private var _notifTime: Duration? = null

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
        this.isAssign = false
        this.db.child("createdOn").setValue(_createdOn)
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
        if (value["isAssign"] != null)
            this._isAssign = value["isAssign"] as Boolean
        if (value["createdOn"] != null)
            this._createdOn = value["timestamp"] as String
        if (value["notifTime"] != null)
            this._notifTime = Duration.parse(value["notifTime"] as String)

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

        db.child("isAssign").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<Boolean?>()
                if (value != null && value != _isAssign)
                    _isAssign = value
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read end date.", error.toException())
            }
        })

        db.child("createdOn").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String>()
                if (value != null && value != _createdOn)
                    _createdOn = value
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read end date.", error.toException())
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
    }


}