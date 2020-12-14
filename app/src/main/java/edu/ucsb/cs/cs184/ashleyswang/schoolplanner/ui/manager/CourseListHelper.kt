package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.courses

import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Course
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Term

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

    private var courseLayout: ConstraintLayout = model.view.findViewById(R.id.courses_main_course_layout)
    private var courseView: RecyclerView = model.view.findViewById(R.id.courses_main_course_view)

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
}

