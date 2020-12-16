package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.course

import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.navigation.NavigationView
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.*

class CourseViewViewModel : ViewModel() {
    lateinit var controller: Controller
    lateinit var course: Course
    lateinit var view: View

    // XML Component Information
    lateinit var toolbar: Toolbar
    lateinit var meetLayout: ConstraintLayout
    lateinit var assignLayout: ConstraintLayout
    lateinit var eventsLayout: ConstraintLayout

    // Term/DB Information
    var meetList: ArrayList<Meeting> = arrayListOf<Meeting>()
    var assignList: ArrayList<Assignment> = arrayListOf<Assignment>()
    var eventsList: ArrayList<Event> = arrayListOf<Event>()

    // Other UI States
    var tabStateTerm: Term? = null
    var inEventsView: Boolean = false
}