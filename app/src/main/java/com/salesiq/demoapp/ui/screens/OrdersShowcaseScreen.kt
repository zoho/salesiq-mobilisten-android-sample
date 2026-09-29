package com.salesiq.demoapp.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.salesiq.demoapp.ui.components.AppButton
import com.salesiq.demoapp.ui.components.AppIcon
import com.salesiq.demoapp.ui.components.AppText
import com.salesiq.demoapp.ui.components.Badge
import com.salesiq.demoapp.ui.components.BadgeTone
import com.salesiq.demoapp.ui.components.ButtonVariant
import com.salesiq.demoapp.ui.components.Card
import com.salesiq.demoapp.ui.components.IconTint
import com.salesiq.demoapp.ui.components.ListRow
import com.salesiq.demoapp.ui.components.ScreenScaffold
import com.salesiq.demoapp.ui.components.Section
import com.salesiq.demoapp.ui.components.TextTone
import com.salesiq.demoapp.ui.components.ToastTone
import com.salesiq.demoapp.ui.components.Toaster
import com.salesiq.demoapp.ui.theme.AppType
import com.zoho.livechat.android.modules.common.ui.entities.PresentOptions
import com.zoho.salesiqembed.ZohoSalesIQ

private data class Order(
    val id: String,
    val statusLabel: String,
    val price: String,
    val faqs: List<String>,
)

private val ORDERS = listOf(
    Order("A-1024", "Delivered", "\$129.00", listOf("Where is my package?", "Start a return")),
    Order("A-1057", "Shipped", "\$58.00", listOf("Change delivery address?")),
)

/** Showcase — customChatId maps each order to its own conversation. */
@Composable
fun OrdersShowcaseScreen(nav: NavController) {
    ScreenScaffold(
        title = "Your orders",
        subtitle = "A conversation per order",
        onBack = { nav.popBackStack() },
        backLabel = "Chat",
    ) {
        AppText(
            "Demonstrates customChatId — the SDK opens (or resumes) the chat mapped to each order id.",
            style = AppType.caption,
            tone = TextTone.Secondary,
            modifier = Modifier.padding(horizontal = 6.dp),
        )

        ORDERS.forEachIndexed { index, order ->
            Section {
                Card {
                    row {
                        ListRow(
                            "Order #${order.id}",
                            subtitle = "${order.statusLabel} · ${order.price}",
                            icon = AppIcon.Homepage,
                            tint = IconTint.Accent,
                            trailing = {
                                Badge(
                                    order.statusLabel,
                                    if (order.statusLabel == "Delivered") BadgeTone.Success else BadgeTone.Primary
                                )
                            },
                        )
                    }
                    order.faqs.forEach { faq ->
                        row {
                            ListRow(
                                faq,
                                icon = AppIcon.Chat,
                                tint = IconTint.Primary,
                                onClick = {
                                    // FAQ chat icon → Chat.start(question = FAQ, customChatId = order id)
                                    ZohoSalesIQ.Chat.start(
                                        faq,
                                        order.id,
                                        null as String?
                                    ) { result ->
                                        if (result.isSuccess) {
                                            Toaster.show("Chat for order #${order.id}", ToastTone.Success)
                                        } else {
                                            failToast("Couldn't start chat", result.error)
                                            // Fallback: open the SDK UI directly on this order's conversation.
                                            ZohoSalesIQ.present(
                                                PresentOptions(
                                                    PresentOptions.Screen.Conversation(
                                                        order.id,
                                                        PresentOptions.Screen.Conversation.SessionType.CHAT,
                                                        PresentOptions.ConversationList.None
                                                    ), false
                                                )
                                            )
                                        }
                                    }
                                },
                            )
                        }
                    }
                    row {
                        AppButton(
                            "Chat about this order",
                            icon = AppIcon.Chat,
                            variant = if (index == 0) ButtonVariant.Primary else ButtonVariant.Secondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                            onClick = {
                                // "Chat about this order" → startWithTrigger(customChatId = order id), no question.
                                // Named args pin this to the (customActionName, customChatId, …) family.
                                ZohoSalesIQ.Chat.startWithTrigger(
                                    customActionName = "order_support",
                                    customChatId = order.id,
                                    departmentName = null
                                ) {
                                    if (!it.isSuccess) {
                                        // Fallback: open the SDK UI directly on this order's conversation.
                                        ZohoSalesIQ.present(
                                            PresentOptions(
                                                PresentOptions.Screen.Conversation(
                                                    order.id,
                                                    PresentOptions.Screen.Conversation.SessionType.CHAT,
                                                    PresentOptions.ConversationList.None
                                                ), false
                                            )
                                        )
                                    }
                                }
                                Toaster.show(
                                    "Opening chat for order #${order.id}",
                                    ToastTone.Success
                                )
                            },
                        )
                    }
                }
            }
        }

        AppText(
            "Chat icon on an FAQ → Chat.start(question, customChatId). \"Chat about this order\" → Chat.startWithTrigger(customActionName, customChatId).",
            style = AppType.caption,
            tone = TextTone.Secondary,
            modifier = Modifier.padding(horizontal = 6.dp),
        )
    }
}
