package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event

import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.Event
import java.time.LocalDateTime
import kotlin.collections.ArrayList

interface RecurringEvent {
    // Add more types of recurrences once we have more finalized behavior
    val id: String
    var start: LocalDateTime
    var end: LocalDateTime
    val event: Event
    val type: String
    val canceled: MutableSet<LocalDateTime>

    /* Adds Event objects to scope events list */
    fun generateEvents()

    /* Removes Event objects associated with this recurId */
    fun removeEvents()

    /* Returns next event based on today's date */
    fun getNextDate(): LocalDateTime?

    /* Returns all events from "from" to "to" */
    fun getDates(from: LocalDateTime, to: LocalDateTime): ArrayList<LocalDateTime>

    /* Removes single event from recurrence */
    fun removeDate(date: LocalDateTime)
}