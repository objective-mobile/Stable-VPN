package com.objmobile.presentation.utils

import androidx.compose.ui.graphics.Color
import com.objmobile.domain.StableVpnStatus
import com.objmobile.presentation.R
import com.objmobile.presentation.model.ButtonUi

/**
 * Extension function to convert VPN status to UI model
 */
fun StableVpnStatus.toUi(): ButtonUi = when (this) {
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