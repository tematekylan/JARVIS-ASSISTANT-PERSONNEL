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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ToolLogEntity
import com.example.ui.MainViewModel
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommandCenterScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val conversations by viewModel.conversations.collectAsState()
    val totalMessages by viewModel.totalMessagesCount.collectAsState()
    val memories by viewModel.memories.collectAsState()
    val toolLogs by viewModel.recentToolLogs.collectAsState()
    val totalToolCalls by viewModel.totalToolCallsCount.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()

    var testToolName by remember { mutableStateOf("calculator") }
    var testToolInput by remember { mutableStateOf("128 * 45") }
    var testToolOutput by remember { mutableStateOf("") }
    var isExecutingTool by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBgVoid)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: System Status Grid
        item {
            Text(
                text = "COMMAND CENTER // SUBSYSTEMS",
                color = JarvisCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SubsystemStatusCard(
                        title = "AI ENGINE",
                        status = if (userSettings?.isDemoMode == true) "DEMO MODE" else "ONLINE",
                        subtext = userSettings?.aiModel ?: "gemini-3.5-flash",
                        color = if (userSettings?.isDemoMode == true) JarvisAmber else JarvisEmerald,
                        modifier = Modifier.weight(1f)
                    )
                    SubsystemStatusCard(
                        title = "VOICE ENGINE",
                        status = "ONLINE",
                        subtext = "TTS & STT Active",
                        color = JarvisCyan,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SubsystemStatusCard(
                        title = "MEMORY VAULT",
                        status = if (userSettings?.memoryEnabled == true) "ACTIVE" else "OFFLINE",
                        subtext = "${memories.size} records",
                        color = if (userSettings?.memoryEnabled == true) JarvisEmerald else JarvisTextMuted,
                        modifier = Modifier.weight(1f)
                    )
                    SubsystemStatusCard(
                        title = "TOOLS MATRIX",
                        status = "7 ACTIVE",
                        subtext = "$totalToolCalls executions",
                        color = JarvisBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section 2: Real-time Telemetry Metrics
        item {
            Text(
                text = "STATISTICS & TELEMETRY",
                color = JarvisCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCounterCard(title = "CONVERSATIONS", value = conversations.size.toString(), modifier = Modifier.weight(1f))
                MetricCounterCard(title = "MESSAGES", value = totalMessages.toString(), modifier = Modifier.weight(1f))
                MetricCounterCard(title = "MEMORIES", value = memories.size.toString(), modifier = Modifier.weight(1f))
                MetricCounterCard(title = "TOOL CALLS", value = totalToolCalls.toString(), modifier = Modifier.weight(1f))
            }
        }

        // Section 3: Interactive Tool Test Workbench
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisBgCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderGlow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "⚡ TOOL BENCHMARK & EXECUTION CONSOLE",
                        color = JarvisCyanGlow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Tool Selector Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ToolSelectorChip(label = "Calc", isSelected = testToolName == "calculator", onClick = {
                            testToolName = "calculator"
                            testToolInput = "128 * 45"
                        })
                        ToolSelectorChip(label = "Weather", isSelected = testToolName == "weather", onClick = {
                            testToolName = "weather"
                            testToolInput = "Paris"
                        })
                        ToolSelectorChip(label = "Time", isSelected = testToolName == "world_time", onClick = {
                            testToolName = "world_time"
                            testToolInput = "Tokyo"
                        })
                        ToolSelectorChip(label = "System", isSelected = testToolName == "system_status", onClick = {
                            testToolName = "system_status"
                            testToolInput = ""
                        })
                        ToolSelectorChip(label = "Search", isSelected = testToolName == "web_search", onClick = {
                            testToolName = "web_search"
                            testToolInput = "Gemini AI"
                        })
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = testToolInput,
                        onValueChange = { testToolInput = it },
                        label = { Text("Parameters / Input", color = JarvisTextMuted, fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tool_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisBorderGlow,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                isExecutingTool = true
                                val res = viewModel.runToolDirect(testToolName, testToolInput)
                                testToolOutput = res
                                isExecutingTool = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_execute_tool")
                    ) {
                        if (isExecutingTool) {
                            CircularProgressIndicator(color = JarvisBgVoid, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (isExecutingTool) "EXECUTING PROTOCOL..." else "EXECUTE TOOL DIRECTIVE",
                            color = JarvisBgVoid,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (testToolOutput.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF060B17))
                                .border(1.dp, JarvisBorderBright, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = testToolOutput,
                                color = JarvisEmerald,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Recent Tool Execution Logs
        item {
            Text(
                text = "RECENT TOOL EXECUTION LOGS",
                color = JarvisCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )
        }

        if (toolLogs.isEmpty()) {
            item {
                Text(
                    text = "No tool executions logged yet.",
                    color = JarvisTextMuted,
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        } else {
            items(toolLogs.take(15), key = { it.id }) { log ->
                ToolLogRow(log = log)
            }
        }
    }
}

@Composable
fun SubsystemStatusCard(
    title: String,
    status: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = JarvisBgCard),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    text = title,
                    color = JarvisTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = status,
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = subtext,
                color = JarvisTextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun MetricCounterCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = JarvisBgCard),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderGlow),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = JarvisCyan,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = title,
                color = JarvisTextMuted,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ToolSelectorChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) JarvisCyan.copy(alpha = 0.2f) else JarvisBgSurface,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) JarvisCyan else JarvisBorderGlow
        ),
        modifier = Modifier
            .clickable { onClick() }
            .testTag("tool_chip_$label")
    ) {
        Text(
            text = label,
            color = if (isSelected) JarvisCyan else JarvisTextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ToolLogRow(log: ToolLogEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = JarvisBgCard),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderGlow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (log.status == "SUCCESS") JarvisEmerald else JarvisCrimson)
                    )
                    Text(
                        text = log.toolName.uppercase(),
                        color = JarvisCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "${log.durationMs}ms // " + SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp)),
                    color = JarvisTextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (log.inputParams.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "IN: ${log.inputParams}",
                    color = JarvisTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "OUT: ${log.outputResult}",
                color = JarvisEmerald.copy(alpha = 0.85f),
                fontSize = 10.sp,
                maxLines = 2,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
