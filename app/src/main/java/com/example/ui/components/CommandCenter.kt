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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HackBgBlack
import com.example.ui.theme.HackBlueAccent
import com.example.ui.theme.HackCyanDark
import com.example.ui.theme.HackCyanLight
import com.example.ui.theme.HackCyanPrimary
import com.example.ui.theme.HackError
import com.example.ui.theme.HackPanelDark
import com.example.ui.theme.HackTextPrimary
import com.example.ui.theme.HackTextSecondary

@Composable
fun FuturisticCommandCenter(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isListening: Boolean,
    onMicClick: () -> Unit,
    isProcessing: Boolean = false,
    onStopClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(HackPanelDark.copy(alpha = 0.90f))
            .border(1.2.dp, HackCyanDark.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("command_center_panel")
    ) {
        Column {
            // Header bar of command center
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COMMAND CENTER",
                    color = HackCyanLight,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                if (isProcessing) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(HackError.copy(alpha = 0.2f))
                            .border(0.8.dp, HackError, RoundedCornerShape(4.dp))
                            .clickable { onStopClick() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PROCESSING... [STOP]",
                            color = HackError,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Input line with Mic, Text Box, Send
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FuturisticMicButton(
                    isListening = isListening,
                    onClick = onMicClick,
                    size = 40.dp
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Text field styled with cyan border and monospace cursor
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(HackBgBlack.copy(alpha = 0.85f))
                        .border(1.dp, if (inputText.isNotBlank()) HackCyanPrimary else HackCyanDark.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (inputText.isEmpty()) {
                        Text(
                            text = "ENTER COMMAND...",
                            color = HackTextSecondary.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                    BasicTextField(
                        value = inputText,
                        onValueChange = onInputChange,
                        textStyle = TextStyle(
                            color = HackTextPrimary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        cursorBrush = SolidColor(HackCyanPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("command_input_field"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Send button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (inputText.isNotBlank()) HackCyanPrimary else HackPanelDark)
                        .border(
                            1.dp,
                            if (inputText.isNotBlank()) HackCyanLight else HackCyanDark.copy(alpha = 0.5f),
                            RoundedCornerShape(6.dp)
                        )
                        .clickable(enabled = inputText.isNotBlank()) { onSendClick() }
                        .testTag("command_send_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Command",
                        tint = if (inputText.isNotBlank()) HackBgBlack else HackCyanDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
