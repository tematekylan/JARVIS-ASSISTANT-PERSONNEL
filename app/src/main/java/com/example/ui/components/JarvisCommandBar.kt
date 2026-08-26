package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBgCard
import com.example.ui.theme.JarvisBgCardElevated
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

@Composable
fun JarvisCommandBar(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    onSendMessage: (String, Uri?) -> Unit,
    isListening: Boolean,
    isSpeaking: Boolean,
    isThinking: Boolean,
    onToggleListen: () -> Unit,
    onStopAll: () -> Unit,
    onQuickPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val micScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        JarvisBgVoid.copy(alpha = 0.85f),
                        JarvisBgVoid
                    )
                )
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Quick Action Directives
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickChip(label = "⚡ Statut Système", onClick = { onQuickPrompt("Donne-moi le statut complet du système JARVIS") })
            QuickChip(label = "🌤️ Météo Paris", onClick = { onQuickPrompt("Quelle est la météo actuelle à Paris ?") })
            QuickChip(label = "🧮 25 × 37", onClick = { onQuickPrompt("Combien font 25 * 37 ?") })
            QuickChip(label = "🧠 Mes Souvenirs", onClick = { onQuickPrompt("Quels souvenirs as-tu enregistrés dans ta mémoire ?") })
            QuickChip(label = "📝 Prendre Note", onClick = { onQuickPrompt("Note: Rendez-vous technique demain à 14h") })
            QuickChip(label = "🌐 Actualités IA", onClick = { onQuickPrompt("Recherche les dernières actualités sur l'intelligence artificielle") })
        }

        // Image Attachment Preview
        AnimatedVisibility(visible = selectedImageUri != null) {
            Box(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, JarvisCyan, RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Selected image preview",
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = { selectedImageUri = null },
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.TopEnd)
                        .background(JarvisBgVoid.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove image",
                        tint = JarvisCrimson,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Main Input Bar Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Attachment Button
            IconButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(JarvisBgCard)
                    .border(1.dp, JarvisBorderGlow, CircleShape)
                    .testTag("btn_attach_image")
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attach image or document",
                    tint = if (selectedImageUri != null) JarvisCyan else JarvisTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Text Input Field
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputTextChange,
                placeholder = {
                    Text(
                        text = if (isListening) "Écoute en direct..." else "Écrivez une commande à JARVIS...",
                        color = if (isListening) JarvisEmerald else JarvisTextMuted,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("command_input_field"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JarvisCyan,
                    unfocusedBorderColor = JarvisBorderGlow,
                    focusedTextColor = JarvisTextPrimary,
                    unfocusedTextColor = JarvisTextPrimary,
                    focusedContainerColor = JarvisBgCardElevated,
                    unfocusedContainerColor = JarvisBgCard
                ),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (inputText.isNotBlank() || selectedImageUri != null) {
                            onSendMessage(inputText, selectedImageUri)
                            selectedImageUri = null
                        }
                    }
                )
            )

            // Dynamic Action Button (Stop vs Mic vs Send)
            if (isSpeaking || isThinking) {
                // STOP Button
                IconButton(
                    onClick = onStopAll,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(JarvisCrimson.copy(alpha = 0.2f))
                        .border(1.5.dp, JarvisCrimson, CircleShape)
                        .testTag("btn_stop_action")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Interrupt JARVIS",
                        tint = JarvisCrimson,
                        modifier = Modifier.size(22.dp)
                    )
                }
            } else if (inputText.isNotBlank() || selectedImageUri != null) {
                // SEND Button
                IconButton(
                    onClick = {
                        onSendMessage(inputText, selectedImageUri)
                        selectedImageUri = null
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(JarvisCyan.copy(alpha = 0.25f))
                        .border(1.5.dp, JarvisCyan, CircleShape)
                        .testTag("btn_send_message")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send directive",
                        tint = JarvisCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                // MICROPHONE Button
                val micColor = if (isListening) JarvisEmerald else JarvisCyan
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .then(
                            if (isListening) Modifier.scale(micScale)
                            else Modifier
                        )
                ) {
                    IconButton(
                        onClick = onToggleListen,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(micColor.copy(alpha = if (isListening) 0.35f else 0.15f))
                            .border(1.5.dp, micColor, CircleShape)
                            .testTag("btn_mic_action")
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Mic else Icons.Default.Mic,
                            contentDescription = "Voice Directive",
                            tint = micColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickChip(label: String, onClick: () -> Unit) {
    Surface(
        color = JarvisBgCard,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderGlow),
        modifier = Modifier
            .clickable { onClick() }
            .testTag("chip_$label")
    ) {
        Text(
            text = label,
            color = JarvisTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}
