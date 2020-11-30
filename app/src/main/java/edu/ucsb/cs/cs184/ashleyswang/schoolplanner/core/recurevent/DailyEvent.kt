package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.recurevent

import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.SingleEvent

class DailyEvent<T: SingleEvent>(event: T) : WeeklyEvent<T>(event) {
    // Finish daily recurring event
}