package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.ActivityItem
import com.example.ui.components.AssistantState
import com.example.ui.components.FuturisticAICore
import com.example.ui.components.FuturisticAudioWaveform
import com.example.ui.components.FuturisticCommandCenter
import com.example.ui.components.FuturisticHeader
import com.example.ui.components.HudPanel
import com.example.ui.components.MiniActivityFeed
import com.example.ui.components.SystemStatusPanel
import com.example.ui.components.toAssistantState
import com.example.ui.theme.HackBgBlack
import com.example.ui.theme.HackTextPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToChat: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coreStateLegacy by viewModel.coreState.collectAsState()
    val audioAmplitude by viewModel.audioAmplitude.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val streamingChunk by viewModel.streamingResponse.collectAsState()
    val isStreaming by viewModel.isStreaming.collectAsState()

    val assistantState: AssistantState = coreStateLegacy.toAssistantState()

    val scrollState = rememberScrollState()
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    var commandInput by remember { mutableStateOf("") }

    // Convert last messages to ActivityItem
    val activityItems: List<ActivityItem> = if (messages.isNotEmpty()) {
        messages.takeLast(4).reversed().map { msg ->
            ActivityItem(
                id = msg.id.toString(),
                time = timeFormatter.format(Date(msg.timestamp)),
                message = if (msg.role == "user") "DIRECTIVE: ${msg.content.take(35)}" else "T-HACK AI: ${msg.content.take(40)}...",
                status = if (msg.role == "user") "SYSTEM" else "SUCCESS"
            )
        }
    } else {
        listOf(
            ActivityItem("1", "09:00", "T-HACK AI Neural Core v3.5 Initialized", "SYSTEM"),
            ActivityItem("2", "09:01", "Base de données locale Room vérifiée", "SUCCESS"),
            ActivityItem("3", "09:02", "Synthétiseur acoustique opérationnel", "SUCCESS")
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HackBgBlack)
            .testTag("home_screen")
    ) {
        // Futuristic Top Header
        FuturisticHeader(
            systemStatusText = when (assistantState) {
                AssistantState.IDLE -> "CORE ONLINE // AWAITING DIRECTIVE"
                AssistantState.LISTENING -> "ACOUSTIC SENSORS ACTIVE // LISTENING..."
                AssistantState.THINKING -> "NEURAL QUANTUM COMPUTING..."
                AssistantState.PROCESSING -> "PROCESSING SYSTEM DIRECTIVE..."
                AssistantState.SPEAKING -> "AUDIO SYNTHESIZER ACTIVE"
                AssistantState.SUCCESS -> "OPERATION COMPLETED // STANDBY"
                AssistantState.ERROR -> "SYSTEM ALERT // SUBSYSTEM ANOMALY"
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Central Holographic AI Core
            Box(
                modifier = Modifier.padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                FuturisticAICore(
                    state = assistantState,
                    audioAmplitude = audioAmplitude,
                    size = 190.dp,
                    onClick = {
                        if (assistantState == AssistantState.LISTENING) {
                            viewModel.stopListening()
                        } else if (assistantState == AssistantState.SPEAKING) {
                            viewModel.stopSpeaking()
                        } else {
                            viewModel.startListening()
                        }
                    }
                )
            }

            // Real-time Waveform if active
            if (assistantState == AssistantState.LISTENING || assistantState == AssistantState.SPEAKING) {
                HudPanel(title = "ACOUSTIC WAVEFORM") {
                    FuturisticAudioWaveform(
                        isActive = true,
                        amplitude = audioAmplitude.coerceIn(0.1f, 1f)
                    )
                }
            }

            // Live streaming response preview if currently streaming
            if (isStreaming && streamingChunk.isNotBlank()) {
                HudPanel(title = "LIVE NEURAL STREAM // GEMINI-3.5-FLASH") {
                    Text(
                        text = streamingChunk,
                        color = HackTextPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }
            }

            // System Status Panel
            SystemStatusPanel(
                cpuUsage = 18,
                ramUsage = 38,
                batteryPercent = 90,
                networkStatus = "ONLINE // 5G SECURE",
                aiEngineStatus = "OPERATIONAL"
            )

            // Recent Activity HUD Feed
            MiniActivityFeed(
                activities = activityItems,
                onViewAllClick = onNavigateToChat
            )

            Spacer(modifier = Modifier.height(6.dp))
        }

        // Bottom Futuristic Command Center (Direct text input, send, mic, and stop controls)
        FuturisticCommandCenter(
            inputText = commandInput,
            onInputChange = { commandInput = it },
            onSendClick = {
                if (commandInput.isNotBlank()) {
                    viewModel.sendMessage(commandInput)
                    commandInput = ""
                }
            },
            isListening = assistantState == AssistantState.LISTENING,
            onMicClick = {
                if (assistantState == AssistantState.LISTENING) {
                    viewModel.stopListening()
                } else {
                    viewModel.startListening()
                }
            },
            isProcessing = isStreaming,
            onStopClick = {
                viewModel.stopSpeaking()
            },
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}
