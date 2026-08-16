package com.taskflow.app.presentation.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun GlassChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(50)
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) GlassPrimary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
        label = "glass_chip_background"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) GlassPrimary else Color.White.copy(alpha = 0.15f),
        label = "glass_chip_border"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) GlassPrimary else MaterialTheme.colorScheme.onSurface,
        label = "glass_chip_content"
    )

    Text(
        text = label,
        color = contentColor,
        style = MaterialTheme.typography.labelLarge,
        modifier = modifier
            .background(backgroundColor, shape)
            .border(1.dp, borderColor, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .semantics {
                role = Role.RadioButton
                this.selected = selected
            }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
