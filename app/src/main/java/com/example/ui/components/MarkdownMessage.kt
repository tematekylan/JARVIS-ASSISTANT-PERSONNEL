package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.entity.MessageEntity
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBgCard
import com.example.ui.theme.JarvisBgCardElevated
import com.example.ui.theme.JarvisBgSurface
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisBorderBright
import com.example.ui.theme.JarvisBorderGlow
import com.example.ui.theme.JarvisCrimson
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisEmerald
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(
    message: MessageEntity,
    onSpeak: (String) -> Unit,
    modifier: Modifier = Modifier,
    isLiveStreaming: Boolean = false
) {
    val isUser = message.role == "user"
    val context = LocalContext.current

    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bubbleBg = if (isUser) Color(0xFF0F2648) else JarvisBgCard
    val borderColor = if (isUser) JarvisBlue.copy(alpha = 0.7f) else if (isLiveStreaming) JarvisCyan else JarvisBorderGlow

    Column(
        horizontalAlignment = alignment,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        // Role & Time Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.padding(bottom = 3.dp, start = 4.dp, end = 4.dp)
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (isLiveStreaming) JarvisCyan else JarvisCyan.copy(alpha = 0.2f))
                        .border(1.dp, JarvisCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isLiveStreaming) Color.White else JarvisCyan)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isLiveStreaming) "JARVIS • EN COURS..." else "JARVIS",
                    color = if (isLiveStreaming) JarvisCyanGlow else JarvisCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            } else {
                Text(
                    text = "COMMANDER",
                    color = JarvisBlue.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
            Text(
                text = timeStr,
                color = JarvisTextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Attached Image
        if (!message.imageUri.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .widthIn(max = 280.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = message.imageUri,
                    contentDescription = "Attached visual data",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Tool Badge if executed
        if (!message.toolName.isNullOrBlank()) {
            ToolBadge(toolName = message.toolName, toolInput = message.toolInput)
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Message Content Box
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.88f else 0.98f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(bubbleBg)
                .border(
                    width = if (isLiveStreaming) 1.5.dp else 1.dp,
                    color = if (message.isError) JarvisCrimson else borderColor,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(14.dp)
        ) {
            Column {
                RenderMarkdownContent(
                    content = if (isLiveStreaming) "${message.content} ▌" else message.content,
                    isError = message.isError
                )

                // Message Actions for Assistant responses (hidden during live streaming)
                if (!isUser && message.content.isNotBlank() && !isLiveStreaming) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { onSpeak(message.content) },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("btn_speak_msg")
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Read aloud",
                                tint = JarvisCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("JARVIS Response", message.content)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Transcribed to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("btn_copy_msg")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy message",
                                tint = JarvisTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThinkingBubble(
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 3.dp, start = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(JarvisCyanGlow.copy(alpha = 0.3f))
                    .border(1.dp, JarvisCyanGlow, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(JarvisCyanGlow)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "JARVIS • RÉFLEXION EN COURS",
                color = JarvisCyanGlow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp))
                .background(JarvisBgCard)
                .border(1.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp))
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = JarvisCyan
                )
                Text(
                    text = "Analyse neurale et synthèse en direct...",
                    color = JarvisTextSecondary,
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

@Composable
fun ToolBadge(toolName: String, toolInput: String?) {
    val (icon, color, label) = when (toolName.lowercase()) {
        "calculator" -> Triple(Icons.Default.Calculate, JarvisAmber, "CALCULATOR TOOL")
        "weather" -> Triple(Icons.Default.Cloud, JarvisCyan, "METEOROLOGY SCAN")
        "world_time", "time" -> Triple(Icons.Default.Schedule, JarvisCyanGlow, "CHRONO SYNC")
        "system_status", "diagnostics" -> Triple(Icons.Default.Memory, JarvisEmerald, "SYSTEM DIAGNOSTICS")
        "notes_manager", "notes" -> Triple(Icons.Default.Note, JarvisBlue, "NOTE ARCHIVE")
        "memory_vault", "memory" -> Triple(Icons.Default.Psychology, JarvisEmerald, "MEMORY VAULT")
        "web_search", "search" -> Triple(Icons.Default.TravelExplore, JarvisCyan, "WEB KNOWLEDGE UPLINK")
        else -> Triple(Icons.Default.Memory, JarvisCyan, "TOOL ENGINE: $toolName")
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Text(
                text = label,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            if (!toolInput.isNullOrBlank()) {
                Text(
                    text = "($toolInput)",
                    color = JarvisTextMuted,
                    fontSize = 9.sp,
                    maxLines = 1,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun RenderMarkdownContent(content: String, isError: Boolean = false) {
    val lines = content.split("\n")
    var inCodeBlock = false
    val currentCodeLines = mutableListOf<String>()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (line in lines) {
            if (line.startsWith("```")) {
                if (inCodeBlock) {
                    // Close code block
                    CodeBlockView(code = currentCodeLines.joinToString("\n"))
                    currentCodeLines.clear()
                    inCodeBlock = false
                } else {
                    inCodeBlock = true
                }
                continue
            }

            if (inCodeBlock) {
                currentCodeLines.add(line)
                continue
            }

            when {
                line.startsWith("![") && line.contains("](") && line.endsWith(")") -> {
                    val alt = line.substringAfter("![").substringBefore("](")
                    val url = line.substringAfter("](").substringBeforeLast(")")
                    MediaImageCard(alt = alt, imageUrl = url)
                }
                line.startsWith("# ") -> {
                    Text(
                        text = line.removePrefix("# ").trim(),
                        color = JarvisCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                }
                line.startsWith("## ") -> {
                    Text(
                        text = line.removePrefix("## ").trim(),
                        color = JarvisCyanGlow,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                line.startsWith("### ") -> {
                    Text(
                        text = line.removePrefix("### ").trim(),
                        color = JarvisBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                line.startsWith("> ") -> {
                    QuoteBlockView(quote = line.removePrefix("> ").trim())
                }
                line.startsWith("- ") || line.startsWith("* ") || line.startsWith("• ") -> {
                    Row(modifier = Modifier.padding(start = 4.dp)) {
                        Text(text = "● ", color = JarvisCyan, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                        FormattedInlineText(
                            text = line.removePrefix("- ").removePrefix("* ").removePrefix("• ").trim(),
                            isError = isError
                        )
                    }
                }
                else -> {
                    FormattedInlineText(text = line, isError = isError)
                }
            }
        }

        if (inCodeBlock && currentCodeLines.isNotEmpty()) {
            CodeBlockView(code = currentCodeLines.joinToString("\n"))
        }
    }
}

@Composable
fun CodeBlockView(code: String) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF060B17))
            .border(1.dp, JarvisBorderBright, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CODE MATRIX",
                    color = JarvisCyan,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("JARVIS Code", code)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = JarvisTextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = code,
                color = JarvisEmerald,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun QuoteBlockView(quote: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(24.dp)
                .background(JarvisCyan)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = quote,
            color = JarvisTextSecondary,
            fontSize = 13.sp,
            fontStyle = FontStyle.Italic
        )
    }
}

@Composable
fun FormattedInlineText(text: String, isError: Boolean = false) {
    val annotatedString = buildAnnotatedString {
        val parts = text.split("**")
        var isBold = false
        for (part in parts) {
            if (isBold) {
                withStyle(
                    style = SpanStyle(
                        color = if (isError) JarvisCrimson else JarvisCyanGlow,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(part)
                }
            } else {
                withStyle(
                    style = SpanStyle(
                        color = if (isError) JarvisCrimson else JarvisTextPrimary
                    )
                ) {
                    append(part)
                }
            }
            isBold = !isBold
        }
    }

    Text(
        text = annotatedString,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
}

@Composable
fun MediaImageCard(alt: String, imageUrl: String) {
    val context = LocalContext.current
    var isFullScreenOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF040914))
            .border(1.dp, JarvisCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(JarvisBgCard)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = JarvisCyanGlow,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SYNTHÈSE VISUELLE IA",
                    color = JarvisCyanGlow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Image URL", imageUrl)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Lien copié dans le presse-papier", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copier le lien",
                        tint = JarvisTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
                IconButton(
                    onClick = { isFullScreenOpen = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Plein écran",
                        tint = JarvisCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Image Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clickable { isFullScreenOpen = true },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = alt,
                modifier = Modifier.fillMaxWidth().height(240.dp),
                contentScale = ContentScale.Crop
            )
        }

        if (alt.isNotBlank()) {
            Text(
                text = alt,
                color = JarvisTextSecondary,
                fontSize = 11.sp,
                maxLines = 2,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }

    if (isFullScreenOpen) {
        Dialog(onDismissRequest = { isFullScreenOpen = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF030712))
                    .border(1.5.dp, JarvisCyanGlow, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "APERÇU PLEIN ÉCRAN",
                            color = JarvisCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        IconButton(
                            onClick = { isFullScreenOpen = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fermer",
                                tint = JarvisCyanGlow,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = alt,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = alt,
                        color = JarvisTextPrimary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}
