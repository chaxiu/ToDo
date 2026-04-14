package com.boycoder.todo

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.TimeUnit
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var taskAdapter: TaskAdapter

    private val taskDetailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val data = result.data!!
            val title = data.getStringExtra(TaskDetailActivity.EXTRA_TASK_TITLE) ?: return@registerForActivityResult
            val description = data.getStringExtra(TaskDetailActivity.EXTRA_TASK_DESCRIPTION)
            val priority = data.getStringExtra(TaskDetailActivity.EXTRA_TASK_PRIORITY)
            val dueDateValue = data.getLongExtra(TaskDetailActivity.EXTRA_TASK_DUE_DATE, -1)
            val dueDate = if (dueDateValue == -1L) null else dueDateValue

            if (data.hasExtra(TaskDetailActivity.EXTRA_TASK_ID)) {
                val id = data.getStringExtra(TaskDetailActivity.EXTRA_TASK_ID)

                // Refactored using Kotlin's 'find'
                taskViewModel.tasks.value?.find { it.id == id }?.let { existingTask ->
                    // Refactored using 'copy' from data class, removing mutable setters on original object!
                    val updatedTask = existingTask.copy(
                        title = title,
                        description = description,
                        priority = priority,
                        dueDate = dueDate
                    )
                    taskViewModel.updateTask(updatedTask)
                }
            } else {
                // Refactored using 'apply'
                val newTask = Task(title, description).apply {
                    this.priority = priority
                    this.dueDate = dueDate
                }
                taskViewModel.addTask(newTask)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view_tasks)
        val fab: FloatingActionButton = findViewById(R.id.fab_add_task)
        val header: android.view.View = findViewById(R.id.text_header)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            header.setPadding(
                header.paddingLeft,
                systemBars.top,
                header.paddingRight,
                header.paddingBottom
            )

            val recyclerPaddingBottomPx = (88 * resources.displayMetrics.density).toInt()
            recyclerView.setPadding(
                recyclerView.paddingLeft,
                recyclerView.paddingTop,
                recyclerView.paddingRight,
                systemBars.bottom + recyclerPaddingBottomPx
            )

            val fabMarginBottomPx = (32 * resources.displayMetrics.density).toInt()
            val fabParams = fab.layoutParams as ViewGroup.MarginLayoutParams
            fabParams.bottomMargin = systemBars.bottom + fabMarginBottomPx
            fab.layoutParams = fabParams

            insets
        }

        recyclerView.layoutManager = LinearLayoutManager(this)

        taskAdapter = TaskAdapter(
            onTaskStateChanged = { task, isCompleted ->
                taskViewModel.updateTask(task.copy(isCompleted = isCompleted))
            },
            onTaskClicked = { task ->
                val intent = Intent(this@MainActivity, TaskDetailActivity::class.java).apply {
                    putExtra(TaskDetailActivity.EXTRA_TASK_ID, task.id)
                    putExtra(TaskDetailActivity.EXTRA_TASK_TITLE, task.title)
                    putExtra(TaskDetailActivity.EXTRA_TASK_DESCRIPTION, task.description)
                    task.priority?.let { putExtra(TaskDetailActivity.EXTRA_TASK_PRIORITY, it) }
                    task.dueDate?.let { putExtra(TaskDetailActivity.EXTRA_TASK_DUE_DATE, it) }
                }
                taskDetailLauncher.launch(intent)
            }
        )
        recyclerView.adapter = taskAdapter

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        taskViewModel.tasks.observe(this) { tasks ->
            taskAdapter.setTasks(tasks)

            val activeCount = taskViewModel.getActiveTaskCount()
            if (header is TextView) {
                header.text = "Tasks ($activeCount left)"
            }
        }

        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, TaskDetailActivity::class.java)
            taskDetailLauncher.launch(intent)
        }

        if (taskViewModel.tasks.value.isNullOrEmpty()) {
            // Refactored using `apply`
            taskViewModel.addTask(Task("Doctor appointment", "Annual physical checkup").apply {
                priority = "High"
                dueDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1) // Tomorrow
            })

            taskViewModel.addTask(Task("Buy groceries", "Milk, Eggs, Bread").apply {
                priority = "Medium"
            })

            taskViewModel.addTask(Task("Clean the garage", null))

            taskViewModel.addTask(Task("Read a book", "Finish chapter 3").apply {
                priority = "Low"
            })

            taskViewModel.addTask(Task("Call mom", "Wish her happy birthday"))
        }
    }
}
