package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ConnectionStatus
import com.example.ui.UiEvent
import com.example.ui.VpnViewModel
import com.example.ui.components.ConfigItemCard
import com.example.ui.components.ConnectButton
import com.example.ui.components.LiveTrafficCard
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.dialogs.SpeedTestDialog
import com.example.ui.dialogs.SubscriptionDialog
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen(
    viewModel: VpnViewModel,
    onRequestVpnPermission: () -> Unit
) {
    val context = LocalContext.current

    val status by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val connectivityState by viewModel.connectivityState.collectAsStateWithLifecycle()
    val connectedSeconds by viewModel.connectedSeconds.collectAsStateWithLifecycle()
    val rxBytesPerSec by viewModel.rxBytesPerSec.collectAsStateWithLifecycle()
    val txBytesPerSec by viewModel.txBytesPerSec.collectAsStateWithLifecycle()
    val totalRxBytes by viewModel.totalRxBytes.collectAsStateWithLifecycle()
    val totalTxBytes by viewModel.totalTxBytes.collectAsStateWithLifecycle()

    val selectedConfig by viewModel.selectedConfig.collectAsStateWithLifecycle()
    val filteredConfigs by viewModel.filteredConfigs.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortMode by viewModel.sortMode.collectAsStateWithLifecycle()

    val isRefreshing by viewModel.isRefreshingSubscription.collectAsStateWithLifecycle()
    val isPingingAll by viewModel.isPingingAll.collectAsStateWithLifecycle()
    val speedTestProgress by viewModel.speedTestProgress.collectAsStateWithLifecycle()
    val isSpeedTestRunning by viewModel.isSpeedTestRunning.collectAsStateWithLifecycle()

    val showSubscriptionDialog by viewModel.showSubscriptionDialog.collectAsStateWithLifecycle()
    val showSpeedTestDialog by viewModel.showSpeedTestDialog.collectAsStateWithLifecycle()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsStateWithLifecycle()
    val showSplitTunnelingDialog by viewModel.showSplitTunnelingDialog.collectAsStateWithLifecycle()
    val showTutorialDialog by viewModel.showTutorialDialog.collectAsStateWithLifecycle()
    val showPrivacyTermsDialog by viewModel.showPrivacyTermsDialog.collectAsStateWithLifecycle()

    // First run check for Privacy Terms
    val isTermsAccepted = settings?.isTermsAccepted ?: true
    val isSettingsLoaded = settings != null
    val showFirstRunTerms = isSettingsLoaded && !isTermsAccepted

    // Handle UI events
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is UiEvent.RequestVpnPermission -> onRequestVpnPermission()
            }
        }
    }

    // Refresh button rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "refresh_rotation")
    val refreshRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Scaffold(
        containerColor = DarkNavyBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Top App Header Bar
                item {
                    TopHeaderBar(
                        settings = settings,
                        onOpenSubscription = { viewModel.openSubscriptionDialog(true) },
                        onOpenTutorial = { viewModel.openTutorialDialog(true) },
                        onOpenSettings = { viewModel.openSettingsDialog(true) }
                    )
                }

                // 2. Large Hero Connect Button
                item {
                    ConnectButton(
                        status = status,
                        onClick = { viewModel.toggleConnection(context) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 3. Live Traffic / Speed Metrics Card with Real Connectivity Check
                item {
                    LiveTrafficCard(
                        status = status,
                        selectedConfig = selectedConfig,
                        connectivityState = connectivityState,
                        connectedSeconds = connectedSeconds,
                        rxBytesPerSec = rxBytesPerSec,
                        txBytesPerSec = txBytesPerSec,
                        totalRxBytes = totalRxBytes,
                        totalTxBytes = totalTxBytes,
                        onOpenSpeedTest = { viewModel.openSpeedTestDialog(true) }
                    )
                }

                // 4. Prominent Action Buttons (Refresh Subscription, Ping All, Best Node)
                item {
                    ActionButtonsBar(
                        isRefreshing = isRefreshing,
                        isPingingAll = isPingingAll,
                        refreshRotation = if (isRefreshing) refreshRotation else 0f,
                        onRefreshClick = { viewModel.refreshSubscription() },
                        onPingAllClick = { viewModel.pingAllConfigs() },
                        onBestPingClick = { viewModel.selectBestPingConfig() }
                    )
                }

                // 4b. Last updated timestamp badge
                item {
                    SubscriptionStatusBanner(
                        lastUpdatedAt = settings?.lastUpdatedAt ?: 0L,
                        configCount = filteredConfigs.size,
                        isRefreshing = isRefreshing
                    )
                }

                // 5. Search & Sorting
                item {
                    SearchAndSortRow(
                        searchQuery = searchQuery,
                        sortMode = sortMode,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onSortChange = { viewModel.setSortMode(it) }
                    )
                }

                // 6. Section Header
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "СПИСОК СЕРВЕРОВ (${filteredConfigs.size})",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Защищено",
                                tint = CyberCyan,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        Text(
                            text = "Защищенное соединение",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                // 7. Configs List
                if (filteredConfigs.isEmpty()) {
                    item {
                        EmptyConfigsState(
                            onAddSubscription = { viewModel.openSubscriptionDialog(true) }
                        )
                    }
                } else {
                    items(
                        items = filteredConfigs,
                        key = { it.id }
                    ) { config ->
                        ConfigItemCard(
                            config = config,
                            isSelected = selectedConfig?.id == config.id,
                            onSelect = { viewModel.selectConfig(config) },
                            onPing = { viewModel.pingConfig(config) }
                        )
                    }
                }

                // Bottom spacer for generous scrolling clearance
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Dialogs
    if (showSubscriptionDialog) {
        SubscriptionDialog(
            currentSettings = settings,
            isRefreshing = isRefreshing,
            onDismiss = { viewModel.openSubscriptionDialog(false) },
            onSaveAndRefresh = { url, interval, enabled ->
                viewModel.setAutoUpdateInterval(interval, enabled)
                viewModel.refreshSubscription(url)
                viewModel.openSubscriptionDialog(false)
            }
        )
    }

    if (showSpeedTestDialog) {
        SpeedTestDialog(
            progress = speedTestProgress,
            isRunning = isSpeedTestRunning,
            onDismiss = {
                viewModel.cancelSpeedTest()
                viewModel.openSpeedTestDialog(false)
            },
            onRetest = { viewModel.runSpeedTest() }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            settings = settings,
            onDismiss = { viewModel.openSettingsDialog(false) },
            onOpenSplitTunneling = { viewModel.openSplitTunnelingDialog(true) },
            onOpenTutorial = { viewModel.openTutorialDialog(true) },
            onOpenPrivacyTerms = { viewModel.openPrivacyTermsDialog(true) },
            onSaveSettings = { bypass, killSwitch, dns ->
                viewModel.saveRouting(bypass, killSwitch, dns)
            }
        )
    }

    if (showSplitTunnelingDialog) {
        com.example.ui.dialogs.SplitTunnelingDialog(
            initialExcludedPackages = settings?.excludedApps ?: "",
            onDismiss = { viewModel.openSplitTunnelingDialog(false) },
            onSave = { excluded ->
                viewModel.saveExcludedApps(excluded)
            }
        )
    }

    if (showTutorialDialog) {
        com.example.ui.dialogs.TutorialDialog(
            onDismiss = { viewModel.completeTutorial() }
        )
    }

    // Explicit User Privacy & Terms Dialog (Manual open or Mandatory First-Run)
    if (showPrivacyTermsDialog || showFirstRunTerms) {
        com.example.ui.dialogs.PrivacyTermsDialog(
            isMandatoryConsent = showFirstRunTerms,
            onAccept = { viewModel.acceptPrivacyAndTerms() },
            onDismiss = { viewModel.openPrivacyTermsDialog(false) }
        )
    }
}

@Composable
private fun TopHeaderBar(
    settings: com.example.data.db.AppSettingsEntity?,
    onOpenSubscription: () -> Unit,
    onOpenTutorial: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        // App Title & Badge
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF003822), Color(0xFF003847), Color(0xFF0D1424))))
                    .border(1.5.dp, AcidLime.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = AcidLime,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Vaynet",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1B3800))
                            .border(1.dp, AcidLime.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "⚡ FAST CORE",
                            color = AcidLime,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Text(
                    text = "Защищенный шлюз активен ⚡",
                    color = AcidLime,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Action Icons
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Tutorial / Help Icon Button
            IconButton(
                onClick = onOpenTutorial,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(DarkNavySurface)
                    .testTag("open_tutorial_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "Обучение",
                    tint = CyberCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Subscription Key Config Icon Button
            IconButton(
                onClick = onOpenSubscription,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(DarkNavySurface)
                    .testTag("open_subscription_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = "Настройка подписки",
                    tint = TextSecondary,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Settings Button
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(DarkNavySurface)
                    .testTag("open_settings_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Настройки",
                    tint = TextSecondary,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
private fun ActionButtonsBar(
    isRefreshing: Boolean,
    isPingingAll: Boolean,
    refreshRotation: Float,
    onRefreshClick: () -> Unit,
    onPingAllClick: () -> Unit,
    onBestPingClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Prominent Update / Refresh Button
            Button(
                onClick = onRefreshClick,
                enabled = !isRefreshing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan,
                    contentColor = DarkNavyBg,
                    disabledContainerColor = DarkNavyCard
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1.1f)
                    .height(46.dp)
                    .testTag("refresh_subscription_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Autorenew,
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(refreshRotation)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isRefreshing) "Обновление..." else "СИНХРОНИЗАЦИЯ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            // Ping All Nodes Button
            ElevatedButton(
                onClick = onPingAllClick,
                enabled = !isPingingAll,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = DarkNavyCard,
                    contentColor = NeonGreen
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .border(1.dp, DarkNavyCardBorder, RoundedCornerShape(12.dp))
                    .testTag("ping_all_btn")
            ) {
                if (isPingingAll) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(15.dp),
                        color = NeonGreen,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (isPingingAll) "Проверка..." else "ПРОВЕРИТЬ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = NeonGreen
                )
            }

            // Best Ping Auto Select Button
            ElevatedButton(
                onClick = onBestPingClick,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = Color(0xFF162E00),
                    contentColor = AcidLime
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1.05f)
                    .height(46.dp)
                    .border(1.dp, AcidLime.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .testTag("best_ping_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = AcidLime,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "⚡ БЫСТРЫЙ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = AcidLime
                )
            }
        }
    }
}

@Composable
private fun SubscriptionStatusBanner(
    lastUpdatedAt: Long,
    configCount: Int,
    isRefreshing: Boolean
) {
    val timeLabel = formatLastUpdated(lastUpdatedAt)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkNavySurface)
            .border(1.dp, DarkNavyCardBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Autorenew,
                contentDescription = null,
                tint = if (isRefreshing) CyberCyan else TextSecondary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isRefreshing) "Синхронизация подписки..." else "Обновлено: $timeLabel",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(NeonGreen, CircleShape)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "$configCount узлов Vaynet 🔒",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatLastUpdated(timestamp: Long): String {
    if (timestamp <= 0L) return "Ещё не обновлялось"
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "Только что"
        diff < 3600_000 -> "${diff / 60_000} мин назад"
        diff < 86400_000 -> "${diff / 3600_000} ч назад"
        else -> SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}

@Composable
private fun SearchAndSortRow(
    searchQuery: String,
    sortMode: String,
    onSearchChange: (String) -> Unit,
    onSortChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Поиск страны или локации...", color = TextMuted, fontSize = 12.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Очистить",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = DarkNavyCardBorder,
                focusedContainerColor = DarkNavySurface,
                unfocusedContainerColor = DarkNavySurface
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("search_configs_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Sorting Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Сортировка:",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            val sortOptions = listOf(
                "DEFAULT" to "По умолчанию",
                "PING" to "⚡ По пингу",
                "NAME" to "🔤 По названию"
            )

            sortOptions.forEach { (mode, label) ->
                val isSelected = sortMode == mode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) Color(0xFF003847) else DarkNavySurface)
                        .border(
                            1.dp,
                            if (isSelected) CyberCyan else DarkNavyCardBorder,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { onSortChange(mode) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) CyberCyan else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyConfigsState(
    onAddSubscription: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkNavySurface)
            .border(1.dp, DarkNavyCardBorder, RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CloudDownload,
            contentDescription = null,
            tint = CyberCyan,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Синхронизация серверов Vaynet",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Нажмите для мгновенной загрузки и проверки серверов из защищенного облака",
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onAddSubscription,
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = DarkNavyBg),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Синхронизировать серверы", fontWeight = FontWeight.Bold)
        }
    }
}
