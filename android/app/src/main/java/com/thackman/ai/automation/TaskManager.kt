package com.thackman.ai.automation

import com.thackman.ai.model.TaskItem
import com.thackman.ai.model.TaskPriority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TaskManager private constructor() {
    private val _tasks = MutableStateFlow<List<TaskItem>>(
        listOf(
            TaskItem(id = "1", title = "Vérification des protocoles de sécurité", priority = TaskPriority.HIGH, isCompleted = true),
            TaskItem(id = "2", title = "Synchronisation avec le cloud Firebase", priority = TaskPriority.MEDIUM, isCompleted = true),
            TaskItem(id = "3", title = "Surveillance télémétrique autonome", priority = TaskPriority.HIGH, isCompleted = false)
        )
    )
    val tasks = _tasks.asStateFlow()

    fun addTask(title: String, category: String = "Général", priority: String = "MEDIUM") {
        val prio = when (priority.uppercase()) {
            "HIGH" -> TaskPriority.HIGH
            "LOW" -> TaskPriority.LOW
            else -> TaskPriority.MEDIUM
        }
        val newTask = TaskItem(
            id = System.currentTimeMillis().toString(),
            title = title,
            category = category,
            priority = prio,
            isCompleted = false
        )
        _tasks.value = listOf(newTask) + _tasks.value
    }

    fun toggleTask(id: String) {
        _tasks.value = _tasks.value.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
    }

    fun deleteTask(id: String) {
        _tasks.value = _tasks.value.filterNot { it.id == id }
    }

    companion object {
        @Volatile
        private var instance: TaskManager? = null

        fun getInstance(): TaskManager {
            return instance ?: synchronized(this) {
                instance ?: TaskManager().also { instance = it }
            }
        }
    }
}
