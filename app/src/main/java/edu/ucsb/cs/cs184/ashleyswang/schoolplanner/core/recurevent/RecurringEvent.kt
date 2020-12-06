package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.recurevent

import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.Event
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.SingleEvent
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

interface RecurringEvent : Event {
    // Add more types of recurrences once we have more finalized behavior
    var start: LocalDateTime
    var end: LocalDateTime?
    var event: SingleEvent
    var canceled: MutableSet<LocalDateTime>

    /* Returns next event based on today's date */
    fun getNextDate(): LocalDateTime?

    /* Returns all events from "from" to "to" */
    fun getDates(from: LocalDateTime, to: LocalDateTime): ArrayList<LocalDateTime>

    /* Removes single event from recurrence */
    fun removeDate(date: LocalDateTime)
}