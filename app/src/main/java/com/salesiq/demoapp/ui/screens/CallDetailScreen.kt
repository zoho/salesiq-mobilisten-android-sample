package com.salesiq.demoapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.salesiq.demoapp.sdk.DetailCache
import com.salesiq.demoapp.ui.components.AppButton
import com.salesiq.demoapp.ui.components.AppIcon
import com.salesiq.demoapp.ui.components.Badge
import com.salesiq.demoapp.ui.components.BadgeTone
import com.salesiq.demoapp.ui.components.Card
import com.salesiq.demoapp.ui.components.IconTint
import com.salesiq.demoapp.ui.components.ListRow
import com.salesiq.demoapp.ui.components.ScreenScaffold
import com.salesiq.demoapp.ui.components.Section
import com.salesiq.demoapp.ui.components.ToastTone
import com.salesiq.demoapp.ui.components.Toaster
import com.zoho.livechat.android.modules.common.ui.entities.PresentOptions
import com.zoho.livechat.android.modules.conversations.models.SalesIQConversation
import com.zoho.salesiqembed.ZohoSalesIQ

/** Detail — opened from Calls › Recent. Open conversation = present(Conversation(id, CALL)). */
@Composable
fun CallDetailScreen(nav: NavController, callId: String) {
    val call = remember(callId) { DetailCache.call(callId) }
    val status = (call as? SalesIQConversation.Call)?.status

    ScreenScaffold(
        title = "Call",
        subtitle = "callId · $callId",
        onBack = { nav.popBackStack() },
        backLabel = "Calls",
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val missed = status == SalesIQConversation.Call.Status.MISSED
            Badge(status?.name ?: "Unknown", if (missed) BadgeTone.Warning else BadgeTone.Success)
        }

        Section(title = "Details") {
            Card {
                row {
                    ListRow(
                        call?.attenderName ?: call?.departmentName ?: "Call",
                        subtitle = "Operator: ${call?.attenderName ?: "—"}",
                        icon = AppIcon.Calls,
                        tint = IconTint.Secondary,
                    )
                }
                row { ListRow("Department", value = call?.departmentName ?: "—") }
                row { ListRow("Question", value = call?.question ?: "—") }
                row { ListRow("Queue position", value = (call?.queuePosition ?: 0).toString()) }
            }
        }

        AppButton("Open conversation", icon = AppIcon.Calls, onClick = {
            val options = PresentOptions.Builder()
                .setScreen(PresentOptions.Screen.Conversation(callId, PresentOptions.Screen.Conversation.SessionType.CALL))
                .build()
            // Open the SDK UI directly on this call conversation.
            ZohoSalesIQ.present(options) { res ->
                Toaster.show(
                    if (res.isSuccess) "Conversation opened" else "Failed to open",
                    if (res.isSuccess) ToastTone.Success else ToastTone.Danger,
                )
            }
        })
    }
}
