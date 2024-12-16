package com.example.todolistapp

import InfiniteViewPageAdapter
import InfiniteViewPageAdapter2
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.example.todolistapp.CalendarUtils.selectedDate
import com.example.todolistapp.databinding.FragmentBlank1Binding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class BlankFragment1 : Fragment(R.layout.fragment_blank1) {

    private var _binding: FragmentBlank1Binding? = null
    private val binding get() = _binding!!
    private lateinit var viewPager: ViewPager
    private lateinit var viewPager2: ViewPager
    private lateinit var infiniteAdapter: InfiniteViewPageAdapter
    private lateinit var infiniteAdapter2: InfiniteViewPageAdapter2
    private val todoList = arrayListOf<TodoModel>()
    private val todoAdapter: TodoAdapter by lazy { TodoAdapter(todoList) }
    private val db by lazy { AppDatabase.getDatabase(requireContext()) }
    private var isExpanded = true


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBlank1Binding.inflate(inflater, container, false)

        setupRecyclerView()
        initSwipe()
        observeTasks()
        setupViewPager()
//        setupViewPager2()
        setupDefaultFragment()

        binding.fullscreenButton.setOnClickListener {
            val fragmentTransaction = parentFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frag_container, BlankFragment3())
            fragmentTransaction.commit()

        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        binding.todoRv.layoutManager = LinearLayoutManager(context)
        binding.todoRv.adapter = todoAdapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupDefaultFragment() {
        val monthlyFragment = parentFragmentManager.findFragmentByTag("MonthlyFragment")
        if (monthlyFragment == null) {
            parentFragmentManager.beginTransaction()
                .add(R.id.viewpage, MonthFragment.newInstance(LocalDate.now()), "MonthlyFragment")
                .commit()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupViewPager() {
        viewPager = binding.viewpage
        infiniteAdapter = InfiniteViewPageAdapter(childFragmentManager).apply {
            setBaseDate(LocalDate.now())
        }
        viewPager.adapter = infiniteAdapter
        viewPager.currentItem = infiniteAdapter.getMiddlePosition()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupViewPager2() {
        viewPager2 = binding.viewpage
        infiniteAdapter2 = InfiniteViewPageAdapter2(childFragmentManager).apply {
            setBaseDate(LocalDate.now())
        }
        viewPager2.adapter = infiniteAdapter2
        viewPager2.currentItem = infiniteAdapter2.getMiddlePosition()
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
                val todo = todoList[position]

                lifecycleScope.launch(Dispatchers.IO) {
                    when (direction) {
                        ItemTouchHelper.LEFT -> { // Swipe left to delete
                            db.todoDao().deleteTask(todo.id)
                            withContext(Dispatchers.Main) {
                                todoList.removeAt(position)
                                todoAdapter.notifyItemRemoved(position)
                                showUndoSnackbar(todo, position, ItemTouchHelper.LEFT)
                            }
                        }

                        ItemTouchHelper.RIGHT -> { // Swipe right to complete
                            db.todoDao().finishTask(todo.id)
                            withContext(Dispatchers.Main) {
                                todoList.removeAt(position)
                                todoAdapter.notifyItemRemoved(position)
                                showUndoSnackbar(todo.copy(isFinished = -1), position, ItemTouchHelper.RIGHT)
                            }
                        }
                    }
                }
            }

            private fun showUndoSnackbar(todo: TodoModel, originalPosition: Int, direction: Int) {
                val actionText = if (direction == ItemTouchHelper.LEFT) "deleted" else "completed"

                Snackbar.make(binding.root, "Task $actionText", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                // Insert the task back into the database
                                val restoredTask = todo.copy(isFinished = if (direction == ItemTouchHelper.LEFT) -1 else todo.isFinished)
                                restoredTask.id += 1
                                db.todoDao().insetTask(restoredTask) // Re-insert task into the database
//                                restoredTask.id = restoredTask.id // Ensure proper ID handling

                                withContext(Dispatchers.Main) {
                                    // Add the task back to the list at the original position
                                    todoList.add(originalPosition, restoredTask)
                                    todoAdapter.notifyItemInserted(originalPosition)
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

    private fun observeTasks() {
        Log.d("BlankFragment1", "observeTasks() called")
        db.todoDao().getTask().observe(viewLifecycleOwner) { tasks ->
            todoList.clear()
            if (!tasks.isNullOrEmpty()) {
                todoList.addAll(0, tasks)
                binding.emptyNotification.visibility = View.GONE
                binding.todoRv.visibility = View.VISIBLE
            } else {
                binding.emptyNotification.visibility = View.VISIBLE
                binding.todoRv.visibility = View.GONE
            }
            todoAdapter.notifyDataSetChanged()

        }
    }

}
