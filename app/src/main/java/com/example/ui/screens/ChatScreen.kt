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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.data.entity.ConversationEntity
import com.example.data.entity.MessageEntity
import com.example.ui.MainViewModel
import com.example.ui.components.HolographicCore
import com.example.ui.components.JarvisCommandBar
import com.example.ui.components.JarvisCoreState
import com.example.ui.components.MessageBubble
import com.example.ui.theme.JarvisBgCard
import com.example.ui.theme.JarvisBgSurface
import com.example.ui.theme.JarvisBgVoid
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisBorderGlow
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
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

    // Auto-scroll when messages change or streaming updates
    LaunchedEffect(messages.size, streamingChunk) {
        if (messages.isNotEmpty() || streamingChunk.isNotEmpty()) {
            listState.animateScrollToItem((messages.size).coerceAtLeast(0))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBgVoid)
    ) {
        // Upper Center: Holographic Reactor Core
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            HolographicCore(
                state = coreState,
                audioAmplitude = audioAmplitude,
                size = 140.dp,
                onClick = {
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

        // Middle: Message Stream & History
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty() && streamingChunk.isEmpty()) {
                // Empty State / Welcome Screen
                EmptyChatWelcome(
                    userName = viewModel.userSettings.collectAsState().value?.userName ?: "Sir",
                    onPromptSelect = { prompt ->
                        viewModel.sendUserMessage(prompt, null)
                    }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        MessageBubble(
                            message = msg,
                            onSpeak = { text -> viewModel.speakText(text) }
                        )
                    }

                    // Live Streaming Chunk Bubble
                    if (isStreaming && streamingChunk.isNotEmpty()) {
                        item {
                            MessageBubble(
                                message = MessageEntity(
                                    conversationId = currentConversation?.id ?: 0,
                                    role = "assistant",
                                    content = streamingChunk,
                                    timestamp = System.currentTimeMillis()
                                ),
                                onSpeak = {}
                            )
                        }
                    }
                }
            }
        }

        // Bottom: Command Bar & Voice Controls
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
fun EmptyChatWelcome(
    userName: String,
    onPromptSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(JarvisCyan.copy(alpha = 0.1f))
                .border(1.dp, JarvisCyan.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = JarvisCyan,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "SYSTÈME INITIALISÉ",
            color = JarvisCyan,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Bonjour $userName. Tous les systèmes sont opérationnels.\nParlez ou tapez une instruction pour débuter.",
            color = JarvisTextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Example Action Cards
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            WelcomePromptCard(
                title = "⚡ Diagnostics & Télémétrie",
                subtitle = "Vérifier l'état de charge, mémoire et liaison réseau",
                onClick = { onPromptSelect("Donne-moi le rapport télémétrique complet des systèmes") }
            )
            WelcomePromptCard(
                title = "🧠 Mémoire Persistante",
                subtitle = "Enregistrer une préférence ou un souvenir dans le coffre",
                onClick = { onPromptSelect("Rappelle-toi que je prépare une application mobile futuriste") }
            )
            WelcomePromptCard(
                title = "🌤️ Conditions Météo Mondiales",
                subtitle = "Obtenir les prévisions atmosphériques de Paris ou Tokyo",
                onClick = { onPromptSelect("Quelle est la météo en direct à Paris ?") }
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
