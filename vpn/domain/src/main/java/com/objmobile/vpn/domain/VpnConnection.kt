package com.objmobile.vpn.domain

import kotlinx.coroutines.flow.Flow

interface VpnConnection {
    fun connectVpn(vpnProfile: VpnProfile)
    suspend fun disconnectVpn()
    fun vpnStatus(): Flow<VpnStatus>
}