package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.courses

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.MainActivity
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Controller
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Course
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Term
import kotlin.collections.ArrayList

// consider what happens during a db update
// ade edge case for null terms
class CoursesFragment : Fragment() {

    val TAG: String = "CoursesFragment"
    val ADD_TERM: Int = 0
    val EDIT_TERM: Int = 1
    val DEL_TERM: Int = 2

    lateinit var root: View
    lateinit var mainLayout: ConstraintLayout
    lateinit var defaultLayout: ConstraintLayout

    lateinit var controller: Controller
    lateinit var activeTerm: Term
    lateinit var courseList: ArrayList<Course>

    lateinit var toolbarHelper: CoursesToolbarHelper
    lateinit var navViewHelper: CoursesNavViewHelper
    lateinit var defaultHelper: CoursesDefaultHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_courses, container, false)
        mainLayout = root.findViewById(R.id.courses_main_layout)
        defaultLayout = root.findViewById(R.id.courses_default_layout)
        controller = (activity as MainActivity).controller

        // Initialize Toolbar
        setHasOptionsMenu(true)
        val toolbar: Toolbar = root.findViewById(R.id.terms_toolbar)
        (activity as MainActivity).setSupportActionBar(toolbar)
        toolbarHelper = CoursesToolbarHelper(this, toolbar)

        // Get Empty Terms View Helper
        val drawer: DrawerLayout = root.findViewById(R.id.terms_toolbar_drawer_layout)
        defaultHelper = CoursesDefaultHelper(this, mainLayout, defaultLayout, toolbar, drawer)

        // Update lists with database data
        val navView: NavigationView = root.findViewById(R.id.terms_toolbar_nav_view)
        navViewHelper = CoursesNavViewHelper(this, navView)

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        requireActivity().menuInflater.inflate(R.menu.terms_toolbar_menu, menu)
        changeActiveTerm(null, true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val actionCode = data.getIntExtra("action", -1)
            when (actionCode) {
                ADD_TERM -> {
                    val termId = data.getStringExtra("termId")
                    val init = (navViewHelper.getTermsList().size == 1)
                    changeActiveTerm(controller.terms[termId]!!, init)
                }
                DEL_TERM -> {
                    Log.d(TAG, "Deleted Term")
                    changeActiveTerm()
                    defaultHelper.openDrawer()
                }
            }
        }
    }

    fun openTermEditor() {
        val intent = Intent(context, TermFormActivity::class.java)
        intent.putExtra("userId", controller.user)
        intent.putExtra("termId", activeTerm.id)
        intent.putExtra("termName", activeTerm.name)
        intent.putExtra("termStart", activeTerm.start.toString())
        intent.putExtra("termEnd", activeTerm.end.toString())
        intent.putExtra("default", controller.default == activeTerm.id)
        this.startActivityForResult(intent, EDIT_TERM)
    }

    fun openTermAdder() {
        val intent = Intent(context, TermFormActivity::class.java)
        intent.putExtra("userId", controller.user)
        this.startActivityForResult(intent, ADD_TERM)
    }

    fun changeActiveTerm(term: Term? = null, init: Boolean = false) {
        var setDefaultView: Boolean = false
        if (term != null) this.activeTerm = term
        else setDefaultView = !getDefaultTerm()

        if (setDefaultView)
            defaultHelper.makeDefaultView()
        else {
            if (defaultHelper.isDefaultView) defaultHelper.hideDefaultView()
            toolbarHelper.notifyTermChange(init)
            // course view layout helper
        }
    }

    private fun getCourseList() {
        courseList = arrayListOf<Course>()
        courseList.addAll(activeTerm.courses.values)
        courseList.sortBy { it.name }
    }

    private fun getDefaultTerm(): Boolean {
        val termsList = navViewHelper.getTermsList()
        if (controller.default != null) {
            Log.d(TAG, "Default is not null")
            this.activeTerm = controller.terms[controller.default!!]!!
            return true
        } else if (termsList.isNotEmpty()) {
            Log.d(TAG, "termsList has size ${termsList.size}")
            this.activeTerm = termsList.last()
            return true
        }
        Log.d(TAG, "return false")
        return false
    }
}