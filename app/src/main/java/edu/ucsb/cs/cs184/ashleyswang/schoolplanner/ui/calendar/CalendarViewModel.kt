package edu.ucsb.cs.cs184.ashleyswang.schoolplanner.ui.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Event
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Term

class CalendarViewModel : ViewModel() {
    var eventsList: ArrayList<Event> = arrayListOf<Event>()
//    private val _text = MutableLiveData<String>().apply {
//        value = "This is calendar Fragment"
//    }
//    val text: LiveData<String> = _text
}