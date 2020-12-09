package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.database.DatabaseReference
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Scope
import java.time.*
import kotlin.math.absoluteValue
import kotlin.random.Random

@RequiresApi(Build.VERSION_CODES.O)
class Event {
    val id: String = Scope.randomString()
    var name: String
        get() { return name }
        set(value: String) {
            name = value
            db.child("name").setValue(name)
        }
    val scope: Scope
    val notifId: Int = Random.nextInt().absoluteValue

    var start: LocalDateTime
        get() { return start }
        set(value: LocalDateTime) {
            start = value
            db.child("start").setValue(start)
        }
    var end: LocalDateTime?
        get() { return end }
        set(value: LocalDateTime?) {
            end = value
            db.child("end").setValue(end)
        }

    val db: DatabaseReference
    var recur: RecurringEvent?
        get() { return recur }
        set(value: RecurringEvent?) {
            recur = value
        }

    /*
     * Constructor:
     * @params: scope: Scope - scope tied to deadline
     * @return:
     * SingleEvent object with given properties. Courses/Events are both empty.
     */
    constructor(scope: Scope) {
        this.scope = scope
        this.db = scope.db.child("events").child(id)
        this.name = "New Event"
        this.start = LocalDateTime.now()
        this.end = null
        this.recur = null
    }

    fun getDuration(): Duration {
        if (end == null)
            return Duration.between(start, start)
        else
            return Duration.between(start, end)
    }
}