package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core

import android.os.Build
import androidx.annotation.RequiresApi
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.DeadlineEvent
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.recurevent.RecurringEvent

@RequiresApi(Build.VERSION_CODES.O)
class Controller {
    val user: String = "develop"
    var terms: MutableMap<String, Term> = mutableMapOf<String, Term>();

    fun addTerm(): Term? {
        val id: String = Scope.randomString()
        val term: Term = Term(this, id)
        return _terms.put(id, term)
    }

    fun removeTerm(id: String): Term? {
        return _terms.remove(id)
    }
}