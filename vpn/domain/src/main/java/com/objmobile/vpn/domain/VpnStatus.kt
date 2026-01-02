package com.objmobile.vpn.domain

sealed interface VpnStatus {
    object Disconnected : VpnStatus
    object Connecting : VpnStatus
    object Pause : VpnStatus
    data class Connected(val country: String, val ip: String) : VpnStatus
    data class Error(val message: String) : VpnStatus
}