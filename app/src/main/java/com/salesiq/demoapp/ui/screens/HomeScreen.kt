package com.salesiq.demoapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.salesiq.demoapp.state.EventStore
import com.salesiq.demoapp.state.SDKInitStatus
import com.salesiq.demoapp.state.SettingsStore
import com.salesiq.demoapp.ui.Routes
import com.salesiq.demoapp.ui.components.AppIcon
import com.salesiq.demoapp.ui.components.AppText
import com.salesiq.demoapp.ui.components.Card
import com.salesiq.demoapp.ui.components.HeaderIconButton
import com.salesiq.demoapp.ui.components.IconTint
import com.salesiq.demoapp.ui.components.ListRow
import com.salesiq.demoapp.ui.components.ScreenScaffold
import com.salesiq.demoapp.ui.components.Section
import com.salesiq.demoapp.ui.components.TextTone
import com.salesiq.demoapp.ui.theme.AppType
import com.salesiq.demoapp.ui.theme.LocalAppColors

/** Screen 01 — dashboard for every module; the hero reflects automatic SDK init. */
@Composable
fun HomeScreen(nav: NavController) {
    val c = LocalAppColors.current
    val initStatus by SettingsStore.initStatus.collectAsState()
    val unseen by EventStore.unseen.collectAsState()

    val (heroTitle, heroBody) = when (initStatus) {
        SDKInitStatus.Initialized -> "SDK initialized at launch" to "Brand online · chat & calls available"
        SDKInitStatus.KeysRequired -> "Keys required" to "Add your app key and access key in Settings to initialize"
        SDKInitStatus.Failed -> "Initialization failed" to "Check Settings for details and retry"
        SDKInitStatus.Pending -> "Initializing…" to "The SDK starts automatically at launch"
    }
    val dotColor = when (initStatus) {
        SDKInitStatus.Initialized -> c.secondary
        SDKInitStatus.Failed -> c.danger
        else -> c.accent
    }

    ScreenScaffold(
        title = "Mobilisten",
        subtitle = "SalesIQ SDK showcase",
        headerRight = {
            HeaderIconButton(AppIcon.Settings, { nav.navigate(Routes.SETTINGS) })
        },
    ) {
        Card(separated = false) {
            row {
                Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(dotColor))
                        AppText(text = heroTitle, style = AppType.subhead.copy(fontSize = 13.sp), weight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    }
                    AppText(text = heroBody, style = AppType.caption, tone = TextTone.Secondary)
                }
            }
        }

        Section(title = "Demo app") {
            Card {
                row { ListRow("Zylker store", subtitle = "A shopping app with SalesIQ built in", icon = AppIcon.Homepage, tint = IconTint.Primary, chevron = true, onClick = { nav.navigate(Routes.STORE) }) }
            }
        }

        Section(title = "Engage") {
            Card {
                row { ListRow("Chat", subtitle = "Start & manage conversations", icon = AppIcon.Chat, tint = IconTint.Primary, chevron = true, onClick = { nav.navigate(Routes.CHAT) }) }
                row { ListRow("Calls", subtitle = "Voice calls & history", icon = AppIcon.Calls, tint = IconTint.Secondary, chevron = true, onClick = { nav.navigate(Routes.CALLS) }) }
                row { ListRow("Launcher", subtitle = "Visibility & position", icon = AppIcon.Launcher, tint = IconTint.Accent, chevron = true, onClick = { nav.navigate(Routes.LAUNCHER) }) }
            }
        }

        Section(title = "Audience & content") {
            Card {
                row { ListRow("Visitor", icon = AppIcon.Visitor, tint = IconTint.Secondary, chevron = true, onClick = { nav.navigate(Routes.VISITOR) }) }
                row { ListRow("Knowledge base", icon = AppIcon.KnowledgeBase, tint = IconTint.Accent, chevron = true, onClick = { nav.navigate(Routes.KNOWLEDGE_BASE) }) }
                row { ListRow("Homepage & help center", icon = AppIcon.Homepage, tint = IconTint.Primary, chevron = true, onClick = { nav.navigate(Routes.HOMEPAGE) }) }
            }
        }

        Section(title = "System") {
            Card {
                row { ListRow("Core & configuration", icon = AppIcon.Core, tint = IconTint.Primary, chevron = true, onClick = { nav.navigate(Routes.CORE) }) }
                row { ListRow("Notifications", icon = AppIcon.Notifications, tint = IconTint.Accent, chevron = true, onClick = { nav.navigate(Routes.NOTIFICATIONS) }) }
                row {
                    ListRow(
                        "Events console",
                        icon = AppIcon.Events,
                        tint = IconTint.Secondary,
                        chevron = true,
                        onClick = { nav.navigate(Routes.EVENTS) },
                        trailing = if (unseen > 0) {
                            { com.salesiq.demoapp.ui.components.Badge(if (unseen > 99) "99+" else unseen.toString(), com.salesiq.demoapp.ui.components.BadgeTone.Danger) }
                        } else null,
                    )
                }
            }
        }
    }
}
