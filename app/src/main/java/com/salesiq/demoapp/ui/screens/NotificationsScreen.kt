package com.salesiq.demoapp.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.salesiq.demoapp.state.EventSource
import com.salesiq.demoapp.state.EventStore
import com.salesiq.demoapp.ui.components.AppButton
import com.salesiq.demoapp.ui.components.AppIcon
import com.salesiq.demoapp.ui.components.AppText
import com.salesiq.demoapp.ui.components.BannerVariant
import com.salesiq.demoapp.ui.components.ButtonVariant
import com.salesiq.demoapp.ui.components.Card
import com.salesiq.demoapp.ui.components.IconTint
import com.salesiq.demoapp.ui.components.ListRow
import com.salesiq.demoapp.ui.components.ResultBlock
import com.salesiq.demoapp.ui.components.ScreenScaffold
import com.salesiq.demoapp.ui.components.Section
import com.salesiq.demoapp.ui.components.SegmentedControl
import com.salesiq.demoapp.ui.components.StatusBanner
import com.salesiq.demoapp.ui.components.SwitchRow
import com.salesiq.demoapp.ui.components.TextTone
import com.salesiq.demoapp.ui.components.TitleTone
import com.salesiq.demoapp.ui.components.ToastTone
import com.salesiq.demoapp.ui.components.Toaster
import com.salesiq.demoapp.ui.theme.AppType
import com.zoho.salesiqembed.ZohoSalesIQ
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class Source { App, Sdk }

