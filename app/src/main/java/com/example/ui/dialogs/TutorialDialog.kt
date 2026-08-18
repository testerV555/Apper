package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

private data class TutorialStep(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconColor: Color,
    val keyPoints: List<Pair<String, String>>
)

@Composable
fun TutorialDialog(
    onDismiss: () -> Unit
) {
    val steps = listOf(
        TutorialStep(
            title = "Главная кнопка подключения",
            subtitle = "Включение и отключение защищенного туннеля в одно касание",
            icon = Icons.Default.Bolt,
            iconColor = AcidLime,
            keyPoints = listOf(
                "⚡ Одно нажатие" to "Нажмите круглую кнопку в центре экрана, чтобы зашифровать ваш трафик.",
                "🟢 Защита включена" to "Когда кнопка светится зеленым, ваш реальный IP скрыт, а сайты не могут перехватить трафик.",
                "🛡️ Фоновый режим" to "Приложение работает в фоне, значок ключа отображается в верхней панели Android."
            )
        ),
        TutorialStep(
            title = "Выбор серверов и кнопка «Быстрый»",
            subtitle = "Где и как находить самое быстрое соединение",
            icon = Icons.Default.Speed,
            iconColor = CyberCyan,
            keyPoints = listOf(
                "⚡ Кнопка «БЫСТРЫЙ»" to "Автоматически находит и выбирает узел с минимальной задержкой (пингом).",
                "📊 Оценка качества" to "Подсказки «Отлично», «Хорошо» показывают, какой сервер лучше для видео и звонков.",
                "🔄 «СИНХРОНИЗАЦИЯ»" to "Скачивает свежие рабочие серверы из защищенного облака."
            )
        ),
        TutorialStep(
            title = "Исключения приложений",
            subtitle = "Как пользоваться банками и Госуслугами без отключения VPN",
            icon = Icons.Default.AltRoute,
            iconColor = NeonGreen,
            keyPoints = listOf(
                "🏛️ Пресет «Банки и РФ»" to "В 1 клик исключает Сбер, Т-Банк, Госуслуги, Ozon и WB, чтобы они не выдавали ошибок.",
                "🌐 Прямой интернет" to "Выбранные приложения будут работать напрямую через вашего оператора, а остальной телефон — через VPN.",
                "⚙️ Доступно в Настройках" to "Кнопка настроек в правом верхнем углу позволяет настроить список в любой момент."
            )
        ),
        TutorialStep(
            title = "Защита и Безопасность",
            subtitle = "Что делает Vaynet для защиты вашей приватности",
            icon = Icons.Default.Security,
            iconColor = CyberCyan,
            keyPoints = listOf(
                "🔒 Шифрование трафика" to "Защищает от перехвата паролей и истории в публичных Wi-Fi сетях.",
                "🛡️ Kill Switch" to "Мгновенно блокирует интернет, если сервер оборвался, предотвращая утечку данных.",
                "🌐 Защищенный DNS" to "Шифрует все запросы к сайтам через Cloudflare и Google DNS (DoH/DoT)."
            )
        )
    )

    val pagerState = rememberPagerState(pageCount = { steps.size })
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DarkNavySurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkNavyCardBorder),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.82f)
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
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Обучение: Как устроен Vaynet",
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

                Spacer(modifier = Modifier.height(8.dp))

                // Pager Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(steps.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (isSelected) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) CyberCyan else DarkNavyCardBorder)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Horizontal Pager Content
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { pageIndex ->
                    val step = steps[pageIndex]
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Icon Hero Badge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFF0C192E), Color(0xFF070B14))))
                                .border(1.5.dp, step.iconColor.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                        ) {
                            Icon(
                                imageVector = step.icon,
                                contentDescription = null,
                                tint = step.iconColor,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Step Title & Subtitle
                        Text(
                            text = step.title,
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = step.subtitle,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Key points cards
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            step.keyPoints.forEach { (pointTitle, pointDesc) ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(DarkNavyCard)
                                        .border(1.dp, DarkNavyCardBorder, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = pointTitle,
                                            color = CyberCyan,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = pointDesc,
                                            color = TextPrimary.copy(alpha = 0.9f),
                                            fontSize = 12.sp,
                                            lineHeight = 17.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Navigation (Prev, Next / Finish)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pagerState.currentPage > 0) {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }
                        ) {
                            Icon(Icons.Default.NavigateBefore, contentDescription = null, tint = TextSecondary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Назад", color = TextSecondary)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(40.dp))
                    }

                    if (pagerState.currentPage < steps.size - 1) {
                        Button(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyan,
                                contentColor = DarkNavyBg
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Далее", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.NavigateNext, contentDescription = null)
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AcidLime,
                                contentColor = DarkNavyBg
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Все понятно!", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}
