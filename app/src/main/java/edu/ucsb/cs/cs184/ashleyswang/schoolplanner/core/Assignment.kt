package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

class Assignment {
    val TAG: String = "Assignment"
    val id: String
    val course: Course
    val db: DatabaseReference
    val eventId: String
    val event: Event
        get() { return course.events[this.eventId]!! }

    var name: String
        get() { return event.name }
        set(value: String) {
            event.name = value
        }

    var options: Options = Options()

    constructor(course: Course, eventId: String) {
        this.id = Scope.randomString()
        this.course = course
        this.eventId = eventId
        this.db = course.db.child("assign").child(id)
        this.name = "New Assignment"
        this.db.child("eventId").setValue(eventId)
        _addDbListener()
    }

    constructor(course: Course, key: String, value: Map<String, Any>) {
        this.id = key
        this.course = course
        this.db = course.db.child("assign").child(id)
        this.eventId = value["eventId"] as String

        if (value["options"] != null) {
            val opts = value["options"] as Map<String, Any>
            this.options = Options()

            val weight = opts["weight"] as Double?
            if (weight != null) this.options._weight = weight.toFloat()

            val grade = opts["grade"] as Double?
            if (grade != null) this.options._grade = grade.toFloat()

            val complete = opts["complete"] as Boolean?
            if (complete != null) this.options._complete = complete
        }
        _addDbListener()
    }

    private fun _addDbListener() {
        db.child("options").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<Map<String, Any>>()
                if (value != null) {
                    options._weight = (value["weight"] as Double?)?.toFloat()
                    options._grade = (value["grade"] as Double?)?.toFloat()
                    options._complete = value["complete"] as Boolean?
                } else options = Options()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read name.", error.toException())
            }
        })
    }

    inner class Options {
        var weight: Float?
            get() { return _weight }
            set(value: Float?) {
                _weight = value
                db.child("options").child("weight").setValue(_weight)
            }
        var grade: Float?
            get() { return _grade }
            set(value: Float?) {
                _grade = value
                db.child("options").child("grade").setValue(_grade)
            }
        var complete: Boolean?
            get() { return _complete }
            set(value: Boolean?) {
                _complete = value
                db.child("options").child("complete").setValue(_complete)
            }

        internal var _weight: Float? = null
        internal var _grade: Float? = null
        internal var _complete: Boolean? = null
    }
}