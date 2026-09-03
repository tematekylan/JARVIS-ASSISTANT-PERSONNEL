package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HackCyanDark
import com.example.ui.theme.HackCyanLight
import com.example.ui.theme.HackCyanPrimary
import com.example.ui.theme.HackPanelDark
import com.example.ui.theme.HackTextSecondary

@Composable
fun HudPanel(
    title: String? = null,
    modifier: Modifier = Modifier,
    borderColor: Color = HackCyanDark.copy(alpha = 0.45f),
    contentPadding: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(HackPanelDark.copy(alpha = 0.75f))
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(contentPadding)
    ) {
        Column {
            if (title != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title.uppercase(),
                        color = HackCyanPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    // HUD corner accent
                    Box(
                        modifier = Modifier
                            .background(HackCyanLight.copy(alpha = 0.6f))
                            .height(2.dp)
                            .fillMaxWidth(0.15f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            content()
        }
    }
}
