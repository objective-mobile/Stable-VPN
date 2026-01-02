package com.objmobile.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.objmobile.domain.StableVpnStatus
import com.objmobile.presentation.mapper.CountryFlagMapper

@Composable
fun VpnStatusText(
    status: StableVpnStatus, modifier: Modifier = Modifier
) {
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
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        color = titleColor,
        modifier = modifier
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