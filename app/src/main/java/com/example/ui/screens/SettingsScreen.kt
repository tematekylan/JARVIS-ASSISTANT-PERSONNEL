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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.data.entity.UserSettingsEntity
import com.example.ui.MainViewModel
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
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val userSettings by viewModel.userSettings.collectAsState()
    val currentSettings = userSettings ?: UserSettingsEntity()

    var userName by remember(currentSettings.userName) { mutableStateOf(currentSettings.userName) }
    var selectedModel by remember(currentSettings.aiModel) { mutableStateOf(currentSettings.aiModel) }
    var speechRate by remember(currentSettings.speechRate) { mutableFloatStateOf(currentSettings.speechRate) }
    var speechPitch by remember(currentSettings.speechPitch) { mutableFloatStateOf(currentSettings.speechPitch) }
    var autoSpeak by remember(currentSettings.autoSpeakResponses) { mutableStateOf(currentSettings.autoSpeakResponses) }
    var demoMode by remember(currentSettings.isDemoMode) { mutableStateOf(currentSettings.isDemoMode) }
    var personality by remember(currentSettings.personalityTone) { mutableStateOf(currentSettings.personalityTone) }

    var showFactoryResetDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBgVoid)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Text(
                text = "SYSTEM CONFIGURATION // SETTINGS",
                color = JarvisCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )
        }

        // Section 1: User Identity & Callsign
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisBgCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderGlow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "COMMANDER IDENTITY & CALLSIGN",
                        color = JarvisCyanGlow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    OutlinedTextField(
                        value = userName,
                        onValueChange = {
                            userName = it
                            viewModel.updateUserSettings(currentSettings.copy(userName = it))
                        },
                        label = { Text("Callsign / Name", color = JarvisTextMuted, fontSize = 11.sp) },
                        placeholder = { Text("Sir, Boss, Commander, Alex", color = JarvisTextMuted, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorderGlow,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_username_field")
                    )
                }
            }
        }

        // Section 2: AI Neural Engine Model
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisBgCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderGlow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "NEURAL AI ENGINE SELECTION",
                        color = JarvisCyanGlow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    ModelOptionRow(
                        title = "Gemini 3.5 Flash (Recommandé)",
                        subtitle = "Rapidité extrême, raisonnement multimodal & faible latence",
                        isSelected = selectedModel == "gemini-3.5-flash",
                        onClick = {
                            selectedModel = "gemini-3.5-flash"
                            viewModel.updateUserSettings(currentSettings.copy(aiModel = "gemini-3.5-flash"))
                        }
                    )

                    ModelOptionRow(
                        title = "Gemini 3.1 Pro",
                        subtitle = "Raisonnement complexe avancé et synthèse approfondie",
                        isSelected = selectedModel == "gemini-3.1-pro-preview",
                        onClick = {
                            selectedModel = "gemini-3.1-pro-preview"
                            viewModel.updateUserSettings(currentSettings.copy(aiModel = "gemini-3.1-pro-preview"))
                        }
                    )

                    ModelOptionRow(
                        title = "Gemini 2.5 Flash Image",
                        subtitle = "Optimisé pour la vision et l'analyse visuelle",
                        isSelected = selectedModel == "gemini-2.5-flash-image",
                        onClick = {
                            selectedModel = "gemini-2.5-flash-image"
                            viewModel.updateUserSettings(currentSettings.copy(aiModel = "gemini-2.5-flash-image"))
                        }
                    )

                    // Demo Mode Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mode Démo / Hors-Ligne",
                                color = JarvisTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Exécute JARVIS localement avec les outils intégrés sans appel API externe.",
                                color = JarvisTextMuted,
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = demoMode,
                            onCheckedChange = { enabled ->
                                demoMode = enabled
                                viewModel.updateUserSettings(currentSettings.copy(isDemoMode = enabled))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = JarvisAmber,
                                checkedTrackColor = JarvisAmber.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("switch_demo_mode")
                        )
                    }
                }
            }
        }

        // Section 3: Voice Synthesizer Settings
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisBgCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderGlow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "SYNTHÈSE VOCALE & PARAMÈTRES AUDIO",
                        color = JarvisCyanGlow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    // Auto speak switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Lecture vocale automatique", color = JarvisTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("JARVIS lit automatiquement ses réponses à haute voix.", color = JarvisTextMuted, fontSize = 10.sp)
                        }
                        Switch(
                            checked = autoSpeak,
                            onCheckedChange = { enabled ->
                                autoSpeak = enabled
                                viewModel.updateUserSettings(currentSettings.copy(autoSpeakResponses = enabled))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = JarvisCyan,
                                checkedTrackColor = JarvisCyan.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("switch_auto_speak")
                        )
                    }

                    // Speech Rate Slider
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Vitesse de parole", color = JarvisTextSecondary, fontSize = 11.sp)
                            Text("${String.format("%.2f", speechRate)}x", color = JarvisCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                        Slider(
                            value = speechRate,
                            onValueChange = {
                                speechRate = it
                                viewModel.updateUserSettings(currentSettings.copy(speechRate = it))
                            },
                            valueRange = 0.6f..1.6f,
                            colors = SliderDefaults.colors(
                                thumbColor = JarvisCyan,
                                activeTrackColor = JarvisCyan,
                                inactiveTrackColor = JarvisBgSurface
                            ),
                            modifier = Modifier.testTag("slider_speech_rate")
                        )
                    }

                    // Pitch Slider
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tonalité (Pitch)", color = JarvisTextSecondary, fontSize = 11.sp)
                            Text("${String.format("%.2f", speechPitch)}x", color = JarvisCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                        Slider(
                            value = speechPitch,
                            onValueChange = {
                                speechPitch = it
                                viewModel.updateUserSettings(currentSettings.copy(speechPitch = it))
                            },
                            valueRange = 0.6f..1.5f,
                            colors = SliderDefaults.colors(
                                thumbColor = JarvisCyan,
                                activeTrackColor = JarvisCyan,
                                inactiveTrackColor = JarvisBgSurface
                            ),
                            modifier = Modifier.testTag("slider_speech_pitch")
                        )
                    }

                    // Test Voice Button
                    Button(
                        onClick = {
                            viewModel.speakText("Tous les systèmes audio sont opérationnels, $userName. Calibrage terminé.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan.copy(alpha = 0.2f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_test_voice")
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("TESTER LA VOIX DE JARVIS", color = JarvisCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                }
            }
        }

        // Section 4: Security & Factory Reset
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisBgCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCrimson.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "SÉCURITÉ & RÉINITIALISATION",
                        color = JarvisCrimson,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Efface l'historique complet des conversations, les notes, journaux d'outils et réinitialise JARVIS.",
                        color = JarvisTextMuted,
                        fontSize = 11.sp
                    )

                    Button(
                        onClick = { showFactoryResetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCrimson),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_factory_reset")
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("RÉINITIALISER LE SYSTÈME", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                }
            }
        }
    }

    if (showFactoryResetDialog) {
        AlertDialog(
            onDismissRequest = { showFactoryResetDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = JarvisCrimson) },
            title = {
                Text("PURGE TOTALE DU SYSTÈME", color = JarvisCrimson, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            },
            text = {
                Text(
                    "Êtes-vous certain de vouloir purger toutes les données ? Cette opération est irréversible et supprimera vos conversations, messages et journaux.",
                    color = JarvisTextPrimary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.factoryReset()
                        showFactoryResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCrimson)
                ) {
                    Text("CONFIRMER LA PURGE", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFactoryResetDialog = false }) {
                    Text("ANNULER", color = JarvisTextSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            },
            containerColor = JarvisBgCard,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun ModelOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) JarvisCyan.copy(alpha = 0.15f) else JarvisBgSurface,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) JarvisCyan else JarvisBorderGlow
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) JarvisCyan else Color.Transparent)
                    .border(2.dp, if (isSelected) JarvisCyan else JarvisTextMuted, CircleShape)
            )
            Column {
                Text(
                    text = title,
                    color = if (isSelected) JarvisCyan else JarvisTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = subtitle,
                    color = JarvisTextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}
