package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionStatus
import com.example.ui.theme.AcidLime
import com.example.ui.theme.AcidLimeGlow
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanGlow
import com.example.ui.theme.DarkNavyBg
import com.example.ui.theme.DarkNavyCard
import com.example.ui.theme.DarkNavySurface
import com.example.ui.theme.ElectricYellow
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonViolet

@Composable
fun ConnectButton(
    status: ConnectionStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = status == ConnectionStatus.CONNECTED
    val isConnecting = status == ConnectionStatus.CONNECTING || status == ConnectionStatus.RECONNECTING

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnected || isConnecting) 1.35f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (isConnected || isConnecting) 0.65f else 0.2f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isConnected) 4000 else 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_angle"
    )

    val glowColor = when (status) {
        ConnectionStatus.CONNECTED -> AcidLime
        ConnectionStatus.CONNECTING, ConnectionStatus.RECONNECTING -> ElectricYellow
        ConnectionStatus.ERROR -> NeonRed
        ConnectionStatus.DISCONNECTED, ConnectionStatus.DISCONNECTING -> CyberCyan
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(185.dp)
        ) {
            // Animated Acid / Neon Shockwave Rings
            if (isConnected || isConnecting) {
                Canvas(modifier = Modifier.size(185.dp)) {
                    val radius = (size.minDimension / 2f) * pulseScale * 0.75f
                    drawCircle(
                        color = glowColor.copy(alpha = pulseAlpha),
                        radius = radius,
                        style = Stroke(width = 4.dp.toPx())
                    )
                    // Secondary shockwave
                    drawCircle(
                        color = CyberCyan.copy(alpha = pulseAlpha * 0.5f),
                        radius = radius * 0.85f,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            // Outer Neon Energy Ring with Arc Segments
            Canvas(
                modifier = Modifier
                    .size(160.dp)
                    .rotate(if (isConnected || isConnecting) rotationAngle else 0f)
            ) {
                drawCircle(
                    color = glowColor.copy(alpha = 0.2f),
                    style = Stroke(width = 3.dp.toPx())
                )

                if (isConnected || isConnecting) {
                    // Electric Energy Arcs
                    drawArc(
                        color = glowColor,
                        startAngle = 0f,
                        sweepAngle = 70f,
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = CyberCyan,
                        startAngle = 120f,
                        sweepAngle = 50f,
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = ElectricYellow,
                        startAngle = 220f,
                        sweepAngle = 60f,
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Inner Central Power Button with Lightning Core
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = if (isConnected) {
                                listOf(AcidLimeGlow, Color(0xFF003D1A), DarkNavyBg)
                            } else if (isConnecting) {
                                listOf(Color(0x66FFCC00), Color(0xFF332200), DarkNavyBg)
                            } else {
                                listOf(Color(0xFF0C192E), DarkNavyCard, DarkNavyBg)
                            }
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = glowColor),
                        onClick = onClick
                    )
                    .testTag("connect_button")
            ) {
                if (isConnected) {
                    // High-voltage lightning bolt icon when active
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Туннель активен",
                        tint = AcidLime,
                        modifier = Modifier.size(64.dp)
                    )
                } else {
                    Icon(
                        imageVector = if (isConnecting) Icons.Default.Bolt else Icons.Default.PowerSettingsNew,
                        contentDescription = "VPN Connect Action",
                        tint = if (isConnecting) ElectricYellow else CyberCyan,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Status Label with Lightning Bolt Badge
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isConnected) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = AcidLime,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = when (status) {
                    ConnectionStatus.CONNECTED -> "ЗАЩИТА ВКЛЮЧЕНА ⚡"
                    ConnectionStatus.CONNECTING -> "ПОДКЛЮЧЕНИЕ К СЕТИ..."
                    ConnectionStatus.RECONNECTING -> "ПЕРЕПОДКЛЮЧЕНИЕ..."
                    ConnectionStatus.DISCONNECTING -> "ОТКЛЮЧЕНИЕ..."
                    ConnectionStatus.ERROR -> "ОШИБКА СЕТИ"
                    ConnectionStatus.DISCONNECTED -> "НАЖМИТЕ ДЛЯ ПОДКЛЮЧЕНИЯ"
                },
                color = glowColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.1.sp
            )
        }
    }
}
