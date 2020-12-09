package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import com.google.firebase.database.DatabaseReference

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
}