package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.helpers

import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.drawerlayout.widget.DrawerLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Controller
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Term
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.ManagerFragment
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.ManagerViewModel
import kotlin.collections.ArrayList

class NavViewHelper (
    private val fragment: ManagerFragment,
    private val model: ManagerViewModel
) {
    private val TAG: String = "NavViewHelper"

    private val view: View
        get() { return model.view }
    private val menu: Menu
        get() { return model.navView.menu}

    private val controller: Controller
        get() { return model.controller}
    private val defaultTerm: Term?
        get() { return model.controller.terms[controller.default] }
    private var termsList: ArrayList<Term>
        get() { return model.termsList }
        set(value) {
            model.termsList = value
        }

    private lateinit var menuItems: MutableMap<String, MenuItem>
    private lateinit var defaultListener: ValueEventListener
    private lateinit var termsListener: ValueEventListener
    private var nameListeners: MutableMap<String, ValueEventListener> = mutableMapOf()

    init {
        makeTermsList()
        fragment.getDefaultTerm()
        makeTermsMenu()
        makeStaticDbListeners()
        setStaticDbListeners()
        setMenuInitListener()
        for (term in termsList)
            setNameChangeListener(term)
    }

    private fun makeTermsList() {
        termsList.clear()
        termsList.addAll(controller.terms.values)
        termsList.sortBy { it.createdOn.toString() }
    }

    private fun makeTermsMenu() {
        menuItems = mutableMapOf<String, MenuItem>()
        val curr: MenuItem = menu.findItem(R.id.terms_toolbar_curr_term)
        val term: Term? = defaultTerm

        if (term != null) {
            curr.isVisible = true
            curr.title = term.name
            menuItems.put(term.id, curr)
        } else
            curr.isVisible = false

        val prevTerm: MenuItem = menu.findItem(R.id.terms_toolbar_prev_header)
        val prevSubMenu: SubMenu = prevTerm.subMenu
        prevSubMenu.clear()
        for (t in termsList.reversed()) {
            if (t.id != term?.id) {
                val item = prevSubMenu.add(prevTerm.groupId, Menu.NONE, Menu.NONE, t.name)
                item.icon = fragment.getContext()?.let { AppCompatResources.getDrawable(it, R.drawable.ic_check_circle_24) } //race condition here.
                //item.icon = AppCompatResources.getDrawable(fragment.requireContext(), R.drawable.ic_check_circle_24)
                setTermClickListener(item, t)
                menuItems.put(t.id, item)
            }
        }
    }

    private fun setMenuInitListener() {
        val addTerm: MenuItem = menu.findItem(R.id.terms_toolbar_add_term)
        addTerm.setOnMenuItemClickListener {
            fragment.openTermAdder()
            model.drawer.close()
            return@setOnMenuItemClickListener true
        }

        val currTerm: MenuItem = menu.findItem(R.id.terms_toolbar_curr_term)
        currTerm.setOnMenuItemClickListener {
            Log.w(TAG, "menu click")
            model.activeTerm.value = defaultTerm!!
            model.drawer.close()
            return@setOnMenuItemClickListener true
        }
    }

    private fun setTermClickListener(item: MenuItem, term: Term) {
        item.setOnMenuItemClickListener {
            Log.w(TAG, "item click")
            model.activeTerm.value = term
            val drawer: DrawerLayout = view.findViewById(R.id.terms_toolbar_drawer_layout)
            drawer.close()
            return@setOnMenuItemClickListener true
        }
    }

    private fun makeStaticDbListeners() {
        // Changes to Default Term
        defaultListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                makeTermsMenu()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read database.", error.toException())
            }
        }

        // Changes to Terms
        termsListener = object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val oldTerms = termsList
                makeTermsList()

                val removedTerms = oldTerms.minus(termsList)
                for (t in removedTerms)
                    removeNameChangeListener(t)

                makeTermsMenu()
                val addedTerms = termsList.minus(oldTerms)
                for (t in addedTerms)
                    setNameChangeListener(t)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read database.", error.toException())
            }
        }
    }

    private fun setStaticDbListeners() {
        controller.db.child("default").addValueEventListener(defaultListener)
        controller.db.child("terms").addValueEventListener(termsListener)
    }

    private fun removeStaticDbListeners() {
        controller.db.child("default").removeEventListener(defaultListener)
        controller.db.child("terms").addValueEventListener(termsListener)
    }

    private fun makeNameChangeListener(term: Term): ValueEventListener {
        return object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<String>()
                if (value != null) {
                    val item = menuItems[term.id]!!
                    item.title = value
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read database.", error.toException())
            }
        }
    }

    // Set Listeners for Database Changes (term.name & controller.default & controller.terms)
    private fun setNameChangeListener(term: Term) {
        if (nameListeners.get(term.id) == null) {
            val listener = makeNameChangeListener(term)
            term.db.child("name").addValueEventListener(listener)
            nameListeners.put(term.id, listener)
        }
    }

    // Remove Listeners on Terms List Change
    private fun removeNameChangeListener(term: Term) {
        val listener = nameListeners.get(term.id)
        listener?.let { term.db.child("name").removeEventListener(it) }
        nameListeners.remove(term.id)
    }

}