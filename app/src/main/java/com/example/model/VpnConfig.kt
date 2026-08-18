package com.example.model

enum class VpnProtocol(val displayName: String, val defaultPort: Int) {
    VLESS("VLESS", 443),
    VMESS("VMess", 443),
    TROJAN("Trojan", 443),
    SHADOWSOCKS("Shadowsocks", 8388),
    SOCKS5("SOCKS5", 1080),
    HYSTERIA2("Hysteria 2", 443);

    companion object {
        fun fromScheme(scheme: String): VpnProtocol {
            return when (scheme.lowercase()) {
                "vless" -> VLESS
                "vmess" -> VMESS
                "trojan" -> TROJAN
                "ss" -> SHADOWSOCKS
                "socks5", "socks" -> SOCKS5
                "hysteria2", "hy2" -> HYSTERIA2
                else -> VLESS
            }
        }
    }
}

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    DISCONNECTING,
    ERROR
}

data class VpnConfig(
    val id: String,
    val name: String,
    val protocol: VpnProtocol,
    val serverHost: String,
    val serverPort: Int,
    val networkType: String = "TCP / Reality",
    val security: String = "TLS",
    val countryCode: String = "GLOBAL",
    val countryName: String = "Global Node",
    val countryFlag: String = "🌐",
    val pingMs: Long? = null,
    val lastTestedAt: Long = 0L,
    val isProtectedDrm: Boolean = true, // Prevents export/copying
    val rawPayloadEncrypted: String = "", // Protected payload
    val uuidOrKeyMasked: String = "••••••••-••••-••••-••••-••••••••••••"
) {
    val isOnline: Boolean
        get() = pingMs != null && pingMs > 0

    val pingRatingColor: Long
        get() = when {
            pingMs == null -> 0xFF888888
            pingMs < 0 -> 0xFFEF4444 // Error / Timeout
            pingMs < 150 -> 0xFF10B981 // Green
            pingMs < 300 -> 0xFFF59E0B // Amber
            else -> 0xFFEF4444 // Red
        }

    val pingDisplay: String
        get() = when {
            pingMs == null -> "— ms"
            pingMs < 0 -> "Таймаут"
            else -> "$pingMs ms"
        }

    val maskedEndpoint: String
        get() {
            val parts = serverHost.split(".")
            return if (parts.size >= 4) {
                "${parts[0]}.***.***.${parts.last()}:$serverPort"
            } else if (serverHost.length > 8) {
                "${serverHost.take(4)}***.${serverHost.takeLast(4)}:$serverPort"
            } else {
                "***.node:$serverPort"
            }
        }
}
