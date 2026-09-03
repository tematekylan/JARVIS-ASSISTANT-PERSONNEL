package com.example.ui.components

import androidx.compose.foundation.background
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
import com.example.ui.theme.HackSuccess
import com.example.ui.theme.HackTextPrimary
import com.example.ui.theme.HackTextSecondary

@Composable
fun SystemStatusPanel(
    modifier: Modifier = Modifier,
    cpuUsage: Int = 24,
    ramUsage: Int = 41,
    batteryPercent: Int = 87,
    networkStatus: String = "ONLINE",
    aiEngineStatus: String = "READY"
) {
    HudPanel(
        title = "SYSTEM STATUS",
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // CPU & RAM telemetry rows
            TelemetryMetricRow(
                label = "CPU",
                valueText = "$cpuUsage%",
                progress = cpuUsage / 100f,
                barColor = HackCyanPrimary
            )
            TelemetryMetricRow(
                label = "RAM",
                valueText = "$ramUsage%",
                progress = ramUsage / 100f,
                barColor = HackBlueAccent
            )
            TelemetryMetricRow(
                label = "BATTERY",
                valueText = "$batteryPercent%",
                progress = batteryPercent / 100f,
                barColor = HackSuccess
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Network & Engine status pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusIndicator(label = "NET", status = networkStatus, activeColor = HackSuccess)
                StatusIndicator(label = "AI ENGINE", status = aiEngineStatus, activeColor = HackCyanPrimary)
            }
        }
    }
}

@Composable
private fun TelemetryMetricRow(
    label: String,
    valueText: String,
    progress: Float,
    barColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = HackTextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(62.dp)
        )
        // Ultra-thin HUD progress bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(3.5.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(HackCyanDark.copy(alpha = 0.25f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(3.5.dp)
                    .background(barColor)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = valueText,
            color = HackTextPrimary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatusIndicator(
    label: String,
    status: String,
    activeColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(activeColor)
        )
        Text(
            text = "$label: $status",
            color = HackTextSecondary,
            fontSize = 9.5.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
