package com.zoho.salesiq.mobilisten.showcase.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.zoho.livechat.android.utils.LiveChatUtil
import com.zoho.salesiq.mobilisten.showcase.state.SDKInitStatus
import com.zoho.salesiq.mobilisten.showcase.state.SettingsStore
import com.zoho.salesiq.mobilisten.showcase.state.hasPlaceholderKeys
import com.zoho.salesiq.mobilisten.showcase.state.nextLanguage
import com.zoho.salesiq.mobilisten.showcase.ui.components.AppIcon
import com.zoho.salesiq.mobilisten.showcase.ui.components.Card
import com.zoho.salesiq.mobilisten.showcase.ui.components.Field
import com.zoho.salesiq.mobilisten.showcase.ui.components.IconTint
import com.zoho.salesiq.mobilisten.showcase.ui.components.ListRow
import com.zoho.salesiq.mobilisten.showcase.ui.components.ScreenScaffold
import com.zoho.salesiq.mobilisten.showcase.ui.components.Section
import com.zoho.salesiq.mobilisten.showcase.ui.components.SegmentedControl
import com.zoho.salesiq.mobilisten.showcase.ui.components.SwitchRow
import com.zoho.salesiq.mobilisten.showcase.ui.components.TitleTone
import com.zoho.salesiq.mobilisten.showcase.ui.components.ToastTone
import com.zoho.salesiq.mobilisten.showcase.ui.components.Toaster
import com.zoho.salesiq.mobilisten.showcase.ui.theme.ThemeMode
import com.zoho.salesiqembed.ZohoSalesIQ


/** Screen 11 — appearance, language, credentials (keys-required state), logger, SDK version. */
@Composable
fun SettingsScreen(nav: NavController) {
    val context = LocalContext.current
    val mode by SettingsStore.themeMode.collectAsState()
    val appKey by SettingsStore.appKey.collectAsState()
    val accessKey by SettingsStore.accessKey.collectAsState()
    val initStatus by SettingsStore.initStatus.collectAsState()

    var language by remember { mutableStateOf("English") }
    // Read whether the SDK's on-device debug logging is currently on.
    var loggerEnabled by remember { mutableStateOf(ZohoSalesIQ.Logger.isEnabled()) }
    var appKeyDraft by remember { mutableStateOf(if (hasPlaceholderKeys(appKey, accessKey)) "" else appKey) }
    var accessKeyDraft by remember { mutableStateOf(if (hasPlaceholderKeys(appKey, accessKey)) "" else accessKey) }

    ScreenScaffold(
        title = "Settings",
        subtitle = "Appearance, credentials, and developer tools",
        onBack = { nav.popBackStack() },
    ) {
        Section(title = "Appearance") {
            SegmentedControl(
                segments = listOf(ThemeMode.LIGHT to "Light", ThemeMode.DARK to "Dark", ThemeMode.SYSTEM to "System"),
                selected = mode,
                onSelect = { SettingsStore.setThemeMode(it) },
            )
        }

        Section(title = "Preferences") {
            Card {
                row {
                    ListRow("Language", icon = AppIcon.Globe, tint = IconTint.Primary, value = language, chevron = true, onClick = {
                        val next = nextLanguage(language)
                        language = next.label
                        // Set the language the chat UI is displayed in.
                        ZohoSalesIQ.Chat.setLanguage(next.code)
                    })
                }
            }
        }

        Section(title = "Credentials") {
            Card {
                row { Field("App key", appKeyDraft, { appKeyDraft = it }, placeholder = "<YOUR_APP_KEY>", autoCapitalize = false) }
                row { Field("Access key", accessKeyDraft, { accessKeyDraft = it }, placeholder = "<YOUR_ACCESS_KEY>", autoCapitalize = false) }
                if (initStatus == SDKInitStatus.KeysRequired) {
                    row { ListRow("Keys required", subtitle = "Add keys — the SDK initializes on next launch", icon = AppIcon.Key, tint = IconTint.Danger) }
                }
                row {
                    ListRow("Save keys", titleTone = TitleTone.Brand, onClick = {
                        if (appKeyDraft.isBlank() || accessKeyDraft.isBlank()) {
                            Toaster.show("Both keys are required", ToastTone.Danger)
                        } else {
                            SettingsStore.setCredentials(appKeyDraft.trim(), accessKeyDraft.trim())
                            Toaster.show("Keys saved — restart the app to initialize", ToastTone.Success)
                        }
                    })
                }
            }
        }

        Section(title = "Developer") {
            Card {
                row {
                    SwitchRow("Debug logs", loggerEnabled, {
                        loggerEnabled = it
                        // Turn the SDK's on-device debug logging on or off.
                        ZohoSalesIQ.Logger.setEnabled(it)
                    }, subtitle = "Capture SDK logs on device", icon = AppIcon.Logs, tint = IconTint.Accent)
                }
                row {
                    ListRow("View logs", titleTone = TitleTone.Brand, chevron = true, onClick = {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.zoho.com/salesiq/help/developer-section/android-sdk-logger.html")))
                        }.onFailure { error ->
                            failToast("Couldn't open logs guide", error)
                        }
                    })
                }
                row { ListRow("SDK version", value = LiveChatUtil.getMobilistenVersionName()) }
            }
        }
    }
}
