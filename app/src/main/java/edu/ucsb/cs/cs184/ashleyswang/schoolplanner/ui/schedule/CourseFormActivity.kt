package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.schedule

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Controller
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Term

class CourseFormActivity : AppCompatActivity() {

    private lateinit var controller: Controller
    private lateinit var term: Term

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_form)

        val userId = intent.getStringExtra("userId")!!
        val termId = intent.getStringExtra("termId")!!
        controller = Controller(userId)
        term = controller.terms[termId]!!

        // go back to terms fragment when done button is clicked
        val doneBtn: Button = this.findViewById(R.id.done)
        doneBtn.setOnClickListener {
            addCourse()
            finish()
        }

        val cancelBtn: Button = this.findViewById(R.id.cancel)
        cancelBtn.setOnClickListener {
            finish()
        }
    }

    fun addCourse() {
        val course = term.addCourse()
        val title: EditText = this.findViewById(R.id.course_title)
        var titleValue = title.text
        course.name = titleValue.toString()
    }
}