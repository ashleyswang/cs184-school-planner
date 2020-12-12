package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.terms

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R

class TermFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_term_form)

        Log.d("hello", "in form activity")

        val intent = intent
        var finished: Boolean = false

        val editTitle: EditText = this.findViewById(R.id.term_title)
        val termTitle: String = editTitle.text.toString()

        // go back to terms fragment when done button is clicked
        val done_btn: Button = this.findViewById(R.id.done)
        done_btn.setOnClickListener {
//            finished = true
//            intent.putExtra("finished", finished)
//            intent.putExtra("title", termTitle)
            setResult(Activity.RESULT_OK, intent);
            finish()
        }
    }
}