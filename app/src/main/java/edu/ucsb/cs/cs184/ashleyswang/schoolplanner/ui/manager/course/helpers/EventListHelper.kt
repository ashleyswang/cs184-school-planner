package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.course.helpers

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Event
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Course
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.course.CourseViewFragment
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.course.CourseViewViewModel
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.forms.EventFormActivity
import org.w3c.dom.Text
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.*


class EventListHelper(
    private val fragment: CourseViewFragment,
    private val model: CourseViewViewModel
) {
    private val TAG: String = "EventListHelper"

    private var course: Course
        get() { return model.course }
        set(value) {
            model.course = value
        }
    private var eventsList: ArrayList<Event>
        get() { return model.eventsList }
        set(value) {
            model.eventsList = value
        }

    private var listView: RecyclerView = model.view.findViewById(R.id.manager_course_event_list)

    private var itemListeners: MutableMap<String, ValueEventListener> = mutableMapOf()
    private lateinit var adapter: EventAdapter

    init {
        makeListViewAdapter()
        setDatabaseListeners()
        setFabTouchListener()
        makeEventList()
    }

    private fun makeListViewAdapter() {
        adapter = EventAdapter(eventsList)
        listView.adapter = adapter
        listView.layoutManager = LinearLayoutManager(fragment.requireContext())
    }

    private fun makeEventList() {
        eventsList.clear()
        eventsList.addAll(course.events.values
            .filter { it.recurId == null && !it.isAssign && it.end != null })
        eventsList.sortBy { it.start }
        adapter.notifyDataSetChanged()
    }

    private fun setFabTouchListener() {
        model.view
            .findViewById<FloatingActionButton>(R.id.add_event)
            .setOnClickListener {
                openEventAdder()
            }
    }

    private fun setDatabaseListeners() {
        course.db.child("events").addValueEventListener(
            object: ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val oldList = eventsList
                    makeEventList()

                    val removedItems = oldList.minus(eventsList)
                    for (item in removedItems)
                        removeItemChangeListener(item)

                    val addedItems = eventsList.minus(oldList)
                    for (item in addedItems)
                        setItemChangeListener(item)
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Failed to read database.", error.toException())
                }
            })
    }

    private fun makeItemChangeListener(event: Event): ValueEventListener {
        return object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<Any>()
                if (value != null) {
                    val index = eventsList.indexOf(event)
                    adapter.notifyItemChanged(index)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read database.", error.toException())
            }
        }
    }

    private fun setItemChangeListener(event: Event) {
        if (itemListeners.get(event.id) == null) {
            val listener = makeItemChangeListener(event)
            event.db.addValueEventListener(listener)
            itemListeners.put(event.id, listener)
        }
    }

    private fun removeItemChangeListener(event: Event) {
        val listener = itemListeners.get(event.id)
        listener?.let { event.db.removeEventListener(it) }
        itemListeners.remove(event.id)
    }

    private fun openEventAdder() {
        val intent = Intent(fragment.context, EventFormActivity::class.java)
        intent.putExtra("userId", model.controller.user)
        intent.putExtra("termId", course.term.id)
        intent.putExtra("courseId", course.id)
        fragment.startActivity(intent)
    }

    private fun openEventEditor(event: Event) {
        val intent = Intent(fragment.context, EventFormActivity::class.java)
        intent.putExtra("userId", model.controller.user)
        intent.putExtra("termId", course.term.id)
        intent.putExtra("courseId", course.id)
        intent.putExtra("eventId", event.id)
        intent.putExtra("eventName", event.name)
        intent.putExtra("eventStart", event.start.toString())
        intent.putExtra("eventEnd", event.end!!.toString())
        fragment.startActivity(intent)
    }

    private fun getDateDisplayString(datetime: LocalDateTime): String {
        val dayOfWeek =
            if (datetime.dayOfWeek == DayOfWeek.THURSDAY) "R"
            else datetime.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.US)

        return "$dayOfWeek ${datetime.monthValue}/${datetime.dayOfMonth}"
    }

    private fun getTimeDisplayString(datetime: LocalDateTime): String {
        val hourDigit =
            if (datetime.hour < 13) datetime.hour
            else datetime.hour-12
        val hourString = if (hourDigit == 0) "12" else hourDigit.toString()
        val minString =
            if (datetime.minute < 10) "0${datetime.minute}"
            else datetime.minute.toString()
        val timeSuffix = if (datetime.hour < 12) "AM" else "PM"

        return "$hourString:$minString $timeSuffix"
    }

    inner class EventAdapter (private val events: List<Event>)
        : RecyclerView.Adapter<EventAdapter.ViewHolder>()
    {
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
        {
            lateinit var event: Event
            val eventName: TextView = itemView.findViewById(R.id.event_name)
            val eventDate: TextView = itemView.findViewById(R.id.event_date)
            val eventTime: TextView = itemView.findViewById(R.id.event_time)

            init {
                itemView.setOnClickListener {
                    openEventEditor(event)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val context = parent.context
            val inflater = LayoutInflater.from(context)
            val eventItem = inflater.inflate(R.layout.manager_event_list_item, parent, false)
            return ViewHolder(eventItem)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            val event: Event = events.get(position)

            // Set Course Item Values
            viewHolder.event = event
            viewHolder.eventName.text = event.name
            viewHolder.eventDate.text = getDateDisplayString(event.start)
            var timeString = "${getTimeDisplayString(event.start)}"
            timeString +=
                if (event.end != null) " - ${getTimeDisplayString(event.end!!)}"
                else ""
            viewHolder.eventTime.text = timeString
        }

        // Returns the total count of items in the list
        override fun getItemCount(): Int {
            return events.size
        }
    }
}

