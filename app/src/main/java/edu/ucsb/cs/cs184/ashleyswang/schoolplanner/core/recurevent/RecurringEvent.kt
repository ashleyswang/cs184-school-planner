package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.recurevent

import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.Event
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.SingleEvent
import java.util.*

interface RecurringEvent<T: SingleEvent> : Event {
    // Add more types of recurrences once we have more finalized behavior
    var start: Calendar
    var end: Calendar?
    var event: T
    var canceled: MutableSet<Calendar>

    /* Returns next event based on today's date */
    fun getNextEvent()

    /* Returns all events from "from" to "to" */
    fun getEvents(from: Calendar, to: Calendar)

    /* Removes single event from recurrence */
    fun removeEvent(date: Calendar)
}