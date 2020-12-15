package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Course
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Term
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Event
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*
import kotlin.collections.ArrayList

class CourseListHelper(
    private val fragment: ManagerFragment,
    private val model: ManagerViewModel
) {
    private val TAG: String = "CourseListHelper"

    private var activeTerm: Term?
        get() { return model.activeTerm.value }
        set(value) {
            model.activeTerm.value = value
        }
    private var courseList: ArrayList<Course>
        get() { return model.courseList }
        set(value) {
            model.courseList = value
        }

    private var courseLayout: ConstraintLayout = model.view.findViewById(R.id.manager_main_course_layout)
    private var courseView: RecyclerView = model.view.findViewById(R.id.manager_main_course_view)

    // Don't worry about database updates for now
    // private var prevActiveId: String? = null

    init {
        val activeTermObserver: Observer<Term> = Observer<Term> {
            if (activeTerm != null) notifyTermChange()
        }
        model.activeTerm.observe(fragment.viewLifecycleOwner, activeTermObserver)
        // make listeners
    }

    private fun notifyTermChange() {
        initCourseList()
    }

    private fun initCourseList() {
        makeCourseList()
        makeCourseDisplay()
    }

    private fun makeCourseList() {
        courseList.clear()
        courseList.addAll(activeTerm!!.courses.values)
        courseList.sortBy { it.name }
    }

    private fun makeCourseDisplay() {
        val adapter = CoursesAdapter(courseList)
        courseView.adapter = adapter
        courseView.layoutManager = LinearLayoutManager(fragment.requireContext())
    }

    private fun getCourseNextEvent(course: Course): Event? {
        return course.events.values.minBy { it.start }
    }

    inner class CoursesAdapter (private val courses: List<Course>)
        : RecyclerView.Adapter<CoursesAdapter.ViewHolder>()
    {
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val courseName: TextView = itemView.findViewById(R.id.course_item_name)
            val eventName: TextView = itemView.findViewById(R.id.course_item_event_name)
            val eventDate: TextView = itemView.findViewById(R.id.course_item_event_date)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoursesAdapter.ViewHolder {
            val context = parent.context
            val inflater = LayoutInflater.from(context)
            val courseItem = inflater.inflate(R.layout.manager_course_list_item, parent, false)
            return ViewHolder(courseItem)
        }

        override fun onBindViewHolder(viewHolder: CoursesAdapter.ViewHolder, position: Int) {
            val course: Course = courses.get(position)
            val event: Event? = getCourseNextEvent(course)

            // Set Course Item Values
            viewHolder.courseName.text = course.name
            if (event != null) {
                val nameString = "Upcoming: ${event.name}"
                val dayOfWeek =
                    if (event.start.dayOfWeek == DayOfWeek.THURSDAY) "R"
                    else event.start.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.US)
                val dayOfMonth =
                    if (event.start.dayOfMonth < 10) "0${event.start.dayOfMonth}"
                    else event.start.dayOfMonth.toString()
                val month =
                    if (event.start.monthValue < 10) "0${event.start.monthValue}"
                    else event.start.monthValue.toString()
                val dateString = "$dayOfWeek $month/$dayOfMonth"

                viewHolder.eventName.text = nameString
                viewHolder.eventDate.text = dateString
            } else
                viewHolder.eventName.text = "No upcoming events or deadlines"
        }

        // Returns the total count of items in the list
        override fun getItemCount(): Int {
            return courses.size
        }
    }
}

