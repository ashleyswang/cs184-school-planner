package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.deadlines

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R

class DeadlineFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deadline_form)

        // go back to terms fragment when done button is clicked
        val doneBtn: Button = this.findViewById(R.id.done)
        doneBtn.setOnClickListener {
            finish()
        }

        val cancelBtn: Button = this.findViewById(R.id.cancel)
        cancelBtn.setOnClickListener {
            finish()
        }
    }
}