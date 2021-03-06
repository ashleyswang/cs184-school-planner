package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.course

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.MainActivity
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Controller
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Course
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.ManagerFragment
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.course.helpers.AssignmentListHelper
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.course.helpers.CourseToolbarHelper
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.course.helpers.EventListHelper
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.course.helpers.MeetingListHelper
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.forms.CourseFormActivity
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.helpers.ToolbarHelper

class CourseViewFragment(
    val parent: ManagerFragment,
    val course: Course
) : Fragment() {

    val TAG: String = "CourseViewFragment"

    private var controller: Controller
        get() { return model.controller }
        set(value) {
            model.controller = value
        }
    private var root: View
        get() { return model.view }
        set(value) {
            model.view = value
        }
    private var meetLayout: ConstraintLayout
        get() { return model.meetLayout }
        set(value) {
            model.meetLayout = value
        }
    private var assignLayout: ConstraintLayout
        get() { return model.assignLayout }
        set(value) {
            model.assignLayout = value
        }
    private var eventsLayout: ConstraintLayout
        get() { return model.eventsLayout }
        set(value) {
            model.eventsLayout = value
        }

    private lateinit var model: CourseViewViewModel
    private lateinit var tabLayout: TabLayout

    private lateinit var toolbarHelper: CourseToolbarHelper
    private lateinit var meetingHelper: MeetingListHelper
    private lateinit var assignHelper: AssignmentListHelper
    private lateinit var eventsHelper: EventListHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        parent.model.mainLayout.visibility = View.GONE
        model = ViewModelProvider(requireActivity()).get(CourseViewViewModel::class.java)
        root = inflater.inflate(R.layout.fragment_course_view, container, false)
        model.toolbar = root.findViewById(R.id.course_toolbar)
        meetLayout = root.findViewById(R.id.manager_course_meet_layout)
        assignLayout = root.findViewById(R.id.manager_course_assign_layout)
        eventsLayout = root.findViewById(R.id.manager_course_event_layout)
        tabLayout = root.findViewById(R.id.course_view_tab_layout)

        // Initialize Database View Model Info
        controller = (activity as MainActivity).controller
        model.course = course

        // Initialize Toolbar
        setHasOptionsMenu(true)
        (activity as MainActivity).setSupportActionBar(model.toolbar)

        meetingHelper = MeetingListHelper(this, model)
        assignHelper = AssignmentListHelper(this, model)
        eventsHelper = EventListHelper(this, model)
        initializeView()

        return model.view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        requireActivity().menuInflater.inflate(R.menu.course_view_toolbar_menu, menu)

        // Layout Helpers
        toolbarHelper = CourseToolbarHelper(this, model)
        setTabEventListeners()
    }

    private fun setTabEventListeners() {
        tabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    Log.d(TAG, tabLayout.selectedTabPosition.toString())
                    when (tab.text) {
                        "Classes"     -> changeToMeetingView()
                        "Assignments" -> changeToAssignView()
                        "Exams"       -> changeToEventsView()
                    }
                }
                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
    }

    private fun initializeView() {
        when (model.tabState) {
            "meeting" -> changeToMeetingView()
            "assign"  -> changeToAssignView()
            "events"  -> changeToEventsView()
        }
    }

    private fun changeToMeetingView() {
        assignLayout.visibility = View.GONE
        eventsLayout.visibility = View.GONE
        meetLayout.visibility = View.VISIBLE
        model.tabState = "meeting"

        if (tabLayout.selectedTabPosition != 0)
            tabLayout.selectTab(tabLayout.getTabAt(0))
    }

    private fun changeToAssignView() {
        meetLayout.visibility = View.GONE
        eventsLayout.visibility = View.GONE
        assignLayout.visibility = View.VISIBLE
        model.tabState = "assign"

        if (tabLayout.selectedTabPosition != 1)
            tabLayout.selectTab(tabLayout.getTabAt(1))
    }

    private fun changeToEventsView() {
        meetLayout.visibility = View.GONE
        assignLayout.visibility = View.GONE
        eventsLayout.visibility = View.VISIBLE
        model.tabState = "events"

        if (tabLayout.selectedTabPosition != 2)
            tabLayout.selectTab(tabLayout.getTabAt(2))
    }

}