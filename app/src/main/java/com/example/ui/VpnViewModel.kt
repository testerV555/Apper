package com.example.ui

import android.app.Application
import android.content.Context
import android.net.VpnService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.RayVpnApp
import com.example.data.db.AppSettingsEntity
import com.example.data.network.SpeedTestPhase
import com.example.data.network.SpeedTestProgress
import com.example.data.network.SpeedTester
import com.example.model.ConnectionStatus
import com.example.model.VpnConfig
import com.example.vpn.ConnectivityState
import com.example.vpn.RayVpnService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    object RequestVpnPermission : UiEvent()
}

class VpnViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as RayVpnApp).repository

    val connectionStatus: StateFlow<ConnectionStatus> = RayVpnService.vpnState
    val connectivityState: StateFlow<ConnectivityState> = RayVpnService.connectivityState
    val connectedSeconds: StateFlow<Long> = RayVpnService.connectedSeconds
    val rxBytesPerSec: StateFlow<Long> = RayVpnService.rxBytesPerSec
    val txBytesPerSec: StateFlow<Long> = RayVpnService.txBytesPerSec
    val totalRxBytes: StateFlow<Long> = RayVpnService.totalRxBytes
    val totalTxBytes: StateFlow<Long> = RayVpnService.totalTxBytes

    val settings: StateFlow<AppSettingsEntity?> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedProtocolFilter = MutableStateFlow<String?>("ALL")
    val selectedProtocolFilter = _selectedProtocolFilter.asStateFlow()

    private val _sortMode = MutableStateFlow("DEFAULT") // "DEFAULT", "PING", "NAME"
    val sortMode = _sortMode.asStateFlow()

    private val _rawConfigs = repository.allConfigs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredConfigs: StateFlow<List<VpnConfig>> = combine(
        _rawConfigs,
        _searchQuery,
        _selectedProtocolFilter,
        _sortMode
    ) { configs, query, protocol, sort ->
        var list = configs
        if (protocol != null && protocol != "ALL") {
            list = list.filter { it.protocol.name.equals(protocol, ignoreCase = true) }
        }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.countryName.lowercase().contains(q) ||
                it.countryCode.lowercase().contains(q) ||
                it.protocol.displayName.lowercase().contains(q)
            }
        }

        // Apply sorting
        when (sort) {
            "PING" -> list.sortedWith(
                compareBy<VpnConfig> { it.pingMs == null || it.pingMs <= 0 }
                    .thenBy { it.pingMs ?: Long.MAX_VALUE }
                    .thenBy { it.name }
            )
            "NAME" -> list.sortedBy { it.name }
            else -> list
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedConfig = MutableStateFlow<VpnConfig?>(null)
    val selectedConfig = _selectedConfig.asStateFlow()

    private val _isRefreshingSubscription = MutableStateFlow(false)
    val isRefreshingSubscription = _isRefreshingSubscription.asStateFlow()

    private val _isPingingAll = MutableStateFlow(false)
    val isPingingAll = _isPingingAll.asStateFlow()

    private val _speedTestProgress = MutableStateFlow<SpeedTestProgress?>(null)
    val speedTestProgress = _speedTestProgress.asStateFlow()

    private val _isSpeedTestRunning = MutableStateFlow(false)
    val isSpeedTestRunning = _isSpeedTestRunning.asStateFlow()

    private val _showSubscriptionDialog = MutableStateFlow(false)
    val showSubscriptionDialog = _showSubscriptionDialog.asStateFlow()

    private val _showSpeedTestDialog = MutableStateFlow(false)
    val showSpeedTestDialog = _showSpeedTestDialog.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog = _showSettingsDialog.asStateFlow()

    private val _showSplitTunnelingDialog = MutableStateFlow(false)
    val showSplitTunnelingDialog = _showSplitTunnelingDialog.asStateFlow()

    private val _showTutorialDialog = MutableStateFlow(false)
    val showTutorialDialog = _showTutorialDialog.asStateFlow()

    private val _showPrivacyTermsDialog = MutableStateFlow(false)
    val showPrivacyTermsDialog = _showPrivacyTermsDialog.asStateFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    private var speedTestJob: Job? = null
    private var autoUpdateJob: Job? = null

    init {
        // Auto select first config if none is selected
        viewModelScope.launch {
            _rawConfigs.collect { configs ->
                if (_selectedConfig.value == null && configs.isNotEmpty()) {
                    _selectedConfig.value = configs.first()
                }
            }
        }

        // Start background auto-update periodic runner
        startAutoUpdateScheduler()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setProtocolFilter(filter: String?) {
        _selectedProtocolFilter.value = filter
    }

    fun setSortMode(mode: String) {
        _sortMode.value = mode
        viewModelScope.launch {
            repository.saveSortMode(mode)
        }
    }

    fun selectBestPingConfig() {
        val currentConfigs = _rawConfigs.value
        if (currentConfigs.isEmpty()) return

        // Find config with lowest ping > 0
        val bestPingConfig = currentConfigs
            .filter { it.pingMs != null && it.pingMs > 0 }
            .minByOrNull { it.pingMs ?: Long.MAX_VALUE }

        if (bestPingConfig != null) {
            selectConfig(bestPingConfig)
            viewModelScope.launch {
                _uiEvents.emit(UiEvent.ShowToast("Выбран самый быстрый узел: ${bestPingConfig.name} (${bestPingConfig.pingMs} ms)"))
            }
        } else {
            // Ping all first and select best
            viewModelScope.launch {
                _uiEvents.emit(UiEvent.ShowToast("Измеряем пинг для поиска лучшего узла..."))
                _isPingingAll.value = true
                repository.pingAllConfigsList(currentConfigs)
                _isPingingAll.value = false

                val freshConfigs = repository.getInitialSettings()
                val reloaded = _rawConfigs.value
                val best = reloaded.filter { it.pingMs != null && it.pingMs > 0 }
                    .minByOrNull { it.pingMs ?: Long.MAX_VALUE } ?: reloaded.firstOrNull()

                if (best != null) {
                    selectConfig(best)
                    _uiEvents.emit(UiEvent.ShowToast("Выбран лучший сервер: ${best.name} (${best.pingMs ?: 0} ms)"))
                }
            }
        }
    }

    private fun getExcludedAppsList(): List<String> {
        val raw = settings.value?.excludedApps ?: ""
        return raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun selectConfig(config: VpnConfig) {
        _selectedConfig.value = config
        viewModelScope.launch {
            repository.updateSelectedConfigId(config.id)
            if (connectionStatus.value == ConnectionStatus.CONNECTED) {
                // Reconnect to new node seamlessly on the fly
                val context = getApplication<Application>().applicationContext
                RayVpnService.stop(context)
                delay(300)
                RayVpnService.start(context, config, getExcludedAppsList())
            }
        }
    }

    fun toggleConnection(context: Context) {
        val currentStatus = connectionStatus.value
        val config = _selectedConfig.value

        if (currentStatus == ConnectionStatus.CONNECTED || currentStatus == ConnectionStatus.CONNECTING) {
            RayVpnService.stop(context)
            viewModelScope.launch {
                _uiEvents.emit(UiEvent.ShowToast("VPN отключен"))
            }
        } else {
            if (config == null) {
                viewModelScope.launch {
                    _uiEvents.emit(UiEvent.ShowToast("Выберите сервер для подключения"))
                }
                return
            }

            // Check VPN permission
            val vpnIntent = VpnService.prepare(context)
            if (vpnIntent != null) {
                viewModelScope.launch {
                    _uiEvents.emit(UiEvent.RequestVpnPermission)
                }
            } else {
                RayVpnService.start(context, config, getExcludedAppsList())
                viewModelScope.launch {
                    _uiEvents.emit(UiEvent.ShowToast("Подключение к ${config.name}..."))
                }
            }
        }
    }

    fun startVpnWithPreparedPermission(context: Context) {
        val config = _selectedConfig.value ?: return
        RayVpnService.start(context, config, getExcludedAppsList())
    }

    fun refreshSubscription(urlOverride: String? = null) {
        viewModelScope.launch {
            _isRefreshingSubscription.value = true
            val currentSettings = repository.getInitialSettings()
            val targetUrl = urlOverride ?: currentSettings.subscriptionUrl

            if (targetUrl.isBlank()) {
                _isRefreshingSubscription.value = false
                _showSubscriptionDialog.value = true
                _uiEvents.emit(UiEvent.ShowToast("Укажите ссылку на подписку ключей"))
                return@launch
            }

            val result = repository.fetchAndSaveSubscription(targetUrl)
            _isRefreshingSubscription.value = false

            result.onSuccess { count ->
                _uiEvents.emit(UiEvent.ShowToast("Успешно обновлено $count серверов Vaynet ⚡"))
            }.onFailure { error ->
                _uiEvents.emit(UiEvent.ShowToast(error.message ?: "Ошибка синхронизации"))
            }
        }
    }

    fun pingConfig(config: VpnConfig) {
        viewModelScope.launch {
            val ping = repository.pingSingleConfig(config)
            val msg = if (ping > 0) "${config.name}: $ping ms" else "${config.name}: Таймаут"
            _uiEvents.emit(UiEvent.ShowToast(msg))
        }
    }

    fun pingAllConfigs() {
        viewModelScope.launch {
            val currentList = _rawConfigs.value
            if (currentList.isEmpty()) return@launch

            _isPingingAll.value = true
            repository.pingAllConfigsList(currentList)
            _isPingingAll.value = false
            _uiEvents.emit(UiEvent.ShowToast("Проверка пинга (gstatic) завершена"))
        }
    }

    fun runSpeedTest() {
        speedTestJob?.cancel()
        _isSpeedTestRunning.value = true
        _showSpeedTestDialog.value = true
        speedTestJob = viewModelScope.launch {
            SpeedTester.runSpeedTest().collect { progress ->
                _speedTestProgress.value = progress
                if (progress.isFinished) {
                    _isSpeedTestRunning.value = false
                }
            }
        }
    }

    fun cancelSpeedTest() {
        speedTestJob?.cancel()
        _isSpeedTestRunning.value = false
        _speedTestProgress.value = null
    }

    fun setAutoUpdateInterval(minutes: Int, enabled: Boolean) {
        viewModelScope.launch {
            repository.saveAutoUpdateSettings(minutes, enabled)
            _uiEvents.emit(UiEvent.ShowToast("Настройки авто-обновления сохранены"))
        }
    }

    fun saveRouting(bypassLan: Boolean, killSwitch: Boolean, dns: String) {
        viewModelScope.launch {
            repository.saveRoutingSettings(bypassLan, killSwitch, dns)
            _uiEvents.emit(UiEvent.ShowToast("Параметры маршрутизации сохранены"))
        }
    }

    fun openSubscriptionDialog(show: Boolean) {
        _showSubscriptionDialog.value = show
    }

    fun openSpeedTestDialog(show: Boolean) {
        _showSpeedTestDialog.value = show
        if (show && _speedTestProgress.value == null) {
            runSpeedTest()
        }
    }

    fun openSettingsDialog(show: Boolean) {
        _showSettingsDialog.value = show
    }

    fun openSplitTunnelingDialog(show: Boolean) {
        _showSplitTunnelingDialog.value = show
    }

    fun openTutorialDialog(show: Boolean) {
        _showTutorialDialog.value = show
    }

    fun openPrivacyTermsDialog(show: Boolean) {
        _showPrivacyTermsDialog.value = show
    }

    fun acceptPrivacyAndTerms() {
        viewModelScope.launch {
            repository.saveTermsAccepted(true)
            _showPrivacyTermsDialog.value = false
            _uiEvents.emit(UiEvent.ShowToast("Соглашение принято"))
        }
    }

    fun completeTutorial() {
        viewModelScope.launch {
            repository.saveTutorialCompleted(true)
            _showTutorialDialog.value = false
        }
    }

    fun saveExcludedApps(packages: Set<String>) {
        viewModelScope.launch {
            repository.saveExcludedApps(packages)
            _uiEvents.emit(UiEvent.ShowToast("Список исключенных приложений обновлен (${packages.size})"))
            // If connected, reconnect to apply new routing
            if (connectionStatus.value == ConnectionStatus.CONNECTED) {
                val currentConfig = _selectedConfig.value ?: return@launch
                val context = getApplication<Application>().applicationContext
                RayVpnService.stop(context)
                delay(300)
                RayVpnService.start(context, currentConfig, packages.toList())
            }
        }
    }

    private fun startAutoUpdateScheduler() {
        autoUpdateJob?.cancel()
        autoUpdateJob = viewModelScope.launch {
            while (true) {
                val current = repository.getInitialSettings()
                if (current.isAutoUpdateEnabled && current.autoUpdateIntervalMinutes > 0 && current.subscriptionUrl.isNotBlank()) {
                    val last = current.lastUpdatedAt
                    val intervalMs = current.autoUpdateIntervalMinutes * 60 * 1000L
                    if (System.currentTimeMillis() - last > intervalMs) {
                        repository.fetchAndSaveSubscription(current.subscriptionUrl)
                    }
                }
                delay(60_000) // Check every minute
            }
        }
    }
}
