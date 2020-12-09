package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.database.DatabaseReference
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
class DailyEvent : RecurringEvent {
    override var start: LocalDateTime
        get() { return start }
        set(value: LocalDateTime) {
            start = value
            db.child("start").setValue(start)
        }
    override var end: LocalDateTime?
        get() { return end }
        set(value: LocalDateTime?) {
            end = value
            db.child("end").setValue(end)
        }
    override var event: Event
    private var _canceled: MutableSet<LocalDateTime> = mutableSetOf<LocalDateTime>()

    val db: DatabaseReference

    constructor(event: Event) {
        this.event = event
        this.db = event.db.child("recur")
        this.start = event.start
        this.end = null
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
        val eventDate = event.start
        val removedDate: LocalDateTime = date.withHour(eventDate.hour)
            .withMinute(eventDate.minute)
            .withSecond(eventDate.second)
        _canceled.add(removedDate)
        db.child("canceled").child(_canceled.size.toString()).setValue(removedDate)
    }

    private fun getDateAfter(date: LocalDateTime): LocalDateTime? {
        if (end != null && date.isAfter(end))
            return null
        if (date.isBefore(start))
            return start
        val eventDate = event.start
        var newDate = date.withHour(eventDate.hour)
            .withMinute(eventDate.minute)
            .withSecond(eventDate.second)

        while (!newDate.isAfter(date))
            newDate = newDate.plusDays(1.toLong())

        if (end != null && newDate.isAfter(end)) return null
        else return newDate
    }
}