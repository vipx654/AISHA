package com.aisha.domain.model

data class Task(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val description: String = "",
    val dueTime: Long? = null,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val isReminderSet: Boolean = false,
    val reminderTime: Long? = null
)

enum class TaskPriority {
    LOW, MEDIUM, HIGH
}

enum class TaskCategory {
    GENERAL, WORK, PERSONAL, HEALTH, STUDY, OTHER
}
