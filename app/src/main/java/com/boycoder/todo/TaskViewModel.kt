package com.boycoder.todo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class TaskViewModel : ViewModel() {
    private val _tasksLiveData = MutableLiveData<List<Task>>(emptyList())
    val tasks: LiveData<List<Task>> = _tasksLiveData

    private val client = OkHttpClient()
    private val gson = Gson()
    // For real devices on the same WiFi, use your Mac's physical LAN IP
    private val baseUrl = "http://192.168.0.102:8000/api/tasks"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

    fun fetchTasks() {
        val request = Request.Builder()
            .url("$baseUrl/list")
            .post("{}".toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TaskViewModel", "Failed to fetch tasks", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val taskListType = object : TypeToken<List<Task>>() {}.type
                        val fetchedTasks: List<Task> = gson.fromJson(responseBody, taskListType)
                        _tasksLiveData.postValue(fetchedTasks)
                    }
                }
            }
        })
    }

    fun addTask(task: Task) {
        val taskJson = gson.toJson(task)
        val request = Request.Builder()
            .url("$baseUrl/add")
            .post(taskJson.toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TaskViewModel", "Failed to add task", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val addedTask: Task = gson.fromJson(responseBody, Task::class.java)
                        val currentTasks = _tasksLiveData.value ?: emptyList()
                        _tasksLiveData.postValue(currentTasks + addedTask)
                    }
                }
            }
        })
    }

    fun updateTask(updatedTask: Task) {
        val taskJson = gson.toJson(updatedTask)
        val request = Request.Builder()
            .url("$baseUrl/update")
            .post(taskJson.toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TaskViewModel", "Failed to update task", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val returnedTask: Task = gson.fromJson(responseBody, Task::class.java)
                        val currentTasks = _tasksLiveData.value ?: emptyList()
                        _tasksLiveData.postValue(currentTasks.map { task ->
                            if (task.id == returnedTask.id) returnedTask else task
                        })
                    }
                }
            }
        })
    }

    fun deleteTask(taskId: String) {
        val jsonBody = """{"id": "$taskId"}"""
        val request = Request.Builder()
            .url("$baseUrl/delete")
            .post(jsonBody.toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("TaskViewModel", "Failed to delete task", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val currentTasks = _tasksLiveData.value ?: emptyList()
                    _tasksLiveData.postValue(currentTasks.filter { it.id != taskId })
                }
            }
        })
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
