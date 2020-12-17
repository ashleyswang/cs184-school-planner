package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.helpers

import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Term
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.ManagerFragment
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.ManagerViewModel

class DefaultLayoutHelper(
    private val fragment: ManagerFragment,
    private val model: ManagerViewModel
) {
    private val mainLayout: ConstraintLayout
        get() { return model.mainLayout }
    private val defaultLayout: ConstraintLayout
        get() { return model.defaultLayout }
    private val toolbar: Toolbar
        get() { return model.toolbar }
    private val drawer: DrawerLayout
        get() { return model.drawer }

    private var isDefaultView: Boolean = false

    init {
        val defaultAdd: Button = defaultLayout.findViewById(R.id.manager_default_button)
        defaultAdd.setOnClickListener {
            fragment.openTermAdder()
            drawer.close()
        }

        val activeTermObserver: Observer<Term> = Observer<Term> {
            if (model.activeTerm.value == null)
                makeDefaultView()
            else if (isDefaultView && model.activeTerm.value != null)
                hideDefaultView()
        }
        model.activeTerm.observe(fragment.viewLifecycleOwner, activeTermObserver)
    }

    private fun makeDefaultView() {
        Log.d("DefaultLayoutHelper", "in Make Default View")
        isDefaultView = true

        mainLayout.visibility = View.GONE
        defaultLayout.visibility = View.VISIBLE

        toolbar.title = ""
        try {
            var item = toolbar.menu.findItem(R.id.terms_toolbar_mark_fav)
            item.isVisible = false
            item = toolbar.menu.findItem(R.id.terms_toolbar_edit_term)
            item.isVisible = false
        } catch (e: Exception) { }
    }

    private fun hideDefaultView() {
        var item = toolbar.menu.findItem(R.id.terms_toolbar_mark_fav)
        item.isVisible = true
        item = toolbar.menu.findItem(R.id.terms_toolbar_edit_term)
        item.isVisible = true

        defaultLayout.visibility = View.GONE
        mainLayout.visibility = View.VISIBLE

        isDefaultView = false
    }
}