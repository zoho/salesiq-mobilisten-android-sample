package com.salesiq.demoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.salesiq.demoapp.ui.theme.AppType
import com.salesiq.demoapp.ui.theme.LocalAppColors
import com.salesiq.demoapp.ui.theme.Radius

// ─────────────────────────────────────────────────────────────
// StatusBanner — tint bg radius 14, icon box + title 13.5/600 + body 12
// ─────────────────────────────────────────────────────────────

enum class BannerVariant { Info, Ok, Warning, Danger }

@Composable
fun StatusBanner(
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    variant: BannerVariant = BannerVariant.Info,
    icon: AppIcon? = null,
) {
    val c = LocalAppColors.current
    val (bg, fg, glyph) = when (variant) {
        BannerVariant.Info -> Triple(c.tintPrimary, c.primary, icon ?: AppIcon.Info)
        BannerVariant.Ok -> Triple(c.tintSecondary, c.secondary, icon ?: AppIcon.Check)
        BannerVariant.Warning -> Triple(c.tintAccent, c.accent, icon ?: AppIcon.Alert)
        BannerVariant.Danger -> Triple(c.tintDanger, c.danger, icon ?: AppIcon.Alert)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(Radius.icon))
                .background(fg.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(glyph.vector, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            AppText(
                text = title,
                style = AppType.subhead.copy(fontSize = 13.5.sp),
                weight = FontWeight.SemiBold,
                color = fg,
            )
            if (body != null) {
                AppText(
                    text = body,
                    style = AppType.caption,
                    tone = TextTone.Secondary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// ResultBlock — resultBlockBg surface, mono 12, keys primary / strings green
// ─────────────────────────────────────────────────────────────

@Composable
fun ResultBlock(
    text: String,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val c = LocalAppColors.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (label != null) {
            AppText(
                text = label,
                style = AppType.sectionLabel,
                tone = TextTone.Tertiary,
                uppercase = true,
                modifier = Modifier.padding(horizontal = 6.dp),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.control))
                .background(c.resultBlockBg)
                .border(1.dp, c.border, RoundedCornerShape(Radius.control))
                .padding(horizontal = 13.dp, vertical = 11.dp),
        ) {
            Text(
                text = highlightJson(text, c.primary, c.secondary, c.textSecondary),
                style = AppType.mono,
            )
        }
    }
}

/** Tints JSON keys with the primary color and string values secondary-green. */
private fun highlightJson(
    text: String,
    keyColor: Color,
    stringColor: Color,
    defaultColor: Color,
): AnnotatedString = buildAnnotatedString {
    val lines = text.split("\n")
    lines.forEachIndexed { index, line ->
        val match = Regex("""^(\s*)("[^"]+")(:\s*)(.*)$""").find(line)
        if (match != null) {
            val (indent, key, colon, rest) = match.destructured
            append(indent)
            withStyle(SpanStyle(color = keyColor)) { append(key) }
            withStyle(SpanStyle(color = defaultColor)) { append(colon) }
            val isString = Regex("""^".*",?$""").matches(rest)
            withStyle(SpanStyle(color = if (isString) stringColor else defaultColor)) { append(rest) }
        } else {
            withStyle(SpanStyle(color = defaultColor)) { append(line) }
        }
        if (index < lines.size - 1) append("\n")
    }
}

// ─────────────────────────────────────────────────────────────
// EventRow — colored stripe + name + payload mono + time
// ─────────────────────────────────────────────────────────────

@Composable
fun EventRow(
    name: String,
    payload: String,
    time: String,
    stripeColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 1.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(stripeColor)
                .size(width = 3.dp, height = 34.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            AppText(
                text = name,
                style = AppType.subhead.copy(fontSize = 13.sp),
                weight = FontWeight.SemiBold,
            )
            AppText(
                text = payload,
                style = AppType.mono.copy(fontSize = 11.5.sp),
                tone = TextTone.Secondary,
                maxLines = 2,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        AppText(text = time, style = AppType.caption, tone = TextTone.Tertiary)
    }
}
