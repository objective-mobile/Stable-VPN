@file:OptIn(ExperimentalMaterial3Api::class)

package com.objmobile.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.objmobile.domain.StableVpnStatus
import com.objmobile.presentation.mapper.CountryFlagMapper
import com.objmobile.presentation.ui.theme.StableVPNTheme

@Composable
fun VpnScreen(viewModel: VpnViewModel) {
    val status by viewModel.vpnStatus.collectAsState()
    StableVpnHomeScreen(
        status = status,
        onMainButtonClick = {
            when (status) {
                is StableVpnStatus.Connected -> viewModel.disconnectVpn()
                is StableVpnStatus.Disconnected -> viewModel.connectVpn()
                is StableVpnStatus.Error -> viewModel.connectVpn()
                is StableVpnStatus.Pause -> viewModel.connectVpn()
                is StableVpnStatus.Connecting -> viewModel.disconnectVpn()
            }
        },
        onLocationClick = { /* TODO: Implement location selection */ },
        onSettingsClick = { /* TODO: Implement settings */ })
}

// --- Screen ---
@Composable
fun StableVpnHomeScreen(
    status: StableVpnStatus,
    onMainButtonClick: () -> Unit,
    onLocationClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val bg = Brush.verticalGradient(
        listOf(
            Color(0xFFF8FAFC), Color(0xFFEEF2FF)
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) { // App icon in title bar
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "StableVpn App Icon",
                            modifier = Modifier.size(24.dp),
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            "StableVpn",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1D4ED8)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(20.dp))
                VpnMainButton(
                    status = status, onClick = onMainButtonClick
                )
                Spacer(Modifier.height(40.dp))
                StatusText(status)
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

// --- Main Button ---
@Composable
private fun VpnMainButton(
    status: StableVpnStatus, onClick: () -> Unit
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
    val glowSize =
        buttonSize + 64.dp // Increased glow area // Outer glow container with extra space
    Box(
        contentAlignment = Alignment.Center, modifier = Modifier
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

// --- Status Text block ---
@Composable
private fun StatusText(status: StableVpnStatus) {
    val title: String
    val subtitle: String?
    val titleColor: Color

    when (status) {
        StableVpnStatus.Disconnected -> {
            title = "Disconnected"
            subtitle = "Tap to connect"
            titleColor = Color(0xFF0F172A)
        }

        StableVpnStatus.Connecting -> {
            title = "Connecting…"
            subtitle = "Establishing secure tunnel"
            titleColor = Color(0xFF0F172A)
        }

        is StableVpnStatus.Connected -> {
            title = "Connected"
            subtitle = "${status.country} • ${status.ip}"
            titleColor = Color(0xFF0F172A)
        }

        StableVpnStatus.Pause -> {
            title = "Paused"
            subtitle = "Connection temporarily stopped"
            titleColor = Color(0xFF0F172A)
        }

        is StableVpnStatus.Error -> {
            title = "Connection error"
            subtitle = status.message.ifBlank { "Tap to retry" }
            titleColor = Color(0xFFB91C1C)
        }
    }

    Text(
        text = title, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = titleColor
    )

    if (!subtitle.isNullOrBlank()) {
        Spacer(Modifier.height(6.dp)) // Show flag and country info for connected state
        if (status is StableVpnStatus.Connected) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) { // Country flag icon
                Icon(
                    painter = painterResource(id = CountryFlagMapper.getFlagResource(status.country)),
                    contentDescription = "Flag of ${status.country}",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Unspecified // Don't tint flag icons to preserve colors
                )

                Spacer(modifier = Modifier.width(8.dp)) // Country name and IP
                Text(
                    text = "${status.country} • ${status.ip}",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
            }
        } else { // Regular subtitle for other states
            Text(
                text = subtitle, fontSize = 14.sp, color = Color(0xFF64748B)
            )
        }
    }
}

// --- UI mapping for button ---
private data class ButtonUi(
    val iconRes: Int, val gradientColors: List<Color>, val glowColor: Color
)

private fun StableVpnStatus.toUi(): ButtonUi = when (this) {
    StableVpnStatus.Disconnected -> ButtonUi(
        iconRes = R.drawable.ic_power_settings_new,
        gradientColors = listOf(Color(0xFFD1D5DB), Color(0xFF9CA3AF)),
        glowColor = Color(0xFF9CA3AF)
    )

    StableVpnStatus.Connecting -> ButtonUi(
        iconRes = R.drawable.ic_autorenew,
        gradientColors = listOf(Color(0xFF4FACFE), Color(0xFF00F2FE)),
        glowColor = Color(0xFF4FACFE)
    )

    is StableVpnStatus.Connected -> ButtonUi(
        iconRes = R.drawable.ic_lock,
        gradientColors = listOf(Color(0xFF34D399), Color(0xFF22C55E)),
        glowColor = Color(0xFF22C55E)
    )

    StableVpnStatus.Pause -> ButtonUi(
        iconRes = R.drawable.ic_pause,
        gradientColors = listOf(Color(0xFFFBBF24), Color(0xFFF59E0B)),
        glowColor = Color(0xFFF59E0B)
    )

    is StableVpnStatus.Error -> ButtonUi(
        iconRes = R.drawable.ic_warning,
        gradientColors = listOf(Color(0xFFFB7185), Color(0xFFEF4444)),
        glowColor = Color(0xFFEF4444)
    )
}

// --- Small helper: clickable with ripple effect ---
@Composable
private fun Modifier.clickableWithRipple(onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    return this.clickable(
        interactionSource = interaction, //        indication = rememberRipple(
        //            bounded = true,
        //            radius = 100.dp,
        //            color = Color.White.copy(alpha = 0.3f)
        //        ),
        onClick = onClick
    )
}

// --- Alternative: clickable without ripple (if needed) ---
@Composable
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    return this.clickable(
        interactionSource = interaction, indication = null, onClick = onClick
    )
}

// --- Previews ---
@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun Preview_Disconnected() {
    StableVpnHomeScreen(
        status = StableVpnStatus.Disconnected, onMainButtonClick = {})
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun Preview_Connecting() {
    StableVpnHomeScreen(
        status = StableVpnStatus.Connecting, onMainButtonClick = {})
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun Preview_Connected() {
    StableVpnHomeScreen(
        status = StableVpnStatus.Connected(country = "Germany", ip = "192.168.1.1"),
        onMainButtonClick = {})
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun Preview_Paused() {
    StableVpnHomeScreen(
        status = StableVpnStatus.Pause, onMainButtonClick = {})
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun Preview_Error() {
    StableVPNTheme {
        StableVpnHomeScreen(
            status = StableVpnStatus.Error("Tap to retry"), onMainButtonClick = {})
    }
}