package com.example.todolistapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

class TodoAdapter (val list : List<TodoModel>) :
    RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        return TodoViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false))
    }

    override fun getItemId(position: Int): Long {
        return list[position].id // Assuming each TodoModel has a unique `id` field.
    }


    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
//        holder.bind(list[position])
    }

    class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        fun bind(todoModel: TodoModel) {
//            with(itemView){
//                val colors = resources.getIntArray(R.array.random_color)
//                val randomColor = colors[Random().nextInt(colors.size)]
//                findViewById<View>(R.id.viewColorTag).setBackgroundColor(randomColor)
//                findViewById<TextView>(R.id.txtShowTitle).text = todoModel.title
//                findViewById<TextView>(R.id.txtShowTask).text = todoModel.description
//                findViewById<TextView>(R.id.txtShowCategory).text = todoModel.category
//                updateTime(todoModel.time)
//                updateDate(todoModel.date)
//            }
//
//        }

//        private fun updateTime(time: Long) {
//            val myformat = "h : mm a"
//            val sdf = SimpleDateFormat(myformat, Locale.getDefault())
//            itemView.findViewById<TextView>(R.id.txtShowTime).text = sdf.format(Date(time))
//        }

//        private fun updateDate(date: Long) {
//            val myformat = "EEE, d MMM yyyy"
//            val sdf = SimpleDateFormat(myformat, Locale.getDefault())
//            itemView.findViewById<TextView>(R.id.txtShowDate).text = sdf.format(Date(date))
//        }


    }

}