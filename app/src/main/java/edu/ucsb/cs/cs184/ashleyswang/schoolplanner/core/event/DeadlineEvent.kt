package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event

import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Scope
import java.util.*

open class DeadlineEvent : SingleEvent {
    override val id: String
    override var name: String = "New Deadline"
    override val scope: Scope

    private var _date: Calendar = Calendar.getInstance()

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
    constructor(event: DeadlineEvent) {
        this.id = Scope.randomString()
        this.scope = event.scope
        this.name = event.name
        this._date = event.getDate()
    }

    /* Getters and Setters */
    fun getDate(): Calendar { return _date }
    fun setDate(date: Calendar) { _date = date }
}