package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.database.DatabaseReference
import java.time.*
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.O)
open class WeeklyEvent : RecurringEvent {
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

    protected var _days: MutableSet<DayOfWeek> = mutableSetOf<DayOfWeek>()

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
        var i: Int = 0
        while (true) {
            var newDate: LocalDateTime = date.plusDays(i.toLong())
            if (_days.contains(newDate.dayOfWeek)) {
                newDate = newDate.withHour(eventDate.hour)
                    .withMinute(eventDate.minute)
                    .withSecond(eventDate.second)
                if (newDate.isAfter(date) && _canceled.count{e -> newDate.isEqual(e)} == 0)
                    return newDate
            }
        }
    }

    /* Add Day of the Week to recurrence */
    fun addDays(vararg days : DayOfWeek) {
        for (day in days) {
            _days.add(day)
            when (day) {
                DayOfWeek.MONDAY -> db.child("days").child("MONDAY").setValue(true)
                DayOfWeek.TUESDAY -> db.child("days").child("TUESDAY").setValue(true)
                DayOfWeek.WEDNESDAY -> db.child("days").child("WEDNESDAY").setValue(true)
                DayOfWeek.THURSDAY -> db.child("days").child("THURSDAY").setValue(true)
                DayOfWeek.FRIDAY -> db.child("days").child("FRIDAY").setValue(true)
                DayOfWeek.SATURDAY -> db.child("days").child("SATURDAY").setValue(true)
                DayOfWeek.SUNDAY -> db.child("days").child("SUNDAY").setValue(true)
            }
        }
    }

    /* Remove Day of the Week to recurrence */
    fun removeDays(vararg days : DayOfWeek) {
        for (day in days) {
            _days.remove(day)
            when (day) {
                DayOfWeek.MONDAY -> db.child("days").child("MONDAY").removeValue()
                DayOfWeek.TUESDAY -> db.child("days").child("TUESDAY").removeValue()
                DayOfWeek.WEDNESDAY -> db.child("days").child("WEDNESDAY").removeValue()
                DayOfWeek.THURSDAY -> db.child("days").child("THURSDAY").removeValue()
                DayOfWeek.FRIDAY -> db.child("days").child("FRIDAY").removeValue()
                DayOfWeek.SATURDAY -> db.child("days").child("SATURDAY").removeValue()
                DayOfWeek.SUNDAY -> db.child("days").child("SUNDAY").removeValue()
            }
        }
    }
}