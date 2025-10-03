package com.objmobile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun VpnScreen(viewModel: VpnViewModel) {
    val statusText by viewModel.vpnStatus.collectAsState()
    VpnUi(statusText, {
        viewModel.connectVpn()
    }, {
        viewModel.disconnectVpn()
    })
}

@Composable
fun VpnUi(vpnStatusUi: VpnStatusUi, connectUnit: () -> Unit, disconnectUnit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        vpnStatusUi.Show()

        Button(onClick = connectUnit) {
            Text(text = "Connect")
        }

        Button(onClick = disconnectUnit) {
            Text(text = "Disconnect")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    VpnUi(TextStatus("test"), {}, {})
}