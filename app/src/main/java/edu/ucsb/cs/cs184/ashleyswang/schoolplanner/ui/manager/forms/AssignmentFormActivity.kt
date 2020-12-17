package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.forms

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Controller
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.Exception

class AssignmentFormActivity : AppCompatActivity() {

    val TAG: String = "AssignmentFormActivity"
    val ACTION_ADD: Int = 0
    val ACTION_EDIT: Int = 1
    val ACTION_DEL: Int = 2

    private var editExisting: Boolean = false
    private var dueDate: LocalDateTime = LocalDateTime.now()

    private lateinit var controller: Controller
    private lateinit var termId: String
    private lateinit var courseId: String
    private lateinit var assignId: String

    private lateinit var datePicker: DatePickerDialog
    private lateinit var timePicker: TimePickerDialog

    private lateinit var nameEditText: EditText
    private lateinit var dateEditText: EditText
    private lateinit var timeEditText: EditText
    private lateinit var noteEditText: EditText

    private lateinit var notifSwitch: SwitchMaterial
    private lateinit var notifLayout: LinearLayout
    private lateinit var notifValEditText: EditText
    private lateinit var notifUnitSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_assignment)

        val userId = intent.getStringExtra("userId")!!
        controller = Controller(userId)
        termId = intent.getStringExtra("termId")!!
        courseId = intent.getStringExtra("courseId")!!
        editExisting = (intent.getStringExtra("assignId") != null)

        getFormViews()
        setTheme(R.style.TermsDatePicker)
        setInitialValues()
        setFormEditListeners()
        setFinishButtonListeners()
    }

    private fun setFinishButtonListeners() {
        val submitBtn: ImageButton = this.findViewById(R.id.assign_form_submit)
        submitBtn.setOnClickListener {
            if (updateAssignment()) {
                val resultIntent: Intent = Intent()
                val action: Int = if (editExisting) ACTION_EDIT else ACTION_ADD
                resultIntent.putExtra("action", action)
                resultIntent.putExtra("assignId", assignId)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
            else {
                val toast = Toast.makeText(this, "Please fill in all available fields.", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 10)
                toast.show()
            }
        }

        val cancelBtn: ImageButton = this.findViewById(R.id.assign_form_cancel)
        cancelBtn.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun setFormEditListeners() {
        dateEditText.inputType = InputType.TYPE_NULL
        dateEditText.setOnClickListener {
            val month = dueDate.monthValue
            val day = dueDate.dayOfMonth
            val year = dueDate.year

            datePicker = DatePickerDialog(this,
                DatePickerDialog.OnDateSetListener { view, year, monthValue, dayOfMonth ->
                    val dateString
                            = getDateDisplayString(year, monthValue+1, dayOfMonth)
                    dateEditText.setText(dateString)
                    dueDate = dueDate.withYear(year)
                        .withMonth(monthValue+1)
                        .withDayOfMonth(dayOfMonth)
                }, year, month-1, day)
            datePicker.show()
        }

        timeEditText.inputType = InputType.TYPE_NULL
        timeEditText.setOnClickListener {
            val hour = dueDate.hour
            val min = dueDate.minute

            timePicker = TimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    val timeString = getTimeDisplayString(hourOfDay, minute)
                    timeEditText.setText(timeString)
                    dueDate = dueDate.withHour(hourOfDay).withMinute(minute)
                }, hour, min, false)
            timePicker.show()
        }

        notifSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            notifLayout.visibility =
                if (notifSwitch.isChecked) View.VISIBLE
                else View.GONE
        }
    }

    private fun setInitialValues() {
        assignId = if (editExisting) intent.getStringExtra("assignId")!! else ""
        if (editExisting) {
            var assignName = intent.getStringExtra("assignName")!!
            dueDate = LocalDateTime.parse(intent.getStringExtra("assignDue")!!)
            var assignDate = getDateDisplayString(dueDate.year, dueDate.monthValue, dueDate.dayOfMonth)
            var assignTime = getTimeDisplayString(dueDate.hour, dueDate.minute)
            var assignNote = intent.getStringExtra("descript") ?: ""

            // set current course values into editor
            nameEditText.setText(assignName)
            dateEditText.setText(assignDate)
            timeEditText.setText(assignTime)
            noteEditText.setText(assignNote)

            val checkNotif = intent.getStringExtra("assignNotif") != null
            var notifVal: Int = 0
            var notifUnit: Int = -1
            if (checkNotif) {
                val duration = Duration.parse(intent.getStringExtra("assignNotif")!!)
                notifVal = duration.toMinutes().toInt()
                notifUnit = 1
                if (notifVal % 60 == 0) {
                    notifVal /= 60
                    notifUnit = 2
                    if (notifVal % 24 == 0) {
                        notifVal /= 24
                        notifUnit = 3
                        if (notifVal % 7 == 0) {
                            notifVal /= 7
                            notifUnit = 4
                        }
                    }
                }
            }
            notifSwitch.isChecked = checkNotif
            if (checkNotif) {
                notifLayout.visibility = View.VISIBLE
                notifValEditText.setText(notifVal.toString())
                notifUnitSpinner.setSelection(notifUnit)
            }

            makeDeleteButton()
        }
    }

    private fun makeDeleteButton() {
        val deleteBtn: Button = this.findViewById(R.id.assign_form_delete)
        deleteBtn.visibility = View.VISIBLE

        deleteBtn.setOnClickListener{
            val term = controller.terms[termId]!!
            val course = term.courses[courseId]!!
            val assign = course.assign[assignId]!!
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete Assignment")
                .setMessage("Are you sure you want to permanently delete this assignment?")
            builder.apply {
                setPositiveButton("DELETE") { dialog, id ->
                    course.removeAssign(assign)
                    dialog.dismiss()
                    val resultIntent: Intent = Intent()
                    resultIntent.putExtra("action", ACTION_DEL)
                    resultIntent.putExtra("assignId", assignId)
                    setResult(Activity.RESULT_OK, resultIntent)
                    this@AssignmentFormActivity.finish()
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

    private fun updateAssignment(): Boolean {
        val term = controller.terms[termId]!!
        val course = term.courses[courseId]!!

        val nameInput = nameEditText.text.toString()
        val dateInput = dateEditText.text.toString()
        val timeInput = timeEditText.text.toString()
        val noteInput = noteEditText.text.toString()

        val notifInput = notifSwitch.isChecked
        val notifValInput = notifValEditText.text.toString()
        val notifUnitInput = notifUnitSpinner.selectedItemPosition
        var notifDuration = Duration.ZERO

        try {
            if (nameInput == "") throw Exception()
            val dueTime = parseTimeDisplayString(timeInput)
            dueDate = parseDateDisplayString(dateInput)
                .withHour(dueTime.hour).withMinute(dueTime.minute)

            if (notifInput) {
                when (notifUnitInput) {
                    -1 -> throw Exception()
                    1  -> notifDuration = Duration.ofMinutes(notifValInput.toLong())
                    2  -> notifDuration = Duration.ofHours(notifValInput.toLong())
                    3  -> notifDuration = Duration.ofDays(notifValInput.toLong())
                    4  -> notifDuration = Duration.ofDays(notifValInput.toLong()*7)
                }
            }

            val notifSetValue = if (notifInput) notifDuration else null

            if (editExisting) {
                val assign = course.assign[assignId]!!
                assign.updateDatabase(nameInput, dueDate, noteInput, notifSetValue)
                if (!notifInput) assign.event.notifTime = null
            } else {
                course.addAssign(nameInput, dueDate, noteInput, notifSetValue)
            }
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

    private fun parseDateDisplayString(input: String): LocalDateTime {
        val dateValues: ArrayList<Int> = arrayListOf<Int>()
        for (value in input.split('/'))
            dateValues.add(value.toInt())
        return LocalDateTime.of(dateValues[2], dateValues[0],
            dateValues[1], 23, 59)
    }

    private fun getTimeDisplayString(hour: Int, minute: Int): String {
        val hourDigit = if (hour < 13) hour else hour-12
        val hourString: String
        if (hourDigit == 0) hourString = "12"
        else if (hourDigit < 10) hourString = "0$hourDigit"
        else hourString = hourDigit.toString()
        val minString = if (minute < 10) "0$minute" else minute.toString()
        val timeSuffix = if (hour < 12) "AM" else "PM"
        return "$hourString:$minString $timeSuffix"
    }

    // Input should be in the form of HH:MM XM
    // Output will be term start date with input time
    private fun parseTimeDisplayString(input: String): LocalTime {
        val isPM = (input.substring(6) == "PM")
        var hour = input.substring(0, 2).toInt()
        if (hour == 12 && !isPM) hour = 0
        else if (hour != 12 && isPM) hour += 12
        val minute = input.substring(3, 5).toInt()
        return LocalTime.of(hour, minute)
    }

    private fun getFormViews() {
        nameEditText = this.findViewById(R.id.assign_name)
        dateEditText = this.findViewById(R.id.assign_date)
        timeEditText = this.findViewById(R.id.assign_time)
        noteEditText = this.findViewById(R.id.assign_description)

        notifSwitch = this.findViewById(R.id.assign_notif_switch)
        notifLayout = this.findViewById(R.id.assign_notif_layout)
        notifValEditText = this.findViewById(R.id.assign_notif_value)
        notifUnitSpinner = this.findViewById(R.id.assign_notif_unit)
    }
}