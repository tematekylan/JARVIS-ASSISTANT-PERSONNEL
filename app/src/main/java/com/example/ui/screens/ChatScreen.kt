package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.MessageEntity
import com.example.ui.MainViewModel
import com.example.ui.components.HolographicCore
import com.example.ui.components.JarvisCommandBar
import com.example.ui.components.JarvisCoreState
import com.example.ui.components.MessageBubble
import com.example.ui.components.ThinkingBubble
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBgCard
import com.example.ui.theme.JarvisBgSurface
import com.example.ui.theme.JarvisBgVoid
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisBorderGlow
import com.example.ui.theme.JarvisCrimson
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun ChatScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val currentConversation by viewModel.currentConversation.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val coreState by viewModel.coreState.collectAsState()
    val audioAmplitude by viewModel.audioAmplitude.collectAsState()
    val isStreaming by viewModel.isStreaming.collectAsState()
    val streamingChunk by viewModel.streamingResponse.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val recognizedVoiceText by viewModel.recognizedVoiceText.collectAsState()

    // Sync speech recognized text into input bar
    LaunchedEffect(recognizedVoiceText) {
        if (recognizedVoiceText.isNotBlank()) {
            inputText = recognizedVoiceText
        }
    }

    // Auto-scroll to bottom whenever a new message or streaming chunk arrives
    val totalItemCount = messages.size + (if (isStreaming) 1 else 0)
    LaunchedEffect(totalItemCount, streamingChunk.length) {
        if (totalItemCount > 0) {
            listState.animateScrollToItem(totalItemCount - 1)
        }
    }

    val isChatEmpty = messages.isEmpty() && !isStreaming

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBgVoid)
    ) {
        // Top Section: Conditional HUD header
        if (!isChatEmpty) {
            // Compact, non-intrusive status header during conversation
            CompactChatHudBar(
                coreState = coreState,
                conversationTitle = currentConversation?.title ?: "Session Directe",
                onNewChat = { viewModel.startNewConversation() },
                onToggleVoice = {
                    if (coreState == JarvisCoreState.LISTENING) {
                        viewModel.stopListening()
                    } else if (coreState == JarvisCoreState.SPEAKING) {
                        viewModel.stopSpeaking()
                    } else {
                        viewModel.startListening()
                    }
                }
            )
        }

        // Middle Section: Messages Stream & History OR Welcome Screen
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (isChatEmpty) {
                // Empty State / Welcome Screen with central Holographic Core
                EmptyChatWelcome(
                    userName = viewModel.userSettings.collectAsState().value?.userName ?: "Sir",
                    coreState = coreState,
                    audioAmplitude = audioAmplitude,
                    onCoreClick = {
                        if (coreState == JarvisCoreState.LISTENING) {
                            viewModel.stopListening()
                        } else if (coreState == JarvisCoreState.SPEAKING) {
                            viewModel.stopSpeaking()
                        } else {
                            viewModel.startListening()
                        }
                    },
                    onPromptSelect = { prompt ->
                        viewModel.sendUserMessage(prompt, null)
                    }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        MessageBubble(
                            message = msg,
                            onSpeak = { text -> viewModel.speakText(text) }
                        )
                    }

                    // Live Streaming or Thinking Indicator
                    if (isStreaming) {
                        if (streamingChunk.isEmpty()) {
                            item(key = "thinking_indicator") {
                                ThinkingBubble()
                            }
                        } else {
                            item(key = "live_stream_bubble") {
                                MessageBubble(
                                    message = MessageEntity(
                                        conversationId = currentConversation?.id ?: 0,
                                        role = "assistant",
                                        content = streamingChunk,
                                        timestamp = System.currentTimeMillis()
                                    ),
                                    onSpeak = {},
                                    isLiveStreaming = true
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom: Command Bar with Slash shortcuts & Voice Controls
        JarvisCommandBar(
            inputText = inputText,
            onInputTextChange = { inputText = it },
            onSendMessage = { text, uri ->
                var bitmap: Bitmap? = null
                if (uri != null) {
                    try {
                        bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                        } else {
                            @Suppress("DEPRECATION")
                            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                        }
                    } catch (_: Exception) {}
                }
                viewModel.sendUserMessage(text, bitmap, uri?.toString())
                inputText = ""
            },
            isListening = coreState == JarvisCoreState.LISTENING,
            isSpeaking = coreState == JarvisCoreState.SPEAKING,
            isThinking = coreState == JarvisCoreState.THINKING,
            onToggleListen = {
                if (coreState == JarvisCoreState.LISTENING) {
                    viewModel.stopListening()
                } else {
                    viewModel.startListening()
                }
            },
            onStopAll = {
                viewModel.stopAll()
            },
            onQuickPrompt = { prompt ->
                viewModel.sendUserMessage(prompt, null)
            }
        )
    }
}

@Composable
fun CompactChatHudBar(
    coreState: JarvisCoreState,
    conversationTitle: String,
    onNewChat: () -> Unit,
    onToggleVoice: () -> Unit
) {
    val (statusLabel, statusColor) = when (coreState) {
        JarvisCoreState.IDLE -> "JARVIS // ACTIF" to JarvisCyan
        JarvisCoreState.LISTENING -> "ÉCOUTE EN DIRECT..." to JarvisEmerald
        JarvisCoreState.THINKING -> "RÉFLEXION NEURALE..." to JarvisCyanGlow
        JarvisCoreState.SPEAKING -> "PAROLE ACTIVE..." to JarvisBlue
        JarvisCoreState.ERROR -> "ALERTE SYSTÈME" to JarvisCrimson
    }

    Surface(
        color = Color(0xFF070F1E),
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .border(
                width = 0.5.dp,
                color = JarvisBorderGlow
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Status and Arc Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickable { onToggleVoice() }
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Text(
                    text = statusLabel,
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "• $conversationTitle",
                    color = JarvisTextMuted,
                    fontSize = 10.sp,
                    maxLines = 1,
                    modifier = Modifier.widthIn(max = 140.dp)
                )
            }

            // Quick Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onToggleVoice,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (coreState == JarvisCoreState.SPEAKING || coreState == JarvisCoreState.LISTENING) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Voice action",
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onNewChat,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Session",
                        tint = JarvisTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyChatWelcome(
    userName: String,
    coreState: JarvisCoreState,
    audioAmplitude: Float,
    onCoreClick: () -> Unit,
    onPromptSelect: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            HolographicCore(
                state = coreState,
                audioAmplitude = audioAmplitude,
                size = 115.dp,
                onClick = onCoreClick
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Text(
                text = "SYSTÈME JARVIS EN LIGNE",
                color = JarvisCyan,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Bonjour $userName. Tous les systèmes sont opérationnels.\nTouchez le réacteur pour parler ou saisissez une directive.",
                color = JarvisTextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Directives Cards
        item {
            WelcomePromptCard(
                title = "💬 /humain • Mode Conversation Naturelle",
                subtitle = "Parle comme un vrai humain, sans aucune formule robotique d'IA",
                onClick = { onPromptSelect("/humain Raconte-moi une anecdote passionnante sur l'ingénierie moderne comme si on était autour d'un café") }
            )
        }
        item {
            WelcomePromptCard(
                title = "🔬 /rayonx • Vue Éclatée & Ingénierie",
                subtitle = "Analyse interne des composants, moteur, châssis et électronique",
                onClick = { onPromptSelect("/rayonx Analyse les composants internes d'une supercar électrique") }
            )
        }
        item {
            WelcomePromptCard(
                title = "📋 /plan • Masterplan Exécutif de A à Z",
                subtitle = "Génère un plan directeur par étapes, jalons et gestion des risques",
                onClick = { onPromptSelect("/plan Stratégie de lancement d'une startup d'intelligence artificielle") }
            )
        }
        item {
            WelcomePromptCard(
                title = "⚡ Télémétrie & Mémoire Sécurisée",
                subtitle = "Vérifier le statut du réacteur Arc et sauvegarder des données",
                onClick = { onPromptSelect("Donne-moi le rapport télémétrique complet des systèmes") }
            )
        }
    }
}

@Composable
fun WelcomePromptCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = JarvisBgCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderGlow),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                color = JarvisCyanGlow,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = JarvisTextMuted,
                fontSize = 11.sp
            )
        }
    }
}
