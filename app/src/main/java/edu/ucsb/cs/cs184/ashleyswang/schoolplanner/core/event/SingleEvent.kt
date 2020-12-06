package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event

import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Scope
import java.time.*

interface SingleEvent : Event {
    val notifId: Int

    fun getDate() : LocalDateTime          // Start date
    fun setDate(date: LocalDateTime)
    fun getDuration() : Duration            // Length of event

}