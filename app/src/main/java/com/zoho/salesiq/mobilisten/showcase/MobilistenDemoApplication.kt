package com.zoho.salesiq.mobilisten.showcase

import android.app.Application
import com.zoho.salesiq.mobilisten.showcase.sdk.SalesIQManager
import com.zoho.salesiq.mobilisten.showcase.state.ConfigStore
import com.zoho.salesiq.mobilisten.showcase.state.SettingsStore

/**
 * SDK init lives here and runs automatically at launch. Credentials come from
 * BuildConfig placeholders unless overridden in Settings (persisted in prefs).
 */
class MobilistenDemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        SettingsStore.init(this, BuildConfig.APP_KEY, BuildConfig.ACCESS_KEY)
        ConfigStore.init(this)

        // Listeners are wired before init so no early event is missed.
        SalesIQManager.registerListeners()
        SalesIQManager.initialize(
            this@MobilistenDemoApplication,
            SettingsStore.appKey.value,
            SettingsStore.accessKey.value
        )
    }
}
