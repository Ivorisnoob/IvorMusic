package com.ivor.ivormusic.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Helper to get the correct segmented shape based on list position.
 * Implements the Material 3 Expressive segmented list pattern.
 */
@Composable
fun getSegmentedShape(
    index: Int,
    count: Int,
    cornerSize: Dp = 28.dp // Large corner radius for expressive feel
): Shape {
    return when {
        count == 1 -> RoundedCornerShape(cornerSize)
        index == 0 -> RoundedCornerShape(topStart = cornerSize, topEnd = cornerSize)
        index == count - 1 -> RoundedCornerShape(bottomStart = cornerSize, bottomEnd = cornerSize)
        else -> RectangleShape // Middle items are rectangular
    }
}

/**
 * An Expressive List Item wrapper that adds:
 * 1. "Squishy" press animation (spring scale)
 * 2. Segmented shape support
 * 3. Tonal elevation
 */
@Composable
fun ExpressiveListItem(
    modifier: Modifier = Modifier,
    headlineContent: @Composable () -> Unit,
    supportingContent: @Composable (() -> Unit)? = null,
    overlineContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    startContent: (@Composable () -> Unit)? = null, // For consistency with M3 ListItem if needed
    colors: ListItemColors = ListItemDefaults.colors(containerColor = Color.Transparent),
    tonalElevation: Dp = 1.dp,
    shadowElevation: Dp = 0.dp,
    shape: Shape = RectangleShape,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    // 🌟 Interaction Source for "Squishy" animation
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 🌟 Expressive Motion: Spring scale animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ExpressiveScale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale) // Apply the squishy scale
            .clip(shape), // Clip to the segmented shape
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer, // Default container color
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation
    ) {
        // We wrap ListItem in a Box to handle clicks properly with the interaction source
        Box(
            modifier = Modifier.clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                enabled = onClick != null,
                onClick = { onClick?.invoke() }
            )
        ) {
            ListItem(
                headlineContent = headlineContent,
                supportingContent = supportingContent,
                overlineContent = overlineContent,
                leadingContent = leadingContent,
                trailingContent = trailingContent,
                colors = colors,
                modifier = Modifier.fillMaxWidth() // ListItem fills the Surface
            )
        }
    }
}
