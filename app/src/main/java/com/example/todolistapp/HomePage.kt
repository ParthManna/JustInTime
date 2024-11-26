package com.example.todolistapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.todolistapp.databinding.ActivityHomePageBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//Partha Sarathi Manna

class HomePage : AppCompatActivity() {

    private val rotateOpen : Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim) }
    private val rotateClose : Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim) }
    private val fromBottom : Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim) }
    private val toBottom : Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim) }

    private var clicked = false

    private lateinit var binding: ActivityHomePageBinding

    val list = arrayListOf<TodoModel>()
    var adapter = TodoAdapter(list)

    val db by lazy{
        AppDatabase.getDatabase(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setSupportActionBar(binding.toolbar)
        binding.toolbar.overflowIcon = ContextCompat.getDrawable(this, R.drawable.ic_menu_overflow)

        binding.fab.setOnClickListener{
            onfabButtonClicked()
        }

        binding.schedule.setOnClickListener{
            Toast.makeText(this, "Schedule button clicked", Toast.LENGTH_SHORT).show()
        }

        binding.note.setOnClickListener {
            Toast.makeText(this, "Note button clicked", Toast.LENGTH_SHORT).show()
        }

        binding.buttonSchedule.setOnClickListener {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frag_container, BlankFragment1())
                .commit()
        }



//        binding.todoRv.apply {
//            layoutManager = LinearLayoutManager(this@HomePage)
//            adapter = this@HomePage.adapter
//        }

        initSwipe()

        db.todoDao().getTask().observe(this, Observer { tasks ->
            list.clear()
            if (!tasks.isNullOrEmpty()) {
                list.addAll(tasks)
            }
            adapter.notifyDataSetChanged() // Update adapter only when database content changes
        })


//
//        binding.fab.setOnClickListener {
//            startActivity(Intent(this, TaskActivity::class.java))
//        }





    }

    private fun onfabButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)
        clicked = !clicked
    }

    private fun setAnimation(clicked: Boolean) {
        if(!clicked){
            binding.note.startAnimation(fromBottom)
            binding.schedule.startAnimation(fromBottom)
            binding.fab.startAnimation(rotateOpen)
        }else{
            binding.note.startAnimation(toBottom)
            binding.schedule.startAnimation(toBottom)
            binding.fab.startAnimation(rotateClose)
        }
    }

    private fun setVisibility(clicked: Boolean) {
        if(!clicked){
            binding.schedule.visibility = View.VISIBLE
            binding.note.visibility = View.VISIBLE
        }else{
            binding.schedule.visibility = View.INVISIBLE
            binding.note.visibility = View.INVISIBLE
        }
    }

    private fun setClickable(clicked: Boolean){
        if(!clicked){
            binding.note.isClickable = true
            binding.schedule.isClickable = true
        }else{
            binding.note.isClickable = false
            binding.schedule.isClickable = false
        }
    }

    fun initSwipe(){
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.layoutPosition
                val itemId = adapter.getItemId(position)

                lifecycleScope.launch(Dispatchers.IO) {
                    if (direction == ItemTouchHelper.LEFT) {
                        db.todoDao().deleteTask(itemId)
                    } else if (direction == ItemTouchHelper.RIGHT) {
                        db.todoDao().finishTask(itemId)
                    }

                    withContext(Dispatchers.Main) {
                        // Remove the item from the list and notify the adapter about the removal
                        list.removeAt(position)
                        adapter.notifyItemRemoved(position)
                    }
                }
            }

            override fun onChildDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                viewHolder: ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {

                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
                    val itemView = viewHolder.itemView

                    val paint = Paint()
                    val icon:Bitmap

                    if(dX > 0){

                        icon = BitmapFactory.decodeResource(resources, R.drawable.checkmark_32)

                        paint.color = Color.parseColor("#00ff95")

                        canvas.drawRect(
                            itemView.left.toFloat(),itemView.top.toFloat(),
                            itemView.left.toFloat() + dX, itemView.bottom.toFloat(),paint
                        )

                        canvas.drawBitmap(
                            icon,
                            itemView.left.toFloat(),
                            itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - icon.height.toFloat())/2,
                            paint
                        )
                    }else{
                        icon = BitmapFactory.decodeResource(resources, R.drawable.delete_32)

                        paint.color = Color.parseColor("#FF0000")

                        canvas.drawRect(
                            itemView.right.toFloat() + dX,itemView.top.toFloat(),
                            itemView.right.toFloat(), itemView.bottom.toFloat(),paint
                        )

                        canvas.drawBitmap(
                            icon,
                            itemView.right.toFloat() - icon.width,
                            itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - icon.height.toFloat())/2,
                            paint
                        )
                    }

                    viewHolder.itemView.translationX = dX

                }
                else {
                    super.onChildDraw(
                        canvas,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }
            }

        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
//        itemTouchHelper.attachToRecyclerView(binding.todoRv)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        val item = menu?.findItem(R.id.notification)
        val searchView = item?.actionView as SearchView
        item.setOnActionExpandListener(object :MenuItem.OnActionExpandListener{
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                displayTodo()
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                displayTodo()
                return true
            }

        })
        searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(!newText.isNullOrEmpty()){
                    displayTodo(newText)
                }
                return true
            }

        })

        return super.onCreateOptionsMenu(menu)
    }

    fun displayTodo(newText: String=""){
        db.todoDao().getTask().observe(this, Observer {
            if(it.isNotEmpty()){
                list.clear()
                list.addAll(
                    it.filter { todo ->
                        todo.title.contains(newText, true)
                    }
                )
                adapter.notifyDataSetChanged()
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.settings ->{
                startActivity(Intent(this, Settings::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

}