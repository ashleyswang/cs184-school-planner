package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.recurevent

import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Scope
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.SingleEvent
import java.time.DayOfWeek
import java.util.*
import java.util.Calendar.DAY_OF_WEEK
import kotlin.collections.ArrayList

open class WeeklyEvent<T: SingleEvent> : RecurringEvent<T> {
    override val id: String
    override var name: String
    override val scope: Scope

    override var start: Calendar = Calendar.getInstance()
    override var end: Calendar? = null
    override var event: T
    override var canceled: MutableSet<Calendar> = mutableSetOf<Calendar>()

    protected var _days: MutableSet<DayOfWeek> = mutableSetOf<DayOfWeek>()

    constructor(event: T) {
        this.id = event.id
        this.scope = event.scope
        this.name = event.name
        this.event = event
    }

    /* Returns next event based on today's date */
    override fun getNextEvent() {
        val current: Calendar = Calendar.getInstance()
        getEventAfter(current)
    }

    /* Returns all events from "from" to "to" */
    override fun getEvents(from: Calendar, to: Calendar) {
        var events: ArrayList<T> = ArrayList<T>()
        // TODO: Implement
    }

    /* Removes single event from recurrence */
    override fun removeEvent(date: Calendar) {
        // TODO: Implement
    }

    private fun getEventAfter(date: Calendar): T {
        return event
    }

    /* Add Day of the Week to recurrence */
    fun addDays(vararg days : DayOfWeek) {
        for (day in days)
            _days.add(day)
    }

    /* Remove Day of the Week to recurrence */
    fun removeDays(vararg days : DayOfWeek) {
        for (day in days)
            _days.remove(day)
    }
}