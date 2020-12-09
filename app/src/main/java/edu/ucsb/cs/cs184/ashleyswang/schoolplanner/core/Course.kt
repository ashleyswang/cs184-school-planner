package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.Event

class Course : Scope {
    val TAG: String = "Course"
    override val id: String
    override var name: String
        get() { return name }
        set(value: String) {
            name = value
            this.db.child("name").setValue(name)
        }
    override val db: DatabaseReference
    val term: Term
    private var _assign: MutableMap<String, Assignment> = mutableMapOf<String, Assignment>();
    private var _meet: MutableMap<String, Meeting> = mutableMapOf<String, Meeting>();
    private var _events: MutableMap<String, Event> = mutableMapOf<String, Event>();
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
        this.name = value["name"] as String

        val events = value["events"] as Map<String, Map<String, Any>>
        for (key in events.keys) {
            val event = events[key]?.let { Event(this, key, it) }
            if (event != null) this._events.put(key, event)
        }

        val assigns = value["assign"] as Map<String, Map<String, Any>>
        for (key in assigns.keys) {
            val assign = assigns[key]?.let { Assignment(this, key, it) }
            if (assign != null) this._assign.put(key, assign)
        }

        val meets = value["meet"] as Map<String, Map<String, Any>>
        for (key in meets.keys) {
            val meet = meets[key]?.let { Meeting(this, key, it) }
            if (meet != null) this._meet.put(key, meet)
        }
        _addDbListener()
    }

    /* Getters and Setters */
    fun getAssigns() : MutableMap<String, Assignment> { return _assign }
    /*
     * Add Assignment:
     * @params: None
     * @return: Assignment object added if successful. Otherwise null.
     */
    fun addAssign(): Assignment? {
        val event: Event = Event(this)
        val assign: Assignment = Assignment(this, event.id)
        _events.put(event.id, event)
        return _assign.put(assign.id, assign)
    }

    /*
     * Remove Course:
     * @params: id: String - key id for course
     * @return: Course object removed if successful. Otherwise null
     */
    fun removeAssign(id: String): Assignment? {
        db.child("assign").child(id).removeValue()
        db.child("events").child(_assign[id]!!.eventId).removeValue()
        return _assign.remove(id)
    }

    fun getMeets() : MutableMap<String, Meeting> { return _meet}

    fun addMeet(): Meeting? {
        val event: Event = Event(this)
        val meet: Meeting = Meeting(this, event.id)
        _events.put(event.id, event)
        return _meet.put(meet.id, meet)
    }

    fun removeMeet(id: String): Meeting? {
        db.child("meet").child(id).removeValue()
        db.child("events").child(_meet[id]!!.eventId).removeValue()
        return _meet.remove(id)
    }

    /*
     * Add Deadline/Duration Event:
     * @params: None
     * @return: Event object added if successful. Otherwise null.
     */
    /*
     * Add Deadline/Duration Event:
     * @params:
     * @return: Event object added if successful. Otherwise null.
     */

    fun getEvents(): MutableMap<String, Event> { return _events }
    fun getEvent(key: String): Event? {
        return _events[key]
    }

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
                        val event = value[key]?.let { Event(this@Course, key, it) }
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

        // Only deal with new and removed assignments (no internal changes)
        db.child("assign").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<Map<String, Map<String, Any>>>()
                if (value!!.keys.minus(_assign.keys).isNotEmpty() ||
                    _assign.keys.minus(value!!.keys).isNotEmpty()
                ) {
                    val add: Set<String> = value.keys.minus(_assign.keys)
                    val rem: Set<String> = _assign.keys.minus(value.keys)

                    for (key in add) {
                        val assign = value[key]?.let { Assignment(this@Course, key, it) }
                        if (assign != null) _assign.put(key, assign)
                    }

                    for (key in rem)
                        _assign.remove(key)
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
                if (value!!.keys.minus(_meet.keys).isNotEmpty() ||
                    _meet.keys.minus(value!!.keys).isNotEmpty()
                ) {
                    val add: Set<String> = value.keys.minus(_meet.keys)
                    val rem: Set<String> = _meet.keys.minus(value.keys)

                    for (key in add) {
                        val meet = value[key]?.let { Meeting(this@Course, key, it) }
                        if (meet != null) _meet.put(key, meet)
                    }

                    for (key in rem)
                        _meet.remove(key)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read meetings.", error.toException())
            }
        })
    }

}