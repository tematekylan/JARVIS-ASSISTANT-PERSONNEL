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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.HudPanel
import com.example.ui.theme.HackBgBlack
import com.example.ui.theme.HackBlueAccent
import com.example.ui.theme.HackCyanDark
import com.example.ui.theme.HackCyanLight
import com.example.ui.theme.HackCyanPrimary
import com.example.ui.theme.HackPanelDark
import com.example.ui.theme.HackSuccess
import com.example.ui.theme.HackTextPrimary
import com.example.ui.theme.HackTextSecondary

@Composable
fun AppearanceScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTheme by remember { mutableStateOf("Cyan (Default)") }
    var brightness by remember { mutableFloatStateOf(0.9f) }
    var animIntensity by remember { mutableFloatStateOf(1.0f) }
    var reduceMotion by remember { mutableStateOf(false) }
    var glassEffect by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HackBgBlack)
            .testTag("appearance_settings_screen")
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
                text = "SYSTEM SETTINGS // APPEARANCE",
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
            // 1. THEME SELECTION
            item {
                HudPanel(title = "1. HOLOGRAPHIC THEME") {
                    val themes = listOf("Cyan (Default)" to HackCyanPrimary, "Blue Accent" to HackBlueAccent, "Neon Green" to HackSuccess)
                    themes.forEach { (name, color) ->
                        val isSelected = selectedTheme == name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) color.copy(alpha = 0.15f) else HackPanelDark)
                                .border(1.dp, if (isSelected) color else HackCyanDark.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .clickable { selectedTheme = name }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
                                Text(name, color = if (isSelected) color else HackTextPrimary, fontSize = 11.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                            if (isSelected) {
                                Text("ACTIVE", color = color, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            // 2. BRIGHTNESS & ANIMATION INTENSITY
            item {
                HudPanel(title = "2. DISPLAY DYNAMICS") {
                    Text("LUMINOSITÉ HUD : ${(brightness * 100).toInt()}%", color = HackTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Slider(
                        value = brightness,
                        onValueChange = { brightness = it },
                        colors = SliderDefaults.colors(thumbColor = HackCyanPrimary, activeTrackColor = HackCyanPrimary, inactiveTrackColor = HackCyanDark)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("INTENSITÉ DES ANIMATIONS : ${(animIntensity * 100).toInt()}%", color = HackTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Slider(
                        value = animIntensity,
                        onValueChange = { animIntensity = it },
                        colors = SliderDefaults.colors(thumbColor = HackCyanPrimary, activeTrackColor = HackCyanPrimary, inactiveTrackColor = HackCyanDark)
                    )
                }
            }

            // 3. REDUCE MOTION & GLASS EFFECT
            item {
                HudPanel(title = "3. INTERFACE EFFECTS") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("RÉDUIRE LES MOUVEMENTS", color = HackTextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Switch(
                            checked = reduceMotion,
                            onCheckedChange = { reduceMotion = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = HackBgBlack, checkedTrackColor = HackCyanPrimary)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("EFFET VERRE DÉPOLI HUD", color = HackTextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Switch(
                            checked = glassEffect,
                            onCheckedChange = { glassEffect = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = HackBgBlack, checkedTrackColor = HackCyanPrimary)
                        )
                    }
                }
            }
        }
    }
}
