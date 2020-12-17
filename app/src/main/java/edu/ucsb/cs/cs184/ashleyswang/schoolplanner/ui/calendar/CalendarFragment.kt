package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.MainActivity
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Controller
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Event
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Meeting
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Scope
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.*


class CalendarFragment : Fragment() {

    private val TAG: String = "CalendarFragment"

    private lateinit var model: CalendarViewModel
    private lateinit var controller: Controller
    private lateinit var eventList: ArrayList<CalendarItem>
    private lateinit var adapter: CalEventAdapter
    private lateinit var selectedDate: String
    private lateinit var dateTitle: TextView
    private var today: LocalDateTime = LocalDateTime.now()
    private var nextUp: Boolean = false
    private var past: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        model =
            ViewModelProviders.of(this).get(CalendarViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_calendar, container, false)
        var calendarView: CalendarView = root.findViewById(R.id.calendarView)
        var calendarConstraint: ConstraintLayout = root.findViewById(R.id.calendar_layout)
        var calendarFab: FloatingActionButton = root.findViewById(R.id.cal_showing)
        calendarFab.setImageResource(R.drawable.ic_calendar_24px)

        dateTitle = root.findViewById(R.id.selected_day)
        calendarFab.setOnClickListener() {
            val visible = calendarView.visibility
            if (visible == View.VISIBLE) {
                calendarView.visibility = View.GONE

                val constraintSet = ConstraintSet()
                constraintSet.clone(calendarConstraint)
                constraintSet.connect(
                    R.id.calRecyclerView,
                    ConstraintSet.TOP,
                    R.id.guideline2,
                    ConstraintSet.TOP,
                    0
                )
                constraintSet.connect(
                    R.id.divider2,
                    ConstraintSet.BOTTOM,
                    R.id.guideline2,
                    ConstraintSet.TOP,
                    0
                )
                constraintSet.applyTo(calendarConstraint)
            }
            else {
                calendarView.visibility = View.VISIBLE

                val constraintSet = ConstraintSet()
                constraintSet.clone(calendarConstraint)
                constraintSet.connect(
                    R.id.calRecyclerView,
                    ConstraintSet.TOP,
                    R.id.guideline,
                    ConstraintSet.TOP,
                    0
                )
                constraintSet.connect(
                    R.id.divider2,
                    ConstraintSet.BOTTOM,
                    R.id.guideline,
                    ConstraintSet.TOP,
                    0
                )
                constraintSet.applyTo(calendarConstraint)
            }
        }

        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            getSelectedDate(year, month, dayOfMonth)
        }

        controller = (activity as MainActivity).controller
        eventList = arrayListOf<CalendarItem>()
        val sdf = SimpleDateFormat("MM/dd/yyyy") // format date
        selectedDate = sdf.format(Date(calendarView.date))

        //init recyclerview
        var calRecyclerView = root.findViewById<RecyclerView>(R.id.calRecyclerView)
        adapter = CalEventAdapter(eventList)
        makeEventList()
        calRecyclerView.adapter = adapter
        calRecyclerView.layoutManager = LinearLayoutManager(requireContext().applicationContext)
        val dividerItemDecoration = DividerItemDecoration(
            calRecyclerView.context,
            (calRecyclerView.layoutManager as LinearLayoutManager).orientation
        )
        calRecyclerView.addItemDecoration(dividerItemDecoration)

//        val date = Calendar.getInstance()
//        val today = sdf.format(date.time) // today
//        val m = today.substring(0, 2).toInt() - 1
//        val d = today.substring(3, 5).toInt()
//        val y = today.substring(6).toInt()
        //getSelectedDate(y, m, d) // load events from today on init

        return root
    }

    private fun getSelectedDate(year: Int, month: Int, dayOfMonth: Int) {
        var monthFix = month+1
        var monthString = monthFix.toString()
        if(month < 10) monthString = "0$monthString"
        var dayOfMonthString = dayOfMonth.toString()
        if(dayOfMonth < 10) dayOfMonthString = "0$dayOfMonthString"
        selectedDate = "$year-$monthString-$dayOfMonthString"

        // set title of calendar fragment to selected date
        var date: LocalDate = LocalDate.of(year, month + 1, dayOfMonth)
        val day = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US)
        val monthName = date.month.getDisplayName(TextStyle.SHORT, Locale.US)
        val year = date.year.toString()
        val titleStr = "$day $monthName $dayOfMonth, $year"
        dateTitle.text = titleStr

        makeEventList()
        past = true
        nextUp = false

        Log.d(TAG, "selectedDate: " + selectedDate)
    }

