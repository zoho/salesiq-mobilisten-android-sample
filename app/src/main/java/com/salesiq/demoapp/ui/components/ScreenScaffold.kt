package com.salesiq.demoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salesiq.demoapp.ui.theme.AppType
import com.salesiq.demoapp.ui.theme.LocalAppColors

/**
 * Safe-area page → back affordance → large title 28/600 → subtitle → scrollable
 * content, 16 h-padding, 16 gap between sections.
 */
@Composable
fun ScreenScaffold(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    backLabel: String = "Home",
    headerRight: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val c = LocalAppColors.current
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val scroll = rememberScrollState()

    Column(modifier = modifier.fillMaxSize().background(c.page)) {
        Column(
            modifier = Modifier.padding(
                top = topInset + 6.dp,
                start = 20.dp,
                end = 20.dp,
                bottom = 12.dp,
            ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (onBack != null) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(c.tintPrimary)
                                .clickable(onClick = onBack),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                AppIcon.Back.vector,
                                contentDescription = "Back to $backLabel",
                                tint = c.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    AppText(
                        text = title,
                        style = AppType.largeTitle.copy(fontSize = 27.sp),
                    )
                    if (subtitle != null) {
                        AppText(
                            text = subtitle,
                            style = AppType.subhead,
                            tone = TextTone.Secondary,
                            modifier = Modifier.padding(top = 3.dp),
                        )
                    }
                }
                headerRight?.invoke()
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scroll)
                .padding(start = 16.dp, end = 16.dp, bottom = bottomInset + 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            content()
        }
    }
}

/** Small tinted 38dp square icon button used as [ScreenScaffold] headerRight. */
@Composable
fun HeaderIconButton(icon: AppIcon, onClick: () -> Unit, badgeCount: Int = 0) {
    val c = LocalAppColors.current
    androidx.compose.foundation.layout.Box(contentAlignment = Alignment.TopEnd) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(c.tintPrimary)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon.vector, contentDescription = null, tint = c.primary, modifier = Modifier.size(20.dp))
        }
        if (badgeCount > 0) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(c.danger),
                contentAlignment = Alignment.Center,
            ) {
                AppText(
                    text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                    style = AppType.caption.copy(fontSize = 9.sp),
                    color = androidx.compose.ui.graphics.Color.White,
                )
            }
        }
    }
}
