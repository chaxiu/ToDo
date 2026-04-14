package com.boycoder.todo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TaskViewModel : ViewModel() {
    private val _tasksLiveData = MutableLiveData<List<Task>>(emptyList())
    val tasks: LiveData<List<Task>> = _tasksLiveData

    fun addTask(task: Task) {
        val currentTasks = _tasksLiveData.value ?: emptyList()
        _tasksLiveData.value = currentTasks + task
    }

    fun updateTask(updatedTask: Task) {
        // Refactored using map operator (creates a new immutable list)
        val currentTasks = _tasksLiveData.value ?: emptyList()
        _tasksLiveData.value = currentTasks.map { task ->
            if (task.id == updatedTask.id) updatedTask else task
        }
    }

    fun deleteTask(taskId: String) {
        // Refactored using filter operator (creates a new immutable list)
        val currentTasks = _tasksLiveData.value ?: emptyList()
        _tasksLiveData.value = currentTasks.filter { it.id != taskId }
    }

    fun getActiveTaskCount(): Int {
        // Refactored using Kotlin Collection operators
        return _tasksLiveData.value?.count { !it.isCompleted } ?: 0
    }

    fun getActiveTasks(): List<Task> {
        // Refactored using Kotlin Collection operators
        return _tasksLiveData.value?.filter { !it.isCompleted } ?: emptyList()
    }
}
