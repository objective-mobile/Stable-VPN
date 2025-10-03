package com.objmobile.stablevpn

import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.objmobile.data.BaseVpnRepository
import com.objmobile.presentation.VpnScreen
import com.objmobile.presentation.VpnViewModel
import com.objmobile.stablevpn.ui.theme.StableVPNTheme

class MainActivity : ComponentActivity() {
    val viewModel: VpnViewModel by viewModels(factoryProducer = {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VpnViewModel(BaseVpnRepository(this@MainActivity)) as T
            }
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = VpnService.prepare(this)
        if (intent != null) {
            this.startActivityForResult(intent, 1)
        }
        setContent {
            StableVPNTheme {
                VpnScreen(viewModel)
            }
        }
    }

}