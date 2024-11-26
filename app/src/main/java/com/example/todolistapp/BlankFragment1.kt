package com.example.todolistapp

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BlankFragment1.newInstance] factory method to
 * create an instance of this fragment.
 */
class BlankFragment1 : Fragment(), CalenderAdapter.OnItemListener{

    private lateinit var monthYearText: TextView
    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var selectedDate: LocalDate

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedDate = LocalDate.now() // Initialize selected date
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setMonthView() {
        val formattedDate = monthYearFromDate(selectedDate)
        monthYearText.text = formattedDate.toString()
        var daysInMonth : ArrayList<String> =  dayInMonthArray(selectedDate)

        val calendarAdapter = CalenderAdapter(daysInMonth, selectedDate,this)
        calendarRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7) // 7 columns
        calendarRecyclerView.adapter = calendarAdapter
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun dayInMonthArray(date: LocalDate): ArrayList<String> {
        val daysInMonthArray = ArrayList<String>()
        val yearMonth = YearMonth.from(date)
        val daysInMonth = yearMonth.lengthOfMonth()

        val firstOfMonth: LocalDate = date.withDayOfMonth(1)
        val dayOfWeek = firstOfMonth.dayOfWeek.value // 1 = Monday, 7 = Sunday

        // Get the last few days of the previous month
        val prevMonth = date.minusMonths(1)
        val prevMonthYearMonth = YearMonth.from(prevMonth)
        val daysInPrevMonth = prevMonthYearMonth.lengthOfMonth()

        // Add previous month's dates
        for (i in (daysInPrevMonth - dayOfWeek + 1)..daysInPrevMonth) {
            daysInMonthArray.add(i.toString())
        }

        // Add current month's dates
        for (i in 1..daysInMonth) {
            daysInMonthArray.add(i.toString())
        }

        // Add next month's dates to fill the calendar grid
        val remainingDays = 42 - daysInMonthArray.size // 42 is for a 6-row grid
        for (i in 1..remainingDays) {
            daysInMonthArray.add(i.toString())
        }

        return daysInMonthArray
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun monthYearFromDate(selectedDate: LocalDate): Any {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        return selectedDate.format(formatter).uppercase()
    }

    private fun initWidgets() {
        calendarRecyclerView = view?.findViewById(R.id.calenderRecycleview)!!
        monthYearText = view?.findViewById(R.id.monthyeartv)!!
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_blank1, container, false)

        // Initialize widgets
        monthYearText = view.findViewById(R.id.monthyeartv)
        calendarRecyclerView = view.findViewById(R.id.calenderRecycleview)

        // Set up button listeners
        view.findViewById<Button>(R.id.premonth).setOnClickListener {
            changeMonth(-1)
        }
        view.findViewById<Button>(R.id.nextmonth).setOnClickListener {
            changeMonth(1)
        }

        // Set the initial month view
        setMonthView()

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun changeMonth(monthOffset: Long) {
        selectedDate = selectedDate.plusMonths(monthOffset)
        setMonthView()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BlankFragment1.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BlankFragment1().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun OnItemClick(position: Int, dayText: String?) {
        if (dayText.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Invalid date selected", Toast.LENGTH_SHORT).show()
        } else {
            val message = "Selected Date: $dayText ${monthYearFromDate(selectedDate)}"
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}