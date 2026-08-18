package com.example.data.network

import com.example.model.VpnConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit

object PingTester {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(3000, TimeUnit.MILLISECONDS)
        .readTimeout(3000, TimeUnit.MILLISECONDS)
        .followRedirects(false)
        .build()

    /**
     * Measures latency using TCP handshake to host:port or HTTP 204 gstatic
     */
    suspend fun testConfigPing(config: VpnConfig): Long = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            // First attempt: Socket connect to target host and port
            val socket = Socket()
            try {
                socket.connect(InetSocketAddress(config.serverHost, config.serverPort), 2500)
                val duration = System.currentTimeMillis() - startTime
                socket.close()
                return@withContext duration.coerceAtLeast(15)
            } catch (socketEx: Exception) {
                // If direct socket fails (e.g. DNS or port blocked), fallback to gstatic generate_204 ping
                val req = Request.Builder()
                    .url("http://www.gstatic.com/generate_204")
                    .header("User-Agent", "Vaynet-Core/1.8.8")
                    .build()

                val callStart = System.currentTimeMillis()
                httpClient.newCall(req).execute().use { response ->
                    if (response.isSuccessful || response.code == 204) {
                        val httpDuration = System.currentTimeMillis() - callStart
                        return@withContext httpDuration.coerceAtLeast(20)
                    }
                }
            }
            return@withContext -1L // Unreachable / Timeout
        } catch (e: Exception) {
            return@withContext -1L
        }
    }

    /**
     * Quick test of Google gstatic / Internet connectivity
     */
    suspend fun testGstaticPing(): Long = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val req = Request.Builder()
                .url("http://www.gstatic.com/generate_204")
                .header("Cache-Control", "no-cache")
                .build()

            httpClient.newCall(req).execute().use { res ->
                if (res.isSuccessful || res.code == 204) {
                    return@withContext (System.currentTimeMillis() - start).coerceAtLeast(10)
                }
            }
            return@withContext -1L
        } catch (e: Exception) {
            return@withContext -1L
        }
    }
}
