package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBgCard
import com.example.ui.theme.JarvisBgSurface
import com.example.ui.theme.JarvisBgVoid
import com.example.ui.theme.JarvisBorderGlow
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

enum class JarvisScreen {
    CHAT,
    COMMAND_CENTER,
    MEMORY,
    NOTES,
    SETTINGS
}

@Composable
fun JarvisTopBar(
    currentScreen: JarvisScreen,
    onNavigate: (JarvisScreen) -> Unit,
    onNewChat: () -> Unit,
    isDemoMode: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = JarvisBgVoid,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 0.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(JarvisCyan.copy(alpha = 0.4f), Color.Transparent)
                ),
                shape = RoundedCornerShape(0.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Top Title & Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Logo & Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isDemoMode) JarvisAmber else JarvisEmerald)
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "JARVIS",
                                color = JarvisCyan,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "v2.5 // QUANTUM CORE",
                                color = JarvisTextMuted,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = if (isDemoMode) "● DEMO MODE ACTIVE" else "● NEURAL NETWORK ONLINE",
                            color = if (isDemoMode) JarvisAmber else JarvisEmerald,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Right: New Chat Button
                IconButton(
                    onClick = onNewChat,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisBgCard)
                        .border(1.dp, JarvisBorderGlow, RoundedCornerShape(8.dp))
                        .testTag("btn_new_chat")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Conversation",
                        tint = JarvisCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.padding(top = 10.dp))

            // Navigation Tabs Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NavTabButton(
                    title = "Chat",
                    icon = Icons.Default.Chat,
                    isSelected = currentScreen == JarvisScreen.CHAT,
                    onClick = { onNavigate(JarvisScreen.CHAT) }
                )
                NavTabButton(
                    title = "Command Center",
                    icon = Icons.Default.Dashboard,
                    isSelected = currentScreen == JarvisScreen.COMMAND_CENTER,
                    onClick = { onNavigate(JarvisScreen.COMMAND_CENTER) }
                )
                NavTabButton(
                    title = "Memory",
                    icon = Icons.Default.Psychology,
                    isSelected = currentScreen == JarvisScreen.MEMORY,
                    onClick = { onNavigate(JarvisScreen.MEMORY) }
                )
                NavTabButton(
                    title = "Notes",
                    icon = Icons.Default.Note,
                    isSelected = currentScreen == JarvisScreen.NOTES,
                    onClick = { onNavigate(JarvisScreen.NOTES) }
                )
                NavTabButton(
                    title = "Settings",
                    icon = Icons.Default.Settings,
                    isSelected = currentScreen == JarvisScreen.SETTINGS,
                    onClick = { onNavigate(JarvisScreen.SETTINGS) }
                )
            }
        }
    }
}

@Composable
fun NavTabButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = JarvisCyan
    val inactiveColor = JarvisTextMuted

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) JarvisCyan.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (isSelected) JarvisCyan.copy(alpha = 0.6f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("nav_tab_$title"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) activeColor else inactiveColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = title,
                color = if (isSelected) activeColor else inactiveColor,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
