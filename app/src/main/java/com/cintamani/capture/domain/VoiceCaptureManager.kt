package com.cintamani.capture.domain

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

class VoiceCaptureManager(
    private val context: Context,
    private val listener: VoiceCaptureListener
) {

    interface VoiceCaptureListener {
        fun onReady()
        fun onSpeechStart()
        fun onRmsChanged(rmsdB: Float)
        fun onPartialText(partial: String)
        fun onResult(transcribedText: String, vocalWpm: Double, rmsDbAvg: Double, rmsDbPeak: Double)
        fun onError(errorMessage: String)
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var isCurrentlyListening: Boolean = false
    private var speechStartTimestamp: Long = 0L
    private var speechEndTimestamp: Long = 0L
    private val rmsSamples = mutableListOf<Float>()

    fun isListening(): Boolean = isCurrentlyListening

    fun startListening(languageCode: String = Locale.getDefault().toLanguageTag()) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            listener.onError("Pengenalan suara tidak tersedia pada perangkat ini.")
            return
        }

        if (isCurrentlyListening) {
            stopListening()
            return
        }

        try {
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            }

            rmsSamples.clear()
            speechStartTimestamp = 0L
            speechEndTimestamp = 0L

            speechRecognizer?.setRecognitionListener(createRecognitionListener())

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            speechRecognizer?.startListening(intent)
            isCurrentlyListening = true
        } catch (e: Exception) {
            isCurrentlyListening = false
            listener.onError("Gagal memulai pengenalan suara: ${e.message}")
        }
    }

    fun stopListening() {
        if (!isCurrentlyListening) return
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {
        } finally {
            isCurrentlyListening = false
        }
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {
        } finally {
            speechRecognizer = null
            isCurrentlyListening = false
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                listener.onReady()
            }

            override fun onBeginningOfSpeech() {
                speechStartTimestamp = System.currentTimeMillis()
                listener.onSpeechStart()
            }

            override fun onRmsChanged(rmsdB: Float) {
                if (rmsdB > 0f) {
                    rmsSamples.add(rmsdB)
                }
                listener.onRmsChanged(rmsdB)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                speechEndTimestamp = System.currentTimeMillis()
                isCurrentlyListening = false
            }

            override fun onError(error: Int) {
                isCurrentlyListening = false
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Kesalahan perekaman audio"
                    SpeechRecognizer.ERROR_CLIENT -> "Kesalahan klien speech"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Izin mikrofon diperlukan"
                    SpeechRecognizer.ERROR_NETWORK -> "Kesalahan jaringan"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Waktu jaringan habis"
                    SpeechRecognizer.ERROR_NO_MATCH -> "Suara tidak terdeteksi"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Layanan pengenalan suara sedang sibuk"
                    SpeechRecognizer.ERROR_SERVER -> "Kesalahan server pengenalan suara"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Tidak ada suara terdeteksi dalam batas waktu"
                    else -> "Terjadi kesalahan pengenalan suara ($error)"
                }
                listener.onError(errorMsg)
            }

            override fun onResults(results: Bundle?) {
                isCurrentlyListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.EXTRA_RESULTS)
                val fullText = matches?.firstOrNull() ?: ""

                if (speechEndTimestamp <= speechStartTimestamp) {
                    speechEndTimestamp = System.currentTimeMillis()
                }

                val durationMs = (speechEndTimestamp - speechStartTimestamp).coerceAtLeast(500L)
                val durationMinutes = durationMs / 60000.0

                // Hitung WPM vokal berdasarkan jumlah kata
                val wordCount = fullText.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
                val rawVocalWpm = if (durationMinutes > 0.0) wordCount / durationMinutes else 0.0
                val roundedVocalWpm = Math.round(rawVocalWpm * 10.0) / 10.0

                // Hitung statistik RMS Desibel
                val avgRms = if (rmsSamples.isNotEmpty()) {
                    Math.round(rmsSamples.average() * 10.0) / 10.0
                } else {
                    0.0
                }

                val peakRms = if (rmsSamples.isNotEmpty()) {
                    Math.round((rmsSamples.maxOrNull() ?: 0f) * 10.0) / 10.0
                } else {
                    0.0
                }

                listener.onResult(
                    transcribedText = fullText,
                    vocalWpm = roundedVocalWpm,
                    rmsDbAvg = avgRms,
                    rmsDbPeak = peakRms
                )
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.EXTRA_RESULTS)
                val partial = matches?.firstOrNull() ?: ""
                if (partial.isNotEmpty()) {
                    listener.onPartialText(partial)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }
}
