package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event

import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Scope
import java.util.*

open class DurationEvent : SingleEvent {
    override val id: String
    override var name: String = "New Deadline"
    override val scope: Scope

    private var _start: Calendar = Calendar.getInstance()
    private var _end: Calendar = Calendar.getInstance()

    /*
     * Constructor:
     * @params: scope: Scope - scope tied to deadline
     *          id: String   - key id used for mapping event in scope
     * @return:
     * DeadlineEvent object with given properties. Courses/Events are both empty.
     */
    constructor(scope: Scope, id: String) {
        this.id = id
        this.scope = scope
    }

    /* Copy Constructor */
    constructor(event: DurationEvent) {
        this.id = Scope.randomString()
        this.scope = event.scope
        this.name = event.name
        this._start = event.getStart()
        this._end = event.getEnd()
    }

    /* Getters and Setters */
    fun getStart(): Calendar { return _start }
    fun setStart(date: Calendar) { _start = date }
    fun getEnd(): Calendar { return _end }
    fun setEnd(date: Calendar) { _end = date }
}