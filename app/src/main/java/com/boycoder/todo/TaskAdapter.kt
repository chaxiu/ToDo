package com.boycoder.todo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class TaskAdapter(
    private val onTaskStateChanged: (Task, Boolean) -> Unit,
    private val onTaskClicked: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var tasks: List<Task> = emptyList()

    fun setTasks(tasks: List<Task>) {
        this.tasks = tasks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.text_task_title)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.text_task_description)
        private val dateTextView: TextView = itemView.findViewById(R.id.text_task_date)
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkbox_task)

        init {
            checkBox.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onTaskStateChanged(tasks[pos], checkBox.isChecked)
                }
            }

            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onTaskClicked(tasks[pos])
                }
            }
        }

        fun bind(task: Task) {
            // Null safety with ?. and ?:
            descriptionTextView.text = task.description?.takeIf { it.isNotEmpty() } ?: "No description provided"

            // let and String templates
            titleTextView.text = task.priority?.let { "${task.title} [$it]" } ?: task.title

            // Scope functions (apply/let)
            task.dueDate?.let {
                dateTextView.visibility = View.VISIBLE
                dateTextView.text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(it)
            } ?: run {
                dateTextView.visibility = View.GONE
            }

            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = task.isCompleted

            // apply
            val alphaValue = if (task.isCompleted) 0.5f else 1.0f
            titleTextView.alpha = alphaValue
            descriptionTextView.alpha = alphaValue
            dateTextView.alpha = alphaValue
        }
    }
}
