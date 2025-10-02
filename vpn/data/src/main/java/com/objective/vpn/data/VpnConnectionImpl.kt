package com.objective.vpn.data

import android.content.Context
import android.net.VpnService
import androidx.core.app.ActivityCompat.startActivityForResult
import com.objmobile.vpn.domain.VpnConnection
import com.objmobile.vpn.domain.VpnProfile
import com.objmobile.vpn.domain.VpnStatus
import de.blinkt.openvpn.OpenVpnApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VpnConnectionImpl(
    private val context: Context,
    private val vpnConnectionStorage: VpnConnectionStorage
) : VpnConnection {
    override fun connectVpn(vpnProfile: VpnProfile) {
        OpenVpnApi.startVpn(context, vpnProfile.ovpnConfig, null, null, null)
    }

    override fun disconnectVpn() {
    }

    override fun vpnStatus(): Flow<VpnStatus> =
        vpnConnectionStorage.getVpnStatus().map { it.toVpnStatus() }
}