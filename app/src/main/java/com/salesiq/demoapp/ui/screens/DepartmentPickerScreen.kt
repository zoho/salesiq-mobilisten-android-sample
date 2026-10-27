package com.salesiq.demoapp.ui.screens

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.salesiq.demoapp.state.ConfigStore
import com.salesiq.demoapp.ui.components.AppIcon
import com.salesiq.demoapp.ui.components.Card
import com.salesiq.demoapp.ui.components.ListRow
import com.salesiq.demoapp.ui.components.ScreenScaffold
import com.salesiq.demoapp.ui.components.StateRows
import com.salesiq.demoapp.ui.components.ToastTone
import com.salesiq.demoapp.ui.components.Toaster
import com.salesiq.demoapp.ui.theme.LocalAppColors
import com.zoho.livechat.android.SIQDepartment
import com.zoho.livechat.android.modules.conversations.models.CommunicationMode
import com.zoho.salesiqembed.ZohoSalesIQ

/** Detail — getDepartments feeds a tap-to-select picker (setDepartment). The pick is kept in [ConfigStore]. */
@Composable
fun DepartmentPickerScreen(nav: NavController) {
    val c = LocalAppColors.current
    var departments by remember { mutableStateOf<List<SIQDepartment>>(emptyList()) }
    val selected by ConfigStore.department.collectAsState()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(reloadKey) {
        loading = true
        error = null
        // Fetch the departments configured in the portal. A department that takes only calls is left
        // out, because the pick routes chats.
        ZohoSalesIQ.Conversation.getDepartments { result ->
            if (result.isSuccess) {
                departments = result.data.orEmpty().filter { it.communicationMode != CommunicationMode.CALL }
            } else {
                error = result.error?.message ?: "Failed to load departments"
            }
            loading = false
        }
    }

    fun select(name: String?) {
        ConfigStore.setDepartment(name)
        // Route new chats to the chosen department. With none, SalesIQ routes them by itself.
        ZohoSalesIQ.Chat.setDepartment(name)
        Toaster.show(if (name != null) "Routing to $name" else "No department set", ToastTone.Success)
        nav.popBackStack()
    }

    val checkMark: @Composable () -> Unit = {
        Icon(AppIcon.Check.vector, contentDescription = null, tint = c.primary, modifier = Modifier.size(18.dp))
    }

    ScreenScaffold(
        title = "Department",
        subtitle = "Route new chats to…",
        onBack = { nav.popBackStack() },
        backLabel = "Core",
    ) {
        Card {
            StateRows(
                loading = loading,
                error = error,
                empty = departments.isEmpty(),
                onRetry = { reloadKey++ },
                skeletonRows = 4,
                emptyIcon = AppIcon.Department,
                emptyTitle = "No departments",
                emptySubtitle = "Sign in with real keys to list departments",
            ) {
                row {
                    ListRow(
                        "No department",
                        subtitle = "SalesIQ routes the chat",
                        onClick = { select(null) },
                        trailing = if (selected == null) checkMark else null,
                    )
                }
                departments.forEach { dept ->
                    val name = dept.name ?: dept.displayName ?: "Department"
                    val online = dept.available == true
                    row {
                        ListRow(
                            name,
                            subtitle = if (online) "Online" else "Offline",
                            onClick = { select(name) },
                            trailing = if (selected == name) checkMark else null,
                        )
                    }
                }
            }
        }
    }
}
