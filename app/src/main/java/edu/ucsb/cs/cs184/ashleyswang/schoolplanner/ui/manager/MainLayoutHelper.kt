package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Course
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Term
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Event

class MainLayoutHelper(
    private val fragment: ManagerFragment,
    private val model: ManagerViewModel
) {
    private val TAG: String = "MainLayoutHelper"

    private val activeTerm: Term?
        get() { return model.activeTerm.value }
    private var courseList: ArrayList<Course>
        get() { return model.courseList }
        set(value) {
            model.courseList = value
        }
    private var eventsList: ArrayList<Event>
        get() { return model.eventsList }
        set(value) {
            model.eventsList = value
        }

    private var tabLayout: TabLayout = model.view.findViewById(R.id.terms_tab_layout)
    private var courseLayout: ConstraintLayout = model.view.findViewById(R.id.manager_main_course_layout)
    private var eventsLayout: ConstraintLayout = model.view.findViewById(R.id.manager_main_events_layout)

    private var courseListHelper: CourseListHelper = CourseListHelper(fragment, model)
    private var eventsListHelper: EventsListHelper = EventsListHelper(fragment, model)

    init {
        setTabEventListeners()
        getModelState()
    }

    private fun changeToCourseView() {
        eventsLayout.visibility = View.GONE
        courseLayout.visibility = View.VISIBLE
    }

    private fun changeToEventsView() {
        courseLayout.visibility = View.GONE
        eventsLayout.visibility = View.VISIBLE
    }

    private fun setTabEventListeners() {
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.text) {
                    "Courses" -> changeToCourseView()
                    "Events" -> changeToEventsView()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun getModelState() {
        if (model.tabStateTerm != activeTerm) {
            model.tabStateTerm = activeTerm
            model.inEventsView = false
        }

        if (model.inEventsView) changeToEventsView()
    }
}