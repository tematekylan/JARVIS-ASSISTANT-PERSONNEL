package com.thackman.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thackman.ai.model.AssistantState
import com.thackman.ai.ui.components.AICoreOrb
import com.thackman.ai.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateChat: () -> Unit,
    onNavigateCommandCenter: () -> Unit,
    onNavigateTasks: () -> Unit
) {
    val state by viewModel.assistantState.collectAsState()
    val amplitude by viewModel.audioAmplitude.collectAsState()
    var inputQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030609))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Status Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFF0A1219),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF007C91).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF00E5FF), shape = RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "T-HACKMAN ONLINE",
                        color = Color(0xFFE5FCFF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row {
                IconButton(onClick = onNavigateTasks) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Tâches",
                        tint = Color(0xFF31F5A3)
                    )
                }
                IconButton(onClick = onNavigateCommandCenter) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Centre de contrôle",
                        tint = Color(0xFF00E5FF)
                    )
                }
            }
        }

        // Center: Hologram Orb & Greeting
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AICoreOrb(
                state = state,
                amplitude = amplitude,
                onClick = { viewModel.toggleVoiceRecognition() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "T-HACKMAN AI",
                color = Color(0xFFE5FCFF),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Assistant Cybernétique Autonome",
                color = Color(0xFF6F9DA6),
                fontSize = 13.sp
            )
        }

        // Bottom: Input Box with Voice Trigger
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF0D1821),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF007C91).copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.toggleVoiceRecognition() }) {
                    Icon(
                        imageVector = if (state == AssistantState.LISTENING) Icons.Default.Mic else Icons.Default.MicNone,
                        contentDescription = "Micro",
                        tint = if (state == AssistantState.LISTENING) Color(0xFFFF4660) else Color(0xFF00E5FF)
                    )
                }

                TextField(
                    value = inputQuery,
                    onValueChange = { inputQuery = it },
                    placeholder = { Text("Donnez un ordre ou posez une question...", color = Color(0xFF5B7B88), fontSize = 13.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color(0xFFE5FCFF),
                        unfocusedTextColor = Color(0xFFE5FCFF)
                    ),
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        if (inputQuery.isNotBlank()) {
                            viewModel.executeCommand(inputQuery)
                            inputQuery = ""
                            onNavigateChat()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Envoyer",
                        tint = Color(0xFF00E5FF)
                    )
                }
            }
        }
    }
}
