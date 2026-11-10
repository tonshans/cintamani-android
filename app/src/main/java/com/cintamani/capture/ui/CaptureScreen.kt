package com.cintamani.capture.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cintamani.capture.data.SyncStats

@Composable
fun CaptureScreen(
    inputText: String,
    onInputTextChanged: (String) -> Unit,
    onSaveNote: () -> Unit,
    onToggleVoiceNote: () -> Unit,
    isListening: Boolean,
    vocalRms: Float,
    syncStats: SyncStats,
    onQuickSync: () -> Unit,
    onOpenDrawer: () -> Unit,
    feedbackMessage: String?
) {
    val palette = LocalCintamaniTheme.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.canvas)
            .padding(16.dp)
    ) {
        // --- 1. Top Status Strip ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "CINTAMANI",
                    color = palette.text,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(10.dp))
                BrutalistBadge(
                    text = "Unsynced: ${syncStats.unsyncedCount}",
                    backgroundColor = if (syncStats.unsyncedCount > 0) palette.accent2 else palette.surface,
                    textColor = if (syncStats.unsyncedCount > 0 && !palette.isDark) Color(0xFF111111) else palette.text
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Tombol Quick Sync
                BrutalistButton(
                    onClick = onQuickSync,
                    backgroundColor = palette.surface,
                    textColor = palette.text,
                    paddingValues = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shadowOffset = 3.dp,
                    borderWidth = 2.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync",
                            tint = palette.text,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SYNC",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.text
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Tombol Buka Drawer Pengaturan
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier
                        .size(38.dp)
                        .background(palette.surface, RoundedCornerShape(2.dp))
                        .border(2.dp, palette.border, RoundedCornerShape(2.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = palette.text,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // --- Banner Umpan Balik Transien ---
        if (!feedbackMessage.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .background(palette.accent1, RoundedCornerShape(2.dp))
                    .border(2.dp, palette.border, RoundedCornerShape(2.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = feedbackMessage,
                    color = if (palette.isDark) Color(0xFF09090B) else Color(0xFF111111),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- 2. Core Textbox ---
        BrutalistBox(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            backgroundColor = palette.surface,
            borderColor = palette.border
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                BasicTextField(
                    value = inputText,
                    onValueChange = onInputTextChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    textStyle = TextStyle(
                        color = palette.text,
                        fontSize = 17.sp,
                        lineHeight = 25.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(palette.accent1),
                    decorationBox = { innerTextField ->
                        if (inputText.isEmpty() && !isListening) {
                            Text(
                                text = "Ketik pemikiran mentah Anda di sini... (tanpa sensor, simpan langsung)",
                                color = palette.text.copy(alpha = 0.4f),
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                )

                // Baris Info Mikro di Bawah Kotak Ketik
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isListening) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "● MENDENGARKAN...",
                                color = palette.accent2,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RMS: ${Math.round(vocalRms * 10.0) / 10.0} dB",
                                color = palette.text.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        Text(
                            text = "Offline First · Instant Capture",
                            color = palette.text.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = "${inputText.length} chars",
                        color = palette.text.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. Action Row (2 Tombol Utama) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tombol VOICE NOTE
            BrutalistButton(
                onClick = onToggleVoiceNote,
                modifier = Modifier.weight(1f),
                backgroundColor = if (isListening) palette.accent2 else palette.surface,
                textColor = if (isListening && !palette.isDark) Color(0xFF111111) else palette.text,
                paddingValues = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Note",
                        tint = if (isListening && !palette.isDark) Color(0xFF111111) else palette.text,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isListening) "STOP SUARA" else "VOICE NOTE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Tombol CAPTURE / SIMPAN
            BrutalistButton(
                onClick = onSaveNote,
                modifier = Modifier.weight(1.3f),
                backgroundColor = palette.accent1,
                textColor = if (palette.isDark) Color(0xFF09090B) else Color(0xFF111111),
                enabled = inputText.trim().isNotEmpty() || isListening,
                paddingValues = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Capture",
                        tint = if (palette.isDark) Color(0xFF09090B) else Color(0xFF111111),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CAPTURE (SIMPAN)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
