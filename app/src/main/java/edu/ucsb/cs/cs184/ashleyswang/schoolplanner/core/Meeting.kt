package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.Event

class Meeting {
    val TAG: String = "Meeting"
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
        this.db = course.db.child("meet").child(id)
        this.name = "New Meeting"
        this.db.child("eventId").setValue(eventId)
        _addDbListener()
    }

    constructor(course: Course, key: String, value: Map<String, Any>) {
        this.id = key
        this.course = course
        this.db = course.db.child("meet").child(id)
        this.eventId = value["eventId"] as String

        if (value["options"] != null) {
            val opts = value["options"] as Map<String, Any>
            this.options = Options()

            val mandatory = opts["mandatory"] as Boolean?
            if (mandatory != null) this.options._mandatory = mandatory

            val link = opts["link"] as String?
            if (link != null) this.options._link = link
        }
        _addDbListener()
    }

    private fun _addDbListener() {
        db.child("options").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<Map<String, Any>>()
                if (value != null) {
                    options.mandatory = value["mandatory"] as Boolean?
                    options.link = value["link"] as String?
                } else options = Options()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read options.", error.toException())
            }
        })
    }

    inner class Options {
        var mandatory: Boolean?
            get() { return _mandatory }
            set(value: Boolean?) {
                _mandatory = value
                db.child("options").child("mandatory").setValue(_mandatory)
            }
        var link: String?
            get() { return _link }
            set(value: String??) {
                _link = value
                db.child("options").child("link").setValue(_link)
            }

        internal var _mandatory: Boolean? = null
        internal var _link: String? = null
    }
}