package com.salesiq.demoapp.state

import android.content.Context
import com.salesiq.demoapp.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Placeholder credentials — replace with real keys from the SalesIQ portal. */
const val PLACEHOLDER_APP_KEY = "<YOUR_APP_KEY>"
const val PLACEHOLDER_ACCESS_KEY = "<YOUR_ACCESS_KEY>"

enum class SDKInitStatus { Pending, Initialized, Failed, KeysRequired }

/** True while the app is still shipping placeholder credentials. */
fun hasPlaceholderKeys(appKey: String, accessKey: String): Boolean =
    appKey.startsWith("<YOUR_") || accessKey.startsWith("<YOUR_")

/**
 * Persisted app settings: appearance override, stored SDK credentials, and the live
 * init status hero used by Home / Core / Settings. Credentials are stored in the
 * same SharedPreferences the Application reads at launch.
 */
object SettingsStore {
    private const val PREFS = "mobilisten_demo_settings"
    private const val KEY_APP = "customAppKey"
    private const val KEY_ACCESS = "customAccessKey"
    private const val KEY_THEME = "themeMode"

    private lateinit var prefs: android.content.SharedPreferences

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _appKey = MutableStateFlow(PLACEHOLDER_APP_KEY)
    val appKey: StateFlow<String> = _appKey.asStateFlow()

    private val _accessKey = MutableStateFlow(PLACEHOLDER_ACCESS_KEY)
    val accessKey: StateFlow<String> = _accessKey.asStateFlow()

    private val _initStatus = MutableStateFlow(SDKInitStatus.Pending)
    val initStatus: StateFlow<SDKInitStatus> = _initStatus.asStateFlow()

    private val _initError = MutableStateFlow<String?>(null)
    val initError: StateFlow<String?> = _initError.asStateFlow()

    fun init(context: Context, defaultAppKey: String, defaultAccessKey: String) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        _themeMode.value = runCatching {
            ThemeMode.valueOf(prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name)!!)
        }.getOrDefault(ThemeMode.SYSTEM)
        _appKey.value = prefs.getString(KEY_APP, null) ?: defaultAppKey
        _accessKey.value = prefs.getString(KEY_ACCESS, null) ?: defaultAccessKey
        if (hasPlaceholderKeys(_appKey.value, _accessKey.value)) {
            _initStatus.value = SDKInitStatus.KeysRequired
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        if (::prefs.isInitialized) prefs.edit().putString(KEY_THEME, mode.name).apply()
    }

    fun setCredentials(appKey: String, accessKey: String) {
        _appKey.value = appKey
        _accessKey.value = accessKey
        if (::prefs.isInitialized) {
            prefs.edit().putString(KEY_APP, appKey).putString(KEY_ACCESS, accessKey).apply()
        }
        _initStatus.value = if (hasPlaceholderKeys(appKey, accessKey))
            SDKInitStatus.KeysRequired else SDKInitStatus.Pending
    }

    fun setInitStatus(status: SDKInitStatus, error: String? = null) {
        _initStatus.value = status
        _initError.value = error
    }
}
