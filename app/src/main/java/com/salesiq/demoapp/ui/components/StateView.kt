package com.salesiq.demoapp.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salesiq.demoapp.ui.theme.LocalAppColors

/**
 * Standardized async content for a fetch/list [Card]. Call inside the card's
 * `row`-scope in place of a hand-rolled `when { loading … empty … else }`.
 * Precedence: loading → error → empty → [content]. Each state is emitted as a
 * [ColumnScopeRows.row] so it sits correctly inside the card.
 */
@Composable
fun ColumnScopeRows.StateRows(
    loading: Boolean,
    error: String?,
    empty: Boolean,
    onRetry: (() -> Unit)? = null,
    skeletonRows: Int = 3,
    emptyIcon: AppIcon = AppIcon.Search,
    emptyTitle: String = "Nothing here yet",
    emptySubtitle: String? = null,
    content: @Composable ColumnScopeRows.() -> Unit,
) {
    when {
        loading -> repeat(skeletonRows) { row { SkeletonRow() } }
        error != null -> row {
            StateBlock(
                icon = AppIcon.Alert,
                danger = true,
                title = "Couldn’t load",
                subtitle = error,
                actionLabel = if (onRetry != null) "Retry" else null,
                onAction = onRetry,
            )
        }
        empty -> row {
            StateBlock(
                icon = emptyIcon,
                danger = false,
                title = emptyTitle,
                subtitle = emptySubtitle,
                actionLabel = if (onRetry != null) "Refresh" else null,
                onAction = onRetry,
            )
        }
        else -> content()
    }
}

/** One shimmering placeholder row: pulsing icon box + two text bars. */
@Composable
private fun SkeletonRow() {
    val c = LocalAppColors.current
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "alpha",
    )
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box34(c.cardAlt, alpha)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Bar(c.cardAlt, alpha, 0.52f, 11.dp)
            Spacer(Modifier.height(7.dp))
            Bar(c.cardAlt, alpha, 0.78f, 9.dp)
        }
    }
}

@Composable
private fun Box34(color: androidx.compose.ui.graphics.Color, alpha: Float) =
    Spacer(
        Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = alpha)),
    )

@Composable
private fun Bar(color: androidx.compose.ui.graphics.Color, alpha: Float, widthFraction: Float, height: androidx.compose.ui.unit.Dp) =
    Spacer(
        Modifier.fillMaxWidth(widthFraction).height(height).clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = alpha)),
    )

/** Centered icon + title + subtitle (+ optional action) — shared by empty/error. */
@Composable
private fun StateBlock(
    icon: AppIcon,
    danger: Boolean,
    title: String,
    subtitle: String?,
    actionLabel: String?,
    onAction: (() -> Unit)?,
) {
    val c = LocalAppColors.current
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(46.dp).clip(RoundedCornerShape(14.dp))
                .background(if (danger) c.tintDanger else c.cardAlt),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon.vector, contentDescription = null, tint = if (danger) c.danger else c.textSecondary, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(title, color = c.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        if (subtitle != null) {
            Spacer(Modifier.height(3.dp))
            Text(subtitle, color = c.textSecondary, fontSize = 12.5.sp, textAlign = TextAlign.Center)
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(14.dp))
            AppButton(title = actionLabel, onClick = onAction, variant = ButtonVariant.Secondary)
        }
    }
}
