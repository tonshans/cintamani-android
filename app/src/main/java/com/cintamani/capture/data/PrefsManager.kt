package com.cintamani.capture.data

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

class PrefsManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "cintamani_prefs"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_API_TOKEN = "api_token"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_SELECTED_THEME = "selected_theme"
        private const val KEY_ENABLED_PLUGINS = "enabled_plugins"

        private const val DEFAULT_SERVER_URL = "http://192.168.1.100:8000"
        private const val DEFAULT_THEME = "COGNITIVE_TERMINAL"
    }

    init {
        // Inisialisasi device_id permanen jika belum ada
        if (!prefs.contains(KEY_DEVICE_ID)) {
            val generatedId = "android-${UUID.randomUUID()}"
            prefs.edit().putString(KEY_DEVICE_ID, generatedId).apply()
        }
    }

    fun getServerUrl(): String {
        return prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
    }

    fun setServerUrl(url: String) {
        val trimmed = url.trim().trimEnd('/')
        prefs.edit().putString(KEY_SERVER_URL, trimmed).apply()
    }

    fun getApiToken(): String {
        return prefs.getString(KEY_API_TOKEN, "") ?: ""
    }

    fun setApiToken(token: String) {
        prefs.edit().putString(KEY_API_TOKEN, token.trim()).apply()
    }

    fun getDeviceId(): String {
        return prefs.getString(KEY_DEVICE_ID, "android-${UUID.randomUUID()}") ?: "android-default"
    }

    fun getSelectedTheme(): ThemeId {
        val themeName = prefs.getString(KEY_SELECTED_THEME, DEFAULT_THEME) ?: DEFAULT_THEME
        return ThemeId.fromStorageName(themeName)
    }

    fun setSelectedTheme(themeId: ThemeId) {
        prefs.edit().putString(KEY_SELECTED_THEME, themeId.name).apply()
    }

    fun getEnabledPlugins(): Set<String> {
        return prefs.getStringSet(KEY_ENABLED_PLUGINS, emptySet()) ?: emptySet()
    }

    fun setPluginEnabled(pluginId: String, enabled: Boolean) {
        val currentSet = getEnabledPlugins().toMutableSet()
        if (enabled) {
            currentSet.add(pluginId)
        } else {
            currentSet.remove(pluginId)
        }
        prefs.edit().putStringSet(KEY_ENABLED_PLUGINS, currentSet).apply()
    }

    fun isPluginEnabled(pluginId: String): Boolean {
        return getEnabledPlugins().contains(pluginId)
    }
}
