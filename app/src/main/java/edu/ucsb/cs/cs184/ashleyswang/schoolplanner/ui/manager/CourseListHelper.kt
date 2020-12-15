package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager

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
        val dividerItemDecoration = DividerItemDecoration(
            courseView.context,
            (courseView.layoutManager as LinearLayoutManager).orientation
        )
        courseView.addItemDecoration(dividerItemDecoration)
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

    inner class CoursesAdapter (private val courses: List<Course>)
        : RecyclerView.Adapter<CoursesAdapter.ViewHolder>()
    {
        inner class ViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView), View.OnClickListener
        {
            val courseName: TextView = itemView.findViewById(R.id.course_item_name)
            val eventName: TextView = itemView.findViewById(R.id.course_item_event_name)
            val eventDate: TextView = itemView.findViewById(R.id.course_item_event_date)

            init {
                itemView.setOnClickListener(this)
            }

            override fun onClick(v: View?) {
                Log.d(TAG, "temp on click")
            }
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

