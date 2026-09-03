package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AssistantState
import com.example.ui.components.FuturisticAICore
import com.example.ui.theme.HackBgBlack
import com.example.ui.theme.HackCyanDark
import com.example.ui.theme.HackCyanLight
import com.example.ui.theme.HackCyanPrimary
import com.example.ui.theme.HackSuccess
import com.example.ui.theme.HackTextSecondary
import kotlinx.coroutines.delay

@Composable
fun BootScreen(
    onBootComplete: () -> Unit
) {
    val bootLogs = remember { mutableStateListOf<String>() }
    var progressText by remember { mutableStateOf("[••••••••••••••••] 0%") }
    var isSystemReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(250)
        bootLogs.add("[✓] AI CORE")
        progressText = "[••••░░░░░░░░░░░░] 20%"
        delay(250)
        bootLogs.add("[✓] VOICE ENGINE")
        progressText = "[••••••••░░░░░░░░] 40%"
        delay(250)
        bootLogs.add("[✓] MEMORY SYSTEM")
        progressText = "[••••••••••••░░░░] 60%"
        delay(250)
        bootLogs.add("[✓] COMMAND ENGINE")
        progressText = "[••••••••••••••••] 80%"
        delay(250)
        bootLogs.add("[✓] INTERFACE HUD")
        progressText = "[••••••••••••••••] 100%"
        delay(200)
        isSystemReady = true
        delay(600)
        onBootComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HackBgBlack)
            .testTag("boot_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Central simplified AI Core
            FuturisticAICore(
                state = if (isSystemReady) AssistantState.SUCCESS else AssistantState.IDLE,
                size = 180.dp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Boot Check lines
            Column(
                modifier = Modifier
                    .width(240.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                bootLogs.forEach { log ->
                    Text(
                        text = log,
                        color = HackSuccess,
                        fontSize = 11.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = progressText,
                    color = HackCyanPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                if (isSystemReady) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "● SYSTEM READY",
                        color = HackCyanLight,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}
