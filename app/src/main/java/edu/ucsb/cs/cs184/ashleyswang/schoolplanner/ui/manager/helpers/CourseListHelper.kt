package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.helpers

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Course
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Event
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Term
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.ManagerFragment
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.ManagerViewModel
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.course.CourseViewFragment
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

    private var prevActiveId: String? = null
    private var courseListener: ValueEventListener = object: ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val oldCourses = courseList
            makeCourseList()

            val removedCourses = oldCourses.minus(courseList)
            for (c in removedCourses)
                removeNameChangeListener(c)

            val addedCourses = courseList.minus(oldCourses)
            for (c in addedCourses)
                setNameChangeListener(c)
        }
        override fun onCancelled(error: DatabaseError) {
            Log.w(TAG, "Failed to read database.", error.toException())
        }
    }
    private var nameListeners: MutableMap<String, ValueEventListener> = mutableMapOf()
    private lateinit var adapter: CoursesAdapter

    init {
        makeCourseViewAdapter()
        val activeTermObserver: Observer<Term> = Observer<Term> {
            if (activeTerm != null) notifyTermChange()
        }
        model.activeTerm.observe(fragment.viewLifecycleOwner, activeTermObserver)
    }

    private fun notifyTermChange() {
        removeDatabaseListeners()
        setDatabaseListeners()
        makeCourseList()
    }

    private fun makeCourseList() {
        courseList.clear()
        courseList.addAll(activeTerm!!.courses.values)
        courseList.sortBy { it.name }
        adapter.notifyDataSetChanged()
    }

    private fun makeCourseViewAdapter() {
        adapter = CoursesAdapter(courseList)
        courseView.adapter = adapter
        courseView.layoutManager = LinearLayoutManager(fragment.requireContext())
//        val divider = DividerItemDecoration(
//            courseView.context,
//            (courseView.layoutManager as LinearLayoutManager).orientation
//        )
//        courseView.addItemDecoration(divider)
    }

    private fun setDatabaseListeners() {
        activeTerm!!.db.child("courses").addValueEventListener(courseListener)
    }

    private fun removeDatabaseListeners() {
        try {
            if (prevActiveId != null)
                model.controller.terms[prevActiveId!!]!!.db.child("name")
                    .removeEventListener(courseListener)
            prevActiveId = activeTerm?.id
        } catch (e: Exception) { }
    }

    private fun makeNameChangeListener(course: Course): ValueEventListener {
        return object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String>()
                if (value != null) {
                    val index = courseList.indexOf(course)
                    adapter.notifyItemChanged(index)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read database.", error.toException())
            }
        }
    }

    private fun setNameChangeListener(course: Course) {
        if (nameListeners.get(course.id) == null) {
            val listener = makeNameChangeListener(course)
            course.db.child("name").addValueEventListener(listener)
            nameListeners.put(course.id, listener)
        }
    }

    private fun removeNameChangeListener(course: Course) {
        val listener = nameListeners.get(course.id)
        listener?.let { course.db.child("name").removeEventListener(it) }
        nameListeners.remove(course.id)
    }

    private fun getCourseNextEvent(course: Course): Event? {
        return course.events.values.minBy { it.start }
    }

    private fun getDateDisplayString(month: Int, day: Int): String {
        val monthString =
            if (month < 10) "0${month}"
            else month.toString()
        val dayString =
            if (day < 10) "0${day}"
            else day.toString()
        return "$monthString/$dayString"
    }

    private fun getTimeDisplayString(hour: Int, minute: Int): String {
        val hourDigit = if (hour < 13) hour else hour-12
        val hourString = if (hourDigit == 0) "12" else hourDigit.toString()
        val minString = if (minute < 10) "0$minute" else minute.toString()
        val timeSuffix = if (hour < 12) "AM" else "PM"
        return "$hourString:$minString $timeSuffix"
    }

    inner class CoursesAdapter (private val courses: List<Course>)
        : RecyclerView.Adapter<CoursesAdapter.ViewHolder>()
    {
        inner class ViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView), View.OnClickListener
        {
            lateinit var course: Course
            val courseName: TextView = itemView.findViewById(R.id.course_item_name)
            val eventName: TextView = itemView.findViewById(R.id.course_item_event_name)
            val eventDate: TextView = itemView.findViewById(R.id.course_item_event_date)

            init {
                itemView.setOnClickListener(this)
            }

            override fun onClick(v: View?) {
                fragment.makeCourseViewFragment(course)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val context = parent.context
            val inflater = LayoutInflater.from(context)
            val courseItem = inflater.inflate(R.layout.manager_course_list_item, parent, false)
            return ViewHolder(courseItem)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            val course: Course = courses.get(position)
            val event: Event? = getCourseNextEvent(course)

            // Set Course Item Values
            viewHolder.course = course
            viewHolder.courseName.text = course.name
            if (event != null) {
                val nameString = "Upcoming: ${event.name}"
                val dayOfWeek =
                    if (event.start.dayOfWeek == DayOfWeek.THURSDAY) "R"
                    else event.start.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.US)
                val dateString =
                    getDateDisplayString(event.start.monthValue, event.start.dayOfMonth)
                val timeString =
                    getTimeDisplayString(event.start.hour, event.start.minute)
                val displayString = "$dayOfWeek $dateString  $timeString"

                viewHolder.eventName.text = nameString
                viewHolder.eventDate.text = displayString
            } else
                viewHolder.eventName.text = "No upcoming events or deadlines"
        }

        // Returns the total count of items in the list
        override fun getItemCount(): Int {
            return courses.size
        }
    }
}

