package com.objmobile.stablevpn

import android.Manifest
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.objmobile.countries.data.datasource.FirestoreCountryDataSource
import com.objmobile.countries.data.repository.FirebaseCountryRepository
import com.objmobile.data.BaseVpnRepository
import com.objmobile.data.PermissionFactory
import com.objmobile.domain.AdvertisingConfiguration
import com.objmobile.domain.AdvertisingUnit
import com.objmobile.domain.PermissionState
import com.objmobile.presentation.BuildConfig
import com.objmobile.presentation.InterstitialAdScreen
import com.objmobile.presentation.main.VpnScreen
import com.objmobile.presentation.main.VpnViewModel
import com.objmobile.presentation.permissions.PermissionsScreen
import com.objmobile.presentation.permissions.PermissionsViewModel
import com.objmobile.presentation.permissions.PermissionsViewModelFactory
import com.objmobile.stablevpn.BuildConfig.BANNER_ID
import com.objmobile.stablevpn.BuildConfig.INTERSTITIAL_ID
import com.objmobile.stablevpn.BuildConfig.IS_ADVERTISING
import com.objmobile.stablevpn.ui.theme.StableVPNTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : ComponentActivity() {
    private val vpnViewModel: VpnViewModel by viewModels(factoryProducer = {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VpnViewModel(
                    BaseVpnRepository(this@MainActivity), FirebaseCountryRepository(
                        FirestoreCountryDataSource(
                            FirebaseFirestore.getInstance()
                        )
                    ),
                    BANNER_ID, IS_ADVERTISING,
                ) as T
            }
        }
    })

    private val permissionsViewModel: PermissionsViewModel by viewModels(factoryProducer = {
        val permissionManager = PermissionFactory.createPermissionManager(this@MainActivity)
        PermissionsViewModelFactory(permissionManager)
    })

    // Register for VPN permission result
    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            permissionsViewModel.setVpnPermissionGranted() // After VPN permission is granted, check notification permission
            requestNotificationPermissionIfNeeded()
        } else {
            permissionsViewModel.setPermissionDenied()
        }
    }

    // Register for notification permission result
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            permissionsViewModel.setNotificationPermissionGranted()
        } else {
            permissionsViewModel.setPermissionDenied()
        }
    }
    private val advertisingScreen by lazy {
        InterstitialAdScreen(
            this, AdvertisingConfiguration(
                isDebug = BuildConfig.DEBUG,
                showing = false,
                enableAd = IS_ADVERTISING,
                advertisingUnit = AdvertisingUnit(
                    INTERSTITIAL_ID,
                    adUnitDebugId = "ca-app-pub-3940256099942544/1033173712"
                )
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        advertisingScreen
        vpnViewModel.interstitialConfiguration.onEach {
            advertisingScreen.show()
        }.launchIn(lifecycleScope)
        setContent {
            StableVPNTheme {
                AppContent(
                    vpnViewModel = vpnViewModel,
                    permissionsViewModel = permissionsViewModel,
                    onRequestPermission = { requestPermissions() })
            }
        }
    }

    private fun requestPermissions() {
        permissionsViewModel.setPermissionRequesting() // First request VPN permission
        val vpnIntent = VpnService.prepare(this.applicationContext)
        if (vpnIntent != null) {
            try {
                vpnPermissionLauncher.launch(vpnIntent)
            } catch (e: Exception) {
                Log.d(
                    "MainActivity",
                    "requestPermissions: error ${e.message}"
                ) // Handle any potential issues with launching VPN permission dialog
                permissionsViewModel.setPermissionDenied()
            }
        } else { // VPN permission already granted, check notification permission
            permissionsViewModel.setVpnPermissionGranted()
            requestNotificationPermissionIfNeeded()
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                permissionsViewModel.setNotificationPermissionGranted()
            }
        } else { // No notification permission needed on older versions
            permissionsViewModel.setNotificationPermissionGranted()
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