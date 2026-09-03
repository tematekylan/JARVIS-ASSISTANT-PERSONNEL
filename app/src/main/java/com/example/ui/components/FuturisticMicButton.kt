package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.HackBgBlack
import com.example.ui.theme.HackCyanDark
import com.example.ui.theme.HackCyanLight
import com.example.ui.theme.HackCyanPrimary
import com.example.ui.theme.HackPanelDark

@Composable
fun FuturisticMicButton(
    isListening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val transition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )
    val ringAlpha by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mic_ring_alpha"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size + 12.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("futuristic_mic_button"),
        contentAlignment = Alignment.Center
    ) {
        // Expanding pulse rings when listening
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(size)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .border(1.5.dp, HackCyanLight.copy(alpha = ringAlpha), CircleShape)
            )
        }

        // Central circular button
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(if (isListening) HackCyanPrimary else HackPanelDark)
                .border(
                    width = 1.5.dp,
                    color = if (isListening) HackCyanLight else HackCyanDark.copy(alpha = 0.8f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                contentDescription = if (isListening) "Stop Listening" else "Start Listening",
                tint = if (isListening) HackBgBlack else HackCyanPrimary,
                modifier = Modifier.size(size * 0.52f)
            )
        }
    }
}
