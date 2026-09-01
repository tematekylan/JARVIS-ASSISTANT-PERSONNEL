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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    val incidents by viewModel.incidents.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    var isSimulatingIncident by remember { mutableStateOf(false) }

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

        // Section 2.2: AI SWAT Incident Resolution Council & Email Telemetry
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (incidents.isNotEmpty()) JarvisCrimson.copy(alpha = 0.06f) else JarvisBgCard
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (incidents.isNotEmpty()) JarvisCrimson.copy(alpha = 0.5f) else JarvisCyan.copy(alpha = 0.3f)
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
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                tint = if (incidents.isNotEmpty()) JarvisCrimson else JarvisCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "COLLÈGE D'IA DE RÉSOLUTION D'INCIDENTS",
                                color = if (incidents.isNotEmpty()) JarvisCrimson else JarvisCyanGlow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        val alertEmail = userSettings?.developerAlertEmail?.ifBlank { "temateteddy@gmail.com" } ?: "temateteddy@gmail.com"
                        Text(
                            text = "ALERTES : $alertEmail",
                            color = JarvisTextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "Lorsqu'une anomalie survient, l'application isole l'erreur sans interrompre le fil de conversation et convoque immédiatement une cellule de crise d'IA (Architecte, Débogueur, Ingénieur Patch) tout en préparant la transmission du rapport par email.",
                        color = JarvisTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )

                    // Action bar: Trigger test incident simulation & Clear history
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (!isSimulatingIncident) {
                                    isSimulatingIncident = true
                                    viewModel.triggerSimulatedIncident {
                                        isSimulatingIncident = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan.copy(alpha = 0.2f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isSimulatingIncident) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = JarvisCyan, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = if (isSimulatingIncident) "DÉLIBÉRATION..." else "SIMULER ANOMALIE & DÉLIBÉRATION",
                                color = JarvisCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (incidents.isNotEmpty()) {
                            Button(
                                onClick = { viewModel.clearAllIncidents() },
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisBgVoid),
                                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisBorderGlow),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = JarvisTextMuted, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PURGER",
                                    color = JarvisTextMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Incidents List
                    if (incidents.isEmpty()) {
                        Surface(
                            color = JarvisEmerald.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, JarvisEmerald.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = JarvisEmerald, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "AUCUN INCIDENT ACTIF // SYSTÈMES 100% STABLES & SAINS",
                                    color = JarvisEmerald,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            incidents.forEach { incident ->
                                IncidentReportCard(
                                    incident = incident,
                                    onSendEmail = {
                                        val intent = viewModel.incidentEngine.buildEmailIntent(incident)
                                        try {
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            android.widget.Toast.makeText(context, "Client email introuvable.", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onReAnalyze = { viewModel.reConveneAiCouncil(incident) },
                                    onDelete = { viewModel.deleteIncident(incident.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2.5: Slash Command Matrix & Market Shortcuts
        item {
            Text(
                text = "⚡ RACCOURCIS & PROTOCOLES SLASH (/)",
                color = JarvisCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = JarvisBgCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "COMMANDES RAPIDES INTÉGRÉES (Cliquez pour lancer dans le Chat) :",
                        color = JarvisTextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SlashCommandCard(
                            tag = "/humain",
                            title = "Langage 100% Humain",
                            desc = "Zéro tournure robotique, ton naturel, amical et vivant",
                            color = JarvisEmerald,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.sendUserMessage("/humain Parle-moi naturellement et dis-moi comment tu vois notre collaboration", null)
                                viewModel.navigateTo(com.example.ui.components.JarvisScreen.CHAT)
                            }
                        )
                        SlashCommandCard(
                            tag = "/rayonx",
                            title = "Vue Éclatée / X-Ray",
                            desc = "Schéma technique complet, pièces, moteurs et circuits",
                            color = JarvisCyan,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.sendUserMessage("/rayonx Analyse les composants internes d'un moteur d'avion à réaction", null)
                                viewModel.navigateTo(com.example.ui.components.JarvisScreen.CHAT)
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SlashCommandCard(
                            tag = "/plan",
                            title = "Masterplan A à Z",
                            desc = "Plan directeur exécutif complet par blocs & jalons",
                            color = JarvisAmber,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.sendUserMessage("/plan Plan d'exécution complet pour concevoir une application mobile de A à Z", null)
                                viewModel.navigateTo(com.example.ui.components.JarvisScreen.CHAT)
                            }
                        )
                        SlashCommandCard(
                            tag = "/code",
                            title = "Code Production",
                            desc = "Code pur, propre, typé et optimisé sans blabla",
                            color = JarvisBlue,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.sendUserMessage("/code Écris une fonction Kotlin de retry exponentiel pour coroutines", null)
                                viewModel.navigateTo(com.example.ui.components.JarvisScreen.CHAT)
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SlashCommandCard(
                            tag = "/debug",
                            title = "Audit & Debug",
                            desc = "Recherche de bugs et correctif chirurgical",
                            color = JarvisCrimson,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.sendUserMessage("/debug Pourquoi un StateFlow ne déclenche pas de recomposition Compose ?", null)
                                viewModel.navigateTo(com.example.ui.components.JarvisScreen.CHAT)
                            }
                        )
                        SlashCommandCard(
                            tag = "/ironman",
                            title = "Protocole Mk-85",
                            desc = "Télémétrie Stark, réacteur Arc & visée HUD",
                            color = JarvisCyanGlow,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.sendUserMessage("/ironman Jarvis, lance le protocole de combat et scanne la zone", null)
                                viewModel.navigateTo(com.example.ui.components.JarvisScreen.CHAT)
                            }
                        )
                    }
                }
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
fun SlashCommandCard(
    tag: String,
    title: String,
    desc: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = JarvisBgCardElevated),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = tag,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                color = JarvisTextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                color = JarvisTextMuted,
                fontSize = 9.sp,
                lineHeight = 12.sp
            )
        }
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

@Composable
fun IncidentReportCard(
    incident: com.example.data.entity.IncidentReportEntity,
    onSendEmail: () -> Unit,
    onReAnalyze: () -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        colors = CardDefaults.cardColors(containerColor = JarvisBgCard),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCrimson.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header Row
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
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(JarvisCrimson)
                    )
                    Text(
                        text = incident.incidentCode,
                        color = JarvisCrimson,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(JarvisAmber.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = incident.errorCode,
                            color = JarvisAmber,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val dateStr = SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()).format(Date(incident.timestamp))
                    Text(
                        text = dateStr,
                        color = JarvisTextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Supprimer l'incident",
                            tint = JarvisTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Error summary
            Text(
                text = incident.errorMessage,
                color = JarvisTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            if (incident.contextInfo.isNotBlank()) {
                Text(
                    text = "Contexte : ${incident.contextInfo}",
                    color = JarvisTextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // AI Council Deliberation Section
            Surface(
                color = Color(0xFF070E1E),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
                            Text(
                                text = "DÉLIBÉRATION DU COLLÈGE D'IA (SWAT)",
                                color = JarvisCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = if (incident.aiCouncilStatus == "RESOLVED") "CONSEIL UNANIME" else "DÉLIBÉRATION EN COURS",
                            color = if (incident.aiCouncilStatus == "RESOLVED") JarvisEmerald else JarvisAmber,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (isExpanded) {
                        Text(
                            text = incident.aiCouncilDeliberation,
                            color = JarvisTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        if (incident.aiCouncilHotfixCode.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "PROPOSITION DE PATCH / HOTFIX :",
                                color = JarvisAmber,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF030712))
                                    .border(1.dp, JarvisBorderBright, RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = incident.aiCouncilHotfixCode,
                                    color = JarvisEmerald,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSendEmail,
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.5f)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = JarvisBgVoid, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ENVOYER L'ALERTE À TEMATETEDDY@GMAIL.COM",
                        color = JarvisBgVoid,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Button(
                    onClick = onReAnalyze,
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisBgSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "RÉ-ÉVALUER",
                        color = JarvisCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
