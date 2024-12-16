package com.example.todolistapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.todolistapp.databinding.ActivityHomePageBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomePage : AppCompatActivity() {

    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim) }

    private var clicked = false

    private lateinit var binding: ActivityHomePageBinding
    private val list = arrayListOf<TodoModel>()
    private val adapter = TodoAdapter(list)
    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbar)
        binding.toolbar.overflowIcon = ContextCompat.getDrawable(this, R.drawable.ic_menu_overflow)

        binding.fab.setOnClickListener { onFabButtonClicked() }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frag_container, BlankFragment3())
            .commit()

        binding.note.setOnClickListener {
            startActivity(Intent(this, NoteActivity::class.java))
        }

        binding.schedule.setOnClickListener {
            startActivity(Intent(this, TaskActivity::class.java))
        }


        binding.buttonSchedule.setOnClickListener {
            binding.buttonSchedule.setBackgroundResource(R.drawable.button_background)
            binding.buttonNote.setBackgroundResource(R.drawable.background_button)

            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frag_container, BlankFragment3())
                .commit()
        }

        binding.buttonNote.setOnClickListener {
            binding.buttonSchedule.setBackgroundResource(R.drawable.background_button)
            binding.buttonNote.setBackgroundResource(R.drawable.button_background)

            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frag_container, BlankFragment2())
                .commit()

        }
    }

    private fun onFabButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)
        clicked = !clicked
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            binding.note.startAnimation(fromBottom)
            binding.schedule.startAnimation(fromBottom)
            binding.fab.startAnimation(rotateOpen)
        } else {
            binding.note.startAnimation(toBottom)
            binding.schedule.startAnimation(toBottom)
            binding.fab.startAnimation(rotateClose)
        }
    }

    private fun setVisibility(clicked: Boolean) {
        binding.schedule.visibility = if (!clicked) View.VISIBLE else View.INVISIBLE
        binding.note.visibility = if (!clicked) View.VISIBLE else View.INVISIBLE
    }

    private fun setClickable(clicked: Boolean) {
        binding.note.isClickable = !clicked
        binding.schedule.isClickable = !clicked
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
//        val item = menu?.findItem(R.id.search)
//        val searchView = item?.actionView as? SearchView
//
//        item?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
//            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
//                displayTodo()
//                return true
//            }
//
//            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
//                displayTodo()
//                return true
//            }
//        })
//
//        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean = false
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                if (!newText.isNullOrEmpty()) {
//                    displayTodo(newText)
//                }
//                return true
//            }
//        })
//
        return super.onCreateOptionsMenu(menu)
    }
//
//    private fun displayTodo(newText: String = "") {
//        db.todoDao().getTask().observe(this, Observer { tasks ->
//            list.clear()
//            if (tasks.isNotEmpty()) {
//                list.addAll(tasks.filter { todo -> todo.title.contains(newText, true) })
//                adapter.notifyDataSetChanged()
//            }
//        })
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.clearall -> clearalltask()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun clearalltask() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Clear all tasks from the database
                db.todoDao().clearAllTasks()

                withContext(Dispatchers.Main) {
                    // Clear the list and notify the adapter
                    list.clear()
                    adapter.notifyDataSetChanged()


                    // Show confirmation message
                    Snackbar.make(binding.root, "All tasks cleared", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(binding.root, "Failed to clear tasks", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }
}
