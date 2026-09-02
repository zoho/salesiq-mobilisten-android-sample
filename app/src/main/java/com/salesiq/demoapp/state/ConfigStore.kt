package com.salesiq.demoapp.state

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Authentication mode reflected into SalesIQConfiguration at the automatic init call. */
enum class AuthMode { Guest, UserId, Jwt }

/**
 * Persisted configuration-builder state (auth mode, user-id / JWT token, custom fonts).
 * These are read by [com.salesiq.demoapp.sdk.SalesIQManager] when it builds the
 * SalesIQConfiguration for the automatic init on next launch — mirroring the mockup's
 * "Applied at the automatic init call" note.
 */
object ConfigStore {
    private const val PREFS = "mobilisten_demo_config"
    private const val KEY_AUTH_MODE = "authMode"
    private const val KEY_AUTH_VALUE = "authValue"
    private const val KEY_FONTS = "customFonts"

    private lateinit var prefs: android.content.SharedPreferences

    private val _authMode = MutableStateFlow(AuthMode.Guest)
    val authMode: StateFlow<AuthMode> = _authMode.asStateFlow()

    /** User id (UserId mode) or JWT token (Jwt mode). Ignored for Guest. */
    private val _authValue = MutableStateFlow("")
    val authValue: StateFlow<String> = _authValue.asStateFlow()

    private val _customFonts = MutableStateFlow(false)
    val customFonts: StateFlow<Boolean> = _customFonts.asStateFlow()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        _authMode.value = runCatching {
            AuthMode.valueOf(prefs.getString(KEY_AUTH_MODE, AuthMode.Guest.name)!!)
        }.getOrDefault(AuthMode.Guest)
        _authValue.value = prefs.getString(KEY_AUTH_VALUE, "") ?: ""
        _customFonts.value = prefs.getBoolean(KEY_FONTS, false)
    }

    fun setAuthMode(mode: AuthMode) {
        _authMode.value = mode
        if (::prefs.isInitialized) prefs.edit().putString(KEY_AUTH_MODE, mode.name).apply()
    }

    fun setAuthValue(value: String) {
        _authValue.value = value
        if (::prefs.isInitialized) prefs.edit().putString(KEY_AUTH_VALUE, value).apply()
    }

    fun setCustomFonts(enabled: Boolean) {
        _customFonts.value = enabled
        if (::prefs.isInitialized) prefs.edit().putBoolean(KEY_FONTS, enabled).apply()
    }
}
