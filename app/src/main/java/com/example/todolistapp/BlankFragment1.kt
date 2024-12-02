package com.example.todolistapp

import InfiniteViewPageAdapter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.example.todolistapp.databinding.FragmentBlank1Binding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class BlankFragment1 : Fragment(R.layout.fragment_blank1) {

    private lateinit var binding: FragmentBlank1Binding
    private lateinit var viewPager: ViewPager
    private lateinit var infiniteAdapter: InfiniteViewPageAdapter
    private val list = arrayListOf<TodoModel>()
    private val todoAdapter: TodoAdapter by lazy { TodoAdapter(list) }
    private val db by lazy { AppDatabase.getDatabase(requireContext()) }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBlank1Binding.inflate(inflater, container, false)



        // Setup RecyclerView
        setupRecyclerView()



        // Initialize swipe-to-delete or swipe-to-complete functionality
        initSwipe()

        // Observe tasks from the database
        db.todoDao().getTask().observe(viewLifecycleOwner, Observer { tasks ->
            list.clear()
            if (!tasks.isNullOrEmpty()) {
                list.addAll(tasks)
            }
            todoAdapter.notifyDataSetChanged()
        })

        // Initialize ViewPager for calendar
        setupViewPager()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.todoRv.layoutManager = LinearLayoutManager(context)
        binding.todoRv.adapter = todoAdapter
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupViewPager() {
        viewPager = binding.viewpage
        infiniteAdapter = InfiniteViewPageAdapter(childFragmentManager)

        // Pass the current date to the adapter to initialize it
        infiniteAdapter.setBaseDate(LocalDate.now())
        viewPager.adapter = infiniteAdapter

        // Set the current page of the ViewPager to the middle position
        viewPager.currentItem = infiniteAdapter.getMiddlePosition()
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
                            db.todoDao().deleteTask(todo.id)
                            withContext(Dispatchers.Main) {
                                list.removeAt(position)
                                todoAdapter.notifyItemRemoved(position)
                                showUndoSnackbar(todo, position, ItemTouchHelper.LEFT)
                            }
                        }

                        ItemTouchHelper.RIGHT -> { // Swipe right to complete
                            db.todoDao().finishTask(todo.id)
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
                                db.todoDao().insetTask(taskToRestore) // Re-insert task into the database
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
        itemTouchHelper.attachToRecyclerView(binding.todoRv)
    }
}
