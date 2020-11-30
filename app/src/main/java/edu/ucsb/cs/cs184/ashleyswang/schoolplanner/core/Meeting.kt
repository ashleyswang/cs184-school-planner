package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.DurationEvent

class Meeting {
    val id: String
    var name: String = "New Meeting"
    val course: Course
    var event: DurationEvent
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