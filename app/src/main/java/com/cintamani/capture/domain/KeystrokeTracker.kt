package com.cintamani.capture.domain

class KeystrokeTracker {

    private var firstStrokeTimestamp: Long? = null
    private var lastStrokeTimestamp: Long? = null
    private var backspaceCount: Int = 0
    private var previousLength: Int = 0

    fun onTextChanged(newText: String) {
        val currentTimestamp = System.currentTimeMillis()
        val newLength = newText.length

        if (firstStrokeTimestamp == null && newLength > 0) {
            firstStrokeTimestamp = currentTimestamp
        }

        if (newLength < previousLength) {
            val delta = previousLength - newLength
            backspaceCount += delta
        }

        if (newLength > 0) {
            lastStrokeTimestamp = currentTimestamp
        }

        previousLength = newLength
    }

    data class KeystrokeMetrics(
        val typingWpm: Double,
        val backspaceCount: Int,
        val composeDurationMs: Long
    )

    fun calculateMetrics(finalText: String): KeystrokeMetrics {
        val trimmedText = finalText.trim()
        if (trimmedText.isEmpty() || firstStrokeTimestamp == null) {
            return KeystrokeMetrics(
                typingWpm = 0.0,
                backspaceCount = backspaceCount,
                composeDurationMs = 0L
            )
        }

        val start = firstStrokeTimestamp ?: System.currentTimeMillis()
        val end = lastStrokeTimestamp ?: System.currentTimeMillis()
        val durationMs = (end - start).coerceAtLeast(100L)

        // Rumus standar WPM: (Jumlah Karakter / 5) / (Menit)
        val minutes = durationMs / 60000.0
        val wordEquivalents = trimmedText.length / 5.0
        val rawWpm = if (minutes > 0.0) wordEquivalents / minutes else 0.0
        val roundedWpm = Math.round(rawWpm * 10.0) / 10.0

        return KeystrokeMetrics(
            typingWpm = roundedWpm,
            backspaceCount = backspaceCount,
            composeDurationMs = durationMs
        )
    }

    fun reset() {
        firstStrokeTimestamp = null
        lastStrokeTimestamp = null
        backspaceCount = 0
        previousLength = 0
    }
}
