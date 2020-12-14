package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import com.google.firebase.database.DatabaseReference
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.Event

interface Scope {
    companion object scope {
        fun randomString() : String {
            val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            return (1..40)
                .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("");
        }
    }

    val id: String
    var name: String
    val db: DatabaseReference
    val events: MutableMap<String, Event>

    fun addEvent(): Event
    fun removeEvent(event: Event): Event?
}