package com.salesiq.demoapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.salesiq.demoapp.state.EventSource
import com.salesiq.demoapp.state.EventStore
import com.salesiq.demoapp.ui.Routes
import com.salesiq.demoapp.ui.components.AppIcon
import com.salesiq.demoapp.ui.components.AppText
import com.salesiq.demoapp.ui.components.Card
import com.salesiq.demoapp.ui.components.EventRow
import com.salesiq.demoapp.ui.components.ScreenScaffold
import com.salesiq.demoapp.ui.components.TextTone
import com.salesiq.demoapp.ui.theme.AppType
import com.salesiq.demoapp.ui.theme.LocalAppColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val TIME_FMT = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

/** Screen 10 — live feed of every SDK event across chat, calls, launcher, KB, notifications. */
@Composable
fun EventsScreen(nav: NavController) {
    val c = LocalAppColors.current
    val events by EventStore.events.collectAsState()

    LaunchedEffect(Unit) { EventStore.markSeen() }

    ScreenScaffold(
        title = "Events",
        subtitle = "Real-time SDK event stream",
        onBack = { nav.popBackStack() },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            AppText("${events.size} events", style = AppType.subhead, tone = TextTone.Secondary, weight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.clickable(enabled = events.isNotEmpty()) { EventStore.clear() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Icon(AppIcon.Close.vector, contentDescription = null, tint = c.danger, modifier = Modifier.size(14.dp))
                AppText("Clear", style = AppType.caption, weight = FontWeight.SemiBold, color = c.danger)
            }
        }

        Card {
            if (events.isEmpty()) {
                row {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        AppText("No events yet — interact with the SDK to see them appear here.", style = AppType.subhead, tone = TextTone.Secondary)
                    }
                }
            } else {
                events.forEach { event ->
                    val stripe = when (event.source) {
                        EventSource.Chat -> c.primary
                        EventSource.Calls, EventSource.KnowledgeBase -> c.secondary
                        EventSource.Launcher, EventSource.Notification, EventSource.System -> c.accent
                    }
                    row {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.clickable { nav.navigate(Routes.eventDetail(event.id)) },
                        ) {
                            EventRow(
                                name = event.name,
                                payload = event.payload,
                                time = TIME_FMT.format(Date(event.ts)),
                                stripeColor = stripe,
                            )
                        }
                    }
                }
            }
        }
    }
}
