package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import java.time.Duration
import java.time.LocalDateTime

class Course : Scope {
    val TAG: String = "Course"
    override val id: String
    val term: Term
    override val db: DatabaseReference

    override var name: String
        get() { return _name }
        set(value: String) {
            _name = value
            this.db.child("name").setValue(_name)
        }
    val assign: MutableMap<String, Assignment>
        get() { return _assign }
    val meet: MutableMap<String, Meeting>
        get() { return _meet }
    override val events: MutableMap<String, Event>
        get() { return _events }

    private var _name: String = "New Course"
    private var _assign: MutableMap<String, Assignment> = mutableMapOf<String, Assignment>()
    private var _meet: MutableMap<String, Meeting> = mutableMapOf<String, Meeting>()
    private var _events: MutableMap<String, Event> = mutableMapOf<String, Event>()
    // TO DO: add office hours/instructor info

    /*
     * Constructor:
     * @params: term: Term - term tied to course
     *          id: String - key id used for mapping term events
     * @return:
     * Term object with given properties. Courses/Events are both empty.
     */
    constructor(term: Term) {
        this.id = Scope.randomString()
        this.term = term
        this.db = term.db.child("courses").child(id)
        this.name = "New Course"
        _addDbListener()
    }

    constructor(term: Term, key: String, value: Map<String, Any>) {
        this.id = key
        this.term = term
        this.db = term.db.child("courses").child(id)

        this._name = value["name"] as String
        val eventsInfo = value["events"] as Map<String, Map<String, Any>>?
        if (eventsInfo != null)
            for (pair in eventsInfo) {
                val event =
                    Event(
                        this,
                        pair.key,
                        pair.value
                    )
                this._events.put(event.id, event)
            }

        val assignInfo = value["assign"] as Map<String, Map<String, Any>>?
        if (assignInfo != null)
            for (pair in assignInfo) {
                val assign = Assignment(this, pair.key, pair.value)
                this._assign.put(assign.id, assign)
            }

        val meetInfo = value["meet"] as Map<String, Map<String, Any>>?
        if (meetInfo != null)
            for (pair in meetInfo) {
                val meet = Meeting(this, pair.key, pair.value)
                this._meet.put(meet.id, meet)
            }
        _addDbListener()
    }

    /* Getters and Setters */
    fun addAssign(): Assignment {
        val event: Event =
            Event(this)
        _events.put(event.id, event)
        event.isAssign = true
        val assign: Assignment = Assignment(this, event.id)
        _assign.put(assign.id, assign)
        return assign
    }

    fun addAssign(
        name: String,
        dueDate: LocalDateTime,
        descript: String,
        notifTime: Duration?
    ): Assignment {
        val event: Event = Event(this)
        _events.put(event.id, event)
        event.updateDatabase(name, dueDate, null, notifTime, true)
        val assign: Assignment = Assignment(this, event.id)
        val map = mapOf<String, Any?>(
            "eventId"   to event.id,
            "name"      to name,
            "date"      to dueDate.toString(),
            "descript"  to descript,
            "completed" to false
        )
        assign.db.setValue(map)
        _assign.put(assign.id, assign)
        return assign
    }

    fun removeAssign(assign: Assignment): Assignment? {
        db.child("assign").child(assign.id).removeValue()
        db.child("events").child(assign.eventId).removeValue()
        return _assign.remove(id)
    }

    fun addMeet(): Meeting {
        val meet: Meeting = Meeting(this)
        _meet.put(meet.id, meet)
        return meet
    }

    fun removeMeet(meet: Meeting): Meeting? {
        meet.removeEvents(5)
        db.child("meet").child(meet.id).removeValue()
        return _meet.remove(id)
    }

    override fun addEvent(): Event {
        val event: Event =
            Event(this)
        _events.put(event.id, event)
        return event
    }

    override fun removeEvent(event: Event): Event? {
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
                        val event =
                            Event(
                                this@Course,
                                key,
                                value[key]!!
                            )
                        _events.put(event.id, event)
                    }

                    val rem: Set<String> = _events.keys.minus(value.keys)
                    for (key in rem) _events.remove(key)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read events.", error.toException())
            }
        })

        // Only deal with new and removed assignments (no internal changes)
        db.child("assign").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<Map<String, Map<String, Any>>>()
                if (value == null)
                    _assign.clear()
                else if (value.keys.minus(_assign.keys).isNotEmpty() ||
                    _assign.keys.minus(value.keys).isNotEmpty()
                ) {
                    val add: Set<String> = value.keys.minus(_assign.keys)
                    for (key in add) {
                        val assign = Assignment(this@Course, key, value[key]!!)
                        _assign.put(assign.id, assign)
                    }

                    val rem: Set<String> = _assign.keys.minus(value.keys)
                    for (key in rem) _assign.remove(key)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read assignments.", error.toException())
            }
        })

        // Only deal with new and removed meetings (no internal changes)
        db.child("meet").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<Map<String, Map<String, Any>>>()
                if (value == null)
                    _meet.clear()
                else if (value.keys.minus(_meet.keys).isNotEmpty() ||
                    _meet.keys.minus(value.keys).isNotEmpty()
                ) {
                    val add: Set<String> = value.keys.minus(_meet.keys)
                    for (key in add) {
                        val meet = Meeting(this@Course, key, value[key]!!)
                        _meet.put(meet.id, meet)
                    }

                    val rem: Set<String> = _meet.keys.minus(value.keys)
                    for (key in rem) _meet.remove(key)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read meetings.", error.toException())
            }
        })
    }

}