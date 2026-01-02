package com.objmobile.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.objmobile.domain.StableVpnStatus
import com.objmobile.presentation.utils.clickableWithRipple
import com.objmobile.presentation.utils.toUi

@Composable
fun VpnMainButton(
    status: StableVpnStatus, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    val ui = remember(status) { status.toUi() } // Pulse animation for Connecting / Paused
    val infinite = rememberInfiniteTransition(label = "pulse")
    val pulse by infinite.animateFloat(
        initialValue = 0.96f, targetValue = 1.04f, animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulseScale"
    ) // Spinner rotation for Connecting
    val rotation by infinite.animateFloat(
        initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing)
        ), label = "rotation"
    )
    val scale = when (status) {
        StableVpnStatus.Connecting, StableVpnStatus.Pause -> pulse
        else -> 1f
    }
    val glowAlpha = when (status) {
        is StableVpnStatus.Connected -> 0.6f
        StableVpnStatus.Connecting -> 0.4f
        StableVpnStatus.Pause -> 0.3f
        is StableVpnStatus.Error -> 0.5f
        else -> 0.15f
    }
    val buttonSize = 200.dp
    val glowSize = buttonSize + 64.dp // Outer glow container with extra space
    Box(
        contentAlignment = Alignment.Center, modifier = modifier
            .size(glowSize)
            .graphicsLayer {
                shadowElevation = 0f
            }) {
        Box(
            modifier = Modifier
                .size(buttonSize + 48.dp)
                .blur(15.dp)
                .padding(16.dp)
                .background(
                    color = ui.glowColor.copy(alpha = glowAlpha * 0.8f), shape = CircleShape
                )
        ) // Actual button
        Box(
            modifier = Modifier
                .size(buttonSize)
                .scale(scale)
                .shadow(
                    elevation = 8.dp, shape = CircleShape, clip = false
                )
        ) {
            val gradient = Brush.linearGradient(ui.gradientColors)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(gradient)
                    .clickableWithRipple(onClick)
                    .padding(10.dp)
            ) { // Inner ring highlight
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.1f), Color.Transparent
                                )
                            )
                        )
                )
                val iconModifier = Modifier
                    .align(Alignment.Center)
                    .size(56.dp)
                    .then(
                        if (status == StableVpnStatus.Connecting) {
                            Modifier.rotate(rotation)
                        } else Modifier
                    )

                Icon(
                    painter = painterResource(id = ui.iconRes),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = iconModifier
                )
            }
        }
    }
}