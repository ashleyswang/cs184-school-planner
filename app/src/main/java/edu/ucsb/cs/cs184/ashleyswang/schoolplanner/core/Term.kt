package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.Event
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Term : Scope {
    val TAG: String = "Term"
    override val id: String
    val controller: Controller
    override val db: DatabaseReference

    override var name: String
        get() { return _name }
        set(value: String) {
            _name = value
            this.db.child("name").setValue(_name)
        }
    var start: LocalDateTime
        get() { return _start }
        set(value: LocalDateTime) {
            _start = value
            this.db.child("start").setValue(_start.format(format))
        }
    var end: LocalDateTime
        get() { return _end }
        set(value: LocalDateTime) {
            _end = value
            this.db.child("end").setValue(_end.format(format))
        }
    val courses: MutableMap<String, Course>
        get() { return _courses }
    val events: MutableMap<String, Event>
        get() { return _events }
    val createdOn: LocalDateTime
        get() { return _createdOn }

    private val format: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
    private var _name: String = "New Term"
    private var _start: LocalDateTime = LocalDateTime.now()
    private var _end: LocalDateTime = LocalDateTime.now()
    private var _courses: MutableMap<String, Course> = mutableMapOf<String, Course>();
    private var _events: MutableMap<String, Event> = mutableMapOf<String, Event>();
    private var _createdOn: LocalDateTime = LocalDateTime.now()

    constructor(controller: Controller) {
        this.id = Scope.randomString()
        this.controller = controller
        this.db = controller.db.child("terms").child(id)
        this.name = "New Term"
        this.start = LocalDateTime.now()
        this.end = LocalDateTime.now()
        this.db.child("createdOn").setValue(createdOn.format(format))
        _addDbListener()
    }

    constructor(controller: Controller, key: String, value: Map<String, Any>) {
        this.id = key
        this.controller = controller
        this.db = controller.db.child("terms").child(id)

        this._name = value["name"] as String
        if (value["start"] != null)
            this._start = LocalDateTime.parse(value["start"]!! as String, format)
        if (value["end"] != null)
            this._end = LocalDateTime.parse(value["end"]!! as String, format)
        if (value["createdOn"] != null)
            this._createdOn = LocalDateTime.parse(value["createdOn"]!! as String, format)

        val courseInfo = value["courses"] as Map<String, Map<String, Any>>?
        if (courseInfo != null)
            for (pair in courseInfo) {
                val course = Course(this, pair.key, pair.value)
                this._courses.put(course.id, course)
            }

        val eventsInfo = value["events"] as Map<String, Map<String, Any>>?
        if (eventsInfo != null)
            for (pair in eventsInfo) {
                val event = Event(this, pair.key, pair.value)
                this._events.put(event.id, event)
            }
        _addDbListener()
    }

    fun addCourse(): Course {
        val course: Course = Course(this)
        _courses.put(id, course)
        return course
    }

    fun removeCourse(course: Course): Course? {
        db.child("courses").child(course.id).removeValue()
        return _courses.remove(id)
    }

    fun addEvent(): Event {
        val event: Event = Event(this)
        _events.put(event.id, event)
        return event
    }

    fun removeEvent(event: Event): Event? {
        db.child("events").child(event.id).removeValue()
        return _events.remove(id)
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
                val value = dataSnapshot.getValue<String>()
                if (value != null && value != _start.toString())
                    _start = LocalDateTime.parse(value, format)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read start date.", error.toException())
            }
        })

        db.child("end").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String>()
                if (value != null && value != _end.toString())
                    _end = LocalDateTime.parse(value, format)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read end date.", error.toException())
            }
        })

        db.child("createdOn").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String>()
                if (value != null && value != _createdOn.toString())
                    _createdOn = LocalDateTime.parse(value, format)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read end date.", error.toException())
            }
        })

        // Only deal with new and removed courses (no internal changes)
        db.child("courses").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<Map<String, Map<String, Any>>>()
                if (value == null)
                    _courses.clear()
                else if (value.keys.minus(_courses.keys).isNotEmpty() ||
                    _courses.keys.minus(value.keys).isNotEmpty()
                ) {
                    val add: Set<String> = value.keys.minus(_courses.keys)
                    for (key in add) {
                        val course = Course(this@Term, key, value[key]!!)
                        _courses.put(course.id, course)
                    }

                    val rem: Set<String> = _courses.keys.minus(value.keys)
                    for (key in rem) _courses.remove(key)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read courses.", error.toException())
            }
        })

        // Only deal with new and removed events (no internal changes)
        db.child("events").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<Map<String, Map<String, Any>>>()
                if (value == null)
                    _events.clear()
                else if (value.keys.minus(_events.keys).isNotEmpty() ||
                    _events.keys.minus(value.keys).isNotEmpty()
                ) {
                    val add: Set<String> = value.keys.minus(_events.keys)
                    for (key in add) {
                        val event = Event(this@Term, key, value[key]!!)
                        _events.put(key, event)
                    }

                    val rem: Set<String> = _events.keys.minus(value.keys)
                    for (key in rem) _events.remove(key)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read events.", error.toException())
            }
        })
    }
}