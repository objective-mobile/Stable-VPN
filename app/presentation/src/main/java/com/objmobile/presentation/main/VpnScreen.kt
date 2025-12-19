@file:OptIn(ExperimentalMaterial3Api::class)

package com.objmobile.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.objmobile.domain.StableVpnStatus
import com.objmobile.presentation.components.VpnMainButton
import com.objmobile.presentation.components.VpnStatusText
import com.objmobile.presentation.components.VpnTopAppBar
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

@Composable
fun StableVpnHomeScreen(
    status: StableVpnStatus,
    onMainButtonClick: () -> Unit,
    onLocationClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val backgroundGradient = Brush.verticalGradient(
        listOf(
            Color(0xFFF8FAFC), Color(0xFFEEF2FF)
        )
    )

    Scaffold(
        topBar = {
            VpnTopAppBar()
        }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
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
                VpnStatusText(status = status)
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

// --- Previews ---
@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun Preview_Disconnected() {
    StableVPNTheme {
        StableVpnHomeScreen(
            status = StableVpnStatus.Disconnected, onMainButtonClick = {})
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun Preview_Connecting() {
    StableVPNTheme {
        StableVpnHomeScreen(
            status = StableVpnStatus.Connecting, onMainButtonClick = {})
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun Preview_Connected() {
    StableVPNTheme {
        StableVpnHomeScreen(
            status = StableVpnStatus.Connected(country = "Germany", ip = "192.168.1.1"),
            onMainButtonClick = {})
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun Preview_Paused() {
    StableVPNTheme {
        StableVpnHomeScreen(
            status = StableVpnStatus.Pause, onMainButtonClick = {})
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun Preview_Error() {
    StableVPNTheme {
        StableVpnHomeScreen(
            status = StableVpnStatus.Error("Tap to retry"), onMainButtonClick = {})
    }
}