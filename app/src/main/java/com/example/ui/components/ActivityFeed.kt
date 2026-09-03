package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.ui.theme.HackCyanDark
import com.example.ui.theme.HackCyanLight
import com.example.ui.theme.HackCyanPrimary
import com.example.ui.theme.HackError
import com.example.ui.theme.HackSuccess
import com.example.ui.theme.HackTextPrimary
import com.example.ui.theme.HackTextSecondary

data class ActivityItem(
    val id: String,
    val time: String,
    val message: String,
    val status: String = "SUCCESS" // SUCCESS, ERROR, PROCESSING, SYSTEM
)

@Composable
fun MiniActivityFeed(
    activities: List<ActivityItem>,
    modifier: Modifier = Modifier,
    onViewAllClick: () -> Unit = {}
) {
    HudPanel(
        title = "AI ACTIVITY",
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            activities.take(3).forEach { item ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically()
                ) {
                    ActivityFeedRow(item)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // VIEW ALL button styled with HUD typography
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onViewAllClick() }
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VIEW ALL ▸",
                    color = HackCyanLight,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun ActivityFeedRow(item: ActivityItem) {
    val dotColor = when (item.status) {
        "ERROR" -> HackError
        "PROCESSING" -> HackCyanPrimary
        "VOICE" -> HackCyanLight
        else -> HackSuccess
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.time,
            color = HackTextSecondary,
            fontSize = 9.5.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(55.dp)
        )
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = item.message,
            color = HackTextPrimary,
            fontSize = 10.5.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
    }
}
