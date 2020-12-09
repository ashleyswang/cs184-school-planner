package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.database.DatabaseReference
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.Event
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.SingleEvent
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.RecurringEvent

@RequiresApi(Build.VERSION_CODES.O)
class Course : Scope {
    override val id: String = Scope.randomString()
    override var name: String
        get() { return name }
        set(value: String) {
            name = value
            this.db.child("name").setValue(name)
        }
    override val db: DatabaseReference
    private val _term: Term
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
        _term = term
        db = term.db.child("courses").child(id)
        name = "New Course"
    }

    /* Getters and Setters */
    fun getTerm(): Term { return _term }
    fun getAssign() : MutableMap<String, Assignment> { return _assign }

    /*
     * Add Assignment:
     * @params: None
     * @return: Assignment object added if successful. Otherwise null.
     */
    fun addAssign(): Assignment? {
        val assign: Assignment = Assignment(this)
        _events.put(assign.event.id, assign.event)
        db.child("events").child(assign.event.id).child("created").setValue(true)
        return _assign.put(id, assign)
    }

    /*
     * Remove Course:
     * @params: id: String - key id for course
     * @return: Course object removed if successful. Otherwise null
     */
    fun removeAssign(id: String): Assignment? {
        db.child("assign").child(id).removeValue()
        db.child("events").child(_assign[id]?.event!!.id).removeValue()
        return _assign.remove(id)
    }

    fun getMeet() : MutableMap<String, Meeting> { return _meet}

    fun addMeet(): Meeting? {
        val meet: Meeting = Meeting(this)
        _events.put(meet.event.id, meet.event)
        db.child("events").child(meet.event.id).child("created").setValue(true)
        return _meet.put(id, meet)
    }

    fun removeMeet(id: String): Meeting? {
        db.child("meet").child(id).removeValue()
        db.child("events").child(_assign[id]?.event!!.id).removeValue()
        return _meet.remove(id)
    }

    /*
     * Add Deadline/Duration Event:
     * @params: None
     * @return: Event object added if successful. Otherwise null.
     */
    fun addSingleEvent(): SingleEvent? {
        val event: SingleEvent = SingleEvent(this)
        db.child("events").child(event.id).child("created").setValue(true)
        return _events.put(event.id, event) as SingleEvent
    }

    fun addRecurEvent(event: RecurringEvent): RecurringEvent? {
        db.child("events").child(event.id).child("created:").setValue(true)
        return _events.put(event.id, event) as RecurringEvent
    }

    /*
     * Remove Event:
     * @params: id: String - key id for event
     * @return: Event object removed if successful. Otherwise null.
     */
    fun removeEvent(id: String): Event? {
        return _events.remove(id)
    }

}