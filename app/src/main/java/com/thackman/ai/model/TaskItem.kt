package com.thackman.ai.model

data class TaskItem(
    val id: String = System.currentTimeMillis().toString(),
    val title: String = "",
    val category: String = "Général",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH
}
