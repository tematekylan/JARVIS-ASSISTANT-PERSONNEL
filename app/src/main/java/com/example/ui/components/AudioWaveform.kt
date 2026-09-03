package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.ui.theme.HackCyanLight
import com.example.ui.theme.HackCyanPrimary
import kotlin.math.sin

@Composable
fun FuturisticAudioWaveform(
    isActive: Boolean,
    amplitude: Float = 0.5f,
    modifier: Modifier = Modifier.fillMaxWidth().height(28.dp),
    barCount: Int = 32,
    activeColor: Color = HackCyanPrimary,
    glowColor: Color = HackCyanLight
) {
    val transition = rememberInfiniteTransition(label = "waveform_anim")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveform_phase"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val step = width / (barCount + 1)

        val baseAmp = if (isActive) (amplitude.coerceIn(0.15f, 1.0f)) else 0.08f

        for (i in 1..barCount) {
            val x = i * step
            val wave1 = sin(phase + (i * 0.35f))
            val wave2 = sin(phase * 1.5f + (i * 0.7f))
            val combined = ((wave1 + wave2) * 0.5f + 1f) * 0.5f // 0..1
            val barHeight = ((combined * baseAmp * height * 0.85f) + 4f).coerceAtMost(height - 2f)

            val color = if (isActive) {
                if (i % 3 == 0) glowColor else activeColor
            } else {
                activeColor.copy(alpha = 0.2f)
            }

            drawLine(
                color = color,
                start = Offset(x, centerY - barHeight / 2f),
                end = Offset(x, centerY + barHeight / 2f),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
        }
    }
}
