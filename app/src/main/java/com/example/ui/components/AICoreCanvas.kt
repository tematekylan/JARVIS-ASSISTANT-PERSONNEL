package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.HackBlueAccent
import com.example.ui.theme.HackCyanDark
import com.example.ui.theme.HackCyanLight
import com.example.ui.theme.HackCyanPrimary
import com.example.ui.theme.HackError
import com.example.ui.theme.HackSuccess
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws the 7 futuristic visual levels of the T-HACKMAN AI Core using Compose Canvas:
 * 1. Central luminous core with breathing glow
 * 2. Concentric circles
 * 3. Multi-speed rotating rings (5 distinct rings)
 * 4. Circular segmented arcs & cardinal HUD ticks
 * 5. Orbital floating particles
 * 6. Radial telemetry data lines
 * 7. Ambient holographic energy halo
 */
fun DrawScope.drawAICoreCanvas(
    state: AssistantState,
    pulse: Float,
    rot1: Float,
    rot2: Float,
    rot3: Float,
    rot4: Float,
    audioAmplitude: Float,
    processingProgress: Float
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val maxRadius = (size.minDimension / 2f) * 0.94f

    // Determine color schemes based on state
    val (primaryColor, glowColor, accentColor) = when (state) {
        AssistantState.ERROR -> Triple(HackError, Color(0xFFFF8095), Color(0xFFFFB547))
        AssistantState.SUCCESS -> Triple(HackSuccess, Color(0xFF94FFD6), HackCyanPrimary)
        AssistantState.THINKING -> Triple(HackCyanLight, Color.White, HackBlueAccent)
        AssistantState.PROCESSING -> Triple(HackCyanPrimary, HackCyanLight, HackSuccess)
        AssistantState.LISTENING -> Triple(HackCyanPrimary, Color.White, HackBlueAccent)
        AssistantState.SPEAKING -> Triple(HackCyanPrimary, HackCyanLight, HackBlueAccent)
        AssistantState.IDLE -> Triple(HackCyanPrimary, HackCyanLight, HackCyanDark)
    }

    // LEVEL 7: Subtle ambient luminous energy halo
    val haloRadius = maxRadius * (0.85f + (pulse * 0.15f) + (if (state == AssistantState.LISTENING) audioAmplitude * 0.12f else 0f))
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.28f * (if (state == AssistantState.THINKING) 1.5f else 1.0f)),
                primaryColor.copy(alpha = 0.08f),
                Color.Transparent
            ),
            center = center,
            radius = haloRadius
        ),
        radius = haloRadius,
        center = center
    )

    // LEVEL 6: Radial data stream lines (CORE ──── [DATA STREAM] ────)
    val radialCount = 12
    for (i in 0 until radialCount) {
        val angle = i * (360f / radialCount)
        val rad = Math.toRadians(angle.toDouble())
        val startR = maxRadius * 0.42f
        val endR = maxRadius * (if (i % 3 == 0) 0.96f else 0.76f)
        val lineAlpha = if (i % 3 == 0) 0.35f else 0.15f

        drawLine(
            color = accentColor.copy(alpha = lineAlpha),
            start = Offset((center.x + startR * cos(rad)).toFloat(), (center.y + startR * sin(rad)).toFloat()),
            end = Offset((center.x + endR * cos(rad)).toFloat(), (center.y + endR * sin(rad)).toFloat()),
            strokeWidth = if (i % 3 == 0) 1.5f else 1.0f
        )
    }

    // LEVEL 5: Orbital Particles
    val particleSpeedFactor = when (state) {
        AssistantState.THINKING -> 3.0f
        AssistantState.LISTENING -> 2.0f
        AssistantState.PROCESSING -> 2.5f
        else -> 1.0f
    }
    drawCoreParticles(
        center = center,
        maxRadius = maxRadius,
        rotationDeg = rot1,
        color = primaryColor,
        stateMultiplier = particleSpeedFactor
    )

    // LEVEL 3 & 4: Multi-speed Rotating Rings & Segmented Arcs
    drawCoreRings(
        center = center,
        radius = maxRadius,
        rot1 = rot1,
        rot2 = rot2,
        rot3 = rot3,
        rot4 = rot4,
        primaryColor = primaryColor,
        accentColor = accentColor
    )

    // LEVEL 2: Concentric Circles
    drawCircle(
        color = primaryColor.copy(alpha = 0.22f),
        radius = maxRadius * 0.60f,
        center = center,
        style = Stroke(width = 1.2f)
    )
    drawCircle(
        color = accentColor.copy(alpha = 0.30f),
        radius = maxRadius * 0.40f,
        center = center,
        style = Stroke(width = 1.0f)
    )

    // Processing arc (when state == PROCESSING)
    if (state == AssistantState.PROCESSING) {
        val procR = maxRadius * 0.88f
        val procSize = Size(procR * 2, procR * 2)
        val procTopLeft = Offset(center.x - procR, center.y - procR)
        drawArc(
            color = HackSuccess,
            startAngle = -90f,
            sweepAngle = 360f * processingProgress,
            useCenter = false,
            topLeft = procTopLeft,
            size = procSize,
            style = Stroke(width = 3.5f)
        )
    }

    // Success ripple wave (when state == SUCCESS)
    if (state == AssistantState.SUCCESS) {
        val waveR = maxRadius * (0.4f + (pulse * 0.55f))
        drawCircle(
            color = HackSuccess.copy(alpha = (1f - pulse).coerceIn(0f, 1f)),
            radius = waveR,
            center = center,
            style = Stroke(width = 2.0f)
        )
    }

    // LEVEL 1: Central Luminous Core (Breathing and Pulsing)
    val coreRadius = maxRadius * (0.22f + (pulse * 0.04f) + (if (state == AssistantState.LISTENING) audioAmplitude * 0.08f else 0f))
    
    // Core radial glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                glowColor.copy(alpha = 0.95f),
                primaryColor.copy(alpha = 0.80f),
                accentColor.copy(alpha = 0.40f),
                Color.Transparent
            ),
            center = center,
            radius = coreRadius * 1.5f
        ),
        radius = coreRadius * 1.4f,
        center = center
    )

    // Central solid core orb
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, glowColor, primaryColor),
            center = center,
            radius = coreRadius
        ),
        radius = coreRadius,
        center = center
    )
}
