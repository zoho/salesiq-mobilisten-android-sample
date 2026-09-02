package com.salesiq.demoapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salesiq.demoapp.ui.theme.AppType
import com.salesiq.demoapp.ui.theme.LocalAppColors
import com.salesiq.demoapp.ui.theme.Radius
import com.salesiq.demoapp.ui.theme.Sizing

// ─────────────────────────────────────────────────────────────
// Button — height 48, radius 13, 14.5/600
// ─────────────────────────────────────────────────────────────

enum class ButtonVariant { Primary, Secondary, Ghost, Destructive }

@Composable
fun AppButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    icon: AppIcon? = null,
    enabled: Boolean = true,
) {
    val c = LocalAppColors.current
    data class Look(val bg: Color, val border: Color?, val fg: Color)
    val look = when (variant) {
        ButtonVariant.Primary -> Look(c.primary, null, c.onPrimary)
        ButtonVariant.Secondary -> Look(c.card, c.border, c.primary)
        ButtonVariant.Ghost -> Look(Color.Transparent, null, c.primary)
        ButtonVariant.Destructive -> Look(Color.Transparent, c.danger, c.danger)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(Sizing.buttonHeight)
            .clip(RoundedCornerShape(Radius.control))
            .background(look.bg)
            .then(if (look.border != null) Modifier.border(1.dp, look.border, RoundedCornerShape(Radius.control)) else Modifier)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon.vector, contentDescription = null, tint = look.fg, modifier = Modifier.size(18.dp))
            Box(Modifier.size(8.dp))
        }
        Text(
            text = title,
            style = AppType.body.copy(fontSize = 14.5.sp),
            fontWeight = FontWeight.SemiBold,
            color = look.fg,
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Field / Input — label 11/600 uppercase + value 14.5, inside cards
// ─────────────────────────────────────────────────────────────

@Composable
fun Field(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    autoCapitalize: Boolean = true,
) {
    val c = LocalAppColors.current
    Column(modifier = modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        AppText(
            text = label,
            style = AppType.sectionLabel.copy(letterSpacing = 0.4.sp),
            tone = TextTone.Tertiary,
            uppercase = true,
        )
        Box(modifier = Modifier.padding(top = 4.dp)) {
            if (value.isEmpty() && placeholder != null) {
                Text(
                    text = placeholder,
                    style = AppType.body.copy(fontSize = 14.5.sp),
                    color = c.textTertiary,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = AppType.body.copy(fontSize = 14.5.sp, color = c.textPrimary),
                cursorBrush = SolidColor(c.primary),
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    capitalization = if (autoCapitalize) KeyboardCapitalization.Sentences else KeyboardCapitalization.None,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// SegmentedControl
// ─────────────────────────────────────────────────────────────

@Composable
fun <T> SegmentedControl(
    segments: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.input))
            .background(c.segmentTrack)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        segments.forEach { (value, label) ->
            val isSelected = value == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isSelected) c.segmentThumb else Color.Transparent)
                    .clickable { onSelect(value) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = AppType.subhead.copy(fontSize = 12.5.sp),
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) c.textPrimary else c.textSecondary,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// SwitchRow — ListRow with trailing 44×27 switch
// ─────────────────────────────────────────────────────────────

@Composable
fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: AppIcon? = null,
    tint: IconTint = IconTint.Primary,
    enabled: Boolean = true,
) {
    val c = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Sizing.listRowMinHeight)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        if (icon != null) IconBox(icon = icon, tint = tint)
        Column(modifier = Modifier.weight(1f)) {
            AppText(
                text = title,
                style = AppType.body.copy(fontSize = 14.5.sp),
                weight = FontWeight.Medium,
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
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = c.switchOn,
                checkedBorderColor = c.switchOn,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = c.switchOff,
                uncheckedBorderColor = c.switchOff,
            ),
        )
    }
}
