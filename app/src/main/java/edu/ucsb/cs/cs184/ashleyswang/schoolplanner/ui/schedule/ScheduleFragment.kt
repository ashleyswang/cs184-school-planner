package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.schedule

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.manager.CourseFormActivity

class ScheduleFragment : Fragment() {

    private lateinit var notificationsViewModel: ScheduleViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
            ViewModelProviders.of(this).get(ScheduleViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_schedule, container, false)
        val textView: TextView = root.findViewById(R.id.text_notifications)

        val fab: FloatingActionButton = root.findViewById(R.id.add_course)
        fab.setImageResource(R.drawable.ic_add_24px)
        fab.setOnClickListener {
            //define a new Intent for the second Activity
            val intent = Intent(context, CourseFormActivity::class.java)
            intent.putExtra("userId", "test")

            //start the second Activity
            this.startActivity(intent)
            //startActivityForResult(intent, STATIC_RESULT);

            Log.d("hello", "in terms fragment, in fab")
        }

        notificationsViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}