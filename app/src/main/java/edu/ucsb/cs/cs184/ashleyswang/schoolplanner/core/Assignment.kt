package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.Event

class Assignment {
    val TAG: String = "Assignment"
    val id: String
    var name: String
        get() { return event!!.name }
        set(value: String) {
            event!!.name = value
//            this.db.child("name").setValue(value)
        }
    val event: Event?
        get() { return course.getEvent(this.eventId) }
    val course: Course
    val eventId: String
    var options: Options = Options()

    val db: DatabaseReference

    constructor(course: Course, eventId: String) {
        this.id = Scope.randomString()
        this.course = course
        this.eventId = eventId
        this.db = course.db.child("assign").child(id)
        this.name = "New Assignment"
        this.db.child("eventId").setValue(eventId)
//        _addDbListener()
    }

    constructor(course: Course, key: String, value: Map<String, Any>) {
        this.id = key
        this.course = course
        this.db = course.db.child("assign").child(id)
        this.name = value["name"] as String
        this.eventId = value["eventId"] as String

        if (value["options"] != null) {
            val opts = value["options"] as Map<String, Any>
            this.options = Options()

            val weight = opts["weight"]
            if (weight != null) this.options!!.weight = weight as Float

            val grade = opts["grade"]
            if (grade != null) this.options!!.grade = grade as Float

            val complete = opts["complete"]
            if (complete != null) this.options!!.complete = complete as Boolean
        }

//        _addDbListener()
    }

    private fun _addDbListener() {
        db.child("options").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<Map<String, Any>>()
                if (value != null) {
                    options.weight = value["weight"] as Float?
                    options.grade = value["grade"] as Float?
                    options.complete = value["complete"] as Boolean?
                } else options = Options()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read name.", error.toException())
            }
        })
    }

    inner class Options {
        var weight: Float? = null
            get() { return weight }
            set(value: Float?) {
                field = value
                db.child("options").child("weight").setValue(field)
            }
        var grade: Float? = null
            get() { return grade }
            set(value: Float?) {
                field = value
                db.child("options").child("grade").setValue(field)
            }
        var complete: Boolean? = null
            get() { return complete }
            set(value: Boolean?) {
                field = value
                db.child("options").child("complete").setValue(field)
            }
    }
}