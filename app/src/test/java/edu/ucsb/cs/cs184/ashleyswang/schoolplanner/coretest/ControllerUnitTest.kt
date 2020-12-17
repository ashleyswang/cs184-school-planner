package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.coretest

import com.google.firebase.database.FirebaseDatabase
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Controller
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ControllerUnitTest {
    val database: FirebaseDatabase
    val control: Controller

    constructor() {
        // Actual DB for App
        // val database: FirebaseDatabase = Firebase.database

        // Local DB for Unit Testing
        database = FirebaseDatabase.getInstance()
        database.useEmulator("localhost", 9000)
        control = Controller("test")
    }


    @Test
    fun addTerm() {
        val term1 = control.addTerm()
        val term2 = control.addTerm()
        val term3 = control.addTerm()

        val terms = control.terms
        assertEquals(3, terms.size)
    }
}