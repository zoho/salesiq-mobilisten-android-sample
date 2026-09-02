package com.salesiq.demoapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.salesiq.demoapp.ui.components.AppButton
import com.salesiq.demoapp.ui.components.AppIcon
import com.salesiq.demoapp.ui.components.Card
import com.salesiq.demoapp.ui.components.Field
import com.salesiq.demoapp.ui.components.ResultBlock
import com.salesiq.demoapp.ui.components.ScreenScaffold
import com.salesiq.demoapp.ui.components.Section
import com.salesiq.demoapp.ui.components.SegmentedControl
import com.salesiq.demoapp.ui.components.ToastTone
import com.salesiq.demoapp.ui.components.Toaster
import com.zoho.salesiq.core.models.SalesIQData
import com.zoho.salesiqembed.ZohoSalesIQ

private enum class FieldType { Display, Secret }

/** Detail — refreshData(SalesIQData): invokes the registered data provider then pushes to server. */
@Composable
fun RefreshDataScreen(nav: NavController) {
    var fieldType by remember { mutableStateOf(FieldType.Display) }
    var conversationId by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }

    ScreenScaffold(
        title = "Refresh data",
        subtitle = "refreshData(SalesIQData)",
        onBack = { nav.popBackStack() },
        backLabel = "Core",
    ) {
        Section(title = "Field type") {
            SegmentedControl(
                segments = listOf(FieldType.Display to "Display fields", FieldType.Secret to "Secret fields"),
                selected = fieldType,
                onSelect = { fieldType = it },
            )
        }

        Section(title = "Conversation", footer = "Invokes the registered conversation data provider, then pushes the resolved fields to the server.") {
            Card {
                row { Field("Conversation ID", conversationId, { conversationId = it }, placeholder = "c_8f2a", autoCapitalize = false) }
            }
        }

        AppButton("Refresh", icon = AppIcon.Refresh, onClick = {
            val id = conversationId.trim()
            if (id.isEmpty()) {
                Toaster.show("Enter a conversation ID first", ToastTone.Danger)
                return@AppButton
            }
            val data = if (fieldType == FieldType.Display) SalesIQData.DisplayFields(id) else SalesIQData.SecretFields(id)
            // Ask the SDK to re-fetch this conversation's fields from the registered data provider.
            ZohoSalesIQ.refreshData(data) { res ->
                result = res.toResultString { jsonOf("success" to true, "type" to fieldType.name.lowercase()) }
                Toaster.show(
                    if (res.isSuccess) "Data refreshed" else "Refresh failed",
                    if (res.isSuccess) ToastTone.Success else ToastTone.Danger,
                )
            }
        })

        result?.let { Section(title = "Response") { ResultBlock(text = it) } }
    }
}
