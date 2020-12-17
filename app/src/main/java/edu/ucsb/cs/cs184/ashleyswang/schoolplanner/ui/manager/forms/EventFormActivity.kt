package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.forms

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.Exception

class EventFormActivity : AppCompatActivity() {

    val TAG: String = "EventFormActivity"
    val ACTION_ADD: Int = 0
    val ACTION_EDIT: Int = 1
    val ACTION_DEL: Int = 2

    private var editExisting: Boolean = false
    private var dateValue: LocalDateTime = LocalDateTime.now()
    private var startTime: LocalTime = LocalTime.now()
    private var endTime: LocalTime? = null

    private lateinit var controller: Controller
    private lateinit var termId: String
    private lateinit var courseId: String
    private lateinit var eventId: String

    private lateinit var datePicker: DatePickerDialog
    private lateinit var timePicker: TimePickerDialog

    private lateinit var nameEditText: EditText
    private lateinit var dateEditText: EditText
    private lateinit var startEditText: EditText
    private lateinit var endEditText: EditText

    private lateinit var notifSwitch: SwitchMaterial
    private lateinit var notifLayout: LinearLayout
    private lateinit var notifValEditText: EditText
    private lateinit var notifUnitSpinner: Spinner

    private var initName: String = ""
    private var initDate: String = ""
    private var initStart: String = ""
    private var initEnd: String = ""
    private var initNotif: Boolean = false
    private var initNotifDuration: Duration = Duration.ZERO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_event)

        val userId = intent.getStringExtra("userId")!!
        controller = Controller(userId)
        termId = intent.getStringExtra("termId")!!
        courseId = intent.getStringExtra("courseId")!!
        editExisting = (intent.getStringExtra("eventId") != null)

        getFormViews()
        setTheme(R.style.TermsDatePicker)
        setInitialValues()
        setFormEditListeners()
        setFinishButtonListeners()
    }

    private fun setFinishButtonListeners() {
        val submitBtn: ImageButton = this.findViewById(R.id.event_form_submit)
        submitBtn.setOnClickListener {
            if (updateEvent()) {
                val resultIntent: Intent = Intent()
                val action: Int = if (editExisting) ACTION_EDIT else ACTION_ADD
                resultIntent.putExtra("action", action)
                resultIntent.putExtra("eventId", eventId)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
            else {
                val toast = Toast.makeText(this, "Please fill in all available fields.", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 10)
                toast.show()
            }
        }

        val cancelBtn: ImageButton = this.findViewById(R.id.event_form_cancel)
        cancelBtn.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun setFormEditListeners() {
        dateEditText.inputType = InputType.TYPE_NULL
        dateEditText.setOnClickListener {
            val month = dateValue.monthValue
            val day = dateValue.dayOfMonth
            val year = dateValue.year

            datePicker = DatePickerDialog(this,
                DatePickerDialog.OnDateSetListener { view, year, monthValue, dayOfMonth ->
                    val dateString
                            = getDateDisplayString(year, monthValue+1, dayOfMonth)
                    dateEditText.setText(dateString)
                    dateValue = dateValue.withYear(year)
                        .withMonth(monthValue+1)
                        .withDayOfMonth(dayOfMonth)
                }, year, month-1, day)
            datePicker.show()
        }

        startEditText.inputType = InputType.TYPE_NULL
        startEditText.setOnClickListener {
            val hour = startTime.hour
            val min = startTime.minute

            timePicker = TimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    val timeString = getTimeDisplayString(hourOfDay, minute)
                    startEditText.setText(timeString)
                    startTime = LocalTime.of(hourOfDay, minute)
                }, hour, min, false)
            timePicker.show()
        }

        endEditText.inputType = InputType.TYPE_NULL
        endEditText.setOnClickListener {
            val hour = endTime?.hour ?: startTime.hour
            val min = endTime?.minute ?: startTime.minute

            timePicker = TimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    val timeString = getTimeDisplayString(hourOfDay, minute)
                    endEditText.setText(timeString)
                    endTime = LocalTime.of(hourOfDay, minute)
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
        eventId = if (editExisting) intent.getStringExtra("eventId")!! else ""
        if (editExisting) {
            initName = intent.getStringExtra("eventName")!!
            dateValue = LocalDateTime.parse(intent.getStringExtra("eventEnd")!!)
            initEnd = getTimeDisplayString(dateValue.hour, dateValue.minute)
            dateValue = LocalDateTime.parse(intent.getStringExtra("eventStart")!!)
            initDate = getDateDisplayString(dateValue.year,
                dateValue.monthValue, dateValue.dayOfMonth)
            initStart = getTimeDisplayString(dateValue.hour, dateValue.minute)

            // set current course values into editor
            nameEditText.setText(initName)
            dateEditText.setText(initDate)
            startEditText.setText(initStart)
            endEditText.setText(initEnd)

            initNotif = intent.getStringExtra("eventNotif") != null
            var notifVal: Int = 0
            var notifUnit: Int = -1
            if (initNotif) {
                initNotifDuration = Duration.parse(intent.getStringExtra("eventNotif")!!)
                notifVal = initNotifDuration.toMinutes().toInt()
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
            notifSwitch.isChecked = initNotif
            if (initNotif) {
                notifLayout.visibility = View.VISIBLE
                notifValEditText.setText(notifVal.toString())
                notifUnitSpinner.setSelection(notifUnit)
            }

            makeDeleteButton()
        }
    }

    private fun makeDeleteButton() {
        val deleteBtn: Button = this.findViewById(R.id.event_form_delete)
        deleteBtn.visibility = View.VISIBLE

        deleteBtn.setOnClickListener{
            val term = controller.terms[termId]!!
            val course = term.courses[courseId]!!
            val event = course.events[eventId]!!
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete Event")
                .setMessage("Are you sure you want to permanently delete this event?")
            builder.apply {
                setPositiveButton("DELETE") { dialog, id ->
                    course.removeEvent(event)
                    dialog.dismiss()
                    val resultIntent: Intent = Intent()
                    resultIntent.putExtra("action", ACTION_DEL)
                    resultIntent.putExtra("eventId", eventId)
                    setResult(Activity.RESULT_OK, resultIntent)
                    this@EventFormActivity.finish()
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

    private fun updateEvent(): Boolean {
        val term = controller.terms[termId]!!
        val course = term.courses[courseId]!!

        val nameInput = nameEditText.text.toString()
        val dateInput = dateEditText.text.toString()
        val startInput = startEditText.text.toString()
        val endInput = endEditText.text.toString()

        val notifInput = notifSwitch.isChecked
        val notifValInput = notifValEditText.text.toString()
        val notifUnitInput = notifUnitSpinner.selectedItemPosition
        var notifDuration = Duration.ZERO

        try {
            if (nameInput == "") throw Exception()
            dateValue = parseDateDisplayString(dateInput)
            startTime = parseTimeDisplayString(startInput)
            endTime = parseTimeDisplayString(endInput)

            val startDateTime =
                dateValue.withHour(startTime.hour).withMinute(startTime.minute)
            val endDateTime =
                dateValue.withHour(endTime!!.hour).withMinute(endTime!!.minute)

            if (notifInput) {
                when (notifUnitInput) {
                    -1 -> throw Exception()
                    0  -> notifDuration = Duration.ofMinutes(notifValInput.toLong())
                    1  -> notifDuration = Duration.ofHours(notifValInput.toLong())
                    2  -> notifDuration = Duration.ofDays(notifValInput.toLong())
                    3  -> notifDuration = Duration.ofDays(notifValInput.toLong()*7)
                }
            }

            val event = course.events[eventId] ?: course.addEvent()

            if (nameInput != initName) event.name = nameInput
            if (dateInput != initDate || startInput != initStart)
                event.start = startDateTime
            if (dateInput != initDate || endInput != initEnd)
                event.end = endDateTime

            if (!notifInput && initNotif)
                event.notifTime = null
            else if (notifInput && notifDuration != initNotifDuration)
                event.notifTime = notifDuration

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
        nameEditText = this.findViewById(R.id.event_name)
        dateEditText = this.findViewById(R.id.event_date)
        startEditText = this.findViewById(R.id.event_start)
        endEditText = this.findViewById(R.id.event_end)

        notifSwitch = this.findViewById(R.id.event_notif_switch)
        notifLayout = this.findViewById(R.id.event_notif_layout)
        notifValEditText = this.findViewById(R.id.event_notif_value)
        notifUnitSpinner = this.findViewById(R.id.event_notif_unit)
    }
}