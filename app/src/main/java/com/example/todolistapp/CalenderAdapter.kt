package com.example.todolistapp

import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class CalenderAdapter(
    private val daysOfMonth: ArrayList<String>,
    private val selectedDate: LocalDate,
    private val onItemListener: OnItemListener
) : RecyclerView.Adapter<CalenderAdapter.CalenderViewHolder>() {

    interface OnItemListener {
        fun OnItemClick(position: Int, dayText: String?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalenderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.calender_cel, parent, false)

        // Adjust height dynamically for a 6-row calendar
        val layoutParams = view.layoutParams
        layoutParams.height = (parent.height * 0.166666666).toInt() // 1/6th of the parent's height
        view.layoutParams = layoutParams

        return CalenderViewHolder(view, onItemListener)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CalenderViewHolder, position: Int) {
        val dayText = daysOfMonth[position]
        holder.dayOfMonth.text = dayText

        // Determine the first and last valid day indices for the current month
        val firstDayIndex = daysOfMonth.indexOfFirst { it == "1" }
        val lastDayIndex = daysOfMonth.indexOfLast {
            it == YearMonth.from(selectedDate).lengthOfMonth().toString()
        }

        when {
            position < firstDayIndex -> {
                // Days from the previous month
                holder.dayOfMonth.setTextColor(Color.LTGRAY) // Light gray
            }
            position > lastDayIndex -> {
                // Days from the next month
                holder.dayOfMonth.setTextColor(Color.parseColor("#7E7E7E")) // Light gray
            }
            else -> {
                // Days in the current month
                holder.dayOfMonth.setTextColor(Color.WHITE)

                // Days in the current month
                val currentDate = LocalDate.of(
                    selectedDate.year,
                    selectedDate.monthValue,
                    dayText.toInt()
                )
                // Highlight today's date
                val today = LocalDate.now()
                if (currentDate == today) {
                    holder.dayOfMonth.setBackgroundResource(R.drawable.today_background) // Set background
                    holder.dayOfMonth.setTextColor(Color.BLACK) // Text color for visibility
                }
            }
        }

        if (dayText.isNotEmpty() && position in firstDayIndex..lastDayIndex) {
            // Determine the day of the week based on position (assuming week starts on Sunday)
            val dayOfWeek = LocalDate.of(
                selectedDate.year,
                selectedDate.monthValue,
                dayText.toInt()
            ).dayOfWeek.value

            if (dayOfWeek == DayOfWeek.SUNDAY.value) {
                holder.dayOfMonth.setTextColor(Color.RED)
            }
        }
    }






    override fun getItemCount(): Int = daysOfMonth.size

    class CalenderViewHolder(
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
