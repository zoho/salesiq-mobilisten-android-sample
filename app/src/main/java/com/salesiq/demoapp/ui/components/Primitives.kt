package com.salesiq.demoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salesiq.demoapp.ui.theme.AppColors
import com.salesiq.demoapp.ui.theme.AppType
import com.salesiq.demoapp.ui.theme.LocalAppColors
import com.salesiq.demoapp.ui.theme.Radius
import com.salesiq.demoapp.ui.theme.Sizing
import com.salesiq.demoapp.ui.theme.Spacing

// ─────────────────────────────────────────────────────────────
// Text
// ─────────────────────────────────────────────────────────────

enum class TextTone { Primary, Secondary, Tertiary, Brand, Success, Danger, Accent }

private fun tone(c: AppColors, t: TextTone): Color = when (t) {
    TextTone.Primary -> c.textPrimary
    TextTone.Secondary -> c.textSecondary
    TextTone.Tertiary -> c.textTertiary
    TextTone.Brand -> c.primary
    TextTone.Success -> c.secondary
    TextTone.Danger -> c.danger
    TextTone.Accent -> c.accent
}

/** Token-driven text — the only way type styles are applied in the app. */
@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = AppType.body,
    tone: TextTone = TextTone.Primary,
    weight: FontWeight? = null,
    uppercase: Boolean = false,
    color: Color? = null,
    maxLines: Int = Int.MAX_VALUE,
) {
    val colors = LocalAppColors.current
    Text(
        text = if (uppercase) text.uppercase() else text,
        modifier = modifier,
        style = style,
        color = color ?: tone(colors, tone),
        fontWeight = weight,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

// ─────────────────────────────────────────────────────────────
// IconBox — 34×34 tinted icon box (radius 10) + 19dp colored glyph
// ─────────────────────────────────────────────────────────────

@Composable
fun IconBox(
    icon: AppIcon,
    tint: IconTint = IconTint.Primary,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val bg = when (tint) {
        IconTint.Primary -> c.tintPrimary
        IconTint.Secondary -> c.tintSecondary
        IconTint.Accent -> c.tintAccent
        IconTint.Danger -> c.tintDanger
    }
    val fg = when (tint) {
        IconTint.Primary -> c.primary
        IconTint.Secondary -> c.secondary
        IconTint.Accent -> c.accent
        IconTint.Danger -> c.danger
    }
    Box(
        modifier = modifier
            .size(Sizing.listRowIconBox)
            .clip(RoundedCornerShape(Radius.icon))
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon.vector,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(Sizing.listRowIconGlyph),
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Section + SectionHeader
// ─────────────────────────────────────────────────────────────

@Composable
fun Section(
    title: String? = null,
    footer: String? = null,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        if (title != null) {
            AppText(
                text = title,
                style = AppType.sectionLabel,
                tone = TextTone.Tertiary,
                uppercase = true,
                modifier = Modifier.padding(horizontal = 6.dp),
            )
        }
        content()
        if (footer != null) {
            AppText(
                text = footer,
                style = AppType.caption,
                tone = TextTone.Secondary,
                modifier = Modifier.padding(horizontal = 6.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Card — card bg, 1 border, radius 16; children separated by hairlines
// ─────────────────────────────────────────────────────────────

@Composable
fun Card(
    modifier: Modifier = Modifier,
    separated: Boolean = true,
    content: @Composable ColumnScopeRows.() -> Unit,
) {
    val c = LocalAppColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.card))
            .background(c.card)
            .border(1.dp, c.border, RoundedCornerShape(Radius.card)),
    ) {
        val scope = ColumnScopeRows(separated, c.hairline)
        scope.content()
    }
}

/**
 * Scope used by [Card] so that consecutive rows are separated by a hairline.
 * Call [row] for each child; the first has no divider before it.
 */
class ColumnScopeRows(private val separated: Boolean, private val hairline: Color) {
    private var count = 0

    @Composable
    fun row(content: @Composable () -> Unit) {
        if (separated && count > 0) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(hairline),
            )
        }
        count++
        content()
    }
}

// ─────────────────────────────────────────────────────────────
// Divider
// ─────────────────────────────────────────────────────────────

@Composable
fun Hairline(modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(c.hairline),
    )
}

// ─────────────────────────────────────────────────────────────
// ListRow — [icon] + title/subtitle + trailing value/chevron/control
// ─────────────────────────────────────────────────────────────

enum class TitleTone { Primary, Brand, Danger }

@Composable
fun ListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: AppIcon? = null,
    tint: IconTint = IconTint.Primary,
    value: String? = null,
    chevron: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    titleTone: TitleTone = TitleTone.Primary,
) {
    val c = LocalAppColors.current
    val rowMod = modifier
        .fillMaxWidth()
        .then(
            if (onClick != null && enabled) Modifier.clickable(onClick = onClick) else Modifier,
        )
        .heightIn(min = Sizing.listRowMinHeight)
        .padding(horizontal = 14.dp, vertical = 13.dp)

    Row(
        modifier = rowMod,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        if (icon != null) IconBox(icon = icon, tint = tint)
        Column(modifier = Modifier.weight(1f)) {
            AppText(
                text = title,
                style = AppType.body.copy(fontSize = 14.5.sp),
                weight = FontWeight.Medium,
                tone = when (titleTone) {
                    TitleTone.Primary -> TextTone.Primary
                    TitleTone.Brand -> TextTone.Brand
                    TitleTone.Danger -> TextTone.Danger
                },
                color = if (!enabled) c.textTertiary else null,
            )
            if (subtitle != null) {
                AppText(
                    text = subtitle,
                    style = AppType.caption,
                    tone = TextTone.Secondary,
                    modifier = Modifier.padding(top = 1.dp),
                )
            }
        }
        if (value != null) {
            AppText(
                text = value,
                style = AppType.subhead,
                weight = FontWeight.Medium,
                tone = TextTone.Secondary,
            )
        }
        trailing?.invoke()
        if (chevron) {
            Icon(
                imageVector = AppIcon.ChevronRight.vector,
                contentDescription = null,
                tint = c.textTertiary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Badge / Pill
// ─────────────────────────────────────────────────────────────

enum class BadgeTone { Primary, Success, Warning, Danger }

@Composable
fun Badge(
    label: String,
    tone: BadgeTone = BadgeTone.Primary,
    icon: AppIcon? = null,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val (bg, fg) = when (tone) {
        BadgeTone.Primary -> c.tintPrimary to c.primary
        BadgeTone.Success -> c.tintSecondary to c.secondary
        BadgeTone.Warning -> c.tintAccent to c.accent
        BadgeTone.Danger -> c.tintDanger to c.danger
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.pill))
            .background(bg)
            .padding(horizontal = 11.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (icon != null) {
            Icon(icon.vector, contentDescription = null, tint = fg, modifier = Modifier.size(13.dp))
        }
        AppText(text = label, style = AppType.caption, weight = FontWeight.Medium, color = fg)
    }
}
