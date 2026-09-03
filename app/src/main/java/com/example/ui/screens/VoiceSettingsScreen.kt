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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.UserSettingsEntity
import com.example.ui.components.FuturisticAudioWaveform
import com.example.ui.components.HudPanel
import com.example.ui.theme.HackBgBlack
import com.example.ui.theme.HackCyanDark
import com.example.ui.theme.HackCyanLight
import com.example.ui.theme.HackCyanPrimary
import com.example.ui.theme.HackPanelDark
import com.example.ui.theme.HackTextPrimary
import com.example.ui.theme.HackTextSecondary

@Composable
fun VoiceSettingsScreen(
    currentSettings: UserSettingsEntity,
    onSaveSettings: (UserSettingsEntity) -> Unit,
    onTestVoice: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var voiceResponse by remember { mutableStateOf(currentSettings.ttsEnabled) }
    var selectedLanguage by remember { mutableStateOf(currentSettings.voiceLanguage) }
    var speechSpeed by remember { mutableFloatStateOf(currentSettings.speechRate) }
    var speechVolume by remember { mutableFloatStateOf(currentSettings.speechPitch) }
    var isTestingAudio by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HackBgBlack)
            .testTag("voice_settings_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HackCyanPrimary)
            }
            Text(
                text = "SYSTEM SETTINGS // VOICE ENGINE",
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
            // 1. VOICE RESPONSE TOGGLE
            item {
                HudPanel(title = "1. VOICE RESPONSE") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("SYNTHÈSE VOCALE ACTIVE", color = HackTextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Text("T-HACK AI vocalise ses réponses aux directives reçues.", color = HackTextSecondary, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace)
                        }
                        Switch(
                            checked = voiceResponse,
                            onCheckedChange = {
                                voiceResponse = it
                                onSaveSettings(currentSettings.copy(ttsEnabled = it))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = HackBgBlack, checkedTrackColor = HackCyanPrimary)
                        )
                    }
                }
            }

            // 2. VOICE LANGUAGE
            item {
                HudPanel(title = "2. LANGUE VOCALE") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val langs = listOf("fr" to "Français (Défaut)", "en" to "English")
                        langs.forEach { (code, label) ->
                            val isSelected = selectedLanguage == code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) HackCyanPrimary else HackPanelDark)
                                    .border(1.dp, if (isSelected) HackCyanLight else HackCyanDark.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .clickable {
                                        selectedLanguage = code
                                        onSaveSettings(currentSettings.copy(voiceLanguage = code))
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) HackBgBlack else HackTextPrimary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 3. SPEED & VOLUME
            item {
                HudPanel(title = "3. PROSODIE & MODULATION") {
                    Text("VITESSE D'ÉLOCUTION : ${String.format("%.1fx", speechSpeed)}", color = HackTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Slider(
                        value = speechSpeed,
                        onValueChange = {
                            speechSpeed = it
                            onSaveSettings(currentSettings.copy(speechRate = it))
                        },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(thumbColor = HackCyanPrimary, activeTrackColor = HackCyanPrimary, inactiveTrackColor = HackCyanDark)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("HAUTEUR DE TON (PITCH) : ${String.format("%.1fx", speechVolume)}", color = HackTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Slider(
                        value = speechVolume,
                        onValueChange = {
                            speechVolume = it
                            onSaveSettings(currentSettings.copy(speechPitch = it))
                        },
                        valueRange = 0.5f..1.5f,
                        colors = SliderDefaults.colors(thumbColor = HackCyanPrimary, activeTrackColor = HackCyanPrimary, inactiveTrackColor = HackCyanDark)
                    )
                }
            }

            // 4. MICROPHONE & AUDIO TEST
            item {
                HudPanel(title = "4. MICROPHONE & AUDIO TEST") {
                    Text("Tester la synthèse vocale et la modulation acoustique :", color = HackTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(10.dp))

                    FuturisticAudioWaveform(
                        isActive = isTestingAudio,
                        amplitude = if (isTestingAudio) 0.8f else 0.1f
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(HackCyanPrimary)
                            .clickable {
                                isTestingAudio = !isTestingAudio
                                onTestVoice()
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isTestingAudio) "ARRÊTER LE TEST" else "TESTER LA VOIX T-HACK AI",
                            color = HackBgBlack,
                            fontSize = 11.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
