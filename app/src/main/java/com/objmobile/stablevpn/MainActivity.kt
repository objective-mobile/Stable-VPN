package com.objmobile.stablevpn

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Bundle
import android.os.RemoteException
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.blinkt.openvpn.OpenVpnApi
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.VpnStatus
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {

    private val vpnButtonState = mutableStateOf("Connect")
    private val vpnStatusState = mutableStateOf("DISCONNECTED")

    private var vpnStart = false
    private lateinit var vpnService: OpenVPNService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VpnScreen(vpnButtonState.value, vpnStatusState.value) {
                if (vpnStart) {
                    stopVpn()
                } else {
                    prepareVpn()
                }
            }
        }

        vpnService = OpenVPNService()
        VpnStatus.initLogCache(cacheDir)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun prepareVpn() {
        if (!vpnStart) {
            val intent = VpnService.prepare(this)
            if (intent != null) {
                startActivityForResult(intent, 1)
            } else {
                startVpn()
            }
        }
    }

    private fun startVpn() {
        try {
            val conf = assets.open("client.ovpn")
            val isr = InputStreamReader(conf)
            val br = BufferedReader(isr)
            var config = ""
            var line: String?
            while (br.readLine().also { line = it } != null) {
                config += line + "\n"
            }
            br.close()

            OpenVpnApi.startVpn(this, config, null, null, null)
            vpnStart = true
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun stopVpn() {
        try {
            vpnStart = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            startVpn()
        }
    }
}

@Composable
fun VpnScreen(buttonText: String, statusText: String, onButtonClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Status: $statusText")
        Button(onClick = onButtonClick) {
            Text(text = buttonText)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    VpnScreen("Connect", "DISCONNECTED") {}
}