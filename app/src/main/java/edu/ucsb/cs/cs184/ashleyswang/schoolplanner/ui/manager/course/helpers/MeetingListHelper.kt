package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.course.helpers

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Course
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Meeting
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.course.CourseViewFragment
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.course.CourseViewViewModel
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.forms.CourseFormActivity
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.forms.MeetingFormActivity
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule


class MeetingListHelper(
    private val fragment: CourseViewFragment,
    private val model: CourseViewViewModel
) {
    private val TAG: String = "MeetingListHelper"

    private var course: Course
        get() { return model.course }
        set(value) {
            model.course = value
        }
    private var meetList: ArrayList<Meeting>
        get() { return model.meetList }
        set(value) {
            model.meetList = value
        }

    private var listView: RecyclerView = model.view.findViewById(R.id.manager_course_meet_list)
    private lateinit var adapter: MeetingAdapter

    init {
        makeListViewAdapter()
        setDatabaseListeners()
        setFabTouchListener()
        makeMeetingList()
    }

    private fun makeListViewAdapter() {
        adapter = MeetingAdapter(meetList)
        listView.adapter = adapter
        listView.layoutManager = LinearLayoutManager(fragment.requireContext())
    }

    private fun makeMeetingList() {
        meetList.clear()
        meetList.addAll(course.meet.values)
        meetList.sortBy { it.createdOn }
        adapter.notifyDataSetChanged()
    }

    private fun setFabTouchListener() {
        model.view
            .findViewById<FloatingActionButton>(R.id.add_meet)
            .setOnClickListener {
                openMeetingAdder()
            }
    }

    private fun setDatabaseListeners() {
        course.db.child("meet").addValueEventListener(
            object: ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    makeMeetingList()
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Failed to read database.", error.toException())
                }
            })
    }

    private fun openMeetingAdder() {
        val intent = Intent(fragment.context, MeetingFormActivity::class.java)
        intent.putExtra("userId", model.controller.user)
        intent.putExtra("termId", course.term.id)
        intent.putExtra("courseId", course.id)
        fragment.startActivity(intent)
    }

    private fun openMeetingEditor(meet: Meeting) {
        val intent = Intent(fragment.context, MeetingFormActivity::class.java)
        intent.putExtra("userId", model.controller.user)
        intent.putExtra("termId", course.term.id)
        intent.putExtra("courseId", course.id)
        intent.putExtra("meetId", meet.id)
        intent.putExtra("meetName", meet.name)
        intent.putExtra("meetStart", meet.start.toString())
        intent.putExtra("meetEnd", meet.end.toString())
        intent.putExtra("meetRecur", meet.daysToRepeat)
        if (meet.notifTime != null)
            intent.putExtra("meetNotif", meet.notifTime.toString())
        fragment.startActivity(intent)
    }

    private fun getTimeDisplayString(hour: Int, minute: Int): String {
        val hourDigit = if (hour < 13) hour else hour-12
        val hourString = if (hourDigit == 0) "12" else hourDigit.toString()
        val minString = if (minute < 10) "0$minute" else minute.toString()
        val timeSuffix = if (hour < 12) "AM" else "PM"
        return "$hourString:$minString $timeSuffix"
    }

    inner class MeetingAdapter (private val meetings: List<Meeting>)
        : RecyclerView.Adapter<MeetingAdapter.ViewHolder>()
    {
        inner class ViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView), View.OnClickListener
        {
            lateinit var meeting: Meeting
            val meetName: TextView = itemView.findViewById(R.id.meeting_name)
            val meetDays: TextView = itemView.findViewById(R.id.meeting_days)
            val meetTime: TextView = itemView.findViewById(R.id.meeting_time)

            init {
                itemView.setOnClickListener(this)
            }

            override fun onClick(v: View?) {
                openMeetingEditor(meeting)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val context = parent.context
            val inflater = LayoutInflater.from(context)
            val meetItem = inflater.inflate(R.layout.manager_meeting_list_item, parent, false)
            return ViewHolder(meetItem)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            val meeting: Meeting = meetings.get(position)

            // Set Course Item Values
            viewHolder.meeting = meeting
            viewHolder.meetName.text = meeting.name

            val daysToRepeat = meeting.daysToRepeat
            var daysString = ""
            for (i in 0 until 5) {
                if (daysToRepeat[i]) {
                    if (daysString != "") daysString += " "
                    when (i) {
                        0 -> daysString += "M"
                        1 -> daysString += "T"
                        2 -> daysString += "W"
                        3 -> daysString += "R"
                        4 -> daysString += "F"
                    }
                }
            }
            viewHolder.meetDays.text = daysString

            val startTime =
                getTimeDisplayString(meeting.start.hour, meeting.start.minute)
            val endTime =
                getTimeDisplayString(meeting.end.hour, meeting.end.minute)
            val timeString = "$startTime - $endTime"
            viewHolder.meetTime.text = timeString
        }

        // Returns the total count of items in the list
        override fun getItemCount(): Int {
            return meetings.size
        }
    }
}

