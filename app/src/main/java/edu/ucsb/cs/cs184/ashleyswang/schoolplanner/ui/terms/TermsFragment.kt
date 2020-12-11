package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.terms

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.view.menu.ListMenuItemView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.R
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Term


class TermsFragment : Fragment() {

    private lateinit var dashboardViewModel: TermsViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProviders.of(this).get(TermsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_terms, container, false)
        val textView: TextView = root.findViewById(R.id.text_dashboard)

        var terms: ArrayList<Term> = ArrayList()
        var termsList: ListView = root.findViewById(R.id.terms_list)
        var arrayAdapter: ArrayAdapter<Term> = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, terms)
        termsList.adapter = arrayAdapter

        val fab: FloatingActionButton = root.findViewById(R.id.add_term)
        fab.setImageResource(R.drawable.ic_add_24px)
        fab.setOnClickListener {
            var newTerm = Term("Fall 2020")
            terms.add(newTerm)
            arrayAdapter.notifyDataSetChanged()

            Log.d("terms", "terms array: "+terms.toString())
            Log.d("terms", "term[0] id: "+terms[0].id)
        }

        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}