package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager

import android.app.Activity
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.switchmaterial.SwitchMaterial
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Controller
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.WeeklyEvent
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import kotlin.Exception

class CourseFormActivity : AppCompatActivity() {

    val TAG: String = "CourseFormActivity"
    val ACTION_ADD: Int = 0
    val ACTION_EDIT: Int = 1
    val ACTION_DEL: Int = 2

    private var editExisting: Boolean = false
    private var lectStartDate: LocalDateTime = LocalDateTime.now()
    private var lectEndDate: LocalDateTime? = null
    private var sectStartDate: LocalDateTime = LocalDateTime.now()
    private var sectEndDate: LocalDateTime? = null

    private lateinit var controller: Controller
    private lateinit var termId: String
    private var lectureId: String = ""
    private var sectionId: String = ""
    private lateinit var courseId: String
    private lateinit var picker: TimePickerDialog

    private lateinit var courseNameEditText: EditText
    private lateinit var lectStartEditText: EditText
    private lateinit var lectEndEditText: EditText
    private lateinit var lectDaySelectViews: ArrayList<CheckBox>
    private lateinit var sectionSwitch: SwitchMaterial
    private lateinit var sectionLayout: ConstraintLayout
    private lateinit var sectStartEditText: EditText
    private lateinit var sectEndEditText: EditText
    private lateinit var sectDaySelectViews: ArrayList<CheckBox>

