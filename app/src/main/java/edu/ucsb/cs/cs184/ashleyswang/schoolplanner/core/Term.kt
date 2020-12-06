package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import android.os.Build
import androidx.annotation.RequiresApi
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.DeadlineEvent
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.DurationEvent
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.Event
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
class Term : Scope {
    override val id: String
    override var name: String = "New Term"
    private var _start: DeadlineEvent? = null
    private var _end: DeadlineEvent? = null
    private var _courses: MutableMap<String, Course> = mutableMapOf<String, Course>();
    private var _events: MutableMap<String, Event> = mutableMapOf<String, Event>();

    /*
     * Constructor:
     * @params: None
     * @return:
     * Term object with default name "Term #" and null start and end dates.
     */
    constructor(id: String) {
        this.id = id
    }

    /* Getters and Setters for Term Settings*/
    fun addStart() { _start =
        DeadlineEvent(
            this,
            Scope.randomString()
        )
    }
    fun removeStart() { _start = null }
    fun getStart() : Event? { return _start }
    fun setStart(date: LocalDateTime) {
        if (_start == null) addStart()
        _start!!.setDate(date)
    }

    fun addEnd() {
        _end = DeadlineEvent(
            this,
            Scope.randomString()
        )
    }
    fun removeEnd() { _end = null }
    fun getEnd() : Event? { return _end }
    fun setEnd(date: LocalDateTime) {
        if (_end == null) addEnd()
        _end!!.setDate(date)
    }

    fun getCourses(): MutableMap<String, Course> { return _courses }

    /*
     * Add Course:
     * @params: None
     * @return: Course object added if successful. Otherwise null.
     */
    fun addCourse(): Course? {
        val id: String = Scope.randomString()
        val course: Course = Course(this, id)
        return _courses.put(id, course)
    }

    /*
     * Remove Course:
     * @params: id: String - key id for course
     * @return: Course object removed if successful. Otherwise null
     */
    fun removeCourse(id: String): Course? {
        return _courses.remove(id)
    }

    /*
     * Add Deadline/Duration Event:
     * @params: None
     * @return: Event object added if successful. Otherwise null.
     */
    fun addDeadlineEvent(): DeadlineEvent? {
        val id: String = Scope.randomString()
        val event: Event =
            DeadlineEvent(
                this,
                id
            )
        return _events.put(id, event) as DeadlineEvent
    }

    fun addDurationEvent(): DurationEvent? {
        val id: String = Scope.randomString()
        val event: Event =
            DurationEvent(
                this,
                id
            )
        return _events.put(id, event) as DurationEvent
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