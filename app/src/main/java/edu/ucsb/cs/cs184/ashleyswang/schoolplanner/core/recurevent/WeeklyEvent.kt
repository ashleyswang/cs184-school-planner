package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.recurevent

import android.os.Build
import androidx.annotation.RequiresApi
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Scope
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.SingleEvent
import java.time.*
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.O)
open class WeeklyEvent : RecurringEvent {
    override val id: String
    override var name: String
    override val scope: Scope

    override var start: LocalDateTime = LocalDateTime.now()
    override var end: LocalDateTime? = null
    override var event: SingleEvent
    override var canceled: MutableSet<LocalDateTime> = mutableSetOf<LocalDateTime>()

    protected var _days: MutableSet<DayOfWeek> = mutableSetOf<DayOfWeek>()

    constructor(event: SingleEvent) {
        this.id = event.id
        this.scope = event.scope
        this.name = event.name
        this.event = event
    }

    /* Returns next event based on today's date */
    override fun getNextDate(): LocalDateTime? {
        val current: LocalDateTime = LocalDateTime.now()
        return getDateAfter(current)
    }

    /* Returns all events from "from" to "to" */
    override fun getDates(from: LocalDateTime, to: LocalDateTime): ArrayList<LocalDateTime> {
        var events: ArrayList<LocalDateTime> = ArrayList<LocalDateTime>()
        var curr: LocalDateTime? = getDateAfter(from)
        while (curr != null && curr.isAfter(to)) {
            events.add(curr)
            curr = getDateAfter(curr)
        }
        return events
    }

    /* Removes single event from recurrence with matching date */
    override fun removeDate(date: LocalDateTime) {
        val eventDate = event.getDate()
        val removedDate: LocalDateTime = date.withHour(eventDate.hour)
            .withMinute(eventDate.minute)
            .withSecond(eventDate.second)
        canceled.add(removedDate)
    }

    private fun getDateAfter(date: LocalDateTime): LocalDateTime? {
        if (end != null && date.isAfter(end))
            return null
        if (date.isBefore(start))
            return start
        val eventDate = event.getDate()
        var i: Int = 0
        while (true) {
            var newDate: LocalDateTime = date.plusDays(i.toLong())
            if (_days.contains(newDate.dayOfWeek)) {
                newDate = newDate.withHour(eventDate.hour)
                    .withMinute(eventDate.minute)
                    .withSecond(eventDate.second)
                if (newDate.isAfter(date) && canceled.count{e -> newDate.isEqual(e)} == 0)
                    return newDate
            }
        }
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