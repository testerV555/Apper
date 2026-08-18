package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AcidLime
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

@Composable
fun PrivacyTermsDialog(
    isMandatoryConsent: Boolean = false,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    var termsAgreed by remember { mutableStateOf(false) }
    var disclaimerAcknowledged by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { if (!isMandatoryConsent) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isMandatoryConsent,
            dismissOnClickOutside = !isMandatoryConsent,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DarkNavySurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkNavyCardBorder),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Соглашение и Политика",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!isMandatoryConsent) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Закрыть",
                                tint = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Legal Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkNavyBg)
                        .border(1.dp, DarkNavyCardBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Critical Disclaimer Callout
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF261900))
                            .border(1.dp, NeonAmber.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = NeonAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "ВАЖНО: ИНСТРУМЕНТ ИСКЛЮЧИТЕЛЬНО ДЛЯ БЕЗОПАСНОСТИ",
                                    color = NeonAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Vaynet является исключительно техническим клиентским инструментом (ПО) для шифрования интернет-соединения в общедоступных сетях и защиты персональных данных. Разработчик/владелец не предоставляет и не контролирует передаваемый контент.",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LegalSection(
                        title = "1. Назначение ПО и отказ от ответственности",
                        text = "1.1. Приложение Vaynet («ПО») предоставляется на условиях «КАК ЕСТЬ» («AS IS») без каких-либо явных или подразумеваемых гарантий.\n" +
                                "1.2. Разработчик и владелец ПО не несут ответственности за любые прямые или косвенные убытки, сбои, блокировки или действия третьих лиц, возникшие в результате использования ПО.\n" +
                                "1.3. Пользователь несёт полную персональную ответственность за соблюдение законодательства своей юрисдикции при использовании интернета."
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LegalSection(
                        title = "2. Запрещенное использование",
                        text = "Категорически запрещается использовать данное программное обеспечение для совершения любых противоправных действий, включая, но не ограничиваясь: распространение вредоносного ПО, проведение сетевых атак (DDoS), взлом, мошенничество, спам, распространение запрещенных законом материалов. Вся ответственность за любые действия лежит исключительно на конечном пользователе."
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LegalSection(
                        title = "3. Политика конфиденциальности и Zero-Logs",
                        text = "3.1. Приложение Vaynet НЕ собирает, НЕ регистрирует и НЕ хранит персональные данные, логи посещенных сайтов, историю просмотров, DNS-запросы и IP-адреса пользователей.\n" +
                                "3.2. Локальные настройки (исключения приложений, настройки DNS) сохраняются исключительно в защищенном хранилище на вашем устройстве.\n" +
                                "3.3. Приложение не осуществляет передачу пользовательских данных третьим лицам или рекламным сетям."
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LegalSection(
                        title = "4. Ограничение гарантий сетевых шлюзов",
                        text = "Владелец ПО не гарантирует бесперебойную доступность сторонних сетевых узлов, их скорость или постоянную пропускную способность, так как они зависят от маршрутизации магистральных провайдеров."
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Checkboxes for explicit consent
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { termsAgreed = !termsAgreed }
                    ) {
                        Checkbox(
                            checked = termsAgreed,
                            onCheckedChange = { termsAgreed = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = CyberCyan,
                                uncheckedColor = TextSecondary,
                                checkmarkColor = DarkNavyBg
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Я принимаю Пользовательское соглашение и Политику конфиденциальности Vaynet",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { disclaimerAcknowledged = !disclaimerAcknowledged }
                    ) {
                        Checkbox(
                            checked = disclaimerAcknowledged,
                            onCheckedChange = { disclaimerAcknowledged = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = AcidLime,
                                uncheckedColor = TextSecondary,
                                checkmarkColor = DarkNavyBg
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Подтверждаю, что использую Vaynet исключительно как инструмент защиты и несу личную ответственность за свои действия в сети",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isMandatoryConsent) {
                        TextButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
                        ) {
                            Text("Закрыть")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    val canProceed = termsAgreed && disclaimerAcknowledged

                    Button(
                        onClick = {
                            if (canProceed || !isMandatoryConsent) {
                                onAccept()
                            }
                        },
                        enabled = canProceed || !isMandatoryConsent,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canProceed) AcidLime else CyberCyan,
                            contentColor = DarkNavyBg,
                            disabledContainerColor = DarkNavyCard,
                            disabledContentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("accept_terms_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isMandatoryConsent) "Принять и продолжить" else "Согласен",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegalSection(
    title: String,
    text: String
) {
    Column {
        Text(
            text = title,
            color = CyberCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = text,
            color = TextSecondary,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}
