package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import android.util.Log
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase


class Controller {
    val TAG: String = "Controller"
    val user: String
    val db: DatabaseReference
    var currId: String?
        get() { return _currId }
        set(value) {
            _currId = value
            db.child("currId").setValue(_currId)
        }
    val terms: MutableMap<String, Term>
        get() { return _terms }

    private var _currId: String? = null
    private var _terms: MutableMap<String, Term> = mutableMapOf<String, Term>();

    constructor(user: String) {
        this.user = user
        this.db = Firebase.database.getReference("core").child(user)
        _addDbListener()
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
        db.child("currId").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String?>()
                if (value != _currId) _currId = value
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read terms.", error.toException())
            }
        })

        db.child("terms").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<Map<String, Map<String, Any>>>()
                if (value == null)
                    _terms.clear()
                else if (value.keys.minus(_terms.keys).isNotEmpty() ||
                    _terms.keys.minus(value.keys).isNotEmpty()
                ) {
                    val add: Set<String> = value.keys.minus(_terms.keys)
                    for (key in add) {
                        Log.d(TAG, "add term ${key}")
                        val term = Term(this@Controller, key, value[key]!!)
                        _terms.put(key, term)
                    }

                    val rem: Set<String> = _terms.keys.minus(value.keys)
                    for (key in rem) _terms.remove(key)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read terms.", error.toException())
            }
        })
    }
}