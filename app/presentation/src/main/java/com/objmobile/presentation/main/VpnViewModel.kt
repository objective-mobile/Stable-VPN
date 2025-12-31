package com.objmobile.presentation.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.objmobile.countries.domain.CountryRepository
import com.objmobile.domain.AdvertisingConfiguration
import com.objmobile.domain.AdvertisingUnit
import com.objmobile.domain.StableVpnStatus
import com.objmobile.domain.VpnRepository
import com.objmobile.presentation.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class VpnViewModel(
    private val vpnRepository: VpnRepository, private val countryRepository: CountryRepository
) : ViewModel() {
    val vpnStatus: StateFlow<StableVpnStatus> = vpnRepository.stableVpnStatus.onEach { status ->
        if (status is StableVpnStatus.Connected || status is StableVpnStatus.Error) {
            mutableInterstitialConfiguration.emit(Unit)
        }
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(1000), StableVpnStatus.Disconnected
    )
    val bannerConfiguration: StateFlow<AdvertisingConfiguration> = MutableStateFlow(
        AdvertisingConfiguration(
            isDebug = BuildConfig.DEBUG,
            advertisingUnit = AdvertisingUnit("ca-app-pub-8487249106338936/7396398341")
        )
    )
    private val mutableInterstitialConfiguration = MutableSharedFlow<Unit>()
    val interstitialConfiguration = mutableInterstitialConfiguration.asSharedFlow()

    @OptIn(ExperimentalAtomicApi::class)
    private val loading = AtomicBoolean(false)

    @OptIn(ExperimentalAtomicApi::class)
    fun connectVpn() {
        viewModelScope.launch(Dispatchers.IO) {
            if (!loading.load()) {
                loading.store(true)
                try {
                    countryRepository.available().let { country ->
                        vpnRepository.connectProfile(country.countryName, country.countryConfig)
                    }
                } catch (e: Exception) {
                    Log.d("VpnViewModel", "connectVpn: ${e.message}")
                } finally {
                    loading.store(false)
                }
            }
        }
    }

    fun disconnectVpn() {
        viewModelScope.launch(Dispatchers.IO) {
            vpnRepository.disconnectVpn()
        }
    }
}