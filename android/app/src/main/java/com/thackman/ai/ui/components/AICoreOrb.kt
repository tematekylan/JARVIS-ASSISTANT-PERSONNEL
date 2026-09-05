package com.thackman.ai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.thackman.ai.model.AssistantState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AICoreOrb(
    state: AssistantState,
    amplitude: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "core_anim")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val baseColor = when (state) {
        AssistantState.LISTENING -> Color(0xFF00E5FF)
        AssistantState.THINKING -> Color(0xFF78F7FF)
        AssistantState.SPEAKING -> Color(0xFF31F5A3)
        AssistantState.ERROR -> Color(0xFFFF4660)
        else -> Color(0xFF00E5FF)
    }

    Box(
        modifier = modifier
            .size(240.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2.8f) * pulse * (1f + amplitude * 0.25f)

            // Outer energy glow ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(baseColor.copy(alpha = 0.35f), Color.Transparent),
                    center = center,
                    radius = baseRadius * 1.5f
                ),
                radius = baseRadius * 1.5f,
                center = center
            )

            // Dynamic segmented cyber orbit ring
            val segments = 32
            for (i in 0 until segments) {
                val angle = Math.toRadians((i * (360.0 / segments) + rotation)).toFloat()
                val r1 = baseRadius * 1.15f
                val r2 = baseRadius * 1.25f
                val start = Offset(center.x + cos(angle) * r1, center.y + sin(angle) * r1)
                val end = Offset(center.x + cos(angle) * r2, center.y + sin(angle) * r2)
                drawLine(
                    color = baseColor.copy(alpha = 0.6f),
                    start = start,
                    end = end,
                    strokeWidth = 2.dp.toPx()
                )
            }

            // Inner glowing core
            drawCircle(
                color = baseColor.copy(alpha = 0.85f),
                radius = baseRadius * 0.7f,
                center = center
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = baseRadius * 0.35f,
                center = center
            )
        }
    }
}
