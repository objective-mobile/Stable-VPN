package com.objective.vpn.data

import com.objmobile.vpn.domain.VpnStatus

data class VpnStatusData(
    val country: String?, val ip: String?, val message: String?, val status: String
) {
    constructor(vpnStatus: VpnStatus) : this(
        (vpnStatus as? VpnStatus.Connected)?.country,
        (vpnStatus as? VpnStatus.Connected)?.ip,
        (vpnStatus as? VpnStatus.Error)?.message,
        when (vpnStatus) {
            is VpnStatus.Connected -> "CONNECTED"
            is VpnStatus.Connecting -> "CONNECTING"
            is VpnStatus.Disconnected -> "DISCONNECTED"
            is VpnStatus.Error -> "ERROR"
            is VpnStatus.Pause -> "PAUSE"
        }
    )

    fun toVpnStatus(): VpnStatus {
        return when (status) {
            "CONNECTED" -> VpnStatus.Connected(
                country ?: throw IllegalStateException("Empty country"),
                ip ?: throw IllegalStateException("Empty country")
            )

            "CONNECTING" -> VpnStatus.Connecting
            "DISCONNECTED" -> VpnStatus.Disconnected
            "ERROR" -> VpnStatus.Error(message ?: throw IllegalStateException("Empty country"))
            "PAUSE" -> VpnStatus.Pause
            else -> throw IllegalStateException("Unknown status")
        }
    }
}