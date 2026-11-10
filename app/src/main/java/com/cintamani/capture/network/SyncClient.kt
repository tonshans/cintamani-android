package com.cintamani.capture.network

import com.cintamani.capture.data.MicroNote
import com.cintamani.capture.data.PingResult
import com.cintamani.capture.data.SyncResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class SyncClient {

    companion object {
        private const val CONNECT_TIMEOUT_MS = 8000
        private const val READ_TIMEOUT_MS = 15000
        private const val CLIENT_SOURCE = "cintamani-android"
    }

    suspend fun pingServer(serverUrl: String, apiToken: String): PingResult = withContext(Dispatchers.IO) {
        val sanitizedBase = serverUrl.trim().trimEnd('/')
        val pingUrl = "$sanitizedBase/api/v1/ping"
        val startTime = System.currentTimeMillis()

        var connection: HttpURLConnection? = null
        try {
            val url = URL(pingUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("Accept", "application/json")
                setRequestProperty("X-Client-Source", CLIENT_SOURCE)
                if (apiToken.isNotBlank()) {
                    setRequestProperty("Authorization", "Bearer $apiToken")
                }
            }

            val responseCode = connection.responseCode
            val latency = System.currentTimeMillis() - startTime

            if (responseCode == 200) {
                val responseBody = readStream(connection.inputStream)
                val json = JSONObject(responseBody)
                val serverTime = json.optString("server_time", "")
                PingResult.Alive(serverTime = serverTime, latencyMs = latency)
            } else if (responseCode == 401 || responseCode == 403) {
                PingResult.Unreachable("Akses ditolak (HTTP $responseCode): API Token tidak valid.")
            } else {
                val errorStream = connection.errorStream
                val errorBody = if (errorStream != null) readStream(errorStream) else "HTTP $responseCode"
                PingResult.Unreachable("Server merespons error ($responseCode): $errorBody")
            }
        } catch (e: Exception) {
            PingResult.Unreachable("Gagal terhubung ke server: ${e.localizedMessage ?: e.javaClass.simpleName}")
        } finally {
            connection?.disconnect()
        }
    }

    suspend fun syncNotes(
        serverUrl: String,
        apiToken: String,
        deviceId: String,
        notes: List<MicroNote>
    ): SyncResult = withContext(Dispatchers.IO) {
        if (notes.isEmpty()) {
            return@withContext SyncResult.Success(syncedCount = 0, serverTime = "")
        }

        val sanitizedBase = serverUrl.trim().trimEnd('/')
        val syncUrl = "$sanitizedBase/api/v1/sync"

        var connection: HttpURLConnection? = null
        try {
            val payload = JSONObject().apply {
                put("device_id", deviceId)
                val notesArray = JSONArray()
                for (note in notes) {
                    notesArray.put(note.toSyncJsonObject())
                }
                put("notes", notesArray)
            }

            val url = URL(syncUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("X-Client-Source", CLIENT_SOURCE)
                if (apiToken.isNotBlank()) {
                    setRequestProperty("Authorization", "Bearer $apiToken")
                }
            }

            // Tulis request payload
            OutputStreamWriter(connection.outputStream, StandardCharsets.UTF_8).use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val responseBody = readStream(connection.inputStream)
                val json = JSONObject(responseBody)
                val syncedIdsArray = json.optJSONArray("synced_ids")
                val count = syncedIdsArray?.length() ?: notes.size
                val serverTime = json.optString("server_time", "")
                SyncResult.Success(syncedCount = count, serverTime = serverTime)
            } else {
                val errorStream = connection.errorStream
                val errorText = if (errorStream != null) readStream(errorStream) else "Gagal sinkronisasi"
                SyncResult.Error(code = responseCode, message = "HTTP $responseCode: $errorText")
            }
        } catch (e: Exception) {
            SyncResult.Error(code = null, message = "Koneksi gagal: ${e.localizedMessage ?: e.javaClass.simpleName}")
        } finally {
            connection?.disconnect()
        }
    }

    private fun readStream(inputStream: java.io.InputStream): String {
        return BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
            val builder = java.lang.StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                builder.append(line).append('\n')
            }
            builder.toString().trim()
        }
    }
}
