package edu.ucsb.cs.cs184.ashleyswang.schoolplanner

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.test.FirebaseCoreTester

class MainActivity : AppCompatActivity() {
    var counter = 0
    var tester = FirebaseCoreTester()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            tester.runTest(counter)
            Log.d("MainActivity", "Test $counter PASSED")
            counter++
        }
        return super.onTouchEvent(event)
    }

}