package edu.ucsb.cs.cs184.ashleyswang.schoolplanner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.test.core.ControllerUnitTest

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Start Test
        val controlTest = ControllerUnitTest()
        controlTest.addTerm()
    }
}