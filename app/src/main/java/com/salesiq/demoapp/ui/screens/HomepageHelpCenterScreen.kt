package com.salesiq.demoapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.salesiq.demoapp.ui.components.AppButton
import com.salesiq.demoapp.ui.components.AppIcon
import com.salesiq.demoapp.ui.components.Card
import com.salesiq.demoapp.ui.components.Field
import com.salesiq.demoapp.ui.components.IconTint
import com.salesiq.demoapp.ui.components.ResultBlock
import com.salesiq.demoapp.ui.components.ScreenScaffold
import com.salesiq.demoapp.ui.components.Section
import com.salesiq.demoapp.ui.components.SwitchRow
import com.salesiq.demoapp.ui.components.ToastTone
import com.salesiq.demoapp.ui.components.Toaster
import com.zoho.salesiqembed.ZohoSalesIQ

private data class WidgetRow(
    val widget: ZohoSalesIQ.Homepage.Widget,
    val title: String,
    val icon: AppIcon,
    val tint: IconTint
)

private val WIDGETS = listOf(
    WidgetRow(ZohoSalesIQ.Homepage.Widget.CHAT, "Chat", AppIcon.Chat, IconTint.Primary),
    WidgetRow(ZohoSalesIQ.Homepage.Widget.CALL, "Call", AppIcon.Calls, IconTint.Secondary),
    WidgetRow(
        ZohoSalesIQ.Homepage.Widget.ARTICLES,
        "Articles",
        AppIcon.KnowledgeBase,
        IconTint.Accent
    ),
    WidgetRow(ZohoSalesIQ.Homepage.Widget.FAQS, "FAQs", AppIcon.KnowledgeBase, IconTint.Accent),
    WidgetRow(
        ZohoSalesIQ.Homepage.Widget.PREVIOUS_CONVERSATIONS,
        "Previous conversation",
        AppIcon.Events,
        IconTint.Primary
    ),
    WidgetRow(
        ZohoSalesIQ.Homepage.Widget.IMAGE_CARD,
        "Image card",
        AppIcon.Article,
        IconTint.Secondary
    ),
    WidgetRow(
        ZohoSalesIQ.Homepage.Widget.VIDEO_CARD,
        "Video card",
        AppIcon.Article,
        IconTint.Accent
    ),
)

/** Screen 08 — homepage widgets visibility + the AI/agent-backed Help Center. */
@Composable
fun HomepageHelpCenterScreen(nav: NavController) {
    var homepageEnabled by remember { mutableStateOf(true) }
    val widgetState = remember {
        mutableStateMapOf(
            ZohoSalesIQ.Homepage.Widget.CHAT to true,
            ZohoSalesIQ.Homepage.Widget.CALL to true,
            ZohoSalesIQ.Homepage.Widget.ARTICLES to true,
            ZohoSalesIQ.Homepage.Widget.FAQS to true,
            ZohoSalesIQ.Homepage.Widget.PREVIOUS_CONVERSATIONS to false,
            ZohoSalesIQ.Homepage.Widget.IMAGE_CARD to false,
            ZohoSalesIQ.Homepage.Widget.VIDEO_CARD to false,
        )
    }
    var question by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }

    ScreenScaffold(
        title = "Homepage",
        subtitle = "Widgets & the help center",
        onBack = { nav.popBackStack() },
    ) {
        Section(title = "Homepage") {
            Card {
                row {
                    SwitchRow("Enable homepage", homepageEnabled, {
                        homepageEnabled = it
                        // Turn the SDK's home/landing screen on or off.
                        ZohoSalesIQ.Homepage.setEnabled(it)
                    }, subtitle = "Show the SDK landing view")
                }
            }
        }

        Section(title = "Widgets") {
            Card {
                WIDGETS.forEach { row ->
                    row {
                        SwitchRow(row.title, widgetState[row.widget] ?: false, {
                            widgetState[row.widget] = it
                            // Show or hide one widget (chat, call, articles, etc.) on the home screen.
                            ZohoSalesIQ.Homepage.setVisibility(row.widget, it)
                        }, icon = row.icon, tint = row.tint)
                    }
                }
            }
        }

        Section(title = "Help center") {
            Card {
                row {
                    Field(
                        "Question",
                        question,
                        { question = it },
                        placeholder = "Ask anything about the product"
                    )
                }
            }
            AppButton("Ask help center", icon = AppIcon.Help, onClick = {
                // Open the AI-backed Help Center, optionally pre-filled with a question.
                ZohoSalesIQ.HelpCenter.ask(question.ifBlank { " " }) { res ->
                    result = res.toResultString { jsonOf("action" to "ask", "status" to "opened") }
                    if (res.isSuccess) Toaster.show("Help center opened", ToastTone.Success)
                    else failToast("Couldn't open help center", res.error)
                }
            })
        }

        result?.let { ResultBlock(text = it, label = "Last result") }
    }
}
