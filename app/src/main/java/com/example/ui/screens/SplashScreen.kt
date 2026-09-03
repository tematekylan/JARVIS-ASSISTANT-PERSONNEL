package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HackBgBlack
import com.example.ui.theme.HackCyanDark
import com.example.ui.theme.HackCyanLight
import com.example.ui.theme.HackCyanPrimary
import com.example.ui.theme.HackTextPrimary
import com.example.ui.theme.HackTextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val dotScale = remember { Animatable(0f) }
    val ring1Alpha = remember { Animatable(0f) }
    val ring2Alpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val progressAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 1. Central dot appears & pulses
        dotScale.animateTo(1f, animationSpec = tween(400, easing = FastOutSlowInEasing))
        // 2. Circular rings expand
        ring1Alpha.animateTo(1f, animationSpec = tween(400, easing = LinearEasing))
        ring2Alpha.animateTo(1f, animationSpec = tween(400, easing = LinearEasing))
        // 3. Typography fades in
        textAlpha.animateTo(1f, animationSpec = tween(400, easing = LinearEasing))
        // 4. Loading line reaches 100%
        progressAnim.animateTo(1f, animationSpec = tween(600, easing = LinearEasing))
        delay(200)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HackBgBlack)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Background subtle grid and cyber lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // Ring 1
            if (ring1Alpha.value > 0f) {
                drawCircle(
                    color = HackCyanPrimary.copy(alpha = 0.35f * ring1Alpha.value),
                    radius = 90.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.5f)
                )
            }
            // Ring 2
            if (ring2Alpha.value > 0f) {
                drawCircle(
                    color = HackCyanDark.copy(alpha = 0.25f * ring2Alpha.value),
                    radius = 135.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.0f)
                )
            }
            // Center pulsing dot
            val dotR = 10.dp.toPx() * dotScale.value
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, HackCyanLight, HackCyanPrimary, Color.Transparent),
                    center = center,
                    radius = dotR * 2.5f
                ),
                radius = dotR * 2.5f,
                center = center
            )
        }

        // Center Titles
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(textAlpha.value)
        ) {
            Spacer(modifier = Modifier.height(130.dp))
            Text(
                text = "T-HACKMAN AI",
                color = HackCyanPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "PERSONAL ARTIFICIAL INTELLIGENCE",
                color = HackTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp
            )
        }

        // Bottom Initializing Progress Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp, start = 36.dp, end = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "INITIALIZING...",
                color = HackCyanLight,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(HackCyanDark.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressAnim.value)
                        .height(2.dp)
                        .background(HackCyanPrimary)
                )
            }
        }
    }
}
