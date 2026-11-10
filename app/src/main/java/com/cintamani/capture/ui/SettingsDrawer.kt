package com.cintamani.capture.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.cintamani.capture.data.PrefsManager
import com.cintamani.capture.data.SyncStats
import com.cintamani.capture.data.ThemeId
import com.cintamani.capture.plugin.PluginRegistry

@Composable
fun SettingsDrawer(
    prefsManager: PrefsManager,
    pluginRegistry: PluginRegistry,
    syncStats: SyncStats,
    serverUrlInput: String,
    onServerUrlChanged: (String) -> Unit,
    apiTokenInput: String,
    onApiTokenChanged: (String) -> Unit,
    onSaveConfig: () -> Unit,
    onTestPing: () -> Unit,
    pingStatusText: String?,
    onSyncNow: () -> Unit,
    onClearSyncedNotes: () -> Unit,
    currentThemeId: ThemeId,
    onSelectTheme: (ThemeId) -> Unit,
    onPluginToggle: (String, Boolean) -> Unit,
    onCloseDrawer: () -> Unit
) {
    val palette = LocalCintamaniTheme.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.90f)
            .background(palette.canvas)
            .border(width = 3.dp, color = palette.border)
            .padding(20.dp)
            .verticalScroll(scrollState)
    ) {
        // Header Laci
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PENGATURAN",
                color = palette.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            IconButton(
                onClick = onCloseDrawer,
                modifier = Modifier
                    .size(32.dp)
                    .background(palette.surface, RoundedCornerShape(2.dp))
                    .border(2.dp, palette.border, RoundedCornerShape(2.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = palette.text,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // 1. Brain Server Configuration
        // ==========================================
        SectionHeader(title = "1. HOME SERVER CONFIG")
        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "Server Base URL", color = palette.text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        DrawerInputField(
            value = serverUrlInput,
            onValueChange = onServerUrlChanged,
            placeholder = "http://192.168.1.100:8000"
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "API Token (Bearer)", color = palette.text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        DrawerInputField(
            value = apiTokenInput,
            onValueChange = onApiTokenChanged,
            placeholder = "Masukkan string API Token..."
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BrutalistButton(
                onClick = onSaveConfig,
                modifier = Modifier.weight(1f),
                backgroundColor = palette.accent1,
                paddingValues = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp)
            ) {
                Text(text = "SIMPAN", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            BrutalistButton(
                onClick = onTestPing,
                modifier = Modifier.weight(1f),
                backgroundColor = palette.surface,
                textColor = palette.text,
                paddingValues = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp)
            ) {
                Text(text = "TEST PING", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (!pingStatusText.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.surface, RoundedCornerShape(2.dp))
                    .border(2.dp, palette.border, RoundedCornerShape(2.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = pingStatusText,
                    color = palette.accent2,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 2. Manual Synchronization & Storage
        // ==========================================
        SectionHeader(title = "2. SINKRONISASI & STORAGE")
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                label = "Belum Tersinkron",
                value = syncStats.unsyncedCount.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Sudah Tersinkron",
                value = syncStats.syncedCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        BrutalistButton(
            onClick = onSyncNow,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = palette.accent2,
            textColor = if (!palette.isDark) Color(0xFF111111) else palette.text,
            paddingValues = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
        ) {
            Text(text = "SYNC TO BRAIN NOW", fontSize = 12.sp, fontWeight = FontWeight.Black)
        }

        Spacer(modifier = Modifier.height(10.dp))

        BrutalistButton(
            onClick = onClearSyncedNotes,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = palette.surface,
            textColor = palette.text,
            paddingValues = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp)
        ) {
            Text(text = "CLEAR SYNCED NOTES", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 3. Theme Selector
        // ==========================================
        SectionHeader(title = "3. PILIH TEMA WARNA")
        Spacer(modifier = Modifier.height(10.dp))

        ThemeOptionItem(
            themeId = ThemeId.COGNITIVE_TERMINAL,
            name = "Cognitive Terminal (Dark)",
            accentColor = Color(0xFF4ADE80),
            isSelected = currentThemeId == ThemeId.COGNITIVE_TERMINAL,
            onClick = { onSelectTheme(ThemeId.COGNITIVE_TERMINAL) }
        )
        Spacer(modifier = Modifier.height(8.dp))

        ThemeOptionItem(
            themeId = ThemeId.ARCHITECT_DRAFT,
            name = "Architect's Draft (Light)",
            accentColor = Color(0xFF81A1C1),
            isSelected = currentThemeId == ThemeId.ARCHITECT_DRAFT,
            onClick = { onSelectTheme(ThemeId.ARCHITECT_DRAFT) }
        )
        Spacer(modifier = Modifier.height(8.dp))

        ThemeOptionItem(
            themeId = ThemeId.INDUSTRIAL_ARCHIVE,
            name = "Industrial Archive (Light)",
            accentColor = Color(0xFFA3B18A),
            isSelected = currentThemeId == ThemeId.INDUSTRIAL_ARCHIVE,
            onClick = { onSelectTheme(ThemeId.INDUSTRIAL_ARCHIVE) }
        )
        Spacer(modifier = Modifier.height(8.dp))

        ThemeOptionItem(
            themeId = ThemeId.MIDNIGHT_BLUEPRINT,
            name = "Midnight Blueprint (Dark)",
            accentColor = Color(0xFF22D3EE),
            isSelected = currentThemeId == ThemeId.MIDNIGHT_BLUEPRINT,
            onClick = { onSelectTheme(ThemeId.MIDNIGHT_BLUEPRINT) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 4. Plugin Slots
        // ==========================================
        SectionHeader(title = "4. PLUGIN EKSTENSI")
        Spacer(modifier = Modifier.height(10.dp))

        val plugins = pluginRegistry.getRegisteredPlugins()
        if (plugins.isEmpty()) {
            Text(
                text = "Belum ada plugin eksternal terdaftar.",
                color = palette.text.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        } else {
            for (plugin in plugins) {
                val isEnabled = prefsManager.isPluginEnabled(plugin.id)
                PluginItemCard(
                    plugin = plugin,
                    isEnabled = isEnabled,
                    onToggle = { onPluginToggle(plugin.id, it) },
                    prefsManager = prefsManager
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    val palette = LocalCintamaniTheme.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = palette.accent1,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(palette.border)
        )
    }
}

@Composable
private fun DrawerInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    val palette = LocalCintamaniTheme.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.surface, RoundedCornerShape(2.dp))
            .border(2.dp, palette.border, RoundedCornerShape(2.dp))
            .padding(12.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                color = palette.text,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            ),
            cursorBrush = SolidColor(palette.accent1),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(text = placeholder, color = palette.text.copy(alpha = 0.4f), fontSize = 13.sp)
                }
                innerTextField()
            }
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    val palette = LocalCintamaniTheme.current
    Box(
        modifier = modifier
            .background(palette.surface, RoundedCornerShape(2.dp))
            .border(2.dp, palette.border, RoundedCornerShape(2.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(text = label, color = palette.text.copy(alpha = 0.6f), fontSize = 10.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, color = palette.text, fontSize = 20.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun ThemeOptionItem(
    themeId: ThemeId,
    name: String,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val palette = LocalCintamaniTheme.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isSelected) palette.surface else palette.canvas, RoundedCornerShape(2.dp))
            .border(if (isSelected) 3.dp else 1.dp, if (isSelected) palette.accent1 else palette.border, RoundedCornerShape(2.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(accentColor, RoundedCornerShape(2.dp))
                    .border(1.dp, palette.border, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = name,
                color = palette.text,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }

        if (isSelected) {
            BrutalistBadge(
                text = "ACTIVE",
                backgroundColor = palette.accent1,
                textColor = if (palette.isDark) Color(0xFF09090B) else Color(0xFF111111)
            )
        }
    }
}

@Composable
private fun PluginItemCard(
    plugin: com.cintamani.capture.plugin.CintamaniPlugin,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    prefsManager: PrefsManager
) {
    val palette = LocalCintamaniTheme.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.surface, RoundedCornerShape(2.dp))
            .border(2.dp, palette.border, RoundedCornerShape(2.dp))
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = plugin.displayName, color = palette.text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(text = plugin.description, color = palette.text.copy(alpha = 0.6f), fontSize = 11.sp)
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = palette.accent1,
                        checkedTrackColor = palette.surface,
                        uncheckedThumbColor = palette.border,
                        uncheckedTrackColor = palette.canvas
                    )
                )
            }

            if (isEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.canvas, RoundedCornerShape(2.dp))
                        .border(1.dp, palette.border, RoundedCornerShape(2.dp))
                        .padding(8.dp)
                ) {
                    plugin.SettingsSubMenu(prefsManager = prefsManager)
                }
            }
        }
    }
}
