package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.forms

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Controller
import java.lang.Exception
import java.time.LocalDateTime


class TermFormActivity : AppCompatActivity() {

    val TAG: String = "TermFormActivity"
    val ACTION_ADD: Int = 0
    val ACTION_EDIT: Int = 1
    val ACTION_DEL: Int = 2

    private var editExisting: Boolean = false
    private var startDate: LocalDateTime = LocalDateTime.now()
    private var endDate: LocalDateTime? = null

    private lateinit var controller: Controller
    private lateinit var termId: String
    private lateinit var picker: DatePickerDialog

    private lateinit var termNameEditText: EditText
    private lateinit var termStartEditText: EditText
    private lateinit var termEndEditText: EditText
    private lateinit var currTermSwitch: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_term)

        val userId = intent.getStringExtra("userId")!!
        controller = Controller(userId)
        editExisting = (intent.getStringExtra("termId") != null)

        getFormViews()
        setTheme(R.style.TermsDatePicker)
        setInitialValues()
        setDateEditListeners()
        setFinishButtonListeners()
    }

    private fun setFinishButtonListeners() {
        val submitBtn: ImageButton = this.findViewById(R.id.term_form_submit)
        submitBtn.setOnClickListener {
            if (updateTerm()) {
                val resultIntent: Intent = Intent()
                val action: Int = if (editExisting) ACTION_EDIT else ACTION_ADD
                resultIntent.putExtra("action", action)
                resultIntent.putExtra("termId", termId)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
            else {
                val toast = Toast.makeText(this, "Please fill in all available fields.", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 10)
                toast.show()
            }
        }

        val cancelBtn: ImageButton = this.findViewById(R.id.term_form_cancel)
        cancelBtn.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun setDateEditListeners() {
        termStartEditText.inputType = InputType.TYPE_NULL
        termStartEditText.setOnClickListener {
            val month = startDate.monthValue
            val day = startDate.dayOfMonth
            val year = startDate.year

            picker = DatePickerDialog(this,
                OnDateSetListener { view, year, month, day ->
                    val dateString = getDateDisplayString(year, month+1, day)
                    termStartEditText.setText(dateString)
                    startDate = LocalDateTime.of(year, month+1, day, 0, 0)
                }, year, month-1, day)
            picker.show()
        }

        termEndEditText.inputType = InputType.TYPE_NULL
        termEndEditText.setOnClickListener {
            val month = endDate?.monthValue ?: startDate.monthValue
            val day = endDate?.dayOfMonth ?: startDate.dayOfMonth
            val year = endDate?.year ?: startDate.year

            picker = DatePickerDialog(this,
                OnDateSetListener { view, year, month, day ->
                    val dateString = getDateDisplayString(year, month+1, day)
                    termEndEditText.setText(dateString)
                    endDate = LocalDateTime.of(year, month+1, day, 23, 59)
                }, year, month-1, day)
            picker.show()
        }
    }

    private fun setInitialValues() {
        termId = if (editExisting) intent.getStringExtra("termId")!! else ""
        if (editExisting) {
            val termName = intent.getStringExtra("termName")!!
            startDate = LocalDateTime.parse(intent.getStringExtra("termStart")!!)
            endDate = LocalDateTime.parse(intent.getStringExtra("termEnd")!!)
            val default = intent.getBooleanExtra("default", false)

            // set current term values into editor
            termNameEditText.setText(termName)

            val startString =
                getDateDisplayString(startDate.year, startDate.monthValue, startDate.dayOfMonth)
            termStartEditText.setText(startString)

            val endString =
                getDateDisplayString(endDate!!.year, endDate!!.monthValue, endDate!!.dayOfMonth)
            termEndEditText.setText(endString)

            currTermSwitch.isChecked = default

            makeDeleteButton()
        }
    }

    private fun makeDeleteButton() {
        val deleteBtn: Button = this.findViewById(R.id.term_form_delete)
        deleteBtn.visibility = View.VISIBLE

        deleteBtn.setOnClickListener{
            val term = controller.terms[termId]!!
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete Term")
                .setMessage("Are you sure you want to permanently delete \"${term.name}\"?")
            builder.apply {
                setPositiveButton("DELETE") { dialog, id ->
                    controller.removeTerm(term)
                    dialog.dismiss()
                    val resultIntent: Intent = Intent()
                    resultIntent.putExtra("action", ACTION_DEL)
                    resultIntent.putExtra("termId", termId)
                    setResult(Activity.RESULT_OK, resultIntent)
                    this@TermFormActivity.finish()
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

    private fun updateTerm(): Boolean {
        val name = termNameEditText.text.toString()
        val startInput = termStartEditText.text.toString()
        val endInput = termEndEditText.text.toString()
        val currTermInput = currTermSwitch.isChecked

        try {
            if (name == "") throw Exception()

            val startVals: ArrayList<Int> = arrayListOf<Int>()
            for (value in startInput.split('/'))
                startVals.add(value.toInt())
            val start =
                LocalDateTime.of(startVals[2], startVals[0], startVals[1], 0, 0)

            val endVals: ArrayList<Int> = arrayListOf<Int>()
            for (value in endInput.split('/'))
                endVals.add(value.toInt())
            val end =
                LocalDateTime.of(endVals[2], endVals[0], endVals[1], 23, 59)

            val term = if (editExisting) controller.terms[termId]!!
                else controller.addTerm()
            termId = term.id

            if (currTermInput) controller.default = term.id
            term.start = start
            term.name = name
            term.end = end

            return true
        } catch (e: Exception) {
            return false
        }
    }

    private fun getDateDisplayString(year: Int, month: Int, day: Int): String {
        val monthString =
            if (month < 10) "0${month}"
            else month.toString()
        val dayString =
            if (day < 10) "0${day}"
            else day.toString()
        return "$monthString/$dayString/$year"
    }

    private fun getFormViews() {
        termNameEditText = this.findViewById(R.id.term_name)
        termStartEditText = this.findViewById(R.id.term_start)
        termEndEditText = this.findViewById(R.id.term_end)
        currTermSwitch = this.findViewById(R.id.term_form_curr_switch)
    }
}