package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.courses

import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Controller
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Term
import kotlin.collections.ArrayList

class CoursesNavViewHelper (
    private val fragment: CoursesFragment,
    private val navView: NavigationView
) {
    private val TAG: String = "CoursesNavViewHelper"
    private var menu: Menu = navView.menu

    private val defaultTerm: Term?
        get() { return fragment.controller.terms[controller.default] }
    private val controller: Controller
        get() { return fragment.controller}
    private val view: View
        get() { return fragment.root }

    private lateinit var termsList: ArrayList<Term>
    private lateinit var menuItems: MutableMap<String, MenuItem>
    private lateinit var defaultListener: ValueEventListener
    private lateinit var termsListener: ValueEventListener
    private var nameListeners: MutableMap<String, ValueEventListener> = mutableMapOf()

    init {
        makeTermsList()
        makeTermsMenu()
        makeStaticDbListeners()
        setStaticDbListeners()
        setMenuInitListener()
        for (term in termsList)
            setNameChangeListener(term)
    }

    fun getTermsList(): ArrayList<Term> {
        return termsList
    }

    private fun makeTermsList() {
        termsList = arrayListOf<Term>()
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
                item.icon = fragment.resources.getDrawable(R.drawable.ic_arrow_right_24)
                setTermClickListener(item, t)
                menuItems.put(t.id, item)
            }
        }
    }

    private fun setMenuInitListener() {
        val addTerm: MenuItem = menu.findItem(R.id.terms_toolbar_add_term)
        addTerm.setOnMenuItemClickListener {
            fragment.openTermAdder()
            val drawer: DrawerLayout = view.findViewById(R.id.terms_toolbar_drawer_layout)
            drawer.close()
            return@setOnMenuItemClickListener true
        }

        val currTerm: MenuItem = menu.findItem(R.id.terms_toolbar_curr_term)
        currTerm.setOnMenuItemClickListener {
            if (it.title != defaultTerm?.name)
                Log.w("TAG", "Tab Title (${it.title}) and Term Name (${defaultTerm?.name}) do not match")
            fragment.changeActiveTerm(defaultTerm!!)
            val drawer: DrawerLayout = view.findViewById(R.id.terms_toolbar_drawer_layout)
            drawer.close()
            return@setOnMenuItemClickListener true
        }
    }

    private fun setTermClickListener(item: MenuItem, term: Term) {
        item.setOnMenuItemClickListener {
            if (it.title != term.name)
                Log.w("TAG", "Tab Title (${it.title}) and Term Name (${term.name}) do not match")
            fragment.changeActiveTerm(term)
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
        }
    }

    // Remove Listeners on Terms List Change
    private fun removeNameChangeListener(term: Term) {
        val listener = nameListeners.get(term.id)
        listener?.let { term.db.child("name").removeEventListener(it) }
    }

}