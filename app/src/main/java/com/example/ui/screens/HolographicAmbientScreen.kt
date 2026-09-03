package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.os.BatteryManager
import android.view.WindowManager
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.JarvisCoreState
import com.example.ui.components.JarvisScreen
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

data class QuantumMoleculeNode(
    val id: Int,
    val baseAngle: Float,
    val orbitRadius: Float,
    val speed: Float,
    val nodeRadius: Float,
    val color: Color,
    val name: String
)

@Composable
fun HolographicAmbientScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coreState by viewModel.coreState.collectAsState()
    val isStreaming by viewModel.isStreaming.collectAsState()
    val streamingResponse by viewModel.streamingResponse.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()

    var currentTimeStr by remember { mutableStateOf("") }
    var currentDateStr by remember { mutableStateOf("") }
    var batteryPercent by remember { mutableFloatStateOf(100f) }
    var promptInput by remember { mutableStateOf("") }
    val recognizedVoiceText by viewModel.recognizedVoiceText.collectAsState()

    // Sync speech recognized text into prompt input
    LaunchedEffect(recognizedVoiceText) {
        if (recognizedVoiceText.isNotBlank()) {
            promptInput = recognizedVoiceText
        }
    }

    // Keep screen ON while in AOD / Holographic Ambient Mode
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Time and Battery update loop
    LaunchedEffect(Unit) {
        val timeSdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val dateSdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.FRENCH)
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager

        while (true) {
            val now = Date()
            currentTimeStr = timeSdf.format(now)
            currentDateStr = dateSdf.format(now).uppercase()

            val bat = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
            batteryPercent = bat.toFloat()

            delay(1000)
        }
    }

    // Infinite transitions for molecular movement
    val infiniteTransition = rememberInfiniteTransition(label = "quantum_molecules")

    val orbitRotationFast by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_fast"
    )

    val orbitRotationSlow by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_slow"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val corePulse by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_pulse"
    )

    // Molecular nodes structure
    val molecularNodes = remember {
        listOf(
            QuantumMoleculeNode(1, 0f, 130f, 1.0f, 9f, JarvisCyan, "Q-ALPHA"),
            QuantumMoleculeNode(2, 60f, 95f, -1.2f, 7f, JarvisEmerald, "N-CORE"),
            QuantumMoleculeNode(3, 120f, 140f, 0.8f, 10f, JarvisCyanGlow, "M-FLUX"),
            QuantumMoleculeNode(4, 180f, 110f, -0.9f, 8f, JarvisAmber, "E-ORBIT"),
            QuantumMoleculeNode(5, 240f, 145f, 1.1f, 9f, JarvisBlue, "Z-TENSOR"),
            QuantumMoleculeNode(6, 300f, 100f, -1.0f, 7.5f, JarvisCyan, "S-PHOTON")
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("screen_holographic_aod")
    ) {
        // 1. Quantum Holographic Canvas (Molecules, Covalent bonds, Arc Reactor)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    // Tap to trigger voice listening or interact
                }
        ) {
            val center = Offset(size.width / 2f, size.height * 0.44f)

            // Background HUD Crosshairs and Grid Circles
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(JarvisCyan.copy(alpha = 0.15f * corePulse), Color.Transparent),
                    center = center,
                    radius = 280f
                ),
                radius = 280f,
                center = center
            )

            drawCircle(
                color = JarvisCyan.copy(alpha = 0.18f),
                radius = 200f,
                center = center,
                style = Stroke(
                    width = 1.2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), orbitRotationFast)
                )
            )

            drawCircle(
                color = JarvisEmerald.copy(alpha = 0.22f),
                radius = 140f,
                center = center,
                style = Stroke(
                    width = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 12f), orbitRotationSlow)
                )
            )

            // Outer Elliptical Orbital Rings
            rotate(degrees = orbitRotationFast * 0.5f, pivot = center) {
                drawOval(
                    color = JarvisCyan.copy(alpha = 0.35f),
                    topLeft = Offset(center.x - 220f, center.y - 120f),
                    size = androidx.compose.ui.geometry.Size(440f, 240f),
                    style = Stroke(width = 1.8f)
                )
            }

            rotate(degrees = -orbitRotationSlow * 0.6f + 45f, pivot = center) {
                drawOval(
                    color = JarvisAmber.copy(alpha = 0.30f),
                    topLeft = Offset(center.x - 210f, center.y - 110f),
                    size = androidx.compose.ui.geometry.Size(420f, 220f),
                    style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                )
            }

            // Central Arc Reactor Core
            val currentCoreRadius = 46f * corePulse
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        JarvisCyanGlow,
                        JarvisCyan.copy(alpha = 0.6f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = currentCoreRadius * 1.8f
                ),
                radius = currentCoreRadius * 1.8f,
                center = center
            )

            drawCircle(
                color = JarvisCyan,
                radius = currentCoreRadius,
                center = center,
                style = Stroke(width = 3.5f)
            )

            // Inner Core Arc Segments
            for (i in 0 until 12) {
                val segAngle = i * 30f + orbitRotationFast
                val rad = Math.toRadians(segAngle.toDouble())
                val p1 = Offset(
                    center.x + (currentCoreRadius - 12f) * cos(rad).toFloat(),
                    center.y + (currentCoreRadius - 12f) * sin(rad).toFloat()
                )
                val p2 = Offset(
                    center.x + currentCoreRadius * cos(rad).toFloat(),
                    center.y + currentCoreRadius * sin(rad).toFloat()
                )
                drawLine(
                    color = if (i % 3 == 0) JarvisEmerald else JarvisCyanGlow,
                    start = p1,
                    end = p2,
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }

            // Calculate moving molecular node coordinates
            val nodePositions = molecularNodes.map { node ->
                val currentAngle = node.baseAngle + (orbitRotationFast * node.speed)
                val rad = Math.toRadians(currentAngle.toDouble())
                val wobble = sin(wavePhase + node.id) * 12f
                val r = node.orbitRadius + wobble
                val pos = Offset(
                    center.x + r * cos(rad).toFloat(),
                    center.y + (r * 0.75f) * sin(rad).toFloat()
                )
                Pair(node, pos)
            }

            // Draw Dynamic Molecular Bonds (Connecting lines between nodes)
            for (i in nodePositions.indices) {
                val (_, posA) = nodePositions[i]
                // Bond to central core
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(JarvisCyan.copy(alpha = 0.45f), Color.Transparent),
                        start = center,
                        end = posA
                    ),
                    start = center,
                    end = posA,
                    strokeWidth = 1.2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), wavePhase * 5f)
                )

                // Bond to adjacent nodes
                val nextIdx = (i + 1) % nodePositions.size
                val (_, posB) = nodePositions[nextIdx]
                drawLine(
                    color = JarvisCyan.copy(alpha = 0.35f),
                    start = posA,
                    end = posB,
                    strokeWidth = 1.4f
                )
            }

            // Draw Glowing Molecular Nodes (Atoms)
            nodePositions.forEach { (node, pos) ->
                // Glow halo
                drawCircle(
                    color = node.color.copy(alpha = 0.35f),
                    radius = node.nodeRadius * 2.2f,
                    center = pos
                )
                // Solid Atom
                drawCircle(
                    color = node.color,
                    radius = node.nodeRadius,
                    center = pos
                )
                drawCircle(
                    color = Color.White,
                    radius = node.nodeRadius * 0.4f,
                    center = pos
                )
            }
        }

        // 2. Top Bar HUD (Time, Date, Battery & Exit Button)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 36.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = currentTimeStr.ifBlank { "--:--:--" },
                    color = JarvisCyan,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                Text(
                    text = currentDateStr.ifBlank { "STANDBY" },
                    color = JarvisTextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (batteryPercent > 20f) JarvisEmerald else JarvisAmber)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ENERGIE : ${batteryPercent.toInt()}% // MATRIX ACTIVE",
                        color = if (batteryPercent > 20f) JarvisEmerald else JarvisAmber,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Close AOD Mode & Return to Chat
            IconButton(
                onClick = { viewModel.navigateTo(JarvisScreen.CHAT) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray.copy(alpha = 0.5f))
                    .border(1.dp, JarvisCyan.copy(alpha = 0.5f), CircleShape)
                    .testTag("btn_close_aod")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Quitter le mode AOD",
                    tint = JarvisCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 3. Middle / Bottom HUD: Live Streaming AI Text & Direct Voice Input
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live response or state feedback
            if (isStreaming || streamingResponse.isNotBlank()) {
                Surface(
                    color = Color.Black.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyanGlow),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(JarvisCyanGlow)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "T-HACK AI // RÉPONSE EN DIRECT",
                                color = JarvisCyanGlow,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = streamingResponse,
                            color = JarvisTextPrimary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            } else {
                Text(
                    text = "● HOLOGRAMME QUANTIQUE EN VEILLE ●",
                    color = JarvisCyan.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = "Dites « T-HACK ouvre YouTube », « écris à Émilie sur WhatsApp » ou « appelle Teddy »",
                    color = JarvisTextMuted,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 14.dp)
                )
            }

            // Quick Voice & Text Action Bar directly in AOD Mode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.DarkGray.copy(alpha = 0.4f))
                    .border(1.dp, JarvisBorderGlowAmbient, RoundedCornerShape(24.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Voice Recognition Mic Button
                val isListening = coreState == JarvisCoreState.LISTENING
                IconButton(
                    onClick = {
                        if (isListening) {
                            viewModel.stopListening()
                        } else {
                            viewModel.startListening()
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isListening) JarvisCyan else Color.Black.copy(alpha = 0.6f))
                        .border(1.5.dp, JarvisCyanGlow, CircleShape)
                        .testTag("btn_aod_voice_mic")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Parler à T-HACK AI",
                        tint = if (isListening) Color.Black else JarvisCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Text Input
                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholder = {
                        Text(
                            text = "Commande vocale ou texte...",
                            color = JarvisTextMuted,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = JarvisCyan
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                // Send Button
                IconButton(
                    onClick = {
                        if (promptInput.isNotBlank()) {
                            viewModel.sendUserMessage(promptInput, null)
                            promptInput = ""
                        }
                    },
                    enabled = promptInput.isNotBlank(),
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (promptInput.isNotBlank()) JarvisCyan else Color.Transparent)
                        .testTag("btn_aod_send")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Envoyer commande",
                        tint = if (promptInput.isNotBlank()) Color.Black else JarvisTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

val JarvisBorderGlowAmbient = Brush.linearGradient(
    colors = listOf(JarvisCyan.copy(alpha = 0.7f), JarvisEmerald.copy(alpha = 0.5f))
)
