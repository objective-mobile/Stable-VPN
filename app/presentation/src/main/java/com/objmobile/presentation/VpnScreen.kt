package com.objmobile.presentation

import android.R.attr.onClick
import android.content.Context
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
fun VpnScreen(viewModel: VpnViewModel, contetext: Context) {
    val statusText by viewModel.vpnStatus.collectAsState()
    VpnUi("Connect", statusText) {
        viewModel.connectVpn()
    }
}

@Composable
fun VpnUi(buttonText: String, vpnStatusUi: VpnStatusUi, onButtonClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        vpnStatusUi.Show()
        Button(onClick = onButtonClick) {
            Text(text = buttonText)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    VpnUi("Connect", TextStatus("test")) {}
}