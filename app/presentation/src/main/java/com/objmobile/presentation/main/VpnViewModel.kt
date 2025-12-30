package com.objmobile.presentation.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.objmobile.countries.domain.CountryRepository
import com.objmobile.domain.StableVpnStatus
import com.objmobile.domain.VpnRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class VpnViewModel(
    private val vpnRepository: VpnRepository, private val countryRepository: CountryRepository
) : ViewModel() {
    val vpnStatus: StateFlow<StableVpnStatus> = vpnRepository.stableVpnStatus.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(1000), StableVpnStatus.Disconnected
    )

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