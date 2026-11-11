package com.doudrick.tvbackdrop.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.doudrick.tvbackdrop.ui.theme.TvFocusBorder

/**
 * Modifier designed specifically for Google TV 10-foot remote control experience.
 * Applies scale bounce, vibrant focus ring border, and elevation when focused via D-pad.
 */
@Composable
fun Modifier.tvFocusable(
    shape: Shape = RoundedCornerShape(12.dp),
    focusedScale: Float = 1.06f,
    focusedBorderWidth: Dp = 3.dp,
    focusedBorderColor: Color = TvFocusBorder,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: () -> Unit
): Modifier {
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) focusedScale else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "tv_focus_scale"
    )

    return this
        .scale(scale)
        .shadow(
            elevation = if (isFocused) 16.dp else 2.dp,
            shape = shape,
            clip = false
        )
        .then(
            if (isFocused) {
                Modifier.border(
                    width = focusedBorderWidth,
                    color = focusedBorderColor,
                    shape = shape
                )
            } else {
                Modifier
            }
        )
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
        .focusable(interactionSource = interactionSource)
}
