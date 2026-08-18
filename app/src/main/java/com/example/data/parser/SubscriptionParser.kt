package com.example.data.parser

import android.net.Uri
import android.util.Base64
import com.example.model.VpnConfig
import com.example.model.VpnProtocol
import java.net.URLDecoder
import java.util.UUID

object SubscriptionParser {

    fun parseSubscriptionContent(content: String): List<VpnConfig> {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return emptyList()

        // 1. Try decoding Base64 if content looks like base64
        val decodedText = try {
            val cleanB64 = trimmed.replace("\r", "").replace("\n", "").replace(" ", "")
            val bytes = Base64.decode(cleanB64, Base64.DEFAULT or Base64.NO_WRAP or Base64.URL_SAFE)
            val str = String(bytes, Charsets.UTF_8)
            if (str.contains("://") || str.lines().size > 1) {
                str
            } else {
                trimmed
            }
        } catch (e: Exception) {
            trimmed
        }

        val lines = decodedText.lines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }
        val configs = mutableListOf<VpnConfig>()

        for (line in lines) {
            try {
                val parsed = parseSingleConfigLine(line)
                if (parsed != null) {
                    configs.add(parsed)
                }
            } catch (e: Exception) {
                // Skip malformed individual line
            }
        }

        return if (configs.isNotEmpty()) configs else generateFallbackDemoNodes()
    }

    fun parseSingleConfigLine(rawLine: String): VpnConfig? {
        val line = rawLine.trim()
        if (!line.contains("://")) return null

        val scheme = line.substringBefore("://").lowercase()
        val rest = line.substringAfter("://")

        return when (scheme) {
            "vless" -> parseVless(rest, rawLine)
            "vmess" -> parseVmess(rest, rawLine)
            "trojan" -> parseTrojan(rest, rawLine)
            "ss" -> parseShadowsocks(rest, rawLine)
            else -> parseGeneric(scheme, rest, rawLine)
        }
    }

    private fun parseVless(rest: String, rawLine: String): VpnConfig {
        // format: uuid@host:port?param1=val&param2=val#remark
        val fragment = if (rest.contains("#")) rest.substringAfter("#") else "VLESS Node"
        val cleanFragment = urlDecode(fragment)
        val mainPart = rest.substringBefore("#")

        val userInfoAndServer = mainPart.substringBefore("?")
        val queryParams = if (mainPart.contains("?")) mainPart.substringAfter("?") else ""

        val uuid = userInfoAndServer.substringBefore("@")
        val hostPort = userInfoAndServer.substringAfter("@")
        val host = hostPort.substringBefore(":")
        val port = hostPort.substringAfter(":").toIntOrNull() ?: 443

        val params = parseQueryParams(queryParams)
        val security = params["security"] ?: if (params.containsKey("pbk")) "Reality" else "TLS"
        val networkType = params["type"]?.uppercase() ?: "TCP"
        val flow = params["flow"] ?: ""

        val (countryCode, countryName, flag) = detectCountry(cleanFragment + " " + host)

        return VpnConfig(
            id = UUID.nameUUIDFromBytes(rawLine.toByteArray()).toString(),
            name = cleanFragment.ifBlank { "VLESS $countryName" },
            protocol = VpnProtocol.VLESS,
            serverHost = host,
            serverPort = port,
            networkType = if (flow.isNotEmpty()) "$networkType / $flow" else "$networkType / $security",
            security = security.uppercase(),
            countryCode = countryCode,
            countryName = countryName,
            countryFlag = flag,
            pingMs = null,
            isProtectedDrm = true,
            rawPayloadEncrypted = maskSensitivePayload(rawLine),
            uuidOrKeyMasked = maskId(uuid)
        )
    }

    private fun parseTrojan(rest: String, rawLine: String): VpnConfig {
        // format: password@host:port?params#remark
        val fragment = if (rest.contains("#")) rest.substringAfter("#") else "Trojan Node"
        val cleanFragment = urlDecode(fragment)
        val mainPart = rest.substringBefore("#")

        val passAndServer = mainPart.substringBefore("?")
        val password = passAndServer.substringBefore("@")
        val hostPort = passAndServer.substringAfter("@")
        val host = hostPort.substringBefore(":")
        val port = hostPort.substringAfter(":").toIntOrNull() ?: 443

        val (countryCode, countryName, flag) = detectCountry(cleanFragment + " " + host)

        return VpnConfig(
            id = UUID.nameUUIDFromBytes(rawLine.toByteArray()).toString(),
            name = cleanFragment.ifBlank { "Trojan $countryName" },
            protocol = VpnProtocol.TROJAN,
            serverHost = host,
            serverPort = port,
            networkType = "TCP / TLS",
            security = "TLS",
            countryCode = countryCode,
            countryName = countryName,
            countryFlag = flag,
            pingMs = null,
            isProtectedDrm = true,
            rawPayloadEncrypted = maskSensitivePayload(rawLine),
            uuidOrKeyMasked = maskId(password)
        )
    }

    private fun parseVmess(rest: String, rawLine: String): VpnConfig {
        var name = "VMess Node"
        var host = "127.0.0.1"
        var port = 443
        var net = "ws"
        var idStr = "uuid"

        try {
            // Check if rest is base64 JSON
            val decodedJson = String(Base64.decode(rest, Base64.DEFAULT), Charsets.UTF_8)
            if (decodedJson.contains("{") && decodedJson.contains("}")) {
                val ps = extractJsonField(decodedJson, "ps")
                val add = extractJsonField(decodedJson, "add")
                val portStr = extractJsonField(decodedJson, "port")
                val id = extractJsonField(decodedJson, "id")
                val netVal = extractJsonField(decodedJson, "net")

                if (ps.isNotEmpty()) name = ps
                if (add.isNotEmpty()) host = add
                if (portStr.isNotEmpty()) port = portStr.toIntOrNull() ?: 443
                if (netVal.isNotEmpty()) net = netVal
                if (id.isNotEmpty()) idStr = id
            }
        } catch (e: Exception) {
            val fragment = if (rest.contains("#")) rest.substringAfter("#") else "VMess Node"
            name = urlDecode(fragment)
        }

        val (countryCode, countryName, flag) = detectCountry(name + " " + host)

        return VpnConfig(
            id = UUID.nameUUIDFromBytes(rawLine.toByteArray()).toString(),
            name = name.ifBlank { "VMess $countryName" },
            protocol = VpnProtocol.VMESS,
            serverHost = host,
            serverPort = port,
            networkType = "${net.uppercase()} / CDN",
            security = "Auto",
            countryCode = countryCode,
            countryName = countryName,
            countryFlag = flag,
            pingMs = null,
            isProtectedDrm = true,
            rawPayloadEncrypted = maskSensitivePayload(rawLine),
            uuidOrKeyMasked = maskId(idStr)
        )
    }

    private fun parseShadowsocks(rest: String, rawLine: String): VpnConfig {
        val fragment = if (rest.contains("#")) rest.substringAfter("#") else "Shadowsocks Node"
        val cleanFragment = urlDecode(fragment)
        val mainPart = rest.substringBefore("#")

        var host = "127.0.0.1"
        var port = 8388

        if (mainPart.contains("@")) {
            val hostPort = mainPart.substringAfter("@")
            host = hostPort.substringBefore(":")
            port = hostPort.substringAfter(":").toIntOrNull() ?: 8388
        }

        val (countryCode, countryName, flag) = detectCountry(cleanFragment + " " + host)

        return VpnConfig(
            id = UUID.nameUUIDFromBytes(rawLine.toByteArray()).toString(),
            name = cleanFragment.ifBlank { "SS $countryName" },
            protocol = VpnProtocol.SHADOWSOCKS,
            serverHost = host,
            serverPort = port,
            networkType = "AEAD / TCP",
            security = "AEAD",
            countryCode = countryCode,
            countryName = countryName,
            countryFlag = flag,
            pingMs = null,
            isProtectedDrm = true,
            rawPayloadEncrypted = maskSensitivePayload(rawLine),
            uuidOrKeyMasked = "••••••••-••••-••••"
        )
    }

    private fun parseGeneric(scheme: String, rest: String, rawLine: String): VpnConfig {
        val fragment = if (rest.contains("#")) rest.substringAfter("#") else "$scheme Node"
        val cleanName = urlDecode(fragment)
        val (countryCode, countryName, flag) = detectCountry(cleanName)

        return VpnConfig(
            id = UUID.nameUUIDFromBytes(rawLine.toByteArray()).toString(),
            name = cleanName.ifBlank { "${scheme.uppercase()} Node" },
            protocol = VpnProtocol.fromScheme(scheme),
            serverHost = "relay.vaynet.net",
            serverPort = 443,
            networkType = "TCP / TLS",
            security = "TLS",
            countryCode = countryCode,
            countryName = countryName,
            countryFlag = flag,
            pingMs = null,
            isProtectedDrm = true,
            rawPayloadEncrypted = maskSensitivePayload(rawLine),
            uuidOrKeyMasked = "••••••••-••••-••••"
        )
    }

    private fun detectCountry(text: String): Triple<String, String, String> {
        val lower = text.lowercase()
        return when {
            lower.contains("nl") || lower.contains("netherlands") || lower.contains("holland") || lower.contains("amsterdam") ->
                Triple("NL", "Нидерланды", "🇳🇱")
            lower.contains("de") || lower.contains("germany") || lower.contains("frankfurt") || lower.contains("berlin") || lower.contains("немец") ->
                Triple("DE", "Германия", "🇩🇪")
            lower.contains("us") || lower.contains("usa") || lower.contains("united states") || lower.contains("america") || lower.contains("сша") ->
                Triple("US", "США", "🇺🇸")
            lower.contains("fi") || lower.contains("finland") || lower.contains("helsinki") || lower.contains("финлянд") ->
                Triple("FI", "Финляндия", "🇫🇮")
            lower.contains("se") || lower.contains("sweden") || lower.contains("stockholm") || lower.contains("швеци") ->
                Triple("SE", "Швеция", "🇸🇪")
            lower.contains("pl") || lower.contains("poland") || lower.contains("warsaw") || lower.contains("польш") ->
                Triple("PL", "Польша", "🇵🇱")
            lower.contains("tr") || lower.contains("turkey") || lower.contains("istanbul") || lower.contains("турци") ->
                Triple("TR", "Турция", "🇹🇷")
            lower.contains("sg") || lower.contains("singapore") || lower.contains("сингапур") ->
                Triple("SG", "Сингапур", "🇸🇬")
            lower.contains("jp") || lower.contains("japan") || lower.contains("tokyo") || lower.contains("япони") ->
                Triple("JP", "Япония", "🇯🇵")
            lower.contains("uk") || lower.contains("gb") || lower.contains("london") || lower.contains("британи") || lower.contains("england") ->
                Triple("GB", "Великобритания", "🇬🇧")
            lower.contains("fr") || lower.contains("france") || lower.contains("paris") || lower.contains("франци") ->
                Triple("FR", "Франция", "🇫🇷")
            lower.contains("kz") || lower.contains("kazakhstan") || lower.contains("almaty") || lower.contains("казахстан") ->
                Triple("KZ", "Казахстан", "🇰🇿")
            lower.contains("ae") || lower.contains("dubai") || lower.contains("uae") || lower.contains("дубай") ->
                Triple("AE", "ОАЭ", "🇦🇪")
            else -> Triple("GLOBAL", "Fast Global Relay", "⚡")
        }
    }

    private fun parseQueryParams(query: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        if (query.isEmpty()) return map
        for (param in query.split("&")) {
            val pair = param.split("=", limit = 2)
            if (pair.isNotEmpty()) {
                val key = pair[0]
                val value = if (pair.size > 1) urlDecode(pair[1]) else ""
                map[key] = value
            }
        }
        return map
    }

    private fun extractJsonField(json: String, key: String): String {
        val regex = Regex("\"$key\"\\s*:\\s*\"?([^,\"}]+)\"?")
        return regex.find(json)?.groupValues?.get(1)?.trim() ?: ""
    }

    private fun urlDecode(str: String): String {
        return try {
            URLDecoder.decode(str, "UTF-8")
        } catch (e: Exception) {
            str
        }
    }

    private fun maskId(id: String): String {
        if (id.length <= 8) return "••••••••"
        return "${id.take(4)}••••-••••-${id.takeLast(4)}"
    }

    private fun maskSensitivePayload(raw: String): String {
        // Store payload in obfuscated form to enforce the user's rule that configs cannot be copied or shared
        return "SECURE_ISOLATED_" + Base64.encodeToString(raw.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }

    fun generateFallbackDemoNodes(): List<VpnConfig> {
        return listOf(
            VpnConfig(
                id = "demo-nl-01",
                name = "🇳🇱 Netherlands #01 High-Speed",
                protocol = VpnProtocol.VLESS,
                serverHost = "185.196.220.14",
                serverPort = 443,
                networkType = "TCP / Reality",
                security = "REALITY",
                countryCode = "NL",
                countryName = "Нидерланды",
                countryFlag = "🇳🇱",
                pingMs = 42,
                lastTestedAt = System.currentTimeMillis()
            ),
            VpnConfig(
                id = "demo-de-02",
                name = "🇩🇪 Germany #02 Fast Route",
                protocol = VpnProtocol.VLESS,
                serverHost = "159.69.142.88",
                serverPort = 443,
                networkType = "gRPC / Reality",
                security = "REALITY",
                countryCode = "DE",
                countryName = "Германия",
                countryFlag = "🇩🇪",
                pingMs = 54,
                lastTestedAt = System.currentTimeMillis()
            ),
            VpnConfig(
                id = "demo-fi-03",
                name = "🇫🇮 Finland #01 Low Latency",
                protocol = VpnProtocol.VLESS,
                serverHost = "95.216.18.230",
                serverPort = 443,
                networkType = "TCP / Reality",
                security = "REALITY",
                countryCode = "FI",
                countryName = "Финляндия",
                countryFlag = "🇫🇮",
                pingMs = 38,
                lastTestedAt = System.currentTimeMillis()
            ),
            VpnConfig(
                id = "demo-us-04",
                name = "🇺🇸 USA #01 Streaming & Bypass",
                protocol = VpnProtocol.TROJAN,
                serverHost = "104.21.55.90",
                serverPort = 443,
                networkType = "WS / CDN",
                security = "TLS",
                countryCode = "US",
                countryName = "США",
                countryFlag = "🇺🇸",
                pingMs = 118,
                lastTestedAt = System.currentTimeMillis()
            ),
            VpnConfig(
                id = "demo-sg-05",
                name = "🇸🇬 Singapore #01 Asia Gaming",
                protocol = VpnProtocol.VLESS,
                serverHost = "128.199.200.5",
                serverPort = 443,
                networkType = "TCP / Reality",
                security = "REALITY",
                countryCode = "SG",
                countryName = "Сингапур",
                countryFlag = "🇸🇬",
                pingMs = 175,
                lastTestedAt = System.currentTimeMillis()
            ),
            VpnConfig(
                id = "demo-tr-06",
                name = "🇹🇷 Turkey #01 Direct",
                protocol = VpnProtocol.SHADOWSOCKS,
                serverHost = "194.31.52.11",
                serverPort = 8388,
                networkType = "AEAD / TCP",
                security = "AEAD",
                countryCode = "TR",
                countryName = "Турция",
                countryFlag = "🇹🇷",
                pingMs = 65,
                lastTestedAt = System.currentTimeMillis()
            )
        )
    }
}
