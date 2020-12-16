package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.MainActivity
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Controller
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Event
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Scope
import kotlinx.android.synthetic.main.fragment_calendar.*
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*

class CalendarFragment : Fragment() {

    private val TAG: String = "CalendarFragment"

    private lateinit var model: CalendarViewModel
    private lateinit var controller: Controller
    private lateinit var eventList: ArrayList<Event>
    private lateinit var adapter: CalEventAdapter
    private lateinit var selectedDate: String
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        model =
            ViewModelProviders.of(this).get(CalendarViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_calendar, container, false)
        controller = (activity as MainActivity).controller
        eventList = arrayListOf<Event>()
        makeEventList()
        var calRecyclerView = root.findViewById<RecyclerView>(R.id.calRecyclerView)
        adapter = CalEventAdapter(eventList)
        calRecyclerView.adapter = adapter
        calRecyclerView.layoutManager = LinearLayoutManager(requireContext().applicationContext)
        val dividerItemDecoration = DividerItemDecoration(
            calRecyclerView.context,
            (calRecyclerView.layoutManager as LinearLayoutManager).orientation
        )
        calRecyclerView.addItemDecoration(dividerItemDecoration)
        var calendarView = root.findViewById<CalendarView>(R.id.calendarView)
        val sdf = SimpleDateFormat("MM/dd/yyyy")
        selectedDate = sdf.format(Date(calendarView.date))
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            selectedDate = "$month/$dayOfMonth/$year"
            makeEventList()
            Toast.makeText(
                requireContext().applicationContext,
                "Showing events for $selectedDate",
                Toast.LENGTH_SHORT
            ).show()
        }
        return root
    }

    //init {
        //makeEventList()
        //makeCalEventAdapter()
    //}

    private fun makeEventList() {
        if(eventList.size > 0) eventList.clear()
        for(term in controller.terms){
            eventList.addAll(term.value.events.values)
        }
        eventList.sortBy { it.start }
        //adapter.notifyDataSetChanged()
    }

    private fun makeCalEventAdapter() {
        adapter = CalEventAdapter(eventList)
        calRecyclerView.adapter = adapter
        calRecyclerView.layoutManager = LinearLayoutManager(requireContext().applicationContext)
        val dividerItemDecoration = DividerItemDecoration(
            calRecyclerView.context,
            (calRecyclerView.layoutManager as LinearLayoutManager).orientation
        )
        calRecyclerView.addItemDecoration(dividerItemDecoration)
    }

    inner class CalEventAdapter(private val events: List<Event>)
        : RecyclerView.Adapter<CalEventAdapter.ViewHolder>()
    {
        inner class ViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView), View.OnClickListener
        {
            val eventName: TextView = itemView.findViewById(R.id.event_item_name)
            var eventScope: TextView = itemView.findViewById(R.id.event_item_scope)
            val eventStartTime: TextView = itemView.findViewById(R.id.event_item_start_time)

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
            val event: Event = events.get(position)
            val scope: Scope = event.scope

            // Set Course Item Values
            viewHolder.eventName.text = event.name
            viewHolder.eventScope.text = scope.name
            if (event != null) {
                val dayOfWeek =
                    if (event.start.dayOfWeek == DayOfWeek.THURSDAY) "R"
                    else event.start.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.US)
                val dayOfMonth =
                    if (event.start.dayOfMonth < 10) "0${event.start.dayOfMonth}"
                    else event.start.dayOfMonth.toString()
                val month =
                    if (event.start.monthValue < 10) "0${event.start.monthValue}"
                    else event.start.monthValue.toString()
                val year = event.start.year.toString()
                if(selectedDate == "$dayOfMonth/$month/$year") { //Check if event on selected day
                    val dateString = "$dayOfWeek $month/$dayOfMonth"
                    val hour = event.start.hour
                    val minute = event.start.minute
                    val timeString = "$hour:$minute"
                    viewHolder.eventScope.text = dateString
                    viewHolder.eventStartTime.text = timeString
                }
            } else
                viewHolder.eventName.text = "No upcoming events"
        }

        // Returns the total count of items in the list
        override fun getItemCount(): Int {
            return events.size
        }
    }
}