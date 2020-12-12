package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.terms

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Controller

class TermFormActivity : AppCompatActivity() {

    private lateinit var controller: Controller

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_term_form)

        val userId = intent.getStringExtra("userId")!!
        controller = Controller(userId)

        // go back to terms fragment when done button is clicked
        val doneBtn: Button = this.findViewById(R.id.done)
        doneBtn.setOnClickListener {
            addTerm()
            finish()
        }

        val cancelBtn: Button = this.findViewById(R.id.cancel)
        cancelBtn.setOnClickListener {
            finish()
        }
    }

    fun addTerm() {
        val term = controller.addTerm()
        val title: EditText = this.findViewById(R.id.term_title)
        val startDate: EditText = this.findViewById(R.id.term_start)
        val endDate: EditText = this.findViewById(R.id.term_end)
        var titleValue = title.text
        var start = startDate.text
        var end = endDate.text
        Log.d("date", start.toString())
        Log.d("date", end.toString())

        term.name = titleValue.toString()
    }
}