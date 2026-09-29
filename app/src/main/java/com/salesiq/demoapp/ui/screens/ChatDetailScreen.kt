package com.salesiq.demoapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.salesiq.demoapp.sdk.DetailCache
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
import com.salesiq.demoapp.ui.components.Section
import com.salesiq.demoapp.ui.components.ToastTone
import com.salesiq.demoapp.ui.components.Toaster
import com.zoho.livechat.android.modules.common.ui.entities.PresentOptions
import com.zoho.salesiqembed.ZohoSalesIQ

/** Detail — opened from Chat › Get chats. Open = present(id), End = endChat(id). */
@Composable
fun ChatDetailScreen(nav: NavController, chatId: String) {
    val chat = remember(chatId) { DetailCache.chat(chatId) }
    var ended by remember { mutableStateOf(false) }

    ScreenScaffold(
        title = "Conversation",
        subtitle = "chatId · $chatId",
        onBack = { nav.popBackStack() },
        backLabel = "Chat",
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Badge(chat?.chatStatus ?: "Unknown", BadgeTone.Success, AppIcon.Check)
            val unread = chat?.unreadCount ?: 0
            if (unread > 0) Badge("$unread unread", BadgeTone.Warning)
            chat?.rating?.let { Badge("Rating $it", BadgeTone.Primary) }
        }

        Section(title = "Details") {
            Card {
                row {
                    ListRow(
                        chat?.attenderName ?: "Unassigned",
                        subtitle = chat?.attenderEmail ?: "—",
                        icon = AppIcon.Visitor,
                        tint = IconTint.Secondary,
                    )
                }
                row { ListRow("Department", value = chat?.departmentName ?: "—") }
                row { Field("Question", chat?.question ?: "", {}, placeholder = "—") }
            }
        }

        AppButton("Open chat", icon = AppIcon.Chat, enabled = !ended, onClick = {
            val options = PresentOptions.Builder()
                .setScreen(PresentOptions.Screen.Conversation(chatId, PresentOptions.Screen.Conversation.SessionType.CHAT))
                .build()
            // Open the SDK UI directly on this conversation.
            ZohoSalesIQ.present(options) { res ->
                if (res.isSuccess) Toaster.show("Chat opened", ToastTone.Success)
                else failToast("Couldn't open chat", res.error)
            }
        })
        AppButton("End chat", variant = ButtonVariant.Destructive, enabled = !ended, onClick = {
            // End this conversation by its chat ID.
            ZohoSalesIQ.Chat.endChat(chatId)
            ended = true
            Toaster.show("Chat ended", ToastTone.Default)
        })
    }
}