/** Screen 09 — push registers automatically; status, in-app toggle, action source, last payload. */
@Composable
fun NotificationsScreen(nav: NavController) {
    var inAppEnabled by remember { mutableStateOf(true) }
    var source by remember { mutableStateOf(Source.App) }
    var badgeCount by remember { mutableStateOf<Int?>(null) }
    var handleResult by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val events by EventStore.events.collectAsState()

    val lastPayload = events.firstOrNull { it.source == EventSource.Notification }?.payload

    // Live POST_NOTIFICATIONS state (Android 13+; granted at install below 33). Mirrors the RN
    // Notifications screen: a warning banner + Open-Settings CTA whenever notifications are off.
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var notificationsGranted by remember { mutableStateOf(hasNotificationPermission(context)) }

    // Same runtime request MainActivity fires at launch; here it is re-triggerable from the banner
    // when the permission is still requestable (notDetermined / denied-once).
    val requestPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsGranted = granted
        Toaster.show(
            if (granted) "Notifications enabled" else "Notification permission denied",
            if (granted) ToastTone.Success else ToastTone.Danger,
        )
    }

    // Re-read the status whenever the screen resumes, so the banner clears after the user flips the
    // switch in system Settings and returns to the app.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationsGranted = hasNotificationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // shouldShowRequestPermissionRationale is true only while the OS will still show the prompt; once
    // permanently denied it returns false and Settings is the only route back.
    val canRequest = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !notificationsGranted &&
            context.findActivity()?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    it,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            } == true

    ScreenScaffold(
        title = "Notifications",
        subtitle = "Push status & in-app alerts",
        onBack = { nav.popBackStack() },
    ) {
        if (notificationsGranted) {
            StatusBanner(
                title = "Notifications enabled",
                body = "Push is registered automatically at startup and handled by the platform messaging service.",
                variant = BannerVariant.Ok,
                icon = AppIcon.Notifications,
            )
        } else {
            StatusBanner(
                title = "Notifications are turned off",
                body = "Notifications are disabled for this app, so you won't receive chat replies, call alerts, or other messages. Turn them on to start receiving push.",
                variant = BannerVariant.Warning,
                icon = AppIcon.Notifications,
            )
            if (canRequest) {
                AppButton(
                    title = "Enable notifications",
                    onClick = { requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS) },
                    variant = ButtonVariant.Primary,
                )
            } else {
                AppButton(
                    title = "Open notification settings",
                    onClick = { openNotificationSettings(context) },
                    variant = ButtonVariant.Secondary,
                )
            }
        }

        Section(title = "Preferences") {
            Card {
                row {
                    SwitchRow("In-app notifications", inAppEnabled, {
                        inAppEnabled = it
                        // Turn in-app notification banners on or off.
                        if (it) ZohoSalesIQ.Notification.enableInApp() else ZohoSalesIQ.Notification.disableInApp()
                    }, subtitle = "Show alerts inside the app")
                }
                row {
                    ListRow(
                        "Disable push",
                        subtitle = "unregister this device's FCM token",
                        icon = AppIcon.Notifications,
                        tint = IconTint.Primary,
                        titleTone = TitleTone.Danger,
                        onClick = {
                            ZohoSalesIQ.Notification.disablePush()
                            Toaster.show("Push disabled for this device", ToastTone.Default)
                        })
                }
            }
        }

        Section(title = "Action source") {
            SegmentedControl(
                segments = listOf(Source.App to "App", Source.Sdk to "SDK"),
                selected = source,
                onSelect = {
                    source = it
                    // Choose whether the app or the SDK handles taps on chat notifications.
                    ZohoSalesIQ.Notification.setActionSource(if (it == Source.App) ZohoSalesIQ.ActionSource.APP else ZohoSalesIQ.ActionSource.SDK)
                },
            )
            AppText(
                "Who handles a tap on a chat notification.",
                style = AppType.caption,
                tone = TextTone.Secondary,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
        }

        Section(
            title = "Badge",
            footer = "App-icon unread badge, distinct from the in-chat unread count."
        ) {
            Card {
                row {
                    ListRow(
                        "App icon badge count",
                        value = badgeCount?.toString() ?: "—",
                        icon = AppIcon.Notifications,
                        tint = IconTint.Primary,
                        onClick = {
                            // Read the current app-icon unread badge count (synchronous DB I/O — off the main thread).
                            scope.launch {
                                val count =
                                    withContext(Dispatchers.IO) { ZohoSalesIQ.Notification.getBadgeCount() }
                                badgeCount = count
                                Toaster.show("Badge count: $count", ToastTone.Default)
                            }
                        },
                    )
                }
            }
        }

        Section(title = "Handle sample push") {
            AppButton(
                title = "Handle sample push",
                variant = ButtonVariant.Ghost,
                onClick = {
                    // The flow a host app runs from its FCM onMessageReceived: check whether the
                    // payload is a SalesIQ one, hand it to the SDK, then read back the parsed payload.
                    val data = mapOf("sender" to "salesiq", "type" to "chat")
                    if (ZohoSalesIQ.Notification.isZohoSalesIQNotification(data)) {
                        ZohoSalesIQ.Notification.handle(context, data)
                        ZohoSalesIQ.Notification.getPayload(data) { res ->
                            handleResult = res.toResultString { payload ->
                                jsonOf(
                                    "isZohoSalesIQNotification" to true,
                                    "processed" to true,
                                    "payload" to (payload?.let { it::class.simpleName } ?: "none"),
                                )
                            }
                        }
                        Toaster.show("Push handled", ToastTone.Success)
                    } else {
                        handleResult = jsonOf("isZohoSalesIQNotification" to false)
                        Toaster.show("Not a SalesIQ push (as expected for the sample)")
                    }
                },
            )
            handleResult?.let { ResultBlock(text = it, label = "Last result") }
        }
        Section(title = "Last payload received") {
            ResultBlock(text = lastPayload ?: jsonOf("status" to "No notification received yet"))
        }
    }
}

/**
 * True when the app may post notifications. On Android 13+ (API 33) this reflects the runtime
 * POST_NOTIFICATIONS grant; below API 33 the permission is granted at install.
 */
private fun hasNotificationPermission(context: Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

/**
 * Opens this app's OS notification settings so the user can re-enable notifications after denying
 * the runtime prompt (which the system won't show again). Falls back to the app-details page when
 * the per-app notification screen is unavailable (pre-API 26 or unresolvable).
 */
private fun openNotificationSettings(context: Context) {
    val appDetails = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        .setData(Uri.fromParts("package", context.packageName, null))
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    } else {
        appDetails
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        context.startActivity(appDetails)
    }
}

/** Unwraps the Activity from a (possibly wrapped) Compose Context for permission-rationale checks. */
private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
