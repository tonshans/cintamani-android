package com.cintamani.capture

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.cintamani.capture.data.DatabaseHelper
import com.cintamani.capture.data.InputMode
import com.cintamani.capture.data.MicroNote
import com.cintamani.capture.data.NoteMetadata
import com.cintamani.capture.data.PingResult
import com.cintamani.capture.data.PrefsManager
import com.cintamani.capture.data.SyncResult
import com.cintamani.capture.data.ThemeId
import com.cintamani.capture.domain.CoarseLocationProvider
import com.cintamani.capture.domain.KeystrokeTracker
import com.cintamani.capture.domain.VoiceCaptureManager
import com.cintamani.capture.network.SyncClient
import com.cintamani.capture.plugin.PluginRegistry
import com.cintamani.capture.plugin.QuickContextTagPlugin
import com.cintamani.capture.ui.CaptureScreen
import com.cintamani.capture.ui.CintamaniTheme
import com.cintamani.capture.ui.SettingsDrawer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class MainActivity : ComponentActivity(), VoiceCaptureManager.VoiceCaptureListener {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var prefsManager: PrefsManager
    private lateinit var keystrokeTracker: KeystrokeTracker
    private lateinit var coarseLocationProvider: CoarseLocationProvider
    private lateinit var voiceCaptureManager: VoiceCaptureManager
    private lateinit var syncClient: SyncClient
    private lateinit var pluginRegistry: PluginRegistry

    // State untuk observasi Compose
    private var isListeningVoice by mutableStateOf(false)
    private var vocalRmsState by mutableFloatStateOf(0f)
    private var lastVocalWpm: Double? = null
    private var lastRmsAvg: Double? = null
    private var lastRmsPeak: Double? = null
    private var hasVoiceInput by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (audioGranted) {
            voiceCaptureManager.startListening()
        } else {
            Toast.makeText(this, "Izin mikrofon ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi dependensi inti secara eksplisit tanpa framework DI yang dilarang
        databaseHelper = DatabaseHelper(applicationContext)
        prefsManager = PrefsManager(applicationContext)
        keystrokeTracker = KeystrokeTracker()
        coarseLocationProvider = CoarseLocationProvider(applicationContext)
        voiceCaptureManager = VoiceCaptureManager(applicationContext, this)
        syncClient = SyncClient()
        pluginRegistry = PluginRegistry().apply {
            registerPlugin(QuickContextTagPlugin())
        }

        setContent {
            var currentThemeId by remember { mutableStateOf(prefsManager.getSelectedTheme()) }
            var inputText by remember { mutableStateOf("") }
            var syncStats by remember { mutableStateOf(databaseHelper.getSyncStats()) }
            var feedbackMessage by remember { mutableStateOf<String?>(null) }
            var pingStatusText by remember { mutableStateOf<String?>(null) }

            var serverUrlInput by remember { mutableStateOf(prefsManager.getServerUrl()) }
            var apiTokenInput by remember { mutableStateOf(prefsManager.getApiToken()) }

            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val coroutineScope = rememberCoroutineScope()

            fun triggerFeedback(message: String) {
                feedbackMessage = message
                coroutineScope.launch {
                    delay(3000)
                    if (feedbackMessage == message) {
                        feedbackMessage = null
                    }
                }
            }

            fun executeSync() {
                coroutineScope.launch {
                    val unsyncedNotes = databaseHelper.getUnsyncedNotes()
                    if (unsyncedNotes.isEmpty()) {
                        triggerFeedback("Semua catatan sudah tersinkron.")
                        return@launch
                    }

                    triggerFeedback("Menyinkronkan ${unsyncedNotes.size} catatan ke Brain...")
                    val result = syncClient.syncNotes(
                        serverUrl = prefsManager.getServerUrl(),
                        apiToken = prefsManager.getApiToken(),
                        deviceId = prefsManager.getDeviceId(),
                        notes = unsyncedNotes
                    )

                    when (result) {
                        is SyncResult.Success -> {
                            val allIds = unsyncedNotes.map { it.id }
                            databaseHelper.markNotesAsSynced(allIds, result.serverTime)
                            syncStats = databaseHelper.getSyncStats()
                            triggerFeedback("Sukses! ${result.syncedCount} catatan tersinkron.")
                        }
                        is SyncResult.Error -> {
                            triggerFeedback("Gagal Sync: ${result.message}")
                        }
                    }
                }
            }

            CintamaniTheme(themeId = currentThemeId) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            SettingsDrawer(
                                prefsManager = prefsManager,
                                pluginRegistry = pluginRegistry,
                                syncStats = syncStats,
                                serverUrlInput = serverUrlInput,
                                onServerUrlChanged = { serverUrlInput = it },
                                apiTokenInput = apiTokenInput,
                                onApiTokenChanged = { apiTokenInput = it },
                                onSaveConfig = {
                                    prefsManager.setServerUrl(serverUrlInput)
                                    prefsManager.setApiToken(apiTokenInput)
                                    triggerFeedback("Konfigurasi server berhasil disimpan.")
                                },
                                onTestPing = {
                                    pingStatusText = "Menguji koneksi ke server..."
                                    coroutineScope.launch {
                                        when (val ping = syncClient.pingServer(serverUrlInput, apiTokenInput)) {
                                            is PingResult.Alive -> {
                                                pingStatusText = "ONLINE · Latency: ${ping.latencyMs}ms · Server: ${ping.serverTime}"
                                            }
                                            is PingResult.Unreachable -> {
                                                pingStatusText = "UNREACHABLE · ${ping.reason}"
                                            }
                                        }
                                    }
                                },
                                pingStatusText = pingStatusText,
                                onSyncNow = {
                                    executeSync()
                                },
                                onClearSyncedNotes = {
                                    val deletedCount = databaseHelper.clearSyncedNotes()
                                    syncStats = databaseHelper.getSyncStats()
                                    triggerFeedback("Membersihkan $deletedCount catatan lokal tersinkron.")
                                },
                                currentThemeId = currentThemeId,
                                onSelectTheme = { selected ->
                                    currentThemeId = selected
                                    prefsManager.setSelectedTheme(selected)
                                },
                                onPluginToggle = { pluginId, enabled ->
                                    prefsManager.setPluginEnabled(pluginId, enabled)
                                },
                                onCloseDrawer = {
                                    coroutineScope.launch { drawerState.close() }
                                }
                            )
                        }
                    }
                ) {
                    CaptureScreen(
                        inputText = inputText,
                        onInputTextChanged = { newText ->
                            inputText = newText
                            keystrokeTracker.onTextChanged(newText)
                        },
                        onSaveNote = {
                            val content = inputText.trim()
                            if (content.isEmpty() && !isListeningVoice) {
                                return@CaptureScreen
                            }

                            if (isListeningVoice) {
                                voiceCaptureManager.stopListening()
                            }

                            // 1. Hitung Keystroke Dynamics
                            val keystrokeMetrics = keystrokeTracker.calculateMetrics(content)

                            // 2. Ambil Lokasi Kasar Instan (0ms delay)
                            val coords = coarseLocationProvider.getLastKnownCoordinates()

                            // 3. Tentukan Mode Input
                            val inputMode = when {
                                hasVoiceInput && keystrokeMetrics.typingWpm > 0 -> InputMode.MIXED
                                hasVoiceInput -> InputMode.VOICE
                                else -> InputMode.TEXT
                            }

                            // 4. Jalankan Plugin Hooks Pre-Save
                            val initialFlags = mutableMapOf<String, String>()
                            val enrichedFlags = pluginRegistry.applyPreSaveHooks(initialFlags, prefsManager)

                            // 5. Susun Metadata Lengkap
                            val metadata = NoteMetadata(
                                typingWpm = if (inputMode == InputMode.VOICE) null else keystrokeMetrics.typingWpm,
                                backspaceCount = keystrokeMetrics.backspaceCount,
                                composeDurationMs = keystrokeMetrics.composeDurationMs,
                                vocalWpm = lastVocalWpm,
                                rmsDbAvg = lastRmsAvg,
                                rmsDbPeak = lastRmsPeak,
                                coarseLat = coords?.latitude,
                                coarseLon = coords?.longitude,
                                pluginFlags = enrichedFlags
                            )

                            val newNote = MicroNote(
                                id = UUID.randomUUID().toString(),
                                content = content,
                                createdAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                                source = "cintamani-android",
                                inputMode = inputMode,
                                metadata = metadata,
                                isSynced = false,
                                syncedAt = null
                            )

                            // 6. Simpan ke SQLite Lokal
                            val saved = databaseHelper.insertNote(newNote)
                            if (saved) {
                                pluginRegistry.notifyNoteCaptured(newNote, prefsManager)
                                syncStats = databaseHelper.getSyncStats()
                                triggerFeedback("Tersimpan (${syncStats.unsyncedCount} belum sync)")

                                // Reset form
                                inputText = ""
                                keystrokeTracker.reset()
                                hasVoiceInput = false
                                lastVocalWpm = null
                                lastRmsAvg = null
                                lastRmsPeak = null
                            } else {
                                triggerFeedback("Gagal menyimpan ke database lokal!")
                            }
                        },
                        onToggleVoiceNote = {
                            if (isListeningVoice) {
                                voiceCaptureManager.stopListening()
                            } else {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    this@MainActivity,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    voiceCaptureManager.startListening()
                                } else {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.RECORD_AUDIO,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            }
                        },
                        isListening = isListeningVoice,
                        vocalRms = vocalRmsState,
                        syncStats = syncStats,
                        onQuickSync = { executeSync() },
                        onOpenDrawer = {
                            coroutineScope.launch { drawerState.open() }
                        },
                        feedbackMessage = feedbackMessage
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceCaptureManager.destroy()
        databaseHelper.close()
    }

    // --- VoiceCaptureManager.VoiceCaptureListener Implementation ---
    override fun onReady() {
        isListeningVoice = true
        hasVoiceInput = true
    }

    override fun onSpeechStart() {
        isListeningVoice = true
    }

    override fun onRmsChanged(rmsdB: Float) {
        vocalRmsState = rmsdB
    }

    override fun onPartialText(partial: String) {
        // Bisa diintegrasikan untuk realtime text feed jika diperlukan
    }

    override fun onResult(
        transcribedText: String,
        vocalWpm: Double,
        rmsDbAvg: Double,
        rmsDbPeak: Double
    ) {
        isListeningVoice = false
        lastVocalWpm = vocalWpm
        lastRmsAvg = rmsDbAvg
        lastRmsPeak = rmsDbPeak
        if (transcribedText.isNotBlank()) {
            hasVoiceInput = true
            // Append transcribed text
            Toast.makeText(this, "Transkripsi vokal diterima", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onError(errorMessage: String) {
        isListeningVoice = false
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
}
