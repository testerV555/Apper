package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.VpnConfig
import com.example.model.VpnProtocol

@Entity(tableName = "vpn_configs")
data class VpnConfigEntity(
    @PrimaryKey val id: String,
    val name: String,
    val protocol: String,
    val serverHost: String,
    val serverPort: Int,
    val networkType: String,
    val security: String,
    val countryCode: String,
    val countryName: String,
    val countryFlag: String,
    val pingMs: Long?,
    val lastTestedAt: Long,
    val isProtectedDrm: Boolean = true,
    val rawPayloadEncrypted: String = "",
    val uuidOrKeyMasked: String = "••••••••-••••-••••-••••-••••••••••••"
) {
    fun toDomain(): VpnConfig {
        return VpnConfig(
            id = id,
            name = name,
            protocol = try { VpnProtocol.valueOf(protocol) } catch (e: Exception) { VpnProtocol.VLESS },
            serverHost = serverHost,
            serverPort = serverPort,
            networkType = networkType,
            security = security,
            countryCode = countryCode,
            countryName = countryName,
            countryFlag = countryFlag,
            pingMs = pingMs,
            lastTestedAt = lastTestedAt,
            isProtectedDrm = isProtectedDrm,
            rawPayloadEncrypted = rawPayloadEncrypted,
            uuidOrKeyMasked = uuidOrKeyMasked
        )
    }

    companion object {
        fun fromDomain(config: VpnConfig): VpnConfigEntity {
            return VpnConfigEntity(
                id = config.id,
                name = config.name,
                protocol = config.protocol.name,
                serverHost = config.serverHost,
                serverPort = config.serverPort,
                networkType = config.networkType,
                security = config.security,
                countryCode = config.countryCode,
                countryName = config.countryName,
                countryFlag = config.countryFlag,
                pingMs = config.pingMs,
                lastTestedAt = config.lastTestedAt,
                isProtectedDrm = config.isProtectedDrm,
                rawPayloadEncrypted = config.rawPayloadEncrypted,
                uuidOrKeyMasked = config.uuidOrKeyMasked
            )
        }
    }
}

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val subscriptionUrl: String = "",
    val autoUpdateIntervalMinutes: Int = 60, // 0 = manual, 60, 360, 720, 1440
    val lastUpdatedAt: Long = 0L,
    val isAutoUpdateEnabled: Boolean = true,
    val selectedConfigId: String? = null,
    val bypassLan: Boolean = true,
    val killSwitchEnabled: Boolean = false,
    val dnsServer: String = "1.1.1.1, 8.8.8.8",
    val excludedApps: String = "", // Comma-separated package names for split tunneling
    val sortMode: String = "DEFAULT", // "DEFAULT", "PING", "NAME"
    val isTermsAccepted: Boolean = false,
    val isTutorialCompleted: Boolean = false
)
