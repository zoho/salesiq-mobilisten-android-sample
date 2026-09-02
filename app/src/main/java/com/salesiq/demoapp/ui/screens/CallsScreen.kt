package com.salesiq.demoapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.salesiq.demoapp.sdk.DetailCache
import com.salesiq.demoapp.ui.Routes
import com.salesiq.demoapp.ui.components.AppButton
import com.salesiq.demoapp.ui.components.AppIcon
import com.salesiq.demoapp.ui.components.Badge
import com.salesiq.demoapp.ui.components.BadgeTone
import com.salesiq.demoapp.ui.components.ButtonVariant
import com.salesiq.demoapp.ui.components.Card
import com.salesiq.demoapp.ui.components.Field
import com.salesiq.demoapp.ui.components.IconTint
import com.salesiq.demoapp.ui.components.ListRow
import com.salesiq.demoapp.ui.components.ScreenScaffold
import com.salesiq.demoapp.ui.components.StateRows
import com.salesiq.demoapp.ui.components.Section
import com.salesiq.demoapp.ui.components.SegmentedControl
import com.salesiq.demoapp.ui.components.SwitchRow
import com.salesiq.demoapp.ui.components.ToastTone
import com.salesiq.demoapp.ui.components.Toaster
import com.zoho.livechat.android.modules.conversations.models.SalesIQConversation
import com.zoho.salesiq.mobilisten.calls.apis.ZohoSalesIQCalls

private enum class ViewMode { FullScreen, Floating }

private val CALL_COMPONENTS = listOf(
    ZohoSalesIQCalls.CallComponent.OperatorName to "Operator name",
    ZohoSalesIQCalls.CallComponent.OperatorImage to "Operator image",
    ZohoSalesIQCalls.CallComponent.PreChatForm to "Pre-chat form",
    ZohoSalesIQCalls.CallComponent.QueuePosition to "Queue position",
)

