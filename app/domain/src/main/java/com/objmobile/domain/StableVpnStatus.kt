package com.objmobile.domain

sealed interface StableVpnStatus {
    object Disconnected : StableVpnStatus
    object Connecting : StableVpnStatus
    object Pause : StableVpnStatus
    data class Connected(val country: String, val ip: String) : StableVpnStatus
    data class Error(val message: String) : StableVpnStatus
}