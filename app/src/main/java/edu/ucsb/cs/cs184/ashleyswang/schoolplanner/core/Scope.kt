package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

interface Scope {
    companion object scope {
        fun randomString() : String {
            return ""
        }
    }

    val id: String
    var name: String
}