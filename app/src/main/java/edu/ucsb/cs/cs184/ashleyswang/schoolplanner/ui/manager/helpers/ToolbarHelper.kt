package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.helpers

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
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Term
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.ManagerFragment
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.ManagerViewModel

class ToolbarHelper(
    private val fragment: ManagerFragment,
    private val model: ManagerViewModel
) {
    private val TAG: String = "ToolbarHelper"

    private val view: View
        get() { return model.view }
    private val toolbar: Toolbar
        get() { return model.toolbar }
    private val drawer: DrawerLayout
        get() { return model.drawer }
    private val controller: Controller
        get() { return model.controller }
    private val activeTerm: Term?
        get() { return model.activeTerm.value }
    private val default: Boolean
        get() { return activeTerm?.id == controller.default }

    private var prevActiveId: String? = null
    private var dbEventListener: ValueEventListener = object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                initToolbar()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read database.", error.toException())
            }
        }

    init {
        Log.d(TAG, "init")
        toolbar.overflowIcon = fragment.getContext()?.let { AppCompatResources.getDrawable(it, R.drawable.ic_more_vert_white_24) }
        controller.db.child("default").addValueEventListener(dbEventListener)
        setToolbarListeners()

        val activeTermObserver: Observer<Term> = Observer<Term> {
            if (activeTerm != null) notifyTermChange()
        }
        model.activeTerm.observe(fragment.viewLifecycleOwner, activeTermObserver)
    }

    private fun notifyTermChange() {
        Log.d(TAG, "notify term change")
        removeDatabaseListeners()
        setDatabaseListeners()
        initToolbar()
    }

    // Sets Listeners for Toolbar Button Clicks
    private fun setToolbarListeners() {
        // Set Listener to Open Navigation View
        toolbar.setNavigationOnClickListener { v: View ->
            drawer.open()
        }

        // Set Listener for Checking/Unchecking Default
        toolbar.setOnMenuItemClickListener { item: MenuItem ->
            val id = item.itemId
            when (id) {
                R.id.terms_toolbar_mark_fav  -> changeDefaultTerm()
                R.id.terms_toolbar_edit_term -> fragment.openTermEditor()
            }
            return@setOnMenuItemClickListener true
        }
    }

    // Set Term Name & Default Status
    private fun initToolbar() {
        if (activeTerm != null) {
            toolbar.title = activeTerm!!.name
            updateDefaultIcon()
        }
    }

    // Updates Database Default Term & Toolbar UI
    private fun changeDefaultTerm() {
        controller.default = if (default) null else activeTerm!!.id
        updateDefaultIcon()
    }

    // Updates Toolbar Pinned UI
    private fun updateDefaultIcon() {
        val item: MenuItem? = toolbar.menu.findItem(R.id.terms_toolbar_mark_fav)
        item?.icon =
            if (default) fragment.getContext()?.let { AppCompatResources.getDrawable(it, R.drawable.ic_pin_white_24px) }
            else fragment.getContext()?.let { AppCompatResources.getDrawable(it, R.drawable.ic_pin_outline_24px) }

    }

    // Set Listeners for Database Changes (term.name & controller.default)
    private fun setDatabaseListeners() {
        activeTerm!!.db.child("name").addValueEventListener(dbEventListener)
    }

    // Remove Listeners on Term Change
    private fun removeDatabaseListeners() {
        try {
            if (prevActiveId != null)
                controller.terms[prevActiveId!!]!!.db.child("name")
                    .removeEventListener(dbEventListener)
            prevActiveId = activeTerm?.id
        } catch (e: Exception) { }
    }
}