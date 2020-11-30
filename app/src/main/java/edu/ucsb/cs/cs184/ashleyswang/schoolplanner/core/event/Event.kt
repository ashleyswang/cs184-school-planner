package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event

import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Scope

interface Event {
    val id: String
    var name: String
    val scope: Scope
}