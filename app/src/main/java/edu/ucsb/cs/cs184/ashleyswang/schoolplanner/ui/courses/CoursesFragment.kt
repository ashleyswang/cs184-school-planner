package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.courses

import android.content.Intent
import android.graphics.drawable.Drawable
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.MainActivity
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Controller
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Course
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Term
import java.util.*
import kotlin.collections.ArrayList

// consider what happens during a db update
class CoursesFragment : Fragment() {

    var TAG: String = "CoursesFragment"
    var user: String = "test"

    private lateinit var root: View
    private lateinit var mainLayout: ConstraintLayout
    private lateinit var defaultLayout: ConstraintLayout
    private lateinit var toolbar: Toolbar
    private lateinit var controller: Controller
    private lateinit var term: Term
    private lateinit var termsList: ArrayList<Term>
    private lateinit var courseList: ArrayList<Course>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_courses, container, false)
        mainLayout = root.findViewById(R.id.courses_main_layout)
        defaultLayout = root.findViewById(R.id.courses_default_layout)
        controller = (activity as MainActivity).controller

        setHasOptionsMenu(true)
        toolbar = root.findViewById(R.id.terms_toolbar)
        (activity as MainActivity).setSupportActionBar(toolbar)
        val drawable: Drawable = resources.getDrawable(R.drawable.ic_more_vert_white_24)
        toolbar.overflowIcon = drawable

        // Data needed to create dynamic lists for toolbar/drawer
        getTermsList()
        getDefaultTerm()
        getCourseList()

        // Update lists with database data

        initNavMenu()

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        requireActivity().menuInflater.inflate(R.menu.terms_toolbar_menu, menu)
        initToolbar()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("Options", "${item.title} clicked")
        return true
    }

    private fun initToolbar() {
        toolbar.title = term.name
        if (term.id == controller.currId) {
            val item: MenuItem = toolbar.menu.findItem(R.id.terms_toolbar_mark_fav)
            item.icon = resources.getDrawable(R.drawable.ic_pin_24px)
        }

        toolbar.setNavigationOnClickListener { v: View ->
            val drawer: DrawerLayout = root.findViewById(R.id.terms_toolbar_drawer_layout)
            drawer.open()
        }
    }

    private fun initNavMenu() {
        val navView: NavigationView = root.findViewById(R.id.terms_toolbar_nav_view)
        val menu: Menu = navView.menu

        val currTerm: MenuItem = menu.findItem(R.id.terms_toolbar_curr_term)
        currTerm.title = term.name

        val prevTerm: MenuItem = menu.findItem(R.id.terms_toolbar_prev_header)
        val prevSubMenu: SubMenu = prevTerm.subMenu
        prevSubMenu.clear()
        for (t in termsList.reversed()) {
            if (t.id != controller.currId) {
                val item = prevSubMenu.add(prevTerm.groupId, Menu.NONE, Menu.NONE, t.name)
                item.icon = resources.getDrawable(R.drawable.ic_arrow_right_24)
            }
        }
    }

    private fun getDefaultTerm() {
        val currTermId = controller.currId
        if (currTermId != null) term = controller.terms[currTermId]!!
        else term = termsList[0]
    }

    private fun getTermsList() {
        termsList = arrayListOf<Term>()
        if (controller.terms.size == 0) {
            var defaultTerm = controller.addTerm()
            defaultTerm.name = "Default Term"
        }
        termsList.addAll(controller.terms.values)
        termsList.sortBy { it.start.toString() }
    }

    private fun getCourseList() {
        courseList = arrayListOf<Course>()
        courseList.addAll(term.courses.values)
        courseList.sortBy { it.name }
    }

    private fun makeTermDropdown() {
    }

    private fun makeCourseList() {
    }


}