    private var initCourseName: String = ""
    private var initLectStart: String = ""
    private var initLectEnd: String = ""
    private var initLectDays: BooleanArray = BooleanArray(5) { false }
    private var initSectCheck: Boolean = false
    private var initSectStart: String = ""
    private var initSectEnd: String = ""
    private var initSectDays: BooleanArray = BooleanArray(5) { false }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_course)

        val userId = intent.getStringExtra("userId")!!
        controller = Controller(userId)
        termId = intent.getStringExtra("termId")!!
        editExisting = (intent.getStringExtra("courseId") != null)

        getFormViews()
        setTheme(R.style.TermsDatePicker)
        setInitialValues()
        setTimeEditListeners()
        setFinishButtonListeners()
    }

    private fun setFinishButtonListeners() {
        val doneBtn: Button = this.findViewById(R.id.course_form_submit)
        doneBtn.setOnClickListener {
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

        val cancelBtn: Button = this.findViewById(R.id.course_form_cancel)
        cancelBtn.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun setTimeEditListeners() {
        lectStartEditText.inputType = InputType.TYPE_NULL
        lectStartEditText.setOnClickListener {
            val hour = lectStartDate.hour
            val min = lectStartDate.minute

            picker = TimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    val timeString = getTimeDisplayString(hourOfDay, minute)
                    lectStartEditText.setText(timeString)
                    lectStartDate = lectStartDate.withHour(hourOfDay).withMinute(minute)
                }, hour, min, false)
            picker.show()
        }

        lectEndEditText.inputType = InputType.TYPE_NULL
        lectEndEditText.setOnClickListener {
            val hour = lectEndDate?.hour ?: lectStartDate.hour
            val min = lectEndDate?.minute ?: lectStartDate.minute

            picker = TimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    val timeString = getTimeDisplayString(hourOfDay, minute)
                    lectEndEditText.setText(timeString)
                    lectEndDate = lectStartDate.withHour(hourOfDay).withMinute(minute)
                }, hour, min, false)
            picker.show()
        }

        sectStartEditText.inputType = InputType.TYPE_NULL
        sectStartEditText.setOnClickListener {
            val hour = sectStartDate.hour
            val min = sectStartDate.minute

            picker = TimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    val timeString = getTimeDisplayString(hourOfDay, minute)
                    sectStartEditText.setText(timeString)
                    sectStartDate = sectStartDate.withHour(hourOfDay).withMinute(minute)
                }, hour, min, false)
            picker.show()
        }

        sectEndEditText.inputType = InputType.TYPE_NULL
        sectEndEditText.setOnClickListener {
            val hour = sectEndDate?.hour ?: sectStartDate.hour
            val min = sectEndDate?.minute ?: sectStartDate.minute

            picker = TimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    val timeString = getTimeDisplayString(hourOfDay, minute)
                    sectEndEditText.setText(timeString)
                    sectEndDate = sectStartDate.withHour(hourOfDay).withMinute(minute)
                }, hour, min, false)
            picker.show()
        }
    }

    private fun setInitialValues() {
        courseId = if (editExisting) intent.getStringExtra("courseId")!! else ""
        if (editExisting) {
            initCourseName = intent.getStringExtra("courseName")!!
            lectureId = intent.getStringExtra("lectureId")!!
            lectStartDate = LocalDateTime.parse(intent.getStringExtra("lectStart")!!)
            lectEndDate = LocalDateTime.parse(intent.getStringExtra("lectEnd")!!)
            initSectCheck = intent.getBooleanExtra("checkSection", false)

            // set current course values into editor
            courseNameEditText.setText(initCourseName)

            initLectStart = getTimeDisplayString(lectStartDate.hour, lectStartDate.minute)
            lectStartEditText.setText(initLectStart)

            initLectEnd = getTimeDisplayString(lectEndDate!!.hour, lectEndDate!!.minute)
            lectEndEditText.setText(initLectEnd)

            // lecture recurrence set as boolean array representing [M, T, W, R, F]
            initLectDays = intent.getBooleanArrayExtra("lectRecur")!!
            for (i in 0 until 5)
                lectDaySelectViews[i].isChecked = initLectDays[i]

            sectionSwitch.isChecked = initSectCheck

            if (initSectCheck) {
                sectionLayout.visibility = View.VISIBLE
                sectionId = intent.getStringExtra("sectionId")!!

                sectStartDate = LocalDateTime.parse(intent.getStringExtra("sectStart")!!)
                sectEndDate = LocalDateTime.parse(intent.getStringExtra("sectEnd")!!)

                initSectStart = getTimeDisplayString(sectStartDate.hour, sectStartDate.minute)
                sectStartEditText.setText(initSectStart)

                initSectEnd = getTimeDisplayString(sectEndDate!!.hour, sectEndDate!!.minute)
                sectEndEditText.setText(initSectEnd)

                initSectDays = intent.getBooleanArrayExtra("sectRecur")!!
                for (i in 0 until 5)
                    sectDaySelectViews[i].isChecked = initSectDays[i]
            }
            makeDeleteButton()
        }
    }

    private fun makeDeleteButton() {
        val deleteBtn: Button = this.findViewById(R.id.term_form_delete)
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
        val lectStartInput = lectStartEditText.text.toString()
        val lectEndInput = lectEndEditText.text.toString()
        val lectDaySelectInput = arrayListOf<DayOfWeek>()
        for (i in 0 until 5)
            if (lectDaySelectViews[i].isChecked)
                when (i) {
                    0 -> lectDaySelectInput.add(DayOfWeek.MONDAY)
                    1 -> lectDaySelectInput.add(DayOfWeek.TUESDAY)
                    2 -> lectDaySelectInput.add(DayOfWeek.WEDNESDAY)
                    3 -> lectDaySelectInput.add(DayOfWeek.THURSDAY)
                    4 -> lectDaySelectInput.add(DayOfWeek.FRIDAY)
                }
        val makeSection = sectionSwitch.isChecked

        try {
            // Get First Lecture Start Time
            var lectStart = parseTimeDisplayString(lectStartInput)
            var lectEnd = parseTimeDisplayString(lectEndInput)
            if (lectStartInput != initLectStart || lectEndInput != initLectEnd) {
                val lectDuration = Duration.between(lectStart, lectEnd)
                lectStart = makeFirstMeetingDate(lectStart, lectDaySelectInput)
                lectEnd = lectStart.plus(lectDuration)
            }
            if (lectDaySelectInput.isEmpty()) throw Exception()

            if (makeSection) updateSection()

            val course =
                if (editExisting) term.courses[courseId]!! else term.addCourse()

            val lecture = course.meet.values.find { it.name == "Lecture" } ?: course.addMeet()
            if (lectStartInput != initLectStart) lecture.event.start = lectStart
            if (lectEndInput != initLectEnd) lecture.event.end = lectEnd
            if (!recurDayEquals(initLectDays, lectDaySelectInput)) {
                // make recur for event and replace
            }

            // Removed Section
            if (initSectCheck && !makeSection) {
                val section = course.meet.values.find { it.name == "Section" }!!
                course.removeMeet(section)
            }

            return true
        } catch (e: Exception) {
            return false
        }
    }

    private fun updateSection() {
        val term = controller.terms[termId]!!
        val sectStartInput = sectStartEditText.text.toString()
        val sectEndInput = sectEndEditText.text.toString()
        val sectDaySelectInput = arrayListOf<DayOfWeek>()
        for (i in 0 until 5)
            if (sectDaySelectViews[i].isChecked)
                when (i) {
                    0 -> sectDaySelectInput.add(DayOfWeek.MONDAY)
                    1 -> sectDaySelectInput.add(DayOfWeek.TUESDAY)
                    2 -> sectDaySelectInput.add(DayOfWeek.WEDNESDAY)
                    3 -> sectDaySelectInput.add(DayOfWeek.THURSDAY)
                    4 -> sectDaySelectInput.add(DayOfWeek.FRIDAY)
                }
        if (sectDaySelectInput.isEmpty()) throw Exception()

        var sectStart = parseTimeDisplayString(sectStartInput)
        var sectEnd = parseTimeDisplayString(sectEndInput)
        if (sectStartInput != initSectStart || sectEndInput != initSectEnd) {
            val sectDuration = Duration.between(sectStart, sectEnd)
            sectStart = makeFirstMeetingDate(sectStart, sectDaySelectInput)
            sectEnd = sectStart.plus(sectDuration)
        }

        // Make Section Changes to Database
        val course =
            if (editExisting) term.courses[courseId]!! else term.addCourse()
        val section = course.meet[sectionId] ?: course.addMeet()

        if (sectStartInput != initSectStart) section.event.start = sectStart
        if (sectEndInput != initSectEnd) section.event.end = sectEnd

        // Only Start and End Time Changed
        val datesChanged
                = sectStartInput != initSectStart || sectEndInput != initSectEnd
        val recurChanged = !recurDayEquals(initSectDays, sectDaySelectInput)

        if (datesChanged && !recurChanged) {
            val changedEvents = course.events.values.filter { it.recur }
        } else if (recurChanged) {
            val recur = WeeklyEvent(section.event)
            recur.start = term.start
            recur.end = term.end
            recur.addDays(*sectDaySelectInput.toTypedArray())
            section.event.recur = recur
        }
    }

    private fun getTimeDisplayString(hour: Int, minute: Int): String {
        val hourString: String
        if (hour == 0) hourString = "12"
        else if (hour < 10) hourString = "0$hour"
        else if (hour < 13) hourString = hour.toString()
        else hourString = (hour - 12).toString()
        val timeSuffix = if (hour < 12) "AM" else "PM"
        return "$hourString:$minute $timeSuffix"
    }

    // Input should be in the form of HH:MM AM/PM
    // Output will be term start date with input time
    private fun parseTimeDisplayString(input: String): LocalDateTime {
        val isPM = (input.substring(6) == "PM")
        var hour = input.substring(0, 2).toInt()
        if (hour == 12 && !isPM) hour = 0
        else if (isPM) hour += 12
        val minute = input.substring(3, 5).toInt()
        return controller.terms[termId]!!.start
            .withHour(hour).withMinute(minute)
    }

    // Returns First Meeting Based on Start Date
    private fun makeFirstMeetingDate(
        meetingTime: LocalDateTime, recurDays: ArrayList<DayOfWeek>
    ): LocalDateTime {
        var eventDate = meetingTime
        // get first date after termStartDate that has DayOfWeek in recurDays
        while (!recurDays.contains(meetingTime.dayOfWeek)) {
            eventDate = meetingTime.plusDays(1.toLong())
        }
        return eventDate
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
        courseNameEditText = this.findViewById(R.id.course_name)
        lectStartEditText = this.findViewById(R.id.course_start)
        lectEndEditText = this.findViewById(R.id.course_end)

        val lectSelM: CheckBox = this.findViewById(R.id.course_day_mon)
        val lectSelT: CheckBox = this.findViewById(R.id.course_day_tue)
        val lectSelW: CheckBox = this.findViewById(R.id.course_day_wed)
        val lectSelR: CheckBox = this.findViewById(R.id.course_day_thu)
        val lectSelF: CheckBox = this.findViewById(R.id.course_day_fri)
        lectDaySelectViews =
            arrayListOf<CheckBox>(lectSelM, lectSelT, lectSelW, lectSelR, lectSelF)

        sectionSwitch = this.findViewById(R.id.course_form_section_switch)
        sectionLayout = this.findViewById(R.id.course_form_section_layout)
        sectStartEditText = this.findViewById(R.id.section_start)
        sectEndEditText = this.findViewById(R.id.course_end)

        val sectSelM: CheckBox = this.findViewById(R.id.section_day_mon)
        val sectSelT: CheckBox = this.findViewById(R.id.section_day_tue)
        val sectSelW: CheckBox = this.findViewById(R.id.section_day_wed)
        val sectSelR: CheckBox = this.findViewById(R.id.section_day_thu)
        val sectSelF: CheckBox = this.findViewById(R.id.section_day_fri)
        sectDaySelectViews =
            arrayListOf<CheckBox>(sectSelM, sectSelT, sectSelW, sectSelR, sectSelF)
    }
}