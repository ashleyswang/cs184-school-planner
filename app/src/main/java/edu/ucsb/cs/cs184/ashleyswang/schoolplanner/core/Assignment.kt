package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.database.DatabaseReference
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.SingleEvent

@RequiresApi(Build.VERSION_CODES.O)
class Assignment {
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
        this.db = course.db.child("assign").child(id)
        this.name = "New Assignment"
        this.db.child("eventId").setValue(this.event.id)
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