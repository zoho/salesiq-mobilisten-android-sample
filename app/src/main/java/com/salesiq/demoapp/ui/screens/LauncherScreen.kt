package com.salesiq.demoapp.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.salesiq.demoapp.ui.Routes
import com.salesiq.demoapp.ui.components.AppButton
import com.salesiq.demoapp.ui.components.AppIcon
import com.salesiq.demoapp.ui.components.AppText
import com.salesiq.demoapp.ui.components.Card
import com.salesiq.demoapp.ui.components.Field
import com.salesiq.demoapp.ui.components.ListRow
import com.salesiq.demoapp.ui.components.ScreenScaffold
import com.salesiq.demoapp.ui.components.Section
import com.salesiq.demoapp.ui.components.SegmentedControl
import com.salesiq.demoapp.ui.components.SwitchRow
import com.salesiq.demoapp.ui.components.TextTone
import com.salesiq.demoapp.ui.components.ToastTone
import com.salesiq.demoapp.ui.components.Toaster
import com.salesiq.demoapp.ui.theme.AppType
import com.zoho.commons.LauncherModes
import com.zoho.commons.LauncherProperties
import com.zoho.salesiqembed.ZohoSalesIQ

private enum class Visibility { Always, ActiveChat, Never }

/** Screen 03 — launcher visibility, drag-to-dismiss, operator image, position. */
@Composable
fun LauncherScreen(nav: NavController) {
    var visibility by remember { mutableStateOf(Visibility.Always) }
    var dragToDismiss by remember { mutableStateOf(true) }
    var operatorImage by remember { mutableStateOf(true) }
    var customLauncher by remember { mutableStateOf(false) }
    var horizontal by remember { mutableStateOf("Right") }
    var vertical by remember { mutableStateOf("Bottom") }
    var minPressDuration by remember { mutableStateOf("500") }

    val copy = when (visibility) {
        Visibility.Always -> "Shown at all times, even without an active conversation."
        Visibility.ActiveChat -> "Only shown while the visitor has an active conversation."
        Visibility.Never -> "The launcher never appears; present the SDK from your own UI."
    }

    ScreenScaffold(
        title = "Launcher",
        subtitle = "How and where the launcher appears",
        onBack = { nav.popBackStack() },
    ) {
        Section(title = "Visibility mode") {
            SegmentedControl(
                segments = listOf(
                    Visibility.Always to "Always",
                    Visibility.ActiveChat to "Active chat",
                    Visibility.Never to "Never",
                ),
                selected = visibility,
                onSelect = { visibility = it },
            )
            AppText(copy, style = AppType.caption, tone = TextTone.Secondary, modifier = Modifier.padding(horizontal = 6.dp))
        }

        Section(title = "Behavior") {
            Card {
                row { SwitchRow("Drag to dismiss", dragToDismiss, { dragToDismiss = it }, subtitle = "Let users swipe the launcher away") }
                row { SwitchRow("Operator image", operatorImage, { operatorImage = it }, subtitle = "Show agent photo in launcher") }
                row { SwitchRow("Custom launcher", customLauncher, { customLauncher = it }, subtitle = "Use your own trigger view") }
            }
        }

        Section(title = "Gesture", footer = "Long-press duration before the drag gesture activates.") {
            Card {
                row { Field("Minimum press duration (ms)", minPressDuration, { minPressDuration = it }, placeholder = "500") }
            }
        }

        Section(title = "Position", footer = "Android only.") {
            Card {
                row { ListRow("Horizontal", value = horizontal, onClick = { horizontal = if (horizontal == "Left") "Right" else "Left" }) }
                row { ListRow("Vertical", value = vertical, onClick = { vertical = if (vertical == "Top") "Bottom" else "Top" }) }
            }
        }

        AppButton("Apply launcher settings", icon = AppIcon.Launcher, onClick = {
            val mode = when (visibility) {
                Visibility.Always -> ZohoSalesIQ.Launcher.VisibilityMode.ALWAYS
                Visibility.ActiveChat -> ZohoSalesIQ.Launcher.VisibilityMode.WHEN_ACTIVE_CHAT
                Visibility.Never -> ZohoSalesIQ.Launcher.VisibilityMode.NEVER
            }
            if (customLauncher) {
                // Apply the visibility rule to your own custom launcher view.
                ZohoSalesIQ.Launcher.setVisibilityModeToCustomLauncher(mode)
            } else {
                // Show the built-in floating launcher with the chosen visibility rule.
                ZohoSalesIQ.Launcher.show(mode)
            }
            // Let users swipe the launcher away.
            ZohoSalesIQ.Launcher.enableDragToDismiss(dragToDismiss)
            // Show the operator's photo on the launcher button.
            ZohoSalesIQ.Chat.showOperatorImageInLauncher(operatorImage)
            val props = LauncherProperties(LauncherModes.FLOATING)
            props.setDirection(if (horizontal == "Left") LauncherProperties.Horizontal.LEFT else LauncherProperties.Horizontal.RIGHT)
            props.setDirection(if (vertical == "Top") LauncherProperties.Vertical.TOP else LauncherProperties.Vertical.BOTTOM)
            // Apply the launcher's on-screen position (corner) settings.
            ZohoSalesIQ.setLauncherProperties(props)
            // Set the long-press duration before the drag gesture activates.
            minPressDuration.trim().toLongOrNull()?.let { ZohoSalesIQ.Launcher.setMinimumPressDuration(it) }
            Toaster.show("Launcher settings applied", ToastTone.Success)
        })
    }
}
