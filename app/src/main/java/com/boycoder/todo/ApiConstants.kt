package com.boycoder.todo

object ApiConstants {
    // For real devices on the same WiFi, use your Mac's physical LAN IP
    const val BASE_URL = "http://192.168.0.102:8000/api"
    
    const val PATH_USER_PROFILE = "$BASE_URL/user/profile"
    const val PATH_TASK_LIST = "$BASE_URL/tasks/list"
    const val PATH_TASK_ADD = "$BASE_URL/tasks/add"
    const val PATH_TASK_UPDATE = "$BASE_URL/tasks/update"
    const val PATH_TASK_DELETE = "$BASE_URL/tasks/delete"
}
