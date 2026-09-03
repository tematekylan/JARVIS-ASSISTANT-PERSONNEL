package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.cos
import kotlin.math.sin

data class Particle(
    val baseAngle: Float,
    val distanceRatio: Float,
    val size: Float,
    val speedFactor: Float,
    val alpha: Float
)

object ParticleGenerator {
    // 32 pre-calculated particles for ultra-smooth 60fps rendering without heap allocations
    val particles: List<Particle> = (0 until 32).map { index ->
        val angle = (index * (360f / 32f))
        val distRatio = 0.55f + ((index * 13) % 40) / 100f // Between 0.55 and 0.95 of radius
        val pSize = 1.5f + ((index * 7) % 30) / 10f // 1.5f to 4.5f
        val speed = 0.6f + ((index * 17) % 80) / 100f // 0.6 to 1.4
        val alpha = 0.35f + ((index * 19) % 65) / 100f // 0.35 to 1.0
        Particle(
            baseAngle = angle,
            distanceRatio = distRatio,
            size = pSize,
            speedFactor = speed,
            alpha = alpha
        )
    }
}

fun DrawScope.drawCoreParticles(
    center: Offset,
    maxRadius: Float,
    rotationDeg: Float,
    color: Color,
    stateMultiplier: Float = 1.0f
) {
    val particles = ParticleGenerator.particles
    for (p in particles) {
        val currentAngle = Math.toRadians((p.baseAngle + (rotationDeg * p.speedFactor * stateMultiplier)).toDouble())
        val dist = maxRadius * p.distanceRatio
        val px = (center.x + dist * cos(currentAngle)).toFloat()
        val py = (center.y + dist * sin(currentAngle)).toFloat()
        
        drawCircle(
            color = color.copy(alpha = (p.alpha * 0.85f).coerceIn(0.1f, 1f)),
            radius = p.size,
            center = Offset(px, py)
        )
    }
}
