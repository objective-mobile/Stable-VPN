package com.objmobile.vpn.data.mapper

import com.objmobile.vpn.domain.VpnStatus
import de.blinkt.openvpn.core.ConnectionStatus

/**
 * Mapper to convert OpenVPN ConnectionStatus to domain VpnStatus
 */
object ConnectionStatusMapper {
    /**
     * Maps OpenVPN ConnectionStatus to domain VpnStatus
     * @param connectionStatus The OpenVPN connection status
     * @param country The country name for connected state (optional)
     * @param ip The IP address for connected state (optional)
     * @param errorMessage The error message for error states (optional)
     * @return Mapped VpnStatus
     */
    fun mapToDomain(
        connectionStatus: ConnectionStatus,
        country: String = "",
        ip: String = "",
        errorMessage: String = ""
    ): VpnStatus {
        return when (connectionStatus) {
            ConnectionStatus.LEVEL_CONNECTED -> {
                VpnStatus.Connected(
                    country = country.ifEmpty { "Unknown" },
                    ip = ip.ifEmpty { "0.0.0.0" })
            }

            ConnectionStatus.LEVEL_VPNPAUSED -> {
                VpnStatus.Pause
            }

            ConnectionStatus.LEVEL_CONNECTING_SERVER_REPLIED, ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET, ConnectionStatus.LEVEL_START, ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT -> {
                VpnStatus.Connecting
            }

            ConnectionStatus.LEVEL_AUTH_FAILED -> {
                VpnStatus.Error(
                    message = errorMessage.ifEmpty { "Authentication failed" })
            }

            ConnectionStatus.LEVEL_NONETWORK -> {
                VpnStatus.Error(
                    message = errorMessage.ifEmpty { "No network connection available" })
            }

            ConnectionStatus.LEVEL_NOTCONNECTED, ConnectionStatus.UNKNOWN_LEVEL -> {
                VpnStatus.Disconnected
            }
        }
    }

    /**
     * Maps OpenVPN ConnectionStatus to domain VpnStatus with detailed parameters
     * @param connectionStatus The OpenVPN connection status
     * @param vpnInfo Additional VPN information for connected state
     * @return Mapped VpnStatus
     */
    fun mapToDomain(
        connectionStatus: ConnectionStatus, vpnInfo: VpnConnectionInfo? = null
    ): VpnStatus {
        return when (connectionStatus) {
            ConnectionStatus.LEVEL_CONNECTED -> {
                VpnStatus.Connected(
                    country = vpnInfo?.country ?: "Unknown", ip = vpnInfo?.ip ?: "0.0.0.0"
                )
            }

            ConnectionStatus.LEVEL_VPNPAUSED -> {
                VpnStatus.Pause
            }

            ConnectionStatus.LEVEL_CONNECTING_SERVER_REPLIED, ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET, ConnectionStatus.LEVEL_START, ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT -> {
                VpnStatus.Connecting
            }

            ConnectionStatus.LEVEL_AUTH_FAILED -> {
                VpnStatus.Error(
                    message = vpnInfo?.errorMessage ?: "Authentication failed"
                )
            }

            ConnectionStatus.LEVEL_NONETWORK -> {
                VpnStatus.Error(
                    message = vpnInfo?.errorMessage ?: "No network connection available"
                )
            }

            ConnectionStatus.LEVEL_NOTCONNECTED, ConnectionStatus.UNKNOWN_LEVEL -> {
                VpnStatus.Disconnected
            }
        }
    }
}

/**
 * Data class to hold additional VPN connection information
 */
data class VpnConnectionInfo(
    val country: String = "", val ip: String = "", val errorMessage: String = ""
)

/**
 * Extension function for easier mapping
 */
fun ConnectionStatus.toDomainStatus(
    country: String = "", ip: String = "", errorMessage: String = ""
): VpnStatus {
    return ConnectionStatusMapper.mapToDomain(this, country, ip, errorMessage)
}

/**
 * Extension function with VpnConnectionInfo
 */
fun ConnectionStatus.toDomainStatus(vpnInfo: VpnConnectionInfo? = null): VpnStatus {
    return ConnectionStatusMapper.mapToDomain(this, vpnInfo)
}

/**

 * Reverse mapper to convert domain VpnStatus to OpenVPN ConnectionStatus
 * Note: Some information may be lost in this conversion as ConnectionStatus has fewer states
 */
object VpnStatusMapper {
    /**
     * Maps domain VpnStatus to OpenVPN ConnectionStatus
     * @param vpnStatus The domain VPN status
     * @return Mapped ConnectionStatus
     */
    fun mapToOpenVpn(vpnStatus: VpnStatus): ConnectionStatus {
        return when (vpnStatus) {
            is VpnStatus.Connected -> ConnectionStatus.LEVEL_CONNECTED
            is VpnStatus.Connecting -> ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET
            is VpnStatus.Pause -> ConnectionStatus.LEVEL_VPNPAUSED
            is VpnStatus.Disconnected -> ConnectionStatus.LEVEL_NOTCONNECTED
            is VpnStatus.Error -> { // Determine specific error type based on message content
                when {
                    vpnStatus.message.contains(
                        "auth",
                        ignoreCase = true
                    ) -> ConnectionStatus.LEVEL_AUTH_FAILED

                    vpnStatus.message.contains(
                        "network",
                        ignoreCase = true
                    ) -> ConnectionStatus.LEVEL_NONETWORK

                    else -> ConnectionStatus.LEVEL_NOTCONNECTED
                }
            }
        }
    }
}

/**
 * Extension function for reverse mapping
 */
fun VpnStatus.toOpenVpnStatus(): ConnectionStatus {
    return VpnStatusMapper.mapToOpenVpn(this)
}