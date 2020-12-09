package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.database.DatabaseReference
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.Event
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
class Term : Scope {
    override val id: String = Scope.randomString()
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
        this.control = control
        db = control.db.child("terms").child(id)
        name = "New Term"
    }

    fun setStart(date: LocalDateTime) {
        if (_start == null) _start = Event(this)
        _start!!.start = date
        db.child("start").setValue(date)
    }

    fun removeStart() {
        _start = null
        db.child("start").removeValue()
    }

    fun getStart() : Event? { return _start }

    fun setEnd(date: LocalDateTime) {
        if (_end == null) _end = Event(this)
        _end!!.start = date
        db.child("end").setValue(date)
    }

    fun removeEnd() {
        _end = null
        db.child("end").removeValue()
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
        db.child("courses").child(course.id).child("created").setValue(true)
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
        db.child("events").child(event.id).child("created").setValue(true)
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
}