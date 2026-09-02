package com.salesiq.demoapp.ui.screens

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.salesiq.demoapp.ui.components.AppIcon
import com.salesiq.demoapp.ui.components.Card
import com.salesiq.demoapp.ui.components.ListRow
import com.salesiq.demoapp.ui.components.ScreenScaffold
import com.salesiq.demoapp.ui.components.StateRows
import com.salesiq.demoapp.ui.components.ToastTone
import com.salesiq.demoapp.ui.components.Toaster
import com.salesiq.demoapp.ui.theme.LocalAppColors
import com.zoho.livechat.android.SIQDepartment
import com.zoho.livechat.android.listeners.DepartmentListener
import com.zoho.salesiqembed.ZohoSalesIQ

/** Detail — getDepartments feeds a tap-to-select picker (setDepartment). */
@Composable
fun DepartmentPickerScreen(nav: NavController) {
    val c = LocalAppColors.current
    var departments by remember { mutableStateOf<List<SIQDepartment>>(emptyList()) }
    var selected by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(reloadKey) {
        loading = true
        error = null
        // Fetch the list of chat departments configured in the portal.
        ZohoSalesIQ.Chat.getDepartments(object : DepartmentListener {
            override fun onSuccess(list: ArrayList<SIQDepartment>) {
                departments = list
                loading = false
            }

            override fun onFailure(code: Int, message: String?) {
                error = message ?: "Failed to load departments"
                loading = false
            }
        })
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
                departments.forEach { dept ->
                    val name = dept.name ?: dept.displayName ?: "Department"
                    val online = dept.available == true
                    row {
                        ListRow(
                            name,
                            subtitle = if (online) "Online" else "Offline",
                            onClick = {
                                selected = name
                                // Route the next new chat to the chosen department.
                                ZohoSalesIQ.Chat.setDepartment(name)
                                nav.previousBackStackEntry?.savedStateHandle?.set("selectedDepartment", name)
                                Toaster.show("Routing to $name", ToastTone.Success)
                                nav.popBackStack()
                            },
                            trailing = if (selected == name) {
                                { Icon(AppIcon.Check.vector, contentDescription = null, tint = c.primary, modifier = Modifier.size(18.dp)) }
                            } else null,
                        )
                    }
                }
            }
        }
    }
}
