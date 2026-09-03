package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HackBgBlack

@Composable
fun FuturisticAICore(
    state: AssistantState,
    audioAmplitude: Float = 0f,
    processingProgress: Float = 0.68f,
    size: Dp = 220.dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val speedMultiplier = when (state) {
        AssistantState.THINKING -> 3.2f
        AssistantState.PROCESSING -> 2.4f
        AssistantState.LISTENING -> 1.8f
        AssistantState.SPEAKING -> 1.4f
        AssistantState.SUCCESS -> 1.2f
        AssistantState.ERROR -> 0.8f
        AssistantState.IDLE -> 1.0f
    }

    val transition = rememberInfiniteTransition(label = "ai_core_anim")

    // Slow forward rotation
    val rot1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (14000 / speedMultiplier).toInt().coerceAtLeast(1000), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot1"
    )

    // Reverse medium rotation
    val rot2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (8500 / speedMultiplier).toInt().coerceAtLeast(800), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot2"
    )

    // Arc ring rotation
    val rot3 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (6000 / speedMultiplier).toInt().coerceAtLeast(600), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot3"
    )

    // Inner fast ring
    val rot4 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (4000 / speedMultiplier).toInt().coerceAtLeast(400), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot4"
    )

    // Breathing pulse
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AssistantState.LISTENING -> 600
                    AssistantState.THINKING -> 450
                    AssistantState.SPEAKING -> 800
                    AssistantState.SUCCESS -> 900
                    else -> 2200
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_pulse"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("futuristic_ai_core"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            drawAICoreCanvas(
                state = state,
                pulse = pulse,
                rot1 = rot1,
                rot2 = rot2,
                rot3 = rot3,
                rot4 = rot4,
                audioAmplitude = audioAmplitude,
                processingProgress = processingProgress
            )
        }

        // Center typography: T-AI or AI CORE
        Text(
            text = if (state == AssistantState.THINKING) "THINK" else if (state == AssistantState.PROCESSING) "PROC" else "T-AI",
            color = HackBgBlack,
            fontSize = (size.value * 0.075f).sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}
