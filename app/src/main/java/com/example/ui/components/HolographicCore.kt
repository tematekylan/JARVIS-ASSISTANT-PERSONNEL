package com.example.ui.components

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisCrimson
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisEmerald
import kotlin.math.cos
import kotlin.math.sin

enum class JarvisCoreState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

@Composable
fun HolographicCore(
    state: JarvisCoreState,
    audioAmplitude: Float = 0f,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_core")

    // Slow rotation for IDLE
    val rotationSlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_slow"
    )

    // Fast rotation for THINKING
    val rotationFast by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_fast"
    )

    // Pulse breathing
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Fast pulse for listening
    val listenPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "listen_pulse"
    )

    val primaryCoreColor = when (state) {
        JarvisCoreState.IDLE -> JarvisCyan
        JarvisCoreState.LISTENING -> JarvisEmerald
        JarvisCoreState.THINKING -> JarvisCyanGlow
        JarvisCoreState.SPEAKING -> JarvisBlue
        JarvisCoreState.ERROR -> JarvisCrimson
    }

    val secondaryCoreColor = when (state) {
        JarvisCoreState.IDLE -> JarvisBlue
        JarvisCoreState.LISTENING -> JarvisCyan
        JarvisCoreState.THINKING -> JarvisAmber
        JarvisCoreState.SPEAKING -> JarvisCyan
        JarvisCoreState.ERROR -> JarvisAmber
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .testTag("holographic_core")
                .clickable { onClick() }
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val canvasCenter = Offset(this.size.width / 2f, this.size.height / 2f)
                val baseRadius = this.size.width * 0.42f
                val dynamicAmp = audioAmplitude.coerceIn(0f, 1f) * 20f

                val currentPulse = when (state) {
                    JarvisCoreState.LISTENING -> listenPulse + (audioAmplitude * 0.15f)
                    JarvisCoreState.SPEAKING -> pulseScale + (audioAmplitude * 0.12f)
                    JarvisCoreState.THINKING -> 1.02f
                    JarvisCoreState.ERROR -> 1.08f
                    JarvisCoreState.IDLE -> pulseScale
                }

                val currentRot1 = when (state) {
                    JarvisCoreState.THINKING -> rotationFast * 1.5f
                    JarvisCoreState.LISTENING -> rotationSlow * 2f
                    else -> rotationSlow
                }

                val currentRot2 = when (state) {
                    JarvisCoreState.THINKING -> rotationSlow * 3f
                    else -> -rotationSlow * 0.7f
                }

                // 1. Central Ambient Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryCoreColor.copy(alpha = 0.45f),
                            secondaryCoreColor.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = canvasCenter,
                        radius = baseRadius * currentPulse
                    ),
                    center = canvasCenter,
                    radius = baseRadius * currentPulse
                )

                // 2. Outer Ring with Tech Dash Pattern
                rotate(degrees = currentRot1, pivot = canvasCenter) {
                    drawCircle(
                        color = primaryCoreColor.copy(alpha = 0.4f),
                        center = canvasCenter,
                        radius = baseRadius * 0.95f,
                        style = Stroke(
                            width = 2.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(25f, 15f, 40f, 15f), 0f)
                        )
                    )
                }

                // 3. Segmented Arc Ring (Holographic Segmented Shield)
                rotate(degrees = currentRot2, pivot = canvasCenter) {
                    val arcRadius = baseRadius * 0.80f
                    val arcRect = Size(arcRadius * 2, arcRadius * 2)
                    val topLeft = Offset(canvasCenter.x - arcRadius, canvasCenter.y - arcRadius)

                    for (i in 0 until 4) {
                        drawArc(
                            color = secondaryCoreColor.copy(alpha = 0.85f),
                            startAngle = i * 90f + 15f,
                            sweepAngle = 55f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcRect,
                            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                        )
                    }
                }

                // 4. Equalizer Waveform Arcs for SPEAKING / LISTENING
                if (state == JarvisCoreState.SPEAKING || state == JarvisCoreState.LISTENING) {
                    val eqBars = 24
                    val eqRadius = baseRadius * 0.65f
                    for (i in 0 until eqBars) {
                        val angle = (i * (360f / eqBars)) * (Math.PI / 180f)
                        val barHeight = 8f + (dynamicAmp * sin(i * 1.5f + rotationSlow * 0.1f).toFloat() * 6f).coerceAtLeast(0f)
                        val startX = canvasCenter.x + (eqRadius * cos(angle)).toFloat()
                        val startY = canvasCenter.y + (eqRadius * sin(angle)).toFloat()
                        val endX = canvasCenter.x + ((eqRadius + barHeight) * cos(angle)).toFloat()
                        val endY = canvasCenter.y + ((eqRadius + barHeight) * sin(angle)).toFloat()

                        drawLine(
                            color = primaryCoreColor.copy(alpha = 0.9f),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // 5. Inner Core Node
                val innerRadius = baseRadius * 0.35f * currentPulse
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            primaryCoreColor,
                            secondaryCoreColor.copy(alpha = 0.6f)
                        ),
                        center = canvasCenter,
                        radius = innerRadius
                    ),
                    center = canvasCenter,
                    radius = innerRadius
                )

                // 6. Center Energy Dot
                drawCircle(
                    color = Color.White,
                    center = canvasCenter,
                    radius = 4.dp.toPx()
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // State Indicator Badge
        HolographicStatusBadge(state = state)
    }
}

@Composable
fun HolographicStatusBadge(
    state: JarvisCoreState,
    modifier: Modifier = Modifier
) {
    val (statusLabel, statusColor) = when (state) {
        JarvisCoreState.IDLE -> "SYSTEM READY" to JarvisCyan
        JarvisCoreState.LISTENING -> "LISTENING..." to JarvisEmerald
        JarvisCoreState.THINKING -> "PROCESSING QUERY..." to JarvisCyanGlow
        JarvisCoreState.SPEAKING -> "TRANSMITTING VOICE..." to JarvisBlue
        JarvisCoreState.ERROR -> "SUBSYSTEM ALERT" to JarvisCrimson
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF071224))
            .border(1.dp, statusColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
                    .shadow(elevation = 6.dp, shape = CircleShape, spotColor = statusColor)
            )
            Text(
                text = statusLabel,
                color = statusColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )
        }
    }
}
