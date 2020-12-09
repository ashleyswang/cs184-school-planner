package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@RequiresApi(Build.VERSION_CODES.O)
class Controller {
    val user: String = "develop"
    var terms: MutableMap<String, Term> = mutableMapOf<String, Term>();

    val database: FirebaseDatabase = Firebase.database
    val db: DatabaseReference = database.getReference("core").child(user)

    fun addTerm(): Term? {
        val term: Term = Term(this)
        return terms.put(term.id, term)
    }

    fun removeTerm(id: String): Term? {
        db.child("terms").child(id).removeValue()
        return terms.remove(id)
    }
}