package com.boycoder.todo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private TaskViewModel taskViewModel;
    private TaskAdapter taskAdapter;

    private final ActivityResultLauncher<Intent> taskDetailLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String title = data.getStringExtra(TaskDetailActivity.EXTRA_TASK_TITLE);
                    String description = data.getStringExtra(TaskDetailActivity.EXTRA_TASK_DESCRIPTION);
                    String priority = data.getStringExtra(TaskDetailActivity.EXTRA_TASK_PRIORITY);
                    long dueDateValue = data.getLongExtra(TaskDetailActivity.EXTRA_TASK_DUE_DATE, -1);
                    Long dueDate = dueDateValue == -1 ? null : dueDateValue;

                    if (data.hasExtra(TaskDetailActivity.EXTRA_TASK_ID)) {
                        String id = data.getStringExtra(TaskDetailActivity.EXTRA_TASK_ID);
                        
                        // Anti-pattern for Lesson 1: Finding an item with a manual loop
                        // This will be refactored using Kotlin's 'find' or 'firstOrNull'
                        Task updatedTask = null;
                        for (Task t : taskViewModel.getTasks().getValue()) {
                            if (t.getId().equals(id)) {
                                updatedTask = t;
                                break;
                            }
                        }
                        
                        if (updatedTask != null) {
                            // Anti-pattern for Lesson 1: Sequential setters instead of 'apply' or data class 'copy'
                            updatedTask.setTitle(title);
                            updatedTask.setDescription(description);
                            updatedTask.setPriority(priority);
                            updatedTask.setDueDate(dueDate);
                            taskViewModel.updateTask(updatedTask);
                        }
                    } else {
                        // Anti-pattern for Lesson 1: Sequential setters instead of 'apply'
                        Task newTask = new Task(title, description);
                        newTask.setPriority(priority);
                        newTask.setDueDate(dueDate);
                        taskViewModel.addTask(newTask);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        RecyclerView recyclerView = findViewById(R.id.recycler_view_tasks);
        FloatingActionButton fab = findViewById(R.id.fab_add_task);
        android.view.View header = findViewById(R.id.text_header);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            // Apply status bar inset as top padding to header so it moves down naturally
            header.setPadding(header.getPaddingLeft(), systemBars.top, header.getPaddingRight(), header.getPaddingBottom());
            
            // For Recycler View, apply nav bar inset as bottom padding so items scroll behind nav bar
            int recyclerPaddingBottomPx = (int) (88 * getResources().getDisplayMetrics().density);
            recyclerView.setPadding(
                recyclerView.getPaddingLeft(),
                recyclerView.getPaddingTop(),
                recyclerView.getPaddingRight(),
                systemBars.bottom + recyclerPaddingBottomPx
            );
            
            // Move FAB up by setting bottom margin to include system bar height + original 32dp
            int fabMarginBottomPx = (int) (32 * getResources().getDisplayMetrics().density);
            ViewGroup.MarginLayoutParams fabParams = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
            fabParams.bottomMargin = systemBars.bottom + fabMarginBottomPx;
            fab.setLayoutParams(fabParams);
            
            return insets;
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskAdapter = new TaskAdapter(
            (task, isCompleted) -> {
                task.setCompleted(isCompleted);
                taskViewModel.updateTask(task);
            },
            task -> {
                Intent intent = new Intent(MainActivity.this, TaskDetailActivity.class);
                intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, task.getId());
                intent.putExtra(TaskDetailActivity.EXTRA_TASK_TITLE, task.getTitle());
                intent.putExtra(TaskDetailActivity.EXTRA_TASK_DESCRIPTION, task.getDescription());
                if (task.getPriority() != null) {
                    intent.putExtra(TaskDetailActivity.EXTRA_TASK_PRIORITY, task.getPriority());
                }
                if (task.getDueDate() != null) {
                    intent.putExtra(TaskDetailActivity.EXTRA_TASK_DUE_DATE, task.getDueDate());
                }
                taskDetailLauncher.launch(intent);
            }
        );
        recyclerView.setAdapter(taskAdapter);

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        taskViewModel.getTasks().observe(this, tasks -> {
            taskAdapter.setTasks(tasks);
            
            // Update the header with the active task count
            int activeCount = taskViewModel.getActiveTaskCount();
            if (header instanceof android.widget.TextView) {
                ((android.widget.TextView) header).setText("Tasks (" + activeCount + " left)");
            }
        });

        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, TaskDetailActivity.class);
            taskDetailLauncher.launch(intent);
        });

        if (taskViewModel.getTasks().getValue() == null || taskViewModel.getTasks().getValue().isEmpty()) {
            // These verbose sequential setter calls are intentional teaching material.
            // They will be refactored using Kotlin's `apply` scope function.
            
            Task t1 = new Task("Doctor appointment", "Annual physical checkup");
            t1.setPriority("High");
            t1.setDueDate(System.currentTimeMillis() + 86400000); // Tomorrow
            taskViewModel.addTask(t1);

            Task t2 = new Task("Buy groceries", "Milk, Eggs, Bread");
            t2.setPriority("Medium");
            taskViewModel.addTask(t2);

            Task t3 = new Task("Clean the garage", null); // Intentional null description
            taskViewModel.addTask(t3);

            Task t4 = new Task("Read a book", "Finish chapter 3");
            t4.setPriority("Low");
            taskViewModel.addTask(t4);

            Task t5 = new Task("Call mom", "Wish her happy birthday");
            taskViewModel.addTask(t5);
        }
    }
}
