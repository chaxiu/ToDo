package com.boycoder.todo

import java.util.UUID

data class Task(
    var title: String,
    var description: String?,
    val id: String = UUID.randomUUID().toString(),
    var isCompleted: Boolean = false,
    var priority: String? = null,
    var dueDate: Long? = null
)
