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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VpnConfig
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkNavyCard
import com.example.ui.theme.DarkNavyCardBorder
import com.example.ui.theme.DarkNavySurface
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ConfigItemCard(
    config: VpnConfig,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onPing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) CyberCyan else DarkNavyCardBorder
    val backgroundColor = if (isSelected) DarkNavyCard.copy(alpha = 0.9f) else DarkNavySurface

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onSelect)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("config_item_${config.id}")
    ) {
        // Flag icon
        Text(
            text = config.countryFlag,
            fontSize = 24.sp,
            modifier = Modifier.padding(end = 12.dp)
        )

        // Details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = config.name,
                    color = if (isSelected) CyberCyan else TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 3.dp)
            ) {
                Text(
                    text = config.maskedEndpoint,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Защищено",
                    tint = TextMuted,
                    modifier = Modifier.size(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Ping Button & Indicator
        val pingQualityText = when {
            config.pingMs == null -> "Проверить"
            config.pingMs < 0 -> "Таймаут"
            config.pingMs < 100L -> "Отлично"
            config.pingMs < 200L -> "Хорошо"
            config.pingMs < 400L -> "Нормально"
            else -> "Медленно"
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(DarkNavyCard)
                .clickable(onClick = onPing)
                .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Проверить скорость отклика",
                    tint = Color(config.pingRatingColor),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = if (config.pingMs != null && config.pingMs > 0) "${config.pingDisplay} ($pingQualityText)" else config.pingDisplay,
                    color = Color(config.pingRatingColor),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Selected Status Icon
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Выбранный сервер",
                tint = CyberCyan,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, TextMuted, CircleShape)
            )
        }
    }
}
