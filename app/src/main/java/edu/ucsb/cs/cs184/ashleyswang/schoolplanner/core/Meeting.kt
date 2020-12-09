package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

class Meeting {
    val TAG: String = "Meeting"
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
        this.db = course.db.child("meet").child(id)
        this.name = "New Meeting"
        this.db.child("eventId").setValue(this.eventId)
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

            val mandatory = opts["mandatory"]
            if (mandatory != null) this.options!!.mandatory = mandatory as Boolean

            val link = opts["link"]
            if (link != null) this.options!!.link = link as String
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
                    val mandatory = value["mandatory"]
                    if (mandatory != null) options!!.mandatory = mandatory as Boolean

                    val link = value["link"]
                    if (link != null) options!!.link = link as String
                } else options = null
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read options.", error.toException())
            }
        })
    }

    inner class Options {
        var mandatory: Boolean?
            get() { return mandatory }
            set(value: Boolean?) {
                mandatory = value
                db.child("options").child("mandatory").setValue(mandatory)
            }
        var link: String?
            get() { return link }
            set(value: String??) {
                link = value
                db.child("options").child("link").setValue(link)
            }
    }
}