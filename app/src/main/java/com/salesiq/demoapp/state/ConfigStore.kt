package com.salesiq.demoapp.state

import android.content.Context
import com.zoho.salesiqembed.ZohoSalesIQ
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Authentication mode reflected into SalesIQConfiguration at the automatic init call. */
enum class AuthMode { Guest, UserId, Jwt }

/** A launcher visibility that the Launcher screen applied, and whether it applied it to a custom launcher. */
data class LauncherVisibility(val mode: ZohoSalesIQ.Launcher.VisibilityMode, val isCustom: Boolean)

/**
 * Persisted configuration-builder state (auth mode, user-id / JWT token, custom fonts).
 * These are read by [com.salesiq.demoapp.sdk.SalesIQManager] when it builds the
 * SalesIQConfiguration for the automatic init on next launch — mirroring the mockup's
 * "Applied at the automatic init call" note.
 *
 * It also keeps two values in memory for the current run: the department picked on the
 * Department screen, and the launcher visibility that the Launcher screen last applied.
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

    /**
     * The department picked on the Department screen, or null to let SalesIQ route chats by itself.
     * The Chat screen and the Zylker store route their chats to it.
     */
    private val _department = MutableStateFlow<String?>(null)
    val department: StateFlow<String?> = _department.asStateFlow()

    /**
     * The launcher visibility that the Zylker store restores when the visitor leaves it. Until the
     * Launcher screen applies one, it is ALWAYS, the mode that the automatic init sets.
     */
    private val _launcherVisibility = MutableStateFlow(
        LauncherVisibility(ZohoSalesIQ.Launcher.VisibilityMode.ALWAYS, isCustom = false)
    )
    val launcherVisibility: StateFlow<LauncherVisibility> = _launcherVisibility.asStateFlow()

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

    fun setDepartment(name: String?) {
        _department.value = name
    }

    fun setLauncherVisibility(visibility: LauncherVisibility) {
        _launcherVisibility.value = visibility
    }
}
