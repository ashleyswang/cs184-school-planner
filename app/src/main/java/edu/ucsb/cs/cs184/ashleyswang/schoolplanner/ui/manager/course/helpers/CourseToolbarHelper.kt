package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.course.helpers

import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Controller
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Course
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Term
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.ManagerFragment
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.ManagerViewModel
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.course.CourseViewFragment
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.course.CourseViewViewModel

class CourseToolbarHelper(
    private val fragment: CourseViewFragment,
    private val model: CourseViewViewModel
) {
    private val TAG: String = "CourseToolbarHelper"

    private val view: View
        get() { return model.view }
    private val toolbar: Toolbar
        get() { return model.toolbar }
    private val controller: Controller
        get() { return model.controller }
    private val course: Course
        get() { return model.course }

    init {
        setToolbarListeners()
        setDatabaseListeners()
        toolbar.title = course.name
    }

    // Sets Listeners for Toolbar Button Clicks
    private fun setToolbarListeners() {
        // Set Listener to return to Term View
        toolbar.setNavigationOnClickListener { v: View ->
            fragment.parent.model.mainLayout.visibility = View.VISIBLE
            fragment.activity?.supportFragmentManager?.popBackStack()
            fragment.parent.model.activeCourse = null
            model.tabState = "meeting"
        }

        // Set Listener for Course Editor
        toolbar.setOnMenuItemClickListener { item: MenuItem ->
            val id = item.itemId
            when (id) {
                R.id.edit_course ->
                    fragment.parent.openCourseEditor(course)
            }
            return@setOnMenuItemClickListener true
        }
    }

    // Set Listeners for Database Changes (term.name & controller.default)
    private fun setDatabaseListeners() {
        course.db.child("name")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    toolbar.title = course.name
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Failed to read database.", error.toException())
                }
            })
    }
}