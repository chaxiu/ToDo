package com.boycoder.todo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks = new ArrayList<>();
    private final OnTaskStateChangeListener listener;
    private final OnTaskClickListener clickListener;

    public interface OnTaskStateChangeListener {
        void onTaskStateChanged(Task task, boolean isCompleted);
    }
    
    public interface OnTaskClickListener {
        void onTaskClicked(Task task);
    }

    public TaskAdapter(OnTaskStateChangeListener listener, OnTaskClickListener clickListener) {
        this.listener = listener;
        this.clickListener = clickListener;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView descriptionTextView;
        private final TextView dateTextView;
        private final CheckBox checkBox;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_task_title);
            descriptionTextView = itemView.findViewById(R.id.text_task_description);
            dateTextView = itemView.findViewById(R.id.text_task_date);
            checkBox = itemView.findViewById(R.id.checkbox_task);

            checkBox.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    Task task = tasks.get(pos);
                    listener.onTaskStateChanged(task, checkBox.isChecked());
                }
            });
            
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    Task task = tasks.get(pos);
                    clickListener.onTaskClicked(task);
                }
            });
        }

        public void bind(Task task) {
            titleTextView.setText(task.getTitle());
            
            // This verbose null check is an intentional setup for Kotlin's `?.` and `?:` refactoring
            String desc = task.getDescription();
            if (desc != null && !desc.isEmpty()) {
                descriptionTextView.setText(desc);
            } else {
                descriptionTextView.setText("No description provided");
            }
            
            // Setup for string templates and let/apply refactoring
            String priority = task.getPriority();
            if (priority != null) {
                titleTextView.setText(task.getTitle() + " [" + priority + "]");
            } else {
                titleTextView.setText(task.getTitle());
            }
            
            // Setup for scope functions (let) refactoring
            Long dueDate = task.getDueDate();
            if (dueDate != null) {
                dateTextView.setVisibility(View.VISIBLE);
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                dateTextView.setText(sdf.format(dueDate));
            } else {
                dateTextView.setVisibility(View.GONE);
            }
            
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(task.isCompleted());
            
            if (task.isCompleted()) {
                titleTextView.setAlpha(0.5f);
                descriptionTextView.setAlpha(0.5f);
                dateTextView.setAlpha(0.5f);
            } else {
                titleTextView.setAlpha(1.0f);
                descriptionTextView.setAlpha(1.0f);
                dateTextView.setAlpha(1.0f);
            }
        }
    }
}
