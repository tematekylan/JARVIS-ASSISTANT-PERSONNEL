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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.entity.MemoryEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBgCard
import com.example.ui.theme.JarvisBgSurface
import com.example.ui.theme.JarvisBgVoid
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisBorderBright
import com.example.ui.theme.JarvisBorderGlow
import com.example.ui.theme.JarvisCrimson
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MemoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val memories by viewModel.memories.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showWipeConfirmDialog by remember { mutableStateOf(false) }
    var editingMemory by remember { mutableStateOf<MemoryEntity?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBgVoid)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header & Memory Engine Control Switch
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = JarvisBgCard),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderGlow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = JarvisEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "MEMORY VAULT ENGINE",
                                    color = JarvisCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Enables JARVIS to recall user preferences, facts and directives across sessions.",
                                color = JarvisTextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Switch(
                            checked = userSettings?.memoryEnabled ?: true,
                            onCheckedChange = { enabled ->
                                userSettings?.let {
                                    viewModel.updateUserSettings(it.copy(memoryEnabled = enabled))
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = JarvisEmerald,
                                checkedTrackColor = JarvisEmerald.copy(alpha = 0.3f),
                                uncheckedThumbColor = JarvisTextMuted,
                                uncheckedTrackColor = JarvisBgSurface
                            ),
                            modifier = Modifier.testTag("switch_memory_engine")
                        )
                    }
                }
            }

            // Quick Stats & Wipe All Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STORED MEMORIES (${memories.size})",
                        color = JarvisCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    if (memories.isNotEmpty()) {
                        TextButton(
                            onClick = { showWipeConfirmDialog = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = JarvisCrimson),
                            modifier = Modifier.testTag("btn_wipe_all_memories")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WIPE VAULT", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (memories.isEmpty()) {
                item {
                    EmptyMemoryPlaceholder(onAddClick = { showAddDialog = true })
                }
            } else {
                items(memories, key = { it.id }) { memory ->
                    MemoryItemCard(
                        memory = memory,
                        onToggleActive = {
                            viewModel.updateMemory(memory.copy(isActive = !memory.isActive))
                        },
                        onEdit = {
                            editingMemory = memory
                        },
                        onDelete = {
                            viewModel.deleteMemory(memory.id)
                        }
                    )
                }
            }
        }

        // Floating Action Button to Add Memory
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = JarvisCyan,
            contentColor = JarvisBgVoid,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_memory")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add New Memory")
        }
    }

    // Add Memory Dialog
    if (showAddDialog) {
        AddOrEditMemoryDialog(
            memory = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { key, content, category ->
                viewModel.saveMemory(key = key, content = content, category = category)
                showAddDialog = false
            }
        )
    }

    // Edit Memory Dialog
    if (editingMemory != null) {
        AddOrEditMemoryDialog(
            memory = editingMemory,
            onDismiss = { editingMemory = null },
            onConfirm = { key, content, category ->
                editingMemory?.let {
                    viewModel.updateMemory(it.copy(key = key, content = content, category = category))
                }
                editingMemory = null
            }
        )
    }

    // Wipe All Confirmation Dialog
    if (showWipeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showWipeConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = JarvisCrimson) },
            title = {
                Text(
                    text = "CONFIRM MEMORY PURGE",
                    color = JarvisCrimson,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            },
            text = {
                Text(
                    text = "Cette action supprimera définitivement tous les souvenirs enregistrés par JARVIS. Confirmez-vous la purge intégrale ?",
                    color = JarvisTextPrimary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllMemories()
                        showWipeConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCrimson)
                ) {
                    Text("CONFIRMER LA PURGE", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeConfirmDialog = false }) {
                    Text("ANNULER", color = JarvisTextSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            },
            containerColor = JarvisBgCard,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun MemoryItemCard(
    memory: MemoryEntity,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = JarvisBgCard),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (memory.isActive) JarvisBorderGlow else JarvisBgSurface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (memory.isActive) JarvisEmerald else JarvisTextMuted)
                    )
                    Text(
                        text = memory.category.uppercase(),
                        color = JarvisCyanGlow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "• ${memory.key}",
                        color = JarvisTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = JarvisTextMuted, modifier = Modifier.size(14.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = JarvisCrimson, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = memory.content,
                color = if (memory.isActive) JarvisTextPrimary else JarvisTextMuted,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(memory.timestamp))
            Text(
                text = "Recorded: $dateStr",
                color = JarvisTextMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun AddOrEditMemoryDialog(
    memory: MemoryEntity?,
    onDismiss: () -> Unit,
    onConfirm: (key: String, content: String, category: String) -> Unit
) {
    var key by remember { mutableStateOf(memory?.key ?: "") }
    var content by remember { mutableStateOf(memory?.content ?: "") }
    var category by remember { mutableStateOf(memory?.category ?: "Preferences") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (memory == null) "NEW MEMORY RECORD" else "EDIT MEMORY RECORD",
                color = JarvisCyan,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("Key / Title", color = JarvisTextMuted, fontSize = 11.sp) },
                    placeholder = { Text("e.g. Favorite Language, Project Focus", color = JarvisTextMuted, fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorderGlow,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Fact / Memory Content", color = JarvisTextMuted, fontSize = 11.sp) },
                    placeholder = { Text("e.g. User prefers Kotlin for Android and Python for AI", color = JarvisTextMuted, fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorderGlow,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary
                    ),
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category", color = JarvisTextMuted, fontSize = 11.sp) },
                    placeholder = { Text("Preferences, Work, Identity, General", color = JarvisTextMuted, fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisBorderGlow,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (key.isNotBlank() && content.isNotBlank()) {
                        onConfirm(key.trim(), content.trim(), category.trim().ifBlank { "General" })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
            ) {
                Text(
                    text = if (memory == null) "SAVE TO VAULT" else "UPDATE RECORD",
                    color = JarvisBgVoid,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = JarvisTextSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
        },
        containerColor = JarvisBgCard,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun EmptyMemoryPlaceholder(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = null,
            tint = JarvisTextMuted,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "NO ACTIVE MEMORIES IN VAULT",
            color = JarvisTextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Tell JARVIS to remember something during chat,\nor add a custom directive manually.",
            color = JarvisTextMuted,
            fontSize = 11.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan.copy(alpha = 0.2f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("ADD FIRST MEMORY", color = JarvisCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        }
    }
}
