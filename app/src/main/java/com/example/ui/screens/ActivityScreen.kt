package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ActivityItem
import com.example.ui.components.FuturisticHeader
import com.example.ui.components.HudPanel
import com.example.ui.theme.HackBgBlack
import com.example.ui.theme.HackBlueAccent
import com.example.ui.theme.HackCyanDark
import com.example.ui.theme.HackCyanLight
import com.example.ui.theme.HackCyanPrimary
import com.example.ui.theme.HackError
import com.example.ui.theme.HackPanelDark
import com.example.ui.theme.HackSuccess
import com.example.ui.theme.HackTextPrimary
import com.example.ui.theme.HackTextSecondary

@Composable
fun ActivityScreen(
    activities: List<ActivityItem> = listOf(
        ActivityItem("1", "10:42", "Connexion neuronale établie", "SYSTEM"),
        ActivityItem("2", "10:40", "Requête d'intelligence artificielle traitée", "AI"),
        ActivityItem("3", "10:38", "Reconnaissance vocale initialisée", "VOICE"),
        ActivityItem("4", "10:35", "Indexation de la mémoire persistante", "SYSTEM"),
        ActivityItem("5", "10:30", "Diagnostic des sous-systèmes T-HACK AI", "SYSTEM")
    ),
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    val filters = listOf("ALL", "AI", "VOICE", "SYSTEM", "ERROR")

    val filteredActivities = remember(activities, selectedFilter) {
        if (selectedFilter == "ALL") activities
        else activities.filter { it.status.equals(selectedFilter, ignoreCase = true) }
    }

    val totalCount = activities.size
    val errorCount = activities.count { it.status == "ERROR" }
    val successCount = totalCount - errorCount

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HackBgBlack)
            .testTag("activity_screen")
    ) {
        FuturisticHeader(systemStatusText = "ACTIVITY TIMELINE")

        Column(modifier = Modifier.padding(16.dp)) {
            // Counters Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActivityCounterCard("TOTAL", "$totalCount", HackCyanPrimary, Modifier.weight(1f))
                ActivityCounterCard("SUCCESS", "$successCount", HackSuccess, Modifier.weight(1f))
                ActivityCounterCard("ERRORS", "$errorCount", HackError, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filters row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filters) { filter ->
                    val isSelected = filter == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) HackCyanPrimary else HackPanelDark)
                            .border(
                                1.dp,
                                if (isSelected) HackCyanLight else HackCyanDark.copy(alpha = 0.5f),
                                RoundedCornerShape(4.dp)
                            )
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) HackBgBlack else HackCyanLight,
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vertical Timeline
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredActivities) { item ->
                    TimelineItemCard(item)
                }
            }
        }
    }
}

@Composable
private fun ActivityCounterCard(
    label: String,
    count: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(HackPanelDark)
            .border(1.dp, HackCyanDark.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(vertical = 6.dp, horizontal = 8.dp)
    ) {
        Column {
            Text(
                text = label,
                color = HackTextSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = count,
                color = color,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TimelineItemCard(item: ActivityItem) {
    val dotColor = when (item.status) {
        "ERROR" -> HackError
        "VOICE" -> HackBlueAccent
        "SYSTEM" -> HackCyanLight
        else -> HackSuccess
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        // Vertical Timeline stem
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .height(44.dp)
                    .background(HackCyanDark.copy(alpha = 0.4f))
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Content
        HudPanel(
            modifier = Modifier.weight(1f),
            contentPadding = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.status,
                    color = dotColor,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.time,
                    color = HackTextSecondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.message,
                color = HackTextPrimary,
                fontSize = 11.5.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
