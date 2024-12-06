import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.todolistapp.R
import java.time.LocalDate
import java.time.YearMonth

class CalendarAdapter2(
    private val daysOfWeek: ArrayList<String>,
    private val selectedDate: LocalDate,
    private val onItemListener: OnItemListener
) : RecyclerView.Adapter<CalendarAdapter2.CalendarViewHolder>() {

    interface OnItemListener {
        fun OnItemClick(position: Int, dayText: String?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.calender_cel, parent, false)

        // Adjust height dynamically for a 6-row calendar
        val layoutParams = view.layoutParams
        layoutParams.height = parent.height // Divide parent height into 6 rows
        view.layoutParams = layoutParams

        return CalendarViewHolder(view, onItemListener)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val dayText = daysOfWeek[position]

        if (dayText.isEmpty()) {
            // Empty cell
            holder.dayOfMonth.text = ""
            holder.dayOfMonth.setBackgroundColor(Color.TRANSPARENT)
            return
        }

        val dayNumber = dayText.toIntOrNull()
        if (dayNumber == null) {
            // Invalid day
            holder.dayOfMonth.text = ""
            holder.dayOfMonth.setBackgroundColor(Color.TRANSPARENT)
            return
        }

        // Validate day within the month
        val yearMonth = YearMonth.of(selectedDate.year, selectedDate.monthValue)
        if (dayNumber < 1 || dayNumber > yearMonth.lengthOfMonth()) {
            // Invalid day for the current month
            holder.dayOfMonth.text = ""
            holder.dayOfMonth.setBackgroundColor(Color.TRANSPARENT)
            return
        }

        holder.dayOfMonth.text = dayText

        // Styling for valid days
        holder.dayOfMonth.setTextColor(Color.WHITE)
        holder.dayOfMonth.setBackgroundColor(Color.TRANSPARENT)

        val currentDate = LocalDate.of(selectedDate.year, selectedDate.monthValue, dayNumber)
        val today = LocalDate.now()

        if (currentDate == today) {
            holder.dayOfMonth.setBackgroundResource(R.drawable.today_background) // Highlight today
            holder.dayOfMonth.setTextColor(Color.BLACK)
        }

        // Highlight Sundays (only for days in the current month)
        if (currentDate.dayOfWeek == java.time.DayOfWeek.SUNDAY) {
            holder.dayOfMonth.setTextColor(Color.RED)
        }
    }

    override fun getItemCount(): Int = daysOfWeek.size

    class CalendarViewHolder(
        itemView: View,
        private val onItemListener: OnItemListener
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val dayOfMonth: TextView = itemView.findViewById(R.id.celldaytext)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val dayText = dayOfMonth.text.toString()
                onItemListener.OnItemClick(position, dayText)
            }
        }
    }
}
