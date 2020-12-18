package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.forms

import android.app.Activity
import android.app.AlertDialog
import android.app.TimePickerDialog
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
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalTime
import kotlin.Exception

class MeetingFormActivity : AppCompatActivity() {

    val TAG: String = "MeetingFormActivity"
    val ACTION_ADD: Int = 0
    val ACTION_EDIT: Int = 1
    val ACTION_DEL: Int = 2

    private var editExisting: Boolean = false
    private var startTime: LocalTime = LocalTime.now()
    private var endTime: LocalTime? = null

    private lateinit var controller: Controller
    private lateinit var termId: String
    private lateinit var courseId: String
    private lateinit var meetId: String
    private lateinit var picker: TimePickerDialog

    private lateinit var meetNameEditText: EditText
    private lateinit var startTimeEditText: EditText
    private lateinit var endTimeEditText: EditText
    private lateinit var daySelectViews: ArrayList<CheckBox>

    private lateinit var notifSwitch: SwitchMaterial
    private lateinit var notifLayout: LinearLayout
    private lateinit var notifValEditText: EditText
    private lateinit var notifUnitSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_meeting)

        val userId = intent.getStringExtra("userId")!!
        controller = Controller(userId)
        termId = intent.getStringExtra("termId")!!
        courseId = intent.getStringExtra("courseId")!!
        editExisting = (intent.getStringExtra("meetId") != null)

        getFormViews()
        setTheme(R.style.TermsDatePicker)
        setInitialValues()
        setFormEditListeners()
        setFinishButtonListeners()
    }

    private fun setFinishButtonListeners() {
        val submitBtn: ImageButton = this.findViewById(R.id.meeting_form_submit)
        submitBtn.setOnClickListener {
            if (updateMeeting()) {
                val resultIntent: Intent = Intent()
                val action: Int = if (editExisting) ACTION_EDIT else ACTION_ADD
                resultIntent.putExtra("action", action)
                resultIntent.putExtra("meetId", meetId)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
            else {
                val toast = Toast.makeText(this, "Please fill in all available fields.", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 10)
                toast.show()
            }
        }

        val cancelBtn: ImageButton = this.findViewById(R.id.meeting_form_cancel)
        cancelBtn.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun setFormEditListeners() {
        startTimeEditText.inputType = InputType.TYPE_NULL
        startTimeEditText.setOnClickListener {
            val hour = startTime.hour
            val min = startTime.minute

            picker = TimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    val timeString = getTimeDisplayString(hourOfDay, minute)
                    startTimeEditText.setText(timeString)
                    startTime = LocalTime.of(hourOfDay, minute)
                }, hour, min, false)
            picker.show()
        }

        endTimeEditText.inputType = InputType.TYPE_NULL
        endTimeEditText.setOnClickListener {
            val hour = endTime?.hour ?: startTime.hour
            val min = endTime?.minute ?: startTime.minute

            picker = TimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    val timeString = getTimeDisplayString(hourOfDay, minute)
                    endTimeEditText.setText(timeString)
                    endTime = LocalTime.of(hourOfDay, minute)
                }, hour, min, false)
            picker.show()
        }

        notifSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            notifLayout.visibility =
                if (notifSwitch.isChecked) View.VISIBLE
                else View.GONE
        }
    }

    private fun setInitialValues() {
        meetId = if (editExisting) intent.getStringExtra("meetId")!! else ""
        if (editExisting) {
            val meetName = intent.getStringExtra("meetName")!!
            startTime = LocalTime.parse(intent.getStringExtra("meetStart")!!)
            endTime = LocalTime.parse(intent.getStringExtra("meetEnd")!!)

            // set current course values into editor
            meetNameEditText.setText(meetName)

            val meetStart = getTimeDisplayString(startTime.hour, startTime.minute)
            startTimeEditText.setText(meetStart)

            val meetEnd = getTimeDisplayString(endTime!!.hour, endTime!!.minute)
            endTimeEditText.setText(meetEnd)

            // lecture recurrence set as boolean array representing [M, T, W, R, F]
            var meetRecur = intent.getBooleanArrayExtra("meetRecur")!!
            for (i in 0 until 5)
                daySelectViews[i].isChecked = meetRecur[i]

            var checkNotif = intent.getStringExtra("meetNotif") != null
            var notifVal: Int = 0
            var notifUnit: Int = -1
            if (checkNotif) {
                val duration = Duration.parse(intent.getStringExtra("meetNotif")!!)
                notifVal = duration.toMinutes().toInt()
                notifUnit = 0
                if (notifVal % 60 == 0) {
                    notifVal /= 60
                    notifUnit = 1
                    if (notifVal % 24 == 0) {
                        notifVal /= 24
                        notifUnit = 2
                        if (notifVal % 7 == 0) {
                            notifVal /= 7
                            notifUnit = 3
                        }
                    }
                }
            }
            notifSwitch.isChecked = checkNotif
            if (checkNotif) {
                Log.d("checkNotif", "checkNotif is true")
                notifLayout.visibility = View.VISIBLE
                notifValEditText.setText(notifVal.toString())
                notifUnitSpinner.setSelection(notifUnit)
            }

            makeDeleteButton()
        }
    }

    private fun makeDeleteButton() {
        val deleteBtn: Button = this.findViewById(R.id.meeting_form_delete)
        deleteBtn.visibility = View.VISIBLE

        deleteBtn.setOnClickListener{
            val term = controller.terms[termId]!!
            val course = term.courses[courseId]!!
            val meeting = course.meet[meetId]!!
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete Class")
                .setMessage("Are you sure you want to permanently delete this class meeting?")
            builder.apply {
                setPositiveButton("DELETE") { dialog, id ->
                    course.removeMeet(meeting)
                    dialog.dismiss()
                    val resultIntent: Intent = Intent()
                    resultIntent.putExtra("action", ACTION_DEL)
                    resultIntent.putExtra("meetId", meetId)
                    setResult(Activity.RESULT_OK, resultIntent)
                    this@MeetingFormActivity.finish()
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

    private fun updateMeeting(): Boolean {
        val term = controller.terms[termId]!!
        val course = term.courses[courseId]!!
        val nameInput = meetNameEditText.text.toString()
        val startInput = startTimeEditText.text.toString()
        val endInput = endTimeEditText.text.toString()
        val selDayInput = arrayListOf<DayOfWeek>()
        for (i in 0 until 5)
            if (daySelectViews[i].isChecked)
                when (i) {
                    0 -> selDayInput.add(DayOfWeek.MONDAY)
                    1 -> selDayInput.add(DayOfWeek.TUESDAY)
                    2 -> selDayInput.add(DayOfWeek.WEDNESDAY)
                    3 -> selDayInput.add(DayOfWeek.THURSDAY)
                    4 -> selDayInput.add(DayOfWeek.FRIDAY)
                }

        val notifInput = notifSwitch.isChecked
        val notifValInput = notifValEditText.text.toString()
        val notifUnitInput = notifUnitSpinner.selectedItemPosition
        var notifDuration = Duration.ZERO

        try {
            // Get First Lecture Start Time
            val inputStartTime = parseTimeDisplayString(startInput)
            val inputEndTime = parseTimeDisplayString(endInput)
            if (nameInput == "" || selDayInput.isEmpty()) throw Exception()

            if (notifInput) {
                when (notifUnitInput) {
                    -1 -> throw Exception()
                    0  -> notifDuration = Duration.ofMinutes(notifValInput.toLong())
                    1  -> notifDuration = Duration.ofHours(notifValInput.toLong())
                    2  -> notifDuration = Duration.ofDays(notifValInput.toLong())
                    3  -> notifDuration = Duration.ofDays(notifValInput.toLong()*7)
                }
            }

            val meeting = course.meet[meetId] ?: course.addMeet()

            val lectDaysArray = BooleanArray(5) { false }
            for (i in 0 until 5)
                lectDaysArray[i] = daySelectViews[i].isChecked
            val notifSetValue = if (notifInput) notifDuration else null

            meeting.updateDatabase(nameInput, inputStartTime,
                inputEndTime, notifSetValue, lectDaysArray)
            if (!notifInput) meeting.notifTime = null

            return true
        } catch (e: Exception) {
            return false
        }
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

    private fun recurDayEquals(
        init: BooleanArray, edit: ArrayList<DayOfWeek>
    ): Boolean {
        var equals = true
        val daysOfWeek = arrayListOf<DayOfWeek>(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)

        for (i in 0 until 5)
            equals = equals && (init[i] == edit.contains(daysOfWeek[i]))

        return equals
    }

    private fun getFormViews() {
        meetNameEditText = this.findViewById(R.id.meeting_name)
        startTimeEditText = this.findViewById(R.id.meeting_start)
        endTimeEditText = this.findViewById(R.id.meeting_end)

        val selectM: CheckBox = this.findViewById(R.id.meeting_day_mon)
        val selectT: CheckBox = this.findViewById(R.id.meeting_day_tue)
        val selectW: CheckBox = this.findViewById(R.id.meeting_day_wed)
        val selectR: CheckBox = this.findViewById(R.id.meeting_day_thu)
        val selectF: CheckBox = this.findViewById(R.id.meeting_day_fri)
        daySelectViews =
            arrayListOf<CheckBox>(selectM, selectT, selectW, selectR, selectF)

        notifSwitch = this.findViewById(R.id.meeting_notif_switch)
        notifLayout = this.findViewById(R.id.meeting_notif_layout)
        notifValEditText = this.findViewById(R.id.meeting_notif_value)
        notifUnitSpinner = this.findViewById(R.id.meeting_notif_unit)
    }
}