package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.DeadlineEvent

class Assignment {
    val id: String
    var name: String = "New Assignment"
    val course: Course
    var event: DeadlineEvent
    var options: Options? = null

    constructor(course: Course, id: String) {
        this.id = id
        this.course = course
        this.event = DeadlineEvent(course, id)
    }

    inner class Options {
        var weight: Float? = null
        var grade: Float? = null
        var complete: Boolean? = null
    }
}