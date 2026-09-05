package com.thackman.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thackman.ai.service.HardwareController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandCenterScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val hardware = remember { HardwareController(context) }
    var isTorchOn by remember { mutableStateOf(false) }
    val batteryLevel by remember { mutableIntStateOf(hardware.getBatteryLevel()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Centre de Commande Cyber", color = Color(0xFFE5FCFF), fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color(0xFF00E5FF)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF070D12))
            )
        },
        containerColor = Color(0xFF030609)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Battery Status Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0D1821),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF007C91).copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ÉNERGIE MATÉRIELLE",
                        fontSize = 11.sp,
                        color = Color(0xFF6F9DA6),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Niveau de Batterie : $batteryLevel%",
                        fontSize = 20.sp,
                        color = Color(0xFF31F5A3),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Quick Hardware Actions
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0D1821),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF007C91).copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CONTRÔLES MATÉRIELS DIRECTS",
                        fontSize = 11.sp,
                        color = Color(0xFF6F9DA6),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                isTorchOn = !isTorchOn
                                hardware.toggleFlashlight(isTorchOn)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTorchOn) Color(0xFF00E5FF) else Color(0xFF0A1219)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashlightOn,
                                contentDescription = "Lampe",
                                tint = if (isTorchOn) Color.Black else Color(0xFF00E5FF)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTorchOn) "Torche ON" else "Torche OFF",
                                color = if (isTorchOn) Color.Black else Color(0xFFE5FCFF)
                            )
                        }

                        Button(
                            onClick = { hardware.vibrate(250) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A1219)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = "Vibration",
                                tint = Color(0xFF31F5A3)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Haptique", color = Color(0xFFE5FCFF))
                        }
                    }
                }
            }
        }
    }
}
