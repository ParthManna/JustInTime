package com.example.todolistapp

import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import java.time.DateTimeException
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
            // For blank cells
            holder.dayOfMonth.text = ""
            holder.dayOfMonth.setBackgroundColor(Color.TRANSPARENT)
            return
        }

        val dayNumber = dayText.toIntOrNull()
        if (dayNumber == null) {
            // Handle invalid day number gracefully
            holder.dayOfMonth.text = ""
            holder.dayOfMonth.setBackgroundColor(Color.TRANSPARENT)
            return
        }

        // Safely check if the day is valid for the selected month
        val isValidDay = try {
            YearMonth.of(selectedDate.year, selectedDate.month).atDay(dayNumber)
            true
        } catch (e: DateTimeException) {
            false
        }

        if (!isValidDay) {
            // Skip invalid days
            holder.dayOfMonth.text = ""
            holder.dayOfMonth.setBackgroundColor(Color.TRANSPARENT)
            return
        }

        // Create the LocalDate after ensuring validity
        val currentDate = LocalDate.of(selectedDate.year, selectedDate.monthValue, dayNumber)

        holder.dayOfMonth.text = dayText
        holder.dayOfMonth.setTextColor(Color.WHITE)
        holder.dayOfMonth.setBackgroundColor(Color.TRANSPARENT)

        val today = LocalDate.now()

        // Highlight today's date
        if (currentDate == today) {
            holder.dayOfMonth.setBackgroundResource(R.drawable.today_background)
            holder.dayOfMonth.setTextColor(Color.BLACK)
        }

        // Highlight Sundays only in the current month
        if (currentDate.dayOfWeek == java.time.DayOfWeek.SUNDAY &&
            currentDate.month == selectedDate.month
        ) {
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
