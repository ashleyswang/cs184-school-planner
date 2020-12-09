package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.test.core

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Controller

@RequiresApi(Build.VERSION_CODES.O)
class ControllerUnitTest {
    val TAG = "ControllerTest"
    val control = Controller("test")

    fun addTerm() {
        val term1 = control.addTerm()
        val term2 = control.addTerm()
        val term3 = control.addTerm()

        val terms = control.getTerms()

        val passed = (terms.size == 3)
        if (passed) Log.i(TAG, "addTerm() test: PASSED")
        else Log.i(TAG, "addTerm() test: FAILED | expected: 3, actual : ${terms.size}")
    }

}