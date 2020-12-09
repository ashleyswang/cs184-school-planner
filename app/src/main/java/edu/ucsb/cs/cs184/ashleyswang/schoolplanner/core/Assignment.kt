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
    var name: String
        get() { return name }
        set(value: String) {
            name = value
            this.db.child("name").setValue(name)
        }
    val course: Course
    val eventId: String
    var options: Options? = null

    val db: DatabaseReference

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

        _addDbListener()
    }

    private fun _addDbListener() {
        db.child("name").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String>()
                if (value != null && value != name) name = value
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read name.", error.toException())
            }
        })

        db.child("options").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<Map<String, Any>>()
                if (value != null) {
                    options = Options()
                    val weight = value["weight"]
                    if (weight != null) options!!.weight = weight as Float

                    val grade = value["grade"]
                    if (grade != null) options!!.grade = grade as Float

                    val complete = value["complete"]
                    if (complete != null) options!!.complete = complete as Boolean
                } else options = null
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read name.", error.toException())
            }
        })
    }

    inner class Options {
        var weight: Float?
            get() { return weight }
            set(value: Float?) {
                weight = value
                db.child("options").child("weight").setValue(weight)
            }
        var grade: Float?
            get() { return grade }
            set(value: Float?) {
                grade = value
                db.child("options").child("grade").setValue(grade)
            }
        var complete: Boolean?
            get() { return complete }
            set(value: Boolean?) {
                complete = value
                db.child("options").child("complete").setValue(complete)
            }
    }
}