package com.salesiq.demoapp.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.salesiq.demoapp.state.EventStore
import com.salesiq.demoapp.ui.components.AppButton
import com.salesiq.demoapp.ui.components.AppIcon
import com.salesiq.demoapp.ui.components.ButtonVariant
import com.salesiq.demoapp.ui.components.ResultBlock
import com.salesiq.demoapp.ui.components.ScreenScaffold
import com.salesiq.demoapp.ui.components.Section
import com.salesiq.demoapp.ui.components.ToastTone
import com.salesiq.demoapp.ui.components.Toaster
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DETAIL_TIME_FMT = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

/** Detail — tapping an event expands its full payload with copy. */
@Composable
fun EventDetailScreen(nav: NavController, id: Long) {
    val context = LocalContext.current
    val events by EventStore.events.collectAsState()
    val event = events.firstOrNull { it.id == id }

    ScreenScaffold(
        title = event?.name ?: "Event",
        subtitle = event?.let { "${it.source.name} · ${DETAIL_TIME_FMT.format(Date(it.ts))}" } ?: "No payload",
        onBack = { nav.popBackStack() },
        backLabel = "Events",
    ) {
        val payload = event?.let {
            jsonOf(
                "event" to it.name,
                "source" to it.source.name,
                "time" to DETAIL_TIME_FMT.format(Date(it.ts)),
                "payload" to it.payload,
            )
        } ?: jsonOf("status" to "Event no longer available")

        Section(title = "Payload") { ResultBlock(text = payload) }

        AppButton("Copy payload", variant = ButtonVariant.Secondary, icon = AppIcon.Copy, onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("event", payload))
            Toaster.show("Payload copied", ToastTone.Success)
        })
    }
}
