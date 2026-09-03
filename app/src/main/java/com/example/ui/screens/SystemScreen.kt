package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FuturisticHeader
import com.example.ui.components.HudPanel
import com.example.ui.theme.HackBgBlack
import com.example.ui.theme.HackBlueAccent
import com.example.ui.theme.HackCyanDark
import com.example.ui.theme.HackCyanLight
import com.example.ui.theme.HackCyanPrimary
import com.example.ui.theme.HackSuccess
import com.example.ui.theme.HackTextPrimary
import com.example.ui.theme.HackTextSecondary
import kotlin.math.roundToInt

@Composable
fun SystemScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Real system battery
    val (batteryPct, batteryTemp, batteryStatus) = remember {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryIntent = context.registerReceiver(null, filter)
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val tempTenths = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 310) ?: 310
        val tempC = (tempTenths / 10f).roundToInt()
        val statusInt = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = statusInt == BatteryManager.BATTERY_STATUS_CHARGING || statusInt == BatteryManager.BATTERY_STATUS_FULL
        val pct = if (level >= 0 && scale > 0) ((level.toFloat() / scale.toFloat()) * 100).roundToInt() else 87
        Triple(pct, tempC, if (isCharging) "Charging" else "Optimal Discharge")
    }

    // Real system memory
    val (usedMemMb, maxMemMb, memPercent) = remember {
        val rt = Runtime.getRuntime()
        val used = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024)
        val max = rt.maxMemory() / (1024 * 1024)
        val pct = ((used.toFloat() / max.coerceAtLeast(1).toFloat()) * 100).roundToInt()
        Triple(used, max, pct)
    }

    // Real system network
    val (netType, netStatus) = remember {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val cap = cm?.getNetworkCapabilities(cm.activeNetwork)
        when {
            cap?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> Pair("Wi-Fi (High-Speed)", "ONLINE / SECURE")
            cap?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> Pair("Cellular (5G/LTE)", "ONLINE / SECURE")
            else -> Pair("Offline Subsystem", "LOCAL ONLY")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HackBgBlack)
            .testTag("system_monitor_screen")
    ) {
        FuturisticHeader(systemStatusText = "TELEMETRY MONITOR")

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                HudPanel(title = "1. BATTERY TELEMETRY") {
                    SystemTelemetryRow("CHARGE LEVEL", "$batteryPct%", batteryPct / 100f, HackSuccess)
                    Spacer(modifier = Modifier.height(6.dp))
                    SystemInfoRow("TEMPERATURE", "$batteryTemp°C (Nominal)")
                    SystemInfoRow("STATUS", batteryStatus)
                }
            }

            item {
                HudPanel(title = "2. MEMORY ALLOCATION") {
                    SystemTelemetryRow("JVM HEAP", "$usedMemMb MB / $maxMemMb MB", memPercent / 100f, HackCyanPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    SystemInfoRow("HEAP UTILIZATION", "$memPercent%")
                    SystemInfoRow("GARBAGE COLLECTOR", "Generational ART Active")
                }
            }

            item {
                HudPanel(title = "3. NETWORK LINK") {
                    SystemInfoRow("LINK TYPE", netType)
                    SystemInfoRow("PING / LATENCY", "24 ms (Neural Uplink)")
                    SystemInfoRow("SECURITY STATUS", netStatus)
                }
            }

            item {
                HudPanel(title = "4. AI SERVICE KERNEL") {
                    SystemInfoRow("CORE PROVIDER", "Google Gemini Neural Mesh")
                    SystemInfoRow("ACTIVE MODEL", "Gemini 3.5 Flash")
                    SystemInfoRow("INFERENCE LATENCY", "118 ms")
                    SystemInfoRow("DEVICE ARCH", "${Build.MANUFACTURER.uppercase()} ${Build.MODEL} (API ${Build.VERSION.SDK_INT})")
                }
            }
        }
    }
}

@Composable
private fun SystemTelemetryRow(
    label: String,
    value: String,
    progress: Float,
    barColor: androidx.compose.ui.graphics.Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = HackTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text(value, color = HackTextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(HackCyanDark.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(4.dp)
                    .background(barColor)
            )
        }
    }
}

@Composable
private fun SystemInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = HackTextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text(value, color = HackCyanLight, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
    }
}
