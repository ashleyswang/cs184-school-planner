package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.MainActivity
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Controller
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Term

class ManagerFragment : Fragment() {

    val TAG: String = "ManagerFragment"

    val TERM_REQUEST = 0
    val COURSE_REQUEST = 1

    val ACTION_ADD: Int = 0
    val ACTION_EDIT: Int = 1
    val ACTION_DEL: Int = 2

    lateinit var model: ManagerViewModel

    private var controller: Controller
        get() { return model.controller }
        set(value) {
            model.controller = value
        }
    private var activeTerm: Term?
        get() { return model.activeTerm.value }
        set(value) {
            model.activeTerm.value = value
        }

    lateinit var mainHelper: MainLayoutHelper
    lateinit var toolbarHelper: ToolbarHelper
    lateinit var navViewHelper: NavViewHelper
    lateinit var defaultHelper: DefaultLayoutHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        model = ViewModelProvider(requireActivity()).get(ManagerViewModel::class.java)
        model.view = inflater.inflate(R.layout.fragment_manager, container, false)
        model.mainLayout = model.view.findViewById(R.id.courses_main_layout)
        model.defaultLayout = model.view.findViewById(R.id.courses_default_layout)
        model.toolbar = model.view.findViewById(R.id.terms_toolbar)
        model.drawer = model.view.findViewById(R.id.terms_toolbar_drawer_layout)
        model.navView = model.view.findViewById(R.id.terms_toolbar_nav_view)

        // Initialize Toolbar
        setHasOptionsMenu(true)
        (activity as MainActivity).setSupportActionBar(model.toolbar)

        // Initialize Database View Model Info
        controller = (activity as MainActivity).controller
        if (activeTerm == null) getDefaultTerm()

        return model.view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        requireActivity().menuInflater.inflate(R.menu.terms_toolbar_menu, menu)
        // Initialize Helpers
        navViewHelper = NavViewHelper(this, model)
        defaultHelper = DefaultLayoutHelper(this, model)
        toolbarHelper = ToolbarHelper(this, model)
        mainHelper = MainLayoutHelper(this, model)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == TERM_REQUEST)
                when (data.getIntExtra("action", -1)) {
                    ACTION_ADD -> {
                        val termId = data.getStringExtra("termId")
                        activeTerm = controller.terms[termId]!!
                    }
                    ACTION_DEL -> {
                        getDefaultTerm()
                        model.drawer.open()
                    }
                }
        }
    }

    fun openTermEditor() {
        if (activeTerm != null) {
            val intent = Intent(context, TermFormActivity::class.java)
            intent.putExtra("userId", controller.user)
            intent.putExtra("termId", activeTerm!!.id)
            intent.putExtra("termName", activeTerm!!.name)
            intent.putExtra("termStart", activeTerm!!.start.toString())
            intent.putExtra("termEnd", activeTerm!!.end.toString())
            intent.putExtra("default", controller.default == activeTerm!!.id)
            this.startActivityForResult(intent, TERM_REQUEST)
        }
    }

    fun openTermAdder() {
        val intent = Intent(context, TermFormActivity::class.java)
        intent.putExtra("userId", controller.user)
        this.startActivityForResult(intent, TERM_REQUEST)
    }

//    fun openCourseEditor() {
//        val intent = Intent(context, CourseFormActivity::class.java)
//        intent.putExtra("userId", controller.user)
//        intent.putExtra("termId", activeTerm!!.id)
//        intent.putExtra("termName", activeTerm!!.name)
//        intent.putExtra("termStart", activeTerm!!.start.toString())
//        intent.putExtra("termEnd", activeTerm!!.end.toString())
//        intent.putExtra("default", controller.default == activeTerm!!.id)
//        this.startActivityForResult(intent, EDIT_TERM)
//    }

    fun openCourseAdder() {
        val intent = Intent(context, CourseFormActivity::class.java)
        intent.putExtra("userId", controller.user)
        intent.putExtra("termId", activeTerm!!.id)
        this.startActivityForResult(intent, COURSE_REQUEST)
    }

    fun getDefaultTerm() {
        if (controller.default != null)
            this.activeTerm = controller.terms[controller.default!!]!!
        else if (model.termsList.isNotEmpty())
            this.activeTerm = model.termsList.last()
        else
            this.activeTerm = null
    }
}