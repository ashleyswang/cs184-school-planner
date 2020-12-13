package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.courses

import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import kotlinx.android.synthetic.main.fragment_courses.view.*

class CoursesDefaultHelper(
    private val fragment: CoursesFragment,
    private val mainLayout: ConstraintLayout,
    private val defaultLayout: ConstraintLayout,
    private val toolbar: Toolbar,
    private val drawer: DrawerLayout
) {
    val isDefaultView: Boolean
        get() {return _isDefaultView }

    private var _isDefaultView: Boolean = false

    init {
        val defaultAdd: Button = defaultLayout.findViewById(R.id.courses_default_button)
        defaultAdd.setOnClickListener {
            fragment.openTermAdder()
            closeDrawer()
        }
    }

    fun makeDefaultView() {
        Log.d("CoursesDefaultHelper", "in Make Default View")
        _isDefaultView = true

        mainLayout.visibility = View.GONE
        defaultLayout.visibility = View.VISIBLE

        toolbar.title = ""
        var item = toolbar.menu.findItem(R.id.terms_toolbar_mark_fav)
        item.isVisible = false
        item = toolbar.menu.findItem(R.id.terms_toolbar_edit_term)
        item.isVisible = false
    }

    fun hideDefaultView() {
        var item = toolbar.menu.findItem(R.id.terms_toolbar_mark_fav)
        item.isVisible = true
        item = toolbar.menu.findItem(R.id.terms_toolbar_edit_term)
        item.isVisible = true

        defaultLayout.visibility = View.GONE
        mainLayout.visibility = View.VISIBLE

        _isDefaultView = false
    }

    fun openDrawer() {
        drawer.open()
    }

    fun closeDrawer() {
        drawer.close()
    }
}