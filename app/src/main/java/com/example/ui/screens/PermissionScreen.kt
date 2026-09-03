package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.HudPanel
import com.example.ui.theme.HackBgBlack
import com.example.ui.theme.HackCyanDark
import com.example.ui.theme.HackCyanLight
import com.example.ui.theme.HackCyanPrimary
import com.example.ui.theme.HackError
import com.example.ui.theme.HackPanelDark
import com.example.ui.theme.HackSuccess
import com.example.ui.theme.HackTextPrimary
import com.example.ui.theme.HackTextSecondary

@Composable
fun PermissionScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HackBgBlack)
            .testTag("permission_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HackCyanPrimary)
            }
            Text(
                text = "SYSTEM SETTINGS // PERMISSIONS",
                color = HackCyanPrimary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. MICROPHONE PERMISSION
            item {
                HudPanel(title = "1. CAPTEUR ACOUSTIQUE // MICROPHONE") {
                    Text(
                        text = "Nécessaire pour les commandes vocales directes et la détection acoustique.",
                        color = HackTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PermissionStatusRow(
                        name = "MICROPHONE",
                        isGranted = hasMicPermission,
                        onRequest = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                    )
                }
            }

            // 2. NETWORK & TELEMETRY PERMISSIONS
            item {
                HudPanel(title = "2. RÉSEAU ET DONNÉES // INTERNET") {
                    Text(
                        text = "Nécessaire pour communiquer avec les modèles neuronaux Google Gemini en temps réel.",
                        color = HackTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PermissionStatusRow(
                        name = "INTERNET UPLINK",
                        isGranted = true,
                        onRequest = {}
                    )
                }
            }

            // 3. STORAGE / PHOTO PICKER
            item {
                HudPanel(title = "3. SÉCURITÉ ANDROID // ZERO-PERMISSION PICKER") {
                    Text(
                        text = "T-HACK AI respecte les règles de confidentialité Google Play les plus strictes en utilisant le Photo Picker natif sans demander d'accès au stockage global.",
                        color = HackTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PermissionStatusRow(
                        name = "PHOTO PICKER",
                        isGranted = true,
                        onRequest = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionStatusRow(
    name: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(name, color = HackTextPrimary, fontSize = 11.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text(
                text = if (isGranted) "[GRANTED]" else "[DENIED]",
                color = if (isGranted) HackSuccess else HackError,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black
            )
        }

        if (!isGranted) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(HackCyanPrimary)
                    .clickable { onRequest() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("AUTORISER", color = HackBgBlack, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
    }
}
