package com.example.ui.dialogs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.network.SpeedTestPhase
import com.example.data.network.SpeedTestProgress
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkNavyBg
import com.example.ui.theme.DarkNavyCard
import com.example.ui.theme.DarkNavyCardBorder
import com.example.ui.theme.DarkNavySurface
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun SpeedTestDialog(
    progress: SpeedTestProgress?,
    isRunning: Boolean,
    onDismiss: () -> Unit,
    onRetest: () -> Unit
) {
    val downloadSpeed = progress?.currentDownloadMbps ?: 0f
    val uploadSpeed = progress?.currentUploadMbps ?: 0f
    val ping = progress?.pingMs ?: 0L
    val jitter = progress?.jitterMs ?: 0L
    val phase = progress?.phase ?: SpeedTestPhase.IDLE

    val animatedProgress by animateFloatAsState(
        targetValue = progress?.progressPercent ?: 0f,
        label = "progress_anim"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = DarkNavySurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkNavyCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Измеритель скорости",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Phase label
                Text(
                    text = when (phase) {
                        SpeedTestPhase.IDLE -> "Готов к измерению"
                        SpeedTestPhase.MEASURING_PING -> "Проверка задержки (gstatic 204)..."
                        SpeedTestPhase.MEASURING_DOWNLOAD -> "Тест скорости загрузки (Download)..."
                        SpeedTestPhase.MEASURING_UPLOAD -> "Тест скорости отдачи (Upload)..."
                        SpeedTestPhase.COMPLETED -> "Тест завершен успешно"
                        SpeedTestPhase.FAILED -> "Ошибка тестирования сети"
                    },
                    color = when (phase) {
                        SpeedTestPhase.COMPLETED -> NeonGreen
                        SpeedTestPhase.MEASURING_DOWNLOAD, SpeedTestPhase.MEASURING_UPLOAD -> CyberCyan
                        else -> TextSecondary
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Speed Gauge Center Box
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(190.dp)
                ) {
                    Canvas(modifier = Modifier.size(175.dp)) {
                        // Background track arc (240 degrees)
                        drawArc(
                            color = DarkNavyCard,
                            startAngle = 150f,
                            sweepAngle = 240f,
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Active arc based on download speed (0 to 100 Mbps)
                        val speedFraction = (downloadSpeed / 100f).coerceIn(0f, 1f)
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(CyberCyan, NeonGreen, NeonAmber)
                            ),
                            startAngle = 150f,
                            sweepAngle = 240f * speedFraction,
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Numeric display inside gauge
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%.1f", if (phase == SpeedTestPhase.MEASURING_UPLOAD) uploadSpeed else downloadSpeed),
                            color = TextPrimary,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Mbps",
                            color = CyberCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (phase == SpeedTestPhase.MEASURING_UPLOAD) "UPLOAD" else "DOWNLOAD",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = CyberCyan,
                    trackColor = DarkNavyCard
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Metrics Grid (Ping, Jitter, Download, Upload)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MetricBox(
                        label = "PING",
                        value = if (ping > 0) "$ping ms" else "—",
                        icon = Icons.Default.Bolt,
                        color = NeonAmber,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        label = "JITTER",
                        value = if (jitter > 0) "$jitter ms" else "—",
                        icon = Icons.Default.Bolt,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MetricBox(
                        label = "DOWNLOAD",
                        value = String.format(Locale.US, "%.1f Mbps", downloadSpeed),
                        icon = Icons.Default.ArrowDownward,
                        color = CyberCyan,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        label = "UPLOAD",
                        value = String.format(Locale.US, "%.1f Mbps", uploadSpeed),
                        icon = Icons.Default.ArrowUpward,
                        color = NeonGreen,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Retest Button
                Button(
                    onClick = onRetest,
                    enabled = !isRunning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = DarkNavyBg
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("retest_speed_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRunning) "Тестирование..." else "Повторить тест",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DarkNavyCard)
            .border(1.dp, DarkNavyCardBorder, RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
