package com.boycoder.todo

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import java.util.concurrent.TimeUnit
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private val taskViewModel: TaskViewModel by viewModels()
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
        val searchEdit: EditText = findViewById(R.id.edit_search)
        val userProfileLayout: android.view.View = findViewById(R.id.layout_user_profile)
        val usernameText: TextView = findViewById(R.id.text_username)
        val avatarImage: ImageView = findViewById(R.id.image_avatar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            userProfileLayout.setPadding(
                userProfileLayout.paddingLeft,
                systemBars.top,
                userProfileLayout.paddingRight,
                userProfileLayout.paddingBottom
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

        taskViewModel.user.observe(this) { user ->
            if (user != null) {
                usernameText.text = user.username
                avatarImage.loadAvatar(user.avatarUrl)
            } else {
                usernameText.text = "Loading user..."
            }
        }

        taskViewModel.tasks.observe(this) { tasks ->
            taskAdapter.setTasks(tasks)

            val activeCount = taskViewModel.getActiveTaskCount()
            val header: TextView = findViewById(R.id.text_header)
            header.text = "Tasks ($activeCount left)"
        }

        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, TaskDetailActivity::class.java)
            taskDetailLauncher.launch(intent)
        }

        // 原始回调地狱 (反面教材)
        searchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                taskViewModel.searchTasks(query)
            }
        })

        // Fetch initial data from the network sequentially
        taskViewModel.loadDashboardData()
    }
}
