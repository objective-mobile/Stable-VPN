package com.objmobile.presentation.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Clickable modifier with ripple effect
 */
@Composable
fun Modifier.clickableWithRipple(onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    return this.clickable(
        interactionSource = interaction, // indication = rememberRipple(
        //     bounded = true,
        //     radius = 100.dp,
        //     color = Color.White.copy(alpha = 0.3f)
        // ),
        onClick = onClick
    )
}

/**
 * Clickable modifier without ripple effect
 */
@Composable
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    return this.clickable(
        interactionSource = interaction, indication = null, onClick = onClick
    )
}