package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FuturisticHeader
import com.example.ui.theme.HackBgBlack
import com.example.ui.theme.HackCyanDark
import com.example.ui.theme.HackCyanLight
import com.example.ui.theme.HackCyanPrimary
import com.example.ui.theme.HackPanelDark
import com.example.ui.theme.HackSuccess
import com.example.ui.theme.HackTextPrimary
import com.example.ui.theme.HackTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TerminalScreen(
    onExecuteCommand: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val terminalLines = remember {
        mutableStateListOf(
            "T-HACKMAN SYSTEM INITIALIZED...",
            "AI CORE ONLINE [QUANTUM STREAM ACTIVE]",
            "AUDIO SYNTHESIZER & LISTENER CONNECTED.",
            "MEMORY VAULT: LOCAL ENCRYPTION VERIFIED.",
            "NEURAL LINK ESTABLISHED WITH GEMINI-3.5-FLASH.",
            "AWAITING INSTRUCTION... TYPE 'help' FOR LIST OF SYSTEM DIRECTIVES."
        )
    }

    var commandInput by remember { mutableStateOf("") }

    val cursorTransition = rememberInfiniteTransition(label = "cursor_blink")
    val cursorAlpha by cursorTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HackBgBlack)
            .testTag("terminal_screen")
    ) {
        FuturisticHeader(systemStatusText = "TERMINAL TTY_1")

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(HackPanelDark)
                .border(1.dp, HackCyanDark.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .padding(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(terminalLines) { line ->
                    Text(
                        text = line,
                        color = if (line.startsWith(">")) HackCyanLight else HackSuccess,
                        fontSize = 11.5.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Command input line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HackBgBlack)
                    .border(1.dp, HackCyanPrimary, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "root@t-hack:~# ",
                    color = HackCyanPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                BasicTextField(
                    value = commandInput,
                    onValueChange = { commandInput = it },
                    textStyle = TextStyle(
                        color = HackTextPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    cursorBrush = SolidColor(HackCyanPrimary),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                // Blinking block cursor
                Box(
                    modifier = Modifier
                        .size(width = 7.dp, height = 14.dp)
                        .background(HackCyanPrimary.copy(alpha = cursorAlpha))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(HackCyanPrimary)
                        .clickable {
                            if (commandInput.isNotBlank()) {
                                val cmd = commandInput.trim()
                                val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                                terminalLines.add("[$time] > $cmd")
                                when (cmd.lowercase()) {
                                    "help" -> {
                                        terminalLines.add("SYSTEM DIRECTIVES:")
                                        terminalLines.add("- status : Check telemetry and subsystems")
                                        terminalLines.add("- clear  : Clear console buffer")
                                        terminalLines.add("- demo   : Run visual core state cycle")
                                        terminalLines.add("- ping   : Check neural latency")
                                    }
                                    "clear" -> terminalLines.clear()
                                    "status" -> terminalLines.add("[STATUS] ALL SYSTEMS NOMINAL. BATTERY 87%. CPU 24%.")
                                    "ping" -> terminalLines.add("[PONG] Neural Uplink: 42ms response.")
                                    else -> {
                                        terminalLines.add("[EXEC] Routing to T-HACK AI Core...")
                                        onExecuteCommand(cmd)
                                    }
                                }
                                commandInput = ""
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("EXEC", color = HackBgBlack, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
