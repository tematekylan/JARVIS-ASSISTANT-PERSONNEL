package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FuturisticHeader
import com.example.ui.components.HudPanel
import com.example.ui.theme.HackBgBlack
import com.example.ui.theme.HackBlueAccent
import com.example.ui.theme.HackCyanDark
import com.example.ui.theme.HackCyanLight
import com.example.ui.theme.HackCyanPrimary
import com.example.ui.theme.HackError
import com.example.ui.theme.HackPanelDark
import com.example.ui.theme.HackSuccess
import com.example.ui.theme.HackTextPrimary
import com.example.ui.theme.HackTextSecondary

data class TaskItem(
    val id: String,
    val title: String,
    val time: String,
    val category: String, // Rappel, Système, Analyse
    val isCompleted: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    modifier: Modifier = Modifier
) {
    val tasks = remember {
        mutableStateListOf(
            TaskItem("1", "Vérifier la connectivité neuronale de T-HACK AI", "09:00", "Système", true),
            TaskItem("2", "Synchroniser la mémoire à long terme", "11:30", "Analyse", false),
            TaskItem("3", "Rappel : Réunion de calibrage de l'IA", "14:00", "Rappel", false),
            TaskItem("4", "Optimiser le cache audio et les modèles de synthèse vocale", "16:45", "Système", false)
        )
    }

    var selectedTask by remember { mutableStateOf<TaskItem?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskCategory by remember { mutableStateOf("Système") }

    val activeCount = tasks.count { !it.isCompleted }
    val completedCount = tasks.count { it.isCompleted }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HackBgBlack)
            .testTag("task_screen")
    ) {
        FuturisticHeader(systemStatusText = "TASK CENTER")

        Column(modifier = Modifier.padding(16.dp)) {
            // Counters and "+ NEW TASK" button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column {
                        Text("ACTIVE TASKS", color = HackTextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("$activeCount", color = HackCyanPrimary, fontSize = 18.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("COMPLETED", color = HackTextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("$completedCount", color = HackSuccess, fontSize = 18.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }

                // "+ NEW TASK" HUD button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(HackCyanPrimary)
                        .border(1.dp, HackCyanLight, RoundedCornerShape(4.dp))
                        .clickable { showCreateDialog = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("new_task_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = HackBgBlack, modifier = Modifier.size(14.dp))
                        Text(
                            text = "+ NEW TASK",
                            color = HackBgBlack,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Task List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(tasks) { task ->
                    TaskRowCard(
                        task = task,
                        onToggle = {
                            val idx = tasks.indexOfFirst { it.id == task.id }
                            if (idx != -1) {
                                tasks[idx] = task.copy(isCompleted = !task.isCompleted)
                            }
                        },
                        onClick = { selectedTask = task }
                    )
                }
            }
        }
    }

    // Task Detail Modal (Page 23)
    selectedTask?.let { task ->
        BasicAlertDialog(
            onDismissRequest = { selectedTask = null }
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(HackPanelDark)
                    .border(1.5.dp, HackCyanPrimary, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TASK DETAILS // ${task.category.uppercase()}",
                            color = HackCyanLight,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = HackTextSecondary,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { selectedTask = null }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = task.title,
                        color = HackTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Time: ${task.time} • Status: ${if (task.isCompleted) "COMPLETED" else "ACTIVE"}",
                        color = HackTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons COMPLETE, DELETE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (task.isCompleted) HackPanelDark else HackSuccess)
                                .border(1.dp, HackSuccess, RoundedCornerShape(4.dp))
                                .clickable {
                                    val idx = tasks.indexOfFirst { it.id == task.id }
                                    if (idx != -1) {
                                        tasks[idx] = task.copy(isCompleted = !task.isCompleted)
                                    }
                                    selectedTask = null
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (task.isCompleted) "MARK ACTIVE" else "COMPLETE",
                                color = if (task.isCompleted) HackSuccess else HackBgBlack,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(HackError.copy(alpha = 0.15f))
                                .border(1.dp, HackError, RoundedCornerShape(4.dp))
                                .clickable {
                                    tasks.removeAll { it.id == task.id }
                                    selectedTask = null
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "DELETE",
                                color = HackError,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // New Task Dialog
    if (showCreateDialog) {
        BasicAlertDialog(onDismissRequest = { showCreateDialog = false }) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(HackPanelDark)
                    .border(1.5.dp, HackCyanPrimary, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "NEW TASK // DIRECTIVE",
                        color = HackCyanPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("Titre de la tâche", color = HackTextSecondary, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = HackTextPrimary,
                            unfocusedTextColor = HackTextPrimary,
                            focusedBorderColor = HackCyanPrimary,
                            unfocusedBorderColor = HackCyanDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(HackCyanPrimary)
                                .clickable {
                                    if (newTaskTitle.isNotBlank()) {
                                        tasks.add(
                                            TaskItem(
                                                id = System.currentTimeMillis().toString(),
                                                title = newTaskTitle,
                                                time = "Maintenant",
                                                category = newTaskCategory,
                                                isCompleted = false
                                            )
                                        )
                                        newTaskTitle = ""
                                        showCreateDialog = false
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("SAVE TASK", color = HackBgBlack, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskRowCard(
    task: TaskItem,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(HackPanelDark.copy(alpha = 0.85f))
            .border(1.dp, if (task.isCompleted) HackSuccess.copy(alpha = 0.3f) else HackCyanDark.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Check circle
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (task.isCompleted) HackSuccess else Color.Transparent)
                    .border(1.5.dp, if (task.isCompleted) HackSuccess else HackCyanLight, CircleShape)
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = HackBgBlack, modifier = Modifier.size(13.dp))
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = if (task.isCompleted) HackTextSecondary else HackTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = task.category.uppercase(),
                        color = HackCyanLight,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "• ${task.time}",
                        color = HackTextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
