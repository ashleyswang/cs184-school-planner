package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Course
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Term
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.Event
import java.time.LocalDateTime

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
//    private var prevActiveId: String? = null

    init {
        notifyTermChange()

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

    }

    private fun getCourseNextEvent(course: Course): Event {
        val eventList = arrayListOf<Event>()
        eventList.addAll(course.events.values)

    }

    inner class ContactsAdapter (private val courses: List<Course>)
        : RecyclerView.Adapter<ContactsAdapter.ViewHolder>()
    {
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val courseName: TextView = itemView.findViewById(R.id.course_item_name)
            val eventName: TextView = itemView.findViewById(R.id.course_item_event_name)
            val eventDate: TextView = itemView.findViewById(R.id.course_item_event_date)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsAdapter.ViewHolder {
            val context = parent.context
            val inflater = LayoutInflater.from(context)
            val courseItem = inflater.inflate(R.layout.manager_course_list_item, parent, false)
            return ViewHolder(courseItem)
        }

        override fun onBindViewHolder(viewHolder: ContactsAdapter.ViewHolder, position: Int) {
            val course: Course = courses.get(position)
            val event: Event = getCourseNextEvent(course)

            // Set Course Item Values
            viewHolder.courseName.text = course.name
            viewHolder.eventName.text = event.name
            viewHolder.eventDate

            textView.setText(contact.name)
            val button = viewHolder.messageButton
            button.text = if (contact.isOnline) "Message" else "Offline"
            button.isEnabled = contact.isOnline
        }

        // Returns the total count of items in the list
        override fun getItemCount(): Int {
            return mContacts.size
        }
    }
}

