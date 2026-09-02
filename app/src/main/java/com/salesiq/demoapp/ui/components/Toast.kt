package com.salesiq.demoapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salesiq.demoapp.ui.theme.AppType
import com.salesiq.demoapp.ui.theme.LocalAppColors
import com.salesiq.demoapp.ui.theme.Radius
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay

enum class ToastTone { Default, Success, Danger }

data class ToastMessage(val text: String, val tone: ToastTone)

/** Global one-shot toast bus — screens call [Toaster.show] after an SDK action resolves. */
object Toaster {
    val messages = MutableSharedFlow<ToastMessage>(extraBufferCapacity = 4, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    fun show(text: String, tone: ToastTone = ToastTone.Default) {
        messages.tryEmit(ToastMessage(text, tone))
    }
}

/** Mount once near the app root. Renders queued toasts bottom-anchored, auto-dismissed. */
@Composable
fun ToastHost() {
    val c = LocalAppColors.current
    var current by remember { mutableStateOf<ToastMessage?>(null) }
    var visible by remember { mutableStateOf(false) }
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LaunchedEffect(Unit) {
        Toaster.messages.collectLatest { msg ->
            current = msg
            visible = true
            delay(2500)
            visible = false
            delay(250)
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        val msg = current
        AnimatedVisibility(
            visible = visible && msg != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
        ) {
            if (msg != null) {
                val fg = when (msg.tone) {
                    ToastTone.Success -> c.secondary
                    ToastTone.Danger -> c.danger
                    ToastTone.Default -> c.textPrimary
                }
                val glyph = when (msg.tone) {
                    ToastTone.Success -> AppIcon.Check
                    ToastTone.Danger -> AppIcon.Alert
                    ToastTone.Default -> AppIcon.Info
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = bottomInset + 20.dp)
                        .clip(RoundedCornerShape(Radius.control))
                        .background(c.card)
                        .border(1.dp, c.border, RoundedCornerShape(Radius.control))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(glyph.vector, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
                    Text(
                        text = msg.text,
                        style = AppType.body.copy(fontSize = 14.sp),
                        color = c.textPrimary,
                    )
                }
            }
        }
    }
}
