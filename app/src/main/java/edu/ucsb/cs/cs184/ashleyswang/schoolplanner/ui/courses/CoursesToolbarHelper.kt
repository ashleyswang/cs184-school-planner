package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.courses

import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Controller
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Term
import java.lang.Exception

class CoursesToolbarHelper(
    private val fragment: CoursesFragment,
    private val toolbar: Toolbar
) {
    private val TAG: String = "CoursesToolbarHelper"
    private val default: Boolean
        get() { return activeTerm.id == controller.default }
    private val activeTerm: Term
        get() { return fragment.activeTerm }
    private val controller: Controller
        get() { return fragment.controller}
    private val view: View
        get() { return fragment.root }

    private lateinit var dbEventListener: ValueEventListener
    private var drawer: DrawerLayout

    init {
        drawer = view.findViewById(R.id.terms_toolbar_drawer_layout)
        toolbar.overflowIcon = fragment.resources.getDrawable(R.drawable.ic_more_vert_white_24)
        setToolbarListeners()
    }

    fun notifyTermChange(init: Boolean = false) {
        if (!init) removeDatabaseListeners()
        makeDatabaseListener()
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
        toolbar.title = activeTerm.name
        updateDefaultIcon()
    }

    // Updates Database Default Term & Toolbar UI
    private fun changeDefaultTerm() {
        controller.default = if (default) null else activeTerm.id
        updateDefaultIcon()
    }

    // Updates Toolbar Pinned UI
    private fun updateDefaultIcon() {
        val item: MenuItem = toolbar.menu.findItem(R.id.terms_toolbar_mark_fav)
        item.icon =
            if (default) fragment.resources.getDrawable(R.drawable.ic_pin_white_24px)
            else fragment.resources.getDrawable(R.drawable.ic_pin_outline_24px)
    }

    // Make Database Listener
    private fun makeDatabaseListener() {
        dbEventListener = object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                initToolbar()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read database.", error.toException())
            }
        }
    }

    // Set Listeners for Database Changes (term.name & controller.default)
    private fun setDatabaseListeners() {
        activeTerm.db.child("name").addValueEventListener(dbEventListener)
        controller.db.child("default").addValueEventListener(dbEventListener)
    }

    // Remove Listeners on Term Change
    private fun removeDatabaseListeners() {
        try {
            activeTerm.db.child("name").removeEventListener(dbEventListener)
            controller.db.child("default").removeEventListener(dbEventListener)
        } catch (e: Exception) {

        }
    }
}