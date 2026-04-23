package com.boycoder.todo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.reflect.Type
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
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

    private val _userLiveData = MutableLiveData<User?>()
    val user: LiveData<User?> = _userLiveData

    private val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

    /**
     * A generic wrapper that turns OkHttp's Callback into a coroutine `suspend` function.
     * Uses suspendCancellableCoroutine to support structured concurrency cancellation.
     */
    private suspend fun <T> executeRequest(request: Request, typeOfT: Type): T? = suspendCancellableCoroutine { continuation ->
        val call = client.newCall(request)

        // If the coroutine is cancelled (e.g. ViewModel is cleared), cancel the OkHttp network request!
        continuation.invokeOnCancellation {
            call.cancel()
        }

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        try {
                            val result: T = gson.fromJson(body, typeOfT)
                            continuation.resume(result)
                        } catch (e: Exception) {
                            continuation.resumeWithException(e)
                        }
                    } else {
                        continuation.resumeWithException(IOException("Empty body"))
                    }
                } else {
                    continuation.resumeWithException(IOException("HTTP Error ${response.code}"))
                }
            }
        })
    }

    /**
     * Overload for simpler calls where we just need a Class token
     */
    private suspend fun <T> executeRequest(request: Request, clazz: Class<T>): T? {
        return executeRequest(request, clazz as Type)
    }

    private suspend fun fetchUserProfile(): User? {
        val request = Request.Builder()
            .url(ApiConstants.PATH_USER_PROFILE)
            .post("{}".toRequestBody(jsonMediaType))
            .build()
        return executeRequest(request, User::class.java)
    }

    private suspend fun fetchTaskList(): List<Task>? {
        val request = Request.Builder()
            .url(ApiConstants.PATH_TASK_LIST)
            .post("{}".toRequestBody(jsonMediaType))
            .build()
        val taskListType = object : TypeToken<List<Task>>() {}.type
        return executeRequest(request, taskListType)
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                // SEQUENTIAL CALLS (The anti-pattern they need to fix in the homework)
                val user = fetchUserProfile() 
                _userLiveData.value = user
                
                val tasks = fetchTaskList()
                if (tasks != null) {
                    _tasksLiveData.value = tasks
                }
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Failed to load dashboard", e)
            }
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            try {
                val taskJson = gson.toJson(task)
                val request = Request.Builder()
                    .url(ApiConstants.PATH_TASK_ADD)
                    .post(taskJson.toRequestBody(jsonMediaType))
                    .build()

                val addedTask = executeRequest(request, Task::class.java)
                if (addedTask != null) {
                    val currentTasks = _tasksLiveData.value ?: emptyList()
                    _tasksLiveData.value = currentTasks + addedTask
                }
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Failed to add task", e)
            }
        }
    }

    fun updateTask(updatedTask: Task) {
        viewModelScope.launch {
            try {
                val taskJson = gson.toJson(updatedTask)
                val request = Request.Builder()
                    .url(ApiConstants.PATH_TASK_UPDATE)
                    .post(taskJson.toRequestBody(jsonMediaType))
                    .build()

                val returnedTask = executeRequest(request, Task::class.java)
                if (returnedTask != null) {
                    val currentTasks = _tasksLiveData.value ?: emptyList()
                    _tasksLiveData.value = currentTasks.map { task ->
                        if (task.id == returnedTask.id) returnedTask else task
                    }
                }
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Failed to update task", e)
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                val jsonBody = """{"id": "$taskId"}"""
                val request = Request.Builder()
                    .url(ApiConstants.PATH_TASK_DELETE)
                    .post(jsonBody.toRequestBody(jsonMediaType))
                    .build()

                // We don't really care about parsing the return JSON for delete, just that it succeeded
                executeRequest(request, Any::class.java) 
                
                val currentTasks = _tasksLiveData.value ?: emptyList()
                _tasksLiveData.value = currentTasks.filter { it.id != taskId }
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Failed to delete task", e)
            }
        }
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
