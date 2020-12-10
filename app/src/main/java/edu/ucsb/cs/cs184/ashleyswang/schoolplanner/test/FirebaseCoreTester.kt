package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.test

import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.*
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.Event
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.RecurringEvent
import java.time.LocalDateTime

class FirebaseCoreTester {
    val control = Controller("test")

    lateinit var term: Term
    lateinit var course: Course
    lateinit var event: Event
    lateinit var assign: Assignment
    lateinit var meet: Meeting
    lateinit var recur: RecurringEvent

    fun runTest(counter: Int) {
        when (counter) {
            0 -> {
                term = control.addTerm()
                term.name = "Fall 2020"
                term.start = LocalDateTime.of(2020, 10, 1, 0, 0, 0)
                term.end = LocalDateTime.of(2020, 12, 17, 0, 0, 0)
                course = term.addCourse()
                event = term.addEvent()
            }
            1 -> {
                term.removeCourse(course)
                term.removeEvent(event)
            }
            2 -> {
                course = term.addCourse()
                course.name = "CS184"
                assign = course.addAssign()
                meet = course.addMeet()
                event = course.addEvent()
            }
            3 -> {
                course.removeAssign(assign)
                course.removeMeet(meet)
                course.removeEvent(event)
            }
            4 -> {
                assign = course.addAssign()
                assign.name = "Final Project"
                event = assign.event

                assign.options.weight = 0.4F
                assign.options.complete = false
            }
            5 -> {
                assign.options.weight = null
                assign.options.complete = null
            }
            6 -> {
                event.name = "Final Project Presentation"
                event.start = LocalDateTime.of(2020, 12, 17, 15, 0)
                event.end = LocalDateTime.of(2020, 12, 17, 16, 0)
            }
            7 -> {
                meet = course.addMeet()
                event = meet.event
                meet.name = "Section"
                meet.options.mandatory = true
            }
            8 -> {
                // Manually Add Event to Course
                Log.d("CoreTester", "Added Event to Course | Course.events.size = ${course.events.size}")
                // Manually Remove Options from Meeting
                Log.d("CoreTester", "Remove Options from Meeting | Meeting.options = ${meet.options.mandatory.toString()}")
            }
            9 -> {
                course.removeMeet(meet)
                course.removeAssign(assign)
            }
            10 -> {
                control.removeTerm(term)
            }
        }
    }

//    fun runTest(counter: Int) {
//        when (counter) {
//            0 -> {
//                term = control.addTerm()
//                term.name = "Fall 2020"
//                term.start = LocalDateTime.of(2020, 10, 1, 0, 0, 0)
//                term.end = LocalDateTime.of(2020, 12, 17, 0, 0, 0)
//                event = term.addEvent()
//            }
//            1 -> {
//                event = term.events[event.id]!!
//                Log.d("CoreTester", "Event Name: ${event.name}")
//                Log.d("CoreTester", "Event Start: ${event.start.toString()}")
//                Log.d("CoreTester", "Event End: ${event.end.toString()}")
//            }
//        }
//    }
}