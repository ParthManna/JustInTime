import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.parthasarathimanna.todolistapp.CalenderAdapter
import com.parthasarathimanna.todolistapp.R
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class MonthFragment : Fragment() {

    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var monthYearText: TextView
    private lateinit var selectedDate: LocalDate

    companion object {
        private const val ARG_DATE = "date"

        @RequiresApi(Build.VERSION_CODES.O)
        fun newInstance(date: LocalDate): MonthFragment {
            val fragment = MonthFragment()
            val args = Bundle()
            args.putString(ARG_DATE, date.toString())
            fragment.arguments = args
            return fragment
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedDate = LocalDate.parse(arguments?.getString(ARG_DATE))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_month, container, false)

        calendarRecyclerView = view.findViewById(R.id.calenderRecycleview)
        monthYearText = view.findViewById(R.id.monthyeartv)

        setMonthView()

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setMonthView() {
        val formattedDate = monthYearFromDate(selectedDate)
        monthYearText.text = formattedDate

        val daysInMonth: ArrayList<String> = dayInMonthArray(selectedDate)

        val calendarAdapter = CalenderAdapter(daysInMonth, selectedDate, object : CalenderAdapter.OnItemListener {
            override fun OnItemClick(position: Int, dayText: String?) {
                if (dayText.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Invalid date selected", Toast.LENGTH_SHORT).show()
                } else {
                    val message = "Selected Date: $dayText ${monthYearFromDate(selectedDate)}"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        })
        calendarRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
        calendarRecyclerView.adapter = calendarAdapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun dayInMonthArray(date: LocalDate): ArrayList<String> {
        val daysInMonthArray = ArrayList<String>()
        val yearMonth = YearMonth.from(date)
        val daysInMonth = yearMonth.lengthOfMonth()

        val firstOfMonth = date.withDayOfMonth(1)
        val dayOfWeek = firstOfMonth.dayOfWeek.value

        // Add previous month's dates
        val prevMonth = date.minusMonths(1)
        val prevMonthYearMonth = YearMonth.from(prevMonth)
        val daysInPrevMonth = prevMonthYearMonth.lengthOfMonth()
        for (i in (daysInPrevMonth - dayOfWeek + 1)..daysInPrevMonth) {
            daysInMonthArray.add(i.toString())
        }

        // Add current month's dates
        for (i in 1..daysInMonth) {
            daysInMonthArray.add(i.toString())
        }

        // Add next month's dates
        val remainingDays = 42 - daysInMonthArray.size
        for (i in 1..remainingDays) {
            daysInMonthArray.add(i.toString())
        }

        return daysInMonthArray
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun monthYearFromDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        return date.format(formatter).uppercase()
    }
}