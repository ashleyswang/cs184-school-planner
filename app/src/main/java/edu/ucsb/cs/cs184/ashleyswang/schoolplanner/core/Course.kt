package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.DeadlineEvent
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.DurationEvent
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.Event

class Course : Scope {
    override val id: String
    override var name: String = "New Course"
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
    constructor(term: Term, id: String) {
        this.id = id
        _term = term
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
        val id: String = Scope.randomString()
        val assign: Assignment = Assignment(this, id)
        return _assign.put(id, assign)
    }

    /*
     * Remove Course:
     * @params: id: String - key id for course
     * @return: Course object removed if successful. Otherwise null
     */
    fun removeAssign(id: String): Assignment? {
        return _assign.remove(id)
    }

    fun getMeet() : MutableMap<String, Meeting> { return _meet}

    /*
     * Add Assignment:
     * @params: None
     * @return: Assignment object added if successful. Otherwise null.
     */
    fun addMeet(): Meeting? {
        val id: String = Scope.randomString()
        val meet: Meeting = Meeting(this, id)
        return _meet.put(id, meet)
    }

    /*
     * Remove Course:
     * @params: id: String - key id for course
     * @return: Course object removed if successful. Otherwise null
     */
    fun removeMeet(id: String): Meeting? {
        return _meet.remove(id)
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