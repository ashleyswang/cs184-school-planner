package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import android.os.Build
import androidx.annotation.RequiresApi
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.DurationEvent
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.recurevent.RecurringEvent

@RequiresApi(Build.VERSION_CODES.O)
class Meeting {
    val id: String
    var name: String = "New Meeting"
    val course: Course
    var event: DurationEvent
    var recur: RecurringEvent? = null
    var options: Options? = null

    constructor(course: Course, id: String) {
        this.id = id
        this.course = course
        this.event = DurationEvent(course, id)
    }

    inner class Options {
        var mandatory: Boolean? = null
        var link: String? = null
    }
}