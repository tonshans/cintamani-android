package com.cintamani.capture.plugin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cintamani.capture.data.MicroNote
import com.cintamani.capture.data.PrefsManager

interface CintamaniPlugin {
    val id: String
    val displayName: String
    val description: String

    /**
     * Hook yang dipanggil sebelum catatan disimpan ke SQLite.
     * Plugin dapat menyematkan flag metadata tambahan tanpa mengganggu alur utama.
     */
    fun onNotePreSave(currentFlags: Map<String, String>): Map<String, String>

    /**
     * Hook pasif yang dipanggil setelah catatan berhasil tersimpan di SQLite.
     */
    fun onNoteCaptured(note: MicroNote)

    /**
     * Sub-menu konfigurasi spesifik plugin yang dirender di dalam SettingsDrawer.
     */
    @Composable
    fun SettingsSubMenu(prefsManager: PrefsManager)
}

/**
 * Contoh Plugin Referensi Bawaan: QuickContextTagPlugin
 * Menambahkan flag konteks lingkungan ("environment: mobile_node") tanpa merusak kecepatan capture.
 */
class QuickContextTagPlugin : CintamaniPlugin {
    override val id: String = "quick_context_tag"
    override val displayName: String = "Quick Context Tag"
    override val description: String = "Secara otomatis menyematkan label konteks node capture pada metadata catatan."

    override fun onNotePreSave(currentFlags: Map<String, String>): Map<String, String> {
        val updated = currentFlags.toMutableMap()
        updated["node_type"] = "mobile_capture"
        updated["client_version"] = "1.0.0"
        return updated
    }

    override fun onNoteCaptured(note: MicroNote) {
        // Logging / callback hook pasif
    }

    @Composable
    override fun SettingsSubMenu(prefsManager: PrefsManager) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Konfigurasi Label Otomatis",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Setiap catatan akan otomatis ditandai dengan flag: [node_type = mobile_capture]",
                fontSize = 11.sp
            )
        }
    }
}
