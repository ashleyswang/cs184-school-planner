package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.course.helpers

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Assignment
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Course
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.course.CourseViewFragment
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.course.CourseViewViewModel
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.forms.AssignmentFormActivity
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.*


class AssignmentListHelper(
    private val fragment: CourseViewFragment,
    private val model: CourseViewViewModel
) {
    private val TAG: String = "AssignmentListHelper"

    private var course: Course
        get() { return model.course }
        set(value) {
            model.course = value
        }
    private var assignList: ArrayList<Assignment>
        get() { return model.assignList }
        set(value) {
            model.assignList = value
        }

    private var listView: RecyclerView = model.view.findViewById(R.id.manager_course_assign_list)

    private var itemListeners: MutableMap<String, ValueEventListener> = mutableMapOf()
    private lateinit var adapter: AssignmentAdapter

    init {
        makeListViewAdapter()
        setDatabaseListeners()
        setFabTouchListener()
        makeAssignmentList()
    }

    private fun makeListViewAdapter() {
        adapter = AssignmentAdapter(assignList)
        listView.adapter = adapter
        listView.layoutManager = LinearLayoutManager(fragment.requireContext())
    }

    private fun makeAssignmentList() {
        assignList.clear()
        assignList.addAll(course.assign.values)
        assignList.sortBy { it.event.start }
        adapter.notifyDataSetChanged()
    }

    private fun setFabTouchListener() {
        model.view
            .findViewById<FloatingActionButton>(R.id.add_assign)
            .setOnClickListener {
                openAssignmentAdder()
            }
    }

    private fun setDatabaseListeners() {
        course.db.child("assign").addValueEventListener(
            object: ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val oldList = assignList
                    makeAssignmentList()

                    val removedItems = oldList.minus(assignList)
                    for (item in removedItems)
                        removeItemChangeListener(item)

                    val addedItems = assignList.minus(oldList)
                    for (item in addedItems)
                        setItemChangeListener(item)
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Failed to read database.", error.toException())
                }
            })
    }

    private fun makeItemChangeListener(assign: Assignment): ValueEventListener {
        return object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<Any>()
                if (value != null) {
                    val index = assignList.indexOf(assign)
                    adapter.notifyItemChanged(index)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read database.", error.toException())
            }
        }
    }

    private fun setItemChangeListener(assign: Assignment) {
        if (itemListeners.get(assign.id) == null) {
            val listener = makeItemChangeListener(assign)
            assign.db.child("completed").addValueEventListener(listener)
            assign.event.db.addValueEventListener(listener)
            itemListeners.put(assign.id, listener)
        }
    }

    private fun removeItemChangeListener(assign: Assignment) {
        val listener = itemListeners.get(assign.id)
        listener?.let {
            assign.db.child("completed").removeEventListener(it)
            assign.event.db.removeEventListener(it)
        }
        itemListeners.remove(assign.id)
    }

    private fun openAssignmentAdder() {
        val intent = Intent(fragment.context, AssignmentFormActivity::class.java)
        intent.putExtra("userId", model.controller.user)
        intent.putExtra("termId", course.term.id)
        intent.putExtra("courseId", course.id)
        fragment.startActivity(intent)
    }

    private fun openAssignmentEditor(assign: Assignment) {
        val intent = Intent(fragment.context, AssignmentFormActivity::class.java)
        intent.putExtra("userId", model.controller.user)
        intent.putExtra("termId", course.term.id)
        intent.putExtra("courseId", course.id)
        intent.putExtra("assignId", assign.id)
        intent.putExtra("assignName", assign.name)
        intent.putExtra("assignDue", assign.event.start.toString())
        intent.putExtra("descript", assign.descript)
        fragment.startActivity(intent)
    }

    private fun getDateDisplayString(datetime: LocalDateTime): String {
        val dayOfWeek =
            if (datetime.dayOfWeek == DayOfWeek.THURSDAY) "R"
            else datetime.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.US)

        return "$dayOfWeek ${datetime.monthValue}/${datetime.dayOfMonth}"
    }

    private fun getTimeDisplayString(datetime: LocalDateTime): String {
        val hourDigit =
            if (datetime.hour < 13) datetime.hour
            else datetime.hour-12
        val hourString = if (hourDigit == 0) "12" else hourDigit.toString()
        val minString =
            if (datetime.minute < 10) "0${datetime.minute}"
            else datetime.minute.toString()
        val timeSuffix = if (datetime.hour < 12) "AM" else "PM"

        return "$hourString:$minString $timeSuffix"
    }

    inner class AssignmentAdapter (private val assignments: List<Assignment>)
        : RecyclerView.Adapter<AssignmentAdapter.ViewHolder>()
    {
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
        {
            lateinit var assign: Assignment
            val assignName: TextView = itemView.findViewById(R.id.assign_name)
            val assignDate: TextView = itemView.findViewById(R.id.assign_date)
            val assignTime: TextView = itemView.findViewById(R.id.assign_time)
            val assignCompleted: CheckBox = itemView.findViewById(R.id.assign_completed)

            init {
                assignCompleted.setOnClickListener {
                    assign.completed = assignCompleted.isChecked
                }
                itemView.setOnClickListener {
                    openAssignmentEditor(assign)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val context = parent.context
            val inflater = LayoutInflater.from(context)
            val assignItem = inflater.inflate(R.layout.manager_assign_list_item, parent, false)
            return ViewHolder(assignItem)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            val assign: Assignment = assignments.get(position)

            // Set Course Item Values
            viewHolder.assign = assign
            viewHolder.assignName.text = assign.name
            viewHolder.assignDate.text = getDateDisplayString(assign.event.start)
            viewHolder.assignTime.text = getTimeDisplayString(assign.event.start)
            viewHolder.assignCompleted.isChecked = assign.completed
        }

        // Returns the total count of items in the list
        override fun getItemCount(): Int {
            return assignments.size
        }
    }
}

