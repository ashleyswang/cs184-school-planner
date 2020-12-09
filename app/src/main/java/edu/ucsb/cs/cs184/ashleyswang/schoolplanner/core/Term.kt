package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.Event
import java.time.LocalDateTime

class Term : Scope {
    val TAG: String = "Term"
    override val id: String
    override var name: String
        get() { return name }
        set(value: String) {
            name = value
            this.db.child("name").setValue(name)
        }
    val control: Controller
    override val db: DatabaseReference

    private var _start: Event? = null
    private var _end: Event? = null
    private var _courses: MutableMap<String, Course> = mutableMapOf<String, Course>();
    private var _events: MutableMap<String, Event> = mutableMapOf<String, Event>();

    /*
     * Constructor:
     * @params: None
     * @return:
     * Term object with default name "Term #" and null start and end dates.
     */
    constructor(control: Controller) {
        this.id = Scope.randomString()
        this.control = control
        this.db = control.db.child("terms").child(id)
        this.name = "New Term"
        _addDbListener()
    }

    constructor(control: Controller, key: String, value: Map<String, Any>) {
        this.id = key
        this.control = control
        this.db = control.db.child("terms").child(id)
        this.name = value["name"] as String

        if (value["start"] == null) this._start = null
        else setStart(LocalDateTime.parse(value["start"] as String))

        if (value["end"] == null) this._end = null
        else setEnd(LocalDateTime.parse(value["end"] as String))

        val courses = value["courses"] as Map<String, Map<String, Any>>
        for (key in courses.keys) {
            val course = courses[key]?.let { Course(this, key, it) }
            if (course != null) this._courses.put(key, course)
        }

        val events = value["events"] as Map<String, Map<String, Any>>
        for (key in events.keys) {
            val event = events[key]?.let { Event(this, key, it) }
            if (event != null) this._events.put(key, event)
        }

        _addDbListener()
    }

    fun setStart(date: LocalDateTime?) {
        if (date == null) {
            _start = null
            db.child("start").removeValue()
        } else {
            if (_start == null) _start = Event(this)
            _start!!.start = date
            db.child("start").setValue(date.toString())
        }
    }

    fun getStart() : Event? { return _start }

    fun setEnd(date: LocalDateTime?) {
        if (date == null) {
            _end = null
            db.child("end").removeValue()
        } else {
            if (_start == null) _start = Event(this)
            _start!!.start = date
            db.child("start").setValue(date.toString())
        }
    }

    fun getEnd() : Event? { return _end }

    fun getCourses(): MutableMap<String, Course> { return _courses }

    /*
     * Add Course:
     * @params: None
     * @return: Course object added if successful. Otherwise null.
     */
    fun addCourse(): Course? {
        val course: Course = Course(this)
        return _courses.put(id, course)
    }

    /*
     * Remove Course:
     * @params: id: String - key id for course
     * @return: Course object removed if successful. Otherwise null
     */
    fun removeCourse(id: String): Course? {
        db.child("courses").child(id).removeValue()
        return _courses.remove(id)
    }

    /*
     * Add Deadline/Duration Event:
     * @params:
     * @return: Event object added if successful. Otherwise null.
     */
    fun addEvent(): Event? {
        val event: Event = Event(this)
        return _events.put(event.id, event) as Event
    }

    /*
     * Remove Event:
     * @params: id: String - key id for event
     * @return: Event object removed if successful. Otherwise null.
     */
    fun removeEvent(id: String): Event? {
        db.child("events").child(id).removeValue()
        return _events.remove(id)
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
                if (value != null && _start == null ||
                    value != null && value != _start!!.start.toString())
                    setStart(LocalDateTime.parse(value))
                else if (value == null && _start != null)
                    _start = null
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read start date.", error.toException())
            }
        })

        db.child("end").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String?>()
                if (value != null && _end == null ||
                    value != null && value != _end!!.start.toString())
                    setEnd(LocalDateTime.parse(value))
                else if (value == null && _end != null)
                    _end = null
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read end date.", error.toException())
            }
        })

        // Only deal with new and removed courses (no internal changes)
        db.child("courses").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<Map<String, Map<String, Any>>>()
                if (value!!.keys.minus(_courses.keys).size != 0 ||
                    _courses.keys.minus(value!!.keys).size != 0) {
                    val add: Set<String> = value.keys.minus(_courses.keys)
                    val rem: Set<String> = _courses.keys.minus(value.keys)

                    for (key in add) {
                        val course = value[key]?.let { Course(this@Term, key, it) }
                        if (course != null) _courses.put(key, course)
                    }

                    for (key in rem)
                        _courses.remove(key)
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
                if (value!!.keys.minus(_events.keys).isNotEmpty() ||
                    _events.keys.minus(value!!.keys).isNotEmpty()
                ) {
                    val add: Set<String> = value.keys.minus(_events.keys)
                    val rem: Set<String> = _events.keys.minus(value.keys)

                    for (key in add) {
                        val event = value[key]?.let { Event(this@Term, key, it) }
                        if (event != null) _events.put(key, event)
                    }

                    for (key in rem)
                        _events.remove(key)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read events.", error.toException())
            }
        })
    }
}