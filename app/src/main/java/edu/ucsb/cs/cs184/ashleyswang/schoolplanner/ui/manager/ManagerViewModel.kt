package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.courses

import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.navigation.NavigationView
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Controller
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Course
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Term
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.event.Event

class ManagerViewModel : ViewModel() {
    lateinit var controller: Controller
    lateinit var view: View

    // XML Component Information
    lateinit var mainLayout: ConstraintLayout
    lateinit var defaultLayout: ConstraintLayout
    lateinit var toolbar: Toolbar
    lateinit var navView: NavigationView
    lateinit var drawer: DrawerLayout

    // Term/DB Information
    var activeTerm: MutableLiveData<Term> = MutableLiveData<Term>().apply {
        value = null
    }
    var termsList: ArrayList<Term> = arrayListOf<Term>()
    var courseList: ArrayList<Course> = arrayListOf<Course>()
    var eventsList: ArrayList<Event> = arrayListOf<Event>()

    // Other UI States
    var tabStateTerm: Term? = null
    var inEventsView: Boolean = false
}