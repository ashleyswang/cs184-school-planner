package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.courses

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Controller
import java.lang.Exception
import java.time.LocalDateTime


class TermFormActivity : AppCompatActivity() {

    val ACTION_ADD: Int = 0
    val ACTION_EDIT: Int = 1
    val ACTION_DEL: Int = 2

    private var editExisting: Boolean = false
    private var startDate: LocalDateTime = LocalDateTime.now()
    private var endDate: LocalDateTime = LocalDateTime.now()

    private lateinit var controller: Controller
    private lateinit var termId: String
    private lateinit var picker: DatePickerDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_term)

        val userId = intent.getStringExtra("userId")!!
        controller = Controller(userId)
        editExisting = (intent.getStringExtra("termId") != null)

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
        val termStartEditText: EditText = this.findViewById(R.id.term_start)
        termStartEditText.inputType = InputType.TYPE_NULL
        termStartEditText.setOnClickListener {
            val month = startDate.monthValue
            val day = startDate.dayOfMonth
            val year = startDate.year

            picker = DatePickerDialog(this,
                OnDateSetListener { view, year, month, day ->
                    val monthString =
                        if (month < 9) "0${month+1}"
                        else (month+1).toString()
                    val dayString =
                        if (day < 10) "0${day}"
                        else day.toString()
                    val dateString = "$monthString/$dayString/$year"
                    termStartEditText.setText(dateString)
                    startDate = LocalDateTime.of(year, month+1, day, 0, 0)
                }, year, month-1, day)
            picker.show()
        }

        val termEndEditText: EditText = this.findViewById(R.id.term_end)
        termEndEditText.inputType = InputType.TYPE_NULL
        termEndEditText.setOnClickListener {
            val month = endDate.monthValue
            val day = endDate.dayOfMonth
            val year = endDate.year

            picker = DatePickerDialog(this,
                OnDateSetListener { view, year, month, day ->
                    val monthString =
                        if (month < 9) "0${month+1}"
                        else (month+1).toString()
                    val dayString =
                        if (day < 10) "0${day}"
                        else day.toString()
                    val dateString = "$monthString/$dayString/$year"
                    termEndEditText.setText(dateString)
                    endDate = LocalDateTime.of(year, month+1, day, 23, 59)
                }, year, month-1, day)
            picker.show()
        }
    }

    private fun setInitialValues() {
        termId = if (editExisting) intent.getStringExtra("termId")!!
            else ""
        if (editExisting) {
            val termName = intent.getStringExtra("termName")!!
            startDate = LocalDateTime.parse(intent.getStringExtra("termStart")!!)
            endDate = LocalDateTime.parse(intent.getStringExtra("termEnd")!!)
            val default = intent.getBooleanExtra("default", false)

            // set current term values into editor
            val termNameEditText: EditText = this.findViewById(R.id.term_name)
            termNameEditText.setText(termName)

            val termStartEditText: EditText = this.findViewById(R.id.term_start)
            val startMonth =
                if (startDate.monthValue < 10) "0${startDate.monthValue}"
                else startDate.monthValue.toString()
            val startDay =
                if (startDate.dayOfMonth < 10) "0${startDate.dayOfMonth}"
                else startDate.dayOfMonth.toString()
            val startYear = startDate.year.toString()
            val startString = "$startMonth/$startDay/$startYear"
            termStartEditText.setText(startString)

            val termEndEditText: EditText = this.findViewById(R.id.term_end)
            val endMonth =
                if (endDate.monthValue < 10) "0${endDate.monthValue}"
                else endDate.monthValue.toString()
            val endDay =
                if (endDate.dayOfMonth < 10) "0${endDate.dayOfMonth}"
                else endDate.dayOfMonth.toString()
            val endYear = endDate.year.toString()
            val endString = "$endMonth/$endDay/$endYear"
            termEndEditText.setText(endString)

            val currTermSwitch: SwitchMaterial = this.findViewById(R.id.term_form_curr_switch)
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
        val name = this.findViewById<EditText>(R.id.term_name).text.toString()
        val startInput = this.findViewById<EditText>(R.id.term_start).text.toString()
        val endInput = this.findViewById<EditText>(R.id.term_end).text.toString()
        val currTermInput = this.findViewById<SwitchMaterial>(R.id.term_form_curr_switch).isChecked

        try {
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
}