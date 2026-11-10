package com.cintamani.capture.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONObject

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "cintamani_local.db"
        const val DATABASE_VERSION = 1

        const val TABLE_MICRO_NOTES = "micro_notes"
        const val COL_ID = "id"
        const val COL_CONTENT = "content"
        const val COL_CREATED_AT = "created_at"
        const val COL_SOURCE = "source"
        const val COL_INPUT_MODE = "input_mode"
        const val COL_TYPING_WPM = "typing_wpm"
        const val COL_BACKSPACE_COUNT = "backspace_count"
        const val COL_COMPOSE_DURATION_MS = "compose_duration_ms"
        const val COL_VOCAL_WPM = "vocal_wpm"
        const val COL_RMS_DB_AVG = "rms_db_avg"
        const val COL_RMS_DB_PEAK = "rms_db_peak"
        const val COL_COARSE_LAT = "coarse_lat"
        const val COL_COARSE_LON = "coarse_lon"
        const val COL_PLUGIN_FLAGS_JSON = "plugin_flags_json"
        const val COL_IS_SYNCED = "is_synced"
        const val COL_SYNCED_AT = "synced_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableSql = """
            CREATE TABLE IF NOT EXISTS $TABLE_MICRO_NOTES (
                $COL_ID TEXT PRIMARY KEY NOT NULL,
                $COL_CONTENT TEXT NOT NULL,
                $COL_CREATED_AT TEXT NOT NULL,
                $COL_SOURCE TEXT NOT NULL,
                $COL_INPUT_MODE TEXT NOT NULL,
                $COL_TYPING_WPM REAL,
                $COL_BACKSPACE_COUNT INTEGER,
                $COL_COMPOSE_DURATION_MS INTEGER,
                $COL_VOCAL_WPM REAL,
                $COL_RMS_DB_AVG REAL,
                $COL_RMS_DB_PEAK REAL,
                $COL_COARSE_LAT REAL,
                $COL_COARSE_LON REAL,
                $COL_PLUGIN_FLAGS_JSON TEXT NOT NULL DEFAULT '{}',
                $COL_IS_SYNCED INTEGER NOT NULL DEFAULT 0,
                $COL_SYNCED_AT TEXT
            );
        """.trimIndent()
        db.execSQL(createTableSql)

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_micro_notes_is_synced ON $TABLE_MICRO_NOTES($COL_IS_SYNCED);")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_micro_notes_created_at ON $TABLE_MICRO_NOTES($COL_CREATED_AT);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Skema saat ini adalah versi 1. Migrasi ke depan ditambahkan di sini tanpa menghapus data mentah.
    }

    fun insertNote(note: MicroNote): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ID, note.id)
            put(COL_CONTENT, note.content)
            put(COL_CREATED_AT, note.createdAt)
            put(COL_SOURCE, note.source)
            put(COL_INPUT_MODE, note.inputMode.wireValue)

            note.metadata.typingWpm?.let { put(COL_TYPING_WPM, it) } ?: putNull(COL_TYPING_WPM)
            note.metadata.backspaceCount?.let { put(COL_BACKSPACE_COUNT, it) } ?: putNull(COL_BACKSPACE_COUNT)
            note.metadata.composeDurationMs?.let { put(COL_COMPOSE_DURATION_MS, it) } ?: putNull(COL_COMPOSE_DURATION_MS)
            note.metadata.vocalWpm?.let { put(COL_VOCAL_WPM, it) } ?: putNull(COL_VOCAL_WPM)
            note.metadata.rmsDbAvg?.let { put(COL_RMS_DB_AVG, it) } ?: putNull(COL_RMS_DB_AVG)
            note.metadata.rmsDbPeak?.let { put(COL_RMS_DB_PEAK, it) } ?: putNull(COL_RMS_DB_PEAK)
            note.metadata.coarseLat?.let { put(COL_COARSE_LAT, it) } ?: putNull(COL_COARSE_LAT)
            note.metadata.coarseLon?.let { put(COL_COARSE_LON, it) } ?: putNull(COL_COARSE_LON)

            val flagsObj = JSONObject()
            for ((key, value) in note.metadata.pluginFlags) {
                flagsObj.put(key, value)
            }
            put(COL_PLUGIN_FLAGS_JSON, flagsObj.toString())

            put(COL_IS_SYNCED, if (note.isSynced) 1 else 0)
            put(COL_SYNCED_AT, note.syncedAt)
        }

        val rowId = db.insertWithOnConflict(TABLE_MICRO_NOTES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        return rowId != -1L
    }

    fun getUnsyncedNotes(): List<MicroNote> {
        val notes = mutableListOf<MicroNote>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_MICRO_NOTES,
            null,
            "$COL_IS_SYNCED = 0",
            null,
            null,
            null,
            "$COL_CREATED_AT ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                notes.add(cursorToNote(it))
            }
        }
        return notes
    }

    fun markNotesAsSynced(ids: List<String>, syncedAt: String): Int {
        if (ids.isEmpty()) return 0
        val db = writableDatabase
        var updatedRows = 0

        db.beginTransaction()
        try {
            val values = ContentValues().apply {
                put(COL_IS_SYNCED, 1)
                put(COL_SYNCED_AT, syncedAt)
            }

            for (id in ids) {
                updatedRows += db.update(
                    TABLE_MICRO_NOTES,
                    values,
                    "$COL_ID = ?",
                    arrayOf(id)
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        return updatedRows
    }

    fun clearSyncedNotes(): Int {
        val db = writableDatabase
        return db.delete(TABLE_MICRO_NOTES, "$COL_IS_SYNCED = 1", null)
    }

    fun getSyncStats(): SyncStats {
        val db = readableDatabase
        var unsynced = 0
        var synced = 0

        val unsyncedCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_MICRO_NOTES WHERE $COL_IS_SYNCED = 0", null)
        unsyncedCursor.use {
            if (it.moveToFirst()) {
                unsynced = it.getInt(0)
            }
        }

        val syncedCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_MICRO_NOTES WHERE $COL_IS_SYNCED = 1", null)
        syncedCursor.use {
            if (it.moveToFirst()) {
                synced = it.getInt(0)
            }
        }

        return SyncStats(unsyncedCount = unsynced, syncedCount = synced)
    }

    fun getRecentNotes(limit: Int = 20): List<MicroNote> {
        val notes = mutableListOf<MicroNote>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_MICRO_NOTES,
            null,
            null,
            null,
            null,
            null,
            "$COL_CREATED_AT DESC",
            limit.toString()
        )

        cursor.use {
            while (it.moveToNext()) {
                notes.add(cursorToNote(it))
            }
        }
        return notes
    }

    private fun cursorToNote(cursor: Cursor): MicroNote {
        val id = cursor.getString(cursor.getColumnIndexOrThrow(COL_ID))
        val content = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT))
        val createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COL_CREATED_AT))
        val source = cursor.getString(cursor.getColumnIndexOrThrow(COL_SOURCE))
        val inputModeStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_INPUT_MODE))
        val inputMode = InputMode.fromWireValue(inputModeStr)

        val typingWpm = if (cursor.isNull(cursor.getColumnIndexOrThrow(COL_TYPING_WPM))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TYPING_WPM))
        val backspaceCount = if (cursor.isNull(cursor.getColumnIndexOrThrow(COL_BACKSPACE_COUNT))) null else cursor.getInt(cursor.getColumnIndexOrThrow(COL_BACKSPACE_COUNT))
        val composeDurationMs = if (cursor.isNull(cursor.getColumnIndexOrThrow(COL_COMPOSE_DURATION_MS))) null else cursor.getLong(cursor.getColumnIndexOrThrow(COL_COMPOSE_DURATION_MS))
        val vocalWpm = if (cursor.isNull(cursor.getColumnIndexOrThrow(COL_VOCAL_WPM))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(COL_VOCAL_WPM))
        val rmsDbAvg = if (cursor.isNull(cursor.getColumnIndexOrThrow(COL_RMS_DB_AVG))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(COL_RMS_DB_AVG))
        val rmsDbPeak = if (cursor.isNull(cursor.getColumnIndexOrThrow(COL_RMS_DB_PEAK))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(COL_RMS_DB_PEAK))
        val coarseLat = if (cursor.isNull(cursor.getColumnIndexOrThrow(COL_COARSE_LAT))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(COL_COARSE_LAT))
        val coarseLon = if (cursor.isNull(cursor.getColumnIndexOrThrow(COL_COARSE_LON))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(COL_COARSE_LON))

        val flagsJsonStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_PLUGIN_FLAGS_JSON)) ?: "{}"
        val flagsMap = mutableMapOf<String, String>()
        try {
            val flagsObj = JSONObject(flagsJsonStr)
            val keys = flagsObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                flagsMap[key] = flagsObj.optString(key, "")
            }
        } catch (_: Exception) {
            // Abaikan parse error, gunakan map kosong
        }

        val isSynced = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_SYNCED)) == 1
        val syncedAt = if (cursor.isNull(cursor.getColumnIndexOrThrow(COL_SYNCED_AT))) null else cursor.getString(cursor.getColumnIndexOrThrow(COL_SYNCED_AT))

        val metadata = NoteMetadata(
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

        return MicroNote(
            id = id,
            content = content,
            createdAt = createdAt,
            source = source,
            inputMode = inputMode,
            metadata = metadata,
            isSynced = isSynced,
            syncedAt = syncedAt
        )
    }
}
