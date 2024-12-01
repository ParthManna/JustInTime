package com.example.todolistapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolistapp.databinding.FragmentBlank2Binding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class BlankFragment2 : Fragment() {

    private lateinit var binding: FragmentBlank2Binding
    private lateinit var searchEditText: EditText
    private lateinit var todoRecyclerView: RecyclerView
    private val list = arrayListOf<TodoModel>()
    private val db by lazy { AppDatabase2.getDatabase(requireContext()) }
    private val todoAdapter: TodoAdapter2 by lazy { TodoAdapter2(list) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBlank2Binding.inflate(inflater, container, false)



        // Initialize swipe-to-delete or swipe-to-complete functionality
        initSwipe()

        // Observe tasks from the database
        db.todoDao2().getTask().observe(viewLifecycleOwner, Observer { tasks ->
            list.clear()
            if (!tasks.isNullOrEmpty()) {
                list.addAll(tasks)
            }
            todoAdapter.notifyDataSetChanged()
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        searchEditText = view.findViewById(R.id.searchButton)
        todoRecyclerView = view.findViewById(R.id.todoRv2)

        // Set up RecyclerView
        binding.todoRv2.layoutManager = LinearLayoutManager(context)
        binding.todoRv2.adapter = todoAdapter

        // Add search functionality
        setupSearch()
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener { text ->
            val query = text.toString()
            displayTodo(query)
        }
    }

    private fun displayTodo(query: String = "") {
        // Observe the database and update the RecyclerView
        db.todoDao2().getTask().observe(viewLifecycleOwner, Observer { tasks ->
            if (tasks.isNotEmpty()) {
                list.clear()
                list.addAll(tasks.filter { it.title.contains(query, true) })
                todoAdapter.notifyDataSetChanged()
            }
        })
    }
    private fun initSwipe() {
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.layoutPosition
                val todo = list[position]

                lifecycleScope.launch(Dispatchers.IO) {
                    when (direction) {
                        ItemTouchHelper.LEFT -> { // Swipe left to delete
                            db.todoDao2().deleteTask(todo.id)
                            withContext(Dispatchers.Main) {
                                list.removeAt(position)
                                todoAdapter.notifyItemRemoved(position)
                                showUndoSnackbar(todo, position, ItemTouchHelper.LEFT)
                            }
                        }

                        ItemTouchHelper.RIGHT -> { // Swipe right to complete
                            db.todoDao2().finishTask(todo.id)
                            withContext(Dispatchers.Main) {
                                list.removeAt(position)
                                todoAdapter.notifyItemRemoved(position)
                                showUndoSnackbar(todo.copy(isFinished = -1), position, ItemTouchHelper.RIGHT)
                            }
                        }
                    }
                }
            }

            private fun showUndoSnackbar(todo: TodoModel, position: Int, direction: Int) {
                val actionText = if (direction == ItemTouchHelper.LEFT) "deleted" else "completed"

                Snackbar.make(binding.root, "Task $actionText", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                val taskToRestore = todo.copy(isFinished = if (direction == ItemTouchHelper.LEFT) -1 else todo.isFinished)
                                taskToRestore.id = 0
                                db.todoDao2().insetTask(taskToRestore) // Re-insert task into the database
                                withContext(Dispatchers.Main) {
                                    list.add(position, taskToRestore) // Add back to the list
                                    todoAdapter.notifyItemInserted(position)
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Snackbar.make(binding.root, "Failed to undo action", Snackbar.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }.show()
            }

            override fun onChildDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView

                    val paint = Paint()
                    val icon: Bitmap

                    if (dX > 0) { // Swipe right (mark as complete)
                        icon = BitmapFactory.decodeResource(resources, R.drawable.checkmark_32)
                        paint.color = Color.parseColor("#00ff95")
                        canvas.drawRect(
                            itemView.left.toFloat(), itemView.top.toFloat(),
                            itemView.left.toFloat() + dX, itemView.bottom.toFloat(), paint
                        )
                        canvas.drawBitmap(
                            icon,
                            itemView.left.toFloat() + 32,
                            itemView.top.toFloat() + (itemView.height - icon.height) / 2f,
                            paint
                        )
                    } else { // Swipe left (delete)
                        icon = BitmapFactory.decodeResource(resources, R.drawable.delete_32)
                        paint.color = Color.parseColor("#FF0000")
                        canvas.drawRect(
                            itemView.right.toFloat() + dX, itemView.top.toFloat(),
                            itemView.right.toFloat(), itemView.bottom.toFloat(), paint
                        )
                        canvas.drawBitmap(
                            icon,
                            itemView.right.toFloat() - icon.width - 32,
                            itemView.top.toFloat() + (itemView.height - icon.height) / 2f,
                            paint
                        )
                    }

                    viewHolder.itemView.translationX = dX
                } else {
                    viewHolder.itemView.translationX = 0f
                    super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.todoRv2)
    }
}
