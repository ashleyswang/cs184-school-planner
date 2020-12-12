package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.terms

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Controller
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Term

class TermsFragment : Fragment() {

    private lateinit var dashboardViewModel: TermsViewModel
    private lateinit var intent: Intent
    private var fabClicked: Boolean = false
    val STATIC_RESULT = 2 //positive > 0 integer.

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProviders.of(this).get(TermsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_terms, container, false)
        val formView = inflater.inflate(R.layout.activity_term_form, container, false)
        val textView: TextView = root.findViewById(R.id.text_dashboard)
        val control = Controller("test")
        fabClicked = false
        Log.d("why", "created")

        var terms: ArrayList<Term> = ArrayList()
        var termsList: ListView = root.findViewById(R.id.terms_list)
        var arrayAdapter: ArrayAdapter<Term> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            terms
        )
        termsList.adapter = arrayAdapter

        val fab: FloatingActionButton = root.findViewById(R.id.add_term)
        fab.setImageResource(R.drawable.ic_add_24px)
        fab.setOnClickListener {
            fabClicked = true
            Log.d("why", "fab clicked")
            //var term: Term = control.addTerm()

            //define a new Intent for the second Activity
            intent = Intent(context, TermFormActivity::class.java)

            //start the second Activity
            //this.startActivity(intent)
            startActivityForResult(intent, STATIC_RESULT);

            Log.d("hello", "in terms fragment, in fab")

//            term.name = "Fall 2020"
//            term.start = LocalDateTime.of(2020, 10, 1, 0, 0, 0)
//            term.end = LocalDateTime.of(2020, 12, 17, 0, 0, 0)
//            terms.add(term)
//            arrayAdapter.notifyDataSetChanged()
////
//            Log.d("terms", "terms array: " + terms.toString())
//            Log.d("terms", "term[0] id: " + terms[0].name)
        }

        Log.d("hello", "in terms fragment, out of fab")

        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onPause() {
        super.onPause()
        Log.d("hello", "terms fragment paused")
    }

    override fun onResume() {
        super.onResume()
        Log.d("hello", "terms fragment resumed")

//        if (fabClicked) {
//            val finished = intent?.getBooleanExtra("finished", false)
//            Log.d("hello", "finished: "+finished.toString())
//            val test = intent.getStringExtra("title")
//            Log.d("hello", "title is: "+test)
//            fabClicked = false
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("why", "am i here????")

        if (requestCode == STATIC_RESULT) //check if the request code is the one you've sent
        {
            if (resultCode == Activity.RESULT_OK)
            {
                // this is successful mission, do with it.
                Log.d("form", "form submitted")

            } else {
            // the result code is different from the one you've finished with, do something else.
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }
}