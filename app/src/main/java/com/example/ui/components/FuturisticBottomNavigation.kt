package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HackBgBlack
import com.example.ui.theme.HackCyanDark
import com.example.ui.theme.HackCyanLight
import com.example.ui.theme.HackCyanPrimary
import com.example.ui.theme.HackPanelDark
import com.example.ui.theme.HackTextSecondary

enum class THackNavScreen(val label: String, val icon: ImageVector) {
    ASSISTANT("ASSISTANT", Icons.Default.Home),
    ACTIVITY("ACTIVITY", Icons.Default.ListAlt),
    TASKS("TASKS", Icons.Default.CheckCircle),
    MEMORY("MEMORY", Icons.Default.Dataset),
    SETTINGS("SETTINGS", Icons.Default.Settings)
}

@Composable
fun FuturisticBottomNavigation(
    currentScreen: THackNavScreen,
    onScreenSelected: (THackNavScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(HackBgBlack.copy(alpha = 0.96f))
            .border(
                width = 1.dp,
                color = HackCyanDark.copy(alpha = 0.4f)
            )
            .padding(vertical = 6.dp, horizontal = 8.dp)
            .testTag("futuristic_bottom_nav")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            THackNavScreen.values().forEach { screen ->
                val isSelected = currentScreen == screen
                val isAssistant = screen == THackNavScreen.ASSISTANT
                val interactionSource = remember { MutableInteractionSource() }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onScreenSelected(screen) }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .testTag("nav_item_${screen.name.lowercase()}")
                ) {
                    if (isAssistant) {
                        // Special highlighted central HUD diamond/circle for Assistant
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) HackCyanPrimary else HackPanelDark)
                                .border(
                                    1.dp,
                                    if (isSelected) HackCyanLight else HackCyanDark.copy(alpha = 0.6f),
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.label,
                                tint = if (isSelected) HackBgBlack else HackCyanPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.label,
                            tint = if (isSelected) HackCyanPrimary else HackTextSecondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = screen.label,
                        color = if (isSelected) HackCyanPrimary else HackTextSecondary.copy(alpha = 0.6f),
                        fontSize = 8.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Luminous line indicator under active item
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .fillMaxWidth(if (isSelected) 0.8f else 0f)
                            .background(if (isSelected) HackCyanLight else Color.Transparent)
                    )
                }
            }
        }
    }
}
