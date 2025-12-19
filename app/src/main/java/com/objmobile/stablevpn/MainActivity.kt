package com.objmobile.stablevpn

import android.app.Activity
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.objmobile.data.BaseVpnRepository
import com.objmobile.presentation.main.VpnScreen
import com.objmobile.presentation.main.VpnViewModel
import com.objmobile.presentation.permissions.PermissionState
import com.objmobile.presentation.permissions.PermissionsScreen
import com.objmobile.presentation.permissions.PermissionsViewModel
import com.objmobile.stablevpn.ui.theme.StableVPNTheme

class MainActivity : ComponentActivity() {
    private val vpnViewModel: VpnViewModel by viewModels(factoryProducer = {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VpnViewModel(BaseVpnRepository(this@MainActivity)) as T
            }
        }
    })
    private val permissionsViewModel: PermissionsViewModel by viewModels(factoryProducer = {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PermissionsViewModel(
                    permissionChecker = { isVpnPermissionGranted() }) as T
            }
        }
    })

    // Register for VPN permission result
    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            permissionsViewModel.setPermissionGranted()
        } else {
            permissionsViewModel.setPermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            StableVPNTheme {
                AppContent(
                    vpnViewModel = vpnViewModel,
                    permissionsViewModel = permissionsViewModel,
                    onRequestPermission = { requestVpnPermission() })
            }
        }
    }

    private fun isVpnPermissionGranted(): Boolean {
        return VpnService.prepare(this) == null
    }

    private fun requestVpnPermission() {
        permissionsViewModel.setPermissionRequesting()
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else { // Permission already granted
            permissionsViewModel.setPermissionGranted()
        }
    }
}

@Composable
private fun AppContent(
    vpnViewModel: VpnViewModel,
    permissionsViewModel: PermissionsViewModel,
    onRequestPermission: () -> Unit
) {
    val permissionState by permissionsViewModel.permissionState.collectAsState()

    when (permissionState) {
        is PermissionState.Granted -> {
            VpnScreen(vpnViewModel)
        }

        else -> {
            PermissionsScreen(
                viewModel = permissionsViewModel,
                onPermissionGranted = { // This will be handled by the ViewModel state change
                },
                onRequestPermission = onRequestPermission
            )
        }
    }
}