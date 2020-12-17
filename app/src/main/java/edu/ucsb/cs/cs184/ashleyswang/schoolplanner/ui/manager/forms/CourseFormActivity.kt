package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.forms

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Controller
import kotlin.Exception

class CourseFormActivity : AppCompatActivity() {

    val TAG: String = "CourseFormActivity"
    val ACTION_ADD: Int = 0
    val ACTION_EDIT: Int = 1
    val ACTION_DEL: Int = 2

    private var editExisting: Boolean = false

    private lateinit var controller: Controller
    private lateinit var termId: String
    private lateinit var courseId: String

    private lateinit var courseNameEditText: EditText

    private var initCourseName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_course)

        val userId = intent.getStringExtra("userId")!!
        controller = Controller(userId)
        termId = intent.getStringExtra("termId")!!
        editExisting = (intent.getStringExtra("courseId") != null)

        courseNameEditText = this.findViewById(R.id.course_name)
        setTheme(R.style.TermsDatePicker)
        setInitialValues()
        setFinishButtonListeners()
    }

    private fun setFinishButtonListeners() {
        val submitBtn: ImageButton = this.findViewById(R.id.course_form_submit)
        submitBtn.setOnClickListener {
            if (updateCourse()) {
                val resultIntent: Intent = Intent()
                val action: Int = if (editExisting) ACTION_EDIT else ACTION_ADD
                resultIntent.putExtra("action", action)
                resultIntent.putExtra("courseId", courseId)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
            else {
                val toast = Toast.makeText(this, "Please fill in all available fields.", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 10)
                toast.show()
            }
        }

        val cancelBtn: ImageButton = this.findViewById(R.id.course_form_cancel)
        cancelBtn.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }


    private fun setInitialValues() {
        courseId = if (editExisting) intent.getStringExtra("courseId")!! else ""
        if (editExisting) {
            initCourseName = intent.getStringExtra("courseName")!!
            courseNameEditText.setText(initCourseName)
            makeDeleteButton()
        }
    }

    private fun makeDeleteButton() {
        val deleteBtn: Button = this.findViewById(R.id.course_form_delete)
        deleteBtn.visibility = View.VISIBLE

        deleteBtn.setOnClickListener{
            val term = controller.terms[termId]!!
            val course = term.courses[courseId]!!
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete Course")
                .setMessage("Are you sure you want to permanently delete \"${course.name}\"?")
            builder.apply {
                setPositiveButton("DELETE") { dialog, id ->
                    term.removeCourse(course)
                    dialog.dismiss()
                    val resultIntent: Intent = Intent()
                    resultIntent.putExtra("action", ACTION_DEL)
                    resultIntent.putExtra("courseId", courseId)
                    setResult(Activity.RESULT_OK, resultIntent)
                    this@CourseFormActivity.finish()
                }

                setNegativeButton("Cancel") { dialog, id ->
                    dialog.dismiss()
                }
            }
            val dialog = builder.create()
            dialog.show()
            val confirmBtn: Button = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            confirmBtn.setTextColor(resources.getColor(R.color.darkGrey))
            confirmBtn.textSize = 18F
            val cancelBtn: Button = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            cancelBtn.textSize = 18F
            val dialogMsg: TextView = dialog.findViewById(android.R.id.message)
            dialogMsg.textSize = 16F
        }
    }

    private fun updateCourse(): Boolean {
        val term = controller.terms[termId]!!
        val nameInput = courseNameEditText.text.toString()

        try {
            val course = term.courses[courseId] ?: term.addCourse()
            courseId = course.id

            if (nameInput == "") throw Exception()
            if (nameInput != initCourseName) course.name = nameInput
            return true
        } catch (e: Exception) {
            return false
        }
    }
}