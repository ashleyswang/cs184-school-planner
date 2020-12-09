package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import android.util.Log
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase


class Controller {
    val TAG: String = "Controller"
    val user: String
    private var _terms: MutableMap<String, Term> = mutableMapOf<String, Term>();

    val db: DatabaseReference

    constructor(user: String, database: FirebaseDatabase) {
        this.user = user
        this.db = database.getReference("core").child(user)
//        _addDbListener()
    }

    fun getTerms(): MutableMap<String, Term> { return _terms }

    fun getTerm(id: String): Term? {
        return _terms.get(id)
    }

    fun addTerm(): Term {
        val term: Term = Term(this)
        _terms.put(term.id, term)
        return term
    }

    fun removeTerm(term: Term): Term? {
        db.child("terms").child(term.id).removeValue()
        return _terms.remove(term.id)
    }

    private fun _addDbListener() {
        db.child("terms").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<Map<String, Map<String, Any>>>()
                if (value!!.keys.minus(_terms.keys).size != 0 ||
                    _terms.keys.minus(value!!.keys).size != 0) {
                    val add: Set<String> = value.keys.minus(_terms.keys)
                    val rem: Set<String> = _terms.keys.minus(value.keys)

                    for (key in add) {
                        val term = value[key]?.let { Term(this@Controller, key, it) }
                        if (term != null) _terms.put(key, term)
                    }

                    for (key in rem)
                        _terms.remove(key)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read terms.", error.toException())
            }
        })
    }
}