package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HackBlueAccent
import com.example.ui.theme.HackCyanDark
import com.example.ui.theme.HackCyanLight
import com.example.ui.theme.HackCyanPrimary
import com.example.ui.theme.HackPanelDark
import com.example.ui.theme.HackSuccess
import com.example.ui.theme.HackTextPrimary
import com.example.ui.theme.HackTextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FuturisticHeader(
    modifier: Modifier = Modifier,
    systemStatusText: String = "SYSTEM ONLINE",
    isOnline: Boolean = true
) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEE • MMM dd", Locale.US)
        while (true) {
            val now = Date()
            currentTime = timeFormat.format(now)
            currentDate = dateFormat.format(now).uppercase()
            delay(1000)
        }
    }

    val pulseTransition = rememberInfiniteTransition(label = "pulse_online")
    val dotAlpha by pulseTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Tech tags top line
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TechMicroBadge(text = "SYS_001")
                TechMicroBadge(text = "CORE_READY")
                TechMicroBadge(text = "NET_SECURE")
            }
            Text(
                text = currentDate,
                color = HackTextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Main Title and Live Clock Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "T-HACKMAN AI",
                    color = HackCyanPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "YOUR PERSONAL ARTIFICIAL INTELLIGENCE",
                    color = HackTextSecondary,
                    fontSize = 9.sp,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Real-time Clock & Online Indicator
            Column(horizontalAlignment = Alignment.End) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isOnline) HackSuccess.copy(alpha = dotAlpha) else HackCyanDark)
                    )
                    Text(
                        text = systemStatusText,
                        color = if (isOnline) HackCyanLight else HackTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = currentTime.ifBlank { "18:42:17" },
                    color = HackTextPrimary,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fine cyan cyber line with corner brackets
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            HackCyanPrimary.copy(alpha = 0.8f),
                            HackBlueAccent,
                            HackCyanPrimary.copy(alpha = 0.8f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun TechMicroBadge(text: String) {
    Box(
        modifier = Modifier
            .background(HackPanelDark, RoundedCornerShape(2.dp))
            .border(0.7.dp, HackCyanDark.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
            .padding(horizontal = 5.dp, vertical = 1.5.dp)
    ) {
        Text(
            text = text,
            color = HackCyanLight,
            fontSize = 8.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}
