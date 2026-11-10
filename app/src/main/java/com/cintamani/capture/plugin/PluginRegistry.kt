package com.cintamani.capture.plugin

import com.cintamani.capture.data.MicroNote
import com.cintamani.capture.data.PrefsManager

class PluginRegistry {

    private val plugins = mutableListOf<CintamaniPlugin>()

    fun registerPlugin(plugin: CintamaniPlugin) {
        if (plugins.none { it.id == plugin.id }) {
            plugins.add(plugin)
        }
    }

    fun getRegisteredPlugins(): List<CintamaniPlugin> {
        return plugins.toList()
    }

    fun applyPreSaveHooks(
        initialFlags: Map<String, String>,
        prefsManager: PrefsManager
    ): Map<String, String> {
        var currentFlags = initialFlags.toMap()
        val enabledIds = prefsManager.getEnabledPlugins()

        for (plugin in plugins) {
            if (enabledIds.contains(plugin.id)) {
                try {
                    currentFlags = plugin.onNotePreSave(currentFlags)
                } catch (_: Exception) {
                    // Isolasi error: kegagalan satu plugin tidak boleh membatalkan penyimpanan catatan
                }
            }
        }
        return currentFlags
    }

    fun notifyNoteCaptured(note: MicroNote, prefsManager: PrefsManager) {
        val enabledIds = prefsManager.getEnabledPlugins()
        for (plugin in plugins) {
            if (enabledIds.contains(plugin.id)) {
                try {
                    plugin.onNoteCaptured(note)
                } catch (_: Exception) {
                    // Isolasi error hook pasif
                }
            }
        }
    }
}
