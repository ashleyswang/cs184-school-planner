package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event

import android.os.Build
import androidx.annotation.RequiresApi
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Scope
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import kotlin.math.absoluteValue
import kotlin.random.Random

@RequiresApi(Build.VERSION_CODES.O)
open class DeadlineEvent : SingleEvent {
    override val id: String
    override var name: String = "New Deadline"
    override val scope: Scope
    override val notifId: Int = Random.nextInt().absoluteValue

    private var _date: LocalDateTime = LocalDateTime.now()

    /*
     * Constructor:
     * @params: scope: Scope - scope tied to deadline
     *          id: String   - key id used for mapping event in scope
     * @return:
     * DeadlineEvent object with given properties. Courses/Events are both empty.
     */
    constructor(scope: Scope, id: String) {
        this.id = id
        this.scope = scope
    }

    /* Copy Constructor */
    constructor(event: DeadlineEvent) {
        this.id = Scope.randomString()
        this.scope = event.scope
        this.name = event.name
        this._date = event.getDate()
    }

    /* Getters and Setters */
    override fun getDate(): LocalDateTime { return _date }
    override fun setDate(date: LocalDateTime) { _date = date }
    override fun getDuration(): Duration {
        return Duration.between(_date, _date)
    }
}