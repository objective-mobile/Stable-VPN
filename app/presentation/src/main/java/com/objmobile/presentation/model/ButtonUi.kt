package com.objmobile.presentation.model

import androidx.compose.ui.graphics.Color

/**
 * UI model for VPN button appearance
 */
data class ButtonUi(
    val iconRes: Int, val gradientColors: List<Color>, val glowColor: Color
)