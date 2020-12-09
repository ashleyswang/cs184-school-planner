package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.database.DatabaseReference
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.SingleEvent

@RequiresApi(Build.VERSION_CODES.O)
class Meeting {
    val id: String = Scope.randomString()
    var name: String
        get() { return name }
        set(value: String) {
            name = value
            this.db.child("name").setValue(name)
        }
    val course: Course
    var event: SingleEvent
    var options: Options? = null

    val db: DatabaseReference

    constructor(course: Course) {
        this.course = course
        this.event = SingleEvent(course)
        this.db = course.db.child("meet").child(id)
        this.name = "New Meeting"
        this.db.child("eventId").setValue(this.event.id)
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