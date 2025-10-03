package com.objmobile.domain

import kotlinx.coroutines.flow.Flow

interface VpnRepository {
    fun connectProfile(country: String, opvConfig: String)
    suspend fun disconnectVpn()
    val stableVpnStatus: Flow<StableVpnStatus>
}