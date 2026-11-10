package com.cintamani.capture.data

import org.json.JSONObject

enum class InputMode(val wireValue: String) {
    TEXT("text"),
    VOICE("voice"),
    MIXED("mixed");

    companion object {
        fun fromWireValue(value: String): InputMode {
            return entries.firstOrNull { it.wireValue == value } ?: TEXT
        }
    }
}

data class NoteMetadata(
    val typingWpm: Double? = null,
    val backspaceCount: Int? = null,
    val composeDurationMs: Long? = null,
    val vocalWpm: Double? = null,
    val rmsDbAvg: Double? = null,
    val rmsDbPeak: Double? = null,
    val coarseLat: Double? = null,
    val coarseLon: Double? = null,
    val pluginFlags: Map<String, String> = emptyMap()
) {
    fun toJsonObject(): JSONObject {
        val json = JSONObject()
        json.put("typing_wpm", typingWpm ?: JSONObject.NULL)
        json.put("backspace_count", backspaceCount ?: JSONObject.NULL)
        json.put("compose_duration_ms", composeDurationMs ?: JSONObject.NULL)
        json.put("vocal_wpm", vocalWpm ?: JSONObject.NULL)
        json.put("rms_db_avg", rmsDbAvg ?: JSONObject.NULL)
        json.put("rms_db_peak", rmsDbPeak ?: JSONObject.NULL)
        json.put("coarse_lat", coarseLat ?: JSONObject.NULL)
        json.put("coarse_lon", coarseLon ?: JSONObject.NULL)

        val pluginObj = JSONObject()
        for ((key, value) in pluginFlags) {
            pluginObj.put(key, value)
        }
        json.put("plugin_flags", pluginObj)
        return json
    }

    companion object {
        fun fromJsonObject(json: JSONObject): NoteMetadata {
            val typingWpm = if (json.isNull("typing_wpm")) null else json.optDouble("typing_wpm")
            val backspaceCount = if (json.isNull("backspace_count")) null else json.optInt("backspace_count")
            val composeDurationMs = if (json.isNull("compose_duration_ms")) null else json.optLong("compose_duration_ms")
            val vocalWpm = if (json.isNull("vocal_wpm")) null else json.optDouble("vocal_wpm")
            val rmsDbAvg = if (json.isNull("rms_db_avg")) null else json.optDouble("rms_db_avg")
            val rmsDbPeak = if (json.isNull("rms_db_peak")) null else json.optDouble("rms_db_peak")
            val coarseLat = if (json.isNull("coarse_lat")) null else json.optDouble("coarse_lat")
            val coarseLon = if (json.isNull("coarse_lon")) null else json.optDouble("coarse_lon")

            val flagsMap = mutableMapOf<String, String>()
            val pluginJson = json.optJSONObject("plugin_flags")
            if (pluginJson != null) {
                val keys = pluginJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    flagsMap[key] = pluginJson.optString(key, "")
                }
            }

            return NoteMetadata(
                typingWpm = typingWpm,
                backspaceCount = backspaceCount,
                composeDurationMs = composeDurationMs,
                vocalWpm = vocalWpm,
                rmsDbAvg = rmsDbAvg,
                rmsDbPeak = rmsDbPeak,
                coarseLat = coarseLat,
                coarseLon = coarseLon,
                pluginFlags = flagsMap
            )
        }
    }
}

data class MicroNote(
    val id: String,
    val content: String,
    val createdAt: String,
    val source: String = "cintamani-android",
    val inputMode: InputMode,
    val metadata: NoteMetadata,
    val isSynced: Boolean = false,
    val syncedAt: String? = null
) {
    fun toSyncJsonObject(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("content", content)
        json.put("created_at", createdAt)
        json.put("source", source)
        json.put("input_mode", inputMode.wireValue)
        json.put("metadata", metadata.toJsonObject())
        return json
    }
}

data class SyncStats(
    val unsyncedCount: Int,
    val syncedCount: Int
)

sealed class SyncResult {
    data class Success(val syncedCount: Int, val serverTime: String) : SyncResult()
    data class Error(val code: Int?, val message: String) : SyncResult()
}

sealed class PingResult {
    data class Alive(val serverTime: String, val latencyMs: Long) : PingResult()
    data class Unreachable(val reason: String) : PingResult()
}

enum class ThemeId {
    ARCHITECT_DRAFT,
    COGNITIVE_TERMINAL,
    INDUSTRIAL_ARCHIVE,
    MIDNIGHT_BLUEPRINT;

    companion object {
        fun fromStorageName(name: String): ThemeId {
            return entries.firstOrNull { it.name == name } ?: COGNITIVE_TERMINAL
        }
    }
}
