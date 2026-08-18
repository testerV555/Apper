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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionStatus
import com.example.model.VpnConfig
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkNavyCard
import com.example.ui.theme.DarkNavyCardBorder
import com.example.ui.theme.DarkNavySurface
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun LiveTrafficCard(
    status: ConnectionStatus,
    selectedConfig: VpnConfig?,
    connectivityState: com.example.vpn.ConnectivityState,
    connectedSeconds: Long,
    rxBytesPerSec: Long,
    txBytesPerSec: Long,
    totalRxBytes: Long,
    totalTxBytes: Long,
    onOpenSpeedTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = status == ConnectionStatus.CONNECTED

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkNavyCard)
            .border(1.dp, DarkNavyCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        // Top row: Selected server summary
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = selectedConfig?.countryFlag ?: "🌐",
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 10.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = selectedConfig?.name ?: "Сервер не выбран",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Защищенный туннель • ${selectedConfig?.maskedEndpoint ?: "—"}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Защищено",
                        tint = CyberCyan,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            // Ping badge
            val latencyQuality = when {
                selectedConfig?.pingMs == null -> "Не проверен"
                (selectedConfig.pingMs ?: 0L) < 0 -> "Таймаут"
                (selectedConfig.pingMs ?: 0L) < 100L -> "Быстрый ⚡"
                (selectedConfig.pingMs ?: 0L) < 200L -> "Хороший"
                else -> "Нормальный"
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkNavySurface)
                    .border(
                        1.dp,
                        Color(selectedConfig?.pingRatingColor ?: 0xFF64748B),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (selectedConfig?.pingMs != null && selectedConfig.pingMs > 0) "${selectedConfig.pingDisplay} • $latencyQuality" else "Проверить отклик",
                    color = Color(selectedConfig?.pingRatingColor ?: 0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (isConnected) {
            Spacer(modifier = Modifier.height(8.dp))
            // Internet Connectivity verification badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkNavySurface.copy(alpha = 0.8f))
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                when (connectivityState) {
                    is com.example.vpn.ConnectivityState.Checking -> {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(CyberCyan, androidx.compose.foundation.shape.CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Проверка соединения с интернетом...",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    is com.example.vpn.ConnectivityState.Available -> {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(NeonGreen, androidx.compose.foundation.shape.CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Интернет доступен: ${connectivityState.details} (${connectivityState.latencyMs} ms)",
                            color = NeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    is com.example.vpn.ConnectivityState.Unavailable -> {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFFEF4444), androidx.compose.foundation.shape.CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Нет доступа в сеть через сервер",
                            color = Color(0xFFEF4444),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    com.example.vpn.ConnectivityState.Idle -> {}
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Traffic Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Download Speed
            MetricItem(
                label = "СКОРОСТЬ ↓",
                value = if (isConnected) formatSpeed(rxBytesPerSec) else "0.0 KB/s",
                icon = Icons.Default.ArrowDownward,
                iconColor = CyberCyan,
                modifier = Modifier.weight(1f)
            )

            // Upload Speed
            MetricItem(
                label = "ОТДАЧА ↑",
                value = if (isConnected) formatSpeed(txBytesPerSec) else "0.0 KB/s",
                icon = Icons.Default.ArrowUpward,
                iconColor = NeonGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Duration & Speed Test Button Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Duration stopwatch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkNavySurface)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Время подключения",
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formatDuration(connectedSeconds),
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }

            // Speedometer button
            OutlinedButton(
                onClick = onOpenSpeedTest,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CyberCyan
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(CyberCyan.copy(alpha = 0.5f))
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("open_speed_test_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "Тест скорости",
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Тест скорости",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DarkNavySurface)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                color = TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

private fun formatSpeed(bytesPerSec: Long): String {
    return when {
        bytesPerSec >= 1_000_000 -> String.format(Locale.US, "%.1f MB/s", bytesPerSec / 1_000_000.0)
        bytesPerSec >= 1_000 -> String.format(Locale.US, "%.1f KB/s", bytesPerSec / 1_000.0)
        else -> "$bytesPerSec B/s"
    }
}

private fun formatDuration(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
}
