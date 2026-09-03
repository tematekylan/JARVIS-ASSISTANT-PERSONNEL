package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.MessageEntity
import com.example.ui.components.FuturisticHeader
import com.example.ui.components.HudPanel
import com.example.ui.theme.HackBgBlack
import com.example.ui.theme.HackBlueAccent
import com.example.ui.theme.HackCyanDark
import com.example.ui.theme.HackCyanLight
import com.example.ui.theme.HackCyanPrimary
import com.example.ui.theme.HackPanelDark
import com.example.ui.theme.HackSuccess
import com.example.ui.theme.HackTextPrimary
import com.example.ui.theme.HackTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConversationScreen(
    messages: List<MessageEntity>,
    onSpeakMessage: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HackBgBlack)
            .testTag("conversation_screen")
    ) {
        FuturisticHeader(systemStatusText = "CONVERSATION LOG")

        Column(modifier = Modifier.padding(12.dp)) {
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    HudPanel(title = "STATUS") {
                        Text(
                            text = "NO ACTIVE CONVERSATION LOG.\nTransmit directives via the Command Center.",
                            color = HackTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->
                        val timeStr = timeFormatter.format(Date(msg.timestamp))
                        val isUser = msg.role == "user"

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isUser) HackPanelDark else HackPanelDark.copy(alpha = 0.95f))
                                .border(
                                    1.dp,
                                    if (isUser) HackBlueAccent.copy(alpha = 0.5f) else HackCyanPrimary.copy(alpha = 0.6f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Column {
                                // Technical Log Tag Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isUser) "[USER_001] $timeStr" else "[T-HACKMAN AI] $timeStr",
                                        color = if (isUser) HackBlueAccent else HackCyanPrimary,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (!isUser) {
                                        Text(
                                            text = "[REQUEST ANALYZED]",
                                            color = HackSuccess,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Message content
                                Text(
                                    text = msg.content,
                                    color = HackTextPrimary,
                                    fontSize = 12.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 18.sp
                                )

                                // Action buttons for assistant responses: COPY, SHARE, SPEAK
                                if (!isUser) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        LogActionButton(
                                            icon = Icons.Default.ContentCopy,
                                            label = "COPY",
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("T-HACK AI", msg.content))
                                                Toast.makeText(context, "Copié dans le presse-papiers", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        LogActionButton(
                                            icon = Icons.Default.Share,
                                            label = "SHARE",
                                            onClick = {
                                                val sendIntent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(Intent.EXTRA_TEXT, msg.content)
                                                    type = "text/plain"
                                                }
                                                context.startActivity(Intent.createChooser(sendIntent, "Partager via"))
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        LogActionButton(
                                            icon = Icons.Default.VolumeUp,
                                            label = "SPEAK",
                                            onClick = { onSpeakMessage(msg.content) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(HackBgBlack)
            .border(0.8.dp, HackCyanDark, RoundedCornerShape(3.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Icon(icon, contentDescription = label, tint = HackCyanLight, modifier = Modifier.size(11.dp))
        Text(text = label, color = HackCyanLight, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}
