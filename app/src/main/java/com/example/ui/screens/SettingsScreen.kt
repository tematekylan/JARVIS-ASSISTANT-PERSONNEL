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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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

    var aiProvider by remember(currentSettings.activeAiProvider) { mutableStateOf(currentSettings.activeAiProvider) }
    var openaiKey by remember(currentSettings.customOpenAiApiKey) { mutableStateOf(currentSettings.customOpenAiApiKey) }
    var claudeKey by remember(currentSettings.customClaudeApiKey) { mutableStateOf(currentSettings.customClaudeApiKey) }
    var groqKey by remember(currentSettings.customGroqApiKey) { mutableStateOf(currentSettings.customGroqApiKey) }
    var deepseekKey by remember(currentSettings.customDeepSeekApiKey) { mutableStateOf(currentSettings.customDeepSeekApiKey) }
    var customGeminiKey by remember(currentSettings.customGeminiApiKey) { mutableStateOf(currentSettings.customGeminiApiKey) }
    var keySaveFeedback by remember { mutableStateOf<String?>(null) }

    var devAlertEmail by remember(currentSettings.developerAlertEmail) { mutableStateOf(currentSettings.developerAlertEmail) }
    var autoAlerts by remember(currentSettings.autoSendErrorAlerts) { mutableStateOf(currentSettings.autoSendErrorAlerts) }
    var incidentTestFeedback by remember { mutableStateOf<String?>(null) }
    var isRunningIncidentTest by remember { mutableStateOf(false) }

    // Voice Persona & Character Imitation State
    val isAnalyzingVoice by viewModel.isAnalyzingVoice.collectAsState()
    var characterVoiceInput by remember { mutableStateOf("") }
    var voiceCloneFeedback by remember { mutableStateOf<String?>(null) }
    var voiceCloneError by remember { mutableStateOf<String?>(null) }

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

        // Section 0: User Profile & Security Clearance Authentication
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (currentSettings.isLoggedIn) JarvisEmerald.copy(alpha = 0.08f) else JarvisBgCard
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (currentSettings.isLoggedIn) JarvisEmerald.copy(alpha = 0.5f) else JarvisBorderGlow
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (currentSettings.isLoggedIn) JarvisEmerald.copy(alpha = 0.2f) else JarvisCyan.copy(alpha = 0.15f))
                                    .border(1.dp, if (currentSettings.isLoggedIn) JarvisEmerald else JarvisCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentSettings.userName.take(1).uppercase(),
                                    color = if (currentSettings.isLoggedIn) JarvisEmerald else JarvisCyan,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text(
                                    text = currentSettings.userName,
                                    color = JarvisTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (currentSettings.isLoggedIn) "AUTH: ${currentSettings.authProvider.uppercase()}" else "MODE INVITÉ",
                                    color = if (currentSettings.isLoggedIn) JarvisEmerald else JarvisAmber,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.showAuthDialog() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentSettings.isLoggedIn) JarvisBgSurface else JarvisCyan
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = if (currentSettings.isLoggedIn) androidx.compose.foundation.BorderStroke(1.dp, JarvisEmerald) else null
                        ) {
                            Text(
                                text = if (currentSettings.isLoggedIn) "GÉRER" else "CONNEXION",
                                color = if (currentSettings.isLoggedIn) JarvisEmerald else JarvisBgVoid,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (currentSettings.isLoggedIn) {
                        if (currentSettings.userEmail.isNotBlank()) {
                            Text(
                                text = "Email associé : ${currentSettings.userEmail}",
                                color = JarvisCyanGlow,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        if (currentSettings.userPhone.isNotBlank()) {
                            Text(
                                text = "Téléphone lié : ${currentSettings.userPhone}",
                                color = JarvisCyanGlow,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "Niveau d'habilitation : ${currentSettings.securityClearanceLevel}",
                            color = JarvisTextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        Text(
                            text = "Connectez votre compte Google, Email ou Téléphone pour sécuriser vos préférences et synchroniser vos données (100% gratuit, sans carte requise).",
                            color = JarvisTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        // Section 0.5: Holographic Quantum Ambient Mode (Always-On Display)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisCyan.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyanGlow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(JarvisCyanGlow)
                            )
                            Text(
                                text = "ÉCRAN DE VEILLE HOLOGRAPHIQUE (AOD)",
                                color = JarvisCyanGlow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Button(
                            onClick = { viewModel.navigateTo(com.example.ui.components.JarvisScreen.HOLOGRAPHIC_AOD) },
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "LANCER AOD",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Text(
                        text = "Affiche l'image holographique de T-HACK AI avec ses molécules quantiques en lévitation continue, horloge numérique, jauge de batterie et écoute vocale instantanée même en mode veille.",
                        color = JarvisTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
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
                        title = "Gemini 2.5 Flash (Recommandé • Vision & Texte)",
                        subtitle = "Rapidité extrême, analyse d'images multimodale & très faible latence",
                        isSelected = selectedModel == "gemini-2.5-flash",
                        onClick = {
                            selectedModel = "gemini-2.5-flash"
                            viewModel.updateUserSettings(currentSettings.copy(aiModel = "gemini-2.5-flash"))
                        }
                    )

                    ModelOptionRow(
                        title = "Gemini 2.5 Pro (Raisonnement Complexe)",
                        subtitle = "Capacités d'analyse approfondie, code expert et synthèse technique",
                        isSelected = selectedModel == "gemini-2.5-pro",
                        onClick = {
                            selectedModel = "gemini-2.5-pro"
                            viewModel.updateUserSettings(currentSettings.copy(aiModel = "gemini-2.5-pro"))
                        }
                    )

                    ModelOptionRow(
                        title = "Gemini 2.0 Flash (Ultra-Rapide)",
                        subtitle = "Optimisé pour des réponses quasi-instantanées",
                        isSelected = selectedModel == "gemini-2.0-flash",
                        onClick = {
                            selectedModel = "gemini-2.0-flash"
                            viewModel.updateUserSettings(currentSettings.copy(aiModel = "gemini-2.0-flash"))
                        }
                    )

                    ModelOptionRow(
                        title = "Gemini 1.5 Flash (Secours & Stabilité)",
                        subtitle = "Modèle de secours fiable et éprouvé",
                        isSelected = selectedModel == "gemini-1.5-flash",
                        onClick = {
                            selectedModel = "gemini-1.5-flash"
                            viewModel.updateUserSettings(currentSettings.copy(aiModel = "gemini-1.5-flash"))
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
                                text = "Exécute T-HACK AI localement avec les outils intégrés sans appel API externe.",
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

        // Section 2.5: Multi-AI Models & Custom API Keys (BYOK)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisBgCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "INTELLIGENCES ARTIFICIELLES & CLÉS API",
                            color = JarvisCyanGlow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(JarvisCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "MULTI-LLM",
                                color = JarvisCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Text(
                        text = "Vous pouvez connecter d'autres intelligences artificielles ou vos propres clés API pour débloquer OpenAI (GPT-4o), Anthropic (Claude 3.5), Groq (Llama 3.3) ou DeepSeek.",
                        color = JarvisTextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    // Active Provider Selector
                    Text(
                        text = "Fournisseur d'IA actif :",
                        color = JarvisTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            "gemini" to "Gemini",
                            "openai" to "OpenAI",
                            "claude" to "Claude",
                            "groq" to "Groq",
                            "deepseek" to "DeepSeek"
                        ).forEach { (provId, provName) ->
                            val isSel = (aiProvider == provId)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) JarvisCyan.copy(alpha = 0.25f) else JarvisBgSurface)
                                    .border(1.dp, if (isSel) JarvisCyan else JarvisBorderGlow, RoundedCornerShape(6.dp))
                                    .clickable {
                                        aiProvider = provId
                                        viewModel.updateUserSettings(currentSettings.copy(activeAiProvider = provId))
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = provName,
                                    color = if (isSel) JarvisCyanGlow else JarvisTextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Key fields
                    OutlinedTextField(
                        value = openaiKey,
                        onValueChange = { newVal ->
                            openaiKey = newVal
                            keySaveFeedback = null
                        },
                        label = { Text("Clé API OpenAI (sk-...) • GPT-4o & DALL-E 3", color = JarvisTextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorderGlow,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = claudeKey,
                        onValueChange = { newVal ->
                            claudeKey = newVal
                            keySaveFeedback = null
                        },
                        label = { Text("Clé API Anthropic Claude (sk-ant-...)", color = JarvisTextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorderGlow,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = groqKey,
                        onValueChange = { newVal ->
                            groqKey = newVal
                            keySaveFeedback = null
                        },
                        label = { Text("Clé API Groq (gsk_...) • Llama 3.3 Ultra-Rapide", color = JarvisTextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorderGlow,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = deepseekKey,
                        onValueChange = { newVal ->
                            deepseekKey = newVal
                            keySaveFeedback = null
                        },
                        label = { Text("Clé API DeepSeek (sk-...) • V3 & R1", color = JarvisTextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorderGlow,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = customGeminiKey,
                        onValueChange = { newVal ->
                            customGeminiKey = newVal
                            keySaveFeedback = null
                        },
                        label = { Text("Clé API Google Gemini Personnalisée (Optionnel)", color = JarvisTextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorderGlow,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        ),
                        singleLine = true
                    )

                    keySaveFeedback?.let { feedback ->
                        Text(
                            text = feedback,
                            color = JarvisEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.updateUserSettings(
                                currentSettings.copy(
                                    activeAiProvider = aiProvider,
                                    customOpenAiApiKey = openaiKey.trim(),
                                    customClaudeApiKey = claudeKey.trim(),
                                    customGroqApiKey = groqKey.trim(),
                                    customDeepSeekApiKey = deepseekKey.trim(),
                                    customGeminiApiKey = customGeminiKey.trim()
                                )
                            )
                            keySaveFeedback = "✓ Clés API enregistrées et synchronisées avec JARVIS."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ENREGISTRER LES CLÉS API",
                            color = JarvisBgVoid,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
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
                            viewModel.speakText("Tous les systèmes audio sont opérationnels, $userName. Calibrage standard terminé.")
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
                        Text("TESTER LA VOIX STANDARD", color = JarvisCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                }
            }
        }

        // Section 3.5: AI Voice Persona & Character Imitation (Gemini Acoustic Cloning)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (currentSettings.isVoicePersonaActive && currentSettings.voicePersonaName.isNotBlank())
                        JarvisCyan.copy(alpha = 0.08f) else JarvisBgCard
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (currentSettings.isVoicePersonaActive && currentSettings.voicePersonaName.isNotBlank())
                        JarvisCyanGlow else JarvisBorderGlow
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = JarvisCyanGlow,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "CLONAGE VOCAL IA & IMITATION",
                                color = JarvisCyanGlow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (currentSettings.voicePersonaName.isNotBlank()) {
                            Switch(
                                checked = currentSettings.isVoicePersonaActive,
                                onCheckedChange = { enabled ->
                                    viewModel.toggleVoicePersona(enabled)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = JarvisCyan,
                                    checkedTrackColor = JarvisCyan.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.testTag("switch_persona_active")
                            )
                        }
                    }

                    Text(
                        text = "Indiquez le nom d'un personnage, d'un artiste ou d'une célébrité. Gemini recherche ses caractéristiques acoustiques et expressives (timbre, tempo, intonation) pour que JARVIS vous réponde avec sa voix lors des échanges vocaux.",
                        color = JarvisTextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    // Input field & Analyze Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = characterVoiceInput,
                            onValueChange = {
                                characterVoiceInput = it
                                voiceCloneFeedback = null
                                voiceCloneError = null
                            },
                            placeholder = { Text("Ex: Morgan Freeman, Dark Vador, Son Goku, Batman...", color = JarvisTextMuted, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JarvisCyan,
                                unfocusedBorderColor = JarvisBorderGlow,
                                focusedTextColor = JarvisTextPrimary,
                                unfocusedTextColor = JarvisTextPrimary
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_character_voice")
                        )

                        Button(
                            onClick = {
                                if (characterVoiceInput.isNotBlank() && !isAnalyzingVoice) {
                                    voiceCloneFeedback = null
                                    voiceCloneError = null
                                    viewModel.analyzeAndCloneVoiceCharacter(characterVoiceInput) { success, msg ->
                                        if (success) {
                                            voiceCloneFeedback = msg
                                            characterVoiceInput = ""
                                        } else {
                                            voiceCloneError = msg
                                        }
                                    }
                                }
                            },
                            enabled = !isAnalyzingVoice && characterVoiceInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_clone_voice")
                        ) {
                            if (isAnalyzingVoice) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = JarvisBgVoid,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = JarvisBgVoid, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Popular Character Presets Chips
                    Text(
                        text = "Suggestions de personnages rapides :",
                        color = JarvisTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    val presets = listOf(
                        "Morgan Freeman",
                        "Dark Vador",
                        "Son Goku",
                        "Batman",
                        "Optimus Prime",
                        "Yoda",
                        "Tony Stark"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.take(4).forEach { presetName ->
                            Surface(
                                color = JarvisBgSurface,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderGlow),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(enabled = !isAnalyzingVoice) {
                                        characterVoiceInput = presetName
                                        voiceCloneFeedback = null
                                        voiceCloneError = null
                                        viewModel.analyzeAndCloneVoiceCharacter(presetName) { success, msg ->
                                            if (success) voiceCloneFeedback = msg else voiceCloneError = msg
                                        }
                                    }
                            ) {
                                Text(
                                    text = presetName,
                                    color = JarvisCyan,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.drop(4).forEach { presetName ->
                            Surface(
                                color = JarvisBgSurface,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderGlow),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(enabled = !isAnalyzingVoice) {
                                        characterVoiceInput = presetName
                                        voiceCloneFeedback = null
                                        voiceCloneError = null
                                        viewModel.analyzeAndCloneVoiceCharacter(presetName) { success, msg ->
                                            if (success) voiceCloneFeedback = msg else voiceCloneError = msg
                                        }
                                    }
                            ) {
                                Text(
                                    text = presetName,
                                    color = JarvisCyan,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Success Feedback
                    voiceCloneFeedback?.let { msg ->
                        Surface(
                            color = JarvisEmerald.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisEmerald),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = JarvisEmerald, modifier = Modifier.size(16.dp))
                                Text(text = msg, color = JarvisEmerald, fontSize = 11.sp)
                            }
                        }
                    }

                    // Error Feedback
                    voiceCloneError?.let { msg ->
                        Surface(
                            color = JarvisCrimson.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCrimson),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = JarvisCrimson, modifier = Modifier.size(16.dp))
                                Text(text = msg, color = JarvisCrimson, fontSize = 11.sp)
                            }
                        }
                    }

                    // Active Cloned Voice Details Card
                    if (currentSettings.voicePersonaName.isNotBlank()) {
                        Surface(
                            color = JarvisBgSurface,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyanGlow.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "PERSONNAGE : ${currentSettings.voicePersonaName.uppercase()}",
                                            color = JarvisCyanGlow,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Text(
                                        text = if (currentSettings.isVoicePersonaActive) "ACTIF" else "EN PAUSE",
                                        color = if (currentSettings.isVoicePersonaActive) JarvisEmerald else JarvisAmber,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                if (currentSettings.voicePersonaDescription.isNotBlank()) {
                                    Text(
                                        text = "Timbre acoustique : ${currentSettings.voicePersonaDescription}",
                                        color = JarvisTextPrimary,
                                        fontSize = 11.sp
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Pitch: ${String.format("%.2f", currentSettings.voicePersonaPitch)}x  |  Vitesse: ${String.format("%.2f", currentSettings.voicePersonaRate)}x",
                                        color = JarvisTextMuted,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Test button with this voice
                                    Button(
                                        onClick = {
                                            val testPhrase = when {
                                                currentSettings.voicePersonaName.contains("Vador", ignoreCase = true) || currentSettings.voicePersonaName.contains("Vader", ignoreCase = true) ->
                                                    "La force est puissante en vous, $userName. Je réponds désormais avec cette voix."
                                                currentSettings.voicePersonaName.contains("Freeman", ignoreCase = true) ->
                                                    "Bonjour $userName. L'univers tout entier est prêt à vous écouter."
                                                currentSettings.voicePersonaName.contains("Goku", ignoreCase = true) ->
                                                    "Salut c'est Goku ! Je suis super content de discuter avec toi $userName !"
                                                else ->
                                                    "Bonjour $userName, j'utilise maintenant la voix et le style de ${currentSettings.voicePersonaName} pour vous répondre."
                                            }
                                            viewModel.speakText(testPhrase)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan.copy(alpha = 0.2f)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ÉCOUTER", color = JarvisCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }

                                    // Reset button
                                    Button(
                                        onClick = {
                                            viewModel.resetVoicePersona()
                                            voiceCloneFeedback = "Voix standard rétablie."
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = JarvisBgVoid),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderGlow),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, tint = JarvisTextMuted, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("RÉINITIALISER", color = JarvisTextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Developer Diagnostics, Incident Alerts & AI Council
        item {
            val context = androidx.compose.ui.platform.LocalContext.current
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisBgCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(18.dp))
                            Text(
                                text = "ALERTES ERREURS & COLLÈGE D'IA",
                                color = JarvisCyanGlow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(JarvisEmerald.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "RÉSOLUTION AUTONOME",
                                color = JarvisEmerald,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Text(
                        text = "En cas d'anomalie, JARVIS n'affiche plus d'erreur brute au milieu de votre fil de discussion. Un groupe d'IA (Architecte, Débogueur, Ingénieur Patch) se réunit en arrière-plan pour trouver la solution, et un rapport complet est préparé pour votre adresse email.",
                        color = JarvisTextMuted,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )

                    // Email input field
                    OutlinedTextField(
                        value = devAlertEmail,
                        onValueChange = {
                            devAlertEmail = it
                            viewModel.updateUserSettings(currentSettings.copy(developerAlertEmail = it))
                        },
                        label = { Text("Adresse Email Développeur (Destinataire)", fontSize = 10.sp, color = JarvisCyan) },
                        placeholder = { Text("temateteddy@gmail.com", color = JarvisTextMuted, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorderGlow,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Toggle auto alert
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Envoi automatique des alertes", color = JarvisTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("Génère l'intention d'email et prépare le hotfix dès détection", color = JarvisTextMuted, fontSize = 9.sp)
                        }
                        Switch(
                            checked = autoAlerts,
                            onCheckedChange = {
                                autoAlerts = it
                                viewModel.updateUserSettings(currentSettings.copy(autoSendErrorAlerts = it))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = JarvisCyan,
                                checkedTrackColor = JarvisCyan.copy(alpha = 0.3f)
                            )
                        )
                    }

                    // Test Feedback
                    incidentTestFeedback?.let { msg ->
                        Surface(
                            color = JarvisEmerald.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisEmerald),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = JarvisEmerald, modifier = Modifier.size(16.dp))
                                Text(text = msg, color = JarvisEmerald, fontSize = 10.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Test Simulation Button
                        Button(
                            onClick = {
                                if (!isRunningIncidentTest) {
                                    isRunningIncidentTest = true
                                    incidentTestFeedback = null
                                    viewModel.triggerSimulatedIncident { incident ->
                                        isRunningIncidentTest = false
                                        incidentTestFeedback = "Incident #${incident.incidentCode} généré avec succès ! Le Collège d'IA a délibéré et préparé le hotfix."
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan.copy(alpha = 0.2f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isRunningIncidentTest) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = JarvisCyan, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(4.dp))
                            } else {
                                Icon(Icons.Default.BugReport, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text("TESTER LE COLLÈGE D'IA", color = JarvisCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        // Direct Email Button
                        Button(
                            onClick = {
                                val target = devAlertEmail.ifBlank { "temateteddy@gmail.com" }
                                val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                    data = android.net.Uri.parse("mailto:$target")
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "[JARVIS Diagnostic] Rapport d'état système")
                                    putExtra(
                                        android.content.Intent.EXTRA_TEXT,
                                        "Rapport technique JARVIS :\n- Utilisateur: $userName\n- Provider actif: $aiProvider\n- Modèle: ${currentSettings.aiModel}\n- Voix Personnage: ${currentSettings.voicePersonaName.ifBlank { "Standard" }}\n- Horodatage: ${java.util.Date()}\n\nTout fonctionne nominalement.\n"
                                    )
                                }
                                try {
                                    context.startActivity(android.content.Intent.createChooser(intent, "Envoyer le rapport"))
                                } catch (_: Exception) {
                                    android.widget.Toast.makeText(context, "Aucune application de messagerie trouvée.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisBgSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderGlow),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = JarvisTextSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("RAPPORT MANUEL", color = JarvisTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Section 5: Security & Factory Reset
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
                        text = "Efface l'historique complet des conversations, les notes, journaux d'outils et réinitialise T-HACK AI.",
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