//    init {
//        makeEventList()
//        makeCalEventAdapter()
//    }

    private fun makeEventList() {
        Log.d(TAG, "hello")
        if(eventList.size > 0) eventList.clear()
        Log.d(TAG, "controller terms: "+controller.terms.toString())
        for(term in controller.terms){
            for(event in term.value.events){
                var date = event.value.start.toString()
                date = date.dropLast(6)
                //Log.d(TAG, "$date vs $selectedDate")
                if(date == selectedDate) eventList.add(CalendarItem(event.value))
            }
            for(course in term.value.courses){
                for(event in course.value.events){
                    var date = event.value.start.toString()
                    date = date.dropLast(6)
                    //Log.d(TAG, "$date vs $selectedDate")
                    if(date == selectedDate) eventList.add(CalendarItem(event.value))
                }
                for(meeting in course.value.meet) {
                    if (meeting.value.meetsOnDay(today.dayOfWeek)) {
                        var item = CalendarItem(meeting.value, today)
                        eventList.add(item)
                    }
                }
            }
        }

        eventList.sortBy { it.start }
        adapter.notifyDataSetChanged()

        Log.d(TAG, eventList.toString())
    }

    private fun makeTimeString(dateTime: LocalDateTime): String {
        // get time attributes
        var hour = dateTime.hour
        val minute = dateTime.minute
        var xm = "" // AM/PM

        // single digit minute --> 0x double digit
        val minuteStr =
            if(minute.toString().length == 1) "0" + minute.toString()
            else minute.toString()

        // fix hour to be conventional
        if (hour == 0) {
            hour = 12
            xm = "AM"
        }
        else if (hour < 12) {
            xm = "AM"
        }
        else if (hour == 12) {
            xm = "PM"
        }
        else {
            hour -= 12
            xm = "PM"
        }

        return "$hour:"+minuteStr+" "+xm
    }

    inner class CalEventAdapter(private val calendarItems: List<CalendarItem>)
        : RecyclerView.Adapter<CalEventAdapter.ViewHolder>()
    {
        inner class ViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView), View.OnClickListener
        {
            val eventItemWrapper: LinearLayout = itemView.findViewById(R.id.event_item_wrapper)
            val eventName: TextView = itemView.findViewById(R.id.event_item_name)
            val eventStartTime: TextView = itemView.findViewById(R.id.event_item_start_time)
            var eventEndTime: TextView = itemView.findViewById(R.id.event_item_end_time)
            val eventCourseName: TextView = itemView.findViewById(R.id.event_course_name)

            init {
                itemView.setOnClickListener(this)
            }

            override fun onClick(v: View?) {
                Log.d(TAG, "temp on click")
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalEventAdapter.ViewHolder {
            val context = parent.context
            val inflater = LayoutInflater.from(context)
            val courseItem = inflater.inflate(R.layout.calendar_event_list_item, parent, false)
            return ViewHolder(courseItem)
        }

        override fun onBindViewHolder(viewHolder: CalEventAdapter.ViewHolder, position: Int) {
            val item: CalendarItem = calendarItems.get(position)
            val scope: Scope = item.scope

            // Set Course Item Values
            viewHolder.eventName.text = item.name
            viewHolder.eventCourseName.text = scope.name
            if (item != null) {
                // get start and end time strings for calendar list items
                val startTimeString = makeTimeString(item.start)
                var endTimeString = ""
                if (item.end != null) {
                    endTimeString = makeTimeString(item.end)
                }
                viewHolder.eventStartTime.text = startTimeString
                viewHolder.eventEndTime.text = endTimeString

                // HIGHLIGHT NEXT EVENT IN THE CURRENT DAY
                if(item.start.dayOfMonth == today.dayOfMonth
                    && item.start.monthValue == today.monthValue
                    && item.start.year == today.year) {
                    if (past) {
                        if(today.hour < item.start.hour) {
                            past = false
                            nextUp = true
                            viewHolder.eventItemWrapper.setBackgroundResource(R.color.nextEventHighlight)
                        }
                        else if (today.hour == item.start.hour) {
                            if(today.minute < item.start.minute) {
                                past = false
                                nextUp = true
                                viewHolder.eventItemWrapper.setBackgroundResource(R.color.nextEventHighlight)
                            }
                            else if (item.end != null) {
                                if(item.end.hour > today.hour) {
                                    past = false
                                    nextUp = true
                                    viewHolder.eventItemWrapper.setBackgroundResource(R.color.nextEventHighlight)
                                }
                                else if(item.end.hour === today.hour && today.hour < item.end.hour) {
                                    past = false
                                    nextUp = true
                                    viewHolder.eventItemWrapper.setBackgroundResource(R.color.nextEventHighlight)
                                }
                            }
                        }
                    }
                }
            } else
                viewHolder.eventName.text = "No upcoming events"
        }

        // Returns the total count of items in the list
        override fun getItemCount(): Int {
            return calendarItems.size
        }
    }

    inner class CalendarItem {
        val scope: Scope
        val name: String
        val start: LocalDateTime
        val end: LocalDateTime?

        constructor(event: Event) {
            scope = event.scope
            name = event.name
            start = event.start
            end = event.end
        }

        constructor(meeting: Meeting, today: LocalDateTime) {
            scope = meeting.course
            name = meeting.name
            start = LocalDateTime.of(today.toLocalDate(), meeting.start)
            end = LocalDateTime.of(today.toLocalDate(), meeting.end)
        }
    }
}