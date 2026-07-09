package com.aisha.data.repository

import android.content.Context
import com.aisha.domain.model.Task
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("aisha_tasks", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: Flow<List<Task>> = _tasks.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        val json = prefs.getString("tasks", null)
        if (json != null) {
            val type = object : TypeToken<List<Task>>() {}.type
            val taskList: List<Task> = gson.fromJson(json, type)
            _tasks.value = taskList.sortedBy { it.createdAt }.reversed()
        }
    }

    private fun saveTasks() {
        val json = gson.toJson(_tasks.value)
        prefs.edit().putString("tasks", json).apply()
    }

    fun addTask(task: Task) {
        val updated = listOf(task) + _tasks.value
        _tasks.value = updated.sortedBy { it.createdAt }.reversed()
        saveTasks()
    }

    fun updateTask(task: Task) {
        val updated = _tasks.value.map { if (it.id == task.id) task else it }
        _tasks.value = updated.sortedBy { it.createdAt }.reversed()
        saveTasks()
    }

    fun deleteTask(taskId: String) {
        val updated = _tasks.value.filter { it.id != taskId }
        _tasks.value = updated
        saveTasks()
    }

    fun toggleTaskCompletion(taskId: String) {
        val updated = _tasks.value.map { task ->
            if (task.id == taskId) {
                task.copy(
                    isCompleted = !task.isCompleted,
                    completedAt = if (!task.isCompleted) System.currentTimeMillis() else null
                )
            } else task
        }
        _tasks.value = updated.sortedBy { it.createdAt }.reversed()
        saveTasks()
    }

    fun getTaskById(taskId: String): Task? {
        return _tasks.value.find { it.id == taskId }
    }

    fun getIncompleteTasks(): List<Task> {
        return _tasks.value.filter { !it.isCompleted }
    }

    fun getCompletedTasks(): List<Task> {
        return _tasks.value.filter { it.isCompleted }
    }

    fun clearAllCompleted() {
        val updated = _tasks.value.filter { !it.isCompleted }
        _tasks.value = updated
        saveTasks()
    }
}
