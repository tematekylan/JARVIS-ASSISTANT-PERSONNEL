package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.UserSettingsEntity
import com.example.ui.components.FuturisticHeader
import com.example.ui.components.HudPanel
import com.example.ui.theme.HackBgBlack
import com.example.ui.theme.HackCyanDark
import com.example.ui.theme.HackCyanLight
import com.example.ui.theme.HackCyanPrimary
import com.example.ui.theme.HackError
import com.example.ui.theme.HackPanelDark
import com.example.ui.theme.HackSuccess
import com.example.ui.theme.HackTextPrimary
import com.example.ui.theme.HackTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun AISettingsScreen(
    currentSettings: UserSettingsEntity,
    onSaveSettings: (UserSettingsEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var apiKeyInput by remember { mutableStateOf(currentSettings.customGeminiApiKey) }
    var selectedModel by remember { mutableStateOf(currentSettings.aiModel) }
    var memoryActive by remember { mutableStateOf(currentSettings.memoryEnabled) }
    var isTestingKey by remember { mutableStateOf(false) }
    var testKeyStatus by remember { mutableStateOf<String?>(null) }
    var isKeyValid by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HackBgBlack)
            .testTag("ai_settings_screen")
    ) {
        // Back Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = HackCyanPrimary
                )
            }
            Text(
                text = "SYSTEM SETTINGS // AI ENGINE",
                color = HackCyanPrimary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. AI PROVIDER
            item {
                HudPanel(title = "1. AI PROVIDER") {
                    Text(
                        text = "Google Gemini Neural Engine (Default)",
                        color = HackTextPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Haute performance, latence ultra-faible et capacités multimodales natives.",
                        color = HackTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // 2. MODEL SELECTION
            item {
                HudPanel(title = "2. NEURAL MODEL") {
                    val models = listOf("gemini-3.5-flash", "gemini-3.1-pro-preview")
                    models.forEach { model ->
                        val isSelected = selectedModel == model
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) HackCyanPrimary.copy(alpha = 0.15f) else HackPanelDark)
                                .border(1.dp, if (isSelected) HackCyanPrimary else HackCyanDark.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .clickable {
                                    selectedModel = model
                                    onSaveSettings(currentSettings.copy(aiModel = model))
                                }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = model.uppercase(),
                                    color = if (isSelected) HackCyanLight else HackTextPrimary,
                                    fontSize = 11.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (model.contains("3.5")) "Modèle rapide et polyvalent (recommandé)" else "Raisonnement avancé et code complexe",
                                    color = HackTextSecondary,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HackCyanPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            // 3. API KEY CONFIGURATION & TEST
            item {
                HudPanel(title = "3. GEMINI API KEY (LIVE AI)") {
                    Text(
                        text = "Pour activer les réponses complètes en direct de Gemini 3.5 Flash, saisissez votre clé API Google AI Studio :",
                        color = HackTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = {
                            apiKeyInput = it
                            testKeyStatus = null
                        },
                        placeholder = { Text("Collez votre clé API Gemini ici...", color = HackTextSecondary.copy(alpha = 0.5f), fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = HackTextPrimary,
                            unfocusedTextColor = HackTextPrimary,
                            focusedBorderColor = HackCyanPrimary,
                            unfocusedBorderColor = HackCyanDark,
                            cursorColor = HackCyanPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gemini_api_key_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // SAVE KEY BUTTON
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(HackCyanPrimary)
                                .clickable {
                                    val updated = currentSettings.copy(
                                        customGeminiApiKey = apiKeyInput.trim(),
                                        aiModel = selectedModel,
                                        memoryEnabled = memoryActive
                                    )
                                    onSaveSettings(updated)
                                    Toast.makeText(context, "Clé API enregistrée pour T-HACK AI", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ENREGISTRER LA CLÉ",
                                color = HackBgBlack,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // TEST KEY BUTTON
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(HackPanelDark)
                                .border(1.dp, HackCyanPrimary, RoundedCornerShape(4.dp))
                                .clickable(enabled = !isTestingKey && apiKeyInput.isNotBlank()) {
                                    isTestingKey = true
                                    testKeyStatus = "Test de la clé en cours..."
                                    scope.launch {
                                        val valid = testGeminiKey(apiKeyInput.trim())
                                        isTestingKey = false
                                        isKeyValid = valid
                                        testKeyStatus = if (valid) "✓ Clé API validée avec succès !" else "✗ Clé invalide ou quota dépassé."
                                    }
                                }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isTestingKey) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = HackCyanPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "TESTER LA CLÉ",
                                    color = HackCyanLight,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    testKeyStatus?.let { status ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = status,
                            color = if (isKeyValid) HackSuccess else HackError,
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 4. MEMORY TOGGLE
            item {
                HudPanel(title = "4. PERSISTENT MEMORY VAULT") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "MÉMOIRE CONVERSATIONNELLE",
                                color = HackTextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Permet à T-HACK AI de mémoriser vos directives, votre nom et vos préférences.",
                                color = HackTextSecondary,
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Switch(
                            checked = memoryActive,
                            onCheckedChange = {
                                memoryActive = it
                                onSaveSettings(currentSettings.copy(memoryEnabled = it))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = HackBgBlack,
                                checkedTrackColor = HackCyanPrimary,
                                uncheckedThumbColor = HackTextSecondary,
                                uncheckedTrackColor = HackPanelDark
                            )
                        )
                    }
                }
            }
        }
    }
}

private suspend fun testGeminiKey(key: String): Boolean = withContext(Dispatchers.IO) {
    try {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$key"
        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 7000
        conn.readTimeout = 7000

        val payload = JSONObject().apply {
            put("contents", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "ping")
                        })
                    })
                })
            })
        }

        OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
        conn.responseCode in 200..299
    } catch (_: Exception) {
        false
    }
}