/** Screen 06 — active call, view mode, titles, component visibility, reply messages, history. */
@Composable
fun CallsScreen(nav: NavController) {
    var callState by remember { mutableStateOf<ZohoSalesIQCalls.SalesIQCallState?>(null) }
    var viewMode by remember { mutableStateOf(ViewMode.FullScreen) }
    var recent by remember { mutableStateOf<List<SalesIQConversation>>(emptyList()) }
    var recentLoading by remember { mutableStateOf(true) }
    var recentError by remember { mutableStateOf<String?>(null) }
    var onlineTitle by remember { mutableStateOf("") }
    var offlineTitle by remember { mutableStateOf("") }
    var replyMessages by remember { mutableStateOf("On my way, Call back later, In a meeting") }

    val componentState = remember {
        mutableStateMapOf(
            ZohoSalesIQCalls.CallComponent.OperatorName to true,
            ZohoSalesIQCalls.CallComponent.OperatorImage to true,
            ZohoSalesIQCalls.CallComponent.PreChatForm to false,
            ZohoSalesIQCalls.CallComponent.QueuePosition to true,
        )
    }

    fun refreshRecent() {
        recentLoading = true
        recentError = null
        // Fetch the visitor's past calls (call history).
        ZohoSalesIQCalls.getList { res ->
            if (res.isSuccess) {
                val list = (res.data ?: emptyList()).take(8)
                recent = list
                DetailCache.calls = list
            } else {
                recentError = "Failed to load recent calls"
            }
            recentLoading = false
        }
    }

    LaunchedEffect(Unit) { refreshRecent() }

    DisposableEffect(Unit) {
        val listener: (ZohoSalesIQCalls.SalesIQCallState) -> Unit = { state -> callState = state }
        // Subscribe to live call-state updates (ringing, connected, ended).
        ZohoSalesIQCalls.addOnCallStateChangeCallback(listener)
        // Unsubscribe when the screen leaves to avoid leaks.
        onDispose { ZohoSalesIQCalls.removeOnCallStateChangeCallback(listener) }
    }

    val active = callState?.status?.isCallActive == true
    val statusLabel = callState?.status?.name ?: "No active call"

    ScreenScaffold(
        title = "Calls",
        subtitle = "Voice calls with your operators",
        onBack = { nav.popBackStack() },
    ) {
        Section(title = "Active call") {
            Card {
                row {
                    ListRow(
                        "Support line",
                        subtitle = statusLabel,
                        icon = AppIcon.Calls,
                        tint = if (active) IconTint.Secondary else IconTint.Primary,
                        trailing = { Badge(if (active) "Live" else "Idle", if (active) BadgeTone.Success else BadgeTone.Primary) },
                    )
                }
                row {
                    SegmentedControl(
                        segments = listOf(ViewMode.FullScreen to "Full screen", ViewMode.Floating to "Floating"),
                        selected = viewMode,
                        onSelect = {
                            viewMode = it
                            // Switch the call UI between full-screen and a small floating window.
                            if (it == ViewMode.FullScreen) ZohoSalesIQCalls.enterFullScreenMode() else ZohoSalesIQCalls.enterFloatingViewMode()
                        },
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppButton("Start call", icon = AppIcon.Calls, modifier = Modifier.weight(1f), onClick = {
                // Place an outgoing call to an operator.
                ZohoSalesIQCalls.start(null, true, null) { res ->
                    Toaster.show(
                        if (res.isSuccess) "Call started" else "Failed to start call",
                        if (res.isSuccess) ToastTone.Success else ToastTone.Danger,
                    )
                    refreshRecent()
                }
            })
            AppButton("End", variant = ButtonVariant.Destructive, modifier = Modifier.weight(1f), onClick = {
                // Hang up the current call.
                ZohoSalesIQCalls.end { res ->
                    Toaster.show(if (res.isSuccess) "Call ended" else "Failed to end call", if (res.isSuccess) ToastTone.Default else ToastTone.Danger)
                    refreshRecent()
                }
            })
        }

        // ── Configuration ─────────────────────────────────────────────────
        Section(title = "Configuration") {
            Card {
                row { Field("Online title", onlineTitle, { onlineTitle = it }, placeholder = "On a call") }
                row { Field("Offline title", offlineTitle, { offlineTitle = it }, placeholder = "Call back") }
                row { Field("Reply messages · Android", replyMessages, { replyMessages = it }, placeholder = "comma-separated quick replies") }
            }
            AppButton("Apply call configuration", variant = ButtonVariant.Secondary, onClick = {
                // Set the call screen's title for the online and offline states.
                ZohoSalesIQCalls.setTitle(onlineTitle.ifBlank { "On a call" }, offlineTitle.ifBlank { "Call back" })
                val messages = replyMessages.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                // Provide the quick-reply messages a visitor can send to decline a call.
                if (messages.isNotEmpty()) ZohoSalesIQCalls.setReplyMessages(messages)
                Toaster.show("Call configuration applied", ToastTone.Success)
            })
        }

        // ── Visible components ────────────────────────────────────────────
        Section(title = "Visible components") {
            Card {
                CALL_COMPONENTS.forEach { (component, title) ->
                    row {
                        SwitchRow(title, componentState[component] ?: false, {
                            componentState[component] = it
                            // Show or hide one element of the call screen (operator name, image, etc.).
                            ZohoSalesIQCalls.setVisibility(component, it)
                        })
                    }
                }
            }
        }

        // ── Recent calls ──────────────────────────────────────────────────
        Section(title = "Recent calls") {
            Card {
                StateRows(
                    loading = recentLoading,
                    error = recentError,
                    empty = recent.isEmpty(),
                    onRetry = { refreshRecent() },
                    skeletonRows = 3,
                    emptyIcon = AppIcon.Calls,
                    emptyTitle = "No recent calls yet",
                    emptySubtitle = "Completed calls will appear here",
                ) {
                    recent.forEach { conversation ->
                        val callStatus = (conversation as? SalesIQConversation.Call)?.status
                        row {
                            ListRow(
                                conversation.attenderName ?: conversation.departmentName ?: "Call",
                                subtitle = callStatus?.name ?: conversation.question ?: "",
                                icon = AppIcon.Calls,
                                tint = if (callStatus == SalesIQConversation.Call.Status.MISSED) IconTint.Danger else IconTint.Secondary,
                                chevron = true,
                                onClick = { conversation.id?.let { nav.navigate(Routes.callDetail(it)) } },
                            )
                        }
                    }
                }
            }
        }
    }
}
