package com.aisha.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisha.data.repository.TaskRepository
import com.aisha.domain.model.Task
import com.aisha.domain.model.TaskPriority
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TasksState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TasksState())
    val state: StateFlow<TasksState> = _state.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            taskRepository.tasks.collect { tasks ->
                _state.value = _state.value.copy(tasks = tasks, isLoading = false)
            }
        }
    }

    fun addTask(title: String, description: String = "", priority: TaskPriority = TaskPriority.MEDIUM) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                description = description,
                priority = priority
            )
            taskRepository.addTask(task)
        }
    }

    fun toggleTaskCompletion(taskId: String) {
        viewModelScope.launch {
            taskRepository.toggleTaskCompletion(taskId)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
        }
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            taskRepository.clearAllCompleted()
        }
    }
}
