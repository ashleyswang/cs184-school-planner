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
open class DurationEvent : SingleEvent {
    override val id: String
    override var name: String = "New Event"
    override val scope: Scope
    override val notifId: Int = Random.nextInt().absoluteValue

    private var _start: LocalDateTime = LocalDateTime.now()
    private var _end: LocalDateTime = LocalDateTime.now()

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
    constructor(event: DurationEvent) {
        this.id = Scope.randomString()
        this.scope = event.scope
        this.name = event.name
        this._start = event.getStart()
        this._end = event.getEnd()
    }

    /* Getters and Setters */
    fun getStart(): LocalDateTime { return _start }
    fun setStart(date: LocalDateTime) { _start = date }
    fun getEnd(): LocalDateTime { return _end }
    fun setEnd(date: LocalDateTime) { _end = date }

    override fun getDate(): LocalDateTime { return _start }
    override fun setDate(date: LocalDateTime) { _start = date }
    override fun getDuration(): Duration {
        return Duration.between(_start, _end)
    }
}