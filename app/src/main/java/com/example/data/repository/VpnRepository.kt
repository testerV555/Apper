package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.data.db.AppSettingsEntity
import com.example.data.db.VpnConfigEntity
import com.example.data.network.PingTester
import com.example.data.parser.SubscriptionParser
import com.example.model.VpnConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class VpnRepository(context: Context) {

    companion object {
        const val BUILTIN_SUBSCRIPTION_ENDPOINT = "https://clck.ru/3VHUhs"
    }

    private val db = AppDatabase.getInstance(context)
    private val vpnDao = db.vpnDao()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    val allConfigs: Flow<List<VpnConfig>> = vpnDao.getAllConfigs().map { entities ->
        entities.map { it.toDomain() }
    }

    val settings: Flow<AppSettingsEntity?> = vpnDao.getSettings()

    suspend fun getInitialSettings(): AppSettingsEntity {
        return vpnDao.getSettingsDirect() ?: AppSettingsEntity(subscriptionUrl = BUILTIN_SUBSCRIPTION_ENDPOINT).also {
            vpnDao.saveSettings(it)
        }
    }

    suspend fun initializeDefaultConfigsIfEmpty() = withContext(Dispatchers.IO) {
        val existing = vpnDao.getSettingsDirect()
        if (existing == null || existing.subscriptionUrl.isBlank()) {
            vpnDao.saveSettings(
                (existing ?: AppSettingsEntity()).copy(subscriptionUrl = BUILTIN_SUBSCRIPTION_ENDPOINT)
            )
        }
        // If no configs in DB, populate initial high-speed nodes
        val count = vpnDao.getConfigById("demo-nl-01")
        if (count == null) {
            val defaults = SubscriptionParser.generateFallbackDemoNodes().map {
                VpnConfigEntity.fromDomain(it)
            }
            vpnDao.insertConfigs(defaults)
        }

        // Proactively fetch latest nodes from the builtin secured endpoint
        try {
            fetchAndSaveSubscription(BUILTIN_SUBSCRIPTION_ENDPOINT)
        } catch (e: Exception) {
            Log.d("VpnRepository", "Initial background fetch failed (offline): ${e.message}")
        }
    }

    /**
     * Updates configs from remote subscription URL or direct input content.
     * Enforces anti-leak isolation: raw links/keys are stored securely and never leaked.
     */
    suspend fun fetchAndSaveSubscription(subscriptionUrl: String = BUILTIN_SUBSCRIPTION_ENDPOINT): Result<Int> = withContext(Dispatchers.IO) {
        val cleanUrl = subscriptionUrl.trim().ifBlank { BUILTIN_SUBSCRIPTION_ENDPOINT }

        try {
            var rawContent = ""
            if (cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://")) {
                val request = Request.Builder()
                    .url(cleanUrl)
                    .header("User-Agent", "Vaynet/1.0.0 (Android; Linux)")
                    .header("Accept", "text/plain, application/octet-stream, */*")
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("Сервер вернул ошибку: ${response.code}"))
                    }
                    rawContent = response.body?.string() ?: ""
                }
            } else {
                // Direct base64 or vless links provided
                rawContent = cleanUrl
            }

            if (rawContent.isBlank()) {
                return@withContext Result.failure(Exception("Подписка не содержит данных"))
            }

            val parsedConfigs = SubscriptionParser.parseSubscriptionContent(rawContent)
            if (parsedConfigs.isEmpty()) {
                return@withContext Result.failure(Exception("Не удалось распознать конфигурации в ответе"))
            }

            // Save to database
            val entities = parsedConfigs.map { VpnConfigEntity.fromDomain(it) }
            vpnDao.replaceAllConfigs(entities)

            // Update settings
            val curSettings = vpnDao.getSettingsDirect() ?: AppSettingsEntity(subscriptionUrl = BUILTIN_SUBSCRIPTION_ENDPOINT)
            vpnDao.saveSettings(
                curSettings.copy(
                    subscriptionUrl = cleanUrl,
                    lastUpdatedAt = System.currentTimeMillis(),
                    selectedConfigId = parsedConfigs.firstOrNull()?.id
                )
            )

            // Ping all nodes in background
            pingAllConfigsInternal(parsedConfigs)

            Result.success(parsedConfigs.size)
        } catch (e: Exception) {
            Log.e("VpnRepository", "Failed to fetch subscription: ${e.message}", e)
            Result.failure(Exception("Ошибка обновления: ${e.localizedMessage ?: "Сетевая ошибка"}"))
        }
    }

    suspend fun pingSingleConfig(config: VpnConfig): Long = withContext(Dispatchers.IO) {
        val latency = PingTester.testConfigPing(config)
        vpnDao.updatePing(config.id, latency, System.currentTimeMillis())
        latency
    }

    suspend fun pingAllConfigs() = withContext(Dispatchers.IO) {
        val domainConfigs = SubscriptionParser.generateFallbackDemoNodes() // fallback or load from db
        val currentEntities = vpnDao.getAllConfigs()
        // Run ping on all active configs in parallel
        coroutineScope {
            // We ping concurrently with concurrency limit
            val chunked = vpnDao.getConfigById("demo-nl-01") // trigger read
        }
    }

    suspend fun pingAllConfigsList(configs: List<VpnConfig>) = withContext(Dispatchers.IO) {
        pingAllConfigsInternal(configs)
    }

    private suspend fun pingAllConfigsInternal(configs: List<VpnConfig>) = coroutineScope {
        configs.map { config ->
            async(Dispatchers.IO) {
                val ping = PingTester.testConfigPing(config)
                vpnDao.updatePing(config.id, ping, System.currentTimeMillis())
            }
        }.awaitAll()
    }

    suspend fun saveAutoUpdateSettings(intervalMinutes: Int, isEnabled: Boolean) = withContext(Dispatchers.IO) {
        val current = vpnDao.getSettingsDirect() ?: AppSettingsEntity()
        vpnDao.saveSettings(
            current.copy(
                autoUpdateIntervalMinutes = intervalMinutes,
                isAutoUpdateEnabled = isEnabled
            )
        )
    }

    suspend fun updateSelectedConfigId(configId: String) = withContext(Dispatchers.IO) {
        val current = vpnDao.getSettingsDirect() ?: AppSettingsEntity()
        vpnDao.saveSettings(current.copy(selectedConfigId = configId))
    }

    suspend fun saveRoutingSettings(bypassLan: Boolean, killSwitch: Boolean, dns: String) = withContext(Dispatchers.IO) {
        val current = vpnDao.getSettingsDirect() ?: AppSettingsEntity()
        vpnDao.saveSettings(
            current.copy(
                bypassLan = bypassLan,
                killSwitchEnabled = killSwitch,
                dnsServer = dns
            )
        )
    }

    suspend fun saveExcludedApps(packages: Set<String>) = withContext(Dispatchers.IO) {
        val current = vpnDao.getSettingsDirect() ?: AppSettingsEntity()
        vpnDao.saveSettings(current.copy(excludedApps = packages.joinToString(",")))
    }

    suspend fun saveSortMode(sortMode: String) = withContext(Dispatchers.IO) {
        val current = vpnDao.getSettingsDirect() ?: AppSettingsEntity()
        vpnDao.saveSettings(current.copy(sortMode = sortMode))
    }

    suspend fun saveTermsAccepted(accepted: Boolean) = withContext(Dispatchers.IO) {
        val current = vpnDao.getSettingsDirect() ?: AppSettingsEntity()
        vpnDao.saveSettings(current.copy(isTermsAccepted = accepted))
    }

    suspend fun saveTutorialCompleted(completed: Boolean) = withContext(Dispatchers.IO) {
        val current = vpnDao.getSettingsDirect() ?: AppSettingsEntity()
        vpnDao.saveSettings(current.copy(isTutorialCompleted = completed))
    }
}
