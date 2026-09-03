package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin

fun DrawScope.drawCoreRings(
    center: Offset,
    radius: Float,
    rot1: Float,
    rot2: Float,
    rot3: Float,
    rot4: Float,
    primaryColor: Color,
    accentColor: Color
) {
    // Ring 1: Slow forward rotation with fine tech line and tick segments
    rotate(rot1, pivot = center) {
        drawCircle(
            color = primaryColor.copy(alpha = 0.35f),
            radius = radius * 0.95f,
            center = center,
            style = Stroke(width = 1.5f)
        )
        // Draw 4 cardinal alignment notches
        for (i in 0 until 4) {
            val angle = i * 90f
            val rad = Math.toRadians(angle.toDouble())
            val startR = radius * 0.92f
            val endR = radius * 0.98f
            drawLine(
                color = accentColor,
                start = Offset((center.x + startR * cos(rad)).toFloat(), (center.y + startR * sin(rad)).toFloat()),
                end = Offset((center.x + endR * cos(rad)).toFloat(), (center.y + endR * sin(rad)).toFloat()),
                strokeWidth = 2.5f
            )
        }
    }

    // Ring 2: Reverse rotation with distinct dashed pattern
    rotate(-rot2, pivot = center) {
        val dashPath = PathEffect.dashPathEffect(floatArrayOf(14f, 10f, 6f, 10f), 0f)
        drawCircle(
            color = accentColor.copy(alpha = 0.5f),
            radius = radius * 0.82f,
            center = center,
            style = Stroke(width = 1.8f, pathEffect = dashPath)
        )
    }

    // Ring 3: Rotation with interruptions & 6 segmented arc brackets
    rotate(rot3, pivot = center) {
        val arcR = radius * 0.70f
        val arcSize = Size(arcR * 2, arcR * 2)
        val arcTopLeft = Offset(center.x - arcR, center.y - arcR)

        for (i in 0 until 3) {
            drawArc(
                color = primaryColor,
                startAngle = i * 120f + 15f,
                sweepAngle = 70f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )
        }
    }

    // Ring 4: Inner high-speed segmented ring (appearing/disappearing arcs)
    rotate(-rot4, pivot = center) {
        val innerR = radius * 0.52f
        val innerSize = Size(innerR * 2, innerR * 2)
        val innerTopLeft = Offset(center.x - innerR, center.y - innerR)

        for (i in 0 until 4) {
            drawArc(
                color = primaryColor.copy(alpha = 0.7f),
                startAngle = i * 90f + 10f,
                sweepAngle = 40f,
                useCenter = false,
                topLeft = innerTopLeft,
                size = innerSize,
                style = Stroke(width = 2f)
            )
        }
    }

    // Ring 5: Orbital dots on perimeter ring
    rotate(rot1 * 1.5f, pivot = center) {
        val dotR = radius * 0.88f
        for (i in 0 until 8) {
            val angle = i * 45f
            val rad = Math.toRadians(angle.toDouble())
            drawCircle(
                color = if (i % 2 == 0) primaryColor else accentColor,
                radius = if (i % 2 == 0) 3f else 2f,
                center = Offset(
                    (center.x + dotR * cos(rad)).toFloat(),
                    (center.y + dotR * sin(rad)).toFloat()
                )
            )
        }
    }
}
