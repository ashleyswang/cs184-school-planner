package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.deadlines

import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R

class DeadlinesFragment : Fragment() {

    companion object {
        fun newInstance() = DeadlinesFragment()
    }

    private lateinit var viewModel: DeadlinesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_deadlines, container, false)

        val fab: FloatingActionButton = root.findViewById(R.id.add_deadline)
        fab.setImageResource(R.drawable.ic_add_24px)
        fab.setOnClickListener {

            //define a new Intent for the second Activity
            val intent = Intent(context, DeadlineFormActivity::class.java)

            //start the second Activity
            this.startActivity(intent)
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DeadlinesViewModel::class.java)
        // TODO: Use the ViewModel
    }

}